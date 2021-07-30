package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoresPOApprovalDAO extends GenericDAO {

  public StoresPOApprovalDAO() {
    super("store_po");
  }

  private static final String PO_FIELDS = "SELECT * ";
  private static final String PO_COUNT = "SELECT count(po_no) ";
  private static final String PO_TABLES = " FROM (SELECT pom.*,supplier_name,cust_supplier_code from store_po_main pom join "
      + " supplier_master on supplier_code=supplier_id) as foo ";

  private static final String PO_TABLES_CENTER_WISE = " FROM (SELECT pom.*,supplier_name,cust_supplier_code,center_id from store_po_main pom "
      + " join supplier_center_master scm on (scm.supplier_code=pom.supplier_id) "
      + " JOIN supplier_master sm on(sm.supplier_code=scm.supplier_code)) as foo ";

  public static PagedList searchIndentList(Map filter, Map<LISTING, Object> listing)
      throws SQLException, ParseException {
    Connection con = null;
    SearchQueryBuilder qb = null;
    List<Integer> list = new ArrayList<Integer>();
    int centerID = RequestContext.getCenterId();

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerID == 0) {
        qb = new SearchQueryBuilder(con, PO_FIELDS, PO_COUNT, PO_TABLES, listing);

        qb.addFilterFromParamMap(filter);

      } else {
        qb = new SearchQueryBuilder(con, PO_FIELDS, PO_COUNT, PO_TABLES_CENTER_WISE, listing);
        qb.addFilterFromParamMap(filter);
        list.add(0);
        list.add(centerID);
        qb.addFilter(qb.INTEGER, "center_id", "IN", list);
      }
      qb.addSecondarySort("po_no");
      qb.build();

      PagedList l = qb.getMappedPagedList();

      qb.close();
      con.close();

      return l;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  private static final String PO_DETAILS = "SELECT po_no, po_date, store_id, user_id, "
      + " pom.status, d.dept_name,approver_remarks,closure_reasons" + " FROM store_po_main pom"
      + " JOIN stores d on(d.dept_id =  store_id )" + " WHERE po_no = ?";

  public static BasicDynaBean getPOApprovalRejectDetails(String poNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(PO_DETAILS);
      ps.setString(1, poNo);
      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return (BasicDynaBean) l.get(0);
  }

  private final static String PO_ITEMS = "SELECT po_no, i.medicine_id ,medicine_name, qty_req as qty,"
      + " bonus_qty_req AS bonus_qty, qty_received,"
      + " i.status, issue_type,identification,item_remarks,"
      + " coalesce((select sum(qty) from store_stock_details where dept_id = ? "
      + " and medicine_id=i.medicine_id),0) as availableqty" + " FROM store_po i"
      + " JOIN store_item_details itd using (medicine_id)"
      + " JOIN store_category_master on category_id=med_category_id" + " WHERE po_no = ?";

  public static List<BasicDynaBean> getPOItemDetails(String poNo, int store_id)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(PO_ITEMS);
      ps.setInt(1, store_id);
      ps.setString(2, poNo);
      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }

  private static final String GET_ALL_SUPPLIERS = "SELECT supplier_code, supplier_name,cust_supplier_code "
      + " FROM supplier_master ORDER BY supplier_name";

  private static final String GET_ALL_CENTER_SUPPLIERS = "select sm.supplier_code,supplier_name,sm.supplier_address,coalesce(scm.center_id,0) as center_id,sm.cust_supplier_code, "
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city " + " from supplier_master sm  "
      + " left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + " where scm.status='A' and sm.status='A' and scm.center_id IN(?,0) order by sm.supplier_name ";

  public List<BasicDynaBean> listAllcentersforAPo(int centerId) throws SQLException {

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

  private String update_po_details = " UPDATE store_po SET status = ? WHERE po_no = ? ";
  public boolean updatePODetails(Connection con, ViewPO poDetail) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(update_po_details);
      ps.setString(1, poDetail.getStatus());
      ps.setString(2, poDetail.getPono());

      return ps.executeUpdate() > 0;

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
}
