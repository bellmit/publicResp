package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.datatype.XAD;
import ca.uhn.hl7v2.model.v23.datatype.XPN;
import ca.uhn.hl7v2.model.v23.segment.IN1;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class InstaIN1.
 * 
 * @author yashwant
 */
@Component
public class InstaIN1 {

  /**
   * Creates the IN 1.
   *
   * @param in1
   *          the in 1
   * @param bean
   *          the bean
   * @throws DataTypeException
   *           the data type exception
   */
  public void createIN1(IN1 in1, BasicDynaBean bean, Map<String, Object> patientMap)
      throws DataTypeException {
    String externalPlanId = "";
    if (bean.get("plan_interface_code") != null
        && !((String) bean.get("plan_interface_code")).isEmpty()) {
      externalPlanId = (String) bean.get("plan_interface_code");
    } else {
      Integer planId = (Integer) bean.get("plan_id");
      if (planId != null) {
        externalPlanId = Integer.toString(planId);
      }
    }

    String externalInsuraceId = "";
    if (bean.get("ins_co_interface_code") != null
        && !((String) bean.get("ins_co_interface_code")).isEmpty()) {
      externalInsuraceId = (String) bean.get("ins_co_interface_code");
    } else {
      externalInsuraceId = (String) bean.get("insurance_co_id");
    }
    in1.getInsurancePlanID().getCe1_Identifier().setValue(externalPlanId);
    in1.getIn13_InsuranceCompanyID().getCx1_ID().setValue(externalInsuraceId);
    in1.getIn14_InsuranceCompanyName().getXon1_OrganizationName()
        .setValue((String) bean.get("insurance_co_name"));

    XAD insuranceXad = in1.getIn15_InsuranceCompanyAddress();
    insuranceXad.getXad1_StreetAddress().setValue((String) bean.get("insurance_co_address"));
    insuranceXad.getXad3_City().setValue((String) bean.get("insurance_co_city"));
    insuranceXad.getXad4_StateOrProvince().setValue((String) bean.get("insurance_co_state"));
    insuranceXad.getXad6_Country().setValue((String) bean.get("insurance_co_country"));

    in1.getIn17_InsuranceCoPhoneNumber(0).getPhoneNumber()
        .setValue((String) bean.get("insurance_co_phone"));

    in1.getIn112_PlanEffectiveDate().setValue((String) bean.get("policy_validity_start"));
    in1.getIn113_PlanExpirationDate().setValue((String) bean.get("policy_validity_end"));

    XPN xpn = in1.getIn116_NameOfInsured();
    PersonNameXPN.prepareName(xpn, patientMap);

    in1.getIn118_InsuredSDateOfBirth().getTs1_TimeOfAnEvent()
        .setValue((String) patientMap.get("expected_dob"));

    // String[] address = ((String) patientMap.get("patient_address")).split("\r{0,1}\n");
    // if (address != null && address.length >= 1) {
    // in1.getIn119_InsuredSAddress().getXad1_StreetAddress().setValue(address[0]);
    // }
    // if (address != null && address.length >= 2) {
    // in1.getIn119_InsuredSAddress().getXad1_StreetAddress().setValue(address[1]);
    // }
    XAD xad = in1.getIn119_InsuredSAddress();
    AddressXAD.prepareAddress(xad, patientMap);

    in1.getIn143_InsuredSSex().setValue((String) patientMap.get("patient_gender"));
  }

}
