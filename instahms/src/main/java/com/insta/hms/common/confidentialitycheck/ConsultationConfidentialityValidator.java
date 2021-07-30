package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "consultid", "consultationid" },
    urlEntityName = { "consultation", "triage", "initialassessment", "followupconsultation" })
public class ConsultationConfidentialityValidator implements ConfidentialityInterface {

  @LazyAutowired
  DoctorConsultationService docConsultService;

  private static Logger logger = LoggerFactory
      .getLogger(ConsultationConfidentialityValidator.class);

  @Override
  public List<String> getAssociatedMrNo(List<String> consultationIds) {
    List<String> mrnos = new ArrayList<String>();
    for (String consultationId : consultationIds) {
      try {
        Integer consId = Integer.parseInt(consultationId);
        mrnos.add(docConsultService.getMrNoForConsultationId(consId));
      } catch (NumberFormatException exception) {
        logger.error("Invalid consultation id provided");
        return null;
      }
    }
    return mrnos;
  }
  
  @Override
  public Boolean isValidParameter(String parameter) {
    return docConsultService.isConsultationIdValid(parameter);
  }

}
