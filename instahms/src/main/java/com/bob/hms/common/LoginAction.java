package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.Encoder;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.mdm.breaktheglass.UserMrnoAssociationService;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.usermanager.PasswordEncoder;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class loginAction.
 */
public class LoginAction extends Action {
  static Logger logger = LoggerFactory.getLogger(LoginAction.class);

  private static JSONSerializer js = new JSONSerializer().exclude("class");
  private static GenericDAO passwordRulesDao = new GenericDAO("password_rule");
  private static GenericDAO uUserDao = new GenericDAO("u_user");

  private static String userAuthQuery = " SELECT u.emp_username, u.role_id::integer, r.role_name,"
      + "  r.portal_id, u.doctor_id, u.total_login, "
      + "  ucbc.counter_id, c.counter_no as billing_counter, "
      + "  phc.counter_id as pharmacy_counter_id, phc.counter_no as pharmacy_counter, "
      + "  pharmacy_store_id, inventory_store_id, u.scheduler_dept_id, u.sch_default_doctor, "
      + "  u.prescription_note_taker, mod.activation_status AS patient_portal_activated,"
      + "  u.center_id, hcm.center_name, hcm.dhpo_facility_user_id, hcm.dhpo_facility_password,"
      + "  hcm.shafafiya_user_id, hcm.shafafiya_password, " + "  hcm.shafafiya_pbm_active,"
      + "  hcm.shafafiya_preauth_user_id, " + "  hcm.shafafiya_preauth_password, "
      + "  hcm.shafafiya_preauth_active," + "  health_authority, "
      + "  sample_collection_center, u.multi_store, " + " u.password_change_date,po_approval_upto "
      + " FROM u_user u "
      + " LEFT JOIN user_center_billing_counters ucbc ON ucbc.emp_username = u.emp_username "
      + " and ucbc.center_id= u.center_id "
      + " LEFT JOIN counters c ON c.counter_id=ucbc.counter_id and c.status='A' "
      + " LEFT JOIN counters phc ON u.pharmacy_counter_id=phc.counter_id AND phc.status='A' "
      + " JOIN u_role r USING(role_id) "
      + " LEFT JOIN modules_activated mod ON module_id = 'mod_patient_portal' "
      + " JOIN hospital_center_master hcm ON (hcm.center_id = u.center_id AND hcm.status ='A')"
      + " WHERE u.emp_username=? AND emp_password=? AND emp_status='A'";

  private static String certAllowedQuery = "SELECT * from hosp_allowed_certificates"
      + " ORDER BY processing_order";

  private static ConfidentialityGroupService confidentialityService = ApplicationContextProvider
      .getBean(ConfidentialityGroupService.class);
  private static UserMrnoAssociationService userMrnoAssociationService = ApplicationContextProvider
      .getBean(UserMrnoAssociationService.class);
  private static HospitalRoleService hospitalRoleService = ApplicationContextProvider
      .getBean(HospitalRoleService.class);
  private static IpEmrFormService ipEmrFormService =
      ApplicationContextProvider.getBean(IpEmrFormService.class);

  @Override
  @IgnoreConfidentialFilters
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, ServletException, SQLException, NoSuchAlgorithmException, ParseException {

  
    // TODO  change it to center specific offset later
    HashMap<String, Object> timeZone = new HashMap<String, Object>();
    timeZone.put("timeZoneOffset", TimeZone.getDefault().getRawOffset());
    timeZone.put("timeZoneID", TimeZone.getDefault().getID());
    timeZone.put("shortDisplayName",TimeZone.getDefault().getDisplayName(false, 0));
    HttpSession session = request.getSession();
    session.setAttribute("timeZone", timeZone);

    Connection con = null;
    String urlvalue = null;
    boolean success = false;
    boolean warnUser = false;
    boolean blockUser = false;
    int notifyDays = 0;

    /*
     * Extract the referring url from the session
     */
    urlvalue = (String) session.getAttribute("origUrl");
    logger.debug("URL value in login action is: " + urlvalue);

    /*
     * Extract the login info from request parameters
     */
    String userId = request.getParameter("userId");
    if (userId == null) {
      userId = request.getParameter("userid");
    }
    String hospitalId = request.getParameter("hospital");
    if (hospitalId == null) {
      hospitalId = request.getParameter("hospitalId");
    }
    hospitalId = Encoder.cleanSQL(hospitalId);
    userId = Encoder.cleanSQL(userId);
    String password = request.getParameter("password");
    if ((userId == null) || userId.isEmpty() || (password == null) || password.isEmpty()
        || (hospitalId == null) || hospitalId.isEmpty()) {
      logger.warn("Bad or missing parameters for login");
      request.setAttribute("login_status", "Please enter required parameters");
      return mapping.findForward("failure");
    }

    String userHospId = userId + "@" + hospitalId;

    /*
     * Extract the certificate CN info (if any) from the certificate
     */
    String clientCn = null;
    X509Certificate[] certs = (X509Certificate[]) request
        .getAttribute("javax.servlet.request.X509Certificate");

    /*
     * ClientDn is forwarded by Nginx (if Nginx exists in the setup) after SSL Auth If header
     * X-SSL-CLIENT-DN exists then Custom SSL is enabled Sample data of header X-SSL-CLIENT-DN:
     * "emailAddress=krupa@testsvr5,CN=krupa@testsvr5,OU=Operations,O=testsvr,ST=KA,C=IN"
     */
    String clientDn = request.getHeader("X-SSL-CLIENT-DN");

    if (certs != null) {
      String dn = certs[0].getSubjectX500Principal().getName();
      StringTokenizer st = new StringTokenizer(dn, ",");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        logger.debug("token: " + token);
        if (token.startsWith("CN=")) {
          clientCn = token.substring(3);
          break;
        }
      }
    } else if (clientDn != null) {
      clientCn = clientDn.split("CN=")[1].split(",")[0];
    }

    if (clientCn != null) {
      logger.debug("Client CN Info: " + clientCn);
      userHospId += " CN=" + clientCn;
    } else {
      logger.debug("No cert, the scheme is: " + request.getScheme());
    }

    /*
     * Try to authenticate, connect to the right schema before acquiring a connection
     */
    try {
      do {
        session.setAttribute("sesHospitalId", hospitalId);
        session.setAttribute("tempUserId", userId);

        /*
         * Check validity of hospital by getting a connection
         */
        con = DataBaseUtil.getConnection();

        if (con == null) {
          // most likely hospital does not exist. Get the error msg from the session,
          // which would have been set by DataBaseUtil.
          String loginStatus = (String) session.getAttribute("sesErr");
          if (loginStatus == null) {
            loginStatus = "Please provide valid credentials";
          }
          request.setAttribute("login_status", loginStatus);
          logger.warn("Login FAILED: user=" + userHospId + " (" + loginStatus + ")");
          break;
        }

        /*
         * Check validity of user ID/ password
         */
        BasicDynaBean bean = uUserDao.findByKey("emp_username", userId);
        Boolean isEncrypted = null;
        if (bean != null) {
          isEncrypted = (Boolean) bean.get("is_encrypted");
        }
        BasicDynaBean user = null;
        Boolean ssoOnlyUser = null;

        if (bean != null) {
          ssoOnlyUser = (Boolean) bean.get("sso_only_user");
        }
        if (bean != null && ssoOnlyUser) {
          request.setAttribute("login_status", "Password based login disallowed. Use SSO.");
          logger.warn("Login FAILED: user=" + userHospId + " (Password based login disallowed)");
          break;
        }
        if (bean != null && !isEncrypted) {
          try (PreparedStatement ps = con.prepareStatement(userAuthQuery)) {
            ps.setString(1, userId);
            ps.setString(2, password);
            user = DataBaseUtil.queryToDynaBean(ps);
          }
        }

        if (bean != null && isEncrypted
            && PasswordEncoder.matches(password, (String) bean.get("emp_password"), bean)) {
          // need to get the bean again because password might have
          // been changed due to migration from sha-1 to bcrypt
          bean = uUserDao.findByKey("emp_username", userId);
          try (PreparedStatement ps = con.prepareStatement(userAuthQuery)) {
            ps.setString(1, userId);
            ps.setString(2, (String) bean.get("emp_password"));
            user = DataBaseUtil.queryToDynaBean(ps);
          }
        }
        BasicDynaBean passwordRuleBean = passwordRulesDao.getRecord();
        if (user == null) {
          BasicDynaBean userBean = uUserDao.findByKey("emp_username", userId);
          // checking maximum failed login attempts
          if (userBean != null && passwordRuleBean.get("max_login_attempt") != null) {
            String empStatus = (String) userBean.get("emp_status");
            if (empStatus.equals("I")) {
              request.setAttribute("login_status",
                  "Your password is locked. Please contact your admin to unlock password.");
              break;
            }
            if (!PasswordEncoder.matches(password, (String) userBean.get("emp_password"),
                userBean)) {
              Integer numAttempts = (Integer) session.getAttribute("failedAttempts");
              String failedUser = (String) session.getAttribute("failedUser");
              Integer maxLoginAttempt = (Integer) passwordRuleBean.get("max_login_attempt");
              if (null == numAttempts && failedUser == null) {
                numAttempts = 1;
              } else {
                if (!failedUser.equals(userId)) {
                  numAttempts = 1;
                } else {
                  numAttempts++;
                }
              }
              if (numAttempts >= maxLoginAttempt) {
                HashMap<String, Object> keys = new HashMap<String, Object>();
                keys.put("emp_username", userId);
                userBean.set("emp_status", "I");
                uUserDao.update(con, userBean.getMap(), keys);
                request.setAttribute("login_status",
                    "Your password is locked. Please contact your admin to unlock password.");
                break;
              } else {
                session.setAttribute("failedAttempts", numAttempts);
                session.setAttribute("failedUser", userId);
              }
            }
          }
          // no match in the db.
          request.setAttribute("login_status", "Please provide valid credentials");
          logger.warn("Login FAILED: user=" + userHospId + " (db-query=" + userAuthQuery + ")");
          break;
        }

        /*
         * Check user password is expired or not
         */
        int passwordChangeFreqDays = passwordRuleBean.get("password_change_freq_days") != null
            ? (Integer) passwordRuleBean.get("password_change_freq_days")
            : 0;
        int passwordChangeNotifyDays = passwordRuleBean.get("password_change_notify_days") != null
            ? (Integer) passwordRuleBean.get("password_change_notify_days")
            : 0;

        Timestamp passwordChangeTime = (Timestamp) user.get("password_change_date");
        Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();

        int[] noOfDaysHours = DateUtil.getDaysHours(passwordChangeTime, currentTimeStamp, true);
        int noOfDays = noOfDaysHours[0];
        if (passwordChangeFreqDays != 0 && noOfDays >= passwordChangeFreqDays) {
          blockUser = true;
          request.setAttribute("userId", userId);
          request.setAttribute("login_status", "blockUser");
          request.setAttribute("passwordRules",
              js.serialize(ConversionUtils.copyListDynaBeansToMap(passwordRulesDao.listAll())));
          break;
        }

        int diffDays = passwordChangeFreqDays - noOfDays;
        if (passwordChangeNotifyDays != 0 && diffDays <= passwordChangeNotifyDays) {
          warnUser = true;
          notifyDays = diffDays;
        }

        /*
         * Check force password change is enabled
         */
        BasicDynaBean userBean = uUserDao.findByKey("emp_username", userId);
        boolean isForcePasswordChange = userBean != null
                ? (Boolean) userBean.get("force_password_change") : false;
        if (isForcePasswordChange) {
          blockUser = true;
          request.setAttribute("userId", userId);
          request.setAttribute("login_status", "blockUser");
          request.setAttribute("passwordRules",
                  js.serialize(ConversionUtils.copyListDynaBeansToMap(passwordRulesDao.listAll())));
          break;
        }

        /*
         * Match the type of user with the URL being accessed: doctors are allowed only doctorAccess
         * URLs and patients are allowed only patientAccess URLs.
         */

        String portalId = (String) user.get("portal_id");

        if ("Y".equals(mapping.getProperty("doctorAccess"))) {
          // patient portal URL: certificate is not required, check if user type is D
          if (!("D".equals(portalId))) {
            request.setAttribute("login_status", "Invalid username or password");
            logger.warn("Login FAILED: user=" + userHospId + " (no auth for doctor portal)");
            break;
          }
          session.setAttribute("doctorAccess", true);

        } else if ("Y".equals(mapping.getProperty("patientAccess"))) {
          // patient portal URL: certificate is not required, check if user type is P
          if (!("P".equals(portalId))) {
            request.setAttribute("login_status", "Invalid username or password");
            logger.warn("Login FAILED: user=" + userHospId + " (no auth for patient portal)");
            break;
          }

          if (!"Y".equals(user.get("patient_portal_activated"))) {
            // check if the module mod_patient_portal is activated for this hospital
            request.setAttribute("login_status", "Invalid user name or password");
            logger.warn("Login FAILED: user=" + userHospId + " (Patient Portal not activated)");
            break;
          }
          session.setAttribute("patientAccess", true);

        } else {
          // Normal user URL. User should be N type
          if (!("N".equals(portalId))) {
            request.setAttribute("login_status", "Invalid username or password");
            logger.warn("Login FAILED: user=" + userHospId + " (no auth for user area)");
            break;
          }

          /*
           * Certificate is required if request is secure. Non-cert based access is allowed if
           * request is HTTP (ie, not secure). This is controlled by tomcat/firewall setup: remote
           * access and hosted access will disallow/redirect port 80 connections.
           */
          if (clientCn == null && request.isSecure()) {
            request.setAttribute("login_status", "Please provide the certificate");
            logger.warn("Login FAILED: user=" + userHospId + " (No Certificate provided)");
            break;
          }

          if (clientCn != null) {
            boolean certAllowed = false;

            List<BasicDynaBean> certList = DataBaseUtil.queryToDynaList(certAllowedQuery);

            for (BasicDynaBean cert : certList) {
              String certPattern = (String) cert.get("certificate_pattern");
              logger.debug("Matching: " + clientCn + " with pattern: " + certPattern);
              if (clientCn.matches(certPattern)) {
                String action = (String) cert.get("action");
                certAllowed = action.equals("A") ? true : false;
                break;
              }
            }

            if (!certAllowed) {
              request.setAttribute("login_status", "Certificate not authorized for Hospital");
              logger.warn("Login FAILED: user=" + userHospId + " (Certificate disallowed)");
              break;
            }
          }
        }

        List<BasicDynaBean> userGroups = confidentialityService
            .getUserConfidentialityGroups(userId);
        List<Integer> userGroupList = new ArrayList<>();
        for (BasicDynaBean userGroup : userGroups) {
          userGroupList.add((Integer) userGroup.get("confidentiality_grp_id"));
        }

        String multiStoreStr = (String) user.get("multi_store");
        String multiStoreAccess;
        if (multiStoreStr == null || multiStoreStr.trim().equals("")) {
          multiStoreAccess = "N";
        } else {
          if (multiStoreStr.contains(",")) {
            multiStoreAccess = "A";
          } else {
            multiStoreAccess = "N";
          }
        }

        session.setAttribute("userid", userId);
        session.setAttribute("user_accessible_patient_groups", userGroupList);
        session.setAttribute("userId", userId);
        int roleId = (Integer) user.get("role_id");
        String roleName = (String) user.get("role_name");
        session.setAttribute("roleId", roleId);
        session.setAttribute("roleName", roleName);
        session.setAttribute("login_handle", session.getId());
        int centerId = (Integer) user.get("center_id");
        String centerName = (String) user.get("center_name");
        session.setAttribute("centerId", centerId);
        session.setAttribute("centerName", centerName);
        String loginCenterHealthAuthority = user.get("health_authority") != null
            ? (String) user.get("health_authority")
            : "";
        logger.info("Login success: user=" + userHospId + " role=" + roleName + " center="
            + centerName + " ip-addr=" + request.getRemoteAddr());
        session.setAttribute("loginCenterHealthAuthority", loginCenterHealthAuthority);
        session.setAttribute("sampleCollectionCenterId",
            (Integer) user.get("sample_collection_center"));
        session.setAttribute("multiStoreAccess", multiStoreAccess);
        session.setAttribute("billingcounterId", user.get("counter_id"));
        session.setAttribute("billingcounterName", user.get("billing_counter"));
        session.setAttribute("pharmacyCounterId", user.get("pharmacy_counter_id"));
        session.setAttribute("pharmacyCounterName", user.get("pharmacy_counter"));
        session.setAttribute("pharmacyStoreId", user.get("pharmacy_store_id"));
        session.setAttribute("inventoryStoreId", user.get("inventory_store_id"));
        session.setAttribute("doctorId", user.get("doctor_id"));
        session.setAttribute("scheduler_dept_id", user.get("scheduler_dept_id"));
        session.setAttribute("sch_default_doctor", user.get("sch_default_doctor"));
        session.setAttribute("prescriptionNoteTakerPreferences",
            user.get("prescription_note_taker"));
        session.setAttribute("userpoApprovalLimit", user.get("po_approval_upto"));
        session.setAttribute("remote_host_address", request.getRemoteAddr());
        List<Integer> hospitalRoleIds = hospitalRoleService.getHospitalRoleIds(userId);
        session.setAttribute("hospital_role_ids", hospitalRoleIds);
        // Notification count
        // session.setAttribute("count", MessageActionService.NotificationCount(con,userId));
        boolean hospUsesDynamicAddress = "Y"
            .equals(GenericPreferencesDAO.getAllPrefs().get("hosp_uses_dynamic_addresses"));

        if (hospUsesDynamicAddress) {
          /*
           * following statement taking time of 5sec in case of dynamic ip address, so excuted once
           * and kept in session instead of executing it in all the places (ex. registration,
           * diagnostics, consultation)
           */
          InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
          /*
           * The hostname if available, else, the IP address itself is returned, eg, krishna.local
           * or 10.0.0.234. If there is no DNS server that is able to resolve this, it may look into
           * /etc/hosts for the name of the host. If that also fails, then, it gives the IP address
           * itself. Therefore, the hospUsesDynamicAddress preference has to be set only if the
           * hosital uses dynamic IPs, AND there is a DNS that can resolve the host to the name.
           */
          String remoteHostName = addr.getHostName();
          session.setAttribute("remote_host_name", remoteHostName);
          logger.info("remote host name= " + remoteHostName);
        } else {
          /*
           * we are ok to use the IP address itself, since that will uniquely identify the host.
           */
          session.setAttribute("remote_host_name", request.getRemoteAddr());
        }

        setPreferences(request, con);
        ScreenRightsHelper.setScreenRights(con, roleId);
        setCenterCredentialsInSession(session, user);

        /*
         * All checks passed: Success
         */
        success = true;
        userMrnoAssociationService.deleteUserMrNoAssociations(userId);
        ipEmrFormService.deleteSectionLock(userId);
        String nexusToken = LoginHelper.generateToken(userId);
        session.setAttribute("nexus_token", nexusToken);
        updateUserLogin(con, userId, ((BigDecimal) user.get("total_login")).intValue(),
            session.getId(), nexusToken);

      } while (false);

    } catch (SQLException exception) {
      logger.error("Exception Raised in Login Action", exception);
      session.removeAttribute("sesHospitalId");
      session.removeAttribute("userid");
      session.removeAttribute("userId");
      session.removeAttribute("shafafiya_user");
      session.removeAttribute("shafafiya_preauth_user");
      session.removeAttribute("dhpo_facility_user");
      request.setAttribute("login_status", "Please provide valid credentials");

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    if (success) {
      ActionRedirect redirect = null;
      if (warnUser) {
        session.setAttribute("notifyPasswordChange", true);
        session.setAttribute("notifyDays", notifyDays);
        redirect = new ActionRedirect(mapping.findForward("notifyPasswordChange"));
      } else {
        if (urlvalue != null) {
          logger.info("Redirecting to original URL: " + urlvalue);
          session.removeAttribute("origUrl");

          String hashFragment = (String) session.getAttribute("hashFragment");
          if (null != hashFragment) {
            session.removeAttribute("hashFragment");
            urlvalue = urlvalue + hashFragment;
          }

          ((HttpServletResponse) response).sendRedirect(urlvalue);
          return null;
        }
        redirect = new ActionRedirect(mapping.findForward("success"));
        if (!"Y".equals(mapping.getProperty("patientAccess"))) {
          redirect.addParameter("userId", userId);
          redirect.addParameter("hospitalId", hospitalId);
        }
      }
      return redirect;

    } else {
      if (!blockUser) {
        session.removeAttribute("sesHospitalId");
      }

      session.removeAttribute("userid");
      session.removeAttribute("userId");
      // discourage brute force attempts to login: sleep for a while before returning the status
      try {
        Thread.sleep(3000);
      } catch (InterruptedException exception) {
        logger.warn("Login thread interuppted", exception);
        Thread.currentThread().interrupt();
      }
      return mapping.findForward("failure");
    }
  }

  /**
   * Sets the center credentials in session.
   *
   * @param session the session
   * @param user    the user
   */
  public static void setCenterCredentialsInSession(HttpSession session, BasicDynaBean user) {

    String loginCenterHealthAuthority = user.get("health_authority") != null
        ? (String) user.get("health_authority")
        : "";
    session.setAttribute("loginCenterHealthAuthority", loginCenterHealthAuthority);
    /*
     * Set Shafafiya User and Password as session attributes only when PBM module is enabled. These
     * are available for all users who can access the PBM screens through which web services are
     * requested.
     */
    if (session.getAttribute("mod_eclaim_pbm") != null
        && (Boolean) session.getAttribute("mod_eclaim_pbm")) {
      String shafafiyaUser = user.get("shafafiya_user_id") != null
          ? ((String) user.get("shafafiya_user_id")).trim()
          : "";
      String shafafiyaPassword = user.get("shafafiya_password") != null
          ? ((String) user.get("shafafiya_password")).trim()
          : "";
      String shafafiyaPbmActive = user.get("shafafiya_pbm_active") != null
          ? ((String) user.get("shafafiya_pbm_active")).trim()
          : "N";

      session.setAttribute("shafafiya_user", shafafiyaUser);
      session.setAttribute("shafafiya_password", shafafiyaPassword);
      session.setAttribute("shafafiya_pbm_active", shafafiyaPbmActive);
    }

    if (session.getAttribute("mod_eclaim_preauth") != null
        && (Boolean) session.getAttribute("mod_eclaim_preauth")) {
      String shafafiyaPreauthUser = user.get("shafafiya_preauth_user_id") != null
          ? ((String) user.get("shafafiya_preauth_user_id")).trim()
          : "";
      String shafafiyaPreauthPassword = user.get("shafafiya_preauth_password") != null
          ? ((String) user.get("shafafiya_preauth_password")).trim()
          : "";
      String shafafiyaPreauthActive = user.get("shafafiya_preauth_active") != null
          ? ((String) user.get("shafafiya_preauth_active")).trim()
          : "N";

      session.setAttribute("shafafiya_preauth_user", shafafiyaPreauthUser);
      session.setAttribute("shafafiya_preauth_password", shafafiyaPreauthPassword);
      session.setAttribute("shafafiya_preauth_active", shafafiyaPreauthActive);
    } else if (session.getAttribute("mod_eclaim_preauth") != null
        && (Boolean) session.getAttribute("mod_eclaim_preauth")
        && loginCenterHealthAuthority.equals("DHA")) {
      String dhpoFacilityUser = user.get("dhpo_facility_user_id") != null
          ? ((String) user.get("dhpo_facility_user_id")).trim()
          : "";
      String dhpoFacilityPassword = user.get("dhpo_facility_password") != null
          ? ((String) user.get("dhpo_facility_password")).trim()
          : "";
      String shafafiyaPreauthActive = user.get("shafafiya_preauth_active") != null
          ? ((String) user.get("shafafiya_preauth_active")).trim()
          : "N";

      session.setAttribute("dhpo_facility_user", dhpoFacilityUser);
      session.setAttribute("dhpo_facility_password", dhpoFacilityPassword);
      session.setAttribute("shafafiya_preauth_active", shafafiyaPreauthActive);
    }
    /*
     * Set Hospital User and Password as session attributes only when Erx module is enabled. These
     * are available for all users(Clinicians/Doctors) who can access the Erx screens through which
     * DHPO Web services are requested.
     */
    if (session.getAttribute("mod_eclaim_erx") != null
        && (Boolean) session.getAttribute("mod_eclaim_erx")) {
      String dhpoFacilityUser = user.get("dhpo_facility_user_id") != null
          ? ((String) user.get("dhpo_facility_user_id")).trim()
          : "";
      String dhpoFacilityPassword = user.get("dhpo_facility_password") != null
          ? ((String) user.get("dhpo_facility_password")).trim()
          : "";

      session.setAttribute("dhpo_facility_user", dhpoFacilityUser);
      session.setAttribute("dhpo_facility_password", dhpoFacilityPassword);
    }
  }

  /**
   * Retrieve a list of hospital specific preferences and save it in the session.
   *
   * @param req the req
   * @param con the con
   * @throws SQLException the SQL exception
   */
  public static void setPreferences(HttpServletRequest req, Connection con) throws SQLException {

    Preferences prefs = null;
    PreferencesDao dao = new PreferencesDao(con);
    prefs = dao.getPreferences();
    Map groups = prefs.getModulesActivatedMap();
    boolean modAdvIns = "Y".equals(groups.get("mod_adv_ins"));
    boolean modEclaim = "Y".equals(groups.get("mod_eclaim"));
    boolean modEclaimPbm = "Y".equals(groups.get("mod_eclaim_pbm"));
    boolean modEclaimErx = "Y".equals(groups.get("mod_eclaim_erx"));
    boolean modRewardPoints = "Y".equals(groups.get("mod_reward_points"));
    boolean modEclaimPreauth = "Y".equals(groups.get("mod_eclaim_preauth"));
    boolean modPatPendingPres = "Y".equals(groups.get("mod_pat_pending_prescription"));

    req.getSession(false).setAttribute("mod_adv_ins", modAdvIns);
    req.getSession(false).setAttribute("mod_eclaim", modEclaim);
    req.getSession(false).setAttribute("mod_eclaim_pbm", modEclaimPbm);
    req.getSession(false).setAttribute("mod_eclaim_erx", modEclaimErx);
    req.getSession(false).setAttribute("mod_eclaim_preauth", modEclaimPreauth);
    req.getSession(false).setAttribute("mod_reward_points", modRewardPoints);
    req.getSession(false).setAttribute("mod_pat_pending_prescription", modPatPendingPres);
    req.getSession(false).setAttribute("preferences", prefs);
  }

  /**
   * Update user login.
   *
   * @param con        the con
   * @param userId     the user id
   * @param totalLogin the total login
   * @param jsessionId the j session id
   * @param nexusToken the nexus token
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private boolean updateUserLogin(Connection con, String userId, int totalLogin, String jsessionId,
      String nexusToken) throws SQLException, IOException {
    Map fields = new HashMap();

    totalLogin = totalLogin + 1;
    fields.put("nexus_token", nexusToken);
    fields.put("last_login", DataBaseUtil.getDateandTime());
    fields.put("total_login", totalLogin);
    fields.put("login_handle", jsessionId);
    Map keys = new HashMap();
    keys.put("emp_username", userId);

    int update = uUserDao.update(con, fields, keys);

    return update > 0;
  }

  /**
   * Change password.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  public ActionForward changePassword(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    return mapping.findForward("notifyPasswordChange");
  }
}
