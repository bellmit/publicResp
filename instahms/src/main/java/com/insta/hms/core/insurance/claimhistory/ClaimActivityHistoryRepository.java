package com.insta.hms.core.insurance.claimhistory;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ClaimActivityHistoryRepository extends GenericRepository {

  public ClaimActivityHistoryRepository() {
    super("claim_activity_history");
  }

}
