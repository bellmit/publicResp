<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>


<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/testlist.js" />
<script type="text/javascript">
  var tb_names = [];
  var toolBar = {
	Charges : {
		title : 'Edit Charges',
		imageSrc : 'icons/Edit.png',
		href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen',
		onclick : null,
		description : 'View and/or Edit Test Charges'
	}
}
function init() {
	createToolbar(toolBar);
}
</script>
<title>Rate Plan Overrides - Insta HMS</title>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<c:set var="testList" value="${pagedList.dtoList}"/>

<body onload="testAutoComplete(); init()" class="yui-skin-sam">

<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getChargesListScreen" />
	<input type="hidden" name="_searchMethod" value="getChargesListScreen" />
	<input type="hidden" name="chargeCategory" value="diagnostics"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Test Name</div>
				<div class="sboFieldInput">
					<input type="text" name="test_name" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}" style="width: 140px"/>
					<input type="hidden" name="test_name@op" value="ilike" />
					<div id="testContainer" style="width: 220px"></div>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Department</div>
							<div class="sfField">
							<insta:selectdb name="ddept_id" multiple="true" size="5"
								table="diagnostics_departments" valuecol="ddept_id" displaycol="ddept_name"	values="${paramValues.ddept_id}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Service Sub Group</div>
						<div class="sfField">
							<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
							<input type="hidden" name="service_sub_group_id@op" value="in"/>
							<input type="hidden" name="service_sub_group_id@type" value="integer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="eq" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Alias</div>
							<div class="sfField">
							<input type="text" name="alias_item_code" id="alias_item_code" value="${ifn:cleanHtmlAttribute(param.alias_item_code)}"/>
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
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="test_name" title="Test Name"/>
				<insta:sortablecolumn name="item_code" title="Code"/>
				<insta:sortablecolumn name="ddept_name" title="Department"/>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>
			<c:forEach var="test" items="${testList}" varStatus="st">
				<script>tb_names[${st.index}] = <insta:jsString value="${test.test_name}"/>;</script>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}" onclick="showToolbar(${st.index}, event, 'resultTable',
						{testid: '${test.test_id}', test_id: '${test.test_id}', org_id: '${ifn:cleanJavaScript(org_id)}',
						chargeCategory: 'diagnostics', fromItemMaster: 'false',
						test_name: tb_names[${st.index}]});" >
					<td>
						<c:if test="${test.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${test.status eq 'A' && test.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${test.status eq 'A' && test.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'> </c:if>
						<c:out value="${test.test_name}"/>
					</td>
					<td>${test.item_code}</td>
					<td>${test.ddept_name}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" align="right">${ifn:afmt(charges[test.test_id][bed])}</td>
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
	var testNames = ${namesJSON};
	var cpath = '${cpath}';
</script>

</body>
</html>
