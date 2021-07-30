package com.insta.hms.billing;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AllReceiptDetailsDescProvider implements AuditLogDescProvider {
	public AuditLogDesc getAuditLogDesc(String tableName) {
		AuditLogDesc desc = new AuditLogDesc(tableName);

		Map<String,String> ReceiptMap = new LinkedHashMap<String,String>();

		Map paymentTypeMap = new HashMap();
		paymentTypeMap.put("DF", "deposit_refund");
		paymentTypeMap.put("DR", "deposit_settlement");
		paymentTypeMap.put("R", "receipt");
		paymentTypeMap.put("S", "sponsor_receipt");
		paymentTypeMap.put("F", "refund");

		Map receiptTypeMap = new HashMap();
		receiptTypeMap.put("A", "advance");
		receiptTypeMap.put("S", "settlement");

		desc.addField("receipt_no", "Receipt No");
		desc.addField("deposit_no", "Deposit No");
		desc.addField("bill_no","Bill No");
		desc.addField("amount", "Amount");
		desc.addField("display_date", "Receipt Date");
		desc.addField("deposit_date", "Deposit Date");
		desc.addField("bank_name", "Bank Name");
		desc.addField("reference_no", "Reference No");
		desc.addField("mod_time", "Mod Time");
		desc.addField("paid_by", "Paid By");

		desc.addFieldValue("payment_type", paymentTypeMap);
		desc.addFieldValue("recpt_type", receiptTypeMap);
		desc.addFieldValue("payment_mode_id", "payment_mode_master", "mode_id", "payment_mode");
		desc.addFieldValue("card_type_id", "card_type_master", "card_type_id", "card_type");
		desc.addFieldValue("counter", "counters", "counter_id", "counter_no");

		desc.addField("bank_batch_no", "Bank Batch No");
		desc.addField("card_auth_code", "Card Auth Code");
		desc.addFieldValue("currency_id", "foreign_currency", "currency_id", "currency");
		desc.addField("exchange_rate", "Exchange Rate");
		desc.addField("currency_amt", "Currency Amt");
		desc.addField("card_number", "Card Number");

		return desc;

}
}