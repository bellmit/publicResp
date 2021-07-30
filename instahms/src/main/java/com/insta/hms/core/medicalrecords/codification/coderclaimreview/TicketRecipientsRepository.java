package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class TicketRecipientsRepository.
 */
@Repository
public class TicketRecipientsRepository extends GenericRepository {

  /**
   * Instantiates a new ticket recipients repository.
   */
  public TicketRecipientsRepository() {
    super("review_recipients");
  }

}
