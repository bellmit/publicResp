<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="path" value="${pageContext.request.contextPath}"/>
<c:set var="editorMode" value="${empty param.editorMode ? 'T' : param.editorMode}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>HVF Print Template</title>

	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />

	<c:set var="templateMode" value="${empty param.templateMode ? template_mode : param.templateMode}"/>

	<script>
		var templateMode = '${templateMode}';
		var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
		var saveMethod = "${param.method == 'add' ? 'create' : 'update'}";

		if (templateMode == 'H' && (editorMode == 'tinyMCE'|| editorMode == '')) {
			initEditor("hvf_template_content", '${cpath}','${pref.font_name}', '${pref.font_size}');
		}

		function doSave() {
			var name = document.hvfprint.template_name.value;
			var reason = document.hvfprint.reason.value;
			if(name == "") {
				alert("Enter template name");
				return false;
			}
			if (reason == ""){
				alert("Enter  reason for print customization");
				return false;
			}
			document.hvfprint.action= "HVFPrintTemplate.do?method="+saveMethod;
			document.hvfprint.submit();
		}

		function doReset() {
			document.hvfprint.action= "HVFPrintTemplate.do?method=resetToDefault";
			document.hvfprint.submit();
		}

	</script>
</head>

<body>
	<h1>HVF Print Template</h1>

	<insta:feedback-panel/>

	<form method="POST"  name="hvfprint" >

		<input type="hidden" name="method" value=""/>
		<input type="hidden" name="editorMode" value="${ifn:cleanHtmlAttribute(param.editorMode)}">
		<input type="hidden" id="template_mode" name="template_mode" value="${templateMode}"/>

		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td >
					<input type="text" class="field" style="width: 170px" name="template_name" ${ifn:cleanHtmlAttribute(param.method) =='show' ? 'readonly' : ''}
								value="${ifn:cleanHtmlAttribute(template_name)}"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Reason for Customization: </td>
				<td>
					<input type="text" name="reason" id="reason" style="width: 350px" size="50" value='<c:out value="${reason}"/>'/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
		<div style="margin-top:10;">
			<textarea id="hvf_template_content" name="hvf_template_content"
					style="width:800px; height:450px;font-family: Courier,fixed"><c:out value="${hvf_template_content}"/></textarea>
		</div>

		<table class="screenActions">
			<tr>
				<td><input type="button" value="Save" onclick="doSave()"/></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
				<c:if test="${templateMode == 'H'}">
					<c:url var="changeEditor" value="HVFPrintTemplate.do">
						<c:param name="method" value="${param.method == 'show'?'show':'add'}"/>
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
				<c:url var="dashboardUrl" value="HVFPrintTemplate.do">
						<c:param name="method" value="list"/>
				</c:url>
				<c:if test="${param.method=='show'}" >
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${path}/master/HVFPrintTemplate.do?method=templateMode'">Add</a></td>
				</c:if>
				<td>&nbsp;|<a href="javascript:void(0)" onclick="return gotoLocation('${dashboardUrl}')"> HVF Print List</a></td>
			</tr>
		</table>
	</form>

</body>
</html>

