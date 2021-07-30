package com.insta.hms.core.clinical.ivadministrationdetails;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.AbstractPersistable;
import javax.persistence.Temporal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;


@Entity
@DynamicUpdate
@Table(name = "patient_iv_administered_details")
public class PatientIvAdminstrationModel implements Serializable {

  private static final long serialVersionUID = 1L;
  
  @JsonProperty("id")
  private long id;
  
  @JsonProperty("activity_id")
  private long activityId;

  private Character state;

  private String username;

  @JsonProperty("mod_time")
  @JsonFormat(shape = Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private Date modTime;

  private String remarks;
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public long getId() {
    return id;
  }
  
  public void setId(long id) {
    this.id = id;
  }

  @Column(name = "activity_id", nullable = false)
  public long getActivityId() {
    return activityId;
  }

  public void setActivityId(long activityId) {
    this.activityId = activityId;
  }

  @Column(name = "state", nullable = false)
  public Character getState() {
    return state;
  }

  public void setState(Character state) {
    this.state = state;
  }

  @Column(name = "username", nullable = false)
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Column(name = "mod_time", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  public Date getModTime() {
    return modTime;
  }

  public void setModTime(Date modTime) {
    this.modTime = modTime;
  }

  @Column(name = "remarks")
  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

}
