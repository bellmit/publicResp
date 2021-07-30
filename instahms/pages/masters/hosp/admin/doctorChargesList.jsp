<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Rate Plan Overrides - Insta HMS</title>
		<insta:link type="js" file="date_go.js" />
		<insta:link type="js" file="ajax.js" />
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="dashboardColors.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="masters/Adddoctor.js" />
		<insta:link type="js" file="masters/charges_common.js" />

		<script>
			var op_prescribe = ${urlRightsMap.op_prescribe == 'A'};
			var toolBar = {
				Charges : {
					title : 'Edit Charges',
					imageSrc : 'icons/Edit.png',
					href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&fromItemMaster=false',
					onclick : null,
					description : 'View and/or Edit Doctor Charges'
				}
			}
			function init() {
				createToolbar(toolBar);
			}
		</script>

	</head>

	<c:set var="cpath">${pageContext.request.contextPath }</c:set>
	<c:set var="doctorList" value="${pagedList.dtoList}" />
	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

	<body onload="autoCompleteDoctors();init();" class="yui-skin-sam">
		<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
			<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>
			<input type="hidden" name="_method" value="getDoctorChargesList" />
			<input type="hidden" name="_searchMethod" value="getDoctorChargesList" />
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
			<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

			<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
				<div class="searchBasicOpts">
					<div class="sboField">
						<div class="sboFieldLabel">Doctor Name</div>
						<div class="sboFieldInput">
							<input type="text" id="doctor_name" name="doctor_name" value="${ifn:cleanHtmlAttribute(param.doctor_name)}"
								style="width:140px;"/>
								<input type="hidden" name="doctor_name@op" value="ilike"/>
								<div id="doctorContainer"></div>
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel">Payment Category</div>
						<div class="sboFieldInput">
							<insta:selectdb name="payment_category" value="${param.payment_category}" table="category_type_master" valuecol="cat_id"
								displaycol="cat_name" dummyvalue="-- Select --"/>
								<input type="hidden" name="payment_category@type" value="integer">
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel">View Charges For</div>
						<div class="sboFieldInput">
							<select name="_charge_type" class="dropdown" style="width : 220px">
								<c:forEach var="chrgValue" items="${chargeValues}">
								<option value="${chrgValue}" ${chrgValue == param._charge_type ? 'selected':''}>${chargeMap[chrgValue]}</option>
								</c:forEach>
							</select>
						</div>
					</div>
				</div>

				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Department</div>
								<div class="sfField">
									<insta:selectdb name="dept_id" multiple="true" table="department" valuecol="dept_id" displaycol="dept_name"
											orderby="dept_name" values="${paramValues.dept_id}" />
									<input type="hidden" name="dept_id@op" value="in"/>
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
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
				<tr onmouseover="hideToolBar();">
					<insta:sortablecolumn name="doctor_name" title="Doctor Name"/>
					<insta:sortablecolumn name="dept_name" title="Department"/>
					<c:choose>
						<c:when test="${charge_type eq 'op_charge' || charge_type eq 'op_revisit_charge' ||
							charge_type eq 'private_cons_charge' || charge_type eq 'private_cons_revisit_charge' }">
							<th style="width: 2em; overflow: hidden" class="number">Consultation Charge</th>
						</c:when>
						<c:otherwise>
							<c:forEach var="bed" items="${bedTypes}">
									<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
								</c:forEach>
						</c:otherwise>
					</c:choose>
				</tr>
				<c:forEach var="doctor" items="${pagedList.dtoList}" varStatus="st">

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
								{doctor_id: '${doctor.doctor_id}', org_id: '${ifn:cleanJavaScript(org_id)}', mode: 'update', chargeCategory:'doctor'}, '');" >
						<td>
							<c:if test="${doctor.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${doctor.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${doctor.doctor_name}
						</td>
						<td>
							${doctor.dept_name}
						</td>
						<c:choose>
							<c:when test="${charge_type eq 'op_charge' || charge_type eq 'op_revisit_charge' || charge_type eq 'private_cons_charge' || charge_type eq 'private_cons_revisit_charge' }">
								<td class="number" >${ifn:afmt(charges[doctor.doctor_id][org_id].map[charge_type])}</td>
							</c:when>
							<c:otherwise>
								<c:forEach var="bed" items="${bedTypes}">
									<td class="number">${ifn:afmt(charges[doctor.doctor_id][bed].map[charge_type])}</td>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>
		</div>
	</form>

	<table class="screenActions" width="100%">
		<tr>
			<td><a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${ifn:cleanURL(org_id)}">Edit Rate Plan</a></td>
			<td align="right">
				<img src='${cpath}/images/grey_flag.gif'>
				 Inactive
			</td>
		</tr>
	</table>

	<script>
		var doctorNames = ${requestScope.namesJSON};
		var cpath = '${cpath}';
	</script>

</body>
</html>
