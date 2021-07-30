package com.insta.hms.core.clinical.order.doctoritems;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DoctorOrderItemValidator {

  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  /**
   * validate PreAuth Requirements.
   * @param doctorsOrderParams the doctorsOrderParams
   * @param validationErrors the validationErrors
   * @return boolean
   */
  public boolean validatePreAuthRequirements(Object[] doctorsOrderParams,
      ValidationErrorMap validationErrors) {
    boolean isValid = true;
    for (int i = 0; i < doctorsOrderParams.length; i++) {
      Map<String, Object> doctorPrescMap = (Map<String, Object>) doctorsOrderParams[i];
      Integer docPrescId = MapUtils.getInteger(doctorPrescMap, "doctors_doc_presc_id");
      String priorAuthId = MapUtils.getString(doctorPrescMap, "doctors_prior_auth_id");
      Integer priorAuthModeId = MapUtils.getInteger(doctorPrescMap, "doctors_prior_auth_mode_id");
      if ((StringUtils.isBlank(priorAuthId) || priorAuthModeId == null)
          && preAuthItemsService.isDoctorPrescribedForPreAuth(docPrescId)) {
        String testName = (String) doctorPrescMap.get("doctors_act_description");
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
