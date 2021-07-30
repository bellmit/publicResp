<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<title>Discharge Summary - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js"  file="widgets.js" />
	<insta:link type="js"  file="masters/PatientReport.js" />
	<insta:link type="js" file="dashboardsearch.js"/>


</head>
<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
<body onload="initSearch();">

	<div class="pageHeader">Discharge Summary Report Templates</div>
	<insta:feedback-panel/>
	<form name="searchForm" method="GET">  <%-- action is self, no need to specify --%>
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Template Name:</div>
				<div class="sboFieldInput">
					<input type="text" name="caption" value="${ifn:cleanHtmlAttribute(param.caption)}">
					<input type="hidden" name="caption@op" value="ilike"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Format</div>
						<div class="sfField">
							<insta:checkgroup name="format" opvalues="F,T,P" optexts="Fixed Fields, Editable Template, Pdf Template" selValues="${paramValues.format}"/>
						</div>

					</td>
					<td class="last">
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						</div>
					</td>
					<td class="last"></td>
					<td class="last"></td>
					<td class="last"></td>
				</tr>
			</table>
		</div>
	</insta:search>

	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" >
		<table class="resultList" width="100%" id="resultTable" cellspacing="0" cellpadding="0">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="caption" title="Template Name"/>
			    <th>Format</th>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.first ? 'firstRow' : ''}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{id: '${record.id}', format: '${record.format}'},
						null);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}" >
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index +1}</td>
					<td>
						<img src="${cpath}/images/${record.status == 'A'?'empty':'grey'}_flag.gif"/> ${record.caption}
					</td>


					<td>
						<c:if test="${record.format == 'F'}"><font class="hvf">Fixed Fields</font></c:if>
						<c:if test="${record.format == 'T'}"><font class="richtext">Editable Template</font></c:if>
						<c:if test="${record.format == 'P'}"><font class="pdfform">Pdf Form Template</font></c:if>
					</td>
				</tr>
			</c:forEach>
			<insta:noresults hasResults="${hasResults}"/>
		</table>
	</div>

	<c:url var="addTemplateUrl" value="">
		<c:param name="_method" value="add"/>
		<c:param name="format" value="T"/>
	</c:url>

	<c:url var="addFixedField" value="">
		<c:param name="_method" value="add"/>
		<c:param name="format" value="F"/>
	</c:url>

	<c:url var="addEditablePdfUrl" value="">
		<c:param name="_method" value="add"/>
		<c:param name="format" value="P"/>
	</c:url>

	<div class="screenActions" style="float: left">
		<a href="${addTemplateUrl}">Add New Editable Template</a> |
		<a href="${addFixedField}">Add New Fixed Fields Template</a> |
		<a href="${addEditablePdfUrl}">Add Pdf Form Template</a>
	</div>
	<div class="legend" style="display: ${hasResults ? 'block' : 'none'}; float: right" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive Templates</div>
	</div>

</body>
</html>

