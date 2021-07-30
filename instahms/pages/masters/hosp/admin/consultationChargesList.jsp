<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Rate Plan Overrides - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="dashboardColors.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<script>
		 var toolbar = {
			Charges : {
				title : 'Edit Charges',
				imageSrc : 'icons/Edit.png',
				href : '/pages/masters/ratePlan.do?_method=getOverideChargesScreen&fromItemMaster=false',
				onclick : null,
				description : 'View and/or Edit Consultation Chagre Details'
			}
		}
	</script>


</head>
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<body onload="createToolbar(toolbar);">
	<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>

	<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
		<input type="hidden" name="_method" value="getChargesListScreen" />
		<input type="hidden" name="_searchMethod" value="getChargesListScreen" />
		<input type="hidden" name="chargeCategory" value="consultation"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
		<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<insta:search form="ConsultationChargesForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Consultation Type</div>
					<div class="sboFieldInput">
						<insta:selectdb id="consultation_type_id" name="consultation_type_id" value="${param.consultation_type_id}"
							table="consultation_types" class="dropdown"   dummyvalue="-- Select --"
							valuecol="consultation_type_id"  displaycol="consultation_type"  filtered="false" />
					</div>
				</div>
				<input type="hidden" name="consultation_type_id@type" value="integer" />

			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td >
							<div class="sfLabel">Status</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
									selValues="${paramValues.status}"/>
									<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Type</div>
							<div class="sfField">
								<insta:checkgroup name="patient_type" opvalues="i,o,ot" optexts="In Patient,Out Patient,OT"
									selValues="${paramValues.patient_type}"/>
									<input type="hidden" name="patient_type@op" value="in" />
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

		<div class="resultList">
		<table class="resultList"  id="resultTable" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');">
			<tr>
				<th>Consultation Type</th>
				<th>Code</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{consultation_type_id: '${record.consultation_type_id}',org_id: '${ifn:cleanJavaScript(org_id)}',chargeCategory:'consultation'},'');"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A' && record.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${record.status eq 'A' && record.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${record.consultation_type}
					</td>
					<td>${record.consultation_code}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" >${ifn:afmt(charges[record.consultation_type_id][bed].map['charge'])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
		</div>
		<insta:noresults hasResults="${hasResults}"/>

		<table class="screenActions" width="100%">
			<tr>
				<td><a href="${cpath}/pages/masters/ratePlan.do?_method=showRatePlanDetails&org_id=${ifn:cleanURL(org_id)}">Edit Rate Plan</a></td>
				<td  align="right">
					<img src='${cpath}/images/purple_flag.gif'>
						Excluded&nbsp;
					<img src='${cpath}/images/grey_flag.gif'>
						Inactive
			  	</td>
			</tr>
		</table>

	</form>

	<script>
		var cpath = '${cpath}';
	</script>

</body>

</html>
