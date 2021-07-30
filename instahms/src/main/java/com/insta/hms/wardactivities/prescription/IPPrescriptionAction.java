package com.insta.hms.wardactivities.prescription;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.insta.hms.outpatient.PrescriptionBO;
import com.insta.hms.stores.GenericDTO;
import com.insta.hms.stores.GenericMasterDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.wardactivities.PatientActivitiesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class IPPrescriptionAction.
 *
 * @author krishna
 */
public class IPPrescriptionAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(IPPrescriptionAction.class);

  /** The presc DAO. */
  IPPrescriptionDAO prescDAO = new IPPrescriptionDAO();
  
  /** The rdm DAO. */
  RecurrenceDailyMasterDAO rdmDAO = new RecurrenceDailyMasterDAO();
  
  /** The activity dao. */
  PatientActivitiesDAO activityDao = new PatientActivitiesDAO();
  
  /** The user DAO. */
  GenericDAO userDAO = new GenericDAO("u_user");

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @SuppressWarnings("unchecked")
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException {
    String patientId = request.getParameter("visit_id");
    JSONSerializer js = new JSONSerializer().exclude(".class");
    List doctors = Collections.EMPTY_LIST;
    if (patientId != null) {
      request.setAttribute("prescriptions", prescDAO.getPrescriptions(patientId));
      doctors = ConversionUtils.copyListDynaBeansToMap(DoctorMasterDAO.getAllActiveDoctors());
    }
    BasicDynaBean userbean = userDAO.findByKey("emp_username", request.getSession(false)
        .getAttribute("userId"));
    if (userbean != null) {
      request.setAttribute("isSharedLogIn", userbean.get("is_shared_login"));
      request.setAttribute("roleId", userbean.get("role_id"));
      request.setAttribute("userBean", js.deepSerialize(userbean.getMap()));
    }
    request.setAttribute("doctors", js.deepSerialize(doctors));
    Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
    request.setAttribute("frequencies", rdmDAO.listAll(null, filterMap, "display_order"));
    request.setAttribute("actionId", mapping.getProperty("action_id"));
    request.setAttribute("allDoctorConsultationTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new ConsultationTypesDAO().listAll())));

    return mapping.findForward("list");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws ParseException the parse exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException,
      ParseException {
    Map params = request.getParameterMap();
    String[] prescriptions = request.getParameterValues("h_prescription_id");
    Boolean success = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    String patientId = request.getParameter("patient_id");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userId");
    FlashScope flash = FlashScope.getScope(request);

    try {
      if (request.getParameter("isSharedLogIn").equals("Y")) {
        userName = request.getParameter("authUser");
      }

      error = new PrescriptionBO().saveIpPrescriptions(con, null, prescriptions, params, patientId,
          userName, request.getParameterValues("h_delete"), request.getParameter("isSharedLogIn"),
          request.getParameterValues("h_edited"));

      if (error != null) {
        success = false;
      }

    } catch (Exception exe) {
      exe.printStackTrace();
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    flash.put("error", error);
    redirect.addParameter("visit_id", patientId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Find items.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward findItems(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      ServletException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String searchType = request.getParameter("searchType");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String centerIdStr = request.getParameter("center_id");
    int centerId = -1;
    if ((Integer) genericPrefs.get("max_centers_inc_default") > 1 && centerIdStr != null
        && !centerIdStr.equals("")) {
      centerId = Integer.parseInt(centerIdStr);
    }
    String tpaIdStr = request.getParameter("tpa_id");
    String tpaId = "-1";
    if (tpaIdStr != null && !tpaIdStr.equals("")) {
      tpaId = tpaIdStr;
    } else {
      tpaId = "0";
    }
    if (searchType == null) {
      searchType = "";
    }
    String query = request.getParameter("query");
    String orgId = request.getParameter("org_id");
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    List resultList = IPPrescriptionDAO.findItems(orgId, searchType, query, useStoreItems, centerId,
        tpaId, null, null);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(resultList));
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the routes of administrations.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the routes of administrations
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getRoutesOfAdministrations(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException, SQLException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String itemId = request.getParameter("item_id");
    String itemName = request.getParameter("item_name");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    BasicDynaBean routesBean = PharmacymasterDAO.getRoutesOfAdministrations(
        useStoreItems.equals("Y") ? itemId : itemName, useStoreItems);

    js.serialize(routesBean == null ? null : routesBean.getMap(), response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the generic JSON.
   *
   * @param am the am
   * @param af the af
   * @param req the req
   * @param res the res
   * @return the generic JSON
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getGenericJSON(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String genericId = req.getParameter("generic_code");
    if ((genericId == null) || genericId.equals("")) {
      log.error("getGenericJSON: Generic code is required");
      return null;
    }
    Map map = new HashMap();
    GenericDTO genDetails = GenericMasterDAO.getSelectedGenDetails(genericId);
    map.put("gmaster_name", genDetails.getGmaster_name());
    map.put("genCode", genDetails.getGenCode());
    map.put("status", genDetails.getStatus());
    map.put("operation", genDetails.getOperation());
    map.put("classification_id", genDetails.getClassification_id());
    map.put("sub_classification_id", genDetails.getSub_classification_id());
    map.put("standard_adult_dose", genDetails.getStandard_adult_dose());
    map.put("criticality", genDetails.getCriticality());
    map.put("classificationName", genDetails.getClassificationName());
    map.put("sub_ClassificationName", genDetails.getSub_classificationName());
    JSONSerializer js = new JSONSerializer().exclude("class");
    String genericJSON = js.serialize(map);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(genericJSON);
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the package contents.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the package contents
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getPackageContents(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    int packageId = Integer.parseInt(request.getParameter("package_id"));
    String type = request.getParameter("type");
    List list = null;
    if (type.equals("I")) {
      list = PackageDAO.getDiagPackComponents(packageId);
    } else {
      list = PackageDAO.getPackComponents(packageId);
    }
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(list), response.getWriter());
    return null;
  }
}
