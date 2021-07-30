function init() {
	initItemDialog();
	initEditItemDialog();	
}


function initItemDialog() {
	var dialogDiv = document.getElementById("addItemDialog");
	dialogDiv.style.display = 'block';
	addItemDialog = new YAHOO.widget.Dialog("addItemDialog",
			{	width:"700px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add', 'click', addToTable, addItemDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleAddItemCancel, addItemDialog, true);
	var enterKeyListener = new YAHOO.util.KeyListener("addItemDialogFields", { keys:13 },
				{ fn:addToTable, scope:addItemDialog, correctScope:true } );
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddItemCancel,
	                                                scope:addItemDialog,
	                                                correctScope:true } );
	addItemDialog.cfg.setProperty("keylisteners", [escKeyListener, enterKeyListener]);
	addItemDialog.render();
}

function initEditItemDialog() {
	var dialogDiv = document.getElementById("editItemDialog");
	dialogDiv.style.display = 'block';
	editItemDialog = new YAHOO.widget.Dialog("editItemDialog",{
			width:"700px",
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

var itemsAdded = 0;
var colIndex  = 0;
var CENTER = colIndex++, OUTSOURCE = colIndex++, CHARGE = colIndex++, DEFAULT =  colIndex++, STATUS = colIndex++, 
	TRASH_COL = colIndex++, EDIT_COL = colIndex++;

function addToTable() {

	if (!checkMandatoryFields('add'))
		return false;
	if (!checkDuplicates('add'))
		return false;
	
	var id = getNumCharges('itemsTable');
   	var table = document.getElementById("itemsTable");
	var templateRow = table.rows[getTemplateRow('itemsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	var centerID = document.getElementById('ad_center_id').value;
   	var centerName = document.getElementById("ad_center_id").options[document.getElementById("ad_center_id").selectedIndex].text;
   	var outsourceID = document.getElementById('ad_outsources').value;
   	var outsourceName = document.getElementById("ad_outsources").options[document.getElementById("ad_outsources").selectedIndex].text;
   	var charge = document.getElementById("ad_charge").value;
   	var status = document.getElementById("ad_status").value;
   	var isDefault = document.getElementById("ad_default").value;

   	setNodeText(row.cells[CENTER], centerName);
   	setNodeText(row.cells[OUTSOURCE], outsourceName);
	setNodeText(row.cells[CHARGE], charge);
	setNodeText(row.cells[DEFAULT], isDefault == "Y" ? 'Yes' : 'No');
	setNodeText(row.cells[STATUS], (status == "A") ? 'Active' : 'InActive');

	setHiddenValue(id, "source_center_id", centerID);
	setHiddenValue(id, "outsource_dest_id", outsourceID);
	setHiddenValue(id, "status", status);
	setHiddenValue(id, "default_outsource", isDefault);
	setHiddenValue(id, "charge", charge);
	setHiddenValue(id, "added", "true");
	
	itemsAdded++;
	clearFields();
	setRowStyle(id);
	addItemDialog.align("tr", "tl");
	return id;
}

function editTableRow() {

	if (!checkMandatoryFields('edit'))
		return false;
	if (!checkDuplicates('edit'))
		return false;
	
	var id = document.outHouseForm.editRowId.value;
	var row = getChargeRow(id, 'itemsTable');
	
	if (!notAllowToIncactive(id)) {
		return false;
	}
	
	var centerID = document.getElementById('ed_center_id').value;
   	var centerName = document.getElementById("ed_center_id").options[document.getElementById("ed_center_id").selectedIndex].text;
   	var outsourceID = document.getElementById('ed_outsources').value;
   	var outsourceName = document.getElementById("ed_outsources").options[document.getElementById("ed_outsources").selectedIndex].text;
   	var charge = document.getElementById("ed_charge").value;
   	var status = document.getElementById("ed_status").value;
   	var isDefault = document.getElementById("ed_default").value;

   	setNodeText(row.cells[CENTER], centerName);
   	setNodeText(row.cells[OUTSOURCE], outsourceName);
	setNodeText(row.cells[CHARGE], charge);
	setNodeText(row.cells[DEFAULT], isDefault == 'Y' ? 'Yes' : 'No');
	setNodeText(row.cells[STATUS], (status == 'A') ? 'Active' : 'InActive');

	setHiddenValue(id, "source_center_id", centerID);
	setHiddenValue(id, "outsource_dest_id", outsourceID);
	setHiddenValue(id, "charge", charge);
	setHiddenValue(id, "status", status);
	setHiddenValue(id, "default_outsource", isDefault);
	setHiddenValue(id, "edited", "true");
	
	YAHOO.util.Dom.removeClass(row, 'editing');
	setRowStyle(id);

	editItemDialog.cancel();
	return true;
}

function showEditItemDialog(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	
	document.outHouseForm.editRowId.value = id;
	document.getElementById('ed_center_id').value = getIndexedValue("source_center_id", id);
	fillOutsourcesForEdit();
	document.getElementById('ed_outsources').value = getIndexedValue("outsource_dest_id", id);
	document.getElementById('ed_charge').value = getIndexedValue("charge", id);
	document.getElementById('ed_status').value = getIndexedValue("status", id);
	document.getElementById('ed_default').value = getIndexedValue("default_outsource", id);
	
	editItemDialog.show();
	return false;
}

function cancelItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var isNew = getIndexedValue("added", id) == 'true';
	var oldDeleted =  getIndexedValue("delItem", id);

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

function openPrevious() {
	var id = document.outHouseForm.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (!editTableRow()) return false;

	if (id != 0) {
		showEditItemDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var id = document.outHouseForm.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');
	
	if (!editTableRow()) return false;

	if (id+1 != document.getElementById('itemsTable').rows.length-2) {
		showEditItemDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
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

function handleAddItemCancel() {
	this.cancel();	
}

function showAddItemDialog(obj) {

	var row = getThisRow(obj);

	addItemDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addItemDialog.show();
	clearFields();
	parentDialog = addItemDialog;
	fillOutsources();
	return false;
}

function handleEditItemCancel() {
	var id = document.outHouseForm.editRowId.value;
	var row = getChargeRow(id, "itemsTable");
	YAHOO.util.Dom.removeClass(row, 'editing');
	fieldEdited = false;
	this.hide();
}

function clearFields() {
	document.getElementById('ad_center_id').value = '';
	document.getElementById('ad_status').value = 'A';
	document.getElementById('ad_charge').value = '';
	document.getElementById('ad_default').value = 'N';
	var outsource = document.getElementById('ad_outsources');
	outsource.length = 0;
	outsource.options[0] = new Option("-- Select --", ""); 
}

function fillOutsources() {
	var center_id = document.getElementById('ad_center_id').value;
	var filteredList = filterList(centerOutsourcesJSON, "center_id", center_id);
	loadSelectBox(document.getElementById('ad_outsources'), filteredList, "outsource_name", "outsource_dest_id");
}

function fillOutsourcesForEdit() {
	var center_id = document.getElementById('ed_center_id').value;
	var filteredList = filterList(centerOutsourcesJSON, "center_id", center_id);
	loadSelectBox(document.getElementById('ed_outsources'), filteredList, "outsource_name", "outsource_dest_id");
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.outHouseForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.outHouseForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.outHouseForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');

 	//var flagImgs = row.cells[ITEM_TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = getIndexedValue("added", i) == 'true';
	var cancelled = getIndexedValue("delItem", i) == 'true';
	var edited = getIndexedValue("edited", i) == 'true';

	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
	
	row.className = cls;
	
}

function checkMandatoryFields(from) {
	var outsourceElName = "";
	var sourceCenterElName = "";
	var chargeElName = "";
	
	if (from == 'add') {
		outsourceElName = "ad_outsources";
		sourceCenterElName = "ad_center_id";
		chargeElName = "ad_charge";
	} else {		
		outsourceElName = "ed_outsources";
		sourceCenterElName = "ed_center_id";
		chargeElName = "ed_charge";
	}
	
	var sourceCenterValue = document.getElementById(sourceCenterElName).
			options[document.getElementById(sourceCenterElName).selectedIndex].value;
	if (empty(sourceCenterValue)) {
		alert("Please select center..");
		document.getElementById(sourceCenterElName).focus();
		return false;
	}	
	var outsourceObj = document.getElementById(outsourceElName).
			options[document.getElementById(outsourceElName).selectedIndex];
	if (empty(outsourceObj) || empty(outsourceObj.value)) {
		alert("Please select outsource..");
		document.getElementById(outsourceElName).focus();
		return false;
	}
	
	var charge = document.getElementById(chargeElName).value;
	if(trim(charge)=='') {
		alert("Please enter charge for a test..");
		document.getElementById(chargeElName).focus();
		return false;
	}
	
	return true;
}

function checkDuplicates(from) {
	
	var sourceCenterElName = "";
	var outsourceElName = "";
	var defaultOutsourceElName = "";
	var sourceCenterElms = document.getElementsByName("source_center_id");
	var outsourceDestElms = document.getElementsByName("outsource_dest_id");
	var defaultOutsourceElms = document.getElementsByName("default_outsource");
	var id = empty (document.outHouseForm.editRowId.value) ? '' : parseInt(document.outHouseForm.editRowId.value);	
	
	if (from == 'add') {
		outsourceElName = "ad_outsources";
		sourceCenterElName = "ad_center_id";
		defaultOutsourceElName = "ad_default";
		
	} else {
		outsourceElName = "ed_outsources";
		sourceCenterElName = "ed_center_id";	
		defaultOutsourceElName = "ed_default";
	}
	
	var selectedDefaultValue = document.getElementById(defaultOutsourceElName).value;
	var outsourceValue = document.getElementById(outsourceElName).
			options[document.getElementById(outsourceElName).selectedIndex].value;
	var sourceCenterValue = document.getElementById(sourceCenterElName).
			options[document.getElementById(sourceCenterElName).selectedIndex].value;
	
	var outsourceName = document.getElementById(outsourceElName).
			options[document.getElementById(outsourceElName).selectedIndex].text;
	var sourceCenterName = document.getElementById(sourceCenterElName).
			options[document.getElementById(sourceCenterElName).selectedIndex].text;
	
	for (var i=0; i<sourceCenterElms.length; i++) {		
		if ((from == 'add' ? true : i != id) && sourceCenterElms[i].value == sourceCenterValue 
				&& outsourceDestElms[i].value == outsourceValue) {
			alert("Outsource "+ outsourceName + ", is already mapped to center "+ sourceCenterName);
			return false;
		}
	}
	
	for (var i=0; i<sourceCenterElms.length; i++) {
		if ((from == 'add' ? true : i != id) && sourceCenterElms[i].value == sourceCenterValue && selectedDefaultValue == "Y"
				&& defaultOutsourceElms[i].value == "Y") {
			alert("Default outsource is already available for center "+ sourceCenterName);
			return false;
		}
	}
	
	return true;
}

function validateForm() {
	var method = document.getElementById("_method").value;
	var rowsLen = document.getElementById('itemsTable').rows.length - 2;
	
	if (method == 'insertTestToOuthouse') {
		var testName = document.getElementById("test_id").value;
		if(trim(testName)=='') {
			alert("Please select test name..");
			return false;
		}				
	}
	
	var delItemElms = document.getElementsByName("delItem");
	var statusElms = document.getElementsByName("status");
	var count = 0;
	for (i=0; i < delItemElms.length; i++) {
		if (delItemElms[i].value == 'true') {
			count++;
		}
	}
	if(count == rowsLen || rowsLen == 0){
		alert("At least one outsource association is required");
		return false;
	}
	
	document.outHouseForm.submit();
}

function notAllowToIncactive(rowIdx) {
	var recordExists = getIndexedValue('added', rowIdx) == 'false';
	var existingStatus = getIndexedValue('db_status', rowIdx);
	var currentStatus = document.getElementById("ed_status").value;
	
	if (recordExists && existingStatus == 'I' && currentStatus == 'A') {
		alert('You cannot make status as active which is already inactivated.');
		return false;
	}
	
	return true;
}

