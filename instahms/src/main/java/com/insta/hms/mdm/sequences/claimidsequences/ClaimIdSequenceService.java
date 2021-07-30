package com.insta.hms.mdm.sequences.claimidsequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class ClaimIdSequenceService.
 */
@Service
public class ClaimIdSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new claim id sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public ClaimIdSequenceService(ClaimIdSequenceRepository repository, 
                    ClaimIdSequenceValidator validator) {
    super(repository, validator);
  }
}
