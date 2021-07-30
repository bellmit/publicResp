<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="S" value="Settled"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

<title>Receive/Allocate Amounts - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="billing/allocateclaim.js"/>
<insta:link type="js" file="paymentCommon.js"/>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<script>

var currDate = '<fmt:formatDate value="${currentDate}" pattern="dd-MM-yyyy"/>';
var clmDate = '<fmt:formatDate value="${sbilldetails.map.claim_date}" pattern="dd-MM-yyyy"/>';
var sntDate = '<fmt:formatDate value="${sbilldetails.map.sent_date}" pattern="dd-MM-yyyy"/>';
var clsdDate = '<fmt:formatDate value="${sbilldetails.map.closed_date}" pattern="dd-MM-yyyy"/>';

var origStatus = '${sbilldetails.map.status}';
var sReceiptNo = '${sponsor_receipt_no}';
var cancelBillRights = '${actionRightsMap.cancel_bill}';
var roleId = '${roleId}';
var writeOffAmountRights = '${actionRightsMap.allow_writeoff}';

</script>
</head>

<body onload="init();">

<form name="sponsorBillForm" action="allocateSponsorBill.do">
	<input type="hidden" name="_method" value="view">
	<table width="100%">
		<tr>
			<td width="100%"><h1> Receive/Allocate Sponsor Bill Amounts </h1></td>
			<td>Sponsor&nbsp;Bill&nbsp;No:&nbsp;</td>
			<td><input type="text" name="sponsor_bill_no" id="sponsor_bill_no" style="width: 80px"></td>
			<td><input name="getDetails" type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel/>

<form action="allocateSponsorBill.do" method="POST" name="allocateClaimForm">
	<input type="hidden" name="_method" value="allocate"/>
	<input type="hidden" name="sponsor_bill_no" value="${sbilldetails.map.sponsor_bill_no}"/>
	<input type="hidden" name="totalAmtReceived" value="${amtReceivedFromSponsor}"/>
	<input type="hidden" name="totalTdsReceived" value="${tdsReceivedFromSponsor}"/>
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
					<c:choose>
						<c:when test="${sbilldetails.map.status != 'C'}">
							<select name="status" class="dropdown" onchange="onChangeStatus()"></select>
						</c:when>
						<c:otherwise>
							<input type="hidden" value="${sbilldetails.map.status}" name="status">
							Closed
						</c:otherwise>
					</c:choose>
				</td>
				<td class="formlabel">Remarks:</td>
				<td class="forminfo">
					<input type="text" name="remarks" value="${sbilldetails.map.remarks}"/>
					<input type="hidden" name="oldRemarks" value="${sbilldetails.map.remarks}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Claim Amount:</td>
				<td class="forminfo">${sbilldetails.map.claim_amt}</td>
				<td class="formlabel">Received Amount:</td>
				<td class="forminfo">${amtReceivedFromSponsor}</td>
				<td class="formlabel">TDS:</td>
				<td class="forminfo">${tdsReceivedFromSponsor}</td>
				<td class="formlabel">Balance Due:</td>
				<td class="forminfo">${sbilldetails.map.claim_amt - (amtReceivedFromSponsor + tdsReceivedFromSponsor)}</td>
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
				<td class="formlabel">Cancel Reason:</td>
				<td class="forminfo"><input type="text" name="cancel_reason" value="${sbilldetails.map.cancel_reason}"/></td>
			</tr>
		</table>
	</fieldset>


<c:if test="${billingcounterId != null && billingcounterId != ''}">
<c:if test="${(sbilldetails.map.status == 'S') || (sbilldetails.map.status == 'R')}">

<dl class="accordion" style="margin-bottom: 10px;">
	<dt>
		<span>Payments</span>
		<div class="clrboth"></div>
	</dt>
	<dd id="payDD" class="open">
		<div class="bd">
			<table id="paymentsTable" class="formtable">
				<tr>
					<td class="formlabel">Date/Time:</td>
					<fmt:formatDate var="dateVal" value="${serverNow}" pattern="dd-MM-yyyy"/>
					<fmt:formatDate var="timeVal" value="${serverNow}" pattern="HH:mm"/>
					<c:choose>
						<c:when test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
							<td>
								<insta:datewidget name="paymentDate" value="${dateVal}" valid="past" btnPos="left" />
								<input type="text" name="paymentTime" class="timefield" value="${timeVal}"/>
							</td>
						</c:when>
						<c:otherwise>
							<td class="forminfo">${dateVal} ${timeVal}
						</c:otherwise>
					</c:choose>
					<td class="formlabel">Counter:</td>
					<td class="forminfo">
						<b><c:out value="${billingcounterName}"/></b>
						<input type="hidden" name="counterId" id="counterId" value="${billingcounterId}" >
						<input type="hidden" name="counterName" id="counterName" value="${billingcounterName}" >
					</td>
				</tr>

				<tr>
					<td class="formlabel">Pay:</td>
					<td class="forminfo">
						<input type="text" name="totPayingAmt" style="text-align:right"
						onkeypress="return enterNumOnly(event);" onblur="return setMaxAmt();"
						value="">
					</td>
					<td class="formlabel">TDS:</td>
					<td class="forminfo">
						<input type="text" name="totTdsAmt" style="text-align:right"
						onkeypress="return enterNumOnly(event);" onblur="return validateTdsAmt();"
						value="">
					</td>
				</tr>

				<insta:paymentdetails/>

			</table>
		</div>
	</dd>
</dl>
</c:if>
</c:if>

	<div class="detailList" style="margin: 10px 0px 5px 0px;">
		<table class="detailList" cellspacing="0" cellpadding="0" id="billListTab" border="0">
			<tr>
				<th>Bill No</th>
				<th>Mr No</th>
				<th>Visit Id</th>
				<th>Patient Name</th>
				<th>Open Date</th>
				<th>Finalized Date</th>
				<th>Remarks</th>
				<th class="number">Claim Amt</th>
				<th class="number">Recvd. Amt</th>
				<th class="number">Allocated Amt</th>
				<th class="number">Bill Status</th>
				<th style="width:20px"></th>
			</tr>
			<c:set var="closedBillsTotal" value="0"/>
			<c:forEach var="bill" items="${billList}" varStatus="status">
				<c:set var="i" value="${status.index}"/>
				<tr id="billRow${i}">
					<td>
						<img src="${cpath}/images/empty_flag.gif"/>
						${bill.map.bill_no}
						<input type="hidden" name="billNo" id="billNo${i}" value="${bill.map.bill_no}"/>
						<input type="hidden" name="billRemarks" id="billRemarks${i}" value="${bill.map.remarks}"/>
						<input type="hidden" name="claimAmt" id="claimAmt${i}" value="${bill.map.total_claim}"/>
						<input type="hidden" name="allocatedAmt" id="allocatedAmt${i}" value="${bill.map.claim_recd_amount}"/>
						<input type="hidden" name="closeBill" id="closeBill${i}" value="${bill.map.status}"/>
						<input type="hidden" name="oldBillStatus" id="oldBillStatus${i}" value="${bill.map.status}"/>
						<input type="hidden" name="oldBillStatusText" id="oldBillStatusText${i}" value="${statusDisplay[bill.map.status]}"/>
					</td>
					<td>${bill.map.mr_no}</td>
					<td>${bill.map.visit_id}</td>
					<td>${bill.map.salutation} ${bill.map.patient_name} ${bill.map.last_name}</td>
					<td><fmt:formatDate value="${bill.map.open_date}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${bill.map.finalized_date}" pattern="dd-MM-yyyy"/></td>
					<td title="${bill.map.remarks}">
						${fn:substring(bill.map.remarks,0,20)}
					</td>
					<td class="number">${bill.map.total_claim}</td>
					<td class="number">${bill.map.claim_recd_amount}</td>
					<td class="number">
						${bill.map.claim_recd_amount}
						<c:if test="${bill.map.status == 'C'}">
							<c:set var="closedBillsTotal" value="${closedBillsTotal + bill.map.claim_recd_amount}"/>
						</c:if>
					</td>
					<td class="number">
						${statusDisplay[bill.map.status]}
					</td>
					<td>
						<c:choose>
							<c:when test="${(sbilldetails.map.status == 'S' || sbilldetails.map.status == 'R') && bill.map.status != 'C'}">
								<a href="javascript:Edit" onclick="return showEditAmountDialog('${i}');" title="Edit Allocated Amt">
								<img src="${cpath}/icons/Edit.png" class="button" id="editBtn${i}"/>
								</a>
							</c:when>
							<c:otherwise>
								<a href="javascript:Edit" title="Edit Allocated Amt">
								<img src="${cpath}/icons/Edit1.png" class="button"/>
								</a>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:forEach>
				<tr class="footer">
					<td>Allocate Max:</td>
					<td class="number"><b><label id="maxAmt">${amtReceivedFromSponsor + tdsReceivedFromSponsor}</label></b></td>
					<td>amt to all bills</td>
					<td>
						<button name="allocate" type="button" accesskey="A" class="button"onclick="return validateAndAllocateMaxAmt();"
						<c:if test="${sbilldetails.map.status != 'S' && sbilldetails.map.status != 'R'}">disabled</c:if> >
						<u><b>A</b></u>pply</button>
					</td>
					<td><input type="checkbox" name="closeAll" value="Yes" onclick="return closeAllBills();"
						<c:if test="${sbilldetails.map.status != 'S' && sbilldetails.map.status != 'R'}">disabled</c:if>/> Close bills </td>
					<td></td>
					<td colspan="3" class="number">Total Amt Allocated:</td>
					<td class="number"><b><label id="recdAmt"></label></b></td>
					<td></td>
					<td></td>
				</tr>
				<tr class="footer">
					<td>Total:</td>
					<td class="number" align="left"><b><label id="closedBillsAmt"><fmt:formatNumber value="${closedBillsTotal}" pattern="0.00"/></label></b></td>
					<td colspan="3">amt allocated to closed bills</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
				</tr>
		</table>
	</div>

	<div class="screenActions" style="float:left">
		<button type="button" accesskey="S" name="save" class="button" onclick="return validateBillsForm();">
			<u><b>S</b></u>ave</button>&nbsp;
		<label>|</label>
		<a href="./SponsorBillList.do?_method=getSponsorBills&status=O&status=S&status=R">Sponsor Bill List</a>
	</div>

	<div class="screenActions" style="float:right">
	<insta:selectdb name="printerType" table="printer_definition"
			valuecol="printer_id"  displaycol="printer_definition_name"	value="${pref.map.printer_id}"/>
		<c:if test="${ not empty sponsor_receipt_no}">
		<button name="print" type="button" accesskey="P" class="button"onclick="return printReceipt();"/>
			<u><b>P</b></u>rint</button>&nbsp;
		</c:if>
	</div>

</form>


<form name="editBillForm" onsubmit="return false;">
<input type="hidden" id="editRowId" value=""/>
<div id="editAmountDialog" style="display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Allocate Bill Amount</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Mr&nbsp;No:</td>
				<td class="forminfo"><label id="eMrNo"></label>	</td>
				<td class="formlabel">Patient&nbsp;Name:</td>
				<td class="forminfo"><label id="ePatientName"></label></td>
				<td class="formlabel">Visit&nbsp;Id:</td>
				<td class="forminfo"><label id="eVisitId"></label></td>
			</tr>
			<tr>
				<td class="formlabel">Bill&nbsp;No:</td>
				<td class="forminfo"><label id="eBillNo"></label></td>
				<td class="formlabel">Bill&nbsp;Claim&nbsp;Amt:</td>
				<td class="forminfo"><label id="eBillClaimAmt"></label></td>
				<td class="formlabel">Bill&nbsp;Finalized&nbsp;Date:</td>
				<td class="forminfo"><label id="eFinalizedDate"></label></td>
			</tr>
			<tr>
				<td class="formlabel">Balance&nbsp;Due:</td>
				<td class="forminfo"><label id="eBalanceDue"></label></td>
				<td class="formlabel">Allocated&nbsp;Amt:</td>
				<td class="forminfo">
					<input type="text" name="eAllocatedAmt" onkeypress="return enterNumOnly(event);" style="text-align:right"/>
				</td>
				<td class="formlabel">Remarks:</td>
				<td colspan="3"><input type="text" name="eRemarks" maxlength="200" size="100" /></td>
			</tr>
			<tr>
				<td class="formlabel">
					Close&nbsp;Bill
				</td>
				<td class="forminfo">
					<input title="Close Bill" type="checkbox" name="eCloseBill"/>
				</td>
			</tr>
		</table>
		</fieldset>
		<table>
			<tr>
				<td><input type="button" onclick="onEditSubmit()" value="OK" /></td>
				<td><input type="button" onclick="onEditCancel()" value="Cancel"/></td>
			</tr>
		</table>
	</div>
</div>
</form>

</body>
</html>
