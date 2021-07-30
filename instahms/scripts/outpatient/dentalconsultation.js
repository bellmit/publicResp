var dosageAutoComplete = null;
var itemAutoComp = null;

var totDiscPaise = 0;
var totAmtPaise = 0;
var totAmtDuePaise = 0;

function init() {
	initItemDialog();
	initEditItemDialog();
	initFrequencyAutoComplete();
	initToothNumberDialog();
	editDialogGeneric();
	initSubTaskDialog();
	// Display amounts based on action rights and rate plan.
	showRateDetails = displayAmounts();
	initTrtmtItemDialog();
	initEditTrtmtItemDialog();

	var adultToothNumbers = getToothNumbers(true);
	var pediacToothNumbers = getToothNumbers(false);

	if (isNaN(pediacToothNumbers[1])) {
		pediacToothNumbers.sort();
	} else {
		pediacToothNumbers.sort(function(a,b){return a-b});
	}
	pediacToothNumbers.splice(0, 0, 'None'); // add the 'None' option as the first element of the array.
	if (isNaN(adultToothNumbers[1])) {
		adultToothNumbers.sort();
	} else {
		adultToothNumbers.sort(function(a,b){return a-b});
	}
	loadSelectBox(document.getElementById('flt_tooth_number'), pediacToothNumbers.concat(adultToothNumbers), null, null, '-- All --', '');
	// filter results with the default values.
	document.getElementById('flt_duration').value = 3;
	document.getElementById('flt_treatment_status').value = 'P';
	filterResults(document.getElementById('flt_treatment_status'));
	filterResults(document.getElementById('flt_duration'));
	d_plnDocAutoComp = initDoctorAutoComplete('d_pln_doctor', 'plnDoctorContainer', 'd', 'planned');
	d_condDocAutoComp = initDoctorAutoComplete('d_cond_doctor', 'condDoctorContainer', 'd', 'conducting');
	ed_plnDocAutoComp = initDoctorAutoComplete('ed_pln_doctor', 'ed_plnDoctorContainer', 'ed', 'planned');
	ed_condDocAutoComp = initDoctorAutoComplete('ed_cond_doctor', 'ed_condDoctorContainer', 'ed', 'conducting');
	applyDoctorAutoComplete();

	enableDisableDisOrderBtn();
	//not showing loyalty card as payment mode in dental screen
	$(".dropdown option[value='-3']").remove();
}

function modifyUOMLabel(obj, prefix) {
	document.getElementById(prefix+ '_consumption_uom_label').textContent = obj.value;
}

var taskUserAC = new Array();
function taskUserAutoComplete(elId, containerId, specialityId, hiddenElId) {
	if (!empty(taskUserAC[elId])) {
		taskUserAC[elId].destroy();
		taskUserAC[elId] = null;
	}
	var doctorsList = doctorsJSON;
	if (!empty(specialityId))
		doctorsList = filterList(doctorsJSON, "speciality_id", specialityId);
	var ds = new YAHOO.util.LocalDataSource({result: doctorsList});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				]
	};

	var autoComp = new YAHOO.widget.AutoComplete(elId, containerId, ds);
	autoComp.minQueryLength = 1;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 50;
	autoComp.resultTypeList = false;
	autoComp.forceSelection = true;
	autoComp.autoSnapContainer = false;

	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	autoComp.itemSelectEvent.subscribe(function selectDoctor(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById(hiddenElId).value = record.doctor_id;
	});
	autoComp.selectionEnforceEvent.subscribe(function doctorDoctor(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById(hiddenElId).value = '';
	});
	taskUserAC[elId] = autoComp;
	return autoComp;

}

function applyCondDoctor() {
	var condDoctorId = document.getElementById('apply_cond_doctor_id_for_selected').value;
	if (empty(condDoctorId)) {
		alert("Please select the conducting doctor.");
		return false;
	}
	var chkboxes = document.getElementsByName('cond_doctor_chk');
	var h_treatment_id = document.getElementsByName('h_treatment_id');
	var h_speciality_id = document.getElementsByName("h_doc_speciality_id");
	var h_service_name = document.getElementsByName("h_service_name");
	var edited = document.getElementsByName('ht_edited');
	var servicesArray = new Array();

	var table = document.getElementById('trtmtDetails');
	var rowsSelected = false;
	for (var i=0; i<h_treatment_id.length-1; i++) {
		if (!chkboxes[i].checked) continue;

		rowsSelected = true;
		var speciality = h_speciality_id[i].value;
		var doctor = findInList(doctorsJSON, "doctor_id", condDoctorId);
		var row = table.rows[i+1];

		if (!empty(speciality)) {
			if (doctor.speciality_id == speciality) {
				document.getElementsByName('h_completed_by')[i].value = condDoctorId;
				document.getElementsByName('h_completed_by_name')[i].value = doctor.doctor_name;
				setNodeText(row.cells[COMPLETED_BY], doctor.doctor_name, 10);
				edited[i].value = 'true';
			} else {
				servicesArray.push(h_service_name[i].value);
			}
		} else {
			document.getElementsByName('h_completed_by')[i].value = condDoctorId;
			document.getElementsByName('h_completed_by_name')[i].value = doctor.doctor_name;
			setNodeText(row.cells[COMPLETED_BY], doctor.doctor_name, 10);
			edited[i].value = 'true';
		}
	}
	enableDisableSave('', false);
	if (!rowsSelected) {
		alert("Please select atleast one Service before applying Conducting Doctor.");
		return false;
	}
	if (servicesArray.length > 0) {
		alert("Selected Conducting Doctor not applicable for the following Services. \n\n * "+ servicesArray.join("\n * "));
		return false;
	}
}

function applyDoctorAutoComplete() {
	var ds = new YAHOO.util.LocalDataSource({result: doctorsJSON});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				]
	};

	docAutoComp = new YAHOO.widget.AutoComplete('apply_cond_doctor_for_selected', 'apply_cond_doctor_for_selected_container', ds);
	docAutoComp.minQueryLength = 1;
	docAutoComp.animVert = false;
	docAutoComp.maxResultsDisplayed = 50;
	docAutoComp.resultTypeList = false;
	docAutoComp.forceSelection = true;
	docAutoComp.autoSnapContainer = false;

	docAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	docAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	docAutoComp.itemSelectEvent.subscribe(function selectDoctor(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('apply_cond_doctor_id_for_selected').value = record.doctor_id;
	});
	docAutoComp.selectionEnforceEvent.subscribe(function doctorDoctor(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('apply_cond_doctor_id_for_selected').value = '';
	});

	return docAutoComp;
}

var d_plnDocAutoComp = null;
var d_condDocAutoComp = null;
var ed_plnDocAutoComp = null;
var ed_condDocAutoComp = null;
function initDoctorAutoComplete(elId, containerId, prefix, docType) {
	var ds = new YAHOO.util.LocalDataSource({result: doctorsJSON});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				]
	};

	docAutoComp = new YAHOO.widget.AutoComplete(elId, containerId, ds);
	docAutoComp.minQueryLength = 1;
	docAutoComp.animVert = false;
	docAutoComp.maxResultsDisplayed = 50;
	docAutoComp.resultTypeList = false;
	docAutoComp.forceSelection = true;
	docAutoComp.autoSnapContainer = false;

	docAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	docAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	docAutoComp.itemSelectEvent.subscribe(function selectDoctor(sType, oArgs) {
		if (prefix == 'ed') {
			trtFieldEdited = true;
		}
		var record = oArgs[2];
		if (docType == 'planned') {
			document.getElementById(prefix+'_planned_by').value = record.doctor_id;
		} else if (docType == 'conducting') {
			document.getElementById(prefix+'_completed_by').value = record.doctor_id;
		}
	});
	docAutoComp.selectionEnforceEvent.subscribe(function doctorDoctor(sType, oArgs) {
		if (prefix == 'ed') {
			trtFieldEdited = true;
		}
		var record = oArgs[2];
		if (docType == 'planned') {
			document.getElementById(prefix+'_planned_by').value = '';
		} else if (docType == 'conducting') {
			document.getElementById(prefix+'_completed_by').value = '';
		}
	});

	return docAutoComp;
}

var condDocAutoComp = null;
function initCondDoctorAutoComplete(elId, containerId, prefix, specialityId) {
	if (!empty(d_condDocAutoComp)) {
		d_condDocAutoComp.destroy();
		d_condDocAutoComp = null;
	}

	var doctorsList = doctorsJSON;
	if (!empty(specialityId))
		doctorsList = filterList(doctorsJSON, "speciality_id", specialityId);
	var ds = new YAHOO.util.LocalDataSource({result: doctorsList});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				]
	};

	condDocAutoComp = new YAHOO.widget.AutoComplete(elId, containerId, ds);
	condDocAutoComp.minQueryLength = 1;
	condDocAutoComp.animVert = false;
	condDocAutoComp.maxResultsDisplayed = 50;
	condDocAutoComp.resultTypeList = false;
	condDocAutoComp.forceSelection = true;
	condDocAutoComp.autoSnapContainer = false;

	condDocAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	condDocAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	condDocAutoComp.itemSelectEvent.subscribe(function selectDoctor(sType, oArgs) {
		if (prefix == 'ed') {
			trtFieldEdited = true;
		}
		var record = oArgs[2];
		document.getElementById(prefix+'_completed_by').value = record.doctor_id;
	});
	condDocAutoComp.selectionEnforceEvent.subscribe(function doctorDoctor(sType, oArgs) {
		if (prefix == 'ed') {
			trtFieldEdited = true;
		}
		var record = oArgs[2];
		document.getElementById(prefix+'_completed_by').value = '';
	});

	return condDocAutoComp;
}

var editCondDocAutoComp = null;
function initEditCondDoctorAutoComplete(elId, containerId, prefix, specialityId) {
	if (!empty(ed_condDocAutoComp)) {
		ed_condDocAutoComp.destroy();
		ed_condDocAutoComp = null;
	}

	var doctorsList = doctorsJSON;
	if (!empty(specialityId))
		doctorsList = filterList(doctorsJSON, "speciality_id", specialityId);
	var ds = new YAHOO.util.LocalDataSource({result: doctorsList});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				]
	};

	editCondDocAutoComp = new YAHOO.widget.AutoComplete(elId, containerId, ds);
	editCondDocAutoComp.minQueryLength = 1;
	editCondDocAutoComp.animVert = false;
	editCondDocAutoComp.maxResultsDisplayed = 50;
	editCondDocAutoComp.resultTypeList = false;
	editCondDocAutoComp.forceSelection = true;
	editCondDocAutoComp.autoSnapContainer = false;

	editCondDocAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	editCondDocAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	editCondDocAutoComp.itemSelectEvent.subscribe(function selectDoctor(sType, oArgs) {
		if (prefix == 'ed') {
			trtFieldEdited = true;
		}
		var record = oArgs[2];
		document.getElementById(prefix+'_completed_by').value = record.doctor_id;
	});
	editCondDocAutoComp.selectionEnforceEvent.subscribe(function doctorDoctor(sType, oArgs) {
		if (prefix == 'ed') {
			trtFieldEdited = true;
		}
		var record = oArgs[2];
		document.getElementById(prefix+'_completed_by').value = '';
	});

	return editCondDocAutoComp;
}

function getToothNumbers(adult) {
	var toothNumbers = new Array();
	var teeth = adult ? adult_tooth_image_details_json.teeth : pediac_tooth_details_json.teeth;
	if (tooth_numbering_system == 'U') {
		for (var toothNumber in teeth) {
			toothNumbers.push(toothNumber);
		}
	} else {
		for (var toothNumber in teeth) {
			toothNumbers.push(teeth[toothNumber].toothNumberFDI);
		}
	}
	return toothNumbers;
}

var toothNumDialog = null;
var parentDialog = null;
var childDialog = null;
function initToothNumberDialog() {
	var dialogDiv = document.getElementById("toothNumDialog");
	dialogDiv.style.display = 'block';
	toothNumDialog = new YAHOO.widget.Dialog("toothNumDialog",
			{	width:"650px",
				context : ["toothNumDialog", "tr", "tl"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('toothNumDialog_ok', 'click', updateToothNumbers, toothNumDialog, true);
	YAHOO.util.Event.addListener('toothNumDialog_close', 'click', cancelToothNumDialog, toothNumDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelToothNumDialog,
	                                                scope:toothNumDialog,
	                                                correctScope:true } );
	toothNumDialog.cancelEvent.subscribe(cancelToothNumDialog);
	toothNumDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	toothNumDialog.render();
}

function showToothNumberDialog(action, obj) {
	var els = document.getElementsByName('d_chk_tooth_number');
	document.getElementById('dialog_type').value = action;
	var tnumbers = document.getElementById((action == 'add' ? 'd' : 'ed') + '_tooth_number').value.split(",");
	for (var i=0; i<els.length; i++) {
		var checked = false;
		for (var j=0; j<tnumbers.length; j++) {
			if (els[i].value == tnumbers[j]) {
				checked = true;
				break;
			}
		}
		els[i].checked = checked;
	}
	toothNumDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	toothNumDialog.show();
	childDialog = true;
}

function updateToothNumbers() {
	var els = document.getElementsByName('d_chk_tooth_number');
	var tooth_numbers = '';
	var tooth_numbers_text = '';
	var checked_toothNos = 0;
	for (var i=0; i<els.length; i++) {
		if (!els[i].checked) continue;

		if (tooth_numbers != '') {
			tooth_numbers += ',';
			tooth_numbers_text += ',';
		}
		if (checked_toothNos%10 == 0)
			tooth_numbers_text += '\n';

		checked_toothNos++;
		tooth_numbers += els[i].value;
		tooth_numbers_text += els[i].value;
	}
	var action = document.getElementById('dialog_type').value;
	if (action != 'add' && checked_toothNos > 1) {
		alert('Multiple tooth numbers are not allowed.');
		return false;
	}
	document.getElementById(action == 'add' ? 'd_planned_qty' : 'ed_planned_qty').value = checked_toothNos;

	document.getElementById(action == 'add' ? 'd_tooth_number' : 'ed_tooth_number').value = tooth_numbers;
	document.getElementById(action == 'add' ? 'dToothNumberDiv' : 'edToothNumberDiv').textContent = tooth_numbers_text;
	if (action != 'add')
		trtFieldEdited = true;
	childDialog = null;
	this.cancel();
}

function cancelToothNumDialog() {
	childDialog = null;
	toothNumDialog.hide();
}


var subTaskDialog = null;
function initSubTaskDialog() {
	var dialogDiv = document.getElementById("serviceSubTaskDiv");
	dialogDiv.style.display = 'block';
	subTaskDialog = new YAHOO.widget.Dialog("serviceSubTaskDiv",
			{	width:"650px",
				context : ["serviceSubTaskDiv", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('task_Ok', 'click', addServiceSubTasks, subTaskDialog, true);
	YAHOO.util.Event.addListener('task_Cancel', 'click', handleSubTaskDialogCancel, subTaskDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleSubTaskDialogCancel,
	                                                scope:subTaskDialog,
	                                                correctScope:true } );
	subTaskDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	subTaskDialog.render();
}

function handleSubTaskDialogCancel() {
	this.cancel();
}

function addServiceSubTasks() {
	var id = document.getElementById('service_row_id').value;
	var taskIdEls = document.getElementsByName('taskId');
	var taskNames = document.getElementsByName('taskName');
	var plnDate = document.getElementsByName('h_planned_date')[parseInt(id)].value;

	for (var i=0; i<taskIdEls.length-1; i++) {
		var status = document.getElementsByName('taskStatus')[i].value;
		if (status == 'C') {
			var userName = document.getElementsByName('taskUserName')[i].value;
			if (userName == '') {
				alert("Please select the user name.");
				document.getElementsByName('taskUserName')[i].focus();
				return false;
			}
			var completionDate = document.getElementsByName('taskCompletionDate')[i].value;
			if (completionDate == '') {
				alert("Please select the Completion Date.");
				document.getElementsByName('taskCompletionDate')[i].focus();
				return false;
			}
			if (!doValidateDateField(document.getElementsByName('taskCompletionDate')[i], 'past')) {
				document.getElementsByName('taskCompletionDate')[i].focus();
				return false;
			}
			var completionTime = document.getElementsByName('taskCompletionTime')[i].value;
			if (completionTime == '') {
				alert("Please select the Completion Time.");
				document.getElementsByName('taskCompletionTime')[i].focus();
				return false;
			}
			if (!validateTime(document.getElementsByName('taskCompletionTime')[i])) {
				document.getElementsByName('taskCompletionTime')[i].focus();
				return false;
			}

			var taskComplDateTime = getDateTime(completionDate, completionTime);
			var plnDateTime = getDateTime(plnDate.split(' ')[0], plnDate.split(' ')[1]);
			var today = new Date();
			if (taskComplDateTime < plnDateTime || taskComplDateTime > today) {
				alert(taskNames[i].value +"'s Completion Date & Time should be \n * greater than or equal to Planned Date & Time and \n * less than or equal to today");
				return false;
			}
		}
	}
	var hasTasks = false;
	var complCount = 0;
	for (var i=0; i<taskIdEls.length-1; i++) {
		hasTasks = true;
		complCount = (document.getElementsByName('taskStatus')[i].value == 'C'
					|| document.getElementsByName('taskStatus')[i].value == 'NR') ? complCount+1 : complCount;

		setHiddenValue(i, "sub_task_id_"+id, taskIdEls[i].value);
	   	setHiddenValue(i, "st_username_"+id, document.getElementsByName('taskUserName')[i].value);
	   	setHiddenValue(i, "st_completion_date_"+id, document.getElementsByName('taskCompletionDate')[i].value);
	   	setHiddenValue(i, "st_completion_time_"+id, document.getElementsByName('taskCompletionTime')[i].value);
	   	setHiddenValue(i, "st_status_"+id, document.getElementsByName('taskStatus')[i].value);
	   	setHiddenValue(i, "sub_task_name_"+id, document.getElementsByName('taskName')[i].value);
	   	setHiddenValue(i, "task_completed_by_"+id, document.getElementsByName('taskUserId')[i].value);
  	}
  	var trtRow = getThisRow(document.getElementsByName("h_treatment_id")[id]);
  	var taskStatus = "None";
  	if (hasTasks) {
  		if (complCount == 0) {
  			taskStatus = "Not Completed"
  		} else if (complCount > 0 && complCount == taskIdEls.length-1) {
  			taskStatus = "Completed"
  		} else if (complCount > 0 && complCount < taskIdEls.length-1) {
  			taskStatus = "Partial";
  		}
  	}
  	// after completing the main service, if user edits any of the task status to not completed then changing main service status to 'In Progress'.
  	// this is because until & unless user completes all his task items we should not allow him to mark the main service status to 'Completed'.
  	var treatmentStatus = document.getElementsByName('h_treatment_status')[id].value;
  	if (treatmentStatus == 'C' && (taskStatus == 'Not Completed' || taskStatus == 'Partial')) {
  		document.getElementsByName('h_treatment_status')[id].value = 'I';
		setNodeText(trtRow.cells[TREATMENT_STATUS], 'In Progress');
  	}
	setNodeText(trtRow.cells[TR_TASK_STATUS], taskStatus);
  	subTaskDialog.hide();
}

function getFinalTaskStatus(rowid) {
	var hasTasks = false;
	var complCount = 0;
	var taskIdEls = document.getElementsByName('sub_task_id_'+rowid);
	for (var i=0; i<taskIdEls.length; i++) {
		if (taskIdEls[i].value == '_') break;

		hasTasks = true;
		complCount = (document.getElementsByName('st_status_'+rowid)[i].value == 'C'
					|| document.getElementsByName('st_status_'+rowid)[i].value == 'NR') ? complCount+1 : complCount;

  	}

  	var taskStatus = "None";
  	if (hasTasks) {
  		if (complCount == 0) {
  			taskStatus = "Not Completed"
  		} else if (complCount > 0 && complCount == taskIdEls.length) {
  			taskStatus = "Completed"
  		} else if (complCount > 0 && complCount < taskIdEls.length) {
  			taskStatus = "Partial";
  		}
  	}
	return taskStatus;
}


function showSubTaskDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	if(empty(getIndexedValue('h_service_presc_id', id))) {
		alert("Before adding the service sub task please order the service.");
		return false;
	}

	document.getElementById('service_row_id').value = id;
	subTaskDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	subTaskDialog.show();

	var tab = document.getElementById('subTasksListInDialog');
	for (var i=1; i<tab.rows.length-1; ) {
		tab.deleteRow(i);
	}
	document.getElementById('serviceNameForSubTasks').textContent = getIndexedValue("h_service_name", id);
	var subTaskIdEls = document.getElementsByName("sub_task_id_"+id);
	var subTaskUserNameEls = document.getElementsByName("st_username_"+id);
	var subTaskDateEls = document.getElementsByName("st_completion_date_"+id);
	var subTaskTimeEls = document.getElementsByName("st_completion_time_"+id);
	var subTaskNameEls = document.getElementsByName("sub_task_name_"+id);
	var subTaskStatusEls = document.getElementsByName("st_status_"+id);
	var subTaskPrescIds = document.getElementsByName("task_presc_id_"+id);
	var subTaskUserIdEls = document.getElementsByName('task_completed_by_'+id);
	var speciality = getIndexedValue('h_doc_speciality_id', id);

   	var table = document.getElementById("subTasksListInDialog");
	var templateRow = table.rows[getTemplateRow('subTasksListInDialog')];

	taskUserAutocompArray = new Array();

	for (var i=0; i<subTaskIdEls.length; i++) {
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);

		setNodeText(row.cells[0], subTaskNameEls[i].value, 20);
		getElementByName(row, 'taskName').value = subTaskNameEls[i].value;
		getElementByName(row, 'taskId').value = subTaskIdEls[i].value;
		getElementByName(row, 'taskStatus').value = subTaskStatusEls[i].value;
		getElementByName(row, 'taskPrescId').value = subTaskPrescIds[i].value;

		row.cells[2].innerHTML = '<div id="taskUserNameAC'+i+'" style="width: 150px"><input type="text" name="taskUserName" id="taskUserName'+i+'" value="'+subTaskUserNameEls[i].value+'"/>'
								+ '<div id="taskUserNameContainer'+i+'" class="scrolForContainer" style="right: 0px; width: 300px;"></div></div>';

		row.cells[2].appendChild(makeHidden('taskUserId', 'taskUserId'+i, subTaskUserIdEls[i].value));
		autoComp = taskUserAutoComplete('taskUserName'+i, 'taskUserNameContainer'+i, speciality, 'taskUserId'+i);

		if (autoComp._elTextbox.value != '') {
			autoComp._bItemSelected = true;
			autoComp._sInitInputValue = autoComp._elTextbox.value;
		}


		row.cells[3].innerHTML = getDateWidget('taskCompletionDate', 'taskCompletionDate_'+i,
			empty(subTaskDateEls[i].value) ? null : parseDateStr(subTaskDateEls[i].value), 'past', '', true, true, '', cpath);
		row.cells[3].innerHTML += '<input type="text" name="taskCompletionTime" id="taskCompletionTime_'+i+'" class="number" value="'+subTaskTimeEls[i].value+'"/>';
   		makePopupCalendar('taskCompletionDate_'+i);

   		document.getElementById('taskCompletionDate_'+i).disabled = subTaskStatusEls[i].value != 'C';
   		document.getElementById('taskCompletionTime_'+i).disabled = subTaskStatusEls[i].value != 'C';
		document.getElementsByName('taskUserName')[i].disabled = subTaskStatusEls[i].value != 'C';
	}
	return false;
}

function enableTaskFields(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var speciality = getIndexedValue('h_doc_speciality_id', id);

	document.getElementById('taskCompletionDate_'+id).disabled = obj.value != 'C';
	document.getElementById('taskCompletionTime_'+id).disabled = obj.value != 'C';
	document.getElementsByName('taskUserName')[id].disabled = obj.value != 'C';

	// showing the logged in doctor as default when user is marking the subtask as completed.
	// if the service is having doctor speciality, and if the logged in doctor falls under that speciality then display doctor.
	var doctorId = '';
	var doctorName = '';
	if (!empty(user.doctor_id)) {
		var doctor_bean = null;
		if (!empty(speciality))
			doctor_bean = findInList2(doctorsJSON, "speciality_id", speciality, "doctor_id", user.doctor_id);
		else
			doctor_bean = findInList(doctorsJSON, "doctor_id", user.doctor_id);
		if (!empty(doctor_bean)) {
			doctorId = doctor_bean.doctor_id;
			doctorName = doctor_bean.doctor_name;
		}
	}

	document.getElementsByName('taskUserName')[id].value = obj.value == 'C' ? doctorName : '' ;
	document.getElementsByName('taskUserId')[id].value = obj.value == 'C' ? doctorId : '';
	var autoComp = taskUserAC['taskUserName'+id];
	if (autoComp._elTextbox.value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = autoComp._elTextbox.value;
	}

	document.getElementById('taskCompletionDate_'+id).value = (obj.value == 'C' ? formatDate(new Date(), 'ddmmyyyy') : '');
	document.getElementById('taskCompletionTime_'+id).value = (obj.value == 'C' ? formatTime(new Date()) : '');
}

function getServiceSubTasks(serviceId, index, noOfTimes) {
	// gets the service sub tasks for the first time when you try to add the service to grid,
	// got the tasks created elements but failed some validation in add dialog and so it is not added to grid
	// dialog is still open and user corrected the data and again try to add the treatment since we have retrieved the service sub tasks first time
	// we should not make second call just return true.
	if (document.getElementsByName('sub_task_id_'+index).length != 0)
		return true; // found elements.

	var url = cpath + '/DentalConsultation/Consultation.do?_method=getServiceSubTasks&service_id='+serviceId;
	var reqObject = newXMLHttpRequest();
	reqObject.open("GET", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
			eval('var tasks='+reqObject.responseText);
			var table = document.getElementById('serviceSubTasksTab');
			noOfTimes = empty(noOfTimes) ? 1 : noOfTimes;
			for (var h=0; h<noOfTimes; h++) {
				for (var i=0; i<tasks.length; i++) {
					var row = table.insertRow(-1);
					var cell = row.insertCell(-1);
					cell.appendChild(makeHidden('sub_task_id_'+index, '', tasks[i].sub_task_id));
					cell.appendChild(makeHidden('st_treatment_id_'+index, '', '_'));
					cell.appendChild(makeHidden('st_username_'+index, '', ''));
					cell.appendChild(makeHidden('st_completion_date_'+index, '', ''));
					cell.appendChild(makeHidden('st_completion_time_'+index, '', ''));
					cell.appendChild(makeHidden('st_status_'+index, '', 'NC'));
					cell.appendChild(makeHidden('sub_task_name_'+index, '', tasks[i].desc_short));
					cell.appendChild(makeHidden('task_presc_id_'+index, '', '_'));
					cell.appendChild(makeHidden('task_completed_by_'+index, '', ''));
				}
				if (empty(tasks)) {
					var row = table.insertRow(-1);
					var cell = row.insertCell(-1);
					cell.appendChild(makeHidden('sub_task_id_'+index, '', '_'));
					cell.appendChild(makeHidden('st_treatment_id_'+index, '', '_'));
					cell.appendChild(makeHidden('st_username_'+index, '', ''));
					cell.appendChild(makeHidden('st_completion_date_'+index, '', ''));
					cell.appendChild(makeHidden('st_completion_time_'+index, '', ''));
					cell.appendChild(makeHidden('st_status_'+index, '', 'NC'));
					cell.appendChild(makeHidden('sub_task_name_'+index, '', ''));
					cell.appendChild(makeHidden('task_presc_id_'+index, '', '_'));
					cell.appendChild(makeHidden('task_completed_by_'+index, '', ''));
				}
				index++;
			}
			return true;
		}
	}
	return false;
}


var addTrtmtItemDialog = null;
function initTrtmtItemDialog() {
	var dialogDiv = document.getElementById("addTrtmtDialog");
	dialogDiv.style.display = 'block';
	addTrtmtItemDialog = new YAHOO.widget.Dialog("addTrtmtDialog",
			{	width:"600px",
				context : ["addTrtmtDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('trtmtAdd', 'click', addTrtmtToTable, addTrtmtItemDialog, true);
	YAHOO.util.Event.addListener('trtmtClose', 'click', handleAddTrtmtItemCancel, addTrtmtItemDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddTrtmtItemCancel,
	                                                scope:addTrtmtItemDialog,
	                                                correctScope:true } );
	addTrtmtItemDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addTrtmtItemDialog.render();
}

function setStartDate(obj, prefix) {
	if (obj.value != '' && document.getElementById(prefix+'_start_date').value  == '') {
		document.getElementById(prefix+'_start_date').value = formatDate(new Date());
	}
}


var colIndex=0;
var CHECKBOX_BILL = colIndex++, TOOTH_NUMBER = colIndex++, SERVICE = colIndex++, TREATMENT_STATUS = colIndex++,
	PLANNED_BY = colIndex++, PLANNED_DATE = colIndex++, START_DATE = colIndex++, COMPLETED_BY = colIndex++,
	COMPLETED_DATE = colIndex++, BILLING_STATUS = colIndex++, RATE = colIndex++, PLANNED_QTY = colIndex++,
	DISCOUNT = colIndex++, AMOUNT = colIndex++,
	COMMENTS = colIndex++, TR_TASK_STATUS = colIndex++, TR_TRASH_COL = colIndex++, TR_EDIT_COL = colIndex++, TR_TASK_COL = colIndex++;
function addTrtmtToTable() {
	var itemName = document.getElementById('d_serviceName').value;
	if (itemName == '') {
   		alert('Please enter the service');
   		document.getElementById('d_serviceName').focus();
   		return false;
   	}
	var id = getNumCharges('trtmtDetails');

	var centerId = gMax_centers_inc_default > 1 ? document.getElementById('d_center_id').value : '0';
   	var serviceId = document.getElementById('d_service_id').value;
   	var docSpeciality = document.getElementById('d_doc_speciality').value;
   	var charge = document.getElementById('d_service_charge').value;
   	var discount = document.getElementById('d_service_discount').value;
   	var tooth_number = document.getElementById('d_tooth_number').value;
   	var treatment_status = document.getElementById('d_treatment_status').value;
   	var planned_by = document.getElementById('d_planned_by').value;
   	var planned_date = document.getElementById('d_planned_date').value;
   	var planned_time = document.getElementById('d_planned_time').value;
   	var completed_date = document.getElementById('d_completed_date').value;
   	var completed_time = document.getElementById('d_completed_time').value;
   	var completed_by = document.getElementById('d_completed_by').value;
   	var comments = document.getElementById('d_comments').value;
   	var service_group_name = document.getElementById('d_service_group_name').value;
   	var planned_by_name = document.getElementById('d_pln_doctor').value;
	var completed_by_name = document.getElementById('d_cond_doctor').value;
	var tooth_num_required = document.getElementById('d_tooth_num_required').value;
	var creation_date = document.getElementById('d_creation_date').value;
	var creation_time = document.getElementById('d_creation_time').value;
	var start_date = document.getElementById('d_start_date').value;
	var start_time = document.getElementById('d_start_time').value;
	var planned_qty = document.getElementById('d_planned_qty').value;
	var newDen = 'newDen';

	if (!validateDueDateTime('d_planned_date', 'd_planned_time', 'Planned Date', 'Planned Time', null, false))
		return false;
	if (!validateDueDateTime('d_completed_date', 'd_completed_time', 'Completed Date', 'Completed Time', null, true))
		return false;
	if (!validateDueDateTime('d_creation_date', 'd_creation_time', 'Creation Date', 'Creation Time', null, false))
		return false;
	if (!validateDueDateTime('d_start_date', 'd_start_time', 'Start Date', 'Start Time', null, true))
		return false;

	if (tooth_number == '' && tooth_num_required == 'Y') {
		document.getElementById('d_tooth_number').focus();
		alert("This Service required Tooth Number.");
		return false;
	}
	if (planned_qty == '' || planned_qty == 0 || !isInteger(planned_qty)) {
		alert('Planned Qty should be a whole number and should be greater than zero');
		document.getElementById('d_planned_qty').focus();
		return false;
	}

	var suppliesListTable = document.getElementById('addDentalSuppliestabel');
	if(suppliesListTable.rows.length-1 > 0) {
		if(!validateDentalSupplies(suppliesListTable, 'd'))
			return false;
	}

	var matched_pqty_and_tooth_nos = false;
	if (!empty(tooth_number)) {
		var tooth_nos = tooth_number.split(",").length
		if (tooth_nos == planned_qty) {
			matched_pqty_and_tooth_nos = true;
		}
	}

	if (!getServiceSubTasks(serviceId, id, parseInt(planned_qty))) {
		alert("Failed to get Service Sub Tasks.");
		return false;
	}
	var finalTaskStatus = getFinalTaskStatus(id);
	if (treatment_status == 'C' && (finalTaskStatus == 'Partial' || finalTaskStatus == 'Not Completed')) {
		alert('You cannot mark the status as completed until you finish all the sub tasks.');
		return false;
	}

	for (var h=0; h<parseInt(planned_qty); h++) {
		id = getNumCharges('trtmtDetails');

	   	var table = document.getElementById("trtmtDetails");
		var templateRow = table.rows[getTemplateRow('trtmtDetails')];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	   	row.id = "itemRow" + id;

		creaHiddValForDenSupplies(row , suppliesListTable, 'd', centerId);

		/*
			if selected tooth numbers count is matching with planeed qty then split the tooth number and put against each row.
			otherwise add all the tooths to the first row. user has to edit the tooth numbers manually accordingly for each row.
			if the user forgot to edit, block it on save.
		*/
		var toothNo = '';
		if (matched_pqty_and_tooth_nos) {
			toothNo = tooth_number.split(',')[h];
		} else if (!empty(tooth_number) && h==0) {
			toothNo = tooth_number;
		}
	   	setNodeText(row.cells[TOOTH_NUMBER], toothNo, 10);
	   	setNodeText(row.cells[SERVICE], itemName +" ("+ service_group_name +")", 20);
	   	setNodeText(row.cells[TREATMENT_STATUS],
	   		document.getElementById('d_treatment_status').options[document.getElementById('d_treatment_status').selectedIndex].text);
	   	setNodeText(row.cells[PLANNED_BY], planned_by_name, 10);
	   	setNodeText(row.cells[PLANNED_DATE], planned_date + ' ' + planned_time);
	   	var startDateTime = start_date != '' ? (start_date + ' ' + start_time) : '';
	   	setNodeText(row.cells[START_DATE], startDateTime);
	   	setNodeText(row.cells[COMPLETED_BY], completed_by_name, 10);
	   	var completedDateTime = completed_date != '' ? (completed_date + ' ' + completed_time) : '';
	   	setNodeText(row.cells[COMPLETED_DATE], completedDateTime);
	   	setNodeText(row.cells[BILLING_STATUS], '');
	   	setNodeText(row.cells[RATE], formatAmountValue(charge));
	   	setNodeText(row.cells[PLANNED_QTY], 1);
	   	setNodeText(row.cells[DISCOUNT], formatAmountValue(discount*1));
	   	setNodeText(row.cells[AMOUNT], formatAmountValue((charge-discount)*1));
	   	setNodeText(row.cells[COMMENTS], comments, 10);

		// while adding treatment you cannot edit the details of subtasks, so just mark status as None if no tasks found,
		// else Not Completed. he can edit the sub task status using another icon in the grid and change this status.
	   	var taskStatus = 'None';
	   	if (document.getElementsByName('sub_task_id_'+id)[0].value != '_') {
	   		taskStatus = 'Not Completed';
	   	}
	   	document.getElementsByName("_taskEditAnchor")[id].style.display = taskStatus == 'None' ? 'none' : 'block';
	   	document.getElementsByName("disabledTaskIcon")[id].style.display = taskStatus == 'None' ? 'block' : 'none';

	   	setNodeText(row.cells[TR_TASK_STATUS], taskStatus);

		setHiddenValue(id, "h_treatment_id", "_");
		if (tooth_numbering_system == 'U')
		   	setHiddenValue(id, "h_tooth_unv_number", toothNo);
		else
			setHiddenValue(id, "h_tooth_fdi_number", toothNo);
	   	setHiddenValue(id, "h_service_name", itemName);
	   	setHiddenValue(id, "h_service_id", serviceId);
	   	setHiddenValue(id, "h_doc_speciality_id" , docSpeciality);
	   	setHiddenValue(id, "h_service_charge", charge);
	   	setHiddenValue(id, "h_service_discount", discount);
	   	setHiddenValue(id, "h_treatment_status", treatment_status);
	   	setHiddenValue(id, "h_planned_by", planned_by);
	   	setHiddenValue(id, "h_planned_date", planned_date + ' ' + planned_time);
	   	setHiddenValue(id, "h_start_date", startDateTime);
	   	setHiddenValue(id, "h_completed_by", completed_by);
	   	setHiddenValue(id, "h_completed_date", completedDateTime);
	   	setHiddenValue(id, "h_comments", comments);
	   	setHiddenValue(id, "h_service_group_name", service_group_name);
	   	setHiddenValue(id, "h_tooth_num_required", tooth_num_required);
	   	setHiddenValue(id, "h_creation_date", creation_date + ' ' + creation_time);
	   	setHiddenValue(id, "h_qty", 1);
	   	setHiddenValue(id, "h_planned_qty", 1);
	   	setHiddenValue(id, "h_planned_by_name", planned_by_name);
	   	setHiddenValue(id, "h_completed_by_name", completed_by_name);
	   	setHiddenValue(id, "h_den_supplies_flag", newDen);
	   	setHiddenValue(id, "h_order_index", id);
	   	setHiddenValue(id, "h_service_idx", id);
   	}
	clearTrtmtDialogFields();
   	addTrtmtItemDialog.align("tr", "tl");
	document.getElementById('d_tooth_number').focus();

	return id;
}

function selectAllTreatForOrderAndFinalize() {
	var treatCheckElmts = document.consultationForm.orderAndFinalizeCheck;
	if (document.getElementById("orderAndFinalizeAll").checked)	{
		for(var i=0;i<treatCheckElmts.length;i++) {
			if (getThisRow(treatCheckElmts[i]).style.display == "none" || treatCheckElmts[i].disabled){}
			else treatCheckElmts[i].checked=true;
		}
		enableDisableSave('' ,true);
	} else {
		for(var i=0;i<treatCheckElmts.length;i++) {
			treatCheckElmts[i].checked=false;
		}
		enableDisableSave('', false);
	}
}

function validateDentalSupplies (suppliesListTable, prefix) {
	for(var i=0;i<suppliesListTable.rows.length-1;i++) {
		item_id = document.getElementById(prefix+'_item_id'+i).value;
		item_name = document.getElementById(prefix+'_itemLabel'+i).textContent;
		supplier_id = document.getElementById(prefix+'_supplier_id'+i).value;
		shade_id = document.getElementById(prefix+'_shade_id'+i).value;
		item_qty = document.getElementById(prefix+'_item_qty'+i).value;

		if (!empty(supplier_id)) {
			if (item_qty == '') {
				alert("Please Enter a Qty for ("+ item_name +").");
				document.getElementById(prefix+'_item_qty'+i).focus();
				return false;
			}
			if (document.getElementById(prefix+'_pln_doctor').value == ''){
				alert("Please Enter Planned By.");
				document.getElementById(prefix+'_pln_doctor').focus();
				return false;
			}
		}
		if (!empty(item_qty)) {
			if (supplier_id == '') {
				alert("Please Enter a Supplier for ("+ item_name +").");
				document.getElementById(prefix+'_supplier_id'+i).focus();
				return false;
			}
		}
	}
	return true;
}

function creaHiddValForDenSupplies(row, suppliesListTable, prefix, centerId) {
	var id = getRowChargeIndex(row);
	var iDiv = '';
	if (prefix == 'd') {
		iDiv = row.cells[TOOTH_NUMBER].getElementsByTagName('div')[0]
	} else {
		iDiv = row.cells[TOOTH_NUMBER].getElementsByTagName('div')[0];
		var fieldsParent = row.cells[TOOTH_NUMBER].getElementsByTagName('div')[0];
		var fieldsChild = fieldsParent.childNodes;
		for (var k=fieldsChild.length-1;k>=0;k--) {
			if (fieldsChild[k].type == "hidden")
				fieldsParent.removeChild(fieldsChild[k]);
		}
	}
	for(var i=0;i<suppliesListTable.rows.length-1;i++) {
		item_id = document.getElementById(prefix+'_item_id'+i).value;
		item_name = document.getElementById(prefix+'_itemLabel'+i).title;
		supplier_id = document.getElementById(prefix+'_supplier_id'+i).value;
		shade_id = document.getElementById(prefix+'_shade_id'+i).value;
		item_qty = document.getElementById(prefix+'_item_qty'+i).value;
		unit_rate = document.getElementById(prefix+'_unit_rate'+i).value;
		vat_prec = document.getElementById(prefix+'_vat_perc'+i).value;

		iDiv.appendChild(makeHidden('h_'+id+'_supplier_id','h_supplier_id'+i, supplier_id));
		iDiv.appendChild(makeHidden('h_'+id+'_item_id','h_item_id'+i, item_id));
		iDiv.appendChild(makeHidden('h_'+id+'_item_name','h_item_name'+i, item_name));
		iDiv.appendChild(makeHidden('h_'+id+'_shade_id','h_shade_id'+i, shade_id));
		iDiv.appendChild(makeHidden('h_'+id+'_item_qty','h_item_qty'+i, item_qty));
		iDiv.appendChild(makeHidden('h_'+id+'_unit_rate','h_unit_rate'+i, unit_rate));
		iDiv.appendChild(makeHidden('h_'+id+'_vat_perc','h_vat_perc'+i, vat_prec));
		iDiv.appendChild(makeHidden('h_'+id+'_center_id', 'h_center_id'+i, centerId));
	}
	row.cells[TOOTH_NUMBER].appendChild(iDiv);
}

function handleAddTrtmtItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		var suppliesListTable = document.getElementById('addDentalSuppliestabel');
		if(suppliesListTable.rows.length > 0) {
			for(var i=suppliesListTable.rows.length-1;i>0;i--) {
				suppliesListTable.deleteRow(i);
			}
		}
		document.getElementById('addDentalSuppliesDialog').style.display = 'none';
		addTrtmtItemDialog.cancel();
		enableDisableDisOrderBtn();
	}
}

function showAddTrtmtItemDialog(obj) {
	parentDialog = addTrtmtItemDialog;
	var row = getThisRow(obj);

	addTrtmtItemDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addTrtmtItemDialog.show();


	initServiceAutoComplete();
	clearTrtmtDialogFields();
	enableDisableDisOrderBtn();
	if (document.getElementById('flt_tooth_number').value != '') {
		document.getElementById('d_tooth_number').value = document.getElementById('flt_tooth_number').value;
	}
	document.getElementById('d_tooth_number').focus();
	if (!empty(user.doctor_id))
		document.getElementById('d_planned_by').value = user.doctor_id;
	return false;
}

var editTrtmtItemDialog = null;
function initEditTrtmtItemDialog() {
	var dialogDiv = document.getElementById("editTrtmtItemDialog");
	dialogDiv.style.display = 'block';
	editTrtmtItemDialog = new YAHOO.widget.Dialog("editTrtmtItemDialog",{
			width:"600px",
			text: "Edit Item",
			context :["editTrtmtItemDialog", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditItemCancel,
	                                                scope:editTrtmtItemDialog,
	                                                correctScope:true } );
	editTrtmtItemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editTrtmtItemDialog.cancelEvent.subscribe(handleEditItemCancel);

	YAHOO.util.Event.addListener('editTrtmtOk', 'click', editTrtmtTableRow, editTrtmtItemDialog, true);
	YAHOO.util.Event.addListener('editTrtmtCancel', 'click', handleEditTrtmtItemCancel, editTrtmtItemDialog, true);
	YAHOO.util.Event.addListener('editTrtmtPrevious', 'click', openPreviousTrtmt, editTrtmtItemDialog, true);
	YAHOO.util.Event.addListener('editTrtmtNext', 'click', openNextTrtmt, editTrtmtItemDialog, true);
	editTrtmtItemDialog.render();
}

function trtmtFieldEdited() {
	trtFieldEdited = true;
	return true;
}


function addStatusChange(obj) {
	if (obj.value == 'C') {
		if (document.getElementById('d_completed_date').value == '')
			document.getElementById('d_completed_date').value = formatDate(new Date());
		if (document.getElementById('d_completed_time').value == '')
			document.getElementById('d_completed_time').value = formatTime(new Date(), false);
		if (document.getElementById('d_start_date').value == '')
			document.getElementById('d_start_date').value = document.getElementById('d_completed_date').value;
		if (document.getElementById('d_start_time').value == '')
			document.getElementById('d_start_time').value = document.getElementById('d_completed_time').value;
	} else if (obj.value == 'I') {
		if (document.getElementById('d_start_date').value == '')
			document.getElementById('d_start_date').value = formatDate(new Date());
		if (document.getElementById('d_start_time').value == '')
			document.getElementById('d_start_time').value = formatTime(new Date(), false);
		document.getElementById('d_completed_date').value = '';
		document.getElementById('d_completed_time').value = '';
	} else if (obj.value == 'P') {
		document.getElementById('d_start_date').value = '';
		document.getElementById('d_start_time').value = '';
		document.getElementById('d_completed_date').value = '';
		document.getElementById('d_completed_time').value = '';
	} else if (obj.value == 'X') {
		document.getElementById('d_completed_date').value = '';
		document.getElementById('d_completed_time').value = '';
	}
}

function trtmtStatusChange(obj) {
	if (obj.value == 'X' && document.getElementById('ed_service_prescribed_id').value != '') {
		alert("Service is already billed. You can not cancel it.");
		return false;
	}
	if (obj.value == 'C') {
		if (document.getElementById('ed_completed_date').value == '')
			document.getElementById('ed_completed_date').value = formatDate(new Date());
		if (document.getElementById('ed_completed_time').value == '')
			document.getElementById('ed_completed_time').value = formatTime(new Date(), false);
		if (document.getElementById('ed_start_date').value == '')
			document.getElementById('ed_start_date').value = document.getElementById('ed_completed_date').value;
		if (document.getElementById('ed_start_time').value == '')
			document.getElementById('ed_start_time').value = document.getElementById('ed_completed_time').value;

	} else if (obj.value == 'I') {
		if (document.getElementById('ed_start_date').value == '')
			document.getElementById('ed_start_date').value = formatDate(new Date());
		if (document.getElementById('ed_start_time').value == '')
			document.getElementById('ed_start_time').value = formatTime(new Date(), false);
		document.getElementById('ed_completed_date').value = '';
		document.getElementById('ed_completed_time').value = '';
	} else if (obj.value == 'P') {
		document.getElementById('ed_start_date').value = '';
		document.getElementById('ed_start_time').value = '';
		document.getElementById('ed_completed_date').value = '';
		document.getElementById('ed_completed_time').value = '';
	} else if (obj.value == 'X') {
		document.getElementById('ed_completed_date').value = '';
		document.getElementById('ed_completed_time').value = '';
	}
}



var trtFieldEdited = false;
function handleEditTrtmtItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		var id = document.consultationForm.editRowId.value;
		var row = getChargeRow(id, "trtmtDetails");
		YAHOO.util.Dom.removeClass(row, 'editing');
		trtFieldEdited = false;
		var suppliesListTable = document.getElementById('editDentalSuppliestabel');
		if(suppliesListTable.rows.length > 0) {
			for(var i=suppliesListTable.rows.length-1;i>0;i--) {
				suppliesListTable.deleteRow(i);
			}
		}
		document.getElementById('editDentalSuppliesDialog').style.display = 'none';
		enableDisableDisOrderBtn();
		this.hide();
	}
}

function showEditTrtmtItemDialog(obj) {
	parentDialog = editTrtmtItemDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editTrtmtItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editTrtmtItemDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.consultationForm.editTrtmtRowId.value = id;
	document.getElementById('ed_tooth_number').value =
		empty(getIndexedValue('h_tooth_unv_number', id)) ? getIndexedValue('h_tooth_fdi_number', id) : getIndexedValue('h_tooth_unv_number', id);
	var tooth_num_required = getIndexedValue("h_tooth_num_required", id);
	if (tooth_num_required != 'Y' || !empty(getIndexedValue("h_service_presc_id", id)) || getIndexedValue('h_treatment_status', id) == 'C') {
		document.getElementById('edToothNumBtnDiv').style.display = 'none'
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'block'
	} else {
		document.getElementById('edToothNumBtnDiv').style.display = 'block';
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'none';
	}
	var tooth_numbers = (document.getElementById('ed_tooth_number').value).split(",");
	var tooth_numbers_text = '';
	var checked_toothNos = 0;
	for (var i=0; i<tooth_numbers.length; i++) {
		if (tooth_numbers_text != '') {
			tooth_numbers_text += ',';
		}
		if (checked_toothNos%10 == 0)
			tooth_numbers_text += '\n';

		checked_toothNos++;
		tooth_numbers_text += tooth_numbers[i];
	}
	document.getElementById('editDentalSuppliesDialog').style.display = 'none';
	var treatmentId = getIndexedValue('h_treatment_id', id);
	var serviceId = getIndexedValue('h_service_id', id);
	if (getIndexedValue("h_den_supplies_flag", id) == 'newDen') {
		var editDentalSuppliesTable = document.getElementById('editDentalSuppliestabel');
		if(loadEditSuppliesTable(editDentalSuppliesTable, row) > 0)
			document.getElementById('editDentalSuppliesDialog').style.display = 'block';
	} else {
		getDentalSupplies(serviceId, treatmentId);
		var editDentalSuppliesTable = document.getElementById('editDentalSuppliestabel');
		if (loadSuppliesTable(cachedDentalSuppliesTreat[treatmentId], editDentalSuppliesTable, 'ed') > 0)
			document.getElementById('editDentalSuppliesDialog').style.display = 'block';
	}

	document.getElementById('edToothNumberDiv').textContent = tooth_numbers_text;
	document.getElementById('ed_serviceNameLabel').textContent = getIndexedValue("h_service_name", id);
	document.getElementById('ed_treatment_status').value = getIndexedValue("h_treatment_status", id);
	if (getIndexedValue('h_planned_date', id) != '') {
		document.getElementById('ed_planned_date').value = getIndexedValue("h_planned_date", id).split(' ')[0];
		document.getElementById('ed_planned_time').value = getIndexedValue('h_planned_date', id).split(' ')[1];
	} else {
		document.getElementById('ed_planned_date').value = '';
		document.getElementById('ed_planned_time').value = '';
	}
	document.getElementById('ed_planned_by').value = getIndexedValue("h_planned_by", id);
	if (getIndexedValue('h_start_date', id) != '') {
		document.getElementById('ed_start_date').value = getIndexedValue('h_start_date', id).split(' ')[0];
		document.getElementById('ed_start_time').value = getIndexedValue('h_start_date', id).split(' ')[1];
	} else {
		document.getElementById('ed_start_date').value = '';
		document.getElementById('ed_start_time').value = '';
	}
	if (getIndexedValue('h_completed_date', id) != '') {
		document.getElementById('ed_completed_date').value = getIndexedValue("h_completed_date", id).split(' ')[0];
		document.getElementById('ed_completed_time').value = getIndexedValue('h_completed_date', id).split(' ')[1];
	} else {
		document.getElementById('ed_completed_date').value = '';
		document.getElementById('ed_completed_time').value = '';
	}
	document.getElementById('ed_completed_by').value = getIndexedValue("h_completed_by", id);
	document.getElementById('ed_comments').value = getIndexedValue("h_comments", id);
	document.getElementById('ed_service_prescribed_id').value = getIndexedValue("h_service_presc_id", id);
	document.getElementById('ed_tooth_num_required').value = getIndexedValue("h_tooth_num_required", id);
	var docSpeciality = getIndexedValue("h_doc_speciality_id", id);
	document.getElementById('ed_doc_speciality').value = docSpeciality;
	if (getIndexedValue('h_creation_date', id) != '') {
		document.getElementById('ed_creation_date').value = getIndexedValue('h_creation_date', id).split(' ')[0];
		document.getElementById('ed_creation_time').value = getIndexedValue('h_creation_date', id).split(' ')[1];
	} else {
		document.getElementById('ed_creation_date').value = '';
		document.getElementById('ed_creation_time').value = '';
	}

	document.getElementById('ed_planned_qty').value = getIndexedValue('h_planned_qty', id);
	document.getElementById('ed_planned_qty').disabled = true;
	document.getElementById('ed_service_discount').value = getIndexedValue('h_service_discount', id);
	document.getElementById('ed_service_charge').value = getIndexedValue('h_service_charge', id);
	document.getElementById('ed_cond_doctor').value = getIndexedValue('h_completed_by_name', id);
	document.getElementById('ed_pln_doctor').value = getIndexedValue('h_planned_by_name', id);
	if (docSpeciality != null && !empty(docSpeciality)) {
		ed_condDocAutoComp = initEditCondDoctorAutoComplete('ed_cond_doctor', 'ed_condDoctorContainer', 'ed', docSpeciality);
	} else {
		if (!empty(ed_condDocAutoComp)) {
				ed_condDocAutoComp.destroy();
				ed_condDocAutoComp = null;
		}
		ed_condDocAutoComp = initDoctorAutoComplete('ed_cond_doctor', 'ed_condDoctorContainer', 'ed', 'conducting');
	}
	if (ed_condDocAutoComp._elTextbox.value != '') {
		ed_condDocAutoComp._bItemSelected = true;
		ed_condDocAutoComp._sInitInputValue = ed_condDocAutoComp._elTextbox.value;
	}
	if (ed_plnDocAutoComp._elTextbox.value != '') {
		ed_plnDocAutoComp._bItemSelected = true;
		ed_plnDocAutoComp._sInitInputValue = ed_plnDocAutoComp._elTextbox.value;
	}
	document.getElementById('ed_comments').focus();

	return false;
}

function editTrtmtTableRow() {
	var id = document.consultationForm.editTrtmtRowId.value;
	var row = getChargeRow(id, 'trtmtDetails');

	var treatment_status = document.getElementById('ed_treatment_status').value;
	if (treatment_status == 'X' && document.getElementById('ed_service_prescribed_id').value != '') {
		alert("Service is already billed. You can not cancel it.");
		return false;
	}
	var finalTaskStatus = getFinalTaskStatus(id);
	if (treatment_status == 'C' && (finalTaskStatus == 'Partial' || finalTaskStatus == 'Not Completed')) {
		alert('You cannot mark the status as completed until you finish all the sub tasks.');
		return false;
	}
	var centerId = gMax_centers_inc_default > 1 ? document.getElementById('ed_center_id').value : '0';
	var tooth_number = document.getElementById('ed_tooth_number').value;
	var planned_by = document.getElementById('ed_planned_by').value;
   	var planned_date = document.getElementById('ed_planned_date').value;
   	var planned_time = document.getElementById('ed_planned_time').value;
   	var completed_date = document.getElementById('ed_completed_date').value;
   	var completed_time = document.getElementById('ed_completed_time').value;
   	var completed_by = document.getElementById('ed_completed_by').value;
   	var comments = document.getElementById('ed_comments').value;
	var planned_by_name = document.getElementById('ed_pln_doctor').value;
	var completed_by_name = document.getElementById('ed_cond_doctor').value;
	var tooth_num_required = document.getElementById('ed_tooth_num_required').value;
	var docSpeciality = document.getElementById('ed_doc_speciality').value;
	var creation_date = document.getElementById('ed_creation_date').value;
	var creation_time = document.getElementById('ed_creation_time').value;
	var start_date = document.getElementById('ed_start_date').value;
	var start_time = document.getElementById('ed_start_time').value;
	var planned_qty = document.getElementById('ed_planned_qty').value;
	var charge = document.getElementById('ed_service_charge').value;
	var discount = document.getElementById('ed_service_discount').value;
	var newDen = 'newDen';

	if (tooth_number == '' && tooth_num_required == 'Y') {
		document.getElementById('ed_tooth_number').focus();
		alert("This Service required Tooth Number.");
		return false;
	}
	if (planned_qty == '' || planned_qty == 0 || !isInteger(planned_qty)) {
		alert('Planned Qty should be a whole number and should be greater than zero');
		document.getElementById('ed_planned_qty').focus();
		return false;
	}
	var suppliesListTable = document.getElementById('editDentalSuppliestabel');
	if(suppliesListTable.rows.length-1 > 0) {
		if(!validateDentalSupplies(suppliesListTable, 'ed'))
			return false;
	}
	if (!validateDueDateTime('ed_planned_date', 'ed_planned_time', 'Planned Date', 'Planned Time', null, false))
		return false;
	if (!validateDueDateTime('ed_completed_date', 'ed_completed_time', 'Completed Date', 'Completed Time', null, true))
		return false;
	if (!validateDueDateTime('ed_creation_date', 'ed_creation_time', 'Creation Date', 'Creation Time', null, false))
		return false;
	if (!validateDueDateTime('ed_start_date', 'ed_start_time', 'Start Date', 'Start Time', null, true))
		return false;

	setNodeText(row.cells[TOOTH_NUMBER], tooth_number, 10);
   	setNodeText(row.cells[TREATMENT_STATUS],
   		document.getElementById('ed_treatment_status').options[document.getElementById('ed_treatment_status').selectedIndex].text);
   	setNodeText(row.cells[PLANNED_BY], planned_by_name, 10);
 	var plannedDateTime = planned_date != '' ? (planned_date + ' ' + planned_time) : '';
   	setNodeText(row.cells[PLANNED_DATE], plannedDateTime);
   	var startDateTime = start_date != '' ? (start_date + ' ' + start_time) : '';
   	setNodeText(row.cells[START_DATE], startDateTime);
   	setNodeText(row.cells[COMPLETED_BY], completed_by_name, 10);
   	var completedDateTime = completed_date != '' ? (completed_date + ' ' + completed_time) : '';
   	setNodeText(row.cells[COMPLETED_DATE], completedDateTime);
   	setNodeText(row.cells[RATE], formatAmountValue(charge));
   	setNodeText(row.cells[PLANNED_QTY], planned_qty);
   	setNodeText(row.cells[DISCOUNT], formatAmountValue(discount*1));
   	setNodeText(row.cells[AMOUNT], formatAmountValue((charge-discount)*1));
   	setNodeText(row.cells[COMMENTS], comments, 10);

	creaHiddValForDenSupplies(row, suppliesListTable, 'ed', centerId);
	if (tooth_numbering_system == 'U')
   		setHiddenValue(id, "h_tooth_unv_number", tooth_number);
   	else
   		setHiddenValue(id, "h_tooth_fdi_number", tooth_number);
   	setHiddenValue(id, "h_treatment_status", treatment_status);
   	setHiddenValue(id, "h_planned_by", planned_by);
   	setHiddenValue(id, "h_planned_date", plannedDateTime);
   	setHiddenValue(id, "h_start_date", startDateTime);
   	setHiddenValue(id, "h_completed_by", completed_by);
   	setHiddenValue(id, "h_completed_date", completedDateTime);
   	setHiddenValue(id, "h_comments", comments);
   	setHiddenValue(id, "h_creation_date", creation_date + ' ' + creation_time);
   	setHiddenValue(id, "h_planned_by_name", planned_by_name);
   	setHiddenValue(id, "h_completed_by_name", completed_by_name);
   	setHiddenValue(id, "h_qty", 1);
   	setHiddenValue(id, "h_service_charge", charge);
   	setHiddenValue(id, "h_service_discount", discount);
   	setHiddenValue(id, "h_planned_qty", planned_qty);
   	setHiddenValue(id, "h_doc_speciality_id", docSpeciality);
   	setHiddenValue(id, "h_order_index", id);
   	setHiddenValue(id, "h_den_supplies_flag", newDen);

   	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("ht_edited", id, 'true');
	document.getElementById('editDentalSuppliesDialog').style.display = 'none';
   	editTrtmtItemDialog.cancel();
   	enableDisableDisOrderBtn();
   	enableDisableSave('', false);
	return true;
}

function openPreviousTrtmt() {
	var id = document.consultationForm.editTrtmtRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'trtmtDetails');

	if (trtFieldEdited) {
		if (!editTrtmtTableRow()) return false;
		trtFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		var row = getThisRow(document.getElementsByName('_trtmtEditAnchor')[parseInt(id)-1]);
		while(row && row.style.display == 'none') {
			// ignore if the row is hidden.
			id = id-1;
			row = getThisRow(document.getElementsByName('_trtmtEditAnchor')[parseInt(id)]);
		}
		if (row)
			showEditTrtmtItemDialog(getElementByName(row, '_trtmtEditAnchor'));
	}
}

function openNextTrtmt() {
	var id = document.consultationForm.editTrtmtRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'trtmtDetails');

	if (trtFieldEdited) {
		if (!editTrtmtTableRow()) return false;
		trtFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('trtmtDetails').rows.length-2) {
		var row = getThisRow(document.getElementsByName('_trtmtEditAnchor')[parseInt(id)+1]);
		while(row && row.style.display == 'none') {
			// ignore if the row is hidden.
			id = id+1;
			row = getThisRow(document.getElementsByName('_trtmtEditAnchor')[parseInt(id)]);
		}
		if (row)
			showEditTrtmtItemDialog(getElementByName(row, "_trtmtEditAnchor"));
	}
}

function cancelTrtmtItem(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var oldDeleted =  getIndexedValue("ht_delete", id);

	var isNew = getIndexedValue("h_treatment_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;
		deleteSubTasks(id);
	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("ht_delete", id, newDeleted);
		setIndexedValue("ht_edited", id, "true");

		var trashImgs = row.cells[TR_TRASH_COL].getElementsByTagName("img");

		var trashSrc;
		if (newDeleted == 'true') {
			trashSrc = cpath + '/icons/undo_delete.gif';
		} else {
			trashSrc = cpath + '/icons/delete.gif';
		}
		row.className = 'edited';


		if (trashImgs && trashImgs[0])
			trashImgs[0].src = trashSrc;
		enableDisableDisOrderBtn();
	}
	return false;
}

function validateSuppliesItemForTreatment() {
}

function setGranularUnit(event, prefix) {
	var itemFormId = document.getElementById(prefix + '_item_form_id').value;
	var granularUnitForItem = filterList(itemFormList, "item_form_id", itemFormId);
	var granular_unit = '';
	if (granularUnitForItem.length > 0) {
		for (var k=0; k <granularUnitForItem.length; k++) {
			granular_unit = granularUnitForItem[k].granular_units;
			break;
		}
	}
	if (!empty(granular_unit)) {
		document.getElementById(prefix + '_granular_units').value = granular_unit;
		document.getElementById(prefix + '_qty').value = '';
		if (granular_unit == 'Y')
			calcQty(prefix);
		else
			document.getElementById(prefix + '_qty').value = 1;
	}
}

function filterResults(obj) {
	if (obj.name == 'flt_duration') {
		var fromDate = null;
		if (obj.value == 3) {
			fromDate = new Date();
			fromDate.setDate(fromDate.getDate()-365/4);
		} else if (obj.value == 6) {
			fromDate = new Date();
			fromDate.setDate(fromDate.getDate()-365/2);
		} else if (obj.value == 12) {
			fromDate = new Date();
			fromDate.setDate(fromDate.getDate()-365);
		}
		if (obj.value == '') {
			document.consultationForm.flt_from_date.value  = '';
			document.consultationForm.flt_to_date.value = '';
		} else {
			document.consultationForm.flt_from_date.value  = formatDate(fromDate, 'ddmmyyyy', '-');
			document.consultationForm.flt_to_date.value = '';
		}
	}
	var flt_treatment_status = document.consultationForm.flt_treatment_status.value;
	var flt_tooth_number = document.consultationForm.flt_tooth_number.value;
	var flt_from_date = document.consultationForm.flt_from_date.value;
	var flt_to_date = document.consultationForm.flt_to_date.value;

	if (flt_from_date == '' && flt_to_date != '') {
		alert("Please enter the from date.");
		document.consultationForm.flt_from_date.focus();
		return false;
	}
	if (document.consultationForm.flt_duration.value == '' && flt_from_date != '' && flt_to_date == '') {
		alert("Please enter the To date.");
		document.consultationForm.flt_to_date.focus();
		return false;
	}

	var els = document.getElementsByName('h_tooth_unv_number')
	for (var i=0; i<els.length-1; i++) {
		var planned_date = document.getElementsByName('h_planned_date')[i].value;
		var tooth_number =  tooth_numbering_system == 'U' ? document.getElementsByName('h_tooth_unv_number')[i].value :
			document.getElementsByName('h_tooth_fdi_number')[i].value;
		var treatment_status = document.getElementsByName('h_treatment_status')[i].value;

		if ((flt_treatment_status != '' && treatment_status != flt_treatment_status)) {
			getThisRow(document.getElementsByName('h_tooth_unv_number')[i]).style.display = 'none';
			continue;
		}

		if (flt_tooth_number != '') {
			if (flt_tooth_number == 'None' && tooth_number != '') {
				getThisRow(document.getElementsByName('h_tooth_unv_number')[i]).style.display = 'none';
				continue;
			} else if (flt_tooth_number != 'None' &&
				flt_tooth_number != '' &&
				tooth_number.split(",").indexOf(flt_tooth_number) == -1) {

				getThisRow(document.getElementsByName('h_tooth_unv_number')[i]).style.display = 'none';
				continue;
			}
		}

		if (flt_from_date != '') {
			var pdate = parseDateStr(planned_date);
			if ( !(pdate >= parseDateStr(flt_from_date) && (flt_to_date == '' || pdate <= parseDateStr(flt_to_date))) ) {
				getThisRow(document.getElementsByName('h_tooth_unv_number')[i]).style.display = 'none';
				continue;
			}
		}
		getThisRow(document.getElementsByName('h_tooth_unv_number')[i]).style.display = 'table-row';

	}
}

var serviceAutoComp = null;
function initServiceAutoComplete() {
	if (!empty(serviceAutoComp)) {
		serviceAutoComp.destroy();
		serviceAutoComp = null;
	}

	var orgId = document.getElementById('org_id').value;
	var bedType = document.getElementById('bed_type').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/DentalConsultation/Consultation.do');
	ds.scriptQueryAppend = "_method=getServices&org_id=" + orgId+"&bed_type="+bedType;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "service_name"},
					{key : "order_code"},
					{key : "service_id"},
					{key : "charge"},
					{key : "discount"},
					{key : "service_group_name"},
					{key : "tooth_num_required"},
					{key : "doc_speciality_id"}
				],
		numMatchFields: 2
	};

	serviceAutoComp = new YAHOO.widget.AutoComplete("d_serviceName", "serviceNameContainer", ds);
	serviceAutoComp.minQueryLength = 1;
	serviceAutoComp.animVert = false;
	serviceAutoComp.maxResultsDisplayed = 50;
	serviceAutoComp.resultTypeList = false;
	serviceAutoComp.forceSelection = true;

	serviceAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	serviceAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		highlightedValue += "(" +record.service_group_name+")";
		return highlightedValue;
	}

	serviceAutoComp.itemSelectEvent.subscribe(selectService);
	serviceAutoComp.selectionEnforceEvent.subscribe(clearServiceDetails);

	return serviceAutoComp;
}

function clearServiceDetails() {
	document.getElementById('d_service_id').value = '';
	document.getElementById('d_service_charge').value = '';
	document.getElementById('d_service_discount').value = '';
	document.getElementById('d_service_group_name').value = '';
	document.getElementById('d_doc_speciality').value = '';
	document.getElementById('d_cond_doctor').disabled = true;
}

function clearTrtmtDialogFields() {
	document.getElementById('d_serviceName').value = '';
	document.getElementById('d_tooth_number').value = '';
	document.getElementById('dToothNumberDiv').textContent = '';
	document.getElementById('d_service_id').value = '';
	document.getElementById('d_doc_speciality').value = '';
	document.getElementById('d_service_charge').value = '';
	document.getElementById('d_service_discount').value = '';
	document.getElementById('d_service_group_name').value = '';
	document.getElementById('d_treatment_status').value = 'P';
	document.getElementById('d_planned_date').value = formatDate(new Date());
	document.getElementById('d_planned_time').value = formatTime(new Date());
	document.getElementById('d_creation_date').value = formatDate(new Date());
	document.getElementById('d_creation_time').value = formatTime(new Date());
	document.getElementById('d_start_date').value = '';
	document.getElementById('d_start_time').value = '';
	document.getElementById('d_planned_by').value = '';
	document.getElementById('d_completed_date').value = '';
	document.getElementById('d_completed_time').value = '';
	document.getElementById('d_completed_by').value = '';
	document.getElementById('d_comments').value='';
	document.getElementById('d_tooth_num_required').value = '';
	document.getElementById('d_planned_qty').value = 1;
	document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';
	document.getElementById('dToothNumBtnDiv').style.display = 'none';
	document.getElementById('d_pln_doctor').value = ''
	document.getElementById('d_cond_doctor').value = '';
	document.getElementById('d_cond_doctor').disabled = true;
	var suppliesListTable = document.getElementById('addDentalSuppliestabel');
	if(suppliesListTable.rows.length > 0) {
		for(var i=suppliesListTable.rows.length-1;i>0;i--) {
			suppliesListTable.deleteRow(i);
		}
	}
	document.getElementById('addDentalSuppliesDialog').style.display = 'none';
}


function selectService(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_service_id').value = record.service_id;
	document.getElementById('d_service_charge').value = record.charge;
	document.getElementById('d_service_discount').value = record.discount;
	document.getElementById('d_service_group_name').value = record.service_group_name;
	document.getElementById('d_tooth_num_required').value = record.tooth_num_required;
	document.getElementById('d_doc_speciality').value = record.doc_speciality_id != '' ? record.doc_speciality_id : '';
	if (record.tooth_num_required && record.tooth_num_required == 'Y') {
		document.getElementById('dToothNumBtnDiv').style.display = 'block';
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'none';
		var toothNumber = document.consultationForm.flt_tooth_number.value;
		if (toothNumber != '' && toothNumber != 'None') {
			document.getElementById('d_tooth_number').value = toothNumber;
			document.getElementById('dToothNumberDiv').textContent = toothNumber;
		}
		document.getElementById('d_planned_qty').disabled = true;
	} else {
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';
		document.getElementById('dToothNumBtnDiv').style.display = 'none';
		document.getElementById('d_planned_qty').disabled = false;
	}
	document.getElementById('d_pln_doctor').value = '';
	if (record.doc_speciality_id != null && !empty(record.doc_speciality_id)) {
		d_condDocAutoComp = initCondDoctorAutoComplete('d_cond_doctor', 'condDoctorContainer', 'd', record.doc_speciality_id);
	} else {
		if (!empty(d_condDocAutoComp)) {
			d_condDocAutoComp.destroy();
			d_condDocAutoComp = null;
		}
		d_condDocAutoComp = initDoctorAutoComplete('d_cond_doctor', 'condDoctorContainer', 'd', 'conducting');
	}
	document.getElementById('d_cond_doctor').disabled = false;
	getDentalSupplies(record.service_id, '');
	var suppliesListTable = document.getElementById("addDentalSuppliestabel");
	if (cachedDentalSuppliesServ[record.service_id].length > 0) {
		if (loadSuppliesTable(cachedDentalSuppliesServ[record.service_id], suppliesListTable, 'd') > 0)
			document.getElementById('addDentalSuppliesDialog').style.display = 'block';
	} else {
		if(suppliesListTable.rows.length > 0) {
			for(var i=suppliesListTable.rows.length-1;i>0;i--) {
				suppliesListTable.deleteRow(i);
			}
		}
		document.getElementById('addDentalSuppliesDialog').style.display = 'none';
	}
}

/*
 * Response handler for the ajax call to retrieve dental supplies details based on service_id or treatment_id.
 */
var cachedDentalSuppliesTreat = [];
var cachedDentalSuppliesServ = [];
function getDentalSupplies(serviceId, treatmentId) {
	var flag = empty(treatmentId) ? cachedDentalSuppliesServ[serviceId] == undefined : cachedDentalSuppliesTreat[treatmentId] == undefined;
	if (flag) {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/DentalConsultation/Consultation.do?_method=getDentalSupplies&service_id=' + serviceId + '&treatment_id=' + treatmentId;
		ajaxobj.open("POST", url, false);
		ajaxobj.send(null);

		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var dentalSuppliesJSON = " + ajaxobj.responseText);
					if (empty(treatmentId))
						cachedDentalSuppliesServ[serviceId] = dentalSuppliesJSON;
					else
						cachedDentalSuppliesTreat[treatmentId] = dentalSuppliesJSON;
				}
			}
		}
	}
}

function setItemRate(obj, itemId, prefix, id){
	if (obj.value == '') {
		document.getElementById(prefix+'_unit_rate'+id).value = '';
		document.getElementById(prefix+'_vat_perc'+id).value = '';
	} else {
		var record = findInList2(gItems, "supplier_id", obj.value, "item_id", itemId);
		document.getElementById(prefix+'_unit_rate'+id).value = record.unit_rate;
		document.getElementById(prefix+'_vat_perc'+id).value = record.vat_perc;
	}
}

function loadSuppliesTable(dentalSupplies, suppliesListTable, prefix) {
	if(suppliesListTable.rows.length > 0) {
		for(var i=suppliesListTable.rows.length-1;i>0;i--) {
			suppliesListTable.deleteRow(i);
		}
	}
	for(var i=0; i<dentalSupplies.length; i++) {
		var numRows = suppliesListTable.rows.length-1;
    	var id = numRows;
		var row = suppliesListTable.insertRow(id+1);

		var itemName = dentalSupplies[i].item_name;
		var itemId = dentalSupplies[i].item_id;
		var serviceId = dentalSupplies[i].service_id;
		var supplierId = empty(dentalSupplies[i].supplier_id) ? '' : dentalSupplies[i].supplier_id;
		var shadeId = empty(dentalSupplies[i].shade_id) ? '' : dentalSupplies[i].shade_id;
		var itemQty = empty(dentalSupplies[i].item_qty) ? '' : dentalSupplies[i].item_qty;
		var filteredSupplierList = null;
		if (prefix == 'd') {
			// we are trying to add new service get me only active supplier list.
			filteredSupplierList = filterList2(ItemSupplierList, 'item_id',itemId, "status" ,"A")
		} else {
			// get all the supplier details in edit dialog
			filteredSupplierList = filterList2(ItemSupplierList, 'item_id',itemId);
		}
		var cell;
		var fieldEditEvent = '';
		if (prefix == 'ed') {
			fieldEditEvent = 'trtmtFieldEdited();';
		}
		cell = row.insertCell(-1);
		cell.setAttribute("class","forminfo");
		cell.innerHTML = '<label style="width: 100%;" id="'+prefix+'_itemLabel'+id+'" title="'+itemName+'">'+truncateText(itemName,40)+'</label>' +
        				'<input type="hidden" name="'+prefix+'_item_id" id="'+prefix+'_item_id'+id+'" value="'+itemId+'"/>' +
        				'<input type="hidden" name="'+prefix+'_unit_rate" id="'+prefix+'_unit_rate'+id+'" value="" />' +
        				'<input type="hidden" name="'+prefix+'_vat_perc" id="'+prefix+'_vat_perc'+id+'" value="" />';

		cell = row.insertCell(-1);
		cell.setAttribute("align","center");
		cell.innerHTML = '<select style="width: 100%;" name="'+prefix+'_supplier_id" id="'+prefix+'_supplier_id'+id+'" class="dropdown" onchange="return '+fieldEditEvent+' setItemRate(this, '+itemId+',\''+prefix+'\','+id+');" ></select>';
		loadSelectBox(document.getElementById(prefix+'_supplier_id'+id), filteredSupplierList, 'supplier_name', 'supplier_id', '-- Select --', '');
		setSelectedIndex(document.getElementById(prefix+'_supplier_id'+id), supplierId);

		cell = row.insertCell(-1);
		cell.setAttribute("align","center");
		cell.innerHTML = '<select style="width: 100%;" name="'+prefix+'_shade_id" id="'+prefix+'_shade_id'+id+'" class="dropdown" onchange="'+fieldEditEvent+'"></select>';
		loadSelectBox(document.getElementById(prefix+'_shade_id'+id), shadesListJSON, 'shade_name', 'shade_id', '-- Select --', '');
		setSelectedIndex(document.getElementById(prefix+'_shade_id'+id), shadeId);

		cell = row.insertCell(-1);
		cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="num" style="width: 100%;"  name="'+prefix+'_item_qty" id="'+prefix+'_item_qty'+id+'" value="'+itemQty+'"  onchange="'+fieldEditEvent+'" onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="8">';
	}
	return dentalSupplies.length;
}

function loadEditSuppliesTable(editDentalSuppliesTable, row) {
	if(editDentalSuppliesTable.rows.length > 0) {
		for(var i=editDentalSuppliesTable.rows.length-1;i>0;i--) {
			editDentalSuppliesTable.deleteRow(i);
		}
	}
	var id = getRowChargeIndex(row);
	index = new Array(),
	itemName = new Array(),
	itemId = new Array(),
	serviceId = new Array(),
	supplierId = new Array(),
	shadeId = new Array(),
	itemQty = new Array();

	index = getElementsByName(row, 'h_'+id+'_item_id');
	itemName = getElementsByName(row, 'h_'+id+'_item_name');
	itemId = getElementsByName(row, 'h_'+id+'_item_id');
	serviceId = getElementsByName(row, 'h_'+id+'_service_id');
	supplierId = getElementsByName(row, 'h_'+id+'_supplier_id');
	shadeId = getElementsByName(row, 'h_'+id+'_shade_id');
	itemQty = getElementsByName(row, 'h_'+id+'_item_qty');

	for (var i=0; i<index.length; i++) {
		var numRows = editDentalSuppliesTable.rows.length-1;
    	var rId = numRows;
		var newRow = editDentalSuppliesTable.insertRow(rId+1);

		var filteredSupplierList = filterList(ItemSupplierList, 'item_id',itemId[i].value);

		var cell;

		cell = newRow.insertCell(-1);
		cell.setAttribute("class","forminfo");
		cell.innerHTML = '<label style="width: 100%;" id="ed_itemLabel'+rId+'" title="'+itemName[i].value+'">'+truncateText(itemName[i].value,40)+'</label>' +
        				'<input type="hidden" name="ed_item_id" id="ed_item_id'+rId+'" value="'+itemId[i].value+'"/>' +
        				'<input type="hidden" name="ed_unit_rate" id="ed_unit_rate'+rId+'" value="" />' +
        				'<input type="hidden" name="ed_vat_perc" id="ed_vat_perc'+rId+'" value="" />';

		cell = newRow.insertCell(-1);
		cell.setAttribute("align","center");
		cell.innerHTML = '<select style="width: 100%;" name="ed_supplier_id" id="ed_supplier_id'+rId+'" class="dropdown" onchange="trtmtFieldEdited()"></select>';
		loadSelectBox(document.getElementById('ed_supplier_id'+rId), filteredSupplierList, 'supplier_name', 'supplier_id', '-- Select --', '');
		setSelectedIndex(document.getElementById('ed_supplier_id'+rId), supplierId[i].value);

		cell = newRow.insertCell(-1);
		cell.setAttribute("align","center");
		cell.innerHTML = '<select style="width: 100%;" name="ed_shade_id" id="ed_shade_id'+rId+'" class="dropdown" onchange="trtmtFieldEdited()"></select>';
		loadSelectBox(document.getElementById('ed_shade_id'+rId), shadesListJSON, 'shade_name', 'shade_id', '-- Select --', '');
		setSelectedIndex(document.getElementById('ed_shade_id'+rId), shadeId[i].value);

		cell = newRow.insertCell(-1);
		cell.setAttribute("align","center");
		cell.innerHTML = '<input style="width: 100%;" type="text" class="num"   name="ed_item_qty" id="ed_item_qty'+rId+'" onchange="trtmtFieldEdited()" value="'+ itemQty[i].value +'"  onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="8">';
	}
	return index.length;
}

function orderServicesAndFinalizeBills() {
	var count = 0;
	var selected_services = document.getElementsByName('orderAndFinalizeCheck');
	for (var i=0; i<selected_services.length-1; i++) {
		if (!selected_services[i].disabled && selected_services[i].checked) {
			if(!setHiddenVariableForServices(i))
				return false;
			count++;
		} else {
			if(!removeHiddenVariableForServices(i))
				return false;
		}
	}
	if (count > 0) {
		document.consultationForm._method.value = 'orderServiceAndFinalizedBill';
		document.consultationForm.submit();
		return true;
	} else {
		alert("Please select atleast one treatment for order.");
		return false;
	}
}

function setHiddenVariableForServices(index) {
	var treatment_id_els = document.getElementsByName('h_treatment_id');
	var edited = document.getElementsByName('ht_edited');
	var deleted = document.getElementsByName('ht_delete');
	for (var i=0; i<treatment_id_els.length-1; i++) {
		var treatment_id = treatment_id_els[i].value;
		if (treatment_id == '_' || (
			treatment_id != '_' && (edited[i].value == 'true' || deleted[i].vallue == 'true'))) {
			alert("Treatment details added/edited/deleted, please save the changes before placing an order.");
			return false;
		}
	}

	var serviceId = getIndexedValue('h_service_id', index);
	var doctorId = getIndexedValue('h_planned_by', index);
	var teratmentId = getIndexedValue('h_treatment_id', index);
	var order_qty = getIndexedValue('h_qty', index);
	var discount = getIndexedValue('disc', index);

	var conducting_doc_mandatory = getIndexedValue('h_conducting_doc_mandatory', index);
	var conducted_by = getIndexedValue('h_completed_by', index);
    var tooth_number = empty(getIndexedValue('h_tooth_unv_number', index)) ? getIndexedValue('h_tooth_fdi_number', index) : getIndexedValue('h_tooth_unv_number', index);

	if (conducting_doc_mandatory == 'O' && conducted_by == '') {
		alert('Please enter the conducting doctor and save the "'+ getIndexedValue('h_service_name', index) +'" treatment details.');
		return false;
	}
	if (order_qty == '' || order_qty == 0 || !isInteger(order_qty)) {
		alert('Order Qty should be a whole number and should be greater than zero for "'+ getIndexedValue('h_service_name', index) +'" .');
		return false;
	}

	setIndexedValue("order_this_service", index, serviceId);
	setIndexedValue("planned_by_doctor", index, doctorId);
	setIndexedValue("order_treatment_id", index, teratmentId);
	setIndexedValue("order_qty", index, order_qty);
	setIndexedValue("tooth_number", index, tooth_number);
	setIndexedValue("conducting_doctor", index, conducted_by);
	setIndexedValue("overall_discount_auth_name", index, '');
	setIndexedValue("overall_discount_auth", index, 0);
	setIndexedValue("overall_discount_amt", index, discount);

	return true;
}

function removeHiddenVariableForServices(index) {
	setIndexedValue("order_this_service", index, '');
	setIndexedValue("planned_by_doctor", index, '');
	setIndexedValue("order_treatment_id", index, '');
	setIndexedValue("order_qty", index, '');
	setIndexedValue("tooth_number", index, '');
	setIndexedValue("conducting_doctor", index, '');
	setIndexedValue("overall_discount_auth_name", index, '');
	setIndexedValue("overall_discount_auth", index, 0);
	setIndexedValue("overall_discount_amt", index, 0);
	return true;
}

var billRow = '';
var billNumber;
function initiatePaymentForBill(obj, billNo) {
	billNumber=billNo;
	var row = getThisRow(obj);
	billRow = row;
	row.className = 'ordered';
	var id = getRowChargeIndex(row);
	var filteredBillDetailsList = filterList(openAndFinalizedUnPaidBillsList, 'bill_no',billNo);
	if (filteredBillDetailsList.length > 0) {
		toggleInitiatePaymentBts("true");
		var totalAmt,totalRec,totalCla,totalDis,totalDue = 0;
		var billType = '';
		var visitId = '';
		for (var k=0; k <filteredBillDetailsList.length; k++) {
			billType = filteredBillDetailsList[k].actual_bill_type;
			totalAmt = filteredBillDetailsList[k].total_amount;
			totalRec = filteredBillDetailsList[k].total_receipts;
			totalCla = filteredBillDetailsList[k].total_claim;
			totalDis = filteredBillDetailsList[k].total_discount;
			visitId = filteredBillDetailsList[k].visit_id;
		}

		setHiddenValueForPayment("billNo", billNo);
		setHiddenValueForPayment("billType", billType);
		setHiddenValueForPayment("totalAmt", totalAmt);
		setHiddenValueForPayment("totalRec", totalRec);
		setHiddenValueForPayment("totalCla", totalCla);
		setHiddenValueForPayment("totalDis", totalDis);
		setHiddenValueForPayment("totalDue", (totalAmt - totalRec));
		setHiddenValueForPayment("visitId", visitId);

		document.getElementById("paySec").textContent = "Payments For Bill " + billNo;
		document.getElementById("addPaymentsDiv").style.display = 'block';
		var billType = document.getElementById("billType").value;
		document.getElementById("payDD").className = 'open';
		var recLink = document.getElementById("recLink");
		var reclabel = "Patient Payments";
		var url = cpath + "/billing/ReceiptList.do?_method=getReceipts&bill_no="+ billNo +"&payment_type=R&payment_type=F";
		recLink.innerHTML += '| <a href="'+ url +'">'+ reclabel + '</a>';
	    var paymentSelValue = '';
	    if ((totalAmt - totalRec - totalCla) > 0)
	    	 paymentSelValue = 'A';
   	    if ((totalAmt - totalRec - totalCla) == 0)
	    	 paymentSelValue = 'R';

		populatePaymentType(billType, paymentSelValue);
		resetTotals();
	}
}

function resetTotals() {
    resetPayments();
}

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 */

function resetPayments() {
    resetTotalsForPayments();
}

function getTotalAmount() {
    return getPaise(document.getElementById("totalAmt").value);
}

function getTotalAmountDue() {
    return getPaise(document.getElementById("totalDue").value);
}


function canclePayment() {
	toggleInitiatePaymentBts();
	document.getElementById("addPaymentsDiv").style.display = 'none';
	document.getElementById("payDD").className = '';
	setHiddenValueForPayment("billNo", "");
	setHiddenValueForPayment("billType", "");
	setHiddenValueForPayment("totalAmt", "");
	setHiddenValueForPayment("totalRec", "");
	setHiddenValueForPayment("totalCla", "");
	setHiddenValueForPayment("totalDis", "");
	setHiddenValueForPayment("totalDue", "");
	billRow.className = '';
}

// this functin is created for this screen only
function setHiddenValueForPayment(name, value) {
	var el = document.getElementById(name);
	if (el) {
		if (value == null || value == undefined)
				value = "";
		el.value = value;
	}
}

function toggleInitiatePaymentBts(enable) {
	enable = empty(enable) ? false : enable;
	var initiatePaymentBts = document.getElementsByName("initiate_payment");
	for (var i=0; i<initiatePaymentBts.length; i++) {
		initiatePaymentBts[i].disabled = enable;
	}
}

function payAndPrintDetails() {
	document.consultationForm._method.value = 'payAndPrintBill';
	var treatment_id_els = document.getElementsByName('h_treatment_id');
	var edited = document.getElementsByName('ht_edited');
	var deleted = document.getElementsByName('ht_delete');
	for (var i=0; i<treatment_id_els.length-1; i++) {
		var treatment_id = treatment_id_els[i].value;
		if (treatment_id == '_' || (
			treatment_id != '_' && (edited[i].value == 'true' || deleted[i].vallue == 'true'))) {
			alert("Treatment details added/edited/deleted, please save the changes before doing payments.");
			return false;
		}
	}
	var isValid = validateSave();
	if (isValid) {
		document.consultationForm.submit();
	}
}

function validateSave() {
	var valid = true;
	var status = true;
	//valid = valid && validateDRGCode();
	//valid = valid && validatePerdiemCodeChange();
	valid = valid && validateAllNumerics();
	// valid = valid && validateBillRemarks();
	valid = valid && validateCounter();
	valid = valid && validatePaymentRefund();
	valid = valid && validatePaymentTagFields();
	//valid = valid && validateClaimStatus();
	//valid = valid && validateBillDeduction();
	//valid = valid && validatePatientPayment();
	//valid = valid && validateSponsorPayment();
	//valid = valid && validateSponsorBillClose();
	//valid = valid && validateCancel();
	valid = valid && validatePayDates();
	//valid = valid && validateDiscountAuth();
	valid = valid && validatePaymentType();
	//valid = valid && validateDischaregDateTime();
	//valid = valid && validatePrimaryClaimAmt();
	//valid = valid && validateSecondaryClaimAmt();
	//valid = valid && validateTotalClaimAmt();
	//valid = valid && validateFinalizedDateAndTime();
	//valid = valid && checkConductingDoctor();
	//valid = valid && checkFinalization();
	//valid = valid && validateDynaPkg();
	//valid = valid && validateInsuranceApprovalAmount();
	//valid = valid && validateBillRewardPointsRedeemed();
	valid = valid && doPaytmTransactions();
	 if (income_tax_cash_limit_applicability == 'Y'){
		 var mrno=document.getElementById("mr_no").value;
		 var visitId=document.getElementById("visitId").value;
		valid = valid && checkCashLimitValidation(mrno,visitId);
	}
	return valid;
}

function validatePaymentType() {
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(document.consultationForm, "paymentType", i);
			var amtObj = getIndexedFormElement(document.consultationForm, "totPayingAmt", i);

			if (empty(amtObj.value)) {
				alert("Enter payment amount");
				amtObj.focus();
				return false;
			}

			var type = paymentObj.value;
			if ( (null != amtObj) && (amtObj.value != "") ) {
				if (type == '') {
					alert("Select payment type");
					paymentObj.focus();
					return false;
				}
			}
		}
	}
	return true;
}

// discount related functions

function enableDisableDisOrderBtn() {
	if (!empty(discPart) && discPart == 'A') {
		var treatment_id_els = document.getElementsByName('h_treatment_id');
		var edited = document.getElementsByName('ht_edited');
		var deleted = document.getElementsByName('ht_delete');
		for (var i=0; i<treatment_id_els.length-1; i++) {
			var treatment_id = treatment_id_els[i].value;
			if (treatment_id == '_' || (
				treatment_id != '_' && (edited[i].value == 'true' || deleted[i].vallue == 'true'))) {
				document.consultationForm.itemDiscPer.disabled = true;
				document.consultationForm.itemDiscPerApply.disabled = true;
				document.consultationForm.orderAndFinalizeBills.disabled = true;
				document.consultationForm.itemDiscType.disabled = true;
			}
		}
	}
}

function enableDisableSave(obj, disabled) {
	var	flag = true;
	var disabled = disabled;
	var treatment_id_els = document.getElementsByName('h_treatment_id');
	var edited = document.getElementsByName('ht_edited');
	var deleted = document.getElementsByName('ht_delete');
	for (var i=0; i<treatment_id_els.length-1; i++) {
		var treatment_id = treatment_id_els[i].value;
		if (treatment_id == '_' || (
			treatment_id != '_' && (edited[i].value == 'true' || deleted[i].vallue == 'true'))) {
				flag = false;
				disabled = false;
		}
	}
	var selected_services = document.getElementsByName('orderAndFinalizeCheck');
	if (flag) {
		for (var i=0; i<selected_services.length-1; i++) {
			if (!selected_services[i].disabled && selected_services[i].checked) {
				flag = false;
				disabled = true;
			}
		}
	}
	if (flag && !empty(obj)) {
		if(obj.checked) disabled = true;
		else disabled = false;
	}
	document.consultationForm.save.disabled = disabled;
	document.consultationForm.saveAndPrint.disabled = disabled;
}

function validateDiscPer() {
	var discType = document.consultationForm.itemDiscType.value;

	var errorMsg = "Discount percent must be a valid number";
	if (discType == 'R') {
		errorMsg = "Discount Amount must be a valid number";
	}
	if (!validateDecimal(document.consultationForm.itemDiscPer, errorMsg, 2))
		return false;
	return true;
}


function validateDiscountAuth() {
	for (var i=0; i<getNumCharges('trtmtDetails'); i++) {
		var disCheck = getIndexedFormElement(document.consultationForm, "orderAndFinalizeCheck",i);
		var discObj = getIndexedFormElement(mainform, "disc", i);
		if (disCheck.checked) {
			if (getPaise(discObj.value) > 0) {
				 if (consultationForm.discountAuthName && '' == trimAll (consultationForm.discountAuthName.value)) {
					alert("Select discount authorizer for discounts");
					document.consultationForm.discountAuthName.focus();
					return false;
				}
			}
		}
	}
	return true;
}
function validateUserPermissibleDiscount() {
	var itemDiscType = document.consultationForm.itemDiscType.value;
	var itemDiscPer = document.consultationForm.itemDiscPer.value;
	if (itemDiscType == 'P') {
		if(getAmount(itemDiscPer) > userPermissibleDiscount) {
			return true;
		}
	}else {
		for (var i=0;i<getNumCharges('trtmtDetails');i++) {
			var disCheck = getIndexedFormElement(document.consultationForm, "orderAndFinalizeCheck",i);
			if (disCheck.checked) {
				var rateObject = getIndexedFormElement(consultationForm, "rate",i);
				if (rateObject.value == "") { rateObject.value = 0; }
				var quantityObj = getIndexedFormElement(consultationForm, "qty",i);
				var itemRatePaise = getPaise(rateObject.value);
				var quantity = getAmount(quantityObj.value);
				var givenDiscPaise = quantity * getPaise(itemDiscPer);
				var permissibleDiscPaise = itemRatePaise*quantity*userPermissibleDiscount/100;
				if(givenDiscPaise > permissibleDiscPaise) {
					return true;
				}
			}
		}
	}
	return false;
}
function onApplyItemDiscPer() {
	var itemDiscType = document.consultationForm.itemDiscType.value;

	if (!validateDiscPer())
		return false;

	if (!selectedItemsForDiscount()) {
		alert("Select any charge to apply discount.");
		return false;
	}
	if (validateUserPermissibleDiscount()) {
		alert("Not able to apply the discount given, Please ensure that discount doesn't exceeds your permissible limit of "
				+ userPermissibleDiscount + "% for all items selected.");
		return false;
	}

	for (var i=0;i<getNumCharges('trtmtDetails');i++) {
		var disCheck = getIndexedFormElement(document.consultationForm, "orderAndFinalizeCheck",i);
		var discObj = getIndexedFormElement(document.consultationForm, "disc", i);
		var qty, ratePaise;

		if (disCheck.checked) {

			if (getPaise(discObj.value) > 0 ) {
				setIndexedValue("overall_discount_auth_name", i, '');
				setIndexedValue("overall_discount_auth", i, '0');
				setIndexedValue("overall_discount_amt", i, '');
			}

			rateObj = getIndexedFormElement(consultationForm, "rate",i);
			qtyObj = getIndexedFormElement(consultationForm, "qty",i);
			serviceNameObj = getIndexedFormElement(consultationForm, "h_service_name",i);

			if (rateObj.value == "") { rateObj.value = 0; }
			if (qtyObj.value == "") {
 				alert("Order Qty required to apply Discount on " + serviceNameObj.value);
 				//enableDisableSave('', false);
 				return;
 			}

			ratePaise = getPaise(rateObj.value);
			qty = getAmount(qtyObj.value);

			var discPer = 0;
			if (itemDiscType == 'P') {
				discPer = getAmount(document.consultationForm.itemDiscPer.value);
				if (discPer > 100) {
					alert("Discount cannot be greater than 100%");
					return;
				}
			} else {
				discPer = getPaise(document.consultationForm.itemDiscPer.value);
			}

			var discPaise = 0;
			if (itemDiscType == 'R') {
				// discount in rupees
				discPaise = qty * discPer;
				if (discPaise > (ratePaise * qty)) {
					discPaise = ratePaise * qty;
				}
			} else {
				discPaise = ((ratePaise * qty) * discPer) / 100;
			}
			if (discPaise > 0) {
				 if (consultationForm.discountAuthName && '' == trimAll (consultationForm.discountAuthName.value)) {
					alert("Select discount authorizer for discounts");
					document.consultationForm.discountAuthName.focus();
					return false;
				}
			}

			document.getElementById('discAuth').value =  consultationForm.discountAuthName.value;
			setNodeText(getChargeRow(i, 'trtmtDetails').cells[DISCOUNT], formatAmountPaise(discPaise));
			discObj.value = formatAmountPaise(discPaise);
			//setIndexedValue("overall_discount_amt", i, formatAmountPaise(discPaise));

			var changedRate = getPaise(rateObj.value);
			var changedQty = getAmount(qtyObj.value);
			var changedDisc = getPaise(discObj.value);

			var newAmtPaise = ratePaise*qty - discPaise;

			setIndexedValue("amt", i, formatAmountPaise(newAmtPaise));
			setNodeText(getChargeRow(i, 'trtmtDetails').cells[AMOUNT], formatAmountPaise(newAmtPaise));

		}
	}
}

function selectedItemsForDiscount() {
	for (var i=0;i<getNumCharges('trtmtDetails');i++) {
		if (getIndexedFormElement(consultationForm, "orderAndFinalizeCheck",i).checked)
		return true;
	}
	return false;
}

function initItemDialog() {
	var dialogDiv = document.getElementById("addItemDialog");
	dialogDiv.style.display = 'block';
	addItemDialog = new YAHOO.widget.Dialog("addItemDialog",
			{	width:"650px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add', 'click', addToTable, addItemDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleAddItemCancel, addItemDialog, true);
	var enterKeyListener = new YAHOO.util.KeyListener("addItemDialogFields", { keys:13 },
				{ fn:onEnterKeyItemDialog, scope:addItemDialog, correctScope:true } );
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddItemCancel,
	                                                scope:addItemDialog,
	                                                correctScope:true } );
	addItemDialog.cfg.setProperty("keylisteners", [escKeyListener, enterKeyListener]);
	addItemDialog.render();
}

function onEnterKeyItemDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new autocomplete.)
	document.getElementById("d_itemName").blur();
	addToTable();
}

function handleAddItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		this.cancel();
	}
}


function getItemType() {
	var itemTypeObj = document.getElementsByName('d_itemType');
	for (var i=0; i<itemTypeObj.length; i++) {
		if (itemTypeObj[i].checked)
			return itemTypeObj[i].value;
	}
	return null;
}


var parentDialog = null;
var childDialog = null;
function showAddItemDialog(obj) {
	var row = getThisRow(obj);

	addItemDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addItemDialog.show();

	var itemType = getItemType();
	toggleItemFormRow(true);
	initItemAutoComplete();
	clearPrescriptionFields();
	document.getElementById('d_itemName').focus();
	parentDialog = addItemDialog;
	return false;
}

function onItemChange(){
	clearPrescriptionFields();
	initItemAutoComplete();
	toggleItemFormRow(true);
	var itemType = getItemType();
	if (itemType == "All") {

	} else if (itemType == "Medicine" || itemType == 'NonHospital') {
		//document.getElementById('dGenericNameRow').style.display = 'table-row';
		document.getElementById('d_strength').disabled = false;
		document.getElementById('d_admin_strength').disabled = false;
		document.getElementById('d_frequency').disabled = false;
		document.getElementById('d_duration').disabled = false;
		document.getElementById('d_qty').disabled = false;
		document.getElementById('d_remarks').disabled = false;
		document.getElementById('d_medicine_route').disabled = false;

	} else {
		if (itemType == "Instructions") {
		 	document.getElementById('d_remarks').disabled = true;
		} else {
			document.getElementById('d_remarks').disabled = false;
		}
		document.getElementById('d_strength').disabled = true;
		document.getElementById('d_admin_strength').disabled = true;
		document.getElementById('d_frequency').disabled = true;
		document.getElementById('d_duration').disabled = true;
		document.getElementById('d_qty').disabled = true;
		document.getElementById('d_medicine_route').disabled = true;

		//document.getElementById('dGenericNameRow').style.display = 'none';
		document.getElementById('genericNameAnchor_dialog').innerHTML = '';
 		document.getElementById('genericNameAnchor_dialog').style.display = 'none';
   		document.getElementById('genericNameAnchor_dialog').href = '';
	}

}

function initItemAutoComplete() {
	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	var itemType = getItemType();
	if (itemType == 'Instructions' || itemType == 'NonHospital') return null; // for doctor instrctions no need to create the autocomplete.

	var orgId = document.getElementById('org_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeAction.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId + "&center_id=" + latestVisitCenterId + "&non_dental_services=true";
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "generic_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "route_of_admin"},
					{key : "cons_uom_id"},
					{key : "consumption_uom"},
					{key : 'item_form_id'},
					{key : 'item_strength'},
					{key : 'item_strength_units'},
					{key : 'granular_units'}
				 ],
		numMatchFields: 2
	};

	itemAutoComp = new YAHOO.widget.AutoComplete("d_itemName", "itemContainer", ds);
	itemAutoComp.minQueryLength = 1;
	itemAutoComp.animVert = false;
	itemAutoComp.maxResultsDisplayed = 50;
	itemAutoComp.resultTypeList = false;
	var forceSelection = true;
	if (itemType == 'Medicine' && use_store_items != 'Y')
		forceSelection = false;
	itemAutoComp.forceSelection = forceSelection;

	itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			// show qty only for pharmacy items.
			if (use_store_items == 'Y' && prescriptions_by_generics == 'N')
				highlightedValue += "(" + record.qty + ") ";
			// show generic name along with the medicine name when prescriptions done by brand names.
			if (!useGenerics)
				highlightedValue += (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
		}
		return highlightedValue;
	}

	itemAutoComp.dataRequestEvent.subscribe(clearItemDetails);
	if (forceSelection) {
		itemAutoComp.itemSelectEvent.subscribe(selectItem);
		itemAutoComp.selectionEnforceEvent.subscribe(clearItemDetails);
	} else {
		itemAutoComp.itemSelectEvent.subscribe(selectItem);
	}


	return itemAutoComp;
}

function toggleItemFormRow(addDialog) {
	var prefix = addDialog ? 'd_' : 'ed_';
	var itemType = addDialog ? getItemType() : document.getElementById(prefix + 'itemType').value;
	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById(prefix + 'itemFormRow').style.display = 'table-row';
		// allow user to select the medicine form if it is a prescription by generics.
		if (itemType == 'NonHospital' || useGenerics) {
			document.getElementById(prefix + 'item_form_id').disabled = false;
			document.getElementById(prefix + 'item_strength').disabled = false;
			document.getElementById(prefix + 'item_strength_units').disabled = false;
		} else {
			document.getElementById(prefix + 'item_form_id').disabled = true;
			document.getElementById(prefix + 'item_strength').disabled = true;
			document.getElementById(prefix + 'item_strength_units').disabled = true;
		}
	} else {
		document.getElementById(prefix + 'itemFormRow').style.display = 'none';
	}
}

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_item_id').value = record.item_id;
	if (record.item_type == 'Medicine') {
		document.getElementById('d_qty_in_stock').value = record.qty;
		if (!empty(record.generic_name)) {
			document.getElementById('genericNameAnchor_dialog').style.display = 'block';
			document.getElementById('genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "", "dialog", "'+record.generic_code+'")';
			document.getElementById('genericNameAnchor_dialog').innerHTML = record.generic_name;
			document.getElementById('d_generic_code').value = record.generic_code;
			document.getElementById('d_generic_name').value = record.generic_name;
		}
	}

	if (record.master == 'item_master') {
		document.getElementById('d_itemMasterType').textContent = 'Yes';
	} else {
		document.getElementById('d_itemMasterType').textContent = 'No';
	}
	document.getElementById('d_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_consumption_uom_label').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_item_master').value = record.master;
	document.getElementById('d_item_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('d_item_strength').value = record.item_strength;
	document.getElementById('d_item_strength_units').value = record.item_strength_units;
	document.getElementById('d_item_strength_units').selectedIndex=document.getElementById('d_item_strength_units').selectedIndex == -1 ? 0:
		document.getElementById('d_item_strength_units').selectedIndex;
	document.getElementById('d_ispackage').value = record.ispkg;
	document.getElementById('d_granular_units').value = record.granular_units;
	if (record.granular_units != 'Y') {
		document.getElementById('d_qty').value = 1;
	}

	getItemRateDetails();
}

var ajaxRequest = null;
var ajaxInProgress = false;
function getItemRateDetails() {
	var itemMaster = document.getElementById('d_item_master').value;
	var itemType = getItemType();
	var ispackage = document.getElementById('d_ispackage').value;
	if (itemType == 'Doctor' || itemType == 'Instructions'
		|| itemType == 'NonHospital'
		|| (itemType == 'Medicine' && useGenerics)) {
		document.getElementById('d_package_size').value = '';
		document.getElementById('d_price').value = '';
		document.getElementById('d_pkg_size_label').textContent = '';
		document.getElementById('d_price_label').textContent = '';

	} else {

		var orgId = document.getElementById('org_id').value;
		var itemId = document.getElementById('d_item_id').value;
		var itemName = document.getElementById('d_itemName').value;
		var bedType = document.getElementById('bed_type').value;
		var url = cpath+'/outpatient/OpPrescribeAction.do?_method=getItemRateDetails';
		url += '&item_type='+itemType;
		url += '&org_id='+orgId;
		url += '&item_id='+itemId;
		url += '&item_name='+encodeURIComponent(itemName);
		url += '&is_package='+ispackage;
		url += '&bed_type='+bedType;
		ajaxRequest = YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: onGetCharge,
					failure: onGetChargeFailure,
					argument: [itemType, ispackage, itemMaster]}
		)
		ajaxInProgress = true;
	}
}

function onGetCharge(response) {
	if (response.responseText != undefined) {
		var itemType = response.argument[0];
		var rateDetails = eval('(' + response.responseText + ')');
		if (rateDetails == null) {
			document.getElementById('d_price').value = '';
			document.getElementById('d_package_size').value= '';
			document.getElementById('d_pkg_size_label').textContent = '';
			document.getElementById('d_price_label').textContent = '';
			document.getElementById('d_medicine_route').length = 1;
			ajaxInProgress = false;
			return;
		}
		var packageSize = '';
		var price = 0;
		var discount = 0;
		if (itemType == 'Medicine') {
			packageSize = empty(rateDetails.issue_base_unit) ? '' : rateDetails.issue_base_unit;
			price = empty(rateDetails.mrp) ? '' : rateDetails.mrp;
			if (showRateDetails) {
				document.getElementById('d_price').value = price;
				document.getElementById('d_price_label').textContent = price;
			} else {
				document.getElementById('d_price').value = '';
				document.getElementById('d_price_label').textContent = '';
			}

			document.getElementById('d_package_size').value = packageSize;
			document.getElementById('d_pkg_size_label').textContent = packageSize;
			var routeIds = rateDetails.route_id.split(",");
			var routeNames = rateDetails.route_name.split(",");
			var medicine_route_el = document.getElementById('d_medicine_route');
			medicine_route_el.length = 1; // clear the previously populated list
			var len = 1;
			for (var i=0; i<routeIds.length; i++) {
				if (routeIds[i].trim() != '') {
					medicine_route_el.length = len+1;
					medicine_route_el.options[len].value = routeIds[i].trim();
					medicine_route_el.options[len].text = routeNames[i];
					len++;
				}
			}
		} else {
			var charge = empty(rateDetails.charge) ? 0 : rateDetails.charge;
			var discount = empty(rateDetails.discount) ? 0 : rateDetails.discount;
			price = charge - discount;
			if (showRateDetails) {
				document.getElementById('d_price').value = price;
				document.getElementById('d_price_label').textContent = price;
			} else {
				document.getElementById('d_price').value = '';
				document.getElementById('d_price_label').textContent = '';
			}

			document.getElementById('d_package_size').value= '';
			document.getElementById('d_pkg_size_label').textContent = '';
		}
		ajaxInProgress = false;
	}
}

function displayAmounts() {
	return (!empty(showChargesAllRatePlan) && showChargesAllRatePlan == 'A');
}

function onGetChargeFailure() {
	ajaxInProgress = false;
}

function toggleDurationUnits(enable, prefix) {
	enable = empty(enable) ? false : enable;
	var els = document.getElementsByName(prefix+"_duration_units");
	for (var i=0; i<els.length; i++) {
		els[i].disabled = !enable;
		els[i].checked = false;
	}
}

function clearItemDetails(oSelf) {
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_item_id').value = '';
	if (!useGenerics)
		document.getElementById('d_medicine_route').length = 1;
	else
		document.getElementById('d_medicine_route').selectedIndex = 0;
	document.getElementById('d_admin_strength').value = '';
	document.getElementById('d_frequency').value = '';
	document.getElementById('d_strength').value = '';
	document.getElementById('d_duration').value = '';
	var itemType = getItemType();
	var enable = itemType == 'Medicine';
	toggleDurationUnits(enable, 'd');
	if (enable) {
		document.getElementById('d_consumption_uom').disabled = !useGenerics;
		document.getElementsByName('d_duration_units')[0].checked = true;
	} else {
		document.getElementById('d_consumption_uom').disabled = true;
	}

	document.getElementById('d_qty').value = '';
	document.getElementById('d_remarks').value = '';
	document.getElementById('d_consumption_uom').value = '';
	document.getElementById('d_consumption_uom_label').textContent = '';
	document.getElementById('genericNameAnchor_dialog').style.display = 'none';
	document.getElementById('genericNameAnchor_dialog').href = '';
	document.getElementById('genericNameAnchor_dialog').innerHTML = '';
	document.getElementById('d_generic_code').value = '';
	document.getElementById('d_generic_name').value = '';
	document.getElementById('d_itemMasterType').textContent = '';
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_package_size').value = '';
	document.getElementById('d_price').value = '';
	document.getElementById('d_pkg_size_label').textContent = '';
	document.getElementById('d_price_label').textContent = '';
	document.getElementById('d_qty_in_stock').value = '';
	document.getElementById('d_item_form_id').value = '';
	document.getElementById('d_granular_units').value = '';
	document.getElementById('d_item_strength').value = '';
	document.getElementById('d_item_strength_units').value = '';
	document.getElementById('d_ispackage').value = '';

}

var colIndex  = 0;
var ITEM_TYPE = colIndex++, ITEM_NAME = colIndex++, FORM =  colIndex++, STRENGTH = colIndex++,
	ADMIN_STRENGTH = colIndex++, DETAILS = colIndex++, ROUTE = colIndex++, REMARKS = colIndex++;
var	QTY = colIndex++,
	PKG_PRICE = colIndex++,
	UNIT_PRICE = colIndex++, TRASH_COL = colIndex++, EDIT_COL = colIndex++;
var itemsAdded = 0;
function addToTable() {
	if (ajaxInProgress) {
		setTimeout("addToTable()", 100);
		return false
	}
	var itemName = document.getElementById('d_itemName').value;
	if (itemName == '') {
   		alert('Please prescribe the item');
   		document.getElementById('d_itemName').focus();
   		return false;
   	}
   	var duration = document.getElementById('d_duration').value;
   	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById('d_duration').focus();
		return false
	}
	var strength = document.getElementById('d_strength').value;
	var granular_unit = document.getElementById('d_granular_units').value ;
   	if (!empty(granular_unit) && granular_unit == 'Y') {
	   	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	   		document.getElementById('d_strength').focus();
	   		return false;
	   	}
	}
	var item_strength = document.getElementById('d_item_strength').value;
   	var item_strength_units = document.getElementById('d_item_strength_units').value;
   	item_strength_units = item_strength == '' ? '' : item_strength_units;
   	var strength_unit_name = document.getElementById('d_item_strength_units').options[document.getElementById('d_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;

   	var itemType = getItemType();

	var id = getNumCharges('itemsTable');
   	var table = document.getElementById("itemsTable");
	var templateRow = table.rows[getTemplateRow('itemsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	var itemId = document.getElementById('d_item_id').value;
   	var adminStrength = document.getElementById('d_admin_strength').value;
   	var frequency = document.getElementById('d_frequency').value;
   	var qty = document.getElementById('d_qty').value;
   	var remarks = document.getElementById('d_remarks').value;
   	var master = document.getElementById('d_item_master').value;
   	var genericCode = document.getElementById('d_generic_code').value;
   	var genericName = document.getElementById('d_generic_name').value;
   	var ispackage = document.getElementById('d_ispackage').value;
   	var pkg_size = getAmount(document.getElementById('d_package_size').value);
   	var consumption_uom = document.getElementById('d_consumption_uom').value;
   	var price = getPaise(document.getElementById('d_price').value);
   	var routeId = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;
   	var item_form_id = document.getElementById('d_item_form_id').value;
   	var item_form_name = document.getElementById('d_item_form_id').options[document.getElementById('d_item_form_id').selectedIndex].text;
	var item_pkg_price = 0;
	var item_unit_price = 0;

   	setNodeText(row.cells[ITEM_TYPE], itemType);
   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
   		var duration_radio_els = document.getElementsByName('d_duration_units');
		var duration_units;
		for (var k=0; k<duration_radio_els.length; k++) {
			if (duration_radio_els[k].checked) {
				duration_units = duration_radio_els[k].value;
				break;
			}
		}

		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
   		var details = "";
   		if (frequency != '' || duration != '')
   			details = frequency + " / " + duration + ' ' + duration_units;
   		setNodeText(row.cells[DETAILS], details, 20);
		setNodeText(row.cells[QTY], qty);
		if (item_form_id != '') {
			setNodeText(row.cells[FORM], item_form_name, 15);
		}
		setNodeText(row.cells[STRENGTH], item_strength + ' ' + strength_unit_name, 15);

		setHiddenValue(id, "generic_code", genericCode);
		setHiddenValue(id, "admin_strength", adminStrength);
		setHiddenValue(id, "generic_name", genericName);
		setHiddenValue(id, "frequency", frequency);
		setHiddenValue(id, "strength", strength);
		setHiddenValue(id, "duration", duration);
		setHiddenValue(id, "duration_units", duration_units);
		setHiddenValue(id, "medicine_quantity", qty);
		setHiddenValue(id, "qty_in_stock", document.getElementById('d_qty_in_stock').value);
		setHiddenValue(id, "item_form_id", item_form_id);
		setHiddenValue(id, "granular_units", granular_unit);
		setHiddenValue(id, "item_strength", item_strength);
		setHiddenValue(id, "item_strength_units", item_strength_units);
		setHiddenValue(id, "presc_by_generics", prescriptions_by_generics);
		if (pkg_size != '' && price != '' && qty != '') {
			item_unit_price = (price/pkg_size) * qty;
			item_pkg_price = Math.ceil(qty/pkg_size) * price;
		}
	} else {
		item_pkg_price = price;
		item_unit_price = price;
	}

	setNodeText(row.cells[PKG_PRICE], item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setNodeText(row.cells[UNIT_PRICE], item_unit_price == 0? '' : formatAmountPaise(item_unit_price));
	setNodeText(row.cells[ROUTE], routeName);
	setNodeText(row.cells[REMARKS], remarks, 30);

	setHiddenValue(id, "consumption_uom", consumption_uom);
	setHiddenValue(id, "item_prescribed_id", "_");
	setHiddenValue(id, "itemType", itemType);
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "ispackage", ispackage);
	setHiddenValue(id, "pkg_size", pkg_size == '' ? '' : pkg_size);
	setHiddenValue(id, "pkg_price", price == '' ? '' : formatAmountPaise(price));
	setHiddenValue(id, "item_pkg_price", item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setHiddenValue(id, "item_unit_price", item_unit_price == 0 ? '' : formatAmountPaise(item_unit_price));
	setHiddenValue(id, "route_id", routeId);
	setHiddenValue(id, "route_name", routeName);
	setHiddenValue(id, "issued", "P");
	if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
	} else {
		estimateTotal();
	}
	itemsAdded++;
	clearPrescriptionFields();
	setRowStyle(id);
	addItemDialog.align("tr", "tl");
	document.getElementById('d_itemName').focus();
	return id;
}


function estimateTotal() {
	var unitEls = document.getElementsByName('item_unit_price');
	var pkgEls = document.getElementsByName('item_pkg_price');
	var pkgTotalAmount = 0;
	var unitTotalAmount = 0;
	for (var i=0; i<pkgEls.length; i++) {
		var itemPkgAmount = getPaise(pkgEls[i].value);
		pkgTotalAmount += itemPkgAmount;
		var itemUnitAmount = getPaise(unitEls[i].value);
		unitTotalAmount += itemUnitAmount;
	}
	document.getElementById('estimatedUnitTotal').textContent = formatAmountPaise(unitTotalAmount);
	document.getElementById('estimatedPkgTotal').textContent = formatAmountPaise(pkgTotalAmount);
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

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.consultationForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearPrescriptionFields() {
	document.getElementById('d_itemName').value = '';
   	clearItemDetails();
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');
	var prescribedId = getIndexedValue("item_prescribed_id", i);
	var qty_in_stock = getIndexedValue("qty_in_stock", i);

 	var flagImgs = row.cells[ITEM_TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("delItem", i) == 'true';
	var edited = getIndexedValue("edited", i) == 'true';
	var itemType = getIndexedValue("itemType", i);

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
		if (itemType == 'Medicine' && qty_in_stock == 0) cls = 'zero_qty'
		else cls = 'added';
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

function cancelItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var oldDeleted =  getIndexedValue("delItem", id);

	var isNew = getIndexedValue("item_prescribed_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("delItem", id, newDeleted);
		setIndexedValue("edited", id, "true");
		setRowStyle(id);
	}
	var itemType = getIndexedValue("itemType", id);
	if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
	} else {
		estimateTotal();
	}
	return false;
}

function initEditItemDialog() {
	var dialogDiv = document.getElementById("editItemDialog");
	dialogDiv.style.display = 'block';
	editItemDialog = new YAHOO.widget.Dialog("editItemDialog",{
			width:"650px",
			text: "Edit Item",
			context :["itemsTable", "tl", "tl"],
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
	YAHOO.util.Event.addListener('editOk', 'click', editTableRow, editItemDialog, true);
	YAHOO.util.Event.addListener('editCancel', 'click', handleEditItemCancel, editItemDialog, true);
	YAHOO.util.Event.addListener('editPrevious', 'click', openPrevious, editItemDialog, true);
	YAHOO.util.Event.addListener('editNext', 'click', openNext, editItemDialog, true);
	editItemDialog.render();
}

function handleEditItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		var id = document.consultationForm.editRowId.value;
		var row = getChargeRow(id, "itemsTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldEdited = false;
		this.hide();
	}
}

function showEditItemDialog(obj) {
	parentDialog = editItemDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editItemDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.consultationForm.editRowId.value = id;
	var itemType = getIndexedValue("itemType", id);
	document.getElementById('ed_itemTypeLabel').textContent = itemType;
	document.getElementById('ed_itemNameLabel').textContent = getIndexedValue("item_name", id);
	document.getElementById('ed_itemName').value = getIndexedValue("item_name", id);
	document.getElementById('ed_item_id').value = getIndexedValue("item_id", id);
	document.getElementById('ed_itemType').value = itemType;
	var master = getIndexedValue("item_master", id);
	if (master == 'item_master') {
		document.getElementById('ed_itemMasterType').textContent = 'Yes';
	} else {
		document.getElementById('ed_itemMasterType').textContent = 'No';
	}
	toggleItemFormRow(false);
	document.getElementById('ed_consumption_uom_label').textContent = getIndexedValue("consumption_uom", id);
	document.getElementById('ed_consumption_uom').value = getIndexedValue("consumption_uom", id);

	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById('ed_consumption_uom').disabled = itemType == 'Medicine' && !useGenerics;
		document.getElementById('ed_admin_strength').disabled = false;
		document.getElementById('ed_frequency').disabled = false;
		document.getElementById('ed_strength').disabled = false;
		document.getElementById('ed_duration').disabled = false;
		document.getElementById('ed_qty').disabled = false;
		document.getElementById('ed_remarks').disabled = false;

		initEditDosageAutoComplete();
		document.getElementById('ed_medicine_route').textContent = getIndexedValue("route_name", id);
		document.getElementById('ed_strength').value = getIndexedValue("strength", id);
		document.getElementById('ed_frequency').value = getIndexedValue("frequency", id);
		document.getElementById('ed_admin_strength').value = getIndexedValue("admin_strength", id);
		document.getElementById('ed_duration').value = getIndexedValue("duration", id);

		// enable the duration units only if item is not isssued.
		toggleDurationUnits(issued != 'O', 'ed');
		var duration_units = getIndexedValue("duration_units", id);
		var els = document.getElementsByName("ed_duration_units");
		for (var k=0; k<els.length; k++) {
			if (els[k].value == duration_units) {
				els[k].checked = true;
				break;
			}
		}

		document.getElementById('ed_qty').value = getIndexedValue("medicine_quantity", id);
		document.getElementById('genericNameAnchor_editdialog').innerHTML = getIndexedValue("generic_name", id);

		if (itemType == 'Medicine') {
			document.getElementById('genericNameAnchor_editdialog').href =
				'javascript:showGenericInfo("", "", "editdialog", "' + getIndexedValue("generic_code", id) + '")';
			document.getElementById('genericNameAnchor_editdialog').style.display = 'block';
		}
		document.getElementById('ed_item_form_id').value = getIndexedValue("item_form_id", id);
		document.getElementById('ed_granular_units').value = getIndexedValue("granular_units", id);
		document.getElementById('ed_item_strength').value = getIndexedValue('item_strength', id);
		document.getElementById('ed_item_strength_units').value = getIndexedValue('item_strength_units', id);
	} else {
		document.getElementById('ed_consumption_uom').disabled = true;
		toggleDurationUnits(false, 'ed');
		if (itemType == 'Instructions') {
			document.getElementById('ed_remarks').disabled = true;
		} else {
			document.getElementById('ed_remarks').disabled = false;
		}
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_frequency').disabled = true;
		document.getElementById('ed_strength').disabled = true;
		document.getElementById('ed_duration').disabled = true;
		document.getElementById('ed_qty').disabled = true;

		document.getElementById('ed_admin_strength').value = '';
		document.getElementById('ed_strength').value = '';
		document.getElementById('ed_frequency').value = '';
		document.getElementById('ed_duration').value = '';
		document.getElementById('ed_qty').value = '';

		//document.getElementById('edGenericNameRow').style.display = 'none';
		document.getElementById('genericNameAnchor_editdialog').innerHTML = '';
		document.getElementById('genericNameAnchor_editdialog').style.display = 'none';
		document.getElementById('genericNameAnchor_editdialog').href='';
	}
   	document.getElementById('ed_pkg_size_label').textContent = getIndexedValue("pkg_size", id);
   	document.getElementById('ed_price_label').textContent = getIndexedValue("pkg_price", id);

	document.getElementById('ed_package_size').value = getIndexedValue('pkg_size', id);
	document.getElementById('ed_price').value = getIndexedValue('pkg_price', id);
	document.getElementById('ed_ispackage').value = getIndexedValue("ispackage", id);
	document.getElementById('ed_remarks').value = getIndexedValue('item_remarks', id);
	document.getElementById('ed_item_master').value = getIndexedValue('item_master', id);

	var issued = getIndexedValue('issued', id);
	if (issued == 'O') {
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_frequency').disabled = true;
		document.getElementById('ed_strength').disabled = true;
		document.getElementById('ed_duration').disabled = true;
		document.getElementById('ed_qty').disabled = true;
		document.getElementById('ed_remarks').disabled = true;
		document.getElementById('ed_item_strength').disabled = true;
		document.getElementById('ed_item_strength_units').disabled = true;
		document.getElementById('ed_consumption_uom').disabled = true;
		document.getElementById('ed_item_form_id').disabled = true;
	}
	document.getElementById('ed_remarks').focus();
	return false;
}


function editTableRow() {
	var id = document.consultationForm.editRowId.value;
	var issued = getIndexedValue("issued", id);
	if (issued == 'O') {
		editItemDialog.cancel();
		return true;
	}
	var row = getChargeRow(id, 'itemsTable');

	var itemType = document.getElementById('ed_itemType').value;
   	var itemName = document.getElementById('ed_itemName').value;
   	var itemId = document.getElementById('ed_item_id').value;
   	var adminStrength = document.getElementById('ed_admin_strength').value;
   	var frequency = document.getElementById('ed_frequency').value;
   	var strength = document.getElementById('ed_strength').value;
   	var duration = document.getElementById('ed_duration').value;
   	var qty = document.getElementById('ed_qty').value;
   	var remarks = document.getElementById('ed_remarks').value;
   	var master = document.getElementById('ed_item_master').value;
   	var ispackage = document.getElementById('ed_ispackage').value;
   	var pkg_size = getAmount(document.getElementById('ed_package_size').value);
   	var price = getPaise(document.getElementById('ed_price').value);
   	var consumption_uom = document.getElementById('ed_consumption_uom').value;
   	var item_form_id = document.getElementById('ed_item_form_id').value;
   	var granular_unit = document.getElementById('ed_granular_units').value;
   	var item_strength = document.getElementById('ed_item_strength').value;
   	var item_form_name = document.getElementById('ed_item_form_id').options[document.getElementById('ed_item_form_id').selectedIndex].text;
	var item_pkg_price = 0;
	var item_unit_price = 0;

	var item_strength_units = document.getElementById('ed_item_strength_units').value;
	item_strength_units = item_strength == '' ? '' : item_strength_units;
   	var strength_unit_name = document.getElementById('ed_item_strength_units').options[document.getElementById('ed_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;
	if (!empty(granular_unit) && granular_unit == 'Y') {
	   	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	   		document.getElementById('ed_strength').focus();
	   		return false;
	   	}
	}
   	var duration_radio_els = document.getElementsByName('ed_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById('ed_duration').focus();
		return false;
	}
	if (!empty(duration) && empty(duration_units)) {
		alert("Please select the duration units");
		return false;
	}

	setNodeText(row.cells[ITEM_TYPE], itemType);
   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {

   		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
   		var details = "";
   		if (frequency != '' || duration != '')
   			details = frequency + " / " + duration + ' ' + duration_units;
		setNodeText(row.cells[DETAILS], details, 20);
		setNodeText(row.cells[QTY], qty);
		if (item_form_id != '')
			setNodeText(row.cells[FORM], item_form_name, 15);
		setNodeText(row.cells[STRENGTH], item_strength + ' ' + strength_unit_name, 15);

		setHiddenValue(id, "admin_strength", adminStrength);
		setHiddenValue(id, "frequency", frequency);
		setHiddenValue(id, "strength", strength);
		setHiddenValue(id, "duration", duration);
		setHiddenValue(id, "duration_units", duration_units);
		setHiddenValue(id, "medicine_quantity", qty);
		setHiddenValue(id, "item_form_id", item_form_id);
		setHiddenValue(id, "granular_units", granular_unit)
		setHiddenValue(id, "item_strength", item_strength);
		setHiddenValue(id, "item_strength_units", item_strength_units);
		if (pkg_size != '' && price != '' && qty != '') {
			item_unit_price = (price/pkg_size) * qty;
			item_pkg_price = Math.ceil(qty/pkg_size) * price;
		}
	} else {
		item_pkg_price = price;
		item_unit_price = price;
	}
	setNodeText(row.cells[PKG_PRICE], item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setNodeText(row.cells[UNIT_PRICE], item_unit_price == 0? '' : formatAmountPaise(item_unit_price));
	setNodeText(row.cells[REMARKS], remarks, 30);

	setHiddenValue(id, "itemType", itemType);
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "ispackage", ispackage);
	setHiddenValue(id, "consumption_uom", consumption_uom);
	setHiddenValue(id, "pkg_size", pkg_size);
	setHiddenValue(id, "pkg_price", formatAmountPaise(price));
	setHiddenValue(id, "item_pkg_price", formatAmountPaise(item_pkg_price));
	setHiddenValue(id, "item_unit_price", formatAmountPaise(item_unit_price));

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);

	if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
	} else {
		estimateTotal();
	}
	editItemDialog.cancel();
	return true;
}
var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function openPrevious() {
	var id = document.consultationForm.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		if (!editTableRow()) return false;
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditItemDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var id = document.consultationForm.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		if (!editTableRow()) return false;
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('itemsTable').rows.length-2) {
		showEditItemDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
	}
}

function initFrequencyAutoComplete() {
	if (dosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dosage_name"},
						{key : "per_day_qty"},
					 ]
		};
		// Instantiate first AutoComplete
		dosageAutoComplete = new YAHOO.widget.AutoComplete('d_frequency', 'frequencyContainer', ds);
		dosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		//dosageAutoComplete.typeAhead = true;
		dosageAutoComplete.useShadow = true;
		dosageAutoComplete.minQueryLength = 0;
		dosageAutoComplete.allowBrowserAutocomplete = false;
		dosageAutoComplete.maxResultsDisplayed = 20;
		dosageAutoComplete.resultTypeList = false;

		dosageAutoComplete.itemSelectEvent.subscribe(setPerDayQty);
		dosageAutoComplete.unmatchedItemSelectEvent.subscribe(checkDosage);
		dosageAutoComplete.textboxChangeEvent.subscribe(clearQty);
	}
}

function setPerDayQty(sType, oArgs) {
	if (document.getElementById('d_granular_units').value == 'Y' ) {
		var record = oArgs[2];
		document.getElementById('d_per_day_qty').value = record.per_day_qty;
	}
	calcQty('d');
}

function checkDosage() {
	if (document.getElementById('d_granular_units').value == 'Y' ) {
		document.getElementById('d_per_day_qty').value = '';
	}
}

function clearQty(){
	if (document.getElementById('d_granular_units').value == 'Y' ) {
		document.getElementById('d_qty').value = '';
	}
	calcQty('d');
}

function calcQty(idPrefix){
	if (document.getElementById(idPrefix + '_granular_units').value == 'Y' ) {
		var qty = '';
		var frequencyName = document.getElementById(idPrefix + '_frequency').value;
		var duration = document.getElementById(idPrefix + '_duration').value;
		var validNumber = /[1-9]/;
		var regExp = new RegExp(validNumber);

		if (!validateMedBlockExceptQty("onchange", idPrefix)) return false;

		var perDayQty = null;
		for (var i=0; i<medDosages.length; i++) {
			var frequency = medDosages[i];
			if (frequencyName.trim().toLowerCase() == frequency.dosage_name.trim().toLowerCase()) {
				perDayQty = frequency.per_day_qty;
			}
		}
		if (perDayQty != null && !empty(duration)) {
			var duration_units_els = document.getElementsByName(idPrefix+'_duration_units');
			var duration_units = 'D';
			for (var j=0; j<duration_units_els.length; j++) {
				if (duration_units_els[j].checked) {
					duration_units = duration_units_els[j].value;
					break;
				}
			}
			if (duration_units == 'D')
				qty = Math.ceil(duration * perDayQty);
			else if (duration_units == 'W')
				qty = Math.ceil((duration * 7) * perDayQty);
			else if (duration_units == 'M')
				qty = Math.ceil((duration * 30) * perDayQty);

		}
		document.getElementById(idPrefix + '_qty').value = qty;
	}
}

var editDosageAutoComplete = null; // dosage autocomplete for edit item dialog.
function initEditDosageAutoComplete() {
	if (editDosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dosage_name"},
						{key : "per_day_qty"},
					 ]
		};
		// Instantiate first AutoComplete
		editDosageAutoComplete = new YAHOO.widget.AutoComplete('ed_frequency', 'ed_frequencyContainer', ds);
		editDosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		//dosageAutoComplete.typeAhead = true;
		editDosageAutoComplete.useShadow = true;
		editDosageAutoComplete.minQueryLength = 0;
		editDosageAutoComplete.allowBrowserAutocomplete = false;
		editDosageAutoComplete.maxResultsDisplayed = 20;
		editDosageAutoComplete.resultTypeList = false;

		editDosageAutoComplete.itemSelectEvent.subscribe(editSetPerDayQty);
		editDosageAutoComplete.unmatchedItemSelectEvent.subscribe(editCheckDosage);
		editDosageAutoComplete.textboxChangeEvent.subscribe(editClearQty);
	}
}

function editSetPerDayQty(sType, oArgs) {
	if (document.getElementById('ed_granular_units').value == 'Y' ) {
		var record = oArgs[2];
		document.getElementById('ed_per_day_qty').value = record.per_day_qty;
	}
	calcQty('ed');
}

function editCheckDosage() {
	if (document.getElementById('ed_granular_units').value == 'Y' ) {
		document.getElementById('ed_per_day_qty').value = '';
	}
}

function editClearQty() {
	if (document.getElementById('ed_granular_units').value == 'Y' ) {
		document.getElementById('ed_qty').value = '';
		setEdited();
	}
	calcQty('ed');
}

function validateMedBlockExceptQty(calledOn, idPrefix) {
	var itemType = (idPrefix == 'd' ? getItemType() : document.getElementById(idPrefix + "_itemType").value);
	if (itemType != 'Medicine' && itemType != 'NonHospital') return;

	var medicineName = document.getElementById(idPrefix + '_itemName').value;
	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);

	if (medicineName == '') {
		alert("Please enter the Medicine Name");
		return false;
	}
	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById(idPrefix + '_duration').focus();
		return false
	}
	return true;
}

function editDialogGeneric() {
	document.getElementById('genericNameDisplayDialog').style.visibility = 'display';
	genericDialog = new YAHOO.widget.Dialog("genericNameDisplayDialog",
			{
				width:"500px",
				context : ["loadGenInfo", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	YAHOO.util.Event.addListener("genericNameCloseBtn", "click", closeGenericDialog, genericDialog, true);
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:closeGenericDialog, scope:genericDialog, correctScope:true } );
	genericDialog.cancelEvent.subscribe(closeGenericDialog);
	genericDialog.cfg.setProperty("keylisteners", kl);
	genericDialog.render();
}

function closeGenericDialog() {
	childDialog = null;
	this.hide();
}

function showGenericInfo(index, prefix, suffix, generic_code) {
	childDialog = genericDialog;
	var anchor = document.getElementById(prefix + "genericNameAnchor" + index + "_" + suffix);
	genericDialog.cfg.setProperty("context", [anchor, "tr", "tl"], false);
	genericDialog.show();
	if (generic_code != "") {
		var ajaxReqObject = new XMLHttpRequest();
		var url=cpath+"/outpatient/OpPrescribeAction.do?_method=getGenericJSON&generic_code="+encodeURIComponent(generic_code);
		getResponseHandlerText(ajaxReqObject, handleGenericResponse, url);
	} else {
		document.getElementById('classification_name').innerHTML = '';
		document.getElementById('sub_classification_name').innerHTML = '';
		document.getElementById('standard_adult_dose').innerHTML = '';
		document.getElementById('criticality').innerHTML = '';
		document.getElementById('generic_name').innerHTML = '';
	}
}

/*
 * Response handler for the ajax call to retrieve generic details like classification and sub-classification
 */
function handleGenericResponse(responseText) {
	if (responseText==null) return;
	if (responseText=="") return;
	var genericDetails;
    eval("var genericDetails = " + responseText);			// response is an array of item batches
    if (genericDetails != null) {
		var genericId = genericDetails.generic_code;
		document.getElementById('classification_name').innerHTML = genericDetails.classificationName;
		if (genericDetails.sub_ClassificationName != null) {
			document.getElementById('sub_classification_name').innerHTML = genericDetails.sub_ClassificationName;
		}
		document.getElementById('standard_adult_dose').innerHTML = genericDetails.standard_adult_dose;
		document.getElementById('criticality').innerHTML = genericDetails.criticality;
		document.getElementById('gen_generic_name').innerHTML = genericDetails.gmaster_name;

	}
}

function onSave(printValue) {
	var treatment_id_els = document.getElementsByName('h_treatment_id');
	var h_planned_date = document.getElementsByName('h_planned_date');
	var h_service_name = document.getElementsByName('h_service_name');
	var h_treatment_status = document.getElementsByName('h_treatment_status');
	var h_tooth_number_required = document.getElementsByName('h_tooth_num_required');
	var h_tooth_number_els = null;
	if (tooth_numbering_system == 'U')
		h_tooth_number_els = document.getElementsByName("h_tooth_unv_number");
	else
		h_tooth_number_els = document.getElementsByName("h_tooth_fdi_number");

	var edited = document.getElementsByName('ht_edited');
	var deleted = document.getElementsByName('ht_delete');
	for (var i=0; i<treatment_id_els.length-1; i++) {
		var treatment_id = treatment_id_els[i].value;
		if (deleted[i].value != 'true') {
			// checking for tooth numbers when the user entered planned qty for more than 1 and not inequal no of tooth numbers.
			// we copied all the tooth numbers to the first row, and asking user to correct the tooth numbers for each row.
			if ((h_treatment_status[i].value == 'P' ||  h_treatment_status[i].value == 'I') &&
				h_tooth_number_required[i].value == 'Y' &&
				empty(h_tooth_number_els[i].value)) {
				alert("Please enter the tooth number for service : "+h_service_name[i].value);
				return false;
			}

			var plnDateTime = getDateTime(h_planned_date[i].value.split(' ')[0], h_planned_date[i].value.split(' ')[1]);
			var subTaskComplDateEl = document.getElementsByName('st_completion_date_'+i);
			var subTaskComplTimeEl = document.getElementsByName('st_completion_time_'+i);
			var sub_task_name = document.getElementsByName("sub_task_name_"+i);
			for (var j=0; j<subTaskComplDateEl.length; j++) {
				if (!empty(subTaskComplDateEl[j].value)) {
					var taskComplDate = getDateTime(subTaskComplDateEl[j].value, subTaskComplTimeEl[j].value);
					if (taskComplDate < plnDateTime) {
						alert(h_service_name[i].value+"'s sub task "+sub_task_name[j].value+"'s Completion Date & Time should be \n * greater than or equal to Planned Date & Time.");
						return false;
					}
				}
			}// sub task elements
		}// if
	}// for
	document.consultationForm.isPrint.value = printValue;
	document.consultationForm.submit();
	return true;
}

function printTreatmentQuotation() {
	var mrNo = document.consultationForm.mr_no.value;
	var href = cpath+"/DentalConsultation/QuotationPrint.do?_method=print&mr_no="+mrNo;
	window.open(href);
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
	return getElementPaise(getIndexedFormElement(document.consultationForm, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.consultationForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.consultationForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function deleteSubTasks(index) {
	var tableObj = document.getElementById('serviceSubTasksTab');
	var sub_task_elements = document.getElementsByName('sub_task_id_'+index);

	for (var i=sub_task_elements.length-1; i>=0; i--) {
		tableObj.deleteRow((getThisRow(sub_task_elements[i])).rowIndex);
	}
}
