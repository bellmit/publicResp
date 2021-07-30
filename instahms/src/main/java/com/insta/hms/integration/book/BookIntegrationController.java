package com.insta.hms.integration.book;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.HMSErrorResponse;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.URLRoute;
import com.insta.hms.mdm.ReferenceDataConverter;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.practo.integration.sdk.SDKErrorCode;
import com.practo.integration.sdk.SDKException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class BookIntegrationController.
 */
@RestController
@RequestMapping(URLRoute.BOOK_INTEGRATION_PATH)
public class BookIntegrationController extends BaseRestController {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(BookIntegrationController.class);

  /** The book integration service. */
  @LazyAutowired
  private BookIntegrationService bookIntegrationService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The converter. */
  @LazyAutowired
  private ReferenceDataConverter converter;

  /** The center preferences service. */
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;

  /**
   * Show.
   *
   * @return the map
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public Map<String, List<Map>> show() {
    return converter.convert(bookIntegrationService.getReferenceData());

  }

  /**
   * Gets the center prefs.
   *
   * @param centerId the center id
   * @return the center prefs
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/centerprefs", method = RequestMethod.GET)
  public Map<String, List<Map>> getCenterPrefs(@RequestParam("center_id") int centerId) {
    return converter.convert(bookIntegrationService.getCenterPreferences(centerId));
  }

  /**
   * Connect establishment to practo.
   *
   * @param establishmentKey the establishment key
   * @param centerId the center id
   * @throws SDKException the SDK exception
   */
  @RequestMapping(value = "/validate", method = RequestMethod.POST)
  public void connectEstablishmentToPracto(
      @RequestParam("establishment_key") String establishmentKey,
      @RequestParam("center_id") int centerId) throws SDKException {
    try {
      bookIntegrationService.connectEstablishmentToPracto(establishmentKey, centerId);
    } catch (SDKException ex) {
      BookSDKUtil.close(establishmentKey);
      throw ex;
    }
  }

  /**
   * Push doctors.
   *
   * @param doctorIdList the doctor id list
   * @param centerId the center id
   * @return the map
   */
  @RequestMapping(value = "/doctors", method = RequestMethod.POST)
  public Map<String, Object> pushDoctors(@RequestParam("doctor_ids") List<String> doctorIdList,
      @RequestParam("center_id") int centerId) {
    boolean updateDoctors = false;
    return bookIntegrationService.registerDoctors(doctorIdList, centerId, updateDoctors);

  }

  /**
   * Update share pat details prefs.
   *
   * @param centerId the center id
   * @param sharePatientDetials the share patient detials
   */
  // TODO:: move to center preferences controller
  @RequestMapping(value = "/centerprefs", method = RequestMethod.POST)
  public void updateSharePatDetailsPrefs(@RequestParam("center_id") Integer centerId,
      @RequestParam("share_pat_details_to_practo") boolean sharePatientDetials) {
    BasicDynaBean centerPrefs = centerPreferencesService.getCenterPreferences(centerId);
    centerPrefs.set("share_pat_details_to_practo", sharePatientDetials);
    centerPrefs.set("center_id", centerId);
    centerPreferencesService.insertOrUpdateCenterPreferences(centerId, centerPrefs);
  }

  /**
   * Gets the doctors department wise.
   *
   * @param centerId the center id
   * @return the doctors department wise
   */
  // TODO :: Move to Doctor Controller
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/doctors", method = RequestMethod.GET)
  public Map<String, List<Map<String, Object>>> getDoctorsDepartmentWise(
      @RequestParam("center_id") int centerId) {
    Map<String, List<Map<String, Object>>> doctorsDepartmentWise = new HashMap<String,
        List<Map<String, Object>>>();
    List<Map<String, Object>> doctorsList = doctorService.getDoctors(centerId);
    for (Map<String, Object> doctor : doctorsList) {
      String departmentId = (String) doctor.get("dept_id");
      List<Map<String, Object>> doctorsInDept = doctorsDepartmentWise.get(departmentId);
      if (doctorsInDept == null) {
        doctorsInDept = new ArrayList<Map<String, Object>>();
      }
      doctorsInDept.add(doctor);
      doctorsDepartmentWise.put(departmentId, doctorsInDept);

    }
    return doctorsDepartmentWise;

  }

  /**
   * Handle practo exception.
   *
   * @param ex the ex
   * @param request the request
   * @param response the response
   * @return the model and view
   */
  @ExceptionHandler(SDKException.class)
  public ModelAndView handlePractoException(SDKException ex, HttpServletRequest request,
      HttpServletResponse response) {
    logger.error("Practo SDKException ", ex);
    HttpStatus errorCode = HttpStatus.UNAUTHORIZED;
    String errorMessage = BookSDKUtil.getErrorMessage(ex.getErrorCode());
    if (Arrays.asList(SDKErrorCode.NETWORK_ERROR, SDKErrorCode.INVALID_URI)
        .contains(ex.getErrorCode())) {
      errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    ModelAndView mav = new ModelAndView();

    mav.addObject("error", new HMSErrorResponse(errorCode, errorMessage));

    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    return mav;
  }

}
