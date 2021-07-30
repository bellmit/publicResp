package com.insta.hms.mdm.vitals;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterDetailsValidator;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Class VitalValidator. */
@Component
public class VitalValidator extends MasterDetailsValidator {

  /** The Constant log. */
  private static final Logger log = LoggerFactory.getLogger(VitalValidator.class);

  /** The vitals service. */
  @LazyAutowired
  VitalsService vitalsService;

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "param_container",
      "param_label", "param_order", "param_status", };

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "param_id",
      "param_container", "param_label", "param_order", "param_status", };
  // private static final ValidationRule VALIDATE_EXPRESSION = new ValidateExpression();

  /** The Constant RESULT_RANGE_AGE_VALIDATION. */
  private static final ValidationRule RESULT_RANGE_AGE_VALIDATION = new ResultRangeAgeValidation();

  /** The Constant RESULT_RANGE_VALIDATION. */
  private static final ValidationRule RESULT_RANGE_VALIDATION = new ResultRangeValidation();
  /** The Constant RESULT_RANGE_VALIDATION. */
  private static final ValidationRule RESULT_RANGE_NULL_VALIDATION = 
      new ResultRangeNullValidation();

  /** The errors map. */
  private ValidationErrorMap errorsMap = new ValidationErrorMap();

  /** The Constant CENTER_ID. */
  private static final String CENTER_ID = "center_id";

  /** The Constant CENTERS. */
  private static final String CENTERS = "centers";

  /** The Constant DEFAULT. */
  private static final String DEFAULT = "default";

  /** The Constant DEPARTMENT_ID. */
  private static final String DEPARTMENT_ID = "dept_id";

  /** The Constant DEPARTMENTS. */
  private static final String DEPARTMENTS = "departments";

  /** The Constant MANDATORY. */
  private static final String MANDATORY = "mandatory";

  /** The Constant PARAMETER_APPLICABILITIES. */
  private static final String PARAMETER_APPLICABILITIES = "parameter_applicabilities";

  /** The Constant VITAL_PARAMETER_ID. */
  private static final String VITAL_PARAMETER_ID = "param_id";

  /**
   * Instantiates a new vital validator.
   */
  public VitalValidator() {
    super();
    /* INSERT VALIDATION RULES. */
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule("vital_reference_range_master", RESULT_RANGE_AGE_VALIDATION, null);
    addInsertRule("vital_reference_range_master", RESULT_RANGE_VALIDATION, null);
    addInsertRule("vital_reference_range_master", RESULT_RANGE_NULL_VALIDATION, null);

    /* UPDATE VALIDATION RULES */
    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addUpdateRule("vital_reference_range_master", RESULT_RANGE_AGE_VALIDATION, null);
    addUpdateRule("vital_reference_range_master", RESULT_RANGE_VALIDATION, null);
    addUpdateRule("vital_reference_range_master", RESULT_RANGE_NULL_VALIDATION, null);
  }

  /**
   * Validate insert.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  @Override
  public boolean validateInsert(BasicDynaBean bean) {
    super.applyRuleSet(INSERT_RULESET_NAME, bean);
    boolean ok = true;
    errorsMap = super.getErrors();
    if (null != errorMap.getErrorMap() && !errorMap.getErrorMap().isEmpty()) {
      ok = false;
    }
    List<BasicDynaBean> vitalsList = vitalsService.listAll();
    Boolean isDuplicate = duplicateChecks(bean, vitalsList);
    if (isDuplicate) {
      ok = false;
    }

    if (null != bean.get("expr_for_calc_result") && !bean.get("expr_for_calc_result").equals("")) {
      boolean isValidExp = istExpressionValid((String) bean.get("expr_for_calc_result"),
          vitalsList);
      if (!isValidExp) {
        errorsMap.addError("expr_for_calc_result", "exception.vital.parameter.expression", Arrays
            .asList((String) bean.get("param_label"), (String) bean.get("expr_for_calc_result")));
        ok = false;
      }
    }
    if (!errorsMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorsMap);
    }
    return ok;
  }

  @Override
  public boolean validateUpdate(BasicDynaBean bean) {
    super.applyRuleSet(UPDATE_RULESET_NAME, bean);
    boolean ok = true;
    errorsMap = super.getErrors();
    if (null != errorMap.getErrorMap() && !errorMap.getErrorMap().isEmpty()) {
      ok = false;
    }
    List<BasicDynaBean> vitalsList = vitalsService.listAll();
    Boolean isDuplicate = duplicateChecks(bean, vitalsList);
    if (isDuplicate) {
      ok = false;
    }

    if (null != bean.get("expr_for_calc_result") && !bean.get("expr_for_calc_result").equals("")) {
      boolean isValidExp = istExpressionValid((String) bean.get("expr_for_calc_result"),
          vitalsList);
      if (!isValidExp) {
        errorsMap.addError("expr_for_calc_result", "exception.vital.parameter.expression", Arrays
            .asList((String) bean.get("param_label"), (String) bean.get("expr_for_calc_result")));
        ok = false;
      }
    }
    if (!errorsMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorsMap);
    }
    return ok;
  }

  /**
   * Duplicate param_label and param_order checks.
   *
   * @param bean
   *          the bean
   * @param vitalsList
   *          the vitals list
   * @return true, if successful
   */
  private boolean duplicateChecks(BasicDynaBean bean, List<BasicDynaBean> vitalsList) {
    boolean status = false;
    String paramLabel = (String) bean.get("param_label");
    Integer paramOrder = (Integer) bean.get("param_order");
    Integer paramId = (Integer) bean.get("param_id");
    String paramStatus = (String) bean.get("param_status");
    for (BasicDynaBean basicDynaBean : vitalsList) {
      if (basicDynaBean.get("param_label").equals(paramLabel) 
          && (paramId == null || !paramId.equals(basicDynaBean.get("param_id")))) {
        errorsMap.addError("param_label", "exception.entered.value.all.ready.exists",
            Arrays.asList(paramLabel));
        status = true;
      } else if (((Integer) basicDynaBean.get("param_order")).equals(paramOrder) 
          && (paramStatus.equals("A") && basicDynaBean.get("param_status").equals("A")) 
          &&  (paramId == null || !paramId.equals(basicDynaBean.get("param_id")))) {
        errorsMap.addError("param_order", "exception.entered.order.value.already.exists");
        status = true;
      }
    }
    return status;
  }

  /**
   * Checks if is t expression valid.
   *
   * @param expression
   *          the expression
   * @param vitalsList
   *          the vitals list
   * @return true, if is t expression valid
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean istExpressionValid(String expression, List<BasicDynaBean> vitalsList) {
    // List<BasicDynaBean> vitalMaster = vitalsService.lookup(false);
    BasicDynaBean vitalMasterBean = null;
    StringWriter writer = new StringWriter();
    boolean valid = false;
    expression = "<#setting number_format=\"##.##\">\n" + expression;
    try {
      HashMap<String, Object> resultParams = new HashMap<String, Object>();
      Map<String, Object> results = new HashMap<String, Object>();
      List values = new ArrayList();
      for (int i = 0; i < vitalsList.size(); i++) {
        vitalMasterBean = vitalsList.get(i);
        resultParams.put((String) vitalMasterBean.get("param_label"), 1);
        values.add(1);
      }
      results.put("results", resultParams);
      results.put("values", values);
      Template expressionTemplate = new Template("expression", new StringReader(expression),
          new Configuration());
      expressionTemplate.process(results, writer);
    } catch (InvalidReferenceException ine) {
      log.error("", ine);
      return false;
    } catch (TemplateException ex) {
      log.error("", ex);
      return false;
    } catch (freemarker.core.ParseException ex) {
      log.error("", ex);
      return false;
    } catch (ArithmeticException ex) {
      log.error("", ex);
      return false;
    } catch (Exception ex) {
      log.error("", ex);
      return false;
    }
    valid = true;
    valid = writer.toString().contains("[^.\\d]") ? false : true;
    try {
      if (!writer.toString().trim().isEmpty()) {
        BigDecimal validNumber = new BigDecimal(writer.toString());
      }
    } catch (NumberFormatException ne) {
      log.error("", ne);
      valid = false;
    }
    return valid;
  }

  /**
   * Put value inside a deeply nested map.
   *
   * @param nestedMap
   *          the nested map
   * @param keys
   *          the keys in each nesting of the map
   * @param value
   *          the value
   */
  private void putInNestedMap(Map<String, Object> nestedMap, String[] keys, Object value) {
    Map<String, Object> currentMap = nestedMap;
    int index = 0;
    while (index < (keys.length - 1)) {
      String key = keys[index];
      Object currentValue = currentMap.get(key);
      if (currentValue == null) {
        currentValue = new HashMap<>();
        currentMap.put(key, currentValue);
      } else if (!(currentValue instanceof Map)) {
        break;
      }
      currentMap = (Map<String, Object>) currentValue;
      index++;
    }
    if (index == (keys.length - 1)) { // Previous loop did not break
      String key = keys[index];
      if (currentMap.get(key) instanceof Map && value instanceof Map) {
        currentMap = (Map<String, Object>) currentMap.get(key);
        currentMap.putAll((Map<String, Object>) value);
      } else {
        currentMap.put(key, value);
      }
    }
  }

  /**
   * Validate vital applicability save.
   *
   * @param requestBody
   *          the request body
   */
  public void validateVitalApplicabilitySave(Map<String, Object> requestBody) {
    Map<String, Object> errorsMap = new HashMap<>();
    List<Map<String, Object>> departments = (List<Map<String, Object>>) requestBody
        .get(DEPARTMENTS);
    if (departments == null) {
      ValidationErrorMap departmentsValidationErrors = new ValidationErrorMap();
      departmentsValidationErrors.addError(DEPARTMENTS, "exception.vital.notnull.departments");
      errorsMap.putAll(new ValidationException(departmentsValidationErrors).getErrors());
    } else {
      List<Map<String, Object>> centers = null;
      List<Map<String, Object>> parameterApplicabilities = null;
      String currentDepartmentId = null;
      Integer currentCenterId = null;
      Integer departmentIndex = 0;
      for (Map<String, Object> department : departments) {
        currentDepartmentId = (String) department.get(DEPARTMENT_ID);
        if (currentDepartmentId == null) {
          ValidationErrorMap departmentIdValidationErrors = new ValidationErrorMap();
          departmentIdValidationErrors.addError(DEPARTMENT_ID,
              "exception.vital.notnull.department.id");
          putInNestedMap(errorsMap, new String[] { DEPARTMENTS, departmentIndex.toString() },
              new ValidationException(departmentIdValidationErrors).getErrors());
        }
        centers = (List<Map<String, Object>>) department.get(CENTERS);
        if (centers == null) {
          ValidationErrorMap centersValidationErrors = new ValidationErrorMap();
          centersValidationErrors.addError(CENTERS, "exception.vital.notnull.centers");
          putInNestedMap(errorsMap, new String[] { DEPARTMENTS, departmentIndex.toString() },
              new ValidationException(centersValidationErrors).getErrors());
        } else {
          Integer centerIndex = 0;
          for (Map<String, Object> center : centers) {
            currentCenterId = (Integer) center.get(CENTER_ID);
            if (currentCenterId == null) {
              ValidationErrorMap centerIdValidationErrors = new ValidationErrorMap();
              centerIdValidationErrors.addError(CENTER_ID, "exception.vital.notnull.center.id");
              putInNestedMap(errorsMap,
                  new String[] { DEPARTMENTS, departmentIndex.toString(), CENTERS,
                      centerIndex.toString() },
                  new ValidationException(centerIdValidationErrors).getErrors());
            }
            parameterApplicabilities = (List<Map<String, Object>>) center
                .get(PARAMETER_APPLICABILITIES);
            if (parameterApplicabilities == null) {
              ValidationErrorMap paramApplicabilitiesValidationErrors = new ValidationErrorMap();
              paramApplicabilitiesValidationErrors.addError(PARAMETER_APPLICABILITIES,
                  "exception.vital.notnull.parameter.applicabilitites");
              putInNestedMap(errorsMap,
                  new String[] { DEPARTMENTS, departmentIndex.toString(), CENTERS,
                      centerIndex.toString() },
                  new ValidationException(paramApplicabilitiesValidationErrors).getErrors());
            } else {
              Integer parameterApplicabilityIndex = 0;
              for (Map<String, Object> parameterApplicability : parameterApplicabilities) {
                ValidationErrorMap paramApplicabilityValidationErrors = new ValidationErrorMap();
                Integer parameterId = (Integer) parameterApplicability.get(VITAL_PARAMETER_ID);
                if (parameterId == null) {
                  paramApplicabilityValidationErrors.addError(VITAL_PARAMETER_ID,
                      "exception.vital.notnull.param_id");
                }
                Boolean parameterDefault = (parameterApplicability.get(DEFAULT) == null ? null
                    : "Y".equals(parameterApplicability.get(DEFAULT)));
                if (parameterDefault == null) {
                  paramApplicabilityValidationErrors.addError(DEFAULT,
                      "exception.vital.notnull.default");
                }
                String mandatory = (String) parameterApplicability.get(MANDATORY);
                if (mandatory == null) {
                  paramApplicabilityValidationErrors.addError(MANDATORY,
                      "exception.vital.notnull.mandatory");
                } else {
                  if (!parameterDefault && mandatory.equals("Y")) {
                    paramApplicabilityValidationErrors.addError(MANDATORY,
                        "exception.vital.cannot.be.mandatory.if.not.default");
                  }
                }
                if (!paramApplicabilityValidationErrors.getErrorMap().isEmpty()) {
                  putInNestedMap(errorsMap,
                      new String[] { DEPARTMENTS, departmentIndex.toString(), CENTERS,
                          centerIndex.toString(), PARAMETER_APPLICABILITIES,
                          parameterApplicabilityIndex.toString() },
                      new ValidationException(paramApplicabilityValidationErrors).getErrors());
                }
                parameterApplicabilityIndex++;
              }
            }
            centerIndex++;
          }
        }
        departmentIndex++;
      }
    }

    if (!errorsMap.isEmpty()) {
      throw new NestableValidationException(errorsMap);
    }
  }

}
