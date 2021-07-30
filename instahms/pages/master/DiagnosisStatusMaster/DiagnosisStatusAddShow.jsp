<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit Diagnosis Status - Insta HMS</title>
	<script>

	</script>
</head>
<body class="yui-skin-sam">
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Diagnosis Status</h1>
	<insta:feedback-panel/>
	<form action="DiagnosisStatusAction.do">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>
		<input type="hidden" name="diagnosis_status_id" value="${bean.map.diagnosis_status_id}">
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Diagnosis Status Name: </td>
					<td><input type="text" name="diagnosis_status_name" value="${bean.map.diagnosis_status_name}" class="required" title="Diagnosis Status Name is mandatory."></td>
					<td class="formlabel"> </td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td>
						<select name="status" class="dropdown validate-not-first" title="Please select the status.">
							<option value="">-- Select --</option>
							<option value="A" ${bean.map.status == 'A' || param._method == 'add' ? 'selected' : ''}>Active</option>
							<option value="I" ${bean.map.status == 'I' ? 'selected' : ''}>Inactive</option>
						</select>
					</td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save"   ${bean.map.diagnosis_status_id < 0  ? 'disabled' : ''} />
						<c:if test="${param._method == 'show'}">
							| <a href="DiagnosisStatusAction.do?_method=add">Add</a>
						</c:if>
						| <a href="DiagnosisStatusAction.do?_method=list&sortOrder=diagnosis_status_name&sortReverse=false">List</a>
					</td>
				</tr>
			</table>
	</form>
</body>
</html>