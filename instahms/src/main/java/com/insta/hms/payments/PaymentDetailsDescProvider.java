package com.insta.hms.payments;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentDetailsDescProvider implements AuditLogDescProvider {

	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);
		desc.addField("payment_type", "Payment Type");
		desc.addField("voucher_no", "Voucher No");
		desc.addField("amount", "Amount");
		desc.addField("description", "Description");
		desc.addField("category", "Payment Category");
		desc.addField("posted_date", "Posted Date");
		desc.addField("payee_name", "Payee");
		desc.addField("charge_id", "Charge Head");
		desc.addField("account_head", "Account Head");
		desc.addField("activity_id", "Activity");
		desc.addField("account_group", "Account Group");

		Map paymentTypeMap = new LinkedHashMap();
		paymentTypeMap.put("D", "Consulting Doctor Payment");
		paymentTypeMap.put("P", "Prescribing Doctor Payment");
		paymentTypeMap.put("R", "Referral Doctor Payment");
		paymentTypeMap.put("F", "Referral Doctor Payment");
		paymentTypeMap.put("O", "Outhouse Payment");
		paymentTypeMap.put("S", "Supplier Payment");
		paymentTypeMap.put("C", "Miscellaneous Payment");

		desc.addFieldValue("payment_type", paymentTypeMap);
		desc.addFieldValue("payee_name", "payeenames_view", "payee_id", "payee_name");
		return desc;
	}

}
