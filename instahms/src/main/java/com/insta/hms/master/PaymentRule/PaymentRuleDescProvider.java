package com.insta.hms.master.PaymentRule;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentRuleDescProvider implements AuditLogDescProvider {
	
	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);
		
		Map<String,String> paymentOptionsMap = new LinkedHashMap<String,String>();
		paymentOptionsMap.put("1", "Percentage");
		paymentOptionsMap.put("3", "Actual");
		paymentOptionsMap.put("4", "Less than bill amount");

		desc.addField("charge_head", "Charge Head", false);
		desc.addField("dr_payment_value", "Doctor Payment Value");
		desc.addField("dr_payment_option", "Doctor Payment Option");
		desc.addField("ref_payment_value", "Referal Payment Value");
		desc.addField("ref_payment_option", "Referal Payment Option");
		desc.addField("hosp_payment_value", "Hospital Payment Value");
		desc.addField("hosp_payment_option", "Hospital Payment Option");
		desc.addField("presc_payment_value", "Prescribed Payment Value");
		desc.addField("presc_payment_option", "Prescribed Payment Option");
		desc.addField("prescribed_category", "Prescribed Category");
		desc.addField("dr_pkg_amt", "Package Payment Amount");

		
		desc.addFieldValue("dr_payment_option", paymentOptionsMap);
		desc.addFieldValue("ref_payment_option", paymentOptionsMap);
		desc.addFieldValue("hosp_payment_option", paymentOptionsMap);
		desc.addFieldValue("presc_payment_option", paymentOptionsMap);
		
		desc.addFieldValue("charge_head", "chargehead_constants", "chargehead_id", "chargehead_name");
		return desc;
	}
	
}