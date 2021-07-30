<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Email Templates List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/EmailTemplateMaster.do?method=show',
				onclick: null,
				description: "View and/or Edit Email Template details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init()">

	<h1>Email Template Master</h1>

	<insta:feedback-panel/>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<table class="dashboard" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
		<tr>
			<th>#</th>
			<th>Email Category</th>
			<th>Template Name</th>
			<th>From Address</th>
			<th>To Address</th>
			<th>Subject </th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
		<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
			onclick="showToolbar(${st.index}, event, 'resultTable',
			{email_template_id: '${record.email_template_id}'},'');" id="toolbarRow${st.index}">
			<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1}</td>
			<td>${record.email_category}</td>
			<td>${record.template_name}</td>
			<td>${record.from_address}</td>
			<td>${record.to_address}</td>
			<td>${record.subject}</td>
		</tr>
		</c:forEach>
	</table>


</body>
</html>
