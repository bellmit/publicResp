/**
 * 
 */

package com.insta.hms.api.controllers;

import com.insta.hms.api.models.APILoginResponse;
import com.insta.hms.api.services.CustomerService;
import com.insta.hms.api.services.CustomerService.LoginReqParameter;
import com.insta.hms.api.services.CustomerService.ReturnCode;
import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.URLRoute;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.ServletContextUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomerController.
 *
 * @author mohamedanees
 */

@Controller
@RequestMapping(URLRoute.API_CUSTOMER)
public class CustomerController extends BaseRestController {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

  /** The customer service. */
  @LazyAutowired
  private CustomerService customerService;

  /**
   * Login user.
   *
   * @param request the request
   * @param response the response
   * @return the API login response
   */
  @RequestMapping(value = "/login", method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
  public @ResponseBody APILoginResponse loginUser(HttpServletRequest request,
      HttpServletResponse response) {
    APILoginResponse loginResponse = new APILoginResponse();
    try {
      Map<LoginReqParameter, Object[]> paramMap = createLoginParamMap(request);
      loginResponse = customerService.doLogin(paramMap);
      postProcessLoginResponse(loginResponse, response);
    } catch (Exception ex) {
      logger.error("Exception while performing login", ex);
    }
    return loginResponse;
  }

  /**
   * Post process login response.
   *
   * @param loginResponse the login response
   * @param response the response
   */
  private void postProcessLoginResponse(APILoginResponse loginResponse,
      HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-cache");
    response.setStatus(ReturnCode.getHTTPResponseStatus(loginResponse));
  }

  /**
   * Creates the login param map.
   *
   * @param request the request
   * @return the map
   * @throws Exception the exception
   */
  private Map<LoginReqParameter, Object[]> createLoginParamMap(HttpServletRequest request)
      throws Exception {
    Map<LoginReqParameter, Object[]> processedMap = new HashMap<LoginReqParameter, Object[]>();
    String userCredential = ApiUtil.getCredentials(request);
    String hospitalName = request.getParameter("hospital_name");
    Map<String, Object> sessionMap = ServletContextUtil
        .getContextParametersMap(request.getServletContext());
    processedMap.put(LoginReqParameter.SESSION_MAP, new Object[] { sessionMap });
    processedMap.put(LoginReqParameter.CREDENTIALS, new String[] { userCredential });
    processedMap.put(LoginReqParameter.HOSPITAL_NAME, new String[] { hospitalName });
    return processedMap;

  }

}
