package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.AbstractViewObjectMapper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.packages.PlanDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

/**
 * The Class PackagePlanMasterVOMappingService.
 */
@Service
public class PackagePlanMasterVOMappingService
    extends AbstractViewObjectMapper<PackagePlanMasterModel, PackageInsurancePlanMasterVO> {

  /** The Constant planDAO. */
  private static final PlanDAO planDAO = new PlanDAO();

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.AbstractViewObjectMapper#convertModelToViewObject(java.lang.Object)
   */
  @Override
  public PackageInsurancePlanMasterVO convertModelToViewObject(PackagePlanMasterModel modelObj)
      throws Exception {
    if (modelObj != null && modelObj.getPlanId() != 0 && modelObj.getPlanId() != -1) {
      return getPlanDetailsFromPlanId(modelObj.getPlanId());
    }
    return null;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.common.AbstractViewObjectMapper#convertViewObjectToModel(java.lang.Object)
   */
  @Override
  public PackagePlanMasterModel convertViewObjectToModel(PackageInsurancePlanMasterVO viewObj)
      throws Exception {
    if (viewObj != null) {
      PackagePlanMasterModel packagePlanMasterModel = new PackagePlanMasterModel();
      packagePlanMasterModel.setPlanId(viewObj.getPlanId());
      packagePlanMasterModel.setStatus('A');
      return packagePlanMasterModel;
    }
    return null;
  }

  /**
   * Gets the plan details from plan id.
   *
   * @param planId the plan id
   * @return the plan details from plan id
   * @throws SQLException the SQL exception
   */
  public PackageInsurancePlanMasterVO getPlanDetailsFromPlanId(Integer planId)
      throws SQLException {
    if (planId > 0) {
      BasicDynaBean planDetails = planDAO.getPlanDetails(planId);
      if (planDetails != null) {
        PackageInsurancePlanMasterVO insurancePlanMasterVO =
            new PackageInsurancePlanMasterVO();
        insurancePlanMasterVO.setPlanId(planId);
        insurancePlanMasterVO.setPlanName((String) planDetails.get("plan_name"));
        insurancePlanMasterVO
            .setInsuranceCategoryName((String) planDetails.get("category_name"));
        insurancePlanMasterVO
            .setInsuranceCompanyName((String) planDetails.get("insurance_co_name"));
        return insurancePlanMasterVO;
      }
    }
    return null;
  }

}
