<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Consultation Print Templates List - Insta HMS</title>
</head>
<body onload="init();">
	<h1>Consultation Print Templates</h1>

	<insta:link type="css" file="widgets.css"/>

	<script>
	function deleteSelected(e) {
		var deleteEl = document.getElementsByName("deletePrescriptionPrint");
		for (var i=0; i< deleteEl.length; i++) {
			if (deleteEl[i].checked) {
			return true;
			}
		}
		alert("select at least one template name to delete");
		YAHOO.util.Event.stopEvent(e);
		return false;
	}
	var toolbar = {
		Edit: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: 'master/PrescriptionsPrintTemplates.do?method=show',
			onclick: null,
			description: "View and/or Edit Template details"
			}
	};
	function init() {
		createToolbar(toolbar);
	}
	</script>

	<insta:feedback-panel/>
	<form method="POST" action="PrescriptionsPrintTemplates.do">
		<input type="hidden" name="method" value="delete"/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');" id="resultTable">
				<tr onmouseover="hideToolBar();">
					<th>Select</th>
					<th>Template Name</th>
					<th>Template Mode</th>
					<th>User Name</th>
					<th>Reason for customization</th>
				</tr>
				<c:forEach var="temp" items="${templateList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : '' } ${st.index %2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{template_name: '${temp.map.template_name}'},'')"; id="toolbarRow${st.index}">
						<td>
							<input type="checkbox" name="deletePrescriptionPrint" id="deletePrescriptionPrint"
								value="${temp.map.template_name}"/>
						</td>
						<td>${temp.map.template_name}</td>
						<td>${temp.map.template_mode == 'T' ? 'Text' : 'HTML' }</td>
						<td>${temp.map.user_name}</td>
						<td>${temp.map.reason}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<c:url var="url" value="PrescriptionsPrintTemplates.do">
			<c:param name="method" value="templateMode"/>
		</c:url>

		<div class="screenActions">
			<c:if test="${not empty templateList}">
				<input type="submit" name="Delete" value="Delete" onclick="deleteSelected(event)">&nbsp;|
			</c:if>
				<a href="<c:out value='${url}' />"> Add New Consultation Print template</a>
		</div>

	</form>

</body>
</html>