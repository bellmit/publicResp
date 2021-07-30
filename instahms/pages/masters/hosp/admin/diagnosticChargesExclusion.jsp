<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/testlist.js" />
<script type="text/javascript">
	// for special chars handing in test name in toolbar
  var tb_names = [];

	function validateAll(){
		document.diagForm.submit();
	}
	function setApplicable(index) {
		var checkbox = document.getElementById("selectTest"+index);
		var applicable = document.getElementById("applicable"+index);
		if(checkbox.checked)
			applicable.value = 'false';
		else
			applicable.value = 'true';
	}
</script>
<title>${ifn:cleanHtml(screen)} Exclusion - Insta HMS</title>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>


<c:set var="testList" value="${pagedList.dtoList}"/>

<body onload="testAutoComplete(); init()" class="yui-skin-sam">

<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getExcludeChargesScreen" />
	<input type="hidden" name="_searchMethod" value="getExcludeChargesScreen" />
	<input type="hidden" name="chargeCategory" value="diagnostics"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<h1>${ifn:cleanHtml(screen)} Exclusion - ${ifn:cleanHtml(org_name)}</h1>
	<insta:feedback-panel/>

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
			<div></div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Plan Code</div>
				<div class="sboFieldInput">
					<input type="text" name="item_code" id="item_code" value="${ifn:cleanHtmlAttribute(param.item_code)}"/>
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
<form name="diagForm" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="excludeCharges">
	<input type="hidden" name="chargeCategory" value="diagnostics"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th style="padding-top: 0px;padding-bottom: 0px;">
				<input type="checkbox" name="allPageTests" onclick="selectAllItems('selectTest',this)"/></th>
				<insta:sortablecolumn name="test_name" title="Test Name"/>
				<insta:sortablecolumn name="item_code" title="Code"/>
				<insta:sortablecolumn name="ddept_name" title="Department"/>
			</tr>
			<c:forEach var="test" items="${testList}" varStatus="st">
				<script>tb_names[${st.index}] = <insta:jsString value="${test.test_name}"/>;</script>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}">
					<td>
						<c:set var="selected" value="${test.applicable=='false'?'checked':''}"/>
						<input type="checkbox" id="selectTest${st.index}" name="selectTest"
							value="${test.test_id}" ${selected} onclick="setApplicable(${st.index});">
						<input type="hidden" name="applicable" id="applicable${st.index}" value="${test.applicable}"/>
						<input type="hidden" name="category_id" id="category_id${st.index}" value="${test.test_id}"/>
					</td>
					<td>
						<c:if test="${test.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${test.status eq 'A' && test.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${test.status eq 'A' && test.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'> </c:if>
						<c:out value="${test.test_name}"/>
					</td>
					<td>${test.item_code}</td>
					<td>${test.ddept_name}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<div class="screenActions" align="left">
			 <input type="button" name="exclude" id="exclude" value="Exclude" onclick="validateAll();"/>|
			<a href="${cpath}${screenURL}${ifn:cleanURL(org_id)}">Edit ${ifn:cleanHtml(screen)}</a>
			<div class="legend" style="display: ${hasResults ? 'block' : 'none'}" >
				<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
				<div class="flagText">Excluded</div>
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Inactive</div>
			</div>
	</div>
</form>
<insta:noresults hasResults="${hasResults}"/>
<script>
	var testNames = ${namesJSON};
	var cpath = '${cpath}';
</script>

</body>
</html>
