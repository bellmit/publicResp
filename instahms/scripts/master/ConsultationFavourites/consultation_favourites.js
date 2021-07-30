var useGenerics = (prescriptions_by_generics == 'true');
function init() {
	initItemDialog();
	initEditItemDialog();
	initFrequencyAutoComplete();
	initInstructionAutoComplete();
	editDialogGeneric();
}

function modifyUOMLabel(obj, prefix) {
	document.getElementById(prefix+ '_consumption_uom_label').textContent = obj.value;
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

	clearFields();
	var itemType = getItemType();
	if (itemType == 'Medicine') {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'table-row';
	} else {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'none';
	}
	document.getElementById('d_itemName').focus();
	parentDialog = addItemDialog;
	return false;
}

function onItemChange(){
	clearFields();
	var itemType = getItemType();
	if (itemType == 'Medicine') {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'table-row';
	} else {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'none';
	}

	if (itemType == "All") {

	} else if (itemType == "Medicine" || itemType == 'NonHospital') {
		document.getElementById('d_strength').disabled = false;
		document.getElementById('d_admin_strength').disabled = false;
		document.getElementById('d_frequency').disabled = false;
		document.getElementById('d_duration').disabled = false;
		document.getElementById('d_qty').disabled = false;
		document.getElementById('d_remarks').disabled = false;
		document.getElementById('d_medicine_route').disabled = false;

	} else {
		document.getElementById('d_remarks').disabled = false;
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

var itemAutoComp = null;
function initItemAutoComplete() {
	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	var nonHospMedicine = document.getElementById('d_non_hosp_medicine').checked
	var itemType = getItemType();
	if (itemType == 'NonHospital' || (itemType == 'Medicine' && nonHospMedicine)) return null; // for doctor instrctions no need to create the autocomplete.

	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeAction.do');
	ds.scriptQueryAppend = "_method=findItemsForFavourites&searchType=" + itemType + "&center_id=" + centerId ;
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
					{key : "consumption_uom"},
					{key : "cons_uom_id"},
					{key : 'prior_auth_required'},
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
	var non_hosp_medicine = addDialog ? document.getElementById('d_non_hosp_medicine').checked+'' : document.getElementById('ed_non_hosp_medicine').value;

	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById(prefix + 'itemFormRow').style.display = 'table-row';
		// allow user to select the medicine form if it is a prescription by generics.
		if ((itemType == 'Medicine' && non_hosp_medicine == 'true') || itemType == 'NonHospital' || useGenerics) {
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

function toggleDurationUnits(enable, prefix) {
	enable = empty(enable) ? false : enable;
	var els = document.getElementsByName(prefix+"_duration_units");
	for (var i=0; i<els.length; i++) {
		els[i].disabled = !enable;
		els[i].checked = false;
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

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_item_id').value = record.item_id;
	if (record.item_type == 'Medicine') {
		if (!empty(record.generic_name)) {
			document.getElementById('genericNameAnchor_dialog').style.display = 'block';
			document.getElementById('genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "", "dialog", "'+record.generic_code+'")';
			document.getElementById('genericNameAnchor_dialog').innerHTML = record.generic_name;
			document.getElementById('d_generic_code').value = record.generic_code;
			document.getElementById('d_generic_name').value = record.generic_name;
		}
	}
	document.getElementById('d_cons_uom_id').value = record.cons_uom_id == 0 || record.cons_uom_id == null ? '' : record.cons_uom_id;
	document.getElementById('d_consumption_uom_label').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_ispackage').value = record.ispkg;
	document.getElementById('d_item_master').value = record.master;
	document.getElementById('d_item_form_id').value = record.item_form_id == 0 || record.item_form_id == null ? '' : record.item_form_id;
	document.getElementById('d_item_strength').value = record.item_strength;
	document.getElementById('d_item_strength_units').value = record.item_strength_units;
	document.getElementById('d_item_strength_units').selectedIndex=document.getElementById('d_item_strength_units').selectedIndex == -1 ? 0:
		document.getElementById('d_item_strength_units').selectedIndex;
	document.getElementById('d_granular_units').value = record.granular_units;
	if (record.granular_units != 'Y') {
		document.getElementById('d_qty').value = 1;
	}
	if (record.item_type == 'Medicine' && !useGenerics)	getRouteOfAdministrations();
}

function getRouteOfAdministrations() {
	var itemId = document.getElementById('d_item_id').value;
	var itemName = document.getElementById('d_itemName').value;
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
			document.getElementById('d_medicine_route').length = 1;
			return ;
		}
		var routeIds = routes.route_id.split(",");
		var routeNames = routes.route_name.split(",");
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
	}
}

function onGetRoutesFailure() {
}


function clearItemDetails(oSelf) {
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_item_id').value = '';

	document.getElementById('d_admin_strength').value = '';
	document.getElementById('d_frequency').value = '';
	document.getElementById('d_strength').value = '';
	document.getElementById('d_duration').value = '';

	var itemType = getItemType();
	var allRoutes = !(itemType == 'Medicine' && !document.getElementById('d_non_hosp_medicine').checked && !useGenerics);
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

	var enable = itemType == 'Medicine' || itemType == 'NonHospital';
	toggleDurationUnits(enable, 'd');
	if (enable) {
		// disable if it is prescription by generic names for medicines.
		if (itemType == 'Medicine' && useGenerics && !document.getElementById('d_non_hosp_medicine').checked) {
			document.getElementById('d_cons_uom_id').disabled = true;
		} else {
			document.getElementById('d_cons_uom_id').disabled = false;
		}
		document.getElementsByName('d_duration_units')[0].checked = true;
		document.getElementById('d_remarks').value = 'USE <Number of Unit> <Granular Unit> <Frequency> FOR A DURATION OF <Duration>.';
	} else {
		document.getElementById('d_cons_uom_id').disabled = true;
		document.getElementById('d_remarks').value = '';
	}

	document.getElementById('d_qty').value = '';
	document.getElementById('d_remarks').value = '';
	document.getElementById('d_special_instruction').value = '';
	document.getElementById('d_cons_uom_id').value = '';
	document.getElementById('d_consumption_uom_label').textContent = '';
	document.getElementById('genericNameAnchor_dialog').style.display = 'none';
	document.getElementById('genericNameAnchor_dialog').href = '';
	document.getElementById('genericNameAnchor_dialog').innerHTML = '';
	document.getElementById('d_generic_code').value = '';
	document.getElementById('d_generic_name').value = '';
	document.getElementById('d_ispackage').value = '';
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_item_form_id').value = '';
	document.getElementById('d_item_strength').value = '';
	document.getElementById('d_item_strength_units').value = '';
	document.getElementById('d_granular_units').value = '';
	document.getElementById('d_display_order').value = '';

}

var colIndex  = 0;
var ITEM_TYPE = colIndex++, DISPLAY_ORDER=colIndex++, ITEM_NAME = colIndex++, FORM =  colIndex++, STRENGTH = colIndex++,
	ADMIN_STRENGTH = colIndex++, DETAILS = colIndex++, ROUTE = colIndex++, REMARKS = colIndex++, SPECIAL_INSTRUCTION = colIndex++;
	QTY = colIndex++, TRASH_COL = colIndex++, EDIT_COL = colIndex++;
var itemsAdded = 0;
function addToTable() {
	var itemName = document.getElementById('d_itemName').value;
	if (itemName == '') {
   		alert('Please select the item');
   		document.getElementById('d_itemName').focus();
   		return false;
   	}
   	var display_order = document.getElementById('d_display_order').value;
   	if (display_order == '') {
   		alert('Please enter the display order');
   		document.getElementById('d_display_order').focus();
   		return false;
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

   	var strUnitEl = document.getElementById('d_item_strength_units');
   	var strength_unit_name = '';
   	if (!strUnitEl.disabled) {
   		strength_unit_name = document.getElementById('d_item_strength_units').options[document.getElementById('d_item_strength_units').selectedIndex].text;
   		strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;
   	}
   	var duration = document.getElementById('d_duration').value;
   	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById('d_duration').focus();
		return false;
	}
	var duration_radio_els = document.getElementsByName('d_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	if (!empty(duration) && empty(duration_units)) {
		alert("Please select the duration units");
		return false;
	}
   	var itemType = getItemType();

	if (strength === '' && itemType == 'Medicine' && !useGenerics) {
   		alert('Dosage cannot be empty');
   		return false;
	}
	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	   		document.getElementById('d_strength').focus();
	   		return false;
	   	}
	var cons_uom_id = document.getElementById('d_cons_uom_id').value;
	if (cons_uom_id === '' && itemType == 'Medicine' && !useGenerics) {
   		alert('Please select the granular unit');
   		return false;
	}

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
   	var spl_instruction = document.getElementById('d_special_instruction').value;
   	var master = document.getElementById('d_item_master').value;
   	var genericCode = document.getElementById('d_generic_code').value;
   	var genericName = document.getElementById('d_generic_name').value;
   	var ispackage = document.getElementById('d_ispackage').value;
   	
   	var routeId = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;
   	var item_form_id = document.getElementById('d_item_form_id').value;
   	var item_form_name = document.getElementById('d_item_form_id').options[document.getElementById('d_item_form_id').selectedIndex].text;
   	var non_hosp_medicine = false;
   	if (itemType == 'Medicine')
   		non_hosp_medicine = document.getElementById('d_non_hosp_medicine').checked;

   	setNodeText(row.cells[ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine ? '[Non Hosp]' : ''));
   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {

		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
   		var details = "";
   		if (frequency != '' || duration != '')
   			details = frequency + " / " + duration + duration_units;
   		setNodeText(row.cells[DETAILS], details, 20);
		setNodeText(row.cells[QTY], qty);
		if (item_form_id != '') {
			setNodeText(row.cells[FORM], item_form_name, 15);
		}
		setNodeText(row.cells[STRENGTH], item_strength + ' ' + strength_unit_name, 15);

		setHiddenValue(id, "generic_code", genericCode);
		setHiddenValue(id, "generic_name", genericName);
		setHiddenValue(id, "admin_strength", adminStrength);
		setHiddenValue(id, "frequency", frequency);
		setHiddenValue(id, "strength", strength);
		setHiddenValue(id, "duration", duration);
		setHiddenValue(id, "duration_units", duration_units);
		setHiddenValue(id, "medicine_quantity", qty);
		setHiddenValue(id, "item_form_id", item_form_id);
		setHiddenValue(id, "granular_units", granular_unit);
		setHiddenValue(id, "item_strength", item_strength);
		setHiddenValue(id, "item_strength_units", item_strength_units);
		setHiddenValue(id,  "presc_by_generics", prescriptions_by_generics);
	}

	setNodeText(row.cells[ROUTE], routeName);
	setNodeText(row.cells[REMARKS], remarks, 30);
	setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 30);
	setNodeText(row.cells[DISPLAY_ORDER], display_order);

	setHiddenValue(id, "cons_uom_id", cons_uom_id);
	setHiddenValue(id, "favourite_id", "_");
	setHiddenValue(id, "itemType", itemType);
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "special_instr" , spl_instruction);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "ispackage", ispackage);
	setHiddenValue(id, "route_id", routeId);
	setHiddenValue(id, "route_name", routeName);
	setHiddenValue(id, "display_order", display_order);
	setHiddenValue(id, "non_hosp_medicine", non_hosp_medicine);

	itemsAdded++;
	clearFields();
	setRowStyle(id);
	addItemDialog.align("tr", "tl");
	document.getElementById('d_itemName').focus();
	return id;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.favouritesForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearFieldsWhenChanged() {
	document.getElementById('d_itemName').value = '';
	clearItemDetails();
	toggleItemFormRow(true);
	initItemAutoComplete();
}

function clearFields() {
	document.getElementById('d_itemName').value = '';
	if (getItemType() == 'Medicine') {
		document.getElementById('d_non_hosp_medicine').checked = false;
	}
	initItemAutoComplete();
   	clearItemDetails();
   	toggleItemFormRow(true);
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');
	var favouriteId = getIndexedValue("favourite_id", i);

 	var flagImgs = row.cells[ITEM_TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (favouriteId.substring(0,1) == "_");
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
		cls = 'added';
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

	var isNew = getIndexedValue("favourite_id", id) == '_';

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
		var id = document.getElementById('editRowId').value;
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
	document.getElementById('editRowId').value = id;
	var itemType = getIndexedValue("itemType", id);
	var nonHospMedicine = getIndexedValue("non_hosp_medicine", id);
	document.getElementById('ed_itemTypeLabel').textContent = itemType + (itemType == 'Medicine' && nonHospMedicine == 'true' ? '[Non Hosp]' : '');
	document.getElementById('ed_itemNameLabel').textContent = getIndexedValue("item_name", id);
	document.getElementById('ed_itemName').value = getIndexedValue("item_name", id);
	document.getElementById('ed_item_id').value = getIndexedValue("item_id", id);
	document.getElementById('ed_itemType').value = itemType;
	document.getElementById('ed_display_order').value = getIndexedValue('display_order', id);
	document.getElementById('ed_non_hosp_medicine').value = nonHospMedicine;

	document.getElementById('ed_consumption_uom_label').textContent = getIndexedValue("cons_uom_id", id);
	document.getElementById('ed_cons_uom_id').value = getIndexedValue("cons_uom_id", id);

	var master = getIndexedValue("item_master", id);
	toggleItemFormRow(false);
	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById('ed_cons_uom_id').disabled = itemType == 'Medicine' && nonHospMedicine == 'false' && useGenerics;
		document.getElementById('ed_admin_strength').disabled = false;
		document.getElementById('ed_frequency').disabled = false;
		document.getElementById('ed_strength').disabled = false;
		document.getElementById('ed_duration').disabled = false;
		document.getElementById('ed_qty').disabled = false;
		document.getElementById('ed_remarks').disabled = false;

		initEditDosageAutoComplete();
		document.getElementById('ed_medicine_route').textContent = getIndexedValue("route_name", id);
		document.getElementById('ed_strength').value = getIndexedValue("strength", id);
		document.getElementById('ed_admin_strength').value = getIndexedValue("admin_strength", id);
		document.getElementById('ed_frequency').value = getIndexedValue("frequency", id);

		toggleDurationUnits(true, 'ed');
		document.getElementById('ed_duration').value = getIndexedValue("duration", id);
		var duration_units = getIndexedValue("duration_units", id);
		var els = document.getElementsByName("ed_duration_units");
		for (var k=0; k<els.length; k++) {
			if (els[k].value == duration_units) {
				els[k].checked = true;
				break;
			}
		}
		document.getElementById('ed_qty').value = getIndexedValue("medicine_quantity", id);
		if (itemType == 'Medicine') {
			document.getElementById('genericNameAnchor_editdialog').href =
				'javascript:showGenericInfo("", "", "editdialog", "' + getIndexedValue("generic_code", id) + '")';
			document.getElementById('genericNameAnchor_editdialog').style.display = 'block';
			document.getElementById('genericNameAnchor_editdialog').innerHTML = getIndexedValue("generic_name", id);
		}
		document.getElementById('ed_item_form_id').value = getIndexedValue("item_form_id", id);
		document.getElementById('ed_item_strength').value = getIndexedValue('item_strength', id);
		document.getElementById('ed_item_strength_units').value = getIndexedValue('item_strength_units', id);
		document.getElementById('ed_granular_units').value = getIndexedValue("granular_units", id);
	} else {
		document.getElementById('ed_cons_uom_id').disabled = true;
		toggleDurationUnits(false, 'ed');
		document.getElementById('ed_remarks').disabled = false;
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_frequency').disabled = true;
		document.getElementById('ed_strength').disabled = true;
		document.getElementById('ed_duration').disabled = true;
		document.getElementById('ed_qty').disabled = true;

		document.getElementById('ed_strength').value = '';
		document.getElementById('ed_admin_strength').value = '';
		document.getElementById('ed_frequency').value = '';
		document.getElementById('ed_duration').value = '';
		document.getElementById('ed_qty').value = '';

		//document.getElementById('edGenericNameRow').style.display = 'none';
		document.getElementById('genericNameAnchor_editdialog').innerHTML = '';
		document.getElementById('genericNameAnchor_editdialog').style.display = 'none';
		document.getElementById('genericNameAnchor_editdialog').href='';
	}
	document.getElementById('ed_ispackage').value = getIndexedValue("ispackage", id);
	document.getElementById('ed_remarks').value = getIndexedValue('item_remarks', id);
	document.getElementById('ed_special_instruction').value = getIndexedValue('special_instr', id);
	document.getElementById('ed_item_master').value = getIndexedValue('item_master', id);
	initEditInstructionAutoComplete('ed');
	document.getElementById('ed_remarks').focus();
	return false;
}

function editTableRow() {
	var id = document.getElementById('editRowId').value;
	var row = getChargeRow(id, 'itemsTable');

	var display_order = document.getElementById('ed_display_order').value;
   	if (display_order == '') {
   		alert('Please enter the display order');
   		document.getElementById('ed_display_order').focus();
   		return false;
   	}
   	var duration = document.getElementById('ed_duration').value;
   	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById('ed_duration').focus();
		return false;
	}
   	var duration_radio_els = document.getElementsByName('ed_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	if (!empty(duration) && empty(duration_units)) {
		alert("Please select the duration units.");
		return false;
	}
	var strength = document.getElementById('ed_strength').value;
	var granular_unit = document.getElementById('ed_granular_units').value;
	if (!empty(granular_unit) && granular_unit == 'Y') {
		if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	   		document.getElementById('ed_strength').focus();
	   		return false;
	   	}
	}
	var item_strength = document.getElementById('ed_item_strength').value;
	var item_strength_units = document.getElementById('ed_item_strength_units').value;
	item_strength_units = item_strength == '' ? '' : item_strength_units;
   	var strength_unit_name = document.getElementById('ed_item_strength_units').options[document.getElementById('ed_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;

	var itemType = document.getElementById('ed_itemType').value;
	var cons_uom_id = document.getElementById('ed_cons_uom_id').value;
	if (cons_uom_id === '' && itemType == 'Medicine' && !useGenerics) {
   		alert('Please select the granular unit');
   		return false;
	}

   	var itemName = document.getElementById('ed_itemName').value;
   	var itemId = document.getElementById('ed_item_id').value;
   	var adminStrength = document.getElementById('ed_admin_strength').value;
   	var frequency = document.getElementById('ed_frequency').value;
   	var qty = document.getElementById('ed_qty').value;
   	var remarks = document.getElementById('ed_remarks').value;
	var spl_instruction = document.getElementById('ed_special_instruction').value;
   	var master = document.getElementById('ed_item_master').value;
   	var ispackage = document.getElementById('ed_ispackage').value;
   	var item_form_id = document.getElementById('ed_item_form_id').value;
   	var item_form_name = document.getElementById('ed_item_form_id').options[document.getElementById('ed_item_form_id').selectedIndex].text;
   	var non_hosp_medicine = document.getElementById('ed_non_hosp_medicine').value;

   	setNodeText(row.cells[ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine == 'true' ? '[Non Hosp]' : ''));
   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {

		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
   		var details = "";
   		if (frequency != '' || duration != '')
   			details = frequency + " / " + duration + duration_units;
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
		setHiddenValue(id, "granular_units", granular_unit);
		setHiddenValue(id, "item_strength", item_strength);
		setHiddenValue(id, "item_strength_units", item_strength_units);
	}

	setNodeText(row.cells[REMARKS], remarks, 30);
	setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 30);
	setNodeText(row.cells[DISPLAY_ORDER], display_order);

	setHiddenValue(id, "itemType", itemType);
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "special_instr", spl_instruction);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "ispackage", ispackage);
	setHiddenValue(id, "cons_uom_id", cons_uom_id);
	setHiddenValue(id, "display_order", display_order);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);

	editItemDialog.cancel();
	return id;
}
var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function openPrevious() {
	var id = document.getElementById('editRowId').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		editTableRow();
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
	var id = document.getElementById('editRowId').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		editTableRow();
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('itemsTable').rows.length-2) {
		showEditItemDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
	}
}
var dosageAutoComplete = null;
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

var instructionAutoComplete = null;
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
			var dosage = document.getElementById(idPrefix+'_strength').value;
			dosage = dosage == "" || isNaN(dosage) ? 1 : dosage;
			var duration_units = 'D';
			for (var j=0; j<duration_units_els.length; j++) {
				if (duration_units_els[j].checked) {
					duration_units = duration_units_els[j].value;
					break;
				}
			}
			if (duration_units == 'D')
				qty = Math.ceil(duration * perDayQty * dosage);
			else if (duration_units == 'W')
				qty = Math.ceil((duration * 7) * perDayQty * dosage);
			else if (duration_units == 'M')
				qty = Math.ceil((duration * 30) * perDayQty * dosage);

		}
		document.getElementById(idPrefix + '_qty').value = qty;
	}
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

function validateMedBlockExceptQty(calledOn, idPrefix) {
	var itemType = (idPrefix == 'd' ? getItemType() : document.getElementById(idPrefix + "_itemType").value);
	if (itemType != 'Medicine' && itemType != 'NonHospital') return;

	var medicineName = document.getElementById(idPrefix + '_itemName').value;
	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /[1-9]/;
	var regExp = new RegExp(validNumber);

	if (medicineName == '') {
		alert("Please enter the Medicine Name");
		return false;
	}
	if (duration != '' && !regExp.test(duration)) {
		alert("Invalid Duration: Duration should be greater than Zero.");
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

function validateOnSave() {
	if (max_centers>1 && centerId == 0) {
		alert("Manage Consultation Favourites allowed only for center users");
		return false;
	}
	var itemName = document.getElementsByName('item_name');
	var strength_els = document.getElementsByName("strength");
	var granular_units_els = document.getElementsByName("granular_units");
	var medicines = new Array();
	for (var i=0; i<strength_els.length; i++) {
		if (granular_units_els[i].value == 'Y' && strength_els[i].value != '' && (!isDecimal(strength_els[i].value, 2) || strength_els[i].value == 0)) {
			medicines.push(itemName[i].value);
		}
	}
	if (medicines.length > 0) {
		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals). \n" +
				"Please correct the dosage information for following medicines. \n\n * " + medicines.join("\n * "));
		return false;
	}
	document.favouritesForm.submit();
	return true;
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
	return getElementPaise(getIndexedFormElement(document.favouritesForm, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.favouritesForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.favouritesForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}


