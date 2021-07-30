<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page isELIgnored ="false" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hijricalendar" value="<%= GenericPreferencesDAO.getGenericPreferences().getHijriCalendar()%>" />
<c:set var="is_patient_phone_mandate" value="N"/>
<c:if test="${not empty regPref.patientPhoneValidate && (regPref.patientPhoneValidate eq 'A')}">
	<c:set var="is_patient_phone_mandate" value="Y" />
</c:if>
<c:set var="is_patient_email_mandate" value="N"/>
<c:if test="${not empty regPref.validate_email_id && (regPref.validate_email_id eq 'A')}">
	<c:set var="is_patient_email_mandate" value="Y" />
</c:if>
<c:set var="is_patient_care_oftext_mandate" value="N" />	
<c:if test="${not empty regPref.nextOfKinValidate &&
		(regPref.nextOfKinValidate eq 'A')}">
		<c:set var="is_patient_care_oftext_mandate" value="Y" />										
</c:if>
<c:set var= "patientIdentification" value = "${ifn:cleanJavaScript(centerPrefs.map.patient_identification)}" />
<html>
<head>
	<script>
		var defaultCountryCode= '+${defaultCountryCode}';
        var hijriPref = '${hijricalendar}';
		var roleid = '${roleId}';
		var mrNo = '${ifn:cleanJavaScript(param.mrno)}';
		
    	var regPref = ${regPrefJSON};
    	var healthAuthoPref = ${healthAuthoPrefJSON};
    	var agedisable = '${ifn:cleanJavaScript(regPref.allow_age_entry)}';
		var allowNewRegistration = '${actionRightsMap.allow_new_registration}';
		var contextPath = '<%= request.getContextPath()%>';
		var categoryExpirydateText = '${ifn:cleanJavaScript(regPref.categoryExpiryDate)}';
		var oldRegNumFieldText = '${ifn:cleanJavaScript(regPref.oldRegNumField)}';
		var patientCategoryLabel = '${ifn:cleanJavaScript(regPref.patientCategory)}';
		var defaultPatientCategory = '${centerPrefs.map.pref_pre_reg_default_category}';
		var savedPatientCategoryId = '${patient.patient_category_id}';
		var salutationJSON = ${salutationQueryJson};
		var patientConfidentialityCategoriesJSON = ${patientConfidentialityCategories};
		
		var allowFieldEdit = "${(actionRightsMap.edit_custom_fields == 'A' || roleId == '1' || roleId == '2') ? 'A' : 'N' }";
		var categoryJSON = ${categoryWiseRateplans};
		var defaultCity = '${defaultCity}';
		var defaultState = '${defaultState}';
		var defaultCountry = '${defaultCountry}';
		var defaultCityName = '${defaultCityName}';
		var defaultStateName = '${defaultStateName}';
		var defaultCountryName = '${defaultCountryName}';
		var originalMrNo = '${patient.original_mr_no}';
		var govtId_pattern = '';
		var govtId_label = regPref.government_identifier_label;
		var govtId_type_label = regPref.government_identifier_type_label;
		var govtIdentifierTypesJSON = <%=request.getAttribute("govtIdentifierTypesJSON")%>;
		var otherIdentifierTypesJSON = <%=request.getAttribute("otherIdentifierTypesJSON")%>;
		var patientIdentification = '${ifn:cleanJavaScript(centerPrefs.map.patient_identification)}';
		var govtIdentifierMandatory = '';
		var uniqueGovtID = '';
		var screenid= '${screenId}';
		var screenId= '${screenId}';
		var gVisitId = '${ifn:cleanJavaScript(visitId)}';
		
		var isPatientPhoneMandate= '${is_patient_phone_mandate}';
		var isPatientEmailMandate= '${is_patient_email_mandate}';
		var isPatientCareOftextMandate = '${is_patient_care_oftext_mandate}';
		var patientPhoneInitialValue = '${patient.patient_phone}';
		var kinPhoneInitialValue= '${patient.patcontactperson}';
		var patientConfidentialityGroup= '${patient.patient_group}';
		var patientConfidentialityGroupName= '${patient.patient_group_name}';
		var allowMarkDuplicate='${allowMarkDuplicate}';
		var smartCardEnabled = '${centerPrefs.map.pref_smart_card_enabled}';
		var smartCardIDPattern = '${centerPrefs.map.smart_card_id_pattern}';
		var mod_mobile = '${preferences.modulesActivatedMap['mod_mobile']}';
	</script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title><insta:ltext key="registration.patient.preregistration.details.pre_registration.instahms"/></title>
<meta name="i18nSupport" content="true"/>
<insta:link type="js" file="date_go.js"/>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="registration/pre-registration.js" />
<insta:link type="js" file="registration/registrationCommon.js"/>
<insta:link type="js" file="lightbox.js"/>
<insta:link type="css" file="lightbox.css"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<insta:link type="css" file="select2.min.css"/>
<insta:link type="css" file="select2Override.css"/>
<insta:link type="script" file="moment.min.js"/>

<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="common.message"/>
<insta:js-bundle prefix="registration.preregistration"/>
<style>
	#patientinfo .formlabel{
		width: 100px;
		
	}
	td.patient_group_td {
		display: none;
	}
</style>
</head>
<body onload="initPreRegistration();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="title">
<insta:ltext key="registration.patient.preregistration.details.title"/>
</c:set>
<c:set var="patientlogTitle">
<insta:ltext key="registration.patient.preregistration.details.patient.auditlog"/>
</c:set>
<c:set var="allowAgeEntry" value="<%=RegistrationPreferencesDAO.getRegistrationPreferences().getAllow_age_entry()%>" />
<script>
	var allowAgeEntry = "${allowAgeEntry}";
</script>
<c:set var="patientgender">
<insta:ltext key="selectdb.dummy.value"/>,
<insta:ltext key="registration.patient.preregistration.details.male"/>,
<insta:ltext key="registration.patient.preregistration.details.female"/>,
<insta:ltext key="registration.patient.preregistration.details.couple"/>,
<insta:ltext key="registration.patient.preregistration.details.others"/>
</c:set>
<c:set var="agein">
<insta:ltext key="registration.patient.preregistration.details.years"/>,
<insta:ltext key="registration.patient.preregistration.details.months"/>,
<insta:ltext key="registration.patient.preregistration.details.days"/>
</c:set>
<c:set var="portalpataccess">
<insta:ltext key="registration.patient.preregistration.details.yes"/>,
<insta:ltext key="registration.patient.preregistration.details.no"/>
</c:set>
<c:choose>
	<c:when  test="${not empty param.mr_no}">
		<div class="pageheader" style="float: left"><insta:ltext key="registration.patient.preregistration.details.editregistration"/></div>
		<div>
			<insta:patientsearch searchType="mrNo" fieldName="mrno"
			searchUrl="GeneralRegistration.do" searchMethod="show" showDuplicateMrNos="true"/>
		</div>
	</c:when>
	<c:otherwise>
		<div class="pageheader"><insta:ltext key="registration.patient.preregistration.details.preregistration"/></div>
	</c:otherwise>
</c:choose>

<insta:feedback-panel/>
<form name="mainform" method="POST" action="GeneralRegistration.do" enctype="multipart/form-data">
<input type="hidden" name="mr_no" value="${patient.mr_no}" />
<c:if test="${empty patient.mr_no}">
<input type="hidden" name="resource_captured_from" id="resource_captured_from" value="register"/>
</c:if>
<input type="hidden" name="_method" value="insertOrUpdate" />
<input type="hidden" name="pdateofbirth" id="pdateofbirth" value="${dateofbirth}" />
<input type="hidden" name="dateOfBirth" id="dateOfBirth"/>
<input type="hidden" name="imageUrl" value="N"/>
<input type="hidden" name="photosize" value="${patient.photo_size}"/>
<input type="hidden" name="tempMrNo"/>
<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(visitId)}" />
<input type="hidden" name="categoryId" value="${patient.patient_category_id}"/>
<input type="hidden" name="categoryExpiryDate" value="${patient.category_expiry_date}"/>
<input type="hidden" name="areaValidate" value="${ifn:cleanHtmlAttribute(regPref.areaValidate)}" />
<input type="hidden" name="addressValidate" value="${ifn:cleanHtmlAttribute(regPref.addressValidate)}" />
<input type="hidden" name="validateEmailId" value="${ifn:cleanHtmlAttribute(regPref.validate_email_id)}"/>
<input type="hidden" name="nextofkinValidate" value="${ifn:cleanHtmlAttribute(regPref.nextOfKinValidate)}" />
<input type="hidden" name="patientPhoneValidate" value="${ifn:cleanHtmlAttribute(regPref.patientPhoneValidate)}" />
<input type="hidden" name="middle_name_split" id="middle_name_split" value="${patient.middle_name}" />
<input type="hidden" name="name_parts_pref" id="name_parts_pref" value="${ifn:cleanHtmlAttribute(regPref.name_parts)}" />
<input type="hidden" name="appointmentId" value="">

<c:set var="patCity" value="${defaultCity}"/>
<c:set var="patState" value="${defaultState}"/>
<c:set var="patDistrict" value="${defaultDistrict}"/>
<c:set var="patCountry" value="${defaultCountry}"/>

<c:set var="patCityName" value="${defaultCityName}"/>
<c:set var="patDistrictName" value="${defaultDistrictName}"/>
<c:set var="patStateName" value="${defaultStateName}"/>
<c:set var="patCountryName" value="${defaultCountryName}"/>
<c:set var="disabledagefield" value="${regPref.allow_age_entry eq 'N'  ? 'disabled' : ''}"/>
<c:if test="${not empty patient}">
	<c:set var="patCity" value="${patient.patient_city}"/>
	<c:set var="patDistrict" value="${patient.district_id}"/>
	<c:set var="patState" value="${patient.patient_state}"/>
	<c:set var="patCountry" value="${patient.country}"/>

	<c:set var="patCityName" value="${patient.city_name}"/>
	<c:set var="patDistrictName" value="${patient.district_name}"/>	
	<c:set var="patStateName" value="${patient.state_name}"/>
	<c:set var="patCountryName" value="${patient.country_name}"/>
</c:if>

<input type="hidden" name="area_id" id="area_id"/>
<input type="hidden" name="patient_city" id="city_id" value="${patCity}"/>
<input type="hidden" name="patient_district" id="district_id" value="${patDistrict}"/>
<input type="hidden" name="patient_state" id="state_id" value="${patState}"/>
<input type="hidden" name="country" id="country_id" value="${patCountry}"/>

<table class="formtable" width="100%">
	<tr>
		<td>
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="registration.patient.preregistration.details.basicinformation"/></legend>
			<table width="100%" cellspacing="0" cellpadding="0" class="formtable" id="patientinfo">
				<c:if test="${not empty patient}">
					<tr>
						<td class="formlabel"><insta:ltext key="ui.label.mrno"/>.:</td>
						<td class="forminfo">${patient.mr_no}</td>
						<td class="formlabel" colspan="2"><input type="checkbox" name="is_duplicate" ${ allowMarkDuplicate != 'A' ? 'disabled' : '' } id="is_duplicate"  ${not empty patient.original_mr_no ? 'checked' : ''} 
							onclick="isDuplicateChecked();"/><label for="is_duplicate"><insta:ltext key="registration.patient.preregistration.details.markasduplicateof"/>:</label>
						</td>
						<td class="forminfo" valign="top" colspan="3">
							<div id="mrnoAutoComplete">
								<input type="text" name="original_mr_no" id="original_mr_no" style="width:160px" value="${patient.original_mr_no}" ${ allowMarkDuplicate != 'A' ? 'disabled' : '' } disabled/>
								<div id="mrnoContainer" style="width: 300px"></div>
							</div>
						</td>
					</tr>
				</c:if>

	      		<tr>
	        		<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.name"/>:</td>
	        		<td style="white-space: nowrap" colspan="3">
						<insta:selectdb id="salutation" name="salutation" value="${patient.salutation_id}" table="salutation_master"
							valuecol="salutation_id"  displaycol="salutation" usecache="true"
							onchange="salutationChange()" dummyvalue="${title}" style="width: 80px;"/>
						<input type="text" name="patient_name"  id="patient_name" class="field"
							 maxlength="50" value="${patient.patient_name}"
							onblur="capWords(patient_name);" style="width: 90px;"
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'}/>

						<input type="text" name="middle_name" id="middle_name" size="15"  class="field"
							maxlength="50" value="${patient.middle_name}"
							onblur="capWords(middle_name);" style="width: 90px;" style="width: 90px;">
						<c:if test="${regPref.name_parts == 4}">
						<input type="text" name="middle_name2" id="middle_name2" size="15"  class="field"
							maxlength="50" value=""
							onblur="capWords(middle_name2);" style="width: 90px;" style="width: 90px;">
						</c:if>
						<input type="text" name="last_name" id="last_name" size="15"  class="field"
							maxlength="50" value="${patient.last_name}"
							onblur="capWords(last_name);" style="width: 90px;">
							<span class="star">*</span>
					</td>
					
	          		

					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.mobileno"/>.:</td>
	          		<td colspan=2>
						<div style="margin-top:12px">
							<div>
								<input type="hidden" id="patient_phone" name="patient_phone"/>
								<input type="hidden" id="patient_phone_valid" value="N"/>
								<select id="patient_phone_country_code" class="dropdown" style="width:76px" name="patient_phone_country_code">
									<c:if test="${empty defaultCountryCode}">				
											<option value='+' selected> - Select - </option>
									</c:if>
									<c:forEach items="${countryList}" var="list">
										<c:choose>
											<c:when test="${ list[0] == defaultCountryCode}">				
												<option value='+${list[0]}' selected> +${list[0]}(${ list[1]})  </option>	
											</c:when>	
											<c:otherwise>
												<option value='+${list[0]}'> +${list[0]}(${list[1]})  </option>	
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
								<span style="visibility:hidden;padding-left:10px;color:red" id="patient_phone_error"></span>	
							</div>

						</div>
	          		</td>
	        	</tr>
	        	<tr>
	        		<td class="formlabel">Read Card:</td>
	        		<td align="left">
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
	        	</tr>
	        	<c:if test="${regPref.name_local_lang_required == 'Y'}">
	        	<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.commonlabel.patinentnameinlocallang"/>:</td>
					<td colspan = 6 >
					<input type="text" class="field" name="name_local_language" id="name_local_language"
						maxlength="100" style="width:350px" value="${patient.name_local_language}">
					</td>
				</tr>
				</c:if>
				<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.gender"/>:</td>
					<td style="white-space: nowrap; text-align: left" colspan="2">
	          			<c:choose>
	          				<c:when test="${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2)}">
	          					<insta:selectoptions id="patient_gender" name="patient_gender" opvalues=" ,M,F,C,O" optexts="${patientgender}"
										value="${patient.patient_gender}" style="width: 90px;"/>
	          				</c:when>
	          				<c:otherwise>
	          					<insta:selectoptions id="patient_gender" name="patient_gender" opvalues=" ,M,F,C,O" optexts="${patientgender}"
										value="${patient.patient_gender}" style="width: 90px;" disabled="true"/>
	          				</c:otherwise>
	          			</c:choose>
							<span class="star">*</span>
					</td>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.dateofbirth"/>:</td>
					<td colspan=4>
						<input type="text" class="field" style="width:30px;" size="2"
							maxlength="2"  name="dobDay" onkeypress="return enterNumOnly(event)"
						 	id="dobDay" value="DD" onFocus="if (this.value == 'DD') { this.value = ''}"
							onBlur="if (this.value == '') { this.value = 'DD'} else { if(hijriPref == 'Y') gregorianToHijri(); }" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("gregDay")) %>"
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'}/>
						<input type="text" class="field" style="width:25px;" size="2"
							maxlength="2" name="dobMonth" id="dobMonth" value="MM"
							onFocus="if (this.value == 'MM') { this.value = ''}"
							onBlur="if (this.value == '') { this.value = 'MM'} else { if(hijriPref == 'Y') gregorianToHijri(); }" onkeypress="return enterNumOnly(event)" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("gregMonth")) %>" 
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'}/>
						<input type="text" class="field" size="4" maxlength="4" name="dobYear" id="dobYear"
							style="width:40px;"
							onblur="if (this.value == '') { this.value = 'YY'; enableAge(); } else { return calculateAgeAndHijri(); }"
							 value="YY" onkeypress="return enterNumOnly(event)"
							onFocus="if (this.value == 'YY') { this.value = ''}" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("gregYear")) %>"
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'}/>
						<insta:ltext key="registration.patient.preregistration.details.or"/>
						<c:choose>
					   		<c:when test="${(allowAgeEntry eq 'Y' && (empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2))}">
				       			<input type="text" name="age" id="age" value="${empty pat_age?'..Age..':pat_age}" class="field"
									onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="3"
									onFocus="if (this.value == '..Age..') { this.value = ''}"
									onBlur="if (this.value == '') { this.value = '..Age..'; enableDobAndHijriDob()} else { dissableDobAndHijriDob(); }"
					   				style="width:45px; padding:0 0 2px 2px" />
							</c:when>
							<c:otherwise>
								<input type="text" id="age" name="age" value="${empty pat_age?'..Age..':pat_age}" class="field"
									onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="3" readonly
									onFocus="if (this.value == '..Age..') { this.value = ''}"
					   				onBlur="if (this.value == '') { this.value = '..Age..'; }" 
					   				style="width:45px; padding:0 0 2px 2px"/>
					   			</c:otherwise>
						</c:choose>
						
					  		<c:choose>
	          				<c:when test="${(allowAgeEntry eq 'Y' && (empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2))}">
				       			<insta:selectoptions name="ageIn" id="ageIn" opvalues="Y,M,D"
				       				style="padding:0 0 2px 2px; width:70px;" optexts="${agein}"
										value="${patient_agein}"/>
								</c:when>
								<c:otherwise>
									<insta:selectoptions name="ageIn" id="ageIn" opvalues="Y,M,D" disabled="true"
				       				style="padding:0 0 2px 2px; width:70px;" optexts="${agein}"
										value="${patient_agein}"/>
								</c:otherwise>
							</c:choose>
						<span class="star">*</span>				
					</td>
					</tr>
					<tr>
						<c:if test="${hijricalendar=='Y'}">
							<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.dateofbirth"/></br>(Hijri):</td>
							<td style="white-space: nowrap;">
								<input type="text" class="field" style="width:30px;" size="2"
									maxlength="2"  name="dobHDay" onkeypress="return enterNumOnly(event)" 
								 	id="dobHDay" value="<insta:ltext key="registration.patient.show.dd.text"/>" onFocus="if (this.value == '<insta:ltext key="registration.patient.show.dd.text"/>') { this.value = ''}"
									onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.dd.text"/>'} else { if(hijriPref == 'Y') hijriToGregorian(); }" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("hijriDay")) %>">
	
	
								<input type="text" class="field" style="width:30px;" size="2"
									maxlength="2" name="dobHMonth" id="dobHMonth" value="<insta:ltext key="registration.patient.show.mm.text"/>"
									onFocus="if (this.value == '<insta:ltext key="registration.patient.show.mm.text"/>') { this.value = ''}"
									onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.mm.text"/>'} else { if(hijriPref == 'Y') hijriToGregorian();}" onkeypress="return enterNumOnly(event)" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("hijriMonth")) %>">
	
								<input type="text" class="field" size="4" maxlength="4" name="dobHYear" id="dobHYear"
									style="width:40px;" onblur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.yyyy.text"/>'; enableAge(); } else { if(hijriPref == 'Y') hijriToGregorian();}"
									 value="<insta:ltext key="registration.patient.show.yyyy.text"/>" onkeypress="return enterNumOnly(event)" 
									onFocus="if (this.value == '<insta:ltext key="registration.patient.show.yyyy.text"/>') { this.value = ''}" value="<%= com.insta.hms.common.Encoder.cleanJavaScript((String)request.getParameter("hijriYear")) %>"><%-- <insta:ltext key="registration.patient.commonlabel.or.within.brackets"/> --%>
						
								<img title='<insta:ltext key="patient.registration.hijri.calendar.range.note"/>' src="${cpath}/images/help.png" class="imgHelpText">
							
							</td>
						</c:if>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.addnlphone"/>:</td>
					<td colspan=6>
					  	<input type="text" class="field" name="patient_phone2" id="patient_phone2"
						maxlength="100" onblur="capWords(relation)"  value="${patient.addnl_phone}">
					</td>
				</tr>
				
				<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.nextofkinname"/>:</td>
					<td >
						<input type="text" class="field" name="relation" id="relation"
						maxlength="100" onblur="capWords(relation)"  value="${patient.patrelation}">
						<c:if test="${is_patient_care_oftext_mandate == 'Y'}">
							<span class="star">*</span>
						</c:if>
					</td>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.relation"/>:</td>
	        		<td>
	        			<input type="text" name="next_of_kin_relation"  maxlength="30"
						id="patientCareofAddress" value="${patient.next_of_kin_relation}">
	        		</td>
	          		<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.contactno"/>.:</td>
            		<td colspan=2>
						<div style="margin-top:12px">
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
												<option value='+${list[0]}' selected> +${list[0]}(${ list[1]})  </option>;	
											</c:when>	
											<c:otherwise>
												<option value='+${list[0]}'> +${list[0]}(${list[1]})  </option>;	
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
								<span style="visibility:hidden;padding-left:10px;color:red" id="patient_care_oftext_error" ></span>	
							</div>

						</div>
            		</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.photo"/>:</td>
					<td >
					 	<input type="file" name="patientPhoto"  accept="<insta:ltext key="upload.accept.image"/>"/>
						<div id="viewPhoto" style="display: none">
						 <c:url value="GeneralRegistration.do" var="Url">
							<c:param name="_method" value="viewPatientPhoto" />
							<c:param name="mrno" value="${patient.mr_no}" />
						</c:url>
						<a href="${Url}"  rel="lightbox" title="Patient Photo" ><insta:ltext key="registration.patient.preregistration.details.viewphoto"/></a>
						</div>
					</td>
	  				<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.nextofkinaddress"/>:</td>
					<td>
						<input type="text" name="patient_careof_address"  maxlength="250"
							id="patientCareofAddress" value="${patient.pataddress}" style="width:11.5em">
						<c:if test="${is_patient_care_oftext_mandate == 'Y'}">
									<span class="star">*</span>
						</c:if>
					</td>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.vippatient"/>:</td>
					<td colspan=2 >
						<input type="checkbox" name="vip_check" id="vip_check" ${patient.vip_status == 'Y'?'checked':''}
							onclick="enableVipStatus()"/>
						<input type="hidden" name="vip_status" id="vip_status" value="${patient.vip_status}">
					</td>
	            </tr>
	            <tr>
	         		<c:if test="${regPref.patientCategory != '' && regPref.patientCategory != null}">
						<td class="formlabel">${ifn:cleanHtml(regPref.patientCategory)}:</td>
						<td>
							<select name="patient_category_id" id="patient_category_id" class="dropdown"
								onchange="categoryChange();showHideCaseFile();" size="1">
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
					<%--
					<c:if test="${regPref.categoryExpiryDate != '' && regPref.categoryExpiryDate != null}">
						<td class="formlabel">${regPref.categoryExpiryDate}:</td>
						<td><insta:datewidget name="category_expiry_date" valid="future"
							value="${not empty patient.category_expiry_date ? patient.category_expiry_date : ''}"/></td>
					</c:if>
					--%>
				</tr>
				<tr>
					<td colspan="2">
						<c:if test="${regPref.caseFileSetting != null && regPref.caseFileSetting != '' && regPref.caseFileSetting == 'Y'}">
							<table id="caseFileFields" style="display:block;">
								<tr>
									<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.casefileno"/>:</td>
									<td>
										<input type="text" name="casefileNo" id="casefileNo" size="10" value="${patient.casefile_no}"
										onblur="enableCaseFileAutoGen();" onkeyup="upperCase(casefileNo)"
										maxlength="20" onchange="return checkUniqueCasefileNo();">
									</td>
									<td>
										<input type="checkbox" name="oldRegAutoGenerate" value="Y" onclick="enableOldmrno()"
										<c:if test="${not empty patient.casefile_no}"> <insta:ltext key="registration.patient.preregistration.details.disabled"/></c:if> ><insta:ltext key="registration.patient.preregistration.details.auto"/>.
									</td>
									<td></td>
									<td></td>
								</tr>
								<c:if test="${not empty patient.casefile_no && casefile_no ne ''}">
								<tr>
									<td class="formlabel">
										<label id="issuedToLabelId"><insta:ltext key="registration.patient.preregistration.details.issuedto"/>:</label>
									</td>
									<td>
										<label id="caseFileIssuedBy" class="forminfo">${patient.issued_to}</label>
									</td>
									<td></td>
									<td></td>
									<td></td>
								</tr>
								</c:if>
							</table>
						</c:if>
					</td>
					<c:if test="${regPref.oldRegNumField != '' && regPref.oldRegNumField != null}">
						<td class="formlabel">${ifn:cleanHtml(regPref.oldRegNumField)}:</td>
						<td style="width: 100px;padding-top:15px;">
							<input type="text" name="oldmrno" id="oldmrno" size="15" value="${patient.oldmrno}"
									onkeyup="upperCase(oldmrno)" maxlength="20" onchange="return checkUniqueOldMrno();">
							<img id="previoushospimg" name="previoushospimg" src="<%=request.getContextPath()%>/images/ajax-loader.gif"
								title='<insta:ltext key="registration.patient.preregistration.details.verifyingoldmrno"/>' width="16" height="16" style="vertical-align:top;visibility:hidden;"/>
						</td>
					</c:if>
					<td class="formlabel"></td>
					<td colspan = 4></td>
				</tr>
			</table>
		</fieldset>
		</td>
	</tr>
</table>

<div id="CollapsiblePanel1" class="CollapsiblePanel">
	<div class=" title CollapsiblePanelTab" style=" border-left:none;">
    	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;"><insta:ltext key="registration.patient.preregistration.details.additionalpatient"/>&nbsp;<b><u><insta:ltext key="registration.patient.preregistration.details.i"/></u></b><insta:ltext key="registration.patient.preregistration.details.nformation"/></div>
		<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
		<img src="${cpath}/images/up.png" /></div>
		<div class="clrboth"></div>
	</div>
	<div class="CollapsiblePanelContent">
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<c:choose>
					<c:when test="${regPref.enableDistrict == 'Y'}">
						<tr>
							<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.address"/>:</td>
							<td rowspan="2">
								<textarea  class="field" style="width:140px; height:50px; padding:0 0 2px 2px"
									name="patient_address" id="patient_address" >${patient.patient_address}</textarea>
								<c:if test="${not empty regPref.addressValidate && (regPref.addressValidate eq 'A')}">
									<span class="star">*</span>
								</c:if>
							</td>
							<td class="formlabel"><insta:ltext key="ui.label.area.village"/>:</td>
							<td valign="top">
								<div id="autoarea" class="autoComplete">
									<input name="patient_area" id="patient_area" type="text" maxlength="50"
									value="${patient.patient_area.replace('"',"&quot")}" style="width:11.6em"/>
									<div id="area_dropdown" style="width:250px"></div>
								</div>
								<c:if test="${not empty regPref.areaValidate && (regPref.areaValidate eq 'A')}">
									<span class="star">*</span>
								</c:if>
							</td>
							<td class="formlabel"><insta:ltext key="ui.label.district"/>:</td>
							<td><label id="districtlbl" class="formlabel">${patDistrictName}</label></td>
				   		</tr>
				   		<tr>
				   			<td>&nbsp</td>
							<td class="formlabel"><insta:ltext key="ui.label.city.subdistrict"/>:</td>
							<td valign="top">
								<div id="city_state_country_wrapper" class="autoComplete">
									<input type="text" name="pat_city_name" id="pat_city_name" value="${patCityName}"/>
									<div id="city_state_country_dropdown" style="width:250px"></div>
								</div>
								<span class="star">*</span>
							</td>
				   			<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.state"/>:</td>
							<td><label id="statelbl" class="formlabel">${patStateName}</label></td>
				   		</tr>
				   		<tr>
				   			<td>&nbsp</td>
				   			<td>&nbsp</td>
				   			<td>&nbsp</td>
				   			<td>&nbsp</td>
				   			<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.country"/>:</td>
					  	  	<td><label id="countrylbl" class="formlabel">${patCountryName}</label></td>
				   		</tr>
					</c:when>
					<c:otherwise>
						<tr>
							<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.address"/>:</td>
							<td rowspan="2">
								<textarea  class="field" style="width:140px; height:50px; padding:0 0 2px 2px"
									name="patient_address" id="patient_address" >${patient.patient_address}</textarea>
								<c:if test="${not empty regPref.addressValidate && (regPref.addressValidate eq 'A')}">
									<span class="star">*</span>
								</c:if>
							</td>
							<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.area"/>:</td>
							<td valign="top">
								<div id="autoarea" class="autoComplete">
									<input name="patient_area" id="patient_area" type="text" maxlength="50"
									value="${patient.patient_area.replace('"',"&quot")}" style="width:11.6em"/>
									<div id="area_dropdown" style="width:250px"></div>
								</div>
								<c:if test="${not empty regPref.areaValidate && (regPref.areaValidate eq 'A')}">
									<span class="star">*</span>
								</c:if>
							</td>
							<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.state"/>:</td>
							<td><label id="statelbl" class="formlabel">${patStateName}</label></td>
				   		</tr>
						<tr>
							<td>&nbsp</td>
							<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.city"/>:</td>
							<td valign="top">
								<div id="city_state_country_wrapper" class="autoComplete">
									<input type="text" name="pat_city_name" id="pat_city_name" value="${patCityName}"/>
									<div id="city_state_country_dropdown" style="width:250px"></div>
								</div>
		
								<span class="star">*</span>
							</td>
					   		<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.country"/>:</td>
					  	  	<td><label id="countrylbl" class="formlabel">${patCountryName}</label></td>
						</tr>
					</c:otherwise>
				</c:choose>
					<tr>
						<td class="formlabel"><insta:ltext key="ui.label.master.blood.group"/>:</td>
						<td>
						<insta:selectdb name="blood_group_id" table="blood_group_master"
						dummyvalue="---Select---" dummyvalueId="" value="${patient.blood_group_id}" valuecol="blood_group_id"
						displaycol="blood_group_name"
						filtered="true" filtercol="status" filtervalue="A"/>
						</td>

						<td class="formlabel"><insta:ltext key="ui.label.master.marital.status"/>:</td>
						<td>
						<insta:selectdb name="marital_status_id" table="marital_status_master"
						dummyvalue="---Select---" dummyvalueId="" value="${patient.marital_status_id}" valuecol="marital_status_id"
						displaycol="marital_status_name"
						filtered="true" filtercol="status" filtervalue="A"/>
						<c:if test="${not empty regPref.maritalStatusRequired && (regPref.maritalStatusRequired eq 'Y')}">
							<span class="star">*</span>
                        </c:if>
						</td>

						<td class="formlabel"><insta:ltext key="ui.label.master.religion"/>:</td>
						<td>
						<insta:selectdb name="religion_id" table="religion_master"
						dummyvalue="---Select---" dummyvalueId="" value="${patient.religion_id}" valuecol="religion_id"
						displaycol="religion_name"
						filtered="true" filtercol="status" filtervalue="A"/>
						<c:if test="${not empty regPref.religionRequired && (regPref.religionRequired eq 'Y')}">
							<span class="star">*</span>
						</c:if>
						</td>

						<td class="formlabel"><insta:ltext key="ui.label.master.race"/>:</td>
						<td>
						<insta:selectdb name="race_id" table="race_master"
						dummyvalue="---Select---" dummyvalueId="" value="${patient.race_id}" valuecol="race_id"
						displaycol="race_name"
						filtered="true" filtercol="status" filtervalue="A"/>
						</td>
					</tr>
				<c:set var="maincolumns" value="0"/>
				<tr>
					<c:forEach var="num" begin="1" end="9">
					<c:set var="nameField" value="custom_list${num}_name"/>
					<c:set var="showField" value="custom_list${num}_show"/>
					<c:set var="validateField" value="custom_list${num}_validate"/>
					<c:set var="valueCol" value="custom_list${num}_value"/>
					<c:set var="customTableName" value="custom_list${num}_master"/>
					<c:if test="${not empty regPref[nameField] && not empty regPref[showField] && (regPref[showField] eq 'M' || regPref[showField] eq 'D')}">
						<td class="formlabel">${regPref[nameField]}:</td>
						<td>
							<select name="custom_list${num}_value" id="custom_list${num}_value" class="dropdown">
								<option value="">${dummyvalue}</option>
								<c:forEach var="customList" items="${customListMap[customTableName]}">
									<option value='<c:out value="${customList.map.custom_value}"/>' ${customList.map.custom_value == patient[valueCol] ? 'selected' : ''}>
										<c:out value="${customList.map.custom_value}"/>
									</option>
								</c:forEach>
							</select>
							<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
											(screenId eq 'ip_registration' && regPref[validateField] eq 'I'))}">
							<span class="star">*</span>
							</c:if>
						</td>
						<c:set var="maincolumns" value="${maincolumns+1}"/>
						<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
					</c:if>
					</c:forEach>

					<c:forEach var="num" begin="1" end="19">
						<c:set var="labelField" value="custom_field${num}_label"/>
						<c:set var="showField" value="custom_field${num}_show"/>
						<c:set var="validateField" value="custom_field${num}_validate"/>
						<c:set var="valueCol" value="custom_field${num}"/>
						<c:if test="${not empty regPref[labelField] && not empty regPref[showField] && (regPref[showField] eq 'M' || regPref[showField] eq 'D')}">
							<td class="formlabel">${regPref[labelField]}:</td>
							<td>
								<c:choose>
									<c:when test="${num le 13}">
										<input type="text" class="field" name="custom_field${num}" maxlength="50" tabindex="130" value="${patient[valueCol]}">
									</c:when>
									<c:when test="${num le 16}">
										<c:set var="fieldValue">
											<fmt:formatDate pattern="dd-MM-yyyy" value="${patient[valueCol]}"/>
										</c:set>
										<insta:datewidget name="custom_field${num}"  title="custom_field${num}" tabindex="130" value="${fieldValue}"/>
									</c:when>
									<c:otherwise>
										<input type="text" class="number" name="custom_field${num}"  tabindex="130" value="${patient[valueCol]}"/>
									</c:otherwise>
								</c:choose>
								<c:if test="${not empty regPref[validateField] && (regPref[validateField] eq 'A' ||
													(screenId eq 'ip_registration' && regPref[validateField] eq 'I'))}">
								<span class="star">*</span>
								</c:if>
							</td>
							<c:set var="maincolumns" value="${maincolumns+1}"/>
							<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
						</c:if>
					</c:forEach>


					<c:if test="${not empty regPref.passport_no && not empty regPref.passport_no_show
									&& (regPref.passport_no_show eq 'M' || regPref.passport_no_show eq 'D')}">
						<td class="formlabel">${ifn:cleanHtml(regPref.passport_no)}:</td>
						<td>
							<input type="text" class="field" name="passport_no" value="${patient.passport_no}">
								<span id="passport_no_star"  class="star">*</span>
						</td>
						<c:set var="maincolumns" value="${maincolumns+1}"/>
						<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
					</c:if>

					<c:if test="${not empty regPref.nationality && not empty regPref.nationality_show && 
										(regPref.nationality_show eq 'M' || regPref.nationality_show eq 'D') }">
						<td class="formlabel">${ifn:cleanHtml(regPref.nationality)}:</td>
						<td>
							<insta:selectdb name="nationality_id" id="nationality_id" table="country_master"
									class="field" style="width:140px;" dummyvalue="${dummyvalue}" value="${patient.nationality_id}"
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
							
					<c:if test="${not empty regPref.passport_validity && not empty regPref.passport_validity_show
									&& (regPref.passport_validity_show eq 'M' || regPref.passport_validity_show eq 'D')}">
						<td class="formlabel">${ifn:cleanHtml(regPref.passport_validity)}:</td>
						<td>
							<c:set var="dateTitle">
								<insta:ltext key="registration.patient.preregistration.details.passportvalidity"/>
							</c:set>
							<fmt:formatDate var="passportValidity" value="${patient.passport_validity}" pattern="dd-MM-yyyy"/>
							<insta:datewidget name="passport_validity" id="passport_validity"
										btnPos="left" title="${dateTitle}"
										value="${passportValidity}"/>
								<span id="passport_validity_star" class="star">*</span>
						</td>
						<c:set var="maincolumns" value="${maincolumns+1}"/>
						<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
					</c:if>

					<c:if test="${not empty regPref.visa_validity && not empty regPref.visa_validity_show
									&& (regPref.visa_validity_show eq 'M' || regPref.visa_validity_show eq 'D')}">
						<td class="formlabel">${ifn:cleanHtml(regPref.visa_validity)}:</td>
						<td>
						<c:set var="visaTitle">
								<insta:ltext key="registration.patient.preregistration.details.visavalidity"/>
							</c:set>
							<fmt:formatDate var="visaValidity" value="${patient.visa_validity}" pattern="dd-MM-yyyy"/>
							<insta:datewidget name="visa_validity" id="visa_validity"
										btnPos="left" title="${visaTitle}"
										value="${visaValidity}"/>
								<span id="visa_validity_star" class="star">*</span>
						</td>
						<c:set var="maincolumns" value="${maincolumns+1}"/>
						<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
					</c:if>

					<c:if test="${not empty regPref.passport_issue_country && not empty regPref.passport_issue_country_show
									&& (regPref.passport_issue_country_show eq 'M' || regPref.passport_issue_country_show eq 'D')}">
						<td class="formlabel">${ifn:cleanHtml(regPref.passport_issue_country)}:</td>
						<td>
							<insta:selectdb name="passport_issue_country" id="passport_issue_country" table="country_master"
								class="field" style="width:140px;" dummyvalue="${dummyvalue}" value="${patient.passport_issue_country}"
								size="1" valuecol="country_id" displaycol="country_name" usecache="true" filtercol="status,nationality" filtervalue="A,f" />
								<span id="passport_issue_country_star" class="star">*</span>
						</td>
						<c:set var="maincolumns" value="${maincolumns+1}"/>
						<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
					</c:if>

					<c:if test="${not empty regPref.family_id && not empty regPref.family_id_show
									&& (regPref.family_id_show eq 'M' || regPref.family_id_show eq 'D')}">
						<td class="formlabel">${ifn:cleanHtml(regPref.family_id)}:</td>
						<td>
							<input type="text" class="field" name="family_id" value="${patient.family_id}">
							<c:if test="${not empty regPref.family_id_validate &&
											(regPref.family_id_validate eq 'A' ||
												(screenId eq 'ip_registration' && regPref.family_id_validate eq 'I'))}">
								<span class="star">*</span>
							</c:if>
						</td>
						<c:set var="maincolumns" value="${maincolumns+1}"/>
						<c:if test="${(maincolumns % 3) == 0}"></tr><tr></c:if>
					</c:if>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.remarks"/>:</td>
					<td >
						<span style="padding: 0 0px 0px 0;">
							<input type="text" class="field" style="width:100px; padding:0 0 2px 2px"
							name="remarks" id="remarks" value="${patient.remarks}" maxlength="50">
						</span>
					</td>

					<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.emailid"/>:</td>
					<td >
						<span style="padding: 0 0px 0px 0;">
							<input name="email_id" type="text" class="field" id="textfield23"
							style="width:100px; padding:0 0 2px 2px" value="${patient.email_id}"/>
	             		</span>
									
						<span class="patient_email_star">*</span>
	                 </td>
				</tr>
				<!-- added hidden style as part of HMS-22855 fix, this dead code can be removed as part of HMS-23827 -->
				<tr>
					<c:if test="${preferences.modulesActivatedMap.mod_mobile eq 'Y'}">
						<td class="formlabel"><insta:ltext key="registration.patient.preregistration.details.mobileaccess"/>:</td>
						<td >
							<span style="padding: 0 0px 0px 0;">
								<insta:radio radioText="${portalpataccess}" radioIds="mobilePatYes,mobilePatNo" radioValues="Y,N"
									name="mobilePatAccess" value="${not empty patient.mr_no ? 'N' : mobilePatAccess}" />
							</span>
						</td>
					</c:if>
					<td class="formlabel"><insta:ltext key="ui.label.preferred.language"/>:</td>
					<td>
						<select id="preferredLanguage" name="preferredLanguage" class="dropdown" style="width:137px;"> 
						<c:forEach items="${preferredLanguages}" var="lang">
							<option value="${lang.lang_code}" ${lang.lang_code.equals(currentLanguagePreference)?"selected":""} }>${lang.language}</option>										
						</c:forEach>
						</select>
					</td>
					<c:if test="${preferences.modulesActivatedMap.mod_messaging eq 'Y'}">
					<td class="formlabel"><insta:ltext key="ui.label.mode.of.communication"/>:</td>
					<td>
					<input type="checkbox" id="modeOfCommSms" name="modeOfCommSms" ${communicationMode eq 'S' || communicationMode eq 'B' ? 'checked' : ''}/><insta:ltext key="ui.label.sms.allcaps"/>
					<input type="checkbox" id="modeOfCommEmail" name="modeOfCommEmail" ${communicationMode eq 'E' || communicationMode eq 'B' ? 'checked' : ''}/><insta:ltext key="ui.label.email.allcaps"/>
					</td>
					</c:if>
				<tr>
					<c:choose>
						<c:when test="${not empty regPref.government_identifier_type_label}">
							<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_type_label)}</td>
							<td>
								 <select name="identifier_id" id="identifier_id" class="dropdown" style="width:137px;" onchange="setGovtPattern();">
										<option value="">--Select--</option>
										<c:forEach items="${govtIdentifierTypes}" var="govtIdTypes">
											<option value="${govtIdTypes.map.identifier_id}"
												${not empty patient.identifier_id ? (govtIdTypes.map.identifier_id eq patient.identifier_id ? 'selected' : '') : (govtIdTypes.map.default_option eq 'Y' ? 'selected' : '')}>
												${govtIdTypes.map.remarks}</option>
										</c:forEach>
								</select> 
								<c:if test="${patientIdentification=='G'}">
									<span class="star">*</span>
								</c:if>
							</td>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${not empty regPref.government_identifier_label}">
							<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_label)}</td>
							<td>
								<input type="text" name="government_identifier" id="government_identifier"
								maxlength="200" value="${patient.government_identifier}" disabled />
								<span id="govtidstar" class="star">*</span>
							</td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
				</tr>
				<tr>
					<c:choose>
						<c:when test="${not empty regPref.government_identifier_type_label}">
							<td class="formlabel">Other Identification Document Types:</td>
							<td>
								 <select name="other_identification_doc_id" id="other_identification_doc_id" class="dropdown" style="width:137px;" onchange="onChangeOfOtherId();">
										<option value="">--Select--</option>
										<c:forEach items="${otherIdentifierTypes}" var="otherIdTypes">
											<option value="${otherIdTypes.map.other_identification_doc_id}"
												${not empty patient.other_identification_doc_id ? (otherIdTypes.map.other_identification_doc_id eq patient.other_identification_doc_id ? 'selected' : '') :  ''}>
												${otherIdTypes.map.other_identification_doc_name}</option>
										</c:forEach>
								</select> 
							</td>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${not empty regPref.government_identifier_label}">
							<td class="formlabel"id="other_identification_doc_value_label" >Other Identifier Document Value:</td>
							<td>
								<input type="text" name="other_identification_doc_value" id="other_identification_doc_value"
								maxlength="200" value="${patient.other_identification_doc_value}" disabled />
							</td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

<table style="margin-top: 1em" >
	<tr>
		<td>
			<c:url value="/pages/registration/GenerateRegistrationCard.do" var="printRegCard">
	  	    	<c:param name="method" value="execute" />
	  	        <c:param name="mrno" value="${patient.mr_no}"/>
	  	        <c:if test="${not empty patId}">
	  	        	<c:param name="patid" value="${patId}"/>
	  	        </c:if>
	  	        <c:if test="${empty patId}">
	  	        	<c:param name="patid" value="No"/>
	  	        </c:if>
	  	     </c:url>

			   <button type="button" name="register" class="button" accesskey="R" onclick="updateRecord()">
			   <label><u><b><insta:ltext key="registration.patient.preregistration.details.r"/></b></u><insta:ltext key="registration.patient.preregistration.details.egister"/></label>
			   </button>
 	  	     | <a href="${cpath}/Registration/GeneralRegistration.do?_method=show&mrno=${ifn:cleanURL(patient.mr_no)}&visitId=${ifn:cleanURL(visitId)}"><insta:ltext key="registration.patient.preregistration.details.reset"/></a>
			 <c:if test="${not empty patient.mr_no && empty visitId}">
	  	     | <a href="${printRegCard}" target="_blank"><insta:ltext key="registration.patient.preregistration.details.printreg.card"/></a>
 	  	     </c:if>
 	  	     <c:if test="${not empty visitId}">
 	  	     | <a href="${cpath}/pages/registration/editvisitdetails.do?_method=getPatientVisitDetails&patient_id=${ifn:cleanURL(visitId)}&ps_status=active"><insta:ltext key="registration.patient.preregistration.details.editvisitdetails"/></a>
 	  	     </c:if>
 	  	     <c:if test="${not empty patient.mr_no}">
 	  	     	<insta:screenlink screenId="registration_audit_log" label="${patientlogTitle}" addPipe="true" extraParam="?_method=getAuditLogDetails&mr_no=${patient.mr_no}&al_table=patient_details_audit_log"/>
 	  	     </c:if>
		</td>
	</tr>
</table>	

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


</form>
<insta:link type="js" file="select2.min.js"/>
<insta:link type="js" file="phoneNumberUtil.js"/>
<insta:link type="js" file="registration/registrationPhoneNumberCommon.js"/>
<script type="text/javascript">
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:true});
</script>

</body>
</html>
