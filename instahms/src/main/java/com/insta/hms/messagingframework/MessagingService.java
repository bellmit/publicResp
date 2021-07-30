package com.insta.hms.messagingframework;

import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.messaging.InboundMessageBuilder;
import com.insta.hms.messaging.InboundMessageBuilderFactory;
import com.insta.hms.messaging.Message;
import com.insta.hms.messaging.MessageBuilder;
import com.insta.hms.messaging.MessageBuilderFactory;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageParser;
import com.insta.hms.messaging.MessageParserFactory;
import com.insta.hms.messaging.action.MessageActionHandler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class MessagingService.
 *
 * @author pranaysahota
 */
@Service
public class MessagingService {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);

  /** The Constant INSTA_NOTIFICATION_REDIS_KEYs. */
  private static final String INSTA_NOTIFICATION_REDIS_KEYS = "insta_notifications";

  /** The Constant REDIS_KEYS_PLACEHOLDER. */
  private static final String REDIS_KEYS_PLACEHOLDER = "schema:%s;user:%s;%s";

  /** The message log service. */
  @LazyAutowired
  private MessageLogService messageLogService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The redis template. */
  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  /**
   * Gets the notification metadata.
   *
   * @param userId the user id
   * @return the notification metadata
   * @throws SQLException the SQL exception
   */
  public NotificationMetadata getNotificationMetadata(String userId) throws SQLException {
    NotificationMetadata notificationMetadata = getNotificationFromRedisCache(userId);
    if (notificationMetadata == null) {
      notificationMetadata = messageLogService.getNotificationMetadata(userId);
      setNotificationIntoRedisCache(notificationMetadata, userId);
    }
    return notificationMetadata;
  }

  /**
   * Process action.
   *
   * @param requestBody the request body
   * @return the map
   * @throws Exception the exception
   */
  public Map<String, Object> processAction(Map<String, Object> requestBody) throws Exception {
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    requestBody.put("userId", userName);
    MessageActionHandler messageActionHandler =
        MessageActionHandler.get((String) requestBody.get("message_action_type"));
    boolean isSuccess = messageActionHandler.processAction(requestBody);
    if (isSuccess) {
      deleteNotificationFromRedisCache(userName);
    }
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("success", isSuccess);
    return responseMap;
  }

  /**
   * Process event.
   *
   * @param eventId the event id
   */
  public void processEvent(String eventId) {
    MessageManager msgMgr = new MessageManager();
    try {
      msgMgr.processEvent(eventId, null, true);
    } catch (SQLException | ParseException | IOException ex) {
      logger.error(ex.getMessage());
    }
  }

  /**
   * Gets the message details.
   *
   * @param messageType the message type
   * @param userId the user id
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the message details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMessageDetailsList(String messageType, String userId,
      Integer pageNum, Integer pageSize) {
    MessageBuilder msgBuilder = MessageBuilderFactory.getBuilder(messageType);
    List<Map<String, Object>> entities = (List<Map<String, Object>>) msgBuilder
        .getMessageDetailsList(messageType, userId, pageNum, pageSize);
    Map<String, Object> detailsMap = new HashMap<>();
    detailsMap.put("entities", entities);
    return detailsMap;
  }

  /**
   * Sets the notification into redis cash.
   *
   * @param notificationMetadata the new notification into redis cash
   * @param userName the user name
   */
  public void setNotificationIntoRedisCache(NotificationMetadata notificationMetadata,
      String userName) {
    String redisKey =
        String.format(REDIS_KEYS_PLACEHOLDER, RequestContext.getSchema(), userName,
            INSTA_NOTIFICATION_REDIS_KEYS);
    try {

      String stringNotificationContent;
      if (notificationMetadata == null) {
        stringNotificationContent = "{}";
      } else {
        ObjectMapper objectMapper = new ObjectMapper();
        stringNotificationContent = objectMapper.writeValueAsString(notificationMetadata);
      }
      redisTemplate.opsForValue().set(redisKey, stringNotificationContent);
    } catch (Exception ex) {
      logger.error(ex.getMessage());
    }
  }

  /**
   * Gets the notification from redis cash.
   *
   * @param userName the user name
   * @return the notification from redis cash
   */
  public NotificationMetadata getNotificationFromRedisCache(String userName) {
    String redisKey =
        String.format(REDIS_KEYS_PLACEHOLDER, RequestContext.getSchema(), userName,
            INSTA_NOTIFICATION_REDIS_KEYS);
    Object jsonNotificationData = redisTemplate.opsForValue().get(redisKey);
    NotificationMetadata notificationMetadata = null;
    try {
      if (jsonNotificationData != null) {
        notificationMetadata = getNotificationObjectFromJson(jsonNotificationData);
      }
    } catch (Exception exe) {
      logger.error(exe.getMessage());
    }
    return notificationMetadata;
  }

  /**
   * Gets the notification object from json.
   *
   * @param jsonNotificationData the json notification data
   * @return the notification object from json
   * @throws Exception the exception
   */
  private NotificationMetadata getNotificationObjectFromJson(Object jsonNotificationData)
      throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return objectMapper.readValue((String) jsonNotificationData, NotificationMetadata.class);
  }

  /**
   * Delete notification from redis cash.
   *
   * @param userName the user name
   */
  public void deleteNotificationFromRedisCache(String userName) {
    String redisKey =
        String.format(REDIS_KEYS_PLACEHOLDER, RequestContext.getSchema(), userName,
            INSTA_NOTIFICATION_REDIS_KEYS);
    redisTemplate.delete(redisKey);
  }

  /**
   * Process inbound message.
   *
   * @param inboundMessage the inbound message
   * @param parserType the parser type
   * @return the map
   */
  public static Map<String, Object> processInboundMessage(String inboundMessage, 
      String parserType) {
    Map<String, Object> responseMap = new HashMap<>();
    Message msg = new Message();
    msg.setBody(inboundMessage);
    logger.debug("Processing inbound hl7 message");
    logger.debug(inboundMessage);
    if (msg.getErrorMsg() != null && !msg.getErrorMsg().equals("")) {
      responseMap.put("error", msg.getErrorMsg());
      responseMap.put("success", false);
      return responseMap;
    }
    MessageParser parser = MessageParserFactory.getParser(parserType);
    Message instaMessage = parser.getInstaMessage(msg.getBody());
    InboundMessageBuilder messageBuilder =
        InboundMessageBuilderFactory.getBuilder(instaMessage.getMessageType());
    messageBuilder.build(instaMessage);
    boolean isSuccess = messageBuilder.process();

    responseMap.put("success", isSuccess);
    return responseMap;
  }

  /**
   * Send report notification.
   *
   * @param reportId the report id
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void sendReportNotification(String reportId, String isTestDoc)
      throws SQLException, ParseException,
      IOException {
    List<String> activatedModules =
        ApplicationContextProvider.getBean(SecurityService.class).getActivatedModules();

    if (reportId != null && !reportId.equals("") && activatedModules.contains("mod_messaging")) {
      List<BasicDynaBean> beans = null;
      LaboratoryDAO labDao = new LaboratoryDAO();
      beans = labDao.getNotificationUser(Integer.parseInt(reportId), isTestDoc.equals("Y"));
      GenericDAO messageLogBatchIdDAO = new GenericDAO("message_log_batch_id");
      for (BasicDynaBean bean : beans) {
        if (bean != null) {
          Map reportData = new HashMap();
          reportData.put("receipient_id__", bean.get("emp_username"));
          reportData.put("entity_id", reportId);
          int batchId = messageLogBatchIdDAO.getNextSequence();
          reportData.put("batch_id", Integer.toString(batchId));
          MessageManager mgr = new MessageManager();
          mgr.processEvent("diag_report_signedoff_notification", reportData);
        }
      }
    }
  }

}
