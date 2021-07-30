package com.bob.hms.adminmasters.wardandbed;

import org.apache.struts.action.ActionForm;

public class WardAndBedMasterForm extends ActionForm {

  private static final long serialVersionUID = 1L;
  private String wardStatus;
  private int[] noOfBedToAdd;
  private String wardId;
  private String[] bedType;
  private String description;
  private String wardName;
  private String[] bedStatus;
  private String[] bedName;
  private String[] bedId;
  private String bedTypeToUpdate;
  private int centerId;
  private String allowedGender;

  public String getBedTypeToUpdate() {
    return bedTypeToUpdate;
  }

  public void setBedTypeToUpdate(String bedTypeToUpdate) {
    this.bedTypeToUpdate = bedTypeToUpdate;
  }

  public String[] getBedStatus() {
    return bedStatus;
  }

  public void setBedStatus(String[] bedStatus) {
    this.bedStatus = bedStatus;
  }

  public String getWardName() {
    return wardName;
  }

  public void setWardName(String wardName) {
    this.wardName = wardName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getBedType() {
    return bedType;
  }

  public void setBedType(String[] bedType) {
    this.bedType = bedType;
  }

  public String getWardId() {
    return wardId;
  }

  public void setWardId(String wardId) {
    this.wardId = wardId;
  }

  public int[] getNoOfBedToAdd() {
    return noOfBedToAdd;
  }

  public void setNoOfBedToAdd(int[] noOfBedToAdd) {
    this.noOfBedToAdd = noOfBedToAdd;
  }

  public String getWardStatus() {
    return wardStatus;
  }

  public void setWardStatus(String wardStatus) {
    this.wardStatus = wardStatus;
  }

  public String[] getBedName() {
    return bedName;
  }

  public void setBedName(String[] bedName) {
    this.bedName = bedName;
  }

  public String[] getBedId() {
    return bedId;
  }

  public void setBedId(String[] bedId) {
    this.bedId = bedId;
  }

  public int getCenter_id() {
    return centerId;
  }

  public void setCenter_id(int centerId) {
    this.centerId = centerId;
  }

  public String getAllowedGender() {
    return allowedGender;
  }

  public void setAllowedGender(String allowedGender) {
    this.allowedGender = allowedGender;
  }
}
