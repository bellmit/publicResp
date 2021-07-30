package com.insta.hms.outpatient;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.bob.hms.common.RequestContext;
import com.insta.hms.ceed.CeedDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class OpListAction.
 */
public class OpListAction extends BaseAction {

  /** The consult dao. */
  static DoctorConsultationDAO consultDao = new DoctorConsultationDAO();

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(OpListAction.class);

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, ParseException, Exception {

    Integer userCenterId = RequestContext.getCenterId();
    String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
    if (errorMsg != null) {
      req.setAttribute("error", errorMsg);
      return mapping.findForward("OpList");
    }

    Map params = new HashMap(req.getParameterMap());
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
    boolean sortReverse = (Boolean) listingParams.get(LISTING.SORTASC);
    boolean tokenEnabled = RegistrationPreferencesDAO.isTokenGenerationEnabled();

    Connection con = DataBaseUtil.getConnection();
    String doctorId = null;
    Preferences prefs = null;
    try {
      User user = new UserDAO(con).getUser((String) req.getSession(false).getAttribute("userid"));
      if (user != null) {
        doctorId = user.getDoctorId();
      }
      PreferencesDao dao = new PreferencesDao(con);
      prefs = dao.getPreferences();

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    String dateRange = req.getParameter("date_range");
    String fromDate = null;
    String revDatePattern = "yyyy-MM-dd";
    String datePattern = "dd-MM-yyyy";
    SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
    String[] visitingDates = (String[]) params.get("visited_date");
    if (visitingDates == null) {
      if (dateRange != null && dateRange.equals("week")) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date openDt = cal.getTime();
        String visitDt = dateFormat.format(openDt);
        dateFormat = new SimpleDateFormat(revDatePattern);
        fromDate = dateFormat.format(openDt);
        params.put("visited_date", new String[] { visitDt, "" });
        params.put("visited_date@op", new String[] { "ge,le" });
        params.put("visited_date@cast", new String[] { "y" });
      }
    } else {
      if (visitingDates.length > 0 && null != visitingDates[0] && !visitingDates[0].equals("")) {
        Date dt = dateFormat.parse(visitingDates[0]);
        dateFormat = new SimpleDateFormat(revDatePattern);
        fromDate = dateFormat.format(dt);
        params.put("visited_date", new String[] { visitingDates[0], "" });
      }
      if (visitingDates.length > 1 && null != visitingDates[1] && !visitingDates[1].equals("")) {
        params.put("visited_date", new String[] { visitingDates[0], visitingDates[1] });
      }
      params.put("visited_date@op", new String[] { "ge,le" });
      params.put("visited_date@cast", new String[] { "y" });
    }
    params.remove("date_range");

    String sortOrder = (String) listingParams.get(LISTING.SORTCOL);
    String secondarySort = "";
    if ((sortOrder == null) || sortOrder.equals("")) {
      if (tokenEnabled) {
        sortOrder = "doctor_full_name";
        secondarySort = "consultation_token";
      } else {
        sortOrder = "visited_date";
      }
    } else if (sortOrder.equals("visited_date")) {
      sortOrder = "visited_date";
    } else if (sortOrder.equals("consultation_token")) {
      sortOrder = "doctor_full_name";
      secondarySort = "consultation_token";
    } else if (sortOrder.equals("doctor_full_name")) {
      if (sortReverse) {
        sortOrder = "doctor_full_name";
        secondarySort = "consultation_token";
        sortReverse = true;
      } else {
        sortOrder = "doctor_full_name";
        secondarySort = "consultation_token";
      }
    }
    if (doctorId != null && !doctorId.equals("")) {
      // if the doctor logged into the application, show patients of that doctor in op list.
      addToMap(params, "doctor_id", doctorId);
      BasicDynaBean docBean = DoctorMasterDAO.getDoctorById(doctorId);
      addToMap(params, "dept_id", docBean.get("dept_id"));
      req.setAttribute("doctor_logged_in", doctorId);
      req.setAttribute("doctor_logged_in_dept", docBean.get("dept_id"));

    }
    int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);
    PagedList list = consultDao.searchConsultations(params, sortOrder, sortReverse, pageSize,
        pageNum, secondarySort, fromDate);

    ArrayList<BasicDynaBean> dtolist = (ArrayList<BasicDynaBean>) list.getDtoList();

    Map modules = prefs.getModulesActivatedMap();
    String modCeedIntegrationEnabled = (String) modules.get("mod_ceed_integration");
    Map consceedstatus = null;
    if (modCeedIntegrationEnabled != null && modCeedIntegrationEnabled.equals("Y")) {
      ArrayList<Integer> consIdList = new ArrayList<Integer>();
      for (BasicDynaBean b : dtolist) {
        Integer consId = (Integer) b.get("consultation_id");
        consIdList.add(consId);
      }
      if (consIdList.size() > 0) {
        consceedstatus = ConversionUtils
            .listBeanToMapBean(CeedDAO.checkIfCeedCheckDoneForList(consIdList), "consultation_id");
      }
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    String jsonstr = js.deepSerialize(consceedstatus);
    req.setAttribute("consultationCeedStatusMapJson", jsonstr);

    List<String> visitIds = new ArrayList<String>();
    for (BasicDynaBean b : dtolist) {
      String visitId = (String) b.get("patient_id");
      if (visitId != null && !visitId.equals("") && !visitIds.contains(visitId)) {
        visitIds.add(visitId);
      }
    }
    BasicDynaBean clinicalPreferences = ApplicationContextProvider.getApplicationContext()
        .getBean(ClinicalPreferencesService.class).getClinicalPreferences();

    req.setAttribute("clinicalPrefs", clinicalPreferences.getMap());
    // set lab and radiology signed off reports count
    List<BasicDynaBean> labSignedOffRepCount = DoctorConsultationDAO
        .getSignedOffLabReportsCount(visitIds);
    req.setAttribute("lab_rad_reports",
        ConversionUtils.listBeanToMapBean(labSignedOffRepCount, "patient_id"));
    // get service signed off reports count
    List<BasicDynaBean> serviceSignedOffRepCount = DoctorConsultationDAO
        .getSignedOffServiceReportsCount(visitIds);
    req.setAttribute("service_reports",
        ConversionUtils.listBeanToMapBean(serviceSignedOffRepCount, "patient_id"));

    String userid = (String) req.getSession(false).getAttribute("userid");

    req.setAttribute("tokenEnabled", tokenEnabled);
    req.setAttribute("pagedList", list);
    req.setAttribute("directBillingPrefs", ConversionUtils
        .listBeanToMapBean(new GenericDAO("hosp_direct_bill_prefs").listAll(), "item_type"));
    req.setAttribute("doclist", DoctorMasterDAO.getAllActiveDoctors());

    ActionForward forward = new ActionForward(mapping.findForward("OpList").getPath());
    if (dateRange != null && dateRange.equals("week") && req.getParameter("visited_date") == null
        && fromDate != null) {
      Date dt = dateFormat.parse(fromDate);
      dateFormat = new SimpleDateFormat(datePattern);
      String fromDt = dateFormat.format(dt);
      addParameter("visited_date", fromDt, forward);
    }
    return forward;
  }

  /**
   * Close.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    /*
     * Action to batch-close a set of consultations, given all the patient IDs that need to be
     * closed.
     */
    String[] consIdStrs = req.getParameterValues("_closeVisit");
    String userName = (String) req.getSession(false).getAttribute("userid");
    String error = null;
    if (consIdStrs != null) {
      int[] consIds = new int[consIdStrs.length];
      Timestamp closeTime = new java.sql.Timestamp((new java.util.Date()).getTime());

      for (int i = 0; i < consIdStrs.length; i++) {
        consIds[i] = Integer.parseInt(consIdStrs[i]);
      }
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try {

        if (consIds.length > 0
            && !(DoctorConsultationDAO.closeConsultations(con, consIds, closeTime, userName)
                && ResourceDAO.updateAppointments(con, consIds))) {
          error = "Failed to close the consultations";
        }
        if (consIds.length > 0
            && !(DoctorConsultationDAO.closeTriage(con, consIds, closeTime, userName))) {
          error = "Failed to close the triage";
        }
        if (consIds.length > 0
            && !(DoctorConsultationDAO.closeInitialAssessment(con, consIds, closeTime, userName))) {
          error = "Failed to close the initial assessment";
        }
      } finally {
        DataBaseUtil.commitClose(con, error == null);
      }
    }
    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(
        req.getHeader("Referer").replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    if (error != null) {
      flash.put("error", error);
    } else {
      flash.put("success", "Closed visits succesfully");
    }
    return redirect;
  }

  /**
   * Adds the to map.
   *
   * @param params the params
   * @param key    the key
   * @param value  the value
   */
  private void addToMap(Map params, String key, Object value) {
    if (params == null || key == null || value == null || key.equals("")) {
      return;
    }

    String[] array = (String[]) params.get(key);
    if (array == null) {
      if (value instanceof String[]) {
        params.put(key, value);
      } else {
        params.put(key, new String[] { value.toString() });
      }
    }

  }

}
