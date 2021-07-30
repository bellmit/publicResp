package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
/*
 * DAO to manipulate the table store_retail_customers. Note: static
 * methods are used for all queries, and do not need an existing connection.
 */
import java.util.ArrayList;
import java.util.List;

public class RetailCustomerDAO extends GenericDAO {

  Connection con = null;

  public RetailCustomerDAO() {
    super("store_retail_customers");
  }

  public String getNextId() throws SQLException {
    return AutoIncrementId.getSequenceId("retail_customer_id_sequence", "PHCUSTID");
  }

  /*
   * Fetch a retail customer record
   */
  public BasicDynaBean getRetailCustomer(String customerId) throws SQLException {
    return findByKey("customer_id", customerId);
  }

  /*
   * Fetch a retail customer record with Sponsor Name expanded
   */
  private static final String GET_RETAIL_CUSTOMER_EX = " SELECT "
      + " customer_id, customer_name, visit_date, is_credit, phone_no, credit_limit, center_id, "
      + " srp.sponsor_id, srp.sponsor_name, nationality_id, government_identifier, identifier_id "
      + "FROM store_retail_customers src "
      + " LEFT JOIN store_retail_sponsors srp ON (srp.sponsor_id = src.sponsor_name) "
      + " WHERE customer_id=?";

  public BasicDynaBean getRetailCustomerEx(String customerId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_RETAIL_CUSTOMER_EX, customerId);
  }

  public BasicDynaBean getRetailCustomerEx(Connection con, String customerId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(con, GET_RETAIL_CUSTOMER_EX, customerId);
  }

  /*
   * Insert a retail customer record
   */
  public boolean insertRetailCustomer(Connection con, BasicDynaBean b)
      throws SQLException, IOException {
    return insert(con, b);
  }

  private static final String GET_ACTIVE_CREDIT_CUSTOMERS = "SELECT b.bill_no, b.bill_type,customer_id,customer_name,"
      + " visit_date,is_credit,phone_no,credit_limit,prs.sponsor_name, "
      + " (COALESCE((SELECT SUM(r.amount) FROM bill_receipts br join receipts r on r.receipt_id = br.receipt_no WHERE br.bill_no = b.bill_no),0) ) AS refund,"
      + " (COALESCE((SELECT sum(sm.total_item_amount-discount+round_off) FROM store_sales_main sm WHERE sm.bill_no = b.bill_no),0) ) AS bill_amount"
      + " FROM store_retail_customers prc"
      + "  LEFT JOIN store_retail_sponsors prs on prs.sponsor_id=prc.sponsor_name"
      + "  LEFT JOIN bill b ON (b.visit_id=customer_id)"
      + "  JOIN store_sales_main m USING (bill_no)"
      + "  JOIN store_sales_details s USING (sale_id) "
      + " JOIN stores st ON (st.dept_id=m.store_id) "
      + " WHERE b.bill_type='C' AND b.status='A' AND st.center_id=? and customer_name ilike ?"
      + " GROUP BY b.bill_no, bill_type,customer_id,customer_name,"
      + "  visit_date,is_credit,phone_no,credit_limit,prs.sponsor_name" + " ORDER BY customer_name";

  public List getActiveCreditCustomers(int centerId, String name) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ACTIVE_CREDIT_CUSTOMERS);
      ps.setInt(1, centerId);
      ps.setString(2, name + "%");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_DOCTOR_NAMES = "select distinct(doctor_name) from ("
      + " select distinct(doctor_name) from store_retail_doctor where status='A'" + " union all"
      + " select distinct(doctor_name) from doctors where status='A')as doc ";

  public static ArrayList getDoctorNames() throws SQLException {
    return DataBaseUtil.queryToArrayList(GET_DOCTOR_NAMES);
  }

  public static final String GET_DOCTOR_OF_RETAIL_CUSTOMER = "SELECT doctor_name FROM store_sales_main WHERE bill_no = ?";

  public List getDoctorOfRetailCustomer(String billNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_DOCTOR_OF_RETAIL_CUSTOMER);
      ps.setString(1, billNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static String GET_ALL_SPONSOR_NAMES = "SELECT sponsor_name FROM store_retail_sponsors WHERE status='A' group by sponsor_name "
      + " UNION " + " SELECT tpa_name FROM tpa_master  group by tpa_name";

  public static List getAllSponsorNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_SPONSOR_NAMES);
  }

  private static final String IS_UNIQUE_GOVT_ID = "SELECT government_identifier FROM store_retail_customers WHERE government_identifier = ?";

  public static boolean isUniqueGovtID(String govtID) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(IS_UNIQUE_GOVT_ID);
      pstmt.setString(1, govtID);
      return DataBaseUtil.queryToDynaBean(pstmt) == null;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }
}
