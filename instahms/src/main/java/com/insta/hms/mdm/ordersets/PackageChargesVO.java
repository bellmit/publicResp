package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

/**
 * @author manika.singh
 * @since 22/04/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageChargesVO {

  private int packageChargeId;
  private String bedType;
  private BigDecimal charge;
  private BigDecimal discount;
  private String isOverride;
  private String orgId;

  public int getPackageChargeId() {
    return packageChargeId;
  }

  public void setPackageChargeId(int packageChargeId) {
    this.packageChargeId = packageChargeId;
  }

  public String getBedType() {
    return bedType;
  }

  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  public BigDecimal getCharge() {
    return charge;
  }

  public void setCharge(BigDecimal charge) {
    this.charge = charge;
  }

  public BigDecimal getDiscount() {
    return discount;
  }

  public void setDiscount(BigDecimal discount) {
    this.discount = discount;
  }

  public String getIsOverride() {
    return isOverride;
  }

  public void setIsOverride(String isOverride) {
    this.isOverride = isOverride;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }
}
