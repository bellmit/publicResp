package com.bob.hms.otmasters.opemaster;

/**
 * The Class Operation.
 */
public class Operation {

  // this class represents the operation_master

  /** The operation id. */
  private String operationId;
  
  /** The operation name. */
  private String operationName;
  
  /** The dept id. */
  private String deptId;
  
  /** The operation code. */
  private String operationCode;
  
  /** The status. */
  private String status;

  /**
   * Gets the dept id.
   *
   * @return the dept id
   */
  public String getDeptId() {
    return deptId;
  }

  /**
   * Sets the dept id.
   *
   * @param deptId the new dept id
   */
  public void setDeptId(String deptId) {
    this.deptId = deptId;
  }

  /**
   * Gets the operation code.
   *
   * @return the operation code
   */
  public String getOperationCode() {
    return operationCode;
  }

  /**
   * Sets the operation code.
   *
   * @param operationCode the new operation code
   */
  public void setOperationCode(String operationCode) {
    this.operationCode = operationCode;
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
   * Gets the operation name.
   *
   * @return the operation name
   */
  public String getOperationName() {
    return operationName;
  }

  /**
   * Sets the operation name.
   *
   * @param operationName the new operation name
   */
  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param status the new status
   */
  public void setStatus(String status) {
    this.status = status;
  }

}
