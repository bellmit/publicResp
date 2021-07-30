package com.insta.hms.messaging.action;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MessageActionLogDao.
 */
public class MessageActionLogDao extends GenericDAO {

  /**
   * Instantiates a new message action log dao.
   */
  public MessageActionLogDao() {
    super("message_action_log");
  }

  /**
   * Gets the action logs.
   *
   * @param actionId the action id
   * @param entityId the entity id
   * @param messageTime the message time
   * @param messageId the message id
   * @param userId the user id
   * @param batchId the batch id
   * @return the action logs
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getActionLogs(Integer actionId, String entityId, Timestamp messageTime,
      Integer messageId, String userId, Integer batchId) throws SQLException {
    Map filterMap = new HashMap<String, Object>();
    filterMap.put("message_action_id", actionId);

    if (null != entityId) { // when will this be null ? need to check
      filterMap.put("message_action_entity", entityId);
    }

    if (null != messageId) { 
      // this will be null when multiple messages were sent out for the same
      // event and we want to change it as one
      filterMap.put("message_log_id", messageId);
    }

    if (null != userId) { // this will be null when only a single user can act on the message
      filterMap.put("action_done_by", userId);
    }

    if (null != batchId) { // this will be null when only a single user can act on the message
      filterMap.put("batch_id", batchId);
    }
    List<BasicDynaBean> actions = listAll(null, filterMap, null);
    return actions;
  }

}
