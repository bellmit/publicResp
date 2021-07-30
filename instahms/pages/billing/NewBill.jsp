<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="org.apache.struts.Globals"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
<title><insta:ltext key="billing.newebill.details.title"/></title>
<meta http-equiv="Content Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="billing/newbill.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<script>
	var allowBillNowInsurance = '${allowBillNowInsurance}';
</script>
<insta:js-bundle prefix="billing.newbill"/>
<insta:js-bundle prefix="registration.patient"/>
</head>

<%-- TODO: show patientdetails header on selecting the patient --%>

<body class="yui-skin-sam" onload="enableDisabledTpa();">
<h1 style="float: left"><insta:ltext key="billing.newebill.details.newbill"/></h1>
<c:choose>
	<c:when test="${actionRightsMap.create_bill_for_closed_visit eq 'A' || roleId eq '1' || roleId eq '2'}">
	<insta:patientsearch searchType="visit" searchUrl="NewPrepaidBill.do" activeOnly="false"
		buttonLabel="Find" searchMethod="getPatientVisitDetails" fieldName="patient_id" showStatusField="true" addStatusRight="true" />
	</c:when>
	<c:otherwise>
		<insta:patientsearch searchType="visit" searchUrl="NewPrepaidBill.do" activeOnly="true"
		buttonLabel="Find" searchMethod="getPatientVisitDetails" fieldName="patient_id"/>
	</c:otherwise>
</c:choose>



<div><insta:feedback-panel/></div>
<insta:patientdetails visitid="${visitbean.patient_id}" />
<html:form method="POST" action="/pages/BillDischarge/NewPrepaidBill.do">
<input type="hidden" name="_method" value="createNewBill">
<input type="hidden" name="visitId" value="${visitbean.patient_id}">

<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="billing.newebill.details.billtype"/>:</td>
			<td colspan="3">
				<input type="radio" name="creditprepaid" id="prepaid" value="P" checked onclick="enableDisabledTpa();">
				<label for="prepaid"><insta:ltext key="billing.newebill.details.billnow"/></label>
				<c:if test="${actionRightsMap.allow_credit_bill_later eq 'A' || roleId == 1 || roleId ==2}">
					<input type="radio" name="creditprepaid" id="credit" value="C" onclick="enableDisabledTpa();">
					<label for="credit"><insta:ltext key="billing.newebill.details.billlater"/></label>
				</c:if>
			</td>
			<td>
				<c:set var="allowConnectTPA" value="${(not empty visitbean.primary_sponsor_id && empty visitbean.secondary_sponsor_id) || multiPlanExisits}"/>
				<c:if test="${allowConnectTPA && visitbean.use_drg == 'N' && visitbean.use_perdiem == 'N'}">
					<input type="checkbox" name="istpa" id="istpa" value="Y" checked/><insta:ltext key="billing.newebill.details.connecttotpa"/>
				</c:if>
			</td>
			<td></td>
			<td></td>
		</tr>
	</table>
</fieldset>

<div class="screenActions" style="margin-top: 1em">
<button type="button" class="button" name="createBill" accesskey="C" onclick="newBillCreate();"><label><u><b><insta:ltext key="billing.newebill.details.c"/></b></u><insta:ltext key="billing.newebill.details.reatebill"/></label></button>&nbsp;
	<!--<insta:accessbutton buttonkey="billing.newebill.details.createbill" type="submit" onclick="return newBillCreate();" />-->&nbsp;
	|
	<a href="javascript:void(0);" onclick="reset();"><insta:ltext key="billing.newebill.details.reset"/></a>
</div>

</html:form>
</body>
</html>

