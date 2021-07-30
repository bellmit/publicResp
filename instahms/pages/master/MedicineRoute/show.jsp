<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Route of Administrations List - Insta HMS</title>
</head>
<body>
	<h1>Edit Route</h1>
	<form action="update.htm" method="POST">
		<input type="hidden" name="route_id" value="${bean.route_id}">
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Route Name: </td>
					<td><input type="text" name="route_name" value="${bean.route_name}" class="required" title="Route Name is mandatory."></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Route Code: </td>
					<td><input type="text" name="route_code" value="${bean.route_code}"></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td><select name="status" class="dropdown validate-not-first" title="Status is mandatory.">
							<option value="">-- Select --</option>
							<option value="A" ${bean.status == 'A' ? 'selected' : ''}>Active</option>
							<option value="I" ${bean.status == 'I' ? 'selected' : ''}>Inactive</option>
						</select></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save"/>
						| <a href="add.htm">Add</a>
						| <a href="list.htm?sortOrder=route_name&sortReverse=false">List</a>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
</body>
</html>
