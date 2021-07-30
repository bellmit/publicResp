package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDetailsDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {

		Map resultMap = new HashMap(); // initialize to avoid uninitialized access
		try {
			List result = DataBaseUtil.simpleQueryToArrayList("SELECT * from registration_preferences");
			if (null != result && result.size() > 0) {
				resultMap = (Map)result.get(0);
			}
		} catch (SQLException ex) {
			//logger.error("Exception occured while trying to fetch registration preferences, ignoring");
		}

		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("mr_no", "MR No", false);
		desc.addField("patient_name", "Name");
		desc.addField("patient_gender", "Gender");
		desc.addField("patient_care_oftext", "Care of", false);
		desc.addField("patient_careof_address", "Care Of Address", false);
		desc.addField("patient_address", "Address", false);
		desc.addField("patient_city", "City", false);
		desc.addField("patient_state", "State", false);
		desc.addField("patient_phone", "Phone", false);
		desc.addField("salutation", "Salutation", false);
		desc.addField("last_name", "Last Name", false);
		desc.addField("dateofbirth", "Date of Birth");
		desc.addField("country", "Country");
		desc.addField("relation", "Relation", false);
		desc.addField("oldmrno", "Old MR No");
		desc.addField("emr_access", "EMR Access");
		desc.addField("remarks", "Remarks", false);

		// Patient details custom fields
		for (int i=1; i<20; i++) {
			String customFieldLabel = (String)resultMap.get("CUSTOM_FIELD"+i+"_LABEL");
			if (!customFieldLabel.trim().equals(""))
				desc.addField("custom_field"+i, customFieldLabel);
		}

		// Patient details custom list fields
		for (int i=1; i<10; i++) {
			String customFieldLabel = (String)resultMap.get("CUSTOM_LIST"+i+"_NAME");
			if (!customFieldLabel.trim().equals("")) {
				desc.addField("custom_list"+i+"_value", customFieldLabel);
				desc.addFieldValue("custom_list"+i+"_value", "custom_list"+i+"_master", "custom_value", "custom_value");
			}
		}

		if (!((String)resultMap.get("FAMILY_ID")).trim().equals(""))
			desc.addField("family_id", (String)resultMap.get("FAMILY_ID"));
		if (!((String)resultMap.get("MEMBER_ID_LABEL")).trim().equals(""))
			desc.addField("member_id", (String)resultMap.get("MEMBER_ID_LABEL"));
		if (!((String)resultMap.get("MEMBER_ID_VALID_FROM_LABEL")).trim().equals(""))
			desc.addField("policy_validity_start", (String)resultMap.get("MEMBER_ID_VALID_FROM_LABEL"));
		if (!((String)resultMap.get("MEMBER_ID_VALID_TO_LABEL")).trim().equals(""))
			desc.addField("policy_validity_end", (String)resultMap.get("MEMBER_ID_VALID_TO_LABEL"));
		if (!((String)resultMap.get("GOVERNMENT_IDENTIFIER_LABEL")).trim().equals(""))
			desc.addField("government_identifier", (String)resultMap.get("GOVERNMENT_IDENTIFIER_LABEL"));
		if (!((String)resultMap.get("GOVERNMENT_IDENTIFIER_TYPE_LABEL")).trim().equals(""))
			desc.addField("identifier_id", (String)resultMap.get("GOVERNMENT_IDENTIFIER_TYPE_LABEL"));
		if (!((String)resultMap.get("PASSPORT_NO")).trim().equals(""))
			desc.addField("passport_no", (String)resultMap.get("PASSPORT_NO"));
		if (!((String)resultMap.get("PASSPORT_ISSUE_COUNTRY")).trim().equals("")) {
			desc.addField("passport_issue_country", (String)resultMap.get("PASSPORT_ISSUE_COUNTRY"));
			desc.addFieldValue("passport_issue_country", "country_master", "country_id", "country_name");
		}if (!((String)resultMap.get("PASSPORT_VALIDITY")).trim().equals(""))
			desc.addField("passport_validity", (String)resultMap.get("PASSPORT_VALIDITY"));
		if (!((String)resultMap.get("VISA_VALIDITY")).trim().equals(""))
			desc.addField("visa_validity", (String)resultMap.get("VISA_VALIDITY"));

		desc.addField("expected_dob", "DOB (Age)", false);
		desc.addField("timeofbirth", "Time of Birth", false);
		desc.addField("portal_access", "Portal Access", false);
		desc.addField("email_id", "E-mail ID");
		desc.addField("patient_area", "Area", false);
		desc.addField("patient_category_id", "Patient Category");
		desc.addField("category_expiry_date", "Category Expiry Date", false);
		desc.addField("visit_id", "Current Visit ID");
		desc.addField("previous_visit_id", "Last Visit ID");
		desc.addField("casefile_no", "Case File No");

		desc.addField("death_date", "Death Date");
		desc.addField("death_time", "Death Time", false);
		desc.addField("phone2", "Phone 2", false);

		desc.addFieldValue("death_reason_id", "death_reason_master", "reason_id", "reason");
		desc.addFieldValue("country", "country_master", "country_id", "country_name");
		desc.addFieldValue("patient_state", "state_master", "state_id", "state_name");
		desc.addFieldValue("patient_city", "city", "city_id", "city_name");
		desc.addFieldValue("salutation", "salutation_master", "salutation_id", "salutation");
		desc.addFieldValue("relation", "relation_master", "relation_id", "relation__name");
		desc.addFieldValue("patient_category_id", "patient_category_master", "category_id", "category_name");

		return desc;
	}

}
