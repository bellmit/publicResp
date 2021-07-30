<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="path" value="${pageContext.request.contextPath}"/>
<c:set var="templateMode" value="${empty param.templateMode ? template_mode : param.templateMode}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Receipt/Refund Print Template - Insta HMS</title>

	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />

	<script>
		var templateNames = ${templateNames};
		var templateMode = '${ifn:cleanJavaScript(template_mode)}';
		var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
		var saveMethod = "${param.method == 'add' ? 'create' : 'update'}";

		if (templateMode == 'H' && (editorMode == 'tinyMCE'|| editorMode == '')) {
			initEditor("template_content", '${cpath}','${pref.font_name}', '${pref.font_size}');
		}

		function doSave() {
			var name = document.forms[0].template_name.value;
			var reason = document.forms[0].reason.value;
			if(name == "") {
				alert("Enter template name");
				document.forms[0].template_name.focus();
				return false;
			}
			if (reason == ""){
				alert("Enter  reason for print customization");
				document.forms[0].reason.focus();
				return false;
			}
			if (!checkDuplicates()) {
				document.forms[0].template_name.focus();
				return false;
			}
			document.getElementById("template_mode").value=templateMode;
			document.forms[0].action= "ReceiptRefundPrintTemplate.do?method="+saveMethod;
			document.forms[0].submit();
		}

		function doReset() {
			document.forms[0].action= "ReceiptRefundPrintTemplate.do?method=resetToDefault";
			document.forms[0].submit();
		}

		function changeEditor(newMode) {
			window.location = '?method=show&template_name=${ifn:cleanURL(param.template_name)}&editorMode=' + newMode;
		}

		function checkDuplicates() {
			var dbTemplateName = document.getElementById("dbTemplate_name").value;
			var inputTemplateName = document.getElementById('template_name').value;
			for(var i=0; i<templateNames.length; i++) {
				if(dbTemplateName.toUpperCase() != templateNames[i].template_name.toUpperCase()) {
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
<h1>Receipt/Refund Print Template</h1>

<insta:feedback-panel/>

	<form method="POST">

		<input type="hidden" name="method" value=""/>
		<input type="hidden" name="editorMode" value="${ifn:cleanHtmlAttribute(param.editorMode)}">
		<input type="hidden" name="dbTemplate_name" id="dbTemplate_name" value="${ifn:cleanHtmlAttribute(template_name)}" />
		<input type="hidden" id="template_mode" name="template_mode" value="${templateMode}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td>
					<input type="text" id="template_name" name="template_name" ${ifn:cleanHtmlAttribute(param.method) =='show' ? 'readonly' : ''} value="${ifn:cleanHtmlAttribute(template_name)}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Reason for Customization: </td>
				<td>
					<input type="text" name="reason" id="reason" size="50" value='<c:out value="${reason}"/>'/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			</table>
			<div style="margin-top:10px">
				<textarea id="template_content" name="template_content"
				style="width:450px; height:500px;font-family: Courier,fixed">${template_content}</textarea>
			</div>
	</form>

	<table class="screenActions">
		<tr>
			<td><input type="button" value="Save" onclick="doSave()"/></td>
			<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
			<c:url var="dashboardUrl" value="ReceiptRefundPrintTemplate.do">
					<c:param name="method" value="list"/>
			</c:url>
			<c:if test="${templateMode == 'H'}">
				<c:url var="changeEditor" value="ReceiptRefundPrintTemplate.do">
					<c:param name="method" value="${param.method == 'show'?'show':'add'}"/>
					<c:param name="template_mode" value="${templateMode}"/>
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
			<c:if test="${param.method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="#" onclick="window.location.href='${path}/master/ReceiptRefundPrintTemplate.do?method=templateMode'">Add</a></td>
			</c:if>

			<td>&nbsp;|&nbsp;<a href="javascript:void(0)" onclick="return gotoLocation('${dashboardUrl}')">Receipt/Refund Print List</a></td>
		</tr>
	</table>

</body>
</html>

