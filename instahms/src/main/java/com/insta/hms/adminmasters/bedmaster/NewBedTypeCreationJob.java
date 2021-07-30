package com.insta.hms.adminmasters.bedmaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class NewBedTypeCreationJob.
 *
 * @author eshwar-chandra
 * @since 09/06/20
 */
public class NewBedTypeCreationJob extends GenericJob {

  /**
   * The logger.
   */
  private static Logger logger = LoggerFactory.getLogger(NewBedTypeCreationJob.class);
  BedMasterBO bedMasterBO = new BedMasterBO();
  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;
  /**
   * The al.
   */
  private ArrayList<BedDetails> al;

  /**
   * The baseBedForCharges.
   */
  private String baseBedForCharges;

  /**
   * The variance type.
   */
  private String varianceType;

  /**
   * The variance by.
   */
  private Double varianceBy;

  /**
   * The variance value.
   */
  private Double varianceValue;

  /**
   * The use value.
   */
  private boolean useValue;

  /**
   * The nearest round off value.
   */
  private Double nearestRoundOffValue;

  /**
   * The user name.
   */
  private String userName;

  /**
   * The isIcuCategory.
   */
  private String isIcuCategory;

  /**
   * The bed Type.
   */
  private String bedType;

  /**
   * The status.
   */
  private boolean status;

  /*
   * Creates new bed type as a background job.
   *
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    BasicDynaBean masterJobScheduler = masterChargesCronSchedulerDetailsRepository.getBean();
    setJobConnectionDetails();
    Connection con = null;
    try {
      if (jobContext != null && jobContext.getJobDetail() != null
          && jobContext.getJobDetail().getJobDataMap() != null) {
        String schema = (String) jobContext.getJobDetail().getJobDataMap().get("schema");
        this.setSchema(schema);
        bedType = (String) jobContext.getJobDetail().getJobDataMap().get("bedType");
        logger.info("Started inserting default charges for bedType : {}", bedType);
        masterJobScheduler.set("status", "P");
        masterJobScheduler.set("entity", "NewBedType");
        masterJobScheduler.set("entity_id", (String) bedType);
        masterJobScheduler.set("charge", BigDecimal.ZERO);
        masterJobScheduler.set("discount", BigDecimal.ZERO);
        masterChargesCronSchedulerDetailsRepository.insert(masterJobScheduler);

        JobDataMap jobDatamap = jobContext.getJobDetail().getJobDataMap();
        al = (ArrayList<BedDetails>) jobDatamap.get("al");
        baseBedForCharges = jobDatamap.getString("base_bed_for_charges");
        varianceType = jobDatamap.getString("variance_type");
        varianceBy = jobDatamap.getDouble("varianceBy");
        varianceValue = jobDatamap.getDouble("varianceValue");
        useValue = jobDatamap.getBooleanValue("useValue");
        nearestRoundOffValue = jobDatamap.getDoubleValue("nearestRoundOffValue");
        userName = jobDatamap.getString("userName");
        isIcuCategory = jobDatamap.getString("isIcuCategory");

        con = DataBaseUtil.getConnection((Integer) EnvironmentUtil.getDatabaseQueryTimeout() * 2);
        con.setAutoCommit(false);

        logger.info("Started inserting bedType charges : {}", bedType);
        status = bedMasterBO
            .addNewBedCharges(con, al, baseBedForCharges, varianceType, varianceBy,
                varianceValue,
                useValue, nearestRoundOffValue, userName, isIcuCategory);
        GenericDAO rateDao = new GenericDAO("priority_rate_sheet_parameters_view");
        List<BasicDynaBean> ratePlanList = rateDao.listAll();
        if (ratePlanList.size() > 0) {
          for (int i = 0; i < ratePlanList.size(); i++) {
            BasicDynaBean bean = ratePlanList.get(i);
            String ratePlanId = (String) bean.get("org_id");
            Double variance = new Double((Integer) bean.get("rate_variation_percent"));
            Double roundOff = new Double((Integer) bean.get("round_off_amount"));
            String rateSheetId = (String) bean.get("base_rate_sheet_id");
            status = bedMasterBO
                .updateBedForBillingMasters(con, ratePlanId, rateSheetId, variance, roundOff,
                    bedType);
          }
        }
        logger.info("Completed inserting default charges for bed type : {}", bedType);
        masterJobScheduler.set("status", "S");
        Map<String, Object> keys = new HashMap<>();
        keys.put("entity_id", bedType);
        masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
      }
    } catch (Exception exception) {
      logger.error("Error while saving bed type charges for bedType {} , error : {}", bedType,
          exception.getMessage());
      masterJobScheduler.set("status", "F");
      masterJobScheduler.set("error_message", exception.getMessage());
      Map<String, Object> keys = new HashMap<>();
      keys.put("entity_id", bedType);
      masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, keys);
    } finally {
      try {
        DataBaseUtil.commitClose(con, status);
      } catch (SQLException sqle) {
        logger.error("error while commiting the bed_job connection", sqle);
      }
    }

  }

}
