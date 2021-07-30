package com.insta.hms.mdm.strengthunits;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class StrengthUnitRepository.
 *
 * @author sainathbatthala The repository for Strength Unit Master
 */
@Repository
public class StrengthUnitRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new strength unit repository.
   */
  public StrengthUnitRepository() {
    super("strength_units", "unit_id", "unit_name");
  }

}
