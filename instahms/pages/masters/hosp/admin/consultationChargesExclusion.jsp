<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>${ifn:cleanHtml(screen)} Exclusion - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="masters/consultationCharges.js" />
	<insta:link type="script" file="dashboardColors.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<script>
		function validateAll(){
			document.consultationForm.submit();
		}
		function setApplicable(index) {
			var checkbox = document.getElementById("selectConsultation"+index);
			var applicable = document.getElementById("applicable"+index);
			if(checkbox.checked)
				applicable.value = 'false';
			else
				applicable.value = 'true';
		}
	</script>
</head>
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<body>
	<h1>${ifn:cleanHtml(screen)} Exclusion - ${ifn:cleanHtml(org_name)}</h1>
	<insta:feedback-panel />
<form action="${cpath}/pages/masters/ratePlan.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="getExcludeChargesScreen" />
	<input type="hidden" name="_searchMethod" value="getExcludeChargesScreen" />
	<input type="hidden" name="chargeCategory" value="consultation"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Consultation Type</div>
				<div class="sboFieldInput">
					<insta:selectdb id="consultation_type_id" name="consultation_type_id" value="${param.consultation_type_id}"
							table="consultation_types" class="dropdown"   dummyvalue="-- Select --"
							valuecol="consultation_type_id"  displaycol="consultation_type"  filtered="false" />
							<input type="hidden" name="consultation_type_id@type" value="integer" />
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Type</div>
							<div class="sfField">
									<insta:checkgroup name="patient_type" opvalues="i,o,ot" optexts="In Patient,Out Patient,OT"
											selValues="${paramValues.patient_type}"/>
											<input type="hidden" name="patient_type@op" value="in" />
							</div>
						</td>
						<td>
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
<form name="consultationForm" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="excludeCharges">
	<input type="hidden" name="chargeCategory" value="consultation"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

		<div class="resultList">
		<table class="resultList"  id="resultTable" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');">
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px;">
				<input type="checkbox" name="allPageConsultations" onclick="selectAllItems('selectConsultation',this)"/></th>
				<th>Consultation Type</th>
				<th>Code</th>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>
						<c:set var="selected" value="${record.applicable=='false'?'checked':''}"/>
						<input type="checkbox" id="selectConsultation${st.index}" name="selectConsultation"
							value="${record.consultation_type_id}" ${selected} onclick="setApplicable(${st.index});">
						<input type="hidden" name="applicable" id="applicable${st.index}" value="${record.applicable}"/>
						<input type="hidden" name="category_id" id="category_id${st.index}" value="${record.consultation_type_id}"/>

					</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A' && record.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${record.status eq 'A' && record.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${record.consultation_type}
					</td>
					<td>${record.consultation_code}</td>
				</tr>
			</c:forEach>
		</table>
		</div>

		<div class="screenActions" align="left">
			 <input type="button" name="exclude" id="exclude" value="Exclude" onclick="validateAll();"/>|
			<a href="${cpath}${screenURL}${ifn:cleanURL(org_id)}">Edit ${ifn:cleanHtml(screen)}</a>

		   	<div class="legend" style="display: ${hasResults ? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
			<div class="flagText">Excluded</div>
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
			</div>
		</div>
	</form>
	<insta:noresults hasResults="${hasResults}"/>
	<script>
		var cpath = '${cpath}';
	</script>

</body>

</html>
