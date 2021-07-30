package com.insta.hms.mdm.storeitemissuerates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoreItemIssueRateRepository.
 *
 * @author amolbagde
 */
@Repository
public class StoreItemIssueRateRepository extends MasterRepository<Integer> {

  /** Instantiates a new store item issue rate repository. */
  public StoreItemIssueRateRepository() {
    super("store_item_issue_rates", "medicine_id");
  }
}
