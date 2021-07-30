var dosageAutoComplete = null; // dosage autocomplete for add item dialog.
function initFrequencyAutoComplete() {
	if (dosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [
						{key : "dosage_name"},
						{key : "per_day_qty"},
			]
		};
		// Instantiate first AutoComplete
		dosageAutoComplete = new YAHOO.widget.AutoComplete('s_d_frequency', 's_d_frequencyContainer', ds);
		dosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		dosageAutoComplete.useShadow = true;
		dosageAutoComplete.minQueryLength = 0;
		dosageAutoComplete.allowBrowserAutocomplete = false;
		dosageAutoComplete.maxResultsDisplayed = 20;
		dosageAutoComplete.resultTypeList = false;

		dosageAutoComplete.itemSelectEvent.subscribe(setPerDayQty);
		dosageAutoComplete.unmatchedItemSelectEvent.subscribe(checkDosage);
		dosageAutoComplete.textboxChangeEvent.subscribe(clearQty);
	}
}

function setPerDayQty(sType, oArgs) {
	if (document.getElementById('s_d_granular_units').value == 'Y' ) {
		var record = oArgs[2];
		document.getElementById('s_d_per_day_qty').value = record.per_day_qty;
	}
}

function checkDosage() {
	if (document.getElementById('s_d_granular_units').value == 'Y' ) {
		document.getElementById('s_d_per_day_qty').value = '';
	}
}

function clearQty(){
	if (document.getElementById('s_d_granular_units').value == 'Y' ) {
		document.getElementById('s_d_claim_item_qty').value = '';
	}
}

function calcQty(event, idPrefix) {
	if (document.getElementById(idPrefix + '_granular_units').value == 'Y' ) {
		var qty = '';
		var frequencyName = document.getElementById(idPrefix + '_frequency').value;
		var duration = document.getElementById(idPrefix + '_duration').value;
		var validNumber = /[1-9]/;
		var regExp = new RegExp(validNumber);

		if (!validateMedBlockExceptQty("onchange", idPrefix)) return false;

		var perDayQty = null;
		for (var i=0; i<medDosages.length; i++) {
			var frequency = medDosages[i];
			if (frequencyName.trim().toLowerCase() == frequency.dosage_name.trim().toLowerCase()) {
				perDayQty = frequency.per_day_qty;
			}
		}
		if (perDayQty != null && !empty(duration)) {
			var duration_units_els = document.getElementsByName(idPrefix+'_duration_units');
			var duration_units = 'D';
			for (var j=0; j<duration_units_els.length; j++) {
				if (duration_units_els[j].checked) {
					duration_units = duration_units_els[j].value;
					break;
				}
			}
			if (duration_units == 'D')
				qty = Math.ceil(duration * perDayQty);
			else if (duration_units == 'W')
				qty = Math.ceil((duration * 7) * perDayQty);
			else if (duration_units == 'M')
				qty = Math.ceil((duration * 30) * perDayQty);

		}
		document.getElementById(idPrefix + '_claim_item_qty').value = qty;
		var userUOMObj = document.getElementById(idPrefix + '_user_unit');
		if (userUOMObj != null && userUOMObj.options != null && userUOMObj.options.length > 1)
			setSelectedIndex(userUOMObj, "I");

	}
	onQtyChange(idPrefix);
}

function validateMedBlockExceptQty(calledOn, idPrefix) {
	var itemType = document.getElementById(idPrefix + "_itemType").value;
	if (itemType != 'Medicine' && itemType != 'NonHospital') return;

	var medicineName = document.getElementById(idPrefix + '_itemName').value;
	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);

	if (medicineName == '') {
		alert("Please enter the Medicine Name");
		return false;
	}
	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		alert("Duration should be greater than Zero and it should be a whole number.");
		document.getElementById(idPrefix + '_duration').focus();
		return false
	}
	return true;
}


var editDosageAutoComplete = null; // dosage autocomplete for edit item dialog.
function initEditDosageAutoComplete() {
	if (editDosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [
						{key : "dosage_name"},
						{key : "per_day_qty"},
			]
		};
		// Instantiate first AutoComplete
		editDosageAutoComplete = new YAHOO.widget.AutoComplete('s_ed_frequency', 's_ed_frequencyContainer', ds);
		editDosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		editDosageAutoComplete.useShadow = true;
		editDosageAutoComplete.minQueryLength = 0;
		editDosageAutoComplete.allowBrowserAutocomplete = false;
		editDosageAutoComplete.maxResultsDisplayed = 20;
		editDosageAutoComplete.resultTypeList = false;

		editDosageAutoComplete.itemSelectEvent.subscribe(editSetPerDayQty);
		editDosageAutoComplete.unmatchedItemSelectEvent.subscribe(editCheckDosage);
		editDosageAutoComplete.textboxChangeEvent.subscribe(editClearQty);
	}
}

function editSetPerDayQty(sType, oArgs) {
	if (document.getElementById('s_ed_granular_units').value == 'Y' ) {
		var record = oArgs[2];
		document.getElementById('ed_per_day_qty').value = record.per_day_qty;
	}
}

function editCheckDosage() {
	if (document.getElementById('s_ed_granular_units').value == 'Y' ) {
		document.getElementById('ed_per_day_qty').value = '';
	}
}

function editClearQty() {
	if (document.getElementById('s_ed_granular_units').value == 'Y' ) {
		document.getElementById('s_ed_claim_item_qty').value = '';
		setSIQtyFieldsEdited();
		setSIEdited();
	}
}

var siEditItemAutoComp = null;
function initSIEditItemAutoComplete() {
	if (!empty(siEditItemAutoComp)) {
		siEditItemAutoComp.destroy();
		siEditItemAutoComp = null;
	}
	var itemType = document.getElementById('s_ed_itemType').value;
	if (itemType == 'Instructions' || itemType == 'NonHospital') return null; // for doctor instrctions no need to create the autocomplete.

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
					{key : "generic_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "item_form_id"},
					{key : "item_strength"},
					{key : 'item_strength_units'},
					{key : "package_uom"},
					{key : 'issue_uom'},
					{key : 'granular_units'}
				 ],
		numMatchFields: 2
	};

	siEditItemAutoComp = new YAHOO.widget.AutoComplete("s_ed_itemName", "s_ed_itemContainer", ds);
	siEditItemAutoComp.minQueryLength = 1;
	siEditItemAutoComp.animVert = false;
	siEditItemAutoComp.maxResultsDisplayed = 50;
	siEditItemAutoComp.resultTypeList = false;
	var forceSelection = true;
	if (itemType == 'Medicine' && use_store_items != 'Y')
		forceSelection = false;
	siEditItemAutoComp.forceSelection = forceSelection;

	siEditItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	siEditItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			// show qty only for pharmacy items.
			if (use_store_items == 'Y')
				highlightedValue += "(" + record.qty + ") ";
		}
		return highlightedValue;
	}

	siEditItemAutoComp.dataRequestEvent.subscribe(clearSIEditMasterType);
	if (forceSelection) {
		siEditItemAutoComp.itemSelectEvent.subscribe(selectSIEditItem);
		siEditItemAutoComp.selectionEnforceEvent.subscribe(clearSIEditMasterType);
	} else {
		siEditItemAutoComp.itemSelectEvent.subscribe(selectSIEditItem);
	}

	return siEditItemAutoComp;
}

function clearSIEditMasterType(oSelf) {
	//clearMasterSIEditFields('s_ed');
}

function clearMasterSIEditFields(prefix) {
	document.getElementById(prefix+'_item_id').value = '';
	document.getElementById(prefix+'_item_prescribed_id').value = '';

	document.getElementById(prefix+'_ispackage').value = '';
	document.getElementById(prefix+'_consumption_uom').value = '';
	document.getElementById(prefix+'_medicineUOM').textContent = '';
	document.getElementById(prefix+'_item_master').value = '';
	document.getElementById(prefix+'_granular_units').value = '';

	if (!useGenerics)
		document.getElementById(prefix+'_medicine_route').length = 1;
	else
		document.getElementById(prefix+'_medicine_route').selectedIndex = 0;

	//document.getElementById(prefix+'_frequency').value = '';
	document.getElementById(prefix+'_strength').value = '';
	document.getElementById(prefix+'_item_strength_units').selectedIndex = 0;

	document.getElementById(prefix+'_item_form_id').value = '';
	document.getElementById(prefix+'_item_strength').value = '';

	document.getElementById(prefix+'_code_type').textContent = '';
	document.getElementById(prefix+'_code').textContent = '';

	document.getElementById(prefix+'_package_size').value = '';
	document.getElementById(prefix+'_pkg_size_label').textContent = '';
	document.getElementById(prefix+'_user_unit').options.length = 0;

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

	//clearObservationFields(prefix);
}

function setGranularUnit(event, prefix) {
	var itemFormId = document.getElementById(prefix + '_item_form_id').value;
	var granularUnitForItem = filterList(itemFormList, "item_form_id", itemFormId);
	var granular_unit = '';
	if (granularUnitForItem.length > 0) {
		for (var k=0; k <granularUnitForItem.length; k++) {
			granular_unit = granularUnitForItem[k].granular_units;
			break;
		}
	}
	if (!empty(granular_unit)) {
		document.getElementById(prefix + '_granular_units').value = granular_unit;
		document.getElementById(prefix + '_claim_item_qty').value = '';
		if (granular_unit == 'Y')
			calcQty(event, prefix);
		else {
			document.getElementById(prefix + '_claim_item_qty').value = 1;
			calcQty(event, prefix);
		}
	}
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
	if (record.item_type == 'Medicine') {
		document.getElementById('s_ed_qty_in_stock').value = record.qty;
	}

	var userUOMObj = document.getElementById('s_ed_user_unit');
	var optIdx = 0;
	if (!empty(record.issue_uom)) {
		optIdx++;
		userUOMObj.length = optIdx;
		userUOMObj.options[optIdx-1].value = 'I';
		userUOMObj.options[optIdx-1].text = record.issue_uom;
	}
	if (!empty(record.package_uom) && record.issue_uom != record.package_uom) {
		optIdx++;
		userUOMObj.length = optIdx;
		userUOMObj.options[optIdx-1].value = 'P';
		userUOMObj.options[optIdx-1].text = record.package_uom;
	}

	if (!empty(record.issue_uom))
		setSelectedTextIndex(userUOMObj, record.issue_uom);

	document.getElementById('s_ed_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('s_ed_medicineUOM').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('s_ed_ispackage').value = record.ispkg;
	document.getElementById('s_ed_item_master').value = record.master;
	document.getElementById('s_ed_item_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('s_ed_item_strength').value = record.item_strength;
	document.getElementById('s_ed_item_strength_units').value = record.item_strength_units;
	document.getElementById('s_ed_granular_units').value = record.granular_units;
	if (record.granular_units != 'Y') {
		document.getElementById('s_ed_claim_item_qty').value = 1;
	}

	if (document.getElementById('s_ed_item_form_id').options.length == 2)
		document.getElementById('s_ed_item_form_id').selectedIndex = 1;
	if (document.getElementById('s_ed_item_strength_units').options.length == 2)
		document.getElementById('s_ed_item_strength_units').selectedIndex = 1;

	getEditRouteOfAdministrations();

	onQtyChange('s_ed');
}


var siItemAutoComp = null;
function initSIItemAutoComplete() {
	if (!empty(siItemAutoComp)) {
		siItemAutoComp.destroy();
		siItemAutoComp = null;
	}
	var itemType = document.getElementById('s_d_itemType').value;
	if (itemType == 'Instructions' || itemType == 'NonHospital') return null; // for doctor instrctions no need to create the autocomplete.

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
					{key : "generic_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "item_form_id"},
					{key : "item_strength"},
					{key : 'item_strength_units'},
					{key : "package_uom"},
					{key : 'issue_uom'},
					{key : 'granular_units'}
				 ],
		numMatchFields: 2
	};

	siItemAutoComp = new YAHOO.widget.AutoComplete("s_d_itemName", "s_d_itemContainer", ds);
	siItemAutoComp.minQueryLength = 1;
	siItemAutoComp.animVert = false;
	siItemAutoComp.maxResultsDisplayed = 50;
	siItemAutoComp.resultTypeList = false;
	var forceSelection = true;
	if (itemType == 'Medicine' && use_store_items != 'Y')
		forceSelection = false;
	siItemAutoComp.forceSelection = forceSelection;

	siItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	siItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			// show qty only for pharmacy items.
			if (use_store_items == 'Y')
				highlightedValue += "(" + record.qty + ") ";
		}
		return highlightedValue;
	}

	siItemAutoComp.dataRequestEvent.subscribe(clearSIMasterType);
	if (forceSelection) {
		siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
		siItemAutoComp.selectionEnforceEvent.subscribe(clearSIMasterType);
	} else {
		siItemAutoComp.itemSelectEvent.subscribe(selectSIItem);
	}

	return siItemAutoComp;
}

function clearSIMasterType(oSelf) {
	clearMasterSIFields('s_d');
}

function clearMasterSIFields(prefix) {
	document.getElementById(prefix+'_item_id').value = '';
	if (prefix == 's_d') {
		document.getElementById(prefix+'_genericNameAnchor_dialog').style.display = 'none';
		document.getElementById(prefix+'_genericNameAnchor_dialog').href = '';
		document.getElementById(prefix+'_genericNameAnchor_dialog').innerHTML = '';
		document.getElementById(prefix+'_generic_code').value = '';
		document.getElementById(prefix+'_generic_name').value = '';
		document.getElementById(prefix+'_qty_in_stock').value = '';
		document.getElementById(prefix+'_granular_units').value = '';
	}else {
		document.getElementById(prefix+'_genericNameAnchor_editdialog').style.display = 'none';
		document.getElementById(prefix+'_genericNameAnchor_editdialog').href = '';
		document.getElementById(prefix+'_genericNameAnchor_editdialog').innerHTML = '';
	}

	document.getElementById(prefix+'_ispackage').value = '';
	document.getElementById(prefix+'_consumption_uom').value = '';
	document.getElementById(prefix+'_medicineUOM').textContent = '';
	document.getElementById(prefix+'_item_master').value = '';

	if (!useGenerics)
		document.getElementById(prefix+'_medicine_route').length = 1;
	else
		document.getElementById(prefix+'_medicine_route').selectedIndex = 0;

	document.getElementById(prefix+'_frequency').value = '';
	document.getElementById(prefix+'_strength').value = '';
	document.getElementById(prefix+'_item_strength_units').selectedIndex = 0;
	document.getElementById(prefix+'_duration').value = '';

	var itemType = 'Medicine';
	var enable = itemType == 'Medicine' || itemType == 'NonHospital';
	toggleDurationUnits(enable, 's_d');
	if (enable)
		document.getElementsByName(prefix+'_duration_units')[0].checked = true;

	document.getElementById(prefix+'_claim_item_qty').value = '';

	document.getElementById(prefix+'_item_form_id').value = '';
	document.getElementById(prefix+'_item_strength').value = '';

	document.getElementById(prefix+'_code_type').textContent = '';
	document.getElementById(prefix+'_code').textContent = '';

	document.getElementById(prefix+'_package_size').value = '';
	document.getElementById(prefix+'_pkg_size_label').textContent = '';
	document.getElementById(prefix+'_user_unit').options.length = 0;

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

	//clearObservationFields(prefix);
}


function clearObservationFields(prefix) {

	document.getElementById(prefix+'_item_strength_units').value = '';
	//document.getElementById(prefix+'_medicine_route_value_type').value = '';
	//document.getElementById(prefix+'_strength_value_type').value = '';
	//document.getElementById(prefix+'_frequency_value_type').value = '';
	//document.getElementById(prefix+'_item_form_value_type').value = '';
	//document.getElementById(prefix+'_remarks_value_type').value = '';
	//document.getElementById(prefix+'_refills_value_type').value = '';

	document.getElementById(prefix+'_item_strength_code_type').textContent = '';
	document.getElementById(prefix+'_item_strength_code').textContent = '';

	document.getElementById(prefix+'_medicine_route_code_type').textContent = '';
	document.getElementById(prefix+'_medicine_route_code').textContent = '';

	document.getElementById(prefix+'_strength_code_type').textContent = '';
	document.getElementById(prefix+'_strength_code').textContent = '';

	document.getElementById(prefix+'_frequency_code_type').textContent = '';
	document.getElementById(prefix+'_frequency_code').textContent = '';

	document.getElementById(prefix+'_duration_code_type').textContent = '';
	document.getElementById(prefix+'_duration_code').textContent = '';

	document.getElementById(prefix+'_item_form_code_type').textContent = '';
	document.getElementById(prefix+'_item_form_code').textContent = '';

	document.getElementById(prefix+'_remarks_code_type').textContent = '';
	document.getElementById(prefix+'_remarks_code').textContent = '';

	// TODO: Need to save these.
	document.getElementById(prefix+'_refills_code_type').textContent = '';
	document.getElementById(prefix+'_refills_code').textContent = '';

}

function selectSIItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('s_d_item_id').value = record.item_id;
	if (record.item_type == 'Medicine') {
		document.getElementById('s_d_qty_in_stock').value = record.qty;
		if (!empty(record.generic_name)) {
			document.getElementById('s_d_genericNameAnchor_dialog').style.display = 'block';
			document.getElementById('s_d_genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "s_d_", "dialog", "'+record.generic_code+'")';
			document.getElementById('s_d_genericNameAnchor_dialog').innerHTML = record.generic_name;
			document.getElementById('s_d_generic_code').value = record.generic_code;
			document.getElementById('s_d_generic_name').value = record.generic_name;
		}
	}

	var userUOMObj = document.getElementById('s_d_user_unit');
	var optIdx = 0;
	if (!empty(record.issue_uom)) {
		optIdx++;
		userUOMObj.length = optIdx;
		userUOMObj.options[optIdx-1].value = 'I';
		userUOMObj.options[optIdx-1].text = record.issue_uom;
	}
	if (!empty(record.package_uom) && record.issue_uom != record.package_uom) {
		optIdx++;
		userUOMObj.length = optIdx;
		userUOMObj.options[optIdx-1].value = 'P';
		userUOMObj.options[optIdx-1].text = record.package_uom;
	}

	var storeId = document.getElementById('_phStore').value;
	var selectedStore = findInList(jStores, "dept_id", storeId);
	var store_sale_unit = "I";
	if (!empty(selectedStore) && !empty(selectedStore.sale_unit))
		store_sale_unit = selectedStore.sale_unit;

	setSelectedIndex(userUOMObj, store_sale_unit);

	document.getElementById('s_d_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('s_d_medicineUOM').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('s_d_ispackage').value = record.ispkg;
	document.getElementById('s_d_item_master').value = record.master;
	document.getElementById('s_d_item_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('s_d_item_strength').value = record.item_strength;
	document.getElementById('s_d_item_strength_units').value = record.item_strength_units;
	document.getElementById('s_d_item_strength_units').selectedIndex=document.getElementById('s_d_item_strength_units').selectedIndex == -1 ? 0:
		document.getElementById('s_d_item_strength_units').selectedIndex;
	document.getElementById('s_d_granular_units').value = record.granular_units;
	if (record.granular_units != 'Y') {
		document.getElementById('s_d_claim_item_qty').value = 1;
	}

	if (document.getElementById('s_d_item_form_id').options.length == 2)
		document.getElementById('s_d_item_form_id').selectedIndex = 1;
	if (document.getElementById('s_d_item_strength_units').options.length == 2)
		document.getElementById('s_d_item_strength_units').selectedIndex = 1;

	getRouteOfAdministrations();
}

function isRequestDataEditable() {
	var erxPrescIdObj = document.getElementById("erx_presc_id");
	var erxRefNoObj = document.getElementById("erx_reference_no");
	var erxApprovalStatusObj = document.getElementById("erx_approval_status");
	if (erxPrescIdObj != null && erxRefNoObj != null && erxApprovalStatusObj != null) {
		var erxPrescId = erxPrescIdObj.value;
		var erxRefNo = erxRefNoObj.value;
		var erxApprStatus = erxApprovalStatusObj.value;
		if (empty(erxPrescId) || empty(erxRefNo) || empty(erxApprStatus)) {
			return false;
		}
	}
	return true;
}

function validateAddOrEditItemData() {
	if (!isRequestDataEditable()) {
		alert(" Consultation ERx Request is not Approved. \n " +
				"Add/Edit is not allowed.");
		return false;
	}
	return true;
}

function validateConsultationAndPBMFieldValues(prefix, onSave) {
	onSave = empty(onSave) ? false : onSave;

	if (onSave) {
		var message = "Some of the following fields are not same as prescribed during consultation: \n\n";
		message += " * Strength \n";
		message += " * Strength Units \n";
		message += " * Item Form \n";
		message += " * Dosage \n";
		message += " * Route \n";
		message += " * Frequency \n";
		message += " * Duration \n";
		message += " * Duration Units \n";
		message += " * Medicine Quantity \n";

		var msglen = message.length;

		var els = document.getElementsByName('s_issued');
		for (var i=0; i<els.length-1 ; i++) {
			var itemType = document.getElementsByName('s_itemType')[i].value;
			var markedForDelete = document.getElementsByName("s_delItem")[i].value;
			if (itemType != 'Medicine') continue; // skip if it is not a medicine.

			if (els[i].value == 'Y') continue;

			if (markedForDelete == 'true') continue;

			var item_strength = document.getElementsByName('s_item_strength')[i].value;
			var item_strength_units = document.getElementsByName('s_item_strength_units')[i].value;
			var item_strength_units_name = document.getElementsByName('s_item_strength_units_name')[i].value;
			var dosage = document.getElementsByName('s_strength')[i].value;
			var routeId = document.getElementsByName('s_route_id')[i].value;
			var routeName = document.getElementsByName('s_route_name')[i].value;
			var frequency = document.getElementsByName('s_frequency')[i].value;
			var duration = document.getElementsByName('s_duration')[i].value;
			var duration_units = document.getElementsByName('s_duration_units')[i].value;
			var item_form_id = document.getElementsByName('s_item_form_id')[i].value;
			var item_form_name = document.getElementsByName('s_item_form_name')[i].value;
			var medicine_quantity = document.getElementsByName('s_medicine_quantity')[i].value;

			var pat_item_prescribed_id = document.getElementsByName('pat_item_prescribed_id')[i].value;
			var pat_item_strength = document.getElementsByName('pat_item_strength')[i].value;
			var pat_item_strength_units = document.getElementsByName('pat_item_strength_units')[i].value;
			var pat_item_strength_units_name = document.getElementsByName('pat_item_strength_units_name')[i].value;
			var pat_dosage = document.getElementsByName('pat_strength')[i].value;
			var pat_routeId = document.getElementsByName('pat_route_id')[i].value;
			var pat_routeName = document.getElementsByName('pat_route_name')[i].value;
			var pat_frequency = document.getElementsByName('pat_frequency')[i].value;
			var pat_duration = document.getElementsByName('pat_duration')[i].value;
			var pat_duration_units = document.getElementsByName('pat_duration_units')[i].value;
			var pat_item_form_id = document.getElementsByName('pat_item_form_id')[i].value;
			var pat_item_form_name = document.getElementsByName('pat_item_form_name')[i].value;
			var pat_medicine_quantity = document.getElementsByName('pat_medicine_quantity')[i].value;

			if (empty(pat_item_prescribed_id) || pat_item_prescribed_id == 0)
				continue;

			if (((!empty(item_strength) || !empty(pat_item_strength)) && trim(item_strength) != trim(pat_item_strength)) ||
				((!empty(item_strength_units_name) || !empty(pat_item_strength_units_name)) && trim(item_strength_units_name) != trim(pat_item_strength_units_name)) ||
				((!empty(dosage) || !empty(pat_dosage)) && trim(dosage) != trim(pat_dosage)) ||
				((!empty(routeName) || !empty(pat_routeName)) && trim(routeName) != trim(pat_routeName)) ||
				((!empty(frequency) || !empty(pat_frequency)) && trim(frequency) != trim(pat_frequency)) ||
				((!empty(duration) || !empty(pat_duration)) && trim(duration) != trim(pat_duration))  ||
				((!empty(duration_units) || !empty(pat_duration_units)) && trim(duration_units) != trim(pat_duration_units)) ||
				((!empty(item_form_name) || !empty(pat_item_form_name)) && trim(item_form_name) != trim(pat_item_form_name)) ||
				((!empty(medicine_quantity) || !empty(pat_medicine_quantity)) && trim(medicine_quantity) != trim(pat_medicine_quantity)) ) {
				message += "\n for item "+document.getElementsByName('s_item_name')[i].value;
			}
		}

		var errmsglen = message.length;
		if (errmsglen > msglen) {
			var ok = confirm("Warning: "+message +" \n Do you want to continue?");
			if (!ok)
				return false;
		}

	} else {
		if (prefix == 's_ed') {
			var message = "The following fields are not same as prescribed during consultation: \n\n";
			var msglen = message.length;

			var item_strength = document.getElementById(prefix + '_item_strength').value;
			var item_strength_units = document.getElementById(prefix + '_item_strength_units').value;
			var item_strength_units_name = item_strength_units != "" ?
			(document.getElementById(prefix + '_item_strength_units')
			.options[document.getElementById(prefix + '_item_strength_units').selectedIndex].text) : "";
			var dosage = document.getElementById(prefix + '_strength').value;
			var routeId = document.getElementById(prefix + '_medicine_route').value;
			var routeName = routeId != "" ? (document.getElementById(prefix + '_medicine_route')
			.options[document.getElementById(prefix + '_medicine_route').selectedIndex].text) : "";
			var item_form_id = document.getElementById(prefix + '_item_form_id').value;
			var item_form_name = item_form_id != "" ? (document.getElementById(prefix + '_item_form_id')
			.options[document.getElementById(prefix + '_item_form_id').selectedIndex].text) : "";
			var frequency = document.getElementById(prefix + '_frequency').value;
			var medicine_quantity = document.getElementById(prefix + '_claim_item_qty').value;

			var duration = document.getElementById(prefix + '_duration').value;
			var duration_units;
			var du_els = document.getElementsByName(prefix + '_duration_units');
			for (var k=0; k<du_els.length; k++) {
				if (du_els[k].checked) {
					duration_units = du_els[k].value;
					break;
				}
			}

			var id = document.getElementById('s_ed_editRowId').value ;
			var pat_item_prescribed_id = getIndexedValue("pat_item_prescribed_id", id);
			var pat_item_strength = getIndexedValue("pat_item_strength", id);
			var pat_item_strength_units = getIndexedValue("pat_item_strength_units", id);
			var pat_item_strength_units_name = getIndexedValue("pat_item_strength_units_name", id);
			var pat_dosage = getIndexedValue("pat_strength", id);
			var pat_routeId = getIndexedValue("pat_route_id", id);
			var pat_routeName = getIndexedValue("pat_route_name", id);
			var pat_frequency = getIndexedValue("pat_frequency", id);
			var pat_duration = getIndexedValue("pat_duration", id);
			var pat_duration_units = getIndexedValue("pat_duration_units", id);
			var pat_item_form_id = getIndexedValue("pat_item_form_id", id);
			var pat_item_form_name = getIndexedValue("pat_item_form_name", id);
			var pat_medicine_quantity = getIndexedValue("pat_medicine_quantity", id);

			if (empty(pat_item_prescribed_id) || pat_item_prescribed_id == 0){}
			else {
				if ((!empty(item_strength) || !empty(pat_item_strength)) && trim(item_strength) != trim(pat_item_strength))
					message += " * Strength : "+(empty(pat_item_strength) ? "" : pat_item_strength)+" \n";

				if ((!empty(item_strength_units_name) || !empty(pat_item_strength_units_name)) && trim(item_strength_units_name) != trim(pat_item_strength_units_name))
					message += " * Strength Units : "+(empty(pat_item_strength_units_name) ? "" : pat_item_strength_units_name)+" \n";

				if ((!empty(dosage) || !empty(pat_dosage)) && trim(dosage) != trim(pat_dosage))
					message += " * Dosage : "+(empty(pat_dosage) ? "" : pat_dosage)+" \n";

				if ((!empty(routeName) || !empty(pat_routeName)) && trim(routeName) != trim(pat_routeName))
					message += " * Route : "+(empty(pat_routeName) ? "" : pat_routeName)+" \n";

				if ((!empty(frequency) || !empty(pat_frequency)) && trim(frequency) != trim(pat_frequency))
					message += " * Frequency : "+(empty(pat_frequency) ? "" : pat_frequency)+" \n";

				if ((!empty(duration) || !empty(pat_duration)) && trim(duration) != trim(pat_duration))
					message += " * Duration : "+(empty(pat_duration) ? "" : pat_duration)+" \n";

				if ((!empty(duration_units) || !empty(pat_duration_units)) && trim(duration_units) != trim(pat_duration_units))
					message += " * Duration Units : "+(empty(pat_duration_units) ? "" : pat_duration_units)+" \n";

				if ((!empty(item_form_name) || !empty(pat_item_form_name)) && trim(item_form_name) != trim(pat_item_form_name))
					message += " * Item Form : "+(empty(pat_item_form_name) ? "" : pat_item_form_name)+" \n";

				if ((!empty(medicine_quantity) || !empty(pat_medicine_quantity)) && trim(medicine_quantity) != trim(pat_medicine_quantity))
					message += " * Medicine Quantity : "+(empty(pat_medicine_quantity) ? "" : pat_medicine_quantity)+" \n";
			}
			var errmsglen = message.length;
			if (errmsglen > msglen) {
				var ok = confirm("Warning: "+message +" \n Do you want to continue?");
				if (!ok)
					return false;
			}
		}
	}
	return true;
}

function validatePBMFields(prefix, onSave) {
	onSave = empty(onSave) ? false : onSave;
	var message = "The following fields are required \n\n";
		//message += " * Strength \n";
		//message += " * Strength Units \n";
		//message += " * Item Form \n";
		//message += " * Dosage \n";
		//message += " * Route \n";
		//message += " * Frequency \n";
		message += " * Duration \n";
		message += " * Duration Units \n";

	if (onSave) {
		var els = document.getElementsByName('s_issued');
		for (var i=0; i<els.length-1 ; i++) {
			var itemType = document.getElementsByName('s_itemType')[i].value;
			var markedForDelete = document.getElementsByName("s_delItem")[i].value;
			if (itemType != 'Medicine') continue; // skip if it is not a medicine.

			if (els[i].value == 'Y') continue;

			if (markedForDelete == 'true') continue;

			//var item_strength = document.getElementsByName('s_item_strength')[i].value;
			//var item_strength_units = document.getElementsByName('s_item_strength_units')[i].value
			//var dosage = document.getElementsByName('s_strength')[i].value;
			//var routeId = document.getElementsByName('s_route_id')[i].value;
			//var frequency = document.getElementsByName('s_frequency')[i].value;
			var duration = document.getElementsByName('s_duration')[i].value;
			var duration_units = document.getElementsByName('s_duration_units')[i].value;
			//var item_form_id = document.getElementsByName('s_item_form_id')[i].value;

			/*if (empty(item_strength) || empty(item_strength_units) || empty(dosage) || empty(routeId) ||
				empty(frequency) || empty(duration) || empty(duration_units) || (empty(item_form_id) || item_form_id == 0)) {
				alert(message + "\n for item "+document.getElementsByName('s_item_name')[i].value);
				return false;
			}*/

			if (empty(duration) || empty(duration_units)) {
				alert(message + "\n for item "+document.getElementsByName('s_item_name')[i].value);
				return false;
			}
		}
	} else {
		/*var item_strength = document.getElementById(prefix + '_item_strength').value;
		var item_strength_units = document.getElementById(prefix + '_item_strength_units').value
		var dosage = document.getElementById(prefix + '_strength').value;
		var routeId = document.getElementById(prefix + '_medicine_route').value;
		var item_form_id = document.getElementById(prefix + '_item_form_id').value;
		var frequency = document.getElementById(prefix + '_frequency').value;
		*/
		var duration = document.getElementById(prefix + '_duration').value;
		var duration_units;
		var du_els = document.getElementsByName(prefix + '_duration_units');
		for (var k=0; k<du_els.length; k++) {
			if (du_els[k].checked) {
				duration_units = du_els[k].value;
				break;
			}
		}
		/*if (empty(item_strength) || empty(item_strength_units) || empty(dosage) || empty(routeId) ||
			empty(frequency) || empty(duration) || empty(duration_units) || (empty(item_form_id) || item_form_id == 0)) {
			alert(message);
			return false;
		}*/
		if (empty(duration) || empty(duration_units)) {
			alert(message);
			return false;
		}
	}
	return true;
}

function validateEprxFields(prefix, onSave) {
	onSave = empty(onSave) ? false : onSave;
	var message = "Please enter values for following fields for ePrescription \n\n";
		message += " * Dosage \n";
		message += " * Route \n";
		message += " * Frequency \n";
		message += " * Duration \n";
		message += " * Duration Units \n";
		message += " * Total Quantity \n";
		message += " * Remarks \n";

	if (onSave) {
		var els = document.getElementsByName('s_issued');
		for (var i=0; i<els.length-1 ; i++) {
			var itemType = document.getElementsByName('s_itemType')[i].value;
			var markedForDelete = document.getElementsByName("s_delItem")[i].value;
			if (itemType != 'Medicine') continue; // skip if it is not a medicine.

			if (els[i].value == 'Y') continue;

			if (markedForDelete == 'true') continue;

			var dosage = document.getElementsByName('s_strength')[i].value;
			var routeId = document.getElementsByName('s_route_id')[i].value;
			var frequency = document.getElementsByName('s_frequency')[i].value;
			var duration = document.getElementsByName('s_duration')[i].value;
			var duration_units = document.getElementsByName('s_duration_units')[i].value;
			var remarks = document.getElementsByName('s_remarks')[i].value;

			if (empty(dosage) || empty(routeId) ||
				empty(frequency) || empty(duration) || empty(duration_units) || empty(remarks)) {
				alert(message + "\n for item "+document.getElementsByName('s_item_name')[i].value);
				return false;
			}
		}
	} else {

		var dosage = document.getElementById(prefix + '_strength').value;
		var routeId = document.getElementById(prefix + '_medicine_route').value;
		var frequency = document.getElementById(prefix + '_frequency').value;
		var duration = document.getElementById(prefix + '_duration').value;
		var du_els = document.getElementsByName(prefix + '_duration_units');
		var remarks = document.getElementById(prefix + '_remarks').value;

		var duration_units;
		var du_els = document.getElementsByName(prefix + '_duration_units');
		for (var k=0; k<du_els.length; k++) {
			if (du_els[k].checked) {
				duration_units = du_els[k].value;
				break;
			}
		}
		if (empty(dosage) || empty(routeId) ||
			empty(frequency) || empty(duration) || empty(duration_units) || empty(remarks)) {
			alert(message);
			return false;
		}
	}
	return true;
}

function clearSITable() {
	var table = document.getElementById('siTable');
	var numRows = getNumCharges("siTable");
	for (var index = numRows; index >0; index--) {
		var row = table.rows[index];
		var newEl = getElementByName(row, 's_item_prescribed_id');
		if (newEl.value == '_') {
			row.parentNode.removeChild(row);
		}
	}
}

function onStoreChange() {

	var storeId = document.getElementById('_phStore').value;
	var pbmPrescId = mainform.pbm_presc_id.value;

	clearSITable();

	var numRows = getNumCharges("siTable");

	if (numRows > 0) {
		for (var r = 0; r < numRows; r++) {
			var medId = getIndexedValue("s_item_id", r)
			if (empty(medId)) {
				alert(" Store cannot be selected. \n There are some medicines with generic names."
					+" \n Enter medicine names before selecting store. ");
				document.getElementById('_phStore').selectedIndex = 0;
				return false;
			}
		}
	}

	if (empty(storeId) && numRows > 0) {
		alert("No Presc. Store is selected");
		return false;
	}

	var url = cpath+'/PBMAuthorization/PBMPresc.do?_method=getPBMPrescRateDetails';
	url += '&pbmPrescId='+pbmPrescId;
	url += '&storeId='+storeId;

	if (!empty(storeId) && !empty(pbmPrescId)) {
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var pbmPrescRateDetails =" + ajaxobj.responseText);
					if (!empty(pbmPrescRateDetails)) {
						setPBMPrescRateDetails(pbmPrescRateDetails);
					}else
						alert("PBM Presc Rate and Claim amount calculation error.");
				}
			}
		}
	}
	resetClaimTotals();
}

function setPBMPrescRateDetails(prescRates) {

	var num = getNumCharges("siTable");

	for (var id = 0; id < num; id++) {

		var deletedObj = getIndexedFormElement(mainform, "s_delItem", id);
		if (deletedObj.value == 'true')
			continue;

		for (var j = 0; j < prescRates.length; j++) {

			var rItemPrescribedId = prescRates[j].pbm_medicine_pres_id;

			if (getIndexedValue("s_item_prescribed_id", id) == rItemPrescribedId) {

				setHiddenValue(id, "s_item_qty", prescRates[j].medicine_quantity);
				setHiddenValue(id, "s_medicine_quantity", prescRates[j].medicine_quantity);
				setHiddenValue(id, "s_item_package_unit", prescRates[j].package_size);

				setHiddenValue(id, "s_item_user_unit", prescRates[j].user_unit);
				setHiddenValue(id, "s_qty_in_stock", getAmount(prescRates[j].total_available_qty));

				setHiddenValue(id, "s_item_rate", formatAmountValue(prescRates[j].rate));
				setHiddenValue(id, "s_item_disc", formatAmountValue(prescRates[j].discount));

				setHiddenValue(id, "s_item_amount", formatAmountValue(prescRates[j].amount));
				setHiddenValue(id, "s_patient_amount", formatAmountValue(prescRates[j].patient_amount));
				setHiddenValue(id, "s_claim_net_amount", formatAmountValue(prescRates[j].claim_net_amount));

				setHiddenValue(id, "s_orig_item_rate", formatAmountValue(prescRates[j].rate));
				setHiddenValue(id, "s_orig_item_amount", formatAmountValue(prescRates[j].amount));
				setHiddenValue(id, "s_orig_claim_net_amount", formatAmountValue(prescRates[j].claim_net_amount));

				setHiddenValue(id, "s_item_code_type", prescRates[j].code_type);
				setHiddenValue(id, "s_item_code", prescRates[j].item_code);

				setHiddenValue(id, "s_pkg_size", prescRates[j].package_size);

				var row = getChargeRow(id, 'siTable');

				setNodeText(row.cells[ITEM_RATE_COL], formatAmountValue(prescRates[j].rate));
				setNodeText(row.cells[ITEM_DISC_COL], formatAmountValue(prescRates[j].discount));
				setNodeText(row.cells[ITEM_AMOUNT_COL], formatAmountValue(prescRates[j].amount));
				setNodeText(row.cells[ITEM_PATIENT_AMT_COL], formatAmountValue(prescRates[j].patient_amount));
				setNodeText(row.cells[ITEM_CLAIM_AMT_COL], formatAmountValue(prescRates[j].claim_net_amount));
				setNodeText(row.cells[ITEM_USER_UOM], prescRates[j].user_uom);

				itemsEdited++;
				setIndexedValue("s_edited", id, 'true');
				setSIRowStyle(id);
			}
		}
	}
}


function onQtyChange(prefix) {

	var storeId = document.getElementById('_phStore').value;
	var visitId = document.getElementById('patient_id').value;
	var visitType = document.getElementById('visit_type').value;
	var orgId = document.getElementById('org_id').value;
	var planId = document.getElementById('plan_id').value;
	var medicineId = document.getElementById(prefix+'_item_id').value;
	var medQty = document.getElementById(prefix+'_claim_item_qty').value;
	var itemUOM = document.getElementById(prefix+'_user_unit').value;
	var itemPrescId = document.getElementById(prefix+'_item_prescribed_id').value;
	if (empty(itemPrescId))
		itemPrescId = 0;

	if (empty(storeId)) {
		alert("No Presc. Store is selected");
		return false;
	}

	if (empty(medicineId)) {
		alert("No medicine is selected");
		return false;
	}

	var url = cpath+'/PBMAuthorization/PBMPresc.do?_method=ajaxPBMPrescRate';
	url += '&store_id='+storeId;
	url += '&patient_id='+visitId;
	url += '&visit_type='+visitType;
	url += '&org_id='+orgId;
	url += '&plan_id='+planId;
	url += '&medicine_id='+medicineId;
	url += '&medicine_quantity='+medQty;
	url += '&item_uom='+itemUOM;
	url += '&item_prescribed_id='+itemPrescId;

	if (!empty(storeId) && !empty(medicineId) && !empty(medQty)) {
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var pbmRateDetails =" + ajaxobj.responseText);
					if (!empty(pbmRateDetails)) {
						setPBMRateDetails(prefix, pbmRateDetails);
					}else
						alert("Claim amount calculation error.");
				}
			}
		}
	}
}

function setPBMRateDetails(prefix, rateDet) {

	if (!empty(rateDet)) {
		document.getElementById(prefix+'_code_type').textContent = rateDet.code_type;
		document.getElementById(prefix+'_code').textContent = rateDet.item_code;

		document.getElementById(prefix+'_package_size').value = rateDet.package_size;
		document.getElementById(prefix+'_pkg_size_label').textContent = rateDet.package_size;

		var userUOMObj = document.getElementById(prefix+'_user_unit');
		setSelectedIndex(userUOMObj, rateDet.user_unit);

		document.getElementById(prefix+'_qty_in_stock').value = getAmount(rateDet.total_available_qty);

		document.getElementById(prefix+'_price_label').textContent = formatAmountValue(rateDet.rate);
		document.getElementById(prefix+'_price').value = formatAmountValue(rateDet.rate);
		document.getElementById(prefix+'_claim_item_qty').value = rateDet.medicine_quantity;
		document.getElementById(prefix+'_claim_item_disc').textContent = formatAmountValue(rateDet.discount);
		document.getElementById(prefix+'_claim_item_amount').textContent = formatAmountValue(rateDet.amount);
		document.getElementById(prefix+'_claim_item_pat_amount').textContent = formatAmountValue(rateDet.patient_amount);
		document.getElementById(prefix+'_claim_item_net_amount').value = formatAmountValue(rateDet.claim_net_amount);

		if (prefix == 's_d') {
			document.getElementById(prefix+'_claim_item_orig_rate').textContent = formatAmountValue(rateDet.rate);
			document.getElementById(prefix+'_claim_item_orig_amount').textContent = formatAmountValue(rateDet.amount);
			document.getElementById(prefix+'_claim_item_orig_net_amount').textContent = formatAmountValue(rateDet.claim_net_amount);
		}

		setSIEdited();

	}else {
		document.getElementById(prefix+'_code_type').textContent = '';
		document.getElementById(prefix+'_code').textContent = '';

		document.getElementById(prefix+'_package_size').value = 0;
		document.getElementById(prefix+'_pkg_size_label').textContent = 0;
		document.getElementById(prefix+'_user_unit').options.length = 0;

		var zero = formatAmountValue(0);

		document.getElementById(prefix+'_qty_in_stock').value = zero;
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

/* duplicate check is being done based
 1) on the item id if item type is
 		one of Medicine(Pharamcy), Test, Service, Doctor.
 2) on the item name if item type is
 		one of Medicine(non pharmacy), Non Hospital Items.
*/
function checkForSIDuplicates(prefix, id) {
	var itemTypes = document.getElementsByName("s_itemType");
	var dItemType = document.getElementById(prefix+"_itemType").value;
	var dItemName = document.getElementById(prefix+"_item_id").value;
	var itemNames = document.getElementsByName("s_item_id");
	var issuedItems = document.getElementsByName("s_issued");
	var delItem = document.getElementsByName("s_delItem");

	if (dItemType == 'Medicine') {
		dItemName = document.getElementById(prefix+'_itemName').value;
		itemNames = document.getElementsByName('s_item_name');
	}

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
	var prescribedId = getIndexedValue("s_item_prescribed_id", i);
	var qty_in_stock = getIndexedValue("s_qty_in_stock", i);
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
		//if (qty_in_stock == 0) cls = 'zero_qty'
		//else
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
	var isNew = getIndexedValue("s_item_prescribed_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		itemsEdited++;
		setIndexedValue("s_delItem", id, newDeleted);
		setIndexedValue("s_edited", id, "true");
		setSIRowStyle(id);
	}

	resetClaimTotals();
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

function getRouteOfAdministrations() {
	var itemId = document.getElementById('s_d_item_id').value;
	var itemName = document.getElementById('s_d_itemName').value;
	if (empty(itemId) && empty(itemName)) {
		itemId = 0;
	}
	var url = cpath+'/outpatient/OpPrescribeActionAjax.do?_method=getRoutesOfAdministrations';
		 url += '&item_id='+itemId;
		 url += '&item_name='+encodeURIComponent(itemName);
	var ajaxRequestForRoutes = YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: onGetRoutes,
			failure: onGetRoutesFailure
		}
	);
}

function onGetRoutes(response) {
	if (response.responseText != undefined) {
		var routes = eval('(' + response.responseText + ')');
		if (routes == null) {
			document.getElementById('s_d_medicine_route').length = 1;
			return ;
		}
		var routeIds = routes.route_id.split(",");
		var routeNames = routes.route_name.split(",");
		var medicine_route_el = document.getElementById('s_d_medicine_route');
		medicine_route_el.length = 1; // clear the previously populated list
		var len = 1;
		for (var i=0; i<routeIds.length; i++) {
			if (routeIds[i].trim() != '') {
				medicine_route_el.length = len+1;
				medicine_route_el.options[len].value = routeIds[i].trim();
				medicine_route_el.options[len].text = routeNames[i];
				len++;
			}
		}

		if (document.getElementById('s_d_medicine_route').options.length == 2)
			document.getElementById('s_d_medicine_route').selectedIndex = 1;
	}
}

function onGetRoutesFailure() {
}

function getEditRouteOfAdministrations() {
	var itemId = document.getElementById('s_ed_item_id').value;
	var itemName = document.getElementById('s_ed_itemName').value;
	if (empty(itemId) && empty(itemName)) {
		itemId = 0;
	}
	var url = cpath+'/outpatient/OpPrescribeActionAjax.do?_method=getRoutesOfAdministrations';
		 url += '&item_id='+itemId;
		 url += '&item_name='+encodeURIComponent(itemName);

	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if (reqObject.status == 200) {
			if (!empty(reqObject.responseText)) {
				onGetEditRoutes(reqObject);
			} else {
				onGetEditRoutesFailure();
			}
		}
	}
}

function onGetEditRoutes(response) {
	if (response.responseText != undefined) {
		var routes = eval('(' + response.responseText + ')');
		if (routes == null) {
			document.getElementById('s_ed_medicine_route').length = 1;
			return ;
		}
		var routeIds = routes.route_id.split(",");
		var routeNames = routes.route_name.split(",");
		var medicine_route_el = document.getElementById('s_ed_medicine_route');
		medicine_route_el.length = 1; // clear the previously populated list
		var len = 1;
		for (var i=0; i<routeIds.length; i++) {
			if (routeIds[i].trim() != '') {
				medicine_route_el.length = len+1;
				medicine_route_el.options[len].value = routeIds[i].trim();
				medicine_route_el.options[len].text = routeNames[i];
				len++;
			}
		}
		if (document.getElementById('s_ed_medicine_route').options.length == 2)
			document.getElementById('s_ed_medicine_route').selectedIndex = 1;
	}
}

function onGetEditRoutesFailure() {
}

function initEditSIDialog() {
	var dialogDiv = document.getElementById("editSIDialog");
	dialogDiv.style.display = 'block';
	editSIDialog = new YAHOO.widget.Dialog("editSIDialog",{
			width:"650px",
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

function editDialogGeneric() {
	document.getElementById('genericNameDisplayDialog').style.visibility = 'display';
	genericDialog = new YAHOO.widget.Dialog("genericNameDisplayDialog",
			{
				width:"500px",
				context : ["loadGenInfo", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	YAHOO.util.Event.addListener("genericNameCloseBtn", "click", closeGenericDialog, genericDialog, true);
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:closeGenericDialog, scope:genericDialog, correctScope:true } );
	genericDialog.cancelEvent.subscribe(closeGenericDialog);
	genericDialog.cfg.setProperty("keylisteners", kl);
	genericDialog.render();
}

function closeGenericDialog() {
	childDialog = null;
	this.hide();
}

function showGenericInfo(index, prefix, suffix, generic_code) {
	childDialog = genericDialog;
	var anchor = document.getElementById(prefix + "genericNameAnchor" + index + "_" + suffix);
	genericDialog.cfg.setProperty("context", [anchor, "tr", "tl"], false);
	genericDialog.show();
	if (generic_code != "") {
		var ajaxReqObject = new XMLHttpRequest();
		var url=cpath+"/outpatient/OpPrescribeActionAjax.do?_method=getGenericJSON&generic_code="+encodeURIComponent(generic_code);
		getResponseHandlerText(ajaxReqObject, handleGenericResponse, url);
	} else {
		document.getElementById('classification_name').innerHTML = '';
		document.getElementById('sub_classification_name').innerHTML = '';
		document.getElementById('standard_adult_dose').innerHTML = '';
		document.getElementById('criticality').innerHTML = '';
		document.getElementById('generic_name').innerHTML = '';
	}
}

/*
 * Response handler for the ajax call to retrieve generic details like classification and sub-classification
 */
function handleGenericResponse(responseText) {
	if (responseText==null) return;
	if (responseText=="") return;
	var genericDetails;
   eval("var genericDetails = " + responseText);			// response is an array of item batches
	if (genericDetails != null) {
		var genericId = genericDetails.generic_code;
		document.getElementById('classification_name').innerHTML = genericDetails.classificationName;
		if (genericDetails.sub_ClassificationName != null) {
			document.getElementById('sub_classification_name').innerHTML = genericDetails.sub_ClassificationName;
		}
		document.getElementById('standard_adult_dose').innerHTML = genericDetails.standard_adult_dose;
		document.getElementById('criticality').innerHTML = genericDetails.criticality;
		document.getElementById('gen_generic_name').innerHTML = genericDetails.gmaster_name;

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

	addSIDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addSIDialog.show();
	document.getElementById('s_d_itemName').focus();
	parentSIDialog = addSIDialog;
	return false;
}

function initSIDialog() {
	var dialogDiv = document.getElementById("addSIDialog");
	dialogDiv.style.display = 'block';
	addSIDialog = new YAHOO.widget.Dialog("addSIDialog",
			{	width:"650px",
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

