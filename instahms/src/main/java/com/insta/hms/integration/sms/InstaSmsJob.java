package com.insta.hms.integration.sms;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.scheduler.AppointmentRepository;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.jobs.GenericJob;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The Class InstaSmsJob.
 */
public class InstaSmsJob extends GenericJob {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(InstaSmsJob.class);

  /** Redis value template. */
  private String redisValueTemplate = "status:%s;startedAt:%s;completedAt:%s";

  /** RedisTemplate. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The appointment repo. */
  @LazyAutowired
  private AppointmentRepository appointmentRepository;
  
  /** The insta SMS repository. */
  @LazyAutowired
  private InstaSmsRepository instaSMSRepository;
  


  private static final int REDIS_KEY_ALIVE_TIME = 72;

  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
    RequestContext.setConnectionDetails(
        new String[] { null, null, jobDataMap.get("schema").toString(), null, null });
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String startedAt = sdf.format(new Date());
    String redisValue = String.format(redisValueTemplate, "In-Process", startedAt, null);
    String redisKey = jobDataMap.getString("redisKey");
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);

    BasicDynaBean smsBean = instaSMSRepository.findByKey("id", jobDataMap.get("id"));
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("message_id", jobDataMap.get("id"));
    String appStatus = jobDataMap.get("status").toString();
    paramMap.put("status", appStatus);
    paramMap.put("mnumber", smsBean.get("from_phone_no"));
    Calendar calFrom = Calendar.getInstance();
    SimpleDateFormat sdfTo = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calTo = Calendar.getInstance();
    try {
      calFrom.setTime(sdfTo.parse(smsBean.get("received_date_time").toString()));
      calTo.setTime(sdfTo.parse(smsBean.get("received_date_time").toString()));
      calFrom.add(Calendar.DAY_OF_MONTH, 1);
      calFrom.add(Calendar.SECOND, -1);
      calTo.add(Calendar.DATE, 2);
      calTo.add(Calendar.SECOND, -1);
    } catch (ParseException exception) {
      logger.error("Exception in PractoMessageStatusUpdateJob:  ", exception.getMessage());
    }
    paramMap.put("fromDate", calFrom.getTime());
    paramMap.put("toDate", calTo.getTime());
    String completedAt = sdf.format(new Date());
    String jobStatus = null;
    List<BasicDynaBean> appointments = appointmentRepository.getAppointmentBeans(paramMap);
    if (appointments.isEmpty()) {
      jobStatus = "No-Action Performed.";
    } else if (appStatus.equals("Cancel") && appointments.size() > 1) {
      jobStatus = "Found multiple appointments, No-Action Performed.";
    } else {
      appointmentService.updateAppointmentStatus(paramMap,appointments);
      jobStatus = "Updated";
    } 
    smsBean.set("job_status", jobStatus);
    redisValue = String.format(redisValueTemplate, jobStatus, startedAt, completedAt);
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);
    Map<String, Object> updateDataMap = new HashMap<>();
    updateDataMap.put("id", jobDataMap.get("id"));
    instaSMSRepository.update(smsBean, updateDataMap);
  }
}
