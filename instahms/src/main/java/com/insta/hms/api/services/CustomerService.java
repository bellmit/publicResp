package com.insta.hms.api.services;

import com.insta.hms.api.models.APILoginResponse;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.usermanager.PasswordEncoder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomerService.
 */
@Component
public class CustomerService {

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;

  /**
   * The Enum LoginReqParameter.
   */
  public enum LoginReqParameter {
    
    /** The credentials. */
    CREDENTIALS, 
 /** The hospital name. */
 HOSPITAL_NAME, 
 /** The session map. */
 SESSION_MAP;
  }

  /**
   * The Enum ReturnCode.
   */
  public enum ReturnCode {
    
    /** The invalid request. */
    INVALID_REQUEST("1002", HttpServletResponse.SC_BAD_REQUEST,
        "Mandatory fields are not supplied"), 
    /** The login failed. */
    LOGIN_FAILED("1023", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
        "Failed to login the user"), 
    /** The invalid username pwd. */
    INVALID_USERNAME_PWD("1021", HttpServletResponse.SC_UNAUTHORIZED,
        "invalid username or password"), 
    /** The login success. */
    LOGIN_SUCCESS("2001", HttpServletResponse.SC_OK, "Success");

    /**
     * Instantiates a new return code.
     *
     * @param errorCode the error code
     * @param responseStatus the response status
     * @param returnMessage the return message
     */
    private ReturnCode(String errorCode, int responseStatus, String returnMessage) {
      this.returnCode = errorCode;
      this.responseStatus = responseStatus;
      this.returnMessage = returnMessage;
    }

    /** The return code obj map. */
    private static Map<String, ReturnCode> returnCodeObjMap = new HashMap<String, ReturnCode>();

    static {
      for (ReturnCode returnCode : ReturnCode.values()) {
        returnCodeObjMap.put(returnCode.getReturnCode(), returnCode);
      }
    }

    /** The return code. */
    private String returnCode;
    
    /** The response status. */
    private int responseStatus;
    
    /** The return message. */
    private String returnMessage;

    /**
     * Gets the return code.
     *
     * @return the return code
     */
    public String getReturnCode() {
      return this.returnCode;
    }

    /**
     * Gets the return message.
     *
     * @return the return message
     */
    public String getReturnMessage() {
      return this.returnMessage;
    }

    /**
     * Gets the HTTP response status.
     *
     * @return the HTTP response status
     */
    public int getHTTPResponseStatus() {
      return this.responseStatus;
    }

    /**
     * Gets the HTTP response status.
     *
     * @param loginResponse the login response
     * @return the HTTP response status
     */
    public static int getHTTPResponseStatus(APILoginResponse loginResponse) {
      if (StringUtils.isNotBlank(loginResponse.getReturnCode())) {
        ReturnCode returnCode = returnCodeObjMap.get(loginResponse.getReturnCode());
        if (returnCode != null) {
          return returnCode.getHTTPResponseStatus();
        }
      }
      return ReturnCode.LOGIN_FAILED.getHTTPResponseStatus();
    }

  }

  /**
   * Do login.
   *
   * @param paramMap the param map
   * @return the API login response
   */
  public APILoginResponse doLogin(Map<LoginReqParameter, Object[]> paramMap) {
    int flag = 0;
    int newValidDuration = 0;
    Connection con = null;
    String tokenValidation = messageUtil.getMessage("token.validation.duration", null);
    int validDuration = Integer.parseInt(tokenValidation);
    String requestHandaler = null;
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentLoginTime = new java.sql.Timestamp(now.getTime());
    APILoginResponse loginResponse = new APILoginResponse();
    String[] parts = (String[]) paramMap.get(LoginReqParameter.CREDENTIALS);
    if (!ArrayUtils.isEmpty(parts) && StringUtils.isNotBlank(parts[0])) {
      parts = parts[0].split(":");
    }
    String userId = "";
    String password = "";
    String[] hospitalNameParam = (String[]) paramMap.get(LoginReqParameter.HOSPITAL_NAME);
    Boolean loginSuccess = false;
    if (ArrayUtils.isEmpty(parts) || ArrayUtils.getLength(parts) != 2 || (parts[0] == null)
        || parts[0].isEmpty() || (parts[1] == null) || parts[1].isEmpty()
        || ArrayUtils.isEmpty(hospitalNameParam) || hospitalNameParam[0].isEmpty()) {
      loginResponse.setReturnCodeAndMessage(ReturnCode.INVALID_REQUEST);
      return loginResponse;
    } else {
      userId = parts[0];
      password = parts[1];
    }
    String hospitalName = hospitalNameParam[0];
    hospitalName = hospitalName.replaceAll("[^a-zA-Z0-9_]", "");
    Object[] sessionMapObj = paramMap.get(LoginReqParameter.SESSION_MAP);
    Map<String, Object> sessionMap = (Map<String, Object>) sessionMapObj[0];
    /*
     * Try to authenticate, connect to the right schema before acquiring a connection
     */

    try {
      do {

        /*
         * Check validity of hospital by getting a connection
         */
        con = com.insta.instaapi.common.DbUtil.getConnection(hospitalName);

        if (con == null) {
          loginSuccess = false;
          break;
        }

        /*
         * Check validity of user ID/ password
         */
        BasicDynaBean bean = com.insta.instaapi.customer.login.UserDAO.getRecord(con, userId);
        // need
        // to
        // put
        // in
        // userDAO
        // class
        // of
        // insta
        if (bean != null && bean.get("emp_password") != null
            && PasswordEncoder.matches(con, password, (String) bean.get("emp_password"), bean)) {
          // assigning requestHandaler to random string
          String randomKey = UUID.randomUUID().toString();
          randomKey = randomKey.replaceAll("-", "");
          requestHandaler = "instaapi_" + randomKey.substring(0, 11);
          loginSuccess = true;
          // removing the old requst_handler_key for the logged in
          // user
          ArrayList toRemove = new ArrayList();
          Map<String, Object> parametersMap = new HashMap<String, Object>();
          for (String key : sessionMap.keySet()) {
            Map value = (Map) sessionMap.get(key);
            java.sql.Timestamp oldLoginTime = (java.sql.Timestamp) value.get("login_time");
            if (value.get("customer_user_id").equals(userId)
                && value.get("hospital_name").equals(hospitalName)) {
              parametersMap.put("hospital_name", hospitalName);
              parametersMap.put("customer_user_id", userId);
              parametersMap.put("login_time", currentLoginTime);
              parametersMap.put("patient_login", false);
              if ((currentLoginTime.getTime() - oldLoginTime.getTime()) / 60000 < validDuration) {
                requestHandaler = key;
                parametersMap.put("login_time", value.get("login_time"));
                newValidDuration = (int) (validDuration
                    - ((currentLoginTime.getTime() - oldLoginTime.getTime()) / 60000));
                flag = 1;
                loginResponse.setExpiresIn(BigInteger.valueOf(newValidDuration));
                loginResponse.setRequestHandlerKey(requestHandaler);
                loginResponse.setReturnCodeAndMessage(ReturnCode.LOGIN_SUCCESS);
                loginSuccess = true;
                break;
              } else {
                toRemove.add(key);
                flag = 2;
                loginResponse.setReturnCodeAndMessage(ReturnCode.LOGIN_SUCCESS);
                loginResponse.setRequestHandlerKey(requestHandaler);
                loginResponse.setExpiresIn(BigInteger.valueOf(validDuration));
                loginSuccess = true;
                break;
              }
            }
          }

          if (flag == 0) {
            parametersMap.put("hospital_name", hospitalName);
            parametersMap.put("customer_user_id", userId);
            parametersMap.put("login_time", currentLoginTime);
            parametersMap.put("patient_login", false);
            sessionMap.put(requestHandaler, parametersMap);
            loginResponse.setReturnCodeAndMessage(ReturnCode.LOGIN_SUCCESS);
            loginResponse.setRequestHandlerKey(requestHandaler);
            loginResponse.setExpiresIn(BigInteger.valueOf(validDuration));
            loginSuccess = true;
          }
          if (flag == 2) {
            sessionMap.put(requestHandaler, parametersMap);
          }
          sessionMap.keySet().removeAll(toRemove);
        } else {
          loginResponse.setReturnCodeAndMessage(ReturnCode.INVALID_USERNAME_PWD);
          return loginResponse;
        }
      } while (false);
    } catch (Exception ex) {
      loginSuccess = false;
    } finally {
      com.insta.instaapi.common.DbUtil.closeConnections(con, null);
    }
    if (!loginSuccess) {
      loginResponse.setReturnCodeAndMessage(ReturnCode.LOGIN_FAILED);
    }
    return loginResponse;

  }

}
