<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="billing.patientrewardpoints.list.title"/></title>
	<insta:js-bundle prefix="billing.rewardpoints"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.billing.rewardpoints.toolbar");
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing/commonFilters.js"/>
	<insta:link type="script" file="billing/rewardPoints.js"/>
	<style type="text/css">
		.autocomplete1 {
			width:130px;
			padding-bottom:10px;
		}
	</style>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty patientList}"/>

<body onload="initSearch(); showFilterActive(document.searchForm)" >
<c:set var="visitstatus">
<insta:ltext key="salesissues.prescriptionlist.list.ip"/>,
<insta:ltext key="salesissues.prescriptionlist.list.op"/>
</c:set>
<c:set var="activeStatus">
<insta:ltext key="salesissues.prescriptionlist.list.active"/>,
<insta:ltext key="salesissues.prescriptionlist.list.inactive"/>
</c:set>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<div class="pageHeader"><insta:ltext key="billing.patientrewardpoints.list.patientrewardpoints"/></div>

<form method="GET" action="${cpath}/billing/RewardPoints.do" name="searchForm">

<input type="hidden" name="_method" value="getRewardPoints" id="_method">
<input type="hidden" name="_searchMethod" value="getRewardPoints" id="_searchMethod">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="billing.patientrewardpoints.list.mrno.or.patientname"/>: </div>
			<div class="sboFieldInput">
				<div id="mrnoAutoComplete">
					<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
					<div id="mrnoContainer"></div>
				</div>
			</div>
		</div>
	</div>
<div id="optionalFilter" style="clear:both; display:${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientrewardpoints.list.rewardpointstotal"/>:</div>
				<div class="sfField">
					<input type="text" name="total_points_earned" size="10"
					onkeypress="return enterNumOnly(event);"
					value="${ifn:cleanHtmlAttribute(param.total_points_earned)}" class="number"/> <insta:ltext key="billing.patientrewardpoints.list.ormore"/>
					<input type="hidden" name="total_points_earned@type" value="integer"/>
					<input type="hidden" name="total_points_earned@op" value="ge"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientrewardpoints.list.rewardpointsavailable"/>:</div>
				<div class="sfField">
					<input type="text" name="total_points_available" size="10" value="${ifn:cleanHtmlAttribute(param.total_points_available)}"
	                onkeypress="return enterNumOnly(event);" class="number"/> <insta:ltext key="billing.patientrewardpoints.list.ormore"/>
					<input type="hidden" name="total_points_available@type" value="integer"/>
					<input type="hidden" name="total_points_available@op" value="ge"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientrewardpoints.list.status"/>:</div>
				<div class="sfField">
					 <insta:checkgroup name="visit_status" selValues="${paramValues.visit_status}"
					 opvalues="A,I" optexts="${activeStatus}"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientrewardpoints.list.country"/>:</div>
				<div class="sfField">
					<div id="autocountry">
						<input type="text" name="country_name" id="country_name"  value="${ifn:cleanHtmlAttribute(param.country_name)}" />
						<div id="country_dropdown" class="autocomplete1"></div>
						<input type="hidden" name="country_name@op" value="ilike">
					</div>
				</div>
				<div class="sfLabel" style="margin-top:30px"><insta:ltext key="billing.patientrewardpoints.list.state"/>:</div>
				<div class="sfField" style="margin-bottom:30px;">
					<div id="autostate">
						<input type="text" name="state_name" id="state_name" value="${ifn:cleanHtmlAttribute(param.state_name)}" />
						<div id="state_dropdown" class="autocomplete1"></div>
					</div>
				</div>
			</td>
			<td class="last">
				<div class="sfLabel"><insta:ltext key="billing.patientrewardpoints.list.city"/>:</div>
				<div class="sfField">
					<div class="autocity" >
						<input type="text" name="city_name" id="city_name" value="${ifn:cleanHtmlAttribute(param.city_name)}" />
						<div id="city_dropdown" class="autocomplete1"></div>
					</div>
				</div>
				<div class="sfLabel" style="margin-top:30px;"><insta:ltext key="billing.patientrewardpoints.list.area"/>:</div>
				<div class="sfField" style="margin-bottom:30px;">
					<div id="autoarea">
						<input type="text" name="patient_area" id="patient_area" value="${ifn:cleanHtmlAttribute(param.patient_area)}"/>
						<div id="area_dropdown" class="autocomplete1"></div>
					</div>
				</div>
			</td>
			</tr>
	</table>
</div>
</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}"
				numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0"
				align="center" width="100%" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar('');">
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<th><insta:ltext key="billing.patientrewardpoints.list.age.or.gender"/></th>
				<th><insta:ltext key="billing.patientrewardpoints.list.mobileno"/></th>
				<th class="number"><insta:ltext key="billing.patientrewardpoints.list.totalrewardpoints"/></th>
				<th class="number"><insta:ltext key="billing.patientrewardpoints.list.totalpointsredeemed"/></th>
				<th class="number"><insta:ltext key="billing.patientrewardpoints.list.totalopenpointsredeemed"/></th>
				<th class="number"><insta:ltext key="billing.patientrewardpoints.list.totalavailablepoints"/></th>
			</tr>

		<c:forEach var="patient" items="${patientList}" varStatus="status">
		<c:set var="flagColor" >
		<c:choose>
			<c:when test="${patient.map.visit_status == 'A'}">empty</c:when>
			<c:when test="${patient.map.visit_status == 'I'}">grey</c:when>
		</c:choose>
		</c:set>
			<tr class="${status.index == 0 ?'firstRow': ''}  ${status.index % 2 == 0? 'even':'odd' }"
				onclick="showToolbar(${status.index}, event, 'resultTable',{mr_no: '${patient.map.mr_no}'});"
				onmouseover="hideToolBar(${status.index})" id="toolbarRow${status.index}">

				<td>${patient.map.mr_no}</td>

				<td>
					<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
					<insta:truncLabel value="${patient.map.full_name}" length="30"/>
				</td>
				<c:set var="agein" value=""/>
				<c:choose>
					<c:when test="${patient.map.agein eq 'D'}">
						<c:set var="agein" value="Days"/>
					</c:when>
					<c:when test="${patient.map.agein eq 'M'}">
						<c:set var="agein" value="Months"/>
					</c:when>
					<c:otherwise>
						<c:set var="agein" value="Years"/>
					</c:otherwise>
				</c:choose>

				<td>${patient.map.age} ${agein}/${patient.map.patient_gender}</td>
				<td>${patient.map.patient_phone}</td>
				<td class="number">${patient.map.total_points_earned}</td>
				<td class="number">${patient.map.total_points_redeemed}</td>
				<td class="number">${patient.map.total_open_points_redeemed}</td>
				<td class="number">${patient.map.total_points_available}</td>
			</tr>
		</c:forEach>
	</table>

	<c:if test="${param.method == 'getRewardPoints'}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>

</div>
<script>
	var areaList = ${areaList};
	var countryList = ${countryList};
	var stateList = ${stateList};
	var cityList = ${cityList};
</script>

</form>

<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></img></div>
	<div class="flagText"><insta:ltext key="billing.patientrewardpoints.list.inactivepatients"/></div>
</div>
<div></div>

<c:url var="url11" value="RewardPoints.do">
<c:param name="_method" value="addRewardPoints">
</c:param>
</c:url>
<div>
	<a title="add/remove Points" href="${url11}">Add/Remove Points</a>
</div>
</body>
</html>

