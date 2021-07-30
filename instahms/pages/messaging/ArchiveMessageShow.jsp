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
		window.location.href = "${cpath}/message/MyNotifications.do?_method=list&sortOrder=last_sent_date&sortReverse=true";
	}
</script>
<c:set var="messageId" value='<%=request.getParameter("message_log_id")%>'/>
  <script>
    contextPath = "${pageContext.request.contextPath}";
    publicPath = contextPath + "/ui/";
 </script>
</head>
<body>
<form action="MyMsgNotifications.do" method="POST">
	<input type="hidden" name="_method" value="resend">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="message_log_id" value="${messageLog.map.message_log_id}"/>
	</c:if>
	<div class="pageHeader">Edit Archive Notification</div>
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
				<td>${messageRecipientId.map.message_recipient_id} </td>
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
			<td><a href="javascript:void(0)" onclick="doClose();">My Notifications</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/message/MyMessages.do?_method=list&sortOrder=last_sent_date&sortReverse=true">Sent Notifications</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/message/ArchiveMessages.do?_method=list&sortOrder=last_sent_date&sortReverse=true">Archive Messages</a></td>
			<c:forEach var="action" items="${myMsgLists}" varStatus="loop">
			<c:if test="${actionStatusMap[action.map.message_action_id] eq 'A'}">
				<td>&nbsp;|&nbsp;</td>
				<td>
				<c:set var="msgActionName" value="${action.map.options}"/>
				<c:choose>
	    			<c:when test="${fn:contains(msgActionName, ';')}">
	      				<c:if test="${action.map.options ne ''}">
	      				<c:forTokens var="token" items="${msgActionName}" delims=";">
					      	  <a href = "${cpath}/message/notification/StdNotificationAction.do?_method=doAction&message_log_id=${messageId}&screen=archive_msg&message_action_type=${action.map.message_action_type}&option=${token}">
									${token}
							  </a>&nbsp;|&nbsp;
						 </c:forTokens>
						 </c:if>
	    			</c:when>
	   			 	<c:otherwise>
	     			   <a href = "${cpath}/message/notification/StdNotificationAction.do?_method=doAction&message_log_id=${messageId}&screen=archive_msg&message_action_type=${action.map.message_action_type}">
							${action.map.message_action_name}
					   </a>
	  				</c:otherwise>
				</c:choose>
				</td>
				</c:if>
			</c:forEach>
		</tr>
	</table>
</form>

</body>
</html>
