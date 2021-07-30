package com.insta.hms.core.inventory.stock.adjustment;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreAdjustmentMainRepository extends GenericRepository {

  StoreAdjustmentMainRepository() {
    super("store_adj_main");
  }
}
