package com.insta.hms.master.RegistrationPreferences;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesCache;
import com.insta.hms.master.PatientHeaderPref.PatientHeaderPrefDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class RegistrationPreferencesDAO extends GenericDAO {

	Connection con = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	private String table= null;

	private static Map<String, List<CustomField>> cachedCustomFields =
		new HashMap<String, List<CustomField>>();
	private static Map<String, List<CustomField>> cachedVisitCustomFields =
		new HashMap<String, List<CustomField>>();
	private static Map<String, RegistrationPreferencesDTO> cachedPreferences =
		new HashMap<String, RegistrationPreferencesDTO>();
	private static final String[] LANG_CODES = Locale.getISOLanguages();

	public RegistrationPreferencesDAO(String table){
		super(table);
		this.table = table;
	}

	public  RegistrationPreferencesDAO() {
		super("registration_preferences");
	}

	public static void patientDetailsCustomFields(String showClinicalInfo) throws SQLException {
		HttpServletRequest request = RequestContext.getHttpRequest();
		String memberIdLabel = "";
		java.util.List<CustomField>
			customFieldsList = getCachedCustomFields();
		// do not modify the original cached custom fields list copy the list into another and manipulate that.
		java.util.List<CustomField> tempFields = new java.util.ArrayList<CustomField>(customFieldsList);
		java.util.ListIterator listIterator = tempFields.listIterator();
		while (listIterator.hasNext()) {
			// remove the custom fields from list which are not required to display
			//(i.e., whose label is empty or showInPatientdemography set to "N" ).
			CustomField field =	(CustomField) listIterator.next();
			String showInClinical = (String) field.getShowInClinical();
			String showInOthers = (String) field.getShowInOther();
			String txnColName = (String) field.getTxColumnName();
			showInClinical = showInClinical == null ? "" : showInClinical.trim();
			showInOthers = showInOthers == null ? "" : showInOthers.trim();
			showClinicalInfo = showClinicalInfo == null ? "false" : showClinicalInfo;

			String label = (String) field.label;
			label = (label == null) ? "" : label;
			// showInClinical and showInOthers are of type character(1) in database, that is the reason it occupies one character space
			// irrespective of the value exist or not.
			if (txnColName.equals("member_id_label")) {
				memberIdLabel = label;
				listIterator.remove();
				continue;
			}
			if (label.equals("") || (showInClinical.equals("") && showInOthers.equals(""))) {
				listIterator.remove();
			} else if (showClinicalInfo.equals("true") && showInClinical.equals("")) {
				listIterator.remove();
			} else if (showClinicalInfo.equals("false") && showInOthers.equals("")) {
				listIterator.remove();
			}
		}
		request.setAttribute("customFields", tempFields);
		request.setAttribute("memberIdLabel", memberIdLabel);
	}

	public static void patientVisitDetailsCustomFields(String showClinicalInfo) throws SQLException {
		HttpServletRequest request = RequestContext.getHttpRequest();
		java.util.List<CustomField>	visitCustomFieldsList = getCachedVisitCustomFields();
		// do not modify the original cached custom fields list copy the list into another and manipulate that.
		java.util.List<CustomField> tempFields = new java.util.ArrayList<CustomField>(visitCustomFieldsList);
		java.util.ListIterator listIterator = tempFields.listIterator();
		while (listIterator.hasNext()) {
			// remove the custom fields from list which are not required to display
			//(i.e., whose label is empty or showInPatientdemography set to "N" ).
			CustomField field =	(CustomField) listIterator.next();
			String showInClinical = (String) field.getShowInClinical();
			String showInOthers = (String) field.getShowInOther();
			showInClinical = showInClinical == null ? "" : showInClinical.trim();
			showInOthers = showInOthers == null ? "" : showInOthers.trim();
			showClinicalInfo = showClinicalInfo == null ? "false" : showClinicalInfo;

			String label = (String) field.label;
			label = (label == null) ? "" : label;
			// showInClinical and showInOthers are of type character(1) in database, that is the reason it occupies one character space
			// irrespective of the value exist or not.
			if (label.equals("") || (showInClinical.equals("") && showInOthers.equals(""))) {
				listIterator.remove();
			} else if (showClinicalInfo.equals("true") && showInClinical.equals("")) {
				listIterator.remove();
			} else if (showClinicalInfo.equals("false") && showInOthers.equals("")) {
				listIterator.remove();
			}
		}
		request.setAttribute("visitCustomFields", tempFields);
	}
	public void clearCache() {
		String schema = RequestContext.getSchema();
		if (schema != null) {
			cachedCustomFields.remove(schema);
			cachedVisitCustomFields.remove(schema);
			cachedPreferences.remove(schema);
			GenericPreferencesCache.REGCACHEDPREFERENCESBEAN.remove(schema);
		}
	}
	@Override
	public int update(Connection con, Map columndata, Map keys) throws SQLException, IOException {
		clearCache();
		return super.update(con, columndata, keys);
	}

	private static final String GET_CUSTOM_REG_CARD_TEMPLATE =
		"SELECT custom_reg_card_template FROM registration_cards WHERE visit_type=? AND rate_plan=? AND status='A'";
	private static final String GENERAL_RATE_PLAN="";
	private static final String GENERAL_VISIT_TYPE="A";

	public static InputStream getCustomRegCardTemplate(String visitType, String orgId) throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			stmt = con.prepareStatement(GET_CUSTOM_REG_CARD_TEMPLATE);
			stmt.setString(1, visitType);
			stmt.setString(2, orgId);
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				stmt.setString(1, GENERAL_VISIT_TYPE);
			stmt.setString(2, orgId);
			rs.close();
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				stmt.setString(1, visitType);
			stmt.setString(2, GENERAL_RATE_PLAN);
			rs.close();
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				stmt.setString(1, GENERAL_VISIT_TYPE);
			stmt.setString(2, GENERAL_RATE_PLAN);
			rs.close();
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, stmt, rs);
		}

	}

  public static final String GET_PATIENT_IMAGE="SELECT patient_photo FROM patient_details WHERE mr_no=? AND patient_photo!='' ";

	public static InputStream getPatientImage(String mrNo) throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			stmt = con.prepareStatement(GET_PATIENT_IMAGE);
			stmt.setString(1,mrNo);
			rs = stmt.executeQuery();

			if (rs.next()){
				return rs.getBinaryStream(1);
			}else{
				return null;
			}

		} finally {
			DataBaseUtil.closeConnections(con, stmt, rs);
		}
	}

	public static boolean isTokenGenerationEnabled() throws SQLException {
		String generateToken = DataBaseUtil.getStringValueFromDb("SELECT op_generate_token FROM registration_preferences");

		return generateToken.equals("Y");
	}

	private static final String GET_PREFS =
		" SELECT custom_field1_label, custom_field2_label, custom_field3_label, " +
		" 	custom_field4_label, custom_field5_label, custom_field6_label, custom_field7_label," +
		"	custom_field8_label, custom_field9_label, custom_field10_label, custom_field11_label, " +
		"	custom_field12_label, custom_field13_label,custom_field14_label,custom_field15_label,custom_field16_label," +
		"   custom_field17_label,custom_field18_label,custom_field19_label, " +

		"	custom_field1_validate, custom_field2_validate, custom_field3_validate, custom_field4_validate, " +
		" 	custom_field5_validate, custom_field6_validate, custom_field7_validate, custom_field8_validate, " +
		"	custom_field9_validate, custom_field10_validate, custom_field11_validate, custom_field12_validate, " +
		"	custom_field13_validate,custom_field14_validate,custom_field15_validate,custom_field16_validate," +
		"   custom_field17_validate,custom_field18_validate,custom_field19_validate," +

		"	custom_field1_show, custom_field2_show, custom_field3_show, custom_field4_show, custom_field5_show, " +
		"	custom_field6_show, custom_field7_show, custom_field8_show, custom_field9_show, custom_field10_show, " +
		"	custom_field11_show, custom_field12_show, custom_field13_show,custom_field14_show,custom_field15_show,custom_field16_show," +
		"   custom_field17_show,custom_field18_show,custom_field19_show, " +

		"   visit_custom_field1_name, visit_custom_field2_name, visit_custom_field3_name," +
		"   visit_custom_field4_name,visit_custom_field5_name,visit_custom_field6_name," +
		"   visit_custom_field7_name,visit_custom_field8_name,visit_custom_field9_name, " +

		"   visit_custom_field1_show, visit_custom_field2_show, visit_custom_field3_show," +
		"   visit_custom_field4_show,visit_custom_field5_show,visit_custom_field6_show," +
		"   visit_custom_field7_show,visit_custom_field8_show,visit_custom_field9_show, " +

		" 	visit_custom_field1_validate, visit_custom_field2_validate, visit_custom_field3_validate," +
		"   visit_custom_field4_validate,visit_custom_field5_validate,visit_custom_field6_validate," +
		"   visit_custom_field7_validate,visit_custom_field8_validate,visit_custom_field9_validate," +

		"	patient_category_field_label, category_expiry_field_label, area_field_validate, nextofkin_field_validate, " +
		"	address_field_validate, complaint_field_validate, old_reg_field_label, " +
		"	case_file_settings,  hosp_uses_units, dept_units_settings, referredby_field_validate, " +
		"	admitting_doctor_mandatory, reg_validity_period, op_default_selection, " +
		"	ip_default_selection,  " +

		"	custom_list1_name, custom_list2_name, custom_list3_name,custom_list4_name,custom_list5_name,custom_list6_name," +
		"	custom_list7_name,custom_list8_name,custom_list9_name, " +

		" 	custom_list1_validate, custom_list2_validate, custom_list3_validate,custom_list4_validate," +
		"	custom_list5_validate,custom_list6_validate,custom_list7_validate,custom_list8_validate,custom_list9_validate," +

		"	custom_list1_show, custom_list2_show, custom_list3_show,custom_list3_show,custom_list4_show,custom_list5_show,custom_list6_show," +
		"	custom_list7_show,custom_list8_show,custom_list9_show," +

		"	visit_custom_list1_name, visit_custom_list2_name,  " +

		"	visit_custom_list1_show, visit_custom_list2_show,  " +

		"	visit_custom_list1_validate, visit_custom_list2_validate,  " +

		"	government_identifier_label, government_identifier_type_label," +
		"   outside_default_selection, encntr_start_and_end_reqd, encntr_type_reqd, patientphone_field_validate,allow_age_entry," +
		"	passport_no, passport_validity, passport_issue_country, visa_validity," +
		"	passport_no_validate, passport_validity_validate, passport_issue_country_validate, visa_validity_validate, " +
		"	passport_no_show, passport_validity_show, passport_issue_country_show, visa_validity_show," +
		"	nationality, nationality_validate, nationality_show, " +
		"	family_id, family_id_validate, family_id_show, no_reg_charge_sources, " +
		"	doc_eandm_codification_required, visit_type_dependence, allow_multiple_active_visits," +
		"   prior_auth_required, member_id_label, member_id_valid_from_label, member_id_valid_to_label, " +
		"	copy_paste_option, default_followup_eandm_code, default_op_encounter_start_type," +
		"	default_op_encounter_end_type, default_ip_encounter_start_type, default_ip_encounter_end_type,name_parts," +
		"	name_local_lang_required, referal_for_life, validate_email_id , patient_reg_basis, allow_drg_perdiem, "+
		" enable_district, show_referrral_doctor_filter, allow_auto_entry_of_area, patient_photo_mandatory, patient_outstanding_control, plan_code_search, " +
		" last_name_required, marital_status_required, religion_required, diagnosis_for_osp_registration" +
		" FROM registration_preferences ";

	public static RegistrationPreferencesDTO getRegistrationPreferences() throws SQLException {
		String schema = RequestContext.getSchema();
		RegistrationPreferencesDTO prefs = null;
		if (schema != null) {
			prefs = cachedPreferences.get(schema);
			if (prefs != null)
				return prefs;
		}

		prefs = getRegistrationPreferencesFromDB();
		if (schema != null)
			cachedPreferences.put(schema, prefs);
		return prefs;
	}

	public static RegistrationPreferencesDTO getRegistrationPreferencesFromDB() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		RegistrationPreferencesDTO dto = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PREFS);
			rs = ps.executeQuery();
			if (rs.next()){
				dto = new RegistrationPreferencesDTO();
				dto.setCustom_field1_label(rs.getString("custom_field1_label"));
				dto.setCustom_field2_label(rs.getString("custom_field2_label"));
				dto.setCustom_field3_label(rs.getString("custom_field3_label"));
				dto.setCustom_field4_label(rs.getString("custom_field4_label"));
				dto.setCustom_field5_label(rs.getString("custom_field5_label"));
				dto.setCustom_field6_label(rs.getString("custom_field6_label"));
				dto.setCustom_field7_label(rs.getString("custom_field7_label"));
				dto.setCustom_field8_label(rs.getString("custom_field8_label"));
				dto.setCustom_field9_label(rs.getString("custom_field9_label"));
				dto.setCustom_field10_label(rs.getString("custom_field10_label"));
				dto.setCustom_field11_label(rs.getString("custom_field11_label"));
				dto.setCustom_field12_label(rs.getString("custom_field12_label"));
				dto.setCustom_field13_label(rs.getString("custom_field13_label"));
				dto.setCustom_field14_label(rs.getString("custom_field14_label"));
				dto.setCustom_field15_label(rs.getString("custom_field15_label"));
				dto.setCustom_field16_label(rs.getString("custom_field16_label"));
				dto.setCustom_field17_label(rs.getString("custom_field17_label"));
				dto.setCustom_field18_label(rs.getString("custom_field18_label"));
				dto.setCustom_field19_label(rs.getString("custom_field19_label"));

				dto.setCustom_field1_validate(rs.getString("custom_field1_validate"));
				dto.setCustom_field2_validate(rs.getString("custom_field2_validate"));
				dto.setCustom_field3_validate(rs.getString("custom_field3_validate"));
				dto.setCustom_field4_validate(rs.getString("custom_field4_validate"));
				dto.setCustom_field5_validate(rs.getString("custom_field5_validate"));
				dto.setCustom_field6_validate(rs.getString("custom_field6_validate"));
				dto.setCustom_field7_validate(rs.getString("custom_field7_validate"));
				dto.setCustom_field8_validate(rs.getString("custom_field8_validate"));
				dto.setCustom_field9_validate(rs.getString("custom_field9_validate"));
				dto.setCustom_field10_validate(rs.getString("custom_field10_validate"));
				dto.setCustom_field11_validate(rs.getString("custom_field11_validate"));
				dto.setCustom_field12_validate(rs.getString("custom_field12_validate"));
				dto.setCustom_field13_validate(rs.getString("custom_field13_validate"));
				dto.setCustom_field14_validate(rs.getString("custom_field14_validate"));
				dto.setCustom_field15_validate(rs.getString("custom_field15_validate"));
				dto.setCustom_field16_validate(rs.getString("custom_field16_validate"));
				dto.setCustom_field17_validate(rs.getString("custom_field17_validate"));
				dto.setCustom_field18_validate(rs.getString("custom_field18_validate"));
				dto.setCustom_field19_validate(rs.getString("custom_field19_validate"));

				dto.setCustom_field1_show(rs.getString("custom_field1_show"));
				dto.setCustom_field2_show(rs.getString("custom_field2_show"));
				dto.setCustom_field3_show(rs.getString("custom_field3_show"));
				dto.setCustom_field4_show(rs.getString("custom_field4_show"));
				dto.setCustom_field5_show(rs.getString("custom_field5_show"));
				dto.setCustom_field6_show(rs.getString("custom_field6_show"));
				dto.setCustom_field7_show(rs.getString("custom_field7_show"));
				dto.setCustom_field8_show(rs.getString("custom_field8_show"));
				dto.setCustom_field9_show(rs.getString("custom_field9_show"));
				dto.setCustom_field10_show(rs.getString("custom_field10_show"));
				dto.setCustom_field11_show(rs.getString("custom_field11_show"));
				dto.setCustom_field12_show(rs.getString("custom_field12_show"));
				dto.setCustom_field13_show(rs.getString("custom_field13_show"));
				dto.setCustom_field14_show(rs.getString("custom_field14_show"));
				dto.setCustom_field15_show(rs.getString("custom_field15_show"));
				dto.setCustom_field16_show(rs.getString("custom_field16_show"));
				dto.setCustom_field17_show(rs.getString("custom_field17_show"));
				dto.setCustom_field18_show(rs.getString("custom_field18_show"));
				dto.setCustom_field19_show(rs.getString("custom_field19_show"));

				dto.setVisit_custom_field1_name(rs.getString("visit_custom_field1_name"));
				dto.setVisit_custom_field2_name(rs.getString("visit_custom_field2_name"));
				dto.setVisit_custom_field3_name(rs.getString("visit_custom_field3_name"));
				dto.setVisit_custom_field4_name(rs.getString("visit_custom_field4_name"));
				dto.setVisit_custom_field5_name(rs.getString("visit_custom_field5_name"));
				dto.setVisit_custom_field6_name(rs.getString("visit_custom_field6_name"));
				dto.setVisit_custom_field7_name(rs.getString("visit_custom_field7_name"));
				dto.setVisit_custom_field8_name(rs.getString("visit_custom_field8_name"));
				dto.setVisit_custom_field9_name(rs.getString("visit_custom_field9_name"));

				dto.setVisit_custom_field1_show(rs.getString("visit_custom_field1_show"));
				dto.setVisit_custom_field2_show(rs.getString("visit_custom_field2_show"));
				dto.setVisit_custom_field3_show(rs.getString("visit_custom_field3_show"));
				dto.setVisit_custom_field4_show(rs.getString("visit_custom_field4_show"));
				dto.setVisit_custom_field5_show(rs.getString("visit_custom_field5_show"));
				dto.setVisit_custom_field6_show(rs.getString("visit_custom_field6_show"));
				dto.setVisit_custom_field7_show(rs.getString("visit_custom_field7_show"));
				dto.setVisit_custom_field8_show(rs.getString("visit_custom_field8_show"));
				dto.setVisit_custom_field9_show(rs.getString("visit_custom_field9_show"));

				dto.setVisit_custom_field1_validate(rs.getString("visit_custom_field1_validate"));
				dto.setVisit_custom_field2_validate(rs.getString("visit_custom_field2_validate"));
				dto.setVisit_custom_field3_validate(rs.getString("visit_custom_field3_validate"));
				dto.setVisit_custom_field4_validate(rs.getString("visit_custom_field4_validate"));
				dto.setVisit_custom_field5_validate(rs.getString("visit_custom_field5_validate"));
				dto.setVisit_custom_field6_validate(rs.getString("visit_custom_field6_validate"));
				dto.setVisit_custom_field7_validate(rs.getString("visit_custom_field7_validate"));
				dto.setVisit_custom_field8_validate(rs.getString("visit_custom_field8_validate"));
				dto.setVisit_custom_field9_validate(rs.getString("visit_custom_field9_validate"));

				dto.setPatientCategory(rs.getString("patient_category_field_label"));
				dto.setCategoryExpiryDate(rs.getString("category_expiry_field_label"));
				dto.setAreaValidate(rs.getString("area_field_validate"));
				dto.setNextOfKinValidate(rs.getString("nextofkin_field_validate"));
				dto.setAddressValidate(rs.getString("address_field_validate"));
				dto.setComplaintValidate(rs.getString("complaint_field_validate"));
				dto.setReferredbyValidate(rs.getString("referredby_field_validate"));
				dto.setConductingdoctormandatory(rs.getString("admitting_doctor_mandatory"));
				dto.setRegValidityPeriod(rs.getInt("reg_validity_period"));

				dto.setOldRegNumField(rs.getString("old_reg_field_label"));
				dto.setCaseFileSetting(rs.getString("case_file_settings"));
				dto.setHospUsesUnits(rs.getString("hosp_uses_units"));
				dto.setDeptUnitSetting(rs.getString("dept_units_settings"));
				dto.setOpDefaultSelection(rs.getString("op_default_selection"));
				dto.setIpDefaultSelection(rs.getString("ip_default_selection"));

				dto.setCustom_list1_name(rs.getString("custom_list1_name"));
				dto.setCustom_list2_name(rs.getString("custom_list2_name"));
				dto.setCustom_list3_name(rs.getString("custom_list3_name"));
				dto.setCustom_list4_name(rs.getString("custom_list4_name"));
				dto.setCustom_list5_name(rs.getString("custom_list5_name"));
				dto.setCustom_list6_name(rs.getString("custom_list6_name"));
				dto.setCustom_list7_name(rs.getString("custom_list7_name"));
				dto.setCustom_list8_name(rs.getString("custom_list8_name"));
				dto.setCustom_list9_name(rs.getString("custom_list9_name"));

				dto.setCustom_list1_validate(rs.getString("custom_list1_validate"));
				dto.setCustom_list2_validate(rs.getString("custom_list2_validate"));
				dto.setCustom_list3_validate(rs.getString("custom_list3_validate"));
				dto.setCustom_list4_validate(rs.getString("custom_list4_validate"));
				dto.setCustom_list5_validate(rs.getString("custom_list5_validate"));
				dto.setCustom_list6_validate(rs.getString("custom_list6_validate"));
				dto.setCustom_list7_validate(rs.getString("custom_list7_validate"));
				dto.setCustom_list8_validate(rs.getString("custom_list8_validate"));
				dto.setCustom_list9_validate(rs.getString("custom_list9_validate"));

				dto.setCustom_list1_show(rs.getString("custom_list1_show"));
				dto.setCustom_list2_show(rs.getString("custom_list2_show"));
				dto.setCustom_list3_show(rs.getString("custom_list3_show"));
				dto.setCustom_list4_show(rs.getString("custom_list4_show"));
				dto.setCustom_list5_show(rs.getString("custom_list5_show"));
				dto.setCustom_list6_show(rs.getString("custom_list6_show"));
				dto.setCustom_list7_show(rs.getString("custom_list7_show"));
				dto.setCustom_list8_show(rs.getString("custom_list8_show"));
				dto.setCustom_list9_show(rs.getString("custom_list9_show"));


				dto.setVisit_custom_list1_name(rs.getString("visit_custom_list1_name"));
				dto.setVisit_custom_list2_name(rs.getString("visit_custom_list2_name"));

				dto.setVisit_custom_list1_show(rs.getString("visit_custom_list1_show"));
				dto.setVisit_custom_list2_show(rs.getString("visit_custom_list2_show"));

				dto.setVisit_custom_list1_validate(rs.getString("visit_custom_list1_validate"));
				dto.setVisit_custom_list2_validate(rs.getString("visit_custom_list2_validate"));

				dto.setGovernment_identifier_type_label(rs.getString("government_identifier_type_label"));
				dto.setGovernment_identifier_label(rs.getString("government_identifier_label"));

				dto.setOutsideDefaultSelection(rs.getString("outside_default_selection"));
				dto.setEncntr_start_and_end_reqd(rs.getString("encntr_start_and_end_reqd"));
				dto.setEncntr_type_reqd(rs.getString("encntr_type_reqd"));

				dto.setPatientPhoneValidate(rs.getString("patientphone_field_validate"));

				dto.setPassport_no(rs.getString("passport_no"));
				dto.setPassport_no_validate(rs.getString("passport_no_validate"));
				dto.setPassport_validity(rs.getString("passport_validity"));
				dto.setPassport_validity_validate(rs.getString("passport_validity_validate"));
				dto.setPassport_issue_country(rs.getString("passport_issue_country"));
				dto.setPassport_issue_country_validate(rs.getString("passport_issue_country_validate"));
				dto.setVisa_validity(rs.getString("visa_validity"));
				dto.setVisa_validity_validate(rs.getString("visa_validity_validate"));
				dto.setNationality(rs.getString("nationality"));
				dto.setNationality_validate(rs.getString("nationality_validate"));
				dto.setNationality_show(rs.getString("nationality_show"));

				dto.setPassport_no_show(rs.getString("passport_no_show"));
				dto.setPassport_validity_show(rs.getString("passport_validity_show"));
				dto.setPassport_issue_country_show(rs.getString("passport_issue_country_show"));
				dto.setVisa_validity_show(rs.getString("visa_validity_show"));
				dto.setAllow_age_entry(rs.getString("allow_age_entry"));
				dto.setpatient_reg_basis(rs.getString("patient_reg_basis"));
				dto.setFamily_id(rs.getString("family_id"));
				dto.setFamily_id_validate(rs.getString("family_id_validate"));
				dto.setFamily_id_show(rs.getString("family_id_show"));
				dto.setDoc_eandm_codification_required(rs.getString("doc_eandm_codification_required"));
				dto.setVisit_type_dependence(rs.getString("visit_type_dependence"));
				dto.setAllow_multiple_active_visits(rs.getString("allow_multiple_active_visits"));

				dto.setPrior_auth_required(rs.getString("prior_auth_required"));
				dto.setMember_id_label(rs.getString("member_id_label"));
				dto.setMember_id_valid_from_label(rs.getString("member_id_valid_from_label"));
				dto.setMember_id_valid_to_label(rs.getString("member_id_valid_to_label"));
				dto.setCopy_paste_option(rs.getString("copy_paste_option"));
				dto.setDefault_followup_eandm_code(rs.getString("default_followup_eandm_code"));

				dto.setDefault_op_encounter_start_type(rs.getString("default_op_encounter_start_type"));
				dto.setDefault_op_encounter_end_type(rs.getString("default_op_encounter_end_type"));
				dto.setDefault_ip_encounter_start_type(rs.getString("default_ip_encounter_start_type"));
				dto.setDefault_ip_encounter_end_type(rs.getString("default_ip_encounter_end_type"));
				dto.setName_parts(rs.getString("name_parts"));
				dto.setName_local_lang_required(rs.getString("name_local_lang_required"));
				dto.setReferal_for_life(rs.getString("referal_for_life"));
				dto.setValidate_email_id(rs.getString("validate_email_id"));
				dto.setAllow_drg_perdiem(rs.getString("allow_drg_perdiem"));
				dto.setEnableDistrict(rs.getString("enable_district"));
				dto.setShowReferralDoctorFilter(rs.getString("show_referrral_doctor_filter"));
				dto.setAllowAutoEntryOfArea(rs.getString("allow_auto_entry_of_area"));
        dto.setPatientPhotoMandatory(rs.getString("patient_photo_mandatory"));
        dto.setPatient_outstanding_control(rs.getString("patient_outstanding_control"));
        dto.setNo_reg_charge_sources(rs.getString("no_reg_charge_sources"));
        dto.setLastNameRequired(rs.getString("last_name_required"));
        dto.setMaritalStatusRequired(rs.getString("marital_status_required"));
        dto.setReligionRequired(rs.getString("religion_required"));
        dto.setDiagnosis_for_osp_registration(rs.getString("diagnosis_for_osp_registration"));
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return dto;
	}

	public static final String getGenRegCharge="SELECT coalesce(GEN_REG_CHARGE,0) FROM REGISTRATION_PREFERENCES";
	public static String getGenRegCharge() throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        String genRegCharge = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(getGenRegCharge);
            genRegCharge = DataBaseUtil.getStringValueFromDb(ps);
        } finally{
        	DataBaseUtil.closeConnections(con, ps);
        }
        return genRegCharge ;
    }

	public static final String getemrcharge = "SELECT COALESCE(MRCHARGE,0) FROM REGISTRATION_PREFERENCES";
	public static String getEmrChrg(){
	    return DataBaseUtil.getStringValueFromDb(getemrcharge);
	}

	public static final String getregcharges = "SELECT ip_reg_charge, op_reg_charge, reg_validity_period, reg_renewal_charge, mlccharge FROM REGISTRATION_PREFERENCES";
	public static Map getRegistrationCharges() throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		Map bean = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(getregcharges);
			List charges = DataBaseUtil.queryToArrayList(ps);
			bean = (Map)charges.get(0);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	private static final String GET_REG_CHARGE_LIST = "SELECT org_id, bed_type, ip_reg_charge, op_reg_charge, gen_reg_charge, "
			+ "reg_renewal_charge, mlccharge,gen_reg_charge_discount,op_reg_charge_discount,reg_renewal_charge_discount,"
			+ "ip_reg_charge_discount,mlccharge_discount,mrcharge_discount FROM registration_charges ";

	public static List getRegCharges() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		con = DataBaseUtil.getConnection();
		ArrayList regChargeList = null;
		ps = con.prepareStatement(GET_REG_CHARGE_LIST);
		try {
			regChargeList = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return regChargeList;
	}

	public static List<CustomField> getCachedCustomFields() throws SQLException {
		String schema = RequestContext.getSchema();
		List customFieldsList = cachedCustomFields.get(schema);
		if (customFieldsList == null) {
			customFieldsList = getCustomFields();
			cachedCustomFields.put(schema, customFieldsList);
		}
		return customFieldsList;
	}

	public static final String CUSTOM_FIELDS =
		" SELECT custom_field1_label, custom_field2_label, custom_field3_label, custom_field4_label, " +
		"	custom_field5_label, custom_field6_label, custom_field7_label, custom_field8_label, " +
		"	custom_field9_label, custom_field10_label, custom_field11_label, custom_field12_label, " +
		"	custom_field13_label,custom_field14_label,custom_field15_label,custom_field16_label," +
		"	custom_field17_label,custom_field18_label,custom_field19_label, " +

		" 	custom_field1_validate, custom_field2_validate, custom_field3_validate, custom_field4_validate, " +
		"	custom_field5_validate, custom_field6_validate, custom_field7_validate, custom_field8_validate, " +
		"	custom_field9_validate, custom_field10_validate, custom_field11_validate, custom_field12_validate, " +
		"	custom_field13_validate,custom_field14_validate,custom_field15_validate,custom_field16_validate," +
		"	custom_field17_validate,custom_field18_validate,custom_field19_validate, " +

		" 	custom_field1_show, custom_field2_show, custom_field3_show, custom_field4_show, custom_field5_show, " +
		"	custom_field6_show, custom_field7_show, custom_field8_show, custom_field9_show, custom_field10_show, " +
		"	custom_field11_show, custom_field12_show, custom_field13_show,custom_field14_show,custom_field15_show,custom_field16_show, " +
		"	custom_field17_show,custom_field18_show,custom_field19_show, " +

		"	custom_list1_name, custom_list2_name, custom_list3_name, custom_list4_name, custom_list5_name, " +
		"	custom_list6_name, custom_list7_name, custom_list8_name, custom_list9_name, " +

		"	custom_list1_validate, custom_list2_validate, custom_list3_validate, custom_list4_validate, " +
		"	custom_list5_validate, custom_list6_validate, custom_list7_validate, custom_list8_validate, " +
		"	custom_list9_validate, " +

		"	custom_list1_show, custom_list2_show, custom_list3_show, custom_list4_show," +
		" 	custom_list5_show, custom_list6_show, custom_list7_show, custom_list8_show, custom_list9_show," +

		" 	family_id, family_id_validate, family_id_show, " +
		"	nationality, nationality_validate, nationality_show, " +
		"   member_id_label " +
 		" FROM registration_preferences ";

	public static List<CustomField> getCustomFields() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null, ps1 = null;
		List<CustomField> customFieldsList = new ArrayList<CustomField>();
		List patHeaderFields = PatientHeaderPrefDAO.getAllPatientHeaderPrefFields();
		Map fieldsMap = ConversionUtils.listBeanToMapBean(patHeaderFields, "field_name");
		try {

			ps = con.prepareStatement(CUSTOM_FIELDS);
			ResultSet rs = ps.executeQuery();
			CustomField field = null;
			if (rs.next()) {
				for (int i=1; i<10; i++) {
					field = new CustomField();
					field.setLabel(rs.getString("custom_list"+ i +"_name"));
					field.setRequired(rs.getString("custom_list"+ i +"_validate"));
					field.setDisplay(rs.getString("custom_list" + i +"_show"));
					String showType = (String) ((BasicDynaBean) fieldsMap.get("custom_list"+ i +"_value")).get("data_category");
					field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
					field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
					field.setTxColumnName("custom_list"+ i +"_value");
					customFieldsList.add(field);
				}

				for (int i=1; i<20; i++) {
					field = new CustomField();
					field.setLabel(rs.getString("custom_field"+ i +"_label"));
					field.setRequired(rs.getString("custom_field"+ i +"_validate"));
					field.setDisplay(rs.getString("custom_field"+ i +"_show"));
					String showType = (String) ((BasicDynaBean) fieldsMap.get("custom_field"+ i )).get("data_category");
					field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
					field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
					field.setTxColumnName("custom_field"+i);

					customFieldsList.add(field);
				}

				field = new CustomField();
				field.setLabel(rs.getString("family_id"));
				field.setRequired(rs.getString("family_id_validate"));
				field.setDisplay(rs.getString("family_id_show"));
				String showType = (String) ((BasicDynaBean) fieldsMap.get("family_id")).get("data_category");
				field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
				field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
				field.setTxColumnName("family_id");
				customFieldsList.add(field);
				
				field = new CustomField();
				field.setLabel(rs.getString("nationality"));
				field.setRequired(rs.getString("nationality_validate"));
				field.setDisplay(rs.getString("nationality_show"));
				String nationalityShowType = (String) ((BasicDynaBean) fieldsMap.get("nationality_name")).get("data_category");
				field.setShowInClinical(nationalityShowType.equals("C") || nationalityShowType.equals("Both") ? "C" : "");
				field.setShowInOther(nationalityShowType.equals("O") || nationalityShowType.equals("Both") ? "O" : "");
				field.setTxColumnName("nationality_name");
				customFieldsList.add(field);

				field = new CustomField();
				field.setLabel(rs.getString("member_id_label"));
				field.setRequired(null);
				field.setDisplay(null);
				field.setShowInClinical(null);
				field.setShowInOther(null);
				field.setTxColumnName("member_id_label");
				customFieldsList.add(field);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
			if (ps1 != null) ps1.close();
		}
		return customFieldsList;
	}

	public static List<CustomField> getCachedVisitCustomFields() throws SQLException {
		String schema = RequestContext.getSchema();
		List visitCustomFieldsList = cachedVisitCustomFields.get(schema);
		if (visitCustomFieldsList == null) {
			visitCustomFieldsList = getVisitCustomFields();
			cachedVisitCustomFields.put(schema, visitCustomFieldsList);
		}
		return visitCustomFieldsList;
	}

	public static final String VISIT_CUSTOM_FIELDS =
		" SELECT visit_custom_field1_name, visit_custom_field2_name, visit_custom_field3_name," +
		"	 visit_custom_field4_name,visit_custom_field5_name,visit_custom_field6_name," +
		"	 visit_custom_field7_name,visit_custom_field8_name,visit_custom_field9_name," +

		" 	visit_custom_field1_show, visit_custom_field2_show, visit_custom_field3_show, " +
		" 	visit_custom_field4_show, visit_custom_field5_show, visit_custom_field6_show, " +
		" 	visit_custom_field7_show, visit_custom_field8_show, visit_custom_field9_show, " +

		" 	visit_custom_field1_validate, visit_custom_field2_validate, visit_custom_field3_validate, " +
		" 	visit_custom_field4_validate, visit_custom_field5_validate, visit_custom_field6_validate, " +
		" 	visit_custom_field7_validate, visit_custom_field8_validate, visit_custom_field9_validate, " +


		"	visit_custom_list1_name, visit_custom_list2_name,  " +

		"	visit_custom_list1_show, visit_custom_list2_show,  " +

		"	visit_custom_list1_validate, visit_custom_list2_validate  " +


		" FROM registration_preferences ";

	public static List<CustomField> getVisitCustomFields() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null, ps1 = null;
		List<CustomField> visitCustomFieldsList = new ArrayList<CustomField>();
		List patHeaderFields = PatientHeaderPrefDAO.getAllPatientHeaderPrefFields();
		Map fieldsMap = ConversionUtils.listBeanToMapBean(patHeaderFields, "field_name");
		try {

			ps = con.prepareStatement(VISIT_CUSTOM_FIELDS);
			ResultSet rs = ps.executeQuery();
			CustomField field = null;
			if (rs.next()) {

				for (int i=1; i<10; i++) {
					field = new CustomField();
					field.setLabel(rs.getString("visit_custom_field"+ i +"_name"));
					field.setShow(rs.getString("visit_custom_field"+ i + "_show"));
					field.setRequired(rs.getString("visit_custom_field"+ i +"_validate"));
					String showType = (String) ((BasicDynaBean) fieldsMap.get("visit_custom_field"+ i )).get("data_category");
					field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
					field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
					field.setTxColumnName("visit_custom_field"+i);

					visitCustomFieldsList.add(field);
				}

				for (int i=1; i<3; i++) {
					field = new CustomField();
					field.setLabel(rs.getString("visit_custom_list"+ i +"_name"));
					field.setShow(rs.getString("visit_custom_list"+ i + "_show"));
					field.setRequired(rs.getString("visit_custom_list"+ i +"_validate"));
					String showType = (String) ((BasicDynaBean) fieldsMap.get("visit_custom_list"+ i )).get("data_category");
					field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
					field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
					field.setTxColumnName("visit_custom_list"+ i);
					visitCustomFieldsList.add(field);
				}
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
			if (ps1 != null) ps1.close();
		}
		return visitCustomFieldsList;
	}

	public static int getMadatoryAddlnFieldsCount() throws SQLException {
		List<CustomField> customFields =  getCustomFields();
		int count = 0;

		for (CustomField field : customFields) {
			if (field.getRequired() != null && (field.getRequired().equals("A") || field.getRequired().equals("O"))
					&& field.getDisplay() != null && field.getDisplay().equals("M")) {
				count++;
			}
		}

		RegistrationPreferencesDTO regPrefs = getRegistrationPreferences();
		if (regPrefs.getAddressValidate() != null && (regPrefs.getAddressValidate().equals("A") || regPrefs.getAddressValidate().equals("O"))) {
			count++;
		}
		if (regPrefs.getAreaValidate() != null && (regPrefs.getAreaValidate().equals("A") || regPrefs.getAreaValidate().equals("O"))) {
			count++;
		}
		return count;
	}

	public static int[] getMadatoryVisitAddlnFieldsCount() throws SQLException {
		List<CustomField> visitCustomFields =  getVisitCustomFields();
		int countRequired = 0;
		int countLabel = 0;

		for (CustomField field : visitCustomFields) {
			if (field.getRequired() != null && (field.getRequired().equals("A") || field.getRequired().equals("O"))) {
				countRequired++;
			}
			if (field.getLabel() != null && !field.getLabel().trim().equals("")) {
				countLabel++;
			}
		}
		int[] countArr = {countRequired,countLabel};
		return countArr;
	}

	public static int getVisitAddlnFieldsCount() throws SQLException {
		List<CustomField> visitCustomFields =  getVisitCustomFields();
		int count = 0;

		for (CustomField field : visitCustomFields) {
			if (field.getLabel() != null && !field.getLabel().trim().equals("")) {
				count++;
			}
		}
		return count;
	}

	public static final String CUSTOM_FIELDS_FOR_VALIDATION =
			" SELECT custom_field1_label, custom_field2_label, custom_field3_label, custom_field4_label, " +
			"	custom_field5_label, custom_field6_label, custom_field7_label, custom_field8_label, " +
			"	custom_field9_label, custom_field10_label, custom_field11_label, custom_field12_label, " +
			"	custom_field13_label,custom_field14_label,custom_field15_label,custom_field16_label," +
			"	custom_field17_label,custom_field18_label,custom_field19_label, " +

			" 	custom_field1_validate, custom_field2_validate, custom_field3_validate, custom_field4_validate, " +
			"	custom_field5_validate, custom_field6_validate, custom_field7_validate, custom_field8_validate, " +
			"	custom_field9_validate, custom_field10_validate, custom_field11_validate, custom_field12_validate, " +
			"	custom_field13_validate,custom_field14_validate,custom_field15_validate,custom_field16_validate," +
			"	custom_field17_validate,custom_field18_validate,custom_field19_validate, " +

			" 	custom_field1_show, custom_field2_show, custom_field3_show, custom_field4_show, custom_field5_show, " +
			"	custom_field6_show, custom_field7_show, custom_field8_show, custom_field9_show, custom_field10_show, " +
			"	custom_field11_show, custom_field12_show, custom_field13_show,custom_field14_show,custom_field15_show,custom_field16_show, " +
			"	custom_field17_show,custom_field18_show,custom_field19_show, " +

			"	custom_list1_name, custom_list2_name, custom_list3_name, custom_list4_name, custom_list5_name, " +
			"	custom_list6_name, custom_list7_name, custom_list8_name, custom_list9_name, " +

			"	custom_list1_validate, custom_list2_validate, custom_list3_validate, custom_list4_validate, " +
			"	custom_list5_validate, custom_list6_validate, custom_list7_validate, custom_list8_validate, " +
			"	custom_list9_validate, " +

			"	custom_list1_show, custom_list2_show, custom_list3_show, custom_list4_show," +
			" 	custom_list5_show, custom_list6_show, custom_list7_show, custom_list8_show, custom_list9_show," +

			" 	family_id, family_id_validate, family_id_show, " +
			"   member_id_label, " +
			"   passport_no, passport_no_validate, passport_no_show, " +
			"   passport_validity, passport_validity_validate, passport_validity_show, " +
			"   passport_issue_country,passport_issue_country_validate, passport_issue_country_show, " +
			"   visa_validity, visa_validity_validate, visa_validity_show " +
	 		" FROM registration_preferences ";

	public static List<CustomField> getCustomFieldsForValidation() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null, ps1 = null;
		List<CustomField> customFieldsList = new ArrayList<CustomField>();
		List patHeaderFields = PatientHeaderPrefDAO.getAllPatientHeaderPrefFields();
		Map fieldsMap = ConversionUtils.listBeanToMapBean(patHeaderFields, "field_name");
		try {

			ps = con.prepareStatement(CUSTOM_FIELDS_FOR_VALIDATION);
			ResultSet rs = ps.executeQuery();
			CustomField field = null;
			if (rs.next()) {
				for (int i=1; i<10; i++) {
					field = new CustomField();
					field.setLabel(rs.getString("custom_list"+ i +"_name"));
					field.setRequired(rs.getString("custom_list"+ i +"_validate"));
					field.setDisplay(rs.getString("custom_list" + i +"_show"));
					String showType = (String) ((BasicDynaBean) fieldsMap.get("custom_list"+ i +"_value")).get("data_category");
					field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
					field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
					field.setTxColumnName("custom_list"+ i +"_value");
					customFieldsList.add(field);
				}

				for (int i=1; i<20; i++) {
					field = new CustomField();
					field.setLabel(rs.getString("custom_field"+ i +"_label"));
					field.setRequired(rs.getString("custom_field"+ i +"_validate"));
					field.setDisplay(rs.getString("custom_field"+ i +"_show"));
					String showType = (String) ((BasicDynaBean) fieldsMap.get("custom_field"+ i )).get("data_category");
					field.setShowInClinical(showType.equals("C") || showType.equals("Both") ? "C" : "");
					field.setShowInOther(showType.equals("O") || showType.equals("Both") ? "O" : "");
					field.setTxColumnName("custom_field"+i);

					customFieldsList.add(field);
				}

				// Passport Fields
				field = new CustomField();
				field.setTxColumnName("passport_no");
				field.setRequired(rs.getString("passport_no_validate"));
				customFieldsList.add(field);

				field = new CustomField();
				field.setTxColumnName("passport_validity");
				field.setRequired(rs.getString("passport_validity_validate"));
				customFieldsList.add(field);

				field = new CustomField();
				field.setTxColumnName("passport_issue_country");
				field.setRequired(rs.getString("passport_issue_country_validate"));
				customFieldsList.add(field);

				field = new CustomField();
				field.setTxColumnName("visa_validity");
				field.setRequired(rs.getString("visa_validity_validate"));
				customFieldsList.add(field);

			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
			if (ps1 != null) ps1.close();
		}
		return customFieldsList;
	}
	
	public static List<Map<String,String>> getPreferredLanguages(String userLangCode) 
	    throws SQLException{
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

}
