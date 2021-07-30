package com.insta.hms.adminmasters.bedmaster;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

// TODO: Auto-generated Javadoc
/**
 * The Class BedMasterForm.
 */
public class BedMasterForm extends ActionForm {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The charge head. */
  private String chargeHead;
  
  /** The org name. */
  private String orgName;
  
  /** The org id. */
  private String orgId;
  
  /** The page num. */
  private String pageNum;
  
  /** The bedtype. */
  private String bedtype;
  
  /** The status. */
  private String status;
  
  /** The variace type. */
  private String variaceType;
  
  /** The variance by. */
  private Double varianceBy;
  
  /** The variance value. */
  private Double varianceValue;

  /** The bed type details file. */
  private FormFile bedTypeDetailsFile;
  
  /** The upload bed charges file. */
  private FormFile uploadBedChargesFile;
  
  /** The upload ICU bed charges file. */
  private FormFile uploadIcuBedChargesFile;

  /** The base bed. */
  private String[] baseBed;
  
  /** The bed charge. */
  private Double[] bedCharge;
  
  /** The nursing charge. */
  private Double[] nursingCharge;
  
  /** The duty charge. */
  private Double[] dutyCharge;
  
  /** The prof charge. */
  private Double[] profCharge;
  
  /** The hourly charge. */
  private Double[] hourlyCharge;
  
  /** The luxary charge. */
  private Double[] luxaryCharge;
  
  /** The intial charge. */
  private Double[] intialCharge;

  /** The bed charge discount. */
  private Double[] bedChargeDiscount;
  
  /** The nursing charge discount. */
  private Double[] nursingChargeDiscount;
  
  /** The duty charge discount. */
  private Double[] dutyChargeDiscount;
  
  /** The prof charge discount. */
  private Double[] profChargeDiscount;
  
  /** The hourly charge discount. */
  private Double[] hourlyChargeDiscount;
  
  /** The daycare slab 1 charge. */
  private Double[] daycareSlab1Charge;
  
  /** The daycare slab 2 charge. */
  private Double[] daycareSlab2Charge;
  
  /** The daycare slab 3 charge. */
  private Double[] daycareSlab3Charge;
  
  /** The daycare slab 1 charge discount. */
  private Double[] daycareSlab1ChargeDiscount;
  
  /** The daycare slab 2 charge discount. */
  private Double[] daycareSlab2ChargeDiscount;
  
  /** The daycare slab 3 charge discount. */
  private Double[] daycareSlab3ChargeDiscount;

  /** The is ICU category. */
  private String isIcuCategory;
  
  /** The base bead for charges. */
  private String baseBeadForCharges;
  
  /** The group updat component. */
  private String groupUpdatComponent;
  
  /** The groupbed type. */
  private String[] groupbedType;
  
  /** The nearset roundof value. */
  private Double nearsetRoundofValue;
  
  /** The display order. */
  private int displayOrder;
  
  /** The bill bed type. */
  private String billBedType;
  
  /** The code type. */
  private String codeType;
  
  /** The org item code. */
  private String orgItemCode;
  
  /** The allow zero claim amount. */
  private String allowZeroClaimAmount;
  
  /** The billing group id. */
  private Integer billingGroupId;

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
   * @param v the new code type
   */
  public void setCodeType(String v) {
    codeType = v;
  }

  /**
   * Gets the nearset roundof value.
   *
   * @return the nearset roundof value
   */
  public Double getNearsetRoundofValue() {
    return nearsetRoundofValue;
  }

  /**
   * Sets the nearset roundof value.
   *
   * @param nearsetRoundofValue the new nearset roundof value
   */
  public void setNearsetRoundofValue(Double nearsetRoundofValue) {
    this.nearsetRoundofValue = nearsetRoundofValue;
  }

  /**
   * Gets the groupbed type.
   *
   * @return the groupbed type
   */
  public String[] getGroupbedType() {
    return groupbedType;
  }

  /**
   * Sets the groupbed type.
   *
   * @param groupbedType the new groupbed type
   */
  public void setGroupbedType(String[] groupbedType) {
    this.groupbedType = groupbedType;
  }

  /**
   * Gets the group updat component.
   *
   * @return the group updat component
   */
  public String getGroupUpdatComponent() {
    return groupUpdatComponent;
  }

  /**
   * Sets the group updat component.
   *
   * @param groupUpdatComponent the new group updat component
   */
  public void setGroupUpdatComponent(String groupUpdatComponent) {
    this.groupUpdatComponent = groupUpdatComponent;
  }

  /**
   * Gets the base bead for charges.
   *
   * @return the base bead for charges
   */
  public String getBaseBeadForCharges() {
    return baseBeadForCharges;
  }

  /**
   * Sets the base bead for charges.
   *
   * @param baseBeadForCharges the new base bead for charges
   */
  public void setBaseBeadForCharges(String baseBeadForCharges) {
    this.baseBeadForCharges = baseBeadForCharges;
  }

  /**
   * Gets the base bed.
   *
   * @return the base bed
   */
  public String[] getBaseBed() {
    return baseBed;
  }

  /**
   * Sets the base bed.
   *
   * @param baseBed the new base bed
   */
  public void setBaseBed(String[] baseBed) {
    this.baseBed = baseBed;
  }

  /**
   * Gets the bed charge.
   *
   * @return the bed charge
   */
  public Double[] getBedCharge() {
    return bedCharge;
  }

  /**
   * Sets the bed charge.
   *
   * @param bedCharge the new bed charge
   */
  public void setBedCharge(Double[] bedCharge) {
    this.bedCharge = bedCharge;
  }

  /**
   * Gets the duty charge.
   *
   * @return the duty charge
   */
  public Double[] getDutyCharge() {
    return dutyCharge;
  }

  /**
   * Sets the duty charge.
   *
   * @param dutyCharge the new duty charge
   */
  public void setDutyCharge(Double[] dutyCharge) {
    this.dutyCharge = dutyCharge;
  }

  /**
   * Gets the hourly charge.
   *
   * @return the hourly charge
   */
  public Double[] getHourlyCharge() {
    return hourlyCharge;
  }

  /**
   * Sets the hourly charge.
   *
   * @param hourlyCharge the new hourly charge
   */
  public void setHourlyCharge(Double[] hourlyCharge) {
    this.hourlyCharge = hourlyCharge;
  }

  /**
   * Gets the intial charge.
   *
   * @return the intial charge
   */
  public Double[] getIntialCharge() {
    return intialCharge;
  }

  /**
   * Sets the intial charge.
   *
   * @param intialCharge the new intial charge
   */
  public void setIntialCharge(Double[] intialCharge) {
    this.intialCharge = intialCharge;
  }

  /**
   * Gets the bed charge discount.
   *
   * @return the bed charge discount
   */
  public Double[] getBedChargeDiscount() {
    return bedChargeDiscount;
  }

  /**
   * Sets the bed charge discount.
   *
   * @param bedChargeDiscount the new bed charge discount
   */
  public void setBedChargeDiscount(Double[] bedChargeDiscount) {
    this.bedChargeDiscount = bedChargeDiscount;
  }

  /**
   * Gets the duty charge discount.
   *
   * @return the duty charge discount
   */
  public Double[] getDutyChargeDiscount() {
    return dutyChargeDiscount;
  }

  /**
   * Sets the duty charge discount.
   *
   * @param dutyChargeDiscount the new duty charge discount
   */
  public void setDutyChargeDiscount(Double[] dutyChargeDiscount) {
    this.dutyChargeDiscount = dutyChargeDiscount;
  }

  /**
   * Gets the hourly charge discount.
   *
   * @return the hourly charge discount
   */
  public Double[] getHourlyChargeDiscount() {
    return hourlyChargeDiscount;
  }

  /**
   * Sets the hourly charge discount.
   *
   * @param hourlyChargeDiscount the new hourly charge discount
   */
  public void setHourlyChargeDiscount(Double[] hourlyChargeDiscount) {
    this.hourlyChargeDiscount = hourlyChargeDiscount;
  }

  /**
   * Gets the nursing charge discount.
   *
   * @return the nursing charge discount
   */
  public Double[] getNursingChargeDiscount() {
    return nursingChargeDiscount;
  }

  /**
   * Sets the nursing charge discount.
   *
   * @param nursingChargeDiscount the new nursing charge discount
   */
  public void setNursingChargeDiscount(Double[] nursingChargeDiscount) {
    this.nursingChargeDiscount = nursingChargeDiscount;
  }

  /**
   * Gets the prof charge discount.
   *
   * @return the prof charge discount
   */
  public Double[] getProfChargeDiscount() {
    return profChargeDiscount;
  }

  /**
   * Sets the prof charge discount.
   *
   * @param profChargeDiscount the new prof charge discount
   */
  public void setProfChargeDiscount(Double[] profChargeDiscount) {
    this.profChargeDiscount = profChargeDiscount;
  }

  /**
   * Gets the luxary charge.
   *
   * @return the luxary charge
   */
  public Double[] getLuxaryCharge() {
    return luxaryCharge;
  }

  /**
   * Sets the luxary charge.
   *
   * @param luxaryCharge the new luxary charge
   */
  public void setLuxaryCharge(Double[] luxaryCharge) {
    this.luxaryCharge = luxaryCharge;
  }

  /**
   * Gets the nursing charge.
   *
   * @return the nursing charge
   */
  public Double[] getNursingCharge() {
    return nursingCharge;
  }

  /**
   * Sets the nursing charge.
   *
   * @param nursingCharge the new nursing charge
   */
  public void setNursingCharge(Double[] nursingCharge) {
    this.nursingCharge = nursingCharge;
  }

  /**
   * Gets the prof charge.
   *
   * @return the prof charge
   */
  public Double[] getProfCharge() {
    return profCharge;
  }

  /**
   * Sets the prof charge.
   *
   * @param profCharge the new prof charge
   */
  public void setProfCharge(Double[] profCharge) {
    this.profCharge = profCharge;
  }

  /**
   * Gets the variance by.
   *
   * @return the variance by
   */
  public Double getVarianceBy() {
    return varianceBy;
  }

  /**
   * Sets the variance by.
   *
   * @param varianceBy the new variance by
   */
  public void setVarianceBy(Double varianceBy) {
    this.varianceBy = varianceBy;
  }

  /**
   * Gets the variance value.
   *
   * @return the variance value
   */
  public Double getVarianceValue() {
    return varianceValue;
  }

  /**
   * Sets the variance value.
   *
   * @param varianceValue the new variance value
   */
  public void setVarianceValue(Double varianceValue) {
    this.varianceValue = varianceValue;
  }

  /**
   * Gets the variace type.
   *
   * @return the variace type
   */
  public String getVariaceType() {
    return variaceType;
  }

  /**
   * Sets the variace type.
   *
   * @param variaceType the new variace type
   */
  public void setVariaceType(String variaceType) {
    this.variaceType = variaceType;
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   *
   * @param status the new status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets the bedtype.
   *
   * @return the bedtype
   */
  public String getBedtype() {
    return bedtype;
  }

  /**
   * Sets the bedtype.
   *
   * @param bedtype the new bedtype
   */
  public void setBedtype(String bedtype) {
    this.bedtype = bedtype;
  }

  /**
   * Gets the page num.
   *
   * @return the page num
   */
  public String getPageNum() {
    return pageNum;
  }

  /**
   * Sets the page num.
   *
   * @param pageNum the new page num
   */
  public void setPageNum(String pageNum) {
    this.pageNum = pageNum;
  }

  /**
   * Gets the serial version UID.
   *
   * @return the serial version UID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
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
   * Gets the org name.
   *
   * @return the org name
   */
  public String getOrgName() {
    return orgName;
  }

  /**
   * Sets the org name.
   *
   * @param orgName the new org name
   */
  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  /**
   * Gets the charge head.
   *
   * @return the charge head
   */
  public String getChargeHead() {
    return chargeHead;
  }

  /**
   * Sets the charge head.
   *
   * @param chargeHead the new charge head
   */
  public void setChargeHead(String chargeHead) {
    this.chargeHead = chargeHead;
  }

  /* (non-Javadoc)
   * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {
    // TODO Auto-generated method stub

    super.reset(arg0, arg1);
    this.chargeHead = null;
    this.orgName = null;
    this.orgId = null;
    this.pageNum = null;
    this.bedtype = null;
    this.status = null;
    this.variaceType = null;
    this.varianceBy = null;
    this.varianceValue = null;
    this.baseBed = null;
    this.bedCharge = null;
    this.nursingCharge = null;
    this.dutyCharge = null;
    this.profCharge = null;
    this.hourlyCharge = null;
    this.luxaryCharge = null;
    this.intialCharge = null;
    this.bedChargeDiscount = null;
    this.nursingChargeDiscount = null;
    this.dutyChargeDiscount = null;
    this.profChargeDiscount = null;
    this.hourlyChargeDiscount = null;
    this.daycareSlab1Charge = null;
    this.daycareSlab2Charge = null;
    this.daycareSlab3Charge = null;
    this.daycareSlab1ChargeDiscount = null;
    this.daycareSlab2ChargeDiscount = null;
    this.daycareSlab3ChargeDiscount = null;
    this.isIcuCategory = null;
    this.groupbedType = null;
    this.groupUpdatComponent = null;
    this.codeType = null;
    this.orgItemCode = null;
    this.allowZeroClaimAmount = null;
  }

  /**
   * Gets the checks if is ICU category.
   *
   * @return the checks if is ICU category
   */
  public String getIsIcuCategory() {
    return isIcuCategory;
  }

  /**
   * Sets the checks if is ICU category.
   *
   * @param isIcuCategory the new checks if is ICU category
   */
  public void setIsIcuCategory(String isIcuCategory) {
    this.isIcuCategory = isIcuCategory;
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
   * Gets the daycare slab 1 charge.
   *
   * @return the daycare slab 1 charge
   */
  public Double[] getDaycareSlab1Charge() {
    return daycareSlab1Charge;
  }

  /**
   * Sets the daycare slab 1 charge.
   *
   * @param daycareSlab1Charge the new daycare slab 1 charge
   */
  public void setDaycareSlab1Charge(Double[] daycareSlab1Charge) {
    this.daycareSlab1Charge = daycareSlab1Charge;
  }

  /**
   * Gets the daycare slab 1 charge discount.
   *
   * @return the daycare slab 1 charge discount
   */
  public Double[] getDaycareSlab1ChargeDiscount() {
    return daycareSlab1ChargeDiscount;
  }

  /**
   * Sets the daycare slab 1 charge discount.
   *
   * @param daycareSlab1ChargeDiscount the new daycare slab 1 charge discount
   */
  public void setDaycareSlab1ChargeDiscount(Double[] daycareSlab1ChargeDiscount) {
    this.daycareSlab1ChargeDiscount = daycareSlab1ChargeDiscount;
  }

  /**
   * Gets the daycare slab 2 charge.
   *
   * @return the daycare slab 2 charge
   */
  public Double[] getDaycareSlab2Charge() {
    return daycareSlab2Charge;
  }

  /**
   * Sets the daycare slab 2 charge.
   *
   * @param daycareSlab2Charge the new daycare slab 2 charge
   */
  public void setDaycareSlab2Charge(Double[] daycareSlab2Charge) {
    this.daycareSlab2Charge = daycareSlab2Charge;
  }

  /**
   * Gets the daycare slab 2 charge discount.
   *
   * @return the daycare slab 2 charge discount
   */
  public Double[] getDaycareSlab2ChargeDiscount() {
    return daycareSlab2ChargeDiscount;
  }

  /**
   * Sets the daycare slab 2 charge discount.
   *
   * @param daycareSlab2ChargeDiscount the new daycare slab 2 charge discount
   */
  public void setDaycareSlab2ChargeDiscount(Double[] daycareSlab2ChargeDiscount) {
    this.daycareSlab2ChargeDiscount = daycareSlab2ChargeDiscount;
  }

  /**
   * Gets the daycare slab 3 charge.
   *
   * @return the daycare slab 3 charge
   */
  public Double[] getDaycareSlab3Charge() {
    return daycareSlab3Charge;
  }

  /**
   * Sets the daycare slab 3 charge.
   *
   * @param daycareSlab3Charge the new daycare slab 3 charge
   */
  public void setDaycareSlab3Charge(Double[] daycareSlab3Charge) {
    this.daycareSlab3Charge = daycareSlab3Charge;
  }

  /**
   * Gets the daycare slab 3 charge discount.
   *
   * @return the daycare slab 3 charge discount
   */
  public Double[] getDaycareSlab3ChargeDiscount() {
    return daycareSlab3ChargeDiscount;
  }

  /**
   * Sets the daycare slab 3 charge discount.
   *
   * @param daycareSlab3ChargeDiscount the new daycare slab 3 charge discount
   */
  public void setDaycareSlab3ChargeDiscount(Double[] daycareSlab3ChargeDiscount) {
    this.daycareSlab3ChargeDiscount = daycareSlab3ChargeDiscount;
  }

  /**
   * Gets the bed type details file.
   *
   * @return the bed type details file
   */
  public FormFile getbedTypeDetailsFile() {
    return bedTypeDetailsFile;
  }

  /**
   * Sets the bed type details file.
   *
   * @param uploadBedTypeDetailsFile the new bed type details file
   */
  public void setbedTypeDetailsFile(FormFile uploadBedTypeDetailsFile) {
    this.bedTypeDetailsFile = uploadBedTypeDetailsFile;
  }

  /**
   * Gets the upload bed charges file.
   *
   * @return the upload bed charges file
   */
  public FormFile getUploadBedChargesFile() {
    return uploadBedChargesFile;
  }

  /**
   * Sets the upload bed charges file.
   *
   * @param uploadBedChargesFile the new upload bed charges file
   */
  public void setUploadBedChargesFile(FormFile uploadBedChargesFile) {
    this.uploadBedChargesFile = uploadBedChargesFile;
  }

  /**
   * Gets the upload ICU bed charges file.
   *
   * @return the upload ICU bed charges file
   */
  public FormFile getUploadIcuBedChargesFile() {
    return uploadIcuBedChargesFile;
  }

  /**
   * Sets the upload ICU bed charges file.
   *
   * @param uploadIcuBedChargesFile the new upload ICU bed charges file
   */
  public void setUploadICUBedChargesFile(FormFile uploadIcuBedChargesFile) {
    this.uploadIcuBedChargesFile = uploadIcuBedChargesFile;
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

}
