<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Prescription Instructions Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="/masters/presInstructionsMaster.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/PrescriptionInstructionsMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Prescription Instructions details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoPrescriptionInstructionsMaster();
		}
		function setInstructionName() {
			var instructionDesc = document.getElementById('instruction_desc').value;
			document.getElementById('presInstructionName').value = instructionDesc;
		}
		var prescriptionInstructions = '${ifn:cleanJavaScript(prescriptionInstructionsList)}';

	</script>

</head>

<body onload="init()">

	<h1>Prescription Instructions Master</h1>

	<insta:feedback-panel/>

		<form name="presInstSearchForm" action="${cpath}/master/PrescriptionInstructionsMaster.do">
			<input type="hidden" name="_method" value="list"/>
			<insta:search-lessoptions form="presInstSearchForm">
				<table class="searchBasicOpts" >
					<tr>
						<td class="sboField" style="height: 70px">
							<div class="sboField" style="height:69px">
								<div class="sboFieldLabel">Prescription Instructions :</div>
									<div class="sboFieldInput">
										<input type="text" name="instruction_desc" id="instruction_desc" value="${ifn:cleanHtmlAttribute(param.instruction_desc)}" style = "width:32em" >
										<input type="hidden" name="instruction_desc@op" value="ico" />
										<div id="prescriptioninstructioncontainer" style = "width:32em"></div>
									</div>
							</div>
			 			</td>
					</tr>
		 	 	</table>
			</insta:search-lessoptions>
		</form>

	<form name="PrescriptionInstructionSearchForm" method="GET" onsubmit="setInstructionName(); return checkBoxesChecked('checked', event);">

		<input type="hidden" name="_method" value="delete"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
			<input type="hidden" name="presInstructionName" id="presInstructionName" value="" />

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="instruction_desc" title="Prescription Instructions"/>
					<th>Delete</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{instruction_id: '${record.map.instruction_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td><insta:truncLabel value="${record.map.instruction_desc}" length="160"/></td>
						<td><input type="checkbox" name="checked" value="${record.map.instruction_id}"></td>
					</tr>
				</c:forEach>
			</table>
		</div>

		<table class="screenActions">
			<tr>
				<td><input type="submit" name="delete" value="Delete" /></td>
					<td>&nbsp;</td>
					<td>|</td>
					<td>&nbsp;</td>
				<c:url var="url" value="PrescriptionInstructionsMaster.do">
						<c:param name="_method" value="add"/>
				</c:url>
				<td><a href="<c:out value='${url}' />">Add Prescription Instructions</a></td>
			</tr>
		</table>
	</form>

</body>
</html>