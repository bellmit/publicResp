package com.insta.mhms.patient.login;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.instaapi.common.JsonProcessor;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author mithun.saha
 */
public class LoginAction extends DispatchAction {
  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  /**
   * Login method.
   *
   * @param mapping
   *          mapping parameter
   * @param form
   *          form paramter
   * @param request
   *          request object
   * @param response
   *          response object
   * @return returns action forward
   * @throws IOException
   *           Signals that an I/O exception has occurred
   * @throws ServletException
   *           may throw Servlet Exception
   * @throws SQLException
   *           may throw SQL Exception
   * @throws NoSuchAlgorithmException
   *           may throw NoSuchAlgorithmException
   * @throws ParseException
   *           may throw Parsing Exception
   * @throws Exception
   *           may throw Generic exception
   */
  public ActionForward login(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException,
      NoSuchAlgorithmException, ParseException, Exception {
    HttpSession session = request.getSession();
    Connection con = null;
    String hospitalName = request.getParameter("hospital_name");
    String userId = request.getParameter("mobile_user_id");
    String password = request.getParameter("mobile_user_password");
    String returnCode = "";
    String returnMsg = "";

    logger.info("getting user name,password,hospital name" + userId + "--" + password + "---"
        + hospitalName);

    Map<String, Object> loginSuccesssMap = new HashMap<String, Object>();
    JSONSerializer js = JsonProcessor.getJSONParser();
    Boolean loinSuccess = false;
    logger.info("checking for empty parameters....");
    if ((userId == null) || userId.isEmpty() || (password == null) || password.isEmpty()
        || (hospitalName == null) || hospitalName.isEmpty()) {
      logger.warn("Bad or missing parameters for login");
      returnMsg = "Mandatory fields are not supplied";
      loginSuccesssMap.put("return_code", "1002");
      loginSuccesssMap.put("return_message", returnMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(js.deepSerialize(loginSuccesssMap));
      response.flushBuffer();
      return null;
    }

    /*
     * Try to authenticate, connect to the right schema before acquiring a connection
     */

    try {
      do {
        session.setAttribute("sesHospitalId", hospitalName);
        /*
         * Check validity of hospital by getting a connection
         */
        con = DataBaseUtil.getConnection();

        if (con == null) {
          loinSuccess = false;
          // most likely hospital does not exist. Get the error msg from the session,
          // which would have been set by DataBaseUtil.
          String loginStatus = (String) session.getAttribute("sesErr");
          request.setAttribute("login_status", loginStatus);
          logger.warn("Login FAILED: user=" + hospitalName + " (" + loginStatus + ")");
          break;
        }

        /*
         * Check validity of user ID/ password
         */
        BasicDynaBean bean = PatientDetailsDAO.getPatientPhoneDetails(userId);

        logger.info("checking for currect user name and password...");
        if (bean != null && bean.get("mobile_password") != null
            && bean.get("mobile_password").toString().equals(password)) {
          loinSuccess = true;
          session.setAttribute("mobile_user_id", userId);
        } else {
          logger.error("invalid user name password");
          loinSuccess = false;
        }
        logger.info("setting all session realted data.");
        session.setAttribute("userid", userId);
        session.setAttribute("userId", userId);
        session.setAttribute("roleId", 0);
        session.setAttribute("roleName", "");
        session.setAttribute("centerId", 0);
        session.setAttribute("centerName", "Default Center");
        session.setAttribute("loginCenterHealthAuthority", "");
        session.setAttribute("sampleCollectionCenterId", 0);
        session.setAttribute("multiStoreAccess", "N");
        session.setAttribute("billingcounterId", 0);
        session.setAttribute("billingcounterName", 0);
        session.setAttribute("pharmacyCounterId", 0);
        session.setAttribute("pharmacyCounterName", 0);
        session.setAttribute("pharmacyStoreId", 0);
        session.setAttribute("inventoryStoreId", 0);
        session.setAttribute("doctorId", "");
        session.setAttribute("scheduler_dept_id", "");
        session.setAttribute("sch_default_doctor", "");
        session.setAttribute("prescriptionNoteTakerPreferences", "");
        session.setAttribute("remote_host_address", request.getRemoteAddr());
        logger.info("setting all prefs");
        setPreferences(request, con);
      } while (false);
    } catch (Exception exception) {
      logger.error("Exception Raised in Login Action", exception);
      session.removeAttribute("sesHospitalId");
      session.removeAttribute("mobile_user_id");
      returnMsg = "Failed to login the user";
      loginSuccesssMap.put("return_code", "1022");
      loginSuccesssMap.put("return_message", returnMsg);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");
      response.getWriter().write(js.deepSerialize(loginSuccesssMap));
      response.flushBuffer();
      return null;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    if (loinSuccess) {
      // loginSuccesssMap.put("login_status", "success");
      returnCode = "2001";
      returnMsg = "Success";
    } else {
      // loginSuccesssMap.put("login_status", "failure");
      returnCode = "1021";
      returnMsg = "invalid username or password";
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    loginSuccesssMap.put("return_code", returnCode);
    loginSuccesssMap.put("return_message", returnMsg);
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(loginSuccesssMap));
    response.flushBuffer();

    return null;
  }

  /**
   * Set preferences.
   * 
   * @param req
   *          request object
   * @param con
   *          connection object
   * @throws SQLException
   *           may throw Sql Exception
   */
  public static void setPreferences(HttpServletRequest req, Connection con) throws SQLException {

    Preferences prefs = null;
    PreferencesDao dao = new PreferencesDao(con);
    prefs = dao.getPreferences();
    Map groups = prefs.getModulesActivatedMap();
    boolean modAdvIns = false;
    boolean modEclaim = false;
    boolean modAccumed = false;
    boolean modEclaimPbm = false;
    boolean modEclaimErx = false;
    boolean modRewardPoints = false;
    boolean modEclaimPreauth = false;

    if (groups.containsKey("mod_adv_ins") && "Y".equals(groups.get("mod_adv_ins"))) {
      modAdvIns = true;
    }
    if (groups.containsKey("mod_eclaim") && "Y".equals(groups.get("mod_eclaim"))) {
      modEclaim = true;
    }
    if (groups.containsKey("mod_accumed") && "Y".equals(groups.get("mod_accumed"))) {
      modAccumed = true;
    }
    if (groups.containsKey("mod_eclaim_pbm") && "Y".equals(groups.get("mod_eclaim_pbm"))) {
      modEclaimPbm = true;
    }
    if (groups.containsKey("mod_eclaim_erx") && "Y".equals(groups.get("mod_eclaim_erx"))) {
      modEclaimErx = true;
    }
    if (groups.containsKey("mod_eclaim_preauth") && "Y".equals(groups.get("mod_eclaim_preauth"))) {
      modEclaimPreauth = true;
    }
    if (groups.containsKey("mod_reward_points") && "Y".equals(groups.get("mod_reward_points"))) {
      modRewardPoints = true;
    }
    req.getSession(false).setAttribute("mod_adv_ins", modAdvIns);
    req.getSession(false).setAttribute("mod_eclaim", modEclaim);
    req.getSession(false).setAttribute("mod_accumed", modAccumed);
    req.getSession(false).setAttribute("mod_eclaim_pbm", modEclaimPbm);
    req.getSession(false).setAttribute("mod_eclaim_erx", modEclaimErx);
    req.getSession(false).setAttribute("mod_eclaim_preauth", modEclaimPreauth);
    req.getSession(false).setAttribute("mod_reward_points", modRewardPoints);
    req.getSession(false).setAttribute("preferences", prefs);
  }

  private static final String getScreenRightsQuery =
      "SELECT screen_id, rights FROM screen_rights WHERE role_id=?";

  private static final String getActionRightsQuery =
      "SELECT action, rights FROM action_rights WHERE role_id=?";

  private static final String getUrlRightsQuery =
      "SELECT action_id, rights FROM url_action_rights WHERE role_id=?";

  /**
   * Set screen rights.
   *
   * @param con
   *          connection method
   * @param roleId
   *          role id
   * @throws SQLException
   *           may throw Sql Exception
   */
  public static void setScreenRights(Connection con, int roleId) throws SQLException {
    ArrayList screenRights = null;
    ArrayList groupRights = null;
    ArrayList actionRights = null;
    Map urlRightsDbMap = null;

    // for roleId == 1 or 2 (InstaAdmin or su), we assume full access, no need to query the DB
    if ((roleId != 1) && (roleId != 2)) {
      PreparedStatement stmt = con.prepareStatement(getScreenRightsQuery);
      stmt.setInt(1, 1);
      screenRights = DataBaseUtil.queryToArrayList(stmt);
      stmt.close();
      stmt = con.prepareStatement(getActionRightsQuery);
      stmt.setInt(1, 1);
      actionRights = DataBaseUtil.queryToArrayList(stmt);
      stmt.close();
      stmt = con.prepareStatement(getUrlRightsQuery);
      stmt.setInt(1, 1);
      urlRightsDbMap =
          ConversionUtils.listBeanToMapBean(DataBaseUtil.queryToDynaList(stmt), "action_id");
      stmt.close();
    }
  }
}
