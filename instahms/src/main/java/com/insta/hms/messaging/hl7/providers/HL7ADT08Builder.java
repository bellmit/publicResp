package com.insta.hms.messaging.hl7.providers;

import com.insta.hms.messaging.MessageBuilder;

/**
 * The Class HL7ADT08Builder.
 * 
 * @author yashwant
 */
public class HL7ADT08Builder extends MessageBuilder {

  /**
   * Instantiates a new HL 7 ADT 08 builder.
   */
  public HL7ADT08Builder() {
    this.addDataProvider(new HL7ADT08Provider());
  }
}
