<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Store Item Rates - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="masters/storesitemmaster.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			EditRates: {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/pages/master/StoresMaster/StoreItemRates.do?_method=show",
				description : "View and/or Edit the rates of Item"
				}
		};

		function init() {
			createToolbar(toolBar);
			autoItem();
			automanf();
		}
	</script>
	<script>
	var itemList = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER)%>;
	var manfList = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MANFNAMES_IN_MASTER)%>;
	var itemDetailsList = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER) %>;
 </script>
 <style>
			#itemcontainer .yui-ac-content{
				width: 300px;
			}
 </style>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty list.dtoList ? 'true' : 'false'}"/>

<h1>Store Item Rates</h1>

<insta:feedback-panel/>

<form name="itemListSearchForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

<insta:search form="itemListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	<div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Item</div>
				<div class="sboFieldInput">
					<div id="autoItem" >
						<input type="text" name="medicine_name" id="item" value="${ifn:cleanHtmlAttribute(param.medicine_name)}"/>
						<input type="hidden" name="medicine_name@op" value="ilike" />
						<div id="itemcontainer"></div>
					</div>
				</div>
    	</div>
    	<div class="sboField">
			<div class="sboFieldLabel">Store Tariffs</div>
			<div class="sboFieldInput">
				<insta:selectdb name="store_rate_plan_id" table="store_rate_plans" valuecol="store_rate_plan_id"
					displaycol="store_rate_plan_name" orderby="store_rate_plan_name" value="${param.store_rate_plan_id}"
					dummyvalue="--Select--" dummyvalueId=""/>
				<input type="hidden" name="store_rate_plan_id@cast" value="y"/>
			</div>
	   </div>
  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Manufacturer</div>
						<div class="sfField">
							<div id="automanf">
								<input type="text" name="manf_name"  id="manf_name" value="${ifn:cleanHtmlAttribute(param.manf_name)}"/>
								<input type="hidden" name="manf_name@op" value="ilike" />
								<div id="manfcontainer"></div>
							</div>
					    </div>
					</td>
					<td>
						<div class="sfLabel">Category</div>
						<div class="sfField">
							<insta:selectdb name="category" values="${paramValues['category']}" dummyvalue="...Select..."
								table="store_category_master" displaycol="category" valuecol="category" orderby="category" onchange="autoItem();"/>
					    </div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${list.pageNumber}" numPages="${list.numPages}" totalRecords="${list.totalRecords}"/>
</form>
<form name="storeItemRatesForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>Item</th>
			<th>Category</th>
			<th>Manufacturer</th>
			<th>Selling Price Expression</th>

		</tr>
		<c:forEach var="item" items="${list.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {medicine_id:'${item.medicine_id}', store_tariff:'${param.store_rate_plan_id}'},'');">

				<td>
					<insta:truncLabel value="${item.medicine_name}" length="50"/>
				</td>
				<td><c:out value="${item.category}"/></td>
				<td><c:out value="${item.manf_name}"/></td>
				<td><insta:truncLabel value="${item.selling_price_expr}" length="75"/></td>
			</tr>
		</c:forEach>
		</table>
	</div>

		<c:if test="${empty list.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="StoresRatePlans.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left;">
			<a href="${Url}">Add New Store Tariffs</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>

</body>
</html>
