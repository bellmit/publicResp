package com.insta.hms.core.patient;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.govtidentifiers.GovtIdentifierRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class PatientDetailsRepository extends GenericRepository {

  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired 
  GovtIdentifierRepository govtIdentifierRepository;
  
  private static final String[] LANG_CODES = Locale.getISOLanguages();

  public PatientDetailsRepository() {
    super("patient_details");
  }

  private static final String GET_PATIENT_DETAILS_DISPLAY_VIEW = " SELECT * FROM patient_details_display_view WHERE mr_no= ? ";

  private final static String GET_BASIC_DETAILS = "SELECT * FROM patient_or_appointment_details_view ";
  private final static String GET_ADVANCED_DETAILS = "SELECT * FROM patient_header_advanced_details_view "
      + "WHERE mr_no = ?";
  private final static String GET_ADVANCED_DETAILS_VISIT = "SELECT * FROM patient_header_advanced_details_view "
      + "WHERE patient_id = ?";
  private final static String UPDATE_PATIENT_DETAILS = "UPDATE patient_details "
      + "SET patient_phone = ? , email_id = ? " + "WHERE mr_no = ?";
  private final static String UPDATE_PATIENT_PHOTO_PREFIX = "UPDATE patient_details SET ";
  private final static String UPDATE_PATIENT_PHOTO_SUFFIX = " WHERE mr_no = ?";

  private final static String GET_PATIENT_PHOTO_SUFFIX = " FROM patient_details WHERE mr_no = ?";

  private final static String UPDATE_CONTACT_PREF = "UPDATE contact_preferences SET lang_code=? where mr_no=?";
  private final static String ADD_CONTACT_PREF = "INSERT into contact_preferences (mr_no, lang_code) VALUES (?,?)";
  private final static String GET_CONTACT_PREF = "SELECT lang_code FROM contact_preferences WHERE mr_no=?";
  private final static String GET_PATIENT_VISIT_ID = "SELECT visit_id from patient_details WHERE mr_no=?";

  public BasicDynaBean getPatientVisit(String mr_no) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_VISIT_ID, mr_no);
  }

  public BasicDynaBean getPatientDetailsDisplayBean(String mr_no) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_DETAILS_DISPLAY_VIEW, mr_no);
  }

  private static final String GET_MR_NO = " SELECT mr_no FROM patient_details WHERE oldmrno= ? ";

  public BasicDynaBean getMrNoWithOldMrNoBean(String oldMrNo) {
    return DatabaseHelper.queryToDynaBean(GET_MR_NO, oldMrNo);
  }

  private static final String PATIENT_MAIL_ID = " SELECT pd.mr_no, "
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) as patient_name ,"
      + " pd.email_id, uu.emp_password" + " FROM patient_details pd "
      + " LEFT JOIN u_user uu ON (pd.mr_no = uu.emp_username)  " + " WHERE pd.mr_no= ? ";

  public BasicDynaBean getPatientMailId(String mr_no) {
    return DatabaseHelper.queryToDynaBean(PATIENT_MAIL_ID, mr_no);
  }

  private static String GET_REGISTRATION_DOC_VISIT = "select prc.patient_id,prc.doc_id,prc.doc_date, prc.doc_name,"
      + " prc.username,pd.template_id,pd.doc_format,pd.doc_status,dv.doc_type,dv.template_name, pr.status, pr.reg_date, "
      + " dv.access_rights from patient_registration_cards prc "
      + " JOIN patient_documents pd on pd.doc_id = prc.doc_id "
      + " JOIN doc_all_templates_view dv on pd.doc_format = dv.doc_format and pd.template_id=dv.template_id "
      + " JOIN patient_registration pr using (patient_id) WHERE prc.patient_id = ? ";

  private static String GET_REGISTRATION_DOC_PATIENT = "select prc.patient_id,prc.doc_id,prc.doc_date, prc.doc_name,"
      + " prc.username,pd.template_id,pd.doc_format,pd.doc_status,dv.doc_type,dv.template_name, pr.status, pr.reg_date, "
      + " dv.access_rights from patient_registration_cards prc"
      + " JOIN patient_documents pd on pd.doc_id = prc.doc_id "
      + " JOIN doc_all_templates_view dv on pd.doc_format = dv.doc_format and pd.template_id=dv.template_id "
      + " JOIN patient_registration pr using (patient_id)  WHERE pr.mr_no = ?";

  public static List<BasicDynaBean> getRegistrationDocs(Map listingParams, Map extraParams,
      Boolean specialized) {

    if (null != extraParams.get("patient_id") 
        && !((String)extraParams.get("patient_id")).isEmpty()) {
      return DatabaseHelper.queryToDynaList(GET_REGISTRATION_DOC_VISIT,
          extraParams.get("patient_id"));
    } else {
      return DatabaseHelper.queryToDynaList(GET_REGISTRATION_DOC_PATIENT, extraParams.get("mr_no"));
    }
  }

  /**
   * This method gets basic patient details
   * 
   * @param mrNo
   * @param appointmentId
   * @return basic patient details
   */
  public BasicDynaBean getBasicDetails(String mrNo, String appointmentId) {

    StringBuffer query = new StringBuffer(GET_BASIC_DETAILS);
    List<BasicDynaBean> patientBasicDetails = null;

    if (mrNo != null) {
      query.append("WHERE mr_no = ?");
      patientBasicDetails = DatabaseHelper.queryToDynaList(query.toString(), mrNo);
    } else { // if mr_no is null then return appointment details
      query.append("WHERE appointment_id = ?");
      patientBasicDetails = DatabaseHelper.queryToDynaList(query.toString(), appointmentId);
    }

    if (patientBasicDetails.size() > 0) {
      return patientBasicDetails.get(0);
    } else {
      return null;
    }
  }

  /**
   * This method get advanced patient details
   * 
   * @param mrNo
   * @param visitId
   * @return advanced patient details
   */
  public BasicDynaBean getAdvancedDetails(String mrNo, String visitId) {
    BasicDynaBean patientAdvancedDetails = null;
    StringBuffer query = null;

    if (visitId == null || visitId.isEmpty()) {
      query = new StringBuffer(GET_ADVANCED_DETAILS);
      patientAdvancedDetails = DatabaseHelper.queryToDynaBean(query.toString(), mrNo);
    } else {
      query = new StringBuffer(GET_ADVANCED_DETAILS_VISIT);
      patientAdvancedDetails = DatabaseHelper.queryToDynaBean(query.toString(), visitId);
    }

    return patientAdvancedDetails;
  }

  /**
   * This method updates patient details
   * 
   * @param mrNo
   * @param patientContact
   * @param emailId
   * @return updated patient details
   */
  public Integer updatePatientDetails(String mrNo, String patientContact, String emailId) {

    StringBuffer query = new StringBuffer(UPDATE_PATIENT_DETAILS);

    return DatabaseHelper.update(query.toString(), patientContact, emailId, mrNo);
  }

  /**
   * This method uploads photo
   * 
   * @param mrNo
   * @param column
   * @param patientPhoto
   * @return
   */
  public Boolean uploadPhoto(String mrNo, byte[] patientPhoto, byte[] scaled36x36,
      byte[] scaled46x46, byte[] scaled66x66, byte[] scaled84x84) {
    StringBuffer query = new StringBuffer(UPDATE_PATIENT_PHOTO_PREFIX);
    query.append("patient_photo = ?, ");
    query.append("patient_photo_36x36 = ?, ");
    query.append("patient_photo_46x46 = ?, ");
    query.append("patient_photo_66x66 = ?, ");
    query.append("patient_photo_84x84 = ? ");
    query.append(UPDATE_PATIENT_PHOTO_SUFFIX);
    return DatabaseHelper.update(query.toString(), patientPhoto, scaled36x36, scaled46x46,
        scaled66x66, scaled84x84, mrNo) > 0;
  }

  /**
   * This method gets photo
   * 
   * @param mrNo,
   *          columnName
   * @return
   */
  public BasicDynaBean getPhoto(String mrNo, String columnName) {
    StringBuffer query = new StringBuffer("SELECT ");

    query.append(DatabaseHelper.quoteIdent(columnName));
    query.append(GET_PATIENT_PHOTO_SUFFIX);
    return DatabaseHelper.queryToDynaBean(query.toString(), mrNo);
  }

  /**
   * This method checks if mr number is valid or not
   */
  public Boolean isMrNumberValid(String mrNumber) {
    // check if mr number is present in the database
    BasicDynaBean bean = findByKey("mr_no", mrNumber);
    return bean != null;
  }

  private static final String GET_BABY_DETAILS_FOR_MRNO = " SELECT pd.dateofbirth,sm.salutation,adm.parent_id, pd.visit_id,"
      + " pr.patient_id as parent_mr_no, parentpd.government_identifier as parent_government_identifier"
      + " FROM patient_details pd "
      + " LEFT JOIN salutation_master sm ON(pd.salutation = sm.salutation_id) "
      + " JOIN admission adm ON (adm.mr_no = ?)"
      + " LEFT JOIN patient_registration as pr ON (adm.parent_id = pr.patient_id) "
      + " LEFT JOIN patient_details as parentpd ON (pr.patient_id = parentpd.mr_no)"
      + " WHERE (adm.parent_id is not null AND adm.parent_id != '') AND  pd.mr_no = ? ";

  public BasicDynaBean getBabyDOBAndSalutation(String mrNo) {
    return DatabaseHelper.queryToDynaBean(GET_BABY_DETAILS_FOR_MRNO, new Object[] { mrNo, mrNo });
  }

  public String getPatientGender(String mrNo) {
    return DatabaseHelper.getString("SELECT patient_gender from patient_details where mr_no = ?",
        mrNo);
  }

  private static final String GET_REG_DATE_REG_CHARGE_ACCEPTED = " SELECT pr.reg_date AS visit_reg_date, pd.first_visit_reg_date as patient_reg_date "
      + " FROM patient_details pd "
      + "   LEFT JOIN patient_registration pr ON (pr.mr_no = pd.mr_no AND (pr.reg_charge_accepted = 'Y' OR pd.resource_captured_from = 'uhid')) "
      + " WHERE pd.mr_no=? " + " ORDER BY pr.reg_date DESC LIMIT 1";

  public BasicDynaBean getPreviousRegDateChargeAccepted(Object[] object) {
    return DatabaseHelper.queryToDynaBean(GET_REG_DATE_REG_CHARGE_ACCEPTED, object);
  }

  private static final String GENERAL_PATIENT_DETAILS = " SELECT * FROM patient_details_display_view WHERE mr_no= ? ";

  public static BasicDynaBean getPatientGeneralDetailsBean(String mr_no) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GENERAL_PATIENT_DETAILS, mr_no);
    if (bean != null) {
      boolean precise = (bean.get("dateofbirth") != null);
      if (bean.get("expected_dob") != null)
        bean.set("age_text",
            DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise));
    }
    return bean;
  }

  private static final String INSURANCE_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_insurance_plans " + " JOIN plan_docs_details pdd USING(patient_policy_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND patient_policy_id!=0  ORDER BY pdd.doc_id DESC  ";

  private static final String CORPORATE_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration "
      + " JOIN corporate_docs_details pdd USING(patient_corporate_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND patient_corporate_id!=0  ORDER BY pdd.doc_id DESC  ";

  private static final String NATIONAL_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration "
      + " JOIN national_sponsor_docs_details pdd USING(patient_national_sponsor_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND patient_national_sponsor_id!=0 ORDER BY pdd.doc_id DESC ";

  public static BasicDynaBean getCurrentPatientCardImage(String patientId, String sponsorType) {
    if (patientId == null)
      return null;

    if (sponsorType == null || sponsorType.equals("I"))
      return DatabaseHelper.queryToDynaBean(INSURANCE_CARD_IMAGE, patientId);
    else if (sponsorType.equals("C"))
      return DatabaseHelper.queryToDynaBean(CORPORATE_CARD_IMAGE, patientId);
    else if (sponsorType.equals("N"))
      return DatabaseHelper.queryToDynaBean(NATIONAL_CARD_IMAGE, patientId);
    else
      return null;
  }

  public List<BasicDynaBean> listExistingPatientDetails(Map<String, Object> params)
      throws ParseException {
    String dob = params.get("dateofbirth") != null ? params.get("dateofbirth").toString() : null;
    String patientPhone = params.get("patient_phone") != null
        ? params.get("patient_phone").toString() : null;
    if (patientPhone != null && !patientPhone.isEmpty()) {
      List<String> parts = PhoneNumberUtil.getCountryCodeAndNationalPart(patientPhone, null);
      if (parts == null || parts.isEmpty() || parts.get(0).isEmpty()) {
        int centerId = RequestContext.getCenterId();
        String defaultCode = centerService.getCountryCode(centerId);
        if (defaultCode == null) {
          defaultCode = centerService.getCountryCode(0);
        }
        if (defaultCode != null && !defaultCode.equals("")) {
          patientPhone = "+" + defaultCode + patientPhone;
        }
      }
    }


    String governmentIdentifier = params.get("government_identifier") != null
        ? params.get("government_identifier").toString() : null;
    boolean uniqueGovtIdRequired=false;
	if(!StringUtil.isNullOrEmpty(governmentIdentifier)){
	    Integer governmentIdentifierType = (Integer)params.get("identifier_id");
	    Map<String, Object> filterMap = new HashMap<>();
	    filterMap.put("unique_id", "Y");
	    filterMap.put("identifier_id", governmentIdentifierType); 
	    uniqueGovtIdRequired=govtIdentifierRepository.findByKey(filterMap)!=null;
	}    
    StringBuilder q1 = new StringBuilder();
    q1.append(" SELECT mr_no, patient_name, middle_name, last_name, dateofbirth, expected_dob, "
        + " to_char(dateofbirth,'dd-MM-yyyy')::text AS dob, to_char(expected_dob,'dd-MM-yyyy')::text AS eob, "
        + " get_patient_age(dateofbirth, expected_dob)::text AS age,  "
        + " get_patient_age_in(dateofbirth, expected_dob)::text AS agein, "
        + " CASE WHEN patient_gender='M' THEN 'Male' WHEN patient_gender='F' THEN 'Female' ELSE 'Other' END AS patient_gender,"
        + " patient_phone, patient_address, patient_city, city_name, patient_area, government_identifier"
        + " FROM patient_details " + " JOIN city ON (city_id = patient_city) "
        + " WHERE (lower(patient_name) LIKE ? AND lower(middle_name) LIKE ? AND lower(last_name) LIKE ? AND patient_gender = ? ");	
	
    if (null == dob || ("").equals(dob)) {
      q1.append(" and  (select get_patient_age(null, expected_dob)) = ? ");
    } else {
      q1.append(" and dateofbirth = ? ");
    }
    q1.append(")");

    if (null != patientPhone && (!"".equals(patientPhone))) {
      q1.append(" OR ( patient_phone = ? )");
    }
	if (uniqueGovtIdRequired) {
		q1.append(" OR ( lower(government_identifier) = ? )");
	}
    q1.append(" ORDER BY mr_no DESC ");

    List<Object> values = new ArrayList<Object>();
    values.add(((String) params.get("patient_name")).toLowerCase());
    values.add(params.get("middle_name")!= null 
        ? ((String) params.get("middle_name")).toLowerCase() 
            : null);
    values.add(params.get("last_name")!= null 
        ? ((String) params.get("last_name")).toLowerCase() 
            : null);
    values.add(params.get("patient_gender"));

    if (null == dob || "".equals(dob) && params.get("age") != null)
      values.add(Integer.parseInt(params.get("age").toString()));
    else {
      java.sql.Date dateOfBirth = null;
      SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy");
      if (!"".equals(dob)) {
        dateOfBirth = new java.sql.Date((sf.parse((dob))).getTime());
      }
      values.add(dateOfBirth);
    }

    if (null != patientPhone && (!patientPhone.equals(""))) {
      values.add(patientPhone);
    }

    if (uniqueGovtIdRequired) {
      values.add(governmentIdentifier.toLowerCase());
    }
    return DatabaseHelper.queryToDynaList(q1.toString(), values.toArray());
  }
	
  private static final String GET_PATIENT_DETAILS = "SELECT pd.*, pr.center_id FROM patient_registration pr "
      + " JOIN patient_details pd USING (mr_no) WHERE pr.patient_id = ? ";

  public BasicDynaBean getPatientDetailsForVisit(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_DETAILS, new Object[] { visitId });
  }

  /**
   * Return list of Languages with relevant meta data.
   * 
   * @param userLangCode Current User Interface Language
   * @return List of ISO-639 language codes along with display name for that 
   *         language in current UI language and the language itself. 
   */
  public List<Map<String,String>> getPreferredLanguages(String userLangCode) {
    List<Map<String,String>> langList = new ArrayList<>();
    for (String langCode : LANG_CODES) {
      Map<String,String> langMap = new HashMap<>();
      Locale userLocale = Locale.forLanguageTag(userLangCode);
      Locale langLocale = Locale.forLanguageTag(langCode);
      String langDisplay = langLocale.getDisplayLanguage(userLocale);
      if (!langLocale.getDisplayLanguage(userLocale).equals(
          langLocale.getDisplayLanguage(langLocale))) {
        langDisplay += " / " + langLocale.getDisplayLanguage(langLocale);
      }
      langMap.put("lang_code", langCode);
      langMap.put("lang_name", langDisplay);
      langMap.put("language", langDisplay);
      langList.add(langMap);
    }
    
    return langList;
  }

  public Integer updateContactPrefLangCode(String mrNo, String contactPrefLangCode) {

    String contactPref = DatabaseHelper.getString(GET_CONTACT_PREF, mrNo);
    Integer result;

    if (contactPref == null || contactPref.isEmpty()) {
      result = DatabaseHelper.insert(ADD_CONTACT_PREF, mrNo, contactPrefLangCode);
    } else {
      result = DatabaseHelper.update(UPDATE_CONTACT_PREF, contactPrefLangCode, mrNo);
    }

    return result;
  }

  private static final String BABY_OR_MOTHER_DETAILS = "SELECT a2.isBaby, a1.mr_no AS mrno1, "
      + " a1.patient_id AS pat1 ,a2.mr_no AS mrno2, a2.patient_id AS pat2, pd.patient_name, "
      + " pd.middle_name FROM patient_details pd, admission a1 "
      + " JOIN admission a2 ON a1.patient_id = a2.parent_id "
      + " WHERE pd.mr_no=a1.mr_no AND a2.patient_id=?  UNION "
      + " SELECT a1.isBaby, a1.mr_no AS mrno1,a1.patient_id AS pat1 ,"
      + " a2.mr_no AS mrno2, a2.patient_id AS pat2, pd.patient_name, pd.middle_name "
      + " FROM patient_details pd,admission a1 "
      + " JOIN admission a2 ON a1.patient_id = a2.parent_id "
      + " WHERE pd.mr_no=a2.mr_no AND a1.patient_id= ? ";

  public List<BasicDynaBean> getBabyOrMotherDetails(String patientId) {
    return DatabaseHelper.queryToDynaList(BABY_OR_MOTHER_DETAILS,
        new Object[] { patientId, patientId });
  }

  private static final String COUNT_PATIENTS_MRNO_USERGROUP = "SELECT count(*) from patient_details "
      + "where mr_no in (:mrno) and patient_group in (:usergroup)";
  
  private static final String COUNT_BREAK_THE_GLASS_MRNO = "SELECT count(*) from user_mrno_association "
      + "where mr_no in (:mrno) and emp_username = (:currentuser)";

  public Integer getCountPatientForMrNoAndUserGroup(List<String> mrno, List<Integer> userGroups,
      String userName) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mrno", mrno);
    parameters.addValue("usergroup", userGroups);
    Integer accesibleMrNo = DatabaseHelper.getInteger(COUNT_PATIENTS_MRNO_USERGROUP, parameters);
    MapSqlParameterSource breakTheGlassParams = new MapSqlParameterSource();
    breakTheGlassParams.addValue("mrno", mrno);
    breakTheGlassParams.addValue("currentuser", userName);
    Integer breakTheGlass = DatabaseHelper.getInteger(COUNT_BREAK_THE_GLASS_MRNO,
        breakTheGlassParams);
    return accesibleMrNo + breakTheGlass;
  }

  private static final String GET_MR_NO_FOR_VISIT_ID = "Select mr_no from patient_registration where patient_id in (:visitid)";

  public List<String> getMrNoForVisitId(List<String> visitId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("visitid", visitId);
    List<BasicDynaBean> listBeanMrNo = DatabaseHelper.queryToDynaList(GET_MR_NO_FOR_VISIT_ID,
        parameters);
    List<String> mrNos = new ArrayList<>(listBeanMrNo.size());
    for (BasicDynaBean bean : listBeanMrNo) {
      String thisMrNo = (String) bean.get("mr_no");
      if (thisMrNo != null && !thisMrNo.isEmpty()) {
        mrNos.add(thisMrNo);
      }
    }
    return mrNos.isEmpty() ? null : mrNos;
  }

  private static final String GET_CONFIDENTIALITY_GRP_BREAK_THE_GLASS = "Select "
      + "break_the_glass,patient_group from confidentiality_grp_master cgm "
      + "JOIN patient_details pd ON (pd.patient_group = cgm.confidentiality_grp_id) "
      + "where pd.mr_no = (:mrno)";

  public Boolean isBreakTheGlassAllowed(String mrno) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mrno", mrno);
    BasicDynaBean patientBean = DatabaseHelper
        .queryToDynaBean(GET_CONFIDENTIALITY_GRP_BREAK_THE_GLASS, parameters);
    if (patientBean != null) {
      Integer patientGroup = (Integer) patientBean.get("patient_group");
      String breakTheGlass = (String) patientBean.get("break_the_glass");
      if (patientGroup != 0 && breakTheGlass.equals("Y")) {
        return true;
      }
    }
    return false;
  }
  
  private final String CHECK_IF_PATIENT_EXISTS = 
      "select * from patient_details where "
        + "CASE WHEN (middle_name IS NULL OR middle_name = '')"
        + " THEN trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',trim(lower(COALESCE(last_name,'')))))"
        + " ELSE trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',trim(lower(COALESCE(middle_name,''))),' ',trim(lower(COALESCE(last_name,''))))) "
        + " END = :fullName"
        + " AND (patient_phone = :patientContact OR patient_phone2 = :patientContact)"
        + " #SALUTATION_CHECK# #GENDER_CHECK#";
  
  /** The Constant SALUTATION_CHECK. */
  private static final String SALUTATION_CHECK = " AND salutation = :salutation ";
  
  /** The Constant GENDER_CHECK. */
  private static final String GENDER_CHECK = " AND patient_gender = :gender ";
  
  
  @SuppressWarnings("rawtypes")
  public Boolean checkIfPatientExists(Map bean) {
    String query = CHECK_IF_PATIENT_EXISTS;
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
    BasicDynaBean contactBean = contactList != null && contactList.size() > 0 ? contactList.get(0) : null;
    if (contactBean != null) {
      return true;
    }
    return false;
  }
}
