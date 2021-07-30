
package com.insta.hms.mdm.vitalparameters;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class VitalParameterValidator.
 *
 * @author anup vishwas
 */

@Component("parameterValidator")
public class VitalParameterValidator extends MasterValidator {
  
  /** The vital parameter service. */
  @LazyAutowired
  VitalParameterService vitalParameterService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "param_container",
      "param_label", "param_order", "param_status" };

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "param_id",
      "param_container", "param_label", "param_order", "param_status" };

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /** The errors map. */
  ValidationErrorMap errorsMap = new ValidationErrorMap();
  
  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(VitalParameterValidator.class);

  /**
   * Instantiates a new Vital Parameter Validator.
   */
  public VitalParameterValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterValidator#validateInsert(BasicDynaBean)
   */
  @Override
  public boolean validateInsert(BasicDynaBean bean) {
    // super.validateInsert(bean);
    super.applyRuleSet(INSERT_RULESET_NAME, bean);
    errorsMap = super.getErrors();
    boolean status = false;
    if (null != bean.get("expr_for_calc_result") && !bean.get("expr_for_calc_result").equals("")) {
      status = istExpressionValid((String) bean.get("expr_for_calc_result"));
      if (!status) {
        errorsMap.addError("expr_for_calc_result", "exception.vital.parameter.expression", Arrays
            .asList((String) bean.get("param_label"), (String) bean.get("expr_for_calc_result")));
      }
    }
    if (!errorsMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorsMap);
    }
    return status;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterValidator#validateUpdate(BasicDynaBean)
   */
  @Override
  public boolean validateUpdate(BasicDynaBean bean) {
    // super.validateUpdate(bean);
    super.applyRuleSet(UPDATE_RULESET_NAME, bean);
    errorsMap = super.getErrors();
    boolean status = false;
    if (null != bean.get("expr_for_calc_result") && !bean.get("expr_for_calc_result").equals("")) {
      status = istExpressionValid((String) bean.get("expr_for_calc_result"));
      if (!status) {
        errorsMap.addError("expr_for_calc_result", "exception.vital.parameter.expression", Arrays
            .asList((String) bean.get("param_label"), (String) bean.get("expr_for_calc_result")));
      }
    }
    if (!errorsMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorsMap);
    }

    return status;
  }

  /**
   * Checks if is t expression valid.
   *
   * @param expression the expression
   * @return true, if is t expression valid
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean istExpressionValid(String expression) {
    List<BasicDynaBean> vitalMaster = vitalParameterService.listAll();
    BasicDynaBean vitalMasterBean = null;
    StringWriter writer = new StringWriter();
    boolean valid = false;
    expression = "<#setting number_format=\"##.##\">\n" + expression;
    try {
      HashMap<String, Object> resultParams = new HashMap<String, Object>();
      Map<String, Object> results = new HashMap<String, Object>();
      List values = new ArrayList();
      for (int i = 0; i < vitalMaster.size(); i++) {
        vitalMasterBean = vitalMaster.get(i);
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
    } catch (TemplateException | ArithmeticException | IOException exception) {
      log.error("", exception);
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
}
