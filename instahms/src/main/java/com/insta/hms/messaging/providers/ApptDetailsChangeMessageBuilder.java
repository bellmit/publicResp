package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ApptDetailsChangeMessageBuilder.
 *
 * @author Anil N
 */
public class ApptDetailsChangeMessageBuilder extends MessageBuilder {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ApptDetailsChangeMessageBuilder.class);

  /**
   * Instantiates a new appt details change message builder.
   */
  public ApptDetailsChangeMessageBuilder() {
    this.addDataProvider(new ApptDetailsChangeDataProvider());
  }
}
