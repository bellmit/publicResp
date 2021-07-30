package com.insta.hms.model;

import com.insta.hms.core.patient.PatientDetailsModel;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Immutable
@Table(name = "deposits_receipts_view")
public class DepositsReceiptsViewModel implements java.io.Serializable {
  private String paymentType;
  private String receiptNo;
  private BigDecimal amount;
  private BigDecimal tdsAmt;
  private Date displayDate;
  private Date modTime;
  private CounterAssociatedAccountgroupViewModel counter;
  private Integer paymentModeId;
  private Integer cardTypeId;
  private String paymentMode;
  private String cardType;
  private String bankName;
  private String referenceNo;
  private String mobNumber;
  private String totp;
  private String username;
  private String remarks;
  private String bankBatchNo;
  private String cardAuthCode;
  private String cardHolderName;
  private Integer currencyId;
  private BigDecimal exchangeRate;
  private Date exchangeDate;
  private BigDecimal currencyAmt;
  private Date cardExpdate;
  private String cardNumber;
  private String currency;
  private String status;
  private PatientDetailsModel mrNo;
  private String salutation;
  private String patientName;
  private String middleName;
  private String lastName;
  private String patientFullName;
  private Date dob;
  private String patientGender;
  private Character counterType;
  private String counterNo;
  private String paymentModeAccount;
  private String paidBy;
  private Integer centerId;
  private String refRequired;
  private String bankRequired;
  private String bank;
  private BigDecimal conversionRate;
  private String depositAvailableFor;
  private String packageId;

  public DepositsReceiptsViewModel() {

  }

  @Column(name = "payment_type", length = 30)
  public String getPaymentType() {
    return paymentType;
  }

  public void setPaymentType(String paymentType) {
    this.paymentType = paymentType;
  }

  @Id
  @Column(name = "receipt_no", unique = true, nullable = false, length = 15)
  public String getReceiptNo() {
    return receiptNo;
  }

  public void setReceiptNo(String receiptNo) {
    this.receiptNo = receiptNo;
  }

  @Column(name = "amount", precision = 16)
  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  @Column(name = "tds_amt")
  public BigDecimal getTdsAmt() {
    return tdsAmt;
  }

  public void setTdsAmt(BigDecimal tdsAmt) {
    this.tdsAmt = tdsAmt;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "display_date", length = 29)
  public Date getDisplayDate() {
    return displayDate;
  }

  public void setDisplayDate(Date displayDate) {
    this.displayDate = displayDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "mod_time", length = 29)
  public Date getModTime() {
    return modTime;
  }

  public void setModTime(Date modTime) {
    this.modTime = modTime;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "counter", referencedColumnName = "counter_id", nullable = false, insertable = false, updatable = false)
  public CounterAssociatedAccountgroupViewModel getCounter() {
    return counter;
  }

  public void setCounter(CounterAssociatedAccountgroupViewModel counter) {
    this.counter = counter;
  }

  @Column(name = "payment_mode_id")
  public Integer getPaymentModeId() {
    return paymentModeId;
  }

  public void setPaymentModeId(Integer paymentModeId) {
    this.paymentModeId = paymentModeId;
  }

  @Column(name = "card_type_id")
  public Integer getCardTypeId() {
    return cardTypeId;
  }

  public void setCardTypeId(Integer cardTypeId) {
    this.cardTypeId = cardTypeId;
  }

  @Column(name = "payment_mode", length = 30)
  public String getPaymentMode() {
    return paymentMode;
  }

  public void setPaymentMode(String paymentMode) {
    this.paymentMode = paymentMode;
  }

  @Column(name = "card_type", length = 50)
  public String getCardType() {
    return cardType;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

  @Column(name = "bank_name", length = 50)
  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  @Column(name = "reference_no", length = 100)
  public String getReferenceNo() {
    return referenceNo;
  }

  public void setReferenceNo(String referenceNo) {
    this.referenceNo = referenceNo;
  }

  @Column(name = "mob_number", length = 20)
  public String getMobNumber() {
    return mobNumber;
  }

  public void setMobNumber(String mobNumber) {
    this.mobNumber = mobNumber;
  }

  @Column(name = "totp", length = 10)
  public String getTotp() {
    return totp;
  }

  public void setTotp(String totp) {
    this.totp = totp;
  }

  @Column(name = "username", length = 30)
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Column(name = "remarks", length = 100)
  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  @Column(name = "bank_batch_no", length = 100)
  public String getBankBatchNo() {
    return bankBatchNo;
  }

  public void setBankBatchNo(String bankBatchNo) {
    this.bankBatchNo = bankBatchNo;
  }

  @Column(name = "card_auth_code", length = 100)
  public String getCardAuthCode() {
    return cardAuthCode;
  }

  public void setCardAuthCode(String cardAuthCode) {
    this.cardAuthCode = cardAuthCode;
  }

  @Column(name = "card_holder_name", length = 100)
  public String getCardHolderName() {
    return cardHolderName;
  }

  public void setCardHolderName(String cardHolderName) {
    this.cardHolderName = cardHolderName;
  }

  @Column(name = "currency_id")
  public Integer getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(Integer currencyId) {
    this.currencyId = currencyId;
  }

  @Column(name = "exchange_rate", precision = 16)
  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "exchange_date", length = 29)
  public Date getExchangeDate() {
    return exchangeDate;
  }

  public void setExchangeDate(Date exchangeDate) {
    this.exchangeDate = exchangeDate;
  }

  @Column(name = "currency_amt", precision = 16)
  public BigDecimal getCurrencyAmt() {
    return currencyAmt;
  }

  public void setCurrencyAmt(BigDecimal currencyAmt) {
    this.currencyAmt = currencyAmt;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "card_expdate", length = 13)
  public Date getCardExpdate() {
    return cardExpdate;
  }

  public void setCardExpdate(Date cardExpdate) {
    this.cardExpdate = cardExpdate;
  }

  @Column(name = "card_number", length = 150)
  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  @Column(name = "currency", length = 300)
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Column(name = "status", length = 30)
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @NotFound(action = NotFoundAction.IGNORE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mr_no", referencedColumnName = "mr_no", nullable = false, insertable = false, updatable = false)
  public PatientDetailsModel getMrNo() {
    return mrNo;
  }

  public void setMrNo(PatientDetailsModel mrNo) {
    this.mrNo = mrNo;
  }

  @Column(name = "salutation", length = 500)
  public String getSalutation() {
    return salutation;
  }

  public void setSalutation(String salutation) {
    this.salutation = salutation;
  }

  @Column(name = "patient_name", length = 50)
  public String getPatientName() {
    return patientName;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  @Column(name = "middle_name", length = 200)
  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  @Column(name = "last_name", length = 50)
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Column(name = "patient_full_name")
  public String getPatientFullName() {
    return patientFullName;
  }

  public void setPatientFullName(String patientFullName) {
    this.patientFullName = patientFullName;
  }

  @Temporal(TemporalType.DATE)
  @Column(name = "dob", length = 13)
  public Date getDob() {
    return dob;
  }

  public void setDob(Date dob) {
    this.dob = dob;
  }

  @Column(name = "patient_gender", length = 1)
  public String getPatientGender() {
    return patientGender;
  }

  public void setPatientGender(String patientGender) {
    this.patientGender = patientGender;
  }

  @Column(name = "counter_type", length = 1)
  public Character getCounterType() {
    return counterType;
  }

  public void setCounterType(Character counterType) {
    this.counterType = counterType;
  }

  @Column(name = "counter_no", length = 100)
  public String getCounterNo() {
    return counterNo;
  }

  public void setCounterNo(String counterNo) {
    this.counterNo = counterNo;
  }

  @Column(name = "payment_mode_account", length = 100)
  public String getPaymentModeAccount() {
    return paymentModeAccount;
  }

  public void setPaymentModeAccount(String paymentModeAccount) {
    this.paymentModeAccount = paymentModeAccount;
  }

  @Column(name = "paid_by", length = 100)
  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  @Column(name = "center_id")
  public Integer getCenterId() {
    return centerId;
  }

  public void setCenterId(Integer centerId) {
    this.centerId = centerId;
  }

  @Column(name = "ref_required", length = 1)
  public String getRefRequired() {
    return refRequired;
  }

  public void setRefRequired(String refRequired) {
    this.refRequired = refRequired;
  }

  @Column(name = "bank_required", length = 1)
  public String getBankRequired() {
    return bankRequired;
  }

  public void setBankRequired(String bankRequired) {
    this.bankRequired = bankRequired;
  }

  @Column(name = "bank", length = 50)
  public String getBank() {
    return bank;
  }

  public void setBank(String bank) {
    this.bank = bank;
  }

  @Column(name = "conversion_rate")
  public BigDecimal getConversionRate() {
    return conversionRate;
  }

  public void setConversionRate(BigDecimal conversionRate) {
    this.conversionRate = conversionRate;
  }

  @Column(name = "deposit_available_for")
  public String getDepositAvailableFor() {
    return depositAvailableFor;
  }

  public void setDepositAvailableFor(String depositAvailableFor) {
    this.depositAvailableFor = depositAvailableFor;
  }

  @Column(name = "package_id")
  public String getPackageId() {
    return packageId;
  }

  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }
}
