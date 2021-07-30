package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SecondaryComplaintDAO.
 *
 * @author krishna
 */
public class SecondaryComplaintDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(SecondaryComplaintDAO.class);

  /**
   * Instantiates a new secondary complaint DAO.
   */
  public SecondaryComplaintDAO() {
    super("secondary_complaints");
  }

  /**
   * Gets the secondary complaints.
   *
   * @param patientId the patient id
   * @return the secondary complaints
   * @throws SQLException the SQL exception
   */
  public List getSecondaryComplaints(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("SELECT sc.complaint, sc.visit_id, sc.row_id"
          + " FROM secondary_complaints sc "
          + " JOIN patient_registration pr ON (pr.patient_id = sc.visit_id) "
          + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND"
          + " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " + " WHERE sc.visit_id=?");
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * insert/update/delete the seconday complaint.
   *
   * @param con            the con
   * @param rowIds         the row ids
   * @param complaintNames the complaint names
   * @param visitId        the visit id
   * @param userName       the user name
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public Map<String, Object> insert(Connection con, String[] rowIds, String[] complaintNames,
      String visitId, String userName) throws SQLException, IOException {
    Map<String, Object> resultMap = new HashMap<String, Object>();

    if (rowIds == null || complaintNames == null || rowIds.length == 0
        || complaintNames.length == 0) {

      resultMap.put("isSuccess", true);
      resultMap.put("msg", "secondary complaint array found null or empty.");
      log.debug("secondary complaint array found null or empty.");
      return resultMap;
    }

    for (int i = 0; i < rowIds.length; i++) {
      String complaint = complaintNames[i];

      BasicDynaBean scBean = getBean();
      scBean.set("complaint", complaint);
      scBean.set("visit_id", visitId);
      scBean.set("username", userName);
      scBean.set("mod_time", DateUtil.getCurrentTimestamp());
      String rowId = rowIds[i];
      if (rowId.equals("_")) {
        if (!complaint.equals("")) {
          scBean.set("row_id", getNextSequence());
          if (!insert(con, scBean)) {
            resultMap.put("isSuccess", false);
            resultMap.put("msg",
                "Failed to insert the complaint in secondary complaint : " + complaint);
            log.error("Failed to insert the complaint in secondary complaint : " + complaint);
            return resultMap;
          }
        }

      } else {
        // user deleted the saved complaint in the ui, so delete that perticular row.
        if (complaint.equals("") && updateUserName(con, Integer.parseInt(rowId), userName)
            && !delete(con, "row_id", Integer.parseInt(rowId))) {
          resultMap.put("isSuccess", false);
          resultMap.put("msg", "Failed to delete the secondary complaint for id : " + rowId);
          log.error("Failed to delete the secondary complaint for id : " + rowId);
          return resultMap;
        }
        if (!complaint.equals("")
            && update(con, scBean.getMap(), "row_id", Integer.parseInt(rowId)) == 0) {
          // update the saved complaint
          resultMap.put("isSuccess", false);
          resultMap.put("msg", "Failed to update the secondary complaint for id : " + rowId
              + " : complaint : " + complaint);
          log.error("Failed to update the secondary complaint for id : " + rowId + " : complaint : "
              + complaint);
          return resultMap;
        }
      }

    }
    resultMap.put("isSuccess", true);
    resultMap.put("msg", "Transaction Successfull..");
    return resultMap;

  }

  /**
   * Copy complaints.
   *
   * @param con                  the con
   * @param visitDetailsBean     the visit details bean
   * @param latestEpisodeVisitId the latest episode visit id
   * @param username             the username
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean copyComplaints(Connection con, BasicDynaBean visitDetailsBean,
      String latestEpisodeVisitId, String username) throws SQLException, IOException {
    boolean success = true;
    String opType = (String) visitDetailsBean.get("op_type");

    if (latestEpisodeVisitId != null && opType != null
        && (opType.equals("F") || opType.equals("D"))) {

      String visitId = (String) visitDetailsBean.get("patient_id");

      List<BasicDynaBean> previousVisitSecComplaints = findAllByKey("visit_id",
          latestEpisodeVisitId);
      if (previousVisitSecComplaints != null && previousVisitSecComplaints.size() > 0) {
        for (BasicDynaBean complbean : previousVisitSecComplaints) {
          int id = DataBaseUtil.getNextSequence("secondary_complaints_seq");
          complbean.set("row_id", id);
          complbean.set("visit_id", visitId);
        }

        success = insertAll(con, previousVisitSecComplaints);
      }
    }
    return success;
  }

  /** The Constant UPDATE_USERNAME. */
  private static final String UPDATE_USERNAME = " UPDATE secondary_complaints"
      + " SET username = ? WHERE row_id = ? ";

  /**
   * Update user name.
   *
   * @param con      the con
   * @param rowId    the row id
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateUserName(Connection con, int rowId, String userName)
      throws SQLException, IOException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_USERNAME);
      ps.setString(1, userName);
      ps.setInt(2, rowId);
      return ps.executeUpdate() > 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID. */
  private static final String UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID = "UPDATE secondary_complaints"
      + " SET visit_id=? WHERE visit_id=?";

  /**
   * Update visit id.
   *
   * @param con       the con
   * @param opVisitId the op visit id
   * @param ipVisitId the ip visit id
   * @throws SQLException the SQL exception
   */
  public static void updateVisitId(Connection con, String opVisitId, String ipVisitId)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID);
    ps.setString(1, ipVisitId);
    ps.setString(2, opVisitId);
    ps.executeUpdate();
    ps.close();
  }

}
