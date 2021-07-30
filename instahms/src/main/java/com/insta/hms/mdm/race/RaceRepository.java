package com.insta.hms.mdm.race;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RaceRepository extends MasterRepository<String> {

  /**
   * Instantiates a new race repository.
   */
  public RaceRepository() {
    super("race_master", "race_id", "race_name", new String[] { "race_id", "race_name" });
  }
}
