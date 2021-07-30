<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="heading" value="Edit Test Results:"/>
<c:set var="title" value="Test Conduction"/>
<c:if test="${isAddendum}">
	<c:set var="heading" value="Report Addendum:"/>
	<c:set var="title" value="Manage Report"/>
</c:if>
<c:if test="${save eq 'disabled' }">
	<c:set var="title" value="Report Content"/>
	<c:set var="heading" value="Report Content:"/>
</c:if>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<title>${title } - <insta:ltext key="laboratory.managereports.template.instahms"/></title>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
<insta:link type="js" file="lightbox.js" />

<script>
	var isAddendum = '${isAddendum}';
	initEditor("templateContent", "${cpath}",
		"${prefs.map.font_name}", ${prefs.map.font_size},
		"${cpath}/Diagnostics/Images.do?_method=getImageListJS&prescribedId=${ifn:cleanJavaScript(prescribedid)}" );

   function checkSave(){
		var saveStatus = document.mainform.saveStatus.value;
		if(saveStatus){
			if ( opener.document.getElementById("templatesTable") != null ) {
				var openerTemplateRow = opener.document.getElementById("templatesTable").rows[opener.document.getElementById("updatedTemplateRowIndex").value];//template row
				if(!empty(${ifn:cleanJavaScript(param.newtestDetailsId)}))
					getElementByName(openerTemplateRow,"test_details_id").value = '${ifn:cleanJavaScript(param.newtestDetailsId)}';//test details id for new amended template
			}
			window.close();
		}
   }

	window.onbeforeunload = function() {
		var forms = window.opener.document.forms;
		for (var i=0; i<forms.length; i++) {
			if (forms[i].name == 'diagcenterform') {
		   		forms[i].save.disabled=false;
		   		if(isAddendum=='false') {
		   			forms[i].sanvprint.disabled=false;
		   		}
	   		}
   		}
	}

   function onSave() {
   		var forms = window.opener.document.forms;
		for (var i=0; i<forms.length; i++) {
			if (forms[i].name == 'diagcenterform') {
				if (forms[i].save.disabled == false) {
			   		alert("Invalid entry. Please try again.");
			   		window.close();
			   	} else {
			   		document.mainform.submit()
			   	}
			}
		}
   }

</script>

</head>

<c:set var="bodyWidth" value="${prefs.map.page_width - prefs.map.left_margin - prefs.map.right_margin}"/>

<body onload="checkSave();">
	<form name="mainform" action="${cpath}/${isAddendum ?'/Diagnostics/AddendumPopup.do':'/Diagnostics/TemplatePopup.do'}" method="POST">
	<input type="hidden" name="_method" value="${isAddendum ? 'saveAddendum' : 'saveTemplateReport'}">
	<input type="hidden" name="prescribedid" value="${ifn:cleanHtmlAttribute(prescribedid)}">
	<input type="hidden" name="saveStatus" value="${ifn:cleanHtmlAttribute(param.status)}">
	<input type="hidden" name="formatid" value="${ifn:cleanHtmlAttribute(param.formatid)}">
	<input type="hidden" name="reportid" id="reportid" value="${ifn:cleanHtmlAttribute(param.reportid)}"/>
	<input type="hidden" name="testDetailsId" value="${ifn:cleanHtmlAttribute(param.testDetailsId)}"/>
	<input type="hidden" name="testId" value="${ifn:cleanHtmlAttribute(param.testId)}"/>
	<input type="hidden" name="revisionNumber" value="${ifn:cleanHtmlAttribute(param.revisionNumber)}" />
	<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mrno)}"/>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(param.category)}" />

	<span class="msg">${ifn:cleanHtml(msg)}</span>

	<table class="formtable">
		<tr>
			<td>
			<h3>${heading }</h3>
			</td>
		</tr>
		<tr>
			<td align="center">
				<textarea id="templateContent" name="templateContent" style="width: ${bodyWidth}pt; height: 650;">
					<c:out value="${templateContent}"/>
				</textarea>
			</td>
		</tr>

		<tr>
			<td align="left">
				<input type="button" name="save" value="Save" onclick="onSave();" ${save }/>
			</td>
		</tr>
	</table>
</form>
</body>
</html>

