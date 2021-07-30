function captureSubmitEvent() {
	var form = document.NurseNotes;
	form.validateFormSubmit = form.submit;

	form.submit = function validatedSubmit() {
		if (!blockSubmit()) {
			var e = xGetElementById(document.NurseNotes);
			YAHOO.util.Event.stopEvent(e);
			return false;
		}
		form.validateFormSubmit();
		return true;
	};
}
function blockSubmit() {
	if (document.getElementById('patient_discharged').value == 'true') {
		alert("Patient is inactive or discharged, add/edit/delete of nurse note is not allowed.");
		return false;
	}
	return true;
}

function init() {
	document.getElementById('addNewNoteBtn').focus();
	captureSubmitEvent();
	initEditItemDialog();
	initLoginDialog();
}

function addNoteForm() {
	document.getElementById('addNoteDialog').style.display = 'block';
	document.getElementById('notes').focus();
	return true;
}

function closeNote() {
	clearFields();
	document.getElementById('addNoteDialog').style.display = 'none';
	return false;
}

function clearFields() {
	document.getElementById('notes').value = '';
	document.getElementById('chk_finalized').checked = false;
}

function changeFinalized(){
	var finalized = document.getElementById('chk_finalized');
	if (finalized.checked) {
		document.getElementById('finalized').value = 'Y';
	} else {
		document.getElementById('finalized').value = 'N';
	}
}

function emrPrintNotes() {
	var patientId = document.getElementById('patient_id').value;
	window.open(cpath+"/wardactivities/NurseNotes.do?_method=generateReport&patient_id="+patientId);
}

function onClickSave() {
	if (!validateSubmit())
		return false;
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.NurseNotes.submit();
	}
	document.getElementById('authUser').value = document.getElementById('loginUser').value;
	return true;
}

function submitHandler() {
	document.getElementById('authUser').value = document.getElementById('login_user').value;
	document.NurseNotes.submit();
	return false;
}

function validateSubmit() {
	if (document.getElementById('notes').value.trim() == '') {
		alert('Please enter notes');
		document.getElementById('notes').focus();
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
	return true;
}

function cancelNurseNote(obj,index) {
	var oldDeleted = document.getElementById('h_delete_note'+index).value;
	if (!confirm('Nurse Note will be deleted')) return false;
	var newDeleted;
	if(oldDeleted == 'N') {
		document.getElementById('h_delete_note'+index).value = 'Y';
	}
	document.NurseNotes._method.value = 'delete';
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.NurseNotes.submit();
	}
	return true;
}

function showEditNurseNoteDialog(obj, index) {
	var oldEdited = document.getElementById('h_edited'+index).value;
	if(oldEdited == 'false') {
		document.getElementById('h_edited'+index).value = 'true';
	}
	var index = index-1;
	var dialogDiv = document.getElementById("editItemDialog");
	dialogDiv.style.display = 'block';
	editItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editItemDialog.show();
	document.NurseNotes.editRowId.value = index;

	document.getElementById('ed_entered_date').disabled = false;
	document.getElementById('ed_entered_time').disabled = false;

	document.getElementById('ed_note_id').value = getIndexedValue('h_note_id', index);
	document.getElementById('ed_entered_date').value = getIndexedValue('h_creation_datetime', index).split(' ')[0];
	document.getElementById('ed_entered_time').value = getIndexedValue('h_creation_datetime', index).split(' ')[1];
	document.getElementById('ed_notes').value = getIndexedValue('h_notes', index);
	document.getElementById('ed_entered_by_label').textContent = getIndexedValue('h_mod_user', index);
	document.getElementById('ed_mod_user').value = getIndexedValue('h_mod_user', index);
	document.getElementById('edit_finalized').value = getIndexedValue('h_finalized', index);
	document.getElementById('ed_finalized').value = getIndexedValue('h_finalized', index);
	document.getElementById('ed_htover').value = '';
	document.getElementById('ed_htover').value = getIndexedValue('h_htover', index);

	if(document.getElementById('ed_finalized').value == 'Y') {
		document.getElementById('ed_finalized').checked = true;
	} else {
		document.getElementById('ed_finalized').checked = false;
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
		document.NurseNotes.submit();
	}
	return true;
}

function editTableRow() {
	if (document.getElementById('ed_notes').value.trim() == '') {
		alert('Please enter notes');
		document.getElementById('ed_notes').focus();
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
	document.NurseNotes._method.value = 'update';
	return true;
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
	return getElementPaise(getIndexedFormElement(document.NurseNotes, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.NurseNotes, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.NurseNotes, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.NurseNotes, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

