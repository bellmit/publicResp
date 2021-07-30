var mainform = null;
var itemsEdited = 0;
var itemsAdded = 0;

var Dom = YAHOO.util.Dom;

var totalAmountPaise = 0;
var totalDiscountPaise = 0;
var totalGrossAmountPaise = 0;
var totalPatientAmountPaise = 0;
var totalClaimNetAmountPaise = 0;
var totalApprovedNetAmountPaise = 0;

function initPBMPresc() {

	mainform = document.mainform;

	var idx = 0;
	S_S_NO = idx++;
	S_PRESC_DATE = idx++;
	S_FREQUENCY = idx++;
	S_DURATION = idx++;
	S_FORM_NAME = idx++;
	S_STRENGTH = idx++;
	S_ROUTE_NAME = idx++;

	if (useGenerics)
		S_GENERIC_NAME = idx++;

	S_ITEM_NAME = idx++;
	S_ITEM_AVAIL_QTY = idx++;

	ITEM_RATE_COL = idx++;
	ITEM_QTY_COL = idx++;
	ITEM_USER_UOM = idx++;
	ITEM_DISC_COL = idx++;
	ITEM_AMOUNT_COL = idx++;
	ITEM_PATIENT_AMT_COL = idx++;
	ITEM_CLAIM_AMT_COL = idx++;

	S_TRASH_COL = idx++;
   S_EDIT_COL = idx++;

   ITEM_APPRD_AMT_COL = idx++;
   ITEM_PBM_STATUS_COL = idx++;

	// Display amounts based on action rights and rate plan.
	//showRateDetails = displayAmounts();

	clearSIFields();
	initSIDialog();
	initEditSIDialog();
	initSIItemAutoComplete();
	initSIEditItemAutoComplete();
	initFrequencyAutoComplete();
	editDialogGeneric();
	initInsurancePhotoDialog();
	document.getElementById("_comments").value = UnFormatTextAreaValue(document.getElementById("_comments").value);

	var phStoreObj = document.getElementById("_phStore");

	if (phStoreObj != null) {
		if (phStoreObj.type == "text" && phStoreObj.value == "")
		 	document.getElementById('storeErrorDiv').style.display = 'block';
		else if (phStoreObj.type == "select-one" && phStoreObj.options.length == 1)
			document.getElementById('storeErrorDiv').style.display = 'block';
	}else
		document.getElementById('storeErrorDiv').style.display = 'none';

	enableDisableQtyFieldsForCorrection();

	initRejectionDetailsDialog();
}


var rejectionDetailsDialog;
function initRejectionDetailsDialog() {
	    rejectionDetailsDialog = new YAHOO.widget.Dialog('rejectionDetailsDialog', {
	    	context:["rejection_reason","tr","br", ["beforeShow", "windowResize"]],
	        width:"525px",
	        visible: false,
	        modal: true,
	        constraintoviewport: true,
			close :false,
	    });

	    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                             { fn:handleRejectionDialogCancel,
	                                               scope:rejectionDetailsDialog,
	                                               correctScope:true } );
		scope:rejectionDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	    rejectionDetailsDialog.render();
}

function handleRejectionDialogCancel(){
	 document.getElementById('rejectionDetailsDialog').style.display='none';
	 document.getElementById('rejectionDetailsDialog').style.visibility='hidden';
	 rejectionDetailsDialog.cancel();
}


function showRejectionDetailsDialog(){
	document.getElementById('rejectionDetailsDialog').style.display='block';
	document.getElementById('rejectionDetailsDialog').style.visibility='visible';
	rejectionDetailsDialog.show();
}


function recalcItemAmount() {

	var id = document.getElementById('s_ed_editRowId').value ;
	var row = getChargeRow(id, 'siTable');

	var editclaimObj = document.getElementById("s_ed_claim_item_net_amount");
	var editRateLblObj = document.getElementById("s_ed_price_label");
	var editAmtLblObj = document.getElementById("s_ed_claim_item_amount");
	var editPatAmtLblObj = document.getElementById("s_ed_claim_item_pat_amount");

	if (editclaimObj != null)
		if (!validateAmount(editclaimObj, "Claim amount must be a valid amount"))
			return false;

	var amountObj = getIndexedFormElement(mainform, "s_item_amount", id);
	var qtyObj = getIndexedFormElement(mainform, "s_item_qty", id);
	var rateObj = getIndexedFormElement(mainform, "s_item_rate", id);
	var discObj = getIndexedFormElement(mainform, "s_item_disc", id);

	var pkgUnitObj = getIndexedFormElement(mainform, "s_item_package_unit", id);
	var userUnitObj = getIndexedFormElement(mainform, "s_item_user_unit", id);

	var patAmtObj = getIndexedFormElement(mainform, "s_patient_amount", id);
	var origClaimAmtObj = getIndexedFormElement(mainform, "s_orig_claim_net_amount", id);
	var claimAmtObj = getIndexedFormElement(mainform, "s_claim_net_amount", id);

	if (amountObj != null && editclaimObj != null) {

		var row = getThisRow(amountObj);

		var edited = true;
		if (getPaise(editclaimObj.value) == getPaise(claimAmtObj.value))
			edited = false;

		if (edited) {

			var origClaimAmountPaise = getPaise(origClaimAmtObj.value);
			var origRatePaise = getPaise(rateObj.value);
			var origAmountPaise = getPaise(amountObj.value);

			var discountPaise = getPaise(discObj.value);
			var qty = getAmount(qtyObj.value);
			if (userUnitObj.value == 'P')
				 qty = qty  * getAmount(pkgUnitObj.value);

			if (getAmount(pkgUnitObj.value) <= 0) {
				alert("Package Unit is invalid (or) cannot be zero.");
				editclaimObj.focus();
				return false;
			}

			var newClaimPaise = getPaise(editclaimObj.value);
			var newPatientPaise = origAmountPaise - newClaimPaise;
			var newAmountPaise = newPatientPaise + newClaimPaise;

			if (newPatientPaise != 0) {
				var validAmountPaise = newPatientPaise;

				if ((newClaimPaise < origClaimAmountPaise) && (newClaimPaise + newPatientPaise) < 0) {
					alert("Claim amount cannot be less than amount : " + (formatAmountPaise(validAmountPaise)));
					editclaimObj.focus();
					return false;
				}
			}

			var amountDiffPaise = origAmountPaise - newAmountPaise;
			var newRatePaise = origRatePaise - (amountDiffPaise / qty);

			editRateLblObj.textContent = formatAmountPaise(newRatePaise);
			editAmtLblObj.textContent = formatAmountPaise(newAmountPaise);
			editPatAmtLblObj.textContent = formatAmountPaise(newPatientPaise);

			if (newRatePaise < 0) {
				alert("Rate cannot be negative.");
				editclaimObj.focus();
				return false;
			}

			setSIEdited();

		}else {
			editRateLblObj.textContent = formatAmountPaise(getPaise(rateObj.value));
			editAmtLblObj.textContent = formatAmountPaise(getPaise(amountObj.value));
			editPatAmtLblObj.textContent = formatAmountPaise(getPaise(patAmtObj.value));
		}
	}
}


function editItemAmountSubmit(id) {

	var editclaimObj = document.getElementById("s_ed_claim_item_net_amount");
	var editRateLblObj = document.getElementById("s_ed_price_label");
	var editAmtLblObj = document.getElementById("s_ed_claim_item_amount");

	if (editclaimObj != null)
		if (!validateAmount(editclaimObj, "Claim amount must be a valid amount"))
			return false;

	var amountObj = getIndexedFormElement(mainform, "s_item_amount", id);
	var qtyObj = getIndexedFormElement(mainform, "s_item_qty", id);
	var rateObj = getIndexedFormElement(mainform, "s_item_rate", id);
	var discObj = getIndexedFormElement(mainform, "s_item_disc", id);

	var pkgUnitObj = getIndexedFormElement(mainform, "s_item_package_unit", id);
	var userUnitObj = getIndexedFormElement(mainform, "s_item_user_unit", id);

	var patAmtObj = getIndexedFormElement(mainform, "s_patient_amount", id);
	var origClaimAmtObj = getIndexedFormElement(mainform, "s_orig_claim_net_amount", id);
	var claimAmtObj = getIndexedFormElement(mainform, "s_claim_net_amount", id);

	var edited = true;

	if (amountObj != null && editclaimObj != null) {

		var row = getThisRow(amountObj);

		if (getPaise(editclaimObj.value) == getPaise(claimAmtObj.value))
			edited = false;

		if (edited) {

			var origClaimAmountPaise = getPaise(origClaimAmtObj.value);
			var origRatePaise = getPaise(rateObj.value);
			var origAmountPaise = getPaise(amountObj.value);

			var patientPaise = getPaise(patAmtObj.value);
			var discountPaise = getPaise(discObj.value);
			var qty = getAmount(qtyObj.value);
			if (userUnitObj.value == 'P')
				 qty = qty  * getAmount(pkgUnitObj.value);

			if (getAmount(pkgUnitObj.value) <= 0) {
				alert("Package Unit is invalid (or) cannot be zero.");
				editclaimObj.focus();
				return false;
			}

			var newClaimPaise = getPaise(editclaimObj.value);
			var newAmountPaise = patientPaise + newClaimPaise;

			if (patientPaise != 0) {
				var validAmountPaise = patientPaise;

				if ((newClaimPaise < origClaimAmountPaise) && (newClaimPaise + patientPaise) < 0) {
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

			editRateLblObj.textContent = formatAmountPaise(newRatePaise);
			editAmtLblObj.textContent = formatAmountPaise(newAmountPaise);

			resetClaimTotals();
			itemsEdited++;
		}

		if (getPaise(claimAmtObj.value) == getPaise(origClaimAmtObj.value)) {
			edited = false;
		}
	}
	return edited;
}


function resetClaimTotals() {
	var num = getNumCharges("siTable");

	totalAmountPaise = 0;
	totalDiscountPaise = 0;
	totalGrossAmountPaise = 0;
	totalPatientAmountPaise = 0;
	totalClaimNetAmountPaise = 0;
	totalApprovedNetAmountPaise = 0;

	for (var id = 0; id < num; id++) {

		var deletedObj = getIndexedFormElement(mainform, "s_delItem", id);
		if (deletedObj.value == 'true')
			continue;

		var amountPaise = getElementPaise(getIndexedFormElement(mainform, "s_item_amount", id));
		var discountPaise = getElementPaise(getIndexedFormElement(mainform, "s_item_disc", id));
		var patAmtPaise = getElementPaise(getIndexedFormElement(mainform, "s_patient_amount", id));
		var insAmtPaise = getElementPaise(getIndexedFormElement(mainform, "s_claim_net_amount", id));
		var approvedPaise = getElementPaise(getIndexedFormElement(mainform, "s_claim_approved_amount", id));

		totalAmountPaise += amountPaise + discountPaise;
		totalDiscountPaise += discountPaise;
		totalGrossAmountPaise += amountPaise;
		totalPatientAmountPaise += patAmtPaise;
		totalClaimNetAmountPaise += insAmtPaise;
		totalApprovedNetAmountPaise += approvedPaise;
	}

	setNodeText("lblAmount", formatAmountPaise(totalAmountPaise));
	setNodeText("lblDiscount", formatAmountPaise(totalDiscountPaise));
	setNodeText("lblGrossAmount", formatAmountPaise(totalGrossAmountPaise));
	setNodeText("lblPatientAmt", formatAmountPaise(totalPatientAmountPaise));
	setNodeText("lblClaimNetAmt", formatAmountPaise(totalClaimNetAmountPaise));
	setNodeText("lblApprovedAmount", formatAmountPaise(totalApprovedNetAmountPaise));
}

function addSIToTable() {

	if (!validateAddOrEditItemData())
		return false;

	var itemType = document.getElementById('s_d_itemType').value;
	var itemName = document.getElementById('s_d_itemName').value;
	var itemId = document.getElementById('s_d_item_id').value;

	if (itemName == '') {
  		alert('Item Name is required.');
  		document.getElementById('s_d_itemName').focus();
  		return false;
  	}
	if (checkForSIDuplicates('s_d')) {
		alert("Duplicate entry : " + itemName);
		return false;
	}

	var qty = document.getElementById('s_d_claim_item_qty').value;

	if (!validateAmount(document.getElementById('s_d_claim_item_qty'), "Quantity must be a valid amount"))
		return false;

	if (getAmount(qty) == 0) {
  		alert("Quantity is required.");
  		document.getElementById('s_ed_claim_item_qty').focus();
  		return false;
  	}

	if (mod_eclaim_erx == 'Y') {
		if (!validateEprxFields("s_d", false))
		return false;

	}else if (mod_eclaim_pbm == 'Y') {
		if (!validatePBMFields("s_d", false))
		return false;
	}

	var master = document.getElementById('s_d_item_master').value;
	var frequency = document.getElementById('s_d_frequency').value;
	var strength = document.getElementById('s_d_strength').value;
  	var remarks = document.getElementById('s_d_remarks').value;

  	var genericCode = document.getElementById('s_d_generic_code').value;
  	var genericName = document.getElementById('s_d_generic_name').value;
  	var ispackage = document.getElementById('s_d_ispackage').value;

  	var pkg_size = getAmount(document.getElementById('s_d_package_size').value);
  	var consumption_uom = document.getElementById('s_d_consumption_uom').value;

  	var price = getPaise(document.getElementById('s_d_price').value);

  	var routeId = document.getElementById('s_d_medicine_route').options[document.getElementById('s_d_medicine_route').selectedIndex].value;
  	var routeName = document.getElementById('s_d_medicine_route').options[document.getElementById('s_d_medicine_route').selectedIndex].text;
  	routeName = routeId == '' ? '' : routeName;

  	var item_form_id = document.getElementById('s_d_item_form_id').value;
  	var item_strength = document.getElementById('s_d_item_strength').value;
  	var item_form_name = document.getElementById('s_d_item_form_id').options[document.getElementById('s_d_item_form_id').selectedIndex].text;
	var item_pkg_price = 0;
	var item_unit_price = 0;

	var item_strength = document.getElementById('s_d_item_strength').value;
  	var item_strength_units = document.getElementById('s_d_item_strength_units').value;
  	item_strength_units = item_strength == '' ? '' : item_strength_units;
  	var strength_unit_name = document.getElementById('s_d_item_strength_units').options[document.getElementById('s_d_item_strength_units').selectedIndex].text;
  	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;

  	var addActivityEl = document.getElementById('s_d_addActivity');
  	var prescribedDate = document.getElementById('s_d_prescribed_date').value;

 	var duration_radio_els = document.getElementsByName('s_d_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	var granular_unit = document.getElementById('s_d_granular_units').value;
	if (!empty(granular_unit) && granular_unit == 'Y') {
		if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	  		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	  		document.getElementById('s_d_strength').focus();
	  		return false;
	  	}
	}

	var duration = document.getElementById('s_d_duration').value;
    var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById('s_d_duration').focus();
		return false;
	}

	if (duration == '' || duration == 0) {
  		alert("Enter days greater than zero");
  		document.getElementById('s_d_interval').focus();
  		return false;
  	}

	var prescDateObj = document.getElementById('s_d_prescribed_date');
	if (!validateRequired(prescDateObj, "Date is required"))
		return false;

	if (!doValidateDateField(prescDateObj,"past"))
		return false;

	var addActivity = 'false';
	if (addActivityEl && addActivityEl.checked) {
	   		addActivity = 'true';
	}

	var id = getNumCharges('siTable');
   	var table = document.getElementById("siTable");
	var templateRow = table.rows[getTemplateRow('siTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	document.getElementsByName('trashCanAnchor')[id].style.display = 'block';
    row.id = "s_itemRow" + id;
  	var cell = null;

	setNodeText(row.cells[S_S_NO], id+1);

	var freq = frequency + (remarks != '' ? " ("+remarks+")" : '');
	setNodeText(row.cells[S_FREQUENCY], freq, 10);
	setNodeText(row.cells[S_DURATION], duration + " " + duration_units, 20);
	setNodeText(row.cells[ITEM_QTY_COL], qty);

	/*if (itemType == 'Medicine' && strength == '') {
		alert("Dosage is required");
		document.getElementById('s_d_strength').focus();
		return false;
	}*/

  	setNodeText(row.cells[S_PRESC_DATE], prescribedDate);
  	setNodeText(row.cells[S_ITEM_NAME], itemName, 12);

	if (useGenerics)
		setNodeText(row.cells[S_GENERIC_NAME], genericName, 8);

   if (itemType == 'Medicine' || itemType == 'NonHospital') {
		//setNodeText(row.cells[S_DOSAGE], dosage);
		if (item_form_id != '') {
			setNodeText(row.cells[S_FORM_NAME], item_form_name, 15);
		}
		setNodeText(row.cells[S_STRENGTH], item_strength + ' ' + strength_unit_name, 15);

		setHiddenValue(id, "s_item_form_id", item_form_id);
		setHiddenValue(id, "s_item_strength", item_strength);
		setHiddenValue(id, "s_generic_code", genericCode);
		setHiddenValue(id, "s_generic_name", genericName);
		setHiddenValue(id, "s_granular_units", granular_unit);
	}

	setNodeText(row.cells[S_ROUTE_NAME], routeName);

	var item_pkg_price = 0;
	var item_unit_price = 0;

	if (pkg_size != '' && price != '' && qty != '') {
		item_unit_price = (price/pkg_size) * qty;
		item_pkg_price = Math.ceil(qty/pkg_size) * price;
	} else {
		item_pkg_price = price;
		item_unit_price = price;
	}

	setHiddenValue(id, "s_item_prescribed_id", "_");
	setHiddenValue(id, "s_prescribed_date", prescribedDate);
	setHiddenValue(id, "s_frequency", frequency);
	setHiddenValue(id, "s_strength", strength);
	setHiddenValue(id, "s_item_strength_units", item_strength_units);
	setHiddenValue(id, "s_duration", duration);
	setHiddenValue(id, "s_duration_units", duration_units);
	setHiddenValue(id, "s_medicine_quantity", qty);

	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);

	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);

	setHiddenValue(id, "s_consumption_uom", consumption_uom);
	setHiddenValue(id, "s_route_id", routeId);
	setHiddenValue(id, "s_route_name", routeName);

	setHiddenValue(id, "s_pkg_size", pkg_size == '' ? '' : pkg_size);
	setHiddenValue(id, "s_pkg_price", price == '' ? '' : formatAmountPaise(price));
	setHiddenValue(id, "s_item_pkg_price", item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setHiddenValue(id, "s_item_unit_price", item_unit_price == 0 ? '' : formatAmountPaise(item_unit_price));
	setHiddenValue(id, "s_issued", "N");

	var totQtyInStk = getAmount(document.getElementById('s_d_qty_in_stock').value);
	setHiddenValue(id, "s_qty_in_stock", totQtyInStk);
	setNodeText(row.cells[S_ITEM_AVAIL_QTY], totQtyInStk);

	var ratePaise = getPaise(document.getElementById("s_d_price_label").textContent);
	var discPaise = getPaise(document.getElementById("s_d_claim_item_disc").textContent);
	var amtPaise = getPaise(document.getElementById("s_d_claim_item_amount").textContent);
	var patPaise = getPaise(document.getElementById("s_d_claim_item_pat_amount").textContent);
	var claimPaise = getPaise(document.getElementById("s_d_claim_item_net_amount").value);
	var medPkgSize = getAmount(document.getElementById("s_d_pkg_size_label").textContent);

	var userUOMObj = document.getElementById('s_d_user_unit');
	var medUserUom = userUOMObj.options[userUOMObj.selectedIndex].text;
	var medUserUnit = userUOMObj.options[userUOMObj.selectedIndex].value;

	setHiddenValue(id, "s_item_qty", qty);
	setHiddenValue(id, "s_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_item_disc", formatAmountPaise(discPaise));
	setHiddenValue(id, "s_item_package_unit", medPkgSize);
	setHiddenValue(id, "s_item_user_unit", medUserUnit);

	var issueUOM = "";
	var packageUOM = "";

	for (var m=0; m<userUOMObj.options.length; m++) {
		if (userUOMObj.options[m].value == 'I') {
			issueUOM = userUOMObj.options[m].text;
		}
		if (userUOMObj.options[m].value == 'P') {
			packageUOM = userUOMObj.options[m].text;
		}
	}

	setHiddenValue(id, "s_item_package_uom", packageUOM);
	setHiddenValue(id, "s_item_issue_uom", issueUOM);

	setHiddenValue(id, "s_patient_amount", formatAmountPaise(patPaise));
	setHiddenValue(id, "s_claim_net_amount", formatAmountPaise(claimPaise));

	setHiddenValue(id, "s_orig_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_orig_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_orig_claim_net_amount", formatAmountPaise(claimPaise));

	setNodeText(row.cells[ITEM_RATE_COL], formatAmountPaise(ratePaise));
	setNodeText(row.cells[ITEM_DISC_COL], formatAmountPaise(discPaise));
	setNodeText(row.cells[ITEM_AMOUNT_COL], formatAmountPaise(amtPaise));
	setNodeText(row.cells[ITEM_PATIENT_AMT_COL], formatAmountPaise(patPaise));
	setNodeText(row.cells[ITEM_CLAIM_AMT_COL], formatAmountPaise(claimPaise));
	setNodeText(row.cells[ITEM_APPRD_AMT_COL], formatAmountPaise(0));

	setNodeText(row.cells[ITEM_QTY_COL], qty);
	setNodeText(row.cells[ITEM_USER_UOM], medUserUom);

	var img = document.createElement("img");
	img.setAttribute("src", cpath + "/images/empty_flag.gif");
	for (var i=row.cells[ITEM_PBM_STATUS_COL].childNodes.length-1; i>=0; i--) {
		row.cells[ITEM_PBM_STATUS_COL].removeChild(row.cells[ITEM_PBM_STATUS_COL].childNodes[i]);
	}
	var text = document.createTextNode("Open");
	row.cells[ITEM_PBM_STATUS_COL].appendChild(img);
	row.cells[ITEM_PBM_STATUS_COL].appendChild(text);

	itemsAdded++;
	setSIRowStyle(id);
	clearSIFields();
	this.align("tr", "tl");
	document.getElementById('s_d_itemName').focus();
	resetClaimTotals();
	return id;
}

function editSITableRow() {

	if (!validateAddOrEditItemData())
		return false;

	var id = document.getElementById('s_ed_editRowId').value ;
	var row = getChargeRow(id, 'siTable');

	var itemType = document.getElementById('s_ed_itemType').value;
  	var itemName = document.getElementById('s_ed_itemName').value;
  	var itemId = document.getElementById('s_ed_item_id').value;

	if (itemName == '') {
  		alert('Item Name is required.');
  		document.getElementById('s_ed_itemName').focus();
  		return false;
  	}
	if (checkForSIDuplicates('s_ed', id)) {
		alert("Duplicate entry : " + itemName);
		return false;
	}

  	var remarks = document.getElementById('s_ed_remarks').value;
  	var master = document.getElementById('s_ed_item_master').value;
  	var ispackage = document.getElementById('s_ed_ispackage').value;

  	var frequency = document.getElementById('s_ed_frequency').value;
   var strength = document.getElementById('s_ed_strength').value;
   var duration = document.getElementById('s_ed_duration').value;

   var qty = document.getElementById('s_ed_claim_item_qty').value;

   if (!validateAmount(document.getElementById('s_ed_claim_item_qty'), "Quantity must be a valid amount"))
		return false;

   if (getAmount(qty) == 0) {
  		alert("Quantity is required.");
  		document.getElementById('s_ed_claim_item_qty').focus();
  		return false;
  	}

	if (mod_eclaim_erx == 'Y') {
		if (!validateEprxFields("s_ed", false))
		return false;

	}else if (mod_eclaim_pbm == 'Y') {
		if (!validatePBMFields("s_ed", false))
		return false;
	}

	if (!validateConsultationAndPBMFieldValues("s_ed", false))
		return false;

  	var consumptionUOM = document.getElementById('s_ed_consumption_uom').value;
  	var prescribedDate = document.getElementById('s_ed_prescribed_date').value;
  	var item_form_id = document.getElementById('s_ed_item_form_id').value;
  	var item_strength = document.getElementById('s_ed_item_strength').value;
  	var item_form_name = document.getElementById('s_ed_item_form_id').options[document.getElementById('s_ed_item_form_id').selectedIndex].text;
  	item_form_name = item_form_id == '' ? '' : item_form_name;
	var item_pkg_price = 0;
	var item_unit_price = 0;

	var item_strength_units = document.getElementById('s_ed_item_strength_units').value;
	item_strength_units = item_strength == '' ? '' : item_strength_units;
  	var strength_unit_name = document.getElementById('s_ed_item_strength_units').options[document.getElementById('s_ed_item_strength_units').selectedIndex].text;
  	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;

	var granular_unit = document.getElementById('s_ed_granular_units').value;
	if (!empty(granular_unit) && granular_unit == 'Y') {
	  	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	  		alert("Dosage should be greater then Zero and it should be a Number(allowed only two decimals)");
	  		document.getElementById('s_ed_strength').focus();
	  		return false;
	  	}
	}

  	var addActivity = 'false';

	var prescDateObj = document.getElementById('s_ed_prescribed_date');
	if (!validateRequired(prescDateObj, "Date is required"))
		return false;

	if (!doValidateDateField(prescDateObj,"past"))
		return false;

  	var duration_radio_els = document.getElementsByName('s_ed_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById('s_ed_duration').focus();
		return false;
	}
	if (!empty(duration) && empty(duration_units)) {
		alert("Duration units is required");
		return false;
	}

  	var addActivityEl = document.getElementById('s_ed_addActivity');
  	if (addActivityEl && addActivityEl.checked) {
  		addActivity = 'true';
  	}

   setNodeText(row.cells[S_PRESC_DATE], prescribedDate);
   setNodeText(row.cells[S_ITEM_NAME], itemName, 12);

   if (itemType == 'Medicine') {
		if (item_form_id != '')
			setNodeText(row.cells[S_FORM_NAME], item_form_name, 15);
		setNodeText(row.cells[S_STRENGTH], item_strength + ' ' + strength_unit_name, 15);

	}

	var freq = frequency + (remarks != '' ? " ("+remarks+")" : '');
	setNodeText(row.cells[S_FREQUENCY], freq, 10);
	setNodeText(row.cells[S_DURATION], duration +" "+ duration_units);

	var totQtyInStk = getAmount(document.getElementById('s_ed_qty_in_stock').value);
	setHiddenValue(id, "s_qty_in_stock", totQtyInStk);
	setNodeText(row.cells[S_ITEM_AVAIL_QTY], totQtyInStk);

	setHiddenValue(id, "s_prescribed_date", prescribedDate);
	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);
	setHiddenValue(id, "s_consumption_uom", consumptionUOM);

	setHiddenValue(id, "s_granular_units", granular_unit);
	setHiddenValue(id, "s_frequency", frequency);
	setHiddenValue(id, "s_strength", strength);
	setHiddenValue(id, "s_duration", duration);
	setHiddenValue(id, "s_duration_units", duration_units);
	setHiddenValue(id, "s_medicine_quantity", qty);
	setHiddenValue(id, "s_item_form_id", item_form_id);
	setHiddenValue(id, "s_item_strength", item_strength);
	setHiddenValue(id, "s_item_strength_units", item_strength_units);

	var routeId = document.getElementById('s_ed_medicine_route').options[document.getElementById('s_ed_medicine_route').selectedIndex].value;
  	var routeName = document.getElementById('s_ed_medicine_route').options[document.getElementById('s_ed_medicine_route').selectedIndex].text;
  	routeName = routeId == '' ? '' : routeName;

	setHiddenValue(id, "s_route_id", routeId);
	setHiddenValue(id, "s_route_name", routeName);
	setNodeText(row.cells[S_ROUTE_NAME], routeName);

	var ratePaise = getPaise(document.getElementById("s_ed_price_label").textContent);
	var discPaise = getPaise(document.getElementById("s_ed_claim_item_disc").textContent);
	var amtPaise = getPaise(document.getElementById("s_ed_claim_item_amount").textContent);
	var patPaise = getPaise(document.getElementById("s_ed_claim_item_pat_amount").textContent);
	var claimPaise = getPaise(document.getElementById("s_ed_claim_item_net_amount").value);
	var medPkgSize = getAmount(document.getElementById("s_ed_pkg_size_label").textContent);

	var userUOMObj = document.getElementById('s_ed_user_unit');

	if (useGenerics) {
		var itemIssueUOM = "";
		var itemPackageUOM = "";

		for (var m=0; m<userUOMObj.options.length; m++) {
			if (userUOMObj.options[m].value == 'I') {
				itemIssueUOM = userUOMObj.options[m].text;
			}
			if (userUOMObj.options[m].value == 'P') {
				itemPackageUOM = userUOMObj.options[m].text;
			}
		}
		setHiddenValue(id, "s_item_package_uom", itemPackageUOM);
		setHiddenValue(id, "s_item_issue_uom", itemIssueUOM);
	}

	var medUserUom = userUOMObj.options[userUOMObj.selectedIndex].text;
	var medUserUnit = userUOMObj.options[userUOMObj.selectedIndex].value;

	setHiddenValue(id, "s_item_qty", qty);
	setHiddenValue(id, "s_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_item_disc", formatAmountPaise(discPaise));
	setHiddenValue(id, "s_item_package_unit", medPkgSize);
	setHiddenValue(id, "s_item_user_unit", medUserUnit);
	setHiddenValue(id, "s_patient_amount", formatAmountPaise(patPaise));
	setHiddenValue(id, "s_claim_net_amount", formatAmountPaise(claimPaise));

	setHiddenValue(id, "s_orig_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_orig_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_orig_claim_net_amount", formatAmountPaise(claimPaise));

	setNodeText(row.cells[ITEM_RATE_COL], formatAmountPaise(ratePaise));
	setNodeText(row.cells[ITEM_DISC_COL], formatAmountPaise(discPaise));
	setNodeText(row.cells[ITEM_AMOUNT_COL], formatAmountPaise(amtPaise));
	setNodeText(row.cells[ITEM_PATIENT_AMT_COL], formatAmountPaise(patPaise));
	setNodeText(row.cells[ITEM_CLAIM_AMT_COL], formatAmountPaise(claimPaise));

	setNodeText(row.cells[ITEM_QTY_COL], qty);
	setNodeText(row.cells[ITEM_USER_UOM], medUserUom);

	YAHOO.util.Dom.removeClass(row, 'editing');

	var edited = editItemAmountSubmit(id);
	itemsEdited++;

	setIndexedValue("s_edited", id, 'true');
	setSIRowStyle(id);

	editSIDialog.cancel();
	return true;
}

var siFieldEdited = false;
function setSIEdited() {
	siFieldEdited = true;
}

function showEditSIDialog(obj) {

	if (!validateAddOrEditItemData())
		return false;

	parentDialog = editSIDialog;
	var row = getThisRow(obj);

	var id = getRowChargeIndex(row);
	editSIDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editSIDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('s_ed_editRowId').value = id;

	var pbmsts = getIndexedFormElement(mainform, "s_pbm_status", id).value;
	var flag = (pbmsts == 'C') // Approved medicine qty should not be edited
	document.getElementById("siOk").disabled = flag;

	document.getElementById('s_ed_item_prescribed_id').value = getIndexedValue("s_item_prescribed_id", id);

	var itemName = getIndexedValue("s_item_name", id);
	var itemType = getIndexedValue("s_itemType", id);

	document.getElementById('s_ed_itemTypeLabel').textContent = itemType;

	document.getElementById('s_ed_itemName').value = getIndexedValue("s_item_name", id);
	document.getElementById('s_ed_item_id').value = getIndexedValue("s_item_id", id);
	document.getElementById('s_ed_itemType').value = itemType;

	document.getElementById('s_ed_prescribed_date').value = getIndexedValue('s_prescribed_date', id);

	getEditRouteOfAdministrations();
	setSelectedIndex(document.getElementById('s_ed_medicine_route'), getIndexedValue('s_route_id', id));

	initEditDosageAutoComplete();
	document.getElementById('s_ed_strength').value = getIndexedValue("s_strength", id);
	document.getElementById('s_ed_frequency').value = getIndexedValue("s_frequency", id);
	document.getElementById('s_ed_granular_units').value = getIndexedValue("s_granular_units", id);

	document.getElementById('s_ed_duration').value = getIndexedValue("s_duration", id);
	// enable the duration units only if item is not isssued.
	var issued = getIndexedValue("s_issued", id);
	toggleDurationUnits(issued != 'Y', 's_ed');

	var duration_units = getIndexedValue("s_duration_units", id);
	var els = document.getElementsByName("s_ed_duration_units");

	for (var k=0; k<els.length; k++) {
		if (els[k].value == duration_units) {
			els[k].checked = true;
			break;
		}
	}

	document.getElementById('s_ed_qty_in_stock').value = getIndexedValue("s_qty_in_stock", id);

	document.getElementById('s_ed_claim_item_qty').value = getIndexedValue("s_medicine_quantity", id);
	document.getElementById('s_ed_genericNameAnchor_editdialog').innerHTML = getIndexedValue("s_generic_name", id);

	document.getElementById('s_ed_medicineUOM').textContent = getIndexedValue("s_consumption_uom", id);
	document.getElementById('s_ed_consumption_uom').value = getIndexedValue("s_consumption_uom", id);
	document.getElementById('s_ed_genericNameAnchor_editdialog').href =
		'javascript:showGenericInfo("", "s_ed_", "editdialog", "' + getIndexedValue("s_generic_code", id) + '")';
	document.getElementById('s_ed_genericNameAnchor_editdialog').style.display = 'block';

	document.getElementById('s_ed_item_form_id').value = getIndexedValue("s_item_form_id", id);
	document.getElementById('s_ed_item_strength').value = getIndexedValue('s_item_strength', id);
	document.getElementById('s_ed_item_strength_units').value = getIndexedValue('s_item_strength_units', id);

	document.getElementById('s_ed_ispackage').value = getIndexedValue("s_ispackage", id);
	document.getElementById('s_ed_remarks').value = getIndexedValue('s_remarks', id);
	document.getElementById('s_ed_item_master').value = getIndexedValue('s_item_master', id);
	document.getElementById('s_ed_remarks').focus();

	document.getElementById('s_ed_code_type').textContent = getIndexedValue("s_item_code_type", id);
	document.getElementById('s_ed_code').textContent = getIndexedValue("s_item_code", id);

	document.getElementById('s_ed_pkg_size_label').textContent = getIndexedValue('s_item_package_unit', id);

	var issueUOM = getIndexedValue('s_item_issue_uom', id);
	var packageUOM = getIndexedValue('s_item_package_uom', id);

	var userUOMObj = document.getElementById('s_ed_user_unit');
	var optIdx = 0;
	if (!empty(issueUOM)) {
		optIdx++;
		userUOMObj.length = optIdx;
		userUOMObj.options[optIdx-1].value = 'I';
		userUOMObj.options[optIdx-1].text = issueUOM;
	}
	if (!empty(packageUOM) && issueUOM != packageUOM) {
		optIdx++;
		userUOMObj.length = optIdx;
		userUOMObj.options[optIdx-1].value = 'P';
		userUOMObj.options[optIdx-1].text = packageUOM;
	}

	var userUOMValue = getIndexedValue('s_item_user_unit', id);
	setSelectedIndex(userUOMObj, userUOMValue);

	document.getElementById('s_ed_price_label').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_item_rate", id)));

	document.getElementById('s_ed_claim_item_amount').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_item_amount", id)));

	document.getElementById('s_ed_claim_item_disc').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_item_disc", id)));

	document.getElementById('s_ed_claim_item_pat_amount').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_patient_amount", id)));

	document.getElementById('s_ed_claim_item_net_amount').value =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_claim_net_amount", id)));

	document.getElementById('s_ed_claim_item_orig_rate').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_orig_item_rate", id)));

	document.getElementById('s_ed_claim_item_orig_amount').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_orig_item_amount", id)));

	document.getElementById('s_ed_claim_item_orig_net_amount').textContent =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_orig_claim_net_amount", id)));

	if (useGenerics && empty(document.getElementById('s_ed_itemName').value))
		document.getElementById('s_ed_itemName').focus();

	var prescId = getIndexedValue("s_item_prescribed_id", id);
	// set observation code and code types.

	document.getElementById('s_ed_item_strength_code_type').textContent = getIndexedValue("s_item_strength_code_type."+prescId, id);
	document.getElementById('s_ed_item_strength_code').textContent = getIndexedValue("s_item_strength_code."+prescId, id);

	document.getElementById('s_ed_medicine_route_code_type').textContent = getIndexedValue("s_medicine_route_code_type."+prescId, id);
	document.getElementById('s_ed_medicine_route_code').textContent = getIndexedValue("s_medicine_route_code."+prescId, id);

	document.getElementById('s_ed_strength_code_type').textContent = getIndexedValue("s_strength_code_type."+prescId, id);
	document.getElementById('s_ed_strength_code').textContent = getIndexedValue("s_strength_code."+prescId, id);

	document.getElementById('s_ed_frequency_code_type').textContent = getIndexedValue("s_frequency_code_type."+prescId, id);
	document.getElementById('s_ed_frequency_code').textContent = getIndexedValue("s_frequency_code."+prescId, id);

	document.getElementById('s_ed_duration_code_type').textContent = getIndexedValue("s_duration_code_type."+prescId, id);
	document.getElementById('s_ed_duration_code').textContent = getIndexedValue("s_duration_code."+prescId, id);

	document.getElementById('s_ed_item_form_code_type').textContent = getIndexedValue("s_item_form_code_type."+prescId, id);
	document.getElementById('s_ed_item_form_code').textContent = getIndexedValue("s_item_form_code."+prescId, id);

	document.getElementById('s_ed_remarks_code_type').textContent = getIndexedValue("s_remarks_code_type."+prescId, id);
	document.getElementById('s_ed_remarks_code').textContent = getIndexedValue("s_remarks_code."+prescId, id);

	document.getElementById('s_ed_refills_code_type').textContent = getIndexedValue("s_refills_code_type."+prescId, id);
	document.getElementById('s_ed_refills_code').textContent = getIndexedValue("s_refills_code."+prescId, id);

	return false;
}

function toggleDurationUnits(enable, prefix) {
	enable = empty(enable) ? false : enable;
	var els = document.getElementsByName(prefix+"_duration_units");
	for (var i=0; i<els.length; i++) {
		els[i].disabled = !enable;
		els[i].checked = false;
	}
}

function clearSIFields() {

	document.getElementById('s_d_itemName').value = '';
	document.getElementById('s_d_item_id').value = '';
	document.getElementById('s_d_item_prescribed_id').value = '';

	if (!useGenerics)
		document.getElementById('s_d_medicine_route').length = 1;
	else
		document.getElementById('s_d_medicine_route').selectedIndex = 0;

	document.getElementById('s_d_frequency').value = '';
	document.getElementById('s_d_strength').value = '';
	document.getElementById('s_d_item_strength_units').selectedIndex = 0;
	document.getElementById('s_d_duration').value = '';
	var itemType = 'Medicine';
	var enable = itemType == 'Medicine' || itemType == 'NonHospital';
	toggleDurationUnits(enable, 's_d');
	if (enable)
		document.getElementsByName('s_d_duration_units')[0].checked = true;
	if (itemType == 'Service')
		document.getElementById('s_d_claim_item_qty').value = 1;
	else
		document.getElementById('s_d_claim_item_qty').value = '';

	document.getElementById('s_d_remarks').value = '';
	document.getElementById('s_d_consumption_uom').value = '';
	document.getElementById('s_d_medicineUOM').textContent = '';
	document.getElementById('s_d_genericNameAnchor_dialog').style.display = 'none';
	document.getElementById('s_d_genericNameAnchor_dialog').href = '';
	document.getElementById('s_d_genericNameAnchor_dialog').innerHTML = '';
	document.getElementById('s_d_generic_code').value = '';
	document.getElementById('s_d_generic_name').value = '';
	document.getElementById('s_d_granular_units').value = '';
	document.getElementById('s_d_ispackage').value = '';

	document.getElementById('s_d_package_size').value = '';
	document.getElementById('s_d_price').value = '';
	document.getElementById('s_d_pkg_size_label').textContent = '';

	var userUOMObj = document.getElementById('s_d_user_unit');
	setSelectedIndex(userUOMObj, "");

	document.getElementById('s_d_price_label').textContent = '';
	document.getElementById('s_d_qty_in_stock').value = '';

	document.getElementById('s_d_item_form_id').value = '';
	document.getElementById('s_d_item_strength').value = '';

	document.getElementById('s_d_code_type').textContent = '';
	document.getElementById('s_d_code').textContent = '';
	document.getElementById('s_d_claim_item_amount').textContent = '';
	document.getElementById('s_d_claim_item_qty').value = '';
	document.getElementById('s_d_claim_item_pat_amount').textContent = '';
	document.getElementById('s_d_claim_item_disc').textContent = '';
	document.getElementById('s_d_claim_item_net_amount').value = '';

	document.getElementById('s_d_claim_item_orig_rate').textContent = '';
	document.getElementById('s_d_claim_item_orig_amount').textContent = '';
	document.getElementById('s_d_claim_item_orig_net_amount').textContent = '';

	var curDate = (gServerNow != null) ? gServerNow : new Date();
	document.getElementById('s_d_prescribed_date').value = formatDate(curDate, "ddmmyyyy", "-");

	if (document.getElementById('s_d_addActivity')) {
		document.getElementById('s_d_addActivity').checked = true;
	}
}


function FormatTextAreaValues(vText) {
	var vRtnText = vText;
	while (vRtnText.indexOf("\n") > -1) {
		vRtnText = vRtnText.replace("\n", " ");
	}
	while (vRtnText.indexOf("\r") > -1) {
		vRtnText = vRtnText.replace("\r", " ");
	}
	return vRtnText;
}

function formatCommentValue() {
	document.getElementById("_comments").value = FormatTextAreaValues(document.getElementById("_comments").value);
}

function formatClosureRemarksValue() {
	document.getElementById("_closure_remarks").value = FormatTextAreaValues(document.getElementById("_closure_remarks").value);
}

function UnFormatTextAreaValue(vText) {
	var vRtnText = vText;
	while (vRtnText.indexOf("~") > -1) {
		vRtnText = vRtnText.replace("~", "\n");
	}
	while (vRtnText.indexOf("^") > -1) {
		vRtnText = vRtnText.replace("^", "\r");
	}
	return vRtnText;
}

var siQtyFieldEdited = false;
function setSIQtyFieldsEdited() {
	siQtyFieldEdited = true;
}

function enableDisableQtyFieldsForCorrection() {
	var resubObj = document.getElementById("_resubmit_type");
	if (resubObj != null) {
		var resubtype = resubObj.value;
		if ((pbmStatus == 'D' || pbmStatus == 'R') && resubtype == 'correction') {

			document.getElementById("s_ed_claim_item_qty").readOnly = false;
			document.getElementById("s_ed_user_unit").disabled = false;
			document.getElementById("s_ed_frequency").disabled = false;
			document.getElementById("s_ed_duration").readOnly = false;

			var els = document.getElementsByName("s_ed_duration_units");

			for (var k=0; k<els.length; k++) {
				els[k].setAttribute("onchange", "setSIQtyFieldsEdited();setSIEdited();return calcQty(event, 's_ed');");
			}
		}else {

			var flag = document.getElementById("s_ed_user_unit").disabled;
			flag = ((pbmStatus == 'D' || pbmStatus == 'R') && resubtype != 'correction') ? true : flag;

			document.getElementById("s_ed_claim_item_qty").readOnly = flag;
			document.getElementById("s_ed_user_unit").disabled = flag;
			document.getElementById("s_ed_frequency").disabled = flag;

			var durElmt = document.getElementById("s_ed_duration");
			if (flag)
				durElmt.removeAttribute("onchange");
			else
				durElmt.setAttribute("onchange", "setSIQtyFieldsEdited();setSIEdited();return calcQty(event, 's_ed');");
			durElmt.readOnly = flag;

			var els = document.getElementsByName("s_ed_duration_units");

			for (var k=0; k<els.length; k++) {
				if (flag)
					els[k].removeAttribute("onchange");
				else
					els[k].setAttribute("onchange", "setSIQtyFieldsEdited();setSIEdited();return calcQty(event, 's_ed');");
			}
		}
	}
}

function validateComments() {
	formatCommentValue();
	var type = document.getElementById("_resubmit_type");
	var comment = document.getElementById("_comments");
	if (type.value == '') {
		alert("Please select resubmission type.");
		type.focus();
		return false;
	}
	if (trim(comment.value) == '') {
		alert("Please enter resubmission comments.");
		comment.focus();
		return false;
	}

	type.value = trim(type.value);
	comment.value = trim(comment.value);

	return true;
}

function enableResubmissionFields() {
	var type = document.getElementById("_resubmit_type");
	var comment = document.getElementById("_comments");
	var actionCheck = document.getElementById("actionChk");
	if (actionCheck != null) {
		if (actionCheck.checked && actionCheck.value == 'markForResubmission') {
			type.disabled = false;
			comment.disabled = false;
		} else {
			type.disabled = true;
			comment.disabled = true;
		}
	}
}

function validatePBMPresc() {
	var actionCheck = document.getElementById("actionChk");
	var pbmAction = "";
	if (actionCheck != null) {
		if (actionCheck.checked) {
			pbmAction = actionCheck.value;
		} else {
			pbmAction = "";
		}
	}
	var markResubmitObj = document.getElementById("markResubmit");
	if (markResubmitObj != null) markResubmitObj.value = "";
	if (pbmAction == 'markForResubmission') {
		if (markResubmitObj != null) markResubmitObj.value = pbmAction;
		if (!validateComments()) return false;
	}
	if (pbmStatus == 'R') {
		var resubtype = document.getElementById("_resubmit_type");
		var oldresubtype = document.getElementById("_old_resubmit_type");
		var comments = document.getElementById("_old_comments");
		var oldcomments = document.getElementById("_comments");
		if ((trim(oldresubtype.value) != trim(resubtype.value)) ||
			(trim(oldcomments.value) != trim(comments.value))) {

			resubtype.value = trim(resubtype.value);
			pbmAction = "markForResubmission";
		}
	}
	document.mainform._method.value = pbmAction;
	return true;
}

function doReopen() {
	document.mainform._method.value = "reopenPBMPresc";
	document.mainform.submit();
	return true;
}

function validateQtyEditOnCorrection() {
	var resubObj = document.getElementById("_resubmit_type");
	if (resubObj != null) {
		var resubtype = resubObj.value;
		// Only when resubmission type is correction the qty fields should be allowed to be edited
		// otherwise should not allow.
		if ((pbmStatus == 'D' || pbmStatus == 'R') && resubtype != 'correction' && siQtyFieldEdited) {
			alert("Please select Resubmit type as correction (or) reset these changed values: \n"
				+ " * Qty(UOM) \n"
				+ " * Frequency \n"
				+ " * Duration \n"
				+ " * Duration units. \n"
				+ " These should not be edited when resubmit type is internal complaint/legacy.");
			resubObj.focus();
			return false;
		}
	}
	return true;
}

function doSave() {

	if (!validateQtyEditOnCorrection())
		return false;

	if (!validateAddOrEditItemData())
		return false;

	if (!validatePBMPresc()) {
		return false;
	}

	var finalizeChk = document.getElementById("finalizeChk");
	var pbmAction = "";
	if (finalizeChk != null) {
		if (!finalizeChk.disabled) {
			if (finalizeChk.checked) {
				pbmAction = "finalizePBMPresc";
			}else {
				pbmAction = "";
			}
		}
		finalizeChk.value = pbmAction;
	}

	if (mod_eclaim_erx == 'Y') {
		if (!validateEprxFields(null, true))
		return false;

	}else if (mod_eclaim_pbm == 'Y') {
	   if (!validatePBMFields(null, true))
		return false;
	}

	if (!validateConsultationAndPBMFieldValues(null, true))
		return false;

	if (pbmAction != "" || itemsEdited != 0 || itemsAdded != 0)
		document.mainform._method.value = "savePBMDetails";

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