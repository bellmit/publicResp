package com.insta.hms.core.clinical.ipemr;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.multiuser.MultiUserFormController;
import com.insta.hms.core.clinical.patientactivities.PatientActivitesListModel;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesHibernateRepository;
import com.insta.hms.mdm.notetypes.NoteTypesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class IpEmrFormController.
 *
 * @author sonam
 */
@RestController
@RequestMapping(URLRoute.IP_EMR_URL)
public class IpEmrFormController extends MultiUserFormController<String> {

  /** The repo. */
  @LazyAutowired
  PatientActivitiesHibernateRepository repo;

  /**
   * Instantiates a new ip emr form controller.
   *
   * @param service the service
   */
  public IpEmrFormController(IpEmrFormService service) {
    super(service);
  }

  /**
   * Gets the ip emr index.
   *
   * @return the ip emr index
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = URLRoute.VIEW_INDEX_URL)
  public ModelAndView getIpEmrIndex() {
    return renderFlowUi("IP EMR", "ipFlow", "withFlow", "ipFlow", "ipemr", true);
  }

  /**
   * Gets the patient visit list.
   *
   * @param mrNo the mr no
   * @return the patient visit list
   */
  @RequestMapping(value = "/patient/{mrNo}/list", method = RequestMethod.GET)
  public Map<String, Object> getPatientVisitList(@PathVariable("mrNo") String mrNo) {
    return ((IpEmrFormService) service).getPatientVisitList(mrNo);
  }

  /**
   * Gets the notes consumption view.
   *
   * @param patientId the patient id
   * @param request   the request
   * @param response  the response
   * @return the notes consumption view
   */
  @RequestMapping(value = "/visit/{patientId}/notesConsumptionView", method = RequestMethod.GET)
  public Map<String, Object> getNotesConsumptionView(@PathVariable("patientId") String patientId,
      HttpServletRequest request, HttpServletResponse response) {
    Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
    return ((IpEmrFormService) service).getPatNotesInfo(patientId, paramMap);
  }

  /**
   * Gets the sections saved status.
   *
   * @param patientId the patient id
   * @return the sections saved status
   */
  @RequestMapping(value = "/visit/{patientId}/sectionsSavedStatus", method = RequestMethod.GET)
  public Map<String, Object> getSectionsSavedStatus(@PathVariable String patientId) {
    return ((IpEmrFormService) service).getSectionsSavedStatus(patientId);
  }

  /**
   * Gets the sections count.
   *
   * @param patientId the patient id
   * @param requestBody the request body
   * @return the sections count
   */
  @PostMapping(value = "/visit/{patientId}/sectionsCount")
  public ResponseEntity<Map<String, Object>> getSectionsCount(
      @PathVariable(required = true, value = "patientId") String patientId,
      @RequestBody ModelMap requestBody) {
    return new ResponseEntity<>(
        ((IpEmrFormService) service).getSectionsCount(patientId, requestBody), HttpStatus.CREATED);
  }

  /**
   * Gets the prescription items.
   *
   * @param patientId the patient id
   * @param presType the pres type
   * @param searchQuery the search query
   * @param isDischargeMedication indicator for discharge medication section
   * @return the prescription items
   */
  @RequestMapping(value = "/visit/{patientId}/prescription/{presType}/list",
      method = RequestMethod.GET)
  public Map<String, Object> getPrescriptionItems(@PathVariable String patientId,
      @PathVariable String presType,
      @RequestParam(value = "is_prescribable", defaultValue = "true") Boolean invIsPrescribable,
      @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(required = false, value = "is_discharge_medication",
          defaultValue = "false") boolean isDischargeMedication) {
    return ((IpEmrFormService) service).getPrescriptionItems(presType, patientId, invIsPrescribable,
        searchQuery, isDischargeMedication);
  }

  /**
   * Gets the ALL prescription items.
   *
   * @param patientId the patient id
   * @param searchQuery the search query
   * @param isDischargeMedication the is discharge medication
   * @return the ALL prescription items
   */
  @RequestMapping(value = "/visit/{patientId}/prescription/items", method = RequestMethod.GET)
  public Map<String, Object> getALLPrescriptionItems(@PathVariable String patientId, @RequestParam(
      value = "search_query") String searchQuery, @RequestParam(required = false,
      value = "is_discharge_medication", defaultValue = "false") Boolean isDischargeMedication) {
    return ((IpEmrFormService) service).getALLPrescriptionItems(patientId, searchQuery,
        isDischargeMedication);
  }

  /**
   * Gets prescriptions for the visit.
   * 
   * @param visitId the visit id
   * @param isDischargeMedication indicator for discharge medication section
   * @return prescriptions for the visit
   */
  @RequestMapping(value = "/{visitId}/prescriptions", method = RequestMethod.GET)
  public Map<String, Object> getPrescriptionsForVisit(@PathVariable String visitId,
      @RequestParam(required = false, value = "is_discharge_medication",
          defaultValue = "false") Boolean isDischargeMedication) {
    return ((IpEmrFormService) service).getPrescriptionsForVisit(visitId, isDischargeMedication);
  }

  /**
   * Gets the favourite prescriptions.
   *
   * @param patientId the patient id
   * @param itemType the item type
   * @param searchQuery the search query
   * @param nonHospMedicine the non hosp medicine
   * @param pageNo the page no
   * @return the favourite prescriptions
   */
  @GetMapping(value = "/{patientId}/favouritePrescriptions")
  public Map<String, Object> getFavouritePrescriptions(@PathVariable String patientId,
      @RequestParam(required = false, value = "item_type") String itemType, @RequestParam(
          value = "search_query") String searchQuery, @RequestParam(required = false,
          value = "non_hosp_medicine") Boolean nonHospMedicine, @RequestParam(required = false,
          value = "page_no") Integer pageNo) {
    return ((IpEmrFormService) service).getDoctorFavouritePrescriptions(itemType, patientId,
        searchQuery, pageNo, nonHospMedicine);
  }

  /**
   * Package contents.
   *
   * @param packageId the package id
   * @return the map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/packageContents")
  public Map<String, Object> packageContents(
      @RequestParam(value = "package_id") Integer packageId) {
    return ((IpEmrFormService) service).packageContents(packageId);
  }

  /**
   * Mar setup.
   *
   * @param patientId      the patient id
   * @param prescriptionId the prescription id
   * @return the map
   */
  @GetMapping(value = "/{patientId}/marsetup/{prescriptionId}")
  public Map<String, Object> marSetup(@PathVariable(required = true) String patientId,
      @PathVariable(required = true) Integer prescriptionId) {
    return ((IpEmrFormService) service).marSetupDetails(patientId, prescriptionId);
  }

  /**
   * Save mar activity.
   *
   * @param patientId the patient id
   * @param activities the activities
   * @return the map
   */
  @PostMapping(value = "/visit/{patientId}/maractivities",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public PatientActivitesListModel saveMarActivities(
      @PathVariable(required = true) String patientId,
      @RequestBody PatientActivitesListModel activities) {
    return ((IpEmrFormService) service).saveMarActivities(patientId, activities.getActivities());
  }

  /**
   * Gets the mar activities.
   *
   * @param patientId the patient id
   * @param fromDate the from date
   * @param toDate the to date
   * @return the mar activities
   */
  @GetMapping(value = "/visit/{patientId}/maractivities")
  public Map<String, Object> getMarActivities(@PathVariable(required = true) String patientId,
      @RequestParam(required = true, value = "from") String fromDate,
      @RequestParam(required = true, value = "to") String toDate,
      @RequestParam(required = false, value = "prescription_id") Integer prescriptionId) {
    return ((IpEmrFormService) service).getMarActivities(patientId, fromDate, toDate,
        prescriptionId);
  }

  /**
   * Gets the medicine batch details for patient.
   *
   * @param patientId the patient id
   * @param medicineIds the medicine ids
   * @return the medicine batch details for patient
   */
  @GetMapping(value = "/{patientId}/medicineBatchDetailsForPatient")
  public Map<String, Object> getMedicineBatchDetailsForPatient(
      @PathVariable(required = true) String patientId, @RequestParam(required = true,
          value = "medicine_ids") List<Integer> medicineIds) {
    return ((IpEmrFormService) service).getMedicineBatchDetailsForPatient(patientId, medicineIds);
  }

  /**
   * Cancel Erx request.
   *
   * @param patientId the patient id
   * @return the map
   */
  @PostMapping(value = "/{patientId}/cancelERxRequest")
  public Map<String, Object> cancelERxRequest(@PathVariable String patientId) {
    return ((IpEmrFormService) service).cancelERxRequest(patientId);
  }

  @PostMapping(value = "/{patientId}/saveMarSetup/{prescriptionId}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> saveMarSetup(@PathVariable(required = true) String patientId,
      @PathVariable(required = true) Integer prescriptionId, @RequestBody ModelMap requestBody)
      throws ParseException {
    return ((IpEmrFormService) service).saveMarSetup(patientId, prescriptionId, requestBody);
  }

  @PostMapping(value = "/{patientId}/stopMedication/{prescriptionId}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> stopMedication(@PathVariable(required = true) String patientId,
      @PathVariable(required = true) Integer prescriptionId, @RequestBody ModelMap requestBody) {
    return ((IpEmrFormService) service).stopMedication(patientId, prescriptionId, requestBody);
  }
  
  @Override
  public ResponseEntity<Map<String, Object>> saveform(@PathVariable String id,
      @RequestBody ModelMap requestBody) throws ParseException {
    ResponseEntity<Map<String, Object>> response = super.saveform(id, requestBody);
    ((IpEmrFormService) service).triggerEvents(id, response.getBody());
    return response;
  }
  
  @Override
  public ResponseEntity<Map<String, Object>> saveSection(@PathVariable String id,
      @PathVariable Integer sectionId, @RequestBody ModelMap requestBody) {
    ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(
        ((IpEmrFormService) service).saveSection(id, sectionId, requestBody), HttpStatus.CREATED);
    ((IpEmrFormService) service).triggerEvents(id, response.getBody());
    return response;
  }
}
