package com.insta.hms.common.report;


import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CustomReportsRepository extends GenericRepository {

  /**
   * Instantiates a new CustomReportsRepository.
   *
   */
  public CustomReportsRepository() {
    super("custom_reports");
  }
}
