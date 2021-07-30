<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Patient Images - Insta HMS</title>
<script>
	function deleteSelected(e) {
		var deleteEl = document.getElementsByName("delete_image");
		for (var i=0; i< deleteEl.length; i++) {
			if (deleteEl[i].checked) {
				return true;
			}
		}
		alert("select at least one image for delete");
		YAHOO.util.Event.stopEvent(e);
		return false;
	}
</script>
<c:set var="allDelPatDoc" value="${roleId == '1' || roleId == '2' || actionRightsMap['allow_delete_patient_doc'] == 'A'}" />
</head>
<body>
	<c:set var="imageslist" value="${pagedList}"/>
	<div class="pageHeader">Patient Images</div>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails  mrno="${param.mr_no}" />
	<form action="PatientGeneralImageAction.do" method="POST">
		<input type="hidden" name="_method" value="deleteImages"/>
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
		<div class="resultList" style="margin-top: 10px">
			<table class="dataTable" width="100%" cellspacing="0" cellpadding="0">
				<tr>
					<th style="margin-top: -5px"><input type="checkbox" name="checkAllForClose" onclick="return checkOrUncheckAll('delete_image', this)"/></th>
					<th>Image Name</th>
					<th>Content Type</th>
					<th>View</th>
				</tr>
				<c:forEach items="${imageslist}" var="image" varStatus="st">
					<tr class="${st.first ? 'firstRow' : ''}">
						<td><input type="checkbox" name="delete_image" id="delete_image" value="${image.image_id},${image.mr_no}" ${allDelPatDoc == true ? '': 'disabled' }/></td>
						<td>${image.image_name}</td>
						<td>${image.content_type}</td>
						<td><a href="${pageContext.request.contextPath}/${image.viewUrl}" target="_blank">View</a></td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<insta:noresults message="No Images found." hasResults="${not empty imageslist}"/>
		<div class="screensActions" style="display: ${not empty imageslist ? 'block' : 'none'}">
			<input type="submit" name="delete" value="Delete" onclick="return deleteSelected(event);">
		</div>
		<table class="screenActions">
			<tr>
				<c:url value="PatientGeneralImageAction.do" var="addurl">
					<c:param name="_method" value="addPatientImage"/>
					<c:param name="mr_no" value="${param.mr_no}"/>
				</c:url>
				<td><a href='<c:out value="${addurl}"/>'>Add Image</a></td>
			</tr>
		</table>
	</form>
</body>
</html>
