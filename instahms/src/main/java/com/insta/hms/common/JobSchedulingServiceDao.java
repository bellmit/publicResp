package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDetail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * The Class JobSchedulingServiceDao.
 */
public class JobSchedulingServiceDao extends GenericDAO {

  /**
   * Instantiates a new job scheduling service dao.
   */
  public JobSchedulingServiceDao() {
    super("job_log");
  }

  /**
   * Sets the key value pair. return Map for field to be updated
   *
   * @param jobCron        the job cron
   * @param timeStamp      the time stamp
   * @param completedOrNot the completed or not
   * @return the basic dyna bean
   */
  private BasicDynaBean setKeyValuePair(BasicDynaBean jobCron, Timestamp timeStamp,
      String completedOrNot) {
    jobCron.set("job_end_time", timeStamp);
    if (completedOrNot.equals("Success")) {
      jobCron.set("job_status", "Job Completed");
    } else {
      jobCron.set("job_status", "failure");
    }
    return jobCron;
  }

  /**
   * Sets the where clause. return where condition for field to be updated
   *
   * @param jobCron   the job cron
   * @param jobDetail the job detail
   */
  private void setWhereClause(BasicDynaBean jobCron, JobDetail jobDetail) {
    Integer instanceId = (Integer) jobDetail.getJobDataMap().getInt("instance_id");
    if (instanceId != null) {
      jobCron.set("instance_id", instanceId);
    }
  }

  /**
   * Gets the update start time bean. return BasicDynamic Bean of upateStart time of job scheduling
   * Time.
   *
   * @param jobId        the job id
   * @param jobStartTime the job start time
   * @return the update start time bean
   * @throws SQLException the SQL exception
   */
  private BasicDynaBean getUpdateStartTimeBean(String jobId, Timestamp jobStartTime)
      throws SQLException {
    Integer instanceId = getNextSequence();
    BasicDynaBean bean = getBean();
    bean.set("job_id", jobId);
    bean.set("job_start_time", jobStartTime);
    bean.set("instance_id", instanceId);
    bean.set("job_status", "Job Started");
    return bean;
  }

  /**
   * Update start time. To update the start time of an instance of Cron job
   *
   * @param jobId        the job id
   * @param jobStartTime the job start time
   * @return the integer
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public Integer updateStartTime(String jobId, Timestamp jobStartTime)
      throws SQLException, IOException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      BasicDynaBean getStartTimeBean = getUpdateStartTimeBean(jobId, jobStartTime);
      insert(con, getStartTimeBean);
      return (Integer) getStartTimeBean.get("instance_id");
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Update end time. To update the end time of an instance of Cron job
   *
   * @param timeStamp      the time stamp
   * @param jobDetail      the job detail
   * @param completedOrNot the completed or not
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public void updateEndTime(Timestamp timeStamp, JobDetail jobDetail, String completedOrNot)
      throws SQLException, IOException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      BasicDynaBean jobCronJob = getBean();
      setKeyValuePair(jobCronJob, timeStamp, completedOrNot);
      setWhereClause(jobCronJob, jobDetail);
      Integer instanceId = (Integer) jobDetail.getJobDataMap().get("instance_id");
      if (instanceId != null) {
        updateWithName(con, jobCronJob.getMap(), "instance_id");
      }
    } finally {
      if (con != null) {
        con.close();
      }
    }

  }
}
