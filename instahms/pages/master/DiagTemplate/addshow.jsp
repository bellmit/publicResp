<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<title>Diag Report Template - Insta HMS</title>
	<insta:link type="js" file="tiny_mce/tiny_mce.js" />
	<insta:link type="js" file="editor.js" />
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/DiagTemplate.do?_method=list";
		}
		/* initialize the tinyMCE editor */
		initEditor("report_file", '${cpath}', '${prefs.font_name}', ${prefs.font_size});
	</script>
</head>
<c:set var="bodyWidth" value="${prefs.page_width - prefs.left_margin - prefs.right_margin}"/>
<body>
<form name="mainform" action="DiagTemplate.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="testformat_id" value="${bean.testformat_id}"/>
		<%-- no extra params for add operation --%>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Report Template</h1>
	<insta:feedback-panel/>

	<table class="formtable" >
		<tr>
			<td class="formlabel">Template Name:</td>
			<td >
				<input type="text" name="format_name" value="${bean.format_name}" style="width: 200px;"
					class="required validate-length field" length="100"
					title="Name is required and max length of name can be 100" />
			</td>
			<td class="formlabel">Description:</td>
			<td >
				<input type="text" name="format_description" value="${bean.format_description}" style="width: 200px;"
					class="validate-length field" length="500"
					title="Description cannot be more than 500 characters" />
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
	</table>

	<table class="formtable"><tr>
		<td >
				<textarea id="report_file" name="report_file" style="width: ${bodyWidth}pt; height: 450;"><c:out value="${bean.report_file}"/></textarea>
			</td>
	</tr></table>

	<div class="screenActions">
		<c:url var="templatesList" value="DiagTemplate.do">
			<c:param name="_method" value="list"/>
			<c:param name="sortOrder" value="format_name"/>
			<c:param name="sortReverse" value="false"/>
		</c:url>
		<button type="button" accesskey="S" onclick="document.mainform.submit()"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/DiagTemplate.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="${templatesList}">Templates List</a>
	</div>
</form>

</body>
</html>

