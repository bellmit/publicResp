package com.insta.hms.stores;

import com.insta.hms.billing.ChargeDTO;

/*
 * DTO to store values of one sale (set of medicines) sold. Reflects the
 * contents of store_sales_main table.
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MedicineSalesMainDTO {

  private String saleId;
  private String storeId;
  private String type;
  private String billNo;
  private java.sql.Timestamp saleDate; /* date to be printed in bills */
  private java.sql.Timestamp dateTime; /* actual date/time when the transaction was recorded */
  private String username;
  private BigDecimal discount;
  private String discountRemarks;
  private String rBillNo;
  private BigDecimal roundOffPaise;
  private String change_source;
  private BigDecimal discountPer;
  private String doctor;
  private String sponserName;
  private BigDecimal totalItemAmount;
  private BigDecimal totalItemDiscount;
  private BigDecimal totalItemTax;
  private String userRemarks;
  private String saleUnit;
  private String wardNo;
  private Object storeRatePlanId;
  private String chargeId;
  private String preAuthId;
  private String erxReferenceNo;
  private Boolean isExternalPbm;
  private Integer discountPlan;

  /*
   * Extra fields used to get the sale counter
   */

  private String counter;

  /*
   * An optional list of MedicineSales items contained
   */
  private ArrayList<MedicineSalesDTO> saleItems;

  /*
   * List of sale items against which returns are done.
   */

  private ArrayList<MedicineSalesDTO> saleItemsForReturns;

  /*
   * List of sale id charges to be updated while return.
   */

  private List<ChargeDTO> saleIdChargesToUpdate;

  /*
   * Constants
   */
  public static final String TYPE_SALE = "S";
  public static final String TYPE_SALES_RETURN = "R";

  /*
   * Accessors
   */
  public String getSaleId() {
    return saleId;
  }
  public void setSaleId(String v) {
    saleId = v;
  }

  public String getStoreId() {
    return storeId;
  }
  public void setStoreId(String v) {
    storeId = v;
  }

  public String getType() {
    return type;
  }
  public void setType(String v) {
    type = v;
  }

  public String getBillNo() {
    return billNo;
  }
  public void setBillNo(String v) {
    billNo = v;
  }

  public java.sql.Timestamp getSaleDate() {
    return saleDate;
  }
  public void setSaleDate(java.sql.Timestamp v) {
    saleDate = v;
  }

  public java.sql.Timestamp getDateTime() {
    return dateTime;
  }
  public void setDateTime(java.sql.Timestamp v) {
    dateTime = v;
  }

  public String getUsername() {
    return username;
  }
  public void setUsername(String v) {
    username = v;
  }

  public ArrayList<MedicineSalesDTO> getSaleItems() {
    return saleItems;
  }
  public void setSaleItems(ArrayList<MedicineSalesDTO> v) {
    saleItems = v;
  }

  public BigDecimal getDiscount() {
    return discount;
  }
  public void setDiscount(BigDecimal v) {
    discount = v;
  }

  public String getDiscountRemarks() {
    return discountRemarks;
  }
  public void setDiscountRemarks(String v) {
    this.discountRemarks = v;
  }

  public String getRBillNo() {
    return rBillNo;
  }
  public void setRBillNo(String v) {
    rBillNo = v;
  }

  public BigDecimal getRoundOffPaise() {
    return roundOffPaise;
  }
  public void setRoundOffPaise(BigDecimal v) {
    this.roundOffPaise = v;
  }
  public String getChange_source() {
    return change_source;
  }
  public void setChange_source(String change_source) {
    this.change_source = change_source;
  }
  public BigDecimal getDiscountPer() {
    return discountPer;
  }
  public void setDiscountPer(BigDecimal discountPer) {
    this.discountPer = discountPer;
  }
  public String getDoctor() {
    return doctor;
  }
  public void setDoctor(String doctor) {
    this.doctor = doctor;
  }

  public String getSponserName() {
    return sponserName;
  }
  public void setSponserName(String v) {
    sponserName = v;
  }

  public BigDecimal getTotalItemAmount() {
    return totalItemAmount;
  }
  public void setTotalItemAmount(BigDecimal v) {
    totalItemAmount = v;
  }

  public BigDecimal getTotalItemDiscount() {
    return totalItemDiscount;
  }
  public void setTotalItemDiscount(BigDecimal v) {
    totalItemDiscount = v;
  }

  public BigDecimal getTotalItemTax() {
    return totalItemTax;
  }
  public void setTotalItemTax(BigDecimal v) {
    totalItemTax = v;
  }

  public String getUserRemarks() {
    return userRemarks;
  }
  public void setUserRemarks(String v) {
    userRemarks = v;
  }
  public String getSaleUnit() {
    return saleUnit;
  }
  public void setSaleUnit(String saleUnit) {
    this.saleUnit = saleUnit;
  }
  public ArrayList<MedicineSalesDTO> getSaleItemsForReturns() {
    return saleItemsForReturns;
  }
  public void setSaleItemsForReturns(ArrayList<MedicineSalesDTO> saleItemsForReturns) {
    this.saleItemsForReturns = saleItemsForReturns;
  }
  public List<ChargeDTO> getSaleIdChargesToUpdate() {
    return saleIdChargesToUpdate;
  }
  public void setSaleIdChargesToUpdate(List<ChargeDTO> saleIdChargesToUpdate) {
    this.saleIdChargesToUpdate = saleIdChargesToUpdate;
  }
  public String getCounter() {
    return counter;
  }
  public void setCounter(String counter) {
    this.counter = counter;
  }
  public String getWardNo() {
    return wardNo;
  }
  public void setWardNo(String wardNo) {
    this.wardNo = wardNo;
  }
  public Object getStoreRatePlanId() {
    return storeRatePlanId;
  }
  public void setStoreRatePlanId(Object storeRatePlanId) {
    this.storeRatePlanId = storeRatePlanId;
  }
  public String getChargeId() {
    return chargeId;
  }
  public void setChargeId(String chargeId) {
    this.chargeId = chargeId;
  }
  public String getPreAuthId() {
    return preAuthId;
  }
  public void setPreAuthId(String preAuthId) {
    this.preAuthId = preAuthId;
  }
  public String getErxReferenceNo() {
    return erxReferenceNo;
  }
  public void setErxReferenceNo(String erxReferenceNo) {
    this.erxReferenceNo = erxReferenceNo;
  }
  public Boolean getIsExternalPbm() {
    return isExternalPbm;
  }
  public void setIsExternalPbm(Boolean isExternalPbm) {
    this.isExternalPbm = isExternalPbm;
  }
  public Integer getDiscountPlan() {
    return discountPlan;
  }
  public void setDiscountPlan(Integer discountPlan) {
    this.discountPlan = discountPlan;
  }
}
