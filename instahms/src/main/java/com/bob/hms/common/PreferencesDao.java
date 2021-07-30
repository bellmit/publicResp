package com.bob.hms.common;

import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The Class PreferencesDAO.
 */
public class PreferencesDao {

  private Connection con = null;

  /**
   * Instantiates a new preferences DAO.
   *
   * @param con the con
   */
  public PreferencesDao(Connection con) {
    this.con = con;
  }

  private static final String GET_PREFERENCES = "SELECT * FROM registration_preferences";

  private static final String ACTIVATED_MODULES = "SELECT module_id"
      + " FROM modules_activated  WHERE activation_status = 'Y'";

  private static final String PREFIX_LENGTHS = "SELECT pattern_id, std_prefix, num_pattern"
      + " FROM hosp_id_patterns " + " WHERE pattern_id in ('MRNO', 'BILL_DEFAULT')";

  /**
   * Gets the preferences.
   *
   * @return the preferences
   * @throws SQLException the SQL exception
   */
  public Preferences getPreferences() throws SQLException {
    Preferences prefs = new Preferences();

    PreparedStatement ps = con.prepareStatement(GET_PREFERENCES);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      prefs.setIpCredit(rs.getInt("ip_credit"));
      prefs.setIpValidityDays(rs.getInt("ip_validity_days"));
      prefs.setOpValidityDays(rs.getInt("op_validity_days"));
      prefs.setOpValidityPeriod(rs.getInt("op_validity_period"));
      prefs.setOpConsultationValidity(rs.getInt("op_cons_validity"));
      prefs.setOpConsultationValidityType(rs.getString("op_cons_validity_type"));
      prefs.setGrace(rs.getInt("grace"));
      prefs.setScda(rs.getInt("scda"));
      prefs.setReceiptRequired(rs.getString("receipt_require"));
      prefs.setNightPm(rs.getInt("night_pm"));
      prefs.setNightAm(rs.getInt("night_am"));
      prefs.setGeneralChargeCollect(rs.getBoolean("gen_charge_collect"));
      prefs.setIpOp(rs.getBoolean("ip_op"));
      prefs.setOpIp(rs.getBoolean("op_ip"));
      /*
       * prefs.setReg_custom_field1_name(rs.getString("custom_field1_label"));
       * prefs.setReg_custom_field2_name(rs.getString("custom_field2_label"));
       * prefs.setReg_custom_field3_name(rs.getString("custom_field3_label"));
       */
    }
    rs.close();
    ps.close();

    ps = con.prepareStatement(ACTIVATED_MODULES);
    ArrayList modulesActivated = DataBaseUtil.queryToArrayList1(ps);
    ps.close();

    HashMap modulesActivatedMap = new HashMap();

    for (Iterator m = modulesActivated.iterator(); m.hasNext();) {
      String module = (String) m.next();
      modulesActivatedMap.put(module, "Y");
    }

    boolean modEclaimPbm = false;
    boolean modEclaimErx = false;
    boolean modEclaimPreauth = false;
    if (modulesActivatedMap.containsKey("mod_eclaim_pbm")
        && "Y".equals(modulesActivatedMap.get("mod_eclaim_pbm"))) {
      modEclaimPbm = true;
    }
    if (modulesActivatedMap.containsKey("mod_eclaim_erx")
        && "Y".equals(modulesActivatedMap.get("mod_eclaim_erx"))) {
      modEclaimErx = true;
    }
    if (modulesActivatedMap.containsKey("mod_eclaim_preauth")
        && "Y".equals(modulesActivatedMap.get("mod_eclaim_preauth"))) {
      modEclaimPreauth = true;
    }

    // Add the modules based on authority and modules enabled.
    Integer userCenter = RequestContext.getCenterId();
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(userCenter);
    if (healthAuthority != null
        && (healthAuthority.equals("DHA") || healthAuthority.equals("HAAD"))) {

      // Remove the modules if active.
      if (modEclaimPbm) {
        modulesActivatedMap.remove("mod_eclaim_pbm");
      }

      if (modEclaimErx) {
        modulesActivatedMap.remove("mod_eclaim_erx");
      }

      // if (mod_eclaim_preauth)
      // modulesActivatedMap.remove("mod_eclaim_preauth");

      if (healthAuthority.equals("DHA") && modEclaimErx && !modEclaimPbm) {
        modulesActivatedMap.put("mod_eclaim_erx", "Y");

      } else if (healthAuthority.equals("HAAD") && !modEclaimErx && modEclaimPbm) {
        modulesActivatedMap.put("mod_eclaim_pbm", "Y");
      }

      if (modEclaimErx && modEclaimPbm) {
        if (healthAuthority.equals("DHA")) {
          modulesActivatedMap.put("mod_eclaim_erx", "Y");
          modulesActivatedMap.put("mod_eclaim_pbm", "Y");

        } else if (healthAuthority.equals("HAAD")) {
          modulesActivatedMap.put("mod_eclaim_pbm", "Y");
        }
      }
    }

    prefs.setModulesActivatedMap(modulesActivatedMap);

    try (PreparedStatement ps1 = con.prepareStatement(PREFIX_LENGTHS);
        ResultSet rs1 = ps1.executeQuery()) {
      while (rs1.next()) {
        String patternId = rs1.getString("pattern_id");
        String prefix = rs1.getString("std_prefix");
        String pattern = rs1.getString("num_pattern");
        if (patternId.equals("MRNO")) {
          prefs.setMrNoPrefix(prefix);
          prefs.setMrNoDigits(pattern.length() - 2);
        } else if (patternId.equals("BILL_DEFAULT")) {
          prefs.setBillNoPrefix(prefix);
          prefs.setBillNoDigits(pattern.length() - 2);
        }
      }
    }
    return prefs;
  }

}
