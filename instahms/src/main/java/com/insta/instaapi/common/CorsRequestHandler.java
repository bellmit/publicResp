package com.insta.instaapi.common;

import com.bob.hms.common.RequestContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsRequestHandler implements javax.servlet.Filter {
  private FilterConfig config = null;

  public void init(FilterConfig config) throws ServletException {
    this.config = config;
  }

  public void destroy() {
  }

  /**
   * pass request ad response through this filter.
   * 
   * @param request   Request object
   * @param response  Response object
   * @param chain     Filter chain
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    /*
     * Save our request in a global variable (static method of RequestContext
     */
    RequestContext.setRequest(request);

    String source = ((HttpServletRequest) request).getHeader("origin");
    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", source);
    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Credentials", "true");

    /*
     * Do normal processing after this
     */
    chain.doFilter(request, response);

    /*
     * Clean up all the connections on the way back
     */
    RequestContext.cleanupConnections();
  }
}