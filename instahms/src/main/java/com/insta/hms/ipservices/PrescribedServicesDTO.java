package com.insta.hms.ipservices;

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescribedServicesDTO.
 */
public class PrescribedServicesDTO {
  
  /** The service dept. */
  private String serviceDept = "";
  
  /** The service id. */
  private String serviceId;
  
  /** The service name. */
  private String serviceName;
  
  /** The mrno. */
  private String mrno;
  
  /** The patientid. */
  private String patientid;
  
  /** The doctor. */
  private String doctor;
  
  /** The noofdays. */
  private String noofdays;
  
  /** The serviceremark. */
  private String serviceremark;
  
  /** The hoperationid. */
  private String hoperationid;
  
  /** The presdate. */
  private Timestamp presdate;
  
  /** The user name. */
  private String userName;
  
  /** The item code. */
  private String itemCode;
  
  /** The servicecharge. */
  private int servicecharge;
  
  /** The chargeable. */
  private boolean chargeable;
  
  /** The activity id. */
  private int activityId;
  
  /** The service tax. */
  private int serviceTax;
  
  /** The conducted. */
  private String conducted;
  
  /** The specialization. */
  private String specialization;

  /**
   * Gets the specialization.
   *
   * @return the specialization
   */
  public String getSpecialization() {
    return specialization;
  }

  /**
   * Sets the specialization.
   *
   * @param specialization the new specialization
   */
  public void setSpecialization(String specialization) {
    this.specialization = specialization;
  }

  /**
   * Gets the conducted.
   *
   * @return the conducted
   */
  public String getConducted() {
    return conducted;
  }

  /**
   * Sets the conducted.
   *
   * @param conducted the new conducted
   */
  public void setConducted(String conducted) {
    this.conducted = conducted;
  }

  /**
   * Gets the service tax.
   *
   * @return the service tax
   */
  public int getServiceTax() {
    return serviceTax;
  }

  /**
   * Sets the service tax.
   *
   * @param serviceTax the new service tax
   */
  public void setServiceTax(int serviceTax) {
    this.serviceTax = serviceTax;
  }

  /**
   * Checks if is chargeable.
   *
   * @return true, if is chargeable
   */
  public boolean isChargeable() {
    return chargeable;
  }

  /**
   * Sets the chargeable.
   *
   * @param chargeable the new chargeable
   */
  public void setChargeable(boolean chargeable) {
    this.chargeable = chargeable;
  }

  /**
   * Gets the servicecharge.
   *
   * @return the servicecharge
   */
  public int getServicecharge() {
    return servicecharge;
  }

  /**
   * Sets the servicecharge.
   *
   * @param servicecharge the new servicecharge
   */
  public void setServicecharge(int servicecharge) {
    this.servicecharge = servicecharge;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param userName the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Gets the presdate.
   *
   * @return the presdate
   */
  public Timestamp getPresdate() {
    return presdate;
  }

  /**
   * Sets the presdate.
   *
   * @param presdate the new presdate
   */
  public void setPresdate(Timestamp presdate) {
    this.presdate = presdate;
  }

  /**
   * Gets the noofdays.
   *
   * @return the noofdays
   */
  public String getNoofdays() {
    return noofdays;
  }

  /**
   * Sets the noofdays.
   *
   * @param noofdays the new noofdays
   */
  public void setNoofdays(String noofdays) {
    this.noofdays = noofdays;
  }

  /**
   * Gets the doctor.
   *
   * @return the doctor
   */
  public String getDoctor() {
    return doctor;
  }

  /**
   * Sets the doctor.
   *
   * @param doctor the new doctor
   */
  public void setDoctor(String doctor) {
    this.doctor = doctor;
  }

  /**
   * Gets the mrno.
   *
   * @return the mrno
   */
  public String getMrno() {
    return mrno;
  }

  /**
   * Sets the mrno.
   *
   * @param mrno the new mrno
   */
  public void setMrno(String mrno) {
    this.mrno = mrno;
  }

  /**
   * Gets the patientid.
   *
   * @return the patientid
   */
  public String getPatientid() {
    return patientid;
  }

  /**
   * Sets the patientid.
   *
   * @param patientid the new patientid
   */
  public void setPatientid(String patientid) {
    this.patientid = patientid;
  }

  /**
   * Gets the service dept.
   *
   * @return the service dept
   */
  public String getServiceDept() {
    return serviceDept;
  }

  /**
   * Sets the service dept.
   *
   * @param serviceDept the new service dept
   */
  public void setServiceDept(String serviceDept) {
    this.serviceDept = serviceDept;
  }

  /**
   * Gets the service id.
   *
   * @return the service id
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Sets the service id.
   *
   * @param serviceId the new service id
   */
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  /**
   * Gets the service name.
   *
   * @return the service name
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Sets the service name.
   *
   * @param serviceName the new service name
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Gets the serviceremark.
   *
   * @return the serviceremark
   */
  public String getServiceremark() {
    return serviceremark;
  }

  /**
   * Sets the serviceremark.
   *
   * @param serviceremark the new serviceremark
   */
  public void setServiceremark(String serviceremark) {
    this.serviceremark = serviceremark;
  }

  /**
   * Gets the hoperationid.
   *
   * @return the hoperationid
   */
  public String getHoperationid() {
    return hoperationid;
  }

  /**
   * Sets the hoperationid.
   *
   * @param hoperationid the new hoperationid
   */
  public void setHoperationid(String hoperationid) {
    this.hoperationid = hoperationid;
  }

  /**
   * Gets the item code.
   *
   * @return the item code
   */
  public String getItemCode() {
    return itemCode;
  }

  /**
   * Sets the item code.
   *
   * @param itemCode the new item code
   */
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public int getActivity_id() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivity_id(int activityId) {
    this.activityId = activityId;
  }

}
