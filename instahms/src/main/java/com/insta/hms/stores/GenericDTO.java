package com.insta.hms.stores;

public class GenericDTO {

  private String gmaster_name;
  private String genCode;
  private String status;
  private String operation;
  private String classification_id;
  private String sub_classification_id;
  private String standard_adult_dose;
  private String criticality;
  private String classificationName;
  private String sub_ClassificationName;
  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }
  public String getGmaster_name() {
    return gmaster_name;
  }
  public void setGmaster_name(String gmaster_name) {
    this.gmaster_name = gmaster_name;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getGenCode() {
    return genCode;
  }
  public void setGenCode(String genCode) {
    this.genCode = genCode;
  }
  public String getClassification_id() {
    return classification_id;
  }
  public void setClassification_id(String classification_id) {
    this.classification_id = classification_id;
  }
  public String getCriticality() {
    return criticality;
  }
  public void setCriticality(String criticality) {
    this.criticality = criticality;
  }
  public String getStandard_adult_dose() {
    return standard_adult_dose;
  }
  public void setStandard_adult_dose(String standard_adult_dose) {
    this.standard_adult_dose = standard_adult_dose;
  }
  public String getSub_classification_id() {
    return sub_classification_id;
  }
  public void setSub_classification_id(String sub_classification_id) {
    this.sub_classification_id = sub_classification_id;
  }
  public String getClassificationName() {
    return classificationName;
  }
  public void setClassificationName(String classificationName) {
    this.classificationName = classificationName;
  }
  public String getSub_classificationName() {
    return sub_ClassificationName;
  }
  public void setSub_ClassificationName(String sub_ClassificationName) {
    this.sub_ClassificationName = sub_ClassificationName;
  }

}
