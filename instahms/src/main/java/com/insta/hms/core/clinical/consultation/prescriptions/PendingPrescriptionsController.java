package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.clinical.URLRoute;
import com.insta.hms.exception.HMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class PendingPrescriptionsController.
 */
@RestController
@RequestMapping(URLRoute.PENDING_PRESCRIPTION_URL)
public class PendingPrescriptionsController extends BaseRestController {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(PendingPrescriptionsController.class);

  /** ppp service. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionService;

  /** Generic preference. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  @GetMapping("/index")
  public ModelAndView getIndexPage() {
    return renderFlowUi("PendingPrescription - Insta", "dashboards", "withFlow", "dashboards",
        "pendingPrescription", false);
  }

  /**
   * lists all pending prescriptions.
   * 
   * @param request
   *          the request
   * @param mmap
   *          the map
   * @param response
   *          the response
   * @return prescriptions
   * @throws ParseException
   *           the exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/list")
  public Map<String, Object> list(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) throws ParseException {

    String cenId = "center_id";
    if (request.getParameter(cenId) == null && request.getParameter(cenId).equals("")) {
      logger.error("Center id is empty");
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return pendingPrescriptionService.getPendingPrescriptions(request.getParameterMap());
  }

  /**
   * Load remarks of the pending prescription.
   *
   * @param request the request
   * @param mmap the mmap
   * @param response the response
   * @return the list
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/loadRemarks")
  public List<Map<String, Object>> loadRemarks(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {

    if (request == null) {
      logger.info("Bad Request");
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }

    return pendingPrescriptionService.getPrescriptionRemarks(request);
  }

  /**
   * Creates pending prescription and remarks.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @param requestBody
   *          the request body
   * @return the boolean
   */
  @PostMapping(value = "/addPendingPrescriptonAndRemarks", consumes = "application/json")
  public Boolean addPendingPrescriptionAndRemarks(HttpServletRequest request,
      HttpServletResponse response, @RequestBody ModelMap requestBody) {
    if (requestBody.get("pending_prescription_id") == null
        || requestBody.get("pending_prescription_id").equals("")) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    return pendingPrescriptionService.addPendingPrescriptionsAndRemark(requestBody);
  }

  /**
   * loads user and users.
   * 
   * @param request
   *          the request
   * @return roles and users
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/loadUsersAndRoles")
  public List<Map<String, Object>> loadUserAndRoles(HttpServletRequest request) {
    String cenId = "center_id";
    if (request.getParameter(cenId) == null || request.getParameter(cenId).equals("")) {
      return pendingPrescriptionService.getUsersAndRoles(0);
    } else {
      return pendingPrescriptionService
          .getUsersAndRoles(Integer.parseInt(request.getParameter(cenId)));
    }
  }

  /**
   * loads user and users.
   * 
   * @param request
   *          the request
   * @return roles and users
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getAssignedUsersAndRoles")
  public List<Map<String, Object>> getPrescriptionedUsersAndRoles(HttpServletRequest request) {
    String cenId = "center_id";
    if (request.getParameter(cenId) == null || request.getParameter(cenId).equals("")) {
      return pendingPrescriptionService.getPrescriptionedUsersAndRoles(0);
    } else {
      return pendingPrescriptionService
          .getPrescriptionedUsersAndRoles(Integer.parseInt(request.getParameter(cenId)));
    }
  }

  /**
   * Gets the user prescription types.
   *
   * @param request
   *          the request
   * @return the user prescription types
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getUserPrescriptionTypes")
  public List<String> getUserPrescriptionTypes(HttpServletRequest request) {
    return pendingPrescriptionService.getUserPrescriptionTypes();
  }

  @IgnoreConfidentialFilters
  @GetMapping("/getAppointmentId")
  public Map<String, String> getAppointmentId(@RequestParam("patient_prescription_id") Long ppdId) {
    return pendingPrescriptionService.getAppointmentId(ppdId);
  }
}
