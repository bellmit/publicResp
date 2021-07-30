function validateAdmissionRequest() {
	var reqDoctor = document.getElementById('requesting_doc');
	var admissionDate = document.getElementById('admission_date');
	var comments = document.getElementById('remarks');
	var centerId;

	if(gMax_centers_inc_default > 1) {
		centerId = document.getElementById('d_center_id')
		if(empty(centerId.value)) {
			showMessage("js.patient.addoreditadmissiondetails.please.select.a.center");
			centerId.focus();
			return false;
		}
	}

	if(empty(reqDoctor.value)) {
		showMessage('js.patient.addoreditadmissiondetails.please.select.requesting.doctor');
		reqDoctor.focus();
		return false;
	}

	if(empty(admissionDate.value)) {
		showMessage('js.patient.addoreditadmissiondetails.please.enter.admission.date');
		admissionDate.focus();
		return false;
	}

	if(!empty(comments) && !imposeMaxLength(comments,"comments"))
		return false;

	if (!validateDiagnosisDetails(false, true, false))
		return false;
	document.addnewadmissionrequest.submit();

	return true;
}

function imposeMaxLength(obj,text){
	var objDesc = obj.value;
	var newLines = objDesc.split("\n").length;
	var length = objDesc.length + newLines;
	var fixedLen = 2000;
	if (length > fixedLen) {
		alert(text+" "+getString('js.patient.addoreditadmissiondetails.can.not.be.more.than ') +fixedLen +" "+getString('js.patient.addoreditadmissiondetails.characters'));
		obj.focus();
		return false;
	}
	return true;
}

function loadDoctors(obj) {
	var defaultText = getString("js.common.commonselectbox.defaultText");
	var reqDoctorObj = document.getElementById('requesting_doc');
	var list = null;
	if(!empty(obj.value)) {
		list = filterListWithValues(doctors,"center_id",new Array(0,obj.value));
	}
	if(list != null) {
		loadSelectBox(reqDoctorObj, list, "doctor_name", "doctor_id", defaultText);
	}
	setSelectedIndex(reqDoctorObj,consDoctorId);
}
