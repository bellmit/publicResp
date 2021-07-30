package com.insta.hms.insurance;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.orders.OrderDAO;

import flexjson.JSONSerializer;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class EstimationAction.
 *
 * @author lakshmi.p
 */
public class EstimationAction extends DispatchAction {
  
  private static final GenericDAO serviceGroupsDAO = new GenericDAO("service_groups");

  /**
   * Gets the estimation prerequisites screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the estimation prerequisites screen
   * @throws Exception the exception
   */
  public ActionForward getEstimationPrerequisitesScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    EstimatePatientDetails patientDetails = null;
    List bedTypes = null;

    String insuranceID = request.getParameter("insuranceID");

    patientDetails = EstimationDAO.getInsurancePatientDetails(insuranceID);
    bedTypes = EstimationDAO.getBedTypes();
    request.setAttribute("BedTypes", bedTypes);
    request.setAttribute("EstimatePatient", patientDetails);
    request.setAttribute("Organization", ConversionUtils
        .listBeanToListMap(OrgMasterDao.getOrganizations(patientDetails.getPatientId())));

    return mapping.findForward("getEstimationPrerequisitesScreen");
  }

  /**
   * Gets the quick estimation screen.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the quick estimation screen
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getQuickEstimationScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ParseException {

    String orgid = request.getParameter("rate_plan");
    String bedType = request.getParameter("bed_type");

    if (orgid == null) {
      orgid = "ORG0001";
    }
    if (bedType == null) {
      bedType = "GENERAL";
    }
    setBillDetails(request, bedType, orgid);

    EstimatePatientDetails patientDetails = null;
    String insuranceID = request.getParameter("insuranceID") == null
        ? request.getAttribute("insuranceID").toString()
        : request.getParameter("insuranceID");

    patientDetails = EstimationDAO.getInsurancePatientDetails(insuranceID);
    List estimateDetails = new EstimationBO().getBillDetails(insuranceID, "mod_insurance");
    Map requestMap = request.getParameterMap();
    String visitType = requestMap.get("visit_type") != null
        ? ((String[]) requestMap.get("visit_type"))[0]
        : patientDetails.getPatientType();

    if (patientDetails.getPatientType() == null || patientDetails.getPatientType().isEmpty()) {
      visitType = ((String[]) requestMap.get("visit_type"))[0];
    }

    request.setAttribute("insEstimateDetails", estimateDetails);
    request.setAttribute("Estpatient", patientDetails);
    request.setAttribute("screenId", mapping.getProperty("screen_id"));

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
    request.setAttribute("pref", printPref);

    List printType = PrintConfigurationsDAO.getPrinterTypes();
    request.setAttribute("printerType", printType);

    visitType = visitType != null ? visitType : "o";
    request.setAttribute("visit_type", visitType);
    request.setAttribute("rate_plan", orgid);
    request.setAttribute("rate_plan_name", DataBaseUtil
        .getStringValueFromDb("SELECT org_name FROM organization_details WHERE org_id=?", orgid));
    request.setAttribute("bed_type", bedType);

    request.setAttribute("serviceGroups",
        serviceGroupsDAO.listAll(null, "status", "A", "service_group_name"));
    request.setAttribute("serviceGroupsJSON", new JSONSerializer().serialize(ConversionUtils
        .copyListDynaBeansToMap(serviceGroupsDAO.listAll(null, "status", "A", null))));
    request.setAttribute("servicesSubGroupsJSON",
        new JSONSerializer()
            .serialize(ConversionUtils.copyListDynaBeansToMap(new GenericDAO("service_sub_groups")
                .listAll(null, "status", "A", "service_sub_group_name"))));
    request.setAttribute("doctorConsultationTypes",
        new JSONSerializer()
            .serialize(ConversionUtils.copyListDynaBeansToMap(OrderDAO.getConsultationTypes(
                patientDetails.getPatientType() == null ? "A" : patientDetails.getPatientType(),
                false))));
    request.setAttribute("anaeTypesJSON",
        new JSONSerializer().serialize(ConversionUtils.copyListDynaBeansToMap(
            new GenericDAO("anesthesia_type_master").listAll(null, "status", "A", null))));
    Map<String, Object> filterMap = new HashMap<String, Object>();
    // In multi center scheema theatre must belong to visit center.
    if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
      filterMap.put("center_id", patientDetails.getCenterId());
    }
    filterMap.put("status", "A");
    request.setAttribute("otlist_applicabletovisitcenter",
        new GenericDAO("theatre_master").listAll(null, filterMap, "theatre_name"));

    return mapping.findForward("getQuickEstimateScreen");

  }

  /**
   * Sets the bill details.
   *
   * @param req     the req
   * @param bedType the bed type
   * @param orgid   the orgid
   * @throws SQLException the SQL exception
   */
  private void setBillDetails(HttpServletRequest req, String bedType, String orgid)
      throws SQLException {

    BillBO billBOObj = new BillBO();

    if ((orgid == null) || (orgid.equals(""))) {
      orgid = "ORG0001";
    }

    if ((bedType == null) || (bedType.equals(""))) {
      bedType = "GENERAL";
    }

    // get Constants
    req.setAttribute("chargeGroupConstList", billBOObj.getChargeGroupConstNames());
    List chargeHeadList = billBOObj.getChargeHeadConstNames();
    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("chargeHeadsJSON", js.serialize(chargeHeadList));

    Preferences preferences = (Preferences) req.getSession().getAttribute("preferences");
    if (preferences != null) {
      req.setAttribute("modulesActivatedJSON", js.serialize(preferences.getModulesActivatedMap()));
    }

    /*
     * Create a map of chargeHeadId => associated_module
     */
    HashMap chargeHeadModule = new HashMap();
    Iterator it = chargeHeadList.iterator();
    while (it.hasNext()) {
      Hashtable chargeHead = (Hashtable) it.next();
      chargeHeadModule.put(chargeHead.get("CHARGEHEAD_ID"), chargeHead.get("ASSOCIATED_MODULE"));
    }
    req.setAttribute("chargeHeadModule", chargeHeadModule);

    /*
     * JSON data for use in loading dept/ward
     */

    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

    List<String> doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
    req.setAttribute("doctorsJSON", js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));

    BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
    req.setAttribute("masterTimeStamp", mst.get("master_count"));

  }

  /**
   * Save estimation details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveEstimationDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    EstimationForm estimationForm = (EstimationForm) form;
    FlashScope flash = FlashScope.getScope(request);

    List estimateInsertList = new ArrayList();
    List estimateUpdateList = new ArrayList();
    List estimateDeleteList = new ArrayList();

    int numCharges = 0;
    String estimateID = "";
    if (estimationForm.getChargeId() != null) {
      numCharges = estimationForm.getChargeId().length - 1;
    }
    if (estimationForm.getBillNo().equals("")) {
      estimateID = new EstimationBO().getEstimateID(estimationForm.getInsuranceID(),
          "mod_insurance");
    } else {
      estimateID = new EstimationBO().getEstimateID(estimationForm.getBillNo(), "mod_insurance");
    }

    for (int i = 0; i < numCharges; i++) {
      EstimationDTO estimation = new EstimationDTO();
      estimation.setChargeID(estimationForm.getChargeId()[i]);
      estimation.setActRemarks(estimationForm.getRemarks()[i]);
      estimation.setActRate(estimationForm.getRate()[i]);
      estimation.setActQuantity(estimationForm.getQty()[i]);
      estimation.setAmount(estimationForm.getAmt()[i]);
      estimation.setDiscount(estimationForm.getDisc()[i]);

      if ((estimation.getChargeID() == null) || estimation.getChargeID().startsWith("_")) {
        if (!estimationForm.getDelCharge()[i]) {
          estimation.setChargeGroup(estimationForm.getChargeGroupId()[i]);
          estimation.setChargeHead(estimationForm.getChargeHeadId()[i]);
          estimation.setChargeRef(estimationForm.getChargeRef()[i]);
          estimation.setActDescription(estimationForm.getDescription()[i]);
          estimateInsertList.add(estimation);
        }
      } else {
        if (!estimationForm.getDelCharge()[i]) {
          estimateUpdateList.add(estimation);
        } else {
          estimateDeleteList.add(estimation);
        }
      }
    }

    Estimate est = new Estimate();
    est.setEstimateID(estimateID);
    if (estimationForm.getTotAmt() != null && !estimationForm.getTotAmt().equals("")) {
      est.setTotalAmt(new BigDecimal(estimationForm.getTotAmt()));
    }
    est.setBedType(estimationForm.getBedType());
    est.setOrgId(estimationForm.getOrganizationId());
    HttpSession session = request.getSession();
    String loginUser = session.getAttribute("userid").toString();
    est.setUser(loginUser);
    est.setHeaderFlag(estimationForm.getHeaderFlag());
    est.setInsuranceId(estimationForm.getInsuranceID());
    est.setBillNo(estimationForm.getBillNo());
    est.setUpdateEstimationChargeList(estimateUpdateList);
    est.setInsertEstimationChargeList(estimateInsertList);
    est.setDeleteEstimationChargeLIst(estimateDeleteList);

    String message = new EstimationBO().updateEstimateDetails(est);
    if (message.equals("Failed")) {
      flash.error("Transaction Failed");
    } else {
      flash.success(message);
    }

    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("getEstimationScreenAfterSave"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("insuranceID", estimationForm.getInsuranceID());
    return redirect;
  }

  /**
   * View insu bill.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws JRException      the JR exception
   * @throws SQLException     the SQL exception
   */
  public void viewInsuBill(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, JRException, SQLException {
    String insuranceID = request.getParameter("insuranceID");
    insuranceID = request.getParameter("insuranceID") == null
        ? request.getAttribute("insuranceID").toString()
        : request.getParameter("insuranceID");

    String billNo = EstimationDAO.getBillNo(insuranceID);

    /* forwarding to same page with new path */
    ActionForward forward = mapping.findForward("getViewBillScreen");
    StringBuffer path = new StringBuffer("../..");
    path.append(forward.getPath());
    boolean isQuery = (path.indexOf("?") >= 0);
    if (isQuery) {
      path.append("&");
    } else {
      path.append("?");
    }
    path.append("billNo=" + billNo);
    ((HttpServletResponse) response).sendRedirect(path.toString());
  }

  /**
   * Gets the pack charge details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the pack charge details
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   * @throws ParseException   the parse exception
   */
  public ActionForward getPackChargeDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException {

    JSONSerializer js = new JSONSerializer().exclude("class");

    String bedType = (String) request.getParameter("bedType");
    String orgId = (String) request.getParameter("orgId");
    int packageId = Integer.parseInt(request.getParameter("packageId"));

    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setContentType("text/xml");
    response.getWriter()
        .write(js.serialize(PackageDAO.getPackageDeptCharges(packageId, orgId, bedType)));
    return null;
  }

}
