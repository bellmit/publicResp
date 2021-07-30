package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EventLogPlugin implements ActionPlugin {

  static Logger log = LoggerFactory.getLogger(RequestLog.class);

  /**
   * Gets a map of the params in the request and logs into event_log table.
   * 
   * @param params Map of request parameters
   * 
   * @return boolean value indicating success of execution
   */
  public boolean execute(Map<String, String[]> params) {
    GenericDAO eventLogDAO = new GenericDAO("event_log");
    Connection con = null;
    boolean success = false;
    con = DataBaseUtil.getConnection(120);
    try {
      con.setAutoCommit(false);
      int eventId = eventLogDAO.getNextSequence();
      BasicDynaBean eventLogBean = eventLogDAO.getBean();
      eventLogBean.set("event_id", eventId);
      eventLogBean.set("created_at", DateUtil.getCurrentTimestamp());
      // primary key of the main table accessed wrt any action
      String primaryKey = params.get("primary_key")[0];
      eventLogBean.set("entity_id", params.get(primaryKey)[0]);
      eventLogBean.set("remarks", "Event logged successfully");
      eventLogBean.set("event_type", params.get("action_id")[0]);
      eventLogBean.set("schema_name", params.get("schema_name")[0]);
      eventLogBean.set("status", "unread");
      eventLogBean.set("user_id", params.get("user_id")[0]);
      success = eventLogDAO.insert(con, eventLogBean);
      if (!success) {
        log.debug("Failed to write to event_log table");
        return false;
      }
      return success;
    } catch (Exception ex) {
      log.error("{}", ex);
    } finally {
      try {
        DataBaseUtil.commitClose(con, success);
      } catch (SQLException ex) {
        log.error("{}", ex);
      }
    }
    return false;
  }
}