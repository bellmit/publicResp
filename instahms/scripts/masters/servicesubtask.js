/**
 * 
 */
function initSubtasks() {
	initSubtaskDialog();
	initEditSubtaskDialog();
}

function subtaskEntered() {
	return document.getElementById('subtaskDetailsTable').rows.length > 2;
}

var addSubtaskDialog = null;
function initSubtaskDialog() {
	var dialogSubtaskDiv = document.getElementById("addSubtaskDialog");
	if (dialogSubtaskDiv == undefined) return;

	dialogSubtaskDiv.style.display = 'block';
	addSubtaskDialog = new YAHOO.widget.Dialog("addSubtaskDialog",
			{	width:"600px",
				context : ["addSubtaskDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('subtaskDetails_add_btn', 'click', addToSubtaskTable, addSubtaskDialog, true);
	YAHOO.util.Event.addListener('subtaskDetails_cancel_btn', 'click', handleAddSubtaskCancel, addSubtaskDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddSubtaskCancel,
	                                                scope:addSubtaskDialog,
	                                                correctScope:true } );
	addSubtaskDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addSubtaskDialog.render();
}

function handleAddSubtaskCancel() {
		parentSubtaskDialog = null;
		this.cancel();
}

var parentSubtaskDialog = null;
function showAddSubtaskDialog(obj) {
	var row = getSubtaskThisRow(obj);
	clearSubtaskFields();
	button = document.getElementById("btnAddSubtask");
	addSubtaskDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addSubtaskDialog.show();
	document.getElementById('d_desc_short').value='';
	document.getElementById('d_desc_short').focus();
	parentSubtaskDialog = addSubtaskDialog;
	return false;
}


var subtaskColIndex  = 0;
var SUBTASK_DESC_SHORT = subtaskColIndex++, SUBTASK_DESC_LONG = subtaskColIndex++,
SUBTASK_STATUS = subtaskColIndex++,SUBTASK_DISPLAY_ORDER = subtaskColIndex++,
SUBTASK_TRASH_COL = subtaskColIndex++, SUBTASK_EDIT_COL = subtaskColIndex++;
var subtasksAdded = 0;
function addToSubtaskTable() {
	if(!checkDisplayOrdervalidation('add')) return false;
	var subtaskId = document.getElementById('d_sub_task_id').value;
	var shortdesc = (document.getElementById('d_desc_short').value = trim(document.getElementById('d_desc_short').value));	
	if(trim(shortdesc) == '') {
		alert("Name field is mandatory");		
		document.getElementById('d_desc_short').focus();
			return false;
	}
	var longdesc = document.getElementById('d_desc_long').value;
	var subtaskstatus = document.getElementById('d_subtask_status').options[document.getElementById('d_subtask_status').selectedIndex].value;
	var displayOrder = document.getElementById('d_display_order').value;

	var id = getSubtaskNumCharges('subtaskDetailsTable');
   	var table = document.getElementById("subtaskDetailsTable");
	var templateRow = table.rows[getSubtaskTemplateRow('subtaskDetailsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	
	setNodeText(row.cells[SUBTASK_DESC_SHORT], shortdesc,30);
	setNodeText(row.cells[SUBTASK_DESC_LONG], longdesc, 30);
	setNodeText(row.cells[SUBTASK_STATUS], subtaskstatus == 'A' ? 'Active' : 'Inactive');
	setNodeText(row.cells[SUBTASK_DISPLAY_ORDER], displayOrder!='' ? displayOrder:'0');
	
	setSubtaskHiddenValue(id, "sub_task_id", '_');
   	setSubtaskHiddenValue(id, "desc_short", shortdesc);
   	setSubtaskHiddenValue(id, "desc_long", longdesc);
   	setSubtaskHiddenValue(id, "subtask_status", subtaskstatus);
   	setSubtaskHiddenValue(id, "display_order", displayOrder);
   	
   	subtasksAdded++;
	clearSubtaskFields();
	setSubtaskRowStyle(id);
	addSubtaskDialog.align("tr", "tl");
	document.getElementById('d_desc_short').focus();
	return id;
}

function setSubtaskHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.inputForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearSubtaskFields() {
	document.getElementById('d_desc_short').value = '';
	document.getElementById('d_desc_long').value = '';
	document.getElementById('d_subtask_status').value = 'A';
	document.getElementById('d_display_order').value = '';
}

function setSubtaskRowStyle(i) {
	var row = getSubtaskChargeRow(i, 'subtaskDetailsTable');
	var subtaskId = getSubtaskIndexedValue("sub_task_id", i);

	var flagImgs = row.cells[SUBTASK_DESC_SHORT].getElementsByTagName("img");
	var added = (subtaskId.substring(0,1) == "_");
	var cancelled = getSubtaskIndexedValue("service_subtask_deleted", i) == 'true';
	var edited = getSubtaskIndexedValue("service_subtask_edited", i) == 'true';
	var shortdesc = getSubtaskIndexedValue("desc_short", i);

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

}

function cancelSubtask(obj) {

	var row = getSubtaskThisRow(obj);
	var id = getSubtaskRowChargeIndex(row);
	var oldDeleted =  getSubtaskIndexedValue("service_subtask_deleted", id);

	var isNew = getSubtaskIndexedValue("sub_task_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		subtasksAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setSubtaskIndexedValue("service_subtask_deleted", id, newDeleted);
		setSubtaskIndexedValue("service_subtask_edited", id, "true");
		setSubtaskRowStyle(id);
	}
	return false;
}

function initEditSubtaskDialog() {
	var dialogSubtaskDiv = document.getElementById("editSubtaskDialog");
	dialogSubtaskDiv.style.display = 'block';
	editSubtaskDialog = new YAHOO.widget.Dialog("editSubtaskDialog",{
			width:"600px",
			text: "Edit service Subtask",
			context :["subtaskDetailsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditSubtaskCancel,
	                                                scope:editSubtaskDialog,
	                                                correctScope:true } );
	editSubtaskDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editSubtaskDialog.cancelEvent.subscribe(handleEditSubtaskCancel);
	YAHOO.util.Event.addListener('edit_SubtaskDetails_Ok', 'click', editSubtaskTableRow, editSubtaskDialog, true);
	YAHOO.util.Event.addListener('edit_SubtaskDetails_Cancel', 'click', handleEditSubtaskCancel, editSubtaskDialog, true);
	YAHOO.util.Event.addListener('edit_SubtaskDetails_Previous', 'click', openSubtaskPrevious, editSubtaskDialog, true);
	YAHOO.util.Event.addListener('edit_SubtaskDetails_Next', 'click', openSubtaskNext, editSubtaskDialog, true);
	editSubtaskDialog.render();
}

function handleEditSubtaskCancel() {
		parentSubtaskDialog = null;
		var id = document.inputForm.editSubtaskRowId.value;
		var row = getSubtaskChargeRow(id, "subtaskDetailsTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldSubtaskEdited = false;
		this.hide();
}

function checkDisplayOrdervalidation(action) {
	var prefix = null;
	if (action == 'add'){
		prefix = 'd';
	} else if (action == 'edit'){
		prefix = 'ed';
	}
	
	displayOrder = document.getElementById(prefix+"_display_order").value;
	if (displayOrder != '') {
		if (!(isInteger(displayOrder))) {
		alert("Display Order should be numeric field");
			return false;
		} 
	}
	
    return true;
}

function showEditSubtaskDialog(obj) {
	parentSubtaskDialog = editSubtaskDialog;
	var row = getSubtaskThisRow(obj);
	var id = getSubtaskRowChargeIndex(row);
	editSubtaskDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editSubtaskDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.inputForm.editSubtaskRowId.value = id;

	document.getElementById('ed_desc_short').value = getSubtaskIndexedValue("desc_short", id);
	document.getElementById('ed_sub_task_id').value = getSubtaskIndexedValue("sub_task_id", id);
	document.getElementById('ed_desc_long').value = getSubtaskIndexedValue("desc_long", id);
	document.getElementById('ed_subtask_status').value = getSubtaskIndexedValue("subtask_status", id);
	document.getElementById('ed_display_order').value = getSubtaskIndexedValue("display_order", id);
	
	document.getElementById('ed_desc_short').focus();
	return false;
}

function editSubtaskTableRow() {
	if(!checkDisplayOrdervalidation('edit')) return false;
	var subtaskId = document.getElementById('ed_sub_task_id').value;
	var shortdesc = document.getElementById('ed_desc_short').value;
	var shortdesc = (document.getElementById('ed_desc_short').value = trim(document.getElementById('ed_desc_short').value));	
	if(trim(shortdesc) == '') {
		alert("Name field is mandatory");		
		document.getElementById('ed_desc_short').focus();
			return false;
	}
	var longdesc = document.getElementById('ed_desc_long').value;
	var status = document.getElementById('ed_subtask_status').options[document.getElementById('ed_subtask_status').selectedIndex].value;
	var displayOrder = document.getElementById('ed_display_order').value;
	var id = document.inputForm.editSubtaskRowId.value;
   	var row = getSubtaskChargeRow(id, 'subtaskDetailsTable');
	
   	var cell = null;
   	
	setNodeText(row.cells[SUBTASK_DESC_SHORT], shortdesc,30);
	setNodeText(row.cells[SUBTASK_DESC_LONG], longdesc, 30);
	setNodeText(row.cells[SUBTASK_STATUS], status == 'A' ? 'Active' : 'Inactive');
	setNodeText(row.cells[SUBTASK_DISPLAY_ORDER], displayOrder!='' ? displayOrder:'0');
	
	setSubtaskHiddenValue(id, "sub_task_id", subtaskId);
   	setSubtaskHiddenValue(id, "desc_short", shortdesc);
   	setSubtaskHiddenValue(id, "desc_long", longdesc);
   	setSubtaskHiddenValue(id, "subtask_status", status);
   	setSubtaskHiddenValue(id, "display_order", displayOrder);
   	
   	YAHOO.util.Dom.removeClass(row, 'editing');

	setSubtaskIndexedValue("service_subtask_edited", id, 'true');
	setSubtaskRowStyle(id);

	editSubtaskDialog.cancel();
	return true;
}

var fieldSubtaskEdited = false;
function setsubtasksEdited() {
	fieldSubtaskEdited = true;
}

function openSubtaskPrevious() {
	var id = document.inputForm.editSubtaskRowId.value;
	id = parseInt(id);
	var row = getSubtaskChargeRow(id, 'subtaskDetailsTable');

	if (fieldSubtaskEdited) {
		if (!editSubtaskTableRow()) return false;
		fieldSubtaskEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditSubtaskDialog(document.getElementsByName('subtaskEditAnchor')[parseInt(id)-1]);
	}
}

function openSubtaskNext() {
	var id = document.inputForm.editSubtaskRowId.value;
	id = parseInt(id);
	var row = getSubtaskChargeRow(id, 'subtaskDetailsTable');

	if (fieldSubtaskEdited) {
		if (!editSubtaskTableRow()) return false;
		fieldSubtaskEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('subtaskDetailsTable').rows.length-2) {
		showEditSubtaskDialog(document.getElementsByName('subtaskEditAnchor')[parseInt(id)+1]);
	}
}

function getSubtaskNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstSubtaskRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getSubtaskTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getSubtaskNumCharges(tableId) + 1;
}

function getSubtaskChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstSubtaskRow()];
}

function getSubtaskRowChargeIndex(row) {
	return row.rowIndex - getFirstSubtaskRow();
}

function getSubtaskThisRow(node) {
	return findAncestor(node, "TR");
}

function getSubtaskIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.inputForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setSubtaskIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.inputForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

