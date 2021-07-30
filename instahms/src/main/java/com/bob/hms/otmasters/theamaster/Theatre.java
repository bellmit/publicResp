package com.bob.hms.otmasters.theamaster;

public class Theatre {
  // this class represents a theatre_master

  private String theatreId;
  private String theatreName;
  private String status;
  private int unitSize;
  private int minDuration;
  private int slab1Threshold;
  private int incrDuration;
  private boolean schedule;
  private Integer overbookLimit;
  private int centerId;
  private int storeId;
  private String allowZeroClaimAmount;
  private Integer billingGroupId;

  public int getStoreId() {
    return storeId;
  }

  public void setStoreId(int storeId) {
    this.storeId = storeId;
  }

  public int getCenterId() {
    return centerId;
  }

  public void setCenterId(int centerId) {
    this.centerId = centerId;
  }

  public int getIncrDuration() {
    return incrDuration;
  }

  public void setIncrDuration(int incrDuration) {
    this.incrDuration = incrDuration;
  }

  public int getMinDuration() {
    return minDuration;
  }

  public void setMinDuration(int minDuration) {
    this.minDuration = minDuration;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTheatreId() {
    return theatreId;
  }

  public void setTheatreId(String theatreId) {
    this.theatreId = theatreId;
  }

  public String getTheatreName() {
    return theatreName;
  }

  public void setTheatreName(String theatreName) {
    this.theatreName = theatreName;
  }

  public boolean getSchedule() {
    return schedule;
  }

  public void setSchedule(boolean schedule) {
    this.schedule = schedule;
  }

  public Integer getOverbookLimit() {
    return overbookLimit;
  }

  public void setOverbookLimit(Integer obLimit) {
    this.overbookLimit = obLimit;
  }

  public int getUnitSize() {
    return unitSize;
  }

  public void setUnitSize(int unitSize) {
    this.unitSize = unitSize;
  }

  public int getSlab1Threshold() {
    return slab1Threshold;
  }

  public void setSlab1Threshold(int slab1Threshold) {
    this.slab1Threshold = slab1Threshold;
  }

  public String getAllowZeroClaimAmount() {
    return allowZeroClaimAmount;
  }

  public void setAllowZeroClaimAmount(String allowZeroClaimAmount) {
    this.allowZeroClaimAmount = allowZeroClaimAmount;
  }

  public Integer getBillingGroupId() {
    return billingGroupId;
  }

  public void setBillingGroupId(Integer billingGroupId) {
    this.billingGroupId = billingGroupId;
  }

}
