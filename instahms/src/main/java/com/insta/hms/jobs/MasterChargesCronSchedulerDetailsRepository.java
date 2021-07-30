package com.insta.hms.jobs;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class MasterChargeCronSchedulerDetailsRepository.
 */
@Repository
public class MasterChargesCronSchedulerDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new master charge cron repository.
   */
  public MasterChargesCronSchedulerDetailsRepository() {
    super("master_charges_cron_scheduler_details");
  }
}