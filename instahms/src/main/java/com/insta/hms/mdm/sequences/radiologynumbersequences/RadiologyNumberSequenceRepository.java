package com.insta.hms.mdm.sequences.radiologynumbersequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class RadiologyNumberSequenceRepository.
 */
@Repository
public class RadiologyNumberSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant RADIOLOGY_NUMBER_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] RADIOLOGY_NUMBER_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority"};

  /**
   * Instantiates a new radiology number sequence repository.
   */
  public RadiologyNumberSequenceRepository() {
    super(
        "hosp_radiology_number_seq_prefs",
        "radiology_number_seq_id",
        "RAD",
        RADIOLOGY_NUMBER_SEQUENCE_UNIQUE_COLUMNS);
  }
}
