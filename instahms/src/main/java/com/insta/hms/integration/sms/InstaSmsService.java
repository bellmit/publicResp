package com.insta.hms.integration.sms;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.jobs.JobService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InstaSmsService.
 */
@Service
public class InstaSmsService {

  /** The insta SMS repository. */
  @LazyAutowired
  private InstaSmsRepository instaSMSRepository;

  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The redis template. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /** Insta integration service. */
  @LazyAutowired
  private InstaIntegrationService instaIntegrationService;

  /** redisKeyAliveTime. */
  private static final int REDIS_KEY_ALIVE_TIME = 48;

  private static final String SCHEMA_STRING = "sysid";

  /** Redis key template. */
  private String redisKeyTemplate = "schema:%s;phoneNumber:%s;id:%s";

  /** Redis value template. */
  private String redisValueTemplate = "status:%s;startedAt:%s;completedAt:%s";

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(InstaSmsService.class);

  /**
   * Store sms request and schedule job.
   *
   * @requestMap requestParamMap the param map
   */
  public Map<String, String> storeSmsRequestAndScheduleJob(String schema,
      Map<String, String[]> requestMap, HttpServletResponse response) {
    Map<String, Object> requestParamMap = ConversionUtils.flatten(requestMap);
    Map<String, String> returnResponse = new HashMap<>();
    if (!DatabaseHelper.checkSchema(schema)) {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      returnResponse.put("status", "fail");
      returnResponse.put("message", "schema not found!");
      return returnResponse;
    } else {
      requestParamMap.put("schema", schema);
      ServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder
          .getRequestAttributes()).getRequest();
      String ip = servletRequest.getRemoteAddr();
      String xxRealIp = ((HttpServletRequest) servletRequest).getHeader("X-Real-IP");
      if (xxRealIp != null) {
        ip = xxRealIp;
      }
      logger.info("SMS inbound request patient ::: request ip address " + ip);
      logger.info("SMS inbound request patient ::: request get Header X-Real-IP " + xxRealIp);
      RequestContext.setConnectionDetails(new String[] { "", "", schema, "", "0" });
      if (!instaIntegrationService.isIpAllowed(ip, "sms_inbound_request")) {
        response.setStatus(HttpStatus.SC_FORBIDDEN);
        returnResponse.put("status", "fail");
        returnResponse.put("message", "unable to process.");
        return returnResponse;
      }
      if (requestParamMap.get("msg") == null || requestParamMap.get("msg").equals("")) {
        response.setStatus(HttpStatus.SC_FORBIDDEN);
        returnResponse.put("status", "fail");
        returnResponse.put("message", "Message(msg) is empty or null.");
        return returnResponse;
      }
      String message = StringUtils.trimToEmpty(requestParamMap.get("msg").toString());
      // origin is considered as from_phone_no
      if (requestParamMap.get("origin") == null || requestParamMap.get("origin").equals("")) {
        response.setStatus(HttpStatus.SC_FORBIDDEN);
        returnResponse.put("status", "fail");
        returnResponse.put("message", "mobile/phone number(origin) is empty or null.");
        return returnResponse;
      }
      String phoneNumber = StringUtils.trimToEmpty(requestParamMap.get("origin").toString());
      BasicDynaBean receivedSmsBean = instaSMSRepository.getBean();
      int id = instaSMSRepository.getNextSequence();
      receivedSmsBean.set("id", Long.valueOf(id));
      receivedSmsBean.set("message", message);
      receivedSmsBean.set("from_phone_no", phoneNumber);
      receivedSmsBean.set("received_from_ip", ip);
      receivedSmsBean.set("job_status", "Queued");
      if (instaSMSRepository.insert(receivedSmsBean) == 0) {
        throw new HMSException("Failed to store received message");
      }

      BasicDynaBean genPrefBean = genericPreferencesService.getInboundSMSConfValues();
      if (genPrefBean.get("received_sms_appointment_confirm") != null
          && message.equalsIgnoreCase(
              genPrefBean.get("received_sms_appointment_confirm").toString())) {
        requestParamMap.put("status", "Confirmed");
      } else if (genPrefBean.get("received_sms_appointment_cancel") != null
          && message.equalsIgnoreCase(
              genPrefBean.get("received_sms_appointment_cancel").toString())) {
        requestParamMap.put("status", "Cancel");
      } else {
        requestParamMap.put("status", null);
      }
      scheduleJob(requestParamMap, receivedSmsBean);
      response.setStatus(HttpStatus.SC_OK);
      returnResponse.put("status", "success");
      returnResponse.put("message", "successfully processed.");
    }
    return returnResponse;
  }

  /**
   * Schedule job.
   *
   * @param map the map
   */
  private void scheduleJob(Map<String, Object> map, BasicDynaBean bean) {

    String redisKey = null;
    String redisValue = null;
    redisKey = String.format(redisKeyTemplate, map.get("schema").toString(),
        map.get("origin").toString(), bean.get("id").toString());
    map.put("redisKey", redisKey);
    map.put("id", bean.get("id"));

    Map<String, Object> updateDataMap = new HashMap<String, Object>();
    updateDataMap.put("id", bean.get("id"));

    if (map.get("status") != null) {
      redisValue = String.format(redisValueTemplate, "queued", null, null);
      redisTemplate.opsForValue().set(redisKey, redisValue);
      redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);

      jobService.scheduleImmediate(
          buildJob(map.get("schema").toString() + "_ReceivedSms" + System.currentTimeMillis(),
              InstaSmsJob.class, map));
      bean.set("job_id", redisKey);
    } else {
      bean.set("job_status", "Invalid Response");
    }
    instaSMSRepository.update(bean, updateDataMap);
  }
}
