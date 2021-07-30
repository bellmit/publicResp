package com.insta.hms.mdm.sequences.vouchernosequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class VoucherNoSequenceRepository.
 */
@Repository
public class VoucherNoSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant VOUCHER_NO_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] VOUCHER_NO_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority", "center_id"};

  /**
   * Instantiates a new voucher no sequence repository.
   */
  public VoucherNoSequenceRepository() {
    super("hosp_voucher_seq_prefs", "voucher_seq_id", "PVN", VOUCHER_NO_SEQUENCE_UNIQUE_COLUMNS);
  }
}
