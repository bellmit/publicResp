package com.insta.hms.mdm.diagnosticcharges;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import au.com.bytecode.opencsv.CSVReader;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.jobs.CronJobService;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.testorganization.TestOrganizationRepository;
import com.insta.hms.mdm.testorganization.TestOrganizationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagnosticChargeService.
 *
 * @author anil.n
 */
@Service
public class DiagnosticChargeService extends BulkDataService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DiagnosticChargeService.class);

  /** The message util. */
  @LazyAutowired
  MessageUtil msgUtil;

  /** The diagnostic charge repository. */
  @LazyAutowired
  private DiagnosticChargeRepository diagnosticChargeRepository;

  /** The test organization repository. */
  @LazyAutowired
  private TestOrganizationRepository testOrganizationRepository;

  /** The diagnostic test service. */
  @LazyAutowired
  private DiagnosticTestService diagnosticTestService;

  /** The diag department service. */
  @LazyAutowired
  private DiagDepartmentService diagDepartmentService;

  /** The bed type service. */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService organizationService;

  /** The test organization service. */
  @LazyAutowired
  private TestOrganizationService testOrganizationService;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The cron service. */
  @LazyAutowired
  private CronJobService cronJobService;


  /**
   * Instantiates a new diagnostic charge service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   * @param entity
   *          the entity
   */
  public DiagnosticChargeService(DiagnosticChargeRepository repo, 
      DiagnosticChargeValidator validator,
      DiagnosticChargeCsvBulkDataEntity entity) {
    super(repo, validator, entity);
  }

  /**
   * Update charges for derived rate plans.
   *
   * @param baseRateSheetId
   *          the base rate sheet id
   * @param ratePlanIds
   *          the rate plan ids
   * @param bedType
   *          the bed type
   * @param regularcharges
   *          the regularcharges
   * @param testId
   *          the test id
   * @param discounts
   *          the discounts
   * @param applicable
   *          the applicable
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateChargesForDerivedRatePlans(String baseRateSheetId, String[] ratePlanIds,
      String[] bedType, Double[] regularcharges, String testId, Double[] discounts,
      String[] applicable) {

    return diagnosticChargeRepository.updateChargesForDerivedRatePlans(baseRateSheetId,
        ratePlanIds, bedType, regularcharges, diagnosticChargeRepository,
        testOrganizationRepository, "diagnostics", "test_id", testId, discounts, applicable);
  }

  /**
   * Update charges for derived rate plans.
   *
   * @param orgId
   *          the org id
   * @param userName
   *          the user name
   * @param category
   *          the category
   * @param upload
   *          the upload
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateChargesForDerivedRatePlans(String orgId, String userName, String category,
      boolean upload) {

    List<BasicDynaBean> derivedRatePlanList = diagnosticChargeRepository
        .findAllByUsingRateSheet(orgId);
    boolean success = false;

    for (int i = 0; i < derivedRatePlanList.size(); i++) {

      BasicDynaBean bean = derivedRatePlanList.get(i);
      String ratePlanId = (String) bean.get("org_id");
      Double variation = Double.valueOf((Integer) bean.get("rate_variation_percent"));
      Double roundoff = Double.valueOf((Integer) bean.get("round_off_amount"));
      String varianceType = variation > 0.00 ? "Incr" : "Decr";

      BasicDynaBean orgDetBean = organizationService.findByUniqueName(ratePlanId, "org_id");
      String orgName = (String) orgDetBean.get("org_name");

      success = updateChargesForDerivedRatePlans(ratePlanId, varianceType, 0.00, variation, orgId,
          roundoff, userName, orgName, category, upload);
    }
    return success;
  }

  /**
   * Update charges for derived rate plans.
   *
   * @param orgId
   *          the org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @param category
   *          the category
   * @param upload
   *          the upload
   * @return true, if successful
   */
  public boolean updateChargesForDerivedRatePlans(String orgId, String varianceType,
      Double varianceValue, Double varianceBy, String baseOrgId, Double nearstRoundOfValue,
      String userName, String orgName, String category, boolean upload) {

    boolean success = true;
    if (category.equals("tests")) {
      success = updateTestChargesForDerivedRatePlans(orgId, varianceType, varianceValue,
          varianceBy, baseOrgId, nearstRoundOfValue, userName, orgName, upload);

    }
    return success;
  }


  /* 
   * @see com.insta.hms.mdm.bulk.BulkDataService#getMasterData()
   */
  public Map<String, List<BasicDynaBean>> getMasterData() {
    return null;
  }

  /**
   * Insert test charges.
   *
   * @param testId
   *          the test id
   * @param orgName
   *          the org name
   * @param bed
   *          the bed
   * @param priority
   *          the priority
   * @param charge
   *          the charge
   * @param userName
   *          the user name
   * @return the int
   */
  public int insertTestCharges(String testId, String orgName, String bed, String priority,
      BigDecimal charge, String userName) {
    return DatabaseHelper
        .insert(
            "INSERT INTO diagnostic_charges"
            + " (test_id, org_name, charge, bed_type, priority, username)"
            + " VALUES (?, ?, ?, ?, ?, ?)",
            testId, orgName, charge, bed, priority, userName);
  }

  /**
   * Exports data.
   *
   * @param orgId
   *          the org id
   * @return the list
   */
  public Map<String, List<String[]>> exportData(String orgId) {
    return diagnosticChargeRepository.exportCsvData(orgId);
  }

  /**
   * Import data.
   *
   * @param file
   *          the file
   * @param feedback
   *          the feedback
   * @param orgId
   *          the org id
   * @return the string
   */
  public String importCsvData(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback, String orgId) {
    return processAndImportCsv(file, feedback, orgId);
  }

  /**
   * Parses the and import CSV.
   *
   * @param file
   *          the file
   * @param feedback
   *          the feedback
   * @param orgId
   *          the org id
   * @return the string
   */
  public String processAndImportCsv(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback, String orgId) {

    Map<String, String> testIdsMap = diagnosticTestService.getTestNamesAndIds();
    Map<String, String> deptsMap = diagDepartmentService.getDiagDepartmentsMap();

    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<Object, Object>();
    LinkedHashMap<String, String> headersMap = new LinkedHashMap<String, String>();
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    
    headersMap.put("Test Name", "test_name");
    headersMap.put("Status", "status");
    headersMap.put("Department", "ddept_name");
    headersMap.put("Applicable", "applicable");
    headersMap.put("Code", "item_code");
    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    for (BasicDynaBean bedBean : bedTypes) {
      String[] chargeColumns = new String[] { "charge", "discount" };
      for (String column : chargeColumns) {
        headersMap.put((String) bedBean.get("bed_type") + " " + column,
            (String) bedBean.get("bed_type") + " " + column);
      }
    }

    List<String> mandatoryList = Arrays.asList("test_name");

    try {
      InputStreamReader streamReader = new InputStreamReader(file.getInputStream());
      CSVReader csvReader = new CSVReader(streamReader);
      String[] headers = csvReader.readNext();

      if (headers.length < 1) {
        return "exception.csv.missing.headers";
      }
      if (!headers[0].matches("\\p{Print}*")) {
        return "exception.csv.non.printable.characters";
      }
      if (headers.length == 1) {
        return "exception.csv.non.comma.seperators";
      }
      boolean[] ignoreColumn = new boolean[headers.length];
      Integer lineNumber = 0;
      Integer lineWarningsCount = 0;
      Integer updationCount = 0;
      String testId = null;
      CsVBulkDataEntity csvEntity = getCsVDataEntity();

      for (Integer index = 0; index < headers.length; index++) {
        String fieldName = headersMap.get(headers[index].trim());
        if (fieldName == null) {
          addWarningMessage(warnings, lineNumber, "exception.csv.unknown.header", headers[index]);
          ignoreColumn[index] = true;
        } else {
          ignoreColumn[index] = false;
        }

        headers[index] = fieldName;
      }
      lineNumber++;
      for (String mfield : mandatoryList) {
        if (!Arrays.asList(headers).contains(mfield)) {
          addWarningMessage(warnings, lineNumber, "Mandatory field " + mfield
              + " is missing cannot process further in the sheet ", mfield);
        }
      }

      String[] row = null;
      while (null != (row = csvReader.readNext())) {
        Integer nonEmptyColumnsCount = 0;
        boolean hasWarnings = false;
        BasicDynaBean bean = getRepository().getBean();
        lineNumber++;
        String status = null;
        boolean applicable = true;
        String itemCode = null;
        String deptId = null;
        BigDecimal charge = BigDecimal.valueOf((0.0));
        HashMap<String, Map<String, BigDecimal>> chargesMap = new HashMap<>();
        HashMap<String, BigDecimal> chargeOrDiscountMap = new HashMap<>();

        for (Integer columnIndex = 0; columnIndex < headers.length 
            && columnIndex < row.length; columnIndex++) {
          if (ignoreColumn[columnIndex]) {
            continue;
          }

          String fieldName = headers[columnIndex];
          String fieldValue = row[columnIndex].trim();
          DynaProperty property;

          if ((null != fieldValue) && !(fieldValue.isEmpty())) {
            if (fieldName.equals("test_name")) {
              testId = testIdsMap.get(fieldValue.trim());
              if (null == testId) {
                addWarningMessage(warnings, lineNumber, 
                    "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
            }
            if (fieldName.equals("status")) {
              if (fieldValue != null && !fieldValue.equals("")) {
                status = fieldValue;
              }
            }
            if (fieldName.equals("ddept_name")) {
              deptId = deptsMap.get(fieldValue.trim());
              if (deptId == null || deptId.equals("")) {
                addWarningMessage(warnings, lineNumber, 
                    "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
            }
            if (fieldName.equals("applicable")) {
              if (fieldValue != null && !fieldValue.equals("")) {
                applicable = new Boolean(fieldValue);
              }
            }
            if (fieldName.equals("item_code")) {
              if (fieldValue != null && !fieldValue.equals("")) {
                itemCode = fieldValue;
              }
            }
          }
          if (fieldName.trim().endsWith("charge") || fieldName.trim().endsWith("discount")) {
            String bedType = null;
            if (fieldName.trim().endsWith("charge")) {
              if (fieldValue != null && !fieldValue.equals("")) {
                charge = new BigDecimal(fieldValue);
              }
            } else {
              bedType = fieldName.substring(0, fieldName.length() - 9).trim();
              chargeOrDiscountMap.put("charge", charge);
              if (fieldValue != null && !fieldValue.equals("")) {
                chargeOrDiscountMap.put("discount", new BigDecimal(fieldValue));
              } else {
                chargeOrDiscountMap.put("discount", BigDecimal.valueOf(0.0));
              }
              chargesMap.put(bedType, chargeOrDiscountMap);
              chargeOrDiscountMap = new HashMap<>();
              charge = BigDecimal.valueOf((0.0));
            }
          }
          nonEmptyColumnsCount++;
        }

        if (hasWarnings || nonEmptyColumnsCount == 0) {
          continue;
        }

        try {
          boolean success = true;
          if (testId != null && !testId.equals("") && deptId != null && !deptId.equals("")) {
            success = diagnosticTestService.updateTestDetailsForCharge(testId, deptId, 
                         status, userId);
          }
          if (testId != null && !testId.equals("")) {
            success &= testOrganizationService.updateApplicableFlag(testId, orgId, applicable,
                itemCode) > 0;
          }
          if (testId != null && !testId.equals("")) {
            success &= updateTestChargesForBedTypes(testId, orgId, chargesMap, userId);
          }
          if (success) {
            updationCount++;
          }
        } catch (DuplicateEntityException ex) {
          addWarningMessage(warnings, lineNumber, "exception.csv.duplicate.record");
          logger.error("Duplicate record found : " + bean.get("test_name"));
          lineWarningsCount++;
        } catch (DataAccessException ex) {
          addWarningMessage(warnings, lineNumber, 
              "exception.csv.unknown.error", ex.getMostSpecificCause()
              .getMessage());
          logger.error("Error uploading csv line", ex.getCause());
          lineWarningsCount++;
        } catch (ValidationException ex) {
          for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) (Object) ex
              .getErrors()).entrySet()) {
            warnings.add(lineNumber, entry.getValue().get(0));
          }

          logger.error(ex.getMessage());
        }
      }
      feedback.put("warnings", warnings);
      Integer insertionCount = 0;
      meta.add("processed_count", lineNumber - 1);
      meta.add("insertion_count", insertionCount);
      meta.add("updation_count", updationCount);
      feedback.put("result", meta);

    } catch (IOException ex) {
      throw new InvalidFileFormatException(ex);
    }
    return null;
  }

  /**
   * Adds the warning.
   *
   * @param warnings
   *          the warnings
   * @param lineNumber
   *          the line number
   * @param message
   *          the message
   * @param parameters
   *          the parameters
   */
  private void addWarningMessage(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message, Object... parameters) {
    StringBuilder warning = new StringBuilder();
    warning.append(msgUtil.getMessage(message, parameters));
    warnings.add(lineNumber, warning.toString());
  }

  /**
   * Update test charges for bed types.
   *
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @param chargesMap
   *          the charges map
   * @return true, if successful
   */
  public boolean updateTestChargesForBedTypes(String testId, String orgId,
      Map<String, Map<String, BigDecimal>> chargesMap, String userId) {
    boolean flag = true;
    for (Map.Entry<String, Map<String, BigDecimal>> map : chargesMap.entrySet()) {
      String bedType = map.getKey();
      Map<String, BigDecimal> chargeDiscountMap = map.getValue();
      BigDecimal charge = (BigDecimal) chargeDiscountMap.get("charge");
      BigDecimal discount = (BigDecimal) chargeDiscountMap.get("discount");
      if (diagnosticChargeRepository.updateTestChargeAndDiscount(testId, orgId, bedType, charge,
          discount, userId) <= 0) {
        flag = true;
      }
    }
    return flag;
  }

  /**
   * Back upcharges.
   *
   * @param orgId
   *          the org id
   * @param userId
   *          the user id
   */
  public void backUpcharges(String orgId, String userId) {
    diagnosticChargeRepository.backUpChargeData(orgId, userId);
  }

  /**
   * Group increase or decrease test charges.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundTo
   *          the round to
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean groupIncreaseOrDecreaseTestCharges(String orgId, List<String> bedTypes,
      List<String> testIds, BigDecimal amount, boolean isPercentage, BigDecimal roundTo,
      String updateTable, String userName) {

    boolean success = true;
    success = groupIncreaseTestCharges(orgId, bedTypes, testIds, amount, isPercentage, roundTo,
        updateTable, userName);
    if (success) {
      success &= diagnosticTestService.updateDiagnosticTimeStamp();
    }
    return success;
  }

  /**
   * Group increase test charges.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundTo
   *          the round to
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @return true, if successful
   */
  public boolean groupIncreaseTestCharges(String orgId, List<String> bedTypes,
      List<String> testIds, BigDecimal amount, boolean isPercentage, BigDecimal roundTo,
      String updateTable, String userName) {

    if (roundTo.compareTo(BigDecimal.ZERO) == 0) {
      return groupIncreaseChargesNoRoundOff(orgId, bedTypes, testIds, amount, isPercentage,
          updateTable, userName);
    } else {
      return groupIncreaseChargesWithRoundOff(orgId, bedTypes, testIds, amount, isPercentage,
          roundTo, updateTable, userName);
    }
  }

  /** The Constant GROUP_INCR_TEST_CHARGES_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_CHARGES_NO_ROUNDOFF = " UPDATE diagnostic_charges "
      + " SET charge = GREATEST( charge + ?, 0), username = ?" + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_CHARGES_PERCENTAGE_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_CHARGES_PERCENTAGE_NO_ROUNDOFF = 
      " UPDATE diagnostic_charges "
      + " SET charge = GREATEST(charge +(charge * ? / 100 ) , 0), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNTS_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_DISCOUNTS_NO_ROUNDOFF = 
      " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST( discount + ?, 0), charge), username = ? "
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNT_PERCENTAGE_NO_ROUNDOFF. */
  private static final String GROUP_INCR_TEST_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = 
      " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST(discount +(discount * ? / 100 ) , 0), charge),"
      + " username = ? "
      + " WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF. */
  private static final String GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF = " UPDATE diagnostic_charges"
      + " SET discount = LEAST(GREATEST( charge + ?, 0), charge), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF. */
  private static final String GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF = 
      " UPDATE diagnostic_charges"
      + " SET discount = LEAST(GREATEST( charge + (charge * ? / 100) , 0), charge), username = ?"
      + " WHERE org_name=? ";

  /** The Constant AUDIT_LOG_HINT. */
  private static final String AUDIT_LOG_HINT = ":GUP";

  /**
   * Group increase charges no round off.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @return true, if successful
   */
  public boolean groupIncreaseChargesNoRoundOff(String orgId, List<String> bedTypes,
      List<String> testIds, BigDecimal amount, boolean isPercentage, String updateTable,
      String userName) {

    StringBuilder query = null;
    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage ? GROUP_INCR_TEST_CHARGES_PERCENTAGE_NO_ROUNDOFF
          : GROUP_INCR_TEST_CHARGES_NO_ROUNDOFF);

    } else if (updateTable.equals("UPDATEDISCOUNT")) {
      query = new StringBuilder(isPercentage ? GROUP_INCR_TEST_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
          : GROUP_INCR_TEST_DISCOUNTS_NO_ROUNDOFF);
    } else {
      query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE_NO_ROUNDOFF
          : GROUP_APPLY_DISCOUNTS_NO_ROUNDOFF);
    }

    if (bedTypes != null && bedTypes.size() > 0) {
      query.append(" AND bed_type IN (");
      for (int i = 0; i < bedTypes.size(); i++) {
        if (i != bedTypes.size() - 1) {
          query.append("\'" + bedTypes.get(i) + "\',");
        } else {
          query.append("\'" + bedTypes.get(i) + "\')");
        }
      }
    }
    if (testIds != null && testIds.size() > 0) {
      query.append(" AND test_id IN (");
      for (int i = 0; i < testIds.size(); i++) {
        if (i != testIds.size() - 1) {
          query.append("\'" + testIds.get(i) + "\',");
        } else {
          query.append("\'" + testIds.get(i) + "\')");
        }
      }
    }
    String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
    return diagnosticChargeRepository.updateDiagCharges(query.toString(), amount, userNameWithHint,
        orgId);
  }

  /** The Constant GROUP_INCR_TEST_CHARGES. */
  private static final String GROUP_INCR_TEST_CHARGES = " UPDATE diagnostic_charges "
      + " SET charge = GREATEST( round((charge+?)/?,0)*?, 0), username = ?" 
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_CHARGES_PERCENTAGE. */
  private static final String GROUP_INCR_TEST_CHARGES_PERCENTAGE = " UPDATE diagnostic_charges "
      + " SET charge = GREATEST( round(charge*(100+?)/100/?,0)*?, 0), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNTS. */
  private static final String GROUP_INCR_TEST_DISCOUNTS = " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST( round((discount+?)/?,0)*?, 0), charge), username = ? "
      + " WHERE org_name=? ";

  /** The Constant GROUP_INCR_TEST_DISCOUNT_PERCENTAGE. */
  private static final String GROUP_INCR_TEST_DISCOUNT_PERCENTAGE = " UPDATE diagnostic_charges "
      + " SET discount = LEAST(GREATEST( round(discount*(100+?)/100/?,0)*?, 0), charge), "
      + " username = ? "
      + " WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNTS. */
  private static final String GROUP_APPLY_DISCOUNTS = " UPDATE diagnostic_charges"
      + " SET discount = LEAST(GREATEST( round((charge+?)/?,0)*?, 0), charge), username = ?"
      + " WHERE org_name=? ";

  /** The Constant GROUP_APPLY_DISCOUNT_PERCENTAGE. */
  private static final String GROUP_APPLY_DISCOUNT_PERCENTAGE = " UPDATE diagnostic_charges"
      + " SET discount =LEAST(GREATEST( round(charge+(charge*?/100/?),0)*?, 0), charge),"
      + " username = ?"
      + " WHERE org_name=? ";

  /**
   * Group increase charges with round off.
   *
   * @param orgId
   *          the org id
   * @param bedTypes
   *          the bed types
   * @param testIds
   *          the test ids
   * @param amount
   *          the amount
   * @param isPercentage
   *          the is percentage
   * @param roundTo
   *          the round to
   * @param updateTable
   *          the update table
   * @param userName
   *          the user name
   * @return true, if successful
   */
  public boolean groupIncreaseChargesWithRoundOff(String orgId, List<String> bedTypes,
      List<String> testIds, BigDecimal amount, boolean isPercentage, BigDecimal roundTo,
      String updateTable, String userName) {

    StringBuilder query = null;
    if (updateTable != null && updateTable.equals("UPDATECHARGE")) {
      query = new StringBuilder(isPercentage ? GROUP_INCR_TEST_CHARGES_PERCENTAGE
          : GROUP_INCR_TEST_CHARGES);

    } else if (updateTable.equals("UPDATEDISCOUNT")) {
      query = new StringBuilder(isPercentage ? GROUP_INCR_TEST_DISCOUNT_PERCENTAGE
          : GROUP_INCR_TEST_DISCOUNTS);
    } else {
      query = new StringBuilder(isPercentage ? GROUP_APPLY_DISCOUNT_PERCENTAGE
          : GROUP_APPLY_DISCOUNTS);
    }

    if (bedTypes != null && bedTypes.size() > 0) {
      query.append(" AND bed_type IN (");
      for (int i = 0; i < bedTypes.size(); i++) {
        if (i != bedTypes.size() - 1) {
          query.append("\'" + bedTypes.get(i) + "\',");
        } else {
          query.append("\'" + bedTypes.get(i) + "\')");
        }
      }
    }
    if (testIds != null && testIds.size() > 0) {
      query.append(" AND test_id IN (");
      for (int i = 0; i < testIds.size(); i++) {
        if (i != testIds.size() - 1) {
          query.append("\'" + testIds.get(i) + "\',");
        } else {
          query.append("\'" + testIds.get(i) + "\')");
        }
      }
    }
    // sanity: round to zero is not allowed, can cause div/0
    if (roundTo.equals(BigDecimal.ZERO)) {
      roundTo = BigDecimal.ONE;
    }
    String userNameWithHint = ((null == userName) ? "" : userName) + AUDIT_LOG_HINT;
    return diagnosticChargeRepository.updateDiagCharges(query.toString(), amount, roundTo,
        userNameWithHint, orgId);
  }

  /** The Constant UPDATE_CHARGES. */
  private static final String UPDATE_CHARGES = "UPDATE diagnostic_charges AS target SET "
      + " charge = doroundvarying(tc.charge, ?, ?), "
      + " discount = doroundvarying(tc.discount, ?, ?), " + " priority = tc.priority, "
      + " username = ?, is_override = 'N' " + " FROM diagnostic_charges tc, test_org_details tod "
      + " where tod.org_id = ? and tc.test_id = tod.test_id and tod.base_rate_sheet_id = ? and "
      + " target.test_id = tc.test_id and target.bed_type = tc.bed_type and "
      + " tod.applicable = true and target.is_override != 'Y'"
      + " and tc.org_name = ? and target.org_name = ?";

  /**
   * Update test charges for derived rate plans.
   *
   * @param orgId
   *          the org id
   * @param varianceType
   *          the variance type
   * @param varianceValue
   *          the variance value
   * @param varianceBy
   *          the variance by
   * @param baseOrgId
   *          the base org id
   * @param nearstRoundOfValue
   *          the nearst round of value
   * @param userName
   *          the user name
   * @param orgName
   *          the org name
   * @param upload
   *          the upload
   * @return true, if successful
   */
  public boolean updateTestChargesForDerivedRatePlans(String orgId, String varianceType,
      Double varianceValue, Double varianceBy, String baseOrgId, Double nearstRoundOfValue,
      String userName, String orgName, boolean upload) {

    boolean success = true;
    DatabaseHelper
        .update("ALTER TABLE diagnostic_charges "
            + " DISABLE TRIGGER z_diagnostictest_charges_audit_trigger");

    if (upload) {
      List<BasicDynaBean> rateSheetList = testOrganizationRepository.getRateSheetsByPriority(orgId);
      for (int i = 0; i < rateSheetList.size(); i++) {
        BasicDynaBean currentSheet = rateSheetList.get(i);
        Integer variation = (Integer) currentSheet.get("rate_variation_percent");
        String varType = (variation >= 0) ? "Incr" : "Decr";
        Double varBy = Double.valueOf((variation >= 0 ? variation : -variation));
        Double roundOff = Double.valueOf((Integer) currentSheet.get("round_off_amount"));
        if (i == 0) {
          success = testOrganizationRepository.reinitRatePlan(orgId, varType, varBy,
              (String) currentSheet.get("base_rate_sheet_id"), roundOff, userName, orgName);
        } else {
          success = testOrganizationRepository.updateRatePlan(orgId,
              (String) currentSheet.get("base_rate_sheet_id"), varType, varBy, roundOff, userName,
              orgName);
        }
      }
    } else {
      BigDecimal variance = new BigDecimal(varianceBy);
      BigDecimal roundoff = new BigDecimal(nearstRoundOfValue);
      Object[] updparams = { variance, roundoff, variance, roundoff, userName, orgId, baseOrgId,
          baseOrgId, orgId };
      success = testOrganizationRepository.updateCharges(UPDATE_CHARGES, updparams);
    }

    DatabaseHelper
        .update("ALTER TABLE diagnostic_charges "
            + " ENABLE TRIGGER z_diagnostictest_charges_audit_trigger");
    success &= DatabaseHelper.insert(
        "INSERT INTO diagnostic_charges_audit_log(user_name, operation) VALUES (?, ?)", userName,
        "BULK UPDATE") > 0;
    return success;
  }

  /**
   * Scheduling diag charges.
   *
   * @param testId      old Bean
   */
  public void diagChargeScheduleJob(String testId, String userName) {
    LinkedHashMap<String, Object> queryParams =
        new LinkedHashMap<String, Object>();
    // Maintain the queryparams as per diag charges index
    queryParams.put("org_name", "abov.org_id");
    queryParams.put("bed_type", "abov.bed_type");
    queryParams.put("test_id", testId);
    queryParams.put("priority", "R");
    queryParams.put("charge", BigDecimal.ZERO);
    queryParams.put("username", userName);
    cronJobService.masterChargeScheduleJob(queryParams, "diagnostic_charges", "DIAGNOSTIC");
  }
}
