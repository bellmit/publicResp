/**
 *
 */

package com.insta.hms.eauthorization;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

/**
 * The Class EAuthRequest.
 *
 * @author lakshmi
 */
public class EAuthRequest {

  /**
   * The eauthbean.
   */
  BasicDynaBean eauthbean;

  /**
   * The diagnosis.
   */
  List diagnosis;

  /**
   * The activities.
   */
  List activities;

  /**
   * The observations map.
   */
  Map observationsMap;

  /**
   * The attachment.
   */
  String attachment;

  /**
   * Gets the eauthbean.
   *
   * @return the eauthbean
   */
  public BasicDynaBean getEauthbean() {
    return eauthbean;
  }

  /**
   * Sets the eauthbean.
   *
   * @param eauthbean the new eauthbean
   */
  public void setEauthbean(BasicDynaBean eauthbean) {
    this.eauthbean = eauthbean;
  }

  /**
   * Gets the diagnosis.
   *
   * @return the diagnosis
   */
  public List getDiagnosis() {
    return diagnosis;
  }

  /**
   * Sets the diagnosis.
   *
   * @param diagnosis the new diagnosis
   */
  public void setDiagnosis(List diagnosis) {
    this.diagnosis = diagnosis;
  }

  /**
   * Gets the activities.
   *
   * @return the activities
   */
  public List getActivities() {
    return activities;
  }

  /**
   * Sets the activities.
   *
   * @param activities the new activities
   */
  public void setActivities(List activities) {
    this.activities = activities;
  }

  /**
   * Gets the observations map.
   *
   * @return the observations map
   */
  public Map getObservationsMap() {
    return observationsMap;
  }

  /**
   * Sets the observations map.
   *
   * @param observationsMap the new observations map
   */
  public void setObservationsMap(Map observationsMap) {
    this.observationsMap = observationsMap;
  }

  /**
   * Gets the attachment.
   *
   * @return the attachment
   */
  public String getAttachment() {
    return attachment;
  }

  /**
   * Sets the attachment.
   *
   * @param attachment the new attachment
   */
  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }
}
