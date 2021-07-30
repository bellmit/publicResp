var itemsAdded = 0;

function init() {
	initOptionDialog();
	initEditOptionDialog();
	var field_type = document.sectionfield.field_type.value;
	if (field_type == 'text' || field_type == 'wide text') {
		document.sectionfield.field_phrase_category_id.disabled = false;
		document.getElementById('regexp_field_id').disabled = false;
	} else {
		document.sectionfield.field_phrase_category_id.disabled = true;
		document.getElementById('regexp_field_id').disabled = true;
	}
}

function saveFieldValues() {
	var field_type = document.sectionfield.field_type.value;
	var image = document.sectionfield.file_content.value;
	var method = document.sectionfield._method.value;
	var hidden_field_type = document.sectionfield.hidden_field_type.value;
	if (method == 'update') {
		if (field_type == '') {
			alert("Field Type is mandatory.");
			return false;
		} else if (field_type == hidden_field_type) {
			// field type not changed
		} else if ((hidden_field_type == 'text' || hidden_field_type == 'wide text') &&
				(field_type == 'text' || field_type == 'wide text')) {
			// changing field type allowed only from text to wide text or wide text to text.
		} else {
			alert("Can't change field type '"+hidden_field_type+"' to '"+field_type+"'");
			document.sectionfield.field_type.focus();
			return false;

		}
	}
	if (field_type == 'image') {
		var is_mandatory = 'false';
		var elMandatory = document.sectionfield.is_mandatory;
		for (var i=0;i<elMandatory.length; i++) {
			if (elMandatory[i].checked) {
				is_mandatory = elMandatory[i].value;
			}
		}
		if (is_mandatory == 'true') {
			alert("Image Fields can't be marked as mandatory.");
			return false;
		}
		if (method == 'create' && image == '') {
			alert("Image is mandatory");
			document.sectionfield.file_content.focus();
			return false;
		}
		var markers = document.getElementById('selected_markers');
		var selected = false;
		for (var i=0; i<markers.options.length; i++) {
			markers.options[i].selected = true;
			selected = true;
		}
		if (!selected) {
			alert("Please select image markers");
			document.getElementById('avlbl_markers').focus();
			return false;
		}
	} else if (field_type == 'dropdown' || field_type == 'checkbox') {
		var option_value = document.getElementsByName("option_value");
		var atleastOneValueExists = false;
		for (var i=0; i<option_value.length; i++) {
			if (option_value[i].value != '') {
				atleastOneValueExists = true;
				break;
			}
		}
		if (!atleastOneValueExists) {
			alert('Please enter atleast one Option Value.');
			return false;
		}
	}
	if(document.getElementById('allow_others').checked){
		document.getElementById('allow_others').value='Y';
		document.getElementById('allow_others').checked = true;
	}else{
		document.getElementById('allow_others').value='N';
		document.getElementById('allow_others').checked = true;
	}
	
	if(document.getElementById('allow_normal').checked){
		document.getElementById('allow_normal').value='Y';
		document.getElementById('allow_normal').checked = true;
	}else{
		document.getElementById('allow_normal').value='N';
		document.getElementById('allow_normal').checked = true;
	}
	
	document.sectionfield.submit();
	return true;
}

function onFieldTypeChange() {
	var field_type = document.sectionfield.field_type.value;

	if (field_type == 'text' || field_type == 'wide text') {
		document.getElementById('field_phrase_category_id').disabled = false;
		document.getElementById('regexp_field_id').disabled = false;
	} else {
		document.getElementById('field_phrase_category_id').disabled = true;
		document.getElementById('regexp_field_id').disabled = true;
	}
}

function initOptionDialog() {
	var dialogDiv = document.getElementById("addOptionDialog");
	dialogDiv.style.display = 'block';
	addOptionDialog = new YAHOO.widget.Dialog("addOptionDialog",
			{	width:"300px",
				context : ["addOptionDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add', 'click', addToTable, addOptionDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleAddOptionCancel, addOptionDialog, true);
	var enterKeyListener = new YAHOO.util.KeyListener("addOptionDialogFields", { keys:13 },
				{ fn:addToTable, scope:addOptionDialog, correctScope:true } );
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddOptionCancel,
	                                                scope:addOptionDialog,
	                                                correctScope:true } );
	addOptionDialog.cfg.setProperty("keylisteners", [escKeyListener, enterKeyListener]);
	addOptionDialog.render();
}

function handleAddOptionCancel() {
	this.cancel();
}

function showAddOptionDialog(obj) {
	var row = getThisRow(obj);

	addOptionDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addOptionDialog.show();
	clearFields();
	document.getElementById('d_phrase_category_id').disabled = (document.sectionfield.field_type.value != 'checkbox');
	document.getElementById('d_regexp_field_id').disabled = (document.sectionfield.field_type.value != 'checkbox');
	document.getElementById('d_option_value').focus();
	return false;
}

function checkForDuplicates(option_value, id) {
	var valueArray = document.getElementsByName("option_value");
	for (var i=0; i<valueArray.length; i++) {
		if (id == i) continue; // ignore the current selected row while editing.
		if (option_value.trim().toLowerCase() == valueArray[i].value.trim().toLowerCase()) {
		 	return true;
		}
	}
	return false;
}
var OPTION_VALUE=0, VALUE_CODE=1, STATUS=2, DISPLAY_ORDER = 3, OPTION_PHRASE_CAT=4, OPTION_REGEXP=5, TRASHCOL=6;
function addToTable() {
	var option_value = document.getElementById('d_option_value').value;
	if (option_value == '') {
   		alert('Please enter the Option Value');
   		document.getElementById('d_option_value').focus();
   		return false;
   	}
   	if (checkForDuplicates(option_value, -1)) {
   		alert("Option Value '"+option_value+"' Already exists");
   		document.getElementById('d_option_value').focus();
   		return false;
   	}
   	var status = document.getElementById('d_option_status').value;
   	if (status == '') {
   		alert('Please select the status');
   		document.getElementById('d_option_status').focus();
   		return false;
   	}
   	var display_order = document.getElementById('d_display_order').value;
   	if (display_order == '') {
   		alert("Please enter the display order");
   		document.getElementById('d_display_order').focus();
   		return false;
   	}

	var id = getNumCharges('itemsTable');
   	var table = document.getElementById("itemsTable");
   	var templateRow = table.rows[getTemplateRow('itemsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

	var option_phrase_category_id = document.getElementById('d_phrase_category_id').value;
	var option_phrase_category = document.getElementById('d_phrase_category_id').options[document.getElementById('d_phrase_category_id').selectedIndex].text;
	option_phrase_category = empty(option_phrase_category_id) ? '' : option_phrase_category;

	var option_regexp_field_id = document.getElementById('d_regexp_field_id').value;
	var option_regexp_field = document.getElementById('d_regexp_field_id').options[document.getElementById('d_regexp_field_id').selectedIndex].text;
	option_regexp_field = empty(option_regexp_field_id) ? '' : option_regexp_field;

	setNodeText(row.cells[OPTION_VALUE], option_value);
   	setNodeText(row.cells[VALUE_CODE], document.getElementById('d_value_code').value);
   	setNodeText(row.cells[STATUS], status);
	setNodeText(row.cells[DISPLAY_ORDER], display_order);
	setNodeText(row.cells[OPTION_PHRASE_CAT], option_phrase_category);
	setNodeText(row.cells[OPTION_REGEXP],  option_regexp_field);

	setHiddenValue(id, "option_id", "_");
	setHiddenValue(id, "option_value", option_value);
	setHiddenValue(id, "value_code", document.getElementById('d_value_code').value);
	setHiddenValue(id, "option_status", status);
	setHiddenValue(id, "option_display_order", display_order);
	setHiddenValue(id, "option_phrase_category", option_phrase_category_id);
	setHiddenValue(id, "option_regexp_field", option_regexp_field_id);

	itemsAdded++;
	clearFields();
	setRowStyle(id);
	addOptionDialog.align("tr", "tl");
	document.getElementById('d_option_value').focus();
	return id;
}

function clearFields() {
	document.getElementById('d_option_value').value = '';
	document.getElementById('d_value_code').value = '';
	document.getElementById('d_option_status').selectedIndex = 1;
	document.getElementById('d_display_order').value = '';
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');
	var optionId = getIndexedValue("option_id", i);

	var added = (optionId == "_");
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
	} else if (edited) {
		cls = 'edited';
	} else {
		cls = '';
	}
	row.className = cls;

}

function cancelOption(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var isNew = getIndexedValue("option_id", id) == '_';

	if (isNew) {
		// just delete the row, don't mark it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	}
	return false;
}

function initEditOptionDialog() {
	var dialogDiv = document.getElementById("editOptionDialog");
	dialogDiv.style.display = 'block';
	editOptionDialog = new YAHOO.widget.Dialog("editOptionDialog",{
			width:"300px",
			text: "Edit Option",
			context :["itemsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditOptionCancel,
	                                                scope:editOptionDialog,
	                                                correctScope:true } );
	editOptionDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editOptionDialog.cancelEvent.subscribe(handleEditOptionCancel);
	YAHOO.util.Event.addListener('editOk', 'click', editTableRow, editOptionDialog, true);
	YAHOO.util.Event.addListener('editCancel', 'click', handleEditOptionCancel, editOptionDialog, true);
	YAHOO.util.Event.addListener('editPrevious', 'click', openPrevious, editOptionDialog, true);
	YAHOO.util.Event.addListener('editNext', 'click', openNext, editOptionDialog, true);
	editOptionDialog.render();
}

function handleEditOptionCancel() {
	var id = document.sectionfield.editRowId.value;
	var row = getChargeRow(id, "itemsTable");
	YAHOO.util.Dom.removeClass(row, 'editing');
	fieldEdited = false;
	this.hide();
}

function showEditOptionDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editOptionDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editOptionDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.sectionfield.editRowId.value = id;

	document.getElementById('ed_option_value').value = getIndexedValue("option_value", id);
	document.getElementById('ed_value_code').value = getIndexedValue("value_code", id);
	document.getElementById('ed_option_status').value = getIndexedValue("option_status", id);
	document.getElementById('ed_display_order').value = getIndexedValue("option_display_order", id);
	document.getElementById('ed_phrase_category_id').value = getIndexedValue('option_phrase_category', id);
	document.getElementById('ed_regexp_field_id').value = getIndexedValue('option_regexp_field', id);
	document.getElementById('ed_regexp_field_id').disabled = (document.sectionfield.field_type.value != 'checkbox');
	document.getElementById('ed_phrase_category_id').disabled = (document.sectionfield.field_type.value != 'checkbox');

	document.getElementById('ed_option_value').focus();
	return false;
}

function editTableRow() {
	var id = document.sectionfield.editRowId.value;
	var row = getChargeRow(id, 'itemsTable');

	var option_value = document.getElementById('ed_option_value').value;
	if (option_value == '') {
   		alert('Please enter the Option Value');
   		document.getElementById('ed_option_value').focus();
   		return false;
   	}
   	if (checkForDuplicates(option_value, id)) {
   		alert("Option Value '"+option_value+"' Already exists");
   		document.getElementById('ed_option_value').focus();
   		return false;
   	}
   	var option_status = document.getElementById('ed_option_status').value;
   	if (option_status == '') {
   		alert('Please select the status');
   		document.getElementById('ed_option_status').focus();
   		return false;
   	}
   	var display_order = document.getElementById('ed_display_order').value;
   	if (display_order == '') {
   		alert("Please enter the display order");
   		document.getElementById('ed_display_order').focus();
   		return false;
   	}
	var option_phrase_category_id = document.getElementById('ed_phrase_category_id').value;
	var option_phrase_category = document.getElementById('ed_phrase_category_id').options[document.getElementById('ed_phrase_category_id').selectedIndex].text;
	option_phrase_category = empty(option_phrase_category_id) ? '' : option_phrase_category;

	var option_regexp_field_id = document.getElementById('ed_regexp_field_id').value;
    var option_regexp_field = document.getElementById('ed_regexp_field_id').options[document.getElementById('ed_regexp_field_id').selectedIndex].text;
    option_regexp_field = empty(option_regexp_field_id) ? '' : option_regexp_field;

   	setNodeText(row.cells[OPTION_VALUE], option_value);
   	setNodeText(row.cells[VALUE_CODE], document.getElementById('ed_value_code').value);
   	setNodeText(row.cells[STATUS], option_status);
	setNodeText(row.cells[DISPLAY_ORDER], display_order);
	setNodeText(row.cells[OPTION_PHRASE_CAT], option_phrase_category);
	setNodeText(row.cells[OPTION_REGEXP], option_regexp_field);

   	setHiddenValue(id, "option_value", option_value);
   	setHiddenValue(id, "value_code", document.getElementById('ed_value_code').value);
	setHiddenValue(id, "option_status", option_status);
	setHiddenValue(id, "option_display_order", display_order);
	setHiddenValue(id, "option_phrase_category", option_phrase_category_id);
	setHiddenValue(id, "option_regexp_field", option_regexp_field_id);
	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);

	editOptionDialog.cancel();
	return id;
}
var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function openPrevious() {
	var id = document.sectionfield.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		if (!editTableRow()) {
			return false;
		}
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditOptionDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var id = document.sectionfield.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		if (!editTableRow()) {
			return false;
		}
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('itemsTable').rows.length-2) {
		showEditOptionDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
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
	return getElementPaise(getIndexedFormElement(document.sectionfield, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.sectionfield, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.sectionfield, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.sectionfield, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


