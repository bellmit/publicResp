package com.insta.hms.billing;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class Bill {

	private String billNo;
	private String visitId;
	private String visitType;		/* See constants below */
	private String billType;			/* see constants below */
	private Date openDate;
	private Timestamp openDateTime;
	private String status;			/* See constants below */
	private String paymentStatus = "U";
	private Date finalizedDate;
	private Date closedDate;
	private Date modTime;
	private String openedBy;
	private String closedBy;
	private String finalizedBy;
	private String userName;
	private int centerId;
	private String okToDischarge;		/* See constants below */
	private String appModified;		/* See constants below */
	private String billRemarks;
	private String writeOffRemarks;
	private String spnrWriteOffRemarks;
	private int billDiscountAuth;
	private int billDiscountCategory;
	private String discountAuthName;
	private String discountCategoryName;
	private String primaryClaimStatus;
	private String secondaryClaimStatus;
	private BigDecimal claimRecdAmount;
	private String cancelReason;
	private BigDecimal totalAmount;
	private BigDecimal totalDiscount;
	private BigDecimal totalTax;
	private BigDecimal totalClaimTax;

	private String procedureCode;
	private String procedureName;
	private int procedureNo;
	private String sponsorBillNo;
	private String dynaPkgName;
	private int dynaPkgId;
	private BigDecimal dynaPkgCharge;
	private String dynaPkgProcessed = "N";
	private String reopenReason;
	private int billLabelId;
	private String patientWriteOff;
	private String sponsorWriteOff;
	private String cancellationApprovalStatus;
	private String cancellationApprovedBy;

	private int primaryPlanId;
	private int secondaryPlanId;

	private String restrictionType = "N";

	//private BigDecimal totalSponsorReceipts;

	private BigDecimal approvalAmount;
	private BigDecimal primaryApprovalAmount;
	private BigDecimal secondaryApprovalAmount;

	private BigDecimal totalReceipts;

	private BigDecimal totalPrimarySponsorReceipts;
	private BigDecimal totalSecondarySponsorReceipts;

	private BigDecimal primaryTotalClaim;
	private BigDecimal secondaryTotalClaim;

	private BigDecimal billAmount;
	
	private BigDecimal amountPaid;
	private BigDecimal amountDue;
	private BigDecimal depositSetOff = BigDecimal.ZERO;
	private BigDecimal ipDepositSetOff = BigDecimal.ZERO;
	private BigDecimal insuranceDeduction;
	private boolean is_tpa = false;
	private boolean locked = false;
	private int account_group;
	private String claim_id;
	private String isPrimaryBill = "N";
	private String billRatePlanId;
	private String billRatePlanName;

	private int rewardPointsEarned;
	private int rewardPointsRedeemed;
	private BigDecimal rewardPointsRedeemedAmount;
	private String creditNoteReasons;

	/*
	 * Fields not part of bill table
	 */

	private String readyToDischarge;
	private String discharge;
	private String doctorId;
	private String disDate;
	private String disTime;
	private String patientAgeIn;
	private String visitStatus;
	private BigDecimal ipCreditLimitAmount;

	private String patientStatus;
	/*
	 * Extended fields available in some queries
	 */
	private java.sql.Time regTime;
	private java.sql.Date regDate;

	private String mrno;
	private String patientTitle;
	private String patientName;
	private String patientLastName;
	private String patientGender;
	private BigDecimal patientAge;

	/*
	 * If it is a retail customer, then, the following are applicable
	 * instead of the above
	 */
	private String customerName;
	private BigDecimal totalClaim;
	private String billPrinted = "N";
	
	private String primaryClosureType;
	private String secondaryClosureType;
	
	private String primaryClaimID;
	private String secondaryClaimID;
	
	private Date financialDisDate;
	private Timestamp financialDisTime;
	private Timestamp lastFinalizedAt;
	private String billSignature;

	/*
	 * Constants
	 */
	public static final String BILL_TYPE_CREDIT	 = "C";
	public static final String BILL_TYPE_PREPAID = "P";

	public static final String BILL_VISIT_TYPE_IP = "i";
	public static final String BILL_VISIT_TYPE_OP = "o";
	public static final String BILL_VISIT_TYPE_RETAIL = "r";
	public static final String BILL_VISIT_TYPE_INCOMING = "t";

	public static final String BILL_RESTRICTION_HOSPITAL = "N";
	public static final String BILL_RESTRICTION_NONE = "N";
	public static final String BILL_RESTRICTION_PHARMACY = "P";
	public static final String BILL_RESTRICTION_TEST = "T";

	public static final String BILL_STATUS_OPEN = "A";
	public static final String BILL_STATUS_FINALIZED = "F";
	public static final String BILL_STATUS_CLOSED = "C";
	public static final String BILL_STATUS_CANCELLED = "X";

	public static final String BILL_DISCHARGE_OK = "Y";
	public static final String BILL_DISCHARGE_NOTOK = "N";

	public static final String BILL_APP_MODIFIED = "Y";
	public static final String BILL_APP_NOT_MODIFIED = "N";

	public static final String BILL_PAYMENT_UNPAID = "U";
	public static final String BILL_PAYMENT_PAID = "P";

	public static final String BILL_CLAIM_OPEN = "O";
	public static final String BILL_CLAIM_SENT = "S";
	public static final String BILL_CLAIM_RECEIVED = "R";

	public static final int BILL_DEFAULT_ACCOUNT_GROUP = 1;


	/*
	 * Default constructor
	 */
	public Bill() {
		// Billno and visitId must be set, other non-nullable fields can be defaulted
		billType = BILL_TYPE_CREDIT;
		status = BILL_STATUS_OPEN;
		openDate = new Date();
		modTime = new Date();
		okToDischarge = BILL_DISCHARGE_NOTOK;
		appModified = BILL_APP_NOT_MODIFIED;
	}

	/*
	 * Accessors
	 */
	public String getBillNo() { return billNo; }
	public void setBillNo(String v) { billNo = v; }

	public String getVisitId() { return visitId; }
	public void setVisitId(String v) { visitId = v; }

	public String getVisitType() { return visitType; }
	public void setVisitType(String v) { visitType = v; }

	public String getBillType() { return billType; }
	public void setBillType(String v) { billType = v; }

	public Date getOpenDate() { return openDate; }
	public void setOpenDate(Date v) { openDate = v; }
	
	public Timestamp getOpenDateTime() { return openDateTime; }
	public void setOpenDateTime(Timestamp v) { openDateTime = v; }

	public String getStatus() { return status; }
	public void setStatus(String v) { status = v; }

	public String getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(String v) { paymentStatus = v; }

	public Date getFinalizedDate() { return finalizedDate; }
	public void setFinalizedDate(Date v) { finalizedDate = v; }

	public Date getClosedDate() { return closedDate; }
	public void setClosedDate(Date v) { closedDate = v; }

	public Date getModTime() { return modTime; }
	public void setModTime(Date v) { modTime = v; }

	public String getStatusDisplay() {
		if (status.equals(BILL_STATUS_OPEN)) return "Open";
		else if (status.equals(BILL_STATUS_FINALIZED)) return "Finalized";
		else if (status.equals(BILL_STATUS_CLOSED)) return "Closed";
		else if (status.equals(BILL_STATUS_CANCELLED)) return "Cancelled";
		return null;
	}

	public String getOkToDischarge() {return okToDischarge;}
	public void setOkToDischarge(String v) {okToDischarge=v;}

	public String getAppModified() { return appModified; }
	public void setAppModified(String v) { appModified = v; }

	public java.sql.Time getRegTime() { return regTime; }
	public void setRegTime(java.sql.Time v) { regTime = v; }

	public java.sql.Date getRegDate() { return regDate; }
	public void setRegDate(java.sql.Date v) { regDate = v; }

	public String getMrno() { return mrno; }
	public void setMrno(String v) { mrno = v; }

	public String getPatientTitle() { return patientTitle; }
	public void setPatientTitle(String v) { patientTitle = v; }

	public String getPatientName() { return patientName; }
	public void setPatientName(String v) { patientName = v; }

	public String getPatientLastName() { return patientLastName; }
	public void setPatientLastName(String v) { patientLastName = v; }

	public String getPatientGender() { return patientGender; }
	public void setPatientGender(String v) { patientGender = v; }

	public BigDecimal getPatientAge() { return patientAge; }
	public void setPatientAge(BigDecimal v) { patientAge = v; }

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String v) { customerName = v; }

	public String getBillRemarks() { return billRemarks; }
	public void setBillRemarks(String billRemarks) { this.billRemarks = billRemarks; }

	public int getBillDiscountAuth() {return billDiscountAuth; }
	public void setBillDiscountAuth(int billDiscountAuth) { this.billDiscountAuth = billDiscountAuth; }

	public BigDecimal getAmountDue() { return amountDue; }
	public void setAmountDue(BigDecimal amountDue) { this.amountDue = amountDue; }

	public BigDecimal getAmountPaid() { return amountPaid; }
	public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

	public BigDecimal getBillAmount() { return billAmount; }
	public void setBillAmount(BigDecimal billAmount) { this.billAmount = billAmount; }

	public String getReadyToDischarge() { return readyToDischarge; }
	public void setReadyToDischarge(String v) { this.readyToDischarge = v;}

	public String getDischarge() { return discharge; }
	public void setDischarge(String discharge) { this.discharge = discharge; }

	public String getDoctorId() { return doctorId; }
	public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

	public String getDisDate() { return disDate; }
	public void setDisDate(String disDate) { this.disDate = disDate; }

	public String getDisTime() { return disTime; }
	public void setDisTime(String disTime) { this.disTime = disTime; }

	public String getVisitStatus() { return visitStatus; }
	public void setVisitStatus(String v) { visitStatus = v; }

	public String getUserName() { return userName; }
	public void setUserName(String v) { userName = v; }

	public int getCenterId() { return centerId; }
	public void setCenterId(int centerId) {	this.centerId = centerId; }

	public String getOpenedBy() { return openedBy; }
	public void setOpenedBy(String v) { openedBy = v; }

	public String getClosedBy() { return closedBy; }
	public void setClosedBy(String v) { closedBy = v; }

	public String getFinalizedBy() { return finalizedBy; }
	public void setFinalizedBy(String v) { finalizedBy = v; }

	public String getPrimaryClaimStatus() { return primaryClaimStatus; }
	public void setPrimaryClaimStatus(String primaryClaimStatus) {
		this.primaryClaimStatus = primaryClaimStatus;
	}

	public String getSecondaryClaimStatus() { return secondaryClaimStatus; }
	public void setSecondaryClaimStatus(String secondaryClaimStatus) {
		this.secondaryClaimStatus = secondaryClaimStatus;
	}

	public BigDecimal getClaimRecdAmount() { return claimRecdAmount; }
	public void setClaimRecdAmount(BigDecimal v) { claimRecdAmount = v; }

	public String getCancelReason() { return cancelReason; }
	public void setCancelReason(String v) { cancelReason = v; }

	public String getPatientAgeIn() { return patientAgeIn; }
	public void setPatientAgeIn(String patientAgeIn) { this.patientAgeIn = patientAgeIn; }

	public String getPatientStatus() { return patientStatus; }
	public void setPatientStatus(String patientStatus) { this.patientStatus = patientStatus; }

	public BigDecimal getApprovalAmount() { return approvalAmount; }
	public void setApprovalAmount(BigDecimal approvalAmount) { this.approvalAmount = approvalAmount; }

	public BigDecimal getPrimaryApprovalAmount() { return primaryApprovalAmount; }
	public void setPrimaryApprovalAmount(BigDecimal primaryApprovalAmount) {
		this.primaryApprovalAmount = primaryApprovalAmount;
	}

	public BigDecimal getSecondaryApprovalAmount() { return secondaryApprovalAmount; }
	public void setSecondaryApprovalAmount(BigDecimal secondaryApprovalAmount) {
		this.secondaryApprovalAmount = secondaryApprovalAmount;
	}

	public String getDiscountAuthName() { return discountAuthName; }
	public void setDiscountAuthName(String discountAuthName) { this.discountAuthName = discountAuthName; }

	public String getRestrictionType() { return restrictionType; }
	public void setRestrictionType(String restrictionType) { this.restrictionType = restrictionType; }

	public int getAccount_group() { return account_group; }
	public void setAccount_group(int account_group) { this.account_group = account_group; }

	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	
	public BigDecimal getTotalDiscount() { return totalDiscount; }
	public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }

	public String getProcedureCode() { return procedureCode; }
	public void setProcedureCode(String procedureCode) { this.procedureCode = procedureCode; }

	public String getProcedureName() { return procedureName; }
	public void setProcedureName(String procedureName) {this.procedureName = procedureName; }

	public int getProcedureNo() { return procedureNo;}
	public void setProcedureNo(int procedureNo) { this.procedureNo = procedureNo; }

	public String getSponsorBillNo() { return sponsorBillNo; }
	public void setSponsorBillNo(String sponsorBillNo) { this.sponsorBillNo = sponsorBillNo;}

	public BigDecimal getDepositSetOff() { return depositSetOff; }
	public void setDepositSetOff(BigDecimal depositSetOff) { this.depositSetOff = depositSetOff;}

	public BigDecimal getTotalReceipts() { return totalReceipts; }
	public void setTotalReceipts(BigDecimal totalReceipts) { this.totalReceipts = totalReceipts; }

	/*public BigDecimal getTotalSponsorReceipts() { return totalSponsorReceipts; }
	  public void setTotalSponsorReceipts(BigDecimal totalSponsorReceipts) {this.totalSponsorReceipts = totalSponsorReceipts;	}
	 */

	public BigDecimal getTotalPrimarySponsorReceipts() { return totalPrimarySponsorReceipts; }
	public void setTotalPrimarySponsorReceipts( BigDecimal totalPrimarySponsorReceipts) {
		this.totalPrimarySponsorReceipts = totalPrimarySponsorReceipts;
	}

	public BigDecimal getTotalSecondarySponsorReceipts() { return totalSecondarySponsorReceipts;}
	public void setTotalSecondarySponsorReceipts( BigDecimal totalSecondarySponsorReceipts) {
		this.totalSecondarySponsorReceipts = totalSecondarySponsorReceipts;
	}

	public BigDecimal getPrimaryTotalClaim() { return primaryTotalClaim; }
	public void setPrimaryTotalClaim(BigDecimal primaryTotalClaim) {
		this.primaryTotalClaim = primaryTotalClaim;
	}

	public BigDecimal getSecondaryTotalClaim() { return secondaryTotalClaim; }
	public void setSecondaryTotalClaim(BigDecimal secondaryTotalClaim) {
		this.secondaryTotalClaim = secondaryTotalClaim;
	}

	public BigDecimal getInsuranceDeduction() {	return insuranceDeduction; }
	public void setInsuranceDeduction(BigDecimal insuranceDeduction) { this.insuranceDeduction = insuranceDeduction;}

	public BigDecimal getDynaPkgCharge() { return dynaPkgCharge;}
	public void setDynaPkgCharge(BigDecimal dynaPkgCharge) { this.dynaPkgCharge = dynaPkgCharge; }

	public int getDynaPkgId() { return dynaPkgId;}
	public void setDynaPkgId(int dynaPkgId) { this.dynaPkgId = dynaPkgId; }

	public String getDynaPkgName() { return dynaPkgName;}
	public void setDynaPkgName(String dynaPkgName) { this.dynaPkgName = dynaPkgName; }

	public String getDynaPkgProcessed() { return dynaPkgProcessed;}
	public void setDynaPkgProcessed(String dynaPkgProcessed) { this.dynaPkgProcessed = dynaPkgProcessed; }

	public boolean getIs_tpa() { return is_tpa; }
	public void setIs_tpa(boolean is_tpa) {	this.is_tpa = is_tpa;}
	
	public boolean getLocked() { return locked; }
	public void setLocked(boolean locked) {	this.locked = locked;}

	public String getClaim_id() { return claim_id; }
	public void setClaim_id(String claim_id) { this.claim_id = claim_id; }

	public String getIsPrimaryBill() { return isPrimaryBill; }
	public void setIsPrimaryBill(String v) { isPrimaryBill = v; }

	public String getBillRatePlanId() {	return billRatePlanId;	}
	public void setBillRatePlanId(String billRatePlanId) {	this.billRatePlanId = billRatePlanId; }

	public String getBillRatePlanName() { return billRatePlanName; }
	public void setBillRatePlanName(String billRatePlanName) { this.billRatePlanName = billRatePlanName; }

	public BigDecimal getTotalClaim() { return totalClaim; }
	public void setTotalClaim(BigDecimal totalClaim) { this.totalClaim = totalClaim; }

	public String getBillPrinted() { return billPrinted; }
	public void setBillPrinted(String billPrinted) { this.billPrinted = billPrinted; }

	public String getReopenReason() { return reopenReason; }
	public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }

	public int getBillLabelId() { return billLabelId;}
	public void setBillLabelId(int billLabelId) { this.billLabelId = billLabelId; }

	public int getRewardPointsRedeemed() { return rewardPointsRedeemed; }
	public void setRewardPointsRedeemed(int rewardPointsRedeemed) {
		this.rewardPointsRedeemed = rewardPointsRedeemed;
	}
	public BigDecimal getRewardPointsRedeemedAmount() { return rewardPointsRedeemedAmount;}
	public void setRewardPointsRedeemedAmount(BigDecimal rewardPointsRedeemedAmount) {
		this.rewardPointsRedeemedAmount = rewardPointsRedeemedAmount;
	}
	public int getRewardPointsEarned() { return rewardPointsEarned; }
	public void setRewardPointsEarned(int rewardPointsEarned) {
		this.rewardPointsEarned = rewardPointsEarned;
	}

	public int getPrimaryPlanId() {
		return primaryPlanId;
	}

	public void setPrimaryPlanId(int primaryPlanId) {
		this.primaryPlanId = primaryPlanId;
	}

	public int getSecondaryPlanId() {
		return secondaryPlanId;
	}

	public void setSecondaryPlanId(int secondaryPlanId) {
		this.secondaryPlanId = secondaryPlanId;
	}

	public String getPatientWriteOff() {
		return patientWriteOff;
	}

	public void setPatientWriteOff(String patientWriteOff) {
		this.patientWriteOff = patientWriteOff;
	}

	public String getSponsorWriteOff() {
		return sponsorWriteOff;
	}

	public void setSponsorWriteOff(String sponsorWriteOff) {
		this.sponsorWriteOff = sponsorWriteOff;
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

	public String getCancellationApprovalStatus() {
		return cancellationApprovalStatus;
	}

	public void setCancellationApprovalStatus(String cancellationApprovalStatus) {
		this.cancellationApprovalStatus = cancellationApprovalStatus;
	}

	public int getBillDiscountCategory() {
		return billDiscountCategory;
	}

	public void setBillDiscountCategory(int billDiscountCategory) {
		this.billDiscountCategory = billDiscountCategory;
	}

	public String getDiscountCategoryName() {
		return discountCategoryName;
	}

	public void setDiscountCategoryName(String discountCategoryName) {
		this.discountCategoryName = discountCategoryName;
	}

	public String getCancellationApprovedBy() {
		return cancellationApprovedBy;
	}

	public void setCancellationApprovedBy(String cancellationApprovedBy) {
		this.cancellationApprovedBy = cancellationApprovedBy;
	}

	public BigDecimal getIpDepositSetOff() {
		return ipDepositSetOff;
	}

	public void setIpDepositSetOff(BigDecimal ipDepositSetOff) {
		this.ipDepositSetOff = ipDepositSetOff;
	}

	public String getCreditNoteReasons() {
		return creditNoteReasons;
	}

	public void setCreditNoteReasons(String creditNoteReasons) {
		this.creditNoteReasons = creditNoteReasons;
	}

	public String getPrimaryClosureType() {
		return primaryClosureType;
	}

	public String getSecondaryClosureType() {
		return secondaryClosureType;
	}

	public void setPrimaryClosureType(String primaryClosureType) {
		this.primaryClosureType = primaryClosureType;
	}

	public void setSecondaryClosureType(String secondaryClosureType) {
		this.secondaryClosureType = secondaryClosureType;
	}

	public String getPrimaryClaimID() {
		return primaryClaimID;
	}

	public String getSecondaryClaimID() {
		return secondaryClaimID;
	}

	public void setPrimaryClaimID(String primaryClaimID) {
		this.primaryClaimID = primaryClaimID;
	}

	public void setSecondaryClaimID(String secondaryClaimID) {
		this.secondaryClaimID = secondaryClaimID;
	}
	
	public Timestamp getFinancialDisTime() {
		return financialDisTime;
	}

	public Date getFinancialDisDate() {
		return financialDisDate;
	}

	public void setFinancialDisDate(Date financialDisDate) {
		this.financialDisDate = financialDisDate;
	}

	public void setFinancialDisTime(Timestamp financialDisTime) {
		this.financialDisTime = financialDisTime;
	}
	
	public Timestamp getLastFinalizedAt() {
		return lastFinalizedAt;
	}

	public void setLastFinalizedAt(Timestamp lastFinalizedAt) {
		this.lastFinalizedAt = lastFinalizedAt;
	}
	
	public BigDecimal getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(BigDecimal totalTax) {
		this.totalTax = totalTax;
	}
	
	public BigDecimal getTotalClaimTax() {
		return totalClaimTax;
	}

	public void setTotalClaimTax(BigDecimal totalClaimTax) {
		this.totalClaimTax = totalClaimTax;
	}

	public BigDecimal getIpCreditLimitAmount() {
		return ipCreditLimitAmount;
	}

	public void setIpCreditLimitAmount(BigDecimal ipCreditLimitAmount) {
		this.ipCreditLimitAmount = ipCreditLimitAmount;
	}

	public String getBillSignature() {
		return billSignature;
	}

	public void setBillSignature(String billSignature) {
		this.billSignature = billSignature;
	}
}

