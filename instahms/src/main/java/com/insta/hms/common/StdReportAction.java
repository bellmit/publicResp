package com.insta.hms.common;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.common.ScreenRightsHelper;
import com.insta.hms.common.StdReportDesc.Field;
import com.insta.hms.customreports.CustomReportsDAO;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class StdReportAction.
 */
public class StdReportAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(StdReportAction.class);

  /** The job service. */
  private static JobService jobService = JobSchedulingService.getJobService();

  /** The redis template. */
  private static RedisTemplate<String, Object> redisTemplate = 
      (RedisTemplate) ApplicationContextProvider
      .getApplicationContext().getBean("redisTemplate");

  /**
   * Gets the screen.
   *
   * @param mapping the m
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the screen
   * @throws Exception the exception
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    StdReportDesc desc = getReportDesc(mapping);
    req.setAttribute("reportDesc", desc);
    req.setAttribute("fieldNamesSorted", desc.getFieldNamesSorted());
    HashMap<String, List<String>> filterValues = new LinkedHashMap<String, List<String>>();
    HashMap<String, Boolean> allowNull = new HashMap<String, Boolean>();
    HashMap<String, String> filterNames = new HashMap<String, String>();
    HashMap<String, String> filterTypes = new HashMap<String, String>();
    HashMap<String, Integer> fieldWidths = new HashMap<String, Integer>();
    HashMap<String, Boolean> notHrzGrpble = new HashMap<String, Boolean>();

    for (String fieldName : desc.getFilterFieldNamesSorted()) {
      StdReportDesc.Field field = desc.getField(fieldName);
      if (field == null) {
        throw new IllegalArgumentException("Invalid field name: " + fieldName);
      }
      String query = field.getAllowedValuesQuery();
      if (StringUtils.isEmpty(query)) {
        List<String> allowedValues = field.getAllowedValues();
        if (!allowedValues.isEmpty()) {
          filterValues.put(fieldName, allowedValues);
        }
      }
      filterNames.put(fieldName, field.getDisplayName());
      filterTypes.put(fieldName, field.getDataType());
      allowNull.put(fieldName, field.getAllowNull());

      if ((field.getFilterable()) && (field.getAllowedValuesQuery() == null
          && (field.getAllowedValues() == null || field.getAllowedValues().isEmpty()))) {
        notHrzGrpble.put(fieldName, field.getFilterable());
      }
    }

    for (Map.Entry<String, Field> e : desc.getFieldEntries()) {
      fieldWidths.put(e.getKey(), e.getValue().getWidth());
    }

    Boolean chartsActivated = false;
    HttpSession session = req.getSession(false);
    if (session != null) {
      Preferences prefs = (Preferences) session.getAttribute("preferences");
      Map modules = prefs.getModulesActivatedMap();

      if (modules.containsKey("mod_charts") && "Y".equals(modules.get("mod_charts"))) {
        chartsActivated = true;
      }
    }

    req.setAttribute("chartsActivated", chartsActivated);

    JSONSerializer js = new JSONSerializer().exclude("class");

    req.setAttribute("filterOnlyNamesJSON", js.deepSerialize(desc.getFilterOnlyFields()));
    req.setAttribute("filterValuesJSON", js.deepSerialize(filterValues));
    req.setAttribute("filterDisplayNamesJSON", js.deepSerialize(filterNames));
    req.setAttribute("filterTypesJSON", js.deepSerialize(filterTypes));
    req.setAttribute("allowNullJSON", js.deepSerialize(allowNull));
    req.setAttribute("fieldWidthsJSON", js.deepSerialize(fieldWidths));
    req.setAttribute("defaultShowFieldsJSON", js.deepSerialize(desc.getDefaultShowFields()));
    req.setAttribute("notHrzGrpbleJSON", js.deepSerialize(notHrzGrpble));
    req.setAttribute("CustomReportIDsJSON", js.deepSerialize(
        ConversionUtils.listBeanToListMap(CustomReportsDAO.getCustomReportNameAndIDs())));
    req.setAttribute("favRepTitlesJSON", js.deepSerialize(
        ConversionUtils.listBeanToListMap(FavouriteReportDAO.getAllFavReportTitles())));
    req.setAttribute("reptDescFile", mapping.getProperty("report_desc"));
    req.setAttribute("reptDescProvider", mapping.getProperty("desc_provider"));
    return mapping.findForward("getScreen");
  }

  /**
   * Gets the report.
   *
   * @param mapping the m
   * @param form    the f
   * @param req     the req
   * @param res     the res
   * @return the report
   * @throws Exception the exception
   */
  public ActionForward getReport(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    /*
     * Check for whether reports are disabled (licence issues) is based on cfd_max_count being
     * negative: using cfd_max_count kind of obfuscates the check.
     */
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    int cfdMaxCount = (Integer) genericPrefs.get("cfd_max_count");
    if (cfdMaxCount < 0) {
      return mapping.findForward("unavailable");
    }
    StdReportParams params = null;
    StdReportDesc desc = getReportDesc(mapping);

    String outputMode = req.getParameter("outputMode");
    if ((outputMode == null) || outputMode.equals("")) {
      outputMode = "pdf";
    }

    params = new StdReportParams(req.getParameterMap(), desc);

    if (params.isHomePageRedirect() || outputMode.equals("chart")) {
      StdReportEngine eng = null;
      res.getContentType();
      if (outputMode.equals("pdf")) {
        res.setContentType("application/pdf");
        res.setHeader("Content-disposition",
            "inline; filename=\"" + params.getPrintTitle() + ".pdf\"");
        eng = new StdPdfReportEngine(desc, params, res.getOutputStream());
      } else if (outputMode.equals("csv")) {
        res.setContentType("application/csv");
        res.setHeader("Content-disposition",
            "attachment; filename=\"" + params.getPrintTitle() + ".csv\"");
        eng = new StdCsvReportEngine(desc, params, res.getWriter());
      } else if (outputMode.equals("text")) {
        res.setContentType("text/plain");
        res.setHeader("Content-disposition",
            "attachment; filename=\"" + params.getPrintTitle() + ".txt\"");
        eng = new StdTextReportEngine(desc, params, res.getWriter());
      } else if (outputMode.equals("chart")) {
        res.setContentType("application/json");
        res.setHeader("Content-disposition",
            "attachment; filename=\"" + params.getPrintTitle() + ".json\"");
        eng = new StdGraphReportEngine(desc, params, res.getWriter());
      } else {
        logger.error("Invalid output mode: " + outputMode);
        return null;
      }
      res.setHeader("Expires", "0");
      try {
        eng.writeReport();
      } catch (SQLException exception) {
        res.reset();
        if (exception.getSQLState().equals("57014")) { // i.e. SQL query interrupted state
          res.setContentType("text/html");
          res.setHeader("Content-disposition", "inline; filename=timeOut.html");
          return mapping.findForward("sqlTimeout");
        } else {
          throw exception;
        }
      }
      return null;
    }

    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("userName", RequestContext.getUserName());
    jobData.put("centerId", RequestContext.getCenterId());
    // jobData.put("desc", desc);
    jobData.put("report_desc", mapping.getProperty("report_desc"));
    jobData.put("desc_provider", mapping.getProperty("desc_provider"));
    jobData.put("outputMode", outputMode);
    jobData.put("params", params);
    jobData.put("reportAction", "standard");
    jobData.put("rootRealPath", AppInit.getRootRealPath());

    String redisKey = String.format("schema:%s;user:%s;uid:%s", jobData.get("schema").toString(),
        jobData.get("userName").toString(), System.currentTimeMillis());

    jobData.put("redisKey", redisKey);

    // putting status in redis
    String redisValue = "status:queued;fileName:" + params.getPrintTitle();
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // setting expiry time to 1 day

    jobService.scheduleImmediate(
        buildJob("ReportJob_" + System.currentTimeMillis(), ReportJob.class, jobData));

    String url = "/reportdashboard/download.htm?id=" + URLEncoder.encode(redisKey, "UTF-8");
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.setPath(url);
    return redirect;

  }

  /**
   * Gets the report desc.
   *
   * @param mapping the m
   * @return the report desc
   * @throws Exception the exception
   */
  public StdReportDesc getReportDesc(ActionMapping mapping) throws Exception {
    String reportDescName = mapping.getProperty("report_desc");
    String descProviderName = mapping.getProperty("desc_provider");

    StdReportDescProvider provider = null;
    if (descProviderName == null && reportDescName.contains(".srxml")) {
      // use the srxml digester to get the report desc
      provider = new StdReportDescXmlProvider();
    } else if (descProviderName == null && reportDescName.contains(".srjs")) {
      provider = new StdReportDescJsonProvider();
    } else {
      provider = (StdReportDescProvider) Class.forName(descProviderName).newInstance();
    }
    StdReportDesc desc = provider.getReportDesc(reportDescName);
    return desc;
  }

  /**
   * Gets the text.
   *
   * @param mapping the m
   * @param form    the f
   * @param req     the req
   * @param res     the res
   * @return the text
   * @throws Exception the exception
   */
  public ActionForward getText(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    return getReport(mapping, form, req, res);
  }

  /**
   * Gets the chart.
   *
   * @param mapping the m
   * @param form    the f
   * @param req     the req
   * @param res     the res
   * @return the chart
   * @throws Exception the exception
   */
  public ActionForward getChart(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    StringBuffer url = req.getRequestURL();
    String queryString = req.getQueryString();
    url = url.append("?").append(queryString.replaceAll("method=getChart", "method=getReport"));
    req.setAttribute("url", url.toString());
    return mapping.findForward("getChart");
  }

  /**
   * Save favourite.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
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
    String customReportName = request.getParameter("srjsFile");
    String favReportDesc = request.getParameter("reptDescFile");
    String reportGrp = request.getParameter("reportGroup");
    String error = null;
    Calendar cal = Calendar.getInstance();
    java.sql.Date todaysDate = new java.sql.Date((cal.getTime()).getTime());
    String queryParams = FavReportHelper.getReportParams(request.getParameterMap());
    queryParams = URLDecoder.decode(queryParams, "UTF-8");
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
        bean.set("report_id", dao.getNextSequence());
        if (!dao.insert(con, bean)) {
          error = "Failed to save report...";
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    String url = "";
    String baseUrl = "/" + ScreenRightsHelper.getUrl(actionId) + queryParams;
    url = error == null ? baseUrl + "&_savedfavourite=" + reportTitle.trim() : baseUrl;
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.setPath(url);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.put("error", error);
    return redirect;
  }

  /**
   * Gets the report id via favourite.
   *
   * @param mapping the m
   * @param form    the f
   * @param req     the req
   * @param res     the res
   * @return the report id via favourite
   * @throws Exception the exception
   */
  public ActionForward getReportIdViaFavourite(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    req.setAttribute("printerType", "DMP");
    return mapping.findForward("textModePrintApplet");
  }
}
