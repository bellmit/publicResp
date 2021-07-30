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
import java.util.Map;

/**
 * The Class AntenatalDAO.
 *
 * @author mohammed.r
 */
public class AntenatalDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(AntenatalDAO.class);

  /**
   * Instantiates a new antenatal DAO.
   */
  public AntenatalDAO() {
    super("antenatal");
  }

  /** The antenatal main dao. */
  private GenericDAO antenatalMainDao = new GenericDAO("antenatal_main");

  /**
   * Gets the all active antenatal details.
   *
   * @param mrNo the mr no
   * @return the all active antenatal details
   * @throws SQLException the SQL exception
   */
  public static List getAllActiveAntenatalDetails(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT al.*,d.doctor_name, psd.mr_no, psd.finalized,"
          + " psd.finalized_user, usr.temp_username, "
          + " am.antenatal_main_id, psd.section_detail_id " + " FROM antenatal_main am "
          + " JOIN antenatal al ON (al.antenatal_main_id = am.antenatal_main_id)"
          + "   LEFT JOIN doctors d ON (al.doctor_id=d.doctor_id)"
          + "   JOIN patient_section_details psd"
          + " ON (am.section_detail_id = psd.section_detail_id) "
          + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + " WHERE psd.mr_no = ? AND psd.section_id = -14 AND psd.section_status = 'A'"
          + " ORDER BY al.visit_date ");
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all antenatal details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all antenatal details
   * @throws SQLException the SQL exception
   */
  public static List getAllAntenatalDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT al.antenatal_id, "
          + " al.visit_date , al.gestation_age, al.height_fundus,"
          + " al.presentation, al.rel_pp_brim, "
          + " al.foetal_heart, al.urine, al.weight, al.prescription_summary,"
          + " al.doctor_id, al.next_visit_date, al.mod_time, al.username,"
          + " al.systolic_bp, al.diastolic_bp, "
          + " al.movement, al.position, alm.antenatal_main_id, "
          + " alm.section_detail_id, alm.lmp, alm.edd, alm.final_edd,"
          + " alm.pregnancy_result, alm.pregnancy_result_date, alm.number_of_birth,"
          + " alm.remarks, alm.pregnancy_count, "
          + " alm.created_by, alm.modified_by, alm.modified_at, "
          + " alm.pregnancy_count::text as pregnancy_count_key, " + " d.doctor_name, "
          + " psd.mr_no, psd.finalized, psd.finalized_user, psd.section_detail_id,"
          + " usr.temp_username " + " FROM antenatal_main alm "
          + " JOIN antenatal al ON (al.antenatal_main_id = alm.antenatal_main_id)"
          + "   LEFT JOIN doctors d ON (al.doctor_id=d.doctor_id)"
          + "   JOIN patient_section_details psd"
          + " ON (alm.section_detail_id = psd.section_detail_id) "
          + " JOIN patient_section_forms psf" + " ON (psf.section_detail_id=psd.section_detail_id) "
          + "   LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
          + "   WHERE psd.mr_no = ? AND psd.patient_id = ? AND"
          + " coalesce(psd.section_item_id, 0)=? "
          + " AND coalesce(psd.generic_form_id, 0)=? AND item_type=? AND"
          + " psd.section_id = -14 AND psf.form_id=? " + " ORDER BY al.visit_date");
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
   * Insert or update antenatal main.
   *
   * @param con              the con
   * @param sectionDetailsId the section details id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private BasicDynaBean insertOrUpdateAntenatalMain(Connection con, Integer sectionDetailsId)
      throws SQLException, IOException {
    Map<String, Object> map = new HashMap<>();
    map.put("section_detail_id", sectionDetailsId);
    BasicDynaBean findAntenatalMainBean = antenatalMainDao.findByKey(con, map);
    BasicDynaBean antenatalMainBean = antenatalMainDao.getBean();

    antenatalMainBean.set("pregnancy_count", 1);
    antenatalMainBean.set("section_detail_id", sectionDetailsId);

    if (null != findAntenatalMainBean) {
      antenatalMainBean.set("antenatal_main_id", findAntenatalMainBean.get("antenatal_main_id"));
      // Not updating antenatal main table from old jsp screen
    } else {
      antenatalMainBean.set("antenatal_main_id", antenatalMainDao.getNextSequence());
      antenatalMainDao.insert(con, antenatalMainBean);
    }

    return antenatalMainBean;
  }

  /**
   * Update antenatal details.
   *
   * @param con                 the con
   * @param antenatalId         the antenatal id
   * @param uniqueId            the unique id
   * @param visitDate           the visit date
   * @param gestationAge        the gestation age
   * @param heightFundus        the height fundus
   * @param presentation        the presentation
   * @param relPpBrim           the rel pp brim
   * @param foetalHeart         the foetal heart
   * @param urine               the urine
   * @param sbp                 the sbp
   * @param dbp                 the dbp
   * @param weight              the weight
   * @param prescriptionSummary the prescription summary
   * @param doctorId            the doctor id
   * @param nextvisitDate       the nextvisit date
   * @param delete              the delete
   * @param edited              the edited
   * @param userName            the user name
   * @param isInsert            the is insert
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updateAntenatalDetails(Connection con, String antenatalId, Integer uniqueId,
      String visitDate, String gestationAge, String heightFundus, String presentation,
      String relPpBrim, String foetalHeart, String urine, String sbp, String dbp, String weight,
      String prescriptionSummary, String doctorId, String nextvisitDate, boolean delete,
      boolean edited, String userName, Boolean isInsert)
      throws SQLException, IOException, ParseException {

    BasicDynaBean antenatalMainBean = insertOrUpdateAntenatalMain(con, uniqueId);
    if (isInsert) {
      if (!visitDate.equals("")) {
        if (delete) {
          return true; // no need to insert or delete
        } else {

          BasicDynaBean antenatalbean = getBean();
          antenatalbean.set("antenatal_id", getNextSequence());
          antenatalbean.set("obsolete_section_detail_id", uniqueId);
          antenatalbean.set("visit_date", DateUtil.parseDate(visitDate));
          antenatalbean.set("gestation_age", gestationAge.equals("") ? null : gestationAge);
          antenatalbean.set("height_fundus", heightFundus.equals("") ? null
              : BigDecimal.valueOf(Double.parseDouble(heightFundus)));
          antenatalbean.set("presentation", presentation);
          antenatalbean.set("rel_pp_brim", relPpBrim);
          antenatalbean.set("foetal_heart", foetalHeart);
          antenatalbean.set("urine", urine);
          antenatalbean.set("systolic_bp", sbp.equals("") ? null : Integer.parseInt(sbp));
          antenatalbean.set("diastolic_bp", dbp.equals("") ? null : Integer.parseInt(dbp));
          antenatalbean.set("weight",
              weight.equals("") ? null : BigDecimal.valueOf(Double.parseDouble(weight)));
          antenatalbean.set("prescription_summary", prescriptionSummary);
          antenatalbean.set("doctor_id", doctorId);
          antenatalbean.set("next_visit_date", DateUtil.parseDate(nextvisitDate));
          antenatalbean.set("username", userName);
          antenatalbean.set("mod_time", DateUtil.getCurrentTimestamp());
          antenatalbean.set("antenatal_main_id", antenatalMainBean.get("antenatal_main_id"));
          if (!insert(con, antenatalbean)) {
            return false;
          }
        }
      }
    } else {
      if (delete) {
        if (!delete(con, "antenatal_id", new Integer(antenatalId))) {
          return false;
        }
      } else if (!antenatalId.equals("_") && edited) {
        BasicDynaBean antenatalbean = getBean();
        antenatalbean.set("obsolete_section_detail_id", uniqueId);
        antenatalbean.set("visit_date", DateUtil.parseDate(visitDate));
        antenatalbean.set("gestation_age", gestationAge.equals("") ? null : gestationAge);
        antenatalbean.set("height_fundus",
            heightFundus.equals("") ? null : BigDecimal.valueOf(Double.parseDouble(heightFundus)));
        antenatalbean.set("presentation", presentation);
        antenatalbean.set("rel_pp_brim", relPpBrim);
        antenatalbean.set("foetal_heart", foetalHeart);
        antenatalbean.set("urine", urine);
        antenatalbean.set("systolic_bp", sbp.equals("") ? null : Integer.parseInt(sbp));
        antenatalbean.set("diastolic_bp", dbp.equals("") ? null : Integer.parseInt(dbp));
        antenatalbean.set("weight",
            weight.equals("") ? null : BigDecimal.valueOf(Double.parseDouble(weight)));
        antenatalbean.set("prescription_summary", prescriptionSummary);
        antenatalbean.set("doctor_id", doctorId);
        antenatalbean.set("next_visit_date", DateUtil.parseDate(nextvisitDate));
        antenatalbean.set("username", userName);
        antenatalbean.set("mod_time", DateUtil.getCurrentTimestamp());

        HashMap keys = new HashMap();
        keys.put("antenatal_id", new Integer(antenatalId));
        if (update(con, antenatalbean.getMap(), keys) == 0) {
          return false;
        }
      } else if (antenatalId.equals("_") && !visitDate.equals("")) {
        antenatalMainBean.set("pregnancy_count", 1);
        antenatalMainBean.set("section_detail_id", uniqueId);

        BasicDynaBean antenatalbean = getBean();
        antenatalbean.set("antenatal_id", getNextSequence());
        antenatalbean.set("obsolete_section_detail_id", uniqueId);
        antenatalbean.set("visit_date", DateUtil.parseDate(visitDate));
        antenatalbean.set("gestation_age", gestationAge.equals("") ? null : gestationAge);
        antenatalbean.set("height_fundus",
            heightFundus.equals("") ? null : BigDecimal.valueOf(Double.parseDouble(heightFundus)));
        antenatalbean.set("presentation", presentation);
        antenatalbean.set("rel_pp_brim", relPpBrim);
        antenatalbean.set("foetal_heart", foetalHeart);
        antenatalbean.set("urine", urine);
        antenatalbean.set("systolic_bp", sbp.equals("") ? null : Integer.parseInt(sbp));
        antenatalbean.set("diastolic_bp", dbp.equals("") ? null : Integer.parseInt(dbp));
        antenatalbean.set("weight",
            weight.equals("") ? null : BigDecimal.valueOf(Double.parseDouble(weight)));
        antenatalbean.set("prescription_summary", prescriptionSummary);
        antenatalbean.set("doctor_id", doctorId);
        antenatalbean.set("next_visit_date", DateUtil.parseDate(nextvisitDate));
        antenatalbean.set("username", userName);
        antenatalbean.set("mod_time", DateUtil.getCurrentTimestamp());
        antenatalbean.set("antenatal_main_id", antenatalMainBean.get("antenatal_main_id"));

        if (!insert(con, antenatalbean)) {
          return false;
        }
      }
    }
    return true;
  }

}
