package com.insta.hms.common.phonenumber;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.URLRoute;
import com.insta.hms.exception.HMSException;

import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * Phone number validator controller.
 */
@RestController
@RequestMapping(URLRoute.MOBILE_NUMBER_VALIDATION)
public class PhoneNumberValidationController extends BaseRestController {

  /** Phone Number Validation Service class. **/
  @LazyAutowired
  private PhoneNumberValidationService phoneNumberValidationService;

  /**
   * Validate mobile number.
   *
   * @param request the request
   * @param resposne the resposne
   * @param requestBody the request body
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @PostMapping("/validate")
  public void validateMobileNumber(HttpServletRequest request, HttpServletResponse resposne,
      @RequestBody ModelMap requestBody) throws Exception {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    phoneNumberValidationService.validateMobileNumber(requestBody);
  }

}
