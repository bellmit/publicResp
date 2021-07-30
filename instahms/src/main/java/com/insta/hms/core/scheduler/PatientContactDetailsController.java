package com.insta.hms.core.scheduler;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientContactDetailsController.
 */

@SuppressWarnings({ "rawtypes","unchecked" })
@RestController
@RequestMapping(URLRoute.SCHEDULER_CONTACT_DETAILS)
public class PatientContactDetailsController extends BaseRestController {

  /** The patient contact details service. */
  @LazyAutowired
  PatientContactDetailsService patientContactDetailsService;

  /**
   * Adds the contact.
   *
   * @param requestBody the request body
   * @return the map
   */
  @PostMapping(value = "/addContact")
  public Map<String, Object> addContact(@RequestBody ModelMap requestBody) {
    return patientContactDetailsService.insertContactDetails(requestBody);
  }


  /**
   * Update contact.
   *
   * @param requestBody the request body
   * @return the map
   */
  @PostMapping(value = "/updateContact")
  public Map<String, Object> updateContact(@RequestBody ModelMap requestBody) {
    return patientContactDetailsService.updateContactDetails(requestBody);
  }
  
  /**
   * Gets the contact.
   *
   * @param contactId the contact id
   * @return the contact
   */
  @IgnoreConfidentialFilters
  @GetMapping(value = "/getContact")
  public Map getContact(@RequestParam("contactId") String contactId) {
    return patientContactDetailsService.getContactDetails(Integer.parseInt(contactId));
  }
}
