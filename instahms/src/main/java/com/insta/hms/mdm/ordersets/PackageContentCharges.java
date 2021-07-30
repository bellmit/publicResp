package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageContentCharges.
 *
 * @author manika.singh
 * @since 15/04/19
 */
@Entity
@Table(name = "package_content_charges")
public class PackageContentCharges {

  /** The content charge id. */
  private int contentChargeId;

  /** The package content id. */
  private int packageContentId;

  /** The org id. */
  private String orgId;

  /** The bed type. */
  private String bedType;

  /** The charge. */
  private BigDecimal charge;

  /** The discount. */
  private BigDecimal discount;

  /** The is override. */
  private String isOverride;

  private String createdBy;
  @JsonFormat(pattern = "dd-mm-yyyy hh:mm:ss")
  private Date createdAt;
  private String modifiedBy;
  @JsonFormat(pattern = "dd-mm-yyyy hh:mm:ss")
  private Date modifiedAt;
  
  /**
   * Instantiates a new package content charges.
   */
  public PackageContentCharges() {

  }

  /**
   * Instantiates a new package content charges.
   *
   * @param packageContentId the package content id
   * @param orgId the org id
   * @param bedType the bed type
   * @param charge the charge
   * @param discount the discount
   * @param isOverride the is override
   */
  public PackageContentCharges(int packageContentId, String orgId, String bedType,
                               BigDecimal charge, BigDecimal discount, String isOverride) {
    this.packageContentId = packageContentId;
    this.orgId = orgId;
    this.bedType = bedType;
    this.charge = charge;
    this.discount = discount;
    this.isOverride = isOverride;
  }

  /**
   * Gets the content charge id.
   *
   * @return the content charge id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
      "package_content_charges_generator")
  @SequenceGenerator(name = "package_content_charges_generator", sequenceName =
      "package_content_charges_seq", allocationSize = 1)
  @Column(name = "content_charge_id", unique = true, nullable = false)
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
  @Column(name = "package_content_id")
  public int getPackageContentId() {
    return packageContentId;
  }

  /**
   * Sets the package content id.
   *
   * @param packageContentsModel the new package content id
   */
  public void setPackageContentId(int packageContentsModel) {
    this.packageContentId = packageContentsModel;
  }

  /**
   * Gets the org id.
   *
   * @return the org id
   */
  @Column(name = "org_id", nullable = false)
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the org id.
   *
   * @param orgId the new org id
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  /**
   * Gets the bed type.
   *
   * @return the bed type
   */
  @Column(name = "bed_type")
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
  @Column(name = "charge", precision = 15)
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
  @Column(name = "discount", precision = 15)
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
  @Column(name = "is_override", length = 1)
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

  @Column(name = "created_at")
  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Column(name = "modified_by")
  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  @Column(name = "modified_at")
  public Date getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Date modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  @Column(name = "created_by")
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  class CompareByBedType implements Comparator<PackageContentCharges> {
    public int compare(PackageContentCharges p1, PackageContentCharges p2) {
      if (p1.bedType != null && p2.bedType != null) {
        if (p1.bedType.compareTo(p2.bedType) > 0) {
          return 1;
        }
        if (p1.bedType.compareTo(p2.bedType) < 0) {
          return -1;
        }
        return 0;
      }
      return -1;
    }
  }
}
