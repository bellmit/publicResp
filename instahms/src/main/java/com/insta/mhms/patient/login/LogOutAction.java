package com.insta.mhms.patient.login;

import com.bob.hms.common.LoginAction;
import com.insta.instaapi.common.JsonProcessor;
import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 */
public class LogOutAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  /**
   * Logout method.
   * @param mapping mapping parameter
   * @param form form parameter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   */
  public ActionForward logout(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException {
    logger.info("getting registration related master data");

    Map<String, Object> responseMap = new HashMap<String, Object>();
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    HttpSession session = request.getSession(false);
    if (session != null) {
      String sesHospitalId = (String) session.getAttribute("sesHospitalId");
      String successMsg = "";
      String returnCode = "";
      if (sesHospitalId == null || "".equals(sesHospitalId)) {
        successMsg = "Session is expired, please login again";
        logger.info("Session is expired, please login again");
        responseMap.put("return_code", "1001");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        session.invalidate();
        responseMap.put("return_message", "Success");
        responseMap.put("return_code", "2001");
      }
    } else {
      responseMap.put("return_message", "Failed logging out");
      responseMap.put("return_code", "1021");
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    JSONSerializer js = JsonProcessor.getJSONParser();
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();

    return null;
  }
}
