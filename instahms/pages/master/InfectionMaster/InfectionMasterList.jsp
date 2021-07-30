<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Infection Types Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var roleId = '${ifn:cleanJavaScript(roleId)}';
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/ClinicalInfectionMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Clinical Infection Types Master details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			infectionTypesAutoComplete();
		}

		 var infectionTypesAndIds = <%= request.getAttribute("infectionTypesAndIds") %> ;
		 var autoComp = null;

		 function infectionTypesAutoComplete() {
			var datasource = new YAHOO.util.LocalDataSource({result: infectionTypesAndIds});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "infection_type"},{key : "infection_type_id"}]
			};
			autoComp = new YAHOO.widget.AutoComplete('infection_type','infectionTypeContainer', datasource);
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.forceSelection = true ;
			autoComp.animVert = false;
			autoComp.resultTypeList = false;
			autoComp.autoHighlight = false;

			// autoComp.itemSelectEvent.subscribe(setClinicalDetails);
		}
	</script>

</head>

<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Infection Types Master</h1>

	<insta:feedback-panel/>

	<form name="InfectionTypesMasterSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="InfectionTypesMasterSearchForm">

			<div class="searchBasicOpts">
				<div class="sboField" style="height: 69px">
					<div class="sboFieldLabel">Infection Type:</div>
					<div class="sboFieldInput">
						<input type="text" name="infection_type" id="infection_type" value="${ifn:cleanHtmlAttribute(param.infection_type)}">
						<input type="hidden" name="infection_type@op" value="ico" />
						<div id="infectionTypeContainer" style="width: 220px"></div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel" >Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="reasonTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="infection_type" title="Infection Type"/>
					<th>&nbsp;</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'reasonTable',
							{infection_type_id: '${record.infection_type_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>${record.infection_type}
						</td>
						<td>&nbsp;</td>
					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="ClinicalInfectionMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add New Infection Type</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>