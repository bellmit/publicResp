<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
<title><insta:ltext key="salesissues.pendingbillscolection.details.bill"/> :${ifn:cleanHtml(billNo)}</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<insta:link type="js" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="billPaymentCommon.js" />
<insta:link type="script" file="billing.js" />
<insta:link type="script" file="sockjs.min.js"/>
<insta:link type="script" file="stomp.min.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="no_of_credit_debit_card_digits" value='<%=GenericPreferencesDAO.getAllPrefs().get("no_of_credit_debit_card_digits") %>'/>
<c:set var="incomeTaxCashLimitApplicability" value='<%=GenericPreferencesDAO.getAllPrefs().get("income_tax_cash_limit_applicability") %>' />
<c:set var="cashTransactionLimit" value="${cashTransactionLimit}"/>

<script type="text/javascript">

var billStatus = '${ifn:cleanJavaScript(status)}';
var screenId = '${screenId}';
var billNumber = '${billNo}'; // This will be used as reference to Paytm transaction
var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
var ipDepositSetOff = 0;
var generalDepositSetOff = ${empty total_deposit_set_off ? 0 : total_deposit_set_off};
var availableDeposits = ${empty total_balance ? 0 : total_balance};
var ipDeposits = 0;
var availableRewardPoints = 0;
var availableRewardPointsAmount = 0;
var billRewardPoints = 0;
var isMvvPackage  = 0;
var visitType = '${billDetails.getBill().visitType}';
var hasRewardPointsEligibility = false;
var visitId = '${billDetails.getBill().visitId}';
var mrno = '${billDetails.getBill().mrno}';
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
		if(visitType == 'i' || visitType =='o'){
			valid = valid && checkCashLimitValidation(mrno,visitId);
		}else if (visitType == 'r'){
			valid = valid && checkRetCashLimitValidation();
		}
	}

	if (!valid) return false;
	enableFormValues();
	document.retailCreditForm.submit();
}

function onChangeDepositSetOff() {

	if (!validateDepositSetOff(document.retailCreditForm.depositsetoff))
		return false;

	return true;
}

function init(){
	var paymentType = null;

	if (billStatus == 'C' || billStatus == 'X' || billStatus == 'S') {
		document.getElementById('paymentDetails').style.display = 'none';
	} else {
		document.getElementById('paymentDetails').style.display = 'block';
	}
	resetPayments();
}


/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 * getTotalDepositAmountAvailable() -- To get the deposit amount available.
 */
function resetPayments() {
	resetTotalsForPayments();
	resetTotalsForDepositPayments();
}

function getTotalAmount() {
	return getPaise(document.getElementById("lblTotAmtDue").innerHTML);
}

function getTotalAmountDue() {
	return getPaise(document.getElementById("lblTotAmtDue").innerHTML);
}

function getTotalDepositAmountAvailable() {
	if (!isReturns) {
		if (document.getElementById("totaldeposits") != null) {
			return getPaise(document.getElementById("totaldeposits").value);
		}else
			return 0;
	}else {
		if (document.getElementById("totaldepositsSetoff") != null) {
			return getPaise(document.getElementById("totaldepositsSetoff").value);
		}else
			return 0;
	}
}

//Check the Cash Limit for Retail Patients
function checkRetCashLimitValidation(){
	var numPayments = getNumOfPayments();
	if (numPayments <= 0) return true;
	var amount =0;
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
<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
<insta:js-bundle prefix="billing.salucro"/>
<insta:js-bundle prefix="billing.billlist"/>
</head>

<body class="yui-skin-sam" onload="init();ajaxForPrintUrls();loadLoyaltyDialog();loadProcessPaymentDialog();filterPaymentModes();">

<h1><insta:ltext key="salesissues.pendingbillscolection.details.patientcreditbill"/></h1>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"><insta:ltext key="salesissues.pendingbillscolection.details.patientcreditbill"/></legend>
<table class="formtable" cellpadding="0" cellspacing="0" border="0">
	<tr>
	    <td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.billno"/>:</td>
	    <td class="forminfo">${ifn:cleanHtml(billNo)}</td>
	    <td></td>
	    <td></td>
	    <td></td>
	    <td></td>
	</tr>
</table>
</fieldset>

<insta:patientdetails  visitid="${visitId}"/>

<form method="POST" action="PhItemsCreditBill.do" name="retailCreditForm" >

<input type="hidden" value="collectCreditBillPayments" name="_method" />
<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(billNo)}">
<input type="hidden" name="billType" value="${billType}">
<input type="hidden" name="customerId" value="${ifn:cleanHtmlAttribute(visitId)}">
<input type="hidden" name="doctor" value="${doctor}" />
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}" />

<table class="detailList" id="saleItemList" cellpadding="0" cellspacing="0" border="0" width="100%">
	<tr>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.date"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.head"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.description"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.remarks"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.rate"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.qty"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.discount"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.amount"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.patamt"/></th>
		<th><insta:ltext key="salesissues.pendingbillscolection.details.claimamt"/></th>
	</tr>

	<c:set var="total" value="0" />
	<c:set var="advance" value="0" />
	<c:set var="refundAmt" value="0" />
	<c:set var="totAmtdue" value="0" />

	<c:forEach items="${billDetails.receipts}" var="receipt">
		<c:if test="${receipt.counterType eq 'P'}">
			<c:set var="advance" value="${advance + receipt.amount}" />
		</c:if>
	</c:forEach>

	<c:forEach items="${billDetails.refunds}" var="refund">
		<c:if test="${refund.counterType eq 'P'}">
		  <c:set var="refundAmt" value="${refundAmt - refund.amount}" />
		 </c:if>
	</c:forEach>

  <c:set var="discount" value="0"/>
	<c:set var="round_off" value="0"/>

	<c:forEach items="${billIems}" var="saleItemBean">
		<c:set var="saleItem" value="${saleItemBean.map}" />
		<c:set var="patAmt" value="${saleItem.amount-saleItem.insurance_claim_amount}"/>
		<tr>
			<td><fmt:formatDate value="${saleItem.posted_date}" pattern="dd-MM-yyyy" /></td>
			<td>${saleItem.chargehead_name}</td>
			<td>${saleItem.act_description}</td>
			<td>
				<c:set var="remLen" value="${fn:length(saleItem.act_remarks)}"/>
				 <a target="#" title="${saleItem.act_remarks}"
					href="${cpath}/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&printerId=0&duplicate=true&saleId=${fn:substring(saleItem.act_remarks,4,remLen)}">
				 <c:out value="${fn:substring(saleItem.act_remarks,0,15)}"/>
				</a>
			</td>
			<td>${saleItem.act_rate}</td>
			<td>${saleItem.act_quantity}</td>
			<td align="right">${saleItem.discount}</td>
			<td align="right">${saleItem.amount}</td>
			<td>${patAmt}</td>
			<td>${saleItem.insurance_claim_amount}</td>
		</tr>
	<c:set var="total" value="${total + patAmt}" />
	<c:set var="visit_type" value="${billDetails.getBill().visitType}" />
	<c:set var="discount" value="${discount + saleItem.discount}" />
	</c:forEach>

</table>

   <fieldset class="fieldSetBorder">
	  <legend class="fieldSetLabel"><insta:ltext key="salesissues.pendingbillscolection.details.pharmacytotals"/></legend>
	    <table width="700px" align="right" class="infotable">
		  <tr>
			<td></td><td></td>
			<td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.billedamount"/>:</td>
			<td class="forminfo">
				<label id="lblTotBilled">${total + discount}</label>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.discounts"/>:</td>
			<td class="forminfo">
				<label id="lblTotDisc">${discount}</label>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.netamount"/>:</td>
			<td class="forminfo">
				<label id="lblTotAmt">${total}</label>
			</td>
		 </tr>
	 <tr>
	 		<td></td><td></td><td></td><td></td>
			<td class="formlabel">
				<insta:ltext key="salesissues.pendingbillscolection.details.netpayments"/>:

			</td>
			<td class="forminfo">
				<c:if test="${not empty billIems}">
					<label id="lblExistingReceipts">${(advance)-(refundAmt)}</label>
				</c:if>
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.totalcredits"/>:</td>
			<td class="forminfo">
				<c:set var="totalCredits" value="${(advance)-(refundAmt)}"/>
				<label id="lblTotalCredits"><c:out value="${(totalCredits) + (total_deposit_set_off)}"/></label>
			</td>
		</tr>
		<tr>
			<td></td><td></td>
			<td></td><td></td>
			<td></td><td></td>
			<td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.patientdue"/>:</td>
			<c:set var="tDue" value="${(total_deposit_set_off + totalCredits - total) >= 0 ? 0 : (total - totalCredits - total_deposit_set_off)}"/>
			<td class="forminfo">
				<label id="lblTotAmtDue">${tDue}</label>
			</td>
		</tr>
	</table>
</fieldset>

<c:set var="totAmtdue"	value="${(total)-(advance)+(refundAmt)}"/>


<c:if test="${total_deposits > 0}">
	<fieldset>
  		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.depositDetails"/></legend>
  		<table class="formtable">
 			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.depositsBalance"/>: </td>
				<td class="forminfo" style="width:210px">
				    <b>${total_deposits - total_deposit_set_off}</b>
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.pendingbillscolection.details.depositsetoffinbill"/>: </td>
				<td class="forminfo" style="width:210px">
				    <b>${total_deposit_set_off}</b>
				</td>				
			</tr>
  		</table>
	</fieldset>
</c:if>

<div id="paymentDetails" style="display: none">
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="salesissues.pendingbillscolection.details.paymentdetails"/>:</legend>

	<c:set var="totNetpayment" value="${(advance)-(refundAmt)}"/>
	<input type="hidden" name="netPay" id="netPay" value="${totNetpayment}">

	<c:set var="isrefund" value="${tDue < 0}"/>
	<insta:billPaymentDetails formName="retailCreditForm" defaultPaymentType="${isrefund ? 'F' : 'A'}"/>

</fieldset>
</div>

<div class="screenActions" style="float:left">
	<c:choose>
		<c:when test="${status eq 'C' || status eq 'X' || status eq 'S'}">
			<a href="${cpath }/pages/stores/PhItemsCreditBill.do?_method=getCreditBillsList&sortOrder=bill_no&sortReverse=true&status=A"> <insta:ltext key="salesissues.pendingbillscolection.details.backtodashboard"/></a>
		</c:when>
		<c:otherwise>
			<button id="save" type="button" accesskey="Y" class="button" onclick="return onSubmit();"><insta:ltext key="salesissues.pendingbillscolection.details.pa"/><b><u><insta:ltext key="salesissues.pendingbillscolection.details.y"/></u></b> &amp; <insta:ltext key="salesissues.pendingbillscolection.details.print"/></button> |
			<a href="${cpath }/pages/stores/PhItemsCreditBill.do?_method=getCreditBillsList&sortOrder=bill_no&sortReverse=true&status=A"> <insta:ltext key="salesissues.pendingbillscolection.details.backtodashboard"/></a>
		</c:otherwise>
	</c:choose>

</div>

<div class="screenActions" style="float:right">
	<insta:selectdb name="printerType" table="printer_definition"
		valuecol="printer_id" displaycol="printer_definition_name"
		value="${bean.map.printer_id}"/>
</div>

</form>
<jsp:include page="/pages/dialogBox/processPaymentDialog.jsp"></jsp:include>
<jsp:include page="/pages/dialogBox/loyaltyDialog.jsp"></jsp:include>
</body>
</html>
