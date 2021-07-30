<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
<head>
	<title>${ifn:cleanHtml(screen)} Exclusion - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="masters/operation.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<script type="text/javascript">
		// to handle special chars in services names for toolbar
		var tb_names = [];
		function validateAll(){
			document.operationForm.submit();
		}
		function setApplicable(index) {
			var checkbox = document.getElementById("selectOperation"+index);
			var applicable = document.getElementById("applicable"+index);
			if(checkbox.checked)
				applicable.value = 'false';
			else
				applicable.value = 'true';
		}
	</script>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="operationList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="initOperationAc();  init()"  class="yui-skin-sam">

<h1>${ifn:cleanHtml(screen)} Exclusion - ${ifn:cleanHtml(org_name)}</h1>
<insta:feedback-panel/>

<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getExcludeChargesScreen" />
	<input type="hidden" name="_searchMethod" value="getExcludeChargesScreen"/>
	<input type="hidden" name="chargeCategory" value="operations"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

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

<form name="operationForm" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="excludeCharges">
	<input type="hidden" name="chargeCategory" value="operations"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th style="padding-top: 0px;padding-bottom: 0px;">
					<input type="checkbox" name="allPageOperations" onclick="selectAllItems('selectOperation',this)"/>
				</th>
				<insta:sortablecolumn name="operation_name" title="Operation Name"/>
				<insta:sortablecolumn name="item_code" title="Code"/>
				<insta:sortablecolumn name="dept_name" title="Department"/>
			</tr>
			<c:forEach var="operation" items="${operationList}" varStatus="st">
				<script>tb_names[${st.index}] = <insta:jsString value = "${operation.operation_name}"/>;</script>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}" >
					<td>
						<c:set var="selected" value="${operation.applicable=='false'?'checked':''}"/>
						<input type="checkbox" id="selectOperation${st.index}" name="selectOperation"
							value="${operation.op_id}" ${selected} onclick="setApplicable(${st.index});">
						<input type="hidden" name="applicable" id="applicable${st.index}" value="${operation.applicable}"/>
						<input type="hidden" name="category_id" id="category_id${st.index}" value="${operation.op_id}"/>
					</td>
					<td>
						<c:if test="${operation.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${operation.status eq 'A' && operation.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${operation.status eq 'A' && operation.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
							<insta:truncLabel value="${operation.operation_name}" length="50"/>
					</td>
					<td>${operation.item_code}</td>
					<td>${operation.dept_name}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<table class="screenActions" width="100%">
		<tr>
			<td>
				<input type="button" name="exclude" id="exclude" value="Exclude" onclick="validateAll();"/>|
				<a href="${cpath}${screenURL}${ifn:cleanURL(org_id)}">Edit ${ifn:cleanHtml(screen)}</a>
			</td>
		   	<td  align="right">
				<img src='${cpath}/images/purple_flag.gif'>
					Excluded&nbsp;
				<img src='${cpath}/images/grey_flag.gif'>
					Inactive
		  	</td>
		</tr>
	</table>

</form>

<insta:noresults hasResults="${hasResults}"/>

<script>
	var cpath = '${cpath}';
	var operationNames = ${namesJSON};
</script>

</body>
</html>
