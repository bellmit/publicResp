package com.insta.hms.core.patient.registration;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;
import com.insta.hms.exception.HMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class QuickEstimateController.
 */
@RequestMapping(URLRoute.QUICK_ESTIMATE)
public class QuickEstimateController extends BaseRestController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(QuickEstimateController.class);

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /**
   * Estimate amount.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param requestBody
   *          the request body
   * @return the map
   * @throws SQLException
   *           the SQL exception
   */
  @RequestMapping(value = "/estimateamount", 
      method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> estimateAmount(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) throws SQLException {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    Map<String, Object> map = registrationService.estimateAmount(requestBody);
    return map;
  }
}
