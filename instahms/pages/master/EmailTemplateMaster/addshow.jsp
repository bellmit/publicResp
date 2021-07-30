<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Email Template - Insta HMS</title>
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/EmailTemplateMaster.do?method=list";
		}

	/* create the editor for element mailBody */
		initEditor("mail_message", "${cpath}",
		"sans-serif", 10,
		"${cpath}/master/EmailTemplateMaster.do?method=list&templatedId=${bean.map.email_template_id}");
	</script>
</head>
<body>
<c:set var="bodyWidth" value="${prefs.map.page_width - prefs.map.left_margin - prefs.map.right_margin}"/>
<form name="mainform" action="EmailTemplateMaster.do" method="POST">
	<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="email_template_id" value="${bean.map.email_template_id}"/>
	</c:if>

	<h1>${param.method == 'add' ? 'Add' : 'Edit'} Email Templates</h1>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
		<table class="formtable" >
			<tr>
				<td class="formlabel">Email Category:</td>
				<td class="forminfo"> ${category_name} </td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Template Name:</td>
				<td>
					<input type="text" name="template_name" value="${bean.map.template_name}" size="40" maxlength="100" readonly/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">From Address:</td>
				<td>
					<input type="text" name="from_address" value="${bean.map.from_address}"
					class="required validate-email" size="40" maxlength="100" title="From address is required and it should be a valid Email Id" />
				</td>
			</tr>
			<tr>
				<td class="formlabel">To Address:</td>
				<td >
					<input type="text" name="to_address" value="${bean.map.to_address}"
					class="validate-email" size="40" maxlength="100" title="To address should be a valid Email Id" />
				</td>
			</tr>

			<tr>
				<td class="formlabel">Subject:</td>
				<td >
					<input type="text" name="subject" value="${bean.map.subject}"  size="40" maxlength="200" />
				</td>
			</tr>

			<tr>
				<td class="formlabel">Message:</td>
				<td >
					<textarea id="mail_message" name="mail_message" style="width: ${bodyWidth}pt; height: 450;">
						<c:out value="${bean.map.mail_message}"/>
					</textarea>
				</td>
			</tr>

		</table>
	</fieldset>

	<div class="screenActions" style="padding-bottom:5px">
		<button type="button" accesskey="S" onclick="document.mainform.submit()"><b><u>S</u></b>ave</button> | <a href="javascript:void(0);" onclick="doClose(); return true;">Email Template List</a>
	</div>
	<div class="clrboth"></div>
	<table class="dataTable" >
		<tr><th > Token Name </th><th > Token Description </th></tr>
		<c:choose>
			<c:when test="${(bean.map.template_name == 'Patient Template' || bean.map.template_name == 'Doctor Template') && bean.map.email_category == 'C'}">
				<tr><td>user</td><td>Logged in user name</td></tr>
				<tr><td>hospital</td><td>Hospital name</td></tr>
				<tr><td>documents</td><td>Patient documents</td></tr>
				<tr><td>portalurl</td><td>Portal url</td></tr>
			</c:when>
			<c:when test="${bean.map.template_name == 'Patient to Doctor Template' && bean.map.email_category == 'C'}">
		<tr><td>user</td><td>Logged in user name</td></tr>
				<tr><td>patient</td><td>Patient name</td></tr>
				<tr><td>mobileNo</td><td>Patient mobile number</td></tr>
				<tr><td>document</td><td>Document name</td></tr>
				<tr><td>hospital</td><td>Hospital Name</td></tr>
			</c:when>
			<c:when test="${bean.map.template_name == 'Doctor to Patient' && bean.map.email_category == 'C'}">
				<tr><td>patient</td><td>Patient name</td></tr>
				<tr><td>doctor</td><td>Doctor name</td></tr>
				<tr><td>document</td><td>Document name</td></tr>
			</c:when>
			<c:when test="${bean.map.template_name == 'Appointment Booked Template' && bean.map.email_category == 'S'}">
		<tr><td>doctor</td><td>Appointment doctor</td></tr>
				<tr><td>date</td><td>Date of appointment</td></tr>
				<tr><td>time</td><td>Time of appointment</td></tr>
			</c:when>
			<c:when test="${bean.map.template_name == 'Scheduled Email Reports Template' && bean.map.email_category == 'R'}">
		<tr><td>hospital</td><td>Hospital Name</td></tr>
				<tr><td>reportdate</td><td>Date range of the report data</td></tr>
				<tr><td>reportname</td><td>Name of the report</td></tr>
				<tr><td>reportperiod</td><td>Report Trend (Daily / Weekly / Monthly)</td></tr>
			</c:when>
			<c:when test="${bean.map.template_name == 'Patient Registration' && bean.map.email_category == 'C'}">
		<tr><td>user</td><td>Logged in user name</td></tr>
				<tr><td>hospital</td><td>Hospital name</td></tr>
				<tr><td>mrno</td><td>Patient registration no. (portal access user name)</td></tr>
				<tr><td>password</td><td>Portal access password</td></tr>
				<tr><td>portalurl</td><td>Portal url</td></tr>
			</c:when>
			<c:otherwise>
			</c:otherwise>
		</c:choose>
	</table>

</form>

</body>
</html>
