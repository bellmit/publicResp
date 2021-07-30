package com.insta.hms.core.scheduler;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;

import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO: Auto-generated Javadoc
/** The Class SchedulerBulkAppointmentsController. */
@RestController
@RequestMapping(URLRoute.SCHEDULER_BULK_APPOINTMENT_PATH)
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class SchedulerBulkAppointmentsController extends BaseRestController {

  /** The scheduler bulk appointments service. */
  @LazyAutowired SchedulerBulkAppointmentsService schedulerBulkAppointmentsService;

  /** The scheduler ACL. */
  @LazyAutowired private SchedulerACL schedulerACL;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /**
   * Gets the consultation index.
   *
   * @return the consultation index
   */
  @IgnoreConfidentialFilters
  @GetMapping("/index")
  public ModelAndView getConsultationIndex() {
    return renderFlowUi("Appointments", "v12", "withFlow", "opFlow", "appointment", false);
  }

  /**
   * Gets the orderable items for appointments.
   *
   * @param params the params
   * @return the orderable items for appointments
   */
  @IgnoreConfidentialFilters
  @GetMapping(path = "/getorderableitemForAppointments")
  public Map<String, Object> getOrderableItemsForAppointments(
      @RequestParam MultiValueMap<String, String> params) {
    return schedulerBulkAppointmentsService.getOrderableItemsForAppointments(params);
  }

  /**
   * Gets the appointments.
   *
   * @param request the request
   * @param response the response
   * @return the appointments
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getappointments", method = RequestMethod.GET)
  public Map getappointments(HttpServletRequest request, HttpServletResponse response) {
    Map resultMap =
        schedulerBulkAppointmentsService.getAppointmentsForPatient(request.getParameterMap(), null);
    List<Map> appointmentList = (List) resultMap.get("appointments");
    List<Map> resultList = new ArrayList();
    for (Map appointment : appointmentList) {
      if (!appointment.get("app_cat").equals("OPE")) {
        resultList.add(appointment);
      }
    }
    resultMap.put("appointments", resultList);
    return resultMap;
  }

  /**
   * Gets the resources schedule.
   *
   * @param request the request
   * @param response the response
   * @return the resources schedule
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/schedule", method = RequestMethod.GET)
  public Map<String, List<Object>> getResourcesSchedule(
      HttpServletRequest request, HttpServletResponse response) {
    Map<String, String[]> params = request.getParameterMap();
    String orderSetStr = params.get("orderset") != null ? params.get("orderset")[0] : null;
    if (orderSetStr == null || Boolean.parseBoolean(orderSetStr)) {
      return schedulerBulkAppointmentsService.getAvailableSlots(params);
    } else {
      return schedulerBulkAppointmentsService.getAvailableSlotsForASingleApp(params);
    }
  }

  /**
   * Save bulk appointments.
   *
   * @param requestBody the request body
   * @return the list
   */
  @PostMapping(value = "/saveBulkAppointments")
  public List<Map<String, Object>> saveBulkAppointments(@RequestBody ModelMap requestBody) {
    schedulerACL.checkAllowAddEditAppointment();
    return schedulerBulkAppointmentsService.saveAllBulkAppointments(requestBody);
  }

  /**
   * Edits the bulk appointments.
   *
   * @param requestBody the request body
   * @return the list
   */
  @PostMapping(value = "/editBulkAppointments")
  public List<Map<String, Object>> editBulkAppointments(@RequestBody ModelMap requestBody) {
    return schedulerBulkAppointmentsService.editAllBulkAppointment(requestBody);
  }

  /**
   * Cancel bulk appointments.
   *
   * @param requestBody the request body
   * @return the map
   * @throws Exception the exception
   */
  @PostMapping(value = "/cancelBulkAppointments")
  public Map<String, Object> cancelBulkAppointments(@RequestBody ModelMap requestBody)
      throws Exception {
    return schedulerBulkAppointmentsService.cancelBulkAppointments(requestBody);
  }

  /**
   * Arrive bulk appointments.
   *
   * @param requestBody the request body
   * @return the map
   */
  @PostMapping(value = "/arriveBulkAppointments")
  public Map<String, Object> arriveBulkAppointments(@RequestBody ModelMap requestBody) {
    return schedulerBulkAppointmentsService.arriveBulkAppointment(requestBody);
  }

  /**
   * Load details.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @return the map
   */
  @RequestMapping(value = "/patientdetails", method = RequestMethod.GET)
  public Map<String, Object> loadDetails(
      HttpServletRequest request, ModelMap mmap, HttpServletResponse response) {
    Map<String, Object> data = new HashMap<String, Object>();
    String appointmentId = request.getParameter("appointment_id");
    String visitId = request.getParameter("visit_id");
    String mrno = request.getParameter("mr_no");
    String contactId = request.getParameter("contact_id");
    if (appointmentId != null && !appointmentId.equals("")) {
      data = registrationService.getAppointmentDetails(appointmentId);
    } else if (visitId != null && !visitId.equals("")) {
      data = registrationService.getPatientVisitDetails(visitId);
    } else if (mrno != null && !mrno.equals("")) {
      data = registrationService.getPatientDetailsForNewVisit(mrno);
    } else if (contactId != null && !contactId.equals("")) {
      data = registrationService.getContactPatientDetailsForNewVisit(Integer.parseInt(contactId));
    } else {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    data.put("server_time", new Date());
    return data;
  }
}
