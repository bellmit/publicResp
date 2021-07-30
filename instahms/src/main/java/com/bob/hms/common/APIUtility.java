/**
 * 
 */

package com.bob.hms.common;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class APIUtility.
 *
 * @author krishna
 */
public class APIUtility {

  static Logger logger = LoggerFactory.getLogger(APIUtility.class);
  private static final CenterMasterDAO centerDao = new CenterMasterDAO(); 

  /**
   * Sets the connection details.
   *
   * @param ctx               the ctx
   * @param requestHandlerKey the request handler key
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String setConnectionDetails(ServletContext ctx, String requestHandlerKey)
      throws SQLException {
    // this is not an api call.
    if (requestHandlerKey == null || requestHandlerKey.equals("")) {
      return null;
    }

    Map<String, Object> sessionMap = getConetxtParametrsMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map sessionParameters = null;

    String error = null;
    if (sessionMap == null || sessionMap.isEmpty()) {
      error = "invalid request token, please login again";
      return error;
    } else {
      sessionParameters = (Map) sessionMap.get(requestHandlerKey);
      if (sessionParameters == null || sessionParameters.isEmpty()) {
        error = "invalid request token, please login again";
        return error;
      }
    }

    String schema = (String) sessionParameters.get("hospital_name");
    // set connection details with default values.
    RequestContext.setConnectionDetails(
        new String[] { "", "", schema, "", "0" });

    BasicDynaBean centerBean = null;
    String userName = (String) sessionParameters.get("customer_user_id");
    String roleId = null;

    if ((boolean) sessionParameters.get("patient_login")) {
      centerBean = centerDao.findByKey("center_id", 0);
      roleId = "-1"; //Hack to skip role checks for patient in 
    } else {
      GenericDAO userDAO = new GenericDAO("u_user");
      BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
      roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
      centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
    }

    // set connection details with the userid and centerid
    RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
        String.valueOf((Integer) centerBean.get("center_id")), 
        (String) centerBean.get("center_name"), roleId });

    return error;
  }

  /**
   * Gets the conetxt parametrs map.
   *
   * @param ctx the ctx
   * @return the conetxt parametrs map
   */
  public static Map<String, Object> getConetxtParametrsMap(ServletContext ctx) {
    Map<String, Object> sessionMap = (Map<String, Object>) ctx.getAttribute("sessionMap");
    if (sessionMap == null || sessionMap.isEmpty()) {
      ctx.setAttribute("sessionMap", new HashMap<String, Object>());
      sessionMap = (Map<String, Object>) ctx.getAttribute("sessionMap");
    }
    return sessionMap;
  }

  /**
   * Sets the invalid login error.
   *
   * @param response the response
   * @param error    the error
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void setInvalidLoginError(HttpServletResponse response, String error)
      throws IOException {
    error = (error == null || error.isEmpty()) ? "invalid request token,please login again" : error;
    sendErrorResponse(response, error, "1001");
  }

  /**
   * Send error response.
   *
   * @param response  the response
   * @param error     the error
   * @param errorCode the error code
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void sendErrorResponse(HttpServletResponse response, String error, String errorCode)
      throws IOException {
    Map dataMap = new HashMap();

    dataMap.put("return_code", errorCode);
    dataMap.put("return_message", error);
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    JSONSerializer jsonSerializer = new JSONSerializer();
    response.getWriter().write(jsonSerializer.deepSerialize(dataMap));
    response.flushBuffer();
  }

  /**
   * recommended to call only prints.
   *
   * @return the preferences
   * @throws SQLException the SQL exception
   */
  public static Preferences getPreferences() throws SQLException {
    Preferences preferences = null;
    HttpSession session = RequestContext.getSession();
    if (session == null) {
      Connection con = DataBaseUtil.getConnection();
      try {
        preferences = new PreferencesDao(con).getPreferences();
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    } else {
      preferences = (Preferences) session.getAttribute("preferences");
    }
    return preferences;
  }

}
