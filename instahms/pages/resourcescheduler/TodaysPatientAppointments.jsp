<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<%@page import="com.bob.hms.common.Preferences"%>
<%@page import="java.util.Map"%>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<c:choose>
	<c:when test="${actionId eq 'search_resource_scheduler'}">
		<title><insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointmentlist"/></title>
	</c:when>
	<c:otherwise>
		<title><insta:ltext key="patient.resourcescheduler.todayspatientappointments.todaysappointments"/></title>
	</c:otherwise>
</c:choose>

<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
<insta:js-bundle prefix="scheduler.todayspatientappointments"/>

<script>
	var toolbarOptions = getToolbarBundle("js.scheduler.todayspatientappointments.toolbar");
	var regPref = ${regPrefJSON};
	var healthAuthoPref = ${healthAuthoPrefJSON};
	var visitTypeDependence = regPref.visit_type_dependence;
	var cpath = '${pageContext.request.contextPath}';
	var filterValue = '${not empty filterValue ? filterValue : ""}';
	var docJson= ${doctorsJson};
	var scheduleResourceListJSON = ${scheduleResourceListJSON};
	var testsJson=${testsJson};
	var servicesJson=${servicesJson};
	var surgeriesJson=${surgeriesJson};
	var theatresJSON = ${not empty TheatresJSON ? TheatresJSON : null};
	var equipmentsJSON = ${not empty EquipmentsJSON ? EquipmentsJSON : null};
	var bedsJSON = ${not empty BedsJSON ? BedsJSON : null};
	var labTechniciansJSON = ${not empty LabTechniciansJSON ? LabTechniciansJSON : null};
	var mappedTheatresJson = ${not empty mappedTheatresJson ? mappedTheatresJson : null};
	var serviceResourcesListJson = ${not empty serviceResourcesListJson ? serviceResourcesListJson : null};
	var mappedServiceResourcesJson = ${not empty mappedServiceResourcesJson ? mappedServiceResourcesJson : null};
	var mappedEquipmentResourcesJson = ${not empty mappedEquipmentResourcesJson ? mappedEquipmentResourcesJson : null};	
	var genericResourceListJson = <%= request.getAttribute("genericResourceListJson") %>;
	var resourceTypeJSON = ${ not empty resourceTypeListJSON ? resourceTypeListJSON : null};
	var doctorJSON = ${not empty DoctorsJSON ? DoctorsJSON : null};
	var surgeonsJSON = filterList(doctorJSON,"OT_DOCTOR_FLAG","Y");
	var anesthestistsJSON = filterList(doctorJSON,"DEPT_ID","DEP0002");
	var scheduleDoctorJSON = filterList(doctorJSON,"SCHEDULE","T");
	var allowMultipleActiveVisits = '${ifn:cleanJavaScript(allowMultipleActiveVisits)}';
	var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
	var loggedInCenter = <%= request.getAttribute("loggedInCenter") %>;
	var max_centers_inc_default = ${max_centers_inc_default};
	var schedulerGenerateOrder = '<%=GenericPreferencesDAO.getAllPrefs().get("scheduler_generate_order")%>';
	var modAdvancedOT = '${preferences.modulesActivatedMap['mod_advanced_ot']}';
	var modInsExt = '${preferences.modulesActivatedMap['mod_ins_ext']}';
	var modDentalModule = '${preferences.modulesActivatedMap['mod_dental_chart']}';
	var screenId='${screenId}';
	
</script>

<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="instaautocomplete.js" />
<insta:link type="js" file="/resourcescheduler/viewtoday.js"/>
<insta:link type="js" file="doctorConsultations.js" />
<insta:link type="script" file="resourcescheduler/schedulerCommon.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>


</head>
<body onload="initScheduler();document.appointmentSearchForm.method.value='getTodaysPatientAppointments';">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="appointmentStatus">
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.booked"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.confirmed"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.arrived"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.cancelled"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.noshow"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.completed"/>,
</c:set>
<c:set var="visitMode">
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.in.person"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.online"/>,
</c:set>
<c:set var="selectOptions">
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.select"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.doctor"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.surgery"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.services"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.tests"/>,
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
<c:set var="cons_token">
	<insta:ltext key="patient.resourcescheduler.doctorappointments.consultationtoken"/>
</c:set>

<c:set var="screenHeader" value=""/>
<c:choose>
	<c:when test="${actionId == 'search_resource_scheduler'}">
		<c:set var="screenHeader">
			<insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointmentlist"/>
		</c:set>
	</c:when>
	<c:otherwise>
		<c:set var="screenHeader">
			<insta:ltext key="patient.resourcescheduler.todayspatientappointments.todaysappointments"/>
		</c:set>
	</c:otherwise>
</c:choose>
	<h1>${screenHeader}</h1>
	<insta:feedback-panel/>
	<c:set var="AppointmentsList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty AppointmentsList}"/>
	<c:set var="cpath" value = "${pageContext.request.contextPath}"/>
<form action="" name="appointmentSearchForm" method="GET">
		<input type="hidden" name="_method" value="getTodaysPatientAppointments">
		<input type="hidden" name="_searchMethod" value="getTodaysPatientAppointments">
		<input type="hidden" name="doctor" id="doctor" value="${ifn:cleanHtmlAttribute(param.doctor)}">
		<input type="hidden" name="service" id="service" value="${ifn:cleanHtmlAttribute(param.service)}">
		<input type="hidden" name="test" id="test" value="${ifn:cleanHtmlAttribute(param.test)}">
		<input type="hidden" name="surgery" id="surgery" value="${ifn:cleanHtmlAttribute(param.surgery)}">
		<c:set var="cancelAppointment"/>
		<c:choose>
			<c:when test="${roleId == 1 || roleId == 2}">
				<c:set var="cancelAppointment" value="A"/>
			</c:when>
			<c:otherwise>
				<c:set var="cancelAppointment" value="${actionRightsMap.cancel_scheduler_appointment}"/>
			</c:otherwise>
		</c:choose>
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
							<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointmenttypes"/></div>
							<div class="sfField"><insta:selectoptions name="resFilter" id="resFilter"
								optexts="${selectOptions}" opvalues="ALL,DOC,OPE,SNP,DIA"
									value="${param.resFilter}" onchange="enableNames();"/></div>
							<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointment.source"/></div>
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
						<td>
							<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.doctorname"/></div>
							<div class="sfField">
								<div id="docContainer">
									<input type="text" name="doctor_name" id="doctor_name" value="${ifn:cleanHtmlAttribute(param.doctor_name)}"
									${doctor!= null ? '' : 'disabled' }>
									<div id="docDropdown">
								</div>
							</div>
							<div class="sfLabel" style="padding-top:30px"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.servicename"/></div>
							<div class="sfField" style="height:20px">
								<div id="servContainer">
									<input type="text" name="service_name" id="service_name" value="${ifn:cleanHtmlAttribute(param.service_name)}"
										${service != null ? '' : 'disabled'}>
									<div id="servDropdown" style="width: 80em;">
								</div>
							</div>
							<div>
							<div class="sfLabel" style="padding-top:30px"><insta:ltext key="ui.label.visit.mode"/></div>
							<div class="sfField">
								<insta:checkgroup name="visit_mode"
									opvalues="I,O"
									optexts="${visitMode}"
									selValues="${paramValues.visit_mode}"/>
							</div>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.testname"/></div>
							<div class="sfField">
								<div id="testContainer">
									<input type="text" name="test_name" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}"
										${test != null ? '' : 'disabled'}>
									<div id="testDropdown">
								</div>
							</div>
							<div class="sfLabel" style="padding-top:30px"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.surgeryname"/></div>
							<div class="sfField" style="height:20px">
								<div id="surgContainer">
									<input type="text" name="surgery_name" id="surgery_name" value="${ifn:cleanHtmlAttribute(param.surgery_name)}"
										${surgery != null ? '' : 'disabled' }>
									<div id="surgDropdown">
								</div>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.patientname"/></div>
							<div class="sfField">
								<input type="text" name="patient_name" id="patient_name" value="${ifn:cleanHtmlAttribute(param.patient_name)}">
								<input type="hidden" name="patient_name@op" value="ico">
							</div>

							<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.mobileno"/>:</div>
							<div class="sfField" style="height:20px">
									<input type="text" name="patient_contact" id="patient_contact" value="${ifn:cleanHtmlAttribute(param.patient_contact)}">
							</div>
						</td>
						<td class="last">
								<div class="sfLabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointmentstatus"/></div>
								<div class="sfField">
									<insta:checkgroup name="appoint_status"
										opvalues="Booked,Confirmed,Arrived,Cancel,Noshow,Completed,CU,CP"
										optexts="${appointmentStatus}"
										selValues="${paramValues.appoint_status}"/>
								</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable">
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px">
					<input type="checkbox" name="_checkAllForCancel" onclick="return checkOrUncheckAll('_cancelAppointment', this)"/>
				</th>
				<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.date"/></th>
				<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.time"/></th>
				<th><insta:ltext key="ui.label.waitlist.number"/></th>
				<insta:sortablecolumn name="appt_token" title="${token}"/>
				<insta:sortablecolumn name="consultation_token" title="${cons_token}"/>
				<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.arrivaltime"/></th>
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
				<th><insta:ltext key="remarks"/></th>
				<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.prescdoctor"/></th>
				<th><insta:ltext key="patient.resourcescheduler.todayspatientappointments.visitclosingtime"/></th>
				<th><insta:ltext key="ui.label.appointment.consultation.type"/></th>
				<th><insta:ltext key="ui.label.ordered.consultation.type"/></th>
				<th><insta:ltext key="ui.label.visit.mode"/></th>
			</tr>
			<c:forEach var="appointment" items="${AppointmentsList}" varStatus="status">
				<c:set var="index" value="${status.index}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${appointment.appoint_status == 'Booked'}">dark_blue</c:when>
						<c:when test="${appointment.appoint_status == 'Confirmed'}">blue</c:when>
						<c:when test="${appointment.appoint_status == 'Arrived'}">black</c:when>
						<c:when test="${appointment.appoint_status == 'Noshow'}">grey</c:when>
						<c:when test="${appointment.appoint_status == 'Cancel'}">red</c:when>
						<c:when test="${appointment.appoint_status == 'Completed'}">green</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="apptCenterId" value="0"/>
				<c:set var="apptCenterName" value=""/>
				<c:if test="${max_centers_inc_default > 1}">
					<c:set var="apptCenterId" value="${appointment.center_id}"/>
					<c:set var="apptCenterName" value="${appointment.center_name}"/>
				</c:if>
				<c:set var="nomrno" value="${empty appointment.mr_no}"/>
				<c:set var="booked" value="${appointment.appoint_status=='Booked'}"/>
				<c:set var="apptId" value="${appointment.appointment_id}" />
				<c:set var="isArrived" value="${appointment.appoint_status == 'Arrived'}"/>
				<c:set var="newApp" value="${(appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed') && empty appointment.visit_id }"/>
				<c:set var="enableArrived" value="${max_centers_inc_default > 1 && centerId == 0 ? false : newApp}"/>
				<c:set var="resourceandNoShow" value="${(appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed')}"/>
				<c:set var="cancel" value="${(appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed' || appointment.appoint_status == 'Arrived') && (cancelAppointment eq 'A')}"/>
				<c:set var="disabled" value=""/>
				<c:if test="${appointment.appoint_status=='Cancel' || appointment.appoint_status=='Noshow' ||appointment.appoint_status == 'Completed'}">
					<c:set var="disabled" value="disabled"/>
				</c:if>
				<c:if test="${not empty appointment.package_id}">
					<c:set var="disabled" value="disabled"/>
				</c:if>
				<c:set var="apptStatus" value="${appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed' || appointment.appoint_status == 'Arrived'}" />
				<c:set var="enableDental" value="false"/>
				<c:set var="enableDentalConsulation" value = "${preferences.modulesActivatedMap['mod_dental_chart']}" />
				<c:if test="${enableDentalConsulation == 'Y' && not empty appointment.department_type &&  appointment.department_type == 'DENT' }">
					<c:set var="enableDental" value ="${not empty appointment.mr_no && urlRightsMap['dental_consultations'] == 'A' && apptStatus}" />
				</c:if>
				<tr class="${index == 0 ? 'firstRow' : ''} ${index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${index}, event, 'searchformTable',
					{appointment_id:'${appointment.appointment_id}',
							 appointment_status:'${appointment.appoint_status}',
							 visit_mode:'${appointment.visit_mode}',
							 appointment_mrno:'${appointment.mr_no}',
							 mr_no:'${appointment.mr_no}',
							 appointment_patientname:'${fn:replace(appointment.patient_name,'\'','')}',
							 appointment_patientcontact:'${appointment.patient_contact}',
							 category:'${appointment.res_sch_category}', startTime:'${appointment.appoint_date}',
							 appointment_res_sch_id:'${appointment.resource}',
							 appointment_res_sch_name: '',
							 appointment_booked_resource: '',
							 appointment_package_id:'${appointment.package_id}',
							 appointment_booked_resource_id: '${appointment.booked_resource_id}',
							 appointment_consultation: '${appointment.consultation_type}',
							 appointment_duration: '${appointment.duration}',
							 time : '<fmt:formatDate value="${appointment.appoint_time}" pattern="HH:mm"/>', complaint : '${appointment.complaint}',
							 presc_doctor: '${appointment.presc_doctor}',
							 bedname : '${appointment.bed_name}', wardname : '${appointment.ward_name}',appt_center_id :'${apptCenterId}',
							 appt_center_name : '${apptCenterName}',
							 },
							 [${enableArrived},${resourceandNoShow},${newApp && nomrno},${enableDental}, true, true],'');"
					 onmouseover="hideToolBar(${index})" id="toolbarRow${index}">
						<td><input type="checkbox" name="_cancelAppointment" value="${appointment.appointment_id}" ${disabled}/></td>
						<td><fmt:formatDate value="${appointment.appoint_date}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${appointment.appoint_time}" pattern="HH:mm"/></td>
						<td>${appointment.waitlist == 0 ? 'NA' : appointment.waitlist}</td>
						<td>${appointment.appt_token}</td>
						<td>${appointment.consultation_token}</td>
						<td><fmt:formatDate value="${appointment.arrival_time}" pattern="dd-MM-yyyy HH:mm"/></td>
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
						<c:if test="${appointment.res_sch_category == 'OPE'}">
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.surgery"/></td>
						</c:if>
						<c:if test="${appointment.res_sch_category == 'SNP'}">
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.service.procedure"/></td>
						</c:if>
						<c:if test="${appointment.res_sch_category == 'DIA'}">
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.test"/></td>
						</c:if>
						<c:if test="${appointment.res_sch_category == 'BED'}">
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.bed"/></td>
						</c:if>
						<td><insta:truncLabel value="${appointment.booked_resource}" length="30"/></td>
						<td><insta:truncLabel value="${appointment.res_sch_name}" length="30"/></td>
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
					<td>${appointment.remarks}</td>
					<td>${appointment.presc_doctor}</td>
					<td>${appointment.visit_closing_date}</td>
					<td>${appointment.appointment_consultation}</td>
					<td>${appointment.ordered_consultation}</td>
					<c:choose>
  						<c:when test="${appointment.visit_mode == 'O'}">
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.online"/></td>
						</c:when>
						<c:otherwise>
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.in.person"/></td>
						</c:otherwise>
					</c:choose>
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
						<button type="button" name="ConfirmedAppointment" accesskey="O" onclick="changeStatusOfAllSelectedAppointments('confirmed');"
						  title='<insta:ltext key="patient.resourcescheduler.todayspatientappointments.confirm.appointment"/>'><insta:ltext key="patient.resourcescheduler.todayspatientappointments.c"/><b><u><insta:ltext key="patient.resourcescheduler.todayspatientappointments.o"/></u></b><insta:ltext key="patient.resourcescheduler.todayspatientappointments.nfirmed"/></button> |
					</td>
					<td colspan="2">
						<button type="button" name="noshowAppointment" accesskey="N" onclick="changeStatusOfAllSelectedAppointments('noshow');"
						  title='<insta:ltext key="patient.resourcescheduler.todayspatientappointments.appointment.noshow"/>'><b><u><insta:ltext key="patient.resourcescheduler.todayspatientappointments.n"/></u></b><insta:ltext key="patient.resourcescheduler.todayspatientappointments.oshow"/></button>
					</td>
					<c:if test="${cancelAppointment == 'A'}">
						<td>
							| <button type="button" name="cancelAppointment" accesskey="C" onclick="changeStatusOfAllSelectedAppointments('cancel');"
							  title='<insta:ltext key="patient.resourcescheduler.todayspatientappointments.cancel.appointment"/>'><b><u><insta:ltext key="patient.resourcescheduler.todayspatientappointments.c"/></u></b><insta:ltext key="patient.resourcescheduler.todayspatientappointments.ancel"/></button>
						</td>
						<td>&nbsp;</td>
						  <td id="cancelText" style="display:none"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.cancelreason"/></td>
						  <td id="cancelInput" style="display:none"><input type="text" name="_cancel_reason" id="_cancel_reason" value="" style="width:300px" class="required" title='<insta:ltext key="patient.resourcescheduler.todayspatientappointments.cancel.reason.required"/>'></td>
					</c:if>
				</tr>
			</table>
		</div>
	</c:if>
	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.booked"/></div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.confirmed"/></div>
		<div class="flag"><img src='${cpath}/images/black_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.arrived"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.noshow"/></div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.cancel"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.completed"/></div>
		<div class="flag"><img src='${cpath}/images/brown_flag.gif'></div>
	</div>
</form>

<form name="resourcesForm" action="todaysappointments.do" method="POST">
<input type="hidden" name="appointment_id"/>
<input type="hidden" name="appointment_status"/>
<input type="hidden" name="visit_mode"/>
<input type="hidden" name="_category"/>
<input type="hidden" name="name"/>
<input type="hidden" name="contact"/>
<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}"/>
<input type="hidden" name="resFilter" value="${ifn:cleanHtmlAttribute(param.resFilter)}"/>
<input type="hidden" name="startTime" value="${ifn:cleanHtmlAttribute(param.startTime) != null ? param.startTime : appointment.appoint_date}"/>
<input type="hidden" name="appoint_date" id="apptDate0" value="${paramValues.appoint_date[0]}">
<input type="hidden" name="appoint_date" id="apptDate1" value="${paramValues.appoint_date[1]}">
<input type="hidden" name="doctor" id="doctor" value="${ifn:cleanHtmlAttribute(param.doctor)}">
<input type="hidden" name="service" id="service" value="${ifn:cleanHtmlAttribute(param.service)}">
<input type="hidden" name="test" id="test" value="${ifn:cleanHtmlAttribute(param.test)}">
<input type="hidden" name="surgery" id="surgery" value="${ifn:cleanHtmlAttribute(param.surgery)}">
<input type="hidden" name="_registrationType" value=""/>

<input type="hidden" name="_searchMethod" value="getTodaysPatientAppointments"/>



<input type="hidden" id="resourceDialogId" value=""/>
<div id="resourceDialog" style="visibility:hidden;">
	<div class="bd">
		<jsp:include page="SchedulerResourceDialog.jsp"/>
	</div>
</div>
<table id="InnerResourceTable"></table>
</form>
<form name="schedulerMrnoForm" action="todaysappointments.do" method="POST">
<input type="hidden" id="mrnoDialogId" value=""/>
<input type="hidden" name="_method" value=""/>
<input type="hidden" name="resFilter" value="${ifn:cleanHtmlAttribute(param.resFilter)}"/>
<input type="hidden" name="startTime" value="${ifn:cleanHtmlAttribute(param.startTime) != null ? param.startTime : param.appoint_date}"/>
<input type="hidden" name="_registrationType" value=""/>
<input type="hidden" name="appointmentId"/>
<input type="hidden" name="appointment_status"/>
<input type="hidden" name="appoint_date" id="apptDate0" value="${paramValues.appoint_date[0]}">
<input type="hidden" name="appoint_date" id="apptDate1" value="${paramValues.appoint_date[1]}">
<input type="hidden" name="_category"/>
<input type="hidden" name="_name"/>
<input type="hidden" name="_contact"/>
<input type="hidden" name="_referer" value="${ifn:cleanHtmlAttribute(referer)}">
<input type="hidden" name="arrived"/>

<div id="mrnoDialog" style="visibility:hidden">
	<div id="dialog_h" class="hd" style="cursor: move;">
		<label> <insta:ltext key="patient.resourcescheduler.todayspatientappointments.update.mrno"/> </label>
	</div>
	<div class="bd">
		<fieldset class="fieldSetBorder" style="width:550px">
			<legend class="fieldSetLabel"><label id="mrnoVisitDialoglbl"></label></legend>
		<table cellpadding="0" cellspacing="0" class="schedulertable">
		<tr>
			<td class="formlabel"><insta:ltext key="ui.label.mrno"/></td>
			<td class="forminfo"><label id="v_mrno"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.patientname"/></td>
			<td class="forminfo"><label id="v_name"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.mobileno"/></td>
			<td class="forminfo"><label id="v_contact"></label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.ward"/></td>
			<td class="forminfo"><label id="v_wardname"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.bed"/></td>
			<td class="forminfo"><label id="v_bedname"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.complaint"/></td>
			<td class="forminfo"><label id="v_complaint"></label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.time"/></td>
			<td class="forminfo"><label id="v_time"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.duration"/></td>
			<td class="forminfo"><label id="v_duration"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.prescdoctor"/></td>
			<td class="forminfo"><label id="v_prescDoc"></label></td>
		</tr>
		<tr>
			<td class="formlabel"><label id="v_primaryResourcelbl"></label>:</td>
			<td class="forminfo"><label id="v_primaryResource"></label></td>
			<td class="formlabel"><label id="v_secondResourcelbl"></label>:</td>
			<td class="forminfo"><label id="v_secondResource"></label></td>
		</tr>
		</table>
		</fieldset>
		<table cellpadding="0" cellspacing="0" class="schedulertable">
		<tr id="v_chooseMrNoRow">
			<td class="formlabel" style="width:100px;text-align:left">
				<input type="radio" name="_mrnoexists" value="Y" onclick="enableSearchMrnoField(this)">
				<input type="hidden" id="searchPatientFirstName" name="_searchPatientFirstName"/>
				<input type="hidden" id="searchPatientLastName" name="_searchPatientLastName"/>
				<input type="hidden" id="searchPatientPhone" name="_searchPatientPhone"/>
				<insta:ltext key="patient.resourcescheduler.todayspatientappointments.comment"/>
			</td>
			<td class="forminfo" valign="top">
				<table width="100%">
				  <tr>
				  	<td style="padding: 0px 0px 15px 0px">
						<div id="mrnoAutoComplete">
							<input type="text" id="_searchmrno" name="_searchmrno" size="20" readonly/>
							<div id="mrnoAcDropdown" style="width:400px;"></div>
						</div>
					</td>
					<td style="padding: 0px 0px 0px 0px">
						<span id="activePatient">
							<input type="checkbox" name="active_patient"
							id="active_patient" onclick="onActiveCheck();" checked/>
							&nbsp;<insta:ltext key="patient.resourcescheduler.todayspatientappointments.active"/>&nbsp;<insta:ltext key="patient.resourcescheduler.todayspatientappointments.only"/>
						</span>
					</td>
				  </tr>
				 </table>
			</td>
		</tr>
		</table>
		<fieldset class="fieldSetBorder" style="width:550px;">
		<legend class="fieldSetLabel" id="status_visit_label"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.visitassociation"/></legend>
		<table width="100%" class="schedulertable">
			<tr id="v_VisitIdRow">
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.visitid"/></td>
				<td class="forminfo">
				<input type="hidden" name="visitType" id="visitType">
					<select name="patient_id" id="app_visit_id" class="dropdown" disabled onchange="onChangeVisitOfTodaysAppScreen();">
						<c:if test="${not empty allowMultipleActiveVisits && allowMultipleActiveVisits eq 'Y'}">
						<option value="None"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.none"/></option>
						</c:if>
					</select>
				</td>
				<td class="formlabel" id="emptyCell5">&nbsp;</td>
				<td class="forminfo" id="emptyCell6">&nbsp;</td>
				<td id="consultationTypeCell" style="display:none">
					<table>
						<tr>
				   			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.todayspatientappointments.consultationtype"/></td>
				   			<td class="forminfo">
								<select name="consultationTypes" id="consultationTypes" class="dropdown" style="width:135px">
									<option value="">${dummyvalue}</option>
									<c:forEach var="rowOP" items="${consultationTypeForOp}">
										<option value="${rowOP['consultation_type_id']}">
												   ${rowOP['consultation_type']}
										</option>
									</c:forEach>
								</select>
							</td>
						</tr>
					</table>
				</td>
				<td class="formlabel" id="emptyCell1">&nbsp;</td>
				<td class="forminfo" id="emptyCell2">&nbsp;</td>
				<td class="formlabel" id="emptyCell3">&nbsp;</td>
				<td class="forminfo" id="emptyCell4">&nbsp;</td>
			</tr>
			<c:if test="${mod_adv_ins}">
				<tr id="scheduler_prior_auth_row" style="display:none">
					<td class="formlabel" style="width:180px">
						<insta:ltext key="patient.resourcescheduler.todayspatientappointments.priorauthno"/>
					</td>
					<td class="forminput">
						<input type="text" name="scheduler_prior_auth_no" id="scheduler_prior_auth_no" maxlength="50">
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td class="formlabel" style="width:100px">
						<insta:ltext key="patient.resourcescheduler.todayspatientappointments.priorauthmode"/>
					</td>
					<td class="forminput">
						<insta:selectdb  name="scheduler_prior_auth_mode_id" id="scheduler_prior_auth_mode_id" value=""
							table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
							filtered="false" dummyvalue="${dummyvalue}"/>
				</tr>
			</c:if>
			<tr id="v_noMrnoRow">
				<td class="formlabel" style="width:100px;text-align:left">
					<input type="radio" name="_mrnoexists" value="N" onclick="enableSearchMrnoField(this)"/>
					<insta:ltext key="patient.resourcescheduler.todayspatientappointments.no"/>
				</td>
			</tr>
		</table>
		</fieldset>
		<div>&nbsp;</div>
		<div style="margin-left:5px;width:400px">
		<insta:accessbutton buttonkey="patient.resourcescheduler.todayspatientappointments.ok" name="ok" id="ok" type="button" onclick="onEditMrnoSubmit();" />
		<insta:accessbutton buttonkey="patient.resourcescheduler.todayspatientappointments.cancel1"  type="button" onclick="onEditMrnoCancel();" />
		</div>
	</div>
</div>
</form>
</form>
</body>
</html>
