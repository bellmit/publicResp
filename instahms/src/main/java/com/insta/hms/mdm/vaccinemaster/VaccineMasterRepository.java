package com.insta.hms.mdm.vaccinemaster;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Vaccine master repository.
 */
@Repository
public class VaccineMasterRepository extends MasterRepository<Integer> {

  public VaccineMasterRepository() {
    super("vaccine_master", "vaccine_id");
  }
}