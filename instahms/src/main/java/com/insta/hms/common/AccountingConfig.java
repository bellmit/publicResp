package com.insta.hms.common;

public class AccountingConfig {

  private String creditNoteVoucherType;
  private Boolean postSeparateDepositRefund;
  private String voucherSubTypeForDeposit;
  private String voucherSubTypeForDepositSettlement; 
  private String voucherSubTypeForDepositRefund;
  private String voucherSubTypeForBillAdvance;
  private String voucherSubTypeForBillSettlement;
  private String voucherSubTypeForBillRefund;
  private String voucherSubTypeForSponsorAdvance;
  private String voucherSubTypeForSponsorSettlement;
  private String debitAccTypeForTDS;
  
  public String getCreditNoteVoucherType() {
    return creditNoteVoucherType;
  }

  public void setCreditNoteVoucherType(String creditNoteVoucherType) {
    this.creditNoteVoucherType = creditNoteVoucherType;
  }
  
  public Boolean getPostSeparateDepositRefund() {
    return postSeparateDepositRefund;
  }

  public void setPostSeparateDepositRefund(Boolean postSeparateDepositRefund) {
    this.postSeparateDepositRefund = postSeparateDepositRefund;
  }

  public String getVoucherSubTypeForDeposit() {
    return voucherSubTypeForDeposit;
  }

  public void setVoucherSubTypeForDeposit(String voucherSubTypeForDeposit) {
    this.voucherSubTypeForDeposit = voucherSubTypeForDeposit;
  }
  
  public String getVoucherSubTypeForDepositSettlement() {
    return voucherSubTypeForDepositSettlement;
  }

  public void setVoucherSubTypeForDepositSettlement(String voucherSubTypeForDepositSettlement) {
    this.voucherSubTypeForDepositSettlement = voucherSubTypeForDepositSettlement;
  }
  
  public String getVoucherSubTypeForBillAdvance() {
    return voucherSubTypeForBillAdvance;
  }

  public void setVoucherSubTypeForBillAdvance(String voucherSubTypeForBillAdvance) {
    this.voucherSubTypeForBillAdvance = voucherSubTypeForBillAdvance;
  }

  public String getVoucherSubTypeForBillSettlement() {
    return voucherSubTypeForBillSettlement;
  }

  public void setVoucherSubTypeForBillSettlement(String voucherSubTypeForBillSettlement) {
    this.voucherSubTypeForBillSettlement = voucherSubTypeForBillSettlement;
  }

  public String getVoucherSubTypeForSponsorAdvance() {
    return voucherSubTypeForSponsorAdvance;
  }

  public void setVoucherSubTypeForSponsorAdvance(String voucherSubTypeForSponsorAdvance) {
    this.voucherSubTypeForSponsorAdvance = voucherSubTypeForSponsorAdvance;
  }

  public String getVoucherSubTypeForSponsorSettlement() {
    return voucherSubTypeForSponsorSettlement;
  }

  public void setVoucherSubTypeForSponsorSettlement(String voucherSubTypeForSponsorSettlement) {
    this.voucherSubTypeForSponsorSettlement = voucherSubTypeForSponsorSettlement;
  }
  
  public String getVoucherSubTypeForDepositRefund() {
    return voucherSubTypeForDepositRefund;
  }

  public void setVoucherSubTypeForDepositRefund(String voucherSubTypeForDepositRefund) {
    this.voucherSubTypeForDepositRefund = voucherSubTypeForDepositRefund;
  }

  public String getVoucherSubTypeForBillRefund() {
    return voucherSubTypeForBillRefund;
  }

  public void setVoucherSubTypeForBillRefund(String voucherSubTypeForBillRefund) {
    this.voucherSubTypeForBillRefund = voucherSubTypeForBillRefund;
  }

  public String getDebitAccTypeForTDS() {
    return debitAccTypeForTDS;
  }

  public void setDebitAccTypeForTDS(String debitAccTypeForTDS) {
    this.debitAccTypeForTDS = debitAccTypeForTDS;
  }

}
