package com.insta.hms.mdm.storeitemcontroltypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoreItemControlTypeRepository.
 */
@Repository
public class StoreItemControlTypeRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new store item control type repository.
   */
  public StoreItemControlTypeRepository() {
    super("store_item_controltype", "control_type_id", "control_type_name");
    setStatusField(null);
  }

}
