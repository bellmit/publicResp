var mainform = null;
var claimEditForm = null;
var itemsEdited = 0;

var totalAmountPaise = 0;
var totalDiscountPaise = 0;
var totalGrossAmountPaise = 0;
var totalPatientAmountPaise = 0;
var totalClaimNetAmountPaise = 0;

function initForm() {
	mainform = document.mainform;
	claimEditForm = document.claimeditform;

	ITEM_RATE_COL = 7;
	ITEM_QTY_COL = 8;
	ITEM_DISC_COL = 9;
	ITEM_AMOUNT_COL = 10;
	ITEM_PATIENT_AMT_COL = 11;
	ITEM_CLAIM_AMT_COL = 12;
	ITEM_EDIT_COL = 13;
	ITEM_RECD_AMT_COL = 14;
	ITEM_HIST_COL = 23;

	initClaimEditDialog();

	document.getElementById("_comments").value = UnFormatTextAreaValue(document.getElementById("_comments").value);
	initInsurancePhotoDialog();
	disableOrEnableMarkForResub();
	
	initClaimHistoryDialog();
}

function disableOrEnableMarkForResub() {
	if(actualClaimStatus == 'Denied' || claimStatus == 'M') {
		var resubActionCheckObj = document.getElementById("resubActionChk");
		resubActionCheckObj.disabled= false;
	}
}

function initClaimEditDialog() {
	var dialogDiv = document.getElementById("claimEditDialog");
	dialogDiv.style.display = 'block';
	editItemAmountDialog = new YAHOO.widget.Dialog("claimEditDialog", {
		width: "700px",
		text: "Edit Item Amount",
		context: ["billsTable", "tl", "tl"],
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: onEditItemAmountCancel,
		scope: editItemAmountDialog,
		correctScope: true
	});
	editItemAmountDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editItemAmountDialog.render();
}

var claimHistoryDialog;
function initClaimHistoryDialog() {
	var historyDialog = document.getElementById("claimHistoryDialog");
	historyDialog.style.display = 'block';
	claimHistoryDialog = new YAHOO.widget.Dialog("claimHistoryDialog", {
		width: "1150px",
		text: "View Claim History",
		context: ["billsTable", "tl", "tl"],
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: onViewClaimHistryCancel,
		scope: claimHistoryDialog,
		correctScope: true
	});
	claimHistoryDialog.cfg.queueProperty("keylisteners", escKeyListener);
	claimHistoryDialog.cancelEvent.subscribe(onViewClaimHistryCancel);
	claimHistoryDialog.render();
	
}

function onEditItemAmountCancel() {
	var id = claimEditForm.claimEditRowId.value;
	var row = getItemRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	editItemAmountDialog.hide();
	var editImg = row.cells[ITEM_EDIT_COL].childNodes[1];
	editImg.focus();
}

function onViewClaimHistryCancel() {
	var clTable = document.getElementById("claimHistTblId");
	
	for (var i=1; i<clTable.rows.length-1;) {
		clTable.deleteRow(i);
	}
	
	claimHistoryDialog.hide();
}

function showNextEditItemDialog() {
	var id = claimEditForm.claimEditRowId.value;
	if (!onEditItemAmountSubmit())
		return false;

	var row = getItemRow(id);
	var nRow = YAHOO.util.Dom.getNextSibling(row);
	if (nRow != null) {
		YAHOO.util.Dom.removeClass(row, 'editing');
		var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[ITEM_EDIT_COL]);
		if (!empty(anchor))
			showEditItemDialog(anchor);
		else {
			id++;
			claimEditForm.claimEditRowId.value = id;
			showNextEditItemDialog();
		}
	}
}

function showPreviousEditItemDialog() {
	var id = claimEditForm.claimEditRowId.value;
	if (!onEditItemAmountSubmit())
		return false;

	var row = getItemRow(id);
	var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
	var nPrevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
	if (nPrevRow != null) {
		YAHOO.util.Dom.removeClass(row, 'editing');
		var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[ITEM_EDIT_COL]);
		if (!empty(anchor))
			showEditItemDialog(anchor);
		else {
			id--;
			claimEditForm.claimEditRowId.value = id;
			showPreviousEditItemDialog();
		}
	}
}

function showEditItemDialog(obj) {

	var row = getThisRow(obj);
	if (row == null) return false;
	if (document.getElementById("rowIndexValue" + (row.rowIndex)) == null)
		return false;

	var id = document.getElementById("rowIndexValue" + (row.rowIndex)).value;

	YAHOO.util.Dom.addClass(row, 'editing');
	claimEditForm.claimEditRowId.value = id;

	editItemAmountDialog.cfg.setProperty("context", [row.cells[ITEM_EDIT_COL], "tr", "bl"], false);
	
	setItemDetails();
	editItemAmountDialog.show();
	return false;
}

function setItemDetails() {
	var id = claimEditForm.claimEditRowId.value;

	var amountObj = document.getElementById("item_amount" + id);
	var origAmountObj = document.getElementById("orig_item_amount" + id);
	var chargeHeadObj = document.getElementById("charge_head" + id);

	var qtyObj = document.getElementById("item_qty" + id);
	var pkgUnitObj = document.getElementById("item_package_unit" + id);
	var rateObj = document.getElementById("item_rate" + id);
	var origRateObj = document.getElementById("orig_item_rate" + id);
	var discObj = document.getElementById("item_disc" + id);
	var recdAmtObj = document.getElementById("claim_recd_amount" + id);

	var patAmtObj = document.getElementById("patient_amount" + id);
	var origClaimAmtObj = document.getElementById("orig_claim_net_amount" + id);
	var claimAmtObj = document.getElementById("claim_net_amount" + id);

	var claimStatusObj = document.getElementById("bill_claim_status" + id);
	var billStatusObj = document.getElementById("bill_status" + id);
	var actClaimStatusObj = document.getElementById("activity_claim_status" + id);
	var denialAcceptedObj = document.getElementById("item_denial_accepted" + id);
	var rejectionReasonObj = document.getElementById("item_rej_reason" + id);

	var groupObj = document.getElementById("charge_group_name" + id);
	var headObj = document.getElementById("charge_head_name" + id);
	var chargeHeadObj = document.getElementById("charge_head" + id);
	var activityObj = document.getElementById("activity_name" + id);

	var editQtyObj = document.getElementById("claim_item_qty");
	var editPkgUnitObj = document.getElementById("claim_item_pkg_unit");
	var editRateObj = document.getElementById("claim_item_rate");
	var editDiscObj = document.getElementById("claim_item_disc");
	var editAmtObj = document.getElementById("claim_item_amount");
	var editPatAmtObj = document.getElementById("claim_item_pat_amount");
	var editclaimObj = claimEditForm.claim_item_net_amount;
	var editDenialChkObj = claimEditForm.denialCheck;
	var editRejReasonDnObj = claimEditForm.rejection_reasons_drpdn;

	var editOrigRateObj = document.getElementById("claim_item_orig_rate");
	var editOrigAmtObj = document.getElementById("claim_item_orig_amount");
	var editOrigClaimNetObj = document.getElementById("claim_item_orig_net_amount");

	var editGroupObj = document.getElementById("claim_item_group");
	var editHeadObj = document.getElementById("claim_item_head");
	var editActivityObj = document.getElementById("claim_item_activity");

	if (empty(amountObj)) {

		editGroupObj.textContent = "";
		editHeadObj.textContent = "";
		editActivityObj.textContent = "";

		editOrigRateObj.textContent = 0;
		editOrigAmtObj.textContent = 0;
		editOrigClaimNetObj.textContent = 0;

		editAmtObj.textContent = 0;
		editPkgUnitObj.textContent = "";
		editQtyObj.textContent = 0;
		editRateObj.textContent = 0;
		editDiscObj.textContent = 0;
		editPatAmtObj.textContent = 0;
		editclaimObj.value = 0;

		editclaimObj.readOnly = true;
		return;
	} else {

		editGroupObj.textContent = groupObj.value;
		editHeadObj.textContent = headObj.value;
		editActivityObj.textContent = activityObj.value;

		editOrigRateObj.textContent = origRateObj.value;
		editOrigAmtObj.textContent = origAmountObj.value;
		editOrigClaimNetObj.textContent = origClaimAmtObj.value;

		editAmtObj.textContent = amountObj.value;
		editQtyObj.textContent = qtyObj.value;
		if (chargeHeadObj.value == 'PHCMED' || chargeHeadObj.value == 'PHMED')
			editPkgUnitObj.innerHTML = " (<span style='font-weight: normal;'>Pkg Unit:</span>  "+pkgUnitObj.value+")";
		else
			editPkgUnitObj.textContent = "";
		editRateObj.textContent = rateObj.value;
		editDiscObj.textContent = discObj.value;
		editPatAmtObj.textContent = patAmtObj.value;
		editclaimObj.value = claimAmtObj.value;

		var chargeHead = chargeHeadObj.value;

		if (billStatusObj.value == 'F' && (actClaimStatusObj.value == 'O' || actClaimStatusObj.value == 'D')
				&& (actualClaimStatus == 'Denied' || actualClaimStatus == 'ForResub.')) {
			if (chargeHead == 'MARPKG') {
				editclaimObj.readOnly = true;

			} else if (usesDRG == 'Y' && chargeHead != 'MARDRG' && chargeHead != 'OUTDRG' && chargeHead != 'APDRG' && chargeHead != 'BPDRG') {
				editclaimObj.readOnly = true;

			} else if (usesPerdiem == 'Y' && chargeHead != 'MARPDM') {
				editclaimObj.readOnly = true;

			} else
				editclaimObj.readOnly = false;
		} else
			editclaimObj.readOnly = true;
		if(editDenialChkObj != null) {
			if(denialAcceptedObj.value == 'M' || denialAcceptedObj.value == 'D')
				editDenialChkObj.checked = true;
			else
				editDenialChkObj.checked = false;

			editRejReasonDnObj.value = rejectionReasonObj.value;

			// Denial acceptance check box and rejection reason drop down list should be enabled only for denied activities.

			if(actClaimStatusObj.value == 'D') {
				editDenialChkObj.disabled = false;
				if(editDenialChkObj.checked)
					editRejReasonDnObj.disabled = false;
				else
					editRejReasonDnObj.disabled = true;
			} else {
				editDenialChkObj.disabled = true;
				editRejReasonDnObj.disabled = true;
			}
		// when claim is marked for resubmission/denial accepted status is D or claim is closed or user dont
		// have action rights then denial accepted check box gets disabled.
			if((denialAcceptedObj.value != null && denialAcceptedObj.value == 'D') || allowDenialAccepted != 'true' || actualClaimStatus != 'Denied'){
				editDenialChkObj.disabled = true;
				editRejReasonDnObj.disabled = true;
			}
		}
	}
}

function recalcItemAmount() {

	var id = claimEditForm.claimEditRowId.value;
	var editclaimObj = claimEditForm.claim_item_net_amount;

	var editRateObj = document.getElementById("claim_item_rate");
	var editAmtObj = document.getElementById("claim_item_amount");

	if (editclaimObj != null)
		if (!validateAmount(editclaimObj, "Claim amount must be a valid amount"))
			return false;

	var amountObj = document.getElementById("item_amount" + id);
	var qtyObj = document.getElementById("item_qty" + id);
	var rateObj = document.getElementById("item_rate" + id);
	var discObj = document.getElementById("item_disc" + id);

	var pkgUnitObj = document.getElementById("item_package_unit" + id);
	var returnAmtObj = document.getElementById("item_return_amount" + id);

	var patAmtObj = document.getElementById("patient_amount" + id);
	var origClaimAmtObj = document.getElementById("orig_claim_net_amount" + id);
	var claimAmtObj = document.getElementById("claim_net_amount" + id);

	if (amountObj != null && editclaimObj != null) {

		var row = getThisRow(amountObj);

		var edited = true;
		if (getPaise(editclaimObj.value) == getPaise(claimAmtObj.value))
			edited = false;

		if (edited) {

			var origClaimAmountPaise = getPaise(origClaimAmtObj.value);
			var origRatePaise = getPaise(rateObj.value);
			var origAmountPaise = getPaise(amountObj.value);

			var patientPaise = getPaise(patAmtObj.value);
			var discountPaise = getPaise(discObj.value);
			var qty = getAmount(qtyObj.value) * getAmount(pkgUnitObj.value);

			var newClaimPaise = getPaise(editclaimObj.value);

			var newAmountPaise = patientPaise + newClaimPaise;
			var returnAmtPaise = getPaise(returnAmtObj.value);

			if (returnAmtPaise != 0 || patientPaise != 0) {
				var validAmountPaise = returnAmtPaise + patientPaise;

				if ((newClaimPaise < origClaimAmountPaise) && (newClaimPaise + returnAmtPaise + patientPaise) < 0) {
					alert("Claim amount cannot be less than amount : " + (formatAmountPaise(validAmountPaise)));
					editclaimObj.focus();
					return false;
				}
			}

			var amountDiffPaise = origAmountPaise - newAmountPaise;
			var newRatePaise = origRatePaise - (amountDiffPaise / qty);

			editRateObj.textContent = formatAmountPaise(newRatePaise);
			editAmtObj.textContent = formatAmountPaise(newAmountPaise);

			if (newRatePaise < 0) {
				alert("Rate cannot be negative.");
				editclaimObj.focus();
				return false;
			}
		}else {
			editRateObj.textContent = formatAmountPaise(getPaise(rateObj.value));
			editAmtObj.textContent = formatAmountPaise(getPaise(amountObj.value));
		}
	}
}

function onEditItemAmountSubmit() {

	var id = claimEditForm.claimEditRowId.value;
	var editclaimObj = claimEditForm.claim_item_net_amount;

	var editRateObj = document.getElementById("claim_item_rate");
	var editAmtObj = document.getElementById("claim_item_amount");

	if (editclaimObj != null)
		if (!validateAmount(editclaimObj, "Claim amount must be a valid amount"))
			return false;

	var amountObj = document.getElementById("item_amount" + id);
	var qtyObj = document.getElementById("item_qty" + id);
	var rateObj = document.getElementById("item_rate" + id);
	var discObj = document.getElementById("item_disc" + id);

	var pkgUnitObj = document.getElementById("item_package_unit" + id);
	var returnAmtObj = document.getElementById("item_return_amount" + id);

	var patAmtObj = document.getElementById("patient_amount" + id);
	var origClaimAmtObj = document.getElementById("orig_claim_net_amount" + id);
	var claimAmtObj = document.getElementById("claim_net_amount" + id);
	var denialAcceptedObj = document.getElementById("item_denial_accepted" + id);
	var orig_denialAcceptedObj = document.getElementById("orig_item_denial_accepted" + id);
	var rejectionReasonObj = document.getElementById("item_rej_reason" + id);
	var orig_rejectionReasonObj = document.getElementById("orig_item_rej_reason" + id);
	var editDenialChkObj = claimEditForm.denialCheck;
	var editRejReasonDnObj = claimEditForm.rejection_reasons_drpdn;

	var editedObj = document.getElementById("edited" + id);
	var claimAmtEditedObj = document.getElementById("claimAmtEdited" + id);

	if (amountObj != null && editclaimObj != null) {

		var row = getThisRow(amountObj);

		var edited = true;

		if(allowDenialAccepted && editDenialChkObj != null) {
			// validate denial accepted and rejection reasons.
			var success = validateDenailAcceptedAndRejReason(id);
			if(!success)
				return false;

			if(editDenialChkObj.checked)
				denialAcceptedObj.value = 'M';
			else
				denialAcceptedObj.value = 'O';

			if(denialAcceptedObj.value == 'M' && orig_denialAcceptedObj.value == 'D'){
				denialAcceptedObj.value = 'D';
			}

			rejectionReasonObj.value = editRejReasonDnObj.value;

		}

		var cls = "";
		if (getPaise(editclaimObj.value) == getPaise(claimAmtObj.value) &&
			(denialAcceptedObj.value == orig_denialAcceptedObj.value) &&
			(rejectionReasonObj.value == orig_rejectionReasonObj.value) )
			edited = false;

		if (edited) {

			cls = 'edited';
			if (getPaise(editclaimObj.value) == getPaise(claimAmtObj.value))
				claimAmtEditedObj.value = false;
			else
				claimAmtEditedObj.value = true;

			var origClaimAmountPaise = getPaise(origClaimAmtObj.value);
			var origRatePaise = getPaise(rateObj.value);
			var origAmountPaise = getPaise(amountObj.value);

			var patientPaise = getPaise(patAmtObj.value);
			var discountPaise = getPaise(discObj.value);
			var qty = getAmount(qtyObj.value) * getAmount(pkgUnitObj.value);

			var newClaimPaise = getPaise(editclaimObj.value);

			var newAmountPaise = patientPaise + newClaimPaise;
			var returnAmtPaise = getPaise(returnAmtObj.value);

			if (returnAmtPaise != 0 || patientPaise != 0) {
				var validAmountPaise = returnAmtPaise + patientPaise;

				if ((newClaimPaise < origClaimAmountPaise) && (newClaimPaise + returnAmtPaise + patientPaise) < 0) {
					alert("Claim amount cannot be less than amount : " + (formatAmountPaise(validAmountPaise)));
					editclaimObj.focus();
					return false;
				}
			}

			var amountDiffPaise = origAmountPaise - newAmountPaise;
			var newRatePaise = origRatePaise - (amountDiffPaise / qty);

			if (newRatePaise < 0) {
				alert("Rate cannot be negative.");
				editclaimObj.focus();
				return false;
			}

			setNodeText(row.cells[ITEM_RATE_COL], formatAmountPaise(newRatePaise));
			rateObj.value = formatAmountPaise(newRatePaise);

			setNodeText(row.cells[ITEM_AMOUNT_COL], formatAmountPaise(newAmountPaise));
			amountObj.value = formatAmountPaise(newAmountPaise);

			setNodeText(row.cells[ITEM_PATIENT_AMT_COL], formatAmountPaise(patientPaise));
			patAmtObj.value = formatAmountPaise(patientPaise);

			setNodeText(row.cells[ITEM_CLAIM_AMT_COL], formatAmountPaise(newClaimPaise));
			claimAmtObj.value = formatAmountPaise(newClaimPaise);

			editRateObj.textContent = formatAmountPaise(newRatePaise);
			editAmtObj.textContent = formatAmountPaise(newAmountPaise);

			editedObj.value = "true";
			resetClaimTotals();
			itemsEdited++;
		} else {
			editedObj.value = "false";
		}

		if (getPaise(claimAmtObj.value) == getPaise(origClaimAmtObj.value) &&
			(denialAcceptedObj.value == orig_denialAcceptedObj.value) &&
			(rejectionReasonObj.value == orig_rejectionReasonObj.value) ) {
			cls = "";
			editedObj.value = "false";
		}

		row.className = cls;
	}

	editItemAmountDialog.hide();
	return id;
}

function resetClaimTotals() {
	var num = getNumItems();

	totalAmountPaise = 0;
	totalDiscountPaise = 0;
	totalGrossAmountPaise = 0;
	totalPatientAmountPaise = 0;
	totalClaimNetAmountPaise = 0;

	var table = document.getElementById("billsTable");

	for (var i = 0; i < num; i++) {

		var amountPaise = getElementPaise(document.getElementById("item_amount" + i));
		var discountPaise = getElementPaise(document.getElementById("item_disc" + i));
		var patAmtPaise = getElementPaise(document.getElementById("patient_amount" + i));
		var insAmtPaise = getElementPaise(document.getElementById("claim_net_amount" + i));

		totalAmountPaise += amountPaise + discountPaise;
		totalDiscountPaise += discountPaise;
		totalGrossAmountPaise += amountPaise;
		totalPatientAmountPaise += (amountPaise - insAmtPaise);
		totalClaimNetAmountPaise += insAmtPaise;
	}

	setNodeText("lblBillsAmount", formatAmountPaise(totalAmountPaise));
	setNodeText("lblBillsDiscount", formatAmountPaise(totalDiscountPaise));
	setNodeText("lblBillsGrossAmount", formatAmountPaise(totalGrossAmountPaise));
	setNodeText("lblBillsPatientAmt", formatAmountPaise(totalPatientAmountPaise));
	setNodeText("lblClaimNetAmt", formatAmountPaise(totalClaimNetAmountPaise));
}

function getNumItems() {
	return document.getElementById("billsTable").rows.length - 1;
}

function getItemRow(i) {
	i = parseInt(i);
	var table = document.getElementById("billsTable");
	return table.rows[i];
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function doSave() {
	if (!validateClaim()) {
		return false;
	}

	var alertStatus = updateDenialAcceptForCombinedActivities();
	if(alertStatus && itemsEdited != 0) {
		var agree = confirm('\t\t Selected items having some combined activities. \n Denial accepting'+
		' for one activity will apply for other activities also. \n\t\t\t\t Do you want continue ?');
		if(!agree)
			return false;
	}

	if (itemsEdited != 0)
		document.mainform._method.value = "updateClaim";

	if (document.mainform._method.value == "") {
		alert("No changes made to save.");
		return false;
	}
	document.mainform.submit();
	return true;
}

var insurancePhotoDialog;

function initInsurancePhotoDialog() {
	insurancePhotoDialog = new YAHOO.widget.Dialog('insurancePhotoDialog', {
		width: "550px",
		height: "250px",
		visible: false,
		modal: true,
		constraintoviewport: true,
		close: false,
	});

	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: handleInsurancePhotoDialogCancel,
		scope: insurancePhotoDialog,
		correctScope: true
	});
	insurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	insurancePhotoDialog.render();
}

function showInsurancePhotoDialog() {
	var button = document.getElementById('_plan_card');
	resizeCustom(document.getElementById('insuranceImage'), 500, 200);
	insurancePhotoDialog.cfg.setProperty("context", [button, "tl", "bl", ["beforeShow", "windowResize"]], false);
	document.getElementById('insurancePhotoDialog').style.display = 'block';
	document.getElementById('insurancePhotoDialog').style.visibility = 'visible';
	insurancePhotoDialog.show();
}

function handleInsurancePhotoDialogCancel() {
	document.getElementById('insurancePhotoDialog').style.display = 'none';
	document.getElementById('insurancePhotoDialog').style.visibility = 'hidden';
	insurancePhotoDialog.cancel();
}

function validateDenailAcceptedAndRejReason(id) {
	var denialAcceptedObj = document.getElementById("item_denial_accepted" + id);
	var rejectionReasonObj = document.getElementById("item_rej_reason" + id);
	var editDenialChkObj = claimEditForm.denialCheck;
	var editRejReasonDnObj = claimEditForm.rejection_reasons_drpdn;

	//On check of denial acceptance selecting rejection reason is mandatory.
	if(editDenialChkObj.checked) {
		if(editRejReasonDnObj.value != null && editRejReasonDnObj.value != ''){}
		else {
			alert('Please select Rejection Reason');
			return false;
		}
	}

	return true;

}

function disableOrEnableRejReason() {
	var editDenialChkObj = claimEditForm.denialCheck;
	var editRejReasonDnObj = claimEditForm.rejection_reasons_drpdn;

	if(editDenialChkObj != null && editDenialChkObj.checked)
		editRejReasonDnObj.disabled = false;
	else {
		editRejReasonDnObj.disabled = true;
		editRejReasonDnObj.selectedIndex=0;
	}
}

function disableOrEnableClaimRejReason() {
	var claimClosureTypeDnObj = mainform.closure_type;
	var claimRejReasonDnObj = mainform.claim_rejection_reasons_drpdn;

	if(claimClosureTypeDnObj != null && claimClosureTypeDnObj.value == 'D')
		claimRejReasonDnObj.disabled = false;
	else {
		claimRejReasonDnObj.selectedIndex=0;
		claimRejReasonDnObj.disabled = true;
	}
}

function updateDenialAcceptForCombinedActivities() {
	var claimActIdList = document.getElementsByName("claim_activity_id");
	var denialAcceptedStatusList = document.getElementsByName("item_denial_accepted");
	var orgDenialAcceptedStatusList = document.getElementsByName("orig_item_denial_accepted");
	var itemRejReasonList = document.getElementsByName("item_rej_reason");
	var origItemRejReasonList = document.getElementsByName("orig_item_rej_reason");
	var orgDenialAcceptedStatusList = document.getElementsByName("orig_item_denial_accepted");
	var itemEditedList = document.getElementsByName("edited");
	var rejectionReasonList = document.getElementsByName("item_rej_reason");
	var netAmt = document.getElementsByName("claim_net_amount");
	var recdAmt = document.getElementsByName("claim_recd_amount");
	var alertOnCobminedAct = false;
	var claimActIdsStr="";
	for(var i=0; i<claimActIdList.length;i++){
		var claimActId = claimActIdList[i].value;
		var denialStatusEdited = (denialAcceptedStatusList[i].value != orgDenialAcceptedStatusList[i].value) ||
								(itemRejReasonList[i].value != origItemRejReasonList[i].value);
		
		var updateDenialAccStatusForCombActivity = true;
		var netAmtPaise = getPaise(netAmt[i].value);
		var recdAmtPaise = getPaise(recdAmt[i].value);
		
		if(netAmtPaise == 0 && recdAmtPaise == 0) {
			updateDenialAccStatusForCombActivity = false;
		}
		
		if((itemEditedList[i].value == 'true' && denialStatusEdited) 
				&& claimActId.substr(0,3) == 'ACT' && updateDenialAccStatusForCombActivity) {
			if(claimActIdsStr.indexOf(claimActId) != -1)
				continue;
			updateCombinedActivityDenialStaus(claimActId , denialAcceptedStatusList[i].value , rejectionReasonList[i].value)
			claimActIdsStr=claimActIdsStr+claimActId+',';
			alertOnCobminedAct = true;
		}
	}
	return alertOnCobminedAct;
}

function updateCombinedActivityDenialStaus(claimActId , denialAccStatus , rejreason) {
	var combinedActIdxList = document.getElementsByName(claimActId);
	for(var i=0; i<combinedActIdxList.length;i++){
		var index = combinedActIdxList[i].value;
		document.getElementById("item_denial_accepted"+index).value = denialAccStatus;
		document.getElementById("item_rej_reason"+index).value = rejreason ;
		document.getElementById("edited"+index).value = 'true' ;
	}
}

function showItemHistory(obj){
	var row = getThisRow(obj);
	if (row == null) return false;
	if (document.getElementById("rowIndexValue" + (row.rowIndex)) == null)
		return false;

	var idx = document.getElementById("rowIndexValue" + (row.rowIndex)).value;
	
	var chargeId = document.getElementById("charge_id"+idx).value;
	
	var saleItemId = document.getElementById("sale_item_id"+idx).value;
	
	var claimActivityHistList = getClaimActivityHistory(claimId, chargeId, saleItemId);
	
	if(null != claimActivityHistList && claimActivityHistList.length > 0) {
		showClaimActiviytHistory(claimActivityHistList);
	}
	claimHistoryDialog.cfg.setProperty("context", [row.cells[ITEM_HIST_COL], "tr", "bl"], false);
	claimHistoryDialog.show();
}

function showClaimActiviytHistory(claimActivityHistList){
	var claimHistTbl = document.getElementById("claimHistTblId");
	
	for(var i = 0; i<claimActivityHistList.length; i++) {
		
		var len = claimHistTbl.rows.length;
		var templateRow = claimHistTbl.rows[len-1];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		YAHOO.util.Dom.insertBefore(row, templateRow);
		
		var el = row.cells[0];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		setNodeText(el, claimActivityHistList[i].txn_type);
		
		el = row.cells[1];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		setNodeText(el, claimActivityHistList[i].id);
		
		el = row.cells[2];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		setNodeText(el, claimActivityHistList[i].sub_date);
		
		el = row.cells[3];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		setNodeText(el, claimActivityHistList[i].activity_id);
		
		el = row.cells[4];
		el.setAttribute("class", "number");
		setNodeText(el, claimActivityHistList[i].quantity);
		
		el = row.cells[5];
		el.setAttribute("class", "number");
		setNodeText(el, claimActivityHistList[i].claim_amount);
		
		el = row.cells[6];
		el.setAttribute("class", "number");
		setNodeText(el, claimActivityHistList[i].activity_vat);
		
		el = row.cells[7];
		el.setAttribute("class", "number");
		setNodeText(el, claimActivityHistList[i].activity_vat_percent);
		
		el = row.cells[8];
		el.setAttribute("class", "number");
		setNodeText(el, claimActivityHistList[i].payment_amount);
		
		el = row.cells[9];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		setNodeText(el, claimActivityHistList[i].activity_status);
		
		el = row.cells[10];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		var denialCode = null != claimActivityHistList[i].denial_code 
			? truncateText(claimActivityHistList[i].denial_code,10) : claimActivityHistList[i].denial_code
		setNodeText(el, denialCode);
		if(null != claimActivityHistList[i].denial_code && claimActivityHistList[i].denial_code.length > 10){
			row.cells[10].setAttribute("onmouseover", 'this.title="'+claimActivityHistList[i].denial_code+'"'); 
		}
		
		el = row.cells[11];
		el.setAttribute("class", "formlabel");
		el.setAttribute("style", "text-align:left;");
		setNodeText(el, claimActivityHistList[i].resubmission_type);
			
	}
}

function getClaimActivityHistory(claimId, chargeId, saleItemId){
	var url =cpath+"/billing/claimReconciliation.do?_method=getClaimActivityHistory&charge_id="+chargeId+"&claim_id="+claimId+"&sale_item_id="+saleItemId;
	
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			return eval(reqObject.responseText);
		}
	}
	return null;
}
