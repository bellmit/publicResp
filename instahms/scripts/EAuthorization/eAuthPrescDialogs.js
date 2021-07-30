function onQtyChange(prefix) {
	var visitId = document.getElementById('patient_id').value;
	var itemId = document.getElementById(prefix+'_item_id').value;
	var act_qty = document.getElementById(prefix+'_claim_item_qty').value;
	var planId = document.getElementById('plan_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var insuranceCompanyId = document.getElementById('insurance_co_id').value;
	var visitType = document.getElementById('visit_type').value;

	if (prefix == 's_ed')
		dItemType = document.getElementById(prefix+"_itemType").value;
	else
		dItemType = getItemType();

	if (empty(itemId)) {
		alert("No item is selected");
		return false;
	}

	var chargeType = '';
	if (dItemType == 'DOC') {
		chargeType = document.getElementById(prefix+'_doc_cons_type').value;
		if (chargeType == "") {
			setSelectedIndex(document.getElementById(prefix+'_code_type'), "");
			initTrtCodesAutocomp(prefix+'_code', prefix+'_trtDropDown', "", prefix)
			document.getElementById(prefix+'_code').value = "";
			//alert('Doctor consultation type is required.');
			return false;
		}
	}
	

	var url = cpath+'/EAuthorization/EAuthPresc.do?_method=ajaxItemRateCodeCharge';
	url += '&patient_id='+visitId;
	url += '&itemType='+dItemType;
	url += '&itemId='+itemId;
	url += '&act_qty='+act_qty;
	url += '&chargeType='+chargeType;
	url += '&plan_id='+planId;
	url += '&tpa_id='+tpaId;
	url += '&insurance_co_id='+insuranceCompanyId;
	url += '&visit_type='+visitType;
	

	if (!empty(itemId) && !empty(act_qty)) {
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var itemRateDetails =" + ajaxobj.responseText);
					if (!empty(itemRateDetails)) {
						setItemRateDetails(prefix, itemRateDetails);
					}else
						alert("Claim amount calculation error.");
				}
			}
		}
	}
}

function setItemRateDetails(prefix, rateDet) {

	if (!empty(rateDet)) {
			setSelectedIndex(document.getElementById(prefix+'_code_type'), rateDet.codeType);
			initTrtCodesAutocomp(prefix+'_code', prefix+'_trtDropDown', rateDet.codeType, prefix)
			document.getElementById(prefix+'_code').value = rateDet.actRatePlanItemCode;
		// Set code details when item is different.
		if ( rateDet.actDescriptionId != document.getElementById(prefix+'_item_id').value
				|| (!empty(rateDet.consultation_type_id)
							&& rateDet.consultation_type_id != document.getElementById(prefix+'_doc_cons_type_old').value)) {
			setSelectedIndex(document.getElementById(prefix+'_code_type'), rateDet.codeType);
			initTrtCodesAutocomp(prefix+'_code', prefix+'_trtDropDown', rateDet.codeType, prefix)
			document.getElementById(prefix+'_code').value = rateDet.actRatePlanItemCode;
		}

		if (!empty(rateDet.consultation_type_id))
			document.getElementById(prefix+'_doc_cons_type_old').value = rateDet.consultation_type_id;

		document.getElementById(prefix+'_price_label').textContent = formatAmountValue(rateDet.actRate);
		document.getElementById(prefix+'_price').value = formatAmountValue(rateDet.actRate);
		document.getElementById(prefix+'_claim_item_qty').value = rateDet.actQuantity;
		document.getElementById(prefix+'_claim_item_disc').textContent = formatAmountValue(rateDet.discount);
		document.getElementById(prefix+'_claim_item_amount').textContent = formatAmountValue(rateDet.amount);
		document.getElementById(prefix+'_claim_item_pat_amount').textContent = formatAmountValue(rateDet.amount - rateDet.insuranceClaimAmount);
		document.getElementById(prefix+'_claim_item_net_amount').value = formatAmountValue(rateDet.insuranceClaimAmount);

		if (prefix == 's_d') {
			document.getElementById(prefix+'_claim_item_orig_rate').textContent = formatAmountValue(rateDet.actRate);
			document.getElementById(prefix+'_claim_item_orig_amount').textContent = formatAmountValue(rateDet.amount);
			document.getElementById(prefix+'_claim_item_orig_net_amount').textContent = formatAmountValue(rateDet.insuranceClaimAmount);
		}

		setSIEdited();

	}else {
		document.getElementById(prefix+'_code_type').textContent = '';
		document.getElementById(prefix+'_code').textContent = '';

		var zero = formatAmountValue(0);

		document.getElementById(prefix+'_price_label').textContent = zero;
		document.getElementById(prefix+'_price').value = zero;
		document.getElementById(prefix+'_claim_item_disc').textContent = zero;
		document.getElementById(prefix+'_claim_item_amount').textContent = zero;
		document.getElementById(prefix+'_claim_item_pat_amount').textContent = zero;
		document.getElementById(prefix+'_claim_item_net_amount').value = zero;

		if (prefix == 's_d') {
			document.getElementById(prefix+'_claim_item_orig_rate').textContent = zero;
			document.getElementById(prefix+'_claim_item_orig_amount').textContent = zero;
			document.getElementById(prefix+'_claim_item_orig_net_amount').textContent = zero;
		}

		setSIEdited();
	}
	resetClaimTotals();
}

function clearFields() {
	document.getElementById('s_d_itemName').value = '';
	document.getElementById('s_d_toothLabelTd').style.display = 'none';
	document.getElementById('s_d_toothValueTd').style.display = 'none';
	document.getElementById('s_d_docConsLabelTd').style.display = 'none';
	document.getElementById('s_d_docConsValueTd').style.display = 'none';

	var itemType = getItemType();
	if (itemType == 'SER') {
		document.getElementById('s_d_toothLabelTd').style.display = 'table-cell';
		document.getElementById('s_d_toothValueTd').style.display = 'table-cell';
	}else if (itemType == 'DOC') {
		document.getElementById('s_d_docConsLabelTd').style.display = 'table-cell';
		document.getElementById('s_d_docConsValueTd').style.display = 'table-cell';
	}
	initSIItemAutoComplete();
  	clearMasterSIFields('s_d');
}


function onItemChange() {
	clearFields();
	var itemType = getItemType();

	var isInsurancePatient =document.getElementById('tpa_id').value != '';
	if (isInsurancePatient) {
		document.getElementById('s_d_priorAuthLabelTd').style.display = 'block';
		document.getElementById('s_d_priorAuth_label').style.display = 'table-cell';
	} else {
		document.getElementById('s_d_priorAuthLabelTd').style.display = 'none';
		document.getElementById('s_d_priorAuth_label').style.display = 'none';
	}

	if (isInsurancePatient) {
		document.getElementById('s_d_markPriorAuthReqTd').style.display = 'block';
		document.getElementById('s_d_markPriorAuthCheckBox').style.display = 'table-cell';
	}else {
		document.getElementById('s_d_markPriorAuthReqTd').style.display = 'none';
		document.getElementById('s_d_markPriorAuthCheckBox').style.display = 'none';
	}
	resetObservationPanel('add');
}

var toothNumDialog = null;
function initToothNumberDialog() {
	var dialogDiv = document.getElementById("toothNumDialog");
	dialogDiv.style.display = 'block';
	toothNumDialog = new YAHOO.widget.Dialog("toothNumDialog",
			{	width:"600px",
				context : ["toothNumDialog", "tr", "tl"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('toothNumDialog_ok', 'click', updateToothNumbers, toothNumDialog, true);
	YAHOO.util.Event.addListener('toothNumDialog_close', 'click', cancelToothNumDialog, toothNumDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelToothNumDialog,
	                                                scope:toothNumDialog,
	                                                correctScope:true } );
	toothNumDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	toothNumDialog.render();
}

function showToothNumberDialog(action, obj) {
	var els = document.getElementsByName('d_chk_tooth_number');
	document.getElementById('dialog_type').value = action;
	var tnumbers = document.getElementById((action == 'add' ? 's_d' : 's_ed') + '_tooth_number').value.split(",");
	for (var i=0; i<els.length; i++) {
		var checked = false;
		for (var j=0; j<tnumbers.length; j++) {
			if (els[i].value == tnumbers[j]) {
				checked = true;
				break;
			}
		}
		els[i].checked = checked;
	}
	toothNumDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	toothNumDialog.show();
	childDialog = true;
}

function updateToothNumbers() {
	var els = document.getElementsByName('d_chk_tooth_number');
	var tooth_numbers = '';
	var tooth_numbers_text = '';
	var checked_toothNos = 0;
	for (var i=0; i<els.length; i++) {
		if (!els[i].checked) continue;

		if (tooth_numbers != '') {
			tooth_numbers += ',';
			tooth_numbers_text += ',';
		}
		if (checked_toothNos%10 == 0)
			tooth_numbers_text += '\n';

		checked_toothNos++;
		tooth_numbers += els[i].value;
		tooth_numbers_text += els[i].value;
	}
	var action = document.getElementById('dialog_type').value;
	document.getElementById(action == 'add' ? 's_d_tooth_number' : 's_ed_tooth_number').value = tooth_numbers;
	document.getElementById(action == 'add' ? 's_d_ToothNumberDiv' : 's_ed_ToothNumberDiv').textContent = tooth_numbers_text;
	if (action != 'add')
		fieldEdited = true;
	childDialog = null;
	this.cancel();
}

function cancelToothNumDialog() {
	childDialog = null;
	toothNumDialog.cancel();
}

var siEditItemAutoComp = null;
function initSIEditItemAutoComplete() {
	if (!empty(siEditItemAutoComp)) {
		siEditItemAutoComp.destroy();
		siEditItemAutoComp = null;
	}
	var itemType = document.getElementById('s_ed_itemType').value;
	if (itemType == 'DIA')
		itemType = 'Inv.';
	else if (itemType == 'SER')
		itemType = 'Service';
	else if (itemType == 'OPE')
		itemType = 'Operation';
	else if (itemType == 'DOC')
		itemType = 'Doctor';

	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeActionAjax.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType
				+ "&org_id=" + orgId + "&center_id=" + centerId + "&tpa_id=" + tpaId + "&forceUnUseOfGenerics=true";
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : 'prior_auth_required'},
					{key : 'tooth_num_required'}
				 ],
		numMatchFields: 2
	};

	siEditItemAutoComp = new YAHOO.widget.AutoComplete("s_ed_itemName", "s_ed_itemContainer", ds);
	siEditItemAutoComp.minQueryLength = 1;
	siEditItemAutoComp.animVert = false;
	siEditItemAutoComp.maxResultsDisplayed = 50;
	siEditItemAutoComp.resultTypeList = false;
	siEditItemAutoComp.forceSelection = true;
	// the following is to prevent clearing of the autocomp on blur
	siEditItemAutoComp._bItemSelected = true;

	siEditItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	siEditItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		return highlightedValue;
	}

	siEditItemAutoComp.dataRequestEvent.subscribe(clearSIEditMasterType);
	siEditItemAutoComp.itemSelectEvent.subscribe(selectSIEditItem);
	siEditItemAutoComp.selectionEnforceEvent.subscribe(clearSIEditMasterType);

	return siEditItemAutoComp;
}

function clearSIEditMasterType(oSelf) {
	//clearMasterSIEditFields('s_ed');
}

function clearMasterSIEditFields(prefix) {
	document.getElementById(prefix+'_item_id').value = '';

	document.getElementById(prefix+'_ispackage').value = '';
	document.getElementById(prefix+'_item_master').value = '';

	setSelectedIndex(document.getElementById(prefix+'_code_type'), '');
	document.getElementById(prefix+'_code').value = '';

	setSelectedIndex(document.getElementById(prefix+'_preauth_mode'), '');
	document.getElementById(prefix+'_preauth_id').value = '';

	document.getElementById(prefix+'_price_label').textContent = '';
	document.getElementById(prefix+'_price').value = '';
	document.getElementById(prefix+'_claim_item_qty').value = '';
	document.getElementById(prefix+'_claim_item_disc').textContent = '';
	document.getElementById(prefix+'_claim_item_amount').textContent = '';
	document.getElementById(prefix+'_claim_item_pat_amount').textContent = '';
	document.getElementById(prefix+'_claim_item_net_amount').value = '';

	document.getElementById(prefix+'_claim_item_orig_rate').textContent = '';
	document.getElementById(prefix+'_claim_item_orig_amount').textContent = '';
	document.getElementById(prefix+'_claim_item_orig_net_amount').textContent = '';

	document.getElementById(prefix+'_priorAuth_label').textContent = '';
	document.getElementById(prefix+'_priorAuth').value = '';

	setSelectedIndex(document.getElementById(prefix+'_doc_cons_type'), '');

	document.getElementById(prefix+'_tooth_num_required').value = 'N';
	document.getElementById(prefix+'_ToothNumberDiv').textContent = '';
	document.getElementById(prefix+'_tooth_number').value = '';
	document.getElementById(prefix+'_ToothNumBtnDiv').style.display = 'none';
	document.getElementById(prefix+'_ToothNumDsblBtnDiv').style.display = 'block';
}

function setSelectedTextIndex(opt, set_value) {
	var index=0;
	if (opt.options) {
		for(var i=0; i<opt.options.length; i++) {
			var opt_value = opt.options[i].text;
			if (opt_value == set_value) {
				opt.selectedIndex = i;
				return;
			}
		}
	}
}

function selectSIEditItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('s_ed_item_id').value = record.item_id;
	document.getElementById('s_ed_ispackage').value = record.ispkg;
	document.getElementById('s_ed_item_master').value = record.master;
	if (document.getElementById('s_ed_claim_item_qty').value == '')
		document.getElementById('s_d_claim_item_qty').value = 1;
	if (document.getElementById('s_ed_claim_approved_qty').value == '')
		document.getElementById('s_d_claim_approved_qty').value = 0;

	onQtyChange('s_ed');
}

function getItemType() {
	var itemTypeObj = document.getElementsByName('s_d_itemType');
	for (var i=0; i<itemTypeObj.length; i++) {
		if (itemTypeObj[i].checked)
			return itemTypeObj[i].value;
	}
	return null;
}


var siItemAutoComp = null;
function initSIItemAutoComplete() {
	if (!empty(siItemAutoComp)) {
		siItemAutoComp.destroy();
		siItemAutoComp = null;
	}
	var itemType = getItemType();
	if (itemType == 'DIA')
		itemType = 'Inv.';
	else if (itemType == 'SER')
		itemType = 'Service';
	else if (itemType == 'OPE')
		itemType = 'Operation';
	else if (itemType == 'DOC')
		itemType = 'Doctor';
	else if (itemType == 'ITE')
		itemType = 'Inventory';

	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeActionAjax.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType
				+ "&org_id=" + orgId + "&center_id=" + centerId + "&tpa_id=" + tpaId + "&forceUnUseOfGenerics=true";
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : 'prior_auth_required'},
					{key : 'tooth_num_required'}
				 ],
		numMatchFields: 2
	};

	siItemAutoComp = new YAHOO.widget.AutoComplete("s_d_itemName", "s_d_itemContainer", ds);
	siItemAutoComp.minQueryLength = 1;
	siItemAutoComp.animVert = false;
	siItemAutoComp.maxResultsDisplayed = 50;
	siItemAutoComp.resultTypeList = false;
	siItemAutoComp.forceSelection = true;
	// the following is to prevent clearing of the autocomp on blur
	siItemAutoComp._bItemSelected = true;

	siItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	siItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		return highlightedValue;
	}

	siItemAutoComp.dataRequestEvent.subscribe(clearSIMasterType);
	siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
	siItemAutoComp.selectionEnforceEvent.subscribe(clearSIMasterType);

	return siItemAutoComp;
}

function clearSIMasterType(oSelf) {
	clearMasterSIFields('s_d');
}

function clearMasterSIFields(prefix) {

	document.getElementById(prefix+'_item_id').value = '';

	document.getElementById(prefix+'_ispackage').value = '';
	document.getElementById(prefix+'_item_master').value = '';

	document.getElementById(prefix+'_claim_item_qty').value = '';

	setSelectedIndex(document.getElementById(prefix+'_code_type'), '');
	document.getElementById(prefix+'_code').value = '';

	setSelectedIndex(document.getElementById(prefix+'_preauth_mode'), '');
	document.getElementById(prefix+'_preauth_id').value = '';

	setSelectedIndex(document.getElementById(prefix+'_doc_cons_type'), '');

	document.getElementById(prefix+'_price_label').textContent = '';
	document.getElementById(prefix+'_price').value = '';
	document.getElementById(prefix+'_claim_item_qty').value = '';
	document.getElementById(prefix+'_claim_item_disc').textContent = '';
	document.getElementById(prefix+'_claim_item_amount').textContent = '';
	document.getElementById(prefix+'_claim_item_pat_amount').textContent = '';
	document.getElementById(prefix+'_claim_item_net_amount').value = '';

	document.getElementById(prefix+'_claim_item_orig_rate').textContent = '';
	document.getElementById(prefix+'_claim_item_orig_amount').textContent = '';
	document.getElementById(prefix+'_claim_item_orig_net_amount').textContent = '';

	setSelectedIndex(document.getElementById(prefix+'_preauth_act_status'), 'O');
	document.getElementById(prefix+'_item_remarks').value = '';


	var markPriorAuthReqObj = document.getElementById(prefix+'_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		markPriorAuthReqObj.checked = false;
	}

	var curDate = (gServerNow != null) ? gServerNow : new Date();
	document.getElementById(prefix+'_prescribed_date').value = formatDate(curDate, "ddmmyyyy", "-");

	document.getElementById(prefix+'_priorAuth').value = '';
	document.getElementById(prefix+'_tooth_num_required').value = '';
	document.getElementById(prefix+'_tooth_number').value = '';

	document.getElementById(prefix+'_tooth_num_required').value = 'N';
	document.getElementById(prefix+'_ToothNumberDiv').textContent = '';
	document.getElementById(prefix+'_tooth_number').value = '';
	document.getElementById(prefix+'_ToothNumBtnDiv').style.display = 'none';
	document.getElementById(prefix+'_ToothNumDsblBtnDiv').style.display = 'none';
}

function selectSIItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('s_d_item_id').value = record.item_id;
	document.getElementById('s_d_ispackage').value = record.ispkg;
	document.getElementById('s_d_item_master').value = record.master;

	var prior_auth = record.prior_auth_required;
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}
	document.getElementById('s_d_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('s_d_priorAuth').value = prior_auth;

	var markPriorAuthReqObj = document.getElementById('s_d_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		if (TPArequiresPreAuth && TPArequiresPreAuth === 'Y') {
			markPriorAuthReqObj.checked = true;
		}else {
			markPriorAuthReqObj.checked = false;
		}
	}

	document.getElementById('s_d_tooth_num_required').value = record.tooth_num_required;

/*	if (record.tooth_num_required == 'Y') {
	//	document.getElementById('s_d_tooth_number').disabled = false;
		document.getElementById('s_d_ToothNumBtnDiv').style.display = 'none';
		document.getElementById('s_d_ToothNumDsblBtnDiv').style.display = 'none';
	} else {
		document.getElementById('s_d_tooth_number').disabled = true;
		document.getElementById('s_d_ToothNumBtnDiv').style.display = 'none';
		document.getElementById('s_d_ToothNumDsblBtnDiv').style.display = 'none';
	}*/

	document.getElementById('s_d_claim_item_qty').value = 1;
	document.getElementById('s_d_claim_approved_qty').disabled = true;
	document.getElementById('s_d_claim_approved_qty').value = 0;
	onQtyChange('s_d');
}

function clearSITable() {
	var table = document.getElementById('siTable');
	var numRows = getNumCharges("siTable");
	for (var index = numRows; index >0; index--) {
		var row = table.rows[index];
		var newEl = getElementByName(row, 's_preauth_act_id');
		if (newEl.value == '_') {
			row.parentNode.removeChild(row);
		}
	}
}


/* duplicate check is being done based
 1) on the item id if item type is
 		one of Medicine(Pharamcy), Test, Service, Doctor.
 2) on the item name if item type is
 		one of Medicine(non pharmacy), Non Hospital Items.
*/
function checkForSIDuplicates(prefix, id) {
	var itemTypes = document.getElementsByName("s_itemType");
	var dItemType = "";
	if (prefix == 's_ed')
		dItemType = document.getElementById(prefix+"_itemType").value;
	else
		dItemType = getItemType();
	var dItemName = document.getElementById(prefix+"_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	var issuedItems = document.getElementsByName("s_issued");
	var delItem = document.getElementsByName("s_delItem");

	for (var i=0; i<itemTypes.length-1; i++) {
		if (dItemType == itemTypes[i].value) {
			if (prefix == 's_ed' && i == id)
				continue;
			if (itemNames[i].value.trim().toLowerCase() == dItemName.trim().toLowerCase()) {
				var markedForDelete = delItem[i].value;
				var issued = issuedItems[i].value;
				if (issued == 'N' && markedForDelete == 'false') return true;
			}
		}
	}
	return false;
}


function setSIRowStyle(i) {
	var row = getChargeRow(i, 'siTable');
	var prescribedId = getIndexedValue("s_preauth_act_id", i);
	var trashImgs = row.cells[S_TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("s_delItem", i) == 'true';
	var edited = getIndexedValue("s_edited", i) == 'true';

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

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelSIItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var oldDeleted =  getIndexedValue("s_delItem", id);
	var isNew = getIndexedValue("s_preauth_act_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		var newDeleted;
		var delStatus;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
			delStatus = 'A';
		} else {
			newDeleted = 'true';
			delStatus = 'X';
		}
		itemsEdited++;
		setIndexedValue("s_delItem", id, newDeleted);
		setIndexedValue("s_edited", id, "true");
		setIndexedValue("s_status", id, delStatus);
		setSIRowStyle(id);
	}

	resetClaimTotals();
	renameFileUploadEl();
	return false;
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(mainform, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function initEditSIDialog() {
	var dialogDiv = document.getElementById("editSIDialog");
	dialogDiv.style.display = 'block';
	editSIDialog = new YAHOO.widget.Dialog("editSIDialog",{
			width:"680px",
			text: "Edit Item",
			context :["itemsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditSICancel,
	                                                scope:editSIDialog,
	                                                correctScope:true } );
	editSIDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editSIDialog.cancelEvent.subscribe(handleEditSICancel);
	YAHOO.util.Event.addListener('siOk', 'click', editSITableRow, editSIDialog, true);
	YAHOO.util.Event.addListener('siEditCancel', 'click', handleEditSICancel, editSIDialog, true);
	YAHOO.util.Event.addListener('siEditPrevious', 'click', openSIPrevious, editSIDialog, true);
	YAHOO.util.Event.addListener('siEditNext', 'click', openSINext, editSIDialog, true);
	editSIDialog.render();
}

function handleEditSICancel() {
	if (childDialog == null) {
		var id = document.getElementById('s_ed_editRowId').value ;
		var row = getChargeRow(id, "siTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		parentSIDialog = null;
		siFieldEdited = false;
		clearMasterSIFields('s_ed');
		this.hide();
	}
}

function openSINext() {
	var id = document.getElementById('s_ed_editRowId').value ;
	id = parseInt(id);
	var row = getChargeRow(id, 'siTable');
	if (siFieldEdited) {
		siFieldEdited = false;
		if (!editSITableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id+1 != document.getElementById('siTable').rows.length-2) {
		showEditSIDialog(document.getElementsByName('si_editAnchor')[parseInt(id)+1]);
	}
}

function openSIPrevious(id, previous, next) {
	var id = document.getElementById('s_ed_editRowId').value ;
	id = parseInt(id);
	var row = getChargeRow(id, 'siTable');
	if (siFieldEdited) {
		siFieldEdited = false;
		if (!editSITableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditSIDialog(document.getElementsByName('si_editAnchor')[parseInt(id)-1]);
	}
}

var siFieldEdited = false;
function setSIEdited() {
	siFieldEdited = true;
}

var parentSIDialog = null;
var childDialog = null;
function showAddSIDialog(obj) {
	var row = getThisRow(obj);

	resetObservationPanel('add');
	addSIDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient) {
		document.getElementById('s_d_priorAuthLabelTd').style.display = 'block';
		document.getElementById('s_d_priorAuth_label').style.display = 'table-cell';
	} else {
		document.getElementById('s_d_priorAuthLabelTd').style.display = 'none';
		document.getElementById('s_d_priorAuth_label').style.display = 'none';
	}

	if (isInsurancePatient) {
		document.getElementById('s_d_markPriorAuthReqTd').style.display = 'block';
		document.getElementById('s_d_markPriorAuthCheckBox').style.display = 'table-cell';
	}else {
		document.getElementById('s_d_markPriorAuthReqTd').style.display = 'none';
		document.getElementById('s_d_markPriorAuthCheckBox').style.display = 'none';
	}

	var markPriorAuthReqObj = document.getElementById('s_d_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		markPriorAuthReqObj.checked = true;
	}

	addSIDialog.show();
	document.getElementById('s_d_itemName').focus();
	parentSIDialog = addSIDialog;
	initTrtCodesAutocomp('s_d_code', 's_d_trtDropDown', '', 's_d');
	return false;
}

function initSIDialog() {
	var dialogDiv = document.getElementById("addSIDialog");
	dialogDiv.style.display = 'block';
	addSIDialog = new YAHOO.widget.Dialog("addSIDialog",
			{	width:"680px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('SIAdd', 'click', addSIToTable, addSIDialog, true);
	YAHOO.util.Event.addListener('SIClose', 'click', handleAddSICancel, addSIDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddSICancel,
	                                                scope:addSIDialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("addSIDialogFieldsDiv", { keys:13 },
				{ fn:onEnterKeySIItemDialog, scope:addSIDialog, correctScope:true } );
	addSIDialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	addSIDialog.render();
}

function onEnterKeySIItemDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new item from autocomplete.)
	document.getElementById("s_d_itemName").blur();
	addSIToTable();
}

function handleAddSICancel() {
	if (childDialog == null) {
		parentSIDialog = null;
		clearMasterSIFields('s_d');
		this.cancel();
	}
}

function resetObservationPanel(obsValTable) {

	var obsPreFix = obsValTable == 'add' ? 'addobs' : 'obs';

   var obsTable = document.getElementById(obsPreFix+'ValueTable');
   for (var i = obsTable.rows.length; i > 2; i--) {
        obsTable.deleteRow(-1);
    }
    document.getElementById(obsPreFix+'ValueTable').style.display = "none";
    document.getElementById(obsPreFix+'ActItemId').value = "";
    document.getElementById(obsPreFix+'Index').value = "1";
    document.getElementById(obsPreFix+'CodeType.1').value = "";
    document.getElementById(obsPreFix+'Code.1').value = "";
    document.getElementById(obsPreFix+'MasterCodeDesc.1').value = "";
    document.getElementById(obsPreFix+'Value.1').value = "";
    document.getElementById(obsPreFix+'ValueType.1').value = "";
}


var diagAutoCompleteArray = new Array();
var diagAcArrayIndex = 0;
function initializeObservationDialogFields(actItemId){
  	var obsPreFix = 'obs';
  	var hidObsPrefix = 'obser';

	var obsParentDiv = document.getElementById(obsPreFix+'ervations.'+actItemId);
	var noOfObs = 0;
	if (document.getElementById(hidObsPrefix+'Index.'+actItemId) != null)
		noOfObs = document.getElementById(hidObsPrefix+'Index.'+actItemId).value;
   if (noOfObs > 0) document.getElementById(obsPreFix+'ValueTable').style.display = "block";
   var dlg_actItemId = document.getElementById(obsPreFix+'ActItemId');
   dlg_actItemId.value = actItemId;
	diagAutoCompleteArray = new Array();
	diagAcArrayIndex = 0;

    for (var i = 1; i <= noOfObs; i++) {
        var observationType = document.getElementById(hidObsPrefix+'Type.' + actItemId + i).value;
        var observationCode = document.getElementById(hidObsPrefix+'Code.' + actItemId + i).value;
        var observationValue = document.getElementById(hidObsPrefix+'Value.' + actItemId + i).value;
        var observationValueType = document.getElementById(hidObsPrefix+'ValueType.' + actItemId + i).value;
        var observationCodeDesc = document.getElementById(hidObsPrefix+'CodeDesc.' + actItemId + i).value;

        addObservationElements(obsPreFix);

        var dlg_obsIndex = document.getElementById(obsPreFix+'Index').value - 1;

        var dlg_obsCode = document.getElementById(obsPreFix+'Code.' + dlg_obsIndex);
        var dlg_obsCodeType = document.getElementById(obsPreFix+'CodeType.' + dlg_obsIndex);
        var dlg_obsValue = document.getElementById(obsPreFix+'Value.' + dlg_obsIndex);
        var dlg_obsValueType = document.getElementById(obsPreFix+'ValueType.' + dlg_obsIndex);
        var dlg_codeDesc = document.getElementById(obsPreFix+'MasterCodeDesc.' + dlg_obsIndex);

		dlg_obsCode.value = observationCode;
		dlg_obsCodeType.value = observationType;
		dlg_obsValue.value = observationValue;
		dlg_obsValueType.value = observationValueType;
		dlg_codeDesc.value = observationCodeDesc;

		document.getElementById(obsPreFix+'Auto.'+dlg_obsIndex).setAttribute('style','padding-bottom: 20px;');
		var obsAuto = initCodesAutocomplete(obsPreFix+'Code.'+dlg_obsIndex, obsPreFix+'DropDown.'+dlg_obsIndex, observationType);
		obsAuto.itemSelectEvent.subscribe(function(obsPreFix, dlg_obsIndex) {
				return function(sType, aArgs) {
					setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+dlg_obsIndex), aArgs[2].code_desc, 8);
					document.getElementById(obsPreFix+'MasterCodeDesc.'+dlg_obsIndex).value = aArgs[2].code_desc;
				}
			}(obsPreFix, dlg_obsIndex)
		);
		var elNewItem = matches(observationCode, obsAuto);
		obsAuto._selectItem(elNewItem);
		diagAutoCompleteArray[diagAcArrayIndex] =  new Array(2);
		diagAutoCompleteArray[diagAcArrayIndex][0] = new Array ({prefix: obsPreFix, index: dlg_obsIndex});
		diagAutoCompleteArray[diagAcArrayIndex][1] = obsAuto;
		diagAcArrayIndex++;

		dlg_codeDesc.value = observationCodeDesc;
		setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+dlg_obsIndex), observationCodeDesc, 8);
	}
}

function matches(mName, autocomplete) {
    var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ? (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
    elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
    return elListItem;
}

function addObservationsToGrid(obsTable, obsItemId, row) {

	var obsPreFix = obsTable == 'add' ? 'addobs' : 'obs';
	var hidObsPrefix = 'obser';
	var dlg_actItemId = obsItemId;

	var tdNode = row.cells[S_PRESC_DATE];
	var divNodes = tdNode.getElementsByTagName("div");
	if (divNodes) {
		for (var k=divNodes.length-1; k>=0; k--) {
			tdNode.removeChild(divNodes[k]);
		}
	}

	var obsParentDiv = document.createElement('div');
   obsParentDiv.id = 'observations.' + dlg_actItemId;
   obsParentDiv.setAttribute("style", "display: inline");
   tdNode.appendChild(obsParentDiv);

   var obsIndx = document.createElement('input');
   obsIndx.type = "hidden";
   obsIndx.id = hidObsPrefix+'Index.' + dlg_actItemId;

   obsIndx.value = document.getElementById(obsPreFix+'Index').value - 1;
   obsValueTable = document.getElementById(obsPreFix+'ValueTable');

   obsParentDiv.appendChild(obsIndx);

   var validObsCount = 0;
    for (var ii = 1; ii < obsValueTable.rows.length; ii++) {
        i = (obsValueTable.rows[ii].id).split(".")[1];
        var dlg_obsCode = document.getElementById(obsPreFix+'Code.' + i);
        var dlg_obsCodeType = document.getElementById(obsPreFix+'CodeType.' + i);
        var dlg_obsValue = document.getElementById(obsPreFix+'Value.' + i);
        var dlg_obsValueType = document.getElementById(obsPreFix+'ValueType.' + i);
        var dlg_codeDesc = document.getElementById(obsPreFix+'MasterCodeDesc.' + i);

        if (dlg_obsCode.value != '' && dlg_obsCode.value != null && dlg_obsCodeType.value != '' && dlg_obsCodeType.value != null) {
            validObsCount++;
            var obserCode = document.createElement('input');
            obserCode.type = "hidden";
            obserCode.name = hidObsPrefix+'Code.' + dlg_actItemId;
            obserCode.id = hidObsPrefix+'Code.' + dlg_actItemId + validObsCount;
            obserCode.value = dlg_obsCode.value;
            obsParentDiv.appendChild(obserCode);

            var obserType = document.createElement('input');
            obserType.type = "hidden";
            obserType.name = hidObsPrefix+'Type.' + dlg_actItemId;
            obserType.id = hidObsPrefix+'Type.' + dlg_actItemId + validObsCount;
            obserType.value = dlg_obsCodeType.value;
            obsParentDiv.appendChild(obserType);

            var obserValue = document.createElement('input');
            obserValue.type = "hidden";
            obserValue.name = hidObsPrefix+'Value.' + dlg_actItemId;
            obserValue.id = hidObsPrefix+'Value.' + dlg_actItemId + validObsCount;
            obserValue.value = dlg_obsValue.value;
            obsParentDiv.appendChild(obserValue);

            var obserValueType = document.createElement('input');
            obserValueType.type = "hidden";
            obserValueType.name = hidObsPrefix+'ValueType.' + dlg_actItemId;
            obserValueType.id = hidObsPrefix+'ValueType.' + dlg_actItemId + validObsCount;
            obserValueType.value = dlg_obsValueType.value;
            obsParentDiv.appendChild(obserValueType);

            var obserCodeDesc = document.createElement('input');
            obserCodeDesc.type = "hidden";
            obserCodeDesc.name = hidObsPrefix+'CodeDesc.' + dlg_actItemId;
            obserCodeDesc.id = hidObsPrefix+'CodeDesc.' + dlg_actItemId + validObsCount;
            obserCodeDesc.value = dlg_codeDesc.value;
            obsParentDiv.appendChild(obserCodeDesc);
       }
    }
    obsIndx.value = validObsCount;
}

function addObservationElements(obsPreFix) {
	var obsTable = document.getElementById(obsPreFix+'ValueTable');
	var obserIndex = document.getElementById(obsPreFix+'Index');
	var obserIndexValue = obserIndex.value;
    if (obserIndexValue == null || obserIndexValue == '') obserIndexValue = 1;
    if (obserIndexValue == 1) {
        // make the hidden row visible
        obsTable.style.display = 'block';
    } else {
        // add observation elements
        appendObservationElementsToTable(obserIndexValue, obsPreFix);
    }
    obserIndex.value = ++obserIndexValue;
}

function appendObservationElementsToTable(nextRowIndex, obsPreFix) {
    var obsTable = document.getElementById(obsPreFix+'ValueTable');
    var tr1 = obsTable.insertRow(-1);
    tr1.name = obsPreFix+'.' + nextRowIndex;
    tr1.id = obsPreFix+'.' + nextRowIndex;

    var td1 = tr1.insertCell(-1);

    var sel1 = document.createElement('select');
    sel1.name = obsPreFix+'CodeType.' + nextRowIndex;
    sel1.id = obsPreFix+'CodeType.' + nextRowIndex;
    sel1.setAttribute("onchange", "initObsCodesAutocomp('" + obsPreFix + "'," + nextRowIndex + ");");
    sel1.setAttribute("class", "dropDown");
    td1.appendChild(sel1);
    var opt1 = document.createElement('option');
    sel1.appendChild(opt1);
    loadSelectBox(sel1, observationCodeTypesList, 'CODE_TYPE', 'CODE_TYPE', "--Select--", "");

    var td2 = tr1.insertCell(-1);

    var div1 = document.createElement('div');
    div1.id = obsPreFix+'Auto.' + nextRowIndex;
    div1.setAttribute("style", "width:70px;");
    td2.appendChild(div1);

    var txtBx1 = document.createElement("input");
    txtBx1.type = "text";
    txtBx1.name = obsPreFix+'Code.' + nextRowIndex;
    txtBx1.id = obsPreFix+'Code.' + nextRowIndex;
    txtBx1.setAttribute("style", "width:70px;");
    div1.appendChild(txtBx1);

    var childDiv1 = document.createElement('div');
    childDiv1.id = obsPreFix+'DropDown.' + nextRowIndex;
    childDiv1.setAttribute("class", "scrolForContainer");
    childDiv1.setAttribute("style", "width:300px;");
    div1.appendChild(childDiv1);

    var td3 = tr1.insertCell(-1);
    td3.setAttribute("style", "text-align:right;");
    var label1 = document.createElement('label');
    label1.id = obsPreFix+'CodeDesc.' + nextRowIndex;
    label1.setAttribute('class',obsPreFix+"RowCount") ;
    td3.appendChild(label1);

    var hiddenchild = document.createElement("input");
    hiddenchild.type = "hidden";
    hiddenchild.id = obsPreFix+'MasterCodeDesc.' + nextRowIndex;
    hiddenchild.name = obsPreFix+'MasterCodeDesc.' + nextRowIndex;
    td3.appendChild(hiddenchild);

    var helpImg = document.createElement('img');
    helpImg.id = obsPreFix+'HelpImg.' + nextRowIndex;
    helpImg.name = obsPreFix+'HelpImg.' + nextRowIndex;
    helpImg.setAttribute("src", cpath + "/images/help.png");
    helpImg.setAttribute("onclick", "showCodeDescription('" + obsPreFix + "'," + nextRowIndex + ")");
    td3.appendChild(helpImg);

    var td4 = tr1.insertCell(-1);
    var txtBx2 = document.createElement("input");
    txtBx2.id = obsPreFix+'Value.' + nextRowIndex;
    txtBx2.name = obsPreFix+'Value.' + nextRowIndex;
    txtBx2.type = "text";
    td4.appendChild(txtBx2);

    var td5 = tr1.insertCell(-1);
    var txtBx3 = document.createElement("input");
    txtBx3.id = obsPreFix+'ValueType.' + nextRowIndex;
    txtBx3.type = "text";
    txtBx3.name = obsPreFix+'ValueType.' + nextRowIndex;
    td5.appendChild(txtBx3);

    var td6 = tr1.insertCell(-1);
    td6.name = obsPreFix+'Del.' + nextRowIndex;
    td6.id = obsPreFix+'Del.' + nextRowIndex;
    td6.width = "17px";
    td6.setAttribute("align", "right");
    td6.setAttribute("style", "padding-left: 5px; padding-right: 5px; height: 18px; width: 17px;");
}

function showCodeDescription(obsPreFix, index) {
    var imgEl = document.getElementById(obsPreFix+'HelpImg.' + index);
    var codeDesc = document.getElementById(obsPreFix+'MasterCodeDesc.' + index).value;
    var info = codeDesc == null || codeDesc == 'undefined' ? '' : codeDesc;
    showInfoDialogs(imgEl, info);
}

function showInfoDialogs(contextEl, text) {
    infoDialogs.cfg.setProperty("context", [contextEl, "tl", "tl", ["beforeShow", "windowResize"]], false);
    infoDialogs.cfg.setProperty("zIndex", "999");
    infoDialogs.render();
    document.getElementById('infoDialogs').visibility = 'visible';
    document.getElementById('infoDialogs').style.display = 'block';

    document.getElementById('infoDialogsText').textContent = text;
    document.getElementById('infoDialogs').style.zIndex = "999";
    infoDialogs.show();
}

var infoDialogs;

function initInfoDialogs() {
    infoDialogs = new YAHOO.widget.Dialog("infoDialogs", {
        context: ['', 'tr', 'bl', ["beforeShow", "windowResize"]],
        visible: false,
        modal: true,
        iframe: true,
        width: "300px"
    });
    YAHOO.util.Event.addListener('cancelInfoImg', "click", infoDialogs.hide, infoDialogs, true);
}

var obsAutocomp = null;

function initObsCodesAutocomp(obsPreFix, curIndex) {
	for(var i=0; i< diagAutoCompleteArray.length; i++){
		if(diagAutoCompleteArray[i][0].prefix == obsPreFix && diagAutoCompleteArray[i][0].index == curIndex
					&& diagAutoCompleteArray[i][1]!= null) {
			var autoCmp = diagAutoCompleteArray[i][1];
			autoCmp.destroy();
			diagAutoCompleteArray[i][1] = null;
		}
	}

	if(obsAutocomp!= null){
		obsAutocomp.destroy();
	}
	document.getElementById(obsPreFix+'Code.'+curIndex).value ="";
	document.getElementById(obsPreFix+'MasterCodeDesc.'+curIndex).value ="";
	setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+curIndex), "", 8);
	var codeType = document.getElementById(obsPreFix+'CodeType.'+curIndex).value;
	document.getElementById(obsPreFix+'Auto.'+curIndex).setAttribute('style','padding-bottom: 20px;');
	obsAutocomp = initCodesAutocomplete(obsPreFix+'Code.'+curIndex, obsPreFix+'DropDown.'+curIndex, codeType);
	obsAutocomp.itemSelectEvent.subscribe(function (sType, aArgs) {
		setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+curIndex), aArgs[2].code_desc, 8);
		document.getElementById(obsPreFix+'MasterCodeDesc.'+curIndex).value = aArgs[2].code_desc;
		document.getElementById(obsPreFix+'Code.'+curIndex).value = aArgs[2].code;
	});
}


var trtCodeAutoComp = null;
function initTrtCodesAutocomp(feild, dropDown, value, prefix) {
    if (trtCodeAutoComp != null) trtCodeAutoComp.destroy();
    document.getElementById(prefix+'_code').value = '';
    document.getElementById(prefix+'_code_desc').textContent = '';
    trtCodeAutoComp = initCodesAutocomplete(feild, dropDown, value);
    trtCodeAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById(prefix+'_code_desc'), aArgs[2].code_desc, 30, aArgs[2].code_desc);
    })
}

function initEncCodeAutoComplete() {
    encAuto1 = initCodesAutocomplete('encCode', 'encDropDown', 'Encounter Type');
    encAuto1.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('encTypeCodeDesc'), aArgs[2].code_desc, 50);
        document.mainform.encTypeCodeDesc.value = aArgs[2].code_desc;
    })
}

function initDrgCodeAutoComplete() {
	drgAutoComp = initCodesAutocomplete('drgCode', 'drgDropDown', 'IR-DRG');
   drgAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('drgCodeDesc'), aArgs[2].code_desc, 50);
        document.mainform.drgCodeDesc.value = aArgs[2].code_desc;

		  document.mainform.drg_code.value = aArgs[2].code;
        setNodeText(document.getElementById('drgCodeType'), 'IR-DRG');
        document.mainform.drg_code_type.value = 'IR-DRG';
   })
}

function initPerdiemCodeAutoComplete() {
	perdiemAutoComp = initCodesAutocomplete('perdiemCode', 'perdiemDropDown', 'Service Code');
   perdiemAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('perdiemCodeDesc'), aArgs[2].code_desc, 50);
        document.mainform.perdiemCodeDesc.value = aArgs[2].code_desc;

		  document.mainform.perdiem_code.value = aArgs[2].code;
        setNodeText(document.getElementById('perdiemCodeType'), 'Service Code');
        document.mainform.perdiem_code_type.value = 'Service Code';
   })
}

function initCodesAutocomplete(field, dropdown, type) {
    var dataSource = new YAHOO.util.XHRDataSource(cpath + "/pages/medicalrecorddepartment/MRDUpdate.do");
    var queryParams = "_method=getCodesListOfCodeType&codeType=" + encodeURIComponent(type);
    queryParams += (type != 'IR-DRG') ? "" : '&patientType='+patientType;
    dataSource.scriptQueryAppend = queryParams;
    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    dataSource.responseSchema = {
        resultsList: "result",
        fields: [{
            key: "code"
        }, {
            key: "icd"
        }, {
            key: "code_desc"
        }]
    };
    var oAutoComp = new YAHOO.widget.AutoComplete(field, dropdown, dataSource);
    oAutoComp.minQueryLength = 1;
    oAutoComp.forceSelection = true;
    oAutoComp._bItemSelected = true;
    oAutoComp.allowBrowserAutocomplete = false;
    oAutoComp.resultTypeList = false;
    oAutoComp.maxResultsDisplayed = 50;
    var reArray = [];
    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
        var escapedComp = Insta.escape(sQuery);
        reArray[0] = new RegExp('^' + escapedComp, 'i');
        reArray[1] = new RegExp("\\s" + escapedComp, 'i');
        var det = highlight(oResultData.code + ' / ' + oResultData.code_desc, reArray);
        return det;
    };
    oAutoComp.setHeader(' Code / Description ');
    return oAutoComp;
}

function isInArray(objArr, val) {
	for (var k=0; k<objArr.length; k++) {
   	if (objArr[k][1].value == val)
   		return true;
   }
	return false;
}

function removeDupsAndSortDropDown(obj) {
	var objArr = new Array();
	if (!empty(obj)) {
		objArr = new Array();
		var objValue = obj.value;
		var i = 0;
    	for (var n=0; n<obj.options.length; n++) {
			if (!isInArray(objArr, obj.options[n].value)) {
  				objArr[i] = new Array(obj.options[n].text,
  								{text: obj.options[n].text, value: obj.options[n].value});
  				i++;
  			}
    	}
    	objArr.sort();

		var len = 0;
		if (objArr.length > 0) {
	    	for (var n=0; n<objArr.length; n++) {
	    		var optn = new Option(objArr[n][1].text, objArr[n][1].value);
				len++;
				obj.options.length = len;
				obj.options[len - 1] = optn;
	    	}
		}
    	setSelectedIndex(obj, objValue);
    }
}

function setDefaultApprovedAmount() {
    var id = document.getElementById('s_ed_editRowId').value ;
    var preauthActStatus = document.getElementById('s_ed_preauth_act_status').value;
    var claimApprovedAmount = document.getElementById("s_ed_claim_approved_amt").value;
    if(preauthActStatus == 'C' && (claimApprovedAmount == '' || claimApprovedAmount == 0.00)) {
        var claimPaise = getPaise(document.getElementById("s_ed_claim_item_net_amount").value);
        setHiddenValue(id, "s_ed_claim_approved_amt", formatAmountPaise(claimPaise));
    }
}

function setDefaultApprvdQty(prefix,obj) {
	var row = getThisRow(obj);
	var preauthActStatus = document.getElementById(prefix+'_preauth_act_status').value;
    var apprdQty = document.getElementById(prefix+'_claim_approved_qty').value;
    if(preauthActStatus == 'C') {
        var itemQty = document.getElementById(prefix+'_claim_item_qty').value;
        document.getElementById(prefix+'_claim_approved_qty').disabled = false;
        if (apprdQty == '' || apprdQty == 0.00) {
            setHiddenValue(row, prefix+'_claim_approved_qty', itemQty);
        } else {
            setHiddenValue(row, prefix+'_claim_approved_qty', apprdQty);
        }
    } else if (preauthActStatus == 'O' || preauthActStatus == 'D'){
        document.getElementById(prefix+'_claim_approved_qty').disabled = true;
        setHiddenValue(row, prefix+'_claim_approved_qty', apprdQty);
    }
}

