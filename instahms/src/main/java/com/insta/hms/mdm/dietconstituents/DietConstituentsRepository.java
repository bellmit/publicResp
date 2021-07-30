package com.insta.hms.mdm.dietconstituents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class DietConstituentsRepository extends GenericRepository {

  /**
   * Instantiates a new diet constituents repository.
   */
  public DietConstituentsRepository() {
    super("diet_constituents");
  }

}
