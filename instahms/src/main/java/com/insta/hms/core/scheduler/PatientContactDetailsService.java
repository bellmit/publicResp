package com.insta.hms.core.scheduler;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.salutations.SalutationService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientContactDetailsService.
 */
@SuppressWarnings({ "rawtypes" })
@Service
public class PatientContactDetailsService {

  /** The contact details repository. */
  @LazyAutowired
  private PatientContactDetailsRepository contactDetailsRepository;
  
  /** The patient details repository. */
  @LazyAutowired
  private PatientDetailsRepository patientDetailsRepository;
  
  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The center service. */
  @LazyAutowired
  private CenterService centerService;
  
  /** The appointment repository. */
  @LazyAutowired
  private AppointmentRepository appointmentRepository;
  
  /** The salutation service. */
  @LazyAutowired
  private SalutationService salutationService;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Insert contact details.
   *
   * @param contactInfo
   *          the contact info
   * @return the map
   */
  public Map insertContactDetails(Map contactInfo) {

    //action rights check
    String practoApptId = (String) contactInfo.get("practo_appointment_id");
    if (practoApptId == null) {
      Map<String, String> actionRightsMap =
          (Map<String, String>) securityService.getSecurityAttributes().get("actionRightsMap");
      Integer roleId = (Integer) sessionService.getSessionAttributes().get("roleId");
      if (roleId != 1 && roleId != 2 && (
          actionRightsMap != null 
          && actionRightsMap.get("add_edit_contact") != null 
          && actionRightsMap.get("add_edit_contact").equals("N"))) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("contact match", "exception.no.right.to.add.contact");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("contact", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }     
    }
    
    BasicDynaBean bean = contactDetailsRepository.getBean();
    bean.set("patient_name", contactInfo.get("patient_name") != null 
        ? ((String)contactInfo.get("patient_name")).trim() : null);
    bean.set("patient_gender", contactInfo.get("patient_gender"));
    bean.set("patient_contact", 
        appointmentService.generatePhoneNumber(
            (String) contactInfo.get("patient_contact")));
    bean.set("salutation_name", contactInfo.get("salutation_name"));
    bean.set("middle_name", contactInfo.get("middle_name") != null 
        ? ((String)contactInfo.get("middle_name")).trim() : null);
    bean.set("last_name", contactInfo.get("last_name") != null 
        ? ((String)contactInfo.get("last_name")).trim() : null);
    try {
      bean.set("patient_dob", DateUtil.parseDate((String) contactInfo.get("patient_dob")));
    } catch (ParseException exc) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("patient_age", "exception.scheduler.patientDob.invalid.date");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("patient", ex.getErrors());
      throw new NestableValidationException(nestedException);        
    }
    if (contactInfo.get("patient_age") != null 
        && !contactInfo.get("patient_age").equals("")) {
      bean.set("patient_age", Integer.parseInt((String) contactInfo.get("patient_age")));
      bean.set("patient_age_units", contactInfo.get("patient_age_units"));
    }
    bean.set("vip_status", contactInfo.get("vip_status"));
    bean.set("patient_email_id", contactInfo.get("patient_email_id"));
    bean.set("send_sms", contactInfo.get("send_sms"));
    bean.set("send_email", contactInfo.get("send_email"));
    bean.set("preferred_language", contactInfo.get("preferred_language"));
    if (practoApptId != null && !practoApptId.equalsIgnoreCase("")) {
      bean.set("mod_user", "InstaAdmin");
    } else {
      bean.set("mod_user", (String) sessionService.getSessionAttributes().get("userId"));
    }
    bean.set("create_time", new java.sql.Timestamp(new java.util.Date().getTime()));
    
    handlePhoneNumber((Integer) contactInfo.get("center_id"), bean);
    String apiUser = null;
    if (practoApptId == null) {
      apiUser = (String) sessionService.getSessionAttributes().get("apiUser");
    }
    Integer contactId = contactDetailsRepository.getContactIdIfContactExists(bean.getMap());
    // validate only for hms users
    if (apiUser == null && practoApptId == null) {
      // duplicate contacts check
      if (contactId != null) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("contact match", "exception.patient.exists");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("contact", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      
      // duplicate MR patient check
      if (patientDetailsRepository.checkIfPatientExists(bean.getMap())) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("contact match", "exception.patient.exists");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("contact", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }      
    }
    
    boolean success = false;
    if (contactId == null) {
      contactId = contactDetailsRepository.getNextSequence();
      bean.set("contact_id", contactId);
      success = contactDetailsRepository.insert(bean) > 0;
    }
    bean.set("contact_id", contactId);
    Map finalMap = new HashMap<>();
    finalMap.putAll(bean.getMap());
    finalMap.put("contact_id", contactId.toString());
    return finalMap;
  }

  /**
   * Update contact details.
   *
   * @param contactInfo
   *          the contact info
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map updateContactDetails(Map contactInfo) {
    BasicDynaBean bean = contactDetailsRepository.getBean();
    bean.set("patient_name", contactInfo.get("patient_name") != null 
        ? ((String)contactInfo.get("patient_name")).trim() : null);
    bean.set("patient_gender", contactInfo.get("patient_gender"));
    bean.set("patient_contact", 
        appointmentService.generatePhoneNumber(
            (String) contactInfo.get("patient_contact")));
    bean.set("salutation_name", contactInfo.get("salutation_name"));
    bean.set("middle_name", contactInfo.get("middle_name") != null 
        ? ((String)contactInfo.get("middle_name")).trim() : null);
    bean.set("last_name", contactInfo.get("last_name") != null 
        ? ((String)contactInfo.get("last_name")).trim() : null);
    try {
      bean.set("patient_dob", DateUtil.parseDate((String) contactInfo.get("patient_dob")));
    } catch (ParseException exc) {
      // TODO Auto-generated catch block
      logger.debug("Failed to parse the date");
    }
    if (contactInfo.get("patient_age") != null 
        && !contactInfo.get("patient_age").equals("")) {
      bean.set("patient_age", Integer.parseInt((String) contactInfo.get("patient_age")));
      bean.set("patient_age_units", contactInfo.get("patient_age_units"));
    } else {
      bean.set("patient_age", null);
      bean.set("patient_age_units", null);
    }
    bean.set("vip_status", contactInfo.get("vip_status"));
    bean.set("patient_email_id", contactInfo.get("patient_email_id"));
    bean.set("send_sms", contactInfo.get("send_sms"));
    bean.set("send_email", contactInfo.get("send_email"));
    bean.set("preferred_language", contactInfo.get("preferred_language"));
    bean.set("contact_id", contactInfo.get("contact_id"));
    String practoApptId = (String) contactInfo.get("practo_appointment_id");
    if (practoApptId != null && !practoApptId.equalsIgnoreCase("")) {
      bean.set("mod_user", "InstaAdmin");
    } else {
      bean.set("mod_user", (String) sessionService.getSessionAttributes().get("userId"));
    }
    bean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("contact_id", contactInfo.get("contact_id"));
    handlePhoneNumber((Integer) contactInfo.get("center_id"), bean);
    
    // duplicate contact check
    Integer existingId = contactDetailsRepository.getContactIdIfContactExists(bean.getMap());
    if (existingId != null && existingId != (int)contactInfo.get("contact_id")) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("contact match", "exception.patient.exists");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("contact", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    
    // duplicate MR patient check
    if (patientDetailsRepository.checkIfPatientExists(bean.getMap())) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("contact match", "exception.patient.exists");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("contact", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    boolean success = false;
    success = contactDetailsRepository.update(bean, keys) > 0;
    
    // update appointments
    Map tempMap =  new HashMap();
    tempMap.put("salutation_id",(String)bean.get("salutation_name"));
    String salu = "";
    if (bean.get("salutation_name") != null) {
      BasicDynaBean saluBean = salutationService.findByPk(tempMap);
      if (saluBean != null) {
        salu = (String) saluBean.get("salutation");
      }
    }
    appointmentRepository.updateAppsOnContactEdit(
        salu 
            + " " 
            + (String)bean.get("patient_name") 
            + " " 
            + (String)bean.get("last_name"),
        (String) bean.get("patient_contact"),
        (String)bean.get("patient_contact_country_code"),
        (String)bean.get("vip_status"),
        (Integer)contactInfo.get("contact_id"));
    if (success) {
      return bean.getMap();
    } else {
      return null;
    }
  }

  /**
   * Gets the contact details.
   *
   * @param contactId
   *          the contact id
   * @return the contact details
   */
  public Map getContactDetails(Integer contactId) {
    Map<String, Object> map = new HashMap<>();
    map.put("contact_id", contactId);
    BasicDynaBean bean = contactDetailsRepository.findByKey(map);
    return bean != null ? bean.getMap() : new HashMap();
  }
  
  /**
   * Handle phone number.
   *
   * @param centerId
   *          the center id
   * @param bean
   *          the bean
   */
  public void handlePhoneNumber(Integer centerId, BasicDynaBean bean) {
    String defaultCode = null;
    String patientContact = (String) bean.get("patient_contact");
    if (patientContact != null && !patientContact.isEmpty()) {
      try {
        defaultCode = centerService.getCountryCode(centerId);
      } catch (NullPointerException exp) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("appointment", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
      if (defaultCode == null) {
        defaultCode = centerService.getCountryCode(0);
      }
      List<String> parts = PhoneNumberUtil.getCountryCodeAndNationalPart(patientContact, null);
      if (parts != null && !parts.isEmpty() && !parts.get(0).isEmpty()) {
        bean.set("patient_contact_country_code", "+" + parts.get(0));
      } else if (defaultCode != null) {
        bean.set("patient_contact_country_code", "+" + defaultCode);
        if (!patientContact.startsWith("+")) {
          bean.set("patient_contact", "+" + defaultCode + patientContact);
        }
      }
      if (((String) bean.get("patient_contact")).length() > 16) {
        ValidationErrorMap errorMap = new ValidationErrorMap();
        errorMap.addError("patient_contact", "exception.scheduler.patient.invalid.phoneno.long");
        ValidationException ex = new ValidationException(errorMap);
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put("patient", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
  }

}
