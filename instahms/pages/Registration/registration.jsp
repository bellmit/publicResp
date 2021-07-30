<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.commons.beanutils.BasicDynaBean"%>
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%
	BasicDynaBean genericPreferences = GenericPreferencesDAO.getAllPrefs();
%>
<c:set var="allow_all_cons_types_in_reg" value='<%=genericPreferences.get("allow_all_cons_types_in_reg") %>'/>
<c:set var="corpInsurance" value='<%=genericPreferences.get("corporate_insurance")%>' scope="request"/>
<c:set var="hijricalendar" value='<%= genericPreferences.get("hijricalendar")%>' />
<c:set var="is_patient_phone_mandate" value="N"/>
<c:if test="${not empty regPref.patientPhoneValidate &&
			(regPref.patientPhoneValidate eq 'A' ||
			(screenId eq 'ip_registration' && regPref.patientPhoneValidate eq 'I') ||
			(screenId eq'out_pat_reg' && regPref.patientPhoneValidate eq 'O')) }">
	<c:set var="is_patient_phone_mandate" value="Y" />
</c:if>

<c:set var="is_patient_email_mandate" value="N"/>
<c:if test = "${not empty regPref.validate_email_id &&
			(regPref.validate_email_id eq 'A' ||
			(screenId eq 'ip_registration' && regPref.validate_email_id eq 'I') ||
			(screenId eq'out_pat_reg' && regPref.validate_email_id eq 'O'))}">
	<c:set var="is_patient_email_mandate" value="Y" />
</c:if>
												
<c:set var="is_patient_care_oftext_mandate" value="N" />
<c:if test="${not empty regPref.nextOfKinValidate &&
			(regPref.nextOfKinValidate eq 'A' ||
			(screenId eq 'ip_registration' && regPref.nextOfKinValidate eq 'I') ||
			(screenId eq'out_pat_reg' && regPref.nextOfKinValidate eq 'O'))}">
	<c:set var="is_patient_care_oftext_mandate" value="Y" />
</c:if>
<c:if test="${screenId eq 'ip_registration'}"><c:set var="ga_page_label" value="IP Registration" /></c:if>
<c:if test="${screenId eq 'out_pat_reg'}"><c:set var="ga_page_label" value="Outside Patient Registration" /></c:if>
<c:set var= "patientIdentification" value = "${ifn:cleanJavaScript(centerPrefs.map.patient_identification)}" />
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%
	String msg = (String) request.getAttribute("msg");
	if ((msg == null)) {
		msg = "";
	}
%>
<%@page import="org.apache.struts.util.MessageResources,
					org.apache.struts.Globals" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<c:set var="mod_mrd_icd" value="${preferences.modulesActivatedMap.mod_mrd_icd}"/>
<title>
	<c:if test="${screenId eq 'ip_registration'}"><insta:ltext key="registration.patient.commonlabel.ipregistration.instahms"/></c:if>
	<c:if test="${screenId eq 'out_pat_reg'}"><insta:ltext key="registration.patient.commonlabel.outsidepatientregistration.instahms"/></c:if>
</title>
<meta name="i18nSupport" content="true"/>
<script>
  var genPrefs = ${genPrefs}
	var uhidPatient = '${uhidPatient}';
	var uhidPatientFirstName = '${uhidPatientFirstName}';
	var uhidPatientMiddleName = '${uhidPatientMiddleName}';
	var uhidPatientLastName = '${uhidPatientLastName}';
	var uhidPatientPhone = '${uhidPatientPhone}';
	var uhidPatientGender = '${uhidPatientGender}';
	var uhidPatientUHID = '${uhidPatientUHID}';
	var uhidPatientAge = '${uhidPatientAge}';
	var defaultCountryCode= "+${defaultCountryCode}";
	var screenId = "${screenId}";
    var hijriPref = '${hijricalendar}';
	var salutationJSON = ${salutationQueryJson};
	var patientConfidentialityCategoriesJSON = ${patientConfidentialityCategories};

	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	var regPref = ${regPrefJSON};
	var centerPrefs = ${centerPrefsJson}
	var healthAuthoPref = ${healthAuthoPrefJSON};

	var registrationChargeApplicability = '${actionRightsMap.reg_charges_app}'
	var editFirstName = '${actionRightsMap.edit_first_name}';
	var catChangeRights = '${actionRightsMap.patient_category_change}';
	var allowNewRegistration = '${actionRightsMap.allow_new_registration}';
	var allowBackdate = "${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_backdate}";
	var allowBackDateBillActivities = "${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_back_date_bill_activities}";
	var roleId='${ifn:cleanJavaScript(roleId)}';
	var contextPath = '<%= request.getContextPath()%>';
	var complaintField = regPref.complaintValidate;
	var categoryExpirydateText = regPref.categoryExpiryDate;
	var oldRegNumFieldText = regPref.oldRegNumField;
	var referal_for_life = regPref.referal_for_life;
	var deptUnitSetting = regPref.deptUnitSetting;
	var defaultIpBillType = centerPrefs.pref_default_ip_bill_type;
	var defaultOpBillType = centerPrefs.pref_default_op_bill_type;
	var patientCategory = regPref.patientCategory;
	var opDefaultSelection = regPref.opDefaultSelection;
	var ipDefaultSelection = regPref.ipDefaultSelection;
	var outsideDefaultSelection  = regPref.outsideDefaultSelection;
	var enableDistrict = regPref.enableDistrict;
	var patient_reg_basis  = regPref.patient_reg_basis;
	var opCategoryDefaultSelection = centerPrefs.pref_op_default_category;
	var ipCategoryDefaultSelection = centerPrefs.pref_ip_default_category;
	var ospCategoryDefaultSelection = centerPrefs.pref_osp_default_category;
	var smartCardEnabled = centerPrefs.pref_smart_card_enabled;
	var smartCardIDPattern = centerPrefs.smart_card_id_pattern;
	var insuranceCardMandatory = "Y";
	var priorAuthRequired = regPref.prior_auth_required;
	var visitTypeDependence = regPref.visit_type_dependence;
	var allowMultipleActiveVisits = regPref.allow_multiple_active_visits;
	var memberIdLabel = regPref.member_id_label;
	var corpInsuranceCheck = '${corpInsurance}';
	var memberIdValidFromLabel = regPref.member_id_valid_from_label;
	var memberIdValidToLabel = regPref.member_id_valid_to_label;
	var patientType = 'o';
	var appointmentDetailsList = <%= request.getAttribute("appointmentDetailsList") %>;
	var admissionRequestDetails = <%= request.getAttribute("admissionRequestDetails") %>;
	var admissionReqId = <%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getAttribute("adm_request_id")) %>;
	var gAppointmentId = <%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getAttribute("appointmentId")) %>;
	<c:if test="${screenId eq 'out_pat_reg'}">
	var defaultPatientCategory = opCategoryDefaultSelection;
	patientType = 'o';
	</c:if>
	<c:if test="${screenId eq 'ip_registration'}">
	var defaultPatientCategory = ipCategoryDefaultSelection;
	patientType = 'i';
	</c:if>
	var allowFieldEdit = "${actionRightsMap.edit_custom_fields == 'A' || roleId == '1' || roleId == '2' ? 'A' : 'N' }";
	var screenid = '${screenId}';
	var printReceiptNosList =  <%= request.getAttribute("returnReceiptList") %> ;
    var printPaymentTypeList =  <%= request.getAttribute("returnPaymentTypeList") %> ;
    var gPrintReceiptPrinterType = '${ifn:cleanJavaScript(param.printerType)}';
    var billNo = '${ifn:cleanJavaScript(param.billNo)}';
    var billingCounterId = '${billingCounterId}';
	var creditBillScreen = '${urlRightsMap['credit_bill_collection']}';
	var orderItemsUrl = '${orderItemsUrlWithoutOrgTpa}';
	var customTemplate = '${ifn:cleanJavaScript(param.customTemplate)}';
	var visitId = '${ifn:cleanJavaScript(param.visitid)}';
	cpath='${cpath}';
	luxuryTaxApplicableOn = genPrefs.luxTax;
	gPrescDocRequired = genPrefs.prescribingDoctorRequired;
	var consultationValidityUnits = '${clinicalPrefs.consultation_validity_units}';
	var gOnePrescDocForOP = genPrefs.op_one_presc_doc;
	var consultingDocName = '';
	var InsuranceModuleEnabled ='${preferences.modulesActivatedMap['mod_insurance']}';
	var modAdvInsurance ='${preferences.modulesActivatedMap['mod_adv_ins']}';
	var modMrdIcdEnabled ='${preferences.modulesActivatedMap['mod_mrd_icd']}';
	var mod_adv_packages = '${preferences.modulesActivatedMap['mod_adv_packages']}';
	var mod_mobile = '${preferences.modulesActivatedMap['mod_mobile']}';
	var forceSubGroupSelection = genPrefs.forceSubGroupSelection;
	var doctorConsultationTypes =  <%= request.getAttribute("doctorConsultationTypes") %>;
	var allDoctorConsultationTypes =  <%= request.getAttribute("allDoctorConsultationTypes") %>;
	var fixedOtCharges = genPrefs.fixedOtCharges;
	var goLiveDate = new Date(genPrefs.go_live_date.time);

	var anaeTypes = <%= request.getAttribute("anaeTypesJSONs") %>;
	var agedisable = '${ifn:cleanJavaScript(regPref.allow_age_entry)}';
	var mrno = null;
	var MRNo = '${ifn:cleanJavaScript(param.mr_no)}'; // taken as a separate parameter to aviod calling init method from orders.js file.
    var doctorId = '${ifn:cleanJavaScript(param.doctor_id)}';
	var consDateTime = '${ifn:cleanJavaScript(param.cons_date_time)}';
	var patientName = '${ifn:cleanJavaScript(param.patient_name)}';
	var patientPhone = '${ifn:cleanJavaScript(param.mobile_no)}';
	var salutation = '${ifn:cleanJavaScript(param.patient_salutation)}';
	var patientMName = '${ifn:cleanJavaScript(param.patient_middle_name)}';
	var patientLName = '${ifn:cleanJavaScript(param.patient_last_name)}';
	var patientDOB = '${ifn:cleanJavaScript(param.patient_dob)}';
	var patientAge = '${ifn:cleanJavaScript(param.patient_age)}';
	var patientGender = '${ifn:cleanJavaScript(param.pat_gender)}';
	var nKinName = '${ifn:cleanJavaScript(param.next_of_kin_name)}';
	var patientAddress = '${ifn:cleanJavaScript(param.pat_address)}';
	var patientCity = '${ifn:cleanJavaScript(param.pat_city)}';
	var patientEmail = '${ifn:cleanJavaScript(param.patient_email)}';
	var docDeptName = '<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getAttribute("docDeptName")) %>';
	var docName = '<%= request.getAttribute("docName") %>';
	var docDeptId = '<%= request.getAttribute("docDeptId") %>';
	var dutyDoctorReq = '${ip_prefs.map.duty_doctor_selection}';
	var allocBed = '${ip_prefs.map.allocate_bed_at_reg}';
	var isAllocBed = !empty(allocBed) && allocBed == 'Y';
	var allowBillNowInsurance = '${allowBillNowInsurance}';
	<c:if test="${screenId == 'out_pat_reg'}">
		allowBillNowInsurance = 'true';
	</c:if>
	<c:if test="${registrationType != null}">
		var registerType = '${ifn:cleanJavaScript(registrationType)}';
	</c:if>

	var isModAdvanceIns = !empty(modAdvInsurance) && modAdvInsurance == 'Y';
	var isModInsurance  = !empty(InsuranceModuleEnabled) && InsuranceModuleEnabled == 'Y';
	var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
	var advancedOTModule = '${preferences.modulesActivatedMap['mod_advanced_ot']}';
	advancedOTModule = (empty(advancedOTModule)) ? 'N' : advancedOTModule;
	var allOrdersJSON = new Array();
	var newOrdersMap = null;
	var mainConsultations = new Array();
	var editOrders = true;
	var max_centers = genPrefs.max_centers_inc_default;
	var centerId = ${centerId};
	var scheduleName = null;
	<c:if test="${scheduleName != null}">
		scheduleName = '${scheduleName}';
	</c:if>
	var category = null;
	<c:if test="${category != null}">
		category = '${category}';
	</c:if>

	var showChargesAllRatePlan = ('${ifn:cleanJavaScript(roleId)}' == '1' || '${ifn:cleanJavaScript(roleId)}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
	var defaultArea = '${defaultArea}';
	var defaultCity = '${defaultCity}';
	var defaultDistrict = '${defaultDistrict}';
	var defaultState = '${defaultState}';
	var defaultCountry = '${defaultCountry}';
	var defaultAreaName = '${defaultAreaName}';
	var defaultCityName = '${defaultCityName}';
	var defaultDistrictName = '${defaultDistrictName}';
	var defaultStateName = '${defaultStateName}';
	var defaultCountryName = '${defaultCountryName}';

	var billingBedTypeForOp = genPrefs.billingBedTypeForOP;
	var gToothNumberRequired = true;
	var allow_all_cons_types_in_reg = '${allow_all_cons_types_in_reg}';
	var insured = false;
	var advancedPackageModule = '${preferences.modulesActivatedMap['mod_adv_packages']}';
	advancedPackageModule = (empty(advancedOTModule)) ? 'N' : advancedPackageModule;
	var multiPlanExists = false;
	var govtId_pattern = '';
	var govtId_label = regPref.government_identifier_label;
	var govtId_type_label = regPref.government_identifier_type_label;
	var govtIdentifierTypesJSON = <%=request.getAttribute("govtIdentifierTypesJSON")%>;
	var patientIdentification = '${ifn:cleanJavaScript(centerPrefs.map.patient_identification)}';
	var govtIdentifierMandatory = '';
	var uniqueGovtID = '';
	var channellingOrders = <%= request.getAttribute("channellingOrdersList") %>;
	var dept_id = '<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getAttribute("dept_id")) %>';
	var allowCopayChange = '${actionRightsMap.allow_dynamic_copay_change}';
	var schedulerGenerateOrder = genPrefs.schedulerGenerateOrder;
	var apptDetailsExcludingOrders = <%=request.getAttribute("apptDetailsExcludingOrders")%>;
	
	var isPatientPhoneMandate= '${is_patient_phone_mandate}';
	var isPatientEmailMandate= '${is_patient_email_mandate}';
	var isPatientCareOftextMandate = '${is_patient_care_oftext_mandate}';
	var patientPhoneInitialValue = '';
	var kinPhoneInitialValue= '';
	var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
	var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
	var patientConfidentialityGroup= '';
	var patientConfidentialityGroupName= '';
</script>

<insta:link type="js" file="orders/orders.js" />
<insta:link type="js" file="orders/test_additional_details.js"/>
<insta:link type="js" file="phoneNumberUtil.js"/>
<insta:link type="js" file="registration/registration.js" />
<insta:link type="js" file="registration/registrationInsuranceCommon.js"/>
<insta:link type="js" file="registration/registrationCommon.js"/>
<insta:link type="js" file="registration/visitInsuranceDetails.js"/>
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="instaautocomplete.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="lightbox.js" />
<c:if test="${screenId eq 'ip_registration'}">
<insta:link type="js" file="orderdialog.js" />
</c:if>
<insta:link type="js" file="ordertable.js" />
<insta:link type="js" file="doctorConsultations.js" />
<insta:link type="js" file="outpatient/prescribe.js"/>
<insta:link type="js" file="areacommon.js"/>

<insta:link type="css" file="select2.min.css"/>
<insta:link type="css" file="select2Override.css"/>
<insta:link type="css" file="lightbox.css" />
<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<insta:link type="js" file="medicalrecorddepartment/mrdupdatescreen.js"/>
<insta:link type="js" file="outpatient/diagnosis_details.js"/>
<insta:link type="js" file="copyPaste.js"/>
<insta:link type="script" file="moment.min.js"/>

<style type="text/css">

/* todo: this remains here, should not move to style.css */

  table#visitType td {padding: 3px 2px 3px 2px;}
	.yui-ac {
			padding-bottom: 20px;
	}
	#autoarea {
		width: 12.1em;
		position: absolute;
		display: inline;
	}
	#policyNoAutoComplete {
		display: inline;
		padding-bottom: 2px;
	}
	.scrolForContainer .yui-ac-content{
		 max-height:11em;overflow:auto;overflow-x:auto;width: 350px; /* scrolling */
	}
	.yui-ac-input {
		/* position:relative; */
	}

	table#patientinfo td {
		width: 148px;
	}
	table#patientinfo td.formlabel {
		width: 102px;
	}
	.info-overlay {
			color:#000;
			border-color: #D3D9E0 #AFB4BA #AFB4BA;
			padding:6px;
			border-width:1px;
			border-style:solid;
			background-color: #E4EBF3;
			margin:10px;
	}
	td.patient_group_td {
		display: none;
	}

</style>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="ui.notification.registration.patient"/>
<insta:js-bundle prefix="common.message"/>
<insta:js-bundle prefix="patient.consultation"/>
<insta:js-bundle prefix="order.common"/>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
<insta:js-bundle prefix="patient.diagnosis"/>
</head>

<body onload="initRegistration();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<%--
  This form is used only to trap Enter key in the MRNO textbox, without having
  a submit button. It works in all browsers as long as there is only one textfield
  in the form, onsubmit is called. We return false always as we don't want the form
  to be actually submitted.
--%>
<form name="mrnoform" action="ShouldNotBeCalled" onsubmit="return false;" style="margin: 0">
	<table class="formtable">
		<tr>
			<td>
				 <c:if test="${screenId eq 'ip_registration'}">
	                <h1><span class="pageHeader"><insta:ltext key="registartion.patient.in.heading"/></span></h1>
					<input type="hidden" name="group" id="groupIp" value="ipreg">
	             </c:if>
	             <c:if test="${screenId eq 'out_pat_reg'}">
					<h1><insta:ltext key="registration.patient.outside.heading"/></h1>
					<input type="hidden" name="group" id="groupOp" value="opreg">
				 </c:if>
             </td>
		</tr>
	</table>
	<table width="100%">
		<tr>
			<%
			  int max_centers = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
				int centerId = (Integer) session.getAttribute("centerId");

				MessageResources bundle = (MessageResources)request.getAttribute(Globals.MESSAGES_KEY);
				String msg1 = bundle.getMessage("registration.patient.action.message.registration.allowed.only.for.center.users");
				//error msg not converted into i18n message
				if (max_centers >1 && centerId == 0)
					request.setAttribute("error", msg1);

			%>
			<td><insta:feedback-panel/></td>
		</tr>
	</table>
	<table width="100%" class="formtable">
		<tr>
			<td>
				<fieldset class="fieldSetBorder" id="patDetFieldset" style="height:80px;">
				<legend class="fieldSetLabel"><insta:ltext key="registration.patient.commonlabel.patient.details"/></legend>
					<table class="formtable" width="100%">
						<tr>
							<td>
								<div id="newRegType">
									<input type="radio" value="new" checked name="regType" id="regTypenew"
										onclick="clearRegDetails()">
									<label for="regTypenew"><insta:ltext key="registration.patient.commonlabel.newregistration"/>:</label>
								</div>
							</td>
							<td></td>
							<td>
								<input type="checkbox" name="mlccheck" id="mlccheck" onclick="populateMLCTemplates()">
								<label for="mlccheck"><insta:ltext key="registration.patient.commonlabel.medicoLegalCase"/></label>
								<input type="button" name="mlcBtn" onclick="showMlcDialog()" value="..." disabled/>
							</td>
							<td >
							</td>
						</tr>
						<tr>
							<td valign="top">
								<input type="radio" value="regd" name="regType" id="regTyperegd" onclick="clearRegDetails()"
									accesskey="M">
								<label for="regTyperegd"><u><b><insta:ltext key="registration.patient.label.m"/></b></u><insta:ltext key="registration.patient.label.RNoNamePhone"/>:</label>&nbsp;&nbsp;
								<div id="mrnoAutocomplete" style="display: inline; position: absolute;">
									<input type="text" id="mrno" name="mrno" class="field" disabled
										style="width:135px;height:21px; display: inline;"/>
									<div id="mrnoAcDropdown" style="width: 34em;"></div>
								</div>
							</td>
							<td align="center">
								<c:if test="${centerPrefs.map.pref_smart_card_enabled == 'Y'}">
								<c:if test="${not empty regPref.government_identifier_label}">
									<insta:analytics tagType="button" type="button" name="readCard" id="readCard" clickevent="return readFromCard();" style="width:100px"
									 	category="Registration" action="Read Card" label="${ ga_page_label }">
										<img src="${cpath}/images/CardIcon.png" width="23" height="18" align="left"/>
										<span style="margin-top:1px;position:absolute;margin-left:-23px; " >ReadData</span>
									</insta:analytics>
								</c:if>
								</c:if>
							</td>
							<td>
								<div style="float:left">
									<label id="prevVisitTag" style="display: none;"><insta:ltext key="registration.patient.commonlabel.previousVisit"/>:&nbsp;&nbsp;</label>
								</div>
								<div style="float:left">
									<label class="dark bold" id="prvsDoctor"></label>
									<label class="dark bold" id="prvsDate"></label>
								</div>
							</td>
							<%-- Label for mlc dialog to be displayed --%>
							<td width="50px" align="center">
								<label id="openmlc"></label>
								<c:if test="${regPref.allow_multiple_active_visits == 'Y'}">
									<label for="close_last_active_visit"><insta:ltext key="registration.patient.commonlabel.closePreviousActiveVisit"/></label>
									<input type="checkbox" name="close_last_active_visit" id="close_last_active_visit" onclick="setLastVisitToClose();"/>
								</c:if>
							</td>
						</tr>
					</table>
					<table id="patientDetailsTable" width="100%" class="formtable">
					</table>
				</fieldset>
			</td>
		</tr>
	</table>
</form>
<c:set var="mobilepatientaccess">
	<insta:ltext key="registration.patient.preregistration.details.yes"/>,
	<insta:ltext key="registration.patient.preregistration.details.no"/>
</c:set>
<c:set var="addnlOpen">
	<c:choose>
		<c:when test="${screenId eq 'ip_registration'}">true</c:when>
		<c:when test="${hasMadatoryAddlnFields}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>

<c:set var="visitAddnlOpen">
	<c:choose>
		<c:when test="${screenId eq 'ip_registration'}">true</c:when>
		<c:when test="${hasMadatoryVisitAddlnFields}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>

<c:set var="corpInsurancehid" scope="request">
	<c:choose>
		<c:when test="${corpInsurance eq 'Y'}">display:none</c:when>
		<c:otherwise></c:otherwise>
	</c:choose>
</c:set>
<c:set var="allowAgeEntry" value="<%=RegistrationPreferencesDAO.getRegistrationPreferences().getAllow_age_entry()%>" />
<script>
	var allowAgeEntry = "${allowAgeEntry}";
</script>
<form name="mainform" method="POST" action="IpRegistration.do" enctype="multipart/form-data" style="margin: 0" autocomplete="off">
	<jsp:include flush="true" page="/pages/outpatient/CommonInclude.jsp"/>

	<input type="hidden" name="area" id="area_id" value="${defaultArea}"/>
    <input type="hidden" name="patient_city" id="city_id" value="${defaultCity}"/>
	<input type="hidden" name="patient_district" id="district_id" value=""/>
    <input type="hidden" name="patient_state" id="state_id" value="${defaultState}"/>
    <input type="hidden" name="country" id="country_id" value="${defaultCountry}"/>
	<input type="hidden" name="dept_allowed_gender">
	<input type="hidden" name="screenId" value="${screenId}">
	<input type="hidden" name="mrno" /> <%-- copy of mrno in mrno form which needs to be submitted --%>
	<input type="hidden" name="regType">
	<input type="hidden" name="group" />
	<input type="hidden" name="consRevisit" value="false"/>
	<input type="hidden" name="ipwardname" />
	<input type="hidden" name="reg_charge_applicable" value="Y" />
	<input type="hidden" name="_method" />
	<input type="hidden" name="consFees" id="consFees">
	<input type="hidden" name="imageUrl" value="Y" />
	<input type="hidden" name="insuranceId" value="" />
	<input type="hidden" name="opdocchrg" id="opdocchrg" value="" />
    <input type="hidden" name="opIpcharge" id="opIpcharge" value="" />
	<input type="hidden" name="dateOfBirth" id="dateOfBirth">
	<input type="hidden" name="patientMlcStatus" value="N" />
	<input type="hidden" name="patientType" value="GENERAL" />
	<input type="hidden" name="patientReferaldoctorType" value="" />
	<input type="hidden" id="cardExpiryDate" />
	<input type="hidden" name="mlc_template_id" value="" />
	<input type="hidden" name="mlc_template_name" value="" />
    <input type="hidden" name="areaValidate" value="${ifn:cleanHtmlAttribute(regPref.areaValidate)}" />
	<input type="hidden" name="addressValidate" value="${ifn:cleanHtmlAttribute(regPref.addressValidate)}" />
	<input type="hidden" name="validateEmailId" value="${ifn:cleanHtmlAttribute(regPref.validate_email_id)}"/>
	<input type="hidden" name="nextofkinValidate" value="${ifn:cleanHtmlAttribute(regPref.nextOfKinValidate)}" />
	<input type="hidden" name="patientPhoneValidate" value="${ifn:cleanHtmlAttribute(regPref.patientPhoneValidate)}" />
	<input type="hidden" name="referredbyValidate" value="${ifn:cleanHtmlAttribute(regPref.referredbyValidate)}" />
	<input type="hidden" name="conductingdoctormandatory" value="${ifn:cleanHtmlAttribute(regPref.conductingdoctormandatory)}"/>
	<input type="hidden" name="main_visit_id" value="">
	<input type="hidden" name="regAndBill" value="N">
	<input type="hidden" name="previousConsultationId" value="" />
	<input type="hidden" name="previousVisit" value="N">
	<input type="hidden" name="referer" value="${ifn:cleanHtmlAttribute(referer)}">
	<input type="hidden" name="appointmentId" value="">
	<input type="hidden" name="adm_request_id" value="">
	<input type="hidden" name="category" value="">
	<input type="hidden" name="scheduleName" value="${scheduleName}">
	<input type="hidden" name="last_active_visit" value="">
   <input type="hidden" name="pat_package_id" id="pat_package_id" value="${pat_package_id}"/>
	<jsp:useBean id="todayDate" class="java.util.Date"/>
	<input type="hidden" name="current_date" id="current_date" value="<fmt:formatDate value="${todayDate}" pattern="dd-MM-yyyy"/>">
    <input type="hidden" name="current_time" id="current_time" value="<fmt:formatDate value="${todayDate}" pattern="HH:mm"/>">
    <input type="hidden" name="visitLimitsChanged" id="visitLimitsChanged" value="N" />
    <input type="hidden" name="schedulerCategory" value="${ifn:cleanHtmlAttribute(category)}" />
    <input type="hidden" name="resource_captured_from" id="resource_captured_from" value="register"/>
    <input type="hidden" name="cardImage" id="cardImage" value=""/>
    <input type="hidden" name="uhidPatientUHID" id="uhidPatientUHID" value=""/>

	<table width="100%" class="formtable">
		<tr>
			<td>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="registration.patient.commonfieldset.basicInformation"/></legend>
						<table id="patientinfo" class="formtable" width="100%">
							<tr>
								<c:set var="patTitle">
									<insta:ltext key="js.registration.patient.show.title"/>
								</c:set>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.patinentName"/>:</td>
								<td style="white-space: nowrap; width: 350px" colspan="3">
									<insta:selectdb id="salutation" name="salutation" value="${patTitle}" table="salutation_master"
										valuecol="salutation_id"  displaycol="salutation" usecache="true"
										class="dropdown" onchange="salutationChange()" dummyvalue="${patTitle}"  style="width: 60px;"/>
									<input type="text" name="patient_name"  id="patient_name" class="field"
										value="..FirstName.." maxlength="50"
										onblur="capWords(patient_name);if (this.value == '') { this.value = '..FirstName..'}"
										onFocus="if (this.value == '..FirstName..') { this.value = ''}"  style="width: 120px;">
									<input type="text" name="middle_name" id="middle_name" size="15"  class="field"
										maxlength="50" value="..MiddleName.."
										onblur="capWords(middle_name);if (this.value == '') { this.value = '..MiddleName..'}"
										onFocus="if (this.value == '..MiddleName..') { this.value = ''}"  style="width: 120px;">
									<c:if test="${regPref.name_parts == 4}">
									<input type="text" name="middle_name2" id="middle_name2" size="15"  class="field"
										maxlength="50" value="..MiddleName2.."
										onblur="capWords(middle_name2);if (this.value == '') { this.value = '..MiddleName2..'}"
										onFocus="if (this.value == '..MiddleName2..') { this.value = ''}"  style="width: 120px;">
									</c:if>
									<input type="text" name="last_name" id="last_name" size="15"  class="field"
										maxlength="50" value="..LastName.."
										onblur="capWords(last_name);if (this.value == '') { this.value = '..LastName..'}"
										onFocus="if (this.value == '..LastName..') { this.value = ''}"  style="width: 120px;">
										<span class="star">*</span>
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.patinentPhone"/>:</td>
								<td colspan="2">
								<div style="margin-top:12px;">
									<div>
										<input type="hidden" id="patient_phone" name="patient_phone"/>
										<input type="hidden" id="patient_phone_valid" value="N"/>
										<select id="patient_phone_country_code" class="dropdown" style="width:76px;" name="patient_phone_country_code">
											<c:if test="${empty defaultCountryCode}">
													<option value='+' selected> - Select - </option>
											</c:if>
											<c:forEach items="${countryList}" var="list">
												<c:choose>
													<c:when test="${ list[0] == defaultCountryCode}">
														<option value='+${list[0]}' selected>+${list[0]}(${list[1]})</option>
													</c:when>
													<c:otherwise>
														<option value='+${list[0]}'>+${list[0]}(${list[1]})</option>
													</c:otherwise>
												</c:choose>
											</c:forEach>
										 </select>
										 <input type="text" class="field" id="patient_phone_national" maxlength ="15" onkeypress="return enterNumOnlyzeroToNine(event)"
												 style="width:9.6em;padding-top:1px" />
												<span class="patient_phone_star">*</span>
											
											<img class="imgHelpText" id="patient_phone_help" src="${cpath}/images/help.png"/>
									</div>
									<div>
										<span style="visibility:hidden;padding-left:10px;color:#f00" id="patient_phone_error"></span>
									</div>

								</div>
	          					</td>
							</tr>
							<c:if test="${regPref.name_local_lang_required == 'Y'}">
							<tr>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.patinentnameinlocallang"/>:</td>
								<td colspan=6>
									<input type="text" class="field" name="name_local_language" id="name_local_language"
										maxlength="100" style="width:350px">
								</td>
							</tr>
							</c:if>
							<tr>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.patientdateOfBirth"/>:</td>
								<td style="width: 230px; white-space: nowrap;" colspan=6>
									<input type="text" class="field" style="width:30px;" size="2"
										maxlength="2"  name="dobDay" onkeypress="return enterNumOnly(event)"
									 	id="dobDay" value="<insta:ltext key="registration.patient.show.dd.text"/>" onFocus="if (this.value == '<insta:ltext key="registration.patient.show.dd.text"/>') { this.value = ''}"
										onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.dd.text"/>'; enableAge();} else { if(hijriPref == 'Y') gregorianToHijri(); }" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("gregDay")) %>">



									<input type="text" class="field" style="width:30px;" size="2"
										maxlength="2" name="dobMonth" id="dobMonth" value="<insta:ltext key="registration.patient.show.mm.text"/>"
										onFocus="if (this.value == '<insta:ltext key="registration.patient.show.mm.text"/>') { this.value = ''}"
										onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.mm.text"/>'; enableAge();} else { if(hijriPref == 'Y') gregorianToHijri(); }" onkeypress="return enterNumOnly(event)" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("gregMonth")) %>" >



									<input type="text" class="field" size="4" maxlength="4" name="dobYear" id="dobYear"
										style="width:40px;" onblur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.yy.text"/>'; enableAge(); } else { return calculateAgeAndHijri(); }"
										 value="<insta:ltext key="registration.patient.show.yy.text"/>" onkeypress="return enterNumOnly(event)"
										onFocus="if (this.value == '<insta:ltext key="registration.patient.show.yy.text"/>') { this.value = ''}" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("gregYear")) %>"><insta:ltext key="registration.patient.commonlabel.or.within.brackets"/>

									<c:choose>
										<c:when test="${allowAgeEntry eq 'Y'}">
										<input type="text" class="field" name="age" id="age" size="5"
											style="width:45px;"
											onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.age.text"/>'; enableDobAndHijriDob();} else dissableDobAndHijriDob()"
											value="<insta:ltext key="registration.patient.show.age.text"/>" onFocus="if (this.value == '<insta:ltext key="registration.patient.show.age.text"/>') { this.value = ''}"
											onkeypress="return enterNumOnlyzeroToNine(event)" />
										</c:when>
										<c:otherwise>
											<input type="text" class="field" name="age" id="age" size="5"
											style="width:45px;"  readonly
											onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.age.text"/>'}"
											value="<insta:ltext key="registration.patient.show.age.text"/>" onFocus="if (this.value == '<insta:ltext key="registration.patient.show.age.text"/>') { this.value = ''}"
											onkeypress="return enterNumOnlyzeroToNine(event)" />
										</c:otherwise>
									</c:choose>

									<c:choose>
										<c:when test="${allowAgeEntry eq 'Y'}">
											<select name="ageIn" id="ageIn" class="dropdown" style="width:70px;">
												<option value="Y"><insta:ltext key="registration.patient.commonselectbox.ageIn.years"/></option>
												<option value="M"><insta:ltext key="registration.patient.commonselectbox.ageIn.months"/></option>
												<option value="D"><insta:ltext key="registration.patient.commonselectbox.ageIn.days"/></option>
											</select>
										</c:when>
										<c:otherwise>
											<select name="ageIn" id="ageIn" class="dropdown" style="width:70px;" disabled>
												<option value="Y"><insta:ltext key="registration.patient.commonselectbox.ageIn.years"/></option>
												<option value="M"><insta:ltext key="registration.patient.commonselectbox.ageIn.months"/></option>
												<option value="D"><insta:ltext key="registration.patient.commonselectbox.ageIn.days"/></option>
											</select>
										</c:otherwise>
									</c:choose>
									<span class="star">*</span>
									</td>
								</tr>
								<tr>
								<c:if test="${hijricalendar=='Y'}">
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.patientdateOfBirth.hijri"/>:</td>
								<td style="width: 120px; white-space: nowrap;">
									<input type="text" class="field" style="width:30px;" size="2"
											maxlength="2"  name="dobHDay" onkeypress="return enterNumOnly(event)"
										 	id="dobHDay" value="<insta:ltext key="registration.patient.show.dd.text"/>" onFocus="if (this.value == '<insta:ltext key="registration.patient.show.dd.text"/>') { this.value = ''}"
											onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.dd.text"/>'; enableAge();} else { if(hijriPref == 'Y') hijriToGregorian(); }" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("hijriDay")) %>">



									<input type="text" class="field" style="width:30px;" size="2"
										maxlength="2" name="dobHMonth" id="dobHMonth" value="<insta:ltext key="registration.patient.show.mm.text"/>"
										onFocus="if (this.value == '<insta:ltext key="registration.patient.show.mm.text"/>') { this.value = ''}"
										onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.mm.text"/>'; enableAge();} else { if(hijriPref == 'Y') hijriToGregorian();}" onkeypress="return enterNumOnly(event)" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("hijriMonth")) %>">



									<input type="text" class="field" size="4" maxlength="4" name="dobHYear" id="dobHYear"
										style="width:40px;" onblur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.yyyy.text"/>'; enableAge(); } else { if(hijriPref == 'Y') hijriToGregorian();}"
										 value="<insta:ltext key="registration.patient.show.yyyy.text"/>" onkeypress="return enterNumOnly(event)"
										onFocus="if (this.value == '<insta:ltext key="registration.patient.show.yyyy.text"/>') { this.value = ''}" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("hijriYear")) %>"><%-- <insta:ltext key="registration.patient.commonlabel.or.within.brackets"/> --%>
									<img title='<insta:ltext key="patient.registration.hijri.calendar.range.note"/>' src="${cpath}/images/help.png" class="imgHelpText">
								</td>
								</c:if>



								<td class="formlabel" style="width: 70px"><insta:ltext key="registration.patient.commonlabel.gender"/>:</td>
								<td style="white-space: nowrap">
									<select class=" dropdown"  name="patient_gender" id="patient_gender" onchange="onGenderChange()">
											<option value="N"><insta:ltext key="registration.patient.commonselectbox.patientGender.defaultText"/></option>
											<option value="M"><insta:ltext key="registration.patient.commonselectbox.patientGender.male"/></option>
											<option value="F"><insta:ltext key="registration.patient.commonselectbox.patientGender.female1"/></option>
											<option value="C"><insta:ltext key="registration.patient.commonselectbox.patientGender.couple"/></option>
											<option value="O"><insta:ltext key="registration.patient.commonselectbox.patientGender.others"/></option>
									</select>
									<span class="star">*</span>
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.addnlPhone"/>:</td>
								<td colspan=6>
									<input type="text" class="field" name="patient_phone2" id="patient_phone2"
										maxlength="15"  >
								</td>
							</tr>

							<tr>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.nextOfKinName"/>:</td>
								<td>
									<input type="text" name="relation" id="relation" class="field"
										size="16" maxlength="100">
									<c:if test="${is_patient_care_oftext_mandate == 'Y'}">
										<span class="star">*</span>
									</c:if>
								</td>
								<td class="formlabel" style="width: 70px"><insta:ltext key="registration.patient.commonlabel.relation"/>:</td>
								<td>
									<input type="text" name="next_of_kin_relation" id="next_of_kin_relation"
									maxlength="30"/>
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.contactNo"/>.:</td>
								<td colspan=2>
									<div style="margin-top:12px" >
										<div>
											<input type="hidden" id="patient_care_oftext" name="patient_care_oftext"/>
											<input type="hidden" id="patient_care_oftext_valid" value="N"/>
											<select id="patient_care_oftext_country_code" class="dropdown" style="width:76px" name="patient_care_oftext_country_code">
												<c:if test="${empty defaultCountryCode}">
														<option value='+' selected> - Select - </option>
												</c:if>
												<c:forEach items="${countryList}" var="list">
													<c:choose>
														<c:when test="${ list[0] == defaultCountryCode}">
															<option value='+${list[0]}' selected>+${list[0]}(${list[1]})</option>
														</c:when>
														<c:otherwise>
															<option value='+${list[0]}'>+${list[0]}(${list[1]})</option>
														</c:otherwise>
													</c:choose>
												</c:forEach>
											</select>
											<input type="text" class="field"
												id="patient_care_oftext_national" maxlength="15"  onkeypress="return enterNumOnlyzeroToNine(event)"
												 style="width:9.6em;padding-top:1px" />
											<c:if test="${is_patient_care_oftext_mandate == 'Y'}">
												<span class="star">*</span>
											</c:if>
											<img class="imgHelpText" id="patient_care_oftext_help"
													src="${cpath}/images/help.png"/>
										</div>
										<div>
											<span style="visibility:hidden;padding-left:10px;color:#f00" id="patient_care_oftext_error"></span>
										</div>

									</div>
								</td>
							</tr>

							<tr>
								<c:if test="${regPref.patientCategory != '' && regPref.patientCategory != null}">
									<td class="formlabel">${ifn:cleanHtml(regPref.patientCategory)}:</td>
									<td>
										<select id="patient_category_id" name="patient_category_id" size="1"
											class="dropdown" onchange="onChangeCategory()">
											<option value="">${dummyvalue}</option>
										</select>
										<span class="star">*</span>
									</td>
								</c:if>
								<td class="formlabel patient_group_td"><insta:ltext key="ui.label.patient.confidential.group" />:</td>
								<td class="patient_group_td">
									<select id="patient_group" name="patient_group" size="1" class="dropdown" ></select>
									<span class="star">*</span>
								</td>
								 <c:if test="${regPref.caseFileSetting != null && regPref.caseFileSetting != ''
									&& regPref.caseFileSetting == 'Y'}">
								 <td class="formlabel"><insta:ltext key="registration.patient.commonlabel.caseFile"/>:</td>
								 <td>
									<input type="text" name="casefileNo" id="casefileNo" size="10"
									maxlength="20" onblur="enableCaseFileAutoGen();"
									onkeyup="upperCase(casefileNo)" onchange="return checkUniqueCasefileNo();">
								 </td>
								 <td colspan="2">
									<table id="caseFileFields" style="display:block">
										<tr>
											<td id="autoGenCaseFileDiv" style="display: inline;">
												<input type="checkbox" name="oldRegAutoGenerate" id="oldRegAutoGenerate" value="Y"
												onclick="enableOldmrno()">
												<label for="oldRegAutoGenerate"><insta:ltext key="registration.patient.commonlabel.auto"/>.</label>
											</td>
											<td id="caseFileIssuedDiv" style="display: none;white-space:nowrap;">
												<label><insta:ltext key="registration.patient.commonlabel.caseFileWith"/>:</label>
												<label id="caseFileIssuedBy" class="forminfo"></label>
												<input type="checkbox" name="raiseCaseFileIndent" id="caseFileRaiseIndent" value="Y" disabled/>
												<label for="caseFileRaiseIndent"><insta:ltext key="registration.patient.commonlabel.raiseIndent"/></label>
											</td>
										</tr>
									</table>
								</td>
								</c:if>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
		</table>

		<table class="formtable" >
			<tr>
				<td>
					<div id="CollapsiblePanel1" class="CollapsiblePanel">
						<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
							<div class="fltL " style="width: 230px; margin:5px 0 0 10px;"><insta:ltext key="registration.patient.label.additional"/>&nbsp;<insta:ltext key="registration.patient.label.patient"/>&nbsp;<b><u><insta:ltext key="registration.patient.label.i"/></u></b><insta:ltext key="registration.patient.label.nformation"/></div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
								<c:choose>
									<c:when test="${addnlOpen}"> <img src="${cpath}/images/up.png" /> </c:when>
									<c:otherwise> <img src="${cpath}/images/down.png" /> </c:otherwise>
								</c:choose>
							</div>
							<div class="clrboth"></div>
						</div>
						<fieldset class="fieldSetBorder">
						<table class="formtable">
							<tr>
								<td class="formlabel"><insta:ltext key="registration.patient.additionalinformation.commonlabel.address"/>:</td>
								<td rowspan="2">
									<textarea  class="field" style="width:150px; height:50px; padding:0 0 2px 2px"
										name="patient_address" id="patient_address"></textarea>
									<c:if test="${not empty regPref.addressValidate &&
											(regPref.addressValidate eq 'A' ||
												(screenId eq 'ip_registration' && regPref.addressValidate eq 'I') ||
												(screenId eq'out_pat_reg' && regPref.addressValidate eq 'O'))}">
									<span class="star">*</span>
									</c:if>
								</td>
								<td class="formlabel"><insta:ltext key="${regPref.enableDistrict == 'Y' ? 'ui.label.area.village' : 'ui.label.area' }" />:</td>
								<td>
									<table>
										<tr>
											<td valign="top">
												<div id="autoarea" class="autoComplete">
													<input name="patient_area" id="patient_area" type="text"
													value="${defaultAreaName}" style="width:11.6em" maxlength="50" />
													<div id="area_dropdown" style="width:250px"></div>
												</div>
											</td>
											<td class="formlabel" style="padding: 0 05px">
												<c:if test="${not empty regPref.areaValidate &&
														(regPref.areaValidate eq 'A' ||
															(screenId eq 'ip_registration' && regPref.areaValidate eq 'I') ||
															(screenId eq'out_pat_reg' && regPref.areaValidate eq 'O'))}">
												<span class="star">*</span>
												</c:if>
											</td>
										</tr>
									</table>
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.additionalinformation.commonlabel.state"/>:</td>
								<td><label id="statelbl" class="formlabel">${defaultStateName}</label></td>
								<c:if test="${regPref.enableDistrict == 'Y'}">
									<td class="formlabel"><insta:ltext key="ui.label.district"/>:</td>
									<td><label id="districtlbl" class="formlabel">${defaultDistrictName}</label></td>
								</c:if>
						   </tr>
							<tr>
								<td>&nbsp;</td>
								<td class="formlabel"><insta:ltext key="${regPref.enableDistrict == 'Y' ? 'ui.label.city.subdistrict' : 'ui.label.city' }" />:</td>
								<td>
									<table>
										<tr>
											<td>
												<div id="city_state_country_wrapper" class="autoComplete">
													<input type="text" name="pat_city_name" id="pat_city_name"
														value="${defaultCityName}" />
													<div id="city_state_country_dropdown" style="width:250px"></div>
												</div>
											</td>
											<td>
												<span class="star">*</span>
											</td>
										</tr>
									</table>
								</td>
							   	  <td class="formlabel"><insta:ltext key="registration.patient.additionalinformation.commonlabel.country"/>:</td>
							  	  <td><label id="countrylbl" class="formlabel">${defaultCountryName}</label></td>
							</tr>
							
							<tr>
								<td class="formlabel"><insta:ltext key="ui.label.master.blood.group"/>:</td>
								<td>
								<insta:selectdb name="blood_group_id" table="blood_group_master"
								dummyvalue="---Select---" dummyvalueId="" value="${blood_group_id}" valuecol="blood_group_id"
								displaycol="blood_group_name"
								filtered="true" filtercol="status" filtervalue="A"/>
								</td>
								
								<td class="formlabel"><insta:ltext key="ui.label.master.marital.status"/>:</td>
								<td>
								<insta:selectdb name="marital_status_id" table="marital_status_master"
								dummyvalue="---Select---" dummyvalueId="" value="${marital_status_id}" valuecol="marital_status_id"
								displaycol="marital_status_name"
								filtered="true" filtercol="status" filtervalue="A"/>
								<c:if test="${not empty regPref.maritalStatusRequired && (regPref.maritalStatusRequired eq 'Y' ||
										(screenId eq 'ip_registration' && regPref.maritalStatusRequired eq 'I') ||
										(screenId eq 'out_pat_reg' && regPref.maritalStatusRequired eq 'O'))}">
									<span class="star">*</span>
                                </c:if>
								</td>
								
								<td class="formlabel"><insta:ltext key="ui.label.master.religion"/>:</td>
								<td>
								<insta:selectdb name="religion_id" table="religion_master"
								dummyvalue="---Select---" dummyvalueId="" value="${religion_id}" valuecol="religion_id"
								displaycol="religion_name"
								filtered="true" filtercol="status" filtervalue="A"/>
								<c:if test="${not empty regPref.religionRequired && (regPref.religionRequired eq 'Y' ||
										(screenId eq 'ip_registration' && regPref.religionRequired eq 'I') ||
										(screenId eq 'out_pat_reg' && regPref.religionRequired eq 'O'))}">
									<span class="star">*</span>
								</c:if>
								</td>
								
								<td class="formlabel"><insta:ltext key="ui.label.master.race"/>:</td>
								<td>
								<insta:selectdb name="race_id" table="race_master"
								dummyvalue="---Select---" dummyvalueId="" value="${race_id}" valuecol="race_id"
								displaycol="race_name"
								filtered="true" filtercol="status" filtervalue="A"/>
								</td>
							</tr>

								<%-- Patient custom lists --%>
								<c:set var="maincolumns" value="0"/>
								<c:forEach var="num" begin="1" end="9">
								<c:set var="nameField" value="custom_list${num}_name"/>
								<c:set var="showField" value="custom_list${num}_show"/>
								<c:set var="validateField" value="custom_list${num}_validate"/>
								<c:if test="${not empty regPref[nameField] && not empty regPref[showField] && regPref[showField] eq 'M'}">
									<td class="formlabel">${regPref[nameField]}:</td>
									<td>
										<insta:selectdb name="custom_list${num}_value" id="custom_list${num}_value"  value="" table="custom_list${num}_master"
											style="width:140px;" valuecol="custom_value" displaycol="custom_value" usecache="true"
											dummyvalue="${dummyvalue}" size="1" orderby="custom_value"/>
										<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
															(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
															(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
											<span class="star">*</span>
										</c:if>
									</td>
									<c:set var="maincolumns" value="${maincolumns+1}"/>
									<c:if test="${(maincolumns % 3) == 0}"></tr><tr>
									</c:if>
								</c:if>
								</c:forEach>

								<%-- Patient custom fields --%>
								<c:forEach var="num" begin="1" end="19">
									<c:set var="labelField" value="custom_field${num}_label"/>
									<c:set var="showField" value="custom_field${num}_show"/>
									<c:set var="validateField" value="custom_field${num}_validate"/>
									<c:if test="${not empty regPref[labelField] && not empty regPref[showField] && regPref[showField] eq 'M'}">
										<td class="formlabel">${regPref[labelField]}:</td>
										<td>
											<c:choose>
												<c:when test="${num le 13}">
													<input type="text" class="field" name="custom_field${num}">
												</c:when>
												<c:when test="${num le 16}">
													<insta:datewidget name="custom_field${num}"  title="custom_field${num}"/>
												</c:when>
												<c:otherwise>
													<input type="text" class="number" name="custom_field${num}"/>
												</c:otherwise>
											</c:choose>
											<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
															(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
															(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
											<span class="star">*</span>
											</c:if>
										</td>
										<c:set var="maincolumns" value="${maincolumns+1}"/>
										<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
									</c:if>
								</c:forEach>

							<c:if test="${not empty regPref.passport_no && not empty regPref.passport_no_show && regPref.passport_no_show eq 'M'}">
								<td class="formlabel">${ifn:cleanHtml(regPref.passport_no)}:</td>
								<td>
									<input type="text" class="field" name="passport_no" >
										<span id="passport_no_star" class="star">*</span>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.passport_validity && not empty regPref.passport_validity_show && regPref.passport_validity_show eq 'M'}">
								<td class="formlabel">${ifn:cleanHtml(regPref.passport_validity)}:</td>
								<td>
									<insta:datewidget name="passport_validity" id="passport_validity"
												btnPos="left" title="Passport validity"/>
										<span id="passport_validity_star" class="star">*</span>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.visa_validity && not empty regPref.visa_validity_show && regPref.visa_validity_show eq 'M'}">
								<td class="formlabel">${ifn:cleanHtml(regPref.visa_validity)}:</td>
								<td>
									<insta:datewidget name="visa_validity" id="visa_validity"
												btnPos="left" title="Visa validity"/>
										<span id="visa_validity_star" class="star">*</span>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.passport_issue_country && not empty regPref.passport_issue_country_show && regPref.passport_issue_country_show eq 'M'}">
								<td class="formlabel">${ifn:cleanHtml(regPref.passport_issue_country)}:</td>
								<td>
									<insta:selectdb name="passport_issue_country" id="passport_issue_country" table="country_master"
										class="field" style="width:140px;" dummyvalue="${dummyvalue}" filtercol="status,nationality" filtervalue="A,f"
										size="1" valuecol="country_id" displaycol="country_name" usecache="true"/>
										<span id="passport_issue_country_star" class="star">*</span>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.government_identifier_type_label}">
								<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_type_label)}</td>
								<td>
									<select name="identifier_id" id="identifier_id" class="dropdown" style="width:137px;" onchange="setGovtPattern();">
											<option value="">--Select--</option>
											<c:forEach items="${govtIdentifierTypes}" var="govtIdTypes">
												<option value="${govtIdTypes.map.identifier_id}" ${govtIdTypes.map.default_option eq 'Y' ? 'selected' : ''}>
													${govtIdTypes.map.remarks}</option>
											</c:forEach>
									</select>
									<c:if test="${patientIdentification=='G'}">
									<span class="star">*</span>
									</c:if>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>
							<c:if test="${not empty regPref.government_identifier_label}">
								<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_label)}</td>
								<td>
									<input type="text" name="government_identifier" disabled id="government_identifier" maxlength="50" onchange="onChangeOfGovtIdType();"/>
										<span id="govtidstar" class="star">*</span>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.government_identifier_type_label}">
								<td class="formlabel">Other Identification Document Types:</td>
								<td>
									<select name="other_identification_doc_id" id="other_identification_doc_id" class="dropdown" style="width:137px;" onchange="onChangeOfOtherId();">
											<option value="">--Select--</option>
											<c:forEach items="${otherIdentifierTypes}" var="otherIdTypes">
												<option value="${otherIdTypes.map.other_identification_doc_id}">
													${otherIdTypes.map.other_identification_doc_name}</option>
											</c:forEach>
									</select>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>
							<c:if test="${not empty regPref.government_identifier_label}">
								<td class="formlabel"id="other_identification_doc_value_label" >Other Identifier Document Value:</td>
								<td>
									<input type="text" name="other_identification_doc_value" disabled id="other_identification_doc_value" maxlength="50" />
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.family_id && not empty regPref.family_id_show && regPref.family_id_show eq 'M'}">
								<td class="formlabel">${ifn:cleanHtml(regPref.family_id)}:</td>
								<td>
									<input type="text" name="family_id" id="family_id">
									<c:if test="${not empty regPref.family_id_validate &&
													(regPref.family_id_validate eq 'A' ||
														(screenId eq 'ip_registration' && regPref.family_id_validate eq 'I') ||
														(screenId eq'out_pat_reg' && regPref.family_id_validate eq 'O'))}">
										<span class="star">*</span>
									</c:if>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<c:if test="${not empty regPref.nationality && not empty regPref.nationality_show && regPref.nationality_show eq 'M'}">
								<td class="formlabel">${ifn:cleanHtml(regPref.nationality)}:</td>
								<td>
									<insta:selectdb name="nationality_id" id="nationality_id" table="country_master"
										class="field" style="width:140px;" dummyvalue="${dummyvalue}"
										size="1" valuecol="country_id" displaycol="country_name" usecache="true"/>

									<c:if test="${not empty regPref.nationality_validate &&
													(regPref.nationality_validate eq 'A' ||
														(screenId eq 'ip_registration' && regPref.nationality_validate eq 'I') ||
														(screenId eq'out_pat_reg' && regPref.nationality_validate eq 'O'))}">
										<span class="star">*</span>
									</c:if>
								</td>
								<c:set var="maincolumns" value="${maincolumns+1}"/>
								<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
							</c:if>

							<tr>
								<td class="formlabel"><insta:ltext key="registration.patient.additionalinformation.commonlabel.remarks"/>:</td>
								<td>
									<input type="text" name="remarks" id="remarks" maxlength="50">
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.additionalinformation.commonlabel.emailId"/>:</td>
								<td>
									<input type="text" name="email_id" id="email_id">
									<span class="patient_email_star">*</span>
									
								</td>
								<td class="formlabel"><insta:ltext key="ui.label.preferred.language"/>:</td>
								<td>
									<select id="preferredLanguage" name="preferredLanguage" class="dropdown" style="width:137px;">
									<c:forEach items="${preferredLanguages}" var="lang">
										<option value="${lang.lang_code}" ${lang.lang_code.equals(defaultPreferredLanguage)?"selected":""}>${lang.language}</option>
									</c:forEach>
									</select>
								</td>
								<td class="formlabel">
									<insta:ltext key="registration.patient.label.p"/><insta:ltext key="registration.patient.label.m"/><u><b><insta:ltext key="registration.patient.label.o"/></u></b><insta:ltext key="registration.patient.label.re"/>:</td>
								<td>
									<input type="button" name="btnCustomFields" id="btnCustomFields" title="Custom Fields"
										onclick="return showCustomDialog(this);" value="..."
										accesskey="O" class="button"/>
								</td>
							</tr>
							<tr>
								<c:if test="${preferences.modulesActivatedMap['mod_mobile'] eq 'Y' && screenId eq 'ip_registration'}">
								<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.mobileaccess"/>:</td>
								<td >
									<span style="padding: 0 0px 0px 0;">
										<insta:radio radioText="${mobilepatientaccess}" radioIds="mobilePatYes,mobilePatNo" radioValues="Y,N"
											name="mobilePatAccess" value="${mobilePatAccess}"/>
									</span>
								</td>
								</c:if>
								<c:if test="${preferences.modulesActivatedMap.mod_messaging eq 'Y'}">
									<td class="formlabel"><insta:ltext key="ui.label.mode.of.communication"/>:</td>
									<td>
									<input type="checkbox" id="modeOfCommSms" name="modeOfCommSms" checked/><insta:ltext key="ui.label.sms.allcaps"/>
									<input type="checkbox" id="modeOfCommEmail" name="modeOfCommEmail" checked/><insta:ltext key="ui.label.email.allcaps"/>
									</td>
								</c:if>
							</tr>
						</table>
						</fieldset>
					</div>
				</td>
			</tr>
		</table>

		<c:if test="${not empty hasVisitAddlnFields && hasVisitAddlnFields}">
		<table class="formtable" >
			<tr>
				<td>
					<div id="VisitCollapsiblePanel1" class="CollapsiblePanel">
						<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
							<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">
								<insta:ltext key="registration.patient.label.additional"/>&nbsp;<insta:ltext key="registration.patient.label.visit"/>&nbsp;<insta:ltext key="registration.patient.label.i"/><b><u><insta:ltext key="registration.patient.label.n"/></u></b><insta:ltext key="registration.patient.label.formation"/></div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
								<c:choose>
									<c:when test="${visitAddnlOpen}"> <img src="${cpath}/images/up.png" /> </c:when>
									<c:otherwise> <img src="${cpath}/images/down.png" /> </c:otherwise>
								</c:choose>
							</div>
							<div class="clrboth"></div>
						</div>
						<fieldset class="fieldSetBorder">
						<table class="formtable">
							<c:set var="visitcolumns" value="0"/>
							<tr>
								<%-- Visit custom lists --%>
								<c:forEach var="num" begin="1" end="2">
								<c:set var="nameField" value="visit_custom_list${num}_name"/>
								<c:set var="validateField" value="visit_custom_list${num}_validate"/>
								<c:set var="showField" value="visit_custom_list${num}_show"/>
								<c:if test="${not empty regPref[nameField] && not empty regPref[showField] && regPref[showField] eq 'M'}">
									<td class="formlabel">${regPref[nameField]}:</td>
									<td>
										<insta:selectdb name="visit_custom_list${num}" id="visit_custom_list${num}"  value="" table="custom_visit_list${num}_master"
											style="width:140px;" valuecol="custom_value" displaycol="custom_value" usecache="true"
											dummyvalue="${dummyvalue}" size="1" orderby="custom_value"/>
										<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
												(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
												(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
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
									<c:set var="showField" value="visit_custom_field${num}_show"/>
									<c:if test="${not empty regPref[labelField] && not empty regPref[showField] && regPref[showField] eq 'M'}">
										<td class="formlabel">${regPref[labelField]}:</td>
											<td>
												<c:choose>
													<c:when test="${num le 3}">
														<input type="text" class="field" name="visit_custom_field${num}">
													</c:when>
													<c:when test="${num le 6}">
														<insta:datewidget name="visit_custom_field${num}"  title="visit_custom_field${num}"/>
													</c:when>
													<c:otherwise>
														<input type="text" class="number" name="visit_custom_field${num}" onkeypress=""/>
													</c:otherwise>
												</c:choose>
												<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
																	(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
																	(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
												<span class="star">*</span>
												</c:if>
											</td>
										<c:set var="visitcolumns" value="${visitcolumns+1}"/>
										<c:if test="${(visitcolumns % 3) == 0}"></tr><tr></c:if>
									</c:if>
								</c:forEach>
							</tr>
							<tr id="displayVisitCustomBtn" style="display:none">
								<td class="formlabel">
									<b><u><insta:ltext key="registration.patient.label.v"/></u></b><insta:ltext key="registration.patient.label.more"/>:
								</td>
								<td>
									<input type="button" name="btnVisitCustomFields" id="btnVisitCustomFields" title='<insta:ltext key="registration.patient.label.visitcustomfields"/>'
										onclick="return showVisitCustomDialog(this);" value="..."
										accesskey="V" class="button"/>
								</td>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
								<td>&nbsp;</td>
							</tr>
						</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
	</c:if>


	<div id="customFieldsDialog" style="display: none;">
		<div class="hd"><insta:ltext key="registration.patient.customfieldsdialog.header"/></div>
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="registration.patient.customfieldsdialog.title"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.customfieldsdialog.patientPhoto"/>:</td>
						<td colspan="2">
	 						<input type="file" name="patPhoto" id="patPhoto" accept="<insta:ltext key="upload.accept.image"/>">
							<div id="viewPhoto" style="display: none">
								<a href="javascript:void(0)" rel="lightbox" title='<insta:ltext key="registration.patient.label.patientphoto"/>'><insta:ltext key="registration.patient.customfieldsdialog.viewpatientPhoto"/></a>
							</div>
						</td>
						<c:if test="${regPref.copy_paste_option eq 'Y'}">
							<td class="formlabel"><insta:ltext key="registration.patient.pasteImage.imageLabel"/>:</td>

							<td >
							<div style="border:1px dashed grey;height: 45px;width: 65px;font-size: 11px;" id="pastedPhoto">
								<div style="padding:1px;text-align:center"><insta:ltext key="js.common.copy.and.press.paste"/></div>
							</div>
							</td>

						</c:if>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.customfieldsdialog.nextOfKinAddress"/>:</td>
						<td>
							<input type="text"  name="patient_careof_address" id="patient_careof_address" maxlength="250" />
							<c:if test="${is_patient_care_oftext_mandate == 'Y'}">
										<span class="star">*</span>
							</c:if>
						</td>
						<c:if test="${regPref.oldRegNumField != '' && regPref.oldRegNumField != null}">
							<td class="formlabel" ><label for="previoushosid">${ifn:cleanHtml(regPref.oldRegNumField)}:</label></td>
							<td style="width: 196px; padding-top: 15px">
								<input type="text" name="oldmrno" id="oldmrno" size="10" maxlength="20" class="field"
									onkeyup="upperCase(oldmrno)" onchange="return checkUniqueOldMrno();">
								<img id="previoushospimg" name="previoushospimg" title="Verifying Old Mr No."
									src="<%=request.getContextPath()%>/images/ajax-loader.gif"  width="16"
									height="16" style="vertical-align:top;visibility:hidden;" />
							</td>
						</c:if>
						<td>
							<input type="checkbox" name="vip_check" id="vip_check" onclick="enableVipStatus()"/>
							<label for="vip_check"><insta:ltext key="registration.patient.customfieldsdialog.vipPatient"/>:</label>
							<input type="hidden" name="vip_status" id="vip_status" value="N">
						</td>
					</tr>

					<c:set var="columns" value="0"/>
					<tr>
						<%-- Patient custom lists --%>
						<c:forEach var="num" begin="1" end="9">
						<c:set var="nameField" value="custom_list${num}_name"/>
						<c:set var="showField" value="custom_list${num}_show"/>
						<c:set var="validateField" value="custom_list${num}_validate"/>
						<c:if test="${not empty regPref[nameField] && not empty regPref[showField] && regPref[showField] eq 'D'}">
							<td class="formlabel">${regPref[nameField]}:</td>
							<td>
								<insta:selectdb name="custom_list${num}_value" id="custom_list${num}_value"  value="" table="custom_list${num}_master"
									style="width:140px;" valuecol="custom_value" displaycol="custom_value" usecache="true"
									dummyvalue="${dummyvalue}" size="1" orderby="custom_value"/>
								<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
												(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
												(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
								<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>
						</c:forEach>

						<%-- Patient custom fields --%>
						<c:forEach var="num" begin="1" end="19">
							<c:set var="labelField" value="custom_field${num}_label"/>
							<c:set var="showField" value="custom_field${num}_show"/>
							<c:set var="validateField" value="custom_field${num}_validate"/>
							<c:if test="${not empty regPref[labelField] && not empty regPref[showField] && regPref[showField] eq 'D'}">
								<td class="formlabel">${regPref[labelField]}:</td>
								<td>
									<c:choose>
										<c:when test="${num le 13}">
											<input type="text" class="field" name="custom_field${num}">
										</c:when>
										<c:when test="${num le 16}">
											<insta:datewidget name="custom_field${num}"  title="custom_field${num}"/>
										</c:when>
										<c:otherwise>
											<input type="text" class="number" name="custom_field${num}"/>
										</c:otherwise>
									</c:choose>
									<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
													(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
													(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
									<span class="star">*</span>
									</c:if>
								</td>
								<c:set var="columns" value="${columns+1}"/>
								<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
							</c:if>
						</c:forEach>

						<c:if test="${not empty regPref.passport_no && not empty regPref.passport_no_show && regPref.passport_no_show eq 'D'}">
							<td class="formlabel">${ifn:cleanHtml(regPref.passport_no)}:</td>
							<td>
								<input type="text" class="field" name="passport_no" >
								<c:if test="${not empty regPref.passport_no_validate &&
												(regPref.passport_no_validate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.passport_no_validate eq 'I') ||
													(screenId eq'out_pat_reg' && regPref.passport_no_validate eq 'O'))}">
									<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>

						<c:if test="${not empty regPref.passport_validity && not empty regPref.passport_validity_show && regPref.passport_validity_show eq 'D'}">
							<td class="formlabel">${ifn:cleanHtml(regPref.passport_validity)}:</td>
							<td>
								<insta:datewidget name="passport_validity" id="passport_validity"
											btnPos="left" title="Passport validity"/>
								<c:if test="${not empty regPref.passport_validity_validate &&
												(regPref.passport_validity_validate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.passport_validity_validate eq 'I') ||
													(screenId eq'out_pat_reg' && regPref.passport_validity_validate eq 'O'))}">
									<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>

						<c:if test="${not empty regPref.visa_validity && not empty regPref.visa_validity_show && regPref.visa_validity_show eq 'D'}">
							<td class="formlabel">${ifn:cleanHtml(regPref.visa_validity)}:</td>
							<td>
								<insta:datewidget name="visa_validity" id="visa_validity"
											btnPos="left" title="Visa validity"/>
								<c:if test="${not empty regPref.visa_validity_validate &&
												(regPref.visa_validity_validate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.visa_validity_validate eq 'I') ||
													(screenId eq'out_pat_reg' && regPref.visa_validity_validate eq 'O'))}">
									<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>

						<c:if test="${not empty regPref.passport_issue_country && not empty regPref.passport_issue_country_show && regPref.passport_issue_country_show eq 'D'}">
							<td class="formlabel">${ifn:cleanHtml(regPref.passport_issue_country)}:</td>
							<td>
								<insta:selectdb name="passport_issue_country" id="passport_issue_country" table="country_master"
									class="field" style="width:140px;" dummyvalue="${dummyvalue}" filtercol="status,nationality" filtervalue="A,f"
									size="1" valuecol="country_id" displaycol="country_name" usecache="true"/>
								<c:if test="${not empty regPref.passport_issue_country_validate &&
												(regPref.passport_issue_country_validate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.passport_issue_country_validate eq 'I') ||
													(screenId eq'out_pat_reg' && regPref.passport_issue_country_validate eq 'O'))}">
									<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>

						<c:if test="${not empty regPref.family_id && not empty regPref.family_id_show && regPref.family_id_show eq 'D'}">
							<td class="formlabel">${ifn:cleanHtml(regPref.family_id)}:</td>
							<td>
								<input type="text" name="family_id" id="family_id">
								<c:if test="${not empty regPref.family_id_validate &&
												(regPref.family_id_validate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.family_id_validate eq 'I') ||
													(screenId eq'out_pat_reg' && regPref.family_id_validate eq 'O'))}">
									<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>

						<c:if test="${not empty regPref.nationality && not empty regPref.nationality_show && regPref.nationality_show eq 'D'}">
							<td class="formlabel">${ifn:cleanHtml(regPref.nationality)}:</td>
							<td>
								<insta:selectdb name="nationality_id" id="nationality_id" table="country_master"
										class="field" style="width:140px;" dummyvalue="${dummyvalue}"
										size="1" valuecol="country_id" displaycol="country_name" usecache="true"/>

								<c:if test="${not empty regPref.nationality_validate &&
												(regPref.nationality_validate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.nationality_validate eq 'I') ||
													(screenId eq'out_pat_reg' && regPref.nationality_validate eq 'O'))}">
									<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>

					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td><button type="button" id="customFieldsOkBtn" value="OK" ><insta:ltext key="registration.patient.label.o"/><b><u><insta:ltext key="registration.patient.label.k"/></u></b></button></td>
				</tr>
			</table>
		</div>
	</div>

	<div id="visitCustomFieldsDialog" style="display: none;">
		<div class="hd"><insta:ltext key="registration.patient.visitcustomfieldsdialog.header"/></div>
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="registration.patient.visitcustomfieldsdialog.title"/></legend>
				<table class="formtable">
					<c:set var="columns" value="0"/>
					<tr>
						<%-- Patient Visit custom lists --%>
						<c:forEach var="num" begin="1" end="2">
						<c:set var="nameField" value="visit_custom_list${num}_name"/>
						<c:set var="showField" value="visit_custom_list${num}_show"/>
						<c:set var="validateField" value="visit_custom_list${num}_validate"/>
						<c:if test="${not empty regPref[nameField] && not empty regPref[showField] && regPref[showField] eq 'D'}">
							<td class="formlabel">${regPref[nameField]}:</td>
							<td>
								<insta:selectdb name="visit_custom_list${num}" id="visit_custom_list${num}"  value="" table="custom_visit_list${num}_master"
									style="width:140px;" valuecol="custom_value" displaycol="custom_value" usecache="true"
									dummyvalue="-- Select --" size="1" orderby="custom_value"/>
								<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
												(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
												(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
								<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="columns" value="${columns+1}"/>
							<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
						</c:if>
						</c:forEach>

						<%-- visit custom fields --%>
						<c:forEach var="num" begin="1" end="9">
							<c:set var="labelField" value="visit_custom_field${num}_name"/>
							<c:set var="showField" value="visit_custom_field${num}_show"/>
							<c:set var="validateField" value="visit_custom_field${num}_validate"/>
							<c:if test="${not empty regPref[labelField] && not empty regPref[showField] && regPref[showField] eq 'D'}">
								<td class="formlabel">${regPref[labelField]}:</td>
								<td>
									<c:choose>
										<c:when test="${num le 3}">
											<input type="text" class="field" name="visit_custom_field${num}">
										</c:when>
										<c:when test="${num le 6}">
											<insta:datewidget name="visit_custom_field${num}"  title="visit_custom_field${num}"/>
										</c:when>
										<c:otherwise>
											<input type="text" class="number" name="visit_custom_field${num}"/>
										</c:otherwise>
									</c:choose>
									<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
													(screenId eq 'ip_registration' && regPref[validateField] eq 'I') ||
													(screenId eq'out_pat_reg' && regPref[validateField] eq 'O'))}">
									<span class="star">*</span>
									</c:if>
								</td>
								<c:set var="columns" value="${columns+1}"/>
								<c:if test="${(columns % 2) == 0}"></tr><tr></c:if>
							</c:if>
						</c:forEach>
					</tr>
				</table>
			</fieldset>

			<table>
				<tr>
					<td><button type="button" id="visitCustomFieldsOkBtn" value="OK" ><insta:ltext key="registration.patient.label.o"/><b><u><insta:ltext key="registration.patient.label.k"/></u></b></button></td>
				</tr>
			</table>
		</div>
	</div>

		<table class="formtable" >
			<tr>
				<td>
					<fieldset class="fieldSetBorder" style="width: 935px; box-sizing: content-box">
					<legend class="fieldSetLabel"><insta:ltext key="registration.patient.sponsor.title"/></legend>
						<table class="formtable">
							<tr>

								<td class="formlabel"><insta:ltext key="registration.patient.primary.sponsor"/>:</td>
								<td>
								<input type="checkbox" name="primary_sponsor_wrapper" id="primary_sponsor_wrapper" onchange="onChangePrimarySponsor()">
								<input type="hidden" name="primary_sponsor" id="primary_sponsor" value="">
								</td>
								<td class="formlabel"><insta:ltext key="registration.patient.secondary.sponsor"/>:</td>
								<td>
								<input type="checkbox" name="secondary_sponsor_wrapper" id="secondary_sponsor_wrapper" onchange="onChangeSecondarySponsor()" disabled>
								<input type="hidden" name="secondary_sponsor" id="secondary_sponsor" value="">
								</td>
							
								<td></td>
								<td></td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>

			<insta:insurance-panel sponsorIndex="P" />
			<insta:insurance-panel sponsorIndex="S" />

			<tr>
				<td>
					<fieldset class="fieldSetBorder" style="width: 935px; box-sizing: content-box">
					<legend class="fieldSetLabel"><insta:ltext key="registration.patient.payment.title"/></legend>
						<table class="formtable">
							<tr>
								<c:if test="${screenId ne 'out_pat_reg'}">
									<td class="formlabel"><insta:ltext key="registration.patient.payment.billType"/>:</td>
									<td>
										<select name="bill_type" id="bill_type" class="dropdown" onchange="onChangeBillType()">
											<option value="P"><insta:ltext key="registration.patient.payment.billtypeselect.billNow"/></option>
											<c:if test="${actionRightsMap.allow_credit_bill_later eq 'A' || roleId == 1 || roleId ==2}">
												<option value="C"><insta:ltext key="registration.patient.payment.billtypeselect.billLater"/></option>
											</c:if>
										</select>
									</td>
								</c:if>
								<td class="formlabel">
									<label for="patientType1"><insta:ltext key="registration.patient.payment.ratePlan"/>:</label>
								</td>
								<td>
									<select id="organization" name="organization" size="1" class="dropdown"
										   onchange="ratePlanChange()">
									</select>
						<span class="star">*</span>
								</td>
								<c:if test="${screenId eq 'ip_registration' }">
									<td class="formlabel">
										<label for="creditlimit"><insta:ltext key="ui.label.ippatient.creditlimit"/>:</label>
									</td>
									<td>
										<input type="text" class="field" name="ipcreditlimit" id="ipcreditlimit" onkeypress="return enterFloatNumOnly(event);"
													${actionRightsMap.allow_ip_patient_credit_limit_change eq 'A' || roleId == 1 || roleId ==2 ? '' : 'readonly'}>
									</td>
									<td style="padding-left: 0px;">
										<img class="imgHelpText" id="credit_limit_help"
										title="Available Credit Limit = Credit Limit + Available Deposits - Patient Dues. Credit Limit : 0.00, Available Deposits : 0.00, Patient Dues : 0.00"
					 					src="${cpath}/images/help.png"/>
									</td>	
								</c:if>
								<td class="formlabel">
									<label id ="discPlanLabel" style="display:none"><insta:ltext key="registration.patient.payment.discount_plan"/>:</label>
								</td> 
								<td class="forminfo">
									<label id="insurance_discount_plan_lbl" style="display:none" ></label>
									<input type="hidden" id="insurance_discount_plan" name="insurance_discount_plan"/>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
		</table>


		<table class="formtable" >
			<tr>
				<td>
					<c:if test="${screenId != 'out_pat_reg' || regPref.showReferralDoctorFilter != 'Y'}">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">
							<c:if test="${screenId eq 'out_pat_reg'}"><insta:ltext key="registration.patient.visitinformation.header"/></c:if>
							<c:if test="${screenId eq 'ip_registration'}"><insta:ltext key="registration.patient.commonlabel.admissioninformation"/></c:if>
							</legend>
							<table class="formtable">
									<c:if test="${screenId ne 'out_pat_reg'}">
									<tr>
										<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.department"/>:</td>
										<td>
											<select id="dept_name" name="dept_name" onChange="onDeptChange()" class="dropdown">
												<option selected value=""><insta:ltext key="common.selectbox.defaultText"/></option>
												<c:forEach var="maketype" items="${arrdeptDetails}">
													<option value='${maketype.DEPT_ID}'>
														${maketype.DEPT_NAME}</option>
												</c:forEach>
											</select>
											<span class="star">*</span>
										</td>
										<c:if test="${regPref.hospUsesUnits != null && regPref.hospUsesUnits == 'Y'
											&& regPref.deptUnitSetting != null}">
											<c:choose>
												<c:when test="${regPref.deptUnitSetting == 'M'}">
													<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.unit"/>:</td>
													<td>
														<select id="unit_id" name="unit_id"  class="dropdown" >
															<option selected value=""><insta:ltext key="common.selectbox.defaultText"/></option>
														</select>
													</td>
												</c:when>
												<c:otherwise>
													<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.unit"/>:</td>
													<td>
														<select id="unit_id" name="unit_id"  class="dropdown">
															<option selected value=""><insta:ltext key="common.selectbox.defaultText"/></option>
														</select>
													</td>
												</c:otherwise>
											</c:choose>
										</c:if>
										<c:if test="${regPref.hospUsesUnits eq 'N' }">
											<td></td>
											<td></td>
										</c:if>
										<c:choose>
												<c:when test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'}">
												<td class="formlabel"> <insta:ltext key="registration.patient.commonlabel.transferredfrom"/>: </td>
												<td>
													<insta:selectdb name="transfer_source" table="transfer_hospitals" valuecol="transfer_hospital_id"
													displaycol="transfer_hospital_name" dummyvalue="${dummyvalue}"  dummyvalueId=""
													orderby="transfer_hospital_name"/>
												</td>
											</c:when>
											<c:otherwise>
												<td> </td>
												<td> </td>
											</c:otherwise>
										</c:choose>
									</tr>
									</c:if>

									<c:if test="${screenId eq 'ip_registration'}">
										<tr>
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.admttingDoctor"/>:</td>
											
											<td valign="top">
												<div id="doctor_wrapper" class="autoComplete">
					    							<input type="text" name="doctor_name" id="doctor_name" value="" style="width:180px;"/>
						    						<div id="doc_dept_dropdown" class="scrolForContainer" style="width:180px">
						    						</div>
						    					</div>
						    					<span class="star" style="float:right">*</span>
						    					<input type="hidden" name="opdocdescription" id="opdocdescription" value=""/>
												<input type="hidden" name="doctor" id="doctor" value=""/>
											</td>
											
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.bedType"/>:</td>
											<td>
												<select name="bed_type" id="bed_type" onchange="onBedTypeChange()" class="dropdown">
													<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
												</select>

												<span class="star">*</span>
											</td>
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.ward"/>:</td>
											<td>
												<select name="ward_id" id="ward_id" class="dropdown" onchange="onWardChange()">
													<option value="">${dummyvalue}</option>
													<c:forEach items="${wards}" var="ward">
														<option value="${ward.map.ward_no }">${ward.map.ward_name }</option>
													</c:forEach>
												</select>
												<span class="star">*</span>
											</td>
										</tr>
										<c:if test="${ip_prefs.map.allocate_bed_at_reg == 'Y'}">
											<tr>
												<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.bedName"/>:</td>
												<td>
													<select name="bed_id" id="bed_id" class="dropdown">
														<option value="0"><insta:ltext key="common.selectbox.defaultText"/></option>
													</select>
													<span class="star">*</span>
												</td>
												<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.dutyDoctor"/>:</td>
												<td>
													<select name="duty_doctor_id" style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;width: 140px">
														<option value="">...Select...</option>
														<c:forEach items="${dutydoclist}" var="doc" >
															<option value="${doc.map.doctor_id}">${doc.map.doctor_name}</option>
														</c:forEach>
													</select>
													<c:if test="${ip_prefs.map.duty_doctor_selection != 'N'}">
														<span class="star" id="duty_doc_star" style="visibility:visible;">*</span>
													</c:if>
												</td>
												<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.isDayCare"/>:</td>
												<td>
													<input type="checkbox" name="daycare_status" id="daycare_status" value="Y"
														style="margin: 0px; padding: 0px" onchange="populateBedCharge();"/>
													<c:set var="nextDay" value="${ifn:nextDate(currentDate,1)}"/>
													<fmt:formatDate var="curDateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
													<fmt:formatDate var="curTimeVal" value="${currentDate}" pattern="HH:mm"/>
													<fmt:formatDate var="nextDateVal" value="${nextDay}" pattern="dd-MM-yyyy"/>

													<input type="hidden" name="start_date_dt" value="${curDateVal}" />
													<input type="hidden" name="start_date_tm" value="${curTimeVal}" />
													<input type="hidden" name="end_date_dt" value="${nextDateVal}" />
													<input type="hidden" name="end_date_tm" value="${curTimeVal}" />
												</td>
											</tr>
										</c:if>
										<tr>
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.bedsAvailable"/>:</td>
											<td>
												<label class="forminfo" id="availabelBeds" ></label>
												<input type="hidden" id="ipfreebeds" name="ipfreebeds" value="0" />
											</td>
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.charge"/>:</td>
											<td style="width: 250px">
												<label class="forminfo" id="bedAdvance" ></label>
												<input type="hidden" id="ipbedavance" name="ipbedavance" value="0" />
											</td>
										<%--<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.initialAmount"/>:</td>
											<td>
												<label id="initialAmount" class="forminfo"></label>
											</td>
											--%>
										</tr>
									</c:if>

									<tr>
										<c:if test="${regPref.showReferralDoctorFilter != 'Y'}">
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.referredBy"/>:</td>
											<td>
												<table style="width:190px;">
												<tr>
												<td style="width:1800px;padding: 0px 10px 0px 0px;">
												<div id="referalAutoComplete" class="autoComplete">
													<input type="text" name="referaldoctorName" id="referaldoctorName"
														class="field" style="width:180px;" maxlength="100" />
													<div id="referalNameContainer" class="scrolForContainer" style="width:240px;"></div>
												</div>
												</td>
												<td>
												<c:if test="${not empty regPref.referredbyValidate &&
														(regPref.referredbyValidate eq 'A' ||
															(screenId eq 'ip_registration' && regPref.referredbyValidate eq 'I') ||
															(screenId eq 'out_pat_reg' && regPref.referredbyValidate eq 'P'))}">
												<span class="star">*</span>
												</c:if>
												<input type="hidden" name="referred_by" id="referred_by"/>
												</td>
												</tr>
												</table>
											</td>
										</c:if>
										<c:if test="${screenId ne 'out_pat_reg'}">
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.complaint"/>:</td>
											<td colspan="1" >
												<input type="text" name="ailment" id="ailment"/>
												<c:if test="${not empty regPref.complaintValidate &&
															(regPref.complaintValidate eq 'A' ||
																(screenId eq 'ip_registration' && regPref.complaintValidate eq 'I') ||
																(screenId eq 'out_pat_reg' && regPref.complaintValidate eq 'O'))}">
													<span class="star">*</span>
												</c:if>
											</td>
										</c:if>
											<td class="formlabel" name="visitClassification"><insta:ltext key="ui.label.visit.classification"/>:</td>
												<td style="width: 250px">
														<select name="visitClassification" id="visitClassification" class="dropdown">
														<option value="">${dummyvalue}</option>
														<c:forEach items="${visitClassifications}" var="vclass">
															<option value="${vclass.key }">${vclass.value }</option>
														</c:forEach>
												</td>
										<c:if test="${regPref.showReferralDoctorFilter != 'Y' and screenId eq 'out_pat_reg'}">
											<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.clinicianId"/>:</td>
											<td>
												<label class="forminfo" id="clinician_label"></label>
												<input type="hidden" name="clinician_id" id="clinician_id" >
											</td>
											<td></td>
											<td></td>
										</c:if>
									</tr>
							</table>
						</fieldset>
						</c:if>
						<c:if test="${regPref.showReferralDoctorFilter == 'Y'}">
						<fieldset>
							<legend class="fieldSetLabel"><insta:ltext key="ui.label.referral.information"/></legend>
							<table>
								<tr>
									<td class="formlabel"><insta:ltext key="ui.label.state"/>:</td>
								   	<td>
										<div id="autoreferralstate" class="autoComplete">
											<input name="referral_filter_state" id="referral_filter_state" type="text"
											value="" style="width:11.6em" maxlength="50" />
											<div id="referral_filter_state_dropdown" style="width:250px"></div>
											<input type="hidden" name="referral_filter_state_id" id="referral_filter_state_id" value=""/>
											<input type="hidden" name="referral_filter_country" id="referral_filter_country" value=""/>
											<input type="hidden" name="referral_filter_country_id" id="referral_filter_country_id" value=""/>
										</div>
									</td>
									<c:if test="${regPref.enableDistrict == 'Y'}">
										<td class="formlabel">
											<insta:ltext key="ui.label.district"/>:
										</td>
										<td>
											<div id="autoreferraldistrict" class="autoComplete">
												<input name="referral_filter_district" id="referral_filter_district" type="text"
												value="" style="width:11.6em" maxlength="50" />
												<div id="referral_filter_district_dropdown" style="width:250px"></div>
												<input type="hidden" name="referral_filter_district_id" id="referral_filter_district_id" value=""/>
											</div>
										</td>
									</c:if>
									<td class="formlabel"><insta:ltext key="${regPref.enableDistrict == 'Y' ? 'ui.label.city.subdistrict' : 'ui.label.city' }"/>:</td>
									<td>
										<div id="autoreferralcity" class="autoComplete">
											<input type="text" name="referral_filter_city" id="referral_filter_city"
												value="" />
											<div id="referral_filter_city_dropdown" style="width:250px"></div>
											<input type="hidden" name="referral_filter_city_id" id="referral_filter_city_id" value=""/>
										</div>
									</td>
									<td class="formlabel"><insta:ltext key="${regPref.enableDistrict == 'Y' ? 'ui.label.area.village' : 'ui.label.area' }"/>:</td>
									<td>
										<div id="autoreferralarea" class="autoComplete">
											<input name="referral_filter_area" id="referral_filter_area" type="text"
											value="" style="width:11.6em" maxlength="50" />
											<div id="referral_filter_area_dropdown" style="width:250px"></div>
											<input type="hidden" name="referral_filter_area_id" id="referral_filter_area_id" value=""/>
										</div>
									</td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.referredBy"/>:</td>
									<td>
										<table style="width:190px;">
										<tr>
										<td style="width:1800px;padding: 0px 10px 0px 0px;">
										<div id="referalAutoComplete" class="autoComplete">
											<input type="text" name="referaldoctorName" id="referaldoctorName"
												class="field" style="width:180px;" maxlength="100" />
											<div id="referalNameContainer" class="scrolForContainer" style="width:240px;"></div>
										</div>
										</td>
										<td>
										<c:if test="${not empty regPref.referredbyValidate &&
												(regPref.referredbyValidate eq 'A' ||
													(screenId eq 'ip_registration' && regPref.referredbyValidate eq 'I') ||
													(screenId eq 'out_pat_reg' && regPref.referredbyValidate eq 'P'))}">
										<span class="star">*</span>
										</c:if>
										<input type="hidden" name="referred_by" id="referred_by"/>
										</td>
										</tr>
										</table>
									</td>
									<c:if test="${screenId eq 'out_pat_reg'}">
										<td class="formlabel"><insta:ltext key="registration.patient.visitinformation.clinicianId"/>:</td>
										<td>
											<label class="forminfo" id="clinician_label"></label>
											<input type="hidden" name="clinician_id" id="clinician_id" >
										</td>
									</c:if>
									<td></td>
									<td></td>
								</tr>
							</table>
						</fieldset>
						</c:if>
					</td>
				</tr>
				<c:if test="${preferences.modulesActivatedMap['mod_mrd_icd'] eq 'Y' && screenId eq 'out_pat_reg'}">
					<tr>
						<td>
							<jsp:include page="/pages/outpatient/DiagnosisDetailsInclude.jsp">
								<jsp:param name="form_name" value="mainform"/>
								<jsp:param name="displayPrvsDiagnosisBtn" value="false"/>
							</jsp:include>
						</td>
					</tr>
				</c:if>
			</table>

			<c:if test="${preferences.modulesActivatedMap['mod_adv_ins'] eq 'Y'}">
				<c:if test="${not empty docs}">
				<table class="formtable" >
					<tr>
						<td>
							<input type="hidden" name="doc_type" value="SYS_RG">
							<fmt:formatDate var="cdate" value="${currentDate}" pattern="dd-MM-yyyy"/>
							<input type="hidden" name="doc_date" value="${cdate}">

							<fieldset class="fieldSetBorder">
								<legend class="fieldSetLabel"><insta:ltext key="registration.patient.uploaddocumentsdetails.header"/></legend>
								<table align="center"  width="100%" cellpadding="0" cellspacing="0" id="docTable">

									<!-- Upload other docs required according mandatory field in docs_upload table
										allowed values for mandatory field are None/All/IP/OP/Outside/Insured = N/A/I/O/S/P -->
									<c:set var="i" value="1"/>
									<c:forEach var="docs" items="${docs}">
										<c:set var="docName" value="${fn:toLowerCase(docs.map.doc_name)}"/>
										<c:if test="${docName ne 'insurance card'}">
										<tr>
											<td class="formlabel">${docs.map.doc_name}:</td>
											<td colspan="5">
												<input type="file" name="doc_content_bytea${i}" id="doc_content_bytea${i}" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.pdf"/>">
												<input type="hidden" name="doc_name" id="doc_name${i}" value="${docs.map.doc_name}">
												<input type="hidden" name="format" id="format${i}" value="${docs.map.doc_format}">
												<input type="hidden" name="mandatory" id="mandatory${i}" value="${docs.map.mandatory}">
											</td>
										</tr>
										<c:set var="i" value="${i+1}"/>
										</c:if>
									</c:forEach>
								</table>
							</fieldset>
						</td>
					</tr>
				</table>
				</c:if>
			</c:if>

	<table class="formtable" width="100%">
		<tr>
			<td>
				<c:set var="resetLink">
					<insta:ltext key="registration.patient.common.screenlink.reset"/>
				</c:set>
 	  	        	<insta:analytics tagType="button" type="button" name="registerBtn" id="registerBtn" class="button" accesskey="R"
 	  	        	 category="Registration" action="Register" label="${ ga_page_label }"
 	  	           	 clickevent="registervalidate()"><label><u><b><insta:ltext key="common.button.char.r"/></b></u><insta:ltext key="registration.patient.common.button.egister"/></label></insta:analytics>
 	  	        	<!-- <insta:accessbutton buttonkey="registration.patient.common.button.register" id="registerBtn" type="button" name="registerBtn"   onclick="registervalidate()"/> -->
 	  	        <c:if test="${screenId eq 'ip_registration'}">
 	  	        	<insta:screenlink screenId="ip_registration" extraParam="?_method=getdetails" label="${resetLink}"/>
 	  	        </c:if>
			</td>
		</tr>
	</table>

	<table id="innerDocVisitForPack"></table> <%-- required to store doctor visit details for pkgs --%>
	<table id="innerCondDocForPack"></table>

	<%-- MLC fields dialog --%>

	<div id="mlcFieldsDialog" style="display: none;">
	<div class="hd"><insta:ltext key="registration.patient.mlcdialog.header"/></div>
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="registration.patient.mlcdialog.title"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.mlcdialog.mlc.document"/>:</td>
					<td>
						<select name="mlc_template" onchange="onChangeMLCDoc()" class="dropdown">
							<option value=""><insta:ltext key="common.selectbox.defaultText"/></option>
							<c:forEach var="template" items="${mlc_templates}">
								<option value="${template.map.template_id},${template.map.format}"
									id="${template.map.template_name}">${template.map.template_name}</option>
							</c:forEach>
						</select>
					</td>
					<td class="formlabel"><insta:ltext key="registration.patient.mlcdialog.mlc.type"/>:</td>
					<td><input type="text" name="mlc_type"></td>
					<td class="formlabel"><insta:ltext key="registration.patient.mlcdialog.accident.place"/>:</td>
					<td><input type="text" name="accident_place"></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.mlcdialog.police.station"/>:</td>
					<td><input type="text" name="police_stn"></td>
					<td class="formlabel"><insta:ltext key="registration.patient.mlcdialog.mlc.remarks"/>:</td>
					<td><input type="text" name="mlc_remarks"></td>
					<td class="formlabel"><insta:ltext key="registration.patient.mlcdialog.certificate.status"/>:</td>
					<td><input type="text" name="certificate_status"></td>
				</tr>
			</table>
		</fieldset>
		<table>
			<tr>
				<td><button type="button" id="mlcFieldsOkBtn" value="OK" ><insta:ltext key="registration.patient.label.o"/><b><u><insta:ltext key="registration.patient.label.k"/></u></b></button></td>
			</tr>
		</table>
	</div>
	</div>

	<%-- Information Dialog  For principal diagnosis code in Outside patient screen --%>

	<div id="infoDialog" class="info-overlay" style="display:none">
		<div class="bd">
			<table width="100%">
				<tr>
					<td>
						<label id="infoDialogText" style="float: left"></label>
					</td>
					<td style="width: 12px;" valign="top">
						<a id="cancelInfoImg" href="javascript:void(0);">
							<img src="${cpath}/icons/std_cancel.png" style="width: 12px;" />
						</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="div-shadow" id="shadowDiv"></div>
	</div>

	<%-- PatientPhoto and Insurance card Dialog --%>

	<div id="showphotoViewDialog" style="display: none">
		<div class="bd">
			<img id="photoImgId" src="" width="330px" height="210"/>
		</div>
	</div>

	<div id="showPimarySponsorViewDialog" style="display: none">
		<div class="bd">
			<img id="primarySponsorImgId" src="" width="330px" height="210"/>
		</div>
	</div>

	<div id="showSecondarySponsorViewDialog" style="display: none">
		<div class="bd">
			<img id="secondarySponsorImgId" src="" width="330px" height="210"/>
		</div>
	</div>
	<div id="patientRegPlanDetailsDialog" style="display: none;visibility: hidden;" ondblclick="handlePatientRegPlanDetailsDialogCancel();">
	<div class="bd" id="bd1" style="padding-top: 0px;">
		<table class="formTable" align="center" id="pd_planDialogTable" style="width:480px;">
			<tr>
				<td>
					<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
					<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patient.registration.plan.summary"/></legend>
							<table class="formTable" align="center" style="width:480px;">
								<tr>
									<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patient.registration.plan.exclusions"/></legend>
											<p style="width:450px;" id="plan_exclusions"></p>
										 </fieldset>
									</td>
								</tr>
								<tr>
									<td>
									<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
										<legend class="fieldSetLabel"><insta:ltext key="patient.registration.plan.notes"/></legend>
										<p style="width:450px;" id="plan_notes"></p>
									</fieldset>
									</td>
								</tr>
							</table>
					 </fieldset>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="<insta:ltext key='patient.registration.button.close'/>" style="cursor:pointer;" onclick="handlePatientRegPlanDetailsDialogCancel();"/>
				</td>
			</tr>
		</table>
	</div>
</div>
<div id="patientMvDetailsDialog" style="display:none;visibility:hidden"">
	<div class="hd" id="patMvDialogHeader"><insta:ltext key='patient.registration.dialog.header.label.mvpackage.details'/></div>
		<div class="bd" id="mvDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key='patient.registration.label.mvpackage.details'/></legend>
				<table class="formTable" align="center" id="pd_mvDialogTable" style="width:480px;">
				</table>
			</fieldset>
			<div style="height:5px;">&nbsp;</div>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key='patient.registration.label.mvpackage.component.details'/></legend>
				<table class="detailList" cellspacing="0" cellpadding="0" id="packageComponentsDetails" border="0" width="100%" style="margin-top: 10px;">
					<tr>
						<th><insta:ltext key='patient.registration.label.item.name'/></th>
						<th><insta:ltext key='patient.registration.label.item.type'/></th>
						<th><insta:ltext key='patient.registration.label.total.qty'/></th>
						<th><insta:ltext key='patient.registration.label.available.qty'/></th>
					</tr>
				</table>
			</fieldset>
			<div style="height:6px;">&nbsp;</div>
	 		<div style="margin-top: 10px">
	 			<table>
	 				<tr>
	 					<td>
	 						<input type="button" id="mv_prev" name="mv_prev" value="<insta:ltext key='patient.registration.button.previous'/>" />
	 					</td>
	 					<td>&nbsp;</td>
	 					<td>
	 						<input type="button" id="mv_next" name="mv_next" value="<insta:ltext key='patient.registration.button.next'/>" />
	 					</td>
	 					<td>&nbsp;</td>
	 					<td>
	 						<input type="button" id="mv_cancel" name="mv_cancel" value="<insta:ltext key='patient.registration.button.close'/>" />
	 					</td>
	 				</tr>
	 			</table>
	 		</div>
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

<div id="SmartCardConflictDialog" style="display: none;visibility: hidden;">
	<div class="hd"><insta:ltext key="patient.registration.information.conflict.smartcard" /></div>
	<div class="bd" style="padding: 0px">
 	<table id="wrapperTable">
		<tr>
			<td style="width:300px;border-right: 1px solid;border-bottom: 1px solid;padding:10px 0px 0px 20px">
				<div style="width:100%;height:40px;text-align:center">Information from card</div>
				<div><img id="img_sc" style="width:72px;height:96px;margin-left:114px" src="" /></div>
				<div style="width:100px;float:left;height:30px">Id:</div><div id="nationalid_sc" style="width:200px;float:left;height:30px"></div>
				<div style="width:100px;float:left;height:30px">Name:</div><div id="patientName_sc" style="width:200px;float:left;height:30px"></div>
				<div style="width:100px;float:left;height:30px">Date of Birth:</div><div id="dob_sc" style="width:200px;float:left;height:30px"></div>
			</td>
			<td style="width:300px;border-bottom: 1px solid;padding-left: 15px;padding:10px 0px 0px 20px">
				<div style="width:100%;height:40px;text-align:center">Information from system</div>
				<div><img id="img_sys" style="width:72px;height:96px;margin-left:114px" src="" /></div>
				<div style="width:100px;float:left;height:30px">Id:</div><div id="nationalid_sys" style="width:200px;float:left;height:30px"></div>
				<div style="width:100px;float:left;height:30px">Name:</div><div id="patientName_sys" style="width:200px;float:left;height:30px"></div>
				<div style="width:100px;float:left;height:30px">Date of Birth:</div><div id="dob_sys" style="width:200px;float:left;height:30px"></div>
			</td>
		</tr>
 	</table>
	<div style="color:red;width:600px;padding:10px 10px 10px 10px">
		The details from the card does not match the details in the system. Do you want to Update the information from the card?
 	</div>
 	<div style="height:40px;padding:0px 10px 10px 10px">
	 	<div style="float:left;margin-top:10px">
	 		<input type="button" value="<insta:ltext key='patient.registration.button.update'/>" style="cursor:pointer;" onclick="updateOnConflict();" />
	 	</div>
	 	<div style="float:right;margin-top:10px">
	 		<input type="button" value="<insta:ltext key='patient.registration.button.noUpdate'/>" style="cursor:pointer;" onclick="cancelOnConflict();"/>
	 	</div>
 	</div>
</div>
</div>

<div id="patientDetailsFromSmartCardDialog" style="display: none;visibility: hidden;">
	<div class="hd" style="width:600px"><insta:ltext key="patient.registration.nationalid.card.details.smartcard" /></div>
	<div class="bd" style="width:200px; height:240px; float:left;">
		<div style="width:120px; height:120px; margin:auto; margin-top:10px; border:1px solid; border-color:#CFDAE4; border-radius:4px">
			<img id="patient-card-image" style="width:72px; height:96px; margin-left:24px; margin-top:12px" src="" />
		</div>
	</div>
	<div class="bd" style="width:381px; height:240px; float:left; border-width:0px 1px 0px 0px">
		<table id="pd_patDetailsFromSCDialogTableMain">
			  <tr>
			  <td>
				<fieldset class="fieldSetBorder" style="border:0px">
				  	<table class="" id="patDetailsDialogTableHeader">
				  		 <!-- <tr><td class="formlabel">Image:</td>
							<td class="forminfo" id="Image_sc1">
								<img id="patient-card-image" style="width:72px;height:96px" src="" />
							</td>
						</tr> -->
				  		<tr><td style="width:100px; padding:10px">National ID:</td>
							<td class="forminfo" style="width:240px; padding:10px">
								<label id="nationalid_sc1"></label>
							</td>
						</tr>
						<tr><td style="width:100px; padding:10px">Title:</td>
								<td class="forminfo" style="width:240px; padding:10px">
									<label id="title_sc1" ></label>
								</td>
						</tr>
						<tr><td style="width:100px; padding:10px">Patient Name:</td>
							<td class="forminfo" style="width:240px; padding:10px">
							<label id="patientName_sc1"></label>
							</td>
						</tr>
						<tr><td style="width:100px; padding:10px">Sex:</td>
							<td class="forminfo" style="width:240px; padding:10px">
								<label id="gender_sc1"></label>
							</td>
						</tr>
						<tr><td style="width:100px; padding:10px">Nationality:</td>
							<td class="forminfo" style="width:240px; padding:10px">
							<label id="nationality_sc1"></label>
						</td>
						</tr>
						<tr><td style="width:100px; padding:10px">DOB:</td>
							<td class="forminfo" style="width:240px; padding:10px">
							<label  id="dob_sc1" ></label>
							</td>
						</tr>
				  	</table>
				 </fieldset>
				</td></tr>
			   </table>
	</div>
	<div class="bd" style="width:600px">
  		<table>
			<tr>
				<td>
					<input type="button" value="<insta:ltext key='patient.registration.button.save'/>" style="cursor:pointer;" onclick="saveDetailsInRegScreen();" />
				</td>
				<td>
					<input type="button" value="<insta:ltext key='patient.registration.button.cancel'/>" style="cursor:pointer;" onclick="handlePatDetailsFromSCDialogCancel();"/>
				</td>
			</tr>
		</table>
 	</div>
</div>


<div id="patientDetailsFromSmartCardDialogForErr" style="display: none;visibility: hidden;">
<div class="hd"><insta:ltext key="patient.registration.nationalid.card.details.smartcard" /></div>
  <div class="bd" >
  <table id="pd_patDetailsFromSCDialogTableMainErr">
  <tr><td>
	<fieldset class="fieldSetBorder" style="width:400px; height:130px;white-space: normal">
	  	<table class="formTable" id="patDetailsDialogTableHeaderForErr">
	  		<tr><td align="center">
	  		<img src="${cpath}/images/warningExclamation.png"/>
	  		</td></tr>
	  		<tr><td align="center" style="color:red; font-size:15 "><label id="pd_sc_error"></label></td></tr>
	  	</table>
	 </fieldset>
  </td></tr>
   </table>
	  <div style="height:6px;">&nbsp;</div>
	  <table>
		<tr><td>
				<input type="button" value="<insta:ltext key='patient.registration.button.save'/>" style="cursor:pointer;" onclick="closeErrDialogue();" />
		</td></tr>
	</table>
  </div>
  </div>
</form>

<script>
	var ajaxDetails;
	var policynames;
	var companyTpaList;
	var discountPlansJSON;
	var bedCharges = null;
	var wardsList = <%= request.getAttribute("wardsJson") %>;
	var orgId = 'ORG0001';
	var bedTypesList = <%= request.getAttribute("bedTypesJson") %>;
	// TODO : sponsor type migration - perdayrate
	var tpanames;
	var insuCatNames;
	var insuCompanyDetails;
	var doctorsList = ${doctorsList};
	var genRegCharge = '${genRegCharge}';
	var contextPath = '<%=request.getContextPath()%>';
	var masterTimestamp = '${masterTimestamp}';
	var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
	var categoryJSON;
	var patientConfidentialityCategoriesJSON = ${patientConfidentialityCategories};
	var orgNamesJSON;
	var jDocDeptNameList = <%= request.getAttribute("docDeptNameList") %>;
	var unitList = ${unitDetails};
	var deptList = ${deptsList};
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:${addnlOpen}});
	var VisitCollapsiblePanel1 = (document.getElementById("VisitCollapsiblePanel1")) ?
					new Spry.Widget.CollapsiblePanel("VisitCollapsiblePanel1", {contentIsOpen:${visitAddnlOpen}}) : null;
	var mod_mrd_icd =  '${preferences.modulesActivatedMap.mod_mrd_icd}';
	var gen_category = ${gen_category};
	var sponsorTypeList= ${sponsorTypelist};
	var deadPatientMessage = "<%=bundle.getMessage("ui.notification.registration.patient.not.alive")%>";
</script>

<c:if test="${screenId eq 'ip_registration'}">
	<insta:AddOrderDialog visitType="i"/>
</c:if>
<jsp:include page="/pages/orderEditCommon.jsp"/>
<insta:link type="js" file="select2.min.js"/>
<insta:link type="js" file="registration/registrationPhoneNumberCommon.js"/>
<script type="text/javascript">
	mealTimingsRequired = true;
	equipTimingsRequired = true;
</script>

</body>

</html>
