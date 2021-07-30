<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="org.apache.struts.Globals"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath">${pageContext.request.contextPath }</c:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Doctor Details</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<insta:link type="css" file="select2.min.css"/>
<insta:link type="css" file="select2Override.css"/>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/Adddoctor.js" />

<script>
	var defaultCountryCode= "+${defaultCountryCode}";
	var cpath = '${cpath}';
	var doctorNames = ${doctorNames};
	var chanelling = '${preferences.modulesActivatedMap.mod_channeling}';
</script>
</head>

<body onload="autoCompleteDoctors();splitDocName();" class="yui-skin-sam">
<form method="POST" action="${cpath}/master/DoctorMasterCharges.do?_method=${mode == 'update' ? 'updateDoctorDetails' : 'addNewDoctor'}" enctype="multipart/form-data">
	<c:if test="${mode == 'update'}">
		<h1>Edit Doctor Details</h1>
	</c:if>
	<c:if test="${mode != 'update'}">
		<h1>Add New Doctor </h1>
	</c:if>
	<insta:feedback-panel/>
	<input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(doctor_id)}"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}" />
	<input type="hidden" name="mode" value="${ifn:cleanHtmlAttribute(mode)}" />
	<input type="hidden" name="doctor_name" id="doctor_name" value="${DocDetails.doctor_name}" />
	<input type="hidden" name="doctor_id" id="doctor_id" value="${DocDetails.doctor_id}" />
	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Salutation :</td>
			<td>
				<div>
					<insta:selectdb id="doc_salutation_id" name="doc_salutation_id" value="${DocDetails.doc_salutation_id}" table="salutation_master" valuecol="salutation_id"
						orderby="salutation" displaycol="salutation" onchange="setDoctorName()"/><span class="star">*</span>
				</div>
			</td>
			<td class="formlabel">First Name :</td>
			<td>
				<div>
					<input type="text" name="doc_first_name" id="doc_first_name" maxlength="50" value="${DocDetails.doc_first_name}"
						style="width:138px;" class="required" onfocusout="setDoctorName()"/>
					<span class="star">*</span>
				</div>
			</td>
			<td class="formlabel">Middle Name :</td>
			<td>
				<div>
					<input type="text" name="doc_middle_name" id="doc_middle_name" maxlength="100" value="${DocDetails.doc_middle_name}"
						style="width:138px;" onfocusout="setDoctorName()"/>
				</div>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Last Name :</td>
			<td>
				<div>
					<input type="text" name="doc_last_name" id="doc_last_name" maxlength="50" value="${DocDetails.doc_last_name}"
						style="width:138px;" class="required" onfocusout="setDoctorName()"/><span class="star">*</span>
				</div>
			</td>
			<td class="formlabel">Department:</td>
			<td>
				<insta:selectdb name="dept_id" value="${DocDetails.dept_id}" table="department" valuecol="dept_id"
						orderby="dept_name" displaycol="dept_name" />
			</td>
			<td class="formlabel">Doctor Type:</td>
			<td>
				<insta:selectoptions name="doctor_type" value="${DocDetails.doctor_type}"
						opvalues="HOSPITAL,CONSULTANT"  optexts="HOSPITAL,CONSULTANT"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Qualification:</td>
			<td><input type="text" name="qualification" maxlength="50" value="${DocDetails.qualification}"/></td>
			<td class="formlabel">Specialization:</td>
			<td><input type="text" name="specialization" id="specialization" value="${DocDetails.specialization}" size="15" maxlength="50"/></td>
			<td class="formlabel">Registration Number:</td>
			<td><input type="text" name="registration_no" id="registration_no" value="${DocDetails.registration_no}"  size="15" maxlength="15"/></td>
		</tr>
		<tr>
			<td class="formlabel">Clinic Phone:</td>
			<td><input type="text" name="clinic_phone"   value="${DocDetails.clinic_phone}"  maxlength="15" /></td>
			<td class="formlabel">Mobile:</td>
			<td>
				<div  style="margin-top:12px">				
				 <div>
						<input type="hidden" id="doctor_mobile" name="doctor_mobile"/>
						<input type="hidden" id="doctor_mobile_valid" value="Y"/>
						<select id="doctor_mobile_country_code" class="dropdown" style="width:76px" name="doctor_mobile_country_code">
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
						 <input type="text" class="field" id="doctor_mobile_national" maxlength ="15" onkeypress="return enterNumOnlyzeroToNine(event)"
								 style="width:9em;padding-top:1px" />
							<img class="imgHelpText" id="doctor_mobile_help" src="${cpath}/images/help.png"/>
					</div>
					<div>
						<span style="visibility:hidden;padding-left:10px;color:#f00" id="doctor_mobile_error"></span>	
					</div> 
				</div>
			</td>
			<td class="formlabel">Res.Phone:</td>
			<td><input type="text" name="res_phone"  value="${DocDetails.res_phone}"  size="15" maxlength="15" /></td>
	   </tr>
		<tr>			
			<td class="formlabel">Doctor License Number:</td>
			<td>
			  <div style="display: flex" >
			    <input type="text" name="doctor_license_number" id="doctor_license_number" value="${DocDetails.doctor_license_number}"
			      size="15" />
                <img id="loaderimg" name="loaderimg" title="Verifying Doctor License No."
                  src="<%=request.getContextPath()%>/images/ajax-loader.gif"  width="16"
                  height="16" style="vertical-align:top;visibility:hidden;float:right;" />
			  </div>
            </td>
			<td class="formlabel">Address:</td>
			<td><textarea rows="2" name="doctor_address" id="doctor_address">${DocDetails.doctor_address}</textarea></td>
			<td class="formlabel">Prescribe By Favourites:</td>
			<td>
				<insta:selectoptions name="prescribe_by_favourites" value="${DocDetails.prescribe_by_favourites}"
						opvalues="N,Y"  optexts="No,Yes"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Enable Doctor for Surgery/Procedure:</td>
			<td>
				<insta:selectoptions name="ot_doctor_flag" value="${DocDetails.ot_doctor_flag}"
						opvalues="N,Y"  optexts="NO,Yes"/>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${DocDetails.status}"
						opvalues="A,I"  optexts="Active,InActive"/>
			</td>
			<td class="formlabel">Schedulable by:</td>
			<td><insta:selectoptions name="scheduleable_by" value="${DocDetails.scheduleable_by}"
						opvalues="N,S,A"  optexts="None,HMS Staff,All" onchange="changeOverbookValue()" />
		</tr>
		<tr>
			<td class="formlabel">Email:</td>
			<td colspan="3"><input type="text" name="doctor_mail_id" value="${DocDetails.doctor_mail_id}" style="width: 444px"  maxlength="500"/></td>
			<c:set var="overbookDisable" value="${DocDetails.schedule ? '' : 'disabled'}"/>
			
			<td class="formlabel">Overbook Limit:</td>
			
			<td><input type="text" name="overbook_limit" value="${overbookDisable == 'disabled'? 0 :DocDetails.overbook_limit}" ${overbookDisable}
			onkeypress="return enterNumOnlyzeroToNine(event)" maxlength=2 class="required" style="width:132px" /><span style="float: right" class="star">*</span>
			<img class="imgHelpText" title=" Zero - overbook not allowed.
 Specific number - that many overbooking allowed.
 Maximum allowed value is 99" src="${cpath}/images/help.png" style="float:right"></td>
			       

		</tr>
		<tr>
			<td class="formlabel">Payment Category:</td>
			<td>
				<insta:selectdb name="payment_category" value="${DocDetails.payment_category}" table="category_type_master" valuecol="cat_id"
					displaycol="cat_name" />
			</td>
			<td class="formlabel">Payment Eligible:</td>
			<td>
				<insta:selectoptions name="payment_eligible" value="${DocDetails.payment_eligible}"
						opvalues="N,Y"  optexts="NO,Yes"/>
			</td>
			<td class="formlabel">Doctor Speciality:</td>
			<td>
				<insta:selectdb id="speciality_id" name="speciality_id" value="${DocDetails.speciality_id}" table="doctor_speciality_master"
					valuecol="speciality_id"  displaycol="display_name" class="dropdown" dummyvalue="---select---" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">OP Revisit Consultation Validity:</td>
			<td><input type="text" name="op_consultation_validity"
				onkeypress='return enterNumOnlyzeroToNine(event)' value="${DocDetails.op_consultation_validity}" size="15"/></td>
			<td class="formlabel">OP Revisit Consultation Count:</td>
			<td><input type="text" name="allowed_revisit_count"
				onkeypress='return enterNumOnlyzeroToNine(event)' value="${DocDetails.allowed_revisit_count}" size="15"/></td>
			<c:if test="${preferences.modulesActivatedMap.mod_op == 'Y' and preferences.modulesActivatedMap.mod_newcons != 'Y'}">
					<td class="formlabel">OP Consultation Template</td>
					<td><select id="op_template_id" name="op_template_id" class="dropdown">
							<option value="0">---select---</option>
							<c:forEach var="template" items="${OpConsultTemplates}">
								<option value="${template.map.template_id}"
									${template.map.template_id == DocDetails.op_template_id ? 'selected' : ''}>${template.map.template_name}</option>
							</c:forEach>
						</select>
					</td>
			</c:if>
			<c:set var="onlineConsultDisable" value="${not DocDetails.schedule}"/>
			<td class="formlabel">Schedulable for Online Consults:</td>
			<td>
				<insta:selectoptions name="available_for_online_consults" id="available_for_online_consults" disabled="${onlineConsultDisable}"
				value="${onlineConsultDisable ? 'N' : DocDetails.available_for_online_consults}"
				opvalues="N,Y"  optexts="No,Yes"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">IP Discharge Consultation Validity:</td>
			<td><input type="text" name="ip_discharge_consultation_validity"
				onkeypress='return enterNumOnlyzeroToNine(event)' value="${DocDetails.ip_discharge_consultation_validity}" size="15"/></td>
			<td class="formlabel">IP Discharge Consultation Count:</td>
			<td><input type="text" name="ip_discharge_consultation_count"
				onkeypress='return enterNumOnlyzeroToNine(event)' value="${DocDetails.ip_discharge_consultation_count}" size="15"/></td>
			<td class="formlabel">Practitioner:</td>
			<td><select id="practitioner_id" name="practitioner_id" class="dropdown">
					<option value="">---select---</option>
					<c:forEach var="practitioner" items="${PractitionerTypes}">
						<option value="${practitioner.practitioner_id}"
							${practitioner.practitioner_id == DocDetails.practitioner_id ? 'selected' : ''}>${practitioner.practitioner_name}</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
        <c:if test="${not empty GDocDetails.doctorCustomField1}">
				<td class="formlabel">${ifn:cleanHtml(GDocDetails.doctorCustomField1)}:</td>
				<td><input type="text" class="text-input"
					name="custom_field1_value" value="${DocDetails.custom_field1_value}"  size="15" tabindex="259"></td>
		</c:if>
	   <c:if test="${not empty GDocDetails.doctorCustomField2}">
				<td class="formlabel">${ifn:cleanHtml(GDocDetails.doctorCustomField2)}:</td>
				<td><input type="text" class="text-input"
					name="custom_field2_value" value="${DocDetails.custom_field2_value}" size="15" tabindex="260"></td>
		</c:if>
	   <c:if test="${not empty GDocDetails.doctorCustomField3}">
				<td class="formlabel">${ifn:cleanHtml(GDocDetails.doctorCustomField3)}:</td>
				<td><input type="text" class="text-input"
					name="custom_field3_value" value="${DocDetails.custom_field3_value}"  size="15" tabindex="261"></td>
		</c:if>
	</tr>
	<tr>
	   <c:if test="${not empty GDocDetails.doctorCustomField4}">
				<td class="formlabel">${ifn:cleanHtml(GDocDetails.doctorCustomField4)}:</td>
				<td><input type="text" class="text-input"
					name="custom_field4_value" value="${DocDetails.custom_field4_value}"  size="15" tabindex="262"></td>
		</c:if>
	   <c:if test="${not empty GDocDetails.doctorCustomField5}">
				<td class="formlabel">${ifn:cleanHtml(GDocDetails.doctorCustomField5)}:</td>
				<td><input type="text" class="text-input"
					name="custom_field5_value" value="${DocDetails.custom_field5_value}"  size="15" tabindex="263"></td>
		</c:if>

		</tr>

		<tr>
			<c:if test="${preferences.modulesActivatedMap.mod_ipservices == 'Y'}">
					<td class="formlabel">IP Consultation Template</td>
					<td><select id="ip_template_id" name="ip_template_id" class="dropdown">
							<option value="0">---select---</option>
							<c:forEach var="template" items="${OpConsultTemplates}">
								<option value="${template.map.template_id}"
									${template.map.template_id == DocDetails.ip_template_id ? 'selected' : ''}>${template.map.template_name}</option>
							</c:forEach>
						</select>
					</td>
			</c:if>
			<td class="formlabel">Photo: </td>
			<td><input type="file" name="photo" id="photo" accept="<insta:ltext key="upload.accept.image"/>"/>
				<c:if test="${photoExist}"><a href="DoctorMasterCharges.do?_method=viewPhoto&doctor_id=${ifn:cleanURL(doctor_id)}" target="_blank">view</a></c:if>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Allow signature to be used by others: </td>
			<td >
				<select name="allow_sig_usage_by_others" class="dropdown">
					<option value="N" ${DocDetails.allow_sig_usage_by_others == 'N' ? 'selected' : ''}>No</option>
					<option value="Y" ${DocDetails.allow_sig_usage_by_others == 'Y' ? 'selected' : ''}>Yes</option>
				</select>
			</td>
			<td class="formlabel">Signature: </td>
			<td ><input type="file" name="userSignature" id="userSignature" accept="<insta:ltext key="upload.accept.image"/>"/>
			<c:if test="${not empty signature_username}">
				<a href="${cpath}/master/DoctorMasterCharges.do?_method=viewSignature&doctor_id=${ifn:cleanURL(doctor_id)}" title="View Signature" target="_blank">View Signature</a>
			</c:if>
			</td>
			<td class="formlabel">Send Feedback SMS:</td>
			<td><input type="checkbox" name="send_feedback_sms" ${DocDetails.send_feedback_sms ?'checked':''} 
			${mode != 'update'?'checked':''}></td>
		</tr>

	</table>
	</fieldset>

	<div class="screenActions">
		<button type="button" accesskey="S" onclick="return validations(true);"><b><u>S</u></b>ave</button>
		<c:if test="${mode == 'update'}">
		|
		<a href="${cpath}/master/DoctorMasterCharges.do?_method=add&orgId=${ifn:cleanURL(org_id)}">Add</a>
		</c:if>
		|
		<a href="${cpath}/master/DoctorMaster.do?_method=list&status=A&sortOrder=doctor_name&sortReverse=false&org_id=${ifn:cleanURL(org_id)}">Doctors List</a>
		<c:if test="${mode == 'update' && urlRightsMap.op_prescribe == 'A'}">
			| <a href="${cpath}/master/ConsultationFavourites.do?_method=list&doctor_id=${ifn:cleanURL(doctor_id)}"
				title="Manage Consultation Favourites">Manage Consultation Favourites</a> | <a href="${cpath}/master/DiagnosisCodeFavourites.do?_method=list&doctor_id=${ifn:cleanURL(doctor_id)}"
				title="Manage Diagnosis Favourites">Manage Diagnosis Favourites</a>
		</c:if>
		<c:if test="${max_centers_inc_default > 1}">
		<c:if test="${mode == 'update'}">
		|	<insta:screenlink screenId="mas_doctors_cen_app" extraParam="?_method=getScreen&doctor_id=${doctor_id}"
					label="Center Applicability" />
		</c:if>
		</c:if>
	</div>
</form>

<insta:link type="js" file="select2.min.js"/>
<insta:link type="js" file="phoneNumberUtil.js"/>
<script type="text/javascript">	
	(function(){
		var doctorMobile = $("#doctor_mobile");		
		var doctorMobileNational=$("#doctor_mobile_national");
		var doctorMobileCountryCode=$("#doctor_mobile_country_code");
		var doctorMobileHelp=$("#doctor_mobile_help");
		var doctorMobileError =$("#doctor_mobile_error");
		var doctorMobileValid = $("#doctor_mobile_valid");
		
		doctorMobileCountryCode.select2();
		
		doctorMobileCountryCode.on('change', function (e) {
			//get text for help menu
		    getExamplePhoneNumber(this.value,doctorMobileHelp,doctorMobileError);
		});
		
		doctorMobileCountryCode.on('select2:select', function (e) {
		    doctorMobileNational.focus();
		});
		
		doctorMobileNational.on('blur',function(e){

			clearErrorsAndValidatePhoneNumber(doctorMobile,doctorMobileValid,
					doctorMobileNational,doctorMobileCountryCode,doctorMobileError,'N');
				
		});
		// Get help text for doctor_mobile
		getExamplePhoneNumber(defaultCountryCode,doctorMobileHelp,doctorMobileError);

		//set country and national number of doctor_mobile
		
		var doctorMobileNumber = '${DocDetails.doctor_mobile}';
		if(doctorMobileNumber){
			insertNumberIntoDOM(doctorMobileNumber,doctorMobile,doctorMobileCountryCode,
				   doctorMobileNational);
		}
	})();
</script>
</body>
</html>
