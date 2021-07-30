package com.insta.hms.mdm.sequences.billsequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class BillSequenceService.
 */
@Service
public class BillSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new bill sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public BillSequenceService(BillSequenceRepository repository, BillSequenceValidator validator) {
    super(repository, validator);
  }
}
