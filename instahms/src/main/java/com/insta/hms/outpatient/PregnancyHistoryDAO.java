package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

/**
 * The Class PregnancyHistoryDAO.
 *
 * @author mohammed.r
 */
public class PregnancyHistoryDAO extends GenericDAO {

  static Logger log = LoggerFactory.getLogger(PregnancyHistoryDAO.class);

  /**
   * Instantiates a new pregnancy history DAO.
   */
  public PregnancyHistoryDAO() {
    super("pregnancy_history");
  }

  /**
   * Gets the all active pregnancy details.
   *
   * @param mrNo the mr no
   * @return the all active pregnancy details
   * @throws SQLException the SQL exception
   */
  public static List getAllActivePregnancyDetails(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          " SELECT ph.*, psd.mr_no, psd.finalized," + " psd.finalized_user, usr.temp_username "
              + " FROM pregnancy_history ph " + "   JOIN patient_section_details psd"
              + " ON (ph.section_detail_id = psd.section_detail_id) "
              + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
              + " WHERE psd.mr_no = ? AND psd.section_id = -13 AND psd.section_status = 'A'"
              + " ORDER BY ph.date");
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all pregnancy details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all pregnancy details
   * @throws SQLException the SQL exception
   */
  public static List getAllPregnancyDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          " SELECT ph.*, psd.mr_no, psd.finalized," + " psd.finalized_user, usr.temp_username "
              + " FROM pregnancy_history ph " + "   JOIN patient_section_details psd"
              + " ON (ph.section_detail_id = psd.section_detail_id) "
              + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id)"
              + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
              + " WHERE psd.mr_no = ? AND psd.patient_id = ? AND coalesce(psd.section_item_id, 0)=?"
              + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND psd.section_id = -13 "
              + " AND  psf.form_id=? " + " ORDER BY ph.date");
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
   * Update pregnancy details.
   *
   * @param con                   the con
   * @param pregnancyhistoryId    the pregnancyhistory id
   * @param uniqueId              the unique id
   * @param pregnancyDate         the pregnancy date
   * @param pregnancyWeek         the pregnancy week
   * @param pregnancyPlace        the pregnancy place
   * @param pregnancyMethod       the pregnancy method
   * @param pregnancyWeight       the pregnancy weight
   * @param pregnancySex          the pregnancy sex
   * @param pregnancyComplication the pregnancy complication
   * @param pregnancyFeeding      the pregnancy feeding
   * @param pregnancyOutcome      the pregnancy outcome
   * @param delete                the delete
   * @param edited                the edited
   * @param userName              the user name
   * @param isInsert              the is insert
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updatePregnancyDetails(Connection con, String pregnancyhistoryId, Integer uniqueId,
      String pregnancyDate, String pregnancyWeek, String pregnancyPlace, String pregnancyMethod,
      String pregnancyWeight, String pregnancySex, String pregnancyComplication,
      String pregnancyFeeding, String pregnancyOutcome, boolean delete, boolean edited,
      String userName, Boolean isInsert) throws SQLException, IOException, ParseException {

    if (isInsert) {
      if (!pregnancyDate.equals("")) {
        if (delete) {
          return true; // no need to insert or delete
        } else {
          BasicDynaBean pregnancyHistorybean = getBean();
          pregnancyHistorybean.set("pregnancy_history_id", getNextSequence());
          pregnancyHistorybean.set("section_detail_id", uniqueId);
          pregnancyHistorybean.set("date", DateUtil.parseDate(pregnancyDate));
          pregnancyHistorybean.set("weeks",
              pregnancyWeek.equals("") ? null : Integer.parseInt(pregnancyWeek));
          pregnancyHistorybean.set("place", pregnancyPlace);
          pregnancyHistorybean.set("method", pregnancyMethod);
          pregnancyHistorybean.set("weight",
              pregnancyWeight.equals("") ? null : BigDecimal.valueOf(new Double(pregnancyWeight)));
          pregnancyHistorybean.set("sex", pregnancySex);
          pregnancyHistorybean.set("complications", pregnancyComplication);
          pregnancyHistorybean.set("feeding", pregnancyFeeding);
          pregnancyHistorybean.set("outcome", pregnancyOutcome);
          pregnancyHistorybean.set("username", userName);
          pregnancyHistorybean.set("mod_time", DateUtil.getCurrentTimestamp());

          if (!insert(con, pregnancyHistorybean)) {
            return false;
          }
        }
      }
    } else {
      if (delete) {
        if (!delete(con, "pregnancy_history_id", new Integer(pregnancyhistoryId))) {
          return false;
        }
      } else if (!pregnancyhistoryId.equals("_") && edited) {
        BasicDynaBean pregnancyHistorybean = getBean();
        pregnancyHistorybean.set("section_detail_id", uniqueId);
        pregnancyHistorybean.set("date", DateUtil.parseDate(pregnancyDate));
        pregnancyHistorybean.set("weeks",
            pregnancyWeek.equals("") ? null : Integer.parseInt(pregnancyWeek));
        pregnancyHistorybean.set("place", pregnancyPlace);
        pregnancyHistorybean.set("method", pregnancyMethod);
        pregnancyHistorybean.set("weight",
            pregnancyWeight.equals("") ? null : BigDecimal.valueOf(new Double(pregnancyWeight)));
        pregnancyHistorybean.set("sex", pregnancySex);
        pregnancyHistorybean.set("complications", pregnancyComplication);
        pregnancyHistorybean.set("feeding", pregnancyFeeding);
        pregnancyHistorybean.set("outcome", pregnancyOutcome);
        pregnancyHistorybean.set("username", userName);
        pregnancyHistorybean.set("mod_time", DateUtil.getCurrentTimestamp());

        HashMap keys = new HashMap();
        keys.put("pregnancy_history_id", new Integer(pregnancyhistoryId));
        if (update(con, pregnancyHistorybean.getMap(), keys) == 0) {
          return false;
        }
      } else if (pregnancyhistoryId.equals("_") && !pregnancyDate.equals("")) {
        BasicDynaBean pregnancyHistorybean = getBean();
        pregnancyHistorybean.set("pregnancy_history_id", getNextSequence());
        pregnancyHistorybean.set("section_detail_id", uniqueId);
        pregnancyHistorybean.set("date", DateUtil.parseDate(pregnancyDate));
        pregnancyHistorybean.set("weeks",
            pregnancyWeek.equals("") ? null : Integer.parseInt(pregnancyWeek));
        pregnancyHistorybean.set("place", pregnancyPlace);
        pregnancyHistorybean.set("method", pregnancyMethod);
        pregnancyHistorybean.set("weight",
            pregnancyWeight.equals("") ? null : BigDecimal.valueOf(new Double(pregnancyWeight)));
        pregnancyHistorybean.set("sex", pregnancySex);
        pregnancyHistorybean.set("complications", pregnancyComplication);
        pregnancyHistorybean.set("feeding", pregnancyFeeding);
        pregnancyHistorybean.set("outcome", pregnancyOutcome);
        pregnancyHistorybean.set("username", userName);
        pregnancyHistorybean.set("mod_time", DateUtil.getCurrentTimestamp());

        if (!insert(con, pregnancyHistorybean)) {
          return false;
        }
      }
    }
    return true;
  }

}
