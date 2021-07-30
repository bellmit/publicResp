package com.insta.hms.mdm.dialysisaccesstypes;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class DialysisAccessTypeService.
 */
@Service
public class DialysisAccessTypeService extends MasterService {

  /**
   * Instantiates a new dialysis access type service.
   *
   * @param dialysisAccessTypesRepository the dialysis access types repository
   * @param dialysisAccessTypesValidator the dialysis access types validator
   */
  public DialysisAccessTypeService(DialysisAccessTypeRepository dialysisAccessTypesRepository,
      DialysisAccessTypeValidator dialysisAccessTypesValidator) {
    super(dialysisAccessTypesRepository, dialysisAccessTypesValidator);
  }

}
