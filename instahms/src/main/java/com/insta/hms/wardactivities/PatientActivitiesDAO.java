package com.insta.hms.wardactivities;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class PatientActivitiesDAO.
 *
 * @author krishna
 */
public class PatientActivitiesDAO extends GenericDAO {

  /**
   * Instantiates a new patient activities DAO.
   */
  public PatientActivitiesDAO() {
    super("patient_activities");
  }

  /** The Constant ACTIVITIES. */
  private static final String ACTIVITIES = "SELECT a.activity_id, a.activity_type,"
      + " a.activity_status, a.due_date, a.activity_num, a.order_no, "
      + " a.activity_remarks as activity_remarks, COALESCE(pmp.medicine_remarks,"
      + " pomp.medicine_remarks,ptp.test_remarks, psp.service_remarks, pcp.cons_remarks,"
      + " potp.item_remarks) as remarks, a.completed_date, a.prescription_type, a.completed_by,"
      + " a.ordered_by, a.prescription_id, CASE WHEN activity_type = 'G' THEN gen_activity_details"
      + " WHEN presc_type = 'NonBillable' THEN item_name WHEN presc_type = 'Medicine' THEN"
      + " sid.medicine_name WHEN presc_type = 'Inv.' THEN atp.test_name"
      + " WHEN presc_type = 'Doctor' THEN cdoc.doctor_name WHEN presc_type = 'Service' THEN"
      + " service_name END item_name,item_form_name as med_form_name,"
      + " rdm.display_name as recurrence_name, pmp.item_strength as med_strength, pp.freq_type, "
      + " pp.repeat_interval, pp.repeat_interval_units, pp.start_datetime, pp.end_datetime, "
      + " pp.no_of_occurrences, pp.end_on_discontinue, "
      + " COALESCE(pmp.medicine_remarks, pomp.medicine_remarks, ptp.test_remarks,"
      + " psp.service_remarks,"
      + " pcp.cons_remarks, potp.item_remarks) as presc_remarks, pmp.strength as med_dosage,"
      + " sid.cons_uom_id, cum.consumption_uom, doc.doctor_name, med_batch, med_exp_date,"
      + " pp.doctor_id, COALESCE(pmp.medicine_id::text, ptp.test_id, psp.service_id, pcp.doctor_id,"
      + " '') AS item_id, ordered_datetime, pmp.admin_strength,"
      +
        // for reconducted tests, get the common order id and conducted status of newly inserted
        // prescription.
        " (CASE WHEN tp.new_test_prescribed_id IS NULL THEN tp.conducted ELSE "
      + " (SELECT conducted FROM tests_prescribed WHERE prescribed_id=tp.new_test_prescribed_id)"
      + " end) as test_conducted, sp.conducted as service_conducted,"
      + " CASE WHEN test.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status,"
      + " COALESCE(packp.common_order_id,"
      + " (CASE WHEN tp.new_test_prescribed_id IS NULL THEN tp.common_order_id ELSE"
      + " (SELECT common_order_id FROM tests_prescribed"
      + " WHERE prescribed_id=tp.new_test_prescribed_id) end), sp.common_order_id)"
      + " as common_order_id,"
      + " COALESCE(test.mandate_additional_info, 'N') as mandate_additional_info, "
      + " atp.ispkg "
      + " FROM patient_activities a "
      + " LEFT JOIN patient_prescription pp on (pp.patient_presc_id=a.prescription_id)"
      + " LEFT JOIN patient_medicine_prescriptions pmp on (pp.presc_type='Medicine' and"
      + " pmp.op_medicine_pres_id=pp.patient_presc_id) "
      + " LEFT JOIN patient_other_medicine_prescriptions pomp on (pp.presc_type='Medicine'"
      + " and pomp.prescription_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_test_prescriptions ptp on (pp.presc_type='Inv.' and"
      + " ptp.op_test_pres_id=pp.patient_presc_id)"
      + " LEFT JOIN patient_consultation_prescriptions pcp on (pp.presc_type='Doctor'"
      + " and pcp.prescription_id=pp.patient_presc_id) "
      + " LEFT JOIN patient_service_prescriptions psp on (pp.presc_type='Service' and"
      + " pp.patient_presc_id=psp.op_service_pres_id)"
      + " LEFT JOIN patient_other_prescriptions potp on ((pp.presc_type='NonBillable' and"
      + " pp.patient_presc_id=potp.prescription_id))"
      + " LEFT JOIN services_prescribed sp ON (pp.presc_type='Service' AND"
      + " a.order_no=sp.prescription_id) "
      + " LEFT JOIN all_tests_pkgs_view atp ON (pp.presc_type = 'Inv.' AND ptp.test_id=atp.test_id)"
      + " LEFT JOIN tests_prescribed tp ON (pp.presc_type='Inv.' "
      + " AND NOT atp.ispkg  AND a.order_no=tp.prescribed_id)"
      + " LEFT JOIN diagnostics test ON (pp.presc_type='Inv.' AND test.test_id=ptp.test_id)"
      + // this is joined to get the sample needed column of test
        " LEFT JOIN doctors doc ON (pp.doctor_id=doc.doctor_id)"
      + " LEFT JOIN store_item_details sid ON (pp.presc_type = 'Medicine' AND"
      + " pmp.medicine_id=sid.medicine_id) "
      + " LEFT JOIN generic_name gn ON (pp.presc_type='Medicine' AND"
      + " pmp.generic_code=gn.generic_code)"
      + " LEFT JOIN services s ON (pp.presc_type = 'Service' AND psp.service_id=s.service_id)"
      + " LEFT JOIN doctors cdoc ON (presc_type = 'Doctor' AND pcp.doctor_id=cdoc.doctor_id) "
      + " LEFT JOIN recurrence_daily_master rdm ON (pp.recurrence_daily_id=rdm.recurrence_daily_id)"
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + " LEFT JOIN package_prescribed packp ON (pp.presc_type='Inv.' AND atp.ispkg "
      + " AND a.order_no=packp.prescription_id)"
      + " LEFT JOIN consumption_uom_master cum ON (sid.cons_uom_id = cum.cons_uom_id)"
      + " WHERE a.patient_id=? #medicine_filter# ORDER BY a.due_date";

  /**
   * Gets the activities.
   *
   * @param patientId the patient id
   * @return the activities
   * @throws SQLException the SQL exception
   */
  public static List getActivities(String patientId, Boolean includeMedicine) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = "";
    if (includeMedicine) {
      query = ACTIVITIES.replace("#medicine_filter#", "");
    } else {
      query = ACTIVITIES.replace("#medicine_filter#", "AND (a.prescription_type!='M' OR"
          + " a.prescription_type IS NULL OR  a.prescription_type='')" );
    }
    try {
      ps = con.prepareStatement(query);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ACTIVITIES_FIELDS. */
  private static final String ACTIVITIES_FIELDS = "SELECT a.activity_id, a.activity_type,"
      + " a.activity_status, a.due_date, a.activity_num, a.order_no,"
      + " a.activity_remarks as activity_remarks, ipp.remarks as remarks, a.completed_date,"
      + " prescription_type, a.completed_by, a.ordered_by, a.prescription_id, "
      + " CASE WHEN activity_type = 'G' THEN gen_activity_details WHEN presc_type = 'O' THEN"
      + " item_name WHEN presc_type = 'M' THEN sid.medicine_name WHEN presc_type = 'I' THEN"
      + " test_name WHEN presc_type = 'C' THEN cdoc.doctor_name WHEN presc_type = 'S' THEN"
      + " service_name END"
      + " item_name, item_form_name as med_form_name, rdm.display_name as recurrence_name,"
      + " ipp.med_strength, ipp.freq_type,"
      + " ipp.repeat_interval, ipp.start_datetime, ipp.end_datetime, ipp.no_of_occurrences,"
      + " ipp.end_on_discontinue, ipp.remarks as presc_remarks, ipp.med_dosage,"
      + " sid.consumption_uom, doc.doctor_name, med_batch, med_exp_date, ipp.doctor_id ";
  
  /** The Constant COUNT. */
  private static final String COUNT = "SELECT count(*) ";
  
  /** The Constant TABLES. */
  private static final String TABLES = " FROM patient_activities a "
      + " LEFT JOIN ip_prescription ipp ON (a.prescription_id=ipp.prescription_id)"
      + " LEFT JOIN doctors doc ON (ipp.doctor_id=doc.doctor_id)"
      + " LEFT JOIN store_item_details sid ON (presc_type = 'M' AND"
      + " ipp.item_id=sid.medicine_id::text)"
      + " LEFT JOIN generic_name gn ON (presc_type='M' AND ipp.generic_code=gn.generic_code)"
      + " LEFT JOIN all_tests_pkgs_view atp ON (presc_type = 'I' AND ipp.item_id=atp.test_id)"
      + " LEFT JOIN services s ON (presc_type = 'S' AND ipp.item_id=s.service_id)"
      + " LEFT JOIN doctors cdoc ON (presc_type = 'C' AND ipp.item_id=cdoc.doctor_id)"
      + " LEFT JOIN recurrence_daily_master rdm"
      + " ON (ipp.recurrence_daily_id=rdm.recurrence_daily_id)"
      + " LEFT JOIN item_form_master ifm ON (ipp.med_form_id=ifm.item_form_id)";
  
  /** The Constant WHERE. */
  private static final String WHERE = " WHERE a.due_date::date=current_date AND"
      + " a.activity_status in ('P', 'O')";

  /**
   * Gets the activities.
   *
   * @param patientId the patient id
   * @param pageNumParam the page num param
   * @param pageSizeParam the page size param
   * @return the activities
   * @throws SQLException the SQL exception
   */
  public static PagedList getActivities(String patientId, String pageNumParam,
      String pageSizeParam)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    SearchQueryBuilder qb = null;
    try {
      int pageNum = 1;
      if (pageNumParam != null && !pageNumParam.equals("")) {
        pageNum = Integer.parseInt(pageNumParam);
      }
      int pageSize = 20;
      if (pageSizeParam != null && !pageSizeParam.equals("")) {
        pageSize = Integer.parseInt(pageSizeParam);
      }
      qb = new SearchQueryBuilder(con, ACTIVITIES_FIELDS, COUNT, TABLES, WHERE, "a.due_date",
          false, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "a.patient_id", "=", patientId);
      qb.build();

      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The activity exists. */
  public static String ACTIVITY_EXISTS = "SELECT activity_id FROM patient_activities "
      + " WHERE prescription_id=? and prescription_type=?";

  /**
   * Activity exists.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean activityExists(int prescriptionId, String prescriptionType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(ACTIVITY_EXISTS);
      ps.setInt(1, prescriptionId);
      ps.setString(2, prescriptionType);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return false;
  }

  /** The update status. */
  public static String UPDATE_STATUS = "UPDATE patient_activities set activity_status='P',"
      + " completed_date=null, completed_by=null, order_no=null"
      + " WHERE order_no=? and prescription_type=?";

  /**
   * Update status.
   *
   * @param con the con
   * @param orderNo the order no
   * @param prescriptionType the prescription type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateStatus(Connection con, int orderNo, String prescriptionType)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_STATUS);
      ps.setInt(1, orderNo);
      ps.setString(2, prescriptionType);
      return ps.executeUpdate() == 1;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The get activity. */
  public static String GET_ACTIVITY = "SELECT * FROM patient_activities "
      + " WHERE order_no=? and prescription_type=?";

  /**
   * Gets the activity.
   *
   * @param con the con
   * @param orderNo the order no
   * @param prescriptionType the prescription type
   * @return the activity
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getActivity(Connection con, int orderNo, String prescriptionType)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ACTIVITY);
      ps.setInt(1, orderNo);
      ps.setString(2, prescriptionType);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the activity.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param activityNum the activity num
   * @return the activity
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getActivity(Connection con, int prescriptionId, int activityNum)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("SELECT activity_id FROM patient_activities"
              + " WHERE prescription_id=? "
              + " AND activity_status='P' AND activity_num>?");
      ps.setInt(1, prescriptionId);
      ps.setInt(2, activityNum);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
  
  /**
   * Cancel activity.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean cancelActivity(Connection con, int prescriptionId, String prescriptionType)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("UPDATE patient_activities SET activity_status='X'"
          + " WHERE activity_status='P' AND prescription_id=? AND prescription_type=?");
      ps.setInt(1, prescriptionId);
      ps.setString(2, prescriptionType);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Delete incomplete activity.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteIncompleteActivity(Connection con, int prescriptionId,
      String prescriptionType) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("DELETE FROM patient_activities"
          + " WHERE activity_status='P' AND prescription_id=? AND prescription_type=?");
      ps.setInt(1, prescriptionId);
      ps.setString(2, prescriptionType);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  /**
   * Completed activities exists.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean completedActivitiesExists(Connection con, int prescriptionId,
      String prescriptionType) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement("SELECT * FROM patient_activities"
          + " WHERE activity_status in ('C', 'O') AND prescription_id=?"
          + " AND prescription_type=?");
      ps.setInt(1, prescriptionId);
      ps.setString(2, prescriptionType);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
      return false;
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
  }

  /**
   * Gets the pending activity.
   *
   * @param con the con
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return the pending activity
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPendingActivity(Connection con, int prescriptionId,
      String prescriptionType) 
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("SELECT * FROM patient_activities "
          + " WHERE activity_status='P' AND prescription_id=? AND prescription_type=?");
      ps.setInt(1, prescriptionId);
      ps.setString(2, prescriptionType);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The get open patient indents. */
  private static final String GET_OPEN_PATIENT_INDENTS = " SELECT pa.*"
      + " FROM patient_activities pa"
      + " JOIN patient_registration pr ON (pr.patient_id = pa.patient_id)"
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no)"
      + " WHERE pa.patient_id = ? AND pa.activity_status = ? AND prescription_type !='M' "
      + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Gets the pending ward activities.
   *
   * @param patientId the patient id
   * @return the pending ward activities
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPendingWardActivities(String patientId) throws SQLException {
    /*
     * This method gets pending ward activities for a patient
     */
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_OPEN_PATIENT_INDENTS);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      ps.setString(2, "P");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
