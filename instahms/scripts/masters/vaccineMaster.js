
function init() {
	initializeAddDialog();
	initializeEditDialog();
	initializeDeleteDosageDialog();
}

function disableAddIcon() {
	var table = document.getElementById('dosageTable');
	if (single_dose == 'Y' && table.rows.length == 3) {
		document.getElementById('btnAddItem').disabled = true;
		document.getElementById('btnAddItem').getElementsByTagName('img')[0].src = cpath + '/icons/Add1.png';
	}
}

function initializeAddDialog() {

	dosageDialog = document.getElementById('addDosageDialog');
	dosageDialog.style.display = 'block';
	addDosageDialog = new YAHOO.widget.Dialog("addDosageDialog", {
							width: "430px",
							context : ["dosageTable", "tr", "br"],
							visible:false,
							modal:true,
							constraintoviewport:true
						});

	YAHOO.util.Event.addListener('DosageAdd', 'click', addDosageToTable, addDosageDialog, true);
	YAHOO.util.Event.addListener('DosageClose', 'click', handleAddDosageCancel, addDosageDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddDosageCancel,
	                                                scope:addDosageDialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("addDosageFormFieldsDiv", { keys:13 },
				{ fn:addDosageToTable, scope:addDosageDialog, correctScope:true } );
	addDosageDialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	addDosageDialog.render();
}

function initializeEditDialog() {

	dosageDialog = document.getElementById('editDosageDialog');
	dosageDialog.style.display = 'block';
	editDosageDialog = new YAHOO.widget.Dialog("editDosageDialog", {
							width: "430px",
							context : ["dosageTable", "tr", "br"],
							visible:false,
							modal:true,
							constraintoviewport:true
						});

	YAHOO.util.Event.addListener('ed_Add', 'click', editDosageToTable, editDosageDialog, true);
	YAHOO.util.Event.addListener('ed_Cancel', 'click', handleEditDosageCancel, editDosageDialog, true);
	YAHOO.util.Event.addListener('ed_Previous', 'click', handlePrevious, editDosageDialog, true);
	YAHOO.util.Event.addListener('ed_Next', 'click', handleNext, editDosageDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditDosageCancel,
	                                                scope:editDosageDialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("editDosageDialogFields", { keys:13 },
				{ fn:editDosageToTable, scope:editDosageDialog, correctScope:true } );
	editDosageDialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	editDosageDialog.render();
}

function initializeDeleteDosageDialog() {
	deleteDosageDialogElement = document.getElementById('deleteDosageDialog');
	deleteDosageDialogElement.style.display = 'block';
	deleteDosageDialog = new YAHOO.widget.Dialog("deleteDosageDialog", {
							width: "430px",
							context : ["dosageTable", "tr", "br"],
							visible:false,
							modal:true,
							constraintoviewport:true
						});

	YAHOO.util.Event.addListener('deleteConfirm', 'click', deleteDosageDetails, deleteDosageDialog, true);
	YAHOO.util.Event.addListener('deleteCancel', 'click', handleDeleteDosageCancel, deleteDosageDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleDeleteDosageCancel,
	                                                scope:deleteDosageDialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("deleteDosageDialogFields", { keys:13 },
				{ fn:deleteDosageDetails, scope:deleteDosageDialog, correctScope:true } );
	deleteDosageDialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	deleteDosageDialog.render();
}

/*function getDosageCount() {
	var table = document.getElementById('dosageTable');
	var tableLen = table.rows.length-1;
	var count = 0;
	for (var i=1; i<= tableLen; i++) {
		var row = table.rows[i];
		var isDeleted = getIndexedValue('is_deleted', row.rowIndex-1);
		if (isDeleted != 'Y')
			count++;
	}
	return count;
}*/

function showAddDosageDialog(obj) {
	addDosageDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addDosageDialog.show();
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.dosageForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getNumCharges(tableId) {

	return document.getElementById(tableId).rows.length-2;
}

function getTemplateRow(tableId) {

	return getNumCharges(tableId) + 1;
}

var DOSAGE = 0, RECOMENDED_AGE = 1, NOTIFICATION_TIME = 2, DOSAGE_TRASH = 3, DOSAGE_EDIT = 4;

function addDosageToTable() {

	var d_a_dose = '';
	var d_a_recommended_age = document.getElementById('d_a_recommended_age').value;
	var d_a_age_in = document.getElementsByName('d_a_age_in');
	var d_a_notification_lead_time = document.getElementById('d_a_notification_lead_time').value;
	var d_a_status = document.getElementById('d_a_status').value;
	if (single_dose == 'N') {
		d_a_dose = document.getElementById('d_a_dose').value;
		if (d_a_dose == '') {
			alert('Please enter Dose');
			document.getElementById('d_a_dose').focus();
			return false;
		}
		if (!checkDuplicateDosage(d_a_dose, -1)) {
			return false;
		}
	}

	if (d_a_recommended_age == '') {
		alert('Please enter Recommended Age.');
		document.getElementById('d_a_recommended_age').focus();
		return false;
	}

	if (d_a_notification_lead_time == '') {
		alert('Please enter Notification Lead Time.');
		document.getElementById('d_a_notification_lead_time').focus();
		return false;
	}

	var age_in_value = '';

	for (var i=0; i<d_a_age_in.length; i++) {
		if (d_a_age_in[i].checked) {
			age_in_value = d_a_age_in[i].value;
		}
	}

	if (age_in_value == '') {
		alert('Please enter Age units.');
		return false;
	}

	var table = document.getElementById('dosageTable');
	var id = getNumCharges('dosageTable');
	var templateRow = table.rows[getTemplateRow('dosageTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);

	var age_in_text = age_in_value == 'W' ? 'Weeks' : age_in_value == 'M' ? 'Months' : 'Years';

	setHiddenValue(id, 'dose_num', d_a_dose);
	setHiddenValue(id, 'recommended_age', d_a_recommended_age);
	setHiddenValue(id, 'age_units', age_in_value);
	setHiddenValue(id, 'notification_lead_time_days', d_a_notification_lead_time);
	setHiddenValue(id, 'status', d_a_status);
	setHiddenValue(id, 'is_new', 'Y');

	if (parseInt(d_a_recommended_age) == 1) {
		parts = age_in_text.split('s');
		age_in_text = parts[0];
	}

	if (d_a_status == 'I')
		row.cells[DOSAGE].getElementsByTagName('img')[0].src = cpath+'/images/grey_flag.gif';
	else
		row.cells[DOSAGE].getElementsByTagName('img')[0].src = cpath+'/images/empty_flag.gif';
	setNodeText(row.cells[RECOMENDED_AGE], d_a_recommended_age+' '+age_in_text);
	setNodeText(row.cells[NOTIFICATION_TIME], d_a_notification_lead_time+" days");
	if (single_dose == 'N')
		setNodeText(row.cells[DOSAGE], d_a_dose);

	clearFields();
	this.align("tr", "tl");

	return id;
}

function clearFields() {

	if (single_dose == 'N')
		document.getElementById('d_a_dose').value = '';
	document.getElementById('d_a_recommended_age').value = '';
	document.getElementsByName('d_a_age_in')[0].checked = true;
	document.getElementById('d_a_notification_lead_time').value = '';
	document.getElementById('d_a_status').value = 'A';
}

function handleAddDosageCancel() {
	addDosageDialog.cancel();
}

function handleEditDosageCancel() {
	editDosageDialog.cancel();
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.dosageForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function showEditDosageDialog(obj) {

	var row = getThisRow(obj, 'TR');
	var index = row.rowIndex -1;
	document.getElementById('dosage_edit_row_id').value = index;
	editDosageDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editDosageDialog.show();

	if (single_dose == 'N')
		document.getElementById('d_e_dose').value = getIndexedValue('dose_num', index);
	document.getElementById('d_e_recommended_age').value = getIndexedValue('recommended_age', index);;
	document.getElementById('d_e_notification_lead_time').value = getIndexedValue('notification_lead_time_days', index);;
	document.getElementById('d_e_status').value = getIndexedValue('status', index);
	var ageIn = document.getElementsByName('d_e_age_in');
	var ageInVal = getIndexedValue('age_units', index);

	for (var i=0; i<ageIn.length; i++) {
		if (ageIn[i].value == ageInVal) {
			ageIn[i].checked  = true;
		}
	}
}

function getDosageRow(id) {
	var table = document.getElementById('dosageTable');
	var row = table.rows[parseInt(id)+1];
	return row;
}


function editDosageToTable() {
	var id = document.getElementById('dosage_edit_row_id').value;
	var row = getDosageRow(id);

	var existingStatus = getIndexedValue('status', row.rowIndex-1);
	var vaccineDoseID = getIndexedValue('vaccine_dose_id', row.rowIndex-1);
	var d_e_status = document.getElementById('d_e_status').value;
	var d_e_dose = '';
	var d_e_recommended_age = document.getElementById('d_e_recommended_age').value;
	var d_e_age_in = document.getElementsByName('d_e_age_in');
	var d_e_notification_lead_time = document.getElementById('d_e_notification_lead_time').value;

	if (single_dose == 'N') {
		d_e_dose = document.getElementById('d_e_dose').value;
		if (d_e_dose == '') {
			alert('Please enter Dose');
			document.getElementById('d_e_dose').focus();
			return false;
		}
		if (!checkDuplicateDosage(d_e_dose, row.rowIndex-1)) {
			return false;
		}
	}

	if (d_e_recommended_age == '') {
		alert('Please enter Recommended Age.');
		document.getElementById('d_e_recommended_age').focus();
		return false;
	}

	if (d_e_notification_lead_time == '') {
		alert('Please enter Notification Lead Time.');
		document.getElementById('d_e_notification_lead_time').focus();
		return false;
	}

	var age_in_value = '';
	for (var i=0; i<d_e_age_in.length; i++) {
		if (d_e_age_in[i].checked) {
			age_in_value = d_e_age_in[i].value;
		}
	}

	if (age_in_value == '') {
		alert('Please enter Age units.');
		return false;
	}

	var age_in_text = age_in_value == 'W' ? 'Weeks' : age_in_value == 'M' ? 'Months' : 'Years';

	setHiddenValue(id, 'dose_num', d_e_dose);
	setHiddenValue(id, 'recommended_age', d_e_recommended_age);
	setHiddenValue(id, 'age_units', age_in_value);
	setHiddenValue(id, 'notification_lead_time_days', d_e_notification_lead_time);
	setHiddenValue(id, 'status', d_e_status);
	setHiddenValue(id, 'is_edited', 'Y');

	if (parseInt(d_e_recommended_age) == 1) {
		parts = age_in_text.split('s');
		age_in_text = parts[0];
	}
	if (d_e_status == 'I')
		row.cells[DOSAGE].getElementsByTagName('img')[0].src = cpath+'/images/grey_flag.gif';
	else
		row.cells[DOSAGE].getElementsByTagName('img')[0].src = cpath+'/images/empty_flag.gif';
	setNodeText(row.cells[DOSAGE], d_e_dose);
	setNodeText(row.cells[RECOMENDED_AGE], d_e_recommended_age+' '+age_in_text);
	setNodeText(row.cells[NOTIFICATION_TIME], d_e_notification_lead_time+" days");

	editDosageDialog.cancel();
	return true;

}

function handlePrevious() {

	var id = document.getElementById('dosage_edit_row_id').value;
	var row = getDosageRow(id);
	if (!editDosageToTable()) {
		return false;
	}
	if (id != 0) {
		showEditDosageDialog(document.getElementsByName('dosage_editAnchor')[parseInt(id)-1]);
	} else {
		editDosageDialog.cancel();
	}
}

function handleNext() {

	var id = document.getElementById('dosage_edit_row_id').value;
	var row = getDosageRow(id);
	if (!editDosageToTable()) {
		return false;
	}
	if (parseInt(id)+1 != document.getElementById('dosageTable').rows.length-2) {
		showEditDosageDialog(document.getElementsByName('dosage_editAnchor')[parseInt(id)+1]);
	} else {
		editDosageDialog.cancel();
	}
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.dosageForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function updateDosageNos() {
	var tableObj = document.getElementById('dosageTable');
	var tableLen = tableObj.rows.length-2;
	var row = null;
	var isDeleted = null;
	var dosageSeq = 0;

	for (var i=1; i <= tableLen; i++) {
		row = tableObj.rows[i];
		isDeleted = getIndexedValue('is_deleted', i-1);

		if (isDeleted == 'Y') {
		  setIndexedValue('dose_num', i-1, '');
		  setNodeText(row.cells[DOSAGE], '');
		} else {
			dosageSeq++;
			setIndexedValue('dose_num', i-1, dosageSeq);
			setNodeText(row.cells[DOSAGE], dosageSeq);
		}
	}
}

function validate() {

	var table = document.getElementById('dosageTable');
	var tableLen = table.rows.length-2;
	var inactivateCount = 0;
	var activeCount = 0;
	for (var i=1; i<= tableLen; i++) {
		var row = table.rows[i];
		var status = getIndexedValue('status', row.rowIndex-1);
		if (status == 'I') {
			inactivateCount++;
		} else if (status == 'A') {
			activeCount++;
		}
	}

	if (single_dose == 'Y' && activeCount >1) {
		alert('Only at most one active dosage should exist for single dose vaccine.');
		return false;
	}

	if (tableLen == inactivateCount || tableLen == 0) {
		alert('At least one active dosage should exist.');
		return false;
	}
}

function checkDuplicateDosage(enteredDoseNo, editRow) {

	var table = document.getElementById('dosageTable');
	var tableLen = table.rows.length-2;
	var inactivatedDoses = eval(inactivatedDoseNos);

	for (var i=1; i<=tableLen; i++) {
		var row = table.rows[i];
		doseNumber = getIndexedValue('dose_num', row.rowIndex-1);
		if (parseInt(enteredDoseNo) == parseInt(doseNumber) && parseInt(editRow) != parseInt(row.rowIndex-1)) {
			alert('Dose number already exist.');
			return false;
		}
	}

	var dosageId = getIndexedValue('vaccine_dose_id', editRow);

	for (var i=0; i<inactivatedDoses.length; i++) {
		if (parseInt(enteredDoseNo) == parseInt(inactivatedDoses[i]['dose_num']) && parseInt(dosageId) != parseInt(inactivatedDoses[i]['vaccine_dose_id'])) {
			alert('Dose number already exist for inactivated dose.');
			return false;
		}
	}

	return true;
}

function showDeleteDosageDetailsDialog(obj) {
    var row = getThisRow(obj, 'TR');
    var index = row.rowIndex -1;
    var dosageNumber = getIndexedValue('dose_num', index);
    document.getElementById('dosage_delete_row_id').value = index;
    deleteDosageDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
    deleteDosageDialog.show();
    document.getElementById("deleteText").textContent = "Do you want to delete Dose Number : "+ dosageNumber + " ? ";
}

function deleteDosageDetails() {
  var id = document.getElementById('dosage_delete_row_id').value;
  var row = getDosageRow(id);
  var dosageNumber = getIndexedValue('dose_num', row.rowIndex-1);
  setHiddenValue(id,'is_deleted', 'Y');
  document.getElementById('dosage_'+dosageNumber).style='display: none';
  document.getElementById('dosageDeleteIcon_'+(dosageNumber-1)).src = cpath+"/icons/Delete.png";
  document.getElementById('dosageDeleteLink_'+(dosageNumber-1)).style= "pointer-events: all";
  deleteDosageDialog.cancel();
}

function handleDeleteDosageCancel() {
  deleteDosageDialog.cancel();
}
