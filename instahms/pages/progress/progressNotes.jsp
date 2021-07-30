<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Progress Notes</title>
	<style>
		.status_I {
			background-color: #dbe7f6;
		}
	</style>
	<jsp:useBean id="now" class="java.util.Date" />
	<script>
		function getPrint() {
			var filterType = 'all';
			var filtervisitId = '';
			var filterTypes = document.getElementsByName('filterType');
			for(var i=0; i<filterTypes.length; i++) {
				if (filterTypes[i].checked) {
					if (filterTypes[i].value == 'all') {
						filterType = 'all';
					} else if (filterTypes[i].value == 'patient') {
						filterType = 'patient'
					} else if (filterTypes[i].value == 'visit') {
						filterType = 'visit';
						if(document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex] == undefined
							|| document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex].value == '') {
							alert('Please select visit');
							document.getElementById('filtervisitId').focus();
							return false;
						}
					}
				}
			}
			if(document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex] != undefined)
				filtervisitId = document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex].value;
			window.open('${cpath}/patient/progress/print.do?method=getPrint&mr_no=${ifn:cleanJavaScript(param.mr_no)}&fromScreen=mainScreen&filterType='+filterType+'&filtervisitId='+filtervisitId);
		}

		function validateSubmit() {
			var doctorName = document.getElementById('doctor').value;
			if (document.getElementById('date_time_dt').value.trim() == '') {
				alert("Please enter date");
				return false;
			}
			if (!document.getElementById('date_time_tm').value.trim() == '') {
				if (!isTime(document.getElementById('date_time_tm').value.trim())) {
					alert('Please enter time in HH:mm format');
					return false;
				}
			} else {
				alert('Please enter time');
				return false;
			}

			if(doctorName == '') {
				alert('Please select doctor name');
				return false;
			}
			if (document.getElementById('prgNtsTOvisit').checked) {
				if (document.getElementById('visitId').options[document.getElementById('visitId').selectedIndex] == undefined
					|| document.getElementById('visitId').options[document.getElementById('visitId').selectedIndex].value == '') {
					alert('Please select visit');
					document.getElementById('visitId').focus();
					return false;
				}
			}
			if (document.getElementById('notes').value.trim() == '') {
				alert('Please enter notes');
				return false;
			}

			return true;
		}

		function submitFilter() {
			document.progressForm._method.value = 'show';
			var filterTypes = document.getElementsByName('filterType');
			for(var i=0; i<filterTypes.length; i++) {
				if (filterTypes[i].checked) {
					if (filterTypes[i].value == 'all') {

					} else if (filterTypes[i].value == 'patient') {

					} else if (filterTypes[i].value == 'visit') {

						if(document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex] == undefined
							|| document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex].value == '') {
							alert('Please select visit to search');
							document.getElementById('filtervisitId').focus();
							return false;
						}
					}
				}
			}
			return true;
		}
	</script>
</head>
	<body>
		<h1 style="float: left">Progress Notes</h1>

		<insta:patientsearch searchType="" searchUrl="PatientProgress.do" buttonLabel="Find"
			searchMethod="show" fieldName="mr_no" />

	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" showClinicalInfo="true"/>

	<c:set var="end" value="${fn:length(progressNtsList)}" />
	<c:if test="${not empty progressNtsList}">
		<c:forEach items="${progressNtsList}" var="progressNts">
			<c:if test="${progressNts.status eq 'E'}">
				<c:set var="firstRecord" value="${progressNts}" />
			</c:if>
		</c:forEach>
	</c:if>

	<form action="PatientProgress.do" method="POST" name="progressForm" autocomplete="off">
		<input type="hidden" name="_method" value="saveProgressNotes" />
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
		<input type="hidden" name="fromScreen" value="mainScreen"/>
		<input type="hidden" name="progress_notes_id" value="${firstRecord.progress_notes_id}" />

		<fieldset class="fieldsetborder">
		<table class="formtable">
			<c:set var="date"><fmt:formatDate value="${empty firstRecord ? now : firstRecord.date_time}" pattern="dd-MM-yyyy"/></c:set>
			<tr>
				<td class="formlabel">Date/Time:</td>

					<c:set var="time"><fmt:formatDate pattern="HH:mm" value="${empty firstRecord ? now : firstRecord.date_time}" /></c:set>
				<td>
					<insta:datewidget name="date_time_dt" id="date_time_dt" value="${date}" />
					<input type="text" size="4" name="date_time_tm" id="date_time_tm" value="${time}" class="timefield" />  |

					Patient<input type="radio" name="progressNotesTO" value="patient" ${empty visitsList ? 'checked' : ''}/>
					Visit<input type="radio" name="progressNotesTO" id="prgNtsTOvisit" value="visit" ${not empty visitsList ? 'checked' : '' }/>
					<select class="dropdown" name="visitId" id="visitId">
						<c:forEach var="visit" items="${visitsList}">
							<option value="${visit.map.patient_id}" class="${visit.map.status == 'I' ? 'status_I' : ''}" ${firstRecord.visit_id == visit.map.patient_id ? 'selected' : ''}>
									${visit.map.patient_id}(<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.map.reg_date}"/>)
							</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Doctor:</td>
				<td><insta:selectdb name="doctor" id="doctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name" value="${firstRecord.doctor}"
								dummyvalue="-----Select-----" dummyvalueId="" orderby="doctor_name"/></td>
			</tr>
			<c:if test="${not empty firstRecord.username}">
				<tr>
					<td class="formlabel">User Name:</td>
					<td><label>${firstRecord.username}</label></td>
				</tr>
			</c:if>
			<tr>
				<td class="formlabel">Notes:</td>
				<td><textarea name="notes" id="notes" onfocus="this.rows=18" cols="80" rows="7">${firstRecord.notes}</textarea></td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td>
					<insta:selectoptions name="status" opvalues="E,C" optexts="Edited,Completed" value="${firstRecord.status}" />
				</td>
			</tr>
		</table>
		<c:if test="${not empty param.mr_no}">
			<div class="screenActions">
				<input type="submit" value="Save" name="save" onclick="return validateSubmit();"/> |
				<input type="button" value="Print" name="print" onclick="getPrint()"/>
			</div>
		</c:if>
		</fieldset>
		<c:if test="${not empty param.mr_no}">
			<div class="screenActions" style="padding-left: 49em; padding-bottom: 12px;">
				<%--All<input type="radio" name="filterType" value="all" ${empty visitsList ? 'checked' : '' }/>--%>
				Patient<input type="radio" name="filterType" value="patient" ${filterType eq 'patient' ? 'checked' : ''}/>
				Visit<input type="radio" name="filterType" value="visit" ${filterType eq 'visit' ? 'checked' : ''}/>
				<select class="dropdown" name="filtervisitId" id="filtervisitId">
					<c:forEach var="visit" items="${visitsList}">
							<option value="${visit.map.patient_id}" ${filtervisitId eq visit.map.patient_id ? 'selected' : ''}>
							${visit.map.patient_id}(<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.map.reg_date}"/>)
						</option>
					</c:forEach>
				</select> |
				<input type="submit" name="filter" value="Filter" onclick="return submitFilter();"/>
			</div>
		</c:if>
		<c:if test="${not empty progressNtsList}">
		<fieldset class="fieldsetborder">
		<table class="formtable">
			<c:forEach items="${progressNtsList}" var="progressNts">
				<c:if test="${progressNts.status eq 'C'}">
					<tr>
						<td><label style="font-size: large; font-style: oblique">------ <font>Progress Notes On</font>
							<font><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${progressNts.date_time}" />
							<font>By</font> ${progressNts.doctor_name} (User: ${progressNts.username})</font> ------</label></td>
					</tr>
					<tr>
						<td><label >${ifn:breakContent(progressNts.notes)}</label></td>
					</tr>
				</c:if>
			</c:forEach>
		</table>
		</fieldset>
		</c:if>

	</form>

	</body>
</html>