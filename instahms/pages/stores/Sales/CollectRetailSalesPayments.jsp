<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<head>
<title><insta:ltext key="salesissues.rtailcreditpendingbills.list.retailpayments.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<insta:link type="js" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="billPaymentCommon.js" />
<insta:link type="script" file="sockjs.min.js"/>
<insta:link type="script" file="stomp.min.js"/>


<c:set var="no_of_credit_debit_card_digits" value="${genPrefs.no_of_credit_debit_card_digits}"/>
<c:set var="incomeTaxCashLimitApplicability" value="${genPrefs.incomeTaxCashLimitApplicability}"/>
<c:set var="cashTransactionLimitAmt" value="${cashTransactionLimit}"/>

<script type="text/javascript">
var screenId = '${screenId}';
var billNumber = '${billNo}';
var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
var billNumber='${ifn:cleanHtmlAttribute(param.bill_no)}';
var points_redemption_rate = 0;
var ipDepositSetOff = 0;
var generalDepositSetOff = 0;
var availableDeposits = 0;
var ipDeposits = 0;
var availableRewardPoints = 0;
var availableRewardPointsAmount = 0;
var billRewardPoints = 0;
var isMvvPackage  = 0;
var visitType = '';
var hasRewardPointsEligibility = false;
var mrno = null;
var visitId = '${billDetails.bill.visitId}';
var income_tax_cash_limit_applicability = '${incomeTaxCashLimitApplicability}';
var cashTransactionLimitAmt =  ${empty cashTransactionLimitAmt ? 0 : cashTransactionLimitAmt};


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
	if(income_tax_cash_limit_applicability == 'Y') {
		valid = valid && checkCashLimitValidation(mrno,visitId);
	}

	if (!valid) return false;
	enableFormValues();
	document.retailCreditForm.submit();
}

function onPrint() {
	var billno = document.retailCreditForm.billNo.value;
	var customerid = document.retailCreditForm.customerId.value;
	var roundOffValue = document.retailCreditForm.round_off.value;
	window.open(cpath+"/pages/stores/RetailPendingSalesBillPrint.do?_method=printRetailCreditBill"+
		"&billNo="+billno+"&customerId="+customerid+"&round_off="+roundOffValue);
}

function init(){
	document.getElementById("close").checked=false;
	document.getElementById('closeAcc').style.display="table-cell";

	resetPayments();
	resetTotal();
}

function resetTotal() {
	var obj = document.getElementById('c_round_off');
	calculateRoundOff(obj)
}

function calculateRoundOff(obj) {
	var totalAmountDue = 0;
	var totAmtPaise = 0;
	var newRoundOffPaise = 0;
	var roundOff = document.getElementById('l_round_off').textContent;
	var previousRoundedAmt = document.getElementById('p_rounded_amount').value;
	var totalAmtWithoutRoundoff = document.getElementById('total_without_roundoff').value;
	totAmtPaise = getPaise(document.getElementById('c_total').value);
	totalAmountDue = getPaise(document.getElementById('amountdue').value);
	var fixedTotalAmtDue = getPaise(document.getElementById('totalAmountDue').value);

	if(obj.checked) {
		newRoundOffPaise = getRoundOffPaise(totAmtPaise);
		totAmtPaise +=newRoundOffPaise;
		fixedTotalAmtDue+=newRoundOffPaise
		document.getElementById('l_round_off').textContent = (newRoundOffPaise == 0 ? (previousRoundedAmt == 0 ? roundOff : previousRoundedAmt) : formatAmountPaise(newRoundOffPaise));
		document.getElementById('l_total').textContent = formatAmountPaise(totAmtPaise);
		document.getElementById('l_total').style.fontWeight= 'bold';
		document.getElementById('d_amountdue').value = formatAmountPaise(fixedTotalAmtDue);
		document.getElementById('amountdue').value = formatAmountPaise(fixedTotalAmtDue);
		document.getElementById('round_off').value = (newRoundOffPaise == 0 ? (previousRoundedAmt == 0 ? roundOff : previousRoundedAmt) : formatAmountPaise(newRoundOffPaise));
	} else {
		document.getElementById('l_round_off').textContent = formatAmountPaise(0);
		document.getElementById('l_total').textContent = totalAmtWithoutRoundoff;
		document.getElementById('d_amountdue').value = formatAmountPaise(fixedTotalAmtDue);
		document.getElementById('amountdue').value = formatAmountPaise(fixedTotalAmtDue);
		document.getElementById('round_off').value = formatAmountPaise(0);
		document.getElementById('l_total').style.fontWeight= 'bold';
	}
}

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 */
function resetPayments() {
	resetTotalsForPayments();
}

function getTotalAmount() {
	return getPaise(document.getElementById("amountdue").value);
}

function getTotalAmountDue() {
	return getPaise(document.getElementById("amountdue").value);
}

</script>
<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
<insta:js-bundle prefix="billing.salucro"/>
</head>

<body class="yui-skin-sam" onload="init();ajaxForPrintUrls();loadLoyaltyDialog();loadProcessPaymentDialog();filterPaymentModes();">

<form method="GET" action="RetailpendingSalesBill.do" name="retailCreditForm" >

<input type="hidden" value="collectSaleRetailPayments" name="_method" />
<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(billNo)}">
<input type="hidden" name="billType" value="${billType}">
<input type="hidden" name="customerId" value="${ifn:cleanHtmlAttribute(customerId)}">
<input type="hidden" name="doctor" value="${doctor}" />
<input type="hidden" name="chargeId" value="${billChargeBean.map.charge_id}">

<h1><insta:ltext key="salesissues.rtailcreditpendingbills.list.collectretailsalepayments"/></h1>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.retailcustomerdetails"/></legend>

	<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
		<tr>
	    	<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.billno"/>: </td>
	    	<td class="forminfo">${ifn:cleanHtml(param.billno)}</td>
			<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.customername"/>: </td>
			<td class="forminfo">${retailcustomer.customer_name }</td>
			<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.doctorname"/>:</td>
			<td class="forminfo">${doctor}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.contactno"/>: </td>
			<td class="forminfo">${retailcustomer.phone_no}</td>
			<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.creditlimit"/>: </td>
			<td class="forminfo">${retailcustomer.credit_limit }</td>
		</tr>
	</table>
</fieldset></br>

<table class="dataTable" width="100%" align="center" id="saleItemList" cellpadding="0" cellspacing="0">

	<tr>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.saledate"/></th>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.saleid"/></th>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.itemname"/></th>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.qty"/></th>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.manufacturer"/></th>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.batch.or.serial.no"/></th>
		<th><insta:ltext key="salesissues.rtailcreditpendingbills.list.expirydate"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.rtailcreditpendingbills.list.amount"/></th>
	</tr>

	<c:set var="total" value="0" />
	<c:set var="advance" value="0" />
	<c:set var="refundAmt" value="0" />
	<c:set var="totAmtdue" value="0" />
	<c:set var="rounded" value="N"/>

	<c:forEach items="${billDetails.receipts}" var="receipt">
		<c:set var="advance" value="${advance + receipt.amount}" />
	</c:forEach>

	<c:forEach items="${billDetails.refunds}" var="refund">
		<c:set var="refundAmt" value="${(refundAmt) - (refund.amount)}" />
	</c:forEach>

  	<c:set var="discount" value="0"/>
	<c:set var="roundOff" value="0.00"/>
	<c:if test="${(not empty billChargeBean && billChargeBean.map.status != 'X')}">
		<c:set var="roundOff" value="${roundOff+billChargeBean.map.amount}"/>
	</c:if>
	<c:set var="previousSaleId" value=""/>
	<c:forEach items="${saleDetails}" var="saleItemBean">
		<c:set var="saleItem" value="${saleItemBean.map}" />
		<c:set var="currentSaleId" value="${saleItem.sale_id}"/>
		<tr>
			<td><fmt:formatDate value="${saleItem.sale_date}" pattern="dd-MM-yyyy" /></td>
			<td>${saleItem.sale_id}</td>
			<td><insta:truncLabel value="${saleItem.medicine_name}" length="25"/> </td>
			<td>${saleItem.quantity}</td>
			<td>${saleItem.manf_mnemonic}</td>
			<td>${saleItem.batch_no}</td>
			<td><fmt:formatDate value="${saleItem.expiry_date}" pattern="MMM-yyyy" /></td>
			<td style="text-align: right">${saleItem.amount}</td>
		</tr>
		<c:if test="${currentSaleId ne previousSaleId || previousSaleId eq ''}">
			<c:set var="discount" value="${discount+saleItem.bill_discount}"/>
		</c:if>
	<c:set var="previousSaleId" value="${saleItem.sale_id}"/>
	<c:set var="total" value="${total + saleItem.amount}" />
	</c:forEach>

	<c:if test="${discount!=0}">
		<tr>
			<td colspan="6">&nbsp;</td>
			<td style="text-align:right"><insta:ltext key="salesissues.rtailcreditpendingbills.list.billdiscount"/></td>
			<td style="text-align:right">${discount}</td> </tr>
	</c:if>
	<tr>
		<td colspan="5">&nbsp;</td>
		<td style="text-align: right" >
			<c:if test="${not empty billChargeBean && billChargeBean.map.status != 'X'}">
				<c:set var="rounded" value="Y"/>
			</c:if>
			<div style="float: right">
				<input type="checkbox" name="c_round_off" id="c_round_off" align="bottom" onclick="calculateRoundOff(this);" ${rounded == 'Y'? 'checked' : ''}>
			</div>
			<div style="margin-top: 2px; float: right"><insta:ltext key="salesissues.rtailcreditpendingbills.list.roundoff"/></div>
			<input type="hidden" name="round_off" id="round_off" value="${(not empty billChargeBean && billChargeBean.map.status != 'X') ? billChargeBean.map.amount : 0}"/>
			<input type="hidden" name="p_rounded_amount" id="p_rounded_amount" value="${(not empty billChargeBean && billChargeBean.map.status != 'X') ? billChargeBean.map.amount : 0}">
		</td>
		<td style="text-align: right"><insta:ltext key="salesissues.rtailcreditpendingbills.list.roundoff"/></td>
		<td style="text-align: right"><label id="l_round_off">${not empty billChargeBean ? billChargeBean.map.amount : roundOff}</td>
	</tr>

	<input type="hidden" name="total_without_roundoff" id="total_without_roundoff" value="${total-discount}"/>
	<c:set var="total" value="${total - discount + roundOff}" />
		<tr>
			<td colspan="6">&nbsp;</td>
			<td style="text-align: right"><insta:ltext key="salesissues.rtailcreditpendingbills.list.grandtotal"/></td>
			<td style="text-align: right"><label id="l_total"><b>${total}</b></label></td>
			<input type="hidden" name="c_total" id="c_total" value="${total-roundOff}"/>
		</tr>
</table>

<c:set var="totAmtdue"	value="${(total)-(advance)+(refundAmt)-roundOff}"/>

<div id="paymentDetails">
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.paymentdetails"/>:</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="90%" border="0">
		<tr>
			<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.netpayments"/>:</td>
			<td>
				<c:set var="totNetpayment" value="${(advance)-(refundAmt)}"/>
				<input type="text" name="netPay" id="netPay"
				maxlength="50" class="number6dig" value="${totNetpayment}"billremarks
				readonly="readonly">
			</td>
			<td class="formlabel"><insta:ltext key="salesissues.rtailcreditpendingbills.list.amountdue"/>:</td>
			<td><input type="text" name="d_amountdue" id="d_amountdue"
				maxlength="50" class="number6dig" value="${totAmtdue}"
				readonly="readonly">
				<input type="hidden" name="totalAmountDue" id="totalAmountDue" value="${totAmtdue}">
				<input type="hidden" name="amountdue" id="amountdue" value="${totAmtdue}">
			</td>
			<td class="formlabel" id="closeAcc"><insta:ltext key="salesissues.rtailcreditpendingbills.list.closeaccount"/>:</td>
			<td class="forminfo"><input type="checkbox" name="close" id="close"></td>
		</tr>
		<tr>
			<td class="formlabel"> <insta:ltext key="salesissues.rtailcreditpendingbills.list.billremarks"/>: </td>
			<td colspan="3">
				<input type="text" name="billRemarks" id="billRemarks" size="30" style="width:455px" value="${ifn:cleanHtmlAttribute(billDetails.bill.billRemarks)}"/>
				<input type="hidden" name="oldRemarks" id="oldRemarks" value="${ifn:cleanHtmlAttribute(billDetails.bill.billRemarks)}"/>
			</td>
			<td></td>
			<td></td>
		</tr>
	</table>

	<c:set var="isrefund" value="${totAmtdue < 0}"/>
	<insta:billPaymentDetails formName="retailCreditForm" defaultPaymentType="${isrefund ? 'F' : 'A'}" hasRewardPointsEligibility="false"
		 	availableRewardPoints="0" availableRewardPointsAmount="0" origBillStatus=""/>

</fieldset>
</div>

<div class="screenActions">
	<button id="save" type="button" accesskey="Y" class="button" onclick="onSubmit()"><insta:ltext key="salesissues.rtailcreditpendingbills.list.pa"/><b><u><insta:ltext key="salesissues.rtailcreditpendingbills.list.y"/></u></b> &amp; <insta:ltext key="salesissues.rtailcreditpendingbills.list.print"/></button>
	<button type="button" accesskey="P" class="button" onclick="onPrint()" ><b><u><insta:ltext key="salesissues.rtailcreditpendingbills.list.p"/></u></b><insta:ltext key="salesissues.rtailcreditpendingbills.list.rint"/></button>
</div>

</form>
<jsp:include page="/pages/dialogBox/processPaymentDialog.jsp"></jsp:include>
<jsp:include page="/pages/dialogBox/loyaltyDialog.jsp"></jsp:include>
</body>

</html>
