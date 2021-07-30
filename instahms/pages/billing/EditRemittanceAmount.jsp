<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<c:set var="bill" value="${billDetails.bill}"/>
<c:set var="charges" value="${billDetails.charges}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<jsp:useBean id="claimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${claimStatusDisplay}" property="O" value="Open"/>
<c:set target="${claimStatusDisplay}" property="B" value="Batched"/>
<c:set target="${claimStatusDisplay}" property="C" value="Closed"/>
<c:set target="${claimStatusDisplay}" property="M" value="Marked For Resubmission"/>

<c:set var="existingReceipts" value="${bill.totalReceipts}"/>
<c:set var="existingRecdAmount" value="${bill.claimRecdAmount}"/>
<c:set var="existingSponsorReceipts" value="${bill.totalPrimarySponsorReceipts + bill.totalSecondarySponsorReceipts}"/>
<c:set var="billDeposits" value="${bill.depositSetOff}"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>${ifn:cleanHtml(bill.billNo)} <insta:ltext key="billing.editremittanceamount.details.remittanceamount"/></title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="billing.js"/>
<insta:link type="script" file="billing/editBillRemittance.js"/>

<script type="text/javascript">
	var origBillStatus = '${ifn:cleanJavaScript(bill.status)}';
	var billType = '${ifn:cleanJavaScript(bill.billType)}';
	var restrictionType = '${ifn:cleanJavaScript(bill.restrictionType)}';
	var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
	var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
	var existingSponsorReceipts = ${empty existingSponsorReceipts ? 0 : existingSponsorReceipts};
	var billDeposits = ${empty billDeposits ? 0 : billDeposits};
	var roleId = '${roleId}';
	var isTpa       = ${empty bill ? false : bill.is_tpa};
	var billNowTpa  = billType == 'P' && isTpa && (restrictionType == 'N' || restrictionType == 'P');
	var cancelBillRights = '${actionRightsMap.cancel_bill}';
	var allowBackDate = '${actionRightsMap.allow_backdate}';
	var allowBackDateBillActivities = "${roleId == 1 || roleId == 2 ? 'A' :actionRightsMap.allow_back_date_bill_activities}";
	var writeOffAmountRights = '${actionRightsMap.allow_writeoff}';
	var finalizedDate = '<fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/>';
	var finalizedTime = '<fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/>';
	var regDate = '<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>';
	var regTime = '<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>';
	var dischargeType = '${dischargeType}';
	var sponsorBillNo = '${ifn:cleanJavaScript(bill.sponsorBillNo)}';
	var pendingEquipmentFinalization = '${pendingEquipmentFinalization}';
	var pendingBedFinalization = '${pendingBedFinalization}';
	var chargePaymentsJSON = <%= request.getAttribute("chargePaymentsJSON") %>;
	var totInsAmtPaise = 0;
	var totRemAmtPaise = 0;
	var totPatientAmtPaise = 0;
	var totAmtPaise = ${empty bill || empty bill.totalAmount ? 0 : bill.totalAmount};
</script>
<insta:js-bundle prefix="billing.billlist"/>
<insta:js-bundle prefix="billing.billremittance"/>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="initForm();">

<form name="billForm" action="billRemittanceAmount.do">
	<input type="hidden" name="_method" value="getBillRemittance">
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="billing.editremittanceamount.details.editbillremittanceamount"/></h1></td>
			<td><insta:ltext key="billing.editremittanceamount.details.bill"/>&nbsp;<insta:ltext key="billing.editremittanceamount.details.no"/>:&nbsp;</td>
			<td><input type="text" name="billNo" id="billNo" style="width: 80px"></td>
			<td><input type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel/>
<insta:patientdetails visitid="${patient.patient_id}"/>

<form name="mainform" method="post" action="billRemittanceAmount.do">
<input type="hidden" name="_method" value="saveBillRemittance">
<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(bill.billNo)}">

<input type="hidden" name="paymentForceClose" value="N"/>
<input type="hidden" name="claimForceClose" value="N"/>

<%-- bill type display --%>
<c:set var="billTypeDisplay">
	<c:choose>
		<c:when test="${bill.billType == 'P' && bill.restrictionType == 'P'}"><insta:ltext key="billing.editremittanceamount.details.billnow_ph"/></c:when>
		<c:when test="${bill.billType == 'C' && bill.restrictionType == 'P'}"><insta:ltext key="billing.editremittanceamount.details.billlater_ph"/></c:when>
		<c:when test="${bill.billType == 'P'}"><insta:ltext key="billing.editremittanceamount.details.billnow"/></c:when>
		<c:when test="${bill.billType == 'C'}"><insta:ltext key="billing.editremittanceamount.details.billlater"/></c:when>
		<c:otherwise><insta:ltext key="billing.editremittanceamount.details.other"/></c:otherwise>
	</c:choose>
</c:set>

<c:if test="${bill.billType == 'C' && bill.isPrimaryBill == 'N'}">
	<c:set var="billTypeDisplay" value="Sec. ${billTypeDisplay}"/>
</c:if>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"><insta:ltext key="billing.editremittanceamount.details.billdetails"/></legend>
	<table class="formtable" width="100%">
		<%-- Information row: Bill no (type), Open date (by), Finalized Date --%>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.billno.type.in.brackets"/>:</td>
			<td class="forminfo">${ifn:cleanHtml(bill.billNo)} (${ifn:cleanHtml(billTypeDisplay)})
				<input type="hidden" name="billType" value="${ifn:cleanHtmlAttribute(bill.billType)}"/>
			</td>

			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.opendate.by.in.brackets"/>:</td>
			<td class="forminfo">
				<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/> (${ifn:cleanHtml(bill.openedBy)})
			</td>

			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.finalizeddate"/>:</td>
			<td>
				<c:set var="finaldate"><fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/></c:set>
				<c:set var="finaltime"><fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/></c:set>
				<c:choose>
					<c:when test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
						<insta:datewidget name="finalizedDate" value="${finaldate}"
							id="f_date" btnPos="left" readOnly="true"/>
						<input type="text" size="4" name="finalizedTime" readOnly="true"
							value="${finaltime}" class="timefield"/>
					</c:when>
					<c:otherwise>
						<fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/>
						<fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/>
						<input type="hidden" name="finalizedDate" value="${finaldate}"/>
						<input type="hidden" name="finalizedTime" value="${finaltime}"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.billstatus"/>:</td>
			<c:choose>
				<c:when test="${(bill.billType=='P' && !bill.is_tpa) || bill.status == 'C' || bill.status == 'X'}">
					<td class="forminfo">
						<b>${statusDisplay[bill.status]}</b>
						<input type="hidden" name="billStatus" value="${ifn:cleanHtmlAttribute(bill.status)}" />
					</td>
				</c:when>
				<c:otherwise>
					<td>
						<select name="billStatus" id="billStatus" onchange="onChangeBillStatus()" class="dropdown">
						</select>
					</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.paymentstatus"/>:</td>
			<td class="forminfo">
				<c:choose>
					<c:when test="${(bill.billType=='P' && !bill.is_tpa) || bill.status == 'C' || bill.status == 'X'}">
						<b>${paymentStatusDisplay[bill.paymentStatus]}</b>
						<input type="hidden" name="paymentStatus" value="${ifn:cleanHtmlAttribute(bill.paymentStatus)}" />
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="paymentStatus" value="${bill.paymentStatus}"
				 		opvalues="U,P" optexts="Unpaid,Paid" onchange="onChangePaymentStatus()"/>
					</c:otherwise>
				</c:choose>
			</td>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.claimstatus"/>:</td>
			<td class="forminfo">
				<c:if test="${bill.status == 'A'}">
					<c:set var="claimStatusDisabled">disabled="1"</c:set>
				</c:if>
				<%-- <insta:selectoptions name="primaryClaimStatus" value="${bill.primaryClaimStatus}" disabled="${bill.status == 'A'}"
				 opvalues="O,S,R" optexts="Open,Sent,Closed"/> --%>
				 ${claimStatusDisplay[bill.primaryClaimStatus]}
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.remarks"/>:</td>
			<td class="forminfo">
				<input type="hidden" name="oldRemarks" id="oldRemarks" value="${ifn:cleanHtmlAttribute(bill.billRemarks)}"/>
				<input type="text" name="billRemarks" id="billRemarks" value="${ifn:cleanHtmlAttribute(bill.billRemarks)}" maxlength="100" />
			</td>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.approvalamount"/>:</td>
			<td class="forminfo">${bill.approvalAmount}</td>
			<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.patientdeduction"/>:</td>
			<td class="forminfo">${billDetails.bill.insuranceDeduction}</td>
		</tr>
	</table>
</fieldset>

<c:set var="totalRemittanceAmount"  value="0"/>
<c:set var="totalAllocatedAmount"   value="0"/>
<c:set var="totalUnallocatedAmount" value="0"/>
<c:set var="i" value="0"/>

<fieldset class="fieldSetBorder" style="width:820px">
	<legend class="fieldSetLabel"><insta:ltext key="billing.editremittanceamount.details.paymentsummary"/></legend>
	<div class="resultList" style="margin: 10px 0px 5px 0px;width:820px" >
	<table class="detailList" cellspacing="0" cellpadding="0" id="paymentReferencesTable" border="0" style="width:820px">
		<tr>
			<th style="width: 133px"><insta:ltext key="billing.editremittanceamount.details.remittance"/> #</th>
			<th style="width: 133px"><insta:ltext key="billing.editremittanceamount.details.paymentreference"/></th>
			<th style="width: 133px"><insta:ltext key="billing.editremittanceamount.details.date"/></th>
			<th style="width: 133px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.amount"/></th>
			<th style="width: 133px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.allocated"/></th>
			<th style="width: 133px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.unallocated"/></th>
			<th style="width:  20px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.edit"/></th>
		</tr>
		<c:if test="${!isDetailLevelAllocation}">
		<c:choose>
			<c:when test="${empty billPayments}">
				<tr id="pRow${i}" style="display:none">
					<td>
						<label> </label>
						<input type="hidden" name="paymentRemittanceId" value="">
						<input type="hidden" name="paymentIdSeq" value="">
					</td>
					<td>
						<label> </label>
						<input type="hidden" name="paymentReference" id="paymentReference_" value="">
						<input type="hidden" name="origPaymentReference" value="">
					</td>
					<td>
						<label>	</label>
						<input type="hidden" name="paymentRecdDate" value=""/>
					</td>
					<td class="number">
						<label> </label>
						<input type="hidden" name="paymentAmount" value="">
					</td>
					<td class="number">
						<label> </label>
						<input type="hidden" name="paymentAllocAmount" value="">
					</td>
					<td class="number">
						<label> </label>
						<input type="hidden" name="paymentUnallocAmount" id="paymentUnallocAmount_" value="">
					</td>
					<td>
						<a href="javascript:Edit" title="Edit Payment Details">
						<img src="${cpath}/icons/Edit.png" class="button" onclick="return showPaymentDialog(this);"/>
						</a>
					</td>
				</tr>
			</c:when>
			<c:otherwise>
				<c:forEach items="${billPayments}" var="billpmt" varStatus="status">
					<c:set var="i" value="${status.index}"/>
					<c:set var="totalRemittanceAmount"  value="${totalRemittanceAmount + billpmt.amount_recd}"/>
					<c:set var="totalAllocatedAmount"   value="${totalAllocatedAmount + (billpmt.amount_recd - billpmt.unalloc_amount)}"/>
					<c:set var="totalUnallocatedAmount" value="${totalUnallocatedAmount + billpmt.unalloc_amount}"/>
					<tr id="pRow${i}">
						<td>
							<label>
								<c:if test="${billpmt.remittance_id > 0}">
									<insta:screenlink screenId="ins_remittance" label="${billpmt.remittance_id}"
										extraParam="?_method=show&remittance_id=${billpmt.remittance_id}"
										title="View Remittance File"/>
								</c:if>
							</label>
							<input type="hidden" name="paymentRemittanceId" value="${billpmt.remittance_id}">
							<input type="hidden" name="paymentIdSeq" value="${billpmt.payment_id}">
						</td>
						<td>
							<label> ${billpmt.payment_reference} </label>
							<input type="hidden" name="paymentReference" id="paymentReference_${billpmt.payment_reference}" value="${billpmt.payment_reference}">
							<input type="hidden" name="origPaymentReference" value="${billpmt.payment_reference}">
						</td>
						<td>
							<fmt:formatDate value="${billpmt.received_date}" var="recdDate" pattern="dd-MM-yyyy"/>
							<label>	${recdDate}	</label>
							<input type="hidden" name="paymentRecdDate" value="${recdDate}"/>
						</td>
						<td class="number">
							<label>${ifn:afmt(billpmt.amount_recd)}</label>
							<input type="hidden" name="paymentAmount" value="${billpmt.amount_recd}">
						</td>
						<td class="number">
							<label>${ifn:afmt(billpmt.amount_recd - billpmt.unalloc_amount)}</label>
							<input type="hidden" name="paymentAllocAmount" value="${billpmt.amount_recd - billpmt.unalloc_amount}">
						</td>
						<td class="number">
							<label>${ifn:afmt(billpmt.unalloc_amount)}</label>
							<input type="hidden" name="paymentUnallocAmount" id="paymentUnallocAmount_${billpmt.payment_reference}" value="${billpmt.unalloc_amount}">
						</td>
						<td>
							<a href="javascript:Edit" title="Edit Payment Details">
							<img src="${cpath}/icons/Edit.png" class="button" onclick="return showPaymentDialog(this);"/>
							</a>
						</td>
					</tr>
				</c:forEach>
			</c:otherwise>
		</c:choose>
		</c:if>
	</table>
	<table class="addButton">
		<tr>
			<td style="width: 399px;text-align: right"><b><insta:ltext key="billing.editremittanceamount.details.total"/></b></td>
			<td style="width: 170px;text-align: right;"><label id="lblTotalRemittanceAmt">${ifn:afmt(totalRemittanceAmount)}</label></td>
			<td style="width: 133px;text-align: right;"><label id="lblTotalAllocatedAmt">${ifn:afmt(totalAllocatedAmount)}</label></td>
			<td style="width: 133px;text-align: right;"><label id="lblTotalUnallocatedAmt">${ifn:afmt(totalUnallocatedAmount)}</label></td>
			<td style="width:  20px;text-align: right;">
				<c:if test="${!isDetailLevelAllocation}">
				<button type="button" name="btnAddPayment" id="btnAddPayment" title="Add New Payment"
					onclick="showPaymentDialog(this, 'add');return false;"
					accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</c:if>
			</td>
		</tr>
	</table>
	</div>
</fieldset>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="billing.editremittanceamount.details.allocations"/></legend>
	<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="detailList" cellspacing="0" cellpadding="0" id="chargesTable" border="0" width="100%">
		<tr>
			<th style="width: 20px"><input type="checkbox" onclick="selectAllCharges()" id="allCharges"/></th>
			<th style="width: 30px"><insta:ltext key="billing.editremittanceamount.details.posteddate"/></th>
			<th style="width: 40px"><insta:ltext key="billing.editremittanceamount.details.chargehead"/></th>
			<th style="width: 60px"><insta:ltext key="billing.editremittanceamount.details.description"/></th>
			<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.amount"/></th>
			<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.patientamt"/></th>
			<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.claimamt"/></th>
			<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.totalremittances"/></th>
			<th style="width: 10px;text-align: right;"><insta:ltext key="billing.editremittanceamount.details.edit"/></th>
		</tr>
		<c:forEach items="${charges}" var="charge">
			<c:if test="${(charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt) >= 0}">
				<tr>
					<td>
						<input type="checkbox" name="chargeCheck" value="${charge.chargeId}"/>
					</td>
					<td>
						<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>
					</td>
					<td>
						<label title="${charge.chargeHeadName}">${charge.chargeHeadName}</label>
					</td>
					<td><insta:truncLabel value="${charge.actDescription}" length="30"/></td>
					<td class="number"><label>${charge.amount + charge.returnAmt}</label></td>
					<td class="number"><label>${(charge.amount + charge.returnAmt) - (charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt)}</label></td>
					<td class="number">
						<label>${charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt}</label>
						<input type="hidden" name="patientAmt" value='${(charge.amount + charge.returnAmt) - (charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt)}' />
						<input type="hidden" name="insClaimAmt" value='${charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt}' />
						<input type="hidden" name="chargeId" value='${charge.chargeId}' />
						<c:forEach items="${chargePayments}" var="chrgpmt">
							<c:if test="${chrgpmt.charge_id == charge.chargeId && chrgpmt.payment_reference == chrgpmt.payment_reference}">
								<input type="hidden" name="chargeid_paymentref_amount"  id="${charge.chargeId}_${chrgpmt.payment_reference}"
								value='${charge.chargeId}_${chrgpmt.payment_reference}_${chrgpmt.amount}' />
							</c:if>
						</c:forEach>
					</td>
					<td class="number">
						<label>${charge.claimRecdAmount}</label>
						<input type="hidden" name="allocatedClaimAmt" value="${charge.claimRecdAmount}"/>
					</td>
					<td class="number">
						<a href="javascript:Edit" title="Edit Allocation Details">
						<img src="${cpath}/icons/Edit.png" class="button" onclick="return showAllocateDialog(this);" />
						</a>
					</td>
				</tr>
			</c:if>
		</c:forEach>
	</table>
	</div>
</fieldset>

<div style="float:right;padding-top:10px;">
	<button onclick="return resetAllocations()" accesskey="R"><b><u><insta:ltext key="billing.editremittanceamount.details.r"/></u></b><insta:ltext key="billing.editremittanceamount.details.esetallocation"/></button> |
	<button onclick="return autoAllocate()" accesskey="A"><b><u><insta:ltext key="billing.editremittanceamount.details.a"/></u></b><insta:ltext key="billing.editremittanceamount.details.utoallocate"/></button>
	<img class="imgHelpText" title="Please note: Auto allocate will reset the previous allocations and selected charges are allocated amounts again."
		src="${cpath}/images/help.png"/>
</div>

<table class="screenActions">
	<tr>
		<td>
			<c:set var="disabled" value=""/>
			<c:if test="${bill.status == 'C' || bill.status == 'X'}">
			<c:set var="disabled"> disabled = "disabled" </c:set>
			</c:if>
			<button onclick="return saveRemittance()" ${disabled} accesskey="S"><b><u><insta:ltext key="billing.editremittanceamount.details.s"/></u></b><insta:ltext key="billing.editremittanceamount.details.ave"/></button>
		</td>
		<td>
			<insta:screenlink screenId="credit_bill_collection" addPipe="true" label="View/Edit Bill: ${bill.billNo}"
				extraParam="?_method=getCreditBillingCollectScreen&billNo=${bill.billNo}"
				title="View/Edit Bill."/>
		</td>
	</tr>
</table>
</form>

<form name="paymentform">
<input type="hidden" id="paymentRowId" value=""/>
<div id="paymentDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.editremittanceamount.details.addoreditpayment"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.paymentreference"/>:</td>
				<td class="forminfo">
					<input style="width:80px" type="text" name="pPaymentReference" value="">
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.date"/>:</td>
				<td class="forminfo">
					<insta:datewidget name="pPaymentDate" value="today" btnPos="left"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.totalamount"/>:</td>
				<td class="forminfo">
					<input style="width:80px" type="text" name="pTotalAmount" value="">
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.allocatedamount"/>:</td>
				<td class="forminfo"><label id="pAllocatedAmount">0.00</label></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editremittanceamount.details.unallocatedamount"/>:</td>
				<td class="forminfo"><label id="pUnallocatedAmount">0.00</label></td>
			</tr>
			<tr>
		</table>
	</fieldset>
	<table>
		<tr>
			<td>
				<input type="button" name="editPaymentBtn" id="editPaymentBtn" onclick="onPaymentSubmit()" value="OK" style="display:inline" />
				<input type="button" name="savePaymentBtn" id="savePaymentBtn" onclick="addNewPayment()" value="Save" style="display:none"/>
			</td>
			<td><input type="button" onclick="onPaymentCancel()" value="Cancel"/></td>
			<td><input type="button" onclick="showPreviousPaymentDialog()" value="<<Prev"/></td>
			<td><input type="button" onclick="showNextPaymentDialog()" value="Next>>"/></td>
		</tr>
	</table>
</div>
</div>
</form>

<form name="allocateform">
<input type="hidden" id="allocateRowId" value=""/>
<div id="allocateDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.editremittanceamount.details.editallocation"/></legend>
		<table class="formtable">
			<tr>
				<th><insta:ltext key="billing.editremittanceamount.details.paymentref"/>.</th>
				<th><insta:ltext key="billing.editremittanceamount.details.date"/></th>
				<th align="left"><insta:ltext key="billing.editremittanceamount.details.amount"/></th>
			</tr>
			<c:forEach items="${billPayments}" var="billpmt" varStatus="status">
				<c:set var="i" value="${status.index}"/>
			    <tr>
					<td align="center">${billpmt.payment_reference}</td>
					<td align="center">
						<fmt:formatDate value="${billpmt.received_date}" pattern="dd-MM-yyyy"/>
					</td>
					<td class="number" align="left">
						<input style="width:80px" type="text" name="aAllocateAmount" id="aAllocateAmount_${billpmt.payment_reference}"
							value="" onchange="calculateChargeAllocTotal()">
						<input type="hidden" name="aPaymentReference" id="aPaymentReference${i}" value="${billpmt.payment_reference}">
						<input type="hidden" name="aOrigAllocateAmount" id="aOrigAllocateAmount_${billpmt.payment_reference}" value="">
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="2" align="right"><insta:ltext key="billing.editremittanceamount.details.total"/>:</td>
				<td class="forminfo" align="left"><label id="allocTotal"></label></td>
			</tr>
		</table>
	</fieldset>
	<table>
		<tr>
			<td><input type="button" onclick="onAllocateSubmit()" value="OK" /></td>
			<td><input type="button" onclick="onAllocateCancel()" value="Cancel"/></td>
			<td><input type="button" onclick="showPreviousAllocateDialog()" value="<<Prev"/></td>
			<td><input type="button" onclick="showNextAllocateDialog()" value="Next>>"/></td>
		</tr>
	</table>
</div>
</div>
</form>

</body>

