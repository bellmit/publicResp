<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Patient Docket List - Insta HMS</title>
	<insta:link type="js" file="/emr/patientdocket.js"/>
	<script>
		var contextPath = '${pageContext.request.contextPath}';
	</script>
</head>

<body>
	<div class="pageHeader">Patient Documents</div>
	<span align="center" >${ifn:cleanHtml(msg)}</span>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>

	<c:choose>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${param.mr_no}" showClinicalInfo="true"/>
		</c:otherwise>
	</c:choose>

	<form action="PatientDocket.do" method="GET" autocomplete="off">
		<input type="hidden" name="_method" value="printDocket"/>
		<table width="100%" style="border-bottom: 0px">
			<tr><td>
				<table class="dataTable" style="margin-bottom:0px" cellspacing="0" cellpadding="0" width="100%">
					<tr>
						<th style="padding-top: 0px;padding-bottom: 0px"><input type="checkbox" name="checkAllForClose" onclick="return checkOrUncheckAll('printDocument', this)"/></th>
						<th>Visit No</th>
						<th>Document Name</th>
						<th>Document Type</th>
						<th>Date</th>
						<th>User</th>
					</tr>
					<c:forEach items="${docList}" var="pdocument" varStatus="status">
						<tr class="${status.first ? 'firstRow' : ''}">
							<td>
								<input type="checkbox" ${(pdocument.pdfSupported==true &&  pdocument.authorized==true)?'':'disabled'}
									name="printDocument" id="printDocument"
									value="${pdocument.docid},${pdocument.provider.providerName},${pdocument.printerId}"/>
							</td>
							<td>${pdocument.visitid}</td>
							<td>${pdocument.title}</td>
							<td>${docTypes[pdocument.type].map.doc_type_name}</td>
							<td><fmt:formatDate pattern="dd-MM-yyyy" value="${pdocument.date}"/></td>
							<td>${pdocument.updatedBy}</td>
						</tr>
					</c:forEach>
				</table>
			</td></tr>
		</table>
		<insta:noresults message="No documents found for MR No. ${param.mr_no}" hasResults="${not empty docList}"/>
		<table class="screenActions">
			<tr>
				<td colspan="7" align="center"><button type="button" name="printdocket" accesskey="P"
					onclick="return checkDocuments();" ${empty docList?'disabled':''} target="_blank"><b><u>P</u></b>rint Docket</button></td>
			</tr>

		</table>
	</form>
</body>
</html>
