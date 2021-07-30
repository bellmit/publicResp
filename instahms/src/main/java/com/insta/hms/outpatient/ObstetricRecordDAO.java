package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
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

/**
 * The Class ObstetricRecordDAO.
 */
public class ObstetricRecordDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(ObstetricRecordDAO.class);

  /**
   * Instantiates a new obstetric record DAO.
   */
  public ObstetricRecordDAO() {
    super("obstetric_headrecords");
  }

  /**
   * Gets the all active obstetric details.
   *
   * @param mrNo the mr no
   * @return the all active obstetric details
   * @throws SQLException the SQL exception
   */
  public static List getAllActiveObstetricDetails(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          " SELECT ord.*, psd.mr_no, psd.finalized, psd.finalized_user," + " usr.temp_username "
              + " FROM obstetric_headrecords ord " + "   JOIN patient_section_details psd"
              + " ON (ord.section_detail_id = psd.section_detail_id) "
              + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
              + " WHERE psd.mr_no = ? AND psd.section_id = -13 AND psd.section_status = 'A'" + "");
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all obstetric head details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all obstetric head details
   * @throws SQLException the SQL exception
   */
  public static List getAllObstetricHeadDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          " SELECT ord.*, psd.mr_no, psd.finalized," + " psd.finalized_user, usr.temp_username "
              + " FROM obstetric_headrecords ord " + "   JOIN patient_section_details psd"
              + " ON (ord.section_detail_id = psd.section_detail_id) "
              + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id)"
              + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
              + " WHERE psd.mr_no = ? AND psd.patient_id = ? "
              + " AND coalesce(psd.section_item_id, 0)=?  AND coalesce(psd.generic_form_id, 0)=?"
              + " AND item_type=? AND psd.section_id = -13" + " AND  psf.form_id=? " + " ");
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setInt(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setString(5, itemType);
      ps.setInt(6, formId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update obstetric details.
   *
   * @param con               the con
   * @param obstetricrecordId the obstetricrecord id
   * @param uniqueId          the unique id
   * @param fieldG            the field G
   * @param fieldP            the field P
   * @param fieldL            the field L
   * @param fieldA            the field A
   * @param userName          the user name
   * @param isInsert          the is insert
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean updateObstetricDetails(Connection con, String obstetricrecordId, Integer uniqueId,
      String fieldG, String fieldP, String fieldL, String fieldA, String userName, Boolean isInsert)
      throws SQLException, IOException {

    if (isInsert) {
      BasicDynaBean obstetricbean = getBean();
      obstetricbean.set("obstetric_record_id", new Integer(getNextSequence()));
      obstetricbean.set("section_detail_id", uniqueId);
      obstetricbean.set("field_g", fieldG.equals("") ? null : Integer.parseInt(fieldG));
      obstetricbean.set("field_p", fieldP.equals("") ? null : Integer.parseInt(fieldP));
      obstetricbean.set("field_l", fieldL.equals("") ? null : Integer.parseInt(fieldL));
      obstetricbean.set("field_a", fieldA.equals("") ? null : Integer.parseInt(fieldA));

      if (!insert(con, obstetricbean)) {
        return false;
      }
    } else {
      if (obstetricrecordId.equals("")) {
        BasicDynaBean obstetricbean = getBean();
        obstetricbean.set("obstetric_record_id", getNextSequence());
        obstetricbean.set("section_detail_id", uniqueId);
        obstetricbean.set("field_g", fieldG.equals("") ? null : Integer.parseInt(fieldG));
        obstetricbean.set("field_p", fieldP.equals("") ? null : Integer.parseInt(fieldP));
        obstetricbean.set("field_l", fieldL.equals("") ? null : Integer.parseInt(fieldL));
        obstetricbean.set("field_a", fieldA.equals("") ? null : Integer.parseInt(fieldA));

        if (!insert(con, obstetricbean)) {
          return false;
        }
      } else {
        BasicDynaBean obstetricbean = getBean();
        obstetricbean.set("section_detail_id", uniqueId);
        obstetricbean.set("field_g", fieldG.equals("") ? null : Integer.parseInt(fieldG));
        obstetricbean.set("field_p", fieldP.equals("") ? null : Integer.parseInt(fieldP));
        obstetricbean.set("field_l", fieldL.equals("") ? null : Integer.parseInt(fieldL));
        obstetricbean.set("field_a", fieldA.equals("") ? null : Integer.parseInt(fieldA));

        HashMap keys = new HashMap();
        keys.put("obstetric_record_id", new Integer(obstetricrecordId));
        if (update(con, obstetricbean.getMap(), keys) == 0) {
          return false;
        }
      }
    }
    return true;
  }

}
