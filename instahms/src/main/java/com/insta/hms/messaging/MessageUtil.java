package com.insta.hms.messaging;

import com.bob.hms.common.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class MessageUtil.
 */
public class MessageUtil {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageUtil.class);

  /**
   * Allow message notification.
   *
   * @param req the req
   * @param messagetype the messagetype
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean allowMessageNotification(HttpServletRequest req, String messagetype)
      throws SQLException {
    return allowMessageNotification(req);
  }
    
   
  /**
   * Allow message notification.
   *
   * @param req the req
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean allowMessageNotification(HttpServletRequest req)
        throws SQLException {

    boolean sendMessage = false;
    HttpSession session = req.getSession(false);
    if (session != null) {
      Preferences prefs = (Preferences) session.getAttribute("preferences");
      Map modules = prefs.getModulesActivatedMap();

      if (modules.containsKey("mod_messaging") && "Y".equals(modules.get("mod_messaging"))) {
        sendMessage = true;
      }
    }

    if (!sendMessage) {
      logger.info("Messaging module is not enabled, message is skipped");
    }
    return sendMessage;
  }
}