package com.insta.hms.mdm.religionmaster;

import com.insta.hms.mdm.MasterService;
import org.springframework.stereotype.Service;

@Service
public class ReligionService extends MasterService {

  /**
   * Instantiates a new master service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public ReligionService(ReligionRepository repository,
      ReligionValidator validator) {
    super(repository, validator);
  }
}
