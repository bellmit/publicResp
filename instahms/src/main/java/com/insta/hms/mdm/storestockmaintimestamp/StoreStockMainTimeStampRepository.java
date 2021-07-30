package com.insta.hms.mdm.storestockmaintimestamp;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoreStockMainTimeStampRepository.
 *
 * @author irshadmohammed
 */
@Repository
public class StoreStockMainTimeStampRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new store stock main time stamp repository.
   */
  public StoreStockMainTimeStampRepository() {
    super("store_main_stock_timestamp", "medicine_timestamp");
  }

  /** The get medicine timestamp. */
  public static String GET_MEDICINE_TIMESTAMP = "SELECT * FROM store_main_stock_timestamp";

  /**
   * Gets the medicine timestamp.
   *
   * @return the medicine timestamp
   */
  public int getMedicineTimestamp() {
    return DatabaseHelper.getInteger(GET_MEDICINE_TIMESTAMP);
  }
}
