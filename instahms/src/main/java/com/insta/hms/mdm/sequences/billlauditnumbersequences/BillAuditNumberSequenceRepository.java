package com.insta.hms.mdm.sequences.billlauditnumbersequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class BillAuditNumberSequenceRepository.
 */
@Repository
public class BillAuditNumberSequenceRepository extends SequenceMasterRepository<Integer> {
  
  /** The Constant BILL_AUDIT_CONTROL_NUMBER_UNIQUE_COLUMNS. */
  public static final String[] BILL_AUDIT_CONTROL_NUMBER_UNIQUE_COLUMNS =
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
   * Instantiates a new bill audit number sequence repository.
   */
  public BillAuditNumberSequenceRepository() {
    super(
        "hosp_bill_audit_seq_prefs",
        "bill_audit_number_seq_id",
        "ACN",
        BILL_AUDIT_CONTROL_NUMBER_UNIQUE_COLUMNS);
  }
}
