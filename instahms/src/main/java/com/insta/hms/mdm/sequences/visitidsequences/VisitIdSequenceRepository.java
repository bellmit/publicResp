package com.insta.hms.mdm.sequences.visitidsequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class VisitIdSequenceRepository.
 */
@Repository
public class VisitIdSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant VISIT_ID_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] VISIT_ID_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority", "center_id", "visit_type"};

  /**
   * Instantiates a new visit id sequence repository.
   */
  public VisitIdSequenceRepository() {
    super("hosp_op_ip_seq_prefs", "visit_seq_id", "VID", VISIT_ID_SEQUENCE_UNIQUE_COLUMNS);
  }
}
