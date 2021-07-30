package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StoresMasterDAO {

  /*
   * Constants used for sort order
   */
  public static final int FIELD_NONE = 0;
  public static final int FIELD_CATEGORY = 1;

  public static final int FIELD_MEDICINE = 1;
  public static final int FIELD_MANUF = 2;
  public static final int FIELD_GENERIC = 3;
  public static final int FIELD_MEDCATEGORY = 4;
  Connection con = null;

  StoresMasterDAO() {
  }

  public StoresMasterDAO(Connection con) {
    this.con = con;
  }
  /**
   * GENERIC_MASTER table insertion
   * 
   * @param genericName
   * @return genericId
   * @throws SQLException
   */

  public String insertGenericMaster(String genericName) throws SQLException {
    int count = 0;
    String genericId = AutoIncrementId.getSequenceId("generic_sequence", "GENERICNAME");
    try (PreparedStatement ps = con.prepareStatement("INSERT INTO GENERIC_NAME VALUES(?,?)");) {
      ps.setString(1, genericName);
      ps.setString(2, genericId);
      count = ps.executeUpdate();
      if (count > 0) {
        return genericId;
      }
      return null;
    }
  }

  /**
   * MANF_MASTER table insertion
   * 
   * @param manfName
   * @return
   * @throws SQLException
   */

  public String insertManfMaster(String manfName, String mnemonic) throws SQLException {
    int count = 0;
    String manfId = AutoIncrementId.getSequenceId("manufacturer_id_seq", "Manufacturer");
    try (PreparedStatement ps = con.prepareStatement(
        "INSERT INTO MANF_MASTER (MANF_CODE,MANF_NAME,STATUS,MANF_MNEMONIC) VALUES(?,?,?,?)");) {
      ps.setString(1, manfId);
      ps.setString(2, manfName);
      ps.setString(3, "A");
      ps.setString(4, mnemonic);
      count = ps.executeUpdate();
      if (count > 0) {
        return manfId;
      }
      return null;
    }
  }

  private static final String GET_ALL_SUPPLIERS = "SELECT supplier_code, supplier_name,cust_supplier_code "
      + " FROM supplier_master ORDER BY supplier_name";

  public static ArrayList getAllSuppliers() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_SUPPLIERS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static List getAllSuppliersNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_SUPPLIERS);
  }

  private static final String GET_ALL_STORES = "SELECT dept_id, dept_name FROM stores ";

  public static ArrayList getAllStores() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_STORES);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_MED_LIST = "SELECT DISTINCT MEDICINE_ID,MEDICINE_NAME FROM store_item_details";

  public static ArrayList getAllMedicines() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MED_LIST);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String INSERT_MED_STORE = "INSERT INTO item_store_level_details(DEPT_ID,MEDICINE_ID,"
      + " MIN_LEVEL,MAX_LEVEL,REORDER_LEVEL,DANGER_LEVEL) VALUES(?,?,?,?,?,?)";

  private static final String UPDATE_MED_STORE = "UPDATE item_store_level_details SET MIN_LEVEL=?,MAX_LEVEL=?,"
      + " REORDER_LEVEL=?,DANGER_LEVEL=? WHERE DEPT_ID=? AND MEDICINE_ID=?";

  private static final String DELETE_MED_STORE = "DELETE FROM item_store_level_details WHERE DEPT_ID=? AND MEDICINE_ID=?";

  public boolean insertStore(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(INSERT_MED_STORE);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        StoreLevelDTO stock = (StoreLevelDTO) iterator.next();
        ps.setString(1, stock.getDeptId());
        ps.setString(2, stock.getMedicineId());
        ps.setFloat(3, stock.getMinmumLevel());
        ps.setFloat(4, stock.getMaximumLevel());
        ps.setFloat(5, stock.getReorderLevel());
        ps.setFloat(6, stock.getDangerLevel());
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

  public boolean updateStore(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_MED_STORE);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        StoreLevelDTO stock = (StoreLevelDTO) iterator.next();
        ps.setFloat(1, stock.getMinmumLevel());
        ps.setFloat(2, stock.getMaximumLevel());
        ps.setFloat(3, stock.getReorderLevel());
        ps.setFloat(4, stock.getDangerLevel());
        ps.setString(5, stock.getDeptId());
        ps.setString(6, stock.getMedicineId());

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

  public boolean deleteStore(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(DELETE_MED_STORE);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        StoreLevelDTO stock = (StoreLevelDTO) iterator.next();
        ps.setString(1, stock.getDeptId());
        ps.setString(2, stock.getMedicineId());
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

  public int getNextCategoryId() throws SQLException {
    PreparedStatement ps = con.prepareStatement("select nextval('store_category_sequence')");
    return DataBaseUtil.getIntValueFromDb(ps);
  }

  public String insertCategoryMaster(String catName, boolean claimable) throws SQLException {
    int count = 0;
    int catId = getNextCategoryId();
    try (PreparedStatement ps = con.prepareStatement(
        "INSERT INTO STORE_CATEGORY_MASTER (CATEGORY_ID,CATEGORY,STATUS, claimable) VALUES(?,?,?,?)");) {
      ps.setInt(1, catId);
      ps.setString(2, catName);
      ps.setString(3, "A");
      ps.setBoolean(4, claimable);
      count = ps.executeUpdate();
      if (count > 0) {
        return new Integer(catId).toString();
      }
      return null;
    }
  }

  private static final String CATEGORY_EXT_QUERY_FIELDS = " SELECT CATEGORY_NAME,STATUS,CATEGORY_ID";

  private static final String CATEGORY_EXT_QUERY_COUNT = " SELECT count(CATEGORY_ID) ";

  private static final String CATEGORY_EXT_QUERY_TABLES = " FROM STORE_CATEGORY_MASTER ";

  public static PagedList searchMedCategory(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, CATEGORY_EXT_QUERY_FIELDS,
        CATEGORY_EXT_QUERY_COUNT, CATEGORY_EXT_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("category_id");
    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }

  private static void populateValues(PharmacyMasterDTO cat, ResultSet rs) throws SQLException {
    cat.setCategoryName(rs.getString("category_name"));
    cat.setStatus(rs.getString("status"));
    cat.setCategoryId(rs.getInt("category_id"));
  }

  private static final String GET_CATEGORY_DET = "SELECT category_id, category, claimable, status "
      + "FROM store_category_master WHERE category_id=?";

  public static PharmacyMasterDTO getSelectedCategoryDetails(int catId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    PharmacyMasterDTO dto = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_CATEGORY_DET);
      ps.setInt(1, catId);
      rs = ps.executeQuery();

      if (rs.next()) {
        dto = new PharmacyMasterDTO();
        dto.setCategoryName(rs.getString("CATEGORY"));
        dto.setCategoryId(rs.getInt("CATEGORY_ID"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setClaimable(rs.getBoolean("claimable"));
      }
      return dto;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

  }

  private static final String GETCATEGORYS = "SELECT * FROM STORE_CATEGORY_MASTER  ORDER BY CATEGORY";

  public static ArrayList getCategoryNamesInMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GETCATEGORYS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String UPDATE_CATEGORY = " UPDATE STORE_CATEGORY_MASTER SET CATEGORY=?,STATUS=?, claimable = ? WHERE CATEGORY_ID=?";

  public boolean updateCategory(PharmacyMasterDTO dto) throws SQLException {
    PreparedStatement ps = null;
    boolean status = false;
    int count = 0;
    try {
      ps = con.prepareStatement(UPDATE_CATEGORY);
      ps.setString(1, dto.getCategoryName());
      ps.setString(2, dto.getStatus());
      ps.setBoolean(3, dto.getClaimable());
      ps.setInt(4, dto.getCategoryId());

      count = ps.executeUpdate();
      if (count > 0) {
        status = true;
      }
      return status;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public boolean insertCategory(PharmacyMasterDTO dto) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    boolean status = false;
    int catId = getNextCategoryId();
    try {
      ps = con.prepareStatement(
          "INSERT INTO STORE_CATEGORY_MASTER (CATEGORY_ID,CATEGORY,STATUS, claimable) VALUES(?,?,?,?)");
      ps.setInt(1, catId);
      ps.setString(2, dto.getCategoryName());
      ps.setString(3, dto.getStatus());
      ps.setBoolean(4, dto.getClaimable());
      count = ps.executeUpdate();
      if (count > 0)
        status = true;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return status;
  }

  private static final String MEDICINE_EXT_QUERY_FIELDS = " SELECT *";

  private static final String MEDICINE_EXT_QUERY_COUNT = " SELECT count(MEDICINE_ID) ";

  private static final String MEDICINE_EXT_QUERY_TABLES = " FROM( SELECT PMD.MEDICINE_ID,PMD.MEDICINE_NAME,PMD.CUST_ITEM_CODE,MM.MANF_NAME,GN.GENERIC_NAME,PMD.ISSUE_BASE_UNIT,"
      + " MC.CATEGORY AS CATEGORY_NAME,PMD.STATUS,MC.CLAIMABLE"
      + " FROM store_item_details pmd  join " + " MANF_MASTER MM ON PMD.MANF_NAME=MM.MANF_CODE"
      + " LEFT JOIN GENERIC_NAME GN ON PMD.GENERIC_NAME=GN.GENERIC_CODE"
      + " JOIN STORE_CATEGORY_MASTER MC ON PMD.MED_CATEGORY_ID=MC.CATEGORY_ID) AS FOO";

  public static PagedList searchMedicine(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, MEDICINE_EXT_QUERY_FIELDS,
        MEDICINE_EXT_QUERY_COUNT, MEDICINE_EXT_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("medicine_id");
    qb.build();

    PagedList l = qb.getMappedPagedList();

    qb.close();
    con.close();

    return l;
  }

  private static void populateMedValues(PharmacyMasterDTO med, ResultSet rs) throws SQLException {
    med.setMedicineId(rs.getString("medicine_id"));
    med.setSearchMedName(rs.getString("medicine_name"));
    med.setSearchManfName(rs.getString("manf_name"));
    med.setSearchGenName(rs.getString("generic_name"));
    med.setSearchMedCatName(rs.getString("category_name"));
    med.setIssuPerBaseUnit(rs.getFloat("issue_base_unit"));
    med.setStatus(rs.getString("status"));
  }

  private static final String GET_MEDICINE_DETAILS = "SELECT pmd.medicine_name,pmd.medicine_id,coalesce(gm.generic_name,'')as generic_name,pmd.composition,"
      + " pmd.therapatic_use,pmd.status,pmd.issue_base_unit,sic.control_type_name,mm.manf_name,pmd.package_type,pmd.issue_units,pmd.medicine_short_name,"
      + " mc.category as category_name,mc.claimable,pmd.cust_item_code FROM store_item_details pmd "
      + " left outer join generic_name gm on gm.generic_code=pmd.generic_name "
      + " left outer join manf_master mm on mm.manf_code=pmd.manf_name "
      + " left outer join STORE_CATEGORY_MASTER mc on pmd.med_category_id=mc.category_id "
      + " left outer join store_item_controltype sic on sic.control_type_id = pmd.control_type_id "
      + " where pmd.medicine_id=?";

  public static BasicDynaBean getSelectedMedicineDetails(String medId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MEDICINE_DETAILS);
      ps.setString(1, medId);
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

  private static String GET_ALL_MEDICINE_CATEGORY_NAMES = "SELECT category_id,category FROM STORE_CATEGORY_MASTER ";

  public static List getAllMedicineCategoryNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_MEDICINE_CATEGORY_NAMES);
  }

  public static String getGenNameToId(Connection con, String genericName) throws SQLException {
    StoresMasterDAO masterDAO = new StoresMasterDAO(con);
    String genericId = StoresDBTablesUtil.genericNameToId(genericName);
    if (genericId == null || genericId == "") {
      if (!genericName.equals("")) {
        genericId = masterDAO.insertGenericMaster(genericName);
      }
    }
    return genericId;
  }

  public static String getCatNameToId(Connection con, String catName, boolean claimable)
      throws SQLException {
    StoresMasterDAO masterDAO = new StoresMasterDAO(con);
    String catId = StoresDBTablesUtil.categoryNameToId(catName);
    if (catId == null || catId == "") {
      if (!catName.equals("")) {
        catId = masterDAO.insertCategoryMaster(catName, claimable);
      }
    }
    return catId;
  }

  public static String getManfNameToId(Connection con, String manfName) throws SQLException {
    StoresMasterDAO masterDAO = new StoresMasterDAO(con);
    String manfId = StoresDBTablesUtil.manfNameToId(manfName);
    if (manfId == null || manfId == "") {
      if (!manfName.equals("")) {
        String manfCode = manfName.length() > 4
            ? manfName.substring(0, 4)
            : manfName.substring(0, manfName.length());
        manfId = masterDAO.insertManfMaster(manfName, manfCode);
      }
    }
    return manfId;
  }

}
