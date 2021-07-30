package com.insta.hms.core.clinical.outpatient;

import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class DoctorFormValidator.
 *
 * @author sonam
 */
@Component
public class DoctorFormValidator {

  /**
   * Validate consultation id paramaeter.
   *
   * @param consultId the consult id
   * @return true, if successful
   */
  public boolean validateConsultationIdParamaeter(String consultId) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (consultId == null) {
      errMap.addError("consultation_id", "exception.consultation.id.notnull");
    } else {
      if (!ValidationUtils.isKeyValid("doctor_consultation", consultId, "consultation_id")) {
        errMap.addError("consultation_id", "exception.consultation.id.notvalid");
      }
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }

  /**
   * Validate consultation status.
   *
   * @param status the status
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateConsultationStatus(String status, ValidationErrorMap errorMap) {
    boolean valid = true;
    if (status.equals("C")) {
      errorMap.addError("consultation_status", "exception.consultation.status.isclosed");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate consultation details.
   *
   * @param newConsBean the new cons bean
   * @param oldBean the oldbean
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateConsultationDetails(BasicDynaBean newConsBean, BasicDynaBean oldBean,
      ValidationErrorMap errorMap) {
    boolean valid = true;
    if (newConsBean.get("start_datetime") == null || newConsBean.get("start_datetime").equals("")) {
      errorMap.addError("start_datetime", "exception.start.time.notnull");
      valid = false;
    } else {
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startDate = dateFormat.format((Timestamp) newConsBean.get("start_datetime"));
        String prescDate = dateFormat.format((Timestamp) oldBean.get("presc_date"));
        Date prescDateTime = dateFormat.parse(prescDate);
        Date startDateTime = dateFormat.parse(startDate);

        if (oldBean.get("presc_date") != null && prescDateTime.after(startDateTime)) {
          errorMap.addError("start_datetime", "exception.start.time.notvalid");
          valid = false;
        }
        if (oldBean.get("cancel_status") != null && oldBean.get("cancel_status").equals("C")) {
          errorMap.addError("consultation_cancelled",
              "ui.label.consultation.order.has.been.cancelled");
          valid = false;
        }
        
      } catch (java.text.ParseException exp) {
        // TODO Auto-generated catch block
      }
    }

    valid = valid && validateEndDateTime(newConsBean, errorMap);

    return valid;
  }

  /**
   * Validate end date time.
   *
   * @param newConsBean the new cons bean
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateEndDateTime(BasicDynaBean newConsBean, ValidationErrorMap errorMap) {
    boolean valid = true;
    if (newConsBean.get("end_datetime") != null && !newConsBean.get("end_datetime").equals("")
        && newConsBean.get("start_datetime") != null) {

      if (((Timestamp) newConsBean.get("start_datetime"))
          .after((Timestamp) newConsBean.get("end_datetime"))) {
        errorMap.addError("end_datetime", "exception.end.time.notvalid");
        valid = false;
      }

    }

    return valid;
  }

  /**
   * Validate triage end date time.
   *
   * @param newConsBean the new cons bean
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateTriageEndDateTime(BasicDynaBean newConsBean, ValidationErrorMap errorMap) {
    boolean valid = true;
    if (newConsBean.get("triage_end_datetime") != null
        && !newConsBean.get("triage_end_datetime").equals("")
        && newConsBean.get("triage_start_datetime") != null) {

      if (((Timestamp) newConsBean.get("triage_start_datetime"))
          .after((Timestamp) newConsBean.get("triage_end_datetime"))) {
        errorMap.addError("triage_end_datetime", "exception.triage.end.time.notvalid");
        valid = false;
      }
    }
    return valid;
  }

  /**
   * Validate initial assessment end date time.
   *
   * @param newConsBean the new cons bean
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateInitialAssessmentEndDateTime(BasicDynaBean newConsBean,
      ValidationErrorMap errorMap) {
    boolean valid = true;
    if (newConsBean.get("ia_end_datetime") != null && !newConsBean.get("ia_end_datetime").equals("")
        && newConsBean.get("ia_start_datetime") != null) {

      if (((Timestamp) newConsBean.get("ia_start_datetime"))
          .after((Timestamp) newConsBean.get("ia_end_datetime"))) {
        errorMap.addError("ia_end_datetime", "exception.initailassessment.end.time.notvalid");
        valid = false;
      }

    }

    return valid;
  }

  /**
   * Validate op type.
   *
   * @param oldOpType the old op type
   * @param newOpType the new op type
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateOpType(String oldOpType, String newOpType, ValidationErrorMap errorMap) {

    boolean valid = true;
    if (!oldOpType.equals("F")) {
      errorMap.addError("visit_type", "exception.consultation.previous.visit.type.notvalid");
      valid = false;
    }
    if (!newOpType.equals("M")) {
      errorMap.addError("visit_type", "exception.consultation.visit.type.notvalid");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate const type id.
   *
   * @param consTypeId the cons type id
   * @param errorMap the error map
   * @return true, if successful
   */
  public boolean validateConstTypeId(String consTypeId, ValidationErrorMap errorMap) {
    boolean valid = true;
    if (consTypeId.isEmpty()) {
      errorMap.addError("consultation_type_id", "exception.consultation.type.id.isnull");
      valid = false;
    } else if (!ValidationUtils.isKeyValid("consultation_types", Integer.parseInt(consTypeId),
        "consultation_type_id")) {
      errorMap.addError("consultation_type_id ", "exception.consultation.type.id.notvalid");
      valid = false;
    }
    return valid;
  }

  /**
   * Validate status.
   *
   * @param status the status
   * @return true, if successful
   */
  public boolean validateStatus(String status) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (!status.equals("C")) {
      errMap.addError("consultation_status", "exception.consultation.status.isnotclosed");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }

  /**
   * Validate triage status.
   *
   * @param status the status
   * @return true, if successful
   */
  public boolean validateTriageStatus(String status) {
    if (!status.equals("Y")) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("triage_status", "exception.triage.status.isnotclosed");
      throw new ValidationException(errMap);
    }
    return true;
  }

  /**
   * Validate initial assessment status.
   *
   * @param status the status
   * @return true, if successful
   */
  public boolean validateInitialAssessmentStatus(String status) {
    if (!status.equals("Y")) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("initial_assessment_status", "exception.triage.status.isnotclosed");
      throw new ValidationException(errMap);
    }
    return true;
  }

}
