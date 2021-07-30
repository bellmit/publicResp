<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance - Insta HMS</title>
	<insta:link type="script" file="Insurance/insurance.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<script>
		function dashboard() {
			window.location.href = "${cpath}/Insurance/InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true";
	}
	</script>
</head>

<body>
	<c:set var="hasResults" value="${not empty docList}"/>
	<div class="pageHeader">Send Message</div>
	<insta:feedback-panel/>
	<c:choose>
		<c:when test="${not empty patient_id}">
			<insta:patientdetails  visitid="${patient_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${mr_no}" />
		</c:otherwise>
	</c:choose>

<form action="SendToTpa.do" method="GET" name="SendToTpaForm">
<input type="hidden" name="_method" value="sendToTpa"/>
<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(insurance_id)}">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}">
<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(patient_id)}">
<div>
	<fieldset class="fieldSetBorder">
		<table class="formtable" >
			<tr>
				<td class="formlabel">To : </td>
				<td class="forminfo"><input type="text" name="email_to" id="email_to" size="60" value="${TPAEmailIds.map.email_id}"/>(comma separated mail id's)</td>
				<td></td><td></td><td></td><td></td>
			</tr>
			<tr>
				<td class="formlabel">CC : </td>
				<td class="forminfo"><input type="text" name="email_cc" id="email_cc" size="60"/>(comma separated mail id's)</td>
				<td></td><td></td><td></td><td></td>
			</tr>
			<tr>
				<td class="formlabel">Subject : </td>
				<td class="forminfo"><input type="text" name="email_subject" id="email_subject" size="80"/></td>
				<td></td><td></td><td></td><td></td>
			</tr>
			<tr>
				<td class="formlabel">Message : </td>
				<td class="forminfo"><textarea name="email_body" id="email_body" rows="5" cols="50"></textarea></td>
				<td></td><td></td><td></td><td></td>
			</tr>
		</table>
	</fieldset>
</div>
<div class="resultList">
	<table class="resultList" align=left >
		<tr>
			<th></th>
			<th>Document Name</th>
			<th>Document Type</th>
			<th>Date</th>
			<th>User</th>
			<th>Action</th>
		</tr>
		<c:forEach items="${docList}" var="pdocument">
			<tr>
				<td>
				<c:choose>
					<c:when test="${not empty pdocument.provider.providerName}">
						<input type="checkbox"
						name="sendToTpa" id="sendToTpa"
						value="${pdocument.docid},${pdocument.provider.providerName},${pdocument.title},${pdocument.printerId}"/>
					</c:when>
					<c:otherwise>
						<input type="checkbox" }
						name="sendToTpa" id="sendToTpa"
						value="${pdocument.docid},InsuranceProvider,${pdocument.title},${pdocument.printerId}"/>
					</c:otherwise>
				</c:choose>
				</td>
				<td>${pdocument.title}</td>
				<td>${docTypes[pdocument.type].map.doc_type_name}</td>
				<td><fmt:formatDate pattern="dd-MM-yyyy" value="${pdocument.date}"/></td>
				<td>${pdocument.updatedBy}</td>
				<td><a href="${cpath}${pdocument.displayUrl}" target="_blank">View</a></td>
			</tr>
		</c:forEach>
	</table>
</div>
<c:if test="${!hasResults}">
	<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
		<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
			<img src="${cpath}/images/alert.png"/>
		</div>
		<div style="float: left; margin-top: 10px">
			${not empty message ? message : 'No Documents Available to be Sent'}
		</div>
	</div>
</c:if>
<div>
	<c:if test="${not empty docList}">
			<a href="javascript:selectAll()">Select All</a> <b>|</b> <a href="javascript:unSelectAll()">Unselect All</a>
	</c:if>
</div>
<div class="screenActions">
	<button type="button" name="Send" accesskey="S" onclick="checkDocuments();"><b><u>S</u></b>end</button> |
	<a href="javascript:void(0)" onclick="dashboard();">Case List</a> |
	<c:choose>
		<c:when test="${not empty patient_id}">
			<a href="AddOrEditCase.do?_method=addshow&insurance_id=${ifn:cleanURL(insurance_id)}&visit_id=${ifn:cleanURL(patient_id)}">Case</a> |
			<a href="UploadReceivedDocs.do?_method=add&visit_id=${ifn:cleanURL(patient_id)}&insurance_id=${ifn:cleanURL(insurance_id)}&mr_no=${ifn:cleanURL(mr_no)}">Upload</a> |
		</c:when>
		<c:otherwise>
			<a href="AddOrEditCase.do?_method=addshow&insurance_id=${ifn:cleanURL(insurance_id)}">Case</a> |
			<a href="UploadReceivedDocs.do?_method=add&mr_no=${ifn:cleanURL(mr_no)}&insurance_id=${ifn:cleanURL(insurance_id)}">Upload</a> |
		</c:otherwise>
	</c:choose>
	<a href="InsuranceHistory.do?_method=show&mr_no=${ifn:cleanURL(mr_no)}&visit_id=${ifn:cleanURL(patient_id)}&insurance_id=${ifn:cleanURL(insurance_id)}">History</a>
</div>
	</form>
</body>
</html>
