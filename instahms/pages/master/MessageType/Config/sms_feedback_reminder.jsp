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
<c:set var="day1" value="${configMap['day_1'].map.param_value}"/>
<c:set var="day2" value="${configMap['day_2'].map.param_value}"/>
<form action="${cpath}/master/${urlPrefix}/${ifn:cleanURL(param.message_type_id)}/MessageConfig.do" method="POST">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="message_type_id" value="${ifn:cleanHtmlAttribute(param.message_type_id)}"/>

	<div class="pageHeader">Edit Message Configuration</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Message Type: ${messageType.map.message_type_name}</td>		
			</tr>
			<tr>
				<td>
				Config 1:&nbsp;&nbsp;Registration Start time&nbsp;&nbsp;
					<input type="hidden" name="message_configuration_id" value="${configMap['start_time_1'].map.message_configuration_id}"/>
					<input style="width:45px;" type="text" id='param_value' name="param_value_${configMap['start_time_1'].map.message_configuration_id}"
					value="${configMap['start_time_1'].map.param_value}" />
					<img class="imgHelpText" title="${configMap['start_time_1'].map.param_description}" src="${cpath}/images/help.png"/>
				</td>
				<td>
				&nbsp;&nbsp;Registration End time&nbsp;&nbsp;
					<input type="hidden" name="message_configuration_id" value="${configMap['end_time_1'].map.message_configuration_id}"/>
					<input style="width:45px;" type="text" id='param_value' name="param_value_${configMap['end_time_1'].map.message_configuration_id}"
					value="${configMap['end_time_1'].map.param_value}" />
					<img class="imgHelpText" title="${configMap['end_time_1'].map.param_description}" src="${cpath}/images/help.png"/>
				</td>
				<td>
				&nbsp;&nbsp;Include visits on day&nbsp;&nbsp;
				<input type="hidden" name="message_configuration_id" value="${configMap['day_1'].map.message_configuration_id}" />
				<input style="width:45px;" type="text" id='param_value' name="param_value_${configMap['day_1'].map.message_configuration_id}"
					onblur="validatePositiveInteger(this, 'No. of days must be an integer greater than or equal to 0');"
					value="${configMap['day_1'].map.param_value}" />
				<img class="imgHelpText" title="${configMap['day_1'].map.param_description}" src="${cpath}/images/help.png"/>
				&nbsp;&nbsp;days back&nbsp;&nbsp;
				</td>
			</tr>
			<tr>
				<td>
				Config 2:&nbsp;&nbsp;Registration Start time&nbsp;&nbsp;
					<input type="hidden" name="message_configuration_id" value="${configMap['start_time_2'].map.message_configuration_id}"/>
					<input style="width:45px;" type="text" id='param_value' name="param_value_${configMap['start_time_2'].map.message_configuration_id}"
					value="${configMap['start_time_2'].map.param_value}" />
					<img class="imgHelpText" title="${configMap['start_time_2'].map.param_description}" src="${cpath}/images/help.png"/>
				</td>
				<td>
				&nbsp;&nbsp;Registration End time&nbsp;&nbsp;
					<input type="hidden" name="message_configuration_id" value="${configMap['end_time_2'].map.message_configuration_id}"/>
					<input style="width:45px;" type="text" id='param_value' name="param_value_${configMap['end_time_2'].map.message_configuration_id}"
					value="${configMap['end_time_2'].map.param_value}" />
					<img class="imgHelpText" title="${configMap['end_time_2'].map.param_description}" src="${cpath}/images/help.png"/>
				</td>
				<td>
				&nbsp;&nbsp;Include visits on day&nbsp;&nbsp;
				<input type="hidden" name="message_configuration_id" value="${configMap['day_2'].map.message_configuration_id}"/>
				<input style="width:45px;" type="text" id='param_value' name="param_value_${configMap['day_2'].map.message_configuration_id}"
					onblur="validatePositiveInteger(this, 'No. of days must be an integer greater than or equal to 0');"
					value="${configMap['day_2'].map.param_value}" />
				<img class="imgHelpText" title="${configMap['day_2'].map.param_description}" src="${cpath}/images/help.png"/>
				&nbsp;&nbsp;days back&nbsp;&nbsp;
				</td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" 
			><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose('${urlPrefix}');">Message Type List</a></td>
		</tr>
	</table>
</form>

</body>
</html>
