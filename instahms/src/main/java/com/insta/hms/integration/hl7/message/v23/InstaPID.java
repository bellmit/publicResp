package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v23.datatype.XAD;
import ca.uhn.hl7v2.model.v23.datatype.XPN;
import ca.uhn.hl7v2.model.v23.segment.PID;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class InstaPID.
 * 
 * @author yashwant
 */
@Component
public class InstaPID {

  /**
   * Creates the PID.
   *
   * @param pid
   *          the pid
   * @param patientMap
   *          the patient dyna bean
   * @throws HL7Exception
   *           the HL 7 exception
   */
  public void createPID(PID pid, Map<String, Object> patientMap) throws HL7Exception {
    // 2.3 version
    // PID-1
    pid.getPid1_SetIDPatientID().setValue("1");
    // PID-3.1
    pid.insertPid3_PatientIDInternalID(0).getCx1_ID().setValue((String) patientMap.get("mr_no"));
    // PID-3.2
    pid.getPatientIDInternalID(0).getCodeIdentifyingTheCheckDigitSchemeEmployed()
        .setValue("InstaHMS");
    // PID-4
    pid.getPid4_AlternatePatientID().getAssigningAuthority().getHd2_UniversalID()
        .setValue((String) patientMap.get("government_identifier"));
    pid.getPid4_AlternatePatientID().getAssigningAuthority().getHd3_UniversalIDType()
        .setValue((String) patientMap.get("identifier_type"));
    // PID-6.1
    XPN xpn = pid.getPatientName(0);
    PersonNameXPN.prepareName(xpn, patientMap);
    // PID-7
    pid.getDateOfBirth().getTimeOfAnEvent().setValue((String) patientMap.get("expected_dob"));
    // PID-8
    pid.getSex().setValue((String) patientMap.get("patient_gender"));
    // PID-11
    XAD xad = pid.insertPatientAddress(0);
    AddressXAD.prepareAddress(xad, patientMap);
    // PID-13
    pid.insertPid13_PhoneNumberHome(0).getPhoneNumber()
        .setValue((String) patientMap.get("patient_phone"));
    pid.insertPid13_PhoneNumberHome(0).getEmailAddress()
        .setValue((String) patientMap.get("email_id"));
    pid.getPid28_NationalityCode().getCe1_Identifier()
        .setValue((String) patientMap.get("nationality_code"));
    pid.getPid28_NationalityCode().getCe2_Text()
        .setValue((String) patientMap.get("nationality_name"));
  }
}
