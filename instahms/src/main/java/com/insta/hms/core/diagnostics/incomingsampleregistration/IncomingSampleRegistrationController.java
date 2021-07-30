package com.insta.hms.core.diagnostics.incomingsampleregistration;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class IncomingSampleRegistrationController.
 * 
 * @author yashwant
 */
@RestController
@RequestMapping("/incomingsampleregistration")
public class IncomingSampleRegistrationController extends BaseController {

  /** The isr service. */
  @Autowired
  private IncomingSampleRegistrationService isrService;

  /**
   * Gets the consultation index.
   *
   * @return the consultation index
   */
  @IgnoreConfidentialFilters
  @GetMapping("/index")
  public ModelAndView getConsultationIndex() {
    return renderMasterUi("Update Patient Details", "isr", false);
  }

  /**
   * Gets the patient details.
   *
   * @param incomingVisitId
   *          the incoming visit id
   * @return the patient details
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/incomingVisitId/{incomingVisitId}", method = RequestMethod.GET)
  public ResponseEntity<Map<Object, Object>> getPatientDetails(
      @PathVariable(required = true, value = "incomingVisitId") String incomingVisitId) {
    BasicDynaBean bean = isrService.getPatientDetails(incomingVisitId);
    int centerId = (Integer) bean.get("center_id");
    String countryCode = isrService.getCountryCode(centerId);
    Map<Object, Object> responseMap = new HashMap<>();
    responseMap.putAll(bean.getMap());
    responseMap.put("country_code", countryCode);

    return new ResponseEntity<>(responseMap, HttpStatus.OK);

  }

  /**
   * Update.
   *
   * @param incomingVisitId
   *          the incoming visit id
   * @param requestBody
   *          the request body
   * @return the response entity
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @RequestMapping(value = "/incomingVisitId/{incomingVisitId}", method = RequestMethod.POST)
  public ResponseEntity<Map> update(
      @PathVariable(required = true, value = "incomingVisitId") String incomingVisitId,
      @RequestBody ModelMap requestBody) {
    requestBody.put("incoming_visit_id", incomingVisitId);
    BasicDynaBean bean = isrService.toBean(requestBody);
    Map keys = new HashMap<>();
    keys.put("incoming_visit_id", incomingVisitId);
    int centerId = (Integer) bean.get("center_id");
    String countryCode = isrService.getCountryCode(centerId);
    Map responseMap = new HashMap<>();
    responseMap.putAll(bean.getMap());
    responseMap.put("country_code", countryCode);
    int updateCount = isrService.update(bean, keys);
    if (updateCount > 0) {
      return new ResponseEntity<>(responseMap, HttpStatus.CREATED);
    } else {
      keys.clear();
      keys.put("error", "Update Failed");
      return new ResponseEntity<>(keys, HttpStatus.BAD_REQUEST);
    }
  }
}
