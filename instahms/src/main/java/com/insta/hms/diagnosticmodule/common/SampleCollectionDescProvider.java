package com.insta.hms.diagnosticmodule.common;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

public class SampleCollectionDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		desc.addField("mr_no", "MR No", false);
		desc.addField("patient_id", "Patient Visit", false);
		desc.addField("sample_sno", "Sample Sno", false);
		desc.addField("sample_subsno", "Sample Sub No", false);
		desc.addField("sample_status", "Sample Status", false);
		desc.addField("sample_date", "Sample Date");
		desc.addField("cancel_date", "Cancel Date");
		desc.addField("user_name", "User Name");
		desc.addField("sample_type_id", "Sample Type");
		desc.addField("cancel_date", "Cancelled Date");
		desc.addField("assertion_time", "Assertion Time");
		desc.addField("handover_by", "Handover By");
		desc.addField("received_by", "Received By");
		desc.addField("sample_source_id", "Sample Source");
		desc.addField("specimen_condition", "Specimen Condition");
		desc.addField("sample_collection_id", "Sample Collection Id");
		desc.addField("sample_qty", "Sample Qty");
		desc.addField("sample_no_counter", "Sample No Counter");
		desc.addField("transfer_batch_id", "Transfer Batch Id");
		desc.addField("oh_id", "Oh ID");

		desc.addFieldValue("sample_type_id", "sample_type", "sample_type_id", "sample_type");
		desc.addFieldValue("sample_source_id", "sample_sources", "source_id","source_name");

		return desc;
	}
}
