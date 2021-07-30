var dosageAutoComplete = null;
var itemAutoComp = null;
var itemDocFavAutoComp = null;
var instructionAutoComplete = null;
var presform = document.prescribeForm;
function init() {
	complaintForm = document.prescribeForm;
	if (document.prescribeForm.one_time_prescriptions)
		initOneTimePrescriptions();
	if (displayActivityAndDueDate && document.prescribeForm.standing_prescriptions) {
		initStandingInstructions();
	}

	// which is a common dialog used for generic name display in one-time and standing prescriptions.
	editDialogGeneric();
	initInfoDialog();
	initHistoricalDialogs();

	// show the previous consultation summary only once. later user will select from the dropdown if wants to see it.
	// if the current visit is not Main Visit Or Outside patient and previous consultation id exists then
	// auto popupulate the previous visit details.
	if (consultation_status == 'A' && document.getElementById('op_type').value != 'M'
			&& document.getElementById('op_type').value != 'O'
			&& previousConsultationId > 0)
		previousVisitSummaryDialog.show(); // show previous visit summary dialog on page load.

	checkUsePerdiem();
	initPkgValueCapDialog();
	copyPasteImage('pastedPhoto', processPastedDataConsultation)
	loadReports();
}

//Global variable for pasted image.
var pastedImages = new Object();
var pastedImagesPrefix = 'pastedPhoto_';
var pastedImagesCount = 0;
function processPastedDataConsultation(elementId, source, blob, blobName, blobType) {
	var identifier = pastedImagesPrefix+pastedImagesCount
	pastedImagesCount +=1;
	var imgTag = document.createElement('img');

	imgTag.src = source;
	pastedImages[identifier] = {"imgTag" : imgTag};
	imgTag.onload  = function() {
	     var bb = createJpgImg(imgTag,identifier);
	      storeBlob(elementId, bb,identifier);
	}

}

function createJpgImg(imgTag){
	canvas = document.createElement("canvas")
	var width = imgTag.width;
	var height = imgTag.height

	canvas.width = width;
	canvas.height = height;
	canvas.getContext('2d').drawImage(imgTag,0,0,width,height,0,0,width,height);
	jpgSource = canvas.toDataURL('image/jpeg');
	return base64ToBlob(jpgSource);
}

function storeBlob(elementId, blob, identifier) {
	var xhr = new XMLHttpRequest();
	var formData = new FormData();
	formData.append("image", blob);
	xhr.onreadystatechange = function() {
		if (xhr.readyState == XMLHttpRequest.DONE) {
			if (!isNaN(xhr.responseText) && parseInt(xhr.responseText, 10)) {
				pastedImages[identifier]['id'] = xhr.responseText;
				pastedImages[identifier]["checked"] = false;
				displayNote(identifier, '', true);
				attachImages('now', identifier, true);
			} else {
				alert("Error while pasting the Image.");
			}
		}
	}
	xhr.open('POST', document.prescribeForm.getAttribute('action')+'?_method=pasteImage', true);
	xhr.send(formData);
}

function togglecheckBoxValue(identifier) {
	pastedImages[identifier]["checked"] = !pastedImages[identifier]["checked"];
}

function validatPBMFields(prefix, onSave) {
	if (document.getElementById('require_pbm_authorization').value != 'Y') return true;

	onSave = empty(onSave) ? false : onSave;
	var message = getString("js.outpatient.consultation.mgmt.planrequires.pbmauthorization");
		//message += " * Strength \n";
		//message += " * Strength Units \n";
		//message += " * Item Form \n";
		//message += " * Dosage \n";
		//message += " * Route \n";
		//message += " * Frequency \n";
		message += " "+getString("js.outpatient.consultation.mgmt.duration");
		message += " "+getString("js.outpatient.consultation.mgmt.durationunits");

	if (onSave) {
		var els = document.getElementsByName('issued');
		for (var i=0; i<els.length-1 ; i++) {
			var itemType = document.getElementsByName('itemType')[i].value;
			var non_hosp_medicine = document.getElementsByName('non_hosp_medicine')[i].value;
			if (!(itemType == 'Medicine')) continue; // skip if it is not a medicine.
			if (non_hosp_medicine == 'true') continue; // medicine but non hospital item. skip.
			if (els[i].value == 'Y') continue;

			//var item_strength = document.getElementsByName('item_strength')[i].value;
			//var item_strength_units = document.getElementsByName('item_strength_units')[i].value
			//var dosage = document.getElementsByName('strength')[i].value;
			//var routeId = document.getElementsByName('route_id')[i].value;
			//var frequency = document.getElementsByName('frequency')[i].value;
			var duration = document.getElementsByName('duration')[i].value;
			var duration_units = document.getElementsByName('duration_units')[i].value;
			//var item_form_id = document.getElementsByName('item_form_id')[i].value;

			/* if (empty(item_strength) || empty(item_strength_units) || empty(dosage) || empty(routeId) ||
				empty(frequency) || empty(duration) || empty(duration_units) || (empty(item_form_id) || item_form_id == 0)) {
				alert(message + "\n for item "+document.getElementsByName('item_name')[i].value);
				return false;
			} */

			if (empty(duration) || empty(duration_units)) {
				alert(message + getString("js.outpatient.consultation.mgmt.foritem")+" "+document.getElementsByName('item_name')[i].value);
				return false;
			}
		}
	} else {
		/* var item_strength = document.getElementById(prefix + '_item_strength').value;
		var item_strength_units = document.getElementById(prefix + '_item_strength_units').value
		var dosage = document.getElementById(prefix + '_strength').value;
		var routeId;
		if (prefix == 'd')
			routeId = document.getElementById(prefix + '_medicine_route').value;
		else
			routeId = document.getElementById(prefix + '_medicine_route').textContent;

		var item_form_id = document.getElementById(prefix + '_item_form_id').value;
		var frequency = document.getElementById(prefix + '_frequency').value;
		*/
		var duration = document.getElementById(prefix + '_duration').value;
		var duration_units;
		var du_els = document.getElementsByName(prefix + '_duration_units');
		for (var k=0; k<du_els.length; k++) {
			if (du_els[k].checked) {
				duration_units = du_els[k].value;
				break;
			}
		}
		/* if (empty(item_strength) || empty(item_strength_units) || empty(dosage) || empty(routeId) ||
			empty(frequency) || empty(duration) || empty(duration_units) || (empty(item_form_id) || item_form_id == 0)) {
			alert(message);
			return false;
		} */
		if (empty(duration) || empty(duration_units)) {
			alert(message);
			return false;
		}
	}
	return true;
}

function initInfoDialog() {
	infoDialog = new YAHOO.widget.Overlay("infoDialog",
				{ 	context: ['', 'tr', 'bl'],
					visible:false,
	            	modal: true,
					width:"300px" } );
	infoDialog.render(document.body);
	YAHOO.util.Event.addListener('cancelInfoImg', "click", infoDialog.hide, infoDialog, true);

}


function showInfoDialog(contextEl, text, pos) {
	var inputCorner = "tl", overlayCorner = "br";
	if (pos!=null) {
		if (pos=='left') {
			inputCorner = "tr";
			overlayCorner = "br";
		} else if (pos == 'topright') {
			inputCorner = "bl";
			overlayCorner = "tr";
		} else if (pos == 'topleft') {
			inputCorner = "br";
			overlayCorner = "tr"
		}
	}
	infoDialog.cfg.setProperty("context", [contextEl, overlayCorner, inputCorner], false);
	document.getElementById('infoDialogText').textContent = text;
	infoDialog.render(document.body);
	infoDialog.show();
}


function subscribeEscKeyEvent(dialog) {
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}

function consultationNotesEntered() {
	if (note_taker_prefs == 'Y') return true; // note taker is enabled no need to check for the consultation notes.

	var field_values = document.getElementsByName("field_value");
	for (var i=0; i<field_values.length; i++) {
		if (field_values[i].value != '') {
			return true;
		}
	}
	return false;
}

function vitalsEntered() {
	var vital_param_values = document.getElementsByName("vital_param_value");
	for (var i=0; i<vital_param_values.length; i++) {
		if (vital_param_values[i].value != '') return true;
	}
	return false;
}

function validateConsultDates() {
	var end_date = document.prescribeForm.consultation_end_date.value;
	var end_time = document.prescribeForm.consultation_end_time.value;
	var start_date = document.prescribeForm.consultation_start_date.value;
	var start_time = document.prescribeForm.consultation_start_time.value;
	var closeConsultation = document.prescribeForm.closeConsultation;

	if (!doValidateDateField(document.prescribeForm.consultation_start_date)) return false;
	if (!doValidateTimeField(document.prescribeForm.consultation_start_time)) return false;
	if (!doValidateDateField(document.prescribeForm.consultation_end_date)) return false;
	if (!doValidateTimeField(document.prescribeForm.consultation_end_time)) return false;

	if (!empty(start_date) && empty(start_time)) {
		showMessage("js.outpatient.consultation.mgmt.entertheconsultationstarttime");
		document.prescribeForm.consultation_start_time.focus();
		return false;
	} else if (empty(start_date) && !empty(start_time)) {
		showMessage("js.outpatient.consultation.mgmt.entertheconsultationstartdate");
		document.prescribeForm.consultation_start_date.focus();
		return false;
	}
	if (empty(end_date) && empty(end_time) && closeConsultation.checked){
		showMessage("js.outpatient.consultation.mgmt.entertheconsultationenddate");
		document.prescribeForm.consultation_end_date.focus();
		return false;
	}else if (!empty(end_date) && empty(end_time)) {
		showMessage("js.outpatient.consultation.mgmt.entertheconsultationendtime");
		document.prescribeForm.consultation_end_time.focus();
		return false;
	} else if (empty(end_date) && !empty(end_time)) {
		showMessage("js.outpatient.consultation.mgmt.entertheconsultationenddate");
		document.prescribeForm.consultation_end_date.focus();
		return false;
	}

	if (!empty(start_date) && !empty(start_time) &&
		getDateTime(start_date, start_time).getTime() <
			getDateTime(document.prescribeForm.admitted_date.value, document.prescribeForm.admitted_time.value).getTime()) {
		showMessage("js.outpatient.consultation.mgmt.startdateandtime.notbelessadm.or.regdateandtime");
		return false;
	}

	if (!empty(start_date) && !empty(start_time) && !empty(end_date) && !empty(end_time) &&
		getDateTime(end_date, end_time).getTime() < getDateTime(start_date, start_time).getTime()) {
		showMessage("js.outpatient.consultation.mgmt.enddateandtimelessthanstartdateandtime");
		return false;
	}
	return true;
}

function chkFormDetailsEdited(print, sendRequest, ceedcheck) {
	document.prescribeForm.isPrint.value = print;
	var closeConsultation = document.prescribeForm.closeConsultation;
	var visitType = document.prescribeForm.visitType.value;

	// Send Ceed Request after saving
	if (!empty(ceedcheck) && ceedcheck) {
		if(!validateDiagnosisDetails(true, diagnosisSectionMandatory, true)) {
			return false;
		}
		if (!prescExists()) {
			alert(getString("js.outpatient.consultation.mgmt.prescription.is.mandatory"));
			return false;
		}
		document.prescribeForm.ceedcheck.value = 'Y';
	}

	if (!validateConsultDates()) return false;
	if (!validateSysGenForms()) return false;
	if (!validateComplaint()) return false;
	if (!validateAllergies()) return false;

	if (mod_ceed_enabled && closeConsultation.checked && !ceedstatus) {
		var retstatus = confirm(getString("js.outpatient.consultation.mgmt.confirm.close.consultation.without.ceed"));
		if(retstatus == false) {
			return false;
		}
	}

	if (mod_eclaim_erx == 'Y') {
		if (!validateEprxFields(null, true)) return false;
	}else if (mod_eclaim_pbm == 'Y') {
		if (!validatPBMFields(null, true)) return false;
	}

	// validate mandatory fields in physician forms.
	if (!validateMandatoryFields()) return false;

	if (!validatePrescriptions()) return false;

	if (!validateUsePerdiem()) return false;

	// Send ERx Request after saving.
	if (!empty(sendRequest) && sendRequest)
		document.prescribeForm.sendErxRequest.value = 'Y';
	else
		document.prescribeForm.sendErxRequest.value = 'N';
		disableSaveButton(true);
	// do not put any code after this line.
	document.getElementById('callOnBeforeUnload').value = false;

	// setting the pasted image ids in hidden elements.
	for(var key in pastedImages) {
		if(!pastedImages[key]["checked"]){
			var element = document.createElement("input");
	        element.type = "hidden";
	        element.value = pastedImages[key]['id'];
	        element.name = 'pastedPhoto';
	        document.prescribeForm.append(element);
		}
	}

	document.prescribeForm.submit();

	if (!empty(ceedcheck) && ceedcheck) {
		eventTracking('Code Check Submission','Code Check Submission','Code Check Submission');
	}
	return true;
}

function prescExists() {
	var itemTypes = document.prescribeForm.itemType;
	var nonHospMedicine = document.prescribeForm.non_hosp_medicine;
	var deleted = document.prescribeForm.delItem;
	for (var i=0; i<itemTypes.length-1; i++) {
		if (deleted[i].value != 'true' && (itemTypes[i].value == 'Inv.' || itemTypes[i].value == 'Service'
			|| itemTypes[i].value == 'Operation'
			|| (itemTypes[i].value == 'Medicine' && nonHospMedicine[i].value == 'false'))) {
			return true;
		}
	}
	return false;
}

/*function sendERxRequest() {
	var consultationId = document.prescribeForm.consultation_id.value;
	var visitId = document.prescribeForm.patient_id.value;
	var url = cpath+'/';
	url += 'ERxPrescription/ERxRequest.do';
	url += '?_method=sendERxRequest';
	url += '&consultation_id='+consultationId;
	url += '&visit_id='+visitId;
	document.prescribeForm.action = url;
	document.prescribeForm.submit();
	return true;
}*/

function cancelERxRequest() {
	if (!validatePreviousERxRequest()) {
		return false;
	}
	var consultationId = document.prescribeForm.consultation_id.value;
	var visitId = document.prescribeForm.patient_id.value;
	var url = cpath+'/';
	url += 'ERxPrescription/ERxRequest.do';
	url += '?_method=cancelERxRequest';
	url += '&consultation_id='+consultationId;
	url += '&visit_id='+visitId;
	document.prescribeForm.action = url;
	document.prescribeForm.submit();
	return true;
}

function validatePreviousERxRequest() {
	if (!isRequestDataEditable()) {
		var ok = confirm(" Warning: Consultation ERx Request is Sent. \n " +
								"Cancelling ERx request will delete the Request. ERx request needs to be sent again. \n " +
				 				"Do you want to proceed ? ");
		if (!ok) {
			return false;
		}
	}
	return true;
}

function isRequestDataEditable() {
	var erxPrescIdObj = document.prescribeForm.erx_presc_id;
	var erxRefNoObj = document.prescribeForm.erx_reference_no;
	if (erxPrescIdObj != null && erxRefNoObj != null) {
		var erxPrescId = erxPrescIdObj.value;
		var erxRefNo = erxRefNoObj.value;
		if (!empty(erxPrescId) && !empty(erxRefNo)) {
			return false;
		}
	}
	return true;
}

function validateAddOrEditERxData() {
	if (!isRequestDataEditable()) {
		/*var ok = confirm(" Warning: Consultation ERx Request is Sent. \n " +
								"Add/Edit is not allowed. Cancel ERx request and send again. \n " +
				 				"Do you want to proceed anyway ? ");
		if (!ok) {
			return false;
		}*/

		alert(getString("js.outpatient.consultation.mgmt.consultationerxrequestissent") +
			getString("js.outpatient.consultation.mgmt.addoreditisnotallowedformedicines"));
		return false;
	}
	return true;
}

function reopen() {
	var time_limit_in_sec = consultation_reopen_time_limit * 60 * 60;
	if (!(roleId == 1 || roleId == 2 || reopen_consultation_after_time_limit == 'A')) {
		var hours = Math.floor(no_of_sec_till_date/(60 * 60));
		var remainingSecs =  no_of_sec_till_date%(60 * 60);
		var mins = 0;
		if (remainingSecs > 59) mins = Math.floor(remainingSecs/60);
		var fulltime = hours + " hrs ";
		fulltime += (mins == 0 ? "" : (mins + " mins"));
		if (no_of_sec_till_date > time_limit_in_sec) {
			alert(getString("js.outpatient.consultation.mgmt.theconsultationwasclosed")+fulltime+getString("js.outpatient.consultation.mgmt.whichexceedsreopentimelimitof")+consultation_reopen_time_limit+ " "+getString("js.outpatient.consultation.mgmt.hrs"));
			return false;
		}
	}
	document.prescribeForm._method.value = 'reopenConsultation';
	document.prescribeForm.submit();
	return true;
}

function openDoctorWeekView() {

	var followupdate = document.getElementById('followup_date');
	var doctorId = document.getElementById('consult_doctor_id').value;
	var href = cpath+'/pages/resourcescheduler/docweekview.do?method=getWeekView&category=DOC&includeResources='+doctorId;
	if (followupdate && followupdate.value != '') {
		href += '&date='+followupdate.value;
		href += '&choosenWeekDate='+followupdate.value;
	}
	window.open(href);

}

function triageSummaryPrint() {
	var consultationId = document.prescribeForm.consultation_id.value;
	var href = cpath+"/print/printTriage.json?consultationId="+consultationId;
	window.open(href);
}

function checkUsePerdiem() {
	var perdiemCheckObj = document.getElementById("perdiem_check");
	var usePerdiemObj = document.getElementById("use_perdiem");
	if (usePerdiemObj != null) {
		usePerdiemObj.value = (perdiemCheckObj.checked) ? "Y" : "N";

		if (!perdiemCheckObj.disabled) {
			if (perdiemCheckObj.checked) {
				document.getElementById("per_diem_code").disabled = false;
			}else {
				setSelectedIndex(document.getElementById("per_diem_code"), "");
				document.getElementById("per_diem_code").disabled = true;
			}
		}
	}

	var perdiemCodeObj = document.getElementById("per_diem_code");
	if (perdiemCodeObj != null) {
		var perdiemCode = perdiemCodeObj.value;
		var perdiemDet = findInList(perdiemCodesListJSON, "per_diem_code", perdiemCode);
		if (!empty(perdiemDet)) {
			document.getElementById("per_diem_code").title = perdiemDet.per_diem_description;
		}else {
			document.getElementById("per_diem_code").title = "";
		}
	}
}

function validateUsePerdiem() {
	var perdiemCheckObj = document.getElementById("perdiem_check");
	var perdiemCodeObj = document.getElementById("per_diem_code");
	if (perdiemCheckObj != null && perdiemCheckObj.checked) {
		if (perdiemCodeObj != null && perdiemCodeObj.value == "")  {
			showMessage("js.outpatient.consultation.mgmt.patientismarkedasperdiem");
			perdiemCodeObj.focus();
			return false;
		}
	}
	return true;
}

function selectListItem(jsonArray, autocomplete) {
	var textEl = autocomplete._elTextbox;
	var textBoxValue = textEl.value;
	var txtdefault = textEl.getAttribute('txtdefault') ? textEl.getAttribute('txtdefault') : '';

	if (txtdefault == textBoxValue) return false;
	if (empty(textBoxValue)) return false;

	var elListItem = autocomplete._elList.childNodes[0];
	sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key ||
               		autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
	for (var i=0; i<jsonArray.length; i++) {
    	var value = jsonArray[i][sMatchKey];
    	if (textBoxValue == value) {
    		oResult = jsonArray[i];
    		elListItem._sResultMatch = value;
		    elListItem._oResultData = oResult;
		    autocomplete._selectItem(elListItem);
		    return true;
    	}
    }
    return false;
}



function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.prescribeForm, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.prescribeForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.prescribeForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function validateEprxFields(prefix, onSave) {

	if (mod_eclaim_erx == 'Y'){
		onSave = empty(onSave) ? false : onSave;
		var message = getString("js.outpatient.consultation.mgmt.entervalues.sforeprescription");
			message += " "+getString("js.outpatient.consultation.mgmt.route");
			message += " "+getString("js.outpatient.consultation.mgmt.frequency");
			message += " "+getString("js.outpatient.consultation.mgmt.duration");
			message += " "+getString("js.outpatient.consultation.mgmt.durationunits");
			message += " "+getString("js.outpatient.consultation.mgmt.totalquantity");
			message += " "+getString("js.outpatient.consultation.mgmt.instructions");

		if (onSave) {
			var els = document.getElementsByName('issued');
			for (var i=0; i<els.length-1 ; i++) {
				var itemType = document.getElementsByName('itemType')[i].value;
				var non_hosp_medicine = document.getElementsByName('non_hosp_medicine')[i].value;
				if (!(itemType == 'Medicine')) continue; // skip if it is not a medicine.
				if (non_hosp_medicine == 'true') continue; // medicine but non hospital item. skip.
				if (els[i].value == 'Y') continue;

				var granular_unit = document.getElementsByName('granular_units')[i].value;
				var dosage = document.getElementsByName('strength')[i].value;
				var routeId = document.getElementsByName('route_id')[i].value;
				var frequency = document.getElementsByName('frequency')[i].value;
				var duration = document.getElementsByName('duration')[i].value;
				var duration_units = document.getElementsByName('duration_units')[i].value;
				var medicine_quantity = document.getElementsByName('medicine_quantity')[i].value;
				var remarks = document.getElementsByName('item_remarks')[i].value;

				var dosage_flag = false;
				if (granular_unit == 'Y' && empty(dosage)) {
					message += " "+getString("js.outpatient.consultation.mgmt.dosage");
					dosage_flag = true;
				}

				if (dosage_flag || empty(routeId) ||
					empty(frequency) || empty(duration) || empty(duration_units) || empty(medicine_quantity) || empty(remarks)) {
					alert(message +getString("js.outpatient.consultation.mgmt.foritem")+" "+document.getElementsByName('item_name')[i].value);
					return false;
				}
			}
		} else {

			var dosage = document.getElementById(prefix + '_strength').value;
			var granular_unit = document.getElementById(prefix + '_granular_units').value;
			var routeId;
			if (prefix == 'd')
				routeId = document.getElementById(prefix + '_medicine_route').value;
			else
				routeId = document.getElementById(prefix + '_medicine_route').textContent;

			var medicine_quantity = document.getElementById(prefix + '_qty').value;
			var frequency = document.getElementById(prefix + '_frequency').value;
			var duration = document.getElementById(prefix + '_duration').value;
			var duration_units;
			var du_els = document.getElementsByName(prefix + '_duration_units');
			var remarks = document.getElementById(prefix + '_remarks').value;

			for (var k=0; k<du_els.length; k++) {
				if (du_els[k].checked) {
					duration_units = du_els[k].value;
					break;
				}
			}

			var dosage_flag = false;
			if (granular_unit == 'Y' && empty(dosage)) {
				message += " "+getString("js.outpatient.consultation.mgmt.dosage");
				dosage_flag = true;
			}

			if (dosage_flag || empty(routeId) ||
				empty(frequency) || empty(duration) || empty(duration_units) || empty(medicine_quantity) || empty(remarks)) {
				alert(message);
				return false;
			}
		}
		return true;
	} else {

		return true;
	}
}

function checkForVitalMandatoryFields() {
	var vital_param_labels = document.getElementsByName("vital_param_label");
	var vital_param_values = document.getElementsByName("vital_param_value");
	var vital_mandatory_in_txs = document.getElementsByName("vital_mandatory_in_tx");
	var mandatoryFieldLabels = new Array();

	for (var i=0; i<vital_param_labels.length; i++) {
		if (vital_mandatory_in_txs[i].value == 'Y' && vital_param_values[i].value == '') {
			mandatoryFieldLabels.push(vital_param_labels[i].value);
		}
	}
	if (mandatoryFieldLabels.length > 0) {
		alert(getString('js.outpatient.consultation.mgmt.entervalues.vitalmandatoryfields')+'* '+mandatoryFieldLabels.join("\n* "));
		return false;
	}

	return true;
}

function disableSaveButton(disable){
	if (disable) {
		if (document.prescribeForm.Save != null) document.prescribeForm.Save.disabled = true;
		if (document.prescribeForm.SaveAndPrint != null) document.prescribeForm.SaveAndPrint.disabled = true;
		if (mod_ceed_enabled){
			if (document.prescribeForm.SaveAndCodeCheck != null) document.prescribeForm.SaveAndCodeCheck.disabled = true;
		}
	} else {
		if (document.prescribeForm.Save != null) document.mainform.Save.disabled = false;
		if (document.prescribeForm.SaveAndPrint != null) document.prescribeForm.SaveAndPrint.disabled = false;
		if (mod_ceed_enabled){
			if (document.prescribeForm.SaveAndCodeCheck != null) document.prescribeForm.SaveAndCodeCheck.disabled = false;
		}
	}
}

var finalizeAllInstaSections = function () {
	var finalizedElements = document.getElementsByClassName("finalize");
	var finlalizeAll = document.getElementById("finalizeAll");
	var finalizeAllChecked = finalizeAll.checked;
	for (var i = 0; i < finalizedElements.length; i++) {
		if (!finalizedElements[i].disabled) {
			finalizedElements[i].checked = finalizeAllChecked;
		}
	}
};

function consultationEndDateTime(){
	var end_date = document.prescribeForm.consultation_end_date.value;
	var end_time = document.prescribeForm.consultation_end_time.value;
	var db_end_date = document.prescribeForm.db_consultation_end_date.value;
	var db_end_time = document.prescribeForm.db_consultation_end_time.value;
	var closeConsultation = document.prescribeForm.closeConsultation;
	if(closeConsultation.checked){
		if (empty(end_date) && empty(end_time)){
			var curDate = new Date();
			document.prescribeForm.consultation_end_date.value = formatDate(curDate);
			document.prescribeForm.consultation_end_time.value = formatTime(curDate, false);
		}
	}else{
		document.prescribeForm.consultation_end_date.value = db_end_date;
		document.prescribeForm.consultation_end_time.value = db_end_time;
	}
}

