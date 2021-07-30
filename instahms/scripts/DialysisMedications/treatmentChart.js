 var Dom = YAHOO.util.Dom;
function initStandingtreatments() {
	initSIDialog();
	initEditSIDialog();
	editDialogGeneric();
}


function initSIDialog() {
	var dialogDiv = document.getElementById("addSIDialog");
	dialogDiv.style.display = 'block';
	addSIDialog = new YAHOO.widget.Dialog("addSIDialog",
			{	width:"600px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('SIAdd', 'click', addSIToTable, addSIDialog, true);
	YAHOO.util.Event.addListener('SIClose', 'click', handleAddSICancel, addSIDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddSICancel,
	                                                scope:addSIDialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("addSIDialogFieldsDiv", { keys:13 },
				{ fn:onEnterKeySIItemDialog, scope:addSIDialog, correctScope:true } );
	addSIDialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	addSIDialog.render();
}

function onEnterKeySIItemDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new item from autocomplete.)
	document.getElementById("s_d_itemName").blur();
	addSIToTable();
}

function handleAddSICancel() {
	if (childDialog == null) {
		parentSIDialog = null;
		this.cancel();
	}
}

var parentSIDialog = null;
var childDialog = null;
function showAddSIDialog(obj) {
	var row = getThisRow(obj);

	addSIDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addSIDialog.show();
	onSIItemChange();
	document.getElementById('s_d_itemName').focus();
	parentSIDialog = addSIDialog;
	return false;
}

function onSIItemChange() {
	clearSIFields();
	initSIItemAutoComplete();
	var itemType = document.getElementById('s_d_itemType').value;

	if (itemType == "All") {

	} else {
		//document.getElementById('dGenericNameRow').style.display = 'table-row';
		document.getElementById('s_d_dosage').disabled = false;
		document.getElementById('s_d_remarks').disabled = false;
		document.getElementById('s_d_medicine_route').disabled = false;

	}

}

function toggleInterval(prefix) {
	var frequency = document.getElementById(prefix+ "frequency_name").value;
	if (frequency == 0) {
		document.getElementById(prefix + "interval").disabled = false;
	} else {
		document.getElementById(prefix + "interval").disabled = true;
	}
}


var siItemAutoComp = null;
function initSIItemAutoComplete() {
	if (!empty(siItemAutoComp)) {
		siItemAutoComp.destroy();
		siItemAutoComp = null;
	}
	var itemType = document.getElementById('s_d_itemType').value;
	if (itemType == 'Instructions' || itemType == 'NonHospital') return null; // for doctor instrctions no need to create the autocomplete.

	var orgId = document.getElementById('org_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeActionAjax.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId + "&isStanding=true";
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
					{key : "item_form_id"},
					{key : "item_strength"}
				 ],
		numMatchFields: 2
	};

	siItemAutoComp = new YAHOO.widget.AutoComplete("s_d_itemName", "s_d_itemContainer", ds);
	siItemAutoComp.minQueryLength = 1;
	siItemAutoComp.animVert = false;
	siItemAutoComp.maxResultsDisplayed = 50;
	siItemAutoComp.resultTypeList = false;
	var forceSelection = true;
	if (itemType == 'Medicine' && use_store_items != 'Y')
		forceSelection = false;
	siItemAutoComp.forceSelection = forceSelection;

	siItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	siItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			if (use_store_items == 'Y')
			highlightedValue += "(" + record.qty + ") ";
			highlightedValue += (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
		}
		return highlightedValue;
	}

	siItemAutoComp.dataRequestEvent.subscribe(clearSIMasterType);
	if (forceSelection) {
		siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
		siItemAutoComp.selectionEnforceEvent.subscribe(clearSIMasterType);
	} else {
		siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
	}

	return siItemAutoComp;
}

function selectSIItem(sType, oArgs) {
	var record = oArgs[2];
	if (record.item_type == 'Medicine' && !empty(record.generic_name)) {
		//document.getElementById('dGenericNameRow').style.display = 'table-row';
		document.getElementById('s_d_genericNameAnchor_dialog').style.display = 'block';
		document.getElementById('s_d_genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "s_d_", "dialog", "'+record.generic_code+'")';
		document.getElementById('s_d_genericNameAnchor_dialog').innerHTML = record.generic_name;
		document.getElementById('s_d_generic_code').value = record.generic_code;
		document.getElementById('s_d_generic_name').value = record.generic_name;
	}
	if (record.master == 'item_master') {
		document.getElementById('s_d_itemMasterType').textContent = 'Yes';
	} else {
		document.getElementById('s_d_itemMasterType').textContent = 'No';
	}
	document.getElementById('s_d_qty_in_stock').value = record.qty;
	document.getElementById('s_d_item_id').value = record.item_id;
	document.getElementById('s_d_ispackage').value = record.ispkg;
	document.getElementById('s_d_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('s_d_medicineUOM').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('s_d_item_master').value = record.master;
	document.getElementById('s_d_item_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('s_d_item_strength').value = record.item_strength;
	if (record.item_type == 'Medicine')	getRouteOfAdministrations();


}

function getRouteOfAdministrations() {
	var itemId = document.getElementById('s_d_item_id').value;
	var itemName = document.getElementById('s_d_itemName').value;
	var url = cpath+'/outpatient/OpPrescribeAction.do?_method=getRoutesOfAdministrations';
		url += '&item_id='+itemId;
		url += '&item_name='+encodeURIComponent(itemName);
	var	ajaxRequestForRoutes = YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: onGetRoutes,
					failure: onGetRoutesFailure
				}
		)
}

function onGetRoutes(response) {
	if (response.responseText != undefined) {
		var routes = eval('(' + response.responseText + ')');
		if (routes == null) {
			document.getElementById('s_d_medicine_route').length = 1;
			return ;
		}
		var routeIds = routes.route_id.split(",");
		var routeNames = routes.route_name.split(",");
		var medicine_route_el = document.getElementById('s_d_medicine_route');
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
	}
}

function onGetRoutesFailure() {
}

function clearSIMasterType(oSelf) {
	document.getElementById('s_d_item_id').value = '';
	document.getElementById('s_d_itemMasterType').textContent = '';
	document.getElementById('s_d_genericNameAnchor_dialog').style.display = 'none';
	document.getElementById('s_d_genericNameAnchor_dialog').href = '';
	document.getElementById('s_d_genericNameAnchor_dialog').innerHTML = '';
	document.getElementById('s_d_generic_code').value = '';
	document.getElementById('s_d_generic_name').value = '';
	document.getElementById('s_d_ispackage').value = '';
	document.getElementById('s_d_consumption_uom').value = '';
	document.getElementById('s_d_medicineUOM').textContent = '';
	document.getElementById('s_d_item_master').value = '';
	document.getElementById('s_d_qty_in_stock').value = '';
	document.getElementById('s_d_item_form_id').value = '';
	document.getElementById('s_d_item_strength').value = '';
}

var S_PRESC_DATE=0, S_ITEM_TYPE=1, S_ITEM_NAME = 2, S_FORM = 3, S_STRENGTH = 4, S_DOSAGE = 5, S_SCHEDULE=6, S_DAYS=7, S_ROUTE=8, S_REMARKS = 9,
	S_TRASH_COL=10, S_EDIT_COL=11;
var itemsAdded = 0;
function addSIToTable() {
	var dosage = document.getElementById('s_d_dosage').value;
	var itemType = document.getElementById('s_d_itemType').value;
	var itemName = document.getElementById('s_d_itemName').value;
	var itemId = document.getElementById('s_d_item_id').value;
	var frequency = document.getElementById('s_d_frequency_name').options[document.getElementById('s_d_frequency_name').selectedIndex].text;
   	var frequencyId = document.getElementById('s_d_frequency_name').value;
   	var interval = document.getElementById('s_d_interval').value;
   	var addActivityEl = document.getElementById('s_d_addActivity');
   	var prescribedDate = document.getElementById('s_d_prescribed_date').value;

	if (itemName == '') {
   		showMessage('js.dialysismedications.treatmentchart.prescribe.item');
   		document.getElementById('s_d_itemName').focus();
   		return false;
   	}
	if (checkForSIDuplicates()) {
		var msg=getString("js.dialysismedications.treatmentchart.duplicate.entry");
		msg+=itemName;
		alert(msg);
		return false;
	}
	if (itemType == 'Medicine' && dosage == '') {
		showMessage("js.dialysismedications.treatmentchart.enter.dosage");
		document.getElementById('s_d_dosage').focus();
		return false;
	}

   	var addActivity = 'false';
   	if (addActivityEl && addActivityEl.checked) {
   		addActivity = 'true';
	}
	if (frequency == '') {
   		showMessage("js.dialysismedications.treatmentchart.enter.frequency");
   		document.getElementById('s_d_frequency_name').focus();
   		return false;
   	}
   	if (interval == '' || interval == 0) {
   		showMessage("js.dialysismedications.treatmentchart.days.greaterzero");
   		document.getElementById('s_d_interval').focus();
   		return false;
   	}

   	if (!validateDateFormat1(prescribedDate)) {
		document.getElementById('s_ed_presc_date').focus();
		return false;
	}

	var id = getNumCharges('siTable');
   	var table = document.getElementById("siTable");
	var templateRow = table.rows[getTemplateRow('siTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	document.getElementsByName('trashCanAnchor')[id].style.display = 'block';
   	row.id = "s_itemRow" + id;

   	var cell = null;
   	var remarks = document.getElementById('s_d_remarks').value;
   	var master = document.getElementById('s_d_item_master').value;
   	var genericCode = document.getElementById('s_d_generic_code').value;
   	var genericName = document.getElementById('s_d_generic_name').value;
   	var ispackage = document.getElementById('s_d_ispackage').value;
   	var consumption_uom = document.getElementById('s_d_consumption_uom').value;
   	var routeId = document.getElementById('s_d_medicine_route').options[document.getElementById('s_d_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('s_d_medicine_route').options[document.getElementById('s_d_medicine_route').selectedIndex].text;
   	var item_form_id = document.getElementById('s_d_item_form_id').value;
   	var item_strength = document.getElementById('s_d_item_strength').value;
   	var item_form_name = document.getElementById('s_d_item_form_id').options[document.getElementById('s_d_item_form_id').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;

   	setNodeText(row.cells[S_PRESC_DATE], prescribedDate);
   	setNodeText(row.cells[S_ITEM_TYPE], itemType);
   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		setNodeText(row.cells[S_DOSAGE], dosage);
		if (item_form_id != '') {
			setNodeText(row.cells[S_FORM], item_form_name, 15);
		}
		setNodeText(row.cells[S_STRENGTH], item_strength, 15);

		setHiddenValue(id, "s_item_form_id", item_form_id);
		setHiddenValue(id, "s_item_strength", item_strength);
		setHiddenValue(id, "s_generic_code", genericCode);
		setHiddenValue(id, "s_generic_name", genericName);
	}

	setNodeText(row.cells[S_SCHEDULE], frequency);
	setNodeText(row.cells[S_DAYS], interval);
	setNodeText(row.cells[S_ROUTE], routeName);
	setNodeText(row.cells[S_REMARKS], remarks, 30);

	setHiddenValue(id, "s_prescription_id", "_");
	setHiddenValue(id, "s_prescribed_date", prescribedDate);
	setHiddenValue(id, "s_days", interval);
	setHiddenValue(id, "s_medicine_dosage", dosage);
	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);
	setHiddenValue(id, "s_recurrence_daily_id", frequencyId);
	setHiddenValue(id, "s_freq_type", frequencyId == '0' ? 'R' : 'F');
	setHiddenValue(id, "s_consumption_uom", consumption_uom);
	setHiddenValue(id, "s_route_id", routeId);
	setHiddenValue(id, "s_route_name", routeName);

	setSIRowStyle(id);
	itemsAdded++;
	clearSIFields();

	this.align("tr", "tl");
	document.getElementById('s_d_itemName').focus();
	return id;
}

function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.treatmentForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearSIFields() {
//	document.getElementById('s_d_prescribed_date').value = currentDate;
	document.getElementById('s_d_itemName').value = '';
   	document.getElementById('s_d_dosage').value = '';
   	document.getElementById('s_d_medicine_route').length = 1;
   	document.getElementById('s_d_remarks').value = '';
   	document.getElementById('s_d_ispackage').value = '';
   	document.getElementById('s_d_genericNameAnchor_dialog').innerHTML = '';
   	document.getElementById('s_d_genericNameAnchor_dialog').style.display = 'none';
   	document.getElementById('s_d_genericNameAnchor_dialog').href = '';
   	document.getElementById('s_d_frequency_name').value = '';
   	document.getElementById('s_d_interval').value = '';
   	document.getElementById('s_d_medicineUOM').textContent = '';
   	document.getElementById('s_d_consumption_uom').value = '';
	document.getElementById('s_d_itemMasterType').textContent = '';
	if (document.getElementById('s_d_addActivity')) {
		document.getElementById('s_d_addActivity').checked = true;
	}
	document.getElementById('s_d_item_form_id').value = '';
	document.getElementById('s_d_item_strength').value = '';
}

function setSIRowStyle(i) {
	var row = getChargeRow(i, 'siTable');
	var prescribedId = getIndexedValue("s_prescription_id", i);
	var qty_in_stock = getIndexedValue("s_qty_in_stock", i);
	//var flagImgs = row.cells[S_ITEM_TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[S_TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("s_delItem", i) == 'true';
	var edited = getIndexedValue("s_edited", i) == 'true';

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

//	if (flagImgs && flagImgs[0])
//		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelSIItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var oldDeleted =  getIndexedValue("s_delItem", id);
	var isNew = getIndexedValue("s_prescription_id", id) == '_';

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

		setIndexedValue("s_delItem", id, newDeleted);
		setIndexedValue("s_edited", id, "true");
		setSIRowStyle(id);
	}
	return false;
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.treatmentForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function initEditSIDialog() {
	var dialogDiv = document.getElementById("editSIDialog");
	dialogDiv.style.display = 'block';
	editSIDialog = new YAHOO.widget.Dialog("editSIDialog",{
			width:"600px",
			text: "Edit Item",
			context :["itemsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditSICancel,
	                                                scope:editSIDialog,
	                                                correctScope:true } );
	editSIDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editSIDialog.cancelEvent.subscribe(handleEditSICancel);
	YAHOO.util.Event.addListener('siOk', 'click', editSITableRow, editSIDialog, true);
	YAHOO.util.Event.addListener('siEditCancel', 'click', handleEditSICancel, editSIDialog, true);
	YAHOO.util.Event.addListener('siEditPrevious', 'click', openSIPrevious, editSIDialog, true);
	YAHOO.util.Event.addListener('siEditNext', 'click', openSINext, editSIDialog, true);
	editSIDialog.render();
}

function handleEditSICancel() {
	if (childDialog == null) {
		var id = document.getElementById('s_ed_editRowId').value ;
		var row = getChargeRow(id, "siTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		parentSIDialog = null;
		siFieldEdited = false;
		this.hide();
	}
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.treatmentForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function showEditSIDialog(obj) {
	parentDialog = editSIDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editSIDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editSIDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	var itemName = getIndexedValue("s_item_name", id);
	document.getElementById('s_ed_editRowId').value = id;
	var itemType = getIndexedValue("s_itemType", id);
	document.getElementById('s_ed_itemTypeLabel').textContent = itemType;
	document.getElementById('s_ed_itemNameLabel').textContent = itemName;
	document.getElementById('s_ed_itemName').value = getIndexedValue("s_item_name", id);
	document.getElementById('s_ed_item_id').value = getIndexedValue("s_item_id", id);
	document.getElementById('s_ed_itemType').value = itemType;
	document.getElementById('s_ed_medicine_route').textContent = getIndexedValue('s_route_name', id);
	document.getElementById('s_ed_presc_date').value = getIndexedValue('s_prescribed_date', id);
	var master = getIndexedValue("s_item_master", id);
	if (master == 'item_master') {
		document.getElementById('s_ed_itemMasterType').textContent = 'Yes';
	} else {
		document.getElementById('s_ed_itemMasterType').textContent = 'No';
	}
	if (itemType == 'Medicine') {
		document.getElementById('s_ed_dosage').disabled = false;
		document.getElementById('s_ed_remarks').disabled = false;
		document.getElementById('s_ed_dosage').value = getIndexedValue("s_medicine_dosage", id);
		if (itemType == 'Medicine') {
			document.getElementById('s_ed_medicineUOM').textContent = getIndexedValue("s_consumption_uom", id);
			document.getElementById('s_ed_consumption_uom').value = getIndexedValue("s_consumption_uom", id);
			document.getElementById('s_ed_genericNameAnchor_editdialog').href =
				'javascript:showGenericInfo("", "s_ed_", "editdialog", "' + getIndexedValue("s_generic_code", id) + '")';
			document.getElementById('s_ed_genericNameAnchor_editdialog').style.display = 'block';
			document.getElementById('s_ed_genericNameAnchor_editdialog').innerHTML = getIndexedValue("s_generic_name", id);
		}
		document.getElementById('s_ed_item_form_id').value = getIndexedValue("s_item_form_id", id);
		document.getElementById('s_ed_item_strength').value = getIndexedValue('s_item_strength', id);
	}
	var prescId = getIndexedValue("s_prescription_id", id);

	//document.getElementById('s_ed_discontinue').checked = (getIndexedValue("s_discontinued", id) == 'Y');
	document.getElementById('s_ed_frequency_name').value = getIndexedValue('s_recurrence_daily_id', id);
	document.getElementById('s_ed_interval').value = getIndexedValue('s_days', id);
	document.getElementById('s_ed_ispackage').value = getIndexedValue("s_ispackage", id);
	document.getElementById('s_ed_remarks').value = getIndexedValue('s_item_remarks', id);
	document.getElementById('s_ed_item_master').value = getIndexedValue('s_item_master', id);
	document.getElementById('s_ed_remarks').focus();

	return false;
}

function editSITableRow() {
	var id = document.getElementById('s_ed_editRowId').value ;
	var row = getChargeRow(id, 'siTable');

	var itemType = document.getElementById('s_ed_itemType').value;
   	var itemName = document.getElementById('s_ed_itemName').value;
   	var itemId = document.getElementById('s_ed_item_id').value;
   	var dosage = document.getElementById('s_ed_dosage').value;
   	var remarks = document.getElementById('s_ed_remarks').value;
   	var master = document.getElementById('s_ed_item_master').value;
   	var ispackage = document.getElementById('s_ed_ispackage').value;
   	var consumption_uom = document.getElementById('s_ed_consumption_uom').value;
   	var frequency = document.getElementById('s_ed_frequency_name').options[document.getElementById('s_ed_frequency_name').selectedIndex].text;
   	var frequencyId = document.getElementById('s_ed_frequency_name').value;
   	var interval = document.getElementById('s_ed_interval').value;
   	var addDiscontiuedEl = document.getElementById('s_ed_discontinue');
   	var consumptionUOM = document.getElementById('s_ed_consumption_uom').value;
   	var prescribedDate = document.getElementById('s_ed_presc_date').value;
   	var item_form_id = document.getElementById('s_ed_item_form_id').value;
   	var item_strength = document.getElementById('s_ed_item_strength').value;
   	var item_form_name = document.getElementById('s_ed_item_form_id').options[document.getElementById('s_ed_item_form_id').selectedIndex].text;
   	var addActivity = 'false';

   	if (!validateDateFormat1(prescribedDate)) {
		document.getElementById('s_ed_presc_date').focus();
		return false;
	}

/*   	if (allowForUnDiscontinue(id) && !addDiscontiuedEl.checked) {
		showMessage("Duplicate entry : " + itemName +". You can't unmark the discountinue.");
		addDiscontiuedEl.checked = true;
		return false;
	}*/

   	if (frequency == '') {
   		showMessage("js.dialysismedications.treatmentchart.enter.frequency");
   		document.getElementById('s_ed_frequency_name').focus();
   		return false;
   	}
   	if (frequencyId == 0 && (interval == '' || interval == 0)) {
   		showMessage("js.dialysismedications.treatmentchart.interval.greaterzero");
   		document.getElementById('s_ed_interval').focus();
   		return false;
   	}

   	var addActivityEl = document.getElementById('s_ed_addActivity');
   	if (addActivityEl && addActivityEl.checked) {
   		addActivity = 'true';
   	}

  // 	var discontinued = 'N';
   //	if (addDiscontiuedEl && addDiscontiuedEl.checked) {
   //		discontinued = 'Y';
   //	}

   	setNodeText(row.cells[S_PRESC_DATE], prescribedDate);
	setNodeText(row.cells[S_ITEM_TYPE], itemType);
   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine') {
		setNodeText(row.cells[S_DOSAGE], dosage);
		if (item_form_id != '')
			setNodeText(row.cells[S_FORM], item_form_name, 15);
		setNodeText(row.cells[S_STRENGTH], item_strength, 15);

		setHiddenValue(id, "s_item_form_id", item_form_id);
		setHiddenValue(id, "s_item_strength", item_strength);
		setHiddenValue(id, "s_medicine_dosage", dosage);
	}

	setNodeText(row.cells[S_SCHEDULE], frequency);
	setNodeText(row.cells[S_DAYS], interval);
	setNodeText(row.cells[S_REMARKS], remarks, 30);

	setHiddenValue(id, "s_prescribed_date", prescribedDate);
	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);
	setHiddenValue(id, "s_recurrence_daily_id", frequencyId);
	setHiddenValue(id, "s_consumption_uom", consumption_uom);
	setHiddenValue(id, "s_days", interval);
	setHiddenValue(id, "s_medicine_dosage", dosage);
	//setHiddenValue(id, "s_discontinued", discontinued);
	setHiddenValue(id, "s_freq_type", frequencyId == '0' ? 'R' : 'F');
	setHiddenValue(id, "s_consumption_uom", consumptionUOM);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("s_edited", id, 'true');
	setSIRowStyle(id);

	editSIDialog.cancel();
	return true;
}

var siFieldEdited = false;
function setSIEdited() {
	siFieldEdited = true;
}

function openSIPrevious(id, previous, next) {
	var id = document.getElementById('s_ed_editRowId').value ;
	id = parseInt(id);
	var row = getChargeRow(id, 'siTable');
	if (siFieldEdited) {
		siFieldEdited = false;
		if (!editSITableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditSIDialog(document.getElementsByName('si_editAnchor')[parseInt(id)-1]);
	}
}

function openSINext() {
	var id = document.getElementById('s_ed_editRowId').value ;
	id = parseInt(id);
	var row = getChargeRow(id, 'siTable');
	if (siFieldEdited) {
		siFieldEdited = false;
		if (!editSITableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id+1 != document.getElementById('siTable').rows.length-2) {
		showEditSIDialog(document.getElementsByName('si_editAnchor')[parseInt(id)+1]);
	}
}

function hideDiscontinuedItems(obj) {
	var els = Dom.getElementsByClassName("discontinued", "tr", "siTable");
	for (var i=0; i<els.length; i++) {
		els[i].style.display = obj.checked ? 'none' : 'table-row'
	}
}

/* duplicate check is being done based
 1) on the item id if item type is
 		one of Medicine(Pharamcy), Test, Service, Doctor.
 2) on the item name if item type is
 		one of Medicine(non pharmacy), Non Hospital Items.
*/
function checkForSIDuplicates(prefix) {
	var itemTypes = document.getElementsByName("s_itemType");
//	var issuedItems = document.getElementsByName("s_discontinued");
	var dItemType = document.getElementById("s_d_itemType").value;
	var dItemName = document.getElementById("s_d_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	if (dItemType == 'NonHospital' || (dItemType == 'Medicine' && use_store_items != 'Y')) {
		dItemName = document.getElementById('s_d_itemName').value;
		itemNames = document.getElementsByName('s_item_name');
	}

	for (var i=0; i<itemTypes.length-1; i++) {
		if (dItemType == itemTypes[i].value) {
			if (itemNames[i].value.trim().toLowerCase() == dItemName.trim().toLowerCase()) {
				 return true;
			}
		}
	}
	return false;
}

function allowForUnDiscontinue(index) {
	var itemTypes = document.getElementsByName("s_itemType");
	var issuedItems = document.getElementsByName("s_discontinued");
	var dItemType = document.getElementById("s_ed_itemType").value;
	var dItemName = document.getElementById("s_ed_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	if (dItemType == 'NonHospital' || (dItemType == 'Medicine' && use_store_items != 'Y')) {
		itemNames = document.getElementsByName('s_item_name');
		dItemName = document.getElementById("s_ed_itemName").value;
	}

	for (var i=0; i<itemTypes.length-1; i++) {
		if (dItemType == itemTypes[i].value) {
			if (itemNames[i].value.trim().toLowerCase() == dItemName.trim().toLowerCase()) {
				if (i == index) continue; // excluding the selected item.
				var issued = issuedItems[i].value;
				if (issued == 'N') return true;
			}
		}
	}
	return false;
}

function isInteger(text) {
    var str = text.toString();
    var re = /^\d*$/;
	return re.test(text);
}

function validateDateFormat1(dateStr) {

	var myarray = dateStr.split("-");

	if (myarray.length != 3) {
		showMessage("js.dialysismedications.treatmentchart.incorrectdataformat.use.ddmmyy");
		document.getElementById('s_d_prescribed_date').focus();
		return false;
	}

	var date = myarray[0];
	var mth = myarray[1];
	var yr = myarray[2];

	if (!isInteger(date)) {
		showMessage("js.dialysismedications.treatmentchart.incorrectdataformat.date.notnumber");
		document.getElementById('s_d_prescribed_date').focus();
		return false;
	}

	if (!isInteger(mth)) {
		showMessage("js.dialysismedications.treatmentchart.incorrectdataformat.month.notnumber");
		document.getElementById('s_d_prescribed_date').focus();
		return false;
	}
	if (!isInteger(yr)) {
		showMessage("js.dialysismedications.treatmentchart.incorrectdataformat.year.notnumber");
		document.getElementById('s_d_prescribed_date').focus();
		return false;
	}

	if (parseInt(date) > 31) {
		showMessage("js.dialysismedications.treatmentchart.invalidday.1to31day");
		document.getElementById('s_d_prescribed_date').focus();
		return false;
	}

    if (parseInt(mth) > 12) {
    	showMessage("js.dialysismedications.treatmentchart.invalidday.1to12month");
    	document.getElementById('s_d_prescribed_date').focus();
        return false;
    }

    if ( (yr.length != 2) && (yr.length != 4) ) {
    	showMessage("js.dialysismedications.treatmentchart.invalidday.2to4year");
    	document.getElementById('s_d_prescribed_date').focus();
		return false;
	}

	return true;
}

function validateSubmit() {
	if (mrNo == '') {
		showMessage("js.dialysismedications.treatmentchart.select.Mrno.save");
		return false;
	}
	if (document.getElementById('prescriptionTOvisit').checked) {
		if (document.getElementById('visitId').options[document.getElementById('visitId').selectedIndex].value == '') {
			showMessage('js.dialysismedications.treatmentchart.select.visit');
			document.getElementById('visitId').focus();
			return false;
		}
	}
	return true;
}

function submitFilter() {
	if (mrNo == '') {
		showMessage("js.dialysismedications.treatmentchart.select.Mrno.search");
		return false;
	}
	document.treatmentForm._method.value = 'show';
	var filterTypes = document.getElementsByName('filterType');
	for(var i=0; i<filterTypes.length; i++) {
		if (filterTypes[i].checked) {
			if (filterTypes[i].value == 'all') {

			} else if (filterTypes[i].value == 'patient') {

			} else if (filterTypes[i].value == 'visit') {

				if(document.getElementById('filtervisitId').options[document.getElementById('filtervisitId').selectedIndex].value == '') {
					showMessage('js.dialysismedications.treatmentchart.select.visit.search');
					document.getElementById('filtervisitId').focus();
					return false;
				}
			}
		}
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
