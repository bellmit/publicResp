

function init(){
		initializeAddDialog();
		initEditDialog();
		validateFields();
		validateAllowAddendumOverrideValue();
		setAllowAddendumOverrideValue();
}

var addDialog;

function initializeAddDialog() {
	var dialogDiv = document.getElementById("centerConfigDivDialog");
	dialogDiv.style.display = 'block';
	addDialog = new YAHOO.widget.Dialog("centerConfigDivDialog",
								{	width:"650px",
									context : ["centerConfList", "tr", "br"],
									visible:false,
									modal:true,
									constraintoviewport:true
								}); 
	
	YAHOO.util.Event.addListener('Add', 'click', addToTable, addDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', closeDialog, addDialog, true);

	var enterKeyListener = new YAHOO.util.KeyListener("addItemDialogFields", { keys:13 },
											{
											   fn:onEnterKeyItemDialog, 
											   scope:addDialog, correctScope:true 
											 } );
	
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
											{ fn:handleAddItemCancel,
                                                scope:addDialog,
                                                correctScope:true } );
	addDialog.cfg.setProperty("keylisteners", [escKeyListener, enterKeyListener]);
	addDialog.render();
	 
 }

var editDialog;
function initEditDialog() {
	var dialogDiv = document.getElementById("editHl7CenterConfig");
	dialogDiv.style.display = 'block';
	
	editDialog = new YAHOO.widget.Dialog("editHl7CenterConfig",{
						width:"650px",
						context :["centerConfList", "tl", "tl"],
						visible:false,
						modal:true,
						constraintoviewport:true
					});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                              { fn:handleEditItemCancel,
                                                scope:editDialog,
                                                correctScope:true } );
	editDialog.cfg.queueProperty("keylisteners", escKeyListener);
	//editDialog.cancelEvent.subscribe(handleEditItemCancel);
	YAHOO.util.Event.addListener('OK', 'click', editTableRow, editDialog, true);
	YAHOO.util.Event.addListener('EditCancel', 'click', closeDialog, editDialog, true);
	/*YAHOO.util.Event.addListener('EditPrevious', 'click', openPrevious, editDialog, true);*/
	/*YAHOO.util.Event.addListener('EditNext', 'click', openNext, editDialog, true);*/
	editDialog.render();
	
}

function showDialog(obj) {
	checkExportType();
	/*checkImportType();*/
	addDialog.show();
}
	
var parentDialog;
function showEditCenterInterface(obj){

	//parentDialog = editDialog;
	
	document.getElementById('editHl7CenterConfig').style.display='block';
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editDialog.show();
	document.hl7Configuration.editRowId.value = id;
	document.getElementById('ed_center_id').value = getIndexedValue("center_id", id);
	document.getElementById('ed_center_name').value = getIndexedValue("center_name", id);
	document.getElementById('ed_center_name').disabled=true;
	document.getElementById('ed_export_type').value = getIndexedValue("export_type", id); 
	document.getElementById('ed_orders_export_dir').value = getIndexedValue("orders_export_dir", id); 
	document.getElementById('ed_orders_export_ip_addr').value = getIndexedValue("orders_export_ip_addr", id);
	document.getElementById('ed_orders_export_port').value = getIndexedValue("orders_export_port", id); 
	/*document.getElementById('ed_import_type').value = getIndexedValue("import_type", id); 
	document.getElementById('ed_import_dir').value = getIndexedValue("import_dir", id); 
	document.getElementById('ed_import_ip_addr').value = getIndexedValue("import_ip_addr", id); 
	document.getElementById('ed_import_port').value = getIndexedValue("import_port", id);*/
	checkEdExportType();
	
}


function onEnterKeyItemDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new autocomplete.)
	addToTable();
}


function closeDialog(obj){
	addDialog.cancel();
	editDialog.cancel()
}
function handleAddItemCancel() {
	this.cancel();
}

function handleEditItemCancel(){
	this.cancel();
}



var colIndex  = 0;
var CENTER_NAME_COL = colIndex++, EXPORT_TYPE = colIndex++, EXPORT_DIR = colIndex++, EXPORT_IP_ADDR =  colIndex++, EXPORT_PORT = colIndex++,
//IMPORT_TYPE = colIndex++, IMPORT_DIR = colIndex++, IMPORT_IP_ADDR = colIndex++, IMPORT_PORT = colIndex++, 
TRASH_COL=colIndex++, EDIT_COL=colIndex++;

function addToTable() {
	
	if(checkDuplicateCenter('d_center_id')){
		return;
	}
	
	var center_id = document.getElementById('d_center_id').value; 
	var export_type = document.getElementById('d_export_type').value; 
	var export_dir= document.getElementById('d_orders_export_dir').value;
	var export_ip_addr= document.getElementById('d_orders_export_ip_addr').value;
	var export_port = document.getElementById('d_orders_export_port').value;
	var center_name = findInList(centersJson, 'center_id', center_id).center_name;
	
	/*var import_type= document.getElementById('d_import_type').value;
	var import_dir= document.getElementById('d_import_dir').value;
	var import_ip_addr= document.getElementById('d_import_ip_addr').value;
	var import_port=document.getElementById('d_import_port').value;*/

	var id = getNumCharges('centerConfList');
	var table = document.getElementById('centerConfList');
	var templateRow = table.rows[getTemplateRow('centerConfList')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row,templateRow);
	row.id = "itemRow" + id;
	var cell = null;
	
	setNodeText(row.cells[CENTER_NAME_COL], center_name);			
	setNodeText(row.cells[EXPORT_TYPE], export_type);			
	setNodeText(row.cells[EXPORT_DIR], export_dir);			
	setNodeText(row.cells[EXPORT_IP_ADDR], export_ip_addr);			
	setNodeText(row.cells[EXPORT_PORT], export_port);	
	
	/*setNodeText(row.cells[IMPORT_TYPE], import_type);			
	setNodeText(row.cells[IMPORT_DIR], import_dir);			
	setNodeText(row.cells[IMPORT_IP_ADDR], import_ip_addr);			
	setNodeText(row.cells[IMPORT_PORT], import_port);		*/	
	
	setHiddenValue(id, "center_id", center_id);
	setHiddenValue(id, "center_name", center_name);	
	setHiddenValue(id, "export_type", export_type);
	setHiddenValue(id, "orders_export_dir", export_dir);
	setHiddenValue(id, "orders_export_ip_addr", export_ip_addr);
	setHiddenValue(id, "orders_export_port", export_port);
	
	/*setHiddenValue(id, "import_type", import_type);
	setHiddenValue(id, "import_dir", import_dir);
	setHiddenValue(id, "import_ip_addr", import_ip_addr);
	setHiddenValue(id, "import_port", import_port);*/
	setHiddenValue(id, "inserted", true);
	addDialog.cancel();
	clearAddDialog();
	
}

function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;

}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.hl7Configuration, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}



function cancelCenterInterface(obj) {
	
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var oldDeleted =  getIndexedValue("deleted", id);
	var isNew = getIndexedValue("inserted", id) == 'true';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
	}else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}

		setIndexedValue("deleted", id, newDeleted);
		setIndexedValue("edited", id, "false");
		setRowStyle(id);
	}
	false;
}


function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.hl7Configuration, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.hl7Configuration, name, index);
	if (obj)
		obj.value = value;
	return obj;
}



function editTableRow(){
	
	var id = document.hl7Configuration.editRowId.value;
	var row = getChargeRow(id, 'centerConfList');
	
	var center_id = document.getElementById('ed_center_id').value; 
	var center_name = document.getElementById('ed_center_name').value; 	
	var export_type = document.getElementById('ed_export_type').value; 	
	var export_dir= document.getElementById('ed_orders_export_dir').value;
	var export_ip_addr= document.getElementById('ed_orders_export_ip_addr').value;
	var export_port = document.getElementById('ed_orders_export_port').value;
		
/*	var import_type= document.getElementById('ed_import_type').value;
	var import_dir= document.getElementById('ed_import_dir').value;
	var import_ip_addr= document.getElementById('ed_import_ip_addr').value;
	var import_port=document.getElementById('ed_import_port').value;
*/
	var cell = null;
	
	setNodeText(row.cells[CENTER_NAME_COL], center_name);			
	setNodeText(row.cells[EXPORT_TYPE], export_type);			
	setNodeText(row.cells[EXPORT_DIR], export_dir);			
	setNodeText(row.cells[EXPORT_IP_ADDR], export_ip_addr);			
	setNodeText(row.cells[EXPORT_PORT], export_port);	
	
/*	setNodeText(row.cells[IMPORT_TYPE], import_type);			
	setNodeText(row.cells[IMPORT_DIR], import_dir);			
	setNodeText(row.cells[IMPORT_IP_ADDR], import_ip_addr);			
	setNodeText(row.cells[IMPORT_PORT], import_port);			
*/	
	setHiddenValue(id, "center_id", center_id);
	setHiddenValue(id, "export_type", export_type);
	setHiddenValue(id, "orders_export_dir", export_dir);
	setHiddenValue(id, "orders_export_ip_addr", export_ip_addr);
	setHiddenValue(id, "orders_export_port", export_port);
/*	
	setHiddenValue(id, "import_type", import_type);
	setHiddenValue(id, "import_dir", import_dir);
	setHiddenValue(id, "import_ip_addr", import_ip_addr);
	setHiddenValue(id, "import_port", import_port);
*/
	setHiddenValue(id, "edited", true);
	editDialog.cancel();
	
}

function handleEditCancel(){
	
	if (childDialog == null) {
		parentDialog = null;
		var id = document.hl7Configuration.editRowId.value;
		var row = getChargeRow(id, "centerConfList");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldEdited = false;
		this.hide();
	}
}

function setRowStyle(i){
	var row = getChargeRow(i, 'centerConfList');
	var inserted = getIndexedValue("inserted", i);
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");
	var added = (inserted.substring(0,1) == "ture");
	var cancelled = getIndexedValue("deleted", i) == 'true';
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
	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;

	
}

function clearAddDialog() {
	
	document.getElementById('d_center_id').value=''; 
	document.getElementById('d_export_type').value='N';
	document.getElementById('d_orders_export_dir').value=null;
	document.getElementById('d_orders_export_ip_addr').value=null;
	document.getElementById('d_orders_export_port').value=null;
	
	document.getElementById('d_orders_export_dir').disabled=true;
	document.getElementById('d_orders_export_ip_addr').disabled=true;
	document.getElementById('d_orders_export_port').disabled=true;
	
	
	
/*	document.getElementById('d_import_type').value='N';
	document.getElementById('d_import_dir').value=null;
	document.getElementById('d_import_ip_addr').value=null;
	document.getElementById('d_import_port').value=null;
	
	document.getElementById('d_import_dir').disabled=true;
	document.getElementById('d_import_ip_addr').disabled=true;
	document.getElementById('d_import_port').disabled=true;*/
	
}

function checkDuplicateCenter(id) {
	
	var centerIds = document.getElementsByName('center_id');
	
	for(var i =0; i< centerIds.length ; i++){
		if(centerIds.item(i).type =='hidden' && centerIds.item(i).value != ''){
			var d_center_id = document.getElementById(id);
			var ed_center_id = document.getElementById('ed_center_id');
			if(d_center_id.value == centerIds.item(i).value) {
				showMessage('js.generalmasters.hl7configuration.addShow.DuplicateCenter');
				return true;
			}
		}
	}
	return false;
}


var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function openPrevious(id, previous, next) {
	var id = document.getElementById('editRowId').value ;
	id = parseInt(id);
	var row = getChargeRow(id, 'centerConfList');
	if (fieldEdited) {
		fieldEdited = false;
		if (!editTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditCenterInterface(document.getElementsByName('editAnchor')[parseInt(id)-1]);
	}
}

/*function openNext() {
	var id = document.getElementById('editRowId').value ;
	id = parseInt(id);
	var row = getChargeRow(id, 'centerConfList');
	if (fieldEdited) {
		fieldEdited = false;
		if (!editTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id+1 != document.getElementById('centerConfList').rows.length-2) {
		showEditCenterInterface(document.getElementsByName('editAnchor')[parseInt(id)+1]);
	}
}*/





function checkExportType(){

	var id = document.getElementById('d_export_type');
	
	if(id.value == 'N'){
		document.getElementById("d_orders_export_dir").value=null;
		document.getElementById("d_orders_export_ip_addr").value=null;
		document.getElementById("d_orders_export_port").value=null;
		
		document.getElementById("d_orders_export_dir").disabled=true;
		document.getElementById("d_orders_export_ip_addr").disabled=true;
		document.getElementById("d_orders_export_port").disabled=true;
	}else if (id.value == 'F' ){
		
		document.getElementById("d_orders_export_dir").value=null;
		document.getElementById("d_orders_export_ip_addr").value=null;
		document.getElementById("d_orders_export_port").value=null;
		
		document.getElementById("d_orders_export_dir").disabled=false;
		document.getElementById("d_orders_export_ip_addr").disabled=true;
		document.getElementById("d_orders_export_port").disabled=true;
	}else if(id.value == 'S') {
		document.getElementById("d_orders_export_dir").value=null;
		document.getElementById("d_orders_export_ip_addr").value=null;
		document.getElementById("d_orders_export_port").value=null;
		
		document.getElementById("d_orders_export_dir").disabled=true;
		document.getElementById("d_orders_export_ip_addr").disabled=false;
		document.getElementById("d_orders_export_port").disabled=false;
		
	}else if(id.value == 'D') {
		document.getElementById("d_orders_export_dir").value=null;
		document.getElementById("d_orders_export_ip_addr").value=null;
		document.getElementById("d_orders_export_port").value=null;
		
		document.getElementById("d_orders_export_dir").disabled=true;
		document.getElementById("d_orders_export_ip_addr").disabled=false;
		document.getElementById("d_orders_export_port").disabled=false;
		
	}else {
		document.getElementById("d_orders_export_dir").disabled=false;
		document.getElementById("d_orders_export_ip_addr").disabled=false;
		document.getElementById("d_orders_export_port").disabled=false
	}
}

function checkImportType(){

	var id = document.getElementById('d_import_type');
	
	if(id.value == 'N'){
		document.getElementById("d_import_dir").value=null;
		document.getElementById("d_import_port").value=null;
		document.getElementById("d_import_ip_addr").value=null;
		
		document.getElementById("d_import_dir").disabled=true;
		document.getElementById("d_import_port").disabled=true;
		document.getElementById("d_import_ip_addr").disabled=true;
	}else if(id.value=='F'){
		document.getElementById("d_import_dir").value=null;
		document.getElementById("d_import_port").value=null;
		document.getElementById("d_import_ip_addr").value=null;
		
		document.getElementById("d_import_dir").disabled=false;
		document.getElementById("d_import_port").disabled=true;
		document.getElementById("d_import_ip_addr").disabled=true;
	}else if(id.value=='S') {
		document.getElementById("d_import_dir").value=null;
		document.getElementById("d_import_port").value=null;
		document.getElementById("d_import_ip_addr").value=null;
		
		document.getElementById("d_import_dir").disabled=true;
		document.getElementById("d_import_port").disabled=false;
		document.getElementById("d_import_ip_addr").disabled=false;

	}else {
		document.getElementById("d_import_dir").disabled=false;
		document.getElementById("d_import_port").disabled=false;
		document.getElementById("d_import_ip_addr").disabled=false;
	}
	
}





function checkEdExportType(){
	
	var ed_export_type = document.getElementById('ed_export_type');
	if(ed_export_type.value == 'N'){
		document.getElementById('ed_orders_export_dir').value=null;
		document.getElementById('ed_orders_export_ip_addr').value=null;
		document.getElementById('ed_orders_export_port').value=null;
		
		document.getElementById('ed_orders_export_dir').disabled=true;
		document.getElementById('ed_orders_export_ip_addr').disabled=true;
		document.getElementById('ed_orders_export_port').disabled=true;
	}else if(ed_export_type.value == 'F') {
		document.getElementById('ed_orders_export_ip_addr').value=null;
		document.getElementById('ed_orders_export_port').value=null;
		document.getElementById('ed_orders_export_ip_addr').disabled=true;
		document.getElementById('ed_orders_export_port').disabled=true;
		document.getElementById('ed_orders_export_dir').disabled=false;
	}else if(ed_export_type.value == 'S') {
		document.getElementById('ed_orders_export_dir').value=null;
		document.getElementById('ed_orders_export_dir').disabled=true;
		document.getElementById('ed_orders_export_ip_addr').disabled=false;
		document.getElementById('ed_orders_export_port').disabled=false;
	}else if(ed_export_type.value == 'D') {
		document.getElementById('ed_orders_export_dir').value=null;
		document.getElementById('ed_orders_export_dir').disabled=true;
		document.getElementById('ed_orders_export_ip_addr').disabled=false;
		document.getElementById('ed_orders_export_port').disabled=false;
	}else {
		document.getElementById('ed_orders_export_dir').disabled=false;
		document.getElementById('ed_orders_export_ip_addr').disabled=false;
		document.getElementById('ed_orders_export_port').disabled=false;
		
	}
	
	
/*	var ed_import_type = document.getElementById('ed_import_type');
	
	if(ed_import_type.value == 'N'){
		document.getElementById('ed_import_dir').value=null;
		document.getElementById('ed_import_ip_addr').value=null;
		document.getElementById('ed_import_port').value=null;
		
		document.getElementById('ed_import_dir').disabled=true;
		document.getElementById('ed_import_ip_addr').disabled=true;
		document.getElementById('ed_import_port').disabled=true;
	}else if(ed_import_type.value == 'F'){
		document.getElementById('ed_import_ip_addr').value=null;
		document.getElementById('ed_import_port').value=null;
		document.getElementById('ed_import_ip_addr').disabled=true;
		document.getElementById('ed_import_port').disabled=true;
		document.getElementById('ed_import_dir').disabled=false;
	}else if(ed_import_type.value == 'S'){
		document.getElementById('ed_import_dir').value=null;
		document.getElementById('ed_import_dir').disabled=true;
		document.getElementById('ed_import_ip_addr').disabled=false;
		document.getElementById('ed_import_port').disabled=false;
	}else {
		document.getElementById('ed_import_dir').disabled=false;
		document.getElementById('ed_import_ip_addr').disabled=false;
		document.getElementById('ed_import_port').disabled=false;
	}*/
}

function validateFields(){
	var sendOrm = document.getElementById("send_orm").value;
	var reportGroupMethod = document.getElementById("report_group_method").value;
	var receivePreliminaryReport = document.getElementById("receive_preliminary_report");
	var receivePreliminaryReportHidden = document.getElementById("receive_preliminary_report_hidden");
	var resultParameterSource = document.getElementById("result_parameter_source");
	var resultParameterSourceHidden = document.getElementById("result_parameter_source_hidden");
	
	if (sendOrm == 'T'){
		resultParameterSource.value = 'H';
		resultParameterSourceHidden.value = 'H';
		resultParameterSource.setAttribute("disabled", "true");
	} else if(sendOrm == 'R') {
		resultParameterSource.value = 'M';
		resultParameterSourceHidden.value = 'M';
		resultParameterSource.setAttribute("disabled", "true");
	}
	
	if ((sendOrm != 'T') || (reportGroupMethod != 'N')){
		receivePreliminaryReport.value = 'N';
		receivePreliminaryReportHidden.value = 'N';
		receivePreliminaryReport.setAttribute("disabled", "true");
	} else {
		receivePreliminaryReport.removeAttribute("disabled");  
	}
}

function setReceivePriliminaryValue() {
	var receivePreliminaryReport = document.getElementById("receive_preliminary_report");
	var receivePreliminaryReportHidden = document.getElementById("receive_preliminary_report_hidden");
	if (receivePreliminaryReport.value == 'Y') {
		receivePreliminaryReportHidden.value='Y';
	} else {
		receivePreliminaryReportHidden.value='N';
	}
}

function validateAllowAddendumOverrideValue() {
	var allowAddendumOverride = document.getElementById("allow_addendum_override");
	var allowAddendumOverrideHidden = document.getElementById("allow_addendum_override_hidden");
	var consolidateMultipleObx = document.getElementById("consolidate_multiple_obx");
	
	if (consolidateMultipleObx.checked) {
		allowAddendumOverride.removeAttribute("disabled");  
	} else {
		allowAddendumOverride.value='N';
		allowAddendumOverrideHidden.value='N';
		allowAddendumOverride.setAttribute("disabled", "true");
	}
}

function setAllowAddendumOverrideValue() {
	var allowAddendumOverride = document.getElementById("allow_addendum_override");
	var allowAddendumOverrideHidden = document.getElementById("allow_addendum_override_hidden");
		
	if (allowAddendumOverride.value == 'Y') {
		allowAddendumOverrideHidden.value='Y';
	} else {
		allowAddendumOverrideHidden.value='N';
	}
}