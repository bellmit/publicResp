package com.insta.hms.mdm.practitionertypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(URLRoute.PRACTIONER_TYPE_MASTER_PATH)
public class PractitionerTypeController extends MasterRestController {

  @LazyAutowired private PractitionerTypeService service;

  @LazyAutowired private PractitionerTypeMappingsService mappingService;

  public PractitionerTypeController(PractitionerTypeService service) {
    super(service);
    // TODO Auto-generated constructor stub
  }

  /**
   * create mapping method.
   * @param req request
   * @param resp response
   * @param requestBody request body
   * @return response
   */
  @RequestMapping(
      value = "/createmapping",
      method = RequestMethod.POST,
      consumes = "application/json"
  )
  protected ResponseEntity createMapping(
      HttpServletRequest req, HttpServletResponse resp, @RequestBody ModelMap requestBody) {
    BasicDynaBean bean = mappingService.toBean(requestBody);
    mappingService.insert(bean);
    return new ResponseEntity(HttpStatus.CREATED);
  }

  /**
   * update mapping method.
   * @param req request
   * @param resp response
   * @param requestBody request body
   * @return response
   */
  @RequestMapping(value = "/updatemapping", method = RequestMethod.POST)
  protected ResponseEntity updateMapping(
      HttpServletRequest req, HttpServletResponse resp, @RequestBody ModelMap requestBody) {
    mappingService.update(requestBody);
    return new ResponseEntity(HttpStatus.OK);
  }
}
