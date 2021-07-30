package com.insta.hms.dischargemedication;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.MedicineRoute.MedicineRouteDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.usermanager.UserDAO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class DischargeMedicationAction.
 *
 * @author krishna
 */
public class DischargeMedicationAction extends DispatchAction {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(DischargeMedicationAction.class);
  
  /** The route DAO. */
  final MedicineRouteDAO routeDAO = new MedicineRouteDAO();

  /**
   * Gets the discharge medication screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the discharge medication screen
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getDischargeMedicationScreen(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, SQLException, IOException, Exception {
    Integer userCenterId = RequestContext.getCenterId();
    String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
    if (errorMsg != null) {
      request.setAttribute("error", errorMsg);
      request.setAttribute("feature_not_applicable", "true");
      return mapping.findForward("dischargemedication");
    }
    String patientId = request.getParameter("visit_id");
    java.util.Map patient = null;
    if (patientId != null && !patientId.equals("")) {
      patient = com.insta.hms.Registration.VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
      if (patient == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      request.setAttribute("patient", patient);
    }
    setRequestAttributes(request, patient);
    return mapping.findForward("dischargemedication");
  }

  /**
   * Sets the request attributes.
   *
   * @param req the req
   * @param patient the patient
   * @throws SQLException the SQL exception
   */
  public void setRequestAttributes(HttpServletRequest req, Map patient) throws SQLException {
    JSONSerializer js = new JSONSerializer().exclude("class");
    HttpSession session = req.getSession(false);
    String userId = (String) session.getAttribute("userid");
    int centerId = (Integer) session.getAttribute("centerId");
    boolean isDoctor = false;
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    BasicDynaBean userbean = new GenericDAO("u_user").findByKey("emp_username", userId);
    DischargeMedicationDAO dmdao = new DischargeMedicationDAO();
    req.setAttribute("genericPrefs", genericPrefs.getMap());

    List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
    req.setAttribute("doctorsJSON", js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));

    GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");
    List medDosages = medDosageDao.listAll();
    req.setAttribute(
        "medDosages", js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));

    GenericDAO presInstructionDao = new GenericDAO("presc_instr_master");
    List presInstructions = presInstructionDao.listAll();
    req.setAttribute(
        "presInstructions", js.serialize(ConversionUtils.copyListDynaBeansToMap(presInstructions)));

    List itemFormList = new GenericDAO("item_form_master").listAll();
    req.setAttribute(
        "itemFormList", js.serialize(ConversionUtils.copyListDynaBeansToMap(itemFormList)));
    
    String patientId = patient == null ? null : (String) patient.get("patient_id");

    if (patientId != null && !patientId.isEmpty()) {
      Integer patientCenterId = (Integer) patient.get("center_id");
      String prescByGenerics =
          (String)
              HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
                      CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId))
                  .getPrescriptions_by_generics();
      
      String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");

      req.setAttribute(
          "prescriptions_by_generics",
          prescriptionUsesStores.equals("Y") && prescByGenerics.equals("Y"));

      List<BasicDynaBean> dischargeMedicationDetails = null;
      dischargeMedicationDetails =
          DischargeMedicationDAO.getDischargeMedicationDetails(patientId, patientCenterId);
      req.setAttribute("medicationDetails", dischargeMedicationDetails);
      req.setAttribute(
          "dischargeMedicationBean", DischargeMedicationDAO.getDischargeMedicationBean(patientId));
      String defaultDoctor = (String) UserDAO.getUserBean(userId).get("doctor_id");
      String doctorName = "";
      if (defaultDoctor != null && !defaultDoctor.isEmpty()) {
        doctorName =
            DataBaseUtil.getStringValueFromDb(
                "SELECT doctor_name FROM doctors WHERE doctor_id=?", defaultDoctor);
      }
      req.setAttribute("defaultDoctor", defaultDoctor);
      req.setAttribute("defaultDoctorName", doctorName);

      if (userbean.get("doctor_id") != null && !userbean.get("doctor_id").toString().isEmpty()) {
        isDoctor = true;
      }
      req.setAttribute("isDoctor", js.serialize(isDoctor));
    }
    req.setAttribute(
        "routes_list_json",
        js.serialize(
            ConversionUtils.copyListDynaBeansToMap(routeDAO.listAll(null, "status", "A"))));
  }

  /**
   * Save discharge medication.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public ActionForward saveDischargeMedication(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, SQLException, IOException, Exception {
    Map map = null;
    Connection con = null;
    HttpSession session = request.getSession();
    Map params = request.getParameterMap();
    String print = request.getParameter("isPrint");
    DischargeMedicatonBO dmbo = new DischargeMedicatonBO();
    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("listRedirect"));
    String userName = (String) request.getSession(false).getAttribute("userid");

    BasicDynaBean visitInsDet = VisitDetailsDAO.getVisitDetails(patientId);
    int patientCenterId = (Integer) visitInsDet.get("center_id");

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String[] prescribedIds = (String[]) params.get("item_prescribed_id");
    String medicationIdStr = request.getParameter("medication_id");
    String doctorId = request.getParameter("doctor_id");

    DischargeMedicationDAO dmdao = new DischargeMedicationDAO();
    GenericDAO dmddao = new GenericDAO("discharge_medication_details");
    Integer medicationId = 0;
    String saveOrUpdate = "save";

    boolean flag = true;
    boolean success = false;
    Map<String,Object> prescIdByOperation = new HashMap<>();
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      txn:
      {
        if (medicationIdStr != null && !medicationIdStr.isEmpty()) {
          medicationId = Integer.parseInt(medicationIdStr);
          saveOrUpdate = "update";
        } else {
          medicationId = dmdao.getNextSequence();
        }
        if (!dmbo.saveAndUpdateDischargeMedication(
            con, medicationId, saveOrUpdate, doctorId, patientId, userName)) {
          flag = false;
          break txn;
        }
        if (prescribedIds != null) {
          
          prescIdByOperation.put("insert", new ArrayList<Integer>());
          prescIdByOperation.put("update", new ArrayList<Integer>());
          prescIdByOperation.put("delete", new ArrayList<Integer>());
          for (int i = 0; i < prescribedIds.length - 1; i++) {
            int itemPrescriptionId = 0;

            if (prescribedIds[i].equals("_")) {
              itemPrescriptionId = dmddao.getNextSequence();
            } else {
              itemPrescriptionId = Integer.parseInt(prescribedIds[i]);
            }

            map =
                dmbo.saveMedicine(
                    con,
                    medicationId,
                    itemPrescriptionId,
                    params,
                    i,
                    patientCenterId,
                    genericPrefs,
                    patientId, prescIdByOperation);

            if (!(Boolean) map.get("success")) {
              flag = false;
              break txn;
            }
          }
        }
      }
      success = flag;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    if (print != null && print.equalsIgnoreCase("Y")) {
      BasicDynaBean printpref =
          PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
      Integer printerId = (Integer) printpref.get("printer_id");

      BasicDynaBean bean = DischargeMedicationDAO.getDischargeMedicationBean(patientId);
      if (success && bean != null) {
        List<String> printURLs = new ArrayList<String>();
        printURLs.add(
            request.getContextPath()
                + "/pages/dischargeMedicationPrint.do?_method=dischargeMedicationPrint"
                + "&patient_id="
                + patientId
                + "&printerId="
                + printerId);
        session.setAttribute("printURLs", printURLs);
      }
    }

    redirect.addParameter("visit_id", patientId);
    FlashScope flash = FlashScope.getScope(request);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the patient visit prescribed medicines.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the patient visit prescribed medicines
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  public ActionForward getPatientVisitPrescribedMedicines(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, IOException, ServletException {
    String patientId = request.getParameter("patient_id");
    patientId = patientId == null ? "" : patientId;
    Integer centerId = RequestContext.getCenterId();
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    Map patientDetMap = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
    String visitType = (String) patientDetMap.get("visit_type");

    String pageNumStr = request.getParameter("pageNum");
    int pageNum = 1;
    if (pageNumStr != null && !pageNumStr.equals("")) {
      pageNum = Integer.parseInt(pageNumStr);
    }

    PagedList pagedList = null;
    if (!patientId.equals("")) {
      pagedList =
          new DischargeMedicationDAO()
              .getAllVisitPrescribedMedicines(patientId, pageNum, healthAuthority, visitType);
    }

    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(pagedList, response.getWriter());
    return null;
  }

  /**
   * Gets the item routes.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the item routes
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemRoutes(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, IOException, ServletException {
    BasicDynaBean itemRouteBean = null;
    String itemId = request.getParameter("item_id");
    String itemName = request.getParameter("item_name");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    Map map = new HashMap();
    if (itemId != null && !itemId.isEmpty()) {
      itemRouteBean =
          PharmacymasterDAO.getRoutesOfAdministrations(
              useStoreItems.equals("Y") ? itemId : itemName, useStoreItems);
    }
    if (itemRouteBean != null) {
      map.putAll(itemRouteBean.getMap());
    }
    response.setContentType("application/json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.serialize(map, response.getWriter());
    response.flushBuffer();
    return null;
  }
}
