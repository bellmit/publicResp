package com.insta.instaapi.customer.changepassword;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.usermanager.PasswordEncoder;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserBO;
import com.insta.hms.usermanager.UserDAO;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ServletContextUtil;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChangePasswordAction extends DispatchAction {
  
  private static final PatientDetailsDAO patDetailsDao = new PatientDetailsDAO();
  private static final UserDAO userDao = new UserDAO();
  static Logger logger = LoggerFactory.getLogger(ChangePasswordAction.class);
  private static final JSONSerializer js = JsonProcessor.getJSONParser();

  /**
   * Change password for API User.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  public ActionForward updatePassword(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException {
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    response.setContentType("application/json");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Cache-Control", "no-cache");
    String successMsg = "";
    Map<String, Object> responseMap = new HashMap<String, Object>();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    boolean isAValidRequest = false;
    Map sessionParameters = null;
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }
    if (!isAValidRequest) {
      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      responseMap.put("return_code", "1001");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    String userId = request.getParameter("user_id");
    if ((boolean) sessionParameters.get("patient_login")) {
      userId = (String) sessionParameters.get("customer_user_id");
    }
    String newPassword = request.getParameter("new_password");
    String oldPassword = request.getParameter("old_password");
    if ((userId == null || userId.isEmpty()) || (oldPassword == null || oldPassword.isEmpty())
        || (newPassword == null || newPassword.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    Connection con = null;
    String hospitalName = (String) sessionParameters.get("hospital_name");
    if (isAValidRequest) {
      con = com.insta.instaapi.common.DbUtil.getConnection(hospitalName);
      logger.info("getting connection object" + con + "----" + hospitalName);
      con.setAutoCommit(false);
      User userBean = userDao.getUser(con, userId);
      BasicDynaBean patientBean = null;
      if (userBean == null) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("mr_no", userId);
        keys.put("mobile_password", oldPassword);
        patientBean = patDetailsDao.findByKey(con, keys);
        if (patientBean == null) {
          successMsg = "Invalid User ID / Password";
          responseMap.put("return_code", "1021");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        } else {
          if (newPassword.length() > 6) {
            responseMap.put("return_code", "1020");
            responseMap.put("return_message", "Password can not be more than 6 characters");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
          Map<String, Object> columndata = new HashMap<String, Object>();
          columndata.put("mobile_password", newPassword);
          int result = patDetailsDao.update(con, columndata, keys);
          if (result > 0) {
            con.commit();
            con.close();
            successMsg = "Success";
            responseMap.put("return_code", "2001");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          } else {
            con.rollback();
            con.close();
            successMsg = "Failed to change the password. " + result;
            responseMap.put("return_code", "1023");
            responseMap.put("return_message", successMsg);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.info("sending the response back to the requesting server");
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
          }
        }
      }
      String newPasswordStr = null;
      String oldPasswordStr = null;
      Boolean isEncrypted = (Boolean) userBean.isEncrypted();
      if (isEncrypted) {
        newPasswordStr = PasswordEncoder.encode(newPassword);
        oldPasswordStr = PasswordEncoder.encode(oldPassword);
      } else {
        newPasswordStr = newPassword;
        oldPasswordStr = oldPassword;
      }
      BasicDynaBean bean = userDao.getUserBean(con, userId);
      String currentPassword = (String) userBean.getPassword();
      if (!PasswordEncoder.matches(con, oldPassword, currentPassword, bean)) {
        successMsg = "Old password is not matching with the existing password";
        responseMap.put("return_code", "1022");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        logger.info("sending the response back to the requesting server");
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
      if (PasswordEncoder.matches(con, oldPassword, newPassword, bean)) {
        successMsg = "New password is same as the existing password";
        responseMap.put("return_code", "1025");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        logger.info("sending the response back to the requesting server");
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }

      UserBO user = new UserBO();
      String msg = user.checkPasswordStrength(con, newPassword);
      if (msg == null) {
        msg = user.checkPasswordFrequency(con, newPassword, userId);
      }
      if (msg == null) {
        String result = userDao.changePassword(con, userId, currentPassword, newPasswordStr);
        if (result.equalsIgnoreCase("success")) {
          user.updatePasswordHistory(con, userId, currentPassword);
          con.commit();
          con.close();
          successMsg = "Success";
          responseMap.put("return_code", "2001");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_OK);
          logger.info("sending the response back to the requesting server");
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        } else {
          con.rollback();
          con.close();
          successMsg = "Failed to change the password. " + result;
          responseMap.put("return_code", "1023");
          responseMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          logger.info("sending the response back to the requesting server");
          response.getWriter().write(js.deepSerialize(responseMap));
          response.flushBuffer();
          return null;
        }
      } else {
        successMsg = "Failed to change the password. " + msg;
        responseMap.put("return_code", "1023");
        responseMap.put("return_message", successMsg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        logger.info("sending the response back to the requesting server");
        response.getWriter().write(js.deepSerialize(responseMap));
        response.flushBuffer();
        return null;
      }
    }
    successMsg = "Failed to change the password.";
    responseMap.put("return_code", "1024");
    responseMap.put("return_message", successMsg);
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }
  
}
