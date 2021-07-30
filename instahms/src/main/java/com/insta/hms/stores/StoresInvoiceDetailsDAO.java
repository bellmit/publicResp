package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StoresInvoiceDetailsDAO {

  public static final int FIELD_NONE = 0;
  public static final int FIELD_GRNNO = 1;
  public static final int FIELD_GRNSUPPLIER = 2;
  public static final int FIELD_GRNINVOICE = 3;
  public static final int FIELD_GDATE = 4;

  public static final int FIELD_SUPPLIER = 1;
  public static final int FIELD_INVOICE = 2;
  public static final int FIELD_IDATE = 3;
  public static final int FIELD_DDATE = 4;

  public static final int FIELD_ISSUE_NO = 1;
  public static final int FIELD_ISSUE_DATE = 2;

  public static final int FIELD_RETURNNO = 1;
  public static final int FIELD_RETURN_DATE = 2;

  public static final int FIELD_TRANSFERNO = 1;
  public static final int FIELD_TRANSFERDATE = 2;

  public static final int FIELD_PONO = 1;
  public static final int FIELD_PODATE = 2;
  public static final int FIELD_SUPP = 3;
  public static final int FIELD_QUT_NO = 4;
  
  private static final GenericDAO storeStockDetailsDAO = new GenericDAO("store_stock_details");

  Connection connection = null;

  public StoresInvoiceDetailsDAO() {
  }

  public StoresInvoiceDetailsDAO(Connection con) {
    connection = con;
  }

  public static String getNextId() throws SQLException {
    return AutoIncrementId.getSequenceId("inventory_grn_id_seq", "INVENTORY GRN");
  }

  private static final String GET_ITEMS_IN_MASTER = " SELECT DISTINCT ITD.MEDICINE_NAME,ITD.MEDICINE_ID,ITD.CUST_ITEM_CODE,M.MANF_NAME,M.MANF_CODE,M.MANF_MNEMONIC,COALESCE(ITD.PKG_TYPE,'') AS PACKAGE_TYPE, "
      + " ITD.ISSUE_QTY, IC.CATEGORY_ID AS CATEGORY_NAME,IC.ISSUE_TYPE,ITD.ISSUE_UNITS,ITD.VALUE,ITD.PKG_SIZE,IC.IDENTIFICATION,"
      + " COALESCE((select cost_price from store_grn_details "
      + " join store_grn_main using(grn_no) where medicine_id=ITD.medicine_ID "
      + " order by grn_date desc limit 1),0) AS COSTPRICE,"
      + " COALESCE((SELECT MRP FROM STORE_STOCK_DETAILS WHERE MEDICINE_ID=ITD.MEDICINE_ID ORDER BY EXP_DT DESC LIMIT 1),0) AS MRP ,"
      + " IC.IDENTIFICATION " + " FROM STORE_ITEM_DETAILS ITD "
      + " RIGHT OUTER JOIN MANF_MASTER M USING(MANF_CODE) "
      + " RIGHT OUTER JOIN store_category_master IC USING(CATEGORY_ID)"
      + " WHERE M.STATUS='A' AND ITD.STATUS='A' AND IC.STATUS='A' ORDER BY MEDICINE_NAME";

  public static ArrayList getItemNamesInMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ITEMS_IN_MASTER);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_CAT_IDEN = "select * from store_category_master";

  public static ArrayList getCatIdentification() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CAT_IDEN);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String[] QUERY_FIELD_NAMES5 = { "", "TRANSFER_NO", "DATE_TIME" };

  private static final String TRANS_EXT_QUERY_FIELDS = " SELECT TRANSFER_NO,TO_CHAR(DATE_TIME,'DD-MM-YYYY HH:MI:SS') AS TRANSDATE,"
      + " GDF.DEPT_NAME AS FROMSTORE,GDT.DEPT_NAME AS TOSTORE,USERNAME";

  private static final String TRANS_EXT_QUERY_COUNT = " SELECT count(TRANSFER_NO) ";

  private static final String TRANS_EXT_QUERY_TABLES = " FROM STORE_TRANSFER_MAIN ISTM JOIN  STORES GDF ON "
      + " ISTM.STORE_FROM= GDF.DEPT_ID " + " JOIN STORES GDT ON ISTM.STORE_TO=GDT.DEPT_ID";

  public static PagedList searchStockTransfers(String transNo, String fStore, String tStore,
      java.sql.Date fromDate, java.sql.Date toDate, int sortOrder, boolean sortReverse,
      int pageSize, int pageNum) throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    String sortField = null;
    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES5.length)) {
      sortField = QUERY_FIELD_NAMES5[sortOrder];
    }

    SearchQueryBuilder qb = new SearchQueryBuilder(con, TRANS_EXT_QUERY_FIELDS,
        TRANS_EXT_QUERY_COUNT, TRANS_EXT_QUERY_TABLES, null, sortField, sortReverse, pageSize,
        pageNum);

    if (!transNo.equalsIgnoreCase("0"))
      qb.addFilter(qb.NUMERIC, "TRANSFER_NO", "=", new BigDecimal(transNo));
    qb.addFilter(qb.STRING, "STORE_FROM", "=", fStore);
    qb.addFilter(qb.STRING, "STORE_TO", "=", tStore);
    qb.addFilter(qb.DATE, "date_trunc('day', DATE_TIME)", ">=", fromDate);
    qb.addFilter(qb.DATE, "date_trunc('day', DATE_TIME)", "<=", toDate);
    try {
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      try {
        qb.close();
      } catch (SQLException e) {
      } // ignored
      DataBaseUtil.closeConnections(con, null);
    }

  }

  public static boolean updateStock(Connection con, Map<String, Object> item, String query)
      throws SQLException {

    try (PreparedStatement ps = con.prepareStatement(query);) {
      for (Map.Entry e : (Collection<Map.Entry<String, Object>>) item.entrySet()) {
        ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
      }
      int count = ps.executeUpdate();
      return count > 0;
    }
  }

  public static BigDecimal[] copyStringArrayTOBigdecimal(int len, Object array) {
    BigDecimal[] value = new BigDecimal[len];
    String[] a = (String[]) array;
    for (int i = 0; i < len; i++) {
      value[i] = new BigDecimal(a[i]);
    }
    return value;
  }

  private static final String STORE_MAIN_STOCK_DETAILS_QUERY = "SELECT * FROM STORE_STOCK_DETAILS "
      + " JOIN store_item_details sib USING(medicine_id) "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)";
  public BasicDynaBean getStock(Connection con, String store, String item_id,
      String item_identiofier) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    BasicDynaBean bean = null;
    try {
      ps = con.prepareStatement(STORE_MAIN_STOCK_DETAILS_QUERY
          + " WHERE DEPT_ID=? AND sibd.MEDICINE_ID=? AND sibd.BATCH_NO=?");
      ps.setInt(1, Integer.parseInt(store));
      ps.setInt(2, Integer.parseInt(item_id));
      ps.setString(3, item_identiofier);
      rs = ps.executeQuery();
      while (rs.next()) {
        bean = storeStockDetailsDAO.getBean();
        bean.set("dept_id", rs.getInt("dept_id"));
        bean.set("medicine_id", rs.getInt("medicine_id"));
        bean.set("batch_no", rs.getString("batch_no"));
        bean.set("package_cp", rs.getBigDecimal("package_cp"));
        bean.set("package_sp", rs.getBigDecimal("package_sp"));
        bean.set("asset_approved", rs.getString("asset_approved"));
        bean.set("stock_time", rs.getTimestamp("stock_time"));
        bean.set("username", rs.getString("username"));
        bean.set("qty", rs.getBigDecimal("qty"));
        bean.set("qty_in_use", new BigDecimal("0"));
        bean.set("qty_maint", new BigDecimal("0"));
        bean.set("qty_retired", new BigDecimal("0"));
        bean.set("qty_lost", new BigDecimal("0"));
        bean.set("qty_unknown", new BigDecimal("0"));
        bean.set("qty_kit", new BigDecimal("0"));
        bean.set("tax", rs.getBigDecimal("tax"));
        bean.set("consignment_stock", rs.getBoolean("consignment_stock"));
        bean.set("stock_pkg_size", rs.getBigDecimal("stock_pkg_size"));
        bean.set("max_cp_grn", rs.getString("max_cp_grn"));
        bean.set("last_cp_grn", rs.getString("last_cp_grn"));
        bean.set("package_uom", rs.getString("package_uom"));
        bean.set("item_lot_id", rs.getInt("item_lot_id"));
      }
      return bean;
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
  }

  private final static String STORE_CONSIGNMENT_STOCK_RETRUNS_AMOUNTS = "SELECT m.user_return_no,"
      + " SUM(ic.amount*r.qty) AS final_amt,ic.supplier_id, DATE( m.date_time) AS con_return_date, s.supplier_name,s.cust_supplier_code "
      + " FROM store_issue_returns_main m "
      + " JOIN store_issue_returns_details r ON r.user_return_no = m.user_return_no "
      + " JOIN store_consignment_invoice ic on ic.issue_id = m.user_issue_no AND "
      + " ic.item_id = r.medicine_id AND ic.item_identifier = r.batch_no "
      + " JOIN store_grn_main ingm ON ingm.grn_no=ic.grn_no "
      + " JOIN store_invoice i ON i.supplier_invoice_id=ingm.supplier_invoice_id "
      + " JOIN supplier_master s ON (ic.supplier_id = s.supplier_code) "
      + " WHERE date(m.date_time) between ? AND ? AND i.account_group=?"
      + " GROUP BY m.user_return_no, date(m.date_time), ic.supplier_id, s.supplier_name ,ic.issue_id "
      + " ORDER BY m.user_return_no";
  public static List getConsignmentStockReturnedAmounts(java.sql.Date fromDate,
      java.sql.Date toDate, Integer accountGroup) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(STORE_CONSIGNMENT_STOCK_RETRUNS_AMOUNTS);
      ps.setDate(1, fromDate);
      ps.setDate(2, toDate);
      ps.setInt(3, accountGroup);
      return DataBaseUtil.queryToDynaListDates(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
