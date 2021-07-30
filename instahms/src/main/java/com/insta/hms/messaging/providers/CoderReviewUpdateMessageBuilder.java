package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The Class CoderReviewUpdateMessageBuilder.
 */

public class CoderReviewUpdateMessageBuilder extends MessageBuilder {

  /** The logger. */
  Logger logger = LoggerFactory.getLogger(CoderReviewUpdateMessageBuilder.class);

  /**
   * Instantiates a new coder review update message builder.
   */
  public CoderReviewUpdateMessageBuilder() {
    addDataProvider(new CoderReviewUpdateDataProvider());
  }
}
