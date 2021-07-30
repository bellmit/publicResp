<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>

<head>
	<title>Surgery / Procedure Management - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body>
	<h1>Surgery / Procedure Management</h1>
	 <insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>
	 <form name="otManagementForm" action="OtManagement.do" method="POST">
	 	<table>
	 		<tr>
	 			<td valign="top" style="height: 330px;">
					<fieldset class="fieldSetBorder" style="width: 457px;float: left;box-sizing: content-box">
						<legend class="fieldSetLabel">
							<c:choose>
								<c:when test="${operationdetailedscreen}">
										<insta:screenlink screenId="operation_detailed_screen" extraParam="?_method=getOperationDetailedScreen&visitId=${patient.patient_id}&prescribed_id=${prescribed_id}&mr_no=${patient.mr_no}&operation_details_id=${param.operation_details_id}"
											 label="Surgery / Procedure" addPipe="false" />
								</c:when>
								<c:otherwise>
									Operation
								</c:otherwise>
							</c:choose>
						</legend>
						<div style="float:left; height: 300px; width: 457px; ">
							<div style="height: 280px">
								<table style="margin-left: 5px;" width="457px" class="formtable">
									<tr>
										<td class="formlabel" style="width: 80px;">Theatre/Room:</td>
										<td style="font-weight:bold" >${operationDetails.map.theatre_name}</td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Primary Procedure:</td>
										<td style="font-weight:bold" ><insta:truncLabel value="${operationDetails.map.operation_name}" length="30"/></td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Secondary Procedures:</td>
										<td style="font-weight:bold" ><insta:truncLabel value="${secondaryOpeartions}" length="30"/></td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Primary Surgeon/Doctor:</td>
										<td style="font-weight:bold" >${surgeonDetails.map.surgeon}</td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Primary Anaesthetist:</td>
										<td style="font-weight:bold" >${anesteatistDetails.map.anaesthetist}</td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Anaesthesia:</td>
										<td style="font-weight:bold">
											<insta:truncLabel value="${anathesia_type_details}" length="30"/>
										</td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Surgery/Procedure Start:</td>
										<td style="font-weight:bold" >
											<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${operationDetails.map.surgery_start}" var="surgeryStartDateAndTime"/>
											${surgeryStartDateAndTime}
										</td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">End:</td>
										<td style="font-weight:bold" >
											<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${operationDetails.map.surgery_end}" var="surgeryEndDateAndTime"/>
											 ${surgeryEndDateAndTime}
										</td>
									</tr>
									<tr>
										<td class="formlabel" style="width: 80px;">Billing Status:</td>
										<td style="font-weight:bold" >
											${billingStatus}
										</td>
									</tr>
								</table>
							</div>
							<div style="clear: both"></div>
						</div>
					</fieldset>
				</td>
				<td rowspan="3" valign="top">
					<fieldset class="fieldSetBorder" style="width: 457px;float: left;margin-left: 10px">
						<legend class="fieldSetLabel">
							<c:choose>
								<c:when test="${not empty param.operation_details_id && otrecordrights eq 'yes'}">
									<insta:screenlink screenId="ot_record" extraParam="?_method=getOperationsList&visit_id=${patient.patient_id}&operation_details_id=${param.operation_details_id}"
												label="Surgery / Procedure Forms" addPipe="false" style="margin-left: 10px"/>
								</c:when>
								<c:otherwise>
									Operations List
								</c:otherwise>
							</c:choose>
						</legend>
						<c:set var="len" value="${fn:length(otRecordForms.dtoList)}"/>
						<div style="float:left; ${ len<20 ? 'height: 655px' : ''}; width: 457px; ">
							<div style="${ len<20 ? 'height: 655px' : ''};">
								<insta:paginate curPage="${otRecordForms.pageNumber}" numPages="${otRecordForms.numPages}" totalRecords="${otRecordForms.totalRecords}" />
									<table style="margin-left: 5px;" width="457px" class="formtable">
										<c:set var="opProcId" value=""/>
										<c:forEach items="${otRecordForms.dtoList}" var="otRecordForm" varStatus="st">
											<c:set var="curOpProcId" value="${otRecordForm.map.operation_proc_id}"/>
											<tr>
												<td>
													<c:if test="${curOpProcId != opProcId }">
														<label style="font-weight: normal;">
															<insta:truncLabel value="${otRecordForm.map.operation_name}" length="30"/>
														</label>
														<c:set var="opProcId" value="${curOpProcId}"/>
													</c:if>
												</td>
												<td>
												<c:set var="sectionid" value="${fn:trim(otRecordForm.map.section_id)}" />
													<c:if test="${sectionid > 0}">
														<c:forEach items="${physician_forms}" var="patient_form_group" varStatus="j">
															<c:if test="${patient_form_group.map.section_id == sectionid}" >
																	<font style="font-weight: bold">${patient_form_group.map.section_title}</font>
															</c:if>
														</c:forEach>
													</c:if>
												</td>
											</tr>
										</c:forEach>
									</table>
							</div>
							<div style="clear: both"></div>
						</div>
					</fieldset>
				</td>
			</tr>
			<tr>
				<td colspan="2" valign="top" style="height: 150px;">
					<fieldset class="fieldSetBorder" style="width: 457px;float: left">
					<legend class="fieldSetLabel">
						<c:choose>
							<c:when test="${patientIssueRights}">
								<insta:screenlink screenId="patient_inventory_issue" extraParam="/add.htm?store=0&visit_id=${param.visit_id}
									&fromOTScreen=Y&operation_details_id=${param.operation_details_id}&visitIdForOT=${param.visit_id}"
											label="Medication/Inv Issue" addPipe="false" style="margin-left: 10px"/>
							</c:when>
							<c:otherwise>
								Medication/Inv Issue
							</c:otherwise>
						</c:choose>
					</legend>
					<div style="float:left; height: 150px; width: 457px; ">
						<div style="height: 100px">
							<insta:paginate curPage="${patientIssueDetails.pageNumber}" numPages="${patientIssueDetails.numPages}"
								totalRecords="${patientIssueDetails.totalRecords}" pageNumParam="issueDetailsPageNum"/>
								<table style="margin-left: 5px;" width="100%" class="formtable">
									<tr>
										<th align = "left">Medicine</th>
										<th align = "left">Quantity</th>
									</tr>
									<c:forEach items="${patientIssueDetails.dtoList}" var="issue">
											<tr>
												<td><insta:truncLabel value="${issue.map.medicine_name}" length="30"/></td>
												<td class="number">${issue.map.qty}</td>
											</tr>
									</c:forEach>
								</table>
							</div>
						<div style="clear: both"></div>
					</div>
				</fieldset>
				</td>
			</tr>
			<tr>
				<td colspan="2" valign="top">
					<fieldset class="fieldSetBorder" style="width: 457px;float: left">
							<legend class="fieldSetLabel">
								<c:choose>
									<c:when test="${equipOrderRights}">
										<insta:screenlink screenId="equipment_order" extraParam="?_method=getOrders&patient_id=${param.visit_id}
											&operation_details_id=${param.operation_details_id}&fromOTScreen=Y"
													label="Equipment Order" addPipe="false" style="margin-left: 10px"/>
									</c:when>
									<c:otherwise>
										Equipment Order
									</c:otherwise>
								</c:choose>
							</legend>
							<div style="float:left; height: 150px; width: 457px; ">
								<div style="height: 100px">
									<insta:paginate curPage="${equipDetails.pageNumber}" numPages="${equipDetails.numPages}"
									totalRecords="${equipDetails.totalRecords}" pageNumParam="equipDetailsPageNum"/>
									<table style="margin-left: 5px;" width="100%" class="formtable">
										<tr>
											<th align = "left">Equipment Name</th>
											<th align = "left">Used From</th>
											<th align = "left">Used Till</th>
										</tr>
										<c:forEach items="${equipDetails.dtoList}" var="equip">
												<tr>
													<td align="left"><insta:truncLabel value="${equip.map.equipment_name}" length="30"/></td>
													<td align="left">${equip.map.used_from}</td>
													<td align="left">${equip.map.used_till}</td>
												</tr>
										</c:forEach>
									</table>
								</div>
								<div style="clear: both"></div>
							</div>
						</fieldset>
					</td>
				</tr>
		</table>
		<div style="margin-top: 10px">
			<insta:screenlink screenId="get_planned_operations_list" extraParam="?_method=getPlannedOperationsList&sortOrder=mr_no&operation_status=P"
						target="_blank" label="Planned Surgeries/Procedures" addPipe="false"/>
			<insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}"
				target="_blank" label="Patient EMR" addPipe="true" />
			<insta:screenlink screenId="visit_emr_screen" extraParam="?_method=list&visit_id=${patient.patient_id}"
				target="_blank" label="Visit EMR" addPipe="true"/>
			<insta:screenlink screenId="cssd_surgery_kit_avbl" extraParam="?_method=list&appointment_date=today&appointment_date=today&issue_status=N"
				target="_blank" label="Surgery/Procedure Kit Availability" addPipe="true"/>
			<insta:screenlink screenId="cssd_ot_stock_consumption" extraParam="?_method=list&date=today&mr_no=${patient.mr_no}"
				target="_blank" label="Surgery/Procedure Stock Consumption" addPipe="true"/>
			<c:if test="${patient.visit_type eq 'i'}">
				| <a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/>
				</a>
			</c:if>
			<insta:screenlink screenId="ope_scheduler" extraParam="?method=getScheduleDetails"
				target="_blank" label="Surgery/Procedure Scheduler" addPipe="true"/>
			<c:set var="params" value="mrno=${patient.mr_no}&mr_no=${patient.mr_no}&patientId=${patient.patient_id}
						&patient_id=${patient.patient_id}&visit_id=${patient.patient_id}&
						patientid=${patient.patient_id}&visitId=${patient.patient_id}&
						visit_type=${patient.visit_type}&orgid=${patient.org_id}&
						gender=${patient.patient_gender}&dept=${patient.dept_id}&
						isbaby=${patient.isbaby}&age=${patient.age_text}&vifromDate=today" />
						<br>
			<insta:screenlink screenId="stores_patient_indent_add" extraParam="?_method=addshow&${params}"
					label="Patient Indent" addPipe="false"/>
			<c:if test="${patient.visit_type eq 'i'}">
				<insta:screenlink screenId="medication_chart" extraParam="?_method=list&visit_id=${patient.patient_id}"
							label="Medication Chart" addPipe="true" />
			</c:if>
			<insta:screenlink screenId="vital_measurements" extraParam="?method=list&patient_id=${patient.patient_id}&vifromDate=today"
					label="Vitals" addPipe="true"/>
			<c:if test="${urlRightsMap.conduct_or_add_operation_doc == 'A' &&  operationDetails.map.added_to_bill == 'Y'}">
				<insta:screenlink screenId="conduct_or_add_operation_doc"
					extraParam="?_method=searchOperationDocuments&prescription_id=${prescribed_id}&prescribed_id=${prescribed_id}
						&operation_details_id=${param.operation_details_id}&visitId=${param.visit_id}"
						label="Add/Edit OT Docs " addPipe="true" />
			</c:if>

		</div>
	 </form>
</body>
</html>