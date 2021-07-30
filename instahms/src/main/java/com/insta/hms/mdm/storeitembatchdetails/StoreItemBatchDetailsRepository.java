package com.insta.hms.mdm.storeitembatchdetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class StoreItemBatchDetailsRepository.
 *
 * @author Amol
 */
@Repository
public class StoreItemBatchDetailsRepository extends MasterRepository<Integer> {

  /** Instantiates a new store item batch details repository. */
  public StoreItemBatchDetailsRepository() {
    super("store_item_batch_details", "item_batch_id");
  }
  
  private static final String EXP_CHECK_QUERY = "select * from store_item_batch_details "
      + " where batch_no = ? and medicine_id = ? and exp_dt >= current_date ";

  public boolean isItemExpired(String batchNo, int medicineId) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(EXP_CHECK_QUERY, batchNo, medicineId);
    return null == bean;
  }
}
