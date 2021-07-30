<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Ophthalmology Dashboard</title>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<script>
	var contextPath = '<%=request.getContextPath()%>';
	var toolbar = {
		Edit: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: '/opthalmology/OpthalmologyTestsList.do?_method=show',
			onclick: null,
			description: "View and/or Edit Optho Details"
		}
	};

	function init() {
		Insta.initMRNoAcSearch(contextPath, "mr_no", "mrnoAcDropdown", "active","","");
	}
</script>
</head>

<c:set var="patientsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty patientsList}"/>
<body onload="createToolbar(toolbar);init();">
	<form name="OpthoPendingListForm">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list" />

	<h1>Ophthalmology Dashboard</h1>

	<insta:search form="OpthoPendingListForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Mr No:</div>
				<div class="sboFieldInput">
					<input type="text" id="mr_no" name="mr_no"  />
					<div id="mrnoAcDropdown" style="width: 34em;"></div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Test Status:</div>
						<div class="sfField">
							<select name="status" id="status" class="dropdown">

								<option value="">Optometrist Test Pending</option>
								<option value="D">Doctor Eye Exam Pending</option>
								<option value="S">Counselor Session Pending</option>
								<option value="C">All Tests Completed</option>
							</select>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Consulting Doctor:</div>
						<div class="sfField">
							<select name="doctor_id" id="doctor_id" multiple="true" class="listbox">
								<c:forEach var="doctor" items="${doctorList}">
									    <option value=${doctor.DOCTOR_ID }>${doctor.DOCTOR_NAME}</option>
								</c:forEach>

						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<table class="datatable" width="100%" id="resultTable">
		<tr>
			<td>Mrno</td>
			<td>Patient Name</td>
			<td>Consulting Doctor</td>
			<td>Prescribed Date</td>
			<td>Status</td>
		</tr>

		<c:forEach var="list" items="${patientsList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
					{patient_id: '${list.patient_id}', mr_no: '${list.mr_no }', doctor_id: '${list.doctor_id }',status: '${list.status}',complaint: '${list.complaint }'},'');"
				onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

				<td>${list.mr_no}</td>
				<td>${list.patient_name}</td>
				<td>${list.doctor_name}</td>
				<td><fmt:formatDate value="${list.presc_date}" pattern="dd-MM-yyyy HH:mm"/></td>
				<td>${list.status_name}</td>

			</tr>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${hasResults}"/>

	</form>
</body>
</html>
