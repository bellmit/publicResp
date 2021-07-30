package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.BaseJPAService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.ordersets.InsurancePlanApplicabilityVO.PlanApplicabilityType;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class PackagePlanService.
 *
 * @author manika.singh
 * @since 21/06/19
 */
@Service
public class PackagePlanService
    extends BaseJPAService<PackagePlanRepository, PackagePlanMasterModel, Integer> {

  /**
   * Instantiates a new package plan service.
   *
   * @param repository the repository
   */
  @LazyAutowired
  public PackagePlanService(PackagePlanRepository repository) {
    super(repository);
  }

  /** The object mapping service. */
  @LazyAutowired
  private PackagePlanMasterVOMappingService objectMappingService;

  /**
   * Find by package id.
   *
   * @param packageId the package id
   * @return the list
   */
  public List<PackagePlanMasterModel> findByPackageId(Integer packageId) {
    return this.repository.findByPackId(packageId);
  }

  /**
   * Save or update plan mapping.
   *
   * @param packageId the package id
   * @param insurancePlanApplicabilityVO the insurance plan applicability VO
   */
  public void saveOrUpdatePlanMapping(int packageId,
      InsurancePlanApplicabilityVO insurancePlanApplicabilityVO) {
    List<PackagePlanMasterModel> existingPlanMappings = findByPackageId(packageId);
    List<PackagePlanMasterModel> packageSponsorModelsFromVO =
        getPackagePlanModelFromVO(insurancePlanApplicabilityVO, packageId);
    saveOrUpdate(existingPlanMappings, packageSponsorModelsFromVO, true);
  }

  /**
   * Gets the package plan model from VO.
   *
   * @param insurancePlanApplicabilityVO the insurance plan applicability VO
   * @param packageId the package id
   * @return the package plan model from VO
   */
  private List<PackagePlanMasterModel> getPackagePlanModelFromVO(
      InsurancePlanApplicabilityVO insurancePlanApplicabilityVO, int packageId) {
    List<PackagePlanMasterModel> packagePlanMasterModels = new ArrayList<>();
    PlanApplicabilityType planApplicabilityType =
        PlanApplicabilityType.getApplicabilityTypeFromStatus(
            insurancePlanApplicabilityVO.getPlanApplicabilityType());
    List<PackageInsurancePlanMasterVO> planMasterVos =
        insurancePlanApplicabilityVO.getPlanList();
    if (planApplicabilityType == PlanApplicabilityType.ALL_PLANS) {
      packagePlanMasterModels.add(getPlanMasterModel(-1));
    } else if (planApplicabilityType == PlanApplicabilityType.SOME_PLANS) {
      if (CollectionUtils.isNotEmpty(planMasterVos)) {
        packagePlanMasterModels
            .addAll(objectMappingService.convertViewObjectsToModels(planMasterVos));
      }
    } else {
      packagePlanMasterModels.add(getPlanMasterModel(0));
    }
    if (CollectionUtils.isNotEmpty(packagePlanMasterModels)) {
      for (PackagePlanMasterModel packagePlanMasterModel : packagePlanMasterModels) {
        packagePlanMasterModel.setPackId(packageId);
      }
    }
    return packagePlanMasterModels;
  }

  /**
   * Gets the plan master model.
   *
   * @param planId the plan id
   * @return the plan master model
   */
  private PackagePlanMasterModel getPlanMasterModel(Integer planId) {
    PackagePlanMasterModel masterModel = new PackagePlanMasterModel();
    masterModel.setStatus('A');
    masterModel.setPlanId(planId);
    return masterModel;
  }

  /**
   * Gets the package plan applicability VO.
   *
   * @param packageId the package id
   * @return the package plan applicability VO
   */
  public InsurancePlanApplicabilityVO getPackagePlanApplicabilityVO(int packageId) {
    List<PackagePlanMasterModel> planMasterModels = findByPackageId(packageId);
    if (CollectionUtils.isNotEmpty(planMasterModels)) {
      InsurancePlanApplicabilityVO planApplicabilityVO = new InsurancePlanApplicabilityVO();
      for (PackagePlanMasterModel packagePlanMasterModel : planMasterModels) {
        if (packagePlanMasterModel.getPlanId() == 0) {
          planApplicabilityVO
              .setPlanApplicabilityType(PlanApplicabilityType.NO_PLANS.getApplicabilityType());
          break;

        } else if (packagePlanMasterModel.getPlanId() == -1) {
          planApplicabilityVO.setPlanApplicabilityType(
              PlanApplicabilityType.ALL_PLANS.getApplicabilityType());
          break;
        } else {
          planApplicabilityVO.setPlanApplicabilityType(
              PlanApplicabilityType.SOME_PLANS.getApplicabilityType());
        }
      }
      planApplicabilityVO
          .setPlanList(objectMappingService.convertModelsToViewObjects(planMasterModels));
      return planApplicabilityVO;
    }
    return null;
  }

}
