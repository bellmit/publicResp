package com.insta.hms.mdm.storescontractor;

/*
 * Owner : Ashok Pal, 5th April 2017
 */
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class ContractorRepository.
 */
@Repository
public class ContractorRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new contractor repository.
   */
  public ContractorRepository() {
    super("contractor_master", "contractor_id", "contractor_name");
  }

}
