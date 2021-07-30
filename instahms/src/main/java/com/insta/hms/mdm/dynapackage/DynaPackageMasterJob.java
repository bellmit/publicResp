package com.insta.hms.mdm.dynapackage;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class NewBedTypeCreationJob.
 *
 * @author eshwar-chandra
 * @since 15/12/20
 */
@Component
public class DynaPackageMasterJob extends GenericJob {

  /**
   * The logger.
   */
  private static Logger logger = LoggerFactory.getLogger(DynaPackageMasterJob.class);
  /**
   * The package name.
   */
  private String packageName;
  /**
   * The processKey.
   */
  private String processKey;
  /**
   * The variance type.
   */
  private int dynaPackageId;

  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;
  
  @LazyAutowired
  private DynaPackageService dynaPackageService;

  /**
   * Execute internal.
   *
   * @param jobContext the job context
   * @throws JobExecutionException the job execution exception
   */
  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    BasicDynaBean masterJobScheduler = masterChargesCronSchedulerDetailsRepository.getBean();
    setJobConnectionDetails();
    try {
      if (jobContext != null && jobContext.getJobDetail() != null
          && jobContext.getJobDetail().getJobDataMap() != null) {

        String schema = (String) jobContext.getJobDetail().getJobDataMap().get("schema");
        this.setSchema(schema);
        processKey = (String) jobContext.getJobDetail().getJobDataMap().get("processKey");
        packageName = (String) jobContext.getJobDetail().getJobDataMap().get("dynaPackageName");
        dynaPackageId = (int) jobContext.getJobDetail().getJobDataMap().getInt("dynaPackageId");
        logger.info("Started inserting / updating DynaPackage : {}", packageName);
        masterJobScheduler.set("status", "P");
        masterJobScheduler.set("entity", processKey);
        masterJobScheduler.set("entity_id", "DP-" + dynaPackageId);
        masterJobScheduler.set("charge", BigDecimal.ZERO);
        masterJobScheduler.set("discount", BigDecimal.ZERO);
        masterChargesCronSchedulerDetailsRepository.insert(masterJobScheduler);
        
        logger.info("started inserting / updating DynaPackage: {}", packageName);

        dynaPackageService.createOrUpdateDynaPackage(jobContext);
        
        logger.info("Completed inserting / updating DynaPackage: {}", packageName);
        
        masterJobScheduler.set("status", "S");
        Map<String, Object> keys = new HashMap<>();
        keys.put("entity_id", "DP-" + dynaPackageId);
        keys.put("entity", processKey);
        masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);

      }
    } catch (Exception exception) {
      logger.error("Error while inserting / updating DynaPackage {} , error : {}", packageName,
          exception.getMessage());
      masterJobScheduler.set("status", "F");
      masterJobScheduler.set("error_message", exception.getMessage());
      Map<String, Object> keys = new HashMap<>();
      keys.put("entity_id", "DP-" + dynaPackageId);
      masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
    }

  }

}
