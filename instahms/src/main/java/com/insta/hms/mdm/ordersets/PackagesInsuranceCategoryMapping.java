package com.insta.hms.mdm.ordersets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author manika.singh
 * @since 10/04/19
 */
@Entity
@Table(name = "packages_insurance_category_mapping")
public class PackagesInsuranceCategoryMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
      "packages_insurance_category_mapping_generator")
  @SequenceGenerator(name = "packages_insurance_category_mapping_generator", sequenceName =
      "packages_insurance_category_mapping_id_seq", allocationSize = 1)
  @Column(name = "packages_insurance_category_mapping_id")
  private int packagesInsuranceCategoryMappingId;

  @Column(name = "package_id", nullable = false)
  private int packageId;

  @Column(name = "insurance_category_id", nullable = false)
  private Integer insuranceCategoryId;

  public Integer getPackageId() {
    return packageId;
  }

  public void setPackageId(Integer packageId) {
    this.packageId = packageId;
  }

  public Integer getInsuranceCategoryId() {
    return insuranceCategoryId;
  }

  public void setInsuranceCategoryId(Integer insuranceCategoryId) {
    this.insuranceCategoryId = insuranceCategoryId;
  }

  public int getId() {
    return packagesInsuranceCategoryMappingId;
  }

  public void setId(int packagesInsuranceCategoryMappingId) {
    this.packagesInsuranceCategoryMappingId = packagesInsuranceCategoryMappingId;
  }
}
