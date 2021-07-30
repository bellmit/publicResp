package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatientVisitDescProvider implements AuditLogDescProvider {

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

		Map<String,String> statusMap = new LinkedHashMap<String,String>();
		Map<String,String> visitTypeMap = new LinkedHashMap<String,String>();
		Map<String,String> opTypeMap = new LinkedHashMap<String,String>();
		Map<String,String> dischargeTypeMap = new LinkedHashMap<String,String>();

		statusMap.put("A", "Active");
		statusMap.put("C", "Closed / Discharged");

		visitTypeMap.put("i", "IP");
		visitTypeMap.put("o", "OP");
		visitTypeMap.put("d", "Test");
		visitTypeMap.put("r", "Retail");
		visitTypeMap.put("t", "Incoming Test");

		opTypeMap.put("M", "Main");
		opTypeMap.put("F", "FollowUp");
		opTypeMap.put("D", "FollowUp (No Cons.)");
		opTypeMap.put("R", "Revisit");
		opTypeMap.put("O", "OutSide");

		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("mr_no", "MR No", false);
		desc.addField("patient_id", "Patient ID", false);
		desc.addField("reg_date", "Registration Date", false);
		desc.addField("reg_time", "Registration Time", false);
		desc.addField("status", "Status", true); // lookup
		desc.addField("visit_type", "Visit Type", true);
		desc.addField("op_type", "Op Type / Visit", true);
		desc.addField("revisit", "Revisit", false);
		desc.addField("primary_sponsor_id", "Primary TPA/Sponsor", true); // lookup
		desc.addField("secondary_sponsor_id", "Secondary TPA/Sponsor", true); // lookup
		desc.addField("ready_to_discharge", "Ready to Discharge", false);
		desc.addField("insurance_amt", "Insurance Amount", true);
		desc.addField("diagnosis", "Diagnosis", false);
		desc.addField("icd_code", "ICD Code", false); // lookup ?
		desc.addField("treatment", "Treatment", false);
		desc.addField("mrd_remarks", "MRD Remarks", false);
		desc.addField("patient_care_oftext", "Care Of", false);
		desc.addField("patient_careof_address", "Care Of Address", false);
		desc.addField("relation", "Relation", false);
		desc.addField("original_visit_id", "Original Visit", false);
		desc.addField("reg_charge_accepted", "Registration Charge Accepted", false);
		desc.addField("complaint", "Complaint", false); // lookup
		desc.addField("reference_docto_id", "Referred By", false); // lookup
		desc.addField("bed_type", "Bed Type", true);
		desc.addField("ward_id", "Ward", true); // lookup
		desc.addField("mlc_status", "MLC Status", true);
		desc.addField("dept_name", "Department", true); // lookup
		desc.addField("org_id", "Rate Plan", true); // lookup
		desc.addField("discharge_date", "Discharge Date", false);
		desc.addField("discharge_time", "Discharge Time", false);
		desc.addField("discharge_flag", "Discharged", true); // lookup
		desc.addField("discharge_doctor_id", "Discharged By");
		desc.addField("discharge_type", "Discharge Type", true);
		desc.addField("doctor", "Doctor"); // lookup
		desc.addField("discharge_finalized_user", "Discharge Finalized By", true);
		desc.addField("discharge_finalized_date", "Discharge Finalized Date", false);
		desc.addField("discharge_finalized_time", "Discharge Finalized Time", false);
		desc.addField("unit_id", "Unit"); // lookup
		desc.addField("referred_to", "Referred To", false);
		desc.addField("patient_category_id", "Patient Category", true);
		desc.addField("primary_insurance_co", "Primary Insurance Company", true);
		desc.addField("secondary_insurance_co", "Secondary Insurance Company", true);
		desc.addField("encounter_type", "Encounter Type", true); // lookup
		desc.addField("encounter_start_type", "Encounter Start Type", true); // lookup
		desc.addField("encounter_end_type", "Encounter End Type", true); // lookup


		// Visit details custom fields
		for (int i=1; i<10; i++) {
			String customFieldLabel = (String)resultMap.get("VISIT_CUSTOM_FIELD"+i+"_NAME");
			if (!customFieldLabel.trim().equals(""))
				desc.addField("visit_custom_field"+i, (String)resultMap.get("VISIT_CUSTOM_FIELD"+i+"_NAME"));
		}

		// Visit details custom list fields
		for (int i=1; i<3; i++) {
			String customFieldLabel = (String)resultMap.get("VISIT_CUSTOM_LIST"+i+"_NAME");
			if (!customFieldLabel.trim().equals("")) {
				desc.addField("visit_custom_list"+i, (String)resultMap.get("VISIT_CUSTOM_LIST"+i+"_NAME"));
				desc.addFieldValue("visit_custom_list"+i, "custom_visit_list"+i+"_master", "custom_value", "custom_value");
			}
		}

		desc.addFieldValue("visit_type", visitTypeMap);
		desc.addFieldValue("status", statusMap);
		desc.addFieldValue("op_type", opTypeMap);

		desc.addFieldValue("tpa_id", "tpa_master", "tpa_id", "tpa_name");
		desc.addFieldValue("relation", "relation_master", "relation_id", "relation__name");
		desc.addFieldValue("reference_docto_id", "all_referrers_view", "id", "referrer");
		desc.addFieldValue("ward_id", "ward_names", "ward_no", "ward_name");
		desc.addFieldValue("dept_name", "department", "dept_id", "dept_name");
		desc.addFieldValue("org_id", "organization_details", "org_id", "org_name");
		desc.addFieldValue("discharge_doctor_id", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("doctor", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("patient_category_id", "patient_category_master", "category_id", "category_name");
		desc.addFieldValue("unit_id", "dept_unit_master", "unit_id", "unit_name");
		desc.addFieldValue("insurance_company", "insurance_company_master", "insurance_co_id", "insurance_co_name");
		desc.addFieldValue("encounter_type", "encounter_type_codes", "encounter_type_id", "encounter_type_desc");
		desc.addFieldValue("encounter_start_type", "encounter_type_codes", "encounter_type_id", "encounter_type_desc");
		desc.addFieldValue("encounter_end_type", "encounter_type_codes", "encounter_type_id", "encounter_type_desc");

		return desc;
	}

}
