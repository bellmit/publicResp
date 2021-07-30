package com.insta.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * The Class NewRelicIgnoreTransactionFilter.
 */
public class NewRelicIgnoreTransactionFilter implements Filter {

  /** The config. */
  private FilterConfig config = null;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(NewRelicIgnoreTransactionFilter.class);

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter( javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    req.setAttribute("com.newrelic.agent.IGNORE", true);

    logger.debug(req.getRequestURI());

    chain.doFilter(req, response);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    this.config = filterConfig;
  }

}
