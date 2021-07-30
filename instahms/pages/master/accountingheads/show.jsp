<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.ACCOUNTING_HEAD_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Insta HMS</title>
</head>
<body>
	<h1>Edit Account Head</h1>

	<insta:feedback-panel/>
	<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
	<form action="${actionUrl}" method="POST">
		<input type="hidden" name="account_head_id" value="${param.account_head_id}" />
			<fieldset class="fieldSetBorder">
			<table class="formtable" >
				<tr>
					<td class="formlabel">Account Head Name: </td>
					<td><input type="text" name="account_head_name" class="required field"
							value="${bean.account_head_name}" readOnly							
							title="Account Head Name is required.">
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr><td class="formlabel">Display Order: </td>
					<td><input type="text" name="display_order" class="required validate-number" maxlength="4"
							value="${bean.display_order}"
							title="Display Order is mandatory and it should be integer">
					</td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				</tr>
			</table>
		</fieldset>

			<div class="screenActions">
				<button type="submit" name="save" accesskey="S"><b><u>S</u></b>ave</button>
				<a href="${cpath}/${pagePath}/add.htm?" >|&nbsp; Add &nbsp;|</a>
				<a href="${cpath}/${pagePath}/list.htm?sortOrder=display_order&status=A" >Account Heads List</a>
			</div>
	</form>
</body>
</html>
