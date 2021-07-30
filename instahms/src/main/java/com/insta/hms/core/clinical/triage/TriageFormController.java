package com.insta.hms.core.clinical.triage;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.ClinicalFormController;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class TriageFormController.
 */
@RestController
@RequestMapping(URLRoute.TRIAGE_URL)
public class TriageFormController extends ClinicalFormController<Integer> {

  /**
   * Instantiates a new triage form controller.
   *
   * @param service the service
   */
  public TriageFormController(TriageFormService service) {
    super(service);
  }

  /** The service. */
  @LazyAutowired
  private TriageFormService service;

  /** The vital reading service. */
  @LazyAutowired
  private VitalReadingService vitalReadingService;
  
  @LazyAutowired
  private DoctorConsultationRepository doctorConsultationRepository;

  /**
   * Gets the triage index.
   *
   * @return the triage index
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = URLRoute.VIEW_INDEX_URL)
  public ModelAndView getTriageIndex() {
    return renderFlowUi("Triage", "v12", "withFlow", "opFlow", "triage", false);
  }

  /**
   * Gets the patient consultation details list.
   *
   * @param mrNo the mr no
   * @return the patient consultation details list
   */
  @GetMapping(value = URLRoute.TRIAGE_LIST)
  public Map<String, Object> getPatientConsultationDetailsList(@PathVariable("mrNo") String mrNo) {
    return ((TriageFormService) service).getPatientTriageDetailsList(mrNo);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormController#metadata()
   */
  @IgnoreConfidentialFilters
  @Override
  @GetMapping(value = URLRoute.TRIAGE_INDEPENDENT_META_DATA_URL)
  public Map<String, Object> metadata() {
    return super.metadata();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.FormController#view(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @IgnoreConfidentialFilters
  @Override
  @GetMapping(value = "/imageMarker/view")
  public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Map<String, Object> parameterMap = getParameterMap(request);
    BasicDynaBean bean = service.getImageMarkerByKey(parameterMap);

    String fieldName = (String) parameterMap.get("field_name");
    response.setContentType(service.getContentType(parameterMap, bean));

    OutputStream responseStream = null;

    try {
      responseStream = response.getOutputStream();
      StreamUtils.copy((InputStream) bean.get(fieldName), responseStream);
    } finally {
      if (null != responseStream) {
        responseStream.close();
      }
    }
  }
  
  @Override
  public ResponseEntity<Map<String, Object>> saveform(@PathVariable Integer id,
      @RequestBody ModelMap requestBody) throws ParseException {
    ResponseEntity<Map<String, Object>> response = super.saveform(id, requestBody);
    service.triggerEvents(doctorConsultationRepository.getVisitId(id), response.getBody());
    return response;
  }
}
