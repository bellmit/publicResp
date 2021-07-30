package com.insta.hms.mdm.formheader;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

/**
 * @author anup vishwas.
 *
 */

@Service
public class FormHeaderService {

  @LazyAutowired
  private FormHeaderRepository formHeaderRepo;

  public BasicDynaBean getFormHeaderDetail(int docId, String patientId) {

    return formHeaderRepo.getFormHeaderDetail(docId, patientId);
  }
}
