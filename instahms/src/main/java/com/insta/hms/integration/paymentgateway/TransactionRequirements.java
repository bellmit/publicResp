package com.insta.hms.integration.paymentgateway;

public class TransactionRequirements {

  private float amount;
  private String billNumber;

  public float getAmount() {
    return amount;
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public String getBillNumber() {
    return billNumber;
  }

  public void setBillNumber(String billNumber) {
    this.billNumber = billNumber;
  }

}
