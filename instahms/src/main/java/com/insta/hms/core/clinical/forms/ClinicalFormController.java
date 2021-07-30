package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.forms.FormController;

import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class FormController.
 *
 * @param <T> the generic type
 */
public abstract class ClinicalFormController<T> extends FormController<T> {

  /**
   * Instantiates a new form controller.
   *
   * @param service the service
   */
  public ClinicalFormController(ClinicalFormService service) {
    super(service);
  }

  /**
   * Calc vital expressions.
   *
   * @param requestBody the request body
   * @return the map
   */
  @PostMapping(value = URLRoute.CALCULATE_VITAL_EXPRESSIONS)
  public Map<String, List<Map<String, Object>>> calcVitalExpressions(
      @RequestBody ModelMap requestBody) {
    return ((ClinicalFormService) service).getVitalExprData(requestBody);
  }

  /**
   * Gets the patient recent allergies.
   *
   * @param mrNo the mr no
   * @return the patient recent allergies
   */
  @GetMapping(value = "/patient/{mrNo}/allergies")
  public Map<String, Object> getPatientRecentAllergies(@PathVariable("mrNo") String mrNo) {
    return ((ClinicalFormService) service).getPatientRecentAllergies(mrNo);
  }

  /**
   * Gets the diag year of onset.
   *
   * @param mrNo the mr no
   * @param diagCode the diag code
   * @return the diag year of onset
   */
  @GetMapping(value = URLRoute.GET_DIAGNOSIS_ONSET_YEAR)
  public Map<String, Object> getDiagYearOfOnset(@PathVariable String mrNo, @RequestParam(
      required = true, value = "diag_code") String diagCode) {
    return ((ClinicalFormService) service).getDiagYearOfOnset(mrNo, diagCode);
  }

  /**
   * Gets the diagnosis code.
   *
   * @param searchQuery the search query
   * @param diagCodeType the diag code type
   * @return the diagnosis code
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/diagCodes", method = RequestMethod.GET)
  public Map<String, Object> getDiagnosisCode(
      @RequestParam(value = "search_query") String searchQuery, @RequestParam(
          value = "diag_code_type") String diagCodeType) {
    return ((ClinicalFormService) service).getDiagnosisCodes(searchQuery, diagCodeType);
  }

  /**
   * Gets the previous diagnosis details.
   *
   * @param patientId the patient id
   * @return the previous diagnosis details
   */
  @RequestMapping(value = "/visit/{patientId}/previousDiagnosisDetails",
      method = RequestMethod.GET)
  public Map<String, Object> getPreviousDiagnosisDetails(
      @PathVariable("patientId") String patientId,
      @RequestParam(required = false, value = "page_no") Integer pageNo) {
    return ((ClinicalFormService) service).getPreviousDiagnosisDetails(patientId, pageNo);
  }

  /**
   * Gets the favrourites diagnosis code.
   *
   * @param doctorId the doctor id
   * @param searchQuery the search query
   * @param diagCodeType the diag code type
   * @return the favrourites diagnosis code
   */

  @IgnoreConfidentialFilters
  @RequestMapping(value = "/favDiagCodes", method = RequestMethod.GET)
  public Map<String, Object> getFavrouritesDiagnosisCode(
      @RequestParam(value = "doctor_id") String doctorId,
      @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(value = "diag_code_type") String diagCodeType) {
    return ((ClinicalFormService) service).getFavrouritesDiagnosisCode(doctorId, searchQuery,
        diagCodeType);
  }

  @GetMapping(value = URLRoute.GET_INSURANCE_DETAILS)
  public Map<String, Object> getInsuranceDetails(@PathVariable("visitId") String visitId) {
    return ((ClinicalFormService) service).getPatInsuranceInfo(visitId);
  }

  /**
   * Get Patient Problem History.
   * 
   * @param request the http request
   * @param mmap the map
   * @param response http response
   * @return map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/patientProblems/history")
  public Map<String, Object> getPatientProblemHistory(HttpServletRequest request, ModelMap mmap,
      HttpServletResponse response) {
    return ((ClinicalFormService)service).getPatientProblemHistory(request.getParameterMap());
  }

  /**
   * Gets the Get Patient Problem List.
   *
   * @param searchQuery the search query
   * @return the patient problem list
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/patientProblems/search")
  public Map<String, Object> getPatientProblemList(
      @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(value = "problem_code_type") String codeType) {
    return ((ClinicalFormService)service).getPatientProblemList(searchQuery, codeType);
  }

  /**
   * Gets the templatedetails.
   *
   * @param request the request
   * @param response the response
   * @return the templatedetails
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/gettemplatedetails")
  public Map<String, Object> gettemplatedetails(HttpServletRequest request,
      HttpServletResponse response) {
    return ((ClinicalFormService)service).getNoteTypesTemplateDetails(request.getParameterMap());
  }

  /**
   * Gets allergies.
   *
   * @param request the request
   * @param response the response
   * @return the allergies list
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/allergieslookup")
  public Map<String, Object> getAllergies(HttpServletRequest request,
      HttpServletResponse response) {
    return ((ClinicalFormService) service).getAllergies(request.getParameterMap());
  }

  /**
   * Send prescription by email for given consultation id.
   *
   * @param id consultation id
   * @param requestBody request body
   */
  @PostMapping(value = URLRoute.SEND_PRESCRIPTION, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void sendPrescriptionEmail(@PathVariable T id, @RequestBody ModelMap requestBody)
      throws ParseException {
    ((ClinicalFormService)service).updateAndSendPrescriptionEmail(id, requestBody);
  }
}
