<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>View/Edit Sponsor Bill - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="billing/editsponsorclaim.js"/>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<script>

var currDate = '<fmt:formatDate value="${currentDate}" pattern="dd-MM-yyyy"/>';
var clmDate = '<fmt:formatDate value="${sbilldetails.map.claim_date}" pattern="dd-MM-yyyy"/>';
var sntDate = '<fmt:formatDate value="${sbilldetails.map.sent_date}" pattern="dd-MM-yyyy"/>';
var clsdDate = '<fmt:formatDate value="${sbilldetails.map.closed_date}" pattern="dd-MM-yyyy"/>';

var origStatus = '${sbilldetails.map.status}';
var cancelBillRights = '${actionRightsMap.cancel_bill}';
var roleId = '${roleId}';
</script>
</head>

<body onload="init();">

<form name="sponsorBillForm" action="editSponsorBill.do">
	<input type="hidden" name="_method" value="show">
	<table width="100%">
		<tr>
			<td width="100%"><h1> View/Edit Sponsor Bill</h1></td>
			<td>Sponsor&nbsp;Bill&nbsp;No:&nbsp;</td>
			<td><input type="text" name="sponsor_bill_no" id="sponsor_bill_no" style="width: 80px"></td>
			<td><input name="getDetails" type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel/>

<form action="editSponsorBill.do" method="POST" name="billsForm">
	<input type="hidden" name="_method" value="update"/>
	<input type="hidden" name="sponsor_bill_no" value="${sbilldetails.map.sponsor_bill_no}"/>
	<input type="hidden" name="totalClaimAmt" value="${sbilldetails.map.claim_amt}"/>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">
			<c:choose>
				<c:when test="${sbilldetails.map.sponsor_type eq 'S'}">TPA</c:when>
				<c:otherwise>Other Hospital</c:otherwise>
			</c:choose>
		</legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
			<tr>
				<td class="formlabel">Sponsor Bill No:</td>
				<td class="forminfo">${sbilldetails.map.sponsor_bill_no}</td>
				<td class="formlabel">Sponsor Name:</td>
				<td class="forminfo">${sponsor_name}</td>
				<td class="formlabel">Status:</td>
				<td class="forminfo">
					<select name="status" class="dropdown" onchange="onChangeStatus()"></select>
				</td>
				<td class="formlabel">Remarks:</td>
				<td class="forminfo"><input type="text" name="remarks" value="${sbilldetails.map.remarks}"/></td>
			</tr>
			<tr>
				<td class="formlabel">Claim Amount:</td>
				<td class="forminfo">${sbilldetails.map.claim_amt}</td>
				<td class="formlabel">Received Amount:</td>
				<td class="forminfo">${amtReceivedFromSponsor}</td>
				<td class="formlabel">TDS:</td>
				<td class="forminfo">${tdsReceivedFromSponsor}</td>
				<td class="formlabel">Cancel Reason:</td>
				<td class="forminfo"><input type="text" name="cancel_reason" value="${sbilldetails.map.cancel_reason}"/></td>
			</tr>
			<tr>
				<fmt:formatDate var="dateVal" value="${serverNow}" pattern="dd-MM-yyyy"/>
				<c:choose>
				<c:when test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
					<td class="formlabel">Claim Date:</td>
					<td class="forminfo">
						<insta:datewidget name="claim_date" valid="past"
						valueDate="${not empty sbilldetails.map.claim_date?sbilldetails.map.claim_date:serverNow}" />
					</td>
					<td class="formlabel">Sent Date:</td>
					<td class="forminfo">
						<insta:datewidget name="sent_date" valid="past"
						valueDate="${not empty sbilldetails.map.sent_date?sbilldetails.map.sent_date:''}"/>
					</td>
					<td class="formlabel">Closed Date:</td>
					<td class="forminfo">
						<insta:datewidget name="closed_date" valid="past"
						valueDate="${not empty sbilldetails.map.closed_date?sbilldetails.map.closed_date:''}"/>
					</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel">Claim Date:</td>
					<td class="forminfo"><label id="claimDt"><fmt:formatDate value="${not empty sbilldetails.map.claim_date?sbilldetails.map.claim_date:serverNow}" pattern="dd-MM-yyyy"/></label></td>
					<td class="formlabel">Sent Date:</td>
					<td class="forminfo"><label id="sentDt"><fmt:formatDate value="${not empty sbilldetails.map.sent_date?sbilldetails.map.sent_date:''}" pattern="dd-MM-yyyy"/></label></td>
					<td class="formlabel">Closed Date:</td>
					<td class="forminfo"><label id="closeDt"><fmt:formatDate value="${not empty sbilldetails.map.closed_date?sbilldetails.map.closed_date:''}" pattern="dd-MM-yyyy"/></label></td>
				</c:otherwise>
				</c:choose>
			</tr>
		</table>
	</fieldset>

	<div class="detailList" style="margin: 10px 0px 5px 0px;">
		<table class="detailList" cellspacing="0" cellpadding="0" id="billListTab" border="0">
			<tr>
				<th>Bill No</th>
				<th>Mr No</th>
				<th>Visit Id</th>
				<th>Patient Name</th>
				<th>Bill Open Date</th>
				<th>Bill Finalized Date</th>
				<th class="number">Bill Claim Amount</th>
				<th class="number">Allocated Amount</th>
				<th style="width:20px"></th>
			</tr>
			<c:forEach var="bill" items="${billList}" varStatus="status">
				<c:set var="i" value="${status.index}"/>
				<tr id="billRow${i}">
					<td>
						<img src="${cpath}/images/empty_flag.gif"/>
						${bill.map.bill_no}
						<input type="hidden" name="billNo" id="billNo${i}" value="${bill.map.bill_no}"/>
					</td>
					<td>${bill.map.mr_no}</td>
					<td>${bill.map.visit_id}</td>
					<td>${bill.map.salutation} ${bill.map.patient_name} ${bill.map.last_name}</td>
					<td><fmt:formatDate value="${bill.map.open_date}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${bill.map.finalized_date}" pattern="dd-MM-yyyy"/></td>
					<td class="number">${bill.map.total_claim}</td>
					<td class="number">${bill.map.claim_recd_amount}</td>

					<td>
						<c:choose>
							<c:when test="${sbilldetails.map.status == 'O'}">
								<a href="javascript:Cancel Bill" onclick="return cancelBill(this,'${i}');" title="Cancel Bill">
								<img src="${cpath}/icons/Delete.png" class="imgDelete button"/>
								</a>
							</c:when>
							<c:otherwise>
								<a href="javascript:Cancel Bill" title="Cancel Bill">
								<img src="${cpath}/icons/Delete1.png" class="imgDelete button"/>
								</a>
							</c:otherwise>
						</c:choose>

						<input type="hidden" name="billSelect" id="billSelect${i}" value="${bill.map.total_claim}"/>
						<input type="hidden" name="billDelete" id="billDelete${i}" value="false"/>
					</td>
				</tr>
			</c:forEach>

		</table>
	</div>

	<div class="screenActions" style="float:left">
		<button type="button" accesskey="S" name="save" class="button" onclick="return validateBillsForm();"/>
		<label><u><b>S</b></u>ave</label></button>&nbsp;
		<label>|</label>
		<a href="./SponsorBillList.do?_method=getSponsorBills&status=O&status=S&status=R">Sponsor Bill List</a>
	</div>

	<div class="screenActions" style="float:right">
	<insta:selectdb name="printerType" table="printer_definition"
			valuecol="printer_id"  displaycol="printer_definition_name"	value="${pref.map.printer_id}"/>
		<button name="print" type="button" accesskey="P"  class="button"onclick="return printConsolidatedBill();"/>
		<label><b><u>P</u></b>rint</label></button>&nbsp;
	</div>

</form>
</body>
</html>
