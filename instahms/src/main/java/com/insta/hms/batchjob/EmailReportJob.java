package com.insta.hms.batchjob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.EmailTemplateMaster.EmailTemplateMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.ScheduledEmailableReport.ScheduledEmailableReportDAO;
import com.insta.hms.scheduledreport.BuiltinEmailReportProvider;
import com.insta.hms.scheduledreport.CustomEmailReportProvider;
import com.insta.hms.scheduledreport.EmailAttachmentDataSource;
import com.insta.hms.scheduledreport.EmailReportProviderInterface;
import com.insta.hms.scheduledreport.FavouriteEmailReportProvider;
import com.insta.hms.scheduledreport.SendMail;

import jlibs.core.util.regex.TemplateMatcher;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailReportJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(EmailReportJob.class);

  private String params;

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  @SuppressWarnings("unchecked")
  @Transactional
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    String debugMode = logger.isDebugEnabled() ? "yes" : "no";
    String schema = getSchema();
    String params = getParams();
    String[] paramStrings = params.split(",");
    String event = paramStrings[0];
    String workDir = paramStrings[1];
    logger.info("Sending " + event + " EmailReport");

    // RequestContext.setConnectionDetails(new String[]{null,null,schema});
    String[] dbSchema = new String[] { null, null, schema, "", "0", "" };
    RequestContext.setConnectionDetails(dbSchema);
    // AppInit.setRootRealPath(workDir);

    if (debugMode.equalsIgnoreCase("yes")) {
      logger.debug("Running in debug mode, will not send emails");
      // System.out.println("Using host: " + host + ", database: " + database + "; schema: " +
      // schema);
    }

    Date fromDate = null;
    Date toDate = null;
    String emailSubject = event + " Report: ";

    List<BasicDynaBean> reports = null;
    GenericPreferencesDTO prefs = null;
    String mailFromAddress = null;
    try {
      prefs = GenericPreferencesDAO.getGenericPreferences();
      mailFromAddress = prefs.getHospMailID();
      if (mailFromAddress.equals("")) {
        mailFromAddress = "ScheduledReports";
      }

      reports = new ScheduledEmailableReportDAO().getReportsForTrigger(event);

    } catch (Exception exception) {
      logger.error(exception.getMessage());
      throw new JobExecutionException(exception.getMessage());
    }

    for (BasicDynaBean report : reports) {
      String reportId = (String) report.get("report_id");
      String subEvent = (String) report.get("subevent");
      String userParams = (String) report.get("params");
      String outputMode = (String) report.get("output_mode");
      String reportType = (String) report.get("report_type");
      String reportName = null;

      try {
        Date[] dates = getReportPeriod(subEvent);
        fromDate = dates[0];
        toDate = dates[1];
        String reportDate = getReportDate(fromDate, toDate, subEvent);
        java.sql.Date sqlFromdate = null;
        java.sql.Date sqlTodate = null;
        if (!subEvent.equals("") && !subEvent.equals("Report Default")) {
          sqlFromdate = new java.sql.Date(fromDate.getTime());
          sqlTodate = new java.sql.Date(toDate.getTime());
        }

        Map paramMap = new HashMap();
        paramMap.putAll(ConversionUtils.flatten(ConversionUtils.getParameterMap(userParams)));
        setDateParams(paramMap, sqlFromdate, sqlTodate);
        String contentType;
        String ext;

        if (outputMode.equals("csv")) {
          contentType = "application/csv";
          ext = ".csv";
        } else if (outputMode.equals("text")) {
          contentType = ("text/plain");
          ext = ".txt";
        } else {
          contentType = "application/pdf";
          ext = ".pdf";
        }
        EmailReportProviderInterface provider = null;
        provider = getEmailReportProvider(reportType);

        /*
         * Process the report and get the bytes
         */
        reportName = provider.getReportName(reportId);
        logger.debug("Generating: " + reportName + " (" + reportType + ")");

        byte[] bytes = provider.getReportBytes(reportId, outputMode, paramMap);

        if (bytes == null) {
          logger.debug("Provider returned null bytes for report: " + reportId);
          continue;
        }

        if (!debugMode.equalsIgnoreCase("yes")) {

          EmailTemplateMasterDAO templDao = new EmailTemplateMasterDAO();
          BasicDynaBean templ = templDao.getTemplate("Scheduled Email Reports Template");

          String subjectTemplate = (String) templ.get("subject");
          String messageTemplate = (String) templ.get("mail_message");

          String subject = getFormattedContent(subjectTemplate, prefs.getHospitalName(), reportDate,
              reportName, event, subEvent);
          String message = getFormattedContent(messageTemplate, prefs.getHospitalName(), reportDate,
              reportName, event, subEvent);

          sendEmailReport(contentType, reportName, ext, bytes, report, subject, message,
              mailFromAddress);
        } else {
          writeEmailAsFileString(workDir, reportName, ext, bytes);
        }

      } catch (Exception exception) {
        logger.error("Generating Report For : " + reportName + " Failed :" + exception);
      }
    }
  }

  private Date[] getReportPeriod(String subEvent) {

    Date fromDate = null;
    Date toDate = null;
    if (subEvent.equalsIgnoreCase("Yesterday")) {
      Date yesterDay = DateUtil.getYesterDay();
      fromDate = yesterDay;
      toDate = yesterDay;

    } else if (subEvent.equalsIgnoreCase("Last Week")) {
      Calendar cal = Calendar.getInstance();
      int lastWeekNumber = cal.get(Calendar.WEEK_OF_YEAR) - 1;
      fromDate = DateUtil.getFirstDayInWeek(lastWeekNumber);
      toDate = DateUtil.getLastDayInWeek(lastWeekNumber);

    } else if (subEvent.equalsIgnoreCase("Last Month")) {
      Calendar calendar = Calendar.getInstance();
      //Calendar months are 0 indexed
      int lastMonth = calendar.get(Calendar.MONTH);
      fromDate = DateUtil.getFirstDayInMonth(lastMonth);
      toDate = DateUtil.getLastDayInMonth(lastMonth);

    } else if (subEvent.equalsIgnoreCase("Month till date")) {
      fromDate = DateUtil.getFirstDayInMonth();
      toDate = new Date();

    } else if (subEvent.equalsIgnoreCase("Year till date")) {
      Date today = new Date();
      fromDate = DateUtil.getFirstDayOfYear(today);
      toDate = today;
    }

    return new Date[] { fromDate, toDate };
  }

  private String getReportDate(Date fromDate, Date toDate, String subEvent) {
    DateUtil dateUtil = new DateUtil();
    String reportDate = null;
    if (subEvent.equalsIgnoreCase("Yesterday")) {
      reportDate = dateUtil.getDateFormatter().format(fromDate);
    } else if (!subEvent.equals("") && !subEvent.equals("Report Default")) {
      reportDate = "(" + dateUtil.getDateFormatter().format(fromDate) + " to "
          + dateUtil.getDateFormatter().format(toDate) + ")";
    }
    return reportDate;
  }

  private EmailReportProviderInterface getEmailReportProvider(String reportType) {
    EmailReportProviderInterface provider = null;
    if (reportType.equalsIgnoreCase("F")) {
      // favourite reports
      provider = new FavouriteEmailReportProvider();
      // todo: how was this used earlier?
      // paramMap.put("reportNameToUse", report.get("report_name"));

    } else if (reportType.equalsIgnoreCase("C")) {
      // custom reports
      provider = new CustomEmailReportProvider();

    } else {
      provider = new BuiltinEmailReportProvider();
    }
    return provider;
  }

  private void sendEmailReport(String contentType, String reportName, String ext, byte[] bytes,
      BasicDynaBean report, String subject, String message, String mailFromAddress)
      throws Exception {

    EmailAttachmentDataSource datasource = new EmailAttachmentDataSource();
    datasource.setContentType(contentType);
    datasource.setName(reportName + ext);
    datasource.setData(bytes);
    datasource.setMailIds(report.get("email_id").toString().split(","));

    String success = SendMail.sendMail(datasource, mailFromAddress, subject, message);
    if (success.equals("success")) {
      logger
          .debug(new Date() + " : " + datasource.getName() + " : mail has been sent successfully.");
    } else {
      logger.debug(new Date() + " : " + datasource.getName() + " : mail Send Failed.");
    }
  }

  private void writeEmailAsFileString(String workDir, String reportName, String ext, byte[] bytes)
      throws Exception {
    File basedir = new File(workDir);
    basedir.mkdirs();
    String filename = workDir + File.separator + reportName.replace(' ', '_') + ext;
    FileOutputStream file = new FileOutputStream(new File(filename));
    file.write(bytes);
    file.flush();
    file.close();
    // System.out.println(new Date() + ": " + reportId + " Saved to " + filename);
  }

  /**
   * Sets the date params.
   *
   * @param paramMap the param map
   * @param from     the from
   * @param to       the to
   */
  @SuppressWarnings("unchecked")
  public static void setDateParams(Map paramMap, java.sql.Date from, java.sql.Date to) {

    if (from != null) {
      paramMap.put("fromdate", from);
      paramMap.put("fromDate", from);
      Timestamp fromDateTime = new Timestamp(from.getTime());
      paramMap.put("fromDateTime", fromDateTime);
    }

    if (to != null) {
      paramMap.put("todate", to);
      paramMap.put("toDate", to);
      Timestamp toDateTime = new Timestamp(to.getTime() + (24 * 60 * 60 - 1) * 1000); // 23:59
      paramMap.put("toDateTime", toDateTime);
      paramMap.put("reportDate", to);
    }
  }

  private String getFormattedContent(String templateContent, String hospotalName, String reportDate,
      String reportName, String event, String subEvent) throws Exception {

    TemplateMatcher matcher = new TemplateMatcher("${", "}");
    Map<String, String> tokens = new HashMap<String, String>();
    tokens.put("hospital", hospotalName);
    tokens.put("reportdate", reportDate);
    tokens.put("reportname", reportName);
    tokens.put("reportperiod", event);
    tokens.put("subevent", subEvent);

    String content = matcher.replace(templateContent, tokens);
    return content;
  }
}
