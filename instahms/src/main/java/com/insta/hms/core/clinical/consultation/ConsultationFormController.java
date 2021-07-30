package com.insta.hms.core.clinical.consultation;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.ClinicalFormController;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author teja.
 *
 */
@RestController
@RequestMapping(URLRoute.CONSULTATION_URL)
public class ConsultationFormController extends ClinicalFormController<Integer> {

  public ConsultationFormController(ConsultationFormService service) {
    super(service);
  }

  @Autowired
  private BeanFactory beanFactory;

  @Autowired
  private AppointmentService appointmentService;
  
  @LazyAutowired
  private DoctorConsultationRepository doctorConsultationRepository;

  @Autowired
  public void setConsultationFormService() {
    this.service = beanFactory.getBean(ConsultationFormService.class,
        FormComponentsService.FormType.Form_CONS);
  }

  @IgnoreConfidentialFilters
  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getConsultationIndex() {
    return renderFlowUi("Consultation", "v12", "withFlow", "opFlow", "consultation", false);
  }

  @RequestMapping(value = "/patient/{mrNo}/list", method = RequestMethod.GET)
  public Map<String, Object> getPatientConsultationDetailsList(@PathVariable("mrNo") String mrNo) {
    return ((ConsultationFormService) service).getPatientConsultationDetailsList(mrNo);
  }

  @RequestMapping(value = "/visit/{patientId}/prescription/{presType}/list",
      method = RequestMethod.GET)
  public Map<String, Object> getPrescriptionItems(@PathVariable String patientId,
      @PathVariable String presType, @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(value = "is_prescribable", defaultValue = "true") Boolean invIsPrescribable,
      @RequestParam(required = false, value = "is_discharge_medication",
          defaultValue = "false") boolean isDischargeMedication) {
    return ((ConsultationFormService) service).getPrescriptionItems(presType, patientId,
        invIsPrescribable, searchQuery, isDischargeMedication);
  }

  @RequestMapping(value = "/visit/{patientId}/invConductionDate", method = RequestMethod.GET)
  public Map<String, Object> getPrescriptionInvList(@PathVariable String patientId,
      @RequestParam(value = "test_ids") List<String> idList) {
    return ((ConsultationFormService) service).invConductionDate(patientId, idList);
  }

  @RequestMapping(value = "/visit/{patientId}/prescription/items", method = RequestMethod.GET)
  public Map<String, Object> getALLPrescriptionItems(@PathVariable String patientId,
      @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(required = false, value = "is_discharge_medication",
          defaultValue = "false") Boolean isDischargeMedication) {
    return ((ConsultationFormService) service).getALLPrescriptionItems(patientId, searchQuery,
        isDischargeMedication);
  }

  @RequestMapping(value = "/{consId}/previousConsultations", method = RequestMethod.GET)
  public Map<String, Object> getPreviousConsultations(@PathVariable Integer consId,
      @RequestParam(required = false, value = "current_cons_reqd",
          defaultValue = "false") Boolean isCurrentConsRequired) {
    return ((ConsultationFormService) service).getPreviousConsultations(consId,
        isCurrentConsRequired);
  }

  @RequestMapping(value = "/{consId}/prescriptions", method = RequestMethod.GET)
  public Map<String, Object> getPrescriptions(@PathVariable Integer consId,
      @RequestParam(required = false, value = "is_discharge_medication",
          defaultValue = "false") Boolean isDischargeMedication) {
    return ((ConsultationFormService) service).getPrescriptions(consId, isDischargeMedication);
  }

  @GetMapping(value = "/{consId}/favouritePrescriptions")
  public Map<String, Object> getFavouritePrescriptions(@PathVariable Integer consId,
      @RequestParam(required = false, value = "item_type") String itemType,
      @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(required = false, value = "non_hosp_medicine") Boolean nonHospMedicine,
      @RequestParam(required = false, value = "page_no") Integer pageNo) {
    return ((ConsultationFormService) service).getDoctorFavouritePrescriptions(itemType, consId,
        searchQuery, pageNo, nonHospMedicine);
  }

  @PostMapping(value = "/prescriptionEstimatedAmount")
  public Map<String, Object> getPrescriptionsEstimatedAmount(@RequestBody ModelMap requestBody)
      throws SQLException {
    return ((ConsultationFormService) service).getPrescriptionsEstimatedAmount(requestBody);
  }

  @RequestMapping(value = "/{consId}/previousPrescriptions", method = RequestMethod.GET)
  public Map<String, Object> getPreviousPrescriptions(@PathVariable Integer consId,
      @RequestParam(required = false, value = "is_discharge_medication",
          defaultValue = "false") Boolean isDischargeMedication) {
    return ((ConsultationFormService) service).getPreviousPrescriptions(consId,
        isDischargeMedication);
  }

  /**
   * ResponseEntity.
   * @param consultationId the int
   * @param requestBody the ModelMap
   * @return ResponseEntity
   */
  @RequestMapping(value = "/{consultationId}/convert", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> convertVisit(
      @PathVariable(required = true, value = "consultationId") Integer consultationId,
      @RequestBody ModelMap requestBody) {
    return new ResponseEntity<>(
        ((ConsultationFormService) service).convertVisit(consultationId, 0, requestBody),
        HttpStatus.CREATED);
  }

  @PostMapping(value = "/{consId}/cancelERxRequest")
  public Map<String, Object> cancelERxRequest(@PathVariable Integer consId) {
    return ((ConsultationFormService) service).cancelERxRequest(consId);
  }

  @RequestMapping(value = "/{consId}/sectionsSavedStatus", method = RequestMethod.GET)
  public Map<String, Object> getSectionsSavedStatus(@PathVariable Integer consId) {
    return ((ConsultationFormService) service).getSectionsSavedStatus(consId);
  }

  /**
   * Response entity.
   * @param sectionItemId the map
   * @param requestBody  the ModelMap
   * @return ResponseEntity
   */
  @PostMapping(value = "/{consultationId}/sectionsCount")
  public ResponseEntity<Map<String, Object>> getSectionsCount(
      @PathVariable(required = true, value = "consultationId") Integer sectionItemId,
      @RequestBody ModelMap requestBody) {
    return new ResponseEntity<>(
        ((ConsultationFormService) service).getSectionsCount(sectionItemId, requestBody),
        HttpStatus.CREATED);
  }

  @IgnoreConfidentialFilters
  @Override
  @RequestMapping(value = "/commonConsultationDetails", method = RequestMethod.GET)
  public Map<String, Object> metadata() {
    return service.metadata();
  }

  @Override
  @RequestMapping(value = "/{consultationId}/metadata", method = RequestMethod.GET)
  public Map<String, Object> metadata(@PathVariable("consultationId") Integer consultationId) {
    return (service.metadata(consultationId));
  }

  @IgnoreConfidentialFilters
  @GetMapping(value = "/packageContents")
  public Map<String, Object> packageContents(@RequestParam("package_id") Integer packageId) {
    return ((ConsultationFormService) service).packageContents(packageId);
  }

  /**
   * view.
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/imageMarker/view")
  public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ConsultationFormService consultationFormService = ((ConsultationFormService) service);
    Map<String, Object> parameterMap = getParameterMap(request);
    BasicDynaBean bean = consultationFormService.getImageMarkerByKey(parameterMap);

    String fieldName = (String) parameterMap.get("field_name");
    response.setContentType(consultationFormService.getContentType(parameterMap, bean));

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
    appointmentService.pushConsultationAppointmentToRedis(id);
    ((ConsultationFormService) service).triggerEvents(doctorConsultationRepository.getVisitId(id),
        response.getBody());
    return response;
  }

  /**
   * Get Claims.
   * @param request the HttpServletRequest
   * @param response the HttpServletResponse
   * @param drugIds the list of integer
   * @param icdCodes list of string
   * @param allergies list of string
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/cims")
  public void getcims(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(required = false) List<Integer> drugIds,
      @RequestParam(required = false) List<String> icdCodes,
      @RequestParam(required = false) List<String> allergies) throws Exception {
    String xmlResponse = ((ConsultationFormService) service).getcimsDrugToDrugInteraction(drugIds,
        icdCodes, allergies);
    response.setContentType("text/xml");
    response.getWriter()
        .write("<?xml-stylesheet type=\"text/xsl\" href=\"" + request.getContextPath()
            + "/scripts/Cims/Monograph-Interaction-CDSDefault_withDuplicateAllergy.xsl\" ?>"
            + xmlResponse);
  }
}
