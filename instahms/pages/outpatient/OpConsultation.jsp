<%@ page import = "java.util.HashMap" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>OP Case Form - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">

	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="outpatient/opconsultation.js"/>
	<style>
		tr.deleted {background-color: #F2DCDC; color: gray; }
	</style>
	<script>

	</script>
</head>

<body>

<div class="pageHeader">OP Case Form</div>
<span align="center"><insta:feedback-panel/></span>

<insta:patientdetails visitid="${consultation.patient_id}"/>

<form action="OpConsultAction.do">
<input type="hidden" name="method" value="renameOrDelete"/>
<input type="hidden" name="consultation_id" value="${consultation.consultation_id}"/>
	<c:choose><c:when test="${not empty consultationForms}">
		<div align="center">Filled in Case forms:</div>
		<table align="center" class="dashboard" id="addedFormsTable" style="margin-bottom: 0px">
			<tr>
				<th>Form Name</th>
				<th>Edit</th>
				<th>Print</th>
				<th>Rename</th>
				<th>Delete</th>
			</tr>

			<c:forEach items="${consultationForms}" var="cons" varStatus="status">
				<c:set var="index" value="${status.index+1}"/>
				<tr>
					<td><label id="cons_form_title" name="cons_form_title">${cons.map.form_name}</label>
						<input type="text" name="form_name" id="form_name" value="${cons.map.form_name}" style="display: none"/>
						<input type="hidden" name="consult_form_id" value="${cons.map.consultation_form_id}"></td>
					<td>
						<a href="OpConsultAction.do?method=edit&consultation_form_id=${cons.map.consultation_form_id}">Edit</a>
					</td>
					<td>
						<a target="_blank"
							href="OpConsultAction.do?method=view&consultation_form_id=${cons.map.consultation_form_id}">Print</a>
					</td>
					<td><input type="checkbox" name="renameForm" onchange="return hideShowInputBox(this, '${ifn:cleanJavaScript(index)}')"/>
						<input type="hidden" name="rename" value="N"/></td>
					<td><input type="checkbox" name="deleteForm" value="${cons.map.consultation_form_id}" onchange="return changeRowColor(this, '${ifn:cleanJavaScript(index)}')"/>
						</td>
				</tr>
			</c:forEach>
			<tr><td colspan="5" align="right"><input type="button" name="save" value="Save" onclick="return renameOrDeleteForms();">
					</td></tr>
		</table>

	</c:when>
	<c:otherwise>
		<div align="center">
			No case forms found. Choose Add to enter a case form.
		</div>
	</c:otherwise></c:choose>
</form>
<div align="center" style="margin-top: 2em">
	Add another Case Form:
	<table class="dashboard">
		<tr>
			<insta:sortablecolumn name="title" title="Case Form Templates"/>
		</tr>

		<c:url value="OpConsultAction.do" var="addUrl">
			<c:param name="method" value="add"/>
			<c:param name="consultation_id" value="${consultation.consultation_id}"/>
		</c:url>
		<c:choose>
			<c:when test="${not empty forms.dtoList}">
				<c:forEach items="${forms.dtoList}" var="form">
					<tr>
						<td><a href="<c:out value='${addUrl}' />"&form_id=${form.map.form_id}" title="Add ${form.map.title} Form">
							${form.map.title}</a></td>
					</tr>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<tr><td>
					No Case Forms found.
				</tr>
			</c:otherwise>
		</c:choose>
	</table>
	<insta:paginate numPages="${forms.numPages}" curPage="${forms.pageNumber}" totalRecords="${pagedList.totalRecords}"/>
</div>

<table align="center" cellpadding="5">
	<tr>
		<td>
			<c:url var="dashboardUrl" value="/outpatient/OpListAction.do">
				<c:param name="method" value="list"/>
				<c:param name="status" value="O"/>
				<c:param name="sort_reverse" value="true"/>
			</c:url>
			<a href="${dashboardUrl}">Patient List</a>
		</td>
		<td>
			<a href="${cpath}/outpatient/OpPrescribeAction.do?method=list&consultation_id=${consultation.consultation_id}">Consult &amp; Prescribe</a>
		</td>
		<td>
			<a href="${cpath}/vitalForm/genericVitalForm.do?method=show&visitId=${consultation.patient_id}&mrno=${consultation.mr_no}">Vital Form</a>
		</td>
		<td><insta:screenlink screenId="emr_screen" extraParam="?method=list&MrNo=${consultation.mr_no}"
			label="EMR View"/>
		</td>
	</tr>
</table>

</body>
</html>

