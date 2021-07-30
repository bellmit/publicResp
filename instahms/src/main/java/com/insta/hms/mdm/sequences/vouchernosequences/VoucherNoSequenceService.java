package com.insta.hms.mdm.sequences.vouchernosequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class VoucherNoSequenceService.
 */
@Service
public class VoucherNoSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new voucher no sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public VoucherNoSequenceService(VoucherNoSequenceRepository repository, 
                                   VoucherNoSequenceValidator validator) {
    super(repository, validator);
  }
}
