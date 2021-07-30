<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Rate Sheets - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/pages/masters/hosp/admin/Organ.do?_method=editOrganiaztionDetails",
				onclick : null,
				description : "View and/or Edit the contents of this Rate Sheet"
				},
			RatePlans : {
				title : "Rate Plans",
				imageSrc : "icons/Edit.png",
				href : "/pages/masters/ratePlan.do?_method=getRatePlanDetails&_searchMethod=getRatePlanDetails&sortOrder=org_name&sortReverse=false",
				onclick : null,
				description : "View the Rate Plans of this Rate Sheet"
			}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>


<h1>Rate Sheets</h1>

<insta:feedback-panel/>

<form name="RatePlanForm" method="GET">

	<input type="hidden" name="_method" value="getOrganizationDetails"/>
	<input type="hidden" name="_searchMethod" value="getOrganizationDetails"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="RatePlanForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Rate Sheet Name</div>
					<div class="sboFieldInput">
						<input type="text" name="org_name" value="${ifn:cleanHtmlAttribute(param.org_name)}" />
						<input type="hidden" name="org_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
				<c:if test="${mod_reward_points}">
					<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Eligible to earn points</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="eligible_to_earn_points" opvalues="Y,N" optexts="Yes,No" selValues="${paramValues.eligible_to_earn_points}"/>
							<input type="hidden" name="eligible_to_earn_points@op" value="ico"/>
					</div>
				</div>
				</c:if>
			</div>
		</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="org_name" title="Rate Sheet Name"/>
			<th>Rate Variation</th>
			<c:if test="${mod_reward_points}">
				<insta:sortablecolumn name="eligible_to_earn_points" title="Eligible" tooltip="Eligible to earn points"/>
			</c:if>
			<th>Mail ID</th>
			<th>Contact Person</th>
			<th>Phone</th>
			<th>Validity Period</th>

		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {orgId: '${record.org_id}', base_rate_sheet_id: '${record.org_id}'},'');">

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${record.org_name}
				</td>
				<td>${record.rate_variation}</td>
				<c:if test="${mod_reward_points}">
					<td>${record.eligible_to_earn_points == 'Y' ? 'Yes' : 'No'}</td>
				</c:if>
				<td>${record.org_mailid}</td>
				<td>${record.org_contact_person}</td>
				<td>${record.org_phone}</td>
				<fmt:parseDate value="${record.valid_from_date }" pattern="yyyy-MM-dd" var="from"/>
				<fmt:parseDate value="${record.valid_to_date }" pattern="yyyy-MM-dd" var="to"/>
				<fmt:formatDate value="${from }" pattern="dd/MM/yyyy" var="fromDate"/>
				<fmt:formatDate value="${to }" pattern="dd/MM/yyyy" var="toDate"/>
				<td>${fromDate} - ${toDate}</td>
			</tr>
		</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="Organ.do">
			<c:param name="_method" value="getNewOrganiaztionScreen"/>
		</c:url>

		<div class="screenActions" style="float: left;">
			<a href="${Url}">Add New Rate Sheet</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
</form>
<br/><br/>
<div id="content">
	
<h1>
	Rate Sheet Creation Job status
</h1>
<div class="resultList" >
	<table class="resultList" id="schedulerTable">
	<tr onmouseover="hideToolBar();">
		<th>ID</th>
		<th>Rate sheet Id</th>
		<th>Rate sheet Name</th>
		<th>Status</th>
		<th>Error</th>
	</tr>
	<c:forEach var="record" items="${createdJobList}">
		<tr>
			<td>${record.id}</td>
			<td>${record.org_id}</td>
			<td>${record.org_name}</td>
			<td>${record.status == 'P'? 'Processing': record.status == 'S'? 'Success': 'Failed'}</td>
			<td>${record.error_message}</td>
		</tr>
	</c:forEach>
	</table>
</div>

	<c:if test="${empty createdJobList}">
		<insta:noresults hasResults="${not empty createdJobList}"/>
	</c:if>
</div>


</body>
</html>
