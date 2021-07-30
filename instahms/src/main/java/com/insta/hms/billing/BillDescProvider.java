package com.insta.hms.billing;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class BillDescProvider implements AuditLogDescProvider {

		public BillDescProvider() {
		}

		public AuditLogDesc getAuditLogDesc(String tableName) {

			AuditLogDesc desc = new AuditLogDesc(tableName);

			// Create all the static lookup maps
			Map statusMap = new LinkedHashMap(); // Bill status is a set static values
			statusMap.put("A", "Open");
			statusMap.put("F", "Finalized");
			statusMap.put("S", "Settled");
			statusMap.put("C", "Closed");
			statusMap.put("X", "Cancelled");

			Map visitTypeMap = new LinkedHashMap(); // Visit Type is a set static values
			visitTypeMap.put("i", "IP");
			visitTypeMap.put("o", "OP");
			visitTypeMap.put("d", "Test");
			visitTypeMap.put("r", "Retail");
			visitTypeMap.put("t", "Incoming Test");

			Map billTypeMap = new LinkedHashMap(); // Bill Type is a set static values
			billTypeMap.put("P", "Bill Now");
			billTypeMap.put("C", "Bill Later");
			billTypeMap.put("M", "Pharmacy Bill");
			billTypeMap.put("R", "Pharmacy Return");

			Map claimStatusMap = new LinkedHashMap(); // Claim status is a static map
			claimStatusMap.put("O", "Opened");
			claimStatusMap.put("S", "Sent");
			claimStatusMap.put("R", "Received");

			Map restrictionTypeMap = new LinkedHashMap(); // Claim status is a static map
			restrictionTypeMap.put("N", "Hospital");
			restrictionTypeMap.put("P", "Pharmacy");
			restrictionTypeMap.put("T", "Incoming Test");

			// Set the display names for all the audited fields. This follows from the fields that were
			// specified in the trigger

			desc.addField("bill_no", "Bill No", false);
			desc.addField("visit_id", "Visit ID", false);
			desc.addField("bill_type", "Bill Type");
			desc.addField("status", "Bill Status");
			desc.addField("finalized_date", "Finalized Date", false);
			desc.addField("last_finalized_at", "Last Finalized Date", false);
			desc.addField("closed_date", "Closed Date", false);
			desc.addField("discharge_status", "OK to Discharge");
			desc.addField("visit_type", "Visit Type");
			desc.addField("remarks", "Remarks");
			desc.addField("reopen_reason", "Reopen Reason");
			desc.addField("cancel_reason", "Cancel Reason");
			desc.addField("closed_by", "Closed By");
			desc.addField("primary_claim_status", "Primary Claim Status");
			desc.addField("secondary_claim_status", "Secondary Claim Status");
			desc.addField("claim_recd_amount", "Received Claim Amount");
			desc.addField("cancel_reason", "Cancel Reason", false);
			desc.addField("deposit_set_off", "Deposit Set Off");
			desc.addField("discount_auth", "Discount Authorizer");
			desc.addField("approval_amount", "Approval Amount", false);
			desc.addField("total_receipts", "Amount Received");
			desc.addField("last_receipt_no", "Last Receipt / Refund Issued");
			desc.addField("procedure_no", "Sponsor Procedure");
			desc.addField("sponsor_bill_no", "Sponsor Bill No");
			desc.addField("primary_total_sponsor_receipts", "Primary Total Sponsor Receipts");
			desc.addField("secondary_total_sponsor_receipts", "Secondary Total Sponsor Receipts");
			desc.addField("bill_label_id", "Bill Label Name");

			// Now set the lookup value mapping wherever necessary

			desc.addFieldValue("visit_type", visitTypeMap);
			desc.addFieldValue("status", statusMap);
			desc.addFieldValue("bill_type", billTypeMap);
			desc.addFieldValue("primary_claim_status", claimStatusMap);
			desc.addFieldValue("secondary_claim_status", claimStatusMap);
			desc.addFieldValue("restriction_type", restrictionTypeMap);

			// Add all lookup values from a db table
			desc.addFieldValue("discount_auth", "discount_authorizer", "disc_auth_id", "disc_auth_name");
			desc.addFieldValue("procedure_no", "sponsor_procedure_limit", "procedure_no", "procedure_name");
			desc.addFieldValue("bill_label_id", "bill_label_master", "bill_label_id", "bill_label_name");
			return desc;
		}
}
