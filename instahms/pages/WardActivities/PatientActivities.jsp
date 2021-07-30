<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Patient Ward Activities</title>
	<insta:link type="js" file="wardactivities/patientactivities.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<style>
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			padding-bottom: 20px;
		}
		input.size2 {width:30px;}
	</style>
	<script>
		var cpath = '${cpath}';
		var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
		var actionId = '${actionId}';
		var users = ${users};
		var roleId = '${roleId}';
		var loggedUserName = '${ifn:cleanJavaScript(userId)}';
		var visitType = '${patbean.visit_type}';
		var ipCreditLimitAmount = '${patbean.ip_credit_limit_amount}';
		var visitTotalPatientDue = '${visitTotalPatientDue}';
		var ip_credit_limit_rule = <insta:jsString value="${ip_credit_limit_rule}"/>;
		var creditLimitDetailsJSON = <%=request.getAttribute("creditLimitDetailsJSON")%>;
	</script>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="billing.billlist"/>
	</head>

<body onload="init();">
	<h1>Patient Ward Activities</h1>
	<insta:feedback-panel/>
	<insta:patientdetails visitid="${param.patient_id}" showClinicalInfo="true"/>
	<form name="activities" action="PatientActivitiesAction.do" method="POST" autocomplete="off">
		<input type="hidden" name="_method" value="updateActivities">
		<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}">
		<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
		<input type="hidden" name="authUser" id="authUser" value=""/>
		<input type="hidden" name="patient_discharged" id="patient_discharged"
				value="${patient.visit_status == 'I' && patient.discharge_flag == 'D'}"/>
		<table class="formtable" style="margin-top: 5px">
			<tr>
				<td class="formlabel">Filter on Type: </td>
				<td><select name="filterOnType" id="filterOnType" class="dropdown" onchange="return filterActivitiesOnType(this.value);">
						<option value="">All</option>
						<option value="I">Investigation</option>
						<option value="S">Service</option>
						<option value="C">Consultation</option>
						<option value="O">Others</option>
						<option value="G">General Activity</option>
					</select>
				</td>
				<td class="formlabel">Hide Cancelled Items: </td>
				<td><input type="checkbox" name="hideCancelledItems" id="hideCancelledItems" onclick="return hideCancelled(this);" checked/></td>
				<td class="formlabel">Hide Done	 Items: </td>
				<td><input type="checkbox" name="hideCompletedItems" id="hideCompletedItems" onclick="return hideCompleted(this);" checked/></td>
			</tr>
		</table>
		<div class="resultList">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="activitiesTable" border="0" width="100%" style="margin-top: 10px;">
				<tr>
					<th width="60px">Due Date</th>
					<th>Type</th>
					<th>Item</th>
					<th>Prescribing Doc</th>
					<th>Activity No.</th>
					<th>Order</th>
					<th>Remarks</th>
					<th >Done Time</th>
					<th width="16px"></th>
				</tr>
				<c:set var="numActivities" value="${fn:length(patient_activities)}"/>
				<c:forEach begin="1" end="${numActivities+1}" var="i" varStatus="loop">
					<c:set var="activity" value="${patient_activities[i-1].map}"/>
					<c:if test="${empty activity}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<c:set var="item_name" value=""/>
					<c:set var="prescription_type_name" value=""/>
					<c:set var="activity_remarks_value" value=""/>
					<c:set var="userId" value="${userid}"/>
					<c:choose>
						<c:when test="${not empty activity.activity_remarks and activity.activity_remarks != ''}">
							<c:set var="activity_remarks_value" value="${activity.activity_remarks}"/>
						</c:when>
						<c:otherwise>
							<c:set var="activity_remarks_value" value="${activity.remarks}"/>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${activity.prescription_type == 'M'}">
							<c:set var="item_name" value="${activity.item_name}"/>
							<c:if test="${not empty activity.med_form_name}">
								<c:set var="item_name" value="${item_name}/${activity.med_form_name}"/>
							</c:if>
							<c:if test="${not empty activity.med_strength}">
								<c:set var="item_name" value="${item_name}/${activity.med_strength}"/>
							</c:if>
						</c:when>
						<c:otherwise>
							<c:set var="item_name" value="${activity.item_name}"/>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${activity.prescription_type == 'M'}">
							<c:set var="prescription_type_name" value="Medicine"/>
						</c:when>
						<c:when test="${activity.prescription_type == 'I'}">
							<c:set var="prescription_type_name" value="Inv."/>
						</c:when>
						<c:when test="${activity.prescription_type == 'S'}">
							<c:set var="prescription_type_name" value="Service"/>
						</c:when>
						<c:when test="${activity.prescription_type == 'C'}">
							<c:set var="prescription_type_name" value="Consultation"/>
						</c:when>
						<c:when test="${activity.prescription_type == 'O'}">
							<c:set var="prescription_type_name" value="Others"/>
						</c:when>
						<c:when test="${activity.activity_type == 'G'}">
							<c:set var="prescription_type_name" value="General Activity"/>
						</c:when>
					</c:choose>
					<c:set var="class_for_status" value=""/>
					<c:choose>
						<c:when test="${activity.activity_status == 'P'}">
							<c:set var="class_for_status" value="inprogress"/>
						</c:when>
						<c:when test="${activity.activity_status == 'O'}">
							<c:set var="class_for_status" value="ordered"/>
						</c:when>
						<c:when test="${activity.activity_status == 'D'}">
							<c:set var="class_for_status" value="completed"/>
						</c:when>
						<c:when test="${activity.activity_status == 'X'}">
							<c:set var="class_for_status" value="cancelled"/>
						</c:when>

					</c:choose>

					<tr ${style} class="${activity.activity_type == 'G' ? 'G' : activity.prescription_type}${' '}${class_for_status}">
						<c:choose>
							<c:when test="${activity.activity_status == 'X'}">
								<c:set var="flagColor" value="red"/>
							</c:when>
							<c:when test="${activity.activity_status == 'D'}">
								<c:set var="flagColor" value="green"/>
							</c:when>
							<c:when test="${not empty activity.order_no}">
								<c:set var="flagColor" value="blue"/>
							</c:when>
							<c:otherwise>
								<c:set var="flagColor" value="empty"/>
							</c:otherwise>
						</c:choose>
						<td>
							<fmt:formatDate var="due_datetime" value="${activity.due_date}" pattern="dd-MM-yyyy HH:mm"/>
							<fmt:formatDate var="completed_datetime" value="${activity.completed_date}" pattern="dd-MM-yyyy HH:mm"/>
							<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${activity.end_datetime}" var="end_datetime"/>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${activity.med_exp_date}" var="med_expiry_date"/>
							<fmt:formatDate var="ordered_datetime" value="${activity.ordered_datetime}" pattern="dd-MM-yyyy HH:mm"/>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							<label >${due_datetime}</label>
							<c:set var="user" value="${userid}"/>
							<input type="hidden" name="activity_id" value="${activity.activity_id}">
							<input type="hidden" name="item_name" value="${item_name}"/>
							<input type="hidden" name="activity_type" value="${activity.activity_type}"/>
							<input type="hidden" name="due_date" value="${due_datetime}"/>
							<input type="hidden" name="completed_date" value="${completed_datetime}"/>
							<input type="hidden" name="activity_remarks" value="${activity.activity_remarks}"/>
							<input type="hidden" name="activity_status" value="${activity.activity_status}"/>
							<input type="hidden" name="completed_by" value="${activity.completed_by != null ? activity.completed_by : isSharedLogIn == 'Y' ?'': user}"/>
							<input type="hidden" name="ordered_by" value="${activity.ordered_by}"/>
							<input type="hidden" name="med_batch" value="${activity.med_batch}"/>
							<input type="hidden" name="med_expiry_date" value="${med_expiry_date}"/>
							<input type="hidden" name="is_already_completed" value="${activity.activity_status == 'D'}"/>
							<input type="hidden" name="is_already_cancelled" value="${activity.activity_status == 'X'}"/>
							<input type="hidden" name="is_already_ordered" value="${not empty activity.order_no}"/>
							<input type="hidden" name="item_id" value="${activity.item_id}"/>
							<input type="hidden" name="raise_order" value="false"/>
							<input type="hidden" name="edited" value="false"/>
							<input type="hidden" name="ordered_datetime" value="${ordered_datetime}"/>
							<input type="hidden" name="order_no" value="${activity.order_no}"/>
							<!-- prescription details hidden fields -->
							<input type="hidden" name="prescription_type" value="${activity.prescription_type}"/>
							<input type="hidden" name="prescription_id" value="${activity.prescription_id}"/>
							<input type="hidden" name="med_dosage" value="${activity.med_dosage}"/>
							<input type="hidden" name="admin_strength" value="${activity.admin_strength}"/>
							<input type="hidden" name="med_strength" value="${activity.med_strength}"/>
							<input type="hidden" name="doctor_name" value="${activity.doctor_name}">
							<input type="hidden" name="presc_doctor_id" value="${activity.doctor_id}"/>
							<input type="hidden" name="recurrence_name" value="${activity.recurrence_name}"/>
							<input type="hidden" name="repeat_interval" value="${activity.repeat_interval}"/>
							<input type="hidden" name="repeat_interval_units" value="${activity.repeat_interval_units}"/>
							<input type="hidden" name="end_datetime" value="${end_datetime}"/>
							<input type="hidden" name="no_of_occurrences" value="${activity.no_of_occurrences}"/>
							<input type="hidden" name="end_on_discontinue" value="${activity.end_on_discontinue}"/>
							<input type="hidden" name="presc_remarks" value="${activity.presc_remarks}">
							<input type="hidden" name="mandate_test_additional_info" value="${activity.mandate_additional_info}"/>
							<input type="hidden" name="is_pkg" value="${activity.ispkg}"/>
						</td>
						<td>${prescription_type_name}</td>
						<td><insta:truncLabel length="30" value="${item_name}"/></td>
						<td><insta:truncLabel length="20" value="${activity.doctor_name}"/></td>
						<td><insta:truncLabel length="4" value="${activity.activity_num}"/></td>
						<td>
							<c:set var="fontColor" value="#666"/>
							<c:if test="${not empty activity.common_order_id}">
								<c:choose>
									<c:when test="${activity.prescription_type == 'I'}">
										<c:choose>
											<c:when test="${activity.test_conducted == 'C' ||
												activity.test_conducted == 'V' ||
												activity.test_conducted == 'S' ||
												activity.test_conducted == 'RC' ||
												activity.test_conducted == 'RV' ||
												activity.test_conducted == 'CRN'}"><!-- CRN is the completed status for tests with no result entry  -->
												<c:set var="fontColor" value="green" />
											</c:when>
											<c:when test="${(activity.test_conducted == 'N' || activity.test_conducted == 'NRN') && activity.sample_status == '1'}">
												<c:set var="fontColor" value="sky blue" />
											</c:when>
										</c:choose>
									</c:when>
									<c:when test="${activity.prescription_type == 'S'}">
										<c:if test ="${activity.service_conducted == 'C'}">
												<c:set var="fontColor" value="green" />
										</c:if>
									</c:when>
								</c:choose>
								<font color="${fontColor}">
									<insta:truncLabel length="10" value="${activity.common_order_id}"/>
								</font>
							</c:if>
						</td>
						<td><insta:truncLabel length="30" value="${activity_remarks_value}"/></td>
						<td>${completed_datetime}</td>
						<td style="text-align: center; ">
							<a name="_editAnchor" href="javascript:Edit" onclick="return showEditDialog(this);" title="Edit Item Details">
								<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</td>
					</tr>
				</c:forEach>
			</table>
			<table class="addButton">
				<tr>
					<td>&nbsp;</td>
					<td style="width: 16px; text-align: center">
						<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Item (Alt_Shift_+)"
							onclick="showAddDialog(this); return false;"
							accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>
				</tr>
			</table>
		</div>
		<div id="addActivityDialog" style="display: none">
			<div class="hd">Add</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Activity Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">General Activity</td>
							<td><input type="text" name="d_gen_activity_details" id="d_gen_activity_details" style="width: 600px"/></td>
						</tr>
						<tr>
							<td class="formlabel">Remarks</td>
							<td>
								<textarea name="d_activity_remarks" id="d_activity_remarks" rows="2" cols="80"></textarea>
							</td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Activity Status</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Due Date: </td>
							<td>
								<div style="display: inline">
									<fmt:formatDate pattern="HH:mm" var="current_time" value="<%=new java.util.Date()%>"/>
									<insta:datewidget name="d_due_date" id="d_due_date" value="today"/>
									<input type="text" name="d_due_time" id="d_due_time" value="${current_time}" style="width: 50px"/>
								</div>
							</td>
							<td class="formlabel">Ordered Date: </td>
							<td></td>
							<td class="formlabel">Ordered by: </td>
							<td></td>
						</tr>
						<tr>
							<td class="formlabel">Status: </td>
							<td>
								<select class="dropdown" id="d_activity_status" name="d_activity_status" onchange="setCompletedDateTime('d');">
									<option value="">-- Select --</option>
									<option value="P">In progress</option>
									<option value="D">Done</option>
									<option value="X">Cancelled</option>
								</select>
							</td>
							<td class="formlabel">Done on Date: </td>
							<td style="width: 250px">
								<div style="display: inline;">
									<insta:datewidget name="d_completed_date" id="d_completed_date"/>
									<input type="text" name="d_completed_time" id="d_completed_time" style="width: 50px"/>
								</div>
							</td>
							<td class="formlabel">Done by user: </td>
							<td>
								<c:set var="user" value="${userid}"/>
								<label id="d_done_by_label"></label>
								<input type="hidden" name="d_completed_by" id="d_completed_by" value="${isSharedLogIn == 'Y' ?'': ifn:cleanHtmlAttribute(user)}"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Order: </td>
							<td><input type="checkbox" name="d_order" id="d_order" value="Y" disabled/></td>
						</tr>
					</table>
				</fieldset>
				<table style="margin-top: 10px">
					<tr>
						<td>
							<button type="button" id="d_Add" name="d_Add" accessKey="A">
							<b><u>A</u></b>dd</button>
							<button type="button" id="d_Close" name="d_Close" accessKey="C">
							<b><u>C</u></b>lose</button>
						</td>
					</tr>
				</table>
			</div>
		</div>
		<div id="editActivityDialog" style="display:none">
			<input type="hidden" name="editRowId" id="editRowId" value="">
			<div class="hd">Edit Activity</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" >Prescription</legend>
					<table class="formtable" >
						<tr>
							<td class="formlabel"><label id="itemDisplayLabel"></label></td>
							<td colspan="5" class="forminfo"><label id="itemNameLabel"></label></td>
						</tr>
						<tr>
							<td class="formlabel">Type: </td>
							<td class="forminfo"><label id="itemTypeLabel"></label></td>
							<td class="formlabel">Presc. Doctor: </td>
							<td class="forminfo"><label id="prescDoctorLabel"></label></td>
							<td class="formlabel">Admin Strength: </td>
							<td class="forminfo"><insta:truncLabel length="15" id="adminStrengthLabel" value=""/></td>
						</tr>
						<tr>
							<td class="formlabel">Dosage: </td>
							<td class="forminfo"><label id="dosageLabel"></label></td>
							<td class="formlabel">Frequency: </td>
							<td class="forminfo"><label id="frequencyLabel"></label></td>
							<td class="formlabel">Interval: </td>
							<td class="forminfo"><label id="intervalLabel"></label></td>
						</tr>
						<tr>
							<td class="formlabel">End Date: </td>
							<td class="forminfo"><label id="endDateLabel"></label></td>
							<td class="formlabel">No. of Occ.: </td>
							<td class="forminfo"><label id="noofOccurrencesLabel"></label></td>
							<td class="formlabel">Till Discontinued: </td>
							<td class="forminfo"><label id="tillDiscontinuedLabel"></label></td>
						</tr>
						<tr>
							<td class="formlabel">Presc. Remarks</td>
							<td colspan="5" class="forminfo"><label id="prescRemarksLabel"></label></td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Activity Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Medication Batch: </td>
							<td><input type="text" name="ed_med_batch" id="ed_med_batch" value="" onchange="setEdited();"></td>
							<td class="formlabel">Expiry (MM-YY): </td>
							<td>
								<input type="text" name="ed_med_expiry_month" id="ed_med_expiry_month" maxlength="2" class="size2" onchange="setEdited();"/>
								<input type="text" name="ed_med_expiry_year" id="ed_med_expiry_year" maxlength="2" class="size2" onchange="setEdited();"/>
							</td>
							<td class="formlabel"></td>
							<td></td>
						</tr>
						<tr id="genActivityRow">
							<td class="formlabel">General Activity</td>
							<td class="forminfo"><label id="genActivityDetailsLabel"></label></td>
						</tr>
						<tr>
							<td class="formlabel">Activity Remarks</td>
							<td colspan="5">
								<input type="hidden" name="ed_prescription_type" id="ed_prescription_type" value=""/>
								<input type="hidden" name="ed_activity_type" id="ed_activity_type" value=""/>
								<textarea name="ed_activity_remarks" id="ed_activity_remarks" rows="2" cols="70" onchange="setEdited();"></textarea>
							</td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Activity Status</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Due Date: </td>
							<td>
								<div style="display: inline">
									<insta:datewidget name="ed_due_date" id="ed_due_date" extravalidation="setEdited();"/>
									<input type="text" name="ed_due_time" id="ed_due_time" style="width: 50px" onchange="setEdited();"/>
								</div>
							</td>
							<td class="formlabel">Ordered Date: </td>
							<td class="forminfo"><label id="edOrderedDateLabel"></label></td>
							<td class="formlabel">Ordered by: </td>
							<td class="forminfo"><label id="edOrderedbyLabel"></label></td>
						</tr>
						<tr>
							<td class="formlabel">Status: </td>
							<td>
								<select class="dropdown" id="ed_activity_status" name="ed_activity_status" onchange="setEdited();setCompletedDateTime('ed');">
									<option value="">-- Select --</option>
									<option value="P">In progress</option>
									<option value="D">Done</option>
									<option value="X">Cancelled</option>
								</select>
							</td>
							<td class="formlabel">Done on Date: </td>
							<td style="width: 250px">
								<div style="display: inline">
									<insta:datewidget name="ed_completed_date" id="ed_completed_date" extravalidation="setEdited()"/>
									<input type="text" name="ed_completed_time" id="ed_completed_time" style="width: 50px" onchange="setEdited();"/>
								</div>
							</td>
							<td class="formlabel">Done by user: </td>
							<td>
								<label id="ed_done_by_label"></label>
								<input type="hidden" name="ed_completed_by" id="ed_completed_by" />
							</td>
						</tr>
						<tr>
							<td class="formlabel">Order: </td>
							<td><input type="checkbox" name="ed_order" id="ed_order" value="Y" onclick="setEdited();"/></td>
						</tr>
					</table>
				</fieldset>
				<table style="margin-top: 10px">
					<tr>
						<td>
							<input type="button" name="ed_okBtn" id="ed_okBtn" value="Ok"/>
							<input type="button" name="ed_cancelBtn" id="ed_cancelBtn" value="Cancel"/>
							<input type="button" name="ed_previousBtn" id="ed_previousBtn" value="<<Previous">
							<input type="button" name="ed_nextBtn" id="ed_nextBtn" value="Next>>"/>
						</td>
					</tr>
				</table>
			</div>
		</div>
		<div id="loginDiv" style="display: none">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Login Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">User ID: </td>
							<td><input type="text" name="login_user" id="login_user"/></td>
							<td class="formlabel">&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel">Password: </td>
							<td><input type="password" name="login_password" id="login_password" onkeypress="return submitOnEnter(event);"/></td>
							<td class="formlabel">&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
					</table>
					<table style="margin-top: 10px">
						<tr>
							<td><input type="button" name="submitForm" id="submitForm" value="Submit" />
								<input type="button" name="cancelSubmit" id="cancelSubmit" value="Cancel" />
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<div style="float: left; margin-top: 10px">
			<c:if test="${not empty patient.patient_id}">
				<input type="button" name="save" id="save" value="Save" onclick="return showLoginDialog();">
				<insta:screenlink screenId="patient_activities_audit_log" label="Audit Log Search" addPipe="true" target="_blank"
					extraParam="?_method=getSearchScreen"/>
			</c:if>

		</div>
		<div style="float: right; margin-top: 10px">
			<insta:selectdb name="printerId" id="printerId" table="printer_definition" class="dropdown"
							valuecol="printer_id"  displaycol="printer_definition_name"/>
			<input type="button" name="print" value="Print" onclick="printActivities()"/>
		</div>
		<div style="clear: both"></div>
		<div class="legend" >
			<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
			<div class="flagText">Cancelled</div>
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText">Ordered</div>
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText">Done</div>
		</div>

	</form>
</body>
</html>