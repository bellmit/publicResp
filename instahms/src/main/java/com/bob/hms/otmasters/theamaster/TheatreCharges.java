package com.bob.hms.otmasters.theamaster;

import java.math.BigDecimal;

public class TheatreCharges {

  private String theatreId;
  private String orgId;
  private String bedType;
  private BigDecimal dailyCharge;
  private BigDecimal minCharge;
  private BigDecimal slab1Charge;
  private BigDecimal incrCharge;
  private BigDecimal dailyChargeDiscount;
  private BigDecimal minChargeDiscount;
  private BigDecimal slab1ChargeDiscount;
  private BigDecimal incrChargeDiscount;

  public String getBedType() {
    return bedType;
  }

  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  public BigDecimal getDailyCharge() {
    return dailyCharge;
  }

  public void setDailyCharge(BigDecimal dailyCharge) {
    this.dailyCharge = dailyCharge;
  }

  public BigDecimal getIncrCharge() {
    return incrCharge;
  }

  public void setIncrCharge(BigDecimal incrCharge) {
    this.incrCharge = incrCharge;
  }

  public BigDecimal getMinCharge() {
    return minCharge;
  }

  public void setMinCharge(BigDecimal minCharge) {
    this.minCharge = minCharge;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getTheatreId() {
    return theatreId;
  }

  public void setTheatreId(String theatreId) {
    this.theatreId = theatreId;
  }

  public BigDecimal getDailyChargeDiscount() {
    return dailyChargeDiscount;
  }

  public void setDailyChargeDiscount(BigDecimal dailyChargeDiscount) {
    this.dailyChargeDiscount = dailyChargeDiscount;
  }

  public BigDecimal getIncrChargeDiscount() {
    return incrChargeDiscount;
  }

  public void setIncrChargeDiscount(BigDecimal incrChargeDiscount) {
    this.incrChargeDiscount = incrChargeDiscount;
  }

  public BigDecimal getMinChargeDiscount() {
    return minChargeDiscount;
  }

  public void setMinChargeDiscount(BigDecimal minChargeDiscount) {
    this.minChargeDiscount = minChargeDiscount;
  }

  public BigDecimal getSlab1Charge() {
    return slab1Charge;
  }

  public void setSlab1Charge(BigDecimal slab1Charge) {
    this.slab1Charge = slab1Charge;
  }

  public BigDecimal getSlab1ChargeDiscount() {
    return slab1ChargeDiscount;
  }

  public void setSlab1ChargeDiscount(BigDecimal slab1ChargeDiscount) {
    this.slab1ChargeDiscount = slab1ChargeDiscount;
  }

}
