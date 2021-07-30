package com.bob.hms.adminmasters.organization;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.ClaimGenerationJob;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class RateSheetSchedulerDetailsDAO.
 */
public class RateSheetSchedulerDetailsDAO extends GenericDAO {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ClaimGenerationJob.class);

  /**
   * Instantiates a new rate sheet scheduler details DAO.
   */
  public RateSheetSchedulerDetailsDAO() {
    super("rate_sheet_creation_scheduler_details");
  }

  /** The Constant GET_RECENT_RATESHEET_CREATION_DETAILS. */
  private static final String GET_RECENT_RATESHEET_CREATION_DETAILS = "SELECT * from"
      + " rate_sheet_creation_scheduler_details "
      + "WHERE scheduled_timestamp >  current_timestamp - interval '1 day'"
      + " ORDER BY scheduled_timestamp DESC";

  /**
   * Gets the recent rate sheet scheduler details.
   *
   * @return the recent rate sheet scheduler details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getRecentRateSheetSchedulerDetails() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_RECENT_RATESHEET_CREATION_DETAILS);
  }

  /**
   * Insert scheduled ratesheet creation job.
   *
   * @param schedulerSeqId the scheduler seq id
   * @param status         the status
   * @param errorMessage   the error message
   * @param orgId          the org id
   * @return true, if successful
   */
  public boolean updateScheduledRatesheetCreationJob(Integer schedulerSeqId, boolean status,
      String errorMessage, String orgId) {
    Map<String, Integer> keyMap = new HashMap<>();
    keyMap.put("id", schedulerSeqId);
    Map<String, String> rateSheetUpdateDataMap = new HashMap<>();
    rateSheetUpdateDataMap.put("org_id", orgId);
    rateSheetUpdateDataMap.put("status", status ? "S" : "F");
    rateSheetUpdateDataMap.put("error_message", errorMessage);
    try (Connection con = DataBaseUtil.getConnection()) {
      return DataBaseUtil.dynaUpdate(con, this.getTable(), rateSheetUpdateDataMap, keyMap) == 1;
    } catch (SQLException sqlException) {
      logger.error("Unable to update the rate sheet job details ", sqlException);
    }
    return false;
  }

  /**
   * Insert scheduled job details.
   *
   * @param orgId        the org id
   * @param orgName      the org name
   * @param status       the status
   * @param errorMessage the error message
   * @return true, if successful
   */
  public boolean insertScheduledJobDetails(String orgId, String orgName, String status,
      String errorMessage) {
    Map<String, String> rateSheetInsertDataMap = new HashMap<>();
    rateSheetInsertDataMap.put("org_name", orgName);
    rateSheetInsertDataMap.put("org_id", orgId);
    rateSheetInsertDataMap.put("status", status);
    rateSheetInsertDataMap.put("error_message", errorMessage);
    try (Connection con = DataBaseUtil.getConnection()) {
      return DataBaseUtil.dynaInsert(con, getTable(), rateSheetInsertDataMap);
    } catch (SQLException sqlException) {
      logger.error("Unable to insert the rate sheet job details ", sqlException);
    }
    return false;
  }

  /** The Constant GET_CURRENT_JOB_SEQ_ID. */
  private static final String GET_CURRENT_JOB_SEQ_ID = "SELECT last_value"
      + " FROM rate_sheet_creation_scheduler_id_seq";

  /**
   * Gets the current scheduled job seq id.
   *
   * @return the current scheduled job seq id
   * @throws SQLException the SQL exception
   */
  public Integer getCurrentScheduledJobSeqId() throws SQLException {
    return DataBaseUtil.getIntValueFromDb(GET_CURRENT_JOB_SEQ_ID);
  }

}
