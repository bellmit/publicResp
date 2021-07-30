package com.insta.hms.core.inventory.stock.adjustment;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StoreAdjustmentDetailsRepository extends GenericRepository {
  StoreAdjustmentDetailsRepository() {
    super("store_adj_details");
  }

}
