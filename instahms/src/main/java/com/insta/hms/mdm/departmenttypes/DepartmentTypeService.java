package com.insta.hms.mdm.departmenttypes;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class DepartmentTypeService.
 */
@Service
public class DepartmentTypeService extends MasterService {

  /**
   * Instantiates a new department type service.
   *
   * @param repository
   *          DepartmentTypeRepository
   * @param validator
   *          DepartmentTypeValidator
   */
  public DepartmentTypeService(DepartmentTypeRepository repository,
      DepartmentTypeValidator validator) {
    super(repository, validator);
  }
}
