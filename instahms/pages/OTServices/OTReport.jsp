<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags"  prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<c:set var="cPath"  value="${pageContext.request.contextPath}"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>OT Report</title>
<insta:link type="css" file="hmsNew.css"></insta:link>
<script type="text/javascript">
	function setHref(reportId){
	var patientId = '${ifn:cleanJavaScript(param.patientId)}';
		if(document.getElementById("ottemplates").value == "" ){
			alert("Please select a Template to Edit");
			document.getElementById("ottemplates").focus();
			return false;
		}else{
			var url = "${cPath }/otservices/operations.do?_method=getOTTemplate&templateId="+document.getElementById("ottemplates").value+"&reportId="+reportId+"&patientId="+patientId+"&prescriptionId=${prescId}";
			document.otreport.action = url;
			document.otreport.submit();

		}
	}
</script>
</head>
<body>
<h1>OT Report</h1>
<table>
	<span style="text-align: center; font-weight: bold;"/>
	<span class="error"/></span>

</table>
<insta:patientdetails visitid="${param.patientId}" />
<form method="POST" name="otreport" action="${cPath }/otservices/operations.do?method=addReportToOperation">

	<table class="dashboard" align="center" width="100%">
		<tr>
			<th>Operation Name</th>
			<th>Report Name</th>
			<th>Actions</th>
		</tr>
		<tr>
			<td>${reportAndOpNames.map.operation_name }</td>
			<td>${ empty param.reportId or param.reportId eq '0' ? 'No Report' :  reportAndOpNames.map.report_name}</td>
			<td>
				<c:if test="${not empty param.reportId and param.reportId ne '0' }" >
					<a href="${cPath }/otservices/operations.do?_method=print&reportId=${ifn:cleanURL(param.reportId)}" target="_blank">Print</a> |
					<a href="${cPath }/otservices/operations.do?_method=getOTTemplate&reportId=${ifn:cleanURL(param.reportId)}&patientId=${ifn:cleanURL(param.patientId)}&prescriptionId=${ifn:cleanURL(param.prescription_id)}">Edit</a>
				</c:if>
			</td>
		</tr>
	</table>

	<c:set var="disabled" value="${not empty param.reportId and param.reportId ne '0' ? 'disabled' : ''}"/>

<table class="formtable" width="100%">
	<tr>
		<td class="label">OT Template:
			<select name="ottemplates" id="ottemplates"  ${disabled}>
				<option value="">....Select Template....</option>
				<c:forEach items="${ottemplates }" var="template">
					<option value="${template.map.template_id }">${template.map.template_name }</option>
				</c:forEach>
			</select>
			<c:if test="${disabled ne 'disabled'}">
				<a href="#" onclick="setHref(0);">Add</a>
			</c:if>
		</td>
	</tr>
	<c:url
			var="URL" value="operations.do">
			<c:param value="getScheduledOperations" name="_method" />
			<c:param name="sortOrder" value="mr_no" />
			<c:param name="sortReverse" value="true"/>
		</c:url>

	<tr><td><a href='<c:out value="${URL}"/>'>OT Services</a></td></td></tr>
</table>
</form>
</body>
</html>
