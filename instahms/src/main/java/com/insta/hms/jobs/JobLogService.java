package com.insta.hms.jobs;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * provide service for job_log table.
 *
 * @author yashwant
 */
@Service
public class JobLogService {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(JobLogService.class);

  /**
   * To maintain status in job_log table.
   *
   * @author yashwant
   */
  public enum JobStatus {

    /** The scheduled. */
    SCHEDULED("Job Scheduled", 1),
    /** The started. */
    STARTED("Job Started", 2),
    /** The success. */
    SUCCESS("Job Finished", 3),
    /** The failure. */
    FAILURE("Job Failed", 4);

    /** The status description. */
    private final String statusDescription;

    /** The status code. */
    private final int statusCode;

    /**
     * Instantiates a new job status.
     *
     * @param statusDescription
     *          the status description
     * @param statusCode
     *          the status code
     */
    JobStatus(String statusDescription, int statusCode) {
      this.statusDescription = statusDescription;
      this.statusCode = statusCode;
    }

    /**
     * Gets the status description.
     *
     * @return the status description
     */
    public String getStatusDescription() {
      return statusDescription;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
      return statusCode;
    }
  }

  private static final String JOBLOGID = "jobLogId";
  /** The job log repository. */
  @LazyAutowired
  private JobLogRepository jobLogRepository;

  /** The cron job service. */
  @LazyAutowired
  private CronJobService cronJobService;

  /**
   * Will insert one tow in job_log table.
   *
   * @param context
   *          the context
   */
  @Transactional
  public void insertJobstatus(JobExecutionContext context) {
    JobDetail jobDetail = context.getJobDetail();
    String schema = (String) jobDetail.getJobDataMap().get("schema");
    if (null == schema || schema.isEmpty()) {
      logger.warn(
          "job_log table will not insert start time because schema is missing "
          + "(May be job is not for schema specific) : "
              + jobDetail.getKey());
      return;
    }
    String[] dbSchema = new String[] { null, "", schema, "_system", "", "" };
    RequestContext.setConnectionDetails(dbSchema);
    String jobName = jobDetail.getKey().getName();
    String jobGroup = jobDetail.getKey().getGroup();
    BasicDynaBean bean = jobLogRepository.getBean();
    Integer jobLogId = jobLogRepository.getNextSequence();
    bean.set(JobLogRepository.JOB_START_TIME, new Timestamp(new Date().getTime()));
    bean.set(JobLogRepository.JOB_LOG_ID, jobLogId);
    bean.set(JobLogRepository.JOB_STATUS, JobStatus.STARTED.toString());
    bean.set(JobLogRepository.STATUS_DESC, 
        JobStatus.STARTED.getStatusDescription() + " In " + schema);
    bean.set(JobLogRepository.JOB_NAME, jobName);
    bean.set(JobLogRepository.JOB_GROUP, jobGroup);
    int status = jobLogRepository.insert(bean);
    if (status != 0) {
      jobDetail.getJobDataMap().put(JOBLOGID, jobLogId);
    }
    logger.debug("Inserted status : JobLogId :" + jobLogId + ", schema :" + schema + ", job_key"
        + jobDetail.getKey());
  }

  /**
   * It will update the status in job_log table. When job runs.
   *
   * @param jec
   *          the jec
   * @param jobStatus
   *          the job status
   * @param jee
   *          the jee
   */
  @Transactional
  public void updateEndTime(JobExecutionContext jec, JobStatus jobStatus,
      JobExecutionException jee) {
    JobDetail jobDetail = jec.getJobDetail();
    String schema = (String) jobDetail.getJobDataMap().get("schema");
    if (null == schema || schema.isEmpty()) {
      logger.warn(
          "job_log table will not update end time because schema is missing "
          + "(May be job is not for schema specific) : "
              + jobDetail.getKey());
      return;
    }
    String[] dbSchema = new String[] { null, "", schema, "", "", "" };
    RequestContext.setConnectionDetails(dbSchema);
    BasicDynaBean bean = jobLogRepository.getBean();
    Timestamp jonEndTime = new Timestamp(new Date().getTime());
    bean.set("job_end_time", jonEndTime);
    if (JobStatus.SUCCESS.equals(jobStatus)) {
      bean.set("job_status", jobStatus.toString());
      bean.set("status_desc", jobStatus.getStatusDescription() + " In " + schema);
    } else if (JobStatus.FAILURE.equals(jobStatus)) {
      bean.set("job_status", jobStatus.toString());
      bean.set("status_desc",
          jobStatus.getStatusDescription() + " In " + schema + " :: " + jee.getMessage());
    }
    Map<String, Object> key = new HashMap<>();
    key.put("job_log_id", jobDetail.getJobDataMap().get(JOBLOGID));
    jobLogRepository.update(bean, key);
    cronJobService.updateLastRunStatus(jobStatus, jonEndTime, jobDetail);
    logger.debug("Updated status : JobLogId :" + jobDetail.getJobDataMap().get(JOBLOGID)
        + ", schema :" + schema + ", job_key" + jobDetail.getKey());
  }

  /**
   * List job_log table content.
   *
   * @param columns
   *          the columns
   * @param filterMap
   *          the filter map
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listJobLog(List<String> columns, Map<String, Object> filterMap,
      String sortColumn) {
    return jobLogRepository.listAll(columns, filterMap, sortColumn);
  }
}
