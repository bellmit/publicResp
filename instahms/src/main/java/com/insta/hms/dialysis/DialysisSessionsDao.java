package com.insta.hms.dialysis;

import static com.insta.hms.Registration.VisitDetailsDAO.getPatientLatestVisitId;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class DialysisSessionsDao.
 */
public class DialysisSessionsDao extends GenericDAO {

  /**
   * Instantiates a new dialysis sessions dao.
   */
  public DialysisSessionsDao() {
    super("dialysis_session");
  }

  /** The current sessions query fields. */
  // O/P/I/F/C: Ordered, Prepared, In Progress, Completed, Closed
  private static String CURRENT_SESSIONS_QUERY_FIELDS = "select  pr.center_id as visit_center, "
      + "ds.prescription_id as dialysis_presc_id ,"
      + " sp.prescription_id::integer,sp.mr_no,get_patient_name(pd.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name) as patient_name,"
      + " dms.machine_id,dmm.machine_name, case when dms.polled_status='D' then 'Dialyzing' when"
      + " dms.polled_status='N' then 'Not dialyzing' when dms.polled_status='X' "
      + "then 'Cannot connect' END as machine_status,"
      + " ds.status,case when ds.status = 'O' then 'Ordered' when ds.status='P'"
      + " then 'Prepared' when "
      + " ds.status='I' then 'In Progress' "
      + " when ds.status = 'F' then 'Completed' when ds.status='C' then 'Closed' END "
      + "as status_name,ds.order_id,"
      + " ds.prescription_id,ds.start_attendant,ds.alerts,ds.start_time,ds.end_time,"
      + "sp.presc_date as ordered_date,"
      + " b.bill_no,b.bill_type,b.status as bill_status";
  
  /** The current sessions count. */
  private static String CURRENT_SESSIONS_COUNT = "SELECT count(sp.mr_no) ";
  
  /** The current sessions tables. */
  private static String CURRENT_SESSIONS_TABLES = " FROM services_prescribed sp "
      + " LEFT JOIN dialysis_session ds on ds.order_id =  sp.prescription_id "
      + " LEFT JOIN bill_activity_charge bac on (bac.activity_id=sp.prescription_id::varchar)"
      + " AND bac.activity_code = 'SER' "
      + " LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id"
      + " LEFT JOIN bill b on b.bill_no=bc.bill_no"
      + " JOIN patient_registration pr ON (pr.patient_id=b.visit_id) "
      + " JOIN patient_details pd on (pd.mr_no = pr.mr_no "
      + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))"
      + " LEFT JOIN dialysis_machine_status dms using (machine_id)"
      + " LEFT JOIN dialysis_machine_master dmm using (machine_id) "
      + " JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id) ";

  /**
   * Gets the all current sessions.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the all current sessions
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getAllCurrentSessions(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, CURRENT_SESSIONS_QUERY_FIELDS,
        CURRENT_SESSIONS_COUNT, CURRENT_SESSIONS_TABLES, null, sortField, sortReverse, pageSize,
        pageNum);

    if (filter.get("machine_id") != null && !(filter.get("machine_id").equals(""))) {
      qb.addFilter(qb.INTEGER, "ds.machine_id", "=",
          Integer.parseInt((String) filter.get("machine_id")));
    }

    qb.addFilter(qb.STRING, "ds.start_attendant", "=", filter.get("start_attendant"));
    qb.addFilter(qb.DATE, "ds.start_time::date", ">=", filter.get("start_time0"));
    qb.addFilter(qb.DATE, "ds.start_time::date", "<=", filter.get("start_time1"));
    qb.addFilter(qb.STRING, "ds.status", "IN", filter.get("status"));
    qb.addFilter(qb.STRING, "sp.specialization", "=", "D");
    qb.addFilter(qb.STRING, "sp.conducted", "!=", "X");
    qb.appendToQuery(" (ds.status!='C' or ds.status is null) ");
    qb.addFilter(qb.DATE, "sp.presc_date::date", ">=", filter.get("order_time0"));
    qb.addFilter(qb.DATE, "sp.presc_date::date", "<=", filter.get("order_time1"));
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      qb.addFilter(SearchQueryBuilder.INTEGER, "pr.center_id", "=", centerId);
    }

    qb.addSecondarySort("sp.prescription_id", true);

    qb.build();

    PagedList list = qb.getMappedPagedList();

    qb.close();
    con.close();

    return list;
  }

  /**
   * Check for active prescription.
   *
   * @param mrno the mrno
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int checkForActivePrescription(String mrno) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int prescriptionId = 0;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(
          "select dialysis_presc_id from dialysis_prescriptions where mr_no=? and status='A'");
      ps.setString(1, mrno);
      prescriptionId = DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return prescriptionId;
  }

  /** The dialysis prefill details. */
  private static String dialysis_prefill_details = "SELECT dp.dialyzer_type_id,"
      + " dp.dialysis_presc_id,dp.access_type_id,dp.access_site_id,dp.duration as est_duration,"
      + " dp.target_weight as dry_wt,dp.target_weight as target_wt,dp.heparin_bolus,"
      + " dp.heparin_hourly as heparin_rate,dp.notes,dp.dialysate_type_id, "
      + " '' as order_id, '' as prescription_id, '' as machine_id, '' as location_id, "
      + " '' as status,'' as completion_status,'' as start_time,'' as end_time,"
      + "  '' as start_attendant, '' as end_attendant, "
      + " '' as dialyzer_repr_date, "
      + " '' as dialyzer_check_user2,'' as in_patient_cond, '' as in_bp_high_sit,"
      + " '' as in_bp_low_sit, '' as in_pulse_sit, '' as in_bp_high_stand,"
      + " '' as in_bp_low_stand,'' as in_pulse_stand, '' as in_respiration,"
      + "  '' as in_temperature, '' as in_total_wt,'' as in_prosthetic_wt, "
      + " '' as in_wheelchair_wt, '' as in_real_wt, '' as fluid_in_wt,'' as target_wt_removal,"
      + " '' as heparin_start,'' as in_dilayzer_rating_id, '' as start_notes,"
      + " '' as fin_patient_cond, '' as fin_bp_high_sit,'' as fin_bp_low_sit,"
      + " '' as fin_pulse_sit, '' as fin_bp_high_stand, '' as fin_bp_low_stand,"
      + "'' as fin_pulse_stand, '' as fin_respiration, '' as fin_temperature,"
      + " '' as fin_total_wt,'' as fin_prosthetic_wt, '' as fin_wheelchair_wt,"
      + "  '' as heparin_left,'' as total_heparin, "
      + " '' as fin_dialyzer_rating_id, '' as completion_notes, '' as intra_min_bp_high,"
      + " '' as intra_min_bp_low, '' as total_wt_loss, "
      + " '' as total_fluid_removed,  '' as alerts, '' as weight_change,"
      + "  ''as access_site_infection, "
      + " '' as access_patency, '' as patency_nf, '' as patency_rf, '' as patency_bruit,"
      + "  '' as patency_thrill, "
      + " '' as sterilant_present, '' as machine_disinfected, '' as machine_rinsed, "
      + " '' as dialyzer_rinsed, '' as dialyzer_primed,"
      + " '' as sterilant_negative, '' as dialysate_comp,'' as conductivity,"
      + "  '' as machine_test,'' as pressure_test, '' as alarm_test,"
      + " '' as air_detector_armed,'' as saline_clamped, '' as pressure_limits_set,"
      + " '' as nurses_round,'' as secondary_check_done,"
      + " '' as iso_uf,'' as iso_uf_time,'' as chest_auscultation_clear,'' as peripheral_edema,"
      + " ''as chest_pain_discomfort, "
      + " '' as recent_surgery,'' as intradialysis_complaints,'' as breakfast_lunch_dinner,"
      + " '' as bloodline_sterilant,'' as salinebag_sterilant,"
      + " '' as dialysate_counter_flow , '' as perm_cath_flow,'' as cannulation,"
      + " '' as cannulation_reattempt,'' as anticoagulation,'' as frequency,'' as volume,"
      + " '' as dialysate_counter_flow , '' as perm_cath_flow, '' as patency_bruit_thrill,"
      + "  '' as other_staff, '' as in_odometer_reading,"
      + " ds1.dialyzer_lot_num,ds1.fin_real_wt,ds1.dialyzer_repr_count,ds1.post_session_notes,"
      + "  dp.dry_wt_date, ds1.single_use_dialyzer, "
      + " '' as needle_type, dp.heparin_type,'' as filtration_replacement_fluid_volume,"
      + " dp.low_heparin_intrim_dose, dp.low_heparin_initial_dose,'' as physician,"
      + " '' as cannulation_nurse "
      + " FROM dialysis_prescriptions dp LEFT JOIN dialysis_session ds1"
      + "  on ds1.prescription_id = dp.dialysis_presc_id and "
      + " ds1.order_id = (select max(order_id) from dialysis_session where "
      + " prescription_id=dp.dialysis_presc_id)"
      + " WHERE dialysis_presc_id=?";

  /**
   * Gets the dialysis session details.
   *
   * @param dialysisPrescId the dialysis presc id
   * @return the dialysis session details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getDialysisSessionDetails(int dialysisPrescId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean prescriptionDetails = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(dialysis_prefill_details);
      ps.setInt(1, dialysisPrescId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        prescriptionDetails = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return prescriptionDetails;
  }

  /** The pre dialysis details. */
  private static String pre_dialysis_details = "select ds.*,dp.notes,dp.target_weight as dry_wt, "
      + " dp.dry_wt_date" + " FROM dialysis_session ds"
      + " JOIN dialysis_prescriptions dp on dp.dialysis_presc_id = ds.prescription_id "
      + " WHERE ds.order_id = ?";

  /**
   * Gets the presc and dialysis details.
   *
   * @param orderId the order id
   * @return the presc and dialysis details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPrescAndDialysisDetails(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean preSesDetails = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(pre_dialysis_details);

      ps.setInt(1, orderId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        preSesDetails = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return preSesDetails;
  }
  
  /**
   * Gets the presc and dialysis details.
   *
   * @param mrNo the mr no
   * @return the presc and dialysis details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPrescAndDialysisDetails(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PRE_DIALYSIS_DETAILS);
      ps.setString(1, mrNo);
      list = DataBaseUtil.queryToDynaBean(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The sessions summary query fields. */
  private static String SESSIONS_SUMMARY_QUERY_FIELDS = "select lm.location_name,"
      + " ds.start_attendant || ',' || coalesce (ds.end_attendant, '') as "
      + " attendants,dat.access_type,dt.dialyzer_type_name || "
      + " '(' || ds.dialyzer_lot_num || ')' as dialyzer_lot,"
      + " ds.dialyzer_repr_count,ds.est_duration,ds.status,case when ds.status = 'O' "
      + " then 'Ordered' when ds.status='P' then 'Prepared' when ds.status='I' then 'In Progress' "
      + " when ds.status = 'F' then 'Completed' when ds.status='C' "
      + " then 'Closed' END as status_name,"
      + " case when ds.completion_status='N' then 'Normal'"
      + "  when ds.completion_status ='D' then 'Discontinued'  "
      + " when ds.completion_status='X' then 'Canceled' END as completion_status,"
      + " ds.in_real_wt,ds.fin_real_wt,"
      + " ds.target_wt,ds.total_wt_loss,ds.total_fluid_removed,ds.total_heparin,"
      + " ds.alerts,ds.in_temperature,"
      + " ds.fin_temperature,ds.in_pulse_stand,ds.fin_pulse_stand,ds.in_bp_high_sit,"
      + " ds.in_bp_high_stand,"
      + " ds.fin_bp_high_sit,ds.fin_bp_high_stand, ds.in_bp_low_sit,ds.in_bp_low_stand,"
      + " ds.fin_bp_low_sit,"
      + " ds.fin_bp_low_stand,ds.start_time,ds.end_time, dp.presc_date,ds.order_id,"
      + " ds.prescription_id,ds.min_bp_time, "
      + " dsp1.bp_high as first_bp_high,dsp1.bp_low as first_bp_low,dsp1.pulse_rate"
      + "  as first_pulse_rate,"
      + " dsp2.bp_high as last_bp_high,dsp2.bp_low as last_bp_low,dsp2.pulse_rate"
      + "  as last_pulse_rate,"
      + " coalesce(dsp2.uf_rate, dsp1.uf_rate) as last_uf_rate,dsp2.uf_removed "
      + " as last_uf_removed";
  
  /** The sessions summary count. */
  private static String SESSIONS_SUMMARY_COUNT = "SELECT count(sp.mr_no) ";
  
  /** The sessions summary tables. */
  private static String SESSIONS_SUMMARY_TABLES = " FROM dialysis_session  ds"
      + " LEFT JOIN dialysis_session_parameters dsp1 on dsp1.order_id = ds.order_id and"
      + "   dsp1.obs_type ='F'"
      + " LEFT JOIN dialysis_session_parameters dsp2 on dsp2.order_id = ds.order_id and "
      + "  dsp2.obs_type ='L'"
      + " JOIN dialysis_prescriptions dp on dp.dialysis_presc_id = ds.prescription_id"
      + " JOIN services_prescribed sp on sp.prescription_id = ds.order_id"
      + " LEFT JOIN location_master lm using (location_id)"
      + " LEFT JOIN dialysis_access_types dat on dat.access_type_id = ds.access_type_id"
      + " LEFT JOIN dialyzer_types dt  on dt.dialyzer_type_id= ds.dialyzer_type_id "
      + " JOIN patient_registration pr ON(sp.patient_id = pr.patient_id)";

  /**
   * Gets the session summary.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the session summary
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getSessionSummary(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, SESSIONS_SUMMARY_QUERY_FIELDS,
        SESSIONS_SUMMARY_COUNT, SESSIONS_SUMMARY_TABLES, null, sortField, sortReverse, pageSize,
        pageNum);

    qb.addFilter(qb.DATE, "ds.start_time::date", ">=", filter.get("start_time"));
    qb.addFilter(qb.DATE, "ds.start_time::date", "<=", filter.get("end_time"));
    qb.addFilter(qb.STRING, "ds.status", "IN", filter.get("status"));
    qb.addFilter(qb.INTEGER, "ds.alerts", (String) filter.get("operator"), filter.get("value"));
    qb.addFilter(qb.STRING, "sp.mr_no", "=", filter.get("mr_no"));

    qb.build();

    PagedList list = qb.getMappedPagedList();

    qb.close();
    con.close();

    return list;
  }

  /**
   * Gets the intra dialysis details.
   *
   * @param orderId the order id
   * @return the intra dialysis details
   * @throws SQLException the SQL exception
   */
  public List getIntraDialysisDetails(int orderId) throws SQLException {
    List intraDetails = new ArrayList();
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(
          "select * from dialysis_session_parameters where order_id=? order by observation_id");
      ps.setInt(1, orderId);
      intraDetails = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return intraDetails;
  }

  /** The get machine status. */
  private static String GET_MACHINE_STATUS = "select * from  dialysis_machine_status dms"
      + " JOIN dialysis_session ds using(machine_id)"
      + " WHERE ds.order_id = dms.assigned_order_id and ds.order_id=?";

  /**
   * Gets the machine status details.
   *
   * @param orderId the order id
   * @return the machine status details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getMachineStatusDetails(int orderId) throws SQLException {

    BasicDynaBean bean = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MACHINE_STATUS);
      ps.setInt(1, orderId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /** The get session details. */
  private static String GET_SESSION_DETAILS = "select ds.order_id,ds.start_time,"
      + " ds.start_attendant,ds.status, ds.completion_status, lm.location_name,"
      + " dmm.machine_name,case when dms.polled_status='D' then 'Dialyzing'"
      + "  when dms.polled_status='N' then 'Not dialyzing' "
      + " when dms.polled_status='X' then 'Cannot connect' END as machine_status,"
      + " ds.intra_min_bp_high,ds.intra_min_bp_low,ds.min_bp_time,ds.poll_count,ds.total_pulse,"
      + " ds.total_dialysate_pressure,ds.total_venous_pressure,ds.prescription_id,dms.polled_status"
      + " FROM dialysis_session ds" + " LEFT join location_master lm  using (location_id)"
      + " JOIN dialysis_machine_master dmm using (machine_id)"
      + " LEFT JOIN dialysis_machine_status dms on dms.machine_id = "
      + " ds.machine_id and dms.assigned_order_id = ds.order_id"
      + " where ds.order_id=?";

  /**
   * Gets the session details.
   *
   * @param orderId the order id
   * @return the session details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getSessionDetails(int orderId) throws SQLException {

    BasicDynaBean bean = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SESSION_DETAILS);
      ps.setInt(1, orderId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /**
   * Update status.
   *
   * @param con the con
   * @param status the status
   * @param originalStatus the original status
   * @param completedStatus the completed status
   * @param orderId the order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateStatus(Connection con, String status, String originalStatus,
      String completedStatus, int orderId) throws SQLException {

    PreparedStatement ps = null;
    boolean updateStatus = false;

    try {
      if (originalStatus.equals("I") && status.equals("F")) {
        ps = con.prepareStatement(
            "update dialysis_session set status=?,end_time=?,completion_status=? where order_id=?");
        ps.setString(1, status);
        ps.setTimestamp(2, DateUtil.getCurrentTimestamp());
        ps.setString(3, completedStatus);
        ps.setInt(4, orderId);
      } else {
        ps = con.prepareStatement(
            "update dialysis_session set status=?, completion_status=? where order_id=?");
        ps.setString(1, status);
        ps.setString(2, completedStatus);
        ps.setInt(3, orderId);
      }

      int result = ps.executeUpdate();
      if (result > 0) {
        updateStatus = true;
      }
    } finally {
      ps.close();
    }
    return updateStatus;
  }

  /** The get current session details. */
  private static String GET_CURRENT_SESSION_DETAILS = "SELECT ds.order_id,ds.prescription_id,"
      + " dp.mr_no,ds.status FROM dialysis_session ds"
      + " JOIN dialysis_prescriptions dp on dp.dialysis_presc_id=ds.prescription_id"
      + " WHERE ds.order_id=?";

  /**
   * Gets the current session details.
   *
   * @param orderId the order id
   * @return the current session details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getCurrentSessionDetails(int orderId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CURRENT_SESSION_DETAILS);
      ps.setInt(1, orderId);

      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /** The get consumables details. */
  private static String GET_CONSUMABLES_DETAILS = "select s.service_id ,s.service_name,"
      + " sp.conducted,pr.patient_id,ds.prescription_id"
      + " FROM services s" + " JOIN services_prescribed sp using(service_id)"
      + " JOIN service_consumables sc using (service_id) "
      + " JOIN store_item_details iid on iid.medicine_id = sc.consumable_id "
      + " JOIN dialysis_session ds on ds.order_id = sp.prescription_id"
      + " JOIN patient_registration pr on pr.mr_no =sp.mr_no and pr.status='A'"
      + " WHERE ds.order_id =?";

  /**
   * Gets the consumable details.
   *
   * @param orderId the order id
   * @return the consumable details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getConsumableDetails(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CONSUMABLES_DETAILS);
      ps.setInt(1, orderId);

      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /** The Session report query. */
  private static String Session_report_query = "select dmm.machine_name,lm.location_name,"
      + " ds.start_attendant ,ds.dialyzer_repr_count,ds.end_attendant, "
      + " to_char(ds.start_time ,'dd-MM-yyyy') as start_date ,ds.est_duration,"
      + " to_char(ds.start_time ,'HH24:mi') as start_time,"
      + " to_char(ds.end_time ,'HH24:mi') as end_time,"
      + " ds.heparin_bolus,ds.dialyzer_check_user2,dp.heparin_cutoff,ds.heparin_rate,"
      + " dp.dialysate_flow,ds.nxt_dialysis_date,ds.shift,"
      + " dp.dialysate_temp,diat.dialysate_type_name,ds.in_real_wt, ds.target_wt, "
      + " dp.target_weight,ds.in_bp_high_sit,ds.in_bp_low_sit,ds.in_temperature,"
      + " case when ds.access_site_infection='Y' then 'Yes' when ds.access_site_infection='N'"
      + "  then 'No' else '-' END as access_site_infection,"
      + " ds.in_bp_high_stand,ds.in_bp_low_stand,ds.in_pulse_sit,ds.in_pulse_stand,"
      + " case when ds.chest_auscultation_clear='Y' then 'Yes' "
      + " when ds.chest_auscultation_clear='N' then 'No' else '-' END as chest_auscultation_clear,"
      + " case when ds.peripheral_edema='Y' then 'Yes' when ds.peripheral_edema='N' "
      + " then 'No' else '-' END as peripheral_edema,"
      + " case when ds.chest_pain_discomfort='Y' then 'Yes' when ds.chest_pain_discomfort='N' "
      + " then 'No' else '-' END as chest_pain_discomfort,"
      + " case when ds.recent_surgery='Y' then 'Yes' when ds.recent_surgery='N' then 'No' "
      + " else '-' END as recent_surgery,"
      + " case when ds.intradialysis_complaints='Y' then 'Yes' "
      + " when ds.intradialysis_complaints='N' then 'No' else '-' END as intradialysis_complaints,"
      + " case when ds.breakfast_lunch_dinner='Y' then 'Yes' when"
      + "  ds.breakfast_lunch_dinner='N' then 'No' else '-' END as breakfast_lunch_dinner,"
      + " case when ds.perm_cath_flow='N' then 'Normal' when ds.perm_cath_flow='R'"
      + "  then 'Reverse' else '-' END as perm_cath_flow,"
      + " case when ds.patency_bruit_thrill='Y' then 'Yes' when ds.patency_bruit_thrill='O' "
      + " then 'No' when ds.patency_bruit_thrill='N' then 'Not Applicable' "
      + " END as patency_bruit_thrill,"
      + " case when ds.fin_patency_bruit_thrill='Y' then 'Yes' when "
      + " ds.fin_patency_bruit_thrill='O' then 'No' when ds.fin_patency_bruit_thrill='N' "
      + " then 'Not Applicable' END as fin_patency_bruit_thrill,"
      + " case when ds.prolonged_bleeding_at_sites='Y' then 'Yes' when "
      + " ds.prolonged_bleeding_at_sites='N' then 'No' END as prolonged_bleeding_at_sites,"
      + " ds.fin_real_wt,ds.fin_bp_high_sit,ds.fin_bp_low_sit,ds.fin_bp_high_stand,"
      + " ds.fin_bp_low_stand,ds.fin_temperature,ds.fin_pulse_sit,"
      + " dsp1.dialysate_cond as first_dialysate_cond,dsp1.pulse_rate as first_pulse_rate,"
      + " dsp2.pulse_rate as last_pulse_rate,"
      + " dsp2.uf_removed as last_uf_removed,ds.anticoagulation,ds.frequency,ds.volume,"
      + "  ds.completion_notes,"
      + " ds.post_session_notes, ds.in_odometer_reading, ds.fin_odometer_reading,"
      + "  ds.fin_patient_cond, dat.access_type, "
      + " pr.center_id, hcm.center_name " + " FROM dialysis_session  ds"
      + " JOIN dialysis_prescriptions dp on dp.dialysis_presc_id = ds.prescription_id"
      + " LEFT JOIN dialysis_session_parameters dsp1 on dsp1.order_id = ds.order_id and "
      + "  dsp1.obs_type ='F'"
      + " LEFT JOIN dialysis_session_parameters dsp2 on dsp2.order_id = ds.order_id and "
      + "  dsp2.obs_type ='L'"
      + " JOIN services_prescribed sp on sp.prescription_id = ds.order_id"
      + " JOIN dialysis_machine_master dmm on dmm.machine_id = ds.machine_id"
      + " JOIN dialysate_type diat on diat.dialysate_type_id = dp.dialysate_type_id"
      + " LEFT JOIN location_master lm on lm.location_id = ds.location_id"
      + " LEFT JOIN dialysis_access_types dat on dat.access_type_id = ds.access_type_id"
      + " LEFT JOIN dialyzer_types dt  on dt.dialyzer_type_id= ds.dialyzer_type_id "
      + " JOIN patient_registration pr ON (sp.patient_id=pr.patient_id) "
      + " JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id)"
      + " where ds.order_id =?";

  /**
   * Gets the session details for report.
   *
   * @param orderId the order id
   * @return the session details for report
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getSessionDetailsForReport(int orderId) throws SQLException {

    BasicDynaBean bean = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(Session_report_query);
      ps.setInt(1, orderId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /** The prev session details. */
  private static String prev_session_details = "";

  /**
   * Gets the prev session details.
   *
   * @param orderId the order id
   * @return the prev session details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPrevSessionDetails(int orderId) throws SQLException {

    BasicDynaBean bean = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(prev_session_details);
      ps.setInt(1, orderId);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /**
   * Check finalized.
   *
   * @param orderId the order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean checkFinalized(int orderId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean isFinalized = true;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement("select * from dialysis_session_parameters  where order_id =? and"
          + " (finalized='' or finalized is null or finalized='N')");
      ps.setInt(1, orderId);
      rs = ps.executeQuery();
      if (rs.next()) {
        isFinalized = false;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return isFinalized;
  }

  /** The prev session notes. */
  private static String prev_session_notes = "select dp.mr_no,max(start_time),post_session_notes,"
      + " fin_real_wt  from dialysis_session ds"
      + " join dialysis_prescriptions dp on dp.dialysis_presc_id = ds.prescription_id"
      + " where order_id !=? and dp.mr_no=?"
      + " group by mr_no,post_session_notes, order_id, fin_real_wt"
      + " ORDER BY order_id DESC LIMIT 1";

  /**
   * Gets the prev session notes.
   *
   * @param orderId the order id
   * @param mrNo the mr no
   * @return the prev session notes
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPrevSessionNotes(int orderId, String mrNo) throws SQLException {

    BasicDynaBean bean = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(prev_session_notes);
      ps.setInt(1, orderId);
      ps.setString(2, mrNo);
      List list = DataBaseUtil.queryToDynaList(ps);

      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  /** The shift machine. */
  private static String SHIFT_MACHINE = "UPDATE dialysis_session " + " SET machine_id=? "
      + " WHERE order_id=?";

  /**
   * Update machine.
   *
   * @param con the con
   * @param machineId the machine id
   * @param orderId the order id
   * @throws SQLException the SQL exception
   */
  public void updateMachine(Connection con, int machineId, int orderId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(SHIFT_MACHINE);
    ps.setInt(1, machineId);
    ps.setInt(2, orderId);
    ps.execute();
    ps.close();

  }

  /**
   * On cancel order.
   *
   * @param con the con
   * @param orderId the order id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean onCancelOrder(Connection con, int orderId) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    boolean result = true;
    DialysisSessionsDao dao = new DialysisSessionsDao();
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(
          "select specialization from services_prescribed  where prescription_id =? ");
      ps.setInt(1, orderId);
      rs = ps.executeQuery();
      if (rs.next()) {
        BasicDynaBean bean = dao.findByKey("order_id", orderId);
        deleteSessionParamDetails(con, orderId);
        deleteSessionDetails(con, orderId);
        if (bean != null && bean.get("machine_id") != null
            && !(bean.get("machine_id").toString().equals(""))) {
          DialysisMachinesStatusDAO.unassignMachine(con, (Integer) bean.get("machine_id"));
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return result;

  }

  /**
   * Delete session param details.
   *
   * @param con the con
   * @param orderId the order id
   * @throws SQLException the SQL exception
   */
  public static void deleteSessionParamDetails(Connection con, int orderId) throws SQLException {

    PreparedStatement ps = con
        .prepareStatement("delete from dialysis_session_parameters where order_id=?");
    ps.setInt(1, orderId);
    ps.execute();
    ps.close();

  }

  /**
   * Delete session details.
   *
   * @param con the con
   * @param orderId the order id
   * @throws SQLException the SQL exception
   */
  public static void deleteSessionDetails(Connection con, int orderId) throws SQLException {

    PreparedStatement ps = con.prepareStatement("delete from dialysis_session where order_id=?");
    ps.setInt(1, orderId);
    ps.execute();
    ps.close();

  }

  /** The Constant Fields_for_emrdoc. */
  private static final String Fields_for_emrdoc = "select ds.order_id, sp.prescription_id, "
      + " dp.presc_doctor,"
      + "  dp.presc_date, ds.start_time::date as start_date, sp.patient_id, sp.service_id,"
      + "  sp.doctor_id, sp.docid, sp.user_name, drs.doctor_name,pr.reg_date"
      + SESSIONS_SUMMARY_TABLES
      + "  LEFT JOIN doctors drs on sp.doctor_id = drs.doctor_id WHERE pr.mr_no = ? ";

  /**
   * Fields for emrdoc.
   *
   * @param mrNo the mr no
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> fieldsForEmrdoc(String mrNo) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    List list = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(Fields_for_emrdoc);
      pstmt.setString(1, mrNo);
      list = DataBaseUtil.queryToDynaList(pstmt);
    } finally {

      DataBaseUtil.closeConnections(con, pstmt);
    }
    return list;
  }

  /** The Constant Clinical_Staff_For_Dialysis. */
  private static final String Clinical_Staff_For_Dialysis = "SELECT emp_username,"
      + "  emp_password, role_id"
      + " FROM screen_rights" + " JOIN u_role USING(role_id)"
      + " JOIN u_user USING(role_id) WHERE emp_status='A' AND screen_id= ? and center_id=?";

  /**
   * Clinical staff list.
   *
   * @param screenId the screen id
   * @param centerId the center id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List clinicalStaffList(String screenId, int centerId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(Clinical_Staff_For_Dialysis);
      pstmt.setString(1, screenId);
      pstmt.setInt(2, centerId);
      List list = DataBaseUtil.queryToDynaList(pstmt);
      if (list.size() > 0) {
        return list;
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
    return null;
  }

  /** The Constant Flow_Sheet_Fields. */
  private static final String Flow_Sheet_Fields = "SELECT sp.mr_no, pd.patient_name, "
      + " pd.patient_gender,"
      + " ds.start_time, ds.end_time, ds.in_bp_high_sit, ds.in_bp_low_sit, ds.in_bp_high_stand,"
      + " ds.in_bp_low_stand, ds.in_real_wt, ds.fin_real_wt, ds.start_notes, "
      + " ds.dialyzer_repr_count,"
      + " ds.fin_bp_low_sit, ds.fin_bp_high_sit, ds.fin_bp_high_stand, ds.fin_bp_low_stand,"
      + "  ds.order_id, center_name"
      + " FROM dialysis_session ds"
      + " JOIN services_prescribed sp on(sp.prescription_id = ds.order_id)"
      + " JOIN patient_registration pr ON (sp.patient_id=pr.patient_id) "
      + " JOIN hospital_center_master hcm ON (pr.center_id=hcm.center_id)"
      + " JOIN patient_details pd on (pr.mr_no=pd.mr_no)"
      + " WHERE sp.mr_no=? AND ds.start_time::date>=? AND ds.end_time::date<=? ORDER BY start_time";

  /**
   * Gets the dialysis flow sheet.
   *
   * @param filterMap the filter map
   * @return the dialysis flow sheet
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<BasicDynaBean> getDialysisFlowSheet(Map filterMap)
      throws SQLException, ParseException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(Flow_Sheet_Fields);
      ps.setString(1, (String) filterMap.get("mr_no"));
      ps.setDate(2, DateUtil.parseDate(filterMap.get("fromDate").toString()));
      ps.setDate(3, DateUtil.parseDate(filterMap.get("toDate").toString()));
      List<BasicDynaBean> beans = DataBaseUtil.queryToDynaList(ps);
      if (beans.size() > 0) {
        return beans;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /** The Constant PREV_FIN_REAL_WT. */
  private static final String PREV_FIN_REAL_WT = "SELECT fin_real_wt" + " FROM dialysis_session ds"
      + " JOIN services_prescribed sp on(sp.prescription_id = ds.order_id)"
      + " WHERE sp.mr_no=? and ds.start_time::date<? order by start_time desc LIMIT 1";

  /**
   * Gets the prev session final real wt.
   *
   * @param mrNo the mr no
   * @param dateStr the date str
   * @return the prev session final real wt
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static BasicDynaBean getPrevSessionFinalRealWt(String mrNo, String dateStr)
      throws SQLException, ParseException {

    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(PREV_FIN_REAL_WT);
      pstmt.setString(1, mrNo);
      pstmt.setDate(2, DateUtil.parseDate(dateStr));
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(pstmt);
      if (bean != null) {
        return bean;
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
    return null;
  }

  /** The Constant DRUG_DETAILS. */
  private static final String DRUG_DETAILS = "SELECT dpd.*, d.doctor_name FROM "
      + "drugs_administered dpd " + "LEFT JOIN doctors d ON(d.doctor_id=dpd.doctor_id) "
      + "WHERE order_id = ? order by drug_administered_id";

  /**
   * Gets the drug list.
   *
   * @param orderId the order id
   * @return the drug list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getDrugList(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(DRUG_DETAILS);
      pstmt.setInt(1, orderId);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant ACCESS_TYPES1. */
  private static final String ACCESS_TYPES1 = "select dat.access_type_id, dat.access_type,"
      + "  das.access_site_id, das.access_site  FROM permanent_access_types pa"
      + " JOIN  dialysis_access_types dat ON(pa.access_type_id_p = dat.access_type_id) "
      + " JOIN dialysis_access_sites das ON(das.access_site_id = pa.access_site_p)"
      + " WHERE mr_no = ?";

  /** The Constant ACCESS_TYPES2. */
  private static final String ACCESS_TYPES2 = "select dat.access_type_id, dat.access_type,"
      + " das.access_site_id,  das.access_site  FROM temporary_access_types ta"
      + " JOIN  dialysis_access_types dat ON(ta.access_type_id_t = dat.access_type_id) "
      + " JOIN dialysis_access_sites das ON(das.access_site_id = ta.access_site_t)"
      + " WHERE mr_no = ?";

  /**
   * Gets the access types.
   *
   * @param mrNo the mr no
   * @return the access types
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAccessTypes(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    List<BasicDynaBean> list = new ArrayList<>();
    List<BasicDynaBean> accessTypesList = new ArrayList<>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(ACCESS_TYPES1);
      pstmt.setString(1, mrNo);
      list = DataBaseUtil.queryToDynaList(pstmt);
      for (BasicDynaBean bean : list) {
        accessTypesList.add(bean);
      }
      pstmt = con.prepareStatement(ACCESS_TYPES2);
      pstmt.setString(1, mrNo);
      list = DataBaseUtil.queryToDynaList(pstmt);
      for (BasicDynaBean bean : list) {
        accessTypesList.add(bean);
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
    return accessTypesList;
  }

  /**
   * Builder.
   *
   * @param list the list
   * @param name the name
   * @return the string builder
   */
  public static StringBuilder builder(List<BasicDynaBean> list, String name) {
    StringBuilder drugString = new StringBuilder();
    int inc = 1;
    if (!list.isEmpty()) {
      Iterator<BasicDynaBean> it = list.iterator();
      while (it.hasNext()) {
        BasicDynaBean bean = it.next();
        if (inc == 2) {
          drugString.append(",");
        }
        drugString.append(bean.get(name));
        if (inc == 1) {
          inc++;
        } else {
          inc = 1;
          drugString.append("<br/>");
        }
      }
    }
    return drugString;
  }

  /** The Constant POST_DRUG_DETAILS. */
  private static final String POST_DRUG_DETAILS = " SELECT drug_administered_id, mr_no,"
      + "  prescribed_date, medicine_id, "
      + " medicine_name, quantity, expiry_date, doctor_id, staff, "
      + " dosage, remarks, d.doctor_name, batch_no, " + " mr.route_id, mr.route_name "
      + " FROM drugs_administered dc "
      + " LEFT JOIN medicine_route mr ON (dc.route_of_admin=mr.route_id) "
      + "   LEFT JOIN doctors d USING(doctor_id)"
      + " WHERE order_id=? order by drug_administered_id";

  /**
   * Gets the post drug list.
   *
   * @param orderId the order id
   * @return the post drug list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPostDrugList(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(POST_DRUG_DETAILS);
      pstmt.setInt(1, orderId);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant ISSUED_TO_PATIENT. */
  private static final String ISSUED_TO_PATIENT = "SELECT distinct sd.medicine_name, "
      + "sd.medicine_id,  mr.route_name, "
      + " route_of_admin, sid.qty, sid.batch_no, sibd.exp_dt" + " FROM stock_issue_main tim"
      + "  JOIN stock_issue_details sid ON(tim.user_issue_no=sid.user_issue_no)"
      + "  JOIN store_item_details sd ON(sd.medicine_id=sid.medicine_id)"
      + "   JOIN store_item_batch_details sibd ON(sibd.item_batch_id = sid.item_batch_id)"
      + " LEFT JOIN medicine_route mr ON(mr.route_id::text = sd.route_of_admin)"
      + " WHERE issued_to = ?";

  /**
   * Gets the issued to patien list.
   *
   * @param mrNo the mr no
   * @return the issued to patien list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getIssuedToPatienList(String mrNo) throws SQLException {
    String visitID = getPatientLatestVisitId(mrNo, true, null);
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(ISSUED_TO_PATIENT);
      pstmt.setString(1, visitID);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant DRUG_DETAILS_FOR_FLOWSHEET. */
  private static final String DRUG_DETAILS_FOR_FLOWSHEET = "SELECT dpd.*, d.doctor_name FROM "
      + "drugs_administered dpd " + "LEFT JOIN doctors d ON(d.doctor_id=dpd.doctor_id) "
      + "WHERE mr_no = ? AND dpd.prescribed_date::date >=? AND dpd.prescribed_date::date <=? "
      + " order by drug_administered_id";

  /**
   * Gets the drug list for flowsheet.
   *
   * @param mrNo the mr no
   * @param fromDate the from date
   * @param toDate the to date
   * @return the drug list for flowsheet
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<BasicDynaBean> getDrugListForFlowsheet(String mrNo, String fromDate,
      String toDate) throws SQLException, ParseException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(DRUG_DETAILS_FOR_FLOWSHEET);
      pstmt.setString(1, mrNo);
      pstmt.setDate(2, DateUtil.parseDate(fromDate));
      pstmt.setDate(3, DateUtil.parseDate(toDate));
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant VISIT_CENTER. */
  private static final String VISIT_CENTER = "SELECT center_id FROM patient_registration pr"
      + "  JOIN services_prescribed sp ON pr.patient_id=sp.patient_id"
      + " WHERE sp.prescription_id=?";

  /**
   * Gets the visit center.
   *
   * @param orderId the order id
   * @return the visit center
   * @throws SQLException the SQL exception
   */
  public static int getVisitCenter(int orderId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(VISIT_CENTER);
      ps.setInt(1, orderId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The pre dialysis details. */
  private static String PRE_DIALYSIS_DETAILS = "select  count(sp.prescription_id) as "
      + " count_record,ds.order_id,dp.dialysis_presc_id,dp.mr_no,pr.center_id"
      + " FROM services_prescribed sp "
      + " LEFT JOIN dialysis_session ds on ds.order_id =  sp.prescription_id "
      + " LEFT JOIN dialysis_prescriptions dp on(sp.mr_no=dp.mr_no)"
      + " LEFT JOIN bill_activity_charge bac on (bac.activity_id=sp.prescription_id::varchar)"
      + "  AND bac.activity_code = 'SER' "
      + " LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id "
      + " LEFT JOIN bill b on b.bill_no=bc.bill_no "
      + " JOIN patient_details pd on pd.mr_no = sp.mr_no"
      + " LEFT JOIN dialysis_machine_status dms using (machine_id) "
      + " LEFT JOIN dialysis_machine_master dmm using (machine_id) "
      + " JOIN patient_registration pr ON (pr.patient_id=b.visit_id)"
      + " JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id) "
      + " WHERE (sp.specialization = 'D') AND (sp.conducted != 'X') "
      + " AND  (ds.status!='C' or ds.status is null)  and sp.mr_no = ?"
      + " group by ds.order_id,dp.dialysis_presc_id,dp.mr_no,pr.center_id";

  /**
   * Gets the presc and dialysis details list.
   *
   * @param mrNo the mr no
   * @return the presc and dialysis details list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPrescAndDialysisDetailsList(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PRE_DIALYSIS_DETAILS);
      ps.setString(1, mrNo);
      list = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }


  /** The Constant PRE_AND_POST_DIALYSIS_PREP_DETAILS. */
  private static final String PRE_AND_POST_DIALYSIS_PREP_DETAILS = "SELECT * FROM "
      + " dialysis_prep_master dpm "
      + " JOIN dialysis_prep_values dpv ON(dpv.prep_param_id = dpm.prep_param_id)"
      + " WHERE prep_state = ? AND order_id = ? AND status='A'";

  /** The Constant PRE_AND_POST_DIALYSIS_PREP_DETAILS_WITHOUT_ORDER_ID. */
  private static final String PRE_AND_POST_DIALYSIS_PREP_DETAILS_WITHOUT_ORDER_ID = "SELECT * "
      + " FROM dialysis_prep_master dpm "
      + " WHERE prep_state = ? AND status='A' ";

  /**
   * Gets the pre dialysi prep details.
   *
   * @param orderId the order id
   * @return the pre dialysi prep details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPreDialysiPrepDetails(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PRE_AND_POST_DIALYSIS_PREP_DETAILS);
      ps.setString(1, "pre");
      ps.setInt(2, orderId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pre dialysi prep details.
   *
   * @return the pre dialysi prep details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPreDialysiPrepDetails() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PRE_AND_POST_DIALYSIS_PREP_DETAILS_WITHOUT_ORDER_ID);
      ps.setString(1, "pre");
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the post dialysi prep details.
   *
   * @param orderId the order id
   * @return the post dialysi prep details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPostDialysiPrepDetails(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PRE_AND_POST_DIALYSIS_PREP_DETAILS);
      ps.setString(1, "post");
      ps.setInt(2, orderId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the post dialysi prep details.
   *
   * @return the post dialysi prep details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPostDialysiPrepDetails() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PRE_AND_POST_DIALYSIS_PREP_DETAILS_WITHOUT_ORDER_ID);
      ps.setString(1, "post");
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UPADTE_DIALYSIS_PREP_VALUES. */
  private static final String UPADTE_DIALYSIS_PREP_VALUES = "UPDATE dialysis_prep_values SET "
      + " order_id=?,prep_param_id=?,prep_param_value=?"
      + " WHERE prep_param_id = ? AND order_id = ?";

  /**
   * Update dilaysis prep details.
   *
   * @param con the con
   * @param paramValue the param value
   * @param prepParamId the prep param id
   * @param orderId the order id
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int updateDilaysisPrepDetails(Connection con, String paramValue, int prepParamId,
      int orderId) throws SQLException {
    PreparedStatement ps = null;
    int rows = 0;
    try {
      ps = con.prepareStatement(UPADTE_DIALYSIS_PREP_VALUES);
      ps.setInt(1, orderId);
      ps.setInt(2, prepParamId);
      ps.setString(3, paramValue);
      ps.setInt(4, prepParamId);
      ps.setInt(5, orderId);
      rows = ps.executeUpdate();
      return rows;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant IS_POST_PPREP_RECODS_EXIST. */
  private static final String IS_POST_PPREP_RECODS_EXIST = "SELECT * FROM dialysis_prep_master dpm "
      + " JOIN dialysis_prep_values dpv ON(dpv.prep_param_id = dpm.prep_param_id)"
      + " WHERE prep_state = ? AND order_id = ? ";

  /**
   * Checks if is post records exist.
   *
   * @param orderId the order id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean isPostRecordsExist(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IS_POST_PPREP_RECODS_EXIST);
      ps.setString(1, "post");
      ps.setInt(2, orderId);
      return DataBaseUtil.queryToDynaBean(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant VACCINATIONS_TO_PATIENT. */
  private static final String VACCINATIONS_TO_PATIENT = "SELECT cvm.vaccination_type,"
      + " cvd.vaccination_type_id, cvd.next_due_date FROM"
      + " clinical_vaccinations_master cvm"
      + " JOIN clinical_vaccinations_details cvd ON(cvm.vaccination_type_id = "
      + "  cvd.vaccination_type_id)"
      + " JOIN clinical_vaccination cv ON(cvd.vaccination_id = cv.vaccination_id)"
      + " WHERE cv.mr_no = ? AND cvm.status = 'A' AND next_due_date >= localtimestamp(0)::date "
      + " AND next_due_date <= ?"
      + " ORDER BY next_due_date";

  /**
   * Gets the vaccination list.
   *
   * @param mrNo the mr no
   * @return the vaccination list
   * @throws SQLException the SQL exception
   */
  public static ArrayList getVaccinationList(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    java.util.Date date = (java.util.Date) DateUtil.getExpectedDate(6, "D", false, true);
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(VACCINATIONS_TO_PATIENT);
      pstmt.setString(1, mrNo);
      pstmt.setDate(2, sqlDate);
      return DataBaseUtil.queryToArrayList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant LAB_RESULTS_FOR_PATIENT. */
  private static final String LAB_RESULTS_FOR_PATIENT = "SELECT clr.* FROM"
      + " clinical_lab_recorded clr"
      + " WHERE clr.mrno = ?  AND next_due_date >= localtimestamp(0)::date AND next_due_date <= ?"
      + " ORDER BY next_due_date desc limit 1";

  /**
   * Gets the lab results list.
   *
   * @param mrNo the mr no
   * @return the lab results list
   * @throws SQLException the SQL exception
   */
  public static ArrayList getLabResultsList(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    java.util.Date date = (java.util.Date) DateUtil.getExpectedDate(6, "D", false, true);
    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(LAB_RESULTS_FOR_PATIENT);
      pstmt.setString(1, mrNo);
      pstmt.setDate(2, sqlDate);
      return DataBaseUtil.queryToArrayList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant GET_PREP_VALUES_BY_ORDER. */
  private static final String GET_PREP_VALUES_BY_ORDER = "SELECT *,"
      + " CASE WHEN dpv.prep_param_value = 'Y' THEN 'YES' "
      + " WHEN dpv.prep_param_value='N' then 'NO' ELSE '-' END AS prep_param_name "
      + " FROM dialysis_prep_master dpm "
      + " LEFT JOIN dialysis_prep_values dpv  ON (dpv.prep_param_id = dpm.prep_param_id) "
      + " WHERE order_id = ? and status ='A'" + " UNION "
      + " SELECT *,CASE WHEN dpv.prep_param_value = 'Y' THEN 'YES' "
      + " WHEN dpv.prep_param_value='N' then 'NO' ELSE '-' END AS prep_param_name "
      + " FROM dialysis_prep_master dpm "
      + " LEFT JOIN dialysis_prep_values dpv  ON (dpv.prep_param_id = dpm.prep_param_id) "
      + " WHERE order_id is null and status ='A' order by prep_param";

  /**
   * Gets the prep values.
   *
   * @param orderId the order id
   * @return the prep values
   * @throws SQLException the SQL exception
   */
  public static List getPrepValues(int orderId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PREP_VALUES_BY_ORDER);
      pstmt.setInt(1, orderId);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant GET_PREP_MASTER_VALUES. */
  private static final String GET_PREP_MASTER_VALUES = "SELECT * FROM dialysis_prep_master"
      + "  WHERE status = 'A' order by prep_param_id";

  /**
   * Gets the prep master values.
   *
   * @return the prep master values
   * @throws SQLException the SQL exception
   */
  public static List getPrepMasterValues() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PREP_MASTER_VALUES);
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

}
