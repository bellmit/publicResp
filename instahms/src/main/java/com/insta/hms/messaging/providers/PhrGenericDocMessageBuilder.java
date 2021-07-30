package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhrGenericDocMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PhrGenericDocMessageBuilder.class);

  public PhrGenericDocMessageBuilder() {
    this.addDataProvider(new PhrGenericDocDataProvider());
  }
}
