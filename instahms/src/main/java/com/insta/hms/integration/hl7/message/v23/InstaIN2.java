package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.segment.IN2;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class InstaIN2.
 * 
 * @author yashwant
 */
@Component
public class InstaIN2 {

  /**
   * Creates the IN 2.
   *
   * @param in2
   *          the in 2
   * @param bean
   *          the bean
   * @param patientMap
   *          the patient map
   * @throws DataTypeException
   *           the data type exception
   */
  public void createIN2(IN2 in2, BasicDynaBean bean, Map<String, Object> patientMap)
      throws DataTypeException {
    in2.getIn225_PayorID().getCx1_ID().setValue((String) bean.get("member_id"));
    in2.getIn261_PatientMemberNumber().getCx1_ID().setValue((String) bean.get("member_id"));
    in2.getIn263_InsuredSTelephoneNumberHome(0).getPhoneNumber()
        .setValue((String) patientMap.get("patient_phone"));
  }
}
