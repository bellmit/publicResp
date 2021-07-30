/*
 * Copyright (c) 2008-2009 Insta Health Solutions Pvt Ltd All rights reserved.
 */

package com.insta.hms.usermanager;

/*
 * Role: Simple DTO to store Role information
 */
public class Role {
  private int roleId;
  private String name;
  private String status;
  private String remarks;
  private String portalId;
  private String modUser;
  private String modDate;

  public String getModDate() {
    return modDate;
  }

  public void setModDate(String modDate) {
    this.modDate = modDate;
  }

  public String getModUser() {
    return modUser;
  }

  public void setModUser(String modUser) {
    this.modUser = modUser;
  }

  public int getRoleId() {
    return roleId;
  }

  public void setRoleId(int roleId) {
    this.roleId = roleId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getPortalId() {
    return portalId;
  }

  public void setPortalId(String portalId) {
    this.portalId = portalId;
  }

  public static String BILL_REOPEN = "bill_reopen";
  public static String DISHCHARGE_CLOSE = "dishcharge_close";
  public static String BED_CLOSE = "bed_close";
  public static String ADDTOBILL_CHARGES = "addtobill_charges";
  public static String REFUNDS = "allow_refund";
  public static String EDIT_PATIENT_FIRST_NAME = "edit_first_name";
  public static String CANCEL_TEST_AFTER_SAMPLE_COLLECTION = "allow_cancel_test";
  public static String USR_COUNTER_DAY_BOOK_ACCESS = "usr_or_counter_day_book_access";

}
