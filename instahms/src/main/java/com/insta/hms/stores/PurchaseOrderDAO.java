package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PurchaseOrderDAO {

  /*
   * Constants used for sort order
   */
  public static final int FIELD_NONE = 0;
  public static final int FIELD_SUPPLIER = 1;
  public static final int FIELD_INVOICE = 2;
  public static final int FIELD_IDATE = 3;
  public static final int FIELD_DDATE = 4;

  public static final int FIELD_PONO = 1;
  public static final int FIELD_PODATE = 2;
  public static final int FIELD_SUPP = 3;
  public static final int FIELD_QUT_NO = 4;

  Connection con = null;

  public PurchaseOrderDAO(Connection con) {
    this.con = con;
  }

  public PurchaseOrderDAO() {
  }

  private static final String GET_RATES = "SELECT pmsd.medicine_id,pmsd.mrp,COALESCE(pmd.package_type,'') AS package_type, "
      + "pmsd.package_cp,m.manf_name,m.manf_code,m.manf_mnemonic,COALESCE(g.generic_name,'') AS generic_name,mc.category_id, "
      + "pmd.issue_base_unit,sic.control_type_name, SUM(pmsd.qty) AS qty, pmsd.package_sp,mc.category AS category_name, "
      + "issue_type,issue_units,value " + "FROM store_stock_details pmsd "
      + " JOIN store_item_details pmd USING(medicine_id) "
      + " JOIN manf_master m ON pmd.manf_name=m.manf_code "
      + " JOIN store_category_master mc ON pmd.med_category_id=mc.category_id "
      + " LEFT OUTER JOIN  generic_name g ON pmd.generic_name=g.generic_code "
      + " LEFT JOIN store_item_controltype sic ON sic.control_type_id = pmd.control_type_id "
      + "WHERE pmsd.dept_id=? AND pmsd.medicine_id=? "
      + "GROUP BY pmsd.medicine_id,pmsd.mrp, pmd.package_type, g.generic_name, pmsd.package_cp, "
      + "m.manf_name,m.manf_code,m.manf_mnemonic,mc.category_id, pmd.issue_base_unit,sic.control_type_name,pmsd.exp_dt, "
      + "pmsd.package_sp,mc.category ,issue_type,issue_units, pmd.value "
      + "ORDER BY pmsd.exp_dt DESC";

  public static ArrayList getSelMedicineDetails(int medId, int store) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RATES);
      ps.setInt(1, store);
      ps.setInt(2, medId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_MED_RATES = "SELECT PMD.MEDICINE_ID,COALESCE(PMD.PACKAGE_TYPE,'') AS PACKAGE_TYPE,"
      + " M.MANF_NAME,M.MANF_CODE,COALESCE(G.GENERIC_NAME,'') AS GENERIC_NAME,COALESCE(PMSD.MRP,'0') AS MRP,"
      + " COALESCE(PMSD.PACKAGE_CP,'0') AS PACKAGE_CP,0 AS QTY,MC.CATEGORY as CATEGORY_NAME,MC.CATEGORY_ID,"
      + " COALESCE(PMSD.PACKAGE_SP,'0') AS PACKAGE_SP,PMD.ISSUE_BASE_UNIT,M.MANF_MNEMONIC,sic.control_type_name,issue_type,issue_units,value"
      + " FROM store_item_details PMD LEFT OUTER JOIN store_stock_details PMSD USING (MEDICINE_ID)"
      + " JOIN MANF_MASTER M ON PMD.MANF_NAME=M.MANF_CODE"
      + " JOIN store_category_master MC ON PMD.MED_CATEGORY_ID=MC.CATEGORY_ID"
      + " LEFT OUTER JOIN  GENERIC_NAME G ON PMD.GENERIC_NAME=G.GENERIC_CODE "
      + " LEFT JOIN store_item_controltype sic ON sic.control_type_id = PMD.control_type_id "
      + " WHERE PMD.MEDICINE_ID=?";

  public static ArrayList getSelMedDetNIStock(int medId) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MED_RATES);
      ps.setInt(1, medId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String PO_SEQUENCE_PATTERN = " SELECT pattern_id FROM hosp_po_seq_prefs "
      + " WHERE priority = ( " + "  SELECT min(priority) FROM hosp_po_seq_prefs "
      + "  WHERE (store_id=? or store_id='*') " + " ) ";

  public static String getNextId(String storeId) throws SQLException {
    BasicDynaBean b = DataBaseUtil.queryToDynaBean(PO_SEQUENCE_PATTERN, storeId);
    String patternId = (String) b.get("pattern_id");
    return DataBaseUtil.getNextPatternId(patternId);
  }

  public static String getAdditionalTermsAndConditions() {
    return DataBaseUtil
        .getStringValueFromDb("SELECT hospital_terms_conditions FROM store_miscellaneous_settings");
  }

  public static ArrayList getPaymentTermsAndConditions() {
    return DataBaseUtil.queryToArrayList(
        "SELECT TEMPLATE_CODE,TEMPLATE_NAME,TERMS_CONDITIONS FROM PH_PAYMENT_TERMS WHERE STATUS='A' AND IS_DELIVERY_INSTRUCTION != 'Y' ORDER BY TEMPLATE_NAME ");
  }

  public static ArrayList getDeliveryInstructions() {
    return DataBaseUtil.queryToArrayList(
        "SELECT TEMPLATE_CODE,TEMPLATE_NAME,TERMS_CONDITIONS FROM PH_PAYMENT_TERMS WHERE STATUS='A' AND IS_DELIVERY_INSTRUCTION ='Y' ORDER BY TEMPLATE_NAME ");
  }

  private static final String INSERT_PO_MAIN = "INSERT INTO store_po_main (PO_NO,PO_DATE,QUT_NO,QUT_DATE,SUPPLIER_ID, "
      + " REFERENCE,VAT_RATE,PO_TOTAL,STATUS,SUPPLIER_TERMS,HOSPITAL_TERMS,ACTUAL_PO_DATE,USER_ID,VAT_TYPE,MRP_TYPE,STORE_ID)"
      + " VALUES(?,?,?,?,?,?,?,?,?,?,?,localtimestamp(0),?,?,?,?)";

  private static final String INSERT_PO = "INSERT INTO store_po (PO_NO,MEDICINE_ID,QTY_REQ,MRP,ADJ_MRP,COST_PRICE,VAT_RATE,VAT,DISCOUNT,MED_TOTAL)"
      + " VALUES(?,?,?,?,?,?,?,?,?,?)";

  public String insertPOMain(PO po) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(INSERT_PO_MAIN)) {
      String poNo = getNextId(po.getStore());
      ps.setString(1, poNo);
      ps.setDate(2, po.getPoDate());
      ps.setString(3, po.getQuotationNo());
      ps.setDate(4, po.getQuotationDate());
      ps.setString(5, po.getSupplier());
      ps.setString(6, po.getReference());
      ps.setBigDecimal(7, po.getVatRate());
      ps.setBigDecimal(8, po.getTotAmt());
      ps.setString(9, "O");
      ps.setString(10, po.getSuppterms());
      ps.setString(11, po.getHospterms());
      ps.setString(12, po.getUserId());
      if (po.getVatType().equalsIgnoreCase("MRP Based")) {
        ps.setString(13, "M");
      } else
        ps.setString(13, "C");
      if (po.getMrptype().equalsIgnoreCase("Inclusive of Taxes")) {
        ps.setString(14, "I");
      } else
        ps.setString(14, "E");
      ps.setString(15, po.getStore());
      int count = ps.executeUpdate();
      if (count > 0) {
        return poNo;
      }
    }

    return null;
  }

  public boolean insertPO(PO po) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(INSERT_PO)) {
      ps.setString(1, po.getPoNo());
      ps.setString(2, po.getHmedicineId());
      ps.setFloat(3, po.getHqty());
      ps.setBigDecimal(4, po.getHmrp());
      ps.setBigDecimal(5, po.getHamrp());
      ps.setBigDecimal(6, po.getHrate());
      ps.setBigDecimal(7, po.getVatRate());
      ps.setBigDecimal(8, po.getHvat());
      ps.setBigDecimal(9, po.getHdisc());
      ps.setBigDecimal(10, po.getHamt());
      int count = ps.executeUpdate();
      if (count > 0) {
        return true;
      }
    }

    return false;
  }

  private static final String GET_PO_DET = " SELECT pom.*, sm.supplier_name, "
      + " sm.tcs_applicable, sm.cust_supplier_code,transportation_charges "
      + " FROM store_po_main pom JOIN supplier_master sm ON (sm.supplier_code = pom.supplier_id) "
      + " WHERE po_no = ?";

  public static BasicDynaBean getPoDetails(String poNo) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_PO_DET, poNo);
  }

  private static final String UPDATE_PO = "UPDATE store_po_main SET STATUS=? WHERE PO_NO=?";

  public boolean closePo(List closePO) throws SQLException {
    int resultCount = 0;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PO)) {
      Iterator itr = closePO.iterator();
      ViewPO po = null;
      while (itr.hasNext()) {
        po = (ViewPO) itr.next();
        ps.setString(1, po.getStatus());
        ps.setString(2, po.getPono());
        resultCount = resultCount + ps.executeUpdate();
      }
    }
    return resultCount == closePO.size();
  }

  public boolean cancelPo(List cancelPO) throws SQLException {
    int resultCount = 0;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PO)) {
      Iterator itr = cancelPO.iterator();
      ViewPO po = null;
      while (itr.hasNext()) {
        po = (ViewPO) itr.next();
        ps.setString(1, po.getStatus());
        ps.setString(2, po.getPono());
        resultCount = resultCount + ps.executeUpdate();
      }
    }
    return resultCount == cancelPO.size();
  }

  private static final String GET_INV_DET = "SELECT pinv.invoice_no,TO_CHAR(pinv.invoice_date,'DD-MM-YYYY') AS invoice_date,"
      + "TO_CHAR(pinv.due_date,'DD-MM-YYYY') AS due_date,pinv.po_reference,pinv.po_no,pinv.supplier_id,"
      + "pinv.discount,pinv.round_off,discount_type,discount_per, cess_tax_rate, tax_name, cst_rate "
      + "FROM store_invoice pinv WHERE pinv.invoice_no=? AND supplier_id=?";

  public static StockEntry getinvDetails(String invNo, String suppId) throws SQLException {
    StockEntry sdto = null;
    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INV_DET);
      ps.setString(1, invNo);
      ps.setString(2, suppId);
      rs = ps.executeQuery();

      if (rs.next()) {
        sdto = new StockEntry();
        sdto.setPonum(rs.getString("po_no"));
        sdto.setSuppCode(rs.getString("supplier_id"));
        sdto.setInvno(rs.getString("invoice_no"));
        sdto.setInvDate(rs.getString("invoice_date"));
        sdto.setInvdueDate(rs.getString("due_date"));
        sdto.setReference(rs.getString("po_reference"));
        sdto.setDisc(rs.getBigDecimal("discount"));
        sdto.setRoff(rs.getBigDecimal("round_off"));
        sdto.setInvDisc(rs.getString("discount_type"));
        sdto.setDiscPer(rs.getBigDecimal("discount_per"));
        sdto.setCessTaxRate(rs.getBigDecimal("cess_tax_rate"));
        sdto.setCstRate(rs.getBigDecimal("cst_rate"));
        sdto.setTaxName(rs.getString("tax_name"));
      }
      return sdto;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  private static final String[] QUERY_FIELD_NAMES = { "", "SM.SUPPLIER_NAME", "PINV.INVOICE_NO",
      "PINV.INVOICE_DATE", "PINV.DUE_DATE" };

  private static final String INV_EXT_QUERY_FIELDS = " SELECT sm.supplier_name,sm.cust_supplier_code,pinv.supplier_id,pinv.invoice_no,pinv.status,pinv.supplier_id, "
      + " pinv.invoice_date,pinv.due_date,pinv.po_no,pinv.status ,pinv.round_off,coalesce(pinv.other_charges,0) AS other_charges,"
      + "pinv.discount_type,pinv.discount,pinv.discount_per, pinv.cess_tax_amt"
      + " ,	coalesce((select SUM(((png.billed_qty * png.cost_price) - png.discount + png.tax)) AS amt FROM store_grn_details png,"
      + "store_grn_main pngm  WHERE 	png.grn_no=pngm.grn_no AND pngm.supplier_invoice_id=pinv.supplier_invoice_id "
      + " GROUP BY pngm.supplier_invoice_id),0) as final_amt";

  private static final String INV_EXT_QUERY_COUNT = " SELECT COUNT(pinv.invoice_no) ";

  private static final String INV_EXT_QUERY_TABLES = " FROM store_invoice pinv JOIN supplier_master sm ON supplier_code= supplier_id ";

  public static PagedList searchInvoices(String invNo, List suppId, List status,
      java.sql.Date ifDate, java.sql.Date itDate, java.sql.Date dfDate, java.sql.Date dtDate,
      int sortOrder, boolean sortReverse, int pageSize, int pageNum)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    String sortField = null;
    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES.length)) {
      sortField = QUERY_FIELD_NAMES[sortOrder];
    }

    SearchQueryBuilder qb = new SearchQueryBuilder(con, INV_EXT_QUERY_FIELDS, INV_EXT_QUERY_COUNT,
        INV_EXT_QUERY_TABLES, null, sortField, sortReverse, pageSize, pageNum);

    qb.addFilter(qb.STRING, "PINV.INVOICE_NO", "ILIKE", invNo);
    qb.addFilter(qb.STRING, "PINV.SUPPLIER_ID", "IN", suppId);
    qb.addFilter(qb.STRING, "PINV.STATUS", "IN", status);
    qb.addFilter(qb.DATE, "date_trunc('day', PINV.INVOICE_DATE)", ">=", ifDate);
    qb.addFilter(qb.DATE, "date_trunc('day', PINV.INVOICE_DATE)", "<=", itDate);
    qb.addFilter(qb.DATE, "date_trunc('day', PINV.DUE_DATE)", ">=", dfDate);
    qb.addFilter(qb.DATE, "date_trunc('day', PINV.DUE_DATE)", "<=", dtDate);
    qb.addSecondarySort("PINV.INVOICE_NO");
    qb.build();
    ArrayList list = new ArrayList();
    int totalCount = 0;

    try (PreparedStatement psData = qb.getDataStatement();
        PreparedStatement psCount = qb.getCountStatement();
        ResultSet rsData = psData.executeQuery();
        ResultSet rsCount = psCount.executeQuery();) {

      while (rsData.next()) {
        ViewPO inv = new ViewPO();
        populateBill(inv, rsData);
        list.add(inv);
      }

      if (rsCount.next()) {
        totalCount = rsCount.getInt(1);
      }
    }

    qb.close();
    con.close();

    return new PagedList(list, totalCount, pageSize, pageNum);
  }

  private static void populateBill(ViewPO inv, ResultSet rs) throws SQLException {
    inv.setSuppName(rs.getString("supplier_name"));
    inv.setInvoiceNo(rs.getString("invoice_no"));
    inv.setStatus(rs.getString("status"));
    inv.setInvdate(rs.getDate("invoice_date"));
    inv.setDuetdate(rs.getDate("due_date"));
    BigDecimal amount = rs.getBigDecimal("final_amt").add(rs.getBigDecimal("other_charges"))
        .add(rs.getBigDecimal("cess_tax_amt"));
    if (rs.getString("discount_type").equals("A")) {
      amount = amount.subtract(rs.getBigDecimal("discount"));
    } else if (rs.getString("discount_type").equals("P")) {
      BigDecimal discount = amount.multiply(rs.getBigDecimal("discount_per"))
          .divide(new BigDecimal(100));
      amount = amount.subtract(discount);
    }
    inv.setAmount(amount.add(rs.getBigDecimal("round_off")));
    inv.setSuppId(rs.getString("supplier_id"));

  }

  private static final String[] QUERY_FIELD_NAMES1 = { "", "POM.PO_NO", "POM.PO_DATE",
      "SM.SUPPLIER_NAME", "POM.QUT_NO" };

  private static final String PO_EXT_QUERY_FIELDS = " SELECT POM.PO_NO,POM.PO_DATE,POM.STATUS,POM.QUT_NO,"
      + " (SELECT COUNT(*) FROM store_grn_main WHERE PO_NO=POM.PO_NO) AS GRNCOUNT,"
      + " POM.PO_TOTAL,SM.SUPPLIER_NAME,SM.CUST_SUPPLIER_CODE,POM.SUPPLIER_ID ";

  private static final String PO_EXT_QUERY_COUNT = " SELECT count(POM.PO_NO) ";

  private static final String PO_EXT_QUERY_TABLES = " FROM  store_po_main POM JOIN SUPPLIER_MASTER SM ON SM.SUPPLIER_CODE=POM.SUPPLIER_ID";

  public static PagedList searchPOS(String poNo, String qutNo, List suppId, List status,
      java.sql.Date fromDate, java.sql.Date toDate, int sortOrder, boolean sortReverse,
      int pageSize, int pageNum) throws SQLException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    String sortField = null;
    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES1.length)) {
      sortField = QUERY_FIELD_NAMES1[sortOrder];
    }

    SearchQueryBuilder qb = new SearchQueryBuilder(con, PO_EXT_QUERY_FIELDS, PO_EXT_QUERY_COUNT,
        PO_EXT_QUERY_TABLES, null, sortField, sortReverse, pageSize, pageNum);

    qb.addFilter(qb.STRING, "POM.PO_NO", "=", poNo);
    qb.addFilter(qb.STRING, "POM.QUT_NO", "ILIKE", qutNo);
    qb.addFilter(qb.STRING, "POM.SUPPLIER_ID", "IN", suppId);
    qb.addFilter(qb.STRING, "POM.STATUS", "IN", status);
    qb.addFilter(qb.DATE, "date_trunc('day', POM.PO_DATE)", ">=", fromDate);
    qb.addFilter(qb.DATE, "date_trunc('day', POM.PO_DATE)", "<=", toDate);

    qb.build();
    ArrayList list = new ArrayList();
    int totalCount = 0;

    try(PreparedStatement psData = qb.getDataStatement();
        PreparedStatement psCount = qb.getCountStatement();
        ResultSet rsData = psData.executeQuery();
        ResultSet rsCount = psCount.executeQuery();) {
   
      while (rsData.next()) {
        ViewPO po = new ViewPO();
        populatePOBill(po, rsData);
        list.add(po);
      }
      rsData.close();
      
      if (rsCount.next()) {
        totalCount = rsCount.getInt(1);
      }
      rsCount.close();
    }

    qb.close();
    con.close();

    return new PagedList(list, totalCount, pageSize, pageNum);
  }

  private static void populatePOBill(ViewPO po, ResultSet rs) throws SQLException {
    po.setPono(rs.getString("po_no"));
    po.setPoDate(rs.getDate("po_date"));
    po.setStatus(rs.getString("status"));
    po.setSuppName(rs.getString("supplier_name"));
    po.setAmount(rs.getBigDecimal("po_total"));
    po.setGrnCount(rs.getInt("grncount"));
    po.setQutNo(rs.getString("qut_no"));

  }

  public static final String GET_TAX_RATES = "SELECT tax_rate FROM store_grn_details GROUP BY tax_rate ORDER BY tax_rate ASC";

  public static List getAllTaxRates() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_TAX_RATES);
  }

  private static final String GET_ALL_USER_NAME = "SELECT emp_username as username "
      + "FROM u_user " + "JOIN u_role USING(role_id) "
      + "WHERE hosp_user='Y' and portal_id='N' and emp_status='A'ORDER BY emp_username ";

  public List getAllUsers() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_USER_NAME);
  }

  private static final String CHECK_MEDICINE_NAME_IN_STORE = "SELECT medicine_id FROM store_stock_details WHERE dept_id=? and medicine_id=?";

  public static boolean checkMedicineName(int medicineID, int store) throws SQLException {

    PreparedStatement ps = null;
    boolean target = true;
    String medicineStatus = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(CHECK_MEDICINE_NAME_IN_STORE);
      ps.setInt(1, store);
      ps.setInt(2, medicineID);
      medicineStatus = DataBaseUtil.getStringValueFromDb(ps);
      if (medicineStatus == null)
        target = false;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return target;
  }

  private static final String GET_PO_ITEMS = "SELECT poi.item_order, poi.medicine_id, pmd.medicine_name, poi.mrp, poi.adj_mrp, pmd.max_cost_price, "
      + " poi.qty_req, poi.bonus_qty_req, poi.discount, poi.discount_per, "
      + " poi.vat_rate, poi.vat_type, poi.vat, "
      + " pom.po_qty_unit, poi.cost_price, poi.med_total, "
      + " COALESCE(pmd.package_type,'') AS package_type, "
      + " pmd.issue_units, mc.billable, poi.item_ced_per, poi.item_ced, pmd.item_barcode_id,"
      + " poi.po_pkg_size, pmd.package_uom,poi.status,poi.item_remarks,"
      + "pmd.tax_rate as master_vat_rate,pmd.tax_type as master_vat_type,sic.item_code "
      + "FROM store_po poi " + " JOIN store_item_details pmd using(medicine_id) "
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id AND hict.health_authority=?) "
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = pmd.medicine_id AND sic.code_type = hict.code_type) "
      + " JOIN store_category_master mc ON pmd.med_category_id=mc.category_id"
      + " JOIN store_po_main pom using(po_no) where po_no=? order by item_order";

  public static List<BasicDynaBean> getPOItems(String poNo, String healthAuthority)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PO_ITEMS, healthAuthority, poNo);
  }

  public static ArrayList getPOItemsList(String poNo, String healthAuthority) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PO_ITEMS);
      ps.setString(1, healthAuthority);
      ps.setString(2, poNo);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_ALL_SUPPLIERS = "SELECT supplier_code, supplier_name,cust_supplier_code "
      + " FROM supplier_master ORDER BY supplier_name";

  public static final String GET_ALL_CENTER_SUPPLIERS = "select sm.*, sm.supplier_name,scm.center_id, "
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city " + " from supplier_master sm  "
      + " left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + " where scm.status='A' and sm.status='A' and scm.center_id IN(?,0)";
  
  public static final String GET_ALL_CENTER_SUPPLIERS_ORDERBY = " order by sm.supplier_name ";

  public static ArrayList listAllcentersforAPo(int centerId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        StringBuilder query = new StringBuilder(GET_ALL_CENTER_SUPPLIERS + GET_ALL_CENTER_SUPPLIERS_ORDERBY);
        pstmt = con.prepareStatement(query.toString());
        pstmt.setInt(1, centerId);
      } else {
        pstmt = con.prepareStatement(GET_ALL_SUPPLIERS);
      }
      return DataBaseUtil.queryToArrayList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  //TODO rename, restructure
  private static final String GET_ALL_SUPPLIERS_2 = "SELECT *," 
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city "
      + " FROM supplier_master sm WHERE status='A' ";

  public static List filterSuppliersByQuery(Integer centerId, String searchQuery, Integer limit) throws SQLException{
    Connection con = null;
    PreparedStatement pstmt = null;
    try{
      con = DataBaseUtil.getReadOnlyConnection();
      StringBuilder query;
      if (centerId != 0) { 
        query = new StringBuilder(GET_ALL_CENTER_SUPPLIERS);
      }else{
        query = new StringBuilder(GET_ALL_SUPPLIERS_2);
      }
        if(!StringUtils.isEmpty(searchQuery)){
          query.append(" AND (sm.supplier_name||' '||COALESCE(sm.cust_supplier_code,'')||"
              + "' '||COALESCE(sm.supplier_city,'') ~* ? OR sm.supplier_name||' '||COALESCE("
              + "sm.cust_supplier_code,'')||' '||COALESCE(sm.supplier_city,'') ~* ? ) ");
        }
        
        if(limit != null && limit > 0){
          query.append(" LIMIT ? ");
        }
        
        pstmt = con.prepareStatement(query.toString());
        int i = 1;
        if(centerId != 0){
          pstmt.setInt(i++, centerId);
        }
        if(!StringUtils.isEmpty(searchQuery)){
          pstmt.setString(i++, "^" + searchQuery.replaceAll("\\s+", ".*\\\\s+"));
          pstmt.setString(i++, ".*\\s+" + searchQuery.replaceAll("\\s+", ".*\\\\s+"));
        }
        if(limit != null && limit > 0){
          pstmt.setInt(i++, limit);
        }
      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }
		
  public static BasicDynaBean getSupplierById(String supplierCode) throws SQLException{
    StringBuilder query = new StringBuilder(GET_ALL_SUPPLIERS_2);
    query.append(" AND sm.supplier_code = ?");
    
    return DataBaseUtil.queryToDynaBean(query.toString(),supplierCode);
  }

  public static List<BasicDynaBean> listsuppliersforAPo(int centerId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        pstmt = con.prepareStatement(GET_ALL_CENTER_SUPPLIERS);
        pstmt.setInt(1, centerId);
      } else {
        pstmt = con.prepareStatement(GET_ALL_SUPPLIERS);
      }
      return DataBaseUtil.queryToDynaList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private static final String GET_ALL_PO_SUPPLIERS = "SELECT supplier_code, supplier_name,cust_supplier_code "
      + " FROM supplier_master ORDER BY supplier_name";

  public static final String GET_ALL_PO_CENTER_SUPPLIERS = "select * from "
      + "	(select sm.supplier_code,supplier_name,cust_supplier_code,coalesce(scm.center_id,0) as center_id "
      + "		from supplier_master sm  "
      + "		left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + "	where scm.status='A' order by sm.supplier_name ) as foo where (foo.center_id=?)";

  public static List listAllsuppliersforAPo(int centerId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        pstmt = con.prepareStatement(GET_ALL_PO_CENTER_SUPPLIERS);
        pstmt.setInt(1, centerId);
      } else {
        pstmt = con.prepareStatement(GET_ALL_PO_SUPPLIERS);
      }
      return DataBaseUtil.queryToDynaList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private static final String GET_PO_ITEMS_LIST = "SELECT distinct sitd.medicine_name, item_order, "
      + " pom.po_no,to_char(pom.po_date,'dd-mon-yy') as po_date,po_pkg_size, "
      + " sm.supplier_name,sm.cust_supplier_code, REPLACE(sm.supplier_address,'~','')as supplier_address, "
      + " supplier_phone1, supplier_phone2, "
      + " COALESCE(supplier_city,'') as city_name,COALESCE(supplier_country,'') as country_name, "
      + " COALESCE(supplier_state ,'') as state_name,pom.supplier_terms,po.item_ced_per,po.item_ced, "
      + " pom.hospital_terms, pom.delivery_instructions, COALESCE(sm.supplier_tin_no,'')as supplier_tin, "
      + " po.discount,po.qty_req,po.bonus_qty_req,po.mrp,po.cost_price,po.vat_rate,po.vat, "
      + " pom.qut_no,COALESCE(to_char(pom.qut_date,'dd-mon-yy'),'') as qut_date,sitd.issue_units, "
      + " sitd.issue_base_unit, sitd.package_type, mf.manf_name, mf.manf_mnemonic, "
      + " po.adj_mrp,pom.reference,po.med_total,pom.credit_period,pom.delivery_date,pom.actual_po_date,"
      + " pom.user_id,s.dept_name as store_name,sitd.package_uom,po.discount_per,ifm.item_form_name, "
      + " hcms.center_name as store_center,hcms.center_address as store_center_address, sitd.cust_item_code, sm.cust_supplier_code, "
      + " sm.drug_license_no, sm.pan_no, sm.cin_no, sm.supplier_tin_no,s.pharmacy_tin_no,hcms.tin_number"
      +
      // " ig.item_group_name,isb.item_subgroup_name,sptd.tax_rate,sptd.tax_amt, "+
      " FROM store_po_main pom join store_po po on pom.po_no = po.po_no AND po.status != 'R' "
      + "   JOIN supplier_master sm on sm.supplier_code = pom.supplier_id "
      + "   JOIN store_item_details sitd on po.medicine_id = sitd.medicine_id "
      + "   LEFT JOIN item_form_master ifm on sitd.item_form_id = ifm.item_form_id "
      + "   JOIN stores s on (s.dept_id=pom.store_id) "
      + "	JOIN hospital_center_master hcms on (hcms.center_id=s.center_id) "
      + "   JOIN manf_master mf ON (sitd.manf_name = mf.manf_code) " +
      // " LEFT JOIN store_po_tax_details sptd on sptd.po_no = pom.po_no "+
      // " LEFT JOIN item_sub_groups_tax_details isgtd on isgtd.item_subgroup_id =
      // sptd.item_subgroup_id "+
      // " LEFT JOIN item_sub_groups isb on isb.item_subgroup_id = isgtd.item_subgroup_id "+
      // " LEFT JOIN item_groups ig on ig.item_group_id = isb.item_group_id" +
      " WHERE pom.po_no=? order by item_order";

  public static List<BasicDynaBean> getPurchaseOrderList(Connection con, String poNo)
      throws SQLException {
    List<BasicDynaBean> resultBeans = null;

    try(PreparedStatement ps = con.prepareStatement(GET_PO_ITEMS_LIST);) { 
      ps.setString(1, poNo);
      resultBeans = DataBaseUtil.queryToDynaList(ps);
    }
    
    return resultBeans;
  }

  public static BasicDynaBean getSupplierDetails(Connection con, String poNo) throws SQLException {

    List list = null;
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          "select * from supplier_master where supplier_code= (select supplier_id from store_po_main where po_no=?)");
      ps.setString(1, poNo);
      list = DataBaseUtil.queryToDynaList(ps);
 
    } finally {
      if (ps != null) { ps.close();}
    }

    if (list.size() > 0) {
      return (BasicDynaBean) list.get(0);
    } else {
      return null;
    }

  }

  public static final String GET_ITEMS_IN_MASTER = "SELECT DISTINCT pmd.medicine_name, pmd.medicine_id,pmd.cust_item_code, 'false' as supplier_rate_validation,"
      + "  COALESCE(pmd.package_type,'') AS package_type, pmd.issue_base_unit, pmd.issue_units, pmd.max_cost_price, "
      + "  pmd.package_uom, pmd.item_barcode_id, m.manf_name, "
      + "  price.cost_price, price.mrp,COALESCE(sir.tax_rate,pmd.tax_rate) as tax_rate,"
      + "  COALESCE(sir.tax_type,pmd.tax_type) as tax_type, "
      + "  COALESCE((SELECT SUM(qty) AS qty FROM store_stock_details "
      + "    WHERE medicine_id=pmd.medicine_id AND dept_id=?),0) AS qty, "
      + "  COALESCE((SELECT SUM(qty) AS qty FROM store_stock_details ssd "
      + "   JOIN stores s USING(dept_id) "
      + "    WHERE medicine_id=pmd.medicine_id *centerfilter* ),0) AS totalqty, "
      + "  ic.billable,sic.item_code " + "FROM store_item_details pmd "
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = pmd.medicine_id AND hict.health_authority=?) "
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = pmd.medicine_id AND sic.code_type = hict.code_type) "
      +

      "  LEFT JOIN store_item_rates sir ON (pmd.medicine_id = sir.medicine_id AND "
      + "    sir.store_rate_plan_id = ?) "
      + "  JOIN store_category_master ic ON pmd.med_category_id=ic.category_id "
      + "  JOIN manf_master m ON m.manf_code=pmd.manf_name " + "  LEFT JOIN ( "
      + "    SELECT cost_price, mrp FROM store_grn_details sg "
      + "      JOIN store_grn_main sgm USING (grn_no) "
      + "      LEFT JOIN store_invoice si ON (si.supplier_invoice_id = sgm.supplier_invoice_id "
      + "        AND supplier_id = ?) "
      + "    WHERE sg.medicine_id=? AND sgm.debit_note_no IS NULL #"
      + "    ORDER BY supplier_id, grn_date DESC LIMIT 1 " + "  ) as price ON (true) "
      + "WHERE pmd.medicine_id=?";

  public static BasicDynaBean getItemDetailsForPo(int itemId, int storeId, String suppId,
      List<Integer> storeList, int centerId) throws SQLException {

    BasicDynaBean storeBean = StoreDAO.findByStore(storeId);
    String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
    String healthAuthority = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();

    int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null
        ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    StringBuilder query = new StringBuilder("");
    DataBaseUtil.addWhereFieldInList(query, "store_id", storeList, true);
    Connection con = null;
    PreparedStatement ps = null;

    try {
      int h = 1;
      int i = 6;
      con = DataBaseUtil.getReadOnlyConnection();
      String itemQuery = GET_ITEMS_IN_MASTER.replace("#", query.toString());
      ps = con.prepareStatement(
          itemQuery.replace("*centerfilter*", (centerId == 0 ? "" : " AND s.center_id = ?")));
      ps.setInt(h++, storeId);
      if (centerId != 0) {
        ps.setInt(h++, centerId);
        i++;
      }
      ps.setString(h++, healthAuthority);
      ps.setInt(h++, storeRatePlanId);
      ps.setString(h++, suppId);
      ps.setInt(h++, itemId);
      for (Integer storeNo : storeList) {
        ps.setInt(i, storeNo);
        i++;
      }
      ps.setInt(i, itemId);

      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  public static final String GET_INDENT_DETAILS = " SELECT distinct indent_no,COALESCE(to_char(date_time,'dd-mon-yy'),'') as indent_date"
      + "       ,hc.center_name as indent_center_name " + " FROM store_indent_details "
      + " JOIN store_indent_main using(indent_no) "
      + " JOIN stores s on ( s.dept_id = indent_store ) "
      + " LEFT JOIN hospital_center_master hc  ON( s.center_id = hc.center_id)"
      + " WHERE po_no LIKE ?";

  public static List<BasicDynaBean> getIndentNo(String poNo) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INDENT_DETAILS);
      ps.setString(1, "%" + poNo + "%");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  public static List getApprovedPOs() throws SQLException {
    return DataBaseUtil
        .queryToDynaList("SELECT * FROM store_po_main WHERE status IN ('A','AA') ORDER BY po_no");
  }

  public static List getApprovedPOs(int centerId) throws SQLException {
    return DataBaseUtil.queryToDynaList("SELECT spm.* FROM store_po_main spm "
        + "JOIN stores s ON (s.dept_id = spm.store_id)  "
        + "WHERE s.center_id in (?) and spm.status IN ('A','AA') ORDER BY po_no", centerId);
  }

  public static List getPoBean(String poNum) throws SQLException {
    return DataBaseUtil.queryToDynaList("SELECT * FROM store_po_main WHERE po_no=?", poNum);
  }

  public static final String UPDATE_RECD_QTY = " UPDATE store_po SET qty_received=qty_received+?, bonus_qty_received=bonus_qty_received+?, "
      + " status= (CASE WHEN (qty_received+?>=qty_req) AND (bonus_qty_received+?>=bonus_qty_req) "
      + "  THEN 'F' ELSE 'P' END) " + " WHERE po_no=? AND medicine_id=? ";

  public static void updateReceivedQty(Connection con, String poNo, int medicineId,
      BigDecimal billedQty, BigDecimal bonusQty) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_RECD_QTY);) {
    int i = 1;
    ps.setBigDecimal(i++, billedQty);
    ps.setBigDecimal(i++, bonusQty);
    ps.setBigDecimal(i++, billedQty);
    ps.setBigDecimal(i++, bonusQty);
    ps.setString(i++, poNo);
    ps.setInt(i++, medicineId);
    ps.executeUpdate();
    }
  }

  public static final String COPY_PO_DETAIL_OLD = "INSERT INTO store_po (po_no, qty_req, mrp, adj_mrp, cost_price, vat, discount, med_total, "
      + "  vat_rate, qty_received, vat_type, medicine_id, status, bonus_qty_req, "
      + "  bonus_qty_received, discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced) "
      + "SELECT ?, qty_req, mrp, adj_mrp, cost_price, "
      + "		( CASE WHEN vat_type = 'MB' THEN (adj_mrp * (qty_req + bonus_qty_req)/po_pkg_size*vat_rate/100)"
      + "        	  WHEN vat_type = 'M' THEN (adj_mrp * (qty_req)/po_pkg_size*vat_rate/100)"
      + "		 	  WHEN vat_type = 'C' THEN  (cost_price*qty_req/po_pkg_size -discount+item_ced) * vat_rate / 100"
      + "    		  ELSE (cost_price*(qty_req + bonus_qty_req)/po_pkg_size -discount+item_ced) * vat_rate / 100 END ) as vat, "
      + "	discount, (foo.cost_price * foo.qty_req / foo.po_pkg_size " + "	- foo.discount + "
      + "		( CASE WHEN vat_type = 'MB' THEN (adj_mrp * (qty_req + bonus_qty_req)/po_pkg_size*vat_rate/100)"
      + "        	  WHEN vat_type = 'M' THEN (adj_mrp * (qty_req)/po_pkg_size*vat_rate/100)"
      + "		 	  WHEN vat_type = 'C' THEN  (cost_price*qty_req/po_pkg_size -discount+item_ced) * vat_rate / 100"
      + "    		  ELSE (cost_price*(qty_req + bonus_qty_req)/po_pkg_size -discount+item_ced) * vat_rate / 100 END ) + foo.item_ced) as med_total, "
      + "  vat_rate, 0, vat_type, medicine_id, status, bonus_qty_req, "
      + "  0, discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced "
      + " FROM ( SELECT qty_req, mrp, mrp/(1 + COALESCE(ssir.tax_rate,vat_rate)/100) as adj_mrp, cost_price,  discount, med_total, "
      + "  			COALESCE(ssir.tax_rate,vat_rate) as vat_rate, 0,    "
      + "  			COALESCE(ssir.tax_type,vat_type) as vat_type,sp.medicine_id, sp.status, bonus_qty_req, "
      + "  			0,discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced "
      + "FROM store_po sp " + "  JOIN stores s ON ( dept_id = ? ) "
      + "  LEFT JOIN store_item_rates ssir ON (sp.medicine_id = ssir.medicine_id AND "
      + "    ssir.store_rate_plan_id = s.store_rate_plan_id) "
      + "WHERE po_no=? AND sp.status !='R' ) as foo ";

  public static final String COPY_PO_DETAIL = "INSERT INTO store_po (po_no, qty_req, mrp, adj_mrp, cost_price, vat, discount, med_total, "
      + " vat_rate, vat_type, medicine_id, status, bonus_qty_req, "
      + "  discount_per, item_remarks, po_pkg_size, item_ced_per, item_ced) "
      + "SELECT ?, sp.qty_req, sp.mrp, sp.adj_mrp, sp.cost_price, sp.vat, sp.discount, sp.med_total, "
      + " sp.vat_rate, sp.vat_type, sp.medicine_id, sp.status, sp.bonus_qty_req, "
      + " sp.discount_per, sp.item_remarks, sp.po_pkg_size, sp.item_ced_per, sp.item_ced "
      + " FROM store_po sp " + " JOIN store_po_main spm ON (spm.po_no = sp.po_no) "
      + " LEFT JOIN stores s ON ( s.dept_id::text = spm.dept_id )"
      + " JOIN store_item_details sid ON (sid.medicine_id = sp.medicine_id )"
      + " WHERE sp.po_no=? AND sp.status !='R' AND sid.status = 'A' ";

  public static final String COPY_PO_TAX_DETAIL = "INSERT INTO store_po_tax_details "
      + " (medicine_id, po_no, item_subgroup_id, tax_rate, tax_amt) "
      + " SELECT medicine_id, ?, item_subgroup_id, tax_rate, tax_amt "
      + " FROM store_po_tax_details sptd WHERE sptd.po_no = ?";

  public static void copyPoDetail(Connection con, String fromPo, String toPo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(COPY_PO_DETAIL);) {
    ps.setString(1, toPo);
    ps.setString(2, fromPo);
    ps.executeUpdate();
    }
  }

  public static void copyPoTaxDetail(Connection con, String fromPo, String toPo)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(COPY_PO_TAX_DETAIL);) {
      ps.setString(1, toPo);
      ps.setString(2, fromPo);
      ps.executeUpdate();
    }
  }

  public static final String CLOSE_PO_IF_ALL_RECEIVED = "UPDATE store_po_main pom SET status = 'C' WHERE po_no = ? AND NOT EXISTS "
      + " (SELECT * FROM store_po WHERE po_no = pom.po_no AND "
      + "   (qty_req > qty_received OR bonus_qty_req > bonus_qty_received))";

  public static void closePoIfAllReceived(Connection con, String poNo) throws SQLException {
    try(PreparedStatement ps = con.prepareStatement(CLOSE_PO_IF_ALL_RECEIVED);) {
      ps.setString(1, poNo);
      ps.executeUpdate();
    }

  }

  public static final String UPDATE_PO_ITEM_STATUS = " UPDATE store_po SET status = ? WHERE po_no = ? ";

  public boolean updatePOItemsStatus(Connection con, String poNo, String status)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_PO_ITEM_STATUS);
      ps.setString(1, status);
      ps.setString(2, poNo);

      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }
  
  public static final String UPDATE_PO_VAT_AND_MED_TOTAL = " UPDATE store_po SET med_total = ?, "
      + " vat_rate = ? , vat = ? WHERE po_no = ?  and medicine_id = ? and item_order = ?";

  public boolean updatePOItemsVat(Connection con, Map keys, BigDecimal medTotal,
      BigDecimal netTaxRate, BigDecimal netTaxAmt) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_PO_VAT_AND_MED_TOTAL);
      ps.setBigDecimal(1, medTotal);
      ps.setBigDecimal(2, netTaxRate);
      ps.setBigDecimal(3, netTaxAmt);

      ps.setString(4, (String) keys.get("po_no"));
      ps.setInt(5, (int) keys.get("medicine_id"));
      ps.setInt(6, (int) keys.get("item_order"));
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

  }

  private static final String GET_UPLOADED_DOC = " SELECT quotation_file_name, quotation_attachment, quotation_contenttype "
      + " FROM store_po_main " + " WHERE po_no=?";

  public static Map getUploadedDocInfo(String poNo) throws SQLException {
    Map<String, Object> upload = new HashMap<String, Object>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_UPLOADED_DOC);
      ps.setString(1, poNo);
      rs = ps.executeQuery();
      if (rs.next()) {
        String filename = rs.getString(1);
        upload.put("filename", filename);
        InputStream uploadfile = rs.getBinaryStream(2);
        upload.put("uploadfile", uploadfile);
        String contenttype = rs.getString(3);
        upload.put("contenttype", contenttype);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return upload;
  }

  public static List getAllSubGroups() throws SQLException {
    return DataBaseUtil.queryToDynaList(
        "SELECT item_group_name, isg.item_subgroup_id, item_subgroup_display_order, ig.item_group_id FROM item_sub_groups isg "
            + " JOIN item_groups ig ON (isg.item_group_id = ig.item_group_id) JOIN item_group_type igt ON (ig.item_group_type_id = igt.item_group_type_id)"
            + " WHERE isg.status = 'A' AND igt.item_group_type_name = 'TAX' ORDER BY item_subgroup_display_order");
  }

  public static List getAllGroups() throws SQLException {
    return DataBaseUtil.queryToDynaList("SELECT item_group_name, item_group_id FROM item_groups ig "
        + " JOIN item_group_type igt ON (ig.item_group_type_id = igt.item_group_type_id)"
        + " WHERE ig.status = 'A' AND igt.item_group_type_name = 'TAX' ORDER BY item_group_display_order");
  }

  private static final String GET_TAX_DETAILS = "SELECT pom.po_no,po.item_order,ig.item_group_name,isb.item_subgroup_name,sptd.tax_rate,sptd.tax_amt,sum(sptd.tax_amt) as total_tax_amt "
      + " FROM store_po_main pom "
      + " JOIN store_po po ON pom.po_no = po.po_no AND po.status != 'R' "
      + " JOIN store_po_tax_details sptd ON sptd.po_no = pom.po_no AND sptd.medicine_id = po.medicine_id "
      + " JOIN item_sub_groups isb ON isb.item_subgroup_id = sptd.item_subgroup_id "
      + " JOIN item_groups ig ON ig.item_group_id = isb.item_group_id" + " WHERE pom.po_no=? "
      + " GROUP BY pom.po_no, po.item_order, item_group_name, item_subgroup_name, sptd.tax_rate, sptd.tax_amt "
      + " ORDER BY item_order";

  public static List<BasicDynaBean> getTaxDetails(Connection con, String poNo) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_TAX_DETAILS);) { 
      ps.setString(1, poNo);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }

  public static List getPOTaxDetails(String poNo) throws SQLException {
    return DataBaseUtil
        .queryToDynaList("SELECT sptd.*, UPPER(isg.item_subgroup_name) as item_subgroup_name,ig.* "
            + " FROM store_po_tax_details sptd JOIN item_sub_groups isg ON (sptd.item_subgroup_id = isg.item_subgroup_id) "
            + " JOIN item_groups ig ON (isg.item_group_id = ig.item_group_id)"
            + " WHERE isg.status='A' AND sptd.po_no = ? ", poNo);
  }

  public List<BasicDynaBean> getItemsOfPO(String poNo) throws SQLException {
    return DataBaseUtil.queryToDynaList("SELECT *,sid.status as item_status "
        + " FROM store_po sp  JOIN store_item_details sid ON ( sid.medicine_id = sp.medicine_id ) "
        + " WHERE  sp.po_no = ? ", poNo);
  }

  private static final String GET_APPROVED_PO_FOR_STORE = "SELECT spm.po_no, spm.store_id, "
      + " spm.supplier_id, spm.discount_type, spm.discount_per, spm.discount, "
      + " spm.tcs_type, spm.tcs_per, spm.tcs_amount, "
      + " spm.round_off, spm.po_qty_unit, spm.remarks, spm.reference, "
      + " spm.transportation_charges, spm.purpose_of_purchase,spm.po_total "
      + " FROM store_po_main spm " 
      + " JOIN stores s ON (s.dept_id = spm.store_id)"
      + " WHERE s.dept_id =? and spm.status IN ('A','AA') ORDER BY po_no ";

  public static List<BasicDynaBean> getApprovedPOsForStore(int storeId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_APPROVED_PO_FOR_STORE, storeId);
  }
}
