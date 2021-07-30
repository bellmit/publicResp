<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Add/Edit Print Template - Insta HMS</title>
	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="editorMode" value="${empty param.editorMode ? 'T' : param.editorMode}"/>
	<c:set var="templateMode" value="${empty param.templateMode ? templateMode : param.templateMode}"/>
	<script>
		var templateNames = ${templateNames};
		var templateMode ='${templateMode}';
		var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
		var saveMethod = "${param._method == 'add'  ? 'create' : 'update'}";

		if (templateMode == 'H' && (editorMode == 'tinyMCE')) {
			initEditor("template_content", '${cpath}','${pref.font_name}', '${pref.font_size}');
		}

		function doSave() {
			var templateName = document.printTemplate.template_name.value;
			var reason = document.printTemplate.reason.value
			if (templateName == '') {
				alert("Enter Template Name");
				document.printTemplate.name.focus();
				return false;
			}
			if (templateName.length > 100) {
				alert("Template Name should be less than 100 chars.");
				document.printTemplate.name.focus();
				return false;
			}

			if (reason == '') {
				alert("Enter reason for Print Customozation..");
				document.printTemplate.reason.focus();
				return false;
			}

			if (!checkDuplicates()) {
				document.forms[0].name.focus();
				return false;
			}

			document.printTemplate.action = "CommonPrintTemplates.do?_method="+saveMethod;
			document.printTemplate.submit();
		}

		function doReset() {
			document.printTemplate.action= "CommonPrintTemplates.do?_method=resetToDefault";
			document.printTemplate.submit();
		}

		function checkDuplicates() {
			var dbtemplateName = document.getElementById("dbtemplate_name").value;
			var inputTemplateName = document.getElementById('template_name').value;
			for(var i=0; i<templateNames.length; i++) {
				if(dbtemplateName.toUpperCase() != templateNames[i].template_name.toUpperCase()) {
					if (inputTemplateName.toUpperCase() == templateNames[i].template_name.toUpperCase()) {
						alert("Template Name Already Exists");
						return false;
					}
				}
			}
			return true;
		}
	</script>
</head>
<body>
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Print Template</h1>
	<insta:feedback-panel/>

	<form method="POST" name="printTemplate">

		<input type="hidden" name="_method" value=""/>
		<input type="hidden" name="template_mode" id="template_mode" value="${templateMode}">
		<input type="hidden" name="template_type" value="${ifn:cleanHtmlAttribute(template_type)}">
		<input type="hidden" name="editorMode" value="${ifn:cleanHtmlAttribute(param.editorMode)}"/>
		<input type="hidden" name="dbtemplate_name" id="dbtemplate_name" value="${ifn:cleanHtmlAttribute(template_name)}">
		<input type="hidden" name="print_template_id" id="print_template_id" value="${print_template_id}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name:</td>
				<td>
					<input type="text" name="template_name" id="template_name" class="field" style="width: 170px"
						${ifn:cleanHtmlAttribute(param.method) =='show' ? 'readonly' : ''} value="${ifn:cleanHtmlAttribute(template_name)}">
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formLabel">Reason for Customization:</td>
				<td>
					<input type="text" name="reason" id="reason" style="width: 350px" size="50" value='<c:out value="${reason}"/>'/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<c:if test="${templateMode == 'H' && param.template_type == 'InstaGenericForm'}">
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

		<div style="margin-top:10;">
			<textarea id="template_content" name="template_content"
					style="width:800px; height:450px;font-family: Courier,fixed"><c:out value="${template_content}"/></textarea>
		</div>

		<table class="screenActions">
			<tr>
				<td><input type="button" value="Save" onclick="doSave()"/></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
				<c:if test="${templateMode == 'H'}">
					<c:url var="changeEditor" value="CommonPrintTemplates.do">
						<c:param name="_method" value="${param._method == 'show'?'show':'add'}"/>
						<c:param name="templateMode" value="${templateMode}"/>
						<c:param name="template_name" value="${template_name}"/>
						<c:param name="template_type" value="${template_type}"/>
					</c:url>
					<c:choose>
						<c:when test="${param.editorMode == 'tinyMCE'}">
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
				</c:if>
				<c:url var="dashboardUrl" value="CommonPrintTemplates.do">
						<c:param name="_method" value="list"/>
				</c:url>
				<c:if test="${param._method=='show'}" >
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${cpath}/master/CommonPrintTemplates.do?_method=templateMode'">Add</a></td>
				</c:if>
				<td>&nbsp;|<a href="javascript:void(0)" onclick="return gotoLocation('${dashboardUrl}')"> Print Templates List</a></td>
			</tr>
		</table>
	</form>

</body>
</html>