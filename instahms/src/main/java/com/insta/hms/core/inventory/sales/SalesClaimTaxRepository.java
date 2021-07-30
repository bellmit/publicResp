package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class SalesClaimTaxRepository extends GenericRepository{

  public SalesClaimTaxRepository() {
    super("sales_claim_tax_details");
    // TODO Auto-generated constructor stub
  }

}
