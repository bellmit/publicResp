function hotKeys() {
	/**
	* Keylistener for the document (key Alt + Shift + N).
	* toggles the Visit Custom Fields Collapsible Panel.
	*/
	var addVisitInfoKeyListener = new YAHOO.util.KeyListener(document, { alt:true, shift: true, keys:78 },
					{ fn:toggleVisitCollapsiblePanel,
					scope:VisitCollapsiblePanel1,
					correctScope:true} );
	addVisitInfoKeyListener.enable();
}

function init() {
	hotKeys();
	var dept = document.mainform.dept_name.value;
	setDeptAllowedGender(dept);
	initDoctorDept(dept);
	if (regPref !== null) {
  	if (regPref.showReferralDoctorFilter == 'Y') {
    		initRegistrationReferralDoctorFilterAutocompletes();
    }
  }

	if (document.getElementById("unit_id") != null) {
		loadDeptUnit(dept);
	}

	referalDoctorAutoComplete('referred_by', 'referaldoctorName', 'referalNameContainer');
	if (document.mainform.bed_type != null) {
		var bedObj = document.getElementById('bed_type');
		var wardObj = document.getElementById('ward_id');
		populateBedTypes();
		setSelectedIndex(bedObj, bedtype);
		document.getElementById('bedTypelbl').textContent = bedtype;
		onChangeBedType(bedObj, wardObj);
		setSelectedIndex(wardObj, wardid);
	}
	onChangeMLCDoc();
	sortDropDown(document.mainform.dept_name);
	getPatientDoctorVisits(document.mainform.doctor.value);

	// Visit Custom fields
	getVisitCustomFieldList();
}

function populateBedTypes(){
	 var obj = document.getElementById('bed_type');
	 var generalOrgBeds = filterList(bedCharges, 'organization', 'ORG0001');
	 loadSelectBox(obj, generalOrgBeds, 'bedtype', 'bedtype', '--Select--');
}

function isVisitEmpty() {
	var visitId = document.patientSearch.patient_id.value;
	if (visitId == null || visitId == '') return true;
	else return false;
}

function checkRevisiting(){
	if (isVisitEmpty() == true) {
		if(document.mainform.previous_visit_id.value == ""
			|| (document.mainform.previous_visit_id.value != "" && document.mainform.previous_visit_id.value == gVisitId)){
			if(document.mainform.revisit[0].checked){
				var ok = confirm("This patient doesn't have any previous visits to mark it as Revisit. \n Do you want to mark as revisit?");
				if (!ok) {
					document.mainform.revisit[0].checked = false;
					document.mainform.revisit[1].checked = true;
				}
			}
		}
	}
}
/* commented for fix of HMS-24689.
function setEstablishedType(obj) {
	var deptId = obj.value;
	if( prevDeptsJSON != '' ) {
		var estChangd = false;
		for(i=0; i<prevDeptsJSON.length; i++) {
			var prevDept = prevDeptsJSON[i];
			if(prevDept['dept_name'] == deptId) {
				estChangd = true;
				setSelectedIndex(document.getElementById('established_type'), 'E');
			}
		}
		if(!estChangd) {
			setSelectedIndex(document.getElementById('established_type'), 'N');
		}
	} else {
		setSelectedIndex(document.getElementById('established_type'), 'N');
	}
}
*/
function onChangeDepartment() {
	var deptId = document.mainform.dept_name.value;
	//setEstablishedType(document.mainform.dept_name); --HMS-24689
	document.mainform.dept_allowed_gender.value = '';
	setDeptAllowedGender(deptId);

	document.mainform.doctor.value = '';
	document.mainform.doctor_name.value = '';
	initDoctorDept(deptId);
	if (document.mainform.unit_id != null) {
		document.mainform.unit_id.selectedIndex = 0;
		document.mainform.unit_id.length = 1;
		loadDeptUnit(deptId);
	}
}

function loadDeptUnit(dept) {
	if (dept == null || dept == '' && document.mainform.unit_id != null) {
		document.getElementById("unit_id").selectedIndex = 0;
		document.getElementById("unit_id").length = 1;
		return false;
	}

	var unitSelect = document.getElementById("unit_id");

	if (unitSelect != null) {
		var unitid = unitSelect.value;
		loadDepartmentUnit(unitSelect, dept);
		setSelectedIndex(unitSelect, unitid);
	}
}

function checkRatePlanValidity() {
	var type = "RatePlan";
	var rateplan = document.mainform.org_id.value;

	if (rateplan == '') {
		showMessage("js.registration.editvisitdetails.selectrateplan");
		document.mainform.org_id.focus();
		return false;
	}

	if (rateplan != '') {
		return validityCheck(type, rateplan);
	}
	return true;
}
function validityCheck(type, id) {
	var url = cpath+'/pages/registration/regUtils.do?_method=ajaxCheckValidity&type='+type+'&id='+id;
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.open("POST",url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ( (ajaxobj.status == 200) && (ajaxobj.responseText!=null) ) {
				eval("var isValid = " + ajaxobj.responseText);
				if(isValid != null && isValid == 'true') {
					return true;
			 	} else {
			 		var msg=type;
			 		msg+=getString("js.registration.editvisitdetails.validityexpired.currentdate");
			 		alert(msg);
			 		return false;
			 	}
			}
		}
	}
	return true;
}

function onChangeMLCDoc() {
	var template = document.mainform.mlc_template.options[document.mainform.mlc_template.options.selectedIndex];
	if (template != null && template.value != '') {
		document.mainform.mlc_template_name.value = template.id;
		document.mainform.mlc_type.disabled = false;
		document.mainform.accident_place.disabled = false;
		document.mainform.police_stn.disabled = false;
		document.mainform.mlc_remarks.disabled = false;
		document.mainform.certificate_status.disabled = false;
	} else {
		document.mainform.mlc_template_name.value = '';
		document.mainform.mlc_type.disabled = true;
		document.mainform.accident_place.disabled = true;
		document.mainform.police_stn.disabled = true;
		document.mainform.mlc_remarks.disabled = true;
		document.mainform.certificate_status.disabled = true;
	}
}

function getBillAndPaymentStatus() {
	var visitId = document.mainform.patient_id.value;
	var url = cpath+'/pages/registration/regUtils.do?_method=ajaxBillAndPaymentStatus&patient_id='+visitId+'&visit_type='+visittype;
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.open("POST",url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ( (ajaxobj.status == 200) && (ajaxobj.responseText!=null) ) {
				eval("var billAndPaymentDetails = "+ajaxobj.responseText);
				return billAndPaymentDetails;
			}
		}
	}
	return null;
}

function closeVisit() {
	var closed = document.getElementById("close").checked;
	var closeVisit = document.getElementById("dischargeOrcloseVisit");
	if (closed) {
		var billAndPaymentDetails = getBillAndPaymentStatus();
		if(billAndPaymentDetails == null || billAndPaymentDetails == '') {
			var check = confirm("Patient has no bills created,Are you sure you want to close the visit?");
			if (check) {
				closeVisit.value = "Y";
				return true;
			} else {
				closeVisit.value = "N";
				document.getElementById("close").checked = false;
				return false;
			}
		}else {
			var bill_status_ok = billAndPaymentDetails.bill_status_ok;
			var payment_ok = billAndPaymentDetails.payment_ok;
			if (bill_status_ok && payment_ok) {
				showMessage("js.registration.editvisitdetails.wanttoclosethevisit");
				closeVisit.value = "Y";
				return true;
			}else {
				var ok = confirm(getString("js.registration.editvisitdetails.billsarenotclosed.wanttoclosethevisit"));
				if (ok) {
					closeVisit.value = "Y";
					return true;
				}
				else {
				 closeVisit.value = "N";
				 document.getElementById("close").checked = false;
				 return false;
				}
			}
		}
	}else {
		closeVisit.value = "N";
	}
	return true;
}

function dischargePatient() {
	var discharged = document.getElementById("discharge").checked;
	var dischargePatient = document.getElementById("dischargeOrcloseVisit");
	if (discharged) {
		var billAndPaymentDetails = getBillAndPaymentStatus();
		if(billAndPaymentDetails == null || billAndPaymentDetails == '') {
			dischargePatient.value = "N";
			showMessage("js.registration.editvisitdetails.patienthasnobillscreated");
			return false;
		}else {
			var bill_status_ok = billAndPaymentDetails.bill_status_ok;
			var payment_ok = billAndPaymentDetails.payment_ok;
			if (bill_status_ok && payment_ok) {
				showMessage("js.registration.editvisitdetails.wanttodischargethepatient");
				dischargePatient.value = "Y";
				return true;
			}else {
				var ok = confirm("Bill(s) are not Closed,Are you sure you want to discharge the patient?");
				if (ok) {
					dischargePatient.value = "Y";
					return true;
				}
				else {
				 document.getElementById("discharge").checked = false;
				 dischargePatient.value = "N";
				 return false;
				}
			}
		}
	}else {
		dischargePatient.value = "N";
	}
	return true;
}

function saveVisitdetails() {

	var valid = true;
	var mrno = document.mainform.mrno.value;
	if (mrno == null || mrno == '') {
		showMessage("js.registration.editvisitdetails.selectvisitid.editthevisitdetails");
		return false;
	}
	onChangeMLCDoc();

	var doctorObj  = document.mainform.doctor_name;
	var bedtypeObj = document.mainform.bed_type;
	var wardObj    = document.mainform.ward_id;
	var deptObj    = document.mainform.dept_name;
	var deptValue  = document.getElementById('dept_name').options[document.getElementById('dept_name').selectedIndex].text;
	document.mainform.new_dept_name.value = deptValue;
	var regDateObj = document.mainform.reg_date;
	var regTimeObj = document.mainform.reg_time;
	var opTypeObj  = document.getElementById('op_type');
	var docMandatory = document.getElementById('docMandatory');
  var referaldoctorValidate = document.mainform.referredbyValidate.value;
  var referaldoctorName = document.getElementById("referaldoctorName").value;
  var visitStatus = document.getElementById('visitStatus').value;

 
	if(visittype == 'o' && document.mainform.encounter_type){
		const encounterType = (document.mainform.encounter_type != undefined) ? document.mainform.encounter_type.value : undefined;
		if ((encounterType == null || encounterType == '')) {
			if (visitStatus !== 'I') {
				showMessage("js.registration.patient.encounter.type.required");
				return false;
			}
		}
	}

	if (visittype == 'i') {
		valid = valid && validateRequired(doctorObj, getString("js.registration.editvisitdetails.doctorisrequired"));
		if(!valid) {
			doctorObj.focus();
			return false;
		}
		valid = valid && validateRequired(bedtypeObj, getString("js.registration.editvisitdetails.bedtypeisrequired"));
		if(!valid) {
			bedtypeObj.focus();
			return false;
		}
		valid = valid && validateRequired(wardObj, getString("js.registration.editvisitdetails.wardisrequired"));
		if(!valid) {
			wardObj.focus();
			return false;
		}
		if (((referaldoctorValidate == 'I'|| referaldoctorValidate == 'A')) && referaldoctorName.trim() == '') {
      showMessage("js.registration.patient.referral.required");
      document.mainform.referaldoctorName.focus();
      return false;
		}
	}

	if (opTypeObj && opTypeObj.value != 'O') {
		valid = valid && validateRequired(deptObj, getString("js.registration.editvisitdetails.departmentisrequired"));
		if(!valid) {
			deptObj.focus();
			return false;
		}
		if(docMandatory.value == 'Y') {
			valid = valid && validateRequired(doctorObj, getString("js.registration.editvisitdetails.doctorisrequired"));
			if(!valid) {
				doctorObj.focus();
				return false;
			}
		}
		if (((referaldoctorValidate == 'P'|| referaldoctorValidate == 'A')) && referaldoctorName.trim() == '') {
      showMessage("js.registration.patient.referral.required");
      document.mainform.referaldoctorName.focus();
      return false;
    }
	}

	valid = valid && validateRequired(regDateObj, getString("js.registration.editvisitdetails.registrationdateisrequired"));
	if(!valid) {
		regDateObj.focus();
		return false;
	}

	valid = valid && validateRequired(regTimeObj, getString("js.registration.editvisitdetails.registrationtimeisrequired"));
	if(!valid) {
		regTimeObj.focus();
		return false;
	}

	if (!doValidateDateField(regDateObj, 'past')) {
		regDateObj.focus();
		return false;
	}

	if (!validateTime(regTimeObj)) {
		regTimeObj.focus();
		return false;
	}

	if(trim(regTimeObj.value.length)!=5){
		showMessage("js.registration.editvisitdetails.incorrecttimeformat.enter.hh.mi");
		regTimeObj.focus();
		return false;
	}

	if (!validateDeptAllowedGender()) return false;

	var myDate = new Date();
	var currDate= formatDueDate(myDate);

	if (wardObj != null) wardObj.disabled = false;

	if (!validateOPtype()) return false;

	if (!validateCustomVisitFields()) return false;

	if (!validateCreditLimitLength()) return false;

	if (!validateCreditLimitAmount()) return false;

  if(!onWardChang()) return false;

	document.mainform.submit();
	return true;
}

function validateCreditLimitAmount() {
	var availableCreditLimitObj = document.getElementById('ip_credit_limit_amount');
	if(visittype != 'i' || availableCreditLimitObj == null || creditLimitDetailsJSON == undefined || creditLimitDetailsJSON == null) {
		return true;
	}
	var patientDue = creditLimitDetailsJSON.visitPatientDue;
	var availableDeposit = creditLimitDetailsJSON.availableDeposit;
	var availableCreditLimit = availableCreditLimitObj.value;
	var patientAndDepositDiff = (parseFloat(availableDeposit) - parseFloat(patientDue))

	if(availableCreditLimit < patientAndDepositDiff ) {
		alert(getString("js.registration.patient.creditlimit.cannot.less")+formatAmountValue(patientAndDepositDiff));
		document.getElementById('ip_credit_limit_amount').focus();
		return false;
	}

	return true;
}

function validateCreditLimitLength() {
	var availableCreditLimitObj = document.getElementById('ip_credit_limit_amount');
	if(visittype != 'i' || availableCreditLimitObj == null) {
		return true;
	}

	var availableCreditLimit = availableCreditLimitObj.value;
	if(availableCreditLimit == null || availableCreditLimit == "") {
		alert(getString("js.registration.patient.creditlimit.cannot.empty"));
		document.getElementById('ip_credit_limit_amount').focus();
		return false;
	}

	var noOfDigits = getNumberOfDigits(availableCreditLimit);
	if(noOfDigits > 13) {
		alert(getString("js.registration.patient.creditlimit.cannot.moredigits"));
		document.getElementById('ip_credit_limit_amount').focus();
		return false;
	}
	return true;
}

function getNumberOfDigits(availableCreditLimit) {
	var decimalsBeforeDot = (availableCreditLimit.split(".")[0]);

	if(decimalsBeforeDot.indexOf("-") != -1) {
		return decimalsBeforeDot.length - 1;
	} else {
		return decimalsBeforeDot.length;
	}
}

function validateCustomVisitFields() {
	if (visitFieldsList.length > 0) {
		for (var i = 0; i < visitFieldsList.length; i++) {
			var custfieldObj = customVisitFieldValidation(visittype.toUpperCase(), visitFieldsList[i]);
			if (custfieldObj != null) {
				if (VisitCollapsiblePanel1.isOpen()) {
					setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
				} else {
					VisitCollapsiblePanel1.open();
					setTimeout("document.mainform." + custfieldObj + ".focus()", 800);
				}
				return false;
			}
		}
	}
	return true;
}

function checkTPAValidity() {
        var type = "Sponsor";
        var regdate = document.mainform.reg_date.value;
        var sponsor = document.mainform.tpa_id.value;

        /*if (sponsor == '') {
                alert("Please select TPA");
                document.mainform.tpa_id.focus();
                return false;
        }*/
        if (sponsor != '') {
                return validityCheck(type, regdate, sponsor);
        }
        return true;
}

function getDoctor(doctorId) {
	if (empty(doctorId)) return null;

	var doctor = findInList(doctorsList, 'doctor_id', doctorId);
	return doctor;
}

function getDepartment(deptId) {
	if (empty(deptId)) return null;

	var dept = findInList(deptList, 'dept_id', deptId);
	return dept;
}

function getOpTypeText() {
	var opTypeObj = document.mainform.op_type;
	if (opTypeObj != null) {
		return opTypeObj.options[opTypeObj.options.selectedIndex].text;
	}
	return "";
}

/* Get the patient previous visits for the selected doctor */
var gPreviousDocVisits = null;

function getPatientDoctorVisits(doctor) {
	var mrno = document.mainform.mrno.value;
	if (mrno != null && mrno != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getPatientDoctorVisits&mrNo=' + mrno + '&doctor=' + doctor;
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

function isVisitWithinValidity() {
	var doctorId = document.mainform.doctor.value;
	var doctor = getDoctor(doctorId);
	if (doctor == null) return false;
	var dept = doctor.dept_id;

	var validityDays = doctor.op_consultation_validity;
	var maxVisits = doctor.allowed_revisit_count;

	var revisitCount = 0;
	var visitWithinValidity = false;
	for (var i = 0; i < gPreviousDocVisits.length; i++) {
		var cons = gPreviousDocVisits[i];

		// Based on visit type dependence (Doctor/Speciality) the op-type is determined.
		if ((visitTypeDependence == 'D' && doctorId == cons.doctor_name) || (visitTypeDependence == 'S' && dept == cons.dept_name)) {
			var visitDate = new Date(cons.visited_date);
			revisitCount++;
			if (daysDiff(visitDate, getServerDate()) <= validityDays) {
				visitWithinValidity = true;
			}
			if (!visitWithinValidity)
				break;
		}
	}
	return visitWithinValidity && (revisitCount <= maxVisits);
}

/* Get the visit doctor consultations */

function getVisitConsultations(visitId) {
	var docConsultations = null;
	if (!empty(visitId)) {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getVisitConsultations&visitId=' + visitId ;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var doctorCons =" + ajaxobj.responseText);
					if (!empty(doctorCons)) {
						docConsultations = doctorCons;
					}else
						docConsultations = null;
				}
			}
		}
	}
	return docConsultations;
}

function validateDoctorConsultation(oldMainVisit, newDoctor) {
	var doctor = getDoctor(newDoctor);

	var docConsultations = getVisitConsultations(oldMainVisit.patient_id);

	if (empty(docConsultations)) {
		var msg=getString("js.registration.editvisitdetails.thepatientprevious.mainvisitdetails");
		msg+=getString("js.registration.editvisitdetails.visitdate");
		msg+=formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.hasnodoctorconsultations");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.cannotchangeto");
		msg+=" ";
		msg+=getOpTypeText();
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.resettingvisittypeback.originalvisittype");
		alert(msg);
		return false;
	}else {
		var oldMainVisitDoctorCons = getConsultation(docConsultations, newDoctor);

		if (empty(oldMainVisitDoctorCons)) {
			var msg=getString("js.registration.editvisitdetails.thepatientprevious.mainvisitdetails");
			msg+=getString("js.registration.editvisitdetails.visitdate");
			msg+=formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-");
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.doctor");
			msg+=getDoctor(oldMainVisit.doctor_name).doctor_name;
			msg+="\n\n";
			msg+=getString("js.registration.editvisitdetails.nodoctorconsultation.withdoctor");
			msg+=getDoctor(oldMainVisit.doctor_name).doctor_name;
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.cannotchangeto");
			msg+=" ";
			msg+=getOpTypeText();
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.resettingvisittypeback.originalvisittype");
			alert(msg);
			return false;
		}else {
			if (!empty(oldMainVisit.cancel_status) && oldMainVisit.cancel_status == 'C') {
				var msg=getString("js.registration.editvisitdetails.thepatientprevious.mainvisitdetails");
				msg+="\n";
				msg+=getString("js.registration.editvisitdetails.visitdate");
				msg+=formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-");
				msg+="\n";
				msg+=getString("js.registration.editvisitdetails.doctor");
				msg+=getDoctor(oldMainVisit.doctor_name).doctor_name;
				msg+="\n\n";
				msg+=getString("js.registration.editvisitdetails.visitdoctorconsultation.cancelled");
				msg+="\n";
				msg+="\n";
				msg+=getString("js.registration.editvisitdetails.cannotchangeto");
				msg+=" ";
				msg+=getOpTypeText();
				msg+="\n";
				msg+=getString("js.registration.editvisitdetails.resettingvisittypeback.originalvisittype");
				alert(msg);
				return false;
			}
		}
	}
	return true;
}

function getPatientPreviousMainVisit(doctor) {
	var previousDocMainVisits = null;
	var mrno = document.mainform.mrno.value;
	if (mrno != null && mrno != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getPatientPreviousMainVisits&mrNo=' + mrno + '&doctor=' + doctor;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var visits =" + ajaxobj.responseText);
					if (!empty(visits)) {
						previousDocMainVisits = visits;
					}else
						previousDocMainVisits = null;
				}
			}
		}
	}
	return previousDocMainVisits;
}

function validateDoctorDepartemnt() {
	var newDoctor = document.mainform.doctor.value;
	var doctor = getDoctor(newDoctor);

	var opTypeObj = document.mainform.op_type;
	if (opTypeObj != null && (opTypeObj.value == 'F' || opTypeObj.value == 'D')) {
		if (empty(doctor)) {
			showMessage("js.registration.editvisitdetails.consultingdoctorisrequired");
			document.mainform.doctor_name.focus();
			return false;
		}
	}

	if (!empty(gDoctorId) && empty(doctor)) {
		var ok = confirm(getString("js.registration.editvisitdetails.nodoctorisselected.wanttoproceed"));
		if (!ok) {
			return false;
		}
	}

	var newDept = !empty(doctor) ? doctor.dept_id : document.mainform.dept_name.value;

	if (opTypeObj != null && (opTypeObj.value == 'F' || opTypeObj.value == 'D')) {
		if (visitTypeDependence == 'D') {
			if (!empty(gDoctorId) && !empty(newDoctor)) {
				if (gDoctorId != newDoctor) {
					var pdocName = getDoctor(gDoctorId).doctor_name;
					var docName = getDoctor(newDoctor).doctor_name;
					var msg=getString("js.registration.editvisitdetails.selecteddoctor");
					msg+=docName;
					msg+=getString("js.registration.editvisitdetails.notsameastheadmittingdoctor");
					msg+=pdocName;
					alert(msg);
					return false;
				}
			}
		}else if (visitTypeDependence == 'S') {
			if (!empty(gDeptId) && !empty(newDept)) {
				if (gDeptId != newDept) {
					var pdeptName = getDepartment(gDeptId).dept_name;
					var deptName = getDepartment(newDept).dept_name;
					var msg=getString("js.registration.editvisitdetails.selecteddepartment");
					msg+=deptName;
					msg+=getString("js.registration.editvisitdetails.notsameastheadmittingdepartment");
					msg+=pdeptName;
					alert(msg);
					return false;
				}
			}
		}
	}

	if (empty(gDoctorId) && !empty(newDoctor)) {
		var ok = confirm("Do you want the selected doctor to be the Admitting Doctor for the Visit?");
		if (!ok) {
			return false;
		}
	}
	return true;
}

function validateOPtype() {
	var opTypeObj = document.mainform.op_type;
	var doctorObj = document.mainform.doctor;

	var patientId = document.mainform.patient_id.value;

	if (opTypeObj == null) return true;

	if (empty(gOptype)) return true;

	// Validate Op Type for OP patient only.
	if (opTypeObj != null && visittype == 'o') {

		var valid = true;
		valid = valid && validateDoctorDepartemnt();
		valid = valid && validateSelectedOpType();
		return valid;
	}

	return true;
}

function validateSelectedOpType(newOpType, newDoctor) {
	var opTypeObj = document.mainform.op_type;
	var doctorObj = document.mainform.doctor;

	var newOpType = (opTypeObj != null) ? opTypeObj.value : null;
	var newDoctor = (doctorObj != null) ? doctorObj.value : null;

	var doc = getDoctor(newDoctor);
	var newDoctorName = doc != null ? doc.doctor_name : "";

	if (gOptype == newOpType) {
		setSelectedIndex(document.mainform.op_type, gOptype);
		document.mainform.main_visit_id.value = gMainVisitId;
		return true;
	}

	if (gOptype == 'M') {

		switch (newOpType) {
			case ('F') : {
				if (!validateMainToFollowUp(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('D') : {
				if (!validateMainToFollowUpNoCons(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('R') : {
				if (!validateMainToRevisit(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('M') : {
				return true;
				break;
			}
		}
	}else if (gOptype == 'F') {

		switch (newOpType) {
			case ('D') : {
				if (!validateFollowUpToFollowUpNoCons(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('R') : {
				if (!validateFollowUpToRevisit(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('M') : {
				if (!validateFollowUpToMain(opTypeObj, newDoctor))
					return false;
				break;
			}
			case ('F') : {
				return true;
				break;
			}
		}
	}else if (gOptype == 'D') {

		switch (newOpType) {
			case ('F') : {
				if (!validateFollowUpNoConsToFollowUp(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('R') : {
				if (!validateFollowUpNoConsToRevisit(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('M') : {
				if (!validateFollowUpNoConsToMain(opTypeObj, newDoctor))
					return false;
				break;
			}
			case ('D') : {
				return true;
				break;
			}
		}
	}else if (gOptype == 'R') {

		switch (newOpType) {
			case ('M') : {
				if (!validateRevisitToMain(opTypeObj))
					return false;
				break;
			}
			case ('F') : {
				if (!validateRevisitToFollowUp(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('D') : {
				if (!validateRevisitToFollowUpNoCons(opTypeObj, newDoctor, newDoctorName))
					return false;
				break;
			}
			case ('R') : {
				return true;
				break;
			}
		}
	}
	return true;
}

function getMainVisit(previousVisits) {
	var oldMainVisit = null;

	if (!empty(previousVisits)) {
		for (var i=0;i<previousVisits.length;i++) {
			var patVisit = previousVisits[i];
			if (patVisit.op_type == "M" || patVisit.op_type == "R") {
				oldMainVisit = patVisit;
				break;
			}
		}
	}
	return oldMainVisit;
}

function getConsultation(docConsultations, newDoctor) {
	var oldMainVisitDoctorCons = null;
	var doctor = getDoctor(newDoctor);
	if (doctor == null) return null;
	var newDept = doctor.dept_id;

	for (var i=0;i<docConsultations.length;i++) {
		var docCons = docConsultations[i];
		// Based on visit type dependence (Doctor/Speciality) the op-type is determined.
		if ((visitTypeDependence == 'D' && docCons.doctor_id == newDoctor) || (visitTypeDependence == 'S' && docCons.dept_id == newDept)) {
			oldMainVisitDoctorCons = docCons;
			break;
		}
	}
	return oldMainVisitDoctorCons;
}

function isPreviousVisitDoctorExists(docVisits) {
	if (empty(docVisits)) {
		var msg=getString("js.registration.editvisitdetails.patienthasnodoctor.previousvisit");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.cannotchangeto");
		msg+=" ";
		msg+=getOpTypeText();
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.resettingvisittypeback.originalvisittype");
		alert(msg);
		return false;
	}
	return true;
}

function isPreviousMainExists(previousVisits, newDoctorName) {
	if (empty(previousVisits)) {
		if (visitTypeDependence == 'D') {
			var msg=getString("js.registration.editvisitdetails.patienthasnopreviousmainvisit.thedoctor");
			msg+=newDoctorName;
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.cannotchangeto");
			msg+=" ";
			msg+=getOpTypeText();
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.resettingvisittypeback.originalvisittype");
			alert(msg);
		}else {
			var msg=getString("js.registration.editvisitdetails.patienthasnopreviousmainvisit.thedepartment");
			msg+=document.mainform.dept_name.options[document.mainform.dept_name.options.selectedIndex].text;
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.cannotchangeto");
			msg+=" ";
			msg+=getOpTypeText();
			msg+="\n";
			msg+=getString("js.registration.editvisitdetails.resettingvisittypeback.originalvisittype");
			alert(msg);
		}
		return false;
	}
	return true;
}

/********  Main to others *****************/
function validateMainToFollowUp(opTypeObj, newDoctor, newDoctorName) {
	var patientPreviousMainVisits = getPatientPreviousMainVisit(newDoctor);

	if (!isPreviousMainExists(patientPreviousMainVisits, newDoctorName)) {
		setSelectedIndex(opTypeObj,"M");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}
	var oldMainVisit = getMainVisit(patientPreviousMainVisits);
	var mainVisitId = oldMainVisit.main_visit_id;

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"M");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else if (!isVisitWithinValidity(patientPreviousMainVisits, newDoctor)) {

		var msg=getString("js.registration.editvisitdetails.patientpreviousmainvisitas ");
		msg+=mainVisitId;
		msg+=".";
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.mainvisitvalidityexpired");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.cannotchangeto");
		msg+=" ";
		msg+=getOpTypeText();
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.resettingvisittypebacktomain");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.themainvisitdetailsare");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.visitid");
		msg+=oldMainVisit.patient_id;
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.doctor");
		msg+=newDoctorName;
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.visiteddate");
		msg+=formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-");
		alert(msg);
		setSelectedIndex(opTypeObj,"M");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else {

		var ok = confirm("Main Visit will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"M");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = mainVisitId;
		}
	}
	return true;
}


function validateMainToFollowUpNoCons(opTypeObj, newDoctor, newDoctorName) {
	var patientPreviousMainVisits = getPatientPreviousMainVisit(newDoctor);

	if (!isPreviousMainExists(patientPreviousMainVisits, newDoctorName)) {
		setSelectedIndex(opTypeObj,"M");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}
	var oldMainVisit = getMainVisit(patientPreviousMainVisits);
	var mainVisitId = oldMainVisit.main_visit_id;

	//if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		//setSelectedIndex(opTypeObj,"M");
		//document.mainform.main_visit_id.value = gMainVisitId;
		//return false;

	//}else {
		var ok = confirm("Main Visit will be converted to "+ getOpTypeText()+".Are you sure? "
						+"\nPlease cancel doctor consultation.");
		if (!ok) {
			setSelectedIndex(opTypeObj,"M");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = mainVisitId;
		}
	//}
	return true;
}

function validateMainToRevisit(opTypeObj, newDoctor, newDoctorName) {
	var patientPreviousMainVisits = getPatientPreviousMainVisit(newDoctor);

	if (!isPreviousMainExists(patientPreviousMainVisits, newDoctorName)) {
		setSelectedIndex(opTypeObj,"M");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}
	var oldMainVisit = getMainVisit(patientPreviousMainVisits);
	var mainVisitId = oldMainVisit.main_visit_id;

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"M");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else if (!isVisitWithinValidity(patientPreviousMainVisits, newDoctor)) {

		var ok = confirm("Main Visit will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"M");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = mainVisitId;
		}

	}else {
		var ok = confirm("Warning: The Main visit has validity."
				+"\nThe Main visit details are :"
				+"\nVisit Id :" +oldMainVisit.patient_id
				+"\nDoctor :" +newDoctorName
				+"\nVisited Date :" + formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-")
				+"\nMain Visit will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"M");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = mainVisitId;
		}
	}

	return true;
}

/********  Follow Up to others *****************/
function validateFollowUpToFollowUpNoCons(opTypeObj, newDoctor, newDoctorName) {

	if (!isPreviousVisitDoctorExists(gPreviousDocVisits)) {
		setSelectedIndex(opTypeObj,"F");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}
	var oldMainVisit = getMainVisit(gPreviousDocVisits);

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"F");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else {
		var ok = confirm("Follow Up (With Consultation) will be converted to "+ getOpTypeText()+".Are you sure? "
						+"\nPlease cancel doctor consultation.");
		if (!ok) {
			setSelectedIndex(opTypeObj,"F");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gMainVisitId;
		}
	}

	return true;
}

function validateFollowUpToRevisit(opTypeObj, newDoctor, newDoctorName) {

	if (!isPreviousVisitDoctorExists(gPreviousDocVisits)) {
		setSelectedIndex(opTypeObj,"F");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}

	if (empty(newDoctor)) return true;

	var oldMainVisit = getMainVisit(gPreviousDocVisits);

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"F");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else if (!isVisitWithinValidity(gPreviousDocVisits, newDoctor)) {

		var ok = confirm("Follow Up (With Consultation) will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"F");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}

	}else {
		var ok = confirm("Warning: The Main visit has validity."
				+"\nFollow Up (With Consultation) will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"F");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}

	return true;
}


function validateFollowUpToMain(opTypeObj, newDoctor) {

	if (!isPreviousVisitDoctorExists(gPreviousDocVisits)) {
		setSelectedIndex(opTypeObj,"F");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}

	if (empty(newDoctor)) return true;

	if (isVisitWithinValidity(gPreviousDocVisits, newDoctor)) {

		var ok = confirm("Warning:The Main visit has validity."
				+"\nFollow Up (With Consultation) will be converted to "+ getOpTypeText()
				+"\nPlease change consultation in codification screen.Are you sure to proceed? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"F");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}else {
		var ok = confirm("Follow Up (With Consultation) will be converted to Main Visit.Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"F");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}
	return true;
}

/********  Follow Up No cons. to others *****************/
function validateFollowUpNoConsToFollowUp(opTypeObj, newDoctor, newDoctorName) {

	if (!isPreviousVisitDoctorExists(gPreviousDocVisits)) {
		setSelectedIndex(opTypeObj,"D");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}
	var oldMainVisit = getMainVisit(gPreviousDocVisits);

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"D");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else if (!isVisitWithinValidity(gPreviousDocVisits, newDoctor)) {
		var msg=getString("js.registration.editvisitdetails.thismainvisitvalidityhasbeenexpired");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.cannotchangeto");
		msg+=" ";
		msg+=getOpTypeText();
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.resettingvisittype.followupwithoutcons");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.themainvisitdetailsare");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.visitid");
		msg+=oldMainVisit.patient_id;
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.doctor");
		msg+=newDoctorName;
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.visiteddate");
		msg+=formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-");
		alert(msg);
		setSelectedIndex(opTypeObj,"D");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else {

		var ok = confirm("Follow Up (No Consultation) will be converted to "+ getOpTypeText()+".Are you sure? "
						+"\nPlease order doctor consultation.");
		if (!ok) {
			setSelectedIndex(opTypeObj,"D");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gMainVisitId;
		}
	}
	return true;
}

function validateFollowUpNoConsToRevisit(opTypeObj, newDoctor, newDoctorName) {

	if (!isPreviousVisitDoctorExists(gPreviousDocVisits)) {
		setSelectedIndex(opTypeObj,"D");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}

	if (empty(newDoctor)) return true;

	var oldMainVisit = getMainVisit(gPreviousDocVisits);
	var mainVisitId = oldMainVisit.main_visit_id;

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"D");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else if (!isVisitWithinValidity(gPreviousDocVisits, newDoctor)) {
		var ok = confirm("Follow Up (No Consultation) will be converted to "+ getOpTypeText()+".Are you sure? "
						+"\nPlease order doctor consultation.");
		if (!ok) {
			setSelectedIndex(opTypeObj,"D");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}else {
		var ok = confirm("Warning: The Main visit has validity."
				+"\nFollow Up (No Consultation) will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"D");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}
	return true;
}


function validateFollowUpNoConsToMain(opTypeObj, newDoctor) {

	if (!isPreviousVisitDoctorExists(gPreviousDocVisits)) {
		setSelectedIndex(opTypeObj,"D");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}

	if (empty(newDoctor)) return true;

	var oldMainVisit = getMainVisit(gPreviousDocVisits);

	if (!isVisitWithinValidity(gPreviousDocVisits, newDoctor)) {
		var ok = confirm("Follow Up (No Consultation) will be converted to "+ getOpTypeText()+".Are you sure? "
						+"\nPlease order doctor consultation.");
		if (!ok) {
			setSelectedIndex(opTypeObj,"D");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}else {
		var ok = confirm("Warning: The Main visit has validity."
				+"\nFollow Up (No Consultation) will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"D");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = gVisitId;
		}
	}
	return true;
}

/********  Revisit to others *****************/
function validateRevisitToMain(opTypeObj) {
	var ok = confirm("Revisit will be converted to Main Visit.Are you sure? ");
	if (!ok) {
		setSelectedIndex(opTypeObj,"R");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}else {
		document.mainform.main_visit_id.value = gVisitId;
	}
	return true;
}

function validateRevisitToFollowUp(opTypeObj, newDoctor, newDoctorName) {
	var oldMainVisit = getMainVisit(gPreviousDocVisits);
	var mainVisitId = oldMainVisit.main_visit_id;

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"R");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else if (!isVisitWithinValidity(gPreviousDocVisits, newDoctor)) {
		var msg=getString("js.registration.editvisitdetails.thismainvisitvalidityhasbeenexpired");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.cannotchangeto");
		msg+=" ";
		msg+=getOpTypeText();
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.resettingvisittype.backrevisit");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.themainvisitdetailsare");
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.visitid");
		msg+=oldMainVisit.patient_id;
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.doctor");
		msg+=newDoctorName;
		msg+="\n";
		msg+=getString("js.registration.editvisitdetails.visiteddate");
		msg+=formatDate(new Date(oldMainVisit.visited_date), "ddmmyyyy", "-");
		alert(msg);
		setSelectedIndex(opTypeObj,"R");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;
	}else {

		var ok = confirm("Warning: The Main visit has validity."
				+"\nRevisit will be converted to "+ getOpTypeText()
				+"\nPlease change the consultation in Codification screen."
				+"\nAre you sure to proceed? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"R");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = mainVisitId;
		}
	}

	return true;
}

function validateRevisitToFollowUpNoCons(opTypeObj, newDoctor, newDoctorName) {
	var oldMainVisit = getMainVisit(gPreviousDocVisits);
	var mainVisitId = oldMainVisit.main_visit_id;

	if (!validateDoctorConsultation(oldMainVisit, newDoctor)) {
		setSelectedIndex(opTypeObj,"R");
		document.mainform.main_visit_id.value = gMainVisitId;
		return false;

	}else {
		var ok = confirm("The Main visit has validity."
				+"\nRevisit will be converted to "+ getOpTypeText()+".Are you sure? ");
		if (!ok) {
			setSelectedIndex(opTypeObj,"R");
			document.mainform.main_visit_id.value = gMainVisitId;
			return false;
		}else {
			document.mainform.main_visit_id.value = mainVisitId;
		}
	}
	return true;
}
