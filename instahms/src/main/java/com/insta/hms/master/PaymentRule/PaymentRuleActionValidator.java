package com.insta.hms.master.PaymentRule;

import com.insta.hms.billing.payment.PaymentEngine;
import com.insta.hms.common.AbstractFormValidator;
import com.insta.hms.master.PaymentRule.PaymentRuleAction.ActionType;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

public class PaymentRuleActionValidator extends AbstractFormValidator<String> {

	static Logger log = LoggerFactory.getLogger(PaymentRuleActionValidator.class);

	private static final String PERCENTAGE_RANGE_ERROR = "Percentage must be between 0 to 100";
	private static final String INVALID_PRESCRIBED_PAYMENT_TYPE_SELECTED = "Invalid prescribed Payment Type selected";
	private static final String INVALID_REFERRAL_PAYMENT_TYPE_SELECTED = "Invalid Referral Payment Type selected";
	private static final String INVALID_DOCTOR_PAYMENT_TYPE_SELECTED = "Invalid Doctor Payment Type selected";
	private static final String ENCOUNTERED_EXCEPTION_WHILE_PERFORMING_VALIDATIONS = "Encountered exception while performing validations ";
	private static final String DOCTOR_PAYMENT_EXPRESSION_ERROR = "Doctor Payment Expression error: ";
	private static final String REFERER_PAYMENT_EXPRESSION_ERROR = "Referer Payment Expression error: ";
	private static final String PRESCRIBED_PAYMENT_EXPRESSION_ERROR = "Prescribed Payment Expression error: ";

	private ActionType actionType = null;
	private HttpServletRequest req = null;

	public PaymentRuleActionValidator(PaymentRuleAction.ActionType actionType, HttpServletRequest req) {
		this.req = req;
		this.actionType = actionType;

	}

	private void validateUpdateAction() throws Exception {
		validatePaymentTypesAndValues();
		validatePaymentRegexExpressions();
	}

	private void validatePaymentRegexExpressions() throws IOException {
		String drExpr = (String) req.getParameter("dr_payment_expr");
		String refExpr = (String) req.getParameter("ref_payment_expr");
		String prescExpr = (String) req.getParameter("presc_payment_expr");

		if (StringUtils.isNotBlank(drExpr)) {
			String drExprError = validateExpr(drExpr);
			if (StringUtils.isNotBlank(drExprError)) {
				errorList.add((DOCTOR_PAYMENT_EXPRESSION_ERROR + drExprError));
			}
		}

		if (StringUtils.isNotBlank(refExpr)) {
			String refExprError = validateExpr(refExpr);
			if (StringUtils.isNotBlank(refExprError)) {
				errorList.add(REFERER_PAYMENT_EXPRESSION_ERROR + refExprError);
			}
		}

		if (StringUtils.isNotBlank(prescExpr)) {
			String prescExprError = validateExpr(prescExpr);
			if (StringUtils.isNotBlank(prescExprError)) {
				errorList.add(PRESCRIBED_PAYMENT_EXPRESSION_ERROR + prescExprError);
			}
		}

	}

	private void validatePaymentTypesAndValues() {
		String docPaymentOption = (String) req.getParameter("dr_payment_option");
		String docPaymentValue = (String) req.getParameter("dr_payment_value");
		if (StringUtils.isBlank(docPaymentOption)) {
			errorList.add(INVALID_DOCTOR_PAYMENT_TYPE_SELECTED);
		} else {
			validateTypeValuePair(docPaymentOption, docPaymentValue);
		}

		String refPaymentOption = (String) req.getParameter("ref_payment_option");
		String refPaymentValue = (String) req.getParameter("ref_payment_value");
		if (StringUtils.isBlank(docPaymentOption)) {
			errorList.add(INVALID_REFERRAL_PAYMENT_TYPE_SELECTED);
		} else {
			validateTypeValuePair(refPaymentOption, refPaymentValue);
		}
		String prescPaymentOption = (String) req.getParameter("presc_payment_option");
		String prescPaymentValue = (String) req.getParameter("presc_payment_value");
		if (StringUtils.isBlank(prescPaymentOption)) {
			errorList.add(INVALID_PRESCRIBED_PAYMENT_TYPE_SELECTED);
		} else {
			validateTypeValuePair(prescPaymentOption, prescPaymentValue);
		}

	}

	private void validateTypeValuePair(String paymentOption, String paymentValue) {
		switch (paymentOption) {
		case "1":
			validatePercentage(paymentValue);
			break;
		case "3":
		case "4":
			validateAmount(paymentValue);
			break;
		}
	}

	private void validateAmount(String paymentValue) {
		try {
			double amount = Double.parseDouble(paymentValue);
			if (amount < 0) {
				errorList.add("Amount cannot be less than 0, entered value is "+ paymentValue);
			}
		} catch (Exception e) {
			errorList.add(paymentValue + " is not a valid amount format.");
		}

	}

	private void validatePercentage(String paymentValue) {
		try {
			double percentage = Double.parseDouble(paymentValue);
			if (percentage < 0 || percentage > 100) {
				errorList.add(PERCENTAGE_RANGE_ERROR + ", entered value is " + percentage);
			}
		} catch (Exception ex) {
			errorList.add(paymentValue + " is not a valid percentage format.");
		}
	}

	@Override
	protected void validate() {
		try {
			switch (this.actionType) {
			case UPDATE:
				validateUpdateAction();
				break;
			case CREATE:
				validateCreateAction();
				break;
			default:
				throw new Exception("Unimplemented validator type");
			}
		} catch (Exception e) {
			errorList.add(ENCOUNTERED_EXCEPTION_WHILE_PERFORMING_VALIDATIONS + e.getMessage());
			log.error(ENCOUNTERED_EXCEPTION_WHILE_PERFORMING_VALIDATIONS, e);
		}
	}

	private void validateCreateAction() throws IOException {
		validatePaymentTypesAndValues();
		validatePaymentRegexExpressions();
	}

	private static String validateExpr(String expression) throws IOException {

		expression = "<#setting number_format=\"#\">\n" + expression;
		StringWriter writer = new StringWriter();

		try {
			Template t = new Template("Expression", new StringReader(expression), new Configuration());
			HashMap<String, Object> params = new HashMap<String, Object>();
			PaymentEngine.putPaymentExprParams(params, null, null, null, null, null, null);
			t.process(params, writer);

		} catch (TemplateException e) {
			log.error("Template Error: ", e);
			return "Invalid expression: " + e.getMessage();
		} catch (freemarker.core.ParseException e) {
			log.error("Parse error: ", e);
			return "Invalid expression: " + e.getMessage();
		}

		log.debug("Result before trim: " + writer.toString());
		String valueStr = writer.toString().trim();
		try {
			BigDecimal valueNumber = new BigDecimal(valueStr);
		} catch (NumberFormatException e) {
			String msg = "Conversion error: Expression resulted in '" + valueStr + "'. This is not a valid number";
			log.error(msg, e);
			return msg;
		}
		log.debug("Valid result: " + valueStr);
		return null;
	}

}
