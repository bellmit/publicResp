package com.insta.hms.discharge;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.dischargesummary.DischargeSummaryBOImpl;
import com.insta.hms.dischargesummary.DischargeSummaryDAOImpl;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.v2.Hl7Repository;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.stores.StoresPatientIndentDAO;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.wardactivities.PatientActivitiesDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.lang.NumberFormatException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



// TODO: Auto-generated Javadoc
/** The Class DischargeAction. */
public class DischargeAction extends BaseAction {

  /** The js. */
  JSONSerializer js = new JSONSerializer().exclude("class");

  /** The reg dao. */
  VisitDetailsDAO regDao = new VisitDetailsDAO();

  /** The dis dao. */
  DischargeDAO disDao = new DischargeDAO();

  /** The spi dao. */
  StoresPatientIndentDAO spiDao = new StoresPatientIndentDAO();

  /** The p dao. */
  PatientActivitiesDAO patActDao = new PatientActivitiesDAO();

  /** The order dao. */
  OrderDAO orderDao = new OrderDAO();
  
  private static InterfaceEventMappingService interfaceEventMappingService =
      ApplicationContextProvider.getBean(InterfaceEventMappingService.class);

  private static Hl7Repository hl7Repository =
      ApplicationContextProvider.getBean(Hl7Repository.class);
  
  private static final GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");

  private static final GenericDAO patientDetailsDAO = new GenericDAO("patient_details");
  
  /**
   * Gets the discharge details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param responce the responce
   * @return the discharge details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getDischargeDetails(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse responce)
      throws IOException, SQLException {

    String patientId = request.getParameter("patientid");
    if (patientId == null) {
      return mapping.findForward("dischargescreen");
    }
    Map patientMap = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
    String mrNo;
    int patCenterId = 0;
    if (patientId != null && !patientId.equals("")) {
      BasicDynaBean bean = regDao.findByKey("patient_id", patientId);
      if (bean == null) {
        // user entered visit id is not a valid patient id.
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", "No Patient with Id:" + patientId);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("dischargeRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      patCenterId = (Integer) bean.get("center_id");
      request.setAttribute("canDischarge", new DischargeDAO().isDischargeable(patientId));
      request.setAttribute("patient", patientMap);
      BasicDynaBean dischargeDetails = new DischargeSummaryDAOImpl().getDischargeDetails(patientId);
      if (dischargeDetails != null) {
        request.setAttribute("discharge_details", dischargeDetails.getMap());
      }
    }
    mrNo = (String) patientMap.get("mr_no");
    List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList(patCenterId);
    request.setAttribute(
        "doctorsJSON", js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));

    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multiCentered =
        GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;
    if (multiCentered && centerId == 0) {
      request.setAttribute("error", "Discharge is allowed only for center users.");
    }
    request.setAttribute("multiCentered", multiCentered);

    // send clinical discharge details as request attribute
    DynaBean clinicalDischargeDetails = disDao.getClinicalDischargeDetails(patientId);
    request.setAttribute("clinicalDischargeDetails", clinicalDischargeDetails);

    // send financial discharge details as request attribute
    DynaBean financialDischargeDetails = disDao.getFinancialDischargeDetails(patientId);
    request.setAttribute("financialDischargeDetails", financialDischargeDetails);

    // send report finalized discharge details as request attribute
    DynaBean reportFinalizedDetails = disDao.getReportFinalizedDetails(patientId);
    request.setAttribute("reportFinalizedDetails", reportFinalizedDetails);

    // send report finalized discharge details as request attribute
    DynaBean physicalDischargeDetails = disDao.getPhysicalDischargeDetails(patientId);
    request.setAttribute("physicalDischargeDetails", physicalDischargeDetails);

    // send initiate discharge details as request attribute
    DynaBean initiateDischargeDetails = disDao.getInitiateDischargeDetails(mrNo, patientId);
    request.setAttribute("initiateDischargeDetails", initiateDischargeDetails);

    // Checking if the logged in user is doctor or not
    DynaBean userDetails =
        UserDAO.getUserBean((String) request.getSession().getAttribute("userid"));
    String userDocId = (String) userDetails.get("doctor_id");
    if (userDocId != null && !userDocId.isEmpty()) {
      request.setAttribute("userIsDoc", true);
      request.setAttribute("docId", userDocId);
      request.setAttribute(
          "docName", (String) DoctorMasterDAO.getDoctorById(userDocId).get("doctor_name"));

    } else {
      request.setAttribute("userIsDoc", false);
      request.setAttribute("docId", "");
      request.setAttribute("docName", "");
    }

    // get patient indents in open status
    List<BasicDynaBean> openPatientIndentsList = spiDao.getOpenPatientIndents(patientId);
    request.setAttribute("noOpenPatientIndents", openPatientIndentsList.size());

    // get pending ward activities
    List<BasicDynaBean> pendingWardActivities = patActDao.getPendingWardActivities(patientId);
    request.setAttribute("noPendingWardActivites", pendingWardActivities.size());

    // get pending operations and orders
    List<BasicDynaBean> pendingOperations = orderDao.getPendingOperations(patientId);
    List<BasicDynaBean> pendingOrders = orderDao.getPendingOrders(patientId);
    request.setAttribute(
        "noPendingOrdersOperations", pendingOperations.size() + pendingOrders.size());
    
    BasicDynaBean genericPreferences = GenericPreferencesDAO.getAllPrefs();

    request.setAttribute(
        "dischargeForPendingIndent", genericPreferences.get("discharge_for_pending_indent"));

    return mapping.findForward("dischargescreen");
  }

  /**
   * Discharges patient visit and updates patient registration tables.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward discharge(
      ActionMapping mapping,
      ActionForm af,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {
    HttpSession session = request.getSession(false);
    Connection con = null;
    boolean result = false;
    String msg = "Successfully Discharged";
    DischargeSummaryBOImpl dischargeDao = new DischargeSummaryBOImpl();
    Map params = request.getParameterMap();
    Map<String, Object> patientDetailsMap = new HashMap<String, Object>();
    String patientID = ((String[]) params.get("patient_id"))[0];
    String mrNo = ((String[]) params.get("mrNo"))[0];
    boolean deadPatient = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      
      String nokContact = request.getParameter("nok_contact_d");
      String dischargeTypeIdStr = request.getParameter("discharge_type");
      
      Integer dischargeTypeId = null;
      if (dischargeTypeIdStr == null || dischargeTypeIdStr.isEmpty()) {
        dischargeTypeId = null;
      } else {
        dischargeTypeId = Integer.parseInt(dischargeTypeIdStr);
      }

      BasicDynaBean patientRegistration =
          patientRegistrationDAO.findByKey(con, "patient_id", patientID);
      String oldDischargeStatus = "";
      if (patientRegistration.get("patient_discharge_status") != null) {
        oldDischargeStatus = (String) patientRegistration.get("patient_discharge_status");
      }
      patientRegistration.set("discharge_type_id", dischargeTypeId);
      String transferToHosp = request.getParameter("transfer_destination");
      patientRegistration.set("transfer_destination", transferToHosp);
      try {
        patientRegistration.set("reason_for_referral_id", 
            Integer.parseInt(request.getParameter("reason_for_referral")));  
      } catch (NumberFormatException ex) {
        patientRegistration.set("reason_for_referral_id", null);
      }
      

      ActionForward errForward = copyToDynaBean(request, response, patientRegistration);
      if (errForward != null) {
        return errForward;
      }
      
      BasicDynaBean patientDetailsBean = patientDetailsDAO.getBean();
      
      new PatientDetailsDAO().loadByteaRecords(patientDetailsBean, "mr_no", mrNo);
      errForward = copyToDynaBean(request, response, patientDetailsBean);
      if (errForward != null) {
        return errForward;
      }
      patientDetailsMap.put("mr_no", mrNo);
      
      String disDate = ((String[]) params.get("discharge_date"))[0];
      String disTime = ((String[]) params.get("discharge_time"))[0];
      
      if (disDate != null && disTime != null) {
        patientDetailsMap.put("discharge_date", disDate);
        patientDetailsMap.put("discharge_time", disTime);
      }
      patientDetailsMap.put("visit_id", patientID);
      patientDetailsMap.put("patient_name", (String) patientDetailsBean.get("patient_name"));
      patientDetailsMap.put("patient_contact", (String) patientDetailsBean.get("patient_phone"));
      patientDetailsMap.put("patient_mail", (String) patientDetailsBean.get("email_id"));
      patientDetailsMap.put("relation", (String) patientDetailsBean.get("relation"));
      patientDetailsMap.put("admission_date", patientRegistration.get("reg_date"));
      patientDetailsMap.put("admission_time", patientRegistration.get("reg_time"));
      patientDetailsMap.put(
          "discharge_doctor_id", (String) patientRegistration.get("discharge_doctor_id"));
      patientDetailsMap.put("referral_doctor_id", patientRegistration.get("reference_docto_id"));
      patientDetailsMap.put("bed_name", request.getParameter("bed_name"));
      patientDetailsMap.put("ward_name", request.getParameter("ward_name"));
      patientDetailsMap.put("dept_name", request.getParameter("deptName"));
      patientDetailsMap.put("visit_type", (String) patientRegistration.get("visit_type"));
      patientDetailsMap.put("lang_code", PatientDetailsDAO.getContactPreference(mrNo));

      HashMap<String, Object> physicalDischargeData =
          dischargeDao.getPhysicalDischargeTokens(patientDetailsMap);
      result =
          dischargeDao.dischargePatient(
              con,
              mrNo,
              patientID,
              (String) session.getAttribute("userid"),
              disDate,
              disTime,
              patientRegistration,
              patientDetailsBean);

      if (result && dischargeTypeId == 3) {
        deadPatient = true;
      }
      MessageManager mgr = new MessageManager();
      boolean sendCommunication = result
          && MessageUtil.allowMessageNotification(request, "general_message_send")
          && physicalDischargeData.get("visit_type").equals("i");
      if (sendCommunication && dischargeTypeId != 3) {
        physicalDischargeData.put("receipient_id__", mrNo);
        physicalDischargeData.put("recipient_mobile", physicalDischargeData.get("patient_contact"));
        physicalDischargeData.put("recipient_email", physicalDischargeData.get("patient_mail"));
        mgr.processEvent("patient_on_discharge", physicalDischargeData);

        physicalDischargeData.put("recipient_mobile", nokContact);
        physicalDischargeData.put("recipient_email", null);
        mgr.processEvent("inform_nok_on_patient_discharge", physicalDischargeData);

      }
      if (sendCommunication) {
        physicalDischargeData.put("receipient_id__",
            physicalDischargeData.get("discharge_doctor_id"));
        physicalDischargeData.put("recipient_email", physicalDischargeData.get("doctor_mail"));
        physicalDischargeData.put("recipient_mobile", physicalDischargeData.get("doctor_mobile"));
        mgr.processEvent("patient_physical_discharge", physicalDischargeData);
      }
      if (!oldDischargeStatus.equals("D") && sendCommunication) {
        List<String> signOffList = new ArrayList<String>();
        List<BasicDynaBean> reportList = LaboratoryDAO.getAllReportsForPatientId(patientID);
        String contextPath = RequestContext.getRequest().getServletContext().getRealPath("");
        if (null != reportList && reportList.size() > 0) {
          for (BasicDynaBean reportListBean : reportList) {
            if (reportListBean.get("report_id") != null) {
              int reportId = (Integer) reportListBean.get("report_id");
              String reportIdStr = Integer.toString(reportId);
              signOffList.add(reportIdStr);
            }
          }
          String[] signOff = signOffList.toArray(new String[signOffList.size()]);
          Map<String, Object> jobData = new HashMap<>();
          jobData.put("report_id", signOff);
          jobData.put("path", contextPath);
          mgr.processEvent("ip_phr_diag_share", jobData);
        }
      }
      FlashScope flash = FlashScope.getScope(request);
      if (result) {
        // set communication preference as N for dead patients
        if (deadPatient) {
          PatientDetailsDAO patientDetailDao = new PatientDetailsDAO();
          patientDetailDao.updateContactPreference(mrNo, "en", "N");
        }
        interfaceEventMappingService.physicalDischargeEvent(patientID);
        flash.success(msg);
      } else {
        flash.error("Failed to  Discharge");
      }
      String url = "DischargePatient.do?_method=getDischargeDetails&patientid=" + patientID;
      ActionRedirect redirect = new ActionRedirect(url);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } finally {
      DataBaseUtil.commitClose(con, result);
    }
  }

  /**
   * Initiate discharge.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  
  public ActionForward initiateDischarge(
      ActionMapping mapping,
      ActionForm af,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, SQLException, ParseException {
    Boolean status = false;
    String patientId = request.getParameter("patient_id");
    String mrNo = request.getParameter("mrNo");
    String nokContact = request.getParameter("nok_contact_i");
    String initiateCheck = request.getParameter("initiate_check");
    String initiateExpectedDischargeDate = request.getParameter("initiate_expected_discharge_date");
    String initiateExpectedDischargeTime = request.getParameter("initiate_expected_discharge_time");
    String initiateDischargeDoctor = request.getParameter("initiate_discharge_doctor_id");
    String initiateDischargeUser = request.getParameter("initiate_dischargeuser");
    String initiateDischargeRemarks = request.getParameter("initiate_discharge_remarks");
    Boolean initiateDischargeStatus;
    
    if (initiateCheck != null) {
      initiateDischargeStatus = true;
    } else {
      initiateDischargeStatus = false;
    }
    if (disDao.checkIfPatientDischargeEntryExists(patientId) != null) {
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      try {
        status =
            disDao.updateInitiateDischargeDetails(
                con,
                patientId,
                initiateDischargeStatus,
                initiateDischargeDoctor,
                initiateExpectedDischargeDate,
                initiateExpectedDischargeTime,
                initiateDischargeRemarks,
                initiateDischargeUser);
      } finally {
        DataBaseUtil.commitClose(con, status);
      }
    } else {
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try {
        disDao.insertInitiateDischargeDetails(
            con,
            patientId,
            initiateDischargeStatus,
            initiateDischargeDoctor,
            initiateExpectedDischargeDate,
            initiateExpectedDischargeTime,
            initiateDischargeRemarks,
            initiateDischargeUser);
        status = true;
      } finally {
        DataBaseUtil.commitClose(con, status);
      }
    }
    DynaBean initiateDischargeDetails = disDao.getInitiateDischargeDetails(mrNo, patientId);
    Boolean initiateDischargeFlag =
        (Boolean) initiateDischargeDetails.get("initiate_discharge_status");
    String initiateDischargeDate = null;
    String initiateDischargeTime = null;
    if (initiateDischargeDetails.get("initiate_discharging_date") != null) {
      initiateDischargeDate =
          new SimpleDateFormat("dd-MM-yyyy")
              .format(initiateDischargeDetails.get("initiate_discharging_date"));
    }
    if (initiateDischargeDetails.get("initiate_discharging_date") != null) {
      initiateDischargeTime =
          (String)
              new SimpleDateFormat("HH:mm")
                  .format(initiateDischargeDetails.get("initiate_discharging_time"));
    }
    String patientName = (String) initiateDischargeDetails.get("patient_name");
    String patientContact = (String) initiateDischargeDetails.get("patient_phone");
    String patientMail = (String) initiateDischargeDetails.get("email_id");
    boolean isSuccess =
        initiateDischargeFlag
            && status
            && MessageUtil.allowMessageNotification(request, "general_message_send");
    if (isSuccess && request.getParameter("visit_type").equals("i")) {
      Map<String, Object> patientDischargeData = new HashMap<String, Object>();
      patientDischargeData.put("patient_name", patientName);
      patientDischargeData.put("receipient_id__", mrNo);
      patientDischargeData.put("visit_id", patientId);
      if (initiateDischargeDate != null && initiateDischargeTime != null) {
        patientDischargeData.put("initiate_discharging_date", initiateDischargeDate);
        patientDischargeData.put("initiate_discharging_time", initiateDischargeTime);
      }
      patientDischargeData.put(
          "expected_discharge_date",
          new SimpleDateFormat("dd-MM-yyyy")
              .format(initiateDischargeDetails.get("expected_discharge_date")));
      patientDischargeData.put(
          "expected_discharge_time",
          new SimpleDateFormat("HH:mm")
              .format(initiateDischargeDetails.get("expected_discharge_time")));
      patientDischargeData.put("recipient_mobile", patientContact);
      patientDischargeData.put("recipient_email", patientMail);
      patientDischargeData.put("visit_type", request.getParameter("visit_type"));
      patientDischargeData.put("discharge_state", "Initiate Discharge");
      patientDischargeData.put("discharge_status", "I");
      MessageManager mgr = new MessageManager();
      mgr.processEvent("patient_on_discharge", patientDischargeData);
    }
    if (isSuccess && request.getParameter("visit_type").equals("i")) {
      Map<String, Object> patientDischargeDataNok = new HashMap<String, Object>();
      patientDischargeDataNok.put("patient_name", patientName);
      patientDischargeDataNok.put("receipient_id__", mrNo);
      patientDischargeDataNok.put("visit_id", patientId);
      if (initiateDischargeDate != null && initiateDischargeTime != null) {
        patientDischargeDataNok.put("initiate_discharging_date", initiateDischargeDate);
        patientDischargeDataNok.put("initiate_discharging_time", initiateDischargeTime);
      }
      patientDischargeDataNok.put(
          "expected_discharge_date",
          new SimpleDateFormat("dd-MM-yyyy")
              .format(initiateDischargeDetails.get("expected_discharge_date")));
      patientDischargeDataNok.put(
          "expected_discharge_time",
          new SimpleDateFormat("HH:mm")
              .format(initiateDischargeDetails.get("expected_discharge_time")));
      patientDischargeDataNok.put("recipient_mobile", nokContact);
      patientDischargeDataNok.put("discharge_state", "Initiate Discharge");
      patientDischargeDataNok.put("discharge_status", "I");
      patientDischargeDataNok.put("lang_code", PatientDetailsDAO.getContactPreference(mrNo));
      MessageManager mgr = new MessageManager();
      mgr.processEvent("inform_nok_on_patient_discharge", patientDischargeDataNok);
    }

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    Map map = new HashMap();
    String initiateDischargeEnteredBy = (String) initiateDischargeDetails.get("temp_username");
    
    map.put("initiate_discharge_status", initiateDischargeFlag);
    map.put("initiate_entered_by", initiateDischargeEnteredBy);
    map.put("initiate_discharging_date", initiateDischargeDate);
    map.put("initiate_discharging_time", initiateDischargeTime);
    
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.serialize(map, response.getWriter());
    response.flushBuffer();
    return null;
  }

  /**
   * Clinical discharge.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  
  public ActionForward clinicalDischarge(
      ActionMapping mapping,
      ActionForm af,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, SQLException {
    String patientId = request.getParameter("patient_id");
    String clinicalDischarge = request.getParameter("clinicaldischarge");
    Boolean clinicalDischargeBoolean = null;
    if (clinicalDischarge == null) {
      clinicalDischargeBoolean = false;
    } else if (clinicalDischarge.equals("yes")) {
      clinicalDischargeBoolean = true;
    }
    String clinicalDischargeUser = request.getParameter("clinicaldischargeuser");
    String clinicalDischargeComments = request.getParameter("clinicaldischargecomments");

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);

    Boolean status = false;
    try {
      if (disDao.checkIfPatientDischargeEntryExists(patientId) == null) {
        disDao.insertClinicalDischargeDetails(
            con,
            patientId,
            clinicalDischargeBoolean,
            clinicalDischargeUser,
            clinicalDischargeComments);
      } else {
        disDao.updateClinicalDischargeDetails(
            con,
            patientId,
            clinicalDischargeBoolean,
            clinicalDischargeUser,
            clinicalDischargeComments);
      }
      status = true;
    } finally {
      DataBaseUtil.commitClose(con, status);
    }

    // get clinical discharge details
    BasicDynaBean clinicalDischargeDetails = disDao.getClinicalDischargeDetails(patientId);
    String clinicalDischargeDate = null;
    String clinicalDischargeTime = null;
    Map clinicalDischargeDetailsMap = new HashMap();
    Boolean clinicalDischargeFlag =
        (Boolean) clinicalDischargeDetails.get("clinical_discharge_flag");
    if (clinicalDischargeDetails.get("clinical_discharging_date") != null) {
      clinicalDischargeDate =
          new SimpleDateFormat("dd-MM-yyyy")
              .format(clinicalDischargeDetails.get("clinical_discharging_date"));
    }
    if (clinicalDischargeDetails.get("clinical_discharging_time") != null) {
      clinicalDischargeTime =
          new SimpleDateFormat("HH:mm")
              .format(clinicalDischargeDetails.get("clinical_discharging_time"));
    }
    clinicalDischargeDetailsMap.put("clinical_discharge_flag", clinicalDischargeFlag);
    String clinicalEnteredBy = (String) clinicalDischargeDetails.get("temp_username");
    clinicalDischargeDetailsMap.put("clinical_entered_by", clinicalEnteredBy);
    clinicalDischargeDetailsMap.put("clinical_discharging_date", clinicalDischargeDate);
    clinicalDischargeDetailsMap.put("clinical_discharging_time", clinicalDischargeTime);
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");

    js.serialize(clinicalDischargeDetailsMap, response.getWriter());

    response.flushBuffer();
    return null;
  }

  /**
   * Gets the initiate discharge details.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the initiate discharge details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  
  public ActionForward getInitiateDischargeDetails(
      ActionMapping mapping,
      ActionForm af,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, SQLException {
    String patientId = request.getParameter("patientId");
    String mrNo = request.getParameter("mrno");
    BasicDynaBean initiateDischargeDetails = disDao.getInitiateDischargeDetails(mrNo, patientId);
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    Map initiateDischargeDetailsMap = null;
    if (initiateDischargeDetails != null) {
      initiateDischargeDetailsMap = initiateDischargeDetails.getMap();
    }
    js.serialize(initiateDischargeDetailsMap, response.getWriter());

    response.flushBuffer();
    return null;
  }

  /**
   * Gets the clinical discharge details.
   *
   * @param mapping the mapping
   * @param af the af
   * @param request the request
   * @param response the response
   * @return the clinical discharge details
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  
  public ActionForward getClinicalDischargeDetails(
      ActionMapping mapping,
      ActionForm af,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, SQLException {
    String patientId = request.getParameter("patientId");
    BasicDynaBean clinicalDischargeDetails = disDao.getClinicalDischargeDetails(patientId);
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    Map clinicalDischargeDetailsMap = null;
    if (clinicalDischargeDetails != null) {
      clinicalDischargeDetailsMap = clinicalDischargeDetails.getMap();
    }

    js.serialize(clinicalDischargeDetailsMap, response.getWriter());

    response.flushBuffer();
    return null;
  }
}
