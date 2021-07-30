package com.insta.hms.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class InboundMessageBuilderFactory {
  public static final Logger logger = LoggerFactory.getLogger(InboundMessageBuilderFactory.class);
  /** The Constant builderMap. */
  private static final Map<String, String> builderMap = new HashMap<>();

  /** The Constant defaultBuilderMap. */
  private static final Map<String, String> defaultBuilderMap = new HashMap<>();

  static {
    builderMap.put("orm_lab", "com.insta.hms.messaging.hl7.providers.OrmLabInboundMessageBuilder");
    builderMap.put("ORU_R01", "com.insta.hms.messaging.hl7.providers.ORULabInboundMessageBuilder");
  }

  /**
   * Gets the builder.
   *
   * @param messageType the message type
   * @return the builder
   */
  public static InboundMessageBuilder getBuilder(String messageType) {
    String builderClass = builderMap.get(messageType);
    InboundMessageBuilder builder = null;
    try {
      Class<?> cls = Class.forName(builderClass);
      builder = (InboundMessageBuilder) cls.newInstance();
    } catch (ClassNotFoundException cnfe) {
      logger.error("ClassNotFoundException in messageBuilderFactory", cnfe);
    } catch (InstantiationException ie) {
      logger.error("InstantiationException in messageBuilderFactory", ie);
    } catch (IllegalAccessException iae) {
      logger.error("IllegalAccessException in messageBuilderFactory", iae);
    }
    return builder;
  }
}
