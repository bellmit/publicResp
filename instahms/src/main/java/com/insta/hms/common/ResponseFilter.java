package com.insta.hms.common;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Added Response Filter to modify html content for removing extra tabs
 * and whitespaces for performant page load time.
 */
public class ResponseFilter implements Filter {
  @Override public void init(FilterConfig filterConfig) throws ServletException { }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    ResponseWrapper wrapper = new ResponseWrapper((HttpServletResponse) response);
    PrintWriter writer = response.getWriter();
    if (wrapper != null && wrapper.getContentType() != null && wrapper.getContentType()
        .contains("text/html")) {
      CharArrayWriter charArrayWriter = new CharArrayWriter();
      String originalContent = wrapper.toString();
      //Remove 2(or more) spaces with one space (replaces \n \t \r \s)
      originalContent = originalContent.replaceAll("[\\r\\n]+","\n");
      originalContent = originalContent.replaceAll(">(\\s{2,})<","> <");
      charArrayWriter.write(originalContent);
      writer.write(charArrayWriter.toString());
      chain.doFilter(request, wrapper);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override public void destroy() { }
}
