package com.insta.hms.jobs;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class CronJobRepository.
 */
@Repository
public class CronJobRepository extends GenericRepository {

  /**
   * Instantiates a new cron job repository.
   */
  public CronJobRepository() {
    super("cron_job");
  }
  
  public static final String JOB_NAME = "job_name";
  public static final String JOB_GROUP = "job_group";
  public static final String JOB_TIME = "job_time";
  public static final String JOB_PARAMS = "job_params";
  public static final String JOB_STATUS = "job_status";
  public static final String JOB_LAST_RUNTIME = "job_last_runtime";
  public static final String JOB_LAST_STATUS = "job_last_status";
}
