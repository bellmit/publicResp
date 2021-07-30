YAHOO.util.Event.onContentReady("content", init);
function init() {
	if (defaultScreen) return;

	initToothNumberDialog();
	if (addPresc) {
		document.getElementsByName('presc_type')[0].checked = true;
		clearFields();
	}
	loadConductingPersonnel();
	showInsFieldSet();
}

function showInsFieldSet() {

	var itemType = getItemType();
	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (itemType == 'Doctor') {
		document.getElementById('insFieldSet').style.display = 'none';
	} else {
		if (mod_eclaim_preauth == 'Y' && isInsurancePatient) {
			document.getElementById('insFieldSet').style.display = 'block';
			if (multiPlanExists == true) {
				document.getElementById('secPreAuthRow').style.display = 'table-row';
			}
		} else {
			document.getElementById('insFieldSet').style.display = 'none';
		}
	}
}

function setPreAuth(obj) {
	document.getElementById('requirePriorAuth').value = obj.checked ? 'Y' : 'N';
}

function setDoNotOrderStatus(obj) {
	document.getElementById('do_not_order').value = obj.checked ? 'Y' : 'N';
}

function loadConductingPersonnel() {
	var itemType = getItemType();
	if (itemType != 'Inv.') return;

	var test_dept_id = document.getElementById('test_dept_id').value;
	if (empty(test_dept_id)) return;

	var el = document.getElementById('conducting_personnel');
	el.length = 1;
	var list = filterList(usersJson, "labDepartment", test_dept_id);
	if (empty(list)) return;

	for (var i=0; i<list.length; i++) {
		el.length = el.length+1;
		el.options[el.length-1].value = list[i].name;
		el.options[el.length-1].text = list[i].name;
	}
	el.value = conducting_personnel;
}

function onSave() {
	var itemType = getItemType();
	if (itemType == '') {
		showMessage("js.outpatient.pending.prescriptions.addshow.select.item_type");
		return false;
	}
	var itemName = document.getElementById('itemName').value;
	if (itemName == '') {
		showMessage("js.outpatient.pending.prescriptions.addshow.select.item");
		return false;insFieldSet
	}
	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (mod_eclaim_preauth == 'Y' && itemType == 'Inv.' ) {
		var itemFromPackage = document.getElementById('ispackage').value;
		if (isInsurancePatient && itemFromPackage == 'true') {
			showMessage("js.outpatient.pending.prescriptions.addshow.pkgitems.not.allowed");
			return false;
		}
	}
	if (itemType == 'Service') {
		var qty = document.getElementById('qty').value;

		var validNumber = /^[0-9]+$/;
		var regExp = new RegExp(validNumber);
		if (qty == '') {
			showMessage("js.outpatient.pending.prescriptions.addshow.enter.qty");
			return false;
		} else if (qty != '' && (!regExp.test(qty) || qty == 0)) {
			showMessage("js.outpatient.pending.prescriptions.addshow.enter.qty.gtzero");
			document.getElementById('ed_duration').focus();
			return false;
		}

		var tooth_num_req = document.getElementById('tooth_num_required').value;
		var tooth_number = document.getElementById('tooth_number').value;
		if (tooth_num_req == 'Y' && tooth_number == '') {
			showMessage("js.outpatient.pending.prescriptions.addshow.tooth_num_required");
			return false;
		}
	}
	var do_not_order = document.getElementById('chk_do_not_order').checked;
	var no_order_reason = document.getElementById('no_order_reason').value;
	if (do_not_order && no_order_reason == '') {
		showMessage("js.outpatient.pending.prescriptions.addshow.enter.reason");
		return false;
	}
	var priPreAuthNo = document.getElementById('pri_pre_auth_no').value;
	var priPreAuthMode = document.getElementById('pri_pre_auth_mode_id').value;
	if (priPreAuthNo != '' && priPreAuthMode == '') {
		alert(getString("js.common.order.prior.auth.mode.required")+"\n"+getString("js.common.order.select.prior.auth.mode.string"));
		return false;
	}


	document.presc_addoredit_form.submit();
	return true;
}

function getItemType() {
	if (!addPresc) return document.presc_addoredit_form.presc_type.value;

	var itemTypeObj = document.getElementsByName('presc_type');
	for (var i=0; i<itemTypeObj.length; i++) {
		if (itemTypeObj[i].checked)
			return itemTypeObj[i].value;
	}
	return null;
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
	var tnumbers = document.getElementById('tooth_number').value.split(",");
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
	document.getElementById('tooth_number').value = tooth_numbers;
	document.getElementById('toothNumberDiv').textContent = tooth_numbers_text;
	if (action != 'add')
		fieldEdited = true;
	childDialog = null;
	this.cancel();
}

function cancelToothNumDialog() {
	childDialog = null;
	toothNumDialog.cancel();
}


function onItemChange() {
	clearFields();
	showInsFieldSet();
	var itemType = getItemType();
}

var itemAutoComp = null;
function initItemAutoComplete() {
	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	var itemType = getItemType();
	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeAction.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId + "&center_id=" + centerId + "&tpa_id=" + tpaId;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "generic_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "route_of_admin"},
					{key : "consumption_uom"},
					{key : 'prior_auth_required'},
					{key : 'item_form_id'},
					{key : 'item_strength'},
					{key : 'tooth_num_required'},
					{key : 'item_strength_units'},
					{key : 'dept_id'}
				 ],
		numMatchFields: 2
	};

	itemAutoComp = new YAHOO.widget.AutoComplete("itemName", "itemContainer", ds);
	itemAutoComp.minQueryLength = 1;
	itemAutoComp.animVert = false;
	itemAutoComp.maxResultsDisplayed = 50;
	itemAutoComp.resultTypeList = false;
	itemAutoComp.forceSelection = true;

	itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		return highlightedValue;
	}

	itemAutoComp.dataRequestEvent.subscribe(clearItemDetails);
	itemAutoComp.itemSelectEvent.subscribe(selectItem);
	itemAutoComp.selectionEnforceEvent.subscribe(clearItemDetails);

	return itemAutoComp;
}

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	var prior_auth = record.prior_auth_required;
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}

	var markPriorAuthReqObj = document.getElementById('send_for_prior_auth');
	if (markPriorAuthReqObj != null) {
		if (prior_auth == 'A' && !empty(TPArequiresPreAuth) && TPArequiresPreAuth == 'Y') {
			markPriorAuthReqObj.checked = true;
		}else {
			markPriorAuthReqObj.checked = false;
		}
	}

	document.getElementById('item_id').value = record.item_id;
	document.getElementById('ispackage').value = record.ispkg;
	document.getElementById('test_dept_id').value = record.dept_id;

	document.getElementById('tooth_num_required').value = record.tooth_num_required;
	if (record.tooth_num_required == 'Y') {
		document.getElementById('toothNumBtnDiv').style.display = 'block';
		document.getElementById('toothNumDsblBtnDiv').style.display = 'none';
	} else {
		document.getElementById('toothNumBtnDiv').style.display = 'none';
		document.getElementById('toothNumDsblBtnDiv').style.display = 'block';
	}
	loadConductingPersonnel();
}

function clearFields() {
	document.getElementById('itemName').value = '';
	document.getElementById('serviceExtraInfoRow').style.display = 'none';

	var itemType = getItemType();
	document.getElementById('qty').value = '';
	if (itemType == 'Service') {
		document.getElementById('serviceExtraInfoRow').style.display = 'table-row';
		document.getElementById('qty').value = 1;
	}
	document.getElementById('item_remarks').value = '';
	document.getElementById('tooth_number').value = '';
	document.getElementById('toothNumBtnDiv').style.display = 'none';
	document.getElementById('toothNumDsblBtnDiv').style.display = 'block';
	document.getElementById('send_for_pre_auth').checked = false;
	document.getElementById('requirePriorAuth').value = 'N';
	document.getElementById('pri_pre_auth_no').value = '';
	document.getElementById('pri_pre_auth_mode_id').value = '';
	document.getElementById('sec_pre_auth_no').value = '';
	document.getElementById('sec_pre_auth_mode_id').value = '';
	document.getElementById('conducting_personnel').value = '';
	document.getElementById('chk_do_not_order').checked = false;
	document.getElementById('do_not_order').value = 'N';
	document.getElementById('no_order_reason').value = '';
	document.getElementById('ispackage').value = false;
	document.getElementById('test_dept_id').value = '';

	initItemAutoComplete();
   	clearItemDetails();
}

function clearItemDetails() {
	document.getElementById('item_id').value = '';
	loadConductingPersonnel();
}