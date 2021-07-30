package com.bob.hms.otmasters.opemaster;

/**
 * The Class OperationCharges.
 */
public class OperationCharges {

  /** The operation id. */
  // this class represents a operation_charges
  private String operationId;
  
  /** The org id. */
  private String orgId;
  
  /** The bed type. */
  private String bedType;
  
  /** The surgeon charge. */
  private Double surgeonCharge;
  
  /** The anesthetist charge. */
  private Double anesthetistCharge;
  
  /** The surgica asst charge. */
  private Double surgicaAsstCharge;

  /** The org item code. */
  private String orgItemCode;
  
  /** The applicable. */
  private boolean applicable;


  /**
   * Gets the applicable.
   *
   * @return the applicable
   */
  public boolean getApplicable() {
    return applicable;
  }

  /**
   * Sets the applicable.
   *
   * @param applicable the new applicable
   */
  public void setApplicable(boolean applicable) {
    this.applicable = applicable;
  }

  /**
   * Gets the org item code.
   *
   * @return the org item code
   */
  public String getOrgItemCode() {
    return orgItemCode;
  }

  /**
   * Sets the org item code.
   *
   * @param orgItemCode the new org item code
   */
  public void setOrgItemCode(String orgItemCode) {
    this.orgItemCode = orgItemCode;
  }

  /**
   * Gets the anesthetist charge.
   *
   * @return the anesthetist charge
   */
  public Double getAnesthetistCharge() {
    return anesthetistCharge;
  }

  /**
   * Sets the anesthetist charge.
   *
   * @param anesthetistCharge the new anesthetist charge
   */
  public void setAnesthetistCharge(Double anesthetistCharge) {
    this.anesthetistCharge = anesthetistCharge;
  }

  /**
   * Gets the bed type.
   *
   * @return the bed type
   */
  public String getBedType() {
    return bedType;
  }

  /**
   * Sets the bed type.
   *
   * @param bedType the new bed type
   */
  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  /**
   * Gets the operation id.
   *
   * @return the operation id
   */
  public String getOperationId() {
    return operationId;
  }

  /**
   * Sets the operation id.
   *
   * @param operationId the new operation id
   */
  public void setOperationId(String operationId) {
    this.operationId = operationId;
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
   * Gets the surgeon charge.
   *
   * @return the surgeon charge
   */
  public Double getSurgeonCharge() {
    return surgeonCharge;
  }

  /**
   * Sets the surgeon charge.
   *
   * @param surgeonCharge the new surgeon charge
   */
  public void setSurgeonCharge(Double surgeonCharge) {
    this.surgeonCharge = surgeonCharge;
  }

  /**
   * Gets the surgica asst charge.
   *
   * @return the surgica asst charge
   */
  public Double getSurgicaAsstCharge() {
    return surgicaAsstCharge;
  }

  /**
   * Sets the surgica asst charge.
   *
   * @param surgicaAsstCharge the new surgica asst charge
   */
  public void setSurgicaAsstCharge(Double surgicaAsstCharge) {
    this.surgicaAsstCharge = surgicaAsstCharge;
  }

}
