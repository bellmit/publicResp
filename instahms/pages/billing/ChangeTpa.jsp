<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@page import="com.insta.hms.Registration.PatientInsurancePlanDAO"%>
<%@page import="java.util.Map"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="genPrefs" value='<%=GenericPreferencesDAO.getAllPrefs()%>' scope="request"/>
<c:set var="corpInsurance" value='${genPrefs.get("corporate_insurance")}'/>
<c:set var="addEditInsuranceApplicable" value='${genPrefs.get("add_edit_insurance_applicable")}'/>
<c:set var="corpInsurancehid" scope="request">
	<c:choose>
		<c:when test="${corpInsurance eq 'Y'}">display:none</c:when>
		<c:otherwise></c:otherwise>
	</c:choose>
</c:set>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true" />
<title><insta:ltext key="billing.changetpa.add.editpatientinsurance.changetpa.instahms"/></title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="instaautocomplete.js" />
<insta:link type="script" file="billing/change_tpa.js" />
<insta:link type="script" file="registration/visitInsuranceDetails.js"/>
<insta:link type="css" file="lightbox.css" />
<insta:link type="script" file="ajax.js" />
<insta:js-bundle prefix="registration.patient" />
<insta:js-bundle prefix="patient.consultation" />
<insta:link type="script" file="moment.min.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<jsp:useBean id="currentDate" class="java.util.Date" />

<style>
	#myDialog_mask.mask {
	    z-index: 1;
	    display: none;
	    position: absolute;
	    top: 0;
	    left: 0;
	    -moz-opacity: 0.0001;
	    opacity: 0.0001;
	    filter: alpha(opacity=50);
	    background-color: #CCC;
	}
	#myDialog1_mask.mask {
	    z-index: 1;
	    display: none;
	    position: absolute;
	    top: 0;
	    left: 0;
	    -moz-opacity: 0.0001;
	    opacity: 0.0001;
	    filter: alpha(opacity=50);
	    background-color: #CCC;
	}
	a.picture, a.picture:hover {
	    text-decoration: none;
	    background: #ffffff;
	}
	a.picture img.large {
	    position: absolute;
	    top: 441px;
	    left: 713px;
	    border:2px solid #CCCCCC;
	    display: none;
	}
	a.picture:hover img.large {
	    display: block;
	}

	.scrolForContainer .yui-ac-content{
	 	max-height:11em;overflow:auto;overflow-x:auto;width: 350px; /* scrolling */
	}
	.yui-ac-input {
		position:relative;
	}
</style>

<script>
    var ajaxDetails;
    var policynames;
    var companyTpaList;
    var comTpaListAll;
	var insuCompanyDetails;
	var insuCatNames;
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);


	var corporate_sponsor_id = '${patient.corporate_sponsor_id}';
	var national_sponsor_id = '${patient.national_sponsor_id}';
	var sec_corporate_sponsor_id = '${patient.sec_corporate_sponsor_id}';
	var sec_national_sponsor_id = '${patient.sec_national_sponsor_id}';
	var primary_sponsor_id = '${patient.primary_sponsor_id}';
	var secondary_sponsor_id = '${patient.secondary_sponsor_id}';
	var primary_insurance_appoval = '${patient.primary_insurance_approval}';
	var secondary_insurance_approval = "${patient.secondary_insurance_approval}";
	var corpInsuranceCheck = '${corpInsurance}';
	var patientDateOfBirth =  '${patient.dateofbirth}';
	var patientRegDate =  '${patient.reg_date}';
	
	<c:set var="patCategory" value="${patient.patient_category}" />
	var patientCategory = '${patCategory}';

	var categoryJSON= ${ifn:convertListToJson(categoryJSON)};

	var tpanames = ${ifn:convertListToJson(tpaList)}
	var networkTypeSponsorIdListMap = ${networkTypeSponsorIdListMap};

	var orgNamesJSON=${ifn:convertListToJson(ratePlanList)};
	var gPatientPolciyNos = ${ifn:convertListToJson(policyNos)};
	var gAllPatientPolciyNos =${ifn:convertListToJson(allPolicyNos)};
	var gPatientCategoryRatePlan = '${patient.org_id}';

	var tpaBillsJSON = ${ifn:convertListToJson(allTpaBills)};

	var InsuranceModuleEnabled ='${preferences.modulesActivatedMap['mod_insurance']}';
	var visit_type = '${patient.visit_type}';
	var currentInsuranceCase = '${patient.primary_insurance_co}';
	var currentTpaId = '${patient.primary_sponsor_id}';
	var gPlan = '${patient.plan_id}';

		
	var regPref = ${(patient.visit_type == 'o' && oldEditIns == 'false') ? ifn:convertMapToJson(regPref) : regPrefJSON};
	var usePerdiem = '${patient.use_perdiem}';
	
	var insuranceCardMandatory = regPref.insuranceCardMandatory;
	var priorAuthRequired = regPref.prior_auth_required;
	var memberIdLabel = regPref.member_id_label;
	var memberIdValidFromLabel = regPref.member_id_valid_from_label;
	var memberIdValidToLabel = regPref.member_id_valid_to_label;
	var patientCategoryFieldLabel = regPref.patientCategory;

	var InsuranceModuleEnabled ='${preferences.modulesActivatedMap['mod_insurance']}';
	var modAdvInsurance ='${preferences.modulesActivatedMap['mod_adv_ins']}';
	var isModAdvanceIns = ${preferences.modulesActivatedMap['mod_adv_ins'] == 'Y'};
	var isModInsurance  = ${preferences.modulesActivatedMap['mod_insurance'] == 'Y'};

	var isModAdvBilling  = ${preferences.modulesActivatedMap['adv_billing'] == 'Y'};
	var gIsInsurance =  true;

	var gPreviousPrimarySponsorIndex = '${patient.sponsor_type}';
	var gPreviousSecondarySponsorIndex = '${patient.sec_sponsor_type}';

	var gPreviousSecondaryInsCompany = '${not empty patient.secondary_insurance_co? patient.secondary_insurance_co: null}';
	var gPreviousSecondaryTpa = '${not empty patient.secondary_sponsor_id? patient.secondary_sponsor_id: null}';

	var gPreviousCorporateRelation =  <insta:jsString value="${not empty patient.patient_corporate_relation ? patient.patient_corporate_relation : null}"/>;
	var gPreviousSecCorporateRelation =  <insta:jsString value="${not empty patient.sec_patient_corporate_relation ? patient.sec_patient_corporate_relation : null}"/>;

	var gPreviousCorporateEmployeeId = <insta:jsString value="${not empty patient.employee_id ? patient.employee_id : null}"/>;
	var gPreviousCorporateEmployeeName =  <insta:jsString value="${not empty patient.employee_name ? patient.employee_name : null}"/>;

	var gPreviousSecCorporateEmployeeId = <insta:jsString value="${not empty patient.sec_employee_id ? patient.sec_employee_id : null}"/>;
	var gPreviousSecCorporateEmployeeName = <insta:jsString value="${not empty patient.sec_employee_name ? patient.sec_employee_name : null}"/>;

	var gPreviousNationalSponsorId =  '${not empty patient.national_sponsor_id ? patient.national_sponsor_id : null}';
	var gPreviousCorporateSponsorId = '${not empty patient.corporate_sponsor_id ? patient.corporate_sponsor_id : null}';

	var gPreviousSecNationalSponsorId =  '${not empty patient.sec_national_sponsor_id ? patient.sec_national_sponsor_id : null}';
	var gPreviousSecCorporateSponsorId = '${not empty patient.sec_corporate_sponsor_id ? patient.sec_corporate_sponsor_id : null}';

	var gPreviousNationalId = <insta:jsString value="${not empty patient.national_id ? patient.national_id : null}"/>;
	var gPreviousNationalCitizenName = <insta:jsString value="${not empty patient.citizen_name ? patient.citizen_name : null}"/>;
	var gPreviousNationalRelation = <insta:jsString value="${not empty patient.patient_national_relation ? patient.patient_national_relation : null}"/>;
	var gPatientBillsApprovalTotal ='${billsApprovalTotal}';

	var gPreviousSecNationalId = <insta:jsString value="${not empty patient.sec_national_id ? patient.sec_national_id : null}"/>;
	var gPreviousSecNationalCitizenName = <insta:jsString value="${not empty patient.sec_citizen_name ? patient.sec_citizen_name : null}"/>;
	var gPreviousSecNationalRelation = <insta:jsString value="${not empty patient.sec_patient_national_relation ? patient.sec_patient_national_relation : null}"/>;


	var moreThanOneTpaBillsExist = '${allowSecSponsor eq 'N'}';
	var patientCategoryId = ${patCategory};

	var gPreviousPatientCategoryId = ${patCategory};
	var gPreviousPatientCategoryExpDate = '${not empty patient.category_expiry_date ? patient.category_expiry_date : null}';
	var gPreviousRatePlan = '${patient.org_id}';
	var gInitRatePlan = '${patient.org_id}';
	var pvisitType = '${patient.visit_type}';
	var pOpType = '${patient.op_type}';

	var creditNoteList = '${creditNoteList}';
	var isCopyPaste = "${regPref.copy_paste_option eq 'Y'}";

	var screenid = "Edit_Insurance";
	var visitType= pvisitType;
	var itemCatlist = ${ifn:convertListToJson(itemCatlist)};
	var visitId="${ifn:cleanJavaScript(param.visitId)}";
	var allowCopayChange = '${actionRightsMap.allow_dynamic_copay_change}';
	var roleId='${roleId}';
	var sponsorTypeList= ${ifn:convertListToJson(sponsorTypelist)};
	var oldEditIns = ${oldEditIns};
	// TODO : sponsor type migration - perdayrate
</script>

<script>
<c:set var="primarySponsorId">${patient.primary_sponsor_id}</c:set>
<c:set var="secSponsorId">${patient.secondary_sponsor_id}</c:set>

<c:set var="ptype">${patient.sponsor_type}</c:set>
<c:set var="stype">${patient.sec_sponsor_type}</c:set>

</script>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="common.message"/>
</head>

<body onload="init();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<form name="billNoForm" action="BillAction.do" method="GET" onsubmit="return validateForm();">
	<input type="hidden" name="_method" value="changeTpa">
	<input type="hidden" name="_isAdvnInsuActivtd" id="_isAdvnInsuActivtd"
				value="${preferences.modulesActivatedMap['mod_adv_ins']}">
	<input type="hidden" name="_isInsuActivtd" id="_isInsuActivtd"
				value="${preferences.modulesActivatedMap['mod_insurance']}">
<table width="100%">
	<tr>
		<td width="100%"> <h1><insta:ltext key="billing.changetpa.add.editpatientinsurance.addoreditpatientinsurance"/></h1> </td>
	</tr>
</table>
</form>

<insta:feedback-panel />

<div class="helpPanel">
<table>
	<tr>
		<td valign="top" style="width: 30px">
			<img src="${cpath}/images/information.png"/>
		</td>
		<td style="padding-bottom: 5px">
			<insta:ltext key="billing.changetpa.add.editpatientinsurance.theinsurancestatustemplate"/>
		</td>
	</tr>
</table>
</div>

<div><insta:patientdetails visitid="${patient.patient_id}" /></div>
<c:set var="actionPath" value=""/>
<c:choose>
	<c:when test="${patient.visit_type == 'o' && oldEditIns == 'false'}">
		<c:set var="actionPath" value="${cpath}/insurance/updateInsuranceDetails.htm"></c:set>
	</c:when>
	<c:otherwise>
		<c:set var="actionPath" value="changeTPA.do"></c:set>
	</c:otherwise>
</c:choose>
<form name="mainform" action="${actionPath}" method="POST" enctype="multipart/form-data" autocomplete="off">
	<input type="hidden" name="_method" value="">
	<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}" />
	<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(param.isNewUX)}" />
	<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(param.billNo)}" />
	<input type="hidden" name="mrno" id="mrno" value="${patient.mr_no}" />
	<input type="hidden" name="rate_plan" id="rate_plan" value="${patient.org_id}" />
	<input type="hidden" name="existing_plan_id" id="existing_plan_id" value="${patient.plan_id}" />
	<input type="hidden" name="existing_tpa_id" id="existing_tpa_id" value="${patient.primary_sponsor_id}" />
	<input type="hidden" name="existing_use_drg" id="existing_use_drg" value="${patient.use_drg}" />
	<input type="hidden" name="existing_use_perdiem" id="existing_use_perdiem" value="${patient.use_perdiem}" />
	<input type="hidden" name="primaryDocUpdated" id="primaryDocUpdated" value="N" />
	<input type="hidden" name="secondaryDocUpdated" id="secondaryDocUpdated" value="N" />
	<input type="hidden" name="visitLimitsChanged" id="visitLimitsChanged" value="N" />
	<input type="hidden" name="visitType" id="visitType" value="${patient.visit_type}"/>

<table class="formtable">
	<tr>
		<td>
		<fieldset class="fieldSetBorder" style="width: 935px;">
			<legend class="fieldSetLabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.editsponsordetails"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.changeclaimamountsforsponsorbills"/>:</td>
						<td class="forminfo" style="width:180px;">
							
						<c:choose>
							<c:when test="${addEditInsuranceApplicable == 'A'}">
								<select name="bills_to_change_sponsor_amounts"
									id="bills_to_change_sponsor_amounts" class="dropdown">
									<option value="all_bills" selected><insta:ltext
											key="billing.changetpa.add.editpatientinsurance.allbills.open.finalized.closed" /></option>
									<option value="open_bills"><insta:ltext
											key="billing.changetpa.add.editpatientinsurance.onlyopenbills" /></option>
								</select>
								<span class="star">*</span>
								<img class="imgHelpText" src="${cpath}/images/help.png"
									title='<insta:ltext key="registration.patient.sponsor.template1" /> ${mod_adv_ins ? 'NOTE: If bill has claim which is marked as Sent then that bill is not reopened.' : ''}<insta:ltext key="registration.patient.sponsor.template2" />'/>
							</c:when>
							<c:otherwise>
								<insta:ltext key="billing.changetpa.add.editpatientinsurance.onlyopenbills"/>
								<input type="hidden" name="bills_to_change_sponsor_amounts" id="bills_to_change_sponsor_amounts" value="open_bills"/>
							</c:otherwise>
						</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.primarysponsor"/>:</td>
						<td>
								<input type="checkbox" name="primary_sponsor_wrapper" id="primary_sponsor_wrapper" onchange="onChangePrimarySponsor()">
								<input type="hidden" name="primary_sponsor" id="primary_sponsor" value="">
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.secondary.sponsor"/>:</td>
								<td>
								<input type="checkbox" name="secondary_sponsor_wrapper" id="secondary_sponsor_wrapper" onchange="onChangeSecondarySponsor()" disabled>
								<input type="hidden" name="secondary_sponsor" id="secondary_sponsor" value="">
								</td>
						<td class="formlabel">
							<label for="patientType1">
								<insta:ltext key="registration.patient.payment.ratePlan" />:</label>
						</td>
						<td class="forminfo">
							<select id="organization" name="organization" size="1" class="dropdown"
							onchange="ratePlanChange()"></select>
							<img class="imgHelpText" src="${cpath}/images/help.png"
 								title='<insta:ltext key="billing.changetpa.add.editpatientinsurance.rateplandetailstemplate"/>'/>
						</td>
					</tr>
			</table>
		</fieldset>
		</td>
	</tr>
	<insta:insurance-panel sponsorIndex="P" visitType='${patient.visit_type}' screenid="Edit_Insurance"/>
	<insta:insurance-panel sponsorIndex="S" visitType='${patient.visit_type}' screenid="Edit_Insurance"/>

<div id="showphotoViewDialog" style="display: none">
	<div class="bd"><img id="photoImgId" src="" width="330px" height="210" /></div>
</div>

<div id="showPimarySponsorViewDialog" style="display: none">
	<div class="bd"><img id="primarySponsorImgId" src="" width="330px" height="210" /></div>
</div>

<div id="showSecondarySponsorViewDialog" style="display: none">
	<div class="bd"><img id="secondarySponsorImgId" src="" width="330px" height="210" /></div>
</div>


<c:set var="primaryPolicyID" value="0"/>
<c:set var="secondaryPolicyID" value="0"/>
<c:set var="sponsor" value="none"/>

<c:set var="policyDetailsLen" value="${fn:length(policyNos)}"/>
	<c:choose>
		<c:when test="${policyDetailsLen eq 2}">
			<c:set var="primaryPolicyID" value="${policyNos[0].patient_policy_id}" />
			<c:set var="secondaryPolicyID" value="${policyNos[1].patient_policy_id}" />
		</c:when>
		<c:when test="${policyDetailsLen eq 1}">
			<%
				String sponsor = new PatientInsurancePlanDAO().isPrimaryOrSecondaryPlan((String)((java.util.Map)request.getAttribute("patient")).get("patient_id"));
			%>
			<c:set var="sponsor" value="<%=sponsor%>"/>
			<c:choose>
				<c:when test="${sponsor eq 'primary'}">
					<c:set var="primaryPolicyID" value="${policyNos[0].patient_policy_id}" />
				</c:when>
				<c:when test="${sponsor eq 'secondary'}">
					<c:set var="secondaryPolicyID" value="${policyNos[0].patient_policy_id}" />
				</c:when>
				<c:otherwise></c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise></c:otherwise>
	</c:choose>


<table cellpadding="0" cellspacing="0" border="0" width="100%">
	<tr>
		<td align="left">
			<c:choose>
				<c:when
					test="${not empty patient.op_type && (patient.op_type == 'F' || patient.op_type == 'D')}">
					<button type="button" id="saveButton" accessKey="S"
						onclick="return validate();"><b><u><insta:ltext key="billing.changetpa.add.editpatientinsurance.s"/></u></b><insta:ltext key="billing.changetpa.add.editpatientinsurance.ave"/></button>
				</c:when>
				<c:otherwise>
					<button type="button" id="saveButton" accessKey="S"
						onclick="return validate();"><b><u><insta:ltext key="billing.changetpa.add.editpatientinsurance.s"/></u></b><insta:ltext key="billing.changetpa.add.editpatientinsurance.ave"/></button>
				</c:otherwise>
			</c:choose>
			|&nbsp; <a href="${pageContext.request.contextPath}/editVisit/changeTPA.do?
							_method=changeTpa&amp;visitId=${patient.patient_id}&amp;billNo=${ifn:cleanURL(param.billNo)}"><insta:ltext key="billing.changetpa.add.editpatientinsurance.reset"/></a>
			<c:if test="${not empty param.billNo}">
				|&nbsp;<insta:backToBillLink mr_no="${patient.mr_no}" visit_type="${patient.visit_type}" bill_no="${billNo}" is_new_ux="${ifn:cleanURL(param.isNewUX)}" bill_label_prefix_key="billing.changetpa.add.editpatientinsurance.backto" />
			</c:if>
			| <a href="${pageContext.request.contextPath}/pages/registration/editvisitdetails.do?
							_method=getPatientVisitDetails&patient_id=${patient.patient_id}"><insta:ltext key="billing.changetpa.add.editpatientinsurance.editvisitdetails"/></a>
			</td>
	</tr>
</table>


<input type="hidden" name="primary_policy_id" value="${primaryPolicyID}" />
<input type="hidden" name="secondary_policy_id" value="${secondaryPolicyID}" />

<div id="primaryInsurancePhotoDialog" style="display:none;visibility:hidden;"
	ondblclick="handleInsurancePhotoDialogCancel();">
	<div class="bd" id="bd2" style="padding-top: 0px;">
		<table style="text-align:top;vertical-align:top;" width="100%">
			<tr>
				<td>
				<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
					<legend class="fieldSetLabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.primarysponsordocument"/></legend>
					<c:choose>
						<c:when test="${isPrimaryInsuranceCardAvailable eq true }">
							<embed id="pinsuranceImage" height="450px" width="500px" style="overflow:auto"
							src="${cpath}/Registration/GeneralRegistrationPlanCard.do?_method=viewInsCardImageForVisit&visitId=${patient.patient_id}&sponsorIndex=P&sponsorType=${ptype}&patient_policy_id=${primaryPolicyID}"/>
						</c:when>
						<c:otherwise> <insta:ltext key="billing.changetpa.add.editpatientinsurance.nouploadeddocumentavailable"/>	</c:otherwise>
					</c:choose>
				</fieldset>
				</td>
			</tr>
			<tr>
				<td align="left"><input type="button" value="Close" style="cursor:pointer;"
					onclick="handlePrimaryInsurancePhotoDialogCancel();"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="secondaryInsurancePhotoDialog" style="display:none;visibility:hidden;"
	ondblclick="handlePrimaryInsurancePhotoDialogCancel();">
	<div class="bd" id="sbd2" style="padding-top: 0px;">
		<table style="text-align:top;vertical-align:top;" width="100%">
			<tr>
				<td>
				<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
					<legend class="fieldSetLabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.secondarysponsordocument"/></legend>
					<c:choose>
						<c:when test="${isSecondaryInsuranceCardAvailable eq true }">
							<embed id="sinsuranceImage" height="450px" width="500px" style="overflow:auto"
							src="${cpath}/Registration/GeneralRegistrationPlanCard.do?_method=viewInsCardImageForVisit&visitId=${patient.patient_id}&sponsorIndex=S&sponsorType=${stype}&patient_policy_id=${secondaryPolicyID}"/>
						</c:when>
						<c:otherwise> <insta:ltext key="billing.changetpa.add.editpatientinsurance.nouploadeddocumentavailable"/> </c:otherwise>
					</c:choose>
				</fieldset>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="Close" style="cursor:pointer;"
					onclick="handleSecondaryInsurancePhotoDialogCancel();" />
				</td>
			</tr>
		</table>
	</div>
</div>
<div id="insurancePlanDetailsDialog" style="display: none;visibility: hidden;">
  <div class="bd">
  <table id="pd_insDialogTableMain">
  <tr>
  <td>
	<fieldset class="fieldSetBorder" style="width:430px;white-space: normal;">
	  <legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patient.registration.insurance.details.summary" /></legend>
	  	<table class="formTable" align="center"id="pd_insDialogTableHeader" cellspacing="10" cellpadding="5">
			<tr>
				<input type="hidden" id="insEdited" name="insEdited" value=""/>
				<input type="hidden" id="copayFlag" name="copayFlag" value=""/>
				<td style="width: 60px"><insta:ltext key="registration.patient.payment.itemcategory" /></td>
				<td style="width: 60px"><insta:ltext key="registration.patient.payment.sponsorlimit" /></td>
				<td style="width: 60px"><insta:ltext key="registration.patient.payment.catdeductible" /></td>
				<td style="width: 60px"><insta:ltext key="registration.patient.payment.itemdeductible" /></td>
				<td style="width: 60px"><insta:ltext key="registration.patient.payment.copaypercent" /></td>
				<td style="width: 50px"><insta:ltext key="registration.patient.payment.maxcopay" /></td>
			</tr>
	  	</table>
	  	<div style="height: 300px; overflow-y: scroll;">
	  	<table class="formTable" id="pd_insDialogTable1" cellspacing="10" cellpadding="5">
	  		<tr></tr>
	  	</table>
	  	</div>
	 </fieldset>
   </table>
	  <div style="height:6px;">&nbsp;</div>
	  <table>
		<tr>
			<td>
				<input type="button" value="<insta:ltext key='patient.registration.button.save'/>" style="cursor:pointer;" onclick="return savePatientInsPlanDetails();" />
			</td>
			<td>
				<input type="button" value="<insta:ltext key='patient.registration.button.cancel'/>" style="cursor:pointer;" onclick="handleInsurancePlanDetailsDialogCancel();"/>
			</td>
		</tr>
	</table>
  </div>
</div>

</form>
</body>
</html>
