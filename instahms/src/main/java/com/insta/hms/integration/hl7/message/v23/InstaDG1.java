package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.segment.DG1;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class InstaDG1.
 * 
 * @author yashwant
 */
@Component
public class InstaDG1 {

  /**
   * Creates the DG 1.
   *
   * @param diagSeg1
   *          the diag seg 1
   * @param bean
   *          the bean
   * @throws DataTypeException
   *           the data type exception
   */
  public void createDG1(DG1 diagSeg1, BasicDynaBean bean) throws DataTypeException {
    diagSeg1.getDg13_DiagnosisCode().getCe1_Identifier()
        .setValue((String) bean.get("diagnosis_code"));
    diagSeg1.getDg14_DiagnosisDescription().setValue((String) bean.get("diagnosis"));
  }

}
