package com.insta.hms.mdm.vitals;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/** The Class VitalsController. */
@RestController("vitalsController")
@RequestMapping(URLRoute.VITALS_MASTER_PATH)
public class VitalsController extends MasterDetailsRestController {

  /**
   * Instantiates a new vitals controller.
   *
   * @param service
   *          the service
   */
  public VitalsController(VitalsService service) {
    super(service);
  }

  /**
   * Gets the vital index page.
   *
   * @return the vital index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getVitalIndexPage() {
    return renderMasterUi("Master", "hospitalAdminMasters");
  }

  /**
   * Gets the departmentwise applicability.
   *
   * @param departmentId          the department id
   * @param visitType the visit type
   * @return the departmentwise applicability
   */
  @GetMapping(value = "/getdepartmentwiseapplicability")
  public Map<String, Object> getDepartmentwiseApplicability(
      @RequestParam(required = true, name = "department_id") String departmentId,
      @RequestParam(required = true, name = "visit_type") String visitType) {
    return ((VitalsService) getService()).getDepartmentwiseApplicability(departmentId,visitType);
  }

  /**
   * Gets the centerwise applicability.
   *
   * @param centerId          the center id
   * @param visitType the visit type
   * @return the centerwise applicability
   */
  @GetMapping(value = "/getcenterwiseapplicability")
  public Map<String, Object> getcenterwiseApplicability(
      @RequestParam(required = true, name = "center_id") int centerId,
      @RequestParam(required = true, name = "visit_type") String visitType) {
    return ((VitalsService) getService()).getCenterwiseApplicability(centerId,visitType);
  }

  /**
   * Gets the all params.
   *
   * @return the all params
   */
  @GetMapping(value = "/getallparams")
  public Map<String, Object> getallParams() {
    Map<String, Object> response = new HashMap<>();
    ((VitalsService) getService()).listAll();
    response.put("params",
        ConversionUtils.listBeanToListMap(((VitalsService) getService()).listAll()));
    return response;
  }

  
  /**
   * Gets the departments.
   *
   * @return the departments
   */
  @GetMapping(value = "/getdepartments")
  public Map<String, Object> getDepartments() {
    return ((VitalsService) getService()).getDepartments();
  }

  /**
   * Save.
   *
   * @param requestBody          the request body
   * @return the map
   */
  @PostMapping(value = "/save")
  public Map<String, Object> save(@RequestBody ModelMap requestBody) {
    return ((VitalsService) getService()).save((Map<String, Object>) requestBody);
  }
  
  @GetMapping(value = "/getUserCenters")
  public Map<String, Object> getUserCenters(HttpServletRequest request) {
    Map<String, String[]> parameters = request.getParameterMap();
    return ((VitalsService) getService()).getLoggedInCenters(parameters);
  }

}
