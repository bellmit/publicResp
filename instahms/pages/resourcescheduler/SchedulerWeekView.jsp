<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.bob.hms.common.Preferences"%>
<%@page import="java.util.Map"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<c:set var="hijricalendar" value='<%=GenericPreferencesDAO.getAllPrefs().get("hijricalendar")%>'/>
<c:set var="schType">
<c:choose>
	<c:when test="${category eq 'DOC'}"><insta:ltext key="patient.resourcescheduler.schedulerweekview.doctor"/></c:when>
	<c:when test="${category eq 'OPE'}"><insta:ltext key="patient.resourcescheduler.schedulerweekview.surgery"/></c:when>
	<c:when test="${category eq 'SNP'}"><insta:ltext key="patient.resourcescheduler.schedulerweekview.service"/></c:when>
	<c:when test="${category eq 'DIA'}"><insta:ltext key="patient.resourcescheduler.schedulerweekview.test"/></c:when>
</c:choose>
</c:set>
<html>
<head>
	<title>${title} <insta:ltext key="patient.resourcescheduler.schedulerweekview.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="scheduler.doctorscheduler"/>
	<script>
		var defaultCountryCode= "+${defaultCountryCode}";
	    var hijriPref = '${hijricalendar}';
		var toolbarOptions = getToolbarBundle("js.scheduler.doctorscheduler.toolbar");
		var regPref = ${regPrefJSON};
		var healthAuthoPref = ${healthAuthoPrefJSON};
		var visitTypeDependence = regPref.visit_type_dependence;
		var cpath = '${cpath}';
		var category='${ifn:cleanJavaScript(category)}';
		var screenId = '${screenId}';
		var actionRight = '${ifn:cleanJavaScript(allowbackDated)}';
		var mappedRes = ${not empty mappedRes ? mappedRes : null};
		var serverNow = new Date(<%= (new java.util.Date()).getTime()%>)
		var extraDetails = [];
		var timingsJson = ${timingsJson};
		var doctorCenters = <%= request.getAttribute("cenDoctors") %>;
		var	primaryResourceCentersList = <%= request.getAttribute("primaryResourceCentersList") %>;
		var cancelAppointment='${actionRightsMap.cancel_scheduler_appointment}';
		var addeditAppointment='${actionRightsMap.add_edit_scheduler_rights}';
		var allowApptOverBooking = '${actionRightsMap.allow_appt_overbooking}';
		var roleId = '${roleId}';
		var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
		var max_centers_inc_default = ${max_centers_inc_default};
		var loggedInCenterId = ${centerId};
		var allowMultipleActiveVisits = '${ifn:cleanJavaScript(allowMultipleActiveVisits)}';
		var centerResourcesList = null;
		if(category == 'DOC')
		var jDocDeptNameList = <%= request.getAttribute("schResourceNameList") %>;
		else if (category == 'OPE')
		var jTheatreNameList = <%= request.getAttribute("schResourceNameList") %>;
		<c:if test="${category == 'OPE'}">
			var opApplicableFor = '${opApplicableFor}';
			var surgeryNameRequired = '${genPrefs.map.surgery_name_required}';
			var surgeriesJson=${surgeriesJson};
		</c:if>
		var doctorsJson = <%= request.getAttribute("doctorsJSON") %>;
		var gPrescDocRequired = '${genPrefs.map.prescribing_doctor_required}';
		var gOnePrescDocForOP = '${genPrefs.map.op_one_presc_doc}';
		var resourceAvailabilityOverridesRight = '${urlRightsMap.res_availability}';
		var schedulerGenerateOrder = '${genPrefs.map.scheduler_generate_order}';
		var modAdvancedOT = '${preferences.modulesActivatedMap['mod_advanced_ot']}';
		var doctorsCount = '${doctorsCount}';
		var docId = '${scheduler_user_doctor.map.doctor_id}';
		var empUser = '${scheduler_user_doctor.map.emp_username}';
		var cenName = '${ifn:cleanJavaScript(cenName)}';
		var isScheduled = '${scheduler_user_doctor.map.schedule}';
		var centerId = '${centerId}';
		var modInsExt = '${preferences.modulesActivatedMap['mod_ins_ext']}';
	</script>
    <insta:link type="script" file="resourcescheduler/jquery.calendars.js" />
    <insta:link type="script" file="resourcescheduler/jquery.calendars.plus.js" />
    <insta:link type="css" file="jquery.calendars.picker.css" />
    <insta:link type="script" file="resourcescheduler/jquery.plugin.js" />
    <insta:link type="script" file="resourcescheduler/jquery.calendars.picker.js" />
    <insta:link type="script" file="resourcescheduler/jquery.calendars.ummalqura.js" />
    <insta:link type="css" file="select2.min.css"/>
    <insta:link type="css" file="select2Override.css"/>
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="dashboardsearch.js" />
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="phoneNumberUtil.js"/>
	<insta:link type="js" file="resourcescheduler/SchedulerWeekView.js"/>
	<insta:link type="js" file="resourcescheduler/SchedulerDayView.js"/>
	<insta:link type="script" file="resourcescheduler/schedulerCommon.js"/>
	<insta:link type="js" file="doctorConsultations.js" />
	<style>
		.dateHeader{
			background-color:#f1f1f1;
			border-right:1px #ccc solid;
			border-left:1px #fff solid;
		}
		.today{
			background-color:#f1f1f1;
			border-bottom:1px #999 solid;
			border-right:1px #ccc solid;
			border-left:1px #fff solid;
			font-size:13px;
			padding:5px 5px 2px 5px;
		}
		.hand{
			cursor: pointer;
			text-color: blue;
			decoration:no underline;
			text-decoration: none;
		}
		.progress{
			cursor: copy;
			text-color: blue;
			decoration:no underline;
			text-decoration: none;
		}
		.defaultAvailable{
			background-color : white;
		}
		.notAvailble{
			background-color : #EAEAEA;
		}

		.booked{
			background-color : #FCDFFF;
		}

		.overBooked{
			background-color : #FFDEAD;
		}
		.availble{
			background-color : white;
		}
		.onHoverAvailable{
			background-color : fffbc1;
		}
		.onHoverDefaultAvailable{
			background-color : #E4EBF3;
		}
		.fontClass {
			font-size:13px;
			color:#333;
			text- align:center;
		}
		.navigate {
			padding-left: 5px;
			padding-right: 5px;
		}
		.dropdown{
			width:150px;
		}
		.timeBgMouseOver
		{
			background-color:#E4EBF3;
		}
		.timeBgMouseOut
		{
			background-color:white;
		}
		.yui-skin-sam .yui-ac-input
		{
			position:relative;
		}
		.rowbgToolBar{background-color:#7b9ebe; cursor:pointer;
				border-bottom:0px #666 solid;  border-right:0px #999 solid;
				padding:0px 0px 0px 0px;  color:#fff; 
		}
	</style>

<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="common.incorrect"/>
<insta:js-bundle prefix="registration.patient"/>
<c:set var="version">
	<fmt:message key='insta.software.version' />
</c:set>
<c:set var="resoureScheduleUrl"
	value="${resoureScheduleUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}" />
<c:if test="${category=='OPE'}">
	<script
		src="${cpath}/pages/resourcescheduler/opeappointments.do?method=getScheduleNames&category=${ifn:cleanURL(category)}&${resoureScheduleUrl}">
	</script>
</c:if>

</head>

	<c:set var="rulerBr" value="${canlderList['rulerBreakTime'][0]}"/>
	<c:set var="height" value="${canlderList['defaultHeightInPx'][0]}"/>
	<c:set var="timeruler" value="${canlderList['ruler']}"/>
	<jsp:useBean id="statusMap" class="java.util.HashMap"/>
	<c:set target="${statusMap}" property="Booked" value= "blue" />
	<c:set target="${statusMap}" property="Cancel" value= "red" />
	<c:set target="${statusMap}" property="Noshow" value= "grey" />
	<c:set target="${statusMap}" property="Arrived" value= "black" />
	<c:set target="${statusMap}" property="Confirmed" value= "light_blue" />

<body onload="initResource();init();onCheckDoctor();">
<c:choose>
	<c:when test="${category == 'DOC'}">
		<c:set var="longertext">
 		<insta:ltext key="patient.resourcescheduler.schedulerweekview.note.doctor"/>
 		</c:set>
		</c:when>
		<c:when test="${category == 'OPE'}">
		<c:set var="longertext">
 		<insta:ltext key="patient.resourcescheduler.schedulerweekview.note.surgery"/>
 		</c:set>
		</c:when>
	</c:choose>
	<c:choose>
		<c:when test="${category == 'DOC'}">
			<h1><insta:ltext key="patient.resourcescheduler.schedulerweekview.doctor.weekscheduler"/></h1></c:when>
		<c:when test="${category == 'OPE'}">
			<h1><insta:ltext key="patient.resourcescheduler.schedulerweekview.surgery.weekscheduler"/></h1></c:when>
	</c:choose>
<insta:feedback-panel/>

<form name="mainform">
	<input type="hidden" name="choosenWeekDate" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${choosenWeekDate}"/>'/>
	<input type="hidden" name="day"/>
	<input type="hidden" name="method" value="getWeekView"/>
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="login_center_name" id="login_center_name" value="${ifn:cleanHtmlAttribute(centerName)}"/>
	<input type="hidden" name="oncenterchange" id="oncenterchange" value="false"/>

	<c:set var="formattedDate"> <fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/> </c:set>
	<c:set var="formattedChoosenDate"> <fmt:formatDate pattern="dd-MM-yyyy" value="${choosenWeekDate}"/> </c:set>
	<c:set var="querystring" value="?method=getWeekView&category=${category}&choosenWeekDate=${formattedChoosenDate}&date=${formattedDate}&includeResources=${includeResources}"/>

	<c:set var="showTooltipButton" value="true"/>
	<div style="display: ${empty showTooltipButton or not showTooltipButton ? 'none' : 'block' }"
			class="fltL tooltipactive" id="toolTipSwitch"
			title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.enable.disable.tooltip"/>' ></div>

	<c:choose>
	<c:when test="${category == 'DOC'}">
		<c:set var="path" value="${cpath}/pages/resourcescheduler/docweekview.do${querystring}"/>
		</c:when>
		<c:when test="${category == 'OPE'}">
		<c:set var="path" value="${cpath}/pages/resourcescheduler/opeweekview.do${querystring}"/>
		</c:when>
	</c:choose>

	<div class="searchBasicOpts" style="width: 100%;">
	<div style="clear: both"></div>
	<c:if test="${category == 'DOC' && max_centers_inc_default > 1 && userCenterId == 0}">
		<div class="sboField" >
			<div class="sboFieldLabel"><b><insta:ltext key="patient.resourcescheduler.schedulerweekview.center"/></b></div>
			<div class="sboFieldInput"><select class="dropdown" name="centerId" id="centerId"  onchange="onSelectCenter();onResourceCenterChangeSubmit(this);">
			<c:forEach items="${allCenters}" var="center">
					<option value="${center.map.center_id}" ${param.centerId==center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>

				</c:forEach>
			</select></div>
		</div>
	</c:if>
	<div style="clear: both"></div>
	<c:choose>
		<c:when test="${category == 'DOC'}">
			<c:choose>
				<c:when test="${(roleId !=1 || roleId !=2) && not empty scheduler_user_doctor.map.doctor_name}">
					<c:set var="resourceName"
						value="${scheduler_user_doctor.map.doctor_name} (${scheduler_user_doctor.map.dept_name})" />
				</c:when>
				<c:otherwise>
					<c:set var="resourceName"
						value="${schBean.map.resource_name} (${schBean.map.dept_name})" />
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:when test="${category == 'OPE'}">
			<c:set var="resourceName" value="${schBean.map.resource_name}" />
		</c:when>
	</c:choose>
	<div class="sboField" style="width: 25%;">
		<c:choose>
			<c:when test="${category == 'DOC'}">
				<c:choose>
					<c:when test="${(roleId !=1 || roleId !=2) && not empty scheduler_user_doctor.map.doctor_name}">
						<div class="sboFieldLabel" style="width:250%;"><b><insta:ltext key="patient.resourcescheduler.schedulerweekview.doctor"/>&nbsp;:&nbsp;</b>
							<label id="doctorlbl" >${scheduler_user_doctor.map.doctor_name}
								(${scheduler_user_doctor.map.dept_name}) </label>
						</div>
					</c:when>
					<c:otherwise>
						<div class="sboFieldInput" style="width:90%;"><b><insta:ltext key="patient.resourcescheduler.schedulerweekview.doctor"/>&nbsp;:&nbsp;</b>
							<input type="text" name="resource_name" id="resource_name"
								value="${resourceName}" style="width:95%;" />
							<div id="resource_dropdown"></div>
						</div>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
			<div class="sboFieldLabel"><b><insta:ltext key="patient.resourcescheduler.schedulerweekview.surgery.theatre"/>&nbsp;:&nbsp;</b></div>
				<div class="sboFieldInput" style="width: 90%;">
					<input type="text" name="resource_name" id="resource_name"
						value="${resourceName}" style="width: 90%;" />
					<div id="resource_dropdown"></div>
				</div>
			</c:otherwise>
		</c:choose>
		</div>
	</div>
	<c:if test="${hijricalendar=='Y'}">
	<div class="sboField" style="width: 32%;">
	        <div class="sboFieldLabel">Hijri Calendar: &nbsp;</div>
	        <div class="sboFieldInput">
	                <div style="float: left;"><a href="${path}&day=Now" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.thisweek"/>' class="today"><insta:ltext key="patient.resourcescheduler.schedulerweekview.thisweek"/></a>
				    <img class="imgHelpText" title=<insta:jsString value="${longertext}"/>
					 src="${cpath}/images/help.png"/></div>
					<div style="float: left;"><b><a href="${path}&day=prevWeek" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.prevweek"/>' class="navigate">&lt;&lt;</a></b></div>
					<div style="float: left;"><b><a href="${path}&day=prevDay" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.prevday"/>' class="navigate">&lt;</a></b></div>
					<div style="float: left;"><input id="hijriImagePicker" size="10" readonly>&nbsp;</div>
					<div style="float: left;"><b><a href="${path}&day=nextDay" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.nextday"/>' class="navigate">&gt;</a></b></div>
					<div style="float: left;"><b><a href="${path}&day=nextWeek" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.nextweek"/>' class="navigate">&gt;&gt;</a></b></div>
	        </div>
	</div>
	</c:if>
	<c:if test="${hijricalendar != 'Y'}">
	<div class="sboField" style="width: 32%;">
	        <div class="sboFieldLabel">&nbsp;</div>
	        <div class="sboFieldInput">&nbsp;&nbsp;&nbsp;</div>
	</div>
	</c:if>
	<div class="sboField" style="width: 32%;float: center;">
	        <div class="sboFieldLabel">Gregorian Calendar:&nbsp;&nbsp;<b>${dateString }</b></div>
	        <div class="sboFieldInput">
	        <div style="float: left;"><a href="${path}&day=Now" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.thisweek"/>' class="today"><insta:ltext key="patient.resourcescheduler.schedulerweekview.thisweek"/>
				</a>
				<img class="imgHelpText" title=<insta:jsString value="${longertext}"/>
					 src="${cpath}/images/help.png"/></div>
			<div style="float: left;"><b><a href="${path}&day=prevWeek" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.prevweek"/>' class="navigate">&lt;&lt;</a></b></div>
			<div style="float: left;"><b><a href="${path}&day=prevDay" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.prevday"/>' class="navigate">&lt;</a></b></div>
			<div style="float: left;"><input id="gregImagePicker" size="10" readonly>&nbsp;</div>
			<div style="float: left;"><b><a href="${path}&day=nextDay" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.nextday"/>' class="navigate">&gt;</a></b></div>
			<div style="float: left;"><b><a href="${path}&day=nextWeek" title='<insta:ltext key="patient.resourcescheduler.schedulerweekview.nextweek"/>' class="navigate">&gt;&gt;</a></b></div>
			</div>
	</div>
	</div>

    <table id="schedulerTable" style="border:0.1em #CCCCCC solid" >
		<tr>
			<c:set var="count" value="0"/>
			<c:set var="borderWidth" value="1"/>
			<td id="schTd">
				<table>
					<tr id="schedulerRow">
						<td height="100%">
							<table height="100%" width="40px" style="border-right:1px #CCCCCC solid">
								<tr> <td class="dateHeader"><b><insta:ltext key="patient.resourcescheduler.schedulerweekview.time"/></b></td> </tr>
						        <c:forEach var="rtime" items="${timeruler}" varStatus="st">
						        	<c:set var="rowTime"><fmt:formatDate type="time" value="${rtime}" timeStyle="short" pattern="HH:mm" /></c:set>
						        	<c:set var="substr">
										${fn:substring(rtime,3,5)}
						        	</c:set>

									<c:if test="${substr==00}">
										<c:set var="hrBorder" value="border-top:1px solid;border-bottom:-.3em solid
											border-top-color:#4C0530;border-bottom-color:#CCCCCC;"/>
									</c:if>
									<c:if test="${substr!=00}">
										<c:set var="minBorder" value="border-top:1px solid;border-bottom:-.3em solid
											;border-top-color:#CCCCCC;border-bottom-color:#CCCCCC;"/>
									</c:if>
						        		<c:if test="${rtime == rulerBr}">
					        				<tr>
					        					<td style="height:${height}px;border-top:1px solid;border-top-color:#666666;" bgcolor="#F2F6B2">&nbsp;</td>
					        				</tr>
						        		</c:if>
				        				<tr>
											<td style="height:${height}px;${substr==00?hrBorder:minBorder}" id="${rowTime}" align="right">
												<c:choose>
													<c:when test="${substr==00}">${rowTime}</c:when>
													<c:when test="${substr==15||substr==45}">---</c:when>
													<c:when test="${substr==30}">-----</c:when>
													<c:otherwise>---</c:otherwise>
												</c:choose>
											</td>
										</tr>
							   </c:forEach>
						   </table>
						</td>
						<c:set var="schresource" value="${canlderList['resources']}" />
						<c:forEach var="res" items="${schresource}" varStatus="h">
						<c:set var = "slots" value="${res.slots}"/>
						<c:set var="sindex" value="${h.index}"/>
						<c:set var ="bgcolor" value=""/>
						<c:set var="count" value="${h.index}"/>
						<c:set var="overbook_limit" value=""/>
						<td height="100%" width="180px">
							<table height="100%" width="100%">
								<tr>
									<td class="dateHeader" align="center" width="100%">
										<b>
											<fmt:formatDate pattern="EEEE" value="${res.schedule.scheduleDate}"/>
											<fmt:formatDate pattern="dd - MMM - yyyy" value="${res.schedule.scheduleDate}"/>
											<input type="hidden" name="scheduleName" value="${res.schedule.scheduleName}"/>
											<select class="dropdown" name="resourceSchedule" id="resourceSchedule${sindex}"
													onchange="showSchedules();" style="display:none">
												<c:forEach var="schres" items="${scheduleResourceList}">
													<option value="${schres.map.resource_id}"
														<c:if test="${schres.map.resource_id == res.schedule.scheduleName}">
															<c:set var="overbook_limit" value="${schres.map.overbook_limit}"/>
															<insta:ltext key="patient.resourcescheduler.schedulerweekview.selected"/>
														</c:if> >
														${schres.map.resource_name}
													</option>
												</c:forEach>
											</select>
										</b>
									</td>
								 </tr>
								<tr>
							        <td height="100%"  width="180px">
							        	<table height="100%" width="100%" style="${borderWidth < 5 ?'border-right:1px #CCCCCC solid':''}">
							        	<c:set var="dialogpopup" value="true"/>
							        	<c:set var="borderWidth" value="${borderWidth+1}"/>
							        	<c:forEach var="slot" items="${slots}" varStatus="slotStatus">
							        		<c:set var="hrmin">
							        			${fn:substring(slot.time,0,5)}
							        		</c:set>
							        		<c:set var="rindex" value="${slotStatus.index}"/>

											<c:if test="${slot.availble == 1 && slot.appointmentApplicable && slot.completedOrCancelledCount==0}">
							            		<c:set var ="bgcolor" value="availble"/>
							            	</c:if>
							            	<c:if test="${slot.availble == 1 && slot.appointmentApplicable && slot.completedOrCancelledCount != 0}">
							            		<c:set var ="bgcolor" value="bookedSlot"/>
							            	</c:if>
							            	<c:if test="${slot.availble == 1 && !slot.appointmentApplicable && slot.completedOrCancelledCount==0}">
							            		<c:set var ="bgcolor" value="bookedSlot"/>
							            	</c:if>
							            	<c:if test="${slot.availble == 1 && !slot.appointmentApplicable && slot.completedOrCancelledCount!=0}">
							            		<c:set var ="bgcolor" value="bookedSlot"/>
							            	</c:if>
							            	<c:if test="${slot.availble == 2}">
							            		<c:set var ="bgcolor" value="notAvailble"/>
							            	</c:if>
							            	<c:choose>
							            		<c:when test="${slot.completedOrCancelledCount==0 && slot.appointmentApplicable}">
							            			<c:set var="backColor" value=""/>
							            		</c:when>
							            		<c:when test="${slot.completedOrCancelledCount==0 && !slot.appointmentApplicable && slot.appointmentApplicableCount == 1}">
							            			<c:set var="backColor" value="#FCDFFF"/>
							            		</c:when>
							            		<c:when test="${slot.completedOrCancelledCount==0 && !slot.appointmentApplicable && slot.appointmentApplicableCount >= 2}">
							            			<c:set var="backColor" value="#FFDEAD"/>
							            		</c:when>
							            		<c:when test="${slot.completedOrCancelledCount == 1 && !slot.appointmentApplicable}">
							            			<c:set var="backColor" value="#FFDEAD"/>
							            		</c:when>
							            		<c:when test="${slot.completedOrCancelledCount == 1 && slot.appointmentApplicable}">
							            			<c:set var="backColor" value="#FCDFFF"/>
							            		</c:when>
							            		<c:when test="${slot.completedOrCancelledCount > 1 &&  slot.appointmentApplicable}">
							            			<c:set var="backColor" value="#FFDEAD"/>
							            		</c:when>
							            		<c:when test="${slot.completedOrCancelledCount > 1 &&  !slot.appointmentApplicable}">
							            			<c:set var="backColor" value="#FFDEAD"/>
							            		</c:when>
											</c:choose>
											<c:set var="rowTime">	<fmt:formatDate type="time" value="${slot.time}" timeStyle="short" pattern="HH:mm" />  </c:set>
											<%--applying thick or thin row borders depending upon slot timings --%>
											<c:set var="substr">
												${fn:substring(slot.time,3,5)}
								        	</c:set>
											<c:if test="${substr==00}">
													<c:set var="hrBorder" value="border-top:1px solid;border-bottom:-.3em solid
														border-top-color:#4C0530;border-bottom-color:#CCCCCC;"/>
											</c:if>
											<c:if test="${substr!=00}">
												<c:set var="minBorder" value="border-top:1px solid;border-bottom:-.3em solid
													;border-top-color:#CCCCCC;border-bottom-color:#CCCCCC;"/>
											</c:if>

											<fmt:formatDate var="dateToCompare" pattern="yyyy-MM-dd" value="${res.schedule.scheduleDate}"/>
											<%--comparing current timings and slot timings in order to pop up dialog box --%>
											<c:set var="slothour" value="${fn:substring(slot.time,0,2)}"/>
											<c:set var="slotmin" value="${fn:substring(slot.time,3,5)}"/>
											<c:set var="currenthour" value="${fn:substring(currentTime,11,13)}"/>
											<c:set var="currentmin" value="${fn:substring(currentTime,14,16)}"/>
											<c:set var="currentDate" value="${fn:substring(currentTime,0,10)}"/>
											<c:if test="${slot.time == rulerBr}">
						        				<tr>
						        					<td style="height:${height}px;border-top:1px solid;border-top-color:#666666;color:black"
						        						bgcolor="#F2F6B2" align="center"> <insta:ltext key="patient.resourcescheduler.schedulerweekview.break"/> </td>
						        				</tr>
							        		</c:if>
											<tr>

											<fmt:formatDate var="columnDate" pattern="dd-MM-yyyy" value="${res.schedule.scheduleDate}"/>
											<c:set var="appntIdArray" value=""/>
												<c:choose>
												    <c:when test= "${ not empty slot.appointList }" >
												    	<td style="height:${slot.rowSpan}px;" class="${bgcolor} hand">
												    	<div style="height:${slot.rowSpan}px;overflow-y:scroll">

												    		<c:set var="appntIdArray" value=""/>
												    		<c:set var="appntIdCount" value="0"/>
												    		<c:set var="counter" value="1"/>
												    		<c:set var="appntLength" value="${fn:length(slot.appointList)}"/>
													  		<c:forEach var="appointment" items="${slot.appointList}" varStatus="status">
													  		<c:set var="appntIdArray" value=""/>
													  		<%--collecting appointmentid's as a string which is used to implement "next" "prev" buttons in dialog box--%>
													  		<c:forEach var="appId" items="${slot.appointList}" varStatus="idcount">
													  			<c:if test="${appointment.appointStatus != 'Completed' ||
													  						appointment.appointStatus != 'Cancel' || appointment.appointStatus != 'Noshow'}">
													  				<c:if test="${appId.resourceType == category}">
													  					<c:set var="appntIdArray" value="${appntIdArray},${appId.appointmentId}"/>
													  				</c:if>
													  			</c:if>
													  			<c:if test="${appointment.appointStatus == 'Cancel' || appointment.appointStatus == 'Noshow'}">
													  				<c:set var="appntIdCount" value="${appntIdCount+1}"/>
													  			</c:if>
													  			<c:if test="${appointment.appointStatus == 'Booked' || appointment.appointStatus == 'Confirmed' || appointment.appointStatus=='Arrived'}">
													  				<c:set var="appntIdCount"  value="${idcount.index}"/>
													  			</c:if>
													  		</c:forEach>

													  		<c:set var="aindex" value="${status.index}"/>
															  <c:set var="centralResource">
																	<c:if test="${appointment.resourceType != 'DOC'}">
																		${appointment.scheduleName}
																	</c:if>
																</c:set>
																<c:set var="schedulerType">
																	<c:choose>
																		<c:when test="${appointment.resourceType == 'DOC'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerweekview.consultation"/>
																		</c:when>
																		<c:when test="${appointment.resourceType == 'OPE'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerweekview.surgery"/>
																		</c:when>
																		<c:when test="${appointment.resourceType == 'SNP'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerweekview.service"/>
																		</c:when>
																		<c:when test="${appointment.resourceType == 'DIA'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerweekview.test"/>
																		</c:when>
																	</c:choose>
																</c:set>
																<c:set var="bookedResource"/>
																<c:forEach var="recourceList" items="${appointment.appointResourceList}" varStatus="rx">
																	<c:choose>
																		<c:when test="${rx.index == 0}">
																			<c:set var="bookedResource"> ${recourceList.resourceName} </c:set>
																		</c:when>
																		<c:otherwise>
																			<c:set var="bookedResource"> ${recourceList.resourceName} , ${bookedResource} </c:set>
																		</c:otherwise>
																	</c:choose>
																</c:forEach>
																<c:set var="centerName"/>
																<c:choose>
																	<c:when test="${max_centers_inc_default == 1}">
																		<c:set var="centerName" value=""/>
																	</c:when>
																	<c:otherwise>
																		<c:set var="centerName" value="${appointment.centerName}"/>
																	</c:otherwise>
																</c:choose>
														    	<table width="100%">
																	<c:choose>
																		<c:when test="${appointment.appointStatus == 'Completed' || appointment.appointStatus == 'Cancel' ||
																			appointment.appointStatus == 'Noshow' || appointment.bookedAsSecondaryResource == true}">
																	  		<tr>
																	  			<td style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)}; color:#707070;font-size:11px;" class="show_tooltip"
																	  				id="toolbarRow${rindex}${sindex}${aindex}" bgcolor="${backColor}"
																	  				title="${title}" width="100%">
																	  				 <c:if test="${appointment.appointStatus =='Completed'}">
																				  		<img src="${cpath}/images/green_flag.gif"/>
																				  	</c:if>
																				  	<c:if test="${appointment.appointStatus =='Cancel'}">
																				  				<img src="${cpath}/images/red_flag.gif"/>
																				  	</c:if>
																				  	<c:if test="${appointment.appointStatus =='Noshow'}">
																				  		<img src="${cpath}/images/grey_flag.gif"/>
																				  	</c:if>
																				  	<c:if test="${appointment.appointStatus =='Booked'}">
																				  		<img src='${cpath}/images/dark_blue_flag.gif'>
																		  			</c:if>
																		  			<c:if test="${appointment.appointStatus =='Arrived'}">
																		  				<img src="${cpath}/images/black_flag.gif"/>
																		  			</c:if>
																		  			<c:if test="${appointment.appointStatus =='Confirmed'}">
																		  				<img src="${cpath}/images/blue_flag.gif"/>
																		  			</c:if>
																					<c:if test="${not empty appointment.abbreviation}">
																						<div style="border: 1px solid #ccc; border-radius:0px; display: inline-block; padding: 0px 2px 0px 2px;">${appointment.abbreviation}</div>
																					</c:if>	

																				     ${appointment.patientName} (${schedulerType}), &nbsp;
																					 ${appointment.mrNo}${not empty appointment.mrNo?',':''} &nbsp;
																					 ${appointment.phoneNo}&nbsp; ,<insta:truncLabel value="${centerName}" length ="10"/>

																					 <script>
																						 	extraDetails['toolbarRow${rindex}${sindex}${aindex}'] = {
																						 				'Mr No' : '${appointment.mrNo}',
																						 				'Visit Id' : '${empty appointment.visitId ? None : appointment.visitId}',
																							 			'Name' :   '${appointment.patientName}',
																							 			'Contact' :'${appointment.phoneNo}',
																							 			'Center': <insta:jsString value="${max_centers_inc_default == 1 ? '' : appointment.centerName}"/>,
																							 			'Booked Res' :'${bookedResource}',
																							 			'Central Res' :<insta:jsString value="${centralResource}"/>,
																							 		    'Schedl type' :'${schedulerType}',
																							 		    'Complaint' : <insta:jsString value="${appointment.complaintName}"/>,
																							 		    'Remarks' : <insta:jsString value="${appointment.remarks}"/>
																							};
																					 </script>
																				 </td>
																			 </tr>
																	  			</c:when>
																	  			<c:otherwise>
																	  				<tr>
																	  		<%--collecting appointment id's to use for navigation using next and previous buttons in dailog box--%>
																	  					<td class="${bgcolor} hand show_tooltip" bgcolor="${backColor}"
																	  						onclick="( function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}${aindex}', event, 'schedulerTable',
																								{slotIndex: '${rindex}${sindex}${aindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																								 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}', appntIds: [${appntIdArray}],
																								 addEdit: 'edit', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																								 appt_applicable : ${slot.appointmentApplicable},defaultDur : '${slot.defaultDuration}',
																						 		 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																								 rowtime : '${rowTime}', appointmentId : '${appointment.appointmentId}',appointmentStatus : '${appointment.appointStatus}',
																	  							 slotTime: '${slot.time}', index: '${sindex}', colDate: '${columnDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }'},
																								[${dialogpopup},false, false, true, false, true, true, false,true,false]);} }) ();"

																	  						onmouseover="hideToolBar('${rindex}${sindex}${aindex}');changeColor(this, '${hrmin}');"

																	  						ondblclick="showAddorEditDialog('${rindex}${sindex}${aindex}','${bgcolor}',
																	  									'${dialogpopup}',[${appntIdArray}],'edit',
																	  									'${res.schedule.scheduleName}','${rowTime}',
																	  									'${appointment.appointmentId}','${schedulerDate}',
                                                      '${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
                                                      '${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}',
																	  									'${slot.time}','${sindex}', '${columnDate}','${slot.resource_availabilty_center_id }');"

																						    id="toolbarRow${rindex}${sindex}${aindex}"
																						    style="height:${slot.rowSpan}px;width:176px;${substr==00?hrBorder:minBorder}; color:#707070;font-size:11px;"
																						    title="${title}"
																						    onmouseout="changeColor(this, '${hrmin}');" >
																						     <c:if test="${appointment.appointStatus=='Booked'}">
																				  				<img src='${cpath}/images/dark_blue_flag.gif'>
																				  			</c:if>
																				  			<c:if test="${appointment.appointStatus =='Arrived'}">
																				  				<img src="${cpath}/images/black_flag.gif"/>
																				  			</c:if>
																				  			<c:if test="${appointment.appointStatus =='Confirmed'}">
																				  				<img src="${cpath}/images/blue_flag.gif"/>
																				  			</c:if>
																				  			<c:if test="${not empty appointment.abbreviation}">
																								<div style="border: 1px solid #ccc; border-radius:0px; display: inline-block; padding: 0px 2px 0px 2px;">${appointment.abbreviation}</div>
																							</c:if>	
																						     ${appointment.patientName} (${schedulerType}), &nbsp;
																							 ${appointment.mrNo}${not empty appointment.mrNo?',':''} &nbsp;
																							 ${appointment.phoneNo}&nbsp; ,<insta:truncLabel value="${centerName}" length ="10"/>

																							 <script>
																						 		extraDetails['toolbarRow${rindex}${sindex}${aindex}'] = {
																						 				'Mr No' : '${appointment.mrNo}',
																						 				'Visit Id' : '${empty appointment.visitId ? None : appointment.visitId}',
																							 			'Name' :   '${appointment.patientName}',
																							 			'Contact' :'${appointment.phoneNo}',
																							 			'Center': <insta:jsString value="${max_centers_inc_default == 1 ? '' : appointment.centerName}"/>,
																							 			'Booked Res' :'${bookedResource}',
																							 			'Central Res' :<insta:jsString value="${centralResource}"/>,
																							 		    'Schedl type' :'${schedulerType}',
																							 		    'Complaint' : <insta:jsString value="${appointment.complaintName}"/>,
																							 		    'Remarks' : <insta:jsString value="${appointment.remarks}"/>
																								};
																					 		 </script>
																				  		 </td>
																				 	</tr>
																		  		</c:otherwise>
																  			</c:choose>
																	</c:forEach>
																	<tr>
																	<c:choose>
																		<c:when test="${empty overbook_limit || (not empty overbook_limit && overbook_limit > 0 && appntIdCount < overbook_limit) && (allowApptOverBooking == 'A' || (roleId ==1 || roleId ==2)) }">

																			<td style="width:176px;${substr==00?hrBorder:minBorder};color:#707070;font-size:11px;"
																				class="${bgcolor} hand"
																				onclick="( function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}${aindex}', event, 'schedulerTable',
																							{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																							 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}', appntIds: [${appntIdArray}],
																							 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																							 appt_applicable : ${slot.appointmentApplicable},defaultDur : '${slot.defaultDuration}',
																						 	 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																							 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																		  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${columnDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }'},
																							[${dialogpopup},false, false, false, false, false, true, false,true, false]);} }) ();"

																				onmouseover="hideToolBar('${rindex}${sindex}${aindex}');changeColor(this,'${hrmin}');"

																				ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																							'${dialogpopup}',[${appntIdArray}],'add',
																							'${res.schedule.scheduleName}','${schedulerDate}',
                                              '${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
                                              '${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}',
																							'${rowTime}','No','${slot.time}','${sindex}', '${columnDate}','${slot.resource_availabilty_center_id }');"

																				onmouseout="changeColor(this,'${hrmin}');"
																				id="toolbarRow${rindex}${sindex}" >New
																			</td>
																		</c:when>
																		<c:otherwise>
																			<td style="width:176px;${substr==00?hrBorder:minBorder};"
																				class="${bgcolor}hand" bgcolor="${backColor}"
																				onmouseover="hideToolBar('${rindex}${sindex}${aindex}');changeColor(this,'${hrmin}');"
																				onmouseout="changeColor(this,'${hrmin}');" id="toolbarRow${rindex}${sindex}">
																			</td>
																		</c:otherwise>
																	</c:choose>
																	</tr>
																</table>
																</div>
															</td>
														</c:when>
														<c:otherwise>
														  <c:choose>
														    <c:when test="${bgcolor eq 'availble'}">
														    	<td style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)}; color:#707070;font-size:11px;"
														    		class="${bgcolor} hand" bgcolor="${backColor}"
														    		onclick="( function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}',contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${columnDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }'},
																						[${dialogpopup}, false, false, false, false, false, true, false,true, false]);} }) ();"

																	onmouseover="hideToolBar('${rindex}${sindex}');changeColor(this,'${hrmin}');"

																	ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																				'${dialogpopup}',[${appntIdArray}],'add',
																				'${res.schedule.scheduleName}',
																				'${schedulerDate}',
																				'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
                                        '${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}',
																				'${rowTime}','No','${slot.time}','${sindex}', '${columnDate}','${slot.resource_availabilty_center_id }');"

																	onmouseout="changeColor(this,'${hrmin}');"

																	id="toolbarRow${rindex}${sindex}">	&nbsp;</td>

														    </c:when>
														    <c:when test="${bgcolor eq 'defaultAvailable'}">
														    	<td style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)};color:#707070;font-size:11px;"
														    		class="${bgcolor} hand" bgcolor="${backColor}"
														    		onclick="( function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}',contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${columnDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }'},
																						[${dialogpopup}, false, false, false, false, false, true, false,true, false]);} }) ();"

																	onmouseover="hideToolBar(${rindex}${sindex});changeColor(this,'${hrmin}');"

																	ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																				'${dialogpopup}',[${appntIdArray}],'add',
																				'${res.schedule.scheduleName}',
																				'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
                                        '${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}',
																				'${rowTime}','No','${slot.time}','${sindex}', '${columnDate}','${slot.resource_availabilty_center_id }');"

																	onmouseout="changeColor(this,'${hrmin}');"

																	id="toolbarRow${rindex}${sindex}"> &nbsp;</td>

														    </c:when>
														    <c:when test="${bgcolor eq 'bookedSlot'}">
														    	<td style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)};color:#707070;font-size:11px;"
														    		class="${bgcolor} hand" bgcolor="${backColor}"
														    		onclick="( function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}',contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${columnDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }'},
																						[${dialogpopup}, false, false, false, false, false, true, false,true, false]);} }) ();"

																	onmouseover="hideToolBar(${rindex}${sindex});changeColor(this,'${hrmin}');"

																	ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																				'${dialogpopup}',[${appntIdArray}],'add',
																				'${res.schedule.scheduleName}',
																				'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
                                        '${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}',
																				'${rowTime}','No','${slot.time}','${sindex}', '${schedulerDate}','${slot.resource_availabilty_center_id }');"

																	onmouseout="changeColor(this,'${hrmin}');"

																	id="toolbarRow${rindex}${sindex}"> &nbsp;</td>

														    </c:when>
														    <c:otherwise>
														    	<td style="height:${slot.rowSpan}px;width:176px"
														    		class="${bgcolor} hand" bgcolor="${backColor}"
														    		onclick="( function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}',contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}',overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${columnDate}',resourceUnAvailCenterId: '${slot.resource_unavail_center_id}'},
																						[false, false, false, false, false,false,false, true,true, false]);} }) ();"

																	onmouseover="hideToolBar(${rindex}${sindex});"

														    		id="toolbarRow${rindex}${sindex}"><insta:truncLabel value="${slot.unavailableRemarks}" length="25"/></td>
														    </c:otherwise>
															</c:choose>
														 </c:otherwise>
														</c:choose>
													</tr>
											</c:forEach>
							        	</table>
						       		</td>
								</tr>
							</table>
						</td>
					</c:forEach>
				</tr>
			</table>
		</td>
	</tr>
</table>
</form>

<form name="resourceform" method="POST" action="opeweekview.do">
	<input type="hidden" name="choosenWeekDate" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${choosenWeekDate}"/>'/>
	<input type="hidden" name="method" value="saveAppointment"/>
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="hijriDate" id="hijriDate" value="${ifn:cleanHtmlAttribute(hijriCalDate)}" />
	<input type="hidden" name="gregDate" id="gregDate" value="${gregoDate}" />
	<input type="hidden" name="recurrDate" value=""/>
	<input type="hidden" name="untilDate" value=""/>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="appointmentId"/>
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="defaultDuration" value="${ifn:cleanHtmlAttribute(defaultDuration)}"/>
	<input type="hidden" name="appointmentStatus"/>
	<input type="hidden" name="overbook_limit"/>
	<input type="hidden" name="apptList" />
	<input type="hidden" name="_registrationType" value="">
	<input type="hidden" name="resFilter" value="">
	<input type="hidden" name="arrived" value="">
	<input type="hidden" name="_appointmentCenterId" value="">
	<input type="hidden" name="isArrivedDialogOpened" value="Y"/>
	<input type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />
	<input type="hidden" id="contactId" name="contactId"  value ="" />

	<input type="hidden" id="dialogId" value=""/>
	<div id="dialog" style="visibility:hidden;display:none;">
			<div id="dialog_h" class="hd" style="cursor: move;">${schType}
			<insta:ltext key="patient.resourcescheduler.schedulerdayview.appointment"/> <fmt:formatDate pattern="dd-MM-yyyy" value="${date}" /></div>
		<div class="bd">
			<jsp:include page="SchedulerResources.jsp"/>
			<div>&nbsp;</div>
			<div id="prenxtdiv" style="margin-left:5px;width:200px;float:left">

			<insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.prev" name="Prev" id="Prev" type="button" onclick="getNxtPrevAppId('prev');" />
			<insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.next" name="Nxt" id="Nxt" type="button" onclick="getNxtPrevAppId('nxt');" />
			<insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.new" name="add" id="add" type="button" onclick="addNewAppointment()" />
			</div>
			<div style="margin-left:5px;width:400px">

			<insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.ok" name="ok" id="ok" type="button" onclick="handleSubmit();" />
			<insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.cancel"  type="button" onclick="handleCancel();" />
			</div>
		</div>
	</div>

<c:if test="${not empty resourcesListJSON  }">
<script>
	 var resourcesListJSON = ${resourcesListJSON};
	 var resourceTypes = ${resourceTypesJSON};
</script>
</c:if>

<script>
	var defaultTimeslotJSON = ${defaultTimeSlotsJSON};
	var availbiltyList = ${availbiltyList};
	var scheduleResourceListJSON = ${scheduleResourceListJSON};
	//YAHOO.util.Event.addListener(window, "load", function() {makePopupCalendar('name1','right')});
</script>

</form>

<form name="rescheduleForm">
	<input type="hidden" name="choosenWeekDate" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${choosenWeekDate}"/>'/>
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="method" value="rescheduleAppointment"/>
	<input type="hidden" name="appointmentId"/>
	<input type="hidden" name="appointment_center"/>
	<input type="hidden" name="centerId"/>
	<input type="hidden" name="slotTime"/>
	<input type="hidden" name="rescheduleResourceId"/>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />
	<input type="hidden" id="contactId" name="contactId"  value ="" />
</form>

<form name="availabilityForm">
	<input type="hidden" name="choosenWeekDate" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${choosenWeekDate}"/>'/>
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="method" value="markResourceAvailable"/>
	<input type="hidden" id="availabilityDialogId" value="" />
	<input type="hidden" name="slotTime"/>
	<input type="hidden" name="resourceId"/>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />
	<c:if test="${category == 'DOC' && max_centers_inc_default > 1}">
	<div id="availability_dialog" style="visibility:hidden">
		<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel" id="avDialogTitle"><insta:ltext key="patient.resourcescheduler.schedulerdayview.comment.available"/></legend>
			<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.resourcename"/></td>
				<td class="forminfo"><label id="availableDoctorLbl"></label>
				<input type="hidden" name="availableDoctor"></td>
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.date"/></td>
				<td class="forminfo"><label id="availableDateLbl"></label></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.fromtime"/></td>
				<td class="forminfo"><input type="text" name="avFirstSlotFromTime"
					id="avFirstSlotFromTime" class="timefield" maxlength="5" readonly>
				</td>
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.totime"/></td>
				<td class="forminfo"><input type="text" name="avFirstSlotToTime"
					id="avFirstSlotToTime" class="timefield" maxlength="5" onchange="getCompleteTime(this)"></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerweekview.center"/></td>
				<c:choose>
					<c:when test="${centerId== 0}">
						<td class="forminfo">
						<div id="listAllCenter" style="display:block">
							<select name="_dialog_center" id="_dialog_center" class="dropdown" onchange="fillHidenCenterName(this)">
								<option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
								<c:forEach items="${centers}" var="center">
									<option value="${center.map.center_id}">${center.map.center_name}</option>
								</c:forEach>
							</select>
						</div>
						<div id="listBelongcenter" style="display:none">
							<select name="dialog_center" id="dialog_center" class="dropdown" onchange="fillHidenCenterName(this)"/>
							<option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option></select>
						</div>
						<input type="hidden" name="_dialog_center_hid" id="_dialog_center_hid" value="">
						</td>
					</c:when>
					<c:otherwise>
						<td>
							<select name="dialog_center_id" id="dialog_center_id" class="dropdown" ></select>
							<label id="dialog_label" ></label>
							<input type="hidden" name="dialog_center_hid" id="dialog_center_hid" value="">
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.remarks"/></td>
				<td class="forminfo" colspan="3"><textarea rows="4" cols="32" name="remarks"></textarea></td>
			</tr>
			</table>
		</fieldset>
		<table>
		<tr>
			<td><insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.ok" name="availOk" id="availOk" type="button" onclick="onEditDoctorAvailabilitySubmit();" /></td>
			<td><insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.cancel" type="button" onclick="onEditDoctorAvailabilityCancel();" /></td>
		</tr>
		</table>
		</div>
	</div>
	</c:if>
</form>

<form name="completeAppointmentForm" action="todaysappointments.do" method="POST">
	<input type="hidden" name="_method" value="updateAppointmentStatusAsCompleted"/>
	<input type="hidden" name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}"/>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="appointment_status" value="">
	<input type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}"/>
	<input type="hidden" name="appointment_id">
</form>

<form name="editResourceTimingsForm">
	<input type="hidden" name="method" value="editResourceTimings"/>
	<input type="hidden" name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}"/>
	<input type="hidden" name="_screenName" value="schedulerScreen"/>
	<input type="hidden" name="res_sch_name" value=""/>
	<input type="hidden" name="res_sch_type" value=""/>
	<input type="hidden" name="_col_date" value=""/>
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />
</form>

<form name="nonAvailableForm">
	<input type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}"/>
	<input type="hidden" name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}" />
	<input type="hidden" name="date" id="name1" value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>'/>
	<input type="hidden" name="method" value="markResourceNonAvailable"/>
	<input type="hidden" id="nonAvailabilityDialogId" value=""/>
	<input type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}"/>
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />

	<div id="nonAvailabilityDialog" style="visibility:hidden">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel" id="dialogTitle"><insta:ltext key="patient.resourcescheduler.schedulerweekview.resource.nonavailable"/></legend>
				<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerweekview.resourcename"/></td>
					<td class="forminfo">
						<label id="nonAvailableDoctorLbl"></label>
						<input type="hidden" name="nonAvailableDoctor">
					</td>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerweekview.date"/></td>
					<td class="forminfo">
						<label id="nonAvailableDateLbl"></label>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerweekview.fromtime"/></td>
					<td class="forminfo" >
						<input type="text" name="firstSlotFromTime" id="firstSlotFromTime" class="timefield" maxlength="5" readonly>
					</td>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerweekview.totime"/></td>
					<td class="forminfo">
						<input type="text" name="firstSlotToTime" id="firstSlotToTime" class="timefield" maxlength="5" onchange="getCompleteTime(this)">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerweekview.remarks"/></td>
					<td class="forminfo" colspan="3">
						<textarea rows="4" cols="32"  name="remarks"></textarea>
					</td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr>
					<td><insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.ok" name="nonAvailOk" id="nonAvailOk" type="button" onclick="onEditDoctorNonAvailabilitySubmit();" /></td>
					<td><insta:accessbutton buttonkey="patient.resourcescheduler.schedulerweekview.cancel" type="button" onclick="onEditDoctorNonAvailabilityCancel();" /></td>
				</tr>
			</table>
		</div>
	</div>
</form>

<div class="legend">
	<table cellpadding="0" cellspacing="0" border="0" width="1005">
		<tr>
			<td align="left">
				<a href="${cpath}/schedulerAppointments/auditlog/AuditLogSearch.do?_method=getSearchScreen"><insta:ltext key="patient.resourcescheduler.schedulerweekview.audit"/>
			</td>
			<td align="right">
				<insta:selectdb name="printType" table="printer_definition" id="printType"
							valuecol="printer_id"  displaycol="printer_definition_name"
							value=""/>
			</td>
			<td align="left" width="62px">&nbsp;</td>
		</tr>
	</table>
</div>
<div class="legend">
<table class="legend">
	<tr>
		<td style="padding:0px 5px 0px 5px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.appointmentsts"/>&nbsp;</td>
		<td class="flag"><img src='${cpath}/images/blue_flag.gif'></td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.confirmed"/></td>
		<td class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.notconfirmed"/></td>
		<td class="flag"><img src='${cpath}/images/black_flag.gif'></td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.arrived"/></td>
		<td class="flag"><img src='${cpath}/images/red_flag.gif'></td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.cancelled"/></td>
		<td class="flag"><img src='${cpath}/images/grey_flag.gif'></td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.noshow"/></td>
		<td class="flag"><img src='${cpath}/images/green_flag.gif'></td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.completed"/></td>
		<td align="left" width="55px"></td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr>
		<td style="padding:0px 5px 0px 5px;text-align: right;" ><insta:ltext key="patient.resourcescheduler.schedulerweekview.slotavailability"/>&nbsp;</td>
		<td class="availble" style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.available"/></td>
		<td class="notAvailble" style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.notavailable"/></td>
		<td class="booked" style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.booked"/></td>
		<td class="overBooked" style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td><td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerweekview.overbooked"/></td>
		<td>&nbsp;</td><td>&nbsp;</td>
		<td>&nbsp;</td><td>&nbsp;</td>
		<td>&nbsp;</td><td>&nbsp;</td>
	</tr>
</table>
</div>

</body>
</html>
