/**
 * 
 */
function initHl7Mapping() {
	initHl7MappingDialog();
	initEditHl7mappingDialog();
}

function Hl7MappingEntered() {
	return document.getElementById('hl7mappingtable').rows.length > 2;
}

var addHl7mappingDialog = null;
function initHl7MappingDialog() {
	var dialogHl7mappingDiv = document.getElementById("addHl7mappingDialog");
	if (dialogHl7mappingDiv == undefined) return;

	dialogHl7mappingDiv.style.display = 'block';
	addHl7mappingDialog = new YAHOO.widget.Dialog("addHl7mappingDialog",
			{	width:"600px",
				context : ["addHl7mappingDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('hl7mapping_add_btn', 'click', addToHl7mappingTable, addHl7mappingDialog, true);
	YAHOO.util.Event.addListener('hl7mapping_cancel_btn', 'click', handleAddHl7mappingCancel, addHl7mappingDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddHl7mappingCancel,
	                                                scope:addHl7mappingDialog,
	                                                correctScope:true } );
	addHl7mappingDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addHl7mappingDialog.render();
}

function handleAddHl7mappingCancel() {
		parentHl7mappingDialog = null;
		this.cancel();
}

var parentHl7mappingDialog = null;
function showAddHl7mappingDialog(obj) {
	var rowObject = getThisRow(obj);
	button = document.getElementById("btnAddHl7mapping");
	addHl7mappingDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addHl7mappingDialog.show();
	parentSubtaskDialog = addHl7mappingDialog;
	return false;
}

var hl7mappingColIndex  = 0;
var HL7_MAPPING_MESSAGE_TYPE = hl7mappingColIndex++,  HL7_MAPPING_INTERFACE_NAME = hl7mappingColIndex++,  
HL7_MAPPING_TRASH_COL = hl7mappingColIndex++, HL7_MAPPING_EDIT_COL = hl7mappingColIndex++;
var hl7recordsAdded = 0;
function addToHl7mappingTable() {
	var messageType = document.getElementById('d_item_type').options[document.getElementById('d_item_type').selectedIndex].text;
	var messageValue = document.getElementById('d_item_type').options[document.getElementById('d_item_type').selectedIndex].value;
	var intefaceName = document.getElementById('d_interface_name').options[document.getElementById('d_interface_name').selectedIndex].text;
	var interfaceID = document.getElementById('d_interface_name').options[document.getElementById('d_interface_name').selectedIndex].value;
	if(document.getElementById('d_interface_name').value == '') {
		alert("Please select Interface Name.");
		return false;
	}
	if(document.getElementById('d_item_type').value == '') {
		alert("Please select Message Type.");
		return false;
	}
	var id = getHl7MappingNumCharges('hl7mappingtable');
   	var table = document.getElementById("hl7mappingtable");
	var messageTypes = document.getElementsByName("item_type");
	var intefaceNames = document.getElementsByName("interface_name");
	for (var i=0;i<intefaceNames.length;i++) {
		if ((i!=id) && (intefaceNames[i].value == intefaceName) &&
				(messageTypes[i].value == messageValue)) {
			alert("Duplicate Entry");
				return false;
		}
	}
   	//row.id = "itemRow" + id;
	var templateRow = table.rows[getHl7MappingTemplateRow('hl7mappingtable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	var cell = null;
   	
	setNodeText(row.cells[HL7_MAPPING_MESSAGE_TYPE], messageType, 20);
	setNodeText(row.cells[HL7_MAPPING_INTERFACE_NAME], intefaceName, 30);
	
	getElementByName(row, 'hl7_mapping_test_id').value = "_";
	getElementByName(row, 'interface_name').value = intefaceName;
	getElementByName(row, 'hl7_lab_interface_id').value = interfaceID;
	getElementByName(row, 'item_type').value = messageValue;
   	
	hl7recordsAdded++;
	clearHl7mappingFields();
	addHl7mappingDialog.align("tr", "tl");
	setHl7mappingRowStyle(id);
	return id;
}

function initEditHl7mappingDialog() {
	var dialogHl7mappingDiv = document.getElementById("editHl7mappingDialog");
	dialogHl7mappingDiv.style.display = 'block';
	editHl7mappingDialog = new YAHOO.widget.Dialog("editHl7mappingDialog",{
			width:"600px",
			text: "Edit Hl7 Interface",
			context :["hl7mappingDetailsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditHl7mapingCancel,
	                                                scope:editHl7mappingDialog,
	                                                correctScope:true } );
	editHl7mappingDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editHl7mappingDialog.cancelEvent.subscribe(handleEditHl7mapingCancel);
	YAHOO.util.Event.addListener('edit_Hl7mappingDetails_Ok', 'click', editHl7mappingTableRow, editHl7mappingDialog, true);
	YAHOO.util.Event.addListener('edit_Hl7mappingDetails_Cancel', 'click', handleEditHl7mapingCancel, editHl7mappingDialog, true);
	YAHOO.util.Event.addListener('edit_Hl7mappingDetails_Previous', 'click', openHl7mappingPrevious, editHl7mappingDialog, true);
	YAHOO.util.Event.addListener('edit_Hl7mappingDetails_Next', 'click', openHl7mappingNext, editHl7mappingDialog, true);
	editHl7mappingDialog.render();
}

function handleEditHl7mapingCancel() {
		var id = document.addtest.editHl7MappingRowId.value;
		var row = getHl7MappingChargeRow(id, "hl7mappingtable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldHl7mappingEdited = false;
		this.hide();
}


function showEditHl7MappingDialog(obj) {
	var row = getThisRow(obj);
	var id = getHl7MappingRowChargeIndex(row);
	editHl7mappingDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editHl7mappingDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.addtest.editHl7MappingRowId.value = id;

	document.getElementById('ed_interface_name').value = getHl7MappingIndexedValue("hl7_lab_interface_id", id);
	document.getElementById('ed_item_type').value = getHl7MappingIndexedValue("item_type", id);
	return false;
}

function editHl7mappingTableRow() {
	var messageType =  document.getElementById('ed_item_type').options[document.getElementById('ed_item_type').selectedIndex].text;
	var messageValue = document.getElementById('ed_item_type').options[document.getElementById('ed_item_type').selectedIndex].value;
	var intefaceName = document.getElementById('ed_interface_name').options[document.getElementById('ed_interface_name').selectedIndex].text;
	var intefaceID = document.getElementById('ed_interface_name').options[document.getElementById('ed_interface_name').selectedIndex].value;
	if(document.getElementById('ed_interface_name').value == '') {
		alert("Please select Interface Name.");
		return false;
	}
	if(document.getElementById('ed_item_type').value == '') {
		alert("Please select Message Type.");
		return false;
	}
	var id = document.addtest.editHl7MappingRowId.value;
   	var row = getHl7MappingChargeRow(id, 'hl7mappingtable');
   	var messageTypes = document.getElementsByName("item_type");
	var intefaceNames = document.getElementsByName("interface_name");
	for (var i=0;i<intefaceNames.length;i++) {
		if ((i!=id) && (intefaceNames[i].value == intefaceName) &&
				(messageTypes[i].value == messageValue)) {
			alert("Duplicate Entry");
				return false;
		}
	}
   	var cell = null;
   	
   	setNodeText(row.cells[HL7_MAPPING_MESSAGE_TYPE], messageType, 20);
   	setNodeText(row.cells[HL7_MAPPING_INTERFACE_NAME], intefaceName, 30);
	
	getElementByName(row, 'item_type').value = messageValue;
	getElementByName(row, 'interface_name').value = intefaceName;
	getElementByName(row, 'hl7_lab_interface_id').value = intefaceID;
   	
   	YAHOO.util.Dom.removeClass(row, 'editing');
   	setHl7MappingIndexedValue("hl7_mapping_edited", id, 'true');
   	editHl7mappingDialog.cancel();
	return true;
}

var fieldHl7mappingEdited = false;
function hl7mappingsEdited() {
	fieldHl7mappingEdited = true;
}

function cancelHl7MappingDetails(obj) {
	var row = getThisRow(obj);
	var id = getHl7MappingRowChargeIndex(row);
	var oldDeleted =  getHl7MappingIndexedValue("hl7_mapping_deleted", id);

	var isNew = getHl7MappingIndexedValue("hl7_mapping_test_id", id) == "_";
	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		hl7recordsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setHl7MappingIndexedValue("hl7_mapping_deleted", id, newDeleted);
		setHl7MappingIndexedValue("hl7_mapping_edited", id, "true");
		setHl7mappingRowStyle(id);
	}
	return false;
}

function setHl7mappingRowStyle(i) {
	var row = getHl7MappingChargeRow(i, 'hl7mappingtable');
	var hl7mapping_id = getHl7MappingIndexedValue("hl7_mapping_test_id", i);
	var trashImgs = row.cells[HL7_MAPPING_TRASH_COL].getElementsByTagName("img");
	var flagImgs = row.cells[HL7_MAPPING_MESSAGE_TYPE].getElementsByTagName("img");
	var added = (hl7mapping_id.substring(0,1) == "_");
	var cancelled = getHl7MappingIndexedValue("hl7_mapping_deleted", i) == 'true';
	var edited = getHl7MappingIndexedValue("hl7_mapping_edited", i) == 'true';

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

function setHl7MappingHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.addtest, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearHl7mappingFields() {
	document.getElementById('d_item_type').value = '';
	document.getElementById('d_interface_name').value = '';
}

function getHl7MappingRows() {
	var table = document.getElementById("hl7mappingtable");
	return  (table.rows.length - 3);
}

function getHl7MappingNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstHl7MappingRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getHl7MappingTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getHl7MappingNumCharges(tableId) + 1;
}

function getHl7MappingChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstHl7MappingRow()];
}

function getHl7MappingRowChargeIndex(row) {
	return row.rowIndex - getFirstHl7MappingRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getHl7MappingIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.addtest, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setHl7MappingIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.addtest, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function openHl7mappingPrevious() {
	var id = document.addtest.editHl7MappingRowId.value;
	id = parseInt(id);
	var row = getHl7MappingChargeRow(id, 'hl7mappingtable');

	if (fieldHl7mappingEdited) {
		if (!editHl7mappingTableRow()) return false;
		fieldHl7mappingEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditHl7MappingDialog(document.getElementsByName('hl7mappingEditAnchor')[parseInt(id)-1]);
	}
}

function openHl7mappingNext() {
	var id = document.addtest.editHl7MappingRowId.value;
	id = parseInt(id);
	var row = getHl7MappingChargeRow(id, 'hl7mappingtable');

	if (fieldHl7mappingEdited) {
		if (!editHl7mappingTableRow()) return false;
		fieldHl7mappingEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('hl7mappingtable').rows.length-2) {
		showEditHl7MappingDialog(document.getElementsByName('hl7mappingEditAnchor')[parseInt(id)+1]);
	}
}
