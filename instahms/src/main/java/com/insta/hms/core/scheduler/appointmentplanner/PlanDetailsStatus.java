package com.insta.hms.core.scheduler.appointmentplanner;

public enum PlanDetailsStatus {
  DELETED("D"), NEW("N"), PROGRESS("P");

  private String status;

  PlanDetailsStatus(String status) {
    this.status = status;
  }

  public String planDetailsStatus() {
    return status;
  }
}
