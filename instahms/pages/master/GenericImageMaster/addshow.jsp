<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Patient Images - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<script type="text/javascript">
	function doCancel()
	{
		window.location.href="${cpath}/master/GenericImageMaster.do?_method=list";
	}
</script>

</head>
<body >
	<div class="pageHeader">Add Image</div>
	<insta:feedback-panel/>
	<form action="GenericImageMaster.do?_method=create" method="POST" enctype="multipart/form-data" accept="image/gif">
		<fieldset class="fieldSetBorder">
			<table class="formtable" >
				<tr>
					<td class="formlabel" >Image Name: </td>
					<td ><input type="text" name="image_name" id="image_name" class="required"
							title="Image Name is required." size="30"/></td>
					<td class="formlabel">&nbsp;</td>
					<td></td>
					<td class="formlabel">&nbsp;</td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel" style="white-space: nowrap">Upload File <b>(limit: 10MB)</b>: </td>
					<td ><input type="file" name="image_content" id="image_content" class="required"
						title="please upload the file."  accept="<insta:ltext key="upload.accept.medical_image"/>"/></td>
				</tr>
			</table>
		</fieldset>
		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S" name="save" id="save" ><b><u>S</u></b>ave</button></td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doCancel()">Generic Images List</a></td>
			</tr>
		</table>
	</form>
</body>
</html>
