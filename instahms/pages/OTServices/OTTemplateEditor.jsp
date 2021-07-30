<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />
<insta:link type="css" file="hmsNew.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
initEditor("reportContent", "${cPath}",
		"${prefs.map.font_name}", ${prefs.map.font_size},
		"${cPath}/otservices/operationsPopUp.do?method=getImageListJS&reportId=${ifn:cleanJavaScript(reportId)}");


</script>
<title>OT Report Template Editor</title>

</head>
<c:set var="bodyWidth" value="${prefs.map.page_width - prefs.map.left_margin - prefs.map.right_margin}"/>
<c:set var="cPath" value="${pageContext.request.contextPath}"/>
<body >
	<form action="${cPath }/otservices/operations.do?_method=addOrEditOTReportTemplate" method="POST" name="ottemplateform">
	<input type="hidden" name="visit_id" id="visit_id" value="${ifn:cleanHtmlAttribute(param.patientId)}"/>
	<input type="hidden" name="report_id" id="report_id" value="${template.map.report_id}"/>
	<input type="hidden" name="prescId" id="prescId" value="${ifn:cleanHtmlAttribute(param.prescriptionId)}"/>
	<input type="hidden" name="templateId" id="templateId" value="${ifn:cleanHtmlAttribute(param.templateId)}"/> <%-- will be available while adding --%>

	<table class="formtable">
		<tr><td><h1> OT Report </h1></td></tr>
		<tr>
			<td>
				<table>
				   <tr>
					   <td class="formlabel">Report Name: </td>
					   <td class="forminfo"> <input type="text" name="report_name" value="${template.map.report_name}"/></td>
					</tr>
				</table>
			</td>
		</tr>

		<tr>
			<td>
				<div id="reportContent"	style="width: ${bodyWidth}pt; height: 575; " name="reportContent">${ifn:cleanHtml(templateContent)}
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<input type="submit" name="save" value="Save" />
			</td>
		</tr>
	</table>
	</form>
</body>
</html>
