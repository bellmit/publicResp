package com.insta.hms.mdm;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterDetailsRestController.
 */
public class MasterDetailsRestController extends MasterRestController {

  /**
   * Instantiates a new master details rest controller.
   *
   * @param service the service
   */
  public MasterDetailsRestController(MasterService service) {
    super(service);
  }

  /**
   * Show.
   *
   * @param pkId the pk id
   * @return the response entity
   */
  @GetMapping(value = "{pkId}/show")
  public ResponseEntity<LinkedHashMap<String, Object>> show(
      @PathVariable(required = true, value = "pkId") Object pkId) {
    return new ResponseEntity<LinkedHashMap<String, Object>>(
        ((MasterDetailsService) getService()).show(pkId), HttpStatus.OK);
  }


  @Override
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> show(HttpServletRequest req,
      HttpServletResponse resp) {
    ResponseEntity<Map<String, Object>> re = super.show(req, resp);
    Map<String, Object> responseBody = re.getBody();
    responseBody
        .putAll(((MasterDetailsService) getService()).findDetailsByPk(req.getParameterMap()));
    return re;
  }


  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  protected ResponseEntity create(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    BasicDynaBean parentBean = jsonToBean(requestBody);
    Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = 
        ((MasterDetailsService) getService()).toBeansMap(requestBody, parentBean);
    ((MasterDetailsService) getService()).insertDetailsMap(parentBean, detailMapBean);
    return new ResponseEntity(HttpStatus.CREATED);
  }

  
  @Override
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ResponseEntity update(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    ModelAndView mav = new ModelAndView();
    BasicDynaBean parentBean = jsonToBean(requestBody);
    Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = 
        ((MasterDetailsService) getService()).toBeansMap(requestBody, parentBean);
    ((MasterDetailsService) getService()).updateDetailsMap(parentBean, detailMapBean);
    return new ResponseEntity(HttpStatus.OK);
  }

}
