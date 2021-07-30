<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insta HMS</title>
<insta:link type="js" file="master/accounting/accountingprefs.js"/>
	<style type="text/css">
		.field {width: 170px}
	</style>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>
<body>
	<h1>Special Account Names</h1>

	<form action="SpecialAccountNames.do" method="POST">

		<input type="hidden" name="method" value="update"/>

		<fieldset class="fieldSetBorder">
			<table class="formtable">


				<tr>
					<td class="formlabel">TDS on Receipts: </td>
					<td class><input type="text" class="field" name="tds_receipt_ac_name" id="tds_receipt_ac_name"
						value="${specialaccountnames.tds_receipt_ac_name}"/>
					</td>
					<td class="formlabel">TDS On Payments:</td>
					<td class><input type="text" class="field" name="tds_payment_ac_name" id="tds_payment_ac_name"
						value="${specialaccountnames.tds_payment_ac_name}"/>
					<td class="formlabel">TPA/Sponsor Claims: </td>
					<td>
						<input type="text" class="field" name="claims_ac_name" id="claims_ac_name"
							value="${specialaccountnames.claims_ac_name}">
					</td>
				</tr>
				<tr>
					<td class="formlabel" >Patient Deposits:</td>
					<td>
						<input type="text" class="field" name="patient_deposit_ac_name" id="patient_deposit_ac_name" value="${specialaccountnames.patient_deposit_ac_name}"/>
					</td>
					<td class="formlabel" >Hospital Transfer: </td>
					<td>
						<input type="text" class="field" name="hospital_transfer_act_name" id="hospital_transfer_act_name" value="${specialaccountnames.hospital_transfer_act_name}">
						<img class="imgHelpText" src="${cpath}/images/help.png" title="Account Name used by the Pharmacy when transferring items from Hospital, to be paid for later"/>
					</td>
					<td class="formlabel" >Transfer Expenses: </td>
					<td ><input type="text" name="transfer_expenses" id="transfer_expenses" value="${specialaccountnames.transfer_expenses}"/>
						<img class="imgHelpText" src="${cpath}/images/help.png" title="Pharmacy: Expenses towards transfers"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel" >Pharmacy Claims: </td>
					<td >
						<input type="text" class="field" name="pharma_claim_ac_name" id="pharma_claim_ac_name" value="${specialaccountnames.pharma_claim_ac_name}">
					</td>
					<td class="formlabel">Pharmacy Receipts: </td>
					<td>
						<input type="text" class="field" name="pharma_receipts_ac_name" id="pharma_receipts_ac_name" value="${specialaccountnames.pharma_receipts_ac_name}">
					</td>
					<td class="formlabel" >Pharmacy Refunds:</td>
					<td>
						<input type="text" class="field" name="pharma_refunds_ac_name" id="pharma_refunds_ac_name" value="${specialaccountnames.pharma_refunds_ac_name}">
					</td>
				</tr>

				<tr>
					<td class="formlabel" >IP Receivables:</td>
					<td>
						<input type="text" class="field" name="counter_receipts_ac_name_ip"
							id="counter_receipts_ac_name_ip" value="${specialaccountnames.counter_receipts_ac_name_ip}">
						<img class="imgHelpText" src="${cpath}/images/help.png"
							title="The customer account to which all advances are credited and payables are debited, in case of In Patients"/>
					</td>

					<td class="formlabel" >OP Receivables:</td>
					<td>
						<input type="text" class="field" name="counter_receipts_ac_name_op"
							id="counter_receipts_ac_name_op" value="${specialaccountnames.counter_receipts_ac_name_op}">
						<img class="imgHelpText" src="${cpath}/images/help.png"
							title="Same as IP Receivables, but for OP"/>
					</td>

					<td class="formlabel" >Other Receivables:</td>
					<td>
						<input type="text" class="field" name="counter_receipts_ac_name_others" id="counter_receipts_ac_name_others" value="${specialaccountnames.counter_receipts_ac_name_others}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Recievables from other customers (eg, Pharmacy retail)"/></td>
				</tr>

				<tr>
					<td class="formlabel">Conducting Dr. Expenses: </td>
					<td>
						<input type="text" class="field" name="doctor_payments_exp_ac_name" id="doctor_payments_exp_ac_name" value="${specialaccountnames.doctor_payments_exp_ac_name}">
					</td>
					<td class="formlabel" >Prescribing Dr. Expenses: </td>
					<td >
						<input type="text" class="field" name="prescribing_doctor_payments_exp_ac_name" id="prescribing_doctor_payments_exp_ac_name" value="${specialaccountnames.prescribing_doctor_payments_exp_ac_name}">
					</td>
					<td class="formlabel" >Referer Expenses: </td>
					<td>
						<input type="text" class="field" name="referral_payments_exp_act_name" id="referral_payments_exp_act_name" value="${specialaccountnames.referral_payments_exp_act_name}">
					</td>
				</tr>

				<tr>
					<td class="formlabel">Outhouse Expenses: </td>
					<td>
						<input type="text" class="field" name="outhouse_payments_exp_act_name" id="outhouse_payments_exp_act_name" value="${specialaccountnames.outhouse_payments_exp_act_name}">
					</td>
					<td class="formlabel" >Miscellaneous Expenses: </td>
					<td >
						<input type="text" class="field" name="misc_payments_ac_name" id="misc_payments_ac_name" value="${specialaccountnames.misc_payments_ac_name}">
						<img class="imgHelpText" src="${cpath}/images/help.png" title="Expense accounts for payments made as miscellaneous payments."/>
					</td>
					<td class="formlabel" >Outgoing CED: </td>
					<td>
						<input type="text" class="field" name="outgoing_ced_ac_name" id="outgoing_ced_ac_name" value="${specialaccountnames.outgoing_ced_ac_name}">
					</td>
				</tr>

				<tr>
					<td class="formlabel" >Purchases: </td>
					<td ><input type="text" class="field" name="inv_purchase_ac_name" id="inv_purchase_ac_name" value="${specialaccountnames.inv_purchase_ac_name}">
						<img class="imgHelpText" src="${cpath}/images/help.png" title="Pharmacy/Inventory Purchase Account Name"/>
					</td>

					<td class="formlabel" >Outgoing VAT: </td>
					<td>
						<input type="text" class="field" name="outgoing_vat_ac_name" id="outgoing_vat_ac_name" value="${specialaccountnames.outgoing_vat_ac_name}">
					</td>

					<td class="formlabel" >Outgoing CST: </td>
					<td>
						<input type="text" name="incoming_cst_ac_name" id="incoming_cst_ac_name" value="${specialaccountnames.incoming_cst_ac_name}">
						<img class="imgHelpText" src="${cpath}/images/help.png" title="Outgoing CST Account Name"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Invoice Discounts: </td>
					<td >
						<input type="text" class="field" name="pharm_inv_disc_ac_name" id="pharm_inv_disc_ac_name" value="${specialaccountnames.pharm_inv_disc_ac_name}">
					</td>

					<td class="formlabel">Invoice Round-Offs: </td>
					<td >
						<input type="text" class="field" name="pharm_inv_round_off_ac_name" id="pharm_inv_round_off_ac_name" value="${specialaccountnames.pharm_inv_round_off_ac_name}">
					</td>

					<td class="formlabel" >Invoice Other Charges: </td>
					<td>
						<input type="text" class="field" name="pharm_inv_other_charges_ac_name" id="pharm_inv_other_charges_ac_name" value="${specialaccountnames.pharm_inv_other_charges_ac_name}"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Sales Discount:</td>
					<td ><input type="text" class="field" name="pharm_sales_disc_ac_name" id="pharm_sales_disc_ac_name" value="${specialaccountnames.pharm_sales_disc_ac_name}">
					</td>
					<td class="formlabel" >Sales Round-Off: </td>
					<td ><input type="text" class="field" name="pharm_sales_round_off_ac_name" id="pharm_sales_round_off_ac_name" value="${specialaccountnames.pharm_sales_round_off_ac_name}">
					</td>
					<td class="formlabel" >Incoming VAT: </td>
					<td ><input type="text" class="field" name="incoming_vat_ac_name" id="incoming_vat_ac_name" value="${specialaccountnames.incoming_vat_ac_name}">
					</td>
				</tr>
				<tr>
					<td class="formlabel">Patient Points:</td>
					<td><input type="text" name="patient_points_ac_name" class="field" id="patient_points_ac_name" value="${specialaccountnames.patient_points_ac_name}"/>
					</td>
				</tr>
			</table>
		</fieldset>
		<div class="screenActions" >
			<button type="submit" name="save" accesskey="S">
			<b><u>S</u></b>ave</button>
			 | <a href="javascript:void(0);" onclick="return dashboard('${pageContext.request.contextPath}');">Accounting Preferences</a>
		</div>

	</form>
</body>
</html>
