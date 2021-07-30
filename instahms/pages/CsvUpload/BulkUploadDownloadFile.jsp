<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<jsp:useBean id="detailDisplay" class="java.util.HashMap" />
<%@ page import="com.insta.hms.integration.URLRoute"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="masters/upload_download_common.js" />
<%-- <insta:link type="js" file="registration/quickEstimate.js" /> --%>
<insta:link type="js" file="common.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<title>Bulk Upload Download</title>
</head>
<body onload="initPage()">
	<form name="fileUploadDownloadForm"
		action="${cpath}/bulkUploadDownload/uploadDownloadFiles.htm"
		method="POST" enctype="multipart/form-data">
		<div class="pageHeader">Bulk Upload Download</div>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Upload Download Details</legend>
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">Master Name :</td>
					<td class="forminfo"><select name="master" id="master"
						class="dropdown" onchange="onChangeMasterNameAction()">
					</select></td>
					<td class="formlabel">Action :</td>
					<td class="forminfo"><select name="action" id="action"
						class="dropdown" onchange="onChangeAction();onChangeMasterNameAction()">
							<option value="upload">Upload</option>
							<option value="download">Download</option>
					</select></td>
					<td class="formlabel" id="templateLabel">Template :</td>
					<td class="forminfo" ><input id="template" name="template" type="checkbox"
						 onchange="onChangeTemplate()" value="N">
					</td>
					<td class="formlabel" id="fileUploadLabel">Upload File:</td>
					<td class="forminfo" colspan="2"><input type="file"
						name="fileUpload" id="fileUpload"
						accept="<insta:ltext key="upload.accept.master"/>" /></td>
				</tr>
				<tr>
					<td class="formlabel">Is charges :</td>
					<td class="forminfo"><input type="checkbox" name="isCharges"
						id="isCharges" onchange="onChangeOrganizationId()" value="N">
					</td>
					<td class="formlabel" id="orgLabel">Rate Sheet :</td>
					<td id="orgId"><select id="organization" name="organization"
						class="dropdown">
					</select></td>
				</tr>
				<tr>
					<td class="formlabel" id="codeSystemCategoryLabel">Code System Category :</td>
					<td class="forminfo" id="codeSystemCategoryId"><insta:selectdb name="code_system_category_id"
							id="code_system_category_id" value="${param.code_system_category_id}"
							table="code_system_categories" valuecol="id" orderby="label"
							displaycol="label" filtered="true" filtercol="status"
							filtervalue="A" dummyvalue="-- Select --" />
					</td>
					<td class="formlabel" id="codeSystemsLabel">Code System :</td>
					<td id="codeSystemId"><insta:selectdb name="code_systems_id" id="code_systems_id"
							value="${param.code_systems_id}" table="code_systems" valuecol="id"
							orderby="label" displaycol="label" filtered="true"
							filtercol="status" filtervalue="A" dummyvalue="-- Select --" />
					</select></td>
				</tr>
				<tr>
					<td><button name="Submit" value="Submit"
							onclick="return validateImportFile();">Submit</button></td>
				</tr>
			</table>
		</fieldset>
	</form>
	<form>
		<div class="resultList">
			<br> <b>Upload Download Dashboard</b>
			<table class="resultList" cellpadding="0" cellspacing="0"
				id="resultTable">
				<tr>
					<th>Master Name</th>
					<th>Action</th>
					<th>Status</th>
					<th>Started At</th>
					<th>Completed At</th>
					<th>View / Download</th>
				</tr>
				<c:forEach var="csvUpload" items="${csvUploadList}">
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${csvUpload.status == 'completed'}">green</c:when>
							<c:when test="${csvUpload.status == 'queued'}">yellow</c:when>
							<c:when test="${csvUpload.status == 'fail'}">red</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>
					<tr
						class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}">
						<td>${csvUpload.master}</td>
						<td>${csvUpload.action}</td>
						<c:choose>
							<c:when test="${csvUpload.status == 'completed'}">
								<td><img class="flag"
									src="${cpath}/images/${flagColor}_flag.gif" />
									Completed</td>
								<td>${csvUpload.startedAt}</td>
								<td>${csvUpload.completedAt}</td>
								<td><a
									href=${cpath}/${pagePath}bulkUploadDownload/download.htm?id=${csvUpload.id}>Download</a></td>
							</c:when>
							<c:when test="${csvUpload.status == 'fail'}">
								<td><img class="flag"
									src="${cpath}/images/${flagColor}_flag.gif" />
									Failed</td>
								<td>${csvUpload.startedAt}</td>
								<td>${csvUpload.completedAt}</td>
								<td><a
									href=${cpath}/${pagePath}bulkUploadDownload/download.htm?id=${csvUpload.id}>View
										Log</a></td>
							</c:when>
							<c:otherwise>
								<td><img class="flag"
									src="${cpath}/images/${flagColor}_flag.gif" />
									${csvUpload.status}</td>
								<td>${csvUpload.startedAt}</td>
								<td>${csvUpload.completedAt}</td>
								<td>Please Wait</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div class="legend">
			<div class="flag">
				<img src='${cpath}/images/green_flag.gif'>
			</div>
			<div class="flagText">Completed</div>
			<div class="flag">
				<img src='${cpath}/images/yellow_flag.gif'>
			</div>
			<div class="flagText">Queued</div>
			<div class="flag">
				<img src='${cpath}/images/yellow_flag.gif'>
			</div>
			<div class="flagText">In-Process</div>
			<div class="flag">
				<img src='${cpath}/images/red_flag.gif'>
			</div>
			<div class="flagText">Failed</div>
		</div>
	</form>
</body>
</html>
