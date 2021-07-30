package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * The Class StoreDAO.
 */
public class StoreDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(StoreDAO.class);

  /** The con. */
  Connection con = null;

  /**
   * Instantiates a new store DAO.
   *
   * @param con
   *          the con
   */
  public StoreDAO(Connection con) {
    this.con = con;
  }

  /** The Constant GET_STORE_NAMES. */
  public static final String GET_STORE_NAMES = "SELECT dept_name as store_name FROM stores"
      + " WHERE status='A' ORDER BY dept_name";

  /** The Constant GET_STORE_DETAILS. */
  public static final String GET_STORE_DETAILS = "SELECT dept_id,dept_name,store_rate_plan_id"
      + "  FROM stores WHERE status='A' ORDER BY dept_name";

  /** The Constant GET_STORE_WISE_STOCK_DETAILS. */
  public static final String GET_STORE_WISE_STOCK_DETAILS = "SELECT d.dept_name,pds.dept_id,"
      + "pds.min_level,pds.max_level,pds.danger_level,pds.reorder_level,pds.medicine_id,pds.bin "
      + "FROM item_store_level_details pds,store_item_details pmd,"
      + "stores d WHERE pds.dept_id=d.dept_id and pds.medicine_id=pmd.medicine_id and"
      + " pds.medicine_id=?";

  /** The Constant INSERT_STORE_STOCK_LEVELS. */
  public static final String INSERT_STORE_STOCK_LEVELS = "INSERT INTO item_store_level_details("
      + "dept_id,medicine_id,min_level,max_level,reorder_level,danger_level,bin)"
      + "VALUES(?,?,?,?,?,?,?)";

  /** The Constant UPDATE_STORE_STOCK_LEVELS. */
  public static final String UPDATE_STORE_STOCK_LEVELS = "UPDATE item_store_level_details SET"
      + " min_level=?,max_level=?,reorder_level=?,danger_level=?,bin=?"
      + " WHERE dept_id=? AND medicine_id=?";

  /** The Constant DELETE_STATOC_LEVEL. */
  public static final String DELETE_STATOC_LEVEL = "DELETE FROM item_store_level_details WHERE"
      + " dept_id=? AND medicine_id=?";

  /**
   * Gets the store details.
   *
   * @return the store details
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getStoreDetails() throws SQLException {

    PreparedStatement ps = null;
    ps = con.prepareStatement(GET_STORE_DETAILS);
    ArrayList storeDetailsLists = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    return storeDetailsLists;
  }

  /** The Constant GET_STORE_INFO. */
  public static final String GET_STORE_INFO = "SELECT *  FROM stores WHERE dept_id = ?"
      + " AND status='A' ORDER BY dept_name";

  /**
   * Find by store.
   *
   * @param storeId
   *          the store id
   * @return the basic dyna bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean findByStore(int storeId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_STORE_INFO);
      ps.setInt(1, storeId);

      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all store names.
   *
   * @return the all store names
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllStoreNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_STORE_NAMES);
  }

  /**
   * Gets the storewise stock details.
   *
   * @param medicineId
   *          the medicine id
   * @return the storewise stock details
   * @throws SQLException
   *           the SQL exception
   */
  public String getStorewiseStockDetails(String medicineId) throws SQLException {

    PreparedStatement ps = null;
    String storewiseStockDetails = null;
    try {
      ps = con.prepareStatement(GET_STORE_WISE_STOCK_DETAILS + "and pds.medicine_id= ?");
      ps.setString(1, medicineId);
      storewiseStockDetails = DataBaseUtil.getXmlContentWithNoChild(ps, "FIND"); 
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return storewiseStockDetails;
  }

  /**
   * Gets the store wise med.
   *
   * @param medId
   *          the med id
   * @return the store wise med
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getStoreWiseMed(String medId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List storeMedList = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_STORE_WISE_STOCK_DETAILS);
      ps.setInt(1, Integer.parseInt(medId));
      return storeMedList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * Insert stock levels.
   *
   * @param medicineID
   *          the medicine ID
   * @param storeStockarrayList
   *          the store stockarray list
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String insertStockLevels(String medicineID, List storeStockarrayList) throws SQLException {

    PreparedStatement ps = null;
    int resultCount = 0;
    try {
      ps = con.prepareStatement(INSERT_STORE_STOCK_LEVELS);
      Iterator itr = storeStockarrayList.iterator();
      while (itr.hasNext()) {
        StoreLevelDTO medicineStockLevels = (StoreLevelDTO) itr.next();
        ps.setString(1, medicineStockLevels.getDeptId());
        ps.setString(2, medicineID);
        ps.setFloat(3, medicineStockLevels.getMinmumLevel());
        ps.setFloat(4, medicineStockLevels.getMaximumLevel());
        ps.setFloat(5, medicineStockLevels.getReorderLevel());
        ps.setFloat(6, medicineStockLevels.getDangerLevel());
        ps.setString(7, medicineStockLevels.getBin());
        resultCount = resultCount + ps.executeUpdate();
      }
  
      if (resultCount == 0) {
        medicineID = null;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return medicineID;
  }

  /**
   * Update stock levels.
   *
   * @param medicinedto
   *          the medicinedto
   * @param storeStockarrayList
   *          the store stockarray list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateStockLevels(MedicineDTO medicinedto, List storeStockarrayList)
      throws SQLException {

    PreparedStatement ps = null;
    int resultCount = 0;
    boolean target = false;
    try {
      ps = con.prepareStatement(UPDATE_STORE_STOCK_LEVELS);
      Iterator itr = storeStockarrayList.iterator();
      while (itr.hasNext()) {
        StoreLevelDTO medicineStockLevels = (StoreLevelDTO) itr.next();
        ps.setFloat(1, medicineStockLevels.getMinmumLevel());
        ps.setFloat(2, medicineStockLevels.getMaximumLevel());
        ps.setFloat(3, medicineStockLevels.getReorderLevel());
        ps.setFloat(4, medicineStockLevels.getDangerLevel());
        ps.setString(5, medicineStockLevels.getDeptId());
        ps.setString(6, medicineStockLevels.getMedicineId());
        ps.setString(7, medicineStockLevels.getBin());
        resultCount = ps.executeUpdate();
        if (resultCount == 0) {
          try (PreparedStatement insertps = con.prepareStatement(INSERT_STORE_STOCK_LEVELS);) {
            insertps.setString(1, medicineStockLevels.getDeptId());
            insertps.setString(2, medicineStockLevels.getMedicineId());
            insertps.setFloat(3, medicineStockLevels.getMinmumLevel());
            insertps.setFloat(4, medicineStockLevels.getMaximumLevel());
            insertps.setFloat(5, medicineStockLevels.getReorderLevel());
            insertps.setFloat(6, medicineStockLevels.getDangerLevel());
            insertps.setString(7, medicineStockLevels.getBin());
            resultCount = insertps.executeUpdate();
          } finally {
            //
          }
        }
      }
      if (resultCount == 0) {
        target = false;
  
      } else {
        target = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return target;
  }

  /**
   * Delet stock level.
   *
   * @param deleteStockArrayList
   *          the delete stock array list
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deletStockLevel(List deleteStockArrayList) throws SQLException {

    PreparedStatement ps = null;
    boolean status = false;
    int resultCount = 0;
    try {
      ps = con.prepareStatement(DELETE_STATOC_LEVEL);
      Iterator itr = deleteStockArrayList.iterator();
      while (itr.hasNext()) {
  
        StoreLevelDTO deleteStoreStockLevels = (StoreLevelDTO) itr.next();
        ps.setString(1, deleteStoreStockLevels.getDeleteDeptId());
        ps.setString(2, deleteStoreStockLevels.getMedicineId());
  
        resultCount = resultCount + ps.executeUpdate();
      }
      if (resultCount > 0) {
        status = true;
      } else {
        status = false;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return status;
  }

  /** The Constant ITEM_DETAILS. */
  public static final String ITEM_DETAILS = "select distinct medicine_name, pmd.medicine_id,"
      + " pmsd.dept_id,pmd.cust_item_code from store_item_details pmd  "
      + "join  store_stock_details pmsd on pmd.medicine_id = pmsd.medicine_id  "
      + "join stores gd on pmsd.dept_id = gd.dept_id";

  /**
   * Gets the item names.
   *
   * @return the item names
   * @throws SQLException
   *           the SQL exception
   */
  public List<Hashtable> getItemNames() throws SQLException {
    PreparedStatement ps = null;
    try {

      ps = con.prepareStatement(ITEM_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant SUPPLIER_DETAILS. */
  // public static String SUPPLIER_DETAILS = "select
  // supplier_code,supplier_name,cust_supplier_code from supplier_master order by
  // supplier_name";
  public static final String SUPPLIER_DETAILS = "SELECT supplier_code,supplier_name,"
      + "cust_supplier_code, CASE WHEN cust_supplier_code IS NOT NULL AND"
      + "  TRIM(cust_supplier_code) != ''  THEN supplier_name||' - '||cust_supplier_code"
      + " ELSE supplier_name END as cust_supplier_code_with_name FROM supplier_master "
      + " ORDER BY supplier_name ";

  /**
   * Gets the supplier names.
   *
   * @return the supplier names
   * @throws SQLException
   *           the SQL exception
   */
  public List<Hashtable> getSupplierNames() throws SQLException {
    PreparedStatement ps = null;
    try {

      ps = con.prepareStatement(SUPPLIER_DETAILS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant GET_ALL_ACTIVE_SUPPLIERS. */
  private static final String GET_ALL_ACTIVE_SUPPLIERS = "SELECT supplier_code, supplier_name,"
      + " cust_supplier_code,  CASE WHEN cust_supplier_code IS NOT NULL AND "
      + " TRIM(cust_supplier_code) != ''  THEN supplier_name||' - '||cust_supplier_code"
      + " ELSE supplier_name END as cust_supplier_code_with_name "
      + " FROM supplier_master where status='A' ORDER BY supplier_name";

  /** The Constant CENTER_SUPPLIER_DETAILS. */
  public static final String CENTER_SUPPLIER_DETAILS = "select sm.supplier_code,supplier_name,"
      + " cust_supplier_code, coalesce(scm.center_id,0) as center_id, "
      + " CASE WHEN cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != ''  THEN"
      + " supplier_name||' - '||cust_supplier_code ELSE supplier_name END as"
      + " cust_supplier_code_with_name from supplier_master sm "
      + " left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + " where scm.status='A' and sm.status='A' and scm.center_id IN(?,0) order by"
      + " sm.supplier_name ";

  /**
   * Gets the center supplier names.
   *
   * @param centerId
   *          the center id
   * @return the center supplier names
   * @throws SQLException
   *           the SQL exception
   */
  public List<Hashtable> getCenterSupplierNames(Integer centerId) throws SQLException {
    PreparedStatement ps = null;
    try {

      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        ps = con.prepareStatement(CENTER_SUPPLIER_DETAILS);
        ps.setInt(1, centerId);
      } else {
        ps = con.prepareStatement(GET_ALL_ACTIVE_SUPPLIERS);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Gets the center supplier names.
   *
   * @return the center supplier names
   * @throws SQLException
   *           the SQL exception
   */
  public List<Hashtable> getCenterSupplierNames() throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ALL_ACTIVE_SUPPLIERS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant STORE_PURCHASES. */
  public static final String STORE_PURCHASES = " SELECT store_name, "
      + "  sum(item_amount+ced_amt -item_discount+item_tax +"
      + " other_charges-discount+round_off+cess) "
      + "  AS purchase_amt  FROM store_purchase_invoice_report_view "
      + " WHERE grn_date::DATE between ? AND ?  AND ( ? = 0 OR center_id = ? ) "
      + " GROUP BY store_name";

  /**
   * Gets the store purchases totals.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @param centerId
   *          the center id
   * @return the store purchases totals
   * @throws SQLException
   *           the SQL exception
   */
  public List getStorePurchasesTotals(java.sql.Date from, java.sql.Date to, int centerId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(STORE_PURCHASES);
      ps.setDate(1, from);
      ps.setDate(2, to);
      ps.setInt(3, centerId);
      ps.setInt(4, centerId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Exists in center.
   *
   * @param storeId
   *          the store id
   * @param centerId
   *          the center id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean existsInCenter(int storeId, int centerId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement("SELECT dept_id FROM stores where dept_id=? and center_id=?");
      ps.setInt(1, storeId);
      ps.setInt(2, centerId);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
      if (rs != null) {
        rs.close();
      }
    }
    return false;
  }

  /**
   * Gets the active insurance categories.
   *
   * @param medicineId
   *          the medicine id
   * @return the active insurance categories
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getActiveInsuranceCategories(Integer medicineId)
      throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try{
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(SELECT_INSURANCE_CATEGORY_IDS);
      ps.setInt(1, medicineId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally{
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SELECT_INSURANCE_CATEGORY_IDS. */
  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM store_items_insurance_category_mapping WHERE medicine_id =?";

}