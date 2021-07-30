package com.insta.instaapi.customer.login;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.usermanager.PasswordEncoder;
import com.insta.hms.usermanager.Role;
import com.insta.hms.usermanager.RoleDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserBO;
import com.insta.hms.usermanager.UserDAO;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ScreenRights;
import com.insta.instaapi.common.ServletContextUtil;
import com.insta.mhms.patient.login.LoginAction;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CustomerLoginAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  /**
   * Validate and create session for API User.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException IO Exception
   */
  public ActionForward login(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    logger.info("getting login related parameters");
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    int flag = 0;
    int newValidDuration = 0;
    Connection con = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    String hospitalName = request.getParameter("hospital_name");
    hospitalName = hospitalName.replaceAll("[^a-zA-Z0-9_]", "");
    String userCredential = ApiUtil.getCredentials(request);
    Map<String, Object> loginSuccessMap = new HashMap<String, Object>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    String successMsg = "";
    if (userCredential == null || userCredential.isEmpty()) {
      logger.error("invalid user name password");
      logger.error("invalid user name password");
      logger.error("invalid user name password");
      successMsg = "invalid username or password";
      loginSuccessMap.put("return_code", "1021");
      loginSuccessMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(js.deepSerialize(loginSuccessMap));
      response.flushBuffer();
      return null;
    }
    String[] parts = userCredential.split(":", 2);
    String userId = parts[0];
    String password = parts[1];
    String requestHandaler = null;
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentLoginTime = new java.sql.Timestamp(now.getTime());

    logger.info("getting user name,hospital name " + userId + "---" + hospitalName);

    Boolean loginSuccess = false;
    logger.info("checking for empty parameters....");
    if ((userId == null) || userId.isEmpty() || (password == null) || password.isEmpty()
        || (hospitalName == null) || hospitalName.isEmpty()) {
      logger.warn("Bad or missing parameters for login");
      successMsg = "Mandatory fields are not supplied";
      loginSuccessMap.put("return_code", "1002");
      loginSuccessMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(js.deepSerialize(loginSuccessMap));
      response.flushBuffer();
      return null;
    }

    /*
     * Try to authenticate, connect to the right schema before acquiring a connection
     */

    try {
      do {

        /*
         * Check validity of hospital by getting a connection
         */
        con = com.insta.instaapi.common.DbUtil.getConnection(hospitalName);
        logger.info("getting connection form database " + con);

        if (con == null) {
          loginSuccess = false;
          break;
        }

        // Check validity of user ID/ password
        // TODO: need to put in userDAO class of insta
        BasicDynaBean bean = com.insta.instaapi.customer.login.UserDAO.getRecord(con, userId);
        logger.info("getting user related data.");

        logger.info("checking for currect user name and password...");
        logger.info("{}", bean != null);
        String pwd = null;
        boolean patientLogin = false;
        String userDoctorId = null;
        if (bean != null && bean.get("emp_password") != null
            && PasswordEncoder.matches(con, password, (String) bean.get("emp_password"), bean)) {
          loginSuccess = true;
          userDoctorId = (String) bean.get("doctor_id");
          if (bean.get("emp_username").equals("addons_user")
              && !com.insta.instaapi.customer.login.UserDAO.isModuleActivated(con,"mod_addons")) {
            loginSuccess = false;
          }
        } else {
          PatientDetailsDAO patientDetailDao = new PatientDetailsDAO();
          BasicDynaBean patientDetailsBean = patientDetailDao
              .findByKey(con,"mr_no", userId);
          if (patientDetailsBean != null && patientDetailsBean.get("mobile_password") != null
              && password.equals(patientDetailsBean.get("mobile_password"))) {
            loginSuccess = true;
            patientLogin = true;
          }
        }
        if (loginSuccess) {
          // assigning requestHandaler to random string
          String randomKey = UUID.randomUUID().toString();
          randomKey = randomKey.replaceAll("-", "");
          requestHandaler = "instaapi_" + randomKey.substring(0, 11);
          loginSuccess = true;
          // removing the old requst_handler_key for the logged in user
          ArrayList toRemove = new ArrayList();
          Map<String, Object> parametersMap = new HashMap<String, Object>();
          for (String key : sessionMap.keySet()) {
            Map value = (Map) sessionMap.get(key);
            java.sql.Timestamp oldLoginTime = (java.sql.Timestamp) value.get("login_time");
            if (value.get("customer_user_id").equals(userId)
                && value.get("hospital_name").equals(hospitalName)) {
              parametersMap.put("hospital_name", hospitalName);
              parametersMap.put("user_doctor_id", userDoctorId);
              parametersMap.put("customer_user_id", userId);
              parametersMap.put("login_time", currentLoginTime);
              parametersMap.put("patient_login", patientLogin);
              if ((currentLoginTime.getTime() - oldLoginTime.getTime()) / 60000 < validDuration) {
                requestHandaler = key;
                parametersMap.put("login_time", value.get("login_time"));
                newValidDuration = (int) (validDuration
                    - ((currentLoginTime.getTime() - oldLoginTime.getTime()) / 60000));
                flag = 1;
                successMsg = "Success";
                loginSuccessMap.put("return_code", "2001");
                loginSuccessMap.put("return_message", successMsg);
                loginSuccessMap.put("request_handler_key", requestHandaler);
                loginSuccessMap.put("expires_in", newValidDuration);
                loginSuccess = true;
                break;
              } else {
                toRemove.add(key);
                flag = 2;
                successMsg = "Success";
                loginSuccessMap.put("return_code", "2001");
                loginSuccessMap.put("return_message", successMsg);
                loginSuccessMap.put("request_handler_key", requestHandaler);
                loginSuccessMap.put("expires_in", validDuration);
                loginSuccess = true;
                break;
              }
            }
            // parametersMap.clear();
          }

          if (flag == 0) {
            parametersMap.put("hospital_name", hospitalName);
            parametersMap.put("customer_user_id", userId);
            parametersMap.put("user_doctor_id", userDoctorId);
            parametersMap.put("login_time", currentLoginTime);
            parametersMap.put("patient_login", patientLogin);
            sessionMap.put(requestHandaler, parametersMap);
            successMsg = "Success";
            loginSuccessMap.put("return_code", "2001");
            loginSuccessMap.put("return_message", successMsg);
            loginSuccessMap.put("request_handler_key", requestHandaler);
            loginSuccessMap.put("expires_in", validDuration);
            loginSuccess = true;
          }
          if (flag == 2) {
            sessionMap.put(requestHandaler, parametersMap);
          }
          sessionMap.keySet().removeAll(toRemove);
        } else {
          logger.error("invalid user name password");
          successMsg = "invalid username or password";
          loginSuccessMap.put("return_code", "1021");
          loginSuccessMap.put("return_message", successMsg);
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setContentType("application/json");
          response.setHeader("Cache-Control", "no-cache");
          // response.setHeader("Access-Control-Allow-Origin", "*");
          response.getWriter().write(js.deepSerialize(loginSuccessMap));
          response.flushBuffer();
          return null;
        }
      } while (false);
    } catch (Exception ex) {
      logger.error("Exception Raised in Login Action", ex);
      loginSuccess = false;
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(con, null);
    }
    if (!loginSuccess) {
      successMsg = "Failed to login the user";
      loginSuccessMap.put("return_code", "1023");
      loginSuccessMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    logger.info("sending the response back to the requesting server");

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(loginSuccessMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Save API User.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws IOException  IO Exception
   * @throws SQLException SQL Exception
   */
  public ActionForward saveUser(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, SQLException {
    logger.info("save user");
    String requestHandalerKey = ApiUtil.getRequestKey(request);
    JSONSerializer js = JsonProcessor.getJSONParser();
    String successMsg = "success";
    Map<String, Object> responseMap = new HashMap<String, Object>();
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    boolean isAValidRequest = false;
    Map<String, Object> sessionParameters = null;
    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String, Object>) sessionMap.get(requestHandalerKey);
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
    logger.info("valid request");
    // Check the rights
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx, "createUser");
    isScreenRights = isScreenRights
        && ScreenRights.getScreenRights(requestHandalerKey, ctx, "portalDashboard");
    if (!isScreenRights) {
      successMsg = "Permission Denied. Please check with Administrator.";
      responseMap.put("return_code", "1003");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    logger.info("got screen rights");
    String userNameToUpdate = request.getParameter("user_name");
    String password = request.getParameter("password");
    if ((userNameToUpdate == null || userNameToUpdate.isEmpty())
        || (password == null || password.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    if (userNameToUpdate.contains(" ") || password.contains(" ")) {
      successMsg = "Username and Password should not contain spaces.";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    if (userNameToUpdate.length() >= 30 || userNameToUpdate.length() <= 3) {
      successMsg = "Username should be between 4 to 30 characters.";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    if (password.length() <= 3) {
      successMsg = "password should be more than 3 characters";
      responseMap.put("return_code", "1002");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    logger.info("mandatory fields supplied");
    Connection con = com.insta.instaapi.common.DbUtil
        .getConnection((String) sessionParameters.get("hospital_name"));
    UserDAO dao = new UserDAO(con);
    BasicDynaBean bean = dao.getUserBean(con, userNameToUpdate);
    if (bean != null) {
      com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      successMsg = "username already exists";
      logger.info("username already exists");
      responseMap.put("return_code", "1021");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    Integer roleInt = null;
    RoleDAO roleDao = new RoleDAO(con);
    Role role = roleDao.getRoleFromName("AddonsAdmin");
    if (role != null) {
      roleInt = role.getRoleId();
    } else {
      com.insta.instaapi.common.DbUtil.closeConnections(con, null);
      logger.info("Role PartnerAddon does not exist");
      successMsg = "Role PartnerAddon does not exist";
      responseMap.put("return_code", "1022");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
    UserBO userBO = new UserBO();
    Map resultMap = null;
    User objDto = new User();
    objDto.setName(userNameToUpdate);
    objDto.setFullname(userNameToUpdate);
    HttpSession session = request.getSession();
    String modifiedByUser = (String) session.getAttribute("userid");
    objDto.setModUser(modifiedByUser);
    objDto.setMultiStoreId(new String[0]);
    objDto.setPoApprovalLimit("");
    objDto.setWriteOffLimit("");
    objDto.setPrescription_note_taker("N");
    objDto.setStatus("A");
    objDto.setUserCenter(0);
    objDto.setRoleId(roleInt);
    password = PasswordEncoder.encode(password);
    objDto.setPassword(password);
    objDto.setEncryptAlgo("BCRYPT");
    objDto.setIsHospUser("N");
    objDto.setLoginControlsApplicable("Y");

    boolean success = dao.createUser(objDto);
    com.insta.instaapi.common.DbUtil.closeConnections(con, null);
    if (!success) {
      successMsg = "failed to insert user";
      logger.info("failed to insert use");
      responseMap.put("return_code", "1025");
      responseMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    responseMap.put("return_code", "2001");
    responseMap.put("return_message", "Success");
    logger.info("sending the response back to the requesting server");
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

}
