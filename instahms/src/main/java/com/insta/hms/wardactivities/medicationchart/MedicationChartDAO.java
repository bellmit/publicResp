package com.insta.hms.wardactivities.medicationchart;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MedicationChartDAO.
 *
 * @author nikunj.s
 */
public class MedicationChartDAO {

  /** The Constant GET_MEDICATION_CHART_FIELDS. */
  public static final String GET_MEDICATION_CHART_FIELDS = "SELECT * ";

  /** The Constant COUNT. */
  private static final String COUNT = "SELECT count(*) ";

  /** The Constant TABLES. */
  private static final String TABLES = "FROM "
      + " ( SELECT pp.visit_id, pp.doctor_id, doc.doctor_name, 'M' as presc_type,"
      + " pmp.strength as med_dosage, mr.route_name as med_route_name, "
      + " pmp.item_strength as med_strength, pp.start_datetime, pp.end_datetime,"
      + " pp.prescribed_date as prescription_date, pmp.medicine_remarks as presc_remarks,"
      + " rdm.display_name as recurrence_name, pp.repeat_interval, "
      + " pp.repeat_interval_units, pmp.medicine_id as item_id, sid.medicine_name, "
      + " pa.activity_remarks, pa.completed_by, pa.completed_date, pp.freq_type "
      + " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp on (pmp.op_medicine_pres_id=pp.patient_presc_id)"
      + " JOIN doctors doc ON (pp.doctor_id=doc.doctor_id) "
      + " LEFT JOIN patient_activities pa ON (pp.patient_presc_id = pa.prescription_id) "
      + " LEFT JOIN store_item_details sid ON (pp.presc_type = 'Medicine' "
      + " AND pmp.medicine_id=sid.medicine_id) "
      + " LEFT JOIN recurrence_daily_master rdm "
      + " ON (pp.recurrence_daily_id=rdm.recurrence_daily_id) "
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + " LEFT JOIN medicine_route mr ON (pmp.route_of_admin=mr.route_id) "
      + " WHERE pp.presc_type = 'Medicine' AND pa.activity_status = 'D') AS foo ";

  /**
   * Gets the medication chart details.
   *
   * @param filter the filter
   * @param listingParams the listing params
   * @return the medication chart details
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  public static PagedList getMedicationChartDetails(Map filter, Map<LISTING, Object> listingParams)
      throws SQLException, Exception {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    SearchQueryBuilder qb = null;
    Map newFilter = new HashMap<>();
    try {
      int pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
      int pageNum = (Integer) listingParams.get(LISTING.PAGENUM);

      qb = new SearchQueryBuilder(con, GET_MEDICATION_CHART_FIELDS, COUNT, TABLES, null,
          (String) listingParams.get(LISTING.SORTCOL),
          (Boolean) listingParams.get(LISTING.SORTASC), pageSize, pageNum);
      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("completed_date", false);
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant MEDICATION_DOCS. */
  public static final String MEDICATION_DOCS = " SELECT pr.patient_id,"
      + " doc.doctor_name, pr.reg_date"
      + " FROM patient_prescription pp"
      + " JOIN patient_registration pr ON (pp.visit_id=pr.patient_id)"
      + " JOIN doctors doc ON (pr.doctor=doc.doctor_id) "
      + " JOIN patient_activities pa ON (pa.prescription_id=pp.patient_presc_id)";

  /**
   * Gets the medication chart for EMR.
   *
   * @param patientId the patient id
   * @param mrNo the mr no
   * @return the medication chart for EMR
   * @throws SQLException the SQL exception
   */
  public List getMedicationChartForEMR(String patientId, String mrNo) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(MEDICATION_DOCS + " WHERE "
          + (patientId != null ? " pr.patient_id=? " : " pr.mr_no=? AND pa.activity_status = 'D'")
          + " GROUP BY pr.patient_id, doc.doctor_name, pr.reg_date");
      ps.setString(1, patientId != null ? patientId : mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MEDICATION_CHART. */
  public static final String GET_MEDICATION_CHART = "SELECT pp.doctor_id, doc.doctor_name,"
      + " 'M' as presc_type, pmp.strength as med_dosage, mr.route_name as med_route_name,"
      + " pmp.item_strength as med_strength, pp.start_datetime, pp.end_datetime, "
      + " pp.prescribed_date as prescription_date, pmp.medicine_remarks as presc_remarks,"
      + " rdm.display_name as recurrence_name, pp.repeat_interval, pp.repeat_interval_units,"
      + " pmp.medicine_id::text as item_id,sid.medicine_name,  pa.activity_remarks,"
      + " pa.completed_by, pa.completed_date, pp.freq_type  "
      + " FROM patient_prescription pp  "
      + "    JOIN patient_medicine_prescriptions pmp "
      + " ON (pmp.op_medicine_pres_id=pp.patient_presc_id)"
      + "    JOIN doctors doc ON (pp.doctor_id=doc.doctor_id)"
      + "    LEFT JOIN patient_activities pa ON (pp.patient_presc_id = pa.prescription_id)  "
      + "    LEFT JOIN store_item_details sid ON (pp.presc_type = 'Medicine'"
      + "    AND pmp.medicine_id=sid.medicine_id) "
      + "    LEFT JOIN recurrence_daily_master rdm "
      + " ON (pp.recurrence_daily_id=rdm.recurrence_daily_id) "
      + "    LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + "    LEFT JOIN medicine_route mr ON (pmp.route_of_admin=mr.route_id)"
      + " WHERE pp.presc_type = 'Medicine' AND pa.activity_status = 'D'   AND (pp.visit_id = ? )"
      + " ORDER BY pa.completed_date ";

  /**
   * Gets the medication chart report.
   *
   * @param patientId the patient id
   * @return the medication chart report
   * @throws SQLException the SQL exception
   */
  public static List getMedicationChartReport(String patientId) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_MEDICATION_CHART);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
