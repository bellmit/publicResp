package com.insta.hms.integration.practo.common;

import com.insta.hms.common.http.HttpResponse;
import com.insta.hms.integration.practo.api.util.HttpClientUtil;
import com.insta.hms.integration.practo.api.util.URLConstants;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class CityLocalityMappingAction.
 *
 * @author insta
 */
public class CityLocalityMappingAction extends Action {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(CityLocalityMappingAction.class);

  /**
   * Excecute method of the Action.
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    String cityId = request.getParameter("city_id");
    String urlParams = String.format("city_id=%s", cityId);

    Map<String, String> headerMap = new HashMap<String, String>();
    headerMap.put("Accept", "application/json");

    HttpResponse httpResponse = HttpClientUtil
        .sendHttpGetRequest(URLConstants.PRACTO_FABRIC_API_LOCALITIES, headerMap, urlParams);

    String res = httpResponse.getMessage();

    response.setContentType("application/x-json");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(res);
    response.flushBuffer();

    return null;
  }

}