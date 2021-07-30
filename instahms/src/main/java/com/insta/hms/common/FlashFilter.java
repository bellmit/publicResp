package com.insta.hms.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class FlashFilter.
 */
public class FlashFilter implements Filter {

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
   * javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;

    String prgkey = request.getParameter(FlashScope.FLASH_KEY);
    String key = null;
    if (prgkey != null) {
      key = prgkey;
    }

    if (key != null) {
      FlashScope scope = FlashScope.getScope(req, key);
      if (scope != null) {
        scope.copyToRequest(req);
      }
    }

    // cleanup expired scopes
    FlashScope.cleanup(req);

    HttpServletResponse resp = (HttpServletResponse) response;
    chain.doFilter(req, resp);
  }

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
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
  }

}
