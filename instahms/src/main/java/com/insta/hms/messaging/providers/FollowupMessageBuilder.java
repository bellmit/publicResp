package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FollowupMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(FollowupMessageBuilder.class);

  public FollowupMessageBuilder() {
    this.addDataProvider(new FollowupDataProvider());
  }
}
