<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Rate Plan Overrides - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js" />
		<insta:link type="js" file="dashboardsearch.js" />
		<insta:link type="css" file="widgets.css" />
		<insta:link type="script" file="widgets.js" />
		<insta:link type="js" file="masters/addTheatre.js" />
		<c:set value="${pageContext.request.contextPath}" var="cpath" />
		<style type="text/css">
	  		.status_I{background-color: #E4C89C}
		</style>
		<script type="text/javascript">
			var toolBar = {
			 Edit: {
					title : 'Edit Charges',
					imageSrc : 'icons/Edit.png',
					href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&fromItemMaster=false',
					onclick : null,
					description : 'Edit OT Charges'
				 }
			};

			function init() {
				createToolbar(toolBar);
			}
		</script>
	</head>

	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
	<c:set var="theatreList" value="${pagedList.dtoList}" />
	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<body onload="init();" >
		<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
			<input type="hidden" name="_method" value="getOtChargesListScreen" />
			<input type="hidden" name="_searchMethod" value="getOtChargesListScreen" />
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
			<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>
		<insta:search form="operationTheaterForm" optionsId="optionalFilter" closed="${hasResult}">
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">View Charges For:</div>
					<div class="sboFieldInput">
						<insta:selectoptions name="_chargeType" opvalues="daily_charge,min_charge,incr_charge"  value="${chargeType}"
						optexts="Daily Charge,Min Charge,Incr Charge" onchange="changeRate();"/>
					</div>
				</div>
			</div>
			<c:if test="${ multiCenters }">
				<div class="sboFieldLabel">Center:</div>
				<div class="sboFieldInput">
					<select class="dropdown" name="center_id" id="center_id">
						<option value="">-- Select --</option>
						<c:forEach items="${centers}" var="center">
							<option value="${center.map.center_id}"
								${param.centerId == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
						</c:forEach>
					</select>
					<input type="hidden" name="centerId@cast" value="y"/>
				</div>
			</c:if>
			<div id="optionalFilter" style="clear: both; display: ${hasResult ? 'none' : 'block'}">
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Status</div>
							<div class="sfField">
								<insta:checkgroup name="status" selValues="${paramValues.status}"
								optexts="Active ,Inactive" opvalues="A,I"/>
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

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}"
			totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
		<table class="resultList" id="theatreListTable" onmouseover="hideToolBar()">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="theatre_name" title="Theatre Name"/>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>
			<c:forEach var="ot" items="${theatreList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{theatre_id: '${ot.theatre_id}', org_id: '${ifn:cleanJavaScript(org_id)}', org_name: '${ifn:cleanJavaScript(org_name)}', chargeCategory: 'operationTheatre'}, '');" >
					<td>
						<c:if test="${ot.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${ot.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${ot.theatre_name}
					</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number">${ifn:afmt(charges[ot.theatre_id][bed].map[chargeType])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
	<table class="screenActions" width="100%">
		<tr>
			<td><a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${ifn:cleanURL(org_id)}">Edit Rate Plan</a></td>
			<td align="right">
				<img src='${cpath}/images/grey_flag.gif'>Inactive Theatre
			</td>
		</tr>
	</table>
</form>
</body>
</html>