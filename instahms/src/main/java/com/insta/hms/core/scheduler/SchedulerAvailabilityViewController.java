package com.insta.hms.core.scheduler;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.practitionerconsultationmapping.PractitionerConsultationMappingService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class SchedulerAvailabilityViewController.
 */
@RestController
@RequestMapping(URLRoute.AVAILABILITY_VIEW_URL)
public class SchedulerAvailabilityViewController extends BaseRestController {
  
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** The scheduler service. */
  @LazyAutowired
  private SchedulerService schedulerService;
  
  /** The appointment service. */
  @LazyAutowired
  private AppointmentService appointmentService;
  
  /** The scheduler ACL. */
  @LazyAutowired
  private SchedulerACL schedulerACL;
  
  /** The practitioner consultation mapping service. */
  @LazyAutowired
  private PractitionerConsultationMappingService practitionerConsultationMappingService;
  
  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;
  
  @LazyAutowired
  private PractitionerTypeMappingsService practitionerTypeMappingService;

  /**
   * Save appointment.
   *
   * @param request the request
   * @param response the response
   * @param requestBody the request body
   * @return the map
   */
  @RequestMapping(value = {
      "/saveappt" }, method = RequestMethod.POST, consumes = "application/json")
  public Map<String, Object> saveAppointment(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    schedulerACL.checkAllowAddEditAppointment();
    return schedulerService.saveAppointment(requestBody);
  }

  /**
   * Gets the appointment details.
   *
   * @param request the request
   * @param response the response
   * @return the appointment details
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/getsecondaryresources", method = RequestMethod.GET)
  public Map<String, Object> getAppointmentDetails(HttpServletRequest request,
      HttpServletResponse response) {
    return appointmentService.getSecondaryResources(request.getParameterMap());
  }

  /**
   * Gets the doctor consultation types.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the doctor consultation types
   */

  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getdoctorconsultationtypes", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getDoctorConsultationTypes(HttpServletRequest request,
      HttpServletResponse response) {
    String doctorId = request.getParameter("doctor_id");
    Map<String, Object> responseMap = null;
    if (null == doctorId || "".equals(doctorId)) {
      responseMap = new HashMap<>();
      return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
    }
    Map<String, String> filterMap = new HashMap<>();
    filterMap.put("doctor_id", doctorId);
    BasicDynaBean doctorBean = doctorService.findByPk(filterMap);
    if (null == doctorBean) {
      responseMap = new HashMap<>();
      return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
    }
    Integer practitionerTypeId = (Integer) doctorBean.get("practitioner_id");
    String apptCat = request.getParameter("appt_cat");
    responseMap = new HashMap<>(practitionerConsultationMappingService
        .getDoctorConsultationTypes(practitionerTypeId, apptCat));
    List defaultList = ConversionUtils.listBeanToListMap(practitionerTypeMappingService
        .getPractitionerMappings(RequestContext.getCenterId(), practitionerTypeId));
    responseMap.put("default_consultation", defaultList);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }

  /**
   * checks if user is allowed to over book appointment.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return boolean value true or false
   */
  @IgnoreConfidentialFilters
  @GetMapping("/isoverbookallowed")
  public ResponseEntity<Map<String, Object>> isUserAllowedToOverBook(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, String[]> params = request.getParameterMap();
    String resourceId = params.get("resource_id")[0];
    String apptTimeStrg = params.get("appt_time")[0];
    String resCategory = params.get("category")[0];
    boolean isAllowed = schedulerService.isUserAllowedToBookAppts(resourceId,
        apptTimeStrg, resCategory);
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("isOverBookAllowed", isAllowed);
    return new ResponseEntity<>(responseMap, HttpStatus.OK);
  }
}
