<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Ward And Bed Master List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolbar = {
			WardDetails: {
				title: "Ward Details",
				imageSrc: "icons/Edit.png",
				href: '/pages/masters/insta/admin/WardAndBedMasterAction.do?method=getWardDetails',
				onclick: null,
				description: "View and/or Edit the contents of this Ward and Bed Master"
				},
			EditBeds: {
				title:"Edit Beds",
				imageSrc:"icons/Edit.png",
				href: 'pages/masters/insta/admin/WardAndBedMasterAction.do?method=getBedNames',
				onclick: null,
				description: "View and/of Edit the contents of this Ward and Bed Master"
				}
		};
		function init() {

			createToolbar(toolbar);

		}
	</script>

</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty wardandbeddetails ? 'true' : 'false'}"/>
<h1>Ward And Bed Master</h1>
<insta:feedback-panel/>
<form name="WardAndBedForm" method="GET">
	<input type="hidden" name="method" value="getWardandBedMaster" />

	<insta:search-lessoptions form="WardAndBedForm" >
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Ward Name</div>
				<div class="sboFieldInput">
					<input type="text" id="ward_name" name="ward_name" value="${ifn:cleanHtmlAttribute(param.ward_name)}" style="width: 140px"/>
					<input type="hidden" name="ward_name@op" value="ilike" />

				</div>
			</div>
			<div class="sboField">
				<div class="sfLabel">Status</div>
					<div class="sfField">
						<insta:checkgroup name="status" opvalues="ACTIVE,INACTIVE" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in" />
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
								${param.center_id == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
						</c:forEach>
					</select>
					<input type="hidden" name="center_id@cast" value="y"/>
				</div>
			</div>
		</c:if>
	</insta:search-lessoptions>
	<insta:paginate curPage="${wardandbeddetails.pageNumber}" numPages="${wardandbeddetails.numPages}" totalRecords="${wardandbeddetails.totalRecords}"/>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<th>Ward Name</th>
			<c:if test="${ multiCenters }">
				<th>Center</th>
			</c:if>
			<th>Bed Type</th>
			<th>No.Of Beds(Active)</th>
			<th>Description</th>
		</tr>
		<c:forEach var="record" items="${wardandbeddetails.dtoList}" varStatus="st">

			<c:set var="newScreen" value="${record.bed_status eq 'A'}"/>

			<c:set var="editBeds" value="${(record.active_count != 0 || record.inactive_count != 0) && record.bed_status eq 'A'}"/>


			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{wardId: '${record.ward_no }',bedType: '${record.bed_type}'},
						 [${newScreen},${editBeds}]);" >

						<td>
							${(wardandbeddetails.pageNumber - 1) * wardandbeddetails.pageSize + (st.index + 1)}
						</td>
						<td>
							<c:if test="${record.status eq 'INACTIVE'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'ACTIVE'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.ward_name}
						</td>
						<c:if test="${ multiCenters }">
							<td><insta:getCenterName center_id="${record.center_id}"/></td>
						</c:if>
						<td>
							<font color="${record.bed_status eq 'I' ? 'purple' : ''}">${record.bed_type}</font>
						</td>
						<td >
							${record.active_count}
						</td>
						<c:if test="${row.map.active_count == 0 && row.map.inactive_count == 0}">
							<td></td>
						</c:if>
						<td>
							${record.description}
						</td>

			</tr>
		</c:forEach>
	</table>
</div>

	<c:if test="${empty wardandbeddetails}">
			<insta:noresults hasResults="${hasResults}"/>
	</c:if>

	<c:url var="url" value="WardAndBedMasterAction.do">
			<c:param name="method" value="getNewWardScreen"/>
	</c:url>

	<div class="screenActions" style="float: left">
		<a href="<c:out value='${url}' />">Add New Ward</a>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>

</form>

</body>
</html>
