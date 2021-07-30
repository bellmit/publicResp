package com.insta.hms.core.scheduler.appointmentplanner;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.exception.AccessDeniedException;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class AppointmentPlannerACL.
 */
@Component
public class AppointmentPlannerACL {

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /**
   * Check allow add appointment plan.
   */
  @SuppressWarnings("unchecked")
  public void checkAllowAddAppointmentPlan() {
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if ("N".equals(actionRightsMap.get("add_edit_appointment_plan"))) {
      throw new AccessDeniedException("exception.access.denied");
    }
  }

  /**
   * Check allow edit appointment plan.
   */
  public void checkAllowEditAppointmentPlan() {
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if ("N".equals(actionRightsMap.get("edit_appointment_plan"))) {
      throw new AccessDeniedException("exception.access.denied");
    }
  }
}
