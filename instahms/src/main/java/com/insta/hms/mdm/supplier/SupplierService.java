package com.insta.hms.mdm.supplier;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class SupplierService.
 *
 * @author irshadmohammed
 */
@Service
public class SupplierService extends MasterService {

  /**
   * Instantiates a new supplier service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public SupplierService(SupplierRepository repository, SupplierValidator validator) {
    super(repository, validator);
  }

}
