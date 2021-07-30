package com.insta.hms.mdm.insaggregatortpainsco;

import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterDetailsValidator;

import org.springframework.stereotype.Component;

/**
 * The Class InsAggregatorTpaInsCoValidator.
 */
@Component
public class InsAggregatorTpaInsCoValidator extends MasterDetailsValidator {

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] {"ia_id"};

  /** The Constant NOT_NULL_RULE. */
  private static final ValidationRule NOT_NULL_RULE = new NotNullRule();

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE = new String[] {"ia_id", "ia_tpa_insco_id"};

  /** The Constant INS_AGGREGATOR_TPA_INSURANCECO_NOT_NULL_FIELDS_INSERT. */
  private static final String[] INS_AGGREGATOR_TPA_INSURANCECO_NOT_NULL_FIELDS_INSERT =
      new String[] {"ia_id", "ia_tpa_insco_id"};

  /** The Constant INS_AGGREGATOR_TPA_INSURANCECO_NOT_NULL_FIELDS_UPDATE. */
  private static final String[] INS_AGGREGATOR_TPA_INSURANCECO_NOT_NULL_FIELDS_UPDATE =
      new String[] {"ia_id", "ia_tpa_insco_id"};

  /**
   * Instantiates a new ins aggregator tpa ins co validator.
   */
  public InsAggregatorTpaInsCoValidator() {
    super();
    addInsertRule(NOT_NULL_RULE, NOT_NULL_FIELDS_INSERT);
    addInsertRule(
        "ia_tpa_insco_supported_services",
        NOT_NULL_RULE,
        INS_AGGREGATOR_TPA_INSURANCECO_NOT_NULL_FIELDS_INSERT);

    addUpdateRule(NOT_NULL_RULE, NOT_NULL_FIELDS_UPDATE);
    addUpdateRule(
        "ia_tpa_insco_supported_services",
        NOT_NULL_RULE,
        INS_AGGREGATOR_TPA_INSURANCECO_NOT_NULL_FIELDS_UPDATE);
  }
}


