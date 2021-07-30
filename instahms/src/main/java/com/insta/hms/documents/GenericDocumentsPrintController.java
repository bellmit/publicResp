package com.insta.hms.documents;

import com.insta.hms.common.BaseController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsPrintController.
 */
public abstract class GenericDocumentsPrintController extends BaseController {

  /** The service. */
  private GenericDocumentsPrintService service;

  /**
   * Instantiates a new generic documents print controller.
   *
   * @param service the service
   */
  public GenericDocumentsPrintController(GenericDocumentsPrintService service) {
    this.service = service;
  }

  /**
   * Prints the.
   *
   * @param request the request
   * @param response the response
   * @return the model and view
   * @throws Exception the exception
   */
  @RequestMapping(value = "/print", method = RequestMethod.GET)
  public ModelAndView print(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    Map<String, String[]> params = request.getParameterMap();
    Map<String, Object> requestMap = new HashMap<>();
    service.print(params, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

}
