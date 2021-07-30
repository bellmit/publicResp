<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="pbmStatusDisplay" class="java.util.HashMap"/>
<c:set target="${pbmStatusDisplay}" property="O" value="Open"/>
<c:set target="${pbmStatusDisplay}" property="S" value="Sent"/>
<c:set target="${pbmStatusDisplay}" property="D" value="Denied"/>
<c:set target="${pbmStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${pbmStatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="activityPbmStatusDisplay" class="java.util.HashMap"/>
<c:set target="${activityPbmStatusDisplay}" property="O" value="Open"/>
<c:set target="${activityPbmStatusDisplay}" property="D" value="Denied"/>
<c:set target="${activityPbmStatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="denialCodeStatusDisplay" class="java.util.HashMap"/>
<c:set target="${denialCodeStatusDisplay}" property="A" value="Active"/>
<c:set target="${denialCodeStatusDisplay}" property="I" value="Retired"/>

<jsp:useBean id="erxApprovalStatus" class="java.util.HashMap"/>
<c:set target="${erxApprovalStatus}" property="F" value="Fully Approved"/>
<c:set target="${erxApprovalStatus}" property="R" value="Fully Rejected"/>

<html>
<head>
	<title>PBM ${pbmPrescBean.map.pbm_presc_id == 0 ? '' : pbmPrescBean.map.pbm_presc_id} Prescription</title>
	<script>
		var mod_eclaim_erx = '${preferences.modulesActivatedMap.mod_eclaim_erx}';
		var mod_eclaim_pbm = '${preferences.modulesActivatedMap.mod_eclaim_pbm}';
		var centerId = ${centerId};
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var prescriptions_by_generics = '${prescriptions_by_generics}';
		var pbmStatus		= '${pbmPrescBean.map.pbm_presc_status}';
		var deptId 			= '${pharmacyStoreId}';
		var medDosages 	= ${medDosages};
		var useGenerics 	= (use_store_items == 'Y' && prescriptions_by_generics == 'true');
		var gServerNow 	= new Date(<%= (new java.util.Date()).getTime() %>);
		var showChargesAllRatePlan = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
		var pbmObservations = ${pbmObservations};
		var jStores = <%= request.getAttribute("storesJSON") %>;
		var itemFormList = <%= request.getAttribute("itemFormList") %>;
	</script>

	<script type="text/javascript">
		function validatePrescId() {
			var prescStr = document.pbmPrescFindForm.pbm_presc_id.value;
			if (empty(prescStr) || !isInteger(prescStr)) {
				alert("Invalid PBM presc. Id");
				document.pbmPrescFindForm.pbm_presc_id.focus();
				return false;
			}
			return true;
		}
	</script>

	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="PBMAuthorization/pbmPresc.js"/>
	<insta:link type="script" file="PBMAuthorization/pbmPrescDialogs.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<style>
		.scrolForContainer .yui-ac-content{
			 max-height:300px;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
</head>

<body onload="initPBMPresc();" class="yui-skin-sam">

<c:choose>
	<c:when test="${param._method == 'getPBMPrescriptionScreen'}">
		<h1 style="float: left">Add New PBM Prescription</h1>
		<c:url var="searchUrl" value="/PBMAuthorization/PBMPresc.do" />
		<insta:patientsearch searchType="visit" searchUrl="${searchUrl}" visitType="o"
		buttonLabel="Find" searchMethod="getPBMPrescriptionScreen" fieldName="patient_id"/>
	</c:when>
	<c:otherwise>
		<form name="pbmPrescFindForm" action="./PBMPresc.do" onsubmit="return validatePrescId();">
			<input type="hidden" name="_method" value="getPBMPrescription">
			<table width="100%">
				<tr>
					<td width="100%"><h1>PBM Prescription</h1></td>
					<td>PBM&nbsp;Presc.&nbsp;Id:&nbsp;</td>
					<td><input type="text" name="pbm_presc_id" id="pbm_presc_id" style="width: 80px"></td>
					<td><input type="submit" class="button" value="Find"></td>
				</tr>
			</table>
		</form>
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
											src="${cpath}/Registration/GeneralRegistrationPlanCard.do?_method=viewInsuranceCardImage&visitId=${pbmPrescBean.map.patient_id}"/>
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
<insta:patientdetails visitid="${not empty pbmPrescBean ? pbmPrescBean.map.patient_id : param.patient_id}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Other Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Emirates Id:</td>
			<td class="forminfo">${pbmPrescBean.map.emirates_id_number}</td>
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
		<tr>
			<td class="formlabel">Encounter Type:</td>
			<td class="forminfo" colspan="2">${pbmPrescBean.map.encounter_type_desc}</td>
		</tr>
	</table>
</fieldset>

<form action="./PBMPresc.do" method="POST" name="mainform">
	<input type="hidden" name="_method" value="savePBMDetails" />
	<input type="hidden" name="pbm_presc_id" value="${pbmPrescBean.map.pbm_presc_id == 0 ? '' : pbmPrescBean.map.pbm_presc_id}" />
	<input type="hidden" name="consultation_id" value="${pbmPrescBean.map.consultation_id == 0 ? '' : pbmPrescBean.map.consultation_id}" />
	<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}" />
	<input type="hidden" name="tpa_id" id="tpa_id" value="${patient.primary_sponsor_id}"/>
	<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}" />
	<input type="hidden" name="visit_type" id="visit_type" value="${patient.visit_type}" />
	<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}" />
	<input type="hidden" name="plan_id" id="plan_id" value="${patient.plan_id}" />

<c:if test="${mod_eclaim_erx}">
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">ERx Request Information</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">ERx Presc. Id:</td>
			<td class="forminfo">${pbmPrescBean.map.erx_presc_id}</td>
			<td class="formlabel">ERx Reference No.:</td>
			<td class="forminfo">
				${pbmPrescBean.map.erx_reference_no}
				<input type="hidden" id=erx_presc_id name="erx_presc_id" value="${pbmPrescBean.map.erx_presc_id}"/>
				<input type="hidden" id="erx_reference_no" name="erx_reference_no" value="${pbmPrescBean.map.erx_reference_no}"/>
				<input type="hidden" id="erx_approval_status" name="erx_approval_status" value="${pbmPrescBean.map.erx_approval_status}"/>
			</td>
			<td class="formlabel">ERx Approval Status:</td>
			<td class="forminfo">${erxApprovalStatus[pbmPrescBean.map.erx_approval_status]}</td>
		</tr>
	</table>
</fieldset>
</c:if>
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">PBM Presc. Details</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">PBM Presc. Id:</td>
			<td class="forminfo">${pbmPrescBean.map.pbm_presc_id == 0 ? '' : pbmPrescBean.map.pbm_presc_id}</td>
			<td class="formlabel">Status:</td>
			<td class="forminfo">
				${pbmStatusDisplay[pbmPrescBean.map.pbm_presc_status]}
			</td>
			<td class="formlabel">Drug Count:</td>
			<td class="forminfo">${pbmPrescBean.map.drug_count}</td>
		</tr>
		<tr>
			<td class="formlabel">PBM Request Id:</td>
			<td class="forminfo">${pbmPrescBean.map.pbm_request_id}</td>
			<td class="formlabel">Resubmission Request:</td>
			<td class="forminfo">${(not empty pbmPrescBean && not empty pbmPrescBean.map.pbm_request_id) ? ( pbmPrescBean.map.is_resubmit == 'Y' ? 'Yes' : 'No') : '' }</td>
			<td class="formlabel">Presc. Store:</td>
			<td class="forminfo" style="padding-right:70px;">
			<c:choose>
				<c:when test="${pbmPrescBean.map.pbm_finalized == 'Y'}">
					${pbmPrescBean.map.pbm_store_name}
					<input type = "hidden" name="_phStore" id="_phStore" value="${pbmPrescBean.map.pbm_store_id}" />
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')}">
							<insta:userstores username="${userid}" elename="_phStore" id="_phStore"
								onlySalesStores="Y" storesWithTariff="Y" onchange="onStoreChange();"
								showDefaultValueForNormalUsers='Y' defaultVal="-- Select --"
								val="${not empty pbmPrescBean.map.pbm_store_id ? pbmPrescBean.map.pbm_store_id : dept_id}"/>
						</c:when>
						<c:otherwise>
							<b> ${ifn:cleanHtml(dept_name)} </b>
							<input type = "hidden" name="_phStore" id="_phStore" value="${ifn:cleanHtmlAttribute(dept_id)}" />
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
			<div id="storeErrorDiv" style="display:none;white-space:nowrap;color:red;">
			<div class="clrboth"></div>
			<div class="fltL" style="width: 15px; margin:0 0 0 0px;"> <img src="${cpath}/images/error.png" /></div>
				<div class="fltL" style="margin:0px 0 0 5px; width:55px;">
					User has No Store With Tariff
				</div>
			</div>
			</td>
			</tr>
			<tr>
				<td class="formlabel">Rejection Details:</td>
				<td class="forminfo">
					${pbmPrescBean.map.approval_comments}
				</td>
			</tr>
	</table>
</fieldset>

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
				<th title="Frequency(Remarks)">Freq(Rem)</th>
				<th title="Duration">Dur.</th>
				<th>Form</th>
				<th title="Dose/Strength">Strength</th>
				<th>Route</th>
				<c:if test="${prescriptions_by_generics}">
				<th>Generic</th>
				</c:if>
				<th>Name</th>
				<th title="Total Available Qty">Avail.</th>
				<th class="number">Rate</th>
				<th class="number">Qty</th>
				<th>UOM</th>
				<th class="number">Disc</th>
				<th class="number">Gross</th>
				<th class="number">Patient</th>
				<th class="number">Net</th>
				<th style="width: 16px"></th>
				<th style="width: 16px"></th>
				<th class="number" title="Approved Net">Apprd.</th>
				<th>PBM Status</th>
				<th>Denial Code</th>
				<th>Code Type</th>
				<th>Code Status</th>
			</tr>
			<c:set var="numPrescriptions" value="${fn:length(prescActivitiesList)}"/>
			<c:forEach begin="1" end="${numPrescriptions+1}" var="i" varStatus="status">
				<c:set var="prescActivity" value="${prescActivitiesList[i-1]}"/>

				<c:set var="activity" value="${prescActivity.activity}"/>
				<c:set var="observations" value="${prescActivity.observations}"/>

				<c:set var="item_issued" value="${not empty activity.issued ? activity.issued : 'N'}"/>
				<c:set var="item_issued" value="${activity.issued == 'P' or activity.issued == 'Y' ? 'Y' : activity.issued}"/>

				<c:set var="flagColor">
					<c:choose>
						<c:when test="${activity.pbm_status eq 'O'}">empty</c:when>
						<c:when test="${activity.pbm_status eq 'D'}">red</c:when>
						<c:when test="${activity.pbm_status eq 'C'}">grey</c:when>
					</c:choose>
				</c:set>
				<c:if test="${empty activity}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<tr ${style}>
					<c:set var="rowindex" value="${status.index}"/>
					<td>${status.index}</td>
					<td>
						<fmt:formatDate pattern="dd-MM-yyyy" value="${activity.prescribed_date}" var="prescribed_date"/>
						<label>${prescribed_date}</label>
						<input type="hidden" name="s_prescribed_date" value="${prescribed_date}"/>
						<input type="hidden" name="s_item_prescribed_id" value="${activity.item_prescribed_id}"/>
						<input type="hidden" name="s_item_name" value="<c:out value='${activity.item_name}'/>"/>
						<input type="hidden" name="s_item_id" value="${activity.item_id}"/>
						<input type="hidden" name="s_strength" value="${activity.strength}"/>
						<input type="hidden" name="s_frequency" value="${activity.frequency}"/>
						<input type="hidden" name="s_duration" value="${activity.duration}"/>
						<input type="hidden" name="s_duration_units" value="${activity.duration_units}"/>
						<input type="hidden" name="s_medicine_quantity" value="${activity.medicine_quantity}"/>
						<input type="hidden" name="s_remarks" value="${activity.medicine_remarks}"/>
						<input type="hidden" name="s_item_master" value="${activity.master}"/>

						<input type="hidden" name="s_ispackage" id="s_ispackage" value="${activity.ispackage}"/>
						<input type="hidden" name="s_issued" value="${item_issued}"/>
						<input type="hidden" name="s_generic_code" value="${activity.generic_code}"/>
						<input type="hidden" name="s_generic_name" value="${activity.generic_name}"/>
						<input type="hidden" name="s_granular_units" value="${activity.granular_units}"/>

						<input type="hidden" name="pat_item_prescribed_id" value="${activity.pat_presc_item_prescribed_id}"/>
						<input type="hidden" name="pat_strength" value="${activity.pat_presc_strength}"/>
						<input type="hidden" name="pat_frequency" value="${activity.pat_presc_frequency}"/>
						<input type="hidden" name="pat_duration" value="${activity.pat_presc_duration}"/>
						<input type="hidden" name="pat_duration_units" value="${activity.pat_presc_duration_units}"/>
						<input type="hidden" name="pat_medicine_quantity" value="${activity.pat_presc_medicine_quantity}"/>
						<input type="hidden" name="pat_remarks" value="${activity.pat_presc_medicine_remarks}"/>
						<input type="hidden" name="pat_route_id" value="${activity.pat_presc_route_id}"/>
						<input type="hidden" name="pat_route_name" value="${activity.pat_presc_route_name}"/>
						<input type="hidden" name="pat_item_form_id" value="${activity.pat_presc_item_form_id}"/>
						<input type="hidden" name="pat_item_form_name" value="${activity.pat_presc_item_form_name}"/>
						<input type="hidden" name="pat_item_strength" value="${activity.pat_presc_item_strength}"/>
						<input type="hidden" name="pat_item_strength_units" value="${activity.pat_presc_item_strength_units}"/>
						<input type="hidden" name="pat_item_strength_units_name" value="${activity.pat_presc_unit_name}"/>

						<c:set var="activity_due_date" value=""/>
						<c:if test="${not empty activity.activity_due_date}">
							<fmt:formatDate value="${activity.activity_due_date}" pattern="dd-MM-yyyy HH:mm" var="activity_due_date"/>
						</c:if>
						<input type="hidden" name="s_activity_due_date" value="${activity_due_date}"/>
						<input type="hidden" name="s_addActivity" value="${not empty activity_due_date}"/>

						<input type="hidden" name="s_pkg_size" value=""/>
						<input type="hidden" name="s_pkg_price" value=""/>
						<input type="hidden" name="s_item_pkg_price" value=""/>
						<input type="hidden" name="s_item_unit_price" value=""/>

						<input type="hidden" name="s_consumption_uom" value="${activity.consumption_uom}"/>
						<input type="hidden" name="s_qty_in_stock" value="${activity.total_available_qty}"/>

						<input type="hidden" name="s_route_id" value="${activity.route_id}"/>
						<input type="hidden" name="s_route_name" value="${activity.route_name}"/>

						<input type="hidden" name="s_item_form_name" value="${activity.item_form_name}"/>
						<input type="hidden" name="s_item_form_id" value="${activity.item_form_id == 0 ? '' : activity.item_form_id}"/>
						<input type="hidden" name="s_item_strength" value="${activity.item_strength}"/>
						<input type="hidden" name="s_item_strength_units" value="${activity.item_strength_units}"/>
						<input type="hidden" name="s_item_strength_units_name" value="${activity.unit_name}"/>

						<input type="hidden" name="s_delItem" id="s_delItem" value="false" />
						<input type="hidden" name="s_itemType" value="Medicine"/>
						<input type="hidden" name="s_edited" value='false'/>

						<input type="hidden" name="s_pbm_presc_status" value="${pbmPrescBean.map.pbm_presc_status}" />
						<input type="hidden" name="s_pbm_status" value="${activity.pbm_status}" />

						<input type="hidden" name="s_item_code_type" value="${activity.code_type}"/>
						<input type="hidden" name="s_item_code" value="${activity.item_code}"/>

						<input type="hidden" name="s_item_package_uom" value="${activity.package_uom}"/>
						<input type="hidden" name="s_item_issue_uom" value="${activity.issue_uom}"/>

						<input type="hidden" name="s_item_qty" value="${activity.medicine_quantity}"/>
						<input type="hidden" name="s_item_user_unit" value="${activity.user_unit}"/>
						<input type="hidden" name="s_item_user_uom" value="${activity.user_uom}"/>
						<input type="hidden" name="s_item_package_unit" value="${activity.package_size}"/>
						<input type="hidden" name="s_orig_item_rate" value="${activity.rate}" />
						<input type="hidden" name="s_item_rate" value="${activity.rate}" />
						<input type="hidden" name="s_item_disc" value="${activity.discount}" />
						<input type="hidden" name="s_item_amount" value="${activity.amount}"/>
						<input type="hidden" name="s_orig_item_amount" value="${activity.amount}"/>
						<input type="hidden" name="s_patient_amount" value="${activity.amount - activity.claim_net_amount}"/>
						<input type="hidden" name="s_claim_net_amount" value="${activity.claim_net_amount}"/>
						<input type="hidden" name="s_orig_claim_net_amount" value="${activity.claim_net_amount}"/>
						<input type="hidden" name="s_claim_approved_amount" value="${activity.claim_net_approved_amount}"/>

						<c:forEach items="${observations}" var="obs">
							<c:if test="${obs.patient_med_presc_value_column == 'item_strength'}">
								<input type="hidden" name="s_item_strength_code_type.${activity.item_prescribed_id}" value="${obs.type}">
								<input type="hidden" name="s_item_strength_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'route_of_admin'}">
							<input type="hidden" name="s_medicine_route_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_medicine_route_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'strength'}">
							<input type="hidden" name="s_strength_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_strength_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'frequency'}">
							<input type="hidden" name="s_frequency_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_frequency_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'duration'}">
							<input type="hidden" name="s_duration_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_duration_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'item_form_id'}">
							<input type="hidden" name="s_item_form_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_item_form_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'medicine_remarks'}">
							<input type="hidden" name="s_remarks_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_remarks_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
							<c:if test="${obs.patient_med_presc_value_column == 'refills'}">
							<input type="hidden" name="s_refills_code_type.${activity.item_prescribed_id}" value="${obs.type}">
							<input type="hidden" name="s_refills_code.${activity.item_prescribed_id}" value="${obs.code}">
							</c:if>
						</c:forEach>

						<c:set value="${activity.frequency}" var="freqRem"/>
						<c:if test="${not empty activity.medicine_remarks && activity.medicine_remarks != ''}">
						<c:set value="${activity.frequency}(${activity.medicine_remarks})" var="freqRem"/>
						</c:if>

					</td>
					<td title="${freqRem}">
						<insta:truncLabel value="${freqRem}" length="10"/>
					</td>
					<td><label>${activity.duration} ${activity.duration_units}</label></td>
					<td><insta:truncLabel value="${activity.item_form_name}" length="20"/></td>
					<td><insta:truncLabel value="${activity.item_strength} ${activity.unit_name}" length="20"/></td>
					<td><label>${activity.route_name}</label></td>
					<c:if test="${prescriptions_by_generics}">
					<td><insta:truncLabel value="${activity.generic_name}" length="8"/></td>
					</c:if>
					<td><insta:truncLabel value="${activity.item_name}" length="12"/></td>
					<td class="number"><label>${activity.total_available_qty}</label></td>
					<td class="number"><label>${activity.rate}</label></td>
					<td class="number"><label>${activity.medicine_quantity}</label></td>
					<td><label>${activity.user_uom}</label></td>
					<td class="number"><label>${activity.discount}</label></td>
					<td class="number"><label>${activity.amount}</label></td>
					<td class="number"><label>${activity.amount - activity.claim_net_amount}</label></td>
					<td class="number"><label>${activity.claim_net_amount}</label></td>

					<td style="text-align: center; ">
						<c:choose>
							<c:when test="${pbmPrescBean.map.pbm_finalized == 'Y' || not empty error}">
								<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button" />
							</c:when>
							<c:otherwise>
								<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelSIItem(this);" title="Cancel Item" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align: center">
						<a name="si_editAnchor" href="javascript:Edit" onclick="return showEditSIDialog(this);" title="Edit Item Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
					<td class="number"><label>${activity.claim_net_approved_amount}</label></td>

					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						${activityPbmStatusDisplay[activity.pbm_status]}
					</td>
					<td>${activity.denial_code}</td>
					<td>${activity.denial_code_type}</td>
					<td>${denialCodeStatusDisplay[activity.denial_code_status]}</td>

					<c:set var="totalAmount" value="${totalAmount + activity.amount + activity.discount}"/>
					<c:set var="totalDiscount" value="${totalDiscount + activity.discount}"/>
					<c:set var="totalGrossAmount" value="${totalGrossAmount + activity.amount}"/>
					<c:set var="totalPatientAmount" value="${totalPatientAmount + (activity.amount - activity.claim_net_amount)}"/>
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
						<c:when test="${pbmPrescBean.map.pbm_finalized == 'Y' || not empty error}">
							<button type="button" name="btnAddItem" id="btnAddItem"
								title="Add PBM Prescription (Alt_Shift_O)"
								accesskey="O" class="imgButton"><img src="${cpath}/icons/Add1.png"></button>
						</c:when>
						<c:otherwise>
							<button type="button" name="btnAddItem" id="btnAddItem" title="Add PBM Prescription (Alt_Shift_O)"
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
	  <legend class="fieldSetLabel">PBM Presc. Totals</legend>
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

	<fieldset class="fieldSetBorder">
	<table class="formtable" align="center">
		<tr>
			<td class="formlabel">Resubmit type:</td>
			<td class="forminfo">
				<c:if test="${not empty pbmPrescBean.map.pbm_presc_status}">
				<c:choose>
					<c:when test="${pbmPrescBean.map.pbm_presc_status ne 'R'}">
						<insta:selectoptions name="_resubmit_type" id="_resubmit_type"
							 value="${pbmPrescBean.map.resubmit_type}" dummyvalue="-- Select --"
							 optexts="correction,internal complaint,legacy"
							 opvalues="correction,internal complaint,legacy" disabled="true"
							 onchange="enableDisableQtyFieldsForCorrection();"/>
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="_resubmit_type" id="_resubmit_type"
							 value="${pbmPrescBean.map.resubmit_type}" dummyvalue="-- Select --"
							 optexts="correction,internal complaint,legacy"
							 opvalues="correction,internal complaint,legacy"
							 onchange="enableDisableQtyFieldsForCorrection();"/>
					</c:otherwise>
				</c:choose>
				<input type="hidden" name="_old_resubmit_type" id="_old_resubmit_type"
						value="${pbmPrescBean.map.resubmit_type}" />
				</c:if>
			</td>
			<td class="formlabel">Comments:</td>
			<td class="forminfo">
				<textarea name="_comments" id= "_comments" title="Comments for resubmission" rows="2" cols="60"
					${(not empty pbmPrescBean.map.pbm_presc_status && pbmPrescBean.map.pbm_presc_status ne 'R') ? 'disabled' :''}
					>${pbmPrescBean.map.comments}</textarea>
				<textarea style="display:none" name="_old_comments"
					id="_old_comments">${pbmPrescBean.map.comments}</textarea>
			</td>
		</tr>
	</table>
	</fieldset>

	<table class="screenActions">
	<tr>
		<td>
			<button type="button" name="actionBtn" value="save"
				accessKey="S" onclick="return doSave();"><b><u>S</u></b>ave PBM</button>
			<label>|</label>
			<c:if test="${pbmPrescBean.map.pbm_presc_status eq 'O'}">
			<c:choose>
				<c:when test="${pbmPrescBean.map.pbm_finalized == 'Y'}">
					<button type="button" name="actionBtn" value="reopen"
						accessKey="R" onclick="return doReopen();"><b><u>R</u></b>eopen</button>
					<label>|</label>
				</c:when>
				<c:otherwise>
					<c:if test="${((actionRightsMap.pbm_prescription_finalize == 'A')||(roleId==1)||(roleId==2))}">
					<input type="checkbox" name="finalizeChk" id="finalizeChk"
						value="" accesskey="Z">
					<label>Finali<b><u>Z</u></b>e</label> &nbsp;
					<label>|</label>
					</c:if>
				</c:otherwise>
			</c:choose>
			</c:if>
			<c:choose>
				<c:when test="${pbmPrescBean.map.pbm_presc_status eq 'O'}">
				</c:when>
				<c:when test="${pbmPrescBean.map.pbm_presc_status eq 'S'}">
					<%-- Bug # 37768 -- Should we allow to mark PBM as denied?
						<input type="checkbox" name="actionChk" id="actionChk"
						value="markAsDenied" accesskey="D">
					<label><b><u>M</u></b>ark As Denied</label> &nbsp;
					<label>|</label> --%>
				</c:when>
				<c:when test="${pbmPrescBean.map.pbm_presc_status eq 'D'}">
					<input type="checkbox" name="actionChk" id="actionChk" onclick="enableResubmissionFields();"
						value="markForResubmission" accesskey="M">
					<input type="hidden" name="markResubmit" id="markResubmit" value=""/>
					<label><b><u>M</u></b>ark for Resubmission</label> &nbsp;
					<label>|</label>
						<a href="./PBMPresc.do?_method=addOrEditAttachment&pbm_presc_id=${pbmPrescBean.map.pbm_presc_id}">Add/Edit Attachment</a>
						<label>|</label>
				</c:when>
			</c:choose>

			<a href="./PBMPrescList.do?_method=getList&pbm_finalized=N">PBM Prescriptions List</a>
			<insta:screenlink addPipe="true" screenId="pbm_requests" label="PBM Requests"
						extraParam="?_method=getRequests&pbm_finalized=Y&pbm_presc_status=O&pbm_presc_status=R"/>
		</td>
	</tr>
	</table>

	<%@include file="/pages/PBMAuthorization/PBMPrescDialogs.jsp" %>

	<!-- ends HERE -->

	<div class="legend">
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Denied</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Closed</div>
	</div>

</form>
</body>
</html>
