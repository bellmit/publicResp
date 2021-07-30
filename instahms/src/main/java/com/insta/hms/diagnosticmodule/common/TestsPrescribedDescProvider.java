package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestsPrescribedDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map presTypeMap = new LinkedHashMap();
		presTypeMap.put("i", "In-Coming");
		presTypeMap.put("o", "Out-Going");
		presTypeMap.put("h", "Hospital");

		Map priorityMap = new LinkedHashMap();
		priorityMap.put("R", "Routine");
		priorityMap.put("S", "Static");
		priorityMap.put("R", "Schedule");

		Map conductStatusMap = new LinkedHashMap();
		conductStatusMap.put("U", "Conduction Not Required");
		conductStatusMap.put("N", "Not Conducted");
		conductStatusMap.put("P", "Partially Conducted");
		conductStatusMap.put("C", "Report Completed");
		conductStatusMap.put("V", "Validated");
		conductStatusMap.put("MA", "Patient Arrived");
		conductStatusMap.put("CC", "Conduction Completed");
		conductStatusMap.put("TS", "Scheduled for Transcriptionist");
		conductStatusMap.put("CR", "Change Required");
		conductStatusMap.put("RP", "Revision InProgress");
		conductStatusMap.put("RC", "Revision Completed");
		conductStatusMap.put("RV", "Revision Validated");
		conductStatusMap.put("RAS", "Reconducted After Signoff");
		conductStatusMap.put("RBS", "Reconducted Before Signoff");
		conductStatusMap.put("NRN", "Not Conducted(No Results)");
		conductStatusMap.put("CRN", "Completed(No Results)");
		conductStatusMap.put("S", "Signedoff");
		conductStatusMap.put("X", "Cancelled");


		desc.addField("mr_no", "MR No", false);
		desc.addField("pat_id", "Patient Visit", false);
		desc.addField("test_id", "Test", false);
		desc.addField("pres_date", "Prescribed Date");
		desc.addField("pres_time", "Prescribed Time");
		desc.addField("pres_doctor", "Prescribed Doctor");
		desc.addField("conducted", "Conducted");
		desc.addField("cancelled_by", "Cancelled By");
		desc.addField("cancel_date", "Cancelled Date");
		desc.addField("priority", "Priority", false);
		desc.addField("package_name", "Package Name", false);
		desc.addField("remarks", "Remarks", false);
		desc.addField("report_id", "Report");
		desc.addField("prescription_type", "Prescription Type", false);
		desc.addField("labno", "Lab No.", false);
		desc.addField("re_conduction", "Re-conduction");
		desc.addField("is_outhouse_selected", "Is Outhouse Test");

		desc.addFieldValue("prescription_type", presTypeMap);
		desc.addFieldValue("priority", priorityMap);
		desc.addFieldValue("conducted", conductStatusMap);

		desc.addFieldValue("test_id", "diagnostics", "test_id", "test_name");
		desc.addFieldValue("pres_doctor", "doctors", "doctor_id", "doctor_name");

		// TODO: lookup values should be setup for sflag, package_name
		// TODO: reference_pres is a self-referencing field
		return desc;
	}
}
