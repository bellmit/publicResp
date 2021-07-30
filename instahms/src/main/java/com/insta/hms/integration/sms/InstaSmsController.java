package com.insta.hms.integration.sms;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InstaSmsController.
 */
@RestController
@RequestMapping(URLRoute.INSTA_SMS)
public class InstaSmsController extends BaseRestController {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(InstaSmsController.class);

  /** Insta SMS Service. */
  @LazyAutowired
  private InstaSmsService instaSmsService;

  /**
   * Receives sms via api.
   * 
   * @param request  the request
   * @param response the response
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/smsInbound/{schema}", method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON_VALUE })
  public Map<String, String> receiveSms(@PathVariable String schema, HttpServletRequest request,
      HttpServletResponse response) {
    return instaSmsService.storeSmsRequestAndScheduleJob(schema, request.getParameterMap(),
        response);
  }
}
