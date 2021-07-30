<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>System Message - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
		function doCancel()
		{
			window.location.href="${cpath}/master/SystemMessageMaster.do?method=list";
		}
	</script>

</head>
<body>

	<form action="SystemMessageMaster.do" method="POST">
		<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
		<c:if test="${param.method == 'show'}">
			<input type="hidden" name="message_id" value="${bean.map.message_id}"/>
		</c:if>

		<h1>${param.method == 'add' ? 'Add' : 'Edit'} Message</h1>
		<insta:feedback-panel/>

		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Message:</td>
					<td colspan="5">
						<input type="text" name="messages" value="${bean.map.messages}"
						class="required validate-length" length="300" maxlength="300" style="width: 750px"
						title="Message is required and max length  can be 300" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Severity:</td>
					<td>
						<insta:selectoptions name="severity" opvalues="I,W,A" optexts="Information,Warning,Alert"
						value="${bean.map.severity}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Display Order:</td>
					<td>
						<input type="text" name="display_order" value="${bean.map.display_order}"
						class="required validate-number" title="Display order is required and must be a number"/>
					</td>
				</tr>
			</table>
		</fieldset>

		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
				<c:if test="${param.method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${cpath}/master/SystemMessageMaster.do?method=add'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doCancel()">System Messages</a></td>
			</tr>
		</table>

	</form>

	</body>
	</html>
