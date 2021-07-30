package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BillCancellationMessageBuilder.
 *
 * @author nikunj.s
 */
public class BillCancellationMessageBuilder extends MessageBuilder {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(BillCancellationMessageBuilder.class);

  /**
   * Instantiates a new bill cancellation message builder.
   */
  public BillCancellationMessageBuilder() {
    this.addDataProvider(new BillCancellationDataProvider());
  }
}
