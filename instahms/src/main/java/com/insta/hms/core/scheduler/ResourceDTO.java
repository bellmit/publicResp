package com.insta.hms.core.scheduler;

public class ResourceDTO {

  // Used for scheduler_appointment_items updations
  private String resourceDelete;
  private String resourceCheck;
  private String resourceId;
  private String resourceType;
  private int appointmentId;
  private int appointmentItemId;
  private String userName;
  private java.sql.Timestamp modTime;

  // Used for Scheduler_Item_Master updations
  private int resSchId;
  private String itemId;

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceCheck() {
    return resourceCheck;
  }

  public void setResourceCheck(String resourceCheck) {
    this.resourceCheck = resourceCheck;
  }

  public String getResourceDelete() {
    return resourceDelete;
  }

  public void setResourceDelete(String resourceDelete) {
    this.resourceDelete = resourceDelete;
  }

  public int getAppointment_item_id() {
    return appointmentItemId;
  }

  public void setAppointment_item_id(int appointmentItemId) {
    this.appointmentItemId = appointmentItemId;
  }

  public int getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(int appointmentId) {
    this.appointmentId = appointmentId;
  }

  public int getRes_sch_id() {
    return resSchId;
  }

  public void setRes_sch_id(int resSchId) {
    this.resSchId = resSchId;
  }

  public String getItem_id() {
    return itemId;
  }

  public void setItem_id(String itemId) {
    this.itemId = itemId;
  }

  public java.sql.Timestamp getMod_time() {
    return modTime;
  }

  public void setMod_time(java.sql.Timestamp modTime) {
    this.modTime = modTime;
  }

  public String getUser_name() {
    return userName;
  }

  public void setUser_name(String userName) {
    this.userName = userName;
  }
}
