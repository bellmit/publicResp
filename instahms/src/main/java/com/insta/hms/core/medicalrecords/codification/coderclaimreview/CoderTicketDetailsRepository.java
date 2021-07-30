package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CoderTicketDetailsRepository extends GenericRepository {

  public CoderTicketDetailsRepository() {
    super("review_details");
  }

}
