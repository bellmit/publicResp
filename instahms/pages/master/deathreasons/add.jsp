<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.DEATH_REASON_PATH %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add Death Reason - Insta HMS</title>
</head>
<body>
	<h1>Add Death Reason</h1>
	<insta:feedback-panel/>
	<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
	<form action="${actionUrl}" method="POST">
		<input type="hidden" name="reason_id" value="${bean.reason_id}">
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Reason: </td>
					<td><input type="text" name="reason" value="${bean.reason}" class="required" title="Reason is mandatory."></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
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
						| <a href="${cpath}/${pagePath}/list.htm?sortOrder=reason&sortReverse=false">List</a>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
</body>
</html>