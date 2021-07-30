package com.insta.hms.mdm.stores;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.CenterCheckRule;
import com.insta.hms.common.validation.MaximumLengthRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.servicedepartments.ServiceDepartmentsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StoreValidator.
 * 
 * @author tanmay.k
 * 
 *         Validation - Not Null, Maximum Length, User Store, Multi Store, Service and Diagnostics
 *         Dependents, Bill Raise not allowed if not a sales store
 */
@Component
public class StoreValidator extends MasterValidator {

  /** The store service. */
  @LazyAutowired
  private StoreService storeService;

  /** The service departments service. */
  @LazyAutowired
  private ServiceDepartmentsService serviceDepartmentsService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] { "dept_name",
      "store_type_id", "status", "is_sales_store" };

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] { "dept_name",
      "store_type_id", "dept_id", "is_sales_store" };

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /** The Constant MAXIMUM_LENGTH_FIELDS_INSERT. */
  private static final String[] MAXIMUM_LENGTH_FIELDS_INSERT = new String[] { "dept_name" };

  /** The Constant MAXIMUM_LENGTH_RULE_INSERT. */
  private static final ValidationRule MAXIMUM_LENGTH_RULE_INSERT = new MaximumLengthRule(100);

  /** The Constant MAXIMUM_LENGTH_FIELDS_UPDATE. */
  private static final String[] MAXIMUM_LENGTH_FIELDS_UPDATE = new String[] { "dept_name" };

  /** The Constant MAXIMUM_LENGTH_RULE_UPDATE. */
  private static final ValidationRule MAXIMUM_LENGTH_RULE_UPDATE = new MaximumLengthRule(100);

  /** The Constant CENTER_CHECK_RULE. */
  private static final ValidationRule CENTER_CHECK_RULE = new CenterCheckRule();

  /** The Constant CENTER_IDENTIFIER_FIELD. */
  private static final String[] CENTER_IDENTIFIER_FIELD = new String[] { "center_id" };

  /** The errors key. */
  private static final String ERRORS_KEY = "Stores";

  /** The failed validation message key. */
  private static final String FAILED_DEPENDENTS_VALIDATION_MESSAGE_KEY = 
      "exception.stores.dependents.validation.failed";

  /** The bill raise not allowed validation message key. */
  private static final String BILL_RAISE_NOT_ALLOWED_VALIDATION_MESSAGE_KEY = 
      "exception.stores.notallowed.billraise";

  /**
   * Instantiates a new store validator.
   */
  public StoreValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);

    addInsertRule(MAXIMUM_LENGTH_RULE_INSERT, MAXIMUM_LENGTH_FIELDS_INSERT);
    addUpdateRule(MAXIMUM_LENGTH_RULE_UPDATE, MAXIMUM_LENGTH_FIELDS_UPDATE);

    addInsertRule(CENTER_CHECK_RULE, CENTER_IDENTIFIER_FIELD);
    addUpdateRule(CENTER_CHECK_RULE, CENTER_IDENTIFIER_FIELD);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterValidator#validateUpdate(org.apache.commons.
   * beanutils.BasicDynaBean)
   */
  @Override
  public boolean validateUpdate(BasicDynaBean bean) {
    super.validateUpdate(bean);
    ValidationErrorMap errorsMap = new ValidationErrorMap();
    String isSalesStore = (String) bean.get("is_sales_store");
    String isBillRaisingAllowed = (String) bean.get("allowed_raise_bill");

    BasicDynaBean databaseBean = storeService.findByUniqueName((String) bean.get("dept_name"),
        "dept_name");
    // I is stands for inactive. Basically, only valid when a request is to
    // soft delete a store.
    if (null != bean.get("status")
        && ("I".equals((String) bean.get("status")) && (null == databaseBean || !("I"
            .equals(databaseBean.get("status")))))) {
      List<BasicDynaBean> userDefaultStoreDependents = storeService
          .getUserDefaultStoreDependents(bean);
      if (userDefaultStoreDependents != null && !userDefaultStoreDependents.isEmpty()) {
        errorsMap.addError(ERRORS_KEY + "UserDefaultValidation",
            FAILED_DEPENDENTS_VALIDATION_MESSAGE_KEY,
            Arrays.asList(new String[] { "User default" }));
      }

      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put("store_id", (Integer) bean.get("dept_id"));
      List<BasicDynaBean> serviceStoreDependents = serviceDepartmentsService
          .lookup(true, filterMap);

      if (serviceStoreDependents != null && !serviceStoreDependents.isEmpty()) {
        errorsMap.addError(ERRORS_KEY + "ServicesDependentsValidation",
            FAILED_DEPENDENTS_VALIDATION_MESSAGE_KEY,
            Arrays.asList(new String[] { "Service Department" }));
      }

      List<BasicDynaBean> diagnosticsStoreDependents = storeService
          .getDiagnosticsStoreDependents(bean);
      if (diagnosticsStoreDependents != null && !diagnosticsStoreDependents.isEmpty()) {
        errorsMap.addError(ERRORS_KEY + "DiagnosticsDependentsValidation",
            FAILED_DEPENDENTS_VALIDATION_MESSAGE_KEY,
            Arrays.asList(new String[] { "Diagnostic Department" }));
      }

      List<BasicDynaBean> userMultiStoreDependents = storeService.getUserMultiStoreDependents(bean);
      if (userMultiStoreDependents != null && !userMultiStoreDependents.isEmpty()) {
        errorsMap.addError(ERRORS_KEY + "UserMultiStoreValidation",
            FAILED_DEPENDENTS_VALIDATION_MESSAGE_KEY, Arrays.asList(new String[] { "User multi" }));
      }

    }

    if (isSalesStore != null && isBillRaisingAllowed != null && "N".equals(isSalesStore)
        && "Y".equals(isBillRaisingAllowed)) {
      errorsMap.addError(ERRORS_KEY + "BillRaisingNotAllowedValidation",
          BILL_RAISE_NOT_ALLOWED_VALIDATION_MESSAGE_KEY);
    }

    if (!errorsMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errorsMap);
    }

    return true;
  }
}
