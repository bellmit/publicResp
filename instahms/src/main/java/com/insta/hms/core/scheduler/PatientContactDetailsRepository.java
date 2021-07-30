package com.insta.hms.core.scheduler;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.patient.outpatientlist.PatientSearchRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientContactDetailsRepository.
 */
@Repository
public class PatientContactDetailsRepository extends GenericRepository {

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;
  
  /** The patient search repository. */
  @LazyAutowired
  private PatientSearchRepository patientSearchRepository;

  /** The field map. */
  private Map<String, Object> fieldMap = new HashMap<String, Object>();
  
  /** The default match type. */
  private Map<String, String> defaultMatchType = new HashMap<String, String>();

  /**
   * Instantiates a new patient contact details repository.
   */
  public PatientContactDetailsRepository() {
    super("contact_details");
    defaultMatchType.put("name", "contains");
    defaultMatchType.put("phone_number", "starts_with");
    fieldMap.put("name",
        Arrays.asList("LOWER(cd.patient_name)", "LOWER(cd.middle_name)", "LOWER(cd.last_name)"));
    fieldMap.put("phone_number",Arrays.asList("cd.patient_contact",
                "replace(cd.patient_contact, CASE WHEN cd.patient_contact_country_code IS NULL "
                + " THEN '' ELSE cd.patient_contact_country_code END,'')"));
  }

  /** The search on values. */
  private final List<String> searchOnValues = Arrays.asList("name", "phone_number");
  
  /** The match type values. */
  private final List<String> matchTypeValues = Arrays.asList("starts_with", "exact_match");


  /**
   * Search available contacts.
   *
   * @param params the params
   * @return the map
   */
  public Map searchAvailableContacts(MultiValueMap<String, String> params) {
    String findString = (params.getFirst("q") != null && !params.getFirst("q").isEmpty()) ? params
        .getFirst("q").trim() : "";
    Map<String, Object> mapResults = new HashMap<String, Object>();
    if (findString.equals("")) {
      mapResults.put("page_size", 15);
      mapResults.put("page_num", 1);
      mapResults.put("total_records", 0);
      mapResults.put("patients", new ArrayList<Object>());
      return mapResults;
    }
    String matchType = (params.getFirst("match_type") != null && matchTypeValues.contains(params
        .getFirst("match_type").toLowerCase())) ? params.getFirst("match_type").toLowerCase()
        : null;
    Integer pageSize = params.getFirst("page_size") != null ? Integer.parseInt(params
        .getFirst("page_size")) : 15;
    int pageNum = params.getFirst("page_num") != null ? Integer.parseInt(params
        .getFirst("page_num")) : 1;
    pageNum = pageNum < 0 ? 15 : pageNum;
    pageSize = pageSize < 0 ? 15 : pageSize;
    pageSize = pageSize > 100 ? 100 : pageSize;
    int roleId = RequestContext.getRoleId();
    Map urlRightsMap = (Map) RequestContext.getSession().getAttribute("urlRightsMap");
    Map actionRightsMap = (Map) RequestContext.getSession().getAttribute("actionRightsMap");
    int centerId = RequestContext.getCenterId();
    // All center access available to InstaAdmin, admin roles.
    // For other roles it is allowed if user has screen access to Registration or Orders
    // and action right "Show Other Center Patients on Reg Screen".
    // Action right is discarded if system pref "Registration -> Selection For Mrno Search" is No
    // Refer Bugzilla BUG 45807 for center filter for discard condition
    boolean allCenterAccess = roleId == 1 || roleId == 2;
    allCenterAccess = allCenterAccess
        || (actionRightsMap.get("show_other_center_patients").equals("A"));

    List<String> filterParams = patientSearchRepository.getArgsList(params.get("filters"));
    List<String> joins = new ArrayList<String>();
    List<String> filters = new ArrayList<String>();
    List<String> searchOnList = new ArrayList<String>();
    List<Object> args = new ArrayList<Object>();
    String searchOn = (params.getFirst("search_on") != null && searchOnValues.contains(params
        .getFirst("search_on").toLowerCase())) ? params.getFirst("search_on").toLowerCase() : null;
    if (searchOn == null) {
      Boolean found = false;
      if (findString.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
        searchOnList.add("phone_number");
        found = true;
      }
      if (findString.matches("\\D+")) {
        searchOnList.add("name");
        found = true;
      }
      if (!found) {
        mapResults.put("page_size", 15);
        mapResults.put("page_num", 1);
        mapResults.put("total_records", 0);
        mapResults.put("patients", new ArrayList<Object>());
        return mapResults;
      }
    } else {
      searchOnList.add(searchOn);
    }
    if (!findString.isEmpty()) {
      String[] stringParts = findString.split(" ");
      for (String stringPart : stringParts) {
        if (stringPart.isEmpty()) {
          continue;
        }
        List<String> partFilter = new ArrayList<String>();
        for (String filterField : searchOnList) {
          if (!fieldMap.containsKey(filterField)) {
            continue;
          }
          String fieldMatchType = matchType;
          if (fieldMatchType == null) {
            fieldMatchType = defaultMatchType.get(filterField);
          }
          Object obj = fieldMap.get(filterField);
          if (obj instanceof List) {
            List<String> list = (List<String>) obj;
            for (String item : list) {
              boolean hasLower = item.toUpperCase().startsWith("LOWER");
              String value = hasLower ? stringPart.toLowerCase() : stringPart;
              if (fieldMatchType.equalsIgnoreCase("starts_with")) {
                partFilter.add(item + " LIKE ?");
                args.add(value + "%");
              } else if (fieldMatchType.equalsIgnoreCase("contains")) {
                partFilter.add(item + " LIKE ?");
                args.add("%" + value + "%");
              }
            }
          }
        }
        if (!partFilter.isEmpty()) {
          filters.add("(" + StringUtils.collectionToDelimitedString(partFilter, " OR ") + ")");
        }
      }
    }
    joins.add("salutation_master sm on sm.salutation_id = cd.salutation_name");
    List<String> groupBy = new ArrayList<String>();
    List<String> sortOn = new ArrayList<String>();
    List<String> fields = new ArrayList<String>(
        Arrays
            .asList(
                "cd.contact_id",
                "cd.patient_name",
                "cd.middle_name",
                "cd.last_name",
                "cd.patient_gender",
                "cd.patient_dob as dob",
                "cd.patient_contact as patient_phone",
                "cd.patient_contact_country_code as patient_phone_country_code",
                "cd.vip_status",
                "sm.salutation_id",
                "cd.patient_email_id",
                "CONCAT_WS(' ', sm.salutation, cd.patient_name, cd.middle_name, cd.last_name)"
                + " AS full_name",
                "COALESCE("
                + "CONCAT_WS('', cd.patient_age , cd.patient_age_units),"
                + "CASE WHEN AGE(patient_dob) >= interval '5 year' "
                    + "     THEN to_char(AGE(patient_dob), 'FMYYYY\"Y\"') "
                    + "     WHEN AGE(patient_dob) >= interval '1 year' "
                    + "     THEN to_char(AGE(patient_dob), 'FMYYYY\"Y\"+FMMM\"M\"') "
                    + "     WHEN AGE(patient_dob) >= interval '1 month' "
                    + "     THEN to_char(AGE(patient_dob), 'FMMM\"M\"') "
                    + "     ELSE to_char(AGE(patient_dob), 'FMDD\"D\"') END) as age_text"));
    String querySql = patientSearchRepository.getQuery("contact_details cd", 
        fields, joins, null, filters, groupBy, sortOn);
    if (pageSize != 0) {
      querySql += " LIMIT ?";
      args.add(pageSize);
    }

    if (pageNum != 0) {
      querySql += " OFFSET ?";
      args.add((pageNum - 1) * pageSize);
    }
    List<BasicDynaBean> results = null;
    results = DatabaseHelper.queryToDynaList(querySql, args.toArray());
    mapResults.put("page_size", pageSize);
    mapResults.put("page_num", pageNum);
    if (results == null || results.isEmpty()) {
      mapResults.put("total_records", 0);
      mapResults.put("patients", new ArrayList<Object>());

      return mapResults;
    }
    mapResults.put("total_records", 0);
    Map<String, Map<String, Object>> patientMap = new HashMap<String, Map<String, Object>>();
    for (BasicDynaBean result : results) {
      Map<String, Object> resultMap = new HashMap<String, Object>(result.getMap());
      resultMap.remove("cn");
      patientMap.put(((Integer) resultMap.get("contact_id")).toString(), resultMap);
    }
    mapResults.put("patients", patientMap.values());

    return mapResults;

  }
  
  /** The Constant CHECK_IF_CONTACT_EXISTS. */
  private static String CHECK_IF_CONTACT_EXISTS = 
      "select * from contact_details where"
          + " CASE WHEN (middle_name IS NULL OR middle_name = '')"
          + " THEN trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',"
          + " trim(lower(COALESCE(last_name,'')))))"
          + " ELSE trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',"
          + " trim(lower(COALESCE(middle_name,''))),' ',"
          + " trim(lower(COALESCE(last_name,''))))) "
          + " END = :fullName"
          + " AND patient_contact = :patientContact"
          + " #SALUTATION_CHECK# #GENDER_CHECK#";
  
  /** The Constant SALUTATION_CHECK. */
  private static final String SALUTATION_CHECK = " AND salutation_name = :salutation ";
  
  /** The Constant GENDER_CHECK. */
  private static final String GENDER_CHECK = " AND patient_gender = :gender ";
  
  /**
   * Gets the contact id if contact exists.
   *
   * @param bean the bean
   * @return the contact id if contact exists
   */
  @SuppressWarnings("rawtypes")
  public Integer getContactIdIfContactExists(Map bean) {
    String query = CHECK_IF_CONTACT_EXISTS;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    
    String salutation = (String) bean.get("salutation_name");
    if (salutation != null && !salutation.equals("")) {
      query = query.replace("#SALUTATION_CHECK#", SALUTATION_CHECK);
      parameters.addValue("salutation", salutation);
    } else {
      query = query.replace("#SALUTATION_CHECK#", "");
    }
    
    String gender = (String) bean.get("patient_gender");
    if (gender != null && !gender.equals("")) {
      query = query.replace("#GENDER_CHECK#", GENDER_CHECK);
      parameters.addValue("gender", gender);
    } else {
      query = query.replace("#GENDER_CHECK#", "");
    }
    
    String fullName = (String)bean.get("patient_name") 
        + " "
        + (bean.get("last_name") != null ? ((String)bean.get("last_name")) : ""); 
    parameters.addValue("fullName", fullName.trim().toLowerCase());
    
    String patientContact = (String)bean.get("patient_contact");
    parameters.addValue("patientContact", patientContact);
    
    List<BasicDynaBean> contactList = DatabaseHelper.queryToDynaList(query, parameters);
    BasicDynaBean contactBean = 
        contactList != null  && contactList.size() > 0 ? contactList.get(0) : null;
    
    if (contactBean != null) {
      return (Integer) contactBean.get("contact_id");
    }
    return null;
  }
  
  private static final String UPDATE_CONTACT_DETAILS_CONTACT_PATIENT = 
      "Update contact_details set send_sms = :sendSms, send_email = :sendEmail"
      + " where contact_id = :contactId ";

  /**
   * update contact.
   *
   * @param patientInfo
   * 
   */
  public static void updateContact(Map<String, Object> patientInfo) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    if (!StringUtils.isEmpty(patientInfo.get("contact_id"))
        && !StringUtils.isEmpty(patientInfo.get("send_sms"))
        && !StringUtils.isEmpty(patientInfo.get("send_email"))) {
      parameters.addValue("contactId", patientInfo.get("contact_id"));
      parameters.addValue("sendSms", patientInfo.get("send_sms"));
      parameters.addValue("sendEmail", patientInfo.get("send_email"));
      String query = UPDATE_CONTACT_DETAILS_CONTACT_PATIENT;
      DatabaseHelper.update(query, parameters);
    }
  }

}
