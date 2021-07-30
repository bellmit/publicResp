var Dom = YAHOO.util.Dom;
function filterActivitiesOnType(filterVal) {
	var table = document.getElementById('activitiesTable');
	var compChecked = document.getElementById('hideCompletedItems');
	var canlChecked = document.getElementById('hideCancelledItems');
	for (var i=1; i<table.rows.length-1; i++) {
		if ((filterVal == '' || Dom.hasClass(table.rows[i], filterVal)) &&
			(!compChecked.checked || !Dom.hasClass(table.rows[i], 'completed')) &&
			(!canlChecked.checked || !Dom.hasClass(table.rows[i], 'cancelled'))
			) {
			table.rows[i].style.display = 'table-row';
		} else {
			table.rows[i].style.display = 'none';
		}
	}
}

function hideCompleted(obj) {
	if (!obj) return;
	var filterVal = document.getElementById('filterOnType').value;
	var els = Dom.getElementsByClassName("completed", "tr", "activitiesTable");
	for (var i=0; i<els.length; i++) {
		if (obj.checked) {
			els[i].style.display = 'none';
		} else {
			if (filterVal == '') {
				els[i].style.display = 'table-row';
			} else if (Dom.hasClass(getThisRow(els[i]), filterVal)) {
				els[i].style.display = 'table-row';
			} else {
				els[i].style.display = 'none';
			}
		}
	}
}

function hideCancelled(obj) {
	if (!obj) return;

	var filterVal = document.getElementById('filterOnType').value;
	var els = Dom.getElementsByClassName("cancelled", "tr", "activitiesTable");
	for (var i=0; i<els.length; i++) {
		if (obj.checked) {
			els[i].style.display = 'none';
		} else {
			if (filterVal == '') {
				els[i].style.display = 'table-row';
			} else if (Dom.hasClass(getThisRow(els[i]), filterVal)) {
				els[i].style.display = 'table-row';
			} else {
				els[i].style.display = 'none';
			}
		}
	}
}

function printActivities() {
	var patientId = document.getElementById('patient_id').value;
	var printerId = document.getElementById('printerId').value;
	if (printerId == '') {
		alert("Please select the printer settings");
		return false;
	}
	if (patientId == '') {
		alert("Please select the patient Id");
		return false;
	}
	window.open(cpath+"/pages/wardactivities/PatientActivitiesAction.do?_method=printActivities&patient_id="+patientId+"&printerId="+printerId);
}

function enableInterval(value) {
	if (value == '0') {
		document.getElementById('repeat_interval').disabled = false;
	} else {
		document.getElementById('repeat_interval').value = ''
		document.getElementById('repeat_interval').disabled = true;
	}
}

function submitHandler() {
	document.getElementById('authUser').value = document.getElementById('login_user').value;
	document.forms.activities.submit();
	return false;
}

function captureSubmitEvent() {
	var form = document.activities;
	form.validateFormSubmit = form.submit;

	form.submit = function validatedSubmit() {
		if (!blockSubmit()) {
			var e = xGetElementById(document.activities);
			YAHOO.util.Event.stopEvent(e);
			return false;
		}
		form.validateFormSubmit();
		return true;
	};
}
function blockSubmit() {
	if (document.getElementById('patient_discharged').value == 'true') {
		alert("Patient is inactive or discharged, add/edit of activities is not allowed.");
		return false;
	}
	return true;
}

function init() {
	captureSubmitEvent();
	initDialog();
	initEditDialog();
	initLoginDialog();
	hideCompleted(document.getElementById('hideCompletedItems'));
	hideCancelled(document.getElementById('hideCancelledItems'));
}

function showLoginDialog() {
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		if(validateCreditLimitRule()) {
			document.forms.activities.submit();
		}	
	}
	return true;
}

function initDialog() {
	var dialogDiv = document.getElementById("addActivityDialog");
	dialogDiv.style.display = 'block';
	addDialog = new YAHOO.widget.Dialog("addActivityDialog",
			{	width:"800px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_Add', 'click', addToTable, addDialog, true);
	YAHOO.util.Event.addListener('d_Close', 'click', handleAddActivityCancel, addDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddActivityCancel,
	                                                scope:addDialog,
	                                                correctScope:true } );
	addDialog.cfg.queueProperty("keylisteners", escKeyListener);
	addDialog.render();
}

function showAddDialog(obj) {
	var row = getThisRow(obj);

	addDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addDialog.show();
	document.getElementById('d_gen_activity_details').focus();
	return false;
}

function validateDueDateTime(datefieldid, timefieldid, dateFieldTitle, timeFieldTitle, valid, validEmpty) {
	var dateField = document.getElementById(datefieldid);
	var timeField = document.getElementById(timefieldid);
	if (dateField.value == '' && timeField.value == '') {
		if (!validEmpty) {
			alert("Please enter the " + dateFieldTitle+" & " + timeFieldTitle);
			return false;
		} else {
			return true;
		}
	}
	if (dateField.value != '' && timeField.value == '') {
		alert("Please enter the " + timeFieldTitle + ".");
		return false;
	}
	if (timeField.value != '' && dateField.value == '') {
		alert("Please enter the " + dateFieldTitle + ".");
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

var colIndex = 0;
var DUE_DATE = colIndex++, ITEM_TYPE = colIndex++, ITEM = colIndex++, PRESC_DOC = colIndex++, ACTIVITY_NUM = colIndex++,
	ORDER = colIndex++, ITEM_REMARKS = colIndex++, COMPL_DATETIME = colIndex++, EDIT_COL = colIndex++;
function addToTable() {
	var genActivityDetails = document.getElementById('d_gen_activity_details').value;
	var itemRemarks = document.getElementById('d_activity_remarks').value;
	var d_due_date = document.getElementById('d_due_date').value;
	var d_due_time = document.getElementById('d_due_time').value;
	var d_completed_date = document.getElementById('d_completed_date').value;
   	var d_completed_time = document.getElementById('d_completed_time').value;
   	var activity_status = document.getElementById('d_activity_status').value;
	var completed_by = document.getElementById('d_completed_by').value;
	document.getElementsByName('completed_by').value = document.getElementById('d_completed_by').value;
   	if (activity_status == 'D' && (d_completed_date == '' || d_completed_time == '')) {
   		alert("Please enter the completed date & time");
   		return false;
   	}

	if (!validateDueDateTime('d_due_date', 'd_due_time', 'Due Date', 'Due Time', null, false))
		return false;
	if (!validateDueDateTime('d_completed_date', 'd_completed_time', 'Completed Date', 'Completed Time', null, true))
	 	return false;

	if (genActivityDetails == '') {
		alert('Please enter the General Activity');
		document.getElementById('d_gen_activity_details').focus();
		return false;
	}

	if (activity_status == '') {
		alert('Please select the Activity Status');
		document.getElementById('d_activity_status').focus();
		return false;
	}

	var id = getNumCharges('activitiesTable');
   	var table = document.getElementById("activitiesTable");
	var templateRow = table.rows[getTemplateRow('activitiesTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "s_itemRow" + id;

   	var cell = null;


	setNodeText(row.cells[DUE_DATE], d_due_date + " " + d_due_time);
	setNodeText(row.cells[ITEM_TYPE], 'General Activity', 15);
   	setNodeText(row.cells[ITEM], genActivityDetails, 30);
   	setNodeText(row.cells[PRESC_DOC], '', 20);
   	setNodeText(row.cells[ACTIVITY_NUM], 1);
   	setNodeText(row.cells[ITEM_REMARKS], itemRemarks, 30);

	setHiddenValue(id, "activity_id", "_");
	setHiddenValue(id, "item_name", genActivityDetails);
	setHiddenValue(id, "activity_remarks", itemRemarks);
	setHiddenValue(id, "activity_type", 'G');
	var dueDateTime = d_due_date != '' ? (d_due_date + ' ' + d_due_time) : '';
	setHiddenValue(id, "due_date", dueDateTime);

	var completedDateTime = d_completed_date != '' ? (d_completed_date + ' ' + d_completed_time) : '';
	setNodeText(row.cells[COMPL_DATETIME], completedDateTime);
	setHiddenValue(id, "completed_date", completedDateTime);
	setHiddenValue(id, "activity_status", activity_status);
	setHiddenValue(id, "completed_by", completed_by);

	row.className = "added";
	Dom.addClass(row, 'G');
	clearFields();

	this.align("tr", "tl");
	document.getElementById('d_activity_remarks').focus();
	return id;

}

function toggleActivityCompleted(obj) {
	document.getElementsByName("activity_completed")[obj.value].value = obj.checked;
}

function clearFields() {
	document.getElementById('d_activity_remarks').value = '';
	document.getElementById('d_due_date').value = formatDate(new Date());
	document.getElementById('d_due_time').value = formatTime(new Date());
	document.getElementById('d_activity_status').checked = '';
	document.getElementById('d_completed_date').value = '';
	document.getElementById('d_completed_time').value = ''
	document.getElementById('d_gen_activity_details').value = '';
	document.getElementById('d_order').checked = false;
}

function handleAddActivityCancel() {
	this.cancel();
}

function initEditDialog(obj) {
	var editDialogDiv = document.getElementById("editActivityDialog");
	editDialogDiv.style.display = 'block';
	editDialog = new YAHOO.widget.Dialog("editActivityDialog",
			{	width:"800px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditActivityCancel,
	                                                scope:editDialog,
	                                                correctScope:true } );
	editDialog.cfg.queueProperty("keylisteners", escKeyListener);
	YAHOO.util.Event.addListener('ed_okBtn', 'click', editTableRow, editDialog, true);
	YAHOO.util.Event.addListener('ed_cancelBtn', 'click', handleEditActivityCancel, editDialog, true);
	YAHOO.util.Event.addListener('ed_previousBtn', 'click', openPrevious, editDialog, true);
	YAHOO.util.Event.addListener('ed_nextBtn', 'click', openNext, editDialog, true);
	editDialog.render();
}

function handleEditActivityCancel() {
	var id = document.activities.editRowId.value;
	var row = getChargeRow(id, 'activitiesTable');
	YAHOO.util.Dom.removeClass(row, 'editing');
	this.cancel();
}

function showEditDialog(obj) {
	parentDialog = editDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.activities.editRowId.value = id;
	var itemType = getIndexedValue("prescription_type", id);

	var type = '';
	if (itemType == 'M') type = 'Medicine';
	if (itemType == 'I') type = 'Inv.';
	if (itemType == 'S') type = 'Service';
	if (itemType == 'C') type = 'Consultation';
	if (itemType == 'O') type = 'Others';

	var activityCompleted = (getIndexedValue("activity_status", id) == 'D' || getIndexedValue("activity_status", id) == 'X');
	document.getElementById('itemDisplayLabel').textContent = itemType == 'C' ? 'Doctor: ' : 'Item: ' ;
	document.getElementById('itemTypeLabel').textContent = type;
	document.getElementById('prescDoctorLabel').textContent = getIndexedValue("doctor_name", id);
	document.getElementById('dosageLabel').textContent = getIndexedValue("med_dosage", id);
	document.getElementById('adminStrengthLabel').textContent = getIndexedValue("admin_strength", id);
	document.getElementById('frequencyLabel').textContent = getIndexedValue("recurrence_name", id);

	var interval = getIndexedValue("repeat_interval", id);
	var intervalUnits = null;
	if (getIndexedValue('repeat_interval_units', id) == 'M')
		intervalUnits = 'Minutes';
	else if (getIndexedValue('repeat_interval_units', id) == 'H')
		intervalUnits = 'Hours';
	else if (getIndexedValue('repeat_interval_units', id) == 'D')
		intervalUnits = 'Days';
	interval = empty(interval) ? '' : (interval + ' ' +intervalUnits)

	document.getElementById('intervalLabel').textContent = interval;

	document.getElementById('endDateLabel').textContent = getIndexedValue("end_datetime", id);
	document.getElementById('noofOccurrencesLabel').textContent = getIndexedValue("no_of_occurrences", id);
	document.getElementById('tillDiscontinuedLabel').textContent = getIndexedValue('end_on_discontinue', id);
	document.getElementById('prescRemarksLabel').textContent = getIndexedValue('presc_remarks', id);

	document.getElementById('ed_due_date').disabled = activityCompleted;
	document.getElementById('ed_due_time').disabled = activityCompleted;
	document.getElementById('ed_completed_date').disabled = activityCompleted;
	document.getElementById('ed_completed_time').disabled = activityCompleted;
	document.getElementById('ed_activity_remarks').disabled = activityCompleted;
	document.getElementById('ed_activity_status').disabled = activityCompleted;
	document.getElementById('ed_med_batch').disabled = activityCompleted;
	document.getElementById('ed_med_expiry_month').disabled = activityCompleted;
	document.getElementById('ed_med_expiry_year').disabled = activityCompleted;

	var enableMedSection = (itemType == 'M' && getIndexedValue("activity_status", id) != 'X');

	document.getElementById('ed_med_batch').disabled = !enableMedSection;
	document.getElementById('ed_med_expiry_month').disabled = !enableMedSection;
	document.getElementById('ed_med_expiry_year').disabled = !enableMedSection;

	document.getElementById('ed_activity_type').value = getIndexedValue('activity_type', id);
	document.getElementById('ed_prescription_type').value = getIndexedValue('prescription_type', id);

	if (getIndexedValue('activity_type', id) == 'G') {
		document.getElementById('genActivityDetailsLabel').textContent =  getIndexedValue('item_name', id);
		document.getElementById('genActivityRow').style.display = 'table-row';
		document.getElementById('itemNameLabel').textContent = '';
	} else {
		document.getElementById('itemNameLabel').textContent = getIndexedValue('item_name', id);
		document.getElementById('genActivityDetailsLabel').textContent = '';
		document.getElementById('genActivityRow').style.display = 'none';
	}

	if (itemType == 'I' || itemType == 'S') {
		document.getElementById('ed_order').disabled = !empty(getIndexedValue("order_no", id));
	} else {
		document.getElementById('ed_order').disabled = true;
	}
	if (activityCompleted)
		document.getElementById('ed_order').disabled = true;
	document.getElementById('ed_order').checked = getIndexedValue('raise_order', id) == 'true' || !empty(getIndexedValue("order_no", id));

	document.getElementById('edOrderedDateLabel').textContent = getIndexedValue('ordered_datetime', id);
	document.getElementById('edOrderedbyLabel').textContent = getIndexedValue('ordered_by', id);

	var expiryDate = getIndexedValue('med_expiry_date', id);
	var expiryMonth = '';
	var expiryYear = '';
	if (expiryDate != '') {
		expiryMonth = expiryDate.split("-")[1];
		expiryYear = expiryDate.split("-")[2].substring(2,4);
	}
	document.getElementById('ed_med_expiry_month').value = expiryMonth;
	document.getElementById('ed_med_expiry_year').value = expiryYear;
	document.getElementById('ed_med_batch').value = getIndexedValue('med_batch', id);
	document.getElementById('ed_activity_remarks').value = getIndexedValue('activity_remarks', id);
	document.getElementById('ed_activity_status').value = getIndexedValue('activity_status', id);
	var ActivityStatus =  document.getElementById('ed_activity_status').value;
	if(ActivityStatus !=''){
		if(ActivityStatus !='D'){
			document.getElementById('ed_done_by_label').textContent = '';
			document.getElementById('ed_completed_by').value = '';
		}
		else{
			document.getElementById('ed_done_by_label').textContent = getIndexedValue('completed_by', id);
			document.getElementById('ed_completed_by').value = getIndexedValue('completed_by', id);
		}
	}
	if (getIndexedValue('due_date', id) != '') {
		document.getElementById("ed_due_date").value = getIndexedValue('due_date', id).split(' ')[0];
		document.getElementById("ed_due_time").value = getIndexedValue('due_date', id).split(' ')[1];
	} else {
		document.getElementById("ed_due_date").value = '';
		document.getElementById("ed_due_time").value = '';
	}
	if (getIndexedValue("completed_date", id) != '') {
		document.getElementById("ed_completed_date").value = getIndexedValue('completed_date', id).split(' ')[0];
		document.getElementById("ed_completed_time").value = getIndexedValue('completed_date', id).split(' ')[1];
	} else {
		document.getElementById("ed_completed_date").value = '';
		document.getElementById("ed_completed_time").value = '';
	}
	document.getElementById('ed_activity_remarks').focus();
	return false;
}

function getLastDayForMonth(month, year) {
	var dt = new Date(parseInt(year),parseInt(month),0);
	return [(dt.getDate() < 10 ? ('0'+ dt.getDate()) : dt.getDate()),(dt.getMonth() < 9 ? ('0'+ (dt.getMonth()+1)) : (dt.getMonth()+1)),dt.getFullYear()].join('-');
}

function editTableRow() {
	var id = document.activities.editRowId.value;
	var row = getChargeRow(id, 'activitiesTable');

	var itemType = document.getElementById('ed_prescription_type').value;
	var activityType = document.getElementById('ed_activity_type').value;
   	var remarks = document.getElementById('ed_activity_remarks').value;
   	var ed_due_date = document.getElementById('ed_due_date').value;
	var ed_due_time = document.getElementById('ed_due_time').value;
	var ed_completed_date = document.getElementById('ed_completed_date').value;
   	var ed_completed_time = document.getElementById('ed_completed_time').value;
   	var activity_status = document.getElementById('ed_activity_status').value;
	var completed_by = document.getElementById('ed_completed_by').value;
	var raise_order = document.getElementById('ed_order').checked;
	var mandate_test_additional_info = getIndexedValue('mandate_test_additional_info', id);

	if (activity_status == '') {
		alert('Please select the Activity Status');
		document.getElementById('ed_activity_status').focus();
		return false;
	}
	if (activity_status == 'D' && (ed_completed_date == '' || ed_completed_time == '')) {
   		alert("Please enter the completed date & time");
   		return false;
   	}
   	if (activity_status == 'X' && getIndexedValue('order_no', id) != '') {
   		alert("Activity is Ordered. Hence you can not mark the activity as Cancelled.");
   		return false;
   	}
   	if (activity_status == 'X' && !document.getElementById('ed_order').disabled && raise_order) {
   		alert("Activity marked for Cancellation, hence you cannot order this activity.");
   		return false;
   	}
   	if (!document.getElementById('ed_order').disabled && raise_order && mandate_test_additional_info == 'O') {
   		alert('This test is marked to capture additional information, to attach a document or capture notes please do so from the Order screen');
   	}

   if (!validateDueDateTime('ed_due_date', 'ed_due_time', 'Due Date', 'Due Time', null, false))
		return false;
	if (!validateDueDateTime('ed_completed_date', 'ed_completed_time', 'Completed Date', 'Completed Time', null, true))
	 	return false;
	if (itemType == 'M') {
		var med_batch = document.getElementById('ed_med_batch').value;
		var med_expiry_month = document.getElementById('ed_med_expiry_month').value;
		var med_expiry_year = document.getElementById('ed_med_expiry_year').value;
		if (med_expiry_month == '' && med_expiry_year != '') {
			alert("Please enter the Med Expiry Month");
			document.getElementById('ed_med_expiry_month').focus();
			return false;
		}
		if (med_expiry_year == '' && med_expiry_month != '') {
			alert("Please enter the Med Expiry Year");
			document.getElementById('ed_med_expiry_year').focus();
			return false;
		}
		var med_expiry_date = med_expiry_month + "-" + med_expiry_year;
		if (activity_status == 'D') {
			if (med_batch == '') {
				alert('Please enter the medicine batch');
				document.getElementById('ed_med_batch').focus();
				return false;
			}
			if (med_expiry_date == '-') {
				alert('Please enter Medicine Expiry Date');
				document.getElementById('ed_med_expiry_month').focus();
				return false;
			}
		}
		var regExpMonth = /\d{1,2}/;
		if (!regExpMonth.test(med_expiry_month) || parseInt(med_expiry_month) == 0 || parseInt(med_expiry_month) > 12) {
			alert("Please Enter the valid Expiry Month");
			document.getElementById('ed_med_expiry_month').focus();
			return false;
		}
		var regExpYear = /\d{2}/
		if (!regExpYear.test(med_expiry_year) || parseInt(med_expiry_month) > 99) {
			alert("Please Enter the valid Expiry Year");
			document.getElementById('ed_med_expiry_month').focus();
			return false;
		}
		setHiddenValue(id, "med_batch", med_batch);
		setHiddenValue(id, "med_expiry_date", med_expiry_date == '-' ? '' : getLastDayForMonth(med_expiry_month, "20" + med_expiry_year));
	}
	setNodeText(row.cells[ITEM_REMARKS], remarks, 30);

	setHiddenValue(id, "prescription_type", itemType);
	setHiddenValue(id, "activity_type", activityType);
	setHiddenValue(id, "activity_remarks", remarks);

	var dueDateTime = ed_due_date != '' ? (ed_due_date + ' ' + ed_due_time) : '';
	setHiddenValue(id, "due_date", dueDateTime);
	setNodeText(row.cells[DUE_DATE], dueDateTime);

	var completedDateTime = ed_completed_date != '' ? (ed_completed_date + ' ' + ed_completed_time) : '';
	setNodeText(row.cells[COMPL_DATETIME], completedDateTime);
	setHiddenValue(id, "completed_date", completedDateTime);
	setHiddenValue(id, "activity_status", activity_status);
	setHiddenValue(id, "completed_by", completed_by);
	setHiddenValue(id, "raise_order", raise_order);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);

	editDialog.cancel();
	return true;
}

function openPrevious() {
	var id = document.activities.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'activitiesTable');

	if (fieldEdited) {
		fieldEdited = false;
		if (!editTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		var row = getThisRow(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
		while(row && row.style.display == 'none') {
			// ignore if the row is hidden.
			id = id-1;
			row = getThisRow(document.getElementsByName('_editAnchor')[parseInt(id)]);
		}
		if (row)
			showEditDialog(getElementByName(row, '_editAnchor'));
	}

}

function openNext() {
	var id = document.activities.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'activitiesTable');

	if (fieldEdited) {
		fieldEdited = false;
		if (!editTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id+1 != document.getElementById('activitiesTable').rows.length-2) {
		var row = getThisRow(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
		while(row && row.style.display == 'none') {
			// ignore if the row is hidden.
			id = id+1;
			row = getThisRow(document.getElementsByName('_editAnchor')[parseInt(id)]);
		}
		if (row)
			showEditDialog(getElementByName(row, "_editAnchor"));
	}
}

function setCompletedDateTime(prefix) {
	var completed = document.getElementById(prefix+'_activity_status').value;
	if (completed == 'D') {
		if (document.getElementById(prefix+'_completed_date').value == '') {
			document.getElementById(prefix+'_completed_date').value = formatDate(new Date());
		}
		if (document.getElementById(prefix+'_completed_time').value == '') {
			document.getElementById(prefix+'_completed_time').value = formatTime(new Date());
		}
		if (document.getElementById(prefix+'_completed_by').value == '') {
			document.getElementById(prefix+'_done_by_label').textContent = isSharedLogIn == 'Y' ?'': loggedUserName;
			document.getElementById(prefix+'_completed_by').value = isSharedLogIn == 'Y' ?'': loggedUserName;
		}
	}else{
		document.getElementById(prefix+'_completed_date').value = '';
		document.getElementById(prefix+'_completed_time').value = '';
		document.getElementById(prefix+'_done_by_label').textContent = '';
		document.getElementById(prefix+'_completed_by').value ='';
	}
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.activities, name, index);
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

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.activities, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.activities, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {href=""
	var obj = getIndexedFormElement(document.activities, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'activitiesTable');
	var activityId = getIndexedValue("activity_id", i);

	var added = (activityId.substring(0,1) == "_");
	var edited = getIndexedValue("edited", i) == 'true';

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

	row.className = cls;

}

var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function validateCreditLimitRule() {
	var newOrEditItemsExist = isNewOrOrderItemsExist();
	//Credit limit rule is applicable for IP visits only
	if(visitType != 'i' || !newOrEditItemsExist || creditLimitDetailsJSON == undefined || creditLimitDetailsJSON == null) {
		return true;
	}
	
	var visitPatientDuePaise = getPaise(visitTotalPatientDue);
	var availableCreditLimit = parseFloat(creditLimitDetailsJSON.availableCreditLimit);
	if(ip_credit_limit_rule == 'B') {
		if(!(availableCreditLimit > 0)) {
			var msg=getString("js.billing.billlist.and.below.currentoutstanding");
			msg+=' '+ formatAmountPaise(visitPatientDuePaise);
			msg+="\n";
			msg+=getString("js.billing.billlist.ipcreditlimitis");
			msg+=' '+ formatAmountValue(availableCreditLimit);
			alert(msg);
			return false;
		}
	} else if (ip_credit_limit_rule == 'W') {
		if(!(availableCreditLimit > 0)) {
			var msg=getString("js.billing.billlist.and.below.currentoutstanding");
			msg+=' '+ formatAmountPaise(visitPatientDuePaise);
			msg+="\n";
			msg+=getString("js.billing.billlist.ipcreditlimitis");
			msg+=' '+ formatAmountValue(availableCreditLimit) ;
			msg+="\n";
			msg+=getString("js.billing.billlist.doyouwanttoproceed");
			var ok = confirm(msg);
			if(!ok)
				return false;
		}
	}
	return true;
}

function isNewOrOrderItemsExist() {
	var activityTypeList = document.getElementsByName("activity_type");
	var raiseOrderList = document.getElementsByName("raise_order");
	var editedList = document.getElementsByName("edited");
	var isAlreadyOrderedList = document.getElementsByName("is_already_ordered");
	
	for (var i=0; i<activityTypeList.length; i++) {
		if(activityTypeList[i] != null && activityTypeList[i].value == "P" && 
				raiseOrderList[i] != null && raiseOrderList[i].value == "true" &&
				editedList[i] != null && editedList[i].value == "true" &&
				isAlreadyOrderedList[i] != null && !(isAlreadyOrderedList[i].value == "true")) {
			return true;
		}
	}
	return false;
	
}