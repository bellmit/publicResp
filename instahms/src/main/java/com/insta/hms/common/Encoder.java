package com.insta.hms.common;

import org.owasp.esapi.errors.EncodingException;

/*
 * Wrapper around an Encoder to address XSS flaws.
 * Currently uses ESAPI encoder
 */

/**
 * The Class Encoder.
 */
public class Encoder {

  /*
   * public static String cleanHtmlAttribute(String value) { return
   * ESAPI.encoder().encodeForHTMLAttribute(value); }
   * 
   * public static String cleanHtml(String value) { return ESAPI.encoder().encodeForHTML(value); }
   * 
   * public static String cleanJavaScript(String value) { return
   * ESAPI.encoder().encodeForJavaScript(value); }
   * 
   * public static String cleanURL(String value) throws EncodingException { return
   * ESAPI.encoder().encodeForURL(value); }
   */

  /**
   * Clean html attribute.
   *
   * @param value the value
   * @return the string
   */
  public static String cleanHtmlAttribute(String value) {
    return org.owasp.encoder.Encode.forHtmlAttribute(value);
  }

  /**
   * Clean html.
   *
   * @param value the value
   * @return the string
   */
  public static String cleanHtml(String value) {
    return org.owasp.encoder.Encode.forHtml(value);
  }

  /**
   * Clean java script.
   *
   * @param value the value
   * @return the string
   */
  public static String cleanJavaScript(String value) {
    if (value != null) {
      return org.owasp.encoder.Encode.forJavaScript(value);
    } else {
      return null;
    }
  }

  /**
   * Clean SQL.
   *
   * @param value the value
   * @return the string
   */
  public static String cleanSQL(String value) {
    if (value != null) {
      return org.owasp.esapi.ESAPI.encoder().encodeForSQL(
          new org.owasp.esapi.codecs.MySQLCodec(org.owasp.esapi.codecs.MySQLCodec.Mode.ANSI),
          value);
    } else {
      return null;
    }
  }

  /**
   * Clean URL.
   *
   * @param value the value
   * @return the string
   * @throws EncodingException the encoding exception
   */
  public static String cleanURL(String value) throws EncodingException {
    return org.owasp.encoder.Encode.forUriComponent(value);
  }

}
