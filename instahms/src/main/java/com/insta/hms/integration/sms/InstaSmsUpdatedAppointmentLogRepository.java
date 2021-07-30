package com.insta.hms.integration.sms;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class InstaSmsUpdatedAppointmentLogRepository.
 */
@Repository
public class InstaSmsUpdatedAppointmentLogRepository extends GenericRepository {
  /**
   * Instantiates a new insta sms log repository.
   */
  public InstaSmsUpdatedAppointmentLogRepository() {
    super("received_message_status_update_log");
  }
}
