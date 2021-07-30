package com.insta.hms.common;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.stores.GenericDTO;
import com.insta.hms.stores.GenericMasterDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.wardactivities.prescription.IPPrescriptionDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class CommonAjaxCallForAllModulesAction.
 *
 * @author mithun.saha
 */
public class CommonAjaxCallForAllModulesAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(CommonAjaxCallForAllModulesAction.class);
  
  private PatientDetailsDAO patientDetailsDAO = new PatientDetailsDAO();

  /**
   * Find items.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException     the SQL exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward findItems(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

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

    String searchType = request.getParameter("searchType");
    if (searchType == null) {
      searchType = "";
    }

    String deptId = request.getParameter("dept_id");
    String genderApplicability = request.getParameter("gender_applicability");
    String orgId = request.getParameter("org_id");
    String query = request.getParameter("query");
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    List list = IPPrescriptionDAO.findItems(orgId, searchType, query, useStoreItems, centerId,
        tpaId, deptId, genderApplicability);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(list));

    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the routes of administrations.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the routes of administrations
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getRoutesOfAdministrations(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String itemId = request.getParameter("item_id");
    String itemName = request.getParameter("item_name");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    BasicDynaBean routesBean = PharmacymasterDAO
        .getRoutesOfAdministrations(useStoreItems.equals("Y") ? itemId : itemName, useStoreItems);

    js.serialize(routesBean == null ? null : routesBean.getMap(), response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the generic JSON.
   *
   * @param am  the am
   * @param af  the af
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
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the package contents
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
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
  
  /**
   * Gets the Family Annual Pharmacy Utilization.
   *
   * @param am  the mapping
   * @param af     the form
   * @param req  the request
   * @param res the response
   * @return the Family Annual Pharmacy Utilization
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward getFamilyAnnualPharmacyUtilization(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String familyId = "";
    if (null != req.getParameter("family_id")) {
      familyId = (String) req.getParameter("family_id");
    } else {
      String mrNo = req.getParameter("mr_no");
      BasicDynaBean patientBean = patientDetailsDAO.findByKey("mr_no", mrNo);
      familyId = (String) patientBean.get("family_id");
    }
    List<BasicDynaBean> employeesList = 
        MedicineSalesDAO.getFamilyAnnualPharmacyUtilization(familyId);
    ArrayList<Map<String, Object>> employeesMap = new ArrayList<Map<String, Object>>(); 
    for (BasicDynaBean employeeBean : employeesList) {
      employeesMap.add(employeeBean.getMap());
    }
    JSONSerializer js = new JSONSerializer().exclude("class");
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.deepSerialize(employeesMap));
    res.flushBuffer();
    return null;
  }
}
