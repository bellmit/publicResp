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
<title>Surgery / Procedure Bookings</title>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
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
	var serviceResourcesListJson = ${not empty serviceResourcesListJson ? serviceResourcesListJson : null};
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
<body onload="initOTBookings();document.appointmentSearchForm.method.value='getTodaysPatientAppointments';">
	<h1>Surgery / Procedure Bookings</h1>
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
		<insta:search form="appointmentSearchForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()">
			<div class="searchbasicOpts">
				<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
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
						<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changePatientStatus()"/>Active Only
					</div>
				</div>
				</div>
				<div >
					<div style="float: left;padding : 5px 5px 0px 5px;">From:</div>
					<div class="sboFieldInput" style="float: left">
						<insta:datewidget name="appoint_date" id="appoint_date0" value="${fromdate}"/>
						<input type="text" name="appoint_time" id="appoint_time0" class="timefield" value="${paramValues.appoint_time[0]}">
					</div>
					<div style="float: left;padding : 5px 5px 0px 5px;">To:</div>
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
							<div class="sfLabel">Surgery / Procedure Name</div>
							<div class="sfField">
								<div id="surgContainer">
									<input type="text" name="surgery_name" id="surgery_name" value="${ifn:cleanHtmlAttribute(param.surgery_name)}">
									<div id="surgDropdown" style="width:100px;">
								</div>
							</div>
						</td>
						<td>
							<div class="sfLabel">Patient Name:</div>
							<div class="sfField">
								<input type="text" name="patient_name" id="patient_name" value="${ifn:cleanHtmlAttribute(param.patient_name)}">
								<input type="hidden" name="patient_name@op" value="ico">
							</div>
						</td>
						<td class="last">
								<div class="sfLabel">Appointment Status:</div>
								<div class="sfField">
									<insta:checkgroup name="appoint_status"
										opvalues="Booked,Confirmed,Arrived,Cancel,Noshow,Completed"
										optexts="Booked,Confirmed,Arrived,Cancelled,Noshow,Completed"
										selValues="${paramValues.appoint_status}"/>
									<input type="hidden" name="resFilter" id="resFilter" value="OPE"/>
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
				<th>Date</th>
				<th>Time</th>
				<th>Mr No</th>
				<th>Visit Id</th>
				<th>Name</th>
				<th>Ward/Bed</th>
				<th>Mobile No</th>
				<th>Theatre/Room</th>
				<th>Surgery / Procedure Name</th>
				<th>Bill Status</th>
				<c:if test="${max_centers_inc_default > 1}">
					<th>Center Name</th>
				</c:if>
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
				<c:set var="isArrived" value="${appointment.appoint_status == 'Arrived'}"/>
				<c:set var="newApp" value="${(appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed') && empty appointment.visit_id }"/>
				<c:set var="enableArrived" value="${max_centers_inc_default > 1 && centerId == 0 ? false : newApp}"/>
				<c:set var="resourceandNoShow" value="${(appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed')}"/>
				<c:set var="cancel" value="${(appointment.appoint_status=='Booked' || appointment.appoint_status=='Confirmed' || appointment.appoint_status == 'Arrived') && (cancelAppointment eq 'A')}"/>
				<c:set var="disabled" value=""/>
				<c:if test="${appointment.appoint_status=='Cancel' || appointment.appoint_status=='Noshow' ||appointment.appoint_status == 'Completed'}">
					<c:set var="disabled" value="disabled"/>
				</c:if>
				<tr class="${index == 0 ? 'firstRow' : ''} ${index % 2 == 0 ? 'even' : 'odd'}">
						<td><input type="checkbox" name="_cancelAppointment" value="${appointment.appointment_id}" ${disabled}/></td>
						<td><fmt:formatDate value="${appointment.appoint_date}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${appointment.appoint_time}" pattern="HH:mm"/></td>
						<td>${appointment.mr_no}</td>
						<td>${appointment.visit_id}</td>
						<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
							<insta:truncLabel value="${appointment.patient_name}" length="30"/>
						</td>
						<td>
							<c:if test="${not empty appointment.bed_name }">
								${appointment.ward_name}/${appointment.bed_name}
							</c:if>
						</td>
						<td>${appointment.patient_contact}</td>

						<td><insta:truncLabel value="${appointment.booked_resource}" length="30"/></td>
						<td><insta:truncLabel value="${appointment.res_sch_name}" length="30"/></td>
						<c:choose>
						<c:when test="${appointment.credit_bill_exists == 'true'}">
						<c:choose>
							<c:when test="${appointment.bill_status_ok == 'true'}">
								<c:if test="${appointment.payment_ok == 'true'}">
									<td>Okay</td>
								</c:if>
								<c:if test="${appointment.payment_ok == 'false'}">
									<td align="left"><font color="#FFA07A">Payment Due</font></td>
								</c:if>
							</c:when>
							<c:otherwise>
								<c:if test="${appointment.payment_ok == 'true'}">
									<td>Okay</td>
								</c:if>
								<c:if test="${appointment.payment_ok == 'false'}">
									<td align="left"><font color="#FFA07A">Payment Due</font></td>
								</c:if>
							</c:otherwise>
						</c:choose>
						</c:when>
						<c:when test="${appointment.credit_bill_exists == 'false'}">
							<td>Okay</td>
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
						<button type="button" name="ConfirmedAppointment" accesskey="O" onclick="changeStatusOfAllSelectedAppointments('confirmed');"
						  title="Confirm an Appointment">C<b><u>O</u></b>nfirmed</button> |
					</td>
					<td colspan="2">
						<button type="button" name="noshowAppointment" accesskey="N" onclick="changeStatusOfAllSelectedAppointments('noshow');"
						  title="Appointment No Show"><b><u>N</u></b>o Show</button>
					</td>
					<c:if test="${cancelAppointment == 'A'}">
						<td>
							| <button type="button" name="cancelAppointment" accesskey="C" onclick="changeStatusOfAllSelectedAppointments('cancel');"
							  title="Cancel an Appointment"><b><u>C</u></b>ancel</button>
						</td>
						<td>&nbsp;</td>
						  <td id="cancelText" style="display:none">Cancel Reason:</td>
						  <td id="cancelInput" style="display:none"><input type="text" name="_cancel_reason" id="_cancel_reason" value="" style="width:300px" class="required" title="cancel reason is required"></td>
					</c:if>
				</tr>
			</table>
		</div>
	</c:if>
	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></div>
		<div class="flagText">Booked</div>
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText">Confirmed</div>
		<div class="flag"><img src='${cpath}/images/black_flag.gif'></div>
		<div class="flagText">Arrived</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">No Show</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Cancel</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Completed</div>
	</div>
</form>

<form name="resourcesForm" action="${cpath}/otservices/otManagement/otBookedList.do" method="POST">
<input type="hidden" name="appointment_id"/>
<input type="hidden" name="appointment_status"/>
<input type="hidden" name="_category"/>
<input type="hidden" name="name"/>
<input type="hidden" name="contact"/>
<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}"/>
<input type="hidden" name="resFilter" value="OPE"/>
<input type="hidden" name="startTime" value="${ifn:cleanHtmlAttribute(param.startTime != null ? param.startTime : appointment.appoint_date)}"/>
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
		<jsp:include page="/pages/resourcescheduler/SchedulerResourceDialog.jsp"/>
	</div>
</div>
<table id="InnerResourceTable"></table>
</form>
<form name="schedulerMrnoForm" action="${cpath}/otservices/otManagement/otBookedList.do" method="POST">
<input type="hidden" id="mrnoDialogId" value=""/>
<input type="hidden" name="_method" value=""/>
<input type="hidden" name="resFilter" value="OPE"/>
<input type="hidden" name="startTime" value="${ifn:cleanHtmlAttribute(param.startTime != null ? param.startTime : param.appoint_date)}"/>
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
		<label> Update MRNo/Visit Id </label>
	</div>
	<div class="bd">
		<fieldset class="fieldSetBorder" style="width:550px">
			<legend class="fieldSetLabel"><label id="mrnoVisitDialoglbl"></label></legend>
		<table cellpadding="0" cellspacing="0" class="schedulertable">
		<tr>
			<td class="formlabel">MR No:</td>
			<td class="forminfo"><label id="v_mrno"></label></td>
			<td class="formlabel">Patient Name:</td>
			<td class="forminfo"><label id="v_name"></label></td>
			<td class="formlabel">Mobile No:</td>
			<td class="forminfo"><label id="v_contact"></label></td>
		</tr>
		<tr>
			<td class="formlabel">Ward:</td>
			<td class="forminfo"><label id="v_wardname"></label></td>
			<td class="formlabel">Bed:</td>
			<td class="forminfo"><label id="v_bedname"></label></td>
			<td class="formlabel">Complaint:</td>
			<td class="forminfo"><label id="v_complaint"></label></td>
		</tr>
		<tr>
			<td class="formlabel">Time:</td>
			<td class="forminfo"><label id="v_time"></label></td>
			<td class="formlabel">Duration:</td>
			<td class="forminfo"><label id="v_duration"></label></td>
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
				If patient has MR No. Enter MR No/Name:
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
							&nbsp;Active&nbsp;Only
						</span>
					</td>
				  </tr>
				 </table>
			</td>
		</tr>
		</table>
		<fieldset class="fieldSetBorder" style="width:550px;">
		<legend class="fieldSetLabel" id="status_visit_label">Visit Association</legend>
		<table width="100%" class="schedulertable">
			<tr id="v_VisitIdRow">
				<td class="formlabel">Visit Id:</td>
				<td class="forminfo">
				<input type="hidden" name="visitType" id="visitType">
					<select name="patient_id" id="app_visit_id" class="dropdown" disabled onchange="onChangeVisitOfTodaysAppScreen();">
						<c:if test="${not empty allowMultipleActiveVisits && allowMultipleActiveVisits eq 'Y'}">
						<option value="None">None</option>
						</c:if>
					</select>
				</td>
				<td class="formlabel" id="emptyCell5">&nbsp;</td>
				<td class="forminfo" id="emptyCell6">&nbsp;</td>
				<td id="consultationTypeCell" style="display:none">
					<table>
						<tr>
				   			<td class="formlabel">Consultation Types:</td>
				   			<td class="forminfo">
								<select name="consultationTypes" id="consultationTypes" class="dropdown" style="width:135px">
									<option value="">--Select--</option>
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
						Prior Auth No:
					</td>
					<td class="forminput">
						<input type="text" name="scheduler_prior_auth_no" id="scheduler_prior_auth_no" maxlength="50">
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td class="formlabel" style="width:100px">
						Prior Auth Mode:
					</td>
					<td class="forminput">
						<insta:selectdb  name="scheduler_prior_auth_mode_id" id="scheduler_prior_auth_mode_id" value=""
							table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
							filtered="false" dummyvalue="-- Select --"/>
				</tr>
			</c:if>
			<tr id="v_noMrnoRow">
				<td class="formlabel" style="width:100px;text-align:left">
					<input type="radio" name="_mrnoexists" value="N" onclick="enableSearchMrnoField(this)"/>
					No, Register as a new patient.
				</td>
			</tr>
		</table>
		</fieldset>
		<div>&nbsp;</div>
		<div style="margin-left:5px;width:400px">
			<button type="button" name="ok" id="ok" onclick="onEditMrnoSubmit();" accessKey="K">O<b><u>K</u></b></button>
			<button type="button" onclick="onEditMrnoCancel();" accessKey="A">C<b><u>A</u></b>ncel</button>
		</div>
	</div>
</div>
</form>
</form>
</body>
</html>
