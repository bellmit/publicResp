package com.insta.hms.core.scheduler;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.validation.ActionRightsACLRule;
import com.insta.hms.core.patient.registration.RegistrationCustomFieldsService;
import com.insta.hms.exception.AccessDeniedException;

import org.springframework.stereotype.Component;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerACL.
 */
@Component
public class SchedulerACL {

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The action rights ACL rule. */
  @LazyAutowired
  private ActionRightsACLRule actionRightsACLRule;

  /** The custom fields service. */
  @LazyAutowired
  private RegistrationCustomFieldsService customFieldsService;

  /**
   * Check allow add edit appointment.
   */
  @SuppressWarnings("unchecked")
  public void checkAllowAddEditAppointment() {
    Map<String, String> actionRightsMap = (Map<String, String>) securityService
        .getSecurityAttributes().get("actionRightsMap");
    if ("N".equals(actionRightsMap.get("add_edit_scheduler_rights"))) {
      throw new AccessDeniedException("exception.access.denied");
    }
  }
}
