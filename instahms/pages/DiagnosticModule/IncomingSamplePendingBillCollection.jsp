<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="existingReceipts" value="${billDetails.bill.totalReceipts}"/>
<c:set var="no_of_credit_debit_card_digits" value='<%=GenericPreferencesDAO.getAllPrefs().get("no_of_credit_debit_card_digits") %>'/>

<html>
<head>
<title><insta:ltext key="laboratory.samplepayment.collection.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="diagnostics/incomingCollection.js" />
<insta:link type="script" file="billPaymentCommon.js" />

<style type="text/css">
	.status_X { background-color: #F2DCDC }

	table.infotable td.formlabel { width: 150px; text-align: right;}
	table.infotable td.forminfo { width: 60px; text-align: right;  padding-right: 5px;}
</style>

<script type="text/javascript">

var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
var roleId = '${roleId}';
var writeOffAmountRights = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_writeoff}';
var allowBackDate = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_backdate}';
var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};

</script>
<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
</head>

<body onload="resetPayments();ajaxForPrintUrls();">
<c:choose>
	<c:when test="${param.category == 'DEP_LAB'}">
        	<c:set var="Url" value="IncomingSamplePendingBill.do"/>
	</c:when>
	<c:otherwise>
        	<c:set var="Url" value="IncomingSamplePendingBillRad.do"/>
	</c:otherwise>
</c:choose>

<form method="POST" action="${Url}" name="IncomingPendingBillForm" >

<input type="hidden" value="collectSampleBillPayments" name="_method"/>
<input type="hidden" name="billNo" value="${patientDetails.map.billno}">
<input type="hidden" name="billType" value="${patientDetails.map.bill_type}" />
<input type="hidden" name="billStatus" value="${patientDetails.map.status}" />
<input type="hidden" name="patientId" value="${patientDetails.map.incoming_visit_id}">
<input type="hidden" name="action"/>
<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>

<div class="pageHeader"><insta:ltext key="laboratory.samplepayment.collection.incomingsamplepayment"/></div>

<div><insta:feedback-panel/></div>

<fieldset class="fieldSetBorder">
	<legend	class="fieldSetLabel"><insta:ltext key="laboratory.samplepayment.collection.paymentdetails"/></legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
		<tr>
		    <td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.billno"/>: </td>
		    <td class="forminfo">${patientDetails.map.billno}</td>
		    <td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.billtype"/>:</td>
		    <td class="forminfo">
	    		<c:if test="${patientDetails.map.bill_type eq 'C'}"><insta:ltext key="laboratory.samplepayment.collection.billlater"/></c:if>
	    		<c:if test="${patientDetails.map.bill_type eq 'P'}"><insta:ltext key="laboratory.samplepayment.collection.billnow"/></c:if>
		    </td>
			<td></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
			<td class="forminfo">${patientDetails.map.patient_name }</td>
			<td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.age.gender"/>:</td>
			<td class="forminfo">${patientDetails.map.patient_age}${fn:toLowerCase(patientDetails.map.age_unit)} / ${patientDetails.map.patient_gender}</td>
			<td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.hospitalname"/>:</td>
			<td class="forminfo">${patientDetails.map.hospital_name}</td>
		</tr>
	</table>
</fieldset>

<c:set var="total" value="0" />
<c:set var="advance" value="0" />
<c:set var="refundAmt" value="0" />
<c:set var="totAmtdue" value="0" />
<c:set var="discount" value="0" />

<div class="resultList" style="margin: 10px 0px 5px 0px;">
<table class="detailList" width="100%" cellspacing="0" cellpadding="0">
	<tr>
		<th><insta:ltext key="laboratory.samplepayment.collection.testname"/></th>
		<th><insta:ltext key="laboratory.samplepayment.collection.qty"/></th>
		<c:if test="${param.category eq 'DEP_LAB'}">
		<th><insta:ltext key="laboratory.samplepayment.collection.sampleno"/></th>
		</c:if>
		<th style="text-align: right"><insta:ltext key="laboratory.samplepayment.collection.amount"/></th>
	</tr>

	<c:forEach items="${billDetails.receipts}" var="receipt">
		<c:set var="advance" value="${advance + receipt.amount}" />
	</c:forEach>
	<c:forEach items="${billDetails.refunds}" var="refund">
		<c:set var="refundAmt" value="${(refundAmt) - (refund.amount)}" />
	</c:forEach>

	<c:forEach items="${sampleDetails}" var="sampleBean" varStatus="st">
		<c:set var="sample" value="${sampleBean.map}" />
		<tr class="${st.first ? 'firstRow' : ''}">

			<td><img src="${cpath}/images/red_flag.gif" style="display: ${sample.status == 'X' ? 'block' : 'none'};float:left"/>
				${sample.test_name}
			</td>
			<td>${sample.act_quantity}</td>
			<c:if test="${param.category eq 'DEP_LAB'}">
			<td>${sample.orig_sample_no}</td>
			</c:if>
			<td style="text-align: right;">${sample.amount}</td>

		</tr>
		<c:choose>
			<c:when test="${sample.status == 'X'}">
			</c:when>
			<c:otherwise>
				<c:set var="total" value="${total + sample.amount}" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
</table>

<table align="right" class="infotable" width="100%">
	<tr>
		<c:if test="${discount > 0}">
			<td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.discount"/>:</td>
			<td class="forminfo">${discount}</td>
		</c:if>
		<td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.total"/>:</td>
		<td class="forminfo"><label id="totalLable">${total}</label></td>
	</tr>
</table>

<c:set var="total" value="${total - discount}" />
<c:set var="totAmtdue"	value="${(total)-(advance)+(refundAmt)}"/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="laboratory.samplepayment.collection.paymentdetails"/>:</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">

		<tr>
			<td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.netpayments"/>:</td>
			<td><input type="text" name="netPay" id="netPay"
				maxlength="50" class="number6dig" value="${ifn:afmt((advance)-(refundAmt))}"
				readonly="readonly"></td>
			<td class="formlabel"><insta:ltext key="laboratory.samplepayment.collection.amountdue"/>:</td>
			<td><input type="text" name="amountdue" id="amountdue"
				maxlength="50" class="number6dig" value="${totAmtdue}"
				readonly="readonly"></td>

			<c:if test="${patientDetails.map.bill_type == 'C'}">
				<td class="formlabel" id="closeAcc"><insta:ltext key="laboratory.samplepayment.collection.closeaccount"/>:</td>
				<td class="forminfo"><input type="checkbox" name="close" id="close"></td>
			</c:if>
			<c:if test="${patientDetails.map.bill_type == 'P'}">
				<td class="formlabel"></td>
				<td class="forminfo"><input type="checkbox" name="close" id="close" style="display:none"></td>
			</c:if>

		</tr>

		<tr>
			<td class="formlabel"> <insta:ltext key="laboratory.samplepayment.collection.billremarks"/>: </td>
			<td colspan="3">
				<input type="text" name="billRemarks" id="billRemarks" size="30" style="width:455px" 
					value="${ifn:cleanHtmlAttribute(billDetails.bill.billRemarks)}"/>
				<input type="hidden" name="oldRemarks" id="oldRemarks" 
					value="${(billDetails.bill.billRemarks)}"/>
			</td>
			<td></td>
			<td></td>
		</tr>

	</table>

		<c:set var="isrefund" value="${totAmtdue < 0}"/>
		<insta:billPaymentDetails formName="IncomingPendingBillForm" isRefundPayment="${isrefund}"/>

</fieldset>

<div class="screenActions" style="float: left">
	<c:choose>
		<c:when test="${patientDetails.map.status != 'C'}">
			<c:choose>
				<c:when test="${patientDetails.map.bill_type == 'C'}">
					<button id="btnaction" name="btnaction" type="button" accesskey="S" class="button" onclick="return validate('Save')">
					<b><u><insta:ltext key="laboratory.samplepayment.collection.s"/></u></b><insta:ltext key="laboratory.samplepayment.collection.ave"/></button>
				</c:when>
				<c:otherwise>
					<button id="btnaction" name="btnaction" type="button" accesskey="C" class="button" onclick="return validate('Pay')">
					<insta:ltext key="laboratory.samplepayment.collection.pay"/> &amp; <b><u><insta:ltext key="laboratory.samplepayment.collection.c"/></u></b><insta:ltext key="laboratory.samplepayment.collection.lose"/></button>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<button name="btnaction" type="button" accesskey="R" class="button" onclick="return validate('Reopen')"><b><u><insta:ltext key="laboratory.samplepayment.collection.r"/></u></b><insta:ltext key="laboratory.samplepayment.collection.eopen"/></button>
		</c:otherwise>
	</c:choose>
</div>

<div style="float: right; margin-top: 10px">
	<insta:selectdb name="printer" table="printer_definition"
				valuecol="printer_id"  displaycol="printer_definition_name"
				value="${pref.map.printer_id}"/>
	<button name="printaction" type="submit" accesskey="P" onclick="return validate('Print')"><b><u><insta:ltext key="laboratory.samplepayment.collection.p"/></u></b><insta:ltext key="laboratory.samplepayment.collection.rint"/></button>
</div>
<div></div>
<div class="legend" style="margin-top: 15px; margin-right:10px">
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.samplepayment.collection.cancelledtest"/></div>
</div>

</form>
</body>

</html>
