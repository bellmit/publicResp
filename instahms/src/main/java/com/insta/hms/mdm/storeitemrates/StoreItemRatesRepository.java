package com.insta.hms.mdm.storeitemrates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoreItemRatesRepository.
 */
@Repository
public class StoreItemRatesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new store item rates repository.
   */
  public StoreItemRatesRepository() {
    super("store_item_rates", "store_item_rates_id");
  }
}
