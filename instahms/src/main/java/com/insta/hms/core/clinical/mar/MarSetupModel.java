package com.insta.hms.core.clinical.mar;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@Entity
@DynamicUpdate
@Table(name = "patient_mar_setup")
public class MarSetupModel implements Serializable{
  
  private static final long serialVersionUID = 1L;
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  @JsonProperty("setup_id")
  private Integer setupId;
  @JsonProperty("serving_frequency_id")
  private Integer servingFrequencyId;
  @JsonProperty("prescription_id")
  private Integer prescriptionId;
  @JsonProperty("remarks")
  private String remarks;
  @JsonProperty("mod_time")
  @JsonFormat(shape = Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private Date modTime;
  private String username;
  @JsonProperty("serving_dosage")
  private String servingDosage;
  @JsonProperty("package_uom")
  private String packageUom;
  
  @Id
  @Column(name = "setup_id", updatable = false)
  public Integer getSetupId() {
    return setupId;
  }
  public void setSetupId(Integer setupId) {
    this.setupId = setupId;
  }
  
  @Column(name = "serving_frequency_id")
  public Integer getServingFrequencyId() {
    return servingFrequencyId;
  }
  public void setServingFrequencyId(Integer servingFrequencyId) {
    this.servingFrequencyId = servingFrequencyId;
  }
  
  @Column(name = "prescription_id", updatable = false)
  public Integer getPrescriptionId() {
    return prescriptionId;
  }
  public void setPrescriptionId(Integer prescriptionId) {
    this.prescriptionId = prescriptionId;
  }
  
  @Column(name = "remarks") 
  public String getRemarks() {
    return remarks;
  }
  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
  
  @Column(name = "mod_time", insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  public Date getModTime() {
    return modTime;
  }
  public void setModTime(Date modTime) {
    this.modTime = modTime;
  }

  @JsonSetter(value = "mod_time")
  public void setModTime(String modTime) throws ParseException {
    this.modTime = dateFormat.parse(modTime);
  }

  @Column(name = "username")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
  
  @Column(name = "serving_dosage")
  public String getServingDosage() {
    return servingDosage;
  }
  public void setServingDosage(String servingDosage) {
    this.servingDosage = servingDosage;
  }
  
  @Column(name = "package_uom")
  public String getPackageUom() {
    return packageUom;
  }
  public void setPackageUom(String packageUom) {
    this.packageUom = packageUom;
  }
  
  
  
  
}
