package com.insta.hms.core.inventory.stocks;

import com.insta.hms.common.validation.NonNegativeNumberRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class StockUploadValidator extends MasterValidator {
	
	private final static String STOCK_UPLOAD_NOT_NULL_MSG = "exception.stockupload.notnull";
	private final static String[] NOT_NULL_FIELDS = new String[] { "dept_id", "medicine_id", "stock_pkg_size", "batch_no" };
	private final static String[] NON_NEGATIVE_FIELDS = new String[]{"qty", "package_cp", "package_sp"};
	
	private final static ValidationRule NOT_NULL_RULE = new NotNullRule(STOCK_UPLOAD_NOT_NULL_MSG);
	private final static ValidationRule NON_NEGATIVE_RULE = new NonNegativeNumberRule(); 
	
	public StockUploadValidator() {
		addRule("insert", NOT_NULL_RULE, NOT_NULL_FIELDS);
		addRule("insert", NON_NEGATIVE_RULE, NON_NEGATIVE_FIELDS);
	}
	
}