package com.insta.hms.core.clinical.discharge;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.referraldoctors.ReferralDoctorService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DischargeService {

  @LazyAutowired
  private DoctorService doctorService;

  @LazyAutowired
  private ReferralDoctorService referalDoctorService;

  @LazyAutowired
  private PatientDischargeRepository patDischargeRepo;

  /**
   * Gets the physical discharge tokens.
   *
   * @param patientDetailsMap the patient details map
   * @return the physical discharge tokens
   */
  public Map<String, Object> getPhysicalDischargeTokens(Map<String, Object> patientDetailsMap) {

    HashMap<String, Object> physicalDischargeData = new HashMap<String, Object>();
    String doctor = (String) patientDetailsMap.get("discharge_doctor_id");
    Map<String, Object> doctorDetails = doctorService.getDoctorDetails(doctor);

    String referalDoctor = (String) patientDetailsMap.get("referral_doctor_id");
    if (referalDoctor != null) {
      BasicDynaBean referralDetails = referalDoctorService.findByKey(referalDoctor);
      if (referralDetails != null) {
        physicalDischargeData.put("referral_mobile",
            (String) referralDetails.get("referal_doctor_phone"));
        physicalDischargeData.put("referral_email",
            (String) referralDetails.get("referal_doctor_email"));
      } else {
        Map<String, Object> referalDoctorDetails = doctorService.getDoctorDetails(doctor);
        physicalDischargeData.put("referral_mobile",
            (String) referalDoctorDetails.get("doctor_mobile"));
        physicalDischargeData.put("referral_email",
            (String) referalDoctorDetails.get("doctor_mail_id"));
      }
    }
    String doctorMobile = (String) doctorDetails.get("doctor_mobile");
    String doctorMail = (String) doctorDetails.get("doctor_mail_id");
    String referralMobile = (String) physicalDischargeData.get("referral_mobile");
    String referralMail = (String) physicalDischargeData.get("referral_email");

    if (doctorMobile == null) {
      if (referralMobile != null) {
        doctorMobile = referralMobile;
      }
    } else {
      if (referralMobile != null) {
        doctorMobile = doctorMobile + "," + referralMobile;
      }
    }
    if (doctorMail == null) {
      if (referralMail != null) {
        doctorMail = referralMail;
      }
    } else {
      if (referralMail != null) {
        doctorMail = doctorMail + "," + referralMail;
      }
    }
    physicalDischargeData.putAll(patientDetailsMap);
    physicalDischargeData.put("doctor_name", (String) doctorDetails.get("doctor_name"));
    physicalDischargeData.put("doctor_mobile", doctorMobile);
    physicalDischargeData.put("doctor_mail", doctorMail);
    physicalDischargeData.put("discharge_status", "P");

    return physicalDischargeData;
  }

  public boolean checkIfPatientDischargeEntryExists(String patientId) {
    BasicDynaBean dischargeBean = patDischargeRepo.findByKey("patient_id", patientId);
    return dischargeBean != null && !dischargeBean.getMap().isEmpty();
  }

  /**
   * Insert or update financial discharge details.
   *
   * @param patientId the patient id
   * @param financialDischargeStatus the financial discharge status
   * @param financialDischargeUser the financial discharge user
   * @param isEntryExists the is entry exists
   * @return true, if successful
   */
  public boolean insertOrUpdateFinancialDischargeDetails(String patientId,
      Boolean financialDischargeStatus, String financialDischargeUser, Boolean isEntryExists) {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", patientId);
    BasicDynaBean financialDischargeBean = patDischargeRepo.getBean();
    financialDischargeBean.set("patient_id", patientId);
    financialDischargeBean.set("financial_discharge_status", financialDischargeStatus);
    financialDischargeBean.set("financial_discharge_date",
        financialDischargeStatus ? DateUtil.getCurrentDate() : null);
    financialDischargeBean.set("financial_discharge_time",
        financialDischargeStatus ? DateUtil.getCurrentTime() : null);
    financialDischargeBean.set("financial_entered_by", financialDischargeUser);
    if (isEntryExists) {
      return patDischargeRepo.update(financialDischargeBean, keys) > 0;
    } else {
      return patDischargeRepo.insert(financialDischargeBean) > 0;
    }
  }

}
