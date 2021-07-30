<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>View Message Log - Insta HMS</title>
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
<script>

	function doClose() {
		window.location.href = "${cpath}/message/MessageLog.do?_method=list";
	}
</script>
<script>
  contextPath = "${pageContext.request.contextPath}";
  publicPath = contextPath + "/ui/";
</script>	
</head>
<body>
<form action="MessageLog.do" method="POST">
	<input type="hidden" name="_method" value="resend">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="message_log_id" value="${messageLog.map.message_log_id}"/>
	</c:if>

	<div class="pageHeader">Edit Message Log</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Message Type:</td>
				<td class=forminfo>
					${messageLog.map.message_type_id}
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Mode:</td>
				<td class="forminfo">${dispatcherMap[messageLog.map.message_mode].map.display_name}</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td class="forminfo">
					<c:if test="${messageLog.map.last_status eq 'F'}">Failed</c:if>
					<c:if test="${messageLog.map.last_status eq 'S'}">Sent </c:if>
					<c:if test="${messageLog.map.last_status eq 'R'}">Delivered </c:if>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Reason:</td>
				<td class="forminfo">${messageLog.map.last_status_message}</td>
			</tr>

			<tr>
				<td class="formlabel">From:</td>
				<td>
					<input type="text" name="message_sender" value="${messageLog.map.message_sender}"
						class="validate-length" length="100" title="Max length of from address can be 100" />
				</td>
			</tr>
			<tr>
				<td class="formlabel">To:</td>
				<td>
					<input type="text" name="message_to" value="${messageLog.map.message_to}"
						class="required" title="To address is required" />
				</td>
			</tr>
			<tr>
				<td class="formlabel">Cc:</td>
				<td>
					<input type="text" name="message_cc" value="${messageLog.map.message_cc}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Bcc:</td>
				<td>
					<input type="text" name="message_bcc" value="${messageLog.map.message_bcc}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Subject:</td>
				<td>${messageLog.map.message_subject}</td>
			</tr>
			<tr>
				<td class="formlabel">Message Body:</td>
				<td colspan="5">
				<div><p>${messageLog.map.message_body}</p></div>
				</td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="R"><b><u>R</u></b>esend</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Message Log List</a></td>
		</tr>
	</table>
</form>

</body>
</html>
