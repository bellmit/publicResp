package com.insta.hms.mdm.storetypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;


@Repository
class StoreTypeRepository extends MasterRepository<Integer> {

  public StoreTypeRepository() {
    super("store_type_master", "store_type_id", "store_type_name");
  }

}
