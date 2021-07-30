package com.insta.hms.messaging.hl7.providers;

import com.insta.hms.messaging.MessageBuilder;

/**
 * The Class HL7ADT18Builder.
 * 
 * @author yashwant
 */
public class HL7ADT18Builder extends MessageBuilder {

  /**
   * Instantiates a new HL 7 ADT 18 builder.
   */
  public HL7ADT18Builder() {
    this.addDataProvider(new HL7ADT18Provider());
  }
}
