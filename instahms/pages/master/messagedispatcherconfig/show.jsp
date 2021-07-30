<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Message Dispatcher Configuration - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="messaging/messageDispatcherConfiguration.js" />
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/messagedispatcherconfig/list.htm";
		}
	</script>
</head>
<body onload="initPage()">

<c:set var="actionUrl" value="${cpath}/master/messagedispatcherconfig/update.htm?message_mode=${bean.message_mode}"/>
<form action="${actionUrl}" method="POST">
	<input type="hidden" name="message_mode" value="${bean.message_mode}"/>
	<input type="hidden" name="display_name" value="${bean.display_name}"/>

	<div class="pageHeader">Edit Message Dispatcher Configuration</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Message Mode:</td>
				<td>${bean.message_mode}</td>
				<td class="formlabel">Name:</td>
				<td>${bean.display_name}</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td class="formlabel">Protocol:</td>
				<td>
					 <insta:selectoptions name="protocol" id="protocol" value='${bean.protocol}' opvalues="'',http,smtp,tcp" optexts="--Select--,HTTP,SMTP,TCP" onchange="onProtocolChange()"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Host:</td>
				<td>
                  	<input type="text" name="host_name" id="host_name" value='${bean.host_name }' />
				</td>
				<td class="formlabel">Port:</td>
				<td>
                	<input type="text" name="port_no" value='${bean.port_no }' id="port_number" onkeypress="return enterNumOnlyzeroToNine(event);" />
				</td>
			</tr>
			<tr>
                <td class="formlabel">Authentication Required : </td>
                <td>
                  <insta:selectoptions name="auth_required" id="auth_required" value='${bean.auth_required }' opvalues="true,false" optexts="Yes,No"/>
                </td>
                <td class="formlabel">Use TLS : </td>
                <td>
                  <insta:selectoptions name="use_tls" id="use_tls" value='${bean.use_tls }' opvalues="true,false" optexts="Yes,No"/>
                </td>
                <td class="formlabel">Use SSL : </td>
                <td>
                  <insta:selectoptions name="use_ssl" id="use_ssl" value='${bean.use_ssl }' opvalues="true,false" optexts="Yes,No"/>
                </td>
			</tr>
            <tr>
	            <td class="formlabel">User Name : </td>
	            <td>
	              	<input type="text" name="username" id="username" value='${bean.username}' />
	            </td>
	            <td class="formlabel">Password : </td>
	            <td>
	              	<input type="text" name="password" id="password" value='${bean.password}' />
	            </td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
            </tr>
            <tr>
	            <td class="formlabel">Attachment Allowed : </td>
	            <td>
                	<insta:selectoptions name="use_tls" id="attachment" value='${bean.attachment_allowed}' opvalues="true,false" optexts="Yes,No"/>
	            </td>
	            <td class="formlabel">Max. Attachment Size (kb) : </td>
	            <td>
                	<input type="text" name="max_attachment_kb" value='${bean.max_attachment_kb}' id="max_attachment_kb" onkeypress="return enterNumOnlyzeroToNine(event);" />
	            </td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
            </tr>
			<tr>
				<td class="formlabel">Custom Parameter 1:</td>
				<td>
					<input type="text" name="custom_param_1" id="custom_param_1" value="${bean.custom_param_1}" class="validate-length" length="100"/>
				</td>
				<td class="formlabel">Custom Parameter 2:</td>
				<td>
					<input type="text" name="custom_param_2" id="custom_param_2" value="${bean.custom_param_2}" class="validate-length" length="100"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Country Code:</td>
				<td>
					<input type="text" name="country_code_prefix" id="country_code_prefix" value="${bean.country_code_prefix}" class="validate-length" length="2"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Protocol Template Type:</td>
				<td>
					<insta:selectoptions name="protocol_template_type" id="templateType" value='${bean.protocol_template_type}' opvalues="'',xml,json" optexts="--Select--,XML,JSON"/>
				</td>
				<td class="formlabel">HTTP Method:</td>
				<td>
					<insta:selectoptions name="http_method" id="httpMethod" value='${bean.http_method}' opvalues="'',GET,POST" optexts="--Select--,GET,POST"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">URL:</td>
				<td colspan="5">
					<textarea name="http_url" id="url" cols="83" rows="3">${bean.http_url}</textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Header:</td>
				<td colspan="5">
					<textarea name="http_header" id="httpheader" cols="83" rows="3">${bean.http_header}</textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Body:</td>
				<td colspan="5">
					<textarea name="http_body" id="httpbody" cols="83" rows="5">${bean.http_body}</textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Success Response:</td>
				<td colspan="5">
					<textarea name="http_success_response" id="httpsuccessresponse" cols="83" rows="5">${bean.http_success_response}</textarea>
				</td>
			</tr>
		</table>

	</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Message Dispatcher Configuration</a></td>
		</tr>
	</table>

</form>

</body>
</html>
