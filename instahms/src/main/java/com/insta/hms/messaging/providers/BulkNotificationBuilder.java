package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BulkNotificationBuilder.
 */
public class BulkNotificationBuilder extends MessageBuilder {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(BulkNotificationBuilder.class);

  /**
   * Instantiates a new bulk notification builder.
   */
  public BulkNotificationBuilder() {
    // addDataProvider(new PatientDataProvider());
    // addDataProvider(new DoctorDataProvider());
    addDataProvider(new UserDataProvider());
  }
}
