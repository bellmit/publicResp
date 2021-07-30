<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>OP Medicine Frequency List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/master/MedicineDosage.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of this Frequency Dosage"
				}
		};

		function init() {

			createToolbar(toolBar);

		}

		function setDosageName() {
			var dosageName = document.getElementById('doseFormdosageName').value;
			document.getElementById('medDosageDoasageName').value = dosageName;
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>OP Medicine Frequency</h1>

<insta:feedback-panel/>
	<form name="dosageSearchForm" action="${cpath}/master/MedicineDosage.do">
		<input type="hidden" name="_method" value="list"/>
		<insta:search-lessoptions form="dosageSearchForm" >
				<div class="searchBasicOpts">
					<div class="sboField">
						<div class="sboFieldLabel">Frequency:</div>
						<div class="sboFieldInput">
							<input type="text" name="dosage_name" id="doseFormdosageName" value="${ifn:cleanHtmlAttribute(param.dosage_name)}" />
							<input type="hidden" name="dosage_name@op" value="ico"/>
						</div>
					</div>
				</div>
		</insta:search-lessoptions>
	</form>

<form name="MedicineDosageForm" method="GET" onsubmit="setDosageName(); return checkBoxesChecked('checked', event); ">

	<input type="hidden" name="_method" value="delete"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="medDosageDoasageName" id="medDosageDoasageName" value="" />


	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="dosage_name" title="Frequency"/>
			<th>Per Day Frequency</th>
			<th>Frequency Value</th>
			<th>Frequency Type</th>
			<th>Delete</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {med_dosage_name: '${record.dosage_name}'},'');">

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>${record.dosage_name}</td>
				<td>${record.per_day_qty}</td>
				<td>${record.frequency_value}</td>
				<td>${record.frequency_type}</td>
				<td><input type="checkbox" name="checked" value="${record.dosage_name}"></td>
			</tr>
		</c:forEach>
		</table>
	</div>

	<insta:noresults hasResults="${hasResults}" message="Frequency dosages not found"/>

	<table class="screenActions">
		<tr>
			<td><input type="submit" name="delete" value="Delete" /></td>
			<td>&nbsp;</td>
			<td>|</td>
			<td>&nbsp;</td>
				<c:url var="Url" value="MedicineDosage.do">
						<c:param name="_method" value="add"/>
				</c:url>
			<td><a href="${Url}">Add Dosage</a></td>
		</tr>
	</table>

</form>
</body>
</html>
