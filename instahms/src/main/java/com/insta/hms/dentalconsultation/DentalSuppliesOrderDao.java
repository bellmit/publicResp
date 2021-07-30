package com.insta.hms.dentalconsultation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DentalSuppliesOrderDao extends GenericDAO {

  public DentalSuppliesOrderDao() {
    super("dental_supplies_order");
  }

  public static final String GET_ORDER_DETAILS = " SELECT dso.supplies_order_id, dso.mr_no, "
      + "supplies_order_status, dso.supplier_id, dsup.supplier_name, dso.treatment_id, "
      + "s.service_name, ordered_by, ordered_date, doctor_name as ordered_by_name,"
      + " dso.remarks, dso.center_id, hcm.center_name "
      + "FROM dental_supplies_order dso "
      + "JOIN dental_supplier_master dsup ON (dsup.supplier_id=dso.supplier_id) "
      + "   LEFT JOIN tooth_treatment_details ttd ON (ttd.treatment_id=dso.treatment_id) "
      + "   LEFT JOIN services s ON (ttd.service_id=s.service_id) "
      + "JOIN doctors d ON (d.doctor_id=dso.ordered_by) "
      + "JOIN hospital_center_master hcm ON (hcm.center_id=dso.center_id) " + " WHERE dso.mr_no=? "
      + "ORDER BY dso.supplies_order_id";

  /**
   * Gets the order details.
   *
   * @param mrNo the mr no
   * @return the order details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOrderDetails(String mrNo) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ORDER_DETAILS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
