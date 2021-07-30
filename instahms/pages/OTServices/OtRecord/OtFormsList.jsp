<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>

<html>

<head>
	<title>Surgery/Procedure Forms List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body>
	<h1>Surgery/Procedure Forms List</h1>
	<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>

	<table style="margin-top: 10px" class="resultList">
		<tr>
			<th>#</th>
			<th>Surgery/Procedure Name</th>
			<th>Form Name</th>
		</tr>
		<c:forEach items="${forms}" var="form" varStatus="st">
			<tr>
				<td>${st.index+1}</td>
				<td>${form.map.operation_name}</td>
				<td><a href="${cpath}/otservices/OtManagement/OtRecord.do?_method=list&visit_id=${ifn:cleanURL(param.visit_id)}&operation_proc_id=${form.map.operation_proc_id}">
					${form.map.form_name}
				</td>
			</tr>
		</c:forEach>
	</table>
	<div style="margin-top: 10px">
		<insta:screenlink screenId="get_ot_management_screen" extraParam="?_method=getOtManagementScreen&visit_id=${param.visit_id}&prescription_id=&operation_details_id=${param.operation_details_id}"
						label="Surgery/Procedure Management" addPipe="false"/>
	</div>
</body>
</html>