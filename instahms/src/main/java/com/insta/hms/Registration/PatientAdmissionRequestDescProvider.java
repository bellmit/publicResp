package com.insta.hms.Registration;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class PatientAdmissionRequestDescProvider implements AuditLogDescProvider {

	@Override
	public AuditLogDesc getAuditLogDesc(String tableName) {
		
		AuditLogDesc auditLogDesc = new AuditLogDesc(tableName);
		
		auditLogDesc.addField("mr_no", "Mr No");
		auditLogDesc.addField("adm_request_id", "Admission Request Id");
		auditLogDesc.addField("consultation_id", "Consultation Id");
		auditLogDesc.addField("request_date", "Request Date");
		auditLogDesc.addField("chief_complaint", "Chief Complaint");
		auditLogDesc.addField("remarks", "Remarks");
		auditLogDesc.addField("admission_date", "Admission Date");
		auditLogDesc.addField("duration_of_stay", "Duration Of Stay");
		auditLogDesc.addField("requesting_doc", "Requesting Doctor");
		auditLogDesc.addField("center_id", "Center Id");
		auditLogDesc.addField("status", "Status");
		auditLogDesc.addField("cancelled_by", "Cancelled By");
		auditLogDesc.addField("cancellation_remarks", "Cancellation Remarks");
		auditLogDesc.addField("cancelled_on", "Cancelled On");
		
		return auditLogDesc;
	}

}
