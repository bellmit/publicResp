package com.insta.hms.integration.sms;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class InstaSmsRepository.
 */
@Repository
public class InstaSmsRepository extends GenericRepository {

  /**
   * Instantiates a new insta sms repository.
   */
  public InstaSmsRepository() {
    super("received_message");
  }

}
