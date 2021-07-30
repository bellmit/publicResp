package com.insta.hms.master.SponsorProcedureMaster;

import com.insta.hms.common.validation.NonNegativeNumberRule;
import com.insta.hms.common.validation.NotNullRule;
import com.insta.hms.common.validation.ValidationRule;
import com.insta.hms.mdm.MasterValidator;

import org.springframework.stereotype.Component;

@Component
public class SponsorProcedureValidator extends MasterValidator {
	
	private final static String[] NOT_NULL_FIELDS = new String[]{"tpa_id", "procedure_no", "procedure_code", "procedure_name"};
	private final static String[] NON_NEGATIVE_FIELDS = new String[]{"procedure_limit"};
	
	private final static ValidationRule NOT_NULL_RULE = new NotNullRule(); 
	private final static ValidationRule NON_NEGATIVE_RULE = new NonNegativeNumberRule(); 
	
	public SponsorProcedureValidator() {
		super();
		addDefaultRule(NOT_NULL_RULE, NOT_NULL_FIELDS);
		addDefaultRule(NON_NEGATIVE_RULE, NON_NEGATIVE_FIELDS);
	}
}
