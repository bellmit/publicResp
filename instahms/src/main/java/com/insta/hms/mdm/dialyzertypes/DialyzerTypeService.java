package com.insta.hms.mdm.dialyzertypes;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class DialyzerTypeService.
 */
@Service
public class DialyzerTypeService extends MasterService {

  /**
   * Instantiates a new dialyzer type service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public DialyzerTypeService(DialyzerTypeRepository repository, DialyzerTypeValidator validator) {
    super(repository, validator);
  }

}