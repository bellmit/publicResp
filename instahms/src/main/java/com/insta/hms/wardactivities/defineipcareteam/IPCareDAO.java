package com.insta.hms.wardactivities.defineipcareteam;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class IPCareDAO.
 */
public class IPCareDAO extends GenericDAO {
  
  private static final GenericDAO visitCareTeamDAO = new GenericDAO("visit_care_team");
  
  /**
   * Instantiates a new IP care DAO.
   */
  public IPCareDAO() {
    super("visit_care_team");
  }

  /** The Constant GET_CARE_DETAILS. */
  public static final String GET_CARE_DETAILS = " Select vct.patient_id, vct.care_doctor_id,"
      + " vct.username,vct.mod_time,doc.doctor_name,doc.dept_id,dept.dept_name "
      + " From visit_care_team vct "
      + " JOIN doctors doc ON (vct.care_doctor_id=doc.doctor_id) "
      + " JOIN department dept ON (doc.dept_id=dept.dept_id) " + " where # order by vct.mod_time";

  /**
   * Gets the visit care details.
   *
   * @param patientId the patient id
   * @return the visit care details
   * @throws SQLException the SQL exception
   */
  public List getVisitCareDetails(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = GET_CARE_DETAILS;
    try {
      query = query.replace("#", " patient_id=?");
      ps = con.prepareStatement(query);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Insert visit care deatils.
   *
   * @param con the con
   * @param visitDetailsBean the visit details bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean insertVisitCareDeatils(Connection con, BasicDynaBean visitDetailsBean)
      throws SQLException, IOException {
    boolean success = false;
    java.util.Date parsedDate = new java.util.Date();
    java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());

    try {
      BasicDynaBean visitCareBean = visitCareTeamDAO.getBean();
      visitCareBean.set("patient_id", visitDetailsBean.get("patient_id"));
      visitCareBean.set("care_doctor_id", visitDetailsBean.get("doctor"));
      visitCareBean.set("username", visitDetailsBean.get("user_name"));
      visitCareBean.set("mod_time", new Timestamp(datetime.getTime()));
      success = visitCareTeamDAO.insert(con, visitCareBean);

    } finally {
      DataBaseUtil.closeConnections(null, null);
    }
    return success;
  }

  /**
   * Update care daetaild.
   *
   * @param con the con
   * @param patientId the patient id
   * @param oldCareId the old care id
   * @param newCareId the new care id
   * @param userName the user name
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void updateCareDaetaild(Connection con, String patientId, String oldCareId,
      String newCareId, String userName) throws SQLException, IOException {
    java.util.Date parsedDate = new java.util.Date();
    int success;
    java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());
    try {

      LinkedHashMap<String, Object> keys = new LinkedHashMap<String, Object>();
      BasicDynaBean visitCareBean = visitCareTeamDAO.getBean();
      keys.put("patient_id", patientId);
      keys.put("care_doctor_id", oldCareId);

      LinkedHashMap<String, Object> keyCol = new LinkedHashMap<String, Object>();
      keyCol.put("patient_id", patientId);
      keyCol.put("care_doctor_id", newCareId);
      keyCol.put("username", userName);
      keyCol.put("mod_time", new Timestamp(datetime.getTime()));
      success = visitCareTeamDAO.update(con, keyCol, keys);

    } finally {
      DataBaseUtil.closeConnections(null, null);
    }
  }

}
