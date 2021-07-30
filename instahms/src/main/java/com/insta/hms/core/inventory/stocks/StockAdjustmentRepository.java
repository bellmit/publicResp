package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StockAdjustmentRepository extends GenericRepository {
  
  private static final String GET_NEXT_SEQ_QUERY = "SELECT "
      + " nextval('stockadjust_sequence')";

  public StockAdjustmentRepository() {
    super("store_adj_main");
  }

  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GET_NEXT_SEQ_QUERY);
  }
}
