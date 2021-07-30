package com.insta.hms.mdm.sequences.billsequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class BillSequenceRepository.
 */
@Repository
public class BillSequenceRepository extends SequenceMasterRepository<Integer> {
  
  /** The Constant BILL_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] BILL_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {
        "pattern_id",
        "priority",
        "center_id",
        "visit_type",
        "bill_type",
        "restriction_type",
        "is_credit_note",
        "is_tpa"
      };

  /**
   * Instantiates a new bill sequence repository.
   */
  public BillSequenceRepository() {
    super("hosp_bill_seq_prefs", "bill_seq_id", "BLN", BILL_SEQUENCE_UNIQUE_COLUMNS);
  }
}
