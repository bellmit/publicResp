package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BulkMessageBuilder.
 */
public class BulkMessageBuilder extends MessageBuilder {
  
  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(BulkMessageBuilder.class);

  /**
   * Instantiates a new bulk message builder.
   */
  public BulkMessageBuilder() {
    addDataProvider(new PatientDataProvider());
    addDataProvider(new PatientListDataProvider());
    addDataProvider(new DoctorDataProvider());
    addDataProvider(new UserDataProvider());

  }
}
