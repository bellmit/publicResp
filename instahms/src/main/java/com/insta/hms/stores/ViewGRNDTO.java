package com.insta.hms.stores;

import java.sql.Date;

public class ViewGRNDTO {

  // view grn variabels
  private String grnNo;

  private String invNo;
  private Date gdate;
  private String status;

  // view stock issue variabels
  private String isNo;
  private String idate;
  private String frStore;
  private String toStore;

  // view Supplier Returns variables
  private String reNo;
  private String redate;

  // view Stock Adjustments variables
  private String adNo;
  private String addate;
  private String storeName;

  // general variables
  private String user;
  private String suppname;

  private String userType;
  private String issuedTo;

  private String transNo;
  private String transDate;

  private String returnedBy;

  public String getReturnedBy() {
    return returnedBy;
  }
  public void setReturnedBy(String returnedBy) {
    this.returnedBy = returnedBy;
  }
  public String getIssuedTo() {
    return issuedTo;
  }
  public void setIssuedTo(String issuedTo) {
    this.issuedTo = issuedTo;
  }
  public String getUserType() {
    return userType;
  }
  public void setUserType(String userType) {
    this.userType = userType;
  }
  public String getIdate() {
    return idate;
  }
  public void setIdate(String idate) {
    this.idate = idate;
  }

  public String getIsNo() {
    return isNo;
  }
  public void setIsNo(String isNo) {
    this.isNo = isNo;
  }

  public String getFrStore() {
    return frStore;
  }
  public void setFrStore(String frStore) {
    this.frStore = frStore;
  }
  public String getToStore() {
    return toStore;
  }
  public void setToStore(String toStore) {
    this.toStore = toStore;
  }
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  public Date getGdate() {
    return gdate;
  }
  public void setGdate(Date gdate) {
    this.gdate = gdate;
  }
  public String getGrnNo() {
    return grnNo;
  }
  public void setGrnNo(String grnNo) {
    this.grnNo = grnNo;
  }
  public String getInvNo() {
    return invNo;
  }
  public void setInvNo(String invNo) {
    this.invNo = invNo;
  }
  public String getSuppname() {
    return suppname;
  }
  public void setSuppname(String suppname) {
    this.suppname = suppname;
  }
  public String getRedate() {
    return redate;
  }
  public void setRedate(String redate) {
    this.redate = redate;
  }
  public String getReNo() {
    return reNo;
  }
  public void setReNo(String reNo) {
    this.reNo = reNo;
  }
  public String getAddate() {
    return addate;
  }
  public void setAddate(String addate) {
    this.addate = addate;
  }
  public String getAdNo() {
    return adNo;
  }
  public void setAdNo(String adNo) {
    this.adNo = adNo;
  }
  public String getStoreName() {
    return storeName;
  }
  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }
  public String getTransDate() {
    return transDate;
  }
  public void setTransDate(String transDate) {
    this.transDate = transDate;
  }
  public String getTransNo() {
    return transNo;
  }
  public void setTransNo(String transNo) {
    this.transNo = transNo;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

}
