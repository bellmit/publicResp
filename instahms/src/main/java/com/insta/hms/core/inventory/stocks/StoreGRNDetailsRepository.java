package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

@Repository
public class StoreGRNDetailsRepository extends GenericRepository {

  public StoreGRNDetailsRepository() {
    super("store_grn_details");
  }

  private static final String UPDATE_GRN_QUANTITY = "update store_grn_details set issue_qty=issue_qty+? where grn_no=? and medicine_id=? and batch_no=?";

  public Integer updateQuantity(String grnNo, int itemId, String batchNo, BigDecimal quantity) {
    return DatabaseHelper.update(UPDATE_GRN_QUANTITY, quantity, grnNo, itemId, batchNo);
  }
}
