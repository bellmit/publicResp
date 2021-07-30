package com.insta.hms.adminmasters.bedmaster;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class BedDetails.
 */
public class BedDetails implements Serializable {

  // this represents both bed_details,icu_bed_charges

  // for normal beds both bedType and baseBed have same value

  /** The bed type. */
  private String bedType;
  
  /** The org id. */
  private String orgId;
  
  /** The bed status. */
  private String bedStatus;
  
  /** The display order. */
  private int displayOrder;
  
  /** The bill bed type. */
  private String billBedType;
  
  /** The is Icu. */
  private String isIcu;
  
  /** The allow zero claim amount. */
  private String allowZeroClaimAmount;
  
  /** The billing group id. */
  private Integer billingGroupId;

  /** The base bed. */
  private String baseBed;
  
  /** The bed charge. */
  private Double bedCharge;
  
  /** The nursing charge. */
  private Double nursingCharge;
  
  /** The duty charge. */
  private Double dutyCharge;
  
  /** The prof charge. */
  private Double profCharge;
  
  /** The hourly charge. */
  private Double hourlyCharge;
  
  /** The luxary charge. */
  private Double luxaryCharge;
  
  /** The intial charge. */
  private Double intialCharge;
  
  /** The bed charge discount. */
  private Double bedChargeDiscount;
  
  /** The nursing charge discount. */
  private Double nursingChargeDiscount;
  
  /** The duty charge discount. */
  private Double dutyChargeDiscount;
  
  /** The prof charge discount. */
  private Double profChargeDiscount;
  
  /** The hourly charge discount. */
  private Double hourlyChargeDiscount;
  
  /** The daycare slab 1 charge. */
  private Double daycareSlab1Charge;
  
  /** The daycare slab 2 charge. */
  private Double daycareSlab2Charge;
  
  /** The daycare slab 3 charge. */
  private Double daycareSlab3Charge;
  
  /** The daycare slab 1 charge discount. */
  private Double daycareSlab1ChargeDiscount;
  
  /** The daycare slab 2 charge discount. */
  private Double daycareSlab2ChargeDiscount;
  
  /** The daycare slab 3 charge discount. */
  private Double daycareSlab3ChargeDiscount;
  
  /** The code type. */
  private String codeType;
  
  /** The org item code. */
  private String orgItemCode;

  /**
   * Gets the org item code.
   *
   * @return the org item code
   */
  public String getOrgItemCode() {
    return orgItemCode;
  }

  /**
   * Sets the org item code.
   *
   * @param orgItemCode the new org item code
   */
  public void setOrgItemCode(String orgItemCode) {
    this.orgItemCode = orgItemCode;
  }

  /**
   * Gets the code type.
   *
   * @return the code type
   */
  public String getCodeType() {
    return codeType;
  }

  /**
   * Sets the code type.
   *
   * @param codType the new code type
   */
  public void setCodeType(String codType) {
    codeType = codType;
  }

  /**
   * Gets the allow zero claim amount.
   *
   * @return the allow zero claim amount
   */
  public String getAllowZeroClaimAmount() {
    return allowZeroClaimAmount;
  }

  /**
   * Sets the allow zero claim amount.
   *
   * @param allowZeroClaimAmount the new allow zero claim amount
   */
  public void setAllowZeroClaimAmount(String allowZeroClaimAmount) {
    this.allowZeroClaimAmount = allowZeroClaimAmount;
  }

  /**
   * Gets the billing group id.
   *
   * @return the billing group id
   */
  public Integer getBillingGroupId() {
    return billingGroupId;
  }

  /**
   * Sets the billing group id.
   *
   * @param billingGroupId the new billing group id
   */
  public void setBillingGroupId(Integer billingGroupId) {
    this.billingGroupId = billingGroupId;
  }

  /**
   * Gets the base bed.
   *
   * @return the base bed
   */
  public String getBaseBed() {
    return baseBed;
  }

  /**
   * Sets the base bed.
   *
   * @param baseBed the new base bed
   */
  public void setBaseBed(String baseBed) {
    this.baseBed = baseBed;
  }

  /**
   * Gets the bed charge.
   *
   * @return the bed charge
   */
  public Double getBedCharge() {
    return bedCharge;
  }

  /**
   * Sets the bed charge.
   *
   * @param bedCharge the new bed charge
   */
  public void setBedCharge(Double bedCharge) {
    this.bedCharge = bedCharge;
  }

  /**
   * Gets the bed status.
   *
   * @return the bed status
   */
  public String getBedStatus() {
    return bedStatus;
  }

  /**
   * Sets the bed status.
   *
   * @param bedStatus the new bed status
   */
  public void setBedStatus(String bedStatus) {
    this.bedStatus = bedStatus;
  }

  /**
   * Gets the bed type.
   *
   * @return the bed type
   */
  public String getBedType() {
    return bedType;
  }

  /**
   * Sets the bed type.
   *
   * @param bedType the new bed type
   */
  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  /**
   * Gets the duty charge.
   *
   * @return the duty charge
   */
  public Double getDutyCharge() {
    return dutyCharge;
  }

  /**
   * Sets the duty charge.
   *
   * @param dutyCharge the new duty charge
   */
  public void setDutyCharge(Double dutyCharge) {
    this.dutyCharge = dutyCharge;
  }

  /**
   * Gets the hourly charge.
   *
   * @return the hourly charge
   */
  public Double getHourlyCharge() {
    return hourlyCharge;
  }

  /**
   * Sets the hourly charge.
   *
   * @param hourlyCharge the new hourly charge
   */
  public void setHourlyCharge(Double hourlyCharge) {
    this.hourlyCharge = hourlyCharge;
  }

  /**
   * Gets the intial charge.
   *
   * @return the intial charge
   */
  public Double getIntialCharge() {
    return intialCharge;
  }

  /**
   * Sets the intial charge.
   *
   * @param intialCharge the new intial charge
   */
  public void setIntialCharge(Double intialCharge) {
    this.intialCharge = intialCharge;
  }

  /**
   * Gets the luxary charge.
   *
   * @return the luxary charge
   */
  public Double getLuxaryCharge() {
    return luxaryCharge;
  }

  /**
   * Sets the luxary charge.
   *
   * @param luxaryCharge the new luxary charge
   */
  public void setLuxaryCharge(Double luxaryCharge) {
    this.luxaryCharge = luxaryCharge;
  }

  /**
   * Gets the nursing charge.
   *
   * @return the nursing charge
   */
  public Double getNursingCharge() {
    return nursingCharge;
  }

  /**
   * Sets the nursing charge.
   *
   * @param nursingCharge the new nursing charge
   */
  public void setNursingCharge(Double nursingCharge) {
    this.nursingCharge = nursingCharge;
  }

  /**
   * Gets the org id.
   *
   * @return the org id
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId the new org id
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the prof charge.
   *
   * @return the prof charge
   */
  public Double getProfCharge() {
    return profCharge;
  }

  /**
   * Sets the prof charge.
   *
   * @param profCharge the new prof charge
   */
  public void setProfCharge(Double profCharge) {
    this.profCharge = profCharge;
  }

  /**
   * Gets the bed charge discount.
   *
   * @return the bed charge discount
   */
  public Double getBedChargeDiscount() {
    return bedChargeDiscount;
  }

  /**
   * Sets the bed charge discount.
   *
   * @param bedChargeDiscount the new bed charge discount
   */
  public void setBedChargeDiscount(Double bedChargeDiscount) {
    this.bedChargeDiscount = bedChargeDiscount;
  }

  /**
   * Gets the duty charge discount.
   *
   * @return the duty charge discount
   */
  public Double getDutyChargeDiscount() {
    return dutyChargeDiscount;
  }

  /**
   * Sets the duty charge discount.
   *
   * @param dutyChargeDiscount the new duty charge discount
   */
  public void setDutyChargeDiscount(Double dutyChargeDiscount) {
    this.dutyChargeDiscount = dutyChargeDiscount;
  }

  /**
   * Gets the hourly charge discount.
   *
   * @return the hourly charge discount
   */
  public Double getHourlyChargeDiscount() {
    return hourlyChargeDiscount;
  }

  /**
   * Sets the hourly charge discount.
   *
   * @param hourlyChargeDiscount the new hourly charge discount
   */
  public void setHourlyChargeDiscount(Double hourlyChargeDiscount) {
    this.hourlyChargeDiscount = hourlyChargeDiscount;
  }

  /**
   * Gets the nursing charge discount.
   *
   * @return the nursing charge discount
   */
  public Double getNursingChargeDiscount() {
    return nursingChargeDiscount;
  }

  /**
   * Sets the nursing charge discount.
   *
   * @param nursingChargeDiscount the new nursing charge discount
   */
  public void setNursingChargeDiscount(Double nursingChargeDiscount) {
    this.nursingChargeDiscount = nursingChargeDiscount;
  }

  /**
   * Gets the prof charge discount.
   *
   * @return the prof charge discount
   */
  public Double getProfChargeDiscount() {
    return profChargeDiscount;
  }

  /**
   * Sets the prof charge discount.
   *
   * @param profChargeDiscount the new prof charge discount
   */
  public void setProfChargeDiscount(Double profChargeDiscount) {
    this.profChargeDiscount = profChargeDiscount;
  }

  /**
   * Gets the bill bed type.
   *
   * @return the bill bed type
   */
  public String getBillBedType() {
    return billBedType;
  }

  /**
   * Sets the bill bed type.
   *
   * @param billBedType the new bill bed type
   */
  public void setBillBedType(String billBedType) {
    this.billBedType = billBedType;
  }

  /**
   * Gets the display order.
   *
   * @return the display order
   */
  public int getDisplayOrder() {
    return displayOrder;
  }

  /**
   * Sets the display order.
   *
   * @param displayOrder the new display order
   */
  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  /**
   * Gets the checks if is ICU.
   *
   * @return the checks if is ICU
   */
  public String getIsIcu() {
    return isIcu;
  }

  /**
   * Sets the checks if is ICU.
   *
   * @param isIcu the new checks if is ICU
   */
  public void setIsIcu(String isIcu) {
    this.isIcu = isIcu;
  }

  /**
   * Gets the daycare slab 1 charge.
   *
   * @return the daycare slab 1 charge
   */
  public Double getDaycareSlab1Charge() {
    return daycareSlab1Charge;
  }

  /**
   * Sets the daycare slab 1 charge.
   *
   * @param daycareSlab1Charge the new daycare slab 1 charge
   */
  public void setDaycareSlab1Charge(Double daycareSlab1Charge) {
    this.daycareSlab1Charge = daycareSlab1Charge;
  }

  /**
   * Gets the daycare slab 1 charge discount.
   *
   * @return the daycare slab 1 charge discount
   */
  public Double getDaycareSlab1ChargeDiscount() {
    return daycareSlab1ChargeDiscount;
  }

  /**
   * Sets the daycare slab 1 charge discount.
   *
   * @param daycareSlab1ChargeDiscount the new daycare slab 1 charge discount
   */
  public void setDaycareSlab1ChargeDiscount(Double daycareSlab1ChargeDiscount) {
    this.daycareSlab1ChargeDiscount = daycareSlab1ChargeDiscount;
  }

  /**
   * Gets the daycare slab 2 charge.
   *
   * @return the daycare slab 2 charge
   */
  public Double getDaycareSlab2Charge() {
    return daycareSlab2Charge;
  }

  /**
   * Sets the daycare slab 2 charge.
   *
   * @param daycareSlab2Charge the new daycare slab 2 charge
   */
  public void setDaycareSlab2Charge(Double daycareSlab2Charge) {
    this.daycareSlab2Charge = daycareSlab2Charge;
  }

  /**
   * Gets the daycare slab 2 charge discount.
   *
   * @return the daycare slab 2 charge discount
   */
  public Double getDaycareSlab2ChargeDiscount() {
    return daycareSlab2ChargeDiscount;
  }

  /**
   * Sets the daycare slab 2 charge discount.
   *
   * @param daycareSlab2ChargeDiscount the new daycare slab 2 charge discount
   */
  public void setDaycareSlab2ChargeDiscount(Double daycareSlab2ChargeDiscount) {
    this.daycareSlab2ChargeDiscount = daycareSlab2ChargeDiscount;
  }

  /**
   * Gets the daycare slab 3 charge.
   *
   * @return the daycare slab 3 charge
   */
  public Double getDaycareSlab3Charge() {
    return daycareSlab3Charge;
  }

  /**
   * Sets the daycare slab 3 charge.
   *
   * @param daycareSlab3Charge the new daycare slab 3 charge
   */
  public void setDaycareSlab3Charge(Double daycareSlab3Charge) {
    this.daycareSlab3Charge = daycareSlab3Charge;
  }

  /**
   * Gets the daycare slab 3 charge discount.
   *
   * @return the daycare slab 3 charge discount
   */
  public Double getDaycareSlab3ChargeDiscount() {
    return daycareSlab3ChargeDiscount;
  }

  /**
   * Sets the daycare slab 3 charge discount.
   *
   * @param daycareSlab3ChargeDiscount the new daycare slab 3 charge discount
   */
  public void setDaycareSlab3ChargeDiscount(Double daycareSlab3ChargeDiscount) {
    this.daycareSlab3ChargeDiscount = daycareSlab3ChargeDiscount;
  }

}
