package com.insta.hms.mdm;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.exception.EntityNotFoundException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterRestController.
 */
public abstract class MasterRestController extends BaseRestController {

  /** The service. */
  private MasterService service;

  /** The converter. */
  @Autowired
  private ReferenceDataConverter converter;

  /**
   * Instantiates a new master rest controller.
   *
   * @param service the service
   */
  public MasterRestController(MasterService service) {
    this.service = service;
  }

  /**
   * List.
   *
   * @param request the request
   * @param response the response
   * @return the response entity
   */
  @RequestMapping(value = { "/list", "" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> list(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map paramMap = request.getParameterMap();
    PagedList pagedList = service.search(paramMap);
    responseMap.put("paged_list", pagedList);
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }

  /**
   * Show.
   *
   * @param req the req
   * @param resp the resp
   * @return the response entity
   */
  @RequestMapping(value = "/show", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> show(HttpServletRequest req,
      HttpServletResponse resp) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, String[]> params = req.getParameterMap();
    BasicDynaBean bean = service.findByPk(params, true);
    if (bean == null) {
      throw new EntityNotFoundException(new String[] { service.getRepository().getBeanName(),
          service.getRepository().getKeyColumn(),
          req.getParameter(service.getRepository().getKeyColumn()) });
    }
    responseMap.put("bean", bean.getMap());
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }

  /**
   * Creates the.
   *
   * @param req the req
   * @param resp the resp
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  protected ResponseEntity create(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    BasicDynaBean bean = jsonToBean(requestBody);
    service.insert(bean);
    return new ResponseEntity(bean.getMap(), HttpStatus.CREATED);
  }

  /**
   * Update.
   *
   * @param req the req
   * @param resp the resp
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/update", method = RequestMethod.POST)
  protected ResponseEntity update(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    BasicDynaBean bean = jsonToBean(requestBody);
    service.update(bean);
    return new ResponseEntity(bean.getMap(), HttpStatus.OK);
  }

  /**
   * Mark invalid.
   *
   * @param req the req
   * @param resp the resp
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/markInvalid", method = RequestMethod.POST)
  protected ResponseEntity markInvalid(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    BasicDynaBean bean = jsonToBean(requestBody);
    service.delete(bean);
    return new ResponseEntity(bean.getMap(), HttpStatus.OK);
  }

  /**
   * Delete.
   *
   * @param req the req
   * @param resp the resp
   * @param requestBody the request body
   * @return the response entity
   */
  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  protected ResponseEntity delete(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    BasicDynaBean bean = jsonToBean(requestBody);
    service.delete(bean, true);
    return new ResponseEntity(HttpStatus.OK);
  }

  /**
   * Lookup.
   *
   * @param request the request
   * @return the response entity
   */
  @RequestMapping(value = "/lookup", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> lookup(HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, String[]> parameters = request.getParameterMap();
    String filterText = (null != parameters && parameters.containsKey("filterText")) ? parameters
        .get("filterText")[0] : null;
    List<BasicDynaBean> searchSet = (null != filterText) ? service.autocomplete(filterText,
        parameters) : service.lookup(true);
    responseMap.put("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    responseMap.put("listSize", searchSet.size());
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }

  /**
   * Filtered List.
   *
   * @param request the request
   * @return the response entity
   */
  @RequestMapping(value = { "/filteredlist" }, method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> filteredList(HttpServletRequest request) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    Map<String, String[]> parameters = request.getParameterMap();
    String filterText = (null != parameters && parameters.containsKey("filterText")) ? parameters
        .get("filterText")[0] : "";
    PagedList pagedList = service.filter(parameters, filterText);
    responseMap.put("paged_list", pagedList);
    return new ResponseEntity<Map<String, Object>>(responseMap, HttpStatus.OK);
  }


  /**
   * Json to bean.
   *
   * @param requestBody the request body
   * @return the basic dyna bean
   */
  public BasicDynaBean jsonToBean(ModelMap requestBody) {
    return service.toBean(requestBody);
  }

  /**
   * Gets the service.
   *
   * @return the service
   */
  public MasterService getService() {
    return service;
  }

}
