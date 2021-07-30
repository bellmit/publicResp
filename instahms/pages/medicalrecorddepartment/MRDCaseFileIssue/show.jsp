<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>MRD Case File User Issue Log- Insta HMS</title>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>

	<style type="text/css">
		.status_Available{background-color: }
		.status_Inactive{background-color: #GEFCBB}
		.status_Lost{background-color: #000FFF}
		.status_Issued{background-color: #BBBCCC}
	</style>
	<script>
		function doClose() {
			var screen = '${ifn:cleanJavaScript(param.mrdscreen)}';
			if(screen == 'issue') {
				window.location.href = '${cpath}/medicalrecorddepartment/MRDCaseFileIssue.do?_method=list&mrdscreen='+screen+'&case_status=';
			}else {
				window.location.href = '${cpath}/medicalrecorddepartment/MRDCaseFileReturn.do?_method=list&mrdscreen='+screen+'&case_status=';
			}
		}
	</script>
</head>
<body>
<h1>MRD Case File Issue Log  </h1>
<insta:patientgeneraldetails mrno="${mrdfile.map.mr_no}" />
<form>
	<input type="hidden" name="_method">
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Other Details</legend>
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
					<c:choose>
						<c:when test="${not empty inActiveVisitId}">
							<tr>
								<td class="formlabel">Last Visit No:</td><td class="forminfo">${inactivePatient.map.visit_id}</td>
								<td class="formlabel">Last Dept:</td><td class="forminfo">${inactivePatient.map.dept_name}</td>
								<td class="formlabel">Last Doctor:</td><td class="forminfo">${inactivePatient.map.doctor_name}</td>
							</tr>
							<tr>
								<td class="formlabel">Last Ward:</td><td class="forminfo">${inactivePatient.map.reg_ward_name == null || inactivePatient.map.reg_ward_name == ""  ?inactivePatient.map.alloc_ward_name:inactivePatient.map.reg_ward_name}</td>
								<td class="formlabel">Last Bed Type:</td><td class="forminfo">${inactivePatient.map.alloc_bed_type == null || inactivePatient.map.alloc_bed_type == ""  ?inactivePatient.map.bill_bed_type:inactivePatient.map.alloc_bed_type}</td>
								<td class="formlabel">Last Bed Name:</td><td class="forminfo">${inactivePatient.map.alloc_bed_name}</td>
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
								<td class="formlabel">Ward:</td><td class="forminfo">${activePatient.map.reg_ward_name == null || activePatient.map.reg_ward_name == ""  ?activePatient.map.alloc_ward_name:activePatient.map.reg_ward_name}</td>
								<td class="formlabel">Bed Type:</td><td class="forminfo">${activePatient.map.alloc_bed_type == null || activePatient.map.alloc_bed_type == ""  ?activePatient.map.bill_bed_type:activePatient.map.alloc_bed_type}</td>
								<td class="formlabel">Bed Name:</td><td class="forminfo">${activePatient.map.alloc_bed_name}</td>
							</tr>
							<tr>
								<td class="formlabel">Date of Admission:</td><td class="forminfo"><fmt:formatDate value="${activePatient.map.reg_date}" pattern="dd-MM-yyyy"/></td>
								<td class="formlabel">Date of Discharge:</td><td class="forminfo"><fmt:formatDate value="${activePatient.map.discharge_date}" pattern="dd-MM-yyyy"/></td>
							</tr>
						</c:when>
					</c:choose>
					<tr>
						<td class="formlabel">Old MR No:</td><td class="forminfo">${patient.oldmrno}</td>
						<td class="formlabel">Case File No:</td><td class="forminfo">${patient.casefile_no}
							<c:if test="${mrdfile.map.recreated}">(Recreated)</c:if>
						</td>
					</tr>
				</table>
			</fieldset>
			<div class="resultList">
				<table class="dashboard" width="100%">
					<tr>
						<th>Issue Date</th>
						<th>Issued To</th>
						<th>Purpose</th>
						<th>Issued By</th>
						<th>Return Date</th>
						<th>Received By</th>
					</tr>
					<c:forEach var="record" items="${issueLoglist}">
						<tr>
							<td><fmt:formatDate value="${record.map.issued_on}" pattern="dd-MM-yyyy HH:mm"/></td>
							<td>${record.map.issued_to}</td>
							<td>${record.map.purpose}</td>
							<td>${record.map.issue_user}</td>
							<td><fmt:formatDate value="${record.map.returned_on}" pattern="dd-MM-yyyy HH:mm"/></td>
							<td>${record.map.return_user}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="screenActions"><a href="javascript:void(0)"onclick="doClose()">MRD Case Files</a></div>
	</form>
</body>
</html>