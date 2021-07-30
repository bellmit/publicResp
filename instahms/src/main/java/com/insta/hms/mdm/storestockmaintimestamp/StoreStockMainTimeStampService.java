package com.insta.hms.mdm.storestockmaintimestamp;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class StoreStockMainTimeStampService.
 *
 * @author irshadmohammed
 */
@Service("storeStockMainTimeStampService")
public class StoreStockMainTimeStampService extends MasterService {

  /** The store stock main time stamp repository. */
  @LazyAutowired private StoreStockMainTimeStampRepository storeStockMainTimeStampRepository;

  /**
   * Instantiates a new store stock main time stamp service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public StoreStockMainTimeStampService(
      StoreStockMainTimeStampRepository repository, StoreStockMainTimeStampValidator validator) {
    super(repository, validator);
    this.storeStockMainTimeStampRepository = repository;
  }

  /**
   * Gets the medicine main timestamp.
   *
   * @return the medicine main timestamp
   */
  public int getMedicineMainTimestamp() {
    return storeStockMainTimeStampRepository.getMedicineTimestamp();
  }
}
