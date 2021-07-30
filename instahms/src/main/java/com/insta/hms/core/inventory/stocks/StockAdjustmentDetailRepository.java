package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StockAdjustmentDetailRepository extends GenericRepository {

  public StockAdjustmentDetailRepository() {
    super("store_adj_details");
  }

}
