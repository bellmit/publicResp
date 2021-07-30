/**
 *
 */
package com.insta.hms.erxprescription;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author lakshmi
 *
 */
public class EPrescriptionActivity {

  public class Frequency {
    private BigDecimal unit; // Granular unit of the frequency.
    private BigDecimal value; // Number of repetitions for a given frequency.
    private String type; // Frequency time unit. Possible values: Hour/Day/Week/Once
    private String valueType;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public BigDecimal getUnit() {
      return unit;
    }

    public void setUnit(BigDecimal unit) {
      this.unit = unit;
    }

    public BigDecimal getValue() {
      return value;
    }

    public void setValue(BigDecimal value) {
      this.value = value;
    }

    public String getValueType() {
      return valueType;
    }

    public void setValueType(String valueType) {
      this.valueType = valueType;
    }
  }

  private int medicineID;
  private String activityName;
  private String activityID;
  private String activityStart;
  private String activityType;
  private String activityCode;
  private BigDecimal quantity;
  private BigDecimal duration;
  private BigDecimal refills;
  private String routeOfAdminName;
  private int routeOfAdminId;
  private String routOfAdmin;
  private String instructions;
  private Frequency frequency;

  public Frequency getFrequency() {
    return frequency;
  }

  public void setFrequency(Frequency frequency) {
    this.frequency = frequency;
  }

  private ArrayList<EPrescriptionActivityObservation> observations;

  public EPrescriptionActivity() {
    frequency = new Frequency();
    observations = new ArrayList<>();
  }

  public ArrayList<EPrescriptionActivityObservation> getObservations() {
    return observations;
  }

  public void setObservations(ArrayList<EPrescriptionActivityObservation> observations) {
    this.observations = observations;
  }

  public void addObservation(EPrescriptionActivityObservation observation) {
    observations.add(observation);
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public String getActivityID() {
    return activityID;
  }

  public void setActivityID(String activityID) {
    this.activityID = activityID;
  }

  public String getActivityCode() {
    return activityCode;
  }

  public void setActivityCode(String activityCode) {
    this.activityCode = activityCode;
  }

  public String getActivityStart() {
    return activityStart;
  }

  public void setActivityStart(String activityStart) {
    this.activityStart = activityStart;
  }

  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public BigDecimal getDuration() {
    return duration;
  }

  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }

  public String getInstructions() {
    return instructions;
  }

  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getRefills() {
    return refills;
  }

  public void setRefills(BigDecimal refills) {
    this.refills = refills;
  }

  public String getRoutOfAdmin() {
    return routOfAdmin;
  }

  public void setRoutOfAdmin(String routOfAdmin) {
    this.routOfAdmin = routOfAdmin;
  }

  public int getMedicineID() {
    return medicineID;
  }

  public void setMedicineID(int medicineID) {
    this.medicineID = medicineID;
  }

  public String getRouteOfAdminName() {
    return routeOfAdminName;
  }

  public void setRouteOfAdminName(String routeOfAdminName) {
    this.routeOfAdminName = routeOfAdminName;
  }

  public int getRouteOfAdminId() {
    return routeOfAdminId;
  }

  public void setRouteOfAdminId(int routeOfAdminId) {
    this.routeOfAdminId = routeOfAdminId;
  }
}
