package com.insta.hms.mdm.diagnosticcharges;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.bulk.BulkDataRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagnosticChargeRepository.
 *
 * @author anil.n
 */
@Repository
public class DiagnosticChargeRepository extends BulkDataRepository<String> {

  /** The bed type service. */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /**
   * Instantiates a new diagnostic charge repository.
   */
  public DiagnosticChargeRepository() {
    super("diagnostic_charges", TEST_ID);
  }

  /** The Constant GET_RATE_PLAN_PARAMETERS. */
  private static final String GET_RATE_PLAN_PARAMETERS = "SELECT * FROM rate_plan_parameters "
      + " WHERE base_rate_sheet_id = ? AND org_id = ? ";

  /** The Constant APPLICABLE. */
  private static final String APPLICABLE = "applicable";
  
  /** The Constant BED_TYPE. */
  private static final String BED_TYPE = "bed_type";
  
  /** The Constant DIAGNOSTICS. */
  private static final String DIAGNOSTICS = "diagnostics";
  
  /** The Constant ORG_NAME. */
  private static final String ORG_NAME = "org_name";
  
  /** The Constant TEST_ID. */
  private static final String TEST_ID = "test_id";
  
  /**
   * Update charges for derived rate plans.
   *
   * @param baseRateSheetId the base rate sheet id
   * @param ratePlanIds the rate plan ids
   * @param bedType the bed type
   * @param regularcharges the regularcharges
   * @param repository the repository
   * @param orgTbl the org tbl
   * @param category the category
   * @param categoryId the category id
   * @param categoryIdValue the category id value
   * @param discounts the discounts
   * @param applicable the applicable
   * @return true, if successful
   */
  public boolean updateChargesForDerivedRatePlans(String baseRateSheetId, String[] ratePlanIds,
      String[] bedType, Double[] regularcharges, MasterRepository repository,
      MasterRepository orgTbl, String category, String categoryId, String categoryIdValue,
      Double[] discounts, String[] applicable) {

    boolean success = false;
    for (int i = 0; i < ratePlanIds.length; i++) {
      BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_RATE_PLAN_PARAMETERS,
          baseRateSheetId, ratePlanIds[i]);
      int variation = (Integer) bean.get("rate_variation_percent");
      int roundoff = (Integer) bean.get("round_off_amount");
      List<BasicDynaBean> chargeList = new ArrayList();
      boolean overrided = isChargeOverrided(ratePlanIds[i], categoryId, categoryIdValue, category,
          orgTbl);

      if (!overrided) {
        for (int k = 0; k < bedType.length; k++) {
          BasicDynaBean charge = repository.getBean();
          if (category.equals("consultation") || category.equals("packages")) {
            charge.set(categoryId, Integer.parseInt(categoryIdValue));
          } else {
            charge.set(categoryId, categoryIdValue);
          }  
          if (category.equals(DIAGNOSTICS)) {
            charge.set(ORG_NAME, ratePlanIds[i]);
          } else {
            charge.set("org_id", ratePlanIds[i]);
          }
          charge.set(BED_TYPE, bedType[k]);
          Double rpCharge = calculateCharge(regularcharges[k], Double.valueOf(variation), roundoff);
          Double rpDiscount = calculateCharge(discounts[k], Double.valueOf(variation), roundoff);
          if (category.equals("services")) {
            charge.set("unit_charge", new BigDecimal(rpCharge));
          } else {
            charge.set("charge", new BigDecimal(rpCharge));
          }
          charge.set("discount", new BigDecimal(rpDiscount));
          chargeList.add(charge);
        }
      }

      for (BasicDynaBean c : chargeList) {
        if (category.equals(DIAGNOSTICS)) {
          HashMap map = new HashMap();
          map.put(ORG_NAME, c.get(ORG_NAME));
          map.put(BED_TYPE, c.get(BED_TYPE));
          map.put(TEST_ID, c.get(TEST_ID));
          update(c, map);
        }
      }
      success = true;

      boolean overrideItemCharges = checkItemStatus(ratePlanIds[i], orgTbl, category, categoryId,
          categoryIdValue, applicable[i]);
      if (overrideItemCharges) {
        overrideItemCharges(repository, category, ratePlanIds[i], categoryId, categoryIdValue);
      }  
    }
    return success;
  }

  /** The Constant GET_ORG_DETAILS. */
  private static final String GET_ORG_DETAILS = "SELECT * FROM test_org_details "
      + " WHERE org_id = ? AND test_id = ? ";

  /**
   * Checks if is charge overrided.
   *
   * @param ratePlanId the rate plan id
   * @param categoryId the category id
   * @param categoryIdValue the category id value
   * @param category the category
   * @param orgTbl the org tbl
   * @return true, if is charge overrided
   */
  public boolean isChargeOverrided(String ratePlanId, String categoryId, String categoryIdValue,
      String category, MasterRepository orgTbl) {

    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_ORG_DETAILS, ratePlanId,
        categoryIdValue);
    String override = (String) bean.get("is_override");

    return override.equals("Y");
  
  }

  /**
   * Calculate charge.
   *
   * @param rsCharge the rs charge
   * @param variance the variance
   * @param roundOff the round off
   * @return the double
   */
  private Double calculateCharge(Double rsCharge, Double variance, int roundOff) {
    Double charge = rsCharge + (rsCharge * variance) / 100;
    if (roundOff != 0) {
      Double ch = Double.valueOf(roundOff) / 2;
      ch = charge + ch;
      ch = ch / roundOff;
      int intVal = ch.intValue();
      charge = roundOff * Double.valueOf(intVal);
    }
    return charge;
  }

  /** The Constant CHECK_TEST_EXIST. */
  private static final String CHECK_TEST_EXIST = "SELECT * FROM test_org_details "
      + " WHERE org_id = ? AND test_id = ?";

  /**
   * Check item status.
   *
   * @param ratePlanId the rate plan id
   * @param repository the repository
   * @param chargeCategory the charge category
   * @param categoryId the category id
   * @param categoryValue the category value
   * @param applicable the applicable
   * @return true, if successful
   */
  public boolean checkItemStatus(String ratePlanId, MasterRepository repository,
      String chargeCategory, String categoryId, String categoryValue, String applicable) {

    boolean success = false;
    BasicDynaBean bean = DatabaseHelper
        .queryToDynaBean(CHECK_TEST_EXIST, ratePlanId, categoryValue);
    String baseRateSheetId = (String) bean.get("base_rate_sheet_id");
    bean = DatabaseHelper.queryToDynaBean(CHECK_TEST_EXIST, baseRateSheetId, categoryValue);
    return bean.get(APPLICABLE).equals(true) != applicable.equals("true");
  }

  /**
   * Override item charges.
   *
   * @param chargeTblName the charge tbl name
   * @param chargeCategory the charge category
   * @param orgId the org id
   * @param categoryIdName the category id name
   * @param categoryIdValue the category id value
   * @return true, if successful
   */
  public boolean overrideItemCharges(MasterRepository chargeTblName, String chargeCategory,
      String orgId, String categoryIdName, String categoryIdValue) {

    BasicDynaBean chargeBean = getBean();
    chargeBean.set("is_override", "Y");
    Map<String, Object> chKeys = new HashMap<String, Object>();
    if (chargeCategory.equals(DIAGNOSTICS)) {
      chKeys.put(ORG_NAME, orgId);
    } else {
      chKeys.put("org_id", orgId);
    }
    if (chargeCategory.equals("operations")) {
      chKeys.put("op_id", categoryIdValue);
    } else {
      if (chargeCategory.equals("consultation") || chargeCategory.equals("dynapackages")
          || chargeCategory.equals("packages")) {
        chKeys.put(categoryIdName, Integer.parseInt(categoryIdValue));
      } else {
        chKeys.put(categoryIdName, categoryIdValue);
      }  
    }
    
    boolean success = false;
    success = update(chargeBean, chKeys) > 0;
    return success;
  }

  /**
   * Export CSV data.
   *
   * @param orgId the org id
   * @return the map
   */
  public Map<String, List<String[]>> exportCsvData(String orgId) {

    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    StringBuilder query = new StringBuilder();
    int bed = 0;

    /* Construting the query */
    query
        .append("SELECT itemTable.test_name, itemTable.status, deptTable.ddept_name, "
            + "orgTable.applicable,orgTable.item_code, ");

    String[] chargeColumns = { "charge", "discount" };
    for (BasicDynaBean bean : bedTypes) {
      int charge = 0;
      ++bed;
      String bedType = (String) bean.get(BED_TYPE);
      for (String chargeColumn : chargeColumns) {
        ++charge;
        query
            .append("(SELECT "
                + chargeColumn
                + " FROM diagnostic_charges WHERE "
                + "test_id = itemTable.test_id AND bed_type = '"
                + bedType
                + "' AND org_name = '"
                + orgId
                + "') AS "
                + DatabaseHelper.quoteIdent(bean.get(BED_TYPE).toString() + " " + chargeColumn,
                    true));
        if (bed < bedTypes.size() || charge < chargeColumns.length) {
          query.append(",");
        }
      }

    }
    query
        .append(" FROM diagnostics itemTable "
            + " JOIN test_org_details orgTable "
            + " ON (itemTable.test_id = orgTable.test_id AND org_id = '"
            + orgId
            + "') "
            + "JOIN diagnostics_departments deptTable ON (deptTable.ddept_id = itemTable.ddept_id)"
            + " WHERE itemTable.status = 'A' ORDER BY itemTable.test_id");

    return DatabaseHelper.queryWithCustomMapper(query.toString(), new CsvEntityMapper());

  }

  /**
   * The Class CsvEntityMapper.
   */
  final class CsvEntityMapper implements ResultSetExtractor<Map<String, List<String[]>>> {
    
    /* (non-Javadoc)
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
     */
    @Override
    public Map<String, List<String[]>> extractData(ResultSet resultSet) throws SQLException {
      ResultSetMetaData meta = resultSet.getMetaData();
      Integer columnsCount = meta.getColumnCount();

      String[] headers = new String[columnsCount];
      List<String[]> headersList = new ArrayList<>();
      List<String[]> rows = new ArrayList<>();

      boolean isFirstRow = true;

      while (resultSet.next()) {
        String[] row = new String[columnsCount];
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          Object rowValue = resultSet.getObject(header);

          if (isFirstRow) {
            if (header.equals("test_name")) {
              headers[columnIndex - 1] = "Test Name";
            } else if (header.equals("status")) {
              headers[columnIndex - 1] = "Status";
            } else if (header.equals("ddept_name")) {
              headers[columnIndex - 1] = "Department";
            } else if (header.equals(APPLICABLE)) {
              headers[columnIndex - 1] = "Applicable";
            } else if (header.equals("item_code")) {
              headers[columnIndex - 1] = "Code";
            } else {
              headers[columnIndex - 1] = header;
            }
          }
          row[columnIndex - 1] = null != rowValue ? String.valueOf(rowValue) : "";
        }
        rows.add(row);
        isFirstRow = false;
      }
      if (isFirstRow) {
        for (Integer columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
          String header = meta.getColumnName(columnIndex);
          if (header.equals("test_name")) {
            headers[columnIndex - 1] = "Test Name";
          } else if (header.equals("status")) {
            headers[columnIndex - 1] = "Status";
          } else if (header.equals("ddept_name")) {
            headers[columnIndex - 1] = "Department";
          } else if (header.equals(APPLICABLE)) {
            headers[columnIndex - 1] = "Applicable";
          } else if (header.equals("item_code")) {
            headers[columnIndex - 1] = "Code";
          } else {
            headers[columnIndex - 1] = header;
          }
        }
      }
      headersList.add(headers);
      Map<String, List<String[]>> resultData = new HashMap<>();
      resultData.put("headers", headersList);
      resultData.put("rows", rows);
      return resultData;
    }
  }

  /** The Constant UPDATE_TEST_CHARGE. */
  private static final String UPDATE_TEST_CHARGE = " UPDATE diagnostic_charges SET charge = ?, "
      + "discount = ?, username = ? WHERE test_id = ? AND org_name = ? AND bed_type = ?";

  /**
   * Update test charge and discount.
   *
   * @param testId the test id
   * @param orgId the org id
   * @param bedType the bed type
   * @param charge the charge
   * @param discount the discount
   * @return the int
   */
  public int updateTestChargeAndDiscount(String testId, String orgId, String bedType,
      BigDecimal charge, BigDecimal discount, String userId) {
    return DatabaseHelper.update(UPDATE_TEST_CHARGE, charge, discount, userId, testId, 
               orgId, bedType);
  }

  /** The Constant BACKUP_CHARGES. */
  private static final String BACKUP_CHARGES = " INSERT INTO diagnostic_charges_backup "
      + "(user_name, bkp_time, org_name, bed_type, test_id, charge, discount) "
      + " SELECT ?, current_timestamp, org_name, bed_type, test_id, charge, discount "
      + " FROM diagnostic_charges WHERE org_name=?";

  /**
   * Back up charge data.
   *
   * @param orgId the org id
   * @param userId the user id
   */
  public void backUpChargeData(String orgId, String userId) {
    DatabaseHelper.insert(BACKUP_CHARGES, userId, orgId);
  }

  /** The Constant FIND_ALL_BY_RATE_SHEETS. */
  private static final String FIND_ALL_BY_RATE_SHEETS = " SELECT * FROM rate_plan_parameters "
      + " WHERE base_rate_sheet_id = ?";

  /**
   * Find all by using rate sheet.
   *
   * @param baseRateSheetId the base rate sheet id
   * @return the list
   */
  public List<BasicDynaBean> findAllByUsingRateSheet(String baseRateSheetId) {
    return DatabaseHelper.queryToDynaList(FIND_ALL_BY_RATE_SHEETS, baseRateSheetId);
  }

  /**
   * Update diag charges.
   *
   * @param query the query
   * @param amount the amount
   * @param userNameWithHint the user name with hint
   * @param orgId the org id
   * @return true, if successful
   */
  public boolean updateDiagCharges(String query, BigDecimal amount, String userNameWithHint,
      String orgId) {
    return DatabaseHelper.update(query, new Object[] { amount, userNameWithHint, orgId }) > 0;
  }

  /**
   * Update diag charges.
   *
   * @param query the query
   * @param amount the amount
   * @param roundTo the round to
   * @param userNameWithHint the user name with hint
   * @param orgId the org id
   * @return true, if successful
   */
  public boolean updateDiagCharges(String query, BigDecimal amount, BigDecimal roundTo,
      String userNameWithHint, String orgId) {
    return DatabaseHelper.update(query, new Object[] { amount, roundTo, roundTo, userNameWithHint,
        orgId }) > 0;
  }
}
