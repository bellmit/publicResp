package com.insta.hms.core.patient;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PatientDetailsMapper extends
		OutputMapper<Map<String, Object>, Map<String, Object>> {

	// used linked hash map because the ordering of inserting reg_date in fieldNameMap matters 
	private static Map<String, String> fieldNameMap = new LinkedHashMap<String, String>();

	public PatientDetailsMapper() {
		fieldNameMap.put("addnl_phone", "patient_phone2");
		fieldNameMap.put("patrelation", "relation");
		fieldNameMap.put("pataddress", "patient_careof_address");
		fieldNameMap.put("patcontactperson", "patient_care_oftext");
		fieldNameMap.put("patient_category_name", "category_name");
		fieldNameMap.put("salutation_id", "salutation");
		fieldNameMap.put("reg_date", "first_visit_reg_date");
		fieldNameMap.put("current_visit_reg_date", "reg_date");
		fieldNameMap.put("death_reason", "death_reason_id");
		fieldNameMap.put("patient_mod_time", "mod_time");
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
