package com.insta.hms.mdm.sequences.receiptnumber;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class ReceiptNumberSequenceService.
 */
@Service
public class ReceiptNumberSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new receipt number sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public ReceiptNumberSequenceService(
      ReceiptNumberSequenceRepository repository, ReceiptNumberSequenceValidator validator) {
    super(repository, validator);
  }
}
