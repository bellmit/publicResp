package com.insta.hms.mdm.hospitalroles;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterDetailsRestController;

import org.springframework.http.HttpStatus;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(URLRoute.HOSPITAL_ROLE_MASTER)
public class HospitalRoleController extends MasterDetailsRestController {

  public HospitalRoleController(HospitalRoleService hospitalRoleService) {
    super(hospitalRoleService);
  }

  /**
   * Find hospital role based on filters.
   *
   * @param params
   *          filter map
   * @return map of hospital roles list
   */
  @GetMapping(value = URLRoute.FIND_BY_FILTER)
  public ResponseEntity<Map<String, Object>> findByFilters(
      @RequestParam Map<String, String> params) {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.putAll(((HospitalRoleService) getService()).findByFilters(params));

    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Show.
   *
   * @param roleId
   *          the role id
   * @return the response entity
   */
  @Override
  @GetMapping(value = "{roleId}/show")
  public ResponseEntity<LinkedHashMap<String, Object>> show(
      @PathVariable(required = true, value = "roleId") Object roleId) {
    return new ResponseEntity<LinkedHashMap<String, Object>>(
        ((HospitalRoleService) getService()).show(roleId), HttpStatus.OK);
  }

  @PostMapping(value = "/save", consumes = "application/json")
  public LinkedHashMap<String, Object> save(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    return ((HospitalRoleService) getService()).save(requestBody);
  }

  /**
   * Gets the index page.
   *
   * @return the index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getIndexPage() {
    //return new ModelAndView(URLRoute.HOSPITAL_ADMIN_INDEX_PAGE);
    return renderMasterUi("Master","hospitalAdminMasters");
  }

}
