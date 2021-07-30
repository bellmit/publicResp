/*
 * Functions used by pages/stores/PatientIndent/addshow.jsp
 */

var oAutoComp;
var batchAutoComp;
var dialog;
var dlgForm;
var mainForm;
var gRowUnderEdit = undefined;
var gColIndexes = [];
var itemNamesArray = '';

YAHOO.util.Event.onContentReady("content", init);

function init() {

	var idx = 0;
	gColIndexes.medicine_name = idx++;
	if(isReturn) {
		gColIndexes.batch_no = idx++;
		gColIndexes.exp_dt = idx++;
	}
	gColIndexes.code_type = idx++;
	gColIndexes.item_code = idx++;
	gColIndexes.qty_required_display = idx++;
	gColIndexes.qty_received_display = idx++;
	if (showAvblQty)
		gColIndexes.qty_avbl_display = idx++;
	gColIndexes.qty_unit_display = idx++;
	gColIndexes.issue_base_unit = idx++;

	dlgForm = document.dlgForm;
	mainForm = document.patientIndentForm;

	initDialog();
	initPrescribingDoctorAutoComp();
	disableIndetDetails();
	initSearchDialog();
	onChangeStore();

	var visit_id = document.patientIndentForm.visit_id.value;
	var store_id  = document.patientIndentForm.indent_store.value;
	if (!stop_doctor_orders && !isReturn && !empty(visit_id) && !empty(store_id))
		getDoctorOrders(visit_id, store_id);
}

function getDoctorOrders(visit_id, store_id) {
	var url = cpath + "/stores/PatientIndentAdd.do?_method=getDoctorOrders&store_id=" + store_id + "&patient_id="+visit_id;
	YAHOO.util.Connect.asyncRequest('GET', url, {
		success: addDoctorOrdersToGrid,
		failure: null
	});
}

function addDoctorOrdersToGrid(response) {
	eval("var doctor_orders = " + response.responseText);
	if (doctor_orders.length > 0 && !confirm("There are prescriptions available for the patient. \n Do you want to auto fill prescriptions?")) return false;

	for (var i=0; i<doctor_orders.length; i++) {
		var row = insertRow();

		var record = doctor_orders[i];
		getElementByName(row, "medicine_id").value = record.medicine_id;
		getElementByName(row, "medicine_name").value = record.medicine_name;
		getElementByName(row, "category").value = record.category;
		getElementByName(row, "manf_name").value = record.manf_name;
		getElementByName(row, "package_type").value = record.package_type;
		getElementByName(row, "qty_avbl").value = record.qty_avbl;
		getElementByName(row, "qty_avbl_display").value = record.qty_avbl;
		getElementByName(row, "issue_base_unit").value = record.issue_base_unit;
		getElementByName(row, "issue_units").value = record.issue_units;
		getElementByName(row, "qty_unit").value = isForcePackageSelectionAppl() ? "P" : "I";
		getElementByName(row, "package_uom").value = record.package_uom;
		getElementByName(row, "qty_required_display").value = 1;
		getElementByName(row, "dispense_status").value = "O";

		// calculate the stored values in the row based on user display values
		calcRowStoredValues(row);

		// set the labels in the row based on hidden values
		rowHiddenToLabels(row, gColIndexes);
	}

}

function initItemAutoComplete() {

	document.dlgForm.medicine_name.value = '';
	if (oAutoComp != null) {
		oAutoComp.destroy();
	}

	var itemNames = [];
	var j = 0;

	var dataSource = null;

	if (isReturn) {
		//return indentable items
		var filteredList = filterZeroItemsInList(returnIndentableItems[document.patientIndentForm.indent_store.value],"qty_avbl")
		dataSource = new YAHOO.widget.DS_JSArray(filteredList);

	} else if (document.getElementById("eShowItemsA").checked) {
		// master items
		dataSource = new YAHOO.widget.DS_JSArray(jItemNames);

	} else {
		// storewise items
		var storeItems = jMedicineNames;
		dataSource = new YAHOO.widget.DS_JSArray(storeItems);
	}

	dataSource.responseSchema = {
		resultsList: "result",
		fields: [ {key : "cust_item_code_with_name"},{key : "medicine_name"},{key : "process_type"} ]
	};
	
	itemNamesArray = dataSource;

	oAutoComp = new YAHOO.widget.AutoComplete('medicine_name', 'item_dropdown', dataSource);
	oAutoComp.maxResultsDisplayed = 10;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oAutoComp.itemSelectEvent.subscribe(onSelectItem);
	oAutoComp._bItemSelected = true;
}

function initPrescribingDoctorAutoComp() {
	var ds = new YAHOO.util.LocalDataSource(doctorDetails,{ queryMatchContains : true });

	ds.responseSchema = {
		fields : ["DOCTOR_NAME", "DOCTOR_ID"]
	};

	var doctAutoComp = new YAHOO.widget.AutoComplete('prescribing_doctor_name', 'prescribing_doctor_name_drop_down', ds);
	doctAutoComp.maxResultsDisplayed = 20;
	doctAutoComp.allowBrowserAutocomplete = false;
	doctAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	doctAutoComp.typeAhead = false;
	doctAutoComp.useShadow = false;
	doctAutoComp.animVert = false;
	doctAutoComp.minQueryLength = 0;
	doctAutoComp.forceSelection = true;
}
//var batchAutoComp = null;

function initBatchNoAutoComplete(batchList) {
	
	if (batchAutoComp != undefined) {
		batchAutoComp.destroy();
	}
   if (!empty(batchAutoComp)) {
		batchAutoComp.destroy();
	}
	var sortedBatches = new Array();
	if (batchList) {
		for (var i=0; i<batchList.length; i++) {
			sortedBatches.push(batchList[i]);
		}
		sortedBatches.sort(function(b1, b2) {
				return (b1.batch_no<b2.batch_no) ? -1 : (b1.batch_no>b2.batch_no) ? 1 : 0;
		});
	}
	dataSource = new YAHOO.util.LocalDataSource(batchList)
	dataSource.queryMatchContainsWordBegining = true;
	dataSource.responseSchema = {fields : ["batch_no", "qty", "exp_dt" ,"item_batch_id"]};

	batchAutoComp = new YAHOO.widget.AutoComplete('batch_no', 'batch_dropdown', dataSource);
	batchAutoComp.allowBrowserAutocomplete = false;
	batchAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	batchAutoComp.useShadow = true;
	batchAutoComp.minQueryLength = 0;
	batchAutoComp.animVert = false;
	batchAutoComp.forceSelection = true;
	batchAutoComp.resultTypeList = false;
	batchAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		details = oResultData.batch_no + "/"  + formatExpiry(oResultData.exp_dt) +
					 "/(<b>" + oResultData.qty + "</b>)";
		return details;
	};
	if (sortedBatches.length == 1) {
		document.getElementById("batch_no").value = sortedBatches[0].batch_no;
		batchAutoComp._bItemSelected = true;
	}
	batchAutoComp.itemSelectEvent.subscribe(onSelectBatch);

}

function onChangeBatch() {
	var sel = document.getElementById("batch_no");
	if (sel) {
		var returnBatches = returnIndentableBatchItems[document.patientIndentForm.indent_store.value];
		var batchList = filterList(returnBatches, "medicine_name", document.getElementById("medicine_name").value);
		if (batchList && batchList.length > 0) {
			handleAjaxResponseForItemDetails(batchList[sel.selectedIndex]);
		}
	}
}

function onSelectBatch(sType,aArgs) {
	record = aArgs[2];
	document.getElementById("batch_exp_qty").value = formatExpiry(record.exp_dt)+"/"+record.qty;
	document.getElementById("item_batch_id").value = record.item_batch_id;
}

function formatExpiry(dateMSecs) {
	if (dateMSecs == null) return '(---)';
	var dateObj = new Date(dateMSecs);
	var dateStr = formatDate(dateObj, 'monyyyy', '-');
	return dateStr;
}

function onChangeStore() {

	if (document.getElementById("eShowItemsA").checked) {
		initItemAutoComplete();
		// nothing to do
	} else if (isReturn) {
		initItemAutoComplete();
	} else {
		// fetch the medicines of this store
			getMedicinesForStore(document.patientIndentForm.indent_store, initItemAutoComplete);
	}
}

function initDialog() {
	document.getElementById("addIndentDialog").style.display = 'block';
	dialog = new YAHOO.widget.Dialog("addIndentDialog", { width:"510px",
			context: ["indentItemListTab", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	
	dialog.render();
	dialog.cancelEvent.subscribe(closeDialog);
	subscribeEscKey();
}

function subscribeEscKey(){
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}

function closeDialog() {
	setDispenseStatusVisibility('hidden');
	dialog.hide();
}

function clearDialog(){
	document.dlgForm.medicine_name.value = '';
	document.getElementById("lbl_code_type").innerHTML = '';
	document.getElementById("lbl_item_code").innerHTML = '';
	document.getElementById("lbl_category").innerHTML = '';
	document.getElementById("lbl_manf_name").innerHTML = '';
	document.getElementById("lbl_package_type").innerHTML = '';
	if ( elementExists('lbl_qty_avbl_display') )
		document.getElementById("lbl_qty_avbl_display").innerHTML = '';
	document.getElementById("lbl_qty_received_display").innerHTML = 0;
	document.dlgForm.qty_required_display.value = '';
	document.dlgForm.qty_unit.value = -1;
	document.dlgForm.dispense_status.value = 'O';
	if(isReturn) {
		document.getElementById("batch_no").options.length = 0;
		document.getElementById("item_batch_id").value ='';
		document.getElementById("exp_dt").value ='';
	}
}

function onDialogSave() {
	if (!validateDialog())
		return false;

	if (gRowUnderEdit == undefined) {
		// add mode
		addDialogToGrid();
		showNextDialog();

	} else {
		// edit mode
		if(isReturn) {
			var itemBatchId = document.getElementById("item_batch_id").value;
			var itemExpDt = document.getElementById("exp_dt").value;
			if(itemBatchId == '' && itemExpDt == '') {
				var medicineName = document.getElementById("medicine_name").value;
				var batchList = filterBatchList(medicineName);
				var batchEl = document.getElementById("batch_no");
				var selectedItem = batchEl.selectedIndex;
				document.getElementById("item_batch_id").value = batchList[selectedItem].item_batch_id;
				document.getElementById("exp_dt").value = formatExpiry(batchList[selectedItem].exp_dt);
			}
		}
		saveDialogToGrid();
		closeDialog();
	}
}

function showAddDialog(imgObj) {
	if (!visitSelected && !patIndentNoSelected) {
		showMessage ("js.sales.issues.selectpatient.beforeadditems");
		document.patientSearch.patient_id.focus();
		return false;
	}

	gRowUnderEdit = undefined;
	clearDialog();
	dialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
	dialog.show();
	document.dlgForm.medicine_name.focus();
}

function onSelectItem(sType,aArgs) {
	var itemname = aArgs[2][1];
	document.getElementById("medicine_name").value = itemname;
	var itemId = document.getElementById("medicine_id").value;
	var store_id  = document.patientIndentForm.indent_store.value;

	if (itemname == '')
		return;

	if (isReturn) {
		var visit_id = document.patientIndentForm.visit_id.value;
/*		var url = 'PatientIndentAddReturn.do?_method=getMedicineBatchDetail&patient_id='+visit_id
			+'&item_id='+itemId;
		Ajax.get(url, function(data, status) {
				var item = eval("(" + data + ")");
				handleAjaxResponseForBatchNoDetails(item);
				}); */
		var process_type = aArgs[2][1];
		var returns = returnIndentableItems[document.patientIndentForm.indent_store.value];
		var batchLoaded = loadBatch();
		if (!batchLoaded) {
			for ( var i = 0;i<returns.length;i++){
				if ( document.getElementById("medicine_name").value == returns[i].medicine_name ) {
					var item = returns[i];
					handleAjaxResponseForItemDetails(item);
					break;
				}
			}
		}
	} else {
		var url = 'PatientIndentUtils.do?_method=getItemDetails&itemname='+encodeURIComponent(itemname)
			+'&store_id='+store_id;
		Ajax.get(url, function(data, status) {
				var item = eval("(" + data + ")");
				handleAjaxResponseForItemDetails(item);
				});
	}
}

function loadBatch() {
	var returnBatches = returnIndentableBatchItems[document.patientIndentForm.indent_store.value];
	var batchList = filterList(returnBatches, "medicine_name", document.getElementById("medicine_name").value);
	var batchEl = document.getElementById("batch_no");
	if (batchList && batchList.length > 0 && batchEl) {
		loadSelectBox(batchEl, batchList, "batch_no", "batch_no");
		handleAjaxResponseForItemDetails(batchList[0]);
		return true;
	}
	return false;
}

function populateBatchList(medicineName) {
	var batchList = filterBatchList(medicineName);
	var batchEl = document.getElementById("batch_no");
	loadBatchList(batchEl, batchList);
}

function filterBatchList(medicineName) {
	var returnBatches = returnIndentableBatchItems[document.patientIndentForm.indent_store.value];
	var batchList = filterList(returnBatches, "medicine_name", medicineName);
	return batchList;
}

function loadBatchList(batchEl, batchList) {
	if (batchList && batchList.length > 0 && batchEl) {
		loadSelectBox(batchEl, batchList, "batch_no", "batch_no");
		return true;
	}
	return false;
}

function handleAjaxResponseForItemDetails(item){
	if (item != null && item != '') {
		itemToDialog(item);
	}
}

function handleAjaxResponseForBatchNoDetails(item){
	if (item != null && item != '') {
		initBatchNoAutoComplete(item);
	}
}

/*
 * Save the contents of the item object into dialog input fields and labels.
 */
function itemToDialog(item) {

	loadUOM(item.issue_units, item.package_uom);
	setSelectedIndex(document.dlgForm.qty_unit, isForcePackageSelectionAppl() ? "P" : "I");

	item.qty_avbl_display = item.qty_avbl;
	item.qty_received_display = 0;
	if(isReturn) {
		if (isInteger(item.exp_dt)) {
			item.exp_dt=formatExpiry(item.exp_dt);
		}
	}
	objectToForm(item, dlgForm);
	formToLabelIds(dlgForm, 'lbl_', 20);
	if ( showAvblQty )
		onChangeUOM();

}

function loadUOM(issueUnits, packageUOM) {
	document.dlgForm.qty_unit.length = issueUnits != packageUOM ? 2 : 1;
	document.dlgForm.qty_unit.options[0].text = issueUnits;
	document.dlgForm.qty_unit.options[0].value = 'I';
	if (issueUnits != packageUOM) {
		document.dlgForm.qty_unit.options[1].text = packageUOM;
		document.dlgForm.qty_unit.options[1].value = 'P';
	}
}

function onChangeUOM() {
	// when the dialog UOM changes, we need to re-calc the available qty in the selected UOM
	if (dlgForm.qty_unit.value == 'I') {
		dlgForm.qty_avbl_display.value = dlgForm.qty_avbl.value;
	} else {
		var qtyAvbl = parseFloat(dlgForm.qty_avbl.value);
		var pkgSize = parseFloat(dlgForm.issue_base_unit.value);
		dlgForm.qty_avbl_display.value = (qtyAvbl/pkgSize).toFixed(2);
	}
	document.getElementById("lbl_qty_avbl_display").innerHTML =
		formatAmountObj(dlgForm.qty_avbl_display, true);
}

/*
 * Called when user chooses Add in the add dialog (not edit)
 */
function addDialogToGrid() {
	var row = insertRow();
	// copy the dialog form to the hidden values in the row
	formToHidden(dlgForm, row);
	// calculate the stored values in the row based on user display values
	calcRowStoredValues(row);
	// set the labels in the row based on hidden values
	rowHiddenToLabels(row, gColIndexes);
}

function calcRowStoredValues(row) {
	var qtyUnit = getElementByName(row, "qty_unit").value;

	var qtyReq = parseFloat(getElementByName(row, "qty_required_display").value);
	var qtyRec = parseFloat(getElementByName(row, "qty_received_display").value);
	var qtyAvbl = parseFloat(getElementByName(row, "qty_avbl_display").value);
	var qtyUnitDisplay = getElementByName(row, "issue_units").value;
	var qtyCodeType = getElementByName(row, "code_type").value;
	var qtyItemCode = getElementByName(row, "item_code").value;
	//var batchNo = getElementByName(row, "batch_no").value;

	if (qtyUnit == 'P') {
		var pkgSize = parseFloat(getElementByName(row, "issue_base_unit").value);
		qtyReq = qtyReq * pkgSize;
		qtyRec = qtyRec * pkgSize;
		qtyAvbl = qtyAvbl* pkgSize;
		qtyUnitDisplay = getElementByName(row, "package_uom").value;
	}
	getElementByName(row,"code_type").value = qtyCodeType;
	getElementByName(row,"item_code").value = qtyItemCode;
	getElementByName(row,"qty_required").value = qtyReq;
	getElementByName(row,"qty_received").value = qtyRec;
	getElementByName(row,"qty_avbl").value = qtyAvbl;
	getElementByName(row,"qty_unit_display").value = qtyUnitDisplay;

}

function showEditDialog(btnObject){
	var row = getThisRow(btnObject);
	gRowUnderEdit = row;
	clearDialog();
	if(isReturn) {
		var medicineName = getElementByName(row,"medicine_name").value;
		populateBatchList(medicineName);
	}
	rowToDialog(row);
	if (getElementByName(row,"editable").value == 'false')
		disableDialog(row);
	dialog.cfg.setProperty("context", [btnObject, "tr", "br"], false);
	dialog.show();
	document.dlgForm.medicine_name.focus();
}

function rowToDialog(row) {
	loadUOM(getElementByName(row,"issue_units").value, getElementByName(row,"package_uom").value);
	hiddenToForm(row, dlgForm);
	formToLabelIds(dlgForm, 'lbl_', 20);
}

function saveDialogToGrid() {
	var row = gRowUnderEdit;
	// copy the dialog form to the hidden values in the row
	formToHidden(dlgForm, row);
	// calculate the stored values in the row based on user display values
	calcRowStoredValues(row);
	// set the labels in the row based on hidden values
	rowHiddenToLabels(row, gColIndexes);
}

function insertRow(){
	var table = document.getElementById("indentItemListTab");
	var numRows = table.rows.length;
	var templateRow = table.rows[numRows-1];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.className = "added";
	return row;
}

function showNextDialog() {
	showAddDialog(document.patientIndentForm.btnAddItem);
}

function disableIndetDetails(){
	document.patientIndentForm.indent_store.disabled = (indentStatus != '' && indentStatus != 'O');
	document.patientIndentForm.status.disabled = (indentStatus != '' && indentStatus != 'O');
	document.patientIndentForm.remarks.disabled = (indentStatus != '' && indentStatus != 'O');
	document.patientIndentForm.expected_date_dt.disabled = (indentStatus != '' && indentStatus != 'O');
	document.patientIndentForm.expected_date_tm.disabled = (indentStatus != '' && indentStatus != 'O');
	document.getElementById("save").disabled =  !storeExists || ( method == 'view' || !validUser || (indentDispenseStatus != '' && ( indentDispenseStatus == 'C' && !itemDispenseStatuseditable) )) ;
	if(document.patientIndentForm.prescribing_doctor_name){
		document.patientIndentForm.prescribing_doctor_name.disabled = (indentStatus != '' && indentStatus != 'O');
	}
}

function raiseIndent(){
	var valid = true;

	valid = valid && validate();

	if (valid) {
		document.patientIndentForm.action = cpath + ( !patIndentNoSelected ? '/stores/PatientIndentAdd'+( isReturn ? 'Return' : '' )+'.do?_method=create' : '/stores/PatientIndentEdit'+( isReturn ? 'Return' : '' )+'.do?_method=update' );
		document.patientIndentForm.submit();
	}
	return true;
}

function validate(){
	if ( document.patientIndentForm.indent_store.value == '' ){
		showMessage("js.sales.issues.selectindentstore");
		document.patientIndentForm.indent_store.focus();
		return false;
	}

	if ( empty(document.patientIndentForm.expected_date_dt.value) ){
		showMessage("js.sales.issues.required.expecteddate");
		document.patientIndentForm.expected_date_dt.focus();
		return false;
	}

	if ( empty(document.patientIndentForm.expected_date_tm.value) ){
		showMessage("js.sales.issues.required.expectedtime");
		document.patientIndentForm.expected_date_tm.focus();
		return false;
	}

	if ( !document.patientIndentForm.expected_date_dt.disabled ) {//finalized indent doent allow date edit so no need to validate
		var d = getDateFromField(document.patientIndentForm.expected_date_dt);
		var time = document.patientIndentForm.expected_date_tm.value.split(":");
		d.setHours(time[0]);
		d.setMinutes(time[1]);
		var errorStr = validateDateTime(d, 'future');
		if (errorStr != null) {
			alert(errorStr);
			return false;
		}
	}

	if ( document.getElementById("indentItemListTab").rows.length == 2 ) {
		showMessage("js.sales.issues.additem.indent");
		return false;
	}

	if ( document.patientIndentForm.status.value == 'O' ) {
		if ( !confirm("Indent is to be saved in open status.Are you sure?") )
			return false;
	}
	if (isReturn) {
		if ( document.patientIndentForm.status.value == 'F' ) {
			var els = document.patientIndentForm.batch_no;
			 for(var i=0; i<els.length-1 ;i++) {
			  if( els[i].value  == '') {
			  	showMessage("js.sales.issues.selectbatch");
				return false;
			  }
			}
		}
	}

	return true;
}

function disableDialog(row){

	var inputs = document.getElementById("addIndentItemsTable").getElementsByTagName('input');
	var itemDispenseStatus = getElementByName(row,"itemDispenseStatusOnload").value;
	for(var i=0; i<inputs.length; ++i)
		inputs[i].disabled=true;
	document.dlgForm.dispense_status.disabled = ( !itemDispenseStatuseditable || itemDispenseStatus == 'C' );
	document.dlgForm.qty_unit.disabled = true;
	if (isReturn) {
		document.getElementById("batch_no").disabled =true;
	}
	if ( itemDispenseStatuseditable ) {
		//Item level dispense status is editable for Finalized indents
		setDispenseStatusVisibility('visible');
	}

}

function setDispenseStatusVisibility(visibility) {
	document.getElementById("dispense_status_td").style.visibility = visibility;
	document.getElementById("dispense_status_label_td").style.visibility = visibility;
}

function validateDialog() {
	if (empty(document.dlgForm.medicine_name.value)){
		showMessage("js.sales.issues.selectitem");
		document.dlgForm.medicine_name.focus();
		return false;
	}

	if (empty(trim(document.dlgForm.qty_required_display.value))){
		showMessage("js.sales.issues.enter.requiredquantity");
		document.dlgForm.qty_required_display.value = '';
		document.dlgForm.qty_required_display.focus();
		return false;
	}

	if ( document.dlgForm.qty_required_display.value == 0 ){
		showMessage("js.sales.issues.requiredquantity.notzero");
		document.dlgForm.qty_required_display.focus();
		return false;
	}

	if (allowDecimalsForQty == 'Y') {
		if (!validateDecimal(document.dlgForm.qty_required_display, getString("js.sales.issues.enter.validquantity"), 2))
			return false;

	} else {
		if (!validateInteger(document.dlgForm.qty_required_display, getString("js.sales.issues.enter.validquantity"), 2))
			return false;
	}
	
	var msg = "";
	var medicineName = document.getElementById("medicine_name").value;
	if (gStockNegative == 'W' && ( (parseFloat(document.dlgForm.qty_required_display.value) > 0 && parseFloat(document.dlgForm.qty_avbl_display.value) <= 0) || parseFloat(document.dlgForm.qty_avbl_display.value) < parseFloat(document.dlgForm.qty_required_display.value))) {
		msg += getString("js.sales.issues.storesuserissues.warning.insufficientquantity")+" '"+ medicineName + "': " + parseFloat(document.dlgForm.qty_required_display.value).toFixed(2);
		msg += "\n";
		alert(msg);
	}
	
	if(isReturn) {
		if (empty(document.dlgForm.batch_no.value)){
		showMessage("js.sales.issues.batchno");
		document.dlgForm.batch_no.focus();
		return false;
	}

	}
	var itemListTable = document.getElementById("indentItemListTab");
	var numItems = itemListTable.rows.length  - 1;
	var editedRowIndex =  ( gRowUnderEdit == undefined ) ? undefined : gRowUnderEdit.rowIndex ;

	for (var k=1; k < numItems; k++) {
		if (k == editedRowIndex) {
			// currently being edited row, skip this.
			continue;
		}
		var rowObj = itemListTable.rows[k];
		if(isReturn) {
			// check medicine name and batch for duplicate entry .It will allow same medicine with diff batch no.
			if (getElementByName(rowObj,'medicine_id').value == dlgForm.medicine_id.value && getElementByName(rowObj,'batch_no').value == dlgForm.batch_no.value) {
				showMessage("js.sales.issues.duplicateentry");
				return false;
			}
		} else if (getElementByName(rowObj,'medicine_id').value == dlgForm.medicine_id.value) {
				showMessage("js.sales.issues.duplicateentry");
				return false;
		}
	}
	// only for returns, restrict the indent quantity to available quantity.
	if (document.getElementsByName("indent_type")[0].value == 'R'
			&& document.dlgForm.qty_required_display.value > parseFloat(document.dlgForm.qty_avbl_display.value) ){
		alert(getString("js.sales.issues.requiredquantity.notexceed")+document.dlgForm.qty_avbl_display.value);
		document.dlgForm.qty_required_display.value = '';
		document.dlgForm.qty_required_display.focus();
		return false;
	}

	var processTypes = document.getElementsByName("process_type");
	for ( var i =0;i< processTypes.length;i++ ) {
		if ( !empty(processTypes[i].value) &&  processTypes[i].value != document.dlgForm.process_type.value ) {
			alert((processTypes[i].value == 'I' ? 'Issued' : 'Sold') +" "+ getString("js.sales.issues.indentnotmix")+" "+( processTypes[i].value == 'S' ? 'Issued' : 'Sold' ) +' indent');
			return false;
		}
	}
	return true;
}

function deleteIndentItem(delObj) {
	var row = getThisRow(delObj);

	if ( empty(getElementByName(row,"patient_indent_no").value) )
		row.parentNode.removeChild(row);
	else
		markForDeletion(row);
}

function markForDeletion(row) {
	getElementByName(row,"deleted").value = getElementByName(row,"deleted").value == 'Y' ? 'N' : 'Y';
	var links = row.getElementsByTagName("a");
	var trashImg = links[0].getElementsByTagName("img")[0];
	var editImg = links[1].getElementsByTagName("img")[0];
	var link = links[1];
	if ( getElementByName(row,"deleted").value == 'Y') {
		trashImg.src = cpath+"/icons/undo_delete.gif";
		editImg.src = cpath+"/icons/Edit1.png";
		link.setAttribute("onclick","");
	}
	else {
		trashImg.src = cpath+"/icons/delete.gif";
		editImg.src = cpath+"/icons/Edit.png";
		link.setAttribute("onclick","showEditDialog(this);");
	}
}

function elementExists(elementId){
	var element =  document.getElementById(elementId);
	return (typeof(element) != 'undefined' && element != null)
}

function isForcePackageSelectionAppl(){
	var storeId = document.patientIndentForm.indent_store.value ;
	for ( var i = 0,j = storesJsonList.length;i<j;i+= 1){
		if ( storeId == storesJsonList[i].dept_id && storesJsonList[i].sale_unit == 'P' ){
			return true;
		}
	}
	return false;
}

function initSearchDialog(){
	initGenericSearchDialog();
	clearFields();
	initAutoNameSearch();
	initAutoGenericName();
}

function initGenericSearchDialog() {
	document.getElementById("genericSearchDialog").style.display = 'block';
	genericSearchDialog = new YAHOO.widget.Dialog("genericSearchDialog",
			{
				width:"510px",
				context : ["searchGen", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	var escKeyListener = new YAHOO.util.KeyListener("genericSearchDialog", { keys:27 },
	                                              { fn:closeGenericSearchDialog} );
	genericSearchDialog.cfg.queueProperty("keylisteners", escKeyListener);
	genericSearchDialog.render();
}

function initAutoNameSearch(){
	if (AutoComp != undefined) {
		AutoComp.destroy();
	}

	var dataSource = new YAHOO.util.XHRDataSource(cpath+"/pages/stores/MedicineSalesAjax.do");
    dataSource.scriptQueryAppend = "method=search";
    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

    dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "cust_item_code_with_name"},
					{key : "genericname"},
					{key : "medicinename"}
				 ]
	};

    itemNamesArray = dataSource;
    var AutoComp = new YAHOO.widget.AutoComplete('medicinename','medicinename_dropdown', dataSource);
    AutoComp.allowBrowserAutocomplete = false;
    AutoComp.prehighlightClassName = "yui-ac-prehighlight";
    AutoComp.typeAhead = false;
    AutoComp.useShadow = false;
    AutoComp.animVert = false;
    AutoComp.minQueryLength = 1;
    AutoComp.forceSelection = true;
    AutoComp.filterResults = Insta.queryMatchWordStartsWith;

	AutoComp.formatResult = Insta.autoHighlight;
	AutoComp.itemSelectEvent.subscribe(onSelectMedicineName = function (type, args){
		document.getElementById('generic_name').value = args[2][1];
		document.getElementById('medicinename').value = args[2][2];
	});
}

function initAutoGenericName(){
	if (AutoComplete != undefined) {
		AutoComplete.destroy();
	}

	dataSource = new YAHOO.util.LocalDataSource({result : genericNames});
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "generic_name"} ]
		};

	//dataSource = new YAHOO.widget.DS_JSArray(genericNames);
	var AutoComplete = new YAHOO.widget.AutoComplete('generic_name','generic_name_dropdown', dataSource);

    AutoComplete.useShadow = true;
    AutoComplete.minQueryLength = 0;
    AutoComplete.allowBrowserAutocomplete = false;
    AutoComplete.filterResults = Insta.queryMatchWordStartsWith;
	AutoComplete.formatResult = Insta.autoHighlightWordBeginnings;
	AutoComplete.maxResultsDisplayed = 20;
	AutoComplete.itemSelectEvent.subscribe(onChangeGenericName = function (type, args){
		document.getElementById('medicinename').value = '';
	});
}


function clearFields(){
	document.getElementById("medicine_name").value = '';
	document.getElementById("generic_name").value = '';
	var results = document.getElementById("results");
	results.length = 1;
}

function showSearchWindow(){
	var id = 1;
	var button = document.getElementById("searchGen");
	genericSearchDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	genericSearchDialog.show();
	clearFeilds();
}

function closeGenericSearchDialog() {
	genericSearchDialog.hide();
	// set focus to the parent's field
	setFocus ();
	return false;
}

function setFocus () {
	document.getElementById("medicine_name").focus();
}

function searchMedicine(){
	var dialogId = document.getElementById("dialogId").value;
	var medName = document.getElementById("medicinename").value;
	var genericName = document.getElementById("generic_name").value;
	var storeId = document.patientIndentForm.indent_store.value;
	var saleType = isReturn ? "returns" : '';
	if (medName == '' && genericName == ''){
		return false;
	}

	var ajaxReqObject = newXMLHttpRequest();
	//var url="PatientIndentAjax.do?method=getEquivalentMedicinesList&allStores=true&medicineName="+
	//encodeURIComponent(medName)+"&storeId="+storeId+"&saleType="+saleType+"&genericName="+encodeURIComponent(genericName);

	var url=cpath+"/indent/getItemList.json?allStores=false&medicineName="+
	encodeURIComponent(medName)+"&storeId="+storeId+"&saleType="+saleType+"&genericName="+encodeURIComponent(genericName);

	ajaxReqObject.open("GET",url.toString(), false);
	ajaxReqObject.send(null);

	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			eval("var medDetails = "+ajaxReqObject.responseText);
			displaySearchResults(medDetails);
		}
	}
}

function displaySearchResults(medDetails){
	var results = document.getElementById("results");
	loadSelectBox(results, medDetails, "display_name", "name_qty", "Select Item");
	if(results.options.length >0 ){
		for(var i = results.options.length - 1; i >= 0; i--){
			results.options[i].title = results.options[i].text;
			results.options[i].value = results.options[i].value;
		}
	}
	results.selectedIndex = 0;
}

function setMedicine() {
	var selectedMed = document.getElementById("results").value;
	if (selectedMed == ''){
		showMessage("js.sales.issues.noitemselected");
		return false;
	}
	
	selectedMed = selectedMed.split('@');
	var med_qty = selectedMed[0];
	var med_name = selectedMed[1];
	
	if (gStockNegative == 'D' && med_qty <= 0){
		showMessage("js.sales.issues.nostockavailable.thisitemintheselectedstore");
		return false;
	}
		
	document.getElementById("medicine_name").value = med_name;
	onSelectItemFromGenericDialog(med_name);
	clearFields();
	genericSearchDialog.hide();
}

function clearFeilds() {
	document.patientIndentForm.medicinename.value='';
	document.patientIndentForm.generic_name.value='';
    document.patientIndentForm.results.length=0;
}

function onSelectItemFromGenericDialog(itemname) {
	//var itemname = aArgs[2][1];
	document.getElementById("medicine_name").value = itemname;
	var itemId = document.getElementById("medicine_id").value;
	var store_id  = document.patientIndentForm.indent_store.value;

	if (itemname == '')
		return;

	if (isReturn) {
		var visit_id = document.patientIndentForm.visit_id.value;
/*		var url = 'PatientIndentAddReturn.do?_method=getMedicineBatchDetail&patient_id='+visit_id
			+'&item_id='+itemId;
		Ajax.get(url, function(data, status) {
				var item = eval("(" + data + ")");
				handleAjaxResponseForBatchNoDetails(item);
				}); */
		//var process_type = aArgs[2][1];
		var returns = returnIndentableItems[document.patientIndentForm.indent_store.value];
		var batchLoaded = loadBatch();
		if (!batchLoaded) {
			for ( var i = 0;i<returns.length;i++){
				if ( document.getElementById("medicine_name").value == returns[i].medicine_name ) {
					var item = returns[i];
					handleAjaxResponseForItemDetails(item);
					break;
				}
			}
		}
	} else {
		var url = 'PatientIndentUtils.do?_method=getItemDetails&itemname='+encodeURIComponent(itemname)
			+'&store_id='+store_id;
		Ajax.get(url, function(data, status) {
				var item = eval("(" + data + ")");
				handleAjaxResponseForItemDetails(item);
				});
	}
}

