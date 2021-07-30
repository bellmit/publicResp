<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<c:set var="pagePath" value="<%=URLRoute.STOCK_UPLOAD_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<title>Master Data Upload-Insta HMS</title>
<script>
	function doUpload() {
	
	   var importForm = document.stockUploadImportForm;
		if (importForm.uploadFile.value == "") {
			alert("Please browse and select a file to upload");
			return false;
		}
		importForm.submit();
	 }
</script>
</head>
<body>
	<h1>Master Data Migration</h1>
	<insta:feedback-panel/>
	<c:url var="url" value="${pagePath}">
	</c:url>
	<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Initial Stock Upload</legend>
		<table>
			<tr style="padding: 5px"><td></td><td></td></tr>
			<tr>
				<td>Import Stock Data:</td>
				<td>
					<form name="stockUploadImportForm" action="${cpath}${pagePath}/import.htm" method="POST"
						enctype="multipart/form-data">
						<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
						<button type="button" onclick="return doUpload()">Upload</button>
					</form>
				</td>
			</tr>
			<tr>	
				<td>
					<a href="<c:out value='${url}/template.htm' />"> Stock Upload Sample Template</a>
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Note: This is the template of stock upload csv file."/>
				</td>
				<td></td>
			</tr>
		</table>
		<table>
			<tr><td>&nbsp;</td></tr>
			<tr align="left">
				<td class="label"><font color="#F00"><insta:ltext key="bulk.stockupload.import.note"/></font></td>
			</tr>
		</table>
	</fieldset>
  </body>
</html>