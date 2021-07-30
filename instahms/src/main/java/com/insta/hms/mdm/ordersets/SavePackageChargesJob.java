package com.insta.hms.mdm.ordersets;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.organization.OrganizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author manika.singh
 * @since 25/07/19
 */
@Component
public class SavePackageChargesJob extends GenericJob {

  static Logger logger = LoggerFactory.getLogger(SavePackageChargesJob.class);

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

  @LazyAutowired
  private PackageService packageService;

  @LazyAutowired
  private List<String> bedNames;

  @LazyAutowired
  private List<String> orgIds;

  private int packageId;

  private String action;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public int getPackageId() {
    return packageId;
  }

  public void setPackageId(int packageId) {
    this.packageId = packageId;
  }

  public List<String> getBedNames() {
    return bedNames;
  }

  public void setBedNames(List<String> bedNames) {
    this.bedNames = bedNames;
  }

  public List<String> getOrgIds() {
    return orgIds;
  }

  public void setOrgIds(List<String> orgIds) {
    this.orgIds = orgIds;
  }

  @SuppressWarnings("unchecked")
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
        logger.info("Started inserting default charges for packageId : {}", packageId);
        if (CollectionUtils.isEmpty(orgIds)) {
          orgIds = organizationService.getAllOrgIds();
        }
        if (CollectionUtils.isEmpty(bedNames)) {
          bedNames = bedTypeService.getAllBedTypeNames();
        }
        logger.info("Started inserting respective Master charges for packageId : {}", packageId);
        List<PackageContentsModel> packageContentList =
            (List<PackageContentsModel>) jobContext.getJobDetail().getJobDataMap()
                .get("packageContentList");
        List<PackageContentsModel> deletedPackageContents =
            (List<PackageContentsModel>) jobContext.getJobDetail().getJobDataMap()
                .get("deletedPackageContents");
        List<String> orgIdList =
            (List<String>) jobContext.getJobDetail().getJobDataMap().get("orgIds");
        action = (String) jobContext.getJobDetail().getJobDataMap().get("action");

        //started master charge insertion for the items
        if (CollectionUtils.isNotEmpty(deletedPackageContents)) {
          this.packageChargesService
              .reCalculateAndSavePackageChargeAfterDelete(deletedPackageContents, packageId,
                  orgIdList);
          this.packageService.deletePackageContentsAndCharges(deletedPackageContents);
        }
        if (!packageContentList.isEmpty()) {
          this.packageContentChargesService
              .savePackageContentAndCharge(packageContentList, orgIdList, packageId, action);
        }

        logger.info("Completed inserting default charges for packageId : {}", packageId);
        masterJobScheduler.set("status", "S");
        Map<String, Object> keys = new HashMap<>();
        keys.put("entity_id", packageId + "-" + action);
        masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
      }
    } catch (Exception exception) {
      logger.error("Error while saving package charges for packageId {}", packageId,
          exception);
      masterJobScheduler.set("status", "F");
      masterJobScheduler.set("error_message", exception.getMessage());
      Map<String, Object> keys = new HashMap<>();
      keys.put("entity_id", packageId + "-" + action);
      masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
    }
  }
}
