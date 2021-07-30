/**
 *
 */
package com.insta.hms.erxprescription;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lakshmi
 *
 */
public class EPrescription {

  // Format : FacilityID_PayerID_UniqueNumber Auto-generated, Ex:
  // DHA-F-0046895-INS017-20130212172328';
  private String id; // Unique ID generated and sent to DHPO
  private String type;
  private String payerId;
  private String clinician;
  private EPrescriptionPatient patient;
  private EPrescriptionEncounter encounter;

  private List<EPrescriptionDiagnosis> diagnosis;
  private List<EPrescriptionActivity> activities;

  public EPrescription() {
    diagnosis = new ArrayList<EPrescriptionDiagnosis>();
    activities = new ArrayList<EPrescriptionActivity>();
  }

  public String getClinician() {
    return clinician;
  }

  public void setClinician(String clinician) {
    this.clinician = clinician;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPayerId() {
    return payerId;
  }

  public void setPayerId(String payerId) {
    this.payerId = payerId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public EPrescriptionPatient getPatient() {
    return patient;
  }

  public void setPatient(EPrescriptionPatient patient) {
    this.patient = patient;
  }

  public EPrescriptionEncounter getEncounter() {
    return encounter;
  }

  public void setEncounter(EPrescriptionEncounter encounter) {
    this.encounter = encounter;
  }

  public List<EPrescriptionDiagnosis> getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(List<EPrescriptionDiagnosis> diagnosis) {
    this.diagnosis = diagnosis;
  }

  public void addDiagnosis(EPrescriptionDiagnosis diag) {
    diagnosis.add(diag);
  }

  public List<EPrescriptionActivity> getActivities() {
    return activities;
  }

  public void setActivities(List<EPrescriptionActivity> activities) {
    this.activities = activities;
  }

  public void addActivity(EPrescriptionActivity activity) {
    activities.add(activity);
  }
}
