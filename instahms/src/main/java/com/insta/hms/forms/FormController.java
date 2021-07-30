package com.insta.hms.forms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class FormController.
 *
 * @param <T> the generic type
 */
public abstract class FormController<T> extends BaseRestController {

  /** The service. */
  protected FormService service;

  /**
   * Instantiates a new form controller.
   */
  public FormController() {}

  /**
   * Instantiates a new form controller.
   *
   * @param service the service
   */
  public FormController(FormService service) {
    this.service = service;
  }

  /**
   * Show details.
   *
   * @param id the id
   * @return the map
   */
  @GetMapping(value = UrlRoute.SHOW_URL)
  @IgnoreConfidentialFilters
  public Map<String, Object> showDetails(@PathVariable T id) {
    return service.loadForm(id);
  }

  /**
   * Gets the section.
   *
   * @param id the id
   * @param sectionId the section id
   * @param formId the form id
   * @param changeForm the change form
   * @return the section
   */
  @GetMapping(value = UrlRoute.GET_SECTION_URL)
  public Map<String, Object> getSection(@PathVariable T id, @PathVariable Integer sectionId,
      @RequestParam(required = false, value = "form_id") Integer formId, @RequestParam(
          required = false, value = "change_form") Boolean changeForm) {
    if (changeForm != null && changeForm) {
      return service.getSectionDetails(id, sectionId, formId, changeForm);
    } else {
      return service.getSectionDetails(id, sectionId);
    }
  }

  /**
   * Save section.
   *
   * @param id the id
   * @param sectionId the section id
   * @param requestBody the request body
   * @return the response entity
   * @throws JsonProcessingException the json processing exception
   */
  @PostMapping(value = UrlRoute.AUTO_SAVE_SECTION_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> saveSection(@PathVariable T id,
      @PathVariable Integer sectionId, @RequestBody ModelMap requestBody)
      throws JsonProcessingException {
    String autoSaveId = service.saveSection(id, sectionId, requestBody);
    Map<String, Object> map = new HashMap<>();
    map.put("auto_save_section_id", autoSaveId);
    return new ResponseEntity<>(map, HttpStatus.CREATED);
  }

  /**
   * Saveform.
   *
   * @param id the id
   * @param requestBody the request body
   * @return the response entity
   * @throws ParseException the parse exception
   */
  @PostMapping(value = UrlRoute.FORM_SAVE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> saveform(@PathVariable T id,
      @RequestBody ModelMap requestBody) throws ParseException {
    return new ResponseEntity<>(service.save(id, requestBody), HttpStatus.CREATED);
  }

  /**
   * Saveform without section and care team data.
   *
   * @param id the id
   * @param requestBody the request body
   * @return the response entity
   * @throws ParseException the parse exception
   */
  @PostMapping(value = UrlRoute.FINALIZE_FORM, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> finalizeForm(@PathVariable T id,
      @RequestBody ModelMap requestBody) throws ParseException {
    return new ResponseEntity<>(service.finalizeForm(id, requestBody), HttpStatus.CREATED);
  }

  /**
   * Reopen form.
   *
   * @param id the id
   * @param requestBody the request body
   * @return the map
   */
  @PostMapping(value = UrlRoute.FORM_REOPEN)
  public Map<String, Object> reopenForm(@PathVariable T id, @RequestBody ModelMap requestBody) {
    return service.reopenForm(id, requestBody);
  }

  /**
   * Metadata.
   *
   * @param id the id
   * @return the map
   */
  @GetMapping(value = UrlRoute.DEPENDENT_META_DATA_URL)
  public Map<String, Object> metadata(@PathVariable T id) {
    return service.metadata(id);
  }

  /**
   * Metadata.
   *
   * @return the map
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = UrlRoute.INDEPENDENT_META_DATA_URL)
  public Map<String, Object> metadata() {
    return service.metadata();
  }

  /**
   * Save custom form.
   *
   * @param requestBody the request body
   * @return the response entity
   * @throws JsonProcessingException the json processing exception
   */
  @PostMapping(value = UrlRoute.SAVE_CUSTOM_FORM, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Integer> saveCustomForm(@RequestBody ModelMap requestBody)
      throws JsonProcessingException {
    return new ResponseEntity<>(service.saveCustomForm(requestBody), HttpStatus.CREATED);
  }

  /**
   * Change form.
   *
   * @param id the id
   * @param formId the form id
   * @return the map
   */
  @GetMapping(value = UrlRoute.CHANGE_FORM)
  public Map<String, Object> changeForm(@PathVariable T id, @PathVariable Integer formId) {
    return service.changeForm(id, formId);
  }

  
  /**
   * View.
   *
   * @param request the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
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
}
