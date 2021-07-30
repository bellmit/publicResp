package com.insta.mhms.patient.changepassword;

import com.bob.hms.common.DataBaseUtil;
import com.insta.instaapi.common.JsonProcessor;
import flexjson.JSONSerializer;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mohammed.r
 */
public class ChangePasswordAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(ChangePasswordAction.class);

  /**
   * method to update password.
   * @param mapping mapping parameter
   * @param form form paramter
   * @param request request object
   * @param response response object
   * @return returns action forward
   * @throws IOException Signals that an I/O exception has occurred
   * @throws ServletException may throw Servlet Exception
   * @throws SQLException may throw SQL Exception
   */
  public ActionForward updatePassword(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    HttpSession session = request.getSession(false);
    Boolean statusSuccess = false;
    boolean editPassword = false;
    response.setContentType("application/json");
    Map<String, Object> statusSuccesssMap = new HashMap<String, Object>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    // response.setHeader("Access-Control-Allow-Origin", "*");
    String successMsg = "";
    String sesHospitalId = (String) session.getAttribute("sesHospitalId");
    if (sesHospitalId == null || "".equals(sesHospitalId)) {
      successMsg = "Session is expired, please login again";
      logger.info("Session is expired, please login again");
      statusSuccesssMap.put("return_code", "1001");
      statusSuccesssMap.put("return_message", successMsg);
      logger.info("sending the response back to the requesting server");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(js.deepSerialize(statusSuccesssMap));
      response.flushBuffer();
      return null;
    }
    logger.info("checking for empty parameters....");
    String userid = (String) session.getAttribute("userId");
    String newPassword = request.getParameter("mobile_new_password");
    String oldPassword = request.getParameter("mobile_old_password");
    if ((userid == null)
        || userid.isEmpty()
        || (newPassword == null)
        || newPassword.isEmpty()
        || oldPassword == null
        || oldPassword.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      statusSuccesssMap.put("return_code", "1002");
      statusSuccesssMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(statusSuccesssMap));
      response.flushBuffer();
      return null;
    }
    if (newPassword.length() > 6) {
      successMsg = "Password length should not be more than 6";
      statusSuccesssMap.put("return_code", "1023");
      statusSuccesssMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(statusSuccesssMap));
      response.flushBuffer();
      return null;
    }
    Connection con = null;
    try {
      con = com.insta.instaapi.common.DbUtil.getConnection(sesHospitalId);
      String existingPassword = ChangePasswordDAO.getPatientPasswordDetails(con, userid);

      if (oldPassword.trim().equals(existingPassword.trim())) {

        if (newPassword.trim().equals(existingPassword.trim())) {
          // statusSuccesssMap.put("password_status", "failure");
          successMsg = "Old and new password are same.";
          statusSuccesssMap.put("return_code", "1021");
          statusSuccesssMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } else {

          editPassword = ChangePasswordDAO.updatePatientPassword(con, userid, newPassword);
          // statusSuccesssMap.put("password_status", "success");
          successMsg = "Success";
          statusSuccesssMap.put("return_code", "2001");
          statusSuccesssMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_OK);
        }
      } else {

        // statusSuccesssMap.put("password_status", "failure");
        successMsg = "Old password is not matching with the existing password";
        statusSuccesssMap.put("return_code", "1022");
        statusSuccesssMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(js.deepSerialize(statusSuccesssMap));
    response.flushBuffer();
    return null;
  }
}
