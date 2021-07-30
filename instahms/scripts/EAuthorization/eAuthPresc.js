var Dom = YAHOO.util.Dom;
var mainform = null;
var itemsEdited = 0;
var itemsAdded = 0;

var totalAmountPaise = 0;
var totalDiscountPaise = 0;
var totalGrossAmountPaise = 0;
var totalPatientAmountPaise = 0;
var totalClaimNetAmountPaise = 0;
var totalApprovedNetAmountPaise = 0;

function initPreAuthPresc() {

	mainform = document.mainform;

	var idx = 0;
	S_S_NO = idx++;
	S_PRESC_DATE = idx++;
	S_PRESC_SEND_FLAG = idx++;
	S_PREAUTH_ACT_TYPE = idx++;
	S_PREAUTH_ACT_NAME = idx++;
	S_TOOTH_NUMBER = idx++;
	S_PREAUTH_ACT_REMARKS = idx++;

	S_PREAUTH_ACT_CODE_TYPE = idx++;
	S_PREAUTH_ACT_CODE = idx++;

	S_PREAUTH_ID = idx++;
	S_PREAUTH_MODE = idx++;

	ITEM_RATE_COL = idx++;
	ITEM_QTY_COL = idx++;
	REM_QTY_COL = idx++;
	ITEM_DISC_COL = idx++;
	ITEM_AMOUNT_COL = idx++;
	ITEM_PATIENT_AMT_COL = idx++;
	ITEM_CLAIM_AMT_COL = idx++;

	S_TRASH_COL = idx++;
   S_EDIT_COL = idx++;
   ITEM_APPRD_QTY_COL = idx++;
   ITEM_APPRD_AMT_COL = idx++;
   ITEM_PREAUTH_STATUS_COL = idx++;

	clearSIFields();
	clearFields();
	initSIDialog();
	initEditSIDialog();
	initSIItemAutoComplete();
	initSIEditItemAutoComplete();
	initInsurancePhotoDialog();

	initInfoDialogs();
	initToothNumberDialog();
	initEncCodeAutoComplete();
	initDrgCodeAutoComplete();
	initPerdiemCodeAutoComplete();
	if (document.getElementById("_comments"))
	document.getElementById("_comments").value = UnFormatTextAreaValue(document.getElementById("_comments").value);

	if (TPAEAuthMode == 'M') {
		document.getElementById("s_d_preauth_act_status").disabled = false;
		document.getElementById("s_ed_preauth_act_status").disabled = false;
	}

	var editDocConsTypesObj = document.getElementById("s_ed_doc_cons_type");
	var addDocConsTypesObj = document.getElementById("s_d_doc_cons_type");
	removeDupsAndSortDropDown(editDocConsTypesObj);
	removeDupsAndSortDropDown(addDocConsTypesObj);

	var editCodeTypesObj = document.getElementById("s_ed_code_type");
	var addCodeTypesObj = document.getElementById("s_d_code_type");
	removeDupsAndSortDropDown(editCodeTypesObj);
	removeDupsAndSortDropDown(addCodeTypesObj);
}


function recalcItemAmount() {

	var id = document.getElementById('s_ed_editRowId').value ;
	var row = getChargeRow(id, 'siTable');

	var editclaimObj = document.getElementById("s_ed_claim_item_net_amount");
	var editRateLblObj = document.getElementById("s_ed_price_label");
	var editAmtLblObj = document.getElementById("s_ed_claim_item_amount");
	var editPatAmtLblObj = document.getElementById("s_ed_claim_item_pat_amount");
	var editClaimApprovedAmountObj = document.getElementById("s_ed_claim_approved_amt");

	if (editclaimObj != null)
		if (!validateAmount(editclaimObj, "Claim amount must be a valid amount"))
			return false;
	
	if (editClaimApprovedAmountObj) {
		if(!validateAmount(editClaimApprovedAmountObj, "Claim Approved Amount must be a valid amount")) {
			editClaimApprovedAmountObj.value = 0.00;
			return false;
		}
		if(editclaimObj){
			var claimAmountPaise = getPaise(editclaimObj.value);
			var approvedClaimAmtPaise = getPaise(editClaimApprovedAmountObj.value);
			if(claimAmountPaise < approvedClaimAmtPaise){
				alert('Approved Amount cannot be greater than claim amount');
				editClaimApprovedAmountObj.value = 0.00; // reset Approved Amount to 0 
				return false;
			}
		}
	}

	var amountObj = getIndexedFormElement(mainform, "s_item_amount", id);
	var qtyObj = getIndexedFormElement(mainform, "s_item_qty", id);
	var rateObj = getIndexedFormElement(mainform, "s_item_rate", id);
	var discObj = getIndexedFormElement(mainform, "s_item_disc", id);

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

			itemsEdited++;
		}

		if (getPaise(claimAmtObj.value) == getPaise(origClaimAmtObj.value)) {
			edited = false;
		}
	}
	resetClaimTotals();
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

	var itemType = getItemType();

	var itemTypeTxt = '';
	if (itemType == 'DIA')
		itemTypeTxt = 'Investigation';
	else if (itemType == 'SER')
		itemTypeTxt = 'Service';
	else if (itemType == 'OPE')
		itemTypeTxt = 'Operation';
	else if (itemType == 'DOC')
		itemTypeTxt = 'Doctor';
	else if (itemType == 'ITE')
		itemTypeTxt = 'Inventory';

	var itemName = document.getElementById('s_d_itemName').value;
	var itemId = document.getElementById('s_d_item_id').value;

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (itemType == 'DIA') {
		var itemFromPackage = document.getElementById('s_d_ispackage').value;
		if (isInsurancePatient && itemFromPackage == 'true') {
			alert(" Patient TPA requires Online Prior authorization. \n " +
				"Package Items not allowed.");
			return false;
		}
	}

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
	var approvedQty = document.getElementById('s_d_claim_approved_qty').value;

	if (getAmount(qty) == 0) {
  		alert("Quantity is required.");
  		document.getElementById('s_d_claim_item_qty').focus();
  		return false;
  	}

	if (getAmount(approvedQty) > getAmount(qty)) {
		alert("Approved Quantity Cannot be more than the Item Quantity");
		document.getElementById('s_d_claim_approved_qty').focus();
		return false;
	}

  	if (!validateInteger(document.getElementById('s_d_claim_item_qty'), "Quantity must be a valid amount"))
		return false;

	if (!validateInteger(document.getElementById('s_d_claim_approved_qty'), "Approved Quantity must be a valid amount"))
		return false;

  	var claimApprovedAmountObj  = document.getElementById('s_d_claim_item_approved_amount');
  	if(claimApprovedAmountObj) {
  		if(!validateAmount(claimApprovedAmountObj, 'Claim Approved Amount has to be a valid Amount')) {
  			return false;
  		}
  		var claimAmountObj =  document.getElementById('s_d_claim_item_net_amount');
		if(claimAmountObj){
			var claimApprovedAmount = claimApprovedAmountObj.value;
			if(claimApprovedAmount > claimAmountObj.value) {
				alert('Claim Approved Amount cannot be higher than Claim Amount');
				return false;
			}
		}
  		
  	}
  	
  	var tooth_number = document.getElementById('s_d_tooth_number').value;
  	var tooth_num_required = document.getElementById('s_d_tooth_num_required').value;

  	if(itemType == 'SER' && tooth_num_required == 'Y') {
  		var obsCodeType = Dom.getElementsByClassName("addobsRowCount");
  		var obsCodes = "";
  		var first = true;
	  	for(var i=1;i<=obsCodeType.length;i++) {
	  		if(document.getElementById('addobsCodeType.'+i).value == 'Universal Dental') {
	  			if(!empty(document.getElementById('addobsCode.'+i).value)) {
		  			if(!first)
						obsCodes = obsCodes+","+document.getElementById('addobsCode.'+i).value;
					else
						obsCodes = document.getElementById('addobsCode.'+i).value;
					first = false;
				}
	  		}
	  	}
	  	tooth_number = obsCodes;
  	}

  	if (tooth_num_required == 'Y' && tooth_number == '') {
  		alert('Please add observation as tooth number for the service.');
  		return false;
  	}

	var sDMarkPriorAuthReqTdObj = document.getElementById('s_d_markPriorAuthReqTd');
	var sDMarkPriorAuthReqObj = document.getElementById('s_d_markPriorAuthReq');
  	var docConsType = document.getElementById('s_d_doc_cons_type').value;
  	if (itemType == 'DOC' && sDMarkPriorAuthReqTdObj != null && sDMarkPriorAuthReqObj.checked && docConsType == "") {
  		alert('Doctor consultation type is required.');
  		return false;
  	}

	var master = document.getElementById('s_d_item_master').value;
  	var remarks = document.getElementById('s_d_item_remarks').value;

  	var ispackage = document.getElementById('s_d_ispackage').value;
  	var price = getPaise(document.getElementById('s_d_price').value);

  	var addActivityEl = document.getElementById('s_d_addActivity');
  	var prescribedDate = document.getElementById('s_d_prescribed_date').value;
	var prescribedTime = formatTime(new Date(), 'Y');
  	var code = document.getElementById('s_d_code').value;
  	var codeType = document.getElementById('s_d_code_type').value;
  	var preauthId = document.getElementById('s_d_preauth_id').value;
  	var preauthMode = document.getElementById('s_d_preauth_mode').value;
  	var preauthModeTxt = '';
  	if(TPAEAuthMode == "O"){
		preauthMode = "4";
		preauthModeTxt = "Electronic";
	}
  	if (!empty(preauthMode))
  		document.getElementById('s_d_preauth_mode').options[document.getElementById('s_d_preauth_mode').selectedIndex].text;

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

	setNodeText(row.cells[ITEM_QTY_COL], qty);

	setNodeText(row.cells[REM_QTY_COL], qty);

   var addActivity = 'false';
   if (addActivityEl && addActivityEl.checked) {
   		addActivity = 'true';
	}

	var prescDateObj = document.getElementById('s_d_prescribed_date');
	if (!validateRequired(prescDateObj, "Date is required"))
		return false;

	if (!doValidateDateField(prescDateObj))
		return false;

  	setNodeText(row.cells[S_PRESC_DATE], prescribedDate);

	setHiddenValue(id, "s_preauth_act_id", "_");
	setHiddenValue(id, "s_prescribed_date", prescribedDate);
	setHiddenValue(id, "s_prescribedTime", prescribedTime);

	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "obserActItemId", itemId);

	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);
	setHiddenValue(id, "s_issued", "N");

	setNodeText(row.cells[S_PREAUTH_ACT_TYPE], itemTypeTxt, 5);

	setNodeText(row.cells[S_PREAUTH_ACT_REMARKS], remarks, 10);
	setNodeText(row.cells[S_PREAUTH_ACT_CODE_TYPE], codeType);
	setNodeText(row.cells[S_PREAUTH_ACT_CODE], code);

	setNodeText(row.cells[S_PREAUTH_ID], preauthId);
	setNodeText(row.cells[S_PREAUTH_MODE], preauthModeTxt);

	setHiddenValue(id, "s_item_code_type", codeType);
	setHiddenValue(id, "s_item_code", code);
	setHiddenValue(id, "s_item_preauth_id", preauthId);
	setHiddenValue(id, "s_item_preauth_mode", preauthMode);

	setHiddenValue(id, "s_doc_cons_type", docConsType);

	setNodeText(row.cells[S_PREAUTH_ACT_NAME], itemName + (tooth_number == '' ? '' : ('[' + tooth_number + ']')), 12);
	setNodeText(row.cells[S_TOOTH_NUMBER], tooth_number);

	setHiddenValue(id, "tooth_num_required", tooth_num_required);

	if (tooth_num_required == 'Y') {
		if (tooth_numbering_system == 'U')
			setHiddenValue(id, 'tooth_unv_number', tooth_number);
		else
			setHiddenValue(id, 'tooth_fdi_number', tooth_number);
	}

	var priorAuth = document.getElementById('s_d_priorAuth').value;
	setHiddenValue(id, "s_priorAuth", priorAuth);

	var sendForPriorAuthFlagSrc = "";
	var markPriorAuthReqObj = document.getElementById('s_d_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		if (markPriorAuthReqObj.checked) {
			setHiddenValue(id, "s_preauth_required", "Y");
			sendForPriorAuthFlagSrc = cpath + '/images/request.png';
		}else {
			setHiddenValue(id, "s_preauth_required", "N");
			sendForPriorAuthFlagSrc = cpath + '/images/request1.png';
		}
	}

	var img = document.createElement("img");
	img.setAttribute("src", sendForPriorAuthFlagSrc);
	for (var i=row.cells[S_PRESC_SEND_FLAG].childNodes.length-1; i>=0; i--) {
		row.cells[S_PRESC_SEND_FLAG].removeChild(row.cells[S_PRESC_SEND_FLAG].childNodes[i]);
	}
	row.cells[S_PRESC_SEND_FLAG].appendChild(img);

	var preauthActStatus = document.getElementById('s_d_preauth_act_status').value;

	setHiddenValue(id, "s_status", "A");
	setHiddenValue(id, "s_preauth_act_status", preauthActStatus);
	var ratePaise = getPaise(document.getElementById("s_d_price_label").textContent);
	var discPaise = getPaise(document.getElementById("s_d_claim_item_disc").textContent);
	var amtPaise = getPaise(document.getElementById("s_d_claim_item_amount").textContent);
	var patPaise = getPaise(document.getElementById("s_d_claim_item_pat_amount").textContent);
	var claimPaise = getPaise(document.getElementById("s_d_claim_item_net_amount").value);
	var claimApprovedPaise = getPaise(document.getElementById("s_d_claim_item_approved_amount").value);
	var remApprvdQty =  approvedQty;

	setHiddenValue(id, "s_item_qty", qty);
	setHiddenValue(id,"s_item_rem_qty", qty);
	setHiddenValue(id, "s_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_item_disc", formatAmountPaise(discPaise));

	setHiddenValue(id, "s_patient_amount", formatAmountPaise(patPaise));
	setHiddenValue(id, "s_claim_net_amount", formatAmountPaise(claimPaise));

    setHiddenValue(id, "s_claim_approved_amount", formatAmountPaise(claimApprovedPaise));

	setHiddenValue(id, "s_orig_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_orig_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_orig_claim_net_amount", formatAmountPaise(claimPaise));
	setHiddenValue(id, "s_item_approved_qty", approvedQty);
	setHiddenValue(id, "s_item_approved_rem_qty", remApprvdQty);

	setNodeText(row.cells[ITEM_RATE_COL], formatAmountPaise(ratePaise));
	setNodeText(row.cells[ITEM_DISC_COL], formatAmountPaise(discPaise));
	setNodeText(row.cells[ITEM_AMOUNT_COL], formatAmountPaise(amtPaise));
	setNodeText(row.cells[ITEM_PATIENT_AMT_COL], formatAmountPaise(patPaise));
	setNodeText(row.cells[ITEM_CLAIM_AMT_COL], formatAmountPaise(claimPaise));
	setNodeText(row.cells[ITEM_APPRD_QTY_COL],approvedQty );
	setNodeText(row.cells[ITEM_APPRD_AMT_COL], formatAmountPaise(claimApprovedPaise));

	setNodeText(row.cells[ITEM_QTY_COL], qty);
	
	setNodeText(row.cells[REM_QTY_COL], qty);

	// Set observations in grid for added item.
	addObservationsToGrid('add', itemId, row);
	resetObservationPanel('add');

	var img = document.createElement("img");
	img.setAttribute("src", cpath + "/images/empty_flag.gif");
	for (var i=row.cells[ITEM_PREAUTH_STATUS_COL].childNodes.length-1; i>=0; i--) {
		row.cells[ITEM_PREAUTH_STATUS_COL].removeChild(row.cells[ITEM_PREAUTH_STATUS_COL].childNodes[i]);
	}
	var text = document.createTextNode("Open");
	row.cells[ITEM_PREAUTH_STATUS_COL].appendChild(img);
	row.cells[ITEM_PREAUTH_STATUS_COL].appendChild(text);

	itemsAdded++;
	setSIRowStyle(id);
	clearSIFields();
	this.align("tr", "tl");
	document.getElementById('s_d_itemName').focus();
	resetClaimTotals();
	renameFileUploadEl();
	return id;
}

function renameFileUploadEl() {
	var table = document.getElementById('siTable');
	var uploadEls = YAHOO.util.Dom.getElementsByClassName("preAuthFileUpload", 'input', table);
	for (var i=0; i<uploadEls.length; i++) {
		var el = uploadEls[i];
		var row = getThisRow(el);

		var id = row.rowIndex - 1;
		el.setAttribute("name", "activity_file_upload[" + id + "]");
		el.setAttribute("id", "activity_file_upload"+id);
	}
}

function editSITableRow() {

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

	if (allowPreauthPrescriptionEdit != 'A') {
		alert("You are not authorized to edit Prior Auth Prescription.");
		return false;
	}

	var itemTypeTxt = '';
	if (itemType == 'DIA')
		itemTypeTxt = 'Investigation';
	else if (itemType == 'SER')
		itemTypeTxt = 'Service';
	else if (itemType == 'OPE')
		itemTypeTxt = 'Operation';
	else if (itemType == 'DOC')
		itemTypeTxt = 'Doctor';

	var qty = Number(document.getElementById('s_ed_claim_item_qty').value);
	var approvedQty = Number(document.getElementById('s_ed_claim_approved_qty').value);
	var remQty = Number(document.getElementById('s_ed_claim_approved_rem_qty').value);
	var consumedQty = qty - remQty;

	if (getAmount(qty) == 0) {
  		alert("Quantity is required.");
  		document.getElementById('s_ed_claim_item_qty').focus();
  		return false;
  	}

	if (getAmount(approvedQty) > getAmount(qty)) {
		alert("Approved Quantity Cannot be more than the Item Quantity");
		document.getElementById('s_ed_claim_approved_qty').focus();
		return false;
	}

	if ( qty < consumedQty) {
		alert("Quantity Cannot be less then Consumed Qty.");
        document.getElementById('s_ed_claim_item_qty').focus();
        return false;
	}

  	if (!validateInteger(document.getElementById('s_ed_claim_item_qty'), "Quantity must be a valid amount"))
		return false;

	if (!validateInteger(document.getElementById('s_ed_claim_approved_qty'), "Approved Quantity must be a valid amount")){
		return false;
	}

  	var tooth_number = document.getElementById('s_ed_tooth_number').value;
  	var tooth_num_required = document.getElementById('s_ed_tooth_num_required').value;
  /*	if (tooth_num_required == 'Y' && tooth_number == '') {
  		alert('Tooth Number required for the service.');
  		return false;
  	}*/

  	if(itemType == 'SER' && tooth_num_required == 'Y') {
  		var obsCodeType = Dom.getElementsByClassName("obsRowCount");
  		var obsCodes = "";
  		var first = true;
	  	for(var i=1;i<=obsCodeType.length;i++) {
	  		if(document.getElementById('obsCodeType.'+i).value == 'Universal Dental') {
	  			if(!empty(document.getElementById('obsCode.'+i).value)) {
		  			if(!first)
						obsCodes = obsCodes+","+document.getElementById('obsCode.'+i).value;
					else
						obsCodes = document.getElementById('obsCode.'+i).value;
					first = false;
				}
	  		}
	  	}
	  	tooth_number = obsCodes;
  	}

  	if (tooth_num_required == 'Y' && tooth_number == '') {
  		alert('Please add observation as tooth number for the service.');
  		return false;
  	}

	var sEdMarkPriorAuthReqTdObj = document.getElementById('s_ed_markPriorAuthReqTd');
	var sEdMarkPriorAuthReqObj = document.getElementById('s_ed_markPriorAuthReq');
  	var docConsType = document.getElementById('s_ed_doc_cons_type').value;
  	if (itemType == 'DOC' && sEdMarkPriorAuthReqTdObj != null && sEdMarkPriorAuthReqObj.checked && docConsType == "") {
  		alert('Doctor consultation type is required.');
  		return false;
  	}

	var remarks = document.getElementById('s_ed_item_remarks').value;
  	var master = document.getElementById('s_ed_item_master').value;
  	var ispackage = document.getElementById('s_ed_ispackage').value;

  	var prescribedDate = document.getElementById('s_ed_prescribed_date').value;
	var prescribedTime = formatTime(new Date(), 'Y');
	var prescDateObj = document.getElementById('s_ed_prescribed_date');
	if (!validateRequired(prescDateObj, "Date is required"))
		return false;

	if (!doValidateDateField(prescDateObj))
		return false;

	var addActivity = 'false';

  	var addActivityEl = document.getElementById('s_ed_addActivity');
  	if (addActivityEl && addActivityEl.checked) {
  		addActivity = 'true';
  	}

  	var code = document.getElementById('s_ed_code').value;
  	var codeType = document.getElementById('s_ed_code_type').value;
  	var preauthId = document.getElementById('s_ed_preauth_id').value;
  	var preauthMode = document.getElementById('s_ed_preauth_mode').value;
  	var preauthModeTxt = '';
  	if(TPAEAuthMode == "O"){
		preauthMode = "4";
		preauthModeTxt = "Electronic";
	}
  	if (!empty(preauthMode))
  		document.getElementById('s_ed_preauth_mode').options[document.getElementById('s_ed_preauth_mode').selectedIndex].text;


	setNodeText(row.cells[S_PRESC_DATE], prescribedDate);
	setHiddenValue(id, "s_prescribed_date", prescribedDate);
	setHiddenValue(id, "s_prescribedTime", prescribedTime);

	setHiddenValue(id, "s_itemType", itemType);
	setHiddenValue(id, "s_item_name", itemName);
	setHiddenValue(id, "s_item_id", itemId);
	setHiddenValue(id, "s_item_remarks", remarks);
	setHiddenValue(id, "s_item_master", master);
	setHiddenValue(id, "obserActItemId", itemId);

	setHiddenValue(id, "s_ispackage", ispackage);
	setHiddenValue(id, "s_addActivity", addActivity);

	setNodeText(row.cells[S_PREAUTH_ACT_TYPE], itemTypeTxt, 5);

	setNodeText(row.cells[S_PREAUTH_ACT_REMARKS], remarks, 10);
	setNodeText(row.cells[S_PREAUTH_ACT_CODE_TYPE], codeType);
	setNodeText(row.cells[S_PREAUTH_ACT_CODE], code);

	setNodeText(row.cells[S_PREAUTH_ID], preauthId);
	setNodeText(row.cells[S_PREAUTH_MODE], preauthModeTxt);

	setHiddenValue(id, "s_item_code_type", codeType);
	setHiddenValue(id, "s_item_code", code);
	var codeDesc = '';
	if (trim(code) != '')
		codeDesc = document.getElementById('s_ed_code_desc').textContent;
	setHiddenValue(id, "s_item_code_desc", codeDesc);
	setHiddenValue(id, "s_item_preauth_id", preauthId);
	setHiddenValue(id, "s_item_preauth_mode", preauthMode);

	setHiddenValue(id, "s_doc_cons_type", docConsType);

	setNodeText(row.cells[S_PREAUTH_ACT_NAME], itemName + (tooth_number == '' ? '' : ('[' + tooth_number + ']')), 12);
	setNodeText(row.cells[S_TOOTH_NUMBER], tooth_number);

	setHiddenValue(id, "tooth_num_required", tooth_num_required);

	if (tooth_num_required == 'Y') {
		if (tooth_numbering_system == 'U')
			setHiddenValue(id, 'tooth_unv_number', tooth_number);
		else
			setHiddenValue(id, 'tooth_fdi_number', tooth_number);
	}

	var sendForPriorAuthFlagSrc = "";
	var markPriorAuthReqObj = document.getElementById('s_ed_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		if (markPriorAuthReqObj.checked) {
			setHiddenValue(id, "s_preauth_required", "Y");
			sendForPriorAuthFlagSrc = cpath + '/images/request.png';
		}else {
			setHiddenValue(id, "s_preauth_required", "N");
			sendForPriorAuthFlagSrc = cpath + '/images/request1.png';
		}
	}

	var img = document.createElement("img");
	img.setAttribute("src", sendForPriorAuthFlagSrc);
	for (var i=row.cells[S_PRESC_SEND_FLAG].childNodes.length-1; i>=0; i--) {
		row.cells[S_PRESC_SEND_FLAG].removeChild(row.cells[S_PRESC_SEND_FLAG].childNodes[i]);
	}
	row.cells[S_PRESC_SEND_FLAG].appendChild(img);

	var preauthActStatus = document.getElementById('s_ed_preauth_act_status').value;

	setHiddenValue(id, "s_status", "A");
	setHiddenValue(id, "s_preauth_act_status", preauthActStatus);

	setActStatusFlag(preauthActStatus, row);

	var ratePaise = getPaise(document.getElementById("s_ed_price_label").textContent);
	var discPaise = getPaise(document.getElementById("s_ed_claim_item_disc").textContent);
	var amtPaise = getPaise(document.getElementById("s_ed_claim_item_amount").textContent);
	var patPaise = getPaise(document.getElementById("s_ed_claim_item_pat_amount").textContent);
	var claimPaise = getPaise(document.getElementById("s_ed_claim_item_net_amount").value);
	var claimApprovedPaise = getPaise(document.getElementById("s_ed_claim_approved_amt").value);
	var apprvdQty = Number(document.getElementById("s_ed_claim_approved_qty").value);
	
	var remQty = Number(document.getElementById('s_ed_claim_item_rem_qty').value);
	var prevQty = Number(getIndexedValue("s_item_qty", id));
	if (qty > prevQty) {
	  remQty = Number(remQty) + (qty - prevQty);
	} else if ((qty < prevQty) && (qty < remQty)) {
	  remQty = qty;
	}
	var remApprvdQty = Number(document.getElementById('s_ed_claim_approved_rem_qty').value);
	var prevApprvdQty = Number(getIndexedValue("s_item_approved_qty", id));
	if (apprvdQty > prevApprvdQty) {
	  remApprvdQty = Number(remApprvdQty) + (apprvdQty - prevApprvdQty);
	} else if ((apprvdQty < prevApprvdQty) && (apprvdQty < remApprvdQty)) {
		remApprvdQty = apprvdQty;
	}

	setHiddenValue(id, "s_item_qty", qty);
	setHiddenValue(id,"s_item_rem_qty", remQty);
	setHiddenValue(id, "s_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_item_disc", formatAmountPaise(discPaise));

	setHiddenValue(id, "s_patient_amount", formatAmountPaise(patPaise));
	setHiddenValue(id, "s_claim_net_amount", formatAmountPaise(claimPaise));

	setHiddenValue(id, "s_orig_item_rate", formatAmountPaise(ratePaise));
	setHiddenValue(id, "s_orig_item_amount", formatAmountPaise(amtPaise));
	setHiddenValue(id, "s_orig_claim_net_amount", formatAmountPaise(claimPaise));

	setHiddenValue(id, "s_claim_approved_amount", formatAmountPaise(claimApprovedPaise));
	setHiddenValue(id, "s_item_approved_qty", apprvdQty);
	setHiddenValue(id, "s_item_approved_rem_qty", remApprvdQty);

	setNodeText(row.cells[ITEM_RATE_COL], formatAmountPaise(ratePaise));
	setNodeText(row.cells[ITEM_DISC_COL], formatAmountPaise(discPaise));
	setNodeText(row.cells[ITEM_AMOUNT_COL], formatAmountPaise(amtPaise));
	setNodeText(row.cells[ITEM_PATIENT_AMT_COL], formatAmountPaise(patPaise));
	setNodeText(row.cells[ITEM_CLAIM_AMT_COL], formatAmountPaise(claimPaise));
	setNodeText(row.cells[ITEM_APPRD_QTY_COL], apprvdQty);
	setNodeText(row.cells[ITEM_APPRD_AMT_COL], formatAmountPaise(claimApprovedPaise));

	setNodeText(row.cells[ITEM_QTY_COL], qty);
	setNodeText(row.cells[REM_QTY_COL], remQty);

	// Set observations in grid for chosen item.
	addObservationsToGrid('edit', itemId, row);

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

	parentDialog = editSIDialog;
	var row = getThisRow(obj);

	var id = getRowChargeIndex(row);
	editSIDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editSIDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('s_ed_editRowId').value = id;

	var itemName = getIndexedValue("s_item_name", id);
	var itemId = getIndexedValue("s_item_id", id);
	var itemType = getIndexedValue("s_itemType", id);

	resetObservationPanel('edit');
	initializeObservationDialogFields(itemId);

	var itemTypeTxt = '';
	if (itemType == 'DIA')
		itemTypeTxt = 'Investigation';
	else if (itemType == 'SER')
		itemTypeTxt = 'Service';
	else if (itemType == 'OPE')
		itemTypeTxt = 'Operation';
	else if (itemType == 'DOC')
		itemTypeTxt = 'Doctor';

	document.getElementById('s_ed_itemTypeLabel').textContent = itemTypeTxt;

	document.getElementById('s_ed_itemName').value = itemName;
	document.getElementById('s_ed_item_id').value = itemId;
	document.getElementById('s_ed_itemType').value = itemType;

	document.getElementById('s_ed_toothLabelTd').style.display = 'none';
	document.getElementById('s_ed_toothValueTd').style.display = 'none';

	document.getElementById('s_ed_docConsLabelTd').style.display = 'none';
	document.getElementById('s_ed_docConsValueTd').style.display = 'none';

	if (document.getElementById('s_ed_markPriorAuthReq'))
		document.getElementById('s_ed_markPriorAuthReq').checked = false;

	var itemCodeType = getIndexedValue("s_item_code_type", id);
	initTrtCodesAutocomp('s_ed_code', 's_ed_trtDropDown', itemCodeType, 's_ed');

	document.getElementById('s_ed_prescribed_date').value = getIndexedValue('s_prescribed_date', id);

	document.getElementById('s_ed_claim_item_qty').value = getIndexedValue("s_item_qty", id);
	document.getElementById('s_ed_claim_item_rem_qty').value = getIndexedValue("s_item_rem_qty", id);
	document.getElementById('s_ed_claim_approved_qty').value = getIndexedValue("s_item_approved_qty", id);
	document.getElementById('s_ed_claim_approved_rem_qty').value = getIndexedValue("s_item_approved_rem_qty", id);

	document.getElementById('s_ed_ispackage').value = getIndexedValue("s_ispackage", id);
	document.getElementById('s_ed_item_remarks').value = getIndexedValue('s_item_remarks', id);
	document.getElementById('s_ed_item_master').value = getIndexedValue('s_item_master', id);

	setSelectedIndex(document.getElementById('s_ed_code_type'), getIndexedValue("s_item_code_type", id));
	document.getElementById('s_ed_code').value = getIndexedValue("s_item_code", id);
	setNodeText(document.getElementById('s_ed_code_desc'), getIndexedValue("s_item_code_desc", id), 30);

	setSelectedIndex(document.getElementById('s_ed_preauth_mode'), getIndexedValue("s_item_preauth_mode", id));

	document.getElementById('s_ed_preauth_id').value = getIndexedValue("s_item_preauth_id", id);

	if (TPAEAuthMode == 'O' && overrideOnlinePriorAuthStatus == 'N') {
		document.getElementById('s_ed_preauth_id').disabled = true;
		document.getElementById('s_ed_claim_approved_amt').disabled = true;
		document.getElementById('s_ed_preauth_mode').disabled = true;
		
	}else if(TPAEAuthMode == 'M' || overrideOnlinePriorAuthStatus == 'A'){
		document.getElementById('s_ed_preauth_id').disabled = false;
		document.getElementById('s_ed_claim_approved_amt').disabled = false;
		document.getElementById('s_ed_preauth_mode').disabled = false;
	}

	if (itemType == 'SER') {
		document.getElementById('s_ed_toothLabelTd').style.display = 'table-cell';
		document.getElementById('s_ed_toothValueTd').style.display = 'table-cell';

	}else if (itemType == 'DOC') {
		document.getElementById('s_ed_docConsLabelTd').style.display = 'table-cell';
		document.getElementById('s_ed_docConsValueTd').style.display = 'table-cell';
	}

	setSelectedIndex(document.getElementById('s_ed_doc_cons_type'), getIndexedValue("s_doc_cons_type", id));
	document.getElementById('s_ed_doc_cons_type_old').value = getIndexedValue("s_doc_cons_type", id);

	document.getElementById('s_ed_tooth_num_required').value = getIndexedValue('tooth_num_required', id);
	if (document.getElementById('s_ed_tooth_num_required').value == 'Y') {
		document.getElementById('s_ed_ToothNumBtnDiv').style.display = 'none';
		document.getElementById('s_ed_ToothNumDsblBtnDiv').style.display = 'none';
	} else {
		document.getElementById('s_ed_ToothNumBtnDiv').style.display = 'none';
		document.getElementById('s_ed_ToothNumDsblBtnDiv').style.display = 'block';
	}
	var tooth_number = tooth_numbering_system == 'U' ? getIndexedValue('tooth_unv_number', id) : getIndexedValue('tooth_fdi_number', id);
	document.getElementById('s_ed_tooth_number').value = tooth_number;

	var tooth_numbers = tooth_number.split(",");
	var tooth_numbers_text = '';
	var checked_toothNos = 0;
	for (var i=0; i<tooth_numbers.length; i++) {
		if (tooth_numbers_text != '') {
			tooth_numbers_text += ',';
		}
		if (checked_toothNos%10 == 0)
			tooth_numbers_text += '\n';

		checked_toothNos++;
		tooth_numbers_text += tooth_numbers[i];
	}

	document.getElementById('s_ed_ToothNumberDiv').textContent = tooth_numbers_text;

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

	document.getElementById('s_ed_claim_approved_amt').value =
		formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_claim_approved_amount", id)));

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient) {
		document.getElementById('s_ed_priorAuthLabelTd').style.display = 'block';
		document.getElementById('s_ed_priorAuth_label').style.display = 'block';
	} else {
		document.getElementById('s_ed_priorAuthLabelTd').style.display = 'none';
		document.getElementById('s_ed_priorAuth_label').style.display = 'none';
	}

	if (isInsurancePatient) {
		document.getElementById('s_ed_markPriorAuthReqTd').style.display = 'block';
		document.getElementById('s_ed_markPriorAuthCheckBox').style.display = 'block';
	}else {
		document.getElementById('s_ed_markPriorAuthReqTd').style.display = 'none';
		document.getElementById('s_ed_markPriorAuthCheckBox').style.display = 'none';
	}

	var prior_auth = getIndexedValue('s_priorAuth', id);
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}
	document.getElementById('s_ed_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('s_ed_priorAuth').value = prior_auth;

	var preAuthRequired = getIndexedValue("s_preauth_required", id);
	if (document.getElementById('s_ed_markPriorAuthReq'))
		document.getElementById('s_ed_markPriorAuthReq').checked = (preAuthRequired == 'Y');


	var preauthActStatus = getIndexedValue("s_preauth_act_status", id);
	setSelectedIndex(document.getElementById('s_ed_preauth_act_status'), preauthActStatus);
	if(overrideOnlinePriorAuthStatus == 'A') {
		document.getElementById('s_ed_preauth_act_status').disabled = false;
	}

	if (overrideOnlinePriorAuthStatus == 'A') {
		document.getElementById('s_ed_claim_approved_qty').disabled = false;
	} else {
		document.getElementById('s_ed_claim_approved_qty').disabled = true;
	}

	var patPrescId = getIndexedValue('s_item_patient_presc_id', id);
	if(null == patPrescId || patPrescId == 0) {
		document.getElementById('s_ed_markPriorAuthReq').disabled = true;
	}

	document.getElementById('s_ed_item_remarks').focus();

	return false;
}


function clearSIFields() {

	document.getElementById('s_d_itemName').value = '';
	document.getElementById('s_d_item_id').value = '';

	document.getElementById('s_d_ispackage').value = '';
	document.getElementById('s_d_price').value = '';
	document.getElementById('s_d_price_label').textContent = '';

	setSelectedIndex(document.getElementById('s_d_code_type'), '');
	document.getElementById('s_d_code').value = '';
	document.getElementById('s_d_code_desc').textContent = '';

	setSelectedIndex(document.getElementById('s_d_preauth_mode'), '');
	document.getElementById('s_d_preauth_id').value = '';

	document.getElementById('s_d_item_remarks').value = '';
	document.getElementById('s_d_item_master').value = '';

	document.getElementById('s_d_priorAuth').value = '';
	document.getElementById('s_d_tooth_num_required').value = '';
	document.getElementById('s_d_tooth_number').value = '';

	document.getElementById('s_d_claim_item_amount').textContent = '';
	document.getElementById('s_d_claim_item_qty').value = '';
	document.getElementById('s_d_claim_item_pat_amount').textContent = '';
	document.getElementById('s_d_claim_item_disc').textContent = '';
	document.getElementById('s_d_claim_item_net_amount').value = '';
	document.getElementById('s_d_claim_approved_qty').value = '';

	document.getElementById('s_d_claim_item_orig_rate').textContent = '';
	document.getElementById('s_d_claim_item_orig_amount').textContent = '';
	document.getElementById('s_d_claim_item_orig_net_amount').textContent = '';

	setSelectedIndex(document.getElementById('s_d_preauth_act_status'), 'O');

	var markPriorAuthReqObj = document.getElementById('s_d_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		markPriorAuthReqObj.checked = true;
	}

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

function validateEAuthPresc() {
	var actionCheck = document.getElementById("actionChk");
	var preauthAction = "";
	if (actionCheck != null) {
		if (actionCheck.checked) {
			preauthAction = actionCheck.value;
		} else {
			preauthAction = "";
		}
	}
	var markResubmitObj = document.getElementById("markResubmit");
	if (markResubmitObj != null) markResubmitObj.value = "";
	if (preauthAction == 'markForResubmission') {
		if (markResubmitObj != null) markResubmitObj.value = preauthAction;
		if (!validateComments()) return false;
	}
	if (preauthStatus == 'R') {
		var resubtype = document.getElementById("_resubmit_type");
		var oldresubtype = document.getElementById("_old_resubmit_type");
		var comments = document.getElementById("_old_comments");
		var oldcomments = document.getElementById("_comments");
		if (resubtype != null && oldresubtype != null) {
			if ((trim(oldresubtype.value) != trim(resubtype.value)) ||
				(trim(oldcomments.value) != trim(comments.value))) {

				resubtype.value = trim(resubtype.value);
				preauthAction = "markForResubmission";
			}
		}
	}
	document.mainform._method.value = preauthAction;
	return true;
}

function validateNoOfActivities() {
	var num = getNumCharges("siTable");
	var perdiemCodeObj = document.getElementById("perdiemCode");
	var drgCodeObj = document.getElementById("drgCode");
	var perdiemCodeVal = (perdiemCodeObj != null) ? trim(perdiemCodeObj.value) : "";
	var drgCodeVal = (drgCodeObj != null) ? trim(drgCodeObj.value) : "";
	if (num == 0 && perdiemCodeVal == "" && drgCodeVal == "") {
		alert("Prescription has no activities/items to save.");
		return false;
	}
	return true;
}

function anyObservationValuesEmpty() {
	var isEmpty = false;
	var acts = document.getElementsByName("obserActItemId");
	for (var j=0; j<acts.length; j++) {
		var actsObs = document.getElementsByName("obserCode." +acts[j].value);
		isEmpty = false;
		for (var i=0; i<actsObs.length; i++) {
	      var dlg_obsCode = document.getElementById("obserCode." +acts[j].value + (i+1));
	      var dlg_obsCodeType = document.getElementById("obserType." +acts[j].value + (i+1));
	      var dlg_obsValue = document.getElementById("obserValue." +acts[j].value + (i+1));
	      var dlg_obsValueType = document.getElementById("obserValueType." +acts[j].value + (i+1));

	      if (dlg_obsCodeType != null && dlg_obsCodeType.value != ''
	      			&& dlg_obsCode != null && (dlg_obsCode.value).trim() != ''
	      			&& dlg_obsValue != null && (dlg_obsValue.value).trim() == '' ) {
				isEmpty = true;
				break;
	      }
	   }
	   if (isEmpty) {
		 	return isEmpty;
		}
	}
	return isEmpty;
}

function doSave() {

	if (!validateEAuthPresc()) {
		return false;
	}

	if (empty(document.mainform.patient_id.value)) {
		alert("No changes made to save.");
		return false;
	}

	if (empty(document.mainform.encCode.value)) {
		alert("Encounter Type is Required.");
		document.mainform.encCode.focus();
		return false;
	}

	if (op_type == 'O') {
		if (!validateDiagnosisDetails(false, diagnosisSectionMandatory, false))
			return false;
	}

	if (!validateNoOfActivities())
		return false;

	var perdiemNetObj = document.getElementById("perdiem_net");
	var drgNetObj = document.getElementById("drg_net");

	if (perdiemNetObj != null)
		if (!validateAmount(perdiemNetObj, "Perdiem Net amount must be a valid amount"))
			return false;
	if (drgNetObj != null)
		if (!validateAmount(drgNetObj, "DRG Net amount must be a valid amount"))
			return false;

	if (!validateEncEndDateTime()) return false;

	//if (itemsEdited != 0 || itemsAdded != 0)
		document.mainform._method.value = "saveEAuthDetails";

	if (document.mainform._method.value == "") {
		alert("No changes made to save.");
		return false;
	}

	if (anyObservationValuesEmpty()) {
		if (!confirm("Warning: Some of the Treatment Observation Value(s) are not entered, Proceed anyway ?."))
			return false;
	}

	if (allowPreauthPrescriptionEdit != 'A') {
		alert("You are not authorized to save Prior Auth Prescription.");
		return false;
	}

	document.mainform.submit();
	return true;
}

function validateEncEndDateTime() {
	var preauthEndDateObj = document.mainform.preauth_enc_end_date;
	var preauthEndTimeObj = document.mainform.preauth_enc_end_time;
	var drgCodeObj = document.mainform.drgCode;
	var drgCodeVal = drgCodeObj != null ? drgCodeObj.value : "";
	var valid = true;

	/*if (empty(drgCodeVal)) {
		if (preauthEndDateObj != null) preauthEndDateObj.value = "";
		if (preauthEndTimeObj != null) preauthEndTimeObj.value = "";
		return true;
	}*/

	if (!empty(drgCodeVal))
	valid = valid && validateRequired(preauthEndDateObj, "Encounter End date is required");
	if(!valid) {
		preauthEndDateObj.focus();
		return false;
	}

	if (!empty(drgCodeVal))
	valid = valid && validateRequired(preauthEndTimeObj, "Encounter End time is required");
	if(!valid) {
		preauthEndTimeObj.focus();
		return false;
	}

	if (!doValidateDateField(preauthEndDateObj)) {
		preauthEndDateObj.focus();
		return false;
	}

	if (!validateTime(preauthEndTimeObj)) {
		preauthEndTimeObj.focus();
		return false;
	}

	if (!empty(preauthEndTimeObj.value))
	if(trim(preauthEndTimeObj.value.length)!=5){
		alert("Incorrect time format : please enter HH:MI");
		preauthEndTimeObj.focus();
		return false;
	}

	var regDtObj = document.mainform.regDate;
	var regTimeObj = document.mainform.regTime;

	if (!empty(regDtObj.value) && !empty(regTimeObj.value)) {

		var regDt = getDateTimeFromField(regDtObj, regTimeObj);
		regDt.setSeconds(0);
		regDt.setMilliseconds(0);

		var encEndDt = getDateTimeFromField(preauthEndDateObj, preauthEndTimeObj);
		encEndDt.setSeconds(0);
		encEndDt.setMilliseconds(0);

		var diff = encEndDt - regDt;
		if (diff < 0) {
           alert("Encounter End & Time cannot be less than Reg date & time: " + regDtObj.value +" "+regTimeObj.value);
           preauthEndDateObj.focus();
           return false;
       }
	}
	return true;
}

function sendEAuthRequest() {
	var ok = confirm(" Prior Authorization Request will be Sent \n " +
			 				"Do you want to proceed with the prescribed activities. ? ");
	if (!ok) {
		return false;
	}
	var preauthPrescId = document.mainform.preauth_presc_id.value;
	var priority = document.mainform.priority.value;
	var insuranceCoId = document.mainform.insurance_co_id.value;
	var url = cpath+'/';
	url += 'EAuthorization/EAuthRequest.do';
	url += '?_method=sendEAuthRequest';
	url += '&preauth_presc_id='+preauthPrescId+'&insurance_co_id='+insuranceCoId+'&priority='+priority ;
	document.mainform.action = url;
	document.mainform.submit();
	return true;
}

function cancelEAuthRequest() {
	var ok = confirm(" Prior Authorization Request will be Cancelled. \n " +
			 				"Do you want to proceed ? ");
	if (!ok) {
		return false;
	}
	var preauthPrescId = document.mainform.preauth_presc_id.value;
	var priority = document.mainform.priority.value;
	var insuranceCoId = document.mainform.insurance_co_id.value;
	var url = cpath+'/';
	url += 'EAuthorization/EAuthRequest.do';
	url += '?_method=cancelEAuthRequest';
	url += '&preauth_presc_id='+preauthPrescId+'&insurance_co_id='+insuranceCoId+'&priority='+priority ;
	document.mainform.action = url;
	document.mainform.submit();
	return true;
}

function resubmitEAuthRequest() {
	var type = document.getElementById("_resubmit_type");
	var ok = confirm(" Prior Authorization Request will be Resubmitted. \n " +
			 				"Do you want to proceed with resubmit type as "+type.value+". ? ");
	if (!ok) {
		return false;
	}
	var preauthPrescId = document.mainform.preauth_presc_id.value;
	var priority = document.mainform.priority.value;
	var insuranceCoId = document.mainform.insurance_co_id.value;

	var url = cpath+'/';
	url += 'EAuthorization/EAuthRequest.do';
	url += '?_method=sendEAuthRequest';
	url += '&preauth_presc_id='+preauthPrescId+'&insurance_co_id='+insuranceCoId+'&priority='+priority ;
	document.mainform.action = url;
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
/* Given the priorAuth activity Status and the row in the table. Sets the correct flag and status text.*/
function setActStatusFlag(preauthActStatus, row) {
	var preauthActStatustext = "";
	var preauthFlagSrc = "";
	if (preauthActStatus == 'O') {
		preauthActStatustext = ' Open';
		preauthFlagSrc = cpath + '/images/empty_flag.gif';

	} else if (preauthActStatus == 'C') {
		preauthActStatustext = ' Approved';
		preauthFlagSrc = cpath + '/images/grey_flag.gif';

	} else if (preauthActStatus == 'D') {
		preauthActStatustext = ' Denied';
		preauthFlagSrc = cpath + '/images/red_flag.gif';
	}

	var img = document.createElement("img");
	img.setAttribute("src", preauthFlagSrc);
	for (var i = row.cells[ITEM_PREAUTH_STATUS_COL].childNodes.length - 1; i >= 0; i--) {
		row.cells[ITEM_PREAUTH_STATUS_COL]
				.removeChild(row.cells[ITEM_PREAUTH_STATUS_COL].childNodes[i]);
	}
	var text = document.createTextNode(preauthActStatustext);
	row.cells[ITEM_PREAUTH_STATUS_COL].appendChild(img);
	row.cells[ITEM_PREAUTH_STATUS_COL].appendChild(text);
}

/* Copies Prior Auth id, prior auth mode and prior auth activity status to all other activities in the table.
Refer https://practo.atlassian.net/browse/HMS-32075 */
function copyDialogToAllItems (obj) {
	var preAuthId = document.getElementById('copy_preauth_id').value;
	var preAuthMode = document.getElementById('copy_preauth_mode').value;
	var preAuthModeTxt = document.getElementById('copy_preauth_mode').options[preAuthMode].innerText;
	var preauthActStatus = document.getElementById('copy_preauth_act_status').value;
	// copy details to add other rows
	var rowLen = getNumCharges("siTable");
	for (var id = 0; id < rowLen; id++) {
		var itemQty = 0;
		var apprvdQty = 0;
		var apprvdRemQty = 0;
		var row = getChargeRow(id, 'siTable');
		setHiddenValue(id, "s_item_preauth_id", preAuthId);
		setHiddenValue(id, "s_preauth_act_status", preauthActStatus);
		setHiddenValue(id, "s_item_preauth_mode", preAuthMode);
		var claimAmt = formatAmountPaise(getElementPaise(getIndexedFormElement(mainform, "s_claim_net_amount", id)));
		if (preauthActStatus == 'C'){
			itemQty = getIndexedFormElement(mainform, "s_item_qty", id).value;
			apprvdQty = itemQty;
			apprvdRemQty = itemQty;
		} else {
			apprvdQty = getIndexedFormElement(mainform, "s_item_intial_apprvd_qty", id).value;
			apprvdRemQty = getIndexedFormElement(mainform, "s_item_intial_approved_rem_qty", id).value;
		}
		setHiddenValue(id, "s_claim_approved_amount", claimAmt);
		setHiddenValue(id, "s_item_approved_qty", apprvdQty);
		setHiddenValue(id, "s_item_approved_rem_qty", apprvdRemQty);
		setActStatusFlag(preauthActStatus, row);
		setNodeText(row.cells[S_PREAUTH_ID], preAuthId);
		setNodeText(row.cells[S_PREAUTH_MODE], preAuthModeTxt);
		setNodeText(row.cells[ITEM_APPRD_QTY_COL], apprvdQty);
		setNodeText(row.cells[ITEM_APPRD_AMT_COL], claimAmt);
		setIndexedValue("s_edited", id, 'true');
	}
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

function showAttachment(obj){
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var preAuthActivityId = getIndexedValue('s_preauth_act_id', id);
	window.open(cpath+"/EAuthorization/EAuthPresc.do?_method=showActivityAttachment&preauth_act_id="+preAuthActivityId+"&cache=false");
}

function setDelAttachHiddenField(obj){
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var delAttach = getIndexedFormElement(mainform, "delAttach", id);
	if(delAttach.checked){
		setIndexedValue("s_del_attach", id, 'true');
	}else{
		setIndexedValue("s_del_attach", id, 'false');
	}
}

function enableDelCheck(obj){
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var delElement = getIndexedFormElement(mainform, "delAttach", id);
	delElement.disabled = false;
}
