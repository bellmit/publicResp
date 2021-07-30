package com.insta.hms.mdm.sequences.labnumbersequences;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.sequences.SequenceMasterValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The Class LabNumberSequenceValidator.
 */
@Component
public class LabNumberSequenceValidator extends SequenceMasterValidator {
  
  /** The lab no sequence service. */
  @LazyAutowired LabNumberSequenceService labNoSequenceService;

  /** The Constant NOT_NULL_FIELDS_INSERT. */
  private static final String[] NOT_NULL_FIELDS_INSERT = new String[] {"pattern_id", "priority"};
  
  /** The Constant NOT_NULL_FIELDS_UPDATE. */
  private static final String[] NOT_NULL_FIELDS_UPDATE =
      new String[] {"lab_number_seq_id", "pattern_id", "priority"};

  /**
   * Instantiates a new lab number sequence validator.
   */
  public LabNumberSequenceValidator() {
    super();
    super.errorsKey = "Lab Number Sequence";
    addInsertRule(NOT_NULL_RULE_INSERT, NOT_NULL_FIELDS_INSERT);
    addUpdateRule(NOT_NULL_RULE_UPDATE, NOT_NULL_FIELDS_UPDATE);
  }

  /**
   * @see com.insta.hms.mdm.sequences.SequenceMasterValidator#getRuleList(
   *              org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List<BasicDynaBean> getRuleList(BasicDynaBean bean) {
    return labNoSequenceService.getConfictRules(bean);
  }
}
