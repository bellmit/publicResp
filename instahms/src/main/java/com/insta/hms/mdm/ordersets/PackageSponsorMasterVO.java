package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * The Class PackageSponsorMasterVO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageSponsorMasterVO {
  
  /** The tpa id. */
  private String tpaId;
  
  /** The tpa name. */
  private String tpaName;
  
  /** The package id. */
  private Integer packageId;
  
  /**
   * Gets the tpa id.
   *
   * @return the tpa id
   */
  public String getTpaId() {
    return tpaId;
  }

  /**
   * Sets the tpa id.
   *
   * @param tpaId the new tpa id
   */
  public void setTpaId(String tpaId) {
    this.tpaId = tpaId;
  }

  /**
   * Gets the tpa name.
   *
   * @return the tpa name
   */
  public String getTpaName() {
    return tpaName;
  }

  /**
   * Sets the tpa name.
   *
   * @param tpaName the new tpa name
   */
  public void setTpaName(String tpaName) {
    this.tpaName = tpaName;
  }

  /**
   * Gets the package id.
   *
   * @return the package id
   */
  public Integer getPackageId() {
    return packageId;
  }

  /**
   * Sets the package id.
   *
   * @param packageId the new package id
   */
  public void setPackageId(Integer packageId) {
    this.packageId = packageId;
  }
}
