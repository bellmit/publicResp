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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The Class StoresSupplierReturnsDAO.
 */
public class StoresSupplierReturnsDAO {

  /** The Constant FIELD_NONE. */
  /*
   * Constants used for sort order
   */
  public static final int FIELD_NONE = 0;

  /** The Constant FIELD_RETURNNO. */
  public static final int FIELD_RETURNNO = 1;

  /** The Constant FIELD_DEBITNO. */
  public static final int FIELD_DEBITNO = 1;

  /**
   * Gets the stores.
   *
   * @param suppId
   *          the supp id
   * @param invno
   *          the invno
   * @return the stores
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getStores(String suppId, String invno) throws SQLException {
    ArrayList<String> storeId = new ArrayList<String>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    Connection con = null;
    BigDecimal supplierInvoiceId = DirectStockEntryDAO.getSupplierInvoice(suppId, invno);
    String query = "select distinct(pngm.store_id)" + " from store_grn_main pngm"
        + " join store_grn_details png using(grn_no)"
        + " join store_item_details pmd using(medicine_id)"
        + " join manf_master mm on pmd.manf_name=manf_code"
        + " join store_stock_details pmsd on dept_id=pngm.store_id and pmsd.medicine_id="
        + " png.medicine_id and pmsd.batch_no=png.batch_no left join store_supplier_returns_main"
        + " psrm on psrm.store_id=pngm.store_id and psrm.supplier_invoice_id="
        + " pngm.supplier_invoice_id  left join store_supplier_returns psr using(return_no)"
        + " where pngm.supplier_invoice_id=?";

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      ps.setBigDecimal(1, supplierInvoiceId);
      rs = ps.executeQuery();
      while (rs.next()) {
        storeId.add(rs.getString("store_id"));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return storeId;
  }

  /**
   * Supplier id to name.
   *
   * @param suppId
   *          the supp id
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public static String supplierIdToName(String suppId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    String supplyQuery = "SELECT SUPPLIER_NAME FROM SUPPLIER_MASTER WHERE SUPPLIER_CODE=?";
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(supplyQuery);
      ps.setString(1, suppId);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the store names.
   *
   * @param storeId
   *          the store id
   * @return the store names
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getStoreNames(String storeId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList response = new ArrayList();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("SELECT DEPT_ID,DEPT_NAME FROM stores WHERE DEPT_ID = ?");
      ps.setInt(1, Integer.parseInt(storeId));
      response = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return response;
  }

  /** The Constant GET_SEL_MED_DET. */
  private static final String GET_SEL_MED_DET = "select store_id,pmsd.medicine_id,sibd.batch_no,"
      + " sum(invqty) as invoiceqty, medicine_name,mm.manf_mnemonic,"
      + " round(pmsd.qty/pmd.issue_base_unit,2) as stockqty,"
      + " sibd.exp_dt as date,sibd.mrp,pmd.issue_base_unit,pmsd.package_cp,pmd.tax_type,"
      + " pmd.tax_rate,pmsd.package_sp,to_char(sibd.exp_dt,'mm/dd/yyyy') as expdt,"
      + " (case when pmd.tax_type='CB' then"
      + " (pmsd.package_cp-((pmsd.package_cp*100)/(100+pmd.tax_rate)))"
      + " when pmd.tax_type='C' then (pmsd.package_cp-((pmsd.package_cp*100)/(100+pmd.tax_rate)))"
      + " when pmd.tax_type='MB' then ((pmsd.package_sp*pmd.tax_rate)/100) "
      + " else ((pmsd.package_sp*pmd.tax_rate)/100) end )as taxamount, 0 as returnedqty,"
      + " 0 as discount,0 as tax from "
      + " (select store_id,medicine_id,batch_no,sum(qty) as invqty" + " from"
      + " (select store_id,medicine_id,batch_no,sum(billed_qty+bonus_qty) as qty"
      + " from store_grn_main" + " join store_grn_details using(grn_no)"
      + " where store_id=? and supplier_invoice_id=?" + " group by store_id,medicine_id,batch_no"
      + " union all" + " select store_id,medicine_id,batch_no,sum(0-qty)"
      + " from store_supplier_returns_main" + " join store_supplier_returns using(return_no)"
      + " where store_id=? and supplier_invoice_id=?"
      + " group by store_id,medicine_id,batch_no) as sub1"
      + " group by store_id,medicine_id,batch_no) as sub2"
      + " join store_item_details pmd using(medicine_id)"
      + " join manf_master mm on manf_code=pmd.manf_name"
      + " join store_stock_details pmsd on dept_id=store_id and"
      + " pmsd.medicine_id=sub2.medicine_id and pmsd.batch_no=sub2.batch_no"
      + " JOIN store_item_batch_details sibd ON ( sibd.item_batch_id = pmsd.item_batch_id)"
      + " group by store_id,pmsd.medicine_id,pmsd.batch_no,medicine_name,mm.manf_mnemonic,"
      + " pmsd.qty,pmd.issue_base_unit,pmsd.exp_dt,pmsd.mrp,pmsd.package_cp,"
      + " pmsd.package_sp,pmd.tax_rate,pmd.tax_type";

  /**
   * Gets the med det of sel invocie.
   *
   * @param suppId
   *          the supp id
   * @param invno
   *          the invno
   * @param storeId
   *          the store id
   * @return the med det of sel invocie
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getMedDetOfSelInvocie(String suppId, String invno,
      String storeId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      BigDecimal supplierInvoiceId = DirectStockEntryDAO.getSupplierInvoice(suppId, invno);
      ps = con.prepareStatement(GET_SEL_MED_DET);
      ps.setString(1, storeId);
      ps.setBigDecimal(2, supplierInvoiceId);
      // ps.setString(3, invno);
      ps.setString(3, storeId);
      ps.setBigDecimal(4, supplierInvoiceId);
      // ps.setString(6, invno);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the med det.
   *
   * @param suppId
   *          the supp id
   * @param invno
   *          the invno
   * @param storeId
   *          the store id
   * @return the med det
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList<String> getMedDet(String suppId, String invno, String storeId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SEL_MED_DET);
      ps.setString(1, storeId);
      ps.setString(2, suppId);
      ps.setString(3, invno);
      ps.setString(4, storeId);
      ps.setString(5, suppId);
      ps.setString(6, invno);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_RET_DET. */
  private static final String GET_RET_DET = "select return_no,supplier_name,cust_supplier_code,"
      + " pm.supplier_id,return_type,pm.remarks,ret_qty_unit,store_id"
      + " from store_supplier_returns_main pm "
      + " join supplier_master on supplier_code=pm.supplier_id where return_no=?";

  /**
   * Gets the return med det.
   *
   * @param retNo
   *          the ret no
   * @return the return med det
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getReturnMedDet(int retNo) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RET_DET);
      ps.setInt(1, retNo);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the return med det.
   *
   * @param supplierId
   *          the supplier id
   * @param invoiceNo
   *          the invoice no
   * @return the return med det
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getReturnMedDet(String supplierId, String invoiceNo)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INV_DET);
      ps.setString(1, supplierId);
      ps.setString(2, invoiceNo);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


  /**
   * Gets the supplier returns next id.
   *
   * @return the supplier returns next id
   * @throws SQLException
   *           the SQL exception
   */
  public static int getSupplierReturnsNextId() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("select nextval('supplier_return_sequence')");
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the store gate pass next id.
   *
   * @return the store gate pass next id
   * @throws SQLException
   *           the SQL exception
   */
  public static int getStoreGatePassNextId() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("select nextval('store_gatepass_seq')");
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update stock.
   *
   * @param item
   *          the item
   * @param query
   *          the query
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateStock(List<HashMap<String, Object>> item, String query)
      throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    int count = 0;
    int count1 = 0;
    Boolean exists = false;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(query);
      for (Map<String, Object> items : item) {
        for (Map.Entry e : (Collection<Map.Entry<String, Object>>) items.entrySet()) {
          ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
        }
        count1 = ps.executeUpdate();
        count = count + count1;
      }
      if (count == item.size()) {
        con.commit();
      } else {
        con.rollback();
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return count == item.size();
  }

  /**
   * Update stock with GRN.
   *
   * @param item
   *          the item
   * @param query
   *          the query
   * @param grnQuery
   *          the grn query
   * @param strWhere
   *          the str where
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateStockWithGRN(List<HashMap<String, Object>> item, StringBuilder query,
      String grnQuery, String strWhere) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    int count = 0;
    int count1 = 0;
    Boolean success = false;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);
      query.append(grnQuery);
      query.append(strWhere);
      for (Map<String, Object> items : item) {
        ps = con.prepareStatement(query.toString());
        for (Map.Entry e : (Collection<Map.Entry<String, Object>>) items.entrySet()) {
          ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
        }

        count1 = ps.executeUpdate();
        count = count + count1;
        success = true;
      }
      if (count == item.size()) {
        con.commit();
      } else {
        con.rollback();
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return count == item.size();
  }

  /** The Constant UPDATE_REPLACED_QTY. */
  private static final String UPDATE_REPLACED_QTY = "update store_supplier_returns set"
      + " replaced_qty=(replaced_qty+?) where return_no=? and medicine_id=? and batch_no=?";

  /**
   * Update replaced qty.
   *
   * @param con
   *          the con
   * @param item
   *          the item
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateReplacedQty(Connection con, List<HashMap<String, Object>> item)
      throws SQLException {

    PreparedStatement ps = null;
    int count = 0;
    int count1 = 0;
    try {
      ps = con.prepareStatement(UPDATE_REPLACED_QTY);
      for (Map<String, Object> items : item) {
        for (Map.Entry e : (Collection<Map.Entry<String, Object>>) items.entrySet()) {
          ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
        }
        count1 = ps.executeUpdate();
        count = count + count1;
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return count == item.size();
  }

  /** The Constant QUERY_FIELD_NAMES2. */
  private static final String[] QUERY_FIELD_NAMES2 = { "", "RETURN_NO" };

  /** The Constant RETURN_EXT_QUERY_FIELDS. */
  private static final String RETURN_EXT_QUERY_FIELDS = " SELECT RETURN_NO,S.SUPPLIER_NAME,"
      + " S.CUST_SUPPLIER_CODE,TO_CHAR(PSRM.DATE_TIME,'DD-MM-YYYY ') AS RETURNDATE,"
      + " RETURN_NO,USER_NAME," + " RETURN_TYPE,PSRM.STATUS,ORIG_RETURN_NO";

  /** The Constant RETURN_EXT_QUERY_COUNT. */
  private static final String RETURN_EXT_QUERY_COUNT = " SELECT count(RETURN_NO) ";

  /** The Constant RETURN_EXT_QUERY_INITWHERE_ISNULL. */
  private static final String RETURN_EXT_QUERY_INITWHERE_ISNULL = " WHERE ORIG_RETURN_NO IS NULL";

  /** The Constant ETURN_EXT_QUERY_INITWHERE_ISNOTNULL. */
  private static final String ETURN_EXT_QUERY_INITWHERE_ISNOTNULL = " WHERE ORIG_RETURN_NO IS"
      + " NOT NULL";

  /** The Constant RETURN_EXT_QUERY_TABLES. */
  private static final String RETURN_EXT_QUERY_TABLES = " FROM store_supplier_returns_main PSRM "
      + " JOIN SUPPLIER_MASTER S ON SUPPLIER_CODE=PSRM.SUPPLIER_ID";

  /**
   * Search supplier returns.
   *
   * @param retNo
   *          the ret no
   * @param supplier
   *          the supplier
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param sortOrder
   *          the sort order
   * @param sortReverse
   *          the sort reverse
   * @param returnType
   *          the return type
   * @param status
   *          the status
   * @param type
   *          the type
   * @param pageSize
   *          the page size
   * @param pageNum
   *          the page num
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList searchSupplierReturns(int retNo, String supplier, java.sql.Date fromDate,
      java.sql.Date toDate, int sortOrder, boolean sortReverse, List returnType, List status,
      String type, int pageSize, int pageNum) throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    String sortField = null;
    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES2.length)) {
      sortField = QUERY_FIELD_NAMES2[sortOrder];
    }
    SearchQueryBuilder qb = null;
    if (type != null) {
      if (type.equalsIgnoreCase("o")) {
        qb = new SearchQueryBuilder(con, RETURN_EXT_QUERY_FIELDS, RETURN_EXT_QUERY_COUNT,
            RETURN_EXT_QUERY_TABLES, RETURN_EXT_QUERY_INITWHERE_ISNULL, null, sortField,
            sortReverse, pageSize, pageNum);
      } else if (type.equalsIgnoreCase("e")) {
        qb = new SearchQueryBuilder(con, RETURN_EXT_QUERY_FIELDS, RETURN_EXT_QUERY_COUNT,
            RETURN_EXT_QUERY_TABLES, ETURN_EXT_QUERY_INITWHERE_ISNOTNULL, null, sortField,
            sortReverse, pageSize, pageNum);
      }
    } else {
      qb = new SearchQueryBuilder(con, RETURN_EXT_QUERY_FIELDS, RETURN_EXT_QUERY_COUNT,
          RETURN_EXT_QUERY_TABLES, null, sortField, sortReverse, pageSize, pageNum);
    }

    if (retNo > 0) {
      qb.addFilter(qb.INTEGER, "RETURN_NO", "=", retNo);
    }
    if (!supplier.equalsIgnoreCase("All")) {
      qb.addFilter(qb.STRING, "PSRM.SUPPLIER_ID", "=", supplier);
    }
    qb.addFilter(qb.DATE, "date_trunc('day', PSRM.DATE_TIME)", ">=", fromDate);
    qb.addFilter(qb.DATE, "date_trunc('day', PSRM.DATE_TIME)", "<=", toDate);
    qb.addFilter(qb.STRING, "RETURN_TYPE", "IN", returnType);
    qb.addFilter(qb.STRING, "PSRM.STATUS", "IN", status);
    try {
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }

  }



  /** The Constant GET_RET_MED_LIST. */
  private static final String GET_RET_MED_LIST = "select medicine_name,psr.medicine_id,"
      + " sibd.batch_no, round(psr.qty,2) as returnedqty,mm.manf_mnemonic,"
      + " psr.replaced_qty as replacedqty,sibd.mrp,sibd.exp_dt as date,"
      + " pmd.issue_base_unit,scm.identification,pmd.issue_units,"
      + " psr.item_batch_id,psr.return_detail_no,batch_no_applicable "
      + " from store_supplier_returns_main psrm"
      + " join store_supplier_returns psr using(return_no)"
      + " join store_item_details pmd using(medicine_id)"
      + " join store_category_master scm on med_category_id=category_id"
      + " join manf_master mm on pmd.manf_name=mm.manf_code"
      + " JOIN store_item_batch_details sibd USING(item_batch_id)" + " WHERE return_no=?"
      + " group by return_no,psr.medicine_id,sibd.batch_no,psr.qty,psr.replaced_qty,medicine_name,"
      + " mm.manf_mnemonic,sibd.mrp,sibd.exp_dt,scm.identification,pmd.issue_units,"
      + " issue_base_unit,psr.item_batch_id,return_detail_no,batch_no_applicable";

  /**
   * Gets the return med list.
   *
   * @param retNo
   *          the ret no
   * @return the return med list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getReturnMedList(int retNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RET_MED_LIST);
      ps.setInt(1, retNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the reutrn store.
   *
   * @param retNo
   *          the ret no
   * @return the reutrn store
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getReutrnStore(int retNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String query = "select store_id from store_supplier_returns_main where return_no=?";
    ArrayList response = new ArrayList();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      ps.setInt(1, retNo);
      String storeId = DataBaseUtil.getStringValueFromDb(ps);
      response = StoresSupplierReturnsDAO.getStoreNames(storeId);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return response;
  }

  /** The Constant UPDATE_STATUS. */
  private static final String UPDATE_STATUS = "UPDATE store_supplier_returns_main SET STATUS=?"
      + " WHERE RETURN_NO=?";

  /**
   * Update return status.
   *
   * @param con
   *          the con
   * @param item
   *          the item
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateReturnStatus(Connection con, Map<String, Object> item)
      throws SQLException {

    PreparedStatement ps;
    ps = con.prepareStatement(UPDATE_STATUS);

    for (Map.Entry e : (Collection<Map.Entry<String, Object>>) item.entrySet()) {
      ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
    }
    int count = ps.executeUpdate();

    return count > 0;

  }

  /** The Constant GET_INV_DET. */
  private static final String GET_INV_DET = "select pi.*,supplier_name,cust_supplier_code from"
      + " store_invoice pi join supplier_master on supplier_code=supplier_id where"
      + " pi.supplier_id=? and invocie_no=?";


  /**
   * Gets the GR ns.
   *
   * @param supplierId
   *          the supplier id
   * @param invoiceNo
   *          the invoice no
   * @return the GR ns
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getGRNs(String supplierId, String invoiceNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      BigDecimal supplierInvoiceId = DirectStockEntryDAO.getSupplierInvoice(supplierId, invoiceNo);
      ps = con.prepareStatement("SELECT grn_no FROM store_grn_main  WHERE supplier_invoice_id=? AND"
          + " debit_note_no IS NULL");
      ps.setBigDecimal(1, supplierInvoiceId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the debits.
   *
   * @param supplierId
   *          the supplier id
   * @param invoiceNo
   *          the invoice no
   * @return the debits
   * @throws SQLException
   *           the SQL exception
   */
  public static ArrayList getDebits(String supplierId, String invoiceNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      BigDecimal supplierInvoiceId = DirectStockEntryDAO.getSupplierInvoice(supplierId, invoiceNo);
      ps = con
          .prepareStatement("SELECT debit_note_no FROM store_grn_main  WHERE supplier_invoice_id=?"
              + " AND debit_note_no IS NOT NULL");
      ps.setBigDecimal(1, supplierInvoiceId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the next id.
   *
   * @param seqName
   *          the seq name
   * @param typeNumber
   *          the type number
   * @return the next id
   * @throws SQLException
   *           the SQL exception
   */
  public static String getNextId(String seqName, String typeNumber) throws SQLException {
    return AutoIncrementId.getSequenceId(seqName, typeNumber);
  }

  /** The Constant QUERY_FIELD_NAMES3. */
  private static final String[] QUERY_FIELD_NAMES3 = { "", "DEBIT_NOTE_NO" };

  /** The Constant DEBIT_EXT_QUERY_FIELDS. */
  private static final String DEBIT_EXT_QUERY_FIELDS = " SELECT DEBIT_NOTE_NO,S.SUPPLIER_NAME,"
      + " S.CUST_SUPPLIER_CODE,TO_CHAR(PDN.DEBIT_NOTE_DATE,'DD-MM-YYYY ') AS DEBITDATE,"
      + " RETURN_TYPE,PDN.STATUS,PDN.SUPPLIER_ID,PDN.STORE_ID";

  /** The Constant DEBIT_EXT_QUERY_COUNT. */
  private static final String DEBIT_EXT_QUERY_COUNT = " SELECT count(DEBIT_NOTE_NO) ";

  /** The Constant DEBIT_EXT_QUERY_TABLES. */
  private static final String DEBIT_EXT_QUERY_TABLES = " FROM store_debit_note PDN "
      + "JOIN SUPPLIER_MASTER S ON SUPPLIER_CODE=PDN.SUPPLIER_ID";

  /**
   * Search supplier return debits.
   *
   * @param suppId
   *          the supp id
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param debitNote
   *          the debit note
   * @param sortOrder
   *          the sort order
   * @param sortReverse
   *          the sort reverse
   * @param returnType
   *          the return type
   * @param status
   *          the status
   * @param pageSize
   *          the page size
   * @param pageNum
   *          the page num
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList searchSupplierReturnDebits(List suppId, java.sql.Date fromDate,
      java.sql.Date toDate, String debitNote, int sortOrder, boolean sortReverse, List returnType,
      List status, int pageSize, int pageNum) throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    String sortField = null;
    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES3.length)) {
      sortField = QUERY_FIELD_NAMES3[sortOrder];
    }

    SearchQueryBuilder qb = new SearchQueryBuilder(con, DEBIT_EXT_QUERY_FIELDS,
        DEBIT_EXT_QUERY_COUNT, DEBIT_EXT_QUERY_TABLES, null, sortField, sortReverse, pageSize,
        pageNum);

    qb.addFilter(qb.STRING, "PDN.SUPPLIER_ID", "IN", suppId);
    qb.addFilter(qb.DATE, "date_trunc('day', DEBIT_NOTE_DATE)", ">=", fromDate);
    qb.addFilter(qb.DATE, "date_trunc('day', DEBIT_NOTE_DATE)", "<=", toDate);
    qb.addFilter(qb.STRING, "RETURN_TYPE", "IN", returnType);
    qb.addFilter(qb.STRING, "PDN.STATUS", "IN", status);
    qb.addFilter(qb.STRING, "DEBIT_NOTE_NO", "ilike", debitNote);
    try {
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }

  }

  /** The Constant EDIT_DEBIT. */
  private static final String EDIT_DEBIT = "select medicine_name,png.mrp,(0-billed_qty) as"
      + " returnedqty, cost_price as package_cp,(0-discount) as discount,png.tax_rate,"
      + " (0-png.tax) as tax,adj_mrp as package_sp,"
      + " png.exp_dt as date,png.tax_type,(((select sum(billed_qty+bonus_qty)"
      + " from store_grn_main join store_grn_details png using(grn_no)"
      + " join store_stock_details pmsd on dept_id=store_id and pmsd.medicine_id="
      + " png.medicine_id and png.batch_no=pmsd.batch_no where store_id=? and supplier_id=? and "
      + " invoice_no=? and png.medicine_id=pmsd.medicine_id and png.batch_no=pmsd.batch_no)"
      + " -(select coalesce(sum(psr.qty-replaced_qty),0) from store_supplier_returns_main psrm"
      + " join store_supplier_returns psr using(return_no)"
      + " join store_stock_details pmsd on dept_id=store_id and pmsd.medicine_id=png.medicine_id "
      + " and png.batch_no=pmsd.batch_no"
      + " where store_id=? and supplier_id=? and invoice_no=? and "
      + " psr.medicine_id=pmsd.medicine_id and psr.batch_no=pmsd.batch_no)) ) as invoiceqty"
      + " ,mm.manf_mnemonic,pmd.issue_base_unit,round(pmsd.qty/pmd.issue_base_unit,2) as stockqty,"
      + " 0 as taxamount,png.medicine_id,png.batch_no"
      + " from store_grn_main pngm join store_grn_details png using(grn_no)"
      + " join store_item_details pmd using(medicine_id) "
      + " join manf_master mm on pmd.manf_name=manf_code join "
      + " store_stock_details pmsd on dept_id=pngm.store_id and pmsd.medicine_id=png.medicine_id"
      + " and pmsd.batch_no=png.batch_no where debit_note_no=?";

  /**
   * Gets the med det of sel debit.
   *
   * @param suppId
   *          the supp id
   * @param invno
   *          the invno
   * @param storeId
   *          the store id
   * @param debitNo
   *          the debit no
   * @return the med det of sel debit
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getMedDetOfSelDebit(String suppId, String invno, String storeId,
      String debitNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(EDIT_DEBIT);
      ps.setString(1, storeId);
      ps.setString(2, suppId);
      ps.setString(3, invno);
      ps.setString(4, storeId);
      ps.setString(5, suppId);
      ps.setString(6, invno);
      ps.setString(7, debitNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DEBIT_DET. */
  private static final String GET_DEBIT_DET = "select pdn.*,supplier_name,sm.supplier_code ,"
      + " cust_supplier_code, case when pdn.discount_type='P' then pdn.discount_per else"
      + " pdn.discount end as discamt, TO_CHAR(consignment_date,'DD-MM-YYYY') as"
      + " consignment_date_fmt from store_debit_note pdn  "
      + " join supplier_master sm on supplier_code=pdn.supplier_id " + " where debit_note_no=?";

  /**
   * Gets the debit details.
   *
   * @param debitNo
   *          the debit no
   * @return the debit details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getDebitDetails(String debitNo) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_DEBIT_DET);
      ps.setString(1, debitNo);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MED_RETURNANDREPLACE_DETAILS. */
  private static final String GET_MED_RETURNANDREPLACE_DETAILS = "select supplier_name,"
      + " cust_supplier_code,supplier_address,contact_person_name,contact_person_mobile_number,"
      + " pmsd.exp_dt as exp," + " pmsd.batch_no,coalesce(pmsr.date_time,null) as grn_date,"
      + " case when pmsr.orig_return_no is null then psr.qty  else (0-psr.qty)end as qty"
      + " ,0 as rate,0 as disc,0 as vat,medicine_name"
      + " ,coalesce(psr.return_no::text,'') as grn_no"
      + " ,coalesce(pmsr.supplier_id,'') as supplier_id,"
      + " case when pmsr.orig_return_no is null then 'R' else 'X' end as type,pmsd.medicine_id,"
      + " pmsd.dept_id as store,0 as bonus_qty, 0 as mrp" + " from store_stock_details pmsd"
      + " join store_supplier_returns psr on  psr.medicine_id=pmsd.medicine_id and"
      + " psr.batch_no=pmsd.batch_no join store_item_details pmd on pmsd.medicine_id="
      + " pmd.medicine_id join store_supplier_returns_main pmsr on pmsr.return_no=psr.return_no"
      + " and pmsd.dept_id=store_id join supplier_master on supplier_code=pmsr.supplier_id"
      + " where pmsd.dept_id=?";

  /** The Constant GET_MED_PURCHASE_DETAILS. */
  private static final String GET_MED_PURCHASE_DETAILS = " union all"
      + " select supplier_name,cust_supplier_code,supplier_address,contact_person_name,"
      + " contact_person_mobile_number,exp_dt as exp,batch_no,grn_date,"
      + " billed_qty as qty,cost_price as rate ,png.discount as disc,tax as vat,medicine_name,"
      + " case when pngm.debit_note_no is null then grn_no else pngm.debit_note_no end as grn_no,"
      + " coalesce(pi.supplier_id,pdn.supplier_id) as supplier_id,case when pngm.debit_note_no is"
      + " null then 'P' else 'D' end as type,png.medicine_id,"
      + " pngm.store_id as store,bonus_qty, mrp" + " from store_grn_main pngm"
      + " join store_grn_details png using(grn_no)"
      + " join store_item_details pmd using(medicine_id)"
      + " left join store_invoice pi using(supplier_invoice_id)"
      + " left join store_debit_note pdn on  pdn.debit_note_no=pngm.debit_note_no"
      + " join supplier_master on supplier_code=pi.supplier_id or supplier_code=pdn.supplier_id"
      + " where pngm.store_id=?";

  /**
   * Gets the medicine details array size.
   *
   * @param storeId
   *          the store id
   * @param medId
   *          the med id
   * @param batchNo
   *          the batch no
   * @param genName
   *          the gen name
   * @return the medicine details array size
   * @throws SQLException
   *           the SQL exception
   */
  public static int getMedicineDetailsArraySize(String storeId, String medId, String batchNo,
      String genName) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    int medListsSize = 0;
    List<Object> params = new ArrayList<Object>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String query = GET_MED_RETURNANDREPLACE_DETAILS;
      params.add(storeId);
      if (!medId.equals("")) {
        query = query + " and  pmsd.medicine_id= ?";
        params.add(medId);
      }
      if (!genName.equals("")) {
        query = query + "and pmd.generic_name= ?";
        params.add(genName);
      }
      if (!batchNo.equals("")) {
        String tempBatch = null;
        if ((batchNo).charAt(0) == '%' || (batchNo).charAt((batchNo).length() - 1) == '%') {
          tempBatch = batchNo;
        } else {
          tempBatch = "%" + batchNo + "%";
        }
        query = query + " and  pmsd.batch_no ilike ? ";
        params.add(tempBatch);
      }
      query = query + GET_MED_PURCHASE_DETAILS;
      params.add(storeId);
      if (!medId.equals("")) {
        query = query + " and  png.medicine_id= ?";
        params.add(medId);
      }
      if (!genName.equals("")) {
        query = query + "and pmd.generic_name= ?";
        params.add(genName);
      }
      if (!batchNo.equals("")) {
        String tempBatch = null;
        if ((batchNo).charAt(0) == '%' || (batchNo).charAt((batchNo).length() - 1) == '%') {
          tempBatch = batchNo;
        } else {
          tempBatch = "%" + batchNo + "%";
        }
        query = query + " and  png.batch_no ilike ?";
        params.add(tempBatch);
      }
      ps = con.prepareStatement(query);
      ListIterator<Object> paramsIterator = params.listIterator();
      while (paramsIterator.hasNext()) {
        Object param = paramsIterator.next();
        int idx = paramsIterator.nextIndex();
        ps.setObject(idx, param);
      }
      ArrayList medicineList = DataBaseUtil.queryToArrayList(ps);
      medListsSize = medicineList.size() / 15;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return medListsSize;
  }

  /**
   * Gets the purchase med list.
   *
   * @param storeId
   *          the store id
   * @param medId
   *          the med id
   * @param batchNo
   *          the batch no
   * @param genName
   *          the gen name
   * @param offsetNum
   *          the offset num
   * @return the purchase med list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getPurchaseMedList(String storeId, String medId, String batchNo,
      String genName, int offsetNum) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      List<Object> params = new ArrayList<Object>();
      String query = GET_MED_RETURNANDREPLACE_DETAILS;
      params.add(storeId);
      if (!medId.equals("")) {
        query = query + " and  pmsd.medicine_id= ?";
        params.add(medId);
      }
      if (!genName.equals("")) {
        query = query + "and pmd.generic_name= ?";
        params.add(genName);
      }
      if (!batchNo.equals("")) {
        String tempBatch = null;
        if ((batchNo).charAt(0) == '%' || (batchNo).charAt((batchNo).length() - 1) == '%') {
          tempBatch = batchNo;
        } else {
          tempBatch = "%" + batchNo + "%";
        }
        query = query + " and  pmsd.batch_no ilike ?";
        params.add(tempBatch);
      }
      query = query + GET_MED_PURCHASE_DETAILS;
      params.add(storeId);
      if (!medId.equals("")) {
        query = query + " and  png.medicine_id= ?";
        params.add(medId);
      }
      if (!genName.equals("")) {
        query = query + "and pmd.generic_name= ?";
        params.add(genName);
      }
      if (!batchNo.equals("")) {
        String tempBatch = null;
        if ((batchNo).charAt(0) == '%' || (batchNo).charAt((batchNo).length() - 1) == '%') {
          tempBatch = batchNo;
        } else {
          tempBatch = "%" + batchNo + "%";
        }
        query = query + " and  png.batch_no ilike ?";
        params.add(tempBatch);
      }
      ps = con.prepareStatement(query + "ORDER BY grn_date DESC LIMIT 15 OFFSET ?");
      params.add(offsetNum);
      ListIterator<Object> paramsIterator = params.listIterator();
      while (paramsIterator.hasNext()) {
        Object param = paramsIterator.next();
        int idx = paramsIterator.nextIndex();
        ps.setObject(idx, param);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the page list.
   *
   * @param dataList
   *          the data list
   * @param totalCount
   *          the total count
   * @param pageSize
   *          the page size
   * @param pageNum
   *          the page num
   * @return the page list
   */
  public static PagedList getPageList(List dataList, int totalCount, int pageSize, int pageNum) {
    return new PagedList(dataList, totalCount, pageSize, pageNum);
  }

  /** The Constant DEBIT_EDIT. */
  private static final String DEBIT_EDIT = "SELECT stock.qty AS store_stock, "
      + "COALESCE(sgd.exp_dt, sibd.exp_dt) AS exp_dt,sgd.orig_debit_rate, "
      + "sgd.cost_price, sgd.orig_discount, sgd.discount, sgd.tax as debit_tax, sgd.orig_tax, "
      + "pmd.issue_base_unit, "
      + "CASE WHEN s.grn_qty_unit = 'P' THEN pmd.package_uom ELSE pmd.issue_units END AS"
      + " user_uom,  *,(CASE WHEN (0-sgd.item_ced) > 0 THEN"
      + " round((0-sgd.item_ced)/(CASE WHEN  s.grn_qty_unit = 'P' THEN"
      + " (0-sgd.billed_qty)/pmd.issue_base_unit ELSE (0-sgd.billed_qty) END),2) ELSE 0 END )"
      + " as item_ced_amt FROM store_grn_main s JOIN store_grn_details sgd USING(grn_no) "
      + " JOIN ( SELECT min(item_ced_amt) as in_item_ced_amt,sum(qty) as qty,item_batch_id,"
      + " dept_id FROM store_stock_details ssd GROUP BY item_batch_id,dept_id ) as stock ON  "
      + " stock.item_batch_id = sgd.item_batch_id AND stock.dept_id = s.store_id "
      + " JOIN store_item_batch_details sibd ON(stock.item_batch_id = sibd.item_batch_id)"
      + " JOIN store_item_details pmd on sgd.medicine_id = pmd.medicine_id "
      + "   LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id AND"
      + " hict.health_authority=?)  LEFT JOIN store_item_codes sico ON (sico.medicine_id ="
      + " pmd.medicine_id AND sico.code_type = hict.code_type) "
      + " JOIN store_category_master scm on med_category_id=category_id "
      + " JOIN manf_master on manf_code=pmd.manf_name WHERE debit_note_no=?";

  /**
   * Gets the med list of sel debit.
   *
   * @param debitNo
   *          the debit no
   * @param healthAuthority
   *          the health authority
   * @return the med list of sel debit
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getMedListOfSelDebit(String debitNo, String healthAuthority)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(DEBIT_EDIT);
      ps.setString(1, healthAuthority);
      ps.setString(2, debitNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update debit note details.
   *
   * @param con
   *          the con
   * @param item
   *          the item
   * @param query
   *          the query
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateDebitNoteDetails(Connection con, Map<String, Object> item,
      String query) throws SQLException {
    PreparedStatement ps;
    ps = con.prepareStatement(query);
    for (Map.Entry e : (Collection<Map.Entry<String, Object>>) item.entrySet()) {
      ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
    }
    int count = ps.executeUpdate();
    if (ps != null) {
      ps.close();
    }
    return count > 0;

  }

  /** The Constant SUPPLIER_RETURNS_DEBITS. */
  private static final String SUPPLIER_RETURNS_DEBITS = " SELECT supplier_id, sm.supplier_name,"
      + " sm.cust_supplier_code, sdn.debit_note_no, sdn.debit_note_date, remarks, "
      + " TRUNC(SUM((((sgd.billed_qty/sgd.grn_pkg_size) * sgd.cost_price) - sgd.discount)), 2)"
      + " AS amt, SUM(sgd.tax) as tax_amt, SUM(sgd.item_ced) as ced_tax, sgd.tax_rate,"
      + " sdn.discount_type, sdn.discount_per, sdn.discount, "
      + " sdn.round_off, coalesce(sdn.other_charges, 0) as other_charges, sdn.tax_name, "
      + " (case when sdn.tax_name = 'VAT' then s.purchases_store_vat_account_prefix "
      + " else s.purchases_store_cst_account_prefix end) as purchase_store_account_prefix, "
      + " scm.purchases_cat_vat_account_prefix, scm.purchases_cat_cst_account_prefix, "
      + " hcm.center_code, s.center_id " + " FROM store_debit_note sdn  "
      + " JOIN store_grn_main sgm ON (sgm.debit_note_no = sdn.debit_note_no) "
      + " JOIN store_grn_details sgd ON (sgd.grn_no=sgm.grn_no) "
      + " JOIN store_item_details sid ON (sid.medicine_id=sgd.medicine_id) "
      + " JOIN store_category_master scm ON (sid.med_category_id=scm.category_id) "
      + " JOIN stores s ON (sgm.store_id=s.dept_id) "
      + " JOIN supplier_master sm ON (supplier_code = supplier_id) "
      + " JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id) "
      + " WHERE sdn.status='C' ";

  /** The Constant SUPPLIER_RETURNS_DEBITS_GROUP. */
  private static final String SUPPLIER_RETURNS_DEBITS_GROUP = " GROUP BY supplier_id,"
      + " sdn.debit_note_no, sgd.tax_rate, sdn.debit_note_date, sm.supplier_name,"
      + " sm.cust_supplier_code, sgm.store_id, scm.category_id, (case when sdn.tax_name = 'VAT'"
      + " then s.purchases_store_vat_account_prefix else"
      + " s.purchases_store_cst_account_prefix end), "
      + " scm.purchases_cat_vat_account_prefix, scm.purchases_cat_cst_account_prefix, "
      + " remarks, sdn.discount_type, sdn.discount, sdn.other_charges, sdn.discount_per,"
      + " sdn.round_off, sdn.tax_name, hcm.center_code, s.center_id ";

  /**
   * Gets the returns with debit.
   *
   * @param con
   *          the con
   * @param fromDate
   *          the from date
   * @param toDate
   *          the to date
   * @param accountGroup
   *          the account group
   * @param centerId
   *          the center id
   * @param debitNoteNos
   *          the debit note nos
   * @return the returns with debit
   * @throws SQLException
   *           the SQL exception
   */
  public static List getReturnsWithDebit(Connection con, java.sql.Timestamp fromDate,
      java.sql.Timestamp toDate, Integer accountGroup, int centerId, List debitNoteNos)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      StringBuilder where = new StringBuilder();
      if (fromDate != null && toDate != null) {
        where.append(" AND date_time BETWEEN ? AND ? AND sdn.account_group=? ");
        if (centerId != 0) {
          where.append(" AND s.center_id=? ");
        }
      } else {
        if (debitNoteNos == null || debitNoteNos.isEmpty()) {
          return Collections.EMPTY_LIST;
        }
        DataBaseUtil.addWhereFieldInList(where, "sdn.debit_note_no", debitNoteNos, true);
      }
      ps = con.prepareStatement(SUPPLIER_RETURNS_DEBITS + where.toString()
          + SUPPLIER_RETURNS_DEBITS_GROUP + " ORDER BY sdn.debit_note_no, sgd.tax_rate");

      if (fromDate != null && toDate != null) {
        ps.setTimestamp(1, fromDate);
        ps.setTimestamp(2, toDate);
        ps.setInt(3, accountGroup);
        if (centerId != 0) {
          ps.setInt(4, centerId);
        }
      } else {
        Iterator it = debitNoteNos.iterator();
        int i1 = 1;
        while (it.hasNext()) {
          ps.setString(i1++, (String) ((Map) it.next()).get("voucher_no"));
        }
      }

      return DataBaseUtil.queryToDynaListDates(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Check qty available.
   *
   * @param con
   *          the con
   * @param storeId
   *          the store id
   * @param medicineId
   *          the medicine id
   * @param itemBatchId
   *          the item batch id
   * @param returnqty
   *          the returnqty
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean checkQtyAvailable(Connection con, String storeId, String medicineId,
      String itemBatchId, BigDecimal returnqty) throws SQLException {
    boolean qtyAvailable = false;
    PreparedStatement ps = null;
    int medicineIdNum = -1;
    int storeIdNum = -1;
    ps = con.prepareStatement(
        "select sum(qty) from store_stock_details where medicine_id =? and item_batch_id=?"
            + " and dept_id=? and qty>0 and qty >=?");
    if ((storeId != null) && (storeId.trim().length() > 0)) {
      storeIdNum = Integer.parseInt(storeId);
    }
    if ((medicineId != null) && (medicineId.trim().length() > 0)) {
      medicineIdNum = Integer.parseInt(medicineId);
    }
    if (medicineIdNum > 0) {
      ps.setInt(1, medicineIdNum);
    }
    ps.setInt(2, Integer.parseInt(itemBatchId));

    ps.setInt(3, storeIdNum);

    ps.setBigDecimal(4, returnqty);

    ResultSet rs = null;
    rs = ps.executeQuery();

    if (rs.next()) {
      qtyAvailable = true;
    }

    return qtyAvailable;

  }

  /** The Constant GET_RETURN_DET. */
  private static final String GET_RETURN_DET = "SELECT RETURN_NO,S.SUPPLIER_NAME,"
      + " S.CUST_SUPPLIER_CODE,"
      + " TO_CHAR(ISRM.DATE_TIME,'DD-MM-YYYY ') AS RETURNDATE,RETURN_NO,dept_NAME,"
      + " case when RETURN_TYPE='D' then 'Damage' when RETURN_TYPE='E' then 'Expiry' "
      + " when RETURN_TYPE='N' then 'Non-Moving' else 'Others' end as return_type,"
      + " case when isrm.status='O' then 'Open' when isrm.status='P' then 'Paritally Received' "
      + " when isrm.status='R' then 'Received' else 'Closed ' end as status"
      + " FROM store_supplier_returns_main ISRM JOIN SUPPLIER_MASTER S"
      + " ON SUPPLIER_CODE=SUPPLIER_ID JOIN stores on dept_id=store_id WHERE RETURN_NO=?";

  /**
   * Gets the return item det.
   *
   * @param retNo
   *          the ret no
   * @return the return item det
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getReturnItemDet(int retNo) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RETURN_DET);
      ps.setInt(1, retNo);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_MEDICINE_STOCK_QUERY. */
  /*
   * Returns the available stock for a given medicine ID in all departments
   */
  private static final String GET_MEDICINE_STOCK_QUERY = "SELECT msd.medicine_id, msd.batch_no,"
      + " msd.qty,msd.exp_dt, msd.mrp, msd.tax, msd.stock_time, package_sp, tax_rate, "
      + " package_cp, tax_type, msd.received_date, msd.dept_id, COALESCE(isld.bin,m.bin) as bin,"
      + " m.medicine_name, msd.item_ced_amt, mf.manf_code, mf.manf_name, mf.manf_mnemonic,"
      + " m.issue_base_unit,  m.package_type, COALESCE(m.issue_units, '') AS issue_units,"
      + " COALESCE(mc.discount,0) AS meddisc, mc.category as category_name,sic.control_type_name,"
      + " CASE WHEN char_length(g.generic_name) >= 21 THEN substr(g.generic_name,0,20) ELSE"
      + " g.generic_name END as generic_name,  pst.qty_rejected as qty_in_transit, si.indent_no "
      + " FROM store_stock_details msd JOIN store_item_details m USING(medicine_id) "
      + " LEFT OUTER JOIN store_stock_transfer_view pst on msd.medicine_id = pst.medicine_id and"
      + " msd.batch_no = pst.batch_no and msd.dept_id = pst.store_from  "
      + " LEFT OUTER JOIN store_indent_details si on si.indent_no = pst.indent_no and"
      + " si.medicine_id = pst.medicine_id "
      + " JOIN manf_master mf ON (mf.manf_code = m.manf_name) "
      + " JOIN store_category_master mc ON mc.category_id = m.med_category_id "
      + " LEFT OUTER JOIN generic_name g ON m.generic_name = g.generic_code "
      + " LEFT JOIN item_store_level_details  isld ON isld.medicine_id=msd.medicine_id AND"
      + " isld.dept_id = msd.dept_id  LEFT JOIN store_item_controltype sic ON"
      + " (sic.control_type_id = m.control_type_id) ";

  /**
   * Gets the group med details for rej indents.
   *
   * @param saleType
   *          the sale type
   * @param deptId
   *          the dept id
   * @param medIds
   *          the med ids
   * @param batchNos
   *          the batch nos
   * @param qty
   *          the qty
   * @param indentno
   *          the indentno
   * @return the group med details for rej indents
   * @throws SQLException
   *           the SQL exception
   */
  public static List getGroupMedDetailsForRejIndents(String saleType, String deptId, List medIds,
      List batchNos, List qty, List indentno) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      int deptIdNum = -1;

      deptIdNum = Integer.parseInt(deptId);

      StringBuilder where = new StringBuilder();
      DataBaseUtil.addWhereFieldInList(where, "msd.batch_no", batchNos);
      DataBaseUtil.addWhereFieldInList(where, "msd.medicine_id", medIds);
      DataBaseUtil.addWhereFieldInList(where, "si.indent_no", indentno);
      // [b12.5, b14.5, b44]
      // [MD21, MD22, MD19]

      StringBuilder query = new StringBuilder(GET_MEDICINE_STOCK_QUERY);
      query.append(where);
      if (saleType.equalsIgnoreCase("D")) {
        query.append(" AND dept_id=? AND pst.qty_rejected>0 ");
      } else {
        query.append(" AND dept_id=?  ");
      }

      ps = con.prepareStatement(query.toString());

      int i1 = 1;

      if (batchNos != null) {
        Iterator it = batchNos.iterator();
        while (it.hasNext()) {
          ps.setString(i1, (String) it.next());
          i1++;
        }
      }
      if (medIds != null) {
        Iterator it = medIds.iterator();
        while (it.hasNext()) {
          int medIdNum = -1;
          String medId = (String) it.next();
          if ((medId != null) && (medId.trim().length() > 0)) {
            medIdNum = Integer.parseInt(medId);
          }
          if (medIdNum > 0) {
            ps.setInt(i1, medIdNum);
          }
          i1++;
        }
      }
      if (indentno != null) {
        Iterator it = indentno.iterator();
        while (it.hasNext()) {
          int indentNum = Integer.parseInt((String) it.next());
          ps.setInt(i1, indentNum);
          i1++;
        }
      }

      ps.setInt(i1, deptIdNum);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Insert replaced stock.
   *
   * @param insertList
   *          the insert list
   * @param itemBatchInsertlist
   *          the item batch insertlist
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  public static boolean insertReplacedStock(List<BasicDynaBean> insertList,
      List<BasicDynaBean> itemBatchInsertlist) throws Exception {
    boolean success = false;
    Connection con = null;
    PreparedStatement ps = null;
    GenericDAO itemBatchDAO = new GenericDAO("store_item_batch_details");
    GenericDAO storeStockDetailsDAO = new GenericDAO("store_stock_details");

    try {
      con = DataBaseUtil.getConnection();

      for (Iterator iter = insertList.iterator(); iter.hasNext();) {
        BasicDynaBean stockBean = (BasicDynaBean) iter.next();
        for (BasicDynaBean itemBatchDetails : itemBatchInsertlist) {
          if (((Integer) itemBatchDetails.get("item_batch_id"))
              .intValue() == ((Integer) stockBean.get("item_batch_id")).intValue()) {
            itemBatchDAO.insert(con, itemBatchDetails);
          }
        }
        success = storeStockDetailsDAO.insert(con, stockBean);
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return success;

  }

  /** The Constant GET_SUP_RETURN_ITEMS. */
  public static final String GET_SUP_RETURN_ITEMS = " select srm.return_no, user_name,"
      + " srm.date_time::date as return_date, dept_name,orig_return_no, "
      + " medicine_name,m.cust_item_code, sibd.batch_no, sr.qty as qty, srm.remarks,"
      + " 0 as qty_received,  case when return_type='O' then 'Others' "
      + " when return_type='N' then 'Non Moving' " + " when return_type='E' then 'Expiry'  "
      + " when return_type='D' then 'Damage'  "
      + " END as return_type,sm.supplier_name,srm.gatepass_id, "
      + " to_char(sibd.exp_dt,'MON-YYYY') AS exp_date,mm.manf_name, "
      + " round((sr.qty * (sibd.mrp/m.issue_base_unit)), 2) as value_against_return_qty, "
      + " '' as invoice_no, '' as invoice_date, " + " CASE "
      + "   when srm.ret_qty_unit = 'P' then 'Package Units' "
      + "   when srm.ret_qty_unit = 'I' then 'Issue Units' "
      + " END as qty_unit, sm.cust_supplier_code, sm.drug_license_no, sm.pan_no, sm.cin_no,"
      + " sr.cost_value as amt from store_supplier_returns_main srm "
      + " join store_supplier_returns sr on sr.return_no = srm.return_no "
      + " left join store_item_details m on m.medicine_id = sr.medicine_id "
      + " left join stores s on s.dept_id = srm.store_id  "
      + " JOIN store_item_batch_details sibd ON (sibd.item_batch_id = sr.item_batch_id)"
      + " join manf_master mm on mm.manf_code=m.manf_name "
      + " left join supplier_master sm on sm.supplier_code = srm.supplier_id "
      + " where srm.return_no= ? ";

  /**
   * Gets the supp return item list.
   *
   * @param returnNo
   *          the return no
   * @return the supp return item list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getSuppReturnItemList(String returnNo) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SUP_RETURN_ITEMS);
      ps.setInt(1, Integer.parseInt(returnNo));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SUPP_DEBIT_ITEMS. */
  public static final String GET_SUPP_DEBIT_ITEMS = "select sdn.debit_note_no, user_name,"
      + " debit_note_date, dept_name, medicine_name,m.cust_item_code, sgd.batch_no, "
      + " sdn.date_time as debit_note_datetime, grn_qty_unit, "
      + " CASE WHEN grn_qty_unit='P' then round(billed_qty/sgd.grn_pkg_size,2) "
      + " WHEN grn_qty_unit='I' then billed_qty END as qty, "
      + " CASE WHEN grn_qty_unit='P' then round(bonus_qty/sgd.grn_pkg_size,2) "
      + " WHEN grn_qty_unit='I' then bonus_qty END as bonus_qty, "
      + " sdn.remarks, sdn.gatepass_id, " + " case when return_type='O' then 'Others' "
      + " when return_type='N' then 'Non Moving' " + " when return_type='E' then 'Expiry' "
      + " when return_type='D' then 'Damage' "
      + " END as return_type, sm.supplier_name, sm.cust_supplier_code, sm.drug_license_no,"
      + " sm.pan_no, sm.cin_no, sdn.remarks, COALESCE(sm.supplier_tin_no,'') AS supplier_tin,"
      + " s.pharmacy_tin_no, hcms.tin_number, CASE WHEN sdn.discount_type = 'P' "
      + " then round(( select sdn1.discount_per * (0-sum((gd1.billed_qty/gd1.grn_pkg_size)"
      + " *gd1.cost_price-gd1.discount+gd1.tax+(gd1.item_ced)))/100 "
      + " from store_debit_note sdn1 join store_grn_main gm1 using (debit_note_no) "
      + " join store_grn_details gd1 using (grn_no) "
      + " group by sdn1.debit_note_no,sdn1.discount_per "
      + " having sdn1.debit_note_no = sdn.debit_note_no) ,gp.after_decimal_digits) "
      + " ELSE sdn.discount END as rev_discount , " + " CASE WHEN sdn.discount_type = 'P' "
      + " then round(( select sdn2.discount_per * (0-sum((png1.billed_qty/png1.grn_pkg_size)*"
      + " png1.orig_debit_rate-(png1.orig_discount)+png1.orig_tax+(png1.item_ced)))/100 "
      + " from store_debit_note sdn2 join store_grn_main gm2 using(debit_note_no) "
      + " join store_grn_details png1 using (grn_no) "
      + " group by sdn2.debit_note_no, sdn2.discount_per having sdn2.debit_note_no ="
      + " sdn.debit_note_no) ,gp.after_decimal_digits)  ELSE sdn.discount END as dbt_discount, "
      + " CASE WHEN sdn.discount_type = 'P' "
      + " then round(( select sdn1.discount_per * (0-sum((gd1.billed_qty/gd1.grn_pkg_size)*"
      + " gd1.cost_price-gd1.scheme_discount+gd1.tax+(gd1.item_ced)))/100 "
      + " from store_debit_note sdn1 join store_grn_main gm1 using (debit_note_no) "
      + " join store_grn_details gd1 using (grn_no) "
      + " group by sdn1.debit_note_no,sdn1.discount_per "
      + " having sdn1.debit_note_no = sdn.debit_note_no) ,gp.after_decimal_digits) "
      + " ELSE sdn.discount END as rev_scheme_discount , " + " CASE WHEN sdn.discount_type = 'P' "
      + " then round(( select sdn2.discount_per * (0-sum((png1.billed_qty/png1.grn_pkg_size)*"
      + " png1.orig_debit_rate-(png1.orig_scheme_discount)+png1.orig_tax+(png1.item_ced)))/100 "
      + " from store_debit_note sdn2 join store_grn_main gm2 using(debit_note_no) "
      + " join store_grn_details png1 using (grn_no) "
      + " group by sdn2.debit_note_no, sdn2.discount_per having sdn2.debit_note_no ="
      + " sdn.debit_note_no) ,gp.after_decimal_digits) " + " ELSE sdn.discount END as dbt_scheme_discount, "
      + " sinv.invoice_no, "
      + " sinv.invoice_date, CASE WHEN sgm.grn_qty_unit='P' then 'Package' WHEN"
      + " sgm.grn_qty_unit='I' then 'ISSUE' END as qty_unit, "
      + " sdn.round_off, sdn.other_charges,sdn.other_charges_remarks,sdn.remarks,mm.manf_mnemonic,"
      + " mm.manf_name, TO_CHAR(sgd.EXP_DT,'MON-YYYY') AS EXP_DATE,"
      + " round((0-((sgd.billed_qty/sgd.grn_pkg_size)*sgd.orig_debit_rate)),gp.after_decimal_digits) as amt, "
      + " round((0-(sgd.billed_qty/sgd.grn_pkg_size)),gp.after_decimal_digits) as qty, sgd.cost_price,"
      + " (0-sgd.orig_discount) as itemdiscount,(0-sgd.orig_scheme_discount) as"
      + " itemschemediscount, sgd.tax_rate, (0-sgd.orig_tax) as tax, "
      + " round((0-((sgd.billed_qty/sgd.grn_pkg_size)*sgd.orig_debit_rate-(sgd.orig_discount+"
      + " sgd.orig_scheme_discount)+sgd.orig_tax+sgd.item_ced)),gp.after_decimal_digits) as totamt, "
      + " round((0-((sgd.billed_qty/sgd.grn_pkg_size)*sgd.cost_price-(sgd.discount+"
      + " sgd.scheme_discount)+sgd.tax+(sgd.item_ced))),gp.after_decimal_digits) as totrecamt "
      + " from store_debit_note sdn "
      + " left join store_grn_main sgm on sgm.debit_note_no = sdn.debit_note_no "
      + " left join store_grn_details sgd on sgd.grn_no = sgm.grn_no "
      + " left join store_item_details m on m.medicine_id = sgd.medicine_id "
      + " left join manf_master mm on mm.manf_code=m.manf_name "
      + " left join store_invoice sinv on (sinv.supplier_invoice_id = sgm.supplier_invoice_id) "
      + " left join stores s on s.dept_id = sdn.store_id "
      + " LEFT JOIN hospital_center_master hcms ON (hcms.center_id=s.center_id) "
      + " left join supplier_master sm on sm.supplier_code = sdn.supplier_id "
      + " JOIN generic_preferences gp ON true"
      + " where sdn.debit_note_no=?";

  /**
   * Gets the supp debit item list.
   *
   * @param debitNo
   *          the debit no
   * @return the supp debit item list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getSuppDebitItemList(String debitNo) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SUPP_DEBIT_ITEMS);
      ps.setString(1, debitNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SUPP_ITEMS. */
  public static final String GET_SUPP_ITEMS = "select distinct medicine_name,smd.medicine_id,"
      + " smd.item_barcode_id,smd.cust_item_code, CASE WHEN smd.cust_item_code IS NOT NULL AND"
      + "  TRIM(smd.cust_item_code) != ''  THEN smd.medicine_name||' - '||smd.cust_item_code ELSE"
      + " smd.medicine_name END as cust_item_code_with_name FROM "
      + " ( SELECT item_supplier_code,medicine_id,sum(qty) as qty,item_batch_id,"
      + " batch_no,sum(qty_in_use) as qty_in_use,sum(qty_maint) as qty_maint, "
      + " sum(qty_retired) as qty_retired,sum(qty_lost) as qty_lost,sum(qty_kit) as qty_kit, "
      + " sum(qty_unknown) as qty_unknown,consignment_stock,asset_approved,dept_id "
      + " FROM store_stock_details  WHERE consignment_stock = false "
      + " GROUP BY item_supplier_code,batch_no,item_batch_id,medicine_id,consignment_stock,"
      + " asset_approved,dept_id ORDER BY medicine_id ) as msd JOIN store_item_details smd"
      + " USING(medicine_id) where item_supplier_code=? and dept_id=? and qty > 0"
      + " order by medicine_name;";

  /**
   * Gets the supp med.
   *
   * @param suppId
   *          the supp id
   * @param storeId
   *          the store id
   * @return the supp med
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getSuppMed(String suppId, String storeId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_SUPP_ITEMS,
        new Object[] { suppId, Integer.parseInt(storeId) });
  }

  /** The Constant IS_SERIAL_BATCH_PRESENT. */
  public static final String IS_SERIAL_BATCH_PRESENT = " SELECT * FROM "
      + " store_item_details sid "
      + " JOIN store_category_master scm  ON (scm.category_id = med_category_id) "
      + " JOIN store_stock_details ssd ON (ssd.medicine_id = sid.medicine_id "
      + "  AND ssd.medicine_id = ? " + "  ) " + " WHERE identification = 'S' AND batch_no = ? ";

  /**
   * Checks if is serial batch no already present.
   *
   * @param medicineId
   *          the medicine id
   * @param batch
   *          the batch
   * @return true, if is serial batch no already present
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean isSerialBatchNoAlreadyPresent(int medicineId, String batch)
      throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(IS_SERIAL_BATCH_PRESENT);
      ps.setInt(1, medicineId);
      ps.setString(2, batch);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return true;
      } else {
        return false;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the stock row.
   *
   * @param con
   *          the con
   * @param store
   *          the store
   * @param medId
   *          the med id
   * @param batch
   *          the batch
   * @return the stock row
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getStockRow(Connection con, int store, int medId, String batch)
      throws SQLException {

    PreparedStatement ps = null;
    String grnbquery = "select * from store_stock_details where dept_id=? and medicine_id=?"
        + " and batch_no=?";
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(grnbquery);
      ps.setInt(1, store);
      ps.setInt(2, medId);
      ps.setString(3, batch);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return (BasicDynaBean) list.get(0);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_GRN_NO_FROM_DEBIT_NOTE. */
  private static final String GET_GRN_NO_FROM_DEBIT_NOTE = "select grn_no from store_grn_main"
      + " where debit_note_no = ? ";

  /**
   * Gets the GRN from debit note.
   *
   * @param debitNoteNo
   *          the debit note no
   * @return the GRN from debit note
   * @throws SQLException
   *           the SQL exception
   */
  public static String getGRNFromDebitNote(String debitNoteNo) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    String grnNo = "";
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String query = GET_GRN_NO_FROM_DEBIT_NOTE;
      ps = con.prepareStatement(query);
      ps.setString(1, debitNoteNo);
      grnNo = DataBaseUtil.getStringValueFromDb(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return grnNo;
  }

  /** The Constant GET_ITEM_BATCH_ID. */
  private static final String GET_ITEM_BATCH_ID = "select item_batch_id from"
      + " store_item_batch_details where batch_no = ? ";

  /**
   * Gets the batch id.
   *
   * @param batchNo
   *          the batch no
   * @return the batch id
   * @throws SQLException
   *           the SQL exception
   */
  public static int getBatchId(String batchNo) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    int itemBatchId = 0;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String query = GET_ITEM_BATCH_ID;
      ps = con.prepareStatement(query);
      ps.setString(1, batchNo);
      itemBatchId = DataBaseUtil.getIntValueFromDb(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return itemBatchId;
  }

  /** The Constant GET_TAX_DETAILS. */
  private static final String GET_TAX_DETAILS = "SELECT sgtd.medicine_id,sgtd.grn_no,"
      + " sgtd.item_batch_id,sgm.po_no, sgd.batch_no,sgd.item_order, "
      + " ig.item_group_name,isb.item_subgroup_name,sgtd.tax_rate,sgtd.tax_amt "
      + " FROM store_debit_note sdn  "
      + " LEFT JOIN store_grn_main sgm ON sgm.debit_note_no = sdn.debit_note_no "
      + " LEFT JOIN store_grn_details sgd ON sgd.grn_no = sgm.grn_no "
      + " JOIN store_grn_tax_details sgtd ON (sgtd.grn_no = sgd.grn_no AND sgtd.medicine_id"
      + " = sgd.medicine_id AND sgtd.item_batch_id = sgd.item_batch_id) "
      + " JOIN item_sub_groups isb ON isb.item_subgroup_id = sgtd.item_subgroup_id "
      + " JOIN item_groups ig ON ig.item_group_id = isb.item_group_id"
      + " WHERE sdn.debit_note_no=? ";

  /**
   * Gets the debit tax details.
   *
   * @param debitNo
   *          the debit no
   * @return the debit tax details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getDebitTaxDetails(String debitNo) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TAX_DETAILS);
      ps.setString(1, debitNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


  private static final String GET_SUPPLIER_DEBIT_NOTE_DETAILS =
      "SELECT sgm.grn_no, sdn.debit_note_no,user_name,sdn.date_time AS debit_note_datetime,"
          + "grn_qty_unit,"
          + "debit_note_date,dept_name,medicine_name,m.cust_item_code,sgd.batch_no,CASE WHEN "
          + "grn_qty_unit='P' THEN round(billed_qty/sgd.grn_pkg_size,2)WHEN grn_qty_unit='I' THEN"
          + " billed_qty END AS qty,CASE WHEN grn_qty_unit='P' THEN round(bonus_qty/sgd"
          + ".grn_pkg_size,2)WHEN grn_qty_unit='I' THEN bonus_qty END AS bonus_qty,sdn.remarks,"
          + "CASE WHEN return_type='O' THEN'Others' WHEN return_type='N' THEN'Non Moving' WHEN "
          + "return_type='E' THEN'Expiry' WHEN return_type='D' THEN'Damage' END AS return_type,sm"
          + ".supplier_name,sm.cust_supplier_code,CASE WHEN sdn.discount_type='P' THEN round("
          + "(SELECT sdn1.discount_per*(0-sum((gd1.billed_qty/gd1.grn_pkg_size)*gd1"
          + ".cost_price-gd1.discount+gd1.tax+(gd1.item_ced)))/100 FROM store_debit_note sdn1 "
          + "JOIN store_grn_main gm1 USING(debit_note_no)JOIN store_grn_details gd1 USING(grn_no)"
          + "GROUP BY sdn1.debit_note_no,sdn1.discount_per HAVING sdn1.debit_note_no=sdn"
          + ".debit_note_no),2)ELSE sdn.discount END AS rev_discount,CASE WHEN sdn"
          + ".discount_type='P' THEN round((SELECT sdn2.discount_per*(0-sum((png1.billed_qty/png1"
          + ".grn_pkg_size)*png1.orig_debit_rate-(png1.orig_discount)+png1.orig_tax+(png1"
          + ".item_ced)))/100 FROM store_debit_note sdn2 JOIN store_grn_main gm2 USING"
          + "(debit_note_no)JOIN store_grn_details png1 USING(grn_no)GROUP BY sdn2.debit_note_no,"
          + "sdn2.discount_per HAVING sdn2.debit_note_no=sdn.debit_note_no),2)ELSE sdn.discount "
          + "END AS dbt_discount,CASE WHEN sdn.discount_type='P' THEN round((SELECT sdn1"
          + ".discount_per*(0-sum((gd1.billed_qty/gd1.grn_pkg_size)*gd1.cost_price-gd1"
          + ".scheme_discount+gd1.tax+(gd1.item_ced)))/100 FROM store_debit_note sdn1 JOIN "
          + "store_grn_main gm1 USING(debit_note_no)JOIN store_grn_details gd1 USING(grn_no)GROUP"
          + " BY sdn1.debit_note_no,sdn1.discount_per HAVING sdn1.debit_note_no=sdn"
          + ".debit_note_no),2)ELSE sdn.discount END AS rev_scheme_discount,CASE WHEN sdn"
          + ".discount_type='P' THEN round((SELECT sdn2.discount_per*(0-sum((png1.billed_qty/png1"
          + ".grn_pkg_size)*png1.orig_debit_rate-(png1.orig_scheme_discount)+png1.orig_tax+(png1"
          + ".item_ced)))/100 FROM store_debit_note sdn2 JOIN store_grn_main gm2 USING"
          + "(debit_note_no)JOIN store_grn_details png1 USING(grn_no)GROUP BY sdn2.debit_note_no,"
          + "sdn2.discount_per HAVING sdn2.debit_note_no=sdn.debit_note_no),2)ELSE sdn.discount "
          + "END AS dbt_scheme_discount,sinv.invoice_no,sinv.invoice_date,sdn.round_off,sdn"
          + ".other_charges,sdn.other_charges_remarks,mm.manf_mnemonic,mm.manf_name,TO_CHAR(sgd"
          + ".EXP_DT,'MON-YYYY')AS EXP_DATE,round((0-((sgd.billed_qty/sgd.grn_pkg_size)*sgd"
          + ".orig_debit_rate)),2)AS amt,round((0-(sgd.billed_qty/sgd.grn_pkg_size)),2)AS qty,sgd"
          + ".cost_price,(0-sgd.orig_discount)AS itemdiscount,(0-sgd.orig_scheme_discount)AS "
          + "itemschemediscount,(0-sgd.orig_tax)AS tax,round((0-((sgd.billed_qty/sgd"
          + ".grn_pkg_size)*sgd.orig_debit_rate-(sgd.orig_discount+sgd.orig_scheme_discount)+sgd"
          + ".orig_tax+sgd.item_ced)),2)AS totamt,round((0-((sgd.billed_qty/sgd.grn_pkg_size)*sgd"
          + ".cost_price-(sgd.discount+sgd.scheme_discount)+sgd.tax+(sgd.item_ced))),2)AS "
          + "totrecamt,(SELECT string_agg(isg.item_subgroup_name,',')AS tax_subgroups FROM "
          + "store_grn_tax_details sgtd JOIN item_sub_groups isg ON(sgtd.item_subgroup_id=isg"
          + ".item_subgroup_id)WHERE grn_no=sgm.grn_no),(SELECT string_agg(sgtd.tax_rate::text,',"
          + "')AS tax_rate FROM store_grn_tax_details sgtd JOIN item_sub_groups isg ON(sgtd"
          + ".item_subgroup_id=isg.item_subgroup_id)WHERE grn_no=sgm.grn_no),sdn"
          + ".received_debit_amt,sdn.consignment_no,sdn.consignment_date,sdn.company_name,sgd"
          + ".grn_no||'#'||sgd.item_batch_id as return_detail_no,sdn.other_reason,sibd.mrp FROM "
          + "store_debit_note sdn LEFT JOIN store_grn_main sgm ON sgm.debit_note_no=sdn"
          + ".debit_note_no LEFT JOIN store_grn_details sgd ON sgd.grn_no=sgm.grn_no LEFT JOIN "
          + "store_item_details m ON m.medicine_id=sgd.medicine_id LEFT JOIN "
          + "store_item_batch_details sibd on m.medicine_id=sibd.medicine_id and sgd"
          + ".item_batch_id=sibd.item_batch_id LEFT JOIN manf_master mm ON mm.manf_code=m"
          + ".manf_name LEFT JOIN store_invoice sinv ON(sinv.supplier_invoice_id=sgm"
          + ".supplier_invoice_id)LEFT JOIN stores s ON s.dept_id=sdn.store_id LEFT JOIN "
          + "hospital_center_master hcms ON(hcms.center_id=s.center_id)LEFT JOIN supplier_master "
          + "sm ON sm.supplier_code=sdn.supplier_id WHERE sdn.debit_note_no=?";

  public static List<BasicDynaBean> getSupplierDebitNoteDetails(String debitNoteNo) throws SQLException{
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_SUPPLIER_DEBIT_NOTE_DETAILS);
      ps.setString(1, debitNoteNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
