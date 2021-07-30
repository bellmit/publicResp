package com.insta.hms.mdm.sequences.labnumbersequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class LabNumberSequenceService.
 */
@Service
public class LabNumberSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new lab number sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public LabNumberSequenceService(LabNumberSequenceRepository repository, 
                                  LabNumberSequenceValidator validator) {
    super(repository, validator);
  }
}
