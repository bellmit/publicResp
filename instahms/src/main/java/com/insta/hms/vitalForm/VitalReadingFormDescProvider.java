/**
 * 
 */
package com.insta.hms.vitalForm;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class VitalReadingFormDescProvider implements AuditLogDescProvider {
	
	public VitalReadingFormDescProvider(){
		
	}

	@Override
	public AuditLogDesc getAuditLogDesc(String tableName) {
		// TODO Auto-generated method stub
		AuditLogDesc desc = new AuditLogDesc(tableName);
		
		

		Map<String,String> containerTypeMap = new LinkedHashMap<String,String>();
		containerTypeMap.put("V", "Vital");
		containerTypeMap.put("I", "Intake");
		containerTypeMap.put("O", "Output");
		
		desc.addField("vital_reading_id", "Vital Reading Id");
		desc.addField("patient_id", "Patient Id");
		desc.addField("mr_no", "MR No.");
		desc.addField("date_time", "Date and Time");
		desc.addField("user_name", "User Name");
		desc.addField("vital_reading_id","Vital Reading Id");
		
		desc.addField("param_id", "Parameter Name", true);
		desc.addField("param_uom", "Unit Of Mesurement",false);
		desc.addField("mod_time", "Modification Time", true);
		desc.addField("date_time", "Vital Date", true);
		desc.addField("param_value","Parameter Value", true);
		desc.addField("param_container", "Parameter Container", false);
		desc.addFieldValue("param_container", containerTypeMap);
		
		desc.addField("param_label", "Parameter Label", false);
		desc.addField("param_order", "Parameter Order", false);
		desc.addField("param_status", "Parameter Status", false);
		desc.addField("username", "User Name");
		desc.addField("mod_time", "Modification Time");
		
		desc.addFieldValue("param_id", "vital_parameter_master", "param_id", "param_label");
		
		return desc;
	}

}
