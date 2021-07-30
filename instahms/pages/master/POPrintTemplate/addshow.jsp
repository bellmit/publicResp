<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>


<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>PO Print Template</title>
	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />
	<c:set var="templateMode" value="${empty param.templateMode ? template_mode : param.templateMode}"/>

	<script>
		var templateMode = '${templateMode}';
		var editorMode = '${ifn:cleanJavaScript(param.editorMode)}';
		var saveMethod = "${param.method == 'add' ? 'create' : 'update'}";

		if (templateMode == 'H' && (editorMode == 'tinyMCE'|| editorMode == '')) {
			initEditor("pharmacy_template_content", '${cpath}','${pref.font_name}', '${pref.font_size}');
		}

		function doSave() {
			var name = document.pharmacyprint.template_name;
			var reason = document.pharmacyprint.reason;
			if(name.value == "") {
				alert("Enter template name");
				name.focus();
				return false;
			}
			if (reason.value == ""){
				alert("Enter reason for print customization");
				reason.focus();
				return false;
			}
			document.pharmacyprint.action= "POPrintTemplate.do?method="+saveMethod;
			document.pharmacyprint.submit();
		}

		function doReset() {
			document.pharmacyprint.action= "POPrintTemplate.do?method=resetToDefault";
			document.pharmacyprint.submit();
		}
	</script>
</head>

<body>
	<h1>Pharmacy PO Print Template</h1>
	<insta:feedback-panel/>

	<form method="POST"  name="pharmacyprint">
		<input type="hidden" name="method" value=""/>
		<input type="hidden" name="editorMode" value="${ifn:cleanHtmlAttribute(param.editorMode)}">
		<input type="hidden" id="template_mode" name="template_mode" value="${templateMode}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td>
					<input type="text" name="template_name" ${ifn:cleanHtmlAttribute(param.method)=='show' ? 'readonly' : ''}
								value="${ifn:cleanHtmlAttribute(template_name)}" tabindex="1"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel"s>Reason for Customization: </td>
				<td>
					<input type="text" size="50" id="reason" name="reason" value='<c:out value="${reason}"/>' tabindex="2"/>
				</td>
			</tr>
		</table>

		<div style="margin-top:10px">
			<textarea id="pharmacy_template_content" name="pharmacy_template_content" tabindex="3"
				style="width: 600; height:500; font-family: Fixed,Courier"><c:out value="${pharmacy_template_content}"/></textarea>
		</div>

		<table class="screenActions">
			<tr>
				<td><input type="button" value="Save" onclick="doSave()"/></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
				<c:if test="${templateMode == 'H'}">
					<c:url var="changeEditor" value="POPrintTemplate.do">
						<c:param name="method" value="${param.method == 'show'?'show':'add'}"/>
						<c:param name="templateMode" value="${templateMode}"/>
						<c:param name="template_name" value="${template_name}"/>
					</c:url>
					<c:choose>
						<c:when test="${param.editorMode == 'tinyMCE' || empty param.editorMode}">
							<td>
								<input type="button" value="Use Plain Text Editor" tabindex="20" onclick="return gotoLocation('${ifn:cleanJavaScript(changeEditor)}&editorMode=text')"/>
							</td>
						</c:when>
						<c:otherwise>
							<td>
								<input type="button" value="Use Rich Text Editor" tabindex="20" onclick="return gotoLocation('${ifn:cleanJavaScript(changeEditor)}&editorMode=tinyMCE')"/>
							</td>
						</c:otherwise>
					</c:choose>
				</c:if>
				<c:url var="dashboardUrl" value="POPrintTemplate.do">
						<c:param name="method" value="list"/>
				</c:url>
				<c:if test="${param.method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${cpath}/master/POPrintTemplate.do?method=getTemplateMode'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;<a href="javascript:void(0)" onclick="return gotoLocation('${dashboardUrl}');">Pharmacy PO Print Template List</a></td>
			</tr>
		</table>
	</form>

</body>
</html>

