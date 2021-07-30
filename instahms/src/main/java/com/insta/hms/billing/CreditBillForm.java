package com.insta.hms.billing;

import org.apache.struts.action.ActionForm;

import java.math.BigDecimal;

public class CreditBillForm extends ActionForm{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String mrNo;
	private String patientId;
	private String billNo;
	private String doctorId;
	private String visitId;

	private String billStatus;
	private String paymentStatus;
	private String okToDischarge;

	private String corpName;
	private String billRemarks;
	private String writeOffRemarks;
	private String spnrWriteOffRemarks;

	private int billDiscountAuth;
	private int billDiscountCategory;

	private String dischargeDate;
	private String dischargeTime;

	private String finalizedDate;
	private String finalizedTime;

	private String opendate;
	private String opentime;
	private String modTime;
	private String primaryClaimStatus;
	private String secondaryClaimStatus;
	private String cancelReason;
	private BigDecimal claimRecdAmount;

	private String secondarySponsorExists;

	private int procedure_no;
	private String primaryApprovalAmount;
	private String secondaryApprovalAmount;

	private String primaryTotalClaim;
	private String secondaryTotalClaim;
	private String insuranceDeduction;
	private String reopenReason;
	private int billLabelId;

	private Boolean[] edited;
	private String[] chargeId;
	private String[] postedDate;
	private String[] postedTime;
	private String[] descriptionId;
	private String[] description;
	private String[] remarks;
	private String[] userRemarks;
	private String[] itemRemarks;
	private String[] actItemCode;
	private String[] actRatePlanItemCode;
	private String[] codeType;
	private BigDecimal[] rate;
	private BigDecimal[] qty;
	private BigDecimal[] disc;
	private BigDecimal[] amt;

	private BigDecimal[] insClaimAmt;
	private BigDecimal[] priInsClaimAmt;
	private BigDecimal[] secInsClaimAmt;
	private BigDecimal[] docAmt;
	
	private BigDecimal[] priInsClaimTaxAmt;
	private BigDecimal[] secInsClaimTaxAmt;
	
	private String[] priIncludeInClaim;
	public String[] getPriIncludeInClaim() {
		return priIncludeInClaim;
	}
	public void setPriIncludeInClaim(String[] priIncludeInClaim) {
		this.priIncludeInClaim = priIncludeInClaim;
	}
	private String[] secIncludeInClaim;

	public String[] getSecIncludeInClaim() {
		return secIncludeInClaim;
	}
	public void setSecIncludeInClaim(String[] secIncludeInClaim) {
		this.secIncludeInClaim = secIncludeInClaim;
	}
	private boolean[] allowDiscount;
	private boolean[] allowRateVariation;

	private String[] units;
	private String[] header;
	private String[] chargeHeadId;
	private String[] chargeGroupId;
	private String[] departmentId;
	private String[] chargeRef;
	private String[] prescDocId;
	private String[] payeeDocId;
	private boolean[] delCharge;
	private BigDecimal[] originalRate;

	private String counterId;

	private String[] paymentType;
	private int[] currencyId;
	private BigDecimal[] currencyAmt;
	private BigDecimal[] exchangeRate;
	private String[] exchangeDateTime;

	private BigDecimal[] totPayingAmt;
	private int[] paymentModeId;
	private int[] cardTypeId;
	private String[] bankName;
	private String[] refNumber;
	private String[] paymentRemarks;
	private String payDate;
	private String payTime;

	// Card details
	private String[] bankBatchNo;
	private String[] cardAuthCode;
	private String[] cardHolderName;
	private String[] cardNumber;
	private String[] cardExpDate;

	private BigDecimal[] tdsAmt;
	private String[] paidBy;

	private BigDecimal depositSetOff;
	private BigDecimal ipDepositSetOff;
	private String depositType;

	private int rewardPointsRedeemed;
	private BigDecimal rewardPointsRedeemedAmount;

	private int[] discount_auth_dr;
	private BigDecimal[] dr_discount_amt;

	private int[] discount_auth_pres_dr;
	private BigDecimal[] pres_dr_discount_amt;

	private int[] discount_auth_ref;
	private BigDecimal[] ref_discount_amt;

	private int[] discount_auth_hosp;
	private BigDecimal[] hosp_discount_amt;

	private int[] overall_discount_auth;
	private BigDecimal[] overall_discount_amt;
	private int[] service_sub_group_id;

	private String[] conducting_doc_mandatory;
	private int[] consultation_type_id;
	private String[] op_id;

	private String[] from_date;
	private String[] to_date;
	private int dynaPkgId;
	private BigDecimal dynaPkgCharge;
	private String dynaPkgProcessed;

	private String[] chargeExcluded;
	private String[] packageFinalized;
	private Integer[] insuranceCategoryId;
	private Boolean[] firstOfCategory;
	private String[] preAuthId;
	private String[] secPreAuthId;
	private Integer[] preAuthModeId;
	private Integer[] secPreAuthModeId;
	private boolean[] allowRateDecrease;
	private boolean[] allowRateIncrease;
	private int[] redeemed_points;

	private BigDecimal[] amount_included;
	private BigDecimal[] qty_included;

	private String per_diem_code;

	private Boolean[] isClaimLocked;
	private String[] pricreditNote;
	private String[] secreditNote;
	private String[] isEdited;
	
	private String[] isSystemDiscount;
	private String[] dynaPackageExcluded;
	private String activityConducted;
	
	public String getActivityConducted() {
    return activityConducted;
  }
  public void setActivityConducted(String activityConducted) {
    this.activityConducted = activityConducted;
  }
  public String[] getIsSystemDiscount() {
    return isSystemDiscount;
    }
	
	public void setIsSystemDiscount(String[] isSystemDiscount) {
    this.isSystemDiscount = isSystemDiscount;
    }

	public boolean[] getAllowRateDecrease() {
		return allowRateDecrease;
	}
	public void setAllowRateDecrease(boolean[] allowRateDecrease) {
		this.allowRateDecrease = allowRateDecrease;
	}
	public boolean[] getAllowRateIncrease() {
		return allowRateIncrease;
	}
	public void setAllowRateIncrease(boolean[] allowRateIncrease) {
		this.allowRateIncrease = allowRateIncrease;
	}
	public Integer[] getPreAuthModeId() {
		return preAuthModeId;
	}
	public void setPreAuthModeId(Integer[] preAuthModeId) {
		this.preAuthModeId = preAuthModeId;
	}
	public String[] getPreAuthId() {
		return preAuthId;
	}
	public void setPreAuthId(String[] preAuthId) {
		this.preAuthId = preAuthId;
	}
	public Integer[] getInsuranceCategoryId() {
		return insuranceCategoryId;
	}
	public void setInsuranceCategoryId(Integer[] insuranceCategoryId) {
		this.insuranceCategoryId = insuranceCategoryId;
	}
	public Boolean[] getFirstOfCategory() {
		return firstOfCategory;
	}
	public void setFirstOfCategory(Boolean[] firstOfCategory) {
		this.firstOfCategory = firstOfCategory;
	}
	public int[] getService_sub_group_id() {
		return service_sub_group_id;
	}
	public void setService_sub_group_id(int[] service_sub_group_id) {
		this.service_sub_group_id = service_sub_group_id;
	}
	public String[] getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType[]) {
		this.paymentType = paymentType;
	}
	public BigDecimal[] getAmt() {
		return amt;
	}
	public void setAmt(BigDecimal[] amt) {
		this.amt = amt;
	}
	public String getBillStatus() {
		return billStatus;
	}
	public void setBillStatus(String billStatus) {
		this.billStatus = billStatus;
	}

	public String getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(String v) { paymentStatus = v; }

	public String[] getChargeId() {
		return chargeId;
	}
	public void setChargeId(String[] chargeId) {
		this.chargeId = chargeId;
	}

	public Boolean[] getEdited() { return edited; }
	public void setEdited(Boolean[] v) { edited = v; }

	public String getCorpName() {
		return corpName;
	}
	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}
	public String getCounterId() {
		return counterId;
	}
	public void setCounterId(String counterId) {
		this.counterId = counterId;
	}

	public String[] getDescriptionId() { return descriptionId; }
	public void setDescriptionId(String[] v) { descriptionId = v; }

	public String[] getDescription() {
		return description;
	}
	public void setDescription(String[] description) {
		this.description = description;
	}
	public BigDecimal[] getDisc() {
		return disc;
	}
	public void setDisc(BigDecimal[] disc) {
		this.disc = disc;
	}
	public String[] getHeader() {
		return header;
	}
	public void setHeader(String[] header) {
		this.header = header;
	}
	public String getMrNo() {
		return mrNo;
	}
	public void setMrNo(String mrNo) {
		this.mrNo = mrNo;
	}
	public String getOkToDischarge() {
		return okToDischarge;
	}
	public void setOkToDischarge(String okToDischarge) {
		this.okToDischarge = okToDischarge;
	}
	public String getPatientId() {
		return patientId;
	}
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	public BigDecimal[] getQty() {
		return qty;
	}
	public void setQty(BigDecimal[] qty) {
		this.qty = qty;
	}
	public BigDecimal[] getRate() {
		return rate;
	}
	public void setRate(BigDecimal[] rate) {
		this.rate = rate;
	}
	public String[] getRemarks() {
		return remarks;
	}
	public void setRemarks(String[] remarks) {
		this.remarks = remarks;
	}
	public String[] getUserRemarks() { return userRemarks; }
	public void setUserRemarks(String[] v) { userRemarks = v; }

	public String[] getActItemCode() { return actItemCode; }
	public void setActItemCode(String[] v) { actItemCode = v; }

	public String[] getActRatePlanItemCode() { return actRatePlanItemCode; }
	public void setActRatePlanItemCode(String[] v) { actRatePlanItemCode = v; }

	public String[] getCodeType() { return codeType; }
	public void setCodeType(String[] v) { codeType = v; }

	public String[] getBankName() {
		return bankName;
	}
	public void setBankName(String[] bankName) {
		this.bankName = bankName;
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
	public int[] getPaymentModeId() {
		return paymentModeId;
	}
	public void setPaymentModeId(int[] paymentModeId) {
		this.paymentModeId = paymentModeId;
	}
	public String[] getPaymentRemarks() {
		return paymentRemarks;
	}
	public void setPaymentRemarks(String[] paymentRemarks) {
		this.paymentRemarks = paymentRemarks;
	}
	public String[] getRefNumber() {
		return refNumber;
	}
	public void setRefNumber(String[] refNumber) {
		this.refNumber = refNumber;
	}
	public BigDecimal[] getTotPayingAmt() {
		return totPayingAmt;
	}
	public void setTotPayingAmt(BigDecimal[] totPayingAmt) {
		this.totPayingAmt = totPayingAmt;
	}
	public String[] getUnits() {
		return units;
	}
	public void setUnits(String[] units) {
		this.units = units;
	}
	public String getBillNo() {
		return billNo;
	}
	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}
	public String[] getPostedDate() {
		return postedDate;
	}
	public void setPostedDate(String[] postedDate) {
		this.postedDate = postedDate;
	}
	public String[] getPostedTime() {
		return postedTime;
	}
	public void setPostedTime(String[] postedTime) {
		this.postedTime = postedTime;
	}
	public String[] getChargeGroupId() {
		return chargeGroupId;
	}
	public void setChargeGroupId(String[] v) {
		this.chargeGroupId = v;
	}
	public String[] getChargeHeadId() {
		return chargeHeadId;
	}
	public void setChargeHeadId(String[] chargeHeadId) {
		this.chargeHeadId = chargeHeadId;
	}
	public String[] getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(String[] departmentId) {
		this.departmentId = departmentId;
	}
	public String[] getChargeRef() {
		return chargeRef;
	}
	public void setChargeRef(String[] chargeRef) {
		this.chargeRef = chargeRef;
	}
	public boolean[] getDelCharge() {
		return delCharge;
	}
	public void setDelCharge(boolean[] v) {
		this.delCharge = v;
	}
	public BigDecimal[] getOriginalRate() {
		return originalRate;
	}
	public void setOriginalRate(BigDecimal[] originalRate) {
		this.originalRate = originalRate;
	}
	public String getBillRemarks() {
		return billRemarks;
	}
	public void setBillRemarks(String billRemarks) {
		this.billRemarks = billRemarks;
	}
	public BigDecimal[] getDocAmt() {
		return docAmt;
	}
	public void setDocAmt(BigDecimal[] docAmt) {
		this.docAmt = docAmt;
	}
	public String getDoctorId() {
		return doctorId;
	}
	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}
	public String getVisitId() {
		return visitId;
	}
	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}
	public String getDischargeTime() {
		return dischargeTime;
	}
	public void setDischargeTime(String dischargeTime) {
		this.dischargeTime = dischargeTime;
	}
	public String getDischargeDate() {
		return dischargeDate;
	}
	public void setDischargeDate(String dischargeDate) {
		this.dischargeDate = dischargeDate;
	}
	public String getFinalizedDate() {
		return finalizedDate;
	}
	public void setFinalizedDate(String finalizedDate) {
		this.finalizedDate = finalizedDate;
	}
	public String getFinalizedTime() {
		return finalizedTime;
	}
	public void setFinalizedTime(String finalizedTime) {
		this.finalizedTime = finalizedTime;
	}

	public String[] getPayeeDocId() { return payeeDocId; }
	public void setPayeeDocId(String[] v) { payeeDocId = v; }

	public String[] getPrescDocId() { return prescDocId; }
	public void setPrescDocId(String[] v) { prescDocId = v; }

	public int getBillDiscountAuth() {
		return billDiscountAuth;
	}
	public void setBillDiscountAuth(int billDiscountAuth) {
		this.billDiscountAuth = billDiscountAuth;
	}
	public BigDecimal[] getInsClaimAmt() {
		return insClaimAmt;
	}
	public void setInsClaimAmt(BigDecimal[] insClaimAmt) {
		this.insClaimAmt = insClaimAmt;
	}

	public String getCancelReason() { return cancelReason; }
	public void setCancelReason(String v) { cancelReason = v; }

	public BigDecimal getClaimRecdAmount() { return claimRecdAmount; }
	public void setClaimRecdAmount(BigDecimal v) { claimRecdAmount = v; }
	public BigDecimal getDepositSetOff() {
		return depositSetOff;
	}
	public void setDepositSetOff(BigDecimal depositSetOff) {
		this.depositSetOff = depositSetOff;
	}
	public int[] getDiscount_auth_dr() {
		return discount_auth_dr;
	}
	public void setDiscount_auth_dr(int[] discount_auth_dr) {
		this.discount_auth_dr = discount_auth_dr;
	}
	public int[] getDiscount_auth_hosp() {
		return discount_auth_hosp;
	}
	public void setDiscount_auth_hosp(int[] discount_auth_hosp) {
		this.discount_auth_hosp = discount_auth_hosp;
	}
	public int[] getDiscount_auth_pres_dr() {
		return discount_auth_pres_dr;
	}
	public void setDiscount_auth_pres_dr(int[] discount_auth_pres_dr) {
		this.discount_auth_pres_dr = discount_auth_pres_dr;
	}
	public int[] getDiscount_auth_ref() {
		return discount_auth_ref;
	}
	public void setDiscount_auth_ref(int[] discount_auth_ref) {
		this.discount_auth_ref = discount_auth_ref;
	}
	public BigDecimal[] getDr_discount_amt() {
		return dr_discount_amt;
	}
	public void setDr_discount_amt(BigDecimal[] dr_discount_amt) {
		this.dr_discount_amt = dr_discount_amt;
	}
	public BigDecimal[] getHosp_discount_amt() {
		return hosp_discount_amt;
	}
	public void setHosp_discount_amt(BigDecimal[] hosp_discount_amt) {
		this.hosp_discount_amt = hosp_discount_amt;
	}
	public BigDecimal[] getOverall_discount_amt() {
		return overall_discount_amt;
	}
	public void setOverall_discount_amt(BigDecimal[] overall_discount_amt) {
		this.overall_discount_amt = overall_discount_amt;
	}
	public int[] getOverall_discount_auth() {
		return overall_discount_auth;
	}
	public void setOverall_discount_auth(int[] overall_discount_auth) {
		this.overall_discount_auth = overall_discount_auth;
	}
	public BigDecimal[] getPres_dr_discount_amt() {
		return pres_dr_discount_amt;
	}
	public void setPres_dr_discount_amt(BigDecimal[] pres_dr_discount_amt) {
		this.pres_dr_discount_amt = pres_dr_discount_amt;
	}
	public BigDecimal[] getRef_discount_amt() {
		return ref_discount_amt;
	}
	public void setRef_discount_amt(BigDecimal[] ref_discount_amt) {
		this.ref_discount_amt = ref_discount_amt;
	}
	public int getProcedure_no() {
		return procedure_no;
	}
	public void setProcedure_no(int procedure_no) {
		this.procedure_no = procedure_no;
	}
	public String[] getPaidBy() {
		return paidBy;
	}
	public void setPaidBy(String[] paidBy) {
		this.paidBy = paidBy;
	}
	public BigDecimal[] getTdsAmt() {
		return tdsAmt;
	}
	public void setTdsAmt(BigDecimal[] tdsAmt) {
		this.tdsAmt = tdsAmt;
	}
	public boolean[] getAllowDiscount() {
		return allowDiscount;
	}
	public void setAllowDiscount(boolean[] allowDiscount) {
		this.allowDiscount = allowDiscount;
	}
	public boolean[] getAllowRateVariation() {
		return allowRateVariation;
	}
	public void setAllowRateVariation(boolean[] allowRateVariation) {
		this.allowRateVariation = allowRateVariation;
	}
	public String[] getConducting_doc_mandatory() {
		return conducting_doc_mandatory;
	}
	public void setConducting_doc_mandatory(String[] conducting_doc_mandatory) {
		this.conducting_doc_mandatory = conducting_doc_mandatory;
	}
	public BigDecimal getDynaPkgCharge() {
		return dynaPkgCharge;
	}
	public void setDynaPkgCharge(BigDecimal dynaPkgCharge) {
		this.dynaPkgCharge = dynaPkgCharge;
	}
	public int getDynaPkgId() {
		return dynaPkgId;
	}
	public void setDynaPkgId(int dynaPkgId) {
		this.dynaPkgId = dynaPkgId;
	}
	public String getDynaPkgProcessed() {
		return dynaPkgProcessed;
	}
	public void setDynaPkgProcessed(String dynaPkgProcessed) {
		this.dynaPkgProcessed = dynaPkgProcessed;
	}
	public String[] getChargeExcluded() {
		return chargeExcluded;
	}
	public void setChargeExcluded(String[] chargeExcluded) {
		this.chargeExcluded = chargeExcluded;
	}
	public String[] getPackageFinalized() {
		return packageFinalized;
	}
	public void setPackageFinalized(String[] packageFinalized) {
		this.packageFinalized = packageFinalized;
	}
	public int[] getConsultation_type_id() {
		return consultation_type_id;
	}
	public void setConsultation_type_id(int[] consultation_type_id) {
		this.consultation_type_id = consultation_type_id;
	}
	public String[] getOp_id() {
		return op_id;
	}
	public void setOp_id(String[] op_id) {
		this.op_id = op_id;
	}
	public String[] getFrom_date() {
		return from_date;
	}
	public void setFrom_date(String[] from_date) {
		this.from_date = from_date;
	}
	public String[] getTo_date() {
		return to_date;
	}
	public void setTo_date(String[] to_date) {
		this.to_date = to_date;
	}
	public int[] getCardTypeId() {
		return cardTypeId;
	}
	public void setCardTypeId(int[] cardTypeId) {
		this.cardTypeId = cardTypeId;
	}
	public BigDecimal[] getCurrencyAmt() {
		return currencyAmt;
	}
	public void setCurrencyAmt(BigDecimal[] currencyAmt) {
		this.currencyAmt = currencyAmt;
	}
	public int[] getCurrencyId() {
		return currencyId;
	}
	public void setCurrencyId(int[] currencyId) {
		this.currencyId = currencyId;
	}
	public String[] getExchangeDateTime() {
		return exchangeDateTime;
	}
	public void setExchangeDateTime(String[] exchangeDateTime) {
		this.exchangeDateTime = exchangeDateTime;
	}
	public BigDecimal[] getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(BigDecimal[] exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	public String[] getBankBatchNo() {
		return bankBatchNo;
	}
	public void setBankBatchNo(String[] bankBatchNo) {
		this.bankBatchNo = bankBatchNo;
	}
	public String[] getCardAuthCode() {
		return cardAuthCode;
	}
	public void setCardAuthCode(String[] cardAuthCode) {
		this.cardAuthCode = cardAuthCode;
	}
	public String[] getCardHolderName() {
		return cardHolderName;
	}
	public void setCardHolderName(String[] cardHolderName) {
		this.cardHolderName = cardHolderName;
	}
	public String[] getCardExpDate() {
		return cardExpDate;
	}
	public void setCardExpDate(String[] cardExpDate) {
		this.cardExpDate = cardExpDate;
	}
	public String[] getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String[] cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String[] getItemRemarks() {
		return itemRemarks;
	}
	public void setItemRemarks(String[] itemRemarks) {
		this.itemRemarks = itemRemarks;
	}
	public String getPrimaryApprovalAmount() {
		return primaryApprovalAmount;
	}
	public void setPrimaryApprovalAmount(String primaryApprovalAmount) {
		this.primaryApprovalAmount = primaryApprovalAmount;
	}
	public String getSecondaryApprovalAmount() {
		return secondaryApprovalAmount;
	}
	public void setSecondaryApprovalAmount(String secondaryApprovalAmount) {
		this.secondaryApprovalAmount = secondaryApprovalAmount;
	}
	public String getPrimaryTotalClaim() {
		return primaryTotalClaim;
	}
	public void setPrimaryTotalClaim(String primaryTotalClaim) {
		this.primaryTotalClaim = primaryTotalClaim;
	}
	public String getSecondaryTotalClaim() {
		return secondaryTotalClaim;
	}
	public void setSecondaryTotalClaim(String secondaryTotalClaim) {
		this.secondaryTotalClaim = secondaryTotalClaim;
	}
	public String getPrimaryClaimStatus() {
		return primaryClaimStatus;
	}
	public void setPrimaryClaimStatus(String primaryClaimStatus) {
		this.primaryClaimStatus = primaryClaimStatus;
	}
	public String getSecondaryClaimStatus() {
		return secondaryClaimStatus;
	}
	public void setSecondaryClaimStatus(String secondaryClaimStatus) {
		this.secondaryClaimStatus = secondaryClaimStatus;
	}
	public String getSecondarySponsorExists() {
		return secondarySponsorExists;
	}
	public void setSecondarySponsorExists(String secondarySponsorExists) {
		this.secondarySponsorExists = secondarySponsorExists;
	}
	public String getInsuranceDeduction() {
		return insuranceDeduction;
	}
	public void setInsuranceDeduction(String insuranceDeduction) {
		this.insuranceDeduction = insuranceDeduction;
	}
	public String getReopenReason() {
		return reopenReason;
	}
	public void setReopenReason(String reopenReason) {
		this.reopenReason = reopenReason;
	}
	public int getBillLabelId() {
		return billLabelId;
	}
	public void setBillLabelId(int billLabelId) {
		this.billLabelId = billLabelId;
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
	public int[] getRedeemed_points() {
		return redeemed_points;
	}
	public void setRedeemed_points(int[] redeemed_points) {
		this.redeemed_points = redeemed_points;
	}
	public BigDecimal[] getAmount_included() {
		return amount_included;
	}
	public void setAmount_included(BigDecimal[] amount_included) {
		this.amount_included = amount_included;
	}
	public BigDecimal[] getQty_included() {
		return qty_included;
	}
	public void setQty_included(BigDecimal[] qty_included) {
		this.qty_included = qty_included;
	}
	public String getPer_diem_code() {
		return per_diem_code;
	}
	public void setPer_diem_code(String per_diem_code) {
		this.per_diem_code = per_diem_code;
	}
	public BigDecimal[] getPriInsClaimAmt() {
		return priInsClaimAmt;
	}
	public void setPriInsClaimAmt(BigDecimal[] priInsClaimAmt) {
		this.priInsClaimAmt = priInsClaimAmt;
	}
	public BigDecimal[] getSecInsClaimAmt() {
		return secInsClaimAmt;
	}
	public void setSecInsClaimAmt(BigDecimal[] secInsClaimAmt) {
		this.secInsClaimAmt = secInsClaimAmt;
	}
	public String[] getSecPreAuthId() {
		return secPreAuthId;
	}
	public void setSecPreAuthId(String[] secPreAuthId) {
		this.secPreAuthId = secPreAuthId;
	}
	public Integer[] getSecPreAuthModeId() {
		return secPreAuthModeId;
	}
	public void setSecPreAuthModeId(Integer[] secPreAuthModeId) {
		this.secPreAuthModeId = secPreAuthModeId;
	}
	public String getWriteOffRemarks() {
		return writeOffRemarks;
	}
	public void setWriteOffRemarks(String writeOffRemarks) {
		this.writeOffRemarks = writeOffRemarks;
	}
	public String getSpnrWriteOffRemarks() {
		return spnrWriteOffRemarks;
	}
	public void setSpnrWriteOffRemarks(String spnrWriteOffRemarks) {
		this.spnrWriteOffRemarks = spnrWriteOffRemarks;
	}
	public int getBillDiscountCategory() {
		return billDiscountCategory;
	}
	public void setBillDiscountCategory(int billDiscountCategory) {
		this.billDiscountCategory = billDiscountCategory;
	}
	public BigDecimal getIpDepositSetOff() {
		return ipDepositSetOff;
	}
	public void setIpDepositSetOff(BigDecimal ipDepositSetOff) {
		this.ipDepositSetOff = ipDepositSetOff;
	}
	public String getDepositType() {
		return depositType;
	}
	public void setDepositType(String depositType) {
		this.depositType = depositType;
	}
	public String getOpendate() {
		return opendate;
	}
	public void setOpendate(String opendate) {
		this.opendate = opendate;
	}
	public String getOpentime() {
		return opentime;
	}
	public void setOpentime(String opentime) {
		this.opentime = opentime;
	}
	public Boolean[] getIsClaimLocked() {
		return isClaimLocked;
	}
	public void setIsClaimLocked(Boolean[] isClaimLocked) {
		this.isClaimLocked = isClaimLocked;
	}
	public String[] getPricreditNote() {
		return pricreditNote;
	}
	public String[] getSecreditNote() {
		return secreditNote;
	}
	public void setPricreditNote(String[] pricreditNote) {
		this.pricreditNote = pricreditNote;
	}
	public void setSecreditNote(String[] secreditNote) {
		this.secreditNote = secreditNote;
	}
	public String[] getIsEdited() {
		return isEdited;
	}
	public void setIsEdited(String[] isEdited) {
		this.isEdited = isEdited;
	}
	public BigDecimal[] getPriInsClaimTaxAmt() {
		return priInsClaimTaxAmt;
	}
	public void setPriInsClaimTaxAmt(BigDecimal[] priInsClaimTaxAmt) {
		this.priInsClaimTaxAmt = priInsClaimTaxAmt;
	}
	public BigDecimal[] getSecInsClaimTaxAmt() {
		return secInsClaimTaxAmt;
	}
	public void setSecInsClaimTaxAmt(BigDecimal[] secInsClaimTaxAmt) {
		this.secInsClaimTaxAmt = secInsClaimTaxAmt;
	}
	public String getModTime() {
		return modTime;
	}

	public void setModTime(String modTime) {
		this.modTime = modTime;
	}
	public String[] getDynaPackageExcluded() {
		return dynaPackageExcluded;
	}
	public void setDynaPackageExcluded(String[] dynaPackageExcluded) {
		this.dynaPackageExcluded = dynaPackageExcluded;
	}
	
}
