package com.insta.hms.messaging.action;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public abstract class StatusChangeActionHandler extends MessageActionHandler {

  private static GenericDAO notificationDao = new GenericDAO("message_log");

  @Override
  public boolean handleAction(Integer msgId, String option, Map context) throws Exception {
    boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      BasicDynaBean message = getMessageLog(con, msgId);
      success = changeMessageStatus(message);
      if (success) {
        saveMessage(con, msgId, message);
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return success;
  }

  @Override
  public boolean handleAction(Map<String, Object> actionData) throws Exception {
    return false;
  }

  private BasicDynaBean getMessageLog(Connection con, Integer msgId) throws SQLException {
    BasicDynaBean notificationBean = null;
    notificationBean = notificationDao.findByKey(con, "message_log_id", msgId);
    return notificationBean;
  }

  private void saveMessage(Connection con, Integer messageId, BasicDynaBean message)
      throws SQLException, IOException {
    notificationDao.update(con, message.getMap(), "message_log_id", messageId);
  }

  public abstract boolean changeMessageStatus(BasicDynaBean message);
  
}
