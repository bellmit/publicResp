<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="Cache-Control" content="no-cache"/>
<title>Registration Preferences - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<style type="text/css">
		table.resultList tr:hover td {
			background-color:white;
			cursor:default;
			color:#666;
		}

		.selectClass {
			border-top:1px #999 solid;
			border-left:1px #999 solid;
			border-bottom:1px #ccc solid;
			border-right:1px #ccc solid;
			color:#666;
			height:22px;
			width:85px;
			padding:2px 0px 0px 2px;
			vertical-align: middle;
		}
	</style>

	<script>

		function checkStatus(event){
			YAHOO.util.Event.stopEvent(event);
		}
		function validateForm() {

			for (var i=1; i<20; i++) {
				var customFieldObj = eval("document.RegForm.custom_field"+i+"_label");
				if (customFieldObj) customFieldObj.value = trim(customFieldObj.value);
			}

			for (var i=1; i<10; i++) {
				var customListFieldObj = eval("document.RegForm.custom_list"+i+"_name");
				if (customListFieldObj) customListFieldObj.value = trim(customListFieldObj.value);
			}

			for (var i=1; i<10; i++) {
				var visitCustomFieldObj = eval("document.RegForm.visit_custom_field"+i+"_name");
				if (visitCustomFieldObj) visitCustomFieldObj.value = trim(visitCustomFieldObj.value);
			}

			for (var i=1; i<3; i++) {
				var visitCustomListObj = eval("document.RegForm.visit_custom_list"+i+"_name");
				if (visitCustomListObj) visitCustomListObj.value = trim(visitCustomListObj.value);
			}

			document.RegForm.old_reg_field_label.value = trim(document.RegForm.old_reg_field_label.value);

			document.RegForm.passport_no.value				= trim(document.RegForm.passport_no.value);
			document.RegForm.passport_validity.value		= trim(document.RegForm.passport_validity.value);
			document.RegForm.passport_issue_country.value	= trim(document.RegForm.passport_issue_country.value);
			document.RegForm.visa_validity.value			= trim(document.RegForm.visa_validity.value);
			document.RegForm.nationality.value			    = trim(document.RegForm.nationality.value);

			document.RegForm.family_id.value	= trim(document.RegForm.family_id.value);

			document.RegForm.reg_validity_period.value   	  = trim(document.RegForm.reg_validity_period.value);
			document.RegForm.member_id_label.value	 		  = trim(document.RegForm.member_id_label.value);
			document.RegForm.member_id_valid_from_label.value = trim(document.RegForm.member_id_valid_from_label.value);
			document.RegForm.member_id_valid_to_label.value	  = trim(document.RegForm.member_id_valid_to_label.value);

			document.RegForm.default_followup_eandm_code.value	  = trim(document.RegForm.default_followup_eandm_code.value);

			document.RegForm.unidentified_patient_first_name.value	  = trim(document.RegForm.unidentified_patient_first_name.value);
			document.RegForm.unidentified_patient_last_name.value	  = trim(document.RegForm.unidentified_patient_last_name.value);

			if (document.RegForm.reg_validity_period.value == "") {
				alert("Registration validity period value is required");
				document.RegForm.reg_validity_period.focus();
				return false;
			}

			if (document.RegForm.member_id_label.value == "") {
				alert("Member Id/Reference No. Label is required");
				document.RegForm.member_id_label.focus();
				return false;
			}

			if (document.RegForm.member_id_valid_from_label.value == "") {
				alert("Member/Ref. Valid From Label is required");
				document.RegForm.member_id_valid_from_label.focus();
				return false;
			}

			if (document.RegForm.member_id_valid_to_label.value == "") {
				alert("Member/Ref. Valid To Label is required");
				document.RegForm.member_id_valid_to_label.focus();
				return false;
			}
			return true;
		}

	</script>

</head>
<body onload="init();" class="yui-skin-sam">

<form name="RegForm" onsubmit="return validateForm();" action="RegistrationPreferences.do?method=update" method="POST" >
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="pr_no" value="${beanMap.pr_no}"/>
	</c:if>

	<h1>Registration Preferences </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Registration Preferences</legend>
		<table class="formtable" >
			<tr>

				<td class="formlabel">Old Reg. Field Name:</td>
				<td>
					<input type="text" name="old_reg_field_label" value="${beanMap.old_reg_field_label}" maxlength="50"/>
				</td>
				<td class="formlabel">Generate Case File No:</td>
				<td>
					<insta:selectoptions name="case_file_settings" optexts="Yes,No" opvalues="Y,N"
					value="${beanMap.case_file_settings}"/>
				</td>
				<td class="formlabel">Generate Token For OP:</td>
				<td>
					<insta:selectoptions name="op_generate_token" value="${beanMap.op_generate_token}"
					opvalues="Y,N" optexts="Yes,No"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Area Required:</td>
				<td>
					<insta:selectoptions name="area_field_validate" opvalues="N,A,I,O" optexts="No,Yes,For IP Only,For OP Only"
						value="${beanMap.area_field_validate}"/></td>
				<td class="formlabel">Next of Kin Required:</td>
				<td>
					<insta:selectoptions name="nextofkin_field_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.nextofkin_field_validate}"/>
				</td>
				<td class="formlabel">Address Required:</td>
				<td>
					<insta:selectoptions name="address_field_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.address_field_validate}"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Complaint Required:</td>
				<td><insta:selectoptions name="complaint_field_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.complaint_field_validate}"/></td>
				<td class="formlabel">Referred by Required:</td>
				<td><insta:selectoptions name="referredby_field_validate" opvalues="N,A,I,O,P"
					optexts="No,Yes,For IP Only,For OP Only,For Outside Only" value="${beanMap.referredby_field_validate }"/></td>
				<td class="formlabel">Consulting/Admitting Doctor Required:</td>
				<td class="forminfo">
					${beanMap.admitting_doctor_mandatory eq 'N'? 'No':'Yes'}
				</td>
			</tr>

			<tr>
				<td class="formlabel">Hospital Uses Units:</td>
				<td>
					<insta:selectoptions name="hosp_uses_units" optexts="Yes,No" opvalues="Y,N"
					value="${beanMap.hosp_uses_units}"/>
				</td>
				<td class="formlabel">Unit Determined:</td>
				<td>
					<insta:selectoptions name="dept_units_settings" optexts="--Select--,Manually,Based on Rules"
					opvalues=" ,M,R" value="${beanMap.dept_units_settings}"/>
				</td>
				<td class="formlabel">Registration Validity Period (in Days):</td>
				<td><input type="text" name="reg_validity_period" value="${beanMap.reg_validity_period}" class="number validate-decimal"/></td>
			</tr>

			<tr>
				<td class="formlabel">OP Screen Default Selection:</td>
				<td>
					<insta:selectoptions name="op_default_selection" optexts="-- Select --,New,Mr No"
						opvalues=" ,N,M" value="${beanMap.op_default_selection}"/>
				</td>
				<td class="formlabel">IP Screen Default Selection:</td>
				<td>
					<insta:selectoptions name="ip_default_selection" optexts="-- Select --,New,Mr No"
						opvalues=" ,N,M" value="${beanMap.ip_default_selection}"/>
				</td>
				<td class="formlabel">Outside Screen Default Selection:</td>	<%-- NOTE: keep empty td for empty cells --%>
				<td><insta:selectoptions name="outside_default_selection" optexts="-- Select --,New,Mr No"
						opvalues=" ,N,M" value="${beanMap.outside_default_selection}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Govt. Identifier Type Label</td>
				<td>
					<input type="text" name="government_identifier_type_label" value="${beanMap.government_identifier_type_label}"
					maxlength="200"/>
				</td>
				<td class="formlabel">Govt. Identifier Label:</td>
				<td>
					<input type="text" name="government_identifier_label" value="${beanMap.government_identifier_label}"
					maxlength="200"/>
				</td>
				<td class="formlabel">Email Id Required:</td>	<%-- NOTE: keep empty td for empty cells --%>
				<td>
					<insta:selectoptions name="validate_email_id" opvalues="N,A,I,O"
						optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.validate_email_id}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Issue to Dept./Indent on Registration:</td>
				<td>
					<insta:selectoptions name="issue_to_mrd_on_registration" optexts="Yes,No"
						opvalues="Y,N" value="${beanMap.issue_to_mrd_on_registration}"/>
					</select>
				</td>
				<td class="formlabel">Encounter Start and End Required: </td>
				<td class="forminfo">
					${beanMap.encntr_start_and_end_reqd eq 'NR'? 'Not Required':beanMap.encntr_start_and_end_reqd eq 'IP'? 'Only for IP':beanMap.encntr_start_and_end_reqd eq 'OP'?'Only for OP':'Required'}
					<input type="hidden" name ="encntr_start_and_end_reqd" value="${beanMap.encntr_start_and_end_reqd}" />
				</td>
				<td class="formlabel">Encounter Type Required: </td>
				<td class="forminfo">
					${beanMap.encntr_type_reqd eq 'NR'? 'Not Required':beanMap.encntr_type_reqd eq 'IP'? 'Only for IP':beanMap.encntr_type_reqd eq 'OP'?'Only for OP':'Required'}
					<input type="hidden" name ="encntr_type_reqd" value="${beanMap.encntr_type_reqd}" />
				</td>
			</tr>

			<tr>
				<td class="formlabel">Mobile No. Required: </td>
				<td class="forminfo">
					<insta:selectoptions name="patientphone_field_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.patientphone_field_validate}"/>
				</td>
				<td class="formlabel">Followup Rules based on: </td>
				<td class="forminfo">
					<insta:selectoptions name="visit_type_dependence" opvalues="D,S,A"
					optexts="Doctor,Department,Advanced" value="${beanMap.visit_type_dependence}"/>
					<img class="imgHelpText" title="Visit Type Dependence is used as a preference to select the Visit/OP type
										for a patient in registration i.e Main/FollowUp/Revisit is set according to the department selected
										if this is Department or set according to Doctor if this is set to Doctor
										if this is Advanced, then System will pick the visit type from followup rule and applicability masters."
					 src="${cpath}/images/help.png"/>
				</td>
				<td class="formlabel">OP-IP Conversion description:</td>
				<td><input type="text" name="default_op_ip_description" value="${beanMap.default_op_ip_description}"
					maxlength="100"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Member Id/Reference No. Label:</td>
				<td>
					<input type="text" name="member_id_label" value="${beanMap.member_id_label}" maxlength="200"/>
				</td>
				<td class="formlabel">Member/Ref. Valid From Label:</td>
				<td>
					<input type="text" name="member_id_valid_from_label" value="${beanMap.member_id_valid_from_label}" maxlength="200"/>
				</td>
				<td class="formlabel">Member/Ref. Valid To Label:</td>
				<td>
					<input type="text" name="member_id_valid_to_label" value="${beanMap.member_id_valid_to_label}" maxlength="200"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Allow Multiple Active OP Visits: </td>
				<td class="forminfo">
					<insta:selectoptions name="allow_multiple_active_visits" opvalues="Y,N"
					optexts="Yes,No" value="${beanMap.allow_multiple_active_visits}"/>
				</td>
				<td class="formlabel">Prior Auth. Required:</td>
				<td class="forminfo">
					<insta:selectoptions name="prior_auth_required" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.prior_auth_required}"/>
				</td>
				<td class="formlabel">Paste Option Required: </td>
				<td class="forminfo">
					<insta:selectoptions name="copy_paste_option" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.copy_paste_option}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Doctor E&M Codification Required: </td>
				<td class="forminfo">
					<insta:selectoptions name="doc_eandm_codification_required" opvalues="Y,N"
					optexts="Yes,No" value="${beanMap.doc_eandm_codification_required}"/>
				</td>
				<td class="formlabel">Default FollowUp E&M Code: </td>
				<td class="forminfo">
					<input type="text" name="default_followup_eandm_code" value="${beanMap.default_followup_eandm_code}" maxlength="15"/>
				</td>
				<td class="formlabel">Default OP Encounter Start type:</td>
				<td class="forminfo">
					<insta:selectdb displaycol="code_desc" table="encounter_start_types"
						valuecol="code" name="default_op_encounter_start_type"
						dummyvalue="-- Select --" value="${beanMap.default_op_encounter_start_type}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Default OP Encounter End type:</td>
				<td class="forminfo">
					<insta:selectdb displaycol="code_desc" table="encounter_end_types"
						valuecol="code" name="default_op_encounter_end_type"
						dummyvalue="-- Select --" value="${beanMap.default_op_encounter_end_type}"/>
				</td>
				<td class="formlabel">Default IP Encounter Start type:</td>
				<td class="forminfo">
					<insta:selectdb displaycol="code_desc" table="encounter_start_types"
						valuecol="code" name="default_ip_encounter_start_type"
						dummyvalue="-- Select --" value="${beanMap.default_ip_encounter_start_type}"/>
				</td>
				<td class="formlabel">Default IP Encounter End type:</td>
				<td class="forminfo">
					<insta:selectdb displaycol="code_desc" table="encounter_end_types"
						valuecol="code" name="default_ip_encounter_end_type"
						dummyvalue="-- Select --" value="${beanMap.default_ip_encounter_end_type}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Referal for life:</td>
				<td>
					<insta:selectoptions name="referal_for_life" opvalues="Y,N"
					optexts="Yes,No" value="${beanMap.referal_for_life}"/>
				</td>

				<td class="formlabel">Default Patient Category basis: </td>
				<td class="forminfo">
					${beanMap.patient_reg_basis eq 'P'? 'Patient':'Visit'}
					<input type="hidden" name ="patient_reg_basis" value="${beanMap.patient_reg_basis}" />
				</td>


			</tr>
			<tr>
				<td class="formlabel">Allow DRG/Perdiem: </td>
				<td class="forminfo">
					<insta:selectoptions name="allow_drg_perdiem" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.allow_drg_perdiem}"/>
				</td>
				<td class="formlabel">Follow Up determination across centers: </td>
				<td class="forminfo">
					<insta:selectoptions name="followup_across_centers" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.followup_across_centers}"/>
				</td>
				<td class="formlabel">Default visit details across centers: </td>
				<td class="forminfo">
					<insta:selectoptions name="default_visit_details_across_center" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.default_visit_details_across_center}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Unidentified Patient First Name: </td>
				<td class="forminfo">
					<input type="text" name="unidentified_patient_first_name" value="${beanMap.unidentified_patient_first_name}" maxlength="50" onblur="capWords(unidentified_patient_first_name)" />
				</td>
				<td class="formlabel">Unidentified Patient Last Name: </td>
				<td class="forminfo">
					<input type="text" name="unidentified_patient_last_name" value="${beanMap.unidentified_patient_last_name}" maxlength="40" onblur="capWords(unidentified_patient_last_name)" />
				</td>
				<td class="formlabel">Emergency Patient Department: </td>
				<td class="forminfo">
					<select id="emergency_patient_department_id"  name="emergency_patient_department_id" size="1" class="dropdown" >
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Enable District: </td>
				<td class="forminfo">
					<insta:selectoptions name="enable_district" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.enable_district}"/>
				</td>
				<td class="formlabel">Show Referral Doctor Filter </td>
				<td class="forminfo">
					<insta:selectoptions name="show_referrral_doctor_filter" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.show_referrral_doctor_filter}"/>
				</td>
				<td class="formlabel">Allow Auto Entry Of Areas </td>
				<td class="forminfo">
					<insta:selectoptions name="allow_auto_entry_of_area" opvalues="Y,N"
											optexts="Yes,No" value="${beanMap.allow_auto_entry_of_area}"/>
				</td>
			</tr>
			<tr>
            	<td class="formlabel">Patient Photo Mandatory: </td>
            	<td class="forminfo">
            		<insta:selectoptions name="patient_photo_mandatory" opvalues="Y,N"
            								optexts="Yes,No" value="${beanMap.patient_photo_mandatory}"/>
            	</td>
            	<td class="formlabel">Patient Outstanding Controls For OP Registration: </td>
            	<td class="forminfo">
            		<insta:selectoptions name="patient_outstanding_control" opvalues="Warn,Block"
            								optexts="Warn,Block" value="${beanMap.patient_outstanding_control}"/>
            		<img class="imgHelpText" title="Control on OP Registration to Warn/Block further visit registration if current outstanding > 0."
					 src="${cpath}/images/help.png"/>
            	</td>
            	<td class="formlabel">Plan Code Search For OP Registration: </td>
            	<td class="forminfo">
            		<insta:selectoptions name="plan_code_search" opvalues="Y,N"
            								optexts="Yes,No" value="${beanMap.plan_code_search}"/>
            		<img class="imgHelpText" title="Enable Search On Plan Code."
					 src="${cpath}/images/help.png"/>
            	</td>
            </tr>
			<tr>
            	<td class="formlabel">Patient data source where registration/renewal charges are to be skipped : </td>
            	<td class="forminfo">
            		<input type="text" name="no_reg_charge_sources" value="${beanMap.no_reg_charge_sources}" maxlength="300"/>
            	</td>
            	<td class="formlabel"><insta:ltext key="ui.label.registration.preference.last.name.required"></insta:ltext></td>
				<td class="forminfo">
					<insta:selectoptions name="last_name_required" opvalues="N,Y,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.last_name_required}"/>
				</td>
				<td class="formlabel"><insta:ltext key="ui.label.registration.preference.marital.status.required"></insta:ltext></td>
				<td class="forminfo">
					<insta:selectoptions name="marital_status_required" opvalues="N,Y,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.marital_status_required}"/>
				</td>
            </tr>
            <tr>
            	<td class="formlabel"><insta:ltext key="ui.label.registration.preference.religion.required"></insta:ltext></td>
				<td class="forminfo">
					<insta:selectoptions name="religion_required" opvalues="N,Y,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.religion_required}"/>
				</td>
				 	<td class="formlabel"><insta:ltext key="ui.label.registration.preference.close.previous.active.visit"></insta:ltext></td>
        				<td class="forminfo">
        					<insta:selectoptions name="close_previous_active_visit" opvalues="N,Y"
        					optexts="No,Yes" value="${beanMap.close_previous_active_visit}"/>
        				</td>
            </tr>
            <td class="formlabel">Diagnosis for OSP Registration : </td>
                  <td class="forminfo">
                    <insta:selectoptions name="diagnosis_for_osp_registration" opvalues="O,M"
                       optexts="Optional,Mandatory" value="${beanMap.diagnosis_for_osp_registration}"/>
                  </td>
             </tr>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder" style="width: 550px;">
	  <legend class="fieldSetLabel">Passport Fields</legend>
	  <div class="resultList" style="margin: 10px 0px 5px 0px;width: 550px;">
	   <table class="resultList" cellspacing="0" cellpadding="0" border="0" style="width: 550px;">
		<tr>
			<th style="width: 50px">Field</th>
			<th style="width: 30px">Name</th>
			<th style="width: 10px">Required</th>
			<th style="width: 20px">Display</th>
		</tr>
		<tr>
			<td>Passport No. Label</td>
			<td><input type="text" name="passport_no" value="${beanMap.passport_no}" maxlength="50"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="passport_no_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.passport_no_validate}"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="passport_no_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${beanMap.passport_no_show}"/></td>
		</tr>
		<tr>
			<td>Passport Validity Label</td>
			<td><input type="text" name="passport_validity" value="${beanMap.passport_validity}" maxlength="50"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="passport_validity_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.passport_validity_validate}"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="passport_validity_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${beanMap.passport_validity_show}"/></td>
		</tr>
		<tr>
			<td>Passport Issue Country Label</td>
			<td><input type="text" name="passport_issue_country" value="${beanMap.passport_issue_country}" maxlength="50"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="passport_issue_country_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.passport_issue_country_validate}"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="passport_issue_country_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${beanMap.passport_issue_country_show}"/></td>
		</tr>
		<tr>
			<td>Visa Validity Label</td>
			<td><input type="text" name="visa_validity" value="${beanMap.visa_validity}" maxlength="50"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="visa_validity_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.visa_validity_validate}"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="visa_validity_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${beanMap.visa_validity_show}"/></td>
		</tr>
	   </table>
	  </div>
	</fieldset>

	<fieldset class="fieldSetBorder" style="width: 660px;">
	  <legend class="fieldSetLabel">Nationality Id Field</legend>
	  <div class="resultList" style="margin: 10px 0px 5px 0px;width: 660px;">
	   <table class="resultList" cellspacing="0" cellpadding="0" border="0" style="width: 680px;">
		<tr>
			<th style="width: 50px">Field</th>
			<th style="width: 30px">Name</th>
			<th style="width: 10px">Required</th>
			<th style="width: 20px">Display</th>
		</tr>
		<tr>
			<td>Nationality Label</td>
			<td><input type="text" name="nationality" value="${beanMap.nationality}" maxlength="50"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="nationality_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.nationality_validate}"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="nationality_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${beanMap.nationality_show}"/></td>
		</tr>
	   </table>
	  </div>
	</fieldset>
	
	<fieldset class="fieldSetBorder" style="width: 680px;">
	  <legend class="fieldSetLabel">Family ID Field</legend>
	  <div class="resultList" style="margin: 10px 0px 5px 0px;width: 680px;">
	   <table class="resultList" cellspacing="0" cellpadding="0" border="0" style="width: 680px;">
		<tr>
			<th style="width: 50px">Field</th>
			<th style="width: 30px">Name</th>
			<th style="width: 10px">Required</th>
			<th style="width: 20px">Display</th>
		</tr>
		<tr>
			<td>Family ID Label</td>
			<td><input type="text" name="family_id" value="${beanMap.family_id}" maxlength="50"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="family_id_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${beanMap.family_id_validate}"/></td>
			<td><insta:selectoptions styleClass="selectClass" name="family_id_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${beanMap.family_id_show}"/></td>
		</tr>
	   </table>
	  </div>
	</fieldset>


	<fieldset class="fieldSetBorder" style="width: 680px;">
		<legend class="fieldSetLabel">Patient Details Custom Fields</legend>
		<div class="resultList" style="margin: 10px 0px 5px 0px;width: 680px;">
		<table class="resultList" cellspacing="0" cellpadding="0" border="0" style="width: 680px;">
		<tr bgcolor="#8FBC8F">
			<th style="width: 50px">Custom Field</th>
			<th style="width: 30px">Name</th>
			<th style="width: 10px">Required</th>
			<th style="width: 20px">Display</th>
		</tr>
		<c:forEach begin="1" end="9" var="i">
		 <tr>
			<c:set var="fieldName" value="custom_list${i}_name"/>
				<c:set var="fieldValue" value="${beanMap[fieldName]}"/>

			<c:set var="fieldValidate" value="custom_list${i}_validate"/>
				<c:set var="fieldValidateValue" value="${beanMap[fieldValidate]}"/>

			<c:set var="fieldShow" value="custom_list${i}_show"/>
				<c:set var="fieldShowValue" value="${beanMap[fieldShow]}"/>

			<td>Custom List ${i}</td>
			<td><input type="text" name="custom_list${i}_name" value="${fieldValue}" maxlength="50"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="custom_list${i}_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${fieldValidateValue}"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="custom_list${i}_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${fieldShowValue}"/></td>

		 </tr>
		</c:forEach>
		<tr style="background-color:#f2f5f9;cursor:default;color:#666;">
			<td colspan="7" style="background-color:#f2f5f9;cursor:default;color:#666;"></td>
		</tr>
		<c:forEach begin="1" end="19" var="i">
		<tr>
			<c:set var="fieldName" value="custom_field${i}_label"/>
				<c:set var="fieldValue" value="${beanMap[fieldName]}"/>

			<c:set var="fieldValidate" value="custom_field${i}_validate"/>
				<c:set var="fieldValidateValue" value="${beanMap[fieldValidate]}"/>

			<c:set var="fieldShow" value="custom_field${i}_show"/>
				<c:set var="fieldShowValue" value="${beanMap[fieldShow]}"/>

			<c:if test="${i eq 14}">
		 		<tr style="background-color:#f2f5f9;cursor:default;color:#666;">
					<td colspan="7" style="background-color:#f2f5f9;cursor:default;color:#666;"></td>
				</tr>
		 	</c:if>
		 	<c:if test="${i eq 17}">
		 		<tr style="background-color:#f2f5f9;cursor:default;color:#666;">
					<td colspan="7" style="background-color:#f2f5f9;cursor:default;color:#666;"></td>
				</tr>
		 	</c:if>
			<c:choose>
				<c:when test="${i le 13}">
		 			<td>Custom Field ${i}</td>
		 		</c:when>
		 		<c:when test="${i le 16}">
		 			<td>Custom Date Field ${i}</td>
		 		</c:when>
		 		<c:otherwise>
		 			<td>Custom Numeric Field ${i}</td>
		 		</c:otherwise>
		 	</c:choose>
			<td><input type="text" name="custom_field${i}_label" value="${fieldValue}" maxlength="50"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="custom_field${i}_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${fieldValidateValue}"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="custom_field${i}_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${fieldShowValue}"/></td>

		 </tr>
		</c:forEach>

	  </table>
	 </div>
	</fieldset>

	<fieldset class="fieldSetBorder" style="width: 680px;">
		<legend class="fieldSetLabel">Visit Details Custom Fields</legend>
		<div class="resultList" style="margin: 10px 0px 5px 0px;width: 680px;">
		<table class="resultList" cellspacing="0" cellpadding="0" border="0"  style="width: 680px;">
		<tr bgcolor="#8FBC8F">
			<th style="width: 50px">Custom Field</th>
			<th style="width: 30px">Name</th>
			<th style="width: 10px">Required</th>
			<th style="width: 20px">Display</th>
		</tr>
		<c:forEach begin="1" end="2" var="i">
		 <tr>
			<c:set var="fieldName" value="visit_custom_list${i}_name"/>
				<c:set var="fieldValue" value="${beanMap[fieldName]}"/>

			<c:set var="fieldValidate" value="visit_custom_list${i}_validate"/>
				<c:set var="fieldValidateValue" value="${beanMap[fieldValidate]}"/>

			<c:set var="fieldShow" value="visit_custom_list${i}_show"/>
				<c:set var="fieldShowValue" value="${beanMap[fieldShow]}"/>


			<td>Visit Custom List ${i}</td>
			<td><input type="text" name="visit_custom_list${i}_name" value="${fieldValue}" maxlength="50"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="visit_custom_list${i}_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${fieldValidateValue}"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="visit_custom_list${i}_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${fieldShowValue}"/></td>
		 </tr>
		</c:forEach>
		<tr style="background-color:#f2f5f9;cursor:default;color:#666;">
			<td colspan="6" style="background-color:#f2f5f9;cursor:default;color:#666;"></td>
		</tr>
		<c:forEach begin="1" end="9" var="i">
		<tr>
			<c:set var="fieldName" value="visit_custom_field${i}_name"/>
				<c:set var="fieldValue" value="${beanMap[fieldName]}"/>

			<c:set var="fieldValidate" value="visit_custom_field${i}_validate"/>
				<c:set var="fieldValidateValue" value="${beanMap[fieldValidate]}"/>

			<c:set var="fieldShow" value="visit_custom_field${i}_show"/>
				<c:set var="fieldShowValue" value="${beanMap[fieldShow]}"/>

			<c:if test="${i eq 4}">
		 		<tr style="background-color:#f2f5f9;cursor:default;color:#666;">
					<td colspan="7" style="background-color:#f2f5f9;cursor:default;color:#666;"></td>
				</tr>
		 	</c:if>
		 	<c:if test="${i eq 7}">
		 		<tr style="background-color:#f2f5f9;cursor:default;color:#666;">
					<td colspan="7" style="background-color:#f2f5f9;cursor:default;color:#666;"></td>
				</tr>
		 	</c:if>
			<c:choose>
				<c:when test="${i le 3}">
		 			<td>Visit Custom Field ${i}</td>
		 		</c:when>
		 		<c:when test="${i le 6}">
		 			<td>Visit Custom Date Field ${i}</td>
		 		</c:when>
		 		<c:otherwise>
		 			<td>Visit Custom Numeric Field ${i}</td>
		 		</c:otherwise>
		 	</c:choose>
			<td><input type="text" name="visit_custom_field${i}_name" value="${fieldValue}" maxlength="50"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="visit_custom_field${i}_validate" opvalues="N,A,I,O"
					optexts="No,Yes,For IP Only,For OP Only" value="${fieldValidateValue}"/></td>

			<td><insta:selectoptions styleClass="selectClass" name="visit_custom_field${i}_show" opvalues="M,D"
					optexts="Primary,Secondary" value="${fieldShowValue}"/></td>

		 </tr>
		</c:forEach>

	  </table>
	 </div>
	</fieldset>


	<div class="screenActions">
		<button type="submit" accesskey="S" class="button"><b><u>S</u></b>ave</button>
	</div>

</form>
<script>
	var jsCity = ${cityJson};
	var categoryJSON=${categoryWiseRateplans};
	var departments = ${department};

	function departmentsOptionsList() {
		var categorylist = document.getElementById("emergency_patient_department_id");
		var len = 0;
		var option;
		for (var i=0;i<departments.length;i++) {
			option = new Option(departments[i].dept_name, departments[i].dept_id);
			categorylist.options[len] =  option;
			len++;
			if ("${beanMap.emergency_patient_department_id}") {
				setSelectedIndex(document.forms[0].emergency_patient_department_id, "${beanMap.emergency_patient_department_id}");
			}
		}
	}

	function init() {
		departmentsOptionsList();
		focus();
	}
</script>
</body>
</html>
s