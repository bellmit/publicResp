package com.insta.hms.mdm.storeitemlotdetails;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoreItemLotDetailsRepository.
 *
 * @author Amol
 */

@Repository
class StoreItemLotDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new store item lot details repository.
   */
  public StoreItemLotDetailsRepository() {
    super("store_item_lot_details", "item_lot_id");
  }
}
