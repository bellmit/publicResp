<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<title>Insurance Estimates - Insta HMS</title>

<style type="text/css">
		td.forminfo { font-weight: bold; }
		form { padding: 0px; margin: 0px; }
		table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse; }
		table.detailFormTable td { white-space: nowrap; border: 1px solid silver;}
		.stwMain { margin: 5px 7px }
		tr.deleted {background-color: #F2DCDC; color: gray; }
		tr.deleted input {background-color: #F2DCDC; color: gray;}
	</style>
<script type="text/javascript">

 function validate(){
 	var bedtype = document.getElementById("bed_type").value;
 	var org = document.getElementById("rate_plan").value;
 	if(org == ""){
 		alert("Select Rate Plan");
 		return false;
 	}
 	if(bedtype == ""){
 		alert("Select BedType");
 		return false;
 	}
 	document.getElementById('orgName').value=document.forms[0].rate_plan.options[document.forms[0].rate_plan.selectedIndex].text
	return true;
 }


</script>

</head>
<body>
<div class="pageHeader"> Estimate Prerequisites </div>
<div align="right"><a href="../../Insurance/InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true" name="back">Back To Dashboard</a></div>
<c:choose>
	<c:when test="${not empty EstimatePatient.patientId}">
		<insta:patientdetails  visitid="${EstimatePatient.patientId}" />
	</c:when>
	<c:otherwise>
		<insta:patientgeneraldetails  mrno="${EstimatePatient.mrNo}" />
	</c:otherwise>
</c:choose>
<form name="mainform" action="./EstimateAction.do">
<input type="hidden" name="method" value="getQuickEstimationScreen">
<input type="hidden" name="insuranceID" value="${ifn:cleanHtmlAttribute(insuranceID)}">
<input type="hidden" name="moduleId" value="${ifn:cleanHtmlAttribute(moduleId)}">
<input type="hidden" name="orgName" id="orgName">
<table cellpadding="0" cellspacing="0" width="100%">

<tr>
<td align="center">
<fieldset class="fieldSetBorder" style="width:50%;">
<table align="center" class="formtable">
	<tr>
		<td class="formlabel">Rate Plan</td>
		<td>
			<select name="rate_plan" id="rate_plan" class="dropdown" onchange="document.getElementById('orgName').value=document.forms[0].rate_plan.options[document.forms[0].rate_plan.selectedIndex].text"> <option value="">...SELECT...</option>
			<c:forEach items="${Organization}" var="org">
			<c:choose>
				<c:when test="${EstimatePatient.organizationId == org.org_id}">
					<option value="${org.org_id}" selected>${org.org_name}</option>
				</c:when>
				<c:otherwise>
					<option value="${org.org_id}">${org.org_name}</option>
				</c:otherwise>
			</c:choose>
			</c:forEach>
			 </select>
		</td>
	</tr>
	<tr>
		<td class="formlabel">BedType:</td>
		<td>
			<select name="bed_type" id="bed_type" class="dropdown"> <option value="">...SELECT...</option>
			<c:forEach items="${BedTypes}" var="bedtypes">
			<c:choose>
				<c:when test="${EstimatePatient.bedType == bedtypes.BED_TYPE}">
					<option value="${bedtypes.BED_TYPE}" selected>${bedtypes.BED_TYPE}</option>
				</c:when>
				<c:otherwise>
					<option value="${bedtypes.BED_TYPE}">${bedtypes.BED_TYPE}</option>
				</c:otherwise>
			</c:choose>
			</c:forEach>
			 </select>
	   </td>
	 </tr>
	 <tr>
	 	<td class="formlabel">Visit Type:</td>
	 	<td>
	 		<select name="visit_type" id="visit_type" class="dropdown">
	 			<option value="i">IN Patient</option>
	 			<option value="o">OP Patient</option>
	 		</select>
	 	</td>
	 </tr>

<tr><td colspan="2" align="center"><input name="action" type="submit" class="button" value="OK" onclick="return validate()"></td></tr>
</table>
</fieldset>
</td>
</tr>
</table>

</form>
</body>
</html>
