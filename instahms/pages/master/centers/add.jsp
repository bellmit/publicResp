<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.CENTER_PATH %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add Center - Insta HMS</title>
	<style>
	 .centerMandatory {
	    display: inline-block;
	    padding: 5px;
	 }
	</style>
	<script>

	var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
	var modMalaffiEnabled = "${preferences.modulesActivatedMap['mod_malaffi']}" === 'Y';
	var regPref = ${ifn:convertListToJson(regPref)}[0];
	var cityStateCountryJSON = ${ifn:convertListToJson(cityStateCountryList)};
	var allCentersSameCompName = '${acc_prefs[0].all_centers_same_comp_name}';
	function init() {
		initAutoCityStateCountry("pat_city_name","city_state_country_dropdown", "city_id");
	}
	function setRegionId() {
		document.getElementById('region_id').value = document.getElementById('regionId').value;
	}
	function SaveCenter() {
		
		document.getElementById("defaultcity").value = document.getElementById("city_id").value;
		document.getElementById("defaultstate").value = document.getElementById("state_id").value;
		document.getElementById("defaultcountry").value = document.getElementById("country_id").value;
		document.getElementById('accounting_company_name').value = trim(document.getElementById('accounting_company_name').value);
		document.getElementById('hospital_center_service_reg_no').value = trim(document.getElementById('hospital_center_service_reg_no').value);
		var accountingCompName = document.getElementById('accounting_company_name').value;
		var centerServiceRegNo = document.getElementById('hospital_center_service_reg_no').value;
		if (allCentersSameCompName == 'N') {
			if (accountingCompName == '') {
				alert("Accounting Preferences: All Centers Same Comp. preference is No. \nHence Accounting Company Name is required.");
				document.getElementById('accounting_company_name').focus();
				return false;
			}
			if (!empty(eClaimModule) && eClaimModule == "Y" && centerServiceRegNo == "") {
				alert("Accounting Preferences: All Centers Same Comp. preference is No. \nHence Center Service Reg No. is required.");
				document.getElementById('hospital_center_service_reg_no').focus();
				return false;
			}
		}
		if (modMalaffiEnabled && centerServiceRegNo.trim().length === 0) {
			alert('As per Malaffi Integration requirements some of the mandatory fields are missing.');
			document.getElementById('hospital_center_service_reg_no').focus();
			return false;
		}
		return true;
	}
	function checkLength(obj,len,field){
		if( obj.value.length  > len ){
			alert("Max "+len+" characters are allowed in Address field");
			obj.value = (obj.value).substring(0,200);
			obj.focus();
			return false;
		}
		return true;
	}
	</script>
	<insta:link type="js" file="registration/registration.js" />
	<insta:link type="js" file="registration/registrationCommon.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="common.message"/>
	<c:set var="startOfWeek">
		<insta:ltext key="patient.resourcescheduler.schedulerdayview.today"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.sunday"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.monday"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.tuesday"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.wednesday"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.thursday"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.friday"/>,
		<insta:ltext key="patient.resourcescheduler.doctornonavailability.saturday"/>
	</c:set>
</head>
<body onload="init();" class="yui-skin-sam">
	<h1> Add Center </h1>
	<insta:feedback-panel/>
	<form action="${cpath}${pagePath}/create.htm" method="post">
		<input type="hidden" name="center_id" value="${bean.center_id}">
		<input type="hidden" name="patient_city" id="city_id" value="${bean.city_id}"/>
    	<input type="hidden" name="patient_state" id="state_id" value="${bean.state_id}"/>
    	<input type="hidden" name="country" id="country_id" value="${bean.country_id}"/>
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Center Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Center Name: </td>
					<td><input type="text" name="center_name" class="required" title="Center Name is mandatory."></td>
					<td class="formlabel">Center Code: </td>
					<td><input type="text" name="center_code" class="required" title="Center Code is mandatory."></td>
					<td class="formlabel">Status: </td>
					<td><Select name="status" class="dropdown validate-not-empty" title="Status is mandatory.">
							<option value="">-- Select --</option>
							<option value="A" selected >Active</option>
							<option value="I" >Inactive</option>
						</Select>
					</td>
				</tr>
				<tr>
					<td class="formlabel">City: </td>
					<td>
						<input type="hidden" name="city_id" id="defaultcity"/>
						<div id="city_state_country_wrapper" class="autoComplete">
							<input type="text" name="pat_city_name" id="pat_city_name"
								value="${cityName}" class="required" title="City is mandatory."/>
						<div id="city_state_country_dropdown" style="width:250px"></div>
						</div>
						<div class="centerMandatory">
							<span class="star">*</span>
						</div>
					</td>
					<td class="formlabel">State: </td>
					<td>
						<input type="hidden" name="state_id" id="defaultstate" />
						<label id="statelbl" class="formlabel">${stateName}</label>
					</td>
					<td class="formlabel">Country: </td>
					<td>
						<input type="hidden" name="country_id" id="defaultcountry"/>
						<label id="countrylbl" class="formlabel">${countryName}</label>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Address: </td>
					<td>
						<textarea style="width:150px; height:50px; padding:0 0 2px 2px"
							title="Please enter Address upto 200 characters only." name="center_address"
							id="center_address" tabindex="90" onkeypress="return checkLength(this, 200, event);"></textarea>
					</td>
					<td class="formlabel">Center Contact No:</td>
					<td>
						<input type="text" maxlength="30" name="center_contact_phone" id="center_contact_phone">
					</td>
					<td class="formlabel">Region:</td>
					<td>
						<select name="region_id" id="region_id" class="dropdown">
                            <option value=""> -- Select -- </option>
                            <c:forEach items="${regionBean}" var="region" >
                                <option value="${region.region_id}" ${bean.region_id == region.region_id ? 'selected':''}>${region.region_name}</option>
                            </c:forEach>
                        </select>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Center Service Reg No:</td>
					<td><input type="text" name="hospital_center_service_reg_no" id="hospital_center_service_reg_no"
							class="validate-length" length="200" title="Center Service reg no. max length is 200" ></td>
					<td class="formlabel">Accounting Company Name: </td>
					<td><input type="text" name="accounting_company_name" id="accounting_company_name" class="validate-length"
							onblur="capWords(accounting_company_name)" length="200" title="max length of Company name can be 200 chars."/></td>
					<c:if test="${mod_eclaim}">
					<td class="formlabel">Health Authority:</td>
					<td>
						<label style="width: 100px;">${bean.health_authority}</label>

						<img class="imgHelpText" title="Allowed Health Authority (or) Schema for ERx/PBM/Eclaim -- HAAD/DHA,this field can be only updatable from backend"
							src="${cpath}/images/help.png"/>
					</td>
					</c:if>					
				</tr>
				<tr>
					<td class="formlabel">Tax Identification Number:</td>
					<td><input type="text" name="tin_number" id="tin_number"
							class="validate-length" length="50" title="Tax Identification Number max length is 50" ></td>
					<td class="formlabel">Start day of Scheduler Week View:</td>
					<td> 
						<insta:selectoptions name="start_of_week" id="start_of_week"
							opvalues="-1,0,1,2,3,4,5,6" optexts="${startOfWeek}"
							value="${bean.start_of_week}"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Reporting Meta:</td>
					<td colspan="3">
						<textarea style="width:450px; height:100px; padding:0 0 2px 2px"
							title="input custom json containing custom values for use in reports." name="reporting_meta"
							id="reporting_meta">${bean.reporting_meta}</textarea>
					</td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save" onclick="return SaveCenter();"/>
					<td> | <a href="${cpath}${pagePath}/list.htm?sortOrder=center_name&sortReverse=false">List</a>&nbsp;</td>
				</tr>
			</table>
	</form>
</body>
</html>