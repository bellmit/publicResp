package com.insta.hms.core.clinical.patientactivities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesModel.ActivityStatus;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@DynamicUpdate
@Table(name = "patient_activities")
public class PatientActivitiesModel implements Serializable{

  public enum ActivityStatus {
    S,P,D,X 
  }

  private static final long serialVersionUID = 1L;

  SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  @JsonProperty("activity_id")
  private long activityId;
  @JsonProperty("patient_id")
  private String patientId;
  @JsonProperty("activity_type")
  private Character activityType;
  @JsonProperty("activity_num")
  private Integer activityNum;
  @JsonProperty("due_date")
  @JsonFormat(shape = Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private Date dueDate;
  @JsonProperty("prescription_type")
  private Character prescriptionType;
  @JsonProperty("prescription_id")
  private Integer prescriptionId;
  @JsonProperty("presc_doctor_id")
  private String prescDoctorId;
  @JsonProperty("med_batch")
  private String medBatch;
  @JsonProperty("med_exp_date")
  @JsonFormat(pattern = "dd-MM-yyyy")
  private Date medExpDate;
  @JsonProperty("activity_status")
  @Enumerated(EnumType.STRING)
  private ActivityStatus activityStatus;
  @JsonProperty("order_no")
  private Integer orderNo;
  @JsonProperty("activity_remarks")
  private String activityRemarks;
  @JsonProperty("added_by")
  private String addedBy;
  @JsonProperty("mod_time")
  @JsonFormat(shape = Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private Date modTime;

  private String username;
  @JsonProperty("completed_date")
  @JsonFormat(shape = Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private Date completedDate;
  @JsonProperty("completed_by")
  private String completedBy;
  @JsonProperty("ordered_datetime")
  @JsonFormat(shape = Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private Date orderedDatetime;
  @JsonProperty("gen_activity_details")
  private String genActivityDetails;
  @JsonProperty("ordered_by")
  private String orderedBy;
  @JsonProperty("external_order_no")
  private String externalOrderNo;
  @JsonProperty("setup_id")
  private Integer setupId;

  private String stock;
  @JsonProperty("infusion_site")
  private Integer infusionSite;
  @JsonProperty("iv_status")
  private Character ivStatus;
  @JsonProperty("serving_remarks_id")
  private Integer servingRemarksId;


  @Id
  @Column(name = "activity_id", nullable = false, updatable = false)
  public long getActivityId() {
    return activityId;
  }
  public void setActivityId(long activityId) {
    this.activityId = activityId;
  }

  @Column(name = "patient_id", nullable = false, updatable = false)
  public String getPatientId() {
    return patientId;
  }
  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  @Column(name = "activity_type", nullable = false)
  public Character getActivityType() {
    return activityType;
  }
  public void setActivityType(Character activityType) {
    this.activityType = activityType;
  }

  @Column(name = "activity_num", nullable = false)
  public Integer getActivityNum() {
    return activityNum;
  }
  public void setActivityNum(Integer activityNum) {
    this.activityNum = activityNum;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "due_date", nullable = false )
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  @JsonSetter(value = "due_date")
  public void setDueDate(String dueDate) throws ParseException {
    this.dueDate = dateFormat.parse(dueDate);
  }

  @Column(name = "prescription_type")
  public Character getPrescriptionType() {
    return prescriptionType;
  }
  public void setPrescriptionType(Character prescriptionType) {
    this.prescriptionType = prescriptionType;
  }

  @Column(name = "prescription_id", updatable = false)
  public Integer getPrescriptionId() {
    return prescriptionId;
  }
  public void setPrescriptionId(Integer prescriptionId) {
    this.prescriptionId = prescriptionId;
  }

  @Column(name = "presc_doctor_id")
  public String getPrescDoctorId() {
    return prescDoctorId;
  }
  public void setPrescDoctorId(String prescDoctorId) {
    this.prescDoctorId = prescDoctorId;
  }

  @Column(name = "med_batch")
  public String getMedBatch() {
    return medBatch;
  }
  public void setMedBatch(String medBatch) {
    this.medBatch = medBatch;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "med_exp_date")
  public Date getMedExpDate() {
    return medExpDate;
  }
  public void setMedExpDate(Date medExpDate) {
    this.medExpDate = medExpDate;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_status", length = 1)
  public ActivityStatus getActivityStatus() {
    return activityStatus;
  }
  public void setActivityStatus(ActivityStatus activityStatus) {
    this.activityStatus = activityStatus;
  }

  @Column(name = "order_no")
  public Integer getOrderNo() {
    return orderNo;
  }
  public void setOrderNo(Integer orderNo) {
    this.orderNo = orderNo;
  }

  @Column(name = "activity_remarks")
  public String getActivityRemarks() {
    return activityRemarks;
  }
  public void setActivityRemarks(String activityRemarks) {
    this.activityRemarks = activityRemarks;
  }

  @Column(name = "added_by", updatable = false )
  public String getAddedBy() {
    return addedBy;
  }
  public void setAddedBy(String addedBy) {
    this.addedBy = addedBy;
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

  @Column(name = "completed_date")
  @Temporal(TemporalType.TIMESTAMP)
  public Date getCompletedDate() {
    return completedDate;
  }
  public void setCompletedDate(Date completedDate) {
    this.completedDate = completedDate;
  }

  @JsonSetter(value = "completed_date")
  public void setCompletedDate(String completedDate) throws ParseException {
    if(completedDate != null && !"".equals(completedDate)) {
      this.completedDate = dateFormat.parse(completedDate);
    }
  }

  @Column(name = "completed_by")
  public String getCompletedBy() {
    return completedBy;
  }
  public void setCompletedBy(String completedBy) {
    this.completedBy = completedBy;
  }

  @Column(name = "ordered_datetime")
  @Temporal(TemporalType.TIMESTAMP)
  public Date getOrderedDatetime() {
    return orderedDatetime;
  }
  public void setOrderedDatetime(Date orderedDatetime) {
    this.orderedDatetime = orderedDatetime;
  }

  @JsonSetter(value = "ordered_datetime")
  public void setOrderedDatetime(String orderedDatetime) throws ParseException {
    if (orderedDatetime != null && !"".equals(orderedDatetime)) {
      this.orderedDatetime = dateFormat.parse(orderedDatetime);
    }
  }

  @Column(name = "gen_activity_details")
  public String getGenActivityDetails() {
    return genActivityDetails;
  }
  public void setGenActivityDetails(String genActivityDetails) {
    this.genActivityDetails = genActivityDetails;
  }

  @Column(name = "ordered_by")
  public String getOrderedBy() {
    return orderedBy;
  }
  public void setOrderedBy(String orderedBy) {
    this.orderedBy = orderedBy;
  }

  @Column(name = "external_order_no")
  public String getExternalOrderNo() {
    return externalOrderNo;
  }
  public void setExternalOrderNo(String externalOrderNo) {
    this.externalOrderNo = externalOrderNo;
  }

  @Column(name = "setup_id")
  public Integer getSetupId() {
    return setupId;
  }
  public void setSetupId(Integer setupId) {
    this.setupId = setupId;
  }

  @Column(name = "stock")
  public String getStock() {
    return stock;
  }
  public void setStock(String stock) {
    this.stock = stock;
  }

  @Column(name = "infusion_site")
  public Integer getInfusionSite() {
    return infusionSite;
  }
  public void setInfusionSite(Integer infusionSite) {
    this.infusionSite = infusionSite;
  }

  @Column(name = "iv_status")
  public Character getIvStatus() {
    return ivStatus;
  }
  public void setIvStatus(Character ivStatus) {
    this.ivStatus = ivStatus;
  }

  @Column(name = "serving_remarks_id")
  public Integer getServingRemarksId() {
    return servingRemarksId;
  }
  public void setServingRemarksId(Integer servingRemarksId) {
    this.servingRemarksId = servingRemarksId;
  }

  public void copyforUpdate(PatientActivitiesModel updateEntity) {
    // updateEntity must be the current db entity
    if (updateEntity.getActivityStatus().equals(ActivityStatus.D)) {
      throw new ValidationException("Administered activities can't be updated");
    }
    ValidationErrorMap errorMap = new ValidationErrorMap();
    updateEntity.setActivityId(this.activityId);
    updateEntity.setActivityRemarks(this.activityRemarks);
    if (this.activityStatus.equals(ActivityStatus.D)) {
      if (this.medBatch == null || "".equals(this.medBatch)) {
        errorMap.addError("med_batch", "Medication batch is required");
      }
      if (this.medExpDate == null) {
        errorMap.addError("med_exp_date", "Medication Expiry date is required");
      }
      if (this.completedDate == null) {
        errorMap.addError("completed_date", "Completed date is required");
      }
    }
    updateEntity.setActivityStatus(this.activityStatus);
    if (this.stock != null) {
      updateEntity.setStock(this.stock);
    }
    if (this.medBatch != null) {
      updateEntity.setMedBatch(this.medBatch);
    }
    if (this.medExpDate != null) {
      if ((updateEntity.getDueDate() != null && updateEntity.getDueDate().after(this.medExpDate)) || 
          (updateEntity.getDueDate() == null && this.medExpDate.before(new Date()))) {
        errorMap.addError("med_exp_date", "exception.mar.date.expiry");
      }
      updateEntity.setMedExpDate(this.medExpDate);
    }
    if (this.servingRemarksId != null) {
      updateEntity.setServingRemarksId(this.servingRemarksId);
    }
    if (this.infusionSite != null) {
      updateEntity.setInfusionSite(this.infusionSite);
    }
    if (this.ivStatus != null) {
      updateEntity.setIvStatus(this.ivStatus);
    }
    if (this.infusionSite != null) {
      updateEntity.setInfusionSite(this.infusionSite);
    }
    if (this.completedDate !=null) {
      updateEntity.setCompletedDate(this.completedDate);
    }
    updateEntity.setModTime(new Date());
    if (!errorMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorMap);
    }
  }
}
