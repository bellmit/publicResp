package com.insta.hms.mdm.supplier;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class SupplierRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class SupplierRepository extends MasterRepository<String> {

  /**
   * Instantiates a new supplier repository.
   */
  public SupplierRepository() {
    super("supplier_master", "supplier_code", "supplier_name");
  }
}
