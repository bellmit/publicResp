package com.insta.hms.mdm.sequences.radiologynumbersequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class RadiologyNumberSequenceService.
 */
@Service
public class RadiologyNumberSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new radiology number sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public RadiologyNumberSequenceService(
      RadiologyNumberSequenceRepository repository, RadiologyNumberSequenceValidator validator) {
    super(repository, validator);
  }
}
