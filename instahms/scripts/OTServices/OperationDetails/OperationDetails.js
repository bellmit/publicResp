// initialization method which calls all init methods.
function init() {
	var appointmentId = document.getElementById('appointment_id');
	if(appointmentId && appointmentId.options.length == 2) {
		setSelectedIndex(appointmentId,appointmentId.options[1].value);
	}
	initPrescribedDoctorAutoComplete();
	initOperationDetailsDialog();
	initOperationsAutoComolete();
	initSurgeonsDialog();
	initSurgeonsAutoComplete();
	initAnaesthetistsDialog();
	initAnaesthetistsAutoComplete();
	initPaediatricianDialog();
	initPaediatricianAutoComplete();
	initAnaesthesiaTypesDialog();
}

function initPrescribedDoctorAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result: doctorsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('prescribing_doctor','prescribing_doctorAcDropdown', datasource);
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
	autoComp.resultTypeList = false;

 	if (autoComp._elTextbox.value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = autoComp._elTextbox.value;
	}

	autoComp.itemSelectEvent.subscribe(setPrescribingDoctorId);
	autoComp.selectionEnforceEvent.subscribe(clearPrescribingDoctorId);
	return autoComp;
}

function setPrescribingDoctorId(oSelf, sArgs) {
	document.getElementById("prescribing_doctorId").value = sArgs[2].doctor_id;
}

function clearPrescribingDoctorId(oSelf, sClearedValue) {
	document.getElementById("prescribing_doctorId").value = '';
}

/*function onchangeOperationStatus(obj){
	if(!empty(obj.value) && obj.value == 'X') {
		document.getElementById('complete_add_to_bill').disabled = true;
	} else {
		document.getElementById('complete_add_to_bill').disabled = false;
	}
}*/


function addResourcesToBill() {
	document.getElementById('_method').value = 'AddBillableItemsToBill';
	return true;
}


function loadOperationDetailsLinkedWithAppointment(obj) {
	var appointmentId = obj.value;
	if(!empty(appointmentId)) {
		eval("var operationDetails = "+getAppointmentOperationDetails(appointmentId));
		if(operationDetails != null) {
			var appProcedureDetails = operationDetails['procedureDetails'];
			var appSurgeonDetails = operationDetails['surgeonDetails'];
			var appAnestiatistDetails = operationDetails['anestiatistDetails'];
			if(appProcedureDetails != null) {
				document.getElementById('op_details_div').style.display = 'block';
				loadAppProcedureDetails(appProcedureDetails);
			}

			if(appSurgeonDetails != null) {
				for (var i=0;i<appSurgeonDetails.length;i++) {
					loadAppSurgeonDetails(appSurgeonDetails[i])
				}
			}

			if(appAnestiatistDetails != null) {
				for (var i=0;i<appAnestiatistDetails.length;i++) {
					loadAppAnestiatistDetails(appAnestiatistDetails[i])
				}
			}
		}
	}
}

function getAppointmentOperationDetails(appointmentId) {
	var ajaxReqObject = newXMLHttpRequest();
	var url = cpath+"/otservices/OtManagement/Operation.do?_method=getAppointmentOperationDetails"
	url = url + "&appointment_id=" + appointmentId;
	ajaxReqObject.open("POST", url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
			return ajaxReqObject.responseText;
		}
	}
	return null;
}

function loadAppSurgeonDetails(appSurgeonDetails) {
	var tableId = 'surgeonDetailsTable';
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);

	var surgeonName = appSurgeonDetails.surgeon_name;
   	var surgeonId = appSurgeonDetails.surgeon_id;

	setNodeText(row.cells[OP_SURGEON_NAME], surgeonName);
	setNodeText(row.cells[OP_SURGEON_SPECIALITY], "Surgeon");

	setHiddenValue(id, "su_resource_id", surgeonId);
	setHiddenValue(id, "su_operation_speciality", 'SU');

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "suDeleteIcon");
	deleteImg.setAttribute("id", "suDeleteIcon");
	deleteImg.setAttribute("title", "Delete Surgeon Row");
	deleteImg.setAttribute("onclick","cancelSurgeons(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");


	for (var i=row.cells[OP_SURGEON_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_SURGEON_DELETE].removeChild(row.cells[OP_SURGEON_DELETE].childNodes[i]);
	}
	row.cells[OP_SURGEON_DELETE].appendChild(deleteImg);
}

function loadAppProcedureDetails(appProcedureDetails) {
	var tableId = 'procedureDetailsTable';
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);

	var operationName = appProcedureDetails.operation_name;
   	var operationId = appProcedureDetails.operation_id;

	setNodeText(row.cells[OP_OPERATION_NAME], operationName);
	setNodeText(row.cells[OP_OPERATION_PRIORITY], "Primary");

	setHiddenValue(id, "operation_id", operationId);
	setHiddenValue(id, "operation_name", operationName);

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", "Edit Operation");
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditOpertaionDialog(this)");
	editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "oDeleteIcon");
	deleteImg.setAttribute("title", "Delete Operation Row");
	deleteImg.setAttribute("onclick","cancelOperations(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");

	for (var i=row.cells[OP_OPERATION_EDIT].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_EDIT].removeChild(row.cells[OP_OPERATION_EDIT].childNodes[i]);
	}
	row.cells[OP_OPERATION_EDIT].appendChild(editImg);
		for (var i=row.cells[OP_OPERATION_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_DELETE].removeChild(row.cells[OP_OPERATION_DELETE].childNodes[i]);
	}
	row.cells[OP_OPERATION_DELETE].appendChild(deleteImg);
}

function loadAppAnestiatistDetails(appAnestiatistDetails) {
	var tableId = 'anaesthetistDetailsTable';
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);

	var anestiatistName = appAnestiatistDetails.anestiatist_name;
   	var anestiatistId = appAnestiatistDetails.anestiatist_id;

	setNodeText(row.cells[OP_ANAESTHETISTS_NAME], anestiatistName);
	setNodeText(row.cells[OP_ANAESTHETISTS_SPECIALITY], "Anaesthetists");

	setHiddenValue(id, "an_resource_id", anestiatistId);
	setHiddenValue(id, "an_operation_speciality", "ASU");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "anDeleteIcon");
	deleteImg.setAttribute("id", "anDeleteIcon");
	deleteImg.setAttribute("title", "Delete Surgeon Row");
	deleteImg.setAttribute("onclick","cancelAnaesthetists(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");


	for (var i=row.cells[OP_ANAESTHETISTS_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_ANAESTHETISTS_DELETE].removeChild(row.cells[OP_ANAESTHETISTS_DELETE].childNodes[i]);
	}
	row.cells[OP_ANAESTHETISTS_DELETE].appendChild(deleteImg);
}


 // common methods used in all over places in this js.
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

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.detailedOperatioForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getItemRow(i,tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

var addOperationsDialog;
var addSurgeonsDialog;
var addAnaesthetistsDialog;
var addPaediatricianDialog;
var addAnaesthesiaTypeDialog;
// initialization of operation dialog.
function initOperationDetailsDialog() {
	var dialogDiv = document.getElementById("addOperationDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addOperationsDialog = new YAHOO.widget.Dialog("addOperationDialog",
			{	width:"600px",
				context : ["addOperationDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_operation_add_btn', 'click', addOperationRecord, addOperationsDialog, true);
	YAHOO.util.Event.addListener('d_operation_cancel_btn', 'click', cancelOperationsDialog, addOperationsDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelOperationsDialog,
	                                                scope:addOperationsDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:addOperationRecord,
	                                                scope:addOperationsDialog,
	                                                correctScope:true } );

	addOperationsDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addOperationsDialog.cfg.setProperty("keylisteners", [enterKeyListener]);
	addOperationsDialog.render();
}
// initialization of Surgeons dialog.
function initSurgeonsDialog() {
	var dialogDiv = document.getElementById("addSurgeonDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addSurgeonsDialog = new YAHOO.widget.Dialog("addSurgeonDialog",
			{	width:"400px",
				context : ["addSurgeonDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_surgeon_add_btn', 'click', addSurgeonRecord, addSurgeonsDialog, true);
	YAHOO.util.Event.addListener('d_surgeon_cancel_btn', 'click', cancelSurgeonDialog, addSurgeonsDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelSurgeonDialog,
	                                                scope:addSurgeonsDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:addSurgeonRecord,
	                                                scope:addSurgeonsDialog,
	                                                correctScope:true } );

	addSurgeonsDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addSurgeonsDialog.cfg.setProperty("keylisteners", [enterKeyListener]);
	addSurgeonsDialog.render();
}
// initialization of Anaesthetists dialog.
function initAnaesthetistsDialog() {
	var dialogDiv = document.getElementById("addAnaesthetistsDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addAnaesthetistsDialog = new YAHOO.widget.Dialog("addAnaesthetistsDialog",
			{	width:"400px",
				context : ["addAnaesthetistsDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_anaesthetists_add_btn', 'click', addAnaesthetistsRecord, addAnaesthetistsDialog, true);
	YAHOO.util.Event.addListener('d_anaesthetists_cancel_btn', 'click', cancelAnaesthetistsDialog, addAnaesthetistsDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelAnaesthetistsDialog,
	                                                scope:addAnaesthetistsDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                             { fn:addAnaesthetistsRecord,
	                                               scope:addAnaesthetistsDialog,
	                                               correctScope:true } );

	addAnaesthetistsDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addAnaesthetistsDialog.cfg.setProperty("keylisteners", [enterKeyListener]);
	addAnaesthetistsDialog.render();
}

function initPaediatricianDialog() {
	var dialogDiv = document.getElementById("addPaediatricianDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addPaediatricianDialog = new YAHOO.widget.Dialog("addPaediatricianDialog",
			{	width:"400px",
				context : ["addPaediatricianDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_paediatrician_add_btn', 'click', addPaediatricianRecord, addPaediatricianDialog, true);
	YAHOO.util.Event.addListener('d_paediatrician_cancel_btn', 'click', cancelPaediatricianDialog, addPaediatricianDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelPaediatricianDialog,
	                                                scope:addPaediatricianDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                             { fn:addPaediatricianRecord,
	                                               scope:addPaediatricianDialog,
	                                               correctScope:true } );

	addPaediatricianDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addPaediatricianDialog.cfg.setProperty("keylisteners", [enterKeyListener]);
	addPaediatricianDialog.render();
}

function initAnaesthesiaTypesDialog() {
	var dialogDiv = document.getElementById("addAnaesthesiaTypesDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addAnaesthesiaTypeDialog = new YAHOO.widget.Dialog("addAnaesthesiaTypesDialog",
			{	width:"400px",
				context : ["addAnaesthesiaTypesDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_anaesthesia_type_add_btn', 'click', addAnaesthesiaTypeRecord, addAnaesthesiaTypeDialog, true);
	YAHOO.util.Event.addListener('d_anaesthesia_type_cancel_btn', 'click', cancelAnaesthesiaTypeDialog, addAnaesthesiaTypeDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelAnaesthesiaTypeDialog,
	                                                scope:addAnaesthesiaTypeDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                             { fn:addAnaesthesiaTypeRecord,
	                                               scope:addAnaesthesiaTypeDialog,
	                                               correctScope:true } );

	addAnaesthesiaTypeDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addAnaesthesiaTypeDialog.cfg.setProperty("keylisteners", [enterKeyListener]);
	addAnaesthesiaTypeDialog.render();
}

// dialog related methods for operation dialog.
var gRowObj;
function openEditOpertaionDialog(obj,opeId) {
	var rowObj = findAncestor(obj,"TR");
	gRowObj = rowObj;
	var index = getRowItemIndex(rowObj);

	rowObj.className = 'selectedRow';
	document.getElementById('operationdialogheader').innerHTML = 'Edit Operation';
	if(!empty(opeId)){
		document.getElementById('d_operation_name').disabled = true;
		document.getElementById('d_oper_priority').disabled = true;
	} else {
		document.getElementById('d_operation_name').disabled = false;
		document.getElementById('d_oper_priority').disabled = false;
	}
	document.getElementById('opDialogId').value = index;
	updateGridToOperationDialog(obj,rowObj);
	return false;
}

function updateGridToOperationDialog(obj,rowObj) {
	document.getElementById('d_oper_priority').value         =  getElementByName(rowObj,"oper_priority").value;
	document.getElementById('d_operation_id').value 	=  getElementByName(rowObj,"operation_id").value;
	document.getElementById('d_operation_name').value   =  getElementByName(rowObj,"operation_name").value;
	document.getElementById('d_modifier').value         =  getElementByName(rowObj,"modifier").value;
	addOperationsDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	addOperationsDialog.show();
}

function addOperationRecord() {
	var tableId = "procedureDetailsTable";
	var id = getNumCharges(tableId);
	var dialogId = document.detailedOperatioForm.opDialogId.value;

	if(!validateOperationDialog())
		return false;

	if (!empty(dialogId)) {
		var rowObj = getItemRow(dialogId,tableId);
		updateOperationRecordToGrid(rowObj,tableId);
		cancelOperationsDialog();
	} else {
		addOperationRecordToGrid(tableId);
		showAddEditOperationDialog();
	}

}
var operationColIndex = 0;
var OP_OPERATION_NAME=operationColIndex++,OP_OPERATION_PRIORITY=operationColIndex++,OP_OPERATION_MODIFIER=operationColIndex++,
	OP_OPERATION_EDIT_CONSUMABLE = operationColIndex++,
	OP_OPERATION_EDIT=operationColIndex++,OP_OPERATION_DELETE=operationColIndex++;
function addOperationRecordToGrid(tableId) {
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var operationName = document.getElementById('d_operation_name').value;
   	var operationId = document.getElementById('d_operation_id').value;
	var operPriorityText = document.getElementById('d_oper_priority').options[document.getElementById('d_oper_priority').selectedIndex].text;
	var operPriorityValue = document.getElementById('d_oper_priority').value;
	var modifier = document.getElementById('d_modifier').value;

	setNodeText(row.cells[OP_OPERATION_NAME], operationName);
	setNodeText(row.cells[OP_OPERATION_PRIORITY], operPriorityText);
	setNodeText(row.cells[OP_OPERATION_MODIFIER], modifier);

	setHiddenValue(id, "operation_id", operationId);
	setHiddenValue(id, "oper_priority", operPriorityValue);
	setHiddenValue(id, "operation_name", operationName);
	setHiddenValue(id, "modifier", modifier);

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", "Edit Operation");
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditOpertaionDialog(this)");
	editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "oDeleteIcon");
	deleteImg.setAttribute("title", "Delete Operation Row");
	deleteImg.setAttribute("onclick","cancelOperations(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");

	for (var i=row.cells[OP_OPERATION_EDIT_CONSUMABLE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_EDIT_CONSUMABLE].removeChild(row.cells[OP_OPERATION_EDIT_CONSUMABLE].childNodes[i]);
	}

	for (var i=row.cells[OP_OPERATION_EDIT].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_EDIT].removeChild(row.cells[OP_OPERATION_EDIT].childNodes[i]);
	}
	row.cells[OP_OPERATION_EDIT].appendChild(editImg);
	for (var i=row.cells[OP_OPERATION_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_DELETE].removeChild(row.cells[OP_OPERATION_DELETE].childNodes[i]);
	}
	row.cells[OP_OPERATION_DELETE].appendChild(deleteImg);
	clearOperationDialog();
	return true;

}

function updateOperationRecordToGrid(row,tableId) {
	var operationName = document.getElementById('d_operation_name').value;
   	var operationId = document.getElementById('d_operation_id').value;
	var operPriorityText = document.getElementById('d_oper_priority').options[document.getElementById('d_oper_priority').selectedIndex].text;
	var operPriorityValue = document.getElementById('d_oper_priority').value;
	var modifier = document.getElementById('d_modifier').value;

	setNodeText(row.cells[OP_OPERATION_NAME], operationName);
	setNodeText(row.cells[OP_OPERATION_PRIORITY], operPriorityText);
	setNodeText(row.cells[OP_OPERATION_MODIFIER], modifier);

	getElementByName(row, "operation_id").value = operationId;
	getElementByName(row, "operation_name").value = operationName;
	getElementByName(row, "oper_priority").value = operPriorityValue;
	getElementByName(row, "modifier").value = modifier;

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", "Edit Operation");
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditOpertaionDialog(this)");
	editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "deleteIcon");
	deleteImg.setAttribute("title", "Delete Operation Row");
	deleteImg.setAttribute("onclick","cancelOperations(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");

	for (var i=row.cells[OP_OPERATION_EDIT].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_EDIT].removeChild(row.cells[OP_OPERATION_EDIT].childNodes[i]);
	}
	row.cells[OP_OPERATION_EDIT].appendChild(editImg);
		for (var i=row.cells[OP_OPERATION_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_OPERATION_DELETE].removeChild(row.cells[OP_OPERATION_DELETE].childNodes[i]);
	}
	row.cells[OP_OPERATION_DELETE].appendChild(deleteImg);
	clearOperationDialog();
	document.getElementById('opDialogId').value = getRowItemIndex(row);


}

function cancelOperations(imgObj,tableId) {
	var table = document.getElementById(tableId);
	var rowObj = getThisRow(imgObj);
	var id = getRowChargeIndex(rowObj);
	var deltedId = rowObj.rowIndex;
	var deleted = getElementByName(rowObj,'op_row_deleted');
	var newRow = getElementByName(rowObj,"op_is_new_row").value;
	var operationName = getElementByName(rowObj,"operation_name").value;

	if (newRow == 'Y') {
		table.deleteRow(deltedId);
	} else {
		if(deleted.value == 'Y') {
			getElementByName(rowObj,"op_row_deleted").value = 'N';
			getElementByName(rowObj,"oDeleteIcon").src = cpath + "/icons/delete.gif";
			rowObj.className = '';
		} else {
			var deleteOp = confirm('Any forms filled for operation \''+ operationName +'\' will be deleted. Do you wish to continue ?');
			if (deleteOp == true) {
				getElementByName(rowObj,"op_row_deleted").value = 'Y';
				getElementByName(rowObj,"oDeleteIcon").src = cpath + "/icons/delete_disabled.gif";
				rowObj.className = 'deletedRow';
			} else {
				getElementByName(rowObj,"op_row_deleted").value = 'N';
				getElementByName(rowObj,"oDeleteIcon").src = cpath + "/icons/delete.gif";
				rowObj.className = '';
			}
		}
	}
}


function validateOperationDialog() {
	var opPriority = document.getElementById('d_oper_priority');
	var opId = document.getElementById('d_operation_id');
	var opName = document.getElementById('d_operation_name');
	if(empty(opPriority.value)) {
		alert("Operation Priority is required.");
		opPriority.focus();
		return false;
	}

	if(empty(opName.value)) {
		alert("Operation Name is required.");
		opName.focus();
		return false;
	}

	return true;
}

function clearOperationDialog() {
	document.getElementById('d_operation_name').value = '';
	document.getElementById('d_operation_id').value = '';
	document.getElementById('d_oper_priority').value = '';
	document.getElementById('d_modifier').value = '';
	document.getElementById('opDialogId').value = '';
}

function cancelOperationsDialog() {
	clearOperationDialog();
	addOperationsDialog.cancel();
}

function showAddEditOperationDialog() {
	var button = document.getElementById("btnOperationAddItem");
	addOperationsDialog.cfg.setProperty('context', [button, 'tr', 'tl'], false);
	document.getElementById('d_operation_name').disabled = false;
	document.getElementById('d_oper_priority').disabled = false;
	addOperationsDialog.show();
}

var oAutoComp;
function initOperationsAutoComolete() {
	var datasource = new YAHOO.util.LocalDataSource({result: operationsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "operation_name"},{key : "operation_code"},{key : "op_id"} ],
		numMatchFields: 2
	};

	var oAutoComp = new YAHOO.widget.AutoComplete('d_operation_name','d_operation_name_container', datasource);
	oAutoComp.minQueryLength = 0;
 	oAutoComp.maxResultsDisplayed = 20;
 	oAutoComp.forceSelection = true ;
 	oAutoComp.animVert = false;
 	oAutoComp.resultTypeList = false;
 	oAutoComp.typeAhead = false;
 	oAutoComp.allowBroserAutocomplete = false;
 	oAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	oAutoComp.autoHighlight = true;
	oAutoComp.useShadow = false;
	oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
 	if (oAutoComp._elTextbox.value != '') {
			oAutoComp._bItemSelected = true;
			oAutoComp._sInitInputValue = oAutoComp._elTextbox.value;
	}
	oAutoComp.itemSelectEvent.subscribe(setOperationId);
	oAutoComp.selectionEnforceEvent.subscribe(clearOperationId);
}

function setOperationId(oSelf, sArgs) {
	document.getElementById("d_operation_id").value = sArgs[2].op_id;
}

function clearOperationId(oSelf, sClearedValue) {
	document.getElementById("d_operation_id").value = '';
}

// dialog related methods for surgeon dialog.
function validateSurgeonDialog() {
	var surgeonSpec = document.getElementById('d_surgeon_speciality');
	var surgeonName = document.getElementById('d_surgeon_name');
	var surgeonId = document.getElementById('d_surgeon_id');
	if(empty(surgeonSpec.value)) {
		alert("speciality is required.");
		surgeonSpec.focus();
		return false;
	}

	if(empty(surgeonId.value)) {
		alert("Surgeon Name is required.");
		surgeonName.focus();
		return false;
	}

	return true;
}

function clearSurgeonDialog() {
	document.getElementById('d_surgeon_name').value = '';
	document.getElementById('d_surgeon_id').value = '';
	document.getElementById('d_surgeon_speciality').value = '';
}

function cancelSurgeonDialog() {
	clearSurgeonDialog();
	addSurgeonsDialog.cancel();
}

function showAddSurgeonsDialog() {
	var button = document.getElementById("btnSurgeonAddItem");
	addSurgeonsDialog.cfg.setProperty('context', [button, 'tr', 'tl'], false);
	document.getElementById('d_surgeon_speciality').focus();
	addSurgeonsDialog.show();
}

function addSurgeonRecord() {
	var tableId = "surgeonDetailsTable";
	var id = getNumCharges(tableId);

	if(!validateSurgeonDialog())
		return false;

	addSurgeonRecordToGrid(tableId);
	showAddSurgeonsDialog();

}
var surgeonColIndex = 0;
var OP_SURGEON_NAME=surgeonColIndex++,OP_SURGEON_SPECIALITY=surgeonColIndex++,OP_SURGEON_EMPTY_ROW=surgeonColIndex++,
OP_SURGEON_DELETE=surgeonColIndex++;
function addSurgeonRecordToGrid(tableId) {
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var specialityText = document.getElementById('d_surgeon_speciality').options[document.getElementById('d_surgeon_speciality').selectedIndex].text;
   	var specialityValue = document.getElementById('d_surgeon_speciality').value;
   	var surgeonId = document.getElementById('d_surgeon_id').value;
	var surgeonName = document.getElementById('d_surgeon_name').value;

	setNodeText(row.cells[OP_SURGEON_NAME], surgeonName);
	setNodeText(row.cells[OP_SURGEON_SPECIALITY], specialityText);

	setHiddenValue(id, "su_resource_id", surgeonId);
	setHiddenValue(id, "su_operation_speciality", specialityValue);

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "suDeleteIcon");
	deleteImg.setAttribute("id", "suDeleteIcon");
	deleteImg.setAttribute("title", "Delete Surgeon Row");
	deleteImg.setAttribute("onclick","cancelSurgeons(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");


	for (var i=row.cells[OP_SURGEON_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_SURGEON_DELETE].removeChild(row.cells[OP_SURGEON_DELETE].childNodes[i]);
	}
	row.cells[OP_SURGEON_DELETE].appendChild(deleteImg);
	clearSurgeonDialog();
	return true;

}

function cancelSurgeons(imgObj,tableId) {
	var table = document.getElementById(tableId);
	var rowObj = getThisRow(imgObj);
	var id = getRowChargeIndex(rowObj);
	var deltedId = rowObj.rowIndex;
	var deleted = getElementByName(rowObj,'su_row_deleted');
	var newRow = getElementByName(rowObj,"su_is_new_row").value;

	if (newRow == 'Y') {
		table.deleteRow(deltedId);
	} else {
		if(deleted.value == 'Y') {
			getElementByName(rowObj,"su_row_deleted").value = 'N';
			getElementByName(rowObj,"suDeleteIcon").src = cpath + "/icons/delete.gif";
			rowObj.className = '';
		} else {
			getElementByName(rowObj,"su_row_deleted").value = 'Y';
			getElementByName(rowObj,"suDeleteIcon").src = cpath + "/icons/delete_disabled.gif";
			rowObj.className = 'deletedRow';
		}
	}
}


var sAutoComp;
function initSurgeonsAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result: surgeonsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};
	var sAutoComp = new YAHOO.widget.AutoComplete('d_surgeon_name','d_surgeon_name_container', datasource);
	sAutoComp.minQueryLength = 0;
 	sAutoComp.maxResultsDisplayed = 20;
 	sAutoComp.forceSelection = true ;
 	sAutoComp.animVert = false;
 	sAutoComp.resultTypeList = false;
 	sAutoComp.typeAhead = false;
 	sAutoComp.allowBroserAutocomplete = false;
 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	sAutoComp.autoHighlight = true;
	sAutoComp.useShadow = false;
 	if (sAutoComp._elTextbox.value != '') {
			sAutoComp._bItemSelected = true;
			sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
	}
	sAutoComp.itemSelectEvent.subscribe(setSurgeonId);
	sAutoComp.selectionEnforceEvent.subscribe(clearSurgeonId);
}

function setSurgeonId(oSelf, sArgs) {
	document.getElementById("d_surgeon_id").value = sArgs[2].doctor_id;
}

function clearSurgeonId(oSelf, sClearedValue) {
	document.getElementById("d_surgeon_id").value = '';
}

// dialog related methods for anaesthetists dialog.
function validateAnaesthetistsDialog() {
	var speciality = document.getElementById('d_anaesthetists_speciality');
	var anaesthetistsName = document.getElementById('d_anaesthetists_name');
	var anaesthetistsId = document.getElementById('d_anaesthetists_id');
	if(empty(speciality.value)) {
		alert("speciality is required.");
		speciality.focus();
		return false;
	}

	if(empty(anaesthetistsId.value)) {
		alert("Anaesthetists Name is required.");
		anaesthetistsName.focus();
		return false;
	}

	return true;
}

function clearAnaesthetistsDialog() {
	document.getElementById('d_anaesthetists_name').value = '';
	document.getElementById('d_anaesthetists_id').value = '';
	document.getElementById('d_anaesthetists_speciality').value = '';
}

function cancelAnaesthetistsDialog() {
	clearAnaesthetistsDialog();
	addAnaesthetistsDialog.cancel();
}

function showAddAnaesthetistsDialog() {
	var button = document.getElementById("btnAnaesthetistsAddItem");
	addAnaesthetistsDialog.cfg.setProperty('context', [button, 'tr', 'tl'], false);
	document.getElementById('d_anaesthetists_speciality').focus();
	addAnaesthetistsDialog.show();
}

function addAnaesthetistsRecord() {
	var tableId = "anaesthetistDetailsTable";
	var id = getNumCharges(tableId);

	if(!validateAnaesthetistsDialog())
		return false;

	addAnaesthetistsRecordToGrid(tableId);
	showAddAnaesthetistsDialog();

}
var anaesthetistsColIndex = 0;
var OP_ANAESTHETISTS_NAME=anaesthetistsColIndex++,OP_ANAESTHETISTS_SPECIALITY=anaesthetistsColIndex++,OP_ANAESTHETISTS_EMPTY_ROW=anaesthetistsColIndex++,
OP_ANAESTHETISTS_DELETE=anaesthetistsColIndex++;
function addAnaesthetistsRecordToGrid(tableId) {
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var specialityText = document.getElementById('d_anaesthetists_speciality').options[document.getElementById('d_anaesthetists_speciality').selectedIndex].text;
   	var specialityValue = document.getElementById('d_anaesthetists_speciality').value;
   	var anaesthetistsId = document.getElementById('d_anaesthetists_id').value;
	var anaesthetistsName = document.getElementById('d_anaesthetists_name').value;

	setNodeText(row.cells[OP_ANAESTHETISTS_NAME], anaesthetistsName);
	setNodeText(row.cells[OP_ANAESTHETISTS_SPECIALITY], specialityText);

	setHiddenValue(id,"an_resource_id",anaesthetistsId) ;
	setHiddenValue(id,"an_operation_speciality",specialityValue) ;

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "anDeleteIcon");
	deleteImg.setAttribute("id", "anDeleteIcon");
	deleteImg.setAttribute("title", "Delete Surgeon Row");
	deleteImg.setAttribute("onclick","cancelAnaesthetists(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");


	for (var i=row.cells[OP_ANAESTHETISTS_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_ANAESTHETISTS_DELETE].removeChild(row.cells[OP_ANAESTHETISTS_DELETE].childNodes[i]);
	}
	row.cells[OP_ANAESTHETISTS_DELETE].appendChild(deleteImg);
	clearAnaesthetistsDialog();
	return true;

}

function cancelAnaesthetists(imgObj,tableId) {
	var table = document.getElementById(tableId);
	var rowObj = getThisRow(imgObj);
	var id = getRowChargeIndex(rowObj);
	var deltedId = rowObj.rowIndex;
	var deleted = getElementByName(rowObj,'an_row_deleted');
	var newRow = getElementByName(rowObj,"an_is_new_row").value;

	if (newRow == 'Y') {
		table.deleteRow(deltedId);
	} else {
		if(deleted.value == 'Y') {
			getElementByName(rowObj,"an_row_deleted").value = 'N';
			getElementByName(rowObj,"anDeleteIcon").src = cpath + "/icons/delete.gif";
			rowObj.className = '';
		} else {
			getElementByName(rowObj,"an_row_deleted").value = 'Y';
			getElementByName(rowObj,"anDeleteIcon").src = cpath + "/icons/delete_disabled.gif";
			rowObj.className = 'deletedRow';
		}
	}
}


var aAutoComp;
function initAnaesthetistsAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result: anaesthetistsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};
	var aAutoComp = new YAHOO.widget.AutoComplete('d_anaesthetists_name','d_anaesthetists_name_container', datasource);
	aAutoComp.minQueryLength = 0;
 	aAutoComp.maxResultsDisplayed = 20;
 	aAutoComp.forceSelection = true ;
 	aAutoComp.animVert = false;
 	aAutoComp.resultTypeList = false;
 	aAutoComp.typeAhead = false;
 	aAutoComp.allowBroserAutocomplete = false;
 	aAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	aAutoComp.autoHighlight = true;
	aAutoComp.useShadow = false;
 	if (aAutoComp._elTextbox.value != '') {
			aAutoComp._bItemSelected = true;
			aAutoComp._sInitInputValue = aAutoComp._elTextbox.value;
	}
	aAutoComp.itemSelectEvent.subscribe(setAnaesthetistsId);
	aAutoComp.selectionEnforceEvent.subscribe(clearAnaesthetistsId);
}

function setAnaesthetistsId(oSelf, sArgs) {
	document.getElementById("d_anaesthetists_id").value = sArgs[2].doctor_id;
}

function clearAnaesthetistsId(oSelf, sClearedValue) {
	document.getElementById("d_anaesthetists_id").value = '';
}

var paedAutoComp;
function initPaediatricianAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result: surgeonsJson});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};
	var paedAutoComp = new YAHOO.widget.AutoComplete('d_paediatrician_name','d_paediatrician_name_container', datasource);
	paedAutoComp.minQueryLength = 0;
 	paedAutoComp.maxResultsDisplayed = 20;
 	paedAutoComp.forceSelection = true ;
 	paedAutoComp.animVert = false;
 	paedAutoComp.resultTypeList = false;
 	paedAutoComp.typeAhead = false;
 	paedAutoComp.allowBroserAutocomplete = false;
 	paedAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	paedAutoComp.autoHighlight = true;
	paedAutoComp.useShadow = false;
 	if (paedAutoComp._elTextbox.value != '') {
			paedAutoComp._bItemSelected = true;
			paedAutoComp._sInitInputValue = aAutoComp._elTextbox.value;
	}
	paedAutoComp.itemSelectEvent.subscribe(setPaediatricianId);
	paedAutoComp.selectionEnforceEvent.subscribe(clearPaediatricianId);
}

function setPaediatricianId(oSelf, sArgs) {
	document.getElementById("d_paediatrician_id").value = sArgs[2].doctor_id;
}

function clearPaediatricianId(oSelf, sClearedValue) {
	document.getElementById("d_paediatrician_id").value = '';
}


function validatePaediatricianDialog() {
	var paediatricianName = document.getElementById('d_paediatrician_name');
	var paediatricianId = document.getElementById('d_paediatrician_id');

	if(empty(paediatricianId.value)) {
		alert("Paediatrician Name is required.");
		paediatricianName.focus();
		return false;
	}
	return true;
}

function clearPaediatricianDialog() {
	document.getElementById('d_paediatrician_name').value = '';
	document.getElementById('d_paediatrician_id').value = '';
}

function cancelPaediatricianDialog() {
	clearPaediatricianDialog();
	addPaediatricianDialog.cancel();
}

function showAddPaediatricianDialog() {
	var button = document.getElementById("btnPaediatricianAddItem");
	addPaediatricianDialog.cfg.setProperty('context', [button, 'tr', 'tl'], false);
	document.getElementById('d_paediatrician_name').focus();
	addPaediatricianDialog.show();
}

function validateAnaesthesiaTypeDialog() {
	var anaesthesiaType = document.getElementById('d_anaesthesia_type');
	var anaesthesiaTypeFromDate = document.getElementById('d_anes_start_date');
	var anaesthesiaTypeEndDate = document.getElementById('d_anes_end_date');
	var anaesthesiaTypeFromTime = document.getElementById('d_anes_start_time');
	var anaesthesiaTypeEndTime = document.getElementById('d_anes_end_time');

	if(empty(anaesthesiaType.value)) {
		alert("Anaesthesia Type is required.");
		anaesthesiaType.focus();
		return false;
	}
	if(empty(anaesthesiaTypeFromDate.value)) {
		alert("From Date is required.");
		anaesthesiaTypeFromDate.focus();
		return false;
	}
	if(empty(anaesthesiaTypeEndDate.value)) {
		alert("End Date is required.");
		anaesthesiaTypeEndDate.focus();
		return false;
	}
	if(empty(anaesthesiaTypeFromTime.value)) {
		alert("From Time is required.");
		anaesthesiaTypeFromTime.focus();
		return false;
	}
	if(empty(anaesthesiaTypeEndTime.value)) {
		alert("End Time is required.");
		anaesthesiaTypeEndTime.focus();
		return false;
	}

	var anStartDateTime = getDateTime(anaesthesiaTypeFromDate.value,anaesthesiaTypeFromTime.value);
	var anEndDateTime = getDateTime(anaesthesiaTypeEndDate.value,anaesthesiaTypeEndTime.value);

	if (anEndDateTime < anStartDateTime) {
		alert("Anaesthesia End Date can not be less than Anaesthesia Start Date.");
		anaesthesiaTypeFromDate.focus();
		return false;
	}
	return true;
}

function clearAnaesthesiaTypeDialog() {
	document.getElementById('d_anaesthesia_type').value = '';
}

function cancelAnaesthesiaTypeDialog() {
	clearAnaesthesiaTypeDialog();
	addAnaesthesiaTypeDialog.cancel();
}

function showAddAnaesthesiaTypeDialog(obj){
	var opStartDate = document.getElementById('surgery_start_date');
	var opStartTime =document.getElementById('surgery_start_time');
	var opEndDate = document.getElementById('surgery_end_date');
	var opEndTime =document.getElementById('surgery_end_time');
	addAnaesthesiaTypeDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);

	document.getElementById('d_anaesthesia_type').focus();

	document.getElementById('d_anes_start_date').value = empty(opStartDate.value) ?
		formatDate(getServerDate(),'ddmmyyyy','-') : formatDate(parseDateStr(opStartDate.value),'ddmmyyyy','-');

	document.getElementById('d_anes_start_time').value = empty(opStartTime.value) ?
		formatTime(getServerDate()) : opStartTime.value;

	document.getElementById('d_anes_end_date').value = empty(opEndDate.value) ?
		formatDate(getServerDate(),'ddmmyyyy','-') : formatDate(parseDateStr(opEndDate.value),'ddmmyyyy','-');

	document.getElementById('d_anes_end_time').value = empty(opEndTime.value) ?
		formatTime(getServerDate()) : opEndTime.value;

	addAnaesthesiaTypeDialog.show();
}

function addAnaesthesiaTypeRecord() {
	var tableId = "anaesthesiaDetailsTable";
	var id = getNumCharges(tableId);

	if(!validateAnaesthesiaTypeDialog())
		return false;

	addAnaesthesiaTypeRecordToGrid(tableId);
	addAnaesthesiaTypeDialog.align("tr", "tl");
}

function addPaediatricianRecord() {
	var tableId = "paediatricianDetailsTable";
	var id = getNumCharges(tableId);

	if(!validatePaediatricianDialog())
		return false;

	addPaediatricianRecordToGrid(tableId);
	showAddPaediatricianDialog();

}
var paediatricianColIndex = 0;
var OP_PAEDIATRICIAN_NAME=paediatricianColIndex++,OP_PAEDIATRICIAN_SPECIALITY=paediatricianColIndex++,OP_PAEDIATRICIAN_EMPTY_ROW=paediatricianColIndex++,
OP_PAEDIATRICIAN_DELETE=paediatricianColIndex++;
function addPaediatricianRecordToGrid(tableId) {
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var specialityValue = "PAED";
   	var paediatricianId = document.getElementById('d_paediatrician_id').value;
	var paediatricianName = document.getElementById('d_paediatrician_name').value;

	setNodeText(row.cells[OP_PAEDIATRICIAN_NAME], paediatricianName);
	setNodeText(row.cells[OP_SURGEON_SPECIALITY], "Doctor");

	setHiddenValue(id, "paed_resource_id", paediatricianId);
	setHiddenValue(id, "paed_operation_speciality", specialityValue);

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "paedDeleteIcon");
	deleteImg.setAttribute("id", "paedDeleteIcon");
	deleteImg.setAttribute("title", "Delete Paediatrician Row");
	deleteImg.setAttribute("onclick","cancelPaediatrician(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");


	for (var i=row.cells[OP_PAEDIATRICIAN_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_PAEDIATRICIAN_DELETE].removeChild(row.cells[OP_PAEDIATRICIAN_DELETE].childNodes[i]);
	}
	row.cells[OP_PAEDIATRICIAN_DELETE].appendChild(deleteImg);
	clearPaediatricianDialog();
	return true;

}

function cancelPaediatrician(imgObj,tableId) {
	var table = document.getElementById(tableId);
	var rowObj = getThisRow(imgObj);
	var id = getRowChargeIndex(rowObj);
	var deltedId = rowObj.rowIndex;
	var deleted = getElementByName(rowObj,'paed_row_deleted');
	var newRow = getElementByName(rowObj,"paed_is_new_row").value;

	if (newRow == 'Y') {
		table.deleteRow(deltedId);
	} else {
		if(deleted.value == 'Y') {
			getElementByName(rowObj,"paed_row_deleted").value = 'N';
			getElementByName(rowObj,"paedDeleteIcon").src = cpath + "/icons/delete.gif";
			rowObj.className = '';
		} else {
			getElementByName(rowObj,"paed_row_deleted").value = 'Y';
			getElementByName(rowObj,"paedDeleteIcon").src = cpath + "/icons/delete_disabled.gif";
			rowObj.className = 'deletedRow';
		}
	}
}

var anaethesiaTypeColIndex = 0;
var OP_ANAESTHESIA_TYPE_NAME=anaethesiaTypeColIndex++,OP_ANAESTHESIA_TYPE_START_DATE=anaethesiaTypeColIndex++,OP_ANAESTHESIA_TYPE_END_DATE=anaethesiaTypeColIndex++,
OP_ANAESTHESIA_TYPE_EMPTY_ROW=anaethesiaTypeColIndex++,OP_ANAESTHESIA_TYPE_DELETE=anaethesiaTypeColIndex++;

function addAnaesthesiaTypeRecordToGrid(tableId) {
	var id = getNumCharges(tableId);
   	var table = document.getElementById(tableId);
	var templateRow = table.rows[getTemplateRow(tableId)];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var specialityValue = "PAED";
   	var anaethesiaTypeId = document.getElementById('d_anaesthesia_type').value;
   	var anaethesiaTypeFrom = formatDate(parseDateStr(document.getElementById('d_anes_start_date').value))+ " "+document.getElementById('d_anes_start_time').value;
   	var anaethesiaTypeTo = formatDate(parseDateStr(document.getElementById('d_anes_end_date').value))+ " "+document.getElementById('d_anes_end_time').value;
	var anaethesiaTypeName = document.getElementById('d_anaesthesia_type').options[document.getElementById('d_anaesthesia_type').selectedIndex].text;

	setNodeText(row.cells[OP_PAEDIATRICIAN_NAME], anaethesiaTypeName);
	setNodeText(row.cells[OP_ANAESTHESIA_TYPE_START_DATE], anaethesiaTypeFrom);
	setNodeText(row.cells[OP_ANAESTHESIA_TYPE_END_DATE], anaethesiaTypeTo);

	setHiddenValue(id, "an_type_anaesthesia_type_id", anaethesiaTypeId);
	setHiddenValue(id, "an_type_anaesthesia_type_from", anaethesiaTypeFrom);
	setHiddenValue(id, "an_type_anaesthesia_type_to", anaethesiaTypeTo);

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/delete.gif");
	deleteImg.setAttribute("name", "anTypeDeleteIcon");
	deleteImg.setAttribute("id", "anTypeDeleteIcon");
	deleteImg.setAttribute("title", "Delete Anaesthesia Type Row");
	deleteImg.setAttribute("onclick","cancelAnaesthesiaType(this,'"+tableId+"')");
	deleteImg.setAttribute("class", "button");


	for (var i=row.cells[OP_ANAESTHESIA_TYPE_DELETE].childNodes.length-1; i>=0; i--) {
		row.cells[OP_ANAESTHESIA_TYPE_DELETE].removeChild(row.cells[OP_ANAESTHESIA_TYPE_DELETE].childNodes[i]);
	}
	row.cells[OP_ANAESTHESIA_TYPE_DELETE].appendChild(deleteImg);
	clearAnaesthesiaTypeDialog();
	return true;
}

function cancelAnaesthesiaType(imgObj,tableId) {
	var table = document.getElementById(tableId);
	var rowObj = getThisRow(imgObj);
	var id = getRowChargeIndex(rowObj);
	var deltedId = rowObj.rowIndex;
	var deleted = getElementByName(rowObj,'an_type_row_deleted');
	var newRow = getElementByName(rowObj,"an_type_is_new_row").value;

	if (newRow == 'Y') {
		table.deleteRow(deltedId);
	} else {
		if(deleted.value == 'Y') {
			getElementByName(rowObj,"an_type_row_deleted").value = 'N';
			getElementByName(rowObj,"anTypeDeleteIcon").src = cpath + "/icons/delete.gif";
			rowObj.className = '';
		} else {
			getElementByName(rowObj,"an_type_row_deleted").value = 'Y';
			getElementByName(rowObj,"anTypeDeleteIcon").src = cpath + "/icons/delete_disabled.gif";
			rowObj.className = 'deletedRow';
		}
	}
}


function validateOperationBillableItems() {
	var prescribedBy = document.getElementById('prescribing_doctorId');
	var theater = document.getElementById('theatre_id');
	var chargeType  = document.getElementById('charge_type');
//	var anaesthesiaType = document.getElementById('anaesthesia_type');
	var surgeryStartDate = document.getElementById('surgery_start_date');
	var surgeryStartTime = document.getElementById('surgery_start_time');
	var surgeryEndDate = document.getElementById('surgery_end_date');
	var surgeryEndTime= document.getElementById('surgery_end_time');
	var surgeryTable = document.getElementById('procedureDetailsTable');
	var surgeonTable = document.getElementById('surgeonDetailsTable');
	var anaesthesiaTable = document.getElementById('anaesthetistDetailsTable');
	var surgeryTableLen = surgeryTable.rows.length -2;
	var surgeonTableLen = surgeonTable.rows.length -2;
	var anaesthesiaTableLen = anaesthesiaTable.rows.length -2;
	var surgeryDeleted = document.getElementsByName('op_row_deleted');
	var surgeonDeleted = document.getElementsByName('su_row_deleted');
	var anaesthesiaDeleted = document.getElementsByName('an_row_deleted');
	var operPriority = document.getElementsByName('oper_priority');
	var surgeonSpeciality = document.getElementsByName('su_operation_speciality');

	if(surgeryTableLen < 1) {
		alert("please add a primary surgery to proceed.");
		return false;
	} else {
		var primarySurgeryIndex = 0;
		var primarySurgeryCount = 0;
		var primarySurgeryExists = false;
		for(var i=0;i<surgeryDeleted.length;i++) {
			if(operPriority[i].value == 'P') {
				primarySurgeryExists = true;
				primarySurgeryCount++;
				if(surgeryDeleted[i].value == 'Y') {
					primarySurgeryIndex++;
				}
			}
		}

		if(!primarySurgeryExists) {
			alert("please add a primary surgery to proceed.");
			return false;
		}

		if(primarySurgeryCount == primarySurgeryIndex) {
			alert("please add a primary surgery to proceed.");
			return false;
		}
	}

	if(surgeonTableLen < 1) {
		alert("please add a primary surgeon to proceed.");
		return false;
	} else {
		var primarySurgeonIndex = 0;
		var primarySurgeonExists = false;
		var primarySurgeonCount = 0;
		for(var i=0;i<surgeonDeleted.length;i++) {
			if(surgeonSpeciality[i].value == 'SU') {
				primarySurgeonExists = true;
				primarySurgeonCount++;
				if(surgeonDeleted[i].value == 'Y') {
					primarySurgeonIndex++;
				}
			}
		}

		if(!primarySurgeonExists) {
			alert("please add a primary surgeon to proceed.");
			return false;
		}

		if(primarySurgeonCount == primarySurgeonIndex) {
			alert("please add a primary surgeon to proceed.");
			return false;
		}
	}

	if(fixedOtCharges != 'Y' && empty(theater.value)) {
		alert("operation theater is required.");
		theater.focus();
		return false;
	}

	if(!chargeType.disabled && empty(chargeType.value)) {
		alert("charge type is required.");
		chargeType.focus();
		return false;
	}

	if(gPrescDocRequired == "Y" && empty(prescribedBy.value)) {
		alert("prescribed by is required.");
		document.getElementById('prescribing_doctor').focus();
		return false;
	}

	var anaesthesiaIndex = 0;
	if(anaesthesiaTableLen > 0) {
		for(var i=0;i<anaesthesiaDeleted.length;i++) {
			if(anaesthesiaDeleted[i].value == 'Y') {
				anaesthesiaIndex++;
			}
		}
	}

/*	if (!anaesthesiaType.disabled && (anaesthesiaTableLen > 0 && anaesthesiaTableLen != anaesthesiaIndex) && empty(anaesthesiaType.value)) {
		alert("anaesthesia type is required.");
		anaesthesiaType.focus();
		return false;
	}*/

	if(fixedOtCharges != "Y" && empty(surgeryStartDate.value)) {
		alert("surgery start date is required.");
		surgeryStartDate.focus();
		return false;
	}

	if(fixedOtCharges != "Y" && empty(surgeryEndDate.value)) {
		alert("surgery end date is required.");
		surgeryEndDate.focus();
		return false;
	}
	return true;
}

function saveOperationDetails() {
	var specimen = document.getElementById('specimen');
	var conductionRemarks = document.getElementById('conduction_remarks');
	var appointmentId = document.getElementById('appointment_id');
	var operationStatus = document.getElementById('operation_status');
	var surgeryTable = document.getElementById('procedureDetailsTable');
	var surgeryTableLen = surgeryTable.rows.length -2;
	var surgeryDeleted = document.getElementsByName('op_row_deleted');
	var operPriority = document.getElementsByName('oper_priority');
	var cancelReason = document.getElementById('cancel_reason');
	var operationIds = document.getElementsByName('operation_id');
	var opeDeleted = document.getElementsByName('op_row_deleted');

	if(appointmentId) {
		if(appointmentId.options.length == 1) {
			alert("Please book a surgery appointment using the surgery scheduler.");
			return false;
		}else {
			if(empty(appointmentId.value)) {
				alert("please select an scheduled appointment for this patient.");
				appointmentId.focus();
				return false;
			}
		}
	}

	if(!validateDateAndTime())
		return false;

	if(operationStatus.value == 'C') {
		if(!validateOperationBillableItems())
			return false;
	}

	if(!empty(addedToBill) && addedToBill == "Y" && operationStatus.value == 'X') {
		alert("can not cancel an ordered operation. ");
		operationStatus.focus();
		return false;
	}

	if(operationStatus.value == 'X' && empty(cancelReason.value)) {
		alert("please enter valid reason for cancel the operation.");
		cancelReason.focus();
		return false;
	}

	if(!imposeMaxLength(specimen,"speciman"))
		return false;

	if(!imposeMaxLength(conductionRemarks,"conduction remarks"))
		return false;

	if(operationStatus.value == 'X' && !imposeMaxLength(cancelReason,"cancel reason"))
		return false;


	if(surgeryTableLen > 0) {
		var primarySurgeryIndex = 0;
		for(var i=0;i<surgeryDeleted.length;i++) {
			if(operPriority[i].value == 'P' && surgeryDeleted[i].value == 'N') {
				primarySurgeryIndex++;
			}
		}
		if(primarySurgeryIndex > 1) {
			alert("more than one primary surgery is not allowed");
			return false;
		}
	}

/*	for(var i=0;i<operationIds.length;i++) {
		if(!empty(operationIds[i].value) && opeDeleted[i].value == 'N') {
			var index = 0;
			for(var j=0;j<operationIds.length;j++) {
				if(opeDeleted[j].value == 'N' && operationIds[i].value == operationIds[j].value) {
					index++;
					if(index > 1) {
						alert("duplicate operations are not allowed.");
						return false;
					}
				}
			}
		}
	}*/
	document.getElementById("theatre_id").disabled = false;
	return true;
}

function validateDateAndTime() {
	var wheelInDate = document.getElementById('wheel_in_date');
	var wheelOutDate = document.getElementById('wheel_out_date');
/*	var anaesthesiaStartDate = document.getElementById('anaesthesia_start_date');
	var anaesthesiaEndDate = document.getElementById('anaesthesia_end_date');*/
	var operationStartDate = document.getElementById('surgery_start_date');
	var operationEndDate = document.getElementById('surgery_end_date');

	var wheelInTime = document.getElementById('wheel_in_time');
	var wheelOutTime = document.getElementById('wheel_out_time');
/*	var anaesthesiaStartTime = document.getElementById('anaesthesia_start_time');
	var anaesthesiaEndTime = document.getElementById('anaesthesia_end_time');*/
	var operationStartTime = document.getElementById('surgery_start_time');
	var operationEndTime = document.getElementById('surgery_end_time');

	var wheelamsttime = wheelInTime.value.split(":");
	var wheelamendtime = wheelOutTime.value.split(":");
/*	var anaesthesiaamsttime = anaesthesiaStartTime.value.split(":");
	var anaesthesiaamendtime = anaesthesiaEndTime.value.split(":");*/
	var surgeryamsttime = operationStartTime.value.split(":");
	var surgeryamendtime = operationEndTime.value.split(":");

	var parsedwheelInDate = parseDateStr(wheelInDate.value);
	var parsedwheelOutDate = parseDateStr(wheelOutDate.value);
/*	var parsedanaesthesiaStartDate = parseDateStr(anaesthesiaStartDate.value);
	var parsedanaesthesiaEndDate = parseDateStr(anaesthesiaEndDate.value);*/
	var parsedoperationStartDate = parseDateStr(operationStartDate.value);
	var parsedoperationEndDate = parseDateStr(operationEndDate.value);

	if(!doValidateDateField(wheelInDate))
			return false;

	if(!doValidateDateField(wheelOutDate))
		return false;

/*	if(!doValidateDateField(anaesthesiaStartDate))
			return false;

	if(!doValidateDateField(anaesthesiaEndDate))
		return false;*/

	if(!doValidateDateField(operationStartDate))
			return false;

	if(!doValidateDateField(operationEndDate))
		return false;

	if(!empty(wheelInTime.value)) {
		if (!validateTime(wheelInTime)) return false;
	}

	if (!empty(wheelOutTime.value)) {
		if (!validateTime(wheelOutTime)) return false;
	}

/*	if(!empty(anaesthesiaStartTime.value)) {
		if (!validateTime(anaesthesiaStartTime)) return false;
	}

	if (!empty(anaesthesiaEndTime.value)) {
		if (!validateTime(anaesthesiaEndTime)) return false;
	}*/

	if(!empty(operationStartTime.value)) {
		if (!validateTime(operationStartTime)) return false;
	}

	if(!empty(operationEndTime.value)) {
		if (!validateTime(operationEndTime)) return false;
	}


	if(!empty(wheelInDate.value) && empty(wheelInTime.value)) {
		alert("wheel in time is required");
		wheelInTime.focus();
		return false;
	}

	if(!empty(wheelOutDate.value) && empty(wheelOutTime.value)) {
		alert("wheel out time is required");
		wheelOutTime.focus();
		return false;
	}

/*	if(!empty(anaesthesiaStartDate.value) && empty(anaesthesiaStartTime.value)) {
		alert("anaesthesia start time is required");
		anaesthesiaStartTime.focus();
		return false;
	}

	if(!empty(anaesthesiaEndDate.value) && empty(anaesthesiaEndTime.value)) {
		alert("anaesthesia end time is required");
		anaesthesiaEndTime.focus();
		return false;
	}?*/

	if(!empty(operationStartDate.value) && empty(operationStartTime.value)) {
		alert("surgery start time is required");
		operationStartTime.focus();
		return false;
	}


	if(!empty(operationEndDate.value) && empty(operationEndTime.value)) {
		alert("surgery end time is required");
		operationEndTime.focus();
		return false;
	}

	if(!empty(wheelInTime.value) && empty(wheelInDate.value)) {
		alert("wheel in date is required");
		wheelInDate.focus();
		return false;
	}

	if(!empty(wheelOutTime.value) && empty(wheelOutDate.value)) {
		alert("wheel out date is required");
		wheelOutDate.focus();
		return false;
	}

/*	if(!empty(anaesthesiaStartTime.value) && empty(anaesthesiaStartDate.value)) {
		alert("anaesthesia start date is required");
		anaesthesiaStartDate.focus();
		return false;
	}

	if(!empty(anaesthesiaEndTime.value) && empty(anaesthesiaEndDate.value)) {
		alert("anaesthesia end date is required");
		anaesthesiaEndDate.focus();
		return false;
	}*/

	if(!empty(operationStartTime.value) && empty(operationStartDate.value)) {
		alert("surgery start date is required");
		operationStartDate.focus();
		return false;
	}


	if(!empty(operationEndTime.value) && empty(operationEndDate.value)) {
		alert("surgery end date is required");
		operationEndDate.focus();
		return false;
	}

	var regDateTime = getDateTime(regDate,regTime);
	var surgeryStartDateTime = getDateTime(operationStartDate.value,operationStartTime.value);

	if (fixedOtCharges != "Y" && !empty(addedToBill) && addedToBill == "N" && surgeryStartDateTime < regDateTime) {
		alert("surgery start date and time can not be less than admit date and time.");
		operationStartDate.focus();
		return false;
	}

	var parsedWheelInDateAndTime = getDateTime(wheelInDate.value,wheelInTime.value);
	var parsedWheelOutDateAndTime = getDateTime(wheelOutDate.value,wheelOutTime.value);
/*	var parsedanaesthesiaStartDateAndTime = getDateTime(anaesthesiaStartDate.value,anaesthesiaStartTime.value);
	var parsedanaesthesiaEndDateAndTime = getDateTime(anaesthesiaEndDate.value,anaesthesiaEndTime.value);*/
	var parsedSurgeryStartDateAndTime = getDateTime(operationStartDate.value,operationStartTime.value);
	var parsedSurgeryEndDateAndTime = getDateTime(operationEndDate.value,operationEndTime.value);

	if(!empty(parsedWheelInDateAndTime) && !empty(parsedWheelOutDateAndTime)) {
		if(parsedWheelOutDateAndTime.getTime() <= parsedWheelInDateAndTime.getTime()) {
			alert("wheel-in date and time can not be equal to or greater than wheel-end date and time.");
			return false;
		}
	}

/*	if(!empty(parsedanaesthesiaStartDateAndTime) && !empty(parsedanaesthesiaEndDateAndTime)) {
		if(parsedanaesthesiaEndDateAndTime.getTime() <= parsedanaesthesiaStartDateAndTime.getTime()) {
			alert("anaesthesia-start date and time can not be equal to or greater than  anaesthesia-end date and time.");
			return false;
		}
	}*/
	if(!empty(parsedSurgeryStartDateAndTime) && !empty(parsedSurgeryEndDateAndTime)) {
		if(parsedSurgeryEndDateAndTime.getTime() <= parsedSurgeryStartDateAndTime.getTime()) {
			alert("surgery-start date and time can not be equal to or greater than surgery-end date and time.");
			return false;
		}
	}

	return true;
}


function getCompleteTime(obj) {
	var time = obj.value;
	if (time != '') {
		if (time.length <= 2) {
			obj.value = time + ":00";
		}
	}
}

function compareTimes(time1, time2) {
	if (eval(time1[0]) <= eval(time2[0])) {} else {
		if (eval(time1[1]) <= eval(time2[1])) {} else {
			return false;
		}
	}
	return true;
}

function imposeMaxLength(obj,text){
	var objDesc = obj.value;
	var newLines = objDesc.split("\n").length;
	var length = objDesc.length + newLines;
	var fixedLen = 2000;
	if (length > fixedLen) {
		alert(text+" can not be more than" +fixedLen +" characters");
		obj.focus();
		return false;
	}
	return true;
}

function validateSave(){
	var priorAuthReq = document.getElementsByName("prior_auth_required");
	var priorAuthNo = document.getElementsByName("prior_auth_id");
	var priorAuthMode = document.getElementsByName("prior_auth_mode_id");

	if(isModAdvanceIns) {
		for(var i=0; i<priorAuthReq.length; i++) {
			if(priorAuthReq[i].value != 'N' && priorAuthReq[i].value!= '') {
				if(priorAuthNo[i].value == ''){
					if(priorAuthReq[i].value == 'A'){
						alert("Prior Auth Number is required... \nPlease Enter the Prior Auth Number");
						return false;
					}else{
						return confirm("Some items may need a prior auth number\n Check plan details for more details");
					}
				}

				if( priorAuthMode[i].value == '' ){
					if(priorAuthReq[i].value == 'A'){
						alert("Prior Auth Mode is required...\nPlease select the Prior Auth Mode");
						return false;
					}else{
						return confirm("Some items may need a prior auth mode\n Check plan details for more details");
					}
				}

			}
		}
	}
	var orderRemarks = document.getElementById("order_remarks");
	var valid = imposeMaxLength(orderRemarks,'Order Remarks');
	if(!valid) return false;
	return true;
}

function showHideCancelReason(obj) {
	if(!empty(obj.value) && obj.value == "X") {
		document.getElementById('cancelReasonRow').style.display = 'table-row';
	} else {
		document.getElementById('cancelReasonRow').style.display = 'none';
	}
}
