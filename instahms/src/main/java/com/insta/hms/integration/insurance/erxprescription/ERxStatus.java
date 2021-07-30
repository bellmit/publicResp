package com.insta.hms.integration.insurance.erxprescription;

public enum ERxStatus {
  SUCCESS("Success"),
  FAILED("Failed"),
  IN_PROGRESS("In Progress");

  private ERxStatus(String status) {
    this.status = status;
  }

  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
  
 
}
