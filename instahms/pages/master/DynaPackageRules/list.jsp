<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Dyna Package Rules List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="master/DynaPackageRules/dynaPkgRules.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/DynaPackageRulesMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit the contents of this Dyna Package Rule"
				}
		};

		function init() {
			createToolbar(toolbar);
			showFilterActive(document.dynaPkgRulesForm);
		}
	</script>
</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>Dyna Package Rules Master</h1>
<insta:feedback-panel/>
<form name="dynaPkgRulesForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="dynaPkgRulesForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Priority:</div>
				<div class="sboFieldInput">
					<input type="text" name="priority" value="${ifn:cleanHtmlAttribute(param.priority)}" style="text-align:right"/>
					<input type="hidden" name="priority@type" value="integer" />
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Category Name:</div>
				<div class="sboFieldInput">
					<insta:selectdb id="dyna_pkg_cat_id" name="dyna_pkg_cat_id" table="dyna_package_category"
					valuecol="dyna_pkg_cat_id" displaycol="dyna_pkg_cat_name" value="${param.dyna_pkg_cat_id}"
					filtered="false" orderby="dyna_pkg_cat_name" dummyvalue="-- Select --"/>
					<input type="hidden" name="dyna_pkg_cat_id@type" value="integer"/>
				</div>
			</div>
			<c:if test="${max_centers_inc_default > 1}">
				<div class="sboField">
					<div class="sboFieldLabel">Center:</div>
					<div class="sboFieldInput">
						<insta:selectdb  name="center_id"  table="hospital_center_master"
						valuecol="center_id" displaycol="center_name" orderby="center_name"
						dummyvalue="-- Select --" value="${param.center_id}"/>
						<input type="hidden" name="center_id@type" value="text" />
					</div>
				</div>
			</c:if>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table  class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabelSub">Charge Group:</div>
						<div class="sfField">
							<insta:selectdb  name="chargegroup_id" value="${param.chargegroup_id}"
							table="chargegroup_constants" valuecol="chargegroup_id" displaycol="chargegroup_name"
							filtered="false" dummyvalue="-- Select --" orderby="chargegroup_name"/>
							<input type="hidden" name="chargegroup_id@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabelSub">Charge Head:</div>
						<div class="sfField">
							<insta:selectdb  id="chargehead_id" name="chargehead_id" value="${param.chargehead_id}"
							table="chargehead_constants" valuecol="chargehead_id" displaycol="chargehead_name"
							filtered="false" dummyvalue="-- Select --" orderby="chargehead_name"/>
							<input type="hidden" name="chargehead_id@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabelSub">Service Group:</div>
						<div class="sfField">
							<insta:selectdb id="service_group_id" name="service_group_id" value="${param.service_group_id}"
							table="service_groups" valuecol="service_group_id" displaycol="service_group_name"
							filtered="false" dummyvalue="-- Select --" orderby="service_group_name"/>
							<input type="hidden" name="service_group_id@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabelSub">Service Sub Group:</div>
						<div class="sfField">
							<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value="${param.service_sub_group_id}"
							table="service_sub_groups" valuecol="service_sub_group_id" displaycol="service_sub_group_name"
							filtered="false" dummyvalue="-- Select --" orderby="service_sub_group_name"/>
							<input type="hidden" name="service_sub_group_id@op" value="in" />
						</div>
					</td>
					<td>
						<div class="sfLabelSub">Activity/Item Name:</div>
						<div class="sfField">
							<input type="text" name="activity_name" id="activity_name" value="${ifn:cleanHtmlAttribute(param.activity_name)}"/>
							<input type="hidden" name="activity_name@op" value="ico" />
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
</form>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<form action="DynaPackageRulesMaster.do" name="dynaruleform" />
	<c:if test="${not empty pagedList.dtoList}">
		<input type="hidden" name="_method" value="">
		<div class="screenActions">
			<input type="button" name="reorder" value="Renumber Priority" onclick=" return reorderPriorityValues();"/>
		</div>
	</c:if>
<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>Delete</th>
			<insta:sortablecolumn name="priority" title="Pri."/>
			<insta:sortablecolumn name="chargegroup_name" title="Charge Group"/>
			<insta:sortablecolumn name="chargehead_name" title="Charge Head"/>
			<insta:sortablecolumn name="service_group_name" title="Service Group"/>
			<insta:sortablecolumn name="service_sub_group_name" title="Service Sub Group"/>
			<c:if test="${max_centers_inc_default > 1}">
				<insta:sortablecolumn name="center_id" title="Center Name"/>
			</c:if>
			<insta:sortablecolumn name="activity_type" title="Activity/Item Type"/>
			<insta:sortablecolumn name="service_group_name" title="Activity/Item Name"/>
			<insta:sortablecolumn name="activity_name" title="Category Name"/>
		</tr>

		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
				{pkg_rule_id: '${record.pkg_rule_id}'}, '');" id="toolbarRow${st.index}">

				<td>
					<input type="checkbox" name="_ruleDelete" value="${record.priority}" />
				</td>

				<td>${record.priority}</td>
				<td>
					<c:choose>
						<c:when test="${record.chargegroup_name eq null}">(All)</c:when>
						<c:otherwise>
							<insta:truncLabel value="${record.chargegroup_name}" length="15"/>
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:choose>
						<c:when test="${record.chargehead_name eq null}">(All)</c:when>
						<c:otherwise>
							<insta:truncLabel value="${record.chargehead_name}" length="15"/>
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:choose>
						<c:when test="${record.service_group_name eq null}">(All)</c:when>
						<c:otherwise>
							<insta:truncLabel value="${record.service_group_name}" length="15"/>
						</c:otherwise>
					</c:choose>
				</td>
				<td>
					<c:choose>
						<c:when test="${record.service_sub_group_name eq null}">(All)</c:when>
						<c:otherwise>
							<insta:truncLabel value="${record.service_sub_group_name}" length="15"/>
						</c:otherwise>
					</c:choose>
				</td>
				<c:if test="${max_centers_inc_default > 1}">
					<td>
						<c:choose>
							<c:when test="${record.center_id eq '*'}">(All)</c:when>
							<c:otherwise>${record.center_name}</c:otherwise>
						</c:choose>
					</td>
				</c:if>
				<td>
					<c:set var="activityType" value="" />
					<c:choose>
						<c:when test="${record.activity_type == '_ALL_DOCTORS'}">
							<c:set var="activityType" value="All Doctors" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_SERVICES'}">
							<c:set var="activityType" value="All Services" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_DIET'}">
							<c:set var="activityType" value="All Diet" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_LABTESTS'}">
							<c:set var="activityType" value="All Lab. Tests" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_RADTESTS'}">
							<c:set var="activityType" value="All Rad. Tests" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_OPERATIONS'}">
							<c:set var="activityType" value="All Surgeries" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_EQUIPMENTS'}">
							<c:set var="activityType" value="All Equipments" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_PACKAGES'}">
							<c:set var="activityType" value="All Packages" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_OTHER_CHARGES'}">
							<c:set var="activityType" value="All Other Charges" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_NORMAL_BEDTYPES'}">
							<c:set var="activityType" value="All Normal Bedtypes" />
						</c:when>
						<c:when test="${record.activity_type == '_ALL_ICU_BEDTYPES'}">
							<c:set var="activityType" value="All ICU Bedtypes" />
						</c:when>
						<c:when test="${record.activity_type == '*'}">
							<c:set var="activityType" value="(All)" />
						</c:when>
						<c:otherwise>
							<c:set var="activityType" value="${record.item_type}" />
						</c:otherwise>
					</c:choose>
					<insta:truncLabel value="${activityType}" length="25"/>
				</td>
				<td><insta:truncLabel value="${empty(record.activity_name) ? '(All)' : record.activity_name}" length="45"/></td>
				<td><insta:truncLabel value="${record.dyna_pkg_cat_name}" length="45"/></td>
			</tr>
		</c:forEach>
	</table>

	<c:url value="DynaPackageRulesMaster.do" var="addUrl">
		<c:param name="_method" value="add" />
	</c:url>

	<div class="screenActions">
		<input type="button" name="delete" value="Delete Rule" onclick="return deleteRules();"/>
		|
		<a href="<c:out value='${addUrl}'/>">Add New Pkg Rule</a>
	</div>
</div>

<c:if test="${empty pagedList.dtoList}"> <insta:noresults hasResults="${hasResults}"/> </c:if>

</form>
<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText">Inactive</div>
</div>

</body>
</html>
