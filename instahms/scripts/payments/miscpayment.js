var miscForm = document.miscPaymentForm;
var paymentsAdded = 0;
var ACCOUNT_HEAD = 0, DESCRIPTION = 1, CATEGORY = 2, AMOUNT = 3, TRASH_COL = 4, EDIT_COL = 5;

function init() {
	miscForm = document.miscPaymentForm;
	initPaymentDialog();
 	loadAcchountHeads();
	miscForm.name.focus();
}

function initPaymentDialog() {
	var dialogDiv = document.getElementById("addPaymentDialog");
	dialogDiv.style.display = 'block';
	paymentDialog = new YAHOO.widget.Dialog("addPaymentDialog",
			{	width:"300px",
				context : ["payment", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add', 'click', addToTable, paymentDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleCancel, paymentDialog, true);
	YAHOO.util.Event.addListener('Ok', 'click', handleOk, paymentDialog, true);
	YAHOO.util.Event.addListener('Previous', 'click', openPrevious, paymentDialog, true);
	YAHOO.util.Event.addListener('Next', 'click', openNext, paymentDialog, true);
	subscribeKeyListeners(paymentDialog);
	paymentDialog.render();
}

function subscribeKeyListeners(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

function editTableRow(id) {
	var row = getChargeRow(id);

	var accountHeadId = document.getElementById('_dAccountHead').value;
	var accountHead = document.getElementById('_dAccountHead').options[document.getElementById('_dAccountHead').selectedIndex].text;
   	var description = document.getElementById('_dDescription').value;
   	var category = document.getElementById('_dCategory').value;
   	var amount = document.getElementById('_dAmount').value;

   	if (amount == '' || amount == 0) {
   		alert('Please enter the amount');
   		return false;
   	}

   	setNodeText(row.cells[ACCOUNT_HEAD], accountHead);
	setNodeText(row.cells[DESCRIPTION], description, 30);
	setNodeText(row.cells[CATEGORY], category, 10);
	setNodeText(row.cells[AMOUNT], amount, 30);

	setHiddenValue(id, "accountHead", accountHeadId);
	setHiddenValue(id, "description", description);
	setHiddenValue(id, "category", category);
	setHiddenValue(id, "amount", amount);
	YAHOO.util.Dom.removeClass(row, 'editing');

	resetTotals();
	return id;
}

function openPrevious(id, previous, next) {
	var id = document.miscPaymentForm.editRowId.value;
	editTableRow(id);
	this.cancel();
	if (parseInt(id) != 0)
		showEditPaymentDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);

}

function openNext() {
	var id = document.miscPaymentForm.editRowId.value;
	editTableRow(id);
	this.cancel();
	if (parseInt(id)+1 != document.getElementById('paymentsTable').rows.length-3) {
		showEditPaymentDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
	}
}

function handleOk() {
	var id = document.miscPaymentForm.editRowId.value;
	editTableRow(id);
	this.cancel();
}

function addPaymentDialog(obj) {
	if (parseInt(maxcenters) >1 && parseInt(centerId) == 0) {
		alert('Please impersonate a center user to post miscellaneous payments.');
		return false;
	}
	var row = getThisRow(obj);

	// clearFields needs to be called before opening and closing dialog.
	// because same dialog is used for add and editing payments.
	clearFields();

	document.getElementById('titleForAdd').style.display = 'block';
	document.getElementById('titleForEdit').style.display = 'none';
	document.getElementById('Ok').style.display = 'none';
	document.getElementById('Add').style.display = 'block';
	document.getElementById('Previous').style.display = 'none';
	document.getElementById('Next').style.display = 'none';

	paymentDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	paymentDialog.show();
	document.miscPaymentForm._dAccountHead.focus();
	return false;
}

function showEditPaymentDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	// to avoid the flash on the screen when u click on next and previous buttons,
	//	we need to show the dialog first and then set the values
	paymentDialog.show();

	document.getElementById('titleForAdd').style.display = 'none';
	document.getElementById('titleForEdit').style.display = 'block';
	document.getElementById('Add').style.display = 'none';
	document.getElementById('Ok').style.display = 'block';
	document.getElementById('Previous').style.display = 'block';
	document.getElementById('Next').style.display = 'block';

	paymentDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	YAHOO.util.Dom.addClass(row, 'editing');

	var deleted = getIndexedValue("delPayment", id);
	var accountHead = getIndexedValue("accountHead",id);

	document.miscPaymentForm._dAccountHead.value = getIndexedValue("accountHead", id);
	document.miscPaymentForm._dDescription.value = getIndexedValue("description", id);
	document.miscPaymentForm._dCategory.value = getIndexedValue("category", id);
	document.miscPaymentForm._dAmount.value = getIndexedValue("amount", id);
	document.miscPaymentForm.editRowId.value = id;

	document.miscPaymentForm._dAccountHead.focus();
	return false;
}

function handleCancel() {
	var id = document.miscPaymentForm.editRowId.value;
	var row = getChargeRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	this.cancel();
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.miscPaymentForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function addToTable() {

	var amount = document.getElementById('_dAmount').value;
	if (amount == '' || amount == 0) {
   		alert('Please enter the amount');
   		return false;
   	}

	var id = getNumCharges();
   	var table = document.getElementById("paymentsTable");
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "paymentRow" + id;

   	var cell = null;
   	var accountHead = document.getElementById('_dAccountHead').options[document.getElementById('_dAccountHead').selectedIndex].text;
   	var accountHeadId = document.getElementById('_dAccountHead').value;
   	var description = document.getElementById('_dDescription').value;
   	var category = document.getElementById('_dCategory').value;

   	setNodeText(row.cells[ACCOUNT_HEAD], accountHead, 30);
	setNodeText(row.cells[DESCRIPTION], description, 30);
	setNodeText(row.cells[CATEGORY], category, 30);
	setNodeText(row.cells[AMOUNT], formatAmountValue(amount));

	setHiddenValue(id, "accountHead", accountHeadId);
	setHiddenValue(id, "description", description);
	setHiddenValue(id, "category", category);
	setHiddenValue(id, "amount", amount);

	row.className = "added";
	paymentsAdded++;
	resetTotals();
	clearFields();

	this.align("tr", "tl");
	return id;
}

function clearFields() {
	document.getElementById('_dAccountHead').value = '';
   	document.getElementById('_dDescription').value = '';
   	document.getElementById('_dCategory').value = '';
   	document.getElementById('_dAmount').value = '';
}

function setRowStyle(i) {
	var row = getChargeRow(i);
	var accountHeadId = getIndexedValue("accountHeadId", i);
	var flagImgs = row.cells[ACCOUNT_HEAD].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (accountHeadId.substring(0,1) == "_");
	var cancelled = getIndexedValue("delPayment", i) == 'true';
	var edited = getIndexedValue("edited", i) == 'true';
	var chargeHead = getIndexedValue("accountHead", i);

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

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelPayment(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var oldDeleted =  getIndexedValue("delPayment", id);
	var isNew = getIndexedValue("isNew", id);

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		paymentsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}

		setIndexedValue("delPayment", id, newDeleted);
		setIndexedValue("edited", id, "true");
		setRowStyle(id);
	}

	resetTotals();
	return false;
}

function resetTotals() {
	var totAmtPaise = 0;
	for (var i=0; i<getNumCharges(); i++) {
		var delPayment = getIndexedFormElement(document.miscPaymentForm, "delPayment", i);
		if (delPayment && "true" == delPayment.value)
			continue;
		totAmtPaise += getIndexedPaise("amount", i);
	}
	document.getElementById("lblTotalAmt").textContent = formatAmountPaise(totAmtPaise);
}

function getNumCharges() {
	// header, add row, hidden template row: totally 3 extra
	return document.getElementById("paymentsTable").rows.length-3;
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges() + 1;
}

function getChargeRow(i) {
	i = parseInt(i);
	var table = document.getElementById("paymentsTable");
	return table.rows[i + getFirstItemRow()];
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.miscPaymentForm, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.miscPaymentForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.miscPaymentForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function roundEnteredNumber(number, decimal) {
	formatAmountObj(document.getElementById('_dAmount'));
}

function validateForm(obj) {
	document.getElementById("screenAction").value = obj.value;
	 var amountIsZero = false;
	 miscForm = document.miscPaymentForm;
	 payeeName = miscForm.name.value;
	if (payeeName=="") {
		alert("Enter Payee Name");
		miscForm.name.focus();
		return false;
	}
	var ch = payeeName.substring(0,1);
	if ((ch<"A" || ch>"Z") &&
			(ch<"a" || ch>"z") && (ch==" ")) {
		alert("Name Starts with Alphabate only");
		miscForm.name.value="";
		miscForm.name.focus();
		return false;
	}
	if (paymentsAdded == 0) {
		alert('Please add atleast one row.');
		return false;
	}
	var amt = document.getElementsByName("amount");
	for (i=0;i<amt.length-1;i++) {
		if (amt[i].value == "") {
			alert("Enter Amount");
			amt[i].focus();
			return false;
		} else {
			var enteredAmt = amt[i].value;
			for (j=0; j<enteredAmt.length; j++) {
				if (enteredAmt[j]!= "0" && enteredAmt[j]!= ".") {
					amountIsZero = false;
					break;
				} else {
					amountIsZero = true;
				}
			}
			if (amountIsZero) {
				alert("Entered amount should be greater than zero");
				amt[i].focus();
				return false;
			}
		}
	}

	var paydate = miscForm.paydate.value;
	if (paydate=="") {
		alert("Select Date as Current Date");
		return false
	}

	var msg = validateDateStr(paydate,"past");
	if (msg == null){

	} else {
		alert(msg);
		return false;
	}
	miscForm.submit();
}


function loadAcchountHeads() {
	var sel = document.miscPaymentForm._dAccountHead;
	loadSelectBox(sel, accountHeadList, "account_head_name", "account_head_id", " Select ", "");
}

function hidePaymentModeForPaymentVoucher() {
	var paymentModeId = "paymentModeId";
	
	$("#"+paymentModeId+" option[value='-8']").remove();
	$("#"+paymentModeId+" option[value='-6']").remove();
	$("#"+paymentModeId+" option[value='-7']").remove();
	$("#"+paymentModeId+" option[value='-9']").remove();
}
