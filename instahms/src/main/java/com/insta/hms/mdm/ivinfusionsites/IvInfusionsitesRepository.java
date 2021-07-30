package com.insta.hms.mdm.ivinfusionsites;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class IvInfusionsitesRepository.
 */
@Repository
public class IvInfusionsitesRepository extends GenericRepository {

  /**
   * Instantiates a new iv infusionsites repository.
   */
  public IvInfusionsitesRepository() {
    super("iv_infusionsites");
  }

}
