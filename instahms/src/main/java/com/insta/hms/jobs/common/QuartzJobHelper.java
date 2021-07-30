package com.insta.hms.jobs.common;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import com.insta.hms.jobs.ex.ExpressionValidationException;

import org.apache.commons.lang3.RandomUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * The Class QuartzJobHelper.
 *
 * @author yashwant QuartzHelper is used for building a trigger and jobDetail. Best way to use this
 *         class method as an argument of scheduleAt of
 * 
 */
public class QuartzJobHelper {

  /**
   * Builds the job.
   *
   * @param jobName
   *          the job name
   * @param jobClass
   *          the job class
   * @param jobData
   *          the job data
   * @return the job detail
   */
  @SuppressWarnings("rawtypes")
  public static JobDetail buildJob(String jobName, Class jobClass, Map<String, Object> jobData) {
    String jobGroup = jobName;
    if (jobData != null && jobData.get("schema") != null) {
      jobGroup = (String) jobData.get("schema") + "_" + jobClass.getSimpleName();
    }
    return buildJob(jobName, jobGroup, jobClass, jobData);
  }

  /**
   * buildInstaJob is used for building a job of JobDetail type.
   *
   * @param jobName
   *          : can be unique key for identifying job
   * @param jobGroup
   *          : this can be same name as class name
   * @param jobClass
   *          : This is mandatory
   * @param jobData
   *          : this is optional, it can be null
   * @return the job detail
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static JobDetail buildJob(String jobName, String jobGroup, Class jobClass,
      Map<String, Object> jobData) {

    JobDetail jobDetail = null;
    jobDetail = newJob(jobClass).withIdentity(jobKey(jobName, jobGroup)).requestRecovery().build();
    if (jobData != null) {
      jobDetail.getJobDataMap().putAll(jobData);
    }
    return jobDetail;
  }

  /**
   * Used for scheduling simple scheduler type of trigger.
   *
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @param simpleBuilder
   *          the simple builder
   * @return the trigger
   */
  public static Trigger buildTrigger(String triggerName, String triggerGroup,
      SimpleScheduleBuilder simpleBuilder) {
    return newTrigger().withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(simpleBuilder).build();
  }

  /**
   * Used for building cron expression type of job.
   *
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @param expression
   *          the expression
   * @return the trigger
   */
  public static Trigger buildTrigger(String triggerName, String triggerGroup, String expression) {
    validateCronExpression(expression);
    CronScheduleBuilder csb = cronSchedule(expression);
    return buildTrigger(triggerName, triggerGroup, csb);

  }

  /**
   * Used for building CronScheduleBuilder type of trigger.
   *
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @param cronBuilder
   *          the cron builder
   * @return the trigger
   */
  public static Trigger buildTrigger(String triggerName, String triggerGroup,
      CronScheduleBuilder cronBuilder) {
    return newTrigger().withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(cronBuilder).build();
  }

  /**
   * Validate cron expression.
   *
   * @param expression
   *          the expression
   */
  public static void validateCronExpression(String expression) {
    try {
      CronExpression.validateExpression(expression);
    } catch (ParseException exception) {
      throw new ExpressionValidationException("exception.master.cronjobs.incorrect.expression",
          new String[] { expression });
    } catch (Exception ex) {
      throw new ExpressionValidationException("exception.master.cronjobs.incorrect.expression",
          new String[] { expression });
    }
  }

  /**
   * Daily trigger.
   *
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @param hour
   *          the hour
   * @param minute
   *          the minute
   * @return the trigger
   */
  public static Trigger dailyTrigger(String triggerName, String triggerGroup, int hour,
      int minute) {
    return newTrigger().withIdentity(triggerKey(triggerName, triggerGroup))
        .withSchedule(dailyAtHourAndMinute(hour, minute)).build();
  }

  /**
   * Returns trigger scheduled at random seconds from current time.
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @param range
   *          upper bound for range
   * @return the trigger
   */
  public static Trigger randomTrigger(String triggerName, String triggerGroup, int range) {
    if (range < 0 ) {
      range = 0;
    }
    int offset = RandomUtils.nextInt(1, range + 1);
    Date currentDate = new Date(System.currentTimeMillis() + 10000 + (offset * 1000));
    return startAtDateTrigger(triggerName, triggerGroup, currentDate);
  }

  /**
   * Better to use scheduleImmediate method of JobService.
   * <p>
   * return (Trigger) newTrigger().withIdentity(triggerKey(triggerName,
   * triggerGroup)).startNow().build(); // HMS-11547 Getting JobPersistenceException in Immediate
   * job startNow() so making some delay. Below are exception message: Couldn't acquire next trigger
   * :Couldn't retrieve trigger : No records found for selection of trigger with key So making few
   * millis seconds delay in trigger
   * </p>
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @return the trigger
   */
  public static Trigger immediateTrigger(String triggerName, String triggerGroup) {
    Date currentDate = new Date(System.currentTimeMillis() + 20000);
    return startAtDateTrigger(triggerName, triggerGroup, currentDate);
  }

  /**
   * Better to use scheduleImmediate of JobService.
   *
   * @param triggerName
   *          the trigger name
   * @return the trigger
   */
  public static Trigger immediateTrigger(String triggerName) {
    return newTrigger().withIdentity(triggerKey(triggerName)).startNow().build();
  }
  
  /**
   * Better to use scheduleImmediate with priority method of JobService.
   * <p>
   * return (Trigger) newTrigger().withIdentity(triggerKey(triggerName,
   * triggerGroup)).startNow().build(); // HMS-11547 Getting JobPersistenceException in Immediate
   * job startNow() so making some delay. Below are exception message: Couldn't acquire next trigger
   * :Couldn't retrieve trigger : No records found for selection of trigger with key So making few
   * millis seconds delay in trigger
   * </p>
   * 
   * @param triggerName the trigger name
   * @param triggerGroup the trigger group
   * @return the trigger
   */
  public static Trigger immediateTriggerWithPriority(String triggerName, String triggerGroup) {
    Date currentDate = new Date(System.currentTimeMillis() + 5000);
    return startAtDateTriggerWithPriority(triggerName, triggerGroup, currentDate, 10);
  }

  /**
   * Start job at particular time.
   * Added MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
   *
   * @param triggerName
   *          the trigger name
   * @param triggerGroup
   *          the trigger group
   * @param date
   *          the date
   * @return the trigger
   */
  public static Trigger startAtDateTrigger(String triggerName, String triggerGroup, Date date) {
    return newTrigger().withIdentity(triggerName, triggerGroup).startAt(date)
        .withSchedule(
            simpleSchedule().withMisfireHandlingInstructionIgnoreMisfires().withRepeatCount(0))
        .build();
  }

  /**
   * Start job at particular time with priority. Added MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
   *
   * @param triggerName the trigger name
   * @param triggerGroup the trigger group
   * @param date the date
   * @param triggerPriority priority
   * @return the trigger
   */
  public static Trigger startAtDateTriggerWithPriority(String triggerName, String triggerGroup,
      Date date, int triggerPriority) {
    return newTrigger().withIdentity(triggerName, triggerGroup).startAt(date)
        .withSchedule(
            simpleSchedule().withMisfireHandlingInstructionIgnoreMisfires().withRepeatCount(0))
        .withPriority(triggerPriority).build();
  }


}
