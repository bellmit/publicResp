package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreGatePassRepository extends GenericRepository {

  public StoreGatePassRepository() {
    super("store_gatepass");
  }

}
