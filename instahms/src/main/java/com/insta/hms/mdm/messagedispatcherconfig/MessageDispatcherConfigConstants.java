package com.insta.hms.mdm.messagedispatcherconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageDispatcherConfigConstants {

  public static final Map<String, String> MESSAGE_MODES;
  
  static {
    Map<String, String> tempMap = new HashMap<>();
    tempMap.put("NOTIFICATION", "Notification");
    tempMap.put("EMAIL", "Email");
    tempMap.put("SMS", "Mobile Text Message");
    MESSAGE_MODES = Collections.unmodifiableMap(tempMap);
  }

}
