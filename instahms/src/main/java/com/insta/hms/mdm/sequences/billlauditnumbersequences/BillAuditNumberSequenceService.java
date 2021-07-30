package com.insta.hms.mdm.sequences.billlauditnumbersequences;

import com.insta.hms.mdm.sequences.SequenceMasterService;

import org.springframework.stereotype.Service;

/**
 * The Class BillAuditNumberSequenceService.
 */
@Service
public class BillAuditNumberSequenceService extends SequenceMasterService {

  /**
   * Instantiates a new bill audit number sequence service.
   *
   * @param repository the r
   * @param validator the v
   */
  public BillAuditNumberSequenceService(
      BillAuditNumberSequenceRepository repository, BillAuditNumberSequenceValidator validator) {
    super(repository, validator);
  }
}
