package com.insta.hms.mdm.gendermaster;

import com.insta.hms.mdm.MasterService;
import org.springframework.stereotype.Service;

@Service
public class GenderService extends MasterService {

  /**
   * Instantiates a new master service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public GenderService(GenderRepository repository,
      GenderValidator validator) {
    super(repository, validator);
  }
}
