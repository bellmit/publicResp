package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.forms.genericforms.GenericFormService;

import java.util.ArrayList;
import java.util.List;

@ConfidentialityValidator(queryParamNames = {"genericFormId", "genFormId"},
    urlEntityName = {"genericform"})
public class GenericFormConfidentialityValidator implements ConfidentialityInterface {

  @LazyAutowired
  private GenericFormService genFormSvc;

  @Override
  public List<String> getAssociatedMrNo(List<String> genFormIds) {
    List<String> associatedMrNos = new ArrayList<>();
    for (String genFormId : genFormIds) {
      associatedMrNos.add(genFormSvc.getGenFormAssociatedMrNo(Integer.parseInt(genFormId)));
    }
    return associatedMrNos;
  }

  @Override
  public Boolean isValidParameter(String genFormId) {
    return genFormSvc.isGenericFormIdValid(Integer.parseInt(genFormId));
  }

}
