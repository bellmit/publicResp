package com.insta.hms.messaging.action;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Map;

public class ArchiveMessageActionHandler extends StatusChangeActionHandler {

  private static String THIS_ACTION_TYPE = "std_archive";

  @Override
  public boolean changeMessageStatus(BasicDynaBean message) {
    if (!"A".equalsIgnoreCase((String) message.get("notification_status"))) {
      message.set("notification_status", "A");
      return true;
    }
    return false;
  }

  @Override
  public String getActionType() {
    return THIS_ACTION_TYPE;
  }

}
