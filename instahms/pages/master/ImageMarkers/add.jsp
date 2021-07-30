<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Image Marker - Insta HMS</title>


</head>
<body >

<form action="create.htm" method="POST" name="ImageMarkerForm" enctype="multipart/form-data">
	<h1>Add Image Marker</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Label:</td>
				<td>
					<input type="text" name="label" id="label" class="required" title="Label is mandatory."/>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="A" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Image:</td>
				<td>
					<input type="file" name="file_content" id="file_content"
						class="required"
						title="Please upload the image." accept="<insta:ltext key="upload.accept.image"/>"/><b>(Upload limit: 10MB)</b>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" ><b><u>S</u></b>ave</button>
		| <a href="list.htm?" title="Markers List">Markers List</a>
	</div>
</form>

</body>
</html>
