package com.insta.hms.diagnosticsmasters.outhousemaster;

public class OutHouseMaster {

  private String ohId;
  private int outsourceDestId;
  private String testId;
  private Double charge;
  private String ohName;
  private String testName;
  private String status;
  private String templateName;
  private String cliaNo;
  private String ohAddress;

  public String getOhAddress() {
    return ohAddress;
  }

  public void setOhAddress(String ohAddress) {
    this.ohAddress = ohAddress;
  }

  public String getCliaNo() {
    return cliaNo;
  }

  public void setCliaNo(String cliaNo) {
    this.cliaNo = cliaNo;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getOhName() {
    return ohName;
  }

  public void setOhName(String ohName) {
    this.ohName = ohName;
  }

  public String getTestName() {
    return testName;
  }

  public void setTestName(String testName) {
    this.testName = testName;
  }

  public Double getCharge() {
    return charge;
  }

  public void setCharge(Double charge) {
    this.charge = charge;
  }

  public String getOhId() {
    return ohId;
  }

  public void setOhId(String ohId) {
    this.ohId = ohId;
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public String getTemplate_name() {
    return templateName;
  }

  public void setTemplate_name(String templateName) {
    this.templateName = templateName;
  }

  public int getOutsourceDestId() {
    return outsourceDestId;
  }

  public void setOutsourceDestId(int outsourceDestId) {
    this.outsourceDestId = outsourceDestId;
  }

}
