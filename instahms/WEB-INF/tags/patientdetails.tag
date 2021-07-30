<%@tag import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@tag import="com.insta.hms.master.CenterMaster.CenterMasterDAO"%>
<%@tag import="com.insta.hms.common.PhoneNumberUtil"%>
<%@tag import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@ tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="visitid" required="false" %>
<%@ attribute name="patient" required="false" type="java.util.Map"%>
<%@ attribute name="showClinicalInfo" required="false" %>
<%@ attribute name="fieldSetTitle" required="false" %>
<%@ attribute name="tableID" required="false" %>

<%--
	Example Usage:
	To display a patient demography , we would specify the tag as:
        <insta:patientdetails  visitid="${patient.patient_id}"/>
	OR, if you already have the patient details in the request as an attribute:
        <insta:patientdetails  patient="${patientdetails}"/>

Parameter :
 visitid :  the visitid of a patient
 addBox (optional): set to false if you dont want a box around, it will add only the rows.
--%>
<insta:link type="css" file="select2.min.css"/>
<insta:link type="css" file="select2Override.css"/>

<%
request.setAttribute("newLineChar", "\n");

 if (patient == null && visitid != null) {
	java.util.Map  patient =  com.insta.hms.Registration.VisitDetailsDAO.getPatientHeaderDetailsMap(visitid);
	request.setAttribute("patient", patient);
	if (patient != null &&  patient.get("patient_id") != null)
		request.setAttribute("patient_plan_details", new com.insta.hms.Registration.PatientInsurancePlanDAO().getVisitPlanSponsorsDetails((String)patient.get("patient_id")));

}

// Patient Custom Fields & List Values
com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO.patientDetailsCustomFields(showClinicalInfo);

// Visit Custom Fields & List Values
com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO.patientVisitDetailsCustomFields(showClinicalInfo);

java.util.Map  patient = null;
java.util.List allAllergies = null;

if (request.getAttribute("patient") != null) patient = (java.util.Map) request.getAttribute("patient");
if (patient != null) {
	allAllergies = com.insta.hms.outpatient.AllergiesDAO.getActiveAllergiesForPatient(patient.get("mr_no").toString());
}
request.setAttribute("allAllergies", allAllergies);
request.setAttribute("allAllergiesJSON", new flexjson.JSONSerializer().exclude("class").deepSerialize(com.insta.hms.common.ConversionUtils.copyListDynaBeansToMap(allAllergies)));
request.setAttribute("defaultCountryCode", new CenterMasterDAO().getCountryCode(0));
request.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
if ( patient != null && patient.get("patient_id") != null) {
	request.setAttribute("patient_plan_details", new com.insta.hms.Registration.PatientInsurancePlanDAO().getVisitPlanSponsorsDetails((String)patient.get("patient_id")));
}

%>
<c:set var="pat_generic_prefs" value='<%=com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getAllPrefs() %>'/>

<c:set var="cntxtpath" value="${pageContext.request.contextPath}" />
<script type="text/javascript">

if (window.addEventListener) {
    window.addEventListener('load', initPdDialogs, false);
} else if (window.attachEvent) {
    window.attachEvent('onload', initPdDialogs);
}

//YAHOO.util.Event.onContentReady("content", initEditPatientDetailsDialog);

function initPdDialogs(event) {
	initPatientPlanDetailsDialog();
    initPatientPhotoDialog();
    initEditPatientDetailsDialog();
}

var allAllergiesJSON = <%= request.getAttribute("allAllergiesJSON") %> ;
var edit_action_rights = '${actionRightsMap.edit_patient_header}';

var patientDetailTagRegPref = <%= request.getAttribute("patientDetailTagRegPref") %>;


var editPatDetailsDialog = null;
function initEditPatientDetailsDialog() {
	var dialogDiv = document.getElementById("edit_patdet_dialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	editPatDetailsDialog = new YAHOO.widget.Dialog("edit_patdet_dialog",
			{	width:"880px",
				context : ["edit_patdet_dialog", "tr", "br"],
				visible:false,
				//modal:true,
				fixedcenter: false,
				constraintoviewport:true,
				hideaftersubmit: false
			});
	YAHOO.util.Event.addListener('edit_patdet_Ok', 'click', updatePatientDetails, editPatDetailsDialog, true);
	YAHOO.util.Event.addListener('edit_patdet_Close', 'click', cancelEditPHDialog, editPatDetailsDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:editPatDetailsDialog.cancel,
	                                                scope:editPatDetailsDialog,
	                                                correctScope:true } );
	editPatDetailsDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	var handleSuccess = function(o) {
		if (o.responseText == 'true')
			editPatDetailsDialog.hide();
		else
			showMessage("js.patientdetails.common.failtoupdate.header");
	}
	var handleFailure = function(o) {
		showMessage("js.patientdetails.common.failtoupdate.header");
	};
	editPatDetailsDialog.callback = { success: handleSuccess,
                                      failure: handleFailure };
    editPatDetailsDialog.validate = function() {
    	var phonoNoEl = document.getElementById('eph_patient_phone');
    	var emailId = document.getElementById('eph_email_id');
	    if (phonoNoEl) {
	    	var phNo = phonoNoEl.value;
	    	if (phNo.length > 16) { // This includes prefix '+'
	    		showMessage("js.patientdetails.common.enter.lessdigits");
	    		return false;
	    	}
	    	if (null != phonoNoEl && trim(phonoNoEl.value) != '' ) {
		      	if($("#eph_patient_phone_valid").val() == 'N') {
		 				alert(getString("js.registration.patient.mobileNumber")+" "+
		 				getString("js.patientdetails.common.enter.govt.invalid.string"));
		 				$("#eph_patient_phone_national").focus();
		 	            return false;
		 	    }
		     }
	    	
	     /* if (mobile_phone_pattern != '')	{
	    	if (trim(phonoNoEl.value) == '') {
	    		showMessage("js.patientdetails.common.phone.no.required");
	    			phonoNoEl.focus();
					return false;
	    	}
	    	} */
	    }
	    if (!(FIC_checkField(" validate-email ", document.getElementById('eph_email_id')))) {
	            showMessage("js.patientdetails.common.enter.validemail");
	            emailId.focus();
	            return false;
	    }
	    return true;
    }

	editPatDetailsDialog.render();
}

function showEditPatDetailsDialog(obj) {
	var ptab = document.getElementById("tab_patient_details");
	var vtab = document.getElementById("tab_visit_details");
	var prows = ptab.rows;
	var vrows = vtab.rows;
	for (var i=0; i<ptab.rows.length; ) {
		ptab.deleteRow(ptab.rows[i]);
	}
	for (var i=0; i<vtab.rows.length; ) {
		vtab.deleteRow(vtab.rows[i]);
	}
	var url = cpath+'/PatientHeaderAction.do?_method=getHeaderDetails&showClinicalInfo=${showClinicalInfo}&visit_type=${patient.visit_type}&createToken=true';
	url += "&patientId=${patient.patient_id}";
	ajaxRequest = YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: onGetHeaderDetails,
				failure: onGetHeaderDetailsFailure
			}
	);

	editPatDetailsDialog.cfg.setProperty('context', [obj, 'tl', 'tr'], false);

	editPatDetailsDialog.show();
	return false;
}

function cancelEditPHDialog() {
	editPatDetailsDialog.cancel();
}

function updatePatientDetails() {
	editPatDetailsDialog.submit();
}

var roleId = ${roleId};
function onGetHeaderDetails(response) {
	if (response.responseText != undefined) {
		var details = eval('(' + response.responseText + ')');
		var patientDetails = details.patientDetails;
		var preferredLanguages = details.preferredLanguages;
		// retrieved patient details from the database everytime, to get the modified data(if updated from the dialog).
		var patientJSON = details.patientJSON;
		var row = null;
		var table = document.getElementById('tab_patient_details');
		document.getElementById('fsPatDetails').style.display = patientDetails.length == 0 ? 'none' : 'block';
		for (var i=0; i<patientDetails.length; i++) {
			if (i%3 == 0) {
				row = table.insertRow(-1);
			}
			var cell = row.insertCell(-1);
			cell.setAttribute("class", "formlabel");
			
			cell.innerHTML = patientDetails[i].field_desc + ': ';	
			var cell = row.insertCell(-1);
			cell.setAttribute("class", "forminfo");
			var fieldName = patientDetails[i].field_name;
			var value = patientJSON[fieldName];
			value = empty(value) ? '' :  value;
			var type = patientDetails[i].data_type;
			if (type == 'date') {
				if (!empty(value)){ 
					value = new Date(value);
					value.setTime( value.getTime() - new Date().getTimezoneOffset()*60*1000 );
					value = formatDate(value, 'ddmmyyyy', '-');
					}
			} else if (type == 'time') {
				if (!empty(value)) value = formatTime(new Date(value), false);
			} else if (type == 'timestamp') {
				if (!empty(value)) value = formatDateTime(new Date(value));
			} else {
				value = value; // no format required.
			}
			var displayTextField = (roleId == 1 || roleId == 2 || edit_action_rights == 'A') && (fieldName == 'patient_phone' || fieldName == 'email_id');
			
			if(displayTextField){
				if(fieldName == 'patient_phone'){
					cell.innerHTML = 
						'<div style="margin-top:12px;font-weight:normal">' +
									 '<div>' + 
										'<input type="hidden" id="eph_'+fieldName+'" name="eph_'+fieldName+'"/> ' +
										'<input type="hidden" id="eph_'+fieldName+'_valid" value="N"/>' +
										'<select id="eph_'+ fieldName + '_country_code" class="dropdown" style="width:76px" name="eph_'+fieldName+'_country_code">' +
											<c:if test="${empty defaultCountryCode}">				
													'<option value="+" selected> - Select - </option>' + 
											</c:if>
											<c:forEach items="${countryList}" var="list">
												<c:choose>
													<c:when test="${ list[0] == defaultCountryCode}"> 		
														'<option value="+${list[0]}" selected> +${list[0]}(${ list[1]})  </option>' + 
													</c:when>
													<c:otherwise> 
														'<option value="+${list[0]}"> +${list[0]}(${list[1]})  </option>' +	
													</c:otherwise>
												
												</c:choose>
																		  
											</c:forEach>
									 	'</select>' +
										
										 '<input type="text" class="field" id="eph_'+fieldName+'_national" maxlength ="15" onkeypress="return enterNumOnlyzeroToNine(event)"' +
												 'style="width:9.0em;padding-top:1px" />' +
											'<img class="imgHelpText" id="eph_'+fieldName+'_help" src="${pageContext.request.contextPath}/images/help.png"/>' +
									'</div>' +
									'<div>' +
										'<span style="visibility:hidden;padding-left:2px;color:#f00" id="eph_'+fieldName+'_error"></span>'	+
									'</div>' +

							'</div>';
							
					phoneNumberAction(value);
					
				}
				else{
					cell.innerHTML = '<input type="text" name="eph_'+fieldName+'" id="eph_'+fieldName+'" value="'+value+'"/>';
				}
		}else if(fieldName == "contact_pref_lang_code"){
					

					let selectElement = document.createElement('select');
					selectElement.name = 'eph_' + fieldName;
					selectElement.id = 'eph_' + fieldName;
					selectElement.className = "dropdown";

					preferredLanguages.forEach(function(language){
						let optElement = document.createElement('option');
						optElement.value = language.lang_code;
						optElement.defaultSelected = language.lang_code == patientJSON.contact_pref_lang_code;
						optElement.innerText = language.lang_name;
					    selectElement.add(optElement);
					});

					cell.innerHTML = selectElement.outerHTML;

		}
		else{
			cell.innerHTML = value;
		}
	}
		var visitDetails = details.visitDetails;
		var table = document.getElementById('tab_visit_details');
		document.getElementById('fsVisitDetails').style.display = visitDetails.length == 0 ? 'none' : 'block';
		row = null;
		for (var i=0; i<visitDetails.length; i++) {
			if (i%3 == 0) {
				row = table.insertRow(-1);
			}
			var cell = row.insertCell(-1);
			cell.setAttribute("class", "formlabel");
			cell.innerHTML = visitDetails[i].field_desc + ': ';

			var cell = row.insertCell(-1);
			cell.setAttribute("class", "forminfo");
			var value = patientJSON[visitDetails[i].field_name];
			value = empty(value) ? '' :  value;
			var type = visitDetails[i].data_type;
			if (type == 'date') {
				if (!empty(value)) value = formatDate(new Date(value), 'ddmmyyyy', '-');
			} else if (type == 'time') {
				if (!empty(value)) value = formatTime(new Date(value), false);
			} else if (type == 'timestamp') {
				if (!empty(value)) value = formatDateTime(new Date(value));
			} else {
				value = value; // no format required.
			}
			cell.innerHTML = value;
		}
		if( document.getElementById("_insta_transaction_token") ) {
			document.getElementById("_insta_transaction_token").value = details.token;
		}
	}
}

function onGetHeaderDetailsFailure() {
}

</script>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:js-bundle prefix="patientdetails.common"/>
<c:if test="${empty addBox}"><c:set var="addBox" value="true"/></c:if>
<c:if test="${empty fieldSetTitle}"><c:set var="fieldSetTitle" value="Patient Details"/></c:if>
<c:set var="viewPhoto">
	<insta:ltext key="patientdetails.common.tag.viewphoto"/>
</c:set>
<c:set var="male">
	<insta:ltext key="patientdetails.common.tag.male"/>
</c:set>
<c:set var="female">
	<insta:ltext key="patientdetails.common.tag.female"/>
</c:set>
<c:set var="couple">
	<insta:ltext key="patientdetails.common.tag.couple"/>
</c:set>
<c:set var="tpaSponsor">
	<insta:ltext key="patientdetails.common.tag.tpa.sponsor"/>
</c:set>
<c:set var="corporateName">
	<insta:ltext key="patientdetails.common.tag.corporatename"/>
</c:set>
<c:set var="sponsorName">
	<insta:ltext key="patientdetails.common.tag.sponsorname"/>
</c:set>
<c:set var="sponsorCo">
	<insta:ltext key="patientdetails.common.tag.tpa.sponsorco"/>
</c:set>
<c:set var="insuranceCo">
	<insta:ltext key="patientdetails.common.tag.insco"/>
</c:set>
<c:set var="corporateCo">
	<insta:ltext key="patientdetails.common.tag.corporateco"/>
</c:set>
<c:set var="title1">
	<insta:ltext key="patientdetails.common.tag.title1"/>
</c:set>
<c:set var="title2">
	<insta:ltext key="patientdetails.common.tag.title2"/>
</c:set>
<c:set var="title3">
	<insta:ltext key="patientdetails.common.tag.title3"/>
</c:set>
<c:set var="activeStatus">
	<insta:ltext key="patientdetails.common.tag.active"/>
</c:set>
<c:set var="inactiveStatus">
	<insta:ltext key="patientdetails.common.tag.inactive"/>
</c:set>

<fieldset class="fieldSetBorder hide-patient-details" style="margin-bottom: 5px;">
	<legend class="fieldSetLabel"><a name="_editPatDetailsAnchor" href="javascript:Edit" onclick="return showEditPatDetailsDialog(this);"
						title="Edit ${fieldSetTitle}">
						<img src="${cntxtpath}/images/dark_blue_patient.jpeg" class="button" style="width: 20px; height: 20px"/>
					</a></legend>
<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%" id="patientDetailsTab">
<tr>
	<td class="formlabel"><insta:ltext key="ui.label.mrno"/>:</td>
	<td class="forminfo">
		<div title="${patient.mr_no}">${patient.mr_no}
			<c:if test="${patient.patient_gender eq 'M' && patient.patient_photo_available eq 'Y' }">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/man-icon.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();" style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'M' && patient.patient_photo_available eq 'N' }">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/man-icon1.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();" style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'F' && patient.patient_photo_available eq 'Y' }">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/woman-icon.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();"  style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'F' && patient.patient_photo_available eq 'N' }">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/woman-icon1.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();"  style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'O' && patient.patient_photo_available eq 'Y'}">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/genericuser-icon.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();"  style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'O' && patient.patient_photo_available eq 'N'}">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/genericuser-icon1.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();"  style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'C' && patient.patient_photo_available eq 'Y'}">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/genericuser-icon.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();"  style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
			<c:if test="${patient.patient_gender eq 'C' && patient.patient_photo_available eq 'N'}">
				<img id="pd_viewPhotoIcon" title="${viewPhoto}" alt="view photo" src="${cntxtpath}/images/genericuser-icon1.png" onclick="initPatientPhotoDialog();showPatientPhotoDialog();"  style="vertical-align:middle;cursor:pointer;"/>
			</c:if>
		</div>
	</td>
	<td class="formlabel" title="${patient.vip_status=='Y' ? 'VIP' : ''}"><insta:ltext key="patientdetails.common.tag.name"/>:</td>
	<td class="forminfo ${patient.vip_status=='Y' ? 'vipIndicator' : ''}" title="${patient.vip_status=='Y' ? 'VIP' : ''}">
		<div title="${patient.full_name}">${patient.full_name}</div>
	</td>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.age.gender"/>:</td>
	<td class="forminfo">
		<c:if test="${not empty patient.dateofbirth}">
			<c:set var="date_of_birth">(<fmt:formatDate value="${patient.dateofbirth}" pattern="dd-MM-yyyy"/>)
			</c:set>
		</c:if>
		<c:if test="${not empty patient.patient_gender && patient.patient_gender == 'M'}">
			<c:set var="gender" value="${male}"/>
		</c:if>
		<c:if test="${not empty patient.patient_gender && patient.patient_gender == 'F'}">
			<c:set var="gender" value="${female}"/>
		</c:if>
		<c:if test="${not empty patient.patient_gender && patient.patient_gender == 'C'}">
			<c:set var="gender" value="${couple}"/>
		</c:if>
		<div title="${patient.age_text} / ${gender} ${date_of_birth}">
			${patient.age_text} ${date_of_birth} / ${gender}
		<div>
	</td>
</tr>
<tr>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.visitno"/>:</td>
	<td class="forminfo ${patient.mlc_status=='Y' ? 'mlcIndicator' : ''}">
		<c:set var="op_type">
			<c:if test="${not empty patient.op_type}">
				<c:choose>
					<c:when test="${patient.op_type eq 'M'}"></c:when>
					<c:when test="${patient.op_type eq 'F'}"><insta:ltext key="patientdetails.common.tag.followup"/></c:when>
					<c:when test="${patient.op_type eq 'D'}"><insta:ltext key="patientdetails.common.tag.followup.nocons"/></c:when>
					<c:when test="${patient.op_type eq 'R'}"><insta:ltext key="patientdetails.common.tag.revisit"/></c:when>
					<c:when test="${patient.op_type eq 'O'}"><insta:ltext key="patientdetails.common.tag.outside"/></c:when>
					<c:otherwise></c:otherwise>
				</c:choose>
			</c:if>
		</c:set>
		<c:set var="drg_patient">
			<c:if test="${not empty patient.use_drg && patient.use_drg == 'Y'}"><insta:ltext key="patientdetails.common.tag.drg"/></c:if>
		</c:set>
		<c:set var="perdiem_patient">
			<c:if test="${not empty patient.use_perdiem && patient.use_perdiem == 'Y'}"><insta:ltext key="patientdetails.common.tag.perdiem"/></c:if>
		</c:set>
		<div title="${patient.patient_id} ${op_type}${drg_patient}${perdiem_patient}">
			${patient.patient_id} ${op_type}${drg_patient}${perdiem_patient}
		</div>
	</td>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.dept"/>:</td>
	<td class="forminfo"><c:set var="dept" value="${patient.dept_name}"/>
		<c:if test="${not empty patient.unit_id}"><c:set var="unit" value="(${patient.unit_name})"/></c:if>
		<div title="${dept} ${unit}">${dept} ${unit}</div>
	</td>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.doctor"/>:</td>
	<td class="forminfo"><div title="${patient.doctor_name}">${patient.doctor_name}</div></td>
</tr>
<c:if test="${patient.visit_type eq 'i'}">
	<c:choose>
		<c:when test="${preferences.modulesActivatedMap['mod_ipservices'] eq 'Y'}">
			<%-- show the allocated bed, if not allocated, no need to show reg. bed type/ward --%>
			<c:choose>
				<c:when test="${empty patient.alloc_bed_name}"><c:set var="wardBed" value="(Not allocated)"/></c:when>
				<c:otherwise><c:set var="wardBed" value="${patient.alloc_ward_name}/${patient.alloc_bed_name}"/></c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise><c:set var="wardBed" value="${patient.reg_ward_name}/${patient.bill_bed_type}"/></c:otherwise>
	</c:choose>
</c:if>
<c:if test="${not empty patient.reg_date || not empty patient.refdoctorname || not empty wardBed}">
	<tr>
		<fmt:formatDate pattern="dd-MM-yyyy" value="${patient.reg_date}" var="reg_date"/>
		<fmt:formatDate pattern="HH:mm" value="${patient.reg_time}" var="reg_time"/>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.adm.regdate"/>:</td>
		<td class="forminfo"><div title="${reg_date} ${reg_time}">${reg_date} ${reg_time}</div></td>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.preferredby"/>:</td>
		<td class="forminfo"><div title="${patient.refdoctorname}">${patient.refdoctorname}</div></td>
		<c:if test="${patient.visit_type eq 'i'}">
			<td class="formlabel"><insta:ltext key="patientdetails.common.tag.ward.bed"/>:</td>
			<td class="forminfo"><div title="${wardBed}">${wardBed}</div></td>
		</c:if>
	</tr>
</c:if>
<c:if test="${showClinicalInfo == true && (not empty patient.complaint || not empty patient.primary_diagnosis_description ||
					not empty patient.secondary_diagnosis_description)}">
	<tr>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.complaint"/>: </td>
		<td class="forminfo"><div title="${patient.complaint}">${patient.complaint}</div></td>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.pri.diagnosis"/>: </td>
		<td class="forminfo"><div title="${patient.primary_diagnosis_description}">${patient.primary_diagnosis_description}</div></td>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.sec.diagnosis"/>: </td>
		<td class="forminfo"><div title="${patient.secondary_diagnosis_description}">${patient.secondary_diagnosis_description}</div></td>
	</tr>
</c:if>
<c:set var="noofplans" value="${fn:length(patient_plan_details)}"/>
<c:if test="${not empty patient_plan_details}">
	<c:set var="noofplans" value="${fn:length(patient_plan_details)}"/>
</c:if>
<c:set var="planIndex" value="${1-1}"/>
	<tr>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.rateplan"/>:</td>
		<td class="forminfo"><div title="${patient.org_name}">${patient.org_name}</div></td>

		<c:if test="${(not empty patient.primary_sponsor_id)}">

		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.tpa.sponsor"/>:</td>
		<td class="forminfo"><div title="${patient.tpa_name}">${patient.tpa_name}</div></td>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.insco"/>:</td>
		<td class="forminfo">
		<div title="${patient_plan_details[0].map.insurance_co_name}" style="display: inline">${patient_plan_details[0].map.insurance_co_name}</div>
			<c:if test="${(!empty screenId && screenId == 'credit_bill_collection') || (!empty screenId && screenId == 'new_prepaid_bills') || (!empty screenId && screenId == 'order') || (!empty screenId && screenId == 'edit_visit_details')}">
				<c:if test="${(not empty patient_plan_details[0].map.insurance_co_name) && (not empty patient_plan_details[0].map.insurance_rules_doc_name )}">
					<c:url var="insUrl" value="/master/InsuranceCompMaster.do">
						<c:param name="_method" value="getviewInsuDocument"/>
						<c:param name="inscoid" value="${patient.primary_insurance_co}"/>
					</c:url>
					<div style="display: inline">
						&nbsp;<a href="${insUrl}" title="Insurance Rules Document" id="imgLink" target="_blank">
								<img class="newWindow" id="newWindowImg" style="width: 12px;" src="${cntxtpath}/images/doc.gif"/>
						</a>
					</div>
				</c:if>
			</c:if>
		</td>
	</tr>

	<tr>
		<td class="formlabel">
			<c:choose>
				<c:when  test="${patient.primary_member_id_label != null && not empty patient.primary_member_id_label}">${patient.primary_member_id_label}:</c:when>
				<c:otherwise><insta:ltext key="patientdetails.common.tag.membershipid"/>:</c:otherwise>
			</c:choose>
		</td>
		<td class="forminfo">
				<div title="${patient_plan_details[0].map.member_id}">${patient_plan_details[0].map.member_id}</div>
		</td>
		<td class="formlabel">
			<c:choose>
				<c:when test="${patient.primary_plan_type_label != null && not empty patient.primary_plan_type_label}">${patient.primary_plan_type_label}:</c:when>
				<c:otherwise><insta:ltext key="patientdetails.common.tag.net.plantype"/>:</c:otherwise>
			</c:choose>
		</td>
		<td class="forminfo"><div title="${patient_plan_details[0].map.plan_type_name}">${patient_plan_details[0].map.plan_type_name}</div></td>
		<c:set var="planIndex" value="${noofplans}"/>
		<td class="formlabel"> <insta:ltext key="patientdetails.common.tag.planname"/>:</td>
		<td class="forminfo">
			<div title="${patient_plan_details[0].map.plan_name}" >
				<div style="float: left; width: 80%; padding-top: 5px;">${patient_plan_details[0].map.plan_name}</div>
				<div style="display: inline;">
					<button id="pd_planButton" title="${title2}" style="cursor:pointer;"
					onclick="javascript:initPatientPlanDetailsDialog();showPatientPlanDetailsDialog();" type="button" > .. </button>
				</div>
			</div>
		</td>
	</c:if>
	</tr>


<c:set var="planIndex" value="${planIndex == 0 ? ( noofplans >= 1 ? noofplans : 0) : ( noofplans > 1 ? noofplans : 0) }"/>
<c:if test="${(not empty patient.secondary_sponsor_id) }">
	<tr>
		<td class="formlabel"></td>
		<td class="forminfo"></td>
		<td class="formlabel"> <insta:ltext key="patientdetails.common.tag.sec"/>TPA/Sponsor:</td>
		<td class="forminfo"><div title="${patient.sec_tpa_name}">${patient.sec_tpa_name}</div></td>
		<td class="formlabel"><insta:ltext key="patientdetails.common.tag.insco"/>:</td>
        <td class="forminfo"><div title="${patient.sec_insurance_co_name}" style="display: inline">${patient.sec_insurance_co_name}</div>
        	<c:if test="${ (!empty screenId && screenId == 'credit_bill_collection') || (!empty screenId && screenId == 'new_prepaid_bills') || (!empty screenId && screenId == 'order') || (!empty screenId && screenId == 'edit_visit_details')}">
				<c:if test="${ (not empty patient_plan_details[noofplans-1].map.insurance_co_name) && (not empty patient_plan_details[noofplans-1].map.insurance_rules_doc_name)}">
					<c:url var="insUrl" value="/master/InsuranceCompMaster.do">
						<c:param name="_method" value="getviewInsuDocument"/>
						<c:param name="inscoid" value="${patient.secondary_insurance_co}"/>
					</c:url>
				<div style="display: inline">
					&nbsp;<a href="${insUrl}" title="Insurance Rules Document" id="imgLink" target="_blank">
						<img class="newWindow" id="newWindowImg" style="width: 12px;" src="${cntxtpath}/images/doc.gif"/>
					</a>
			   </div>
			</c:if>
		</c:if>
	</td>
	</tr>
	<tr>
		<td class="formlabel">
			<c:choose>
				<c:when  test="${patient.secondary_member_id_label != null && not empty patient.secondary_member_id_label}">${patient.secondary_member_id_label}:</c:when>
				<c:otherwise><insta:ltext key="patientdetails.common.tag.membershipid"/>:</c:otherwise>
			</c:choose>
		</td>
		<td class="forminfo">
				<div title="${patient_plan_details[noofplans-1].map.member_id}">${patient_plan_details[noofplans-1].map.member_id}</div>
		</td>
		<td class="formlabel">
				<c:choose>
					<c:when test="${patient.secondary_plan_type_label != null && not empty patient.secondary_plan_type_label}">${patient.secondary_plan_type_label}:</c:when>
					<c:otherwise><insta:ltext key="patientdetails.common.tag.net.plantype"/>:</c:otherwise>
				</c:choose>
		</td>
		<td class="forminfo">
				<div title="${patient_plan_details[noofplans-1].map.plan_type_name}">${patient_plan_details[noofplans-1].map.plan_type_name}</div>
		</td>
		<td class="formlabel"> <insta:ltext key="patientdetails.common.tag.planname"/>:</td>
		<td class="forminfo">
				<div title="${patient_plan_details[noofplans-1].map.plan_name}" >
					<div style="float: left; width: 80%; padding-top: 5px;">${patient_plan_details[noofplans-1].map.plan_name}</div>
					<div style="display: inline;">
						<button id="pd_planButton" title="${title2}" style="cursor:pointer;"
						onclick="javascript:initPatientSecPlanDetailsDialog();showPatientSecPlanDetailsDialog();" type="button" > .. </button>
					</div>
				</div>
		</td>
	</tr>
</c:if>

<c:if test="${not empty customFields}">
	<c:set var="index" value="0"/>
	<c:forEach var="field" items="${customFields}" varStatus="status">
		<c:if test="${not empty patient[field.txColumnName]}">
			<c:if test="${index%3 == 0}">
				<tr>
			</c:if>
				<td class="formlabel"><insta:truncLabel value="${field.label}" length="10"/>:</td>
				<td class="forminfo"><div title="${patient[field.txColumnName]}">${patient[field.txColumnName]}</div></td>
			<c:if test="${index%3 == 2 || status.last}">
				</tr>
			</c:if>
			<c:set var="index" value="${index+1}"/>
		</c:if>
	</c:forEach>
</c:if>

<c:set var="noAllergies" value=""/>
<c:set var="medAllergies" value=""/>
<c:set var="foodAllergies" value=""/>
<c:set var="otherAllergies" value=""/>

<c:set var="haveNoAllergies" value="false"/>
<c:set var="haveMedAllergies" value="false"/>
<c:set var="haveFoodAllergies" value="false"/>
<c:set var="haveOtherAllergies" value="false"/>

<c:forEach var="allergy_bean" items="${allAllergies}">
	<c:set var="allergy" value="${allergy_bean.map}"/>
	<c:if test="${allergy.status == 'A'}">
		<c:choose>
			<c:when test="${allergy.allergy_type == 'N'}">
				<c:if test="${haveNoAllergies}">
					<c:set var="noAllergies" value="${noAllergies},"/>	
				</c:if>
				<c:set var="noAllergies" value="${noAllergies}${allergy.allergy}"/>
				<c:set var="haveNoAllergies" value="true"/>
			</c:when>
			<c:when test="${allergy.allergy_type == 'M'}">
				<c:if test="${haveMedAllergies}">
					<c:set var="medAllergies" value="${medAllergies},"/>	
				</c:if>
				<c:set var="medAllergies" value="${medAllergies}${allergy.allergy}"/>
				<c:set var="haveMedAllergies" value="true"/>
			</c:when>
			<c:when test="${allergy.allergy_type == 'F'}">
				<c:if test="${haveFoodAllergies}">
					<c:set var="foodAllergies" value="${foodAllergies},"/>	
				</c:if>
				<c:set var="foodAllergies" value="${foodAllergies}${allergy.allergy}"/>
				<c:set var="haveFoodAllergies" value="true"/>
			</c:when>
			<c:when test="${allergy.allergy_type == 'O'}">
				<c:if test="${haveOtherAllergies}">
					<c:set var="otherAllergies" value="${otherAllergies},"/>	
				</c:if>
				<c:set var="otherAllergies" value="${otherAllergies}${allergy.allergy}"/>
				<c:set var="haveOtherAllergies" value="true"/>
			</c:when>
		</c:choose>
	</c:if>
</c:forEach>



<c:if test="${showClinicalInfo == true && (haveNoAllergies || haveMedAllergies || haveFoodAllergies || haveOtherAllergies)}">
	<insta:js-bundle prefix="patientdetails"/>
	<tr>
		<td class="formlabel"><b><font id="allergiesLabel" style="color: red"><insta:ltext key="patientdetails.common.tag.allergies"/>:</font></b></td>
		<td colspan="8" class="forminfo" id="AllergiesColId">
			<c:if test="${haveNoAllergies}">  <insta:ltext key="patientdetails.common.tag.noallergies"/> ${noAllergies}</c:if>
			<c:if test="${haveMedAllergies}">  <insta:ltext key="patientdetails.common.tag.med"/> ${medAllergies}</c:if>
			<c:if test="${haveFoodAllergies}"> / <insta:ltext key="patientdetails.common.tag.food"/> ${foodAllergies}</c:if>
			<c:if test="${haveOtherAllergies}"> / <insta:ltext key="patientdetails.common.tag.other"/> ${otherAllergies}</c:if>
		</td>
	</tr>
	<script>
		var allergyColor = 'red';
		for (var i=0 ; i<allAllergiesJSON.length; i++) {
			if (allAllergiesJSON[i].allergy_type == 'N' && allAllergiesJSON[i].status == 'A')
				allergyColor = 'black';
		}
		document.getElementById('allergiesLabel').style.color = allergyColor;
		Insta.Tooltip = {
			ttipObj: null,
		};
		function createAllergyTooltip() {
			var allergyMessage = '' ;
			for (var i=0 ; i<allAllergiesJSON.length; i++ ) {
					allergyMessage = allergyMessage +getString("js.patientdetails.common.tag.allergy")+ allAllergiesJSON[i].allergy + "</br>" +
									 getString("js.patientdetails.common.tag.onset")+ allAllergiesJSON[i].onset_date + "</br>" +
									 getString("js.patientdetails.common.tag.reaction")+ allAllergiesJSON[i].reaction + "</br>" +
									 getString("js.patientdetails.common.tag.status")+ (allAllergiesJSON[i].status == 'A' ? 'Active' : 'Inactive') + "</br>" +
									 "---------------------------------------" + "</br>";
			}
			Insta.Tooltip.ttipObj = new YAHOO.widget.Tooltip("tooltip",
			{ 	context: 'AllergiesColId',
				text:allergyMessage,
				hidedelay: 0,
				showdelay: 1000,
				autodismissdelay: 10000
			} );
		}
		createAllergyTooltip();
	</script>
</c:if>

<c:if test="${not empty visitCustomFields}">
	<c:set var="index" value="0"/>
	<c:forEach var="field" items="${visitCustomFields}" varStatus="status">
		<c:if test="${not empty patient[field.txColumnName]}">
			<c:if test="${index%3 == 0}">
				<tr>
			</c:if>
				<td class="formlabel"><insta:truncLabel value="${field.label}" length="10"/>:</td>
				<td class="forminfo"><div title="${patient[field.txColumnName]}">${patient[field.txColumnName]}</div></td>
			<c:if test="${index%3 == 2 || status.last}">
				</tr>
			</c:if>
			<c:set var="index" value="${index+1}"/>
		</c:if>
	</c:forEach>
</c:if>


<tr style="height:0px">
<td colspan="6">
<div id="edit_patdet_dialog" style="display: none">
	<div class="bd">
		<form name="eph_DetailsForm" action="${cntxtpath}/PatientHeaderAction.do?_method=update" method="POST">
			<input type="hidden" name="eph_mr_no" value="${patient.mr_no}"/>
			<input type="hidden" name="eph_patient_id" value="${patient.patient_id}"/>
			<fieldset class="fieldSetBorder" id="fsPatDetails">
				<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.patientdetails"/></legend>
				<table class="formtable" id="tab_patient_details">

				</table>
			</fieldset>
			<fieldset class="fielSetBorder" id="fsVisitDetails">
				<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.visitdetails"/></legend>
				<table class="formtable" id="tab_visit_details">

				</table>
			</fieldset>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" id="edit_patdet_Ok" value="Ok"/>
						<input type="button" id="edit_patdet_Close" value="Close"/>
					</td>
				</tr>
			</table>
		</form>
	</div>
</div>
</td>
</tr>

	</table>
</fieldset>

<c:if test="${!addBox}">
<tr id="plnTr" class="yui-skin-sam">
<td>
</c:if>

	<div id="patientPlanDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientPlanDetailsDialogCancel();">
		<div class="bd" id="bd1" style="padding-top: 0px;">
			<table class="formTable" align="center" id="pd_planDialogTable" style="width:480px;">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
						<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patientdetails.common.tag.plansummary"/></legend>
								<table class="formTable" align="center" style="width:480px;">
									<tr>
										<td>
											<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
												<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.exclusions"/></legend>
												<c:if test="${not empty patient.primary_sponsor_id && noofplans >= 1}">
												<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">${fn:replace(patient_plan_details[0].map.plan_exclusions, newLineChar, "<br />")}</p>
												</c:if>
											 </fieldset>
										</td>
									</tr>
									<tr>
										<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.notes"/></legend>
											<c:if test="${not empty patient.primary_sponsor_id && noofplans >= 1}">
											<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;"> ${fn:replace(patient_plan_details[0].map.plan_notes, newLineChar, "<br />")}</p>
											</c:if>
										</fieldset>
										</td>
									</tr>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientPlanDetailsDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="patientSecPlanDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientSecPlanDetailsDialogCancel();">
		<div class="bd" id="bd1" style="padding-top: 0px;">
			<table class="formTable" align="center" id="pd_secplanDialogTable" style="width:480px;">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
						<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patientdetails.common.tag.plansummary"/></legend>
								<table class="formTable" align="center" style="width:480px;">
									<tr>
										<td>
											<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
												<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.exclusions"/></legend>
												<c:if test="${not empty patient.secondary_sponsor_id && planIndex >= 0}">
												<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">${fn:replace(patient_plan_details[noofplans-1].map.plan_exclusions, newLineChar, "<br />")}</p>
												</c:if>
											 </fieldset>
										</td>
									</tr>
									<tr>
										<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.notes"/></legend>
											<c:if test="${not empty patient.secondary_sponsor_id && planIndex >= 0}">
											<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;"> ${fn:replace(patient_plan_details[noofplans-1].map.plan_notes, newLineChar, "<br />")}</p>
											</c:if>
										</fieldset>
										</td>
									</tr>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientSecPlanDetailsDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="patientPhotoDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientPhotoDialogCancel();">
		<div class="bd" id="bd2" style="padding-top: 0px;">
			<table  style="text-align:top;vetical-align:top;" width="100%">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
							<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.patient.photo"/></legend>
									<c:choose>
										<c:when test="${patient.patient_photo_available eq 'Y' }">
											<img id="pd_patientImage" alt="No Patient Photo Available" src="${cntxtpath}/Registration/GeneralRegistrationPatientPhoto.do?_method=viewPatientPhoto&mrno=${patient.mr_no}"/>
										</c:when>
										<c:otherwise>
											<insta:ltext key="patientdetails.common.tag.nophoto.available"/>
										</c:otherwise>
									</c:choose>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientPhotoDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

<c:if test="${!addBox}">
</td>
</tr>
</c:if>
<insta:link type="js" file="select2.min.js"/>
<insta:link type="js" file="phoneNumberUtil.js"/>
<script type="text/javascript">
var departmentId = '${patient.dept_id}';
var patientGender = '${patient.patient_gender}';
function phoneNumberAction(value){
	
	var patientPhone = $("#eph_patient_phone");		
	var patientPhoneNational=$("#eph_patient_phone_national");
	var patientPhoneCountryCode=$("#eph_patient_phone_country_code");
	var patientPhoneHelp=$("#eph_patient_phone_help");
	var patientPhoneError =$("#eph_patient_phone_error");
	var patientPhoneValid = $("#eph_patient_phone_valid");
	
	var defaultCountryCode = '+${defaultCountryCode}';
	
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
	// Get help text for patient_phone
	getExamplePhoneNumber(defaultCountryCode,patientPhoneHelp,patientPhoneError);

	//set country and national number of patient_phone

	insertNumberIntoDOM(value,patientPhone,patientPhoneCountryCode,
			patientPhoneNational);
	
}
</script>






