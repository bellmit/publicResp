package com.insta.hms.cssd;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class SurgeryKitAvailabilityDao {

  private static final String scheduled_operation_fields = "SELECT * ";

  private static final String scheduled_operation_count = " SELECT count(*)  ";

  private static final String scheduled_operation_tables = " FROM (SELECT mr_no, visit_id, "
      + "patient_name, operation_name, kit_id, op_id,"
      + "  appointment_time::date as appointment_date, appointment_time, "
      + "  kit_name, theatre_name, theatre_id, tm.center_id, tm.store_id, sa.appointment_id,"
      + "  coalesce(stm.transfer_no,0) as transfer_no, "
      + "  (CASE WHEN stm.transfer_no IS NULL THEN 'N' ELSE 'Y'END) as issue_status  "
      + " FROM scheduler_appointments sa"
      + "  JOIN scheduler_appointment_items USING(appointment_id) "
      + "  JOIN theatre_master tm ON (theatre_id = resource_id) "
      + "  JOIN operation_master ON (op_id = res_sch_name) "
      + "  LEFT JOIN store_transfer_main stm ON(stm.appointment_id = sa.appointment_id) "
      + "  JOIN store_kit_main USING(kit_id) WHERE appointment_status != 'Cancel') as kits";

  /**
   * Gets the scheduled operations.
   *
   * @param filter the filter
   * @param listing the listing
   * @param centerId the center id
   * @return the scheduled operations
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public PagedList getScheduledOperations(Map filter, Map listing, int centerId)
      throws ParseException, SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, scheduled_operation_fields,
          scheduled_operation_count, scheduled_operation_tables, listing);

      qb.addFilterFromParamMap(filter);
      if (centerId != 0) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }

      qb.addSecondarySort("appointment_time");
      qb.build();

      PagedList list = qb.getMappedPagedList();
      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String KIT_DETAILS = " SELECT kit_id,kit_item_id,qty,medicine_name "
      + " FROM store_kit_details kd JOIN store_kit_main km USING(kit_id)"
      + "       JOIN store_item_details sid ON(medicine_id = kit_item_id) ";

  private static final String available_kits = KIT_DETAILS + "WHERE kit_id NOT IN ("
      + "  SELECT kit_id from store_kit_details iskd JOIN ("
      + "    SELECT sid.medicine_id, sum(coalesce(qty,0)) as qty "
      + "     FROM store_item_details sid  "
      + "       LEFT JOIN  store_stock_details ssd USING(medicine_id)  "
      + "     GROUP BY sid.medicine_id) as stock "
      + "     ON (iskd.kit_item_id=stock.medicine_id) "
      + "   WHERE iskd.qty>stock.qty) ORDER BY kit_id";

  /**
   * List available kits.
   *
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> listAvailableKits() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(available_kits);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String avblItems = " SELECT sum(ssd.qty) as qty,medicine_id "
      + "from store_stock_details ssd"
      + "   JOIN stores s USING(dept_id)  "
      + "WHERE s.is_sterile_store = 'Y' AND  ssd.medicine_id IN ("
      + "     SELECT kit_item_id FROM store_kit_details"
      + "       JOIN store_kit_main USING(kit_id) WHERE status = 'A'"
      + "    ) AND ssd.qty > 0 GROUP BY medicine_id ";

  /**
   * List available kit items.
   *
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List listAvailableKitItems() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(avblItems);

      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Gets the kit detals.
   *
   * @param kitId the kit id
   * @return the kit detals
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getKitDetals(int kitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(KIT_DETAILS + " WHERE kit_id = ? AND km.status = 'A'");
      ps.setInt(1, kitId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_ACTIVE_KIT_ITEMS_STOCK = " WITH all_kit_items AS ( "
      + "  SELECT DISTINCT kit_item_id " + "  FROM store_kit_details skd "
      + "   JOIN store_kit_main skm ON (skm.kit_id = skd.kit_id) " + "  WHERE skm.status = 'A' "
      + " ) " + " SELECT ssd.dept_id, ssd.medicine_id, sum(ssd.qty) as qty "
      + " FROM all_kit_items skd "
      + "  JOIN store_stock_details ssd ON (ssd.medicine_id = skd.kit_item_id) "
      + "  JOIN store_item_batch_details ibd ON (ibd.item_batch_id = ssd.item_batch_id) "
      + "  JOIN stores s ON (s.dept_id = ssd.dept_id) "
      + " WHERE (ibd.exp_dt IS NULL OR ibd.exp_dt >= current_date::date) "
      + "  AND s.is_sterile_store = 'Y' AND s.center_id=? "
      + " GROUP BY ssd.dept_id, ssd.medicine_id ";

  public List<BasicDynaBean> getActiveKitItemsStock(int storeId)
      throws SQLException, java.io.IOException {
    return DataBaseUtil.queryToDynaList(GET_ACTIVE_KIT_ITEMS_STOCK, storeId);
  }

}
