<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
	<title>Dental Supplies List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="pagepath" value="<%=URLRoute.DENTAL_SUPPLIES_MASTER %>" />
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/show.htm?',
				description: "Edit Item Details"
			}
		}
		function init() {
			createToolbar(toolbar);
			autoItem();
		}
		var itemList = ${ifn:convertListToJson(itemNames)};
		var autoComp;
		function autoItem() {
			var datasource = new YAHOO.util.LocalDataSource({result: itemList});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "item_name"},{key : "item_id"} ]
			};
			var autoComp = new YAHOO.widget.AutoComplete('item','itemcontainer', datasource);
			autoComp.minQueryLength = 0;
		 	autoComp.maxResultsDisplayed = 20;
		 	autoComp.forceSelection = false ;
		 	autoComp.animVert = false;
		 	autoComp.resultTypeList = false;
		 	autoComp.typeAhead = false;
		 	autoComp.allowBroserAutocomplete = false;
		 	autoComp.prehighlightClassname = "yui-ac-prehighlight";
			autoComp.autoHighlight = true;
			autoComp.useShadow = false;
		 	if (autoComp._elTextbox.value != '') {
					autoComp._bItemSelected = true;
					autoComp._sInitInputValue = autoComp._elTextbox.value;
			}
		}

	</script>
</head>

<body onload="init()">
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty pagedList.dtoList}"/>
	<h1>Dental Supplies Master</h1>
	<insta:feedback-panel/>
	<c:url var="url" value="${pagepath}/list.htm">
	</c:url>
	<form name="DentalSuppliesSearchForm" action="${url}">
			<insta:search-lessoptions form="DentalSuppliesSearchForm">
					<table class="searchBasicOpts" >
						<tr>
							<td class="sboField" style="height: 70px">
								<div class="sboField">
									<div class="sboFieldLabel">Item: </div>
										<div class="sboFieldInput">
											<div id="autoItem">
												<input type="text" name="item_name" id="item" value="${ifn:cleanHtmlAttribute(param.item_name)}" style = "width:32em"/>
												<input type="hidden" name="item_name@op" value="ilike" />
												<div id="itemcontainer" style = "width:32em"></div>
											</div>
										</div>
				    				</div>
				 			</td>
				 			<td></td>
							<td class="sboField" style="height: 70px">
							<div class="sboField">
								<div class="sboFieldLabel">Status: </div>
									<div class="sboFieldInput">
										<insta:checkgroup name="status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.status}"/>
								</div>
							</td>
							<td></td>
						</tr>
			 	 	</table>
			</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr onmouseover="hideToolBar();">
			    <th>#</th>
				<insta:sortablecolumn name="item_name" title="Item Name"/>
				<th>Status</th>
			</tr>
           	<c:forEach items="${dtoList}" var="item" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{item_id:'${item.item_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${item.item_name}</td>
					<td>${item.status}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>
	<c:url var="url" value="${pagepath}/add.htm">
	</c:url>
	<table style="margin-top: 10px;float: left">
		<tr>
			<td><a href="<c:out value='${url}' />">Add</a></td>
		</tr>
	</table>
</body>
</html>