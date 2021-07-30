/**
 *
 */
package com.insta.hms.billing;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author anupama
 *
 */
public class BillChargeDescProvider implements AuditLogDescProvider {

	/* (non-Javadoc)
	 * @see com.insta.hms.auditlog.AuditLogDescProvider#getAuditLogDesc(java.lang.String)
	 */
	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		// Create all the static lookup maps
		Map statusMap = new HashMap(); // Bill status is a set static values
		statusMap.put("A", "Active");
		statusMap.put("X", "Cancelled");

		// Set the display names for all the audited fields. This follows from the fields that were
		// specified in the trigger

		desc.addField("bill_no", "Bill No", false);

		desc.addField("charge_group", "Charge Group", false);
		desc.addField("charge_head", "Charge Head", false);

		desc.addField("act_department_id", "Department", false);
		desc.addField("act_description", "Description", false);
		desc.addField("act_remarks", "Details", false);
		desc.addField("user_remarks", "Remarks", false);
		desc.addField("act_rate", "Rate", false);
		desc.addField("act_unit", "Unit", false);
		desc.addField("act_quantity", "Quantity", false);
		desc.addField("amount", "Amount");
		desc.addField("discount", "Discount");
		desc.addField("discount_reason", "Discount Reason", false);
		desc.addField("posted_date", "Posted Date", false);
		desc.addField("status", "Charge Status");
		desc.addField("approval_id", "Approval");

		desc.addField("insurance_claim_amount", "Insurance Claim Amount");

		// Doctor Payments
		desc.addField("payee_doctor_id", "Doctor");
		desc.addField("doctor_amount", "Doctor Fees");
		desc.addField("doc_payment_id", "Doctor Payment Voucher", false);
		desc.addField("dr_discount_amt", "Doctor Discount");
		desc.addField("discount_auth_dr", "Doctor Discount Authorizer", false);

		// referal payments
		desc.addField("referal_amount", "Referal Amount");
		desc.addField("ref_payment_id", "Referal Payment Voucher", false);
		desc.addField("ref_discount_amt", "Referal Discount");
		desc.addField("discount_auth_ref", "Referal Discount Authorizer", false);

		// outhouse payments
		desc.addField("out_house_amount", "Out House Amount");
		desc.addField("oh_payment_id", "Out House Payment Voucher", false);

		// prescribing doctor payments
		desc.addField("prescribing_dr_id", "Prescribing Doctor");
		desc.addField("prescribing_dr_amount", "Prescribing Doctor Amount");
		desc.addField("prescribing_dr_payment_id", "Prescribing Doctor Payment Voucher", false);
		desc.addField("pres_dr_discount_amt", "Prescribing Doctor Discount");
		desc.addField("discount_auth_pres_dr", "Prescribing Doctor Discount Authorizer", false);

		// hospital discounts
		desc.addField("hosp_discount_amt", "Hospital Discount");
		desc.addField("discount_auth_hosp", "Hospital Discount Authorizer", false);

		// overall discounts
		desc.addField("overall_discount_amt", "Overall Discount");
		desc.addField("overall_discount_auth", "Overall Discount Authorizer", false);

		desc.addField("activity", "Activity Conducted", false);

		// act_description_id, hasactivity
		desc.addFieldValue("status", statusMap);

		// Now set the lookup value mapping wherever necessary
		desc.addFieldValue("charge_head", "chargehead_constants", "chargehead_id", "chargehead_name");
		desc.addFieldValue("charge_group", "chargegroup_constants", "chargegroup_id", "chargegroup_name");

		desc.addFieldValue("payee_doctor_id", "doctors", "doctor_id", "doctor_name");
		desc.addFieldValue("prescribing_dr_id", "doctors", "doctor_id", "doctor_name");

		// TODO: We need to potimize on multiple fields referring to the same table.
		desc.addFieldValue("discount_auth_dr", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("discount_auth_ref", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("discount_auth_pres_dr", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("discount_auth_hosp", "discount_authorizer", "disc_auth_id", "disc_auth_name");
		desc.addFieldValue("overall_discount_auth", "discount_authorizer", "disc_auth_id", "disc_auth_name");

		// TODO : following field need to be included.
		// activity_desc_id, charge_ref, account_group, package_unit
		return desc;
	}
}
