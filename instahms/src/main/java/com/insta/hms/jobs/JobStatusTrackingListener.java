package com.insta.hms.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The listener interface for receiving jobStatusTracking events. The class that is interested in
 * processing a jobStatusTracking event implements this interface, and the object created with that
 * class is registered with a component using the component's <code>addJobStatusTrackingListener
 * </code> method. When the jobStatusTracking event occurs, that object's appropriate method is
 * invoked.
 * 
 * @author - tanmay.k
 */
public class JobStatusTrackingListener extends JobListenerSupport {

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private JobLogService jobLogService;

  /**
   * @see org.quartz.JobListener#getName()
   */
  @Override
  public String getName() {
    return "jobStatusTracker";
  }

  /**
   *  
   * @see org.quartz.listeners.JobListenerSupport#jobWasExecuted(org.quartz.JobExecutionContext,
   * org.quartz.JobExecutionException)
   */
  @Override
  public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
    try {
      if (jee != null && !("").equals(jee.getMessage())) {
        jobLogService.updateEndTime(jec, JobLogService.JobStatus.FAILURE, jee);
      } else {
        jobLogService.updateEndTime(jec, JobLogService.JobStatus.SUCCESS, null);
      }
    } catch (Exception ex) {
      logger.error("job_log table update failed : " , ex);
    }
  }

  /**
   * It start when job execution starts
   * 
   * @see org.quartz.listeners.JobListenerSupport#jobToBeExecuted(org.quartz.JobExecutionContext)
   */
  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    try {
      jobLogService.insertJobstatus(context);
    } catch (Exception ex) {
      logger.error("job_log table insert failed : " , ex);
    }
  }
}
