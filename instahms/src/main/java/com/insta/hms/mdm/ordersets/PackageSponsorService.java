package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.ordersets.SponsorPackageApplicabilityVO.SponsorApplicabilityType;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class PackageSponsorService.
 *
 * @author manika.singh
 * @since 13/06/19
 */
@Service
public class PackageSponsorService
    extends BaseJPAService<PackageSponsorJpaRepository, PackageSponsorMasterModel, Integer> {

  /** The Constant STATUS_ACTIVE. */
  private static final Character STATUS_ACTIVE = 'A';

  /** The Constant APPLICABLE_TO_ALL_TPA. */
  private static final String APPLICABLE_TO_ALL_TPA = "-1";

  /** The Constant APPLICABLE_TO_NO_TPA. */
  private static final String APPLICABLE_TO_NO_TPA = "0";

  /**
   * Instantiates a new package sponsor service.
   *
   * @param repository the repository
   */
  @LazyAutowired
  public PackageSponsorService(PackageSponsorJpaRepository repository) {
    super(repository);
  }

  /** The object mapper service. */
  @LazyAutowired
  private PackageSponsorMasterVOMappingService objectMapperService;

  /**
   * Find by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<PackageSponsorMasterModel> findByPackageId(Integer packageId) {
    return this.repository.findByPackId(packageId);
  }

  /**
   * Find all VO by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<PackageSponsorMasterVO> findAllVOByPackageId(Integer packageId) {
    List<PackageSponsorMasterModel> deptPackageApplicabilityModels =
        this.repository.findByPackId(packageId);
    return objectMapperService.convertModelsToViewObjects(deptPackageApplicabilityModels);
  }

  /**
   * Gets the sponsor package applicability VO.
   *
   * @param packageId the package id
   * @return the sponsor package applicability VO
   */
  public SponsorPackageApplicabilityVO getSponsorPackageApplicabilityVO(Integer packageId) {
    List<PackageSponsorMasterVO> sponsorMasterVObjects = this.findAllVOByPackageId(packageId);
    return objectMapperService.constructSponsorPackageApplicabilityVO(sponsorMasterVObjects);
  }

  /**
   * Gets the package sponsor model from VO.
   *
   * @param sponsorPackageApplicabilityVO the sponsor package applicability VO
   * @param packageId the package id
   * @return the package sponsor model from VO
   */
  public List<PackageSponsorMasterModel> getPackageSponsorModelFromVO(
      SponsorPackageApplicabilityVO sponsorPackageApplicabilityVO, Integer packageId) {
    List<PackageSponsorMasterModel> resultSponsorMasterModels = new ArrayList<>();
    List<PackageSponsorMasterVO> sponsorList = sponsorPackageApplicabilityVO.getSponsorList();
    SponsorApplicabilityType applicabilityType =
        SponsorApplicabilityType.getApplicabilityTypeFromStatus(
            sponsorPackageApplicabilityVO.getSponsorApplicabilityType());
    if (applicabilityType == SponsorApplicabilityType.ALL_SPONSORS) {
      resultSponsorMasterModels.add(getSponsorMasterModel(APPLICABLE_TO_ALL_TPA));
    } else if (applicabilityType == SponsorApplicabilityType.SOME_SPONSORS) {
      resultSponsorMasterModels
          .addAll(objectMapperService.convertViewObjectsToModels(sponsorList));
    } else {
      resultSponsorMasterModels.add(getSponsorMasterModel(APPLICABLE_TO_NO_TPA));
    }
    for (PackageSponsorMasterModel masterModel : resultSponsorMasterModels) {
      masterModel.setPackId(packageId);
    }
    return resultSponsorMasterModels;

  }

  /**
   * Construct all sponsor model object.
   *
   * @param packageId the package id
   * @return the package sponsor master model
   */
  private PackageSponsorMasterModel getSponsorMasterModel(String tpaId) {
    PackageSponsorMasterModel packageSponsorMasterModel = new PackageSponsorMasterModel();
    packageSponsorMasterModel.setStatus(STATUS_ACTIVE);
    packageSponsorMasterModel.setTpaId(tpaId);
    return packageSponsorMasterModel;
  }

  /**
   * Save or update sponsor mapping.
   *
   * @param packageId the package id
   * @param sponsorPackageApplicability the sponsor package applicability
   */
  public void saveOrUpdateSponsorMapping(int packageId,
      SponsorPackageApplicabilityVO sponsorPackageApplicability) {
    List<PackageSponsorMasterModel> existingSponsorMappings = findByPackageId(packageId);
    List<PackageSponsorMasterModel> packageSponsorModelsFromVO =
        getPackageSponsorModelFromVO(sponsorPackageApplicability, packageId);
    saveOrUpdate(existingSponsorMappings, packageSponsorModelsFromVO, true);
  }
}
