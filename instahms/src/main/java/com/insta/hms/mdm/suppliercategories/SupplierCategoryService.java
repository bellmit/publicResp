package com.insta.hms.mdm.suppliercategories;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class SupplierCategoryService.
 */
@Service
public class SupplierCategoryService extends MasterService {

  /** The supplier category repositry. */
  SupplierCategoryRepository supplierCategoryRepositry;

  /**
   * Instantiates a new supplier category service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public SupplierCategoryService(SupplierCategoryRepository repository,
      SupplierCategoryValidator validator) {
    super(repository, validator);
    supplierCategoryRepositry = repository;
  }

}
