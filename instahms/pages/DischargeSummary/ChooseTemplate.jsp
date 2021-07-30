<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
<title>Choose Template - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js" />
	<script>
		function validateAdd() {
			if (document.addForm.template_id.value == "") {
				alert("Please select a template");
				return false;
			}
			return true;
		}
	</script>
</head>

<body>

	<h1>Add Discharge Summary</h1>
	<insta:patientdetails visitid="${param.patient_id}"/>

	<div class="dark bold" style="margin-top: 10px">Choose a template</div>

	<table class="dataTable " cellspacing="0" cellpadding="0" style="margin-top: 10px">
		<tr>
   		<th>Template Name</th>
   		<th>Format</th>
		</tr>

		<c:forEach var="record" items="${templates}">
			<tr>
				<td>
					<c:url var="addUrl" value="discharge.do">
						<c:choose>
							<c:when test="${record.format == 'T'}">
								<c:param name="_method" value="getTemplateContent"/>
								<c:param name="templateId" value="${record.id}"/>
								<c:param name="displayType" value="HTML"/>
							</c:when>
							<c:when test="${record.format == 'F'}">
								<c:param name="_method" value="getDischarge"/>
								<c:param name="form_id" value="${record.id}"/>
								<c:param name="displayType" value="FORM"/>
							</c:when>
							<c:when test="${record.format == 'EPF'}">
								<c:param name="_method" value="getDischarge"/>
								<c:param name="templateId" value="${record.id}"/>
								<c:param name="displayType" value="PDF"/>
							</c:when>
						</c:choose>
						<c:param name="patient_id" value="${param.patient_id}"/>
						<c:param name="templateType" value="${record.type}"/>
					</c:url>
					<a href='<c:out value="${addUrl}"/>' title="Add Discharge Summary Report">${record.caption}</a>
				</td>

				<td>
					<c:if test="${record.format == 'F'}">Fixed Fields (HVF)</c:if>
					<c:if test="${record.format == 'T'}"><span style="color: brown">Rich Text Template</span></c:if>
					<c:if test="${record.format == 'EPF'}"><span class="pdfform">Pdf Form Template</span></c:if>
				</td>
			</tr>
		</c:forEach>

		<tr>
			<c:url var="addUrl" value="discharge.do">
				<c:param name="_method" value="getUploadedFile"/>
				<c:param name="patient_id" value="${param.patient_id}"/>
				<c:param name="displayType" value="UPLOAD"/>
			</c:url>
			<td><a href='<c:out value="${addUrl}"/>'>Upload File</a></td>
			<td><span style="color: black">Any document</span></td>
		</tr>
	</table>

</body>
</html>

