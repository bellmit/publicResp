
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<jsp:useBean id="preauthStatusDisplay" class="java.util.HashMap"/>
<c:set target="${preauthStatusDisplay}" property="O" value="Open"/>
<c:set target="${preauthStatusDisplay}" property="S" value="Sent"/>
<c:set target="${preauthStatusDisplay}" property="D" value="Denied"/>
<c:set target="${preauthStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${preauthStatusDisplay}" property="C" value="Closed"/>
<c:set target="${preauthStatusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="activityPreauthStatusDisplay" class="java.util.HashMap"/>
<c:set target="${activityPreauthStatusDisplay}" property="O" value="Open"/>
<c:set target="${activityPreauthStatusDisplay}" property="S" value="Sent"/>
<c:set target="${activityPreauthStatusDisplay}" property="D" value="Denied"/>
<c:set target="${activityPreauthStatusDisplay}" property="C" value="Approved"/>

<jsp:useBean id="denialCodeStatusDisplay" class="java.util.HashMap"/>
<c:set target="${denialCodeStatusDisplay}" property="A" value="Active"/>
<c:set target="${denialCodeStatusDisplay}" property="I" value="Retired"/>
<c:set var="overrideOnlinePriorAuthStatus" value="${actionRightsMap.override_online_prior_auth_status == 'A' || roleId == 1 || roleId ==2 ? 'A' : 'N'}"/>

<style type="text/css">
		.scrolForContainer .yui-ac-content{
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
		    _height:18em; max-width:30em; width:30em;/* ie6 */
		}
</style>

<html>
<head>
	<title>Prior Auth ${preauthPrescBean.map.preauth_presc_id == 0 ? '' : preauthPrescBean.map.preauth_presc_id} Prescription</title>
	<script>
		var collapsiblePanels = {};
		var centerId = ${centerId};
		var preauthStatus		= '${preauthPrescBean.map.preauth_status}';
		var gServerNow 	= new Date(<%= (new java.util.Date()).getTime() %>);
		var showChargesAllRatePlan = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
		var patientType = '${patient.visit_type}';
		var op_type = '${patient.op_type}';
		var tooth_numbering_system = '${genericPrefs.tooth_numbering_system}';
		var observationCodeTypesList =  <%= request.getAttribute("observationCodeTypeList") %>;
		var mrdSupportedCodeTypes = <%= request.getAttribute("mrdSupportedCodeTypes") %>;
		var TPArequiresPreAuth = '<%= request.getAttribute("TPArequiresPreAuth") %>' ;
		var TPAEAuthMode = '<%= request.getAttribute("TPAEAuthMode") %>';
		var allowPreauthPrescriptionEdit = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_preauth_prescription_edit}';
		// var phrase_suggestions_json = ${phrase_suggestions_json};
		var phrase_suggestions_by_dept_json = <%= request.getAttribute("phrase_suggestions_by_dept_json") %>;
		var complaintForm = null;
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var overrideOnlinePriorAuthStatus = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.override_online_prior_auth_status}';
		function init() {
				complaintForm = document.mainform;
			}
	</script>

	<script type="text/javascript">
		function validatePreauthPrescId() {
			var preauthPrescStr = document.preauthFindForm.preauth_presc_id.value;
			if (empty(preauthPrescStr) || !isInteger(preauthPrescStr)) {
				alert("Invalid Prior Auth presc. Id");
				document.preauthFindForm.preauth_presc_id.focus();
				return false;
			}
			return true;
		}
		
		function priorAuthPrint(printerId){
			var printerType = document.mainform.printType.value;
			var preauth_presc_id = document.mainform.preauth_presc_id.value;
			var insurance_co_id = document.mainform.insurance_co_id.value;
			var consultation_id = document.mainform.consultation_id.value;
			var patient_id = document.mainform.patient_id.value;
			var url = cpath + "/EAuthorization/EAuthPresc.do?_method=printEAuthPrescription";
			url += "&printerType="+printerType;
			url += "&preauth_presc_id="+preauth_presc_id;
			url += "&patient_id="+patient_id;
			url += "&consultation_id="+consultation_id;
			window.open(url);
		}
	</script>

	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="EAuthorization/eAuthPresc.js"/>
	<insta:link type="script" file="EAuthorization/eAuthPrescDialogs.js" />
	<insta:link type="script" file="outpatient/diagnosis_details.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="outpatient/insta_section.js"/>
	<script type="text/javascript">
		var complaintForm = null;
	</script>
	<!-- the below link has to be after the variable complaintForm gets the form name.
		complaintForm is initialized iniside the init method of prescribe.js-->
	<insta:link type="script" file="outpatient/complaint.js"/>

	<style>
		.complaintAc {
		    padding-bottom:2em;
		}

		.scrolForContainer .yui-ac-content{
			 max-height:300px;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
	<insta:js-bundle prefix="patient.diagnosis"/>
</head>

<body onload="initPreAuthPresc();" class="yui-skin-sam">
<c:choose>
	<c:when test="${param._method == 'getEAuthPrescriptionScreen'}">
		<table width="100%">
			<tr>
				<td width="100%"><h1 style="float: left">Add New Prior Auth Prescription</h1></td>
				<c:url var="searchUrl" value="/EAuthorization/EAuthPresc.do" />
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<table width="100%">
			<tr>
				<td width="100%"><h1 style="float: left">Prior Auth Prescription</h1></td>
			</tr>
		</table>
	</c:otherwise>
</c:choose>
<div><insta:feedback-panel/></div>
<div id="insurancePhotoDialog" style="display:none;visibility:hidden;" ondblclick="handleInsurancePhotoDialogCancel();">
	<div class="bd" id="bd2" style="padding-top: 0px;">
		<table  style="text-align:top;vetical-align:top;" width="100%">
			<tr>
				<td>
					<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
						<legend class="fieldSetLabel">Insurance Card</legend>
								<c:choose>
									<c:when test="${isInsuranceCardAvailable eq true }">
										<embed id="insuranceImage" height="450px" width="500px" style="overflow:auto"
											src="${cpath}/Registration/GeneralRegistrationPlanCard.do?_method=viewInsuranceCardImage&visitId=${preauthPrescBean.map.patient_id}"/>
									</c:when>
									<c:otherwise>
										No Insurance Card Available
									</c:otherwise>
								</c:choose>
					 </fieldset>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="Close" style="cursor:pointer;" onclick="handleInsurancePhotoDialogCancel();"/>
				</td>
			</tr>
		</table>
	</div>
</div>
<insta:patientdetails visitid="${not empty preauthPrescBean ? preauthPrescBean.map.patient_id : param.patient_id}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Other Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">

		<tr>
			<td class="formlabel">Emirates Id:</td>
			<td class="forminfo">${preauthPrescBean.map.emirates_id_number}</td>
			<td class="formlabel">Provider Id:</td>
			<td class="forminfo">${service_reg_no}</td>
			<c:choose>
				<c:when test="${isInsuranceCardAvailable eq true}">
					<td class="formlabel">View Insurance Card:</td>
					<td class="forminfo">
						<button id="_plan_card" title="Uploaded Insurance Card..." style="cursor:pointer;" onclick="javascript:showInsurancePhotoDialog();" type="button"> .. </button>
					</td>
				</c:when>
				<c:otherwise>
					<td></td>
					<td></td>
				</c:otherwise>
			</c:choose>
		</tr>
	</table>
</fieldset>
<form action="./EAuthPresc.do?_method=saveEAuthDetails" method="POST" name="mainform" enctype="multipart/form-data">
	<input type="hidden" name="_method" value="saveEAuthDetails" />
	<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

	<input type="hidden" name="preauth_presc_id" value="${preauthPrescBean.map.preauth_presc_id == 0 ? '' : preauthPrescBean.map.preauth_presc_id}" />
	<input type="hidden" name="consultation_id" value="${preauthPrescBean.map.consultation_id == 0 ? '' : preauthPrescBean.map.consultation_id}" />
	<input type="hidden" name="priority" id="priority" value="${preauthPrescBean.map.priority == 0 ? '' : preauthPrescBean.map.priority}" />
	<input type="hidden" name="dept" id="dept" value="${patient.dept_id}" />

	<input type="hidden" name="regDate" value="<fmt:formatDate value="${preauthPrescBean.map.reg_date}" pattern="dd-MM-yyyy"/>">
	<input type="hidden" name="regTime" value="<fmt:formatDate value="${preauthPrescBean.map.reg_time}" pattern="HH:mm"/>">

	<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}" />
	<input type="hidden" name="tpa_id" id="tpa_id" value="${preauthPrescBean.map.tpa_id}"/>
	<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}" />
	<input type="hidden" name="visit_type" id="visit_type" value="${patient.visit_type}" />
	<input type="hidden" name="plan_id" id="plan_id" value="${preauthPrescBean.map.plan_id}" />
	<input type="hidden" name="mr_no" id="mr_no" value="${preauthPrescBean.map.mr_no}" />
	<input type="hidden" name="insurance_co_id" id="insurance_co_id" value="${preauthPrescBean.map.insurance_co_id}" />

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Prior Auth Presc. Details</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">${preauthPrescBean.map.priority == 1 ? 'Primary' : 'Secondary'} TPA/Sponsor :</td>
			<td class="forminfo">${not empty preauthPrescBean.map.tpa_name ? preauthPrescBean.map.tpa_name : ''}</td>
			<td class="formlabel">${preauthPrescBean.map.priority == 1? 'Primary' : 'Secondary'} Insurance Co. :</td>
			<td class="forminfo">${not empty preauthPrescBean.map.insurance_co_name ? preauthPrescBean.map.insurance_co_name : ''}</td>
			<td class="formlabel">Prior Auth Presc. Id:</td>
			<td class="forminfo">${preauthPrescBean.map.preauth_presc_id == 0 ? '' : preauthPrescBean.map.preauth_presc_id}</td>
		</tr>
		<tr>
			<td class="formlabel">Prior Auth Status:</td>
			<td class="forminfo">
				<c:choose>
					<c:when test="${TPAEAuthMode == 'O'}">
					<c:choose>
					<c:when test="${preauthPrescBean.map.preauth_presc_id == 0 || overrideOnlinePriorAuthStatus == 'N' || preauthPrescBean.map.preauth_status == 'X' 
					   || preauthPrescBean.map.preauth_status == 'R' || preauthPrescBean.map.preauth_status == 'O'}">
						${preauthStatusDisplay[preauthPrescBean.map.preauth_status]}
				    </c:when>
				    <c:otherwise>
				    <insta:selectoptions name="manual_preauth_status" id="manual_preauth_status"
							 value="${preauthPrescBean.map.preauth_status}"
							 optexts="Sent,Denied,Closed"
							 opvalues="S,D,C"/>
					</c:otherwise>
					</c:choose>
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="manual_preauth_status" id="manual_preauth_status"
							 value="${preauthPrescBean.map.preauth_status}"
							 optexts="Open,Sent,Denied,ForResub,Closed"
							 opvalues="O,S,D,R,C"/>
					</c:otherwise>
				</c:choose>
			</td>
			<td class="formlabel">Prior Auth Request Id:</td>
			<td class="forminfo">${preauthPrescBean.map.preauth_request_id}</td>
			<td class="formlabel">Resubmission Request:</td>
			<td class="forminfo">${(not empty preauthPrescBean && not empty preauthPrescBean.map.preauth_request_id) ? ( preauthPrescBean.map.is_resubmit == 'Y' ? 'Yes' : 'No') : '' }</td>
		</tr>
		<c:if test="${TPAEAuthMode == 'M' || overrideOnlinePriorAuthStatus == 'A'}">
			<tr>
				<td class="formlabel">Prior Auth Id: </td>
				<td class="forminfo">
					<input type="text" name="copy_preauth_id" id="copy_preauth_id">
				</td>
				<td class="formlabel">Prior Auth Mode: </td>
				<td class="forminfo">
					<insta:selectdb  name="copy_preauth_mode" id="copy_preauth_mode" value="" table="prior_auth_modes"
						valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
						dummyvalue="-- Select --"/>
				</td>
				<td class="formlabel">Prior Auth Activity Status:</td>
				<td class="forminfo">
				<insta:selectoptions name="copy_preauth_act_status" id="copy_preauth_act_status"
					 value="" optexts="Open,Denied,Approved" opvalues="O,D,C" disabled="false"/>
				</td>
				
				<td class="formlabel"></td>
				<td class="forminfo">
					<button type="button" name="s_ed_copy_to_all_items" onclick="copyDialogToAllItems(this)">Copy to all charges</button>
				</td>
			</tr>
		</c:if>
		<tr>
			<c:if test="${not empty preauthPrescBean.map.validity_start_date && not empty preauthPrescBean.map.validity_end_date}" >
				<td class="formlabel">Valid From:</td>
				<!-- substring is used to display only DD/MM/YYYY and truncate HH:MM:SS -->
				<td class="forminfo">${fn:substring(preauthPrescBean.map.validity_start_date, 0, 10)}</td>
				<td class="formlabel">Valid To:</td>
				<td class="forminfo">${fn:substring(preauthPrescBean.map.validity_end_date, 0, 10)}</td>
			</c:if>
			<c:if test="${preauthPrescBean.map.preauth_status == 'D' || preauthPrescBean.map.preauth_status == 'R' || preauthPrescBean.map.preauth_status == 'C'}">		
				<td class ="formlabel">Comments:</td>
				<td class="forminfo"><insta:truncLabel value="${preauthPrescBean.map.approval_comments}" length="30"/></td>
			</c:if>
		</tr>
		<tr></tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Prior Auth Visit Details</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Encounter&nbsp;Type Code/Desc:</td>
			<td class="forminfo">
				<div id="encounterAuto" style="padding-bottom: 20px">
					<input type="text" name="encCode" id="encCode" value="${preauthPrescBean.map.encounter_type}" />
					<div id="encDropDown" class="scrolForContainer" style="width: 250px"></div>
				</div>
			</td>
			<td class="forminfo" style="white-space: nowrap; width: 250px">
				<insta:truncLabel id="encTypeCodeDesc" value="${preauthPrescBean.map.encounter_type_desc}" length="35"/></td>
				<input type="hidden" name="encTypeCodeDesc" value="${preauthPrescBean.map.encounter_type_desc}" />
			</td>
			<td class="formlabel">Encounter&nbsp;End:</td>
			<td colspan="2">
				<fmt:formatDate var="encenddate" value="${preauthPrescBean.map.preauth_enc_end_datetime}"
					pattern="dd-MM-yyyy" />
				<fmt:formatDate var="encendtime" value="${preauthPrescBean.map.preauth_enc_end_datetime}"
					pattern="HH:mm" />
				<insta:datewidget name="preauth_enc_end_date"
						value="${encenddate}" btnPos="right" />
				<input type="text" name="preauth_enc_end_time" value="${encendtime}" class="timefield" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Perdiem Code/Desc:</td>
			<td class="forminfo">
				<div id="perdiemAuto" style="padding-bottom: 20px">
					<input type="text" name="perdiemCode" id="perdiemCode" value="${perdiemCode.act_code}"
					${(empty preauthPrescBean.map.preauth_status || preauthPrescBean.map.preauth_status eq 'O') ? '' : 'disabled'}/>
					<div id="perdiemDropDown" class="scrolForContainer" style="width: 250px"></div>
				</div>
			</td>
			<td colspan="2" class="forminfo"  style="white-space: nowrap;">
				<label id="perdiemCodeDesc">
					<insta:truncLabel value="${perdiemCode.code_master_desc}" length="50"/>
				</label>
				<input type="hidden" name="perdiemCodeDesc" value="${perdiemCode.code_master_desc}" />

				<input type="hidden" name="perdiem_act_id" value="${perdiemCode.preauth_act_id}"/>
				<input type="hidden" name="perdiem_code" value="${perdiemCode.act_code}"/>
			</td>
				<td class="formlabel">Perdiem Code Type:</td>
				<td class="forminfo"><label id="perdiemCodeType">${perdiemCode.act_code_type}</label>
					<input type="hidden" name="perdiem_code_type" value="${perdiemCode.act_code_type}"/>
				</td>
		</tr>

		<tr>
			<td class="formlabel">Perdiem Net:</td>
			<td class="forminfo">
				<input type="text" class="number" name="perdiem_net" id="perdiem_net" value="${perdiemCode.claim_net_amount}" />
			</td>
			<td class="formlabel">Perdiem Apprd.:</td>
			<td class="forminfo">${perdiemCode.claim_net_approved_amount}</td>
			<td class="formlabel">Perdiem Denial Code/Type:</td>
			<td class="forminfo">${perdiemCode.denial_code} ${perdiemCode.denial_code_type}</td>
		</tr>
		<tr>
			<td class="formlabel">Perdiem Status:</td>
			<td class="forminfo">${activityPreauthStatusDisplay[perdiemCode.preauth_act_status]}</td>
			<td class="formlabel">Perdiem Prior Auth Id:</td>
			<td class="forminfo">
				<input type="text" name="perdiem_preauth_id" id="perdiem_preauth_id" value="${perdiemCode.preauth_id}" />
			</td>
			<td class="formlabel">Perdiem Prior Auth Mode:</td>
			<td class="forminfo">
				<insta:selectdb  name="perdiem_preauth_mode" id="perdiem_preauth_mode" value="${perdiemCode.preauth_mode}" table="prior_auth_modes"
					valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
					dummyvalue="-- Select --"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">DRG Code/Desc:</td>
			<td class="forminfo">
				<div id="drgAuto" style="padding-bottom: 20px">
					<input type="text" name="drgCode" id="drgCode" value="${drgCode.act_code}"
					${(empty preauthPrescBean.map.preauth_status || preauthPrescBean.map.preauth_status eq 'O') ? '' : 'disabled'}/>
					<div id="drgDropDown" class="scrolForContainer" style="width: 250px"></div>
				</div>
			</td>
			<td colspan="2" class="forminfo" style="white-space: nowrap;">
				<label id="drgCodeDesc">
					<insta:truncLabel value="${drgDescription}" length="50"/>
				</label>
				<input type="hidden" name="drgCodeDesc" value="${ifn:cleanHtmlAttribute(drgDescription)}" />

				<input type="hidden" name="drg_act_id" value="${drgCode.preauth_act_id}"/>
				<input type="hidden" name="drg_code" value="${drgCode.act_code}"/>
			</td>
			<td class="formlabel">DRG Code Type:</td>
			<td class="forminfo"><label id="drgCodeType">${drgCode.act_code_type}</label>
				<input type="hidden" name="drg_code_type" value="${drgCode.act_code_type}"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">DRG Net:</td>
			<td class="forminfo">
				<input type="text" class="number" name="drg_net" id="drg_net" value="${drgCode.claim_net_amount}" />
			</td>
			<td class="formlabel">DRG Apprd.:</td>
			<td class="forminfo">${drgCode.claim_net_approved_amount}</td>
			<td class="formlabel">DRG Denial Code/Type:</td>
			<td class="forminfo">${drgCode.denial_code} ${drgCode.denial_code_type}</td>
		</tr>
		<tr>
			<td class="formlabel">DRG Status:</td>
			<td class="forminfo">${activityPreauthStatusDisplay[drgCode.preauth_act_status]}</td>
			<td class="formlabel">DRG Prior Auth Id:</td>
			<td class="forminfo">
				<input type="text" name="drg_preauth_id" id="drg_preauth_id" value="${drgCode.preauth_id}" />
			</td>
			<td class="formlabel">DRG Prior Auth Mode:</td>
			<td class="forminfo">
				<insta:selectdb  name="drg_preauth_mode" id="drg_preauth_mode" value="${drgCode.preauth_mode}" table="prior_auth_modes"
					valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
					dummyvalue="-- Select --"/>
			</td>
		</tr>
	</table>
</fieldset>
<c:forTokens items="${form.map.sections}" delims="," var="formid">
	<c:if test="${formid == -1}">
	<%-- Here -1 is the id of Complaint form ID --%>
		<jsp:include page="ComplaintInclude.jsp">
			<jsp:param name="section_id" value="${formid}"/>
			<jsp:param name="form_name" value="mainform"/>
		</jsp:include>
	</c:if>
</c:forTokens>
<c:choose>
	<c:when test="${patient.op_type == 'O'}">
		<input type="hidden" id="diagnosis_details_exists" value="true"/>
			<jsp:include page="/pages/outpatient/DiagnosisDetailsInclude.jsp">
			<jsp:param name="form_name" value="mainform"/>
			<jsp:param name="displayPrvsDiagnosisBtn" value="false"/>
		</jsp:include>
	</c:when>
	<c:otherwise>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Diagnosis Details</legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
				<c:choose>
					<c:when test="${not empty diagnosisList}">
						<c:forEach items="${diagnosisList}" var="diag">
							<tr>
								<td class="formlabel">Diagnosis Type:</td>
								<td class="forminfo">${diag.map.diag_type} (${diag.map.code_type})</td>
								<td class="formlabel">Diagnosis Code:</td>
								<td class="forminfo">${diag.map.icd_code}</td>
								<td class="formlabel">Diagnosis Desc:</td>
								<td class="forminfo">
									<div title="${diag.map.code_desc}">
									<insta:truncLabel value="${diag.map.code_desc}" length="60"/>
									</div>
								</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:otherwise>
							<tr>
								<td></td>
								<td colspan="2" class="forminfo">No diagnosis codes available.</td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
					</c:otherwise>
				</c:choose>
			</table>
		</fieldset>
	</c:otherwise>
</c:choose>

<c:set var="totalAmount" value="0"/>
<c:set var="totalDiscount" value="0"/>
<c:set var="totalGrossAmount" value="0"/>
<c:set var="totalPatientAmount" value="0"/>
<c:set var="totalClaimNetAmount" value="0"/>
<c:set var="totalApprovedNetAmount" value="0"/>

	<!-- starts HERE -->
	<div class="resultList">
		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0"
				id="siTable" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
			<tr>
				<th>#</th>
				<th>Presc Date</th>
				<th></th>
				<th>Type</th>
				<th>Name</th>
				<th>Tooth#</th>
				<th>Remarks</th>
				<th>CodeType</th>
				<th>Code</th>
				<th>PriorAuthId</th>
				<th>Mode</th>
				<th class="number">Rate</th>
				<th class="number">Qty</th>
				<th class="number">Rem Qty</th>
				<th class="number">Disc</th>
				<th class="number">Gross</th>
				<th class="number">Patient</th>
				<th class="number">Net</th>
				<th style="width: 16px"></th>
				<th style="width: 16px"></th>
				<th class="number">Apprd Qty</th>
				<th class="number" title="Approved Net">Apprd.</th>
				<th>Status</th>
				<th>Denial Code</th>
				<th>Code Type</th>
				<th>Code Status</th>
				<th>Upload Attachment</th>
				<th>View Attachment</th>
				<th>Delete Attachment</th>
			</tr>
			<c:set var="numPrescriptions" value="${fn:length(prescActivitiesList)}"/>
			<c:set var="fileIndex" value="0"/>
			<c:forEach begin="1" end="${numPrescriptions+1}" var="i" varStatus="status">
				<c:set var="prescActivity" value="${prescActivitiesList[i-1]}"/>

				<c:set var="activity" value="${prescActivity.activity}"/>
				<c:set var="observations" value="${prescActivity.observations}"/>

				<c:set var="flagColor">
					<c:choose>
						<c:when test="${activity.preauth_act_status eq 'O'}">empty</c:when>
						<c:when test="${activity.preauth_act_status eq 'D'}">red</c:when>
						<c:when test="${activity.preauth_act_status eq 'C'}">grey</c:when>
						<c:when test="${activity.preauth_act_status eq 'S'}">violet</c:when>
					</c:choose>
				</c:set>
				<c:set var="priorAuthFlagColor">
					<c:choose>
						<c:when test="${activity.preauth_required eq 'Y'}">request</c:when>
						<c:when test="${activity.preauth_required eq 'N' || activity.preauth_required eq '' || activity.preauth_required eq ' '}">request1</c:when>
					</c:choose>
				</c:set>
				<c:if test="${empty activity}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<c:set var="priorAuth" value="${not empty patient.primary_sponsor_id ? activity.preauth_req_master : ''}"/>

				<c:set var="preauthActivityType">
					<c:choose>
						<c:when test="${activity.preauth_act_type eq 'DIA'}">Investigation</c:when>
						<c:when test="${activity.preauth_act_type eq 'SER'}">Service</c:when>
						<c:when test="${activity.preauth_act_type eq 'OPE'}">Operation</c:when>
						<c:when test="${activity.preauth_act_type eq 'DOC'}">Doctor</c:when>
						<c:when test="${activity.preauth_act_type eq 'ITE'}">Inventory</c:when>
					</c:choose>
				</c:set>

				<tr ${style}>
					<c:set var="rowindex" value="${status.index}"/>
					<td>${status.index}</td>
					<td>
						<fmt:formatDate pattern="dd-MM-yyyy" value="${activity.prescribed_date}" var="prescribed_date"/>
						<label>${prescribed_date}</label>
						<input type="hidden" name="s_prescribed_date" value="${prescribed_date}"/>
						<fmt:formatDate pattern="HH:mm:ss" value="${activity.prescribed_date}" var="prescribedTime"/>
						<input type="hidden" name="s_prescribedTime" value="${prescribedTime}"/>
						<input type="hidden" name="s_preauth_act_id" value="${activity.preauth_act_id}"/>
						<input type="hidden" name="s_item_name" value="<c:out value='${activity.preauth_act_name}'/>"/>
						<input type="hidden" name="s_item_id" value="${activity.preauth_act_item_id}"/>
						<input type="hidden" name="s_item_remarks" value="${activity.preauth_act_item_remarks}"/>
						<input type="hidden" name="s_item_master" value="${activity.master}"/>

						<input type="hidden" name="s_ispackage" id="s_ispackage" value="${activity.ispackage}"/>
						<input type="hidden" name="s_issued" value="${activity.added_to_bill}"/>

						<c:set var="activity_due_date" value=""/>
						<c:if test="${not empty activity.activity_due_date}">
							<fmt:formatDate value="${activity.activity_due_date}" pattern="dd-MM-yyyy HH:mm" var="activity_due_date"/>
						</c:if>
						<input type="hidden" name="s_activity_due_date" value="${activity_due_date}"/>
						<input type="hidden" name="s_addActivity" value="${not empty activity_due_date}"/>

						<input type="hidden" name="s_delItem" id="s_delItem" value="${activity.status == 'X' }" />
						<input type="hidden" name="s_itemType" value="${activity.preauth_act_type}"/>
						<input type="hidden" name="s_edited" value='false'/>

						<input type="hidden" name="s_status" id="s_status" value="${activity.status}" />

						<input type="hidden" name="s_priorAuth" value="${priorAuth}" />
						<input type="hidden" name="s_preauth_required" value="${activity.preauth_required}" />

						<input type="hidden" name="s_preauth_status" value="${preauthPrescBean.map.preauth_status}" />
						<input type="hidden" name="s_preauth_act_status" value="${activity.preauth_act_status}" />

						<input type="hidden" name="s_item_code_type" value="${activity.act_code_type}"/>
						<input type="hidden" name="s_item_code" value="${activity.act_code}"/>
						<input type="hidden" name="s_item_code_desc" value="${activity.code_master_desc}"/>

						<input type="hidden" name="s_item_qty" value="${activity.act_qty}"/>
						<input type="hidden" name="s_item_rem_qty" value="${activity.rem_qty}"/>
						<input type="hidden" name="s_orig_item_rate" value="${activity.rate}" />
						<input type="hidden" name="s_item_rate" value="${activity.rate}" />
						<input type="hidden" name="s_item_disc" value="${activity.discount}" />
						<input type="hidden" name="s_item_amount" value="${activity.amount}"/>
						<input type="hidden" name="s_orig_item_amount" value="${activity.amount}"/>
						<input type="hidden" name="s_patient_amount" value="${activity.patient_share}"/>
						<input type="hidden" name="s_claim_net_amount" value="${activity.claim_net_amount}"/>
						<input type="hidden" name="s_orig_claim_net_amount" value="${activity.claim_net_amount}"/>
						<input type="hidden" name="s_claim_approved_amount" value="${activity.claim_net_approved_amount}"/>
						<input type="hidden" name="s_added_to_bill" value="${activity.added_to_bill}"/>

						<input type="hidden" name="tooth_unv_number" value="${activity.tooth_unv_number}"/>
						<input type="hidden" name="tooth_fdi_number" value="${activity.tooth_fdi_number}"/>
						<input type="hidden" name="tooth_num_required" value="${activity.tooth_num_required}"/>

						<input type="hidden" name="s_doc_cons_type" value="${activity.doc_cons_type}"/>

						<input type="hidden" name="s_del_attach" value="false"/>

						<input type="hidden" name="s_item_preauth_id" value="${activity.preauth_id}"/>
						<%-- <input type="hidden" name="s_item_preauth_mode" value="${activity.preauth_mode}"/> --%>
						<c:if test="${TPAEAuthMode == 'O' || overrideOnlinePriorAuthStatus == 'N'}">
						<input type="hidden" name="s_item_preauth_mode" value="4"/>
						</c:if>
						<c:if test="${TPAEAuthMode == 'M' || overrideOnlinePriorAuthStatus == 'A'}">
						<input type="hidden" name="s_item_preauth_mode" value="${activity.preauth_mode}"/>
						</c:if>
						<input type="hidden" name="s_item_approved_qty" value="${activity.approved_qty}"/>
						<input type="hidden" name="s_item_approved_rem_qty" value="${activity.rem_approved_qty}"/>

						<input type="hidden" name="s_item_intial_apprvd_qty" value="${activity.approved_qty}"/>
						<input type="hidden" name="s_item_intial_approved_rem_qty" value="${activity.rem_approved_qty}"/>
						<input type="hidden" name="s_item_patient_presc_id" value="${activity.patient_pres_id}"/>

						<input type="hidden" name="obserActItemId" value="${activity.preauth_act_item_id}" />
						<div id="observations.${activity.preauth_act_item_id}" style="display: inline">
							<c:set var="obsCnt" value="0"/>
							<c:forEach items="${observations}" varStatus="obsStatus" var="obs">
							   <c:set var="obsCnt" value="${obsCnt+1}"/>
								<input type="hidden"  name="obserCode.${activity.preauth_act_item_id}" id="obserCode.${activity.preauth_act_item_id}${obsCnt}" value="${obs.code}"/>
								<input type="hidden" name="obserType.${activity.preauth_act_item_id}" id="obserType.${activity.preauth_act_item_id}${obsCnt}" value="${obs.obs_type}"/>
								<input type="hidden" name="obserValue.${activity.preauth_act_item_id}" id="obserValue.${activity.preauth_act_item_id}${obsCnt}" value="${obs.value}"/>
								<input type="hidden" name="obserValueType.${activity.preauth_act_item_id}" id="obserValueType.${activity.preauth_act_item_id}${obsCnt}" value="${obs.value_type}"/>
								<input type="hidden" name="obserCodeDesc.${activity.preauth_act_item_id}" id="obserCodeDesc.${activity.preauth_act_item_id}${obsCnt}" value="${obs.code_desc}"/>
							</c:forEach>
							<input type="hidden" id="obserIndex.${activity.preauth_act_item_id}" value="${obsCnt}"/>
						</div>
					</td>
					<td><img src="${cpath}/images/${priorAuthFlagColor}.png"/></td>
					<td><insta:truncLabel value="${preauthActivityType}" length="5"/></td>
					<td><insta:truncLabel value="${activity.preauth_act_name}" length="12"/></td>
					<td><label>${activity.tooth_unv_number}</label></td>
					<td title="${activity.preauth_act_item_remarks}">
						<insta:truncLabel value="${activity.preauth_act_item_remarks}" length="10"/>
					</td>
					<td><label>${activity.act_code_type}</label></td>
					<td><label>${activity.act_code}</label></td>
					<td><label>${activity.preauth_id}</label></td>
					<td>
					<%-- <label>${activity.prior_auth_mode_name}</label> --%>
					<c:if test="${TPAEAuthMode == 'O'}">
					<c:set var="eAuthMode" value="Electronic"></c:set>
					</c:if>
					<c:if test="${TPAEAuthMode == 'M' || overrideOnlinePriorAuthStatus == 'A'}">
					<c:set var="eAuthMode" value="${activity.prior_auth_mode_name}"></c:set>
					</c:if>
					<label>${eAuthMode}</label>
					</td>
					<td class="number"><label>${activity.rate}</label></td>
					<td class="number"><label>${activity.act_qty}</label></td>
					<td class="number"><label>${activity.rem_qty}</label></td>
					<td class="number"><label>${activity.discount}</label></td>
					<td class="number"><label>${activity.amount}</label></td>
					<td class="number"><label>${activity.patient_share}</label></td>
					<td class="number"><label>${activity.claim_net_amount}</label></td>
					<td style="text-align: center; ">
						<c:choose>
							<c:when test="${not empty preauthPrescBean.map.preauth_presc_id && preauthPrescBean.map.preauth_presc_id != 0 && preauthPrescBean.map.preauth_status ne 'O'  && (activity.act_qty != activity.rem_qty)}">
								<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button" />
							</c:when>
							<c:otherwise>
								<c:choose>
									<c:when test="${activity.status == 'X'}">
										<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelSIItem(this);" title="Cancel Item">
											<img src="${cpath}/icons/undo_delete.gif" class="imgDelete button" />
										</a>
									</c:when>
									<c:otherwise>
										<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelSIItem(this);" title="Cancel Item">
											<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
										</a>
									</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align: center">
						<a name="si_editAnchor" href="javascript:Edit" onclick="return showEditSIDialog(this);" title="Edit Item Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
					<td class="number"><label>${activity.approved_qty}</label></td>
					<td class="number"><label>${activity.claim_net_approved_amount}</label></td>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						${activityPreauthStatusDisplay[activity.preauth_act_status]}
					</td>
					<td>${activity.denial_code}</td>
					<td>${activity.denial_code_type}</td>
					<td>${denialCodeStatusDisplay[activity.denial_code_status]}</td>

					<td>
						<input type="file" id="activity_file_upload${fileIndex}" name="activity_file_upload[${fileIndex}]"
							class="preAuthFileUpload"  onchange="enableDelCheck(this)" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
						<c:set var="fileIndex" value="${fileIndex+1}"/>
					</td>

					<td>
						<c:choose>
							<c:when test="${!(activity.attachment_size > 0)}">
								<c:set var="btndisabled" value="disabled"/>
							</c:when>
							<c:otherwise>
								<c:set var="btndisabled" value=""/>
							</c:otherwise>
						</c:choose>

						<button type="button" name="viewAttachment" accesskey="V" value="" ${btndisabled} onclick="showAttachment(this);">
							<b><u>V</u></b>iew
						</button>
						<insta:truncLabel value="${activity.file_name}" length="15"/>
					</td>
					<td>
						<c:choose>
							<c:when test="${!(activity.attachment_size > 0)}">
								<c:set var="chkdisabled" value="disabled"/>
							</c:when>
							<c:otherwise>
								<c:set var="chkdisabled" value=""/>
							</c:otherwise>
						</c:choose>

						<input type="checkbox" name="delAttach"  ${chkdisabled} onclick="setDelAttachHiddenField(this);"/>
					</td>

					<c:set var="totalAmount" value="${totalAmount + activity.amount + activity.discount}"/>
					<c:set var="totalDiscount" value="${totalDiscount + activity.discount}"/>
					<c:set var="totalGrossAmount" value="${totalGrossAmount + activity.amount}"/>
					<c:set var="totalPatientAmount" value="${totalPatientAmount + activity.patient_share}"/>
					<c:set var="totalClaimNetAmount" value="${totalClaimNetAmount + activity.claim_net_amount}"/>
					<c:set var="totalApprovedNetAmount" value="${totalApprovedNetAmount + activity.claim_net_approved_amount}"/>

				</tr>
			</c:forEach>
		</table>
		<table class="addButton">
			<tr>
				<td></td>
				<td width="16px" style="text-align: center">
					<c:choose>
						<c:when test="${not empty preauthPrescBean.map.preauth_presc_id && preauthPrescBean.map.preauth_presc_id != 0 && preauthPrescBean.map.preauth_status ne 'O'}">
							<button type="button" name="btnAddItem" id="btnAddItem"
								title="Add Prior Auth Prescription (Alt_Shift_O)"
								accesskey="O" class="imgButton"><img src="${cpath}/icons/Add1.png"></button>
						</c:when>
						<c:otherwise>
							<button type="button" name="btnAddItem" id="btnAddItem" title="Add Prior Auth Prescription (Alt_Shift_O)"
								onclick="showAddSIDialog(this); return false;"
								accesskey="O" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>
	</div>
	<br/>
	<fieldset class="fieldSetBorder">
	  <legend class="fieldSetLabel">Prior Auth Presc. Totals</legend>
		<table width="840" align="left" class="infotable">
	   	<tr>
	   		<td class="formlabel">Total Amt:</td>
				<td class="forminfo" align="right">
					<label id="lblAmount">${totalAmount}</label>
				</td>

				<td class="formlabel">Total Discount:</td>
				<td class="forminfo" align="right">
					<label id="lblDiscount">${totalDiscount}</label>
				</td>

				<td class="formlabel">Total Gross Amt:</td>
				<td class="forminfo" align="right">
					<label id="lblGrossAmount">${totalGrossAmount}</label>
				</td>

				<td class="formlabel">Total Patient Amt:</td>
				<td class="forminfo" align="right">
					<label id="lblPatientAmt">${totalPatientAmount}</label>
				</td>

				<td class="formlabel">Total Claim Net Amt:</td>
				<td class="forminfo" align="right">
					<label id="lblClaimNetAmt">${totalClaimNetAmount}</label>
				</td>
				<td></td>
				<td></td>
		   	</tr>
			<tr>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td class="formlabel">Total Approved Net Amt:</td>
				<td class="forminfo" align="right">
					<label id="lblApprovedAmount">${totalApprovedNetAmount}</label>
				</td>
				<td></td>
				<td></td>
		   	</tr>
		 </table>
	</fieldset>

<c:if test="${TPAEAuthMode == 'O'}">
	<fieldset class="fieldSetBorder">
	<table class="formtable" align="center">
		<tr>
			<td class="formlabel">Resubmit type:</td>
			<td class="forminfo">
				<c:if test="${not empty preauthPrescBean.map.preauth_status}">
				<c:choose>
					<c:when test="${preauthPrescBean.map.preauth_status ne 'R'}">
						<insta:selectoptions name="_resubmit_type" id="_resubmit_type"
							 value="${preauthPrescBean.map.resubmit_type}" dummyvalue="-- Select --"
							 optexts="correction,internal complaint,legacy"
							 opvalues="correction,internal complaint,legacy" disabled="true"/>
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="_resubmit_type" id="_resubmit_type"
							 value="${preauthPrescBean.map.resubmit_type}" dummyvalue="-- Select --"
							 optexts="correction,internal complaint,legacy"
							 opvalues="correction,internal complaint,legacy"/>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="_old_resubmit_type" id="_old_resubmit_type"
						value="${preauthPrescBean.map.resubmit_type}" />
				</c:if>
			</td>
			<td class="formlabel">Comments:</td>
			<td class="forminfo">
				<textarea name="_comments" id= "_comments" title="Comments for resubmission" rows="2" cols="60"
					${(not empty preauthPrescBean.map.preauth_status && (preauthPrescBean.map.preauth_status ne 'C')) ? 'disabled' :''}
					>${preauthPrescBean.map.comments}</textarea>
				<textarea style="display:none" name="_old_comments"
					id="_old_comments">${preauthPrescBean.map.comments}</textarea>
			</td>
		</tr>
	</table>
	</fieldset>
</c:if>

	<table class="screenActions" width="100%">
	<tr>
		<td align="left"><c:if test="${preauthPrescBean.map.preauth_status != 'S' || overrideOnlinePriorAuthStatus == 'A' || TPAEAuthMode == 'M'}">
			<button type="button" name="actionBtn" value="save"
				${(preauthPrescBean.map.preauth_status != 'X') ? '' : 'disabled'}
				accessKey="S" onclick="return doSave();"><b><u>S</u></b>ave Prior Auth</button>
			<label>|</label>
			</c:if>
		<c:if test="${TPAEAuthMode == 'O'}">
			<c:choose>
				<c:when test="${preauthPrescBean.map.preauth_status eq 'O'}">
				</c:when>
				<c:when test="${preauthPrescBean.map.preauth_status eq 'S'}">
				</c:when>
				<c:when test="${preauthPrescBean.map.preauth_status eq 'D' || preauthPrescBean.map.preauth_status eq 'C'}">
					<input type="checkbox" name="actionChk" id="actionChk" onclick="enableResubmissionFields();"
						value="markForResubmission" accesskey="M">
					<input type="hidden" name="markResubmit" id="markResubmit" value=""/>
					<label><b><u>M</u></b>ark for Resubmission</label> &nbsp;
					<label>|</label>
						<a href="./EAuthPrescAttachFile.do?_method=addOrEditAttachment&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}">Add/Edit Attachment</a>
						<label>|</label>
				</c:when>
			</c:choose>
		</c:if>

			<c:if test="${not empty preauthPrescBean.map.preauth_presc_id && preauthPrescBean.map.preauth_presc_id != 0}">
			 <c:if test="${TPAEAuthMode == 'O'}">
				<c:if test="${preauthPrescBean.map.preauth_status eq 'O'}">
					<button type="button" value="Send"
					accessKey="R" onclick="return sendEAuthRequest();">Send Prior Auth <b><u>R</u></b>equest</button>
					<label>|</label>
				</c:if>
				<c:if test="${preauthPrescBean.map.preauth_status != 'X' && preauthPrescBean.map.preauth_status != 'O'
					&& (not empty approval_bean && approval_bean.map.approval_status != 'R')}">
					<button type="button" value="Cancel"
					accessKey="C" onclick="return cancelEAuthRequest();"><b><u>C</u></b>ancel Prior Auth Request</button>
					<label>|</label>
				</c:if>
				<c:if test="${preauthPrescBean.map.preauth_status eq 'R' || preauthPrescBean.map.preauth_status eq 'C'}">
					<button type="button" value="Resubmit"
					accessKey="C" onclick="return resubmitEAuthRequest();"><b><u>R</u></b>esubmit Prior Auth Request</button>
					<label>|</label>
				</c:if>
					<a href="${cpath}/EAuthorization/EAuthRequest.do?_method=viewEAuthRequestXML
						&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}
						&priority=${preauthPrescBean.map.priority}">View Prior Auth Request XML</a>
 				<label>|</label>
				<a href="${cpath}/EAuthorization/EAuthRequest.do?_method=downloadXMLResponseFile
						&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}&preauth_request_id=${preauthPrescBean.map.preauth_request_id}
						&download_key=all">Download All Prior Auth Response XML Files</a>
				<label>|</label>
			</c:if>
			</c:if>
			<a href="./EAuthPrescList.do?_method=getList">Prior Auth Prescriptions List</a>
			</td>
			<!-- print label --->
			<td align="right">
				<insta:selectdb name="printType" table="printer_definition"
				valuecol="printer_id"  displaycol="printer_definition_name" orderby="printer_definition_name"
				value="${pref.map.printer_id}"/>
			
			<button type="button" id="printButton" accessKey="P" onclick ="return priorAuthPrint();" >
			<b><u><insta:ltext key="prior.EAuthPrescription.details.p"/></u></b><insta:ltext key="prior.EAuthPrescription.details.rint"/></button>
		</td>
	</tr>
	</table>

	<%@ include file="/pages/EAuthorization/EAuthPrescDialogs.jsp" %>
	
	<div class="legend">
		<div class="flag"><img src='${cpath}/images/request.png'></div>
		<div class="flagText">Send for Prior auth</div>
		<div class="flag"><img src='${cpath}/images/request1.png'></div>
		<div class="flagText">Not Send for Prior auth</div>
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Denied</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Approved</div>
	</div>
	<jsp:include page="/pages/outpatient/InstaSectionDialogsInclude.jsp"/>
</form>
</body>
</html>
