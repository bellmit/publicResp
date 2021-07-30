package com.insta.hms.messaging.hl7.providers;

import com.insta.hms.messaging.MessageBuilder;

/**
 * The Class HL7ADT04Builder.
 * 
 * @author yashwant
 */
public class HL7ADT04Builder extends MessageBuilder {

  /**
   * Instantiates a new HL 7 ADT 04 builder.
   */
  public HL7ADT04Builder() {
    this.addDataProvider(new HL7ADT04Provider());
  }
}
