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

// TODO: Auto-generated Javadoc
/**
 * The Class HealthMaintenanceDAO.
 *
 * @author nikunj.s
 */
public class HealthMaintenanceDAO extends GenericDAO {

  static Logger log = LoggerFactory.getLogger(HealthMaintenanceDAO.class);

  /**
   * Instantiates a new health maintenance DAO.
   */
  public HealthMaintenanceDAO() {
    super("patient_health_maintenance");
  }

  /**
   * Gets the all active health maintenance.
   *
   * @param mrNo the mr no
   * @return the all active health maintenance
   * @throws SQLException the SQL exception
   */
  public static List getAllActiveHealthMaintenance(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT phm.*, d.doctor_name, psd.mr_no, psd.finalized,"
          + " psd.finalized_user, usr.temp_username " + " FROM patient_health_maintenance phm "
          + "   LEFT JOIN doctors d ON (phm.doctor_id=d.doctor_id)"
          + "   JOIN patient_section_details psd"
          + " ON (phm.section_detail_id = psd.section_detail_id) "
          + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no = ? AND psd.section_id = -15 AND psd.section_status = 'A'"
          + " ORDER BY phm.recorded_date ");
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all health maintenance.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all health maintenance
   * @throws SQLException the SQL exception
   */
  public static List getAllHealthMaintenance(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT phm.*, d.doctor_name, psd.mr_no, psd.finalized,"
          + " psd.finalized_user, usr.temp_username " + " FROM patient_health_maintenance phm "
          + "   LEFT JOIN doctors d ON (phm.doctor_id=d.doctor_id)"
          + "   JOIN patient_section_details psd"
          + " ON (phm.section_detail_id = psd.section_detail_id) "
          + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
          + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=? "
          + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND psd.section_id = -15 "
          + " AND psf.form_id=? " + " ORDER BY phm.recorded_date");
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
   * Update health maintenance details.
   *
   * @param con           the con
   * @param healthMaintId the health maint id
   * @param uniqueId      the unique id
   * @param doctorId      the doctor id
   * @param activity      the activity
   * @param date          the date
   * @param dueBy         the due by
   * @param remarks       the remarks
   * @param status        the status
   * @param delete        the delete
   * @param edited        the edited
   * @param userName      the user name
   * @param isInsert      the is insert
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updateHealthMaintenanceDetails(Connection con, String healthMaintId,
      Integer uniqueId, String doctorId, String activity, String date, String dueBy, String remarks,
      String status, boolean delete, boolean edited, String userName, Boolean isInsert)
      throws SQLException, IOException, ParseException {

    if (isInsert) {
      if (!activity.equals("")) {
        if (delete) {
          return true; // no need to insert or delete
        } else {
          BasicDynaBean healthMaintbean = getBean();
          healthMaintbean.set("health_maint_id", new Integer(getNextSequence()));
          healthMaintbean.set("section_detail_id", uniqueId);
          healthMaintbean.set("doctor_id", doctorId);
          healthMaintbean.set("activity", activity);
          healthMaintbean.set("remarks", remarks);
          healthMaintbean.set("status", status);
          healthMaintbean.set("recorded_date", DateUtil.parseTimestamp(date));
          healthMaintbean.set("due_by", dueBy);
          healthMaintbean.set("username", userName);
          healthMaintbean.set("mod_time", DateUtil.getCurrentTimestamp());

          if (!insert(con, healthMaintbean)) {
            return false;
          }
        }
      }
    } else {
      if (delete) {
        if (!delete(con, "health_maint_id", new Integer(healthMaintId))) {
          return false;
        }
      } else if (!healthMaintId.equals("_") && edited) {
        BasicDynaBean healthMaintbean = getBean();
        healthMaintbean.set("section_detail_id", uniqueId);
        healthMaintbean.set("doctor_id", doctorId);
        healthMaintbean.set("activity", activity);
        healthMaintbean.set("remarks", remarks);
        healthMaintbean.set("status", status);
        healthMaintbean.set("recorded_date", DateUtil.parseTimestamp(date));
        healthMaintbean.set("due_by", dueBy);
        healthMaintbean.set("username", userName);
        healthMaintbean.set("mod_time", DateUtil.getCurrentTimestamp());

        HashMap keys = new HashMap();
        keys.put("health_maint_id", new Integer(healthMaintId));
        if (update(con, healthMaintbean.getMap(), keys) == 0) {
          return false;
        }
      } else if (healthMaintId.equals("_") && !activity.equals("")) {
        BasicDynaBean healthMaintbean = getBean();
        healthMaintbean.set("health_maint_id", getNextSequence());
        healthMaintbean.set("section_detail_id", uniqueId);
        healthMaintbean.set("doctor_id", doctorId);
        healthMaintbean.set("activity", activity);
        healthMaintbean.set("remarks", remarks);
        healthMaintbean.set("status", status);
        healthMaintbean.set("recorded_date", DateUtil.parseTimestamp(date));
        healthMaintbean.set("due_by", dueBy);
        healthMaintbean.set("username", userName);
        healthMaintbean.set("mod_time", DateUtil.getCurrentTimestamp());

        if (!insert(con, healthMaintbean)) {
          return false;
        }
      }
    }
    return true;
  }
}
