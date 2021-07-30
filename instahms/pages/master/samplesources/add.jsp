<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Sample Source - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
</head>
<body>
<c:set var="pagePath" value="<%=URLRoute.SPONSOR_PROCEDURE_PATH %>"/>
<h1>Add Sample Source</h1>

<form action="create.htm" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Sample Source:</td>
			<td>
				<input type="text" name="source_name"  value="${bean.source_name}"
					class="required validate-length" maxlength="50" title="Name is required and max length of name can be 50" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/samplesources.htm?sortOrder=source_name&sortReverse=false&status=A">
				Sample Sources
			</a></td>
		</tr>
	</table>

</form>
</body>
</html>
