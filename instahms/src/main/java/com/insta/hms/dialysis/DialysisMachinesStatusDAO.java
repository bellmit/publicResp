package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DialysisMachinesStatusDAO.
 */
public class DialysisMachinesStatusDAO {

  /** The dialysis machstatus fields. */
  private static String DIALYSIS_MACHSTATUS_FIELDS = "SELECT lm.location_name, sp.mr_no,"
      + "  pd.patient_name,"
      + " dms.polled_status, dmm.machine_id, dmm.machine_name, dms.assigned_status,"
      + " dmm.status,pd.last_name, ds.order_id,dmm.network_address, sp.prescription_id, "
      + " CASE WHEN polled_status = 'D' "
      + "   THEN dms.last_polled_time - '1 second'::interval*dms.dialysis_time*60 "
      + "   ELSE NULL END as start_time ";

  /** The dialysis machstatus count. */
  private static String DIALYSIS_MACHSTATUS_COUNT = "SELECT count(dmm.machine_id)";

  /** The dialysis machstatus tables. */
  private static String DIALYSIS_MACHSTATUS_TABLES = " FROM dialysis_machine_status dms"
      + " JOIN dialysis_machine_master dmm ON dms.machine_id = dmm.machine_id"
      + " JOIN location_master lm ON dmm.location_id = lm.location_id "
      + " LEFT JOIN dialysis_session ds ON dms.assigned_order_id = ds.order_id "
      + " LEFT JOIN services_prescribed sp ON sp.prescription_id = ds.order_id"
      + " LEFT JOIN patient_details pd ON (pd.mr_no = sp.mr_no)";

  /** The init where. */
  private static String INIT_WHERE = " WHERE ( patient_confidentiality_check(COALESCE"
      + " (pd.patient_group, 0),pd.mr_no) )";

  /**
   * Gets the all machines status.
   *
   * @param filters the filters
   * @param listing the listing
   * @return the all machines status
   * @throws SQLException the SQL exception
   */
  public PagedList getAllMachinesStatus(Map filters, Map listing) throws SQLException {

    Connection con = DataBaseUtil.getConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, DIALYSIS_MACHSTATUS_FIELDS,
        DIALYSIS_MACHSTATUS_COUNT, DIALYSIS_MACHSTATUS_TABLES, INIT_WHERE, null, sortField,
        sortReverse, pageSize, pageNum);

    qb.addFilter(qb.INTEGER, "lm.location_id", "=", filters.get("locationId"));
    qb.addFilter(qb.STRING, "dms.polled_status", "in", filters.get("machineStatus"));
    qb.addFilter(qb.STRING, "dms.assigned_status", "in", filters.get("allocStatus"));
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      qb.addFilter(SearchQueryBuilder.INTEGER, "lm.center_id", "=", centerId);
    }

    qb.build();

    PagedList pl = qb.getMappedPagedList();

    qb.close();
    con.close();

    return pl;
  }

  /** The status details. */
  private static String STATUS_DETAILS = "SELECT dmm.machine_name, lm.location_name, "
      + "dmm.network_address,"
      + " pd.patient_name, sp.mr_no, dms.last_polled_time, dms.last_results_time, "
      + " dms.polled_status, dms.uf_goal, dms.uf_removed, dms.uf_rate, dms.blood_pump_rate,"
      + " dms.heparin_rate, pd.last_name, dms.dialysate_temp, dms.dialysate_cond,"
      + " dms.venous_pressure,"
      + " dms.dialysate_pressure, dms.tmp, dms.dialysis_time, dms.blood_pump_rate,"
      + " dms.bp_time,"
      + " dms.bp_high, dms.bp_low, dms.pulse_rate, dms.air_alarm, dms.blood_leak_alarm,"
      + " dms.tmp_alarm,"
      + " dms.other_alarm, dms.bp_alarm " + DIALYSIS_MACHSTATUS_TABLES
      + " WHERE dmm.machine_id = ? AND patient_confidentiality_check("
      + " COALESCE(pd.patient_group, 0),pd.mr_no) ";

  /**
   * Gets the status details.
   *
   * @param machineID the machine ID
   * @return the status details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getStatusDetails(int machineID) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean statusDetails = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(STATUS_DETAILS);
      ps.setInt(1, machineID);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        statusDetails = (BasicDynaBean) list.get(0);
      }

    } finally {

      DataBaseUtil.closeConnections(con, ps);
    }

    return statusDetails;
  }

  /** The assign machine. */
  private static String ASSIGN_MACHINE = "UPDATE dialysis_machine_status "
      + " SET assigned_status='A', assigned_order_id=? " + " WHERE machine_id=?";

  /**
   * Assign machine.
   *
   * @param con the con
   * @param machineId the machine id
   * @param orderId the order id
   * @throws SQLException the SQL exception
   */
  public static void assignMachine(Connection con, int machineId, int orderId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(ASSIGN_MACHINE);
    ps.setInt(1, orderId);
    ps.setInt(2, machineId);
    ps.execute();
    ps.close();
  }

  /** The unassign machine. */
  private static String UNASSIGN_MACHINE = "UPDATE dialysis_machine_status "
      + " SET assigned_status='U', assigned_order_id=null " + " WHERE machine_id=?";

  /**
   * Unassign machine.
   *
   * @param con the con
   * @param machineId the machine id
   * @throws SQLException the SQL exception
   */
  public static void unassignMachine(Connection con, int machineId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UNASSIGN_MACHINE);
    ps.setInt(1, machineId);
    ps.execute();
    ps.close();
  }

  /** The already assigned. */
  private static String ALREADY_ASSIGNED = "select assigned_order_id,machine_id from "
      + "dialysis_machine_status where machine_id =? and assigned_status='A'";

  /**
   * Check for already assigned.
   *
   * @param machineID the machine ID
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean checkForAlreadyAssigned(int machineID) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean statusDetails = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(ALREADY_ASSIGNED);
      ps.setInt(1, machineID);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        statusDetails = (BasicDynaBean) list.get(0);
      }

    } finally {

      DataBaseUtil.closeConnections(con, ps);
    }

    return statusDetails;
  }

}
