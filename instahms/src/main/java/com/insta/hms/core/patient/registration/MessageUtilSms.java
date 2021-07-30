package com.insta.hms.core.patient.registration;

import com.bob.hms.common.Preferences;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class MessageUtilSms.
 */
@Component
public class MessageUtilSms {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageUtilSms.class);

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Allow message notification.
   *
   * @param messagetype
   *          the messagetype
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public boolean allowMessageNotification(String messagetype) {

    boolean sendMessage = false;
    Map<String, Object> sessionMap = sessionService
        .getSessionAttributes(new String[] { "preferences" });
    if (sessionMap != null && !sessionMap.isEmpty()) {
      Preferences prefs = (Preferences) sessionMap.get("preferences");
      Map modules = prefs.getModulesActivatedMap();

      if (modules.containsKey("mod_messaging") 
          && "Y".equals(modules.get("mod_messaging"))) {
        sendMessage = true;
      }
    }

    if (!sendMessage) {
      logger.info("Messaging module is not enabled, "
          + "message type:  " + messagetype + " is skipped");
    }
    return sendMessage;
  }

}
