<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Consultation Types - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ConsultationTypes/ConsultationTypes.js"/>
</head>

<body onload="createToolbar(toolbar);">
	<h1>Consultation Types</h1>
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<form name="ConsultationTypesForm">
		<input type="hidden" name="_method" value="list">

		<insta:search form="ServiceGroupForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Consultation Types:</div>
					<div class="sboFieldInput">
						<insta:selectdb id="consultation_type_id" name="consultation_type_id" value=""
							table="consultation_types" class="dropdown"   dummyvalue="-- Select --"
							valuecol="consultation_type_id"  displaycol="consultation_type"  filtered="false" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I,ot" optexts="Active,Inactive,OT" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<table class="resultList"  id="resultTable" onmouseover="hideToolBar('');">
			<tr>
				<th>Consultation Type</th>
				<th>Consultation Code</th>
				<th>Patient Type</th>
				<th>Status</th>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{consultation_type_id: '${record.consultation_type_id}'});"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${record.consultation_type}</td>
					<td>${record.consultation_code}</td>
					<td>
						<c:if test="${record.patient_type eq 'i'}">InPatient</c:if>
						<c:if test="${record.patient_type eq 'o'}">OutPatient</c:if>
						<c:if test="${record.patient_type eq 'ot'}">OT</c:if>
					</td>
					<td>
						<c:if test="${record.status eq 'A'}">Active</c:if>
						<c:if test="${record.status eq 'I'}">InActive</c:if>
					</td>
				</tr>
			</c:forEach>
		</table>

		<c:url var="url" value="consultTypes.do">
			<c:param name="_method" value="add"/>
		</c:url>
		<table class="formtable">
			<tr>
				<td><a href="<c:out value='${url}' />">Add Consultation Type</a></td>
			</tr>
		</table>
	</form>
</body>

</html>
