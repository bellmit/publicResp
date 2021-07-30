package com.insta.hms.mdm.dentalsuppliers;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class {@link DentalSupplierRepository}.
 * @author amolbagde
 *
 */
@Repository
public class DentalSupplierRepository extends MasterRepository<Integer> {

  public DentalSupplierRepository() {
    super("dental_supplier_master", "supplier_id", "supplier_name");
  }
}
