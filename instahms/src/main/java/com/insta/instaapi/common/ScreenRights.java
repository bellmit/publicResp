package com.insta.instaapi.common;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

public class ScreenRights {

  static Logger logger = LoggerFactory.getLogger(ScreenRights.class);

  public static Map<String, List<String>> rightsMap = new HashMap<String, List<String>>();

  static {

    rightsMap.put("preRegistration", Arrays.asList(new String[] { "reg_general" }));
    rightsMap.put("regMasterData", Arrays
        .asList(new String[] { "mas_states", "mas_cities", "mas_salutation", "mas_patient_cat" }));
    rightsMap.put("schedulerMasterData", Arrays.asList(new String[] { "mas_doctors" }));
    rightsMap.put("doctorScheduler",
        Arrays.asList(new String[] { "doc_scheduler_available_slots" }));
    rightsMap.put("PatientMobileAccess", Arrays.asList(new String[] { "edit_visit_details" }));
    rightsMap.put("doctorAvailabilitySlots",
        Arrays.asList(new String[] { "cat_resource_scheduler", "res_availability" }));
    rightsMap.put("patientClinicalData",
        Arrays.asList(new String[] { "clinical_data_lab_results" }));
    rightsMap.put("patientDetails", Arrays.asList(new String[] { "patient_details_search" }));
    rightsMap.put("visitDetails", Arrays.asList(new String[] { "visit_details_search" }));
    rightsMap.put("patientOrders", Arrays.asList(new String[] { "order" }));
    rightsMap.put("billing", Arrays.asList(new String[] { "credit_bill_collection" }));
    rightsMap.put("labReportsData", Arrays.asList(new String[] { "lab_schedules_list" }));
    rightsMap.put("visitEMRReports", Arrays.asList(new String[] { "visit_emr_screen" }));
    rightsMap.put("diagIncomingSampleReg",
        Arrays.asList(new String[] { "diag_incoming_sample_reg" }));
    rightsMap.put("createUser", Arrays.asList(new String[] { "usr_userdashboard_user" }));
    rightsMap.put("portalDashboard", Arrays.asList(new String[] { "portal_dashboard" }));
  }

  public static final String GET_RIGHTS_DETAILS = "SELECT rights FROM url_action_rights"
      + " where role_id = ? AND action_id = ? ";

  /**
   * Get screen rights.
   * @param roleId          role id
   * @param actionIdsList   action id list
   * @param hospitalName    schema name
   * @return                List of screen rights
   * @throws SQLException   SQL Exception
   */
  public static boolean getScreenRights(int roleId, List<String> actionIdsList, String hospitalName)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String status = null;
    boolean isRights = true;
    try {
      con = com.insta.instaapi.common.DbUtil.getConnection(hospitalName);
      ps = con.prepareStatement(GET_RIGHTS_DETAILS);
      ps.setInt(1, roleId);
      for (String st : actionIdsList) {
        ps.setString(2, st);
        status = DataBaseUtil.getStringValueFromDb(ps);
        if (status == null || !status.equals("A")) {
          isRights = false;
        }
      }
      return isRights;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Get screen rights.
   * @param requestHandlerKey   Request handler key
   * @param ctx                 Servlet context
   * @param apiName             API name
   * @return                List of screen rights
   * @throws SQLException   SQL Exception
   */
  public static boolean getScreenRights(String requestHandlerKey, ServletContext ctx,
      String apiName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    boolean isRights = true;
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map sessionParameters = null;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map) sessionMap.get(requestHandlerKey);
    }
    if ((Boolean) sessionParameters.get("patient_login")) {
      return true;
    }
    try {
      con = com.insta.instaapi.common.DbUtil
          .getConnection((String) sessionParameters.get("hospital_name"));
      ps = con.prepareStatement(" SELECT role_id FROM u_user WHERE emp_username = ? ");
      ps.setString(1, (String) sessionParameters.get("customer_user_id"));

      int roleId = DataBaseUtil.getIntValueFromDb(ps);
      if (!(roleId == 1 || roleId == 2)) {
        isRights = getScreenRights(roleId, rightsMap.get(apiName),
            (String) sessionParameters.get("hospital_name"));
      }
      return isRights;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
