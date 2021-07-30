<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Message Configuration - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
	<script>
		function doClose(urlPrefix) {
			if (empty(urlPrefix)) {
				urlPrefix = 'general';
			}
			window.location.href = "${cpath}/master/" + urlPrefix + "/MessageType.do?_method=list";
		}
	</script>

</head>
<body>
<c:set var="urlPrefix" value="messages"/>
<form action="${cpath}/master/${urlPrefix}/${ifn:cleanURL(param.message_type_id)}/MessageConfig.do" method="POST">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="message_type_id" value="${ifn:cleanHtmlAttribute(param.message_type_id)}"/>

	<div class="pageHeader">Edit Message Configuration</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Message Type:</td>
				<td>${messageType.map.message_type_name}</td>
				<td></td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Show followup due in the <b>past</b> </td>
				<td>
					<input type="hidden" name="message_configuration_id" value="${configMap['overdue_period'].map.message_configuration_id}"/>
					<input style="width:45px;" type="text" onblur="validateRequired(this);validateNonZeroInteger(this, 'No. of days must be > 0');"
						name="param_value_${configMap['overdue_period'].map.message_configuration_id}" value="${configMap['overdue_period'].map.param_value}" />
					&nbsp;&nbsp;days&nbsp;&nbsp;&nbsp;<img class="imgHelpText" title="${configMap['overdue_period'].map.param_description}" src="${cpath}/images/help.png"/>
				</td>
				<td></td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Show followup due in the <b>next</b> </td>
				<td>
					<input type="hidden" name="message_configuration_id" value="${configMap['due_period'].map.message_configuration_id}"/>
					<input style="width:45px;" type="text" onblur="validateRequired(this);validateNonZeroInteger(this, 'No. of days must be > 0');"
					name="param_value_${configMap['due_period'].map.message_configuration_id}" value="${configMap['due_period'].map.param_value}" />
					&nbsp;&nbsp;days&nbsp;&nbsp;&nbsp;<img class="imgHelpText" title="${configMap['due_period'].map.param_description}" src="${cpath}/images/help.png"/>
				</td>
				<td></td>
				<td>&nbsp;</td>
			</tr>

		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose('${urlPrefix}');">Message Type List</a></td>
		</tr>
	</table>
</form>

</body>
</html>
