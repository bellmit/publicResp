package com.insta.hms.mdm.stockadjustmentreason;
/*
 * Owner : Ashok Pal, 7th Aug 2017
 */

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class StockAdjustmentReasonRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new stock adjustment reason repository.
   */
  public StockAdjustmentReasonRepository() {
    super("stock_adjustment_reason_master", "adjustment_reason_id", "adjustment_reason");
    // TODO Auto-generated constructor stub
  }
}
