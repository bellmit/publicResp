package com.insta.hms.mdm.maritalstatusmaster;

import com.insta.hms.mdm.MasterService;
import org.springframework.stereotype.Service;

@Service
public class MaritalStatusService extends MasterService {

  /**
   * Instantiates a new master service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public MaritalStatusService(MaritalStatusRepository repository,
      MaritalStatusValidator validator) {
    super(repository, validator);
  }
}
