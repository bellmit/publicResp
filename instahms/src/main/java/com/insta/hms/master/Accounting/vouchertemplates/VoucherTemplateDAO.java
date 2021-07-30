package com.insta.hms.master.Accounting.vouchertemplates;

import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;

public class VoucherTemplateDAO extends GenericDAO {

	public VoucherTemplateDAO(){
		super("acc_voucher_templates");
	}

	public String getCustomizedTemplate(String voucherType) throws SQLException {
		return (String) findByKey("voucher_type", voucherType).get("template_content");
	}
}
