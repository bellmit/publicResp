
var dialog = {};
var i=0;

ITEM_COL = i++; INDENT_QTY_COL = i++;DELIVERED_QTY_COL = i++;
ACCEPTED_QTY_COL = i++; REJECTED_QTY_COL = i++; PKG_SIZE_COL=i++;ISSUE_UOM_COL=i++; STATUS_COL = i++;
BATCH_NP_COL=i++;INDENT_EDIT_COL=i++;

function initDialog() {
	var rowLen = document.getElementById("indentItemListTab").rows.length;
	dialog.length = rowLen-1;
	for(var i=0;i<rowLen-1;i++) {
		var dialogid = "dialog"+i;
		dialog[i] = new YAHOO.widget.Dialog(dialogid,
			{
				width:"475px",
				context : ["indentItemListTab", "tl", "bl"],
				visible:false,
				modal:true,
				close: false,
				constraintoviewport:true
		});

		YAHOO.util.Event.addListener(document.getElementsByName("okButton"+i), "click",
				handleSubmit, dialog[i], true);
		YAHOO.util.Event.addListener(document.getElementsByName("nextButton"+i), "click",
				handleNextIndent, dialog[i], true);
		YAHOO.util.Event.addListener(document.getElementsByName("prevButton"+i), "click",
				handlePreviousIndent, dialog[i], true);

		dialog[i].render();
		document.getElementById(dialogid).style.display = 'block';
	}
}

function handleNextIndent() {
	handleNextPrevious(1);
}

function handlePreviousIndent() {
	handleNextPrevious(-1);
}

function handleNextPrevious(incr) {
	if (!handleSubmit()) {
		return false;
	}

	var id = document.getElementById("dialogId").value;
	var nextId = parseInt(id) + incr;

	if (nextId >=0 && nextId < getNumIndentRows())
		showIndentifierDialog(nextId);
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndentRow(i) {
	i = parseInt(i);
	var table = document.getElementById("indentItemListTab");
	return table.rows[i+1];
}

function getRow(i) {
	i = parseInt(i);
	var table = document.getElementById("indentItemListTab");
	return table.rows[i];
}

function getNumIndentRows() {
	var table = document.getElementById("indentItemListTab");
	return table.rows.length - 1;
}

var origRowClass = '';
function showIndentifierDialog(id) {
	var button = document.getElementById("dialogShow"+id);
	dialog[id].cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	var row = getIndentRow(id);
	origRowClass = row.className;
	row.className = 'editing';
	dialog[id].show();
}

function handleSubmit() {

	var dialogId = document.getElementById("dialogId").value;

	var processelmts = document.getElementsByName("process"+dialogId);
	var identifierelmts = document.getElementsByName("batch_no"+dialogId);
	var qtyrecelmts = document.getElementsByName("item_qty_rec"+dialogId);
	var qtyrejelmts = document.getElementsByName("item_qty_rej"+dialogId);
	var qtydelvelmts = document.getElementsByName("item_qty_del"+dialogId);

	var identification = document.mainform["identification"+dialogId].value;

	var totalAccepted = 0;
	var totalRejected = 0;
	var acceptedQty = 0;
	var rejectedQty = 0;
	var batch = '';
	var anyProcessed = false;

	for (var i=0; i<processelmts.length; i++) {
		if (processelmts[i].value != "Y")
			continue;

		if (!isValidNumber(qtyrecelmts[i], allowDecimalsForQty)) return false;

		if ('' == qtyrecelmts[i].value){
			qtyrecelmts[i].value = 0;
		}

		var recdQty = getElementAmount(qtyrecelmts[i]);
		var delvQty = getElementAmount(qtydelvelmts[i]);

		if (recdQty > delvQty) {
			showMessage("js.stores.mgmt.qtyreceived.more.delieveredqty");
			qtyrecelmts[i].focus();
			return false;
		}

		totalAccepted += recdQty;
		if(receiveStatus != 'F')
			totalRejected += delvQty - recdQty;
		anyProcessed = true;
		batch =  batch + identifierelmts[i].value+"("+qtyrecelmts[i].value+")"+",";
	}

	acceptedQty = parseFloat(document.getElementById("total_item_qty_recd"+dialogId).value);
	rejectedQty = parseFloat(document.getElementById("total_item_qty_rej"+dialogId).value);

	document.getElementById("batchlbl"+dialogId).innerHTML = batch;
	document.getElementById("itemrecdqtylbl"+dialogId).innerHTML = formatAmountValue(acceptedQty+totalAccepted, true);
	document.getElementById("itemrejqtylbl"+dialogId).innerHTML = formatAmountValue(rejectedQty+totalRejected, true);
	document.getElementById('indentSelect'+dialogId).value = anyProcessed;

	var row = getIndentRow(dialogId);
	row.className = anyProcessed ? 'edited' : '';

	dialog[dialogId].hide();
	return true;
}

function updateSerialItemCount(i) {
	var processObj = document.getElementsByName("process"+i);
	var itemCheckObj = document.getElementsByName("item_check"+i);
	var recdQty = 0;
	var rejQty = 0;

	for (var j=0; j<processObj.length; j++) {
		if (processObj[j].value == 'Y' && itemCheckObj[j].checked)
				recdQty += 1;
		else
			rejQty += 1;
	}

	document.mainform["accept_count"+i].value = recdQty;
	if(receiveStatus != 'F')
		document.mainform["reject_count"+i].value = rejQty;
}

/*
 * Called on clicking Select for a serial item
 */
function onSerialSelectItem(obj, i, j) {
	selectSerialItem(obj, i, j);
	updateSerialItemCount(i);
}

function selectSerialItem(chkBox, i, j) {
	var row = getThisRow(chkBox);
	var qtyRecdObj = getElementByName(row, "item_qty_rec"+i);
	var qtyRejObj = getElementByName(row, "item_qty_rej"+i);

	if (chkBox.checked) {
		qtyRecdObj.value  = "1";
		qtyRejObj.value  = "0";

	} else {
		qtyRecdObj.value  = "0";
		qtyRejObj.value  = "1";
	}
}

function onSerialSelectAll(allObj, i) {
	var elmts = document.getElementsByName("process_check"+i);
	for (var j=0;j<elmts.length;j++) {
		elmts[j].checked = allObj.checked;
		onClickProcess(elmts[j], i, j);
	}
	updateSerialItemCount(i);
}

function onClickProcess(obj, i, j) {

	doProcess(obj, i, j);
	var ident = document.mainform["identification"+i].value;
	if (ident == 'S') {
		updateSerialItemCount(i);
	}
}

function doProcess(obj, i, j) {
	var row = getThisRow(obj);
	var cellObjOfQtyRecd = getElementByName(row , "item_qty_rec"+i);
	var hProcess = getElementByName(row, "process"+i);
	var qtyDeliveredObj = getElementByName(row, "item_qty_del"+i);
	var itemCheckObj = getElementByName(row,"item_check"+i);
	var qtyRecdObj = getElementByName(row, "item_qty_rec"+i);
	var qtyRejObj = getElementByName(row, "item_qty_rej"+i);
	var ident = document.mainform["identification"+i].value;

	hProcess.value =  (obj.checked) ? "Y" : "N";

	if (obj.checked) {
		if (ident != 'S') {
			if(receiveStatus != 'F')
				cellObjOfQtyRecd.readOnly = false;
		} else {
			if(receiveStatus != 'F')
				itemCheckObj.disabled = false;
		}

		var qtyDelivered = getElementAmount(qtyDeliveredObj);
		var qtyRec = getElementAmount(cellObjOfQtyRecd);
		var qtyRej = getElementAmount(qtyRejObj);

		// select as accepted
		if (ident != 'S') {
			cellObjOfQtyRecd.value = formatAmountValue(qtyDelivered, true);
			qtyRejObj.value = formatAmountValue(0, true);
		} else {
			itemCheckObj.checked = true;
			selectSerialItem(itemCheckObj, i, j);
		}

	} else {
		if (ident != 'S') {
			cellObjOfQtyRecd.value = formatAmountValue(0, true);
			qtyRejObj.value = formatAmountValue(0, true);
		} else {
			itemCheckObj.checked = false;
			selectSerialItem(itemCheckObj, i, j);
			itemCheckObj.disabled = true;
		}
	}
}

function onChangeAcceptQty(obj,i, j) {
	var row = getThisRow(obj);
	var hProcess = getElementByName(row, "process"+i);
	var qtyDeliveredObj = getElementByName(row, "item_qty_del"+i);
	var qtyRecdObj = getElementByName(row, "item_qty_rec"+i);
	var qtyRejObj = getElementByName(row, "item_qty_rej"+i);

	if (hProcess.value == "Y") {
		var qtyDelivered = getElementAmount(qtyDeliveredObj);
		var qtyRec = getElementAmount(qtyRecdObj);
		var qtyRej = getElementAmount(qtyRejObj);
		if(receiveStatus != 'F')
			qtyRejObj.value = formatAmountValue((qtyDelivered - qtyRec), true);
	}
}

function saveForm() {
	var indentSelectelmts = document.getElementsByName("indentSelect");

	var anyRowProcessed = false;
	for (var i = 0; i < indentSelectelmts.length; i++) {
		if (indentSelectelmts[i].value == 'true') {
			anyRowProcessed = true;
			break;
		}
	}

	if (!anyRowProcessed) {
		showMessage("js.stores.mgmt.noitemsprocessed");
		return false;
	}
	document.forms[0].submit();
}

function checkstoreallocation() {
	if (gRoleId != 1 && gRoleId != 2 && accessstores != 'A') {
		if (deptId == "") {
			showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
			document.getElementById("storecheck").style.display = 'none';
		}
	}
}
