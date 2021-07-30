package com.insta.hms.jobs;

import com.bob.hms.common.RequestContext;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

/**
 * The listener interface for receiving jobCleanup events. The class that is interested in
 * processing a jobCleanup event implements this interface, and the object created with that class
 * is registered with a component using the component's <code>addJobCleanupListener</code> method.
 * When the jobCleanup event occurs, that object's appropriate method is invoked.
 *
 * @author - tanmay.k
 */
public class JobCleanupListener extends JobListenerSupport {

  /**
   * 
   * @see org.quartz.JobListener#getName()
   */
  @Override
  public String getName() {
    return "dbConnectionCleaner";
  }

  /* 
   * @see org.quartz.listeners.JobListenerSupport#jobWasExecuted(org.quartz.JobExecutionContext, 
   * org.quartz.JobExecutionException)
   * 
   * @see org.quartz.listeners.JobListenerSupport#jobWasExecuted(org.quartz.JobExecutionContext,
   * org.quartz.JobExecutionException)
   */
  @Override
  public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
    RequestContext.cleanupConnections();
    super.jobWasExecuted(jec, jee);
  }
}
