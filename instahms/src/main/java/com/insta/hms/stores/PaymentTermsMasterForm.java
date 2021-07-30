package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class PaymentTermsMasterForm extends ActionForm {

  private String templateName;
  private String statusAll;
  private String statusActive;
  private String statusInActive;
  private String status;
  private String tempCode;
  private String operation;
  private String tremsAndConditions;
  private String tempName;
  public String getTempName() {
    return tempName;
  }
  public void setTempName(String tempName) {
    this.tempName = tempName;
  }
  public String getTremsAndConditions() {
    return tremsAndConditions;
  }
  public void setTremsAndConditions(String tremsAndConditions) {
    this.tremsAndConditions = tremsAndConditions;
  }
  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getStatusActive() {
    return statusActive;
  }
  public void setStatusActive(String statusActive) {
    this.statusActive = statusActive;
  }
  public String getStatusAll() {
    return statusAll;
  }
  public void setStatusAll(String statusAll) {
    this.statusAll = statusAll;
  }
  public String getStatusInActive() {
    return statusInActive;
  }
  public void setStatusInActive(String statusInActive) {
    this.statusInActive = statusInActive;
  }
  public String getTempCode() {
    return tempCode;
  }
  public void setTempCode(String tempCode) {
    this.tempCode = tempCode;
  }
  public String getTemplateName() {
    return templateName;
  }
  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

}
