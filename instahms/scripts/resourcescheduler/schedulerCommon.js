//common js file for scheduler,using by schedulerdayview.jsp and todaysappointment.jsp.


var default_gp_first_consultation = null,
	default_gp_revisit_consultation = null,
	default_sp_first_consultation = null,
	default_sp_first_consultation = null;
	var gPreviousDocVisits = null;
	var gFollowUpDocVisits = null;
	var gPatientLastIpVisit = null;

/*var priAuthIdObj = getPrimaryAuthIdObj();
var priAuthModeIdObj = getPrimaryAuthModeIdObj();

var secPlanObj = getSecondaryPlanObj();
var secAuthIdObj = getSecondaryAuthIdObj();
var secAuthModeIdObj = getSecondaryAuthModeIdObj();

if (isModAdvanceIns && priPlanObj != null && priPlanObj.value != ''
		&& !empty(priorAuthRequired) && trim(priAuthIdObj.value) == ""
		&& (priorAuthRequired=="A" || (priorAuthRequired=="I" && screenid == "ip_registration")
			|| (priorAuthRequired=="O" && screenid == "reg_registration" ))) {
	showMessage("js.registration.patient.prior.auth.no.required");
	document.getElementById('prior_auth_id').focus();
	return false;
}

function getPrimaryAuthIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_prior_auth_id");
	}
	return null;
}
f
function getSecondaryAuthIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_prior_auth_id");
	}
	return null;
}

function getPrimaryAuthModeIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_prior_auth_mode_id");
	}
	return null;
}

function getSecondaryAuthModeIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_prior_auth_mode_id");
	}
	return null;
}

if (priAuthModeIdObj != null) {
	if(!validatePriorAuthMode(null, null, priAuthIdObj.name, priAuthModeIdObj.name))
	return false;
}*/

var orgId = "ORG0001";
var orgName = "General";

// Set active visits globally. on visit change use the visit type to load consultations and
// also validate op, ip applicable tests, services, operations.
// Bed, ward and rate plan are also used when the active visit is selected.
var gActiveVisits;
var gAppointmentDetails

function getActiveVisits(mrno,appointmentId) {

	gActiveVisits = null;
	gAppointmentDetails = null;

	if(modInsExt == 'Y')
		return gActiveVisits;

	if (mrno != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/resourcescheduler/addeditappointments.do?method=getPatientVisitDetailsJSON'
						+ '&mrno=' + mrno;
		if(!empty(appointmentId))
			url += "&appointment_id="+appointmentId;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {

					eval("schPatientInfo =" + ajaxobj.responseText);

					if (!empty(schPatientInfo.activeVisits) && schPatientInfo.activeVisits.length != 0)
						gActiveVisits = schPatientInfo.activeVisits;
					if(!empty(schPatientInfo.appointmentDetails))
						gAppointmentDetails = schPatientInfo.appointmentDetails;
				}
			}
		}
	}
	return gActiveVisits;
}

function setDoctorChargeBasedOnPractitionForSchedulerScreen(doctorId, docChrgObj, isFirstVisit, isIpFollowUp) {
	var doctor = findInList(scheduleResourceListJSON, "resource_id", doctorId);
	if (!empty(doctor)) {
		var practition_type = doctor.practition_type;
		if (!empty(practition_type)) {
			if (practition_type == 'G') {
				if (isFirstVisit) {
					if (!empty(default_gp_first_consultation))
						setSelectedIndex(docChrgObj, default_gp_first_consultation);
					else setSelectedIndex(docChrgObj, '-1');
				} else {
					if (typeof(isIpFollowUp) == 'undefined') {
						if (!empty(default_gp_revisit_consultation))
							setSelectedIndex(docChrgObj, default_gp_revisit_consultation);
						else setSelectedIndex(docChrgObj, '-2');
					} else {
						if (isIpFollowUp) setSelectedIndex(docChrgObj, '-4');
						else setSelectedIndex(docChrgObj, '-2');
					}
				}
			} else if (practition_type == 'S') {
				if (isFirstVisit) {
					if (!empty(default_sp_first_consultation))
						setSelectedIndex(docChrgObj, default_sp_first_consultation);
					else setSelectedIndex(docChrgObj, '-1');
				} else {
					if (typeof(isIpFollowUp) == 'undefined') {
						if (!empty(default_sp_revisit_consultation))
							setSelectedIndex(docChrgObj, default_sp_revisit_consultation);
						else setSelectedIndex(docChrgObj, '-2');
					} else {
						if (isIpFollowUp)
							setSelectedIndex(docChrgObj, '-4');
						else setSelectedIndex(docChrgObj, '-2');
					}
				}
			} else {
				if (isFirstVisit)
					setSelectedIndex(docChrgObj, '-1');
				else {
					if (typeof(isIpFollowUp) == 'undefined')
						setSelectedIndex(docChrgObj, '-2');
					else {
						if (isIpFollowUp)
							setSelectedIndex(docChrgObj, '-4');
						else setSelectedIndex(docChrgObj, '-2');
					}
				}
			}
		} else {
			if (isFirstVisit)
				setSelectedIndex(docChrgObj, '-1');
			else {
				if (typeof (isIpFollowUp == 'undefined'))
					setSelectedIndex(docChrgObj, '-2');
				else {
					if (isIpFollowUp)
						setSelectedIndex(docChrgObj, '-4');
					else setSelectedIndex(docChrgObj, '-2');
				}
			}
		}
	} else {
		setSelectedIndex(docChrgObj, '');
	}
}


function getRegDetailsForSchedulerSreen(formname) {
	var mrNo;
	if(formname == "schedulerMrnoForm") {
		formname = document.schedulerMrnoForm;
		mrNo = mr_no;
	} else {
		mrNo = rform.mrno.value;
	}
	var visitId = (!empty(rform.app_visit_id.value)) ? (rform.app_visit_id.value) : '';
	var screenid = null;
	if (!empty(rform.visitType.value) && rform.visitType.value == 'i') {
		screenid = "ip_registration";
	} else if(!empty(rform.visitType.value) && rform.visitType.value == 'o') {
		screenid = "new_op_registration";
	} else if(!empty(rform.visitType.value) && rform.visitType.value == 'O'){
		screenid = "out_pat_reg";
	}
	var ajaxobj1 = newXMLHttpRequest();
	var url = cpath + '/pages/registration/regUtils.do?_method=getPatientDetailsJSON&mrno=' + mrNo;
	url = url + '&reg_screen_id='+screenid;
	url = url+'&patient_id='+visitId;
	getResponseHandlerText(ajaxobj1, patientDetailsResponseHandler, url.toString());
}

function patientDetailsResponseHandler(responseText) {

	clearPreviousPatientDetailsForSchedulerScreen();

	eval("patientInfo =" + responseText);
	gPatientLastIpVisit = patientInfo.patientLastIpVisit;
	gFollowUpDocVisits = patientInfo.followUpDocVisits;
}

function clearPreviousPatientDetailsForSchedulerScreen() {
	gPatientLastIpVisit = null;
	gFollowUpDocVisits = null;
	gPreviousDocVisits = null;
}

/* Get the patient previous visits for the selected doctor */

function getPatientDoctorVisitsForSchedulerScreen(doctor,formname) {
	var mrNo;
	if(formname == "schedulerMrnoForm") {
		formname = document.schedulerMrnoForm;
		mrNo = mr_no;
	} else {
		mrNo = rform.mrno.value;
	}
	if (mrNo != null && mrNo != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getPatientDoctorVisits&mrNo=' + mrNo + '&doctor=' + doctor;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var visits =" + ajaxobj.responseText);
					if (!empty(visits)) {
						gPreviousDocVisits = visits;
					}else
						gPreviousDocVisits = null;
				}
			}
		}
	}
}

function setVisitType(doctorId, gPreviousDocVisits) {

	if (doctorId == null) return false;

	var doctor = findInList(scheduleResourceListJSON, 'resource_id', doctorId);
	if (doctor == null) return false;
	var dept = doctor.dept_id;

	if (empty(gPreviousDocVisits)) return false;

	var validityDays = doctor.op_consultation_validity;
	var maxVisits = doctor.allowed_revisit_count;

	var revisitCount = 0;
	var visitWithinValidity = false;
	var mainVisitId = null;
	var k = 0;
	for (var i = 0; i < gPreviousDocVisits.length; i++) {
		var cons = gPreviousDocVisits[i];

		// Based on visit type dependence (Doctor/Speciality) the op-type is determined.
		if ((visitTypeDependence == 'D' && doctorId == cons.doctor_name) || (visitTypeDependence == 'S' && dept == cons.dept_name)) {
			var visitDate = new Date(cons.visited_date);
			revisitCount++;
			if (daysDiff(visitDate, getServerDate()) <= validityDays) {
				visitWithinValidity = true;
				// Choose the latest doctor visit for setting the main visit id.
				if (k == 0) {
					mainVisitId = cons.main_visit_id;
				}
				k++;
			}
		}
	}

	return visitWithinValidity && (revisitCount <= maxVisits);
}


function setDocRevistCharges(doctorId,doctor,docChrgObj,formname) {
	getPatientDoctorVisitsForSchedulerScreen(doctorId,formname);
	// OP follow up for IP visit
	if (isRevisitAfterDischarge(doctorId, gPatientLastIpVisit, gFollowUpDocVisits)) {
		setDoctorChargeBasedOnPractitionForSchedulerScreen(doctorId,docChrgObj, false, true); // isFirstVisit = false, isIpFollowUp = true, default IP follow up consultation
	}
	// Doctor no previous visits -- Main Visit
	if (empty(gPreviousDocVisits)) {
		setDoctorChargeBasedOnPractitionForSchedulerScreen(doctorId,docChrgObj, true); // isFirstVisit = true, default OP Main visit consultation
	} else {
		// Doctor has no validity days or count -- Main Visit
		if (doctor == null || empty(doctor.op_consultation_validity) || empty(doctor.allowed_revisit_count)) {
			setDoctorChargeBasedOnPractitionForSchedulerScreen(doctorId,docChrgObj, true); // isFirstVisit = true, default OP Main visit consultation
			// Doctor visit not within validity -- Revisit
		} else if (!setVisitType(doctorId, gPreviousDocVisits)) {
			setDoctorChargeBasedOnPractitionForSchedulerScreen(doctorId,docChrgObj, true); // isFirstVisit = true, default OP Main visit consultation
			// Doctor visit within validity -- Follow Up Visit with consultation
		} else {
			setDoctorChargeBasedOnPractitionForSchedulerScreen(doctorId,docChrgObj, false); // isFirstVisit = false, default OP Follow up consultation
		}
	}

	// Note: Follow Up Visit without consultation -- user needs to select manually
}

function loadConsultationTypes(list) {
	var consSelect = document.getElementById('consultationTypes');
	var consVal = consSelect.value;
	loadSelectBox(consSelect, list,
		'consultation_type', 'consultation_type_id', '-- Select --', '');
	setSelectedIndex(consSelect, consVal);
}

function getPatientAppointmentDetails(appointmentId) {
	var reqObject = newXMLHttpRequest();
	var url = "../../pages/resourcescheduler/addeditappointments.do?method=getAppointmentDetails&appointmentId=" + appointmentId;
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			eval("var list =" + reqObject.responseText);
			var obj = list.appntDetailsList;
			if (obj != null && obj != '') {
				return obj[0];
			}
		}
	}
	return null;
}

function getConsultationTypes(orgId, centerId, patientType) {
		var ajaxobj = newXMLHttpRequest();
		var url = '../../pages/resourcescheduler/docappointments.do?method=getConsultationTypesForScheduler&orgId=' + orgId + '&centerId=' + centerId + '&patientType=' + patientType;
		ajaxobj.open("POST", url, false);
		ajaxobj.send(null);

		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var consultationTypes = " + ajaxobj.responseText);
					return consultationTypes;
				}
			}
		}
		return null;
}