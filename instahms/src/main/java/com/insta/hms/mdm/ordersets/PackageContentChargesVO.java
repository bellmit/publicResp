package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

/**
 * The Class PackageContentChargesVO.
 *
 * @author manika.singh
 * @since 25/04/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageContentChargesVO {
  
  /**
   * Instantiates a new package content charges VO.
   */
  public PackageContentChargesVO() {
    // Default constructor
  }

  /** The content charge id. */
  private int contentChargeId;
  
  /** The package content id. */
  private int packageContentId;
  
  /** The package content name. */
  private String packageContentName;
  
  /** The bed type. */
  private String bedType;
  
  /** The charge. */
  private BigDecimal charge;
  
  /** The discount. */
  private BigDecimal discount;
  
  /** The is override. */
  private String isOverride;
  
  /** The content quantity. */
  private Integer contentQuantity;
  
  /** The activity id. */
  private String activityId;
  
  /** The charge head. */
  private String chargeHead;

  /** The panel id. */
  private Integer panelId;

  /**
   * Gets the content charge id.
   *
   * @return the content charge id
   */
  public int getContentChargeId() {
    return contentChargeId;
  }

  /**
   * Sets the content charge id.
   *
   * @param contentChargeId the new content charge id
   */
  public void setContentChargeId(int contentChargeId) {
    this.contentChargeId = contentChargeId;
  }

  /**
   * Gets the package content id.
   *
   * @return the package content id
   */
  public int getPackageContentId() {
    return packageContentId;
  }

  /**
   * Sets the package content id.
   *
   * @param packageContentId the new package content id
   */
  public void setPackageContentId(int packageContentId) {
    this.packageContentId = packageContentId;
  }

  /**
   * Gets the package content name.
   *
   * @return the package content name
   */
  public String getPackageContentName() {
    return packageContentName;
  }

  /**
   * Sets the package content name.
   *
   * @param packageContentName the new package content name
   */
  public void setPackageContentName(String packageContentName) {
    this.packageContentName = packageContentName;
  }

  /**
   * Gets the bed type.
   *
   * @return the bed type
   */
  public String getBedType() {
    return bedType;
  }

  /**
   * Sets the bed type.
   *
   * @param bedType the new bed type
   */
  public void setBedType(String bedType) {
    this.bedType = bedType;
  }

  /**
   * Gets the charge.
   *
   * @return the charge
   */
  public BigDecimal getCharge() {
    return charge;
  }

  /**
   * Sets the charge.
   *
   * @param charge the new charge
   */
  public void setCharge(BigDecimal charge) {
    this.charge = charge;
  }

  /**
   * Gets the discount.
   *
   * @return the discount
   */
  public BigDecimal getDiscount() {
    return discount;
  }

  /**
   * Sets the discount.
   *
   * @param discount the new discount
   */
  public void setDiscount(BigDecimal discount) {
    this.discount = discount;
  }

  /**
   * Gets the checks if is override.
   *
   * @return the checks if is override
   */
  public String getIsOverride() {
    return isOverride;
  }

  /**
   * Sets the checks if is override.
   *
   * @param isOverride the new checks if is override
   */
  public void setIsOverride(String isOverride) {
    this.isOverride = isOverride;
  }

  /**
   * Gets the content quantity.
   *
   * @return the content quantity
   */
  public Integer getContentQuantity() {
    return contentQuantity;
  }

  /**
   * Sets the content quantity.
   *
   * @param contentQuantity the new content quantity
   */
  public void setContentQuantity(Integer contentQuantity) {
    this.contentQuantity = contentQuantity;
  }

  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }


  /**
   * Gets the charge head.
   *
   * @return the charge head
   */
  public String getChargeHead() {
    return chargeHead;
  }

  /**
   * Sets the charge head.
   *
   * @param chargeHead the new charge head
   */
  public void setChargeHead(String chargeHead) {
    this.chargeHead = chargeHead;
  }

  /**
   * Gets the panel id.
   *
   * @return the panel id
   */
  public Integer getPanelId() {
    return panelId;
  }

  /**
   * Sets the panel id.
   *
   * @param panelId the panel Id
   */
  public void setPanelId(Integer panelId) {
    this.panelId = panelId;
  }


  /**
   * Instantiates a new package content charges VO.
   *
   * @param contentChargeId the content charge id
   * @param packageContentId the package content id
   * @param packageContentName the package content name
   * @param bedType the bed type
   * @param charge the charge
   * @param discount the discount
   * @param isOverride the is override
   * @param contentQuantity the content quantity
   * @param activityId the activity id
   * @param chargeHead the charge head
   */
  public PackageContentChargesVO(int contentChargeId, int packageContentId,
                                 String packageContentName, String bedType, BigDecimal charge,
                                 BigDecimal discount, String isOverride, Integer contentQuantity,
                                 String activityId, String chargeHead) {
    this.contentChargeId = contentChargeId;
    this.packageContentId = packageContentId;
    this.packageContentName = packageContentName;
    this.bedType = bedType;
    this.charge = charge;
    this.discount = discount;
    this.isOverride = isOverride;
    this.contentQuantity = contentQuantity;
    this.activityId = activityId;
    this.chargeHead = chargeHead;
  }
}
