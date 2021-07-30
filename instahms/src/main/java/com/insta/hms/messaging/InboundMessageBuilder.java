package com.insta.hms.messaging;

import com.insta.hms.messaging.processor.MessageProcessor;

import java.util.HashMap;
import java.util.Map;

public class InboundMessageBuilder {

  /** The provider data map. */
  private Map<String, Object> processorDataMap = new HashMap<>();

  /** The data providers. */
  private MessageProcessor messageProcessor;

  /**
   * Adds the processor.
   *
   * @param processor the processor
   */
  public void addProcessor(MessageProcessor processor) {
    messageProcessor = processor;
  }

  /**
   * Builds the.
   *
   * @param msg the msg
   */
  public void build(Message msg) {
    processorDataMap = messageProcessor.parse(msg);
  }

  /**
   * Process.
   *
   * @return true, if successful
   */
  public boolean process() {
    if (processorDataMap != null && !processorDataMap.isEmpty()) {
      return messageProcessor.process(processorDataMap);
    } else {
      return false;
    }

  }

}
