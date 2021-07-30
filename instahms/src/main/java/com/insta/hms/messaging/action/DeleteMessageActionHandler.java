package com.insta.hms.messaging.action;

import org.apache.commons.beanutils.BasicDynaBean;

public class DeleteMessageActionHandler extends StatusChangeActionHandler {
  private static String THIS_ACTION_TYPE = "std_delete";

  @Override
  public boolean changeMessageStatus(BasicDynaBean message) {
    if (!"D".equalsIgnoreCase((String) message.get("notification_status"))) {
      message.set("notification_status", "D");
      return true;
    }
    return false;
  }

  @Override
  public String getActionType() {
    return THIS_ACTION_TYPE;
  }

}
