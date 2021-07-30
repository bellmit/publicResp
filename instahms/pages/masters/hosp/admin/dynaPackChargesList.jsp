<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Rate Plan Overrides - Insta HMS</title>
	<insta:link type="js" file="master/DynaPackage/DynaPackage.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js" />

	<script>
	var toolBar = {
		Charges : {
			title : 'Edit Charges',
			imageSrc : 'icons/Edit.png',
			href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&fromItemMaster=false',
			onclick : null,
			description : 'View and/or Edit Test Charges'
		}
	}
	function init() {
		createToolbar(toolBar);
	}
</script>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="dynaPackageList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="init(); loadDynapackageNamesList();">

<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getChargesListScreen" />
	<input type="hidden" name="_searchMethod" value="getChargesListScreen" />
	<input type="hidden" name="chargeCategory" value="dynapackages"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>
	<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Dynamic Package</div>
				<div class="sboFieldInput">
					<input type="text" name="dyna_package_name" id="dyna_package_name" style="width: 210px;" value="${ifn:cleanHtmlAttribute(param.dyna_package_name)}"/>
					<div style="width: 225px" name="dynanamesContainer" id="dynanamesContainer"></div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
								selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
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
</form>

<form name="listform" action="dummy.do">
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr>
				<th>Package Name</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>

			<c:forEach var="dynaPackageDetails" items="${dynaPackageList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{dyna_package_id: '${dynaPackageDetails.dyna_package_id}', org_id: '${ifn:cleanJavaScript(org_id)}',
							chargeCategory: 'dynapackages'}, '');" >
					<td>
						<c:if test="${dynaPackageDetails.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${dynaPackageDetails.status eq 'A' && dynaPackageDetails.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${dynaPackageDetails.status eq 'A' && dynaPackageDetails.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${dynaPackageDetails.dyna_package_name }
					</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" >${ifn:afmt(charges[dynaPackageDetails.dyna_package_id][bed].map['charge'])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>

<table class="screenActions" width="100%">
	<tr>
		<td><a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${ifn:cleanURL(org_id)}">Edit Rate Plan</a></td>
	   	<td  align="right">
			<img src='${cpath}/images/purple_flag.gif'>
				Excluded&nbsp;
			<img src='${cpath}/images/grey_flag.gif'>
				Inactive
	  	</td>
	</tr>
</table>

<script>
	var cpath = '${cpath}';
	var dynapackNamesList = ${namesJSON};
</script>

</body>
</html>
