package com.insta.hms.patientcustomfields;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientCustomFieldsDAO.
 */
public class PatientCustomFieldsDAO {

  /** The Constant GET_PATIENT_VISIT_DETAILS. */
  private static final String GET_PATIENT_VISIT_DETAILS = 
      " SELECT * FROM patient_details_ext_view WHERE mr_no = ? ";

  /**
   * Gets the patient visit details bean.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @return the patient visit details bean
   * @throws SQLException
   *           the SQL exception
   */
  public static Map getPatientVisitDetailsBean(String mrNo, String patientId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    List<BasicDynaBean> list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String query = GET_PATIENT_VISIT_DETAILS 
          + ((patientId != null) ? " AND patient_id = ?" : "");
      ps = con.prepareStatement(query);
      ps.setString(1, mrNo);
      if (patientId != null) {
        ps.setString(2, patientId);
      }
      list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && list.size() > 0) {
        BasicDynaBean bean = (BasicDynaBean) list.get(0);
        return bean.getMap();
      }
      return null;

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
