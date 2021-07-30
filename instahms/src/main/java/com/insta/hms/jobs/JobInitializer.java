package com.insta.hms.jobs;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;
import static com.insta.hms.jobs.common.QuartzJobHelper.buildTrigger;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.SessionCleanupJob;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.common.JobMap;
import com.insta.hms.jobs.ex.ExpressionValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class JobInitializer.
 */
public class JobInitializer {

  /** The Constant logger. */
  static final Logger logger = LoggerFactory.getLogger(JobInitializer.class);

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The scheduled job service. */
  @LazyAutowired
  private CronJobService scheduledJobService;

  /** init method when this class loads. */
  public void init() {
    registerCronJob();
  }

  private static final String SCHEMA = "schema";

  /**
   * This method is for scheduling a job while starting an application.
   */
  public void registerCronJob() {

    List<BasicDynaBean> schemas = DatabaseHelper.getAllSchemas();
    for (BasicDynaBean schema : schemas) {
      scheduleJobForSchema(schema);
    }
  }

  private void scheduleJobForSchema(BasicDynaBean schema) {

    String sch = (String) schema.get(SCHEMA);
    String[] dbSchema = new String[] { null, "", sch, "_system", "", "" };
    RequestContext.setConnectionDetails(dbSchema);
    List<BasicDynaBean> scheduledJobs = scheduledJobService.getAllScheduledJob();

    for (BasicDynaBean scheduledJobBean : scheduledJobs) {
      try {
        String jobName = (String) scheduledJobBean.get("job_name");
        if (JobMap.JOB.get(jobName) == null) {
          logger.error("JOB classes are missing will not schedule job. JOB_NAME :" + jobName);
          continue;
        }
        String jobGroup = sch + "_" + (String) scheduledJobBean.get("job_group");
        String jobTime = (String) scheduledJobBean.get("job_time");
        logger.info("JOBNAME :" + jobName + " JOBGROUP :" + jobGroup + " JOBTIME : " + jobTime);
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("params", (String) scheduledJobBean.get("job_params"));
        jobData.put(SCHEMA, sch);

        if (("I").equals(scheduledJobBean.get("job_status"))) {
          logger.debug("InActivating Job : " + jobName + " : " + jobTime);
          jobService.deleteJob(buildJob(jobName, jobGroup, JobMap.JOB.get(jobName), jobData));
        } else {
          jobService.scheduleAtOnlyIfNotScheduled(
              buildJob(jobName, jobGroup, JobMap.JOB.get(jobName), jobData),
              buildTrigger(jobName, jobGroup, jobTime));
        }

      } catch (ExpressionValidationException expressionValidationException) {
        logger.error(expressionValidationException.getMessage());
      } catch (Exception ex) {
        logger.error(ex.getMessage());
      }
    }
    // License check job do not modify unless you know what you are doing.
    Map<String, Object> sessionCleanupJobData = new HashMap<>();
    sessionCleanupJobData.put(SCHEMA, sch);
    jobService.scheduleAtOnlyIfNotScheduled(
        buildJob("SessionCleanupJob", sch + "_SessionCleanupJob", SessionCleanupJob.class,
            sessionCleanupJobData),
        buildTrigger("SessionCleanupJob", sch + "_SessionCleanupJob", "0 0/1 * * * ?"));
  }
}
