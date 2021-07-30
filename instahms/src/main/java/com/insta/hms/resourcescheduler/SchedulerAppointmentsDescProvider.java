package com.insta.hms.resourcescheduler;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class SchedulerAppointmentsDescProvider implements AuditLogDescProvider{

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map<String,String> RescheduleMap = new LinkedHashMap<String,String>();
		RescheduleMap.put("Y", "Yes");
		RescheduleMap.put("N", "No");

		desc.addField("mr_no", "Mr No");
		desc.addField("patient_name", "Patient Name");
		desc.addField("appointment_time", "Appointment Time");
		desc.addField("duration", "Appointment Duration");
		desc.addField("booked_by", "Booked By");
		desc.addField("booked_time", "Booked Time");
		desc.addField("cancel_reason", "Appointment Cancel Reason");
		desc.addField("appointment_status", "Appointment Status");
		desc.addField("patient_contact", "Mobile No.");
		desc.addField("complaint", "Complaint");
		desc.addField("arrival_time", "Arrival Time");
		desc.addField("booked_time", "Booked Time");
		desc.addField("rescheduled", "Is Rescheduled");
		desc.addField("orig_appt_time", "Original Appointment Time");
		desc.addField("completed_time", "Appointment Completed Time");
		desc.addField("consultation_type_id", "consultationType");
		desc.addField("changed_by", "Changed By");
		desc.addField("changed_time", "Changed Time");

		desc.addFieldValue("consultation_type_id", "consultation_types", "consultation_type_id", "consultation_type");
		desc.addFieldValue("rescheduled", RescheduleMap);

		return desc;
	}

}
