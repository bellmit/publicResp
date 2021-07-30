package com.insta.hms.batchjob.pushevent;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.JsonStringToMap;
import com.insta.hms.common.annotations.EventSub;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.common.http.HttpResponseHandler;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.core.scheduler.SchedulerBulkAppointmentsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@EventSub
public class AppointmentUpdateSubscriberJob extends EventSubscriber {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @LazyAutowired
  SchedulerBulkAppointmentsService schedulerBulkAppointmentsService;

  @Autowired
  AppointmentService appointmentService;

  @LazyAutowired
  EventListenerJob eventListnerJob;
  
  @LazyAutowired
  JsonStringToMap jsonStringMap;


  public ArrayList<String> subscribedEvents = new ArrayList<>(Arrays.asList(
      Events.APPOINTMENT_BOOKED, Events.APPOINTMENT_CONFIRMED, Events.APPOINTMENT_NOSHOW,
      Events.APPOINTMENT_CANCEL, Events.APPOINTMENT_ARRIVED, Events.APPOINTMENT_COMPLETED,
      Events.APPOINTMENT_RESCHEDULED, Events.APPOINTMENT_UPDATED));

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getMergedJobDataMap();
    String schemaName = (String) dataMap.get("schema");
    Map<String, String> customHeader = (Map) jsonStringMap
        .convert(String.valueOf(dataMap.get("custom_header")));
    RequestContext.setConnectionDetails(new String[] { null, null, schemaName, "InstaAdmin", "0" });
    String[] apptIds = new String[1];
    if (dataMap.containsKey("appointment_ids") && dataMap.get("appointment_ids") != null) {
      apptIds = (String[]) dataMap.get("appointment_ids");

    } else if (dataMap.containsKey("appointment_id") && dataMap.get("appointment_id") != null) {
      apptIds[0] = (String) dataMap.get("appointment_id");
    }
    for (String apptId : apptIds) {
      String postDataStr;
      try {
        HashMap<String, Object> result = getAppointmentDetails(dataMap, apptId);
        postDataStr = new ObjectMapper().writeValueAsString(result);
        String salt = "";
        if (!(eventFilter(result, (int) dataMap.get("client_id"),
            String.valueOf(dataMap.get("eventId"))) && !(dataMap.get("hash") == null)
            && !dataMap.get("hash").equals("") && !(dataMap.get("callback_url") == null)
            && !dataMap.get("callback_url").equals(""))) {
          continue;
        }
        salt = (String) dataMap.get("hash");
        String url = "";
        url = (String) dataMap.get("callback_url");
        String signature = generateSignature(postDataStr, salt);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("x-insta-schema", schemaName);
        headerMap.put("x-insta-signature", signature);
        if (customHeader != null && !customHeader.equals("")) {
          headerMap.putAll(customHeader);
        }
        logger.debug(
            "url: " + url + " postDataStr " + postDataStr + " headerMap " + headerMap.toString());
        HttpResponse httpResponse = new HttpClient(new HttpResponseHandler(), 10000, 10000)
            .post(url, postDataStr, headerMap);
        logger.debug("Response code: " + httpResponse.getCode() + "Response Message: "
            + httpResponse.getMessage());
        if (httpResponse.getCode() != 200) {
          HashMap newDataMap = new HashMap<>(dataMap);
          newDataMap.put("appointment_id", apptId);
          eventListnerJob.reTrigger(newDataMap, this.getClass());
        }
      } catch (IOException exp) {
        logger.error(exp.getMessage() + "" + exp.getStackTrace());
      }
    }
  }

  private String generateSignature(String jsonBody, String salt) {
    Mac sha256HMac;
    try {
      sha256HMac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(salt.getBytes(), "HmacSHA256");
      sha256HMac.init(secretKey);
      byte[] hash = sha256HMac.doFinal(jsonBody.getBytes());
      return Base64.encodeBase64String(hash);
    } catch (NoSuchAlgorithmException ex) {
      logger.error("Unsupported Algorithm: HmacSHA256");
    } catch (InvalidKeyException ex) {
      logger.error("Invalid salt supplied");
    }
    return "";
  }

  private HashMap<String, Object> getAppointmentDetails(JobDataMap dataMap, String apptId) {
    Map<String, String[]> params = new HashMap<>();
    params.put("appointment_id", new String[] { apptId });
    logger.info("getting data for appointment id" + apptId);
    HashMap data = (HashMap) schedulerBulkAppointmentsService.getAppointmentsForPatient(params,
        null);
    BasicDynaBean bean = appointmentService
        .getPatientDetailsForAppointment(Integer.parseInt(apptId));
    Map patientDetails = bean.getMap();
    HashMap appointment = ((ArrayList<HashMap>) data.get("appointments")).get(0);
    appointment.put("patient", patientDetails);

    setUtcTime(appointment);
    setUtcTimeForOriginalTime(appointment);
    setUtcTimeForModTime(appointment);
    HashMap<String, Object> result = new HashMap<>();
    result.put("event_type", (String) dataMap.get("eventId"));
    result.put("appointment", appointment);
    return result;
  }

  private void setUtcTime(HashMap appointment) {
    String tempDate = appointment.get("appointment_date") + " "
        + appointment.get("appointment_time");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Timestamp ts = null;
    try {
      ts = new java.sql.Timestamp(sdf.parse(tempDate).getTime());
    } catch (ParseException exp) {
      logger.error(exp.getMessage() + "" + exp.getStackTrace());
    }
    appointment.remove("appointment_time");
    appointment.put("appointment_date", DateUtil.formatIso8601Timestamp(ts));

  }
  
  private void setUtcTimeForOriginalTime(HashMap appointment) {
    if (appointment.get("original_date") == null || appointment.get("original_time") == null) {
      appointment.remove("original_time");
      appointment.put("original_date", null);
    } else {
      String tempDate = appointment.get("original_date") + " " + appointment.get("original_time");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      Timestamp ts = null;
      try {
        ts = new java.sql.Timestamp(sdf.parse(tempDate).getTime());
      } catch (ParseException exp) {
        logger.error(exp.getMessage() + "" + exp.getStackTrace());
      }
      appointment.remove("original_time");
      appointment.put("original_date", DateUtil.formatIso8601Timestamp(ts));
    }

  }

  private void setUtcTimeForModTime(HashMap appointment) {
    if (appointment.get("last_updated_date_time") == null
        || appointment.get("last_updpated_time") == null) {
      appointment.remove("last_updated_time");
      appointment.put("last_updated_date_time", null);
    } else {
      String tempDate = appointment.get("last_updated_date_time") + " "
          + appointment.get("last_updated_time");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      Timestamp ts = null;
      try {
        ts = new java.sql.Timestamp(sdf.parse(tempDate).getTime());
      } catch (ParseException exp) {
        logger.error(exp.getMessage() + "" + exp.getStackTrace());
      }
      appointment.remove("last_updated_time");
      appointment.put("last_updated_date_time", DateUtil.formatIso8601Timestamp(ts));
    }
  }
  
  private boolean eventFilter(Map<String, Object> appDetails, int clientId, String eventId) {
    Boolean includeFilter = true;
    Boolean excludeFilter = true;
    Map<String, Object> subscriberDetails = appointmentService.getSubscriberDetailsForEvent(eventId,
        clientId);
    if (!subscriberDetails.isEmpty()) {
      for (Map.Entry<String, Object> filter : subscriberDetails.entrySet()) {
        if (!(filter.getValue() != null && !filter.getValue().equals(""))) {
          continue;
        }
        Map<String, Object> filterMap = jsonStringMap.convert(String.valueOf(filter.getValue()));
        for (Map.Entry<String, Object> mapEntry : filterMap.entrySet()) {
          if (mapEntry.getKey() != null && !mapEntry.getKey().equals("")
              && filter.getKey().equals("include_filter")
              && !String.valueOf(mapEntry.getValue()).contains(String.valueOf(
                  CommonUtils.getValue(mapEntry.getKey(), (Map) appDetails.get("appointment"))))) {
            includeFilter = false;
            break;
          } else if (mapEntry.getKey() != null && !mapEntry.getKey().equals("")
              && filter.getKey().equals("exclude_filter")
              && String.valueOf(mapEntry.getValue()).contains(String.valueOf(
                  CommonUtils.getValue(mapEntry.getKey(), (Map) appDetails.get("appointment"))))) {
            excludeFilter = false;
            break;
          }
        }
      }
      return includeFilter && excludeFilter;
    }
    return false;
  }

  @Override
  public List clientDetails(String eventId) {
    return appointmentService.getClientDetails(eventId);
  }

}
