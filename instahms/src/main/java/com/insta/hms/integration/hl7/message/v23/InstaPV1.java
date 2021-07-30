package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.segment.PV1;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class InstaPV1.
 * 
 * @author yashwant
 */
@Component
public class InstaPV1 {

  /**
   * Creates the PV 1.
   *
   * @param pv1
   *          the pv 1
   * @param patientMap
   *          the patient dyna bean
   * @throws DataTypeException
   *           the data type exception
   */
  public void createPV1(PV1 pv1, Map<String, Object> patientMap, Map<String, String> configParams)
      throws DataTypeException {
    pv1.getPv11_SetIDPatientVisit().setValue("1");
    String patientClass = (String) patientMap.get("visit_type");
    if (patientClass != null) {
      patientClass = patientClass.toUpperCase();
    }
    pv1.getPatientClass().setValue(patientClass);
    // PV1-19
    pv1.getVisitNumber().getCx1_ID().setValue((String) patientMap.get("patient_id"));

    String isDoctorLicenceNumber = configParams.get("hl7_is_doctor_license_number");
    if (isDoctorLicenceNumber != null && isDoctorLicenceNumber.equalsIgnoreCase("Y")) {
      pv1.getAttendingDoctor(0).getIDNumber()
          .setValue((String) patientMap.get("doctor_license_number"));
      pv1.getAttendingDoctor(0).getGivenName().setValue((String) patientMap.get("doctor_name"));
    } else {
      pv1.getAttendingDoctor(0).getIDNumber().setValue((String) patientMap.get("doctor_id"));
      pv1.getAttendingDoctor(0).getGivenName().setValue((String) patientMap.get("doctor_name"));
    }
  }

}
