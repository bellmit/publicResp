package com.insta.hms.dentalconsultation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DentalSuppliesItemDao extends GenericDAO {

  public DentalSuppliesItemDao() {
    super("dental_supplies_items");
  }

  /**
   * Delete items.
   *
   * @param con the con
   * @param mrNo the mr no
   * @param treatmentId the treatment id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteItems(Connection con, String mrNo, Integer treatmentId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      StringBuilder query = new StringBuilder(
          " DELETE FROM dental_supplies_items dsi USING dental_supplies_order dso "
              + " WHERE dsi.supplies_order_id=dso.supplies_order_id AND  dso.mr_no=? ");
      if (treatmentId != -1) {
        query.append(" AND treatment_id = ? ");
      }
      ps = con.prepareStatement(query.toString());
      ps.setString(1, mrNo);
      if (treatmentId != -1) {
        ps.setInt(2, treatmentId);
      }
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  /**
   * Items exists.
   *
   * @param con the con
   * @param mrNo the mr no
   * @param treatmentId the treatment id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean itemsExists(Connection con, String mrNo, Integer treatmentId)
      throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      StringBuilder query = new StringBuilder(
          " SELECT * FROM dental_supplies_items dsi JOIN dental_supplies_order dso "
              + " ON (dsi.supplies_order_id=dso.supplies_order_id) WHERE  dso.mr_no=? ");
      if (treatmentId != -1) {
        query.append(" AND treatment_id = ? ");
      }
      ps = con.prepareStatement(query.toString());
      ps.setString(1, mrNo);
      if (treatmentId != -1) {
        ps.setInt(2, treatmentId);
      }
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
    return false;
  }

  public static final String GET_ITEM_DETAILS = " SELECT dso.supplies_order_id, "
      + "dso.mr_no, received_date, received_by, item_qty, "
      + "doc.doctor_name as received_by_name, dsi.item_id, dsm.item_name,"
      + "dsi.unit_rate, shades.shade_id, "
      + "shades.shade_name, item_remarks, dsi.vat_perc, dsi.received_qty "
      + "FROM dental_supplies_items dsi "
      + "JOIN dental_supplies_order dso USING (supplies_order_id) "
      + "JOIN dental_supplies_master dsm ON (dsm.item_id=dsi.item_id) "
      + "LEFT JOIN doctors doc ON (doc.doctor_id=dsi.received_by) "
      + "LEFT JOIN dental_shades_master shades ON (dsi.shade_id=shades.shade_id) "
      + "WHERE mr_no=?";

  /**
   * Gets the item details.
   *
   * @param mrNo the mr no
   * @return the item details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getItemDetails(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ITEM_DETAILS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the dental supplies.
   *
   * @param serviceId the service id
   * @param treatmentId the treatment id
   * @return the dental supplies
   * @throws SQLException the SQL exception
   */
  public static List getDentalSupplies(String serviceId, String treatmentId) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      List list = new ArrayList();
      if (treatmentId != null && !treatmentId.equals("")) {
        ps = con.prepareStatement(
            " SELECT dso.*, dsi.item_id, dsi.item_qty, dsi.shade_id, dsm.item_name "
                + " FROM dental_supplies_order dso "
                + " JOIN patient_details pd ON (pd.mr_no = dso.mr_no AND "
                + " patient_confidentiality_check(pd.patient_group,pd.mr_no) ) "
                + " JOIN dental_supplies_items dsi USING (supplies_order_id) "
                + " JOIN dental_supplies_master dsm USING (item_id) "
                + " WHERE dso.treatment_id = ? ");
        ps.setInt(1, Integer.parseInt(treatmentId.equals("") ? "-1" : treatmentId));

        list = DataBaseUtil.queryToDynaList(ps);
        String query =
            "SELECT item_id, service_item_id, service_id, item_name, status, item_qty, shade_id, "
            + "supplier_id "
            + "FROM service_supplies_master ssm "
            + "JOIN dental_supplies_master dsm USING (item_id) "
            + "LEFT JOIN (SELECT * from dental_supplies_items  "
            + " JOIN dental_supplies_order USING (supplies_order_id)  "
            + " WHERE treatment_id = ? ) ffo USING (item_id)  "
            + "JOIN patient_details pd  ON (pd.mr_no = ffo.mr_no "
            + " AND patient_confidentiality_check (pd.patient_group, pd.mr_no))  "
            + "WHERE service_id = ? AND ffo.item_id IS NULL";
        List<BasicDynaBean> l1 = DataBaseUtil.queryToDynaList(query, new Object[] {

            Integer.parseInt(treatmentId.equals("") ? "-1" : treatmentId), serviceId });
        if (l1 != null && !l1.isEmpty()) {
          for (BasicDynaBean bean : l1) {
            list.add(bean);
          }
        }
      } else {
        ps = con.prepareStatement(
            "select distinct dsi.item_name,ssm.*,dsmr.status "
            + "from service_supplies_master ssm "
            + "join dental_supplier_item_rate_master dsmr using(item_id) "
                + "join dental_supplies_master dsi using (item_id) "
                + "where ssm.service_id=? and dsmr.status = 'A' ");
        ps.setString(1, serviceId);
        list = DataBaseUtil.queryToDynaList(ps);
      }
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
