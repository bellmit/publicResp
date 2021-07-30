<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>${title} - Insta HMS</title>
		<insta:link type="js" file="tiny_mce/tiny_mce.js" />
		<insta:link type="js" file="editor.js" />

		<script>
			var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
			if (editorMode == '' || editorMode == 'tinyMCE') {
				/* initialize the tinyMCE editor */
				initEditor("template_content", '${cpath}', '${prefs.font_name}', ${prefs.font_size});
			} // else, use textarea editor as is
			function resetTemplate() {
				document.PatientHeaderForm.resetToDefault.value=true;
				document.PatientHeaderForm.submit();
				return true;
			}
		</script>
	</head>
	<body>
		<div class="pageHeader">${param._method == 'add'?'Add':'Edit'} ${title} Template</div>
		<insta:feedback-panel/>
		<form action="PatientHeaderTemplate.do" method="POST" name="PatientHeaderForm">
			<input type="hidden" name="_method" value="${param._method == 'add'?'create':'update'}"/>
			<input type="hidden" name="type" value="${ifn:cleanHtmlAttribute(param.type)}"/>
			<input type="hidden" name="template_id" value="${ifn:cleanHtmlAttribute(param.template_id)}"/>
			<input type="hidden" name="resetToDefault" value="false"/>

			<table class="formtable" >
				<tr>
					<td class="formlabel">Template Name: </td>
					<td><input type="text" name="template_name" id="template_name" class="required" title="Template Name is mandatory." value="${phTemplateDetails.map.template_name}" style="width: 250px"/></td>
					<td class="formlabel">&nbsp;</td>
					<td>&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<c:choose>
					<c:when test="${param._method == 'show' && phTemplateDetails.map.status == 'A'}">
						<c:set var="active" value="checked"/>
					</c:when>
					<c:when test="${param._method == 'add'}">
						<c:set var="active" value="checked"/>
					</c:when>
					<c:when test="${param._method == 'show' && phTemplateDetails.map.status == 'I'}">
						<c:set var="inactive" value="checked"/>
					</c:when>
					</c:choose>
					<td><input type="radio" name="status" id="statusActive" value="A" class="validate-one-required"	${active} >Active
						<input type="radio" name="status" id="statusActive" value="I" ${inactive}>Inactive<br/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Reason for Customization: </td>
					<td><textarea cols="30" rows="2" name="reason" class="required" title="Reason for Customization is mandatory." style="color:#444">${phTemplateDetails.map.reason}</textarea></td>
				</tr>
			</table>
			<div>
				<textarea name="template_content" id="template_content" style="width: ${prefs.page_width}pt; height: 450;">
					<c:out value="${param._method == 'add'?templateContent:phTemplateDetails.map.template_content}"/>
				</textarea>
			</div>
			<div class="screenActions">

				<input type="submit" name="save" value="Save"/>
				<input type="button" name="resetToDefaultBtn" value="Reset To Default" onclick="return resetTemplate();"/>
				<c:url var="changeEditor" value="PatientHeaderTemplate.do">
					<c:param name="_method" value="${param._method}"/>
					<c:param name="type" value="${param.type}"/>
					<c:param name="template_id" value="${param.template_id}"/>
				</c:url>
				<c:choose>
					<c:when test="${param.editorMode == 'tinyMCE' || empty param.editorMode}">
						<input type="button" value="Use Plain Text Editor" onclick="return gotoLocation('${ifn:cleanJavaScript(changeEditor)}&editorMode=text')"/>
					</c:when>
					<c:otherwise>
						<input type="button" value="Use Rich Text Editor" onclick="return gotoLocation('${ifn:cleanJavaScript(changeEditor)}&editorMode=tinyMCE')"/>
					</c:otherwise>
				</c:choose>
				<c:url var="dashboardUrl" value="PatientHeaderTemplate.do">
					<c:param name="_method" value="list"/>
					<c:param name="sortOrder" value="template_name"/>
					<c:param name="sortReverse" value="false"/>
					<c:param name="status" value="A"/>
				</c:url>
				| <a href="${dashboardUrl}">Templates List</a>

			</div>
		</form>
	</body>
</html>