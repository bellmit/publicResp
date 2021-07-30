package com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * The Class PackagesDTO.
 *
 * @author manika.singh
 * @since 10/04/19
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackagesDTO {

  /** The packages model. */
  private PackagesModel packagesModel;

  /** The insurance category ids. */
  private List<Integer> insuranceCategoryIds;

  /** The package contents models. */
  private List<PackageContentsModel> packageContentsModels;

  /** The appliable center ids. */
  private List<Integer> appliableCenterIds;

  /** The center package applicability. */
  private List<CenterPackageApplicabilityVO> centerPackageApplicability;

  /** The package tax applicability. */
  private List<PackageItemSubGroupsVO> packageTaxApplicability;

  /** The department package applicability. */
  private List<DeptPackageApplicabilityVO> departmentPackageApplicability;

  /** The sponsor package applicability. */
  private SponsorPackageApplicabilityVO sponsorPackageApplicability;

  /** The insurance plan applicability VO. */
  private InsurancePlanApplicabilityVO insurancePlanApplicability;

  /** The applicable rate plans (org ids). */
  private List<String> ratePlanApplicability;
  
  /**
   * Gets the package contents models.
   *
   * @return the package contents models
   */
  public List<PackageContentsModel> getPackageContentsModels() {
    return packageContentsModels;
  }

  /**
   * Sets the package contents models.
   *
   * @param packageContentsModels the new package contents models
   */
  public void setPackageContentsModels(List<PackageContentsModel> packageContentsModels) {
    this.packageContentsModels = packageContentsModels;
  }

  /**
   * Gets the packages model.
   *
   * @return the packages model
   */
  public PackagesModel getPackagesModel() {
    return packagesModel;
  }

  /**
   * Sets the packages model.
   *
   * @param packagesModel the new packages model
   */
  public void setPackagesModel(PackagesModel packagesModel) {
    this.packagesModel = packagesModel;
  }

  /**
   * Gets the insurance category ids.
   *
   * @return the insurance category ids
   */
  public List<Integer> getInsuranceCategoryIds() {
    return insuranceCategoryIds;
  }

  /**
   * Sets the insurance category ids.
   *
   * @param insuranceCategoryIds the new insurance category ids
   */
  public void setInsuranceCategoryIds(List<Integer> insuranceCategoryIds) {
    this.insuranceCategoryIds = insuranceCategoryIds;
  }

  /**
   * Gets the center ids.
   *
   * @return the center ids
   */
  public List<Integer> getCenterIds() {
    return appliableCenterIds;
  }

  /**
   * Sets the center ids.
   *
   * @param centerIds the new center ids
   */
  public void setCenterIds(List<Integer> centerIds) {
    this.appliableCenterIds = centerIds;
  }

  /**
   * Gets the package tax applicability.
   *
   * @return the package tax applicability
   */
  public List<PackageItemSubGroupsVO> getPackageTaxApplicability() {
    return packageTaxApplicability;
  }

  /**
   * Sets the package tax applicability.
   *
   * @param packageTaxApplicability the new package tax applicability
   */
  public void setPackageTaxApplicability(
      List<PackageItemSubGroupsVO> packageTaxApplicability) {
    this.packageTaxApplicability = packageTaxApplicability;
  }

  /**
   * Gets the department package applicability.
   *
   * @return the department package applicability
   */
  public List<DeptPackageApplicabilityVO> getDepartmentPackageApplicability() {
    return departmentPackageApplicability;
  }

  /**
   * Sets the department package applicability.
   *
   * @param departmentPackageApplicability the new department package applicability
   */
  public void setDepartmentPackageApplicability(
      List<DeptPackageApplicabilityVO> departmentPackageApplicability) {
    this.departmentPackageApplicability = departmentPackageApplicability;
  }

  /**
   * Gets the center package applicability.
   *
   * @return the center package applicability
   */
  public List<CenterPackageApplicabilityVO> getCenterPackageApplicability() {
    return centerPackageApplicability;
  }

  /**
   * Sets the center package applicability.
   *
   * @param centerPackageApplicability the new center package applicability
   */
  public void setCenterPackageApplicability(
      List<CenterPackageApplicabilityVO> centerPackageApplicability) {
    this.centerPackageApplicability = centerPackageApplicability;
  }

  /**
   * Gets the sponsor package applicability.
   *
   * @return the sponsor package applicability
   */
  public SponsorPackageApplicabilityVO getSponsorPackageApplicability() {
    return sponsorPackageApplicability;
  }

  /**
   * Sets the sponsor package applicability.
   *
   * @param sponsorPackageApplicability the new sponsor package applicability
   */
  public void setSponsorPackageApplicability(
      SponsorPackageApplicabilityVO sponsorPackageApplicability) {
    this.sponsorPackageApplicability = sponsorPackageApplicability;
  }

  /**
   * Gets the insurance plan applicability VO.
   *
   * @return the insurance plan applicability VO
   */
  public InsurancePlanApplicabilityVO getInsurancePlanApplicability() {
    return insurancePlanApplicability;
  }

  /**
   * Sets the insurance plan applicability VO.
   *
   * @param insurancePlanApplicability the new insurance plan applicability
   */
  public void setInsurancePlanApplicability(
      InsurancePlanApplicabilityVO insurancePlanApplicability) {
    this.insurancePlanApplicability = insurancePlanApplicability;
  }

  /**
   * Gets the list of applicable rate plans.
   *
   * @return the list of rate plans
   */
  public List<String> getRatePlanApplicability() {
    return ratePlanApplicability;
  }

  /**
   * Sets the list of applicable rate plans.
   *
   * @param ratePlanApplicability list of rate plans
   */
  public void setRatePlanApplicability(List<String> ratePlanApplicability) {
    this.ratePlanApplicability = ratePlanApplicability;
  }

}
