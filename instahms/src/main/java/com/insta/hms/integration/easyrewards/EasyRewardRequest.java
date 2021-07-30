package com.insta.hms.integration.easyrewards;

/**
 * The values will be passed from the UI side.
 */
public class EasyRewardRequest {

  private String mobileNumber;
  private String userName;
  private String centerId;
  private String billNumber;

  // Internal Variables used in the backend code
  private String token;

  public String getMobileNumber() {
    return mobileNumber;
  }

  public void setMobileNumber(String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getCenterId() {
    return centerId;
  }

  public void setCenterId(String centerId) {
    this.centerId = centerId;
  }

  public String getBillNumber() {
    return billNumber;
  }

  public void setBillNumber(String billNumber) {
    this.billNumber = billNumber;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

}
