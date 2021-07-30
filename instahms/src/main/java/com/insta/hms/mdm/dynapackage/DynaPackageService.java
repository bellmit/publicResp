package com.insta.hms.mdm.dynapackage;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.rateplan.RatePlanParametersService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Class DynaPackageService.
 */
@Service
public class DynaPackageService extends MasterService {

  @LazyAutowired
  private DynaPackageOrgDetailsService dynaPackageOrgDetailsService;

  @LazyAutowired
  private DynaPackageChargesService dynaPackageChargesService;

  @LazyAutowired
  private DynaPackageCategoryLimitsService dynaPackageCategoryLimitsService;

  @LazyAutowired
  private RatePlanParametersService ratePlanParametersService;

  /**
   * Instantiates a new dyna package service.
   *
   * @param repo      the repo
   * @param validator the validator
   */
  public DynaPackageService(DynaPackageRepository repo, DynaPackageValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the dyna package details.
   *
   * @param dynaPkgId the dyna pkg id
   * @param ratePlan  the rate plan
   * @return the dyna package details
   */
  public BasicDynaBean getDynaPackageDetails(int dynaPkgId, String ratePlan) {
    return ((DynaPackageRepository) this.getRepository())
        .getDynaPackageDetails(dynaPkgId, ratePlan);
  }

  /**
   * Gets the dyna package charges.
   *
   * @param ratePlan  the rate plan
   * @param bedType   the bed type
   * @param dynaPkgId the dyna pkg id
   * @return the dyna package charges
   */
  public List<BasicDynaBean> getDynaPackageCharges(String ratePlan, String bedType, int dynaPkgId) {
    return ((DynaPackageRepository) this.getRepository())
        .getDynaPackageCharges(ratePlan, bedType, dynaPkgId);
  }

  /**
   * Schedule dyna package creation.
   *
   * @param jobData the job data
   */
  public void scheduleDynaPackageCreation(Map<String, Object> jobData) {
    JobService jobService = JobSchedulingService.getJobService();
    jobService.scheduleImmediate(
        buildJob("DynaPackageMasterJob-" + jobData.get("dynaPackageId"), DynaPackageMasterJob.class,
            jobData));
  }

  /**
   * Creates/update the or update dyna package.
   *
   * @param jobContext the job context
   * @throws Exception the exception
   */
  @Transactional(rollbackFor = Exception.class)
  public void createOrUpdateDynaPackage(JobExecutionContext jobContext) throws Exception {
    String processKey = (String) jobContext.getJobDetail().getJobDataMap().get("processKey");
    int dynaPackageId = (int) jobContext.getJobDetail().getJobDataMap().getInt("dynaPackageId");

    if (processKey.equals("NewDynaPKG")) {

      JobDataMap jobDatamap = jobContext.getJobDetail().getJobDataMap();
      dynaPackageId = jobDatamap.getInt("dynaPackageId");
      String userName = (String) jobContext.getJobDetail().getJobDataMap().get("userName");

      dynaPackageOrgDetailsService.copyDynaPackageDetailsToAllOrgs(dynaPackageId);
      dynaPackageChargesService.copyGeneralChargesToAllOrgs(dynaPackageId);
      dynaPackageCategoryLimitsService.copyGeneralChargesToAllOrgs(dynaPackageId, userName);

      OrgMasterDao orgDao = new OrgMasterDao();
      List<BasicDynaBean> ratePlans = orgDao.getRatePlanList();
      String dynaPackId = Integer.toString(dynaPackageId);

      dynaPackageChargesService
          .updateApplicableflagForDerivedRatePlans(ratePlans, "dynapackages",
              "dyna_package_id", dynaPackId, "dyna_package_org_details", null);

    } else {

      String[] derivedRateplanIds =
          (String[]) jobContext.getJobDetail().getJobDataMap().get("derivedRatePlanIds");
      String[] ratePlanApplicable =
          (String[]) jobContext.getJobDetail().getJobDataMap().get("ratePlanapplicable");
      BasicDynaBean orgDetails =
          (BasicDynaBean) jobContext.getJobDetail().getJobDataMap().get("orgDetails");
      String[] categoryIds =
          (String[]) jobContext.getJobDetail().getJobDataMap().get("categoryIds");
      dynaPackageOrgDetailsService
          .updateOrgForDerivedRatePlans(derivedRateplanIds, ratePlanApplicable,
              Integer.toString(dynaPackageId));
      dynaPackageChargesService.updatedynaPackChargesForDerivedRatePlans(derivedRateplanIds,
          (String) orgDetails.get("org_id"), Integer.toString(dynaPackageId),
          ratePlanApplicable);
      if (categoryIds.length > 0) {
        dynaPackageCategoryLimitsService
            .updateCategoryLimitsForDerivedRatePlans(derivedRateplanIds,
                (String) orgDetails.get("org_id"), Integer.toString(dynaPackageId),
                categoryIds);
      }

      List<BasicDynaBean> allDerivedRatePlanIds =
          ratePlanParametersService.getDerivedRatePlanIds((String) orgDetails.get("org_id"));
      if (null != allDerivedRatePlanIds) {
        dynaPackageChargesService
            .updateApplicableflagForDerivedRatePlans(allDerivedRatePlanIds, "dynapackages",
                "dyna_package_id",
                Integer.toString(dynaPackageId), "dyna_package_org_details",
                (String) orgDetails.get("org_id"));
      }
    }
  }

}
