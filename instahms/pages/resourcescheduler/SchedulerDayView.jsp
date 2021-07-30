<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page
	import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.bob.hms.common.Preferences"%>
<%@page import="java.util.Map"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'
	scope="request" />
<c:set var="hijricalendar" value='<%=GenericPreferencesDAO.getAllPrefs().get("hijricalendar")%>'/>
<c:set var="schType">
	<c:choose>
		<c:when test="${category eq 'DOC'}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.doctor"/></c:when>
		<c:when test="${category eq 'OPE'}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.surgery"/></c:when>
		<c:when test="${category eq 'SNP'}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.service"/></c:when>
		<c:when test="${category eq 'DIA'}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.test"/></c:when>
	</c:choose>
</c:set>

<c:set var="schAction">
	<c:choose>
		<c:when test="${category eq 'OPE'}">opeappointments</c:when>
		<c:when test="${category eq 'SNP'}">snpappointments</c:when>
		<c:when test="${category eq 'DIA'}">diaappointments</c:when>
	</c:choose>
</c:set>

<html>
<head>
<insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>
<insta:js-bundle prefix="registration.patient"/>
<insta:link type="script" file="resourcescheduler/jquery.calendars.js" />
<insta:link type="script" file="resourcescheduler/jquery.calendars.plus.js" />
<insta:link type="css" file="jquery.calendars.picker.css" />
<insta:link type="script" file="resourcescheduler/jquery.plugin.js" />
<insta:link type="script" file="resourcescheduler/jquery.calendars.picker.js" />
<insta:link type="script" file="resourcescheduler/jquery.calendars.ummalqura.js" />
<script type="text/javascript">
		var defaultCountryCode= "+${defaultCountryCode}";
        var hijriPref = '${hijricalendar}';
		var toolbarOptions = getToolbarBundle("js.scheduler.doctorscheduler.toolbar");
		var regPref = ${regPrefJSON};
		var healthAuthoPref = ${healthAuthoPrefJSON};
		var mappedRes = ${not empty mappedRes ? mappedRes : null};
		var visitTypeDependence = regPref.visit_type_dependence;
		var cpath = '${cpath}';
		var category='${ifn:cleanJavaScript(category)}';
		var screenId = '${screenId}';
		var actionRight = '${ifn:cleanJavaScript(allowbackDated)}';
		var defaultHeight = ${canlderList['defaultHeightInPx'][0]};
		var rulerIterations = ${canlderList['rulerIterations'][0]};
		var serverNow = new Date(<%= (new java.util.Date()).getTime()%>)
		var deptList = ${departmentListJSON};
		var extraDetails = [];
		var timingsJson = ${timingsJson};
		<c:if test="${category == 'OPE'}">
			var opApplicableFor = '${opApplicableFor}'; 
		</c:if>
		var cancelAppointment='${actionRightsMap.cancel_scheduler_appointment}';
		var addeditAppointment='${actionRightsMap.add_edit_scheduler_rights}';
		var allowApptOverBooking = '${actionRightsMap.allow_appt_overbooking}';
		var roleId = '${roleId}';
		var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
		<c:if test="${category == 'SNP'}">
			var serviceNameRequired = '${genPrefs.map.service_name_required}';
		</c:if>
		<c:if test="${category == 'OPE'}">
			var surgeryNameRequired = '${genPrefs.map.surgery_name_required}';
			var surgeriesJson=${surgeriesJson};
		</c:if>

		var	primaryResourceCentersList = <%= request.getAttribute("primaryResourceCentersList") %>;
		var max_centers_inc_default = ${max_centers_inc_default};
		var loggedInCenterId = ${centerId};
		var allowMultipleActiveVisits = '${ifn:cleanJavaScript(allowMultipleActiveVisits)}';
		var centerResourcesList = null;
		var doctorsJson = <%= request.getAttribute("doctorsJSON") %>;
		var condDoctorsJson = [];
		var gPrescDocRequired = '${genPrefs.map.prescribing_doctor_required}';
		var gOnePrescDocForOP = '${genPrefs.map.op_one_presc_doc}';
		var resourceAvailabilityOverridesRight = '${urlRightsMap.res_availability}';
		var schedulerGenerateOrder = '${genPrefs.map.scheduler_generate_order}';
		var modAdvancedOT = '${preferences.modulesActivatedMap['mod_advanced_ot']}';
		var modInsExt = '${preferences.modulesActivatedMap['mod_ins_ext']}';
	</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<insta:link type="css" file="select2.min.css"/>
<insta:link type="css" file="select2Override.css"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="script" file="ajax.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="phoneNumberUtil.js"/>
<insta:link type="script" file="resourcescheduler/SchedulerDayView.js" />
<insta:link type="script" file="resourcescheduler/schedulerCommon.js" />
<insta:link type="js" file="doctorConsultations.js" />
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<title><fmt:formatDate pattern="dd-MMM-yyyy" value="${date}" />
${title}<insta:ltext key="patient.resourcescheduler.schedulerdayview.dayview"/></title>

<style type="text/css">
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

	.onHoverBooked {
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
<c:set var="version">
	<fmt:message key='insta.software.version' />
</c:set>
<c:set var="resoureScheduleUrl"
	value="${resoureScheduleUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}" />

<c:if test="${category=='SNP'}">
	<script
		src="${cpath}/pages/resourcescheduler/snpappointments.do?method=getScheduleNames&category=${ifn:cleanURL(category)}&${resoureScheduleUrl}"></script>
</c:if>
<c:if test="${category=='DIA'}">
	<script
		src="${cpath}/pages/resourcescheduler/diaappointments.do?method=getScheduleNames&category=${ifn:cleanURL(category)}&${resoureScheduleUrl}"></script>
</c:if>
<c:if test="${category=='OPE'}">
	<script
		src="${cpath}/pages/resourcescheduler/opeappointments.do?method=getScheduleNames&category=${ifn:cleanURL(category)}&${resoureScheduleUrl}"></script>
</c:if>


</head>

<jsp:useBean id="statusMap" class="java.util.HashMap" />
<c:set target="${statusMap}" property="Booked" value="blue" />
<c:set target="${statusMap}" property="Cancel" value="red" />
<c:set target="${statusMap}" property="Noshow" value="grey" />
<c:set target="${statusMap}" property="Arrived" value="black" />
<c:set target="${statusMap}" property="Confirmed" value="light_blue" />

<body onload="init();">

<h1>${heading}</h1>

<insta:feedback-panel />

<form name="mainform"><input type="hidden" name="day" /> <input
	type="hidden" name="method" value="getScheduleDetails" />
	<input type="hidden" name="login_center_name" id="login_center_name" value="${ifn:cleanHtmlAttribute(centerName)}"/> <input
	type="hidden" name="date" id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' /> <input
	type="hidden" name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}" /> <c:set
	var="formattedDate">
	<fmt:formatDate pattern="dd-MM-yyyy" value="${date}" />
</c:set> <c:set var="querystring"
	value="?method=getScheduleDetails&category=${category}&date=${formattedDate}&includeResources=${includeResources}&department=${department}" />

<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
	</c:set>
<c:set var="okBtn">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.ok"/>
</c:set>
<c:set var="cancelBtn">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.cancel"/>
</c:set>
<c:set var="mrno">
	<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitId">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.visitid"/>
</c:set>
<c:set var="name">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.name"/>
</c:set>
<c:set var="contact">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.contact"/>
</c:set>
<c:set var="cName">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.centername"/>
</c:set>
<c:set var="bookedRes">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.bookedres"/>
</c:set>
<c:set var="centralRes">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.centralres"/>
</c:set>
<c:set var="schedlType">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.schedltype"/>
</c:set>
<c:set var="complaint">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.complaint"/>
</c:set>
<c:set var="remarks1">
	<insta:ltext key="patient.resourcescheduler.schedulerdayview.remarks"/>
</c:set>

<c:if test="${category=='DOC'}">
	<c:set var="path"
		value="${cpath}/pages/resourcescheduler/docappointments.do${querystring}" />
</c:if> <c:if test="${category=='SNP'}">
	<c:set var="path"
		value="${cpath}/pages/resourcescheduler/snpappointments.do${querystring}" />
</c:if> <c:if test="${category=='DIA'}">
	<c:set var="path"
		value="${cpath}/pages/resourcescheduler/diaappointments.do${querystring}" />
</c:if> <c:if test="${category=='OPE'}">
	<c:set var="path"
		value="${cpath}/pages/resourcescheduler/opeappointments.do${querystring}" />
</c:if> <c:set var="rulerBr" value="${canlderList['rulerBreakTime'][0]}" /> <c:set
	var="height" value="${canlderList['defaultHeightInPx'][0]}" /> <c:set
	var="timeruler" value="${canlderList['ruler']}" /> <c:set
	var="resHeaders" value="${canlderList['headers']}" /> <c:set
	var="showTooltipButton" value="true" />
<div
	style="display: ${empty showTooltipButton or not showTooltipButton ? 'none' : 'block' }"
	class="fltL tooltipactive" id="toolTipSwitch"
	title='<insta:ltext key="patient.resourcescheduler.schedulerdayview.enable.tooltip"/>'></div>
	<div class="searchBasicOpts" style="width: 100%;">
	<c:if test="${category == 'DOC'}">
	<div class="sboField">
			      <div class="sboFieldLabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.filterbydepartment"/></div>
			      <div class="sboFieldInput"><select class="dropdown"
				name="department" id="department" onchange="onDeptChangeSubmit()">
				<option value="">${dummyvalue}</option>
				<c:forEach var="udept" items="${userDepartments}">
					<option value="${udept.dept_id}"
						<c:if test="${udept.dept_id == department}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.selected"/></c:if>>
					${udept.dept_name}</option>
				</c:forEach>
			    </select></div>
	</div>
	</c:if>
	<c:if test="${category != 'DOC' }">
	<div class="sboField">
			      <div class="sboFieldLabel"></div>
			      <div class="sboFieldInput">&nbsp;&nbsp;&nbsp;</div>
	</div>
    </c:if>
	<c:if test="${category == 'DOC' && max_centers_inc_default > 1 && userCenterId == 0}">
	<div class="sboField">
			      <div class="sboFieldLabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.center"/></div>
			      <div class="sboFieldInput"><select class="dropdown"
				name="centerId" id="centerId" onchange="onCenterChangeSubmit(this)">
				<c:forEach items="${allCenters}" var="center">
					<option value="${center.map.center_id}" ${param.centerId==center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>

				</c:forEach>

			    </select></div>
			   <!--  <input type="hidden" name="centerIdHid" id="centerIdHid" value=""> -->
	</div>
	</c:if>
	<c:if test="${category != 'DOC' || (category == 'DOC' && userCenterId != 0)}">
	<div class="sboField">
			      <div class="sboFieldLabel"></div>
			      <div class="sboFieldInput">&nbsp;&nbsp;&nbsp;</div>
	</div>
    </c:if>
	<c:if test="${hijricalendar=='Y'}">
	<div class="sboField" style="width: 28%;">
			      <div class="sboFieldLabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.hijricalendar"/>:</div>
			      <div class="sboFieldInput">
			              <div style="float: left;"><a href="${path}&day=Now" class="legend1" title="Today" style="float: center;padding-left:4px;padding-bottom:2px;padding-right:4px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.today"/>&nbsp;</a></div>
		                  <div style="float: left;">&nbsp;&nbsp;<b><a href="${path}&day=Prev" title='<insta:ltext key="patient.resourcescheduler.schedulerdayview.prevday"/>'>&lt;&nbsp;</a></b></div>
		                  <div style="float: left;"><input id="hijriImagePicker" size="20" readonly></div>
		                  <div style="float: left;"><b><a href="${path}&day=Next" title='<insta:ltext key="patient.resourcescheduler.schedulerdayview.nextday"/>'>&nbsp;&gt;</a></b></div>
		          </div>
	</div>
	</c:if>
	<div class="sboField" style="width: 30%;float: right;">
			      <div class="sboFieldLabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.gregorian.calendar"/>:&nbsp;&nbsp;<b>${dateString }</b></div>
			      <div class="sboFieldInput">
			              <div style="float: left;"><a href="${path}&day=Now" class="legend1" title="Today" style="float: center;padding-left:4px;padding-bottom:2px;padding-right:4px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.today"/>&nbsp;</a></div>
		                  <div style="float: left;">&nbsp;&nbsp;<b><a href="${path}&day=Prev" title='<insta:ltext key="patient.resourcescheduler.schedulerdayview.prevday"/>'>&lt;&nbsp;</a></b></div>
		                  <div style="float: left;" valign="top"><input id="gregImagePicker" size="20" readonly></div>
		                  <div style="float: left;"><b><a href="${path}&day=Next" title='<insta:ltext key="patient.resourcescheduler.schedulerdayview.nextday"/>'>&nbsp;&gt;</a></b></div>
		          </div>
	</div>
	</div>
<table id="schedulerTable" style="border:0.1em #CCCCCC solid">
	<tr>

		<c:set var="count" value="0" />
		<c:set var="borderWidth" value="1" />
		<td colspan="5" id="schTd">
		<table>
			<tr id="schedulerRow">
				<td height="100%">
					<table height="100%" style="border-right:0.1em #CCCCCC solid">
						<tr>
							<td style="height:22px;width:40px" align="center"><insta:ltext key="patient.resourcescheduler.schedulerdayview.time"/></td>
						</tr>
						<c:forEach var="rtime" items="${timeruler}" varStatus="st">
							<c:set var="rowTime">
								<fmt:formatDate type="time" value="${rtime}" timeStyle="short"
									pattern="HH:mm" />
							</c:set>
							<c:set var="substr">
										${fn:substring(rtime,3,5)}
						        	</c:set>

							<c:if test="${substr==00}">
								<c:set var="hrBorder"
									value="border-top:1px solid;border-bottom:-.3em solid
											border-top-color:#4C0530;border-bottom-color:#CCCCCC;" />
							</c:if>
							<c:if test="${substr!=00}">
								<c:set var="minBorder"
									value="border-top:1px solid;border-bottom:-.3em solid
											;border-top-color:#CCCCCC;border-bottom-color:#CCCCCC;" />
							</c:if>
							<c:if test="${rtime == rulerBr}">
								<tr>
									<td
										style="height:${height}px;border-top:1px solid;border-top-color:#666666;"
										bgcolor="#F2F6B2">&nbsp;</td>
								</tr>
							</c:if>
							<tr>
								<td style="height:${height}px;${substr==00?hrBorder:minBorder}"
									id="${rowTime}" align="right"><c:choose>
									<c:when test="${substr==00}">${rowTime}</c:when>
									<c:when
										test="${substr==05||substr==15||substr==25||substr==35||substr==45||substr==55}">---</c:when>
									<c:when test="${substr==30}">----</c:when>
									<c:otherwise>---</c:otherwise>
								</c:choose></td>
							</tr>
						</c:forEach>
					</table>
				</td>
				<c:set var="schresource" value="${canlderList['resources']}" />
				<c:forEach var="res" items="${schresource}" varStatus="h">
					<input type="hidden" name="scheduleName"
						value="${res.schedule.scheduleName}" />
					<c:set var="slots" value="${res.slots}" />
					<c:set var="sindex" value="${h.index}" />
					<c:set var="bgcolor" value="" />
					<c:set var="count" value="${h.index}" />
					<c:set var="overbook_limit" value="" />
					<td height="100%">
					<table height="100%">
						<tr>
							<td id="resourceTd${sindex}"
								style="height:22px;border-right:0.1em #CCCCCC solid"><c:choose>
								<c:when test="${h.last&& h.count == 1}">
									<select class="dropdown" name="resourceSchedule"
										id="resourceSchedule${sindex}" onchange="showSchedules();">
										<c:forEach var="schres" items="${scheduleResourceList}">
											<option value="${schres.map.resource_id}"
												<c:if test="${schres.map.resource_id == res.schedule.scheduleName}">
																	<c:set var="overbook_limit" value="${schres.map.overbook_limit}"/>
																	<insta:ltext key="patient.resourcescheduler.schedulerdayview.selected"/>
																</c:if>>
											${schres.map.resource_name}</option>
										</c:forEach>
									</select>
									<a href="javascript:void(0);"> <label width="15" border="0"
										height="15"></label> </a>
								</c:when>
								<c:otherwise>
									<div style="float: left"><select class="dropdown"
										name="resourceSchedule" id="resourceSchedule${sindex}"
										onchange="showSchedules();">
										<c:forEach var="schres" items="${scheduleResourceList}">
											<option value="${schres.map.resource_id}"
												<c:if test="${schres.map.resource_id == res.schedule.scheduleName}">
																	<c:set var="overbook_limit" value="${schres.map.overbook_limit}"/>
																	<insta:ltext key="patient.resourcescheduler.schedulerdayview.selected"/>
																</c:if>>
											${schres.map.resource_name}</option>
										</c:forEach>
									</select> <a href="javascript:excludeResource('${sindex}');"> <img
										src="${cpath}/images/fileclose.png" title="close" width="15"
										border="0" height="15"
										style="cursor:pointer;vertical-align:-.3em;" /> </a></div>
								</c:otherwise>
							</c:choose></td>
						</tr>
						<tr>
							<td height="100%">
							<table height="100%"
								style="${borderWidth < 5?'border-right:1px #CCCCCC solid':''}">
								<c:set var="dialogpopup" value="true" />
								<c:set var="borderWidth" value="${borderWidth+1}" />
								<c:forEach var="slot" items="${slots}" varStatus="slotStatus">
									<c:set var="hrmin">
							        			${fn:substring(slot.time,0,5)}
							        		</c:set>
									<c:set var="rindex" value="${slotStatus.index}" />

									<c:if
										test="${slot.availble == 1 && slot.appointmentApplicable && slot.completedOrCancelledCount==0}">
										<c:set var="bgcolor" value="defaultAvailable" />
									</c:if>
									<c:if
										test="${slot.availble == 1 && slot.appointmentApplicable && slot.completedOrCancelledCount != 0}">
										<c:set var="bgcolor" value="bookedSlot" />
									</c:if>
									<c:if
										test="${slot.availble == 1 && !slot.appointmentApplicable && slot.completedOrCancelledCount==0}">
										<c:set var="bgcolor" value="bookedSlot" />
									</c:if>
									<c:if
										test="${slot.availble == 1 && !slot.appointmentApplicable && slot.completedOrCancelledCount!=0}">
										<c:set var="bgcolor" value="bookedSlot" />
									</c:if>
									<c:if test="${slot.availble == 2}">
										<c:set var="bgcolor" value="notAvailble" />
									</c:if>
									<c:choose>
										<c:when
											test="${slot.completedOrCancelledCount==0 && slot.appointmentApplicable}">
											<c:set var="backColor" value="" />
										</c:when>
										<c:when
											test="${slot.completedOrCancelledCount==0 && !slot.appointmentApplicable && slot.appointmentApplicableCount == 1}">
											<c:set var="backColor" value="#FCDFFF" />
										</c:when>
										<c:when
											test="${slot.completedOrCancelledCount==0 && !slot.appointmentApplicable && slot.appointmentApplicableCount >= 2}">
											<c:set var="backColor" value="#FFDEAD" />
										</c:when>
										<c:when
											test="${slot.completedOrCancelledCount == 1 && !slot.appointmentApplicable}">
											<c:set var="backColor" value="#FFDEAD" />
										</c:when>
										<c:when
											test="${slot.completedOrCancelledCount == 1 && slot.appointmentApplicable}">
											<c:set var="backColor" value="#FCDFFF" />
										</c:when>
										<c:when
											test="${slot.completedOrCancelledCount > 1 &&  slot.appointmentApplicable}">
											<c:set var="backColor" value="#FFDEAD" />
										</c:when>
										<c:when
											test="${slot.completedOrCancelledCount > 1 &&  !slot.appointmentApplicable}">
											<c:set var="backColor" value="#FFDEAD" />
										</c:when>
									</c:choose>
									<c:set var="rowTime">
										<fmt:formatDate type="time" value="${slot.time}"
											timeStyle="short" pattern="HH:mm" />
									</c:set>
									<%--applying thick or thin row borders depending upon slot timings --%>
									<c:set var="substr">
												${fn:substring(slot.time,3,5)}
								        	</c:set>
									<c:if test="${substr==00}">
										<c:set var="hrBorder"
											value="border-top:1px solid;border-bottom:-.3em solid
														border-top-color:#4C0530;border-bottom-color:#CCCCCC;" />
									</c:if>
									<c:if test="${substr!=00}">
										<c:set var="minBorder"
											value="border-top:1px solid;border-bottom:-.3em solid
													;border-top-color:#CCCCCC;border-bottom-color:#CCCCCC;" />
									</c:if>

									<%--comparing current timings and slot timings in order to pop up dialog box --%>
									<c:set var="slothour" value="${fn:substring(slot.time,0,2)}" />
									<c:set var="slotmin" value="${fn:substring(slot.time,3,5)}" />
									<c:set var="currenthour"
										value="${fn:substring(currentTime,11,13)}" />
									<c:set var="currentmin"
										value="${fn:substring(currentTime,14,16)}" />
									<c:set var="currentDate"
										value="${fn:substring(currentTime,0,10)}" />
									<c:if test="${slot.time == rulerBr}">
										<tr>
											<td
												style="height:${height}px;border-top:1px solid;border-top-color:#666666;color:black"
												bgcolor="#F2F6B2" align="center"><insta:ltext key="patient.resourcescheduler.schedulerdayview.break"/></td>
										</tr>
									</c:if>
									<tr>
										<fmt:formatDate var="schedulerDate" pattern="dd-MM-yyyy"
											value="${date}" />

										<c:set var="appntIdArray" value="" />
										<c:choose>
											<c:when test="${ not empty slot.appointList }">
												<td style="height:${slot.rowSpan}px;width:176px"
													class="${bgcolor} hand">
												<div
													style="height:${slot.rowSpan}px;width:176px;overflow-y:scroll">

												<c:set var="appntIdArray" value="" /> <c:set
													var="appntIdCount" value="0" /> <c:set var="counter"
													value="1" /> <c:set var="appntLength"
													value="${fn:length(slot.appointList)}" /> <c:forEach
													var="appointment" items="${slot.appointList}"
													varStatus="status">
													<c:set var="appntIdArray" value="" /> 
													<%--collecting appointmentid's as a string which is used to implement "next" "prev" buttons in dialog box--%>
													<c:forEach var="appId" items="${slot.appointList}"
														varStatus="idcount">
														<c:if
															test="${appointment.appointStatus != 'Completed' ||
													  						appointment.appointStatus != 'Cancel' || appointment.appointStatus != 'Noshow'}">
															<c:if test="${appId.resourceType == category}">
																<c:set var="appntIdArray"
																	value="${appntIdArray},${appId.appointmentId}" />
															</c:if>
														</c:if>
														<c:if
															test="${appointment.appointStatus == 'Cancel' || appointment.appointStatus == 'Noshow'}">
															<c:set var="appntIdCount" value="${appntIdCount+1}" />
														</c:if>
														<c:if
															test="${appointment.appointStatus == 'Booked' || appointment.appointStatus == 'Confirmed' || appointment.appointStatus=='Arrived'}">
															<c:set var="appntIdCount" value="${idcount.index}" />
															
														</c:if>
													</c:forEach>

													<c:set var="aindex" value="${status.index}" />
													<c:set var="centralResource">
														<c:if test="${appointment.resourceType != 'DOC'}">
																		${appointment.scheduleName}${appointment.resourceType eq 'SNP' ? appointment.departmentName : ''}
																	</c:if>
													</c:set>
													<c:set var="schedulerType">
														<c:choose>
															<c:when test="${appointment.resourceType == 'DOC'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerdayview.consultation"/>
																		</c:when>
															<c:when test="${appointment.resourceType == 'OPE'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerdayview.surgery"/>
																		</c:when>
															<c:when test="${appointment.resourceType == 'SNP'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerdayview.service"/>
																		</c:when>
															<c:when test="${appointment.resourceType == 'DIA'}">
																			<insta:ltext key="patient.resourcescheduler.schedulerdayview.test"/>
																		</c:when>
														</c:choose>
													</c:set>
													<c:set var="bookedResource" />
													<c:forEach var="recourceList"
														items="${appointment.appointResourceList}" varStatus="rx">
														<c:choose>
															<c:when test="${rx.index == 0}">
																<c:set var="bookedResource"> ${recourceList.resourceName} </c:set>
																		</c:when>
																		<c:otherwise>
																			<c:set var="bookedResource"> ${recourceList.resourceName} , ${bookedResource} </c:set>
																</c:otherwise>
														</c:choose>
													</c:forEach>
													<c:set var="centerName" />
													<c:choose>
														<c:when test="${max_centers_inc_default == 1}">
															<c:set var="centerName" value="" />
														</c:when>
														<c:otherwise>
															<c:set var="centerName" value="${appointment.centerName}" />
														</c:otherwise>
													</c:choose>
													<table>
														<c:choose>
															<c:when
																test="${appointment.appointStatus == 'Completed' ||
																					appointment.appointStatus == 'Cancel' || appointment.appointStatus == 'Noshow' || appointment.bookedAsSecondaryResource == true}">
																<tr>
																	<td
																		style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)}; color:#707070;font-size:11px;"
																		class="show_tooltip"
																		id="toolbarRow${rindex}${sindex}${aindex}"
																		bgcolor="${backColor}">
																	<div style="display: inline-block;"><c:if
																		test="${appointment.appointStatus =='Completed'}">
																		<img src="${cpath}/images/green_flag.gif" />
																	</c:if> <c:if test="${appointment.appointStatus =='Cancel'}">
																		<img src="${cpath}/images/red_flag.gif" />
																	</c:if> <c:if test="${appointment.appointStatus =='Noshow'}">
																		<img src="${cpath}/images/grey_flag.gif" />
																	</c:if> <c:if test="${appointment.appointStatus =='Booked'}">
																		<img src='${cpath}/images/dark_blue_flag.gif'>
																	</c:if> <c:if test="${appointment.appointStatus =='Arrived'}">
																		<img src="${cpath}/images/black_flag.gif" />
																	</c:if> <c:if
																		test="${appointment.appointStatus =='Confirmed'}">
																		<img src="${cpath}/images/blue_flag.gif" />
																	</c:if></div> 
																	<c:if test="${not empty appointment.abbreviation}">
																		<div style="border: 1px solid #ccc; border-radius:0px; display: inline-block; padding: 0px 2px 0px 2px;">${appointment.abbreviation}</div>
																	</c:if>	
																	${appointment.patientName} (${schedulerType}), &nbsp;
																	${appointment.mrNo}${not empty appointment.mrNo?',':''}
																	&nbsp; ${appointment.visitId}${not empty
																	appointment.visitId?',':''} &nbsp;
																	${appointment.phoneNo}&nbsp;,<insta:truncLabel
																	value="${centerName}" length ="10"/> <script>
																	extraDetails['toolbarRow${rindex}${sindex}${aindex}'] =
																	{ '${ifn:cleanJavaScript(mrno)}' : '${appointment.mrNo}', '${ifn:cleanJavaScript(visitId)}' :
																	'${empty appointment.visitId ? None :
																	appointment.visitId}', '${name}' :
																	'${appointment.patientName}', '${ifn:cleanJavaScript(contact)}'
																	:'${appointment.phoneNo}', '${cName}':
																	<insta:jsString value="${max_centers_inc_default == 1 ?
																	'' : appointment.centerName}"/>, '${bookedRes}'
																	:'${bookedResource}', '${centralRes}' :<insta:jsString
																	value="${centralResource}"/>, '${schedlType}'
																	:'${schedulerType}', '${ifn:cleanJavaScript(complaint)}' : <insta:jsString
																	value="${appointment.complaintName}"/>, '${remarks1}' :
																	<insta:jsString value="${appointment.remarks}"/> }; </script></td>
																</tr>
															</c:when>
															<c:otherwise>
																<tr>
																	<%--collecting appointment id's to use for navigation using next and previous buttons in dailog box--%>
																	<td class="${bgcolor} hand show_tooltip"
																		bgcolor="${backColor}"
																		onclick="(function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}${aindex}', event, 'schedulerTable',
																		{slotIndex: '${rindex}${sindex}${aindex}', slotClass: '${bgcolor}',visit_id : '${appointment.visitId}',
																				 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}', appntIds: [${appntIdArray}],
																				 addEdit: 'edit', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																				 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																				 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																				 rowtime : '${rowTime}', appointmentId : '${appointment.appointmentId}', appointmentStatus : '${appointment.appointStatus}',
													  							 paymentStatus: '${appointment.paymentStatus}', slotTime: '${slot.time}', index: '${sindex}', colDate: '${schedulerDate}',
													  							 paidAtSource: '${appointment.paidAtSource}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }',
													  							 packageId: '${appointment.packageId}'},
																				[${dialogpopup}, false, false, true, false, true, true, false,true,false]);} }) ();"
																		onmouseover="hideToolBar('${rindex}${sindex}${aindex}');changeColor(this, '${hrmin}');"
																		ondblclick="showAddorEditDialog('${rindex}${sindex}${aindex}','${bgcolor}',
																	  									'${dialogpopup}',[${appntIdArray}],'edit',
																	  									'${res.schedule.scheduleName}','${rowTime}',
																	  									'${appointment.appointmentId}',
																	  									'${slot.time}','${sindex}', '${schedulerDate}',
																										'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
																										'${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}', '${slot.resource_availabilty_center_id }');"
																		id="toolbarRow${rindex}${sindex}${aindex}"
																		style="height:${slot.rowSpan}px;width:176px;${substr==00?hrBorder:minBorder}; color:#707070;font-size:11px;"
																		onmouseout="changeColor(this, '${hrmin}');">
																	<div style="display: inline-block;"><c:if
																		test="${appointment.appointStatus =='Booked'}">
																		<img src='${cpath}/images/dark_blue_flag.gif'>
																	</c:if> <c:if test="${appointment.appointStatus =='Arrived'}">
																		<img src="${cpath}/images/black_flag.gif" />
																	</c:if> <c:if
																		test="${appointment.appointStatus =='Confirmed'}">
																		<img src="${cpath}/images/blue_flag.gif" />
																	</c:if></div> 
																	<c:if test="${not empty appointment.abbreviation}">
																		<div style="border: 1px solid #ccc; border-radius:0px; display: inline-block; padding: 0px 2px 0px 2px;">${appointment.abbreviation}</div>
																	</c:if>	
																	${appointment.patientName}(${schedulerType}),
																	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ${appointment.mrNo}${not
																	empty appointment.mrNo?',':''} &nbsp;
																	${appointment.visitId}${not empty
																	appointment.visitId?',':''} &nbsp;
																	${appointment.phoneNo} &nbsp;,<insta:truncLabel
																	value="${centerName}" length ="10"/>&nbsp; <script>
																	extraDetails['toolbarRow${rindex}${sindex}${aindex}']=
																	{ '${ifn:cleanJavaScript(mrno)}' : '${appointment.mrNo}', '${ifn:cleanJavaScript(visitId)}' :
																	'${empty appointment.visitId ? None :
																	appointment.visitId}', '${name}' :
																	'${appointment.patientName}', '${ifn:cleanJavaScript(contact)}':
																	'${appointment.phoneNo}', '${cName}': <insta:jsString
																	value="${max_centers_inc_default == 1 ? '' :
																	appointment.centerName}"/>, '${bookedRes}' :
																	'${bookedResource}', '${centralRes}' : <insta:jsString
																	value="${centralResource}"/>, '${schedlType}'
																	:'${schedulerType}', '${ifn:cleanJavaScript(complaint)}' : <insta:jsString
																	value="${appointment.complaintName}"/>, '${remarks1}' :
																	<insta:jsString value="${appointment.remarks}"/> }; </script></td>
																</tr>

															</c:otherwise>
														</c:choose>
														</c:forEach>
														<tr>
															<c:choose>
																<c:when
																	test="${empty overbook_limit || (not empty overbook_limit && overbook_limit > 0 && appntIdCount < overbook_limit) && (allowApptOverBooking == 'A' || (roleId ==1 || roleId ==2)) }">
																	<td
																		style="width:176px;${substr==00?hrBorder:minBorder};font-size:11px;"
																		class="${available} hand"
																		onclick="(function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}${aindex}', event, 'schedulerTable',
																							{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																							 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																							 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																							 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																							 apptDur : '${slot.appointDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																							 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																		  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${schedulerDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }',
																		  					 packageId: '${appointment.packageId}'},
																							[${dialogpopup}, false, false, false, false, false, true, false,true,false]);"
																		onmouseover="hideToolBar('${rindex}${sindex}${aindex}');changeColor(this,'${hrmin}');} }) ();"
																		ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																							'${dialogpopup}',[${appntIdArray}],'add',
																							'${res.schedule.scheduleName}','${overbook_limit}',
																							'${rowTime}','No','${slot.time}','${sindex}', '${schedulerDate}',
																							'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
																							'${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}','${slot.resource_availabilty_center_id }');"
																		onmouseout="changeColor(this,'${hrmin}');"
																		id="toolbarRow${rindex}${sindex}"><insta:ltext key="patient.resourcescheduler.schedulerdayview.new"/></td>
																</c:when>
																<c:otherwise>
																	<td
																		style="width:176px;${substr==00?hrBorder:minBorder};font-size:11px;"
																		class="${bgcolor} hand" bgcolor="${backColor}"
																		onmouseover="hideToolBar('${rindex}${sindex}${aindex}');changeColor(this,'${hrmin}');"
																		onmouseout="changeColor(this,'${hrmin}');"
																		id="toolbarRow${rindex}${sindex}"></td>
																</c:otherwise>

															</c:choose>
														</tr>
													</table></div>
												</td>
											</c:when>
											<c:otherwise>
												<c:choose>
													<c:when test="${bgcolor eq 'availble'}">
														<td
															style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)};font-size:11px;"
															class="${bgcolor} hand" bgcolor="${backColor}"
															onclick="(function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${schedulerDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }',
																	  					 packageId: '${appointment.packageId}'},
																						[${dialogpopup}, false, false, false, false, false, true, false,true,false]);} }) ();"
															onmouseover="hideToolBar('${rindex}${sindex}');changeColor(this,'${hrmin}');"
															ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																				'${dialogpopup}',[${appntIdArray}],'add',
																				'${res.schedule.scheduleName}',
																				'${rowTime}','No','${slot.time}','${sindex}', '${schedulerDate}',
																				'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
																				'${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}', '${slot.resource_availabilty_center_id }');"
															onmouseout="changeColor(this,'${hrmin}');"
															id="toolbarRow${rindex}${sindex}">&nbsp;</td>

													</c:when>
													<c:when test="${bgcolor eq 'defaultAvailable'}">
														<td
															style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)};font-size:11px;"
															class="${bgcolor} hand" bgcolor="${backColor}"
															onclick="(function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}',overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${schedulerDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }',
																	  					 packageId: '${appointment.packageId}'},
																						[${dialogpopup}, false, false, false, false, false, true, false, true, false]);} }) ();"
															onmouseover="hideToolBar(${rindex}${sindex});changeColor(this,'${hrmin}');"
															ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																				'${dialogpopup}',[${appntIdArray}],'add',
																				'${res.schedule.scheduleName}',
																				'${rowTime}','No','${slot.time}','${sindex}', '${schedulerDate}',
																				'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
																				'${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}', '${slot.resource_availabilty_center_id }');"
															onmouseout="changeColor(this,'${hrmin}');"
															id="toolbarRow${rindex}${sindex}">&nbsp;</td>

													</c:when>
													<c:when test="${bgcolor eq 'bookedSlot'}">
														<td
															style="height:${slot.rowSpan}px;width:176px;${(substr==00?hrBorder:minBorder)};font-size:11px;"
															class="${bgcolor} hand" bgcolor="${backColor}"
															onclick="(function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${schedulerDate}',resourceAvailabiltyCenterId: '${slot.resource_availabilty_center_id }',
																	  					 packageId: '${appointment.packageId}'},
																						[${dialogpopup}, false, false, false, false, false, true, false, true, false]);} }) ();"
															onmouseover="hideToolBar(${rindex}${sindex});changeColor(this,'${hrmin}');"
															ondblclick="showAddorEditDialog('${rindex}${sindex}','${bgcolor}',
																				'${dialogpopup}',[${appntIdArray}],'add',
																				'${res.schedule.scheduleName}',
																				'${rowTime}','No','${slot.time}','${sindex}', '${schedulerDate}',
																				'${appointment.mrNo}','${appointment.contactId}', '${appointment.visitId}', '${slot.appointmentApplicable}',
																				'${slot.defaultDuration}', '${slot.appointDuration}', '${appointment.appointStatus}','${slot.resource_availabilty_center_id }');"
															onmouseout="changeColor(this,'${hrmin}');"
															id="toolbarRow${rindex}${sindex}">&nbsp;</td>

													</c:when>
													<c:otherwise>
														<td style="height:${slot.rowSpan}px;width:176px;font-size:11px;"
															class="${bgcolor} hand" bgcolor="${backColor}"
															onclick="(function() { if (${(empty appointment.isPatientGroupAccessible) || (appointment.isPatientGroupAccessible == 'Y')}) {showToolbar('${rindex}${sindex}', event, 'schedulerTable',
																						{slotIndex: '${rindex}${sindex}', slotClass: '${bgcolor}', visit_id : '${appointment.visitId}',
																						 dialogpopup: '${dialogpopup}',mrNo: '${appointment.mrNo}', contactId: '${appointment.contactId}',appntIds: [${appntIdArray}],
																						 addEdit: 'add', schName: '${res.schedule.scheduleName}', overbook_limit : '${overbook_limit}', apptList:'${appntIdCount}',
																						 appt_applicable : ${slot.appointmentApplicable}, defaultDur : '${slot.defaultDuration}',
																						 apptDur : '${appointment.appointmentDuration}',category:'${ifn:cleanJavaScript(category)}',centerId:'${appointment.centerId}',
																						 rowtime : '${rowTime}',appointmentId : 'No', appointmentStatus : '${appointment.appointStatus}',
																	  					 slotTime: '${slot.time}', index: '${sindex}', colDate: '${schedulerDate}' ,resourceUnAvailCenterId: '${slot.resource_unavail_center_id}',
																	  					 packageId: '${appointment.packageId}'},
																						[false, false, false, false, false,false,false, true,true, false]);} }) ();"
															onmouseover="hideToolBar(${rindex}${sindex});"
															id="toolbarRow${rindex}${sindex}"><insta:truncLabel
															value="${slot.unavailableRemarks}" length="25" /></td>
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


<form name="resourceform"><input type="hidden" name="day" /> <input
	type="hidden" name="method" value="saveAppointment" /> <input
	type="hidden" name="date" id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' />
	<input
	type="hidden" name="hijriDate" id="hijriDate"
	value="${ifn:cleanHtmlAttribute(hijriCalDate)}" /> <input
	type="hidden" name="gregDate" id="gregDate"
	value="${gregoDate}" />
	<input type="hidden" name="recurrDate" value=""/><input type="hidden" name="untilDate" value=""/> <input
	type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}" /> <input
	type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}" /> <input
	type="hidden" name="appointmentId" /> <input type="hidden"
	name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}" /> <input
	type="hidden" name="defaultDuration" value="${ifn:cleanHtmlAttribute(defaultDuration)}" /> <input
	type="hidden" name="appointmentStatus" /> <input type="hidden"
	name="overbook_limit" /> <input type="hidden" name="_registrationType"
	value=""> <input type="hidden" name="resFilter" value="">
	<input type="hidden" id="contactId" name="contactId"  value ="" />
<input type="hidden" name="arrived" value=""> <input
	type="hidden" name="_appointmentCenterId" value="" /> <input
	type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />
	<input type="hidden" name="apptList" />
<input type="hidden" name="isArrivedDialogOpened" value="Y"/>
<input type="hidden" id="dialogId" value="" />
<div id="dialog" style="visibility:hidden;display:none;">
<div id="dialog_h" class="hd" style="cursor: move;">${schType}
<insta:ltext key="patient.resourcescheduler.schedulerdayview.appointment"/> <fmt:formatDate pattern="dd-MM-yyyy" value="${date}" /></div>
<div class="bd"><jsp:include page="SchedulerResources.jsp" />
<div>&nbsp;</div>
<div id="prenxtdiv" style="margin-left:5px;width:200px;float:left">
<button type="button" id="Prev" name="Prev"
	onclick="getNxtPrevAppId('prev');" accessKey="P"><b><u><insta:ltext key="patient.resourcescheduler.schedulerdayview.p"/></u></b><insta:ltext key="patient.resourcescheduler.schedulerdayview.rev"/></button>
<button type="button" id="Nxt" name="Nxt"
	onclick="getNxtPrevAppId('nxt');" accessKey="N"><b><u><insta:ltext key="patient.resourcescheduler.schedulerdayview.n"/></u></b><insta:ltext key="patient.resourcescheduler.schedulerdayview.ext"/></button>
<button type="button" id="add" name="add" onclick="addNewAppointment()"
	accessKey="e"><insta:ltext key="patient.resourcescheduler.schedulerdayview.n"/><b><u><insta:ltext key="patient.resourcescheduler.schedulerdayview.e"/></u></b><insta:ltext key="patient.resourcescheduler.schedulerdayview.w"/></button>
</div>
<div style="margin-left:5px;width:400px">
<button type="button" name="ok" id="ok" onclick="handleSubmit();"
	accessKey="O"><b><u><insta:ltext key="patient.resourcescheduler.schedulerdayview.o"/></u></b><insta:ltext key="patient.resourcescheduler.schedulerdayview.k"/></button>&nbsp;
<button type="button" onclick="handleCancel();" accessKey="C"><b><u><insta:ltext key="patient.resourcescheduler.schedulerdayview.c"/></u></b><insta:ltext key="patient.resourcescheduler.schedulerdayview.ancel"/></button>
</div>
</div>
</div>

<c:if test="${not empty resourcesListJSON  }">
	<script>
		 var resourcesListJSON = ${resourcesListJSON};
		 var resourceTypes = ${resourceTypesJSON};
	</script>
</c:if> <script>
		var defaultTimeslotJSON = ${defaultTimeSlotsJSON};
		var availbiltyList = ${availbiltyList};
		var scheduleResourceListJSON = ${scheduleResourceListJSON};
		//YAHOO.util.Event.addListener(window, "load", function() {makePopupCalendar('name1','right')});
	</script></form>

<form name="rescheduleForm" method="POST" action="${schAction}.do"><input type="hidden"
	name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}" /> <input
	type="hidden" name="date" id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' /> <input
	type="hidden" name="method" value="rescheduleAppointment" /> <input
	type="hidden" name="appointmentId" /> <input type="hidden"
	name="centerId" /> <input type="hidden" name="appointment_center" /> <input
	type="hidden" name="slotTime" /> <input type="hidden"
	name="rescheduleResourceId" /> <input type="hidden" name="category"
	value="${ifn:cleanHtmlAttribute(category)}" /> <input type="hidden" name="department"
	value="${ifn:cleanHtmlAttribute(department)}" /></form>

<form name="availabilityForm" method="POST" action="${schAction}.do"><input type="hidden"
	name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}" /> <input
	type="hidden" name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}" /><input
	type="hidden" name="date" id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' /> <input
	type="hidden" name="method" value="markResourceAvailable" /> <input
	type="hidden" id="availabilityDialogId" value="" /> <input
	type="hidden" name="slotTime" /> <input
	type="hidden" name="resourceId" /><input
	type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}" /> <input
	type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}" /> <input
	type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />

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
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.center"/></td>
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
						<select name="dialog_center" id="dialog_center" class="dropdown" onchange="fillHidenCenterName(this)">
						<option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
						</select>
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
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.remarks"/>:</td>
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

<form name="completeAppointmentForm" action="todaysappointments.do"
	method="POST"><input type="hidden" name="_method"
	value="updateAppointmentStatusAsCompleted" /> <input type="hidden"
	name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}" /> <input type="hidden"
	name="category" value="${ifn:cleanHtmlAttribute(category)}" /> <input type="hidden"
	name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}" /> <input
	type="hidden" name="date" id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' /> <input
	type="hidden" name="appointment_status" value=""> <input
	type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}" /> <input
	type="hidden" name="appointment_id"></form>

<form name="editResourceTimingsForm" method="POST" action="${schAction}.do"><input type="hidden"
	name="method" value="editResourceTimings" /> <input type="hidden"
	name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}" /> <input type="hidden"
	name="_screenName" value="schedulerScreen" /> <input type="hidden"
	name="res_sch_name" value="" /> <input type="hidden"
	name="res_sch_type" value="" /> <input type="hidden" name="_col_date"
	value="" /> <input type="hidden" name="includeResources"
	value="${ifn:cleanHtmlAttribute(includeResources)}" /> <input type="hidden" name="date"
	id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' /> <input
	type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}" /> <input
	type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />
</form>

<form name="nonAvailableForm" method="POST" action="${schAction}.do"><input type="hidden"
	name="includeResources" value="${ifn:cleanHtmlAttribute(includeResources)}" /><input type="hidden"
	name="screenmethod" value="${ifn:cleanHtmlAttribute(param.method)}" /><input
	type="hidden" name="date" id="name1"
	value='<fmt:formatDate pattern="dd-MM-yyyy" value="${date}"/>' /> <input
	type="hidden" name="method" value="markResourceNonAvailable" /> <input
	type="hidden" id="nonAvailabilityDialogId" value="" /> <input
	type="hidden" name="department" value="${ifn:cleanHtmlAttribute(department)}" /> <input
	type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}" /> <input
	type="hidden" name="centerId" id="centerId" value="${ifn:cleanHtmlAttribute(param.centerId)}" />

<div id="nonAvailabilityDialog" style="visibility:hidden">
<div class="bd">
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel" id="dialogTitle"><insta:ltext key="patient.resourcescheduler.schedulerdayview.comment"/></legend>
<table class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.resourcename"/></td>
		<td class="forminfo"><label id="nonAvailableDoctorLbl"></label> <input
			type="hidden" name="nonAvailableDoctor"></td>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.date"/></td>
		<td class="forminfo"><label id="nonAvailableDateLbl"></label></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.fromtime"/></td>
		<td class="forminfo"><input type="text" name="firstSlotFromTime"
			id="firstSlotFromTime" class="timefield" maxlength="5" readonly>
		</td>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.totime"/></td>
		<td class="forminfo"><input type="text" name="firstSlotToTime"
			id="firstSlotToTime" class="timefield" maxlength="5"
			onchange="getCompleteTime(this)"></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerdayview.remarks"/>:</td>
		<td class="forminfo" colspan="3"><textarea rows="4" cols="32"
			name="remarks"></textarea></td>
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
		<td align="left"><a
			href="${cpath}/schedulerAppointments/auditlog/AuditLogSearch.do?_method=getSearchScreen"><insta:ltext key="patient.resourcescheduler.schedulerdayview.auditlog"/></td>
		<td align="right"><insta:selectdb name="printType"
			table="printer_definition" id="printType" valuecol="printer_id"
			displaycol="printer_definition_name" value="" /></td>
		<td align="left" width="78px">&nbsp;</td>
	</tr>
</table>
</div>
<div class="legend">
<table class="legend">
	<tr>
		<td style="padding:0px 5px 0px 5px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.appointmentstatus"/>&nbsp;</td>
		<td class="flag"><img src='${cpath}/images/blue_flag.gif'></td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.confirmed"/></td>
		<td class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.notconfirmed"/></td>
		<td class="flag"><img src='${cpath}/images/black_flag.gif'></td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.arrived"/></td>
		<td class="flag"><img src='${cpath}/images/red_flag.gif'></td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.cancelled"/></td>
		<td class="flag"><img src='${cpath}/images/grey_flag.gif'></td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.noshow"/></td>
		<td class="flag"><img src='${cpath}/images/green_flag.gif'></td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.completed"/></td>
		<td class="flag"><img src='${cpath}/images/brown_flag.gif'></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td style="padding:0px 5px 0px 5px;text-align: right;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.slotavailability"/>&nbsp;</td>
		<td class="availble"
			style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.available"/></td>
		<td class="notAvailble"
			style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.notavailable"/></td>
		<td class="booked"
			style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.booked"/></td>
		<td class="overBooked"
			style="width:10px;border:1px solid;border-color:#CCCCCC;">&nbsp;</td>
		<td style="padding:0px 5px 0px 2px;"><insta:ltext key="patient.resourcescheduler.schedulerdayview.overbooked"/></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
</table>
</div>

</body>
</html>