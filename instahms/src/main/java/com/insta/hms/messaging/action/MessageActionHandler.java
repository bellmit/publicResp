package com.insta.hms.messaging.action;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class MessageActionHandler.
 */
public abstract class MessageActionHandler {

  /** The Constant notificationMap. */
  private static final Map<String, String> notificationMap = new HashMap<String, String>();

  static {
    notificationMap.put("std_delete", "com.insta.hms.messaging.action.DeleteMessageActionHandler");
    notificationMap.put("std_archive",
        "com.insta.hms.messaging.action.ArchiveMessageActionHandler");
    notificationMap.put("custom_bill_cancellation",
        "com.insta.hms.messaging.action.BillCancelMessageActionHandler");
    notificationMap.put("custom_diag_notification",
        "com.insta.hms.messaging.action.DiagReportMessageActionHandler");
  }

  /**
   * Creates the action log.
   *
   * @param con
   *          the con
   * @param messageLog
   *          the message log
   * @param actionId
   *          the action id
   * @param actionDoneBy
   *          the action done by
   * @param batchId
   *          the batch id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean createActionLog(Connection con, BasicDynaBean messageLog, Integer actionId,
      String actionDoneBy, Integer batchId) throws SQLException, IOException {
    GenericDAO dao = new GenericDAO("message_action_log");
    BasicDynaBean bean = dao.getBean();
    bean.set("message_log_id", (Integer) messageLog.get("message_log_id"));
    bean.set("message_action_id", actionId);
    bean.set("message_action_time", DateUtil.getCurrentTimestamp());
    bean.set("action_done_by", actionDoneBy);
    bean.set("message_action_entity", (String) messageLog.get("entity_id"));
    bean.set("batch_id", batchId);
    return dao.insert(con, bean);
  }

  /**
   * Gets the.
   *
   * @param handlerType
   *          the handler type
   * @return the message action handler
   */
  public static MessageActionHandler get(String handlerType) {
    String builderClass = notificationMap.get(handlerType);
    MessageActionHandler msgActionHandler = null;

    try {
      Class cls = Class.forName(builderClass);
      msgActionHandler = (MessageActionHandler) cls.newInstance();
    } catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    } catch (InstantiationException ie) {
      ie.printStackTrace();
    } catch (IllegalAccessException iae) {
      iae.printStackTrace();
    }
    return msgActionHandler;
  }

  /**
   * Do handle.
   *
   * @param msgId
   *          the msg id
   * @param option
   *          the option
   * @param actionContext
   *          the action context
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean doHandle(Integer msgId, String option, Map actionContext) throws Exception {
    boolean success = handleAction(msgId, option, actionContext);
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String actionType = getActionType();
      String actionDoneBy = (String) actionContext.get("username");
      BasicDynaBean messageAction = getMessageAction(con, actionType);
      Integer actionId = (Integer) messageAction.get("message_action_id");
      BasicDynaBean messageLog = getMessageLog(con, msgId);
      int batchId = (Integer) messageLog.get("batch_id");
      success = success && createActionLog(con, messageLog, actionId, actionDoneBy, batchId);
      // success = createActionLog(con, messageLog, actionId,actionDoneBy);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Process handle.
   *
   * @param requestBody
   *          the request body
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public boolean processAction(Map<String, Object> requestBody) throws Exception {
    boolean success = handleAction(requestBody);
    success = success && prepareMessageActionLog(requestBody);
    success = success && makredMessageAsRead((Integer) requestBody.get("message_log_id"));
    return success;
  }

  /**
   * Makred message as read.
   *
   * @param messageLogId
   *          the message log id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean makredMessageAsRead(Integer messageLogId) throws SQLException, IOException {
    boolean status = false;
    GenericDAO msgRecipientdao = new GenericDAO("message_recipient");
    BasicDynaBean msgRecipientbean = msgRecipientdao.findByKey("message_log_id", messageLogId);
    String messageStatus = (String) msgRecipientbean.get("message_status");
    if (messageStatus != "R") {
      Connection con = null;
      try {
        con = DataBaseUtil.getConnection();
        msgRecipientbean.set("message_status", "R");
        int count = msgRecipientdao.update(con, msgRecipientbean.getMap(), "message_log_id",
            messageLogId);
        status = count > 0;
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    return status;
  }

  /**
   * Prepare message action log.
   *
   * @param requestBody
   *          the request body
   * @return the boolean
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private Boolean prepareMessageActionLog(Map<String, Object> requestBody)
      throws SQLException, IOException {
    boolean success = false;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      String actionType = getActionType();
      String actionDoneBy = (String) requestBody.get("userId");
      BasicDynaBean messageAction = getMessageAction(con, actionType);
      Integer actionId = (Integer) messageAction.get("message_action_id");
      BasicDynaBean messageLog = getMessageLog(con, (Integer) requestBody.get("message_log_id"));
      int batchId = (Integer) messageLog.get("batch_id");
      success = createActionLog(con, messageLog, actionId, actionDoneBy, batchId);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Gets the message action.
   *
   * @param con
   *          the con
   * @param actionType
   *          the action type
   * @return the message action
   * @throws SQLException
   *           the SQL exception
   */
  private BasicDynaBean getMessageAction(Connection con, String actionType) throws SQLException {
    GenericDAO msgActionsDao = null;
    BasicDynaBean msgActionsBean = null;
    msgActionsDao = new GenericDAO("message_actions");
    msgActionsBean = msgActionsDao.findByKey(con, "message_action_type", actionType);
    return msgActionsBean;
  }

  /**
   * Gets the action type.
   *
   * @return the action type
   */
  public abstract String getActionType();

  /**
   * Gets the message log.
   *
   * @param con
   *          the con
   * @param msgId
   *          the msg id
   * @return the message log
   * @throws SQLException
   *           the SQL exception
   */
  /*
   * public String getActionType() { return null; }
   */
  private BasicDynaBean getMessageLog(Connection con, Integer msgId) throws SQLException {
    GenericDAO notificationDao = null;
    BasicDynaBean notificationBean = null;
    notificationDao = new GenericDAO("message_log");
    notificationBean = notificationDao.findByKey(con, "message_log_id", msgId);
    return notificationBean;
  }

  /**
   * Handle action.
   *
   * @param msgId
   *          the msg id
   * @param option
   *          the option
   * @param actionContext
   *          the action context
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public abstract boolean handleAction(Integer msgId, String option, Map actionContext)
      throws Exception;

  /**
   * Handle action.
   *
   * @param actionData
   *          the action data
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public abstract boolean handleAction(Map<String, Object> actionData) throws Exception;
}
