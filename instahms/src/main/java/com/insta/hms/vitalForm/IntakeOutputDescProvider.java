package com.insta.hms.vitalForm;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * @author nikunj.s
 *
 */
public class IntakeOutputDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map<String,String> containerTypeMap = new LinkedHashMap<String,String>();
		containerTypeMap.put("V", "Vital");
		containerTypeMap.put("I", "Intake");
		containerTypeMap.put("O", "Output");

		desc.addField("patient_id", "Patient Id.", true);
		desc.addField("user_name", "Entered By", true);
		desc.addField("param_id", "Parameter Label", true);
		desc.addField("param_uom", "Unit Of Mesurement",false);
		desc.addField("mod_time", "Modification Time", true);
		desc.addField("date_time", "Intake/Output Date", true);
		desc.addField("param_value","Parameter Value", true);
		desc.addField("param_container", "Parameter Container", false);
		desc.addFieldValue("param_container", containerTypeMap);

		desc.addFieldValue("param_id", "vital_parameter_master", "param_id", "param_label");



		return desc;
	}

}
