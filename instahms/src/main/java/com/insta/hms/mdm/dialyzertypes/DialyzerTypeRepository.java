package com.insta.hms.mdm.dialyzertypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DialyzerTypeRepository.
 */
@Repository
public class DialyzerTypeRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new dialyzer type repository.
   */
  public DialyzerTypeRepository() {
    super("dialyzer_types", "dialyzer_type_id", "dialyzer_type_name");
  }

}
