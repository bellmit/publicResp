package com.insta.hms.messaging;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.hl7.dispatcher.Hl7ADTDispatcher;
import com.insta.hms.modules.ModulesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating MessageDispatcher objects.
 */
public class MessageDispatcherFactory {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(MessageDispatcherFactory.class);
  private static ModulesDAO modulesDao = new ModulesDAO();
  private static GenericDAO configDao = new GenericDAO("message_dispatcher_config");
  private static Map<String, MessageDispatcher> DISPATCHERS = createDispatcherMap();

  /**
   * Initialize Static Message Dispatcher to a map for reuse of instances.
   * @return Map of Message dispatchers
   */
  private static Map<String, MessageDispatcher> createDispatcherMap() {
    Map<String, MessageDispatcher> dispatcherMap = new HashMap<>();
    dispatcherMap.put("SMS", new SmsDispatcher());
    dispatcherMap.put("PRACTOSMS", new CommunicatorSmsDispatcher());
    dispatcherMap.put("EMAIL", new EmailDispatcher());
    dispatcherMap.put("HL7_SOCKET_ADT", new Hl7ADTDispatcher());
    dispatcherMap.put("NOTIFICATION", new NotificationDispatcher());
    return Collections.unmodifiableMap(dispatcherMap);
  }

  /**
   * Transform dispatcher type to handle any extended dispatcher selection.
   * @return Sanitized dispatcher type
   */
  private static String sanitizeDispatcherType(String dispatcherType) {
    if (dispatcherType.equalsIgnoreCase("SMS")) {
      try {
        BasicDynaBean practoSmsBean = modulesDao.findByKey("module_id", "mod_practo_sms");
        if (practoSmsBean != null
            && ((String) practoSmsBean.get("activation_status")).equals("Y")) {
          return "PRACTOSMS";
        }
      } catch (SQLException ex) {
        logger.error("", ex);
      }
    }
    return dispatcherType.toUpperCase();
  }
  
  /**
   * Check if the message dispatcher configuration for dispatcher type is active.
   * @return Sanitized dispatcher type
   */
  private static boolean isDispatcherConfigActive(String dispatcherType) {
    if (dispatcherType.equals("PRACTOSMS")) {
      return true;
    }
    try {
      BasicDynaBean configBean = configDao.findByKey("message_mode", dispatcherType);
      return configBean != null && ((String) configBean.get("status")).equals("A");
    } catch (SQLException ex) {
      logger.error("", ex);
    }
    return false;
  }

  /**
   * Gets the dispatcher.
   * @param dispatcherType Message mode of dispatcher.
   * @return dispatcher instance
   */
  public static MessageDispatcher getDispatcher(String dispatcherType) {
    String sanitizedDispatcherType = sanitizeDispatcherType(dispatcherType);
    return isDispatcherConfigActive(sanitizedDispatcherType) 
        ? DISPATCHERS.get(sanitizedDispatcherType) : null;
  }

}