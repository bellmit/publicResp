/**
 *
 */

package com.insta.hms.eauthorization.priorauth;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * The Class PriorAuthAuthorization.
 *
 * @author lakshmi
 */
public class PriorAuthAuthorization {

  /**
   * The authorization result.
   */
  private String authorizationResult;

  /**
   * The authorization ID.
   */
  private String authorizationID;

  /**
   * The authorization ID payer.
   */
  private String authorizationIDPayer;

  /**
   * The denial code.
   */
  private String denialCode;

  /**
   * The start.
   */
  private String start;

  /**
   * The end.
   */
  private String end;

  /**
   * The limit.
   */
  private BigDecimal limit;

  /**
   * The comments.
   */
  private String comments;

  /**
   * The activities.
   */
  public ArrayList<PriorAuthorizationActivity> activities;

  /**
   * Instantiates a new prior auth authorization.
   */
  public PriorAuthAuthorization() {
    activities = new ArrayList<PriorAuthorizationActivity>();
  }

  /**
   * Gets the activities.
   *
   * @return the activities
   */
  public ArrayList<PriorAuthorizationActivity> getActivities() {
    return activities;
  }

  /**
   * Sets the activities.
   *
   * @param activities the new activities
   */
  public void setActivities(
      ArrayList<PriorAuthorizationActivity> activities) {
    this.activities = activities;
  }

  /**
   * Adds the activity.
   *
   * @param activity the activity
   */
  public void addActivity(PriorAuthorizationActivity activity) {
    activities.add(activity);
  }

  /**
   * Gets the authorization ID.
   *
   * @return the authorization ID
   */
  public String getAuthorizationID() {
    return authorizationID;
  }

  /**
   * Sets the authorization ID.
   *
   * @param authorizationID the new authorization ID
   */
  public void setAuthorizationID(String authorizationID) {
    this.authorizationID = authorizationID;
  }

  /**
   * Gets the authorization ID payer.
   *
   * @return the authorization ID payer
   */
  public String getAuthorizationIDPayer() {
    return authorizationIDPayer;
  }

  /**
   * Sets the authorization ID payer.
   *
   * @param authorizationIDPayer the new authorization ID payer
   */
  public void setAuthorizationIDPayer(String authorizationIDPayer) {
    this.authorizationIDPayer = authorizationIDPayer;
  }

  /**
   * Gets the authorization result.
   *
   * @return the authorization result
   */
  public String getAuthorizationResult() {
    return authorizationResult;
  }

  /**
   * Sets the authorization result.
   *
   * @param authorizationResult the new authorization result
   */
  public void setAuthorizationResult(String authorizationResult) {
    this.authorizationResult = authorizationResult;
  }

  /**
   * Gets the comments.
   *
   * @return the comments
   */
  public String getComments() {
    return comments;
  }

  /**
   * Sets the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   * Gets the denial code.
   *
   * @return the denial code
   */
  public String getDenialCode() {
    return denialCode;
  }

  /**
   * Sets the denial code.
   *
   * @param denialCode the new denial code
   */
  public void setDenialCode(String denialCode) {
    this.denialCode = denialCode;
  }

  /**
   * Gets the end.
   *
   * @return the end
   */
  public String getEnd() {
    return end;
  }

  /**
   * Sets the end.
   *
   * @param end the new end
   */
  public void setEnd(String end) {
    this.end = end;
  }

  /**
   * Gets the limit.
   *
   * @return the limit
   */
  public BigDecimal getLimit() {
    return limit;
  }

  /**
   * Sets the limit.
   *
   * @param limit the new limit
   */
  public void setLimit(BigDecimal limit) {
    this.limit = limit;
  }

  /**
   * Gets the start.
   *
   * @return the start
   */
  public String getStart() {
    return start;
  }

  /**
   * Sets the start.
   *
   * @param start the new start
   */
  public void setStart(String start) {
    this.start = start;
  }
}
