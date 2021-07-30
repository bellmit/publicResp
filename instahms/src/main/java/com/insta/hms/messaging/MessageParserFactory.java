package com.insta.hms.messaging;

import com.insta.hms.messaging.hl7.parser.Hl7Parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating MessageParser objects.
 */
public class MessageParserFactory {
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MessageParserFactory.class);



  /**
   * Gets the parser.
   *
   * @param parserType the parser type
   * @return the parser
   */
  public static MessageParser getParser(String parserType) {
    if (parserType.equalsIgnoreCase("HL7")) {
      return new Hl7Parser();
    }

    return null;
  }
}
