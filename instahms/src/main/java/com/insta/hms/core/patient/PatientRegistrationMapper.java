package com.insta.hms.core.patient;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PatientRegistrationMapper extends OutputMapper<Map<String, Object>, Map<String, Object>> {

	private static Map<String, String>  fieldNameMap = new HashMap<String, String>();
	
	public PatientRegistrationMapper() {
		fieldNameMap.put("dis_finalized_date", "discharge_finalized_date");
		fieldNameMap.put("dept_id", "dept_name");
		fieldNameMap.put("dis_doc_id", "discharge_doc_id");
		fieldNameMap.put("bill_bed_type", "bed_type");
		fieldNameMap.put("patrelation", "relation");
		fieldNameMap.put("reg_ward_id", "ward_id");
		fieldNameMap.put("dis_finalized_user", "discharge_finalized_user");
		fieldNameMap.put("dis_finalized_time", "discharge_finalized_time");
		fieldNameMap.put("dis_format", "discharge_format");
		fieldNameMap.put("pataddress", "patient_careof_address");
		fieldNameMap.put("admitted_by", "user_name");
		fieldNameMap.put("visit_status", "status");
		fieldNameMap.put("insurance_category", "category_id");
		fieldNameMap.put("patcontactperson", "patient_care_oftext");
		
	}

	@Override
	public Map<String, Object> map(Map<String, Object> obj) {
		if (obj == null)
			return obj;
		for (Map.Entry<String, String> entry : fieldNameMap.entrySet()) {
			obj.put(entry.getValue(), obj.get(entry.getKey()));
			obj.remove(entry.getKey());
		}
		return obj;
	}

}
