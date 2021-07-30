<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="path" value="${pageContext.request.contextPath}"/>
<c:set var="editorMode" value="${empty param.editorMode ? 'T' : param.editorMode}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Diagnostic Outhouse Print Template - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css"/>
	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />
	<c:set var="templateMode" value="${empty param.templateMode ? template_mode : param.templateMode}"/>
	<script>
		var templateMode = '${templateMode}';
		var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
		var saveMethod = "${param.method == 'add' ? 'create' : 'update'}";

		if (templateMode == 'H' && (editorMode == 'tinyMCE'|| editorMode == '')) {
			initEditor("diag_template_content", '${cpath}','${pref.font_name}', '${pref.font_size}');
		}

		function doSave() {
			var name = document.diagprint.template_name.value;
			var reason = document.diagprint.reason.value;
			if(name == "") {
				alert("Enter template name");
				return false;
			}
			if (reason == ""){
				alert("Enter  reason for print customization");
				return false;
			}
			document.diagprint.action= "DiagPrintTemplate.do?method="+saveMethod;
			document.diagprint.submit();
		}

		function doReset() {
			document.diagprint.action= "DiagPrintTemplate.do?method=resetToDefault";
			document.diagprint.submit();
		}

	</script>
</head>

<body>
	<h1>Diagnostic Outhouse Print Template</h1>

	<insta:feedback-panel/>

	<form method="POST"  name="diagprint" >

		<input type="hidden" name="method" value=""/>
		<input type="hidden" name="editorMode" value="${ifn:cleanHtmlAttribute(param.editorMode)}">
		<input type="hidden" id="template_mode" name="template_mode" value="${templateMode}"/>

		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td>
					<input type="text" name="template_name" ${ifn:cleanHtmlAttribute(param.method=='show' ? 'readonly' : '')} value="${ifn:cleanHtmlAttribute(template_name)}"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Reason for Customization: </td>
				<td>
					<input type="text" name="reason" id="reason" size="50" value='<c:out value="${reason}"/>'/>
				</td>
			</tr>
		</table>
			<div style="margin-top:10px">
					<textarea id="diag_template_content" name="diag_outhouse_template_content"
							style="width:500px; height:450;font-family: Courier,fixed"><c:out value="${diag_outhouse_template_content}"/></textarea>
			</div>

		<table class="screenActions">
			<tr>
				<td><input type="button" value="Save" onclick="doSave()"/></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
				<c:if test="${templateMode == 'H'}">
					<c:url var="changeEditor" value="DiagPrintTemplate.do">
						<c:param name="method" value="${ifn:cleanURL(param.method == 'show'?'show':'add')}"/>
						<c:param name="templateMode" value="${templateMode}"/>
						<c:param name="template_name" value="${template_name}"/>
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
				</c:if>
				<c:url var="dashboardUrl" value="DiagPrintTemplate.do">
						<c:param name="method" value="list"/>
				</c:url>
				<c:if test="${param.method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${path}/master/DiagPrintTemplate.do?method=templateMode'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;<a href="javascript:void(0)" onclick="return gotoLocation('${dashboardUrl}')">Diagnostic Print List</a></td>
			</tr>
		</table>
	</form>

</body>
</html>

