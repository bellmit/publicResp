package com.insta.hms.integration.book;

import com.practo.integration.sdk.PatientDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class to Represent Patient Details for Practo.
 */
public class PatientDTO {
  
  /** The name. */
  private String name;
  
  /** The email id. */
  private String emailId;
  
  /** The phone no. */
  private String phoneNo;
  
  /** The mr no. */
  private String mrNo;

  /**
   * Instantiates a new patient DTO.
   *
   * @param name the name
   * @param emailId the email id
   * @param phoneNo the phone no
   * @param mrNo the mr no
   */
  public PatientDTO(String name, String emailId, String phoneNo, String mrNo) {
    this.name = name;
    this.emailId = emailId;
    this.phoneNo = phoneNo;
    this.mrNo = mrNo;
  }

  /**
   * Instantiates a new patient DTO.
   */
  public PatientDTO() {

  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the email id.
   *
   * @param emailId the new email id
   */
  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }

  /**
   * Sets the phone no.
   *
   * @param phoneNo the new phone no
   */
  public void setPhoneNo(String phoneNo) {
    this.phoneNo = phoneNo;
  }

  /**
   * Sets the mr no.
   *
   * @param mrNo the new mr no
   */
  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the email id.
   *
   * @return the email id
   */
  public String getEmailId() {
    return emailId;
  }

  /**
   * Gets the phone no.
   *
   * @return the phone no
   */
  public String getPhoneNo() {
    return phoneNo;
  }

  /**
   * Gets the mr no.
   *
   * @return the mr no
   */
  public String getMrNo() {
    return mrNo;
  }

  /**
   * Gets the patient details.
   *
   * @return the patient details
   */
  public PatientDetails getPatientDetails() {
    PatientDetails patientDetails = new PatientDetails();
    patientDetails.setUniqueHospitalId(getMrNo());
    List<String> mobileNumbers = new ArrayList<String>();
    mobileNumbers.add(getPhoneNo());
    patientDetails.setMobileNumbers(mobileNumbers);
    if (!BookSDKUtil.isNullOrEmpty(getName())) {
      String[] patientNameArr = getName().split("\\s+", 3);
      if (patientNameArr.length > 0) {
        patientDetails.setFirstName(patientNameArr[0]);
      }
      if (patientNameArr.length > 1) {
        patientDetails.setMiddleName(patientNameArr[1]);
      }
      if (patientNameArr.length > 2) {
        patientDetails.setLastName(patientNameArr[2]);
      }
    }
    List<String> emailIds = new ArrayList<String>();
    if (!BookSDKUtil.isNullOrEmpty(getEmailId())) {
      emailIds.add(getEmailId());
    }
    patientDetails.setEmailIds(emailIds);
    return patientDetails;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Patient [name=" + name + ", emailId=" + emailId + ", phoneNo=" + phoneNo + ", mrNo="
        + mrNo + "]";
  }

}