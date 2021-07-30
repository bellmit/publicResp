<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>View My Notification - Insta HMS</title>
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
<script>

	function doClose() {
		window.location.href = "${cpath}/message/MyMessages.do?_method=list&sortOrder=last_sent_date&sortReverse=true";
	}


</script>
<script>
	contextPath = "${pageContext.request.contextPath}";
	publicPath = contextPath + "/ui/";
</script>	
<c:set var="messageId" value='<%=request.getParameter("message_log_id")%>'/>
</head>
<body>
<form action="MyMessages.do" method="POST">
	<input type="hidden" name="_method" value="resend">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="message_log_id" value="${messageLog.map.message_log_id}"/>
	</c:if>

	<div class="pageHeader">Edit My Notification</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<table class="formtable">
			<tr>
				<td class="formlabel">From:</td>
				<td>${messageLog.map.message_sender_id}</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">To:</td>
				<td>${messageRecipientId.map.message_recipient_id}</td>
			</tr>
			<tr>
				<td class="formlabel">Cc:</td>
				<td>${messageLog.map.message_cc}</td>
			</tr>

			<tr>
				<td class="formlabel">Subject:</td>
				<td class="forminfo">${messageLog.map.message_subject}</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td class="forminfo">${(messageLog.map.last_status == 'S')?'Delivered':'Failed'}</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td colspan="5">
				<div style="width:300pt;"><pre>${messageLog.map.message_body}</pre></div>
				</td>
			</tr>
		</table>

	</fieldset>


	<table class="screenActions">
		<tr>
			<td><a href="javascript:void(0)" onclick="doClose();">Sent Notifications</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/message/ArchiveMessages.do?_method=list&sortOrder=last_sent_date&sortReverse=true">Archive Messages</a></td>
		</tr>
	</table>
</form>

</body>
</html>
