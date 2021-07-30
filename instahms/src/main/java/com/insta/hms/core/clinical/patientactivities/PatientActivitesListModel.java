package com.insta.hms.core.clinical.patientactivities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public class PatientActivitesListModel implements Serializable{


  private static final long serialVersionUID = 1L;

  @JsonProperty("activities")
  private List<PatientActivitiesModel> activities;

  public List<PatientActivitiesModel> getActivities() {
    return activities;
  }

  public void setActivities(List<PatientActivitiesModel> activities) {
    this.activities = activities;
  }

}
