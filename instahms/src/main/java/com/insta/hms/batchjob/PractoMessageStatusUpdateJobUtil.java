package com.insta.hms.batchjob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.http.HttpClient;
import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.messaging.InstaIntegrationDao;
import com.insta.hms.messaging.SystemDataDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

public class PractoMessageStatusUpdateJobUtil {

  private static Logger logger = LoggerFactory.getLogger(PractoMessageStatusUpdateJobUtil.class);

  /**
   * Update message status.
   *
   * @param schema    the schema
   * @param startDate the start date
   */
  public static void updateMessageStatus(String schema, String startDate) {
    List<String> queryList = new ArrayList<>();
    Connection con = DataBaseUtil.getConnection();
    ObjectMapper mapper = new ObjectMapper();
    String status = "";
    int referenceId = 0;
    char state = 'F';
    String failureReason;
    int rowsStatus = 0;
    Statement stmt = null;

    try {
      // Getting API URL
      String api = (String) new InstaIntegrationDao().findByKey("integration_name", "comm_get")
          .get("url");
      if (api == null) {
        logger.error("Communicator api is not configured");
        throw new MessagingException("Dispatcher not configured");
      }
      // Getting headers
      Map<String, String> headerMap = new HashMap<>();
      headerMap.put("Content-Type", "application/json");
      // Getting argument parameters to be sent to the communicator
      String referenceType = (String) new SystemDataDao().getRecord().get("group_id");

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      String endDate = DateUtil.getCurrentIso8601Timestamp();

      startDate = startDate.replace("T", " ").replace("Z", "");
      endDate = endDate.replace("T", " ").replace("Z", "");
      String[] messageTypesArray = new String[] { "INSTA_SMS_NEXT_DAY_APPOINTMENT_REMINDER",
          "INSTA_SMS_APPOINTMENT_CANCELLATION", "INSTA_SMS_EDIT_PATIENT_ACCESS",
          "INSTA_SMS_DOCTOR_APPOINTMENTS", "INSTA_SMS_PATIENT_ADMITTED",
          "INSTA_SMS_APPOINTMENT_REMINDER", "INSTA_SMS_APPOINTMENT_CONFIRMATION",
          "INSTA_SMS_VACCINE_REMINDER", "INSTA_SMS_APPOINTMENT_DETAILS_CHANGE",
          "INSTA_SMS_FOLLOWUP_REMINDER", "INSTA_SMS_REPORT_READY",
          "INSTA_SMS_APPOINTMENT_RESCHEDULE", "INSTA_SMS_PATIENT_ON_IP_ADMISSION",
          "INSTA_SMS_FAMILY_ON_IP_ADMISSION", "INSTA_SMS_PATIENT_ON_OP_ADMISSION",
          "INSTA_SMS_FAMILY_ON_OP_ADMISSION", "INSTA_SMS_PATIENT_ON_IP_REVISIT",
          "INSTA_SMS_FAMILY_ON_IP_REVISIT", "INSTA_SMS_PATIENT_ON_OP_REVISIT",
          "INSTA_SMS_FAMILY_ON_OP_REVISIT", "INSTA_SMS_BILL_PAYMENT_RECEIVED",
          "INSTA_SMS_ADVANCE_PAID", "INSTA_SMS_NEXT_OF_KIN_WARD_BED_SHIFT",
          "INSTA_SMS_DOCTOR_WARD_BED_SHIFT", "INSTA_SMS_PATIENT_WARD_BED_SHIFT",
          "INSTA_SMS_APPOINTMENT_CONFIRMATION_FOR_DOCTOR", "INSTA_SMS_DISCOUNT_GIVEN",
          "INSTA_SMS_DAILY_COLLECTION", "INSTA_SMS_PATIENT_ON_DISCHARGE",
          "INSTA_SMS_NOK_ON_PATIENT_DISCHARGE", "INSTA_SMS_TO_DOCTOR_ON_PAT_DISCHARGE" };
      Map<String, String> data = new HashMap<>();
      data.put("reference_type", referenceType);
      data.put("start_date", startDate);
      data.put("end_date", endDate);
      data.put("sms_types",
          "'" + StringUtils.arrayToDelimitedString(messageTypesArray, "','") + "'");
      HttpResponse httpResponse = new HttpClient(new CommunicatorGetStatusResponseHandler(), 10000,
          10000).get(api, data, headerMap);
      if (httpResponse.getCode() == HttpResponse.SUCCESS_STATUS_CODE) {
        try {
          String jsonData = httpResponse.getMessage();
          logger.info("response from communicator is " + jsonData);
          jsonData = jsonData.replace("\"[{", "[{").replace("}]\"", "}]").replace("\"[", "[")
              .replace("]\"", "]").replace("\\", "");
          MessageResult reso = mapper.readValue(jsonData, MessageResult.class);
          int len = reso.getResult().size();

          for (int i = 0; i < len; i++) {
            status = reso.getResult().get(i).getStatus();
            referenceId = reso.getResult().get(i).getReference_id();
            failureReason = reso.getResult().get(i).getFailure_reason();
            if (status != null) {
              if (status.equals("FAILED")) {
                state = 'F';
              }
              if (status.equals("SENT") || status.equals("WAITING")) {
                state = 'S';
                failureReason = "Message sent successfully";
              }
              if (status.equals("DELIVERED")) {
                state = 'R';
                failureReason = "Message delivered successfully";
              }
              queryList.add("UPDATE message_log set last_status_message= '" + failureReason
                  + "' where message_log_id = '" + referenceId + "'");
              queryList.add("UPDATE message_log set last_status= '" + state
                  + "' where message_log_id = '" + referenceId + "'");
            }
          }
        } catch (JsonMappingException exception) {
          logger.error("", exception);
        }
        Calendar lastRun = Calendar.getInstance();
        String lastRunTime = dateFormat.format(lastRun.getTime());
        queryList.add("UPDATE cron_details set last_exec_time= '" + lastRunTime
            + "' where cron_name = 'PractoMessageStatusUpdateJob'");
        logger.debug("Executing PractoMessageStatusUpdateJob cron job  in schema = " + schema);
      } else if (httpResponse.getCode() == -100) {
        logger.error(httpResponse.getMessage());
        throw new MessagingException(httpResponse.getMessage());

      } else {
        logger.error("Invalid response from communicator: " + httpResponse.getMessage());
        throw new MessagingException("Message configuration invalid");
      }
    } catch (JsonGenerationException exception) {
      logger.error("JsonGenerationException occured");
    } catch (IOException exception) {
      logger.error("IOException occured");
    } catch (MessagingException exception) {
      logger.error("MessagingException occured");
    } catch (SQLException exception) {
      logger.error("SQLException occured");
    }
    try {
      stmt = con.createStatement();
    } catch (SQLException exception) {
      logger.error(exception.getMessage());
    }
    try {
      int index = 1;
      for (String query : queryList) {
        try {
          rowsStatus = stmt.executeUpdate(query);
          logger.debug("sql-" + index + "=" + query);
        } catch (SQLException exception) {
          logger.error("PractoMessageStatusUpdateJobUtil Error :  " + exception + " : "
              + " In schema=" + RequestContext.getSchema() + "\n" + "SQL=" + query);
        }
        index++;
      }
    } finally {
      DataBaseUtil.closeConnections(con, stmt);
    }

  }
}
