<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insurance Claim</title>
<insta:link type="css" file="widgets.css"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<script>
	function dashboard() {
		window.location.href = "${cpath}/Insurance/InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true";
	}
</script>
</head>

<body>
	<div class="pageHeader">Claim Template</div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>
	<insta:feedback-panel/>

	<c:choose>
		<c:when test="${not empty ClaimDetails.map.patient_id}">
			<insta:patientdetails  visitid="${ClaimDetails.map.patient_id}"/>
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${ClaimDetails.map.mr_no}"/>
		</c:otherwise>
	</c:choose>
	<form action="InsuranceClaim.do?_method=addOrEditRTF" method="POST" enctype="multipart/form-data">

		<input type="hidden" name="insurance_id" id="insurance_id" value="${ClaimDetails.map.insurance_id}">
		<input type="hidden" name="mode" id="mode" value="${ifn:cleanHtmlAttribute(mode)}">

		<div>
		<fieldset class="fieldSetBorder" >
			To edit a claim, do the following steps:
			<ol align="left">
				<li>Download (if a claim has been generated earlier) or Generate the claim based on the bill</li>
				<li>Save the generated claim anywhere on the local disk</li>
				<li>Open and edit the claim using an editor that supports RTF
						(eg, Wordpad, MS Word, OpenOffice)</li>
				<li>Save the claim in the editor</li>
				<li>Upload the saved file. Now, the copy on the local disk can be deleted.</li>
			</ol>

		<table class="formtable">
			<tr>
				<c:url value="InsuranceClaim.do" var="genurl">
					<c:param name="_method" value="generate"/>
					<c:param name="insurance_id" value="${ClaimDetails.map.insurance_id}"/>
				</c:url>
				<c:choose>
					<c:when test="${not empty ClaimDetails.map.claim_docs_id}">
						<td align="right">Current File: </td>
						<c:url value="InsuranceClaim.do" var="url">
							<c:param name="_method" value="view"/>
							<c:param name="insurance_id" value="${ClaimDetails.map.insurance_id}"/>
						</c:url>
						<td align="left">
								<a href="<c:out value='${url}'/>" target="_blank"><b>Download</b></a> |
								<a href="${genurl}" target="_blank"><b>Regenerate</b></a>
						</td>
					</c:when>
					<c:otherwise>
						<td><a href="${genurl}" target="_blank"><b>Generate</b></a></td>
					</c:otherwise>

				</c:choose>
			</tr>
			<tr>
				<td align="right">Upload File <b>(limit: 10MB)</b>: </td>
				<td align="left"><input type="file" name="doc_content_rtf" id="doc_content_rtf" accept="<insta:ltext key="upload.accept.rtf"/>"></td>
			</tr>
		</table>
		</fieldset>
	</div>
	<div class="screenActions">
		<input type="submit" name="save" id="save" value="Save"  /> |
		<a href="javascript:void(0)" onclick="dashboard();">Case List</a>
	</div>
	</form>
</body>
</html>
