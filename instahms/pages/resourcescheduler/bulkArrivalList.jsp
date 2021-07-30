<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>

<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="i18nSupport" content="true"/>
		<title><insta:ltext key="patient.resourcescheduler.bulkarrival.bulkarrivallist"/></title>

		<script>
			var docJson= ${doctorsJSON};
		</script>
		<insta:js-bundle prefix="patient.resourcescheduler.bulkarrival"/>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="js" file="hmsvalidation.js"/>
		<insta:link type="js" file="ajax.js"/>
		<insta:link type="js" file="instaautocomplete.js" />
		<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="/resourcescheduler/bulkArrival.js"/>
		<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>


	</head>
	<body onload="init()">
		<c:set var="dummyvalue">
			<insta:ltext key="selectdb.dummy.value"/>
		</c:set>
		<c:set var="appointmentStatus">
		</c:set>
		<c:set var="confirmtitle">
			<insta:ltext key="patient.resourcescheduler.todayspatientappointments.confirm.appointment"/>
		</c:set>
		<c:set var="noshowtitle">
			<insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointment.noshow"/>
		</c:set>
		<c:set var="canceltitle">
			<insta:ltext key="patient.resourcescheduler.todayspatientappointments.cancel.appointment"/>
		</c:set>
		<c:set var="token">
			<insta:ltext key="patient.resourcescheduler.doctorappointments.token"/>
		</c:set>


		<h1><insta:ltext key="patient.resourcescheduler.bulkarrival.bulkarrivallist"/></h1>
		<insta:feedback-panel/>
		<c:set var="AppointmentsList" value="${pagedList.dtoList}"/>
		<c:set var="hasResults" value="${not empty AppointmentsList}"/>
		<c:set var="cpath" value = "${pageContext.request.contextPath}"/>

		<form action="" name="appointmentSearchForm" method="GET">
			<input type="hidden" name="_method" value="getPendingArrivalForChanneling">
			<input type="hidden" name="_searchMethod" value="getPendingArrivalForChanneling">
			<input type="hidden" name="doctor" id="doctor" value="${ifn:cleanHtmlAttribute(param.doctor)}">

			<insta:search form="appointmentSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()" clearFunction="clearSearchParameters">
				<div class="searchbasicOpts">
					<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.mrno.patientname"/></div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
					</div>
					<div class="sboField">
					<div class="sboFieldLabel">&nbsp;
						<div class="sboFieldInput">
							<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changePatientStatus()"/><insta:ltext key="patient.resourcescheduler.todayspatientappointments.activeonly"/>
						</div>
					</div>
					</div>
					<div>
						<div style="float: left;padding : 5px 5px 0px 5px;"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.from"/></div>
						<div class="sboFieldInput" style="float: left">
							<insta:datewidget name="appoint_date" id="appoint_date0" value="${fromdate}"/>
							<input type="text" name="appoint_time" id="appoint_time0" class="timefield" value="${paramValues.appoint_time[0]}">
						</div>
						<div style="float: left;padding : 5px 5px 0px 5px;"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.to"/></div>
						<div class="sboFieldInput" style="float: left">
							<insta:datewidget name="appoint_date" id="appoint_date1" value="${todate}"/>
							<input type="text" class="timefield" name="appoint_time" id="appoint_time1"value="${paramValues.appoint_time[1]}">
							<input type="hidden" name="appoint_time@op" value="ge,le">
							<input type="hidden" name="appoint_time@type" value="time">
						</div>
					</div>
				</div>
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}">
					<table class="searchformTable">
						<tr>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.doctorname"/></div>
								<div class="sfField">
									<div id="docContainer" >
										<input type="text" name="doctor_name" id="doctor_name" value="${ifn:cleanHtmlAttribute(param.doctor_name)}" style="width: 223px"/>
										<div id="docDropdown"></div>
									</div>
								</div>
							</td>
							<td>
								<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.patientname"/></div>
								<div class="sfField">
									<input type="text" name="patient_name" id="patient_name" value="${ifn:cleanHtmlAttribute(param.patient_name)}">
									<input type="hidden" name="patient_name@op" value="ico">
								</div>
							</td>
							<td class="last">
								<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.bulkarrival.appointment.source"/></div>
					            <div class="sfField">
					                <select name="appt_source" id="appt_source" multiple>
					                    <c:forEach var="source" items="${apptSources}">
							                <option value="${source.APPOINTMENT_SOURCE_ID}"
							                    <c:forEach var="selSource" items="${paramValues.appt_source }">
								                    <c:if test="${source.APPOINTMENT_SOURCE_ID == selSource}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.selected"/></c:if></c:forEach>>
			 				                    ${source.APPOINTMENT_SOURCE_NAME}
			 				                </option>
			                            </c:forEach>
				                    </select>
					            </div>
							</td>
						</tr>
					</table>
				</div>
			</insta:search>
			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		</form>
		<form action="bulkarrival.do" name="bulkArrivalForm" method="POST">
			<input type="hidden" name="appIds" value="" />
			<input type="hidden" name="_method" value="markBulkArrived" />
			<div class="resultList">
				<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable">
					<tr>
						<th style="padding-top: 0px;padding-bottom: 0px">
							<input type="checkbox" name="_checkAllForCancel" onclick="return checkOrUncheckAll('markAppointmentArrived', this)"/>
						</th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.date"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.time"/></th>
						<insta:sortablecolumn name="appt_token" title="${token}"/>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointment.source"/></th>
						<th><insta:ltext key="ui.label.mrno"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.visitid"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.name"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.ward.bed"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.mobileno"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointmenttype"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.primaryresource"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.otherresource"/></th>
						<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.billstatus"/></th>
						<c:if test="${max_centers_inc_default > 1}">
							<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.centername"/></th>
						</c:if>
					</tr>
					<c:forEach var="appointment" items="${AppointmentsList}" varStatus="status">
						<c:set var="index" value="${status.index}"/>
						<c:set var="disableArrived" value="${not empty appointment.is_ip_visit ? appointment.active_visit_count > 0 : (regPrefs.allow_multiple_active_visits eq 'Y' ? false : appointment.active_visit_count > 0)}" />
						<c:set var="flagColor">
		   			   <insta:ltext key="patient.resourcescheduler.todayspatientappointments.empty"/></c:otherwise>
						</c:set>

						<tr class="${index == 0 ? 'firstRow' : ''} ${index % 2 == 0 ? 'even' : 'odd'}"
							onclick=""
							 onmouseover="hideToolBar(${index})" id="toolbarRow${index}">
								<td><input type="checkbox" name="markAppointmentArrived" value="${appointment.appointment_id}" ${disableArrived ? 'disabled' : ''} /></td>
								<td><fmt:formatDate value="${appointment.appoint_date}" pattern="dd-MM-yyyy"/></td>
								<td><fmt:formatDate value="${appointment.appoint_time}" pattern="HH:mm"/></td>
								<td>${appointment.appt_token}</td>
								<td>${appointment.app_source_name}</td>
								<td>${appointment.mr_no}</td>
								<td>${appointment.visit_id}</td>
								<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
									<insta:truncLabel value="${appointment.patient_name}" length="30"/> (<insta:truncLabel value="${appointment.appointment_department}" length="20"/>)
								</td>
								<td>
									<c:if test="${not empty appointment.bed_name }">
										${appointment.ward_name}/${appointment.bed_name}
									</c:if>
								</td>
								<td>${appointment.patient_contact}</td>
								<c:if test="${appointment.res_sch_category == 'DOC'}" >
									<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.consultation"/></td>
								</c:if>
								<td><insta:truncLabel value="${appointment.booked_resource}" length="30"/></td>
								<td>&nbsp;</td>
								<c:choose>
								<c:when test="${appointment.credit_bill_exists == 'true'}">
								<c:choose>
									<c:when test="${appointment.bill_status_ok == 'true'}">
										<c:if test="${appointment.payment_ok == 'true'}">
											<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.okay"/></td>
										</c:if>
										<c:if test="${appointment.payment_ok == 'false'}">
											<td align="left"><font color="#FFA07A"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.paymentdue"/></font></td>
										</c:if>
									</c:when>
									<c:otherwise>
										<c:if test="${appointment.payment_ok == 'true'}">
											<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.okay"/></td>
										</c:if>
										<c:if test="${appointment.payment_ok == 'false'}">
											<td align="left"><font color="#FFA07A"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.paymentdue"/></font></td>
										</c:if>
									</c:otherwise>
								</c:choose>
								</c:when>
								<c:when test="${appointment.credit_bill_exists == 'false'}">
									<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.okay"/></td>
								</c:when>
								<c:otherwise>
									<td></td>
								</c:otherwise>
							</c:choose>
							<c:if test="${max_centers_inc_default > 1}">
								<td>${appointment.center_name}</td>
							</c:if>
						</tr>
					</c:forEach>
				</table>
				<insta:noresults hasResults="${hasResults}"/>
			</div>
			<c:if test="${not empty AppointmentsList}">
				<div class="screenActions">
					<table>
						<tr>
							<td colspan="2">
								<insta:accessbutton buttonkey="patient.resourcescheduler.bulkarrival.arrived"
									name="ConfirmedAppointment" type="button" title="${confirmtitle}" onclick="markArrivedForSelectedAppointments()"
									disabled="${(max_centers_inc_default > 1 && centerId == 0) ? true : ''}"/>
							</td>
						</tr>
					</table>
				</div>
			</c:if>
			<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
				<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
			</div>
		</form>
	</body>
</html>
