package com.insta.hms.messaging;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.action.MessageActionLogDao;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The Class MessageActionService.
 */
public class MessageActionService { // extends GenericDAO {
  /** The redis template. */
  /*
   * public MessageActionLogDao() { super("message_action_log"); }
   */
  static RedisTemplate<String, Object> redisTemplate = (RedisTemplate) ApplicationContextProvider
      .getApplicationContext().getBean("redisTemplate");

  /**
   * Gets the action status map.
   *
   * @param messageId
   *          the message id
   * @param userId
   *          the user id
   * @return the action status map
   * @throws SQLException
   *           the SQL exception
   */
  public Map getActionStatusMap(Integer messageId, String userId) throws SQLException {

    Map map = new HashMap<String, String>();
    // get the action list for this message type
    BasicDynaBean messageLog = getMessageLog(messageId);
    String messageType = (String) messageLog.get("message_type_id");
    String entityId = (String) messageLog.get("entity_id");
    Timestamp ts = (Timestamp) messageLog.get("last_sent_date");
    int batchId = (Integer) messageLog.get("batch_id");
    List<BasicDynaBean> actions = getMessageActions(messageType);

    for (BasicDynaBean action : actions) {
      String allowedActors = (String) action.get("allowed_actors"); // (N)one, (A)ll, (S)ome, (O)ne
      Integer allowedUsages = (null != action.get("allowed_usage"))
          ? (Integer) action.get("allowed_usage") : -1; // -1 means unlimited
      Integer actionId = (Integer) action.get("message_action_id");
      // we dont handle the case of None or Some. will do when the need arises
      List<BasicDynaBean> actionLogs = new ArrayList<BasicDynaBean>();
      // takes care of cases where nothing about usage is specified

      if (null != allowedActors) {
        if (allowedActors.equalsIgnoreCase("O")) {
          actionLogs = getActionLogs(actionId, entityId, null, batchId); // no user filter
        }

        if (allowedActors.equalsIgnoreCase("A")) {
          // for specific user
          actionLogs = getActionLogs(actionId, entityId, ts, messageId, userId, batchId);
        }
      }

      if (actionLogs != null && actionLogs.size() >= allowedUsages && -1 != allowedUsages) {
        map.put(actionId, "D");
      } else {
        map.put(actionId, "A");
      }
    }

    return map;
  }

  /**
   * Gets the message actions.
   *
   * @param messageType
   *          the message type
   * @return the message actions
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getMessageActions(String messageType) throws SQLException {
    GenericDAO tadao = new GenericDAO("message_type_actions");
    BasicDynaBean messageAction = tadao.findByKey("message_type_id", messageType);
    List<BasicDynaBean> actionList = new ArrayList<BasicDynaBean>();
    GenericDAO adao = new GenericDAO("message_actions");
    List<BasicDynaBean> actions = adao.listAll();

    for (BasicDynaBean action : actions) {
      if (((int) action.get("message_action_id"))
          == ((int) messageAction.get("message_action_mask"))) {
        actionList.add(action);
      } 
    }
    return actionList;
  }

  /**
   * Gets the action logs.
   *
   * @param actionId
   *          the action id
   * @param entityId
   *          the entity id
   * @param messageTime
   *          the message time
   * @param batchId
   *          the batch id
   * @return the action logs
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getActionLogs(Integer actionId, String entityId,
      Timestamp messageTime, Integer batchId) throws SQLException {
    return getActionLogs(actionId, entityId, messageTime, null, batchId);
  }

  /**
   * Gets the action logs.
   *
   * @param actionId
   *          the action id
   * @param entityId
   *          the entity id
   * @param messageTime
   *          the message time
   * @param messageId
   *          the message id
   * @param batchId
   *          the batch id
   * @return the action logs
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getActionLogs(Integer actionId, String entityId,
      Timestamp messageTime, Integer messageId, Integer batchId) throws SQLException {
    return getActionLogs(actionId, entityId, messageTime, messageId, null, batchId);
  }

  /**
   * Gets the action logs.
   *
   * @param actionId
   *          the action id
   * @param entityId
   *          the entity id
   * @param messageTime
   *          the message time
   * @param messageId
   *          the message id
   * @param userId
   *          the user id
   * @param batchId
   *          the batch id
   * @return the action logs
   * @throws SQLException
   *           the SQL exception
   */
  private List<BasicDynaBean> getActionLogs(Integer actionId, String entityId,
      Timestamp messageTime, Integer messageId, String userId, Integer batchId)
      throws SQLException {

    MessageActionLogDao alDao = new MessageActionLogDao();
    List<BasicDynaBean> actions = alDao.getActionLogs(actionId, entityId, messageTime, messageId,
        userId, batchId);
    return actions;
  }

  /**
   * Gets the message log.
   *
   * @param messageId
   *          the message id
   * @return the message log
   * @throws SQLException
   *           the SQL exception
   */
  private BasicDynaBean getMessageLog(Integer messageId) throws SQLException {
    GenericDAO dao = new GenericDAO("message_log");
    return dao.findByKey("message_log_id", messageId);
  }

  /** The Constant Notificationcount. */
  private static final String Notificationcount = " SELECT COUNT(*) FROM message_log ml "
      + " LEFT JOIN message_recipient r  on  r.message_log_id = ml.message_log_id "
      + " where ml.last_status <> 'D' and ml.message_mode = 'NOTIFICATION'  "
      + "  and  r.message_status <> 'R'   and coalesce(ml.notification_status, '')"
      + " not in ('D', 'A') and r.message_recipient_id = ? ";

  /**
   * Notification count.
   *
   * @param userId
   *          the user id
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public static String notificationCount(String userId) throws SQLException {
    String redisKey = String.format("schema:%s;user:%s;notificationCount",
        RequestContext.getSchema(), RequestContext.getUserName());
    String count = (String) redisTemplate.opsForValue().get(redisKey);
    if (count != null && !count.isEmpty()) {
      return count;
    } else {
      PreparedStatement ps = null;
      Connection con = DataBaseUtil.getConnection();
      try {
        ps = con.prepareStatement(Notificationcount);
        ps.setString(1, userId);
        count = String.format("%02d", DataBaseUtil.getIntValueFromDb(ps));
        redisTemplate.opsForValue().set(redisKey, count);
        // setting expiry time to 1 day; can be set to infinity to optimise further
        redisTemplate.expire(redisKey, 24, TimeUnit.HOURS);
        return count;
      } finally {
        DataBaseUtil.closeConnections(con, ps);
      }
    }
  }

}
