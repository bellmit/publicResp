package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.master.scheduledexportprefs.ScheduledExportPrefsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingExporterJobScheduling.
 *
 * @author krishna
 */

public class AccountingExporterJobScheduling {

  /** The Constant log. */
  private static final Logger log = LoggerFactory.getLogger(AccountingExporterJobScheduling.class);

  /** The trigger group. */
  private static String triggerGroup = "TallyExport";

  /**
   * Initialize accounting export job.
   *
   * @param schemas
   *          the schemas
   * @throws SQLException
   *           the SQL exception
   */
  public static void initializeAccountingExportJob(List<BasicDynaBean> schemas) 
      throws SQLException {

    ScheduledExportPrefsDAO dao = new ScheduledExportPrefsDAO();
    String[] dbSchema = new String[] { null, null, null };
    RequestContext.setConnectionDetails(dbSchema);
    Connection con = DataBaseUtil.getConnection();
    try {
      if (schemas == null) {
        schemas = DataBaseUtil.getAllSchemas(con, false);
      }
    } catch (SQLException se) {
      log.error("Unable to get all schemas: ", se);
      throw se;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    for (BasicDynaBean schema : schemas) {
      String schemaStr = (String) schema.get("schema");
      List<BasicDynaBean> prefsList = null;
      try {
        dbSchema = new String[] { null, null, schemaStr };
        RequestContext.setConnectionDetails(dbSchema);
        prefsList = dao.listAll();
      } catch (SQLException se) {
        log.error(
            "Unable to get the scheduled tally preferences for schema : " + schemaStr + " : ", se);
        throw se;
      }

      for (BasicDynaBean prefbean : prefsList) {
        String period = (String) prefbean.get("period");
        Integer scheduleId = (Integer) prefbean.get("schedule_id");
        String periodFullName = null;
        Trigger trigger = null;

        if (period.equals("H")) {
          int minutes = (Integer) prefbean.get("hourly_time");
          Calendar cal = Calendar.getInstance();
          cal.set(Calendar.MINUTE, minutes);
          /*
           * trigger = TriggerUtils.makeHourlyTrigger(1);
           * trigger.setName("hourly:"+schema.get("schema") + ":" + scheduleId);
           * trigger.setGroup("TallyExport"); trigger.setStartTime(cal.getTime()); periodFullName =
           * "hourly";
           */
          /*
           * TODO : Review below code and replace with above one String triggerName =
           * "hourly:"+schema.get("schema") + ":" + scheduleId; trigger = newTrigger()
           * .withIdentity(triggerName, triggerGroup) .withSchedule(simpleSchedule()
           * .repeatHourlyForever()) .startAt(cal.getTime()) .build();
           */
          periodFullName = "hourly";

        } else if (period.equals("D")) {
          Time time = (Time) prefbean.get("daily_or_weekly_time");
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(time.getTime());
          int hour = cal.get(cal.HOUR);
          int ampm = cal.get(cal.AM_PM);
          int minute = cal.get(cal.MINUTE);
          if (ampm == 1) {
            hour += 12;
          }
          /*
           * trigger = TriggerUtils.makeDailyTrigger("daily" + "_" + scheduleId, hour, minute);
           * trigger.setName("daily:"+schema.get("schema") + ":" + scheduleId);
           * trigger.setGroup("TallyExport");
           */
          /*
           * TODO : Review below code and replace above one String triggerName =
           * "daily:"+schema.get("schema") + ":" + scheduleId;
           * 
           * trigger = newTrigger() .withIdentity(triggerName,triggerGroup)
           * .withSchedule(dailyAtHourAndMinute(hour, minute)) .build();
           */
          periodFullName = "daily";

        } else if (period.equals("W")) {
          String day = (String) prefbean.get("weekly_on");
          HashMap<String, Integer> weekDays = new HashMap<String, Integer>();
          weekDays.put("Sun", new Integer(1));
          weekDays.put("Mon", new Integer(2));
          weekDays.put("Tue", new Integer(3));
          weekDays.put("Wed", new Integer(4));
          weekDays.put("Thu", new Integer(5));
          weekDays.put("Fri", new Integer(6));
          weekDays.put("Sat", new Integer(7));

          int dayOfWeek = weekDays.get(day);
          Calendar cal = Calendar.getInstance();
          Time time = (Time) prefbean.get("daily_or_weekly_time");
          cal.setTimeInMillis(time.getTime());
          int hour = cal.get(cal.HOUR);
          int minute = cal.get(cal.MINUTE);
          int ampm = cal.get(cal.AM_PM);
          if (ampm == 1) {
            hour += 12;
          }
          /*
           * trigger = TriggerUtils.makeWeeklyTrigger("weekly" + "_" + scheduleId, dayOfWeek, hour,
           * minute);
           * 
           * trigger.setName("weekly:"+schema.get("schema") + ":" + scheduleId);
           * trigger.setGroup("TallyExport");
           */
          /*
           * TODO : Review below code and replace with above one String triggerName =
           * "weekly:"+schema.get("schema") + ":" + scheduleId; trigger = newTrigger()
           * .withIdentity(triggerName,triggerGroup)
           * .withSchedule(weeklyOnDayAndHourAndMinute(dayOfWeek, hour, minute)) .build();
           */
          periodFullName = "weekly";
        }

        Map<String, Object> jobData = new HashMap<String, Object>();
        jobData.put("period", periodFullName);
        jobData.put("schedule_id", prefbean.get("schedule_id"));
        /*
         * JobDetail jobDetail = new JobDetail("TallyExport:"+schema.get("schema") + "_" +
         * scheduleId, "TallyExport", ScheduledAccountingExporter.class);
         */
        /*
         * TODO : Review below code and replace with above one jobDetail
         * 
         * 
         * JobDetail jobDetail = newJob(ScheduledAccountingExporter.class)
         * .withIdentity("TallyExport:"+schema.get("schema") + "_" + scheduleId, "TallyExport")
         * .build();
         */
        // jobDetail.getJobDataMap().put("schema", schema.get("schema")); TODO :: Why required
        // schema
        // jobDetail.getJobDataMap().put("schema", schema.get("schema"));
        // jobDetail.getJobDataMap().put("period", periodFullName);
        // jobDetail.getJobDataMap().put("schedule_id", prefbean.get("schedule_id"));
        // JobService jobService = JobSchedulingService.getJobService();
        // jobService.scheduleAt(jobDetail, trigger);
      }
    }

  }

  /**
   * Un schedule accounting export job.
   *
   * @param schema
   *          the schema
   * @throws SchedulerException
   *           the scheduler exception
   */
  public static void unScheduleAccountingExportJob(String schema) throws SchedulerException {

    // Set triggers =
    // JobSchedulingService.getScheduler()
    // .getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup));
    // Iterator it = triggers.iterator();
    // while(it.hasNext()){
    // Trigger tr = (Trigger)it.next();
    // JobSchedulingService.getScheduler().unscheduleJob(tr.getKey());
    // }
  }

}
