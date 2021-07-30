package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * The Class PackageItemSubGroupsModel.
 */
@Entity
@Table(name = "package_item_sub_groups")
public class PackageItemSubGroupsModel implements java.io.Serializable {

  /** The package item sub groups id. */
  private Integer packageItemSubGroupsId;

  /** The package id. */
  private Integer packageId;

  /** The item subgroup id. */
  @JsonProperty("tax_sub_group_id")
  private Integer itemSubgroupId;

  /**
   * Gets the package item sub groups id.
   *
   * @return the package item sub groups id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "sequence_generator_for_package_subgroup_id")
  @SequenceGenerator(name = "sequence_generator_for_package_subgroup_id",
      sequenceName = "package_item_sub_groups_id_seq", allocationSize = 1)
  @Column(name = "package_item_sub_groups_id", unique = true, nullable = false)
  public Integer getPackageItemSubGroupsId() {
    return packageItemSubGroupsId;
  }

  /**
   * Sets the package item sub groups id.
   *
   * @param packageItemSubGroupsId the new package item sub groups id
   */
  public void setPackageItemSubGroupsId(Integer packageItemSubGroupsId) {
    this.packageItemSubGroupsId = packageItemSubGroupsId;
  }

  /**
   * Gets the package id.
   *
   * @return the package id
   */
  @Column(name = "package_id", nullable = false)
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

  /**
   * Gets the item subgroup id.
   *
   * @return the item subgroup id
   */
  @Column(name = "item_subgroup_id", nullable = false)
  public Integer getItemSubgroupId() {
    return itemSubgroupId;
  }

  /**
   * Sets the item subgroup id.
   *
   * @param itemSubgroupId the new item subgroup id
   */
  public void setItemSubgroupId(Integer itemSubgroupId) {
    this.itemSubgroupId = itemSubgroupId;
  }

}
