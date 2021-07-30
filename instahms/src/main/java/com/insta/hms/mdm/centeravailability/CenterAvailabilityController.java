package com.insta.hms.mdm.centeravailability;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(URLRoute.CENTER_AVAILABILITY_PATH)
public class CenterAvailabilityController extends MasterRestController {

  @LazyAutowired
  private CenterAvailabilityService centerAvailabilityService;

  public CenterAvailabilityController(CenterAvailabilityService service) {
    super(service);
  }

  @RequestMapping(value = "/centertimings", method = RequestMethod.GET)
  public Map<String, Object> getCenterTimings(HttpServletRequest request,
      HttpServletResponse response) {
    return centerAvailabilityService.getCenterTimings();
  }

}