<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Tooth Surface Status Master - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>
	</head>
	<body>
		<c:choose>
			<c:when test="${param._method !='add'}">
				<h1>Edit Tooth Surface Status</h1>
			</c:when>
			<c:otherwise>
				<h1>Add Tooth Surface Status</h1>
			</c:otherwise>
		</c:choose>

	<form action="ToothSurfaceStatus.do" name="editToothsurStatusForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="surface_status_id" value="${bean.map.surface_status_id}"/>
	</c:if>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder"><legend class="fieldSetLabel">Tooth Surface Status Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Surface Status Name:</td>
			<td>
				<input type="text" name="surface_status_name"  value="${bean.map.surface_status_name}" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			</td>
			<c:if test="${param._method != 'add' }">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="${cpath}/master/ToothSurfaceStatus.do?_method=add">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/ToothSurfaceStatus.do?_method=list&sortOrder=surface_status_name&sortReverse=false&status=A";">
			Tooth Surface Status List</a></td>
		</tr>
	</table>
</form>
</body>
</html>
