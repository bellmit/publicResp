package com.insta.hms.redis;

import com.bob.hms.common.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.UUID;

/**
 * The Class RedisMessagePublisher which is reponsible for publishing messages to redis channel.
 * 
 * @author tanmay.k
 */
public class RedisMessagePublisher implements MessagePublisher {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The redis template. */
  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  /** The Constant REDIS_CACHE_INVALIDATE_CHANNEL. */
  public static final String REDIS_CACHE_INVALIDATE_CHANNEL = "ehCache:invalid";
  
  public static final String REDIS_APPOINTMENT_PUSH_CHANNEL = "appointmentPushRedis";

  public static final String REDIS_IPEMR_SECTION_LOCK_CHANNEL = "ipemrSectionLockPushRedis";
  
  public static final String REDIS_ERX_RESPONSE_PUSH_CHANNEL = "eRxResponsePushChannel";

  /** The Constant cacheInvalidationMessageFormat. */
  public static final String cacheInvalidationMessageFormat = "id:%s;cacheRegion:%s";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.redis.MessagePublisher#publish(java.lang.String)
   */
  /**
   * publish.
   *
   * @param message the message
   */
  public void publish(String message) {
    logger.debug("Publishing message to:" + REDIS_CACHE_INVALIDATE_CHANNEL + " with content: "
        + message);
    redisTemplate.convertAndSend(REDIS_CACHE_INVALIDATE_CHANNEL, message);
  }

  /**
   * Notify cache invalidation.
   *
   * @param cacheRegion
   *          the cache region
   */
  public void notifyCacheInvalidation(String cacheRegion) {
    this.publish(String.format(cacheInvalidationMessageFormat, UUID.randomUUID(), cacheRegion));
  }

  /**
   * publish Msg For Schema.
   *
   * @param topic the channel name
   * @param message the message
   */
  public void publishMsgForSchema(String topic, String message) {
    redisTemplate.convertAndSend(topic, RequestContext.getSchema() + ";" + message);
  }
  
  public void publishERxResponse(String topic, Object response) {
    redisTemplate.convertAndSend(topic, response);
  }

}
