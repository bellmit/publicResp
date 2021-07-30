package com.insta.hms.mdm.sequences.receiptnumber;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class ReceiptNumberSequenceRepository.
 */
@Repository
public class ReceiptNumberSequenceRepository extends SequenceMasterRepository<Integer> {
  
  /** The Constant RECEIPT_NUMBER_UNIQUE_COLUMNS. */
  public static final String[] RECEIPT_NUMBER_UNIQUE_COLUMNS =
      new String[] {
        "pattern_id", "priority", "center_id", "visit_type", "bill_type", "restriction_type"
      };

  /**
   * Instantiates a new receipt number sequence repository.
   */
  public ReceiptNumberSequenceRepository() {
    super("hosp_receipt_seq_prefs", "receipt_number_seq_id", "REP", RECEIPT_NUMBER_UNIQUE_COLUMNS);
  }
}
