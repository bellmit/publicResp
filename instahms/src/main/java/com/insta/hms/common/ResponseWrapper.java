package com.insta.hms.common;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ResponseWrapper extends HttpServletResponseWrapper {

  private CharArrayWriter writer;

  /**
   * Constructs a response adaptor wrapping the given response.
   *
   * @param response Http Servlet Response
   * @throws IllegalArgumentException if the response is null
   */
  public ResponseWrapper(HttpServletResponse response) {
    super(response);
    writer = new CharArrayWriter();
  }

  public PrintWriter getWriter() {
    return new PrintWriter(writer);
  }

  public String toString() {
    return writer.toString();
  }
}
