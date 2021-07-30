package com.insta.hms.mdm.storesrateplanmaster;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StoresRatePlanRepository.
 *
 * @author yashwant
 */

@Repository
class StoresRatePlanRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new stores rate plan repository.
   */
  public StoresRatePlanRepository() {
    super("store_rate_plans", "store_rate_plan_id", "store_rate_plan_name");
  }

}
