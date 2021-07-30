package com.insta.hms.common.session;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.URLRoute;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The Class SessionController to expose selected session attributes.
 * 
 * @author tanmay.k
 */
@RestController("sessionController")
@RequestMapping(URLRoute.SESSION_URL)
public class SessionController extends BaseRestController {

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Lists the exposed session attributes
   *
   * @return the map of session attributes.
   */
  @IgnoreConfidentialFilters
  @GetMapping
  public Map<String, Object> list() {
    return sessionService.getSessionAttributes();
  }
}
