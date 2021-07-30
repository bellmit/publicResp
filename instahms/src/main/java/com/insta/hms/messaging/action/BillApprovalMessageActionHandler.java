package com.insta.hms.messaging.action;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class BillApprovalMessageActionHandler extends MessageActionHandler {

  static Logger log = LoggerFactory
      .getLogger(BillApprovalMessageActionHandler.class);
  private static String THIS_ACTION_TYPE = "std_approve_bill";
  
  private static final GenericDAO billDAO = new GenericDAO("bill");

  @Override
  public boolean handleAction(Integer msgId, String option, Map actionContext) throws Exception {
    Connection con = null;
    boolean success = false;
    String username = (String) actionContext.get("username");
    try {
      con = DataBaseUtil.getConnection();
      BasicDynaBean message = getMessageLog(con, msgId);
      String entityNo = (String) message.get("entity_id");
      if (entityNo != null) {
        BasicDynaBean billBean = billDAO.findByKey(con, "bill_no", entityNo);
        if (!"A".equalsIgnoreCase((String) billBean.get("cancellation_approval_status"))
            && !"R".equalsIgnoreCase((String) billBean.get("cancellation_approval_status"))) {
          billBean.set("cancellation_approval_status", "A");
          billBean.set("cancellation_approved_by", username);
          billBean.set("cancellation_approved_date", DateUtil.getCurrentTimestamp());
          billBean.set("cancel_approve_amount", billBean.get("total_amount"));
          billDAO.update(con, billBean.getMap(), "bill_no", entityNo);
          success = true;
        } else {
          log.info("Notification Approvel Already done for Bill no " + entityNo);
        }
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
    GenericDAO notificationDao = null;
    BasicDynaBean notificationBean = null;
    notificationDao = new GenericDAO("message_log");
    notificationBean = notificationDao.findByKey(con, "message_log_id", msgId);
    return notificationBean;
  }

  @Override
  public String getActionType() {
    return THIS_ACTION_TYPE;
  }

}
