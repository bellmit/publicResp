<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Patient Category Master - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="script" file="dashboardColors.js" />


<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>' />

<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/master/PatientCategoryMaster.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of the Patient Category"
				}
		};

		function init() {
			createToolbar(toolBar);
		}

		function clearSearch(form) {
			clearForm(form);
		}
	</script>

</head>

<body onload="init()">

<c:set var="hasResults"
	value="${not empty pagedList.dtoList ? 'true' : 'false'}" />

<h1>Patient Category Master</h1>

<insta:feedback-panel />

<form name="PatientCategForm" method="GET"><input type="hidden"
	name="_method" value="list" /> <input type="hidden"
	name="_searchMethod" value="list" /> <input type="hidden"
	name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}" /> <input type="hidden"
	name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}" /> <insta:search
	form="PatientCategForm" optionsId="optionalFilter"
	closed="${hasResults}" clearFunction="clearSearch">
	<div class="searchBasicOpts">
	<div class="sboField">
	<div class="sboFieldLabel">Category Name:</div>
	<div class="sboFieldInput"><input type="text"
		name="category_name" value="${ifn:cleanHtmlAttribute(param.category_name)}" /> <input
		type="hidden" name="category_name@op" value="ico" /></div>
	</div>
	</div>
	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}">
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">IP Default Rate Plan:</div>
						<div class="sfField">
							<insta:selectdb name="ip_rate_plan_id"
							id="ip_rate_plan_id" table="organization_details" valuecol="org_id"
							displaycol="org_name" dummyvalue="-- Select --" dummyvalueId=""
							value="${param.ip_rate_plan_id}" orderby="org_name" />
							<input type="hidden" name="ip_rate_plan_id@op" value="eq" />
							<input type="hidden" name="ip_rate_plan_id@type" value="string" />
						</div>
					</td>
					<td>
						<div class="sfLabel">OP Default Rate Plan:</div>
						<div  class="sfField">
							<insta:selectdb
							name="op_rate_plan_id" id="op_rate_plan_id"
							table="organization_details" valuecol="org_id" displaycol="org_name"
							dummyvalue="-- Select --" dummyvalueId=""
							value="${param.op_rate_plan_id}" orderby="org_name" />
							<input type="hidden" name="op_rate_plan_id@op" value="eq" />
							<input type="hidden" name="op_rate_plan_id@type" value="string" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Default Primary IP Sponsor:</div>
						<div  class="sfField">
							<insta:selectdb name="primary_ip_sponsor_id"
							id="primary_ip_sponsor_id" value="${param.primary_ip_sponsor_id}" table="tpa_master"
							valuecol="tpa_id" displaycol="tpa_name" dummyvalue="-- Select --"
							dummyvalueId="" orderby="tpa_name" />
							<input type="hidden" name="primary_ip_sponsor_id@op" value="eq" />
							<input type="hidden" name="primary_ip_sponsor_id@type" value="string" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Default Primary OP Sponsor:</div>
						<div class="sfField">
							<insta:selectdb name="primary_op_sponsor_id"
							id="primary_op_sponsor_id" value="${param.primary_op_sponsor_id}" table="tpa_master"
							valuecol="tpa_id" displaycol="tpa_name" dummyvalue="-- Select --"
							dummyvalueId="" orderby="tpa_name" />
							<input type="hidden" name="primary_op_sponsor_id@op" value="eq" />
							<input type="hidden" name="primary_op_sponsor_id@type" value="string" />
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div class="sfLabel">Status:</div>
						<div class="sfField">
							<insta:checkgroup name="status"
								opvalues="A,I" optexts="Active,Inactive"
								selValues="${paramValues.status}" />
							 <input type="hidden" name="status@op" value="in" />
							 <input type="hidden" name="status@type" value="string" />
							 <input type="hidden" name="status@cast" value="y">
						</div>
					</td>
					<c:if test="${max_centers_inc_default > 1}">
						<td>
							<div class="sfLabel">Center:</div>
							<div class="sfField">
								<select class="dropdown" name="center_id" id="center_id">
									<option value="">--select--</option>
									<option value="0" ${param.center_id == 0 ? 'selected' : ''}>All Centers</option>
									<c:forEach items="${centers}" var="center">
										<option value="${center.map.center_id}" ${param.center_id == center.map.center_id ? 'selected' : ''}>
											${center.map.center_name}
										</option>
									</c:forEach>
								</select>
								<input type="hidden" name="center_id@cast" value="y" />
							</div>
						</td>
					</c:if>
				</tr>
			</table>
		</div>
</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}"
	numPages="${pagedList.numPages}"
	totalRecords="${pagedList.totalRecords}" />

<div class="resultList">
<table class="resultList" cellspacing="" cellpadding="" id="resultTable"
	onmouseover="hideToolBar();">
	<tr onmouseover="hideToolBar();">
		<th>#</th>
		<insta:sortablecolumn name="category_name"
			title="Patient Category Name" />
		<c:if test="${max_centers_inc_default > 1}">
			<th>Center Name</th>
		</c:if>
		<th>IP Default Rate Plan</th>
		<th>OP Default Rate Plan</th>
		<th>IP Default Primary TPA</th>
		<th>OP Default Primary TPA</th>
		<th>IP Default Primary Ins. Company</th>
		<th>OP Default Primary Ins. Company</th>
	</tr>
	<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
		<tr
			class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
			id="toolbarRow${st.index}"
			onclick="showToolbar(${st.index}, event, 'resultTable', {category_id: '${record.category_id}'},'');">

			<td>${(pagedList.pageNumber - 1) * pagedList.pageSize +
			(st.index + 1)}</td>
			<td><c:if test="${record.status eq 'I'}">
				<img src='${cpath}/images/grey_flag.gif'>
			</c:if> <c:if test="${record.status eq 'A'}">
				<img src='${cpath}/images/empty_flag.gif'>
			</c:if>
			<insta:truncLabel value="${record.category_name}" length="30"/>
			</td>
			<c:if test="${max_centers_inc_default > 1}">
				<td>${record.center_name}</td>
			</c:if>
			<td><insta:truncLabel value="${record.ip_org_name}" length="15"/></td>
			<td><insta:truncLabel value="${record.op_org_name}" length="15"/></td>
			<td><insta:truncLabel value="${record.ip_tpa_name}" length="15"/></td>
			<td><insta:truncLabel value="${record.op_tpa_name}" length="15"/></td>
			<td><insta:truncLabel value="${record.ip_insurance_co_name}" length="15"/></td>
			<td><insta:truncLabel value="${record.op_insurance_co_name}" length="15"/></td>
		</tr>
	</c:forEach>
</table>

<c:if test="${empty pagedList.dtoList}">
	<insta:noresults hasResults="${hasResults}" />
</c:if></div>

<c:url var="Url" value="PatientCategoryMaster.do">
	<c:param name="_method" value="add" />
</c:url>

<div class="screenActions" style="float: left"><a href="${Url}">Add
New Patient Category</a></div>
<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
<div class="flagText">Inactive</div>
</div>

</form>
</body>
</html>




