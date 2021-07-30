<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Image - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="genericdocuments/addpatientimage.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<script>
	var contextPath = '${cpath}';
</script>
</head>
<body >
	<h1>Add Image</h1>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails  mrno="${param.mr_no}" />
	<form action="PatientGeneralImageAction.do?_method=createPatientImage" method="POST" enctype="multipart/form-data">
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
		<table class="formtable" style="margin-top: 10px">
			<tr>
				<td class="formlabel">Image Name: </td>
				<td >
					<input type="text" name="image_name" id="image_name" class="required"
						title="Image Name is required." size="30"/>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel" style="white-space: nowrap">Upload File <b>(limit: 10MB)</b>: </td>
				<td >
					<input type="file" name="image_content" id="image_content" class="required"
						title="please upload the file." accept="<insta:ltext key="upload.accept.medical_image"/>"/>
				</td>
			</tr>
		</table>
		<table class="screenActions">
			<tr><td colspan="2" align="center"><input type="submit" value="Save" name="save" id="save" />
			| <a href="#" onclick="return patientimages();">Patient Images</a>
			</td></tr>
		</table>
	</form>
</body>
</html>
