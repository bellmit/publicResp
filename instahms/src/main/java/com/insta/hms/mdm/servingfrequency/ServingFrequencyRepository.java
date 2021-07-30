package com.insta.hms.mdm.servingfrequency;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class ServingFrequencyRepository.
 */
@Repository
public class ServingFrequencyRepository extends GenericRepository {

  /**
   * Instantiates a new serving frequency repository.
   */
  public ServingFrequencyRepository() {
    super("serving_frequency_master");
  }

}
