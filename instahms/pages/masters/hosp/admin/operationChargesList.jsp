<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
	<title>Rate Plan Overrides - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/operation.js" />
	<insta:link type="js" file="masters/charges_common.js" />

	<script type="text/javascript">
		var tb_names = [];

		var toolBar = {
			Charges : {
				title : 'Edit Charges',
				imageSrc : 'icons/Edit.png',
				href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&fromItemMaster=false',
				onclick : null,
				description : 'View and/or Edit Operation Charges'
			}
		}

		function init() {
			createToolbar(toolBar);
		}

	</script>

</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="operationList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="initOperationAc(); init()"  class="yui-skin-sam">

<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getChargesListScreen" />
	<input type="hidden" name="_searchMethod" value="getChargesListScreen" />
	<input type="hidden" name="chargeCategory" value="operations"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Operation Name</div>
				<div class="sboFieldInput">
					<input type="text" id="operation_name" name="operation_name" value="${ifn:cleanHtmlAttribute(param.operation_name)}" style="width: 140px"/>
					<input type="hidden" name="operation_name@op" value="ilike" />
						<div style="width: 195px" id="operationAcContainer"></div>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Plan Code</div>
				<div class="sboFieldInput">
					<input type="text" name="item_code" value="${ifn:cleanHtmlAttribute(param.item_code)}"/>
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel">View Charges</div>
				<div class="sboFieldInput">
					<insta:selectoptions name="_chargeType" value="${chargeType}" opvalues="surg_asstance_charge,surgeon_charge,anesthetist_charge"
						optexts=" Surgical Assistance Charge, Surgeon Charge, Anaesthetist Charge"/>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Department</div>
							<div class="sfField">
								<insta:selectdb name="dept_id" multiple="true" size="5" table="department" valuecol="dept_id" displaycol="dept_name"
									orderby="dept_name" values="${paramValues.dept_id}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Treatment Code Type</div>
						<div class="sfField">
							<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
							displaycol="code_type" dummyvalue="--Select--" filtervalue="Treatment"
							filtercol="code_category" value="${param.code_type}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Service Sub Group</div>
						<div class="sfField">
							<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
								<input type="hidden" name="service_sub_group_id@type" value="integer" />
						</div>
					</td>

					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
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
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>

<form name="listform" action="dummy.do">
	<input type="hidden" name="_method" value="groupUpdate">
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="operation_name" title="Operation Name"/>
				<insta:sortablecolumn name="item_code" title="Code"/>
				<insta:sortablecolumn name="dept_name" title="Department"/>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>
			<c:forEach var="operation" items="${operationList}" varStatus="st">
				<script>tb_names[${st.index}] = <insta:jsString value = "${operation.operation_name}"/>;</script>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}" onclick="showToolbar(${st.index}, event, 'resultTable',
					{op_id: '${operation.op_id}', org_id: '${ifn:cleanJavaScript(org_id)}', chargeCategory: 'operations',
					operation_name: tb_names[${st.index}]}, '');" >
					<td>
						<c:if test="${operation.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${operation.status eq 'A' && operation.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${operation.status eq 'A' && operation.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
							<insta:truncLabel value="${operation.operation_name}" length="50"/>
					</td>
					<td>${operation.item_code}</td>
					<td>${operation.dept_name}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number">${ifn:afmt(charges[operation.op_id][bed].map[chargeType])}</td>
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
	    <td align="right">
			<img src='${cpath}/images/purple_flag.gif'>
				Excluded&nbsp;
			<img src='${cpath}/images/grey_flag.gif'>
				Inactive
	    </td>
	</tr>
</table>

<script>
	var cpath = '${cpath}';
	var operationNames = ${namesJSON};
</script>

</body>
</html>
