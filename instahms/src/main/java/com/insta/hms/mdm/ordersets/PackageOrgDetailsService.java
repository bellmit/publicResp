package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.model.PackOrgDetailsIdSequence;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author manika.singh
 * @since 23/04/19
 */
@Service
public class PackageOrgDetailsService extends
    BaseJPAService<PackageOrgDetailsJpaRepository, PackOrgDetailsModel, PackOrgDetailsIdSequence> {

  @LazyAutowired
  OrganizationService organizationService;

  @LazyAutowired
  public PackageOrgDetailsService(PackageOrgDetailsJpaRepository repository) {
    super(repository);
  }

  /**
   * Find By Id.
   * 
   * @param packageId package identifier
   * @param orgId organization identifier
   * @return instance of PackOrgDetailsModel
   */
  public PackOrgDetailsModel findByPackOrgDetailsIdSequence(Integer packageId, String orgId) {
    PackOrgDetailsIdSequence packOrgDetailsIdSequence =
        new PackOrgDetailsIdSequence(packageId, orgId);
    return this.repository.findById(packOrgDetailsIdSequence);
  }

  /**
   * Find list of package organization details for given packageId.
   *
   * @param packageId package identifier
   * @return list of  PackOrgDetailsModel
   */
  public List<String> findOrgIdsByPackageId(Integer packageId) {
    return this.repository.findOrgIdsByPackageId(packageId);
  }

  /**
   * Save or update package org details.
   *
   * @param packageId the package id
   * @param newlyApplicableOrgIdList the org id list for which the package applicability
   *     is newly added
   * @param deletedOrgIdList the org id list for which the package applicability
   *     is removed
   */
  public void saveOrUpdatePackageOrgDetails(int packageId, List<String> newlyApplicableOrgIdList,
      List<String> deletedOrgIdList) {
    if (deletedOrgIdList.size() != 0) {
      this.repository
      .removeByIdPackageIdAndIdOrgIdIn(packageId, deletedOrgIdList);
    }
    if (newlyApplicableOrgIdList.size() == 0) {
      return;
    }
    List<BasicDynaBean> organisations = this.organizationService
        .getRatePlanList(newlyApplicableOrgIdList);
    PackOrgDetailsModel packOrgDetailsModel;
    List<PackOrgDetailsModel> packOrgDetails = new ArrayList<>();
    for (BasicDynaBean org : organisations) {
      PackOrgDetailsIdSequence packOrgDetailsIdSequence = new PackOrgDetailsIdSequence();
      packOrgDetailsIdSequence.setPackageId(packageId);
      packOrgDetailsIdSequence.setOrgId((String) org.get("org_id"));
      packOrgDetailsModel = new PackOrgDetailsModel(packOrgDetailsIdSequence);
      packOrgDetailsModel.setBaseRateSheetId((String) org.get("base_rate_sheet_id"));
      packOrgDetailsModel.setApplicable(true);
      packOrgDetails.add(packOrgDetailsModel);
    }
    this.saveAll(packOrgDetails, false);
  }

}
