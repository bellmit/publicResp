package com.insta.hms.messaging.providers;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagNotificationSignedOffDataProvider.
 */
public class DiagNotificationSignedOffDataProvider extends QueryDataProvider {

  /**
   * Instantiates a new diag notification signed off data provider.
   */
  public DiagNotificationSignedOffDataProvider() {
    super(THIS_NAME);
  }

  /** The log. */
  private static Logger log = LoggerFactory.getLogger(DiagNotificationSignedOffDataProvider.class);

  /** The this name. */
  private static String THIS_NAME = "Diagnostics Notifications Signed Off Data ";

  /** The Constant TEST_REPORT_ENTITIES. */
  private static final String TEST_REPORT_ENTITIES = "SELECT * FROM ( "
      + "   (SELECT tp.mr_no, get_patient_name(tp.mr_no) as patient_name, tvr.report_id, "
      + " tvr.report_name, tvr.category, tvr.report_results_severity_status, "
      + "   ml.message_log_id, NULL as doc_format, NULL as doc_location, 'N' as is_test_doc "
      + "   FROM tests_prescribed tp " 
      + "   JOIN patient_details pd ON (pd.mr_no = tp.mr_no)  "
      + "   JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id)  "
      + "   JOIN message_log ml ON (ml.entity_id = tvr.report_id::text) "
      + "   JOIN message_recipient mr ON (mr.message_log_id = ml.message_log_id) "
      + "   WHERE ml.last_status <> 'D' "
      + "               AND ml.message_type_id = 'notification_diag_report_signed_off' "
      + "               AND ml.message_mode = 'NOTIFICATION'  "
      + "               AND coalesce(ml.notification_status, '') NOT IN ('D', 'A') "
      + "               AND mr.message_status <> 'R' "
      + "               AND tp.coll_prescribed_id IS NULL AND mr.message_recipient_id = ? "
      + "   GROUP BY tp.mr_no, patient_name, tvr.report_id, tvr.category,"
      + " tvr.report_results_severity_status, ml.message_log_id, tvr.report_name, doc_format, "
      + "doc_location, is_test_doc) "
      + "   UNION ALL "
      + "   (SELECT t.mr_no, get_patient_name(t.mr_no) as patient_name, td.doc_id AS report_id, "
      + "    CONCAT('supporting doc for ', d.test_name, ' : ',td.doc_name) AS report_name, "
      + "    dd.category,  NULL as report_results_severity_status,"
      + " ml.message_log_id, pd.doc_format, pd.doc_location, 'Y' as is_test_doc"
      + "               FROM tests_prescribed t "
      + "               JOIN patient_details p ON (p.mr_no = t.mr_no)  "
      + "               JOIN diagnostics d USING (test_id)  "
      + "               JOIN diagnostics_departments dd USING (ddept_id)  "
      + "               JOIN test_documents td USING (prescribed_id) "
      + "               JOIN patient_documents pd USING (doc_id) "
      + "               JOIN message_log ml ON (ml.entity_id = doc_id::text) "
      + "               JOIN message_recipient mr ON (mr.message_log_id = ml.message_log_id) "
      + "            WHERE "
      + "               t.coll_prescribed_id IS NULL AND t.test_doc_id IS NOT NULL "
      + "               AND ml.last_status <> 'D' "
      + "               AND ml.message_type_id = 'notification_diag_report_signed_off' "
      + "               AND coalesce(ml.notification_status, '') NOT IN ('D', 'A') "
      + "               AND mr.message_status <> 'R' "
      + "               AND mr.message_recipient_id = ? "
      + "    GROUP BY t.mr_no, patient_name, td.doc_id, category, report_results_severity_status, "
      + " ml.message_log_id, report_name, doc_format, doc_location, is_test_doc) "
      + ") AS foo ORDER BY message_log_id DESC OFFSET ? LIMIT ?";
  
  /**
   * Gets the message entities.
   *
   * @param messageType
   *          the message type
   * @param userId
   *          the user id
   * @param pageNum
   *          the page num
   * @param pageSize
   *          the page size
   * @return the message entities
   */
  @SuppressWarnings("rawtypes")
  public List getMessageEntities(String messageType, String userId, Integer pageNum,
      Integer pageSize) {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    List list = null;
    if (pageSize == null || pageSize < 0) {
      pageSize = 15;
    }

    if (pageNum == null || pageNum < 0) {
      pageNum = 0;
    }
    try {
      try {
        ps = con.prepareStatement(TEST_REPORT_ENTITIES);
        ps.setString(1, userId);
        ps.setString(2, userId);
        ps.setInt(3, pageNum * pageSize);
        ps.setInt(4, pageSize);
        list = DataBaseUtil.queryToDynaList(ps);
      } finally {
        if (ps != null) {
          ps.close();
        }
        if (con != null) {
          con.close();
        }
      }
    } catch (SQLException exe) {
      log.error(exe.getMessage(), exe);
    }
    return list != null ? list : Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.messaging.providers.QueryDataProvider#getMessageDataList(com.insta.hms.messaging.
   * MessageContext)
   */
  @Override
  public List<Map> getMessageDataList(MessageContext thisCtx) throws ParseException, SQLException {
    List<String> tokenList = new ArrayList<>();
    List<Map> dataList = new ArrayList<>();
    if (null != thisCtx) {
      Map eventData = thisCtx.getEventData();
      if (null != eventData) {
        dataList.add(eventData);
        tokenList.addAll(eventData.keySet());
      }
    }
    return dataList;
  }

}
