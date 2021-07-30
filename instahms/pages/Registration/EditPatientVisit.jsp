<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><insta:ltext key="registration.patient.label.editpatientvisit"/></title>
<meta name="i18nSupport" content="true"/>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="areacommon.js"/>
<insta:link type="js" file="registration/editPatientVisit.js" />
<insta:link type="js" file="registration/registrationCommon.js" />
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO" %>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<script>
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var gVisitId = '${ifn:cleanJavaScript(visitId)}';
		var active = '${active}';
		var deptUnitSetting = '${ifn:cleanJavaScript(regPref.deptUnitSetting)}';
		var visitTypeDependence = '${ifn:cleanJavaScript(regPref.visit_type_dependence)}';
		var screenid = '';
		var prevDeptsJSON   = ${prevDepts};
		var regPref = <%=request.getAttribute("regPrefJSON")%>;
		var enableDistrict = regPref ? regPref.enableDistrict : null;
		var healthAuthoPref = '${healthAuthoPrefJSON}';
		var centerId = ${centerId};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};
		var allowFieldEdit = "${(actionRightsMap.edit_custom_fields == 'A' || roleId == '1' || roleId == '2') ? 'A' : 'N' }";
		var creditLimitDetailsJSON = <%=request.getAttribute("creditLimitDetailsJSON")%>;
</script>

<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="registration.editvisitdetails"/>
</head>
<body onload="init();" class="yui-skin-sam">
<c:set var="optype">
<insta:ltext key="registration.patient.label.mainvisit"/>,
<insta:ltext key="registration.patient.label.followup.with.consultation"/>,
<insta:ltext key="registration.patient.label.followup.without.consultation"/>,
<insta:ltext key="registration.patient.label.revisit"/>
</c:set>
<c:set var="patientgender">
<insta:ltext key="selectdb.dummy.value"/>,
<insta:ltext key="registration.patient.label.male"/>,
<insta:ltext key="registration.patient.label.female"/>,
<insta:ltext key="registration.patient.label.others"/>
</c:set>
<c:set var="establishedtype">
<insta:ltext key="registration.patient.label.established"/>,
<insta:ltext key="registration.patient.label.new"/>
</c:set>
<c:set var="dummyvalue">
<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="addeditrateplan">
<insta:ltext key="registration.patient.label.addeditrateplan"/>
</c:set>
<c:set var="changeRateplan">
<insta:ltext key="billing.patientbill.details.changerateplan.bedtype"/>
</c:set>
<c:set var="visitAuditlog">
<insta:ltext key="billing.patientbill.details.visit.auditlog"/>
</c:set>
<c:set var="newDept" value="" />
<c:set var="consultingDocMand" value="<%=RegistrationPreferencesDAO.getRegistrationPreferences().getConductingdoctormandatory()%>" />
<h1 style="float: left"><insta:ltext key="registration.patient.label.editpatientvisitdetails"/></h1>

<insta:patientsearch searchType="visit" searchUrl="editvisitdetails.do"
	buttonLabel="Find" searchMethod="getPatientVisitDetails"
	fieldName="patient_id" showStatusField="true" />

<insta:feedback-panel />
<insta:patientdetails visitid="${visitId}" />
<c:set var="displayOtherDetails" value="${not empty prevRegdate && not empty visitbean.previous_visit_id && visitbean.previous_visit_id ne visitId}"/>
<fieldset class="fieldSetBorder" style="display: ${displayOtherDetails ? 'block' : 'none'}"><legend
	class="fieldSetLabel"><insta:ltext key="patient.header.fieldset.otherdetails"/></legend>
<table class="patientdetails" style="width:100%">
	<c:choose>
		<c:when
			test="${not empty prevRegdate && not empty visitbean.previous_visit_id && visitbean.previous_visit_id ne visitId}">
			<fmt:formatDate var="prdate" value="${prevRegdate}"
				pattern="dd-MM-yyyy" />
			<fmt:formatDate var="prtime" value="${prevRegtime}" pattern="HH:mm" />
			<c:set var="prevRegdate" value="${prdate}"/>
			<c:set var="prevRegtime" value="${prtime}"/>
			<tr>
				<td class="formlabel" title='<insta:ltext key="registration.patient.label.previousreg.datetime"/>'><insta:ltext key="registration.patient.label.prev.regdate"/>:</td>
				<td class="forminfo">${prdate} ${prtime}</td>
				<c:choose>
					<c:when test="${not empty regPref.oldRegNumField}">
						<td class="formlabel">${ifn:cleanHtml(regPref.oldRegNumField)}:</td>
						<td class="forminfo">${visitbean.oldmrno}</td>
						<td class="formlabel"></td>
						<td></td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"></td>
						<td></td>
						<td class="formlabel"></td>
						<td></td>
					</c:otherwise>
				</c:choose>
			</tr>
		</c:when>
		<c:otherwise>
			<c:set var="prevRegdate" value=""/>
			<c:set var="prevRegtime" value=""/>
			<td class="formlabel"></td>
			<td></td>
			<td class="formlabel"></td>
			<td></td>
			<td class="formlabel"></td>
			<td></td>
		</c:otherwise>
	</c:choose>
</table>
</fieldset>

<form method="post" name="mainform" action="editvisitdetails.do" autocomplete="off">

<input type="hidden" name="_method" value="savePatientVisitDetails" /> <input
	type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(visitId)}" /> <input
	type="hidden" name="mlc_template_name" value="" />
<input type="hidden" name="mrno"  id="mrno" value="${visitbean.mr_no}" />
<input type="hidden" name="prevRegdate" value="prevRegdate">
<input type="hidden" name="prevRegtime" value="prevRegtime">
<input type="hidden" name="docMandatory" id ="docMandatory" value="${consultingDocMand}">
<input type="hidden" name="patient_name"  value="${visitbean.patient_name}" />
<input type="hidden" name="bed_name"  value="${visitbean.alloc_bed_name}" />
<input type="hidden" name="ward_name"  value="${visitbean.reg_ward_name}" />
<input type="hidden" name="patient_phone"  value="${visitbean.patient_phone}" />
<input type="hidden" name="email_id"  value="${visitbean.email_id}" />
<input type="hidden" name="referredbyValidate" value="${ifn:cleanHtmlAttribute(regPref.referredbyValidate)}" />
<input type="hidden" name="visitStatus" id ="visitStatus" value="${visitbean.visit_status}"/>
<c:set var="dischargeSummaryEnabled" value="${urlRightsMap.discharge_summary}" />
<c:set var="isWardEditable">
	<c:if test="${isAdmitted == 'Y'}"> disabled = "disabled"</c:if>
</c:set>

<c:set var="isOpTypeEditable"
	value="${(not empty visitId && active)}"/>

<input type="hidden" name="hasOpType" id="hasOpType" ${isOpTypeEditable ? '' : 'disabled' }/>
<input type="hidden" name="dept_allowed_gender" value="">
<insta:selectoptions name="patient_gender" opvalues=" ,M,F,O" optexts="${patientgender}" value="${visitbean.patient_gender}" style="display:none;" />


<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.patient.label.visitdetails"/></legend>
<table class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.collectioncenter"/>:</td>
		<td class="forminfo">
			<c:choose>
				<c:when test="${genericPrefs.max_centers_inc_default > 1}">
					<c:choose>
						<c:when test="${sampleCollectionCenterId != -1}">
							<label>${visitbean.collection_center}</label>
						</c:when>
						<c:otherwise>
							<select name="collection_center_id" id="collection_center_id" class="dropdown">
								<option value="-1" ${visitbean.collection_center_id == -1?'selected':''}>${defautlCollectionCenter}</option>
								<c:forEach items="${visitCenterWiseCollectionCenters}" var="collectioncenter">
									<c:if test="${collectioncenter.map.status=='A'}">
										<option value="${collectioncenter.map.collection_center_id}" ${visitbean.collection_center_id==collectioncenter.map.collection_center_id ?'selected':''}>
											${collectioncenter.map.collection_center}
										</option>
									</c:if>
								</c:forEach>
							</select>
						</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${sampleCollectionCenterId != -1}">
							<label>${visitbean.collection_center}</label>
						</c:when>
						<c:otherwise>
							<insta:selectdb id="collection_center_id"  name="collection_center_id"
								value="${visitbean.collection_center_id}" table="sample_collection_centers"
								valuecol="collection_center_id" displaycol="collection_center" orderby='collection_center'/>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</td>
		<c:if test="${genericPrefs.max_centers_inc_default > 1}">
			<td class="formlabel"><insta:ltext key="registration.patient.label.center"/>:</td>
			<td class="forminfo">${visitbean.center_name}</td>
		</c:if>
		</tr>
		<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.admissiondate.and.time"/>:</td>
		<td style="width:200px;" align="left">
		<table>
			<tr>
				<fmt:formatDate var="rdate" value="${visitbean.reg_date}"
					pattern="dd-MM-yyyy" />
				<fmt:formatDate var="rtime" value="${visitbean.reg_time}"
					pattern="HH:mm" />
				<c:choose>
					<c:when
						test="${actionRightsMap.allow_backdate == 'A' || roleId == 1 || roleId ==2}">
						<td><insta:datewidget name="reg_date" valid="past"
							value="${rdate}" btnPos="right" /> <input type="text"
							name="reg_time" value="${rtime}" class="timefield" /></td>
					</c:when>
					<c:otherwise>
						<td class="forminfo">${rdate} ${rtime} <input type="hidden"
							name="reg_date" value="${rdate}"> <input type="hidden"
							name="reg_time" value="${rtime}"></td>
					</c:otherwise>
				</c:choose>
			</tr>
		</table>
		</td>
		<c:if test="${visitbean.visit_type == 'o' && visitbean.op_type != 'O'}">
			<td class="formlabel"><insta:ltext key="registration.patient.label.visittype"/>:</td>
			<td>
				<c:choose>
					<c:when test="${isOpTypeEditable}">
						<insta:selectoptions name="op_type" id="op_type"
						opvalues="M,F,D,R" optexts="${optype}"
						value="${visitbean.op_type}"/>
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="op_type" id="op_type"
						opvalues="M,F,D,R" optexts="${optype}"
						value="${visitbean.op_type}" disabled = "disabled"/>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="main_visit_id" id="main_visit_id" value="${visitbean.main_visit_id}"/>
			</td>
			<td class="formlabel"><insta:ltext key="ui.label.encounter.type"/>:</td>
      <td>
              <c:choose>
              <c:when test="${(actionRightsMap.allow_assigning_encounter_type == 'A' || roleId == 1 || roleId == 2) && (visitbean.visit_status == 'A')}">
              <insta:selectdb name="encounter_type" id="encounter_type" dummyvalue="-- Select --"
            	   filtercol="op_applicable,status" filtervalue="Y,A"
                table="encounter_type_codes" displaycol="encounter_type_desc" valuecol="encounter_type_id"
                value="${visitbean.encounter_type}"/>
                <span class="star">*</span>
                </c:when>
                <c:when test="${actionRightsMap.allow_assigning_encounter_type == 'N' || visitbean.visit_status == 'I'}">
                <insta:selectdb name="encounter_type" id="encounter_type" dummyvalue="-- Select --"
                   filtercol="op_applicable,status" filtervalue="Y,A"
                   table="encounter_type_codes" displaycol="encounter_type_desc" valuecol="encounter_type_id"
                   value="${visitbean.encounter_type}" disabled="true" />
                </c:when>
                </c:choose>
            </td>
		</c:if>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.department"/>:</td>
		<td>
			<select name="dept_name" id="dept_name" class="dropdown" onchange="onChangeDepartment()">
				<option value="">${dummyvalue}</option>
				<c:forEach items="${arrdeptDetails}" var="deptDetails">
					<option value="${deptDetails.DEPT_ID}" ${visitbean.dept_id==deptDetails.DEPT_ID ? 'selected' : ''}>
						${deptDetails.DEPT_NAME}
					</option>
				</c:forEach>
			</select>
			<input type="hidden" name="new_dept_name" value=""/>
		</td>
		<td class="formlabel"><insta:ltext key="registration.patient.label.deptestablishedstatus"/>:</td>
		<td class="forminfo">
			<c:choose>
				<c:when test="${visitbean.established_type == 'N'}">
					<insta:ltext key="registration.patient.label.new" />
				</c:when>
				<c:when test="${visitbean.established_type == 'E'}">
					<insta:ltext key="registration.patient.label.established" />
				</c:when>
			</c:choose>
		</td>
		<c:if test="${visitbean.visit_type == 'i'}">
			<td class="formlabel">
				<label for="creditlimit"><insta:ltext key="ui.label.ippatient.creditlimit"/>:</label>
			</td>
			<td>
				<input type="text" class="field" name="ip_credit_limit_amount" id="ip_credit_limit_amount" onkeypress="return enterFloatNumOnly(event);"
					value="${availableCreditLimit}"
					${actionRightsMap.allow_ip_patient_credit_limit_change eq 'A' || roleId == 1 || roleId ==2 ? '' : 'readonly'}>
			</td>
			<td align="left" style="padding-left: 0px;">
				<c:set var="helpMsg">Available Credit Limit = Credit Limit + Available Deposits - Patient Dues. Credit Limit : ${creditLimitDetailsMap.sanctionedCreditLimit} , Available Deposits : ${creditLimitDetailsMap.availableDeposit}, Patient Dues : ${creditLimitDetailsMap.visitPatientDue}</c:set>
				<img class="imgHelpText" align="left" id="credit_limit_help" title="${helpMsg}" src="${cpath}/images/help.png"/>
			</td>
		</c:if>
	</tr>
	<tr>
		<c:if test="${regPref.showReferralDoctorFilter != 'Y'}">
		<td class="formlabel"><insta:ltext key="registration.patient.label.referredby"/>:</td>
		<td valign="top">
			<div id="referalAutoComplete"><input type="text"
				name="referaldoctorName" id="referaldoctorName"
				value="${empty referalDetails ? '' : referalDetails.referal_name}" style="width: 220px;"
				${actionRightsMap.modify_referred_by_details != 'N' || roleId == 1 || roleId == 2 ? '' : 'disabled'}/>
			<div id="referalNameContainer"></div>
			</div>
			<c:if test="${not empty regPref.referredbyValidate &&
      	  (regPref.referredbyValidate eq 'A' ||
      		(visitbean.visit_type == 'i' && regPref.referredbyValidate eq 'I') ||
      		(visitbean.visit_type == 'o' && regPref.referredbyValidate eq 'P'))}">
      <span class="star"
      style="${visitbean.visit_type == 'i' ? 'position: absolute; left: 522px' : 'position: absolute; left: 551px'}">*</span>
      </c:if>
			<input type="hidden" name="reference_docto_id" id="referred_by"
				value="${empty referalDetails ? '' : referalDetails.referal_no}">
		</td>
		</c:if>
		<td class="formlabel"><insta:ltext key="registration.patient.label.doctor"/>:</td>
		<td colspan="2" valign="top"><c:set var="doctorDeptName"
			value="${visitbean.doctor_name} (${visitbean.dept_name})" />
				<%-- Doctor - Department Autocomplete --%>
			<div id="doc_dept_wrapper" style="width: 250px;"><input
				type="text" name="doctor_name" id="doctor_name"
				value="${not empty visitbean.doctor?doctorDeptName:''}"
				style="width: 250px;" />
			<div id="doc_dept_dropdown"></div>
			<input type="hidden" name="doctor" id="doctor"
				value="${visitbean.doctor}">
		</td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.medicolegalcase"/>:</td>
		<td><c:set var="tempid" value="" /> <c:set var="tempformat"
			value="" /> <c:if test="${not empty visit_mlc_template}">
			<c:set var="tempid" value="${visit_mlc_template.map.template_id}" />
			<c:set var="tempformat" value="${visit_mlc_template.map.doc_format}" />
		</c:if> <select name="mlc_template" onchange="onChangeMLCDoc()"
			class="dropdown">
			<option value="">${dummyvalue}</option>
			<c:forEach var="template" items="${mlc_templates}">
				<option value="${template.map.template_id},${template.map.format}"
					${tempid== template.map.template_id && tempformat==
					template.map.format ? 'selected' :''}
							id="${template.map.template_name}">${template.map.template_name}</option>
			</c:forEach>
		</select></td>
		<td class="formlabel"><insta:ltext key="registration.patient.label.mlctype"/>:</td>
		<td><input type="text" name="mlc_type"
			value="${visitbean.mlc_type}"></td>
		<td class="formlabel"><insta:ltext key="registration.patient.label.accidentplace"/>:</td>
		<td><input type="text" name="accident_place"
			value="${visitbean.accident_place}"></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.policestation"/>:</td>
		<td><input type="text" name="police_stn"
			value="${visitbean.police_stn}"></td>
		<td class="formlabel"><insta:ltext key="registration.patient.label.mlcremarks"/>:</td>
		<td><input type="text" name="mlc_remarks"
			value="${visitbean.mlc_remarks}"></td>
		<td class="formlabel"><insta:ltext key="registration.patient.label.certificatestatus"/>:</td>
		<td><input type="text" name="certificate_status"
			value="${visitbean.certificate_status}"></td>
	</tr>

	<c:if
		test="${regPref.hospUsesUnits != null && regPref.hospUsesUnits == 'Y'
							&& regPref.deptUnitSetting != null && regPref.deptUnitSetting != ' '}">
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.unit"/>:</td>
		<td><insta:selectdb id="unit_id" name="unit_id"
			value="${visitbean.unit_id}" table="dept_unit_master"
			valuecol="unit_id" displaycol="unit_name" dummyvalue="${dummyvalue}" />
		</td>
	</tr>
	</c:if>

	<c:if test="${visitbean.visit_type == 'i'}">
		<tr>
			<td class="formlabel"><insta:ltext key="registration.patient.label.bedtype"/>:</td>
			<td class="forminfo">
				<label id="bedTypelbl"></label>
			</td>
			<td class="formlabel"><insta:ltext key="registration.patient.label.ward"/>:</td>
			<td>
				<select name="ward_id" id="ward_id" class="dropdown" ${isWardEditable} onchange="onWardChang()">
					<option value="">${dummyvalue}</option>
				</select>
			</td>
			<td class="formlabel"></td>
			<td>
				<select name="bed_type" id="bed_type" class="dropdown" style="visibility:hidden"
					onchange="onChangeBedType(this, document.mainform.ward_id)">
					<option value="">${dummyvalue}</option>
				</select>
			</td>
		</tr>
	</c:if>
	<tr>
		<c:choose>
			<c:when test="${patient.visit_type == 'o'}">
				<td class="formlabel"><insta:ltext key="registration.patient.label.closevisit"/>:</td>
				<td><input type="checkbox" name="close" id="close"
					onClick="closeVisit()"${active?'':'disabled'}></td>
			</c:when>
			<c:otherwise>
				<td class="formlabel"><insta:ltext key="registration.patient.label.dischargepatient"/>:</td>
				<td><input type="checkbox" name="discharge" id="discharge"
					onClick="dischargePatient()"${active?'':'disabled'}></td>
			</c:otherwise>
		</c:choose>
		<input type="hidden" name="dischargeOrcloseVisit"
			id="dischargeOrcloseVisit" value="N">
	</tr>
</table>

</fieldset>
<c:if test="${regPref.showReferralDoctorFilter == 'Y'}">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="ui.label.referral.information"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="ui.label.state"/>:</td>
			   	<td>
					<div id="autoreferralstate" class="autoComplete">
						<input name="referral_filter_state" id="referral_filter_state" type="text"
						value="${empty referalDetails ? '' : referalDetails.state_name}" style="width:11.6em" maxlength="50" />
						<div id="referral_filter_state_dropdown" style="width:250px"></div>
						<input type="hidden" name="referral_filter_state_id" id="referral_filter_state_id" value="${empty referalDetails ? '' : referalDetails.state_id}"/>
						<input type="hidden" name="referral_filter_country" id="referral_filter_country" value="${empty referalDetails ? '' : referalDetails.country_name}"/>
						<input type="hidden" name="referral_filter_country_id" id="referral_filter_country_id" value="${empty referalDetails ? '' : referalDetails.country_id}"/>
					</div>
				</td>
				<c:if test="${regPref.enableDistrict == 'Y'}">
					<td class="formlabel">
						<insta:ltext key="ui.label.district"/>:
					</td>
					<td>
						<div id="autoreferraldistrict" class="autoComplete">
							<input name="referral_filter_district" id="referral_filter_district" type="text"
							value="${empty referalDetails ? '' : referalDetails.district_name}" style="width:11.6em" maxlength="50" />
							<div id="referral_filter_district_dropdown" style="width:250px"></div>
							<input type="hidden" name="referral_filter_district_id" id="referral_filter_district_id" value="${empty referalDetails ? '' : referalDetails.district_id}"/>
						</div>
					</td>
				</c:if>
				<td class="formlabel"><insta:ltext key="${regPref.enableDistrict == 'Y' ? 'ui.label.city.subdistrict' : 'ui.label.city' }"/>:</td>
				<td>
					<div id="autoreferralcity" class="autoComplete">
						<input type="text" name="referral_filter_city" id="referral_filter_city"
							value="${empty referalDetails ? '' : referalDetails.city_name}" />
						<div id="referral_filter_city_dropdown" style="width:250px"></div>
						<input type="hidden" name="referral_filter_city_id" id="referral_filter_city_id" value="${empty referalDetails ? '' : referalDetails.city_id}"/>
					</div>
				</td>
				<td class="formlabel"><insta:ltext key="${regPref.enableDistrict == 'Y' ? 'ui.label.area.village' : 'ui.label.area' }"/>:</td>
				<td>
					<div id="autoreferralarea" class="autoComplete">
						<input name="referral_filter_area" id="referral_filter_area" type="text"
						value="${empty referalDetails ? '' : referalDetails.area_name}" style="width:11.6em" maxlength="50" />
						<div id="referral_filter_area_dropdown" style="width:250px"></div>
						<input type="hidden" name="referral_filter_area_id" id="referral_filter_area_id" value="${empty referalDetails ? '' : referalDetails.area_id}"/>
					</div>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.referredBy"/>:</td>
				<td colspan="3">
					<div id="referalAutoComplete" class="autoComplete">
						<input type="text" name="referaldoctorName" id="referaldoctorName"
							class="field" style="width:180px;" maxlength="100" value="${empty referalDetails ? '' : referalDetails.referal_name}"/>
						<div id="referalNameContainer" class="scrolForContainer" style="width:340px;"></div>
					</div>
					<c:if test="${not empty regPref.referredbyValidate &&
							(regPref.referredbyValidate eq 'A' ||
								(screenId eq 'ip_registration' && regPref.referredbyValidate eq 'I') ||
								(screenId eq 'out_pat_reg' && regPref.referredbyValidate eq 'P'))}">
					<span class="star">*</span>
					</c:if>
					<input type="hidden" name="reference_docto_id" id="referred_by" value="${empty referalDetails ? '' : referalDetails.referal_no}"/>
				</td>
			</tr>
		</table>
	</fieldset>
</c:if>
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="registration.patient.label.patientdetails"/></legend>
<table class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.label.nextofkinname"/>:</td>
		<td><input type="text" value="${visitbean.patrelation}"
			name="relation"></td>

		<td class="formlabel"><insta:ltext key="registration.patient.label.nextofkincontactno"/>:</td>
		<td><input type="text" name="patient_care_oftext"
			value="${visitbean.patcontactperson}"></td>

		<td class="formlabel"><insta:ltext key="registration.patient.label.nextofkinaddress.or.phone"/>:</td>
		<td><input type="text" name="patient_careof_address"
			value="${visitbean.pataddress}"></td>
	</tr>
</table>
</fieldset>


<c:if test="${not empty hasVisitAddlnFields && hasVisitAddlnFields}">
<table class="formtable" >
	<tr>
		<td>
			<div id="VisitCollapsiblePanel1" class="CollapsiblePanel">
				<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
				<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">
					<insta:ltext key="registration.patient.label.additional"/>&nbsp;
					<insta:ltext key="registration.patient.label.visit"/>&nbsp;
					<insta:ltext key="registration.patient.label.i"/><b><u><insta:ltext key="registration.patient.label.n"/></u></b><insta:ltext key="registration.patient.label.formation"/></div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
					<img src="${cpath}/images/up.png" />
				</div>
				<div class="clrboth"></div>
			</div>
			<fieldset class="fieldSetBorder">
				<table class="formtable">
					<c:set var="visitcolumns" value="0"/>
					<tr>
						<%-- VP--%>
						<c:forEach var="num" begin="1" end="2">
						<c:set var="nameField" value="visit_custom_list${num}_name"/>
						<c:set var="validateField" value="visit_custom_list${num}_validate"/>
						<c:if test="${not empty regPref[nameField]}">
							<td class="formlabel">${regPref[nameField]}:</td>
							<td>
								<c:set var="fieldName" value="visit_custom_list${num}"/>
									<c:set var="fieldValue" value="${visitbean[fieldName]}"/>
								<insta:selectdb name="visit_custom_list${num}" id="visit_custom_list${num}"
									value="${fieldValue}" table="custom_visit_list${num}_master"
									style="width:140px;" valuecol="custom_value" displaycol="custom_value" usecache="true"
									dummyvalue="${dummyvalue}" size="1" orderby="custom_value"/>
								<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
												(visitbean.visit_type eq 'o' && regPref[validateField] eq 'O') ||
												(visitbean.visit_type eq 'i' && regPref[validateField] eq 'I'))}">
								<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="visitcolumns" value="${visitcolumns+1}"/>
							<c:if test="${(visitcolumns % 3) == 0}"></tr><tr></c:if>
						</c:if>
						</c:forEach>


						<%-- Visit custom fields --%>
						<c:forEach var="num" begin="1" end="9">
							<c:set var="labelField" value="visit_custom_field${num}_name"/>
							<c:set var="validateField" value="visit_custom_field${num}_validate"/>
							<c:if test="${not empty regPref[labelField]}">
								<td class="formlabel">${regPref[labelField]}:</td>
								<td>
									<c:set var="fieldName" value="visit_custom_field${num}"/>
									<c:set var="fieldValue" value="${visitbean[fieldName]}"/>
										<c:choose>
											<c:when test="${num le 3}">
												<input type="text" class="field" name="visit_custom_field${num}" value="${fieldValue}"/>
											</c:when>
											<c:when test="${num le 6}">
												<c:set var="fieldValue">
													<fmt:formatDate pattern="dd-MM-yyyy" value="${fieldValue}"/>
												</c:set>
												<insta:datewidget name="visit_custom_field${num}"  title="visit_custom_field${num}" value="${fieldValue}"/>
											</c:when>
											<c:otherwise>
												<input type="text" class="number" name="visit_custom_field${num}" value="${fieldValue}"/>
											</c:otherwise>
										</c:choose>
										<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
														(visitbean.visit_type eq 'o' && regPref[validateField] eq 'O') ||
														(visitbean.visit_type eq 'i' && regPref[validateField] eq 'I'))}">
										<span class="star">*</span>
										</c:if>
								</td>
								<c:set var="visitcolumns" value="${visitcolumns+1}"/>
								<c:if test="${(visitcolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>
						</c:forEach>
					</tr>
				</table>
			</fieldset>
			</div>
		</td>
	</tr>
</table>
</c:if>

<div class="screenActions">
	<insta:accessbutton buttonkey="billing.editvisitrateplan.bedtype.details.save" type="button" onclick="saveVisitdetails();">
		"${not empty visitId && (active || (!active && (actionRightsMap.dishcharge_close != 'N' || roleId == 1 || roleId ==2))) ? '' :'disabled'}"
	</insta:accessbutton>
<c:if test="${not empty visitId}">
	<c:if test="${active}">
			| <a
			href="${cpath}/pages/registration/editvisitdetails.do?_method=getPatientVisitDetails&patient_id=${ifn:cleanURL(visitId)}&ps_status=active">
		<insta:ltext key="registration.patient.label.reset"/></a>
			| <a
			href="${cpath}/pages/registration/GenerateRegistrationCard.do?patid=${ifn:cleanURL(visitId)}&orgId=${ifn:cleanURL(visitbean.org_id)}"
			target="_blank"> <insta:ltext key="registration.patient.label.printreg.card"/></a>
			| <a
			href="${cpath}/pages/registration/GenerateRegistrationBarCode.do?method=execute&mrno=${visitbean.mr_no}&barcodeType=Reg&visitId=${ifn:cleanURL(visitId)}"
			target="_blank"><insta:ltext key="registration.patient.label.printreg.barcode"/></a>
			| <a
			href="${cpath}/Registration/GeneralRegistration.do?_method=show&mrno=${ifn:cleanURL(visitbean.mr_no)}&visitId=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.editpatientdetails"/>
		</a>
			| <a
			href="${cpath}/pages/RegistrationDocuments.do?_method=searchPatientGeneralDocuments&mr_no=${ifn:cleanURL(visitbean.mr_no)}&patient_id=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.editregistrationdocuments"/>
		</a>
		<c:if
			test="${not empty visitId && not empty regPref.patientCategory && (urlRightsMap.patient_category_change eq 'A' || roleId == 1 || roleId == 2)}">
		        | <a
				href="${cpath}/pages/registration/ManageVisits/PatientCategoryChange.do?method=editCategoryDetails&patient_id=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.patientcategorychange"/>
			</a>
		</c:if>
		<c:if test="${not empty visit_mlc_template}">
				| <a
				href="${cpath}/MLCDocuments/MLCDocumentsAction.do?_method=show&mr_no=${visit_mlc_template.map.mr_no}
					&doc_id=${visit_mlc_template.map.doc_id}&template_id=${visit_mlc_template.map.template_id}&format=${visit_mlc_template.map.doc_format}
					&patient_id=${ifn:cleanURL(visitId)}&documentType=mlc&${ifn:cleanURL(visitId)}"
				title="Open selected template"><insta:ltext key="registration.patient.label.updatemlc"/></a>
		</c:if>
		<c:if test="${visitbean.visit_type == 'o'}">
				| <a
				href="${cpath}/pages/registration/ManageVisits/OPIPConversion.do?method=getOPIPConversion&patient_id=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.convertoptoip"/>
			</a>
		</c:if>
	</c:if>
	<c:if test="${!active}">
		| <a href="${cpath}/pages/registration/readmit.do?_method=getReadmitScreen&patient_id=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.re_admit"/></a>
		| <a
			href="${cpath}/pages/registration/GenerateRegistrationCard.do?patid=${ifn:cleanURL(visitId)}&orgId=${ifn:cleanURL(visitbean.org_id)}"
			target="_blank"> <insta:ltext key="registration.patient.label.printreg.card"/></a>
		| <a
			href="${cpath}/pages/registration/GenerateRegistrationBarCode.do?method=execute&mrno=${visitbean.mr_no}&barcodeType=Reg&visitId=${ifn:cleanURL(visitId)}"
			target="_blank"><insta:ltext key="registration.patient.label.printreg.barcode"/></a>

	</c:if>
	<c:if test="${dischargeSummaryEnabled == 'A'}">
		| <a href="${cpath}/inpatients/dischargesummary/index.htm#/filter/default/patient/${ifn:cleanURL(visitbean.mr_no)}/dischargesummary?retain_route_params=true"><insta:ltext key="registration.patient.label.dischargesummary"/>
		</a>
	</c:if>
	<c:if test="${not empty fingerprintHostName}">
		| <insta:screenlink screenId="fp_service_enroll" extraParam="?_method=getEnrollScreen&mr_no=${visitbean.mr_no}&visit_id=${visitId}" label="Re-enroll Finger Print"/>
	</c:if>
	<c:if test="${patient.visit_type == 'o' && patient.use_perdiem == 'N'}">
		|<a href="${cpath}/insurance/showInsuranceDetails.htm?visitId=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.add.or.editinsurance"/></a>
	</c:if>
	<c:if test="${patient.visit_type == 'i' || patient.use_perdiem == 'Y'}">
		|<a href="${cpath}/editVisit/changeTPA.do?_method=changeTpa&visitId=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.label.add.or.editinsurance"/></a>
	</c:if>
	<insta:screenlink screenId="change_visit_org" addPipe="true" label="${changeRateplan}"
							extraParam="?_method=updateRatePlan&visitId=${visitId}&billNo="
							title="${addeditrateplan}"/>
	<insta:screenlink screenId="registration_audit_log" extraParam="?_method=getAuditLogDetails&patient_id=${visitId}&al_table=patient_registration_audit_log" addPipe="true" label="${visitAuditlog}"/>
	<c:if test="${not empty visitbean.mr_no && preferences.modulesActivatedMap['mod_hie'] eq 'Y'}">
  		| <a style="cursor: pointer;" onclick="return openConsentUploadDocumentPopUp('${visitbean.mr_no}','${genericPrefs.upload_limit_in_mb}')"><insta:ltext key="js.label.hie.consent"/></a>
  </c:if>
</c:if>
</div>

</form>

<script type="text/javascript">
	var VisitCollapsiblePanel1 = (document.getElementById("VisitCollapsiblePanel1")) ?
				new Spry.Widget.CollapsiblePanel("VisitCollapsiblePanel1", {contentIsOpen:true}) : null;
</script>

<script>
	var jDocDeptNameList = null;
	var unitList = null;
	var bedCharges = null;
	var bedtype = '';
	var wardid = '';
	var visittype = '';
	var gDoctorId = null;
	var gDeptId = null;
	var gOptype = null;
	var gMainVisitId = null;
	var deptList = null;
	var doctorsList = null;
	if (gVisitId != null && gVisitId != '') {
		jDocDeptNameList = <%= request.getAttribute("docDeptNameList") %>;
		unitList = <%= request.getAttribute("unitList") %>;
		bedCharges = <%= request.getAttribute("bedChargesJson") %>;
		bedtype = <insta:jsString value="${visitbean.bill_bed_type}"/>;
		wardid = '${visitbean.reg_ward_id}';
		visittype = '${visitbean.visit_type}';
		gDoctorId = '${visitbean.doctor}';
		gDeptId = '${visitbean.dept_id}';
		gOptype = '${visitbean.op_type}';
		gMainVisitId = '${visitbean.main_visit_id}';
		deptList =  <%= request.getAttribute("deptsList") %>;
		doctorsList = <%= request.getAttribute("doctorsList") %>;
	}
	var InsuranceModuleEnabled ='${preferences.modulesActivatedMap['mod_insurance']}';
</script>
</body>
</html>
