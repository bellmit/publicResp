<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<head>
	<title>Rate Plan Overrides - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="masters/packmaster.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<style type="text/css">
	.status_A.type_P {background-color: #D9EABB;}
	.status_A.type_T {background-color: #C5D9A3;}
	.status_I.type_P {background-color: #D9EABB; color:grey;}
	.status_I.type_T {background-color: #C5D9A3; color:grey; }

    table.legend { border-collapse: collapse; margin-left: 6px; }
	table.legend td { border: 1px solid grey; padding: 2px 5px;}

	</style>

		<script type="text/javascript">
		var toolbar = {
			Edit_Charge: {
				title: "Edit Charges",
				imageSrc: "icons/Edit.png",
				href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&fromItemMaster=false',
				onclick: null,
				description: 'View and/or Edit Package Charges'
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.PackageSearchForm);
			initOperationsAutocomplete();
		}
		function initOperationsAutocomplete(){
			var datasource = new YAHOO.widget.DS_JSArray(${namesJSON});
			var operationAC = new YAHOO.widget.AutoComplete('package_name','packagenameAcContainer', datasource);
			operationAC.maxResultsDisplayed = 15;
			operationAC.allowBrowserAutocomplete = false;
			operationAC.prehighlightClassName = "yui-ac-prehighlight";
			operationAC.typeAhead = false;
			operationAC.useShadow = false;
			operationAC.minQueryLength = 1;
			operationAC.animVert = false;
			operationAC.autoHighlight = false;
	  	}
	</script>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<body onload="init();" class="yui-skin-sam">

	<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>

	<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="PackageSearchForm">

		<input type="hidden" name="_method" value="getChargesListScreen" />
		<input type="hidden" name="_searchMethod" value="getChargesListScreen"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="chargeCategory" value="packages"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
		<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<insta:search form="PackageSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Package Name</div>
					<div class="sboFieldInput">
						<input type="text" id="package_name" name="package_name" value="${ifn:cleanHtmlAttribute(param.package_name)}"/>
							<input type="hidden" name="package_name@op" value="ico" />
								<div id="packagenameAcContainer" style="width: 300px;"></div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Type:</div>
							<div class="sfField">
								<insta:checkgroup name="package_type" opvalues="i,o,d" optexts="IP,OP,Diag" selValues="${paramValues.package_type}"/>
								<input type="hidden" name="package_type@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Service Sub Group:</div>
							<div class="sfField">
								<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
								<input type="hidden" name="service_sub_group_id@type" value="integer" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="package_active" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.package_active}"/>
								<input type="hidden" name="package_active@op" value="in" />
							</div>
							<input type="hidden" name="type" id="type" value="P"/>
						</td>
						<td class="last">
							<div class="sfLabel">Overrided</div>
							<div class="sfField">
								<insta:checkgroup name="is_override" opvalues="Y,N" optexts="Yes,No" selValues="${paramValues.is_override}"/>
									<input type="hidden" name="is_override@op" value="in" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<c:forEach var="charge" items="${pagedList.dtoList}" varStatus="st">
					<c:if test="${charge[3] ne 'T'}">
					<c:choose>
							<c:when test="${st.index eq 0}">
								<tr onmouseover="hideToolBar();">
									<c:forEach var="bed" items="${charge}">
										<c:choose>
										<c:when test="${bed == 'GENERAL'}">
											<th style="width: 2em; overflow: hidden">GENERAL/OP</th>
										</c:when>
										<c:when test="${bed == 'Package Name'}">
											<insta:sortablecolumn name="package_name" title="${bed}"/>
										</c:when>
										<c:otherwise>
											<th style="width: 2em; overflow: hidden">${bed}</th>
										</c:otherwise>
									</c:choose>
									</c:forEach>
								</tr>
							</c:when>
							<c:otherwise>
							<c:set var="packId" value="${charge[1]}"/>
							<c:set var="temp_type" value="${charge[3]}"/>
							<c:set var="editChargesEnabled" value="${ temp_type eq 'P'}" />
							<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'resultTable',
									{packId: '${packId}', org_id: '${ifn:cleanJavaScript(org_id)}', chargeCategory:'packages', package_id:'${packId}'},
									[true, ${editChargesEnabled}]);" id="toolbarRow${st.index}">

								<c:set var="colCount" value="0" />
								<c:set var="stat" value="${charge[0]}"/>
								<c:set var="applicable" value="${charge[4]}"/>
								<c:forEach var="charges" items="${charge}">
									<c:choose>
										<c:when test="${colCount eq 0}">
											<c:set var="colCount" value="1" />
											<c:set var="status" value="${charges}" />
										</c:when>
										<c:when test="${colCount eq 1}">
											<c:set var="colCount" value="2" />
											<c:set var="packId" value="${charges}" />
										</c:when>
										<c:when test="${colCount eq 2}">
											<td>
												<c:if test="${stat=='A' && temp_type=='P' && applicable=='t'}"><img src="${cpath}/images/empty_flag.gif"></c:if>
												<c:if test="${stat=='I' && temp_type=='P'}"><img src="${cpath}/images/grey_flag.gif"></c:if>
												<c:if test="${stat=='A' && temp_type=='P' && applicable=='f'}"><img src="${cpath}/images/purple_flag.gif"></c:if>
												${charges}
											</td>
											<c:set var="colCount" value="3" />
											<c:set var="testName" value="${charges}" />
										</c:when>
										<c:when test="${(colCount eq 3)}">
											<c:set var="colCount" value="4" />
											<c:set var="type" value="${charges}"/>
										</c:when>
										<c:when test="${(colCount eq 4)}">
											<c:set var="colCount" value="5"/>
											<c:set var="appl" value="${charges}"/>
										</c:when>
										<c:when test="${(colCount eq 5)}">
											<c:set var="colCount" value="6" />
											<c:set var="multiVisitPackage" value="${charges}"/>
										</c:when>
										<c:when test="${(colCount eq 6 ) or (colCount eq 7) or (colCount eq 8)}">
											<td class="number">${charges}</td>
											<c:set var="colCount" value="${colCount + 1}" />
										</c:when>
										<c:when test="${colCount eq 9}">
											<td class="number">${charges}</td>
											<c:set var="colCount" value="${colCount + 1}" />
										</c:when>
										<c:otherwise>
											<td class="number">${charges}</td>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</tr>
							</c:otherwise>
						</c:choose>
						</c:if>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

	<table class="screenActions" width="100%">
		<tr>
			<td><a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${ifn:cleanURL(org_id)}">Edit Rate Plan</a></td>
			<td  align="right">
				<img src='${cpath}/images/empty_flag.gif'>
					Active Package&nbsp;
				<img src='${cpath}/images/purple_flag.gif'>
					Excluded&nbsp;
				<img src='${cpath}/images/grey_flag.gif'>
					Inactive Package
		  	</td>
		</tr>
	</table>
</form>
</body>
</html>
