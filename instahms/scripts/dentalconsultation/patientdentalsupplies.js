function init() {
	initOrderDialog();
	initDoctorAutocomplete();
	hideCompletedOrders(document.getElementById('hide_completed'))
}

var dAutoComp = null;
function initDoctorAutocomplete() {
	var ds = new YAHOO.util.LocalDataSource({result: doctors});
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				],
	};

	dAutoComp = new YAHOO.widget.AutoComplete("d_ordered_by_name", "orderby_container", ds);
	dAutoComp.minQueryLength = 1;
	dAutoComp.animVert = false;
	dAutoComp.maxResultsDisplayed = 20;
	dAutoComp.resultTypeList = false;
	dAutoComp.forceSelection = true;
	dAutoComp.itemSelectEvent.subscribe(selectDoctor);
	dAutoComp.selectionEnforceEvent.subscribe(clearDoctor);

}

function selectDoctor(sType, oArgs) {
	document.getElementById('d_ordered_by').value = oArgs[2].doctor_id;
}

function clearDoctor(sType, oArgs) {
	document.getElementById('d_ordered_by').value = '';
}


var receivedByAutoComp = {};
function initReceivedByAutoComplete(id) {
	var ds = new YAHOO.util.LocalDataSource({result: doctors});
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				],
	};

	rAutoComp = new YAHOO.widget.AutoComplete("d_received_by_name_"+id, "d_received_by_container_"+id, ds);
	rAutoComp.minQueryLength = 1;
	rAutoComp.animVert = false;
	rAutoComp.maxResultsDisplayed = 20;
	rAutoComp.resultTypeList = false;
	rAutoComp.forceSelection = true;
	rAutoComp.itemSelectEvent.subscribe(function(id) {
		return function(sType, oArgs) {
			selectReceivedBy(sType, oArgs, id);
		}
	}(id));
	rAutoComp.selectionEnforceEvent.subscribe(function(id) {
		return function(sType, oArgs) {
			clearReceivedBy(sType, oArgs, id);
		}
	}(id));
	receivedByAutoComp[id] = rAutoComp;
}

function selectReceivedBy(sType, oArgs, id) {
	document.getElementsByName('d_received_by')[id].value = oArgs[2].doctor_id;
}

function clearReceivedBy(sType, oArgs, id) {
	document.getElementsByName('d_received_by')[id].value = '';
}

function setItemRate(obj){
	var row = getThisRow(obj);
	if (obj.value == '') {
		getElementByName(row, 'd_unit_rate').value = '';
		getElementByName(row, 'd_vat_perc').value = '';
	} else {
		var record = findInList2(gItems, "supplier_id", document.getElementById('d_supplier_id').value, "item_id", obj.value);
		getElementByName(row, 'd_unit_rate').value = record.unit_rate;
		getElementByName(row, 'd_vat_perc').value = record.vat_perc;
	}
}

function hideCompletedOrders(obj) {
	var table = document.getElementById('orderDetails');
	if (obj.checked) {
		for (var i=1; i<table.rows.length-2; ) {
			var orderIndex = getElementByName(table.rows[i], "h_order_index");
			var suppliesOrderStatus = getElementByName(table.rows[i], "h_supplies_order_status");
			if (suppliesOrderStatus && suppliesOrderStatus.value == 'C') {
				table.rows[i++].style.display = 'none';
				var itemId = getElementByName(table.rows[i], "h_"+orderIndex.value+"_item_id");
				while(itemId) {
					table.rows[i++].style.display = 'none';
					itemId = getElementByName(table.rows[i], "h_"+orderIndex.value+"_item_id");
				}

			} else {
				i++;
			}
		}
	} else {
		for (var i=1; i<table.rows.length-2; i++) {
			table.rows[i].style.display = 'table-row';
		}
	}
}


var addItemDialog = null;
function initOrderDialog() {
	var dialogDiv = document.getElementById("addItemDialog");
	dialogDiv.style.display = 'block';
	addItemDialog = new YAHOO.widget.Dialog("addItemDialog",
			{	width:"900px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Update', 'click', addToTable, addItemDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleItemCancel, addItemDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleItemCancel,
	                                                scope:addItemDialog,
	                                                correctScope:true } );
	addItemDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addItemDialog.render();
}

function handleItemCancel() {
	addItemDialog.cancel();
}

function cancelSupplier(obj) {
	var row =  getThisRow(obj);
	var orderIndex = getElementByName(row, 'h_order_index');
	var suppliesOrderId = getElementByName(row, 'h_supplies_order_id').value;
	if (empty(suppliesOrderId)) {
		var table = document.getElementById('orderDetails');
		table.deleteRow(row.rowIndex);

		// gets the template row and the normal row.
		var items = document.getElementsByName('h_'+orderIndex.value+"_item_id");
		for (var k=0; k<items.length; ) {
			if (items[k].value != '') {
				table.deleteRow(getThisRow(items[k]).rowIndex);
			} else {
				// dont delete the template row.
				k++;
			}
		}
	} else {
		var cancelled = getElementByName(row, 'h_delete').value == 'false';
		getElementByName(row, 'h_delete').value = cancelled;
		var trashImgs = row.cells[TRASH].getElementsByTagName("img");
		var trashSrc;
		if (cancelled) {
			row.className = 'edited';
			trashSrc = cpath + '/icons/undo_delete.gif';
		} else {
			row.className = '';
			trashSrc = cpath + '/icons/delete.gif';
		}
		if (trashImgs && trashImgs[0])
			trashImgs[0].src = trashSrc;
	}

}

function showAddOrderDialog(obj) {
	addItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	addItemDialog.show();
	document.suppliesForm.editRowId.value = 0;

	document.getElementById('d_supplier_id').value = '';
	document.getElementById('d_ordered_by').value = '';
	document.getElementById('d_ordered_by_name').value = '';
	document.getElementById('d_ordered_date').value = formatDate(new Date());
	document.getElementById('d_remarks').value = '';
	document.getElementById('d_ordered_status').value = 'O';
	if (gMax_centers_inc_default > 1) {
		if (gCenterId == 0)
			document.getElementById('d_center_id').value = '';
		else
			document.getElementById('d_centerName').textContent = gCenterName;
	}

	deleteItemsInDialog();
}

function deleteItemsInDialog() {
	var itemsTable = document.getElementById("item_order_details");
	for (var k=1; k<itemsTable.rows.length-1; ) {
		itemsTable.deleteRow(k);
	}
}

function showEditOrderDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	addItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	addItemDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');

	var orderIndexEl = getElementByName(row, "h_order_index");
	document.suppliesForm.editRowId.value = orderIndexEl.value;

	var supplierId = getElementByName(row, 'h_supplier_id').value
	document.getElementById('d_supplier_id').value = supplierId;
	document.getElementById('d_ordered_by').value = getElementByName(row, 'h_ordered_by').value;
	document.getElementById('d_treatment_name').value = getElementByName(row, 'h_treatment_name').value;
	document.getElementById('d_ordered_by_name').value = getElementByName(row, 'h_ordered_by_name').value;
	document.getElementById('d_ordered_date').value = getElementByName(row, 'h_ordered_date').value;
	document.getElementById('d_remarks').value = getElementByName(row, 'h_remarks').value;
	document.getElementById('d_ordered_status').value = getElementByName(row, 'h_supplies_order_status').value;
	if (gMax_centers_inc_default > 1) {
		document.getElementById('d_center_id').value = getElementByName(row, 'h_center_id').value;
		if (gCenterId > 0)
			document.getElementById('d_centerName').textContent = getElementByName(row, 'h_center_name').value;
	}

	if (dAutoComp._elTextbox.value != '') {
		dAutoComp._bItemSelected = true;
		dAutoComp._sInitInputValue = dAutoComp._elTextbox.value;
	}

	deleteItemsInDialog();
	var items = document.getElementsByName('h_'+orderIndexEl.value+"_item_id");
	for (var i=0; i<items.length; i++) {
		if (items[i].value != '') {
			addRowInline();

			var itemIdEl = document.getElementsByName('d_item_id')[i];
			itemIdEl.value = items[i].value;
			document.getElementsByName('d_item_qty')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_item_qty")[i].value;
			document.getElementsByName('d_received_qty')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_received_qty")[i].value;
			document.getElementsByName('d_received_by')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_received_by")[i].value;
			document.getElementsByName('d_received_by_name')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_received_by_name")[i].value;
			document.getElementsByName('d_received_date')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_received_date")[i].value;
			document.getElementsByName('d_unit_rate')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_unit_rate")[i].value;
			document.getElementsByName('d_vat_perc')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_vat_perc")[i].value;
			document.getElementsByName('d_item_remarks')[i].value = document.getElementsByName('h_'+orderIndexEl.value+"_item_remarks")[i].value;
			var shade = document.getElementsByName('d_shade')[i];
			shade.value = document.getElementsByName('h_'+orderIndexEl.value+"_shade_id")[i].value;

			if (receivedByAutoComp[i]._elTextbox.value != '') {
				receivedByAutoComp[i]._bItemSelected = true;
				receivedByAutoComp[i]._sInitInputValue = dAutoComp._elTextbox.value;
			}
			if (itemIdEl.value == '') {
				// populating the item which is inactive in rate master.
				var record = findInList2(gItems, "item_id", items[i].value, "supplier_id", supplierId);
				var len = itemIdEl.options.length;
				itemIdEl.options.length = len+1;
				itemIdEl.options[len].value = record.item_id;
				itemIdEl.options[len].text = record.item_name;

				itemIdEl.value = record.item_id;
			}
			if (shade.value == '' && document.getElementsByName('h_'+orderIndexEl.value+"_shade_id")[i].value != '') {
				// populating the shade which is inactive in shades master.
				var record = findInList(gShades, "shade_id", document.getElementsByName('h_'+orderIndexEl.value+"_shade_id")[i].value);
				var len = shade.options.length;
				shade.options.length = len+1;
				shade.options[len].value = record.shade_id;
				shade.options[len].text = record.shade_name;

				shade.value = record.shade_id;
			}
		}
	}
}

function setReceivedDate(obj) {
	var regExp = new RegExp(/^[0-9]+$/);
	if (obj.value != '') {
		var row = getThisRow(obj);

		var received_date = getElementByName(row, "d_received_date");
		if (received_date.value == '') {
			received_date.value = formatDate(new Date());
		}
	}
}

function addRowInline() {
	var itemsTable = document.getElementById("item_order_details");
	var id = itemsTable.rows.length-2;
   	var templateRow = itemsTable.rows[id+1];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	itemsTable.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

	// calendar wont work with clone node, hence used javscript utility method.
   	row.cells[5].innerHTML = getDateWidget('d_received_date', 'd_received_date_'+id, null, '', '', true, true, '', cpath);
   	makePopupCalendar('d_received_date_'+id);

   	row.cells[4].innerHTML =
   	'<div id="d_received_by_ac_'+id+'" style="padding-bottom: 20px; width: 10em">' +
   		'<input type="text" id="d_received_by_name_'+id+'" name="d_received_by_name"/>' +
   		'<input type="hidden" name="d_received_by" value="">' +
   		'<div id="d_received_by_container_'+id+'" style="width: 200px">' +
   	'</div>';
   	initReceivedByAutoComplete(id);

	var itemEl = getElementByName(row, "d_item_id");
	var supplierId = document.getElementById('d_supplier_id').value;
	itemEl.length = 1;
	for (var k=0; k<gItems.length; k++) {
		if (supplierId == gItems[k].supplier_id && gItems[k].rate_status == 'A') {
			var len = itemEl.length;
			itemEl.length = len+1;
			itemEl.options[len].value = gItems[k].item_id;
			itemEl.options[len].text = gItems[k].item_name;
		}
	}
	var shadeEl = getElementByName(row, 'd_shade');
	shadeEl.length =1;
	for (var k=0; k<gShades.length; k++) {
		if (gShades[k].status == 'A') {
			var len = shadeEl.length;
			shadeEl.length = len+1;
			shadeEl.options[len].value = gShades[k].shade_id;
			shadeEl.options[len].text = gShades[k].shade_name;
		}
	}
   	return row;
}

var colIndex = 0;
var SLNO = colIndex++, ORDER_DATE = colIndex++,TREATMENT_NAME = colIndex++, SUPPLIER = colIndex++,
	ORDER_STATUS = colIndex++, ITEM = colIndex++, ORDER_QTY = colIndex++, RECEIVED_QTY = colIndex++,
	RECEIVED_DATE = colIndex++, TRASH = colIndex++, EDIT = colIndex++, PRINT = colIndex++;
var gFirstNewItem = true;
var rowsAdded = 1;
function addToTable() {
	var row = null;
	var ordered_date = document.getElementById('d_ordered_date').value;
	var treatment_name = document.getElementById('d_treatment_name').value;
	var order_by_name = document.getElementById('d_ordered_by_name').value;
	var order_by_id = document.getElementById('d_ordered_by').value;
	var ordered_status_name = document.getElementById('d_ordered_status').options[document.getElementById('d_ordered_status').selectedIndex].text;
	var ordered_status = document.getElementById('d_ordered_status').options[document.getElementById('d_ordered_status').selectedIndex].value;
	var supplier_name = document.getElementById('d_supplier_id').options[document.getElementById('d_supplier_id').selectedIndex].text;
	var supplier_id = document.getElementById('d_supplier_id').options[document.getElementById('d_supplier_id').selectedIndex].value;
	var remarks = document.getElementById('d_remarks').value;
	var unit_rate = document.getElementById('d_unit_rate').value;

	if (supplier_id == '') {
		alert("Please select the supplier");
		document.getElementById('d_supplier_id').focus();
		return false;
	}
	if (order_by_name == '') {
		alert("Please enter the Ordered by");
		document.getElementById('d_ordered_by_name').focus();
		return false;
	}
	if (ordered_date == '') {
		alert("Please enter the Ordered Date.");
		document.getElementById('d_ordered_date').focus();
		return false;
	}
	if (gMax_centers_inc_default > 1 && document.getElementById('d_center_id').value == '') {
		alert("Please select the center.");
		document.getElementById('d_center_id').focus();
		return false;
	}
	var itemrows = document.getElementsByName('d_item_id');
	for (var k=0; k<itemrows.length-1; k++) {
		var itemName = document.getElementsByName('d_item_id')[k].value;
		var itemQty = document.getElementsByName('d_item_qty')[k].value;
		var received_by = document.getElementsByName('d_received_by')[k].value;
		var received_by_name = document.getElementsByName('d_received_by_name')[k].value;
		var received_date = document.getElementsByName('d_received_date')[k].value;
		var received_qty = document.getElementsByName('d_received_qty')[k].value;
		var item_remarks = document.getElementsByName('d_item_remarks')[k].value;
		var regExp = new RegExp(/^[0-9]+$/);
		if (itemQty == '' || parseInt(itemQty) == 0) {
			document.getElementsByName('d_item_qty')[k].focus();
			alert("Please enter the Order Qty. It should be greater than zero.");
			return false;
		}
		if (itemQty != '' && !regExp.test(itemQty)) {
			alert("Item Qty should be a whole number.");
			document.getElementsByName('d_item_qty')[k].focus();
			return false;
		}
		if (received_qty != '') {
			if (parseInt(received_qty) == 0) {
				alert("Received Qty should be greater than zero");
				document.getElementsByName('d_received_qty')[k].focus();
				return false;
			}
			if (!regExp.test(received_qty)) {
				alert("Received Qty should be a whole number");
				document.getElementsByName('d_received_qty')[k].focus();
				return false;
			}
		}
		itemQty = empty(itemQty) ? 1 : itemQty;
		if (itemQty != '' && received_qty != '' && parseInt(received_qty) > parseInt(itemQty)) {
			alert("Received Qty should not be greater than Order Qty");
			document.getElementsByName('d_received_qty')[k].focus();
			return false;
		}
		if (itemName == '' &&
			(itemQty != '' || received || received_by != '' || received_date != '')) {
			alert("Please select the Item Name");
			document.getElementsByName('d_item_id')[k].focus();
			return false;
		}
		if (item_remarks == '') {
			alert("Please enter the remarks.");
			document.getElementsByName('d_item_remarks')[k].focus();
			return false;
		}
		if (received_qty != '') {
			if (received_by == '') {
				alert("Please enter the Received by.");
				document.getElementsByName('d_received_by_name')[k].focus();
				return false;
			}
			if (received_date == '') {
				alert("Please enter the Received Date.");
				document.getElementsByName('d_received_date')[k].focus();
				return false;
			}
		} else {
			if (received_by != '' || received_date != '') {
				alert("Please enter the Received Qty");
				document.getElementsByName('d_received_qty')[k].focus();
				return false;
			}
		}
	}
	var table = document.getElementById("orderDetails");
	var orderIndex =  document.suppliesForm.editRowId.value;
	if (orderIndex == 0) {
		// add the patient order main row.
	   	var templateRow = table.rows[getTemplateRow('orderDetails')];
		row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
		orderIndex = parseInt(getElementByName(row, "h_order_index").value, 10);
		if (!gFirstNewItem) {
			orderIndex = orderIndex + rowsAdded++;
		}
		gFirstNewItem = false;
	   	row.id = "orderRow" + orderIndex;
   	} else {
   		var items = document.getElementsByName('h_'+orderIndex+'_item_id');
   		for (var k=0; k<items.length; ) {
   			if (items[k].value == '') {
   				// ignore the template row.
   				k++;
   			} else {
   				var rowObj = getThisRow(items[k]);
   				table.deleteRow(rowObj.rowIndex);
   			}
   		}
		row = getThisRow(document.getElementsByName('h_supplier_id')[orderIndex-1]);
		YAHOO.util.Dom.removeClass(row, 'editing');
   	}
	setNodeText(row.cells[SLNO], orderIndex);
	setNodeText(row.cells[ORDER_DATE], ordered_date);
	setNodeText(row.cells[TREATMENT_NAME], treatment_name);
	setNodeText(row.cells[SUPPLIER], supplier_name);
	setNodeText(row.cells[ORDER_STATUS], ordered_status_name);

	getElementByName(row, "h_ordered_date").value = ordered_date;
	getElementByName(row, "h_ordered_by").value = order_by_id;
	getElementByName(row, "h_supplier_id").value = supplier_id;
	getElementByName(row, "h_supplies_order_status").value = ordered_status;
	getElementByName(row, "h_remarks").value =  remarks;
	getElementByName(row, "h_ordered_by_name").value = order_by_name;
	getElementByName(row, "h_order_index").value = orderIndex;
	getElementByName(row, "h_center_id").value = gMax_centers_inc_default > 1 ? document.getElementById('d_center_id').value : 0;
	if (gMax_centers_inc_default > 1) {
		if (document.getElementById('d_center_id').type == 'select-one') {
			getElementByName(row, "h_center_name").value = document.getElementById('d_center_id').options[document.getElementById('d_center_id').selectedIndex].text;
		}
	}

	var noOfOrders = document.getElementById('noOfOrders').value;
	for (var k=0; k<itemrows.length-1; k++) {
		if (itemrows[k].value != '') {
			var itemTemplateRow = table.rows[getNumCharges('orderDetails')+2];
			var itemrow = itemTemplateRow.cloneNode(true);
			itemrow.style.display = '';
			YAHOO.util.Dom.insertAfter(itemrow, row)

			var itemObj = document.getElementsByName('d_item_id')[k];
			var item = itemObj.options[itemObj.selectedIndex].text;
			var item_id = itemObj.options[itemObj.selectedIndex].value;
			var item_qty = document.getElementsByName('d_item_qty')[k].value;
			item_qty = item_qty == '' ? 1 : item_qty;
			var received_qty = document.getElementsByName('d_received_qty')[k].value
			var received_by = document.getElementsByName('d_received_by')[k].value;
			var received_by_name = document.getElementsByName('d_received_by_name')[k].value;
			var received_date = document.getElementsByName('d_received_date')[k].value;
			var unit_rate = document.getElementsByName('d_unit_rate')[k].value;
			var vat_perc = document.getElementsByName('d_vat_perc')[k].value;
			var item_remarks = document.getElementsByName('d_item_remarks')[k].value;
			var shadeObj = document.getElementsByName('d_shade')[k];
			var shade_id = shadeObj.options[shadeObj.options.selectedIndex].value;
			var shade = shadeObj.options[shadeObj.options.selectedIndex].text;

			setNodeText(itemrow.cells[ITEM], item, 20);
			setNodeText(itemrow.cells[ORDER_QTY], item_qty);
			setNodeText(itemrow.cells[RECEIVED_QTY], received_qty);
			setNodeText(itemrow.cells[RECEIVED_DATE], received_date);

			getElementByName(itemrow, 'h_'+noOfOrders+'_item_id').setAttribute("name", "h_"+orderIndex+"_item_id");
			getElementByName(itemrow, 'h_'+noOfOrders+'_item_qty').setAttribute("name", "h_"+orderIndex+"_item_qty");
			getElementByName(itemrow, 'h_'+noOfOrders+'_received_qty').setAttribute("name", "h_"+orderIndex+"_received_qty");
			getElementByName(itemrow, 'h_'+noOfOrders+'_received_by').setAttribute("name", "h_"+orderIndex+"_received_by");
			getElementByName(itemrow, 'h_'+noOfOrders+'_received_by_name').setAttribute("name", "h_"+orderIndex+"_received_by_name");
			getElementByName(itemrow, 'h_'+noOfOrders+'_received_date').setAttribute("name", "h_"+orderIndex+"_received_date");
			getElementByName(itemrow, 'h_'+noOfOrders+'_unit_rate').setAttribute("name", "h_"+orderIndex+"_unit_rate");
			getElementByName(itemrow, 'h_'+noOfOrders+'_vat_perc').setAttribute("name", "h_"+orderIndex+"_vat_perc");
			getElementByName(itemrow, 'h_'+noOfOrders+'_item_remarks').setAttribute("name", "h_"+orderIndex+"_item_remarks");
			getElementByName(itemrow, 'h_'+noOfOrders+'_shade_id').setAttribute("name", "h_"+orderIndex+"_shade_id");

			getElementByName(itemrow, "h_"+orderIndex+"_item_id").value = item_id;
			getElementByName(itemrow, "h_"+orderIndex+"_item_qty").value = item_qty;
			getElementByName(itemrow, "h_"+orderIndex+"_received_qty").value = received_qty;
			getElementByName(itemrow, "h_"+orderIndex+"_received_by").value = received_by;
			getElementByName(itemrow, "h_"+orderIndex+"_received_by_name").value = received_by_name;
			getElementByName(itemrow, "h_"+orderIndex+"_received_date").value = received_date;
			getElementByName(itemrow, "h_"+orderIndex+"_unit_rate").value = unit_rate;
			getElementByName(itemrow, "h_"+orderIndex+"_vat_perc").value = vat_perc;
			getElementByName(itemrow, "h_"+orderIndex+"_item_remarks").value = item_remarks;
			getElementByName(itemrow, "h_"+orderIndex+"_shade_id").value = shade_id;

		}
	}
	addItemDialog.cancel();
}

function cancelItem(obj) {
	var row = getThisRow(obj);
	var table = document.getElementById('item_order_details');
	table.deleteRow(row.rowIndex);
}

function onSave(){
	document.suppliesForm.submit();
	return true;
}

function getNumCharges(tableId) {
	// header, two hidden template rows: totally 3 extra
	return document.getElementById(tableId).rows.length-3;
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

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.suppliesForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.suppliesForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}
function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.suppliesForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}