package com.insta.hms.core.patient.header;

import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.patient.URLRoute;
import com.insta.hms.core.patient.registration.RegistrationCustomFieldsService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is controller for patient header related end points.
 *
 * @author sainathbatthala
 */
@RestController
@RequestMapping(value = URLRoute.PATIENT_HEADER_INDEX_URL)
public class PatientHeaderController extends BaseRestController {

  /** The service. */
  @Autowired
  PatientHeaderService service;
  
  @Autowired
  RegistrationCustomFieldsService regCustomService;

  /**
   * This method handles get patient details end point.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return patient details map
   * @throws ParseException
   *           the parse exception
   */
  @RequestMapping(value = { "/show", "" }, method = RequestMethod.GET)
  public Map<String, Object> show(HttpServletRequest req, HttpServletResponse resp)
      throws ParseException {

    
    String advanced = req.getParameter("advanced");
    String headerPreferences = req.getParameter("header_preferences");
    String[] visitType = req.getParameterValues("visit_type");

    // set the advanced to default value 'N'
    if (advanced == null) {
      advanced = "N";
    }

    // set the header preferences to default value 'Y'
    if (headerPreferences == null) {
      headerPreferences = "Y";
    }

    // set the visit type to default value ['b', 'i', 'o']
    if (visitType == null) {
      visitType = new String[] { "b", "i", "o" };
    }

    // set the data category to default value ['Both', 'None', 'O', 'C']
    String[] dataCategory = req.getParameterValues("data_category");
    if (dataCategory == null) {
      dataCategory = new String[] { "Both", "None", "O", "C" };
    }
    String mrNo = req.getParameter("mr_no");
    String visitId = req.getParameter("visit_id");
    String appointmentId = req.getParameter("appointment_id");
    Map<String, Object> responseMap = service.getPatientDetails(advanced, mrNo, visitId,
        appointmentId, headerPreferences, visitType, dataCategory);

    return responseMap;
  }

  /**
   * Gets the preferred languages.
   *
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the preferred languages
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/preferredlanguages" }, method = RequestMethod.GET)
  public List<Map<String, String>> getPreferredLanguages(HttpServletRequest request,
      HttpServletResponse response) {
    return service.getPreferredLanguages((String) request.getAttribute("language"));
  }

  /**
   * This method handles update patient details end point.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @return updated patient details map
   */
  @RequestMapping(value = { "/update" }, method = RequestMethod.POST)
  public Map<String, Object> update(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    Map<String, Object> responseMap = service.updatePatientDetails(requestBody);

    return responseMap;
  }

  /**
   * Upload photo.
   *
   * @param req
   *          the req
   * @param resp
   *          the resp
   * @param patientPhoto
   *          the patient photo
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = { "/uploadPhoto" }, method = RequestMethod.POST)
  public void uploadPhoto(HttpServletRequest req, HttpServletResponse resp,
      @RequestParam("patient_photo") MultipartFile patientPhoto) throws IOException {

    String mrNo = req.getParameter("mr_no");
    byte[] patientPhotoBytes = null;
    if (!patientPhoto.isEmpty()) {
      patientPhotoBytes = patientPhoto.getBytes();
    }

    service.uploadPhoto(mrNo, patientPhotoBytes);

  }

  /**
   * This method gets the patient photo.
   *
   * @param request
   *          the request
   * @param resp
   *          the resp
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/getPhoto", method = RequestMethod.GET)
  public void getPhoto(HttpServletRequest request, HttpServletResponse resp) throws IOException {
    HttpHeaders headers = new HttpHeaders();

    String mrNo = request.getParameter("mr_no");
    String dimensions = request.getParameter("dimensions");
    InputStream response = service.getPhoto(mrNo, dimensions);

    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(response).toString();
    if (!(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("params", "exception.illegal.document.type");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("getPhoto", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    resp.addHeader("Content-Type", contentType);

    OutputStream responseStream = null;
    try {
      responseStream = resp.getOutputStream();
      StreamUtils.copy(response, responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
  }
  
  @IgnoreConfidentialFilters
  @RequestMapping(value = { "/customLists" }, method = RequestMethod.GET)
  public Map<String, Object> getCustomLists(HttpServletRequest request,
      HttpServletResponse response) {
    return regCustomService.getCustomFieldValues((String) request.getParameter("type"));
  }
}
