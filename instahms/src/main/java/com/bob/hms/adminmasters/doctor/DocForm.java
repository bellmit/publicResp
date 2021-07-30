package com.bob.hms.adminmasters.doctor;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class DocForm.
 */
public class DocForm extends ActionForm {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The doct id. */
  private String doctId;
  
  /** The doctorname. */
  private String doctorname;
  
  /** The deptid. */
  private String deptid;
  
  /** The doctortype. */
  private String doctortype;
  
  /** The specialization. */
  private String specialization;
  
  /** The email. */
  private String email;
  
  /** The phonenumber. */
  private String phonenumber;
  
  /** The validity. */
  private int validity;
  
  /** The allowed revisit count. */
  private int allowed_revisit_count;
  
  /** The active status. */
  private String activeStatus;
  
  /** The is OT doctor. */
  private String isOTDoctor;
  
  /** The doctor address. */
  private String doctorAddress;
  
  /** The dept name. */
  private String deptName;
  
  /** The doctor OP charge. */
  private Double doctorOPCharge;
  
  /** The OP revist charge. */
  private Double OPRevistCharge;
  
  /** The org name. */
  private String orgName;
  
  /** The doctor filter. */
  private String doctorFilter;
  
  /** The org id. */
  private String orgId;
  
  /** The doctor id. */
  private String doctorId;
  
  /** The bedtype. */
  private String bedtype[];
  
  /** The ip charge. */
  private Double ipCharge[];
  
  /** The night charge. */
  private Double nightCharge[];
  
  /** The ot charge. */
  private Double otCharge[];
  
  /** The ass OT charge. */
  private Double assOTCharge[];
  
  /** The co op surgeon charge. */
  private Double co_OpSurgeonCharge[];

  /** The doctor priv OP charge. */
  private Double doctorPrivOPCharge;
  
  /** The priv OP revist charge. */
  private Double privOPRevistCharge;
  
  /** The pay cat id. */
  private String payCatId;
  
  /** The pay eligible. */
  private String payEligible;

  /** The qualification. */
  private String qualification;
  
  /** The reg number. */
  private String regNumber;
  
  /** The clinic phone. */
  private String clinicPhone;
  
  /** The res phone. */
  private String resPhone;
  
  /** The license number. */
  private String licenseNumber;


  /** The all doctors. */
  // following are for filter
  private String allDoctors;
  
  /** The active doctors. */
  private String activeDoctors;
  
  /** The inactive doctors. */
  private String inactiveDoctors;
  
  /** The charge type. */
  private String charge_type;
  
  /** The dept filter. */
  private String deptFilter[];


  /** The group beds. */
  // following varibles are for group update
  private String groupBeds[];
  
  /** The variance type. */
  private String varianceType;// incr Or Decr
  
  /** The variance by. */
  private Double varianceBy;// percentage
  
  /** The variance value. */
  private Double varianceValue;
  
  /** The groupdoctors. */
  private String groupdoctors[];
  
  /** The group updat component. */
  private String groupUpdatComponent;



  /** The payment type. */
  // following for payments
  private String paymentType;
  
  /** The doc pay for OP. */
  private Double docPayForOP;
  
  /** The doc pay for IP. */
  private Double docPayForIP;
  
  /** The doc pay for operation. */
  private Double docPayForOperation;


  /** The page num. */
  private String pageNum;

  /** The schedulable. */
  private boolean schedulable;


  /*
   * Whenever added one new private varible, pls make sure that it is there in reset method
   */


  /**
   * Gets the page num.
   *
   * @return the page num
   */
  public String getPageNum() {
    return pageNum;
  }

  /**
   * Sets the page num.
   *
   * @param pageNum the new page num
   */
  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * Gets the payment type.
   *
   * @return the payment type
   */
  public String getPaymentType() {
    return paymentType;
  }

  /**
   * Sets the payment type.
   *
   * @param paymentType the new payment type
   */
  public void setPaymentType(String paymentType) {
    this.paymentType = paymentType;
  }

  /**
   * Gets the groupdoctors.
   *
   * @return the groupdoctors
   */
  public String[] getGroupdoctors() {
    return Arrays.copyOf(groupdoctors, groupdoctors.length);
  }

  /**
   * Sets the groupdoctors.
   *
   * @param groupdoctors the new groupdoctors
   */
  public void setGroupdoctors(String[] groupdoctors) {
    this.groupdoctors = Arrays.copyOf(groupdoctors, groupdoctors.length);
  }

  /**
   * Gets the group updat component.
   *
   * @return the group updat component
   */
  public String getGroupUpdatComponent() {
    return groupUpdatComponent;
  }

  /**
   * Sets the group updat component.
   *
   * @param groupUpdatComponent the new group updat component
   */
  public void setGroupUpdatComponent(String groupUpdatComponent) {
    this.groupUpdatComponent = groupUpdatComponent;
  }

  /**
   * Gets the group beds.
   *
   * @return the group beds
   */
  public String[] getGroupBeds() {
    return Arrays.copyOf(groupBeds, groupBeds.length);
  }

  /**
   * Sets the group beds.
   *
   * @param groupBeds the new group beds
   */
  public void setGroupBeds(String[] groupBeds) {
    this.groupBeds = groupBeds;
  }

  /**
   * Gets the variance by.
   *
   * @return the variance by
   */
  public Double getVarianceBy() {
    return varianceBy;
  }

  /**
   * Sets the variance by.
   *
   * @param varianceBy the new variance by
   */
  public void setVarianceBy(Double varianceBy) {
    this.varianceBy = varianceBy;
  }

  /**
   * Gets the org id.
   *
   * @return the org id
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId the new org id
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the org name.
   *
   * @return the org name
   */
  public String getOrgName() {
    return orgName;
  }

  /**
   * Sets the org name.
   *
   * @param orgName the new org name
   */
  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  /**
   * Gets the OP revist charge.
   *
   * @return the OP revist charge
   */
  public Double getOPRevistCharge() {
    return OPRevistCharge;
  }

  /**
   * Sets the OP revist charge.
   *
   * @param revistCharge the new OP revist charge
   */
  public void setOPRevistCharge(Double revistCharge) {
    OPRevistCharge = revistCharge;
  }

  /**
   * Gets the dept name.
   *
   * @return the dept name
   */
  public String getDeptName() {
    return deptName;
  }

  /**
   * Sets the dept name.
   *
   * @param deptName the new dept name
   */
  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

  /**
   * Gets the doctor address.
   *
   * @return the doctor address
   */
  public String getDoctorAddress() {
    return doctorAddress;
  }

  /**
   * Sets the doctor address.
   *
   * @param doctorAddress the new doctor address
   */
  public void setDoctorAddress(String doctorAddress) {
    this.doctorAddress = doctorAddress;
  }

  /**
   * Gets the active status.
   *
   * @return the active status
   */
  public String getActiveStatus() {
    return activeStatus;
  }

  /**
   * Sets the active status.
   *
   * @param activeStatus the new active status
   */
  public void setActiveStatus(String activeStatus) {
    this.activeStatus = activeStatus;
  }

  /**
   * Gets the deptid.
   *
   * @return the deptid
   */
  public String getDeptid() {
    return deptid;
  }

  /**
   * Sets the deptid.
   *
   * @param deptid the new deptid
   */
  public void setDeptid(String deptid) {
    this.deptid = deptid;
  }

  /**
   * Gets the doctorname.
   *
   * @return the doctorname
   */
  public String getDoctorname() {
    return doctorname;
  }

  /**
   * Sets the doctorname.
   *
   * @param doctorname the new doctorname
   */
  public void setDoctorname(String doctorname) {
    this.doctorname = doctorname;
  }

  /**
   * Gets the doctortype.
   *
   * @return the doctortype
   */
  public String getDoctortype() {
    return doctortype;
  }

  /**
   * Sets the doctortype.
   *
   * @param doctortype the new doctortype
   */
  public void setDoctortype(String doctortype) {
    this.doctortype = doctortype;
  }

  /**
   * Gets the email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email.
   *
   * @param email the new email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Gets the phonenumber.
   *
   * @return the phonenumber
   */
  public String getPhonenumber() {
    return phonenumber;
  }

  /**
   * Sets the phonenumber.
   *
   * @param phonenumber the new phonenumber
   */
  public void setPhonenumber(String phonenumber) {
    this.phonenumber = phonenumber;
  }

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
   * Gets the validity.
   *
   * @return the validity
   */
  public int getValidity() {
    return validity;
  }

  /**
   * Sets the validity.
   *
   * @param validity the new validity
   */
  public void setValidity(int validity) {
    this.validity = validity;
  }

  /**
   * Gets the checks if is OT doctor.
   *
   * @return the checks if is OT doctor
   */
  public String getIsOTDoctor() {
    return isOTDoctor;
  }

  /**
   * Sets the checks if is OT doctor.
   *
   * @param isOTDoctor the new checks if is OT doctor
   */
  public void setIsOTDoctor(String isOTDoctor) {
    this.isOTDoctor = isOTDoctor;

  }

  /* (non-Javadoc)
   * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {
    super.reset(arg0, arg1);
    this.doctorname = null;
    this.deptid = null;
    this.doctortype = null;
    this.specialization = null;
    this.email = null;
    this.phonenumber = null;
    this.activeStatus = null;
    this.isOTDoctor = null;
    this.doctorAddress = null;
    this.orgName = null;
    this.orgId = null;
    this.doctorOPCharge = null;
    this.OPRevistCharge = null;
    this.deptName = null;
    this.assOTCharge = null;
    this.bedtype = null;
    this.co_OpSurgeonCharge = null;
    this.ipCharge = null;
    this.nightCharge = null;
    this.otCharge = null;
    this.doctId = null;
    this.allDoctors = null;
    this.activeDoctors = null;
    this.inactiveDoctors = null;
    this.charge_type = null;
    this.deptFilter = null;
    this.groupBeds = null;
    this.varianceType = null;
    this.varianceBy = null;
    this.varianceValue = null;
    this.groupdoctors = null;
    this.paymentType = null;
    this.docPayForOP = null;
    this.docPayForIP = null;
    this.docPayForOperation = null;
  }

  /**
   * Gets the doctor OP charge.
   *
   * @return the doctor OP charge
   */
  public Double getDoctorOPCharge() {
    return doctorOPCharge;
  }

  /**
   * Sets the doctor OP charge.
   *
   * @param doctorOPCharge the new doctor OP charge
   */
  public void setDoctorOPCharge(Double doctorOPCharge) {
    this.doctorOPCharge = doctorOPCharge;
  }

  /**
   * Gets the ass OT charge.
   *
   * @return the ass OT charge
   */
  public Double[] getAssOTCharge() {
    return Arrays.copyOf(assOTCharge, assOTCharge.length);
  }

  /**
   * Sets the ass OT charge.
   *
   * @param assOTCharge the new ass OT charge
   */
  public void setAssOTCharge(Double[] assOTCharge) {
    this.assOTCharge = Arrays.copyOf(assOTCharge, assOTCharge.length);
  }

  /**
   * Gets the bedtype.
   *
   * @return the bedtype
   */
  public String[] getBedtype() {
    return Arrays.copyOf(bedtype, bedtype.length);
  }

  /**
   * Sets the bedtype.
   *
   * @param bedtype the new bedtype
   */
  public void setBedtype(String[] bedtype) {
    this.bedtype = Arrays.copyOf(bedtype, bedtype.length);
  }

  /**
   * Gets the co op surgeon charge.
   *
   * @return the co op surgeon charge
   */
  public Double[] getCo_OpSurgeonCharge() {
    return Arrays.copyOf(co_OpSurgeonCharge, co_OpSurgeonCharge.length);
  }

  /**
   * Sets the co op surgeon charge.
   *
   * @param co_OpSurgeonCharge the new co op surgeon charge
   */
  public void setCo_OpSurgeonCharge(Double[] co_OpSurgeonCharge) {
    this.co_OpSurgeonCharge = Arrays.copyOf(co_OpSurgeonCharge, co_OpSurgeonCharge.length);
  }

  /**
   * Gets the ip charge.
   *
   * @return the ip charge
   */
  public Double[] getIpCharge() {
    return Arrays.copyOf(ipCharge, ipCharge.length);
  }

  /**
   * Sets the ip charge.
   *
   * @param ipCharge the new ip charge
   */
  public void setIpCharge(Double[] ipCharge) {
    this.ipCharge = Arrays.copyOf(ipCharge, ipCharge.length);
  }

  /**
   * Gets the night charge.
   *
   * @return the night charge
   */
  public Double[] getNightCharge() {
    return Arrays.copyOf(nightCharge, nightCharge.length);
  }

  /**
   * Sets the night charge.
   *
   * @param nightCharge the new night charge
   */
  public void setNightCharge(Double[] nightCharge) {
    this.nightCharge = Arrays.copyOf(nightCharge, nightCharge.length);
  }

  /**
   * Gets the doct id.
   *
   * @return the doct id
   */
  public String getDoctId() {
    return doctId;
  }

  /**
   * Sets the doct id.
   *
   * @param doctId the new doct id
   */
  public void setDoctId(String doctId) {
    this.doctId = doctId;
  }

  /**
   * Gets the ot charge.
   *
   * @return the ot charge
   */
  public Double[] getOtCharge() {
    return Arrays.copyOf(otCharge, otCharge.length);
  }

  /**
   * Sets the ot charge.
   *
   * @param otCharge the new ot charge
   */
  public void setOtCharge(Double[] otCharge) {
    this.otCharge = Arrays.copyOf(otCharge, otCharge.length);
  }

  /**
   * Gets the dept filter.
   *
   * @return the dept filter
   */
  public String[] getDeptFilter() {
    return Arrays.copyOf(deptFilter, deptFilter.length);
  }

  /**
   * Sets the dept filter.
   *
   * @param deptFilter the new dept filter
   */
  public void setDeptFilter(String[] deptFilter) {
    this.deptFilter = Arrays.copyOf(deptFilter, deptFilter.length);
  }

  /**
   * Gets the active doctors.
   *
   * @return the active doctors
   */
  public String getActiveDoctors() {
    return activeDoctors;
  }

  /**
   * Sets the active doctors.
   *
   * @param activeDoctors the new active doctors
   */
  public void setActiveDoctors(String activeDoctors) {
    this.activeDoctors = activeDoctors;
  }

  /**
   * Gets the all doctors.
   *
   * @return the all doctors
   */
  public String getAllDoctors() {
    return allDoctors;
  }

  /**
   * Sets the all doctors.
   *
   * @param allDoctors the new all doctors
   */
  public void setAllDoctors(String allDoctors) {
    this.allDoctors = allDoctors;
  }

  /**
   * Gets the inactive doctors.
   *
   * @return the inactive doctors
   */
  public String getInactiveDoctors() {
    return inactiveDoctors;
  }

  /**
   * Sets the inactive doctors.
   *
   * @param inactiveDoctors the new inactive doctors
   */
  public void setInactiveDoctors(String inactiveDoctors) {
    this.inactiveDoctors = inactiveDoctors;
  }

  /* (non-Javadoc)
   * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public ActionErrors validate(ActionMapping am, HttpServletRequest req) {



    return super.validate(am, req);
  }

  /**
   * Gets the variance type.
   *
   * @return the variance type
   */
  public String getVarianceType() {
    return varianceType;
  }

  /**
   * Sets the variance type.
   *
   * @param varianceType the new variance type
   */
  public void setVarianceType(String varianceType) {
    this.varianceType = varianceType;
  }

  /**
   * Gets the variance value.
   *
   * @return the variance value
   */
  public Double getVarianceValue() {
    return varianceValue;
  }

  /**
   * Sets the variance value.
   *
   * @param varianceValue the new variance value
   */
  public void setVarianceValue(Double varianceValue) {
    this.varianceValue = varianceValue;
  }

  /**
   * Gets the doc pay for IP.
   *
   * @return the doc pay for IP
   */
  public Double getDocPayForIP() {
    return docPayForIP;
  }

  /**
   * Sets the doc pay for IP.
   *
   * @param docPayForIP the new doc pay for IP
   */
  public void setDocPayForIP(Double docPayForIP) {
    this.docPayForIP = docPayForIP;
  }

  /**
   * Gets the doc pay for OP.
   *
   * @return the doc pay for OP
   */
  public Double getDocPayForOP() {
    return docPayForOP;
  }

  /**
   * Sets the doc pay for OP.
   *
   * @param docPayForOP the new doc pay for OP
   */
  public void setDocPayForOP(Double docPayForOP) {
    this.docPayForOP = docPayForOP;
  }

  /**
   * Gets the doc pay for operation.
   *
   * @return the doc pay for operation
   */
  public Double getDocPayForOperation() {
    return docPayForOperation;
  }

  /**
   * Sets the doc pay for operation.
   *
   * @param docPayForOperation the new doc pay for operation
   */
  public void setDocPayForOperation(Double docPayForOperation) {
    this.docPayForOperation = docPayForOperation;
  }

  /**
   * Gets the doctor filter.
   *
   * @return the doctor filter
   */
  public String getDoctorFilter() {
    return doctorFilter;
  }

  /**
   * Sets the doctor filter.
   *
   * @param doctorFilter the new doctor filter
   */
  public void setDoctorFilter(String doctorFilter) {
    this.doctorFilter = doctorFilter;
  }

  /**
   * Gets the doctor id.
   *
   * @return the doctor id
   */
  public String getDoctorId() {
    return doctorId;
  }

  /**
   * Sets the doctor id.
   *
   * @param doctorId the new doctor id
   */
  public void setDoctorId(String doctorId) {
    this.doctorId = doctorId;
  }

  /**
   * Gets the doctor priv OP charge.
   *
   * @return the doctor priv OP charge
   */
  public Double getDoctorPrivOPCharge() {
    return doctorPrivOPCharge;
  }

  /**
   * Sets the doctor priv OP charge.
   *
   * @param doctorPrivOPCharge the new doctor priv OP charge
   */
  public void setDoctorPrivOPCharge(Double doctorPrivOPCharge) {
    this.doctorPrivOPCharge = doctorPrivOPCharge;
  }

  /**
   * Gets the priv OP revist charge.
   *
   * @return the priv OP revist charge
   */
  public Double getPrivOPRevistCharge() {
    return privOPRevistCharge;
  }

  /**
   * Sets the priv OP revist charge.
   *
   * @param privOPRevistCharge the new priv OP revist charge
   */
  public void setPrivOPRevistCharge(Double privOPRevistCharge) {
    this.privOPRevistCharge = privOPRevistCharge;
  }

  /**
   * Checks if is schedulable.
   *
   * @return true, if is schedulable
   */
  public boolean isSchedulable() {
    return schedulable;
  }

  /**
   * Sets the schedulable.
   *
   * @param schedulable the new schedulable
   */
  public void setSchedulable(boolean schedulable) {
    this.schedulable = schedulable;
  }

  /**
   * Gets the qualification.
   *
   * @return the qualification
   */
  public String getQualification() {
    return qualification;
  }

  /**
   * Sets the qualification.
   *
   * @param qualification the new qualification
   */
  public void setQualification(String qualification) {
    this.qualification = qualification;
  }

  /**
   * Gets the reg number.
   *
   * @return the reg number
   */
  public String getRegNumber() {
    return regNumber;
  }

  /**
   * Sets the reg number.
   *
   * @param regNumber the new reg number
   */
  public void setRegNumber(String regNumber) {
    this.regNumber = regNumber;
  }

  /**
   * Gets the clinic phone.
   *
   * @return the clinic phone
   */
  public String getClinicPhone() {
    return clinicPhone;
  }

  /**
   * Sets the clinic phone.
   *
   * @param clinicPhone the new clinic phone
   */
  public void setClinicPhone(String clinicPhone) {
    this.clinicPhone = clinicPhone;
  }

  /**
   * Gets the res phone.
   *
   * @return the res phone
   */
  public String getResPhone() {
    return resPhone;
  }

  /**
   * Sets the res phone.
   *
   * @param resPhone the new res phone
   */
  public void setResPhone(String resPhone) {
    this.resPhone = resPhone;
  }

  /**
   * Gets the pay eligible.
   *
   * @return the pay eligible
   */
  public String getPayEligible() {
    return payEligible;
  }

  /**
   * Sets the pay eligible.
   *
   * @param payEligible the new pay eligible
   */
  public void setPayEligible(String payEligible) {
    this.payEligible = payEligible;
  }

  /**
   * Gets the pay cat id.
   *
   * @return the pay cat id
   */
  public String getPayCatId() {
    return payCatId;
  }

  /**
   * Sets the pay cat id.
   *
   * @param payCatId the new pay cat id
   */
  public void setPayCatId(String payCatId) {
    this.payCatId = payCatId;
  }

  /**
   * Gets the license number.
   *
   * @return the license number
   */
  public String getLicenseNumber() {
    return licenseNumber;
  }

  /**
   * Sets the license number.
   *
   * @param licenseNumber the new license number
   */
  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  /**
   * Gets the allowed revisit count.
   *
   * @return the allowed revisit count
   */
  public int getAllowed_revisit_count() {
    return allowed_revisit_count;
  }

  /**
   * Sets the allowed revisit count.
   *
   * @param allowed_revisit_count the new allowed revisit count
   */
  public void setAllowed_revisit_count(int allowed_revisit_count) {
    this.allowed_revisit_count = allowed_revisit_count;
  }

  /**
   * Gets the charge type.
   *
   * @return the charge type
   */
  public String getCharge_type() {
    return charge_type;
  }

  /**
   * Sets the charge type.
   *
   * @param charge_type the new charge type
   */
  public void setCharge_type(String charge_type) {
    this.charge_type = charge_type;
  }

}
