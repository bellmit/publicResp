package com.insta.hms.core.clinical.multiuser;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.clinical.forms.ClinicalFormController;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.Map;

/**
 * The Class MultiUserFormController.
 *
 * @author sonam
 * @param <T> the generic type
 */
public abstract class MultiUserFormController<T> extends ClinicalFormController<T> {

  
  /**
   * Instantiates a new multi user form controller.
   *
   * @param service the service
   */
  public MultiUserFormController(MultiUserFormService service) {
    super(service);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormController#saveSection(java.lang.Object,
   * java.lang.Integer, org.springframework.ui.ModelMap)
   */
  @Override
  @PostMapping(value = URLRoute.AUTO_SAVE_SECTION_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> saveSection(@PathVariable T id,
      @PathVariable Integer sectionId, @RequestBody ModelMap requestBody) {
    return new ResponseEntity<>(((MultiUserFormService) service).saveSection(id, sectionId,
        requestBody), HttpStatus.CREATED);
  }


  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormController#saveform(java.lang.Object,
   * org.springframework.ui.ModelMap)
   */
  @Override
  @PostMapping(value = URLRoute.FORM_SAVE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> saveform(@PathVariable T id,
      @RequestBody ModelMap requestBody) throws ParseException {
    return new ResponseEntity<>(((MultiUserFormService) service).save(id, requestBody),
        HttpStatus.CREATED);
  }


  /**
   * Save care team.
   *
   * @param id the id
   * @param requestBody the request body
   * @return the response entity
   */
  @PostMapping(value = URLRoute.SAVE_CARE_TEAM_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> saveCareTeam(@PathVariable T id,
      @RequestBody ModelMap requestBody) {
    return new ResponseEntity<>(((MultiUserFormService) service).saveCareTeam(id, requestBody),
        HttpStatus.CREATED);
  }

  /**
   * Gets the sections lock.
   *
   * @param patientId the patient id
   * @param sectionId the section id
   * @return the sections lock
   */
  // Get sections Lock and push all locked sections
  @GetMapping(value = "/visit/{patientId}/sectionslock/{sectionId}")
  public boolean getSectionsLock(@PathVariable String patientId, @PathVariable Integer sectionId) {
    return ((MultiUserFormService) service).getSectionsLock(patientId, sectionId);
  }

  /**
   * Delete the section lock.
   *
   * @param patientId the patient id
   * @param sectionId the section id
   */
  @DeleteMapping(value = "/visit/{patientId}/sectionslock")
  public void deleteSectionLock(@PathVariable String patientId,
      @RequestParam(value = "section_id") Integer sectionId) {
    ((MultiUserFormService) service).deleteSectionLock(sectionId, patientId);
  }

}
