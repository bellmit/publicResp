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
function chgRatePlan(){
	var oid=document.addtest.ratePlan.value;
	var orgname=document.addtest.ratePlan.options[document.addtest.ratePlan.selectedIndex].text;
	var tid=document.addtest.testID.value;
	var cp=document.addtest.contextPath.value;
	var ul=cp+"/pages/masters/hosp/diagnostics/addtest.do?_method=editTestCharges&testid="+tid+"&orgName="+orgname+"&orgId="+oid;
	document.addtest.action=ul;
	document.addtest.submit();
}

function validateAllDiscounts() {
	var len = document.addtest.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('regularCharges','discount',i);
	}
	if(!valid) return false;
	else return true;
}
      Insta.masterData=${testsList};
</script>
</head>
<body onload="fillRatePlanDetails('diagnostics','test_id','${addtest.testId}');">
	 <h1 style="float:left">Test Charges</h1>
	 <c:url var="searchUrl" value="/pages/masters/hosp/diagnostics/addtest.do"/>
	 <insta:findbykey keys="test_name,test_id" fieldName="testid" method="editTestCharges" url="${searchUrl}"
      extraParamKeys="orgId" extraParamValues="ORG0001"/>
<html:form action="/pages/masters/hosp/diagnostics/addtest" method="post" onsubmit="return validateAllDiscounts()">
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
	<html:hidden property="chargeType" />
	<html:hidden property="orgId" value="${addtest.orgId}"/>
	<html:hidden property="_method" value="${requestScope.method}"/>
	<html:hidden property="testId"/>
	<html:hidden property="orgName" value="${addtest.orgName}"/>
	<html:hidden property="testID" value="${addtest.testId}"/>
	<input type="hidden" name="Referer" value="${ifn:cleanHtmlAttribute(header.Referer)}"/>

	<c:url var="ED" value="addtest.do">
		<c:param name="_method" value="getEditTest" />
		<c:param name="testid" value="${ifn:cleanURL(testid)}"/>
		<c:param name="orgId" value="${ifn:cleanURL(addtest.orgId)}"/>
		<c:param name="orgName" value="${ifn:cleanURL(addtest.orgName)}"/>
	</c:url>
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
		<table class="formtable">
			</tr>
				<td class="formlabel">Test Name :</td>
				<td> <html:text property="testName" readonly="true"/> </td>
				<td class="formlabel">Rates For Rate Sheet :</td>
				<td>
					<insta:selectdb name="ratePlan" value="${addtest.orgId}"
						table="organization_details" valuecol="org_id"
						displaycol="org_name"  onchange="return chgRatePlan();"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"
						orderby="org_name"/>
				</td>

			</tr>
			<tr>
				<td class="formlabel">Treatment Code Type</td>
				<td>
					<insta:selectdb name="codeType" table="mrd_supported_codes" valuecol="code_type"
					displaycol="code_type" filtercol="code_category" filtervalue="Treatment" dummyvalue="--Select--"
					value="${codeType}" />
				</td>
				<td class="formlabel">Rate Plan Code :</td>
				<td><html:text property="orgItemCode" maxlength="600"/></td>
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
								<input type="hidden" name="bedTypes" value="<c:out value='${item}'/>">
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
										extraParam="?_method=getAuditLogDetails&test_id=${testid}&org_name=${addtest.orgId}&bed_type=${item}&test_name=${ifn:encodeUriComponent(addtest.testName)}&al_table=diagnostic_charges_audit_log_view"/>
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
				<button type="submit" name="Update" accesskey="U"><b><u>U</u></b>pdate</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${ED}">Test Details</a></td>
		</tr>
	</table>
</html:form>
	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
	</script>
</body>
</html>
