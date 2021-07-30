package com.insta.hms.documents;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.annotations.LazyAutowired;
import com.lowagie.text.DocumentException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class GenerateRegistrationCardController.
 */
@Controller
@RequestMapping("generateregistrationcard")
public class GenerateRegistrationCardController extends BaseController {

  /** The service. */
  @LazyAutowired
  private GenerateRegistrationCardService service;

  /**
   * Execute.
   *
   * @param request the request
   * @param response the response
   * @return the model and view
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   */
  @IgnoreConfidentialFilters
  @RequestMapping(value = "/getcard", method = RequestMethod.GET)
  public ModelAndView execute(HttpServletRequest request, HttpServletResponse response)
      throws IOException, DocumentException {

    Map<String, String[]> params = request.getParameterMap();
    Map<String, Object> requestMap = new HashMap<>();
    service.execute(params, requestMap, response);
    return new ModelAndView().addAllObjects(requestMap);
  }

}
