package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreGrnTaxDetailsRepository extends GenericRepository {

  public StoreGrnTaxDetailsRepository() {
    super("store_grn_tax_details");
  }

}
