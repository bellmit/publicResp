package com.insta.hms.usermanager;

import org.apache.struts.action.ActionForm;

import java.util.HashMap;

/**
 * The Class RoleForm.
 */
public class RoleForm extends ActionForm {

  /** The operation. */
  private String operation;
  
  /** The role id. */
  private String roleId;
  
  /** The name. */
  private String name;
  
  /** The status. */
  private String status = "A";
  
  /** The remarks. */
  private String remarks;
  
  /** The sort reverse. */
  private boolean sortReverse;
  
  /** The start page. */
  private String startPage;
  
  /** The end page. */
  private String endPage;
  
  /** The page num. */
  private String pageNum;
  
  /** The sort order. */
  private String sortOrder;
  
  /** The role name. */
  private String roleName;
  
  /** The user name. */
  private String userName;
  
  /** The active. */
  private boolean active;
  
  /** The in active. */
  private boolean inActive;
  
  /** The hospital. */
  private boolean hospital;
  
  /** The patient portal. */
  private boolean patientPortal;
  
  /** The doctor portal. */
  private boolean doctorPortal;

  /** The screen rights. */
  private HashMap screenRights = new HashMap(); // screen_id => rights, eg "bill_billing" => "R"
  
  /** The action rights. */
  private HashMap actionRights = new HashMap(); // action_id => rights, eg "bill_reopen" => "A"

  /**
   * Gets the operation.
   *
   * @return the operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Sets the operation.
   *
   * @param operation the new operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * Gets the role id.
   *
   * @return the role id
   */
  public String getRoleId() {
    return roleId;
  }

  /**
   * Sets the role id.
   *
   * @param roleId the new role id
   */
  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
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

  /**
   * Gets the remarks.
   *
   * @return the remarks
   */
  public String getRemarks() {
    return remarks;
  }

  /**
   * Sets the remarks.
   *
   * @param remarks the new remarks
   */
  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  /**
   * Gets the screen rights.
   *
   * @param key the key
   * @return the screen rights
   */
  public String getScreenRights(String key) {
    return (String) screenRights.get(key);
  }

  /**
   * Sets the screen rights.
   *
   * @param key the key
   * @param value the value
   */
  public void setScreenRights(String key, String value) {
    if (value.equals("A")) {
      screenRights.put(key, value);
    }
  }

  /**
   * Gets the action rights.
   *
   * @param key the key
   * @return the action rights
   */
  public String getActionRights(String key) {
    return (String) actionRights.get(key);
  }

  /**
   * Sets the action rights.
   *
   * @param key the key
   * @param value the value
   */
  public void setActionRights(String key, String value) {
    actionRights.put(key, value);
  }

  /**
   * Gets the end page.
   *
   * @return the end page
   */
  public String getEndPage() {
    return endPage;
  }

  /**
   * Sets the end page.
   *
   * @param endPage the new end page
   */
  public void setEndPage(String endPage) {
    this.endPage = endPage;
  }

  /**
   * Gets the page num.
   *
   * @return the page num
   */
  public String getPageNum() {
    return pageNum;
  }

  /**
   * Sets the page num.
   *
   * @param pageNum the new page num
   */
  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * Gets the sort order.
   *
   * @return the sort order
   */
  public String getSortOrder() {
    return sortOrder;
  }

  /**
   * Sets the sort order.
   *
   * @param sortOrder the new sort order
   */
  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * Checks if is sort reverse.
   *
   * @return true, if is sort reverse
   */
  public boolean isSortReverse() {
    return sortReverse;
  }

  /**
   * Sets the sort reverse.
   *
   * @param sortReverse the new sort reverse
   */
  public void setSortReverse(boolean sortReverse) {
    this.sortReverse = sortReverse;
  }

  /**
   * Gets the start page.
   *
   * @return the start page
   */
  public String getStartPage() {
    return startPage;
  }

  /**
   * Sets the start page.
   *
   * @param startPage the new start page
   */
  public void setStartPage(String startPage) {
    this.startPage = startPage;
  }

  /**
   * Checks if is active.
   *
   * @return true, if is active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets the active.
   *
   * @param active the new active
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Checks if is doctor portal.
   *
   * @return true, if is doctor portal
   */
  public boolean isDoctorPortal() {
    return doctorPortal;
  }

  /**
   * Sets the doctor portal.
   *
   * @param doctorPortal the new doctor portal
   */
  public void setDoctorPortal(boolean doctorPortal) {
    this.doctorPortal = doctorPortal;
  }

  /**
   * Checks if is hospital.
   *
   * @return true, if is hospital
   */
  public boolean isHospital() {
    return hospital;
  }

  /**
   * Sets the hospital.
   *
   * @param hospital the new hospital
   */
  public void setHospital(boolean hospital) {
    this.hospital = hospital;
  }

  /**
   * Checks if is in active.
   *
   * @return true, if is in active
   */
  public boolean isInActive() {
    return inActive;
  }

  /**
   * Sets the in active.
   *
   * @param inActive the new in active
   */
  public void setInActive(boolean inActive) {
    this.inActive = inActive;
  }

  /**
   * Checks if is patient portal.
   *
   * @return true, if is patient portal
   */
  public boolean isPatientPortal() {
    return patientPortal;
  }

  /**
   * Sets the patient portal.
   *
   * @param patientPortal the new patient portal
   */
  public void setPatientPortal(boolean patientPortal) {
    this.patientPortal = patientPortal;
  }

  /**
   * Gets the role name.
   *
   * @return the role name
   */
  public String getRoleName() {
    return roleName;
  }

  /**
   * Sets the role name.
   *
   * @param roleName the new role name
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param userName the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Gets the screen rights map.
   *
   * @return the screen rights map
   */
  public HashMap getScreenRightsMap() {
    return screenRights;
  }

  /**
   * Gets the action rights map.
   *
   * @return the action rights map
   */
  public HashMap getActionRightsMap() {
    return actionRights;
  }

}
