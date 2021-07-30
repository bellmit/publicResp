package com.insta.hms.core.scheduler.appointmentplanner;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(URLRoute.BASE_URL)
public class AppointmentPlannerController extends BaseRestController {

  @LazyAutowired
  private AppointmentPlannerService appointmentPlannerService;

  @PostMapping(path = "/planner")
  public ResponseEntity createNewPlan(@RequestBody ModelMap requestBody) {
    appointmentPlannerService.createNewPlan(requestBody);
    return new ResponseEntity<>(requestBody, HttpStatus.CREATED);
  }

  @GetMapping(path = "/allplans/{mr_no}")
  public Map<String, List<Map<String, Object>>> getAllPlansForPatient(@PathVariable String mrNo) {
    return appointmentPlannerService.getPlansForPatient(mrNo, RequestContext.getCenterId());
  }

  @GetMapping(path = "/planDetails/{plan_id}")
  public Map<String, Object> getPlanDetails(@PathVariable Integer planId) {
    return appointmentPlannerService.getPatientPlanDetails(planId);
  }

  @PutMapping(path = "/planner")
  public ResponseEntity modifyPlan(@RequestBody ModelMap requestBody) {
    appointmentPlannerService.modifyPlan(requestBody);
    return new ResponseEntity(requestBody, HttpStatus.OK);
  }
}
