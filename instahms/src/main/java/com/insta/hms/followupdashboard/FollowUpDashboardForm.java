package com.insta.hms.followupdashboard;

import org.apache.struts.action.ActionForm;

/**
 * The Class FollowUpDashboardForm.
 */
public class FollowUpDashboardForm extends ActionForm {

  /**
   * The first name.
   */
  private String firstName;

  /**
   * The last name.
   */
  private String lastName;

  /**
   * The phone.
   */
  private String phone;

  /**
   * The mrno.
   */
  private String mrno;

  /**
   * The page num.
   */
  private String pageNum;

  /**
   * The sort order.
   */
  private String sortOrder;

  /**
   * The condition.
   */
  private String condition;

  /**
   * The fdate.
   */
  private String fdate;

  /**
   * The tdate.
   */
  private String tdate;

  /**
   * The department.
   */
  private String[] department;

  /**
   * The doctor.
   */
  private String[] doctor;

  /**
   * The sort reverse.
   */
  private boolean sortReverse;

  /**
   * The start page.
   */
  private String startPage;

  /**
   * The end page.
   */
  private String endPage;

  /**
   * Gets the condition.
   *
   * @return the condition
   */
  public String getCondition() {
    return condition;
  }

  /**
   * Sets the condition.
   *
   * @param condition the new condition
   */
  public void setCondition(String condition) {
    this.condition = condition;
  }

  /**
   * Gets the department.
   *
   * @return the department
   */
  public String[] getDepartment() {
    return department;
  }

  /**
   * Sets the department.
   *
   * @param department the new department
   */
  public void setDepartment(String[] department) {
    this.department = department;
  }

  /**
   * Gets the doctor.
   *
   * @return the doctor
   */
  public String[] getDoctor() {
    return doctor;
  }

  /**
   * Sets the doctor.
   *
   * @param doctor the new doctor
   */
  public void setDoctor(String[] doctor) {
    this.doctor = doctor;
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
   * Gets the fdate.
   *
   * @return the fdate
   */
  public String getFdate() {
    return fdate;
  }

  /**
   * Sets the fdate.
   *
   * @param fdate the new fdate
   */
  public void setFdate(String fdate) {
    this.fdate = fdate;
  }

  /**
   * Gets the first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the first name.
   *
   * @param firstName the new first name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName the new last name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Gets the mrno.
   *
   * @return the mrno
   */
  public String getMrno() {
    return mrno;
  }

  /**
   * Sets the mrno.
   *
   * @param mrno the new mrno
   */
  public void setMrno(String mrno) {
    this.mrno = mrno;
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
   * Gets the phone.
   *
   * @return the phone
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the phone.
   *
   * @param phone the new phone
   */
  public void setPhone(String phone) {
    this.phone = phone;
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
   * Gets the tdate.
   *
   * @return the tdate
   */
  public String getTdate() {
    return tdate;
  }

  /**
   * Sets the tdate.
   *
   * @param tdate the new tdate
   */
  public void setTdate(String tdate) {
    this.tdate = tdate;
  }
}
