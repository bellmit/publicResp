package com.insta.hms.billing;

import com.bob.hms.common.RequestContext;
import com.insta.hms.core.insurance.ClaimProcessor;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimGenerationJob.
 */
public class ClaimGenerationJob extends GenericJob {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(ClaimGenerationJob.class);

  /** The submitdao. */
  ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();

  /** The alternate obs codes. */
  InternalCodeObservationProcessor alternateObsCodes = new InternalCodeObservationProcessor();

  /** The claim activity processor. */
  ClaimActivityProcessor claimActivityProcessor = new ClaimActivityProcessor();

  /** The helper. */
  ClaimGeneratorHelper helper = new ClaimGeneratorHelper();

  /** The submission batch id. */
  private String submission_batch_id;

  /** The testing. */
  private String testing;

  /** The path. */
  private String path;

  /** The center id. */
  private String centerId;

  /** The url rights map. */
  private HashMap urlRightsMap;

  /** The action url map. */
  private HashMap actionUrlMap;

  /** The claim processor. */
  @Autowired
  ClaimProcessor claimProcessor;

  /**
   * Gets the submission batch id.
   *
   * @return the submission batch id
   */
  public String getSubmission_batch_id() {
    return submission_batch_id;
  }

  /**
   * Sets the submission batch id.
   *
   * @param submission_batch_id the new submission batch id
   */
  public void setSubmission_batch_id(String submission_batch_id) {
    this.submission_batch_id = submission_batch_id;
  }

  /**
   * Gets the testing.
   *
   * @return the testing
   */
  public String getTesting() {
    return testing;
  }

  /**
   * Sets the testing.
   *
   * @param testing the new testing
   */
  public void setTesting(String testing) {
    this.testing = testing;
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path.
   *
   * @param path the new path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Gets the center id.
   *
   * @return the center id
   */
  public String getCenterId() {
    return centerId;
  }

  /**
   * Sets the center id.
   *
   * @param centerId the new center id
   */
  public void setCenterId(String centerId) {
    this.centerId = centerId;
  }

  /**
   * Gets the url rights map.
   *
   * @return the url rights map
   */
  public HashMap getUrlRightsMap() {
    return urlRightsMap;
  }

  /**
   * Sets the url rights map.
   *
   * @param urlRightsMap the new url rights map
   */
  public void setUrlRightsMap(HashMap urlRightsMap) {
    this.urlRightsMap = urlRightsMap;
  }

  /**
   * Gets the action url map.
   *
   * @return the action url map
   */
  public HashMap getActionUrlMap() {
    return actionUrlMap;
  }

  /**
   * Sets the action url map.
   *
   * @param actionUrlMap the new action url map
   */
  public void setActionUrlMap(HashMap actionUrlMap) {
    this.actionUrlMap = actionUrlMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.
   * JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    String schema = getSchema();
    RequestContext
        .setConnectionDetails(new String[] {null, null, schema, "_system", getCenterId()});
    HashMap rightsUrlMap = getUrlRightsMap();
    HashMap urlActionMap = getActionUrlMap();
    boolean isAccumed = false;
    boolean isNewClaim = false;
    if (jobContext != null && jobContext.getJobDetail() != null
        && jobContext.getJobDetail().getJobDataMap() != null) {
      if (jobContext.getJobDetail().getJobDataMap().containsKey("isAccumed")) {
        isAccumed = jobContext.getJobDetail().getJobDataMap().getBooleanValue("isAccumed");
      }
      if (jobContext.getJobDetail().getJobDataMap().containsKey("isNewClaim")) {
        isNewClaim = jobContext.getJobDetail().getJobDataMap().getBooleanValue("isNewClaim");
      }
    }

    BasicDynaBean submissionbean = null;
    try {
      submissionbean = submitdao.findByKey("submission_batch_id", submission_batch_id);
      String isResubmission = submissionbean.get("is_resubmission") != null
          ? (String) submissionbean.get("is_resubmission") : "N";
      Integer batchCenterId = (Integer) submissionbean.get("center_id");
      BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", batchCenterId);
      if (null == centerbean) {
        throw new Exception("Unknown Center.");
      }
      String healthAuthority = (String) centerbean.get("health_authority");
      if (isAccumed && !"DHA".equals(healthAuthority)) {
        setAccumedHealthAuthorityError(submissionbean);
        return;
      }
      if (healthAuthority.equals("DHA") || healthAuthority.equals("HAAD")) {
        // To:do use migrated spring code to call alternate code processing
        logger.debug("Alternate code processing begins");
        alternateObsCodes.process(submission_batch_id);
        logger.debug("Alternate code processing ends");
        // To:do use migrated spring code to do activity code processing
        claimActivityProcessor.process(submission_batch_id, isResubmission, healthAuthority);
        logger.debug("Activity Code processing ends");
        claimProcessor.process(submission_batch_id, healthAuthority, urlActionMap,
            rightsUrlMap, path, testing.equals("Y"), isAccumed, batchCenterId, isNewClaim);
        logger.debug("Error processing ends");
      } else {
        String haError = "Center configured health authority is not valid.";
        logger.error(haError);
        // set claim generation procession status to Failed.
        submissionbean.set("processing_status", "F");

        submissionbean.set("processing_error", haError);
        setClaimGenerationJobStatus(submissionbean);
      }

    } catch (Exception e) {
      try {
        // set claim generation procession status to Failed.
        submissionbean.set("processing_status", "F");
        submissionbean.set("processing_error", e.getMessage());
        setClaimGenerationJobStatus(submissionbean);
      } catch (SQLException | IOException e1) {
        logger.error("Exception while updating failed status when generating Claim XML" + e);
      }
      logger.error("Exception in ClaimGenerationJob", e);
      throw new JobExecutionException(e);
    }
  }


  /** The Constant accumedHealthAuthorityError. */
  private static final String accumedHealthAuthorityError =
      "Accumed claims cannot be generated for health authorities other than DHA";
  
  /**
   * Sets the accumed health authority error.
   *
   * @param submissionbean the new accumed health authority error
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void setAccumedHealthAuthorityError(BasicDynaBean submissionbean)
      throws SQLException, IOException {
    submissionbean.set("processing_status", "F");
    submissionbean.set("processing_error", accumedHealthAuthorityError);
    setClaimGenerationJobStatus(submissionbean);
  }

  /**
   * Sets the claim generation job status.
   *
   * @param submissionbean the new claim generation job status
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void setClaimGenerationJobStatus(BasicDynaBean submissionbean)
      throws SQLException, IOException {

    Map<String, Object> keys = new HashMap<>();
    keys.put("submission_batch_id", submission_batch_id);
    submitdao.update(submissionbean.getMap(), keys);
  }

}
