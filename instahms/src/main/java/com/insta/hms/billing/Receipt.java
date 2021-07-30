package com.insta.hms.billing;

import java.math.BigDecimal;

public class Receipt {

  /*
   * Bill payment types:
   */
  public static final String PATIENT_ADVANCE = "receipt_advance";
	public static final String PATIENT_SETTLEMENT = "receipt_settlement";

	public static final String REFUND = "refund"; // refund patient amount.

	public static final String PRIMARY_SPONSOR_ADVANCE = "pri_sponsor_receipt_advance";
	public static final String PRIMARY_SPONSOR_SETTLEMENT = "pri_sponsor_receipt_settlement";

	public static final String SECONDARY_SPONSOR_ADVANCE = "sec_sponsor_receipt_advance";
	public static final String SECONDARY_SPONSOR_SETTLEMENT = "sec_sponsor_receipt_settlement";

	/*
	 * Receipt print formats:
	 */

	public static final String BILL_CUM_RECEIPT_PRINT = "Bill";
	public static final String RECEIPT_PRINT = "Receipt";

	public static final String REFUND_PRINT = "Refund";

	public static final String SPONSOR_RECEIPT_PRINT = "Sponsor Receipt";

  private String receiptNo;
  private String receiptType; // specific to each mainType
  private String billNo;
  private BigDecimal amount;
  private java.sql.Timestamp receiptDate;
  private String counter;
  private int paymentModeId;
  private int cardTypeId;
  private String bankName;
  private String referenceNo;
  private String username;
  private String remarks;
  private String paymentType;
  private String tpaId;
  private Boolean isSettlement;
  private String realized = "Y";
  private Boolean isDeposit = false;
  private BigDecimal tdsAmt = BigDecimal.ZERO;
  private String paidBy;
  private String counterType;
  private String sponsorIndex;
  private String sponsorId;
  private String mobNumber;
  private String storeRetailCustomerId;
  private String incomingVisitId;
  private String totp;
  private String edcIMEI;

  /*
   * Extended fields available in some queries
   */
  private String paymentMode;
  private String cardType;

  private String bankBatchNo;
  private String cardAuthCode;
  private String cardHolderName;

  private String cardNumber;
  private java.sql.Date cardExpDate;

  private String currency;
  private int currencyId;
  private BigDecimal currencyAmt;
  private BigDecimal exchangeRate;
  private java.sql.Timestamp exchangeDateTime;

  private String visitId;
  private String visitType;
  private String billType; /* see constants in Bill */
  private String billStatus; /* see constants in Bill */

  private String mrno;
  private String patientTitle;
  private String patientName;
  private String patientLastName;
  private String patientGender;
  private BigDecimal patientAge;
  private String patientAgeIn;
  private Integer packageId;
  private String applicableToIp;
  private String depositAvailableFor;
  private BigDecimal commissionPercentage;
  private BigDecimal commissionAmount;
  private String payerName;
  private String payerAddress;
  private String payerMobileNumber;
  private Integer pointsRedeemed = 0;
  private BigDecimal totalTax = new BigDecimal(0.00) ;
  private int centerId;
  private Integer paymentTransactionId;
  /*
   * If it is a retail customer, then, the following are applicable instead of the above
   */
  private String customerName;

  /*
   * Extra field to set the print type
   */

  private String receiptPrintFormat;
  /*
   * Constants
   */
  public static final String MAIN_TYPE_RECEIPT = "A";
  public static final String MAIN_TYPE_RECEIPT_SETTLE = "S";
  public static final String MAIN_TYPE_REFUND = "F";
  public static final String MAIN_TYPE_THIRD_PARTY_ADVANCE = "3A";
  public static final String MAIN_TYPE_THIRD_PARTY_SETTLE = "3S";
  public static final String MAIN_TYPE_THIRD_PARTY_CLAIM = "C";

  /*
   * Accessors
   */

  /**
   * @return the edcIMEI
   */
  public String getEdcIMEI() {
    return edcIMEI;
  }

  /**
   * @param edcIMEI
   *          the edcIMEI to set
   */
  public void setEdcIMEI(String edcIMEI) {
    this.edcIMEI = edcIMEI;
  }

  public String getReceiptNo() {
    return receiptNo;
  }

  public void setReceiptNo(String v) {
    receiptNo = v;
  }

  public String getReceiptType() {
    return receiptType;
  }

  public void setReceiptType(String v) {
    receiptType = v;
  }

  public String getBillNo() {
    return billNo;
  }

  public void setBillNo(String v) {
    billNo = v;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal v) {
    amount = v;
  }

  public java.sql.Timestamp getReceiptDate() {
    return receiptDate;
  }

  public void setReceiptDate(java.sql.Timestamp v) {
    receiptDate = v;
  }

  public String getCounter() {
    return counter;
  }

  public void setCounter(String v) {
    counter = v;
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

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String v) {
    bankName = v;
  }

  public String getReferenceNo() {
    return referenceNo;
  }

  public void setReferenceNo(String v) {
    referenceNo = v;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String v) {
    username = v;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String v) {
    remarks = v;
  }

  public String getVisitId() {
    return visitId;
  }

  public void setVisitId(String v) {
    visitId = v;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String v) {
    visitType = v;
  }

  public String getBillType() {
    return billType;
  }

  public void setBillType(String v) {
    billType = v;
  }

  public String getBillStatus() {
    return billStatus;
  }

  public void setBillStatus(String v) {
    billStatus = v;
  }

  public String getMrno() {
    return mrno;
  }

  public void setMrno(String v) {
    mrno = v;
  }

  public String getPatientTitle() {
    return patientTitle;
  }

  public void setPatientTitle(String v) {
    patientTitle = v;
  }

  public String getPatientName() {
    return patientName;
  }

  public void setPatientName(String v) {
    patientName = v;
  }

  public String getPatientLastName() {
    return patientLastName;
  }

  public void setPatientLastName(String v) {
    patientLastName = v;
  }

  public String getPatientGender() {
    return patientGender;
  }

  public void setPatientGender(String v) {
    patientGender = v;
  }

  public BigDecimal getPatientAge() {
    return patientAge;
  }

  public void setPatientAge(BigDecimal v) {
    patientAge = v;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String v) {
    customerName = v;
  }

  /**
   * @return the patientAgeIn
   */
  public String getPatientAgeIn() {
    return patientAgeIn;
  }

  /**
   * @param patientAgeIn
   *          the patientAgeIn to set
   */
  public void setPatientAgeIn(String patientAgeIn) {
    this.patientAgeIn = patientAgeIn;
  }

  public String getPaymentType() {
    return paymentType;
  }

  public void setPaymentType(String paymentType) {
    this.paymentType = paymentType;
  }

  public String getTpaId() {
    return tpaId;
  }

  public void setTpaId(String tpaId) {
    this.tpaId = tpaId;
  }

  public Boolean getisSettlement() {
    return isSettlement;
  }

  public void setIsSettlement(Boolean isSettlement) {
    this.isSettlement = isSettlement;
  }

  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  public BigDecimal getTdsAmt() {
    return tdsAmt;
  }

  public void setTdsAmt(BigDecimal tdsAmt) {
    this.tdsAmt = tdsAmt;
  }

  public String getCounterType() {
    return counterType;
  }

  public void setCounterType(String counterType) {
    this.counterType = counterType;
  }

  public String getSponsorIndex() {
    return sponsorIndex;
  }

  public void setSponsorIndex(String sponsorIndex) {
    this.sponsorIndex = sponsorIndex;
  }

  public String getSponsorId() {
    return sponsorId;
  }

  public void setSponsorId(String sponsorId) {
    this.sponsorId = sponsorId;
  }

  public String getMobNumber() {
    return mobNumber;
  }

  public void setMobNumber(String mobNumber) {
    this.mobNumber = mobNumber;
  }

  public void setIncomingVisitId(String incomingVisitId) {
    this.incomingVisitId = incomingVisitId;
  }
  
  public String getIncomingVisitId() {
    return incomingVisitId;
  }
  
  public void setStoreRetailCustomerId(String storeRetailCustomerId) {
    this.storeRetailCustomerId = storeRetailCustomerId;
  }
  
  public String getStoreRetailCustomerId() {
    return storeRetailCustomerId;
  }
  
  public String getTotp() {
    return totp;
  }

  public void setTotp(String totp) {
    this.totp = totp;
  }

  public String getCardType() {
    return cardType;
  }

  public void setCardType(String v) {
    cardType = v;
  }

  public String getPaymentMode() {
    return paymentMode;
  }

  public void setPaymentMode(String v) {
    paymentMode = v;
  }

  public String getBankBatchNo() {
    return bankBatchNo;
  }

  public void setBankBatchNo(String bankBatchNo) {
    this.bankBatchNo = bankBatchNo;
  }

  public String getCardAuthCode() {
    return cardAuthCode;
  }

  public void setCardAuthCode(String cardAuthCode) {
    this.cardAuthCode = cardAuthCode;
  }

  public String getCardHolderName() {
    return cardHolderName;
  }

  public void setCardHolderName(String cardHolderName) {
    this.cardHolderName = cardHolderName;
  }

  public BigDecimal getCurrencyAmt() {
    return currencyAmt;
  }

  public void setCurrencyAmt(BigDecimal currencyAmt) {
    this.currencyAmt = currencyAmt;
  }

  public int getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(int currencyId) {
    this.currencyId = currencyId;
  }

  public java.sql.Timestamp getExchangeDateTime() {
    return exchangeDateTime;
  }

  public void setExchangeDateTime(java.sql.Timestamp exchangeDateTime) {
    this.exchangeDateTime = exchangeDateTime;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public java.sql.Date getCardExpDate() {
    return cardExpDate;
  }

  public void setCardExpDate(java.sql.Date cardExpDate) {
    this.cardExpDate = cardExpDate;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public String getReceiptPrintFormat() {
    return receiptPrintFormat;
  }

  public void setReceiptPrintFormat(String receiptPrintFormat) {
    this.receiptPrintFormat = receiptPrintFormat;
  }

  public Integer getPackageId() {
    return packageId;
  }

  public void setPackageId(Integer packageId) {
    this.packageId = packageId;
  }

  public String getApplicableToIp() {
    return applicableToIp;
  }

  public void setApplicableToIp(String applicableToIp) {
    this.applicableToIp = applicableToIp;
  }

  public BigDecimal getCommissionPercentage() {
    return commissionPercentage;
  }

  public void setCommissionPercentage(BigDecimal commissionPercentage) {
    this.commissionPercentage = commissionPercentage;
  }

  public BigDecimal getCommissionAmount() {
    return commissionAmount;
  }

  public void setCommissionAmount(BigDecimal commissionAmount) {
    this.commissionAmount = commissionAmount;
  }

  public Boolean getIsDeposit() {
    return isDeposit;
  }

  public void setIsDeposit(Boolean isDeposit) {
    this.isDeposit = isDeposit;
  }

  public String getRealized() {
    return realized;
  }

  public void setRealized(String realized) {
    this.realized = realized;
  }

  public String getDepositAvailableFor() {
    return depositAvailableFor;
  }

  public void setDepositAvailableFor(String depositAvailableFor) {
    this.depositAvailableFor = depositAvailableFor;
  }

  public String getPayerName() {
    return payerName;
  }

  public void setPayerName(String payerName) {
    this.payerName = payerName;
  }
  
  public String getPayerAddress() {
    return payerAddress;
  }
  
  public void setPayerAddress(String payerAddress) {
    this.payerAddress = payerAddress;
  }

  public String getPayerMobileNumber() {
    return payerMobileNumber;
  }

  public void setPayerMobileNumber(String payerMobileNumber) {
    this.payerMobileNumber = payerMobileNumber;
  }

  public Integer getPointsRedeemed() {
    return pointsRedeemed;
  }

  public void setPointsRedeemed(Integer pointsRedeemed) {
    this.pointsRedeemed = pointsRedeemed;
  }

  public BigDecimal getTotalTax() {
    return totalTax;
  }

  public void setTotalTax(BigDecimal totalTax) {
    this.totalTax = totalTax;
  }

  public int getCenterId() {
	  return centerId;
  }

  public void setCenterId(int centerId) {
	  this.centerId = centerId;
  }

  public Integer getPaymentTransactionId() {
	  return paymentTransactionId;
  }

  public void setPaymentTransactionId(Integer paymentTransactionId) {
	  this.paymentTransactionId = paymentTransactionId;
  }
}
