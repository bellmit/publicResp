/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to make an object for project.
 */

public class Project {

  /** The project id. */
  private String projectId = "";

  /** The project name. */
  private String projectName = "";

  /** The customer id. */
  private String customerId = "";

  /** The customer name. */
  private String customerName = "";

  /** The description. */
  private String description = "";

  /** The status. */
  private String status = "";

  /** The billing type. */
  private String billingType = "";

  /** The rate. */
  private double rate = 0.00;

  /** The created time. */
  private String createdTime = "";

  /** The currency code. */
  private String currencyCode = "";

  /** The budget hours. */
  private int budgetHours = 0;

  /** The budget type. */
  private String budgetType = "";

  /** The budget amount. */
  private double budgetAmount = 0.00;

  /** The total hours. */
  private String totalHours = "";

  /** The billed hours. */
  private String billedHours = "";

  /** The un billed hours. */
  private String unBilledHours = "";

  /** The tasks. */
  private List<Task> tasks = new ArrayList<Task>();

  /** The users. */
  private List<User> users = new ArrayList<User>();

  /**
   * set the project id.
   * 
   * @param projectId
   *          ID of the project.
   */

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  /**
   * get the project id.
   * 
   * @return Returns the ID of the project.
   */

  public String getProjectId() {
    return projectId;
  }

  /**
   * set the project name.
   * 
   * @param projectName
   *          Name of the project.
   */

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  /**
   * get the project name.
   * 
   * @return Returns the name of the project.
   */

  public String getProjectName() {
    return projectName;
  }

  /**
   * set the customer id.
   * 
   * @param customerId
   *          ID of the customer.
   */

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  /**
   * get the customer id.
   * 
   * @return Returns the ID of the customer.
   */

  public String getCustomerId() {
    return customerId;
  }

  /**
   * set the customer name.
   * 
   * @param customerName
   *          Name of the customer.
   */

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  /**
   * get the customer name.
   * 
   * @return Returns the name of the customer.
   */

  public String getCustomerName() {
    return customerName;
  }

  /**
   * set the description.
   * 
   * @param description
   *          Project description.
   */

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * get the description.
   * 
   * @return Returns the project description.
   */

  public String getDescription() {
    return description;
  }

  /**
   * set the status.
   * 
   * @param status
   *          Status of the project.
   */

  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * get the status.
   * 
   * @return Returns the status of the project.
   */

  public String getStatus() {
    return status;
  }

  /**
   * set the billing type.
   * 
   * @param billingType
   *          The way you bill your customer.
   */

  public void setBillingType(String billingType) {
    this.billingType = billingType;
  }

  /**
   * get the billing type.
   * 
   * @return Returns the way you bill your customer.
   */

  public String getBillingType() {
    return billingType;
  }

  /**
   * set the rate.
   * 
   * @param rate
   *          Hourly rate for a task.
   */

  public void setRate(double rate) {
    this.rate = rate;
  }

  /**
   * get the rate.
   * 
   * @return Returns the hourly rate for a task.
   */

  public double getRate() {
    return rate;
  }

  /**
   * set the created time.
   * 
   * @param createdTime
   *          Created time of the project.
   */

  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   * get the created time.
   * 
   * @return Returns the created time of the project.
   */

  public String getCreatedTime() {
    return createdTime;
  }

  /**
   * set the currency code.
   * 
   * @param currencyCode
   *          Standard code for the currency.
   */

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  /**
   * get the currency code.
   * 
   * @return Returns the standard code for the currency.
   */

  public String getCurrencyCode() {
    return currencyCode;
  }

  /**
   * set the budget hours.
   * 
   * @param budgetHours
   *          Task budget hours.
   */

  public void setBudgetHours(int budgetHours) {
    this.budgetHours = budgetHours;
  }

  /**
   * get the budget hours.
   * 
   * @return Returns the task budget hours.
   */

  public int getBudgetHours() {
    return budgetHours;
  }

  /**
   * set the budget type.
   * 
   * @param budgetType
   *          The way you budget.
   */

  public void setBudgetType(String budgetType) {
    this.budgetType = budgetType;
  }

  /**
   * get the budget type.
   * 
   * @return Returns the way you budget.
   */

  public String getBudgetType() {
    return budgetType;
  }

  /**
   * set the budget amount.
   * 
   * @param budgetAmount
   *          Give value, if you are estimating total project cost.
   */

  public void setBudgetAmount(double budgetAmount) {
    this.budgetAmount = budgetAmount;
  }

  /**
   * get the budget amount.
   * 
   * @return Returns the total project cost.
   */

  public double getBudgetAmount() {
    return budgetAmount;
  }

  /**
   * set the total hours.
   * 
   * @param totalHours
   *          Total hours for the project.
   */

  public void setTotalHours(String totalHours) {
    this.totalHours = totalHours;
  }

  /**
   * get the total hours.
   * 
   * @return Returns the total hours for the project.
   */

  public String getTotalHours() {
    return totalHours;
  }

  /**
   * set the billed hours.
   * 
   * @param billedHours
   *          Billed hours for the project.
   */

  public void setBilledHours(String billedHours) {
    this.billedHours = billedHours;
  }

  /**
   * get the billed hours.
   * 
   * @return Returns the billed hours for the project.
   */

  public String getBilledHours() {
    return billedHours;
  }

  /**
   * set the un billed hours.
   * 
   * @param unBilledHours
   *          Un billed hours for the project.
   */

  public void setUnBilledHours(String unBilledHours) {
    this.unBilledHours = unBilledHours;
  }

  /**
   * get the un billed hours.
   * 
   * @return Returns the un billed hours for the project.
   */

  public String getUnBilledHours() {
    return unBilledHours;
  }

  /**
   * set the tasks.
   *
   * @param tasks
   *          Tasks for the project.
   * @throws Exception
   *           the exception
   */

  public void setTasks(List<Task> tasks) throws Exception {
    this.tasks = tasks;
  }

  /**
   * get the tasks.
   * 
   * @return Returns list of Task object.
   */

  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * set the users.
   *
   * @param users
   *          Users for the project.
   * @throws Exception
   *           the exception
   */

  public void setUsers(List<User> users) throws Exception {
    this.users = users;
  }

  /**
   * get the users.
   * 
   * @return Returns list of User object.
   */

  public List<User> getUsers() {
    return users;
  }

  /**
   * Convert Project object into JSONObject.
   *
   * @return Returns a JSONObject.
   * @throws Exception
   *           the exception
   */

  public JSONObject toJSON() throws Exception {
    JSONObject jsonObject = new JSONObject();

    jsonObject.put("project_name", projectName);
    jsonObject.put("customer_id", customerId);
    jsonObject.put("description", description);
    jsonObject.put("billing_type", billingType);
    jsonObject.put("rate", rate);
    jsonObject.put("budget_type", budgetType);
    jsonObject.put("budget_hours", budgetHours);
    jsonObject.put("budget_amount", budgetAmount);

    if (tasks != null) {
      JSONArray task = new JSONArray();

      for (int i = 0; i < tasks.size(); i++) {
        task.put(tasks.get(i).toJSON());
      }
      jsonObject.put("tasks", task);
    }

    if (users != null) {
      JSONArray userss = new JSONArray();
      for (int j = 0; j < users.size(); j++) {
        JSONObject user = new JSONObject();

        user.put("user_id", users.get(j).getUserId());
        user.put("rate", users.get(j).getRate());
        user.put("budget_hours", users.get(j).getBudgetHours());

        userss.put(user);
      }
      jsonObject.put("users", userss);
    }

    return jsonObject;
  }
}
