<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>

<head>
	<title>IP Case Sheet - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function init() {
				initInfoDialog();
		}
		var doctors = '${doctors}';

		function initInfoDialog() {
			infoDialog = new YAHOO.widget.Overlay("infoDialog",
						{ 	context: ['', 'tr', 'bl'],
							visible:false,
			            	modal: true,
							width:"300px" } );
			infoDialog.render(document.body);
			YAHOO.util.Event.addListener('cancelInfoImg', "click", infoDialog.hide, infoDialog, true);
		}

		function showNoteDesc(index) {
			var imgEl = document.getElementById('labelHelpText'+index);
			var info = document.getElementsByName("note")[index].value;
			showInfoDialog(imgEl, info);
		}

		function showInfoDialog(contextEl, text) {
			infoDialog.cfg.setProperty("context", [contextEl, 'tl', 'bl'], false);
			document.getElementById('infoDialogText').textContent = text;
			infoDialog.render(document.body);
			infoDialog.show();
		}
	</script>
	<style>
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			padding-bottom: 20px;
		}
		table.wardactivities td {
			height: 20px;
			padding: 0px 5px;
			white-space: nowrap;
			text-align: left;
		}
		table.wardactivities th {
			text-align: left;
		}
		.info-overlay {
			color:#000;
			border-color: #D3D9E0 #AFB4BA #AFB4BA;
			padding: 6px;
			border-width:1px;
			border-style:solid;
			background-color: #F1F8FF;
			margin:10px;
		}
	</style>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();">
	<h1>IP Case Sheet</h1>
	<c:set var="patdischarge" value="${urlRightsMap.pat_discharge}" />
	<insta:feedback-panel/>
	<c:choose >
		<c:when test="${not empty param.visit_id}">
		  <insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>

			<form name="patientsummary" action="PatientSummary.do" method="POST">
				<fieldset class="fieldSetBorder" style="width: 457px;float: left">
					<legend class="fieldSetLabel">
					<c:choose>
						<c:when test="${docrights eq 'yes'}">
							<insta:screenlink screenId="doctors_note" extraParam="?_method=doctorsnotescreen&visit_id=${param.visit_id}"
										label="Doctor's Notes" addPipe="false" style="margin-left: 10px"/>
						</c:when>
						<c:otherwise>
							Doctor's Notes
						</c:otherwise>
					</c:choose>
					</legend>
					<div style="float:left; height: 205px; width: 457px; ">
						<div style="height: 180px">
							<insta:paginate curPage="${doctorsnote.pageNumber}" numPages="${doctorsnote.numPages}" totalRecords="${doctorsnote.totalRecords}"
								pageNumParam="doctorsNotePageNum"/>
							<table style="margin-left: 5px;" width="457px" class="wardactivities">
								<c:forEach items="${doctorsnote.dtoList}" var="doctorsnote" varStatus="st">
									<tr>
										<fmt:formatDate pattern="dd-MM-yyyy" value="${doctorsnote.map.creation_datetime}" var="readingDate"/>
										<fmt:formatDate pattern="HH:mm" value="${doctorsnote.map.creation_datetime}" var="readingTime"/>
										<td >
											<font style="font-weight: bold">
												${readingDate} | ${readingTime} | <label>${doctorsnote.map.doctor_name}</label>
											</font>
										</td>
									</tr>
									<tr>
										<td >
											<c:set var="notes" value="${fn:escapeXml(doctorsnote.map.notes)}"/>
											<input type="hidden" name="note" id="note${st.index}" value="${notes}"/>
											<c:choose>
												<c:when test="${fn:length(notes) gt 65}">
													<label id="labelHelpText${st.index}" onclick="javascript:showNoteDesc(${st.index})">
														<c:out value="${ifn:breakAfterNumChar(fn:substring(notes, 0, 122-2), 65)}..." escapeXml="false" />
													</label>
												</c:when>
												<c:otherwise>
													<label id="labelHelpText${st.index}" onclick="javascript:showNoteDesc(${st.index})">
														<c:out value="${ifn:breakContent(notes)}" escapeXml="false" />
													</label>
												</c:otherwise>
											</c:choose>
										</td>
									</tr>
								</c:forEach>
							</table>
						</div>
						<div style="clear: both"></div>
					</div>
				</fieldset>
				<fieldset class="fieldSetBorder" style="width: 457px;float: left; margin-left: 10px">
					<fmt:formatDate pattern="dd-MM-yyyy" var="current_date" value="<%= new java.util.Date()%>"/>
					<legend class="fieldSetLabel">
						<c:choose>
							<c:when test="${activityrights eq 'yes'}">
								<insta:screenlink screenId="activities_list" extraParam="?_method=list&patient_id=${patient.patient_id}"
										label="Today's Ward Activities" addPipe="false" style="margin-left: 10px"/>
							</c:when>
							<c:otherwise>
								Today's Ward Activities
							</c:otherwise>
						</c:choose>
					 | ${current_date}</legend>
					<div style="float:left; height: 205px; width: 457px; ">
						<div style="height: 180px">
							<insta:paginate curPage="${activities.pageNumber}" numPages="${activities.numPages}" totalRecords="${activities.totalRecords}"
								pageNumParam="activitiesPageNum"/>
							<table style="margin-left: 5px;" width="100%" class="wardactivities">
								<tr>
									<th width="30px">Time</th>
									<th width="80px">Type</th>
									<th>Item</th>
									<th>Remarks</th>
								</tr>
								<c:forEach items="${activities.dtoList}" var="activity">
									<tr>
										<fmt:formatDate pattern="HH:mm" var="due_time" value="${activity.map.due_date}"/>
										<c:choose>
											<c:when test="${activity.map.activity_type == 'G'}">
												<c:set var="type" value="General Activity"/>
											</c:when>
											<c:when test="${activity.map.prescription_type == 'M'}">
												<c:set var="type" value="Medicine"/>
											</c:when>
											<c:when test="${activity.map.prescription_type == 'I'}">
												<c:set var="type" value="Inv."/>
											</c:when>
											<c:when test="${activity.map.prescription_type == 'S'}">
												<c:set var="type" value="Service"/>
											</c:when>
											<c:when test="${activity.map.prescription_type == 'C'}">
												<c:set var="type" value="Consultation"/>
											</c:when>
											<c:when test="${activity.map.prescription_type == 'O'}">
												<c:set var="type" value="Others"/>
											</c:when>
										</c:choose>
										<td>${due_time}</td>
										<td>${type}</td>
										<td><insta:truncLabel length="22" value="${activity.map.item_name}"/></td>
										<c:set var="activity_remarks_value" value=""/>
										<c:choose>
											<c:when test="${not empty activity.map.activity_remarks and activity.map.activity_remarks != ''}">
												<c:set var="activity_remarks_value" value="${activity.map.activity_remarks}"/>
											</c:when>
											<c:otherwise>
												<c:set var="activity_remarks_value" value="${activity.map.remarks}"/>
											</c:otherwise>
										</c:choose>
										<td><insta:truncLabel length="20" value="${activity_remarks_value}"/></td>
									</tr>
								</c:forEach>
							</table>
						</div>
						<div style="clear: both"></div>
					</div>
				</fieldset>
				<fieldset class="fieldSetBorder" style="width: 457px;float: left">
					<legend class="fieldSetLabel">
						<c:choose>
							<c:when test="${nursrights eq 'yes'}">
								<insta:screenlink screenId="nurse_note" extraParam="?_method=nursenotescreen&visit_id=${patient.patient_id}"
										label="Nurse's Notes" addPipe="false" style="margin-left: 10px"/>
							</c:when>
							<c:otherwise>
								Nurse's Notes
							</c:otherwise>
						</c:choose>
					</legend>
					<div style="float:left; height: 205px; width: 457px; ">
						<div style="height: 180px">
						<insta:paginate curPage="${nursenote.pageNumber}" numPages="${nursenote.numPages}" totalRecords="${nursenote.totalRecords}"
								pageNumParam="nurseNotePageNum"/>
						<table style="margin-left: 5px;" width="457px" class="wardactivities">
								<c:forEach items="${nursenote.dtoList}" var="nursenote">
									<tr>
										<fmt:formatDate pattern="dd-MM-yyyy" value="${nursenote.map.creation_datetime}" var="readingDate"/>
										<fmt:formatDate pattern="HH:mm" value="${nursenote.map.creation_datetime}" var="readingTime"/>
										<td >
											<font style="font-weight: bold">
												${readingDate} | ${readingTime} | <label>${nursenote.map.mod_user}</label>
											</font>
										</td>
									</tr>
									<tr>
										<td >
											<c:set var="notes" value="${fn:escapeXml(nursenote.map.notes)}"/>
											<c:choose>
												<c:when test="${fn:length(notes) gt 65}">
													<label >
														<c:out value="${ifn:breakAfterNumChar(fn:substring(notes, 0, 122-2), 65)}..." escapeXml="false" />
													</label>
												</c:when>
												<c:otherwise>
													<label >
														<c:out value="${ifn:breakContent(notes)}" escapeXml="false" />
													</label>
												</c:otherwise>
											</c:choose>
										</td>
									</tr>
								</c:forEach>
							</table>
						</div>
						<div style="clear: both"></div>
					</div>
				</fieldset>
				<fieldset class="fieldSetBorder" style="width: 457px;float: left; margin-left: 10px">
					<legend class="fieldSetLabel">
						<c:choose>
							<c:when test="${urlRightsMap.vital_measurements == 'A'}">
								<insta:screenlink screenId="vital_measurements" extraParam="?method=list&patient_id=${param.visit_id}&vifromDate=today"
									label="Vitals" addPipe="false"/>
							</c:when>
							<c:otherwise>
								Vitals
							</c:otherwise>
						</c:choose>
					</legend>
					<div style="float:left; height: 205px; width: 457px; ">
						<div style="height: 72px">
						<insta:paginate curPage="${vitalsDetails.pageNumber}" numPages="${vitalsDetails.numPages}" totalRecords="${vitalsDetails.totalRecords}"
								pageNumParam="vitalsPageNum"/>
						<table style="margin-left: 5px;" width="100%" class="wardactivities">
							<tr>
								<th>Time</th>
								<c:forEach var="columnBean" begin="0" end="5" items="${vitalsFields}">
									<th>
										<insta:truncLabel value="${columnBean.map.param_label}" length="10"/>
									</th>
								</c:forEach>
							</tr>
							<c:forEach items="${vitalsDetails.dtoList}" var="readings">
									<tr>
										<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${readings.map.date_time}" var="readingDate"/>
										<td>${readingDate}</td>
										<c:forEach var="columnBean" begin="0" end="5" items="${vitalsFields}">
											<td>
												<c:forEach items="${readingsMap[readings.map.vital_reading_id]}" var="record">
														<c:if test="${columnBean.map.param_id == record.map.param_id}">
														<c:set var="prefColor" value=""/>
															<c:forEach items="${referenceRanges}" var="referenceRange" >
																<c:if test="${not empty referenceRange}">
																<c:if test="${record.map.param_id eq referenceRange.map.param_id}">
																	<c:set var="referenceBean" value="${referenceRange}"/>
																	<c:catch>
																	<c:choose>
																		<c:when test="${empty record.map.param_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.normal_color_code}"/>
																		</c:when>

																		<c:when test="${(not empty referenceBean.map.max_improbable_value) && record.map.param_value > referenceBean.map.max_improbable_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.improbable_color_code}"/>
																		</c:when>

																		<c:when test="${not empty referenceBean.map.max_critical_value && record.map.param_value > referenceBean.map.max_critical_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.critical_color_code}"/>
																		</c:when>

																		<c:when test="${not empty referenceBean.map.max_normal_value && record.map.param_value > referenceBean.map.max_normal_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.abnormal_color_code}"/>
																		</c:when>

																		<c:when test="${not empty referenceBean.map.min_improbable_value && record.map.param_value < referenceBean.map.min_improbable_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.improbable_color_code}"/>
																		</c:when>

																		<c:when test="${not empty referenceBean.map.min_critical_value && record.map.param_value < referenceBean.map.min_critical_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.critical_color_code}"/>
																		</c:when>

																		<c:when test="${not empty referenceBean.map.min_normal_value && record.map.param_value < referenceBean.map.min_normal_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.abnormal_color_code}"/>
																		</c:when>

																		<c:when test="${not empty referenceBean.map.min_normal_value && not empty referenceBean.map.max_normal_value}">
																			<c:set var="prefColor" value="${prefColorCodes.map.normal_color_code}"/>
																		</c:when>

																		<c:otherwise>
																			<c:set var="prefColor" value="${prefColorCodes.map.normal_color_code}"/>
																		</c:otherwise>
																	</c:choose>
																	</c:catch>
																</c:if>
																</c:if>
															</c:forEach>
															<c:set var="color" value="${not empty prefColor ? (prefColor eq prefColorCodes.map.normal_color_code ? 'grey' : prefColor) : 'grey'}"/>
															<label style="color: ${color};  font-weight: ${color eq 'grey' ? '' : 'bold'}">${record.map.param_value}</label>
														</c:if>
												</c:forEach>
											</td>
										</c:forEach>
									</tr>
							</c:forEach>
						</table>
						</div>
						<div style="clear: both"></div>
					</div>
				</fieldset>
				<div style="margin-top: 10px">
					<insta:screenlink screenId="visit_summary" extraParam="?_method=list&patient_id=${patient.patient_id}"
						label="IP Record" />
					<insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}"
						target="_blank" label="Patient EMR Search" addPipe="true" />
					<insta:screenlink screenId="visit_emr_screen" extraParam="?_method=list&visit_id=${patient.patient_id}"
						target="_blank" label="Visit EMR Search" addPipe="true"/>
					<insta:screenlink screenId="ip_diet_prescribe" extraParam="?_method=getPrescriptionScreen&patient_id=${patient.patient_id}"
						target="_blank" label="Prescribe Diet" addPipe="true"/>
					<insta:screenlink screenId="ope_scheduler" extraParam="?method=getScheduleDetails"
						target="_blank" label="OT Scheduler" addPipe="true"/>
					| <a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/> </a>
					<insta:screenlink screenId="medication_chart" extraParam="?_method=list&visit_id=${patient.patient_id}"
									label="Medication Chart" addPipe="true" />
					<insta:screenlink screenId="intake_output" extraParam="?method=list&patient_id=${param.visit_id}&vifromDate=today"
										label="Intake/Output" addPipe="true"/>
					<c:if test="${preferences.modulesActivatedMap['mod_advanced_ot'] eq 'Y'}">
						<insta:screenlink screenId="get_planned_operations_list" extraParam="?_method=getPatientOperations&visit_id=${patient.patient_id}&mr_no=${patient.mr_no}"
								label="Operations" addPipe="true"/>
						<insta:screenlink screenId="operation_detailed_screen" extraParam="?_method=getOperationDetailedScreen&visit_id=${patient.patient_id}&visitId=${patient.patient_id}&mr_no=${patient.mr_no}"
								label="New Operation" addPipe="true"/>
					</c:if>
					<c:if test="${patdischarge == 'A'}">
						<insta:screenlink screenId="pat_discharge" extraParam="?_method=getDischargeDetails&patientid=${patient.patient_id}"
									label="Discharge" addPipe="true" />
					</c:if>
				</div>
			</form>
		</c:when>
	</c:choose>
	<div id="infoDialog" class="info-overlay">
		<div class="bd">
			<table width="100%">
				<tr>
					<td>
						<label id="infoDialogText" style="float: left"></label>
					</td>
					<td style="width: 12px;" valign="top">
						<a id="cancelInfoImg" href="javascript:void(0);">
							<img src="${cpath}/images/cancel1.png" style="width: 15px;" />
						</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="div-shadow" id="shadowDiv"></div>
	</div>
</body>
</html>