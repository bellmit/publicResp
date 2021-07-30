package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.inventory.patientindent.PatientIndentService;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ConfidentialityValidator(queryParamNames = { "patientindentno",
    "excludeinqbpatientindentno" }, urlEntityName = { "indent" })
public class PatientIndentConfidentialityValidator extends VisitConfidentialityValidator
    implements ConfidentialityInterface {

  private static Logger logger = LoggerFactory
      .getLogger(PatientIndentConfidentialityValidator.class);

  @LazyAutowired
  private PatientIndentService indentService;

  @Override
  public List<String> getAssociatedMrNo(List<String> indentNos) {
    logger.debug(
        "Checking confidentiality for patient indent nos :" + ArrayUtils.toString(indentNos));
    List<String> visitIds = new ArrayList<String>();
    for (String indent : indentNos) {
      visitIds.add(indentService.getVisitId(indent));
    }

    return super.getAssociatedMrNo(visitIds);
  }

  @Override
  public Boolean isValidParameter(String parameter) {
    return indentService.isPatientIdentIdValid(parameter);
  }
}
