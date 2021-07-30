package com.insta.hms.common;

import com.insta.hms.jobs.JobService;

/**
 * The Class JobSchedulingService.
 */
public class JobSchedulingService {

  /**
   * Do not use this method from spring, do an autowire of JobService.
   *
   * @return the job service
   */
  public static JobService getJobService() {
    return (JobService) ApplicationContextProvider.getApplicationContext().getBean("jobService");
  }

}
