<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.STOCK_ADJUSTMENT_REASON_MASTER_PATH %>"/>

<html>
<head>
<title>Adjustment Reason Master - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

<script>

var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '${pagePath}/show.htm?',
		onclick: null,
		description: "View/Edit Stock Adjustment ReasonMaster"
	},

};

function init() {
	createToolbar(toolbar);
}

</script>

</head>

<c:set var="pagePath" value="<%=URLRoute.STOCK_ADJUSTMENT_REASON_MASTER_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<c:set var="itemList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty itemList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>


<body onload="init();">
<h1>Stock Adjustment Reason Master</h1>
<insta:feedback-panel/>

<form name="itemListSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search-lessoptions form="itemListSearchForm" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Adjustment Reason:</div>
				<div class="sboFieldInput">
					<input type="text" name="adjustment_reason" value="${ifn:cleanHtmlAttribute(param.adjustment_reason)}">
					<input type="hidden" name="adjustment_reason@op" value="ilike"/>
				</div>
			</div>
           <div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
			</div>
		 </div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">

			    <th>Stock Adjustment Reason</th>
			    <th>Status</th>


			</tr>
            <c:forEach var="item" items="${itemList}" varStatus="st">
            		<script>
					var stockAdjustmentStr${st.index};
					var statusStr${st.index};

					stockAdjustmentStr${st.index} = <insta:jsString value="${item.adjustment_reason}"/>;
                    var adjustmentID${st.index} = <insta:jsString value="${item.adjustment_reason_id}"/>;
				</script>

            	<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick='showToolbar(${st.index}, event, "resultTable",
					{adjustment_reason: stockAdjustmentStr${st.index} ,adjustment_reason_id: adjustmentID${st.index} },[true])';">

					<td>
							<c:if test="${item.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${item.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${item.adjustment_reason}
						</td>
						<td>${item.status eq 'A' ? 'Active': 'InActive'}</td>

				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>
    <table class="screenActions">
	    	<tr>
				<td><c:url var="Url" value="${pagePath}/add.htm"/>
				<a href="${Url}">Add Stock Adjustment Reason</a>
			</tr>
	</table>
    </form>

	<div class="legend" style="display: ${hasResults ? 'block' : 'none' }">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"> </div>
		<div class="flagText">Active</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"> </div>
		<div class="flagText">Inactive</div>
	</div>

<script>
	var cpath = '${cpath}';
</script>
</body>
</html>
