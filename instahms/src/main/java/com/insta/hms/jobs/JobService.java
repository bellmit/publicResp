package com.insta.hms.jobs;

import static com.insta.hms.jobs.common.QuartzJobHelper.immediateTrigger;
import static com.insta.hms.jobs.common.QuartzJobHelper.immediateTriggerWithPriority;
import static com.insta.hms.jobs.common.QuartzJobHelper.randomTrigger;
import static com.insta.hms.jobs.common.QuartzJobHelper.startAtDateTrigger;

import com.insta.hms.common.annotations.LazyAutowired;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Provide the basic service related to quartz service. Scheduler object should not use outside this
 * class.
 *
 * @author yashwant
 */
@SuppressWarnings("unused")
@Service
public class JobService {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(JobService.class);

  /**
   * The scheduler Scheduler object for scheduling, deleting and adding / deleting listener.
   */
  @LazyAutowired
  private Scheduler scheduler;

  /**
   * Schedule a job using this method if jobKey and triggerKey is same.
   *
   * @param jobDetail
   *          the job detail
   * @param jobDateTime
   *          the job date time
   */
  public void scheduleAt(JobDetail jobDetail, Date jobDateTime) {
    logger.debug("Scheduling Job :" + jobDetail.getKey());
    if (isJobExists(jobDetail)) {
      deleteJob(jobDetail, jobDateTime);
      doSchedule(jobDetail, startAtDateTrigger(jobDetail.getKey().getName(),
          jobDetail.getKey().getGroup(), jobDateTime));
    } else {
      doSchedule(jobDetail, startAtDateTrigger(jobDetail.getKey().getName(),
          jobDetail.getKey().getGroup(), jobDateTime));
    }
  }

  /**
   * This is used for scheduling job, Trigger object is mandatory for scheduling a job. JobDetail
   * object can be null.
   * <p>
   * It will re-scheduled same trigger which is available with same trigger key.
   * </p>
   * @param jobDetail
   *          the job detail
   * @param trigger
   *          the trigger
   */
  public void scheduleAt(JobDetail jobDetail, Trigger trigger) {
    logger.debug("Scheduling Job :" + jobDetail.getKey());
    if (isJobExists(jobDetail)) {
      deleteJob(jobDetail, trigger); 
      doSchedule(jobDetail, trigger);
    } else {
      doSchedule(jobDetail, trigger);
    }
  }

  /**
   * This is used for scheduling job, Trigger object is mandatory for scheduling a job. JobDetail
   * object can be null.
   * 
   *
   * @param jobDetail
   *          the job detail
   * @param trigger
   *          the trigger
   */
  public void scheduleAtOnlyIfNotScheduled(JobDetail jobDetail, Trigger trigger) {
    logger.debug("Scheduling Job : " + jobDetail.getKey());
    if (isJobExists(jobDetail)) {
      return;
    } else {
      logger.debug("Scheduling Job : " + jobDetail.getKey());
      doSchedule(jobDetail, trigger);
    }
  }

  /**
   * Use this method if jobKey and triggerKey is same.
   *
   * @param job
   *          the job
   */
  public void scheduleRandom(JobDetail job, Integer rangeUpperBound) {
    scheduleAt(job, randomTrigger(job.getKey().getName(),
        job.getKey().getGroup(), rangeUpperBound));
  }

  /**
   * Use this method if jobKey and triggerKey is same.
   *
   * @param job
   *          the job
   */
  public void scheduleImmediate(JobDetail job) {
    scheduleAt(job, immediateTrigger(job.getKey().getName(), job.getKey().getGroup()));
  }
  
  /**
   * For scheduling immediate trigger.
   *
   * @param jobDetail
   *          the job detail
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   */
  public void scheduleImmediate(JobDetail jobDetail, String triggerName, String triggerGroup) {
    scheduleAt(jobDetail, immediateTrigger(triggerName, triggerGroup));
  }
  
  /**
   * Use this method if jobKey and triggerKey is same.
   * 
   * @param job the job
   */
  public void scheduleImmediateOnlyIfNotScheduled(JobDetail job) {
    if (!isJobExists(job)) {
      scheduleImmediate(job);
    }
  }
  
  /**
   * Re-Schedule a job using this method if jobKey and triggerKey is same.
   *
   * @param jobDetail
   *          the job detail
   * @param jobDateTime
   *          the job date time
   */
  public void rescheduleJobAt(JobDetail jobDetail, Date jobDateTime) {
    deleteJob(jobDetail);
    if (!isJobExists(jobDetail)) {
      scheduleAt(jobDetail, jobDateTime);
    }
  }
  
  /**
   * Use this method if jobKey and triggerKey is same with priority.
   *
   * @param job
   *          the job
   */
  public void scheduleHighPriorityImmediate(JobDetail job) {
    scheduleAt(job,
        immediateTriggerWithPriority(job.getKey().getName(), job.getKey().getGroup()));
  }
  


  /**
   * Re-schedule the job if new and old trigger key is different.
   *
   * @param oldTrigger
   *          the old trigger
   * @param newTrigger
   *          the new trigger
   */
  private void rescheduleJob(Trigger oldTrigger, Trigger newTrigger) {
    try {
      this.scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Failed to re-schedule a job " + ex);
    }
  }

  /**
   * Do schedule.
   *
   * @param job
   *          the job
   * @param trigger
   *          the trigger
   */
  private void doSchedule(JobDetail job, Trigger trigger) {
    try {
      if (job == null) {
        this.scheduler.scheduleJob(trigger);
      } else {
        this.scheduler.scheduleJob(job, trigger);
      }

    } catch (SchedulerException ex) {
      throw new IllegalStateException("Failed to schedule a job " + ex);
    }
  }

  /**
   * Use this method for deleting a job . If scheduling a job by using JobDetail and Date object
   *
   * @param jobDetail
   *          the job detail
   * @param dateTime
   *          the date time
   */
  public void deleteJob(JobDetail jobDetail, Date dateTime) {
    Trigger trigger = startAtDateTrigger(jobDetail.getKey().getName(),
        jobDetail.getKey().getGroup(), dateTime);
    logger.debug("Deleting trigger, keys =" + trigger.getKey());
    try {
      this.scheduler.unscheduleJob(trigger.getKey());
      this.scheduler.deleteJob(jobDetail.getKey());
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Faild to delete job :" + ex);
    }
  }

  /**
   * Delete the job using jobDetail and trigger object.
   *
   * @param jobDetail
   *          the job detail
   * @param trigger
   *          the trigger
   */
  public void deleteJob(JobDetail jobDetail, Trigger trigger) {
    logger.debug("Deleting trigger, keys = " + trigger.getKey());
    try {
      this.scheduler.unscheduleJob(trigger.getKey());
      this.scheduler.deleteJob(jobDetail.getKey());
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Faild to delete a job :" + ex);
    }
  }

  /**
   * Delete the job only using trigger object.
   *
   * @param trigger
   *          the trigger
   */
  public void deleteJob(Trigger trigger) {
    logger.debug("Deleting trigger, keys = " + trigger.getKey());
    try {
      this.scheduler.unscheduleJob(trigger.getKey());
      this.scheduler.deleteJob(trigger.getJobKey());
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Faild to delete a job :" + ex);
    }
  }

  /**
   * Delete the job explicitly only with the JobDetails if trigger key are same as job key.
   *
   * @param jobDetail
   *          the job detail
   */
  public void deleteJob(JobDetail jobDetail) {
    try {
      TriggerKey key = TriggerKey.triggerKey(jobDetail.getKey().getName(),
          jobDetail.getKey().getGroup());
      this.scheduler.unscheduleJob(key);
      this.scheduler.deleteJob(jobDetail.getKey());
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Faild to delete a job. " + ex);
    }
  }

  /**
   * Delete the job explicitly only with the JobDetails object and TriggerKey.
   *
   * @param jobDetail
   *          the job detail
   * @param triggerKey
   *          the trigger key
   */
  public void deleteJob(JobDetail jobDetail, String triggerKey) {
    try {
      TriggerKey key = TriggerKey.triggerKey(triggerKey);

      this.scheduler.unscheduleJob(key);
      this.scheduler.deleteJob(jobDetail.getKey());
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Faild to delete a job. " + ex);
    }
  }

  /**
   * Checks if is job exists.
   *
   * @param job
   *          the job
   * @return true, if is job exists
   */
  private boolean isJobExists(JobDetail job) {
    try {
      return this.scheduler.getJobDetail(job.getKey()) != null;
    } catch (SchedulerException ex) {
      throw new IllegalStateException("Faild to find job. " + ex);
    }
  }


  /**
   * Adds the job listeneradd.
   *
   * @param jobListener the job listener
   * @param jobKey the job key
   * @throws SchedulerException the scheduler exception
   */
  public void addJobListeneradd(JobListener jobListener, JobKey jobKey) throws SchedulerException {
    scheduler.getListenerManager().addJobListener(jobListener, KeyMatcher.keyEquals(jobKey));
  }

 
  /**
   * Adds the trigger listener.
   *
   * @param triggerListener the trigger listener
   * @param triggerKey the trigger key
   * @throws SchedulerException the scheduler exception
   */
  public void addTriggerListener(TriggerListener triggerListener, TriggerKey triggerKey)
      throws SchedulerException {

    scheduler.getListenerManager().addTriggerListener(triggerListener,
        KeyMatcher.keyEquals(triggerKey));
  }
}
