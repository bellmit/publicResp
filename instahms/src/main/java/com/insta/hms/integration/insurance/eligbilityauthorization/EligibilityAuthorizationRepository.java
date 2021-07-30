package com.insta.hms.integration.insurance.eligbilityauthorization;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class EligibilityAuthorizationRepository extends GenericRepository {

  public EligibilityAuthorizationRepository() {
    super("eligibility_authorization_status");
  }

}
