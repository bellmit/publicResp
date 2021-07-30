package com.insta.hms.mdm.vaccinecategory;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Vaccine category service.
 */
@Repository
public class VaccineCategoryRepository extends MasterRepository<Integer> {

  public VaccineCategoryRepository() {
    super("vaccine_category_master", "vaccine_category_id", "vaccine_category_name");
  }
}