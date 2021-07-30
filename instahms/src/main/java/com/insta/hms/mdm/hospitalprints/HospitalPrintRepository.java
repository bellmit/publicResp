package com.insta.hms.mdm.hospitalprints;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class HospitalPrintRepository.
 */
@Repository
public class HospitalPrintRepository extends GenericRepository {

  /**
   * Instantiates a new hospital print repository.
   */
  public HospitalPrintRepository() {
    super("hosp_print_master");
  }
}
