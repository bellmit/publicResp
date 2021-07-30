<%@page import="org.apache.struts.Globals"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
<title>Allocate Bed - Insta HMS</title>

<meta http-equiv="Content Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="date_go.js" />
<insta:link type="script" file="BedView/allocatebed.js"/>
<insta:link type="script" file="ipservices/bed.js" />
<insta:link type="script" file="hmsvalidation.js" />

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:url var="bedviewURL" value="/pages/ipservices/BedView.do?_method=getBedView"/>

<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

<script type="text/javascript">
		var cpath="${cpath}";
		var currentDate = '${dateVal}';
		var currentTime = '${timeVal }';
		var detailsJSON = '<c:out value="${detailsJSON}"/>';
		var tpaId = '${details.map.primary_sponsor_id}';
		var patientId = '${details.map.visit_id}';
		if(patientId != null){
			var normalbed_payments = ${normalbed_initialpayments};
			var icubed_payments = ${icubed_initialpayments};
			var billDetails = '${billAmtDetailsJson}';
			var balance ='<c:out value="${billadvancebalance}"/>';
		}
		var allowBackDate = '${actionRightsMap.allow_backdate}';
		var roleId = '${roleId}';
		var dutyDoctroSelectionFor = '${ip_preferences.duty_doctor_selection}';
		var nonICUBedTypes = ${nonICUBedTypes};
		var forceRemarks = '${ip_preferences.force_remarks}';
		var gBedTypes = ${bedtypesJSON};
		var bedName = '${details.map.alloc_bed_name}';
		var beddetailsJSON = ${beddetailsJSON};
		var canSetChargedBedType = '${actionRightsMap.set_charged_bed_type }' == 'A' || roleId == 1 || roleId == 2;
</script>
</head>

<body class="yui-skin-sam" onload="initAllocateBed();">
<jsp:useBean id="currentDate" class="java.util.Date"/>

<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="timeVal" value="${currentDate}" pattern="HH:mm"/>
<fmt:formatDate var="reg_date" value="${details.map.reg_date}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="reg_time" value="${details.map.reg_time}" pattern="HH:mm"/>

<c:set var="nextDay" value="${ifn:nextDate(currentDate,1)}"/>
<fmt:formatDate var="nextDateVal" value="${nextDay}" pattern="dd-MM-yyyy"/>

<form action="BedView.do" name="bedviewform" method="GET">
<input type="hidden" name="_method" value="allocateBed"/>
<h1>Allocate Bed</h1>
<insta:feedback-panel/>
<table class="formtable">
	<tr>
		<td>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Bed&nbsp;Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Bed Name:</td>
							<input type="hidden" name="bed_id" value="${beddetails.bed_id}"/>
							<input type="hidden" name="bed_type" value="${ifn:cleanHtmlAttribute(beddetails.bedtype)}"/>
							<input type="hidden" name="current_date" id="current_date" value="${dateVal}"/>
							<input type="hidden" name="current_time" id="current_time" value="${timeVal}"/>
							<input type="hidden" name="reg_date" id="reg_date" value="${reg_date }"/>
							<input type="hidden" name="reg_time" id="reg_time" value="${reg_time }"/>
							<input type="hidden" name="is_bystander" value="${empty beddetails.bed_id}"/>
							<input type="hidden" name="status" value="${empty details.map.alloc_bed_name ? "A" : "C" }"/>
							<input type="hidden" name="bed_state" value="O"/>
							<td class="forminfo">${ifn:cleanHtml(beddetails.bedname)} </td>

							<td class="formlabel">Bed Type:</td>
							<td class="forminfo">${ifn:cleanHtml(beddetails.bedtype)} </td>
						</tr>
						<tr>
							<td class="formlabel">Ward Name:</td>
							<td class="forminfo">${ifn:cleanHtml(beddetails.wardname)}</td>
							<td class="formlabel">Bed Status:</td>
							<c:set var="bed_status" value="Not Occupied"/>
							<c:if test="${beddetails.occupancy == 'Y'}">
								<c:set var="bed_status" value="Occupied"/>
							</c:if>
							<td class="forminfo">${bed_status} </td>
						</tr>
					</table>
			</fieldset>
		</td>
	</tr>
	<tr>
		<td>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Allocate&nbsp;Bed</legend>
					<table>
						<tr>
							<td class="formlabel">Patient Name/Id:</td>
							<td valign="top">
								<div id="visitIdAutoComplete">
									<input type="text" name="patient_id" id="patient_id" value="${details.map.patient_id }"
									 onblur="onChangeMrno(this);">
									<div id="patientIdContainer" style="width:300px"></div>
									<input type="hidden" name="mrno" value="${details.map.mr_no}"/>
								</div>
							</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel">Admitting Date:</td>
							<td>
								<insta:datewidget name="start_date_dt" id="start_date_dt" btnPos="left" value="${dateVal }" />
								<input type="text" class="timefield" size="4" name="start_date_tm" id="start_date_tm" value="${timeVal }" />
							</td>
						</tr>
						<tr>
							<td class="formlabel">Charge Posted Up To:</td>
							<td>
								<insta:datewidget name="end_date_dt" btnPos="left"
									value="${ip_preferences.bed_charge_posting == 'daily_update' ? nextDateVal : curDateVal}" />
								<input type="text" class="timefield" size="4" name="end_date_tm"  id="end_date_tm" value="${timeVal}" />
							</td>
						</tr>
						<c:if test="${bedChargeJobDetails.job_status == 'A'}">
							<tr>
								<td colspan="3" class="formlabel">
									Charges will be recalculated automatically at <b>${bedChargeJobDetails.job_time} Hrs</b> every day
								</td>
							</tr>
						</c:if>
						<tr>
							<td class="formlabel">Duty Doctor:</td>
							<td>
							<insta:selectdb name="duty_doctor_id" table="doctors" valuecol="doctor_id"
									displaycol="doctor_name" dummyvalue="...Select..."  dummyvalueId=""
									orderby="doctor_name"/>
							<c:if test="${ip_preferences.duty_doctor_selection != 'N'}">
								<span class="star">*</span>
							</c:if>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Charged Bed Type:</td>
							<c:choose>
								<c:when test="${actionRightsMap.set_charged_bed_type eq 'A' || roleId == 1 || roleId ==2 }">
								<td >
									<insta:selectdb table="bed_types" valuecol="bed_type_name" displaycol="bed_type_name"
										value="${patient.bill_bed_type}" filtercol="status,billing_bed_type" filtered="true"
										filtervalue="A,Y" name="charged_bed_type"/>
								</td>
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
							<td class="formlabel" >Daycare:</td>
							<td>
								<input type="checkbox" name="daycare_status"
								style="margin: 0px; padding: 0px" value="Y"  onchange="changeDaysToHrs(this);"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel" id="dayhrth">Estimated Days:</td>
							<td>
								<input type="text" name="estimated_days"  id="estimated_days" value="${estimated_days }"
								class="number" maxlength="5"
								${estimate }/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Remarks:</td>
							<td>
								<input type="text" name="remarks" id="remarks" maxlength="100"/>
							</td>
						</tr>
					</table>
			</fieldset>
		</tr>
</table>
</form>
<input type="button" name="allocatebed" value="Allocate Bed" ${multiCentered && centerId == 0 ? 'disabled' : '' } onclick="return validateAllocation();"/>
<insta:screenlink addPipe="true" screenId="bed_view" label="Bed View" extraParam="?_method=getBedView"/>
<insta:screenlink addPipe="true" screenId="adt" label="ADT" extraParam="?_method=getADTScreen"/>
</body>
</html>
