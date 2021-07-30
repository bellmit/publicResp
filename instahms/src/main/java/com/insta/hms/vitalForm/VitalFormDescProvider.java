package com.insta.hms.vitalForm;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * @author nikunj.s
 *
 */
public class VitalFormDescProvider extends MultiAuditLogDescProvider {
	
	private static final String[] VITALS_AUDIT_VIEW_TABLES = new String[]
			{"visit_vitals_audit_log", "vital_reading_audit_log"};

	public VitalFormDescProvider() {
		super(VITALS_AUDIT_VIEW_TABLES);
	}
	
	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = super.getAuditLogDesc(tableName);

		Map<String,String> containerTypeMap = new LinkedHashMap<String,String>();
		containerTypeMap.put("V", "Vital");
		containerTypeMap.put("I", "Intake");
		containerTypeMap.put("O", "Output");
		
		Map<String,String> docViewMap = new LinkedHashMap<String,String>();
		docViewMap.put("t", "Yes");
		docViewMap.put("f", "No");
		
		desc.addField("vital_reading_id", "Vital Reading Id");
		desc.addField("patient_id", "Patient Id.", true);
		desc.addField("mr_no", "MR No.", true);
		desc.addField("user_name", "Entered By", true);
		desc.addField("param_id", "Parameter Id", true);
		desc.addField("param_label", "Parameter Label", true);
		desc.addField("param_uom", "Unit Of Mesurement",false);
		desc.addField("mod_time", "Modification Time", true);
		desc.addField("date_time", "Vital Date", true);
		desc.addField("param_value","Parameter Value", true);
		desc.addField("param_container", "Parameter Container", false);
		desc.addFieldValue("param_container", containerTypeMap);
		desc.addField("doc_view","Doctor View");
		desc.addFieldValue("doc_view", docViewMap);
		desc.addField("v_reading_id", "Vital Reading Id", false, true);
		
		desc.addField("param_label", "Parameter Label", false);
		desc.addField("param_order", "Parameter Order", false);
		desc.addField("param_status", "Parameter Status", false);

		desc.addFieldValue("param_id", "vital_parameter_master", "param_id", "param_label");

		return desc;
	}

}
