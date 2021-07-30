package com.insta.hms.jobs;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class JobLogRepository.
 *
 * @author yashwant
 */
@Repository
public class JobLogRepository extends GenericRepository {

  /**
   * Instantiates a new job log repository.
   */
  public JobLogRepository() {
    super("job_log");
  }

  public static final String JOB_START_TIME = "job_start_time";
  public static final String JOB_LOG_ID = "job_log_id";
  public static final String JOB_STATUS = "job_status";
  public static final String STATUS_DESC = "status_desc";
  public static final String JOB_NAME = "job_name";
  public static final String JOB_GROUP = "job_group";
}
