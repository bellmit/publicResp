package com.insta.hms.testequipmentmasters;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestEquipmentMasterDAO.
 */
public class TestEquipmentMasterDAO extends GenericDAO {

  /**
   * Instantiates a new test equipment master DAO.
   */
  public TestEquipmentMasterDAO() {
    super("test_equipment_master");

  }

  /**
   * Gets the record.
   *
   * @param equipId
   *          the equip id
   * @return the record
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getRecord(int equipId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT tem.*, hcm.center_name FROM test_equipment_master tem "
          + " JOIN hospital_center_master hcm ON (hcm.center_id=tem.center_id) where eq_id=?");
      ps.setInt(1, equipId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The test equipment fields. */
  private static final String TEST_EQUIPMENT_FIELDS = "SELECT eq_id,equipment_name,tm.status,"
      + "schedule,overbook_limit, hl7_export_code, tm.center_id, hcm.center_name, tm.ddept_id,"
      + " dd.ddept_name ";

  /** The Constant TEST_EQUIPMENT_COUNT. */
  private static final String TEST_EQUIPMENT_COUNT = " SELECT COUNT(*)";

  /** The test equipment tables. */
  private static final String TEST_EQUIPMENT_TABLES = "FROM test_equipment_master tm "
      + " JOIN hospital_center_master hcm ON(tm.center_id = hcm.center_id) "
      + " LEFT JOIN diagnostics_departments dd ON(tm.ddept_id = dd.ddept_id)";

  /**
   * Gets the equipment list.
   *
   * @param params
   *          the params
   * @param paginParam
   *          the pagin param
   * @param centerId
   *          the center id
   * @return the equipment list
   * @throws Exception
   *           the exception
   */
  public PagedList getEquipmentList(Map params, Map paginParam, int centerId) throws Exception {

    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, TEST_EQUIPMENT_FIELDS, TEST_EQUIPMENT_COUNT,
        TEST_EQUIPMENT_TABLES, paginParam);
    qb.addFilterFromParamMap(params);
    if (centerId != 0) {
      qb.addFilter(SearchQueryBuilder.INTEGER, "hcm.center_id", "=", new Integer(centerId));
    }

    String[] overbkLmt = (String[]) params.get("_overbook_limit");
    if (overbkLmt != null && !overbkLmt.equals("") && overbkLmt.length > 0) {
      if (!overbkLmt[0].equals("")) {
        boolean overbookLimit = new Boolean(overbkLmt[0]);
        if (overbookLimit) {
          qb.addFilter(SearchQueryBuilder.BOOLEAN, "COALESCE(tm.overbook_limit, 1) > 0 ", "=",
              true);
        } else {
          qb.addFilter(SearchQueryBuilder.INTEGER, "tm.overbook_limit", "=", new Integer(0));
        }
      }
    }

    qb.build();

    PagedList list = qb.getMappedPagedList();
    con.close();

    return list;
  }

  /** The Constant GET_ALL_EQUIPMENT. */
  public static final String GET_ALL_EQUIPMENT = " SELECT eq_id,equipment_name FROM"
      + " test_equipment_master ";

  /**
   * Gets the equipment names.
   *
   * @return the equipment names
   */
  public static List getEquipmentNames() {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList equipmentList = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_EQUIPMENT);
      equipmentList = DataBaseUtil.queryToArrayList(ps);

    } catch (SQLException ex) {
      Logger.log(ex);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return equipmentList;
  }
}