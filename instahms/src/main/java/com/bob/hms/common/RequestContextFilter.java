package com.bob.hms.common;

import org.apache.struts.util.MessageResources;
import org.slf4j.MDC;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The Class RequestContextFilter.
 */
public class RequestContextFilter implements javax.servlet.Filter {
  private FilterConfig config = null;

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
    this.config = config;
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
   * @see javax.servlet.Filter#doFilter (javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      /*
       * Save our request in a global variable (static method of RequestContext
       */
      RequestContext.setRequest(request);

      /*
       * Initialize an MDC so that the logger knows our session details, and print it out in the
       * logs. See API doc of log4j.MDC, our log4j.properties, as well as
       * http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html
       */
      HttpSession session = ((HttpServletRequest) request).getSession(false);

      if (session != null) {
        String schema = (String) session.getAttribute("sesHospitalId");
        String user = (String) session.getAttribute("userid");
        if (session.getAttribute("applicationVersion") == null) {
          MessageResources resource = MessageResources
              .getMessageResources("java.resources.application");
          session.setAttribute("applicationVersion", resource.getMessage("insta.software.version"));
        }
        if (schema != null) {
          MDC.put("schema", schema);
        } else {
          MDC.remove("schema");
        }
        if (user != null) {
          MDC.put("username", user);
        } else {
          MDC.remove("username");
        }
      } else {
        MDC.remove("schema");
        MDC.remove("username");
      }

      /*
       * Do normal processing after this
       */
      chain.doFilter(request, response);
    } finally {

      /*
       * Refer https://bz.apache.org/bugzilla/show_bug.cgi?id=50486. The following lines need to be
       * added to address the memory leak in log4j
       */
      MDC.clear();

      /*
       * Clean up all the connections on the way back
       */
      RequestContext.cleanupConnections();
    }
  }
}