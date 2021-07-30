package com.insta.hms.mdm.diagnostics;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.diagnosticsmasters.addtest.TestCharge;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.mdm.bulk.BulkDataRepository;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/** The Class DiagnosticTestRepository. */
@Repository
public class DiagnosticTestRepository extends BulkDataRepository<String> {

  /** Instantiates a new diagnostic test repository. */
  public DiagnosticTestRepository() {
    super("diagnostics", "test_id", null);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.MasterRepository#getSearchQuery()
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(SEARCH_TEST_TABLES);
  }

  /** The Constant SEARCH_TEST_FIELDS. */
  private static final String SEARCH_TEST_FIELDS = "SELECT * ";

  /** The Constant SEARCH_TEST_COUNT. */
  private static final String SEARCH_TEST_COUNT = "SELECT count(*) ";

  /** The Constant SEARCH_TEST_TABLES. */
  private static final String SEARCH_TEST_TABLES =
      " FROM (SELECT tod.test_id, tod.applicable,"
          + " tod.item_code,d.diag_code AS alias_item_code, d.test_name, d.status,"
          + " d.conduction_format,"
          + " d.ddept_id, dd.ddept_name, "
          + " tod.org_id, tod.code_type, d.service_sub_group_id,od.org_name,"
          + " 'diagnostics'::text as chargeCategory, d.isconfidential,  "
          + " tod.is_override "
          + " FROM test_org_details tod "
          + " JOIN diagnostics d ON (d.test_id = tod.test_id) "
          + " JOIN organization_details od on(od.org_id=tod.org_id) "
          + " JOIN diagnostics_departments dd ON (d.ddept_id = dd.ddept_id)) AS foo";

  /**
   * Gets the test details.
   *
   * @param parameters the parameters
   * @param listingParameters the listing parameters
   * @return the test details
   */
  public PagedList getTestDetails(
      Map<String, String[]> parameters, Map<LISTING, Object> listingParameters) {

    SearchQueryAssembler qb = null;
    qb =
        new SearchQueryAssembler(
            SEARCH_TEST_FIELDS, SEARCH_TEST_COUNT, SEARCH_TEST_TABLES, listingParameters);
    qb.addFilterFromParamMap(parameters);
    qb.build();
    return qb.getMappedPagedList();
  }

  /**
   * Gets the test details.
   *
   * @param testId the test id
   * @return the test details
   */
  public List<BasicDynaBean> getTestDetails(String testId) {
    return DatabaseHelper.queryToDynaList(TEST_DETAILS, testId);
  }

  /**
   * Gets the test charges for all bed types.
   *
   * @param orgId the org id
   * @param bedTypes the bed types
   * @param testIds the test ids
   * @return the test charges for all bed types
   */
  public List<BasicDynaBean> getTestChargesForAllBedTypes(
      String orgId, List<BasicDynaBean> bedTypes, List<String> testIds) {

    StringBuilder query = new StringBuilder();
    query.append(
        "SELECT d.test_id, tod.org_id as org_id, test_name, applicable AS "
            + DatabaseHelper.quoteIdent("Rate Plan Applicable", true));
    query.append(", item_code AS " + DatabaseHelper.quoteIdent("Itemcode", true));
    for (BasicDynaBean bedType : bedTypes) {
      query.append(
          ", (SELECT charge FROM diagnostic_charges dc WHERE "
              + " dc.test_id=d.test_id AND bed_type='"
              + (String) bedType.get("bed_type")
              + "' AND org_name='"
              + orgId
              + "') AS "
              + DatabaseHelper.quoteIdent((String) bedType.get("bed_type"), true));
      query.append(
          ", (SELECT discount FROM diagnostic_charges dc WHERE "
              + " dc.test_id=d.test_id AND bed_type='"
              + (String) bedType.get("bed_type")
              + "' AND org_name='"
              + orgId
              + "') AS "
              + DatabaseHelper.quoteIdent((String) bedType.get("bed_type") + "(Discount)", true));
    }
    query.append(
        " FROM diagnostics d  "
            + " JOIN test_org_details tod ON (d.test_id=tod.test_id AND tod.org_id='"
            + orgId
            + "' )");
    if (testIds == null) {
      query.append(" WHERE d.status='A' ");
    }

    return DatabaseHelper.queryToDynaListWithCase(query.toString());
  }

  /** The Constant ALL_TEST_NAME. */
  private static final String ALL_TEST_NAME =
      "SELECT test_id,test_name " + " FROM diagnostics ORDER BY test_id ";

  /**
   * Gets the all test names.
   *
   * @return the all test names
   */
  public List<BasicDynaBean> getAllTestNames() {
    return DatabaseHelper.queryToDynaList(ALL_TEST_NAME);
  }

  /** The Constant TEST_NAMES. */
  private static final String TEST_NAMES = "SELECT test_name,ddept_id,test_id from diagnostics";

  /**
   * Gets the test names.
   *
   * @return the test names
   */
  public List<BasicDynaBean> getTestNames() {
    return DatabaseHelper.queryToDynaList(TEST_NAMES);
  }

  /** The Constant TEST_FORMATS. */
  public static final String TEST_FORMATS =
      "SELECT testformat_id, format_name " + " FROM test_format order by format_name";

  /**
   * Gets the report formats.
   *
   * @return the report formats
   */
  public List<BasicDynaBean> getReportFormats() {
    return DatabaseHelper.queryToDynaList(TEST_FORMATS);
  }

  /** The Constant GET_TESTID_TEST_NAME. */
  private static final String GET_TESTID_TEST_NAME =
      " SELECT test_name, test_id " + " FROM diagnostics WHERE test_name = ? AND ddept_id = ? ";

  /**
   * Gets the test id and name.
   *
   * @param testName the test name
   * @param deptId the dept id
   * @return the test id and name
   */
  public List<BasicDynaBean> getTestIdAndName(String testName, String deptId) {
    return DatabaseHelper.queryToDynaList(GET_TESTID_TEST_NAME, testName, deptId);
  }

  /** The Constant INSERT_TEMPLATES. */
  private static final String INSERT_TEMPLATES =
      "INSERT INTO test_template_master" + " (test_id,format_name)" + " values(?,?)";

  /**
   * Insert templates.
   *
   * @param testId the test id
   * @param reportNames the report names
   * @return true, if successful
   */
  public boolean insertTemplates(String testId, String[] reportNames) {

    List<Object[]> queryParamsList = new ArrayList<Object[]>();
    boolean success = true;
    String query = INSERT_TEMPLATES;

    for (int i = 0; i < reportNames.length; i++) {
      List<Object> queryParams = new ArrayList<Object>();
      queryParams.add(testId);
      queryParams.add(reportNames[i]);
      queryParamsList.add(queryParams.toArray());
    }

    int[] results = DatabaseHelper.batchInsert(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The init item org details. */
  private static String INIT_ITEM_ORG_DETAILS =
      "INSERT INTO test_org_details "
          + " (test_id, org_id, applicable, item_code, code_type, base_rate_sheet_id, "
          + " is_override)"
          + " (SELECT ?, od.org_id, false, null, null, prspv.base_rate_sheet_id, 'N' "
          + " FROM organization_details od "
          + " LEFT OUTER JOIN priority_rate_sheet_parameters_view prspv ON od.org_id = "
          + " prspv.org_id )";

  /** The init item charges. */
  private static String INIT_ITEM_CHARGES =
      "INSERT INTO diagnostic_charges"
          + " (test_id,org_name,bed_type,"
          + " charge, username, priority)"
          + " (SELECT ?, od.org_id, 'GENERAL', 0.0, ?, 'R' FROM organization_details od)";

  /**
   * Inits the item charges.
   *
   * @param serviceId the service id
   * @param userName the user name
   * @return true, if successful
   */
  public boolean initItemCharges(String serviceId, String userName) {

    boolean status = false;
    status = initItemCharges(INIT_ITEM_ORG_DETAILS, INIT_ITEM_CHARGES, serviceId, userName);
    return status;
  }

  /**
   * Inits the item charges.
   *
   * @param initExclusionsQuery the init exclusions query
   * @param initChargesQuery the init charges query
   * @param serviceId the service id
   * @param userName the user name
   * @return true, if successful
   */
  private boolean initItemCharges(
      String initExclusionsQuery, String initChargesQuery, String serviceId, String userName) {

    boolean status = false;
    if (null != initExclusionsQuery) {
      if (DatabaseHelper.insert(initExclusionsQuery, serviceId) > 0) {
        status = true;
      }
    }
    if (null != initChargesQuery) {
      if (DatabaseHelper.insert(initChargesQuery, serviceId, userName) > 0) {
        status = true;
      }
    }
    return status;
  }

  /** The Constant TEST_DETAILS. */
  private static final String TEST_DETAILS = "SELECT d.diag_code, d.test_name, d.test_id, "
      + " d.ddept_id, dd.ddept_name, d.type_of_specimen,d.remarks, "
      + " d.status, d.sample_needed, d.conduction_format,d.additional_info_reqts,  "
      + " d.conduction_applicable, d.hl7_export_code, d.test_duration,"
      + " dcr.charge as routine_charge, dcs.charge as stat_charge, "
      + " dcsc.charge as schedule_charge, "
      + " dd.category,d.service_sub_group_id, d.conducting_doc_mandatory,d.prior_auth_required,"
      + " d.sample_collection_instructions,d.conduction_instructions, "
      + " d.results_validation, d.allow_rate_increase, d.allow_rate_decrease, "
      + " d.dependent_test_id, dependent.test_name as dependent_test_name, d.sample_type_id, "
      + " d.results_entry_applicable,d.conducting_role_id,d.mandate_additional_info,"
      + " d.isconfidential, d.allow_zero_claim_amount, d.billing_group_id, "
      + " d.result_validity_period, d.result_validity_period_units, d.mandate_clinical_info,"
      + " d.clinical_justification, d.is_prescribable "
      + " FROM diagnostics d "
      + " LEFT JOIN diagnostics dependent ON (d.dependent_test_id = dependent.test_id) "
      + " JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id) "
      + " LEFT OUTER JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id "
      + "   AND dcr.org_name='ORG0001' "
      + "   AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') "
      + " LEFT OUTER JOIN diagnostic_charges dcs ON (d.test_id = dcs.test_id "
      + "   AND dcs.org_name='ORG0001' "
      + "   AND dcs.bed_type = 'GENERAL' AND dcs.priority = 'S') "
      + " LEFT OUTER JOIN diagnostic_charges dcsc ON (d.test_id = dcsc.test_id "
      + "   AND dcsc.org_name='ORG0001' "
      + "   AND dcsc.bed_type = 'GENERAL' AND dcsc.priority = 'SC') " + " WHERE d.test_id=?";

  /** The Constant GET_CENTER_LIST. */
  private static final String GET_CENTER_LIST = "SELECT city_name, state_name, center_name,"
      + " s.state_id, c.city_id, center_id, hcm.status" + " FROM hospital_center_master hcm"
      + " LEFT JOIN city c ON (c.city_id=hcm.city_id)"
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id) "
      + " WHERE hcm.center_id != 0 ORDER BY center_name";

  /**
   * Gets the centers list.
   *
   * @return the centers list
   */
  public static List<BasicDynaBean> getCentersList() {
    return DatabaseHelper.queryToDynaList(GET_CENTER_LIST);
  }

  /** The Constant DIAG_TEST_TIMESTAMP. */
  private static final String DIAG_TEST_TIMESTAMP =
      "SELECT test_timestamp " + " FROM DIAG_TEST_TIMESTAMP";

  /**
   * Gets the count from diag time stamp.
   *
   * @return the count from diag time stamp
   */
  public Integer getCountFromDiagTimeStamp() {
    return DatabaseHelper.getInteger(DIAG_TEST_TIMESTAMP);
  }

  /** The Constant UPDATE_TIMESTAMP. */
  private static final String UPDATE_TIMESTAMP =
      "UPDATE diag_test_timestamp " + " set test_timestamp=test_timestamp+1;";

  /**
   * Update diagnostic time stamp.
   *
   * @return true, if successful
   */
  public boolean updateDiagnosticTimeStamp() {
    return DatabaseHelper.update(UPDATE_TIMESTAMP) > 0;
  }

  /**
   * Update templates.
   *
   * @param testId the test id
   * @param reportNames the report names
   * @param reportGrp the report grp
   * @return true, if successful
   */
  public boolean updateTemplates(String testId, String[] reportNames, String reportGrp) {

    boolean success = true;
    success = deleteTemplates(testId);
    List<Object[]> queryParamsList = new ArrayList<>();
    String insertTemplateQuery = INSERT_TEMPLATES;

    if (null != reportGrp && reportGrp.equals("T")) {
      for (int i = 0; i < reportNames.length; i++) {
        List<Object> queryParams = new ArrayList<>();
        queryParams.add(testId);
        queryParams.add(reportNames[i]);
        queryParamsList.add(queryParams.toArray());
      }
      int[] results = DatabaseHelper.batchInsert(insertTemplateQuery, queryParamsList);
      for (int result : results) {
        if (result < 0) {
          success = false;
          break;
        }
      }
    }
    return success;
  }

  /** The Constant DELETE_TEMPLATES. */
  private static final String DELETE_TEMPLATES =
      "DELETE FROM test_template_master" + " where test_id = ? ";

  /**
   * Delete templates.
   *
   * @param testId the test id
   * @return true, if successful
   */
  public boolean deleteTemplates(String testId) {
    DatabaseHelper.delete(DELETE_TEMPLATES, testId);
    return true;
  }

  /** The Constant GET_SPECIMEN. */
  private static final String GET_SPECIMEN =
      "SELECT sample_type FROM sample_type" + " WHERE sample_type_id = ? ";

  /**
   * Gets the specimen.
   *
   * @param specimenId the specimen id
   * @return the specimen
   */
  public String getSpecimen(int specimenId) {
    return DatabaseHelper.getString(GET_SPECIMEN, specimenId);
  }

  /** The Constant GET_SPECIMEN_ID. */
  private static final String GET_SPECIMEN_ID =
      "SELECT sample_type_id FROM sample_type" + " WHERE sample_type = ? ";

  /**
   * Gets the specimen id.
   *
   * @param specimen the specimen
   * @return the specimen id
   */
  public String getSpecimenId(String specimen) {
    return DatabaseHelper.getString(GET_SPECIMEN_ID, specimen);
  }

  /** The Constant GET_TEMPLATE_LIST. */
  private static final String GET_TEMPLATE_LIST =
      "SELECT format_name FROM test_template_master" + " WHERE test_id = ?";

  /**
   * Gets the template list.
   *
   * @param testId the test id
   * @return the template list
   */
  public List<BasicDynaBean> getTemplateList(String testId) {
    return DatabaseHelper.queryToDynaList(GET_TEMPLATE_LIST, testId);
  }

  /** The Constant GET_DERIVED_RATE_PALN_DETAILS. */
  private static final String GET_DERIVED_RATE_PALN_DETAILS = "SELECT rp.org_id,od.org_name, "
      + " case when rate_variation_percent<0 then 'Decrease By'"
      + " else 'Increase By' end as discormarkup, "
      + " rate_variation_percent,round_off_amount,tod.applicable,tod.test_id,"
      + " rp.base_rate_sheet_id,tod.is_override " + " FROM rate_plan_parameters rp"
      + " JOIN organization_details od on(od.org_id=rp.org_id) "
      + " JOIN test_org_details tod on (tod.org_id = rp.org_id) "
      + " WHERE rp.base_rate_sheet_id =?  AND test_id=?  AND tod.base_rate_sheet_id=? ";

  /**
   * Gets the derived rate plan details.
   *
   * @param baseRateSheetId the base rate sheet id
   * @param testId the test id
   * @return the derived rate plan details
   */
  public List<BasicDynaBean> getDerivedRatePlanDetails(String baseRateSheetId, String testId) {
    return getDerivedRatePlanDetails(
        baseRateSheetId, "diagnostics", testId, GET_DERIVED_RATE_PALN_DETAILS);
  }

  /**
   * Gets the derived rate plan details.
   *
   * @param baseRateSheetId the base rate sheet id
   * @param category the category
   * @param categoryIdValue the category id value
   * @param query the query
   * @return the derived rate plan details
   */
  protected List<BasicDynaBean> getDerivedRatePlanDetails(
      String baseRateSheetId, String category, String categoryIdValue, String query) {

    if (category.equals("consultation")
        || category.equals("packages")
        || category.equals("dynapackages")) {
      return DatabaseHelper.queryToDynaList(
          query, baseRateSheetId, Integer.parseInt(categoryIdValue), baseRateSheetId);
    } else {
      return DatabaseHelper.queryToDynaList(
          query, baseRateSheetId, categoryIdValue, baseRateSheetId);
    }
  }

  /** The Constant GET_DISCOUNT. */
  private static final String GET_DISCOUNT =
      "SELECT discount FROM  diagnostic_charges WHERE "
          + "test_id=? AND bed_type=? AND org_name=? AND priority='R' ";

  /** The Constant GET_ROUTINE_CHARGE. */
  private static final String GET_ROUTINE_CHARGE =
      "SELECT charge FROM  diagnostic_charges WHERE "
          + "test_id=? AND bed_type=? AND org_name=? AND priority='R' ";

  /** The Constant TESTS_NAMES_AND_IDS. */
  private static final String TESTS_NAMES_AND_IDS = "select test_name,test_id from diagnostics";

  /**
   * Gets the tests names and ids.
   *
   * @return the tests names and ids
   */
  public static List getTestsNamesAndIds() {

    return ConversionUtils
        .copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(TESTS_NAMES_AND_IDS));
  }

  /**
   * Edits the test charges.
   *
   * @param bedTypes the bed types
   * @param orgId the org id
   * @param testid the testid
   * @return the map
   */
  public Map editTestCharges(List<BasicDynaBean> bedTypes, String orgId, String testid) {

    LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>>();
    ArrayList<String> beds = new ArrayList<String>();
    ArrayList<String> regularCharge = new ArrayList<String>();
    ArrayList<String> discount = new ArrayList<String>();

    Iterator<BasicDynaBean> it = bedTypes.iterator();

    while (it.hasNext()) {
      BasicDynaBean bean = it.next();
      String bedType = (String) bean.get("bed_type");
      beds.add(bedType);

      discount.add(DatabaseHelper.getString(GET_DISCOUNT, testid, bedType, orgId));
      regularCharge.add(DatabaseHelper.getString(GET_ROUTINE_CHARGE, testid, bedType, orgId));
    }
    map.put("CHARGES", beds);
    map.put("REGULARCHARGE", regularCharge);
    map.put("DISCOUNT", discount);
    return map;
  }

  /** The Constant GET_SAMPLE_TYPES_LIST. */
  private static final String GET_SAMPLE_TYPES_LIST =
      "SELECT sample_type_id, sample_type " + " FROM sample_type where status = 'A'";

  /**
   * Gets the active sample type list.
   *
   * @return the active sample type list
   */
  public List<BasicDynaBean> getActiveSampleTypeList() {
    return DatabaseHelper.queryToDynaList(GET_SAMPLE_TYPES_LIST);
  }

  /** The Constant INSERT_TEST_CHARGE. */
  private static final String INSERT_TEST_CHARGE =
      "INSERT INTO diagnostic_charges(test_id,"
          + "org_name,charge,bed_type,priority,discount,username)VALUES(?,"
          + "?,?,?,?,?,?)  ";

  /** The Constant CHECK_FOR_TESTCHARGE. */
  private static final String CHECK_FOR_TESTCHARGE =
      "SELECT COUNT(*) "
          + " FROM  diagnostic_charges WHERE "
          + " test_id=? AND bed_type=? AND org_name=? AND priority=? ";

  /** The Constant UPDATE_TEST_CHARGE. */
  private static final String UPDATE_TEST_CHARGE =
      "UPDATE diagnostic_charges SET charge=?,"
          + " discount=?,username=? WHERE "
          + " test_id=? AND bed_type=? AND org_name=? AND priority=? ";

  /**
   * Adds the OR edit test charges.
   *
   * @param tcList the tc list
   * @return true, if successful
   */
  public boolean addOrEditTestCharges(ArrayList<TestCharge> tcList) {
    boolean status = true;

    List<Object[]> insertList = new ArrayList<Object[]>();
    List<Object[]> updateList = new ArrayList<Object[]>();
    Iterator<TestCharge> it = tcList.iterator();

    while (it.hasNext()) {
      TestCharge tc = it.next();
      String count = DatabaseHelper.getString(CHECK_FOR_TESTCHARGE, tc.getTestId(), tc.getBedType(),
          tc.getOrgId(), tc.getPriority());
      if (count.equals("0")) {
        List<Object> queryParams = new ArrayList<Object>();
        queryParams.add(tc.getTestId());
        queryParams.add(tc.getOrgId());
        queryParams.add(tc.getCharge());
        queryParams.add(tc.getBedType());
        queryParams.add(tc.getPriority());
        queryParams.add(tc.getDiscount());
        queryParams.add(tc.getUserName());
        insertList.add(queryParams.toArray());
      } else {
        List<Object> queryParams = new ArrayList<Object>();
        queryParams.add(tc.getCharge());
        queryParams.add(tc.getDiscount());
        queryParams.add(tc.getUserName());
        queryParams.add(tc.getTestId());
        queryParams.add(tc.getBedType());
        queryParams.add(tc.getOrgId());
        queryParams.add(tc.getPriority());
        updateList.add(queryParams.toArray());
      }
    }
    int[] insertResults = DatabaseHelper.batchInsert(INSERT_TEST_CHARGE, insertList);
    for (int result : insertResults) {
      if (result < 0) {
        status = false;
        break;
      }
    }
    int[] updateResults = DatabaseHelper.batchUpdate(UPDATE_TEST_CHARGE, updateList);
    for (int result : updateResults) {
      if (result < 0) {
        status = false;
        break;
      }
    }
    return status;
  }

  /** The Constant INSERT_ITEM_CODE. */
  private static final String INSERT_ITEM_CODE =
      "INSERT INTO test_org_details(test_id,"
          + "org_id,applicable,item_code, code_type)VALUES(?,?,?,?,?)  ";

  /** The Constant CHECK_FOR_ITEM_CODE. */
  private static final String CHECK_FOR_ITEM_CODE =
      "SELECT COUNT(*) FROM  test_org_details WHERE " + "test_id=? AND org_Id=?";

  /** The Constant UPDATE_ITEM_CODE. */
  private static final String UPDATE_ITEM_CODE = "UPDATE test_org_details SET applicable=?,"
      + " item_code=?, code_type=? WHERE " + " test_id=? AND org_id=?";

  /**
   * Adds the OR edit item code.
   *
   * @param testCodes the test codes
   * @return true, if successful
   */
  public boolean addOrEditItemCode(ArrayList<TestCharge> testCodes) {

    boolean status = true;
    ArrayList<Object[]> insertList = new ArrayList<Object[]>();
    ArrayList<Object[]> updateList = new ArrayList<Object[]>();

    for (TestCharge tc : testCodes) {

      String count = DatabaseHelper.getString(CHECK_FOR_ITEM_CODE, tc.getTestId(), tc.getOrgId());
      if (count.equals("0")) {
        List<Object> queryParams = new ArrayList<Object>();
        queryParams.add(tc.getTestId());
        queryParams.add(tc.getOrgId());
        queryParams.add(tc.getApplicable());
        queryParams.add(tc.getOrgItemCode());
        queryParams.add(tc.getCodeType());
        insertList.add(queryParams.toArray());
      } else {
        List<Object> queryParams = new ArrayList<Object>();
        queryParams.add(tc.getApplicable());
        queryParams.add(tc.getOrgItemCode());
        queryParams.add(tc.getCodeType());
        queryParams.add(tc.getTestId());
        queryParams.add(tc.getOrgId());
        updateList.add(queryParams.toArray());
      }
    }
    int[] insertResults = DatabaseHelper.batchInsert(INSERT_ITEM_CODE, insertList);
    for (int result : insertResults) {
      if (result < 0) {
        status = false;
        break;
      }
    }
    int[] updateResults = DatabaseHelper.batchUpdate(UPDATE_ITEM_CODE, updateList);
    for (int result : updateResults) {
      if (result < 0) {
        status = false;
        break;
      }
    }
    return status;
  }

  /** The Constant GET_DERIVED_RATE_PLAN_IDS. */
  private static final String GET_DERIVED_RATE_PLAN_IDS = "select org_id "
      + " from rate_plan_parameters " + " where base_rate_sheet_id =?";

  /**
   * Gets the derived rate plan ids.
   *
   * @param baseRateSheetId the base rate sheet id
   * @return the derived rate plan ids
   */
  public List<BasicDynaBean> getDerivedRatePlanIds(String baseRateSheetId) {
    return DatabaseHelper.queryToDynaList(GET_DERIVED_RATE_PLAN_IDS, baseRateSheetId);
  }

  /** The Constant GET_INSURANCE_CATEGORY_LIST. */
  private static final String GET_INSURANCE_CATEGORY_LIST = "SELECT insurance_category_id,"
      + " insurance_category_name " + " FROM item_insurance_categories"
      + " WHERE system_category = 'N'";

  /**
   * Gets the insurance categories.
   *
   * @return the insurance categories
   */
  public List<BasicDynaBean> getInsuranceCategories() {
    return DatabaseHelper.queryToDynaList(GET_INSURANCE_CATEGORY_LIST);
  }

  /** The Constant SELECT_EXPORT_DATA. */
  private static final String SELECT_EXPORT_DATA = "SELECT  d.test_id,d.test_name,d.sample_needed,"
      + "dd.ddept_name,st.sample_type as type_of_specimen,d.conduction_format,d.status, "
      + "sg.service_group_name, ssg.service_sub_group_name,"
      + "d.conduction_applicable, conducting_doc_mandatory, mandate_additional_info, dcr.charge, "
      + "d.results_entry_applicable,d.diag_code,iic.insurance_category_name ,prior_auth_required,"
      + "allow_rate_increase,allow_rate_decrease,hl7_export_code, isconfidential, d.test_duration,"
      + "d.is_prescribable "
      + "FROM diagnostics d " + "JOIN diagnostics_departments dd USING(ddept_id) "
      + "JOIN service_sub_groups ssg using(service_sub_group_id) "
      + "JOIN service_groups sg using(service_group_id) " + "JOIN item_insurance_categories iic "
      + " ON (iic.insurance_category_id = d.insurance_category_id) "
      + "LEFT JOIN diagnostic_charges dcr ON (d.test_id = dcr.test_id AND dcr.org_name='ORG0001' "
      + "AND dcr.bed_type = 'GENERAL' AND dcr.priority = 'R') "
      + "LEFT JOIN sample_type st ON (st.sample_type_id = d.sample_type_id)  "
      + "WHERE d.status='A' ORDER BY test_id";

  /**
   * Gets list of active insurance category id's for the testId.
   *
   * @param testId Test Id
   * @return active insurance categories
   */
  public List<BasicDynaBean> getActiveInsuranceCategories(String testId) {
    return DatabaseHelper.queryToDynaList(SELECT_INSURANCE_CATEGORY_IDS, testId);
  }

  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM diagnostic_test_insurance_category_mapping "
      + "WHERE diagnostic_test_id =?";

  /*
   * (non-Javadoc)
   *
   * @see
   * com.insta.hms.mdm.bulk.BulkDataRepository#exportData(com.insta.hms.mdm.bulk.CSVBulkDataEntity)
   */
  // TODO : query based csv import and export.
  public Map<String, List<String[]>> exportData(CsVBulkDataEntity csvEntity) {
    return DatabaseHelper.queryWithCustomMapper(SELECT_EXPORT_DATA, new CsvEntityMapper());
  }

  /** The Class CsvEntityMapper. */
  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();

      String[] headers = new String[columnsCount];
      List<String[]> rows = new ArrayList<>();

      HashMap<String, String> headersMap = new HashMap<>();
      headersMap.put("test_id", "test_id");
      headersMap.put("test_name", "Test Name");
      headersMap.put("sample_needed", "Sample Needed");
      headersMap.put("ddept_name", "Dept Name");
      headersMap.put("type_of_specimen", "Type Of Specimen");
      headersMap.put("conduction_format", "Conduct In Report Format");
      headersMap.put("status", "Status");
      headersMap.put("service_group_name", "Service Group Name");
      headersMap.put("service_sub_group_name", "Service Sub Group Name");
      headersMap.put("conduction_applicable", "Conduction Applicable");
      headersMap.put("conducting_doc_mandatory", "Conducting Doctor Mandatory");
      headersMap.put("mandate_additional_info", "mandate additional info");
      headersMap.put("charge", "Unit Charge");
      headersMap.put("results_entry_applicable", "Results Entry Applicable");
      headersMap.put("diag_code", "Alias");
      headersMap.put("insurance_category_name", "Insurance Category");
      headersMap.put("prior_auth_required", "Pre Auth Required");
      headersMap.put("allow_rate_increase", "Allow Rate Increase");
      headersMap.put("allow_rate_decrease", "Allow Rate Decrease");
      headersMap.put("hl7_export_code", "Interface Test Code");
      headersMap.put("isconfidential", "Do not auto-share results");
      headersMap.put("test_duration", "test_duration");
      headersMap.put("is_prescribable", "Prescribable");

      boolean isFirstRow = true;

      while (resultSet.next()) {
        String[] row = new String[columnsCount];
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          Object rowValue = resultSet.getObject(header);

          if (isFirstRow) {
            headers[columnIndex - 1] = headersMap.get(header);
          }
          row[columnIndex - 1] = null != rowValue ? String.valueOf(rowValue) : "";
        }
        rows.add(row);
        isFirstRow = false;
      }
      if (isFirstRow) {
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          headers[columnIndex - 1] = headersMap.get(header);
        }
      }
      List<String[]> headersList = new ArrayList<>();
      headersList.add(headers);
      Map<String, List<String[]>> resultData = new HashMap<>();
      resultData.put("headers", headersList);
      resultData.put("rows", rows);
      return resultData;
    }
  }

  /** The Constant GET_MASTERS_COUNTS. */
  private static final String GET_MASTERS_COUNTS =
      "SELECT * FROM masters_deprtmentwise_counts" + " where type=? and dept_id = ? ";

  /**
   * Gets the masters counts.
   *
   * @param type the type
   * @param depId the dep id
   * @return the masters counts
   */
  public BasicDynaBean getMastersCounts(String type, String depId) {
    return DatabaseHelper.queryToDynaBean(GET_MASTERS_COUNTS, new Object[] {type, depId});
  }

  /** The Constant TEST_DETAILS_UPDATE_QUERY. */
  private static final String TEST_DETAILS_UPDATE_QUERY = " UPDATE diagnostics"
      + " SET ddept_id = ?, status = ?, username = ? WHERE test_id = ?";

  /**
   * Update test details.
   *
   * @param testId the test id
   * @param deptId the dept id
   * @param status the status
   * @return the int
   */
  public int updateTestDetails(String testId, String deptId, String status, String userId) {
    return DatabaseHelper.update(TEST_DETAILS_UPDATE_QUERY, deptId, status, userId, testId);
  }

  /**
   * Update test details.
   *
   * @param bean the bean
   * @param testId the test id
   * @return the int
   */
  public int updateTestDetails(BasicDynaBean bean, String testId) {

    String updateQuery = " UPDATE diagnostics SET test_name = ?,"
        + " test_duration = ?,"
        + " conduction_format = ?, username = ? , ddept_id = ?,"
        + " conducting_role_id = ?, sample_collection_instructions = ?,"
        + " conduction_instructions = ?, results_validation = ?, additional_info_reqts = ?,"
        + " mandate_additional_info = ? , service_sub_group_id = ?,"
        + " conduction_applicable = ?, dependent_test_id = ?,"
        + " status = ?, remarks = ?, hl7_export_code = ?, prior_auth_required = ?,"
        + " allow_rate_increase = ?, allow_rate_decrease = ?, sample_needed = ?,"
        + " diag_code = ?, results_entry_applicable = ?,"
        + " conducting_doc_mandatory = ?, isconfidential = ?, allow_zero_claim_amount = ?,"
        + " billing_group_id = ?, result_validity_period = ?, result_validity_period_units = ?,"
        + " mandate_clinical_info = ?, clinical_justification = ?, is_prescribable= ? ";
    String testName = (String) bean.get("test_name");
    String conductionFormat = (String) bean.get("conduction_format");
    String userName = (String) bean.get("username");
    String deptId = (String) bean.get("ddept_id");
    String condRoleIds = (String) bean.get("conducting_role_id");
    String sampleCollctInstns = (String) bean.get("sample_collection_instructions");
    String conductionInstns = (String) bean.get("conduction_instructions");
    String resultValidation = (String) bean.get("results_validation");
    String additionalInfo = (String) bean.get("additional_info_reqts");
    String mandateAddInfo = (String) bean.get("mandate_additional_info");
    Integer serviceGroupId = (Integer) bean.get("service_sub_group_id");
    boolean conduAppble = (Boolean) bean.get("conduction_applicable");
    String dependantTestId = (String) bean.get("dependent_test_id");
    String status = (String) bean.get("status");
    String remarks = (String) bean.get("remarks");
    String hl7ExportCode = (String) bean.get("hl7_export_code");
    String preAuthReq = (String) bean.get("prior_auth_required");
    boolean allowRateIncr = (Boolean) bean.get("allow_rate_increase");
    boolean allowRateDecr = (Boolean) bean.get("allow_rate_decrease");
    String sampleNeeded = (String) bean.get("sample_needed");
    String diagCode = (String) bean.get("diag_code");
    Integer resultValidityPeriod = (Integer) bean.get("result_validity_period");
    String resultValidityPeriodUnits = (String) bean.get("result_validity_period_units");
    Boolean isPrescribable = (Boolean) bean.get("is_prescribable"); 
    boolean resultEntryApplicable = true;
    if (conduAppble) {
      resultEntryApplicable = (Boolean) bean.get("results_entry_applicable");
    } else {
      resultEntryApplicable = false;
    }
    String condDocManadatory = (String) bean.get("conducting_doc_mandatory");
    boolean isConfidential = (Boolean) bean.get("isconfidential");
    String allowZeroClaimAmount = (String) bean.get("allow_zero_claim_amount");
    Integer billingGroupId = (Integer) bean.get("billing_group_id");
    Integer testDuration = (Integer) bean.get("test_duration");
    String mandateClinicalInfo = (String) bean.get("mandate_clinical_info");
    String clinicalJustification = (String) bean.get("clinical_justification");
    Object[] parameters = new Object[] { testName, testDuration, conductionFormat,
        userName, deptId, condRoleIds,
        sampleCollctInstns, conductionInstns, resultValidation, additionalInfo, mandateAddInfo,
        serviceGroupId, conduAppble, dependantTestId, status, remarks,
        hl7ExportCode, preAuthReq, allowRateIncr, allowRateDecr, sampleNeeded, diagCode,
        resultEntryApplicable, condDocManadatory, isConfidential, allowZeroClaimAmount,
        billingGroupId, resultValidityPeriod, resultValidityPeriodUnits,mandateClinicalInfo,
        clinicalJustification, isPrescribable, testId };

    if (bean.get("sample_type_id") != null) {
      updateQuery = updateQuery + ", sample_type_id = " + (Integer) bean.get("sample_type_id")
          + ", " + "type_of_specimen = '" + (String) bean.get("type_of_specimen") + "'";
    }
    updateQuery = updateQuery + " WHERE test_id = ?";

    return DatabaseHelper.update(updateQuery, parameters);
  }

  /** The Constant GET_TEST_ITEM_SUBGROUP_DETAILS. */
  private static final String GET_TEST_ITEM_SUBGROUP_DETAILS =
      "select disg.item_subgroup_id,"
          + " isg.item_subgroup_name,ig.item_group_id,item_group_name,"
          + " igt.item_group_type_id,igt.item_group_type_name "
          + " from diagnostics_item_sub_groups disg "
          + " left join item_sub_groups isg on (isg.item_subgroup_id = disg.item_subgroup_id) "
          + " left join diagnostics t on (t.test_id = disg.test_id) "
          + " left join item_groups ig on (ig.item_group_id = isg.item_group_id)"
          + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"
          + " where disg.test_id = ? ";

  /**
   * Gets the test item sub group details.
   *
   * @param testId the test id
   * @return the test item sub group details
   */
  public List<BasicDynaBean> getTestItemSubGroupDetails(String testId) {
    return DatabaseHelper.queryToDynaList(GET_TEST_ITEM_SUBGROUP_DETAILS, new Object[] {testId});
  }

  /** The Constant GET_TESTS_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_TESTS_ITEM_SUB_GROUP_TAX_DETAILS =
      "SELECT isg.item_subgroup_id,"
          + " isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
          + " FROM diagnostics_item_sub_groups disg "
          + " JOIN item_sub_groups isg ON(disg.item_subgroup_id = isg.item_subgroup_id) "
          + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
          + " WHERE disg.test_id = ? ";

  /**
   * Gets the diagnostics item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the diagnostics item sub group tax details
   */
  public List<BasicDynaBean> getDiagnosticsItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(
        GET_TESTS_ITEM_SUB_GROUP_TAX_DETAILS, new Object[] {actDescriptionId});
  }

  /** The Constant GET_CATEGORY_ID_BASED_ON_PLAN. */
  private static final String GET_CATEGORY_ID_BASED_ON_PLAN =
      " select dicm.diagnostic_test_id as item_id, COALESCE(pipd.insurance_category_id,0) "
          + " as primary_insurance_category_id,"
          + " COALESCE(sipd.insurance_category_id,0) as secondary_insurance_category_id"
          + " from diagnostic_test_insurance_category_mapping dicm "
          + " Left join insurance_plan_details pipd ON pipd.insurance_category_id="
          + " dicm.insurance_category_id and pipd.plan_id =? "
          + " and pipd.patient_type=?"
          + " Left join insurance_plan_details sipd ON sipd.insurance_category_id= "
          + " dicm.insurance_category_id and sipd.plan_id =? "
          + " and sipd.patient_type=?"
          + " Left join (select piic.insurance_category_id from item_insurance_categories piic "
          + " where piic.priority=1 limit 1) as foo "
          + " ON foo.insurance_category_id = pipd.insurance_category_id "
          + " Left join (select siic.insurance_category_id from item_insurance_categories siic "
          + " where siic.priority=1 limit 1) as foo1 "
          + " ON foo1.insurance_category_id = sipd.insurance_category_id ";

  /**
   * Gets the cat id based on plan ids.
   *
   * @param itemIds the item ids
   * @param planIds the plan ids
   * @param visitType the visit type
   * @return the cat id based on plan ids
   */
  public List<BasicDynaBean> getCatIdBasedOnPlanIds(
      List<String> itemIds, Set<Integer> planIds, String visitType) {
    Object[] planId = planIds.toArray();
    String[] placeHolderArr = new String[itemIds.size()];
    Arrays.fill(placeHolderArr, "?");

    List<Object> args = new ArrayList<Object>();
    args.add((int) planId[0]);
    args.add(visitType);
    if (planId.length > 1) {
      args.add((int) planId[1]);
    } else {
      args.add(-1); // return default
    }
    args.add(visitType);
    args.addAll(itemIds);
    String placeHolders = StringUtils.arrayToCommaDelimitedString(placeHolderArr);
    String query =
        GET_CATEGORY_ID_BASED_ON_PLAN
            + " where dicm.diagnostic_test_id in ( "
            + placeHolders
            + ");"; 
    
    return DatabaseHelper.queryToDynaList(query, args.toArray());
  }
}
