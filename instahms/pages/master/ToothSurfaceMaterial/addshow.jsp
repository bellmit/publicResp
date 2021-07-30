<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Tooth Surface Material Master - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>
	</head>
	<body>
		<c:choose>
			<c:when test="${param._method !='add'}">
				<h1>Edit Tooth Surface Material</h1>
			</c:when>
			<c:otherwise>
				<h1>Add Tooth Surface Material</h1>
			</c:otherwise>
		</c:choose>

	<form action="ToothSurfaceMaterial.do" name="editToothsurMatForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="material_id" value="${bean.map.material_id}"/>
	</c:if>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder"><legend class="fieldSetLabel">Tooth Surface Material Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Material Name:</td>
			<td>
				<input type="text" name="material_name"  value="${bean.map.material_name}" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Color Code:</td>
			<td><input type="text" name="color_code" value="${bean.map.color_code}" /></td>
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
				<td><a href="${cpath}/master/ToothSurfaceMaterial.do?_method=add">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/ToothSurfaceMaterial.do?_method=list&sortOrder=material_name&sortReverse=false&status=A";">
			Tooth Surface Material List</a></td>
		</tr>
	</table>
</form>
</body>
</html>
