package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PatientNextDayAppointmentMessageBuilder.
 *
 * @author mohammed.r
 */

public class PatientNextDayAppointmentMessageBuilder extends MessageBuilder {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(PatientNextDayAppointmentMessageBuilder.class);

  /**
   * Instantiates a new patient next day appointment message builder.
   */
  public PatientNextDayAppointmentMessageBuilder() {
    this.addDataProvider(new PatientNextDayAppointmentDataProvider());
  }
}
