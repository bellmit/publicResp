package com.insta.hms.mdm.ordersets;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.organization.OrganizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@DisallowConcurrentExecution
public class UpdateDerivedPackageChargesJob extends GenericJob {

  static Logger logger = LoggerFactory.getLogger(UpdateDerivedPackageChargesJob.class);

  @LazyAutowired
  private PackageChargesService packageChargesService;

  @LazyAutowired
  private PackageContentChargesService packageContentChargesService;

  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  @LazyAutowired
  private BedTypeService bedTypeService;

  @LazyAutowired
  private OrganizationService organizationService;

  private String orgId;
  private int packageId;
  private String username;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  private static final String ENTITY_NAME = "EditPackageRate";

  public int getPackageId() {
    return packageId;
  }

  public void setPackageId(int packageId) {
    this.packageId = packageId;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public static String getEntityName() {
    return ENTITY_NAME;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    BasicDynaBean masterJobScheduler = masterChargesCronSchedulerDetailsRepository.getBean();
    try {
      if (jobContext != null && jobContext.getJobDetail() != null
          && jobContext.getJobDetail().getJobDataMap() != null) {
        String schema = (String) jobContext.getJobDetail().getJobDataMap().get("schema");
        this.setSchema(schema);
        RequestContext
            .setConnectionDetails(new String[] {null, null, schema, "_system"});
        packageId = (int) jobContext.getJobDetail().getJobDataMap().get("packageId");
        logger.info("Started updating derived rate-plan charges for packageId : {} - {}", packageId,
            orgId);
        this.packageChargesService.updateDerivedRatePlanPackageCharges(packageId, orgId, username);
        logger.info("Completed updating derived rate-plan charges for packageId : {}", packageId);
        masterJobScheduler.set("status", "S");
        Map<String, Object> keys = new HashMap<>();
        keys.put("entity_id", String.valueOf(packageId));
        masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
      }
    } catch (Exception exception) {
      logger.error(
          "Error while updating derived rate-plan charges for packageId " + packageId + " - "
              + orgId,
          exception);
      masterJobScheduler.set("status", "F");
      masterJobScheduler.set("error_message", exception.getMessage());
      Map<String, Object> keys = new HashMap<>();
      keys.put("entity", getEntityName());
      keys.put("entity_id", String.valueOf(packageId));
      masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
      throw exception;
    }
  }
}
