<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
 <head>
 <title><insta:ltext key="registration.quickestimatelist.details.quickestimatelist.instahms"/></title>
 <insta:js-bundle prefix="registration.quickestimate"/>
  	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<script type="text/javaScript">
	var toolbarOptions = getToolbarBundle("js.registration.quickestimate.toolbar");
	var orgNamesJSON = <%= request.getAttribute("orgNameJSON") %>;
	var centerWiseOrgNameJSON = <%= request.getAttribute("centerWiseOrgNameJSON") %>;
		var toolbar = {}
				toolbar.Edit = {
					title: toolbarOptions["viewedit"]["name"],
					imageSrc: 'icons/Edit.png',
					href: 'pages/registration/QuickEstimate.do?_method=getQuickEstimateScreen',
					onclick: null,
					description: toolbarOptions["viewedit"]["description"]
		};

		function init() {
			createToolbar(toolbar);
		}

		function populateRatePlan(){

	var ratePlanObj = document.getElementById("rate_plan");
	var ratePlanList = orgNamesJSON;
	var defaultRatePlan = "";
	var allRatePlanList = new Array();
	var filterRatePlanList = new Array();
	if(!empty(centerWiseOrgNameJSON)) {
	for (var i = 0; i < centerWiseOrgNameJSON.length; i++) {
		var item = centerWiseOrgNameJSON[i];
				if (!empty(item.op_allowed_rate_plans) && item.op_allowed_rate_plans != '*') {
					var ratePlanIdList = item.op_allowed_rate_plans.split(',');
					var op_allowedRatePlans = [];
					for (var j = 0; j < ratePlanIdList.length; j++)
						op_allowedRatePlans.push(findInList(orgNamesJSON, "org_id", ratePlanIdList[j]));
					ratePlanList =  !empty(op_allowedRatePlans) ? op_allowedRatePlans : ratePlanList;
				}
				allRatePlanList.push(ratePlanList);
				if (!empty(item.ip_allowed_rate_plans) && item.ip_allowed_rate_plans != '*') {
					var ratePlanIdList = item.ip_allowed_rate_plans.split(',');
					var ip_allowedRatePlans = [];
					for (var j = 0; j < ratePlanIdList.length; j++)
						ip_allowedRatePlans.push(findInList(orgNamesJSON, "org_id", ratePlanIdList[j]));
					ratePlanList =  !empty(ip_allowedRatePlans) ? ip_allowedRatePlans : ratePlanList;
				}
				allRatePlanList.push(ratePlanList);
		}
	}
	var index = 0;
	if (ratePlanObj != null) {
		ratePlanObj.length = 1;
		ratePlanObj.options[index].text = "-- Select --";
		ratePlanObj.options[index].value = "";
	}

	for (var i = 0; i < allRatePlanList.length; i++) {
		var filterRatePlan = allRatePlanList[i];
		var sorted_filterRatePlan = filterRatePlan.sort();
		for (var j = 0; j < filterRatePlan.length; j++) {
			var found = false;
			for(var k = 0; k < filterRatePlanList.length; k++) {
				if(filterRatePlan[j] == filterRatePlanList[k]) {
					found = true;
					break;
				}
			}
    		if (!found) {
    			filterRatePlanList.push(filterRatePlan[j]);
			}
		}
	}
	if(empty(filterRatePlanList)){
		filterRatePlanList = ratePlanList;
	}

	for (var i = 0; i < filterRatePlanList.length; i++) {
		var exists = false;
		var item = filterRatePlanList[i];
		for (var k = 0; k < orgNamesJSON.length; k++) {
			var ratePlanItem = orgNamesJSON[k];
			if (!empty(item) && !empty(ratePlanItem) && (item.org_id == ratePlanItem.org_id)) {
				exists = true;
				break;
			}
		}
		if (exists) {
			index++;
			if (ratePlanObj != null) {
				ratePlanObj.length = index + 1;
				ratePlanObj.options[index].text = item.org_name;
				ratePlanObj.options[index].value = item.org_id;
			}
		}
	}
	if(ratePlanObj != null)
		sortDropDown(ratePlanObj);
}

	</script>
 </head>
  <body onload="init(),populateRatePlan();">
  	<h1><insta:ltext key="registration.quickestimatelist.details.quickestimatelist.header"/></h1>
 	<c:set var="select">
	<insta:ltext key="registration.quickestimatelist.details.select"/>
	</c:set>
	<form name="quickEstimateForm" action="QuickEstimate.do">

		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list"/>
		<insta:search form="quickEstimateForm" optionsId="optionalFilter" closed="${not empty pagedList.dtoList}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="registration.quickestimatelist.details.rateplan"/></div>
					<div class="sboFieldInput">
						<select id="rate_plan" name="rate_plan" size="1" class="dropdown"/>
						<input type="hidden" name="rate_plan@type" value="string" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="registration.quickestimatelist.details.estimateno"/></div>
					<div class="sboFieldInput">
						<input type="text" name="estimate_no" id="estimate_no" style="width: 80px" onkeypress="return enterNumOnlyzeroToNine(event);" value="${ifn:cleanHtmlAttribute(param.estimate_no)}" />
						<input type="hidden" name="estimate_no@type" value="integer">
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${not empty pagedList.dtoList ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="registration.quickestimatelist.details.bedtype"/></div>
							<div class="sfField">
								<select name="bed_type" class="dropdown">
									<option name="bed_type" value="">${select}</option>
									<c:forEach items="${allbedtypes}" var="bed">
										<option name="bed_type" value="${bed.map.bed_type}">${bed.map.bed_type}</option>
									</c:forEach>
								</select>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="registration.quickestimatelist.details.estimatefor"/></div>
							<div class="sfField">
								<input type="text" name="person_name" value="${ifn:cleanHtmlAttribute(param.person_name)}" />
								<input type="hidden" name="person_name@op" value="ilike" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="registration.quickestimatelist.details.remarks"/></div>
							<div class="sfField">
								<input type="text" name="remarks" value="${ifn:cleanHtmlAttribute(param.remarks)}"/>
								<input type="hidden" name="remarks@op" value="ilike"/>
							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
		</insta:search>

	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
  	<div class="reslutList">
  		<table class="resultList" name="listTable" cellpadding="0" cellspacing="0" width="100%">
  			<tr>
  				<th><insta:ltext key="registration.quickestimatelist.details.estimateno"/></th>
  				<th><insta:ltext key="registration.quickestimatelist.details.estimatefor"/></th>
  				<th><insta:ltext key="registration.quickestimatelist.details.rateplan"/></th>
  				<th><insta:ltext key="registration.quickestimatelist.details.bedtype"/></th>
  				<th><insta:ltext key="registration.quickestimatelist.details.visittype"/></th>
  				<th><insta:ltext key="registration.quickestimatelist.details.remarks"/></th>
  			</tr>
	  		<c:forEach var="list" items="${pagedList.dtoList}" varStatus="status">
	  			<tr class="${status.index == 0 ? 'firstRow' : ''}${status.index%2 == 0 ? 'even' : 'odd' }"
	  							onclick="showToolbar('${status.index}', event, 'listTable', {estimate_no: '${list.estimate_no}'},'');", id="toolbarRow${status.index}">
					<td>${list.estimate_no}</td>
					<td><insta:truncLabel value="${list.person_name}" length="20"/></td>
					<td>${list.org_name}</td>
					<td>${list.bed_type}</td>
					<td>${list.visit_type eq 'i' ? 'IP Patient' : 'OP Patient'}</td>
					<td><insta:truncLabel value="${list.remarks}" length="20" /></td>

	  			</tr>
	  		</c:forEach>
  		</table>
  		<insta:noresults hasResults="${not empty pagedList.dtoList}" />
  		<div class="screenActions">
			<a href="${cpath}/pages/registration/QuickEstimate.do?_method=getQuickEstimateScreen"><insta:ltext key="registration.quickestimatelist.details.quickestimate"/></a>
  		</div>
  	</div>
  </body>
 </html>

