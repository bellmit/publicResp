 var Dom = YAHOO.util.Dom;
function initStandingInstructions() {
	initSIDialog();
	initEditSIDialog();
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
	                                              { fn:handleAddItemCancel,
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

function getSIItemType() {
	var itemTypeObj = document.getElementsByName('s_d_itemType');
	for (var i=0; i<itemTypeObj.length; i++) {
		if (itemTypeObj[i].checked)
			return itemTypeObj[i].value;
	}
	return null;
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
	var itemType = getSIItemType();
	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient
		&& ((itemType == 'Medicine' && use_store_items == 'Y')
				|| itemType == 'Inv.'
				|| itemType == 'Service'
				|| itemType == 'Operation')) {
		document.getElementById('s_d_prior_auth_row').style.display = 'table-row';
	} else {
		document.getElementById('s_d_prior_auth_row').style.display = 'none';
	}
	toggleSIItemFormRow(true);
	return false;
}

function onSIItemChange() {
	clearSIFields();
	initSIItemAutoComplete();
	toggleSIItemFormRow(true);
	var itemType = getSIItemType();

	if (itemType == "All") {

	} else if (itemType == "Medicine" || itemType == 'NonHospital') {
		//document.getElementById('dGenericNameRow').style.display = 'table-row';
		document.getElementById('s_d_dosage').disabled = false;
		document.getElementById('s_d_remarks').disabled = false;
		document.getElementById('s_d_medicine_route').disabled = false;
		document.getElementById('s_d_order_lead_time').disabled = true;

	} else {
		if (itemType == "Instructions") {
		 	document.getElementById('s_d_remarks').disabled = true;
		 	document.getElementById('s_d_order_lead_time').disabled = true;
		} else {
			document.getElementById('s_d_remarks').disabled = false;
			document.getElementById('s_d_order_lead_time').disabled = false;
		}
		if (itemType == 'Inv.')
			document.getElementById('s_d_order_lead_time').value = testLeadTime;
		else if (itemType == 'Service')
			document.getElementById('s_d_order_lead_time').value = serviceLeadTime;
		else if (itemType == 'Doctor')
			document.getElementById('s_d_order_lead_time').value = consLeadTime;

		document.getElementById('s_d_dosage').disabled = true;
		document.getElementById('s_d_medicine_route').disabled = true;

		document.getElementById('s_d_genericNameAnchor_dialog').innerHTML = '';
 		document.getElementById('s_d_genericNameAnchor_dialog').style.display = 'none';
   		document.getElementById('s_d_genericNameAnchor_dialog').href = '';
	}

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient
		&& ((itemType == 'Medicine' && use_store_items == 'Y')
				|| itemType == 'Inv.'
				|| itemType == 'Service'
				|| itemType == 'Operation')) {
		document.getElementById('s_d_prior_auth_row').style.display = 'table-row';
	} else {
		document.getElementById('s_d_prior_auth_row').style.display = 'none';
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

function toggleSIItemFormRow(addDialog) {
	var prefix = addDialog ? 's_d_' : 's_ed_';
	var itemType = addDialog ? getSIItemType() : document.getElementById(prefix + 'itemType').value;
	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById(prefix + 'itemFormRow').style.display = 'table-row';
		// allow user to select the medicine form if it is a prescription by generics.
		if (itemType == 'NonHospital') {
			document.getElementById(prefix + 'item_form_id').disabled = false;
			document.getElementById(prefix + 'item_strength').disabled = false;
		} else {
			document.getElementById(prefix + 'item_form_id').disabled = true;
			document.getElementById(prefix + 'item_strength').disabled = true;
		}
	} else {
		document.getElementById(prefix + 'itemFormRow').style.display = 'none';
	}
}


var siItemAutoComp = null;
function initSIItemAutoComplete() {
	if (!empty(siItemAutoComp)) {
		siItemAutoComp.destroy();
		siItemAutoComp = null;
	}
	var itemType = getSIItemType();
	if (itemType == 'Instructions' || itemType == 'NonHospital') return null; // for doctor instrctions no need to create the autocomplete.

	var orgId = document.getElementById('org_id').value;
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
					{key : "item_type"},
					{key : 'prior_auth_required'},
					{key : 'consumption_uom'},
					{key : 'item_form_id'},
					{key : 'item_strength'}
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
			// show qty only for pharmacy items.
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
	if (record.item_type == 'Medicine')	getRouteOfAdministrations();

	var prior_auth = record.prior_auth_required;
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}
	document.getElementById('s_d_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('s_d_priorAuth').value = prior_auth;
	document.getElementById('s_d_item_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('s_d_item_strength').value = record.item_strength;

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
	document.getElementById('s_d_priorAuth_label').textContent = '';
	document.getElementById('s_d_priorAuth').value = '';
	document.getElementById('s_d_item_form_id').value = '';
	document.getElementById('s_d_item_strength').value = '';
}

var S_DOCTOR=1, S_ITEM_TYPE=2, S_ITEM_NAME = 3, S_FORM = 4, S_STRENGTH = 5, S_DOSAGE = 6, S_SCHEDULE=7, S_ROUTE=8, S_REMARKS = 9, S_TRASH_COL=10,
	S_EDIT_COL=11;
var itemsAdded = 0;
function addSIToTable() {
	var dosage = document.getElementById('s_d_dosage').value;
	var itemType = getSIItemType();
	var itemName = document.getElementById('s_d_itemName').value;
	var itemId = document.getElementById('s_d_item_id').value;
	var frequency = document.getElementById('s_d_frequency_name').options[document.getElementById('s_d_frequency_name').selectedIndex].text;
   	var frequencyId = document.getElementById('s_d_frequency_name').value;
   	var interval = document.getElementById('s_d_interval').value;
   	var addActivityEl = document.getElementById('s_d_addActivity');

	if (itemName == '') {
   		alert('Please prescribe the item');
   		document.getElementById('s_d_itemName').focus();
   		return false;
   	}
	if (itemType == 'Medicine' && dosage == '') {
		alert("Please enter the dosage");
		document.getElementById('s_d_dosage').focus();
		return false;
	}

   	var addActivity = 'false';
   	if (addActivityEl && addActivityEl.checked) {
   		addActivity = 'true';
	}
	if (frequency == '') {
   		alert("Please enter the frequency");
   		document.getElementById('s_d_frequency_name').focus();
   		return false;
   	}
   	if (frequencyId == 0 && (interval == '' || interval == 0)) {
   		alert("Please enter the Interval greater than zero");
   		document.getElementById('s_d_interval').focus();
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
   	var doctorId = document.getElementById('consult_doctor_id').value;
   	var doctorName = document.getElementById('consult_doctor_name').value;
   	var remarks = document.getElementById('s_d_remarks').value;
   	var master = document.getElementById('s_d_item_master').value;
   	var genericCode = document.getElementById('s_d_generic_code').value;
   	var genericName = document.getElementById('s_d_generic_name').value;
   	var ispackage = document.getElementById('s_d_ispackage').value;
   	var consumption_uom = document.getElementById('s_d_consumption_uom').value;
   	var order_lead_time = document.getElementById('s_d_order_lead_time').value;
   	var routeId = document.getElementById('s_d_medicine_route').options[document.getElementById('s_d_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('s_d_medicine_route').options[document.getElementById('s_d_medicine_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;
   	var priorAuth = document.getElementById('s_d_priorAuth').value;
   	var item_form_id = document.getElementById('s_d_item_form_id').value;
   	var item_strength = document.getElementById('s_d_item_strength').value;
   	var item_form_name = document.getElementById('s_d_item_form_id').options[document.getElementById('s_d_item_form_id').selectedIndex].text;

	setNodeText(row.cells[S_DOCTOR], doctorName, 20);
   	setNodeText(row.cells[S_ITEM_TYPE], itemType);
   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		setNodeText(row.cells[S_DOSAGE], dosage);
		if (item_form_id != '') {
			setNodeText(row.cells[S_FORM], item_form_name, 15);
		}
		setNodeText(row.cells[S_STRENGTH], item_strength, 15);

		setHiddenValue(id, "s_generic_code", genericCode);
		setHiddenValue(id, "s_generic_name", genericName);
		setHiddenValue(id, "s_qty_in_stock", document.getElementById('s_d_qty_in_stock').value);
		setHiddenValue(id, "s_item_form_id", item_form_id);
		setHiddenValue(id, "s_item_strength", item_strength);

	}

	setNodeText(row.cells[S_SCHEDULE], frequency);
	setNodeText(row.cells[S_ROUTE], routeName);
	setNodeText(row.cells[S_REMARKS], remarks, 30);

	setHiddenValue(id, "s_prescription_id", "_");
	setHiddenValue(id, "s_doctor_id", doctorId);
	setHiddenValue(id, "s_repeat_interval", interval);
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
	setHiddenValue(id, "s_order_lead_time", order_lead_time);
	setHiddenValue(id, "s_route_id", routeId);
	setHiddenValue(id, "s_route_name", routeName);
	setHiddenValue(id, "s_priorAuth", priorAuth);

	setSIRowStyle(id);
	itemsAdded++;
	clearSIFields();

	this.align("tr", "tl");
	document.getElementById('s_d_itemName').focus();
	return id;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.prescribeForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearSIFields() {
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
	document.getElementById('s_d_priorAuth_label').textContent = '';
	document.getElementById('s_d_priorAuth').value = '';
	document.getElementById('s_d_item_form_id').value = '';
	document.getElementById('s_d_item_strength').value = '';
}

function setSIRowStyle(i) {
	var row = getChargeRow(i, 'siTable');
	var prescribedId = getIndexedValue("s_prescription_id", i);
	var qty_in_stock = getIndexedValue("s_qty_in_stock", i);
	var flagImgs = row.cells[S_DOCTOR].getElementsByTagName("img");
	var trashImgs = row.cells[S_TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("s_delItem", i) == 'true';
	var edited = getIndexedValue("s_edited", i) == 'true';
	var priorAuth = getIndexedValue("s_priorAuth", i);

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
	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else if (priorAuth == 'A') {
		flagSrc = cpath + '/images/blue_flag.gif';
	} else if (priorAuth == 'S') {
		flagSrc = cpath + "/images/green_flag.gif";
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
	                                              { fn:handleEditItemCancel,
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
		var id = document.prescribeForm.s_ed_editRowId.value;
		var row = getChargeRow(id, "siTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		parentSIDialog = null;
		siFieldEdited = false;
		this.hide();
	}
}

function showEditSIDialog(obj) {
	parentDialog = editSIDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editSIDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editSIDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	var itemName = getIndexedValue("s_item_name", id);
	document.prescribeForm.s_ed_editRowId.value = id;
	var itemType = getIndexedValue("s_itemType", id);
	document.getElementById('s_ed_itemTypeLabel').textContent = itemType;
	document.getElementById('s_ed_itemNameLabel').textContent = itemName;
	document.getElementById('s_ed_itemName').value = getIndexedValue("s_item_name", id);
	document.getElementById('s_ed_item_id').value = getIndexedValue("s_item_id", id);
	document.getElementById('s_ed_itemType').value = itemType;
	document.getElementById('s_ed_medicine_route').textContent = getIndexedValue('s_route_name', id);
	document.getElementById('s_ed_presc_date').textContent = getIndexedValue('s_prescribed_date', id);
	var master = getIndexedValue("s_item_master", id);
	if (master == 'item_master') {
		document.getElementById('s_ed_itemMasterType').textContent = 'Yes';
	} else {
		document.getElementById('s_ed_itemMasterType').textContent = 'No';
	}
	toggleSIItemFormRow(false);
	if (itemType == 'Medicine' || itemType == 'NonHospital') {
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
	} else {
		if (itemType == 'Instruction') {
			document.getElementById('s_ed_remarks').disabled = true;
		} else {
			document.getElementById('s_ed_remarks').disabled = false;
		}
		document.getElementById('s_ed_dosage').disabled = true;
		document.getElementById('s_ed_dosage').value = '';

		document.getElementById('s_ed_genericNameAnchor_editdialog').innerHTML = '';
		document.getElementById('s_ed_genericNameAnchor_editdialog').style.display = 'none';
		document.getElementById('s_ed_genericNameAnchor_editdialog').href='';
	}
	var prescId = getIndexedValue("s_prescription_id", id);
	var check = (getIndexedValue('s_addActivity', id) == 'true');
	var addActivity = document.getElementById('s_ed_addActivity');
	addActivity.checked = check;
	if (prescId != '_' && check) {
		addActivity.disabled = true;
	} else {
		addActivity.disabled = false;
	}
	document.getElementById('s_ed_discontinue').checked = (getIndexedValue("s_discontinued", id) == 'Y');
	document.getElementById('s_ed_frequency_name').value = getIndexedValue('s_recurrence_daily_id', id);
	document.getElementById('s_ed_interval').value = getIndexedValue('s_repeat_interval', id);
	document.getElementById('s_ed_ispackage').value = getIndexedValue("s_ispackage", id);
	document.getElementById('s_ed_remarks').value = getIndexedValue('s_item_remarks', id);
	document.getElementById('s_ed_item_master').value = getIndexedValue('s_item_master', id);
	document.getElementById('s_ed_order_lead_time').value = getIndexedValue('s_order_lead_time', id);

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient
		&& ((itemType == 'Medicine' && use_store_items == 'Y')
				|| itemType == 'Inv.'
				|| itemType == 'Service'
				|| itemType == 'Operation')) {
		document.getElementById('s_ed_prior_auth_row').style.display = 'table-row';
	} else {
		document.getElementById('s_ed_prior_auth_row').style.display = 'none';
	}

	var prior_auth = getIndexedValue('s_priorAuth', id);
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}
	document.getElementById('s_ed_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('s_ed_priorAuth').value = prior_auth;

	document.getElementById('s_ed_remarks').focus();

	return false;
}

function editSITableRow() {
	var id = document.prescribeForm.s_ed_editRowId.value;
	var row = getChargeRow(id, 'siTable');

	var itemType = document.getElementById('s_ed_itemType').value;
   	var itemName = document.getElementById('s_ed_itemName').value;
   	var itemId = document.getElementById('s_ed_item_id').value;
   	var dosage = document.getElementById('s_ed_dosage').value;
   	var remarks = document.getElementById('s_ed_remarks').value;
   	var master = document.getElementById('s_ed_item_master').value;
   	var ispackage = document.getElementById('s_ed_ispackage').value;
   	var consumption_uom = document.getElementById('s_ed_consumption_uom').value;
   	var order_lead_time = document.getElementById('s_ed_order_lead_time').value;
   	var frequency = document.getElementById('s_ed_frequency_name').options[document.getElementById('s_ed_frequency_name').selectedIndex].text;
   	var frequencyId = document.getElementById('s_ed_frequency_name').value;
   	var interval = document.getElementById('s_ed_interval').value;
   	var doctorId = document.getElementById('consult_doctor_id').value;
   	var doctorName = document.getElementById('consult_doctor_name').value;
   	var addDiscontiuedEl = document.getElementById('s_ed_discontinue');
   	var consumptionUOM = document.getElementById('s_ed_consumption_uom').value;
   	var item_form_id = document.getElementById('s_ed_item_form_id').value;
   	var item_strength = document.getElementById('s_ed_item_strength').value;
   	var item_form_name = document.getElementById('s_ed_item_form_id').options[document.getElementById('s_ed_item_form_id').selectedIndex].text;
   	var addActivity = 'false';

   	if (frequency == '') {
   		alert("Please enter the frequency");
   		document.getElementById('s_ed_frequency_name').focus();
   		return false;
   	}
   	if (frequencyId == 0 && (interval == '' || interval == 0)) {
   		alert("Please enter the Interval greater than zero");
   		document.getElementById('s_ed_interval').focus();
   		return false;
   	}

   	var addActivityEl = document.getElementById('s_ed_addActivity');
   	if (addActivityEl && addActivityEl.checked) {
   		addActivity = 'true';
   	}

   	var discontinued = 'N';
   	if (addDiscontiuedEl && addDiscontiuedEl.checked) {
   		discontinued = 'Y';
   	}
	setNodeText(row.cells[S_DOCTOR], doctorName, 20);
	setNodeText(row.cells[S_ITEM_TYPE], itemType);
   	setNodeText(row.cells[S_ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		setNodeText(row.cells[S_DOSAGE], dosage);
		if (item_form_id != '')
			setNodeText(row.cells[S_FORM], item_form_name, 15);
		setNodeText(row.cells[S_STRENGTH], item_strength, 15);

		setHiddenValue(id, "s_medicine_dosage", dosage);
		setHiddenValue(id, "s_item_form_id", item_form_id);
		setHiddenValue(id, "s_item_strength", item_strength);
	}

	setNodeText(row.cells[S_SCHEDULE], frequency);
	setNodeText(row.cells[S_REMARKS], remarks, 30);

	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);
	setHiddenValue(id, "s_recurrence_daily_id", frequencyId);
	setHiddenValue(id, "s_consumption_uom", consumption_uom);
	setHiddenValue(id, "s_order_lead_time", order_lead_time);
	setHiddenValue(id, "s_doctor_id", doctorId);
	setHiddenValue(id, "s_repeat_interval", interval);
	setHiddenValue(id, "s_medicine_dosage", dosage);
	setHiddenValue(id, "s_discontinued", discontinued);
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
	var id = document.prescribeForm.s_ed_editRowId.value;
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
	var id = document.prescribeForm.s_ed_editRowId.value;
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
	var dItemType = getSIItemType();
	var dItemName = document.getElementById("s_d_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	if (dItemType == 'NonHospital' || (dItemType == 'Medicine' && use_store_items != 'Y')) {
		dItemName = document.getElementById('s_d_itemName').value;
		itemNames = document.getElementsByName('s_item_name');
	}

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

