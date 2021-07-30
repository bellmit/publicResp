package com.insta.hms.mdm.testequipments;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestEquipmentRepository.
 */
@Repository
public class TestEquipmentRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new test equipment repository.
   */
  public TestEquipmentRepository() {
    super("test_equipment_master", "eq_id", "equipment_name");
  }

  /** The test equipment fields. */
  private String testEquipmentFields = 
      "SELECT eq_id,equipment_name,tm.status,schedule,overbook_limit, "
      + " hl7_export_code, tm.center_id, hcm.center_name, tm.ddept_id, dd.ddept_name ";

  /** The Constant TEST_EQUIPMENT_COUNT. */
  private static final String TEST_EQUIPMENT_COUNT = " SELECT COUNT(*)";

  /** The test equipment tables. */
  private String testEquipmentTables = "FROM test_equipment_master tm "
      + " JOIN hospital_center_master hcm ON(tm.center_id = hcm.center_id) "
      + " LEFT JOIN diagnostics_departments dd ON(tm.ddept_id = dd.ddept_id)";

  private String testEquipmentsByCenterFilter = "SELECT eq_id,equipment_name FROM "
      + " test_equipment_master tm JOIN hospital_center_master "
      + " hcm ON(tm.center_id = hcm.center_id) "
      + " LEFT JOIN diagnostics_departments dd ON(tm.ddept_id = dd.ddept_id) "
      + " where tm.status='A' and tm.schedule='t' ##CENTERFILTER## ";

  private String testEquipmentsByTestId = "SELECT tm.eq_id,equipment_name FROM "
      + " test_equipment_master tm "
      + " JOIN diagnostics_test_equipment_master_mapping dtem ON (dtem.eq_id = tm.eq_id) "
      + " JOIN diagnostics d ON (d.test_id = dtem.test_id) "
      + " WHERE tm.status ='A' and tm.schedule='t' "
      + " and dtem.test_id = ?";

  /**
   * Gets the search equipment list.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the search equipment list
   */
  public SearchQueryAssembler getSearchEquipmentList(Map params,
      Map<LISTING, Object> listingParams) {
    SearchQueryAssembler qa = null;
    int centerId = RequestContext.getCenterId();
    qa = new SearchQueryAssembler(testEquipmentFields, TEST_EQUIPMENT_COUNT,
        testEquipmentTables, listingParams);
    qa.addFilterFromParamMap(params);
    if (centerId != 0) {
      qa.addFilter(QueryBuilder.INTEGER, "hcm.center_id", "=", new Integer(centerId));
    }

    String[] overbkLmt = (String[]) params.get("_overbook_limit");
    if (overbkLmt != null && !overbkLmt.equals("") && overbkLmt.length > 0) {
      if (!overbkLmt[0].equals("")) {
        boolean overbookLimit = new Boolean(overbkLmt[0]);
        if (overbookLimit) {
          qa.addFilter(QueryBuilder.BOOLEAN, "COALESCE(tm.overbook_limit, 1) > 0 ", "=", true);
        } else {
          qa.addFilter(QueryBuilder.INTEGER, "tm.overbook_limit", "=", new Integer(0));
        }
      }
    }
    return qa;
  }

  /**
   * Insert test equipment.
   *
   * @param testeEuipmentBean
   *          the teste euipment bean
   * @return true, if successful
   */
  public boolean insertTestEquipment(BasicDynaBean testeEuipmentBean) {

    int result = insert(testeEuipmentBean);
    if (result < 0) {
      return false;
    } else {
      return true;
    }
  }

  /** The Constant GET_TEST_EQUIPMENT_BEAN. */
  private static final String GET_TEST_EQUIPMENT_BEAN = 
      "SELECT tem.*, hcm.center_name FROM test_equipment_master tem " 
      + " JOIN hospital_center_master hcm ON (hcm.center_id=tem.center_id) where eq_id=?";

  
  /**
   * Gets the test equipment bean.
   *
   * @param eqId the eq id
   * @return the test equipment bean
   */
  public BasicDynaBean getTestEquipmentBean(String eqId) {
    return DatabaseHelper.queryToDynaBean(GET_TEST_EQUIPMENT_BEAN,
        new Object[] { Integer.parseInt(eqId) });
  }

  /**
   * Update test equipment.
   *
   * @param testeEuipmentBean
   *          the teste euipment bean
   * @param keys
   *          the keys
   * @return true, if successful
   */
  public boolean updateTestEquipment(BasicDynaBean testeEuipmentBean, Map<String, Integer> keys) {

    int result = update(testeEuipmentBean, keys);
    if (result > 0) {
      return true;
    } else {
      return false;
    }
  }

  /** The Constant TEST_EQUIPMENT_OVERBOOK. */
  private static final String TEST_EQUIPMENT_OVERBOOK = 
      "SELECT overbook_limit from test_equipment_master where eq_id=?";

  /**
  *  Gets the test equipment overbook limit.
  *
  * @param eqId
  *          the eq id
  * @return the test equipment overbook limit
  */
  public Integer getTestEquipmentOverbookLimit(String eqId) {
    return DatabaseHelper.getInteger(TEST_EQUIPMENT_OVERBOOK, Integer.parseInt(eqId));
  }

  /** The Constant TEST_EQUIPMENT_NAME. */
  private static final String TEST_EQUIPMENT_NAME = 
      "SELECT equipment_name from test_equipment_master where eq_id=?";

  /**
  * Gets the test equipment name.
  *
  * @param eqId
  *          the eq id
  * @return the test equipment name
  */
  public String getTestEquipmentName(String eqId) {
    return DatabaseHelper.getString(TEST_EQUIPMENT_NAME, Integer.parseInt(eqId));
  }

  /**
  * Gets the test Equipment mapped to center.
  * @return BasicDynaBean of test equipments mapped to center
  */
  public List<BasicDynaBean> getTestEquipmentsByCenter() {
    int centerId = RequestContext.getCenterId();
    String query = testEquipmentsByCenterFilter;
    if (centerId != 0) {
      query = query.replace("##CENTERFILTER##", "and tm.center_id = ?");
      return DatabaseHelper.queryToDynaList(query, new Object[]{centerId});
    } else {
      query = query.replace("##CENTERFILTER##", "");
      return DatabaseHelper.queryToDynaList(query);
    }
  }

  /**
  * Gets the test equipments mapped to test.
  * @param testId test id
  * @return BasicDynaBean of test equipments mapped to test
  */
  public List<BasicDynaBean> getTestEquipments(String testId) {
    return DatabaseHelper.queryToDynaList(testEquipmentsByTestId, new Object[]{testId});
  }

}
