var dlgForm;
function init(){
	dlgForm = document.editForm;
	document.getElementById("save").disabled  = (document.otstockconsumeform.non_sterile_store.options.length == 0);
	initEditDialog();
}

function initEditDialog() {
    detaildialog = new YAHOO.widget.Dialog("editDialog", {
        width: "500px",
        context: ["editicon", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    var escKeyListener = new YAHOO.util.KeyListener("editDialog",
			{keys: 27 }, handleDetailDialogCancel);
    detaildialog.cfg.queueProperty("keylisteners", escKeyListener);
    detaildialog.render();
}

function onEditRow(img) {
    var row = findAncestor(img, "TR");
	openEditDialog(row);
}


function openEditDialog(row) {
    row.className = 'editing';
	gRowUnderEdit = getRowItemIndex(row);

	setNodeText(document.getElementById("e_medicine_name"), getElementByName(row,'medicine_name').value);
    dlgForm.e_qty_consumed.value = getElementByName(row,'qty_consumed').value;
    dlgForm.e_qty_returned.value = getElementByName(row,'qty_returned').value;

    var issue_type = getElementByName(row,"issue_type").value;
    dlgForm.e_qty_returned.disabled = ( issue_type != 'L' && issue_type != 'P' );
    dlgForm.e_qty_consumed.disabled = ( issue_type == 'L' || issue_type == 'P' );
    dlgForm.qty.value = getElementByName(row,'qty').value;
    dlgForm.e_issue_type.value = issue_type;

	var button = row.cells[4];
	document.getElementById("editDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById("prevDialog").disabled = false;
	document.getElementById("nextDialog").disabled = false;

	detaildialog.show();
    setTimeout("dlgForm.e_qty_consumed.focus()", 100);
}


function getRowItemIndex(row) {
    return row.rowIndex - getFirstItemRow();
}
function getFirstItemRow() {
    // index of the first charge item: 0 is header, 1 is first charge item.
    return 1;
}

function onDialogSave() {
	if (!dialogValidate())
		return false;

	dialogSave();
	return true;
}

function dialogValidate(){
	var qty = dlgForm.qty.value;

	if(parseInt(dlgForm.e_qty_consumed.value) > qty){
		alert("Consumed quantity can not exceed actual quantity "+qty);
		dlgForm.e_qty_consumed.focus();
		return false;
	}

	if(parseInt(dlgForm.e_qty_returned.value) > qty){
		alert("Returned quantity can not exceed actual quantity "+qty);
		dlgForm.e_qty_consumed.focus();
		return false;
	}

	if( ( dlgForm.e_issue_type.value != 'L' && dlgForm.e_issue_type.value != 'P' ) && parseInt(dlgForm.e_qty_consumed.value)+parseInt(dlgForm.e_qty_returned.value) < qty){
		alert("The sum of Consumed and Returned quantity can not be less than actual quantity "+qty);
		dlgForm.e_qty_consumed.focus();
		return false;
	}

	if(parseInt(dlgForm.e_qty_consumed.value)+parseInt(dlgForm.e_qty_returned.value) > qty){
		alert("The sum of Consumed and Returned quantity can not exceed actual quantity "+qty);
		dlgForm.e_qty_consumed.focus();
		return false;
	}

	if (!isValidNumber(dlgForm.e_qty_consumed, 'Y')) return false;

	return true;
}

function dialogSave(){
	saveDialogItemToGrid(gRowUnderEdit);
    detaildialog.cancel();
}

function saveDialogItemToGrid(rowIndex) {

	var row = getItemRow(rowIndex);
	gDialogItem = null;

	setNodeText(row.cells[1], dlgForm.e_qty_consumed.value);
	setNodeText(row.cells[2], dlgForm.e_qty_returned.value);
	setNodeText(row.cells[3], parseInt(dlgForm.e_qty_consumed.value)+parseInt(dlgForm.e_qty_returned.value));

	getElementByName(row,"qty_consumed").value = dlgForm.e_qty_consumed.value;
	getElementByName(row,"qty_returned").value = dlgForm.e_qty_returned.value;
	row.className = '';
}

function getItemRow(i) {
    i = parseInt(i);
    var table = document.getElementById("otconumablestable");
    return table.rows[i + getFirstItemRow()];
}

function handleDetailDialogCancel() {
    detaildialog.cancel();
}

function setRemainingQty(fromObj,toObj){
	if( parseInt(fromObj.value) > parseInt(dlgForm.qty.value) ) {
		alert("Quantity is exceeding actual quantity"+dlgForm.qty.value);
		fromObj.value = 0;
		fromObj.focus();
		return false;
	}

	if ( dlgForm.e_issue_type.value != 'L' && dlgForm.e_issue_type.value != 'P' )
		toObj.value = dlgForm.qty.value - fromObj.value;
}

function onSubmit(){
	document.otstockconsumeform.submit();
}
function onNextPrev(val) {
	if (!dialogValidate())
		return false;
	dialogSave();

	var index = (val.name == 'prevDialog') ? gRowUnderEdit - 1 : gRowUnderEdit + 1;
	if (index >= getNumItems() || index == -1)
		return;

	openEditDialog(getItemRow(index));
	return true;
}


function getNumItems() {
    return document.getElementById("otconumablestable").rows.length - 1;
}