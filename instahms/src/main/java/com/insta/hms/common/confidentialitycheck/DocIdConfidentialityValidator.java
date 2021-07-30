package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.documents.PatientDocumentService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Validator for doc_id parameter.
 * 
 * @author yashwant
 *
 */
@ConfidentialityValidator(queryParamNames = { "docid" }, urlEntityName = { "docid" })
public class DocIdConfidentialityValidator implements ConfidentialityInterface {

  @Autowired
  private PatientDocumentService patientDocumentService;

  @Override
  public List<String> getAssociatedMrNo(List<String> docId) {

    return patientDocumentService.getAssociatedMrNo(docId);
  }
  
  @Override
  public Boolean isValidParameter(String parameter) {
    return patientDocumentService.isDocIdValid(parameter);
  }

}
