package com.bob.hms.changepassword;

import org.apache.struts.action.ActionForm;

public class ChangePasswordForm extends ActionForm {

  /*
   * General getter and setter methods for dynamic retrieval of data from database and storing of
   * data in database tables
   */

  private String oldpwd;
  private String pwd;
  private String uid;

  /**
   * Gets the uid.
   *
   * @return Returns the uid.
   */
  public String getUid() {
    return uid;
  }

  /**
   * Sets the uid.
   *
   * @param uid
   *          The uid to set.
   */
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Gets the oldpwd.
   *
   * @return Returns the oldpwd.
   */
  public String getOldpwd() {
    return oldpwd;
  }

  /**
   * Sets the oldpwd.
   *
   * @param oldpwd
   *          The oldpwd to set.
   */
  public void setOldpwd(String oldpwd) {
    this.oldpwd = oldpwd;
  }

  /**
   * Gets the pwd.
   *
   * @return Returns the pwd.
   */
  public String getPwd() {
    return pwd;
  }

  /**
   * Sets the pwd.
   *
   * @param pwd
   *          The pwd to set.
   */
  public void setPwd(String pwd) {
    this.pwd = pwd;
  }

}