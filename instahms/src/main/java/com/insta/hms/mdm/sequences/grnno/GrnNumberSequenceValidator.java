package com.insta.hms.mdm.sequences.grnno;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.sequences.SequenceMasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The Class GrnNumberSequenceValidator.
 */
@Component
public class GrnNumberSequenceValidator extends SequenceMasterValidator {

  /** The grn number sequence service. */
  @LazyAutowired GrnNumberSequenceService grnNumberSequenceService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] {"pattern_id", "priority"};

  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"grn_number_seq_id", "pattern_id", "priority"};

  /**
   * Instantiates a new grn number sequence validator.
   */
  public GrnNumberSequenceValidator() {
    super();
    super.errorsKey = "GRN Number Sequence";
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

  /**
   * @see com.insta.hms.mdm.sequences.SequenceMasterValidator#getRuleList(
   *                                  org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getRuleList(BasicDynaBean bean) {
    return grnNumberSequenceService.getConfictRules(bean);
  }
}
