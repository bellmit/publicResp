<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.GRN_PRINT_TEMPLATE_PATH %>"/>


<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>GRN Print Template</title>
	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />

	<script>
	var temName = '${param.template_name}';
	var editorMode = '${param.editorMode}';
	function init(){
		if ( editorMode == 'tinyMCE' || editorMode == '' ){
			initEditor("grn_template_content", '${cpath}','${pref.font_name}', '${pref.font_size}');
		}
	}
	var templateMode = '${bean.template_mode}';
	
	
	function doSave() {
		var name = document.grnprint.template_name;
		var reason = document.grnprint.reason;
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
		document.grnprint.action = "updatetemplate.htm";
		document.grnprint.submit();
	}
	
	function doReset() {
		document.grnprint.action = "reset.htm"
		document.grnprint.submit();
	}
	</script>
</head>

<body onload="init();">
	<h1>Edit GRN Print Template</h1>
	<insta:feedback-panel/>

	<form name="grnprint" action="show.htm" method="post">
		<input type="hidden" id="template_mode" name="template_mode" value="${bean.template_mode}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name: </td>
				<td>
					<input type="text" name="template_name" 'readonly'
								value="${ifn:cleanHtmlAttribute(bean.template_name)}" tabindex="1"/>
					<input type="hidden" id="template_id" name="template_id" value="${bean.template_id}"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel"s>Reason for Customization: </td>
				<td>
					<input type="text" size="50" id="reason" name="reason" value='<c:out value="${bean.reason}"/>' tabindex="2"/>
				</td>
			</tr>
		</table>

		<div style="margin-top:10px">
			<textarea id="grn_template_content" name="grn_template_content" tabindex="3"
				style="width: 600; height:500; font-family: Fixed,Courier"><c:out value="${bean.grn_template_content}"/></textarea>
		</div>

		<table class="screenActions">
			<tr>
				<td><input type="button" value="Save" onclick="return doSave()"/></td>
				<td><input type="button" value="Reset to Default" onclick="return doReset()"/></td>
				<c:if test="${bean.template_mode == 'H'}">
					<c:url var="changeEditor" value="${pagePath}/show.htm">
						<c:param name="templateMode" value="${bean.template_mode}"/>
						<c:param name="template_name" value="${bean.template_name}"/>
						<c:param name="template_id" value="${bean.template_id}"/>
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
					<td>&nbsp;|&nbsp;</td>
					<td><a href="${cpath}${pagePath}/add.htm">Add</a></td>
				<td>&nbsp;|&nbsp;<a href="${cpath}${pagePath}/list.htm?sortOrder=template_name&sortReverse=false">GRN Print Template List</a></td>
			</tr>
		</table>
	</form>

</body>
</html>

