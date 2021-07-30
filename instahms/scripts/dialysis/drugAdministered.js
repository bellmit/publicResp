 var Dom = YAHOO.util.Dom;
function initStandingtreatments() {
	initSIDialog();
	initEditSIDialog();
	initSIItemAutoComplete();
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
//	onSIItemChange();
	document.getElementById('s_d_itemName').focus();
	parentSIDialog = addSIDialog;
	return false;
}

/*function onSIItemChange() {
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

}*/

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
	var itemType = 'Medicine';
//	if (itemType == 'Instructions' || itemType == 'NonHospital') return null; // for doctor instrctions no need to create the autocomplete.

	var orgId = null;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeAction.do');
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
					{key : "item_type"}
				 ],
		numMatchFields: 2
	};

	siItemAutoComp = new YAHOO.widget.AutoComplete("s_d_itemName", "s_d_itemContainer", ds);
	siItemAutoComp.minQueryLength = 1;
	siItemAutoComp.animVert = false;
	siItemAutoComp.maxResultsDisplayed = 50;
	siItemAutoComp.resultTypeList = false;
	var forceSelection = false;
	if (itemType == 'Medicine')
		forceSelection = false;
	siItemAutoComp.forceSelection = forceSelection;

	siItemAutoComp1 = new YAHOO.widget.AutoComplete("s_ed_itemNameLabel", "s_ed_itemContainer", ds);
	siItemAutoComp1.minQueryLength = 1;
	siItemAutoComp1.animVert = false;
	siItemAutoComp1.maxResultsDisplayed = 50;
	siItemAutoComp1.resultTypeList = false;
	siItemAutoComp.forceSelection = false;

/*	siItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	siItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			highlightedValue += "(" + record.qty + ") "+ (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
		}
		return highlightedValue;
	}*/

	siItemAutoComp.dataRequestEvent.subscribe(clearSIMasterType);
/*	if (forceSelection) {
		siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
		siItemAutoComp.selectionEnforceEvent.subscribe(clearSIMasterType);
	} else {
		siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
	}*/

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
}

var S_ITEM_NAME = 1, S_DOSAGE = 2, S_ROUTE=3, S_REMARKS =4, S_QUANTITY=5, S_BATCH=6, S_DATE=7, S_ADMIN=8, S_DOCTOR=9, S_TRASH_COL=10, S_EDIT_COL=11;
var itemsAdded = 0;
function addSIToTable() {
	var dosage = document.getElementById('s_d_dosage').value;
	var itemName = document.getElementById('s_d_itemName').value;
	var itemId = document.getElementById('s_d_item_id').value;
   	var quantity = document.getElementById('s_d_quantity').value;
   	var expiryDate = document.getElementById('s_d_expirydate').value;
   	var batchNo = document.getElementById('s_d_batchno').value;
   	var staff = document.getElementById('s_d_staff').value;
   	var doctorID = document.getElementById('s_d_doctors').value;
   	var doctorName = document.getElementById('s_d_doctors').options[document.getElementById('s_d_doctors').selectedIndex].text;
   	var itemType = 'Medicine';

	if (itemName == '') {
   		showMessage('js.dialysis.drugadministered.select.item');
   		document.getElementById('s_d_itemName').focus();
   		return false;
   	}
/*	if (checkForSIDuplicates()) {
		showMessage("Duplicate entry : " + itemName);
		return false;
	}*/

	if (itemType == 'Medicine' && dosage == '') {
		showMessage("js.dialysis.drugadministered.select.dosage");
		document.getElementById('s_d_dosage').focus();
		return false;
	}

   	if (expiryDate != '' && !validateDateFormat2(expiryDate)) {
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
   	routeName = routeId == '' ? '' : routeName;

   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		setNodeText(row.cells[S_DOSAGE], dosage);

		setHiddenValue(id, "s_generic_code", genericCode);
		setHiddenValue(id, "s_generic_name", genericName);
		setHiddenValue(id, "s_qty_in_stock", document.getElementById('s_d_qty_in_stock').value);
	}

	setNodeText(row.cells[S_ROUTE], routeName);
	setNodeText(row.cells[S_REMARKS], remarks, 30);
	setNodeText(row.cells[S_QUANTITY], quantity);
	setNodeText(row.cells[S_BATCH], batchNo);
	setNodeText(row.cells[S_DATE], expiryDate);
	setNodeText(row.cells[S_ADMIN], staff);
	if (doctorID != '')
		setNodeText(row.cells[S_DOCTOR], doctorName);
	else
		setNodeText(row.cells[S_DOCTOR], '');

	setHiddenValue(id, "s_prescription_id", "_");
	setHiddenValue(id, "s_medicine_dosage", dosage);
	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_consumption_uom", consumption_uom);
	setHiddenValue(id, "s_route_id", routeId);
	setHiddenValue(id, "s_route_name", routeName);
	setHiddenValue(id, "s_quantity", quantity);
	setHiddenValue(id, "s_batch_no", batchNo);
	setHiddenValue(id, "s_expdate", expiryDate);
	setHiddenValue(id, "s_staff", staff);
	setHiddenValue(id, "s_doctor", doctorID);

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
	var el = getIndexedFormElement(document.postDialysis, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearSIFields() {
	document.getElementById('s_d_itemName').value = '';
   	document.getElementById('s_d_dosage').value = '';
   	document.getElementById('s_d_medicine_route').value = '';
   	document.getElementById('s_d_remarks').value = '';
   	document.getElementById('s_d_ispackage').value = '';
   	document.getElementById('s_d_medicineUOM').textContent = '';
   	document.getElementById('s_d_consumption_uom').value = '';
	document.getElementById('s_d_quantity').value = '';
	document.getElementById('s_d_batchno').value = '';
   	document.getElementById('s_d_expirydate').value = '';
   	document.getElementById('s_d_staff').value = '';
   	document.getElementById('s_d_doctors').value = '';
}

function setSIRowStyle(i) {
	var row = getChargeRow(i, 'siTable');
	var prescribedId = getIndexedValue("s_prescription_id", i);
	var qty_in_stock = getIndexedValue("s_qty_in_stock", i);
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

/*	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;*/

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
	var obj = getIndexedFormElement(document.postDialysis, name, index);
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
		var id = document.postDialysis.s_ed_editRowId.value;
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
	var obj = getIndexedFormElement(document.postDialysis, name, index);
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
	document.postDialysis.s_ed_editRowId.value = id;
	var itemType = 'Medicine';
	document.getElementById('s_ed_itemNameLabel').value = itemName;
	document.getElementById('s_ed_itemName').value = getIndexedValue("s_item_name", id);
	document.getElementById('s_ed_item_id').value = getIndexedValue("s_item_id", id);
	document.getElementById('s_ed_medicine_route').value = getIndexedValue('s_route_id', id);
	document.getElementById('s_ed_presc_date').textContent = getIndexedValue('s_prescribed_date', id);
	var master = getIndexedValue("s_item_master", id);

	if (itemType == 'Medicine') {
		document.getElementById('s_ed_dosage').disabled = false;
		document.getElementById('s_ed_remarks').disabled = false;
		document.getElementById('s_ed_dosage').value = getIndexedValue("s_medicine_dosage", id);

	}
	var prescId = getIndexedValue("s_prescription_id", id);

	document.getElementById('s_ed_remarks').value = getIndexedValue('s_item_remarks', id);
	document.getElementById('s_ed_item_master').value = getIndexedValue('s_item_master', id);
	document.getElementById('s_ed_remarks').focus();
	document.getElementById('s_ed_quantity').value = getIndexedValue('s_quantity', id);
	document.getElementById('s_ed_expirydate').value = getIndexedValue('s_expdate', id);
	document.getElementById('s_ed_batchno').value = getIndexedValue('s_batch_no', id);
	document.getElementById('s_ed_staff').value = getIndexedValue('s_staff', id);
	document.getElementById('s_ed_doctors').value = getIndexedValue('s_doctor', id);

	return false;
}

function editSITableRow() {
	var id = document.postDialysis.s_ed_editRowId.value;
	var row = getChargeRow(id, 'siTable');

	var itemType = 'Medicine';
   	var itemName = document.getElementById('s_ed_itemNameLabel').value;
   	var itemId = document.getElementById('s_ed_item_id').value;
   	var dosage = document.getElementById('s_ed_dosage').value;
   	var remarks = document.getElementById('s_ed_remarks').value;
   	var master = document.getElementById('s_ed_item_master').value;
   	var consumption_uom = document.getElementById('s_ed_consumption_uom').value;
   	var quantity = document.getElementById('s_ed_quantity').value;
   	var batchNo = document.getElementById('s_ed_batchno').value;
   	var expiryDate = document.getElementById('s_ed_expirydate').value;
   	var staff = document.getElementById('s_ed_staff').value;
   	var routeTxt = document.getElementById('s_ed_medicine_route').options[document.getElementById('s_ed_medicine_route').selectedIndex].text;
   	var routeVal = document.getElementById('s_ed_medicine_route').options[document.getElementById('s_ed_medicine_route').selectedIndex].value;
   	var doctorID = document.getElementById('s_ed_doctors').options[document.getElementById('s_ed_doctors').selectedIndex].value;
   	var doctorName = document.getElementById('s_ed_doctors').options[document.getElementById('s_ed_doctors').selectedIndex].text;

   	if (expiryDate != '' && !validateDateFormat2(expiryDate)) {
   		return false;
   	}

   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
	setNodeText(row.cells[S_DOSAGE], dosage);
	setNodeText(row.cells[S_REMARKS], remarks, 30);
	setNodeText(row.cells[S_QUANTITY], quantity);
	setNodeText(row.cells[S_BATCH], batchNo);
	setNodeText(row.cells[S_DATE], expiryDate);
	setNodeText(row.cells[S_ADMIN], staff);
	if (routeVal != '')
		setNodeText(row.cells[S_ROUTE], routeTxt);
	else
		setNodeText(row.cells[S_ROUTE], '');
	if (doctorID != '')
		setNodeText(row.cells[S_DOCTOR], doctorName);
	else
		setNodeText(row.cells[S_DOCTOR], '');

	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "s_medicine_dosage", dosage);
	setHiddenValue(id, "s_route_id", routeVal);
	setHiddenValue(id, "s_quantity", quantity);
	setHiddenValue(id, "s_batch_no", batchNo);
	setHiddenValue(id, "s_expdate", expiryDate);
	setHiddenValue(id, "s_staff", staff);
	setHiddenValue(id, "s_doctor", doctorID);

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
	var id = document.postDialysis.s_ed_editRowId.value;
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
	var id = document.postDialysis.s_ed_editRowId.value;
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
	var issuedItems = document.getElementsByName("s_discontinued");
	var dItemType = 'Medicine';
	alert('befor');
	alert(document.getElementById("s_d_item_id").value);
	var dItemName = document.getElementById("s_d_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	if (dItemType == 'NonHospital' || (dItemType == 'Medicine')) {
		dItemName = document.getElementById('s_itemName').value;
		itemNames = document.getElementsByName('s_item_name');
	}
	alert('before chk duplicates');

	for (var i=0; i<itemTypes.length-1; i++) {
		if (dItemType == itemTypes[i].value) {
			if (itemNames[i].value.trim().toLowerCase() == dItemName.trim().toLowerCase()) {
				var issued = issuedItems[i].value;
				if (issued == 'N') return true;
			}
		}
	}
	return false;
}

function allowForUnDiscontinue(index) {
	var itemTypes = document.getElementsByName("s_itemType");
	var issuedItems = document.getElementsByName("s_discontinued");
	var dItemName = document.getElementById("s_ed_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	if (dItemType == 'NonHospital' || (dItemType == 'Medicine')) {
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

function validateDateFormat2(dateStr) {

	var myarray = dateStr.split("-");

	if (myarray.length != 2) {
		showMessage("js.dialysis.drugadministered.incorrectdateformat.mm.yyyy");
		return false;
	}

	var mth = myarray[0];
	var yr = myarray[1];

	if (!isInteger(mth)) {
		showMessage("js.dialysis.drugadministered.incorrectdateformat.month");
		return false;
	}
	if (!isInteger(yr)) {
		showMessage("js.dialysis.drugadministered.incorrectdateformat.year");
		return false;
	}

    if (parseInt(mth) > 12) {
    	showMessage("js.dialysis.drugadministered.incorrectdateformat.entercorrectformat");
        return false;
    }

    if ( (yr.length != 2) && (yr.length != 4) ) {
    	showMessage("js.dialysis.drugadministered.incorrectdateformat.year.2or4digit");
		return false;
	}

	return true;
}

function fillFromBill() {
	var req = null;
	var url = cpath+"/dialysis/PostDialysisSessions.do?_method=fillFromBill&mr_no="+mr_no;
	var result = null;
	if (window.XMLHttpRequest) {
		req = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		req = new ActiveXObject("MSXML2.XMLHTTP");
	}
	req.open('POST', url.toString(), false);
	req.send(null);

	if (req.readyState == 4) {
		if (req.status == 200 && req.responseText != null) {
			result = eval(req.responseText);
		}
	}
	var medicineIds = document.getElementsByName("s_item_id");
	if (result != null && result.length != 0) {
		for (var i=0; i<result.length; i++) {
			var fill = true;
				for (var j=0; j<medicineIds.length; j++) {
					if (medicineIds[j].value == result[i].medicine_id) {
						fill = false;
						break;
					} else {
						fill = true;
					}
				}
			if (fill) {
				var id = getNumCharges('siTable');
			   	var table = document.getElementById("siTable");
				var templateRow = table.rows[getTemplateRow('siTable')];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);
				document.getElementsByName('trashCanAnchor')[id].style.display = 'block';
			   	row.id = "s_itemRow" + id;

			   	var itemName = result[i].medicine_name;
			   	var itemId = result[i].medicine_id;
			   	var remarks = '';
			   	var routeId = result[i].route_of_admin;
			   	var routeName = result[i].route_name;
			   	var quantity = result[i].qty;
			   	var batchNo = result[i].batch_no;
			   	var expiryDate = formatDate(new Date(result[i].exp_dt),'mmyyyy','-');
			   	routeName = routeId == '' ? '' : routeName;

			   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
				setNodeText(row.cells[S_ROUTE], routeName);
				setNodeText(row.cells[S_REMARKS], remarks, 30);
				setNodeText(row.cells[S_QUANTITY], quantity);
				setNodeText(row.cells[S_BATCH], batchNo);
				setNodeText(row.cells[S_DATE], expiryDate);
		/*		setNodeText(row.cells[S_ADMIN], staff);
				if (doctorID != '')
					setNodeText(row.cells[S_DOCTOR], doctorName);
				else
					setNodeText(row.cells[S_DOCTOR], '');*/

				setHiddenValue(id, "s_prescription_id", "_");
				setHiddenValue(id, "s_item_name", itemName);
				setHiddenValue(id, "s_item_id", itemId);
				setHiddenValue(id, "s_item_remarks", remarks);
				setHiddenValue(id, "s_route_id", routeId);
				setHiddenValue(id, "s_route_name", routeName);
				setHiddenValue(id, "s_quantity", quantity);
				setHiddenValue(id, "s_batch_no", batchNo);
				setHiddenValue(id, "s_expdate", expiryDate);

				setSIRowStyle(id);
				itemsAdded++;
			}

		}
	}

}

function formatDate(dateObj, format, separator) {
	var year = dateObj.getFullYear();
	var monthIndex = dateObj.getMonth();
	var month = monthIndex + 1;
	var day = dateObj.getDate();

	if (("" + month).length == 1) {
		month = "0" + month;
	}
	if (("" + day).length == 1) {
		day = "0" + day;
	}

	if (separator == null) {
		separator = "-";
	}
	if (format == null) {
		format = "ddmmyyyy";
	}

	if (format == "ddmmyyyy") {
		return day + separator + month + separator + year;
	} else if (format == "mmddyyyy") {
		return month + separator + day + separator + year;
	} else if (format == "mmyyyy") {
		return month + separator + year;
	} else if (format == "mmyy") {
		return month + separator + year.toString().substr(2,4);
	} else if (format == "ddmonyyyy") {
		return day + separator + gMonthShortNames[monthIndex] + separator + year;
	} else if (format == "monyyyy") {
		return gMonthShortNames[monthIndex] + separator + year;
	} else if (format == "monyy") {
		return gMonthShortNames[monthIndex] + separator + year.toString().substr(2,4);
	} else if (format == "yyyymmdd") {
		return year + separator + month + separator + day;
	} else if (format == "yyyymondd") {
		return year + separator + gMonthShortNames[monthIndex] + separator + day;
	}
	return "";
}
