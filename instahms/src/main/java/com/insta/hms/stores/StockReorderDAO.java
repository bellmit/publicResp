package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class StockReorderDAO {

  /*
   * The query structure for obtaining stock reorder is as follows
   *
   * OUTER SELECT clause ( INNER SELECT clause ( BASE SELECT clauses/queries to fetch : --Reorder
   * qty --Stock qty - Indent qty [+Filters -->purchase or otherwise] --Sales qty [+Filters -->sales
   * qty and period] --Purchase order qty [po raised] -- Consumption qty{=>sales+issues+transfers}
   * [+Filters] ) INNER CLAUSE JOINS with service groups and item details etc INNER CLAUSE filters
   * for Indent, consumption * and sales criteria ) OUTER CLAUSE END (+Filters for: Reorder,
   * supplier, service groups PO criteria and order qty>0)
   *
   * For easy reference, each of these sub-queries/clauses have been appended with prefix based on
   * their structural position in query. The filter clauses are used only when applicable.
   *
   */

  public static final Integer PAGE_SIZE = 100;

  protected static ArrayList<Integer> fieldTypes = new ArrayList<Integer>();

  protected static ArrayList<Object> fieldValues = new ArrayList<Object>();

  protected static PreparedStatement dataStmt;

  protected static PreparedStatement countStmt;

  // clause states
  static boolean whereNeeded = true;

  private static boolean intersectNeeded = false;

  // query builder clauses
  public static final String WHERE = " WHERE ";

  public static final String AND = " AND ";

  public static final String UNION_ALL = " UNION ALL ";

  public static final String INTERSECT = " INTERSECT ";

  /*
   * Outermost Select and count Statements(level 4)
   */
  public static final String OUTERMOST_SELECT = " SELECT *, " + " CASE WHEN min_level>0 THEN"
      + "   CASE WHEN availableqty< min_level THEN 'Y' " + "	ELSE 'N' END "
      + " ELSE 'N' END AS below_min_level, " + " CASE WHEN max_level>0 THEN"
      + "   CASE WHEN ord_qty> max_level THEN 'Y' " + "	ELSE 'N' END "
      + " ELSE 'N' END AS above_max_level " + "  FROM ( ";

  public static final String OUTER_COUNT = " SELECT COUNT(*) FROM  (  ";

  public static final String OUTER_OFFSET = " ORDER BY medicine_name LIMIT " + PAGE_SIZE
      + " OFFSET ? ";

  /*
   * Outer Select Queries (level 3)
   */
  public static final String OUTER_CONS_SELECT = " SELECT *,  "
      + " CASE WHEN  min_level IS NOT NULL AND min_level>0  THEN   "
      + " CASE WHEN (((consumedqty/?)* ?) -(availableqty)) < (min_level -(availableqty))   "
      + " THEN (min_level -(availableqty))   "
      + " ELSE (((consumedqty/?)* ?) -(availableqty))::numeric(15,2) END   " + " ELSE   "
      + "  (((consumedqty/?)* ?) -(availableqty))::numeric(15,2) END AS ord_qty  " + "	FROM  ( ";

  public static final String OUTER_INDENT_SELECT = " SELECT *, indentqty AS ord_qty  FROM  ( ";
  public static final String OUTER_PENDING_INDENT_SELECT = " SELECT *, (indentqty-availableqty) AS ord_qty FROM  ( ";
  public static final String OUTER_REORDER_DANGER_LEVEL_SELECT = " SELECT *, (danger_level-availableqty) AS ord_qty FROM  ( ";
  public static final String OUTER_REORDER_REORDER_LEVEL_SELECT = " SELECT *, (reorder_level-availableqty) AS ord_qty FROM  ( ";
  public static final String OUTER_REORDER_MINIMUM_LEVEL_SELECT = " SELECT *, (min_level-availableqty)AS ord_qty FROM  ( ";

  /*
   * Inner Queries (level 2)
   */
  public static final String INNER_SELECT = " SELECT "
      + " itd.medicine_id AS item_id,coalesce(SUM(stock_qty),0) AS availableqty, "
      + " SUM(indent_qty) AS indentqty,sum(po_qty) AS poqty, " + " SUM(pur_qty) AS flaggedqty,	 "
      + " SUM(consumption_qty) AS consumedqty,SUM(reorder_level) AS reorder_level, "
      + " SUM(danger_level) AS danger_level,SUM(max_level) AS max_level, "
      + " SUM(min_level) AS min_level, medicine_name, issue_base_unit AS pkg_size, "
      + " itps.supplier_code,itps.center_id as supplier_center_id,"
      + " coalesce(sup.supplier_name,'')  AS pref_supplier_name, seg.service_group_id,ssg.service_sub_group_id, "
      + " 0 as cost_price,store_center_id " + " FROM (  ";

  public static final String INNER_SELECT_CLOSE = " ) AS foo  "
      + " JOIN store_item_details itd USING(medicine_id)  "
      + " JOIN service_sub_groups ssg ON(ssg.service_sub_group_id = itd.service_sub_group_id) "
      + " JOIN service_groups seg ON(ssg.service_group_id = seg.service_group_id) "
      + " LEFT JOIN item_supplier_prefer_supplier itps ON (itps.medicine_id = itd.medicine_id AND itps.center_id = store_center_id)  "
      + " LEFT JOIN supplier_master sup ON (sup.supplier_code = itps.supplier_code) "
      + " LEFT JOIN supplier_center_master scm ON (sup.supplier_code = scm.supplier_code 	AND store_center_id = scm.center_id) "
      + " WHERE itd.status = 'A' " + " GROUP BY  "
      + " itd.medicine_id, medicine_name, issue_base_unit, itps.supplier_code, sup.supplier_name, itps.center_id, "
      + " seg.service_group_id, ssg.service_sub_group_id, store_center_id ";

  /*------Base Queries (level 1)----------
   * Query to fetch reorder levels
   */
  public static final String BASE_GET_REORDER = " SELECT r.medicine_id , SUM(min_level) AS min_level,   "
      + " 	SUM(max_level) AS max_level, SUM(danger_level) AS danger_level,   "
      + "  	SUM(reorder_level) AS reorder_level, 0 AS stock_qty,  "
      + " 	0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty,0 AS po_qty,0 AS consumption_qty,s.center_id as store_center_id  "
      + " FROM store_reorder_levels r  " + " JOIN stores s USING(dept_id) " + ""
      + " WHERE dept_id IN (%s) and reorder_level > 0   "
      + " GROUP BY medicine_id, min_level, max_level, danger_level, reorder_level, s.center_id  ";

  /*
   * Query to fetch current stock details
   */
  public static final String BASE_GET_STOCK = " SELECT ssd.medicine_id , 0 AS min_level, 0 AS max_level,   "
      + "     0 AS danger_level, 0 AS reorder_level , sum(qty) AS stock_qty,  "
      + "     0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty,0 AS po_qty,0 AS consumption_qty,s.center_id as store_center_id  "
      + " FROM store_stock_details ssd   "
      + " JOIN store_item_batch_details sibd USING(item_batch_id)"
      + " JOIN stores s USING(dept_id) " + " WHERE dept_id IN (%s) AND   "
      + " ( sibd.exp_dt >= current_date   OR sibd.exp_dt IS null ) "
      + " GROUP BY ssd.medicine_id, min_level, max_level, danger_level , reorder_level, s.center_id  ";

  /*
   * Query to fetch indents
   */
  public static final String BASE_GET_INDENT = " SELECT  medicine_id , 0 AS min_level, 0 AS max_level, 0 AS danger_level, 0 AS reorder_level ,  "
      + " 0 AS stock_qty, " + " SUM(qty-qty_fullfilled) AS indent_qty , 0 AS sales_qty, "
      + " COALESCE((select sum(pnd.qty - pnd.qty_fullfilled) WHERE purchase_flag='Y' AND pnd.po_no IS NULL),0) "
      + " AS pur_qty,0 AS po_qty,0 AS consumption_qty ,s.center_id as store_center_id "
      + " FROM store_indent_main pndm " + " JOIN stores s ON(s.dept_id = indent_store) "
      + " JOIN store_indent_details pnd USING (indent_no) "
      + " WHERE pnd.status::text <> 'R'::text AND pndm.status::text <> 'X'::text "
      + " AND indent_store IN (%s) ";

  /*
   * To filter by indent no, when specified
   */
  public static final String BASE_INDENT_NO_FILTER = " AND indent_no = ? ";

  /*
   * To filter by purchase flag date, when specified
   */
  public static final String BASE_INDENT_PURCHASE_FLAG_DATE_FILTER = " AND date(purchase_flag_date) = ? ";
  /*
   * To fetch only those indents which have been marked for purchase
   */
  public static final String BASE_PURCHASE_INDENT_ONLY_FILTER = " AND purchase_flag='Y' AND pnd.po_no IS NULL ";

  /*
   * To fetch indents based on their age.
   */
  public static final String BASE_INDENT_AGE_FILTER = "  AND date(date_time) BETWEEN current_date-? AND current_date  AND pnd.status!= 'R'  ";
  public static final String BASE_INDENT_GROUP_BY = " GROUP BY medicine_id, indent_no,purchase_flag,po_no,s.center_id ";

  /*
   * Query to get sales value (includes returns)
   */
  public static final String BASE_GET_SALE = " SELECT medicine_id,  0 AS min_level, 0 AS max_level, "
      + " 0 AS danger_level, 0 AS reorder_level , 0 AS stock_qty,  " + " 0 AS indent_qty, "
      + " sum(quantity) AS sales_qty , 0 AS pur_qty,0 AS po_qty,0 AS consumption_qty,s.center_id as store_center_id "
      + " FROM store_sales_main pmsm " + " JOIN store_sales_details pms USING (sale_id) "
      + " JOIN stores s ON(s.dept_id = store_id) " + " WHERE  " + " store_id IN (%s)  ";
  /*
   * To filter by sales date and qty
   */
  public static final String BASE_SALE_DAYS_FILTER = " AND DATE(date_time) BETWEEN current_date-? AND current_date ";
  public static final String BASE_SALE_QTY_FILTER_GROUP_BY = " GROUP BY medicine_id, s.center_id  "
      + " HAVING sum(quantity) > ? ";
  public static final String BASE_SALE_GROUP_BY = " GROUP BY medicine_id,s.center_id  ";

  /*
   * Query to fetch the PO's which are not force closed or cancelled
   */
  public static final String BASE_GET_PURCHASE_ORDER = " SELECT  medicine_id , 0 AS min_level, 0 AS max_level, "
      + " 0 AS danger_level, 0 AS reorder_level , 0 AS stock_qty,   "
      + " 0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty,"
      + " SUM(ROUND((po.qty_req+po.bonus_qty_req - po.qty_received- po.bonus_qty_received),2)) AS po_qty,   "
      + " 0 AS consumption_qty ,s.center_id as store_center_id " + " FROM store_po_main pom  "
      + " JOIN store_po po USING (po_no)   " + " JOIN stores s ON(s.dept_id = store_id) "
      + " WHERE store_id IN (%s) AND pom.status NOT IN('FC','X')  AND po.status NOT IN ('R') "
      + " GROUP BY medicine_id ,s.center_id  ";

  /*
   * Query to the consumption qty
   */
  public static final String BASE_GET_CONSUMPTION_QTY = " SELECT  medicine_id , 0 AS min_level, 0 AS max_level, "
      + " 0 AS danger_level, 0 AS reorder_level , 0 AS stock_qty,  "
      + " 0 AS indent_qty , 0 AS sales_qty, 0 AS pur_qty,0 AS po_qty,  "
      + " COALESCE(sum(qty),0) AS consumption_qty, s.center_id as store_center_id "
      + " FROM store_consumption_view  " + " JOIN stores s USING(dept_id) "
      + " WHERE dept_id IN (%s) and qty != 0::numeric ";
  /*
   * Filter for consumption age
   */
  public static final String BASE_CONSUMPTION_DAYS_FILTER = "  AND date(date_time) BETWEEN current_date-? AND current_date ";
  public static final String BASE_CONSUMPTION_DAYS_GROUP_BY = "   GROUP BY medicine_id,s.center_id ";

  /*
   * Inner Filter (level 2)
   */
  public static final String INNER_MED_FILTER = " HAVING itd.medicine_id IN (  ";
  public static final String INNER_MED_FILTER_CLOSE = " ))AS subodr) AS outer_query WHERE ord_qty>0 ";

  /*
   * Inner Indent Filter, to filter indents based on criteria selected
   */
  public static final String INNER_INDENT_FILTER = " SELECT  medicine_id "
      + " FROM store_indent_main pndm " + " JOIN store_indent_details pnd USING (indent_no) "
      + " JOIN stores s ON(s.dept_id = indent_store) "
      + " WHERE pnd.status::text <> 'R'::text AND pndm.status::text <> 'X'::text  "
      + " AND indent_store  IN (%s) ";

  public static final String INNER_INDENT_NO_SUB_FILTER = " AND indent_no = ? ";
  public static final String INNER_PURCHASE_INDENT_SUB_FILTER = " AND purchase_flag='Y' AND pnd.po_no IS NULL  ";
  public static final String INNER_PURCHASE_INDENT_DATE_SUB_FILTER = " AND date(purchase_flag_date)=?  ";
  public static final String INNER_INDENT_AGE_SUB_FILTER = " AND DATE(date_time) BETWEEN current_date-? AND current_date AND pnd.status != 'R' ";
  public static final String INNER_INDENT_GROUP_BY = "GROUP BY medicine_id, indent_no";

  /*
   * Inner Sales, to filter sales based on qty and age
   */
  public static final String INNER_SALES_FILTER = " SELECT medicine_id "
      + " FROM store_sales_main pmsm " + " JOIN store_sales_details pms USING (sale_id) "
      + " JOIN stores s ON(s.dept_id = store_id) "
      + " WHERE pmsm.type = 'S'::bpchar and store_id IN (%s) ";
  public static final String INNER_SALES_DAYS_SUB_FILTER = " AND date(date_time) BETWEEN current_date-? AND current_date ";
  public static final String INNER_SALES_GROUP_BY = " GROUP BY medicine_id ";
  public static final String INNER_SALES_QTY_GROUP_BY = " GROUP BY s.center_id, medicine_id  HAVING sum(quantity) > ? ";

  /*
   * Inner onsumption, to filter consumption based on qty and age
   */
  public static final String INNER_CONSUMPTION_FILTER = " SELECT  medicine_id "
      + " FROM store_consumption_view " + "  WHERE dept_id IN (%s) AND qty != 0::numeric ";
  public static final String INNER_CONSUMPTION_DAYS_SUB_FILTER = " AND DATE(date_time) BETWEEN current_date-? AND current_date ";
  public static final String INNER_CONSUMPTION_GROUP_BY = " GROUP BY medicine_id ";

  /*
   * Outer filters for: Reorder Levels,
   */
  public static final String OUTER_DANGER_LEVEL_FILTER = " danger_level>0 AND danger_level > availableqty ";
  public static final String OUTER_REORDER_LEVEL_FILTER = " reorder_level>0 AND reorder_level > availableqty ";
  public static final String OUTER_MINIMUM_LEVEL_FILTER = " min_level>0 AND min_level > availableqty ";

  /*
   * Outer filters for: supplier, service groups, sub groups, PO not raised.
   */
  public static final String OUTER_PREFERRED_SUPPLIER_FILTER = "  supplier_code = ? ";

  public static final String OUTER_SERVICE_GROUP_FILTER = "  service_group_id = ? ";

  public static final String OUTER_SERVICE_SUB_GROUP_FILTER = "  service_sub_group_id = ? ";

  public static final String OUTER_NOT_RAISED_IN_PO_FILTER = "  item_id " + "  NOT IN ( "
      + "  SELECT distinct medicine_id " + "  FROM store_po_main pom "
      + "  JOIN store_po po using(po_no) " + " JOIN stores s ON(s.dept_id = store_id) "
      + "  where pom.store_id IN (%s) " + "  AND pom.status IN ('O','A','AA','AO')  "
      + "  AND po.status IN ('A','O','P','AA','AO') " + "  ORDER BY medicine_id " + "  ) ";

  private static final String GET_ALL_SUPPLIERS = "SELECT supplier_code, supplier_name, cust_supplier_code "
      + " FROM supplier_master where status='A' ORDER BY supplier_name";

  public static final String GET_ALL_CENTER_SUPPLIERS = "select sm.supplier_code,supplier_name,sm.cust_supplier_code,coalesce(scm.center_id,0) as center_id "
      + "		from supplier_master sm  "
      + "		left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + " 	where scm.status='A' and sm.status='A' and scm.center_id IN(?,0) order by sm.supplier_name ";

  public static List<BasicDynaBean> listAllcentersforAPo(int centerId) throws SQLException {

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

  public Map getStringTypeValMap(HashMap typValMap) {
    if (typValMap == null || typValMap.isEmpty())
      return typValMap;
    HashMap newtypValMap = new HashMap();
    Integer[] types = (Integer[]) typValMap.get("type");
    Integer[] newTypes = new Integer[types.length];
    ArrayList values = (ArrayList) typValMap.get("value");
    ArrayList newValues = new ArrayList();
    for (int i = 0; i < types.length; i++) {
      newTypes[i] = QueryBuilder.STRING;
    }

    for (int i = 0; i < values.size(); i++) {
      newValues.add("" + String.valueOf(values.get(i)) + "");
    }
    newtypValMap.put("type", newTypes);
    newtypValMap.put("value", newValues);
    return newtypValMap;
  }

  public static String preparePlaceHolders(int length) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length;) {
      builder.append("?");
      if (++i < length) {
        builder.append(",");
      }
    }
    return builder.toString();
  }

  public static void setPlaceValues(PreparedStatement preparedStatement, Integer startIndex,
      Object[] values) throws SQLException {
    int start = startIndex != null ? startIndex : 0;
    for (int i = start; i < values.length; i++) {
      preparedStatement.setObject(i + 1, values[i]);
    }
  }

  public static void setFieldValueAndTypes(Object typValObj) {
    setFieldValueAndTypes((HashMap) typValObj);
  }

  public static void setFieldValueAndTypes(HashMap typValMap) {
    if (typValMap == null || typValMap.isEmpty())
      return;
    Integer[] types = (Integer[]) typValMap.get("type");
    if ((typValMap.get("value").getClass()).isAssignableFrom(java.util.ArrayList.class)) {
      List values = (List) typValMap.get("value");
      for (int i = 0; i < values.size(); i++) {
        fieldValues.add(values.get(i));
        fieldTypes.add(types[i]);
      }
    } else {
      fieldValues.add(typValMap.get("value"));
      fieldTypes.add(types[0]);
    }
  }

  public static void setFieldValueAndTypes(Object value, Integer type) {
    fieldValues.add(value);
    fieldTypes.add(type);
  }

  private static void resetStaticFields() {
    fieldTypes.clear();
    fieldValues.clear();
    whereNeeded = true;
    intersectNeeded = false;
    dataStmt = null;
    countStmt = null;
  }

  public PagedList getDynaPagedList(Integer pageNum) throws SQLException {
    try (ResultSet rsData = dataStmt.executeQuery();
        ResultSet rsCount = countStmt.executeQuery();) {

      RowSetDynaClass rsd = new RowSetDynaClass(rsData);
      List dataList = rsd.getRows();
      dataStmt.close();

      RowSetDynaClass rsc = new RowSetDynaClass(rsCount);
      BasicDynaBean countBean = (BasicDynaBean) rsc.getRows().get(0);
      countStmt.close();

      int totalCount = ((Long) countBean.get("count")).intValue();
      return new PagedList(dataList, totalCount, PAGE_SIZE, pageNum, countBean.getMap());
    }
  }

  PagedList getResultPagedList(String query, Integer pageNum) throws SQLException {
    Connection con = null;
    PagedList resultList = null;
    try {
      con = DataBaseUtil.getConnection();
      dataStmt = con.prepareStatement(OUTERMOST_SELECT + query + OUTER_OFFSET);
      countStmt = con.prepareStatement(OUTER_COUNT + query);

      int stmtIndex = 1;
      int numValues = fieldTypes.size();
      for (int i = 0; i < numValues; i++) {

        int type = (Integer) fieldTypes.get(i);
        Object value = fieldValues.get(i);
        if (value == null)
          continue;

        QueryBuilder.setTypeInStatement(dataStmt, stmtIndex, type, value);
        QueryBuilder.setTypeInStatement(countStmt, stmtIndex, type, value);
        stmtIndex++;
      }
      QueryBuilder.setTypeInStatement(dataStmt, stmtIndex, QueryBuilder.INTEGER, pageNum);
      resultList = getDynaPagedList(pageNum);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return resultList;
  }

  List getResultList(String query) throws SQLException {
    Connection con = null;
    List resultList = null;
    try {
      con = DataBaseUtil.getConnection();
      dataStmt = con.prepareStatement(
          OUTERMOST_SELECT + query + " ORDER BY pref_supplier_name, medicine_name ");

      int stmtIndex = 1;
      int numValues = fieldTypes.size();
      for (int i = 0; i < numValues; i++) {

        int type = (Integer) fieldTypes.get(i);
        Object value = fieldValues.get(i);
        if (value == null)
          continue;

        QueryBuilder.setTypeInStatement(dataStmt, stmtIndex, type, value);
        stmtIndex++;
      }

      resultList = DataBaseUtil.queryToDynaList(dataStmt);
    } finally {
      DataBaseUtil.closeConnections(con, dataStmt);
    }
    return resultList;
  }

  public static String addInnerFilters(String innerQuery, Map filterMap) {
    if (innerQuery == null || innerQuery.equals(""))
      return innerQuery;
    StringBuilder filterQuery = new StringBuilder();
    HashMap storesMap = (HashMap) filterMap.get("stores");
    List stores = (List) storesMap.get("value");

    filterQuery.append(innerQuery);
    if (filterMap.containsKey("salesQty") || filterMap.containsKey("salesDays")) {
      filterQuery.append(String.format(INNER_SALES_FILTER, preparePlaceHolders(stores.size())));
      setFieldValueAndTypes(storesMap);

      if (filterMap.containsKey("salesDays")) {
        filterQuery.append(INNER_SALES_DAYS_SUB_FILTER);
        setFieldValueAndTypes(filterMap.get("salesDays"));
      }

      if (filterMap.containsKey("salesQty")) {
        filterQuery.append(INNER_SALES_QTY_GROUP_BY);
        setFieldValueAndTypes(filterMap.get("salesQty"));
      } else {
        filterQuery.append(INNER_SALES_GROUP_BY);
      }
      intersectNeeded = true;
    }
    return filterQuery.toString();

  }

  public static String addOuterFilters(String outerQuery, Map filterMap) {
    if (outerQuery == null || outerQuery.equals(""))
      return outerQuery;
    StringBuilder filterQuery = new StringBuilder();
    HashMap storesMap = (HashMap) filterMap.get("stores");
    List stores = (List) storesMap.get("value");
    filterQuery.append(outerQuery);

    if (filterMap.containsKey("serviceGroup")) {
      filterQuery.append((whereNeeded ? WHERE : AND) + OUTER_SERVICE_GROUP_FILTER);
      whereNeeded = false;
      setFieldValueAndTypes(filterMap.get("serviceGroup"));
    }

    if (filterMap.containsKey("serviceSubGroup")) {
      filterQuery.append((whereNeeded ? WHERE : AND) + OUTER_SERVICE_SUB_GROUP_FILTER);
      whereNeeded = false;
      setFieldValueAndTypes(filterMap.get("serviceSubGroup"));
    }

    if (filterMap.containsKey("preferredSupplier")) {
      filterQuery.append((whereNeeded ? WHERE : AND) + OUTER_PREFERRED_SUPPLIER_FILTER);
      whereNeeded = false;
      setFieldValueAndTypes(filterMap.get("preferredSupplier"));
    }

    if (filterMap.containsKey("excludePO")) {
      filterQuery.append((whereNeeded ? WHERE : AND)
          + String.format(OUTER_NOT_RAISED_IN_PO_FILTER, preparePlaceHolders(stores.size())));
      whereNeeded = false;
      setFieldValueAndTypes(storesMap);
    }

    return filterQuery.toString();
  }

  String getConsumptionQuery(int consumptionDays, int orderdays, Map filterMap) {
    HashMap storesMap = (HashMap) filterMap.get("stores");
    List stores = (List) storesMap.get("value");
    setFieldValueAndTypes(consumptionDays, QueryBuilder.INTEGER);
    setFieldValueAndTypes(orderdays, QueryBuilder.INTEGER);
    setFieldValueAndTypes(consumptionDays, QueryBuilder.INTEGER);
    setFieldValueAndTypes(orderdays, QueryBuilder.INTEGER);
    setFieldValueAndTypes(consumptionDays, QueryBuilder.INTEGER);
    setFieldValueAndTypes(orderdays, QueryBuilder.INTEGER); // (for OUTER_CONS_SELECT)set field
                                                            // types in advance for the outer select
                                                            // query params

    String reorderQuery = String.format(BASE_GET_REORDER, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String stockQuery = String.format(BASE_GET_STOCK, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String indentQuery = String.format(BASE_GET_INDENT, preparePlaceHolders(stores.size()))
        + BASE_INDENT_GROUP_BY;
    setFieldValueAndTypes(storesMap);

    String salesQuery = String.format(BASE_GET_SALE, preparePlaceHolders(stores.size()))
        + BASE_SALE_GROUP_BY;
    setFieldValueAndTypes(storesMap);

    String purchaseQuery = String.format(BASE_GET_PURCHASE_ORDER,
        preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String consumptionQuery = String.format(BASE_GET_CONSUMPTION_QTY,
        preparePlaceHolders(stores.size())) + BASE_CONSUMPTION_DAYS_FILTER
        + BASE_CONSUMPTION_DAYS_GROUP_BY;
    setFieldValueAndTypes(storesMap);
    setFieldValueAndTypes(consumptionDays, QueryBuilder.INTEGER);

    // Build Base query combining all sub-queries to fetch values.
    String baseQuery = reorderQuery + UNION_ALL + stockQuery + UNION_ALL + indentQuery + UNION_ALL
        + salesQuery + UNION_ALL + purchaseQuery + UNION_ALL + consumptionQuery;

    // Build Inner query and add inner filters.
    String innerQuery = INNER_SELECT + baseQuery + INNER_SELECT_CLOSE;

    if (filterMap != null && !filterMap.isEmpty()) {
      innerQuery = innerQuery + INNER_MED_FILTER;
      innerQuery = addInnerFilters(innerQuery, filterMap);
      // add Consumption specific filters
      innerQuery = innerQuery + (intersectNeeded ? INTERSECT : "")
          + String.format(INNER_CONSUMPTION_FILTER, preparePlaceHolders(stores.size()))
          + INNER_CONSUMPTION_DAYS_SUB_FILTER + INNER_CONSUMPTION_GROUP_BY;
      setFieldValueAndTypes(storesMap);
      setFieldValueAndTypes(consumptionDays, QueryBuilder.INTEGER);
      innerQuery = innerQuery + INNER_MED_FILTER_CLOSE;
      whereNeeded = false;
    }
    // Build outer query and add filters
    String outerQuery = innerQuery;
    if (filterMap != null && !filterMap.isEmpty()) {
      outerQuery = addOuterFilters(outerQuery, filterMap);
    }
    outerQuery = OUTER_CONS_SELECT + outerQuery;
    if (!filterMap.containsKey("excludePO")) {
      outerQuery = outerQuery.replaceAll(" AND pnd.po_no IS NULL ", "");
    }

    return outerQuery;
  }

  String getIndentQuery(String indentType, int indentNo, Map filterMap) {
    // indent_type can be either of indent_purchase or indent_all
    boolean purchaseIndentOnly = indentType.equals("purchase");
    HashMap storesMap = (HashMap) filterMap.get("stores");
    List stores = (List) storesMap.get("value");

    String reorderQuery = String.format(BASE_GET_REORDER, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String stockQuery = String.format(BASE_GET_STOCK, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    // indent filters added
    String indentQuery = String.format(BASE_GET_INDENT, preparePlaceHolders(stores.size()))
        + ((indentNo != 0) ? BASE_INDENT_NO_FILTER : "")
        + (purchaseIndentOnly
            ? (BASE_PURCHASE_INDENT_ONLY_FILTER.concat(
                filterMap.get("purchaseDate") == null || filterMap.get("purchaseDate").equals("")
                    ? ""
                    : BASE_INDENT_PURCHASE_FLAG_DATE_FILTER))
            : (filterMap.get("purchaseDate") == null || filterMap.get("purchaseDate").equals("")
                ? ""
                : BASE_INDENT_PURCHASE_FLAG_DATE_FILTER))
        + BASE_INDENT_GROUP_BY;

    setFieldValueAndTypes(storesMap);

    if (indentNo != 0)
      setFieldValueAndTypes(indentNo, QueryBuilder.INTEGER);

    if (filterMap.get("purchaseDate") != null)
      setFieldValueAndTypes(((HashMap) filterMap.get("purchaseDate")).get("value"),
          QueryBuilder.DATE);

    String salesQuery = String.format(BASE_GET_SALE, preparePlaceHolders(stores.size()))
        + BASE_SALE_GROUP_BY;
    setFieldValueAndTypes(storesMap);

    String purchaseQuery = String.format(BASE_GET_PURCHASE_ORDER,
        preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    // Build Base query combining all sub-queries to fetch values.
    String baseQuery = reorderQuery + UNION_ALL + stockQuery + UNION_ALL + indentQuery + UNION_ALL
        + salesQuery + UNION_ALL + purchaseQuery;

    String innerQuery = INNER_SELECT + baseQuery + INNER_SELECT_CLOSE;

    if (filterMap != null && !filterMap.isEmpty()) {
      innerQuery = innerQuery + INNER_MED_FILTER;
      innerQuery = addInnerFilters(innerQuery, filterMap);
      innerQuery = innerQuery + (intersectNeeded ? INTERSECT : "")
          + String.format(INNER_INDENT_FILTER, preparePlaceHolders(stores.size()))
          + (indentNo != 0 ? INNER_INDENT_NO_SUB_FILTER : "")
          + (purchaseIndentOnly ? INNER_PURCHASE_INDENT_SUB_FILTER : "")
          + (filterMap.get("purchaseDate") == null || filterMap.get("purchaseDate").equals("")
              ? ""
              : INNER_PURCHASE_INDENT_DATE_SUB_FILTER)
          + INNER_INDENT_GROUP_BY;
      setFieldValueAndTypes(storesMap);

      if (indentNo != 0)
        setFieldValueAndTypes(indentNo, QueryBuilder.INTEGER);

      if (filterMap.get("purchaseDate") != null)
        setFieldValueAndTypes(((HashMap) filterMap.get("purchaseDate")).get("value"),
            QueryBuilder.DATE);

      innerQuery = innerQuery + INNER_MED_FILTER_CLOSE;
      whereNeeded = false;
    }
    // add outer filters
    String outerQuery = innerQuery;
    if (filterMap != null && !filterMap.isEmpty()) {
      outerQuery = addOuterFilters(outerQuery, filterMap);
    }
    outerQuery = OUTER_INDENT_SELECT + outerQuery;

    if (!filterMap.containsKey("excludePO")) {
      outerQuery = outerQuery.replaceAll(" AND pnd.po_no IS NULL ", "");
    }
    return outerQuery;
  }

  String getIndentAgeQuery(int indentAge, Map filterMap) {
    HashMap storesMap = (HashMap) filterMap.get("stores");
    List stores = (List) storesMap.get("value");

    String reorderQuery = String.format(BASE_GET_REORDER, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String stockQuery = String.format(BASE_GET_STOCK, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    // -- indent filters added
    String indentQuery = String.format(BASE_GET_INDENT, preparePlaceHolders(stores.size()))
        + BASE_INDENT_AGE_FILTER + BASE_INDENT_GROUP_BY;

    setFieldValueAndTypes(storesMap);
    setFieldValueAndTypes(indentAge, QueryBuilder.INTEGER);

    String salesQuery = String.format(BASE_GET_SALE, preparePlaceHolders(stores.size()))
        + BASE_SALE_GROUP_BY;
    setFieldValueAndTypes(storesMap);

    String purchaseQuery = String.format(BASE_GET_PURCHASE_ORDER,
        preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String baseQuery = reorderQuery + UNION_ALL + stockQuery + UNION_ALL + indentQuery + UNION_ALL
        + salesQuery + UNION_ALL + purchaseQuery;

    String innerQuery = INNER_SELECT + baseQuery + INNER_SELECT_CLOSE;

    // add filters
    if (filterMap != null && !filterMap.isEmpty()) {
      innerQuery = innerQuery + INNER_MED_FILTER;
      innerQuery = addInnerFilters(innerQuery, filterMap);
      // add indent specific filters
      innerQuery = innerQuery + (intersectNeeded ? INTERSECT : "")
          + String.format(INNER_INDENT_FILTER, preparePlaceHolders(stores.size()))
          + INNER_INDENT_AGE_SUB_FILTER + INNER_INDENT_GROUP_BY;
      setFieldValueAndTypes(storesMap);
      setFieldValueAndTypes(indentAge, QueryBuilder.INTEGER);

      innerQuery = innerQuery + INNER_MED_FILTER_CLOSE;
      whereNeeded = false;
    }
    // add other filters
    String outerQuery = innerQuery;
    if (filterMap != null && !filterMap.isEmpty()) {
      outerQuery = addOuterFilters(outerQuery, filterMap);
    }
    outerQuery = OUTER_PENDING_INDENT_SELECT + outerQuery;
    if (!filterMap.containsKey("excludePO")) {
      outerQuery = outerQuery.replaceAll(" AND pnd.po_no IS NULL ", "");
    }
    return outerQuery;
  }

  String getReorderLevelQuery(String reorderLevel, Map filterMap) {
    HashMap storesMap = (HashMap) filterMap.get("stores");
    List stores = (List) storesMap.get("value");

    String reorderQuery = String.format(BASE_GET_REORDER, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String stockQuery = String.format(BASE_GET_STOCK, preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    // -- indent filters added
    String indentQuery = String.format(BASE_GET_INDENT, preparePlaceHolders(stores.size()))
        + BASE_INDENT_GROUP_BY;
    setFieldValueAndTypes(storesMap);

    String salesQuery = String.format(BASE_GET_SALE, preparePlaceHolders(stores.size()))
        + BASE_SALE_GROUP_BY;
    setFieldValueAndTypes(storesMap);

    String purchaseQuery = String.format(BASE_GET_PURCHASE_ORDER,
        preparePlaceHolders(stores.size()));
    setFieldValueAndTypes(storesMap);

    String baseQuery = reorderQuery + UNION_ALL + stockQuery + UNION_ALL + indentQuery + UNION_ALL
        + salesQuery + UNION_ALL + purchaseQuery;

    String innerQuery = INNER_SELECT + baseQuery + INNER_SELECT_CLOSE;

    // add filters
    if (filterMap != null && !filterMap.isEmpty()
        && (filterMap.containsKey("salesQty") || filterMap.containsKey("salesDays"))) {
      innerQuery = innerQuery + INNER_MED_FILTER;
      innerQuery = addInnerFilters(innerQuery, filterMap);
      innerQuery = innerQuery + INNER_MED_FILTER_CLOSE;
      whereNeeded = false;
    } else {
      innerQuery = innerQuery + " )AS subodr ) AS outer_query  WHERE ord_qty >0 ";
      whereNeeded = false;
    }
    // add other filters
    String outerQuery = innerQuery;
    if (filterMap != null && !filterMap.isEmpty()) {
      outerQuery = addOuterFilters(outerQuery, filterMap);
      // add re-order level specific filters
      String reorderFilter;
      if (reorderLevel.equalsIgnoreCase("rl")) {
        reorderFilter = OUTER_REORDER_LEVEL_FILTER;
      } else if (reorderLevel.equalsIgnoreCase("dl")) {
        reorderFilter = OUTER_DANGER_LEVEL_FILTER;
      } else {
        reorderFilter = OUTER_MINIMUM_LEVEL_FILTER;
      }
      outerQuery = outerQuery + (whereNeeded ? WHERE : AND) + reorderFilter;
      whereNeeded = false;
    }
    outerQuery = (reorderLevel.equalsIgnoreCase("rl")
        ? OUTER_REORDER_REORDER_LEVEL_SELECT
        : reorderLevel.equalsIgnoreCase("dl")
            ? OUTER_REORDER_DANGER_LEVEL_SELECT
            : OUTER_REORDER_MINIMUM_LEVEL_SELECT)
        + outerQuery;
    if (!filterMap.containsKey("excludePO")) {
      outerQuery = outerQuery.replaceAll(" AND pnd.po_no IS NULL ", "");
    }

    return outerQuery;
  }

  public PagedList getConsumptionBasedReorder(int consumptionDays, int orderdays, Map filterMap,
      Integer pageNum) throws SQLException {
    PagedList resultList = null;
    resetStaticFields();
    String consumptionQuery = getConsumptionQuery(consumptionDays, orderdays, filterMap);
    resultList = getResultPagedList(consumptionQuery, pageNum);
    return resultList;
  }

  public PagedList getIndentBasedReorder(String indentType, int indentNo, Map filterMap,
      int pageNum) throws SQLException {
    PagedList resultList = null;
    resetStaticFields();
    String indentQuery = getIndentQuery(indentType, indentNo, filterMap);
    resultList = getResultPagedList(indentQuery, pageNum);
    return resultList;
  }

  public PagedList getPendingIndentAgeBasedReorder(int indentAge, Map filterMap, int pageNumber)
      throws SQLException {
    PagedList resultList = null;
    resetStaticFields();
    String indentQuery = getIndentAgeQuery(indentAge, filterMap);
    resultList = getResultPagedList(indentQuery, pageNumber);
    return resultList;
  }

  public PagedList getReorderLevelBasedReorder(String reorderLevel, Map filterMap, int pageNumber)
      throws SQLException {
    PagedList resultList = null;
    resetStaticFields();
    String reorderLevelQuery = getReorderLevelQuery(reorderLevel, filterMap);
    resultList = getResultPagedList(reorderLevelQuery, pageNumber);
    return resultList;
  }

  // For CSV Files

  public List getConsumptionBasedReorderForCSV(int consumptionDays, int orderdays, Map filterMap)
      throws SQLException {
    List resultList = null;
    resetStaticFields();
    String consumptionQuery = getConsumptionQuery(consumptionDays, orderdays, filterMap);
    resultList = getResultList(consumptionQuery);
    return resultList;
  }

  public List getIndentBasedReorderForCSV(String indentType, int indentNo, Map filterMap)
      throws SQLException {
    List resultList = null;
    resetStaticFields();
    String indentQuery = getIndentQuery(indentType, indentNo, filterMap);
    resultList = getResultList(indentQuery);
    return resultList;
  }

  public List getPendingIndentAgeBasedReorderForCSV(int indentAge, Map filterMap)
      throws SQLException {
    List resultList = null;
    resetStaticFields();
    String indentQuery = getIndentAgeQuery(indentAge, filterMap);
    resultList = getResultList(indentQuery);
    return resultList;
  }

  public List getReorderLevelBasedReorderForCSV(String reorderLevel, Map filterMap)
      throws SQLException {
    List resultList = null;
    resetStaticFields();
    String reorderLevelQuery = getReorderLevelQuery(reorderLevel, filterMap);
    resultList = getResultList(reorderLevelQuery);
    return resultList;
  }

  // old DAO's

  public static final int FIELD_NONE = 0;

  public static PagedList getPageList(List dataList, int totalCount, int pageSize, int pageNum) {
    return new PagedList(dataList, totalCount, pageSize, pageNum);
  }

  private static final String GET_ITEM_DETAILS = "select medicine_name,medicine_id,manf_mnemonic,issue_base_unit,"
      + " mm.manf_name,package_type,category_id,issue_units,issue_type,value,billable,gn.generic_name,"
      + " COALESCE((select cost_price from store_grn_details"
      + " join store_grn_main using(grn_no) where debit_note_no is null and store_id=? and medicine_id=pmd.medicine_id"
      + " order by grn_date desc limit 1),0) AS COST_PRICE,"
      + " COALESCE((select mrp from store_grn_details "
      + " join store_grn_main using(grn_no) where debit_note_no is null and store_id=? and medicine_id=pmd.medicine_id"
      + " order by grn_date desc limit 1),0) AS MRP," + " pmd.item_barcode_id, "
      + " pmd.service_sub_group_id, ssg.service_group_id, pmd.package_uom,pmd.cust_item_code "
      + " FROM store_item_details pmd " + " JOIN manf_master mm on manf_code=pmd.manf_name"
      + " JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = pmd.service_sub_group_id) "
      + " JOIN store_category_master pcm  ON CATEGORY_ID=med_category_id "
      + " LEFT JOIN generic_name gn ON pmd.generic_name=generic_code ";

  public static List<BasicDynaBean> getPOItems(Connection con, int storeId, List<String> itemIds)
      throws SQLException {

    PreparedStatement ps = null;
    boolean conflag = false;
    try {
      if (con == null) {
        con = DataBaseUtil.getReadOnlyConnection();
        conflag = true;
      }
      String[] itemIdsPlaceholdersArr = new String[itemIds.size()];
      Arrays.fill(itemIdsPlaceholdersArr, "?");
      String itemIdsPlaceholders = StringUtils.arrayToCommaDelimitedString(itemIdsPlaceholdersArr);
      ps = con.prepareStatement(
          GET_ITEM_DETAILS + " where medicine_id in (" + itemIdsPlaceholders + ")");
      ps.setInt(1, storeId);
      ps.setInt(2, storeId);
      ListIterator<String> itemIdIterator = itemIds.listIterator();
      while (itemIdIterator.hasNext()) {
        Object itemId = itemIdIterator.next();
        int idx = itemIdIterator.nextIndex();
        ps.setObject(idx + 2, itemId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(conflag ? con : null, ps);
    }
  }

  public static BasicDynaBean getPOItem(Connection con, int storeId, int itemId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ITEM_DETAILS + " where medicine_id = ?");
      ps.setInt(1, storeId);
      ps.setInt(2, storeId);
      ps.setInt(3, itemId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  public static final String getAdditionalTermsAndConditions() {
    return DataBaseUtil
        .getStringValueFromDb("SELECT HOSPITAL_TERMS_CONDITIONS FROM STORE_MISCELLANEOUS_SETTINGS");
  }

  public static ArrayList getPaymentTermsAndConditions() {
    return DataBaseUtil.queryToArrayList(
        "SELECT TEMPLATE_CODE,TEMPLATE_NAME,TERMS_CONDITIONS FROM PH_PAYMENT_TERMS WHERE STATUS='A' AND IS_DELIVERY_INSTRUCTION != 'Y' ORDER BY TEMPLATE_NAME ");
  }

  private static final String[] QUERY_FIELD_NAMES = { "" };

  private static final String STOCK_EXT_QUERY_FIELDS = " select medicine_name,SUM(qty-qty_fullfilled) AS qty,MAX(indent_no) AS indent_no, "
      + "indent_type,s.center_id as store_center_id ";

  private static final String STOCK_EXT_QUERY_COUNT = " SELECT count(medicine_name) ";

  private static final String STOCK_EXT_QUERY_INIT_WHERE = " where  iim.status !='C' and medicine_id = '0' and ii.status='A' and (qty-qty_fullfilled)> 0";

  private static final String STOCK_EXT_QUERY_TABLES = " from store_indent_main iim JOIN stores s ON(s.dept_id = store_id)  join store_indent_details ii using(indent_no)";

  private static final String GROUP_BY = " indent_store, medicine_name, indent_type";

  public static PagedList searchList(int storeId, int sortOrder, boolean sortReverse, int pageSize,
      int pageNum) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    String sortField = null;
    if ((sortOrder != FIELD_NONE) && (sortOrder < QUERY_FIELD_NAMES.length)) {
      sortField = QUERY_FIELD_NAMES[sortOrder];
    }

    SearchQueryBuilder qb = new SearchQueryBuilder(con, STOCK_EXT_QUERY_FIELDS,
        STOCK_EXT_QUERY_COUNT, STOCK_EXT_QUERY_TABLES, STOCK_EXT_QUERY_INIT_WHERE, GROUP_BY,
        sortField, sortReverse, pageSize, pageNum);

    qb.addFilter(qb.INTEGER, "INDENT_STORE", "=", storeId);
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

  public static boolean updateItemId(Connection con, Map<String, Object> item) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement(
          "update store_indent_details set medicine_id=? where indent_no=? and medicine_name=?");
      for (Map.Entry e : (Collection<Map.Entry<String, Object>>) item.entrySet()) {
        ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
      }
      count = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return count > 0;

  }

  /**
   * for item details pop-up
   */
  private static final String GET_ITEM_DETAILS_FOR_POP_UP = "SELECT INGM.GRN_NO,TO_CHAR(INGM.GRN_DATE,'YYYY-MM-DD') AS GRN_DATE, "
      + " COALESCE(INGM.PO_NO,'') AS PO_NO,COALESCE(IINV.INVOICE_NO,'') AS INVOICE_NO,"
      + " TO_CHAR(IINV.INVOICE_DATE,'YYYY-MM-DD') AS INVOICE_DATE,SM.SUPPLIER_NAME,SM.CUST_SUPPLIER_CODE,"
      + " ITD.MEDICINE_NAME,ING.MRP,ING.COST_PRICE,ING.TAX_RATE,ING.BILLED_QTY,ING.BONUS_QTY,ING.DISCOUNT, ITD.CUST_ITEM_CODE,"
      + " CASE WHEN INGM.consignment_stock=true THEN 'true' ELSE 'false' END  " + " As stock  "
      + " FROM store_grn_main INGM JOIN store_grn_details ING ON INGM.GRN_NO = ING.GRN_NO"
      + " JOIN store_item_details ITD ON ITD.MEDICINE_ID=ING.MEDICINE_ID"
      + " LEFT JOIN store_invoice IINV using(supplier_invoice_id)"
      + " JOIN SUPPLIER_MASTER SM ON SM.SUPPLIER_CODE=IINV.SUPPLIER_ID"
      + " WHERE ING.MEDICINE_ID=?  ";

  public static List<BasicDynaBean> getItemDetails(int itemId, List<Integer> storeList)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    StringBuilder query = new StringBuilder(GET_ITEM_DETAILS_FOR_POP_UP);
    DataBaseUtil.addWhereFieldInList(query, "store_id", storeList, true);

    try {
      int i = 1;
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query.toString() + " ORDER BY IINV.INVOICE_DATE DESC");
      ps.setInt(i, itemId);
      i++;
      for (Integer store : storeList) {
        ps.setInt(i, store);
        i++;
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public List<BasicDynaBean> getLatestCostPrice(List<BasicDynaBean> reorderList)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    StringBuilder query = new StringBuilder(
        "select * from (select medicine_id, cost_price, rank() over (partition by medicine_id order by grn_date desc ) "
            + " from store_grn_details sgd JOIN store_grn_main using(grn_no) ");

    String filter = " AND debit_note_no is null group by medicine_id, cost_price, grn_date) as foo WHERE rank=1 ";

    try {
      con = DataBaseUtil.getConnection();
      QueryBuilder.addWhereFieldOpValue(false, query, "medicine_id", "IN", reorderList);
      query.append(filter);
      ps = con.prepareStatement(query.toString());

      int i = 1;
      for (BasicDynaBean bean : reorderList) {
        ps.setInt(i, (Integer) bean.get("item_id"));
        i++;
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
