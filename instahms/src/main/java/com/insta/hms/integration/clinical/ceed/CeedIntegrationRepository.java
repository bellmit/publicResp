package com.insta.hms.integration.clinical.ceed;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class CeedIntegrationRepository.
 *
 * @author teja
 */
@Repository
public class CeedIntegrationRepository extends GenericRepository {

  /**
   * Instantiates a new ceed integration repository.
   */
  public CeedIntegrationRepository() {
    super("ceed_integration_main");
  }

}
