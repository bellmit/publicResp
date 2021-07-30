function captureSubmitEvent() {
	var form = document.DoctorsNotes;
	form.validateFormSubmit = form.submit;

	form.submit = function validatedSubmit() {
		if (!blockSubmit()) {
			var e = xGetElementById(document.DoctorsNotes);
			YAHOO.util.Event.stopEvent(e);
			return false;
		}
		form.validateFormSubmit();
		return true;
	};
}
function init() {
	document.getElementById('addNewNoteBtn').focus();
	captureSubmitEvent();
	initDoctorAutoComplete();
	initEditItemDialog();
	initDoctorEditAutoComplete();
	initLoginDialog();
}

function addNoteForm() {
	document.getElementById('addNoteDialog').style.display = 'block';
	document.getElementById('notes').focus();
	if (!empty(userRecord.doctor_id)) {
		var record = findInList(doctors, "doctor_id", userRecord.doctor_id);
		document.getElementById('doctor_id').value = userRecord.doctor_id;
		document.getElementById("doctor").value = record.doctor_name;
		docAutoComp._bItemSelected = true;
		docAutoComp._sInitInputValue = docAutoComp._elTextbox.value;
	}
	document.getElementById('billable_consultation').value = 'Y';
	document.getElementById('consultation_type_id').disabled = false;
	return true;
}

function emrPrintNotes() {
	var patientId = document.getElementById('patient_id').value;
	window.open(cpath+"/wardactivities/DoctorsNotes.do?_method=generateReport&patient_id="+patientId);
}

function validateBillable() {
	var billableconsultation = document.getElementById('chk_billable_consultation');
	if (billableconsultation.checked) {
		document.getElementById('billable_consultation').value = 'Y';
		document.getElementById('chk_billable_consultation').value = 'Y';
		document.getElementById('consultation_type_id').disabled = false;
	} else {
		document.getElementById('billable_consultation').value = 'N';
		document.getElementById('chk_billable_consultation').value = 'N';
		document.getElementById('consultation_type_id').disabled = true;
	}
}

function initDoctorAutoComplete() {
	var ds = new YAHOO.util.LocalDataSource({result : doctors}, {queryMatchContains: true});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				 ],
	};

	docAutoComp = new YAHOO.widget.AutoComplete("doctor", "doctorContainer", ds);
	docAutoComp.minQueryLength = 1;
	docAutoComp.animVert = false;
	docAutoComp.maxResultsDisplayed = 50;
	docAutoComp.resultTypeList = false;
	docAutoComp.forceSelection = true;
	docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	docAutoComp.formatResult = Insta.autoHighlight;
	docAutoComp.itemSelectEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('doctor_id').value = record.doctor_id;
	});
	docAutoComp.selectionEnforceEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('doctor_id').value = '';
	});

}

function initDoctorEditAutoComplete() {
	if (!document.getElementById('ed_doctor')) return;

	var ds = new YAHOO.util.LocalDataSource({result : doctors}, {queryMatchContains: true});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				 ],
	};

	editDocAutoComp = new YAHOO.widget.AutoComplete("ed_doctor", "ed_doctorContainer", ds);
	editDocAutoComp.minQueryLength = 1;
	editDocAutoComp.animVert = false;
	editDocAutoComp.maxResultsDisplayed = 50;
	editDocAutoComp.resultTypeList = false;
	editDocAutoComp.forceSelection = true;
	editDocAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	editDocAutoComp.formatResult = Insta.autoHighlight;
	editDocAutoComp.itemSelectEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('ed_doctor_id').value = record.doctor_id;
	});
	editDocAutoComp.selectionEnforceEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('ed_doctor_id').value = '';
	});

}

function onClickSave() {
	if (!validateSubmit())
		return false;
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.DoctorsNotes.submit();
	}
	document.getElementById('authUser').value = document.getElementById('loginUser').value;
	return true;
}

function submitHandler() {
	document.getElementById('authUser').value = document.getElementById('login_user').value;
	document.DoctorsNotes.submit();
	return false;
}

function validateSubmit() {
	var doctorName = document.getElementById('doctor').value;
	if (document.getElementById('notes').value.trim() == '') {
		alert('Please enter notes');
		document.getElementById('notes').focus();
		return false;
	}
	if(doctorName == '') {
		alert('Please select doctor name');
		document.getElementById('doctor').focus();
		return false;
	}
	if (document.getElementById('entered_date').value.trim() == '') {
		alert('Please select date');
		document.getElementById('entered_date').focus();
		return false;
	}
	if (!document.getElementById('entered_time').value.trim() == '') {
		if (!isTime(document.getElementById('entered_time').value.trim())) {
				alert('Please enter time in HH:mm format');
				document.getElementById('entered_time').focus();
				return false;
		}
	} else {
		alert('Please enter time');
		document.getElementById('entered_time').focus();
		return false;
	}

	var doctorId = document.getElementById('doctor_id').value;
	var patientId = document.getElementById('patient_id').value;
	var billableconsultation = document.getElementById('chk_billable_consultation');
	if (billableconsultation.checked) {
		if (document.getElementById('consultation_type_id').value == '') {
			alert('Please select Consultation type');
			document.getElementById('consultation_type_id').focus();
			return false;
		}
		if(!checkBillableConsultationForDay(patientId, doctorId, document.getElementById('entered_date').value))
			return false;
	}
	return true;
}

function checkBillableConsultationForDay(patientId, doctorId, date) {
	var url = "./DoctorsNotes.do?_method=getBillableConsultationCountForDay"
	url = url + "&patient_id=" + patientId;
	url = url + "&doctor_id=" + doctorId;
	url = url + "&creation_datetime=" + date;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			return handleAjaxResponse(reqObject.responseText);
		}
	}
	return true;
}

function handleAjaxResponse(responseText) {
	eval("var billableConsultationCount = " + responseText);

	if(billableConsultationCount != null) {
		if(ipPrefs.max_billable_cons_day <= parseInt(billableConsultationCount)) {
			alert('Maximum Billable Consultations per day : '+ ipPrefs.max_billable_cons_day);
			return false;
		}
	}
	return true;
}

function changeBackground() {
	var highlighted = document.getElementById('highlighted');
	if (highlighted.checked) {
		document.getElementById('highlighted').value = 'Y';
	} else {
		document.getElementById('highlighted').value = 'N';
	}
}

function closeNote() {
	clearFields();
	document.getElementById('addNoteDialog').style.display = 'none';
	return false;
}

function clearFields() {
	document.getElementById('notes').value = '';
	document.getElementById('doctor').value = '';
	document.getElementById('highlighted').checked = false;
	document.getElementById('chk_finalized').checked = false;
    document.getElementById('chk_billable_consultation').checked = false;
	document.getElementById('consultation_type_id').value = '';

}

function changeFinalized(){
	var finalized = document.getElementById('chk_finalized');
	if (finalized.checked) {
		document.getElementById('finalized').value = 'Y';
	} else {
		document.getElementById('finalized').value = 'N';
	}
}

function cancelDoctorNote(obj,index) {
	var oldDeleted = document.getElementById('h_delete_note'+index).value;
	if (!confirm('Doctor Note will be deleted')) return false;
	var newDeleted;
	if(oldDeleted == 'N') {
		document.getElementById('h_delete_note'+index).value = 'Y';
	}
	document.DoctorsNotes._method.value = 'delete';
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.DoctorsNotes.submit();
	}
	return true;
}

function showEditDocNoteDialog(obj, index) {
	var oldEdited = document.getElementById('h_edited'+index).value;
	if(oldEdited == 'false') {
		document.getElementById('h_edited'+index).value = 'true';
	}
	var index = index-1;
	var dialogDiv = document.getElementById("editItemDialog");
	dialogDiv.style.display = 'block';
	editItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editItemDialog.show();
	document.DoctorsNotes.editRowId.value = index;

	document.getElementById('ed_doctor').disabled = false;
	document.getElementById('ed_entered_date').disabled = false;
	document.getElementById('ed_entered_time').disabled = false;

	document.getElementById('ed_note_id').value = getIndexedValue('h_note_id', index);
	document.getElementById('ed_entered_date').value = getIndexedValue('h_creation_datetime', index).split(' ')[0];
	document.getElementById('ed_entered_time').value = getIndexedValue('h_creation_datetime', index).split(' ')[1];
	document.getElementById('edEnteredDate').value = getIndexedValue('h_creation_datetime', index).split(' ')[0];
	document.getElementById('edEnteredTime').value = getIndexedValue('h_creation_datetime', index).split(' ')[1];
 	document.getElementById('ed_doctor').value = getIndexedValue('h_doctor_name', index);
	document.getElementById('ed_doctor_id').value = getIndexedValue('h_doctor_id', index);
	document.getElementById('ed_notes').value = getIndexedValue('h_notes', index);
	document.getElementById('ed_entered_by_label').textContent = getIndexedValue('h_mod_user', index);
	document.getElementById('ed_mod_user').value = getIndexedValue('h_mod_user', index);
	document.getElementById('edit_highlighted').value = getIndexedValue('h_highlighted', index);
	document.getElementById('ed_highlighted').value = getIndexedValue('h_highlighted', index);
	document.getElementById('edit_finalized').value = getIndexedValue('h_finalized', index);
	document.getElementById('ed_finalized').value = getIndexedValue('h_finalized', index);
	document.getElementById('ed_chk_billable_consultation').value = getIndexedValue('h_billable_consultation', index);
	document.getElementById('ed_billable_consultation').value = getIndexedValue('h_billable_consultation', index);
	document.getElementById('ed_consultation_type_id').value = getIndexedValue('h_consultation_type_id', index);

	 if (editDocAutoComp._elTextbox.value != '') {
		editDocAutoComp._bItemSelected = true;
		editDocAutoComp._sInitInputValue = editDocAutoComp._elTextbox.value;
	}

	if(document.getElementById('edit_highlighted').value == 'Y') {
		document.getElementById('edit_highlighted').checked = true;
	} else {
		document.getElementById('edit_highlighted').checked = false;
	}
	if(document.getElementById('ed_finalized').value == 'Y') {
		document.getElementById('ed_finalized').checked = true;
	} else {
		document.getElementById('ed_finalized').checked = false;
	}
	if(document.getElementById('ed_chk_billable_consultation').value == 'Y') {
		document.getElementById('ed_chk_billable_consultation').checked = true;
		document.getElementById('ed_chk_billable_consultation').disabled = true;
		document.getElementById('ed_consultation_type_id').disabled= true;
		document.getElementById('consultationTypeId').value = document.getElementById('ed_consultation_type_id').value;
		document.getElementById('ed_doctor').disabled = true;
		document.getElementById('edEnteredDate').disabled = true;
		document.getElementById('edEnteredTime').disabled = true;
	} else {
		document.getElementById('ed_chk_billable_consultation').checked = false;
		document.getElementById('ed_chk_billable_consultation').disabled = false;
		document.getElementById('ed_consultation_type_id').disabled= true;
		document.getElementById('ed_doctor').disabled = false;
		document.getElementById('edEnteredDate').disabled = false;
		document.getElementById('edEnteredTime').disabled = false;
	}
	document.getElementById('ed_notes').focus();
	document.getElementById('authUser').value = document.getElementById('loginUser').value;
	return false;
}

var editItemDialog = null;
function initEditItemDialog() {
	var dialogDiv = document.getElementById("editItemDialog");
	if (!dialogDiv) return;

	editItemDialog = new YAHOO.widget.Dialog("editItemDialog",
			{	width:"800px",
				context : ["editItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditItemCancel,
	                                                scope:editItemDialog,
	                                                correctScope:true } );
	editItemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editItemDialog.cancelEvent.subscribe(handleEditItemCancel);
	YAHOO.util.Event.addListener('ed_Save', 'click', onClickEditSave, editItemDialog, true);
	YAHOO.util.Event.addListener('ed_Cancel', 'click', handleEditItemCancel, editItemDialog, true);
	editItemDialog.render();
}

function handleEditItemCancel() {
		this.hide();
}

function onClickEditSave(){
	if (!editTableRow())
		return false;
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.DoctorsNotes.submit();
	}
	return true;
}

function editTableRow() {
	var doctorName = document.getElementById('ed_doctor').value;
	if (document.getElementById('ed_notes').value.trim() == '') {
		alert('Please enter notes');
		document.getElementById('ed_notes').focus();
		return false;
	}
	if(doctorName == '') {
		alert('Please select doctor name');
		document.getElementById('ed_doctor').focus();
		return false;
	}
	if (document.getElementById('ed_entered_date').value.trim() == '') {
		alert('Please select date');
		document.getElementById('ed_entered_date').focus();
		return false;
	}
	if (!document.getElementById('ed_entered_time').value.trim() == '') {
		if (!isTime(document.getElementById('ed_entered_time').value.trim())) {
				alert('Please enter time in HH:mm format');
				document.getElementById('ed_entered_time').focus();
				return false;
		}
	} else {
		alert('Please enter time');
		document.getElementById('ed_entered_time').focus();
		return false;
	}
	var doctorId = document.getElementById('ed_doctor_id').value;
	var patientId = document.getElementById('patient_id').value;
	var billableconsultation = document.getElementById('ed_chk_billable_consultation');
	if (billableconsultation.checked) {
		if (document.getElementById('ed_consultation_type_id').value == ''){
			alert('Please select Consultation type');
			document.getElementById('ed_consultation_type_id').focus();
			return false;
		}
		if(billableconsultation.disabled == false && !checkBillableConsultationForDay(patientId, doctorId, document.getElementById('ed_entered_date').value))
			return false;
	}
	document.DoctorsNotes._method.value = 'update';
	return true;
}

function editDateTime() {
	document.getElementById('ed_entered_date').value = document.getElementById('edEnteredDate').value;
	document.getElementById('ed_entered_time').value = document.getElementById('edEnteredTime').value;
}

function ed_changeBackground() {
	var highlighted = document.getElementById('edit_highlighted');
	if (highlighted.checked) {
		document.getElementById('edit_highlighted').value = 'Y';
		document.getElementById('ed_highlighted').value = 'Y';
	} else {
		document.getElementById('edit_highlighted').value = 'N';
		document.getElementById('ed_highlighted').value = 'N';
	}
}

function ed_changeFinalized() {
	var finalized = document.getElementById('edit_finalized');
	if (finalized.checked) {
		document.getElementById('edit_finalized').value = 'Y';
		document.getElementById('ed_finalized').value = 'Y';
	} else {
		document.getElementById('edit_finalized').value = 'N';
		document.getElementById('ed_finalized').value = 'N';
	}
}
function ed_validateBillable() {
	var billableconsultation = document.getElementById('ed_chk_billable_consultation');
	if (billableconsultation.checked) {
		document.getElementById('ed_chk_billable_consultation').value = 'Y';
		document.getElementById('ed_billable_consultation').value = 'Y';
		document.getElementById('ed_consultation_type_id').disabled= false;
		document.getElementById('ed_billable_consultation').checked = true;
		//document.getElementById('ed_doctor').disabled = false;
	} else {
		document.getElementById('ed_chk_billable_consultation').value = 'N';
		document.getElementById('ed_billable_consultation').value = 'N';
		document.getElementById('ed_consultation_type_id').disabled= true;
		document.getElementById('ed_billable_consultation').checked = false;
		//document.getElementById('ed_doctor').disabled = true;
	}
}

function blockSubmit() {
	if (document.getElementById('patient_discharged').value == 'true') {
		alert("Patient is inactive or discharged, add/edit/delete of doctor note is not allowed.");
		return false;
	}
	return true;
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
	return getElementPaise(getIndexedFormElement(document.DoctorsNotes, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.DoctorsNotes, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.DoctorsNotes, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.DoctorsNotes, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

