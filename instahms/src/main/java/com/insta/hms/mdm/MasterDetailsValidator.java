package com.insta.hms.mdm;

import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterDetailsValidator.
 */
public class MasterDetailsValidator extends MasterValidator {

  /**
   * Validate detail tables.
   *
   * @param detailsMap the details map
   * @return true, if successful
   */
  public boolean validateDetailTables(
      Map<String, Map<String, Map<String, BasicDynaBean>>> detailsMap) {
    Map<String, Object> nestedExMap = new HashMap<String, Object>();
    for (Map.Entry<String, Map<String, Map<String, BasicDynaBean>>> detailTableEntry : detailsMap
        .entrySet()) {
      String detailTableName = detailTableEntry.getKey();
      Map<String, Object> detailExMap = new HashMap<String, Object>();

      /* Validate Insert Bean */
      Map<String, BasicDynaBean> insertBeansMap = detailTableEntry.getValue().get("insert");
      for (Map.Entry<String, BasicDynaBean> insertMapEntry : insertBeansMap.entrySet()) {
        try {
          validateInsert(detailTableName, insertMapEntry.getValue());
        } catch (ValidationException ex) {
          detailExMap.put(insertMapEntry.getKey(), ex.getErrors());
        }
      }

      /* Validate Update Bean */
      Map<String, BasicDynaBean> updateBeansMap = detailTableEntry.getValue().get("update");
      for (Map.Entry<String, BasicDynaBean> updateMapEntry : updateBeansMap.entrySet()) {
        try {
          validateUpdate(detailTableName, updateMapEntry.getValue());
        } catch (ValidationException ex) {
          detailExMap.put(updateMapEntry.getKey(), ex.getErrors());
        }
      }

      if (!detailExMap.isEmpty()) {
        nestedExMap.put(detailTableName, detailExMap);
      }
    }
    if (!nestedExMap.isEmpty()) {
      throw new NestableValidationException(nestedExMap);
    }
    return true;
  }

  /**
   * Validate update.
   *
   * @param detailTableName the detail table name
   * @param bean the bean
   * @return true, if successful
   */
  public boolean validateUpdate(String detailTableName, BasicDynaBean bean) {
    boolean result = applyRuleSet(UPDATE_RULESET_NAME + detailTableName, bean);
    if (!result) {
      throwErrors();
    }
    return true;
  }

  /**
   * Validate insert.
   *
   * @param detailTableName the detail table name
   * @param bean the bean
   * @return true, if successful
   */
  public boolean validateInsert(String detailTableName, BasicDynaBean bean) {
    boolean result = applyRuleSet(INSERT_RULESET_NAME + detailTableName, bean);
    if (!result) {
      throwErrors();
    }
    return true;
  }

  /**
   * Adds the insert rule.
   *
   * @param detailTableName the detail table name
   * @param rule the rule
   * @param fields the fields
   */
  public final void addInsertRule(String detailTableName, ValidationRule rule, String[] fields) {
    addRule(INSERT_RULESET_NAME + detailTableName, rule, fields);
  }

  /**
   * Adds the update rule.
   *
   * @param detailTableName the detail table name
   * @param rule the rule
   * @param fields the fields
   */
  public final void addUpdateRule(String detailTableName, ValidationRule rule, String[] fields) {
    addRule(UPDATE_RULESET_NAME + detailTableName, rule, fields);
  }

  /**
   * Adds the default rule.
   *
   * @param detailTableName the detail table name
   * @param rule the rule
   * @param fields the fields
   */
  public final void addDefaultRule(String detailTableName, ValidationRule rule, String[] fields) {
    addRule(DEFAULT_RULESET_NAME + detailTableName, rule, fields);
  }

}
