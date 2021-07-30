<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="claimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${claimStatusDisplay}" property="O" value="Open"/>
<c:set target="${claimStatusDisplay}" property="B" value="Batched"/>
<c:set target="${claimStatusDisplay}" property="C" value="Closed"/>
<c:set target="${claimStatusDisplay}" property="M" value="Marked For Resubmission"/>

<html>
<head>
	<title><insta:ltext key="billing.editclaim.list.editclaim"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
<script>
function doSave() {
	document.newClaimForm.buttonAction.value = 'save';
	document.getElementById("saveButton").disabled = true;
	document.newClaimForm.submit();
	return true;
}
</script>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body>
<c:set var="billno">
<insta:ltext key="billing.editclaim.list.billno"/>
</c:set>
<c:set var="visitid">
<insta:ltext key="billing.editclaim.list.visitid"/>
</c:set>
<c:set var="find">
	<insta:ltext key="common.auditlog.auditlogdetails.find"/>
</c:set>
<form name="billNoForm" action="editClaim.do">
	<input type="hidden" name="_method" value="getClaims">
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="billing.editclaim.list.editclaim"/></h1></td>
			<td><insta:ltext key="billing.editclaim.list.bill"/>&nbsp;<insta:ltext key="billing.editclaim.list.no"/>:&nbsp;</td>
			<td><input type="text" name="bill_no" id="bill_no" style="width: 80px"></td>
			<td><input type="submit" class="button" value="${find}"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel />

<insta:patientdetails visitid="${visitId}"/>

<c:if test="${param._method == 'getClaims'}">
	<form name="newClaimForm">
		<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(visitId)}" />
		<input type="hidden" name="bill_no" id="bill_no" value="${ifn:cleanHtmlAttribute(billNo)}" />
		<input type="hidden" name="buttonAction" value="save">
		<input type="hidden" name="_method" value="createNewClaim">
		<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable" onmouseover="hideToolBar('');">
			<tr>
				<insta:sortablecolumn name="visit_id" title="${visitid}"/>
				<insta:sortablecolumn name="bill_no" title="${billno}"/>
				<th><insta:ltext key="billing.editclaim.list.billopendate"/></th>
				<th><insta:ltext key="billing.editclaim.list.billfinalizeddate"/></th>
				<th><insta:ltext key="billing.editclaim.list.billtype"/></th>
				<th><insta:ltext key="billing.editclaim.list.billstatus"/></th>
				<th><insta:ltext key="billing.editclaim.list.planname"/></th>
				<th><insta:ltext key="billing.editclaim.list.claimno"/></th>
				<th><insta:ltext key="billing.editclaim.list.claimstatus"/></th>
				<th><insta:ltext key="billing.editclaim.list.action"/></th>
			</tr>
			  <c:forEach var="bl" items="${billsList}" >
			  	<c:set var="billTypeDisplay">
					<c:choose>
						<c:when test="${bl.map.bill_type == 'P' && bl.map.restriction_type == 'P'}"><insta:ltext key="billing.editclaim.list.billnow_ph"/></c:when>
						<c:when test="${bl.map.bill_type == 'C' && bl.map.restriction_type == 'P'}"><insta:ltext key="billing.editclaim.list.billlater_ph"/></c:when>
						<c:when test="${bl.map.bill_type == 'P'}"><insta:ltext key="billing.editclaim.list.billnow"/></c:when>
						<c:when test="${bl.map.bill_type == 'C'}"><insta:ltext key="billing.editclaim.list.billlater"/></c:when>
						<c:otherwise><insta:ltext key="billing.editclaim.list.other"/></c:otherwise>
					</c:choose>
				</c:set>
				<tr>
					<td>
						${bl.map.visit_id}
						<input type="hidden" name="visitId" id="visitId" value="${bl.map.visit_id}" />
					</td>
					<td>
						<a href="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen&amp;billNo=${bl.map.bill_no}">
						${bl.map.bill_no}</a>
					</td>

					<td><fmt:formatDate value="${bl.map.open_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					<td><fmt:formatDate value="${bl.map.finalized_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					<td>${billTypeDisplay}</td>
					<td>${statusDisplay[bl.map.status]}</td>
					<td>
						${bl.map.plan_name}
						<input type="hidden" name="plan_id" value="${bl.map.plan_id}"/>
					</td>
					<td>${bl.map.claim_id}</td>
					<%-- This one is coming from the insurance_claim table--%>
					<c:forEach var="cl" items="${claimsList}" >
						<c:if test="${cl.map.claim_id == bl.map.claim_id}">
							<td>${claimStatusDisplay[cl.map.claim_status]}</td>
						</c:if>
					 </c:forEach>
					<td>
						<c:set var="disable" value=""/>
						<c:if test="${bl.map.status != 'A'}">
							<c:set var="disable" value="disabled"/>
						</c:if>
						<select id="selected_claim_id" name="selected_claim_id" ${disable} class="dropdown">
						<option value=""><insta:ltext key="billing.editclaim.list.nochange.in.brackets"/>)</option>
						<option value="${bl.map.bill_no}_New"><insta:ltext key="billing.editclaim.list.attachtonewclaim"/></option>
						<c:forEach var="cl" items="${claimsList}" >
							<c:if test="${cl.map.plan_id == bl.map.plan_id}">
								<option value=${cl.map.claim_id}_${bl.map.bill_no}><insta:ltext key="billing.editclaim.list.attachtoclaim"/> ${cl.map.claim_id}</option>
							</c:if>
						</c:forEach>
						</select>
					</td>
				</tr>
			</c:forEach>
		</table>
		<table>
		<tr>
			<td>
				<button type="button" id="saveButton" accessKey="S" onclick="return doSave();" ${empty visitId ? 'disabled' : '' }><b><u><insta:ltext key="billing.editclaim.list.s"/></u></b><insta:ltext key="billing.editclaim.list.ave"/></button>
			</td>
		</tr>
		</table>
	</form>
</c:if>
</body>
</html>