package com.bob.hms.adminmasters.services;

public class Service {
  /*
   * For service no deptId.only department name only look at:services_departments table
   */

  private String serviceId;
  private String serviceName;
  private String deptId;
  private String units;
  private Double tax;
  private String serviceCode;
  private String status;
  private double serviceCharge;
  private boolean conductionApplicable;
  private Double discount;

  public boolean getConduction_applicable() {
    return conductionApplicable;
  }

  public void setConduction_applicable(boolean conductionApplicable) {
    this.conductionApplicable = conductionApplicable;
  }

  public String getDeptId() {
    return deptId;
  }

  public void setDeptId(String deptId) {
    this.deptId = deptId;
  }

  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Double getTax() {
    return tax;
  }

  public void setTax(Double tax) {
    this.tax = tax;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public double getServiceCharge() {
    return serviceCharge;
  }

  public void setServiceCharge(double serviceCharge) {
    this.serviceCharge = serviceCharge;
  }

  public Double getDiscount() {
    return discount;
  }

  public void setDiscount(Double discount) {
    this.discount = discount;
  }

}
