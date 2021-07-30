<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:useBean id="serverdate" class="java.util.Date"/>

<html>
<head>
	<title><insta:ltext key="registration.beddetails.details.beddetails.instahms"/></title>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="ipservices/ipservices.js" />
	<insta:link type="script" file="ipservices/bed.js" />
	<insta:link type="script" file="hmsvalidation.js"/>

	<script type="text/javascript">
		var daycare = '${ifn:cleanJavaScript(admissiondetails.daycare)}';
		var daycarehrs = '${ip_preferences.max_daycare_hours}';
		var da = 'formatDate( new Date(<%= (new java.util.Date()).getTime() %>))';
		var okto = '<%= request.getAttribute("oktodis") %>';
		var allowBackDate = '${actionRightsMap.allow_backdate}';
		var roleId = '${roleId}';
		var admitDate = '${ifn:cleanJavaScript(admissiondetails.startdate)}';
		var dutyDoctroSelectionFor = '${ip_preferences.duty_doctor_selection}';
		var nonICUBedTypes = ${nonICUBedTypes};
		var forceRemarks = '${ip_preferences.force_remarks}';
		var cpath="${cpath}";
		var bedTypes = JSON.parse('${ifn:cleanJavaScript(bedTypes)}');
		var canSetChargedBedType = '${actionRightsMap.set_charged_bed_type }' == 'A' || roleId == 1 || roleId == 2;
		var visitType = '${patientvisitdetails.map.visit_type}';
		var visitTotalPatientDue = '${visitTotalPatientDue}';
		var ipCreditLimitAmount = '${patientvisitdetails.map.ip_credit_limit_amount}';
		var ip_credit_limit_rule = '${ip_credit_limit_rule}';
		var creditLimitDetailsJSON = <%=request.getAttribute("creditLimitDetailsJSON")%>;
	</script>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="adt.beddetailsdate"/>
	<insta:js-bundle prefix="billing.billlist"/>
</head>

<c:set var="date" value="${serverdate}"/>
<c:set var="finalized" value="disabled"></c:set>
<c:forEach var="occupiedbeds" items="${prvbeds}" varStatus="loop">
	<c:if test="${occupiedbeds.map.bed_state == 'O'}">
		<c:set var="finalized" value=""></c:set>
	</c:if>
</c:forEach>
<c:set var="backDateNotAllowed" value="${actionRightsMap.allow_backdate ne 'A' && (roleId != 1 && roleId != 2) ?'readonly' : '' }"/>
<c:url var="adtURL" value="/pages/ipservices/adt.do?_method=getADTScreen"/>
<c:url var="bedviewURL" value="/pages/ipservices/BedView.do?_method=getBedView"/>
<c:url var="regDocsURL" value="/pages/RegistrationDocuments.do?_method=searchPatientGeneralDocuments&mr_no=${patientvisitdetails.map.mr_no}&patient_id=${patientvisitdetails.map.patient_id}"/>
<c:set var="patdischarge" value="${urlRightsMap.pat_discharge}" />
<c:choose>
	<c:when test="${empty patientvisitdetails.map.alloc_bed_name || patientvisitdetails.map.alloc_bed_name eq null}">
		<c:set var="shift" value="none"></c:set>
	</c:when>
	<c:otherwise>
		<c:set var="shift" value="block"></c:set>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${not empty patientvisitdetails.map.alloc_bed_name}">
		<c:set var="allocate" value="none"></c:set>
	</c:when>
	<c:otherwise>
		<c:set var="allocate" value="block"></c:set>
	</c:otherwise>
</c:choose>
<c:if test="${ip_preferences.bed_charge_posting == 'daily_update'}">
	<c:set var="estimated_days" value="1"/>
	<c:set var="estimate" value="readonly"/>
</c:if>
<c:if test="${ip_preferences.bed_charge_posting == 'actual_elapsed'}">
	<c:set var="estimated_days" value="0"/>
	<c:set var="estimate" value="readonly"/>
</c:if>
<c:if test="${admissiondetails.daycare == 'N'}">
	<c:set var="converttoip" value="disabled"/>
</c:if>


<body onload="init();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="bedView">
	<insta:ltext key="registration.beddetails.details.bedview"/>
</c:set>
<c:set var="save">
	<insta:ltext key="registration.beddetails.details.save"/>
</c:set>
<c:set var="bedShift">
	<insta:ltext key="registration.beddetails.details.bedshift"/>
</c:set>
<c:set var="discharge">
	<insta:ltext key="registration.beddetails.details.discharge"/>
</c:set>
<c:set var="bill">
	<insta:ltext key="registration.beddetails.details.bill"/>
</c:set>
<h1><insta:ltext key="registration.beddetails.details.beddetails"/></h1>
<insta:feedback-panel/>

<c:choose>
	<c:when test="${admissiondetails.daycare == 'Y'}">
		<c:set var="admissiontype" value="Day Care"></c:set>
		<c:set var="admissiotype_style" value="block"></c:set>
		<c:set var="daycare" value="Y"/>
	</c:when>
	<c:otherwise>
		<c:set var="admissiontype" value=""></c:set>
		<c:set var="admissiotype_style" value="none"></c:set>
		<c:set var="daycare" value="N"/>
	</c:otherwise>
</c:choose>
<insta:patientdetails visitid="${param.patientid}" />
<fieldset class="fieldSetBorder" style="display: ${ip_preferences.bed_charge_posting == 'Estimated_duration' ? 'block' : 'none'}">
	<legend class="fieldSetLabel"><insta:ltext key="patient.header.fieldset.otherdetails"/></legend>
	<table cellspacing="0" width="100%" class="patientdetails">
		<tr>
			<td class="formlabel">${not daycare ? "Estimated Days" : "Estimated Hrs" }:</td>
			<td class="forminfo">${ifn:cleanHtml(admissiondetails.estimateddays)}&nbsp;${ifn:cleanHtml(admissiondetails.units)}</td>
			<td class="formlabel"></td>
			<td></td>
			<td class="formlabel"></td>
			<td></td>
		</tr>
	</table>
</fieldset>
<form method="POST" name="ipbedform" action="ipbeddetails.do">
<input type="hidden" name="reg_date" id="reg_date"
	   value='<fmt:formatDate value="${patientvisitdetails.map.reg_date}" pattern="dd-MM-yyyy"/>'/>
<input type="hidden" name="reg_time" id="reg_time" value="${patientvisitdetails.map.reg_time}"/>
<fmt:formatDate var="currentdate" pattern="dd-MM-yyyy" value="${serverdate }"/>
<fmt:formatDate var="currenttime" pattern="HH:mm" value="${serverdate}"/>
<input type="hidden" name="curr_date" id="curr_date" value="${currentdate}"/>
<input type="hidden" name="curr_time" id="curr_time" value="${currenttime}"/>
<input type="hidden" name="daycare" id="daycare" value="${daycare}"/>


<table width="100%" class="formtable">
	<tr>
		<td width="100%">
			<div class="resultList" style="margin: 10px 0px 5px 0px;">
				<b><insta:ltext key="registration.beddetails.details.bed"/>&nbsp;<insta:ltext key="registration.beddetails.details.details"/></b>
				<table class="detailList dialog_displayColumns" width="100%" id="releasetable">
					<tr>
						<th ><insta:ltext key="registration.beddetails.details.ward"/></th>
						<th ><insta:ltext key="registration.beddetails.details.bedname"/></th>
						<th><insta:ltext key="registration.beddetails.details.duration"/></th>
						<th><insta:ltext key="registration.beddetails.details.dutydoctor"/></th>
						<th><insta:ltext key="registration.beddetails.details.startdate"/></th>
						<th ><insta:ltext key="registration.beddetails.details.enddate"/></th>
						<th><insta:ltext key="registration.beddetails.details.admittedby"/></th>
						<th><insta:ltext key="registration.beddetails.details.remarks"/></th>
						<th style="width:14px"></th><!-- trash icon -->
						<th style="width:14px"></th><!-- edit icon -->

					</tr>
					<c:forEach var="prvbeds" items="${prvbeds}" varStatus="loop">
						<c:set var="shift" value=""></c:set>
						<c:set var="dutyDoctor" value="readOnly"/>
						<c:set var="startDate"><fmt:formatDate value="${prvbeds.map.start_date}" pattern="dd-MM-yyyy"/></c:set>
						<c:set var="startTime"><fmt:formatDate value="${prvbeds.map.start_date}" pattern="HH:mm"/></c:set>
						<c:set var="endDate"><fmt:formatDate value="${prvbeds.map.end_date}" pattern="dd-MM-yyyy"/></c:set>
						<c:set var="endTime" ><fmt:formatDate value="${prvbeds.map.end_date}" pattern="HH:mm"/></c:set>

						<c:if test="${prvbeds.map.status == 'Current Bed' || prvbeds.map.status == 'Admitted Bed'
						 	|| prvbeds.map.status == 'Retained Bed'
						 	|| (prvbeds.map.status == 'ByStander Bed' && prvbeds.map.status == 'O')}">
							<input type="hidden" name="prev_date" id="prev_date" value="${startDate }"/>
							<input type="hidden" name="prev_time" id="prev_time" value="${startTime }"/>
						</c:if>
						<c:if test="${prvbeds.map.status == 'Current Bed' || prvbeds.map.status == 'Admitted Bed'}">
								<c:set var="dutyDoctor" value=""/>
						</c:if>

						<c:set var="i" value="${loop.index + 1}"/>
						<c:choose>
							<c:when test="${prvbeds.map.status == 'Retained Bed'
												|| prvbeds.map.status == 'ByStander Bed'}">
								<c:if test="${prvbeds.map.bed_state == 'O'}">
									<c:set var="release_check" value=""/>
								</c:if>
								<c:if test="${prvbeds.map.bed_state == 'F'}">
									<c:set var="release_check" value="disabled"/>
								</c:if>
							</c:when>
							<c:otherwise>
								<c:set var="release_check" value="disabled"/>
							</c:otherwise>
						</c:choose>
						<c:set var="flagColor" value="empty"/>
						<c:if test="${prvbeds.map.bed_status == 'P'}">
							<c:set var="flagColor" value="grey"/>
						</c:if>
						<c:if test="${prvbeds.map.status == 'Retained Bed'}">
							<c:set var="flagColor" value="yellow"/>
						</c:if>

						<c:if test="${prvbeds.map.status == 'ByStander Bed'}">
							<c:set var="flagColor" value="green"/>
						</c:if>

						<c:if test="${prvbeds.map.bed_status == 'X'}">
							<c:set var="flagColor" value="red"/>
						</c:if>
						<tr id="${i}">

							<td class="label"><img src="${cpath}/images/${flagColor}_flag.gif" id="stFlag${i}" />
								${prvbeds.map.ward_name}
								<input type="hidden" name="mrno" id="mrno" value="${patientvisitdetails.map.mr_no}" />
								<input type="hidden" name="patient_id" id="patient_id" value="${patientvisitdetails.map.patient_id}" />
								<input type="hidden" name="admit_id" value="${prvbeds.map.admit_id }"/>
								<input type="hidden" name="bed_id" value="${prvbeds.map.bed_id }"/>
								<input type="hidden" name="bed_state" id="bed_state" value="${prvbeds.map.bed_state }"/>
								<input type="hidden" name="startDate" value="<fmt:formatDate value="${prvbeds.map.start_date}" pattern="dd-MM-yyyy"/>"/>
								<input type="hidden" name="startTime" value="<fmt:formatDate value="${prvbeds.map.start_date}" pattern="HH:mm"/>"/>
								<input type="hidden" name="start_date_dt" value="<fmt:formatDate value="${prvbeds.map.start_date}" pattern="dd-MM-yyyy HH:mm"/>"/>
								<input type="hidden" name="end_date_dt" value="<fmt:formatDate value="${prvbeds.map.end_date}" pattern="dd-MM-yyyy HH:mm"/>"/>
								<input type="hidden" name="endDate" value="<fmt:formatDate value="${prvbeds.map.end_date}" pattern="dd-MM-yyyy"/>"/>
								<input type="hidden" name="endTime" value="<fmt:formatDate value="${prvbeds.map.end_date}" pattern="HH:mm"/>"/>
								<input type="hidden" name="username" id="username" value="${prvbeds.map.username }"/>
								<input type="hidden" name="is_bystander" id="is_bystander" value="${prvbeds.map.is_bystander }"/>
								<input type="hidden" name="charged_bed_type" id="charged_bed_type" value="${prvbeds.map.charged_bed_type }"/>
								<input type="hidden" name="estimated_days" id="estimated_days" value="${prvbeds.map.estimated_days }"/>
								<input type="hidden" name="cancelled" value="N"/>
								<input type="hidden" name="edited" value="N">
								<input type="hidden" name="released" value="N">
								<input type="hidden" name="status" value="${prvbeds.map.bed_status }"/>
								<input type="hidden" name="duty_doctor_id" id="duty_doctor_id" value="${prvbeds.map.duty_doctor_id}"/>
								<input type="hidden" name="is_icu" value="${prvbeds.map.is_icu }"/>
							</td>
							<td class="label" id="bedName${i }">
								${prvbeds.map.bed_name}
								<c:if test="${prvbeds.map.status eq 'Retained Bed'
											|| (prvbeds.map.status == 'ByStander Bed' && prvbeds.map.bed_state == 'O')}">
									<input type="radio" name="retainbed" id="retainbed${i }"  value="${prvbeds.map.bed_id}"  onclick="getretainbedname('${prvbeds.map.bed_id}','${prvbeds.map.admit_id }' ); " checked  ${shift}/>
									<input type="hidden" name="retainbedname" id="retainbedname${i }"  value="${prvbeds.map.bed_name}" />
									<input type="hidden" name="retainbedid" id="retainbedid${i }"  value="${prvbeds.map.bed_id}" />
									<input type="hidden" name="retainbedadmitid" id="retainbedadmitid${i }"  value="${prvbeds.map.admit_id }" />
								</c:if>
							</td>
							<td>
								${prvbeds.map.days} Days ${prvbeds.map.hours} Hrs ${prvbeds.map.mins} Mins
							</td>
							<td>
								${prvbeds.map.doctor_name}
							</td>
							<td class="label">
								<fmt:formatDate value="${prvbeds.map.start_date}" pattern="dd-MM-yyyy HH:mm"/>
							</td>
							<td class="label">
								<fmt:formatDate value="${prvbeds.map.end_date}" pattern="dd-MM-yyyy HH:mm"/>
							</td>
							<td class="label">
								${prvbeds.map.admitted_by }
								<input type="hidden" name="admitted_by" id="admitted_by" value="${prvbeds.map.admitted_by }"/>
							</td>

							<td class="label">
								<insta:truncLabel value="${prvbeds.map.remarks }" length="8"/>
								<input type="hidden" name="remarks" id="remarks" value="${prvbeds.map.remarks }"/>
							</td>
							<td style="text-align: right;display: table-cell;">
								<c:choose>
									<c:when test="${prvbeds.map.bed_status ne 'X' && finalized eq ''}">
										<a href="javascript:void(0)" onclick="cancelBed(this);"
											title='<insta:ltext key="registration.beddetails.details.editbeddetails"/>' >
											<img src="${cpath}/icons/delete.gif" class="button" />
										</a>
									</c:when>
									<c:when test="${finalized ne ''}">
										<img class="imgDelete" src="${cpath}/icons/delete_disabled.gif" />
									</c:when>
									<c:otherwise>
										<img class="imgDelete" src="${cpath}/icons/delete_disabled.gif" />
									</c:otherwise>
								</c:choose>
							</td>
							<td style="text-align: center;display: table-cell;">
								<c:choose>
									<c:when test="${prvbeds.map.bed_state ne 'F'}">
										<a href="javascript:void(0)" name="btnEditBedDetails" id="btnEditBedDetails${i}"
											onclick="showEditDialog(this);"
											title='<insta:ltext key="registration.beddetails.details.editbeddetails"/>' >
											<img src="${cpath}/icons/Edit.png" class="button" />
										</a>
									</c:when>
									<c:otherwise>
										<img class="imgDelete" src="${cpath}/icons/Edit1.png" />
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</c:forEach>
			</table>
		</td>
	</tr>
	<tr height="20"></tr>
	<tr >
		<td>
			<table  width="100%" class="detailList">
				<tr>
			   		<th><insta:ltext key="registration.beddetails.details.billingaction"/></th>
			   		<th><insta:ltext key="registration.beddetails.details.currentvalue"/></th>
			   		<th><insta:ltext key="registration.beddetails.details.update.or.remarks"/></th>
			   	</tr>
			   	<tr>
			   		<td>
			   			<input type="radio" name="actions" id="action_update" value="1"  ${finalized}/>
			   			<insta:ltext key="registration.beddetails.details.bedchargeupdatedupto"/>
		   			</td>
			   		<td colspan="2"><fmt:formatDate value="${admissiondetails.lastUpdated}" pattern="dd-MM-yyyy HH:mm"/></td>
			   	</tr>
			   	<tr>
			   		<td>
				   		<input type="radio" name="actions" id="action_convert" value="2" ${converttoip } ${ finalized}/>
				   		<insta:ltext key="registration.beddetails.details.converttoipbilling"/>
			   		</td>
			   		<td colspan="2"><div style="display: ${admissiotype_style}">${admissiontype}</div></td>
			   	</tr>
			   	<tr>
			   		<td>
			   			<input type="radio" name="actions" id="action_finalze" value="3" ${finalized }/>
			   			<insta:ltext key="registration.beddetails.details.finalizebedcharges"/>
		   			</td>
			   		<td>${estimateddays}</td>
			   		<c:set var="validType" value=""/>
			   		<c:if test="${actionRightsMap.allow_backdate ne 'A' && (roleId != 1 && roleId != 2 )}">
			   			<c:set var="validType" value="future"/>
			   		</c:if>
			   		<td	>
			   			<insta:datewidget name="action_date" id="action_date" value="${currentdate}"
			   				valid="${validType }" btnPos="left" />
				   		<input type="text" id="action_time" name="action_time" class="number"
				   			value="${currenttime}" onblur="setTime(this)"/>
		   			</td>
			   	</tr>
			</table>
		</td>
	<tr>
	<tr>
		<td>
			<input type="button" name="save" id="save" value="${save}" ${multiCentered && centerId == 0 ? 'disabled' : '' } onclick="return onSave();" ${finalized }/>
			<insta:screenlink addPipe="true" screenId="bed_view" label="${bedView}" extraParam="?_method=getBedView"/>
			<insta:screenlink addPipe="true" screenId="adt" label="ADT" extraParam="?_method=getADTScreen&mr_no=${patientvisitdetails.map.mr_no}"/>
			| <a href="${regDocsURL }"><insta:ltext key="registration.beddetails.details.registrationdocuments"/></a>
			<insta:screenlink addPipe="true" screenId="credit_bill_collection" extraParam="?_method=getCreditBillingCollectScreen&billNo=${creditBill.billNo}"
					label="${bill} ${creditBill.billNo}"/>
			<c:forEach var="prvbeds" items="${prvbeds}" varStatus="loop">
				<c:if test="${(prvbeds.map.bed_state == 'O') && (prvbeds.map.bed_status == 'A'|| prvbeds.map.bed_status== 'C' )}">
					<insta:screenlink addPipe="true" screenId="ip_bedshift" extraParam="?_method=getShiftBedScreen&patientid=${patientvisitdetails.map.patient_id}" label="${bedShift}"/>
				</c:if>
			</c:forEach>
			<c:if test="${patdischarge == 'A'}">
				<insta:screenlink addPipe="true" screenId="pat_discharge" extraParam="?_method=getDischargeDetails&patientid=${patientvisitdetails.map.patient_id}" label="${discharge}"/>
			</c:if>
		</td>

	</tr>
</table>
</form>
<form name="editForm">
<input type="hidden" id="editRowId" value=""/>
<div id="editDialog" style="visibility: hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="registration.beddetails.details.editbeddetails"/></legend>
			<table cellpadding="0" cellspacing="0" width="100%" id="panel" class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="registration.beddetails.details.dutydoctor"/>:</td>
					<td>
						<insta:selectdb name="eDutyDoctor" table="doctors" valuecol="doctor_id"
							displaycol="doctor_name" dummyvalue="${dummyvalue}"  dummyvalueId=""
							orderby="doctor_name"/>
					</td>
					<td class="formlabel"><insta:ltext key="registration.beddetails.details.chargedbedtype"/>:</td>
					<td>
						<select name="eChargedBedType" id="eChargedBedType" class="dropdown" >
							<option value="">${dummyvalue}</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="registration.beddetails.details.release"/>:</td>
					<td>
						<input type="checkbox" name="eReleaseBed" id="eReleaseBed${i }"/>
					</td>
					<td class="formlabel"><insta:ltext key="registration.beddetails.details.enddate"/>:</td>
					<td valign="top">
						<insta:datewidget name="eEndDate" id="eEndDate" value="${currentdate}"
			   				valid="${validType }" btnPos="left" />
						<input type="text" name="eEndTime" id="eEndTime" class="number"
							maxlength="5" value="${currenttime}" onblur="validateTime(this),setTime(this)" />
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="registration.beddetails.details.remarks"/>:</td>
					<td>
						<input type="text" name="eRemarks" id="eRemarks" maxlength="100"/>
					</td>
				</tr>
			</table>
		</fieldset>
		<input type="button" name="add" id="add" value="Ok" onclick="onEdit();" tabindex="4"/>
		<input type="button" name="close" id="close" value="Close" onclick="closeDialog();" tabindex="5"/>
	</div>
</div>
</form>

<div class="legend">
	<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
	<div class="flagText"><insta:ltext key="registration.beddetails.details.currentbed"/></div>
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText"><insta:ltext key="registration.beddetails.details.cancelledbed"/></div>
	<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
	<div class="flagText"><insta:ltext key="registration.beddetails.details.retainedbed"/></div>
	<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
	<div class="flagText"><insta:ltext key="registration.beddetails.details.bystanderbed"/></div>
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText"><insta:ltext key="registration.beddetails.details.previousbed"/></div>
</div>
<script type="text/javascript">
	var validType = '${validType}';
</script>
</body>
</html>
