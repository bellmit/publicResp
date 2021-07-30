package com.insta.hms.core.inventory.patientindent;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class StorePatientIndentService {

  @LazyAutowired
  StorePatientIndentRepository storePatientIndentRepository;

  public List<BasicDynaBean> getIndentsForProcess(String visitId, String indentType,
      Integer indentStore, String patientIndentNo) {
    return storePatientIndentRepository.getIndentsForProcess(visitId, indentType, indentStore,
        patientIndentNo);

  }

  public List<BasicDynaBean> getIndentDetailsForProcessOfIndentStore(String visitId, String status,
      String indentType, Integer indentStore, String patientIndentNo) {
   return  storePatientIndentRepository.getIndentDetailsForProcessOfIndentStore(visitId, status, indentType, indentStore, patientIndentNo);
  }
}
