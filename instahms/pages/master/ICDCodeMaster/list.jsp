<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Codes List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '/master/ICDCodesMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit the contents of this Code"
				}
		};
		function init() {

			createToolbar(toolbar);
			showFilterActive(document.CMICDCodesForm);
		}
	</script>

</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>Codes Master</h1>
<insta:feedback-panel/>
<form name="CMICDCodesForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search form="CMICDCodesForm" optionsId="optionalFilter" closed="${hasResults}">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Code:</div>
				<div class="sboFieldInput">
				<input type="text" name="code" value="${ifn:cleanHtmlAttribute(param.code)}"/>
				<input type="hidden" name="code@op" value="ico"/>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldlabel">Code Type:</div>
				<div class="sboFieldInput">
					<insta:selectdb name="code_type" table="mrd_supported_code_types" valuecol="code_type"
							displaycol="code_type" dummyvalue="..Select.." value="${param.code_type}"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >

			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Code Description:</div>
						<div class="sfField">
							<input type="text" name="code_desc" value="${ifn:cleanHtmlAttribute(param.code_desc)}"/>
							<input type="hidden" name="code_desc@op" value="ico"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Status:</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>

					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>



<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>#</th>

			<insta:sortablecolumn name="code_type" title="Code Type"/>
			<insta:sortablecolumn name="code" title="Code"/>
			<insta:sortablecolumn name="code_desc" title="Code Description"/>

		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{code: '${record.code}', codeType: '${record.code_type}'}, '');" >

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td>
							${record.code_type}
						</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.code}
						</td>
						<td>
							<insta:truncLabel value="${record.code_desc}" length="100"/>
						</td>


			</tr>
		</c:forEach>
	</table>
</div>

	<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
	</c:if>

	<c:url var="url" value="ICDCodesMaster.do">
			<c:param name="_method" value="add"/>
	</c:url>
	<div class="screenActions" style="float: left">
		<a href="<c:out value='${url}' />">Add New Code</a>
			| <a href="./ICDUpload.do?_method=add">Upload Codes Master</a>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>

</form>
</body>
</html>
