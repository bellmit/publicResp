
var dlgForm;
var kitForm;
var gItemDetails = {};
var gRowUnderEdit = -1;
var gDialogItem = null;
var medAutoComp;


function init(){
	dlgForm = document.detailForm;
	kitForm = document.orderkitform;

	initAddEditDialog();
	initItemAutoComplete();
}

function addRow() {
    var totalNoOfRows = getNumItems();
    var table = document.getElementById("kititemtable");
    var templateRow = table.rows[getTemplateRow()];
    var row = templateRow.cloneNode(true);
    row.style.display = '';
    table.tBodies[0].insertBefore(row, templateRow);
	return totalNoOfRows;
}


function getNumItems() {
    return document.getElementById("kititemtable").rows.length - 2;
}

function getTemplateRow() {
    return getNumItems() + 1;
}

function initItemAutoComplete() {
    if (medAutoComp != undefined) {
        medAutoComp.destroy();
    }

	dataSource = new YAHOO.widget.DS_JSArray(jItemNames);
	dataSource.responseSchema = {
		resultsList: "result", fields: [{key: "medicine_name"}, {key: "medicine_id"},{key: "issue_base_unit"}]
	};

	medAutoComp = new YAHOO.widget.AutoComplete(dlgForm.medicine_name, 'item_dropdown', dataSource);
	medAutoComp.maxResultsDisplayed = 20;
	medAutoComp.allowBrowserAutocomplete = false;
	medAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	medAutoComp.typeAhead = false;
	medAutoComp.useShadow = false;
	medAutoComp.animVert = false;
	medAutoComp.minQueryLength = 0;
	medAutoComp.forceSelection = true;
	medAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	medAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	medAutoComp.itemSelectEvent.subscribe(onSelectItem);
}

/*
 * Called on selection of an item in the item auto comp
 */
function onSelectItem(type, args) {
	dlgForm.medicine_id.value = args[2][1];
	dlgForm.issue_base_unit.value = args[2][2];
	var row = getThisRow(dlgForm.issue_base_unit);
	setNodeText(row.cells[3], dlgForm.issue_base_unit.value);
}

onAjaxFailure = function (response) {
	alert("Ajax call failed");
}

function openAddDialog() {

	gRowUnderEdit = -1;
	button = document.getElementById("plusItem");
	document.getElementById("addEditDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById("prevDialog").disabled = true;
	document.getElementById("nextDialog").disabled = true;
	
	gDialogItem = newItem();
	objectToForm(gDialogItem, dlgForm);
	
	detaildialog.show();
	document.getElementById("dlgPkgSz").innerHTML = "";
	dlgForm.medicine_name.disabled = false;
	
	setTimeout("dlgForm.medicine_name.focus()", 100);
}


function newItem() {
	var item = {
			medicine_id: '', order_kit_id: '', qty_needed: '',medicine_name: ''
		};

	return item;
}

function initAddEditDialog() {
    detaildialog = new YAHOO.widget.Dialog("addEditDialog", {
        width: "800px",
        context: ["plusItem", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    var escKeyListener = new YAHOO.util.KeyListener("addEditDialog",
			{keys: 27 }, handleDetailDialogCancel);
    detaildialog.cfg.queueProperty("keylisteners", escKeyListener);
    detaildialog.render();
}

function handleDetailDialogCancel() {
    detaildialog.cancel();
}


function onDialogSave() {
	if (!dialogValidate())
		return false;

	// copy dialog to dialogItem
	formToObject(dlgForm, gDialogItem);

	dialogSave();
	return true;
}

function onNextPrev(val) {
	if (!dialogValidate())
		return false;
	dialogSave();

	var index = (val.name == 'prevDialog') ? gRowUnderEdit - 1 : gRowUnderEdit + 1;
	if (index >= getNumItems() || index == -1)
		return;

	openEditDialogIndex(index);
	return true;
}

function dialogValidate(){

	if (!validateRequired(dlgForm.medicine_name, getString("js.storemaster.orderkit.enter.itemname"))) return false;
	if (!validateRequired(dlgForm.qty_needed, getString("js.storemaster.orderkit.empty.qty"))) return false;
	if (!isValidNumber(dlgForm.qty_needed, 'Y')) return false;

	if(dlgForm.qty_needed.value == 0 ){
		alert(getString("js.storemaster.orderkit.zero.qty"));
		dlgForm.qty_needed.focus();
		return false;
	}

	var medNameEls = document.getElementsByName("medicine_name");

	for(var i = 0;i<medNameEls.length-2;i++){
		if ( medNameEls[i].value == dlgForm.medicine_name.value && gRowUnderEdit != i){
			alert(getString("js.storemaster.orderkit.duplicateitem"));
			dlgForm.medicine_name.value = '';
			document.getElementById("dlgPkgSz").innerHTML = "";
			dlgForm.qty_needed.value = '';
			dlgForm.medicine_name.focus();
			return false;
		}
	}
		
	return true;

}

function dialogSave() {

	if (gRowUnderEdit == -1) {
		// new item addition
		addDialogItemToGrid();
		document.getElementById("dlgPkgSz").innerHTML = "";
        detaildialog.cancel();
        openAddDialog();

	} else {
		// existing item edited and saved
		saveDialogItemToGrid(gRowUnderEdit);
        detaildialog.cancel();
	}

}

function addDialogItemToGrid() {
	var rowIndex = addRow();
	saveDialogItemToGrid(rowIndex);
}

function saveDialogItemToGrid(rowIndex) {

	var row = getItemRow(rowIndex);
	gDialogItem = null;				// ensure we don't refer to this again.
	getElementByName(row,'medicine_id').value = dlgForm.medicine_id.value;
	getElementByName(row,'qty_needed').value = dlgForm.qty_needed.value;
	getElementByName(row,'medicine_name').value = dlgForm.medicine_name.value;
	getElementByName(row,'issue_base_unit').value = dlgForm.issue_base_unit.value;
	
	setNodeText(row.cells[0], dlgForm.medicine_name.value);
	setNodeText(row.cells[1], dlgForm.issue_base_unit.value);
	setNodeText(row.cells[2], dlgForm.qty_needed.value);
	
	row.className = '';
}

function openEditDialogIndex(rowIndex) {
	openEditDialog(getItemRow(rowIndex));
}

function openEditDialog(row) {
    row.className = 'editing';
	gRowUnderEdit = getRowItemIndex(row);

    dlgForm.medicine_name.value = getElementByName(row,'medicine_name').value;
    dlgForm.medicine_id.value = getElementByName(row,'medicine_id').value;
    dlgForm.qty_needed.value = getElementByName(row,'qty_needed').value;
    dlgForm.issue_base_unit.value = getElementByName(row,'issue_base_unit').value;
    document.getElementById("dlgPkgSz").innerHTML = dlgForm.issue_base_unit.value;

    medAutoComp._bItemSelected = true;

	var button = row.cells[4];
	document.getElementById("addEditDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById("prevDialog").disabled = false;
	document.getElementById("nextDialog").disabled = false;

	detaildialog.show();
	dlgForm.medicine_name.disabled = true;
   // setTimeout("dlgForm.medicine_name.focus()", 100);
	setTimeout("dlgForm.qty_needed.focus()", 100);
}

function onKeyPressAddQty(e) {
    e = (e) ? e : event;
    var charCode = (e.charCode) ? e.charCode : ((e.which) ? e.which : e.keyCode);
    if (charCode == 13) {
		onDialogSave();
        return false;
    }else{
    	return enterNumOnlyzeroToNine(e);
    }
}

function getItemRow(i) {
    i = parseInt(i);
    var table = document.getElementById("kititemtable");
    return table.rows[i + getFirstItemRow()];
}

function getFirstItemRow() {
    // index of the first charge item: 0 is header, 1 is first charge item.
    return 1;
}

function onSubmit(){
	if ( validate() ){
		document.orderkitform.submit();
	}
}

function validate(){
	document.orderkitform.order_kit_name.value = document.orderkitform.order_kit_name.value.trim();
	if ( document.orderkitform.order_kit_name.value == '' ) {
		alert(getString("js.storemaster.orderkit.entername"));
		document.orderkitform.order_kit_name.focus();
		return false;
	}
	var len = orderkit_names.length;
	for(var i =0;i<len;i++){
		if(document.orderkitform.order_kit_name.value.toUpperCase() == orderkit_names[i].order_kit_name.toUpperCase()){
			alert(getString("js.storemaster.orderkit.duplicatename"));
			document.orderkitform.order_kit_name.focus();
			return false;
		}
	}
	var deletedEls = document.getElementsByName('deleted');
	var hasItems = deletedEls.length > 1;

	if ( !hasItems ) {
		alert(getString("js.storemaster.orderkit.enteritem"));
		return false;
	} else {
		for(var i =0;i<deletedEls.length-1;i++){
			hasItems = (deletedEls[i].value == 'N');
			if ( hasItems ) break;
		}

		if ( !hasItems ) {
			alert(getString("js.storemaster.orderkit.enteritem"));
			return false;
		}
	}
	return true;
}

function onEditRow(img) {
    var row = findAncestor(img, "TR");
	openEditDialog(row);
}

function getRowItemIndex(row) {
    return row.rowIndex - getFirstItemRow();
}


function onDeleteRow(obj) {
    var row = getThisRow(obj);
    var medicine_id  = getElementByName(row,"medicine_id").value;
	var rowIndex = getRowItemIndex(row);
	var newEl = empty(medicine_id);
	var delEl = getElementByName(row,"deleted");

	if ( newEl ) {
		// delete from the grid
	    row.parentNode.removeChild(row);
    } else {
		if( delEl.value == 'N' ){
	    	var trashImg = row.cells[3].getElementsByTagName("img")[0];
			trashImg.src = cpath+"/icons/undo_delete.gif";
			delEl.value = 'Y';
		} else {
			var trashImg = row.cells[3].getElementsByTagName("img")[0];
			trashImg.src = cpath+"/icons/delete.gif";
			delEl.value = 'N';
		}
    }

    return false;
}