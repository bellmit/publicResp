var reversalForm = document.paymentreversal;
var REASON = 0, VOUCHER_NO=1, AMOUNT = 2, TRASH_COL = 3, EDIT_COL = 4;
var paymentsAdded = 0;

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
	YAHOO.util.Event.addListener('Ok', 'click', handleOk, paymentDialog, true);
	YAHOO.util.Event.addListener('Add', 'click', addToTable, paymentDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleCancel, paymentDialog, true);
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

	var reason = document.getElementById('_dReason').value;
	var voucherNo = document.getElementById('_dVoucherNo').value;
   	var amount = document.getElementById('_dAmount').value;

   	if (amount == '' || amount == 0) {
   		alert('Please enter the amount');
   		return false;
   	}

   	setNodeText(row.cells[REASON], reason);
	setNodeText(row.cells[VOUCHER_NO], voucherNo);
	setNodeText(row.cells[AMOUNT], amount, 10);

	setHiddenValue(id, "description", reason);
	setHiddenValue(id, "voucherNo", voucherNo);
	setHiddenValue(id, "amount", amount);
	YAHOO.util.Dom.removeClass(row, 'editing');

	resetTotals();
	return id;
}

function openPrevious(id, previous, next) {
	var id = reversalForm.editRowId.value;
	editTableRow(id);
	this.cancel();
	if (parseInt(id) != 0)
		showEditPaymentDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);

}

function openNext() {
	var id = reversalForm.editRowId.value;
	editTableRow(id);
	this.cancel();
	if (parseInt(id)+1 != document.getElementById('paymentsTable').rows.length-3) {
		showEditPaymentDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
	}
}

function handleOk() {
	var id = reversalForm.editRowId.value;
	editTableRow(id);
	this.cancel();
}

function addPaymentDialog(obj) {
	var row = getThisRow(obj);

	// clearFields needs to be called before opening and closing dialog.
	// because same dialog is used for add and editing payments.
	clearFields();

	document.getElementById('titleForAdd').style.display = 'block';
	document.getElementById('titleForEdit').style.display = 'none';
	document.getElementById('Add').style.display = 'block';
	document.getElementById('Previous').style.display = 'none';
	document.getElementById('Next').style.display = 'none';

	paymentDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	paymentDialog.show();
	reversalForm._dReason.focus();
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
	document.getElementById('Ok').style.display = 'block';
	document.getElementById('Add').style.display = 'none';
	document.getElementById('Previous').style.display = 'block';
	document.getElementById('Next').style.display = 'block';

	paymentDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	YAHOO.util.Dom.addClass(row, 'editing');

	var deleted = getIndexedValue("delPayment", id);

	reversalForm._dReason.value = getIndexedValue("description", id);
	reversalForm._dVoucherNo.value = getIndexedValue("voucherNo", id);
	reversalForm._dAmount.value = getIndexedValue("amount", id);
	reversalForm.editRowId.value = id;

	reversalForm._dReason.focus();
	return false;
}

function handleCancel() {
	var id = reversalForm.editRowId.value;
	var row = getChargeRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	this.cancel();
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(reversalForm, name, index);
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
   	var reason = document.getElementById('_dReason').value;
   	var voucherNo = document.getElementById('_dVoucherNo').value;
   	var amount = document.getElementById('_dAmount').value;

   	setNodeText(row.cells[REASON], reason);
	setNodeText(row.cells[VOUCHER_NO], voucherNo);
	setNodeText(row.cells[AMOUNT], formatAmountValue(amount), 30);

	setHiddenValue(id, "description", reason);
	setHiddenValue(id, "voucherNo", voucherNo);
	setHiddenValue(id, "amount", amount);

	row.className = "added";
	paymentsAdded++;
	resetTotals();
	clearFields();

	this.align("tr", "tl");
	return id;
}



function setRowStyle(i) {
	var row = getChargeRow(i);
	var flagImgs = row.cells[REASON].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = true;
	var cancelled = getIndexedValue("delPayment", i) == 'true';
	var edited = getIndexedValue("edited", i) == 'true';

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
		var delPayment = getIndexedFormElement(reversalForm, "delPayment", i);
		if (delPayment && "true" == delPayment.value)
			continue;
		totAmtPaise += getElementPaise(getIndexedFormElement(reversalForm, "amount", i));
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
	return getElementPaise(getIndexedFormElement(reversalForm, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(reversalForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(reversalForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function roundEnteredNumber(number, decimal) {
	document.getElementById('_dAmount').value =  roundNumber(number,decimal);
}

function init(){
 reversalForm = document.paymentreversal;
 initPaymentDialog();
 clearFields();
}
function clearFields(){
 document.getElementById("_dReason").value="";
 document.getElementById("_dVoucherNo").value ="";
 document.getElementById("_dAmount").value ="";

}

function validateForm(id){
	var payeeName = reversalForm.payeeName.value;
	var payType = document.forms[0].paymentType.value;
	var counter = document.getElementById('counter').value;

	if (payType == "") {
		alert("Select Payment Type");
		return false;
	}

	if (payeeName == "") {
		alert("Enter Payee Name");
		return false;
	}

	if (counter == '') {
		alert("Please select counter");
		document.getElementById('counter').focus();
		return false;
	}

	if (paymentsAdded == 0) {
		alert('Please add atleat one row.');
		return false;
	}

	var amt = document.getElementsByName("amount");
	for (i=0; i<amt.length-1; i++) {
		if (amt[i].value == "") {
			alert("Enter Amount");
			amt[i].focus();
			return false;
		}
	}

	var paydate = reversalForm.payDate.value;
	if (paydate=="") {
		alert("Select Date as Current Date");
		return false
	}

	var msg = validateDateStr(paydate,"past");
	if (msg == null) {

	} else {
		alert(msg);
		return false;
	}
	reversalForm.submit();
}

var payeesNamesAutoCom= null;
function allPayees(payType){

	var payeeNames = document.getElementById("payees");
	var payeeContainer = payeeNameContainer;
	if (payeesNamesAutoCom != null){
		payeesNamesAutoCom.destroy();
	}
		var payeeNameArray = payeesList != null ? payeesList.map(function(x){return x.payee_name}) : [];
	this.osDsArray = new YAHOO.widget.DS_JSArray(payeeNameArray);
	payeesNamesAutoCom = new YAHOO.widget.AutoComplete(payeeNames, payeeContainer, this.osDsArray);
	payeesNamesAutoCom.prehighlightClassName = "yui-ac-prehighlight";
	payeesNamesAutoCom.typeAhead = true;
	payeesNamesAutoCom.useShadow = false;
	payeesNamesAutoCom.allowBrowserAutocomplete = false;
	payeesNamesAutoCom.autoHighlight = true;
	payeesNamesAutoCom.minQueryLength=0;
	payeesNamesAutoCom.forceSelection = true;
	payeesNamesAutoCom.maxResultsDisplayed = 100;

	payeesNamesAutoCom.itemSelectEvent.subscribe(getPayeeName);
	payeesNamesAutoCom.selectionEnforceEvent.subscribe(clearHiddenId);


	function getPayeeName(){
		var payeeName = YAHOO.util.Dom.get(payeeNames).value;
		var payee =  payeesList.find(function(x){ return x.payee_name === payeeName});
		if (!payee) {
			return;
		} 
		reversalForm.payeeId.value = payee.payee_id;
	}

	function clearHiddenId(){
		reversalForm.payeeId.value = "";
	}
}

function onSelectPaymentType(payType) {

// send ajax request to get payment list corresponding to payment type
	var ajaxobj = newXMLHttpRequest();
	var url = cpath
			+ '/pages/payments/PaymentReversal.do?_method=getPayeesList'
			+ '&payee_type=' + payType;
	
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				payeesList = JSON.parse(ajaxobj.responseText);
				allPayees(payType);
			}
		}
	}

	// display/hide account group filter
	if (payType == 'C') {
		document.getElementById('acheadrow').style.display = 'table-row';

	} else {
		document.getElementById('acheadrow').style.display = 'none';

	}
}