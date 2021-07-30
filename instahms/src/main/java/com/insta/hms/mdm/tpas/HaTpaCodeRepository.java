package com.insta.hms.mdm.tpas;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class HaTpaCodeRepository.
 */
@Repository
public class HaTpaCodeRepository extends GenericRepository {

  /**
   * Instantiates a new ha tpa code repository.
   */
  public HaTpaCodeRepository() {
    super("ha_tpa_code");
  }

}
