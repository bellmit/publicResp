package com.insta.hms.core.clinical.order.serviceitems;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ServiceOrderItemValidator.
 */
@Component
public class ServiceOrderItemValidator {

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /**
   * Validate conducting doctor.
   *
   * @param servicesOrderParams the services order params
   * @param validationErrors    the validation errors
   * @return the boolean
   */
  @SuppressWarnings("unchecked")
  public Boolean validateConductingDoctor(Object[] servicesOrderParams,
      ValidationErrorMap validationErrors) {
    Boolean status = false;
    for (int i = 0; i < servicesOrderParams.length; i++) {
      String conductingDoctorMandatory = null;
      String payeeDoctorId = null;

      if (((Map<String, Object>) servicesOrderParams[i])
          .get("services_conducting_doc_mandatory") != null) {
        conductingDoctorMandatory = String.valueOf(((Map<String, Object>) servicesOrderParams[i])
            .get("services_conducting_doc_mandatory"));
      }
      if (((Map<String, Object>) servicesOrderParams[i]).get("services_payee_doctor_id") != null) {
        payeeDoctorId = (String) ((Map<String, Object>) servicesOrderParams[i])
            .get("services_payee_doctor_id");
      }

      if (conductingDoctorMandatory != null
          && (conductingDoctorMandatory.equals("O") || conductingDoctorMandatory.equals("true"))
          && payeeDoctorId == null) {
        String testName = (String) ((Map<String, Object>) servicesOrderParams[i])
            .get("services_act_description");
        List<String> messageParams = new ArrayList<String>();
        messageParams.add(testName);
        validationErrors.addError(Integer.toString(i),
            "exception.order.conducting.doctor.is.required", messageParams);
        if (!status) {
          status = true;
        }
      }
    }
    return status;
  }

  /**
   * Validate pre auth requirements.
   *
   * @param servicesOrderParams the services order params
   * @param validationErrors    the validation errors
   * @return true, if successful
   */
  public boolean validatePreAuthRequirements(Object[] servicesOrderParams,
      ValidationErrorMap validationErrors) {
    boolean isValid = true;
    for (int i = 0; i < servicesOrderParams.length; i++) {
      Map<String, Object> serviceOrderMap = (Map<String, Object>) servicesOrderParams[i];
      Integer docPrescId = MapUtils.getInteger(serviceOrderMap, "services_doc_presc_id");
      String priorAuthId = MapUtils.getString(serviceOrderMap, "services_prior_auth_id");
      Integer priorAuthModeId = (Integer) MapUtils.getInteger(serviceOrderMap,
          "services_prior_auth_mode_id");
      if ((StringUtils.isBlank(priorAuthId) || priorAuthModeId == null)
          && preAuthItemsService.isDoctorPrescribedForPreAuth(docPrescId)) {
        String testName = (String) serviceOrderMap.get("services_act_description");
        List<String> messageParams = new ArrayList<String>();
        messageParams.add(testName);
        validationErrors.addError(Integer.toString(i), "exception.new.order.preauth.required",
            messageParams);
        isValid = false;
      }
    }
    return isValid;

  }

}
