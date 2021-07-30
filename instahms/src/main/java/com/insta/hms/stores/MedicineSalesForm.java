package com.insta.hms.stores;

import org.apache.struts.action.ActionForm;

import java.math.BigDecimal;

public class MedicineSalesForm extends ActionForm {
  private static final long serialVersionUID = 1L;

  private String salesType;
  private String returnType;
  private String counterId;

  private String payDate;
  private String payTime;
  private String visitId;
  private String visitType;
  private String returnPatientType;
  private String custName;
  private String custDoctorName;
  private String searchMrno;
  private boolean salesReturn;
  private String phStore;
  private boolean existingCustomer;
  private String saleBasis;
  private String patientDoctor;

  private String custRetailCreditName; // Credit Retail customer Name
  private String custRetailCreditDocName; // Credit Retail Doctor Name

  private String planId; // plan id incase of Insurance Patient
  private String isTpa; // whether patient has tpa
  private String[] secIncludeInClaim;
  private BigDecimal[] secclaimAmt;
  private String[] secpreAuthId;
  private Integer[] secpreAuthModeId;
  private BigDecimal[] primclaimAmt;
  private String[] priIncludeInClaim;
  private String[] primpreAuthId;
  private Integer[] primpreAuthModeId;
  private String[] erxActivityId;

  private String[] medicineId;
  private String[] batchNo;
  private String[] expiry;
  private String[] pkgmrp;
  private String[] pkgcp;
  private String[] origRate;
  private String[] qty;
  private String[] pkgUnit;
  private String[] tax;
  private String[] taxPer;
  private String[] amt;
  private String[] medDiscRS;
  private String[] medDisc;
  private String[] medDiscType;
  private String[] patCalcAmt;
  private String[] insuranceCategoryId;
  private String[] billingGroupId;

  private String[] consultId;
  private String[] medName;
  private String[] dispensedMedicine;
  private String[] consultationId;
  private String[] medPrescribedId;
  private String[] issueUnits;
  private String[] preAuthId;
  private Integer[] preAuthModeId;
  private int[] itemBatchId;
  // consultaion and mang specific fields
  private String[] duration;
  private String[] durationUnit;
  private String[] frequency;
  private String[] dosage;
  private String[] dosageUnit;
  private String[] routeOfAdmin;
  private String[] doctorRemarks;
  private String[] special_instr;
  private String[] salesRemarks;
  private String[] label_id;
  private String[] warningLabel;

  private String total;
  private String billType;
  private int paymentModeId;
  private int cardTypeId;
  private String paymentBank;
  private String paymentRefNum;
  private String paymentRemarks;
  private String totAmt;
  private String totalTax;
  private String creditBillNo;
  private String disAmt;
  private String roundOffAmt;
  private String disRemark;
  private String custRCreditPhoneNo;
  private String custRCreditLimit;
  private String custRetailSponsor;
  private String rbillNo;
  private String disPer;
  boolean estimate;
  private String depositsetoff;
  private String dispenseStatus;
  private String allUserRemarks;

  private String[] indent_item_id;
  private String[] patient_indent_no;
  private String[] patientIndentNoRef;

  private int rewardPointsRedeemed;
  private BigDecimal rewardPointsRedeemedAmount;

  private String[] priClaimTaxAmt;
  private String[] secClaimTaxAmt;

  private String[] dischargeId;

  private String[] medicationId;

  private String pbm_presc_id;
  private String discountPlanId;
  private String discountCategory;

  private String nationalityId;
  private String governmentIdentifier;
  private Integer identifierId;
  private String rNationalityId;
  private Integer rIdentifierId;
  private String retailPatientMobileNo;

  public String getRetailPatientMobileNo() {
    return retailPatientMobileNo;
  }
  public void setRetailPatientMobileNo(String retailPatientMobileNo) {
    this.retailPatientMobileNo = retailPatientMobileNo;
  }
  public String getrNationalityId() {
    return rNationalityId;
  }
  public void setrNationalityId(String rNationalityId) {
    this.rNationalityId = rNationalityId;
  }
  public Integer getrIdentifierId() {
    return rIdentifierId;
  }
  public void setrIdentifierId(Integer rIdentifierId) {
    this.rIdentifierId = rIdentifierId;
  }
  public String getNationalityId() {
    return nationalityId;
  }
  public void setNationalityId(String nationalityId) {
    this.nationalityId = nationalityId;
  }
  public String getGovernmentIdentifier() {
    return governmentIdentifier;
  }
  public void setGovernmentIdentifier(String governmentIdentifier) {
    this.governmentIdentifier = governmentIdentifier;
  }
  public Integer getIdentifierId() {
    return identifierId;
  }
  public void setIdentifierId(Integer identifierId) {
    this.identifierId = identifierId;
  }
  private String[] orgTaxAmt;

  public String[] getOrgTaxAmt() {
    return orgTaxAmt;
  }
  public void setOrgTaxAmt(String[] orgTaxAmt) {
    this.orgTaxAmt = orgTaxAmt;
  }
  public String[] getDischargeId() {
    return dischargeId;
  }
  public void setDischargeId(String[] dischargeId) {
    this.dischargeId = dischargeId;
  }
  public String getPbm_presc_id() {
    return pbm_presc_id;
  }
  public void setPbm_presc_id(String pbm_presc_id) {
    this.pbm_presc_id = pbm_presc_id;
  }
  public Integer[] getPreAuthModeId() {
    return preAuthModeId;
  }
  public void setPreAuthModeId(Integer[] preAuthModeId) {
    this.preAuthModeId = preAuthModeId;
  }
  public boolean getEstimate() {
    return estimate;
  }
  public void setEstimate(boolean estimate) {
    this.estimate = estimate;
  }
  public String getSalesType() {
    return salesType;
  }
  public void setSalesType(String v) {
    salesType = v;
  }

  public String getReturnType() {
    return returnType;
  }
  public void setReturnType(String v) {
    returnType = v;
  }

  public String getCounterId() {
    return counterId;
  }
  public void setCounterId(String counterId) {
    this.counterId = counterId;
  }

  public String getPayDate() {
    return payDate;
  }
  public void setPayDate(String payDate) {
    this.payDate = payDate;
  }
  public String getPayTime() {
    return payTime;
  }
  public void setPayTime(String payTime) {
    this.payTime = payTime;
  }
  public String getVisitType() {
    return visitType;
  }
  public void setVisitType(String v) {
    visitType = v;
  }

  public String getReturnPatientType() {
    return returnPatientType;
  }
  public void setReturnPatientType(String v) {
    returnPatientType = v;
  }

  public String getVisitId() {
    return visitId;
  }
  public void setVisitId(String v) {
    visitId = v;
  }

  public String getCustName() {
    return custName;
  }
  public void setCustName(String v) {
    custName = v;
  }

  public String getCustDoctorName() {
    return custDoctorName;
  }
  public void setCustDoctorName(String v) {
    custDoctorName = v;
  }

  public String[] getMedicineId() {
    return medicineId;
  }
  public void setMedicineId(String[] v) {
    medicineId = v;
  }

  public String[] getBatchNo() {
    return batchNo;
  }
  public void setBatchNo(String[] v) {
    batchNo = v;
  }

  public String[] getExpiry() {
    return expiry;
  }
  public void setExpiry(String[] v) {
    expiry = v;
  }

  public String[] getPkgmrp() {
    return pkgmrp;
  }
  public void setPkgmrp(String[] v) {
    pkgmrp = v;
  }

  public String[] getPkgcp() {
    return pkgcp;
  }
  public void setPkgcp(String[] v) {
    pkgcp = v;
  }

  public String[] getOrigRate() {
    return origRate;
  }
  public void setOrigRate(String[] v) {
    origRate = v;
  }

  public String[] getQty() {
    return qty;
  }
  public void setQty(String[] v) {
    qty = v;
  }

  public String[] getPkgUnit() {
    return pkgUnit;
  }
  public void setPkgUnit(String[] v) {
    pkgUnit = v;
  }

  public String[] getTaxPer() {
    return taxPer;
  }
  public void setTaxPer(String[] v) {
    taxPer = v;
  }

  public String[] getTax() {
    return tax;
  }
  public void setTax(String[] v) {
    tax = v;
  }

  public String[] getAmt() {
    return amt;
  }
  public void setAmt(String[] v) {
    amt = v;
  }

  public String getTotal() {
    return total;
  }
  public void setTotal(String v) {
    total = v;
  }

  public boolean getSalesReturn() {
    return salesReturn;
  }
  public void setSalesReturn(boolean v) {
    this.salesReturn = v;
  }

  public String getBillType() {
    return billType;
  }
  public void setBillType(String v) {
    billType = v;
  }

  public int getPaymentModeId() {
    return paymentModeId;
  }
  public void setPaymentModeId(int v) {
    paymentModeId = v;
  }

  public int getCardTypeId() {
    return cardTypeId;
  }
  public void setCardTypeId(int v) {
    cardTypeId = v;
  }

  public String getPaymentBank() {
    return paymentBank;
  }
  public void setPaymentBank(String v) {
    paymentBank = v;
  }

  public String getPaymentRefNum() {
    return paymentRefNum;
  }
  public void setPaymentRefNum(String v) {
    paymentRefNum = v;
  }

  public String getPaymentRemarks() {
    return paymentRemarks;
  }
  public void setPaymentRemarks(String v) {
    paymentRemarks = v;
  }

  public String getSearchMrno() {
    return searchMrno;
  }
  public void setSearchMrno(String v) {
    this.searchMrno = v;
  }

  public String getTotAmt() {
    return totAmt;
  }
  public void setTotAmt(String v) {
    this.totAmt = v;
  }

  public String getCreditBillNo() {
    return creditBillNo;
  }
  public void setCreditBillNo(String v) {
    this.creditBillNo = v;
  }

  public String getTotalTax() {
    return totalTax;
  }
  public void setTotalTax(String v) {
    this.totalTax = v;
  }

  public String getPhStore() {
    return phStore;
  }
  public void setPhStore(String v) {
    this.phStore = v;
  }

  public String getDisAmt() {
    return disAmt;
  }
  public void setDisAmt(String v) {
    this.disAmt = v;
  }

  public String getDisRemark() {
    return disRemark;
  }
  public void setDisRemark(String v) {
    this.disRemark = v;
  }

  public boolean getExistingCustomer() {
    return existingCustomer;
  }
  public void setExistingCustomer(boolean v) {
    existingCustomer = v;
  }

  public String getCustRetailCreditDocName() {
    return custRetailCreditDocName;
  }
  public void setCustRetailCreditDocName(String v) {
    this.custRetailCreditDocName = v;
  }

  public String getCustRetailCreditName() {
    return custRetailCreditName;
  }
  public void setCustRetailCreditName(String v) {
    this.custRetailCreditName = v;
  }

  public String getCustRCreditLimit() {
    return custRCreditLimit;
  }
  public void setCustRCreditLimit(String v) {
    this.custRCreditLimit = v;
  }

  public String getCustRCreditPhoneNo() {
    return custRCreditPhoneNo;
  }
  public void setCustRCreditPhoneNo(String v) {
    this.custRCreditPhoneNo = v;
  }

  public String getCustRetailSponsor() {
    return custRetailSponsor;
  }
  public void setCustRetailSponsor(String v) {
    custRetailSponsor = v;
  }

  public String getRbillNo() {
    return rbillNo;
  }
  public void setRbillNo(String v) {
    this.rbillNo = v;
  }

  public String getRoundOffAmt() {
    return roundOffAmt;
  }
  public void setRoundOffAmt(String v) {
    roundOffAmt = v;
  }

  public String[] getMedDiscRS() {
    return medDiscRS;
  }
  public void setMedDiscRS(String[] v) {
    medDiscRS = v;
  }

  public String[] getMedDisc() {
    return medDisc;
  }
  public void setMedDisc(String[] v) {
    medDisc = v;
  }

  public String[] getMedDiscType() {
    return medDiscType;
  }
  public void setMedDiscType(String[] v) {
    medDiscType = v;
  }

  public String[] getPatCalcAmt() {
    return patCalcAmt;
  }
  public void setPatCalcAmt(String[] v) {
    patCalcAmt = v;
  }

  public String[] getInsuranceCategoryId() {
    return insuranceCategoryId;
  }
  public void setInsuranceCategoryId(String[] v) {
    insuranceCategoryId = v;
  }

  public String getSaleBasis() {
    return saleBasis;
  }
  public void setSaleBasis(String v) {
    saleBasis = v;
  }

  public String getDisPer() {
    return disPer;
  }
  public void setDisPer(String v) {
    disPer = v;
  }
  public String[] getConsultId() {
    return consultId;
  }
  public void setConsultId(String[] consultId) {
    this.consultId = consultId;
  }
  public String[] getMedName() {
    return medName;
  }
  public void setMedName(String[] medName) {
    this.medName = medName;
  }
  public String[] getConsultationId() {
    return consultationId;
  }
  public void setConsultationId(String[] consultationId) {
    this.consultationId = consultationId;
  }
  public String[] getDispensedMedicine() {
    return dispensedMedicine;
  }
  public void setDispensedMedicine(String[] dispensedMedicine) {
    this.dispensedMedicine = dispensedMedicine;
  }
  public String getPatientDoctor() {
    return patientDoctor;
  }
  public void setPatientDoctor(String patientDoctor) {
    this.patientDoctor = patientDoctor;
  }

  public String[] getMedPrescribedId() {
    return medPrescribedId;
  }
  public void setMedPrescribedId(String[] medPrescribedId) {
    this.medPrescribedId = medPrescribedId;
  }
  public String getDepositsetoff() {
    return depositsetoff;
  }
  public void setDepositsetoff(String depositsetoff) {
    this.depositsetoff = depositsetoff;
  }
  public String getDispenseStatus() {
    return dispenseStatus;
  }
  public void setDispenseStatus(String dispenseStatus) {
    this.dispenseStatus = dispenseStatus;
  }

  public String getAllUserRemarks() {
    return allUserRemarks;
  }
  public void setAllUserRemarks(String allUserRemarks) {
    this.allUserRemarks = allUserRemarks;
  }
  public String getPlanId() {
    return planId;
  }
  public void setPlanId(String planId) {
    this.planId = planId;
  }
  public String getIsTpa() {
    return isTpa;
  }
  public void setIsTpa(String isTpa) {
    this.isTpa = isTpa;
  }
  public String[] getIssueUnits() {
    return issueUnits;
  }
  public void setIssueUnits(String[] issueUnits) {
    this.issueUnits = issueUnits;
  }

  public String[] getPreAuthId() {
    return preAuthId;
  }
  public void setPreAuthId(String[] preAuthId) {
    this.preAuthId = preAuthId;
  }
  public int getRewardPointsRedeemed() {
    return rewardPointsRedeemed;
  }
  public void setRewardPointsRedeemed(int rewardPointsRedeemed) {
    this.rewardPointsRedeemed = rewardPointsRedeemed;
  }
  public BigDecimal getRewardPointsRedeemedAmount() {
    return rewardPointsRedeemedAmount;
  }
  public void setRewardPointsRedeemedAmount(BigDecimal rewardPointsRedeemedAmount) {
    this.rewardPointsRedeemedAmount = rewardPointsRedeemedAmount;
  }
  public String[] getPatientIndentNoRef() {
    return patientIndentNoRef;
  }
  public void setPatientIndentNoRef(String[] patientIndentNoRef) {
    this.patientIndentNoRef = patientIndentNoRef;
  }
  public int[] getItemBatchId() {
    return itemBatchId;
  }
  public void setItemBatchId(int[] itemBatchId) {
    this.itemBatchId = itemBatchId;
  }
  public BigDecimal[] getSecclaimAmt() {
    return secclaimAmt;
  }
  public void setSecclaimAmt(BigDecimal[] secclaimAmt) {
    this.secclaimAmt = secclaimAmt;
  }
  public String[] getSecpreAuthId() {
    return secpreAuthId;
  }
  public void setSecpreAuthId(String[] secpreAuthId) {
    this.secpreAuthId = secpreAuthId;
  }
  public Integer[] getSecpreAuthModeId() {
    return secpreAuthModeId;
  }
  public void setSecpreAuthModeId(Integer[] secpreAuthModeId) {
    this.secpreAuthModeId = secpreAuthModeId;
  }
  public BigDecimal[] getPrimclaimAmt() {
    return primclaimAmt;
  }
  public void setPrimclaimAmt(BigDecimal[] primclaimAmt) {
    this.primclaimAmt = primclaimAmt;
  }
  public String[] getPrimpreAuthId() {
    return primpreAuthId;
  }
  public void setPrimpreAuthId(String[] primpreAuthId) {
    this.primpreAuthId = primpreAuthId;
  }
  public Integer[] getPrimpreAuthModeId() {
    return primpreAuthModeId;
  }
  public void setPrimpreAuthModeId(Integer[] primpreAuthModeId) {
    this.primpreAuthModeId = primpreAuthModeId;
  }
  public String[] getFrequency() {
    return frequency;
  }
  public void setFrequency(String[] frequency) {
    this.frequency = frequency;
  }

  public String[] getDoctorRemarks() {
    return doctorRemarks;
  }
  public void setDoctorRemarks(String[] doctorRemarks) {
    this.doctorRemarks = doctorRemarks;
  }
  public String[] getDosageUnit() {
    return dosageUnit;
  }
  public void setDosageUnit(String[] dosageUnit) {
    this.dosageUnit = dosageUnit;
  }
  public String[] getSalesRemarks() {
    return salesRemarks;
  }
  public void setSalesRemarks(String[] salesRemarks) {
    this.salesRemarks = salesRemarks;
  }
  public String[] getWarningLabel() {
    return warningLabel;
  }
  public void setWarningLabel(String[] warningLabel) {
    this.warningLabel = warningLabel;
  }
  public String[] getDosage() {
    return dosage;
  }
  public void setDosage(String[] dosage) {
    this.dosage = dosage;
  }

  public String[] getLabel_id() {
    return label_id;
  }
  public void setLabel_id(String[] label_id) {
    this.label_id = label_id;
  }

  public String[] getRouteOfAdmin() {
    return routeOfAdmin;
  }
  public void setRouteOfAdmin(String[] routeOfAdmin) {
    this.routeOfAdmin = routeOfAdmin;
  }
  public String[] getDuration() {
    return duration;
  }
  public void setDuration(String[] duration) {
    this.duration = duration;
  }
  public String[] getDurationUnit() {
    return durationUnit;
  }
  public void setDurationUnit(String[] durationUnit) {
    this.durationUnit = durationUnit;
  }
  public String[] getSpecial_instr() {
    return special_instr;
  }
  public void setSpecial_instr(String[] special_instr) {
    this.special_instr = special_instr;
  }
  public String[] getSecIncludeInClaim() {
    return secIncludeInClaim;
  }
  public void setSecIncludeInClaim(String[] secIncludeInClaim) {
    this.secIncludeInClaim = secIncludeInClaim;
  }
  public String[] getPriIncludeInClaim() {
    return priIncludeInClaim;
  }
  public void setPriIncludeInClaim(String[] priIncludeInClaim) {
    this.priIncludeInClaim = priIncludeInClaim;
  }
  public String[] getMedicationId() {
    return medicationId;
  }
  public void setMedicationId(String[] medicationId) {
    this.medicationId = medicationId;
  }
  public String[] getPriClaimTaxAmt() {
    return priClaimTaxAmt;
  }
  public void setPriClaimTaxAmt(String[] priClaimTaxAmt) {
    this.priClaimTaxAmt = priClaimTaxAmt;
  }
  public String[] getSecClaimTaxAmt() {
    return secClaimTaxAmt;
  }
  public void setSecClaimTaxAmt(String[] secClaimTaxAmt) {
    this.secClaimTaxAmt = secClaimTaxAmt;
  }
  public String[] getErxActivityId() {
    return erxActivityId;
  }
  public void setErxActivityId(String[] erxActivityIds) {
    this.erxActivityId = erxActivityIds;
  }
  public String[] getBillingGroupId() {
    return billingGroupId;
  }
  public void setBillingGroupId(String[] billingGroupId) {
    this.billingGroupId = billingGroupId;
  }
  public String getDiscountPlanId() {
    return discountPlanId;
  }
  public void setDiscountPlanId(String discountPlanId) {
    this.discountPlanId = discountPlanId;
  }
  public String getDiscountCategory() {
    return discountCategory;
  }
  public void setDiscountCategory(String discountCategory) {
    this.discountCategory = discountCategory;
  }
}
