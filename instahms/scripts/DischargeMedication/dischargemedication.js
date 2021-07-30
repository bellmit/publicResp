var showRateDetails = false;

function getActiveOnlyPatients() {
	document.patientSearch.ps_status.checked = true;
	document.patientSearch.ps_status.onchange();
}
function init() {
	getActiveOnlyPatients();

	initDoctorAutoComplete();
	initItemDialog();
	initEditItemDialog();
	initFrequencyAutoComplete();
	initInstructionAutoComplete();
	editDialogGeneric();
	ajaxForPrintUrls();
	initPatientAllVisitMedicinesDialog();
	// Display amounts based on action rights and rate plan.
	showRateDetails = displayAmounts();
}

var allVisitPrescMedDialog = null;
function initPatientAllVisitMedicinesDialog() {
	var dialogDiv = document.getElementById("patientAllVisitPrescribedMedicinesDiv");
	if ( dialogDiv )
		dialogDiv.style.display = 'block';
	allVisitPrescMedDialog = new YAHOO.widget.Dialog("patientAllVisitPrescribedMedicinesDiv",
			{	width:"800px",
				context : ["patientAllVisitPrescribedMedicinesDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('ok_btn', 'click', addToTableFromMedicinePrescriptions, allVisitPrescMedDialog, true);
	YAHOO.util.Event.addListener('close_btn', 'click', allVisitPrescMedDialog.cancel, allVisitPrescMedDialog, true);
	subscribeEscKeyListener(allVisitPrescMedDialog);
	allVisitPrescMedDialog.render();
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

function addToTableFromMedicinePrescriptions() {

	var addToChkBoxes = document.getElementsByName('visit_medication_chkbox');
	var itemId = document.getElementsByName('med_item_id');
	var itemName = document.getElementsByName('med_item_name');
   	var adminStrength = document.getElementsByName('med_admin_strength');;
   	var frequency = document.getElementsByName('med_frequency');
   	var remarks = document.getElementsByName('med_item_remarks');
   	var spl_instruction = document.getElementsByName('med_special_instr');
   	var master = document.getElementsByName('med_item_master');
   	var genericCode = document.getElementsByName('med_generic_code');
   	var genericName = document.getElementsByName('med_generic_name');
   	var drugCode = document.getElementsByName('med_drug_code');
   	var consumption_uom = document.getElementsByName('med_consumption_uom');
   	var cons_uom_id = document.getElementsByName('med_cons_uom_id');
   	var routeId = document.getElementsByName('med_route_id');
   	var routeName = document.getElementsByName('med_route_name');
   	routeName = routeId == '' ? '' : routeName;
   	var item_form_id = document.getElementsByName('med_item_form_id');
   	var item_form_name = document.getElementsByName('med_item_form_name');
   	var duration_units =  document.getElementsByName('med_duration_units');
   	var duration = document.getElementsByName('med_duration');
   	var qty = document.getElementsByName('med_medicine_quantity');
   	var item_strength_units = document.getElementsByName('med_item_strength_units');
   	var item_strength = document.getElementsByName('med_item_strength');
   	var granular_unit  = document.getElementsByName('med_granular_units');
   	var strength_unit_name = document.getElementsByName('med_item_strength_unit_name');
   	var strength = document.getElementsByName('med_strength');
	for (var i=0;i<addToChkBoxes.length;i++) {
		if(addToChkBoxes[i].checked) {
			var id = getNumCharges('itemsTable');
		   	var table = document.getElementById("itemsTable");
			var templateRow = table.rows[getTemplateRow('itemsTable')];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
		   	row.id = "itemRow" + id;

		   	setNodeText(row.cells[ITEM_NAME], itemName[i].value, 20);

			setNodeText(row.cells[ADMIN_STRENGTH], adminStrength[i].value, 15);
		  		var details = "";
		  		if (frequency[i].value != '' || duration[i].value != '')
		  			details = frequency[i].value + " / " + duration[i].value + " " + duration_units[i].value;
		  		setNodeText(row.cells[DETAILS], details, 20);
				setNodeText(row.cells[QTY], qty[i].value);
			if (item_form_id != '') {
				setNodeText(row.cells[FORM], item_form_name[i].value, 15);
			}
			setNodeText(row.cells[STRENGTH], item_strength[i].value + ' ' + strength_unit_name[i].value, 15);

			setHiddenValue(id, "generic_code", genericCode[i].value);
			setHiddenValue(id, "drug_code", drugCode[i].value);
			setHiddenValue(id, "admin_strength", adminStrength[i].value);
			setHiddenValue(id, "generic_name", genericName[i].value);
			setHiddenValue(id, "frequency", frequency[i].value);
			setHiddenValue(id, "strength", strength[i].value);
			setHiddenValue(id, "duration", duration[i].value);
			setHiddenValue(id, "duration_units", duration_units[i].value);
			setHiddenValue(id, "medicine_quantity", qty[i].value);
			setHiddenValue(id, "qty_in_stock", "0");
			setHiddenValue(id, "item_form_id", item_form_id[i].value);
			setHiddenValue(id, "granular_units", granular_unit[i].value);
			setHiddenValue(id, "item_strength", item_strength[i].value);
			setHiddenValue(id, "item_strength_units", item_strength_units[i].value);
			setNodeText(row.cells[ROUTE], routeName[i].value);

			setNodeText(row.cells[REMARKS], remarks[i].value, 20);
			setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction[i].value, 20);
			setHiddenValue(id, "item_remarks", remarks[i].value);
			setHiddenValue(id, "special_instr" , spl_instruction[i].value);

			setHiddenValue(id, "cons_uom_id", cons_uom_id[i].value);
			setHiddenValue(id, "item_prescribed_id", "_");
			setHiddenValue(id, "item_name", itemName[i].value);
			setHiddenValue(id, "item_id", itemId[i].value);
			setHiddenValue(id, "drug_code", drugCode[i].value);
			setHiddenValue(id, "item_master", master[i].value);
			setHiddenValue(id, "pkg_size", '');
			setHiddenValue(id, "pkg_price", '');
			setHiddenValue(id, "route_id", routeId[i].value);
			setHiddenValue(id, "route_name", routeName[i].value);
			setHiddenValue(id, "issued", "P");

			itemsAdded++;
			clearFields();
			setRowStyle(id);
		}
	}
	allVisitPrescMedDialog.cancel();
}

function displayAmounts() {
	return (!empty(showChargesAllRatePlan) && showChargesAllRatePlan == 'A');
}

var dosageAutoComplete = null;
var itemAutoComp = null;
var instructionAutoComplete = null;
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

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.dischargemedicationform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function initDoctorAutoComplete() {
	var datasource = new YAHOO.util.LocalDataSource({result : doctorsJson}, {queryMatchContains: true});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},{key : "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('doctor_name','doctorAcDropdown', datasource);
	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.autoHighlight = true;
	autoComp.useIFrame = true;
	autoComp.minQueryLength = 1;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 50;
	autoComp.resultTypeList = false;
	autoComp.forceSelection = true;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.formatResult = Insta.autoHighlight;




	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	autoComp.resultTypeList = false;

 	if (autoComp._elTextbox.value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = autoComp._elTextbox.value;
	}

	autoComp.itemSelectEvent.subscribe(setDoctorId);
	autoComp.selectionEnforceEvent.subscribe(clearDoctorId);
	return autoComp;
}

function setDoctorId(oSelf, sArgs) {
	document.getElementById("doctor_id").value = sArgs[2].doctor_id;
}

function clearDoctorId(oSelf, sClearedValue) {
	document.getElementById("doctor_id").value = '';
}

function initItemDialog() {
	var dialogDiv = document.getElementById("addItemDialog");
	dialogDiv.style.display = 'block';
	addItemDialog = new YAHOO.widget.Dialog("addItemDialog",
			{	width:"680px",
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

function initEditItemDialog() {
	var dialogDiv = document.getElementById("editItemDialog");
	dialogDiv.style.display = 'block';
	editItemDialog = new YAHOO.widget.Dialog("editItemDialog",{
			width:"680px",
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
		var id = document.dischargemedicationform.editRowId.value;
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

	document.dischargemedicationform.editRowId.value = id;

	editItemDialog.show();
	YAHOO.util.Dom.addClass(row, 'editing');

	document.getElementById('ed_itemNameLabel').textContent = getIndexedValue("item_name", id);
	document.getElementById('ed_itemName').value = getIndexedValue("item_name", id);
	document.getElementById('ed_item_id').value = getIndexedValue("item_id", id);
	document.getElementById('ed_drug_code_label').textContent = getIndexedValue("drug_code", id);
	var master = getIndexedValue("item_master", id);
	toggleItemFormRow(false);

	document.getElementById('ed_consumption_uom_label').textContent = getIndexedValue("consumption_uom", id);
	document.getElementById('ed_cons_uom_id').value = getIndexedValue("cons_uom_id", id);

	document.getElementById('ed_cons_uom_id').disabled = !useGenerics;
	document.getElementById('ed_admin_strength').disabled = false;
	document.getElementById('ed_frequency').disabled = false;
	document.getElementById('ed_strength').disabled = false;
	document.getElementById('ed_duration').disabled = false;
	document.getElementById('ed_qty').disabled = false;
	document.getElementById('ed_remarks').disabled = false;


	var allRoutes = useGenerics;
	initEditDosageAutoComplete();
	if (empty(getIndexedValue("route_name", id))) {
		if (allRoutes) {
			document.getElementById('ed_medicine_route').length = 1;
			var len = 2;
			for (var i=0; i<routesListJson.length; i++) {
				document.getElementById('ed_medicine_route').length	= len;
				document.getElementById('ed_medicine_route').options[len-1].value = routesListJson[i].route_id;
				document.getElementById('ed_medicine_route').options[len-1].text = routesListJson[i].route_name;
				len++;
			}
			document.getElementById('ed_medicine_route').selectedIndex = (document.getElementById('ed_medicine_route').length == 2 ? 1 : 0); // if only one route found, then default it.
		} else  {
			var medItemId = getIndexedValue("item_id", id);
			var medItemName = getIndexedValue("item_id", id);
			var itemRoutes = getItemRoutes(medItemId,medItemName);
			if (itemRoutes != null && itemRoutes != undefined) {
				var routes = eval('(' + itemRoutes + ')');
				var routeIds = routes.route_id.split(",");
				var routeNames = routes.route_name.split(",");
				var medicine_route_el = document.getElementById('ed_medicine_route');
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
				medicine_route_el.selectedIndex = medicine_route_el.length == 2 ? 1 : 0;
			}
		}
	} else {
		document.getElementById('ed_medicine_route').length = 2;
		document.getElementById('ed_medicine_route').options[1].value = getIndexedValue("route_id", id);
		document.getElementById('ed_medicine_route').options[1].text = getIndexedValue("route_name", id);
		setSelectedIndex(document.getElementById('ed_medicine_route'),getIndexedValue("route_id", id));
	}
	document.getElementById('ed_strength').value = getIndexedValue("strength", id);
	document.getElementById('ed_frequency').value = getIndexedValue("frequency", id);
	document.getElementById('ed_admin_strength').value = getIndexedValue("admin_strength", id);
	document.getElementById('ed_duration').value = getIndexedValue("duration", id);

	// enable the duration units only if item is not isssued.
	toggleDurationUnits(issued != 'O', 'ed');
	var duration_units = getIndexedValue("duration_units", id);
	var els = document.getElementsByName("ed_duration_units");
	for (var k=0; k<els.length; k++) {
		if (empty(duration_units) && els[k].value == 'D') {
			els[k].checked = true;
			break;
		}
		if (els[k].value == duration_units) {
			els[k].checked = true;
			break;
		}
	}

	document.getElementById('ed_qty').value = getIndexedValue("medicine_quantity", id);
	document.getElementById('genericNameAnchor_editdialog').innerHTML = getIndexedValue("generic_name", id);
	document.getElementById('genericNameAnchor_editdialog').href =
		'javascript:showGenericInfo("", "", "editdialog", "' + getIndexedValue("generic_code", id) + '")';
	document.getElementById('genericNameAnchor_editdialog').style.display = 'block';
	document.getElementById('ed_item_form_id').value = getIndexedValue("item_form_id", id);
	document.getElementById('ed_granular_units').value = getIndexedValue("granular_units", id);
	document.getElementById('ed_item_strength').value = getIndexedValue('item_strength', id);
	document.getElementById('ed_item_strength_units').value = getIndexedValue('item_strength_units', id);

	document.getElementById('ed_package_size').value = getIndexedValue('pkg_size', id);
	document.getElementById('ed_price').value = getIndexedValue('pkg_price', id);
	document.getElementById('ed_remarks').value = getIndexedValue('item_remarks', id);
	document.getElementById('ed_special_instruction').value = getIndexedValue('special_instr', id);
	document.getElementById('ed_item_master').value = getIndexedValue('item_master', id);

	document.getElementById('ed_pkg_size_label').textContent = getIndexedValue("pkg_size", id);
   	document.getElementById('ed_price_label').textContent = getIndexedValue("pkg_price", id);

	document.getElementById('ed_package_size').value = getIndexedValue('pkg_size', id);
	document.getElementById('ed_price').value = getIndexedValue('pkg_price', id);
	document.getElementById('ed_remarks').value = getIndexedValue('item_remarks', id);
	document.getElementById('ed_special_instruction').value = getIndexedValue('special_instr', id);
	document.getElementById('ed_item_master').value = getIndexedValue('item_master', id);

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
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
		document.getElementById('ed_item_form_id').disabled = true;
		document.getElementById('ed_cons_uom_id').disabled = true;
		document.getElementById('ed_special_instruction').disabled = true;
		document.getElementsByName("ed_duration_units")[0].disabled = true;
		document.getElementsByName("ed_duration_units")[1].disabled = true;
		document.getElementsByName("ed_duration_units")[2].disabled = true;
	}

	initEditInstructionAutoComplete('ed');
	document.getElementById('ed_remarks').focus();
	return false;
}

function getItemRoutes(medItemId,medItemName) {
	if (!empty(medItemId)) {
		var url = "./dischargeMedication.do?_method=getItemRoutes"
		url = url + "&item_id=" + medItemId;
		url = url + "&item_name=" + medItemName;
		var reqObject = newXMLHttpRequest();
		reqObject.open("GET", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return reqObject.responseText;
			}
		}
	}
	return null;
}

var editInstructionAutoComplete = null; // remarks autocomplete for edit item dialog.
function initEditInstructionAutoComplete() {
	if (editInstructionAutoComplete == null) {
		ds = new YAHOO.util.LocalDataSource({result : presInstructions});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "instruction_desc"}, ]
		};
		// Instantiate first AutoComplete
		editInstructionAutoComplete = new YAHOO.widget.AutoComplete('ed_remarks', 'ed_remarksContainer', ds);
		editInstructionAutoComplete.minQueryLength = 0;
		editInstructionAutoComplete.allowBrowserAutocomplete = false;
		editInstructionAutoComplete.animVert = false;
		editInstructionAutoComplete.maxResultsDisplayed = 50;
		editInstructionAutoComplete.queryMatchContains = true;
		editInstructionAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
			return Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		};
		editInstructionAutoComplete.resultTypeList = false;
		editInstructionAutoComplete.autoSnapContainer = false;
		if (document.getElementById('ed_remarks').value != '') {
			editInstructionAutoComplete._bItemSelected = true;
			editInstructionAutoComplete._sInitInputValue = document.getElementById('ed_remarks').value;
		}
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
	setAutoGeneratedInstruction('ed');
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
	setAutoGeneratedInstruction('ed');
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
	setAutoGeneratedInstruction('d');
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
	setAutoGeneratedInstruction('d');
}

function setAutoGeneratedInstruction(prefix) {
	if (document.getElementById(prefix+'_granular_units').value == 'Y' ) {
		var instruction = 'USE ';
		var numberOfUnit = document.getElementById(prefix +'_strength').value;
		var granularUnit = document.getElementById(prefix +'_consumption_uom').value;
		var frequency = document.getElementById(prefix +'_frequency').value;
		var duration = document.getElementById(prefix +'_duration').value;

		instruction += empty(numberOfUnit) ? ' ' : numberOfUnit + ' ';
		instruction += empty(granularUnit) ? ' ': granularUnit + ' ';
		instruction += empty(frequency) ? ' ': frequency + ' ';
		instruction += 'FOR A DURATION OF ';
		instruction += empty(duration) ? ' ': duration + ' ';
		if (!empty(duration)) {
			var duration_units_els = document.getElementsByName(prefix +'_duration_units');
			var duration_units = 'D';
			for (var j=0; j<duration_units_els.length; j++) {
				if (duration_units_els[j].checked) {
					duration_units = duration_units_els[j].value;
					break;
				}
			}
			instruction += (duration_units == 'D' ? 'Days.' : (duration_units == 'W' ? 'Weeks.': 'Months.'));
		}
		document.getElementById(prefix +'_remarks').value = instruction;
	}
}

function initInstructionAutoComplete() {
	if (instructionAutoComplete == null) {
		ds = new YAHOO.util.LocalDataSource({result : presInstructions});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "instruction_desc"}, ]
		};
		// Instantiate first AutoComplete
		instructionAutoComplete = new YAHOO.widget.AutoComplete('d_remarks', 'remarksContainer', ds);
		instructionAutoComplete.minQueryLength = 0;
		instructionAutoComplete.allowBrowserAutocomplete = false;
		instructionAutoComplete.animVert = false;
		instructionAutoComplete.maxResultsDisplayed = 50;
		instructionAutoComplete.queryMatchContains = true;
		instructionAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
			return Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		};
		instructionAutoComplete.resultTypeList = false;
		instructionAutoComplete.autoSnapContainer = false;
		if (document.getElementById('d_remarks').value != '') {
			instructionAutoComplete._bItemSelected = true;
			instructionAutoComplete._sInitInputValue = document.getElementById('d_remarks').value;
		}
	}
}

var parentDialog = null;
var childDialog = null;
function showAddItemDialog(obj) {
	var itemType = "Medicine";
	var row = getThisRow(obj);

	addItemDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addItemDialog.show();
	clearFields();
	document.getElementById('d_itemName').focus();
	parentDialog = addItemDialog;
//	document.getElementById('pkg_details_button').style.display = 'none';
	return false;
}

function clearFields() {
	document.getElementById('d_itemName').value = '';
	initItemAutoComplete();
   	clearItemDetails();
   	toggleItemFormRow(true);
}

function clearItemDetails(oSelf) {
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_item_id').value = '';

	var allRoutes = useGenerics;
	document.getElementById('d_medicine_route').length = 1;
	if (allRoutes) {
		var len = 2;
		for (var i=0; i<routesListJson.length; i++) {
			document.getElementById('d_medicine_route').length	= len;
			document.getElementById('d_medicine_route').options[len-1].value = routesListJson[i].route_id;
			document.getElementById('d_medicine_route').options[len-1].text = routesListJson[i].route_name;
			len++;
		}
	}
	document.getElementById('d_medicine_route').selectedIndex = (document.getElementById('d_medicine_route').length == 2 ? 1 : 0); // if only one route found, then default it.

	document.getElementById('d_admin_strength').value = '';
	document.getElementById('d_frequency').value = '';
	document.getElementById('d_strength').value = '';
	document.getElementById('d_duration').value = '';

	var enable = true;
	toggleDurationUnits(enable, 'd');
	if (enable) {
		// disable if it is prescription by brand names for medicines.
		if (!useGenerics) {
			document.getElementById('d_cons_uom_id').disabled = true;
		} else {
			document.getElementById('d_cons_uom_id').disabled = false;
		}
		document.getElementsByName('d_duration_units')[0].checked = true;
		document.getElementById('d_remarks').value = '';
	} else {
		document.getElementById('d_cons_uom_id').disabled = true;
		document.getElementById('d_remarks').value = '';
	}
	document.getElementById('d_qty').value = '';
	document.getElementById('d_special_instruction').value = '';
	document.getElementById('d_cons_uom_id').value = '';
	document.getElementById('d_consumption_uom_label').textContent = '';
	document.getElementById('genericNameAnchor_dialog').style.display = 'none';
	document.getElementById('genericNameAnchor_dialog').href = '';
	document.getElementById('genericNameAnchor_dialog').innerHTML = '';
	document.getElementById('d_generic_code').value = '';
	document.getElementById('d_drug_code').value = '';
	document.getElementById('d_drug_code_label').textContent = '';
	document.getElementById('d_generic_name').value = '';
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
}

function toggleDurationUnits(enable, prefix) {
	enable = empty(enable) ? false : enable;
	var els = document.getElementsByName(prefix+"_duration_units");
	for (var i=0; i<els.length; i++) {
		els[i].disabled = !enable;
		els[i].checked = false;
	}
}

function toggleItemFormRow(addDialog) {
	var prefix = addDialog ? 'd_' : 'ed_';
	document.getElementById(prefix + 'itemFormRow').style.display = 'table-row';
	// allow user to select the medicine form if it is a prescription by generics.
	if (useGenerics) {
		document.getElementById(prefix + 'item_form_id').disabled = false;
		document.getElementById(prefix + 'item_strength').disabled = false;
		document.getElementById(prefix + 'item_strength_units').disabled = false;
	} else {
		document.getElementById(prefix + 'item_form_id').disabled = true;
		document.getElementById(prefix + 'item_strength').disabled = true;
		document.getElementById(prefix + 'item_strength_units').disabled = true;
	}

}

function initItemAutoComplete() {
	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	var itemType = "Medicine";
	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeActionAjax.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId + "&center_id=" + centerId + "&p_health_authority=" + health_authority + "&tpa_id=" + tpaId;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "generic_code"},
					{key : "drug_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "route_of_admin"},
					{key : "consumption_uom"},
					{key : "cons_uom_id"},
					{key : 'prior_auth_required'},
					{key : 'item_form_id'},
					{key : 'item_strength'},
					{key : 'tooth_num_required'},
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

			// show qty only for pharmacy items.
		if (use_store_items == 'Y' && prescriptions_by_generics == 'false')
			highlightedValue += "(" + record.qty + ") ";
		// show generic name along with the medicine name when prescriptions done by brand names.
		if (!useGenerics)
			highlightedValue += (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
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

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_item_id').value = record.item_id;
	if (record.item_type == 'Medicine') {
		document.getElementById('d_qty_in_stock').value = record.qty;
		document.getElementById('d_duration').value = 1;
		document.getElementsByName('d_duration_units').value = 'D';
		if (!empty(record.generic_name)) {
			document.getElementById('genericNameAnchor_dialog').style.display = 'block';
			document.getElementById('genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "", "dialog", "'+record.generic_code+'")';
			document.getElementById('genericNameAnchor_dialog').innerHTML = record.generic_name;
			document.getElementById('d_generic_code').value = record.generic_code;
			document.getElementById('d_generic_name').value = record.generic_name;
		}
	}

	document.getElementById('d_drug_code').value = empty(record.drug_code) ? '' : record.drug_code;
	document.getElementById('d_cons_uom_id').value = record.cons_uom_id == 0 ? '' : record.cons_uom_id;
	document.getElementById('d_consumption_uom_label').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_item_master').value = record.master;
	document.getElementById('d_item_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('d_item_strength').value = record.item_strength;
	document.getElementById('d_item_strength_units').value = record.item_strength_units == 0 ? '' : record.item_strength_units;
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
	var drugCode = document.getElementById('d_drug_code').value;
	var itemType = "Medicine";
	var ispackage = false;
	if (useGenerics) {
		document.getElementById('d_package_size').value = '';
		document.getElementById('d_price').value = '';
		document.getElementById('d_pkg_size_label').textContent = '';
		document.getElementById('d_price_label').textContent = '';

	} else {
		var orgId = document.getElementById('org_id').value;
		var itemId = document.getElementById('d_item_id').value;
		var itemName = document.getElementById('d_itemName').value;
		var bedType = document.getElementById('bed_type').value;
		var url = cpath+'/outpatient/OpPrescribeActionAjax.do?_method=getItemRateDetails';
		url += '&item_type='+itemType;
		url += '&org_id='+orgId;
		url += '&item_id='+itemId;
		url += '&item_name='+encodeURIComponent(itemName);
		url += '&is_package='+ispackage;
		url += '&bed_type='+bedType;
		ajaxRequest = YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: onGetCharge,
					failure: onGetChargeFailure,
					argument: [itemType, ispackage, itemMaster  , drugCode]}
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
		var drugCode = response.argument[3];

		document.getElementById('d_drug_code_label').textContent = drugCode;
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
		medicine_route_el.selectedIndex = medicine_route_el.length == 2 ? 1 : 0;
		ajaxInProgress = false;
	}
}

function onGetChargeFailure() {
	ajaxInProgress = false;
}

function showGenericInfo(index, prefix, suffix, generic_code) {
	childDialog = genericDialog;
	var anchor = document.getElementById(prefix + "genericNameAnchor" + index + "_" + suffix);
	genericDialog.cfg.setProperty("context", [anchor, "tr", "tl"], false);
	genericDialog.show();
	if (generic_code != "") {
		var ajaxReqObject = new XMLHttpRequest();
		var url=cpath+"/outpatient/OpPrescribeActionAjax.do?_method=getGenericJSON&generic_code="+encodeURIComponent(generic_code);
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

function editTableRow() {
	var id = document.dischargemedicationform.editRowId.value;
	// Even after item is issued Prior auth Request can be made. Need to check.

	var issued = getIndexedValue("issued", id);
	if (issued == 'O') {
		editItemDialog.cancel();
		return true;
	}
	var row = getChargeRow(id, 'itemsTable');


   	var itemName = document.getElementById('ed_itemName').value;
   	var itemId = document.getElementById('ed_item_id').value;
	var adminStrength = document.getElementById('ed_admin_strength').value;
   	var frequency = document.getElementById('ed_frequency').value;
   	var strength = document.getElementById('ed_strength').value;
   	var duration = document.getElementById('ed_duration').value;
   	var qty = document.getElementById('ed_qty').value;
   	var remarks = document.getElementById('ed_remarks').value;
   	var spl_instruction = document.getElementById('ed_special_instruction').value;
   	var master = document.getElementById('ed_item_master').value;
   	var pkg_size = getAmount(document.getElementById('ed_package_size').value);
   	var drugCode = document.getElementById('ed_drug_code_label').value;
   	var price = getPaise(document.getElementById('ed_price').value);
   	var cons_uom_id = document.getElementById('ed_cons_uom_id').value;
   	var item_form_id = document.getElementById('ed_item_form_id').value;
   	var granular_unit = document.getElementById('ed_granular_units').value;
   	var item_strength = document.getElementById('ed_item_strength').value;
   	var item_form_name = document.getElementById('ed_item_form_id').options[document.getElementById('ed_item_form_id').selectedIndex].text;
   	item_form_name = item_form_id == '' ? '' : item_form_name;
   	var routeId = document.getElementById('ed_medicine_route').options[document.getElementById('ed_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('ed_medicine_route').options[document.getElementById('ed_medicine_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;

	var item_strength_units = document.getElementById('ed_item_strength_units').value;
	item_strength_units = item_strength == '' ? '' : item_strength_units;
   	var strength_unit_name = document.getElementById('ed_item_strength_units').options[document.getElementById('ed_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;
   	if (!empty(granular_unit) && granular_unit == 'Y') {
	   	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	   	//	showMessage("js.outpatient.consultation.mgmt.dosageshouldbegreater.zeroandnumber.two.in.brackets");
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
	//	showMessage("js.outpatient.consultation.mgmt.durationshouldbegreater.zeroandnum");
		document.getElementById('ed_duration').focus();
		return false;
	}
	if (!empty(duration) && empty(duration_units)) {
		alert("Please select the duration units");
		return false;
	}

	if (qty == '') {
		alert("Please enter the qty.");
	//	showMessage("js.outpatient.pending.prescriptions.addshow.enter.qty");
		document.getElementById('ed_qty').focus();
		return false;
	}

	if (qty != '' && (!regExp.test(qty) || qty == 0)) {
		alert("Qty should be greater than Zero and it should be a whole number.");
	//	showMessage("js.outpatient.consultation.mgmt.qtyshouldbegreater.number");
		document.getElementById('ed_qty').focus();
		return false;
	}

   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
	setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
	var details = "";
  		if (frequency != '' || duration != '')
  			details = frequency + " / " + duration + " " + duration_units;
	setNodeText(row.cells[DETAILS], details, 20);
	setNodeText(row.cells[QTY], qty);
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

	setNodeText(row.cells[ROUTE], routeName);
	setNodeText(row.cells[REMARKS], remarks, 20);
	setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 20);

	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "drug_code", drugCode);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "special_instr", spl_instruction);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "cons_uom_id", cons_uom_id);
	setHiddenValue(id, "pkg_size", pkg_size);
	setHiddenValue(id, "pkg_price", formatAmountPaise(price));
	setHiddenValue(id, "route_id", routeId);
	setHiddenValue(id, "route_name", routeName);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);
	editItemDialog.cancel();
	return true;
}

var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function openPrevious() {
	var id = document.dischargemedicationform.editRowId.value;
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
	var id = document.dischargemedicationform.editRowId.value;
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
		document.getElementById(prefix + '_remarks').value = '';
		if (granular_unit == 'Y') {
			calcQty(prefix);
			setAutoGeneratedInstruction(prefix);
		} else
			document.getElementById(prefix + '_qty').value = 1;
	}
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

function validateMedBlockExceptQty(calledOn, idPrefix) {
	var medicineName = document.getElementById(idPrefix + '_itemName').value;
	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);

	if (medicineName == '') {
		alert("Please enter the Medicine Name");
	//	showMessage("js.outpatient.consultation.mgmt.pleaseenterthemedicinename");
		return false;
	}
	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
	//	showMessage("js.outpatient.consultation.mgmt.durationshouldbegreater.zeroandnumber");
		document.getElementById(idPrefix + '_duration').focus();
		return false
	}
	return true;
}
var colIndex  = 0;
var ITEM_NAME = colIndex++, FORM =  colIndex++, STRENGTH = colIndex++,
	ADMIN_STRENGTH = colIndex++, DETAILS = colIndex++, ROUTE = colIndex++, REMARKS = colIndex++, SPECIAL_INSTRUCTION = colIndex++;
var QTY = colIndex++, TRASH_COL = colIndex++, EDIT_COL = colIndex++;

var itemsAdded = 0;
function addToTable() {
	var itemType = "Medicine";
	var isInsurancePatient = document.getElementById('tpa_id').value != '';

	if (ajaxInProgress) {
		setTimeout("addToTable()", 100);
		return false
	}
	var itemName = document.getElementById('d_itemName').value;
	if (itemName == '') {
		alert("please enter the Medicine Name");
   	//	showMessage('js.outpatient.consultation.mgmt.prescribetheitem');
   		document.getElementById('d_itemName').focus();
   		return false;
   	}
   	var strength = document.getElementById('d_strength').value;
   	var granular_unit = document.getElementById('d_granular_units').value ;
   	if (!strength) {
   	    alert("please enter Dosage Unit");
   	    document.getElementById('d_strength').focus();
   	    return false;
   	}
   	if (!empty(granular_unit) && granular_unit == 'Y') {
	   	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	   	//	showMessage("js.outpatient.consultation.mgmt.dosageshouldbegreater.zeroandnumber.two.in.brackets");
	   		document.getElementById('d_strength').focus();
	   		return false;
	   	}
	 }
   	var duration = document.getElementById('d_duration').value;
   	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
   		alert("Duration should be greater than Zero and it should be a whole number.");
	//	showMessage("js.outpatient.consultation.mgmt.durationshouldbegreater.zeroandnumber");
		document.getElementById('d_duration').focus();
		return false;
	}

	var qty = document.getElementById('d_qty').value;

	if (qty == '') {
		alert("Please enter the qty");
	//	showMessage("js.outpatient.consultation.mgmt.pleaseentertheqty");
		document.getElementById('d_qty').focus();
		return false;
	}
	if (qty != '' && (!regExp.test(qty) || qty == 0)) {
		alert("Qty should be greater than Zero and it should be a whole number.");
	//	showMessage("js.outpatient.consultation.mgmt.qtyshouldbegreater.number");
		document.getElementById('d_qty').focus();
		return false;
	}
	var item_strength = document.getElementById('d_item_strength').value;
   	var item_strength_units = document.getElementById('d_item_strength_units').value;
   	item_strength_units = item_strength == '' ? '' : item_strength_units;
	if(!(document.getElementById('d_item_strength_units').disabled))
   	var strength_unit_name = document.getElementById('d_item_strength_units').options[document.getElementById('d_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;

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
   	var remarks = document.getElementById('d_remarks').value;
   	var spl_instruction = document.getElementById('d_special_instruction').value;
   	var master = document.getElementById('d_item_master').value;
   	var genericCode = document.getElementById('d_generic_code').value;
   	var genericName = document.getElementById('d_generic_name').value;
   	var drugCode = document.getElementById('d_drug_code').value;
   	var pkg_size = getAmount(document.getElementById('d_package_size').value);
   	var cons_uom_id = document.getElementById('d_cons_uom_id').value;
   	var price = getPaise(document.getElementById('d_price').value);
   	var routeId = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;
   	var item_form_id = document.getElementById('d_item_form_id').value;

   	var item_form_name = document.getElementById('d_item_form_id').options[document.getElementById('d_item_form_id').selectedIndex].text;
   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
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
  			details = frequency + " / " + duration + " " + duration_units;
  		setNodeText(row.cells[DETAILS], details, 20);
		setNodeText(row.cells[QTY], qty);
	if (item_form_id != '') {
		setNodeText(row.cells[FORM], item_form_name, 15);
	}
	setNodeText(row.cells[STRENGTH], item_strength + ' ' + strength_unit_name, 15);

	setHiddenValue(id, "generic_code", genericCode);
	setHiddenValue(id, "drug_code", drugCode);
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

	setNodeText(row.cells[ROUTE], routeName);
	setNodeText(row.cells[REMARKS], remarks, 20);
	setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 20);


	setHiddenValue(id, "cons_uom_id", cons_uom_id);
	setHiddenValue(id, "item_prescribed_id", "_");
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "drug_code", drugCode);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "special_instr" , spl_instruction);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "pkg_size", pkg_size == '' ? '' : pkg_size);
	setHiddenValue(id, "pkg_price", price == '' ? '' : formatAmountPaise(price));
	setHiddenValue(id, "route_id", routeId);
	setHiddenValue(id, "route_name", routeName);
	setHiddenValue(id, "issued", "P");

	itemsAdded++;
	clearFields();
	setRowStyle(id);
	addItemDialog.align("tr", "tl");
	document.getElementById('d_itemName').focus();
	return id;
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');
	var prescribedId = getIndexedValue("item_prescribed_id", i);
	var qty_in_stock = getIndexedValue("qty_in_stock", i);

 //	var flagImgs = row.cells[ITEM_TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("delItem", i) == 'true';
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
		if (qty_in_stock == 0) cls = 'zero_qty'
		else cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}
	/**
	* cancelled flag takes priority when a prescriptions is of type prior auth required and it is cancelld.
	*/
/*	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}*/
	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

/*	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;*/

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
	return false;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.dischargemedicationform, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.dischargemedicationform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function validateSubmit(print) {
	var doctorId = document.getElementById('doctor_id');
	var medicationDetailsTable = document.getElementById('itemsTable');
	var medicationDetailsTableLen = medicationDetailsTable.rows.length -2;
	var medicineDeleted = document.getElementsByName('delItem');
	document.dischargemedicationform.isPrint.value = print;

	if (empty(doctorId.value)) {
		alert("Doctor is mandatory");
		doctorId.focus();
		return false;
	}

	if (medicationDetailsTableLen == 0) {
		alert("please add a medicine to proceed.");
		return false;
	}
	if (allAreDeleted()) {
		alert("All the medicines are marked for deletion. Medication should have atleast one medicine.");
		return false;
	}
	document.dischargemedicationform.submit();
	return true;
}

function validateEMedication() {
	var message = "Please enter values for following fields for eMedication \n\n";
	message += " * Dosage \n";
	message += " * Route \n";
	message += " * Frequency \n";
	message += " * Start Date \n";
	message += " * End Date \n";
	message += " * Duration \n";
	message += " * Duration Units \n";
	message += " * Total Quantity \n";
	message += " * Remarks \n";

	var duration = document.getElementsByName('duration');
	var durationUnits = document.getElementsByName('duration_units');
    var routes = document.getElementsByName('route_id');
    var dosage = document.getElementsByName('strength');
    var totalQty = document.getElementsByName('medicine_quantity');
    var remarks = document.getElementsByName('item_remarks');
    var frequency = document.getElementsByName('frequency');
    var startDate = document.getElementsByName('start_date');
	var endDate = document.getElementsByName('end_date');

    for (var i=0;i<duration.length-1;i++) {
    	if (empty(duration[i].value) || empty(startDate[i].value) || empty(endDate[i].value) || empty(durationUnits[i].value) || empty(remarks[i].value)
    			|| empty(frequency[i].value) || empty(routes[i].value) || empty(dosage[i].value)
    			|| empty(totalQty[i].value)) {

    		alert(message + "\n for item "+document.getElementsByName('item_name')[i].value);
    		return false;
    	}
    }
    return true;
}

function validateMedicationDetails() {
	var itemName = document.getElementsByName('item_name');

	var issued = document.getElementsByName("issued");
	var strength_els = document.getElementsByName("strength");
	var granular_units_els = document.getElementsByName("granular_units");
	var medicines = new Array();
	for (var i=0; i<issued.length; i++) {
		if (issued[i].value != 'O' && granular_units_els[i].value == 'Y' && strength_els[i].value != ''
			&& (!isDecimal(strength_els[i].value, 2) || strength_els[i].value == 0)) {
			medicines.push(itemName[i].value);
		}
	}

	if (medicines.length > 0) {
		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals). \n"
				+"Please correct the dosage information for following medicines. \n\n *") + medicines.join("\n * ")
		return false;
	}
	return true;
}

function getAllVisitPrescribedMedicines(obj) {
	allVisitPrescMedDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	allVisitPrescMedDialog.show();
	var patientId = encodeURIComponent(document.getElementById('patient_id').value);
	makePatinetVisitMedicinesAjaxCall(patientId);
}

function makePatinetVisitMedicinesAjaxCall(patientId, curPage) {
	document.getElementById('medProgressbar').style.visibility = 'none';
	var ok = true;
	if  (curPage) {
		var visit_medication_chkbox =  document.getElementsByName('visit_medication_chkbox');
		var checked = false;
		for (var i=0;i<visit_medication_chkbox.length;i++) {
			if (visit_medication_chkbox[i].checked) {
				checked = true;
				break;
			}
		}
		if (checked)
			ok = confirm("all selected medicines will be unselected.\ndo you want to continue?");
	}
	if (ok) {
		var url = cpath + '/pages/dischargeMedication.do?_method=getPatientVisitPrescribedMedicines';
		url += "&patient_id="+patientId;
		if (curPage)
			url += "&pageNum="+curPage;
		url += "&pageSize=10";

		YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: populateDischargeMedicationRow,
				failure: failedToGetVisitMedicineResults,
				argument: [patientId]
			});
	}
}

function failedToGetVisitMedicineResults() {
}

function populateDischargeMedicationRow(response) {
	var patientId = decodeURIComponent(response.argument[0]);
	if (response.responseText != undefined) {
		var visitMedicineDetails = eval('(' + response.responseText + ')');
		var table = document.getElementById("visitMedicinesTable");
		var label = null;
		for (var i=1; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		visitMedicineDetails = visitMedicineDetails == null ? {dtoList : [], numPages: 0} : visitMedicineDetails;
		var dtoList = visitMedicineDetails.dtoList;
		generateVisitMedicationPaginationSection(patientId, visitMedicineDetails.pageNumber, visitMedicineDetails.numPages);

		var noResultsRow = table.rows[table.rows.length-1];
		noResultsRow.style.display = dtoList.length == 0 ? 'table-row' : 'none';
		for (var i=0; i<dtoList.length; i++) {
			var record = dtoList[i];
			var templateRow = table.rows[table.rows.length-2];
			var row = templateRow.cloneNode(true);
			var id = table.rows.length-3;
			row.style.display = '';
			var inputEle = document.createElement('input');
				inputEle.setAttribute("type", "checkbox");
				inputEle.setAttribute("name", "visit_medication_chkbox");
				inputEle.setAttribute("value", record.item_id);

			table.tBodies[0].insertBefore(row, templateRow);
			row.cells[0].appendChild(inputEle);
			var itemName = empty(record.item_id) ? record.generic_name : record.item_name;
			setNodeText(row.cells[1], itemName,40);
			setNodeText(row.cells[2], record.item_form_name);
			var strength = record.item_strength;
			if (!empty(record.unit_name))
				strength += ' ' + record.unit_name;
			setNodeText(row.cells[3], strength);

			setNodeText(row.cells[4], record.admin_strength);
			var details = empty(record.medicine_dosage) ? '' : record.medicine_dosage;
			details += (!empty(details) && !empty(record.duration)) ? '/' : '';
			details += empty(record.duration) ? '' : (' ' + record.duration_units);

			setNodeText(row.cells[5], details, 20, details);
			setNodeText(row.cells[6], record.route_name, 20, record.med_route_name);

			var	quantity = record.medicine_quantity;
			var	remarks = record.remarks;
			var special_instr = record.special_instr

			setNodeText(row.cells[7], remarks, 20, remarks);
			setNodeText(row.cells[8], special_instr, 20,  special_instr);
			setNodeText(row.cells[9], quantity);

			setHiddenValue(id, "med_item_name", itemName);
			setHiddenValue(id, "med_item_id", empty(record.item_id) ? record.generic_code : record.item_id);
			setHiddenValue(id, "med_strength", record.strength);

			setHiddenValue(id, "med_granular_units", record.granular_units);
			setHiddenValue(id, "med_admin_strength", record.admin_strength);
			setHiddenValue(id, "med_drug_code", record.drug_code);
			setHiddenValue(id, "med_duration", record.duration);
			setHiddenValue(id, "med_duration_units", record.duration_units);
			if (record.granular_units != 'Y') {
				setHiddenValue(id, "med_medicine_quantity", 1);
			} else {
				setHiddenValue(id, "med_medicine_quantity", quantity);
			}
			setHiddenValue(id, "med_item_remarks", remarks);
			setHiddenValue(id, "med_special_instr", special_instr);
			setHiddenValue(id, "med_item_master", record.master);
			setHiddenValue(id, "med_generic_code", record.generic_code);
			setHiddenValue(id, "med_generic_name", record.generic_name);
			setHiddenValue(id, "med_route_id", record.route_id);
			setHiddenValue(id, "med_route_name", record.route_name);
			setHiddenValue(id, "med_consumption_uom", record.consumption_uom);
			setHiddenValue(id, "med_item_form_id", record.item_form_id);
			setHiddenValue(id, "med_item_form_name", record.item_form_name);
			setHiddenValue(id, "med_item_strength", record.item_strength);
			setHiddenValue(id, "med_item_strength_units", record.item_strength_units);
			setHiddenValue(id, "med_item_strength_unit_name", empty(record.unit_name) ? '' : record.unit_name);
			setHiddenValue(id, "med_frequency", empty(record.frequency) ? '' : record.frequency);

		}

	}
	document.getElementById('medProgressbar').style.visibility = 'hidden';

}

function generateVisitMedicationPaginationSection(patientId, curPage, numPages) {
	var div = document.getElementById('medicationPaginationDiv');
	div.innerHTML = '';

	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makePatinetVisitMedicinesAjaxCall("'+encodeURIComponent(patientId)+'", '+(curPage-1)+')');
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
			label.setAttribute('onclick', 'makePatinetVisitMedicinesAjaxCall("'+encodeURIComponent(patientId)+'", '+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}
}

function validateDuration(prefix) {
	var duration = document.getElementById(prefix +'_duration').value;
	if (duration == '0') {
		alert('Please enter valid duration');
		document.getElementById(prefix +'_duration').value = '';
		document.getElementById(prefix +'_end_date').value = '';
		document.getElementById(prefix +'_duration').focus();
		return false;
	}
}

function allAreDeleted() {
	var delItems = document.getElementsByName('delItem');
	var medicationDetailsTable = document.getElementById('itemsTable');
	var medicationDetailsTableLen = medicationDetailsTable.rows.length -2;

	var count = 0;
	for (var i=0; i<delItems.length; i++) {
		if (delItems[i].value == 'true')
			count++;
	}
	if (medicationDetailsTableLen == count) {
		return true;
	}
	return false;
}

