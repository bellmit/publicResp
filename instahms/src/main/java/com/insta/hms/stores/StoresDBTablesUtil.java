package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.instaapi.common.DbUtil;

import flexjson.JSONSerializer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility methods class for all Pharmacy Tables (WIP) ...
 */

public class StoresDBTablesUtil {

  /** The Constant GET_GENERICS_IN_MASTER. */
  public static final String GET_GENERICS_IN_MASTER = "SELECT DISTINCT GENERIC_NAME FROM"
      + " GENERIC_NAME order by GENERIC_NAME";
  
  /** The Constant GET_ACTIVE_GENERICS_IN_MASTER. */
  public static final String GET_ACTIVE_GENERICS_IN_MASTER = "SELECT DISTINCT GENERIC_NAME"
      + " FROM GENERIC_NAME WHERE STATUS='A' order by GENERIC_NAME";
  
  /** The Constant GET_GENERICS_MASTER. */
  public static final String GET_GENERICS_MASTER = "SELECT * FROM GENERIC_NAME order by"
      + " GENERIC_NAME";

  // public static String GET_MEDICINE_NAMES_IN_MASTER = "SELECT MEDICINE_NAME FROM
  /** The Constant GET_MEDICINE_NAMES_IN_MASTER. */
  // store_item_details ORDER BY MEDICINE_NAME ";
  public static final String GET_MEDICINE_NAMES_IN_MASTER = "SELECT medicine_id,medicine_name,"
      + "cust_item_code,  CASE WHEN cust_item_code IS NOT NULL AND  TRIM(cust_item_code) != ''"
      + "  THEN medicine_name||' - '||cust_item_code ELSE medicine_name END as"
      + " cust_item_code_with_name  FROM store_item_details ORDER BY MEDICINE_NAME ";

  /** The Constant GET_ACTIVE_CATAGEROY_IN_MASTER. */
  public static final String GET_ACTIVE_CATAGEROY_IN_MASTER = "SELECT CATEGORY "
      + " FROM store_category_master WHERE STATUS='A' ORDER BY CATEGORY";

  /** The Constant GET_CATAGEROY_MASTER. */
  public static final String GET_CATAGEROY_MASTER = "select * from store_category_master";

  /** The Constant GET_MANFNAMES_IN_MASTER. */
  public static final String GET_MANFNAMES_IN_MASTER = "select distinct manf_name FROM"
      + " manf_master ";

  /** The Constant GETSUPPLIERS. */
  public static final String GETSUPPLIERS = "SELECT *,"
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')"
      + "  AND  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND  "
      + " (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city "
      + " FROM SUPPLIER_MASTER WHERE STATUS='A' ORDER BY SUPPLIER_NAME ";

  /** The Constant GET_CENTER_SUPPLIERS. */
  public static final String GET_CENTER_SUPPLIERS =
      "SELECT DISTINCT SM.SUPPLIER_NAME,SM.*,center_id, "
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND"
      + "  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city " + " FROM SUPPLIER_MASTER SM "
      + " LEFT JOIN SUPPLIER_CENTER_MASTER SCM ON(SCM.SUPPLIER_CODE=SM.SUPPLIER_CODE) "
      + " WHERE SM.STATUS='A' AND SCM.STATUS = 'A' ORDER BY SUPPLIER_NAME ";

  /** The Constant GET_CENTER_BASED_SUPPLIERS. */
  public static final String GET_CENTER_BASED_SUPPLIERS = "SELECT SM.*,center_id, "
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND"
      + "  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city "
      + " FROM SUPPLIER_MASTER SM JOIN SUPPLIER_CENTER_MASTER SCM ON(SCM.SUPPLIER_CODE="
      + " SM.SUPPLIER_CODE) WHERE SM.STATUS='A' AND (scm.center_id = ? OR scm.center_id = 0)"
      + " ORDER BY SUPPLIER_NAME ";

  /** The Constant GET_MANUFACTURE_MASTER. */
  public static final String GET_MANUFACTURE_MASTER = "SELECT * FROM MANF_MASTER  order by"
      + " MANF_NAME";

  /** The Constant GET_SUPP_WITH_INV. */
  public static final String GET_SUPP_WITH_INV = "SELECT SM.SUPPLIER_CODE,SM.SUPPLIER_NAME,"
      + "SM.CUST_SUPPLIER_CODE,TO_CHAR(IINV.INVOICE_DATE,'DD-MM-YYYY') AS INVOICE_DATE, "
      + " IINV.PO_REFERENCE,IINV.INVOICE_NO,TO_CHAR(IINV.DUE_DATE,'DD-MM-YYYY') AS DUE_DATE,"
      + " IINV.PO_NO,IINV.SUPPLIER_ID,IINV.ROUND_OFF,IINV.STATUS,IINV.CONSIGNMENT_STOCK,"
      + " CASE WHEN DISCOUNT_TYPE='P' THEN DISCOUNT_PER ELSE DISCOUNT END AS DISCOUNT,"
      + " DISCOUNT_TYPE,credit_period,tax_name,cst_rate,cess_tax_rate"
      + " FROM SUPPLIER_MASTER SM  LEFT OUTER JOIN store_invoice IINV ON SM.SUPPLIER_CODE="
      + " SUPPLIER_ID WHERE SM.STATUS='A'";

  /** The Constant GET_PO_DETAILS. */
  public static final String GET_PO_DETAILS = "SELECT PO_NO,REFERENCE,SUPPLIER_ID, "
      + " CASE WHEN MRP_TYPE='I' THEN 'it' ELSE 'et' END AS MRPTYPE, status"
      + " FROM store_po_main ";

  /** The Constant GET_STORES_MASTER. */
  public static final String GET_STORES_MASTER = "SELECT * from stores ";

  /** The Constant PATIENT_ISSUE_ITEMS. */
  public static final String PATIENT_ISSUE_ITEMS = "SELECT MEDICINE_NAME FROM store_item_details "
      + " JOIN store_category_master on med_category_id=category_id where issue_type in ('C','R')"
      + " and billable ORDER BY MEDICINE_NAME ";

  /** The Constant USER_ISSUE_ITEMS. */
  public static final String USER_ISSUE_ITEMS = "SELECT MEDICINE_NAME FROM store_item_details pmd"
      + " JOIN store_category_master on med_category_id=category_id where pmd.status='A' and"
      + " issue_type in ('L','C','P')  ORDER BY MEDICINE_NAME ";

  /** The Constant USER_ISSUE_ITEMS_BY_DEPT. */
  public static final String USER_ISSUE_ITEMS_BY_DEPT = "SELECT distinct MEDICINE_NAME, dept_id "
      + "FROM store_stock_details msd " + "RIGHT JOIN store_item_details pmd USING (medicine_id) "
      + "JOIN store_category_master on med_category_id=category_id where pmd.status='A' and"
      + " issue_type in ('L','C','P') ORDER BY MEDICINE_NAME ";

  /** The Constant MEDICINE_NAMES_IN_MASTER. */
  public static final String MEDICINE_NAMES_IN_MASTER = "SELECT MEDICINE_NAME,pmd.status,"
      + " issue_type FROM store_item_details pmd  join store_category_master on"
      + " med_category_id=category_id ORDER BY MEDICINE_NAME ";

  /** The Constant CONSUMABLE_ITEMS. */
  public static final String CONSUMABLE_ITEMS = "select category_id,medicine_id as item_id,"
      + " medicine_name as item_name,issue_qty,issue_units "
      + " from store_item_details sitd "
      + " join  store_category_master scm on (med_category_id=category_id) "
      + " where issue_type='C' and sitd.status='A' ORDER BY medicine_name ";

  /** The Constant LOCATION_MASTER. */
  public static final String LOCATION_MASTER = "SELECT LOCATION_NAME FROM LOCATION_MASTER";
  
  /** The Constant GET_PARENT_ASSETS. */
  public static final String GET_PARENT_ASSETS = "select distinct medicine_name from"
      + " store_item_details  join fixed_asset_master on medicine_id=asset_id"
      + " where parent_asset_id is null";
  
  /** The Constant DOCTORS. */
  public static final String DOCTORS = "select doctor_name from doctors";

  /** The Constant GET_MEDICINE_MASTER. */
  public static final String GET_MEDICINE_MASTER = "select medicine_name,medicine_id,status from"
      + " store_item_details order by medicine_name";

  /** The Constant GET_ONLY_PO_ITEMS. */
  public static final String GET_ONLY_PO_ITEMS = "SELECT std.medicine_name,std.medicine_id,"
      + " std.status,std.manf_name,std.generic_name, std.med_category_id,std.max_cost_price,"
      + " mf.status AS manf_status, scm.status AS cat_status, "
      + " gn.status AS gen_status,std.item_barcode_id, std.issue_units  "
      + " FROM store_item_details std "
      + " JOIN manf_master mf ON ( mf.manf_code = std.manf_name ) "
      + " JOIN  store_category_master scm ON (category_id = med_category_id ) "
      + " LEFT JOIN generic_name gn ON ( std.generic_name = generic_code ) "
      + " where medicine_id in (select medicine_id from store_po where po_no=? and status !='R') "
      + " ORDER BY medicine_name";

  /** The Constant GET_ALL_WO_ITEMS. */
  public static final String GET_ALL_WO_ITEMS = "SELECT wo_item_id,wo_item_name FROM"
      + " work_order_items_master where status='A'";

  /** The Constant GET_CENTER_SUPPLIERS_DETAILS. */
  public static final String GET_CENTER_SUPPLIERS_DETAILS =
      "SELECT SM.*, SM.SUPPLIER_NAME,SCM.CENTER_ID,"
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND"
      + "  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND"
      + "  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city " + " FROM SUPPLIER_MASTER SM "
      + " LEFT JOIN SUPPLIER_CENTER_MASTER SCM ON(SCM.SUPPLIER_CODE=SM.SUPPLIER_CODE) "
      + " WHERE SM.STATUS='A' AND SCM.STATUS = 'A' AND SCM.CENTER_ID IN(?,0) ORDER BY"
      + " SM.SUPPLIER_NAME ";

  /**
   * Method:getSupplierMaster return ArrayList contains supplier_master data.
   *
   * @return the supplier master
   * @throws SQLException the SQL exception
   */
  public static ArrayList getSupplierMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GETSUPPLIERS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the center mater supplier master.
   *
   * @return the center mater supplier master
   * @throws SQLException the SQL exception
   */
  public static ArrayList getCenterMaterSupplierMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CENTER_SUPPLIERS);
      // ps.setInt(1,centerId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ALL_ACTIVE_SUPPLIERS. */
  private static final String GET_ALL_ACTIVE_SUPPLIERS = "SELECT supplier_code, supplier_name,"
      + " cust_supplier_code  FROM supplier_master where status='A' ORDER BY supplier_name";

  /** The Constant CENTER_SUPPLIER_DETAILS. */
  public static final String CENTER_SUPPLIER_DETAILS = "select sm.supplier_code,supplier_name,"
      + " sm.cust_supplier_code,coalesce(scm.center_id,0) as center_id from supplier_master sm  "
      + " left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + " where scm.status='A' and sm.status='A' and scm.center_id IN(?,0) order by"
      + " sm.supplier_name ";

  /**
   * Gets the center based supplier master.
   *
   * @param centerId the center id
   * @return the center based supplier master
   * @throws SQLException the SQL exception
   */
  public static ArrayList getCenterBasedSupplierMaster(Integer centerId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        pstmt = con.prepareStatement(CENTER_SUPPLIER_DETAILS);
        pstmt.setInt(1, centerId);
      } else {
        pstmt = con.prepareStatement(GET_ALL_ACTIVE_SUPPLIERS);
      }
      return DataBaseUtil.queryToArrayList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Gets the center based supplier master.
   *
   * @return the center based supplier master
   * @throws SQLException the SQL exception
   */
  public static ArrayList getCenterBasedSupplierMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_SUPPLIERS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DEPARTMENTS. */
  private static final String GET_DEPARTMENTS = "SELECT * FROM stores";

  /**
   * Method:getDepts return ArrayList contains stores (Pharmacy Stores table) data.
   *
   * @return the depts
   * @throws SQLException the SQL exception
   */
  public static ArrayList getDepts() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_DEPARTMENTS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * This is the General method which return only one column JSON data from any table.
   *
   * @param queryName the query name
   * @return the names in JSON
   * @throws SQLException the SQL exception
   */
  public static String getNamesInJSON(String queryName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList<String> data = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(queryName);
      JSONSerializer js = new JSONSerializer().exclude("class");
      data = DataBaseUtil.queryToArrayList1(ps);
      return js.serialize(data);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * This is the General method which return JSON data from any table.
   *
   * @param query the query
   * @return the table data in JSON
   * @throws SQLException the SQL exception
   */

  public static String getTableDataInJSON(String query) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList data = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      JSONSerializer js = new JSONSerializer().exclude("class");
      data = DataBaseUtil.queryToArrayList(ps);
      return js.deepSerialize(data);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GENERIC_NAME_TO_ID. */
  private static final String GENERIC_NAME_TO_ID = "SELECT generic_code FROM generic_name"
      + " WHERE generic_name=?";

  /**
   * Generic name to id.
   *
   * @param genericName the generic name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String genericNameToId(String genericName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GENERIC_NAME_TO_ID);
      ps.setString(1, genericName);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CATEGORY_NAME_TO_ID. */
  private static final String CATEGORY_NAME_TO_ID = "SELECT category_id FROM"
      + " store_category_master WHERE category=?";

  /**
   * Category name to id.
   *
   * @param catName the cat name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String categoryNameToId(String catName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(CATEGORY_NAME_TO_ID);
      ps.setString(1, catName);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant MANUFACTURER_NAME_TO_ID. */
  private static final String MANUFACTURER_NAME_TO_ID = "SELECT manf_code FROM"
      + " manf_master WHERE manf_name=?";

  /**
   * Manf name to id.
   *
   * @param medicineName the medicine name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String manfNameToId(String medicineName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(MANUFACTURER_NAME_TO_ID);
      ps.setString(1, medicineName);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SUPPLIER_NAME_TO_ID. */
  private static final String SUPPLIER_NAME_TO_ID = "SELECT supplier_code FROM supplier_master"
      + " WHERE supplier_name=?";

  /**
   * Supp name to id.
   *
   * @param suppName the supp name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String suppNameToId(String suppName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SUPPLIER_NAME_TO_ID);
      ps.setString(1, suppName);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SUPPLIER_ID_TO_NAME. */
  private static final String SUPPLIER_ID_TO_NAME = "SELECT supplier_name FROM supplier_master"
      + " left join supplier_center_master scm using(supplier_code) WHERE supplier_code=? and"
      + " scm.status='A'";

  /**
   * Supp id to name.
   *
   * @param suppId the supp id
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String suppIdToName(String suppId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(SUPPLIER_ID_TO_NAME);
      ps.setString(1, suppId);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DEPT_NAME_TO_ID. */
  private static final String DEPT_NAME_TO_ID = "SELECT dept_id FROM stores WHERE dept_name=?";

  /**
   * Dept name to id.
   *
   * @param deptName the dept name
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int deptNameToId(String deptName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(DEPT_NAME_TO_ID);
      ps.setString(1, deptName);

      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DEPT_ID_TO_NAME. */
  private static final String DEPT_ID_TO_NAME = "SELECT dept_name FROM stores WHERE dept_id=?";

  /**
   * Dept id to name.
   *
   * @param deptId the dept id
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String deptIdToName(int deptId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(DEPT_ID_TO_NAME);
      ps.setInt(1, deptId);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Copy string array TO bigdecimal.
   *
   * @param len the len
   * @param array the array
   * @return the big decimal[]
   */
  public static BigDecimal[] copyStringArrayTOBigdecimal(int len, Object array) {
    BigDecimal[] value = new BigDecimal[len];
    String[] a1 = (String[]) array;
    for (int i = 0; i < len; i++) {
      value[i] = a1[i] == null || a1[i].equals("") ? null : new BigDecimal(a1[i]);
    }
    return value;
  }

  /**
   * Update table.
   *
   * @param con the con
   * @param item the item
   * @param query the query
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateTable(Connection con, Map<String, Object> item, String query)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(query);) {
      for (Map.Entry e : item.entrySet()) {
        ps.setObject(Integer.parseInt((e.getKey()).toString()), e.getValue());
      }
      int count = ps.executeUpdate();
      return count > 0;
    }
  }

  /**
   * Update table with dynamic query.
   *
   * @param con the con
   * @param item the item
   * @param query the query
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateTableWithDynamicQuery(Connection con, Map<Integer, Object> item,
      StringBuilder query) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(query.toString());) {
      for (Map.Entry e : item.entrySet()) {
        ps.setObject((Integer) e.getKey(), e.getValue());
      }
      int count = ps.executeUpdate();
      return count > 0;
    }
  }

  /** The Constant ITEM_NAME_TO_ID. */
  private static final String ITEM_NAME_TO_ID = "SELECT medicine_ID FROM store_item_details WHERE"
      + " medicine_name=?";

  /**
   * Item name to id.
   *
   * @param itemName the item name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String itemNameToId(String itemName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(ITEM_NAME_TO_ID);
      ps.setString(1, itemName);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update count.
   *
   * @param con the con
   * @param count the count
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateCount(Connection con, int count) throws SQLException {
    PreparedStatement ps = null;
    String query = "ALTER SEQUENCE store_seq  RESTART WITH " + count;
    boolean status = true;
    try {
      ps = con.prepareStatement(query);
      ps.executeUpdate();
      ps.close();
    } catch (Exception ex) {
      status = false;
    } finally {
      DbUtil.closeConnections(null, ps);
    }
    return status;
  }

  /**
   * Gets the next id.
   *
   * @param seqName the seq name
   * @param con the con
   * @return the next id
   * @throws SQLException the SQL exception
   */
  public static int getNextId(String seqName, Connection con) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement("select nextval(?)");) {
      ps.setString(1, seqName);
      return DataBaseUtil.getIntValueFromDb(ps);
    }
  }

  /**
   * Gets the user issues for dept.
   *
   * @param deptId the dept id
   * @return the user issues for dept
   * @throws SQLException the SQL exception
   */
  public static List getUserIssuesForDept(String deptId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList data = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      ps = con.prepareStatement(USER_ISSUE_ITEMS_BY_DEPT);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_CENTER_STORES. */
  private static final String GET_CENTER_STORES = " SELECT user_stores.dept_id, s.dept_name,"
      + " s.is_super_store,s.center_id FROM (SELECT"
      + " regexp_split_to_table(multi_store, E'\\,')::integer as dept_id FROM u_user "
      + " WHERE emp_username=?) as user_stores "
      + " JOIN stores s ON (s.dept_id=user_stores.dept_id) "
      + " JOIN hospital_center_master hcm ON (s.center_id=hcm.center_id) ";

  /** The Constant ORDER_BY. */
  private static final String ORDER_BY = " ORDER BY s.dept_name";

  /**
   * Gets the logged user stores.
   *
   * @param userName the user name
   * @param onlySuperStores the only super stores
   * @param onlyWithCounters the only with counters
   * @param allowedRaiseBill the allowed raise bill
   * @param onlySalesStores the only sales stores
   * @return the logged user stores
   * @throws SQLException the SQL exception
   */
  public static List getLoggedUserStores(String userName, String onlySuperStores,
      String onlyWithCounters, String allowedRaiseBill, String onlySalesStores)
      throws SQLException {
    return getLoggedUserStores(userName, onlySuperStores, onlyWithCounters, allowedRaiseBill,
        onlySalesStores, null, null);

  }
  
  /**
   * Gets the logged user stores.
   *
   * @param userName the user name
   * @param onlySuperStores the only super stores
   * @param onlyWithCounters the only with counters
   * @param allowedRaiseBill the allowed raise bill
   * @param onlySalesStores the only sales stores
   * @param sterileStores the sterile stores
   * @param storesWithTariff the stores with tariff
   * @return the logged user stores
   * @throws SQLException the SQL exception
   */
  public static List getLoggedUserStores(String userName, String onlySuperStores,
      String onlyWithCounters, String allowedRaiseBill, String onlySalesStores,
      String sterileStores, String storesWithTariff) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int centerId = RequestContext.getCenterId();
    // if the center id is equal to zero, then retrieve all the centers stores.
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      StringBuilder queryBld = new StringBuilder();
      queryBld.append(GET_CENTER_STORES);

      queryBld.append(" WHERE (s.center_id=? or ?=0) ");

      boolean isOnlySuperStore = onlySuperStores != null && !onlySuperStores.equals("")
          && onlySuperStores.equalsIgnoreCase("Y");
      if (isOnlySuperStore) {
        queryBld.append(" AND s.is_super_store='Y' ");
      }

      boolean isOnlyWithCounter = onlyWithCounters != null && !onlyWithCounters.equals("")
          && onlyWithCounters.equalsIgnoreCase("Y");
      if (isOnlyWithCounter) {
        queryBld.append(" AND (counter_id IS NOT NULL AND trim(counter_id) != '') ");
      }

      boolean isAllowedBillRaise = allowedRaiseBill != null && !allowedRaiseBill.equals("")
          && allowedRaiseBill.equalsIgnoreCase("Y");
      if (isAllowedBillRaise) {
        queryBld.append(" AND (allowed_raise_bill = 'Y') ");
      }

      boolean isOnlySalesStores = onlySalesStores != null && !onlySalesStores.equals("")
          && onlySalesStores.equalsIgnoreCase("Y");
      if (isOnlySalesStores) {
        queryBld.append(" AND (is_sales_store = 'Y') ");
      }
      if (sterileStores != null && !sterileStores.isEmpty()) {
        queryBld.append(" AND (is_sterile_store = ?)");
      }
      if (storesWithTariff != null && !storesWithTariff.trim().equals("")) {
        queryBld.append(" AND (COALESCE(store_rate_plan_id, 0) != 0) ");
      }

      queryBld.append(ORDER_BY);
      String storeQuery = queryBld.toString();
      ps = con.prepareStatement(storeQuery);
      ps.setString(1, userName);
      ps.setInt(2, centerId);
      ps.setInt(3, centerId);
      if (sterileStores != null && !sterileStores.isEmpty()) {
        ps.setString(4, sterileStores);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ROLE_FOR_USER. */
  private static final String GET_ROLE_FOR_USER = "select role_id from u_user where "
      + "emp_username=?";

  /**
   * Gets the role for user.
   *
   * @param userName the user name
   * @return the role for user
   * @throws SQLException the SQL exception
   */
  public static int getRoleForUser(String userName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ROLE_FOR_USER);
      ps.setString(1, userName);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_STORE_IDS. */
  private static final String GET_STORE_IDS = " select dept_id::text from stores where "
      + " dept_id::text in (select regexp_split_to_table(multi_store, E'\\,')::CHARACTER VARYING"
      + " FROM u_user where emp_username=?);";

  /**
   * Gets the logged user store ids.
   *
   * @param userName the user name
   * @return the logged user store ids
   * @throws SQLException the SQL exception
   */
  public static ArrayList getLoggedUserStoreIds(String userName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_STORE_IDS);
      ps.setString(1, userName);
      return DataBaseUtil.queryToArrayList1(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The get stores. */
  private static String GET_STORES = "SELECT dept_id,dept_name,center_id FROM stores s WHERE"
      + " status='A' ";
  
  /** The get stores order by. */
  private static String GET_STORES_ORDER_BY = " ORDER BY dept_name ";

  /**
   * Gets the stores.
   *
   * @param onlySuperStores the only super stores
   * @param onlyWithCounters the only with counters
   * @param allowedRaiseBill the allowed raise bill
   * @param onlySalesStores the only sales stores
   * @return the stores
   * @throws SQLException the SQL exception
   */
  public static List getStores(String onlySuperStores, String onlyWithCounters,
      String allowedRaiseBill, String onlySalesStores) throws SQLException {
    return getStores(onlySuperStores, onlyWithCounters, allowedRaiseBill, onlySalesStores, null,
        null);

  }

  /**
   * Gets the stores.
   *
   * @param onlySuperStores the only super stores
   * @param onlyWithCounters the only with counters
   * @param allowedRaiseBill the allowed raise bill
   * @param onlySalesStores the only sales stores
   * @param sterileStores the sterile stores
   * @param storesWithTariff the stores with tariff
   * @return the stores
   * @throws SQLException the SQL exception
   */
  public static List getStores(String onlySuperStores, String onlyWithCounters,
      String allowedRaiseBill, String onlySalesStores, String sterileStores,
      String storesWithTariff) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int centerId = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      StringBuilder queryBld = new StringBuilder();
      queryBld.append(GET_STORES);

      queryBld.append(" AND (s.center_id=? or ?=0) ");

      boolean isOnlySuperStore = onlySuperStores != null && !onlySuperStores.equals("")
          && onlySuperStores.equalsIgnoreCase("Y");
      if (isOnlySuperStore) {
        queryBld.append(" AND s.is_super_store='Y' ");
      }

      boolean isOnlyWithCounter = onlyWithCounters != null && !onlyWithCounters.equals("")
          && onlyWithCounters.equalsIgnoreCase("Y");
      if (isOnlyWithCounter) {
        queryBld.append(" AND (counter_id IS NOT NULL AND trim(counter_id) != '') ");
      }

      boolean isAllowedBillRaise = allowedRaiseBill != null && !allowedRaiseBill.equals("")
          && allowedRaiseBill.equalsIgnoreCase("Y");
      if (isAllowedBillRaise) {
        queryBld.append(" AND (allowed_raise_bill = 'Y') ");
      }

      boolean isOnlySalesStores = onlySalesStores != null && !onlySalesStores.equals("")
          && onlySalesStores.equalsIgnoreCase("Y");
      if (isOnlySalesStores) {
        queryBld.append(" AND (is_sales_store = 'Y') ");
      }
      
      if (sterileStores != null && !sterileStores.isEmpty()) {
        queryBld.append(" AND (is_sterile_store = ?)");
      }

      if (storesWithTariff != null && !storesWithTariff.trim().equals("")) {
        queryBld.append(" AND (COALESCE(store_rate_plan_id, 0) != 0) ");
      }

      queryBld.append(GET_STORES_ORDER_BY);
      String storeQuery = queryBld.toString();
      ps = con.prepareStatement(storeQuery);
      ps.setInt(1, centerId);
      ps.setInt(2, centerId);
      if (sterileStores != null && !sterileStores.isEmpty()) {
        ps.setString(3, sterileStores);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the only PO items.
   *
   * @param pono the pono
   * @return the only PO items
   * @throws SQLException the SQL exception
   */
  public static ArrayList getOnlyPOItems(String pono) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList<String> data = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ONLY_PO_ITEMS);
      ps.setString(1, pono);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  /**
   * Gets the table data.
   *
   * @param query the query
   * @return the table data
   * @throws SQLException the SQL exception
   */
  public static ArrayList getTableData(String query) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(query);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pharmacy grp id.
   *
   * @return the pharmacy grp id
   * @throws SQLException the SQL exception
   */
  public static String getPharmacyGrpId() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(
          "select service_group_id from service_groups where service_group_name ="
          + " 'Pharmacy item'");
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
