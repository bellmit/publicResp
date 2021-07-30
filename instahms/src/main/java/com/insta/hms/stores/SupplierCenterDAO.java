package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SupplierCenterDAO extends GenericDAO {

  public SupplierCenterDAO() {
    super("supplier_center_master");

  }

  public static final String GET_CENTERS = " SELECT scm.center_id, scm.status, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, "
      + "	s.state_name, scm.supp_center_id" + " FROM supplier_center_master  scm "
      + "	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=scm.center_id) "
      + "	LEFT JOIN city c ON (c.city_id=hcm.city_id) "
      + "	LEFT JOIN state_master s ON (s.state_id=c.state_id) " + " WHERE scm.supplier_code=?"
      + " ORDER BY s.state_name, c.city_name, hcm.center_name";

  public List getCenters(String suppID) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_CENTERS);
      ps.setString(1, suppID);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public boolean delete(Connection con, String suppID) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(
        "SELECT * FROM supplier_center_master where supplier_code=? and center_id != 0");) {

      ps.setString(1, suppID);
      try (ResultSet rs = ps.executeQuery();) {
        if (!rs.next())
          return true; // no records to delete.
      }
    }
    try (PreparedStatement ps = con.prepareStatement(
        "DELETE FROM supplier_center_master where supplier_code=? and center_id != 0");) {
      ps.setString(1, suppID);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    }
  }

  public boolean delete(Connection con, Integer centerId, String suppID) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(
        "SELECT * FROM supplier_center_master where supplier_code=? and center_id = ?");) {
      ps.setString(1, suppID);
      ps.setInt(2, centerId);
      try (ResultSet rs = ps.executeQuery();) {
        if (!rs.next())
          return true; // no records to delete.
      }
    }
    try (PreparedStatement ps = con.prepareStatement(
        "DELETE FROM supplier_center_master where supplier_code=? and center_id = ?");) {
      ps.setString(1, suppID);
      ps.setInt(2, centerId);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    }
  }

  public boolean insert(Connection con, int copyFromSupplier, int copyToSupplier)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          "INSERT INTO supplier_center_master(supp_center_id, supplier_code, center_id, status) "
              + "	(SELECT nextval('supplier_center_master_seq'), ?, center_id, status FROM supplier_center_master WHERE supplier_code=?)");
      ps.setInt(1, copyToSupplier);
      ps.setInt(2, copyFromSupplier);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private static final String IS_ITEM_SUPPLIER_EXIST = "select * from item_supplier_prefer_supplier "
      + " where medicine_id=? and center_id=?";
  public boolean isSupplierCenterExist(int medicine_id, int center_id) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(IS_ITEM_SUPPLIER_EXIST);
      ps.setInt(1, medicine_id);
      ps.setInt(2, center_id);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return false;
  }

  private static final String GET_CENTER_SUPPLIER_DETAILS = "select scm.prefer_supplier_id,scm.center_id, scm.supplier_code, "
      + " center_name, sm.supplier_name,sm.cust_supplier_code "
      + " from item_supplier_prefer_supplier scm "
      + " left join supplier_master sm on (sm.supplier_code = scm.supplier_code) "
      + " left join hospital_center_master hcm on (hcm.center_id = scm.center_id) "
      + " left join store_item_details sdt on (sdt.medicine_id = scm.medicine_id) "
      + " where sdt.medicine_id = ? ";

  public static List<BasicDynaBean> getCenterSupplierDetails(Integer medicineId)
      throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CENTER_SUPPLIER_DETAILS);
      ps.setInt(1, medicineId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }
  private static final String GET_SUPPLIER_CENTER_DETAILS = "select scm.center_id, scm.supplier_code, "
      + " center_name, sm.supplier_name,sm.cust_supplier_code "
      + " from item_supplier_prefer_supplier scm "
      + " left join supplier_master sm on (sm.supplier_code = scm.supplier_code) "
      + " left join hospital_center_master hcm on (hcm.center_id = scm.center_id) ";
  public static List<BasicDynaBean> getSupplierCenterDetails() throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SUPPLIER_CENTER_DETAILS);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  private static final String CENTER_SUPPLIER_DETAILS = "select * from "
      + "	(select sm.supplier_code,supplier_name,sm.cust_supplier_code,coalesce(scm.center_id,0) as center_id "
      + "		from supplier_master sm  "
      + "		left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + "	 order by sm.supplier_name ) as foo where (foo.center_id=?)";

  public List getCenterSupplierNames(Integer centerId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(CENTER_SUPPLIER_DETAILS);
      ps.setInt(1, centerId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (ps != null)
        ps.close();
    }
  }

  private static final String GET_SUPPLIER_CENTER_DETAIL = " SELECT scm.center_id,sm.supplier_code, hcm.city_id,hcm.state_id, scm.supp_center_id,"
      + " hcm.center_name,sm.supplier_name,scm.status,c.city_name,s.state_name "
      + "	FROM supplier_center_master  scm "
      + "	LEFT JOIN supplier_master sm on(scm.supplier_code=sm.supplier_code) "
      + "	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=scm.center_id) "
      + "	LEFT JOIN city c ON (c.city_id=hcm.city_id) "
      + "	LEFT JOIN state_master s ON (s.state_id=c.state_id) " + " WHERE scm.center_id != 0 "
      + "	ORDER BY s.state_name, c.city_name, hcm.center_name ";
  public static List<BasicDynaBean> getSupplierCenterDetail() throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SUPPLIER_CENTER_DETAIL);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  private static final String DELETE_DEFAULT_CENTER = "delete from supplier_center_master where "
      + "supplier_code in  (select supplier_code from supplier_center_master  "
      + "GROUP BY supplier_code having count(center_id) > 1) and center_id=0 ";

  public boolean deleteDefaultCenter() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DELETE_DEFAULT_CENTER);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  private static final String SELECT_DEFAULT_CENTER = "select nextval('supplier_center_master_seq') as supp_center_id ,0 as center_id,supplier_code,status from supplier_master "
      + "where supplier_code NOT IN (select supplier_code from supplier_center_master) ";
  public boolean insertDefaultCenter() throws SQLException, IOException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    con.setAutoCommit(false);
    try {
      List supplierCodesList = DataBaseUtil.queryToDynaList(SELECT_DEFAULT_CENTER);
      return new GenericDAO("supplier_center_master").insertAll(con, supplierCodesList);

    } finally {
      con.commit();
      DataBaseUtil.closeConnections(con, null);
    }

  }

}
