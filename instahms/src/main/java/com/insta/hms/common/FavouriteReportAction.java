package com.insta.hms.common;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.common.ScreenRightsHelper;
import com.insta.hms.customreports.CustomReportsDAO;
import com.insta.hms.jobs.JobService;
import com.insta.hms.scheduledreport.StrutsDescParser;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class FavouriteReportAction.
 */
public class FavouriteReportAction extends DispatchAction {

  /** The dao. */
  private static FavouriteReportDAO dao = new FavouriteReportDAO();

  /** The job service. */
  private static JobService jobService = JobSchedulingService.getJobService();

  /** The redis template. */
  private static RedisTemplate<String, Object> redisTemplate = 
      (RedisTemplate) ApplicationContextProvider
      .getApplicationContext().getBean("redisTemplate");

  /**
   * Gets the my favourite.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the my favourite
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   */
  public ActionForward getMyFavourite(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    String myReport = request.getParameter("_myreport");
    String actionId = request.getParameter("_actionId");
    String error = null;
    String queryParams = "";
    String reportTitle = "";
    if (myReport == null || myReport.equals("")) {
      error = "Report Id should not be empty...";
      queryParams = FavReportHelper.getReportParams(request.getParameterMap());
    } else {
      int reportId = Integer.parseInt(myReport);
      Connection con = DataBaseUtil.getConnection();
      try {
        BasicDynaBean bean = dao.findByKey(con, "report_id", reportId);
        queryParams = (String) bean.get("query_params");
        queryParams = URLDecoder.decode(queryParams, "UTF-8");
        reportTitle = (String) bean.get("report_title");
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    FlashScope flash = FlashScope.getScope(request);
    flash.put("error", error);
    String url = ScreenRightsHelper.getUrl(actionId) + queryParams;
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.addParameter("_savedfavourite", reportTitle);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Are report rights avlbl.
   *
   * @param roleId   the role id
   * @param reportId the report id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean areReportRightsAvlbl(int roleId, int reportId) throws SQLException {
    boolean hasRights = FavouriteReportDAO.getFavouriteReportRight(roleId, reportId);
    return hasRights;
  }

  /**
   * Gets the report. returns the list of favourite reports according to the filter provided
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the report
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward getReport(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, SQLException, ParseException {

    int roleid = (Integer) request.getSession().getAttribute("roleId");
    String emergentScreen = request.getParameter("_emergentScreen");

    if (request.getParameter("report_group") == null) {
      emergentScreen = "fav_rpt_dashbd";
    } else {
      emergentScreen = "other_dashbd";
    }

    request.setAttribute("_emergentScreen", emergentScreen);

    // get all reports for a role which have either report rights or run
    // rights
    PagedList list = FavouriteReportDAO.getFavReports(request.getParameterMap(),
        ConversionUtils.getListingParameter(request.getParameterMap()),
        (Integer) request.getSession().getAttribute("roleId"));
    request.setAttribute("list", list);
    String username = (String) request.getSession().getAttribute("userId");

    /*
     * Find out some extra attributes for each row
     */
    ArrayList urlList = new ArrayList();
    ArrayList periodList = new ArrayList();
    ArrayList paramList = new ArrayList();
    ArrayList runRightsList = new ArrayList();
    ArrayList creatorRightsList = new ArrayList();
    ArrayList reportRightsList = new ArrayList();

    for (Map obj : (List<Map>) list.getDtoList()) {
      // get the base URL
      HttpSession session = request.getSession(false);
      // get the rights map
      java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
      // get the actionId - url mapping map
      java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext()
          .getAttribute("actionUrlMap");
      String reportUrl = null;
      if (urlRightsMap.get((String) obj.get("action_id")).equals("A")
          || areReportRightsAvlbl(roleid, (Integer) obj.get("report_id"))) {
        reportUrl = (String) actionUrlMap.get((String) obj.get("action_id"));
      } else {
        reportUrl = "";
      }
      urlList.add(reportUrl);

      // if rights are available to run the report, set rights accordingly
      // for the user's session
      if (obj.get("custom_report_name") != null) {
        String reportRight = (roleid == 1 || roleid == 2) ? "Y"
            : urlRightsMap.get("custom_rpt_list") == null ? "N"
                : urlRightsMap.get("custom_rpt_list").equals("A") ? "Y" : "N";

        // since there is parent in case of custom reports, so just checking whether user is having
        // to right for favourite.
        // Refer : BUG: 40641, main bug is 15839.
        String runRight = roleid == 1 || roleid == 2 ? "Y"
            : areReportRightsAvlbl(roleid, (Integer) obj.get("report_id")) ? "Y" : "N";

        runRightsList.add(runRight);
        reportRightsList.add(reportRight);

      } else {
        String reportRight = (roleid == 1 || roleid == 2) ? "Y"
            : urlRightsMap.get(((String) obj.get("action_id"))) == null ? "N"
                : urlRightsMap.get(((String) obj.get("action_id"))).equals("A") ? "Y" : "N";

        String runRight = roleid == 1 || roleid == 2
            || (urlRightsMap != null && urlRightsMap.get(((String) obj.get("action_id"))) != null
                && urlRightsMap.get(((String) obj.get("action_id"))).equals("A")) ? "Y"
                    : areReportRightsAvlbl(roleid, (Integer) obj.get("report_id")) ? "Y" : "N";

        runRightsList.add(runRight);
        reportRightsList.add(reportRight);
      }

      String creatorRight = ((String) obj.get("user_name")).equals(username) || roleid == 1
          || roleid == 2 ? "Y" : "N";
      creatorRightsList.add(creatorRight);
      // get the params without output mode and method, so that we can set
      // the output format later.
      String reportParams = (String) obj.get("query_params");
      String plainParams = reportParams.replaceAll("\\?method=[^&]*|&outputMode=[^&]*", "");

      paramList.add(plainParams);

      // get the period for which the reports have been created or
      // favourited upon.
      String[] st = reportParams.split("&selDateRange=");

      String str = "";
      String period = null;
      if (st.length > 1) {
        int flag = st[1].indexOf("&");
        str = (flag >= 0) ? st[1].substring(0, flag) : st[1];
        period = str.equals("td") ? "Today"
            : str.equals("pd") ? "Yesterday"
                : str.equals("tm") ? "This Month"
                    : str.equals("pm") ? "Previous Month"
                        : str.equals("tf") ? "This Financial Year"
                            : str.equals("pf") ? "Prev. Financial Year" : "Custom Date";
      }
      periodList.add(period);
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("urlListJSON", js.serialize(urlList));
    request.setAttribute("paramListJSON", js.serialize(paramList));
    request.setAttribute("runRightsList", runRightsList);
    request.setAttribute("creatorRightsList", creatorRightsList);
    request.setAttribute("reportRightsList", reportRightsList);
    request.setAttribute("periodList", periodList);

    FavouriteReportDAO dao = new FavouriteReportDAO();
    int maxCount = dao.getCount();
    request.setAttribute("_recordsCount", (maxCount));
    List creatorList = dao.getAllFavReportCreators();
    request.setAttribute("creatorLst", creatorList);
    List typeList = dao.getAllFavReportTypes();
    request.setAttribute("typeLst", typeList);
    List grpList = dao.getAllFavReportGroups();
    request.setAttribute("grpLst", grpList);

    return mapping.findForward("getReport");
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
    url = url.append("?").append(queryString.replaceAll("method=getChart", "method=runFavReport"));
    req.setAttribute("url", url.toString());
    return mapping.findForward("getChart");
  }

  /**
   * Run fav report.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   * @throws Exception        the exception
   */
  public ActionForward runFavReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException, Exception {
    String myReport = request.getParameter("_myreport");
    String format = request.getParameter("_informat");
    Map paramMap = new HashMap();
    String type = request.getParameter("_sel");
    String fromDateStr = request.getParameter("fromDate");
    String toDateStr = request.getParameter("toDate");

    if (null != type && null != fromDateStr && null != toDateStr) {
      if (!type.equalsIgnoreCase("cstm")) {
        java.sql.Date[] dt = DateUtil.getDateRange(type);
        paramMap.put("fromDate", dt[0]);
        paramMap.put("toDate", dt[1]);
        paramMap.put("defaultReportDate", "N");
      } else {
        Date fromDate = DateUtil.parseDate(fromDateStr);
        Date toDate = DateUtil.parseDate(toDateStr);
        java.sql.Date sqlFromdate = new java.sql.Date(fromDate.getTime());
        java.sql.Date sqlTodate = new java.sql.Date(toDate.getTime());
        paramMap.put("fromDate", sqlFromdate);
        paramMap.put("toDate", sqlTodate);
        paramMap.put("defaultReportDate", "N");
      }
    } else {
      paramMap.put("defaultReportDate", "Y");
    }

    if (format == null || format.equals("")) {
      format = "pdf";
    }

    String error = null;
    FlashScope flash = FlashScope.getScope(request);

    if (myReport == null || myReport.equals("")) {
      error = "Report Id should not be empty...";
      flash.put("error", error);
    } else {
      // obtain the report id from page
      int reportId = Integer.parseInt(myReport);
      // find the report and associated attributes

      if (format.equalsIgnoreCase("chart")) {
        response.setContentType("application/json");
        response.setHeader("Content-disposition", "attachment; filename=report.json");
        byte[] reportBytes = getReportBytes(reportId, format, paramMap);
        response.getOutputStream().write(reportBytes);
        return null;
      } else {
        byte[] redisKeyBytes = getReportBytes(reportId, format, paramMap);

        String redisKey = new String(redisKeyBytes, StandardCharsets.UTF_8);
        String url = "/reportdashboard/download.htm?id=" + URLEncoder.encode(redisKey, "UTF-8");
        ActionRedirect redirect = new ActionRedirect(url);
        redirect.setPath(url);
        return redirect;
      }
    }
    return null;
  }

  /**
   * Gets the fav report desc.
   *
   * @param custmReportSrxmlName the custm report srxml name
   * @param favReportActionId    the fav report action id
   * @return the fav report desc
   * @throws Exception the exception
   */
  public StdReportDesc getFavReportDesc(String custmReportSrxmlName, String favReportActionId)
      throws Exception {
    // if custom_report_name is present, then it is a custom report,
    // otherwise,
    // it is a screen based report, so we need to look at action_id
    if (custmReportSrxmlName != null && !custmReportSrxmlName.equals("null")
        && !custmReportSrxmlName.equals("")) {
      StdReportDescJsonProvider provider = new StdReportDescJsonProvider();
      String[] splitSrx = custmReportSrxmlName.split(".srjs");
      String reportname = splitSrx[0];
      CustomReportsDAO reportsdao = new CustomReportsDAO();
      DynaBean report = reportsdao.findByKey("report_name", reportname);
      String reportMetadata = report.get("report_metadata").toString();
      return provider.getReportDescForString(reportMetadata);
    } else if (favReportActionId != null && !favReportActionId.equals("")) {
      /*
       * Each actionId can have a different provider: fetch the report_desc and the class that
       * provides the descriptor for the action ID
       */
      String[] reportInfo = StrutsDescParser.getStdReportInfo(favReportActionId);
      String reportDesc = reportInfo[0];
      String providerClass = reportInfo[1];
      if (providerClass == null) {
        providerClass = "com.insta.hms.common.StdReportDescJsonProvider";
      }

      Constructor cls = Class.forName(providerClass).getConstructor();
      StdReportDescProvider prov = (StdReportDescProvider) cls.newInstance();
      return prov.getReportDesc(reportDesc);
    } else {
      return null;
    }
  }

  /**
   * Gets the report bytes.
   *
   * @param reportId the report id
   * @param format   the format
   * @param paramMap the param map
   * @return the report bytes
   * @throws Exception the exception
   */
  public byte[] getReportBytes(int reportId, String format, Map paramMap) throws Exception {
    DynaBean favBean = dao.findByKey("report_id", reportId);
    String reportFileName = (String) favBean.get("report_title");

    
    String basePath = AppInit.getRootRealPath();


    String queryParams = favBean.get("query_params").toString();

    String reportType = FavouriteReportAction.getParamValue(queryParams, "reportType");
    if ((reportType == null) || reportType.equals("")) {
      reportType = "list";
    }

    String outputMode = format;
    if ((outputMode == null) || outputMode.equals("")) {
      outputMode = "pdf";
    }
    // For innerWhereClause, if defaultReportDate is 'N', then set the SelDateRange as 'cstm'
    // set the FromDate and ToDate from selected fields, instead of default fromDate, toDate.
    Map<String, String[]> stdReportParam = FavouriteReportAction.getParameterMap(queryParams);
    if ("N".equals(paramMap.get("defaultReportDate"))) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
      stdReportParam.put("selDateRange", new String[] {"cstm"});
      stdReportParam.put("fromDate", new String[] { sdf.format(paramMap.get("fromDate")) });
      stdReportParam.put("toDate", new String[] { sdf.format(paramMap.get("toDate")) });
    }
    String custmReportSrxmlName = (String) favBean.get("custom_report_name");
    String favReportActionId = (String) favBean.get("action_id");
    StdReportDesc desc = getFavReportDesc(custmReportSrxmlName, favReportActionId);
    StdReportParams params = new StdReportParams(stdReportParam, desc);

    if (paramMap.get("defaultReportDate").equals("N")) {
      params.setFromDate((java.sql.Date) paramMap.get("fromDate"));
      params.setToDate((java.sql.Date) paramMap.get("toDate"));
      ArrayList filterValues = new ArrayList();
      filterValues = params.getFilterValues();
      int len = filterValues.size();

      String selectedDateField = params.getParameter("dateFieldSelection");
      if ((selectedDateField != null) && (desc.getFields().get(selectedDateField) != null)
          && (!desc.getSkipDateFilter())) {
        if (filterValues.isEmpty()) {
          filterValues.add((java.sql.Date) paramMap.get("fromDate"));
          filterValues.add((java.sql.Date) paramMap.get("toDate"));          
        } else {
          filterValues.set(len - 2, (java.sql.Date) paramMap.get("fromDate"));
          filterValues.set(len - 1, (java.sql.Date) paramMap.get("toDate"));          
        }
      }

      ArrayList preFilterValues = new ArrayList();
      preFilterValues = params.getPreFilterValues();
      int pfvLen = preFilterValues.size();
      if ((selectedDateField != null)
          && (desc.getFilterOnlyFields().get(selectedDateField) != null)) {
        if (preFilterValues.isEmpty()) {
          preFilterValues.add((java.sql.Date) paramMap.get("fromDate"));
          preFilterValues.add((java.sql.Date) paramMap.get("toDate"));          
        } else {
          preFilterValues.set(pfvLen - 2, (java.sql.Date) paramMap.get("fromDate"));
          preFilterValues.set(pfvLen - 1, (java.sql.Date) paramMap.get("toDate"));
        }
      }

      params.setFilterValues(filterValues);
      params.setPreFilterValues(preFilterValues);
    }

    if (outputMode.equals("chart")) {
      StdReportEngine eng = null;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Writer sw = new BufferedWriter(new OutputStreamWriter(baos));
      eng = new StdGraphReportEngine(desc, params, sw);
      eng.writeReport();
      return baos.toByteArray();
    } else {
      Map<String, Object> jobData = new HashMap<String, Object>();
      jobData.put("schema", RequestContext.getSchema());
      jobData.put("userName", RequestContext.getUserName());
      jobData.put("centerId", RequestContext.getCenterId());
      jobData.put("outputMode", format);
      jobData.put("params", params);
      jobData.put("custmReportSrxmlName", custmReportSrxmlName);
      jobData.put("favReportActionId", favReportActionId);
      jobData.put("reportAction", "favourite");
      jobData.put("reportFileName", reportFileName);
      jobData.put("rootRealPath", AppInit.getRootRealPath());

      String redisKey = String.format("schema:%s;user:%s;uid:%s", jobData.get("schema").toString(),
          jobData.get("userName").toString(), System.currentTimeMillis());

      jobData.put("redisKey", redisKey);

      // putting status in redis
      String redisValue = "status:queued;fileName:" + reportFileName;
      redisTemplate.opsForValue().set(redisKey, redisValue);
      redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // setting expiry time to 1 day

      jobService.scheduleImmediate(
          buildJob("ReportJob_" + System.currentTimeMillis(), ReportJob.class, jobData));

      return redisKey.getBytes();
    }
  }

  /**
   * Gets the parameter map.
   *
   * @param queryParams the query params
   * @return the parameter map
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public static Map getParameterMap(String queryParams) throws UnsupportedEncodingException {
    return ConversionUtils.getParameterMap(queryParams.replaceFirst("\\?", "&"));
  }

  /**
   * Gets the param value.
   *
   * @param paramString the param string
   * @param searchKey   the search key
   * @return the param value
   */
  public static String getParamValue(String paramString, String searchKey) {
    String[] keyStartSplit = paramString.split(searchKey + "=");
    if (keyStartSplit != null && keyStartSplit.length > 1) {
      String[] value = keyStartSplit[1].split("&");
      if (value != null) {
        return value[0];
      }
    }
    return null;
  }

  /**
   * Delete fav report.
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
  public ActionForward deleteFavReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    Boolean success = true;
    FavouriteReportDAO dao = new FavouriteReportDAO();
    String reportsToBeDeleted = request.getParameter("_reportsToBeDeleted");
    String[] reportsToBeDeletedArray = reportsToBeDeleted.split("@");
    Connection con = DataBaseUtil.getConnection();
    if (reportsToBeDeletedArray != null) {
      for (int i = 0; i < reportsToBeDeletedArray.length; i++) {
        if (reportsToBeDeletedArray[i] != null && !reportsToBeDeletedArray[i].equals("")) {
          BasicDynaBean favBean = dao.findByKey("report_title", reportsToBeDeletedArray[i]);
          if (favBean != null) {
            new GenericDAO("favourite_report_rights").delete(con, "favourite_report_id",
                (Integer) favBean.get("report_id"));
          }
          success &= dao.delete(con, "report_title", reportsToBeDeletedArray[i]);
        }
      }
    }
    String delName = null;
    DataBaseUtil.closeConnections(con, null);
    String actionId = request.getParameter("_actionId");
    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.put("success", "The Report(s) were Successfully deleted...");
    } else {
      flash.put("error", "The Report(s) could not be deleted...");
    }
    String emergentScreen = request.getParameter("_emergentScreen");
    request.setAttribute("_emergentScreen", emergentScreen);
    String url = ScreenRightsHelper.getUrl(actionId);
    url = (url.substring(url.indexOf("/Reports/"), url.indexOf(".do")));
    url = url.substring(url.indexOf("/Reports") + 9) + ".do";
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    int pgNm = request.getParameter("_pgNm") == null ? 1
        : Integer.parseInt(request.getParameter("_pgNm"));
    redirect.addParameter("pageNum", pgNm);
    redirect.addParameter("_emergentScreen", emergentScreen);
    redirect.addParameter("_method", "getReport");
    redirect.addParameter("sortOrder", "report_title");
    redirect.addParameter("parent_report_name", request.getParameter("parent_report_name"));
    redirect.addParameter("frequently_viewed", request.getParameter("frequently_viewed"));
    redirect.addParameter("report_group", request.getParameter("report_group"));
    redirect.addParameter("user_name", request.getParameter("user_name"));
    return redirect;
  }

  /**
   * Mark fav report as freq.
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
  public ActionForward markFavReportAsFreq(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    FavouriteReportDAO dao = new FavouriteReportDAO();
    String reportsToBeUpdated = request.getParameter("_reportsToBeMarkdFreq");
    Boolean success = true;
    String[] reportsToBeUpdatedArray = reportsToBeUpdated.split("@");
    Connection con = DataBaseUtil.getConnection();
    if (reportsToBeUpdatedArray != null) {
      for (int i = 0; i < reportsToBeUpdatedArray.length; i++) {
        if (reportsToBeUpdatedArray[i] != null && !reportsToBeUpdatedArray[i].equals("")) {
          success = success && dao.updateFreqStatus(reportsToBeUpdatedArray[i]);
        }
      }
    }

    DataBaseUtil.closeConnections(con, null);
    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.put("success", "The Report(s) were Successfully updated...");
    } else {
      flash.put("error", "The Report(s) could not be updated...");
    }
    String actionId = request.getParameter("_actionId");
    String emergentScreen = request.getParameter("_emergentScreen");
    request.setAttribute("_emergentScreen", emergentScreen);
    String url = ScreenRightsHelper.getUrl(actionId);
    url = (url.substring(url.indexOf("/Reports/"), url.indexOf(".do")));
    url = url.substring(url.indexOf("/Reports") + 9) + ".do";
    ActionRedirect redirect = new ActionRedirect(url);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    int pgNm = request.getParameter("_pgNm") == null ? 1
        : Integer.parseInt(request.getParameter("_pgNm"));
    redirect.addParameter("pageNum", pgNm);
    redirect.addParameter("_method", "getReport");
    redirect.addParameter("_emergentScreen", emergentScreen);
    redirect.addParameter("frequently_viewed", request.getParameter("frequently_viewed"));
    redirect.addParameter("report_group", request.getParameter("report_group"));
    redirect.addParameter("user_name", request.getParameter("user_name"));
    redirect.addParameter("parent_report_name", request.getParameter("parent_report_name"));
    redirect.addParameter("sortOrder", "report_title");
    return redirect;
  }

  /**
   * Gets the todays date.
   *
   * @return the todays date
   */
  public static String getTodaysDate() {
    Calendar cal = new GregorianCalendar();
    String mnth = String.valueOf(cal.get(GregorianCalendar.MONTH) + 1);
    String dy = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
    String yr = String.valueOf(cal.get(GregorianCalendar.YEAR));
    if (mnth.length() < 2) {
      mnth = "0" + mnth;
    }
    if (dy.length() < 2) {
      dy = "0" + dy;
    }
    String dateStr = dy + "-" + mnth + "-" + yr;
    return dateStr;
  }

  /**
   * Edits the report rights.
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
  public ActionForward editReportRights(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    FavouriteReportDAO dao = new FavouriteReportDAO();
    JSONSerializer js = new JSONSerializer().exclude("class");
    String reportTitle = request.getParameter("_savedfavourite");
    BasicDynaBean favBean = dao.findByKey("report_title", reportTitle);
    String reportParams = (String) favBean.get("query_params");
    String[] st = reportParams.split("&selDateRange=");
    String str = "";
    String period = null;
    if (st.length > 1) {

      int flag = st[1].indexOf("&");

      str = (flag >= 0) ? st[1].substring(0, flag) : st[1];
      period = str.equals("td") ? "Today"
          : str.equals("pd") ? "Yesterday"
              : str.equals("tm") ? "This Month"
                  : str.equals("pm") ? "Previous Month"
                      : str.equals("tf") ? "This Financial Year"
                          : str.equals("pf") ? "Prev. Financial Year" : "Custom Date";
    }
    List roles = ConversionUtils.copyListDynaBeansToMap(new GenericDAO("u_role").listAll());

    request.setAttribute("rolesJSON", js.serialize(roles));
    request.setAttribute("roles", roles);
    request.setAttribute("reportBean", favBean);
    request.setAttribute("reportTitle", reportTitle);
    request.setAttribute("report_id", favBean.get("report_id"));
    request.setAttribute("period", period);
    request.setAttribute("rolesWithRights",
        js.serialize(FavouriteReportDAO.getRolesWithRights((Integer) favBean.get("report_id"))));
    return mapping.findForward("editrights");
  }

  /**
   * Save report rights.
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
  public ActionForward saveReportRights(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    FavouriteReportDAO dao = new FavouriteReportDAO();
    GenericDAO rightsDao = new GenericDAO("favourite_report_rights");
    JSONSerializer js = new JSONSerializer().exclude("class");
    Boolean success = true;
    int reportId = Integer.parseInt(request.getParameter("favourite_report_id"));
    String[] roleIds = (String[]) request.getParameterValues("listFields");

    Connection con = DataBaseUtil.getConnection();
    if (rightsDao.findByKey("favourite_report_id", reportId) != null) {
      success = rightsDao.delete(con, "favourite_report_id", reportId);
    }

    if (success && roleIds != null) {
      BasicDynaBean rightsbean = rightsDao.getBean();
      for (int i = 0; i < roleIds.length; i++) {
        if (roleIds[i] != null && !roleIds[i].equals("")) {
          rightsbean.set("favourite_report_id", reportId);
          rightsbean.set("role_id", new BigDecimal(roleIds[i]));
          rightsbean.set("rights", "A");
          success &= rightsDao.insert(con, rightsbean);
        }
      }
    }
    String allowDateChange = request.getParameter("allow_date_change");
    BasicDynaBean bean = dao.findByKey("report_id", reportId);

    if (null != allowDateChange && allowDateChange.equalsIgnoreCase("on")) {
      bean.set("allow_date_change", true);
    } else {
      bean.set("allow_date_change", false);
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("report_id", reportId);
    if (success) {
      int result = dao.update(con, bean.getMap(), keys);
      if (result > 0) {
        success = true;
      }
    }
    DataBaseUtil.closeConnections(con, null);
    FlashScope flash = FlashScope.getScope(request);
    if (success) {
      flash.put("success", "The Report Run Rights were Successfully updated...");
    } else {
      flash.put("error", "The Report Run Rights could not be updated...");
    }
    ActionRedirect redirect = new ActionRedirect("FavouriteReportsDashboard.do");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("_method", "editReportRights");
    redirect.addParameter("_savedfavourite",
        dao.findByKey("report_id", reportId).get("report_title"));
    return redirect;
  }

  /**
   * Gets the date range for fav reports.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the date range for fav reports
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws Exception    the exception
   */
  public ActionForward getDateRangeForFavReports(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, Exception {
    String reportID = request.getParameter("_myreport");
    DynaBean favBean = dao.findByKey("report_id", Integer.parseInt(reportID));
    String customSrxmlName = (String) favBean.get("custom_report_name");
    StdReportDesc desc = null;
    Boolean noneDateFiled = false;
    String favReportActionId = (String) favBean.get("action_id");

    if (customSrxmlName != null && !customSrxmlName.equals("")) {
      StdReportDescJsonProvider provider = new StdReportDescJsonProvider();
      String reportname = customSrxmlName.replaceAll("\\.srjs$", "");
      CustomReportsDAO reportsdao = new CustomReportsDAO();
      DynaBean report = reportsdao.findByKey("report_name", reportname);
      if (report == null) {
        throw new IllegalArgumentException("Report " + reportname + " not found");
      }
      String reportMetadata = report.get("report_metadata").toString();
      desc = provider.getReportDescForString(reportMetadata);

    } else if (favReportActionId != null && !favReportActionId.equals("")) {
      String[] reportInfo = StrutsDescParser.getStdReportInfo(favReportActionId);
      String reportDesc = reportInfo[0];
      String providerClass = reportInfo[1];
      if (providerClass == null) {
        providerClass = "com.insta.hms.common.StdReportDescJsonProvider";
      }

      Constructor cls = Class.forName(providerClass).getConstructor();
      StdReportDescProvider prov = (StdReportDescProvider) cls.newInstance();
      desc = prov.getReportDesc(reportDesc);
    }
    String queryParams = favBean.get("query_params").toString();
    StdReportParams params = new StdReportParams(FavouriteReportAction.getParameterMap(queryParams),
        desc);

    if (null == params.getSelectedDateField()) {
      noneDateFiled = true;
    }

    Boolean chartsActivated = false;
    HttpSession session = request.getSession(false);
    if (session != null) {
      Preferences prefs = (Preferences) session.getAttribute("preferences");
      Map modules = prefs.getModulesActivatedMap();

      if (modules.containsKey("mod_charts") && "Y".equals(modules.get("mod_charts"))) {
        chartsActivated = true;
      }
    }

    request.setAttribute("chartsActivated", chartsActivated);
    String allowDateChange = favBean.get("allow_date_change").toString();
    request.setAttribute("allowDateChange", allowDateChange);
    request.setAttribute("NoneDateField", noneDateFiled);
    request.setAttribute("reportID", reportID);
    request.setAttribute("trend", params.getType());
    return mapping.findForward("dateRangeSelection");
  }

}