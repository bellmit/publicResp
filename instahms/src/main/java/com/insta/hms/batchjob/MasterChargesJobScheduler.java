package com.insta.hms.batchjob;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

public class MasterChargesJobScheduler extends SQLUpdateJob {

  /** The Constant log. */
  private static final Logger log = Logger.getLogger(MasterChargesJobScheduler.class);

  /** The Master Charge Cron Scheduler Details Repository repository. */
  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  /*
   * insert charges to corresponding masters.
   */
  private static final String INSERT_CHARGES_QUERY1 = "INSERT INTO ";

  private static final String INSERT_CHARGES_OPEN_BRACE = "(";

  private static final String INSERT_CHARGES_CLOSE_BRACE = ")";

  private static final String INSERT_CHARGES_QUERY2 = " (SELECT ";

  private static final String BED_TYPES_COUNT_QUERY = " SELECT count(distinct bed_type)::INTEGER ";

  private static final String INSERT_CHARGES_QUERY3 = " FROM all_beds_orgs_view abov ";
  /*
   * insert charges to corresponding masters.
   */
  private static final String WITH_OUT_GENERAL = " WHERE NOT (abov.bed_type = 'GENERAL')";

  @Override
  public void executeInternal(JobExecutionContext context) throws JobExecutionException {
    setJobConnectionDetails();
    JobDataMap jobData = context.getJobDetail().getJobDataMap();
    // column field name:username or user_name
    Map<String, Object> queryParamsMap = (Map<String, Object>) jobData.get("query_params");

    String tableName = (String) jobData.get("table_name");
    if (tableName == null || !queryParamsMap.containsValue("abov.bed_type")
        || !queryParamsMap.containsValue("abov.org_id")) {
      log.error("MasterChargeLog: tablename or bedtype ot org_id not found");
      throw new JobExecutionException("MasterChargeLog: tablename or bedtype ot org_id not found");
    }
    String columnNames = StringUtils.join(queryParamsMap.keySet(), ",");
    String insertQuery = INSERT_CHARGES_QUERY1 + tableName + INSERT_CHARGES_OPEN_BRACE + columnNames
        + INSERT_CHARGES_CLOSE_BRACE + INSERT_CHARGES_QUERY2;
    Object[] values = new Object[queryParamsMap.size() - 2];
    String[] col = new String[queryParamsMap.size()];
    int colIndex = 0;
    int entryIdx = 0;
    for (Map.Entry<String, Object> ent : queryParamsMap.entrySet()) {
      if ("abov.bed_type".equals(ent.getValue())) {
        col[colIndex] = "abov.bed_type";
      } else if ("abov.org_id".equals(ent.getValue())) {
        col[colIndex] = "abov.org_id";
      } else {
        col[colIndex] = "?";
        values[entryIdx] = ent.getValue();
        entryIdx++;
      }
      colIndex++;
    }
    String selectColumn = StringUtils.join(col, ",");
    insertQuery = insertQuery + selectColumn + INSERT_CHARGES_QUERY3;
    String generalConcat = WITH_OUT_GENERAL;
    Boolean withGeneral = jobData.get("with_general") != null
        ? (Boolean) jobData.get("with_general")
        : false;
    if (withGeneral) {
      generalConcat = "";
    }
    insertQuery = insertQuery + generalConcat + INSERT_CHARGES_CLOSE_BRACE;
    BasicDynaBean jobScheduleData = null;
    Map<String, Object> updateKey = new HashMap<String, Object>();
    if (jobData.get("entity") != null) {
      Map<String, Object> searchMap = new HashMap<String, Object>();
      searchMap.put("entity", jobData.get("entity"));
      searchMap.put("entity_id", jobData.get("entity_id"));
      jobScheduleData = masterChargesCronSchedulerDetailsRepository.findByKey(searchMap);
      updateKey.put("id", jobScheduleData.get("id"));
    }
    String bedTypeCount = BED_TYPES_COUNT_QUERY + INSERT_CHARGES_QUERY3;
    int bedCount = DatabaseHelper.getInteger(bedTypeCount);
    if (bedCount <= 1 && !withGeneral) {
      if (jobScheduleData != null) {
        jobScheduleData.set("status", "S");
        masterChargesCronSchedulerDetailsRepository.update(jobScheduleData, updateKey);
      }
      log.error("MasterChargeLog: Only one bed type available");
      throw new JobExecutionException("MasterChargeLog: Only one bed type available");
    }
    try {
      int chargeInsert = DatabaseHelper.insert(insertQuery, 300, values);
      if (chargeInsert > 0 && bedCount >= 1) {
        if (jobScheduleData != null) {
          jobScheduleData.set("status", "S");
          masterChargesCronSchedulerDetailsRepository.update(jobScheduleData, updateKey);
        }
        log.error("MasterChargeLog: " + tableName + " Charges are successfully inserted");
      } else {
        if (jobScheduleData != null && bedCount >= 1) {
          jobScheduleData.set("status", "F");
          jobScheduleData.set("error_message", "Charges are not inserted");
          masterChargesCronSchedulerDetailsRepository.update(jobScheduleData, updateKey);
        }
        log.error("MasterChargeLog: " + tableName + " Charges are not inserted");
      }
    } catch (Exception ex) {
      if (jobScheduleData != null && bedCount >= 1) {
        jobScheduleData.set("status", "F");
        jobScheduleData.set("error_message", "Charges Not iserted due to SQL error");
        masterChargesCronSchedulerDetailsRepository.update(jobScheduleData, updateKey);
      }
      log.error("MasterChargeLog: " + tableName + " Charges are not inserted" + ex.getMessage());
    }
  }
}
