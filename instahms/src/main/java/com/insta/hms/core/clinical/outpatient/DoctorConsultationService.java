package com.insta.hms.core.clinical.outpatient;

import com.bob.hms.common.Constants;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DateHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.medicalrecords.MRDUpdateService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.doctors.DoctorService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

/**
 * The Class DoctorConsultationService.
 */
@Service
public class DoctorConsultationService {

  /** The doc consultation repo. */
  @LazyAutowired
  private DoctorConsultationRepository docConsultationRepo;

  /** The doctor form validator. */
  @LazyAutowired
  DoctorFormValidator doctorFormValidator;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The m RD update service. */
  @LazyAutowired
  private MRDUpdateService mrdUpdateService;

  /** The appointment service. */
  @LazyAutowired
  AppointmentService appointmentService;

  /** The center preferences service. */
  @LazyAutowired
  private CenterPreferencesService centerPrefService;
  
  private static Logger logger = LoggerFactory.getLogger(DoctorConsultationService.class);

  /**
   * List visit consultations.
   *
   * @param visitId the visit id
   * @return the list
   */
  public List<BasicDynaBean> listVisitConsultations(String visitId) {
    return docConsultationRepo.listVisitConsultations(visitId);
  }

  /**
   * Find by key.
   *
   * @param consultationId the consultation id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int consultationId) {
    return docConsultationRepo.findByKey("consultation_id", consultationId);
  }

  /**
   * Gets the consultation fields values.
   *
   * @param consultationId the consultation id
   * @param allFields the all fields
   * @param notEmptyFieldValues the not empty field values
   * @return the consultation fields values
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getConsultationFieldsValues(int consultationId, boolean allFields,
      boolean notEmptyFieldValues) {
    return docConsultationRepo.getConsultationFieldsValues(consultationId, allFields,
        notEmptyFieldValues);
  }

  /**
   * Gets the doctor consult details.
   *
   * @param consultId the consult id
   * @return the doctor consult details
   */
  public BasicDynaBean getDoctorConsultDetails(int consultId) {
    return docConsultationRepo.getDoctorConsultDetails(consultId);
  }

  /**
   * List all.
   *
   * @param patientId the patient id
   * @return the list
   */
  public List<BasicDynaBean> listAll(String patientId) {
    return docConsultationRepo.listAll(null, "patient_id", patientId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return docConsultationRepo.getBean();
  }

  /**
   * Consultation summary info.
   *
   * @param consultationId the consultation id
   * @return the basic dyna bean
   */
  public BasicDynaBean consultationSummaryInfo(int consultationId) {
    return docConsultationRepo.consultationSummaryInfo(consultationId);
  }

  /**
   * Gets the triage summary.
   *
   * @param consultationId the consultation id
   * @return the triage summary
   */
  public BasicDynaBean getTriageSummary(Integer consultationId) {
    return docConsultationRepo.getTriageSummary(consultationId);
  }

  /**
   * Gets the initial assessment summary.
   *
   * @param consultationId the consultation id
   * @return the initial assessment summary
   */
  public BasicDynaBean getInitialAssessmentSummary(Integer consultationId) {
    return docConsultationRepo.getInitialAssessmentSummary(consultationId);
  }

  /**
   * Gets the patient consultation details list.
   *
   * @param mrNo the mr no
   * @param isDoctorLogin the is doctor login
   * @return the patient consultation details list
   */
  public List<BasicDynaBean> getPatientConsultationDetailsList(String mrNo, boolean isDoctorLogin) {

    return docConsultationRepo.getPatientConsultationDetailsList(mrNo, isDoctorLogin);
  }

  /**
   * Gets the patient triage details list.
   *
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param centerId the center id
   * @return the patient triage details list
   */
  public List<BasicDynaBean> getPatientTriageDetailsList(String mrNo, String doctorId,
      Integer centerId) {
    return docConsultationRepo.getPatientTriageDetailsList(mrNo, doctorId, centerId);
  }

  /**
   * Gets the patient initial assessment details list.
   *
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param centerId the center id
   * @return the patient initial assessment details list
   */
  public List<BasicDynaBean> getPatientInitialAssessmentDetailsList(String mrNo, String doctorId,
      Integer centerId) {
    return docConsultationRepo.getPatientInitialAssessmentDetailsList(mrNo, doctorId, centerId);
  }

  /**
   * Gets the consultation list for print.
   *
   * @param mrNo the mr no
   * @param isDoctorLogin the is doctor login
   * @return the consultation list for print
   */
  public List<BasicDynaBean> getConsultationListForPrint(String mrNo, boolean isDoctorLogin) {
    return docConsultationRepo.getConsultationListForPrint(mrNo, isDoctorLogin);
  }

  /**
   * Gets the consultation.
   *
   * @param consultationId the consultation id
   * @return the consultation
   */
  public BasicDynaBean getConsultation(Integer consultationId) {
    return docConsultationRepo.getConsultation(consultationId);
  }

  /**
   * Gets the previous consultations.
   *
   * @param consId the cons id
   * @return the previous consultations
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPreviousConsultations(Integer consId,
      Boolean isCurrentConsRequired) {
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    BasicDynaBean prefs = centerPrefService.getCenterPreferences(centerId);
    BasicDynaBean currentConsultationBean = docConsultationRepo.getDoctorConsultDetails(consId);
    return ConversionUtils
        .listBeanToListMap(docConsultationRepo.getPreviousConsultations(currentConsultationBean,
            (String) prefs.get("op_consultation_data_access"), isCurrentConsRequired));
  }

  /**
   * Gets the previous consultations by doctor.
   *
   * @param consId the cons id
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @return the previous consultations by doctor
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getPreviousConsultationsByDoctor(Integer consId, String mrNo,
      String doctorId) {
    return ConversionUtils.listBeanToListMap(
        docConsultationRepo.getPreviousConsultationsByDoctor(consId, mrNo, doctorId));
  }

  /**
   * Gets the consultation notes template.
   *
   * @param templateId the template id
   * @return the consultation notes template
   */
  public List<BasicDynaBean> getConsultationNotesTemplate(int templateId) {
    return docConsultationRepo.getConsultationNotesTemplate(templateId);
  }

  /**
   * Update consltation.
   *
   * @param bean the bean
   * @param params the params
   * @return the integer
   */
  public Integer updateConsltation(BasicDynaBean bean, FormParameter params) {
    Map<String, Object> key = new HashMap<>();
    key.put("consultation_id", params.getId());
    docConsultationRepo.update(bean, key);
    return 0;
  }

  /**
   * Gets the consultation details.
   *
   * @param patientId the patient id
   * @return the consultation details
   */
  public List<BasicDynaBean> getConsultationDetails(String patientId) {
    return docConsultationRepo.getConsultationDetails(patientId);
  }

  /**
   * Open reopen consultation.
   *
   * @param map the map
   * @return the map
   */
  public Map<String, String> openReopenConsultation(Map<String, String[]> map) {
    String consultationResponse = docConsultationRepo.openReopenConsultation(
        Integer.parseInt((String) map.get("consultation_id")[0]),
        Integer.parseInt((String) map.get("ticket_id")[0]), map.get("patient_id")[0]);
    Map<String, String> responseMap = new HashMap<String, String>();
    responseMap.put("status", consultationResponse);
    return responseMap;
  }

  /**
   * Gets the value.
   *
   * @param key the key
   * @param params the params
   * @param sendNull the send null
   * @return the value
   */
  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params, boolean sendNull) {
    Object obj = params.get(key);
    if (sendNull && obj == null) {
      return null;
    } else if (obj != null) {
      return obj;
    }
    return "";
  }

  /**
   * Gets the value.
   *
   * @param key the key
   * @param params the params
   * @return the value
   */
  @SuppressWarnings("rawtypes")
  private Object getValue(String key, Map params) {
    return getValue(key, params, false);
  }

  /**
   * Gets the OT doctor charges bean.
   *
   * @param doctorId the doctor id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the OT doctor charges bean
   */
  public BasicDynaBean getOTDoctorChargesBean(String doctorId, String bedType, String orgId) {
    return docConsultationRepo.getOTDoctorChargesBean(doctorId, bedType, orgId);
  }

  /**
   * Save specific consultation data.
   *
   * @param reqMap the req map
   * @param params the params
   * @param errorMap the error map
   * @return the map
   * @throws ParseException the parse exception
   */
  @SuppressWarnings({"static-access", "unchecked", "deprecation"})
  public Map<String, Object> saveSpecificConsultationData(Map<String, Object> reqMap,
      FormParameter params, ValidationErrorMap errorMap) throws ParseException {
    Map<String, Object> responseData = new HashMap<>();
    BasicDynaBean updateConstBean = getBean();
    BasicDynaBean oldConsBean = findByKey((int) params.getId());
    int docId = (int) oldConsBean.get("doc_id");
    if (docId == 0) {
      docId = docConsultationRepo.getDocId();
      updateConstBean.set("doc_id", docId);
    }

    if (reqMap.get("start_datetime") != null && !reqMap.get("start_datetime").equals("")) {
      updateConstBean.set("start_datetime",
          new DateUtil().parseTimestamp((String) reqMap.get("start_datetime")));
    }
    if (reqMap.get("end_datetime") != null && !reqMap.get("end_datetime").equals("")) {
      updateConstBean.set("end_datetime",
          new DateUtil().parseTimestamp((String) reqMap.get("end_datetime")));
    } else {
      updateConstBean.set("end_datetime", null);
    }

    String consultationClose = (String) getValue("close_consultation", reqMap);
    if (consultationClose != null && consultationClose.equals("Y")) {
      updateConstBean.set("status", "C");
      updateConstBean.set("consultation_complete_time",
          new java.sql.Timestamp((new java.util.Date()).getTime()));
      if (reqMap.get("end_datetime") == null || reqMap.get("end_datetime").equals("")) {
        updateConstBean.set("end_datetime",
            new java.sql.Timestamp((new java.util.Date()).getTime()));
      }
      // update doctor scheduler appointment on consultation close
      int appointmentId = (int) oldConsBean.get("appointment_id");
      if (appointmentId != 0) {
        Map<String, Object> app = new HashMap<>();
        app.put("appointment_id", appointmentId);
        app.put("appointment_status", "Completed");
        app.put("category", "DOC");
        List updateApp = new ArrayList<>();
        updateApp.add(app);
        Map<String, Object> apptParam = new HashMap<>();
        apptParam.put("update_app_status", updateApp);
        appointmentService.updateAppointmentsStatus(apptParam);
      }
    } else {
      updateConstBean.set("status", "P");
    }
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    updateConstBean.set("prescription_notes", reqMap.get("prescription_notes"));
    updateConstBean.set("consultation_mod_time", DateUtil.getCurrentTimestamp());
    updateConstBean.set("cons_revision_number",
        new BigDecimal(DateUtil.getCurrentTimestamp().getTime()));
    updateConstBean.set("username", userName);
    if (reqMap.containsKey(Constants.DISCHARGE_PRESCRIPTION_NOTES)
            && reqMap.get(Constants.DISCHARGE_PRESCRIPTION_NOTES) != null) {
      updateConstBean.set(Constants.DISCHARGE_PRESCRIPTION_NOTES,
              reqMap.get(Constants.DISCHARGE_PRESCRIPTION_NOTES));
    }
    if (!doctorFormValidator.validateConsultationDetails(updateConstBean, oldConsBean, errorMap)) {
      return responseData;
    }
    Map<String, Object> key = new HashMap<>();
    key.put("consultation_id", params.getId());

    // To solve Multi-Tab issue
    key.put("cons_revision_number", reqMap.get("cons_revision_number"));

    if (docConsultationRepo.update(updateConstBean, key) == 0) {
      errorMap.addError("other", "exception.consultation.saved");
      return responseData;
    }

    boolean isvalid = true;
    String consStatus = (String) oldConsBean.get("status");
    isvalid = doctorFormValidator.validateConsultationStatus(consStatus, errorMap);
    if (!isvalid) {
      return responseData;
    }

    // return the reponse data
    Timestamp closingTime = (Timestamp) updateConstBean.get("consultation_complete_time");
    long numberOfSecTillDate = 0;
    if (closingTime != null) {
      long diff =
          ((new Timestamp(new java.util.Date().getTime())).getTime() - closingTime.getTime());
      numberOfSecTillDate = diff / (1000);
    }
    responseData = updateConstBean.getMap();
    responseData.put("sec_till_date", numberOfSecTillDate);
    responseData.remove("consultation_complete_time");
    responseData.remove("consultation_mod_time");
    responseData.remove("username");
    responseData.remove("doc_id");

    return responseData;
  }

  /**
   * Save triage data.
   *
   * @param requestBody the request body
   * @param parameters the parameters
   * @param errMap the err map
   * @return the map
   * @throws ParseException the parse exception
   */
  public Map<String, Object> saveTriageData(Map<String, Object> requestBody,
      FormParameter parameters, ValidationErrorMap errMap) throws ParseException {
    BasicDynaBean bean = docConsultationRepo.getBean();
    bean.set("username", (String) sessionService.getSessionAttributes().get("userId"));
    bean.set("triage_done", "P");
    bean.set("emergency_category", requestBody.get("emergency_category"));
    if (requestBody.get("triage_start_datetime") != null
        && !requestBody.get("triage_start_datetime").equals("")) {
      bean.set("triage_start_datetime",
          new DateUtil().parseTheTimestamp((String) requestBody.get("triage_start_datetime")));
    }

    if (requestBody.get("triage_end_datetime") != null
        && !requestBody.get("triage_end_datetime").equals("")) {
      bean.set("triage_end_datetime",
          new DateUtil().parseTheTimestamp((String) requestBody.get("triage_end_datetime")));
    } else {
      bean.set("triage_end_datetime", null);
    }
    if ("Y".equals(requestBody.get("triage_done"))) {
      bean.set("triage_done", "Y");
      bean.set("triage_complete_time", new java.sql.Timestamp((new java.util.Date()).getTime()));
      if (requestBody.get("triage_end_datetime") == null) {
        bean.set("triage_end_datetime", new java.sql.Timestamp((new java.util.Date()).getTime()));
      }
    }
    bean.set("triage_revision_number", new BigDecimal(DateUtil.getCurrentTimestamp().getTime()));
    Map<String, Object> keys = new HashMap<>();
    keys.put("consultation_id", parameters.getId());

    // To solve Multi-Tab issue
    keys.put("triage_revision_number", requestBody.get("triage_revision_number"));

    if (!doctorFormValidator.validateTriageEndDateTime(bean, errMap)) {
      return null;
    }

    if (docConsultationRepo.update(bean, keys) == 0) {
      errMap.addError("other", "exception.triage.saved");
      return null;
    }
    // return the reponse data
    Timestamp closingTime = (Timestamp) bean.get("triage_complete_time");
    long numberOfSecTillDate = 0;
    if (closingTime != null) {
      long diff =
          ((new Timestamp(new java.util.Date().getTime())).getTime() - closingTime.getTime());
      numberOfSecTillDate = diff / (1000);
    }
    Map<String, Object> response = new HashMap<>();
    response.put("sec_till_date", numberOfSecTillDate);
    response.put("triage_status", bean.get("triage_done"));
    response.put("emergency_category", bean.get("emergency_category"));
    response.put("triage_start_datetime", bean.get("triage_start_datetime"));
    response.put("triage_end_datetime", bean.get("triage_end_datetime"));
    response.put("triage_revision_number", bean.get("triage_revision_number"));
    return response;
  }

  /**
   * Save initial assessment.
   *
   * @param requestBody the request body
   * @param parameters the parameters
   * @param errMap the err map
   * @return the map
   * @throws ParseException the parse exception
   */
  public Map<String, Object> saveInitialAssessment(Map<String, Object> requestBody,
      FormParameter parameters, ValidationErrorMap errMap) throws ParseException {
    BasicDynaBean bean = docConsultationRepo.getBean();
    bean.set("initial_assessment_status", "P");
    // bean.set("emergency_category", requestBody.get("emergency_category"));
    if (requestBody.get("ia_start_datetime") != null
        && !requestBody.get("ia_start_datetime").equals("")) {
      bean.set("ia_start_datetime",
          new DateUtil().parseTheTimestamp((String) requestBody.get("ia_start_datetime")));
    }

    if (requestBody.get("ia_end_datetime") != null
        && !requestBody.get("ia_end_datetime").equals("")) {
      bean.set("ia_end_datetime",
          new DateUtil().parseTheTimestamp((String) requestBody.get("ia_end_datetime")));
    } else {
      bean.set("ia_end_datetime", null);
    }
    if ("Y".equals(requestBody.get("initial_assessment_status"))) {
      bean.set("ia_complete_time",  DateUtil.getCurrentTimestamp());
      bean.set("initial_assessment_status", "Y");
      if (requestBody.get("ia_end_datetime") == null) {
        bean.set("ia_end_datetime", new java.sql.Timestamp((new java.util.Date()).getTime()));
      }
    }
    bean.set("ia_mod_time", DateUtil.getCurrentTimestamp());
    Map<String, Object> keys = new HashMap<>();
    keys.put("consultation_id", parameters.getId());

    // To solve Multi-Tab issue
    keys.put("ia_mod_time", DateHelper.getTimeStamp((String) requestBody.get("ia_mod_time"),
        "yyyy-MM-dd HH:mm:ss.SSS"));

    if (!doctorFormValidator.validateInitialAssessmentEndDateTime(bean, errMap)) {
      return null;
    }

    if (docConsultationRepo.update(bean, keys) == 0) {
      errMap.addError("other", "exception.assessment.saved");
      return null;
    }
    // return the reponse data
    Timestamp closingTime = (Timestamp) bean.get("ia_complete_time");
    long numberOfSecTillDate = 0;
    if (closingTime != null) {
      long diff =
          (DateUtil.getCurrentTimestamp().getTime() - closingTime.getTime());
      numberOfSecTillDate = diff / (1000);
    }
    Map<String, Object> response = new HashMap<>();
    response.put("sec_till_date", numberOfSecTillDate);
    response.put("initial_assessment_status", bean.get("initial_assessment_status"));
    response.put("emergency_category", bean.get("emergency_category"));
    response.put("ia_start_datetime", bean.get("ia_start_datetime"));
    response.put("ia_end_datetime", bean.get("ia_end_datetime"));
    response.put("ia_mod_time", bean.get("ia_mod_time").toString());
    return response;
  }

  /**
   * Update visit and consultation type.
   *
   * @param reqMap the req map
   * @param params the params
   * @param trxData the trx data
   * @param errorMap the error map
   */
  public void updateVisitAndConsultationType(Map<String, Object> reqMap, FormParameter params,
      boolean trxData, ValidationErrorMap errorMap) {
    boolean isValid = true;
    String opType = reqMap.get("visit_type") != null ? (String) reqMap.get("visit_type") : "";
    String patientId = params.getPatientId();
    BasicDynaBean regBean = registrationService.findByKey(patientId);
    String oldOpType = (String) regBean.get("op_type");
    isValid = doctorFormValidator.validateOpType(oldOpType, opType, errorMap);
    // FollowUp visit can only convert to Main visit not vice versa
    if (isValid) {
      BasicDynaBean visitBean = registrationService.getBean();
      visitBean.set("op_type", opType);
      visitBean.set("main_visit_id", patientId);
      Map<String, Object> key = new HashMap<String, Object>();
      key.put("patient_id", patientId);
      isValid = registrationService.update(visitBean, key) > 0;
    }
    String consTypeId = reqMap.get("consultation_type_id") != null
        ? (String) reqMap.get("consultation_type_id") : "";
    BasicDynaBean updateConstBean = getBean();
    if (isValid && doctorFormValidator.validateConstTypeId(consTypeId, errorMap)) {
      updateConstBean.set("head", consTypeId);
      if (trxData) {
        updateConstBean.set("status", "P");
      } else {
        updateConstBean.set("status", "A");
      }
      Map<String, Object> key = new HashMap<>();
      key.put("consultation_id", params.getId());
      docConsultationRepo.update(updateConstBean, key);
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      updateConstBean.set("consultation_id", params.getId());
      updateConstBean.set("username", userName);
      updateConsultationType(updateConstBean, errorMap); // update consultation charge

    }
  }

  /**
   * Update consultation type.
   *
   * @param constBean the const bean
   * @param errorMap the error map
   */
  public void updateConsultationType(BasicDynaBean constBean, ValidationErrorMap errorMap) {
    Integer consId = (Integer) constBean.get("consultation_id");
    String newConsTypeId = (String) constBean.get("head");
    BasicDynaBean consultRcord = docConsultationRepo.consultationSummaryInfo(consId);
    String orgId = (String) consultRcord.get("org_id");
    String bedType = (String) consultRcord.get("bed_type");
    BasicDynaBean consultationTypeDetails =
        doctorService.getConsultationCharges(Integer.parseInt(newConsTypeId), bedType, orgId);
    String codeType = (String) consultationTypeDetails.get("code_type");
    String itemCode = (String) consultationTypeDetails.get("item_code");
    String visitId = (String) consultRcord.get("visit_id");
    mrdUpdateService.updateDoctorChargesForCode(constBean, visitId, codeType, itemCode, errorMap);
  }

  /**
   * Reopen consultation.
   *
   * @param consultationId the consultation id
   * @param reopenRemarks the reopen remarks
   * @return the string
   */
  public String reopenConsultation(int consultationId, String reopenRemarks) {
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    BasicDynaBean oldConsBean = findByKey(consultationId);
    String consStatus = (String) oldConsBean.get("status");
    if (doctorFormValidator.validateStatus(consStatus)) {
      BasicDynaBean bean = docConsultationRepo.getBean();
      bean.set("username", userName);
      bean.set("status", "P");
      bean.set("reopen_remarks", reopenRemarks);
      bean.set("consultation_complete_time", null);
      bean.set("cons_reopened", true);
      Map<String, Object> key = new HashMap<>();
      key.put("consultation_id", consultationId);
      if (docConsultationRepo.update(bean, key) > 0) {
        consStatus = (String) bean.get("status");
      }
    }
    return consStatus;
  }

  /**
   * Reopens the Closed Triage Form.
   *
   * @param consultationId the consultation id
   * @param reopenRemarks the reopen remarks
   * @return true, if successful
   */
  public boolean reopenTriage(int consultationId, String reopenRemarks) {
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    BasicDynaBean oldConsBean = findByKey(consultationId);
    String status = (String) oldConsBean.get("triage_done");
    if (doctorFormValidator.validateTriageStatus(status)) {
      BasicDynaBean bean = docConsultationRepo.getBean();
      bean.set("username", userName);
      bean.set("triage_done", "P");
      bean.set("reopen_remarks_triage", reopenRemarks);
      bean.set("triage_complete_time", null);
      Map<String, Object> key = new HashMap<>();
      key.put("consultation_id", consultationId);
      return (docConsultationRepo.update(bean, key) > 0);
    }
    return false;
  }

  /**
   * Reopens the Closed Initial Assessment Form.
   *
   * @param consultationId the consultation id
   * @return true, if successful
   */
  public boolean reopenInitialAssessment(int consultationId, String reopenRemarks) {
    BasicDynaBean oldConsBean = findByKey(consultationId);
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String status = (String) oldConsBean.get("initial_assessment_status");
    if (doctorFormValidator.validateInitialAssessmentStatus(status)) {
      BasicDynaBean bean = docConsultationRepo.getBean();
      bean.set("username", userName);
      bean.set("initial_assessment_status", "P");
      bean.set("reopen_remarks_ia", reopenRemarks);
      bean.set("ia_complete_time", null);
      Map<String, Object> key = new HashMap<>();
      key.put("consultation_id", consultationId);
      return (docConsultationRepo.update(bean, key) > 0);
    }
    return false;
  }

  /**
   * Gets the doctor consultation charge.
   *
   * @param chargeId String
   * @return the doctor consultation charge
   */
  public DynaBean getDoctorConsultationCharge(String chargeId) {
    return docConsultationRepo.getDoctorConsultationCharge(chargeId);
  }

  /**
   * Gets the immunization details.
   *
   * @param consId the cons id
   * @return the immunization details
   */
  public BasicDynaBean getImmunizationDetails(Integer consId) {
    return docConsultationRepo.getImmunizationDetails(consId);
  }

  /**
   * Save immunization details.
   *
   * @param requestBody the request body
   * @param consId the cons id
   * @return the boolean
   */
  public Boolean saveImmunizationDetails(Map<String, Object> requestBody, Integer consId) {
    BasicDynaBean bean = docConsultationRepo.getBean();
    bean.set("immunization_status_upto_date", requestBody.get("immunization_status_upto_date"));
    bean.set("immunization_remarks", requestBody.get("immunization_remarks"));
    Map<String, Object> keys = new HashMap<>();
    keys.put("consultation_id", consId);
    return docConsultationRepo.update(bean, keys) == 1;
  }

  /**
   * Gets the consultation field values.
   *
   * @param consId the cons id
   * @return the consultation field values
   */
  public String getConsultationFieldValues(Integer consId) {
    return docConsultationRepo.getConsultationFieldValues(consId);
  }

  public String getMrNoForConsultationId(Integer consId) {
    return docConsultationRepo.getMrNoForConsulatation(consId);
  }

  /**
   * Check if the consultation id is valid.
   * @param consultationId consultation id
   * @return true if valid.
   */
  public Boolean isConsultationIdValid(String consultationId) {
    Integer integerConsultationId = null;
    try {
      integerConsultationId = Integer.parseInt(consultationId);
    } catch (NumberFormatException exception) {
      logger.error("Unable to parse:" + consultationId, exception);
      return false;
    }
    return docConsultationRepo.exist("consultation_id", integerConsultationId);
  }

  /**
   * Check if the consultation id is valid for the logged in user from instaapps.
   * @param consultationId consultation id
   * @param requestHandlerKey request handler key
   * @return true if valid.
   */
  public boolean validateConsultationIdForLoggedInPatient(Integer consultationId,
      String requestHandlerKey) {
    String mrNo = "";
    boolean patientLogin = false;
    WebApplicationContext context = (WebApplicationContext) ApplicationContextProvider
        .getApplicationContext();
    if (context != null) {
      ServletContext servletContext = context.getServletContext();
      Map<String, Object> sessionMap = (Map<String, Object>) servletContext
          .getAttribute("sessionMap");
      Map<String, Object> sessionParameters = (Map) sessionMap.get(requestHandlerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        patientLogin = (boolean) sessionParameters.get("patient_login");
        mrNo = (String) sessionParameters.get("customer_user_id");
      }
      if (patientLogin) {
        String mrNoResult = getMrNoForConsultationId(consultationId);
        return (mrNoResult.equalsIgnoreCase(mrNo));
      }
    }
    return true;
  }

  public Map<String, Object> getConsultationSaveEventSegmentData(int consId) {
    return docConsultationRepo.getConsultationSaveEventSegmentData(consId).getMap();
  }
}
