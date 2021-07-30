package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

public class GenericMasterForm extends ActionForm {

  private String genName;
  private String statusAll;
  private String statusActive;
  private String statusInActive;
  private String gmaster_name;
  private String status;
  private String genCode;
  private String operation;

  public String getGenCode() {
    return genCode;
  }
  public void setGenCode(String genCode) {
    this.genCode = genCode;
  }
  public String getGenName() {
    return genName;
  }
  public void setGenName(String genName) {
    this.genName = genName;
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
  public String getGmaster_name() {
    return gmaster_name;
  }
  public void setGmaster_name(String gmaster_name) {
    this.gmaster_name = gmaster_name;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }
}
