package com.insta.hms.common.multiuser;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/multiuser")
public class MultiUserController extends BaseRestController {

  @LazyAutowired
  private MultiUserService multiUserService;

  @IgnoreConfidentialFilters
  @GetMapping(value = "/actionscreen/lock")
  public Map<String, Object> obtainScreenLock(@RequestParam(value = "screen_id") String screenId,
      @RequestParam(value = "patient_id") String patientId) {
    return multiUserService.getScreenLock(screenId, patientId);
  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/actionscreen/deletelock")
  public Map<String, Object> removeScreenLock(@RequestParam(value = "screen_id") String screenId,
      @RequestParam(value = "patient_id") String patientId) {
    return multiUserService.removeScreenLock(screenId, patientId);
  }

}
