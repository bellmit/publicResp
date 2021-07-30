package com.insta.hms.jobs;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;
import static com.insta.hms.jobs.common.QuartzJobHelper.buildTrigger;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.MasterChargesJobScheduler;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.common.JobMap;
import com.insta.hms.jobs.common.QuartzJobHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CronJobService.
 */
@Service
public class CronJobService {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(CronJobService.class);

  /** The cron job repository. */
  @LazyAutowired
  private CronJobRepository cronJobRepository;

  /** The Master Charge Cron Scheduler Details Repository repository. */
  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /**
   * Gets the all scheduled job.
   *
   * @return the all scheduled job
   */
  public List<BasicDynaBean> getAllScheduledJob() {
    return cronJobRepository.listAll("job_id");
  }

  /**
   * Update.
   *
   * @param bean
   *          the bean
   * @param keys
   *          the keys
   * @return the int
   */
  @Transactional
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    /* Re-scheduling the job */
    String jobName = (String) bean.get(CronJobRepository.JOB_NAME);
    String jobGroup = RequestContext.getSchema() + "_"
        + (String) bean.get(CronJobRepository.JOB_GROUP);
    String jobTime = (String) bean.get(CronJobRepository.JOB_TIME);
    String status = (String) bean.get(CronJobRepository.JOB_STATUS);
    Map<String, Object> jobData = new HashMap<>();
    if (bean.get(CronJobRepository.JOB_PARAMS) != null) {
      jobData.put("params", bean.get(CronJobRepository.JOB_PARAMS));
    }
    jobData.put("schema", RequestContext.getSchema());
    if (status.equalsIgnoreCase("I")) {
      QuartzJobHelper.validateCronExpression(jobTime);
      jobService.deleteJob(buildJob(jobName, jobGroup, JobMap.JOB.get(jobName), jobData));
    }
    jobService.scheduleAt(buildJob(jobName, jobGroup, JobMap.JOB.get(jobName), jobData),
        buildTrigger(jobName, jobGroup, jobTime));

    return cronJobRepository.update(bean, keys);

  }

  /**
   * To bean.
   *
   * @param requestParams
   *          the request params
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    List<String> errorFields = new ArrayList<>();
    BasicDynaBean bean = cronJobRepository.getBean();
    ConversionUtils.copyToDynaBean(requestParams, bean, errorFields);
    return bean;
  }

  /**
   * Update last run status.
   *
   * @param jobStatus
   *          the job status
   * @param jobEndTime
   *          the job end time
   * @param jobDetail
   *          the job detail
   */
  public void updateLastRunStatus(JobLogService.JobStatus jobStatus, Timestamp jobEndTime,
      JobDetail jobDetail) {

    BasicDynaBean bean = cronJobRepository.getBean();
    bean.set(CronJobRepository.JOB_NAME, jobDetail.getKey().getName());
    bean.set(CronJobRepository.JOB_LAST_RUNTIME, jobEndTime);
    bean.set(CronJobRepository.JOB_LAST_STATUS, jobStatus.toString());
    Map<String, Object> keys = new HashMap<>();
    keys.put(CronJobRepository.JOB_NAME, jobDetail.getKey().getName());
    cronJobRepository.update(bean, keys);
  }

  /**
   * Reschedule.
   */
  public void reschedule() {
    List<BasicDynaBean> scheduledJobs = getAllScheduledJob();

    for (BasicDynaBean scheduledJobBean : scheduledJobs) {
      String jobName = (String) scheduledJobBean.get("job_name");
      String jobGroup = RequestContext.getSchema() + "_"
          + (String) scheduledJobBean.get("job_group");
      String jobTime = (String) scheduledJobBean.get("job_time");

      Map<String, Object> jobData = new HashMap<>();
      jobData.put("params", (String) scheduledJobBean.get("job_params"));
      jobData.put("schema", RequestContext.getSchema());

      if (("I").equals(scheduledJobBean.get("job_status"))) {
        logger.debug("InActivating Job : " + jobName + " : " + jobTime);
        jobService.deleteJob(buildJob(jobName, jobGroup, JobMap.JOB.get(jobName), jobData));
      } else {
        logger.debug("Scheduling Job : " + jobName + " : " + jobTime);
        jobService.scheduleAtOnlyIfNotScheduled(
            buildJob(jobName, jobGroup, JobMap.JOB.get(jobName), jobData),
            buildTrigger(jobName, jobGroup, jobTime));
      }
    }
  }

  /**
   * reScheduling master charges.
   *
   * @param entity entity
   * @param entityId   entityId
   * @param userName   userName
   */
  public void retryMasterChargeScheduleJob(String entity, String entityId, String userName) {
    LinkedHashMap<String, Object> queryParams = new LinkedHashMap<String, Object>();
    Map<String, Object> searchMap = new HashMap<String, Object>();
    searchMap.put("entity", entity);
    searchMap.put("entity_id", entityId);
    String tableName = "";
    BasicDynaBean masterJobScheduler =
        masterChargesCronSchedulerDetailsRepository.findByKey(searchMap);
    if ("F".equals(masterJobScheduler.get("status"))) {
      Map<String, Object> jobData = new HashMap<>();
      if ("PACKAGE".equals(entity)) {
        queryParams.put("package_id", Integer.valueOf(entityId));
        queryParams.put("org_id", "abov.org_id");
        queryParams.put("bed_type", "abov.bed_type");
        tableName = "package_charges";
      } else if ("OPERATION".equals(entity)) {
        queryParams.put("org_id", "abov.org_id");
        queryParams.put("bed_type", "abov.bed_type");
        queryParams.put("op_id", entityId);
        queryParams.put("surg_asstance_charge", BigDecimal.ZERO);
        queryParams.put("surgeon_charge", BigDecimal.ZERO);
        queryParams.put("anesthetist_charge", BigDecimal.ZERO);
        queryParams.put("username", userName);
        tableName = "operation_charges";
      } else if ("DIAGNOSTIC".equals(entity)) {
        queryParams.put("org_name", "abov.org_id");
        queryParams.put("bed_type", "abov.bed_type");
        queryParams.put("test_id", entityId);
        queryParams.put("priority", "R");
        queryParams.put("username", userName);
        tableName = "diagnostic_charges";
      } else if ("SERVICE".equals(entity)) {
        queryParams.put("org_id", "abov.org_id");
        queryParams.put("bed_type", "abov.bed_type");
        queryParams.put("service_id", entityId);
        queryParams.put("unit_charge", BigDecimal.ZERO);
        queryParams.put("username", userName);
        tableName = "service_master_charges";
        jobData.put("with_general", true);
      }
      queryParams.put("charge", masterJobScheduler.get("charge"));
      queryParams.put("discount", masterJobScheduler.get("discount"));
      jobData.put("query_params", queryParams);
      jobData.put("table_name", tableName);
      jobData.put("schema", RequestContext.getSchema());
      jobData.put("entity", entity);
      jobData.put("entity_id", entityId);
      String jobName = "MasterChargesJobScheduler" + System.currentTimeMillis();
      jobService.scheduleImmediate(buildJob(jobName, MasterChargesJobScheduler.class, jobData));
      Map<String, Object> updateKey = new HashMap<String, Object>();
      updateKey.put("id", masterJobScheduler.get("id"));
      masterJobScheduler.set("status", "P");
      masterJobScheduler.set("error_message", "");
      masterChargesCronSchedulerDetailsRepository.update(masterJobScheduler, updateKey);
    }
  }

  /**
   * Scheduling master charges.
   *
   * @param queryParams queryParams
   * @param tableName   tableName
   * @param entity   Master name
   */
  public void masterChargeScheduleJob(LinkedHashMap<String, Object> queryParams,
      String tableName, String entity) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("query_params", queryParams);
    jobData.put("table_name", tableName);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("entity", entity);
    String entityId = "";
    if ("PACKAGE".equals(entity)) {
      entityId = String.valueOf((int) queryParams.get("package_id"));
    } else if ("OPERATION".equals(entity)) {
      entityId = (String) queryParams.get("op_id");
    } else if ("DIAGNOSTIC".equals(entity)) {
      entityId = (String) queryParams.get("test_id");
    } else if ("SERVICE".equals(entity)) {
      entityId = (String) queryParams.get("service_id");
      jobData.put("with_general", true);
    }
    jobData.put("entity_id", entityId);
    BasicDynaBean masterJobScheduler = masterChargesCronSchedulerDetailsRepository.getBean();
    masterJobScheduler.set("charge",
        queryParams.get("charge") != null ? queryParams.get("charge") : BigDecimal.ZERO);
    masterJobScheduler.set("discount",
        queryParams.get("discount") != null ? queryParams.get("discount") : BigDecimal.ZERO);
    masterJobScheduler.set("status", "P");
    masterJobScheduler.set("entity", entity);
    masterJobScheduler.set("entity_id", entityId);
    masterChargesCronSchedulerDetailsRepository.insert(masterJobScheduler);
    String jobName = "MasterChargesJobScheduler" + System.currentTimeMillis();
    jobService.scheduleImmediate(buildJob(jobName, MasterChargesJobScheduler.class, jobData));
  }
}
