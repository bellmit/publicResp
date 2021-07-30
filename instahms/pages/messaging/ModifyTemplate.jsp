<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Message Type - Insta HMS</title>
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
<insta:link type="script" file="messaging/messaging.js"/>
<script>
		contextPath = "${pageContext.request.contextPath}";
		publicPath = contextPath + "/ui/";
</script>	
</head>
<body>
<c:set var="practoSmsMod" value="${practoSmsModule.map.activation_status}"/>
<c:set var="editability" value="" />
<c:set var="backgroundColor" value="" />
<c:set var="attachmentDisabled" value="" />
<c:if test="${practoSmsMod == 'Y'}">
	<c:set var="attachmentDisabled" value="disabled" />
	<c:set var="editability" value="readonly" />
	<c:set var="backgroundColor" value="background-color:#EFEBE7;" />
</c:if>

<form action="Message.do" method="POST" name="MessageForm" enctype="multipart/form-data">
	<input type="hidden" name="_method" value="sendMessage" id="_method"/>
 	<input type="hidden" name="message_mode" value="${bean.map.message_mode}" id="message_mode"/>
 	<input type="hidden" name="_recipient_count" value="${recipientCount}" id="_recipient_count"/>
 	<input type="hidden" name="_select_all" value="${_select_all}" id="_select_all"/>

	<div class="pageHeader">Edit Message</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Message Type:</td>
				<td>${messageTypeName}</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Mode:</td>
				<td>${dispatcher.map.display_name}</td>
			</tr>

			<tr>
				<td class="formlabel">From:</td>
				<td><input type="text" ${editability} name="message_sender" value="${bean.map.message_sender}" style="width:160pt;"/></td>
				<td><img class="imgHelpText" title="If you leave this field blank, message will be sent from the address configured in message preferences" src="${cpath}/images/help.png"/></td>
			</tr>
			<tr>
				<td class="formlabel">To:</td>
				<td><input type="text" ${editability} name="message_to" value="${bean.map.message_to}" style="width:160pt;"/></td>
				<td><img class="imgHelpText" title="Anything you enter here will be add to the list of recipients already selected." src="${cpath}/images/help.png"/></td>
			</tr>
			<tr>
				<td class="formlabel">Cc:</td>
				<td><input type="text" ${editability} name="message_cc" value="${bean.map.message_cc}" style="width:160pt;"/></td>
			</tr>
			<tr>
				<td class="formlabel">Bcc:</td>
				<td><input type="text" ${editability} name="message_bcc" value="${bean.map.message_bcc}" style="width:160pt;"/></td>
			</tr>
			<tr>
				<td class="formlabel">Subject:</td>
				<td><input type="text" ${editability} name="message_subject" value='<c:out value="${bean.map.message_subject}" />'  style="width:160pt;"/></td>
				<td><img class="imgHelpText" title="Any valid token used in this field, will be automatically substituted with appropriate values, when the message is sent. Any token not in the 'Available Token List', will be sent as it is." src="${cpath}/images/help.png"/></td>
			</tr>
			<tr>
				<td class="formlabel">Message Body:</td>
				<td colspan="5">
					<textarea id="message_body" ${editability} name="message_body"
					style="width: 350pt; height: 180pt; ${backgroundColor}"><c:out value="${bean.map.message_body}"/></textarea>
				</td>
			</tr>
			<c:if test="${dispatcher.map.attachment_allowed == 'Y'}">
			<c:if test="${not empty attachmentList}">
				<c:forEach var="attachment" items="${attachmentList}" varStatus="status">
					<tr>
					<c:choose>
						<c:when test="${status.index == 0}">
							<td class="formlabel">Attachments:</td>
						</c:when>
						<c:otherwise>
							<td class="formlabel"></td>
						</c:otherwise>
					</c:choose>
					<td colspan="4" >
					<input type="checkbox" name="attached_files" value="${attachment.map.attachment_id}" checked/><a href="#">${attachment.map.attachment_name}</a>
					</td>
					</tr>
				</c:forEach>
			</c:if>
  			<tr>
			<c:choose>
				<c:when test="${not empty attachmentList}">
					<td class="formlabel"></td>
					<td colspan="4">Add another attachment: <input type="file" ${attachmentDisabled} name="attachment" tabindex="150" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>&nbsp;&nbsp;<a href="#" onclick="addAttachment();">Add</a>
					</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel">Attachments:</td>
					<td colspan="5"><input type="file" ${attachmentDisabled} name="attachment" tabindex="150" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>&nbsp;&nbsp;<a href="#" onclick="addAttachment();">Add</a>
					</td>
				</c:otherwise>
			</c:choose>
			</tr>
			</c:if>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="button" accesskey="S" onclick="return sendMessage();"><b><u>S</u></b>end</button></td>
		</tr>
	</table>
	<div class="clrboth"></div><br/>
	<div style="display:none">${fn:length(tokenMap)}</div>
	<c:if test="${not empty tokenMap}">
	<table class="dataTable" >
		<c:choose>
		<c:when test="${fn:length(tokenMap) gt 1}">
			<tr><th colspan="3"> Available Tokens </th></tr>
			<tr>
				<c:forEach var="tokenEntry" items="${tokenMap}" varStatus="status">
				<c:set var="tokenList" value="${tokenEntry.value}"/>
				<c:set var="nrows" value="8"></c:set>
				<c:set var="ncols" value="${fn:length(tokenList) / nrows}"/>
				<fmt:formatNumber var="ncols"
					value="${ncols - (ncols % 1 )+ (((fn:length(tokenList) mod nrows) gt 0) ? 1 : 0)}" pattern="#"/>
				<div style="display:none;">Rows : ${nrows} Columns : ${ncols}</div>
		  		<td valign="top" style="padding:0px">
					<table class="dataTable" style="width:100%;">
					<tr><th colspan="${ncols}">${tokenEntry.key}</th></tr>
					<c:forEach var="row" begin="0" end="${(nrows > 0) ? nrows-1 : 0}">
						<tr>
						<c:forEach var="col" begin="0" end="${(ncols > 0) ? ncols-1 : 0}">
								<td>${tokenList[(ncols * row) + col]}</td>
						</c:forEach>
						</tr>
					</c:forEach>
					</table>
				</td>
				</c:forEach>
			</tr>
		</c:when>
		<c:otherwise>
			<c:forEach var="tokenEntry" items="${tokenMap}" varStatus="status">
				<c:set var="tokenList" value="${tokenEntry.value}"/>
			</c:forEach>
			<c:set var="ncols" value="5"></c:set>
			<c:set var="nrows" value="${fn:length(tokenList) / ncols}"/>
			<fmt:formatNumber var="nrows"
				value="${nrows - (nrows % 1 )+ (((fn:length(tokenList) mod ncols) gt 0) ? 1 : 0)}" pattern="#"/>
			<tr><th colspan="${ncols}"> Available Tokens </th></tr>
			<div style="display:none;">Rows : ${nrows} Columns : ${ncols}</div>
			<c:forEach var="row" begin="0" end="${(nrows > 0) ? nrows-1 : 0}">
				<tr>
				<c:forEach var="col" begin="0" end="${(ncols > 0) ? ncols-1 : 0}">
						<td>${tokenList[(ncols * row) + col]}</td>
				</c:forEach>
				</tr>
			</c:forEach>
		</c:otherwise>
		</c:choose>
	</table>
	</c:if>
</form>

</body>
</html>
