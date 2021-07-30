package com.insta.hms.mdm.insaggregatorpharmacies;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.stores.StoreService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** The Class InsAggregatorPharmaciesValidator. */
@Component
public class InsAggregatorPharmaciesValidator extends MasterValidator {

  /** The store service. */
  @LazyAutowired StoreService storeService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"ia_id", "pharmacy_id", "facility_id"};

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] {"ia_id", "pharmacy_id"};

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /**
   * Checks if is valid store id.
   *
   * @param storeId the store id
   * @return true, if is valid store id
   */
  public boolean isValidStoreId(String storeId) {
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, String> key = new HashMap<String, String>();
    key.put("dept_id", storeId);
    BasicDynaBean storeBean = storeService.findByPk(key);
    if (null == storeBean || storeBean.get("dept_id") == null) {
      errMap.addError("dept_id", "exception.pac.notvalid.store");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }

    return true;
  }

  /** Instantiates a new ins aggregator pharmacies validator. */
  public InsAggregatorPharmaciesValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }
}
