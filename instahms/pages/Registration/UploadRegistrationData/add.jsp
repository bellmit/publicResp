<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="org.apache.struts.Globals"%>
<html>
<head>
<title><insta:ltext key="registration.bulkregistration.add.registrationdataupload.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	function funValidateAndUpload(){
	var doc_content = document.getElementById("csvFile").value;
	if(doc_content == ""){
		alert("Please upload file(s) to save");
		document.forms[0].csvFile.focus();
		return false;
	}
	return true;
}

</script>
</head>
<body>

<form name="RegistrationFormData" method="POST" action="<%=request.getContextPath()%>/Registration/UploadRegistrationData.do" enctype="multipart/form-data">
<input type="hidden" name="method" id="method" value="importRegistrationCSVData">
<div class="pageHeader"><insta:ltext key="registration.bulkregistration.add.bulkupload"/></div>
<insta:feedback-panel/>
	<table width="100%"  align="center">
		<tr align="center">
			<td>
				<table width="100%">
					<tr align="center">
						<td ><insta:ltext key="registration.bulkregistration.add.registrationdata"/>: <input type="file" name="csvFile" id="csvFile" accept="<insta:ltext key="upload.accept.master"/>"></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr height="20"/>
		<tr align="center">
			<td>
				<button type="submit" name="save" id="save"  clsss="button" accesskey="U"  onclick="return funValidateAndUpload();"><label><u><b><insta:ltext key="registration.bulkregistration.add.u"/></b></u><insta:ltext key="registration.bulkregistration.add.pload"/></label></button>&nbsp;
		</tr>
		<tr height="20"/>
		<tr align="left"><td class="label"><insta:ltext key="registration.bulkregistration.add.note"/></td></tr>
	</table>
</form>

</body>
</html>
