package com.insta.hms.api.controllers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.masterdata.MasterdataService;
import com.insta.hms.core.scheduler.SchedulerBulkAppointmentsService;
import com.insta.hms.core.scheduler.SchedulerService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.instaapi.common.ApiUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class ApiSchedulerController.
 */
@Controller
@RequestMapping("/api/scheduler")
public class ApiSchedulerController extends BaseRestController {

  /** The masterdata service. */
  @LazyAutowired
  private MasterdataService masterdataService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /** The scheduler service. */
  @LazyAutowired
  private SchedulerService schedulerService;

  /** The resource availability service. */
  @LazyAutowired
  private ResourceAvailabilityService resourceAvailabilityService;

  /** The order service. */
  @LazyAutowired
  private OrderService orderService;

  /** The scheduler bulk appointments service. */
  @LazyAutowired
  SchedulerBulkAppointmentsService schedulerBulkAppointmentsService;

  /** The Doctor service. */
  @LazyAutowired
  DoctorService doctorService;

  /** The Constant BAD_REQUEST. */
  public static final String BAD_REQUEST = "exception.bad.request";

  /**
   * Save appointment.
   *
   * @param request     the request
   * @param response    the response
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/save", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> saveAppointment(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    return new ResponseEntity<>(schedulerService.saveAppointmentWithUtcTime(requestBody),
        HttpStatus.OK);
  }

  /**
   * Save service appointment.
   *
   * @param request     the request
   * @param response    the response
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/saveservice", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> saveServiceAppointment(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    return new ResponseEntity<>(schedulerService.setPrimaryResAndSaveAppointment(requestBody),
        HttpStatus.OK);
  }

  /**
   * Save service appointment.
   *
   * @param request     the request
   * @param response    the response
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/updateservice", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> updateServiceAppointment(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    return new ResponseEntity<>(schedulerService.setPrimaryResAndUpdateAppointment(requestBody),
        HttpStatus.OK);
  }

  /**
   * Update appointment.
   *
   * @param request     the request
   * @param response    the response
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/edit", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> updateAppointment(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    return new ResponseEntity<>(schedulerService.editAppointmentWithUtcTime(requestBody),
        HttpStatus.OK);
  }

  /**
   * Update appointment status.
   *
   * @param request     the request
   * @param response    the response
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/updatestatus", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> updateAppointmentStatus(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, BAD_REQUEST, null);
    }
    return new ResponseEntity<>(schedulerService.updateAppointmentsStatus(requestBody),
        HttpStatus.OK);
  }

  /**
   * Gets the available slots.
   *
   * @param request    the request
   * @param response   the response
   * @param date       the date
   * @param resourceId the resource id
   * @param centerId   the center id
   * @param bookedSlot the booked slot
   * @return the available slots
   */
  @GetMapping(value = "/availableslots")
  public Map<String, Object> getAvailableSlots(HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(required = false, value = "appointment_date") String date,
      @RequestParam(required = false, value = "from_date") String fromDate,
      @RequestParam(required = false, value = "to_date") String toDate,
      @RequestParam(value = "resource_id") String resourceId,
      @RequestParam(required = false, value = "center_id") Integer centerId,
      @RequestParam(required = false, value = "visit_mode") String visitMode,
      @RequestParam(required = false, value = "booked_slot") String bookedSlot,
      @RequestParam(required = false, value = "first_available") String firstAvailable) {
    return resourceAvailabilityService.getAvailableSlots(date, resourceId, centerId, bookedSlot,
        fromDate, toDate, visitMode, firstAvailable);
  }

  /**
   * Gets the first available slots.
   *
   * @param request    the request
   * @param response   the response
   * @param date       the date
   * @param resourceId the resource id
   * @param centerId   the center id
   * @param bookedSlot the booked slot
   * @return the first available slots
   */
  @GetMapping(value = "/firstavailableslots")
  public Map<String, Object> getFirstAvailableSlots(HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(required = false, value = "appointment_date") String date,
      @RequestParam(value = "resource_id") String[] resourceId,
      @RequestParam(required = false, value = "from_date") String fromDate,
      @RequestParam(required = false, value = "center_id") Integer centerId,
      @RequestParam(required = false, value = "visit_mode") String visitMode,
      @RequestParam(required = false, value = "booked_slot") String bookedSlot) {
    return resourceAvailabilityService.getFirstAvailableSlots(date, resourceId, centerId,
        bookedSlot, fromDate, visitMode);
  }

  /**
   * Gets the orderable item.
   *
   * @param params the params
   * @return the orderable item
   */
  @GetMapping(value = "/orderableitem")
  public Map<String, Object> getOrderableItem(@RequestParam MultiValueMap<String, String> params) {
    params.put("doctor_schedulable", Arrays.asList("A"));
    params.put("orderset_schedulable", Arrays.asList("A"));
    params.put("service_schedulable", Arrays.asList("A"));
    List<BasicDynaBean> orderableItems = orderService
        .getOrderableItem(new InstaLinkedMultiValueMap<String, String>(params));
    Map<String, Object> getOrderableItemData = new HashMap<>();
    List<Map<String,Object>> orderableItemMap = ConversionUtils.listBeanToListMap(orderableItems);
    List<Map<String,Object>> newOrderableItemMap = new ArrayList<>();
    for (Map<String, Object> orderableItem : orderableItemMap) {
      if (orderableItem.get("type").equals("Doctor")) {
        Map item = new HashMap();
        item.putAll(orderableItem);
        String docPhoto = doctorService.getDoctorImageById((String) orderableItem.get("id"));
        if (docPhoto != null && !docPhoto.equals("")) {
          item.put("doctorimage", docPhoto);
        }
        newOrderableItemMap.add(item);
      } else {
        newOrderableItemMap.add(orderableItem);
      }
    }
    getOrderableItemData.put("orderable_items", newOrderableItemMap);
    return getOrderableItemData;
  }

  /**
   * Gets the package components for bulk appts.
   *
   * @param packageId         the package id
   * @param mrNo              the mr no
   * @param multiVisitPackage the multi visit package
   * @return the package components for bulk appts
   */
  @GetMapping(value = "/packagecontents")
  public Map<String, Object> getPackageComponentsForBulkAppts(
      @RequestParam("package_id") Integer packageId,
      @RequestParam(required = false, value = "mr_no") String mrNo,
      @RequestParam(required = false, value = "multi_visit_package") boolean multiVisitPackage) {
    return orderService.getpackagecontentsforbulkappts(packageId.toString(), "Order Sets", mrNo,
        multiVisitPackage);

  }

  /**
   * Save order set appointment.
   *
   * @param requestBody the request body
   * @return the list
   */
  @PostMapping(value = "/saveordersetappointment")
  public List<Map<String, Object>> saveOrderSetAppointment(@RequestBody ModelMap requestBody) {
    return schedulerBulkAppointmentsService.saveBulkAppointmentsUTC(requestBody);
  }

  /**
   * Gets the resources schedule.
   *
   * @param request  the request
   * @param response the response
   * @return the resources schedule
   */
  @RequestMapping(value = "/ordersetslots", method = RequestMethod.GET)
  public Map<String, List<Object>> getResourcesSchedule(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, String[]> requestMap = new HashMap<>(request.getParameterMap());
    requestMap.put("is_utc", new String[] { "true" });
    return schedulerBulkAppointmentsService.getAvailableSlotsUTC(requestMap);
  }

  /**
   * Gets the appointments for patient.
   *
   * @param request  the request
   * @param response the response
   * @return the appointments for patient
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @RequestMapping(value = "/getappointments", method = RequestMethod.GET)
  public Map getAppointmentsForPatient(HttpServletRequest request, HttpServletResponse response)
      throws ParseException {
    String requestHandalerKey = ApiUtil.getRequestHandlerKey((HttpServletRequest) request);
    Map resultMap = schedulerBulkAppointmentsService
        .getAppointmentsForPatient(request.getParameterMap(), requestHandalerKey);
    List<Map> appointmentList = (List) resultMap.get("appointments");
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    for (Map appointment: appointmentList) {
      String tempDate = 
          appointment.get("appointment_date")
          + " " 
          + appointment.get("appointment_time");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      Timestamp ts = null;
      try {
        ts = new java.sql.Timestamp(sdf.parse(tempDate).getTime());
      } catch (ParseException exc) {
        // TODO Auto-generated catch block
        exc.printStackTrace();
      }
      String timeStr = DateUtil.formatSQlTime((Time) appointment.get("appointment_time"));
      String dateStr = DateUtil.formatDate((java.util.Date) appointment.get("appointment_date"));
      java.util.Date date = df.parse(dateStr + " " + timeStr);
      timeStr = DateUtil.formatIso8601TimestampNoSec(date);
      appointment.remove("appointment_time");
      appointment.put("appointment_date", timeStr);
    }
    return resultMap;
  }

  /**
   * Gets service appointment slots.
   *
   * @param request  the request
   * @param response the response
   * @return the service appointment slots
   */
  @RequestMapping(value = "/serviceslots", method = RequestMethod.GET)
  public Map<String, Object> getServiceResSlots(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, String[]> requestMap = new HashMap<>(request.getParameterMap());
    requestMap.put("is_utc", new String[] { "true" });
    Map<String, String[]> params = new HashMap<>(request.getParameterMap());
    params.put("is_utc", new String[] { "true" });
    Map<String,Object> availableSlots = schedulerBulkAppointmentsService
        .getAvailableSlotsForASingleApp(params);
    Map<String, Object> slotsMap = new HashMap<>();
    List slotsData = new ArrayList();
    Integer centerId = Integer.valueOf(params.get("center_id")[0]);
    for (Map.Entry<String, Object> entry : availableSlots.entrySet()) {
      String key = entry.getKey();
      Map map  = new HashMap();
      map.put("timeslot", key);
      map.put("center_id", centerId);
      slotsData.add(map);
    }
    slotsMap.put("slots", slotsData);
    slotsMap.put("first_available_date", params.get("date")[0]);
    return slotsMap;
  }

  /**
   * get doctorImage by doctor id.
   * @param request request object
   * @param response response object
   * @return returns doctorImage byte data
   */
  @RequestMapping(value = "/doctorimage", method = RequestMethod.GET)
  public String getDoctorImage(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, String[]> requestMap = new HashMap<>(request.getParameterMap());
    String doctorId = requestMap.get("doctor_id")[0];
    return doctorService.getDoctorImageById(doctorId);
  }
}
