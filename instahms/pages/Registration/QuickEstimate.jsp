<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="mod_adv_packages" value="${preferences.modulesActivatedMap.mod_adv_packages}"/>

<html>
<head>
	<title><insta:ltext key="registration.quickestimate.list.quickestimate"/> ${estimate.map.estimate_no} - <insta:ltext key="registration.quickestimate.list.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="css" file="select2.min.css"/>
	<insta:link type="css" file="select2Override.css"/>
	<insta:link type="js" file="phoneNumberUtil.js"/>
	<insta:link type="script" file="registration/quickEstimate.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="orderdialog.js" />
	<insta:link type="script" file="ordertable.js"/>
	<insta:link type="css" file="widgets.css"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<jsp:useBean id="currentDate" class="java.util.Date"/>

	<c:set var="orderable" value=""/>

	<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>
	<c:set var="orderItemsUrl" value="${cpath}/master/orderItems.do?method=getOrderableItems"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&filter=&orderable=${orderable}&directBilling=&operationOrderApplicable=${genPrefs.operationApplicableFor}"/>
	<c:set var="scriptOrderItemsUrl" value="${orderItemsUrl}"/>
	<c:choose>
		<c:when test="${!empty estimate}">
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&orgId=${estimate.map.rate_plan}&visitType=${empty estimate.map.visit_type ? 'i' : estimate.map.visit_type}"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&bedType=${empty estimate.map.bed_type ? 'i' : estimate.map.bed_type}"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&tpaId=${empty estimate.map.tpa_id ? '' : estimate.map.tpa_id}"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&planId=${empty estimate.map.plan_id ? '' : estimate.map.plan_id}"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&genderApplicability=${empty estimate.map.gender ? '*' : estimate.map.gender}"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&age=${empty estimate.map.age ? null : estimate.map.age}"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&ageIn=${empty estimate.map.age_in ? null : estimate.map.age_in}"/>
		</c:when>
		<c:otherwise>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&orgId=ORG0001&visitType=i"/>
			<c:set var="orderItemsUrl" value="${orderItemsUrl}&bedType=GENERAL"/>
		</c:otherwise>
	</c:choose>
	<c:set var="corpInsurancehid">
		<c:choose>
			<c:when test="${corpInsurance eq 'Y'}">display:none</c:when>
			<c:otherwise></c:otherwise>
		</c:choose>
	</c:set>
	<script src="${orderItemsUrl}"></script>

	<style type="text/css">
		table.infotable td.forminfo { width: 50px; text-align: right }
		table.infotable td.formlabel { width: 95px; text-align: right }
		#table3 td {
			padding: 0px 10px 10px 10px;
			font-weight: bold;
		}
		#disabler  tr {
		  background-color: #8888ff;
		}

		table.formtable td.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}

		table.formtable tr.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;dropdown
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}
	<style>
		#table3 td {
			padding: 0px 10px 10px 10px;
			font-weight: bold;
		}
		#disabler  tr {
		  background-color: #8888ff;
		}

		table.formtable td.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}

		table.formtable tr.disabler{
			  background-color: #CCCCCC;
			  opacity: 0.5;
			  -webkit-user-select: none;dropdown
			  -khtml-user-select: none;
			  -moz-user-select: none;
			  -o-user-select: none;
			  user-select: none;
		}

	</style>
	<script type="text/javascript">
		var defaultCountryCode ='+${defaultCountryCode}';
		var estimateDate = '<fmt:formatDate value="${not empty estimate?estimate.map.estimate_date:currentDate}" pattern="dd-MM-yyyy"/>';
		var estimateTime = '<fmt:formatDate value="${not empty estimate?estimate.map.estimate_date:currentDate}" pattern="HH:mm"/>';

		var orgIdVal = 'ORG0001';
		var bedType = <insta:jsString value="${not empty estimate?estimate.map.bed_type:bed_type}"/>;
		var showChargesAllRatePlan = 'A'; // hardcoded to 'A', because, in this screen, people want to see estimates of item
		gPrescDocRequired = 'N';
		mealTimingsRequired = true;
		equipTimingsRequired = true
		gPrescDocRequired = 'N';
		var fixedOtCharges = '${genPrefs.fixedOtCharges}';
		var serviceGroupsJSON = ${serviceGroupsJSON};
		var servicesSubGroupsJSON = ${servicesSubGroupsJSON};
		var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
		var mrno = null;
		var anaeTypes = ${anaeTypesJSON};
		var allOrdersJSON = null;
		var estimateNo = '${empty estimate.map.estimate_no ? '' : estimate.map.estimate_no}';
		var gPlanId = '${empty estimate.map.plan_id ? '' : estimate.map.plan_id}';
		var gVisitType= '${empty estimate.map.visit_type ? '' : estimate.map.visit_type}';
		var gOrgId = '${empty estimate.map.rate_plan ? '' : estimate.map.rate_plan}';
		var gTpaId = '${empty estimate.map.tpa_id ? '' : estimate.map.tpa_id}';
		var mod_adv_packages = '${mod_adv_packages}';
		var cpath = "${cpath}";
		var bedCharges = <%= request.getAttribute("bedChargesJson") %>;
		var bedTypesList = filterList(bedCharges, 'organization', orgIdVal);
		var orderItemsUrl = "${scriptOrderItemsUrl}";
		var centerId = ${centerId};
		var tpanames = ${tpanames};
		var policynames = ${policyNames};
		var insuCatNames = ${insuCategoryNames};
		var regPref = ${regPrefJSON};
		var modAdvInsurance ='${preferences.modulesActivatedMap['mod_adv_ins']}';
		var allDoctorConsultationTypes = ${allDoctorConsultationTypes} ;
		var isModAdvanceIns = !empty(modAdvInsurance) && modAdvInsurance == 'Y';
		var insuCompanyDetails = <%= request.getAttribute("insuCompanyDetails") %>;
		var categoryJSON=${categoryWiseRateplans};
		var orgNamesJSON = <%= request.getAttribute("orgNameJSON") %>;
		var centerWiseOrgNameJSON = <%= request.getAttribute("centerWiseOrgNameJSON") %>;
		var companyTpaList = <%= request.getAttribute("insCompTpaList") %>;
		var contextPath = "${cpath}";
		var estiamteNo = '${not empty estimate?estimate.map.estimate_no : ''}';
		var multiPlanExists = false;
		var salutationJSON = '${salutationQueryJson}';
		var claimServiceTaxPer = '${genPrefs.serviceTaxOnClaimAmount}';
		var serviceChargePer = '${genPrefs.serviceChargePercent}';
		var jPolicyNameList = <%= request.getAttribute("policyCharges") %>;
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var gMrNo = "${ifn:cleanJavaScript(param.mr_no)}";
		var corpInsuranceCheck = '${corpInsurance}';
		var gGender= '${empty estimate.map.gender ? '*' : estimate.map.gender}';
		var gAge = '${empty estimate.map.age ? null : estimate.map.age}';
		var gAgeIn = '${empty estimate.map.age_in ? null : estimate.map.age_in}';
	</script>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="registration.quickestimate"/>
</head>


<body onload="init(),ajaxForPrintUrls();" class="yui-skin-sam">

<form name="billNoForm" action="QuickEstimate.do">
	<input type="hidden" name="buttonAction" value="save">
	<input type="hidden" name="_method" value="getQuickEstimateScreen">
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="registration.quickestimate.list.quickestimate"/></h1></td>
		</tr>
	</table>
</form>

<div><insta:feedback-panel/></div>
<c:if test="${empty estimate}"><span class="resultMessage"><b>${resultMsg}</b></span></c:if>

<form name="mainform" method="POST" action="QuickEstimate.do" autocomplete="off">
<input type="hidden" name="_method" value="saveQuickEstimate">
<input type="hidden" name="estimate_no" value="${estimate.map.estimate_no}">
<input type="hidden" name="nationality_id" id="nationality_id" value="${estimate.map.nationality_id}">
<input type="hidden" name="screenId" value="${screenId}">
<input type="hidden" name="isPrint" value"">
<input type="hidden" value="${not empty estimate.map.plan_name ? 'Y' : 'N'}" name="isTpaEstimate" id="isTpaEstimate"/>
<c:choose>
<c:when test="${not empty estimate}">
	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.quickestimatelist.details.patientdetails"/></legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.estimateno"/>:</td>
			<td class="forminfo">${estimate.map.estimate_no}</td>
			<td class="formlabel"><insta:ltext key="ui.label.mrno"/>:</td>
			<td class="forminfo">${estimate.map.mr_no}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.personname"/>:</td>
			<td class="forminfo">${estimate.map.salutation} ${estimate.map.person_name}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.age"/>:</td>
			<td class="forminfo">${estimate.map.age} <c:if test="${not empty estimate.map.age}">${estimate.map.age_in}</c:if></td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.gender"/>:</td>
			<c:choose>
				<c:when test="${estimate.map.gender == 'M'}">
					<c:set var="patGender" value="Male"/>
				</c:when>
				<c:when test="${estimate.map.gender == 'F'}">
					<c:set var="patGender" value="Female"/>
				</c:when>
				<c:when test="${estimate.map.gender == 'C'}">
					<c:set var="patGender" value="Couple"/>
				</c:when>
				<c:when test="${estimate.map.gender == 'O'}">
					<c:set var="patGender" value="Others"/>
				</c:when>
				<c:otherwise>
					<c:set var="patGender" value=""/>
				</c:otherwise>
			</c:choose>
			<td class="forminfo">${patGender}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.mobile"/>:</td>
			<td class="forminfo">${estimate.map.mobile_no}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.visittype"/>:</td>
			<td class="forminfo">${estimate.map.visit_type == 'i' ? 'IP Visit' : 'OP Visit'}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.rateplan"/>:</td>
			<td class="forminfo">${estimate.map.org_name}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.bedtype"/>:</td>
			<td class="forminfo">${estimate.map.bed_type}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.patientcategory"/>:</td>
			<td class="forminfo">${estimate.map.category_name}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.insuranceco"/>:</td>
			<td class="forminfo">${estimate.map.insurance_co_name}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.tpa"/>:</td>
			<td class="forminfo">${estimate.map.tpa_name}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.plantype"/>:</td>
			<td class="forminfo">${estimate.map.plan_type_name}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.planname"/>:</td>
			<td class="forminfo">${estimate.map.plan_name}</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.remarks"/>:</td>
			<td class="forminfo">
				<input type="text" name="estimate_remarks" id="estimate_remarks" value='<c:out value="${estimate.map.remarks}"/>' maxlength="200" />
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="ui.label.nationality"/>:</td>
			<td>
				<insta:selectdb name="nationality_id" id="enationality_id" table="country_master"
						class="field" style="width:140px;" dummyvalue="${dummyvalue}"
						size="1" valuecol="country_id" displaycol="country_name" usecache="true" value="${estimate.map.nationality_id}" onchange="setNationalityValue()"/>
			</td>
		</tr>
	</table>
</fieldset>
</c:when>
<c:otherwise>
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.quickestimatelist.details.patientdetails"/></legend>
		<table id="patientinfo" class="formtable" width="100%">
			<tr>
				<td style="width:500px;">
					<fieldset class="fieldSetBorder" style="width:475px;height:220px;">
						<legend class="fieldSetLabel"><input type="radio" name="patientdetails" id="registered_patient" value="registered" onclick="handlePatientSelect(this)">
							<insta:ltext key="registration.quickestimatelist.details.registeredpatient"/>
						</legend>
						<table class="formtable" width="100%">
						<tr id="registeredpatientdetails">
							<td style="padding-top:0px;padding-bottom:0px;" >
								<table style="width:100%;padding-left:20px;height:170px;">
									<tr>
										<td class="formlabel"><u><b><insta:ltext key="registration.quickestimatelist.details.m"/></b></u><insta:ltext key="registration.quickestimatelist.details.rno"/>:</td>
										<td style="padding: 0 5px 10 5px">
											<div id="mrnoAutocomplete" style="width: 100px; padding-bottom: 8px">
												<input type="text" id="mrno" name="mrno" class="field" style="width:135px; display: inline;"/>
												<div id="mrnoAcContainer" style="width: 38em;"></div>
											</div>
										</td>
										<td>
											<input type="checkbox" name="ps_status" value="active" onchange="reInitializeAc();"
													${param.ps_status == 'active' ? '' : 'checked'}><insta:ltext key="salesissues.sales.details.activeonly"/>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.visitid"/>:</td>
										<td>
											<div>
												<label id="visitId"></label>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.name"/>:</td>
										<td>
											<div style="float:left">
												<label id="fullname"></label>
												<input type="hidden" name="i_salutation_id" value=""/>
												<input type="hidden" name="i_patient_full_name" value=""/>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.age"/>:</td>
										<td>
											<div style="float:left">
												<label id="age"></label>
												<input type="hidden" name="i_patient_age" value=""/>
												<input type="hidden" name="i_patient_age_in" value=""/>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.gender"/>:</td>
										<td>
											<div style="float:left">
												<label id="gender"></label>
												<input type="hidden" name="i_patient_gender" value=""/>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.mobile"/>:</td>
										<td>
											<div style="float:left">
												<label id="mobile"></label>
												<input type="hidden" name="i_patient_mobile" value=""/>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="ui.label.nationality"/>:</td>
										<td>
											<div style="float:left">
												<label id="nationality"></label>
												<input type="hidden" id="oldnationality_id" name="i_patient_nationality" value=""/>
												<insta:selectdb name="nationality_id" id="pnationality_id" table="country_master"
													class="field" style="width:140px;" dummyvalue="${dummyvalue}"
													size="1" valuecol="country_id" displaycol="country_name" usecache="true" onchange="setNationalityValue()"/>
											</div>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</fieldset>
				<td valign="top">
					<fieldset class="fieldSetBorder" style="height:220px;">
						<legend class="fieldSetLabel"><input type="radio" name="patientdetails" id="new_patient" value="new" onclick="handlePatientSelect(this)">
							<insta:ltext key="registration.quickestimatelist.details.newpatient"/>
						</legend>
						<table class="formtable" width="100%">
						<tr id="newpatientdetails">
							<td style="padding-top:0px;paddingprimary_sponsor-bottom:0px;" >
								<table style="width:100%;padding-left:50px;height:170px;">
									<tr>
									<c:set var="patTitle">
										<insta:ltext key="registration.patient.quickestimateprerequisite.show.title"/>
									</c:set>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.name"/>:</td>
										<td style="white-space: nowrap; width: 350px" colspan="3">
											<insta:selectdb id="salutation" name="salutation" value="${patTitle}" table="salutation_master"
															valuecol="salutation_id"  displaycol="salutation" usecache="true"
															class="dropdown" onchange="salutationChange()" dummyvalue="${patTitle}"  style="width: 60px;"/>
											<input type="text" name="person_name"  id="person_name" class="field"
													value="<insta:ltext key="registration.patient.quickestimateprerequisite.firstname"/>" maxlength="50"
													onblur="capWords(person_name);if (this.value == '') { this.value = '<insta:ltext key="registration.patient.quickestimateprerequisite.firstname"/>'}"
													onFocus="if (this.value == '<insta:ltext key="registration.patient.quickestimateprerequisite.firstname"/>') { this.value = ''}"  style="width: 120px;"/>
											<input type="text" name="middle_name" id="middle_name" size="15"  class="field"
													maxlength="50" value="<insta:ltext key="registration.patient.quickestimateprerequisite.middlename"/>"
													onblur="capWords(middle_name);if (this.value == '') { this.value = '<insta:ltext key="registration.patient.quickestimateprerequisite.middlename"/>'}"
													onFocus="if (this.value == '<insta:ltext key="registration.patient.quickestimateprerequisite.middlename"/>') { this.value = ''}"  style="width: 120px;"/>
											<input type="text" name="last_name" id="last_name" size="15"  class="field"
													maxlength="50" value="<insta:ltext key="registration.patient.quickestimateprerequisite.lastname"/>"
													onblur="capWords(last_name);if (this.value == '') { this.value = '<insta:ltext key="registration.patient.quickestimateprerequisite.lastname"/>'}"
													onFocus="if (this.value == '<insta:ltext key="registration.patient.quickestimateprerequisite.lastname"/>') { this.value = ''}"  style="width: 120px;"/>
											<input type="hidden" name="person_full_name" id="person_full_name" value=""/>
											<span class="star">*</span>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.age"/>:</td>
										<td>
											<input type="text" class="field" name="patient_age" id="patient_age" size="2" maxlength="2" style="width:50px;" onkeypress="return enterNumOnly(event);">
											<select name="ageIn" id="ageIn" class="dropdown" style="width:70px;" >
												<option value="Y"><insta:ltext key="registration.patient.commonselectbox.ageIn.years"/></option>
												<option value="M"><insta:ltext key="registration.patient.commonselectbox.ageIn.months"/></option>
												<option value="D"><insta:ltext key="registration.patient.commonselectbox.ageIn.days"/></option>
											</select>
										</td>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.gender"/>:</td>
										<td style="width:125px;">
											<select class="dropdown"  name="patient_gender" id="patient_gender" style="width:120px;">
												<option value="N"><insta:ltext key="common.selectbox.defaultText"/></option>
												<option value="M"><insta:ltext key="registration.quickestimatelist.details.male"/></option>
												<option value="F"><insta:ltext key="registration.quickestimatelist.details.female"/></option>
												<option value="C"><insta:ltext key="registration.quickestimatelist.details.couple"/></option>
												<option value="O"><insta:ltext key="registration.quickestimatelist.details.others"/></option>
											</select>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.mobile"/>:											
										</td>
										<td colspan=2>
											<div style="margin-top:12px">
												<div>
													<input type="hidden" id="mobile_no" name="mobile_no"/>
													<input type="hidden" id="mobile_no_valid" value="Y"/>
													<select id="mobile_no_country_code" class="dropdown" style="width:76px" name="mobile_no_country_code">
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
													
													 <input type="text" class="field" id="mobile_no_national" maxlength ="15" onkeypress="return enterNumOnlyzeroToNine(event)"
															 style="width:9.6em;padding-top:1px" />
														<img class="imgHelpText" id="mobile_no_help" src="${cpath}/images/help.png"/>
												</div>
												<div>
													<span style="visibility:hidden;padding-left:10px;color:#f00" id="mobile_no_error"></span>	
												</div>
								
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="ui.label.nationality"/>:</td>
										<td>
											<insta:selectdb name="nationality_id" id="newnationality_id" table="country_master"
													class="field" style="width:140px;" dummyvalue="${dummyvalue}"
													size="1" valuecol="country_id" displaycol="country_name" usecache="true" onchange="setNationalityValue()"/>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</fieldset>
			</td>
		</tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"><insta:ltext key="registration.quickestimatelist.details.billingconsiderations"/></legend>
	<table class="formtable" >
		<tr>
			<td style="width:200px;">
			<c:set var="isModulesActivated" value="${(preferences.modulesActivatedMap.mod_basic eq 'Y' && not empty preferences.modulesActivatedMap.mod_basic) ||
													 (preferences.modulesActivatedMap.mod_insurance eq 'Y' && not empty preferences.modulesActivatedMap.mod_insurance) ||
													 (preferences.modulesActivatedMap.mod_adv_ins eq 'Y' && not empty preferences.modulesActivatedMap.mod_adv_ins)}"/>
				<fieldset class="fieldSetBorder" style="width:475px;height:300px;">
					<legend class="fieldSetLabel"><input type="radio" name="primary_sponsor" id="primary_sponsor" value="I" onclick="handleInsuranceSelect(this);" ${!isModulesActivated ? 'disabled' : ''}>
						<insta:ltext key="registration.quickestimatelist.details.insurance"/>
					</legend>
					<table class="formtable">
						<tr id="registeredpatient">
							<td style="padding-top:0px;padding-bottom:0px;" >
								<table style="width:100%;padding-left:55px;height:100%">
	  								<tr>
										<td>
											<input type="radio" name="insurancevisittype" id="insurance_ipvisit" value="i" onclick="onChangeIpVisitType(this);">
											<insta:ltext key="registration.quickestimatelist.details.ipvisit"/>
										</td>
										<td>
											<input type="radio" name="insurancevisittype" id="insurance_opvisit" value="o" onclick="onChangeIpVisitType(this);">
											<insta:ltext key="registration.quickestimatelist.details.opvisit"/>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.patientcategory"/>:</td>
										<td style="width:800px;">
											<select id="patient_category_id" name="patient_category_id" size="1"
													class="dropdown" onchange="onChangeCategory()">
												<option value="">${dummyvalue}</option>
											</select>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.patient.payment.insuranceCo"/>:</td>
										<td>
											<insta:selectdb name="primary_insurance_co" id="primary_insurance_co" onchange="onLoadTpaList('P'), insuPrimaryViewDoc(this)"
			 												value="" table="insurance_company_master" style="width:137px;" dummyvalue="-- Select --"
			 												valuecol="insurance_co_id" displaycol="insurance_co_name" orderby="insurance_co_name"/>
			 								<div id="viewinsuranceprimaryruledocs"></div>
										</td>
									</tr>
									<tr>
										<c:choose>
											<c:when test="${corpInsurance eq 'Y'}">
												<td class="formlabel"><insta:ltext key="registration.patient.payment.sponser"/>:</td>
											</c:when>
											<c:otherwise>
												<td class="formlabel"><insta:ltext key="registration.patient.payment.tpa"/>:</td>
											</c:otherwise>
										</c:choose>
										<td>
											<select id="primary_sponsor_id" name="primary_sponsor_id" onchange="onTpaChange('P')" class="dropdown">
												<option selected="selected" value=""><insta:ltext key="common.selectbox.defaultText"/></option>
											</select>
										</td>
									</tr>
									<tr style="${corpInsurancehid}">
										<td class="formlabel"><insta:ltext key="registration.patient.payment.networkPlanType"/>:</td>
										<td>
											<select id="primary_plan_type" name="primary_plan_type" onchange="onInsuCatChange('P')" class="dropdown">
												<option selected="selected" value=""><insta:ltext key="common.selectbox.defaultText"/></option>
											</select>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.patient.payment.planName"/>:</td>
										<td>
											<select id="primary_plan_id" name="primary_plan_id" onchange="onPolicyChange('P');" class="dropdown">
												<option selected="selected" value=""><insta:ltext key="common.selectbox.defaultText"/>-</option>
											</select>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><insta:ltext key="registration.patient.payment.plan.details"/>:</td>
										<td class="forminfo">
											<div title="" id="primary_plan_div">
												<button id="pd_primary_planButton" title="<insta:ltext key='registration.patient.additional.plan.info.dot.dot.dot'/>" style="cursor:pointer;"
														onclick="javascript:initPatientRegPlanDetailsDialog('pd_primary_planButton');showPatientRegPlanDetailsDialog('primary');" type="button"><insta:ltext key="registration.patient.button.dot.dot"/></button>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel"><label for="patientType1"><insta:ltext key="registration.quickestimatelist.details.rateplan"/>:</label></td>
										<td>
											<select id="organization" name="organization" size="1" class="dropdown" onchange="ratePlanChange()"></select>
											<span class="star">*</span>
										</td>
									</tr>
									<tr id="displayBedTypeIp" style="display:none">
										<td class="formlabel"><insta:ltext key="registration.quickestimatelist.details.bedtype"/>:</td>
										<td>
											<select name="bed_type_ip" id="bed_type_ip" onchange="onBedTypeChange(this)" class="dropdown"/>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</fieldset>
			</td>
			<td valign="top">
				<fieldset class="fieldSetBorder" style="width:538px; height:300px;">
				<legend class="fieldSetLabel"><input type="radio" name="primary_sponsor" id="direct_estimate" value="direstimate" onclick="handleInsuranceSelect(this);">
					<insta:ltext key="registration.quickestimatelist.details.directestimate"/>
				</legend>
				<table class="formtable" width="100%">
					<tr id="directestimate">
						<td style="padding-top:0px;padding-bottom:0px;">
							<table style="width:65%; height: 100%">
								<tr>
									<td>&nbsp;</td>
									<td align="left">
										<input type="radio" name="directestimatevisittype" id="directestimate_ipvisit" value="i" onclick="onChangeOpVisitType(this);">
										<insta:ltext key="registration.quickestimatelist.details.ipvisit"/>
									</td>
									<td>
										<input type="radio" name="directestimatevisittype" id="directestimate_opvisit" value="o" onclick="onChangeOpVisitType(this);">
										<insta:ltext key="registration.quickestimatelist.details.opvisit"/>
									</td>
								</tr>
								<tr>
									<td class="formlabel" style="white-space: nowrap; width: 350px"><insta:ltext key="registration.quickestimatelist.details.rateplan"/>:</label></td>
									<td style="white-space: nowrap;" colspan="3">
										<select id="rate_plan_op" name="rate_plan_op" size="1" class="dropdown" onchange="onChangeDirectRatePlan(this)" style="width: 250px;"></select>
										<span class="star">*</span>
									</td>
								</tr>
								<tr id="displayBedTypeOp" style="display:none">
									<td class="formlabel" style="white-space: nowrap; width: 350px"><insta:ltext key="registration.quickestimatelist.details.bedtype"/>:</td>
									<td style="white-space: nowrap;" colspan="3">
										<select name="bed_type_op" id="bed_type_op" onchange="onBedTypeChange(this)" class="dropdown" style="width: 250px;"/>
									</td>
								</tr>
								<tr><td>&nbsp;</td></tr>
								<tr><td>&nbsp;</td></tr>
								<tr><td>&nbsp;</td></tr>
								<tr><td>&nbsp;</td></tr>
								<tr><td>&nbsp;</td></tr>
								<tr><td>&nbsp;</td></tr>
							</table>
						</td>
					</tr>
				</table>
			</fieldset>
		</td>
	</tr>
</table>
</fieldset>


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
</c:otherwise>
</c:choose>
<div class="detailList" style="margin: 10px 0px 5px 0px;">
	<fieldset>
		<legend class="fieldSetLabel"><insta:ltext key="registration.quickestimate.section.estimated.item.fielset.label"/></legend>
		<div style="height:10px;">&nbsp;</div>
		<table class="detailList" cellspacing="0" cellpadding="0" id="chargesTable" border="0" width="100%">
			<tr bgcolor="#8FBC8F" id="chRow0">
				<th><insta:ltext key="registration.quickestimate.list.head"/></th>
				<th><insta:ltext key="registration.quickestimate.list.description"/></th>
				<th><insta:ltext key="registration.quickestimate.list.details.details"/></th>
				<th><insta:ltext key="registration.quickestimate.list.remarks"/></th>
				<th class="number"><insta:ltext key="registration.quickestimate.list.rate"/></th>
				<th class="number"><insta:ltext key="registration.quickestimate.list.qty"/></th>
				<th></th>
				<th class="number"><insta:ltext key="registration.quickestimate.list.details.disc"/></th>
				<th class="number"><insta:ltext key="registration.quickestimate.list.details.amt"/></th>
				<th class="number"><insta:ltext key="billing.patientbill.details.tax"/></th>
				<th class="number" title="Pri Sponsor/Claim Amount"><insta:ltext key="registration.quickestimate.list.details.sponsor.amt"/></th>
				<th class="number" title="${sponseramt}"><insta:ltext key="billing.patientbill.details.sponsortax"/></th>
				<th class="number" title="Patient Amount"><insta:ltext key="registration.quickestimate.list.details.patient.amt"/></th>
				<th class="number"><insta:ltext key="billing.patientbill.details.patient.tax"/></th>
				<th style="width:20px"></th>		<%-- trash icon --%>
				<th style="width:20px"></th>		<%-- edit icon --%>
			</tr>

			<c:set var="totalClaimAmount" value="0"/>
			<c:set var="numCharges" value="${fn:length(charges)}"/>

			<%-- we add one hidden row with a null charge for use as a template to clone from --%>
			<c:forEach begin="1" end="${numCharges+1}" var="i" varStatus="loop">
				<c:set var="charge" value="${charges[i-1]}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${charge.status == 'X'}"><insta:ltext key="registration.quickestimate.list.red"/></c:when>
						<c:otherwise><insta:ltext key="registration.quickestimate.list.empty"/></c:otherwise>
					</c:choose>
				</c:set>

				<c:if test="${empty charge}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>

				<tr class="${charge.status=='X' ? 'delete':''}" ${style}>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<label title="${charge.chargehead_name}">${charge.chargehead_name}</label>
						<input type="hidden" name="chargeHeadName" value='${charge.chargehead_name}'>
						<input type="hidden" name="chargeGroupId" value='${charge.charge_group}'>
						<input type="hidden" name="itemType" value=''>
						<input type="hidden" name="insuranceCategoryId" value=''>
						<input type="hidden" name="consultationTypeId" value=''>
						<input type="hidden" name="opId" value=''>
						<input type="hidden" name="oriTax" value='${charge.tax_amt}'>
						<input type="hidden" name="chargeHeadId" value='${charge.charge_head}'>
						<input type="hidden" name="chargeId" value='${charge.charge_id}' >
						<input type="hidden" name="chargeRef" value='${charge.charge_ref}' >
						<input type="hidden" name="departmentId" value='${charge.act_department_id}'>
						<input type="hidden" name="hasActivity" value='${charge.hasactivity}'>
						<input type="hidden" name="edited" value='false'>
						<input type="hidden" name="taxSubGroupIds" value='' >
						<input type="hidden" name="oriTaxSubGroupIds" value='' >
						<input type="hidden" name="taxAmounts" value='' >
						<input type="hidden" name="oriTaxAmounts" value='' >
						<input type="hidden" name="taxRates" value='' >
						<input type="hidden" name="oriTaxRates" value='' >
						<input type="hidden" name="taxesCnt" value='' >
						<input type="hidden" name="new" value='N'>
						<c:choose>
							<c:when test="${!empty estimate.map.plan_id}">
								<input type="hidden" name="insClaimTaxable" value="${charge.claim_service_tax_applicable}"/>
								<input type="hidden" name="serviceChrgApplicable" value='${charge.service_charge_applicable}'/>
							</c:when>
							<c:otherwise>
								<input type="hidden" name="insClaimTaxable" value=""/>
								<input type="hidden" name="serviceChrgApplicable" value=""/>
							</c:otherwise>
						</c:choose>
					</td>

					<td style="max-width: 15em">
						<insta:truncLabel value="${charge.act_description}" length="25"/>
						<input type="hidden" name="description"
							value="${fn:escapeXml(charge.act_description)}" >
						<input type="hidden" name="descriptionId"
							value="${charge.act_description_id}" >
					</td>

					<td>
						<label title="${charge.act_remarks}">${fn:substring(charge.act_remarks,0,16)}${not empty charge.act_remarks && fn:length(charge.act_remarks) > 16 ? '....' : ''}</label>
						<input type="hidden" name="act_remarks" value="${charge.act_remarks}"/>
					</td>

					<td>
						<label title="${charge.remarks}">${fn:substring(charge.remarks,0,16)}${not empty charge.remarks && fn:length(charge.remarks) > 16 ? '....' : ''}</label>
						<input type="hidden" name="remarks" value="${charge.remarks}"/>
					</td>

					<td class="number">
						<label>${charge.act_rate}</label>
						<input type="hidden" name="rate" value="${charge.act_rate}"/>
						<input type="hidden" name="originalRate" value='${charge.orig_rate}' />
					</td>

					<td class="number">
						<label>${charge.act_quantity}</label>
						<input type="hidden" name="qty" value="${charge.act_quantity}"/>
					</td>

					<td>
						<label>${charge.act_unit}</label>
						<input type="hidden" name="units" value="${charge.act_unit}"/>
					</td>
					<td class="number">
						<label>${charge.discount}</label>
						<input type="hidden" name="discount" value="${charge.discount}" />
					</td>
					<td class="number">
						<label>${charge.amount}</label>
						<input type="hidden" name="amt" value='${charge.amount}' />
					</td>

					<td class="number">
						<label>${charge.tax_amt}</label>
						<input type="hidden" name="tax_amt" value='${charge.tax_amt}'/>
					</td>

					<td class="number">
						<label>${charge.sponsor_amt}</label>
						<input type="hidden" name="sponsor_amt" value="${charge.sponsor_amt}" />
					</td>

					<td class="number">
						<label>${charge.sponsor_tax}</label>
						<input type="hidden" name="sponsor_tax" value='${charge.sponsor_tax}' />
					</td>

					<td class="number">
						<label>${charge.patient_amt}</label>
						<input type="hidden" name="patient_amt" value="${charge.patient_amt}"/>
					</td>

					<td class="number">
						<label>${charge.patient_tax}</label>
						<input type="hidden" name="patient_tax" value="${charge.patient_tax}"/>
					</td>

					<td>
						<a href="javascript:Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="registration.quickestimate.list.cancelitem"/>'>
							<img src="${cpath}/icons/Delete.png" class="imgDelete button"/>
						</a>
						<input type="hidden" name="delCharge" value="${charge.status=='X'?'true':'false'}"	/>
					</td>
					<td>
						<a href="javascript:Edit" onclick="return showEditChargeDialog(this);" title='<insta:ltext key="registration.quickestimate.list.edititemdetails"/>'>
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</tr>
			</c:forEach>
			<tr class="footer">
				<td colspan="12"/>
				<td>
					<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="registration.quickestimate.list.addnewitem"/>'
							onclick="initializeOrderDialog(this); return false;" accesskey="+" class="imgButton">
							<img src="${cpath}/icons/Add.png">
					</button>
				</td>
			</tr>
		</table>
		<div style="height:10px;">&nbsp;</div>
	</fieldset>
</div>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.quickestimate.list.totals"/></legend>
	<table width="100%" align="right" class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.estimatedamount"/>:</td>
			<td class="forminfo"><label id="lblTotAmt"></label></td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.discounts"/>:</td>
			<td class="forminfo"><label id="lbldiscount"></label></td>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totaltax"/>:</td>
			<td class="forminfo"><label id="lblTaxAmt"></label></td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.netamount"/>:</td>
			<td class="forminfo"><label id="lblNetAmt"></label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.sponsoramount"/>:</td>
			<td class="forminfo">
				<label id="sponsorTotAmt"></label>
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsortax"/>:</td>
			<td class="forminfo">
				<label id="sponsorTaxAmt"></label>
			</td>
			<td class="formlabel"><insta:ltext key="registration.quickestimate.list.patientamount"/>:</td>
			<td class="forminfo">
				<label id="patientTotAmt"></label>
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.patienttax"/>:</td>
			<td class="forminfo">
				<label id="patientTaxAmt"></label>
			</td>
		</tr>
	</table>
</fieldset>

<div class="screenActions" style="float: left">
		<button type="button" name="save" id="save" class="button" accesskey=""
			onclick="return doSaveAndPrint('');" tabindex="321"><label><u><b><insta:ltext key="registration.quickestimate.list.s"/></b></u><insta:ltext key="registration.quickestimate.list.ave"/></label></button> |
		<button type="button" name="print" id="print" class="button" accesskey="P"
			onclick="return doSaveAndPrint('P');" tabindex="321"><label><insta:ltext key="registration.quickestimate.list.button.save.ampersand.text"/>&nbsp;<u><b><insta:ltext key="registration.quickestimate.list.button.p.text"/></b></u><insta:ltext key="registration.quickestimate.list.button.rint.text"/></label></button> |
		<c:if test="${empty estimate}">
			<button type="button" id="reset" class="button" onclick="return onClickClear();"><label><insta:ltext key="registration.quickestimate.list.clear"/></label></button> |
		</c:if>
	 <a href="${cpath}/pages/registration/QuickEstimate.do?_method=list&sortOrder=estimate_no&sortReverse=true"><insta:ltext key="registration.quickestimate.list.savedestimates"/></a>
</div>

<div style="float: right;margin-top: 10px">
	<insta:selectdb name="printType" table="printer_definition"
		valuecol="printer_id"  displaycol="printer_definition_name"
		value="${pref.map.printer_id}"/>
</div>
<div style="float: right;margin-top: 10px">&nbsp;</div>
<div style="float: right;margin-top: 10px">
	<select name="quickEstimateTemplate" id="quickEstimateTemplate" class="dropdown">
		<option value="QuickEstimateBillPrint"><insta:ltext key="registration.quickestimate.list.quickEstimatebill"/></option>
		<option value="QuickEstimateBillPrintText"><insta:ltext key="registration.quickestimate.list.Quickestimatebilltextformat"/></option>
		<c:forEach var="template" items="${quickEstimateTemplates}">
			<option value="${template.map.template_name}">${template.map.template_name}</option>
		</c:forEach>
	</select>
</div>

<div style="clear: both"></div>

</form>

<form name="editform">
<input type="hidden" id="editRowId" value=""/>
<div id="editChargeDialog" style="display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="registration.quickestimate.list.edititemchargedetails"/></legend>
			<table class="formtable" id="editChgTbl">
				<c:set var="noOfcols" value="${screenId eq 'reg_quick_estimate' ? '1' : '2'}" />
			<tr>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.head"/>:</td>
				<td class="forminfo" colspan="${noOfcols}"><label id="eChargeHead"></label>	</td>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.description"/>:</td>
				<td class="forminfo" colspan="${noOfcols}"><label id="eDescription"></label>	</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.rate"/>:</td>
				<td>
					<input type="text" name="eRate" onchange="recalcEditChargeAmount()"
					onkeypress="return enterNumOnly(event);" />
				</td>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.qty"/>:</td>
				<td>
					<input type="text" name="eQty" onchange="recalcEditChargeAmount()" onkeypress="return enterNumOnly(event);"/>
				</td>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.discount"/>:</td>
				<td>
					<input type="text" name="eDiscount" onkeypress="return enterNumOnly(event);" onchange="recalcEditChargeAmount()"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.amount"/>:</td>
				<td>
					<input type="text" name="eAmt" readonly="1"/>
				</td>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.sponsor.amt"/>:</td>
				<td>
					<input type="text" name="eSponsorAmt" onkeypress="return enterNumOnly(event);" onchange="recalcEditChargeAmount()"/>
				</td>
				<td class="formlabel"><insta:ltext key="registration.quickestimate.list.patient.amt"/>:</td>
				<td>
					<input type="text" name="ePatAmt" readonly="1"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totaltax"/>:</td>
				<td>
					<input type="text" name="eTax" readonly="1"/>
				</td>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsortax"/>:</td>
				<td>
					<input type="text" name="eSponsorTax" readonly="1"/>
				</td>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.patienttax"/>:</td>
				<td>
					<input type="text" name="ePatTax" readonly="1"/>
				</td>
			</tr>
		</table>
		</fieldset>
		<table>
			<tr>
				<td><input type="button" onclick="onEditSubmit()" value= "<insta:ltext key="registration.quickestimate.list.patient.ok"/>" /></td>
				<td><input type="button" onclick="onEditCancel()" value= "<insta:ltext key="registration.quickestimate.list.patient.cancel"/>" /></td>
			</tr>
		</table>
	</div>
</div>
</form>

<insta:AddOrderDialog visitType="${visit_type}" includeOtDocCharges="Y"/>

<c:if test="${not empty estimate}">
<div class="legend">
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText"><insta:ltext key="registration.quickestimate.list.cancelled.or.deleted"/></div>
</div>
</c:if>
<insta:link type="js" file="select2.min.js"/>
<script>
	var jModulesActivated = <%= request.getAttribute("modulesActivatedJSON") %>;
	var jChargeHeads = <%= request.getAttribute("chargeHeadsJSON") %>;
	var pendingtests = '${pendingtest}';
	var jDiscountAuthorizers = <%= request.getAttribute("discountAuthorizersJSON") %>;
	var jDoctors = <%= request.getAttribute("doctorsJSON") %>;
	var taxSubGroupsList = <%= request.getAttribute("taxSubGroupsJSON") %>;
	var printURL = '${pop_up}';
	var screenId = '${screenId}';
	totAmtPaise = ${totalNetAmount*100};
	var screenid = '${screenId}';//To avoid scripting error.bug no#48086

	//Phone number logic
	(function(){
		var patientPhone = $("#mobile_no");		
		var patientPhoneNational=$("#mobile_no_national");
		var patientPhoneCountryCode=$("#mobile_no_country_code");
		var patientPhoneHelp=$("#mobile_no_help");
		var patientPhoneError =$("#mobile_no_error");
		var patientPhoneValid = $("#mobile_no_valid");

		
		patientPhoneCountryCode.select2();
		
		patientPhoneCountryCode.on('change', function (e) {
			//get text for help menu
		    getExamplePhoneNumber(this.value,patientPhoneHelp,patientPhoneError);
		});
		
		patientPhoneCountryCode.on('select2:select', function (e) {
		    patientPhoneNational.focus();
		});	
		
		patientPhoneNational.on('blur',function(e,eventDataObj){

			clearErrorsAndValidatePhoneNumber(patientPhone,patientPhoneValid,
					patientPhoneNational,patientPhoneCountryCode,patientPhoneError,'N',eventDataObj);
				
		});
		// Get help text for mobile_no
		getExamplePhoneNumber(defaultCountryCode,patientPhoneHelp,patientPhoneError);
	
})();
</script>
</body>
</html>

