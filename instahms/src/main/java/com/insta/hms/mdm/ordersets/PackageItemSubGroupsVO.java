package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageItemSubGroupsVO {
  private String typeTax;
  private Integer taxGroupId;
  private Integer taxSubGroupId;

  public String getTypeTax() {
    return typeTax;
  }

  public void setTypeTax(String typeTax) {
    this.typeTax = typeTax;
  }

  public Integer getTaxSubGroupId() {
    return taxSubGroupId;
  }

  public void setTaxSubGroupId(Integer taxSubGroupId) {
    this.taxSubGroupId = taxSubGroupId;
  }

  public Integer getTaxGroupId() {
    return taxGroupId;
  }

  public void setTaxGroupId(Integer taxGroupId) {
    this.taxGroupId = taxGroupId;
  }

}
