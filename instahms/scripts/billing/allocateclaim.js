var cform = null;
var editform = null;
var statusList = null;
function init() {
	cform = document.allocateClaimForm;
	editform = document.editBillForm;

	var i=0;
	BILLNO_COL=i++; MRNO_COL = i++; VISITID_COL = i++; PATNAME_COL = i++; OPENDATE_COL = i++; FINALDATE_COL = i++; REMARKS_COL = i++;
	CLAIMAMT_COL = i++; RECVDAMT_COL = i++; ALLOCAMT_COL = i++; BILLSTATUS_COL = i++; EDIT_COL = i++;

	// applicable status values for the status dropdown
	if(cancelBillRights == 'A' || roleId == 1 || roleId ==2) {
		if(origStatus == 'O') {
			statusList = [{status:'O',value:1,text:'Open'},
					 	 {status:'S',value:2,text:'Sent'},
					 	 {status:'X',value:3,text:'Cancelled'}];
		}else{
			statusList = [{status:'S',value:1,text:'Sent'},
						  {status:'R',value:2,text:'Received'},
						  {status:'C',value:3,text:'Closed'}];
		}
	} else {
		if(origStatus == 'O') {
			statusList = [{status:'O',value:1,text:'Open'},
						  {status:'S',value:2,text:'Sent'}];
		}else {
			statusList = [{status:'S',value:1,text:'Sent'},
						  {status:'R',value:2,text:'Received'},
						  {status:'C',value:3,text:'Closed'}];
		}
	}
	initEditAmountDialog();
	recalcAllocatedAmount();
	recalcMaxAmount();
	loadStatus();
	printReceipt();
}

/*
 * Forward only status changes.
 */
function loadStatus() {
	var statusObj = cform.status;
	var len = 0;
	var currentStatus = 0;

	for (var i=0;i<statusList.length; i++) {
		if(statusList[i].status == origStatus) {
			currentStatus = statusList[i].value;
		}
	}

	statusObj.length = 0;
	for(var i=0;i<statusList.length; i++) {
		if(statusList[i].value >= currentStatus) {
			statusObj.length = len + 1;
			var option  = new Option(statusList[i].text, statusList[i].status);
			statusObj[len] = option;
			len = len+1;
		}
	}
	if(statusObj.type !='hidden') setSelectedIndex(statusObj,origStatus);
	if(origStatus == 'O' && cform.claim_date != null)  cform.claim_date.readOnly = false; else cform.claim_date.readOnly = true;
	if(origStatus == 'S' && cform.sent_date != null)  cform.sent_date.readOnly = false; else cform.sent_date.readOnly = true;
	if(origStatus == 'C' && cform.closed_date != null)  cform.closed_date.readOnly = false; else cform.closed_date.readOnly = true;
}

function validateStatusDates() {
	var statusObj = cform.status;
	if(statusObj.value == 'O') {
		if(cform.claim_date && trimAll(cform.claim_date.value) == '') {
			alert("Enter claim date");
			cform.claim_date.focus();
			return false;
		}
	}else if(statusObj.value == 'S') {
		if(cform.sent_date && trimAll(cform.sent_date.value) == '') {
			alert("Enter sent date");
			cform.sent_date.focus();
			return false;
		}
	}else if(statusObj.value == 'C') {
		if(cform.closed_date && trimAll(cform.closed_date.value) == '') {
			alert("Enter closed date");
			cform.closed_date.focus();
			return false;
		}
	}else if(statusObj.value == 'X') {
		if(cform.cancel_reason && trimAll(cform.cancel_reason.value) == '') {
			alert("Enter cancelled reason");
			cform.cancel_reason.focus();
			return false;
		}
	}

	return true;
}

function onChangeStatus() {
	var statusObj = cform.status;

	if(cform.claim_date != null) {
		if(statusObj.value == 'O') {
			cform.claim_date.readOnly = false;
			cform.sent_date.readOnly = true;
			cform.closed_date.readOnly = true;

			cform.claim_date.value = clmDate==''?currDate:clmDate;
			cform.sent_date.value = sntDate;
			cform.closed_date.value = clsdDate;

		}else if(statusObj.value == 'S') {
			cform.claim_date.readOnly = true;
			cform.sent_date.readOnly = false;
			cform.closed_date.readOnly = true;

			cform.claim_date.value = clmDate;
			cform.sent_date.value = sntDate==''?currDate:sntDate;
			cform.closed_date.value = clsdDate;

		}else if(statusObj.value == 'C') {
			cform.claim_date.readOnly = true;
			cform.sent_date.readOnly = true;
			cform.closed_date.readOnly = false;

			cform.claim_date.value = clmDate;
			cform.sent_date.value = sntDate;
			cform.closed_date.value = clsdDate==''?currDate:clsdDate;

		}else {
			cform.claim_date.readOnly = true;
			cform.sent_date.readOnly = true;
			cform.closed_date.readOnly = true;

			cform.claim_date.value = clmDate;
			cform.sent_date.value = sntDate;
			cform.closed_date.value = clsdDate;
		}
	}else {
		if(statusObj.value == 'O') {
			document.getElementById("claimDt").textContent = clmDate==''?currDate:clmDate;
			document.getElementById("sentDt").textContent = sntDate;
			document.getElementById("closeDt").textContent = clsdDate;
		}else if(statusObj.value == 'S') {
			document.getElementById("claimDt").textContent = clmDate;
			document.getElementById("sentDt").textContent = sntDate==''?currDate:sntDate;
			document.getElementById("closeDt").textContent = clsdDate;
		}else if(statusObj.value == 'C') {
			document.getElementById("claimDt").textContent = clmDate;
			document.getElementById("sentDt").textContent = sntDate;
			document.getElementById("closeDt").textContent = clsdDate==''?currDate:clsdDate;
		}else {
			document.getElementById("claimDt").textContent = clmDate;
			document.getElementById("sentDt").textContent = sntDate;
			document.getElementById("closeDt").textContent = clsdDate;
		}
	}
}


function printReceipt() {
	if ( sReceiptNo == null || sReceiptNo == '' )
		return false;
	var printerId = cform.printerType.value;
	window.open("../../pages/BillDischarge/allocateSponsorBill.do?_method=sponsorReceiptPrint&sponsorReceiptNo="
		+sReceiptNo+"&printerType="+printerId);
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(cform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(cform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function initEditAmountDialog() {
	var dialogDiv = document.getElementById("editAmountDialog");
	dialogDiv.style.display = 'block';
	editAmountDialog = new YAHOO.widget.Dialog("editAmountDialog",{
			width:"720px",
			text: "Edit Amount",
			close: false,
			context :["billListTab", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onEditCancel,
	                                                scope:editAmountDialog,
	                                                correctScope:true } );
	editAmountDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editAmountDialog.render();
}

function showEditAmountDialog(id) {
	var row = document.getElementById("billRow"+id);

	YAHOO.util.Dom.addClass(row, 'rowUnderEdit');
	editform.editRowId.value = id;

	document.getElementById("eBillNo").textContent = row.cells[BILLNO_COL].textContent;
	document.getElementById("eMrNo").textContent = row.cells[MRNO_COL].textContent;
	document.getElementById("eVisitId").textContent = row.cells[VISITID_COL].textContent;
	document.getElementById("ePatientName").textContent = row.cells[PATNAME_COL].textContent;

	document.getElementById("eFinalizedDate").textContent = row.cells[FINALDATE_COL].textContent;
	document.getElementById("eBillClaimAmt").textContent = row.cells[CLAIMAMT_COL].textContent;
	document.getElementById("eBalanceDue").textContent =
				formatAmountPaise((getPaise(row.cells[CLAIMAMT_COL].textContent) - getPaise(row.cells[ALLOCAMT_COL].textContent)));

	editform.eAllocatedAmt.value = formatAmountValue(row.cells[ALLOCAMT_COL].textContent);
	editform.eRemarks.value = getIndexedValue("billRemarks", id);
	if(getIndexedValue("closeBill", id) == 'C') {
		editform.eCloseBill.checked = true;
	}else {
		editform.eCloseBill.checked = false;
	}

	var button = document.getElementById("editBtn"+id);

	editAmountDialog.cfg.setProperty("context", [button, "tr", "bl"], false);
	editAmountDialog.show();
	editform.eRemarks.focus();
	return false;
}

function onEditSubmit() {
	if (!validateAmount(editform.eAllocatedAmt, "Allocated amt must be a valid amount"))
		return false;

	var id = editform.editRowId.value;
	var row = document.getElementById("billRow"+id);
	recalcAllocatedAmount();

	var totalClaimAmt = row.cells[CLAIMAMT_COL].textContent;

	if(!validateAllocatedAmt()) return false;

	var amt = editform.eAllocatedAmt.value;
	var remarks = editform.eRemarks.value;
	var close = editform.eCloseBill;

	setIndexedValue("allocatedAmt",id, formatAmountValue(amt));
	setNodeText(row.cells[ALLOCAMT_COL], formatAmountValue(amt));
	setIndexedValue("billRemarks", id, remarks);
	setNodeText(row.cells[REMARKS_COL], remarks, 20);
	if(close.checked) {
		if(trimAll(remarks) == '') {
			alert("Please enter closing remarks.");
			return false;
		}
		setIndexedValue("closeBill", id, 'C');
		setNodeText(row.cells[BILLSTATUS_COL], "Closed");
	}else {
		setIndexedValue("closeBill", id, getIndexedValue("oldBillStatus",id));
		setNodeText(row.cells[BILLSTATUS_COL], getIndexedValue("oldBillStatusText",id));
	}
	recalcAllocatedAmount();
	YAHOO.util.Dom.removeClass(row, 'rowUnderEdit');
	editAmountDialog.hide();
	var editImg = row.cells[EDIT_COL].childNodes[1];
	editImg.focus();
}

function onEditCancel(){
	var id = editform.editRowId.value;
	var row = document.getElementById("billRow"+id);
	YAHOO.util.Dom.removeClass(row, 'rowUnderEdit');
	editAmountDialog.hide();
	var editImg = row.cells[EDIT_COL].childNodes[1];
	editImg.focus();
}

function recalcAllocatedAmount() {
	var rows = document.getElementById("billListTab").rows.length - 3;
	var totRcvdamt = 0;
	for(var i=0; i<rows;i++) {
		var row = document.getElementById("billRow"+i);
		totRcvdamt = totRcvdamt + getPaise(row.cells[ALLOCAMT_COL].textContent);
	}
	document.getElementById("recdAmt").textContent = formatAmountPaise(totRcvdamt);
}

function recalcMaxAmount() {
	var rows = document.getElementById("billListTab").rows.length - 3;
	var amt = getPaise(document.getElementById("maxAmt").textContent);
	for(var i=0; i<rows;i++) {
		var row = document.getElementById("billRow"+i);
		if(document.getElementById("oldBillStatus"+i).value == 'C') {
			amt = amt - getPaise(row.cells[ALLOCAMT_COL].textContent);
		}
	}
	document.getElementById("maxAmt").textContent = formatAmountPaise(amt);
}

function validateTdsAmt() {
	if(!numberCheck(cform.totTdsAmt)){
		cform.totTdsAmt.focus();
		return false;
	}
	var tdsamt = getPaise(cform.totTdsAmt.value);
	var payAmt = getPaise(cform.totPayingAmt.value);
	if(tdsamt > payAmt) {
		alert("TDS amount is more than received amount");
		cform.totTdsAmt.focus();
		return false;
	}
	return setMaxAmt();
}

function setMaxAmt() {
	if(!numberCheck(cform.totPayingAmt)){
		cform.totPayingAmt.focus();
		return false;
	}
	var maxamtObj = document.getElementById("maxAmt");
	var amt  = getPaise(cform.totalAmtReceived.value) + getPaise(cform.totalTdsReceived.value)
			  			+ getPaise(cform.totPayingAmt.value) + getPaise(cform.totTdsAmt.value);

	maxamtObj.textContent = formatAmountPaise(amt);
	recalcMaxAmount();
	return true;
}

function validateAndAllocateMaxAmt() {
	var rows = document.getElementById("billListTab").rows.length - 3;
	var sbillTotalamt = 0;
	var unclosedBillsTotalamt = 0;
	var maxamount = getPaise(document.getElementById("maxAmt").textContent);

	for(var i=0; i<rows;i++) {
		var row = document.getElementById("billRow"+i);
		sbillTotalamt = sbillTotalamt + getPaise(row.cells[CLAIMAMT_COL].textContent);
	}

	for(var i=0; i<rows;i++) {
		var row = document.getElementById("billRow"+i);
		if(document.getElementById("oldBillStatus"+i).value != 'C') {
			unclosedBillsTotalamt = unclosedBillsTotalamt + getPaise(row.cells[CLAIMAMT_COL].textContent);
		}
	}

	if(maxamount == 0) {
		return false;
	}
	if(maxamount > unclosedBillsTotalamt) {
		var diff = formatAmountPaise(maxamount - unclosedBillsTotalamt);
		alert("Please check received amount is more than total bills claim amount.\n" +
				"Reduce the total allocated amount by " + diff );
		cform.totPayingAmt.focus();
		return false;
	}

	if(maxamount == unclosedBillsTotalamt) {
		for(var i=0; i<rows;i++) {
			var row = document.getElementById("billRow"+i);
			if(document.getElementById("oldBillStatus"+i).value != 'C') {
				row.cells[ALLOCAMT_COL].textContent = row.cells[CLAIMAMT_COL].textContent;
				document.getElementById("allocatedAmt"+i).value = row.cells[CLAIMAMT_COL].textContent;
			}
		}
	}

	if(maxamount < unclosedBillsTotalamt) {
		var ok = confirm("Received amount is insufficient to allocate to all bills.\n Do you want to allocate to some bills?");
		if(ok) allocateAmtToBills();
		else return false;
	}
	recalcAllocatedAmount();
	setMaxAmt();
}

function allocateAmtToBills() {
	var rows = document.getElementById("billListTab").rows.length - 3;
	var maxamount = getPaise(document.getElementById("maxAmt").textContent);
	for(var i=0; i<rows;i++) {
		var row = document.getElementById("billRow"+i);
		if(document.getElementById("oldBillStatus"+i).value != 'C') {
			//Deduct allocated amount for unclosed bills from max amt
			maxamount = maxamount - getPaise(row.cells[ALLOCAMT_COL].textContent);
		}
	}

	var j = 0;
	for(var i=0; i<rows;i++) {
		var row = document.getElementById("billRow"+i);
		if(document.getElementById("oldBillStatus"+i).value != 'C') {
			//if max amt more than claim amt then allocate
			if(maxamount >= getPaise(row.cells[CLAIMAMT_COL].textContent)) {
				document.getElementById("allocatedAmt"+i).value = formatAmountValue(row.cells[CLAIMAMT_COL].textContent);
				row.cells[ALLOCAMT_COL].textContent = formatAmountValue(row.cells[CLAIMAMT_COL].textContent);
				maxamount = maxamount - getPaise(row.cells[CLAIMAMT_COL].textContent);
			}else {
				// Get the last allocated row for which remaining max amount is insufficient to allocate.
				j = i;
				break;
			}
		}
	}
	var row = document.getElementById("billRow"+j);
	if(document.getElementById("oldBillStatus"+j).value != 'C') {
		//if max amt more than claim amt then allocate
		if(maxamount >= getPaise(row.cells[CLAIMAMT_COL].textContent)) {
			document.getElementById("allocatedAmt"+j).value = formatAmountValue(row.cells[CLAIMAMT_COL].textContent);
			row.cells[ALLOCAMT_COL].textContent = formatAmountValue(row.cells[CLAIMAMT_COL].textContent);
			maxamount = maxamount - getPaise(row.cells[CLAIMAMT_COL].textContent);
		}else{
			document.getElementById("allocatedAmt"+j).value = formatAmountPaise(maxamount);
			row.cells[ALLOCAMT_COL].textContent = formatAmountPaise(maxamount);
			maxamount = 0;
		}
	}
	document.getElementById("maxAmt").textContent = formatAmountPaise(maxamount);
}

function validatePayments() {
	if(cform.paymentDate != null) {
		var payDateObj = cform.paymentDate;
		var payTimeObj = cform.paymentTime;

		var valid = true;

		valid = valid && validateRequired(payDateObj, "Payment  date is required");
		valid = valid && validateRequired(payDateObj, "Payment  time is required");
		valid = valid && doValidateDateField(payDateObj,"past");
		if(!valid) return false;
		valid = valid && validateTime(payTimeObj);
		if(!valid) return false;

		var amt = cform.totPayingAmt;
		var tds = cform.totTdsAmt;

		if(trimAll(amt.value) != '') {
			if(!validateTdsAmt()) return false;
			if (!validatePayment()) return false;
		}
	}
	return true;
}

function closeAllBills() {
	var closeCheck =  cform.closeAll;
	var rows = document.getElementById("billListTab").rows.length - 3;
	if(closeCheck.checked) {
		for(var i=0; i<rows;i++) {
			var row = document.getElementById("billRow"+i);
			setIndexedValue("closeBill", i, 'C');
			setNodeText(row.cells[BILLSTATUS_COL], 'Closed');
		}
	}else {
		for(var i=0; i<rows;i++) {
			var row = document.getElementById("billRow"+i);
			setIndexedValue("closeBill", i, getIndexedValue("oldBillStatus",i));
			setNodeText(row.cells[BILLSTATUS_COL], getIndexedValue("oldBillStatusText",i));
		}
	}
}

function checkBillCloseStatus() {
	var rows = document.getElementById("billListTab").rows.length - 3;
	for(var i=0; i<rows;i++) {
		if(document.getElementById("closeBill"+i).value == 'C' &&
			document.getElementById("oldBillStatus"+i).value != 'C') return true;
	}
	return false;
}

var closingBillsClaimAmt = 0;
var closingBillsAllocatedAmt = 0;

function getClosingBillsTotalAmt() {
	var rows = document.getElementById("billListTab").rows.length - 3;
	for(var i=0; i<rows;i++) {
		if(document.getElementById("closeBill"+i).value == 'C' &&
			document.getElementById("oldBillStatus"+i).value != 'C') {
			 closingBillsClaimAmt += getElementIdPaise("claimAmt"+i);
			 closingBillsAllocatedAmt += getElementIdPaise("allocatedAmt"+i);
		}
	}
}

function validatePaidAmounts() {
	var newBillStatus = cform.status.value;

	var paidAmt = getPaise(document.getElementById("maxAmt").textContent);
	var recdAmt = getPaise(document.getElementById("recdAmt").textContent);
	var claimAmt = getPaise(cform.totalClaimAmt.value);

	var totRecvdAmt = cform.totalAmtReceived.value;
	var totTdsAmt = cform.totalTdsReceived.value;

	var closedBillTotal = getPaise(document.getElementById("closedBillsAmt").textContent);

	paidAmt = paidAmt + closedBillTotal;

	if (newBillStatus == "C" || cform.closeAll.checked) {

		if((recdAmt < paidAmt) || (claimAmt > paidAmt)){

			if(writeOffAmountRights == 'A' || roleId == 1 || roleId == 2) {

				var ok = confirm("Warning: Total bill allocated amount and total paid/claim amounts do not match.\n" +
						"The balance amount will be considered as TPA/Sponsor Write-Off and Bill(s) will be closed. Do you want to proceed?");
				if (!ok)
					return false;
				else {
					if ( (null != cform.remarks) &&
					(('' == trimAll(cform.remarks.value)) ||
					(trimAll(cform.oldRemarks.value) == trimAll(cform.remarks.value)))) {
						alert("Enter a valid reason for closing bill.");
						cform.remarks.focus();
						return false;
					}
				}
			}else {
				alert("Total bill allocated amount and paid/claim amounts do not match.\n"+
					  "You are not authorized to Write-Off the balance amount, please contact the administrator.");
				return false;
			}
		}
	}

	var anyBillClosed = checkBillCloseStatus();

	if (anyBillClosed) {

		getClosingBillsTotalAmt();

		if(closingBillsClaimAmt > closingBillsAllocatedAmt) {
			if(writeOffAmountRights == 'A' || roleId == 1 || roleId == 2) {

				var ok = confirm("Warning: Total bill allocated amount and total paid/claim amounts do not match.\n" +
						"The balance amount will be considered as TPA/Sponsor Write-Off and Bill(s) will be closed. Do you want to proceed?");
				if (!ok)
					return false;
				else {
					var rows = document.getElementById("billListTab").rows.length - 3;
					for(var i=0; i<rows;i++) {
						if(document.getElementById("closeBill"+i).value == 'C' &&
							document.getElementById("oldBillStatus"+i).value != 'C') {
							var billRemarksObj = document.getElementById("billRemarks"+i);
							if ( null != billRemarksObj && ('' == trimAll(billRemarksObj.value))) {
								alert("Enter a valid reason for closing bill.");
								showEditAmountDialog(i);
								editform.eRemarks.focus();
								return false;
							}
						}
					}
				}
			}else {
				alert("Total bill allocated amount and paid/claim amounts do not match.\n"+
					  "You are not authorized to Write-Off the balance amount, please contact the administrator.");
				return false;
			}
		}
	}

	if (recdAmt > paidAmt) {
		var diff = formatAmountPaise(paidAmt - recdAmt);
		alert("Total amount allocated cannot be more than Max Amount.\n" +
				"Reduce the total allocated amount by " + diff );
		return false;
	}

	if(paidAmt > claimAmt) {
		var diff = formatAmountPaise(paidAmt - claimAmt);
		alert("Amount to be allocated cannot be more than total claim amount.\n" +
				"Reduce the amount " + diff );
		return false;
	}

	return true;
}


function validateBillsForm() {
	if(!validateStatusDates()) return false;
	if(!validatePayments()) return false;
	if(!validatePaidAmounts()) return false;
	cform.submit();
}


function validateAllocatedAmt() {
	if(getPaise(editform.eAllocatedAmt.value) > getPaise(document.getElementById("eBillClaimAmt").textContent)) {
		alert("Allocated amount is more than claim amount");
		editform.eAllocatedAmt.focus();
		return false;
	}
	return true;
}
