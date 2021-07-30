package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreConsignmentInvoiceRepository extends GenericRepository {

  public StoreConsignmentInvoiceRepository() {
    super("store_consignment_invoice");
  }

}
