package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StoresDashBoardsDAO {

  static Logger logger = LoggerFactory.getLogger(StoresDashBoardsDAO.class);

  private static final String GRN_EXT_QUERY_FIELDS = "SELECT * ";
  private static final String GRN_EXT_QUERY_COUNT = " SELECT count(GRN_NO) ";
  private static final String GRN_EXT_QUERY_TABLES = " FROM ( "
      + "SELECT grn_no, sm.supplier_name,sm.cust_supplier_code, grn_date, si.invoice_no,"
      + " si.status, dept_name, dept_id,  gm.consignment_stock, " + "  coalesce( "
      + "    (SELECT sum((g.billed_qty/g.grn_pkg_size * g.cost_price) - (g.discount+"
      + "g.scheme_discount) + g.tax + g.item_ced)  FROM store_grn_details g WHERE g.grn_no ="
      + " gm.grn_no), 0) "
      + "  - si.discount + si.round_off + si.other_charges + si.transportation_charges +"
      + " si.cess_tax_amt + si.tcs_amount AS invoice_amt "
      + "FROM store_grn_main gm " + "   JOIN store_invoice si USING (supplier_invoice_id) "
      + "   JOIN supplier_master sm ON (sm.supplier_code=si.supplier_id) "
      + "   JOIN stores on store_id=dept_id " + " ) as query";

  /**
   * Screen: View GRN, ActionMethod: getGrns.
   * 
   * @param filter filter
   * @param listing listing
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchGRNS(Map filter, Map listing) throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, GRN_EXT_QUERY_FIELDS, GRN_EXT_QUERY_COUNT,
          GRN_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("grn_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String PO_EXT_QUERY_FIELDS = " SELECT * ";

  private static final String PO_EXT_QUERY_COUNT = " SELECT count(PO_NO) ";

  private static final String PO_EXT_QUERY_TABLES = " FROM ( SELECT POM.PO_NO,POM.PO_DATE,"
      + " POM.STATUS,POM.QUT_NO,s.center_id,POM.po_alloted_to,POM.user_id,POM.remarks,"
      + " (SELECT COUNT(*) FROM store_grn_main WHERE PO_NO=POM.PO_NO) AS grn_count,"
      + " ((SELECT (COALESCE(SUM (qty_req/po_pkg_size * cost_price + vat - discount),0) ) "
      + "FROM store_po WHERE status != 'R' "
      + "AND po_no = pom.po_no GROUP BY po_no)+pom.transportation_charges+pom.round_off-"
      + "pom.discount + pom.tcs_amount) AS po_total,SM.SUPPLIER_NAME,SM.CUST_SUPPLIER_CODE, POM.SUPPLIER_ID,"
      + " pom.store_id  FROM  store_po_main POM JOIN SUPPLIER_MASTER SM ON SM.SUPPLIER_CODE="
      + " POM.SUPPLIER_ID JOIN stores s ON(pom.store_id = s.dept_id)) as FOO";

  /**
   * Screen: View PO, ActionMethod: getPOs.
   * 
   * @param filter filter
   * @param listing listing
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchPOs(Map filter, Map listing) throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, PO_EXT_QUERY_FIELDS, PO_EXT_QUERY_COUNT,
          PO_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("po_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String UPDATE_PO = "UPDATE store_po_main SET STATUS=?,CANCELLED_BY=?"
      + " WHERE PO_NO=?";

  /**
   *  update Purchase Order.
   * @param po List
   * @param con Connection DB
   * @return boolean boolean
   * @throws SQLException SQLException
   */
  public static boolean updatePo(List po, Connection con) throws SQLException {
    int resultCount = 0;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PO);) {
      Iterator itr = po.iterator();
      ViewPO po1 = null;
      while (itr.hasNext()) {
        po1 = (ViewPO) itr.next();
        ps.setString(1, po1.getStatus());
        ps.setString(2, po1.getUserName());
        ps.setString(3, po1.getPono());
        resultCount = resultCount + ps.executeUpdate();
      }
    }
    return resultCount == po.size();
  }

  private static final String ISSUE_EXT_QUERY_FIELDS = "SELECT * ";

  private static final String ISSUE_EXT_QUERY_COUNT = " SELECT count(USER_ISSUE_NO) ";

  /*
   * private static final String ISSUE_EXT_QUERY_TABLES =
   * " FROM (SELECT ISSUE_NO,DATE_TIME AS ISSUE_DATE," +
   * " GDF.DEPT_NAME AS FROM_STORE,GDT.DEPT_NAME AS TO_STORE,USERNAME" +
   * " FROM PHARMACY_STOCK_ISSUE_MAIN PSIM JOIN  stores GDF ON " + " PSIM.STORE_FROM= GDF.DEPT_ID "
   * + " JOIN stores GDT ON PSIM.STORE_TO=GDT.DEPT_ID) as Foo";
   */

  private static final String ISSUE_EXT_QUERY_TABLES = "FROM (SELECT USER_ISSUE_NO,DATE_TIME AS"
      + " ISSUE_DATE, GDF.DEPT_NAME AS FROM_STORE,ISSUED_TO AS ISSUED_TO, "
      + "USERNAME FROM STOCK_ISSUE_MAIN ISM JOIN stores GDF ON "
      + "(ISM.DEPT_FROM = GDF.DEPT_ID) WHERE USER_TYPE != 'Patient') as Foo ";

  private static final String ISSUE_PAT_QUERY_TABLES = "FROM (SELECT ism.USER_ISSUE_NO,DATE_TIME "
      + " AS ISSUE_DATE, GDF.DEPT_NAME AS FROM_STORE,ISSUED_TO AS ISSUED_TO, "
      + "(PD.PATIENT_NAME ||'  ' ||PD.LAST_NAME) AS PATIENT_NAME, ISM.USERNAME,PRE.MR_NO,"
      + " indent.patient_indent_no FROM STOCK_ISSUE_MAIN ISM LEFT JOIN stores GDF ON "
      + "(ISM.DEPT_FROM = GDF.DEPT_ID) LEFT JOIN PATIENT_REGISTRATION PRE ON "
      + " (ISM.ISSUED_TO = PRE.PATIENT_ID) LEFT JOIN LATERAL (select spiid.patient_indent_no,"
      + " sid.user_issue_no FROM store_patient_indent_item_issue_no_details spiid "
      + "JOIN  stock_issue_details sid ON (sid.item_issue_no = spiid.item_issue_no)  "
      // "JOIN store_patient_indent_details spid USING(patient_indent_no) " +
      + "JOIN store_patient_indent_main spim ON(spim.patient_indent_no=spiid.patient_indent_no) "
      + "WHERE  spim.indent_type = 'I' AND sid.user_issue_no = ism.user_issue_no "
      + " group by spiid.patient_indent_no, sid.user_issue_no ) "
      + " as indent on (ism.user_issue_no = indent.user_issue_no )"
      /*
       * "(select spiid.patient_indent_no, sid.user_issue_no FROM stock_issue_details sid " +
       * "JOIN store_patient_indent_item_issue_no_details spiid ON (sid.item_issue_no = 
       * spiid.item_issue_no)  "
       * + "JOIN store_patient_indent_details spid USING(patient_indent_no) " +
       * "JOIN store_patient_indent_main spim ON(spim.patient_indent_no=spid.patient_indent_no) " +
       * "WHERE  spim.indent_type = 'I' group by spiid.patient_indent_no, sid.user_issue_no )
       *  as indent on (ism.user_issue_no = indent.user_issue_no )"+
       */
      + "LEFT JOIN PATIENT_DETAILS PD ON (PRE.MR_NO = PD.MR_NO)  WHERE USER_TYPE = 'Patient'"
      + "AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) as Foo ";

  /**
   * Screen: View Stock Issues, ActionMethod: getStkIss.
   * 
   * @param filter Map
   * @param listing map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchStkIss(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, ISSUE_EXT_QUERY_FIELDS,
          ISSUE_EXT_QUERY_COUNT, ISSUE_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("user_issue_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Screen: View Stock Issues, ActionMethod: getStkIssForPat.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchStkIssForPatient(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, ISSUE_EXT_QUERY_FIELDS,
          ISSUE_EXT_QUERY_COUNT, ISSUE_PAT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("user_issue_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String ADJ_EXT_QUERY_FIELDS = " SELECT *";

  private static final String ADJ_EXT_QUERY_COUNT = " SELECT count(ADJ_NO) ";

  private static final String ADJ_EXT_QUERY_TABLES = " FROM (SELECT GD.DEPT_NAME,DATE_TIME AS"
      + " ADJUST_DATE,PSAM.ADJ_NO,USERNAME,store_id "
      + "FROM  store_adj_main PSAM  " + "JOIN stores GD ON DEPT_ID=STORE_ID) AS FOO";

  /**
   * Screen: View Stock Adjustments, ActionMethod: getStkAdj.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchStkAdj(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, ADJ_EXT_QUERY_FIELDS, ADJ_EXT_QUERY_COUNT,
          ADJ_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("adj_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String RETURN_EXT_QUERY_FIELDS = " SELECT *";

  private static final String RETURN_EXT_QUERY_COUNT = " SELECT count(RETURN_NO) ";

  private static final String RETURN_EXT_QUERY_TABLES = " FROM (SELECT RETURN_NO,S.SUPPLIER_NAME,"
      + " S.CUST_SUPPLIER_CODE,PSRM.DATE_TIME AS RETURN_DATE,"
      + " USER_NAME,case when orig_return_no is null then 'O' else 'E' end as txn_type,"
      + " RETURN_TYPE,PSRM.STATUS,ORIG_RETURN_NO,supplier_id,psrm.store_id,psrm.gatepass_id"
      + " FROM STORE_SUPPLIER_RETURNS_MAIN PSRM "
      + " JOIN SUPPLIER_MASTER S ON SUPPLIER_CODE=PSRM.SUPPLIER_ID) as FOO";

  private static final String RETURN_EXT_QUERY_TABLES_CENTER_WISE = " FROM (SELECT RETURN_NO,"
      + " S.SUPPLIER_NAME,S.CUST_SUPPLIER_CODE,PSRM.DATE_TIME AS RETURN_DATE,SCM.CENTER_ID,"
      + " USER_NAME,case when orig_return_no is null then 'O' else 'E' end as txn_type,"
      + " RETURN_TYPE,PSRM.STATUS,ORIG_RETURN_NO,supplier_id,psrm.store_id,psrm.gatepass_id"
      + " FROM STORE_SUPPLIER_RETURNS_MAIN PSRM "
      + " JOIN SUPPLIER_MASTER S ON (SUPPLIER_CODE=PSRM.SUPPLIER_ID) "
      + " JOIN SUPPLIER_CENTER_MASTER SCM ON (S.SUPPLIER_CODE=SCM.SUPPLIER_CODE)) as FOO";

  /**
   * Screen: Supplier Returns/Replacements, ActionMethod: getSupplierReturns.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchSuppReturns(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    SearchQueryBuilder qb = null;
    List list = new ArrayList();
    int centerID = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerID == 0) {
        qb = new SearchQueryBuilder(con, RETURN_EXT_QUERY_FIELDS, RETURN_EXT_QUERY_COUNT,
            RETURN_EXT_QUERY_TABLES, listing);
        qb.addFilterFromParamMap(filter);
      } else {
        qb = new SearchQueryBuilder(con, RETURN_EXT_QUERY_FIELDS, RETURN_EXT_QUERY_COUNT,
            RETURN_EXT_QUERY_TABLES_CENTER_WISE, listing);
        qb.addFilterFromParamMap(filter);
        list.add(RequestContext.getCenterId());
        list.add(0);
        qb.addFilter(qb.INTEGER, "center_id", "IN", list);
      }

      qb.addSecondarySort("return_no");
      qb.build();

      PagedList l1 = qb.getMappedPagedList();

      qb.close();
      return l1;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String DEBIT_EXT_QUERY_FIELDS = " SELECT *";

  private static final String DEBIT_EXT_QUERY_COUNT = " SELECT count(DEBIT_NOTE_NO) ";

  private static final String DEBIT_EXT_QUERY_TABLES = " FROM (SELECT DEBIT_NOTE_NO,"
      + " S.SUPPLIER_NAME,S.CUST_SUPPLIER_CODE,PDN.DEBIT_NOTE_DATE AS DEBIT_DATE,"
      + " RETURN_TYPE,PDN.STATUS,PDN.SUPPLIER_ID,PDN.STORE_ID" + " FROM STORE_DEBIT_NOTE PDN "
      + "JOIN SUPPLIER_MASTER S ON SUPPLIER_CODE=PDN.SUPPLIER_ID) as FOO";

  private static final String DEBIT_EXT_QUERY_TABLES_CENTER_WISE = " FROM (SELECT DEBIT_NOTE_NO,"
      + " SCM.CENTER_ID,S.SUPPLIER_NAME,S.CUST_SUPPLIER_CODE,PDN.DEBIT_NOTE_DATE AS DEBIT_DATE,"
      + " RETURN_TYPE,PDN.STATUS,PDN.SUPPLIER_ID,PDN.STORE_ID" + " FROM STORE_DEBIT_NOTE PDN "
      + " JOIN SUPPLIER_MASTER S ON (S.SUPPLIER_CODE=PDN.SUPPLIER_ID) "
      + " JOIN SUPPLIER_CENTER_MASTER SCM ON(SCM.SUPPLIER_CODE=S.SUPPLIER_CODE)) as FOO";

  /**
   * Screen: Supplier Returns/Replacements, ActionMethod: getSupplierReturns.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchSuppReturnDebits(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    SearchQueryBuilder qb = null;
    List list = new ArrayList();
    int centerID = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerID == 0) {
        qb = new SearchQueryBuilder(con, DEBIT_EXT_QUERY_FIELDS, DEBIT_EXT_QUERY_COUNT,
            DEBIT_EXT_QUERY_TABLES, listing);
        qb.addFilterFromParamMap(filter);
      } else {
        qb = new SearchQueryBuilder(con, DEBIT_EXT_QUERY_FIELDS, DEBIT_EXT_QUERY_COUNT,
            DEBIT_EXT_QUERY_TABLES_CENTER_WISE, listing);
        qb.addFilterFromParamMap(filter);
        list.add(RequestContext.getCenterId());
        list.add(0);
        qb.addFilter(qb.INTEGER, "center_id", "IN", list);
      }
      qb.addSecondarySort("debit_note_no");
      qb.build();

      PagedList l1 = qb.getMappedPagedList();

      qb.close();
      return l1;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String GET_MED_PURCHASE_RETURN_REPLACE_EXT_QUERY_FIELDS = " SELECT *";

  private static final String GET_MED_PURCHASE_RETURN_REPLACE_EXT_QUERY_COUNT =
      " select count(*) ";

  private static final String GET_MED_PURCHASE_RETURN_REPLACE_EXT_QUERY_TABLES =
      " from store_purchase_details_view";

  /**
   * Screen: Purchase Details, ActionMethod: getPhPurchaseDetails.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchPhPurchaseDetails(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con,
          GET_MED_PURCHASE_RETURN_REPLACE_EXT_QUERY_FIELDS,
          GET_MED_PURCHASE_RETURN_REPLACE_EXT_QUERY_COUNT,
          GET_MED_PURCHASE_RETURN_REPLACE_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("grn_date", true);
      qb.build();

      PagedList l1 = qb.getMappedPagedList();

      qb.close();
      return l1;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String BILL_EXT_QUERY_FIELDS = " SELECT *";

  private static final String BILL_EXT_QUERY_COUNT = " SELECT count(sale_id) ";

  private static final String BILL_EXT_QUERY_TABLES = "  FROM (SELECT b.bill_no, b.bill_type,"
      + " b.visit_id,pmsm.store_id as dept_id, "
      + "  case when b.bill_type = 'C' then 'C' else 'N' end as bill_bill_type,wn.ward_name, "
      + "  case when b.visit_id=prc.customer_id and b.bill_type='C'  "
      + "  and b.visit_type='r' then 'c' else b.visit_type end as visit_type, "
      + "  pmsm.sale_id, pmsm.type AS pharm_bill_type, pmsm.sale_date, "
      + "  coalesce(pmsm.doctor_name,'') as doctor_name, pr.mr_no, pr.patient_id, "
      + "  coalesce(get_patient_name(pd.salutation, pd.patient_name, pd.middle_name,"
      + " pd.last_name),prc.customer_name) as patient_full_name,"
      + "  pmsm.total_item_amount-pmsm.discount+pmsm.round_off as amount, b.visit_type AS"
      + " bill_visit_type,  user_remarks as remarks,pmsm.store_id,s.center_id  FROM "
      + " store_sales_main pmsm  JOIN stores s ON (pmsm.store_id = s.dept_id) "
      + "  LEFT JOIN ward_names wn using(ward_no) JOIN bill b on(pmsm.bill_no=b.bill_no) "
      + "  JOIN bill_charge bc on(bc.charge_id = pmsm.charge_id)  "
      + "  LEFT JOIN store_retail_customers prc on(prc.customer_id=b.visit_id) "
      + "  LEFT JOIN patient_registration pr on(pr.patient_id=b.visit_id) "
      + "  LEFT JOIN patient_details pd USING (mr_no)"
      + " WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )) AS FOO";

  /**
   * Screen: Duplicate Sales Bills, ActionMethod: getSaleBillsList.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchPhSalesDetails(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = null;
    SearchQueryBuilder qb = null;
    PagedList list = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, BILL_EXT_QUERY_FIELDS, BILL_EXT_QUERY_COUNT,
          BILL_EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      int centerId = RequestContext.getCenterId();

      if (centerId != 0) {
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      }

      qb.build();
      list = qb.getMappedPagedList();
      return list;
    } finally {
      if(qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }

  }

  private static final String PENDING_SALE_BILLS_LIST_QUERY_FIELDS = " SELECT *";

  private static final String PENDING_SALE_BILLS_LIST_QUERY_COUNT = "SELECT count(sale_id) ";

  private static final String PENDING_SALE_BILLS_LIST_QUERY_TABLES = " FROM ( "
      + "  SELECT pr.mr_no, COALESCE(pr.patient_id, customer_id) AS visit_id, "
      + "   coalesce(pd.patient_name|| ' ' ||pd.last_name, prc.customer_name)"
      + " as patient_full_name,   b.bill_no, b.bill_type, pmsm.sale_date AS sale_date,"
      + " pmsm.sale_id,pmsm.type  FROM bill b "
      + " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + "   LEFT JOIN store_retail_customers prc ON (prc.customer_id = b.visit_id) "
      + "   LEFT JOIN patient_details pd ON (pd.mr_no=pr.mr_no) "
      + "   JOIN store_sales_main pmsm ON (pmsm.bill_no=b.bill_no) "
      + "  WHERE b.bill_type = 'P' AND b.restriction_type = 'P' AND b.status = 'A' "
      + "  AND ( patient_confidentiality_check(COALESCE(pd.patient_group,0),pd.mr_no) ))"
      + " AS FOO";

  /**
   * Screen: Pending Bill Lists, ActionMethod: getPendingSaleBillsList.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList getPendingSaleList(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, PENDING_SALE_BILLS_LIST_QUERY_FIELDS,
          PENDING_SALE_BILLS_LIST_QUERY_COUNT, PENDING_SALE_BILLS_LIST_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("sale_id");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  public static final String GET_RETAIL_CREDIT_FIELDS = " SELECT *";

  private static final String GET_RETAIL_CREDIT_FIELDS_COUNT = "SELECT count(*) ";

  private static final String GET_RETAIL_CREDIT_FIELDS_TABLES = "FROM (SELECT  b.bill_no,"
      + " textcat_commacat(sale_id) as saleIds,prc.customer_name,prc.customer_id,"
      + " prc.phone_no,prc.credit_limit,sum(bc.amount+bc.tax_amt) as current_due,"
      + " sum(bc.discount)as discount, "
      + " textcat_commacat((sale_date::date)::text) AS sale_date, b.open_date "
      + " FROM store_retail_customers prc"
      + " JOIN bill b ON b.visit_id=prc.customer_id AND b.bill_type='C' AND b.status='A'"
      + " AND b.visit_type='r' JOIN store_sales_main pmsm ON (pmsm.bill_no = b.bill_no) "
      + " JOIN bill_charge bc ON (bc.charge_id = pmsm.charge_id) "
      + " GROUP BY b.bill_no, b.open_date, prc.customer_name,prc.customer_id,prc.phone_no,"
      + " prc.credit_limit) AS FOO";

  /**
   * Screen: Retail Pending Bill Lists, ActionMethod: getRetailPendingSaleList.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList getRetailPendingSaleList(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, GET_RETAIL_CREDIT_FIELDS,
          GET_RETAIL_CREDIT_FIELDS_COUNT, GET_RETAIL_CREDIT_FIELDS_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("customer_id");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String STK_TRN_EXT_QUERY_FIELDS = "SELECT *";

  private static final String STK_TRN_RETURN_EXT_QUERY_COUNT = " SELECT count(TRANSFER_NO) ";

  private static final String STK_TRN_RETURN__EXT_QUERY_TABLES = " FROM (SELECT TRANSFER_NO,"
      + " DATE_TIME AS TRANSFER_DATE, GDF.DEPT_NAME AS FROM_STORE,gdf.dept_id as from_id,"
      + " GDT.DEPT_NAME AS TO_STORE,gdt.dept_id as to_id,USERNAME"
      + " FROM store_transfer_main ISTM JOIN  stores GDF ON " + " ISTM.STORE_FROM= GDF.DEPT_ID "
      + " JOIN stores GDT ON ISTM.STORE_TO=GDT.DEPT_ID) as Foo";

  /**
   * Screen: View Stock Transfer, ActionMethod: getStkTransfer.
   * 
   * @param filter Map
   * @param listing Mpa
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchStkTransfer(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, STK_TRN_EXT_QUERY_FIELDS,
          STK_TRN_RETURN_EXT_QUERY_COUNT, STK_TRN_RETURN__EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("transfer_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String USER_RETURN_EXT_QUERY_FIELDS = "SELECT *";

  private static final String USER_RETURN_EXT_QUERY_COUNT = " SELECT count(USER_RETURN_NO) ";

  private static final String USER_RETURN__EXT_QUERY_TABLES = " FROM (SELECT USER_RETURN_NO,"
      + " SIRM.USER_ISSUE_NO,SIRM.DATE_TIME AS RETURN_DATE,"
      + " GDF.DEPT_NAME AS TO_STORE,RETURNED_BY,SIRM.USERNAME"
      + " FROM STORE_ISSUE_RETURNS_MAIN  SIRM " + " JOIN  STORES GDF ON SIRM.DEPT_TO="
      + " GDF.DEPT_ID  JOIN STORES GDT ON SIRM.DEPT_TO=GDT.DEPT_ID "
      + " JOIN STOCK_ISSUE_MAIN SIM ON SIM.USER_ISSUE_NO = SIRM.USER_ISSUE_NO "
      + " WHERE USER_TYPE != 'Patient') as Foo";

  /**
   * Screen: View Stock Issues, ActionMethod: getStkIss.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchStkRet(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      SearchQueryBuilder qb = new SearchQueryBuilder(con, USER_RETURN_EXT_QUERY_FIELDS,
          USER_RETURN_EXT_QUERY_COUNT, USER_RETURN__EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("user_return_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String PATIENT_RETURN_EXT_QUERY_FIELDS = "SELECT *";

  private static final String PATIENT_RETURN_EXT_QUERY_COUNT = " SELECT count(USER_RETURN_NO) ";

  private static final String PATIENT_RETURN__EXT_QUERY_TABLES = " FROM (SELECT USER_RETURN_NO,"
      + " SIRM.USER_ISSUE_NO,SIRM.DATE_TIME AS RETURN_DATE,"
      + " GDF.DEPT_NAME AS TO_STORE,(PD.PATIENT_NAME ||'  ' ||PD.LAST_NAME) AS RETURNED_BY,"
      + " SIRM.USERNAME,PRE.MR_NO FROM STORE_ISSUE_RETURNS_MAIN  SIRM JOIN  STORES GDF ON  "
      + " SIRM.DEPT_TO= GDF.DEPT_ID JOIN STORES GDT ON SIRM.DEPT_TO=GDT.DEPT_ID "
      + " JOIN PATIENT_REGISTRATION PRE ON SIRM.RETURNED_BY = PRE.PATIENT_ID "
      + " JOIN PATIENT_DETAILS PD ON PD.MR_NO = PRE.MR_NO "
      + " WHERE (patient_confidentiality_check(PD.patient_group,PD.mr_no))) as Foo";

  /**
   * Screen: View Stock Issues, ActionMethod: searchPatientStkRet.
   * 
   * @param filter Map
   * @param listing Map
   * @return PagedList PagedList
   * @throws SQLException SQLException
   * @throws ParseException ParseException
   */
  public static PagedList searchPatientStkRet(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, PATIENT_RETURN_EXT_QUERY_FIELDS,
          PATIENT_RETURN_EXT_QUERY_COUNT, PATIENT_RETURN__EXT_QUERY_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("user_return_no");
      qb.build();

      PagedList list = qb.getMappedPagedList();

      qb.close();

      return list;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  // check whether the super store array has some elements overlapping with multi_store array

  private static final String MULTI_STORE_WITH_SUPER_STORE_ACCESS = " SELECT *,"
      + " string_to_array(multi_store,',') FROM u_user "
      + " WHERE string_to_array(multi_store,',') && ( "
      + " SELECT ARRAY(SELECT (dept_id::text) from stores where is_super_store='Y') )"
      + " AND emp_username = ? ";

  /**
   * method hasMultiStoreWithSuperStore.
   * 
   * @param userName userName
   * @return boolean boolean
   * @throws SQLException SQLException
   */
  public static boolean hasMultiStoreWithSuperStore(String userName) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(MULTI_STORE_WITH_SUPER_STORE_ACCESS);
      ps.setString(1, userName);
      DynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      if (bean == null) {
        return false;
      } else {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * This method is used to get assigned store id of impersonate center.
   * 
   * @param centerId CenterId
   * @param multiStores String
   * @return String String
   * @throws SQLException SQLException
   * 
   */
  public String getAccessableStoresOfCenter(int centerId, String multiStores) throws SQLException {

    String[] storesArray = multiStores.split("\\,");
    StringBuilder builder = new StringBuilder("SELECT dept_id FROM stores WHERE center_id = ? ");
    DataBaseUtil.addWhereFieldInList(builder, "dept_id", Arrays.asList(storesArray), true);

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(builder.toString());

      int i1 = 1;
      ps.setInt(i1++, centerId);
      if (storesArray != null) {
        for (String val : storesArray) {
          ps.setInt(i1++, Integer.valueOf(val.trim()));
        }
      }
      BasicDynaBean storeBean = DataBaseUtil.queryToDynaBean(ps);

      return (storeBean == null ? null : String.valueOf(storeBean.get("dept_id")));
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  private static final String GET_USER_ISSUE_DETAILS = "select distinct sibd.batch_no,"
      + " to_char(sibd.exp_dt,'Mon-YYYY')as exp_dt, s.dept_name as from_store, "
      + " sitd.medicine_name, to_char(sim.date_time, "
      + " 'DD-MM-YYYY HH:MI AM')as date,sim.username,sid.qty,sim.reference,sid.user_issue_no,"
      + " sim.user_type,sim.issued_to,coalesce(sm.salutation||' '||pd.patient_name||' "
      + "'||pd.middle_name||' '||pd.last_name,'') as patient_name,"
      + " round((sid.amount*sid.qty),2) as amount,sitd.issue_units, case when scm.billable='t'"
      + " then 'true' else 'false' end as billable,"
      + " case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable' when "
      + " issue_type='R'  then 'Retailable' else 'Reusable' end as issue_type,"
      + " case when consignment_stock='t' then 'Consignment' else 'Normal' end as stocktype, "
      + " sid.indent_no,sitd.cust_item_code  from stock_issue_main sim join stock_issue_details"
      + " sid using(user_issue_no) JOIN store_item_batch_details sibd USING(item_batch_id) "
      + " left outer join patient_details pd on pd.mr_no=sim.issued_to "
      + " join  store_item_details sitd  on(sitd.medicine_id=sid.medicine_id) "
      + " join store_category_master scm ON (scm.category_id = sitd.med_category_id) "
      + " join stores s on(s.dept_id = sim.dept_from) "
      + " join (SELECT consignment_stock,item_batch_id FROM store_stock_details GROUP BY"
      + " item_batch_id,consignment_stock) as ssd  on (ssd.item_batch_id = sid.item_batch_id) "
      + " left join patient_registration pr on (pd.mr_no=pr.mr_no and pr.status='A') "
      + " left join salutation_master sm on (sm.salutation_id = pd.salutation) "
      + " where  sid.user_issue_no=? AND ( patient_confidentiality_check(COALESCE("
      + " pd.patient_group, 0),pd.mr_no) ) order by billable desc";

  /**
   * Get user issue details.
   * 
   * @param issueNo int
   * @return List issue details
   * @throws SQLException SQLException
   */
  public static List<BasicDynaBean> getUserIssueDetails(int issueNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_USER_ISSUE_DETAILS);
      ps.setInt(1, issueNo);
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return list;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  private static final String GET_USER_ISSUE_RETURN_DETAILS = "select distinct sibd.batch_no,"
      + " to_char(sibd.exp_dt,'Mon-YYYY')as exp_dt, s.dept_name as to_store, "
      + " sitd.medicine_name,sitd.cust_item_code, "
      + " sirm.dept_to,to_char(sirm.date_time, 'DD-MM-YYYY HH:MI AM')as date,sirm.username,"
      + " sir.qty,sirm.reference,sirm.user_return_no, sirm.returned_by ,coalesce(sm.salutation||'"
      + " '||pd.patient_name||' '||pd.middle_name||' '||pd.last_name,'') as patient_name, "
      + " round((amount*sir.qty),2) as amount, "
      + " case when issue_type='P' then 'Permanent' when issue_type='C' then 'Consumable'  when "
      + " issue_type='R' then 'Retailable' else 'Reusable' end as issue_type,"
      + " case when consignment_stock='t' then 'Consignment' else 'Normal' end as stocktype "
      + " from store_issue_returns_main sirm join store_issue_returns_details sir "
      + " using(user_return_no)  JOIN store_item_batch_details sibd USING(item_batch_id) "
      + " left outer join patient_details pd on pd.mr_no=sirm.returned_by "
      + " join store_item_details  sitd on(sitd.medicine_id = sir.medicine_id) "
      + " join store_category_master scm ON (scm.category_id = sitd.med_category_id) "
      + " join stores s on(s.dept_id = sirm.dept_to) "
      + " join store_stock_details ssd on (ssd.dept_id=dept_to and ssd.medicine_id= "
      + " sir.medicine_id and sir.batch_no=ssd.batch_no)  left join salutation_master"
      + " sm ON (sm.salutation_id = pd.salutation) where sirm.user_return_no=? "
      + " AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )";

  /**
   * Get Issue Return Details.
   * 
   * @param returnNo int
   * @return List List of IssueReturn Details
   * @throws SQLException SQLException
   */
  public static List<BasicDynaBean> getUserIssueReturnDetails(int returnNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_USER_ISSUE_RETURN_DETAILS);
      ps.setInt(1, returnNo);
      List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() > 0) {
        return list;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

}
