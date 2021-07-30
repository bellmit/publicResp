package com.insta.hms.core.clinical.initialassessment;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.ClinicalFormController;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Map;

/**
 * The Class InitialAssessmentFormController.
 */
@RestController
@RequestMapping(URLRoute.INITIAL_ASSESSMENT_URL)
public class InitialAssessmentFormController extends ClinicalFormController<Integer> {

  @LazyAutowired
  private DoctorConsultationRepository doctorConsultationRepository;
  
  /**
   * Instantiates a new initial assessment form controller.
   *
   * @param service
   *          the service
   */
  public InitialAssessmentFormController(InitialAssessmentFormService service) {
    super(service);
  }

  /**
   * Gets the consultation index.
   *
   * @return the consultation index
   */
  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getConsultationIndex() {
    return renderFlowUi(
      "Initial Assessment", "v12", "withFlow", "opFlow", "initialassessment", false);
  }

  /**
   * Gets the patient consultation details list.
   *
   * @param mrNo
   *          the mr no
   * @return the patient consultation details list
   */
  @GetMapping(value = URLRoute.INITIAL_ASSESSMENT_LIST)
  public Map<String, Object> getPatientConsultationDetailsList(@PathVariable("mrNo") String mrNo) {
    return ((InitialAssessmentFormService) service).getPatientInitialAssessmentDetailsList(mrNo);
  }
  
  @Override
  public ResponseEntity<Map<String, Object>> saveform(@PathVariable Integer id,
      @RequestBody ModelMap requestBody) throws ParseException {
    ResponseEntity<Map<String, Object>> response = super.saveform(id, requestBody);
    service.triggerEvents(doctorConsultationRepository.getVisitId(id), response.getBody());
    return response;
  }
}
