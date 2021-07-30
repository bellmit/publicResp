package com.insta.hms.mdm.prescriptionsdeclinedreasonmaster;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class ReviewTypesService.
 */
@Service
public class PrescDeclinedReasonService extends MasterService {

  /**
   * Instantiates a new review types service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * 
   */
  public PrescDeclinedReasonService(PrescDeclinedReasonRepository repository,
      PrescDeclinedReasonValidator validator) {
    super(repository, validator);
  }


}
