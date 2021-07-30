package com.insta.hms.mdm.sequences.visitidsequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class VisitIdSequenceService.
 */
@Service
public class VisitIdSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new visit id sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public VisitIdSequenceService(VisitIdSequenceRepository repository, 
                                VisitIdSequenceValidator validator) {
    super(repository, validator);
  }
}
