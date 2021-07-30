package com.insta.hms.messagingframework;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * The Class MessageLogRepository.
 */
@Repository
public class MessageLogRepository extends GenericRepository {

  /**
   * Instantiates a new message_log repository.
   */
  public MessageLogRepository() {
    super("message_log");
  }

  /** The Constant GET_NOTIFICATION_COUNT_FOR_TYPE. */
  private static final String GET_NOTIFICATION_COUNT_FOR_TYPE = "SELECT ml.message_type_id, "
      + " count(ml.message_type_id) AS count, "
      + " mc.message_category_name, mt.message_type_name, mt.message_type_description "
      + " FROM message_log ml "
      + " JOIN message_recipient r ON r.message_log_id = ml.message_log_id "
      + " JOIN message_types mt ON mt.message_type_id=ml.message_type_id "
      + " JOIN message_category mc on mc.message_category_id=mt.category_id"
      + " WHERE ml.last_status <> 'D' " + "  AND ml.message_mode = 'NOTIFICATION' "
      + " AND  r.message_status <> 'R' "
      + " AND coalesce(ml.notification_status, '') "
      + "  NOT in ('D', 'A') AND r.message_recipient_id = ? "
      + "GROUP BY ml.message_type_id, mc.message_category_name, "
      + " mt.message_type_name, mt.message_type_description";

  /**
   * Gets the notification count.
   *
   * @param userId
   *          the user id
   * @return the notification count
   */
  public List<BasicDynaBean> getNotificationCount(String userId) {
    List<BasicDynaBean> result = DatabaseHelper.queryToDynaList(GET_NOTIFICATION_COUNT_FOR_TYPE,
        new Object[] { userId });
    if (result == null) {
      return Collections.emptyList();
    }
    return result;
  }

  /** The Constant GET_MESSAGE_ACTION_OPTIONS. */
  private static final String GET_MESSAGE_ACTION_OPTIONS = "TO-DO";

  /**
   * Gets the notification actions.
   *
   * @param userId
   *          the user id
   * @return the notification actions
   */
  public List<BasicDynaBean> getNotificationActions(String userId) {
    List<BasicDynaBean> result = DatabaseHelper.queryToDynaList(GET_MESSAGE_ACTION_OPTIONS,
        new Object[] { userId });
    if (result == null) {
      return Collections.emptyList();
    }
    return result;
  }

  /** The query. */
  String query = "UPDATE message_recipient SET message_status = 'X' where message_log_id in ("
      + " SELECT message_log_id from message_log WHERE entity_id in (:entity_id)"
      + " ) and message_status = 'S'";

  /**
   * Marked as canceled.
   *
   * @param entityIds
   *          the entity ids
   * @return the integer
   */
  public Integer markedAsDelete(String[] entityIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("entity_id", entityIds);
    return DatabaseHelper.update(query, parameters);
  }
}
