<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title>Insurance Patients List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="Insurance/insurance.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="datetest.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

</head>
<body>
<div class="pageHeader">Upload Docs</div>
<insta:feedback-panel/>
<div>
	<c:choose>
		<c:when test="${not empty visit_id}">
			<insta:patientdetails  visitid="${visit_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${mr_no}" />
		</c:otherwise>
	</c:choose>
</div>
<form name="upload" method="POST" action="UploadReceivedDocs.do" enctype="multipart/form-data">
<input type="hidden" name="_method" id="_method" value="add">
<input type="hidden" name="insurance_id" id="insurance_id" value="${ifn:cleanHtmlAttribute(insurance_id)}">
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}">
<input type="hidden" name="visit_id" id="visit_id" value="${ifn:cleanHtmlAttribute(visit_id)}">

<div>
	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel">Upload</legend>
		<table class="formTable" width="100%">
			<tr>
				<td class="formlabel">Document Name:</td>
				<td class="forminfo"><input type="text" name="document_name" id="document_name"></td>
				<td></td><td></td><td></td><td></td>
			</tr>
			<tr>
				<td class="formlabel">Description:</td>
				<td class="forminfo"><input type="text" name="description" id="description"></td>
				<td></td><td></td><td></td><td></td>
			</tr>
			<tr>
				<td class="formlabel">Upload:</td>
				<td class="forminfo"><input type="file" name="doc_content" id="doc_content" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/></td>
				<td></td><td></td><td></td><td></td>
			</tr>
		</table>
	</fieldSet>
</div>
<div class="screenActions">
	<button type="button" name="save" id="save" accesskey="S" onclick="return funValidateAndUpload();"><b><u>S</u></b>ave</button> |
	<a href="javascript:void(0)" onclick="funClose()">Case List</a> |
	<a href="AddOrEditCase.do?_method=addshow&insurance_id=${ifn:cleanURL(insurance_id)}&visit_id=${ifn:cleanURL(visit_id)}">Case</a> |
	<a href="SendToTpa.do?_method=show&mr_no=${ifn:cleanURL(mr_no)}&visit_id=${ifn:cleanURL(visit_id)}&insurance_id=${ifn:cleanURL(insurance_id)}">Send</a> |
	<a href="InsuranceHistory.do?_method=show&mr_no=${ifn:cleanURL(mr_no)}&visit_id=${ifn:cleanURL(visit_id)}&insurance_id=${ifn:cleanURL(insurance_id)}">History</a>
</div>
</form>
</body>
</html>