<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Region Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/regions/show.htm?', 
				onclick: null,
				description: "View and/or Edit Region details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			autoRegionMaster();
		}
		
		var region = ${ifn:convertListToJson(referenceData.regionsList)};

		var rAutoComp;
		function autoRegionMaster() {
			var datasource = new YAHOO.util.LocalDataSource({result: region});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "region_name"},{key : "region_id"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('region_name','regioncontainer', datasource);
			rAutoComp.minQueryLength = 0;
		 	rAutoComp.maxResultsDisplayed = 20;
		 	rAutoComp.forceSelection = false ;
		 	rAutoComp.animVert = false;
		 	rAutoComp.resultTypeList = false;
		 	rAutoComp.typeAhead = false;
		 	rAutoComp.allowBroserAutocomplete = false;
		 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			rAutoComp.autoHighlight = true;
			rAutoComp.useShadow = false;
		 	if (rAutoComp._elTextbox.value != '') {
					rAutoComp._bItemSelected = true;
					rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
			}
		}
	</script>

</head>

<body onload="init()">

	<h1>Region Master</h1>

	<insta:feedback-panel/>

	<form name="RegionSearchForm" method="GET">

		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="RegionSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel">Region Name</div>
								<div class="sboFieldInput">
									<input type="text" name="region_name" id="region_name" value="${ifn:cleanHtmlAttribute(param.region_name)}" style = "width:32em" >
									<input type="hidden" name="region_name@op" value="ico" />
									<div id="regioncontainer" style = "width:32em"></div>
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

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="region_name" title="Region"/>
					<th>Status</th>
					<%-- <th>${ pagedList.dtoList[0].get("region_id")}</th>
					<th>${ pagedList.dtoList[0].get("status")}</th>
					<th>${ pagedList.dtoList[0].get("region_name")}</th> --%>
				</tr>

				
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{region_id: ${record.get('region_id')}},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td><insta:truncLabel value="${record.get('region_name')}" length="30"/></td>
						<td>${record.get('status') == 'A' ? 'Active' : 'Inactive'}</td>
					</tr>
				</c:forEach>
 			</table>
		</div>

		<c:url var="url" value="/master/regions/add.htm">
ƒ		</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add Region</a></div>
	</form>

</body>
</html>
