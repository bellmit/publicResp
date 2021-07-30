package com.bob.hms.common;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.mdm.breaktheglass.UserMrnoAssociationService;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class LoginHelper.
 */
@SuppressFBWarnings(value = "HARD_CODE_KEY", justification = "To be refactored later")
public class LoginHelper {
  static Logger logger = LoggerFactory.getLogger(LoginHelper.class);

  private static String userAuthQuery = " SELECT u.emp_username, u.role_id::integer, r.role_name,"
      + "  r.portal_id, u.doctor_id, u.total_login, "
      + "  ucbc.counter_id, c.counter_no as billing_counter, "
      + "  phc.counter_id as pharmacy_counter_id, phc.counter_no as pharmacy_counter, "
      + "  pharmacy_store_id, inventory_store_id, u.scheduler_dept_id, u.sch_default_doctor, "
      + "  u.prescription_note_taker, mod.activation_status AS patient_portal_activated,"
      + "  u.center_id, hcm.center_name, hcm.dhpo_facility_user_id, hcm.dhpo_facility_password,"
      + "  hcm.shafafiya_user_id, hcm.shafafiya_password, hcm.shafafiya_pbm_active,"
      + "  hcm.shafafiya_preauth_user_id, hcm.shafafiya_preauth_password, "
      + "  hcm.shafafiya_preauth_active," + "  health_authority, sample_collection_center, "
      + "  u.multi_store, u.password_change_date,po_approval_upto " 
      + " FROM u_user u "
      + " LEFT JOIN user_center_billing_counters ucbc ON ucbc.emp_username = u.emp_username "
      + " and ucbc.center_id= u.center_id "
      + "  LEFT JOIN counters c ON c.counter_id=ucbc.counter_id and c.status='A' "
      + "  LEFT JOIN counters phc ON u.pharmacy_counter_id=phc.counter_id AND phc.status='A' "
      + "  JOIN u_role r USING(role_id) "
      + "  LEFT JOIN modules_activated mod ON module_id = 'mod_patient_portal' "
      + "  JOIN hospital_center_master hcm ON (hcm.center_id = u.center_id AND hcm.status ='A')"
      + " WHERE u.emp_username=? AND emp_status='A'";

  private static String certAllowedQuery = "SELECT * from hosp_allowed_certificates"
      + " ORDER BY processing_order";

  /**
   * Login.
   *
   * @param realm    the realm
   * @param userId   the user id
   * @param request  the request
   * @param response the response
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static boolean login(String realm, String userId, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException {
    BasicDynaBean user = authenticateUser(realm, userId);
    if (null == user) {
      return false;
    } else {
      return updateUserContextData(realm, userId, user, request, response);
    }
  }

  /**
   * Authenticate user.
   *
   * @param realm  the realm
   * @param userId the user id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  private static BasicDynaBean authenticateUser(String realm, String userId) throws SQLException {
    Connection con = null;
    BasicDynaBean user = null;
    try {
      con = DataBaseUtil.getConnection();
      if (con == null) {
        logger.warn("Login FAILED: user=" + userId + "@" + realm);
        return null;
      }
      user = DataBaseUtil.queryToDynaBean(userAuthQuery, userId);

      if (user == null) {
        logger.warn(
            "Login FAILED: user=" + userId + "@" + realm + " (db-query=" + userAuthQuery + ")");
        return null;
      }
    } catch (SQLException sqle) {
      logger.warn("SQL Excpetion while autheticating user=" + userId + "@" + realm
          + ": SQL State = " + sqle.getSQLState() + ", message=" + sqle.getMessage());
      throw sqle;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return user;
  }

  /**
   * Update user context data.
   *
   * @param realm    the realm
   * @param userId   the user id
   * @param user     the user
   * @param request  the request
   * @param response the response
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private static boolean updateUserContextData(String realm, String userId, BasicDynaBean user,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

    HttpSession session = request.getSession();
    int roleId = (Integer) user.get("role_id");
    Connection con = null;
    con = DataBaseUtil.getConnection();
    boolean success = false;
    UserMrnoAssociationService userMrnoAssociationService = ApplicationContextProvider
        .getBean(UserMrnoAssociationService.class);
    userMrnoAssociationService.deleteUserMrNoAssociations(userId);
    /*
     * Match the type of user with the URL being accessed: doctors are allowed only doctorAccess
     * URLs and patients are allowed only patientAccess URLs.
     */
    try {
      if (!processSecurityCertificates(request)) {
        return false;
      }
      updateUserSession(request, user);
      processRemoteHostAddress(request);
      setPreferences(request, con);
      ScreenRightsHelper.setScreenRights(con, roleId);
      setCenterCredentialsInSession(session, user);
      String nexusToken = LoginHelper.generateToken(userId);
      session.setAttribute("nexus_token", nexusToken);
      session.setAttribute("login_handle", session.getId());
      updateUserLogin(con, userId, ((BigDecimal) user.get("total_login")).intValue(),
          session.getId(), nexusToken);
      success = true;
    } catch (SQLException exception) {
      logger.error("Exception Raised in Login Action", exception);
      session.removeAttribute("sesHospitalId");
      throw (exception);

    } catch (IOException exception) {
      logger.error("Exception Raised in Login Action", exception);
      session.removeAttribute("sesHospitalId");
      throw exception;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return success;
  }

  /**
   * Sets the preferences.
   *
   * @param req the req
   * @param con the con
   * @throws SQLException the SQL exception
   */
  /*
   * Retrieve a list of hospital specific preferences and save it in the session.
   */
  private static void setPreferences(HttpServletRequest req, Connection con) throws SQLException {
    Preferences prefs = null;
    PreferencesDao dao = new PreferencesDao(con);
    prefs = dao.getPreferences();
    Map groups = prefs.getModulesActivatedMap();
    boolean modAdvIns = false;
    boolean modEclaim = false;
    boolean modEclaimPbm = false;
    boolean modEclaimErx = false;
    boolean modRewardPoints = false;
    boolean modEclaimPreauth = false;
    boolean modPatPendingPres = false;

    if (groups.containsKey("mod_adv_ins") && "Y".equals(groups.get("mod_adv_ins"))) {
      modAdvIns = true;
    }
    if (groups.containsKey("mod_eclaim") && "Y".equals(groups.get("mod_eclaim"))) {
      modEclaim = true;
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
    if (groups.containsKey("mod_pat_pending_prescription") 
        && "Y".equals(groups.get("mod_pat_pending_prescription"))) {
      modPatPendingPres = true;
    }

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
   * Process security certificates.
   *
   * @param request the request
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  private static boolean processSecurityCertificates(HttpServletRequest request)
      throws SQLException {
    String userHospId = null;
    String clientCn = null;
    X509Certificate[] certs = (X509Certificate[]) request
        .getAttribute("javax.servlet.request.X509Certificate");

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
      logger.debug("Client CN Info: " + clientCn);
      userHospId += " CN=" + clientCn;
    } else {
      logger.debug("No cert, the scheme is: " + request.getScheme());
    }

    /*
     * Certificate is required if request is secure. Non-cert based access is allowed if request is
     * HTTP (ie, not secure). This is controlled by tomcat/firewall setup: remote access and hosted
     * access will disallow/redirect port 80 connections.
     */
    if (certs == null && request.isSecure()) {
      request.setAttribute("login_status", "You did not provide your certificate");
      logger.warn("Login FAILED: user=" + userHospId + " (No Certificate provided)");
      return false;
    }

    if (certs != null) {
      boolean certAllowed = false;
      List<BasicDynaBean> certList = DataBaseUtil.queryToDynaList(certAllowedQuery);

      for (BasicDynaBean cert : certList) {
        String certPattern = (String) cert.get("certificate_pattern");
        logger.debug("Matching: " + clientCn + " with pattern: " + certPattern);
        if (clientCn != null && clientCn.matches(certPattern)) {
          certAllowed = ((String) cert.get("action")).equals("A");
          break;
        }
      }

      if (!certAllowed) {
        request.setAttribute("login_status", "Certificate not authorized for Hospital");
        logger.warn("Login FAILED: user=" + userHospId + " (Certificate disallowed)");
        return false;
      }
    }

    return true;

  }

  /**
   * Update user session.
   *
   * @param request the request
   * @param user    the user
   */
  private static void updateUserSession(HttpServletRequest request, BasicDynaBean user) {

    HttpSession session = request.getSession();
    String userId = (String) user.get("emp_username");
    String userHospId = userId + "" + session.getAttribute("sesHospitalId");
    String roleName = (String) user.get("role_name");
    String centerName = (String) user.get("center_name");
    logger.info("Login success: user=" + userHospId + " role=" + roleName + " center=" + centerName
        + " ip-addr=" + request.getRemoteAddr());
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
    ConfidentialityGroupService confidentialityService = ApplicationContextProvider
        .getBean(ConfidentialityGroupService.class);
    HospitalRoleService hospitalRoleService = ApplicationContextProvider
        .getBean(HospitalRoleService.class);
    List<BasicDynaBean> userGroups = confidentialityService.getUserConfidentialityGroups(userId);
    List<Integer> userGroupList = new ArrayList<>();
    for (BasicDynaBean userGroup : userGroups) {
      userGroupList.add((Integer) userGroup.get("confidentiality_grp_id"));
    }
    List<Integer> hospitalRoleIds = hospitalRoleService.getHospitalRoleIds(userId);
    int roleId = (Integer) user.get("role_id");
    int centerId = (Integer) user.get("center_id");
    session.setAttribute("userid", userId);
    session.setAttribute("user_accessible_patient_groups", userGroupList);
    session.setAttribute("userId", userId);
    session.setAttribute("roleId", roleId);
    session.setAttribute("roleName", roleName);
    session.setAttribute("centerId", centerId);
    session.setAttribute("centerName", centerName);
    session.setAttribute("multiStoreAccess", multiStoreAccess);
    String loginCenterHealthAuthority = user.get("health_authority") != null
        ? (String) user.get("health_authority")
        : "";
    session.setAttribute("sampleCollectionCenterId",
        (Integer) user.get("sample_collection_center"));
    session.setAttribute("loginCenterHealthAuthority", loginCenterHealthAuthority);

    session.setAttribute("billingcounterId", user.get("counter_id"));
    session.setAttribute("billingcounterName", user.get("billing_counter"));
    session.setAttribute("pharmacyCounterId", user.get("pharmacy_counter_id"));
    session.setAttribute("pharmacyCounterName", user.get("pharmacy_counter"));
    session.setAttribute("pharmacyStoreId", user.get("pharmacy_store_id"));
    session.setAttribute("inventoryStoreId", user.get("inventory_store_id"));
    session.setAttribute("doctorId", user.get("doctor_id"));
    session.setAttribute("scheduler_dept_id", user.get("scheduler_dept_id"));
    session.setAttribute("sch_default_doctor", user.get("sch_default_doctor"));
    session.setAttribute("prescriptionNoteTakerPreferences", user.get("prescription_note_taker"));
    session.setAttribute("userpoApprovalLimit", user.get("po_approval_upto"));
    session.setAttribute("remote_host_address", request.getRemoteAddr()); // ip addr, like 10.0.0.1
    session.setAttribute("hospital_role_ids", hospitalRoleIds);
  }

  /**
   * Process remote host address.
   *
   * @param request the request
   * @throws SQLException         the SQL exception
   * @throws UnknownHostException the unknown host exception
   */
  private static void processRemoteHostAddress(HttpServletRequest request)
      throws SQLException, UnknownHostException {

    HttpSession session = request.getSession();

    boolean hospUsesDynamicAddress = "Y"
        .equals(GenericPreferencesDAO.getAllPrefs().get("hosp_uses_dynamic_addresses"));

    if (hospUsesDynamicAddress) {
      /*
       * following statement taking time of 5sec in case of dynamic ip address, so excuted once and
       * kept in session instead of executing it in all the places (ex. registration, diagnostics,
       * consultation)
       */
      InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
      /*
       * The hostname if available, else, the IP address itself is returned, eg, krishna.local or
       * 10.0.0.234. If there is no DNS server that is able to resolve this, it may look into
       * /etc/hosts for the name of the host. If that also fails, then, it gives the IP address
       * itself. Therefore, the hospUsesDynamicAddress preference has to be set only if the hosital
       * uses dynamic IPs, AND there is a DNS that can resolve the host to the name.
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

  }

  /**
   * Converts Bytes to hexadecimal.
   *
   * @param bytes the bytes
   * @return String
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuffer result = new StringBuffer();
    for (byte byt : bytes) {
      result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString().toLowerCase();
  }

  /**
   * Create authentication token for verification.
   *
   * @param userId the user id
   * @return the string
   */
  protected static String generateToken(String userId) {
    try {
      String salt = "UWlf7bJkfgZ0jrCbSAYBHDSJY6yRCpdDHGFUmvbVl1SjJ9rxO35VmLEVOv6nlQcs";
      String toHash = userId + (new Date()).toString();
      Mac sha256HMac;
      sha256HMac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(salt.getBytes(), "HmacSHA256");
      sha256HMac.init(secretKey);
      byte[] hash = sha256HMac.doFinal(toHash.getBytes());
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
      exception.printStackTrace();
    }
    return null;
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
  private static boolean updateUserLogin(Connection con, String userId, int totalLogin,
      String jsessionId, String nexusToken) throws SQLException, IOException {
    Map fields = new HashMap();

    totalLogin = totalLogin + 1;
    fields.put("nexus_token", nexusToken);
    fields.put("last_login", DataBaseUtil.getDateandTime());
    fields.put("total_login", totalLogin);
    fields.put("login_handle", jsessionId);
    Map keys = new HashMap();
    keys.put("emp_username", userId);

    int update = new GenericDAO("u_user").update(con, fields, keys);

    return update > 0;
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
      session.setAttribute("dhpo_facility_user", dhpoFacilityUser);
      session.setAttribute("dhpo_facility_password", dhpoFacilityPassword);
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

}
