<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Sponsor Limits List - Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<c:set var="pagePath" value="<%=URLRoute.SPONSOR_PROCEDURE_PATH %>"/>
<script>
	var procedureNameList = <%= request.getAttribute("procedureNameList") %>;
	function initProcedureNames() {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(procedureNameList);
			oAutoComp = new YAHOO.widget.AutoComplete('procedure_name', 'pro_dropdown', dataSource);
			oAutoComp.maxResultsDisplayed = 20;
			oAutoComp.queryMatchContains = true;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = false;
		}
	}

	var toolbar = {
		Edit: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: 'master/SponsorProcedureMaster.do?_method=show',
			onclick: null,
			description: "View and/or Edit Sponsor Procedure details"
			}
	};
	function init()
	{
		createToolbar(toolbar);
		showFilterActive(document.SponsorProcedureForm);
		initProcedureNames();
	}

</script>
</head>
<body onload="init();">
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Sponsor Procedure Limits</h1>

	<insta:feedback-panel/>

	<form name="SponsorProcedureForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="SponsorProcedureForm" optionsId="optionalFilter" closed="${hasResults}">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Tpa Name:</div>
				<div class="sboFieldInput">
					<insta:selectdb name="tpa_id" table="tpa_master" valuecol="tpa_id" displaycol="tpa_name"
						orderby="tpa_name" filtered="false" value="${param.tpa_id}" dummyvalue="--Select--"/>
					<input type="hidden" name="tpa_id@op" value="eq" />
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table  class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Procedure Name:</div>
						<div class="sfField">
							<div id="pro_wrapper" style="width: 15em;">
								<input type="text" name="procedure_name" id="procedure_name" value="${ifn:cleanHtmlAttribute(param.procedure_name)}"/>
							<div id="pro_dropdown"></div>
							<input type="hidden" name="procedure_name@op" value="ico" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Status:</div>
						<div class="sfField">
							<insta:checkgroup name="status" optexts="Active,InActive" opvalues="A,I" selValues="${paramValues.status}"/>
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
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
					<insta:sortablecolumn name="tpa_name" title="Tpa Name"/>
					<insta:sortablecolumn name="procedure_code" title="Procedure Code"/>
					<insta:sortablecolumn name="procedure_name" title="Procedure Name"/>
					<th>Limit</th>
					<th>Remarks</th>
					<th>Status</th>
				</tr>
					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{procedure_no: '${record.procedure_no}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>${record.tpa_name}</td>
						<td>${record.procedure_code}</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.procedure_name}
						</td>
						<td>${record.procedure_limit}</td>
						<td>${record.remarks}</td>
					</tr>
					</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		</div>

		<c:url var="url" value="SponsorProcedureMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>
		<%-- 
		<insta:masterLink type="add" url="${url}"/>
		 --%>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}'/>">Add New Sponsor Procedure</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
</body>
</html>
