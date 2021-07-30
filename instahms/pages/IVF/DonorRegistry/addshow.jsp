<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Patient Donor - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

	<script>
		function funSubmit(){
			var mrNo = document.patientDonor.mr_no.value;
			if(document.patientDonor._method.value == 'add') {
				document.patientDonor.action = cpath+"/IVF/DonorRegistry.do?_method=create&mr_no="+mrNo;
			}else {
				document.patientDonor.action = cpath+"/IVF/DonorRegistry.do?_method=update&mr_no="+mrNo;
			}
			document.patientDonor.submit();
		}
	</script>

</head>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<body class="yui-skin-sam">

	<c:if test="${param._method == 'add'}">
		<h1 style="float: left">Add Patient Donor</h1>
		<insta:patientsearch searchType="visit" searchUrl="DonorRegistry.do" activeOnly="true"
			buttonLabel="Find" searchMethod="getPatientVisitDetails" fieldName="patient_id"/>
		<insta:feedback-panel/>
	</c:if>
	<c:if test="${param._method == 'show'}">
		<div class="pageHeader">Edit Patient Donor</div>
	</c:if>
	<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
	<form name="patientDonor" method="post" action="${cpath}/IVF/DonorRegistry.do">
	<input type="hidden" name="_method" id="_method" value="${ifn:cleanHtmlAttribute(param._method)}">
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Donor Details</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Donor Status:</td>
				<td><insta:selectoptions name="donor_status" value="${bean.map.donor_status}"
				 		opvalues="A,I" optexts="Active,InActive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Referral Details:</td>
				<td><textarea name="referral_details">${bean.map.referral_details}</textarea></td>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
		<input type="submit" value="Save" class="button" onclick="return funSubmit();" />
		| <a href="${cpath}/IVF/DonorRegistry.do?_method=list">Patient Donor Registry</a>
		</div>
	</form>
	</body>
</html>