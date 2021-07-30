package com.insta.hms.mdm.sequences.labnumbersequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class LabNumberSequenceRepository.
 */
@Repository
public class LabNumberSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant LAB_NUMBER_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] LAB_NUMBER_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority"};

  /**
   * Instantiates a new lab number sequence repository.
   */
  public LabNumberSequenceRepository() {
    super(
        "hosp_lab_number_seq_prefs",
        "lab_number_seq_id",
        "LAB",
        LAB_NUMBER_SEQUENCE_UNIQUE_COLUMNS);
  }
}
