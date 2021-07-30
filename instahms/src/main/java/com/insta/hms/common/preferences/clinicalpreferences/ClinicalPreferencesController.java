package com.insta.hms.common.preferences.clinicalpreferences;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * The Class ClinicalPreferencesController.
 */
@RestController
@RequestMapping("/clinicalpreferences")
public class ClinicalPreferencesController extends BaseController {

  /** The service. */
  @LazyAutowired
  private ClinicalPreferencesService service;

  /**
   * Name.
   *
   * @return the model and view
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/index")
  public ModelAndView name() {
    return renderMasterUi("Master", "clinicalMaster", true);
  }

  /**
   * Gets the clinical preferences.
   *
   * @return the clinical preferences
   */
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  @GetMapping(value = "")
  public Map<String, Object> getClinicalPreferences() {
    return service.getClinicalPreferences().getMap();
  }

  /**
   * Save clinical preferences.
   *
   * @param requestBody the request body
   * @return the response entity
   */
  @PutMapping(value = "")
  public ResponseEntity<Map<String, Object>> saveClinicalPreferences(
      @RequestBody ModelMap requestBody) {
    return new ResponseEntity<>(service.update(requestBody), HttpStatus.CREATED);
  }

}
