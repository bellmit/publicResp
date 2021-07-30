<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="masters/charges_common.js"/>
<insta:link type="js" file="masters/editCharges.js"/>
<insta:link type="script" file="widgets.js" />
<script>
var masterJobCount = '${masterJobCount}';
function chgRatePlan(){
	var cp = null;
	var ul = null;
	var oid=document.getElementById('ratePlan').value;
	var orgname=document.getElementById('ratePlan').options[document.getElementById('ratePlan').selectedIndex].text;
	var tid=document.getElementById('testId').value;
	cp=document.getElementById('contextPath').value;
	ul=cp+"/master/addeditdiagnostics/editcharge.htm?&testid="+tid+"&orgName="+orgname+"&orgId="+oid;
	document.editchargeform.action=ul;
	document.editchargeform.orgId.value = oid;
	document.editchargeform.method = 'GET';
	document.editchargeform.submit();
}

function validateAllDiscounts() {
	if (masterJobCount != undefined && masterJobCount > 0) {
		alert("Diagnostic charge scheduler in progress");
		return false;
	}
	var len = document.editchargeform.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('regularCharges','discount',i);
	}
	if(!valid) return false;
	else return true;
}
      Insta.masterData=${ifn:convertListToJson(testsList)};
</script>
</head>
<body onload="fillRatePlanDetailsMigrated('diagnostics','test_id','${testId}');">
	 <h1 style="float:left">Test Charges</h1>
	 <c:url var="searchUrl" value="/master/addeditdiagnostics/editcharge.htm"/>
	 <insta:findbykey keys="test_name,test_id" fieldName="testid" method="" url="${searchUrl}"
      extraParamKeys="orgId" extraParamValues="ORG0001"/>
<form action="${cpath }/master/addeditdiagnostics/updatecharge.htm" method="post" onsubmit="return validateAllDiscounts()" name="editchargeform">
	<input type="hidden" name="contextPath" id="contextPath" value="${ifn:cleanHtmlAttribute(pageContext.request.contextPath)}" />
	<input type="hidden" name="chargeType" />
	<input type="hidden" name="orgId" id="orgId" value="${ifn:cleanHtmlAttribute(orgId)}"/>
	<input type="hidden" name="_method" value="${requestScope.method}"/>
	<input type="hidden" name="testId" id="testId" value ="${testId}" />
	<input type="hidden" name="orgName" value="${orgName}"/>
	<input type="hidden" name="testid" id="testid" value="${testid}"/>
	<input type="hidden" name="Referer" value="${ifn:cleanHtmlAttribute(header.Referer)}"/>

	<c:url var="ED" value="show.htm">
		<c:param name="_method" value="getEditTest" />
		<c:param name="testid" value="${ifn:cleanURL(testid)}"/>
		<c:param name="orgId" value="${ifn:cleanURL(orgId)}"/>
		<c:param name="orgName" value="${ifn:cleanURL(orgName)}"/>
	</c:url>
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Test Name :</td>
				<td> <input type ="text" name="testName" value="${ifn:cleanHtml(testName)}" id="testName" readonly="true"/> </td>
				<td class="formlabel">Rates For Rate Sheet :</td>
				<td>
					<select name="ratePlan" id="ratePlan" onchange="return chgRatePlan();" class="dropdown">
						<c:forEach items="${rateSheets}" var="ratesheet">
							<option value="${ratesheet.get('org_id')}" ${ratesheet.get('org_id') eq param.orgId ? 'selected' : '' }>${ratesheet.get('org_name')}</option>
						</c:forEach>
					</select>
				</td>

			</tr>
			<tr>
				<td class="formlabel">Treatment Code Type</td>
				<td>
					<select name="codeType" class="dropdown">
						<option value="" >--Select--</option>
						<c:forEach items="${treatmentCodes}" var="treatment">
							<option value="${treatment.get('code_type')}" ${treatment.get('code_type') eq codeType ? 'selected' : ''} >${treatment.get('code_type')}</option>						
						</c:forEach>
					</select>
				</td>
				<td class="formlabel">Rate Plan Code :</td>
				<td><input type="text" name="orgItemCode" id="orgItemCode" maxlength="600" value="${item_code}"/></td>
			</tr>
		</table>
	</fieldset>
	<div class="resultList">
	<fieldset class="fieldSetBorder">
		<table id="testCharges" class="dataTable">
			<c:forEach var="entry" items="${requestScope.chargeMap}">

				<c:choose>
					<c:when test="${entry.key eq 'CHARGES'}">
						<tr>
							<th>CHARGES</th>
							<c:forEach var="item" items="${entry.value}">
								<c:choose>
									<c:when test="${item == 'GENERAL'}">
										<th>GENERAL/OP</th>
									</c:when>
									<c:otherwise>
										<th>${ifn:cleanHtml(item)}</th>
									</c:otherwise>
								</c:choose>
								<input type="hidden" name="bedTypes" value="${ifn:cleanHtmlAttribute(item)}"/>
							</c:forEach>
						</tr>
					</c:when>
					<c:when test="${entry.key eq 'REGULARCHARGE'}">
						<tr>
							<td>REGULAR CHARGES</td>
							<c:forEach var="item" items="${entry.value}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td><input type="text" value="${ifn:afmt(item)}"
									name="regularCharges" id="regularCharges${i}"  class="number validate-decimal"
									onkeypress="return nextFieldOnTab(event, this, 'testCharges');"
									onblur="validateDiscount('regularCharges','discount','${i}')"/>
									</td>
							</c:forEach>
							<input type="hidden" name="ids" value="${i+1}">
						</tr>
					</c:when>
					<c:when test="${entry.key eq 'DISCOUNT'}">
						<tr>
							<td>DISCOUNT</td>
							<c:forEach var="item" items="${entry.value}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td><input type="text" value="${ifn:afmt(item)}"
									name="discount" id="discount${i}" class="number validate-decimal"
									onkeypress="return nextFieldOnTab(event, this, 'testCharges');"
									onblur="validateDiscount('regularCharges','discount','${i}')"/>
									</td>
							</c:forEach>
						</tr>
					</c:when>
					<c:when test="${entry.key eq 'SCHEDULECHARGE'}">
						<tr>
							<td>SCHEDULE CHARGES</td>
							<c:forEach var="item" items="${entry.value}">
								<td><input type="text" value="${ifn:afmt(item)}"
									name="scheduleCharges" class="number validate-decimal"
									onkeypress="return nextFieldOnTab(event, this, 'testCharges');"/></td>
							</c:forEach>
						</tr>
					</c:when>
				</c:choose>
			</c:forEach>
			<c:forEach var="entry" items="${requestScope.chargeMap}">
				<c:choose>
					<c:when test="${entry.key eq 'CHARGES'}">
						<tr id="audit_log_row">
							<td></td>
							<c:forEach var="item" items="${entry.value}">
								<td>
									<insta:screenlink screenId="diagnosticTests_audit_log"
										label="Audit Log"
										extraParam="?_method=getAuditLogDetails&test_id=${testid}&org_name=${param.orgId}&bed_type=${item}&test_name=${ifn:encodeUriComponent(param.test_name)}&al_table=diagnostic_charges_audit_log_view"/>
									</td>
							</c:forEach>
						</tr>
					</c:when>
				</c:choose>
			</c:forEach>
			<c:if test="${not empty chargeMap}">
				<tr>
					<td>Apply Charges To All</td>
					<td><input type="checkbox" name="checkbox" onclick="fillValues('testCharges', this);" /></td>
					<c:forEach begin="2" end="${fn:length (chargeMap.CHARGES)}">
						<td>&nbsp;</td>
					</c:forEach>
				</tr>
			</c:if>
		</table>
	</fieldset>
	</div>

	<div id="ratePlanDiv" style="display:none">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
			<table class="dashBoard" id="ratePlanTbl">
				<tr class="header">
					<td>Include</td>
					<td>Rate Plan</td>
					<td>Discount / Markup</td>
					<td>Variation %</td>
					<td>&nbsp;</td>
				</tr>
				<tr id="" style="display: none">
			</table>
			<table class="screenActions" width="100%">
				<tr>
					<td align="right">
						<img src='${cpath}/images/blue_flag.gif'>Overridden
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
	<table class="screenAcitons">
		<tr>
			<td colspan="4" align="center">
				<button type="submit" name="Update"><b>U</b>pdate</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${ED}">Test Details</a></td>
		</tr>
	</table>
</form>
	<script>
		var derivedRatePlanDetails = ${ifn:convertListToJson(derivedRatePlanDetails)};
	</script>
</body>
</html>
