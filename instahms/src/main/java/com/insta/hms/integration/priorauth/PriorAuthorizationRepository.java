package com.insta.hms.integration.priorauth;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class PriorAuthorizationRepository.
 */
@Repository
public class PriorAuthorizationRepository extends GenericRepository {

  /**
   * Instantiates a new prior authorization repository.
   */
  public PriorAuthorizationRepository() {
    super("prior_auth_modes");
  }

}
