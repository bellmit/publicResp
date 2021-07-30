<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>${ifn:cleanHtml(screen)} Exclusion - Insta HMS</title>
	<insta:link type="js" file="master/DynaPackage/DynaPackage.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js" />
	<script>
		function validateAll(){
			document.dynaPkgForm.submit();
		}
		function setApplicable(index) {
			var checkbox = document.getElementById("selectPackage"+index);
			var applicable = document.getElementById("applicable"+index);
			if(checkbox.checked)
				applicable.value = 'false';
			else
				applicable.value = 'true';
		}
	</script>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="dynaPackageList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="loadDynapackageNamesList();">
<h1>${ifn:cleanHtml(screen)} Exclusion - ${ifn:cleanHtml(org_name)}</h1>
<insta:feedback-panel />
<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getExcludeChargesScreen" />
	<input type="hidden" name="_searchMethod" value="getExcludeChargesScreen" />
	<input type="hidden" name="chargeCategory" value="dynapackages"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

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
			<table  class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
								selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Charges</div>
						<div class="sfField">
								<insta:checkgroup name="applicable" opvalues="true,false" optexts="Included Only,Excluded Only"
										selValues="${paramValues.applicable}"/>
										<input type="hidden" name="applicable@op" value="in" />
										<input type="hidden" name="applicable@cast" value="y"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>

<form name="dynaPkgForm" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="excludeCharges">
	<input type="hidden" name="chargeCategory" value="dynapackages"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px;">
					<input type="checkbox" name="allPagePackages" onclick="selectAllItems('selectPackage',this)"/></th>
				<th>Package Name</th>
			</tr>

			<c:forEach var="dynaPackageDetails" items="${dynaPackageList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">
					<td>
						<c:set var="selected" value="${dynaPackageDetails.applicable=='false'?'checked':''}"/>
						<input type="checkbox" id="selectPackage${st.index}" name="selectPackage"
							value="${dynaPackageDetails.dyna_package_id}" ${selected} onclick="setApplicable(${st.index});">
						<input type="hidden" name="applicable" id="applicable${st.index}" value="${dynaPackageDetails.applicable}"/>
						<input type="hidden" name="category_id" id="category_id${st.index}" value="${dynaPackageDetails.dyna_package_id}"/>

					</td>
					<td>
						<c:if test="${dynaPackageDetails.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${dynaPackageDetails.status eq 'A' && dynaPackageDetails.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${dynaPackageDetails.status eq 'A' && dynaPackageDetails.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${dynaPackageDetails.dyna_package_name }
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<table class="screenActions" width="100%">
		<tr>
			<td>
				<input type="button" name="exclude" id="exclude" value="Exclude" onclick="validateAll();"/>|
				<a href="${cpath}${screenURL}${ifn:cleanURL(org_id)}">Edit ${ifn:cleanHtml(screen)}</a>
			</td>
		   	<td  align="right">
				<img src='${cpath}/images/purple_flag.gif'>
					Excluded&nbsp;
				<img src='${cpath}/images/grey_flag.gif'>
					Inactive
		  	</td>
		</tr>
	</table>

</form>

<insta:noresults hasResults="${hasResults}"/>
	<script>
		var cpath = '${cpath}';
		var dynapackNamesList = ${namesJSON};
	</script>

</body>
</html>
