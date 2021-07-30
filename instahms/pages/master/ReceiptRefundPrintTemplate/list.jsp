<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Receipt/Refund Print Template - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<script>
		function deleteSelected(e) {
			var deleteEl = document.getElementsByName("deletePrintTemplate");
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
				href: 'master/ReceiptRefundPrintTemplate.do?method=show',
				onclick: null,
				description: "View and/or Edit Area details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
		function addNew()
		{
			window.href.location = 'ReceiptRefundPrintTemplate.do?method=templateMode';
		}

	</script>
</head>

<body onload="init();">

	<h1>Receipt/Refund Print Templates</h1>

	<insta:feedback-panel/>

	<form metod="POST" action="ReceiptRefundPrintTemplate.do">

		<input type="hidden" name="method" value="delete"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>Select </th>
					<th>Template Name</th>
					<th>Template Mode</th>
					<th>User Name</th>
					<th>Reason for customization</th>
				</tr>
				<c:forEach var="temp" items="${templateList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{template_name : '${temp.map.template_name}'},'');" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="deletePrintTemplate" id="deletePrintTemplate"
							value="${temp.map.template_name}"/> </td>
					 	<td>${temp.map.template_name}</td>
						<td>${temp.map.template_mode=='T'?'Text':'HTML'}</td>
						<td>${temp.map.user_name}</td>
						<td>${temp.map.reason}</td>
		  			</tr>
				</c:forEach>
			</table>
		</div>

		<c:url var="url" value="ReceiptRefundPrintTemplate.do">
			<c:param name="method" value="templateMode"/>
		</c:url>

		<div class="screenActions"><input type="submit" name="delete" value="Delete" onclick="deleteSelected(event)"/> |
		<a href="<c:out value='${url}' />">Add New Receipt / Refund Print Template</a></div>

	</form>
</html>

