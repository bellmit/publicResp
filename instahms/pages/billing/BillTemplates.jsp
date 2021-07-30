<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<c:set var="billTypeDisplay">
	<c:choose>
		<c:when test="${billbean.map.bill_type == 'P' && billbean.map.restriction_type == 'P'}"><insta:ltext key="billing.billtemplates.details.billnow_ph"/></c:when>
		<c:when test="${billbean.map.bill_type == 'C' && billbean.map.restriction_type == 'P'}"><insta:ltext key="billing.billtemplates.details.billlater_ph"/></c:when>
		<c:when test="${billbean.map.bill_type == 'P'}"><insta:ltext key="billing.billtemplates.details.billnow"/></c:when>
		<c:when test="${billbean.map.bill_type == 'C'}"><insta:ltext key="billing.billtemplates.details.billlater"/></c:when>
		<c:otherwise><insta:ltext key="billing.billtemplates.details.other"/></c:otherwise>
	</c:choose>
</c:set>
<c:set var="isInsuranceBill" value="${billbean.map.is_tpa && (billbean.map.restriction_type == 'N' || billbean.map.restriction_type == 'P')}"/>

<html>
<head>
	<title><insta:ltext key="billing.billtemplates.details.billprinttemplates.instahms"/></title>
	<script>
		var userNameInBillPrint = '${genPrefs.userNameInBillPrint}';
		var cpath = '${cpath}';

		function billPrint() {
			var href = '';
			href += cpath;
			href +='/pages/Enquiry/billprint.do?';
			var billNo = document.getElementById('billNo').value;
			var billType = document.getElementById('billType').value;
			var printerType = document.billPrintForm.printType.value;
			var templateName= document.billPrintForm.printBill.value;
			var optionParts  = (templateName).split('-');

			href += "&_method=";

			if (optionParts[0] == 'BILL')
				href += "billPrint";
			else if (optionParts[0] == 'EXPS')
				href += "expenseStatement";
			else if (optionParts[0] == 'PHBI')
				href += "pharmaBreakupBill";
			else if (optionParts[0] == 'PHEX')
				href += "pharmaExpenseStmt";
			else if (optionParts[0] == "CUSTOM")
				href += "billPrintTemplate";
			else	{
				alert("Unknown bill print type: " + optionParts[0]);
				return false;
			}
			href += "&printUserName="+userNameInBillPrint;
			if (optionParts[1])
				href += "&detailed="+optionParts[1];

			if (optionParts[2])
				href += "&option="+optionParts[2];
			href +="&billType="+templateName.substring(parseInt(optionParts[0].length)+1,templateName.length);
			href +="&printerType="+printerType;
			href +="&billNo="+billNo;
			window.open(href);
		}

		function closeWindow() {
			var href = cpath;
			href +='/pages/BillDischarge/BillList.do?_method=getBills&_searchMethod=getBills&_actionId=search_bills&bill_no%40op=ilike';
			href += '&bill_no='+document.getElementById('billNo').value;
			window.location.href = href;
		}
	</script>
</head>
<body>
<form name="billPrintForm">
	<input type="hidden" name="_method" value="">
	<c:set var="templateValues" value="CUSTOM-BUILTIN_HTML,CUSTOM-BUILTIN_TEXT"/>
	<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>
	<c:set var="printValues" value="BILL-DET-ALL,BILL-CHS-ALL,BILL-SUM-ALL,BILL-DET-SNP,BILL-DET-DIA,BILL-DET-OPE,BILL-DET-MED,EXPS-DET-ALL,EXPS-CHS-ALL,EXPS-SUM-ALL,EXPS-DET-MED,PHBI,PHEX"/>
	<c:set var="printTexts" value="Bill - Detailed,Bill - Charge Summary,Bill - Group Summary,Bill Extract - Services,Bill Extract - Diagnostics,Bill Extract - Operations,Bill Extract - Pharmacy,Visit Statement- Detailed,Visit Statement - Charge Summary,Visit Statement - Group Summary,Visit Statement - Pharmacy Extract,,Pharmacy Breakup,Pharmacy Expense Statement"/>
	<c:if test="${not empty templateList}">
			<c:forEach var="temp" items="${templateList}">
				<c:set var="templateValues" value="${templateValues},CUSTOM-${temp.map.template_name}"/>
				<c:set var="templateTexts" value="${templateTexts},${temp.map.template_name}"/>
			</c:forEach>
	</c:if>
	<c:if test="${isInsuranceBill}">
		<c:set var="templateValues" value="${templateValues},BILL-INS-ALL,BILL-PAT-ALL"/>
		<c:set var="templateTexts" value="${templateTexts},Bill - Claim Amounts,Bill - Patient Amounts"/>
	</c:if>
	<c:if test="${billbean.map.bill_type == 'C'}">
		<c:set var="templateValues" value="${printValues},${templateValues}"/>
		<c:set var="templateTexts" value="${printTexts},${templateTexts}"/>
	</c:if>
	<h1><insta:ltext key="billing.billtemplates.details.billprinttemplates"/></h1>
	<c:if test="${not (billbean.map.visit_type == 'r' || billbean.map.visit_type == 't')}">
		<insta:patientdetails visitid="${billbean.map.visit_id}" />
	</c:if>
	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="billing.billtemplates.details.billdetails"/></legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
		<%-- Information row: Bill no (type), Open date (by), Billing Bed Type --%>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.billno.type.in.brackets"/>:</td>
			<td class="forminfo">${billbean.map.bill_no} (${billTypeDisplay})
				<input type="hidden" name="billNo" id="billNo" value="${billbean.map.bill_no}">
				<input type="hidden" name="billType" id="billType" value="${billbean.map.bill_type}">
				<input type="hidden" name="visitId" id="visitId" value="${billbean.map.visit_id}">
			</td>

			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.opendate.by.in.brackets"/>:</td>
			<td class="forminfo">
				<fmt:formatDate value="${billbean.map.open_date}" pattern="dd-MM-yyyy"/>(${billbean.map.opened_by})
			</td>

			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.billstatus"/>:</td>
			<td class="forminfo">${statusDisplay[billbean.map.status]}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.paymentstatus"/>:</td>
			<td class="forminfo">${paymentStatusDisplay[billbean.map.payment_status]}</td>
			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.remarks"/>:</td>
			<td class="forminfo">${billbean.map.remarks}</td>
			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.discountsauth"/>:</td>
			<td class="forminfo">${billbean.map.discount_auth}</td>
		</tr>

		<%-- One row for: Remarks, Discount Auth, Finalized Date --%>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.billtemplates.details.finalizeddate"/>:</td>
			<td class="forminfo">
				<fmt:formatDate value="${billbean.map.finalized_date}" pattern="dd-MM-yyyy"/>
				<fmt:formatDate value="${billbean.map.finalized_date}" pattern="HH:mm"/>
			</td>
		</tr>
	</table>
</fieldset>
<table class="formtable">
	<tr>
		<td class="formlabel"></td>
		<td class="forminfo"></td>
		<td class="formlabel"><insta:ltext key="billing.billtemplates.details.template"/>:</td>
		<td class="forminfo">
			<insta:selectoptions name="printBill" id="printSelect"
				opvalues="${templateValues}" optexts="${templateTexts}"
				value="${billbean.map.bill_type eq 'C' ? genPrefs.billLaterPrintDefault : genPrefs.billNowPrintDefault}"/>
		</td>
		<td class="formlabel"><insta:ltext key="billing.billtemplates.details.printerdefinition"/>:</td>
		<td class="forminfo">
			<insta:selectdb name="printType" table="printer_definition"
				valuecol="printer_id"  displaycol="printer_definition_name"
				value="${billbean.map.bill_type eq 'C' ? genPrefs.default_printer_for_bill_later : genPrefs.default_printer_for_bill_now}"/>
		</td>
	</tr>
</table>
	<div class="screenActions" style="float: left">
	<div class="screenActions" style="float: left">
		<table cellpadding="0" cellspacing="0"  border="0" width="100%">
			<tr>
				<td align="left">
					<input type="button" value="Print" onclick="return billPrint();"/>
				</td>
				<td>&nbsp;</td>
				<td>|</td>
				<td><input type="button" value="Close" onclick="return closeWindow();"/></td>
			</tr>
		</table>
	</div>
</div>
</form>

</body>
</html>

