package com.insta.hms.mdm.sequences.vouchernosequences;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.sequences.SequenceMasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The Class VoucherNoSequenceValidator.
 */
@Component
public class VoucherNoSequenceValidator extends SequenceMasterValidator {
  
  /** The voucher no sequence service. */
  @LazyAutowired VoucherNoSequenceService voucherNoSequenceService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] {"pattern_id", "priority"};
  
  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"voucher_seq_id", "pattern_id", "priority"};

  /**
   * Instantiates a new voucher no sequence validator.
   */
  public VoucherNoSequenceValidator() {
    super();
    super.errorsKey = "Voucher Number Sequence";
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

  /**
   * @see com.insta.hms.mdm.sequences.SequenceMasterValidator#getRuleList(
   *                  org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getRuleList(BasicDynaBean bean) {
    return voucherNoSequenceService.getConfictRules(bean);
  }
}
