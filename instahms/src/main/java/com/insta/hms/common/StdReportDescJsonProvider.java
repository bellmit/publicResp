package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.PreferencesDao;
import com.bob.hms.common.RequestContext;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

import flexjson.JSONDeserializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * The Class StdReportDescJsonProvider.
 *
 * @author deepasri.prasad Class to parse a StdReportDesc JSON file, to return a StdReportDesc
 *         object. The JSON files (with suffix .srjs) are placed in WEB-INF/srjs directory.
 */
public class StdReportDescJsonProvider implements StdReportDescProvider {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(StdReportDescJsonProvider.class);

  /** The base dir. */
  protected String baseDir;

  /**
   * Instantiates a new std report desc json provider.
   */
  public StdReportDescJsonProvider() {
    baseDir = AppInit.getRootRealPath();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.StdReportDescProvider#getReportDesc(java.lang.String)
   */
  public StdReportDesc getReportDesc(String descName) throws Exception {
    return getReportDescription(descName);
  }

  /**
   * Gets the report description.
   *
   * @param jsonDescName the json desc name
   * @return the report description
   * @throws Exception the exception
   */
  public StdReportDesc getReportDescription(String jsonDescName) throws Exception {
    return getReportDescription(jsonDescName, true);
  }

  /**
   * Gets the report description.
   *
   * @param jsonDescName        the json desc name
   * @param processCustomFields the process custom fields
   * @return the report description
   * @throws Exception the exception
   */
  public StdReportDesc getReportDescription(String jsonDescName, boolean processCustomFields)
      throws Exception {

    String fileName = baseDir;
    fileName = fileName + "/WEB-INF/srjs/" + jsonDescName;
    FileReader fr = new FileReader(fileName);
    StdReportDesc desc = (StdReportDesc) new JSONDeserializer().use(null, StdReportDesc.class)
        .deserialize(fr);

    List<String> includeNames = desc.getIncludes();
    if (includeNames != null) {
      List<StdReportDesc> incDescs = new ArrayList<StdReportDesc>();
      for (String incName : includeNames) {
        // process custom fields only in the main report.
        incDescs.add(getReportDescription(incName, false));
      }
      desc.setReportIncludes(incDescs, includeNames);
    }
    if (processCustomFields) {
      updateCustomFields(desc);
    }
    desc.validate();
    return desc;
  }

  /**
   * Gets the report desc for string.
   *
   * @param jsonDescName the json desc name
   * @return the report desc for string
   * @throws Exception the exception
   */
  public StdReportDesc getReportDescForString(String jsonDescName)
      throws UnsupportedEncodingException, SQLException {

    InputStream is = new ByteArrayInputStream(jsonDescName.getBytes("UTF-8"));
    Reader reader = new InputStreamReader(is);

    StdReportDesc desc = (StdReportDesc) new JSONDeserializer().use(null, StdReportDesc.class)
        .deserialize(reader);
    // string cannot have includes, only files can have includes.
    updateCustomFields(desc);
    desc.validate();
    return desc;
  }

  /**
   * Called during report processing, to handle custom fields.
   *
   * @param desc the desc
   * @return the std report desc
   * @throws SQLException the SQL exception
   */
  public StdReportDesc updateCustomFields(StdReportDesc desc) throws SQLException {

    boolean modAdvInsurance = false;

    HttpSession session = RequestContext.getSession();
    Preferences prefs = null;
    if (session == null) {
      Connection con = DataBaseUtil.getConnection();
      PreferencesDao prefsDao = new PreferencesDao(con);
      prefs = prefsDao.getPreferences();
      con.close();
    } else {
      prefs = (Preferences) session.getAttribute("preferences");
    }
    Map groups = prefs.getModulesActivatedMap();

    if (groups.containsKey("mod_adv_ins") && "Y".equals(groups.get("mod_adv_ins"))) {
      modAdvInsurance = true;
    }

    BasicDynaBean regBean = new RegistrationPreferencesDAO().getRecord();
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();

    HashMap<String, Object> customNamesMap = new HashMap<String, Object>();

    // Patient details custom fields
    for (int i = 1; i < 20; i++) {
      customNamesMap.put("custom_field" + i, regBean.get("custom_field" + i + "_label"));
    }

    // Patient details custom list fields
    for (int i = 1; i < 10; i++) {
      customNamesMap.put("custom_list" + i + "_value", regBean.get("custom_list" + i + "_name"));
    }

    // Visit details custom fields
    for (int i = 1; i < 10; i++) {
      customNamesMap.put("visit_custom_field" + i, regBean.get("visit_custom_field" + i + "_name"));
    }

    // Visit details custom list fields
    for (int i = 1; i < 3; i++) {
      customNamesMap.put("visit_custom_list" + i, regBean.get("visit_custom_list" + i + "_name"));
    }

    customNamesMap.put("family_id", regBean.get("family_id"));
    customNamesMap.put("member_id", regBean.get("member_id_label"));
    customNamesMap.put("policy_validity_start", regBean.get("member_id_valid_from_label"));
    customNamesMap.put("policy_validity_end", regBean.get("member_id_valid_to_label"));

    customNamesMap.put("government_identifier", regBean.get("government_identifier_label"));
    customNamesMap.put("identifier_id", regBean.get("government_identifier_type_label"));

    customNamesMap.put("passport_no", regBean.get("passport_no"));
    customNamesMap.put("passport_issue_country", regBean.get("passport_issue_country"));
    customNamesMap.put("passport_validity", regBean.get("passport_validity"));
    customNamesMap.put("visa_validity", regBean.get("visa_validity"));

    for (int i = 1; i <= 5; i++) {
      setDocCustomFields(customNamesMap, "" + i, (String) genPrefs.get("doctors_custom_field" + i));
    }

    if ((Integer) genPrefs.get("max_centers_inc_default") == 1) {
      customNamesMap.put("center_name", ""); // normal usage, "the" center.
      customNamesMap.put("center_city", "");
      customNamesMap.put("center_state", "");
      customNamesMap.put("center_country", "");
      customNamesMap.put("center_region", "");
      customNamesMap.put("bill_center_name", ""); // used in collection report, alt center
      customNamesMap.put("from_center_name", ""); // used in stock transfer and indent reports
      customNamesMap.put("to_center_name", ""); // used in stock transfer and indent reports
    }

    if (!modAdvInsurance) {
      customNamesMap.put("case_policy_validity_start", "");
      customNamesMap.put("case_policy_validity_end", "");
      customNamesMap.put("plan_name", "");
      customNamesMap.put("policy_validity_start", "");
      customNamesMap.put("policy_validity_end", "");
      customNamesMap.put("policy_holder_name", "");
      customNamesMap.put("patient_relationship", "");
    }

    desc.updateCustomFields(customNamesMap);
    return desc;
  }

  /**
   * Sets the doc custom fields.
   *
   * @param namesMap  the names map
   * @param suffix    the suffix
   * @param fieldName the field name
   */
  private void setDocCustomFields(HashMap namesMap, String suffix, String fieldName) {
    if (fieldName != null && !fieldName.equals("")) {
      namesMap.put("dr_custom" + suffix, "Doc " + fieldName);
      namesMap.put("ref_custom" + suffix, "Ref Doc " + fieldName);
      namesMap.put("cond_doc_custom" + suffix, "Cond Doc " + fieldName);
      namesMap.put("pres_doc_custom" + suffix, "Pres Doc " + fieldName);
    } else {
      // have to put null, or the unused fields won't get removed
      namesMap.put("dr_custom" + suffix, null);
      namesMap.put("ref_custom" + suffix, null);
      namesMap.put("cond_doc_custom" + suffix, null);
      namesMap.put("pres_doc_custom" + suffix, null);
    }
  }

}
