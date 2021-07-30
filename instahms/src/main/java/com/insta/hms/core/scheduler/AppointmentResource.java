package com.insta.hms.core.scheduler;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentResource.
 */
public class AppointmentResource implements Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1873430004847236417L;
  
  /** The appointment id. */
  private int appointmentId;
  
  /** The resource type. */
  private String resourceType;
  
  /** The resource id. */
  private String resourceId;
  
  /** The resource name. */
  private String resourceName;
  
  /** The appointment item id. */
  private int appointmentItemId;
  
  /** The user name. */
  private String userName;
  
  /** The mod time. */
  private java.sql.Timestamp modTime;

  /**
   * Instantiates a new appointment resource.
   *
   * @param appointmentId the appointment id
   * @param resourceType the resource type
   * @param resourceId the resource id
   */
  public AppointmentResource(int appointmentId, String resourceType, String resourceId) {
    this.appointmentId = appointmentId;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  /**
   * Gets the appointment id.
   *
   * @return the appointment id
   */
  public int getAppointmentId() {
    return appointmentId;
  }

  /**
   * Sets the appointment id.
   *
   * @param appointmentId the new appointment id
   */
  public void setAppointmentId(int appointmentId) {
    this.appointmentId = appointmentId;
  }

  /**
   * Gets the resource id.
   *
   * @return the resource id
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Sets the resource id.
   *
   * @param resourceId the new resource id
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * Gets the resource type.
   *
   * @return the resource type
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Sets the resource type.
   *
   * @param resourceType the new resource type
   */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Gets the resource name.
   *
   * @return the resource name
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * Sets the resource name.
   *
   * @param resourceName the new resource name
   */
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  /**
   * Gets the appointment item id.
   *
   * @return the appointment item id
   */
  public int getAppointment_item_id() {
    return appointmentItemId;
  }

  /**
   * Sets the appointment item id.
   *
   * @param appointmentItemId the new appointment item id
   */
  public void setAppointment_item_id(int appointmentItemId) {
    this.appointmentItemId = appointmentItemId;
  }

  /**
   * Gets the mod time.
   *
   * @return the mod time
   */
  public java.sql.Timestamp getMod_time() {
    return modTime;
  }

  /**
   * Sets the mod time.
   *
   * @param modTime the new mod time
   */
  public void setMod_time(java.sql.Timestamp modTime) {
    this.modTime = modTime;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUser_name() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param userName the new user name
   */
  public void setUser_name(String userName) {
    this.userName = userName;
  }
}
