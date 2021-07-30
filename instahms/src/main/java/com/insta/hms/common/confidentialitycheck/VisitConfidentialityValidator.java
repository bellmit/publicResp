package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.mdm.storeretailcustomers.StoreRetailCustomerService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "patientid", "visitid", "patid" }, 
    urlEntityName = { "visit", "ipemr" })
public class VisitConfidentialityValidator implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory.getLogger(VisitConfidentialityValidator.class);

  @LazyAutowired
  PatientDetailsService patientDetailService;
  
  @LazyAutowired
  PatientRegistrationService patientRegistrationService;
  
  @LazyAutowired
  IncomingSampleRegistrationService isrService;

  @LazyAutowired
  StoreRetailCustomerService srcService;

  @Override
  public List<String> getAssociatedMrNo(List<String> visitIds) {
    logger.debug("Checking validity for visitid:" + ArrayUtils.toString(visitIds));
    List<String> mrNo = patientDetailService.getAssociatedMrNoForVisitId(visitIds);
    if (mrNo == null && !isrService.listVisit(visitIds).isEmpty()) {
      mrNo = Arrays.asList("ISR");
    }
    if (mrNo == null && !srcService.getRetailCustomerDetails(visitIds).isEmpty()) {
      mrNo = Arrays.asList("retail");
    }
    return mrNo;
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return patientRegistrationService.isVisitIdValid(parameter);
  }
}
