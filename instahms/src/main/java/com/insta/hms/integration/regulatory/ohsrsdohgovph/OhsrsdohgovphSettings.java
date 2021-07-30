package com.insta.hms.integration.regulatory.ohsrsdohgovph;

public class OhsrsdohgovphSettings {

  private boolean trainingMode = false;
  private String hfhudCode;
  private String webserviceKey;
  
  public boolean isTrainingMode() {
    return trainingMode;
  }
  
  public void setTrainingMode(boolean trainingMode) {
    this.trainingMode = trainingMode;
  }

  public String getHfhudCode() {
    return hfhudCode;
  }
  
  public void setHfhudCode(String hfhudCode) {
    this.hfhudCode = hfhudCode;
  }

  public String getWebserviceKey() {
    return webserviceKey;
  }
  
  public void setWebserviceKey(String webserviceKey) {
    this.webserviceKey = webserviceKey;
  }
}
