package com.insta.hms.eservice;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * The Interface EResultParser.
 */
public interface EResultParser {

  /**
   * Parses the.
   *
   * @param xml the xml
   * @return the e result
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  EResult parse(String xml) throws IOException, SAXException;

}
