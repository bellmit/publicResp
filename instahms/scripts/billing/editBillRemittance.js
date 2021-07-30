var paymentform = null;
var allocateform  = null;

function initForm() {
	var i=0;
	REMT_COL = i++; PYMT_COL = i++; PDATE_COL = i++; PAMT_COL = i++; ALLOC_COL = i++; UNALLOC_COL = i++; PEDIT_COL = i++;

	var j=0;
	CHECK_COL = j++; CHRG_POSTED_DATE_COL = j++; CHRG_HEAD_COL = j++; CHRG_DESC_COL = j++;
	CHRG_AMOUNT_COL = j++; CHRG_PATIENT_AMT_COL = j++; CHRG_CLAIM_AMT_COL = j++;  CHRG_REM_AMT_COL = j++; CHRG_EDIT_COL = j++;

	mainform	= document.mainform;
	paymentform = document.paymentform;
	allocateform = document.allocateform;

	if ( (roleId == 1) || (roleId == 2) ) {
		allowBackDate = 'A';
		cancelBillRights = 'A';
		writeOffAmountRights = 'A';
	}

	// applicable status values for the bill status dropdown
	if(cancelBillRights == 'A') {
		billStatusList = [{status:'A',value:1,text:'Open'},
					   {status:'F',value:2,text:'Finalized'},
					   {status:'C',value:3,text:'Closed'},
					   {status:'X',value:4,text:'Cancelled'}];
	} else {
		billStatusList = [{status:'A',value:1,text:'Open'},
					   {status:'F',value:2,text:'Finalized'},
					   {status:'C',value:3,text:'Closed'}];
	}
	loadBillStatus();
	initPaymentDialog();
	initAllocationDialog();
}

// Payment edit dialog
function initPaymentDialog() {
	var dialogDiv = document.getElementById("paymentDialog");
	dialogDiv.style.display = 'block';
	editPaymentDialog = new YAHOO.widget.Dialog("paymentDialog",{
			width:"300px",
			text: "Edit Payment",
			context :["paymentReferencesTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onPaymentCancel,
	                                                scope:editPaymentDialog,
	                                                correctScope:true } );
	editPaymentDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editPaymentDialog.render();
}

function showNextPaymentDialog() {
	var id = paymentform.paymentRowId.value;
	var row = getPaymentRow(id);
	var nRow = YAHOO.util.Dom.getNextSibling(row);
    if (nRow != null) {
        YAHOO.util.Dom.removeClass(row, 'editing');
		var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[PEDIT_COL]);
		showPaymentDialog(anchor);
    }
}

function showPreviousPaymentDialog() {
	var id = paymentform.paymentRowId.value;
	var row = getPaymentRow(id);
	var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
    var nPrevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
    if (nPrevRow != null) {
        YAHOO.util.Dom.removeClass(row, 'editing');
        var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[PEDIT_COL]);
        showPaymentDialog(anchor);
    }
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getPaymentRow(i) {
	i = parseInt(i);
	var table = document.getElementById("paymentReferencesTable");
	return table.rows[i + 1];
}

function showPaymentDialog(obj, addnew) {

	if (!validateAllocations())
		return false;

	if (typeof(addnew) != 'undefined') {
		var addBtn = document.getElementById("btnAddPayment");
		paymentform.paymentRowId.value = "";

		editPaymentDialog.cfg.setProperty("context", [addBtn, "tr", "bl"], false);

		setPaymentRefDetails();

		editPaymentDialog.show();
		paymentform.savePaymentBtn.style.display = 'inline';
		paymentform.editPaymentBtn.style.display = 'none';
		paymentform.pPaymentReference.focus();

	}else {
		var row = getThisRow(obj);
		if (row == null) return false;
		var id = row.rowIndex - 1;

		YAHOO.util.Dom.addClass(row, 'editing');
		paymentform.paymentRowId.value = id;

		editPaymentDialog.cfg.setProperty("context", [row.cells[PEDIT_COL], "tr", "bl"], false);

		setPaymentRefDetails();

		editPaymentDialog.show();
		paymentform.editPaymentBtn.style.display = 'inline';
		paymentform.savePaymentBtn.style.display = 'none';
		paymentform.pTotalAmount.focus();
	}
	return false;
}

function setPaymentRefDetails() {
	var id = paymentform.paymentRowId.value;

	if (!empty(id)) {
		var payRef	= getIndexedValue("paymentReference", id);
		var payDate	= getIndexedValue("paymentRecdDate", id);
		var payAmt	= getIndexedValue("paymentAmount", id);
		var payAllocAmt	= getIndexedValue("paymentAllocAmount", id);
		var payUnallocAmt	= getIndexedValue("paymentUnallocAmount", id);

		paymentform.pPaymentReference.value = payRef;
		paymentform.pPaymentDate.value =  payDate;
		paymentform.pTotalAmount.value = payAmt;

		document.getElementById("pAllocatedAmount").textContent = payAllocAmt;
		document.getElementById("pUnallocatedAmount").textContent = payUnallocAmt;

	}else {
		paymentform.pPaymentReference.value = '';
		paymentform.pPaymentDate.value = '';
		paymentform.pTotalAmount.value = '';

		document.getElementById("pAllocatedAmount").textContent = '';
		document.getElementById("pUnallocatedAmount").textContent = '';
	}
}

function onPaymentSubmit() {

	var id = paymentform.paymentRowId.value;
	var row = getPaymentRow(id);

	if (!validateAllocations())
		return false;

	if (!validatePaymentRefFields(id))
		return false;

	var origPayAmt		= getIndexedPaise("paymentAmount",id);
	var origAllocAmt	= getIndexedPaise("paymentAllocAmount",id);
	var origUnallocAmt	= getIndexedPaise("paymentUnallocAmount",id);

	var pRef = paymentform.pPaymentReference.value;
	var pDate = paymentform.pPaymentDate.value;
	var pAmtPaise = getPaise(paymentform.pTotalAmount.value);

	var amtDiff = pAmtPaise - origPayAmt;

	origUnallocAmt += amtDiff;

	if (origUnallocAmt < 0 && origAllocAmt > 0) {
		alert("Payment amount is less by "+Math.abs(formatAmountPaise(amtDiff))
				+".\nPlease reduce the allocated amount by "+Math.abs(formatAmountPaise(amtDiff))
				+"\n(or) Reset Allocations and then edit payment.");
		return false;
	}

	setNodeText(row.cells[PYMT_COL], pRef);
	setNodeText(row.cells[PDATE_COL], pDate);
	setNodeText(row.cells[PAMT_COL], formatAmountPaise(pAmtPaise));
	setNodeText(row.cells[ALLOC_COL], formatAmountPaise(origAllocAmt));
	setNodeText(row.cells[UNALLOC_COL], formatAmountPaise(origUnallocAmt));

	setHiddenValue(id, "paymentReference", pRef);
	setHiddenValue(id, "paymentRecdDate", pDate);
	setHiddenValue(id, "paymentAmount", formatAmountPaise(pAmtPaise));
	setHiddenValue(id, "paymentAllocAmount", formatAmountPaise(origAllocAmt));
	setHiddenValue(id, "paymentUnallocAmount", formatAmountPaise(origUnallocAmt));

	if (isPaymentValueEdited(id)) {
		row.className = "edited";
		recalPaymentRefTotals();
		paymentsEdited++;
	}
	editPaymentDialog.hide();
	return id;
}

function resetPaymentAllocations() {
	for (var i=0;i<getPaymentRefTabRows();i++) {

		var payAmt = getIndexedPaise("paymentAmount",i);
		var allocAmt = getIndexedPaise("paymentAllocAmount",i);
		var unallocAmt = getIndexedPaise("paymentUnallocAmount",i);

		var row = getPaymentRow(i);

		setNodeText(row.cells[ALLOC_COL], formatAmountPaise(0));
		setNodeText(row.cells[UNALLOC_COL], formatAmountPaise(payAmt));

		setHiddenValue(i, "paymentAllocAmount", formatAmountPaise(0));
		setHiddenValue(i, "paymentUnallocAmount", formatAmountPaise(payAmt));

		if (allocAmt != 0 || unallocAmt != payAmt) {
			row.className = "edited";
		}
	}
}

function resetChargeAllocations() {
	for (var i=0;i<getChargesTabRows();i++) {

		var row = getAllocateRow(i);
		var chargeId = getIndexedValue("chargeId", i);
		var allocPaise = getIndexedPaise("allocatedClaimAmt", i);

		var aAllocRefElmts = document.getElementsByName('aPaymentReference');

		for (var j=0 ;j<aAllocRefElmts.length;j++) {
			var payRef = aAllocRefElmts[j].value;
			var chargeRefAmtElmt = chargeId+"_"+payRef;
			document.getElementById(chargeRefAmtElmt).value = chargeRefAmtElmt+"_"+formatAmountPaise(0);
		}

		setNodeText(row.cells[CHRG_REM_AMT_COL], formatAmountPaise(0));
		setHiddenValue(i, "allocatedClaimAmt", formatAmountPaise(0));

		if (allocPaise != 0) {
			row.className = "edited";
			allocationsEdited++;
		}
	}
}

function resetAllocations() {
	resetPaymentAllocations();
	resetChargeAllocations();
	recalPaymentRefTotals();
	return false;
}

function isPaymentValueEdited(id) {
	var pAmt = paymentform.pTotalAmount.value;
	var pRef = paymentform.pPaymentReference.value;
	var pDate = paymentform.pPaymentDate.value;

	var origPAmt	= getIndexedPaise("paymentAmount",id);
	var origPRef	= getIndexedPaise("paymentReference",id);
	var origPDate	= getIndexedPaise("paymentRecdDate",id);

	if (pAmt == origPAmt && pRef == origPRef && pDate == origPDate)
		return false;
	return true;
}

function onPaymentCancel() {
	var id = paymentform.paymentRowId.value;
	if (!empty(id)) {
		var row = getPaymentRow(id);
		YAHOO.util.Dom.removeClass(row, 'editing');
		editPaymentDialog.hide();
		var editImg = row.cells[6].childNodes[1];
		editImg.focus();
	}else {
		editPaymentDialog.hide();
		document.getElementById("btnAddPayment").focus();
	}
}

function selectAllCharges() {
	var chrgCheckElmts = document.mainform.chargeCheck;
	if (document.getElementById("allCharges").checked)	{
		if (chrgCheckElmts.length == undefined)
			chrgCheckElmts.checked = true;
		else {
			for(var i=0;i<chrgCheckElmts.length;i++)
				chrgCheckElmts[i].checked = true;
		}
	} else {
		if (chrgCheckElmts.length == undefined)
			chrgCheckElmts.checked = false;
		else {
			for(var i=0;i<chrgCheckElmts.length;i++)
				chrgCheckElmts[i].checked = false;
		}
	}
}

function validatePaymentRefFields(index) {
	var pAmt = formatAmountValue(paymentform.pTotalAmount.value);
	var pRef = paymentform.pPaymentReference.value;
	var pDate = paymentform.pPaymentDate.value;

	if (pRef == '') {
		alert("Enter payment reference");
		paymentform.pPaymentReference.focus();
		return false;
	}

	if (paymentform.pPaymentDate.value == '') {
		alert("Enter payment date");
		paymentform.pPaymentDate.focus();
		return false;
	}

	if (!validateRequired(paymentform.pPaymentDate, "Payment date is required"))
		return false;

	if (!doValidateDateField(paymentform.pPaymentDate,"past"))
		return false;

	if (paymentform.pTotalAmount.value == '') {
		alert("Enter payment amount");
		paymentform.pTotalAmount.focus();
		return false;
	}

	if (!validateAmount(paymentform.pTotalAmount, "Payment amount must be a valid amount")) {
		paymentform.pTotalAmount.focus();
		return false;
	}

	if (validateDuplicatePaymentRef(index, pRef)) {
		alert("Duplicate Payment Reference. "+pRef+" is already added.");
		paymentform.pPaymentReference.focus();
		return false;
	}
	return true;
}

var paymentsAdded = 0;
var paymentsEdited = 0;

function addNewPayment() {

	var pAmt = formatAmountValue(paymentform.pTotalAmount.value);
	var pRef = paymentform.pPaymentReference.value;
	var pDate = paymentform.pPaymentDate.value;

	var table = document.getElementById("paymentReferencesTable");
	var id = getPaymentRefTabRows();
	var lastRow = table.rows[id];

	if (!validatePaymentRefFields(id))
		return false;

	var newRow = null;
	if (id > 0) {
		newRow = lastRow.cloneNode(true);
		table.tBodies[0].insertBefore(newRow, lastRow);
		newRow = getPaymentRow(id);
		newRow.style.display = '';
	   	newRow.id="pRow"+id;
	}else {
		newRow = lastRow;
	}

	setNodeText(newRow.cells[REMT_COL], '');
	setNodeText(newRow.cells[PYMT_COL], pRef);
	setNodeText(newRow.cells[PDATE_COL], pDate);
	setNodeText(newRow.cells[PAMT_COL], pAmt);
	setNodeText(newRow.cells[ALLOC_COL], 0);
	setNodeText(newRow.cells[UNALLOC_COL], pAmt);

	setHiddenValue(id, "paymentRemittanceId", "_");
	setHiddenValue(id, "paymentReference", pRef);
	setHiddenValue(id, "paymentRecdDate", pDate);
	setHiddenValue(id, "paymentAmount", pAmt);
	setHiddenValue(id, "paymentAllocAmount", 0);
	setHiddenValue(id, "paymentUnallocAmount", pAmt);

	newRow.className = "added";
	paymentsAdded++;
	recalPaymentRefTotals();

	editPaymentDialog.cfg.setProperty("context", [newRow.cells[PEDIT_COL], "tr", "bl"], false);
	paymentform.pTotalAmount.focus();
	return id;
}

function validateDuplicatePaymentRef(index, pRef) {
	for (var i=0;i<getPaymentRefTabRows();i++) {
		if (i != index && getIndexedValue("paymentReference", i) == pRef ) {
			return true;
		}
	}
	return false;
}

function getPaymentRefTabRows() {
	var table = document.getElementById("paymentReferencesTable");
	return  (table.rows.length - 1);
}

function recalPaymentRefTotals() {
	var remTotal = 0;
	var allocAmtTot = 0;
	var unallocAmtTot = 0;
	for (var i=0;i<getPaymentRefTabRows();i++) {

		remTotal += getIndexedPaise("paymentAmount",i);
		allocAmtTot += getIndexedPaise("paymentAllocAmount",i);
		unallocAmtTot += getIndexedPaise("paymentUnallocAmount",i);
	}

	setNodeText("lblTotalRemittanceAmt", formatAmountPaise(remTotal));
	setNodeText("lblTotalAllocatedAmt", formatAmountPaise(allocAmtTot));
	setNodeText("lblTotalUnallocatedAmt", formatAmountPaise(unallocAmtTot));
}

// Allocation edit dialog
function initAllocationDialog() {
	var dialogDiv = document.getElementById("allocateDialog");
	dialogDiv.style.display = 'block';
	editAllocationDialog = new YAHOO.widget.Dialog("allocateDialog",{
			width:"500px",
			text: "Edit Allocation",
			context :["chargesTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onAllocateCancel,
	                                                scope:editAllocationDialog,
	                                                correctScope:true } );
	editAllocationDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editAllocationDialog.render();
}

function getAllocateRow(i) {
	i = parseInt(i);
	var table = document.getElementById("chargesTable");
	return table.rows[i + 1];
}

function showNextAllocateDialog() {
	var id = allocateform.allocateRowId.value;
	var row = getAllocateRow(id);
	var nRow = YAHOO.util.Dom.getNextSibling(row);
    if (nRow != null) {
        YAHOO.util.Dom.removeClass(row, 'editing');
		var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[CHRG_EDIT_COL]);
		showAllocateDialog(anchor);
    }
}

function showPreviousAllocateDialog() {
	var id = allocateform.allocateRowId.value;
	var row = getAllocateRow(id);
	var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
    var nPrevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
    if (nPrevRow != null) {
        YAHOO.util.Dom.removeClass(row, 'editing');
        var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[CHRG_EDIT_COL]);
        showAllocateDialog(anchor);
    }
}

function showAllocateDialog(obj) {

	if (!validateNewPayments())
		return false;

	var row = getThisRow(obj);
	if (row == null) return false;
	var id = row.rowIndex - 1;

	YAHOO.util.Dom.addClass(row, 'editing');
	allocateform.allocateRowId.value = id;

	editAllocationDialog.cfg.setProperty("context", [row.cells[CHRG_EDIT_COL], "tr", "bl"], false);

	setAllocationAmountDetails();

	editAllocationDialog.show();
	var allocElmts = document.getElementsByName("aAllocateAmount");
	if (allocElmts != null && allocElmts.length > 0)
		document.getElementsByName("aAllocateAmount")[0].focus();
	return false;
}

function setAllocationAmountDetails() {
	var id = allocateform.allocateRowId.value;

	var chargeID = getIndexedValue("chargeId", id);
	var chargeRefAmtElmts = document.getElementsByName("chargeid_paymentref_amount");
	var chargePayRefElmts = document.getElementsByName("aPaymentReference");

	var totalAllocAmount = 0;

	if (!empty(chargePaymentsJSON)) {
		for (var j=0;j <chargePaymentsJSON.length;j++) {
			for (var p=0; p<chargePayRefElmts.length; p++) {
				if (chargePaymentsJSON[j].charge_id == chargeID && chargePaymentsJSON[j].payment_reference == chargePayRefElmts[p].value) {
					var chargeRefAmtElmt = chargePaymentsJSON[j].charge_id+"_"+chargePaymentsJSON[j].payment_reference;
					var chrgPymtAmtArr = (document.getElementById(chargeRefAmtElmt).value).split("_");

					document.getElementById("aAllocateAmount_"+chargePaymentsJSON[j].payment_reference).value = formatAmountValue(chrgPymtAmtArr[2]);
					document.getElementById("aOrigAllocateAmount_"+chargePaymentsJSON[j].payment_reference).value = formatAmountValue(chrgPymtAmtArr[2]);
					totalAllocAmount += getPaise(chrgPymtAmtArr[2]);
				}
			}
		}
	}

	document.getElementById("allocTotal").textContent = formatAmountPaise(totalAllocAmount);
}

var allocationsEdited = 0;
function onAllocateSubmit() {

	var id = allocateform.allocateRowId.value;
	var row = getAllocateRow(id);

	var chargeId = getIndexedValue("chargeId", id);
	var chargeInsClaimAmtPaise = getIndexedPaise("insClaimAmt", id);
	var totalPaise = 0;

	var aAllocAmtElmts = document.getElementsByName('aAllocateAmount');
	var aAllocRefElmts = document.getElementsByName('aPaymentReference');
	var aOrigAllocAmtElmts = document.getElementsByName('aOrigAllocateAmount');

	for (var i=0 ;i<aAllocAmtElmts.length;i++) {
		var payPaise = getPaise(aAllocAmtElmts[i].value);
		totalPaise += payPaise;
	}

	if (totalPaise > chargeInsClaimAmtPaise) {
		alert("Allocated claim amount is more than charge insurance claim amount.\n"
			+"Please reduce the allocated amount by	" + formatAmountPaise(totalPaise - chargeInsClaimAmtPaise));
		totalPaise = 0;
		return false;
	}

	totalPaise = 0;

	for (var i=0 ;i<aAllocAmtElmts.length;i++) {

		var payRef = aAllocRefElmts[i].value;
		var payPaise = getPaise(aAllocAmtElmts[i].value);
		var origPayPaise = getPaise(aOrigAllocAmtElmts[i].value);

		var chargeRefAmtElmt = chargeId+"_"+payRef;
		document.getElementById(chargeRefAmtElmt).value = chargeRefAmtElmt+"_"+formatAmountPaise(payPaise);

		var payrow = getPaymentReferenceRow(payRef);
		var origBillUnallocPaise = getPaise(document.getElementById("paymentUnallocAmount_"+payRef).value);
		var billUnallocPaise = getPaise(document.getElementById("paymentUnallocAmount_"+payRef).value);
		var billRecdPaise = getIndexedPaise("paymentAmount", (payrow.rowIndex - 1));

		billUnallocPaise = (billUnallocPaise + origPayPaise) - payPaise;

		if (origBillUnallocPaise != billUnallocPaise) {
			setNodeText(payrow.cells[UNALLOC_COL], formatAmountPaise(billUnallocPaise));
			setHiddenValue((payrow.rowIndex - 1), "paymentUnallocAmount", formatAmountPaise(billUnallocPaise));

			setNodeText(payrow.cells[ALLOC_COL], formatAmountPaise(billRecdPaise - billUnallocPaise));
			setHiddenValue((payrow.rowIndex - 1), "paymentAllocAmount", formatAmountPaise(billRecdPaise - billUnallocPaise));

			payrow.className = "edited";
		}

		totalPaise += payPaise;
	}

	var origRemTotalPaise = getIndexedPaise("allocatedClaimAmt",id);

	setNodeText(row.cells[CHRG_REM_AMT_COL], formatAmountPaise(totalPaise));
	setHiddenValue(id, "allocatedClaimAmt", formatAmountPaise(totalPaise));

	if (origRemTotalPaise != totalPaise) {
		row.className = "edited";
		allocationsEdited++;
		recalPaymentRefTotals();
	}

	editAllocationDialog.hide();
	var editImg = row.cells[CHRG_EDIT_COL].childNodes[1];
	editImg.focus();

	return id;
}

function getPaymentReferenceRow(paymentRef) {
	for (var i=0;i<getPaymentRefTabRows();i++) {
		var rowPayRef = getIndexedValue("paymentReference", i);
		if (paymentRef == rowPayRef) {
			return getPaymentRow(i);
		}
	}
	return null;
}

function getPaymentRefTabRows() {
	var table = document.getElementById("paymentReferencesTable");
	return  (table.rows.length - 1);
}

function getChargesTabRows() {
	var table = document.getElementById("chargesTable");
	return  (table.rows.length - 1);
}

function recalPaymentRefTotals() {
	var remTotal = 0;
	var allocAmtTot = 0;
	var unallocAmtTot = 0;
	for (var i=0;i<getPaymentRefTabRows();i++) {

		remTotal += getIndexedPaise("paymentAmount",i);
		allocAmtTot += getIndexedPaise("paymentAllocAmount",i);
		unallocAmtTot += getIndexedPaise("paymentUnallocAmount",i);
	}

	setNodeText("lblTotalRemittanceAmt", formatAmountPaise(remTotal));
	setNodeText("lblTotalAllocatedAmt", formatAmountPaise(allocAmtTot));
	setNodeText("lblTotalUnallocatedAmt", formatAmountPaise(unallocAmtTot));
}


function calculateChargeAllocTotal() {
	var allocAmtTot = 0;
	var aAllocAmtElmts = document.getElementsByName('aAllocateAmount');

	for (var i=0 ;i<aAllocAmtElmts.length;i++) {
		allocAmtTot += getPaise(aAllocAmtElmts[i].value);
	}
	document.getElementById("allocTotal").textContent = formatAmountPaise(allocAmtTot);
}

function onAllocateCancel() {
	var id = allocateform.allocateRowId.value;
	var row = getAllocateRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	editAllocationDialog.hide();
	var editImg = row.cells[CHRG_EDIT_COL].childNodes[1];
	editImg.focus();
}

function autoAllocate() {
	if (!validateNewPayments())
		return false;

	if (!validateChargeCheck()) {
		alert("Select any charge to allocate amount");
		return false;
	}

	var paytable = document.getElementById("paymentReferencesTable");
	var payTabLen = paytable.rows.length - 1;
	var payRowIndex = 0;

	var chrgCheckElmts = document.getElementsByName("chargeCheck");
	var chrgCheckArr = new Array();

	var n = 0;
	for (var k=0; k<chrgCheckElmts.length; k++) {
		if (chrgCheckElmts[k].checked) {
			chrgCheckArr[n] = k;
			n++;
		}
	}

	var chIndex = 0;

	var paymentRowTotalPaise = 0;
	var paymentRowAllocPaise = 0;
	var paymentRowUnallocPaise = 0;
	var paymentRef = '';

	var chRowIndex = 0;
	var chrgInsClaimPaise = 0;
	var chrgRemTotalPaise = 0;
	var chargeId = '';

	if (payTabLen > 0) {
		paymentRowTotalPaise = getIndexedPaise("paymentAmount", payRowIndex);
		paymentRowAllocPaise = getIndexedPaise("paymentAllocAmount", payRowIndex);
		paymentRowUnallocPaise = getIndexedPaise("paymentUnallocAmount", payRowIndex);
		paymentRef = getIndexedValue("paymentReference", payRowIndex);
	}

	if (n > 0) {
		chRowIndex = chrgCheckArr[chIndex];
		chrgInsClaimPaise = getIndexedPaise("insClaimAmt",chRowIndex);
		chrgRemTotalPaise = getIndexedPaise("allocatedClaimAmt",chRowIndex);
		chargeId = getIndexedValue("chargeId", chRowIndex);
	}

	var chrgRemDuePaise = chrgInsClaimPaise - chrgRemTotalPaise;

	while (chIndex < n && payRowIndex < payTabLen) {

		if (paymentRowUnallocPaise > 0) {

			if (chrgRemDuePaise > 0) {
				if (paymentRowUnallocPaise <= chrgRemDuePaise) {
					chrgRemTotalPaise = chrgRemTotalPaise + paymentRowUnallocPaise;

					var row = getAllocateRow(chRowIndex);

					var chargeRefAmtElmt = chargeId+"_"+paymentRef;
					var chrgPymtAmtArr = (document.getElementById(chargeRefAmtElmt).value).split("_");
					var existingChargeRefPaise = getPaise(chrgPymtAmtArr[2]);
					document.getElementById(chargeRefAmtElmt).value = chargeRefAmtElmt+"_"+formatAmountPaise(existingChargeRefPaise + paymentRowUnallocPaise);

					setNodeText(row.cells[CHRG_REM_AMT_COL], formatAmountPaise(chrgRemTotalPaise));
					setHiddenValue(chRowIndex, "allocatedClaimAmt", formatAmountPaise(chrgRemTotalPaise));

					chrgRemDuePaise = chrgInsClaimPaise - chrgRemTotalPaise;
					row.className = "edited";
					allocationsEdited++;

					var payrow = getPaymentRow(payRowIndex);
					paymentRowUnallocPaise = 0;

					setNodeText(payrow.cells[UNALLOC_COL], formatAmountPaise(paymentRowUnallocPaise));
					setHiddenValue(payRowIndex, "paymentUnallocAmount", formatAmountPaise(paymentRowUnallocPaise));

					setNodeText(payrow.cells[ALLOC_COL], formatAmountPaise(paymentRowTotalPaise - paymentRowUnallocPaise));
					setHiddenValue(payRowIndex, "paymentAllocAmount", formatAmountPaise(paymentRowTotalPaise - paymentRowUnallocPaise));

					payrow.className = "edited";

					payRowIndex++;
					if (payRowIndex < payTabLen) {
						paymentRowTotalPaise	= getIndexedPaise("paymentAmount", payRowIndex);
						paymentRowAllocPaise	= getIndexedPaise("paymentAllocAmount", payRowIndex);
						paymentRowUnallocPaise	= getIndexedPaise("paymentUnallocAmount", payRowIndex);
						paymentRef				= getIndexedValue("paymentReference", payRowIndex);
					}

				}else if (paymentRowUnallocPaise > chrgRemDuePaise) {
					chrgRemTotalPaise = chrgRemTotalPaise + chrgRemDuePaise;
					paymentRowUnallocPaise = paymentRowUnallocPaise - chrgRemDuePaise;

					var row = getAllocateRow(chRowIndex);

					var chargeRefAmtElmt = chargeId+"_"+paymentRef;
					var chrgPymtAmtArr = (document.getElementById(chargeRefAmtElmt).value).split("_");
					var existingChargeRefPaise = getPaise(chrgPymtAmtArr[2]);
					document.getElementById(chargeRefAmtElmt).value = chargeRefAmtElmt+"_"+formatAmountPaise(existingChargeRefPaise + chrgRemDuePaise);

					setNodeText(row.cells[CHRG_REM_AMT_COL], formatAmountPaise(chrgRemTotalPaise));
					setHiddenValue(chRowIndex, "allocatedClaimAmt", formatAmountPaise(chrgRemTotalPaise));

					chrgRemDuePaise = chrgInsClaimPaise - chrgRemTotalPaise;
					row.className = "edited";
					allocationsEdited++;

					if (chrgRemDuePaise == 0) {
						chIndex++;
						if(chIndex < n){
							chRowIndex = chrgCheckArr[chIndex];
							chrgInsClaimPaise	= getIndexedPaise("insClaimAmt",chRowIndex);
							chrgRemTotalPaise	= getIndexedPaise("allocatedClaimAmt",chRowIndex);
							chargeId			= getIndexedValue("chargeId", chRowIndex);

							chrgRemDuePaise = chrgInsClaimPaise - chrgRemTotalPaise;
						}
					}

					var payrow = getPaymentRow(payRowIndex);

					setNodeText(payrow.cells[UNALLOC_COL], formatAmountPaise(paymentRowUnallocPaise));
					setHiddenValue(payRowIndex, "paymentUnallocAmount", formatAmountPaise(paymentRowUnallocPaise));

					setNodeText(payrow.cells[ALLOC_COL], formatAmountPaise(paymentRowTotalPaise - paymentRowUnallocPaise));
					setHiddenValue(payRowIndex, "paymentAllocAmount", formatAmountPaise(paymentRowTotalPaise - paymentRowUnallocPaise));

					payrow.className = "edited";
				}

			}else {
				chIndex++;
				if(chIndex < n){
					chRowIndex = chrgCheckArr[chIndex];
					chrgInsClaimPaise	= getIndexedPaise("insClaimAmt",chRowIndex);
					chrgRemTotalPaise	= getIndexedPaise("allocatedClaimAmt",chRowIndex);
					chargeId			= getIndexedValue("chargeId", chRowIndex);

					chrgRemDuePaise = chrgInsClaimPaise - chrgRemTotalPaise;
				}
			}

		}else {
			payRowIndex++;
			if (payRowIndex < payTabLen){
				paymentRowTotalPaise	= getIndexedPaise("paymentAmount", payRowIndex);
				paymentRowAllocPaise	= getIndexedPaise("paymentAllocAmount", payRowIndex);
				paymentRowUnallocPaise	= getIndexedPaise("paymentUnallocAmount", payRowIndex);
				paymentRef				= getIndexedValue("paymentReference", payRowIndex);
			}
		}
	}
	recalPaymentRefTotals();
	return false;
}

function validateChargeCheck() {
	var chrgCheckElmts = document.mainform.chargeCheck;
	if (chrgCheckElmts.length == undefined && chrgCheckElmts.checked)
		return true;
	else {
		for(var i=0;i<chrgCheckElmts.length;i++) {
			if (chrgCheckElmts[i].checked)
				return true;
		}
	}
	return false;
}

function validateNewPayments() {
	if (paymentsAdded > 0 || paymentsEdited > 0) {
		alert("New payment references have been added or edited. Please save before allocation.");
		return false;
	}
	return true;
}

function validateAllocations() {
	if (allocationsEdited > 0) {
		alert("Charge allocations have been edited. Please save before adding or editing payment references.");
		return false;
	}
	return true;
}

function validatePaymentAmounts() {
	for (var i=0;i<getPaymentRefTabRows();i++) {
		if (!validateAmount(getIndexedFormElement(document.mainform, "paymentUnallocAmount", i), "Unallocated payment must be a valid amount"))
			return false;
	}
	return true;
}


function validateTotalRemittance() {
	for (var i=0;i<getChargesTabRows();i++) {
		var insAmtObj	= getIndexedFormElement(document.mainform, "insClaimAmt", i);
		var allocAmtObj = getIndexedFormElement(document.mainform, "allocatedClaimAmt", i);

		if (getPaise(insAmtObj.value) != getPaise(allocAmtObj.value))
			return false;
	}
	return true;
}

function validatePatientPaymentStatus() {
	document.mainform.paymentForceClose.value = "N";

	if (document.mainform.paymentStatus.value == 'U')
		return true;

	var depositSetOff = getPaise(billDeposits);

	totInsAmtPaise = 0;
	totPatientAmtPaise = 0;
	for (var i=0;i<getChargesTabRows();i++) {
		totInsAmtPaise	+= getIndexedPaise("insClaimAmt", i);
		totPatientAmtPaise  += getIndexedPaise("patientAmt", i);
	}

	var existingReceiptAmt = getPaise(existingReceipts);

	if (Math.abs(totPatientAmtPaise) <= Math.abs(existingReceiptAmt))
		return true;

	if (document.mainform.billStatus.value == "C") {

		if (billType == 'C' && writeOffAmountRights == 'A') {
			var ok = confirm("Warning: total bill amount and patient paid amounts do not match.\n" +
					"The balance amount will be considered as Write-Off. Do you want to proceed?");
			if (!ok) {
				return false;
			} else {
				document.mainform.paymentForceClose.value = "Y";
				if ((null != document.mainform.billRemarks) &&
						(trimAll(document.mainform.oldRemarks.value) ==
						 trimAll(document.mainform.billRemarks.value))) {
					alert("Enter a valid reason for write offs.");
					document.mainform.billRemarks.focus();
					document.mainform.paymentForceClose.value = "N";
					return false;
				}
			}
		} else if (billType == 'C') {
			alert("Total bill amount and patient paid amounts do not match.\n"+
					"You are not authorized to Write-Off the balance amount.");
			document.mainform.paymentForceClose.value = "N";
			return false;

		} else if (totPatientAmtPaise != existingReceiptAmt) {
			// write-off not allowed for prepaid bills at all
			alert("Total bill amount and patient paid amounts do not match.\n"+
					"Cannot close the bill. Patient due exists.");
			document.mainform.paymentForceClose.value = "N";
			return false;
		}
	}
	return true;
}

function validateRemittanceSponsorPayment() {
	document.mainform.claimForceClose.value = "N";
	var claimStatus = document.mainform.primaryClaimStatus;
	if (claimStatus == null || claimStatus.value != 'C')
		return true;

	var existingReceipts = getPaise(existingSponsorReceipts);

	totInsAmtPaise = 0;
	totRemAmtPaise = 0;
	for (var i=0;i<getChargesTabRows();i++) {
		totInsAmtPaise	+= getIndexedPaise("insClaimAmt", i);
		totRemAmtPaise  += getIndexedPaise("allocatedClaimAmt", i);
	}

	if (totInsAmtPaise == totRemAmtPaise)
		return true;

	if (writeOffAmountRights == 'A') {
		var ok = confirm("Warning: total claim amount and TPA paid amounts do not match.\n" +
				"The balance amount will be considered as TPA Write-Off. Do you want to proceed?");
		if (!ok) {
			return false;
		} else {
			document.mainform.claimForceClose.value = "Y";
			if ((null != document.mainform.billRemarks) &&
					(trimAll(document.mainform.oldRemarks.value) ==
					 trimAll(document.mainform.billRemarks.value))) {
				alert("Enter a valid reason for write offs.");
				document.mainform.billRemarks.focus();
				document.mainform.claimForceClose.value = "N";
				return false;
			}
		}
	} else {
		alert("Total claim amount and TPA paid amounts do not match.\n"+
				"You are not authorized to Write-Off the balance amount.");
		document.mainform.claimForceClose.value = "N";
		return false;
	}
	return true;
}

function validateBillAndClaimStatus() {
	valid = true;
	valid = valid && validateBillOpen();
	valid = valid && validatePatientPaymentStatus();
	valid = valid && validateRemittanceSponsorPayment();
	valid = valid && validateSponsorBillClose();
	valid = valid && validateFinalizedDateAndTime();
	valid = valid && checkFinalization();
	return valid;
}

function validateBillOpen() {
	var billStatus = document.mainform.billStatus.value;
	if (billStatus == 'A') {
		alert("Bill is Open. Please finalize and allocate remittance amount.");
		document.mainform.billStatus.focus();
		return false;
	}
	return true;
}

function saveRemittance() {
	if (!validatePaymentAmounts()) {
		return false;
	}

	if (!validateBillAndClaimStatus())
		return false;

	if (!validateTotalRemittance()) {
		var ok = confirm("Warning: total remittance amount and claim amounts \n"+
					" of some charges do not match. Do you want to proceed?");
		if (!ok) return false;
	}

	mainform.submit();
	return true;
}
