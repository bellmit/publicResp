<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Printer Settings List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script>
		function deleteSelected(e) {
		var deleteEl = document.getElementsByName("deleteDefinition");
		for (var i=0; i< deleteEl.length; i++) {
		if (deleteEl[i].checked) {
		return true;
		}
		}
		alert("select at least one definition for delete");
		YAHOO.util.Event.stopEvent(e);
		return false;
		}

		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/PrinterSettingsMaster.do?method=show',
				onclick: null,
				description: "View and/or Edit Print Definition details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>
</head>
<body onload="init()">

	<h1>Print Settings</h1>

	<insta:feedback-panel/>

	<form method="POST" action="PrinterSettingsMaster.do">

		<input type="hidden" name="method" value="deletePrinterDefinition"/>
		<input type="hidden" name="deleteRow" value="0"/>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
				<tr>
					<th>Select </th>
					<th>Printer Setting Name</th>
					<th>Print Mode</th>
					<th>Logo / Header</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable', {printer_id: '${record.map['printer_id']}'},'');" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="deleteDefinition" value="${record.map['printer_id']}"/></td>
						<td>${record.map['printer_definition_name']}</td>
						<td>${record.map['print_mode']=='P'?'PDF':'Text'}</td>
						<td>${record.map['logo_header']=='Y'?'Logo and Header': record.map['logo_header']=='L'?'Logo Only': record.map['logo_header']=='H'?'Header Only':record.map['logo_header']=='N'?'None':'' }</td>
					</tr>
				</c:forEach>
			</table>
		</div>

		<c:url var="url" value="PrinterSettingsMaster.do">
			<c:param name="method" value="add"/>
		</c:url>
		<div class="screenActions">
			<button type="submit" name="delete" accesskey="D" onclick="deleteSelected(event)"><b><u>D</u></b>elete</button>
			&nbsp;|&nbsp;<a href="<c:out value='${url}' />">Add New Printer Settings</a>
		</div>

	</form>

</body>
</html>

