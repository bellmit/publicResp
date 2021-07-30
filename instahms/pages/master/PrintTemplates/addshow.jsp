<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>${ifn:cleanHtml(param.title)} - Insta HMS</title>

	<insta:link type="js" file="tiny_mce/tiny_mce.js" />
	<insta:link type="js" file="editor.js" />
	<insta:link type="js" file="master/printtemplates/printtemplates.js"/>

	<script>
		var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
		if (editorMode == '' || editorMode == 'tinyMCE') {
			/* initialize the tinyMCE editor */
			initEditor("print_template_content", '${cpath}', '${prefs.font_name}', ${prefs.font_size});
		} // else, use textarea editor as is
	</script>

</head>
<body>
	<h1>${ifn:cleanHtml(param.title)}</h1>

	<insta:feedback-panel/>

	<form action="PrintTemplates.do" method="POST" name="customform">

			<input type="hidden" name="method" value="update"/>
			<input type="hidden" name="editorMode" value="${ifn:cleanHtmlAttribute(param.editorMode)}"/>
			<input type="hidden" name="customized" value="${ifn:cleanHtmlAttribute(param.customized)}"/>
			<input type="hidden" name="resetToDefault" value="false"/>
			<input type="hidden" name="title" value="${ifn:cleanHtmlAttribute(param.title)}">
			<input type="hidden" name="template_type" value="${ifn:cleanHtmlAttribute(param.template_type)}">

			<table class="formtable">
				<tr>
					<td class="formlabel">Reason for Customization: </td>
					<td>
						<textarea name="reason" id="reason" cols="40" rows="2"><c:out value="${reason}"></c:out></textarea>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<c:if test="${param.template_type == 'R' || param.template_type == 'L'
						|| param.template_type == 'PWACT' || param.template_type == 'TSheet'
						|| param.template_type == 'CI' || param.template_type == 'Triage'
						|| param.template_type == 'Assessment' || param.template_type == 'WEB_LAB'
						|| param.template_type == 'WEB_RAD'|| param.template_type == 'S'
						|| param.template_type == 'Medication_Chart'
						|| param.template_type == 'API_LAB'
						|| param.template_type == 'API_RAD'
						|| param.template_type == 'Discharge_Medication'}">
					<tr>
						<td class="formlabel">Patient Header Template: </td>
						<td><select name="pheader_template_id" id="pheader_template_id" class="dropdown">
								<option value="" ${empty phTemplateId?'selected':''}>None</option>
								<option value="0" ${phTemplateId == 0?'selected':''}>System Default</option>
								<c:forEach var="phTemplate" items="${phTemplates}">
									<option value="${phTemplate.map.template_id}" ${phTemplateId == phTemplate.map.template_id?'selected':''}>${phTemplate.map.template_name}</option>
								</c:forEach>
							</select>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</c:if>
			</table>
			<div style="padding-top:5px">
				<textarea id="print_template_content" name="print_template_content"
					style="width: ${prefs.page_width}px; height: 500;"><c:out value="${print_template_content}"/></textarea>
			</div>


			<table class="screenActions">
				<tr>
					<td><input type="button" value="Save" onclick="doSave()"/></td>
					<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
					<c:url var="changeEditor" value="PrintTemplates.do">
						<c:param name="method" value="show"/>
						<c:param name="template_type" value="${param.template_type}"/>
						<c:param name="customized" value="${param.customized}"/>
						<c:param name="title" value="${param.title}"/>
					</c:url>
					<c:choose>
						<c:when test="${param.editorMode == 'tinyMCE' || empty param.editorMode}">
							<td>
								<input type="button" value="Use Plain Text Editor" onclick="return gotoLocation('${ifn:cleanJavaScript(changeEditor)}&editorMode=text')"/>
							</td>
						</c:when>
						<c:otherwise>
							<td>
								<input type="button" value="Use Rich Text Editor" onclick="return gotoLocation('${ifn:cleanJavaScript(changeEditor)}&editorMode=tinyMCE')"/>
							</td>
						</c:otherwise>
					</c:choose>
					<c:url var="dashboardUrl" value="PrintTemplates.do">
						<c:param name="method" value="list"/>
					</c:url>
					<td>&nbsp;|&nbsp;<a href="javascript:void(0);" onclick="return gotoLocation('${dashboardUrl}')"/>Print Template List</a></td>
				</tr>
			</table>
		</form>
</body>
</html>
