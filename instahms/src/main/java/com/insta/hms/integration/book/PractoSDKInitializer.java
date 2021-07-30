package com.insta.hms.integration.book;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.insta.hms.batchjob.PractoSDKInitializationJob;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.JobService;

/**
 * Initializes the connection with Practo SDK on Bean initialization and releases its resources on
 * bean destroy.
 */
public class PractoSDKInitializer {
  
  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /**
   * Inits the.
   */
  public void init() {
    Long time = System.currentTimeMillis();
    jobService.scheduleImmediate(
        buildJob("PractoSDKInitializationJob" + time, PractoSDKInitializationJob.class, null));
  }

  /**
   * Destroy.
   */
  public void destroy() {
    BookSDKUtil.closeAll();
  }
}
