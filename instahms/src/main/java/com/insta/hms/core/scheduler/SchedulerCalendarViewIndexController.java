package com.insta.hms.core.scheduler;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(URLRoute.SCHEDULER_CALENDAR_INDEX_URL)
public class SchedulerCalendarViewIndexController extends BaseController {

  /**
   * Mount point for Scheduler Calendar View UI.
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public ModelAndView getSchedulerIndexPage() {
    return renderFlowUi("Doctor Scheduler", "scheduler", "withoutFlow", "withoutFlow", "calendar",
        false, "no-referrer-when-downgrade");
  }
}
