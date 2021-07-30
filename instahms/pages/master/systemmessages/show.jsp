<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>System Message - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagePath" value="<%=URLRoute.SYSTEM_MESSAGE %>"/>

	<script>
		function doCancel()
		{
			window.location.href="${cpath}/master/systemmessages.htm";
		}
	</script>

</head>
<body>
	<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
	<form action="${actionUrl}" method="POST">
		<input type="hidden" name="message_id" value="${bean.message_id}"/>

		<h1>Edit Message</h1>
		<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Message:</td>
					<td colspan="5">
						<input type="text" name="messages" value="${bean.messages}"
						class="required validate-length" length="300" maxlength="300" style="width: 750px"
						title="Message is required and max length  can be 300" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Severity:</td>
					<td>
						<insta:selectoptions name="severity" opvalues="I,W,A" optexts="Information,Warning,Alert"
						value="${bean.severity}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Display Order:</td>
					<td>
						<input type="text" name="display_order" value="${bean.display_order}"
						class="required validate-number" title="Display order is required and must be a number"/>
					</td>
				</tr>
			</table>
		</fieldset>

		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
				<td>&nbsp;|&nbsp;</td>
				<td><td><a href="${cpath}/${pagePath}/add.htm">Add</a></td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doCancel()">System Messages</a></td>
			</tr>
		</table>

	</form>

	</body>
	</html>
