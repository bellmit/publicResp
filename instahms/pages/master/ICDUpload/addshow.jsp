<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>ICD Upload Master - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<script>
	function doClose() {
			window.location.href = "${cpath}/master/icdcodes.htm";
	}
</script>
</head>
<body>
	<div class="pageHeader">
		Upload ICD Master Data
	</div>
	<insta:feedback-panel/>
	<div class="infoPanel">
		<div class="img"><img src="${cpath}/images/information.png"/></div>
		<div class="txt">Please upload a csv file which has ICD Code followed by Description</div>
		<div style="clear: both"></div>
	</div>
	<form action="./ICDUpload.do" method="POST" enctype="multipart/form-data">
		<input type="hidden" name="_method" value="upload"/>
		<fieldset class="fieldSetBorder">
		<table class="formtable" width="54%">

			<tr>
				<td>Code Type:</td>
				<td><insta:selectdb name="icd_type" table="mrd_supported_code_types"
					displaycol="code_type" valuecol="code_type" class="dropdown" dummyvalue="..Code Type.." dummyvalueId=""/>
				</td>
			</tr>

			<tr>
				<td style="width: 63px;">Upload ICD Codes File <b>(limit: 10MB)</b>: </td>
				<td>
					<input type="file" name="icd_upload_file_content" class="required" title="Please upload the file." accept="<insta:ltext key="upload.accept.master"/>"/>
				</td>
			</tr>
		</table>
		</fieldset>
		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></</td>
			<td>&nbsp;|</td>
			<td>&nbsp;<a href="javascript:void(0)" onclick="doClose();">ICD Codes List</a></td>
		</tr>
	  </table>
	</form>
</body>
</html>
