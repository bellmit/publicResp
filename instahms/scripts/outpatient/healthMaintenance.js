var d_healthMaintsDocAutoComp = null;
var ed_healthMaintsDocAutoComp = null;

function initHealthMaint() {
	initHealthMaintDialog();
	initEditHealthMaintDialog();
	d_healthMaintsDocAutoComp = initHealthMaintsDoctorAC('d_healthMaint_doctor', 'd_healthMaint_doctor_container', false, '');
}

function initHealthMaintsDoctorAC(inputElId, containerId, edit, doctorId) {
	var doctorList = [];
	for (var i=0; i<healthMaints_doctors_json.length; i++) {
		if (healthMaints_doctors_json[i].status == 'A' || doctorId == healthMaints_doctors_json[i].doctor_id == '') {
			doctorList.push(healthMaints_doctors_json[i]);
		}
	}
	if (edit && !empty(ed_healthMaintsDocAutoComp)) {
		ed_healthMaintsDocAutoComp.destroy();
		ed_healthMaintsDocAutoComp = null;
	} else if (!empty(d_healthMaintsDocAutoComp)) {
		d_healthMaintsDocAutoComp.destroy();
		d_healthMaintsDocAutoComp = null;
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

	autoComp.itemSelectEvent.subscribe(setHealthMaintsDoctor);
	autoComp.selectionEnforceEvent.subscribe(clearHealthMaintsDoctor);

	return autoComp;
}

function setHealthMaintsDoctor(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_healthMaint_doctor_id").value = sArgs[2].doctor_id;
}

function clearHealthMaintsDoctor(oSelf, sClearedValue) {
	var elId = sClearedValue[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_healthMaint_doctor_id").value = '';
}

var addHealthMaintDialog = null;
function initHealthMaintDialog() {
	var dialogHealthMaintDiv = document.getElementById("addHealthMaintDialog");
	if (dialogHealthMaintDiv == undefined) return;

	dialogHealthMaintDiv.style.display = 'block';
	addHealthMaintDialog = new YAHOO.widget.Dialog("addHealthMaintDialog",
			{	width:"600px",
				context : ["addHealthMaintDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add_HealthMaint_bt', 'click', addToHealthMaintTable, addHealthMaintDialog, true);
	YAHOO.util.Event.addListener('Close_HealthMaint_bt', 'click', handleAddHealthMaintCancel, addHealthMaintDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddHealthMaintCancel,
	                                                scope:addHealthMaintDialog,
	                                                correctScope:true } );
	addHealthMaintDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addHealthMaintDialog.render();
}

function handleAddHealthMaintCancel() {
	if (childHealthMaintDialog == null) {
		parentHealthMaintDialog = null;
		this.cancel();
	}
}

var parentHealthMaintDialog = null;
var childHealthMaintDialog = null;
function showAddHealthMaintDialog(obj) {
	var row = getHealthMainThisRow(obj);
	clearHealthMainFields();
	addHealthMaintDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	if (visit_type == 'o') {
		var consultingDoctorId = document.getElementById('health_consulting_doctor_id').value;
		consultingDoctorId = empty(consultingDoctorId) ? health_user_doctor_id : consultingDoctorId;
		var record = findInList(healthMaints_doctors_json, "doctor_id", consultingDoctorId);

		var consultingDoctorName = empty(consultingDoctorId) ? '' : record.doctor_name;
		document.getElementById('d_healthMaint_doctor').disabled = true;
		document.getElementById('d_healthMaint_doctor').value = consultingDoctorName;
		document.getElementById('d_healthMaint_doctor_id').value = consultingDoctorId;
		d_healthMaintsDocAutoComp = initHealthMaintsDoctorAC('d_healthMaint_doctor', 'd_healthMaint_doctor_container', false, consultingDoctorId);
		if (document.getElementById('d_healthMaint_doctor').value != '') {
			d_healthMaintsDocAutoComp._bItemSelected = true;
			d_healthMaintsDocAutoComp._sInitInputValue = document.getElementById('d_healthMaint_doctor').value;
		}

	} else if (!empty(health_user_doctor_id)) {
		// populate the user doctor in ip visit summary/outside patient registration screens.
		var record = findInList(healthMaints_doctors_json, "doctor_id", health_user_doctor_id);
		var userDoctorName = record.doctor_name
		document.getElementById('d_healthMaint_doctor').value = userDoctorName;
		document.getElementById('d_healthMaint_doctor_id').value = health_user_doctor_id;
		d_healthMaintsDocAutoComp = initHealthMaintsDoctorAC('d_healthMaint_doctor', 'd_healthMaint_doctor_container', false, health_user_doctor_id);
		if (document.getElementById('d_healthMaint_doctor').value != '') {
			d_healthMaintsDocAutoComp._bItemSelected = true;
			d_healthMaintsDocAutoComp._sInitInputValue = document.getElementById('d_healthMaint_doctor').value;
		}
	}
	addHealthMaintDialog.show();
	document.getElementById('d_activity').focus();
	parentHealthMaintDialog = addHealthMaintDialog;
	return false;
}


var healMainColIndex  = 0;
var healthMaintAdded = 0;
var HEALTH_DATETIME = healMainColIndex++, HEALTH_ACTIVITY = healMainColIndex++, HEALTH_DOCTOR = healMainColIndex++, HEALTH_DUEBY =  healMainColIndex++,
	HEALTH_REMARK = healMainColIndex++, HEALTH_STATUS = healMainColIndex++, HEALTH_TRASH_COL = healMainColIndex++, HEALTH_EDIT_COL = healMainColIndex++;
function addToHealthMaintTable() {
	var activity = document.getElementById('d_activity').value;
	if (activity == '') {
   		showMessage('js.outpatient.consultation.mgmt.enteractivity');
   		document.getElementById('d_activity').focus();
   		return false;
   	}
   	var health_doctor = document.getElementById('d_healthMaint_doctor').value;
   	if (health_doctor == '') {
		showMessage('js.outpatient.consultation.mgmt.selectdoctor');
		document.getElementById('d_healthMaint_doctor').focus();
		return false;
	}
	if (!validateHealthMaintDateTime('d_recorded_date', 'd_recorded_time', 'Date', 'Time', null, false))
		return false;

	var id = getHealthMainNumCharges('healthMaintsTable');
   	var table = document.getElementById("healthMaintsTable");
	var templateRow = table.rows[getHealthMainTemplateRow('healthMaintsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	var healthMainId = document.getElementById('d_health_maint_id').value;
   	var healthMain_doctor_id = document.getElementById('d_healthMaint_doctor_id').value;
   	var status = document.getElementById('d_healthMain_status').options[document.getElementById('d_healthMain_status').selectedIndex].value;
   	var healthMain_datetime = document.getElementById('d_recorded_date').value + " " + document.getElementById('d_recorded_time').value;
   	var due_by = document.getElementById('d_due_by').value;
   	var remarks = document.getElementById('d_healthMain_remarks').value;

	setNodeText(row.cells[HEALTH_ACTIVITY], activity, 1000);
	setNodeText(row.cells[HEALTH_DOCTOR], health_doctor, 20);
	setNodeText(row.cells[HEALTH_STATUS], status == 'D' ? 'Due' : status == 'C' ? 'Completed' : status ==  'X' ? 'Cancelled' : status == 'O' ? 'Other' : "");
	setNodeText(row.cells[HEALTH_DATETIME], healthMain_datetime);
	setNodeText(row.cells[HEALTH_DUEBY], due_by, 50);
	setNodeText(row.cells[HEALTH_REMARK], remarks, 1000);

	setHealthMainHiddenValue(id, "health_maint_id", '_');
	setHealthMainHiddenValue(id, "activity", activity);
   	setHealthMainHiddenValue(id, "healthMain_doctor_id", healthMain_doctor_id);
   	setHealthMainHiddenValue(id, "healthMain_doctor_name", health_doctor);
   	setHealthMainHiddenValue(id, "healthMain_status", status);
	setHealthMainHiddenValue(id, "recorded_date", healthMain_datetime);
	setHealthMainHiddenValue(id, "due_by", due_by);
   	setHealthMainHiddenValue(id, "healthMain_remarks", remarks);

   	healthMaintAdded++;
	clearHealthMainFields();
	setHealthMainRowStyle(id);
	addHealthMaintDialog.align("tr", "tl");
	document.getElementById('d_activity').focus();
	return id;
}

function setHealthMainHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[health_maintenance_form], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearHealthMainFields() {
	document.getElementById('d_activity').value = '';
	if (visit_type != 'o') {
		document.getElementById('d_healthMaint_doctor').value = '';
		document.getElementById('d_healthMaint_doctor_id').value = '';
		d_healthMaintsDocAutoComp = initHealthMaintsDoctorAC('d_healthMaint_doctor', 'd_healthMaint_doctor_container', false, health_user_doctor_id);
		if (!empty(health_user_doctor_id)) {
			var record = findInList(healthMaints_doctors_json, "doctor_id", health_user_doctor_id);
			var userDoctorName = record.doctor_name
			document.getElementById('d_healthMaint_doctor').value = userDoctorName;
			document.getElementById('d_healthMaint_doctor_id').value = health_user_doctor_id;
			if (document.getElementById('d_healthMaint_doctor').value != '') {
				d_healthMaintsDocAutoComp._bItemSelected = true;
				d_healthMaintsDocAutoComp._sInitInputValue = document.getElementById('d_healthMaint_doctor').value;
			}
		}
	}

	document.getElementById('d_healthMain_status').value = '';
	document.getElementById('d_recorded_date').value = formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_recorded_time').value = formatTime(new Date(), false);
	document.getElementById('d_due_by').value = '';
	document.getElementById('d_healthMain_remarks').value = '';
}

function setHealthMainRowStyle(i) {
	var row = getHealthMainChargeRow(i, 'healthMaintsTable');
	var healthMaintId = getHealthMainIndexedValue("health_maint_id", i);

	var trashImgs = row.cells[HEALTH_TRASH_COL].getElementsByTagName("img");

	var added = (healthMaintId.substring(0,1) == "_");
	var cancelled = getHealthMainIndexedValue("delHealthMaint", i) == 'true';
	var edited = getHealthMainIndexedValue("healthMaint_edited", i) == 'true';

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

function cancelHealthMaint(obj) {

	var row = getHealthMainThisRow(obj);
	var id = getHealthMainRowChargeIndex(row);
	var oldDeleted =  getHealthMainIndexedValue("delHealthMaint", id);

	var isNew = getHealthMainIndexedValue("health_maint_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		healthMaintAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setHealthMainIndexedValue("delHealthMaint", id, newDeleted);
		setHealthMainIndexedValue("healthMaint_edited", id, "true");
		setHealthMainRowStyle(id);
	}
	return false;
}

var editHealthMaintDialog = null;
function initEditHealthMaintDialog() {
	var dialogHealthMainDiv = document.getElementById("editHealthMaintDialog");
	dialogHealthMainDiv.style.display = 'block';
	editHealthMaintDialog = new YAHOO.widget.Dialog("editHealthMaintDialog",{
			width:"600px",
			text: "Edit Health Maintenance",
			context :["healthMaintsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditHealthMainCancel,
	                                                scope:editHealthMaintDialog,
	                                                correctScope:true } );
	editHealthMaintDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editHealthMaintDialog.cancelEvent.subscribe(handleEditHealthMainCancel);
	YAHOO.util.Event.addListener('edit_HealthMaint_Ok', 'click', editHealthMainTableRow, editHealthMaintDialog, true);
	YAHOO.util.Event.addListener('edit_HealthMaint_Cancel', 'click', handleEditHealthMainCancel, editHealthMaintDialog, true);
	YAHOO.util.Event.addListener('edit_HealthMaint_Previous', 'click', openHealthMainPrevious, editHealthMaintDialog, true);
	YAHOO.util.Event.addListener('edit_HealthMaint_Next', 'click', openHealthMainNext, editHealthMaintDialog, true);
	editHealthMaintDialog.render();
}

function handleEditHealthMainCancel() {
	if (childHealthMaintDialog == null) {
		parentHealthMaintDialog = null;
		var id = document.forms[health_maintenance_form].editHealthMaintRowId.value;
		var row = getHealthMainChargeRow(id, "healthMaintsTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldHealthMainEdited = false;
		this.hide();
	}
}

function showEditHealthMaintDialog(obj) {
	parentHealthMaintDialog = editHealthMaintDialog;
	var row = getHealthMainThisRow(obj);
	var id = getHealthMainRowChargeIndex(row);
	editHealthMaintDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editHealthMaintDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.forms[health_maintenance_form].editHealthMaintRowId.value = id;

	document.getElementById('ed_health_maint_id').value = getHealthMainIndexedValue("health_maint_id", id);
	document.getElementById('ed_activity').value = getHealthMainIndexedValue("activity", id);
	var health_doctor_id = getHealthMainIndexedValue('healthMain_doctor_id', id);
	document.getElementById('ed_healthMaint_doctor').value = getHealthMainIndexedValue('healthMain_doctor_name', id);
	document.getElementById('ed_healthMaint_doctor_id').value = health_doctor_id;
	ed_healthMaintsDocAutoComp = initHealthMaintsDoctorAC('ed_healthMaint_doctor', 'ed_healthMaint_doctor_container', true, health_doctor_id);
	if (document.getElementById('ed_healthMaint_doctor').value != '') {
		ed_healthMaintsDocAutoComp._bItemSelected = true;
		ed_healthMaintsDocAutoComp._sInitInputValue = document.getElementById('ed_healthMaint_doctor').value;
	}
	if (visit_type == 'o') {
		document.getElementById('ed_healthMaint_doctor').disabled = true;
	}
	if (getHealthMainIndexedValue("healthMain_status", id) == " ") {
		setSelectedIndex(document.getElementById('ed_healthMain_status'),0);
	} else
		document.getElementById('ed_healthMain_status').value = getHealthMainIndexedValue("healthMain_status", id);
	var recorded_datetime = getHealthMainIndexedValue("recorded_date", id);
	document.getElementById('ed_recorded_date').value = empty(recorded_datetime) ? '' : recorded_datetime.split(' ')[0];
	document.getElementById('ed_recorded_time').value = empty(recorded_datetime) ? '' : recorded_datetime.split(' ')[1];
	document.getElementById('ed_due_by').value = getHealthMainIndexedValue("due_by", id);
	document.getElementById('ed_healthMain_remarks').value = getHealthMainIndexedValue("healthMain_remarks", id);

	document.getElementById('ed_activity').focus();
	return false;
}

function editHealthMainTableRow() {
	var id = document.forms[health_maintenance_form].editHealthMaintRowId.value;
	var row = getHealthMainChargeRow(id, 'healthMaintsTable');

	var activity = document.getElementById('ed_activity').value;
	if (activity == '') {
   		showMessage('js.outpatient.consultation.mgmt.enteractivity');
   		document.getElementById('ed_activity').focus();
   		return false;
   	}
   	var health_doctor = document.getElementById('ed_healthMaint_doctor').value;
   	if (health_doctor == '') {
		showMessage('js.outpatient.consultation.mgmt.selectdoctor');
		document.getElementById('ed_healthMaint_doctor').focus();
		return false;
	}
	if (!validateHealthMaintDateTime('ed_recorded_date', 'ed_recorded_time', 'Date', 'Time', null, false))
		return false;

	var healthMainId = document.getElementById('ed_health_maint_id').value;
   	var healthMain_doctor_id = document.getElementById('ed_healthMaint_doctor_id').value;
   	var status = document.getElementById('ed_healthMain_status').options[document.getElementById('ed_healthMain_status').selectedIndex].value;
   	var healthMain_datetime = document.getElementById('ed_recorded_date').value + " " + document.getElementById('ed_recorded_time').value;
   	var due_by = document.getElementById('ed_due_by').value;
   	var remarks = document.getElementById('ed_healthMain_remarks').value;

	setNodeText(row.cells[HEALTH_ACTIVITY], activity, 1000);
	setNodeText(row.cells[HEALTH_DOCTOR], health_doctor, 20);
	setNodeText(row.cells[HEALTH_STATUS], status == 'D' ? 'Due' : status == 'C' ? 'Completed' : status ==  'X' ? 'Cancelled' : status == 'O' ? 'Other' : "");
	setNodeText(row.cells[HEALTH_DATETIME], healthMain_datetime);
	setNodeText(row.cells[HEALTH_DUEBY], due_by, 50);
	setNodeText(row.cells[HEALTH_REMARK], remarks, 1000);

	setHealthMainHiddenValue(id, "health_maint_id", healthMainId);
	setHealthMainHiddenValue(id, "activity", activity);
   	setHealthMainHiddenValue(id, "healthMain_doctor_id", healthMain_doctor_id);
   	setHealthMainHiddenValue(id, "healthMain_doctor_name", health_doctor);
   	setHealthMainHiddenValue(id, "healthMain_status", status);
	setHealthMainHiddenValue(id, "recorded_date", healthMain_datetime);
	setHealthMainHiddenValue(id, "due_by", due_by);
   	setHealthMainHiddenValue(id, "healthMain_remarks", remarks);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setHealthMainIndexedValue("healthMaint_edited", id, 'true');
	setHealthMainRowStyle(id);

	editHealthMaintDialog.cancel();
	return true;
}

var fieldHealthMainEdited = false;
function setHealthMaintEdited() {
	fieldHealthMainEdited = true;
}

function openHealthMainPrevious() {
	var id = document.forms[health_maintenance_form].editHealthMaintRowId.value;
	id = parseInt(id);
	var row = getHealthMainChargeRow(id, 'healthMaintsTable');

	if (fieldHealthMainEdited) {
		if (!editHealthMainTableRow()) return false;
		fieldHealthMainEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditHealthMaintDialog(document.getElementsByName('_editHealthMaintAnchor')[parseInt(id)-1]);
	}
}

function openHealthMainNext() {
	var id = document.forms[health_maintenance_form].editHealthMaintRowId.value;
	id = parseInt(id);
	var row = getHealthMainChargeRow(id, 'healthMaintsTable');

	if (fieldHealthMainEdited) {
		if (!editHealthMainTableRow()) return false;
		fieldHealthMainEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('healthMaintsTable').rows.length-2) {
		showEditHealthMaintDialog(document.getElementsByName('_editHealthMaintAnchor')[parseInt(id)+1]);
	}
}

function hmDetailsEntered() {
	return document.getElementById('healthMaintsTable').rows.length > 2;
}

function checkHealthMaintenance() {
	if(!hmDetailsEntered()) {
		showMessage("js.outpatient.consultation.mgmt.enteratleastonehealthmaintenancevalue");
		return false;
	} else {
		var els = document.getElementsByName('recorded_date');
		var hm_delete = document.getElementsByName('delHealthMaint');
		var hmCount = 0;
	
		for (var i=0; i<els.length; i++) {
			var rec_date = els[i].value;
			if (rec_date !='' && hm_delete[i].value == 'false') {
				hmCount++;
			}
		}
		
		if (hmCount == 0) {
			showMessage("js.outpatient.consultation.mgmt.enteratleastonehealthmaintenancevalue");
			return false;
		} 
	 }
	return true;
}

function validateHealthMaintDateTime(datefieldid, timefieldid, dateFieldTitle, timeFieldTitle, valid, validEmpty) {
	var dateField = document.getElementById(datefieldid);
	var timeField = document.getElementById(timefieldid);
	if (dateField.value == '' && timeField.value == '') {
		if (!validEmpty) {
			alert(getString("js.outpatient.consultation.mgmt.enterthe") + dateFieldTitle+" & " + timeFieldTitle);
			return false;
		} else {
			return true;
		}
	}
	if (dateField.value != '' && timeField.value == '') {
		alert(getString("js.outpatient.consultation.mgmt.enterthe") + timeFieldTitle + ".");
		return false;
	}
	if (timeField.value != '' && dateField.value == '') {
		alert(getString("js.outpatient.consultation.mgmt.enterthe") + dateFieldTitle + ".");
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
		showMessage(errorStr);
		return false;
	}
	return true;
}

function getHealthMainNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstHealthMainRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getHealthMainTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getHealthMainNumCharges(tableId) + 1;
}

function getHealthMainChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstHealthMainRow()];
}

function getHealthMainRowChargeIndex(row) {
	return row.rowIndex - getFirstHealthMainRow();
}

function getHealthMainThisRow(node) {
	return findAncestor(node, "TR");
}

function getHealthMainIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[health_maintenance_form], name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setHealthMainIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[health_maintenance_form], name, index);
	if (obj)
		obj.value = value;
	return obj;
}

