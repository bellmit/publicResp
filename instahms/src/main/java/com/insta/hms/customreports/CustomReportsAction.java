package com.insta.hms.customreports;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import au.com.bytecode.opencsv.CSVWriter;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.common.ScreenRightsHelper;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FavReportHelper;
import com.insta.hms.common.FavouriteReportDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.ReportJob;
import com.insta.hms.common.StdReportDesc;
import com.insta.hms.common.StdReportDescJsonProvider;
import com.insta.hms.common.StdReportParams;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.jobs.JobService;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * The Class CustomReportsAction.
 */
public class CustomReportsAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CustomReportsAction.class);

  /** The redis template. */
  private static RedisTemplate<String, Object> redisTemplate 
      = (RedisTemplate) ApplicationContextProvider
        .getApplicationContext().getBean("redisTemplate");

  /** The job service. */
  private static JobService jobService = JobSchedulingService.getJobService();
  
  private static final GenericDAO customReportVariablesDAO =
      new GenericDAO("custom_report_variables");

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    Map<LISTING, Object> listingParameter = ConversionUtils
        .getListingParameter(request.getParameterMap());
    CustomReportsDAO dao = new CustomReportsDAO();
    int roleId = (Integer) request.getSession(false).getAttribute("roleId");
    request.setAttribute("pagedlist",
        dao.listReports(listingParameter, roleId));

    return mapping.findForward("list");
  }

  /**
   * Show.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward show(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    String reportId = request.getParameter("id");

    GenericDAO reportsdao = new GenericDAO("custom_reports");
    request.setAttribute("report",
        reportsdao.findByKey(Constants.REPORT_ID, Integer.parseInt(reportId)));

    request.setAttribute("reportvars",
        customReportVariablesDAO.findAllByKey(Constants.REPORT_ID, Integer.parseInt(reportId)));

    return mapping.findForward("show");
  }

  /**
   * Delete.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward delete(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    String reportId = request.getParameter("id");

    CustomReportsDAO reportsdao = new CustomReportsDAO();
    reportsdao.deleteReport(Integer.parseInt(reportId));
    FlashScope flash = FlashScope.getScope(request);
    flash.put("msg", "Custom Report Deleted");

    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("listRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Run report.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws Exception
   *           the exception
   */
  public ActionForward runReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    int reportId = Integer.parseInt(request.getParameter("id"));

    CustomReportsDAO reportsdao = new CustomReportsDAO();
    DynaBean report = reportsdao.findByKey(Constants.REPORT_ID, reportId);
    List<BasicDynaBean> reportVariables =
        customReportVariablesDAO.findAllByKey(Constants.REPORT_ID, reportId);
    List<DynaBean> subReports = reportsdao.getSubReports(reportId);

    String type = (String) report.get("report_type");
    if ("jrxml".equals(type)) {
      return runCustomJrxmlReport(mapping, request, response, report,
          reportVariables, subReports);
    } else if ("srjs".equals(type)) {
      return runCustomSrjsReport(mapping, request, response, report,
          reportVariables, subReports);
    } else if ("ftl".equals(type)) {
      return runCustomFtlReport(mapping, request, response, report,
          reportVariables, subReports);
    } else if ("csv".equals(type)) {
      return runCustomCsvReport(mapping, request, response, report,
          reportVariables, subReports);
    }
    return null;
  }

  /**
   * Run custom ftl report.
   *
   * @param mapping
   *          the mapping
   * @param request
   *          the request
   * @param response
   *          the response
   * @param report
   *          the report
   * @param reportVariables
   *          the report variables
   * @param subReports
   *          the sub reports
   * @return the action forward
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   */
  private ActionForward runCustomFtlReport(ActionMapping mapping,
      HttpServletRequest request, HttpServletResponse response, DynaBean report,
      List<BasicDynaBean> reportVariables, List<DynaBean> subReports)
      throws ParseException, IOException, SQLException, TemplateException,
      DocumentException {

    String metadata = (String) report.get(Constants.REPORT_METADATA);
    String reportName = report.get(Constants.REPORT_NAME).toString();

    HashMap<String, Object> reportParams = new HashMap<>();

    for (BasicDynaBean var : reportVariables) {
      String variable = (String) var.get(Constants.REPORT_VAR);
      String variableValue = request.getParameter(variable);
      if (var.get(Constants.REPORT_VAR_TYPE).equals("D")) {
        reportParams.put(variable, DateUtil.parseDate(variableValue));
      } else {
        reportParams.put(variable, variableValue);
      }
    }

    FtlReportGenerator fg = new FtlReportGenerator(reportName,
        new StringReader(metadata));
    fg.setReportParams(reportParams);

    String format = request.getParameter("_format");
    reportParams.put("_format", format);
    if (format != null && format.equals("csv")) {
      response.setContentType("text/csv");
      fg.process(response.getWriter());
    } else {
      response.setContentType("application/pdf");
      fg.runPdfReport(response.getOutputStream());
    }
    return null;
  }

  /**
   * Run custom jrxml report.
   *
   * @param mapping
   *          the mapping
   * @param request
   *          the request
   * @param response
   *          the response
   * @param report
   *          the report
   * @param reportVariables
   *          the report variables
   * @param subReports
   *          the sub reports
   * @return the action forward
   */
  private ActionForward runCustomJrxmlReport(ActionMapping mapping,
      HttpServletRequest request, HttpServletResponse response, DynaBean report,
      List<BasicDynaBean> reportVariables, List<DynaBean> subReports) {

    HttpSession session = request.getSession(false);
    String hospital = (String) session.getAttribute("sesHospitalId");
    ServletContext sc = session.getServletContext();

    String metadata = report.get(Constants.REPORT_METADATA).toString();
    long timestamp = ((java.sql.Timestamp) report.get("timestamp")).getTime();
    String reportname = report.get(Constants.REPORT_NAME).toString();
    String jasperPath = "";
    try {
      jasperPath = compileReport(sc, hospital, reportname, metadata, timestamp);
    } catch (JRException exp) {
      logger.info("complile Report JR exception." + exp.getMessage());
    }

    for (DynaBean bean : subReports) {
      String subreportMetadata = bean.get(Constants.REPORT_METADATA).toString();
      String subreportName = bean.get(Constants.REPORT_NAME).toString();
      try {
        compileReport(sc, hospital, subreportName, subreportMetadata,
            timestamp);
      } catch (JRException exp) {
        logger.info("comple report JR exception " + exp.getMessage());
      }
    }

    HashMap<String, Object> reportParams = new HashMap<>();
    reportParams.put("SUBREPORT_DIR",
        sc.getRealPath(Constants.REPORT_BASE_DIR + "/" + hospital));

    for (BasicDynaBean var : reportVariables) {
      String variable = var.get(Constants.REPORT_VAR).toString();
      Object variableValue = request.getParameter(variable);
      if (var.get(Constants.REPORT_VAR_TYPE).equals("D")) {
        try {
          variableValue = DateUtil.parseDate(variableValue.toString());
        } catch (ParseException exp) {
          logger.info(exp.getMessage());
        }
      }
      reportParams.put(variable, variableValue);
    }
    // DEFAULT_SECONDS_60 is the timeout
    Connection con = DataBaseUtil.getConnection(Constants.DEFAULT_SECONDS_60);
    try (OutputStream os = response.getOutputStream()) {
      logger.debug("Loading report");
      InputStream is = JRLoader.getFileInputStream(jasperPath);
      response.setContentType("application/pdf");
      logger.debug("Running report");
      JasperRunManager.runReportToPdfStream(is, os, reportParams, con);
      logger.debug("Done printing report.");
    } catch (IOException | JRException exp) {
      logger.info(exp.getMessage());
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return null;
  }

  /**
   * Run custom srjs report.
   *
   * @param mapping
   *          the mapping
   * @param request
   *          the request
   * @param response
   *          the response
   * @param report
   *          the report
   * @param reportVariables
   *          the report variables
   * @param subReports
   *          the sub reports
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws SAXException
   *           the SAX exception
   * @throws Exception
   *           the exception
   */
  private ActionForward runCustomSrjsReport(ActionMapping mapping,
      HttpServletRequest request, HttpServletResponse response, DynaBean report,
      List<BasicDynaBean> reportVariables, List<DynaBean> subReports)
      throws IOException, SQLException, ParseException, SAXException,
      Exception {

    String reportMetadata = report.get(Constants.REPORT_METADATA).toString();
    StdReportDescJsonProvider newp = new StdReportDescJsonProvider();
    StdReportDesc newdesc = newp.getReportDescForString(reportMetadata);
    request.setAttribute("reportDesc", newdesc);

    HashMap<String, List<String>> filterValues = 
        new LinkedHashMap<String, List<String>>();
    HashMap<String, Boolean> allowNull = new HashMap<>();
    HashMap<String, Boolean> notHrzGrpble = new HashMap<>();
    HashMap<String, String> filterNames = new HashMap<>();
    HashMap<String, String> filterTypes = new HashMap<>();
    for (String fieldName : newdesc.getFilterFieldNames()) {
      StdReportDesc.Field field = newdesc.getField(fieldName);
      String query = field.getAllowedValuesQuery();
      filterNames.put(fieldName, field.getDisplayName());
      filterTypes.put(fieldName, field.getDataType());
      if (StringUtils.isEmpty(query)) {
        filterValues.put(fieldName, field.getAllowedValues());
      }
      allowNull.put(fieldName, field.getAllowNull());

      if ((field.getFilterable())
          && (field.getAllowedValuesQuery() == null 
          && (field.getAllowedValues() == null
          || field.getAllowedValues().isEmpty()))) {
        notHrzGrpble.put(fieldName, field.getFilterable());
      }
    }

    HashMap<String, Integer> fieldWidths = new HashMap<>();
    JSONSerializer js = new JSONSerializer().exclude(Constants.STRING_CLASS);
    String reportName = report.get(Constants.REPORT_NAME).toString();
    request.setAttribute("filterOnlyNamesJSON",
        js.deepSerialize(newdesc.getFilterOnlyFields()));
    request.setAttribute("filterValuesJSON", js.deepSerialize(filterValues));
    request.setAttribute(Constants.SRJS_FILE, reportName + ".srjs");
    request.setAttribute("filterDisplayNamesJSON",
        js.deepSerialize(filterNames));
    request.setAttribute("filterTypesJSON", js.deepSerialize(filterTypes));
    request.setAttribute("allowNullJSON", js.deepSerialize(allowNull));
    request.setAttribute("fieldWidthsJSON", js.deepSerialize(fieldWidths));
    request.setAttribute("defaultShowFieldsJSON",
        js.deepSerialize(newdesc.getDefaultShowFields()));
    request.setAttribute("notHrzGrpbleJSON", js.deepSerialize(notHrzGrpble));
    request.setAttribute("CustomReportIDsJSON", js.deepSerialize(ConversionUtils
        .listBeanToListMap(CustomReportsDAO.getCustomReportNameAndIDs())));
    request.setAttribute("favRepTitlesJSON", js.deepSerialize(ConversionUtils
        .listBeanToListMap(FavouriteReportDAO.getAllFavReportTitles())));
    request.setAttribute(Constants.REPT_DESC_FILE, mapping.getProperty("report_desc"));
    request.setAttribute("fieldNamesSorted", newdesc.getFieldNamesSorted());
    request.setAttribute("isCustom", true);
    return mapping.findForward("getScreen");
  }

  /**
   * Run custom csv report.
   *
   * @param mapping
   *          the mapping
   * @param request
   *          the request
   * @param response
   *          the response
   * @param report
   *          the report
   * @param reportVariables
   *          the report variables
   * @param subReports
   *          the sub reports
   * @return the action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws JRException
   *           the JR exception
   * @throws ParseException
   *           the parse exception
   */
  private ActionForward runCustomCsvReport(ActionMapping mapping,
      HttpServletRequest request, HttpServletResponse response, DynaBean report,
      List<BasicDynaBean> reportVariables, List<DynaBean> subReports)
      throws IOException, SQLException, JRException, ParseException {
    String viewName = (String) report.get("csv_view_name");
    StringBuilder where = new StringBuilder();
    for (BasicDynaBean var : reportVariables) {
      String variable = var.get(Constants.REPORT_VAR).toString();
      String variableValue = (String) request.getParameter(variable);
      if ("fromDate".equals(variable)) {
        DataBaseUtil.addWhereFieldOpValue(where, "date", ">=", variableValue);
      } else if ("toDate".equals(variable)) {
        DataBaseUtil.addWhereFieldOpValue(where, "date", "<=", variableValue);
      } else if ("fromDateTime".equals(variable)) {
        DataBaseUtil.addWhereFieldOpValue(where, "date_time", ">=",
            variableValue);
      } else if ("toDateTime".equals(variable)) {
        DataBaseUtil.addWhereFieldOpValue(where, "date_time", "<=",
            variableValue);
      } else if (variableValue.contains("%")) {
        DataBaseUtil.addWhereFieldOpValue(where,
            DataBaseUtil.quoteIdent(variable), "like", variableValue);
      } else {
        DataBaseUtil.addWhereFieldOpValue(where,
            DataBaseUtil.quoteIdent(variable), "=", variableValue);
      }
    }
    // prepare the statement
    Connection con = null;
    ResultSet rs = null;
    String query = "SELECT * from " + viewName + where.toString();
    try (PreparedStatement ps = con.prepareStatement(query)) {
      con = DataBaseUtil.getReadOnlyConnection();
      int inc = 1;
      for (BasicDynaBean var : reportVariables) {
        String variable = var.get(Constants.REPORT_VAR).toString();
        String value = request.getParameter(variable);
        if ("fromDate".equals(variable)) {
          ps.setDate(inc++, DataBaseUtil.parseDate(value));
        } else if ("toDate".equals(variable)) {
          ps.setDate(inc++, DataBaseUtil.parseDate(value));
        } else if ("fromDateTime".equals(variable)) {
          ps.setTimestamp(inc++, DataBaseUtil.parseTimestamp(value));
        } else if ("toDateTime".equals(variable)) {
          ps.setTimestamp(inc++, DataBaseUtil.parseTimestamp(value));
        } else {
          if (var.get(Constants.REPORT_VAR_TYPE).equals("D")) {
            ps.setDate(inc++, DataBaseUtil.parseDate(value));
          } else {
            ps.setString(inc++, value);
          }
        }
      }
      rs = ps.executeQuery();
      response.setHeader("Content-type", "application/csv");
      response.setHeader("Content-disposition",
          "attachment; filename=insta_report.csv");
      CSVWriter writer = new CSVWriter(response.getWriter(),
          CSVWriter.DEFAULT_SEPARATOR);
      writer.writeAll(rs, true);
      writer.flush();
    } finally {
      rs.close();
      con.close();
    }
    return null;
  }

  /**
   * Gets the text.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the text
   * @throws Exception
   *           the exception
   */
  public ActionForward getText(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    return getReport(map, form, req, res);
  }

  /**
   * Gets the screen.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the screen
   * @throws Exception
   *           the exception
   */
  public ActionForward getScreen(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) {

    String srjsFileWithExt = req.getParameter(Constants.SRJS_FILE);
    Pattern pat = Pattern.compile("\\.srjs");
    String[] splitSrx = pat.split(srjsFileWithExt);
    String reportName = splitSrx[0];
    CustomReportsDAO reportsdao = new CustomReportsDAO();
    DynaBean report = null;
    StdReportDesc newdesc = null;
    try {
      report = reportsdao.findByKey(Constants.REPORT_NAME, reportName);
      String reportMetadata = report.get(Constants.REPORT_METADATA).toString();
      StdReportDescJsonProvider newp = new StdReportDescJsonProvider();
      newdesc = newp.getReportDescForString(reportMetadata);
    } catch (Exception exp) {
      logger.info(exp.getMessage());
    }
    req.setAttribute("reportDesc", newdesc);
    HashMap<String, List<String>> filterValues = new LinkedHashMap<>();
    HashMap<String, Boolean> allowNull = new HashMap<>();
    HashMap<String, String> filterNames = new HashMap<>();
    HashMap<String, String> filterTypes = new HashMap<>();
    HashMap<String, Integer> fieldWidths = new HashMap<>();
    HashMap<String, Boolean> notHrzGrpble = new HashMap<>();
    for (String fieldName : newdesc.getFilterFieldNames()) {
      StdReportDesc.Field field = newdesc.getField(fieldName);
      String query = field.getAllowedValuesQuery();
      if (StringUtils.isEmpty(query)) {
        filterValues.put(fieldName, field.getAllowedValues());
      }
      filterNames.put(fieldName, field.getDisplayName());
      filterTypes.put(fieldName, field.getDataType());
      allowNull.put(fieldName, field.getAllowNull());

      if ((field.getFilterable())
          && (field.getAllowedValuesQuery() == null && (field.getAllowedValues() == null
              || field.getAllowedValues().isEmpty()))) {
        notHrzGrpble.put(fieldName, field.getFilterable());
      }
    }
    try {
      JSONSerializer js = new JSONSerializer().exclude(Constants.STRING_CLASS);
      req.setAttribute(Constants.SRJS_FILE, reportName + ".srjs");
      req.setAttribute("filterValuesJSON", js.deepSerialize(filterValues));
      req.setAttribute("filterDisplayNamesJSON", js.deepSerialize(filterNames));
      req.setAttribute("filterOnlyNamesJSON",
          js.deepSerialize(newdesc.getFilterOnlyFields()));
      req.setAttribute("filterTypesJSON", js.deepSerialize(filterTypes));
      req.setAttribute("allowNullJSON", js.deepSerialize(allowNull));
      req.setAttribute("fieldWidthsJSON", js.deepSerialize(fieldWidths));
      req.setAttribute("notHrzGrpbleJSON", js.deepSerialize(notHrzGrpble));
      req.setAttribute("defaultShowFieldsJSON",
          js.deepSerialize(newdesc.getDefaultShowFields()));
      req.setAttribute("CustomReportIDsJSON", js.deepSerialize(ConversionUtils
          .listBeanToListMap(CustomReportsDAO.getCustomReportNameAndIDs())));
      req.setAttribute("favRepTitlesJSON", js.deepSerialize(ConversionUtils
          .listBeanToListMap(FavouriteReportDAO.getAllFavReportTitles())));
      req.setAttribute(Constants.REPT_DESC_FILE, map.getProperty("report_desc"));
      req.setAttribute("fieldNamesSorted", newdesc.getFieldNamesSorted());
      req.setAttribute("reptDescProvider", map.getProperty("desc_provider"));
      req.setAttribute("isCustom", true);
    } catch (SQLException sqlExp) {
      logger.info(sqlExp.getMessage());
    }
    return map.findForward("getScreen");
  }

  /**
   * Gets the report.
   *
   * @param map
   *          the map
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the report
   * @throws Exception
   *           the exception
   */
  public ActionForward getReport(ActionMapping map, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    String srjsFileWithExt = req.getParameter(Constants.SRJS_FILE);
    Pattern pat = Pattern.compile("\\.srjs");
    String[] splitSrx = pat.split(srjsFileWithExt);
    String reportName = splitSrx[0];
    CustomReportsDAO reportsdao = new CustomReportsDAO();
    DynaBean report = reportsdao.findByKey(Constants.REPORT_NAME, reportName);
    String reportMetadata = report.get(Constants.REPORT_METADATA).toString();
    StdReportDescJsonProvider newp = new StdReportDescJsonProvider();
    StdReportDesc newdesc = newp.getReportDescForString(reportMetadata);

    String outputMode = req.getParameter("outputMode");
    if ((outputMode == null) || outputMode.equals("")) {
      outputMode = "pdf";
    }

    StdReportParams params = new StdReportParams(req.getParameterMap(),
        newdesc);

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("userName", RequestContext.getUserName());
    jobData.put("centerId", RequestContext.getCenterId());
    jobData.put("reportMetadata", reportMetadata);
    jobData.put("outputMode", outputMode);
    jobData.put("params", params);
    jobData.put("reportAction", "custom");
    jobData.put("reportFileName", reportName);
    jobData.put("rootRealPath", AppInit.getRootRealPath());

    String redisKey = String.format("schema:%s;user:%s;uid:%s",
        jobData.get("schema").toString(), jobData.get("userName").toString(),
        System.currentTimeMillis());
    jobData.put("redisKey", redisKey);
    // putting status in redis
    String redisValue = "status:queued;fileName:" + reportName;
    redisTemplate.opsForValue().set(redisKey, redisValue);
    // setting expiry time to 1 day
    redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); 
    jobService.scheduleImmediate(buildJob(
        "ReportJob_" + System.currentTimeMillis(), ReportJob.class, jobData));

    String url = "/reportdashboard/download.htm?id="
        + URLEncoder.encode(redisKey, "UTF-8");
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.setPath(url);
    return redirect;
  }

  /**
   * Compile report.
   *
   * @param sc
   *          the sc
   * @param baseDir
   *          the base dir
   * @param reportname
   *          the reportname
   * @param reportMetadata
   *          the report metadata
   * @param timestamp
   *          the timestamp
   * @return the string
   * @throws JRException
   *           the JR exception
   */
  private static synchronized String compileReport(ServletContext sc,
      String baseDir, String reportname, String reportMetadata, long timestamp)
      throws JRException {
    String basepath = Constants.REPORT_BASE_DIR + "/" + baseDir;
    File basedir = new File(sc.getRealPath(basepath));
    basedir.mkdirs();

    String jasperPath = sc.getRealPath(basepath + "/" + reportname + ".jasper");
    File jasperFile = new File(jasperPath);

    if ((jasperFile.lastModified() == 0)
        || (timestamp > jasperFile.lastModified())) {

      logger.debug("Compiling Custom Report to: " + jasperFile);
      try (FileOutputStream out = new FileOutputStream(jasperFile)) {
        InputStream is = new ByteArrayInputStream(reportMetadata.getBytes());
        JasperCompileManager.compileReportToStream(is, out);
        logger.debug("Finished compilation.");
      } catch (IOException ioEx) {
        logger.info("IO exception ");
        logger.info("file not found exception");
      } catch (JRException jex) {
        // cleanup the file as it is invalid
        jasperFile.delete();
        throw jex; // rethrow exception
      }
    } else {
      logger.debug("Using pre-compiled report: " + jasperPath);
    }

    return jasperPath;
  }

  /**
   * Save favourite.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward saveFavourite(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    Connection con = DataBaseUtil.getConnection();
    FavouriteReportDAO dao = new FavouriteReportDAO();
    BasicDynaBean bean = dao.getBean();
    String actionId = request.getParameter("_actionId");
    String reportTitle = request.getParameter("_report_name");
    String parentReportName = request.getParameter("_parent_report_name");
    String customReportName = request.getParameter(Constants.SRJS_FILE);
    String favReportDesc = request.getParameter(Constants.REPT_DESC_FILE);
    String reportGrp = request.getParameter("reportGroup");
    String error = null;
    Calendar cal = Calendar.getInstance();
    java.sql.Date todaysDate = new java.sql.Date((cal.getTime()).getTime());
    String queryParams = FavReportHelper
        .getReportParams(request.getParameterMap());
    try {
      bean.set("report_title", reportTitle.trim());
      bean.set("user_name", userid);
      bean.set("action_id", actionId);
      bean.set("query_params", queryParams);
      bean.set("parent_report_name", parentReportName);
      bean.set("created_date", todaysDate);
      bean.set("report_group", reportGrp == null ? "Misc. Reports" : reportGrp);
      if (favReportDesc != null && !favReportDesc.equals("null")) {
        bean.set("report_desc_srxml", favReportDesc);
      }
      if (customReportName != null && !customReportName.equals("null")) {
        bean.set("custom_report_name", customReportName);
      }
      if (dao.reportExists(con, userid, actionId, reportTitle.trim())) {
        dao.update(con, bean.getMap(), "report_title", reportTitle.trim());
      } else {
        bean.set(Constants.REPORT_ID, dao.getNextSequence());
        if (!dao.insert(con, bean)) {
          error = "Failed to save report...";
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    String url = "";
    String baseUrl = "/" + ScreenRightsHelper.getUrl(actionId) + queryParams;
    url = error == null ? baseUrl + "&_savedfavourite=" + reportTitle.trim()
        : baseUrl;
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.setPath(url);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.put("error", error);
    return redirect;
  }

  /**
   * Edits the report rights.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward editReportRights(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    CustomReportsDAO dao = new CustomReportsDAO();
    JSONSerializer js = new JSONSerializer().exclude(Constants.STRING_CLASS);
    String reportTitle = request.getParameter("_savedreport");
    BasicDynaBean customBean = dao.findByKey(Constants.REPORT_NAME, reportTitle);
    String str = "";
    String period = null;
    List roles = ConversionUtils
        .copyListDynaBeansToMap(new GenericDAO("u_role").listAll());

    request.setAttribute("rolesJSON", js.serialize(roles));
    request.setAttribute("roles", roles);
    request.setAttribute("reportBean", customBean);
    request.setAttribute("reportTitle", reportTitle);
    request.setAttribute(Constants.REPORT_ID, customBean.get(Constants.REPORT_ID));
    request.setAttribute("period", period);
    request.setAttribute("rolesWithRights", js.serialize(CustomReportsDAO
        .getRolesWithRights((Integer) customBean.get(Constants.REPORT_ID))));
    return mapping.findForward("editrights");
  }

  /**
   * Save report rights.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward saveReportRights(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    CustomReportsDAO dao = new CustomReportsDAO();
    GenericDAO rightsDao = new GenericDAO("custom_report_rights");
    Boolean success = true;
    int reportId = Integer.parseInt(request.getParameter(Constants.CUSTOM_REPORT_ID));
    String[] roleIds = request.getParameterValues("listFields");

    Connection con = DataBaseUtil.getConnection();
    if (rightsDao.findByKey(Constants.CUSTOM_REPORT_ID, reportId) != null) {
      success = rightsDao.delete(con, Constants.CUSTOM_REPORT_ID, reportId);
    }

    if (success && roleIds != null) {
      BasicDynaBean rightsbean = rightsDao.getBean();
      for (int inc = 0; inc < roleIds.length; inc++) {
        if (roleIds[inc] != null && !roleIds[inc].equals("")) {
          rightsbean.set(Constants.CUSTOM_REPORT_ID, reportId);
          rightsbean.set("role_id", new BigDecimal(roleIds[inc]));
          rightsbean.set("rights", "A");
          success &= rightsDao.insert(con, rightsbean);
        }
      }
    }
    BasicDynaBean bean = dao.findByKey(Constants.REPORT_ID, reportId);

    Map<String, Object> keys = new HashMap<>();
    keys.put(Constants.REPORT_ID, reportId);
    if (success && dao.update(con, bean.getMap(), keys) > 0) {
      success = true;
    }
    DataBaseUtil.closeConnections(con, null);
    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.put("success",
          "The Report Run Rights were Successfully updated...");
    } else {
      flash.put("error", "The Report Run Rights could not be updated...");
    }
    ActionRedirect redirect = new ActionRedirect("CustomReports.do");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("method", "editReportRights");
    redirect.addParameter("_savedreport",
        dao.findByKey(Constants.REPORT_ID, reportId)
        .get(Constants.REPORT_NAME));
    return redirect;
  }
}
