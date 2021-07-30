package com.insta.hms.insurance;

import java.math.BigDecimal;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Estimate.
 *
 * @author lakshmi.p
 */
public class Estimate {

  /** The estimate ID. */
  private String estimateID;

  /** The total amt. */
  private BigDecimal totalAmt = new BigDecimal(0);

  /** The bed type. */
  private String bedType;

  /** The org id. */
  private String orgId;

  /** The user. */
  private String user;

  /** The insurance id. */
  private String insuranceId;

  /** The bill no. */
  private String billNo;

  /** The module id. */
  private String moduleId;

  /** The header flag. */
  private boolean headerFlag;

  /** The update estimation charge list. */
  private List updateEstimationChargeList;

  /** The insert estimation charge list. */
  private List insertEstimationChargeList;

  /** The delete estimation charge L ist. */
  private List deleteEstimationChargeLIst;

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
   * Gets the delete estimation charge L ist.
   *
   * @return the deleteEstimationChargeLIst
   */
  public List getDeleteEstimationChargeLIst() {
    return deleteEstimationChargeLIst;
  }

  /**
   * Sets the delete estimation charge L ist.
   *
   * @param deleteEstimationChargeLIst the deleteEstimationChargeLIst to set
   */
  public void setDeleteEstimationChargeLIst(List deleteEstimationChargeLIst) {
    this.deleteEstimationChargeLIst = deleteEstimationChargeLIst;
  }

  /**
   * Gets the estimate ID.
   *
   * @return the estimateID
   */
  public String getEstimateID() {
    return estimateID;
  }

  /**
   * Sets the estimate ID.
   *
   * @param estimateID the estimateID to set
   */
  public void setEstimateID(String estimateID) {
    this.estimateID = estimateID;
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

  /**
   * Gets the insert estimation charge list.
   *
   * @return the insertEstimationChargeList
   */
  public List getInsertEstimationChargeList() {
    return insertEstimationChargeList;
  }

  /**
   * Sets the insert estimation charge list.
   *
   * @param insertEstimationChargeList the insertEstimationChargeList to set
   */
  public void setInsertEstimationChargeList(List insertEstimationChargeList) {
    this.insertEstimationChargeList = insertEstimationChargeList;
  }

  /**
   * Gets the insurance id.
   *
   * @return the insuranceId
   */
  public String getInsuranceId() {
    return insuranceId;
  }

  /**
   * Sets the insurance id.
   *
   * @param insuranceId the insuranceId to set
   */
  public void setInsuranceId(String insuranceId) {
    this.insuranceId = insuranceId;
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
   * Gets the org id.
   *
   * @return the orgId
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId the orgId to set
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the total amt.
   *
   * @return the totalAmt
   */
  public BigDecimal getTotalAmt() {
    return totalAmt;
  }

  /**
   * Sets the total amt.
   *
   * @param totalAmt the totalAmt to set
   */
  public void setTotalAmt(BigDecimal totalAmt) {
    this.totalAmt = totalAmt;
  }

  /**
   * Gets the update estimation charge list.
   *
   * @return the updateEstimationChargeList
   */
  public List getUpdateEstimationChargeList() {
    return updateEstimationChargeList;
  }

  /**
   * Sets the update estimation charge list.
   *
   * @param updateEstimationChargeList the updateEstimationChargeList to set
   */
  public void setUpdateEstimationChargeList(List updateEstimationChargeList) {
    this.updateEstimationChargeList = updateEstimationChargeList;
  }

  /**
   * Gets the user.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets the user.
   *
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

}
