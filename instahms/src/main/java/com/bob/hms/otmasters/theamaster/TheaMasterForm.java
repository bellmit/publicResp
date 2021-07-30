package com.bob.hms.otmasters.theamaster;

import org.apache.struts.action.ActionForm;

public class TheaMasterForm extends ActionForm {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private String theatreId;
  private String theatreName;
  private String status;
  private String orgName;
  private String orgId;
  private int unitSize;
  private int minDuration;
  private int slab1Threshold;
  private int incrDuration;
  private int centerId;
  private int storeId;
  private String[] bedTypes;
  private Double[] dailyCharge;
  private Double[] minCharge;
  private Double[] slab1Charge;
  private Double[] incrCharge;
  private Double[] dailyChargeDiscount;
  private Double[] minChargeDiscount;
  private Double[] slab1ChargeDiscount;
  private Double[] incrChargeDiscount;

  private String allTheatres;
  private String activeTheatres;
  private String inactiveTheatres;
  private String pageNum;
  private String chargeType;

  private String[] groupBeds;
  private String varianceType;
  private Double varianceBy;
  private Double varianceValue;
  private String[] groupTheatres;
  private String groupUpdatComponent;
  private String updateTable;

  private boolean schedule;
  private Integer overbook_limit;
  private String allowZeroClaimAmount;
  private Integer billingGroupId;

  public String[] getGroupBeds() {
    return groupBeds;
  }

  public void setGroupBeds(String[] groupBeds) {
    this.groupBeds = groupBeds;
  }

  public String[] getGroupTheatres() {
    return groupTheatres;
  }

  public void setGroupTheatres(String[] groupTheatres) {
    this.groupTheatres = groupTheatres;
  }

  public String getGroupUpdatComponent() {
    return groupUpdatComponent;
  }

  public void setGroupUpdatComponent(String groupUpdatComponent) {
    this.groupUpdatComponent = groupUpdatComponent;
  }

  public Double getVarianceBy() {
    return varianceBy;
  }

  public void setVarianceBy(Double varianceBy) {
    this.varianceBy = varianceBy;
  }

  public String getVarianceType() {
    return varianceType;
  }

  public void setVarianceType(String varianceType) {
    this.varianceType = varianceType;
  }

  public Double getVarianceValue() {
    return varianceValue;
  }

  public void setVarianceValue(Double varianceValue) {
    this.varianceValue = varianceValue;
  }

  public String getChargeType() {
    return chargeType;
  }

  public void setChargeType(String chargeType) {
    this.chargeType = chargeType;
  }

  public String getPageNum() {
    return pageNum;
  }

  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  public String getActiveTheatres() {
    return activeTheatres;
  }

  public void setActiveTheatres(String activeTheatres) {
    this.activeTheatres = activeTheatres;
  }

  public String getInactiveTheatres() {
    return inactiveTheatres;
  }

  public void setInactiveTheatres(String inactiveTheatres) {
    this.inactiveTheatres = inactiveTheatres;
  }

  public String getAllTheatres() {
    return allTheatres;
  }

  public void setAllTheatres(String allTheatres) {
    this.allTheatres = allTheatres;
  }

  public Double[] getDailyCharge() {
    return dailyCharge;
  }

  public void setDailyCharge(Double[] dailyCharge) {
    this.dailyCharge = dailyCharge;
  }

  public Double[] getIncrCharge() {
    return incrCharge;
  }

  public void setIncrCharge(Double[] incrCharge) {
    this.incrCharge = incrCharge;
  }

  public Double[] getMinCharge() {
    return minCharge;
  }

  public void setMinCharge(Double[] minCharge) {
    this.minCharge = minCharge;
  }

  public String[] getBedTypes() {
    return bedTypes;
  }

  public void setBedTypes(String[] bedTypes) {
    this.bedTypes = bedTypes;
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

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
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

  public Double[] getDailyChargeDiscount() {
    return dailyChargeDiscount;
  }

  public void setDailyChargeDiscount(Double[] dailyChargeDiscount) {
    this.dailyChargeDiscount = dailyChargeDiscount;
  }

  public Double[] getIncrChargeDiscount() {
    return incrChargeDiscount;
  }

  public void setIncrChargeDiscount(Double[] incrChargeDiscount) {
    this.incrChargeDiscount = incrChargeDiscount;
  }

  public Double[] getMinChargeDiscount() {
    return minChargeDiscount;
  }

  public void setMinChargeDiscount(Double[] minChargeDiscount) {
    this.minChargeDiscount = minChargeDiscount;
  }

  public String getUpdateTable() {
    return updateTable;
  }

  public void setUpdateTable(String updateTable) {
    this.updateTable = updateTable;
  }

  public int getCenterId() {
    return centerId;
  }

  public void setCenterId(int centerId) {
    this.centerId = centerId;
  }

  public Integer getOverbook_limit() {
    return overbook_limit;
  }

  public void setOverbook_limit(Integer overbook_limit) {
    this.overbook_limit = overbook_limit;
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

  public Double[] getSlab1Charge() {
    return slab1Charge;
  }

  public void setSlab1Charge(Double[] slab1Charge) {
    this.slab1Charge = slab1Charge;
  }

  public Double[] getSlab1ChargeDiscount() {
    return slab1ChargeDiscount;
  }

  public void setSlab1ChargeDiscount(Double[] slab1ChargeDiscount) {
    this.slab1ChargeDiscount = slab1ChargeDiscount;
  }

  public int getStoreId() {
    return storeId;
  }

  public void setStoreId(int storeId) {
    this.storeId = storeId;
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
