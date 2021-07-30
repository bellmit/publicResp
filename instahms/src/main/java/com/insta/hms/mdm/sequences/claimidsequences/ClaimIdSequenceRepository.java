package com.insta.hms.mdm.sequences.claimidsequences;

import com.insta.hms.mdm.sequences.SequenceMasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class ClaimIdSequenceRepository.
 */
@Repository
public class ClaimIdSequenceRepository extends SequenceMasterRepository<Integer> {

  /** The Constant CLAIM_ID_SEQUENCE_UNIQUE_COLUMNS. */
  public static final String[] CLAIM_ID_SEQUENCE_UNIQUE_COLUMNS =
      new String[] {"pattern_id", "priority", "center_id", "account_group"};

  /**
   * Instantiates a new claim id sequence repository.
   */
  public ClaimIdSequenceRepository() {
    super("hosp_claim_seq_prefs", "claim_seq_id", "CID", CLAIM_ID_SEQUENCE_UNIQUE_COLUMNS);
  }
}
