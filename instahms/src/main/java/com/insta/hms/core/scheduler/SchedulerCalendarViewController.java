package com.insta.hms.core.scheduler;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerCalendarViewController.
 */
@RestController
@RequestMapping(URLRoute.CALENDAR_VIEW_URL)
public class SchedulerCalendarViewController extends BaseRestController {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The scheduler service. */
  @LazyAutowired
  private SchedulerService schedulerService;
  
  /** The scheduler ACL. */
  @LazyAutowired
  private SchedulerACL schedulerACL;

  /**
   * Gets the resources schedule.
   *
   * @param request the request
   * @param response the response
   * @return the resources schedule
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/schedule", method = RequestMethod.GET)
  public Map<String, List<Object>> getResourcesSchedule(HttpServletRequest request,
      HttpServletResponse response) {
    return schedulerService.getSecondaryResourcesSchedule(request.getParameterMap());
  }

  /**
   * Gets the appointment details.
   *
   * @param request the request
   * @param response the response
   * @return the appointment details
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/viewappt", method = RequestMethod.GET)
  public Map<String, Object> getAppointmentDetails(HttpServletRequest request,
      HttpServletResponse response) {
    return schedulerService.getAppointmentDetails(request.getParameterMap());
  }

  /**
   * Edits the appointment details.
   *
   * @param request the request
   * @param response the response
   * @param requestBody the request body
   * @return the map
   */
  @RequestMapping(value = "/editappt", method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> editAppointmentDetails(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    schedulerACL.checkAllowAddEditAppointment();
    return schedulerService.editAppointmentDetails(requestBody);
  }

  /**
   * Update appointment status.
   *
   * @param request the request
   * @param response the response
   * @param requestBody the request body
   * @return the map
   */
  @RequestMapping(value = "/updateapptstatus", method = RequestMethod.POST, 
      consumes = "application/json")
  public Map<String, Object> updateAppointmentStatus(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return schedulerService.updateAppointmentsStatus(requestBody);
  }

  /**
   * Mark resource overrides.
   *
   * @param request the request
   * @param response the response
   * @param requestBody the request body
   * @return the map
   */
  @RequestMapping(value = { "/addoverride" }, method = RequestMethod.POST,
      consumes = "application/json")
  public Map<String, Object> markResourceOverrides(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return schedulerService.addOverrides(requestBody).get(0);
  }
}
