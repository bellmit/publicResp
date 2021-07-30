package com.insta.hms.billing;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.MultiAuditLogDescProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BillAuditViewDescProvider extends MultiAuditLogDescProvider {

	private static final String[] BILL_AUDIT_VIEW_TABLES = new String[]
		{"bill_audit_log", "bill_charge_audit_log"};

	public BillAuditViewDescProvider() {
		super(BILL_AUDIT_VIEW_TABLES);
	}

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = super.getAuditLogDesc(tableName);
		// Create all the static lookup maps
		Map statusMap = new HashMap(); // Bill status is a set static values
		statusMap.put("A", "Open");
		statusMap.put("C", "Closed");
		statusMap.put("X", "Cancelled");
		statusMap.put("F", "Finalized");
		statusMap.put("S", "Settled");

		Map visitTypeMap = new HashMap(); // Visit Type is a set static values
		visitTypeMap.put("i", "IP");
		visitTypeMap.put("o", "OP");
		visitTypeMap.put("d", "Test");
		visitTypeMap.put("r", "Retail");
		visitTypeMap.put("t", "Incoming Test");

		Map billTypeMap = new HashMap(); // Bill Type is a set static values
		billTypeMap.put("P", "Bill Now");
		billTypeMap.put("C", "Bill Later");
		billTypeMap.put("M", "Pharmacy Bill");
		billTypeMap.put("R", "Pharmacy Return");

		Map claimStatusMap = new HashMap(); // Claim status is a static map
		claimStatusMap.put("O", "Opened");
		claimStatusMap.put("S", "Sent");
		claimStatusMap.put("R", "Received");

		Map paymentStatusMap = new LinkedHashMap(); // Payment status is a static map
		paymentStatusMap.put("U", "Unpaid");
		paymentStatusMap.put("P", "Paid");

		Map dischargeOkMap = new LinkedHashMap(); // Bill Discharge status is a static map
		dischargeOkMap.put("Y", "Yes");
		dischargeOkMap.put("N", "No");

		Map primaryBillMap = new LinkedHashMap(); // Bill primary status is a static map
		primaryBillMap.put("Y", "Yes");
		primaryBillMap.put("N", "No");

		Map isTpaBillMap = new LinkedHashMap(); // Bill tpa status is a static map
		isTpaBillMap.put("t", "True");
		isTpaBillMap.put("f", "False");

		// First add all the key fields
		desc.addField("bill_no", "Bill No", false);
		desc.addField("charge_id", "Charge ID", false);
		desc.addField("charge_head", "Charge Head", false);
		desc.addFieldValue("charge_head", "chargehead_constants", "chargehead_id", "chargehead_name");
		desc.addField("act_description", "Description", false);
		desc.addField("claim_id", "Claim Id", false);

		//====================================== BILL DESCRIPTION =====================================
		desc.addField("bill_discharge_status", "OK to Discharge", false);
		desc.addField("bill_claim_recd_amount", "Received Claim Amount", false);
		desc.addField("bill_discount_auth", "Discount Authorizer", false);
		desc.addField("bill_total_receipts", "Amount Received", false);
		desc.addField("bill_last_receipt_no", "Last Receipt / Refund Issued", false);
		desc.addField("bill_procedure_no", "Sponsor Procedure", false);
		desc.addField("bill_primary_total_sponsor_receipts", "Primary Total Sponsor Receipts", false);
		desc.addField("bill_secondary_total_sponsor_receipts", "Secondary Total Sponsor Receipts", false);
		desc.addField("bill_bill_label_id", "Bill Label Name");

		desc.addFieldValue("bill_visit_type", visitTypeMap);
		desc.addFieldValue("bill_status", statusMap);
		desc.addFieldValue("bill_bill_type", billTypeMap);
		desc.addFieldValue("bill_primary_claim_status", claimStatusMap);
		desc.addFieldValue("bill_secondary_claim_status", claimStatusMap);
		desc.addFieldValue("bill_payment_status", paymentStatusMap);
		desc.addFieldValue("bill_discharge_status", dischargeOkMap);
		desc.addFieldValue("bill_is_primary_bill", primaryBillMap);
		desc.addFieldValue("bill_is_tpa", isTpaBillMap);

		// Add all lookup values from a db table
		desc.addFieldValue("bill_discount_auth", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("bill_procedure_no", "sponsor_procedure_limit", "procedure_no", "procedure_name");
		desc.addFieldValue("bill_bill_label_id", "bill_label_master", "bill_label_id", "bill_label_name");

		//===============================BILL CHARGE DESCRIPTION===============================

		// Create all the static lookup maps
		Map chargeStatusMap = new HashMap(); // Bill status is a set static values
		chargeStatusMap.put("A", "Active");
		chargeStatusMap.put("X", "Cancelled");

		// Set the display names for all the audited fields. This follows from the fields that were
		// specified in the trigger

		desc.addField("bill_charge_act_department_id", "Department", false);
		desc.addField("bill_charge_act_description", "Description", false);
		desc.addField("bill_charge_act_remarks", "Details", false);
		desc.addField("bill_charge_act_rate", "Rate", false);
		desc.addField("bill_charge_act_unit", "Unit", false);
		desc.addField("bill_charge_act_quantity", "Quantity", false);
		desc.addField("bill_charge_status", "Charge Status", false);
		desc.addField("bill_charge_approval_id", "Approval", false);

		// charge total discount
		desc.addField("bill_charge_discount", "Discount", false);

		// overall discounts
		desc.addField("bill_charge_overall_discount_amt", "Overall Discount", false);
		desc.addField("bill_charge_overall_discount_auth", "Overall Discount Authorizer", false);

		// Doctor Payments
		desc.addField("bill_charge_payee_doctor_id", "Doctor", false);
		desc.addField("bill_charge_discount_auth_dr", "Doctor Discount Authorizer", false);
		desc.addField("bill_charge_dr_discount_amt", "Doctor Discount", false);

		// referal payments
		desc.addField("bill_charge_discount_auth_ref", "Referal Discount Authorizer", false);
		desc.addField("bill_charge_ref_discount_amt", "Referal Discount", false);

		// prescribing doctor payments
		desc.addField("bill_charge_prescribing_dr_id", "Prescribing Doctor", false);
		desc.addField("bill_charge_pres_dr_discount_amt", "Prescribing Doctor Discount", false);
		desc.addField("bill_charge_discount_auth_pres_dr", "Prescribing Doctor Discount Authorizer", false);

		// hospital discounts
		desc.addField("bill_charge_hosp_discount_amt", "Hospital Discount", false);
		desc.addField("bill_charge_discount_auth_hosp", "Hospital Discount Authorizer", false);

		desc.addField("bill_charge_activity_conducted", "Activity Conducted", false);

		// act_description_id, hasactivity
		desc.addFieldValue("bill_charge_status", chargeStatusMap);

		// Now set the lookup value mapping wherever necessary
		desc.addFieldValue("bill_charge_charge_head", "chargehead_constants", "chargehead_id", "chargehead_name");
		desc.addFieldValue("bill_charge_charge_group", "chargegroup_constants", "chargegroup_id", "chargegroup_name");

		desc.addFieldValue("bill_charge_payee_doctor_id", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("bill_charge_prescribing_dr_id", "doctors", "doctor_id", "doctor_name");

		desc.addFieldValue("bill_charge_discount_auth_dr", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("bill_charge_discount_auth_ref", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("bill_charge_discount_auth_pres_dr", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("bill_charge_discount_auth_hosp", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("bill_charge_overall_discount_auth", "discount_authorizer", "disc_auth_id", "disc_auth_name");

		return desc;
	}
}
