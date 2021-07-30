var addDiagnosisDialog = null;
var d_diagnosisAutoComp = null;
var ed_diagnosisAutoComp = null;
var d_diagnosisDocAutoComp = null;
var ed_diagnosisDocAutoComp = null;
var healthAuthority = '';
var isYearOfOnSetMandatory='';
function initDiagnosisDetails() {
	initAddDiagnosisDetailsDialog();
	initEditDiagnosisDialog();
	d_diagnosisAutoComp = diagnosisCodeAutoComplete('d_diagnosis_code', 'd_diagnosis_code_container', true);
	d_diagnosisDocAutoComp = initDiagDoctorAC('d_diagnosis_doctor', 'd_diagnosis_doctor_container', false, '');
}

function initDiagDoctorAC(inputElId, containerId, edit, doctorId) {
	var doctorList = [];
	for (var i=0; i<diagnosis_doctors_json.length; i++) {
		if (diagnosis_doctors_json[i].status == 'A' || doctorId == diagnosis_doctors_json[i].doctor_id) {
			doctorList.push(diagnosis_doctors_json[i]);
		}
	}
	if (edit && !empty(ed_diagnosisDocAutoComp)) {
		ed_diagnosisDocAutoComp.destroy();
		ed_diagnosisDocAutoComp = null;
	} else if (!empty(d_diagnosisDocAutoComp)) {
		d_diagnosisDocAutoComp.destroy();
		d_diagnosisDocAutoComp = null;
	}

	var ds = new YAHOO.util.LocalDataSource({result: doctorList});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [	{key : "doctor_name"},
					{key : "doctor_id"}
			 	]
	};

	var autoComp = new YAHOO.widget.AutoComplete(inputElId, containerId, ds);
	autoComp.minQueryLength = 1;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 100;
	autoComp.resultTypeList = false; // making the result available as object literal.
	autoComp.forceSelection = true;
	autoComp.autoSnapContainer = false;
	autoComp.queryMatchContains = true;
	autoComp.formatResult = Insta.autoHighlight;
	if (document.getElementById(inputElId).value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = document.getElementById(inputElId).value;
	}

	autoComp.itemSelectEvent.subscribe(setDiagnosisDoctor);
	autoComp.selectionEnforceEvent.subscribe(clearDiagnosisDoctor);

	return autoComp;
}

function setDiagnosisDoctor(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_diagnosis_doctor_id").value = sArgs[2].doctor_id;
}

function clearDiagnosisDoctor(oSelf, sClearedValue) {
	var elId = sClearedValue[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_diagnosis_doctor_id").value = '';
}

function initAddDiagnosisDetailsDialog() {
	var dialogDiv = document.getElementById("addDiagnosisDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addDiagnosisDialog = new YAHOO.widget.Dialog("addDiagnosisDialog",
			{	width:"600px",
				context : ["addDiagnosisDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_diagnosis_add_btn', 'click', addDiagnosisDetailsToGrid, addDiagnosisDialog, true);
	YAHOO.util.Event.addListener('d_diagnosis_cancel_btn', 'click', cancelDiagnosisAddDialog, addDiagnosisDialog, true);

	var enterKeyListener = new YAHOO.util.KeyListener("addDiagnosisDialog", { keys:13 },
				{ fn:onEnterKeyDiagnosisDialog, scope:addDiagnosisDialog, correctScope:true } );
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelDiagnosisAddDialog,
	                                                scope:addDiagnosisDialog,
	                                                correctScope:true } );
	addDiagnosisDialog.cfg.setProperty("keylisteners", [escKeyListener, enterKeyListener]);
	addDiagnosisDialog.render();
}

function onEnterKeyDiagnosisDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new autocomplete.)
	document.getElementById("d_diagnosis_code").blur();
	addDiagnosisDetailsToGrid();
}

function showAddDiagnosisDialog(obj) {

	if ((screenid == 'op_prescribe') && !validateAddOrEditERxData()) {
		return false;
	}

	clearDiagnosisDetailsFields();
	addDiagnosisDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);

	if (screenid == 'out_pat_reg' || (screenid == 'preauth_presc' && op_type == 'O') ) {
		document.getElementById('d_diagnosis_doctor').disabled = true;

	} if (screenid == 'op_prescribe') {
		var consultingDoctorId = document.getElementById('diag_consulting_doctor_id').value;
		consultingDoctorId = empty(consultingDoctorId) ? diag_user_doctor_id : consultingDoctorId;
		var record = findInList(diagnosis_doctors_json, "doctor_id", consultingDoctorId);

		var consultingDoctorName = empty(consultingDoctorId) ? '' : record.doctor_name;
		document.getElementById('d_diagnosis_doctor').disabled = true;
		document.getElementById('d_diagnosis_doctor').value = consultingDoctorName;
		document.getElementById('d_diagnosis_doctor_id').value = consultingDoctorId;
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_diagnosis_doctor', 'd_diagnosis_doctor_container', false, consultingDoctorId);
		if (document.getElementById('d_diagnosis_doctor').value != '') {
			d_diagnosisDocAutoComp._bItemSelected = true;
			d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_diagnosis_doctor').value;
		}

	} else if (screenid == 'reg_admission_request') {
		var requestingDoctorId = document.getElementById('requesting_doc').value;
		requestingDoctorId = empty(requestingDoctorId) ? diag_user_doctor_id : requestingDoctorId;
		var record = findInList(diagnosis_doctors_json, "doctor_id", requestingDoctorId);
		var requestingDoctorName = empty(requestingDoctorId) ? '' : record.doctor_name;
		document.getElementById('d_diagnosis_doctor').value = requestingDoctorName;
		document.getElementById('d_diagnosis_doctor_id').value = requestingDoctorId;
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_diagnosis_doctor', 'd_diagnosis_doctor_container', false, requestingDoctorId);
		if (document.getElementById('d_diagnosis_doctor').value != '') {
			d_diagnosisDocAutoComp._bItemSelected = true;
			d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_diagnosis_doctor').value;
		}
	}else if (!empty(diag_user_doctor_id)) {
		// populate the user doctor in ip visit summary/outside patient registration screens.
		var record = findInList(diagnosis_doctors_json, "doctor_id", diag_user_doctor_id);
		var userDoctorName = record.doctor_name
		document.getElementById('d_diagnosis_doctor').value = userDoctorName;
		document.getElementById('d_diagnosis_doctor_id').value = diag_user_doctor_id;
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_diagnosis_doctor', 'd_diagnosis_doctor_container', false, diag_user_doctor_id);
		if (document.getElementById('d_diagnosis_doctor').value != '') {
			d_diagnosisDocAutoComp._bItemSelected = true;
			d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_diagnosis_doctor').value;
		}
	}
	if (screenid == 'visit_summary' || screenid == 'ot_record') {
		document.getElementById('d_diag_usr_label_td').style.display = 'table-cell';
		document.getElementById('d_diag_usr_value_td').style.display = 'table-cell';
	} else {
		document.getElementById('d_diag_usr_label_td').style.display = 'none';
		document.getElementById('d_diag_usr_value_td').style.display = 'none';
	}
	var diag_deleted_els = document.getElementsByName('diagnosis_deleted');
	var displayPrimary = true;
	for (var i=0; i<diag_deleted_els.length-1; i++) {
		var deleted = diag_deleted_els[i].value;
		if (deleted == 'false') {
			displayPrimary = false;
			break;
		}
	}
	document.getElementById('d_diagnosis_type').value = displayPrimary ? 'P' : 'S';
	addDiagnosisDialog.show();
	document.getElementById('d_diagnosis_code').focus();
}

function cancelDiagnosisAddDialog() {
	this.cancel();
}

function clearDiagnosisDetailsFields() {
	document.getElementById('d_diagnosis_type').value = 'S';
	document.getElementById('d_diagnosis_description_label').textContent = '';
	document.getElementById('d_diagnosis_code').value = '';
	if(screenid == 'op_prescribe' || ((screenid == 'visit_summary' || screenid == 'ot_record') && !empty(diag_user_doctor_id))){
	document.getElementById('d_add_favourite').disabled = false ;
	document.getElementById('d_add_favourite').checked = false ;
	}
	if (screenid != 'op_prescribe' && screenid != 'reg_admission_request') {
		document.getElementById('d_diagnosis_doctor').value = '';
		document.getElementById('d_diagnosis_doctor_id').value = '';
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_diagnosis_doctor', 'd_diagnosis_doctor_container', false, diag_user_doctor_id);
		if (!empty(diag_user_doctor_id)) {
			var record = findInList(diagnosis_doctors_json, "doctor_id", diag_user_doctor_id);
			var userDoctorName = record.doctor_name
			document.getElementById('d_diagnosis_doctor').value = userDoctorName;
			document.getElementById('d_diagnosis_doctor_id').value = diag_user_doctor_id;
			if (document.getElementById('d_diagnosis_doctor').value != '') {
				d_diagnosisDocAutoComp._bItemSelected = true;
				d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_diagnosis_doctor').value;
			}
		}
	}

	document.getElementById('d_diagnosis_date').value = formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_diagnosis_time').value = formatTime(new Date(), false);
	if (mod_mrd_icd == 'Y') {
		document.getElementById('d_diagnosis_code').disabled = false;
		document.getElementById('d_diagnosis_description_label').style.display = 'block';
		document.getElementById('d_diagnosis_description').style.display = 'none';
	} else {
		document.getElementById('d_diagnosis_code').disabled = true;
		document.getElementById('d_diagnosis_description_label').style.display = 'none';
		document.getElementById('d_diagnosis_description').style.display = 'block';
	}
	document.getElementById('d_diagnosis_description_hidden').value = '';
	document.getElementById('d_diagnosis_status_name').value = '';
	document.getElementById('d_diagnosis_remarks').value = '';
	document.getElementById('d_diagnosis_year_of_onset').value = '';
}

var diagnosisColIndex = 0;
var diagnosisDetailsAdded = 0;
var DIAG_DATETIME = diagnosisColIndex++, DIAG_DOCTOR = diagnosisColIndex++, DIAG_TYPE = diagnosisColIndex++,
	DIAG_CODE_TYPE=diagnosisColIndex++, DIAG_CODE = diagnosisColIndex++, DIAG_DESC = diagnosisColIndex++,
	DIAG_STATUS = diagnosisColIndex++, DIAG_REMARKS = diagnosisColIndex++, DAIG_TRASH_COL = diagnosisColIndex++,
	DIAG_EDIT_COL = diagnosisColIndex++;
function addDiagnosisDetailsToGrid() {

	if (screenid == 'op_prescribe' && !validateAddOrEditERxData()) {
		return false;
	}
	var diag_type = document.getElementById('d_diagnosis_type').value;
	var diagnosis_doctor = document.getElementById('d_diagnosis_doctor').value;
	var diagYearOfOnset = document.getElementById('d_diagnosis_year_of_onset');
	// in outside patient registration screen doctor is not a mandatory field.
	if ((screenid != 'out_pat_reg' && (screenid == 'preauth_presc' && op_type != 'O')) && diagnosis_doctor == '') {
		showMessage('js.patient.diagnosis.enter.doctor');
		return false;
	}
	if (diag_type == '') {
		showMessage("js.patient.diagnosis.enter.diagtype");
		document.getElementById('d_diagnosis_type').focus();
		return false;
	}
	if(healthAuthority == 'HAAD' && isYearOfOnSetMandatory == 'true'){
		if(diag_type === 'P' && empty(diagYearOfOnset.value)){
			showMessage("js.patient.diagnosis.year.of.onset.mandatory");
			document.getElementById('d_diagnosis_year_of_onset').focus();
			return false;
		}
	}
	if(!empty(diagYearOfOnset.value)){
		if (diagYearOfOnset.value.length != 4 || !numberCheck(diagYearOfOnset)){
			showMessage('js.patient.diagnosis.year.of.onset.error');
			document.getElementById('d_diagnosis_year_of_onset').focus();
			return false;
		}
	}
	var diagnosis_description = '';
	if (mod_mrd_icd == 'Y') {
		if (document.getElementById('d_diagnosis_code').value == '') {
			showMessage("js.patient.diagnosis.enter.diagcode");
			document.getElementById('d_diagnosis_code').focus();
			return false;
		}
		var diag_codes = document.getElementsByName('diagnosis_code');
		for(var i = 0; i<diag_codes.length-1; i++) {
		   if(diag_codes[i].value == document.getElementById('d_diagnosis_code').value){
		       showMessage('js.patient.diagnosis.duplicate.code');
		       document.getElementById('d_diagnosis_code').focus();
		       return false ;
		    }
		}
		diagnosis_description = document.getElementById('d_diagnosis_description_hidden').value;
	} else {
		diagnosis_description = document.getElementById('d_diagnosis_description').value;
		if (diagnosis_description == '') {
			showMessage("js.patient.diagnosis.enter.diagdescription");
			document.getElementById('d_diagnosis_description').focus();
			return false;
		}
	}
	if (!validateDiagnosisDateTime('d_diagnosis_date', 'd_diagnosis_time', 'Date', 'Time', null, false))
		return false;
	var id = getNumCharges('diagnosisDetailsTable');
   	var table = document.getElementById("diagnosisDetailsTable");
	var templateRow = table.rows[getTemplateRow('diagnosisDetailsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

	var diagnosis_code = document.getElementById('d_diagnosis_code').value;
	var diagnosis_status_id = document.getElementById('d_diagnosis_status_name').value;
	var diagnosis_status_name = document.getElementById('d_diagnosis_status_name').options[document.getElementById('d_diagnosis_status_name').selectedIndex].text;
	diagnosis_status_name = diagnosis_status_id == '' ? '' : diagnosis_status_name;
	var diagnosis_remarks = document.getElementById('d_diagnosis_remarks').value;
	var diagnosis_doctor_id = document.getElementById('d_diagnosis_doctor_id').value;
	var diagnosis_datetime = document.getElementById('d_diagnosis_date').value + " " + document.getElementById('d_diagnosis_time').value;
	if(screenid == 'op_prescribe' || ((screenid == 'visit_summary' || screenid == 'ot_record') && !empty(diag_user_doctor_id))){
	   var add_fav_checkbox = document.getElementById('d_add_favourite').value;
	   if(document.getElementById('d_add_favourite').checked){
	        add_fav_checkbox = 'Y';
	   }
	  else {
	        add_fav_checkbox = 'N';
	   }
	   setDiagnosisHiddenValue(id, "diagnosis_favourite", add_fav_checkbox);
	}
	setNodeText(row.cells[DIAG_DATETIME], diagnosis_datetime);
	setNodeText(row.cells[DIAG_DOCTOR], diagnosis_doctor, 20);
	setNodeText(row.cells[DIAG_TYPE], diag_type == 'P' ? 'Principal' : (diag_type == 'V' ? 'Reason For Visit' : 'Secondary'));
	setNodeText(row.cells[DIAG_CODE_TYPE], defaultDiagnosisCodeType, 10);
   	setNodeText(row.cells[DIAG_CODE], diagnosis_code, 20);
   	setNodeText(row.cells[DIAG_DESC], diagnosis_description, 30);
   	setNodeText(row.cells[DIAG_STATUS], diagnosis_status_name, 20);
   	setNodeText(row.cells[DIAG_REMARKS], diagnosis_remarks, 30);

	setDiagnosisHiddenValue(id, "diagnosis_id", '_');
   	setDiagnosisHiddenValue(id, "diagnosis_type", diag_type);
	setDiagnosisHiddenValue(id, "diagnosis_code", diagnosis_code);
	setDiagnosisHiddenValue(id, "diagnosis_code_type", defaultDiagnosisCodeType);
	setDiagnosisHiddenValue(id, "diagnosis_description", diagnosis_description);
	setDiagnosisHiddenValue(id, "diagnosis_year_of_onset", diagYearOfOnset.value);
	setDiagnosisHiddenValue(id, "diagnosis_status_id", diagnosis_status_id);
	setDiagnosisHiddenValue(id, "diagnosis_status_name", diagnosis_status_name);
	setDiagnosisHiddenValue(id, "diagnosis_remarks", diagnosis_remarks);
	setDiagnosisHiddenValue(id, "diagnosis_datetime", diagnosis_datetime);
	setDiagnosisHiddenValue(id, "diagnosis_doctor_id", diagnosis_doctor_id);
	setDiagnosisHiddenValue(id, "diagnosis_doctor_name", diagnosis_doctor);
	setDiagnosisHiddenValue(id, "is_year_of_onset_mandatory", isYearOfOnSetMandatory);
	setDiagnosisHiddenValue(id, "health_authority", healthAuthority);
	if (screenid == 'visit_summary' || screenid == 'ot_record')
		setDiagnosisHiddenValue(id, "diagnosis_entered_by", document.getElementById('d_diagnosis_entered_by').value);

	clearDiagnosisDetailsFields();
	diagnosisDetailsAdded++;
	addDiagnosisDialog.align("tr", "tl");
	setDiagnosisRowStyle(id);
	document.getElementById('d_diagnosis_code').focus();
	return id;
}

function setDiagnosisRowStyle(id) {
	var row = getChargeRow(id, 'diagnosisDetailsTable');
	var diagnosisId = getDiagnosisIndexedValue("diagnosis_id", id);
	var flagImgs = row.cells[DIAG_DATETIME].getElementsByTagName("img");
	var trashImgs = row.cells[DAIG_TRASH_COL].getElementsByTagName("img");

	var added = (diagnosisId.substring(0,1) == "_");
	var cancelled = getDiagnosisIndexedValue("diagnosis_deleted", id) == 'true';
	var edited = getDiagnosisIndexedValue("diagnosis_edited", id) == 'true';
	/*
	 * Pre-saved state is shown using background colours. The pre-saved states can be:
	 *  - Normal: no background
	 *  - Added: Greenish background
	 *  - Modified: Yellowish background
	 *    (includes cancelled, which is a change in the status attribute)
	 *
	 * Attributes are shown using flags. The only attribute indicated is the cancelled
	 * attribute, using a red flag.
	 *
	 * Possible actions using the trash icon are:
	 *  - Cancel/Delete an item: Normal trash icon.
	 *    (newly added items are deleted, saved items are cancelled)
	 *  - Un-cancel an item: Trash icon with a cross
	 *  - The item cannot be cancelled: Grey trash icon.
	 */

	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelDiagnosis(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var oldDeleted =  getDiagnosisIndexedValue("diagnosis_deleted", id);

	var isNew = getDiagnosisIndexedValue("diagnosis_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		diagnosisDetailsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setDiagnosisIndexedValue("diagnosis_deleted", id, newDeleted);
		setDiagnosisIndexedValue("diagnosis_edited", id, "true");
		setDiagnosisRowStyle(id);
	}
	return false;
}

var editDiagnosisDialog = null;
function initEditDiagnosisDialog() {
	var dialogDiv = document.getElementById("editDiagnosisDialog");
	if (!dialogDiv) return ;

	dialogDiv.style.display = 'block';
	editDiagnosisDialog = new YAHOO.widget.Dialog("editDiagnosisDialog",{
			width:"600px",
			text: "Edit Diagnosis",
			context :["editDiagnosisDialog", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditDiagnosisCancel,
	                                                scope:editDiagnosisDialog,
	                                                correctScope:true } );
	editDiagnosisDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editDiagnosisDialog.cancelEvent.subscribe(handleEditDiagnosisCancel);
	YAHOO.util.Event.addListener('ed_diagnosis_ok_btn', 'click', editDiagnosisTableRow, editDiagnosisDialog, true);
	YAHOO.util.Event.addListener('ed_diagnosis_cancel_btn', 'click', handleEditDiagnosisCancel, editDiagnosisDialog, true);
	YAHOO.util.Event.addListener('ed_diagnosis_previous_btn', 'click', openPreviousDiagnosis, editDiagnosisDialog, true);
	YAHOO.util.Event.addListener('edit_diagnosis_next_btn', 'click', openNextDiagnosis, editDiagnosisDialog, true);
	editDiagnosisDialog.render();
}

function showEditDignosisDialog(obj) {

	if (screenid == 'op_prescribe' && !validateAddOrEditERxData()) {
		return false;
	}
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editDiagnosisDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editDiagnosisDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('diagnosisEditRowId').value = id;

	var diag_type = getDiagnosisIndexedValue("diagnosis_type", id);
	var yearOfOnset = getDiagnosisIndexedValue("diagnosis_year_of_onset", id);
	var diagnosis_doctor_id = getDiagnosisIndexedValue('diagnosis_doctor_id', id);
	isYearOfOnSetMandatory = getDiagnosisIndexedValue("is_year_of_onset_mandatory",id);
	healthAuthority = getDiagnosisIndexedValue("health_authority",id);
	document.getElementById('ed_diagnosis_doctor').value = getDiagnosisIndexedValue('diagnosis_doctor_name', id);
	document.getElementById('ed_diagnosis_doctor_id').value = diagnosis_doctor_id;
	ed_diagnosisDocAutoComp = initDiagDoctorAC('ed_diagnosis_doctor', 'ed_diagnosis_doctor_container', true, diagnosis_doctor_id);
	if (document.getElementById('ed_diagnosis_doctor').value != '') {
		ed_diagnosisDocAutoComp._bItemSelected = true;
		ed_diagnosisDocAutoComp._sInitInputValue = document.getElementById('ed_diagnosis_doctor').value;
	}
	if (screenid == 'op_prescribe' || screenid == 'out_pat_reg' || (screenid == 'preauth_presc' && op_type == 'O') ) {
		document.getElementById('ed_diagnosis_doctor').disabled = true;
	}
	document.getElementById('ed_diagnosis_type').value = diag_type;
	document.getElementById('ed_diagnosis_code').value = getDiagnosisIndexedValue('diagnosis_code', id);
	document.getElementById('ed_diagnosis_remarks').value = getDiagnosisIndexedValue('diagnosis_remarks', id);
	var diagnosis_description = getDiagnosisIndexedValue('diagnosis_description', id);
	document.getElementById('ed_diagnosis_description_hidden').value = diagnosis_description;
	document.getElementById('ed_diagnosis_year_of_onset').value = yearOfOnset;
	document.getElementById('ed_diagnosis_code_type').value = getDiagnosisIndexedValue('diagnosis_code_type', id);
	if (mod_mrd_icd == 'Y') {
		document.getElementById('ed_diagnosis_description_label').style.display = 'block';
		document.getElementById('ed_diagnosis_description').style.display = 'none';
		document.getElementById('ed_diagnosis_description_label').textContent = diagnosis_description;
		if (document.getElementById('ed_diagnosis_code').value != '') {
			if (ed_diagnosisAutoComp != null) {
				ed_diagnosisAutoComp.destroy();
				ed_diagnosisAutoComp = null;
			}
			ed_diagnosisAutoComp = diagnosisCodeAutoComplete('ed_diagnosis_code', 'ed_diagnosis_code_container', false,
					getDiagnosisIndexedValue('diagnosis_code_type', id));
			ed_diagnosisAutoComp._bItemSelected = true;
			ed_diagnosisAutoComp._sInitInputValue = document.getElementById('ed_diagnosis_code').value;
		}
	} else {
		document.getElementById('ed_diagnosis_description_label').style.display = 'none';
		document.getElementById('ed_diagnosis_description').style.display = 'block';
		document.getElementById('ed_diagnosis_description').value = diagnosis_description;
	}
	var names = [];
	var diagnosis_status_id = getDiagnosisIndexedValue('diagnosis_status_id', id);
	for (var i=0; i<diagnosis_status_names_json.length; i++) {
		if (diagnosis_status_names_json[i].status == 'A' || diagnosis_status_names_json[i].diagnosis_status_id == diagnosis_status_id) {
			names.push(diagnosis_status_names_json[i]);
		}
	}
	var diagnosis_datetime = getDiagnosisIndexedValue('diagnosis_datetime', id);
	document.getElementById('ed_diagnosis_date').value = empty(diagnosis_datetime) ? '' : diagnosis_datetime.split(' ')[0];
	document.getElementById('ed_diagnosis_time').value = empty(diagnosis_datetime) ? '' : diagnosis_datetime.split(' ')[1];
	if (screenid == 'visit_summary' || screenid == 'ot_record') {
		document.getElementById('ed_entered_by_label').textContent = getDiagnosisIndexedValue('diagnosis_entered_by', id);
		document.getElementById('ed_diag_usr_label_td').style.display = 'table-cell';
		document.getElementById('ed_diag_usr_value_td').style.display = 'table-cell';
	} else {
		document.getElementById('ed_diag_usr_label_td').style.display = 'none';
		document.getElementById('ed_diag_usr_value_td').style.display = 'none';
	}

	loadSelectBox(document.getElementById('ed_diagnosis_status_name'), names, 'diagnosis_status_name', 'diagnosis_status_id', '-- Select --');
	document.getElementById('ed_diagnosis_status_name').value = diagnosis_status_id;
	document.getElementById('ed_diagnosis_code').focus();
	return false;
}

function editDiagnosisTableRow() {

	if (screenid == 'op_prescribe' && !validateAddOrEditERxData()) {
		return false;
	}

	var id = document.getElementById('diagnosisEditRowId').value;
	var row = getChargeRow(id, 'diagnosisDetailsTable');

	var diag_type = document.getElementById('ed_diagnosis_type').value;
	var diagnosis_doctor = document.getElementById('ed_diagnosis_doctor').value;
	var diagYearOfOnset = document.getElementById('ed_diagnosis_year_of_onset');
	if ((screenid != 'out_pat_reg' && (screenid == 'preauth_presc' && op_type != 'O')) && diagnosis_doctor == '') {
		showMessage('js.patient.diagnosis.enter.doctor');
		return false;
	}
	if (diag_type == '') {
		showMessage("js.patient.diagnosis.enter.diagtype");
		document.getElementById('ed_diagnosis_type').focus();
		return false;
	}
	var diagnosis_description = '';
	if (mod_mrd_icd == 'Y') {
		if (document.getElementById('ed_diagnosis_code').value == '') {
			showMessage("js.patient.diagnosis.enter.diagcode");
			document.getElementById('ed_diagnosis_code').focus();
			return false;
		}
		var diag_codes = document.getElementsByName('diagnosis_code');
		for(var i = 0; i<diag_codes.length-1; i++) {
		     if(diag_codes[id].value != document.getElementById('ed_diagnosis_code').value){
		         if(diag_codes[i].value == document.getElementById('ed_diagnosis_code').value){
		            showMessage('js.patient.diagnosis.duplicate.code');
		            document.getElementById('ed_diagnosis_code').focus();
		       return false ;
		       }
		    }
		}
		 diagnosis_description = document.getElementById('ed_diagnosis_description_hidden').value;
	} else {
		diagnosis_description = document.getElementById('ed_diagnosis_description').value;
		if (diagnosis_description == '') {
			showMessage("js.patient.diagnosis.enter.diagdescription");
			document.getElementById('ed_diagnosis_description').focus();
			return false;
		}
	}

	if(healthAuthority == 'HAAD' && isYearOfOnSetMandatory == 'true'){
		if(diag_type === 'P' && empty(diagYearOfOnset.value)){
			showMessage("js.patient.diagnosis.year.of.onset.mandatory");
			document.getElementById('ed_diagnosis_year_of_onset').focus();
			return false;
		}
	}
	if(!empty(diagYearOfOnset.value)){
		if (diagYearOfOnset.value.length != 4 || !numberCheck(diagYearOfOnset)){
			showMessage('js.patient.diagnosis.year.of.onset.error');
			document.getElementById('ed_diagnosis_year_of_onset').focus();
			return false;
		}
	}
	if (!validateDiagnosisDateTime('ed_diagnosis_date', 'ed_diagnosis_time', 'Date', 'Time', null, false))
		return false;
	var diagnosis_code = document.getElementById('ed_diagnosis_code').value;
	var diagnosis_type = document.getElementById('ed_diagnosis_type').value;
	// check if edited diagcode is same as original and mark diagcode property as edited
	setDiagnosisHiddenValue(id, "diagcodeortypeedited", "false");
	var diagnosis_code_from_db = getDiagnosisIndexedValue("diagnosis_code_from_db", id);
	var diagnosis_type_from_db = getDiagnosisIndexedValue("diagnosis_type_from_db", id);
	if(diagnosis_code_from_db != diagnosis_code || diagnosis_type_from_db != diagnosis_type) {
		setDiagnosisHiddenValue(id, "diagcodeortypeedited", "true");
	}
	var diagnosis_status_id = document.getElementById('ed_diagnosis_status_name').value;
	var diagnosis_status_name = document.getElementById('ed_diagnosis_status_name').options[document.getElementById('ed_diagnosis_status_name').selectedIndex].text;
	diagnosis_status_name = diagnosis_status_id == '' ? '' : diagnosis_status_name;
	var diagnosis_remarks = document.getElementById('ed_diagnosis_remarks').value;
	var diagnosis_datetime = document.getElementById('ed_diagnosis_date').value + ' ' + document.getElementById('ed_diagnosis_time').value;
	var diagnosis_doctor_id = document.getElementById('ed_diagnosis_doctor_id').value;

	setNodeText(row.cells[DIAG_DATETIME], diagnosis_datetime);
	setNodeText(row.cells[DIAG_DOCTOR], diagnosis_doctor, 20);
	setNodeText(row.cells[DIAG_TYPE], diag_type == 'P' ? 'Principal' : (diag_type == 'V' ? 'Reason For Visit' : 'Secondary'));
   	setNodeText(row.cells[DIAG_CODE], diagnosis_code, 20);
   	setNodeText(row.cells[DIAG_DESC], diagnosis_description, 30);
   	setNodeText(row.cells[DIAG_STATUS], diagnosis_status_name, 20);
   	setNodeText(row.cells[DIAG_REMARKS], diagnosis_remarks, 30);

	setDiagnosisHiddenValue(id, "diagnosis_code", diagnosis_code);
	setDiagnosisHiddenValue(id, "diagnosis_type", diag_type);
	setDiagnosisHiddenValue(id, "diagnosis_description", diagnosis_description);
	setDiagnosisHiddenValue(id, "diagnosis_year_of_onset", diagYearOfOnset.value);
	setDiagnosisHiddenValue(id, "diagnosis_status_id", diagnosis_status_id);
	setDiagnosisHiddenValue(id, "diagnosis_status_name", diagnosis_status_name);
	setDiagnosisHiddenValue(id, "diagnosis_remarks", diagnosis_remarks);
	setDiagnosisHiddenValue(id, "diagnosis_datetime", diagnosis_datetime);
	setDiagnosisHiddenValue(id, "diagnosis_doctor_id", diagnosis_doctor_id);
	setDiagnosisHiddenValue(id, "diagnosis_doctor_name", diagnosis_doctor);
	setDiagnosisHiddenValue(id, "is_year_of_onset_mandatory", isYearOfOnSetMandatory);
	setDiagnosisHiddenValue(id, "health_authority", healthAuthority);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setDiagnosisIndexedValue("diagnosis_edited", id, 'true');
	setDiagnosisRowStyle(id);

	editDiagnosisDialog.hide();
	return true;
}

function handleEditDiagnosisCancel() {
	var id = document.getElementById('diagnosisEditRowId').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'diagnosisDetailsTable');
	YAHOO.util.Dom.removeClass(row, 'editing');
	this.hide();
}

var diagnosisFieldEdited = false;
function setDiagnosisEdited() {
	diagnosisFieldEdited = true;
}

function openPreviousDiagnosis() {
	var id = document.getElementById('diagnosisEditRowId').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'diagnosisDetailsTable');

	if (diagnosisFieldEdited) {
		if (!editDiagnosisTableRow()) return false;
		diagnosisFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditDignosisDialog(document.getElementsByName('diagnosisEditAnchor')[parseInt(id)-1]);
	}
}

function openNextDiagnosis() {
	var id = document.getElementById('diagnosisEditRowId').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'diagnosisDetailsTable');

	if (diagnosisFieldEdited) {
		if (!editDiagnosisTableRow()) return false;
		diagnosisFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('diagnosisDetailsTable').rows.length-2) {
		showEditDignosisDialog(document.getElementsByName('diagnosisEditAnchor')[parseInt(id)+1]);
	}
}

function reInitializeCodeAc(addDialog) {
	if (addDialog) {
		if (d_diagnosisAutoComp != null) {
			d_diagnosisAutoComp.destroy();
			d_diagnosisAutoComp = null;
		}
		document.getElementById('d_diagnosis_code').value = '';
		d_diagnosisAutoComp = diagnosisCodeAutoComplete('d_diagnosis_code', 'd_diagnosis_code_container', true);
		document.getElementById('d_diagnosis_code').focus();
	} else {
		if (ed_diagnosisAutoComp != null) {
			ed_diagnosisAutoComp.destroy();
			ed_diagnosisAutoComp = null;
		}
		document.getElementById('ed_diagnosis_code').value = '';
		ed_diagnosisAutoComp = diagnosisCodeAutoComplete('ed_diagnosis_code', 'ed_diagnosis_code_container', false,
				document.getElementById('ed_diagnosis_code_type').value);
		document.getElementById('ed_diagnosis_code').focus();
	}
}

function diagnosisCodeAutoComplete(inputElId, containerId, addDialog, diagCodeType) {

	var ds = new YAHOO.util.XHRDataSource(cpath + "/pages/registration/regUtils.do");
	var favourites = document.getElementById((addDialog ? 'd_' : 'ed_') + 'show_favourites').checked;
	var doctorId = document.getElementById((addDialog ? 'd_' : 'ed_') + 'diagnosis_doctor_id').value;

	var codeType = addDialog ? defaultDiagnosisCodeType : diagCodeType;
	if (screenid == 'visit_summary' || screenid == 'ot_record')
		doctorId = diag_user_doctor_id; // get the favourites only for the logged in doctor.
	if ((screenid == 'op_prescribe' || screenid == 'visit_summary' || screenid == 'ot_record') && favourites && !empty(doctorId)) {
		ds.scriptQueryAppend = "_method=getDiagCodeFavourites&defaultDiagnosisCodeType=" + encodeURIComponent(codeType) + "&doctorId="+doctorId;
	} else {
		ds.scriptQueryAppend = "_method=getDiagnosisCodes&defaultDiagnosisCodeType=" + encodeURIComponent(codeType);
	}

	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [	{key : "code"},
					{key : "icd"},
					{key : "code_desc"},
					{key : "health_authority"},
					{key : "is_year_of_onset_mandatory"}
			 	]
	};

	var autoComp = new YAHOO.widget.AutoComplete(inputElId, containerId, ds);
	autoComp.minQueryLength = 1;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 100;
	autoComp.resultTypeList = false; // making the result available as object literal.
	autoComp.forceSelection = true;
	autoComp.autoSnapContainer = false;
	var reArray = [];
	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var escapedComp = Insta.escape(sQuery);
		reArray[0] = new RegExp( '^' + escapedComp, 'i');
		reArray[1] = new RegExp( "\\s" + escapedComp, 'i');

		var title = oResultData.code + ' / ' + oResultData.code_desc;
    	var det = highlight(oResultData.code + ' / ' + oResultData.code_desc, reArray);

    	var span = document.createElement('span');
    	span.setAttribute("title", title);
    	span.innerHTML = det;
    	var div = document.createElement('div');
    	div.appendChild(span);
    	return div.innerHTML;
    };
    autoComp.setHeader(' Code / Description ');
	if (document.getElementById(inputElId).value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = document.getElementById(inputElId).value;
	}

	autoComp.itemSelectEvent.subscribe(setDiagnosisDescription);
	autoComp.selectionEnforceEvent.subscribe(clearDiagnosisDesc);
	return autoComp;
}

function clearDiagnosisDesc(oSelf, sClearedValue) {
	var elId = sClearedValue[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	var description = document.getElementById(prefix+"_diagnosis_description_label");
	document.getElementById(prefix+"_diagnosis_description_hidden").value = '';
	setNodeText(description, '');
}

function setDiagnosisDescription(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	var description = document.getElementById(prefix+"_diagnosis_description_label");
	document.getElementById(prefix+"_diagnosis_description_hidden").value = sArgs[2].code_desc;
	isYearOfOnSetMandatory = sArgs[2].is_year_of_onset_mandatory ? 'true' : "";
	healthAuthority = sArgs[2].health_authority == null ||
	empty(sArgs[2].health_authority) ? '' : sArgs[2].health_authority;
	setNodeText(description, sArgs[2].code_desc);
	if(screenid == 'op_prescribe' || ((screenid == 'visit_summary' || screenid == 'ot_record') && !empty(diag_user_doctor_id))){
	  var codesArray = JSON.parse(codeslist);                  //disable add favourite checkbox.
	  for (var i = 0; i < codesArray.length; i++) {
	    if(codesArray[i] == sArgs[2].code ){
	         document.getElementById(prefix+"_add_favourite").disabled = true;
	     }
	  }
	}
}


function validateDiagnosisDetails(ceedcheck, mandatory, checkCodeType) {
	if (empty(checkCodeType)) checkCodeType = false;
	var els = document.getElementsByName('diagnosis_code');
	var diag_type_els = document.getElementsByName('diagnosis_type');
	var diag_delete = document.getElementsByName('diagnosis_deleted');
	var diagYearOfOnSet = document.getElementsByName('diagnosis_year_of_onset');
	var diagIsYearOfOnSet = document.getElementsByName('is_year_of_onset_mandatory');
	var diagHealthAutority = document.getElementsByName('health_authority');
	var yearOfOnSet ='';
	var primaryDiagnosisCount = 0;
	var diagnosisCount = 0;
	for (var i=0; i<els.length; i++) {
		var code = els[i].value;
		var diag_type = diag_type_els[i].value;
		if (diag_type == 'P' && diag_delete[i].value == 'false') {
			primaryDiagnosisCount++;
			isYearOfOnSetMandatory = diagIsYearOfOnSet[i].value;
			healthAuthority = diagHealthAutority[i].value;
			yearOfOnSet = diagYearOfOnSet[i].value;

		}
		if (!empty(diag_type) && diag_delete[i].value == 'false') {
			diagnosisCount++;
		}
	}

	// validate if the user entered something eventhough it is not mandatory or if the section is mandatory.
	if (diagnosisCount > 0 || mandatory) {
		if (primaryDiagnosisCount == 0) {
			if(ceedcheck) {
				showMessage("js.outpatient.consultation.mgmt.principaldiagnosis.is.mandatory");
			}
			else {
				showMessage("js.patient.diagnosis.enter.principaldiagnosis");
			}
			return false;
		}
		if (primaryDiagnosisCount > 1) {
			showMessage("js.patient.diagnosis.one_principaldiagnosis_allowed");
			return false;
		}
		if (checkCodeType && !isCodeTypeSameForAll()) {
			if (!confirm(getString("js.outpatient.consultation.mgmt.multiple.codetypes.selected.which.may.not.be.allowed")))
				return false;
		}
		if(primaryDiagnosisCount == 1 && healthAuthority == 'HAAD'
			&& isYearOfOnSetMandatory == 'true' && empty(yearOfOnSet)){
			showMessage("js.patient.diagnosis.year.of.onset.mandatory");
			return false;
		}
	}

	return true;
}

YAHOO.util.Event.onContentReady('content', initDiagnosisResultsDialog);
var pResultsDialog = null;
function initDiagnosisResultsDialog() {
	var dialogDiv = document.getElementById("previousDiagnosisDetailsDiv");
	if ( dialogDiv )
		dialogDiv.style.display = 'block';
	pResultsDialog = new YAHOO.widget.Dialog("previousDiagnosisDetailsDiv",
			{	width:"900px",
				height: '500px',
				context : ["previousDiagnosisDetailsDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('click_Ok', 'click', addToTableFromPrevDiagnosis, pResultsDialog, true);
	YAHOO.util.Event.addListener('previousDiagnosis_btn', 'click', pResultsDialog.cancel, pResultsDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:pResultsDialog.cancel,
	                                                scope:pResultsDialog,
	                                                correctScope:true } );
	pResultsDialog.cfg.queueProperty("keylisteners", escKeyListener);
	pResultsDialog.render();
}

function getPreviousDiagnosis(obj, mrNo, patientId) {
	pResultsDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	pResultsDialog.show();

	mrNo = encodeURIComponent(mrNo);
	patientId = encodeURIComponent(patientId);
	makeDiagnosisAjaxCall(mrNo, patientId);
}


function makeDiagnosisAjaxCall(mrNo, patientId, curPage) {
	document.getElementById('diagnosisprogressbar').style.visibility = 'none';
	var url = cpath + '/pages/registration/regUtils.do?_method=getPreviousDiagnosisDetails';
	url += "&patient_id="+patientId;
	url += "&mr_no="+mrNo;
	if (curPage)
		url += "&pageNum="+curPage;
	url += "&pageSize=10";

	YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: populateDiagnosisResultsDialog,
			failure: failedToGetDiagnosisPreviousResults,
			argument: [mrNo, patientId]
		});
}

function failedToGetDiagnosisPreviousResults() {
}

function populateDiagnosisResultsDialog(response) {
	document.getElementById('mrNoLabel').textContent = decodeURIComponent(response.argument[0]);
	var mrNo = decodeURIComponent(response.argument[0]);
	var patientId = decodeURIComponent(response.argument[1]);
	if (response.responseText != undefined) {
		var previousResults = eval('(' + response.responseText + ')');
		var table = document.getElementById("previousDiagnosisTable");
		var label = null;
		for (var i=1; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		previousResults = previousResults == null ? {dtoList : [], numPages: 0} : previousResults;
		var dtoList = previousResults.dtoList;
		generateDiagnosisPaginationSection(mrNo, patientId, previousResults.pageNumber, previousResults.numPages);

		var noResultsRow = table.rows[table.rows.length-1];
		noResultsRow.style.display = dtoList.length == 0 ? 'table-row' : 'none';
		var visitId = null;
		for (var i=0; i<dtoList.length; i++) {
			var record = dtoList[i];
			var templateRow = table.rows[table.rows.length-2];
			var row = templateRow.cloneNode(true);
			var id = table.rows.length-3;
			row.style.display = '';
			var inputEle = document.createElement('input');
				inputEle.setAttribute("type", "checkbox");
				inputEle.setAttribute("name", "prev_diagnosis_type_chkbox");
				inputEle.setAttribute("value", record.diag_type);
			if(record.code_type != diagnosis_code_type){
			    inputEle.setAttribute("disabled", "true");
			}
			table.tBodies[0].insertBefore(row, templateRow);
			row.cells[0].appendChild(inputEle);
			row.cells[1].setAttribute("style", visitId == null || visitId != record.visit_id ? "" : "border-top: none");
			setNodeText(row.cells[1], visitId == null || visitId != record.visit_id ? record.visit_id : '');
			setNodeText(row.cells[2], record.diagnosis_datetime ? formatDateTime(new Date(record.diagnosis_datetime)) : '');
			setNodeText(row.cells[3], record.doctor_name, 15);
			setNodeText(row.cells[4], record.diag_type == 'P' ? 'Principal' : (record.diag_type == 'V' ? 'Reason For Visit' : 'Secondary'));
			setNodeText(row.cells[5], record.code_type);
			setNodeText(row.cells[6], record.icd_code);
			setNodeText(row.cells[7], record.description, 20);
			setNodeText(row.cells[8], record.diagnosis_status_name, 20);
			setNodeText(row.cells[9], record.username, 15);
			setNodeText(row.cells[10], record.remarks, 50);
			visitId = record.visit_id;
		    setDiagnosisHiddenValue(id, "prev_diagnosis_id",'_');
   	        setDiagnosisHiddenValue(id, "prev_diagnosis_type", record.diag_type);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_code", record.icd_code);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_description", record.description);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_status_name", record.diagnosis_status_name);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_status_id", record.diagnosis_status_id);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_remarks", record.remarks);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_datetime", record.diagnosis_datetime ? formatDateTime(new Date(record.diagnosis_datetime)) : '');
	        setDiagnosisHiddenValue(id, "prev_diagnosis_doctor_id", record.doctor_id);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_doctor_name", record.doctor_name);
	        setDiagnosisHiddenValue(id, "prev_diagnosis_code_type", record.code_type);
	        setDiagnosisHiddenValue(id, "prev_year_of_onset", record.year_of_onset);
	        setDiagnosisHiddenValue(id, "prev_is_year_of_onset_mandatory", record.is_year_of_onset_mandatory);
	        setDiagnosisHiddenValue(id, "prev_health_authority", record.health_authority);
		}

	}
	document.getElementById('diagnosisprogressbar').style.visibility = 'hidden';

}

function generateDiagnosisPaginationSection(mrNo, patientId, curPage, numPages) {
	var div = document.getElementById('diagnosisPaginationDiv');
	div.innerHTML = '';

	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeDiagnosisAjaxCall("'+encodeURIComponent(mrNo)+'", "'+encodeURIComponent(patientId)+'", '+(curPage-1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}
		if (curPage > 1 && curPage < numPages) {
			var txtEl = document.createTextNode(' | ');
			div.appendChild(txtEl);
		}
		if (curPage < numPages) {
			var txtEl = document.createTextNode('Next>>');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeDiagnosisAjaxCall("'+encodeURIComponent(mrNo)+'", "'+encodeURIComponent(patientId)+'", '+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}

function validateDiagnosisDateTime(datefieldid, timefieldid, dateFieldTitle, timeFieldTitle, valid, validEmpty) {
	var dateField = document.getElementById(datefieldid);
	var timeField = document.getElementById(timefieldid);
	if (dateField.value == '' && timeField.value == '') {
		if (!validEmpty) {
			alert(getString("js.patient.diagnosis.enter.common")+" "+  dateFieldTitle+" & " + timeFieldTitle);
			return false;
		} else {
			return true;
		}
	}
	if (dateField.value != '' && timeField.value == '') {
		alert(getString("js.patient.diagnosis.enter.common")+" "+ timeFieldTitle + ".");
		return false;
	}
	if (timeField.value != '' && dateField.value == '') {
		alert(getString("js.patient.diagnosis.enter.common")+" "+ dateFieldTitle + ".");
		return false;
	}
	if (!doValidateDateField(dateField)) {
		return false;
	}
	if (!doValidateTimeField(timeField)) {
		return false;
	}
  	var d = getDateFromField(dateField);
	var time = timeField.value.split(":");
	d.setHours(time[0]);
	d.setMinutes(time[1]);
	var errorStr = validateDateTime(d, valid);
	if (errorStr != null) {
		alert(errorStr);
		return false;
	}
	return true;
}

function setDiagnosisHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[diagnosis_detatils_form], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
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

function getDiagnosisIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.forms[diagnosis_detatils_form], name, index));
}

function setDiagnosisIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[diagnosis_detatils_form], name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getDiagnosisIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[diagnosis_detatils_form], name, index);
	if (obj)
		return obj.value;
	else
	return null;
}
function addToTableFromPrevDiagnosis() {

	var selected_diag = document.getElementsByName('prev_diagnosis_type_chkbox');
	var checked_codes = 0;
	var valid_codes = 0;
	var consultingDoctorId = document.getElementById('diag_consulting_doctor_id').value;
	consultingDoctorId = empty(consultingDoctorId) ? diag_user_doctor_id : consultingDoctorId;
	var record = findInList(diagnosis_doctors_json, "doctor_id", consultingDoctorId);
	var consultingDoctorName = empty(consultingDoctorId) ? '' : record.doctor_name;
	for (var i=0; i<selected_diag.length; i++) {
		if (selected_diag[i].checked) {
		checked_codes = checked_codes + 1;
		if(validateDuplicateCode(i)) {
		    valid_codes = valid_codes + 1;
			var id = getNumCharges('diagnosisDetailsTable');
		   	var table = document.getElementById("diagnosisDetailsTable");
			var templateRow = table.rows[getTemplateRow('diagnosisDetailsTable')];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
		   	row.id = "itemRow" + id;

			var diag_type = document.getElementsByName('prev_diagnosis_type')[i].value;
			//chnages Made for hms-8376
	        //var diagnosis_doctor = document.getElementsByName('prev_diagnosis_doctor_name')[i].value;
	        //var diagnosis_datetime = document.getElementsByName('prev_diagnosis_datetime')[i].value;
	        var diagnosis_code = document.getElementsByName('prev_diagnosis_code')[i].value;
	        var diag_codes = document.getElementsByName('diagnosis_code');
	        var diagnosis_status_name = document.getElementsByName('prev_diagnosis_status_name')[i].value;
	        var diagnosis_description = document.getElementsByName('prev_diagnosis_description')[i].value;
	        var diagnosis_remarks = document.getElementsByName('prev_diagnosis_remarks')[i].value;
            var statusId = document.getElementsByName('prev_diagnosis_status_id')[i].value;
            var doctorId = document.getElementsByName('prev_diagnosis_doctor_id')[i].value;
            var codeType = document.getElementsByName('prev_diagnosis_code_type')[i].value;
            var diagYearOfOnset = document.getElementsByName('prev_year_of_onset')[i].value;
            isYearOfOnSetMandatory = document.getElementsByName('prev_is_year_of_onset_mandatory')[i].value;
            healthAuthority = document.getElementsByName('prev_health_authority')[i].value;

			setNodeText(row.cells[DIAG_DATETIME], currentDateAndTime);
	        setNodeText(row.cells[DIAG_DOCTOR], consultingDoctorName, 20);
	        setNodeText(row.cells[DIAG_TYPE], diag_type == 'P' ? 'Principal' : (diag_type == 'V' ? 'Reason For Visit' : 'Secondary'));
	        setNodeText(row.cells[DIAG_CODE_TYPE], codeType, 10);
   	        setNodeText(row.cells[DIAG_CODE], diagnosis_code, 20);
   	        setNodeText(row.cells[DIAG_DESC], diagnosis_description, 30);
   	        setNodeText(row.cells[DIAG_STATUS], diagnosis_status_name, 20);
   	        setNodeText(row.cells[DIAG_REMARKS], diagnosis_remarks, 30);

            setDiagnosisHiddenValue(id, "diagnosis_id", '_');
   	        setDiagnosisHiddenValue(id, "diagnosis_type", diag_type);
	        setDiagnosisHiddenValue(id, "diagnosis_code", diagnosis_code );
	        setDiagnosisHiddenValue(id, "diagnosis_description", diagnosis_description);
	        setDiagnosisHiddenValue(id, "diagnosis_year_of_onset", diagYearOfOnset);
	        setDiagnosisHiddenValue(id, "diagnosis_status_id", statusId);
	        setDiagnosisHiddenValue(id, "diagnosis_status_name", diagnosis_status_name);
	        setDiagnosisHiddenValue(id, "diagnosis_remarks", diagnosis_remarks);
	        setDiagnosisHiddenValue(id, "diagnosis_datetime", currentDateAndTime);
	        setDiagnosisHiddenValue(id, "diagnosis_doctor_id", consultingDoctorId);
	        setDiagnosisHiddenValue(id, "diagnosis_doctor_name", consultingDoctorName);
	        setDiagnosisHiddenValue(id, "diagnosis_code_type", codeType);
	        setDiagnosisHiddenValue(id, "is_year_of_onset_mandatory", isYearOfOnSetMandatory);
	        setDiagnosisHiddenValue(id, "health_authority", healthAuthority);

			setDiagnosisRowStyle(id);
		  }
		}
	  }
	  document.getElementById("previousDiagnosisDetailsDiv").scrollIntoView(true);
	  if(checked_codes == valid_codes){
	   pResultsDialog.cancel();
	}
}

function validateDuplicateCode(num) {
    var code = document.getElementsByName('prev_diagnosis_code')[num].value
    var diag_codes = document.getElementsByName('diagnosis_code');
		for(var i = 0; i<diag_codes.length-1; i++) {
		   if(diag_codes[i].value == code){
		       alert(getString('js.patient.diagnosis.duplicate.code')+' '+code);
		       document.getElementsByName('prev_diagnosis_code')[num].focus();
		       return false ;
		    }
		}
    return true;
}

function isCodeTypeSameForAll() {
	var codeTypes = document.getElementsByName("diagnosis_code_type");
	var deleted = document.getElementsByName("diagnosis_deleted");
	var type = '';
	for (var i=0; i<codeTypes.length-1; i++) {
		if (deleted[i].value != 'true' && type != '' && type != codeTypes[i].value) {
			return false;
		}
		if (deleted[i].value != 'true')
			type = codeTypes[i].value;

	}
	return true;
}

