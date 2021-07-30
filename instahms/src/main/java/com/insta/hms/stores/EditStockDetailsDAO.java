package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MastersQueryHandler;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EditStockDetailsDAO extends GenericDAO {

  public EditStockDetailsDAO() {
    super("store_stock_details");
    // TODO Auto-generated constructor stub
  }

  private static final String STOCK_EXT_QUERY_FIELDS = " SELECT * ";

  private static final String STOCK_EXT_QUERY_COUNT = " SELECT count(*) ";

  private static final String STOCK_EXT_QUERY_TABLES = " FROM store_stock_details_view sgl ";

  public static PagedList searchList(Map filter, Map listing) throws Exception {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, STOCK_EXT_QUERY_FIELDS,
          STOCK_EXT_QUERY_COUNT, STOCK_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("medicine_id");
      qb.build();

      PagedList l = qb.getMappedPagedList();

      qb.close();
      return l;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

  }

  public static ArrayList getDepartments() {
    return (DataBaseUtil.queryToArrayList(MastersQueryHandler.getDeptIdsAndNames));
  }

  public boolean updateMRP(List stockList) throws SQLException, Exception {
    PreparedStatement psupdateMRP = null; // creating preparedstatment object
    Connection con = null;
    int resultCount = 0; // intializing count variable
    try {
      con = DataBaseUtil.getConnection();
      psupdateMRP = con.prepareStatement(
          "UPDATE store_stock_details SET MRP=?,PACKAGE_SP=?,BIN=?,EXP_DT=?,PACKAGE_CP=?,TAX_RATE=?,USERNAME=?,CHANGE_SOURCE=?  WHERE MEDICINE_ID=?  AND BATCH_NO=? AND DEPT_ID=?");
      Iterator itr = stockList.iterator();
      EditStock stock = null;
      while (itr.hasNext()) {
        stock = (EditStock) itr.next();
        String vatrate = stock.getTrate();
        BigDecimal amt = ConversionUtils.setScale((new BigDecimal(stock.getMrp()))
            .divide(BigDecimal.ONE.add((new BigDecimal(vatrate).divide(new BigDecimal(100))))));
        // double amt =
        // ((Double.parseDouble(stock.getMrp()))/(1+(Double.parseDouble(vatrate)/100)));
        BigDecimal rate = amt;
        // double rate = new BigDecimal(amt).doubleValue();
        psupdateMRP.setBigDecimal(1, new BigDecimal(stock.getMrp()));
        psupdateMRP.setBigDecimal(2, rate);
        psupdateMRP.setString(3, stock.getBin());
        psupdateMRP.setDate(4, new java.sql.Date(
        		DateUtil.getLastDayInMonth(
        				Integer.parseInt(stock.getMon()), 
        				Integer.parseInt(stock.getHyear())).getTime()));
        psupdateMRP.setBigDecimal(5, new BigDecimal(stock.getCp()));
        psupdateMRP.setBigDecimal(6, new BigDecimal(stock.getTrate()));
        psupdateMRP.setString(7, stock.getUsername());
        psupdateMRP.setString(8, stock.getChange_source());
        psupdateMRP.setString(9, stock.getMedicineId());
        psupdateMRP.setString(10, stock.getBatchNo());
        psupdateMRP.setString(11, stock.getDeptId());

        resultCount = resultCount + psupdateMRP.executeUpdate();
      }
    } finally {
      DataBaseUtil.closeConnections(con, psupdateMRP);
    }
    return resultCount == stockList.size();
  }
  public static BasicDynaBean getMedicineDetails(String medicineId, String batchNo, String deptId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    int medicineIdNum = -1;
    int deptIdNum = -1;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      StringBuilder query = new StringBuilder(STOCK_EXT_QUERY_FIELDS + STOCK_EXT_QUERY_TABLES);
      if ((null != medicineId) && (medicineId.trim().length() > 0)) {
        medicineIdNum = Integer.parseInt(medicineId);
      }
      if ((null != deptId) && (deptId.trim().length() > 0)) {
        deptIdNum = Integer.parseInt(deptId);
      }
      if (medicineIdNum > -1) {
        SearchQueryBuilder.addWhereFieldOpValue(false, query, "SGL.medicine_id", "=",
            medicineIdNum);
        SearchQueryBuilder.addWhereFieldOpValue(true, query, "SGL.batch_no", "=",
            DataBaseUtil.quoteIdent(batchNo));
        SearchQueryBuilder.addWhereFieldOpValue(true, query, "SGL.dept_id", "=", deptIdNum);

        ps = con.prepareStatement(query.toString());

        ps.setInt(1, medicineIdNum);
        ps.setString(2, batchNo);
        ps.setInt(3, deptIdNum);
        List l = DataBaseUtil.queryToDynaList(ps);
        if (l.size() > 0)
          return (BasicDynaBean) l.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  public static boolean checkQtyAvailable(Connection con, String storeId, String itemId,
      int identifier, BigDecimal returnqty) throws SQLException {
    boolean qtyAvailable = false;
    PreparedStatement ps = null;
    ResultSet rs = null;

    ps = con.prepareStatement(
        "select sum(qty) as qty from store_stock_details where medicine_id =? and item_batch_id=?"
            + " and dept_id=? and qty>0 and qty >=?");
    ps.setInt(1, Integer.parseInt(itemId));
    ps.setInt(2, identifier);
    ps.setInt(3, Integer.parseInt(storeId));
    ps.setBigDecimal(4, returnqty);

    rs = ps.executeQuery();

    if (rs.next())
      qtyAvailable = true;

    return qtyAvailable;

  }

  private static final String UDATE_APPROVAL = "UPDATE store_stock_details SET ASSET_APPROVED='Y', USERNAME=?, CHANGE_SOURCE=? "
      + " WHERE DEPT_ID=? AND MEDICINE_ID=? AND BATCH_NO=? ";

  public boolean updateApproval(Connection con, String[] itemId, String[] identifier,
      String storeId, String username) throws SQLException {
    boolean status = true;
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(UDATE_APPROVAL);
      for (int i = 0; i < itemId.length; i++) {
        ps.setString(1, username);
        ps.setString(2, "Asset Approved");
        ps.setInt(3, Integer.parseInt(storeId));
        ps.setInt(4, Integer.parseInt(itemId[i]));
        ps.setString(5, identifier[i]);
        status = ps.executeUpdate() > 0;

        if (!status)
          break;
      }
    } catch (Exception e) {
      status = false;
    } finally {
      if (ps != null)
        ps.close();
    }
    return status;
  }

  private static final String UDATE_APPROVAL_ALL = "UPDATE store_stock_details SET asset_approved='Y',"
      + "username=?, change_source=? WHERE dept_id=? AND asset_approved ='N'";

  public boolean updateApproveAll(Connection con, String storeId, String username)
      throws SQLException {
    boolean status = true;
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(UDATE_APPROVAL_ALL);
      ps.setString(1, username);
      ps.setString(2, "Asset Approved");
      ps.setInt(3, Integer.parseInt(storeId));
      count = ps.executeUpdate();
    } catch (Exception e) {
      status = false;
    } finally {
      if (ps != null)
        ps.close();
    }
    return status;
  }

  /**
   * for item details pop-up
   */
  private static final String GET_ITEM_DETAILS_FOR_POP_UP = "SELECT INGM.GRN_NO,TO_CHAR(INGM.GRN_DATE,'YYYY-MM-DD') AS GRN_DATE, "
      + " COALESCE(INGM.PO_NO,'') AS PO_NO,COALESCE(IINV.INVOICE_NO,'') AS INVOICE_NO,"
      + " TO_CHAR(IINV.INVOICE_DATE,'YYYY-MM-DD') AS INVOICE_DATE,SM.SUPPLIER_NAME,SM.CUST_SUPPLIER_CODE,"
      + " ITD.MEDICINE_NAME,ING.MRP,ING.COST_PRICE,ING.TAX_RATE,ITD.CUST_ITEM_CODE "
      + " FROM store_grn_main INGM JOIN store_grn_details ING ON INGM.GRN_NO = ING.GRN_NO"
      + " JOIN store_item_details ITD ON ITD.MEDICINE_ID=ING.MEDICINE_ID"
      + " LEFT JOIN store_invoice IINV using(supplier_invoice_id)"
      + " JOIN SUPPLIER_MASTER SM ON SM.SUPPLIER_CODE=IINV.SUPPLIER_ID"
      + " WHERE ING.MEDICINE_ID=? AND ING.BATCH_NO=?  ";

  public static List<BasicDynaBean> getItemDetails(int itemId, String identifier,
      List<Integer> storeList) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    StringBuilder query = new StringBuilder(GET_ITEM_DETAILS_FOR_POP_UP);

    DataBaseUtil.addWhereFieldInList(query, "store_id", storeList, true);
    try {
      int i = 3;
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query.toString());
      ps.setInt(1, itemId);
      ps.setString(2, identifier);
      for (Integer store : storeList) {
        ps.setInt(i, store);
        i++;
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  private static final String GET_PACKAGE_UOM_AND_PACKAGE_SIZE = "SELECT issue_uom,package_uom,package_size FROM package_issue_uom"
      + " WHERE issue_uom = ? ";
  public static List<BasicDynaBean> getPacakgeUOMDetails(String issueUnit) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PACKAGE_UOM_AND_PACKAGE_SIZE);
      ps.setString(1, issueUnit);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
