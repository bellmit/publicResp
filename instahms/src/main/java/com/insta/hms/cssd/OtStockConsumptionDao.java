package com.insta.hms.cssd;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class OtStockConsumptionDao {

  private static final String KIT_ISSUED_OPERATION_FIELDS = "SELECT * ";

  private static final String KIT_ISSUED_OPERATION_COUNT = " SELECT count(*)  ";

  private static final String ISSUED_OPERATIONS_FIELDS = " SELECT mr_no,patient_name,"
      + "operation_name,km.kit_id,op_id,st.transfer_no, "
      + "appointment_time::date as appointment_date,appointment_time, "
      + "kit_name,theatre_name,theatre_id,tm.center_id,tm.store_id,sa.appointment_id ";
  private static final String ISSUED_OPERATIONS_FROM = " FROM scheduler_appointments sa"
      + "           JOIN scheduler_appointment_items USING(appointment_id) "
      + "           JOIN store_transfer_main st ON(st.appointment_id = sa.appointment_id)"
      + "     JOIN theatre_master tm ON(theatre_id = resource_id) "
      + "     JOIN stores s ON(s.dept_id = tm.store_id ) "
      + "     JOIN operation_master opm ON(op_id = res_sch_name) "
      + "     JOIN store_kit_main km USING(kit_id)";

  private static final String KIT_ISSUED_OPERATION_TABLES = " FROM (" + ISSUED_OPERATIONS_FIELDS
      + ISSUED_OPERATIONS_FROM + " WHERE st.return_status = 'N') as issuedop";

  /**
   * Gets the kit issued operations.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the kit issued operations
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getKitIssuedOperations(Map filter, Map listing)
      throws ParseException, SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, KIT_ISSUED_OPERATION_FIELDS,
          KIT_ISSUED_OPERATION_COUNT, KIT_ISSUED_OPERATION_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("appointment_time");
      qb.build();

      PagedList list = qb.getMappedPagedList();
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the kit issued op details.
   *
   * @param appointmentId the appointment id
   * @return the kit issued op details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getKitIssuedOpDetails(int appointmentId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(ISSUED_OPERATIONS_FIELDS + ",s.dept_name " + ISSUED_OPERATIONS_FROM
          + " WHERE sa.appointment_id = ?");
      ps.setInt(1, appointmentId);

      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String kit_item_issue_details = ISSUED_OPERATIONS_FIELDS
      + " ,sid.medicine_id,sid.medicine_name,td.qty,item_batch_id,"
      + " trn_pkg_size,item_unit,s.dept_name,cm.issue_type " + ISSUED_OPERATIONS_FROM
      + " JOIN store_transfer_details td ON(td.transfer_no = st.transfer_no) "
      + " JOIN store_item_details sid ON(td.medicine_id = sid.medicine_id) "
      + " JOIN store_category_master cm ON(cm.category_id = sid.med_category_id) " + "WHERE ";

  /**
   * Gets the kit item issue details.
   *
   * @param appointmentId the appointment id
   * @param kitId the kit id
   * @return the kit item issue details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getKitItemIssueDetails(int appointmentId, int kitId)
      throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(kit_item_issue_details + " sa.appointment_id = ? ");
      ps.setInt(1, appointmentId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
