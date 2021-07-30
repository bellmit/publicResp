var d_PACDocAutoComp = null;
var ed_PACDocAutoComp = null;

function initPreAnaCheck() {
	initPACDialog();
	initEditPACDialog();
	filterResults(document.getElementById('hide_invalid'));
	d_PACDocAutoComp = initPACDoctorAC('d_pac_doctor_name', 'd_pac_doctor_container', false, '');
}

function pacEntered() {
	return document.getElementById('preAnaesthestheticTable').rows.length > 2;
}

function initPACDoctorAC(inputElId, containerId, edit, doctorId) {
	var doctorList = [];
	for (var i=0; i<pre_anaesthesthetic_doctors_json.length; i++) {
		if (pre_anaesthesthetic_doctors_json[i].status == 'A' || doctorId == pre_anaesthesthetic_doctors_json[i].doctor_id) {
			doctorList.push(pre_anaesthesthetic_doctors_json[i]);
		}
	}
	if (edit && !empty(ed_PACDocAutoComp)) {
		ed_PACDocAutoComp.destroy();
		ed_PACDocAutoComp = null;
	} else if (!empty(d_PACDocAutoComp)) {
		d_PACDocAutoComp.destroy();
		d_PACDocAutoComp = null;
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

	autoComp.itemSelectEvent.subscribe(setPACDoctor);
	autoComp.selectionEnforceEvent.subscribe(clearPACDoctor);

	return autoComp;
}

function setPACDoctor(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_pac_doctor_id").value = sArgs[2].doctor_id;
}

function clearPACDoctor(oSelf, sClearedValue) {
	var elId = sClearedValue[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_pac_doctor_id").value = '';
}

function filterResults(obj) {
	var todayDate = new Date();
	var els = document.getElementsByName('patient_pac_id');
	for (var i=0; i<els.length-1; i++) {
		if (obj.checked) {
			var validity_date = document.getElementsByName('validity_date')[i].value;
			var validityDate = parseDateStr(validity_date);
			if (daysDiff(todayDate, validityDate) < 0) {
				getThisRow(document.getElementsByName('patient_pac_id')[i]).style.display = 'none';
				continue;
			}
		} else {
			getThisRow(document.getElementsByName('patient_pac_id')[i]).style.display = 'table-row';
			continue;
		}
	}
}

function formatDueDate(dateMSecs) {
	var dateObj = new Date(dateMSecs);
	var dateStr = formatDate(dateObj, 'ddmmyyyy', '-');
	return dateStr;
}

var addPreAnaesDialog = null;
function initPACDialog() {
	var dialogPACDiv = document.getElementById("addPreAnaesDialog");
	if (dialogPACDiv == undefined) return;

	dialogPACDiv.style.display = 'block';
	addPreAnaesDialog = new YAHOO.widget.Dialog("addPreAnaesDialog",
			{	width:"630px",
				context : ["addPreAnaesDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add_PreAnaes_bt', 'click', addToPACTable, addPreAnaesDialog, true);
	YAHOO.util.Event.addListener('Close_PreAnaes_bt', 'click', handleAddPACCancel, addPreAnaesDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddPACCancel,
	                                                scope:addPreAnaesDialog,
	                                                correctScope:true } );
	addPreAnaesDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addPreAnaesDialog.render();
}

function handleAddPACCancel() {
	if (childPACDialog == null) {
		parentPACDialog = null;
		this.cancel();
	}
}

var parentPACDialog = null;
var childPACDialog = null;
function showAddPreAnaesDialog(obj) {
	var row = getPACThisRow(obj);
	clearPACFields();
	addPreAnaesDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);

	if (screenid == 'out_pat_reg' || (screenid == 'preauth_presc' && op_type == 'O') ) {
		document.getElementById('d_pac_doctor_name').disabled = true;

	} if (screenid == 'op_prescribe') {
		var consultingDoctorId = document.getElementById('pac_consulting_doctor_id').value;
		consultingDoctorId = empty(consultingDoctorId) ? pac_user_doctor_id : consultingDoctorId;
		var record = findInList(pre_anaesthesthetic_doctors_json, "doctor_id", consultingDoctorId);

		var consultingDoctorName = empty(consultingDoctorId) ? '' : record.doctor_name;
		document.getElementById('d_pac_doctor_name').disabled = true;
		document.getElementById('d_pac_doctor_name').value = consultingDoctorName;
		document.getElementById('d_pac_doctor_id').value = consultingDoctorId;
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_pac_doctor_name', 'd_pac_doctor_container', false, consultingDoctorId);
		if (document.getElementById('d_pac_doctor_name').value != '') {
			d_diagnosisDocAutoComp._bItemSelected = true;
			d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_pac_doctor_name').value;
		}

	} else if (!empty(pac_user_doctor_id)) {
		// populate the user doctor in ip visit summary/outside patient registration screens.
		var record = findInList(pre_anaesthesthetic_doctors_json, "doctor_id", pac_user_doctor_id);
		var userDoctorName = record.doctor_name
		document.getElementById('d_pac_doctor_name').value = userDoctorName;
		document.getElementById('d_pac_doctor_id').value = user_doctor_id;
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_pac_doctor_name', 'd_pac_doctor_container', false, user_doctor_id);
		if (document.getElementById('d_pac_doctor_name').value != '') {
			d_diagnosisDocAutoComp._bItemSelected = true;
			d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_pac_doctor_name').value;
		}
	}
	addPreAnaesDialog.show();
	document.getElementById('d_pac_remarks').focus();
	parentPACDialog = addPreAnaesDialog;
	return false;
}


var pacColIndex  = 0;
var pacAdded = 0;
var PAC_DOCTOR = pacColIndex++, PAC_CONDUCTED_DATETIME = pacColIndex++, PAC_VALIDITY_DATETIME = pacColIndex++, PAC_STATUS =  pacColIndex++,
	PAC_REMARK = pacColIndex++, PAC_TRASH_COL = pacColIndex++, PAC_EDIT_COL = pacColIndex++;

function addToPACTable() {
	var remarks = document.getElementById('d_pac_remarks').value;
	if (remarks == '') {
   		alert(getString("js.outpatient.consultation.mgmt.enterpreanaesthetic.evaluvationmarks"));
   		document.getElementById('d_pac_remarks').focus();
   		return false;
   	}
	if (!validatePACDateTime('d_conducted_date', 'd_conducted_time', 'Date', 'Time', null, false))
		return false;
	if (!validatePACDateTime('d_validity_date', 'd_validity_time', 'Date', 'Time', null, false))
		return false;
	var conducted_date = document.getElementById('d_conducted_date').value;
	var validity_date = document.getElementById('d_validity_date').value;
	if(getDateDiff(conducted_date, validity_date) < 0){
		alert(getString("js.outpatient.consultation.mgmt.validitydategreater.equal.conductiondate") + conducted_date +")");
		return false;
	}

	var id = getPACNumCharges('preAnaesthestheticTable');
   	var table = document.getElementById("preAnaesthestheticTable");
	var templateRow = table.rows[getPACTemplateRow('preAnaesthestheticTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	var pacId = document.getElementById('d_patient_pac_id').value;
   	var pac_doctor_name = document.getElementById("d_pac_doctor_name").value;
   	var pac_doctor_id = document.getElementById('d_pac_doctor_id').value;
   	var status_radio_els = document.getElementsByName('d_anaesthesthetic_status');
	var pac_status;
	for (var k=0; k<status_radio_els.length; k++) {
		if (status_radio_els[k].checked) {
			pac_status = status_radio_els[k].value;
			break;
		}
	}
   	var status = pac_status;
   	var conducted_datetime = conducted_date + " " + document.getElementById('d_conducted_time').value;
   	var validity_datetime = validity_date + " " + document.getElementById('d_validity_time').value;
   	var remarks = document.getElementById('d_pac_remarks').value;

	setNodeText(row.cells[PAC_DOCTOR], pac_doctor_name, 20);
	setNodeText(row.cells[PAC_CONDUCTED_DATETIME], conducted_datetime);
	setNodeText(row.cells[PAC_VALIDITY_DATETIME], validity_datetime);
	setNodeText(row.cells[PAC_STATUS], status == 'F' ? 'Fit' : status == 'U' ? 'Unfit' : "");
	setNodeText(row.cells[PAC_REMARK], remarks, 80, remarks);

	setPACHiddenValue(id, "patient_pac_id", '_');
   	setPACHiddenValue(id, "pac_doctor_id", pac_doctor_id);
   	setPACHiddenValue(id, "pac_doctor_name", pac_doctor_name);
   	setPACHiddenValue(id, "pac_status", status);
	setPACHiddenValue(id, "conducted_date", conducted_datetime);
	setPACHiddenValue(id, "validity_date", validity_datetime);
   	setPACHiddenValue(id, "pac_remarks", remarks);

   	pacAdded++;
	clearPACFields();
	setPACRowStyle(id);
	addPreAnaesDialog.align("tr", "tl");
	document.getElementById('d_pac_remarks').focus();
	return id;
}

function setPACHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[pre_anaesthesthetic_form], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearPACFields() {
	var status_radio_els = document.getElementsByName('d_anaesthesthetic_status');
	for (var k=0; k<status_radio_els.length; k++) {
		if (status_radio_els[k].value == 'F') {
			status_radio_els[k].checked = true;
			break;
		}
	}
	if (screenid != 'op_prescribe') {
		document.getElementById('d_pac_doctor_name').value = '';
		document.getElementById('d_pac_doctor_id').value = '';
		d_diagnosisDocAutoComp = initDiagDoctorAC('d_pac_doctor_name', 'd_pac_doctor_container', false, pac_user_doctor_id);
		if (!empty(pac_user_doctor_id)) {
			var record = findInList(pre_anaesthesthetic_doctors_json, "doctor_id", pac_user_doctor_id);
			var userDoctorName = record.doctor_name
			document.getElementById('d_pac_doctor_name').value = userDoctorName;
			document.getElementById('d_pac_doctor_id').value = user_doctor_id;
			if (document.getElementById('d_pac_doctor_name').value != '') {
				d_diagnosisDocAutoComp._bItemSelected = true;
				d_diagnosisDocAutoComp._sInitInputValue = document.getElementById('d_pac_doctor_name').value;
			}
		}
	}
	document.getElementById('d_conducted_date').value = formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_conducted_time').value = formatTime(new Date(), false);
	var validityDate = new Date();
	validityDate.setDate(validityDate.getDate() + validityDays);
	document.getElementById('d_validity_date').value = formatDate(validityDate, 'ddmmyyyy', '-');
	document.getElementById('d_validity_time').value = formatTime(validityDate, false);
	document.getElementById('d_pac_remarks').value = '';
}

function setPACRowStyle(i) {
	var row = getPACChargeRow(i, 'preAnaesthestheticTable');
	var patientPacId = getPACIndexedValue("patient_pac_id", i);

	var trashImgs = row.cells[PAC_TRASH_COL].getElementsByTagName("img");

	var added = (patientPacId.substring(0,1) == "_");
	var cancelled = getPACIndexedValue("delPreAnaes", i) == 'true';
	var edited = getPACIndexedValue("preAnaes_edited", i) == 'true';

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

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelPreAnaes(obj) {

	var row = getPACThisRow(obj);
	var id = getPACRowChargeIndex(row);
	var oldDeleted =  getPACIndexedValue("delPreAnaes", id);

	var isNew = getPACIndexedValue("patient_pac_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		pacAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setPACIndexedValue("delPreAnaes", id, newDeleted);
		setPACIndexedValue("preAnaes_edited", id, "true");
		setPACRowStyle(id);
	}
	return false;
}

var editPreAnaesDialog = null;
function initEditPACDialog() {
	var dialogEditPACDiv = document.getElementById("editPreAnaesDialog");
	dialogEditPACDiv.style.display = 'block';
	editPreAnaesDialog = new YAHOO.widget.Dialog("editPreAnaesDialog",{
			width:"630px",
			text: "Edit Patient PAC",
			context :["preAnaesthestheticTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditPACCancel,
	                                                scope:editPreAnaesDialog,
	                                                correctScope:true } );
	editPreAnaesDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editPreAnaesDialog.cancelEvent.subscribe(handleEditPACCancel);
	YAHOO.util.Event.addListener('edit_preAnaes_Ok', 'click', editPACTableRow, editPreAnaesDialog, true);
	YAHOO.util.Event.addListener('edit_preAnaes_Cancel', 'click', handleEditPACCancel, editPreAnaesDialog, true);
	YAHOO.util.Event.addListener('edit_preAnaes_Previous', 'click', openPACPrevious, editPreAnaesDialog, true);
	YAHOO.util.Event.addListener('edit_preAnaes_Next', 'click', openPACNext, editPreAnaesDialog, true);
	editPreAnaesDialog.render();
}

function handleEditPACCancel() {
	if (childPACDialog == null) {
		parentPACDialog = null;
		var id = document.forms[pre_anaesthesthetic_form].editPreAnaesRowId.value;
		var row = getPACChargeRow(id, "preAnaesthestheticTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldPACEdited = false;
		this.hide();
	}
}

function showEditPreAnaesDialog(obj) {
	parentPACDialog = editPreAnaesDialog;
	var row = getPACThisRow(obj);
	var id = getPACRowChargeIndex(row);
	editPreAnaesDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editPreAnaesDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.forms[pre_anaesthesthetic_form].editPreAnaesRowId.value = id;

	document.getElementById('ed_patient_pac_id').value = getPACIndexedValue("patient_pac_id", id);
	var pac_status = getPACIndexedValue("pac_status", id);
	var els = document.getElementsByName("ed_anaesthesthetic_status");
	for (var k=0; k<els.length; k++) {
		if (els[k].value == pac_status) {
			els[k].checked = true;
			break;
		}
	}
	var conducted_datetime = getPACIndexedValue("conducted_date", id);
	document.getElementById('ed_conducted_date').value = empty(conducted_datetime) ? '' : conducted_datetime.split(' ')[0];
	document.getElementById('ed_conducted_time').value = empty(conducted_datetime) ? '' : conducted_datetime.split(' ')[1];
	var validity_datetime = getPACIndexedValue("validity_date", id);
	document.getElementById('ed_validity_date').value = empty(validity_datetime) ? '' : validity_datetime.split(' ')[0];
	document.getElementById('ed_validity_time').value = empty(validity_datetime) ? '' : validity_datetime.split(' ')[1];
	var pac_doctor_id = getPACIndexedValue('pac_doctor_id', id);
	document.getElementById('ed_pac_doctor_name').value = getPACIndexedValue('pac_doctor_name', id);
	document.getElementById('ed_pac_doctor_id').value = pac_doctor_id;
	ed_PACDocAutoComp = initPACDoctorAC('ed_pac_doctor_name', 'ed_pac_doctor_container', true, pac_doctor_id);
	if (document.getElementById('ed_pac_doctor_name').value != '') {
		ed_PACDocAutoComp._bItemSelected = true;
		ed_PACDocAutoComp._sInitInputValue = document.getElementById('ed_pac_doctor_name').value;
	}
	if (screenid == 'op_prescribe') {
		document.getElementById('ed_pac_doctor_name').disabled = true;
	}
	document.getElementById('ed_pac_remarks').value = getPACIndexedValue("pac_remarks", id);

	document.getElementById('ed_pac_remarks').focus();
	return false;
}

function editPACTableRow() {
	var id = document.forms[pre_anaesthesthetic_form].editPreAnaesRowId.value;
	var row = getPACChargeRow(id, 'preAnaesthestheticTable');

	var remarks = document.getElementById('ed_pac_remarks').value;
	if (remarks == '') {
   		showMessage('js.outpatient.consultation.mgmt.enterpreanaesthetic.evaluvationmarks');
   		document.getElementById('ed_pac_remarks').focus();
   		return false;
   	}
	if (!validatePACDateTime('ed_conducted_date', 'ed_conducted_time', 'Date', 'Time', null, false))
		return false;
	if (!validatePACDateTime('ed_validity_date', 'ed_validity_time', 'Date', 'Time', null, false))
		return false;
	var conducted_date = document.getElementById('ed_conducted_date').value;
	var validity_date = document.getElementById('ed_validity_date').value;
	if(getDateDiff(conducted_date, validity_date) < 0){
		alert(getString("js.outpatient.consultation.mgmt.validitydategreater.equal.conductiondate") + conducted_date +")");
		return false;
	}

	var pacId = document.getElementById('ed_patient_pac_id').value;
   	var pac_doctor_name = document.getElementById("ed_pac_doctor_name").value;
   	var pac_doctor_id = document.getElementById('ed_pac_doctor_id').value;
   	var status_radio_els = document.getElementsByName('ed_anaesthesthetic_status');
	var pac_status;
	for (var k=0; k<status_radio_els.length; k++) {
		if (status_radio_els[k].checked) {
			pac_status = status_radio_els[k].value;
			break;
		}
	}
   	var status = pac_status;
   	var conducted_datetime = conducted_date + " " + document.getElementById('ed_conducted_time').value;
   	var validity_datetime = validity_date + " " + document.getElementById('ed_validity_time').value;
   	var remarks = document.getElementById('ed_pac_remarks').value;

	setNodeText(row.cells[PAC_DOCTOR], pac_doctor_name, 20);
	setNodeText(row.cells[PAC_CONDUCTED_DATETIME], conducted_datetime);
	setNodeText(row.cells[PAC_VALIDITY_DATETIME], validity_datetime);
	setNodeText(row.cells[PAC_STATUS], status == 'F' ? 'Fit' : status == 'U' ? 'Unfit' : "");
	setNodeText(row.cells[PAC_REMARK], remarks, 80, remarks);

	setPACHiddenValue(id, "patient_pac_id", pacId);
   	setPACHiddenValue(id, "pac_doctor_id", pac_doctor_id);
   	setPACHiddenValue(id, "pac_doctor_name", pac_doctor_name);
   	setPACHiddenValue(id, "pac_status", status);
	setPACHiddenValue(id, "conducted_date", conducted_datetime);
	setPACHiddenValue(id, "validity_date", validity_datetime);
   	setPACHiddenValue(id, "pac_remarks", remarks);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setPACIndexedValue("preAnaes_edited", id, 'true');
	setPACRowStyle(id);

	editPreAnaesDialog.cancel();
	return true;
}

var fieldPACEdited = false;
function setPreAnaesEdited() {
	fieldPACEdited = true;
}

function openPACPrevious() {
	var id = document.forms[pre_anaesthesthetic_form].editPreAnaesRowId.value;
	id = parseInt(id);
	var row = getPACChargeRow(id, 'preAnaesthestheticTable');

	if (fieldPACEdited) {
		if (!editPACTableRow()) return false;
		fieldPACEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		var row = getThisRow(document.getElementsByName('_editPreAnaesAnchor')[parseInt(id)-1]);
		while(row && row.style.display == 'none') {
		// ignore if the row is hidden.
			id = id-1;
			row = getThisRow(document.getElementsByName('_editPreAnaesAnchor')[parseInt(id)]);
		}
		if (row)
			showEditPreAnaesDialog(getElementByName(row, '_editPreAnaesAnchor'));
	}
}

function openPACNext() {
	var id = document.forms[pre_anaesthesthetic_form].editPreAnaesRowId.value;
	id = parseInt(id);
	var row = getPACChargeRow(id, 'preAnaesthestheticTable');

	if (fieldPACEdited) {
		if (!editPACTableRow()) return false;
		fieldPACEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('preAnaesthestheticTable').rows.length-2) {
		var row = getThisRow(document.getElementsByName('_editPreAnaesAnchor')[parseInt(id)+1]);
		while(row && row.style.display == 'none') {
		// ignore if the row is hidden.
			id = id+1;
			row = getThisRow(document.getElementsByName('_editPreAnaesAnchor')[parseInt(id)]);
		}
		if(row)
			showEditPreAnaesDialog(getElementByName(row,'_editPreAnaesAnchor'));
	}
}

function validatePACDateTime(datefieldid, timefieldid, dateFieldTitle, timeFieldTitle, valid, validEmpty) {
	var dateField = document.getElementById(datefieldid);
	var timeField = document.getElementById(timefieldid);
	if (dateField.value == '' && timeField.value == '') {
		if (!validEmpty) {
			alert(getString("js.outpatient.consultation.mgmt.enterthe") + " " + dateFieldTitle + " & " + timeFieldTitle);
			return false;
		} else {
			return true;
		}
	}
	if (dateField.value != '' && timeField.value == '') {
		alert(getString("js.outpatient.consultation.mgmt.enterthe") + " " +timeFieldTitle + ".");
		return false;
	}
	if (timeField.value != '' && dateField.value == '') {
		alert(getString("js.outpatient.consultation.mgmt.enterthe") + " " + dateFieldTitle + ".");
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

function getPACNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstPACRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getPACTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getPACNumCharges(tableId) + 1;
}

function getPACChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstPACRow()];
}

function getPACRowChargeIndex(row) {
	return row.rowIndex - getFirstPACRow();
}

function getPACThisRow(node) {
	return findAncestor(node, "TR");
}

function getPACIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[pre_anaesthesthetic_form], name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setPACIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[pre_anaesthesthetic_form], name, index);
	if (obj)
		obj.value = value;
	return obj;
}

