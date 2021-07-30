<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title> Edit MRD Case File - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="medicalrecorddepartment/editmrdcasefile.js"/>
<insta:link type="js" file="ajax.js"/>
</head>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Available"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<c:set target="${statusDisplay}" property="L" value="Lost"/>
<c:set target="${statusDisplay}" property="U" value="Issued"/>

<body class="yui-skin-sam" onload="">
<h1> Edit MRD Case File </h1>

<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${mrdfile.map.mr_no}" showClinicalInfo="true"/>
<form action="./MRDCaseFileIssue.do" method="POST">
	<input type="hidden" name="_method" value="updateCasefileStatus"/>
	<input type="hidden" name="mr_no" value="${mrdfile.map.mr_no}">
	<input type="hidden" name="mrdReturn" value="N"/>
	<input type="hidden" name="case_status" value="${mrdfile.map.case_status}"/>
	<input type="hidden" name="issued_on" value=""/>
	<input type="hidden" name="mrdscreen" value="${ifn:cleanHtmlAttribute(mrdscreen)}"/>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Patient Details</legend>
			<table class="formtable" cellpadding="0" cellspacing="0">
			<c:choose>
				<c:when test="${not empty inActiveVisitId}">
					<tr>
						<td class="formlabel">Last Visit No:</td><td class="forminfo">${inactivePatient.map.visit_id}</td>
						<td class="formlabel">Last Dept:</td><td class="forminfo">${inactivePatient.map.dept_name}</td>
						<td class="formlabel">Last Doctor:</td><td class="forminfo">${inactivePatient.map.doctor_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Last Ward:</td>
						<td class="forminfo">${inactivePatient.map.reg_ward_name == null || inactivePatient.map.reg_ward_name == ""  ?inactivePatient.map.alloc_ward_name:inactivePatient.map.reg_ward_name}</td>
						<td class="formlabel">Last Bed Type:</td>
						<td class="forminfo">${inactivePatient.map.alloc_bed_type == null || inactivePatient.map.alloc_bed_type == ""  ?inactivePatient.map.bill_bed_type:inactivePatient.map.alloc_bed_type}</td>
						<td class="formlabel">Last Bed Name:</td>
						<td class="forminfo">${inactivePatient.map.alloc_bed_name}:</td>
					</tr>
					<tr>
						<td class="formlabel">Last Date of Admission:</td><td class="forminfo"><fmt:formatDate value="${inactivePatient.map.reg_date}" pattern="dd-MM-yyyy"/></td>
						<td class="formlabel">Last Date of Discharge:</td><td class="forminfo"><fmt:formatDate value="${inactivePatient.map.discharge_date}" pattern="dd-MM-yyyy"/></td>
					</tr>
				</c:when>
				<c:when test="${not empty activeVisitId}">
					<tr>
						<td class="formlabel">Visit No:</td><td class="forminfo">${activePatient.map.visit_id}</td>
						<td class="formlabel">Dept:</td><td class="forminfo">${activePatient.map.dept_name}</td>
						<td class="formlabel">Doctor:</td><td class="forminfo">${activePatient.map.doctor_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Ward:</td>
						<td class="forminfo">${activePatient.map.reg_ward_name == null || activePatient.map.reg_ward_name == ""  ?activePatient.map.alloc_ward_name:activePatient.map.reg_ward_name}</td>
						<td class="formlabel">Bed Type:</td>
						<td class="forminfo">${activePatient.map.alloc_bed_type == null || activePatient.map.alloc_bed_type == ""  ?activePatient.map.bill_bed_type:activePatient.map.alloc_bed_type}</td>
						<td class="formlabel">Bed Name:</td>
						<td class="forminfo">${activePatient.map.alloc_bed_name}:</td>
					</tr>
					<tr>
						<td class="formlabel">Date of Admission:</td><td class="forminfo"><fmt:formatDate value="${activePatient.map.reg_date}" pattern="dd-MM-yyyy"/></td>
						<td class="formlabel">Date of Discharge:</td><td class="forminfo"><fmt:formatDate value="${activePatient.map.discharge_date}" pattern="dd-MM-yyyy"/></td>
					</tr>
				</c:when>
			</c:choose>
			<tr>
				<td class="formlabel">Case File No:</td><td <c:if test="${mlc_status == 'Y' }">class="mlcIndicator"</c:if>><b>${patient.casefile_no}</b>
					<c:if test="${mrdfile.map.recreated}">(Recreated)</c:if>
				</td>
			</tr>
			</table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Case file status</legend>
		<table class="formtable">
			<input type="hidden" name="issuedID" value="${mrdissueBean.map.issued_id}">
			<tr>
				<td class="formlabel">Case Status:</td>
				<td><input type="checkbox" name="caseStatus" <c:if test="${mrdissueBean.map.case_status == 'I'}">checked</c:if>/>Inactive</td>
			</tr>
			<tr>
				<td class="formlabel">Current File Status:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${mrdissueBean.map.file_status =='A'}">
								Available With MRD
						</c:when>
						<c:when test="${mrdissueBean.map.file_status == 'U'}">
							Issued to User
						</c:when>
						<c:when test="${mrdissueBean.map.file_status == 'L'}">
							Lost
						</c:when>
						<c:otherwise>
						</c:otherwise>
						</c:choose>
				</td>
			</tr>
			<c:if test="${mrdissueBean.map.file_status !='L'}">
			<tr>
				<td class="formLabel">New File Status</td>
				<td ><input type="checkbox" name="file_status" value="L" id="file_status">Lost</td>
			</tr>
			</c:if>
			<c:if test="${mrdissueBean.map.file_status =='L'}">
			<tr>
				<td class="formlabel">Recreated :</td>
				<td>
					<input type="checkbox" name="recreated" id="recreated" value="Y"/>Yes
				</td>

			</tr>
			</c:if>

			</tr>
		</table>
	</fieldset>
	<table class="screenActions">
		<tr>
			<td><button type="button" accesskey="S" name="save" onclick="return onSaveValidate();"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="backToSearch();return true;">MRD Case Files Search</a></td>
		</tr>
	</table>

</form>
</body>
</html>
