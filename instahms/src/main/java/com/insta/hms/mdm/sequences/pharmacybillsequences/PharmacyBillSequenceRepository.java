package com.insta.hms.mdm.sequences.pharmacybillsequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class PharmacyBillSequenceRepository.
 */
@Repository
public class PharmacyBillSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant PHARMACY_BILL_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] PHARMACY_BILL_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority", "dept_id", "visit_type", "sale_type", "bill_type"};

  /**
   * Instantiates a new pharmacy bill sequence repository.
   */
  public PharmacyBillSequenceRepository() {
    super(
        "hosp_pharmacy_sale_seq_prefs",
        "pharmacy_bill_seq_id",
        "PHB",
        PHARMACY_BILL_SEQUENCE_UNIQUE_COLUMNS);
  }
}
