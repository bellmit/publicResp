package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.RelevantSorting;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class PharmacymasterDAO extends GenericDAO {

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

  public PharmacymasterDAO() {
    super("store_item_details");
  }

  public PharmacymasterDAO(Connection con) {
    super("store_item_details");
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
    try (PreparedStatement ps = 
        con.prepareStatement("INSERT INTO GENERIC_NAME VALUES(?,?)");) {
      ps.setString(1, genericName);
      ps.setString(2, genericId);
      count = ps.executeUpdate();
    }
    if (count > 0)
      return genericId;
    return null;
  }

  /**
   * MANF_MASTER table insertion
   * 
   * @param manfName
   * @return
   * @throws SQLException
   */

  public String insertManfMaster(String manfName, String mnemonic) throws SQLException {
    PreparedStatement ps = null;
    int count = 0;
    String manfId = AutoIncrementId.getSequenceId("manufacturer_id_seq", "Manufacturer");
    try {
      ps = con.prepareStatement(
          "INSERT INTO MANF_MASTER (MANF_CODE,MANF_NAME,STATUS,MANF_MNEMONIC) VALUES(?,?,?,?)");
      ps.setString(1, manfId);
      ps.setString(2, manfName);
      ps.setString(3, "A");
      ps.setString(4, mnemonic);
      count = ps.executeUpdate();
    } finally {
      if (ps != null) { ps.close();}
    }
    if (count > 0)
      return manfId;
    return null;
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

  private static final String GET_ALL_SUPPLIER = "SELECT supplier_code, supplier_name,cust_supplier_code "
      + " FROM supplier_master ORDER BY supplier_name";

  public static final String GET_ALL_CENTER_SUPPLIERS = "select distinct sm.supplier_name, sm.*, scm.center_id,sm.cust_supplier_code, "
      + " CASE WHEN (cust_supplier_code IS NOT NULL AND  TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NULL OR TRIM(supplier_city) = '') "
      + " THEN supplier_name||' - '||cust_supplier_code "
      + " WHEN (supplier_city IS NOT NULL AND TRIM(supplier_city) != '')  AND  (cust_supplier_code IS NULL OR TRIM(cust_supplier_code) = '') "
      + " THEN supplier_name||' - '||supplier_city "
      + " WHEN (cust_supplier_code IS NOT NULL AND TRIM(cust_supplier_code) != '')  AND  (supplier_city IS NOT NULL AND TRIM(supplier_city) != '') "
      + " THEN supplier_name||' - '||cust_supplier_code||' - '||supplier_city "
      + " ELSE supplier_name END AS supplier_name_with_city " + " from supplier_master sm  "
      + " left join supplier_center_master scm on(scm.supplier_code=sm.supplier_code) "
      + " where scm.status='A' and sm.status='A' and scm.center_id IN(?,0) order by sm.supplier_name ";

  public static ArrayList getAllCenterSuppliers() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      int centerId = RequestContext.getCenterId();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        pstmt = con.prepareStatement(GET_ALL_CENTER_SUPPLIERS);
        pstmt.setInt(1, centerId);
      } else {
        pstmt = con.prepareStatement(GET_ALL_SUPPLIER);
      }
      return DataBaseUtil.queryToArrayList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
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
      ps.close();
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
      ps.close();
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
      ps.close();
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
    PreparedStatement ps = null;
    int count = 0;
    int catId = getNextCategoryId();
    try {
    ps = con.prepareStatement(
        "INSERT INTO STORE_CATEGORY_MASTER (CATEGORY_ID,CATEGORY,STATUS, claimable) VALUES(?,?,?,?)");
    ps.setInt(1, catId);
    ps.setString(2, catName);
    ps.setString(3, "A");
    ps.setBoolean(4, claimable);
    count = ps.executeUpdate();
    } finally {
      if (ps != null) { ps.close();}
    }
    if (count > 0)
      return new Integer(catId).toString();
    return null;
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
      + "FROM STORE_CATEGORY_MASTER WHERE category_id=?";

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
        dto.setCategoryName(rs.getString("CATEGORY_NAME"));
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

  private static final String MEDICINE_EXT_QUERY_TABLES = " FROM (SELECT pmd.medicine_id, pmd.medicine_name,pmd.cust_item_code, mm.manf_name, gn.generic_name, pmd.issue_base_unit, "
      + "			pmd.package_type, mc.category as category_name, pmd.status, mc.claimable, sic.control_type_name, "
      + "			pmd.med_category_id, pmd.issue_units, pmd.value, pmd.consumption_capacity, cum.consumption_uom, "
      + "			if.item_form_name, pmd.item_strength " + " 	 	FROM store_item_details pmd  "
      + "			JOIN manf_master mm ON pmd.manf_name=mm.manf_code "
      + " 			LEFT JOIN item_form_master if ON pmd.item_form_id=if.item_form_id "
      + " 			LEFT JOIN generic_name gn ON pmd.generic_name=gn.generic_code "
      + " 			JOIN store_category_master mc ON pmd.med_category_id=mc.category_id "
      + " 			LEFT JOIN store_item_controltype sic ON sic.control_type_id=pmd.control_type_id	"
      + "           LEFT JOIN consumption_uom_master cum ON cum.cons_uom_id=pmd.cons_uom_id"
      + " 	) AS FOO ";

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

  private static final String ITEM_RATES_QUERY_FIELDS = " SELECT *";
  private static final String ITEM_RATES_QUERY_COUNT = " SELECT COUNT(medicine_id) ";
  private static final String ITEM_RATES_QUERY_TABLES = " FROM ( SELECT pmd.medicine_id, pmd.medicine_name,pmd.cust_item_code, mm.manf_name, gn.generic_name, pmd.issue_base_unit, pmd.package_type, "
      + " mc.category AS category_name, pmd.status, pmd.med_category_id, pmd.issue_units, pmd.value, iir.issue_rate_expr "
      + " FROM store_item_issue_rates iir " + " JOIN store_item_details pmd USING (medicine_id) "
      + " LEFT JOIN manf_master mm ON pmd.manf_name=mm.manf_code "
      + " LEFT JOIN generic_name gn ON pmd.generic_name=gn.generic_code "
      + " LEFT JOIN store_category_master mc ON pmd.med_category_id=mc.category_id ) AS FOO";

  public static PagedList searchMedicineForIssueRates(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, ITEM_RATES_QUERY_FIELDS,
        ITEM_RATES_QUERY_COUNT, ITEM_RATES_QUERY_TABLES, listing);

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
    med.setConsumptionCapacity(rs.getFloat("consumption_capacity"));
    med.setConsumptionUom(rs.getString("consumption_uom"));
  }

  public static final String GET_MEDICINE_DETAILS =
      " SELECT pmd.medicine_name, pmd.medicine_id,pmd.cust_item_code, coalesce(gm.generic_name,"
          + "'') as generic_name, "
          + " pmd.created_timestamp, pmd.updated_timestamp, "
          + " pmd.composition, pmd.therapatic_use, pmd.status, pmd.issue_base_unit,pmd"
          + ".billing_group_id, sic.control_type_name, value, "
          + "	mm.manf_name, mm.manf_mnemonic, pmd.package_type, pmd.issue_units, pmd"
          + ".medicine_short_name,cum.consumption_uom,cum.cons_uom_id,"
          + "	pmd.consumption_capacity, icm.category AS category_name, icm.claimable, pmd"
          + ".med_category_id, mm.manf_code, max_cost_price, "
          + "	icm.integration_category_id, supplier_name, obsolete_preferred_supplier, "
          + "invoice_details, "
          + "	pmd.service_sub_group_id, ssg.service_group_id,item_barcode_id,pmd"
          + ".control_type_id,"
          + "	sic.control_type_name, pmd.insurance_category_id, route_of_admin, "
          + "prior_auth_required, pmd.package_uom, pmd.allow_zero_claim_amount, "
          + "	ifm.item_form_id, item_strength, item_strength_units, identification,tax_type,"
          + "tax_rate,bin,batch_no_applicable,item_selling_price, high_cost_consumable, "
          + "   gm.integration_generic_name_id, gm.generic_name, gm.generic_code,"
          + " mm.integration_manf_id, ifm.item_form_name, ifm.integration_form_id, piu.uom_id, "
          + " su.unit_name, su.integration_strength_unit_id"
          + " FROM store_item_details pmd "
          + "   LEFT OUTER JOIN generic_name gm on gm.generic_code=pmd.generic_name "
          + "   LEFT OUTER JOIN manf_master mm on mm.manf_code=pmd.manf_name "
          + "   LEFT OUTER JOIN store_category_master icm on pmd.med_category_id=icm.category_id "
          + "   LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = pmd"
          + ".service_sub_group_id) "
          + "   LEFT JOIN store_item_controltype sic ON (sic.control_type_id = pmd"
          + ".control_type_id) "
          + "   LEFT JOIN item_form_master ifm ON (pmd.item_form_id = ifm.item_form_id)"
          + "   LEFT JOIN package_issue_uom piu ON (pmd.package_uom = piu.package_uom AND "
          + "piu.issue_uom = pmd.issue_units) "
          + "   LEFT JOIN strength_units su ON (pmd.item_strength_units = su.unit_id)"
          + " LEFT JOIN consumption_uom_master cum ON (pmd.cons_uom_id = cum.cons_uom_id)"
          + " WHERE pmd.medicine_id=?";

  public static BasicDynaBean getSelectedMedicineDetails(String medId) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_MEDICINE_DETAILS);
      ps.setInt(1, Integer.parseInt(medId));
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

  private static final String GET_IDENTIFICATIONS = "SELECT category_id, identification FROM store_category_master";
  public static List getCategoryIdentifiactions() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = con.prepareStatement(GET_IDENTIFICATIONS);
    List list = null;
    try {
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      con.close();
      ps.close();
    }
    return list;
  }

  private static String GET_ALL_MEDICINE_CATEGORY_NAMES = "SELECT category_id,category FROM store_category_master ";

  public static List getAllMedicineCategoryNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_MEDICINE_CATEGORY_NAMES);
  }

  public static String getGenNameToId(Connection con, String genericName) throws SQLException {
    PharmacymasterDAO masterDAO = new PharmacymasterDAO(con);
    String genericId = StoresDBTablesUtil.genericNameToId(genericName);
    if (genericId == null || genericId.equals("")) {
      if (!genericName.equals("")) {
        genericId = masterDAO.insertGenericMaster(genericName);
      }
    }
    return genericId;
  }

  public static String getCatNameToId(Connection con, String catName, boolean claimable)
      throws SQLException {
    PharmacymasterDAO masterDAO = new PharmacymasterDAO(con);
    String catId = StoresDBTablesUtil.categoryNameToId(catName);
    if (catId == null || catId == "") {
      if (!catName.equals("")) {
        catId = masterDAO.insertCategoryMaster(catName, claimable);
      }
    }
    return catId;
  }

  public static String getManfNameToId(Connection con, String manfName) throws SQLException {
    PharmacymasterDAO masterDAO = new PharmacymasterDAO(con);
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

  public static int getNextValFromSequence() {
    Connection con = null;
    PreparedStatement ps = null;
    int i = 0;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT nextVal('item_id_seq')");
      i = DataBaseUtil.getIntValueFromDb(ps);
    } catch (SQLException e) {

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return i;
  }
  private static final String UPDATE_MRP_ETC = "update store_stock_details set mrp=mrp*?,package_cp=package_cp*?,"
      + " package_sp=package_sp*?,tax=tax*?, username = ?, change_source=? where medicine_id=?";

  public static boolean updateMedicineMRP(Connection con, int itemId, float newpkgsize,
      float oldpkgsize, String username) throws SQLException {

    boolean target = false;
    int resultCount = 0;
    float varyingPer = newpkgsize / oldpkgsize;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_MRP_ETC);) {
      ps.setFloat(1, varyingPer);
      ps.setFloat(2, varyingPer);
      ps.setFloat(3, varyingPer);
      ps.setFloat(4, varyingPer);
      ps.setString(5, username);
      ps.setString(6, "MRP Edit");
      ps.setInt(7, itemId);
      resultCount = ps.executeUpdate();
      if (resultCount > 0) {
        target = true;
  
      } else {
        target = false;
      }
    }
    return target;
  }

  private static final String STOREITEMS_NAMESANDiDS = "SELECT medicine_name,medicine_id FROM store_item_details s ";

  public static List getStoreItemsNamesAndIds() throws SQLException {
    return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(STOREITEMS_NAMESANDiDS));
  }

  public static List getStoreBillableItemsNamesAndIds() throws SQLException {
    return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(STOREITEMS_NAMESANDiDS
        + " JOIN store_category_master c ON (c.category_id = s.med_category_id) WHERE billable"));
  }
  private static final String ROUTE_OF_ADMINISTRATIONS_PHARMA = " SELECT textcat_commacat(route_name) as route_name, textcat_commacat(route_id||'') as route_id "
      + " FROM medicine_route where route_id::text IN "
      + "	(SELECT regexp_split_to_table(route_of_admin, ',') from store_item_details where medicine_id=?)";
  private static final String ROUTE_OF_ADMINISTRATIONS_OUTSIDE = " SELECT textcat_commacat(route_name) as route_name, textcat_commacat(route_id||'') as route_id "
      + " FROM medicine_route where route_id::text IN "
      + "	(SELECT regexp_split_to_table(route_of_admin, ',') from prescribed_medicines_master where medicine_name=?) ";
  private static final String ROUTE_OF_ADMINISTRATIONS_LIST = " SELECT sid.medicine_id ,textcat_commacat(route_name) as route_name, textcat_commacat(mr.route_id||'') as route_id "
      + " FROM medicine_route mr "
      + "	JOIN (SELECT regexp_split_to_table(route_of_admin, ',') as route_id, medicine_id from store_item_details) sid "
      + "		ON (mr.route_id::text=sid.route_id)";
  public static BasicDynaBean getRoutesOfAdministrations(String medicineIdOrName,
      String use_store_items) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(use_store_items.equals("Y")
          ? ROUTE_OF_ADMINISTRATIONS_PHARMA
          : ROUTE_OF_ADMINISTRATIONS_OUTSIDE);
      if (use_store_items.equals("Y"))
        ps.setInt(1, Integer.parseInt(medicineIdOrName));
      else
        ps.setString(1, medicineIdOrName);
      List l = DataBaseUtil.queryToDynaList(ps);
      if (l != null && !l.isEmpty())
        return (BasicDynaBean) l.get(0);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }
  public static List<BasicDynaBean> getRoutesOfAdministrationsList(List<Integer> medicineIds)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    StringBuilder str = new StringBuilder(ROUTE_OF_ADMINISTRATIONS_LIST);
    try {
      Iterator<Integer> list = medicineIds.iterator();
      str.append(" WHERE medicine_id IN (");
      while (list.hasNext()) {
        str.append(list.next());
        if (list.hasNext())
          str.append(",");
      }
      str.append(")");
      str.append(" GROUP BY medicine_id ");
      ps = con.prepareStatement(str.toString());
      List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
      return l;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String GET_ITEM_MASTER_TIMESTAMP = "SELECT item_timestamp FROM store_item_timestamp";

  public static int getItemMasterTimestamp() throws SQLException {
    return DataBaseUtil.getIntValueFromDb(GET_ITEM_MASTER_TIMESTAMP);
  }

  public static final String GET_ITEM_MASTER = " SELECT std.medicine_name, std.medicine_id, std.max_cost_price, std.item_barcode_id,std.issue_base_unit, std.cust_item_code, "
      + " CASE WHEN std.cust_item_code IS NOT NULL AND  TRIM(std.cust_item_code) != ''  THEN std.medicine_name||' - '||std.cust_item_code ELSE std.medicine_name END as cust_item_code_with_name "
      + " FROM store_item_details std "
      + " JOIN manf_master mf ON ( mf.manf_code = std.manf_name ) "
      + " JOIN  store_category_master scm ON ( category_id = med_category_id ) "
      + " LEFT JOIN generic_name gn ON std.generic_name = generic_code and gn.status='A' "
      + " WHERE std.status='A' ";

  public static final String GET_ITEM_MASTER_ORDER = " ORDER BY medicine_name";

  public static List<BasicDynaBean> getItemNames(Boolean retailable, Boolean billable,
      String[] issueType, String searchQuery, Boolean isBarCodeSearch) throws SQLException {

    StringBuilder query = new StringBuilder(GET_ITEM_MASTER);

    if (retailable != null) {
      query.append(" AND retailable = " + retailable);
    }

    if (billable != null) {
      query.append(" AND billable = " + billable);
    }

    List<Object> args = new ArrayList<>();
    if (issueType != null) {                                                                                                                                                                                            
          query.append(" AND issue_type IN (");                                                                                                                                                                             
      String[] placeholdersArr = new String[issueType.length];                                                                                                                                                          
      Arrays.fill(placeholdersArr, "?");                                                                                                                                                                                
      query.append(StringUtils.arrayToCommaDelimitedString(placeholdersArr));                                                                                                                                           
      query.append(")");                                                                                                                                                                                                
      args.addAll(Arrays.asList(issueType));                                                                                                                                                                            
    }                                                                                                                                                                                                                   
                                                                                                                                                                                                                    
    if (searchQuery != null && !searchQuery.trim().equals("")) {                                                                                                                                             
      if (isBarCodeSearch) {                                                                                                                                                                                            
        query.append(" AND std.item_barcode_id = ?");                                                                                                                                                                   
        args.add(searchQuery.trim());                                                                                                                                                                                   
      } else {
        String[] searchTextArray = searchQuery.toLowerCase().replace("%", "\\%")
            .replace("\\", "\\\\").split(" ");
        for(String searchText: searchTextArray){
          query.append(" AND (lower(std.medicine_name) like '").append(searchText)
              .append("%' or lower(std.medicine_name) like '% ").append(searchText)
              .append("%' OR lower(std.cust_item_code) like '").append(searchText)
              .append("%' or lower(std.cust_item_code) like '% ").append(searchText).append("%')");
        }
        query.append(" LIMIT 200 ");
      }                                                                                                                                                                                                                 
    }    

    List<BasicDynaBean> itemNames = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query.toString())) {
      ListIterator<Object> argsIterator = args.listIterator();
      while (argsIterator.hasNext()) {
        ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
      }
      itemNames = DataBaseUtil.queryToDynaList(ps);
    }

    return itemNames;
  }

  public static List<Map> getItemNamesMap(Boolean retailable, Boolean billable,
      String[] issueType, String searchQuery,Boolean isBarCodeSearch) throws SQLException {
    List<BasicDynaBean> medicines = getItemNames(retailable, billable, issueType,
        searchQuery, isBarCodeSearch);
    if (searchQuery != null && !searchQuery.trim().equals("")) {
      return ConversionUtils.listBeanToListMap(RelevantSorting.rankBasedSorting(medicines, 
          searchQuery, "cust_item_code_with_name"));
    }
    return ConversionUtils.listBeanToListMap(medicines);
  }

}
