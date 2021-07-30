package com.insta.hms.core.inventory.patientindent;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class StorePatientIndentDetailsRepository extends GenericRepository {

  public StorePatientIndentDetailsRepository() {
    super("store_patient_indent_details");
  }

  public Integer closeAllIndents(String patientIndentNo) {
    BasicDynaBean storePatientIndentDetailsBean = getBean();
    storePatientIndentDetailsBean.set("dispense_status", "C");
    Map<String, Object> params = new HashMap<>();
    params.put("patient_indent_no", patientIndentNo);
    return update(storePatientIndentDetailsBean, params);
  }



}
