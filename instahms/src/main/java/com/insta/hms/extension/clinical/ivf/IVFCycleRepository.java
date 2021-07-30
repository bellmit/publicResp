package com.insta.hms.extension.clinical.ivf;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class IVFCycleRepository.
 */
@Repository
public class IVFCycleRepository extends GenericRepository {

  /**
   * Instantiates a new IVF cycle repository.
   */
  public IVFCycleRepository() {
    super("ivf_cycle");
  }

}
