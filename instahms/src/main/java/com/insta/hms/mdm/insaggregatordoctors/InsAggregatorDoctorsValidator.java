package com.insta.hms.mdm.insaggregatordoctors;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.doctors.DoctorService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** The Class InsAggregatorDoctorsValidator. */
@Component
public class InsAggregatorDoctorsValidator extends MasterValidator {

  /** The doctor service. */
  @LazyAutowired DoctorService doctorService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT =
      new String[] {"ia_id", "doctor_id", "clinician_id"};

  /** The Constant NOT_NULL_RULE_INSERT. */
  private static final ValidationRule NOT_NULL_RULE_INSERT = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] {"ia_id", "doctor_id"};

  /** The Constant NOT_NULL_RULE_UPDATE. */
  private static final ValidationRule NOT_NULL_RULE_UPDATE = new NotNullRule();

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterValidator#validateInsert(org.apache.commons.beanutils.
   * BasicDynaBean)
   */
  @Override
  public boolean validateInsert(BasicDynaBean bean) {
    if (isValidDoctorId(bean) && applyRuleSet(INSERT_RULESET_NAME, bean)) {
      return true;
    } else {
      throwErrors();
    }
    return false;
  }

  /**
   * Checks if is valid doctor id.
   *
   * @param bean the bean
   * @return true, if is valid doctor id
   */
  public boolean isValidDoctorId(BasicDynaBean bean) {
    String doctorId = (String) bean.get("doctor_id");
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, String> key = new HashMap<String, String>();
    key.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(key);
    if (null == doctorBean || doctorBean.get("doctor_id") == null) {
      errMap.addError("doctor_id", "exception.pac.notvalid.doctor");
    }
    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    return true;
  }

  /** Instantiates a new ins aggregator doctors validator. */
  public InsAggregatorDoctorsValidator() {
    super();
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }
}
