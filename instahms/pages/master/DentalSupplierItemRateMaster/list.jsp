<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Dental Supplier Rate Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var suppliers = <%= request.getAttribute("suppliersList") %>;
		var itemList = <%= request.getAttribute("itemList") %>;

		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '/master/DentalSupplierRateMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Dental Supplier Rate details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.DentalSupplierRateSearchForm);
			autoItem();
			autoSuppliers();
		}

		var sAutoComp;
		var iAutoComp;

		function autoSuppliers() {
			var datasource = new YAHOO.util.LocalDataSource({result: suppliers});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "SUPPLIER_NAME"},{key : "SUPPLIER_ID"} ]
			};
			var sAutoComp = new YAHOO.widget.AutoComplete('supplier_name','suppliercontainer', datasource);
			sAutoComp.minQueryLength = 0;
		 	sAutoComp.maxResultsDisplayed = 20;
		 	sAutoComp.forceSelection = false ;
		 	sAutoComp.animVert = false;
		 	sAutoComp.resultTypeList = false;
		 	sAutoComp.typeAhead = false;
		 	sAutoComp.allowBroserAutocomplete = false;
		 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			sAutoComp.autoHighlight = true;
			sAutoComp.useShadow = false;
		 	if (sAutoComp._elTextbox.value != '') {
					sAutoComp._bItemSelected = true;
					sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
			}
		}

		function autoItem() {
			var datasource = new YAHOO.util.LocalDataSource({result: itemList});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "ITEM_NAME"},{key : "ITEM_ID"} ]
			};
			var iAutoComp = new YAHOO.widget.AutoComplete('item_name','itemcontainer', datasource);
			iAutoComp.minQueryLength = 0;
		 	iAutoComp.maxResultsDisplayed = 20;
		 	iAutoComp.forceSelection = false ;
		 	iAutoComp.animVert = false;
		 	iAutoComp.resultTypeList = false;
		 	iAutoComp.typeAhead = false;
		 	iAutoComp.allowBroserAutocomplete = false;
		 	iAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			iAutoComp.autoHighlight = true;
			iAutoComp.useShadow = false;
		 	if (iAutoComp._elTextbox.value != '') {
					iAutoComp._bItemSelected = true;
					iAutoComp._sInitInputValue = iAutoComp._elTextbox.value;
			}
		}

	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Dental Supplier Rate Master List</h1>

	<insta:feedback-panel/>

	<form name="DentalSupplierRateSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="DentalSupplierRateSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Item Name</div>
					<div class="sboFieldInput">
						<div class="sboFieldInput">
							<input type="text" name="item_name" id="item_name" value="${ifn:cleanHtmlAttribute(param.item_name)}" style="width: 140px"/>
							<input type="hidden" name="item_name@op" value="ilike" />
							<div id="itemcontainer" style="width: 220px"></div>
						</div>
					</div>
				</div>
				<div></div>
				<div class="sboField">
					<div class="sboFieldLabel">Supplier Name</div>
					<div class="sboFieldInput">
						<div class="sboFieldInput">
							<input type="text" name="supplier_name" id="supplier_name" value="${ifn:cleanHtmlAttribute(param.supplier_name)}" style="width: 140px"/>
							<input type="hidden" name="supplier_name@op" value="ilike" />
							<div id="suppliercontainer" style="width: 220px"></div>
						</div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="item_rate_status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['item_rate_status']}"/>
								<input type="hidden" name="item_rate_status@op" value="in" />
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
					<th>#</th>
					<insta:sortablecolumn name="item_name" title="Item Name"/>
					<insta:sortablecolumn name="supplier_name" title="Supplier Name"/>
					<th>Unit Rate</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{item_supplier_rate_id: '${record.item_supplier_rate_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.item_rate_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.item_rate_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> ${record.item_name}
						</td>
						<td>${record.supplier_name}</td>
						<td>${record.unit_rate}</td>
					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="DentalSupplierRateMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add Dental Supplier Item Rate</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>