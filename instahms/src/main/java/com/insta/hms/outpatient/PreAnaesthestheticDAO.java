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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

/**
 * The Class PreAnaesthestheticDAO.
 *
 * @author nikunj.s
 */

public class PreAnaesthestheticDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(PreAnaesthestheticDAO.class);

  /**
   * Instantiates a new pre anaesthesthetic DAO.
   */
  public PreAnaesthestheticDAO() {
    super("patient_pac");
  }

  /**
   * Gets the action PAC records.
   *
   * @param mrNo the mr no
   * @return the action PAC records
   * @throws SQLException the SQL exception
   */
  public static List getActionPACRecords(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      StringBuilder query = new StringBuilder(
          " SELECT ppac.*, d.doctor_name," + " psd.mr_no, psd.finalized, "
              + " psd.finalized_user, usr.temp_username FROM patient_pac ppac "
              + " LEFT JOIN doctors d ON (ppac.doctor_id=d.doctor_id)"
              + " JOIN patient_section_details psd"
              + " ON (ppac.section_detail_id = psd.section_detail_id)  "
              + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username ");
      query.append(
          " WHERE psd.mr_no = ? AND psd.section_id = -16 AND" + " psd.section_status = 'A' ");
      query.append(" ORDER BY ppac.pac_validity desc ");
      ps = con.prepareStatement(query.toString());
      ps.setString(1, mrNo);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all PAC records.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all PAC records
   * @throws SQLException the SQL exception
   */
  public static List getAllPACRecords(String mrNo, String patientId, int itemId, int genericFormId,
      int formId, String itemType) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String query = " SELECT ppac.*, d.doctor_name, psd.mr_no, psd.finalized,"
          + " psd.finalized_user, usr.emp_username," + " usr.temp_username "
          + " FROM patient_pac ppac " + "   LEFT JOIN doctors d ON (ppac.doctor_id=d.doctor_id)"
          + "   JOIN patient_section_details psd"
          + " ON (ppac.section_detail_id = psd.section_detail_id) "
          + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
          + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
          + " AND coalesce(psd.generic_form_id, 0) = ? AND item_type=? AND"
          + " psd.section_id = -16 AND psf.form_id=?" + " ORDER BY ppac.pac_validity desc";
      ps = con.prepareStatement(query);
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
   * Update pre anaesthesthetic checkup.
   *
   * @param con          the con
   * @param patientPacId the patient pac id
   * @param uniqueId     the unique id
   * @param doctorId     the doctor id
   * @param status       the status
   * @param remarks      the remarks
   * @param checkupDate  the checkup date
   * @param validityDate the validity date
   * @param userName     the user name
   * @param delete       the delete
   * @param edited       the edited
   * @param isInsert     the is insert
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updatePreAnaesthestheticCheckup(Connection con, String patientPacId,
      Integer uniqueId, String doctorId, String status, String remarks, String checkupDate,
      String validityDate, String userName, boolean delete, boolean edited, Boolean isInsert)
      throws SQLException, IOException, ParseException {

    if (isInsert) {
      if (!remarks.equals("")) {
        if (delete) {
          return true; // no need to insert or delete
        } else {
          BasicDynaBean pacBean = getBean();
          pacBean.set("patient_pac_id", getNextSequence());
          pacBean.set("section_detail_id", uniqueId);
          pacBean.set("doctor_id", doctorId);
          pacBean.set("patient_pac_remarks", remarks);
          pacBean.set("status", status);
          pacBean.set("pac_date", DateUtil.parseTimestamp(checkupDate));
          pacBean.set("pac_validity", DateUtil.parseTimestamp(validityDate));
          pacBean.set("username", userName);
          pacBean.set("mod_time", DateUtil.getCurrentTimestamp());

          if (!insert(con, pacBean)) {
            return false;
          }
        }
      }
    } else {
      if (delete) {
        if (!delete(con, "patient_pac_id", new Integer(patientPacId))) {
          return false;
        }
      } else if (!patientPacId.equals("_") && edited) {
        BasicDynaBean pacBean = getBean();
        pacBean.set("patient_pac_id", getNextSequence());
        pacBean.set("section_detail_id", uniqueId);
        pacBean.set("doctor_id", doctorId);
        pacBean.set("patient_pac_remarks", remarks);
        pacBean.set("status", status);
        pacBean.set("pac_date", DateUtil.parseTimestamp(checkupDate));
        pacBean.set("pac_validity", DateUtil.parseTimestamp(validityDate));
        pacBean.set("username", userName);
        pacBean.set("mod_time", DateUtil.getCurrentTimestamp());

        HashMap keys = new HashMap();
        keys.put("patient_pac_id", new Integer(patientPacId));
        if (update(con, pacBean.getMap(), keys) == 0) {
          return false;
        }
      } else if (patientPacId.equals("_") && !remarks.equals("")) {
        BasicDynaBean pacBean = getBean();
        pacBean.set("patient_pac_id", new Integer(getNextSequence()));
        pacBean.set("section_detail_id", uniqueId);
        pacBean.set("doctor_id", doctorId);
        pacBean.set("patient_pac_remarks", remarks);
        pacBean.set("status", status);
        pacBean.set("pac_date", DateUtil.parseTimestamp(checkupDate));
        pacBean.set("pac_validity", DateUtil.parseTimestamp(validityDate));
        pacBean.set("username", userName);
        pacBean.set("mod_time", DateUtil.getCurrentTimestamp());

        if (!insert(con, pacBean)) {
          return false;
        }
      }
    }
    return true;
  }

}