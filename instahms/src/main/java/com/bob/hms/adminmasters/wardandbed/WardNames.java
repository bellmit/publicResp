package com.bob.hms.adminmasters.wardandbed;

public class WardNames {

  private String wardNo;
  private String wardName;
  private String status;
  private String description;
  private int linenStore;
  private int centerId;
  private String allowedGender;

  public int getCenter_id() {
    return centerId;
  }

  public void setCenter_id(int centerId) {
    this.centerId = centerId;
  }

  public int getLinenStore() {
    return linenStore;
  }

  public void setLinenStore(int linenStore) {
    this.linenStore = linenStore;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getWardName() {
    return wardName;
  }

  public void setWardName(String wardName) {
    this.wardName = wardName;
  }

  public String getWardNo() {
    return wardNo;
  }

  public void setWardNo(String wardNo) {
    this.wardNo = wardNo;
  }

  public String getAllowedGender() {
    return allowedGender;
  }

  public void setAllowedGender(String allowedGender) {
    this.allowedGender = allowedGender;
  }
}
