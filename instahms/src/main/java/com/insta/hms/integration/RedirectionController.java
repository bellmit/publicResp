package com.insta.hms.integration;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller contains endpoints which are exposed to third parties which redirects to internal
 * endpoints.
 *
 * @author Sairam
 */
@Controller
@RequestMapping(URLRoute.REDIRECTIONS_INTEGRATION)
public class RedirectionController extends AbstractRedirectionController {
  
  /** The Constant REGISTRATION_PATH_EXISITING_PATIENT. */
  private static final String REGISTRATION_PATH_EXISITING_PATIENT = "/patients/opregistration/"
      + "index.htm#/filter/default/patient/{mr_no}/registration/visit/new?"
      + "retain_route_params=true&";
  
  /** The Constant REGISTRATION_PATH_NEW_PATIENT. */
  private static final String REGISTRATION_PATH_NEW_PATIENT = "/patients/opregistration/"
      + "index.htm#/filter/default/patient/new/registration?retain_route_params=true&";

  /**
   * Redirects to the Registration page.
   *
   * @param req the req
   * @param resp the resp
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/patient/registration", method = RequestMethod.GET)
  public void redirectToRegistration(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String path = null;
    // Replace '#' character in query string with empty string. We don't support this now
    String queryString = (req.getQueryString() == null ? "" : req.getQueryString())
        .replace("%23", "").replace("#", "");
    if (req.getParameter("mr_no") != null) {
      path = REGISTRATION_PATH_EXISITING_PATIENT.replace("{mr_no}",
          URLEncoder.encode(req.getParameter("mr_no"), "UTF-8"));
      // remove mr_no from query string as it is already added to path
      queryString = queryString
          .replace("mr_no=" + URLEncoder.encode(req.getParameter("mr_no"), "UTF-8"), "")
          .replace("&&", "&");
    } else {
      path = REGISTRATION_PATH_NEW_PATIENT;
    }
    redirect(resp, path + queryString);
  }

}
