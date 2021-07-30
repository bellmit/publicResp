<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:useBean id="severityDisplay" class="java.util.HashMap"/>
<c:set target="${severityDisplay}" property="I" value="<img src='${cpath}/images/blue_flag.gif'> Information"/>
<c:set target="${severityDisplay}" property="W" value="<img src='${cpath}/images/yellow_flag.gif'> Warning"/>
<c:set target="${severityDisplay}" property="A" value="<img src='${cpath}/images/red_flag.gif'> Alert"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<c:set var="pagePath" value="<%=URLRoute.SYSTEM_MESSAGE %>"/>
	<title>System Messages - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>

	<script type="text/javascript">
	var toolbar = {
		Edit: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: '${pagePath}/show.htm?',
			onclick: null,
			description: "View and/or Edit System Messages"
			},
		DELETE: {
			title: "Delete",
			imageSrc: "icons/Delete.png",
			href: '${pagePath}/delete.htm?',
			onclick: null,
			description: "Delete System Messages"
			}
	};
	function init()
	{
		createToolbar(toolbar);
	}
	</script>
</head>

<body onload="init()">

	<h1>System Message Master</h1>

	<insta:feedback-panel/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th>Order</th>
				<th>Message</th>
				<th>Severity</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{message_id: '${record.message_id}'},'');" id="toolbarRow${st.index}">
					<td>${record.display_order}</td>
					<td>${record.messages}</td>
					<td>${severityDisplay[record.severity]}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<c:url var="Murl" value="${pagePath}/add.htm" />

	<table class="screenActions">
		<tr>
			<td><a href="${Murl}">Add New Message</a></td>
		</tr>
	</table>

	<div class="legend">
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText">Information</div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText">Warning</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Alert</div>
	</div>
</body>

</html>
