<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="path" value="${pageContext.request.contextPath}"/>
<c:set var="editorMode" value="${empty param._editorMode ? 'tinyMCE' : param._editorMode}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Claim Template - Insta HMS</title>
	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />

	<script>
		var editorMode = '${ifn:cleanJavaScript(param._editorMode)}';
		var saveMethod = "${param.method == 'add' || param.method == 'defaultshow' ? 'create' : 'update'}";

		if (editorMode == 'tinyMCE' || editorMode == '') {
			initEditor("template_content", '${cpath}');
		}

		function doSave() {
			var name = document.insclaim.template_name.value;
			if(name == "") {
				alert("Enter template name");
				return false;
			}
			document.insclaim.method.value = saveMethod;
			document.insclaim.submit();
		}

		function changeEditor() {
			var tempType = document.forms[0].template_type.value;
			var mode = tempType=='P'?'tinyMCE':'text';
			window.location = '?method=defaultshow&_editorMode='+mode+'&templateType='+tempType;
		}
		function doClose(){
			window.location.href='${path}/master/InsuranceClaimTemplate.do?method=list' +
						'&sortOrder=template_name&sortReverse=false&status=A'
		}
	</script>
</head>

<body>
	<h1>${param.method == 'show' ? 'Edit' : 'Add'} Claim Template</h1>
	<form method="POST" action="InsuranceClaimTemplate.do" name="insclaim">
		<input type="hidden" name="method" value=""/>
		<input type="hidden" name="_editorMode" value="${ifn:cleanHtmlAttribute(param._editorMode)}"/>
		<input type="hidden" name="claim_template_id" id="claim_template_id" value="${claim_template_id}">
	<insta:feedback-panel/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Template Name :</td>
				<td>
					<input type="text" name="template_name" value="${ifn:cleanHtmlAttribute(template_name)}"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Template Type :</td>
				<td>
					<select name="template_type" id="template_type" onchange="changeEditor()" class="dropdown" ${param.method == 'show'?'disabled':''}>
						<option ${template_type=='P' ? 'selected' : ''} value="P">PDF Template</option>
						<option ${template_type=='R' ? 'selected' : ''} value="R">RTF Template</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Status : </td>
				<td>
					<input type="radio" name="status" id="status"
						 ${status== null ?'checked':(status=='A' ? 'checked' : '')}  value="A" >Active
					<input type="radio" name="status" id="status"
						 ${status=='I' ? 'checked' : ''}  value="I">Inactive
				</td>
			</tr>
			<tr>
				<td colspan="2">
						<textarea id="template_content" name="template_content"
							style="width: 500; height:450;">${template_content}</textarea>
				</td>
			</tr>
		</table>

		<div class="screenActions">
			<button type="button" accesskey="S" onclick="doSave()"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param.method != 'add'}">
				<a href="javascript:void(0);" onclick="window.location.href='${path}/master/InsuranceClaimTemplate.do?method=add'">Add</a>
			|
			</c:if>
			<a href="javascript:void(0)" onclick="doClose()">Claim Template</a>

		</div>

	</form>

</body>
</html>

