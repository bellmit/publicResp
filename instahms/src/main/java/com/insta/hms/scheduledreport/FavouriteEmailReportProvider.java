package com.insta.hms.scheduledreport;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FavouriteReportDAO;
import com.insta.hms.common.StdCsvReportEngine;
import com.insta.hms.common.StdPdfReportEngine;
import com.insta.hms.common.StdReportDesc;
import com.insta.hms.common.StdReportDescJsonProvider;
import com.insta.hms.common.StdReportDescProvider;
import com.insta.hms.common.StdReportEngine;
import com.insta.hms.common.StdReportParams;
import com.insta.hms.common.StdTextReportEngine;
import com.insta.hms.customreports.CustomReportsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 * A email report provider is responsible for listing the reports available
 * for emailing, and also to generate the report when requested for.
 */
public class FavouriteEmailReportProvider implements EmailReportProviderInterface {

  FavouriteReportDAO favDao = new FavouriteReportDAO();

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#listAvailableReports()
   */
  public List listAvailableReports() throws SQLException {
    List reportBeans = favDao.getReportIdsNames();
    // we need to return a listMap.
    return ConversionUtils.listBeanToListMap(reportBeans);
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#getReportBytes
   * (java.lang.String, java.lang.String, java.util.Map)
   */
  public byte[] getReportBytes(String reportIdStr, String format, Map params) throws Exception {

    int reportId = Integer.parseInt(reportIdStr);
    DynaBean favBean = favDao.findByKey("report_id", reportId);
    StdReportDesc desc = null;

    String customSrxmlName = (String) favBean.get("custom_report_name");
    String actionId = (String) favBean.get("action_id");

    // if custom_report_name is present, then it is a custom report, otherwise,
    // it is a screen based report
    if (customSrxmlName != null && !customSrxmlName.equals("")) {
      StdReportDescJsonProvider provider = new StdReportDescJsonProvider();
      // remove .srxml at the end
      String reportname = customSrxmlName.replaceAll("\\.srjs$", "");
      CustomReportsDAO reportsdao = new CustomReportsDAO();
      DynaBean report = reportsdao.findByKey("report_name", reportname);
      if (report == null) {
        throw new IllegalArgumentException("Report " + reportname + " not found");
      }
      String reportMetadata = report.get("report_metadata").toString();
      desc = provider.getReportDescForString(reportMetadata);

    } else if (actionId != null && !actionId.equals("")) {
      /*
       * Each actionId can have a different provider: fetch the report_desc and the class that
       * provides the descriptor for the action ID
       */
      String[] reportInfo = StrutsDescParser.getStdReportInfo(actionId);
      String reportDesc = reportInfo[0];
      String providerClass = reportInfo[1];
      if (providerClass == null) {
        providerClass = "com.insta.hms.common.StdReportDescJsonProvider";
      }
      StdReportDescProvider provider = (StdReportDescProvider) Class.forName(providerClass)
          .newInstance();
      desc = provider.getReportDesc(reportDesc);
    } else {
      return null;
    }

    String queryParams = favBean.get("query_params").toString();
    Map reportParamMap = ConversionUtils.getParameterMap(queryParams.replaceFirst("\\?", "&"));

    if (params.get("fromDate") != null && params.get("toDate") != null) {
      // the date range is specified in the email config, use that instead of report
      // default
      java.sql.Date fromDate = (java.sql.Date) params.get("fromDate");
      java.sql.Date toDate = (java.sql.Date) params.get("toDate");
      reportParamMap.put("fromDate",
          new String[] { DateUtil.formatDate(new java.util.Date(fromDate.getTime())) });
      reportParamMap.put("toDate",
          new String[] { DateUtil.formatDate(new java.util.Date(toDate.getTime())) });
      reportParamMap.put("selDateRange", new String[] { "cstm" });
    }

    StdReportParams stdParams = new StdReportParams(reportParamMap, desc);

    StdReportEngine eng = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    if (format.equals("pdf")) {
      eng = new StdPdfReportEngine(desc, stdParams, baos);
    } else if (format.equals("csv")) {
      Writer sw = new BufferedWriter(new OutputStreamWriter(baos));
      eng = new StdCsvReportEngine(desc, stdParams, sw);
    } else if (format.equals("text")) {
      Writer sw = new BufferedWriter(new OutputStreamWriter(baos));
      eng = new StdTextReportEngine(desc, stdParams, sw);
    }

    eng.writeReport();
    return baos.toByteArray();
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#getReportName(java.lang.String)
   */
  public String getReportName(String reportIdStr) throws SQLException {
    int reportId = Integer.parseInt(reportIdStr);
    BasicDynaBean report = favDao.findByKey("report_id", reportId);
    return (String) report.get("report_title");
  }

}
