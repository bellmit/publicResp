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
public class VisitVitalFormDescProvider implements AuditLogDescProvider {
	
	public VisitVitalFormDescProvider(){
		
	}

	@Override
	public AuditLogDesc getAuditLogDesc(String tableName) {
		// TODO Auto-generated method stub
		AuditLogDesc desc = new AuditLogDesc(tableName);
		
		Map<String,String> containerTypeMap = new LinkedHashMap<String,String>();
		containerTypeMap.put("V", "Vital");
		containerTypeMap.put("I", "Intake");
		containerTypeMap.put("O", "Output");
		
		Map<String,String> docViewMap = new LinkedHashMap<String,String>();
		docViewMap.put("t", "True");
		docViewMap.put("f", "False");
		
		desc.addField("patient_id", "Patient Id");
		desc.addField("mr_no", "MR No.");
		desc.addField("date_time", "Date and Time");
		desc.addField("user_name", "User Name");
		desc.addField("vital_reading_id","Vital Reading Id");
		desc.addField("doc_view","Document View");
		desc.addFieldValue("doc_view",docViewMap);
		
		desc.addField("param_id", "Parameter Label", true);
		desc.addField("param_uom", "Unit Of Mesurement",false);
		desc.addField("mod_time", "Modification Time", true);
		desc.addField("date_time", "Vital Date", true);
		desc.addField("param_value","Parameter Value", true);
		desc.addField("param_container", "Parameter Container", false);
		desc.addFieldValue("param_container", containerTypeMap);
		
		desc.addField("param_label", "Parameter Label", false);
		desc.addField("param_order", "Parameter Order", false);
		desc.addField("param_status", "Parameter Status", false);
		
		return desc;
	}

}
