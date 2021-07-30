<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="org.apache.struts.Globals"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />

<c:set var="allocatebystanderbedText">
<insta:ltext key="registration.allocatebed.details.allocatebystanderbed"/>
</c:set>
<c:set var="allocatebedText">
<insta:ltext key="registration.allocatebed.details.allocatebed"/>
</c:set>
<c:set var="title" value="${isBystander ? allocatebystanderbedText : allocatebedText}"/>
<fmt:formatDate var="reg_date" value="${patient.reg_date}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="reg_time" value="${patient.reg_time}" pattern="HH:mm"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="nextDay" value="${ifn:nextDate(currentDate,1)}"/>
<fmt:formatDate var="nextDateVal" value="${nextDay}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="curDateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="curTimeVal" value="${currentDate}" pattern="HH:mm"/>


<html>
<head>
	<title>${title} - <insta:ltext key="registration.allocatebed.details.instahms"/></title>

	<meta http-equiv="Content Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="adt/allocatebed.js" />
	<insta:link type="script" file="hmsvalidation.js" />

	<script type="text/javascript">
		var cpath="${cpath}";
		var allowBackDate = '${actionRightsMap.allow_backdate}';
		var roleId = '${roleId}';
		var normalbed_payments = ${normalbed_initialpayments};
		var icubed_payments = ${icubed_initialpayments};
		var gFreeBeds = ${freeBeds};
		var gBedTypes = ${bedtypesJSON};
		var isBystander = ${isBystander};
		var allbedtypesJSON = ${allbedtypesJSON};
		var bedTypes = ${bedTypes};
		var regDate = '${reg_date}';
		var regTime = '${reg_time}';
		var ipPrefs = ${ipPrefsJSON};
		var billDetails = ${billAmtDetailsJson};
		var balance = billDetails == null ? 0 : billDetails.totalReceipts-billDetails.totalAmount;
		var prvStartDate = '<fmt:formatDate value="${existingbedDetails.map.start_date}" pattern="dd-MM-yyyy"/>';
		var prvStartTime = '<fmt:formatDate value="${existingbedDetails.map.start_date}" pattern="HH:mm"/>';
		var canSetChargedBedType = '${actionRightsMap.set_charged_bed_type }' == 'A' || roleId == 1 || roleId == 2;
		var visitType = '${patient.visit_type}';
		var visitTotalPatientDue = '${visitTotalPatientDue}';
		var ipCreditLimitAmount = '${patient.ip_credit_limit_amount}';
		var ip_credit_limit_rule = '${ip_credit_limit_rule}';
		var creditLimitDetailsJSON = <%=request.getAttribute("creditLimitDetailsJSON")%>;
		function init() {
				populateBedTypes();
				onChangeBedType();
				}
	</script>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="billing.billlist"/>
</head>

<body class="yui-skin-sam" onload="init()">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<h1>${title}</h1>
<insta:feedback-panel/>
<insta:patientdetails patient="${patient}" />

<form action="AllocateBed.do?_method=allocateBed" name="bedform" method="POST">
<input type="hidden" name="mrno" value="${patient.mr_no}"/>
<input type="hidden" name="patient_id" value="${patient.patient_id}"/>
<input type="hidden" name="is_bystander" value="${isBystander}"/>
<input type="hidden" name="status" value="${empty patient.alloc_bed_name ? "A" : "C" }"/>
<input type="hidden" name="bed_state" value="O"/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.allocatebed.details.allocate"/>&nbsp;<insta:ltext key="registration.allocatebed.details.bed"/></legend>
	<table class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.bedtype"/>:</td>
					<td><select name="bed_type" id="bed_type"
						onchange="onChangeBedType()" class="dropdown">
							<option value=""></option>
					</select> <span class="star">*</span>
					</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.ward"/>:</td>
			<td>
				<select name="ward_no" class="dropdown" onchange="changewardname()">
					<option value="">${dummyvalue}</option>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.bedname"/>:</td>
			<td>
				<select name="bed_id" class="dropdown">
					<option value="">${dummyvalue}</option>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.chargedbedtype"/>:</td>
			<c:choose>
				<c:when test="${actionRightsMap.set_charged_bed_type eq 'A' || roleId == 1 || roleId ==2 }">
					<c:choose>
						<c:when test="${isBystander}">
							<td >
								<insta:selectdb table="bed_types" valuecol="bed_type_name" displaycol="bed_type_name"
									value="" filtercol="status,is_icu" filtered="true"
									filtervalue="A,N" name="charged_bed_type"/>
							</td>
						</c:when>
						<c:otherwise>
							<td >
								<insta:selectdb table="bed_types" valuecol="bed_type_name" displaycol="bed_type_name"
									value="${patient.bill_bed_type}" filtercol="status" filtered="true"
									filtervalue="A" name="charged_bed_type"/>
							</td>
						</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<td>
						<label id="chargedBedL">${patient.bill_bed_type}</label>
						<input type="hidden" name="charged_bed_type" id="charged_bed_type" value="${patient.bill_bed_type}" />
					</td>
				</c:otherwise>
			</c:choose>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.startdate"/>:</td>
			<td>
				<insta:datewidget name="start_date_dt" btnPos="left" value="${curDateVal}" />
				<input type="text" class="timefield" size="4" name="start_date_tm" value="${curTimeVal}" />
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.chargepostedupto"/>:</td>
			<td>
				<insta:datewidget name="end_date_dt" btnPos="left"
					value="${ip_preferences.bed_charge_posting == 'daily_update' ? nextDateVal : curDateVal}" />
				<input type="text" class="timefield" size="4" name="end_date_tm" value="${curTimeVal}" />
			</td>
		</tr>
		<c:if test="${bedChargeJobDetails.job_status == 'A'}">
			<tr>
				<td colspan="3" class="formlabel">
					<insta:ltext key="registration.allocatebed.details.chargeswillberecalculated"/> <b>${bedChargeJobDetails.job_time} <insta:ltext key="registration.allocatebed.details.hrs"/></b> <insta:ltext key="registration.allocatebed.details.everyday"/>
				</td>
			</tr>
		</c:if>
		<c:if test="${!isBystander}">
			<tr>
				<td class="formlabel"><insta:ltext key="registration.allocatebed.details.dutydoctor"/>:</td>
				<td>
					<select name="duty_doctor_id" class="dropdown">
					<option value="">${dummyvalue}</option>
					<c:forEach items="${dutyDoctors}" var="doctor">
						<option value="${doctor.map.doctor_id}">${doctor.map.doctor_name}</option>
					</c:forEach>
					</select>
					<c:if test="${ip_preferences.duty_doctor_selection != 'N'}">
						<span class="star" id="duty_doc_star" style="visibility:visible;">*</span>
					</c:if>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="registration.allocatebed.details.daycare"/>:</td>
				<td>
					<input type="checkbox" name="daycare_status" value="Y"/>
				</td>
			</tr>
		</c:if>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.allocatebed.details.remarks"/>:</td>
			<td>
				<input type="text" name="remarks" maxlength="100"/>
			</td>
		</tr>

	</table>
</fieldset>
</form>
<input type="submit" name="allocate" value="Allocate" ${multiCentered && centerId == 0 ? 'disabled' : '' } onclick="return allocateBed();"/>
<insta:screenlink addPipe="true" screenId="bed_view" label="Bed View" extraParam="?_method=getBedView"/>
<insta:screenlink addPipe="true" screenId="adt" label="ADT" extraParam="?_method=getADTScreen"/>
<insta:screenlink addPipe="true" screenId="ip_bed_details" label="Bed Details"
					extraParam="?method=getIpBedDetailsScreen&patientid=${patient.patient_id}"/>

</body>
</html>


