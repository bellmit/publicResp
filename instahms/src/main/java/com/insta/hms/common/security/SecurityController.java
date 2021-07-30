package com.insta.hms.common.security;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.URLRoute;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The Class SecurityController to expose access control and other security attributes in session.
 * 
 * @author tanmay.k
 */
@RestController("securityController")
@RequestMapping(URLRoute.SECURITY_URL)
public class SecurityController extends BaseRestController {

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /**
   * Lists the security attributes.
   *
   * @return the map of security attributes.
   */
  @IgnoreConfidentialFilters
  @GetMapping
  public Map<String, Object> list() {
    return securityService.getSecurityAttributes();
  }
}
