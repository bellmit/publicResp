package com.insta.hms.mdm.icdcodes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller("icdCodesController")
@RequestMapping(URLRoute.ICD_CODES_PATH)
public class IcdCodesController extends MasterController {

  @LazyAutowired
  private IcdCodesService service;
  
  /**
   * Instantiates a new ICD codes controller.
   *
   * @param service the service
   */
  public IcdCodesController(IcdCodesService service) {
    super(service, MasterResponseRouter.ICD_CODES_ROUTER);
  }
  
  /**
   * Gets the Get Patient Problem List.
   *
   * @param searchQuery the search query
   * @return the patient problem list
   */
  @GetMapping(value = "/patientProblems/search")
  public Map<String, Object> getPatientProblemList(
      @RequestParam(value = "search_query") String searchQuery,
      @RequestParam(value = "problem_code_type") String codeType) {
    return service.getPatientProblemList(searchQuery, codeType);
  }
}
