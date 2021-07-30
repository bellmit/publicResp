package com.insta.hms.redis;

import com.amazonaws.util.StringUtils;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BasicCachingDAO;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.integration.insurance.erxprescription.ERxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.HashMap;

/**
 * The Class RedisMessageSubscriber to listen to messages from redis channel. Incoming message
 * formats: 1) Cache Invalidation message - "id:%s;cacheRegion:%s"
 * 
 * @author tanmay.k
 */
public class RedisMessageSubscriber implements MessageListener {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private static final String REDIS_APPOINTMENT_PUSH_CHANNEL = "appointmentPushRedis";
  private static final String REDIS_CACHE_INVALIDATE_CHANNEL = "ehCache:invalid";
  private static final String REDIS_ERX_RESPONSE_PUSH_CHANNEL = "eRxResponsePushChannel";

  @Autowired
  GenericPreferencesService prefservice;

  @Autowired
  AppointmentService appointmentService;
  
  @Autowired
  ERxService erxService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.data.redis.connection.MessageListener#onMessage(org.springframework.data.
   * redis.connection.Message, byte[])
   */
  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String channelName = new String(pattern);
      String messageBody = new String(message.getBody());
      logger.debug("Message received: " + messageBody);
      if (channelName.equals(REDIS_APPOINTMENT_PUSH_CHANNEL)) {
        handleAppointmentPush(messageBody);
      } else if (channelName.equals(REDIS_CACHE_INVALIDATE_CHANNEL)) {
        invalidateCacheRegion(getCacheRegion(message));
      } else if (channelName.equals(REDIS_ERX_RESPONSE_PUSH_CHANNEL)) {
        handleERxResponse(messageBody);
      }
    } catch (Exception exception) {
      logger.error("Exception on Message Subscription" + exception);
    }
  }

  /**
   * handle appointment push.
   *
   * @param messageBody
   *          the messageBody
   */
  private void handleAppointmentPush(String messageBody) {
    try {
      String[] messageArray = messageBody.split(";");
      String schemaName = messageArray[0];
      String appointmentIds = messageArray[1];
      String[] appointmentIdArray = appointmentIds.split(",");
      String appointmentCategories = messageArray[2];
      String[] appointmentCategoriesArray = appointmentCategories.split(",");
      RequestContext.setConnectionDetails(new String[] {null, "", schemaName, "", "", ""});
      appointmentService.pushAppointmentsToWebSocket(appointmentIdArray,
          appointmentCategoriesArray);
    } catch (Exception exception) {
      logger.error("Exception in handleAppointmentPush " + exception);
    }
  }

  /**
   * Gets the cache region.
   *
   * @param message
   *          the message
   * @return the cache region
   */
  private String getCacheRegion(Message message) {
    return message.toString().split(";")[1].split(":")[1];
  }

  /**
   * Invalidate cache region.
   *
   * @param cacheRegion
   *          the cache region
   */
  private void invalidateCacheRegion(String cacheRegion) {
    try {
      if ("generic_preferences".equals(getTableName(cacheRegion))) {
        prefservice.clearCache(getSchema(cacheRegion));
      } else {
        new BasicCachingDAO(getTableName(cacheRegion)).clearCache(cacheRegion);
      }
    } catch (Exception exception) {
      logger.error("Exception in invalidateCacheRegion" + exception);
    }
  }

  /**
   * Gets the table name from the cacheRegion.
   *
   * @param cacheRegion
   *          the cache region
   * @return the table name
   */
  private String getTableName(String cacheRegion) {
    return getCacheDefinitions(cacheRegion)[1];
  }

  /**
   * Gets the schema from the cacheRegion.
   *
   * @param cacheRegion
   *          the cache region
   * @return the schema
   */
  private String getSchema(String cacheRegion) {
    return getCacheDefinitions(cacheRegion)[0];
  }

  /**
   * Gets the cache definations.
   *
   * @param cacheRegion
   *          the cache region
   * @return the cache definations
   */
  private String[] getCacheDefinitions(String cacheRegion) {
    return cacheRegion.split("@");
  }
  
  private void handleERxResponse(String messageBody) {
    try {
      String[] messageArray = messageBody.split(";");
      String message = messageArray[2];
      Boolean success = Boolean.valueOf(messageArray[3]);
      String requestType = messageArray[4];
      HashMap<String, Object> erxResponse = new HashMap<>();
      erxResponse.put("message", message);
      erxResponse.put("success", success);
      erxResponse.put("requestType", requestType);
      String details = messageArray.length == 7 ? messageArray[6] : null;
      if (!StringUtils.isNullOrEmpty(details)) {
        String[] keyValuePairs = details.split(",");
        HashMap<String, Object> erxDetails = new HashMap<>();
        for (String keyValuePair : keyValuePairs) {
          String[] entry = keyValuePair.split("=");
          String key = entry[0].trim();
          String value = null;
          if (entry.length == 2) {
            value = entry[1].trim();
          }
          erxDetails.put(key, value);
        }
        erxResponse.put("details", erxDetails);
      }
      String schemaName = messageArray[0];
      String channelName = messageArray[1];
      String userId = messageArray[5];
      RequestContext.setConnectionDetails(new String[] {null, "", schemaName, "", "", ""});
      erxService.pushERxResponseToWebSocket(userId, erxResponse, channelName);
    } catch (Exception exception) {
      logger.error("Exception in handleERxResponse " + exception);
    }
  }

}
