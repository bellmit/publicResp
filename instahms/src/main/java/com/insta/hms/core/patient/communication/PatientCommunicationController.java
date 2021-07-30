package com.insta.hms.core.patient.communication;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(URLRoute.PATIENT_COMMUNICATION_URL)
public class PatientCommunicationController extends BaseRestController {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @LazyAutowired
  private PatientCommunicationService service;

  @IgnoreConfidentialFilters
  @RequestMapping(value = "/preferences", method = RequestMethod.GET)
  public Map<String, Object> getMessageDetails(HttpServletRequest request,
      HttpServletResponse response) {
    return service.getMessageDetails(request.getParameterMap());
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> removefield(HttpServletRequest request, HttpServletResponse response,
      @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return service.updateCommunicationPreference(requestBody);
  }

}
