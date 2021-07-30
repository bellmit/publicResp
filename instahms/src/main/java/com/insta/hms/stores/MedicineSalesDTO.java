package com.insta.hms.stores;

/*
 * DTO to store values of single medicine sold. Reflects the
 * contents of store_sales_details table.
 */

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicineSalesDTO {
  private int saleItemId;
  private String saleId;
  private String medicineId;
  private String batchNo;
  private BigDecimal quantity;
  private BigDecimal taxPer;
  private BigDecimal rate;
  private BigDecimal origRate;
  private BigDecimal tax;
  private BigDecimal amount; // final amount
  private BigDecimal packageUnit;
  private Date expiryDate;
  private BigDecimal medDiscRS;
  private BigDecimal medDisc;
  private String medDiscType;
  private String basis;
  private BigDecimal mrp;
  private BigDecimal cp;
  private int itemBatchId;
  private BigDecimal costValue;
  private BigDecimal secclaimAmt;
  private String secpreAuthId;
  private String secpreAuthModeId;
  private ArrayList<BigDecimal> claimAmts;
  private ArrayList<String> priorAuthIds;
  private ArrayList<Integer> priorAuthMode;
  private ArrayList<Map<String, Object>> taxMap;
  private String erxActivityId;
  private Boolean itemExcludedFromDoctor;
  private String itemExcludedFromDoctorRemarks;

  /*
   * Used only for transferring to charges in addition to sales detail
   */
  private String medicineName;
  private String manufacturer;

  // New columns due to insurance
  private String claimStatus;
  private BigDecimal insuranceClaimAmt;
  private BigDecimal priminsuranceClaimAmt;
  private BigDecimal secinsuranceClaimAmt;
  private BigDecimal claimRecdAmt;
  private String denialCode;
  private String itemCode;
  private String codeType;
  private int insuranceCategoryId;
  private String billingGroupId;
  private String saleUnit;
  // New column for zero claim
  private boolean allowZeroClaim = false;

  // New columns for insurance (returns)
  private BigDecimal returnInsuranceClaimAmt;
  private BigDecimal returnAmt;
  private BigDecimal returnQty;

  private String preAuthId;
  private Integer preAuthModeId;

  // New columns for prescription managment
  private BigDecimal duration;
  private String duration_unit;
  private String frequency;
  private String dosage;
  private String dosage_unit;
  private BigDecimal route_of_admin;
  private String doctor_remarks;
  private String special_instr;
  private String sales_remarks;
  private String label_id;
  private String warning_label;
  private ArrayList<Boolean> include_in_claim_calc;
  private ArrayList<BigDecimal> claimTaxAmt;
  private BigDecimal sponsorTaxAmt;
  private BigDecimal orgTaxAmt;
  private List<Integer> soldItemsIds = new ArrayList<Integer>();

  public List<Integer> getSoldItemsIds() {
    return soldItemsIds;
  }
  public void setSoldItemsIds(List<Integer> soldItemsIds) {
    this.soldItemsIds = soldItemsIds;
  }
  public BigDecimal getOrgTaxAmt() {
    return orgTaxAmt;
  }
  public void setOrgTaxAmt(BigDecimal orgTaxAmt) {
    this.orgTaxAmt = orgTaxAmt;
  }
  public String getDoctor_remarks() {
    return doctor_remarks;
  }
  public void setDoctor_remarks(String doctor_remarks) {
    this.doctor_remarks = doctor_remarks;
  }
  public String getDosage() {
    return dosage;
  }
  public void setDosage(String dosage) {
    this.dosage = dosage;
  }
  public String getDosage_unit() {
    return dosage_unit;
  }
  public void setDosage_unit(String dosage_unit) {
    this.dosage_unit = dosage_unit;
  }
  public String getFrequency() {
    return frequency;
  }
  public void setFrequency(String frequency) {
    this.frequency = frequency;
  }
  public String getSales_remarks() {
    return sales_remarks;
  }
  public void setSales_remarks(String sales_remarks) {
    this.sales_remarks = sales_remarks;
  }
  public Integer getPreAuthModeId() {
    return preAuthModeId;
  }
  public void setPreAuthModeId(Integer preAuthModeId) {
    this.preAuthModeId = preAuthModeId;
  }
  public String getPreAuthId() {
    return preAuthId;
  }
  public void setPreAuthId(String preAuthId) {
    this.preAuthId = preAuthId;
  }
  public String getSaleUnit() {
    return saleUnit;
  }
  public void setSaleUnit(String saleUnit) {
    this.saleUnit = saleUnit;
  }
  public int getInsuranceCategoryId() {
    return insuranceCategoryId;
  }
  public void setInsuranceCategoryId(int v) {
    insuranceCategoryId = v;
  }

  public String getCodeType() {
    return codeType;
  }
  public void setCodeType(String codeType) {
    this.codeType = codeType;
  }
  public String getItemCode() {
    return itemCode;
  }
  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }
  public int getSaleItemId() {
    return saleItemId;
  }
  public void setSaleItemId(int v) {
    saleItemId = v;
  }

  public String getSaleId() {
    return saleId;
  }
  public void setSaleId(String v) {
    saleId = v;
  }

  public String getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String v) {
    medicineId = v;
  }

  public String getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String v) {
    batchNo = v;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }
  public void setQuantity(BigDecimal v) {
    quantity = v;
  }

  public BigDecimal getTax() {
    return tax;
  }
  public void setTax(BigDecimal v) {
    tax = v;
  }

  public String getMedicineName() {
    return medicineName;
  }
  public void setMedicineName(String v) {
    medicineName = v;
  }

  public String getManufacturer() {
    return manufacturer;
  }
  public void setManufacturer(String v) {
    manufacturer = v;
  }

  public BigDecimal getRate() {
    return rate;
  }
  public void setRate(BigDecimal v) {
    rate = v;
  }

  public BigDecimal getOrigRate() {
    return origRate;
  }
  public void setOrigRate(BigDecimal v) {
    origRate = v;
  }

  public BigDecimal getAmount() {
    return amount;
  }
  public void setAmount(BigDecimal v) {
    amount = v;
  }

  public BigDecimal getTaxPer() {
    return taxPer;
  }
  public void setTaxPer(BigDecimal v) {
    taxPer = v;
  }

  public BigDecimal getPackageUnit() {
    return packageUnit;
  }
  public void setPackageUnit(BigDecimal v) {
    packageUnit = v;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }
  public void setExpiryDate(Date v) {
    expiryDate = v;
  }

  public BigDecimal getMedDiscRS() {
    return medDiscRS;
  }

  public void setMedDiscRS(BigDecimal medDiscRS) {
    this.medDiscRS = medDiscRS;
  }

  public BigDecimal getMedDisc() {
    return medDisc;
  }
  public void setMedDisc(BigDecimal v) {
    medDisc = v;
  }

  public String getMedDiscType() {
    return medDiscType;
  }
  public void setMedDiscType(String v) {
    medDiscType = v;
  }

  public String getBasis() {
    return basis;
  }
  public void setBasis(String v) {
    basis = v;
  }

  public BigDecimal getCp() {
    return cp;
  }
  public void setCp(BigDecimal cp) {
    this.cp = cp;
  }
  public BigDecimal getMrp() {
    return mrp;
  }
  public void setMrp(BigDecimal mrp) {
    this.mrp = mrp;
  }
  public BigDecimal getClaimRecdAmt() {
    return claimRecdAmt;
  }
  public void setClaimRecdAmt(BigDecimal claimRecdAmt) {
    this.claimRecdAmt = claimRecdAmt;
  }
  public String getClaimStatus() {
    return claimStatus;
  }
  public void setClaimStatus(String claimStatus) {
    this.claimStatus = claimStatus;
  }
  public String getDenialCode() {
    return denialCode;
  }
  public void setDenialCode(String denialCode) {
    this.denialCode = denialCode;
  }
  public BigDecimal getInsuranceClaimAmt() {
    return insuranceClaimAmt;
  }
  public void setInsuranceClaimAmt(BigDecimal insuranceClaimAmt) {
    this.insuranceClaimAmt = insuranceClaimAmt;
  }
  public BigDecimal getReturnAmt() {
    return returnAmt;
  }
  public void setReturnAmt(BigDecimal returnAmt) {
    this.returnAmt = returnAmt;
  }
  public BigDecimal getReturnInsuranceClaimAmt() {
    return returnInsuranceClaimAmt;
  }
  public void setReturnInsuranceClaimAmt(BigDecimal returnInsuranceClaimAmt) {
    this.returnInsuranceClaimAmt = returnInsuranceClaimAmt;
  }
  public BigDecimal getReturnQty() {
    return returnQty;
  }
  public void setReturnQty(BigDecimal returnQty) {
    this.returnQty = returnQty;
  }
  public int getItemBatchId() {
    return itemBatchId;
  }
  public void setItemBatchId(int itemBatchId) {
    this.itemBatchId = itemBatchId;
  }
  public BigDecimal getCostValue() {
    return costValue;
  }
  public void setCostValue(BigDecimal costValue) {
    this.costValue = costValue;
  }
  public BigDecimal getSecclaimAmt() {
    return secclaimAmt;
  }
  public void setSecclaimAmt(BigDecimal secclaimAmt) {
    this.secclaimAmt = secclaimAmt;
  }
  public String getSecpreAuthId() {
    return secpreAuthId;
  }
  public void setSecpreAuthId(String secpreAuthId) {
    this.secpreAuthId = secpreAuthId;
  }
  public String getSecpreAuthModeId() {
    return secpreAuthModeId;
  }
  public void setSecpreAuthModeId(String secpreAuthModeId) {
    this.secpreAuthModeId = secpreAuthModeId;
  }
  public ArrayList<BigDecimal> getClaimAmts() {
    return claimAmts;
  }
  public void setClaimAmts(ArrayList<BigDecimal> claimAmts) {
    this.claimAmts = claimAmts;
  }
  public ArrayList<String> getPriorAuthIds() {
    return priorAuthIds;
  }
  public void setPriorAuthIds(ArrayList<String> priorAuthIds) {
    this.priorAuthIds = priorAuthIds;
  }
  public ArrayList<Integer> getPriorAuthMode() {
    return priorAuthMode;
  }
  public void setPriorAuthMode(ArrayList<Integer> priorAuthMode) {
    this.priorAuthMode = priorAuthMode;
  }
  public String getLabel_id() {
    return label_id;
  }
  public void setLabel_id(String label_id) {
    this.label_id = label_id;
  }
  public BigDecimal getRoute_of_admin() {
    return route_of_admin;
  }
  public void setRoute_of_admin(BigDecimal route_of_admin) {
    this.route_of_admin = route_of_admin;
  }
  public String getWarning_label() {
    return warning_label;
  }
  public void setWarning_label(String warning_label) {
    this.warning_label = warning_label;
  }
  public BigDecimal getDuration() {
    return duration;
  }
  public void setDuration(BigDecimal duration) {
    this.duration = duration;
  }
  public String getDuration_unit() {
    return duration_unit;
  }
  public void setDuration_unit(String duration_unit) {
    this.duration_unit = duration_unit;
  }
  public String getSpecial_instr() {
    return special_instr;
  }
  public void setSpecial_instr(String special_instr) {
    this.special_instr = special_instr;
  }
  public ArrayList<Boolean> getInclude_in_claim_calc() {
    return include_in_claim_calc;
  }
  public void setInclude_in_claim_calc(ArrayList<Boolean> include_in_claim_calc) {
    this.include_in_claim_calc = include_in_claim_calc;
  }
  public ArrayList<Map<String, Object>> getTaxMap() {
    return taxMap;
  }
  public void setTaxMap(ArrayList<Map<String, Object>> taxMap) {
    this.taxMap = taxMap;
  }
  public ArrayList<BigDecimal> getClaimTaxAmt() {
    return claimTaxAmt;
  }
  public void setClaimTaxAmt(ArrayList<BigDecimal> claimTaxAmt) {
    this.claimTaxAmt = claimTaxAmt;
  }
  public BigDecimal getSponsorTaxAmt() {
    return sponsorTaxAmt;
  }
  public void setSponsorTaxAmt(BigDecimal sponsorTaxAmt) {
    this.sponsorTaxAmt = sponsorTaxAmt;
  }
  public boolean isAllowZeroClaim() {
    return allowZeroClaim;
  }

  public void setAllowZeroClaim(boolean allowZeroClaim) {
    this.allowZeroClaim = allowZeroClaim;
  }
  public String getErxActivityId() {
    return erxActivityId;
  }
  public void setErxActivityId(String erxActivityId) {
    this.erxActivityId = erxActivityId;
  }
  public String getBillingGroupId() {
    return billingGroupId;
  }
  public void setBillingGroupId(String billingGroupId) {
    this.billingGroupId = billingGroupId;
  }

  public Boolean getItemExcludedFromDoctor() {
    return itemExcludedFromDoctor;
  }

  public void setItemExcludedFromDoctor(Boolean itemExcludedFromDoctor) {
    this.itemExcludedFromDoctor = itemExcludedFromDoctor;
  }

  public String getItemExcludedFromDoctorRemarks() {
    return itemExcludedFromDoctorRemarks;
  }

  public void setItemExcludedFromDoctorRemarks(String itemExcludedFromDoctorRemarks) {
    this.itemExcludedFromDoctorRemarks = itemExcludedFromDoctorRemarks;
  }
  public BigDecimal getPriminsuranceClaimAmt() {
    return priminsuranceClaimAmt;
  }
  public void setPriminsuranceClaimAmt(BigDecimal priminsuranceClaimAmt) {
    this.priminsuranceClaimAmt = priminsuranceClaimAmt;
  }
  public BigDecimal getSecinsuranceClaimAmt() {
    return secinsuranceClaimAmt;
  }
  public void setSecinsuranceClaimAmt(BigDecimal secinsuranceClaimAmt) {
    this.secinsuranceClaimAmt = secinsuranceClaimAmt;
  }
}
