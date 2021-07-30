<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'/>
<c:set var="enableDistrict" value='<%=RegistrationPreferencesDAO.getRegistrationPreferences().getEnableDistrict() %>' />

<html>
<head>
<title>Referral Doctor</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="phoneNumberUtil.js"/>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="areacommon.js"/>
<insta:link type="js" file="select2.min.js"/>

<insta:link type="css" file="select2.min.css"/>
<insta:link type="css" file="select2Override.css"/>

<script>
	$(
		function(){
			window.referralPhone = $('#referal_mobileno');
			window.referralDoctorPhoneNational = $('#referral_doctor_phone_national');
			window.referralDoctorCountryCode=$("#referral_doctor_phone_country_code");
			window.phoneError =$("#phone_error");
			window.phoneValid = $("#phone_valid");
			window.referralPhoneHelp = $('#referral_phone_help');

			referralDoctorPhoneNational.on('blur', () => {
				validatePhone();
			});
			
			var referralMobileNumber = "${bean.map.referal_mobileno}";
			if(referralMobileNumber.trim()){
				insertNumberIntoDOM(referralMobileNumber.trim(), null, referralDoctorCountryCode, referralDoctorPhoneNational	)
			}
			referralDoctorCountryCode.select2();

			getExamplePhoneNumber(referralDoctorCountryCode.val(), referralPhoneHelp, phoneError);

			referralDoctorCountryCode.on('change', function(e){
				getExamplePhoneNumber(e.target.value, referralPhoneHelp, phoneError);
			});
			

		}
	);
	
	function initReferralDoctor() {
		initAddressFieldsAutocompletes();
	}

	function validatePhone(){
		clearErrorsAndValidatePhoneNumber(referralPhone,phoneValid,referralDoctorPhoneNational, referralDoctorCountryCode, phoneError, true, null );
		if (phoneValid.val() == 'N'){
			return false;
		}
		return true;
	}

	function validate(){


		if(document.forms[0].referal_name.value==""){
			alert("Referral doctor name is required");
			document.forms[0].referal_name.focus();
			return false;
		}
		if(document.forms[0].payment_eligible.value == 'Y'){
		    if(document.forms[0].payment_category.selectedIndex==0){
			   alert("Payment category is required");
			   document.forms[0].payment_category.focus();
			   return false;
		    }
	    }
	    else{
	        document.getElementById('payment_category').value = 1;
	    }
		if(document.forms[0].payment_eligible.selectedIndex==0){
			alert("Payment eligible is required");
			document.forms[0].payment_eligible.focus();
			return false;
		}
		document.getElementById('referal_mobileno').value = trim(document.getElementById('referal_mobileno').value);
		if (document.getElementById('referal_mobileno').value == '') {
			alert('Referral doctor mobile number is required');
			document.getElementById('referal_mobileno').focus();
			return false;
		}

		// Area make it mandatory if state/district/city selected by user, otherwise optional
		if(document.forms[0].area_id.value=="" 
			&& (document.forms[0].state_id.value != null && document.forms[0].state_id.value != "") ){
			alert("Referral doctor Area is required");
			document.forms[0].patient_area.focus();
			return false;
		}
		
		if(document.forms[0].city_id.value==""
			&& (document.forms[0].state_id.value != null && document.forms[0].state_id.value != "") ){
			alert("Referral doctor City is required");
			document.forms[0].patient_city.focus();
			return false;
		}

		if(!validatePhone()){
			return false;
		}

		if($('#referal_doctor_email').val() && !isEmail($('#referal_doctor_email').val(), 'Email is invalid')){
			return false;
		}
		document.forms[0].submit();
		return true;
	}

	var cpath = '${cpath}';
	var enableDistrict = '${enableDistrict}';
</script>

</head>
<body onload="initReferralDoctor();" class="yui-skin-sam">

<form action="ReferalDoctor.do" >
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="referal_no" value="${bean.map.referal_no}"/>
	</c:if>

	<div class="pageHeader">${param._method == 'add' ? 'Add' : 'Edit'} Referral</div>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">

	<tr>
		<td class="formlabel">Referral Doctor:</td>
		<td><input type="text" name="referal_name" value="${bean.map.referal_name }" maxlength="100"></td>


		<td class="formlabel">Phone No:</td>
	    <td><input type="text" name="referal_doctor_phone" value="${bean.map.referal_doctor_phone}" maxlength="15"></td>
	   	<td>&nbsp;</td>
	   	<td>&nbsp;</td>
	</tr>

	<tr>
		<td class="formlabel">Status:</td>
                <td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,InActive"/></td>

       	<td class="formlabel">Email Id:</td>
        <td colspan="3"><input type="text" id="referal_doctor_email" name="referal_doctor_email" value="${bean.map.referal_doctor_email}" maxlength="500" style="width: 400px"></td>
	</tr>

	<tr>
		 <td class="formlabel">Mobile No:</td>
		
		<td style="width: 250px;">
			<div>
				<input type="hidden" id="referal_mobileno" name="referal_mobileno"/>
				<input type="hidden" id="phone_valid" value="N"/>
				<select id="referral_doctor_phone_country_code" class="dropdown" style="width:76px;" name="referral_doctor_phone_country_code">
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
				<input type="text" class="field phoneField" id="referral_doctor_phone_national" onkeypress="return enterNumOnlyzeroToNine(event)" maxlength ="15" 
						/>
				<img class="imgHelpText" id="referral_phone_help" src="${cpath}/images/help.png"/>

			</div>
			<div>
				<span style="visibility:hidden;color:#f00" id="phone_error"></span>	
			</div>
		</td>
		
 
	    <td class="formlabel">Address:</td>
		<td><textarea rows="2" name="referal_doctor_address" id="referal_doctor_address">${bean.map.referal_doctor_address}
			</textarea></td>
    </tr>

    <tr>
    	<td class="formlabel">Payment Category:</td>
		<td><insta:selectdb  name="payment_category" id="payment_category" value="${bean.map.payment_category}" table="category_type_master"
			 valuecol="cat_id" displaycol="cat_name" dummyvalue="..Select.."/></td>
	    <td class="formlabel">License Number:</td>
		<td><input type="text" name="clinician_id" id="clinician_id" value="${bean.map.clinician_id}" maxlength="25">
			<img id="loaderimg" name="loaderimg" title="Verifying ReferalDoctor License No."
				src="<%=request.getContextPath()%>/images/ajax-loader.gif"  width="16"
				height="16" style="vertical-align:top;visibility:hidden;float:right;"/>
		</td>
    </tr>

    <tr>
    	<td class="formlabel">Payment eligible:</td>
		<td><insta:selectoptions name="payment_eligible" value="${bean.map.payment_eligible}"
							tabindex="250"  opvalues=" ,Y,N"
							optexts="..Select..,Yes,No" /></td>
    </tr>
    <tr>
	   	<td class="formlabel"><insta:ltext key="ui.label.state"/>:</td>
	   	<td>
	   		<table>
				<tr>
					<td valign="top">
						<div id="autostate" class="autoComplete">
							<input name="patient_state" id="patient_state" type="text"
							value="${bean.map.state_name}" style="width:11.6em" maxlength="50" />
							<div id="state_dropdown" style="width:250px"></div>
							<input type="hidden" name="state_id" id="state_id" value="${bean.map.state_id}"/>
						</div>
					</td>
				</tr>
			</table>
		</td>
		<c:if test="${enableDistrict == 'Y'}">
			<td class="formlabel">
				<insta:ltext key="ui.label.district"/>:
			</td>
			<td>
				<table>
					<tr>
						<td valign="top">
							<div id="autodistrict" class="autoComplete">
								<input name="patient_district" id="patient_district" type="text"
								value="${bean.map.district_name}" style="width:11.6em" maxlength="50" />
								<div id="district_dropdown" style="width:250px"></div>
								<input type="hidden" name="district_id" id="district_id" value="${bean.map.district_id}"/>
							</div>
						</td>
					</tr>
				</table>
			</td>
		</c:if>
    </tr>
    <tr>
    	<td class="formlabel"><insta:ltext key="${enableDistrict == 'Y' ? 'ui.label.city.subdistrict' : 'ui.label.city' }"/>:</td>
		<td>
			<table>
				<tr>
					<td>
						<div id="autocity" class="autoComplete">
							<input type="text" name="patient_city" id="patient_city"
								value="${bean.map.city_name}" />
							<div id="city_dropdown" style="width:250px"></div>
							<input type="hidden" name="city_id" id="city_id" value="${bean.map.city_id}"/>
						</div>
					</td>
				</tr>
			</table>
		</td>
    	<td class="formlabel"><insta:ltext key="${enableDistrict == 'Y' ? 'ui.label.area.village' : 'ui.label.area' }"/>:</td>
		<td>
			<table>
				<tr>
					<td valign="top">
						<div id="autoarea" class="autoComplete">
							<input name="patient_area" id="patient_area" type="text"
							value="${bean.map.area_name}" style="width:11.6em" maxlength="50" />
							<div id="area_dropdown" style="width:250px"></div>
							<input type="hidden" name="area_id" id="area_id" value="${bean.map.area_id}"/>
						</div>
					</td>
				</tr>
			</table>
		</td>
	   	<td class="formlabel"><insta:ltext key="ui.label.country"/>:</td>
	  	<td>
	  		<label id="patient_country" class="formlabel">${bean.map.country_name}</label>
	  		<input type="hidden" name="country_id" id="country_id" value="${bean.map.country_id}"/>
	  	</td>
    </tr>
	</table>
	</fieldset>

	<table class="screenactions">
		<tr>
			<td><button type="button" accesskey="S" onclick="return validate();" ><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${pageContext.request.contextPath}/master/ReferalDoctor.do?_method=add">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/ReferalDoctor.do?_method=list&status=A&sortOrder=referal_name&sortReverse=false">Referral Doctors List</a></td>
		 	<c:if test="${max_centers_inc_default > 1}">
		 	<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td>
					<insta:screenlink screenId="mas_ref_doctors_app" extraParam="?_method=getScreen&referal_no=${param.referal_no}"
						label="Center Applicability" />
				</td>
			</c:if>
			</c:if>
		</tr>
	</table>

</form>

</body>
</html>
