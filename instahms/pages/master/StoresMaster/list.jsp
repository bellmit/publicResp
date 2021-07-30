<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<html>
<head>
<title>Item Category List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
  <insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
  <insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css"/>
<script>
var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: 'master/StoresMaster.do?_method=show',
		onclick: null,
		description: "View/Edit Category Details"
	},

};

var theForm = document.catListSearchForm;

function init() {
	theForm = document.catListSearchForm;
	theForm.category.focus();
	createToolbar(toolbar);
}
</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="catList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty catList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>

<jsp:useBean id="catDisplay" class="java.util.HashMap"/>
<c:set target="${catDisplay}" property="P" value="Permanent"/>
<c:set target="${catDisplay}" property="C" value="Consumable"/>
<c:set target="${catDisplay}" property="L" value="Reusable"/>
<c:set target="${catDisplay}" property="R" value="Retail Only"/>

<jsp:useBean id="identificationDisplay" class="java.util.HashMap"/>
<c:set target="${identificationDisplay}" property="S" value="Serial No"/>
<c:set target="${identificationDisplay}" property="B" value="Batch No"/>
<c:set target="${identificationDisplay}" property="N" value="None"/>
<body onload="init(); showFilterActive(document.catListSearchForm)">

<h1>Item Category Master</h1>
<insta:feedback-panel/>
<form name="catListSearchForm" method="get">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search form="catListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Category</div>
				<div class="sboFieldInput">
						<insta:selectdb name="category" value="${param.category}" dummyvalue="...Select..."
								 table="store_category_master" displaycol="category" valuecol="category" orderby="category"/>
				</div>
	    	</div>
	  	</div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Identification</div>
						<div class="sfField">
							<insta:selectoptions name="identification" value="${param.identification}"
								opvalues="S,B" optexts="Serial No,Batch No" dummyvalue="..Select.."/>
					    </div>
					</td>
					<td>
						<div class="sfLabel">Issue Type</div>
						<div class="sfField">
							<insta:selectoptions name="issue_type" value="${param.issue_type}" opvalues="P,L,C,R"
								optexts="Permanent,Reusable,Consumable,Retail Only" dummyvalue="..Select.."/>
					    </div>
					</td>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <insta:sortablecolumn name="category" title="Category">Category</insta:sortablecolumn>
				<th>Identification</th>
				<th>Issue Type</th>
				<th>Billable</th>
				<th>Retailable</th>
				<th>Claimable</th>
				<th>Prescribable</th>
				<th>Validate Expiry Date</th>
				<th>Discount(%)</th>
			</tr>
            <c:forEach var="cat" items="${catList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{category_id:'${cat.category_id }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>
						<c:if test="${cat.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${cat.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:out value="${cat.category}"/>
					</td>
					<td><c:out value="${identificationDisplay[cat.identification]}"/></td>
					<td><c:out value="${catDisplay[cat.issue_type]}"/></td>
					<td><c:out value="${cat.billable ? 'Yes' : 'No'}"/></td>
					<td><c:out value="${cat.retailable ? 'Yes' : 'No'}"></c:out></td>
					<td><c:out value="${cat.claimable ? 'Yes' : 'No'}"></c:out> </td>
					<td><c:out value="${cat.prescribable ? 'Yes' : 'No'}"></c:out> </td>
					<td><c:out value="${cat.expiry_date_val ? 'Yes' : 'No'}"></c:out> </td>
					<td><c:out value="${cat.discount}"></c:out></td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <table class="screenActions">
    	<tr>
			<td><a href="${cpath }/master/StoresMaster.do?_method=add">Add New Category</a></td>
		</tr>
	</table>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText">Active</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>
</form>

<insta:CsvDataHandler divid="upload1" action="StoresMaster.do"/>

</body>
</html>
