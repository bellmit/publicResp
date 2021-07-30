package com.insta.hms.common.multiuser;

import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.multiuser.MultiUserRedisRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Multi User Service. Current Implementation: Get screenLock for oldui i.e. JSP screens
 * 
 * @author pranaysahota
 *
 */
@Service
public class MultiUserService {

  /** The push service. */
  @LazyAutowired
  private PushService pushService;

  /** The redis repository. */
  @LazyAutowired
  private MultiUserRedisRepository redisRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The Constant WEB_SOCKET_SCREEN_LOCK_CHANNEL. */
  private static final String WEB_SOCKET_SCREEN_LOCK_CHANNEL = "/topic/actionscreen/lock/";

  /** The Constant REDIS_KEY_SCREEN_LOCK. */
  private static final String REDIS_KEY_SCREEN_LOCK = "screen_id:%s;screenlock";
  

  /**
   * Gets the screen lock.
   *
   * @param screenId the screen id
   * @param patientId the patient id
   * @return the screen lock
   */
  public Map<String, Object> getScreenLock(String screenId, String patientId) {
    String loggedInUser = (String) sessionService.getSessionAttributes().get("userId");
    String redisKey = String.format(REDIS_KEY_SCREEN_LOCK, screenId);
    Map<String, Object> existingRedisData = redisRepository.getHashKeyData(redisKey, patientId);
    if (existingRedisData != null) {
      if (existingRedisData.get("user_id").equals(loggedInUser)) {
        return existingRedisData;
      }
    }
    Map<String, Object> redisData = new HashMap<>();
    redisData.put("user_id", loggedInUser);
    redisData.put("lock_obtained_at", (new Date()).getTime());
    redisRepository.addData(redisKey, patientId, redisData);
    String flashMessage = "The screen is currently in use by another user, " + loggedInUser
        + ", and so blocked from being modified.";
    Map<String, Object> payload = new HashMap<>();
    payload.put("message", flashMessage);
    payload.put("user_id", loggedInUser);
    pushService.push(getScreenLockWsChannelName(screenId, patientId), payload);
    return redisData;
  }

  /**
   * Delete screen lock.
   *
   * @param screenId the screen id
   * @param patientId the patient id
   * @return the map
   */
  public Map<String, Object> removeScreenLock(String screenId, String patientId) {
    String redisKey = String.format(REDIS_KEY_SCREEN_LOCK, screenId);
    Map<String, Object> deleteResponse = new HashMap<>();
    deleteResponse.put("is_screen_locked", true);
    Map<String, Object> redisData = redisRepository.getHashKeyData(redisKey, patientId);
    if ( redisData != null) {
      deleteResponse.put("is_screen_locked", false);
      String deleteResponseMessage =
          "The content on this page can now be modified. Please refresh the screen.";
      String userId = (String) redisData.get("user_id");
      Map<String, Object> payload = new HashMap<>();
      payload.put("message", deleteResponseMessage);
      payload.put("user_id", userId);
      redisRepository.deleteData(redisKey, patientId);
      pushService.push(getScreenLockWsChannelName(screenId, patientId), payload);
      return deleteResponse;
    }
    return deleteResponse;
  }
  
  /**
   * Gets the screen lock ws channel name.
   *
   * @param screenId the screen id
   * @param patientId the patient id
   * @return the screen lock ws channel name
   */
  private String getScreenLockWsChannelName(String screenId, String patientId) {
    StringBuilder sb = new StringBuilder(WEB_SOCKET_SCREEN_LOCK_CHANNEL);
    sb.append(screenId).append("/").append(patientId);
    return sb.toString();
  }
}
