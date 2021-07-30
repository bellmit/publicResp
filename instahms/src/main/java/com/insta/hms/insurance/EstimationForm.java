/**
 *
 */
package com.insta.hms.insurance;

import org.apache.struts.action.ActionForm;

/**
 * The Class EstimationForm.
 *
 * @author lakshmi.p
 */
public class EstimationForm extends ActionForm {

  /** The mr no. */
  private String mrNo;

  /** The patient id. */
  private String patientId;

  /** The bill no. */
  private String billNo;

  /** The insurance ID. */
  private String insuranceID;

  /** The module id. */
  private String moduleId;

  /** The bill status. */
  private String billStatus;

  /** The header flag. */
  private boolean headerFlag;

  /** The charge id. */
  private String[] chargeId;

  /** The description. */
  private String[] description;

  /** The remarks. */
  private String[] remarks;

  /** The rate. */
  private String[] rate;

  /** The qty. */
  private String[] qty;

  /** The disc. */
  private String[] disc;

  /** The amt. */
  private String[] amt;

  /** The amt paid. */
  private String[] amtPaid;

  /** The units. */
  private String[] units;

  /** The header. */
  private String[] header;

  /** The charge head id. */
  private String[] chargeHeadId;

  /** The charge group id. */
  private String[] chargeGroupId;

  /** The department id. */
  private String[] departmentId;

  /** The charge ref. */
  private String[] chargeRef;

  /** The del charge. */
  private boolean[] delCharge;

  /** The tot amt. */
  private String totAmt;

  /** The organization id. */
  private String organizationId;

  /** The bed type. */
  private String bedType;

  /**
   * Gets the amt.
   *
   * @return the amt
   */
  public String[] getAmt() {
    return amt;
  }

  /**
   * Sets the amt.
   *
   * @param amt the amt to set
   */
  public void setAmt(String[] amt) {
    this.amt = amt;
  }

  /**
   * Gets the amt paid.
   *
   * @return the amtPaid
   */
  public String[] getAmtPaid() {
    return amtPaid;
  }

  /**
   * Sets the amt paid.
   *
   * @param amtPaid the amtPaid to set
   */
  public void setAmtPaid(String[] amtPaid) {
    this.amtPaid = amtPaid;
  }

  /**
   * Gets the bill no.
   *
   * @return the billNo
   */
  public String getBillNo() {
    return billNo;
  }

  /**
   * Sets the bill no.
   *
   * @param billNo the billNo to set
   */
  public void setBillNo(String billNo) {
    this.billNo = billNo;
  }

  /**
   * Gets the bill status.
   *
   * @return the billStatus
   */
  public String getBillStatus() {
    return billStatus;
  }

  /**
   * Sets the bill status.
   *
   * @param billStatus the billStatus to set
   */
  public void setBillStatus(String billStatus) {
    this.billStatus = billStatus;
  }

  /**
   * Gets the charge group id.
   *
   * @return the chargeGroupId
   */
  public String[] getChargeGroupId() {
    return chargeGroupId;
  }

  /**
   * Sets the charge group id.
   *
   * @param chargeGroupId the chargeGroupId to set
   */
  public void setChargeGroupId(String[] chargeGroupId) {
    this.chargeGroupId = chargeGroupId;
  }

  /**
   * Gets the charge head id.
   *
   * @return the chargeHeadId
   */
  public String[] getChargeHeadId() {
    return chargeHeadId;
  }

  /**
   * Sets the charge head id.
   *
   * @param chargeHeadId the chargeHeadId to set
   */
  public void setChargeHeadId(String[] chargeHeadId) {
    this.chargeHeadId = chargeHeadId;
  }

  /**
   * Gets the charge id.
   *
   * @return the chargeId
   */
  public String[] getChargeId() {
    return chargeId;
  }

  /**
   * Sets the charge id.
   *
   * @param chargeId the chargeId to set
   */
  public void setChargeId(String[] chargeId) {
    this.chargeId = chargeId;
  }

  /**
   * Gets the charge ref.
   *
   * @return the chargeRef
   */
  public String[] getChargeRef() {
    return chargeRef;
  }

  /**
   * Sets the charge ref.
   *
   * @param chargeRef the chargeRef to set
   */
  public void setChargeRef(String[] chargeRef) {
    this.chargeRef = chargeRef;
  }

  /**
   * Gets the del charge.
   *
   * @return the delCharge
   */
  public boolean[] getDelCharge() {
    return delCharge;
  }

  /**
   * Sets the del charge.
   *
   * @param delCharge the delCharge to set
   */
  public void setDelCharge(boolean[] delCharge) {
    this.delCharge = delCharge;
  }

  /**
   * Gets the department id.
   *
   * @return the departmentId
   */
  public String[] getDepartmentId() {
    return departmentId;
  }

  /**
   * Sets the department id.
   *
   * @param departmentId the departmentId to set
   */
  public void setDepartmentId(String[] departmentId) {
    this.departmentId = departmentId;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String[] getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the description to set
   */
  public void setDescription(String[] description) {
    this.description = description;
  }

  /**
   * Gets the disc.
   *
   * @return the disc
   */
  public String[] getDisc() {
    return disc;
  }

  /**
   * Sets the disc.
   *
   * @param disc the disc to set
   */
  public void setDisc(String[] disc) {
    this.disc = disc;
  }

  /**
   * Gets the header.
   *
   * @return the header
   */
  public String[] getHeader() {
    return header;
  }

  /**
   * Sets the header.
   *
   * @param header the header to set
   */
  public void setHeader(String[] header) {
    this.header = header;
  }

  /**
   * Gets the insurance ID.
   *
   * @return the insuranceID
   */
  public String getInsuranceID() {
    return insuranceID;
  }

  /**
   * Sets the insurance ID.
   *
   * @param insuranceID the insuranceID to set
   */
  public void setInsuranceID(String insuranceID) {
    this.insuranceID = insuranceID;
  }

  /**
   * Gets the module id.
   *
   * @return the moduleId
   */
  public String getModuleId() {
    return moduleId;
  }

  /**
   * Sets the module id.
   *
   * @param moduleId the moduleId to set
   */
  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }

  /**
   * Gets the mr no.
   *
   * @return the mrNo
   */
  public String getMrNo() {
    return mrNo;
  }

  /**
   * Sets the mr no.
   *
   * @param mrNo the mrNo to set
   */
  public void setMrNo(String mrNo) {
    this.mrNo = mrNo;
  }

  /**
   * Gets the patient id.
   *
   * @return the patientId
   */
  public String getPatientId() {
    return patientId;
  }

  /**
   * Sets the patient id.
   *
   * @param patientId the patientId to set
   */
  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  /**
   * Gets the qty.
   *
   * @return the qty
   */
  public String[] getQty() {
    return qty;
  }

  /**
   * Sets the qty.
   *
   * @param qty the qty to set
   */
  public void setQty(String[] qty) {
    this.qty = qty;
  }

  /**
   * Gets the rate.
   *
   * @return the rate
   */
  public String[] getRate() {
    return rate;
  }

  /**
   * Sets the rate.
   *
   * @param rate the rate to set
   */
  public void setRate(String[] rate) {
    this.rate = rate;
  }

  /**
   * Gets the remarks.
   *
   * @return the remarks
   */
  public String[] getRemarks() {
    return remarks;
  }

  /**
   * Sets the remarks.
   *
   * @param remarks the remarks to set
   */
  public void setRemarks(String[] remarks) {
    this.remarks = remarks;
  }

  /**
   * Gets the units.
   *
   * @return the units
   */
  public String[] getUnits() {
    return units;
  }

  /**
   * Sets the units.
   *
   * @param units the units to set
   */
  public void setUnits(String[] units) {
    this.units = units;
  }

  /**
   * Gets the tot amt.
   *
   * @return the totAmt
   */
  public String getTotAmt() {
    return totAmt;
  }

  /**
   * Sets the tot amt.
   *
   * @param totAmt the totAmt to set
   */
  public void setTotAmt(String totAmt) {
    this.totAmt = totAmt;
  }

  /**
   * Gets the bed type.
   *
   * @return the bedType
   */
  public String getBedType() {
    return bedType;
  }

  /**
   * Sets the bed type.
   *
   * @param bedType the bedType to set
   */
  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  /**
   * Gets the organization id.
   *
   * @return the organizationId
   */
  public String getOrganizationId() {
    return organizationId;
  }

  /**
   * Sets the organization id.
   *
   * @param organizationId the organizationId to set
   */
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   * Gets the header flag.
   *
   * @return the headerFlag
   */
  public boolean getHeaderFlag() {
    return headerFlag;
  }

  /**
   * Sets the header flag.
   *
   * @param headerFlag the headerFlag to set
   */
  public void setHeaderFlag(boolean headerFlag) {
    this.headerFlag = headerFlag;
  }

}
