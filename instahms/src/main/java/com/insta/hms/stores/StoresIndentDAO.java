package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StoresIndentDAO extends GenericDAO {

  Connection con = null;
  public StoresIndentDAO() {
    super("store_indent_details");
  }

  private static final String INDENT_FIELDS = "SELECT *  ";
  private static final String INDENT_COUNT = "SELECT count(indent_no) ";
  private static final String INDENT_TABLES = " FROM (SELECT requesting_center_id,indent_no,date_time,status,indent_type,expected_date,requester_name,indent_store, "
      + " (select approved_by from store_indent_details pi where pi.indent_no=pim.indent_no limit 1) as approved_by, "
      + "(select approved_time from store_indent_details pi where pi.indent_no=pim.indent_no limit 1) as approved_time, pim.location_type, "
      + " CASE WHEN pim.location_type='D' THEN (select dept_name from department where dept_id = pim.dept_from) "
      + " WHEN pim.location_type = 'W' THEN (select ward_name from ward_names where ward_no = pim.dept_from) "
      + "ELSE (select dept_name::text from stores where dept_id::text = pim.dept_from) END as dept_from_name, pim.dept_from, "
      + " case when pim.location_type='D' then (select dept_id from department where dept_id = pim.dept_from) else '' end as dept, "
      + " case when pim.location_type='W' then (select ward_no from ward_names where ward_no = pim.dept_from) else '' end as ward, "
      + "( SELECT TEXTCAT_COMMACAT(medicine_name) FROM store_indent_details pd "
      + "WHERE  (pd.indent_no = pim.indent_no) GROUP BY indent_no) AS item_name "
      + "FROM store_indent_main pim ) foo";
  private static final String USER_INDENT_TABLES = " FROM (SELECT requesting_center_id,indent_no,date_time,status,indent_type,expected_date,requester_name,indent_store, "
      + " (select approved_by from store_indent_details pi where pi.indent_no=pim.indent_no limit 1) as approved_by, "
      + "(select approved_time from store_indent_details pi where pi.indent_no=pim.indent_no limit 1) as approved_time, pim.location_type, "
      + " CASE WHEN pim.location_type='D' THEN (select dept_name from department where dept_id = pim.dept_from) "
      + " WHEN pim.location_type = 'W' THEN (select ward_name from ward_names where ward_no = pim.dept_from) "
      + "ELSE (select dept_name::text from stores where dept_id::text = pim.dept_from) END as dept_from_name, pim.dept_from, "
      + " case when pim.location_type='D' then (select dept_id from department where dept_id = pim.dept_from) else '' end as dept, "
      + " case when pim.location_type='W' then (select ward_no from ward_names where ward_no = pim.dept_from) else '' end as ward, "
      + "( SELECT TEXTCAT_COMMACAT(medicine_name) FROM store_indent_details pd "
      + "WHERE  (pd.indent_no = pim.indent_no) GROUP BY indent_no) AS item_name "
      + "FROM store_indent_main pim where pim.indent_type = 'U') foo";

  private static final String REC_INDENT_FIELDS = "select * ";
  private static final String REC_INDENT_COUNT = "SELECT count(*) ";

  private static final String REC_INDENT_TABLES = " FROM (SELECT pim.indent_no,pim.date_time,pim.status,indent_type,pim.expected_date,pim.requester_name, "
      + "pim.approved_by,pim.indent_store as indent_store,pim.dept_from,pim.updated_date as processed_date,pim.requesting_center_id "
      + "FROM store_indent_main pim  " + "WHERE pim.indent_type = 'S' and exists "
      + "(select indent_no from store_indent_details sid where status IN ('T', 'A') and qty_fullfilled > 0 and sid.indent_no = pim.indent_no)) as foo ";

  private static final String REJ_INDENT_COUNT = "select count(*) ";
  private static final String REJ_INDENT_FIELDS = "select * ";
  private static final String REJ_INDENT_TABLES = " FROM (SELECT sim.indent_no,sim.updated_date as date_time,sim.expected_date,sim.status,sim.indent_store, "
      + " sim.requester_name, s.dept_name as rej_store_name, std.qty_rejected, "
      + " std.medicine_id, sitd.medicine_name, sibd.batch_no,sibd.exp_dt, sim.dept_from, std.item_batch_id ,std.transfer_detail_no, "
      + " std.received_date,std.received_by " + " FROM store_indent_main sim "
      + "	 JOIN store_transfer_details std ON (std.indent_no = sim.indent_no) "
      + "  JOIN stores s ON (sim.dept_from::Integer = s.dept_id) "
      + "  JOIN store_item_details sitd ON (sitd.medicine_id = std.medicine_id)  "
      + "  JOIN store_item_batch_details sibd ON (sibd.item_batch_id = std.item_batch_id) "
      + " where std.is_rejected_qty_taken = 'N' AND sim.indent_type = 'S' AND std.qty_rejected > 0"
      + " GROUP BY sim.indent_no,sim.updated_date,sim.status,std.medicine_id,sim.expected_date,sim.indent_store,sim.requester_name,std.item_batch_id, "
      + " sitd.medicine_name, sibd.batch_no,sibd.exp_dt, sim.dept_from,s.dept_name,std.qty_rejected,std.transfer_detail_no) as foo ";

  public static PagedList searchReqCentersIndentList(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, INDENT_FIELDS, INDENT_COUNT,
          INDENT_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      qb.addSecondarySort("indent_no");
      if (RequestContext.getCenterId() != 0)
        qb.addFilter(SearchQueryBuilder.INTEGER, "requesting_center_id", "=",
            RequestContext.getCenterId());
      qb.build();

      PagedList l = qb.getMappedPagedList();

      qb.close();
      con.close();

      return l;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  public static PagedList searchIndentList(Map filter, Map listing, String indentStores)
      throws SQLException, ParseException {
    return searchIndentList(filter, listing, indentStores, null);
  }

  public static PagedList searchIndentList(Map filter, Map listing, String indentStores,
      String requestStores) throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      List<Integer> valueList = new ArrayList<Integer>();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, INDENT_FIELDS, INDENT_COUNT,
          INDENT_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      if (indentStores != null) {
        if (indentStores.contains(",")) {
          String[] userStoresArr = indentStores.split(",");
          for (String store : userStoresArr) {
            valueList.add(Integer.parseInt(store));
          }
        } else {
          valueList.add(Integer.parseInt(indentStores));
        }
        qb.addFilter(qb.INTEGER, "indent_store", "IN", valueList);
      }
      if (requestStores != null) {
        List<String> storeList = new ArrayList<String>();
        if (requestStores.contains(",")) {
          String[] userStoresArr = requestStores.split(",");
          for (String store : userStoresArr) {
            storeList.add(store);
          }
        } else {
          storeList.add(requestStores);
        }
        qb.addFilter(qb.STRING, "dept_from", "IN", storeList);
      }

      qb.addSecondarySort("indent_no");
      qb.build();

      PagedList l = qb.getMappedPagedList();

      qb.close();
      con.close();

      return l;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  public static PagedList searchUserIndentList(Map filter, Map listing, String indentStores)
      throws SQLException, ParseException {
    return searchUserIndentList(filter, listing, indentStores, null, null);
  }

  public static PagedList searchUserIndentList(Map filter, Map listing, String indentStores,
      String requestStores, String indentApprovalBy) throws SQLException, ParseException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      List<Integer> valueList = new ArrayList<Integer>();

      SearchQueryBuilder qb = new SearchQueryBuilder(con, INDENT_FIELDS, INDENT_COUNT,
          USER_INDENT_TABLES, listing);

      qb.addFilterFromParamMap(filter);
      if (indentStores != null) {
        if (indentStores.contains(",")) {
          String[] userStoresArr = indentStores.split(",");
          for (String store : userStoresArr) {
            valueList.add(Integer.parseInt(store));
          }
        } else {
          valueList.add(Integer.parseInt(indentStores));
        }
        qb.addFilter(qb.INTEGER, "indent_store", "IN", valueList);
      }
      if (requestStores != null) {
        List<String> storeList = new ArrayList<String>();
        if (requestStores.contains(",")) {
          String[] userStoresArr = requestStores.split(",");
          for (String store : userStoresArr) {
            storeList.add(store);
          }
        } else {
          storeList.add(requestStores);
        }
        qb.addFilter(qb.STRING, "dept_from", "IN", storeList);
      }
      if (GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents().equals("N")
          && ("R".equalsIgnoreCase(indentApprovalBy))) {
        if (RequestContext.getCenterId() != 0)
          qb.addFilter(qb.INTEGER, "requesting_center_id", "=", RequestContext.getCenterId());
      }
      qb.addSecondarySort("indent_no");
      qb.build();

      PagedList l = qb.getMappedPagedList();

      qb.close();
      con.close();

      return l;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  public static PagedList searchReceivedIndentList(Map filter, Map listing, String UserStores)
      throws SQLException, ParseException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    List<String> valueList = new ArrayList<String>();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, REC_INDENT_FIELDS, REC_INDENT_COUNT,
        REC_INDENT_TABLES, listing);
    if (UserStores != null) {
      if (UserStores.contains(",")) {
        String[] userStoresArr = UserStores.split(",");
        for (String store : userStoresArr) {
          valueList.add(store);
        }
      } else {
        valueList.add(UserStores);
      }
      qb.addFilter(qb.STRING, "dept_from", "IN", valueList);
    }
    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("indent_no", true);
    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }

  public static PagedList searchRejectedIndentList(Map filter, Map listing)
      throws SQLException, ParseException {
    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, REJ_INDENT_FIELDS, REJ_INDENT_COUNT,
        REJ_INDENT_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("medicine_name");
    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }

  public static String getStoreName(int store_id) {
    return DataBaseUtil.getStringValueFromDb("SELECT dept_name from stores WHERE dept_id=" + store_id);
  }

  private final static String ITEM_DETAILS = "SELECT pmd.medicine_id,issue_type,identification,package_type,"
      + " coalesce((select sum(qty) from store_stock_details where dept_id = ? and medicine_id= ?),0) "
      + "   as qty_avbl, "
      + " coalesce((select sum(qty) from store_stock_details where dept_id = ? and medicine_id = ?), 0) as qty_avbl_for_reqstore,"
      + " issue_base_unit, issue_units,category,medicine_name,package_uom,m.manf_name,package_uom"
      + " FROM store_item_details pmd " + " JOIN manf_master m ON ( pmd.manf_name = m.manf_code )"
      + " join store_category_master on med_category_id=category_id WHERE pmd.medicine_id= ?";

  public static List<BasicDynaBean> getItemDetails(String item_name, int store_id, int req_store_id)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      String itemid = StoresDBTablesUtil.itemNameToId(item_name);
      if (itemid != null && !itemid.equals("")) {
        ps = con.prepareStatement(ITEM_DETAILS);
        ps.setInt(1, store_id);
        ps.setInt(2, Integer.parseInt(itemid));
        ps.setInt(3, req_store_id);
        ps.setInt(4, Integer.parseInt(itemid));
        ps.setInt(5, Integer.parseInt(itemid));
        l = DataBaseUtil.queryToDynaList(ps);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }

  private final static String INDENT_ITEMS = "SELECT indent_no, i.medicine_id ,i.medicine_name, qty, qty_fullfilled,"
      + " i.status, approved_by,approved_time, issue_type,identification,package_type, issue_units, issue_base_unit,"
      + " coalesce((select sum(qty) from store_stock_details where dept_id = ? and medicine_id=i.medicine_id),0) as availableqty"
      + " FROM store_indent_details i" + " LEFT JOIN store_item_details itd using (medicine_id)"
      + " LEFT JOIN store_category_master on category_id=med_category_id" + " WHERE indent_no = ?";

  private final static String INDENT_ITEMS_WITH_REQUIRED_STORE_QTY = "SELECT indent_no, i.medicine_id ,i.medicine_name, qty, qty_fullfilled,"
      + " i.status, approved_by,approved_time, issue_type,identification,package_type, issue_units, issue_base_unit,"
      + " coalesce((select sum(qty) from store_stock_details where dept_id = ? and medicine_id=i.medicine_id),0) as availableqty,"
      + " coalesce((select sum(qty) from store_stock_details where dept_id = ? and medicine_id=i.medicine_id),0) as availableqtyfor_reqstore"
      + " FROM store_indent_details i" + " LEFT JOIN store_item_details itd using (medicine_id)"
      + " LEFT JOIN store_category_master on category_id=med_category_id" + " WHERE indent_no = ?";

  public static List<BasicDynaBean> getIndentItemDetails(int indent_no, int store_id)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      BasicDynaBean indentTypeBean = new GenericDAO("store_indent_main").findByKey(con, "indent_no",
          indent_no);
      String indentType = (String) indentTypeBean.get("indent_type");
      if (indentType.equals("S")) {
        Integer reqStoreId = Integer.parseInt((String) indentTypeBean.get("dept_from"));

        ps = con.prepareStatement(INDENT_ITEMS_WITH_REQUIRED_STORE_QTY);
        ps.setInt(1, store_id);
        ps.setInt(2, reqStoreId);
        ps.setInt(3, indent_no);
      } else {
        ps = con.prepareStatement(INDENT_ITEMS);
        ps.setInt(1, store_id);
        ps.setInt(2, indent_no);
      }

      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }
  private static final String INDENT_DETAILS = "SELECT indent_no, date_time, indent_type, dept_from, requester_name, "
      + " expected_date, remarks, im.status, store_user, updated_date, closure_reasons,d.dept_name,location_type,wn.ward_name,"
      + " approver_remarks,indent_store, s.dept_name as store_name,sid.cust_item_code "
      + " FROM store_indent_main im " + " JOIN store_indent_details i using(indent_no) "
      + " LEFT JOIN store_item_details sid USING(medicine_id) "
      + " LEFT JOIN stores s on(s.dept_id =  indent_store )"
      + " LEFT JOIN department d on(d.dept_id =  dept_from)"
      + " LEFT JOIN ward_names wn on(wn.ward_no =  dept_from)" + " WHERE indent_no = ?";

  public static BasicDynaBean getIndentApprovalRejectDetails(int indent_no) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(INDENT_DETAILS);
      ps.setInt(1, indent_no);
      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return (BasicDynaBean) l.get(0);
  }

  private static final String INDENT_ITEMS_PROCESS = "SELECT i.indent_no,i.medicine_id, i.medicine_name, i.qty, i.qty_fullfilled,"
      + " coalesce(" + "	nullif(" + "			(select textcat_commacat(sibd.batch_no||'('||qty||')') "
      + "				from stock_issue_details sid "
      + "				JOIN store_item_batch_details sibd USING(item_batch_id) "
      + "			where indent_no =i.indent_no and sid.medicine_id=i.medicine_id), ''" + "		),"
      + "	(select textcat_commacat(sibd.batch_no||'('||qty||')') from store_transfer_details std "
      + "				JOIN store_item_batch_details sibd USING(item_batch_id) "
      + " 			where indent_no =i.indent_no and std.medicine_id=i.medicine_id)" + ") as batch_no,"
      + " coalesce(" + "	nullif(" + "			(select sum(sibd.mrp *sid.qty) "
      + "				from stock_issue_details sid "
      + "				JOIN store_item_batch_details sibd USING(item_batch_id) "
      + "			where indent_no =i.indent_no and sid.medicine_id=i.medicine_id), 0" + "		),"
      + "	(select sum(sibd.mrp*std.qty) from store_transfer_details std "
      + "				JOIN store_item_batch_details sibd USING(item_batch_id) "
      + " 			where indent_no =i.indent_no and std.medicine_id=i.medicine_id)" + ") as total_mrp,"
      + " coalesce(" + "	nullif(" + "			(select sum(sid.cost_value) "
      + "				from stock_issue_details sid "
      + "				JOIN store_item_batch_details sibd USING(item_batch_id) "
      + "			where indent_no =i.indent_no and sid.medicine_id=i.medicine_id), 0" + "		),"
      + "	(select sum(std.cost_value) from store_transfer_details std "
      + "				JOIN store_item_batch_details sibd USING(item_batch_id) "
      + " 			where indent_no =i.indent_no and std.medicine_id=i.medicine_id)"
      + ") as cost_value,"
      + " i,i.status, i.approved_by, i.approved_time,coalesce(iv.availableqty,0) as availableqty,"
      + " coalesce(iv1.availableqty,0) as req_availableqty,"
      + " scm.issue_type,indent_type,billable,purchase_flag, issue_base_unit, issue_units,"
      + " coalesce( "
      + "   (SELECT SUM(qty_req-qty_received) FROM store_po pod JOIN store_po_main pom USING (po_no)"
      + "     WHERE medicine_id = i.medicine_id AND pom.status = 'A' AND pod.status != 'R') "
      + "   ,0) AS poqty, " + " coalesce( "
      + "   (SELECT SUM(bonus_qty_req-bonus_qty_received) FROM store_po pod "
      + "     JOIN store_po_main pom USING (po_no)"
      + "     WHERE medicine_id = i.medicine_id AND pom.status = 'A' AND pod.status != 'R') "
      + "   ,0) AS pobqty, "
      + " case when i.status='A' then 1 when i.status='T' then 2 when i.status='C' then 3 else 4 end as show,"
      + " sitd.issue_units,sitd.cust_item_code " + "  FROM store_indent_main "
      + "  JOIN store_indent_details i using(indent_no) "
      + "  LEFT JOIN store_item_details sitd using(medicine_id) "
      + "  LEFT JOIN store_category_master scm on category_id= med_category_id"
      + " LEFT JOIN store_itemqty_view iv ON iv.medicine_id = i.medicine_id AND iv.dept_id = ? "
      + " LEFT JOIN store_itemqty_view iv1 ON iv1.medicine_id = i.medicine_id AND iv1.dept_id = ? "
      + " WHERE i.indent_no = ? ";

  public static List<BasicDynaBean> getIndentItemDetailsForProcess(int indent_no, int store_id,
      String type, int reqStore) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      if (type.equalsIgnoreCase("print"))
        ps = con.prepareStatement(INDENT_ITEMS_PROCESS + " and i.status != 'R' order by show");
      else
        ps = con.prepareStatement(INDENT_ITEMS_PROCESS + " order by show");
      ps.setInt(1, store_id);
      ps.setInt(2, reqStore);
      ps.setInt(3, indent_no);
      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }

  private static final String INDENT_ITEMS_RECEIPT = "SELECT i.indent_no, i.medicine_id, i.medicine_name, i.qty, i.qty_fullfilled, "
      + " (select sum(td.qty_recd) from store_stock_transfer_view td "
      + "  join ( SELECT item_batch_id,dept_id FROM store_stock_details "
      + "	WHERE dept_id = sim.indent_store AND medicine_id = i.medicine_id GROUP BY item_batch_id,dept_id) as sd "
      + "				on td.item_batch_id = sd.item_batch_id and td.store_from = sd.dept_id "
      + "  where td.indent_no = i.indent_no and td.medicine_id = i.medicine_id) as qty_recd, "
      + " (select sum(td.qty_rejected) from store_stock_transfer_view td "
      + "   join (SELECT item_batch_id,dept_id FROM store_stock_details "
      + "	WHERE dept_id = sim.indent_store AND medicine_id = i.medicine_id "
      + "	GROUP BY item_batch_id,dept_id) sd on td.item_batch_id = sd.item_batch_id and td.store_from = sd.dept_id "
      + "  where td.indent_no = i.indent_no and td.medicine_id = i.medicine_id) as qty_rej, "
      + " CASE WHEN i.status = 'O' THEN 'Open' WHEN i.status = 'A' THEN 'Approved' "
      + "  WHEN i.status = 'R' THEN 'Rejected' WHEN i.status = 'T' THEN 'Transfered' "
      + "  WHEN i.status='C' THEN 'Closed' ELSE 'Fulfilled' " + "  END as status, "
      + " i.approved_by, i.approved_time, indent_type, issue_base_unit, issue_units, identification "
      + "FROM store_indent_main sim " + " JOIN store_indent_details i using(indent_no) "
      + " LEFT JOIN store_item_details USING(medicine_id) "
      + " LEFT JOIN store_category_master ic on ic.category_id=med_category_id "
      + " WHERE sim.dept_from = ? and i.indent_no = ? and (indent_type='S') AND i.status != 'R' "
      + " ORDER BY i.medicine_name";

  public static List<BasicDynaBean> getIndentItemDetailsForReceipt(int indent_no, int store_id)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> l = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(INDENT_ITEMS_RECEIPT);
      ps.setString(1, String.valueOf(store_id));
      ps.setInt(2, indent_no);
      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }

  private static final String GET_INDENT_ITEMS = "SELECT medicine_id,identification,cust_item_code FROM store_indent_details "
      + " LEFT JOIN store_item_details USING (medicine_id)"
      + " LEFT JOIN store_category_master on category_id=med_category_id"
      + " WHERE indent_no = ? AND medicine_id !=0 ";

  private static final String GET_ITEM_IDENTIFIERS = "SELECT distinct sibd.batch_no,sum(qty) as qty,sibd.exp_dt,sum(qty_in_use) as qty_in_use ,sid.issue_base_unit,sibd.item_batch_id,sid.cust_item_code "
      + " FROM store_stock_details ssd "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN store_item_details sid ON(sid.medicine_id = ssd.medicine_id)"
      + "WHERE ssd.medicine_id = ? and dept_id = ? and qty > 0 and asset_approved = 'Y' ";

  private static final String GET_ITEM_GROUP_BY = "GROUP BY sibd.batch_no,sibd.exp_dt,sid.issue_base_unit,sibd.item_batch_id,sid.cust_item_code ORDER BY exp_dt";

  public static List getItemIdentifierDetails(int indent_no, int store_id) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    List l = new ArrayList<HashMap>();
    Map itemidentifiermap = null;
    String saleOfExpiredItemspref = GenericPreferencesDAO.getGenericPreferences()
        .getSaleOfExpiredItems();
    String query = GET_ITEM_IDENTIFIERS;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_INDENT_ITEMS);
      ps.setInt(1, indent_no);
      List itemlist = DataBaseUtil.queryToArrayList(ps);

      Iterator<Hashtable<String, String>> itemitr = itemlist.iterator();
      ps = con.prepareStatement(query + GET_ITEM_GROUP_BY);

      while (itemitr.hasNext()) {
        Hashtable ht = itemitr.next();
        ps.setInt(1, Integer.parseInt(ht.get("MEDICINE_ID").toString()));
        ps.setInt(2, store_id);
        ArrayList identifierList = DataBaseUtil.queryToArrayList(ps);
        itemidentifiermap = new HashMap();
        itemidentifiermap.put("MEDICINE_ID", ht.get("MEDICINE_ID").toString());
        itemidentifiermap.put("ITEM_IDENTIFICATION", ht.get("IDENTIFICATION").toString());
        itemidentifiermap.put("IDENTIFIER_LIST", identifierList);
        l.add(itemidentifiermap);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }

  private static final String GET_INDENT_TRANSFERS = " SELECT id.medicine_id, transfer_no,transfer_detail_no, sibd.batch_no, td.qty, td.qty_recd, td.qty_rejected,td.item_batch_id "
      + " FROM store_indent_details id "
      + "   JOIN store_transfer_details td ON (td.indent_no = id.indent_no "
      + "     AND td.medicine_id = id.medicine_id) "
      + "   JOIN store_item_batch_details sibd USING(item_batch_id) " + " WHERE id.indent_no =? ";

  public static List getIndentTransfers(int indent_no) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_INDENT_TRANSFERS, indent_no);
  }

  private static final String DELETE_INDENT = "DELETE FROM store_indent_details WHERE indent_no=? and medicine_name=? ";

  public static boolean deleteIndent(Connection con, List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(DELETE_INDENT);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        int index = 0;
        BasicDynaBean bean = (BasicDynaBean) iterator.next();
        ps.setInt(++index, (Integer) bean.get("indent_no"));
        ps.setString(++index, (String) bean.get("medicine_name"));
        ps.addBatch();
      }
      int results[] = ps.executeBatch();
      for (int p = 0; p < results.length; p++) {
        if (results[p] <= 0) {
          success = false;
          break;
        }
      }
    }
    return success;
  }

  private static final String GET_INDENT_NO = "select indent_no, medicine_id from store_indent_details where po_no ~ ?";

  public static List<BasicDynaBean> getIndentNos(String poNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INDENT_NO);
      ps.setString(1, poNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private final static String ORDERKIT_ITEM_DETAILS = "SELECT okd.medicine_id,okd.qty_needed, "
      + " SUM(CASE dept_id WHEN ? THEN coalesce(qty,0) ELSE 0 END) AS indent_store_qty, "
      + " SUM(CASE dept_id WHEN ? THEN coalesce(qty,0) ELSE 0 END) AS req_store_qty, "
      + " issue_type,package_type,identification,issue_base_unit, "
      + " issue_units,category,medicine_name,std.package_uom,m.manf_name "
      + " FROM store_stock_details " + " JOIN order_kit_details okd USING(medicine_id) "
      + " JOIN store_item_details std USING(medicine_id) "
      + " JOIN manf_master m ON ( std.manf_name = m.manf_code ) "
      + " JOIN store_category_master on med_category_id=category_id "
      + " where order_kit_id=? and dept_id IN(?,?) group by okd.medicine_id,okd.qty_needed,issue_type,package_type,identification,issue_base_unit, "
      + " issue_units,category,medicine_name,m.manf_name,std.package_uom";

  public static List<BasicDynaBean> getOrderKitItemDetails(int orderKitId, int indent_store_id,
      int req_store_id) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List l = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(ORDERKIT_ITEM_DETAILS);
      ps.setInt(1, indent_store_id);
      ps.setInt(2, req_store_id);
      ps.setInt(3, orderKitId);
      ps.setInt(4, indent_store_id);
      ps.setInt(5, req_store_id);
      l = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return l;
  }

}
