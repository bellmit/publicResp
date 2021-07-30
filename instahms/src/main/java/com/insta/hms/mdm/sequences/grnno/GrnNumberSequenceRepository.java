package com.insta.hms.mdm.sequences.grnno;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class GrnNumberSequenceRepository.
 */
@Repository
public class GrnNumberSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant GRN_NUMBER_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] GRN_NUMBER_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority", "store_id"};

  /**
   * Instantiates a new grn number sequence repository.
   */
  public GrnNumberSequenceRepository() {
    super("hosp_grn_seq_prefs", "grn_number_seq_id", "GRN", GRN_NUMBER_SEQUENCE_UNIQUE_COLUMNS);
  }
}
