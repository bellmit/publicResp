<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title><insta:ltext key="billing.finalization.details.billfinalization"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	function validate() {
		var rows = document.getElementById("chargeTable").rows.length;
		if(rows < 2)
			return false;
		var finalizedDateObj = document.billFinalizationForm.finalized_date;
		var finalizedTimeObj = document.billFinalizationForm.finalized_time;
		var valid = true;
		valid =	valid && validateRequired(finalizedDateObj, getString("js.billing.finalization.daterequired"));
		if(!valid) return false;
		valid = valid && validateRequired(finalizedTimeObj, getString("js.billing.finalization.timerequired"));
		if(!valid) return false;
		valid = valid && doValidateDateField(finalizedDateObj);
		if(!valid) return false;
		valid = valid && validateTime(finalizedTimeObj);
		if(!valid) return false;
		return true;
	}
</script>
</head>
<body>

<form name="billNoForm" action="BillFinalization.do">
	<input type="hidden" name="buttonAction" value="save">
	<input type="hidden" name="_method" value="getRunningBillFinalizationScreen">
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="billing.finalization.details.runningbillfinalization"/></h1></td>

			<td><insta:ltext key="billing.finalization.details.bill"/>&nbsp;<insta:ltext key="billing.finalization.details.no"/>:&nbsp;</td>
			<td><input type="text" name="billNo" id="billNo" style="width: 80px"></td>
			<td><input name="getDetails" type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel/>
<insta:patientdetails patient="${patient}"/>

<form action="BillFinalization.do" method="POST" name="billFinalizationForm">
	<input type="hidden" name="billNo" value="${bill.bill_no}"/>
	<input type="hidden" name="_method" value="saveBillFinalization"/>
	<jsp:useBean id="currentDate" class="java.util.Date"/>

<!-- Finalized date validation .. compare with all charges and check if they r less than that date -->
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.finalization.details.billdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.finalization.details.billno"/>: </td>
				<td class="forminfo">
				<a href="${cpath}/billing/BillFinalization.do?_method=getCreditBillingCollectScreen&billNo=${bill.bill_no}">
					${bill.bill_no}
				</a>
				</td>

				<td class="formlabel"><insta:ltext key="billing.finalization.details.opendate"/>:</td>
				<td class="forminfo"><fmt:formatDate value="${bill.open_date}" pattern="dd-MM-yyyy"/></td>

				<td class="formlabel" colspan="2">
					<table>
						<tr>
							<td class="formlabel"><insta:ltext key="billing.finalization.details.finalizationdate"/>:</td>
							<td class="forminfo">
								<c:set var="finaldate"><fmt:formatDate value="${currentDate}" pattern="dd-MM-yyyy"/></c:set>
								<c:set var="finaltime"><fmt:formatDate value="${currentDate}" pattern="HH:mm"/></c:set>
								<insta:datewidget name="finalized_date" value="${finaldate}"/>
								<input type="text" size="4" name="finalized_time" value="${finaltime}" class="timefield"/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.finalization.details.finalizationcharges"/>:</legend>
		<table class="detailList" id="chargeTable">
			<c:if test="${not empty bill.bill_no}">
				<c:choose>
					<c:when test="${not empty beds || not empty equipments}">
						 <c:if test="${not empty beds}">
								<tr>
									<th><insta:ltext key="billing.finalization.details.wardname"/></th>
									<th><insta:ltext key="billing.finalization.details.bedtype"/></th>
									<th><insta:ltext key="billing.finalization.details.bedname"/></th>
									<th><insta:ltext key="billing.finalization.details.startdate"/></th>
									<th><insta:ltext key="billing.finalization.details.enddate"/></th>
								</tr>
								<c:forEach items="${beds}" var="bed">
									<tr>
										<td>${bed.ward_name}</td>
										<td>${bed.bed_type}</td>
										<td>${bed.bed_name}</td>
										<td>${bed.start_date}</td>
										<td>${bed.end_date}</td>
									</tr>
								</c:forEach>
						</c:if>
						<c:if test="${not empty equipments}">
								<tr>
									<th colspan="3"><insta:ltext key="billing.finalization.details.equipmentname"/></th>
									<th><insta:ltext key="billing.finalization.details.fromdate"/></th>
									<th><insta:ltext key="billing.finalization.details.todate"/></th>
								</tr>
								<c:forEach items="${equipments}" var="equip">
									<tr>
										<td colspan="3">${equip.item_name}</td>
										<td>${equip.from_timestamp}</td>
										<td>${equip.to_timestamp}</td>
									</tr>
								</c:forEach>
						</c:if>
					</c:when>
				<c:otherwise>
					<tr>
						<td></td>
						<td colspan="2"><insta:ltext key="billing.finalization.details.nochargestobefinalized"/></td>
					</tr>
				</c:otherwise>
				</c:choose>
			</c:if>
		</table>
	</fieldset>


	<div class="screenActions">
		<button type="submit" name="finalize" class="button" accesskey="F" onclick="return validate();"/><label><u><b><insta:ltext key="billing.finalization.details.f"/></b></u><insta:ltext key="billing.finalization.details.inalize"/></label></button>&nbsp;
		| <a href="${cpath}/pages/BillDischarge/BillList.do?_method=getBills&status=A&visit_type=i&visit_type=o&sortOrder=open_date&sortReverse=true"><insta:ltext key="billing.finalization.details.openbills"/></a>
	</div>
</form>

</body>
</html>
