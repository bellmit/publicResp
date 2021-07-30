function init(status) {
	if(empty(document.getElementById('dept_id').value)){
		showMessage("js.stores.mgmt.usernothave.assignedstore");
		return false;
	}
	showFilterActive(document.stockSearchForm);
	initStockDetailsDialog();
	initStockConsumptionDetailsDialog();
	if(method != 'getStoreStockConsumptionSearchScreen')
		checkCheckBoxes(status);
	initItemAutoComplete();
}

function initItemAutoComplete() {
	var items = {"item_names": jItemNames};
	var localDs = new YAHOO.util.LocalDataSource(items, { queryMatchContains : true });
	localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = { resultsList : "item_names",
		fields: [ {key : "CUST_ITEM_CODE_WITH_NAME"},{key : "MEDICINE_NAME"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('medicine_name', 'item_dropdown', localDs);

	autoComp.maxResultsDisplayed = 50;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = false;
	autoComp.minQueryLength = 0;
	autoComp.forceSelection = true;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;
	autoComp.itemSelectEvent.subscribe(onSelectItem);
}

var onSelectItem = function(type, args) {
	document.getElementById('medicine_name').value = args[2][1];
}

function checkCheckBoxes(status) {
	if(!empty(status) &&
		(status == 'X' || status == 'F') && document.stockConsumptionDetailsForm._c_select_item) {
		var checkBoxes = document.stockConsumptionDetailsForm._c_select_item;
		if(checkBoxes && checkBoxes.length) {
			for(var i=0;i<checkBoxes.length;i++) {
				if(document.getElementById('_c_select_item'+i).checked) {
					document.getElementById('_c_select_item'+i).checked = false;
				}
				document.getElementById('_c_select_item'+i).disabled = true;
			}
		} else {
			document.stockConsumptionDetailsForm._c_select_item.checked = false;
			document.stockConsumptionDetailsForm._c_select_item.disabled = true;
		}
	} else {
		if(document.stockConsumptionDetailsForm._c_select_item) {
			var checkBoxes = document.stockConsumptionDetailsForm._c_select_item;
			if(checkBoxes && checkBoxes.length) {
				for(var i=0;i<checkBoxes.length;i++) {
					document.getElementById('_c_select_item'+i).disabled = false;
					document.getElementById('_c_select_item'+i).checked = true;
				}
			} else {
				 document.stockConsumptionDetailsForm._c_select_item.disabled = false;
				 document.stockConsumptionDetailsForm._c_select_item.checked = true;
			}
		}
	}
}

// checking the store while seraching,if it is null then showing a message 'store is mandatory'.
// because Stock Consumed Transaction is per store.
function checkStore() {
	document.stockSearchForm.dept_id.value = document.stockSearchForm.dept_id.value;
	if(empty(document.stockSearchForm.dept_id.value)) {
		showMessage("js.stores.mgmt.enterstore.search");
		document.stockSearchForm.dept_id.focus();
		return false;
	}
	return true;
}

function setCheckBoxesValue(obj,index) {
	var row = getThisRow(obj)
	if(obj.checked) {
		document.getElementById('_s_is_insert'+index).value = 'Y';
		row.className = 'rowbgToolbar';
	} else {
		document.getElementById('_s_is_insert'+index).value = 'N';
		row.className = "";
	}
}

function setConsumedItemValue(obj,index) {
	var row = getThisRow(obj)
	if(obj.checked) {
		document.getElementById('_c_is_update'+index).value = 'Y';
		row.className = 'rowbgToolbar';
	} else {
		document.getElementById('_c_is_update'+index).value = 'N';
		row.className = "";
	}
}

function validateStockDetails() {
	var checkBoxes = document.stockConsumptionDetailsForm._s_select_item;
	var flag = false;
	if(checkBoxes.length) {
		for(var i=0;i<checkBoxes.length;i++) {
			if(document.getElementById('_s_select_item'+i).checked) {
				flag = true;
			}
		}
	} else {
		var checkBox = document.stockConsumptionDetailsForm._s_select_item;
		if (!checkBox.disabled && checkBox.checked)
			flag = true;
	}
	if(!flag) {
		showMessage("js.stores.mgmt.checkmoreitems.save");
	} else {
		document.stockConsumptionDetailsForm._method.value = 'saveStockConsumptions';
		document.stockConsumptionDetailsForm.submit();
	}
}

function validateConsumptionDetails() {
/*	var checkBoxes = document.stockConsumptionDetailsForm._c_select_item;
	var flag = false;
	if(checkBoxes.length) {
		for(var i=0;i<checkBoxes.length;i++) {
			if(document.getElementById('_c_select_item'+i).checked) {
				flag = true;
			}
		}
	} else {
		var checkBox = document.stockConsumptionDetailsForm._c_select_item;
		if (!checkBox.disabled && checkBox.checked)
			flag = true;
	}
	if(!flag) {
		alert("check one or more items to save.");
	} else {
		document.stockConsumptionDetailsForm._method.value = 'updateStockConsumptions';
		document.stockConsumptionDetailsForm.submit();
	}*/
	document.stockConsumptionDetailsForm._method.value = 'updateStockConsumptions';
	document.stockConsumptionDetailsForm.submit();
}

var stockDetailsDialog;
var stockConsumptionDetailsDialog

function initStockDetailsDialog() {
	stockDetailsDialog = new YAHOO.widget.Dialog("stock_details_dialog", {
		width:"300px",
		context : ["_s_editIcon", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true,
	} );

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeStockDetailsDialog,
	                                                scope:stockDetailsDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:insertRecord,
	                                                scope:stockDetailsDialog,
	                                                correctScope:true } );

	YAHOO.util.Event.addListener('editDialogOk', "click", insertRecord, stockDetailsDialog, true);
	YAHOO.util.Event.addListener('editDialogCancel', "click", handleCancel, stockDetailsDialog, true);
	YAHOO.util.Event.addListener('editDialogPrevious', 'click', openPrevious, stockDetailsDialog, true);
	YAHOO.util.Event.addListener('editDialogNext', 'click', openNext, stockDetailsDialog, true);
	stockDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener,enterKeyListener]);
	stockDetailsDialog.render();
}

function initStockConsumptionDetailsDialog() {
	stockConsumptionDetailsDialog = new YAHOO.widget.Dialog("consumption_details_dialog", {
		width:"300px",
		context : ["_c_editIcon", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true,
	} );

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeStockConsumptionDialog,
	                                                scope:stockConsumptionDetailsDialog,
	                                                correctScope:true } );

	var enterKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:insertRecord1,
	                                                scope:stockConsumptionDetailsDialog,
	                                                correctScope:true } );

	YAHOO.util.Event.addListener('editDialogOk1', "click", insertRecord1, stockConsumptionDetailsDialog, true);
	YAHOO.util.Event.addListener('editDialogCancel1', "click", handleCancel1, stockConsumptionDetailsDialog, true);
	YAHOO.util.Event.addListener('editDialogPrevious1', 'click', openPrevious1, stockConsumptionDetailsDialog, true);
	YAHOO.util.Event.addListener('editDialogNext1', 'click', openNext1, stockConsumptionDetailsDialog, true);
	stockConsumptionDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener,enterKeyListener]);
	stockConsumptionDetailsDialog.render();
}

function closeStockDetailsDialog() {
	stockDetailsDialog.hide();
}

function closeStockConsumptionDialog() {
	stockConsumptionDetailsDialog.hide();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	// index of the first doctor fee item: 0 is header, 1 is first fee item.
	return 1;
}

function setIndexedValue(form, name, index, value) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function getRow(table,i) {
	i = parseInt(i);
	var table = document.getElementById(table);
	return table.rows[i + getFirstItemRow()];
}

function setHiddenValue(mainform, index, name, value) {
	var el = getIndexedFormElement(mainform, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}
var d_edited = false;

function openPrevious() {
	d_edited = false;
	var table = "storeDetailsTable";
	var id = document.getElementById('_s_editRowId').value;
	var totalQty =  getIndexedFormElement(stockConsumptionDetailsForm, '_s_store_qty', id).value;
	var consumedQty =  document.getElementById('_d_consumed_qty').value;
	var balanceQty = document.getElementById('_d_balance_qty').value;

	var p_consumedQty = getIndexedFormElement(stockConsumptionDetailsForm, '_s_consumed_qty', id).value;
	var p_balanceQty = getIndexedFormElement(stockConsumptionDetailsForm, '_s_balance_qty', id).value;

	if((parseFloat(p_consumedQty) != parseFloat(consumedQty))) {
		consumedQty = document.getElementById('_d_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	} else if((parseFloat(p_balanceQty) != balanceQty)) {
		balanceQty = document.getElementById('_d_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}

	if (validateStockQty(totalQty, consumedQty, balanceQty)) {
		var row =  getRow(table,id);
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_s_consumed_qty", consumedQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_s_balance_qty", balanceQty);
		this.cancel();
		if(d_edited) {
			document.getElementById('_s_select_item'+id).checked = true;
			document.getElementById('_s_is_insert'+id).value = "Y";
			row.className = 'rowbgToolbar';
		}
		if (parseInt(id) != 0)
			openStockDialogBox(document.getElementsByName('_s_editIcon')[parseInt(id)-1]);
	}
}

function openPrevious1() {
	d_edited = false;
	var table = "stcokConsumptionDetailsTable";
	var id = document.getElementById('_c_editRowId').value;
	var totalQty =  getIndexedFormElement(stockConsumptionDetailsForm, '_c_store_qty', id).value;
	var consumedQty =  document.getElementById('_d1_consumed_qty').value;
	var balanceQty = document.getElementById('_d1_balance_qty').value;

	var p_consumedQty = getIndexedFormElement(stockConsumptionDetailsForm, '_c_consumed_qty', id).value;
	var p_balanceQty = getIndexedFormElement(stockConsumptionDetailsForm, '_c_balance_qty', id).value;

	if((parseFloat(p_consumedQty) != parseFloat(consumedQty))) {
		consumedQty = document.getElementById('_d1_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	} else if((parseFloat(p_balanceQty) != balanceQty)) {
		balanceQty = document.getElementById('_d1_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}

	if (validateStockQty(totalQty, consumedQty, balanceQty)) {
		var row =  getRow(table,id);
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_c_consumed_qty", consumedQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_c_balance_qty", balanceQty);
		this.cancel();
		if(d_edited) {
			document.getElementById('_c_select_item'+id).checked = true;
			document.getElementById('_c_is_update'+id).value = "Y";
			row.className = 'rowbgToolbar';
		}
		if (parseInt(id) != 0)
			openConsumptionDialogBox(document.getElementsByName('_c_editIcon')[parseInt(id)-1]);
	}
}

function openNext() {
	d_edited = false
	var table = "storeDetailsTable";
	var id = document.getElementById('_s_editRowId').value;
	var totalQty =  getIndexedFormElement(stockConsumptionDetailsForm, '_s_store_qty', id).value;
	var consumedQty =  document.getElementById('_d_consumed_qty').value;
	var balanceQty = document.getElementById('_d_balance_qty').value;

	var p_consumedQty = getIndexedFormElement(stockConsumptionDetailsForm, '_s_consumed_qty', id).value;
	var p_balanceQty = getIndexedFormElement(stockConsumptionDetailsForm, '_s_balance_qty', id).value;

	if((parseFloat(p_consumedQty) != parseFloat(consumedQty))) {
		consumedQty = document.getElementById('_d_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	} else if((parseFloat(p_balanceQty) != balanceQty)) {
		balanceQty = document.getElementById('_d_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}

	if (validateStockQty(totalQty, consumedQty, balanceQty)) {
		var row =  getRow(table,id);
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_s_consumed_qty", consumedQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_s_balance_qty", balanceQty);
		this.cancel();
		if(d_edited) {
			document.getElementById('_s_select_item'+id).checked = true;
			document.getElementById('_s_is_insert'+id).value = "Y";
			row.className = 'rowbgToolbar';
		}
		if (parseInt(id) != document.getElementById('storeDetailsTable').rows.length-2) {
			openStockDialogBox(document.getElementsByName('_s_editIcon')[parseInt(id)+1]);
		}
	}
}

function openNext1() {
	d_edited = false
	var table = "stcokConsumptionDetailsTable";
	var id = document.getElementById('_c_editRowId').value;
	var totalQty =  getIndexedFormElement(stockConsumptionDetailsForm, '_c_store_qty', id).value;
	var consumedQty =  document.getElementById('_d1_consumed_qty').value;
	var balanceQty = document.getElementById('_d1_balance_qty').value;

	var p_consumedQty = getIndexedFormElement(stockConsumptionDetailsForm, '_c_consumed_qty', id).value;
	var p_balanceQty = getIndexedFormElement(stockConsumptionDetailsForm, '_c_balance_qty', id).value;

	if((parseFloat(p_consumedQty) != parseFloat(consumedQty))) {
		consumedQty = document.getElementById('_d1_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	} else if((parseFloat(p_balanceQty) != balanceQty)) {
		balanceQty = document.getElementById('_d1_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}

	if (validateStockQty(totalQty, consumedQty, balanceQty)) {
		var row =  getRow(table,id);
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_c_consumed_qty", consumedQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_c_balance_qty", balanceQty);
		this.cancel();
		if(d_edited) {
			document.getElementById('_c_select_item'+id).checked = true;
			document.getElementById('_c_is_update'+id).value = "Y";
			row.className = 'rowbgToolbar';
		}
		if (parseInt(id) != document.getElementById('stcokConsumptionDetailsTable').rows.length-2) {
			openConsumptionDialogBox(document.getElementsByName('_c_editIcon')[parseInt(id)+1]);
		}
	}
}


function handleCancel() {
	this.cancel();
}

function handleCancel1() {
	this.cancel();
}

function openStockDialogBox(obj) {
	d_edited = false;
	document.getElementById('stock_details_dialog').style.display = 'block';
	var row = getThisRow(obj);
	var id = getRowIndex(row);
	document.getElementById('_s_editRowId').value = id;

	stockDetailsDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	document.getElementById("_d_item_name").textContent = getIndexedFormElement(stockConsumptionDetailsForm, '_s_item_name', id).value;
	document.getElementById("_d_batch_no").textContent = getIndexedFormElement(stockConsumptionDetailsForm, '_s_batch_no', id).value;
	document.getElementById("_d_store_qty").textContent = getIndexedFormElement(stockConsumptionDetailsForm, '_s_store_qty', id).value;
	document.getElementById("_d_consumed_qty").value = getIndexedFormElement(stockConsumptionDetailsForm, '_s_consumed_qty', id).value;
	document.getElementById("_d_balance_qty").value = getIndexedFormElement(stockConsumptionDetailsForm, '_s_balance_qty', id).value;
	stockDetailsDialog.show();
	document.getElementById("_d_consumed_qty").focus();
	return false;
}

function openConsumptionDialogBox(obj) {
	d_edited = false;
	document.getElementById('consumption_details_dialog').style.display = 'block';
	var row = getThisRow(obj);
	var id = getRowIndex(row);
	document.getElementById('_c_editRowId').value = id;

	stockConsumptionDetailsDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	document.getElementById("_d1_item_name").textContent = getIndexedFormElement(stockConsumptionDetailsForm, '_c_item_name', id).value;
	document.getElementById("_d1_batch_no").textContent = getIndexedFormElement(stockConsumptionDetailsForm, '_c_batch_no', id).value;
	document.getElementById("_d1_store_qty").textContent = getIndexedFormElement(stockConsumptionDetailsForm, '_c_store_qty', id).value;
	document.getElementById("_d1_consumed_qty").value = getIndexedFormElement(stockConsumptionDetailsForm, '_c_consumed_qty', id).value;
	document.getElementById("_d1_balance_qty").value = getIndexedFormElement(stockConsumptionDetailsForm, '_c_balance_qty', id).value;
	stockConsumptionDetailsDialog.show();
	document.getElementById("_d1_consumed_qty").focus();
	return false;
}

function insertRecord() {
	d_edited = false;
	var table = "storeDetailsTable";
	var id = document.getElementById('_s_editRowId').value;
	var totalQty =  getIndexedFormElement(stockConsumptionDetailsForm, '_s_store_qty', id).value;
	formatAmountObj(document.getElementById('_d_consumed_qty'), true);
	formatAmountObj(document.getElementById('_d_balance_qty'), true);
	var consumedQty =  document.getElementById('_d_consumed_qty').value;
	var balanceQty = document.getElementById('_d_balance_qty').value;

	var p_consumedQty = getIndexedFormElement(stockConsumptionDetailsForm, '_s_consumed_qty', id).value;
	var p_balanceQty = getIndexedFormElement(stockConsumptionDetailsForm, '_s_balance_qty', id).value;

	if((parseFloat(p_consumedQty) != parseFloat(consumedQty))) {
		consumedQty = document.getElementById('_d_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	} else if((parseFloat(p_balanceQty) != balanceQty)) {
		balanceQty = document.getElementById('_d_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}

	if (validateStockQty(totalQty, consumedQty, balanceQty)) {
		var row =  getRow(table,id);
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_s_consumed_qty", consumedQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_s_balance_qty", balanceQty);
		this.cancel();
		if(d_edited) {
			document.getElementById('_s_select_item'+id).checked = true;
			document.getElementById('_s_is_insert'+id).value = "Y";
			row.className = 'rowbgToolbar';
		}
		if (parseInt(id) != document.getElementById('storeDetailsTable').rows.length-2) {
			openStockDialogBox(document.getElementsByName('_s_editIcon')[parseInt(id)+1]);
		}
	}
}

function insertRecord1() {
	d_edited = false;
	var table = "stcokConsumptionDetailsTable";
	var id = document.getElementById('_c_editRowId').value;
	var totalQty =  getIndexedFormElement(stockConsumptionDetailsForm, '_c_store_qty', id).value;
	formatAmountObj(document.getElementById('_d1_consumed_qty'), true);
	formatAmountObj(document.getElementById('_d1_balance_qty'), true);
	var consumedQty =  document.getElementById('_d1_consumed_qty').value;
	var balanceQty = document.getElementById('_d1_balance_qty').value;

	var p_consumedQty = getIndexedFormElement(stockConsumptionDetailsForm, '_c_consumed_qty', id).value;
	var p_balanceQty = getIndexedFormElement(stockConsumptionDetailsForm, '_c_balance_qty', id).value;

	if((parseFloat(p_consumedQty) != parseFloat(consumedQty))) {
		consumedQty = document.getElementById('_d1_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	} else if((parseFloat(p_balanceQty) != balanceQty)) {
		balanceQty = document.getElementById('_d1_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}
	if (validateStockQty(totalQty, consumedQty, balanceQty)) {
		var row =  getRow(table,id);
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_c_consumed_qty", consumedQty);
		setHiddenValue(stockConsumptionDetailsForm, id, "_c_balance_qty", balanceQty);
		this.cancel();
		if(d_edited) {
			document.getElementById('_c_select_item'+id).checked = true;
			document.getElementById('_c_is_update'+id).value = "Y";
			row.className = 'rowbgToolbar';
		}
		if (parseInt(id) != document.getElementById('stcokConsumptionDetailsTable').rows.length-2) {
			openConsumptionDialogBox(document.getElementsByName('_c_editIcon')[parseInt(id)+1]);
		}
	}
}

/*function calculate(obj,action) {
	d_edited = false;
	var table = "storeDetailsTable";
	var id = document.getElementById('editRowId').value;
	var totalQty =  getIndexedFormElement(stockDetailsForm, 's_store_qty', id).value;
	var consumedQty = parseInt("0").toFixed(decDigits);
	var balanceQty =  parseInt("0").toFixed(decDigits);
	var row =  getRow(table,id);
	if(!empty(obj.value) && action == 'consumed') {
		consumedQty =  document.getElementById('d_consumed_qty').value;
		balanceQty = (totalQty - consumedQty).toFixed(decDigits);
		d_edited = true;
	}

	if(!empty(obj.value) && action == 'balance') {
		balanceQty =  document.getElementById('d_balance_qty').value;
		consumedQty = (totalQty - balanceQty).toFixed(decDigits);
		d_edited = true;
	}

	if(validateStockQty(totalQty,consumedQty,balanceQty)) {
		setNodeText(row.cells[3], consumedQty);
		setNodeText(row.cells[4], balanceQty);
		setHiddenValue(stockDetailsForm, id, "s_consumed_qty", consumedQty);
		setHiddenValue(stockDetailsForm, id, "s_balance_qty", balanceQty);
		document.getElementById('d_consumed_qty').value = consumedQty;
		document.getElementById('d_balance_qty').value = balanceQty;
	}
}*/

function validateStockQty(totalQty,consumedQty,balanceQty) {
	if(empty(consumedQty) && empty(balanceQty)) {
		showMessage("js.stores.mgmt.enterconsumed.balanceqty");
		document.getElementById('_d_consumed_qty').focus();
		return false;
	} else if (parseFloat(consumedQty) > parseFloat(totalQty)) {
		showMessage("js.stores.mgmt.consumedqty.notgreater.storeqty");
		document.getElementById('_d_consumed_qty').focus();
		return false;
	} else if(parseFloat(balanceQty) > parseFloat(totalQty)) {
		showMessage("js.stores.mgmt.balannceqty.notgreater.storeqty");
		document.getElementById('_d_balance_qty').focus();
		return false;
	}
	return true;
}

function validateCancelltion() {
/*	var checkBoxes = document.stockConsumptionDetailsForm._c_select_item;
	var  index = 0;
	if(checkBoxes && checkBoxes.length) {
		for(var i=0;i<checkBoxes.length;i++) {
			if(document.getElementById('_c_select_item'+i).checked) {
				index++;
				break;
			}
		}
	} else if(document.stockConsumptionDetailsForm._c_select_item) {
		if(document.stockConsumptionDetailsForm._c_select_item.checked)
			index++;
	}

	if(index < 1) {
		alert("there is no record to cancel."+"\n"+"please add one or more records to cancel.");
		return false;
	}*/

	var ok = confirm("stock consumption will be cancelled."+"\n"+"do you want to continue ?");
	if(ok) {
		document.stockConsumptionDetailsForm._method.value = 'cancelConsumptionTransaction';
		document.stockConsumptionDetailsForm.submit();
	}
}

function validateFinalization() {
	var checkBoxes = document.stockConsumptionDetailsForm._c_select_item;
	var index = 0;
	if(checkBoxes && checkBoxes.length) {
		for(var i=0;i<checkBoxes.length;i++) {
			if(document.getElementById('_c_select_item'+i).checked) {
				index++;
				break;
			}
		}
	} else if(document.stockConsumptionDetailsForm._c_select_item) {
		if(document.stockConsumptionDetailsForm._c_select_item.checked)
			index++;
	}

	if(index < 1) {
		showMessage("js.stores.mgmt.checkstockconsumptions.finalize");
		return false;
	}

	var ok = confirm("consumed qty will be reduced from the stock. "+"\n"+ "do you want to continue ?");
	if(ok) {
		document.stockConsumptionDetailsForm._method.value = 'finalizeConsumptionDetails';
		document.stockConsumptionDetailsForm.submit();
	}
}
