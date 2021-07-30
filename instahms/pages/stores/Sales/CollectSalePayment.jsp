<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<head>
<title><insta:ltext
		key="salesissues.pendingbillscolection.details.bill" /> :
	${ifn:cleanHtml(billNo)}</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true" />
<insta:link type="js" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="billPaymentCommon.js" />
<insta:link type="script" file="billing.js" />
<insta:link type="script" file="sockjs.min.js" />
<insta:link type="script" file="stomp.min.js" />

<%
	request.setAttribute("genPrefs",
			com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getGenericPreferences());
%>

<c:set var="bill"
	value="${not empty billDetails ? billDetails.getBill() : ''}" />
<c:set var="points_redemption_rate"
	value="${not empty genPrefs.points_redemption_rate ? genPrefs.points_redemption_rate : 0}" />
<c:set var="no_of_credit_debit_card_digits"
	value="${genPrefs.no_of_credit_debit_card_digits}" />
<c:set var="total_deposits"
	value="${not empty depositAmt ? depositAmt.map.total_deposits : 0}" />
<c:set var="total_deposit_set_off"
	value="${not empty depositAmt ? depositAmt.map.total_deposit_set_off : 0}" />
<c:set var="existingReceipts" value="${bill.totalReceipts}" />
<c:set var="existingRecdAmount" value="${bill.claimRecdAmount}" />
<c:set var="totalAmount" value="${bill.totalAmount}" />
<c:set var="incomeTaxCashLimitApplicability"
	value="${genPrefs.incomeTaxCashLimitApplicability}" />
<c:set var="cashTransactionLimit" value="${cashTransactionLimit}"/>

<script type="text/javascript">
var billNumber='${billNo}';
var points_redemption_rate = ${empty points_redemption_rate ? 0 : points_redemption_rate};
var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
var jPaymentModes = <%=request.getAttribute("paymentModesJSON")%>;
var ipDepositSetOff = 0;
var generalDepositSetOff = ${total_deposit_set_off};
var availableDeposits = ${total_deposits};
var ipDeposits = 0;
var availableRewardPoints = 0;
var availableRewardPointsAmount = 0;
var billRewardPoints = 0;
var isMvvPackage  = 0;
var visitType = '';
var hasRewardPointsEligibility = false;
var origBillStatus = '${bill.status}';
var priSponsorType = '';
var secSponsorType = '';
var priSponsorId = '';
var secSponsorId = '';
var multiPlanExists = '';
var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
var totalBilledAmount = ${empty totalAmount ? 0 : totalAmount};
var existingSponsorReceipts = 0;
var sponsorBillNo = 0;
var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
var separator_type = '${ifn:cleanJavaScript(genPrefs.separator_type)}';
var currency_format = '${genPrefs.currency_format}';
var totalDeposit = '${total_deposits}';
var patientCredit = 0;
var visitId = '${bill.visitId}';
var mrno = '${bill.mrno}';
var visit_type = '${bill.visitType}';
var income_tax_cash_limit_applicability = '${incomeTaxCashLimitApplicability}';
var cashTransactionLimitAmt =  ${empty cashTransactionLimit ? 0 : cashTransactionLimit};

function onSubmit(){
	var valid = true;
	valid = valid && validatePayDates();
	valid = valid && validateCounter();
	valid = valid && validatePaymentRefund();
	valid = valid && validatePaymentTagFields();
	valid = valid && validateAllNumerics();
	valid = valid && validatePaymentAmount();
	valid = valid && doPaytmTransactions();
	valid = valid && checkTransactionLimitValue();
	valid = valid && checkDepositExistsAndNotUsed();
	if(income_tax_cash_limit_applicability == 'Y') {
		if(visit_type == 'i' || visit_type =='o'){
			valid = valid && checkCashLimitValidation(mrno,visitId);
		}else if (visit_type == 'r'){
			valid = valid && checkRetCashLimitValidation();
		}
	}

	if (!valid) return false;
	enableFormValues();
	document.mainform.submit();
}

function init() {
	resetPayments();
	mainform = document.mainform;

} 

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 * getTotalRewardPointsAvailable() -- To get the reward points available.
 */
function resetPayments() {

	resetTotalsForPayments();
	resetTotalsForDepositPayments();
	resetTotalsForPointsRedeemed();

	// for bill now bill, auto-set the amount to be paid by patient.
	if (document.mainform.billType.value == 'BN') {
		// if a single payment mode exists, update that with the due amount automatically
		setTotalPayAmount();
	}
}

function getTotalAmount() {
	return getPaise(document.getElementById("grandtotal").value);
}

function getTotalAmountDue() {
	return getPaise(document.getElementById("grandtotal").value);
}

function getTotalDepositAmountAvailable() {
	if (!isReturns) {
		if (document.mainform.maxAmt != null) {
			return getPaise(document.mainform.maxAmt.value);
		}else
			return 0;
	}else {
		if (document.mainform.DepoSetOfAgainstBill != null) {
			return getPaise(document.mainform.DepoSetOfAgainstBill.value);
		}else
			return 0;
	}
}

//Check the Cash Limit for Retail Patients
function checkRetCashLimitValidation(){
	var amount =0;
	var numPayments = getNumOfPayments();
	for (i=0; i<numPayments; i++){
		var totPayingAmt = "totPayingAmt"+i;
		var paymentModeId = "paymentModeId"+i;
		var paymentModelValue = $("#"+paymentModeId+" option:selected").val();
		if (paymentModelValue == -1) {
			var cashAmount = $("#"+totPayingAmt+"").val();
			amount += getAmount(cashAmount);
		}
		if (amount != 0 && amount > cashTransactionLimitAmt){
			alert("Total cash in aggregate from this patient in a day reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");
			return false;
		}
	}
	return true;
}

</script>
<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon" />
<insta:js-bundle prefix="billing.billlist"/>
<insta:js-bundle prefix="billing.salucro"/>

</head>

<body class="yui-skin-sam"
	onload="init();ajaxForPrintUrls();loadLoyaltyDialog();loadProcessPaymentDialog();filterPaymentModes();">

	<h1>
		<insta:ltext
			key="salesissues.pendingbillscolection.details.pendingbillscollection" />
	</h1>

	<c:choose>
		<c:when test="${empty rc}">
			<insta:patientdetails visitid="${param.visitId}" />
		</c:when>
		<c:otherwise>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">
					<insta:ltext
						key="salesissues.pendingbillscolection.details.retailcustomerdetails" />
				</legend>
				<table class="formtable" cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td class="formlabel" width="10%"><insta:ltext
								key="salesissues.pendingbillscolection.details.name" />:</td>
						<td class="forminfo">${rc.customer_name}</td>
						<td class="formlabel" width="10%"><insta:ltext
								key="salesissues.pendingbillscolection.details.doctor" />:</td>
						<td class="forminfo">${saleDetails[0].map.doctor_name}</td>
					</tr>
				</table>
			</fieldset>
		</c:otherwise>
	</c:choose>

	<form method="POST" action="pendingSalesBill.do" name="mainform">

		<input type="hidden" name="saleId"
			value="${ifn:cleanHtmlAttribute(saleId)}" /> <input type="hidden"
			name="billNo" value="${ifn:cleanHtmlAttribute(billNo)}" /> <input
			type="hidden" name="saleType"
			value="${ifn:cleanHtmlAttribute(saleType)}" /> <input type="hidden"
			name="_method" value="collectSalePayments" /> <input type="hidden"
			name="billStatus" id="billStatus" value="${bill.status}" /> <input
			type="hidden" name="dynaPkgId" id="dynaPkgId" value="" />



		<table class="dataTable" id="chargesTable" width="100%">
			<tr>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.itemname" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.qty" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.manufacturer" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.batch.or.serial.no" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.expirydate" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.amount" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.patamt" /></th>
				<th><insta:ltext
						key="salesissues.pendingbillscolection.details.claimamt" /></th>
			</tr>

			<c:set var="total" value="0" />
			<c:set var="discount" value="${saleDetails[0].map.bill_discount}" />
			<c:set var="round_off" value="${saleDetails[0].map.round_off}" />

			<c:forEach items="${saleDetails}" var="saleItemBean">
				<c:set var="saleItem" value="${saleItemBean.map}" />
				<c:set var="patAmt"
					value="${saleItem.amount - saleItem.insurance_claim_amt - saleItem.pri_sponsor_tax_amt - saleItem.sec_sponsor_tax_amt}" />
				<tr>
					<td><insta:truncLabel value="${saleItem.medicine_name}"
							length="25" /></td>
					<td>${saleItem.quantity}</td>
					<td>${saleItem.manf_mnemonic}</td>
					<td>${saleItem.batch_no}</td>
					<td><fmt:formatDate value="${saleItem.expiry_date}"
							pattern="MMM-yyyy" /></td>
					<td>${saleItem.amount}</td>
					<td>${patAmt }</td>
					<td>${saleItem.insurance_claim_amt + saleItem.pri_sponsor_tax_amt + saleItem.sec_sponsor_tax_amt}</td>
				</tr>
				<c:set var="total" value="${total + patAmt}" />
			</c:forEach>

			<c:if test="${discount!='0.00'}">
				<tr>
					<td colspan="6">&nbsp;</td>
					<td><insta:ltext
							key="salesissues.pendingbillscolection.details.discount" /></td>
					<td>${discount}</td>
				</tr>
				<c:set var="total" value="${total - discount}" />
			</c:if>

			<c:if test="${round_off!='0.00'}">
				<tr>
					<td colspan="6">&nbsp;</td>
					<td><insta:ltext
							key="salesissues.pendingbillscolection.details.roundoff" /></td>
					<td>${round_off}</td>
				</tr>
				<c:set var="total" value="${total + round_off}" />

			</c:if>
			<c:set var="total" value="${total - totalReciepts}" />
		</table>

		<div id="paymentDetails">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">
					<insta:ltext
						key="salesissues.pendingbillscolection.details.paymentdetails" />
					:
				</legend>
				<table class="formtable" cellpadding="0" cellspacing="0"
					width="100%">
					<tr>
						<td class="formlabel"><insta:ltext
								key="salesissues.pendingbillscolection.details.billtype" />:</td>
						<td class="forminfo"><select name="billType" id="billType"
							class="dropdown">
								<option value="BN"><insta:ltext
										key="salesissues.pendingbillscolection.details.billnow" /></option>
						</select></td>
						<td></td>
						<td></td>
						<td class="formlabel"><insta:ltext
								key="salesissues.pendingbillscolection.details.totaltopay" />:</td>
						<td class="forminfo">${total}<input type="hidden"
							name="grandtotal" id="grandtotal" value="${total}" />
						</td>
					</tr>
				</table>

				<c:set var="isrefund" value="${saleType == 'R'}" />
				<insta:billPaymentDetails formName="mainform"
					isBillNowPayment="true" isRefundPayment="${isrefund}"
					hasRewardPointsEligibility="false" availableRewardPoints="0"
					availableRewardPointsAmount="0" origBillStatus="${bill.status}" />

			</fieldset>
		</div>

		<c:set var="hasDeposit"
			value="${not empty depositAmt && (depositAmt.map.total_deposits - depositAmt.map.total_deposit_set_off) > 0}" />
		<c:set var="hasRewardPoints"
			value="${param.saleType eq 'S' && not empty total_points && total_points > 0}" />

		<c:if test="${hasDeposit}">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Deposits</legend>
				<table class="formtable" cellpadding="0" cellspacing="0" width="90%"
					border="0">
					<tr>
						<c:if test="${hasDeposit}">
							<td colspan="2">
								<table>
									<tr>
										<td class="formlabel"><insta:ltext
												key="billing.patientbill.details.depositsBalance" />:</td>
										<td>${depositAmt.map.total_deposits - depositAmt.map.total_deposit_set_off}
										</td>
									</tr>
								</table>
							</td>
						</c:if>
					</tr>
				</table>
			</fieldset>
		</c:if>

		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td>
					<button id="save" type="button" accesskey="P"
						Name="Pay &amp; Print" class="button" onclick="onSubmit();">
						<u><insta:ltext
								key="salesissues.pendingbillscolection.details.p" /></u><insta:ltext key="salesissues.pendingbillscolection.details.ay" />
						&amp;
						<insta:ltext key="salesissues.pendingbillscolection.details.print" />
					</button>
				</td>
				<td align="right"><insta:selectdb name="printerId"
						table="printer_definition" valuecol="printer_id"
						displaycol="printer_definition_name"
						value="${bean.map.printer_id}" /></td>
			</tr>
		</table>

	</form>
	<jsp:include page="/pages/dialogBox/processPaymentDialog.jsp"></jsp:include>
	<jsp:include page="/pages/dialogBox/loyaltyDialog.jsp"></jsp:include>
</body>

</html>
