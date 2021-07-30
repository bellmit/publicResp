function init() {
	initPhysicalDischargeDocAutoComp();
	initInitiateDoctorAutoComp();
	if (!isInitiateDischarge)
		disableInitiateDischarge();
	if (!isClinicalDischarge)
		disableClinicalDischarge();
}

function disableInitiateDischarge() {
	document.getElementById('initiateDischargeDiv').className = "disabler";
	disableFormFields(document.getElementById('initiateDischargeDiv'), true);

}

function disableClinicalDischarge() {
	document.getElementById('clinicalDischargeDiv').className = "disabler";
	disableFormFields(document.getElementById('clinicalDischargeDiv'), true);

}

function disableFormFields(parent, isDisabled) {
	var tagNames = ["INPUT", "SELECT", "TEXTAREA"];
	for (var i = 0; i < tagNames.length; i++) {
	    var elems = parent.getElementsByTagName(tagNames[i]);
	    for (var j = 0; j < elems.length; j++) {
	      elems[j].disabled = isDisabled;
	    }
	}
}

function initPhysicalDischargeDocAutoComp(){
	var doctorsJson = {result : doctors};
	var ds = new YAHOO.util.LocalDataSource(doctorsJson,{ queryMatchContains : true });
	ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = { resultsList : "result",
		  fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};
	if (document.getElementById('discharge_doc_ac')) {

		var autoComp = new YAHOO.widget.AutoComplete('discharge_doc_ac', 'disDocContainer', ds);

		autoComp.typeAhead = false;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = true;
		autoComp.forceSelection = true;
		autoComp.animVert = false;
		autoComp.useIFrame = true;
		autoComp.formatResult = Insta.autoHighlight;

		autoComp.itemSelectEvent.subscribe(function (sType, aArgs){
			var doctor = aArgs[2];
			document.dischargeform.discharge_doctor_id.value = doctor[1];
		});
		return autoComp;
	}
}

function initInitiateDoctorAutoComp(){
	var doctorsJson = {result : doctors};
	var ds = new YAHOO.util.LocalDataSource(doctorsJson,{ queryMatchContains : true });
	ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = { resultsList : "result",
		  fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};
	var autoComp1 = new YAHOO.widget.AutoComplete('initiate_discharge_doc_ac', 'initiateDisDocContainer', ds);

	autoComp1.typeAhead = false;
	autoComp1.useShadow = true;
	autoComp1.allowBrowserAutocomplete = false;
	autoComp1.minQueryLength = 0;
	autoComp1.maxResultsDisplayed = 20;
	autoComp1.autoHighlight = true;
	autoComp1.forceSelection = true;
	autoComp1.animVert = false;
	autoComp1.useIFrame = true;
	autoComp1.formatResult = Insta.autoHighlight;

	autoComp1.itemSelectEvent.subscribe(function (sType, aArgs){
		var doctor = aArgs[2];
		document.initiatedischargeform.initiate_discharge_doctor_id.value = doctor[1];
	});
	return autoComp1;
}


function validateDischaregDateTime() {

	if (document.dischargeform.discharge_date == null)
		return true;

	var disDate = document.dischargeform.discharge_date;
	var disTime = document.dischargeform.discharge_time;

	if (disDate.value == "" ) {
		alert("Select Discharge Date");
		disDate.focus();
		return false;
	}
	if (disTime.value == "" ) {
		alert("Enter Discharge Time");
		disTime.focus();
		return false;
	}
	if (!doValidateDateField(disDate)) return false;
	if (!validateTime(disTime)) return false;

	if(!validateDischargeDate())return false;

	return true;
}

function validateDischargeDate() {
	var regDateTime = getDateTime(regDate, regTime);
	var disDateTime = getDateTimeFromField(document.dischargeform.discharge_date,
			document.dischargeform.discharge_time);

	if (disDateTime < regDateTime) {
		alert("Discharge date/time cannot be earlier than Registration date/time (" + regDate + " " + regTime + ")");
		document.dischargeform.discharge_date.focus();
		return false;
	}
	return true;
}

function toggleDeathDateAndTime(){
	selEl = document.dischargeform.discharge_type;
	if(selEl.value != 3) {
		document.getElementById("death_date").value = "";
		document.getElementById("death_time").value = "";
	}
	return true;
}
function resetDeathReasonInfo(){
	selEl = document.dischargeform.discharge_type;
	if(selEl.value != 3) {
		document.getElementById("death_reason_id").value = "";
	}
	return true;
}
function onDischarge(){
	var disType = document.getElementById("discharge_type").value;
	if (disType == "") {
		alert("Discharge Type is mandatory for Physical Discharge.");
		return false;
	}

	var disDoctor = document.getElementById("discharge_doc_ac").value;
	if (disDoctor == "") {
		alert("Discharge Doctor is mandatory for Physical Discharge.");
		 document.getElementById("discharge_doc_ac").focus();
		return false;
	}

	if(disType == '4') {
		if (empty(document.getElementById("discharge_remarks").value.trim())) {
			alert("Discharge remarks is mandatory");
			document.getElementById("discharge_remarks").focus();
			return false;
		}
	}

	if(patientVisitStatus != 'I' && noOpenPatientIndents > 0){
		if(dischargeForPendingIndent == 'B'){
			alert(getString("js.common.message.block.discharge.for.pending.indent"));
			return false;
		}

		if(dischargeForPendingIndent == 'W'){
			if(!confirm(getString("js.common.message.warn.discharge.for.pending.indent"))){
				return false;
			}
		}
	}

	// reset the Death Reason Id based on drop-down selection.
	resetDeathReasonInfo();

	if( validateDischaregDateTime() && toggleDeathDateAndTime()){
		document.dischargeform.submit();
	}
}

function displayReferredTo(selEl){

	if (selEl.value == 5) {
		document.getElementById("refToHospDiv").style.display = 'table-row';
		document.getElementById("transferToHospDiv").style.display = 'table-row';
	} else {
		document.getElementById("refToHospDiv").style.display = 'none';
		document.getElementById("transferToHospDiv").style.display = 'none';

	}
	if(selEl.value == 3) {
	    document.getElementById("deathDateDiv").style.display = 'table-row';
	    document.getElementById("deathReasonDiv").style.display = 'table-row';
	    document.dischargeform.dead_on_arrival.disabled = false;
        document.dischargeform.cause_of_death_icdcode.disabled = false;
        document.dischargeform.stillborn.disabled = false;
	} else {
        document.getElementById("deathDateDiv").style.display = 'none';
        document.getElementById("deathReasonDiv").style.display = 'none';
        document.dischargeform.dead_on_arrival.disabled = true;
        document.dischargeform.cause_of_death_icdcode.disabled = true;
        document.dischargeform.stillborn.disabled = true;
	 }
}

//This method sends ajax request, when save button for initiate Discharge section is clicked.
function initiateDischarge() {
	if (!validateInitiateDischarge()) {
		return false;
	}
	// send GA event after validations
	eventTracking('Initiate Discharge', 'Initiate Discharge', 'Initiate Discharge');
    var initiateDischargeForm = document.getElementById("initiatedischargeform");
    var url = cpath + "/discharge/DischargePatient.do?_method=initiateDischarge";
    asyncPostForm(initiateDischargeForm, url, false,
    		initiateDischargeSuccess, initiateDischargeFailure);
    return true;
}


function initiateDischargeSuccess(response){
	document.getElementById("initiatedischargebutton").value = "Edit";
    document.getElementById("initiatedischargebutton").onclick = function(){ editInitiateDischarge(); };
    document.getElementById("initiate_check").disabled = true;
    document.getElementById("initiate_expected_discharge_date").disabled = true;
    document.getElementById("initiate_expected_discharge_time").disabled = true;
    document.getElementById("initiate_discharge_doc_ac").disabled = true;
    document.getElementById("initiate_discharge_remarks").disabled = true;
    var responseJson = eval('(' + response.responseText + ')');

    var initiateDischargeClass = responseJson.initiate_discharge_status == true ? "checkmark":"uncheckmark";
    var initiateDischargeLabel = responseJson.initiate_discharge_status == true ? "label":"unlabel";
    var initiateDischargeDate = responseJson.initiate_discharging_date;
    var initiateDischargeTime = responseJson.initiate_discharging_time;
    var initiateDischargeUser = responseJson.initiate_entered_by;
    document.getElementById("initiateDischargeCheck").className = initiateDischargeClass;
    document.getElementById("initiateDischargeLabel").className = initiateDischargeLabel;
    if(responseJson.initiate_discharge_status == true) {
    	document.getElementById("initiateflag").value = "true";
        document.getElementById("initiatediscargedatetime").innerHTML = initiateDischargeDate + " " + initiateDischargeTime;
		document.getElementById("initiatedischargeusername").innerHTML = initiateDischargeUser;
		document.getElementById("clinicaldischarge").disabled = false;
		document.getElementById("clinicaldischargecomments").disabled = false;
		document.getElementById("clinicaldischargebutton").disabled = false;
    }
    else {
    	document.getElementById("initiateflag").value = "false";
        document.getElementById("initiatediscargedatetime").innerHTML = "";
		document.getElementById("initiatedischargeusername").innerHTML = "";
		document.getElementById("clinicaldischarge").disabled = true;
		document.getElementById("clinicaldischargecomments").disabled = true;
		document.getElementById("clinicaldischargebutton").disabled = true;
    }

}

function editInitiateDischarge(){
	document.getElementById("initiatedischargebutton").value = "Save";
    document.getElementById("initiatedischargebutton").onclick = function(){ initiateDischarge(); };
    document.getElementById("initiate_check").disabled = false;
    document.getElementById("initiate_check").onclick = function(){ checkFunction(); };
    var chkbox = document.getElementById("initiate_check");
    if (document.getElementById("isDoctorFlag").value == 'true'){
    	document.getElementById("initiate_discharge_doc_ac").disabled = true;
    	document.getElementById("initiate_discharge_doc_ac").value = document.getElementById("isDoctorName").value;
    	document.getElementById("initiate_discharge_doctor_id").value = document.getElementById("isDoctorId").value;
    }
    if (chkbox.checked){
    	if (document.getElementById("isDoctorFlag").value == 'false'){
    		document.getElementById("initiate_discharge_doc_ac").disabled = false;
    	}
    	document.getElementById("initiate_expected_discharge_date").disabled = false;
    	document.getElementById("initiate_expected_discharge_time").disabled = false;
    	document.getElementById("initiate_discharge_remarks").disabled = false;
    }
}

function initiateDischargeFailure(e){
    alert('Error: Failed to Save');
}

function validateInitiateDischarge() {
	var initiateflag = document.initiatedischargeform.initiateflag;
	var chkBox = document.getElementById("initiate_check");
	if(initiateflag.value == "true" && !chkBox.checked){
		var clinicalDischargeDetails = null;
		var url = cpath + "/discharge/DischargePatient.do?_method=getClinicalDischargeDetails&patientId=" + patientId;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		var status = false;
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					var clinicalDischargeDetails = eval('(' + ajaxobj.responseText + ')');
					if((clinicalDischargeDetails == null) || (clinicalDischargeDetails != null && clinicalDischargeDetails.clinical_discharge_flag == false)) {
						status = true;
					}
				}
			}
		}
		if (status == false) {
			alert("Clinical Discharge should be reset before Initiate Discharge.");
			return false;
		}
	}
	else {
		var disDate = document.initiatedischargeform.initiate_expected_discharge_date;
		var disTime = document.initiatedischargeform.initiate_expected_discharge_time;
		var disDoc = document.initiatedischargeform.initiate_discharge_doc_ac;
		if (!chkBox.checked) {
			alert("Please Select the Initiate Discharge");
			return false;
		}
		if (disDoc.value == "") {
			alert("Discharging Doctor is Required");
			disDoc.focus();
			return false;
		}
		if (disDate.value == "" ) {
			alert("Select Expected Discharge Date");
			disDate.focus();
			return false;
		}
		if (disTime.value == "" ) {
			alert("Enter Expected Discharge Time");
			disTime.focus();
			return false;
		}
		if (!doValidateDateField(disDate)) return false;
		if (!validateTime(disTime)) return false;
		if(!validateExpectedDischargeDate())return false;
	}
	return true;
}

function validateExpectedDischargeDate() {
	var regDateTime = getDateTime(regDate, regTime);
	var disDateTime = getDateTimeFromField(document.initiatedischargeform.initiate_expected_discharge_date,
			document.initiatedischargeform.initiate_expected_discharge_time);

	if (disDateTime < regDateTime) {
		alert("Expected Discharge date/time cannot be earlier than Registration date/time (" + regDate + " " + regTime + ")");
		document.dischargeform.discharge_date.focus();
		return false;
	}
	return true;
}

function checkFunction(){
	var chkBox = document.getElementById("initiate_check");
	if (chkBox.checked)
    {
		document.getElementById("initiate_expected_discharge_date").disabled = false;
	    document.getElementById("initiate_expected_discharge_time").disabled = false;
	    if(document.getElementById("isDoctorFlag").value == 'false'){
	    	document.getElementById("initiate_discharge_doc_ac").disabled = false;
	    }
	    document.getElementById("initiate_discharge_remarks").disabled = false;
    }
	else{
		document.getElementById("initiate_expected_discharge_date").value = "";
	    document.getElementById("initiate_expected_discharge_time").value = "";
	    if (document.getElementById("isDoctorFlag").value == 'false'){
	    	document.getElementById("initiate_discharge_doc_ac").value = "";
	    }
	    document.getElementById("initiate_discharge_remarks").value = "";
		document.getElementById("initiate_expected_discharge_date").disabled = true;
	    document.getElementById("initiate_expected_discharge_time").disabled = true;
	    document.getElementById("initiate_discharge_doc_ac").disabled = true;
	    document.getElementById("initiate_discharge_remarks").disabled = true;
	}
}

//This method validates Initiate Discharge section
function validateClinicalDischarge() {
	var checked = document.getElementById("clinicaldischarge").checked;
	if (checked) {
		var pendingActivities = noPendingOrdersOperations + noPendingWardActivites + noOpenPatientIndents;
		if (pendingActivities > 0) {
			var confirmation = confirm("There are Pending Activites. Do you want to proceed with Clinical Discharge?");
			if (confirmation == false) {
				return false;
			}
		}
		var initiateDischargeDetails = null;
		var url = cpath + "/discharge/DischargePatient.do?_method=getInitiateDischargeDetails&mrno="+mrno+"&patientId="+patientId;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		var status = false;
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					console.log(ajaxobj.responseText);
					var initiateDischargeDetails = eval('(' + ajaxobj.responseText + ')');
					if(initiateDischargeDetails != null && initiateDischargeDetails.initiate_discharge_status == true) {
						status = true;
					}
				}
			}
		}
		if (status == false) {
			alert("Patient should be marked as initiate Discharge before Clinical Discharge.");
		}
		return status;
	} else {
		var clinicalflag = document.clinicaldischargeform.clinicalflag;
		if (clinicalflag.value == "false"){
			alert("Please Select Clinical Discharge");
			return false;
		}
	}
	return true;
}

// This method sends ajax request, when save button for Clinical Discharge section is clicked.
function clinicalDischarge() {
	if (!validateClinicalDischarge()) {
		return false;
	}
	// send GA event after validations
	eventTracking('Clinical Discharge', 'Clinical Discharge', 'Clinical Discharge');
    var clinicalDischargeForm = document.getElementById("clinicaldischargeform");
    var url = cpath + "/discharge/DischargePatient.do?_method=clinicalDischarge";
    asyncPostForm(clinicalDischargeForm, url, false,
    		clinicalDischargeSuccess, clinicalDischargeFailure);
    return true;
}

function clinicalDischargeSuccess(response){
	document.getElementById("clinicaldischargebutton").value = "Edit";
	document.getElementById("clinicaldischargebutton").onclick = function(){ editClinicalDischarge(); };
	document.getElementById("clinicaldischarge").disabled = true;
	document.getElementById("clinicaldischargecomments").disabled = true;
	var responseJson = eval('(' + response.responseText + ')');
	var clinicalDischargeClass = responseJson.clinical_discharge_flag == true ? "checkmark":"uncheckmark";
	var clinicalDischargeLabel = responseJson.clinical_discharge_flag == true ? "label":"unlabel";
	var clinicalDischargeDate = responseJson.clinical_discharging_date;
	var clinicalDischargeTime = responseJson.clinical_discharging_time;
	var clinicalDischargeUser = responseJson.clinical_entered_by;
	document.getElementById("clinicaldischargecheckmark").className = clinicalDischargeClass;
	document.getElementById("clinicaldischargelabel").className = clinicalDischargeLabel;
	if (responseJson.clinical_discharge_flag == true) {
		document.clinicaldischargeform.clinicalflag.value = true;
		document.getElementById("clinicaldiscargedatetime").innerHTML = clinicalDischargeDate + " " + clinicalDischargeTime;
		document.getElementById("clinicaldischargeusername").innerHTML = clinicalDischargeUser;
	}
	else {
		document.clinicaldischargeform.clinicalflag.value = false;
		document.getElementById("clinicaldiscargedatetime").innerHTML = "";
		document.getElementById("clinicaldischargeusername").innerHTML = "";
	}
}

function clinicalDischargeOnClick() {
	var checked = document.getElementById("clinicaldischarge").checked;
	if (checked) {
		document.getElementById("clinicaldischargecomments").disabled = false;
	}
	else {
		document.getElementById("clinicaldischargecomments").value = "";
		document.getElementById("clinicaldischargecomments").disabled = true;
	}
}

function editClinicalDischarge(){
	document.getElementById("clinicaldischargebutton").value = "Save";
	document.getElementById("clinicaldischarge").disabled = false;
	document.getElementById("clinicaldischargebutton").onclick = function(){ clinicalDischarge(); };
	document.getElementById("clinicaldischarge").onclick = function() { clinicalDischargeOnClick(); };
	var checked = document.getElementById("clinicaldischarge").checked;
	if (checked) {
		document.getElementById("clinicaldischargecomments").disabled = false;
	}
	else {
		document.getElementById("clinicaldischargecomments").value = "";
		document.getElementById("clinicaldischargecomments").disabled = true;
	}
}

function clinicalDischargeFailure(e){
	alert('Error: Failed to Save');
}
