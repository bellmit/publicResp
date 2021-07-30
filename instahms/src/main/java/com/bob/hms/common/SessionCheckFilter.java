package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;
import com.insta.hms.sso.SSOHelper;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class SessionCheckFilter.
 */
public class SessionCheckFilter implements javax.servlet.Filter {

  static Logger logger = LoggerFactory.getLogger(SessionCheckFilter.class);

  private static final String loginPage = "loginForm.do";
  private static final String homePage = "home.do";

  private FilterConfig config = null;

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
    this.config = config;
    logger.debug("Inside Init Method of SessionCheckFilter Class");
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

    HttpSession ses = RequestContext.getSession();
    HttpServletRequest req = (HttpServletRequest) request;
    boolean ssoEnabled = request.getServletContext()
        .getFilterRegistration("SpnegoHttpFilter") != null;
    req.setAttribute("ssoEnabled", ssoEnabled);
    // refer BUG 25752 : for why we checked for hospital id also.
    logger.debug("Remote User (Windows AD/LDAP User) => " + req.getRemoteUser());
    if (ses.getAttribute("userid") != null && ses.getAttribute("sesHospitalId") != null) {
      Cache instaLicenseCache = CacheManager.getInstance().getCache("insta_license");
      Map<String, Object> licenseData = null;
      if (instaLicenseCache != null) {
        Element mapElement = instaLicenseCache.get("license_" + ses.getAttribute("sesHospitalId"));
        if (mapElement != null) {
          licenseData = (Map<String, Object>) mapElement.getObjectValue();
        }
      }
      if (licenseData == null) {
        RedisTemplate redisTemplate = (RedisTemplate) ApplicationContextProvider
            .getApplicationContext().getBean("redisTemplate");

        licenseData = redisTemplate.opsForHash()
            .entries("license_" + ses.getAttribute("sesHospitalId"));
        if (licenseData.isEmpty()) {
          licenseData = null;
        }
      }
      Boolean blockAccess = licenseData != null && licenseData.get("block").equals(true)
          && licenseData.get("active").equals(false);
      BasicDynaBean userBean = null;
      request.setAttribute("license_data", licenseData);
      try {
        userBean = new GenericDAO("u_user").findByKey("emp_username", ses.getAttribute("userid"));
      } catch (SQLException se) {
        throw new ServletException(se);
      }
      IpEmrFormService ipEmrSvc = ApplicationContextProvider.getBean(IpEmrFormService.class);
      if (blockAccess) {
        ipEmrSvc.deleteSectionLock((String) ses.getAttribute("userid"));
        ses.invalidate();
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_PAYMENT_REQUIRED);
        return;
      }
      if (!ses.getId().equals(userBean.get("login_handle"))) {
        ipEmrSvc.deleteSectionLock((String) ses.getAttribute("userid"));
        ses.invalidate();
        sendResponse(req, (HttpServletResponse) response);
        return;
      }
      logger.debug("User ID exists in the session, nothing to do");
      // user is already logged in, nothing to do, continue the chain
      chain.doFilter(request, response);
      return;
    }

    StringBuffer origUrl = req.getRequestURL();
    logger.debug("In SessionCheck filter, URL is: " + origUrl);

    // This is an open API hit by pine labs after a transaction is complete.
    // smsInbound is the url used to receive sms response.
    if (origUrl.toString().contains("/pinelabs/transactionresult.json")
        || origUrl.toString().contains("/instasms/smsInbound/")) {
      chain.doFilter(request, response);
      return;
    }

    int slashPosition = origUrl.lastIndexOf("/");
    String lastPath = origUrl.substring(slashPosition, origUrl.length());
    String ssoOnlyParameter = ses.getServletContext().getInitParameter("ssoLoginOnly");
    boolean ssoOnlyMode = ssoOnlyParameter != null && ssoOnlyParameter.equalsIgnoreCase("true");
    boolean ssoOnly = ssoEnabled && ssoOnlyMode;
    if ((!ssoOnly && (lastPath.startsWith("/login.do") || lastPath.startsWith("/loginForm.do")))
        || lastPath.startsWith("/logout.do") || lastPath.contains("DiagnosticReportsDocket")
        || lastPath.contains("patientReportAccessFailure")) {
      ses.removeAttribute("login_status");
      logger.debug(
          "No User ID exists, but is a login/logout/PatientReportAccess request, allow to chain: "
              + lastPath);

      String hashFragment = req.getParameter("hashFragment");
      if (null != hashFragment && !(hashFragment.isEmpty())) {
        ses.setAttribute("hashFragment", hashFragment);
      }
      chain.doFilter(request, response);
      return;
    }
    if (!ssoOnly && lastPath.startsWith("/ChangePassword.do")) {
      chain.doFilter(request, response);
      return;
    }
    /*
     * Special pass-through for signals from localhost only
     */
    if (lastPath.startsWith("/signal.do")) {
      // check both IPv4 as well as IPv6 address formats
      if (req.getRemoteAddr().equals("127.0.0.1")
          || req.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
        ses.removeAttribute("login_status");
        chain.doFilter(request, response);
        return;
      } else {
        logger.warn(
            "Signal not allowed to execute, not called from localhost: " + req.getRemoteAddr());
      }
    }

    /*
     * Check if this a SSO session from the portal where the user has been authenticated earlier
     * (may be in a different session). Look if the designated parameter is in the request.
     */
    if (SSOHelper.isSSOAuthenticated((HttpServletRequest) request)) {
      ses.removeAttribute("login_status");
      chain.doFilter(request, response);
    }

    /*
     * All other URLs require that a valid session with a userid exists. If it comes here it means
     * that a valid user was not found AND the URL is not a login URL. Session must have expired, or
     * the user used bookmark after logging out.
     */
    // do not remember POSTs and other not-so-good URLs
    if (req.getMethod().equalsIgnoreCase("GET")
        && !lastPath.startsWith("/AccessControlForward.do")) {
      String queryString = (req.getQueryString());
      if (queryString != null) {
        origUrl.append("?" + queryString);
      }

      logger.debug("GET request, let us store the original URL as " + origUrl);
      ses.setAttribute("origUrl", origUrl.toString());
    }

    try {
      // if sso module is enabled, do not forward the request to login page.
      // redirect it to home or bookmarked url.

      String remoteUser = req.getRemoteUser();
      // String remoteUser = "InstaAdmin";
      if (null != remoteUser) {
        String defaultSchema = ses.getServletContext().getInitParameter("defaultSchema");
        ses.setAttribute("sesHospitalId", defaultSchema);
        // Pass lowercase usernames as windows ad users are case insensitive
        if (new LoginHelper().login(defaultSchema, remoteUser.toLowerCase(), req,
            (HttpServletResponse) response)) {
          String url = req.getContextPath() + "/" + homePage;
          String relOrigUrl = origUrl
              .substring(origUrl.indexOf(req.getContextPath()) + req.getContextPath().length());
          if (!origUrl.toString().equals("/") && !relOrigUrl.isEmpty() && !relOrigUrl.equals("/")) {
            ses.removeAttribute("origUrl");
            url = origUrl.toString(); // redirect to the bookmark and remove from session.
          }

          ((HttpServletResponse) response).sendRedirect(url);
          return;
        }
      }
    } catch (SQLException se) {
      logger.error("", se); // here we cant throw SQLException, so wrapping and sending it as
      // IOException
      throw new IOException(se);
    }

    logger.debug("Session expired, or bookmarked");
    ses.setAttribute("login_status", "Session has expired, please login again:");
    if (ssoOnly) {
      ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
    sendResponse(req, (HttpServletResponse) response);
  }

  /**
   * Send response.
   *
   * @param request  the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void sendResponse(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String uri = request.getRequestURI();
    logger.debug("uri = " + uri);
    if (uri.toLowerCase().endsWith(".json")) {
      (response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    } else if (uri.contains("/patient/")) { // check if user accessing patient portal
      // redirect to patient login page
      (response).sendRedirect(request.getContextPath() + "/patient/" + loginPage);
    } else if (uri.contains("/doctor/")) {
      // redirect to doctor login page
      (response).sendRedirect(request.getContextPath() + "/doctor/" + loginPage);
    } else {
      // redirect the user to hms login page.
      logger.debug("Sending redirect to: " + request.getContextPath() + "/" + loginPage);
      (response).sendRedirect(request.getContextPath() + "/" + loginPage);
    }

  }

}
