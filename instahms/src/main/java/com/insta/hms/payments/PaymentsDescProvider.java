package com.insta.hms.payments;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentsDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);
		desc.addField("voucher_no", "Voucher No");
		desc.addField("amount", "Amount");
		desc.addField("tax_type", "Tax Type");
		desc.addField("tax_amount", "Tax Amount");
		desc.addField("date", "Payment Date");
		desc.addField("counter", "Counter");
		desc.addField("payment_mode_id", "Payment Mode");
		desc.addField("bank", "Bank");
		desc.addField("reference_no", "Reference No");
		desc.addField("payee_name", "Payee");
		desc.addField("tds_amount", "TDS Amount");
		desc.addField("remarks", "Remarks");
		desc.addField("voucher_category", "Payment Category");
		desc.addField("payment_type", "Payment Type");

		Map paymentTypeMap = new LinkedHashMap();
		paymentTypeMap.put("D", "Consulting Doctor Payment");
		paymentTypeMap.put("P", "Prescribing Doctor Payment");
		paymentTypeMap.put("R", "Referral Doctor Payment");
		paymentTypeMap.put("F", "Referral Doctor Payment");
		paymentTypeMap.put("O", "Outhouse Payment");
		paymentTypeMap.put("S", "Supplier Payment");
		paymentTypeMap.put("C", "Miscellaneous Payment");

		Map voucherCategoryMap = new LinkedHashMap();
		voucherCategoryMap.put("P", "Payments");
		voucherCategoryMap.put("R", "Payment Reversal");

		desc.addFieldValue("payment_type", paymentTypeMap);
		desc.addFieldValue("voucher_category", voucherCategoryMap);

		desc.addFieldValue("payment_mode_id", "payment_mode_master", "mode_id", "payment_mode");
		desc.addFieldValue("counter", "counters", "counter_id", "counter_no");
		desc.addFieldValue("payee_name", "payeenames_view", "payee_id", "payee_name");

		return desc;
	}

}
